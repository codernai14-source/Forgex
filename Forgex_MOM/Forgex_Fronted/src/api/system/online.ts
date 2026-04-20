import http from '../http'

/**
 * 在线用户相关 API。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */

/**
 * 分页查询在线用户列表。
 *
 * @param body 查询参数（current/size/account/tenantId）
 * @returns 在线用户分页结果
 */
export function listOnlineUsers(body: any) {
  return http.post('/sys/online/list', body)
}

/**
 * 获取当前租户在线用户数。
 *
 * @param body 预留参数
 * @returns 在线用户数
 */
export function countOnlineUsers(body: any) {
  return http.post('/sys/online/count', body)
}

/**
 * 强制下线指定 token 会话。
 *
 * @param body 请求体（token）
 * @returns 是否成功
 */
export function kickoutOnlineUser(body: any, config?: any) {
  return http.post('/sys/online/kickout', body, config)
}
