import http from '@/api/http'

/**
 * 租户消息白名单 API
 */

/**
 * 分页查询租户消息白名单
 */
export const pageTenantMessageWhitelist = (params: any) => {
  return http.get('/sys/tenant-message-whitelist/page', { params })
}

/**
 * 根据 ID 获取白名单详情
 */
export const getTenantMessageWhitelist = (id: number) => {
  return http.get(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 保存租户消息白名单（新增或修改）
 */
export const saveTenantMessageWhitelist = (data: any) => {
  if (data && data.id) {
    return http.put('/sys/tenant-message-whitelist', data)
  }

  return http.post('/sys/tenant-message-whitelist', data)
}

/**
 * 删除租户消息白名单
 */
export const deleteTenantMessageWhitelist = (id: number) => {
  return http.delete(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 启用或禁用租户消息白名单
 */
export const toggleEnabled = (id: number, enabled: boolean) => {
  return http.put(`/sys/tenant-message-whitelist/${id}/enabled`, {}, { params: { enabled } })
}

/**
 * 校验跨租户发送权限
 */
export const checkCrossTenantPermission = (senderTenantId: number, receiverTenantId: number) => {
  return http.get('/sys/tenant-message-whitelist/check-permission', {
    params: { senderTenantId, receiverTenantId },
  })
}
