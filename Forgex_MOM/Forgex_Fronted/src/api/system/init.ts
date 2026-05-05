import http from '../http'

/**
 * 获取系统初始化状态。
 * @returns 初始化状态。
 */
export function getInit状态() {
  return http.get('/sys/init/status')
}

/**
 * 提交系统初始化。
 * @param body 初始化参数。
 * @returns 请求结果。
 */
export function applyInit(body: any) {
  return http.post('/sys/init/apply', body)
}
