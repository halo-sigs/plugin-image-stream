import PexelsLogo from '@/assets/pexels.svg'
import PixabayLogo from '@/assets/pixabay.svg'
import UnsplashLogo from '@/assets/unsplash.svg'
import PexelsImages from '@/components/sources/PexelsImages.vue'
import PixabayImages from '@/components/sources/PixabayImages.vue'
import UnsplashImages from '@/components/sources/UnsplashImages.vue'
import { markRaw } from 'vue'

export const PLUGIN_NAME = 'image-stream'
export const DEFAULT_PER_PAGE = 48

export const SOURCES = [
  {
    id: 'unsplash',
    label: 'Unsplash',
    logo: UnsplashLogo,
    component: markRaw(UnsplashImages)
  },
  {
    id: 'pixabay',
    label: 'Pixabay',
    logo: PixabayLogo,
    component: markRaw(PixabayImages)
  },
  {
    id: 'pexels',
    label: 'Pexels',
    logo: PexelsLogo,
    component: markRaw(PexelsImages)
  }
]

export type SOURCE_IDS = 'unsplash' | 'pixabay' | 'pexels'

export const BINDING_LABEL_KEY = (source: SOURCE_IDS) => `image-stream.halo.run/${source}/id`
