import { PLUGIN_NAME } from '@/constants'
import { consoleApiClient } from '@halo-dev/api-client'
import { useQuery } from '@tanstack/vue-query'
import { computed } from 'vue'

export interface BasicConfig {
  downloadMode?: {
    enable?: boolean
    policyName?: string
    groupName?: string
    urlType: 'raw' | 'full' | 'regular' | 'small'
  }
}

export function useConfig() {
  const {
    data: basicConfig,
    isLoading,
    isFetching
  } = useQuery({
    queryKey: ['plugin:image-stream:basic-config'],
    queryFn: async () => {
      const { data: configMapData } = await consoleApiClient.plugin.plugin.fetchPluginJsonConfig(
        {
          name: PLUGIN_NAME
        },
        {
          mute: true
        }
      )

      return (configMapData as any)?.basic as BasicConfig
    }
  })

  const isDownloadMode = computed(() => {
    return basicConfig.value?.downloadMode?.enable && basicConfig.value.downloadMode.policyName
  })

  return { basicConfig, isDownloadMode, isLoading, isFetching }
}
