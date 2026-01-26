import http from '../http'

/**
 * 租户类型枚举
 */
export enum TenantTypeEnum {
  MAIN_TENANT = 'MAIN_TENANT',
  CUSTOMER_TENANT = 'CUSTOMER_TENANT',
  SUPPLIER_TENANT = 'SUPPLIER_TENANT'
}

/**
 * 租户类型标签映射
 */
export const TenantTypeLabels: Record<TenantTypeEnum, string> = {
  [TenantTypeEnum.MAIN_TENANT]: '主租户',
  [TenantTypeEnum.CUSTOMER_TENANT]: '客户租户',
  [TenantTypeEnum.SUPPLIER_TENANT]: '供应商租户'
}

/**
 * 租户查询参数
 */
export interface TenantQueryDTO {
  tenantName?: string
  tenantCode?: string
  tenantType?: TenantTypeEnum
  status?: boolean
}

/**
 * 租户DTO
 */
export interface TenantDTO {
  id: number
  tenantName: string
  tenantCode: string
  description?: string
  logo?: string
  tenantType: TenantTypeEnum
  tenantTypeDesc?: string
  status: boolean
  createTime: string
  updateTime: string
  createBy?: string
  updateBy?: string
}

/**
 * 租户保存参数
 */
export interface TenantSaveParam {
  id?: number
  tenantName: string
  tenantCode: string
  description?: string
  logo?: string
  tenantType: TenantTypeEnum
  status?: boolean
}

/**
 * 查询租户列表
 * @param params 查询参数
 * @returns 租户列表
 */
export function listTenant(params: TenantQueryDTO) {
  return http.post<TenantDTO[]>('/sys/tenant/list', params)
}

/**
 * 分页查询租户列表
 * @param params 分页查询参数
 * @returns 分页结果
 */
export function getTenantPage(params: TenantQueryDTO & { pageNum: number; pageSize: number }) {
  return http.post<{ records: TenantDTO[]; total: number }>('/sys/tenant/page', params)
}

/**
 * 获取租户详情
 * @param params 参数（id）
 * @returns 租户详情
 */
export function getTenant(params: { id: number }) {
  return http.post<TenantDTO>('/sys/tenant/get', params)
}

/**
 * 获取主租户
 * @returns 主租户信息
 */
export function getMainTenant() {
  return http.post<TenantDTO>('/sys/tenant/getMainTenant', {})
}

/**
 * 新增租户
 * @param params 租户参数
 * @returns 租户ID
 */
export function createTenant(params: TenantSaveParam) {
  return http.post<number>('/sys/tenant/create', params)
}

/**
 * 更新租户
 * @param params 租户参数
 * @returns 是否成功
 */
export function updateTenant(params: TenantSaveParam) {
  return http.post<boolean>('/sys/tenant/update', params)
}

/**
 * 删除租户
 * @param params 参数（id）
 * @returns 是否成功
 */
export function deleteTenant(params: { id: number }) {
  return http.post<boolean>('/sys/tenant/delete', params)
}

/**
 * 热更新租户隔离跳过配置
 * @returns 是否成功
 */
export function reloadTenantIgnore() {
  return http.post<boolean>('/sys/tenant/ignore/reload')
}
