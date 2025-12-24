import { definePlugin, utils } from '@halo-dev/ui-shared'
import 'uno.css'
import { markRaw } from 'vue'
import ImageStreamProvider from './components/ImageStreamProvider.vue'

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    'attachment:selector:create': async () => {
      if (utils.permission.has(['*', 'system:attachments:view'])) {
        return [
          {
            id: 'image-stream',
            label: 'Image Stream',
            component: markRaw(ImageStreamProvider)
          }
        ]
      }

      return []
    }
  }
})
