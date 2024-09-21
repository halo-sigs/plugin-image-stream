<script setup lang="ts">
import { IconUpload, VButton } from '@halo-dev/components'

withDefaults(defineProps<{ loading: boolean }>(), { loading: false })
</script>
<template>
  <VButton :loading="loading" size="sm" type="secondary">
    <template #icon>
      <IconUpload class="size-full" />
    </template>
    转存已选择的图片
  </VButton>
</template>
