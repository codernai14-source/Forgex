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

export function getTableConfig(data: { tableCode: string }) {
  return http.post<FxTableConfig>('/sys/common/table/config/get', data)
}
