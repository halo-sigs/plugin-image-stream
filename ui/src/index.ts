import { definePlugin } from '@halo-dev/console-shared'
import { markRaw } from 'vue'
import ImageStreamProvider from './components/ImageStreamProvider.vue'

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    'attachment:selector:create': () => {
      return [
        {
          id: 'image-stream',
          label: 'Image Stream',
          component: markRaw(ImageStreamProvider)
        }
      ]
    }
  }
})
