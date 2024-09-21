export function getFileNameFromUrl(url: string) {
  const urlObj = new URL(url)
  const pathname = urlObj.pathname
  return pathname.split('/').pop()
}
