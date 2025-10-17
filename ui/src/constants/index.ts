import PexelsLogo from '@/assets/pexels.svg'
import PixabayLogo from '@/assets/pixabay.svg'
import UnsplashLogo from '@/assets/unsplash.svg'
import { VLoading } from '@halo-dev/components'
import { defineAsyncComponent } from 'vue'

export const PLUGIN_NAME = 'image-stream'
export const DEFAULT_PER_PAGE = 48
export const UNSPLASH_DEFAULT_PER_PAGE = 30

export const SOURCES = [
  {
    id: 'unsplash',
    label: 'Unsplash',
    logo: UnsplashLogo,
    component: defineAsyncComponent({
      loader: () => import('@/components/sources/UnsplashImages.vue'),
      loadingComponent: VLoading
    })
  },
  {
    id: 'pixabay',
    label: 'Pixabay',
    logo: PixabayLogo,
    component: defineAsyncComponent({
      loader: () => import('@/components/sources/PixabayImages.vue'),
      loadingComponent: VLoading
    })
  },
  {
    id: 'pexels',
    label: 'Pexels',
    logo: PexelsLogo,
    component: defineAsyncComponent({
      loader: () => import('@/components/sources/PexelsImages.vue'),
      loadingComponent: VLoading
    })
  }
]

export type SOURCE_IDS = 'unsplash' | 'pixabay' | 'pexels'

export const BINDING_LABEL_KEY = (source: SOURCE_IDS) => `image-stream.halo.run/${source}/id`
