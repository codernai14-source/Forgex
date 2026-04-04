import http from '../http'

export interface FxTableColumn {
  field: string
  title: string
  align?: 'left' | 'center' | 'right'
  width?: number
  fixed?: 'left' | 'right'
  ellipsis?: boolean
  sortable?: boolean
  sorterField?: string
  queryable?: boolean
  queryType?: string
  queryOperator?: string
  dictCode?: string
  dictField?: string
  renderType?: string
  permKey?: string
  visible?: boolean
  order?: number
}

export interface FxTableQueryField {
  field: string
  label: string
  queryType: string
  queryOperator: string
  dictCode?: string
}

export interface FxTableConfig {
  tableCode: string
  tableName: string
  tableType: string
  rowKey: string
  defaultPageSize: number
  defaultSortJson?: string
  columns: FxTableColumn[]
  queryFields: FxTableQueryField[]
  version: number
}

export interface TableConfigListParams {
  tableCode?: string
  tableName?: string
  tableType?: string
  enabled?: boolean
  current?: number
  pageSize?: number
}

export interface TableConfigItem {
  id?: number
  tableCode: string
  tableNameI18nJson?: string
  tableType: string
  rowKey?: string
  defaultPageSize: number
  defaultSortJson?: string
  enabled: boolean
  version?: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  statusLoading?: boolean
}

export interface TableColumnConfigItem {
  id?: number
  tableCode: string
  field: string
  titleI18nJson?: string
  align?: string
  width?: number
  fixed?: string
  ellipsis?: boolean
  sortable?: boolean
  sorterField?: string
  queryable?: boolean
  queryType?: string
  queryOperator?: string
  dictCode?: string
  renderType?: string
  permKey?: string
  orderNum: number
  enabled: boolean
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  tempId?: number
}

export interface TableConfigDetail extends TableConfigItem {
  columns: TableColumnConfigItem[]
}

export interface TableConfigListResult {
  records: TableConfigItem[]
  total: number
  current: number
  pageSize: number
}

export function getTableConfig(data: { tableCode: string }) {
  return http.post<FxTableConfig>('/sys/common/table/config/get', data, { silentError: true } as any)
}

export function getTableConfigList(params: TableConfigListParams) {
  return http.post<TableConfigListResult>('/sys/common/table/config/list', params)
}

export function getTableConfigDetail(id: number) {
  console.log('API: getTableConfigDetail 被调用，ID:', id, '类型:', typeof id)
  const url = `/sys/common/table/config/info`
  console.log('请求URL:', url)
  return http.post<TableConfigDetail>(url, { id })
}

export function createTableConfig(data: TableConfigDetail) {
  return http.post<{ id: number }>('/sys/common/table/config', data)
}

export function updateTableConfig(data: TableConfigDetail) {
  return http.post<void>('/sys/common/table/config/update', data)
}

export function deleteTableConfig(id: number) {
  return http.post<void>('/sys/common/table/config/delete', { id })
}

export function batchDeleteTableConfig(ids: number[]) {
  return http.post<void>('/sys/common/table/config/batchDelete', { ids })
}

export function toggleTableConfigStatus(id: number, enabled: boolean) {
  return http.post<void>('/sys/common/table/config/updateStatus', { id, enabled })
}

/**
 * 用户级别表格配置相关接口
 */

/**
 * 获取用户级别表格配置
 * 
 * @param params 参数（tableCode, tenantId, userId）
 * @returns 用户级别表格配置
 */
export function getUserTableConfig(params: { 
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<FxTableConfig>('/sys/common/table/config/user', params, { silentError: true } as any)
}

/**
 * 保存用户级别表格配置
 * 
 * @param data 用户级别表格配置数据
 * @returns 配置 ID
 */
export function saveUserTableConfig(data: {
  tableCode: string
  tenantId: number
  userId: number
  columnConfig?: string
  queryConfig?: string
  sortConfig?: string
  pageSize?: number
}) {
  return http.post<number>('/sys/common/table/config/user', data)
}

/**
 * 删除用户级别表格配置
 * 
 * @param params 参数（tableCode, tenantId, userId）
 * @returns 是否删除成功
 */
export function deleteUserTableConfig(params: { 
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<boolean>('/sys/common/table/config/user/delete', params)
}

/**
 * 用户列配置项
 */
export interface UserColumnItem {
  field: string
  visible: boolean
  order: number
}

/**
 * 用户列配置参数
 */
export interface UserColumnConfigParam {
  tableCode: string
  pageSize?: number
  columns: UserColumnItem[]
}

/**
 * 用户列配置返回结果
 */
export interface UserColumnConfigResult {
  tableCode: string
  userId: number
  tenantId: number
  pageSize?: number
  version?: number
  createTime?: string
  updateTime?: string
  columns: FxTableColumn[] | null
}

/**
 * 获取用户列配置
 * <p>
 * 获取当前用户对指定表格的个性化列配置，包括列的显示/隐藏和排序。
 * </p>
 * 
 * @param tableCode 表格编码
 * @returns 用户列配置
 */
export function getUserColumns(tableCode: string) {
  return http.post<UserColumnConfigResult>('/sys/common/table/config/user/columns', { tableCode }, { silentError: true } as any)
}

/**
 * 保存用户列配置
 * <p>
 * 保存当前用户对指定表格的个性化列配置，包括列的显示/隐藏和排序。
 * </p>
 * 
 * @param data 用户列配置参数
 * @returns 配置 ID
 */
export function saveUserColumns(data: UserColumnConfigParam) {
  return http.post<number>('/sys/common/table/config/user/columns/save', data)
}

/**
 * 重置用户列配置
 * <p>
 * 删除当前用户对指定表格的个性化列配置，恢复为默认配置。
 * </p>
 * 
 * @param tableCode 表格编码
 * @returns 操作结果
 */
export function resetUserColumns(tableCode: string) {
  return http.post<void>('/sys/common/table/config/user/columns/reset', { tableCode })
}
