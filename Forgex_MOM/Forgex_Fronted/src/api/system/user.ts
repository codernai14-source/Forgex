/**
 * 用户管理 API
 * 
 * 提供用户相关的 CRUD 操作、角色分配、状态管理等功能
 * 
 * @author Forgex
 * @version 1.0.0
 */
import http from '../http'
import type { User, UserProfile, UserQuery, Department, Position } from '@/views/system/user/types'
import { exportUser } from '@/api/system/excel'

/**
 * 获取用户分页列表
 * 
 * 执行步骤：
 * 1. 接收查询参数（包含分页、搜索条件）
 * 2. 调用后端分页接口
 * 3. 返回分页结果
 * 
 * @param query 查询参数
 * @param query.username 用户名（可选，模糊查询）
 * @param query.account 账号（可选，模糊查询）
 * @param query.status 状态（可选，true=启用，false=禁用）
 * @param query.departmentId 部门 ID（可选）
 * @param query.positionId 职位 ID（可选）
 * @param query.pageNum 页码，默认 1
 * @param query.pageSize 每页条数，默认 10
 * @returns 分页结果，包含 records（用户列表）和 total（总数）
 * @throws 查询失败时抛出异常
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
