import request from '@/api/http'

/**
 * 获取部门树
 * @param params 参数 { tenantId }
 * @returns 部门树
 */
export const getDepartmentTree = (params: { tenantId: string }) => {
  return request.post('/sys/department/tree', params)
}

/**
 * 查询部门列表
 * @param params 查询参数
 * @returns 部门列表
 */
export const listDepartments = (params: any) => {
  return request.post('/sys/department/list', params)
}

/**
 * 获取部门详情
 * @param params 参数 { id, tenantId }
 * @returns 部门详情
 */
export const getDepartment = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/department/get', params)
}

/**
 * 新增部门
 * @param params 部门数据
 * @returns 结果
 */
export const createDepartment = (params: any) => {
  return request.post('/sys/department/create', params)
}

/**
 * 更新部门
 * @param params 部门数据
 * @returns 结果
 */
export const updateDepartment = (params: any) => {
  return request.post('/sys/department/update', params)
}

/**
 * 删除部门
 * @param params 参数 { id, tenantId }
 * @returns 结果
 */
export const deleteDepartment = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/department/delete', params)
}
