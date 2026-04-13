import http from '@/api/http'

/**
 * 绉熸埛娑堟伅鐧藉悕鍗?API
 */

/**
 * 鍒嗛〉鏌ヨ绉熸埛娑堟伅鐧藉悕鍗曘€?
 *
 * @param params 鏌ヨ鍙傛暟锛坈urrent/size/senderTenantId/receiverTenantId/enabled锛?
 * @returns 鐧藉悕鍗曞垎椤电粨鏋?
 */
export const pageTenantMessageWhitelist = (params: any) => {
  return http.get('/sys/tenant-message-whitelist/page', { params })
}

/**
 * 鏍规嵁ID鏌ヨ绉熸埛娑堟伅鐧藉悕鍗曘€?
 *
 * @param id 鐧藉悕鍗旾D
 * @returns 鐧藉悕鍗曢厤缃?
 */
export const getTenantMessageWhitelist = (id: number) => {
  return http.get(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 淇濆瓨绉熸埛娑堟伅鐧藉悕鍗曪紙鏂板鎴栦慨鏀癸級銆?
 *
 * @param data 鐧藉悕鍗曟暟鎹?
 * @returns 鏄惁鎴愬姛
 */
export const saveTenantMessageWhitelist = (data: any) => {
  if (data && data.id) {
    return http.put('/sys/tenant-message-whitelist', data)
  }

  return http.post('/sys/tenant-message-whitelist', data)
}

/**
 * 鍒犻櫎绉熸埛娑堟伅鐧藉悕鍗曘€?
 *
 * @param id 鐧藉悕鍗旾D
 * @returns 鏄惁鎴愬姛
 */
export const deleteTenantMessageWhitelist = (id: number) => {
  return http.delete(`/sys/tenant-message-whitelist/${id}`)
}

/**
 * 鍚敤/绂佺敤绉熸埛娑堟伅鐧藉悕鍗曘€?
 *
 * @param id 鐧藉悕鍗旾D
 * @param enabled 鏄惁鍚敤
 * @returns 鏄惁鎴愬姛
 */
export const toggleEnabled = (id: number, enabled: boolean) => {
  return http.put(`/sys/tenant-message-whitelist/${id}/enabled`, {}, { params: { enabled } })
}

/**
 * 妫€鏌ヨ法绉熸埛娑堟伅鏉冮檺銆?
 *
 * @param senderTenantId 鍙戦€佹柟绉熸埛ID
 * @param receiverTenantId 鎺ユ敹鏂圭鎴稩D
 * @returns 鏄惁鏈夋潈闄?
 */
export const checkCrossTenant权限 = (senderTenantId: number, receiverTenantId: number) => {
  return http.get('/sys/tenant-message-whitelist/check-permission', {
    params: { senderTenantId, receiverTenantId }
  })
}

