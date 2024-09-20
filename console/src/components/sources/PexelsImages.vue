<script setup lang="ts">
import { PexelsV1alpha1Api } from '@/api/generated'
import { useConfig } from '@/composables/use-config'
import { useImageControl } from '@/composables/use-image-control'
import { DEFAULT_PER_PAGE } from '@/constants'
import type { PexelsPhoto, PexelsPhotoResponse } from '@/types'
import { VButton, VLoading } from '@halo-dev/components'
import type { AttachmentLike } from '@halo-dev/console-shared'
import { useQuery } from '@tanstack/vue-query'
import axios from 'axios'
import { ref, watch } from 'vue'
import DownloadButton from '../base/DownloadButton.vue'
import DownloadModeSwitcher from '../base/DownloadModeSwitcher.vue'
import ImageCard from '../base/ImageCard.vue'
import ImageLayout from '../base/ImageLayout.vue'

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

const pexelsAxios = axios.create({
  baseURL: ''
})

const pexelsApiClient = new PexelsV1alpha1Api(undefined, pexelsAxios.defaults.baseURL, pexelsAxios)

const { isDownloadMode } = useConfig()

const images = ref<PexelsPhoto[]>([])
const page = ref(1)
const keyword = ref('')

const { isFetching } = useQuery({
  queryKey: ['plugin:image-stream:pexels:images', page, keyword],
  queryFn: async () => {
    if (keyword.value) {
      const { data } = await pexelsApiClient.searchPexPhotos({
        perPage: DEFAULT_PER_PAGE,
        page: page.value,
        query: keyword.value
      })
      return data as PexelsPhotoResponse
    }
    const { data } = await pexelsApiClient.curatedPexPhotos({
      perPage: DEFAULT_PER_PAGE,
      page: page.value
    })
    return data as PexelsPhotoResponse
  },
  onSuccess(data) {
    images.value = [...images.value, ...data.photos]
  }
})

watch(
  () => keyword.value,
  () => {
    images.value = []
    page.value = 1
  }
)

const {
  selectedImages,
  finalSelectedUrls,
  isBound,
  isDisabled,
  downloading,
  handleDownloadImage,
  handleSelect
} = useImageControl<PexelsPhoto>(
  'pexels',
  images,
  (image) => image.id + '',
  (image) => image.src.original,
  (image) => image.alt,
  (image) => image.id + '',
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
</script>
<template>
  <div class="mb-4 flex flex-wrap items-center justify-between gap-4">
    <div class="flex flex-wrap items-center gap-3">
      <SearchInput v-model="keyword" />
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
        :url="image.src.tiny"
        :html-url="image.url"
        :author="{
          name: image.photographer,
          htmlUrl: image.photographer_url
        }"
        @click="handleSelect(image)"
      />
    </ImageLayout>

    <div v-if="images.length" class="mt-4 flex items-center justify-center">
      <VButton :loading="isFetching" type="secondary" @click="page++">
        {{ isFetching ? '加载中...' : '加载更多' }}
      </VButton>
    </div>
  </div>
</template>
