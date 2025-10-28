<script setup lang="ts">
import { PixabayV1alpha1Api } from '@/api/generated'
import { useConfig } from '@/composables/use-config'
import { useImageControl } from '@/composables/use-image-control'
import { DEFAULT_PER_PAGE } from '@/constants'
import type { PixabayHit, PixabayResponse } from '@/types'
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

const IMAGE_TYPES = [
  {
    label: '全部',
    value: 'all'
  },
  {
    label: '照片',
    value: 'photo'
  },
  {
    label: '插图',
    value: 'illustration'
  },
  {
    label: '矢量',
    value: 'vector'
  }
]

const CATEGORIES = [
  {
    label: '全部'
  },
  {
    label: '背景',
    value: 'backgrounds'
  },
  {
    label: '时尚',
    value: 'fashion'
  },
  {
    label: '自然',
    value: 'nature'
  },
  {
    label: '科学',
    value: 'science'
  },
  {
    label: '教育',
    value: 'education'
  },
  {
    label: '情感',
    value: 'feelings'
  },
  {
    label: '健康',
    value: 'health'
  },
  {
    label: '人们',
    value: 'people'
  },
  {
    label: '宗教',
    value: 'religion'
  },
  {
    label: '地方',
    value: 'places'
  },
  {
    label: '动物',
    value: 'animals'
  },
  {
    label: '工业',
    value: 'industry'
  },
  {
    label: '计算机',
    value: 'computer'
  },
  {
    label: '食物',
    value: 'food'
  },
  {
    label: '体育',
    value: 'sports'
  },
  {
    label: '交通',
    value: 'transportation'
  },
  {
    label: '旅行',
    value: 'travel'
  },
  {
    label: '建筑',
    value: 'buildings'
  },
  {
    label: '商业',
    value: 'business'
  },
  {
    label: '音乐',
    value: 'music'
  }
]

const ORDER_TYPES = [
  {
    label: '热门',
    value: 'popular'
  },
  {
    label: '最新',
    value: 'latest'
  }
]

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

const pixabayAxios = axios.create({
  baseURL: ''
})

const pixabayApiClient = new PixabayV1alpha1Api(
  undefined,
  pixabayAxios.defaults.baseURL,
  pixabayAxios
)

const images = ref<PixabayHit[]>([])
const keyword = ref('')
const selectedImageType = ref(IMAGE_TYPES[0].value)
const selectedCategory = ref(CATEGORIES[0].value)
const selectedOrderType = ref(ORDER_TYPES[0].value)
const page = ref(1)

const { isDownloadMode } = useConfig()

const { isFetching } = useQuery({
  queryKey: [
    'plugin:image-stream:pixabay:images',
    page,
    keyword,
    selectedImageType,
    selectedCategory,
    selectedOrderType
  ],
  queryFn: async () => {
    const { data } = await pixabayApiClient.searchPixImages({
      q: keyword.value,
      page: page.value,
      perPage: DEFAULT_PER_PAGE,
      imageType: selectedImageType.value,
      category: selectedCategory.value,
      order: selectedOrderType.value,
      safesearch: 'true'
    })
    return data as PixabayResponse
  },
  retry: 0,
  onSuccess(data) {
    images.value = [...images.value, ...data.hits]
  },
  onError(e) {
    if (e instanceof AxiosError) {
      const data = e.response?.data

      if ('detail' in data) {
        Toast.error(data.detail)
      } else {
        Toast.error(data)
      }
      return
    }
    if (e instanceof Error) {
      Toast.error(e.message)
    }
  }
})

watch(
  [
    () => keyword.value,
    () => selectedImageType.value,
    () => selectedCategory.value,
    () => selectedOrderType.value
  ],
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
} = useImageControl<PixabayHit>('pixabay', images, {
  idHandler: (image) => image.id + '',
  urlHandler: (image) => image.largeImageURL,
  altHandler: (image) => image.tags,
  fileNameHandler: (image) => getFileNameFromUrl(image.previewURL) || `${image.id}.jpg`,
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
      <FilterDropdown v-model="selectedImageType" label="图像类型" :items="IMAGE_TYPES" />
      <FilterDropdown v-model="selectedCategory" label="类别" :items="CATEGORIES" />
      <FilterDropdown v-model="selectedOrderType" label="排序" :items="ORDER_TYPES" />
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
        :url="image.previewURL"
        :html-url="image.pageURL"
        :author="{
          name: image.user,
          avatar: image.userImageURL
        }"
        @click.stop="handleSelect(image)"
      />
    </ImageLayout>

    <div v-if="images.length" class=":uno: mt-4 flex items-center justify-center">
      <VButton :loading="isFetching" type="secondary" @click="page++">
        {{ isFetching ? '加载中...' : '加载更多' }}
      </VButton>
    </div>
  </div>
</template>
