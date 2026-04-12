import http from '../http'

/**
 * 涓婁紶鏂囦欢
 * @param file 鏂囦欢瀵硅薄
 * @param config 璇锋眰閰嶇疆閫夐」
 * @returns 涓婁紶缁撴灉
 */
export function uploadFile(file: File, config: any = {}) {
  const fd = new 表单Data()
  fd.append('file', file)
  return http.post('/sys/file/upload', fd, {
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    ...config
  })
}
