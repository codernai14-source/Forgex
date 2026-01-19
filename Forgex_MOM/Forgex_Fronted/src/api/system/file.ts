import http from '../http'

/**
 * 上传文件
 * @param file 文件对象
 * @returns 上传结果
 */
export function uploadFile(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/sys/file/upload', fd)
}
