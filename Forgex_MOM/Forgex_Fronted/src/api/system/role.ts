import http from '../http'
import type { Role } from '@/views/system/role/types'

/**
 * 获取角色列表
 */
export const getRoleList = (params?: any) => {
  return http.post('/sys/role/list', params || {})
}

/**
 * 分页查询角色
 */
export const getRolePage = (params: any) => {
  return http.post('/sys/role/page', params)
}

/**
 * 根据ID获取角色详情
 */
export const getRoleById = (id: number) => {
  return http.post('/sys/role/detail', { id })
}

/**
 * 新增角色
 */
export const addRole = (data: Partial<Role>) => {
  return http.post('/sys/role/create', data)
}

/**
 * 更新角色
 */
export const updateRole = (data: Partial<Role>) => {
  return http.post('/sys/role/update', data)
}

/**
 * 删除角色
 */
export const deleteRole = (id: number) => {
  return http.post('/sys/role/delete', { id })
}

/**
 * 批量删除角色
 */
export const batchDeleteRoles = (ids: number[]) => {
  return http.post('/sys/role/batchDelete', { ids })
}
