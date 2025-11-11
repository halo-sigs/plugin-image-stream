import { consoleApiClient } from '@halo-dev/api-client'
import { definePlugin } from '@halo-dev/ui-shared'
import 'uno.css'
import { markRaw } from 'vue'
import ImageStreamProvider from './components/ImageStreamProvider.vue'

export default definePlugin({
  components: {},
  routes: [],
  extensionPoints: {
    'attachment:selector:create': async () => {
      const { data } = await consoleApiClient.user.getPermissions({
        name: '-'
      })

      if (
        data.uiPermissions.includes('*') ||
        data.uiPermissions.includes('system:attachments:view')
      ) {
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
