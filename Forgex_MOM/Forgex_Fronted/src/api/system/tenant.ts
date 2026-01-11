import http from '../http'

/**
 * 获取租户忽略列表
 * @returns 忽略列表
 */
export function listTenantIgnore() {
  return http.get('/sys/tenant-ignore')
}

/**
 * 重新加载租户忽略列表
 * @returns 结果
 */
export function reloadTenantIgnore() {
  return http.post('/sys/tenant-ignore/reload')
}
