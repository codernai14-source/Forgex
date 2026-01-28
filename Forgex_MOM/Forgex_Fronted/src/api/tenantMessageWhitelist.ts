import http from '@/api/http'

/**
 * 租户消息白名单 API
 */

/**
 * 分页查询租户消息白名单。
 *
 * @param params 查询参数（current/size/senderTenantId/receiverTenantId/enabled）
 * @returns 白名单分页结果
 */
export const pageTenantMessageWhitelist = (params: any) => {
  return http.get('/sys/tenant-message-whitelist/page', { params })
}

/**
 * 根据ID查询租户消息白名单。
 *
 * @param id 白名单ID
 * @returns 白名单配置
 */
export const getTenantMessageWhitelist = (id: number) => {
  return http.get(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 保存租户消息白名单（新增或修改）。
 *
 * @param data 白名单数据
 * @returns 是否成功
 */
export const saveTenantMessageWhitelist = (data: any) => {
  if (data && data.id) {
    return http.put('/sys/tenant-message-whitelist', data)
  }

  return http.post('/sys/tenant-message-whitelist', data)
}

/**
 * 删除租户消息白名单。
 *
 * @param id 白名单ID
 * @returns 是否成功
 */
export const deleteTenantMessageWhitelist = (id: number) => {
  return http.delete(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 启用/禁用租户消息白名单。
 *
 * @param id 白名单ID
 * @param enabled 是否启用
 * @returns 是否成功
 */
export const toggleEnabled = (id: number, enabled: boolean) => {
  return http.put(`/sys/tenant-message-whitelist/${id}/enabled`, {}, { params: { enabled } })
}

/**
 * 检查跨租户消息权限。
 *
 * @param senderTenantId 发送方租户ID
 * @param receiverTenantId 接收方租户ID
 * @returns 是否有权限
 */
export const checkCrossTenantPermission = (senderTenantId: number, receiverTenantId: number) => {
  return http.get('/sys/tenant-message-whitelist/check-permission', {
    params: { senderTenantId, receiverTenantId }
  })
}

