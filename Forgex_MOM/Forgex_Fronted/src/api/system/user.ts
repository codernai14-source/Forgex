import http from '../http'
import { downloadTemplate, exportUser } from '@/api/system/excel'
import type { Department, Position, User, UserProfile, UserQuery } from '@/views/system/user/types'
import { normalizeBooleanValue } from '@/utils/user'

type UserSubmitPayload = User & { profile?: UserProfile }

/**
 * 统一整理用户提交载荷，确保 status 字段为标准布尔值。
 */
function normalizeUserPayload(user: UserSubmitPayload): UserSubmitPayload {
  const status = normalizeBooleanValue(user.status)
  return {
    ...user,
    ...(status !== undefined ? { status } : {}),
  }
}

export async function getUserList(query: UserQuery) {
  return http.post('/sys/user/page', query)
}

export async function getUserDetail(id: string) {
  return http.post('/sys/user/detail', { id })
}

export async function addUser(user: UserSubmitPayload) {
  return http.post('/sys/user/create', normalizeUserPayload(user))
}

export async function updateUser(user: UserSubmitPayload) {
  return http.post('/sys/user/update', normalizeUserPayload(user))
}

export async function deleteUser(id: string) {
  return http.post('/sys/user/delete', { id })
}

export async function batchDeleteUsers(ids: string[]) {
  return http.post('/sys/user/batchDelete', { ids })
}

export async function resetPassword(id: string) {
  return http.post('/sys/user/resetPassword', { id })
}

export async function updateUserStatus(id: string, status: boolean) {
  return http.post('/sys/user/updateStatus', { id, status })
}

export async function getUserAssignedRoles(userId: string) {
  return http.post('/sys/user/role/listByUser', { userId })
}

export async function saveUserRoles(userId: string, roleIds: string[]) {
  return http.post('/sys/user/role/saveByUser', { userId, roleIds })
}

export async function getDepartmentTree(params: { tenantId: string }) {
  return http.post<Department[]>('/sys/department/tree', params)
}

export async function getDepartmentList() {
  return http.post<Department[]>('/sys/department/list', {})
}

export async function getPositionList() {
  return http.post<Position[]>('/sys/position/list', {})
}

export async function exportUsers(query: Partial<UserQuery>) {
  return exportUser({
    tableCode: 'sys_user',
    query: {
      username: query.username,
      account: query.account,
      phone: query.phone,
      departmentId: query.departmentId,
      positionId: query.positionId,
      status: query.status,
      employeeId: query.employeeId,
      userSource: query.userSource,
      roleIds: query.roleIds,
      entryDateStart: query.entryDateStart,
      entryDateEnd: query.entryDateEnd,
    },
  })
}

export async function syncThirdParty(apiCode: string) {
  return http.post('/sys/user/sync-third-party', { apiCode })
}

export async function pullFromThirdParty(apiCode: string) {
  return http.post('/sys/user/pull-from-third-party', { apiCode })
}

export async function importUsers(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return http.post('/sys/user/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    showSuccessMessage: true,
  })
}

export async function downloadUserTemplate() {
  return downloadTemplate({ tableCode: 'sys_user' })
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
  syncThirdParty,
  pullFromThirdParty,
  importUsers,
  downloadUserTemplate,
}
