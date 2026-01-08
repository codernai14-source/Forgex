import http from '../http'

export function uploadFile(file: File) {
  const fd = new FormData()
  fd.append('file', file)
  return http.post('/sys/file/upload', fd)
}

