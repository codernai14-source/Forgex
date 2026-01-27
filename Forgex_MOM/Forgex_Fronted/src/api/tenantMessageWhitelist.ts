import http from '@/utils/http'

/**
 * 租户消息白名单 API
 */

/**
 * 分页查询租户消息白名单
 */
export const pageTenantMessageWhitelist = (params: any) => {
  return http.post('/sys/tenant-message-whitelist/page', params)
}

/**
 * 根据ID查询租户消息白名单
 */
export const getTenantMessageWhitelist = (id: number) => {
  return http.post('/sys/tenant-message-whitelist/get', { id })
}

/**
 * 保存租户消息白名单（新增或修改）
 */
export const saveTenantMessageWhitelist = (data: any) => {
  return http.post('/sys/tenant-message-whitelist/save', data)
}

/**
 * 删除租户消息白名单
 */
export const deleteTenantMessageWhitelist = (id: number) => {
  return http.post('/sys/tenant-message-whitelist/delete', { id })
}

/**
 * 批量删除租户消息白名单
 */
export const batchDeleteTenantMessageWhitelist = (ids: number[]) => {
  return http.post('/sys/tenant-message-whitelist/batchDelete', { ids })
}

/**
 * 启用/禁用租户消息白名单
 */
export const toggleEnabled = (id: number, enabled: boolean) => {
  return http.post(`/sys/tenant-message-whitelist/${id}/enabled`, { enabled })
}

/**
 * 检查跨租户消息权限
 */
export const checkCrossTenantPermission = (senderTenantId: number, receiverTenantId: number) => {
  return http.get('/sys/tenant-message-whitelist/check-permission', {
    params: { senderTenantId, receiverTenantId }
  })
}

