<script setup lang="ts">
import { IconCheckboxFill, IconExternalLinkLine, VAvatar, VCard, VTag } from '@halo-dev/components'

withDefaults(
  defineProps<{
    checked: boolean
    disabled: boolean
    bound?: boolean
    url: string
    htmlUrl: string
    author?: {
      htmlUrl?: string
      name: string
      avatar?: string
      bio?: string
    }
  }>(),
  {
    bound: undefined,
    author: undefined
  }
)
</script>
<template>
  <VCard
    :body-class="[':uno: !p-0 select-none']"
    :class="{
      ':uno: ring-1 ring-black': checked,
      ':uno: pointer-events-none !cursor-not-allowed opacity-50': disabled
    }"
    class=":uno: hover:shadow"
  >
    <div class=":uno: group relative bg-white">
      <div class=":uno: block aspect-10/8 h-full w-full cursor-pointer overflow-hidden bg-gray-100">
        <img
          class=":uno: pointer-events-none size-full object-cover group-hover:opacity-75"
          :src="url"
        />
      </div>
      <div
        class=":uno: absolute left-0 top-0 h-1/3 w-full ease-in-out group-hover:from-gray-300 group-hover:to-transparent group-hover:bg-gradient-to-b"
        :class="{ ':uno: from-gray-300 to-transparent bg-gradient-to-b': checked }"
      >
        <div class=":uno: flex items-center justify-between p-1">
          <a :href="htmlUrl" target="_blank">
            <IconExternalLinkLine
              class=":uno: size-6 cursor-pointer text-white opacity-0 transition-all hover:text-black group-hover:opacity-100"
            />
          </a>

          <div class=":uno: flex items-center gap-1.5">
            <IconCheckboxFill
              :class="{
                ':uno: !text-black !opacity-100': checked
              }"
              class=":uno: size-6 cursor-pointer text-white opacity-0 transition-all hover:text-black group-hover:opacity-100"
            />

            <VTag v-if="bound" theme="primary">已转存</VTag>
          </div>
        </div>
      </div>
      <div
        v-if="author"
        :class="{ ':uno: !flex': checked }"
        class=":uno: absolute bottom-0 left-0 hidden w-full from-gray-600 to-transparent bg-gradient-to-t ease-in-out group-hover:flex"
      >
        <div class=":uno: w-full flex flex-row items-center gap-2 p-1">
          <VAvatar v-if="author.avatar" :src="author.avatar" circle size="sm"></VAvatar>
          <div class=":uno: flex flex-1 flex-col truncate">
            <a
              class=":uno: truncate text-xs text-white font-medium hover:underline"
              :href="author.htmlUrl || 'javascript:void(0)'"
              target="_blank"
            >
              {{ author.name }}
            </a>
            <span v-if="author.bio" class=":uno: truncate text-xs text-white opacity-80">
              {{ author.bio }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </VCard>
</template>
