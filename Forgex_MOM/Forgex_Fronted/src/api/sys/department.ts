import request from '@/api/http'

/**
 * 获取部门树
 */
export const getDepartmentTree = (params: { tenantId: string }) => {
  return request.post('/sys/department/tree', params)
}

/**
 * 查询部门列表
 */
export const listDepartments = (params: any) => {
  return request.post('/sys/department/list', params)
}

/**
 * 获取部门详情
 */
export const getDepartment = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/department/get', params)
}

/**
 * 新增部门
 */
export const createDepartment = (params: any) => {
  return request.post('/sys/department/create', params)
}

/**
 * 更新部门
 */
export const updateDepartment = (params: any) => {
  return request.post('/sys/department/update', params)
}

/**
 * 删除部门
 */
export const deleteDepartment = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/department/delete', params)
}
