import http from '../http'

/**
 * 获取仪表盘统计数据
 * @param body 参数
 * @returns 统计数据
 */
export function getDashboardStatistics(body: any) {
  return http.post('/sys/dashboard/statistics', body)
}

/**
 * 获取服务器信息
 * @returns 服务器信息
 */
export function getServerInfo() {
  return http.get('/sys/dashboard/serverInfo')
}

/**
 * 获取模块使用情况
 * @returns 模块使用数据列表
 */
export function getModuleUsage() {
  return http.get('/sys/dashboard/moduleUsage')
}

/**
 * 获取 JVM 各内存分区占用（柱状图）
 * @returns 分区名称与已用 MB
 */
export function getModuleMemoryUsage() {
  return http.get('/sys/dashboard/moduleMemoryUsage')
}

/**
 * 获取服务内存使用情况
 * @returns 服务内存使用数据列表
 */
export function getServiceMemoryUsage() {
  return http.get('/sys/dashboard/serviceMemoryUsage')
}

/**
 * 获取最近操作日志
 * @param params 查询参数
 * @returns 操作日志列表
 */
export function getRecentOperationLogs(params: any) {
  return http.get('/sys/dashboard/recentOperationLogs', { params })
}

/**
 * 获取最近登录日志
 * @param params 查询参数
 * @returns 登录日志列表
 */
export function getRecentLoginLogs(params: any) {
  return http.get('/sys/dashboard/recentLoginLogs', { params })
}
