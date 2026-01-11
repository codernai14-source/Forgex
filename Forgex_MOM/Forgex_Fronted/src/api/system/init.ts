import http from '../http'

/**
 * 获取系统初始化状态
 * @returns 初始化状态
 */
export function getInitStatus() {
  return http.get('/sys/init/status')
}

/**
 * 应用初始化
 * @param body 初始化数据
 * @returns 结果
 */
export function applyInit(body: any) {
  return http.post('/sys/init/apply', body)
}
