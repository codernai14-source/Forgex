/**
 * 用户管理 API
 *
 * 提供用户查询、增删改、状态维护、角色分配、部门与岗位列表等接口封装。
 */
import http from '../http'
import { exportUser } from '@/api/system/excel'
import type { Department, Position, User, UserProfile, UserQuery } from '@/views/system/user/types'

type UserSubmitPayload = User & { profile?: UserProfile }

function normalizeBooleanValue(value: unknown): boolean | undefined {
  if (typeof value === 'boolean') {
    return value
  }
  if (typeof value === 'number') {
    if (value === 1) return true
    if (value === 0) return false
    return undefined
  }
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    if (normalized === 'true' || normalized === '1') return true
    if (normalized === 'false' || normalized === '0') return false
  }
  return undefined
}

function normalizeUserPayload(user: UserSubmitPayload): UserSubmitPayload {
  const status = normalizeBooleanValue(user.status)
  return {
    ...user,
    ...(status !== undefined ? { status } : {}),
  }
}

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
export async function addUser(user: UserSubmitPayload) {
  return http.post('/sys/user/create', normalizeUserPayload(user))
}

/**
 * 更新用户
 */
export async function updateUser(user: UserSubmitPayload) {
  return http.post('/sys/user/update', normalizeUserPayload(user))
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
 * 查询用户已分配角色
 */
export async function getUserAssignedRoles(userId: string) {
  return http.post('/sys/user/role/listByUser', { userId })
}

/**
 * 保存用户角色分配
 */
export async function saveUserRoles(userId: string, roleIds: string[]) {
  return http.post('/sys/user/role/saveByUser', { userId, roleIds })
}

/**
 * 获取部门树
 */
export async function getDepartmentTree(params: { tenantId: string }) {
  return http.post<Department[]>('/sys/department/tree', params)
}

/**
 * 获取部门列表
 */
export async function getDepartmentList() {
  return http.post<Department[]>('/sys/department/list', {})
}

/**
 * 获取岗位列表
 */
export async function getPositionList() {
  return http.post<Position[]>('/sys/position/list', {})
}

/**
 * 导出用户
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
