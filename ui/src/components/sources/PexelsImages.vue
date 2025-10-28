<script setup lang="ts">
import { PexelsV1alpha1Api } from '@/api/generated'
import { useConfig } from '@/composables/use-config'
import { useImageControl } from '@/composables/use-image-control'
import { DEFAULT_PER_PAGE } from '@/constants'
import type { PexelsPhoto, PexelsPhotoResponse } from '@/types'
import { getFileNameFromUrl } from '@/utils'
import { Toast, VButton, VLoading } from '@halo-dev/components'
import type { AttachmentLike } from '@halo-dev/console-shared'
import { useQuery } from '@tanstack/vue-query'
import axios, { AxiosError } from 'axios'
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
  retry: 0,
  onSuccess(data) {
    images.value = [...images.value, ...data.photos]
  },
  onError(e) {
    if (e instanceof AxiosError) {
      Toast.error(e.response?.data)
      return
    }
    if (e instanceof Error) {
      Toast.error(e.message)
    }
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
} = useImageControl<PexelsPhoto>('pexels', images, {
  idHandler: (image) => image.id + '',
  urlHandler: (image) => image.src.original,
  altHandler: (image) => image.alt,
  fileNameHandler: (image) => getFileNameFromUrl(image.src.original) || `${image.id}.jpg`,
  afterDownloadHandler: undefined,
  captionHandler: undefined,
  max: props.max
})

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
  <div class=":uno: mb-4 flex flex-wrap items-center justify-between gap-4">
    <div class=":uno: flex flex-wrap items-center gap-3">
      <SearchInput v-model="keyword" />
    </div>
    <HasPermission :permissions="['system:attachments:manage']">
      <div class=":uno: flex flex-none items-center gap-2">
        <DownloadButton
          v-if="isDownloadMode && selectedImages.size"
          :loading="downloading"
          @click="handleDownloadImage"
        />
        <DownloadModeSwitcher />
      </div>
    </HasPermission>
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

    <div v-if="images.length" class=":uno: mt-4 flex items-center justify-center">
      <VButton :loading="isFetching" type="secondary" @click="page++">
        {{ isFetching ? '加载中...' : '加载更多' }}
      </VButton>
    </div>
  </div>
</template>
