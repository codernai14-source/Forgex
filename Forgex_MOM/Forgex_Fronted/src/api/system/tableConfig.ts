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

export interface UserColumnItem {
  field: string
  visible: boolean
  order: number
}

export interface UserColumnConfigParam {
  tableCode: string
  pageSize?: number
  columns: UserColumnItem[]
}

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

export function getTableConfig(data: { tableCode: string }) {
  return http.post<FxTableConfig>('/sys/common/table/config/get', data, { silentError: true } as any)
}

export function getTableConfigList(params: TableConfigListParams) {
  return http.post<TableConfigListResult>('/sys/common/table/config/list', params)
}

export function getTableConfigDetail(id: number) {
  return http.post<TableConfigDetail>('/sys/common/table/config/info', { id })
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

export function getUserTableConfig(params: {
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<FxTableConfig>('/sys/common/table/config/user', params, { silentError: true } as any)
}

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

export function deleteUserTableConfig(params: {
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<boolean>('/sys/common/table/config/user/delete', params)
}

export function getUserColumns(tableCode: string) {
  return http.post<UserColumnConfigResult>('/sys/common/table/config/user/columns', { tableCode }, { silentError: true } as any)
}

export function saveUserColumns(data: UserColumnConfigParam) {
  return http.post<number>('/sys/common/table/config/user/columns/save', data)
}

export function resetUserColumns(tableCode: string) {
  return http.post<void>('/sys/common/table/config/user/columns/reset', { tableCode })
}
