import request from '@/api/http'

/**
 * 查询职位列表
 */
export const listPositions = (params: any) => {
  return request.post('/sys/position/list', params)
}

/**
 * 获取职位详情
 */
export const getPosition = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/position/get', params)
}

/**
 * 新增职位
 */
export const createPosition = (params: any) => {
  return request.post('/sys/position/create', params)
}

/**
 * 更新职位
 */
export const updatePosition = (params: any) => {
  return request.post('/sys/position/update', params)
}

/**
 * 删除职位
 */
export const deletePosition = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/position/delete', params)
}
