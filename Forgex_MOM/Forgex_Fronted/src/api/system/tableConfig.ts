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
  renderType?: string
  permKey?: string
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
  return http.get<TableConfigDetail>(`/sys/common/table/config/${id}`)
}

export function createTableConfig(data: TableConfigDetail) {
  return http.post<{ id: number }>('/sys/common/table/config', data)
}

export function updateTableConfig(data: TableConfigDetail) {
  return http.put<void>(`/sys/common/table/config/${data.id}`, data)
}

export function deleteTableConfig(id: number) {
  return http.delete<void>(`/sys/common/table/config/${id}`)
}

export function batchDeleteTableConfig(ids: number[]) {
  return http.delete<void>('/sys/common/table/config/batch', { data: ids })
}

export function toggleTableConfigStatus(id: number, enabled: boolean) {
  return http.put<void>(`/sys/common/table/config/${id}/status`, { enabled })
}
