import http from '../http'

/**
 * 查询 KMS 密钥分页。
 */
export function pageKms(data: { current: number; size: number; alias?: string }) {
  return http.post('/sys/kms/page', data)
}

/**
 * 创建密钥。
 */
export function createKms(data: { alias: string; keyType: string; keySize: number; description?: string }) {
  return http.post('/sys/kms/create', data)
}

/**
 * 轮换密钥。
 */
export function rotateKms(alias: string) {
  return http.post('/sys/kms/rotate', { alias })
}

/**
 * 禁用密钥。
 */
export function disableKms(alias: string) {
  return http.post('/sys/kms/disable', { alias })
}
