import http from '../http'
import type { Role, GrantBatchParams } from '@/views/system/role/types'

/**
 * 获取角色列表
 * @param params 查询参数
 * @returns 角色列表
 */
export const getRoleList = (params?: any) => {
  return http.post('/sys/role/list', params || {})
}

/**
 * 分页查询角色
 * @param params 分页查询参数
 * @returns 分页结果
 */
export const getRolePage = (params: any) => {
  return http.post('/sys/role/page', params)
}

/**
 * 根据ID获取角色详情
 * @param id 角色ID
 * @returns 角色详情
 */
export const getRoleById = (id: number) => {
  return http.post('/sys/role/detail', { id })
}

/**
 * 新增角色
 * @param data 角色数据
 * @returns 结果
 */
export const addRole = (data: Partial<Role>) => {
  return http.post('/sys/role/create', data)
}

/**
 * 更新角色
 * @param data 角色数据
 * @returns 结果
 */
export const updateRole = (data: Partial<Role>) => {
  return http.post('/sys/role/update', data)
}

/**
 * 删除角色
 * @param id 角色ID
 * @returns 结果
 */
export const deleteRole = (id: number) => {
  return http.post('/sys/role/delete', { id })
}

/**
 * 批量删除角色
 * @param ids 角色ID列表
 * @returns 结果
 */
export const batchDeleteRoles = (ids: number[]) => {
  return http.post('/sys/role/batchDelete', { ids })
}

/**
 * 获取角色授权数据（包含所有菜单权限和已拥有的权限）
 * @param params 参数 { roleId, tenantId }
 * @returns 授权数据
 */
export const getRoleAuthData = (params: { roleId: number; tenantId: string }) => {
  return http.post('/sys/role/menu/authData', params)
}

/**
 * 获取指定模块下的角色菜单授权树
 * @param moduleId 模块ID
 * @param params 参数 { roleId }
 * @returns 带 checked 状态的菜单树
 */
export const getRoleModuleAuthData = (
  moduleId: number,
  params: { roleId: number | string }
) => {
  return http.post(`/sys/role/menu/authData/module/${moduleId}`, params)
}

/**
 * 获取角色菜单列表
 * @param params 参数 { roleId, tenantId }
 * @returns 菜单列表
 */
export const listRoleMenus = (params: { roleId: number; tenantId: string }) => {
  return http.post('/sys/role/menu/list', params)
}

/**
 * 角色授权菜单
 * @param data 授权数据 { roleId, tenantId, menuIds }
 * @returns 结果
 */
export const grantRoleMenus = (data: { roleId: number; tenantId: string; menuIds: number[] }) => {
  return http.post('/sys/role/menu/grant', data)
}

export const getRoleCModuleAuthData = (
  moduleId: number,
  params: { roleId: number | string }
) => {
  return http.post(`/sys/role/c-menu/authData/module/${moduleId}`, params)
}

export const listRoleCMenus = (params: { roleId: number; tenantId: string }) => {
  return http.post('/sys/role/c-menu/list', params)
}

export const grantRoleCMenus = (data: { roleId: number; tenantId: string; menuIds: number[] }) => {
  return http.post('/sys/role/c-menu/grant', data)
}

/**
 * 获取角色已授权的用户列表
 * @param params 参数 { roleId, tenantId, pageNum, pageSize }
 * @returns 用户列表
 */
export const getRoleAuthorizedUsers = (params: { roleId: number; tenantId: string; pageNum?: number; pageSize?: number }) => {
  return http.post('/sys/role/user/list', params)
}

/**
 * 角色授权用户
 * @param data 授权数据 { roleId, tenantId, userIds }
 * @returns 结果
 */
export const grantRoleUsers = (data: { roleId: number; tenantId: string; userIds: number[] }) => {
  return http.post('/sys/role/user/grant', data)
}

/**
 * 角色取消授权用户
 * @param data 取消授权数据 { roleId, tenantId, userIds }
 * @returns 结果
 */
export const revokeRoleUsers = (data: { roleId: number; tenantId: string; userIds: number[] }) => {
  return http.post('/sys/role/user/revoke', data)
}

/**
 * 获取已授权人员列表（支持用户、部门、职位）
 * @param params 参数 { roleId, tenantId, grantType, pageNum, pageSize }
 * @returns 已授权人员列表
 */
export const getGrantedUserList = (params: {
  roleId: number
  tenantId: string
  grantType?: string
  pageNum?: number
  pageSize?: number
}) => {
  return http.post('/sys/role/user/granted/list', params)
}

/**
 * 批量授权（支持用户、部门、职位）
 * @param data 授权数据 { roleId, tenantId, grantType, userIds, departmentIds, positionIds }
 * @returns 结果
 */
export const grantBatch = (data: GrantBatchParams) => {
  return http.post('/sys/role/user/grant/batch', data)
}
