import type { AxiosResponse } from 'axios'

/**
 * 规范化布尔状态值。
 * 支持 boolean / number / string 三类输入，无法识别时返回 undefined。
 */
export function normalizeBooleanValue(value: unknown): boolean | undefined {
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

/**
 * 用户状态展示统一转为 boolean。
 */
export function normalizeUserStatus(value: unknown): boolean {
  return normalizeBooleanValue(value) ?? false
}

/**
 * 角色筛选值统一转为去重后的字符串数组。
 */
export function normalizeRoleFilterValues(value: unknown): string[] {
  if (value === undefined || value === null || value === '') {
    return []
  }
  const values = Array.isArray(value) ? value : [value]
  return Array.from(new Set(values.map(item => String(item).trim()).filter(Boolean)))
}

/**
 * 统一整理用户列表查询参数：
 * 1. entryDate -> entryDateStart / entryDateEnd
 * 2. roleId / role_ids -> roleIds
 */
export function normalizeUserQuery<T extends Record<string, any>>(query: T): Record<string, any> {
  const normalized = { ...query }

  const entryDateRange = Array.isArray(normalized.entryDate) ? normalized.entryDate : []
  if (entryDateRange.length === 2) {
    normalized.entryDateStart = String(entryDateRange[0]).slice(0, 10)
    normalized.entryDateEnd = String(entryDateRange[1]).slice(0, 10)
  }
  delete normalized.entryDate

  const roleFilterValues = normalizeRoleFilterValues(
    normalized.roleIds ?? normalized.roleId ?? normalized.role_ids,
  )
  if (roleFilterValues.length > 0) {
    normalized.roleIds = roleFilterValues
  }
  delete normalized.roleId
  delete normalized.role_ids

  return normalized
}

/**
 * 统一下载二进制响应为本地文件。
 */
export function downloadBlobResponse(
  response: AxiosResponse<ArrayBuffer>,
  fileName: string,
) {
  const blob = new Blob([response.data], {
    type: response.headers?.['content-type'] || 'application/octet-stream',
  })
  const url = window.URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = fileName
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  window.URL.revokeObjectURL(url)
}
