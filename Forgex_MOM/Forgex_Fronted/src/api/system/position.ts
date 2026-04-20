import request from '@/api/http'

/**
 * 查询职位列表
 * 
 * 执行步骤：
 * 1. 接收查询参数（可选）
 * 2. 调用后端职位列表接口
 * 3. 返回职位列表
 * 
 * @param params 查询参数（可选，如 tenantId、name 等）
 * @returns 职位列表
 * @throws 查询失败时抛出异常
 */
export const listPositions = (params: any) => {
  return request.post('/sys/position/list', params)
}

/**
 * 获取岗位树
 *
 * 当前后端未提供独立树接口，这里兼容角色授权页面：
 * 直接复用岗位列表接口，并转换成 a-tree 可消费的节点结构。
 */
export const getPositionTree = async (params: { tenantId: string }) => {
  const list = await listPositions(params)
  if (!Array.isArray(list)) {
    return []
  }

  return list.map((item: any) => ({
    ...item,
    id: item?.id,
    positionName: item?.positionName ?? item?.name ?? '',
    children: [],
  }))
}

/**
 * 分页查询职位
 * 
 * 执行步骤：
 * 1. 接收分页查询参数
 * 2. 调用后端分页接口
 * 3. 返回分页结果
 * 
 * @param params 分页查询参数
 * @param params.pageNum 页码，默认 1
 * @param params.pageSize 每页条数，默认 10
 * @param params.name 职位名称（可选，模糊查询）
 * @param params.status 状态（可选）
 * @returns 分页结果，包含 records（职位列表）和 total（总数）
 * @throws 查询失败时抛出异常
 */
export const getPositionPage = (params: any) => {
  return request.post('/sys/position/page', params)
}

/**
 * 获取职位详情
 * 
 * 执行步骤：
 * 1. 接收职位 ID 和租户 ID
 * 2. 调用后端详情查询接口
 * 3. 返回职位完整信息
 * 
 * @param params 查询参数
 * @param params.id 职位 ID
 * @param params.tenantId 租户 ID
 * @returns 职位详情对象
 * @throws 查询失败时抛出异常
 */
export const getPosition = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/position/get', params)
}

/**
 * 新增职位
 * 
 * 执行步骤：
 * 1. 接收职位数据
 * 2. 调用后端创建接口
 * 3. 返回创建结果
 * 
 * @param params 职位数据
 * @param params.name 职位名称
 * @param params.code 职位编码
 * @param params.sort 排序号（可选）
 * @param params.description 描述（可选）
 * @param params.status 状态（可选，默认 true）
 * @returns 创建结果
 * @throws 创建失败时抛出异常
 */
export const createPosition = (params: any) => {
  return request.post('/sys/position/create', params)
}

/**
 * 更新职位
 * 
 * 执行步骤：
 * 1. 接收职位数据（必须包含 id）
 * 2. 调用后端更新接口
 * 3. 返回更新结果
 * 
 * @param params 职位数据
 * @param params.id 职位 ID（必填）
 * @param params.name 职位名称
 * @param params.code 职位编码
 * @param params.sort 排序号
 * @param params.description 描述
 * @param params.status 状态
 * @returns 更新结果
 * @throws 更新失败时抛出异常
 */
export const updatePosition = (params: any) => {
  return request.post('/sys/position/update', params)
}

/**
 * 删除职位
 * 
 * 执行步骤：
 * 1. 接收职位 ID 和租户 ID
 * 2. 调用后端删除接口
 * 3. 返回删除结果
 * 
 * @param params 删除参数
 * @param params.id 职位 ID
 * @param params.tenantId 租户 ID
 * @returns 删除结果
 * @throws 删除失败时抛出异常（如职位已被用户使用时）
 */
export const deletePosition = (params: { id: string; tenantId: string }) => {
  return request.post('/sys/position/delete', params)
}
