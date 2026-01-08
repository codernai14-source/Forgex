/**
 * 用户管理 API
 */
import http from '../http'
import type { User, UserProfile, UserQuery, Department, Position } from '@/views/system/user/types'

/**
 * 获取用户列表
 */
export async function getUserList(query: UserQuery) {
  return http.get('/sys/user/page', { params: query })
}

/**
 * 获取用户详情
 */
export async function getUserDetail(id: string) {
  return http.get(`/sys/user/${id}`)
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
 * 获取部门列表
 */
export async function getDepartmentList() {
  return http.get('/sys/department/list')
}

/**
 * 获取职位列表
 */
export async function getPositionList() {
  return http.get('/sys/position/list')
}

// 导出API对象以保持兼容性
export const userApi = {
  getUserList,
  getUserDetail,
  addUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  resetPassword,
  getDepartmentList,
  getPositionList
}
