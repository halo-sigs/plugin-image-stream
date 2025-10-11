import Compressor from 'compressorjs'

export function compressImage(
  file: File,
  fileName: string,
  quality: number,
  maxWidth: number
): Promise<File> {
  return new Promise((resolve, reject) => {
    new Compressor(file, {
      quality,
      maxWidth,
      success: (compressedFile) => {
        if (compressedFile instanceof File) {
          console.log('?', compressedFile.size / 1024 / 1024, 'MB')
          return resolve(compressedFile)
        } else {
          return resolve(
            new File([compressedFile], fileName, {
              type: compressedFile.type
            })
          )
        }
      },
      error: reject
    })
  })
}
