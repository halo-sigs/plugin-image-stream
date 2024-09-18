<script lang="ts" setup>
import { useConfig } from '@/composables/use-config'
import { useImageControl } from '@/composables/use-image-control'
import { DEFAULT_PER_PAGE } from '@/constants'
import { VButton, VLoading } from '@halo-dev/components'
import type { AttachmentLike } from '@halo-dev/console-shared'
import { useQuery } from '@tanstack/vue-query'
import { createApi } from 'unsplash-js'
import type { Basic as Photo } from 'unsplash-js/dist/methods/photos/types'
import type { Basic as Topic } from 'unsplash-js/dist/methods/topics/types'
import { computed, ref, watch } from 'vue'
import DownloadButton from '../base/DownloadButton.vue'
import DownloadModeSwitcher from '../base/DownloadModeSwitcher.vue'
import ImageCard from '../base/ImageCard.vue'
import ImageLayout from '../base/ImageLayout.vue'
import axios from 'axios'

const unsplash = createApi({ accessKey: import.meta.env.VITE_UNSPLASH_KEY })

const unsplashApiClient = axios.create({
  baseURL: '/apis/unsplash.halo.run/v1alpha1'
})

const props = withDefaults(
  defineProps<{
    selected: AttachmentLike[]
    min?: number
    max?: number
  }>(),
  {
    selected: () => [],
    min: undefined,
    max: undefined
  }
)

const emit = defineEmits<{
  (event: 'update:selected', attachments: AttachmentLike[]): void
}>()

const selectedTopicId = ref()
const images = ref<Photo[]>([])
const page = ref(1)
const keyword = ref('')

const { isDownloadMode } = useConfig()

const { data: topics } = useQuery<Topic[] | undefined>({
  queryKey: ['plugin:image-stream:unsplash:topics'],
  queryFn: async () => {
    const { data } = await unsplashApiClient.get(`/topics`, {
      params: {
        page: 1,
        per_page: 100,
        order_by: 'featured'
      }
    })
    return data || []
  },
  onSuccess(data) {
    if (data?.length) {
      selectedTopicId.value = data[0].id
    }
  }
})

watch(
  () => selectedTopicId.value,
  () => {
    selectedImages.value = new Set()
    images.value = []
    page.value = 1
  }
)

const { isFetching } = useQuery({
  queryKey: ['plugin:image-stream:unsplash:images', keyword, selectedTopicId, page],
  queryFn: async () => {
    if (keyword.value) {
      const { data } = await unsplashApiClient.get(`/photos/-/search`, {
        params: {
          page: page.value,
          per_page: DEFAULT_PER_PAGE,
          query: keyword.value
        }
      })

      return data || []
    }

    if (!selectedTopicId.value) {
      return []
    }

    const { data } = await unsplashApiClient.get(`/topics/${selectedTopicId.value}/photos`, {
      params: {
        topic_id_or_slug: selectedTopicId.value,
        page: page.value,
        per_page: DEFAULT_PER_PAGE
      }
    })

    return data || []
  },
  onSuccess(data) {
    images.value = [...images.value, ...data]
  },
  keepPreviousData: true
})

const {
  selectedImages,
  finalSelectedUrls,
  isBound,
  isDisabled,
  downloading,
  handleDownloadImage,
  handleSelect
} = useImageControl<Photo>(
  'unsplash',
  images,
  (image) => image.id,
  (image) => image.urls.raw,
  (image) => image.alt_description || '',
  (image) => {
    return `${image.alt_description?.toLowerCase().replace(/\s+/g, '-') || image.id}.jpg`
  },
  props.max
)

watch(
  () => finalSelectedUrls.value,
  (value) => {
    emit('update:selected', Array.from(value))
  },
  {
    deep: true
  }
)

watch(
  () => keyword.value,
  () => {
    images.value = []
    selectedImages.value = new Set()
    page.value = 1
  }
)
</script>
<template>
  <div class="mb-4 flex flex-wrap items-center justify-between gap-4">
    <div class="flex flex-wrap items-center gap-3">
      <SearchInput v-model="keyword" />
      <FilterDropdown
        v-if="!keyword"
        v-model="selectedTopicId"
        label="类别"
        :items="[
          ...(topics?.map((topic) => {
            return {
              label: `${topic.title}(${topic.total_photos})`,
              value: topic.id
            }
          }) || [])
        ]"
      />
    </div>
    <div class="flex flex-none items-center gap-2">
      <DownloadButton
        v-if="isDownloadMode && selectedImages.size"
        :loading="downloading"
        @click="handleDownloadImage"
      />
      <DownloadModeSwitcher />
    </div>
  </div>

  <VLoading v-if="isFetching && images.length === 0" />

  <div v-else>
    <ImageLayout>
      <ImageCard
        v-for="image in images"
        :key="image.id"
        :checked="selectedImages.has(image)"
        :disabled="isDisabled(image)"
        :bound="isBound(image)"
        :url="image.urls.thumb"
        :html-url="image.links.html"
        :author="{
          htmlUrl: image.user.links.html,
          name: image.user.name,
          avatar: image.user.profile_image.medium,
          bio: image.user.bio || undefined
        }"
        @click.stop="handleSelect(image)"
      />
    </ImageLayout>

    <div v-if="images.length > 0" class="mt-4 flex items-center justify-center">
      <VButton :loading="isFetching" type="secondary" @click="page++">
        {{ isFetching ? '加载中...' : '加载更多' }}
      </VButton>
    </div>
  </div>
</template>

<style scoped>
.topics::-webkit-scrollbar-track-piece {
  background-color: #f8f8f8;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

.topics::-webkit-scrollbar {
  width: 5px;
  height: 5px;
}

.topics::-webkit-scrollbar-thumb {
  background-color: #f2eaea;
  background-clip: padding-box;
  -webkit-border-radius: 2em;
  -moz-border-radius: 2em;
  border-radius: 2em;
}

.topics::-webkit-scrollbar-thumb:hover {
  background-color: #bbb;
}
</style>
