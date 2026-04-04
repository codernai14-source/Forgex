import http from '@/api/http'

/**
 * 获取部门树
 * 
 * 执行步骤：
 * 1. 接收租户 ID 参数
 * 2. 调用后端部门树查询接口
 * 3. 返回树形结构的部门数据
 * 
 * @param params 查询参数
 * @param params.tenantId 租户 ID
 * @returns 部门树形结构（嵌套对象，包含 children 数组）
 * @throws 查询失败时抛出异常
 */
export const getDepartmentTree = (params: { tenantId: string }) => {
  return http.post('/sys/department/tree', params)
}

/**
 * 查询部门列表
 * 
 * 执行步骤：
 * 1. 接收查询参数（可选）
 * 2. 调用后端部门列表接口
 * 3. 返回扁平化的部门列表
 * 
 * @param params 查询参数（可选，如 tenantId）
 * @returns 部门列表
 * @throws 查询失败时抛出异常
 */
export const listDepartments = (params: any) => {
  return http.post('/sys/department/list', params)
}

/**
 * 获取部门详情
 * 
 * 执行步骤：
 * 1. 接收部门 ID 和租户 ID
 * 2. 调用后端详情查询接口
 * 3. 返回部门完整信息
 * 
 * @param params 查询参数
 * @param params.id 部门 ID
 * @param params.tenantId 租户 ID
 * @returns 部门详情对象
 * @throws 查询失败时抛出异常
 */
export const getDepartment = (params: { id: string; tenantId: string }) => {
  return http.post('/sys/department/get', params)
}

/**
 * 新增部门
 * 
 * 执行步骤：
 * 1. 接收部门数据
 * 2. 调用后端创建接口
 * 3. 返回创建结果
 * 
 * @param params 部门数据
 * @param params.name 部门名称
 * @param params.parentId 父级部门 ID（可选，顶级部门为 0 或空）
 * @param params.sort 排序号（可选）
 * @param params.description 描述（可选）
 * @returns 创建结果
 * @throws 创建失败时抛出异常
 */
export const createDepartment = (params: any) => {
  return http.post('/sys/department/create', params)
}

/**
 * 更新部门
 * 
 * 执行步骤：
 * 1. 接收部门数据（必须包含 id）
 * 2. 调用后端更新接口
 * 3. 返回更新结果
 * 
 * @param params 部门数据
 * @param params.id 部门 ID（必填）
 * @param params.name 部门名称
 * @param params.parentId 父级部门 ID
 * @param params.sort 排序号
 * @param params.description 描述
 * @returns 更新结果
 * @throws 更新失败时抛出异常
 */
export const updateDepartment = (params: any) => {
  return http.post('/sys/department/update', params)
}

/**
 * 删除部门
 * 
 * 执行步骤：
 * 1. 接收部门 ID 和租户 ID
 * 2. 调用后端删除接口
 * 3. 返回删除结果
 * 
 * @param params 删除参数
 * @param params.id 部门 ID
 * @param params.tenantId 租户 ID
 * @returns 删除结果
 * @throws 删除失败时抛出异常（如部门下存在子部门或用户时）
 */
export const deleteDepartment = (params: { id: string; tenantId: string }) => {
  return http.post('/sys/department/delete', params)
}
