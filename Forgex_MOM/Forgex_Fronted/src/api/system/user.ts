/**
 * 鐢ㄦ埛绠＄悊 API
 * 
 * 鎻愪緵鐢ㄦ埛鐩稿叧鐨?CRUD 鎿嶄綔銆佽鑹插垎閰嶃€佺姸鎬佺鐞嗙瓑鍔熻兘
 * 
 * @author Forgex
 * @version 1.0.0
 */
import http from '../http'
import type { User, UserProfile, UserQuery, Department, Position } from '@/views/system/user/types'
import { exportUser } from '@/api/system/excel'

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
 * 鑾峰彇鐢ㄦ埛鍒嗛〉鍒楄〃
 * 
 * 鎵ц姝ラ锛?
 * 1. 鎺ユ敹鏌ヨ鍙傛暟锛堝寘鍚垎椤点€佹悳绱㈡潯浠讹級
 * 2. 璋冪敤鍚庣鍒嗛〉鎺ュ彛
 * 3. 杩斿洖鍒嗛〉缁撴灉
 * 
 * @param query 鏌ヨ鍙傛暟
 * @param query.username 鐢ㄦ埛鍚嶏紙鍙€夛紝妯＄硦鏌ヨ锛?
 * @param query.account 璐﹀彿锛堝彲閫夛紝妯＄硦鏌ヨ锛?
 * @param query.status 鐘舵€侊紙鍙€夛紝true=鍚敤锛宖alse=绂佺敤锛?
 * @param query.departmentId 閮ㄩ棬 ID锛堝彲閫夛級
 * @param query.positionId 鑱屼綅 ID锛堝彲閫夛級
 * @param query.pageNum 椤电爜锛岄粯璁?1
 * @param query.pageSize 姣忛〉鏉℃暟锛岄粯璁?10
 * @returns 鍒嗛〉缁撴灉锛屽寘鍚?records锛堢敤鎴峰垪琛級鍜?total锛堟€绘暟锛?
 * @throws 鏌ヨ澶辫触鏃舵姏鍑哄紓甯?
 */
export async function getUserList(query: UserQuery) {
  return http.post('/sys/user/page', query)
}

/**
 * 鑾峰彇鐢ㄦ埛璇︽儏
 */
export async function getUserDetail(id: string) {
  return http.post('/sys/user/detail', { id })
}

/**
 * 鏂板鐢ㄦ埛
 */
export async function addUser(user: UserSubmitPayload) {
  return http.post('/sys/user/create', normalizeUserPayload(user))
}

/**
 * 鏇存柊鐢ㄦ埛
 */
export async function updateUser(user: UserSubmitPayload) {
  return http.post('/sys/user/update', normalizeUserPayload(user))
}

/**
 * 鍒犻櫎鐢ㄦ埛
 */
export async function deleteUser(id: string) {
  return http.post('/sys/user/delete', { id })
}

/**
 * 鎵归噺鍒犻櫎鐢ㄦ埛
 */
export async function batchDeleteUsers(ids: string[]) {
  return http.post('/sys/user/batchDelete', { ids })
}

/**
 * 閲嶇疆瀵嗙爜
 */
export async function resetPassword(id: string) {
  return http.post('/sys/user/resetPassword', { id })
}

/**
 * 鏇存柊鐢ㄦ埛鐘舵€?
 */
export async function updateUser状态(id: string, status: boolean) {
  return http.post('/sys/user/update状态', { id, status })
}

/**
 * 鏌ヨ鐢ㄦ埛宸插垎閰嶈鑹诧紙褰撳墠绉熸埛缁村害锛夈€?
 *
 * @param userId 鐢ㄦ埛ID
 * @returns { assignedRoleIds, tenantId }
 */
export async function getUserAssignedRoles(userId: string) {
  return http.post('/sys/user/role/listByUser', { userId })
}

/**
 * 淇濆瓨鐢ㄦ埛瑙掕壊鍒嗛厤缁撴灉锛堝綋鍓嶇鎴风淮搴︼級銆?
 *
 * @param userId 鐢ㄦ埛ID
 * @param roleIds 瑙掕壊ID鍒楄〃锛堜负绌鸿〃绀烘竻绌猴級
 * @returns 鎵ц缁撴灉
 */
export async function saveUserRoles(userId: string, roleIds: string[]) {
  return http.post('/sys/user/role/saveByUser', { userId, roleIds })
}

/**
 * 鑾峰彇閮ㄩ棬鏍?
 */
export async function getDepartmentTree(params: { tenantId: string }) {
  return http.post('/sys/department/tree', params)
}

/**
 * 鑾峰彇閮ㄩ棬鍒楄〃
 */
export async function getDepartmentList() {
  return http.post('/sys/department/list', {})
}

/**
 * 鑾峰彇鑱屼綅鍒楄〃
 */
export async function getPositionList() {
  return http.post('/sys/position/list', {})
}

/**
 * 瀵煎嚭鐢ㄦ埛锛堟寜鍚庡彴瀵煎嚭閰嶇疆鐢熸垚鏂囦欢锛夈€?
 *
 * @param query 鏌ヨ鏉′欢
 * @returns 鏂囦欢涓嬭浇鍝嶅簲锛坆lob锛?
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
  updateUser状态,
  getUserAssignedRoles,
  saveUserRoles,
  getDepartmentTree,
  getDepartmentList,
  getPositionList,
  exportUsers,
}
