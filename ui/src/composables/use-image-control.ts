import { BINDING_LABEL_KEY, type SOURCE_IDS } from '@/constants'
import { compressImage } from '@/utils/image'
import { consoleApiClient, coreApiClient, type Attachment } from '@halo-dev/api-client'
import { Toast } from '@halo-dev/components'
import type { AttachmentLike } from '@halo-dev/console-shared'
import { useMutation, useQuery } from '@tanstack/vue-query'
import { chunk, uniq } from 'es-toolkit'
import { set } from 'es-toolkit/compat'
import { computed, ref, watch, type Ref } from 'vue'
import { useConfig } from './use-config'

export function useImageControl<T>(
  source: SOURCE_IDS,
  images: Ref<T[]>,
  options: {
    idHandler: (image: T) => string
    urlHandler: (image: T) => string
    altHandler: (image: T) => string
    fileNameHandler: (image: T) => string
    afterDownloadHandler?: (image: T) => Promise<void>
    captionHandler?: (image: T) => string | undefined
    max?: number
  }
) {
  const {
    idHandler,
    urlHandler,
    altHandler,
    fileNameHandler,
    afterDownloadHandler,
    captionHandler,
    max
  } = options

  const selectedImages = ref<Set<T>>(new Set()) as Ref<Set<T>>
  const finalSelectedUrls = ref<Set<AttachmentLike>>(new Set())

  const { basicConfig, isDownloadMode } = useConfig()

  const imageIds = computed(() => {
    return images.value.map((image) => idHandler(image))
  })

  watch(
    () => selectedImages.value,
    (value) => {
      finalSelectedUrls.value.clear()

      if (isDownloadMode.value) {
        for (const image of value) {
          const id = idHandler(image)
          const bindAttachment = boundAttachments.value?.find(
            (item) => item.metadata.labels?.[BINDING_LABEL_KEY(source)] === id
          )
          if (bindAttachment) {
            finalSelectedUrls.value.add({
              url: bindAttachment.status?.permalink || '',
              alt: bindAttachment.spec.displayName || '',
              mediaType: bindAttachment.spec.mediaType || 'image/*',
              caption: captionHandler?.(image)
            })
          }
        }
        return
      }
      for (const image of value) {
        finalSelectedUrls.value.add({
          url: urlHandler(image),
          alt: altHandler(image),
          mediaType: 'image/*',
          caption: captionHandler?.(image)
        })
      }
    },
    {
      deep: true
    }
  )

  const handleSelect = async (image: T) => {
    if (selectedImages.value.has(image)) {
      selectedImages.value.delete(image)
      return
    }
    selectedImages.value.add(image)
  }

  const { data: boundAttachments, refetch: refetchAttachments } = useQuery({
    queryKey: ['plugin:image-stream:bound-attachments', imageIds, source],
    queryFn: async () => {
      const uniqueIds = uniq(imageIds.value)
      const idChunks = chunk(uniqueIds, 50)

      const allAttachments: Attachment[] = []

      for (const idChunk of idChunks) {
        const { data } = await coreApiClient.storage.attachment.listAttachment({
          labelSelector: [`image-stream.halo.run/${source}/id=(${idChunk.join(',')})`]
        })
        allAttachments.push(...data.items)
      }

      return allAttachments
    }
  })

  function isBound(image: T) {
    const id = idHandler(image)
    return boundAttachments.value?.some(
      (item) => item.metadata.labels?.[BINDING_LABEL_KEY(source)] === id
    )
  }

  function isDisabled(image: T) {
    if (downloading.value) {
      return true
    }
    if (max !== undefined && max <= selectedImages.value.size && !selectedImages.value.has(image)) {
      return true
    }
    return false
  }

  const { mutateAsync: bindingAttachmentMutate } = useMutation({
    mutationKey: ['plugin:image-stream:binding-attachment', source],
    mutationFn: async ({ attachment, id }: { attachment: Attachment; id: string }) => {
      const labels = set<{
        [key: string]: string
      }>(attachment.metadata.labels || {}, [BINDING_LABEL_KEY(source)], id)

      return await coreApiClient.storage.attachment.patchAttachment(
        {
          name: attachment.metadata.name,
          jsonPatchInner: [
            {
              op: 'add',
              path: '/metadata/labels',
              value: labels
            }
          ]
        },
        {
          mute: true
        }
      )
    },
    retry: 3
  })

  // Download image to attachments
  const downloading = ref(false)

  async function handleDownloadImage() {
    downloading.value = true

    let hasError = false

    for (const image of Array.from(selectedImages.value)) {
      if (isBound(image)) {
        continue
      }

      try {
        await downloadSingleImage(image)
      } catch (error) {
        hasError = true
        Toast.error(`图片 ${altHandler(image)} 下载失败`)
      }
    }

    if (hasError) {
      Toast.warning(`部分图片转存失败`)
    } else {
      Toast.success('所有图片转存完成')
    }

    downloading.value = false
  }

  async function downloadSingleImage(image: T) {
    try {
      const id = idHandler(image)
      const url = urlHandler(image)
      const fileName = fileNameHandler(image)

      const { policyName, groupName } = basicConfig.value?.downloadMode || {}

      const imageResponse = await fetch(url)

      const imageBlob = await imageResponse.blob()
      let imageFile = new File([imageBlob], fileName, { type: imageBlob.type })

      if (basicConfig.value?.downloadMode?.enableCompress) {
        imageFile = await compressImage(
          imageFile,
          fileName,
          Number(basicConfig.value.downloadMode.compressQuality || 0.6),
          Number(basicConfig.value.downloadMode.compressMaxWidth || 1920)
        )
      }

      const { data: newAttachment } = await consoleApiClient.storage.attachment.uploadAttachment({
        file: imageFile,
        policyName: policyName as string,
        groupName: groupName
      })

      await bindingAttachmentMutate({
        attachment: newAttachment,
        id
      })

      const permalink = await getAttachmentPermalink(newAttachment.metadata.name)

      finalSelectedUrls.value.add({
        url: permalink || '',
        alt: newAttachment.spec.displayName || '',
        mediaType: newAttachment.spec.mediaType || 'image/*',
        caption: captionHandler?.(image)
      })

      await refetchAttachments()

      await afterDownloadHandler?.(image)
    } catch (error) {
      throw new Error(`上传失败: ${(error as Error).message}`)
    }
  }

  async function getAttachmentPermalink(name: string): Promise<string> {
    return new Promise((resolve, reject) => {
      const fetchPermalink = () => {
        coreApiClient.storage.attachment
          .getAttachment({
            name: name
          })
          .then((response) => {
            const permalink = response.data.status?.permalink
            if (permalink) {
              resolve(permalink)
            } else {
              setTimeout(fetchPermalink, 1000)
            }
          })
          .catch((error) => reject(error))
      }
      fetchPermalink()
    })
  }

  return {
    selectedImages,
    finalSelectedUrls,
    isBound,
    isDisabled,
    downloading,
    handleSelect,
    handleDownloadImage
  }
}
