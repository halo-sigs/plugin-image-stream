<script setup lang="ts">
import { useConfig, type BasicConfig } from '@/composables/use-config'
import { PLUGIN_NAME } from '@/constants'
import { consoleApiClient } from '@halo-dev/api-client'
import { Toast } from '@halo-dev/components'
import { useQueryClient } from '@tanstack/vue-query'
import { cloneDeep, set } from 'lodash-es'
import { ref } from 'vue'

const queryClient = useQueryClient()

const { basicConfig } = useConfig()

const pluginDetailModal = ref(false)

async function onDownloadModeChange(value: boolean) {
  const basicConfigToUpdate = cloneDeep(basicConfig.value)
  set<BasicConfig>(basicConfigToUpdate || {}, 'downloadMode.enable', value)

  const { data: configMap } = await consoleApiClient.plugin.plugin.fetchPluginConfig({
    name: PLUGIN_NAME
  })

  configMap.data = {
    ...configMap.data,
    basic: JSON.stringify(basicConfigToUpdate)
  }

  const { data: updatedConfigMap } = await consoleApiClient.plugin.plugin.updatePluginConfig({
    name: PLUGIN_NAME,
    configMap: configMap
  })

  const updatedBasicConfig = JSON.parse(updatedConfigMap.data?.basic || '{}') as BasicConfig

  if (updatedBasicConfig.downloadMode?.enable && !updatedBasicConfig.downloadMode.policyName) {
    Toast.warning('开启转存模式需要配置附件存储策略')
    pluginDetailModal.value = true
  }

  queryClient.invalidateQueries({ queryKey: ['plugin:image-stream:basic-config'] })
}

function onPluginDetailModalClose() {
  pluginDetailModal.value = false
  queryClient.invalidateQueries({ queryKey: ['plugin:image-stream:basic-config'] })
}
</script>
<template>
  <FormKit
    v-tooltip="'勾选此选项后，会先上传到服务器，然后获得服务器的图片地址'"
    type="checkbox"
    label="转存模式"
    :model-value="basicConfig?.downloadMode?.enable"
    :classes="{ outer: '!p-0 flex-none', wrapper: 'image-stream-checkbox-wrapper' }"
    @input="onDownloadModeChange"
  />

  <PluginDetailModal
    v-if="pluginDetailModal"
    :name="PLUGIN_NAME"
    @close="onPluginDetailModalClose"
  />
</template>

<style>
.image-stream-checkbox-wrapper {
  margin: 0 !important;
}
</style>
