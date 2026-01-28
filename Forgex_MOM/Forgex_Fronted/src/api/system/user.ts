/**
 * 用户管理 API
 */
import http from '../http'
import type { User, UserProfile, UserQuery, Department, Position } from '@/views/system/user/types'
import { exportUser } from '@/api/system/excel'

/**
 * 获取用户分页列表
 */
export async function getUserList(query: UserQuery) {
  return http.post('/sys/user/page', query)
}

/**
 * 获取用户详情
 */
export async function getUserDetail(id: string) {
  return http.post('/sys/user/detail', { id })
}

/**
 * 新增用户
 */
export async function addUser(user: User & { profile?: UserProfile }) {
  return http.post('/sys/user/create', user)
}

/**
 * 更新用户
 */
export async function updateUser(user: User & { profile?: UserProfile }) {
  return http.post('/sys/user/update', user)
}

/**
 * 删除用户
 */
export async function deleteUser(id: string) {
  return http.post('/sys/user/delete', { id })
}

/**
 * 批量删除用户
 */
export async function batchDeleteUsers(ids: string[]) {
  return http.post('/sys/user/batchDelete', { ids })
}

/**
 * 重置密码
 */
export async function resetPassword(id: string) {
  return http.post('/sys/user/resetPassword', { id })
}

/**
 * 更新用户状态
 */
export async function updateUserStatus(id: string, status: boolean) {
  return http.post('/sys/user/updateStatus', { id, status })
}

/**
 * 查询用户已分配角色（当前租户维度）。
 *
 * @param userId 用户ID
 * @returns { assignedRoleIds, tenantId }
 */
export async function getUserAssignedRoles(userId: string) {
  return http.post('/sys/user/role/listByUser', { userId })
}

/**
 * 保存用户角色分配结果（当前租户维度）。
 *
 * @param userId 用户ID
 * @param roleIds 角色ID列表（为空表示清空）
 * @returns 执行结果
 */
export async function saveUserRoles(userId: string, roleIds: number[]) {
  return http.post('/sys/user/role/saveByUser', { userId, roleIds })
}

/**
 * 获取部门树
 */
export async function getDepartmentTree(params: { tenantId: string }) {
  return http.post('/sys/department/tree', params)
}

/**
 * 获取部门列表
 */
export async function getDepartmentList() {
  return http.post('/sys/department/list', {})
}

/**
 * 获取职位列表
 */
export async function getPositionList() {
  return http.post('/sys/position/list', {})
}

/**
 * 导出用户（按后台导出配置生成文件）。
 *
 * @param query 查询条件
 * @returns 文件下载响应（blob）
 */
export async function exportUsers(query: Partial<UserQuery>) {
  return exportUser({
    tableCode: 'sys_user',
    query: {
      username: query.username,
      account: (query as any).account,
      status: query.status,
    },
  })
}

export const userApi = {
  getUserList,
  getUserDetail,
  addUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  resetPassword,
  updateUserStatus,
  getUserAssignedRoles,
  saveUserRoles,
  getDepartmentTree,
  getDepartmentList,
  getPositionList,
  exportUsers,
}
