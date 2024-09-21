<script setup lang="ts">
import { SOURCES } from '@/constants'
import type { AttachmentLike } from '@halo-dev/console-shared'
import { computed, ref, watch } from 'vue'

withDefaults(
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

const selectedSourceId = ref(SOURCES[0].id)

const selectedSource = computed(() =>
  SOURCES.find((source) => source.id === selectedSourceId.value)
)

function onSelected(attachments: AttachmentLike[]) {
  emit('update:selected', attachments)
}

watch(
  () => selectedSourceId.value,
  () => {
    emit('update:selected', [])
  }
)
</script>

<template>
  <ul class="flex gap-3">
    <li
      v-for="source in SOURCES"
      :key="source.id"
      class="flex cursor-pointer items-center gap-2 rounded-lg p-2 ring-1 ring-gray-200 transition-all hover:shadow-md"
      :class="{ '!ring-gray-300 shadow': selectedSourceId === source.id }"
      @click="selectedSourceId = source.id"
    >
      <img :src="source.logo" class="size-8" />
      <div
        class="text-gray-600 font-semibold"
        :class="{ '!text-gray-900': selectedSourceId === source.id }"
      >
        {{ source.label }}
      </div>
    </li>
  </ul>

  <div class="mt-4">
    <component :is="selectedSource?.component" v-bind="$props" @update:selected="onSelected" />
  </div>
</template>
