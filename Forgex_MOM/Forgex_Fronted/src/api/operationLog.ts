import http from '@/utils/http'

/**
 * 操作日志 API
 */

/**
 * 分页查询操作日志
 */
export const pageOperationLog = (params: any) => {
  return http.post('/sys/operationLog/page', params)
}

/**
 * 根据ID查询操作日志详情
 */
export const getOperationLog = (id: number) => {
  return http.post('/sys/operationLog/get', { id })
}

/**
 * 导出操作日志
 */
export const exportOperationLog = (params: any) => {
  return http.download('/sys/operationLog/export', params)
}

