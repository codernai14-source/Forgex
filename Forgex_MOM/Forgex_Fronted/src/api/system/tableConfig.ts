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
  console.log('API: getTableConfigDetail 琚皟鐢紝ID:', id, '绫诲瀷:', typeof id)
  const url = `/sys/common/table/config/info`
  console.log('璇锋眰URL:', url)
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

export function toggleTableConfig状态(id: number, enabled: boolean) {
  return http.post<void>('/sys/common/table/config/update状态', { id, enabled })
}

/**
 * 鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆鐩稿叧鎺ュ彛
 */

/**
 * 鑾峰彇鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆
 * 
 * @param params 鍙傛暟锛坱ableCode, tenantId, userId锛?
 * @returns 鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆
 */
export function getUserTableConfig(params: { 
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<FxTableConfig>('/sys/common/table/config/user', params, { silentError: true } as any)
}

/**
 * 淇濆瓨鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆
 * 
 * @param data 鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆鏁版嵁
 * @returns 閰嶇疆 ID
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
 * 鍒犻櫎鐢ㄦ埛绾у埆琛ㄦ牸閰嶇疆
 * 
 * @param params 鍙傛暟锛坱ableCode, tenantId, userId锛?
 * @returns 鏄惁鍒犻櫎鎴愬姛
 */
export function deleteUserTableConfig(params: { 
  tableCode: string
  tenantId: number
  userId: number
}) {
  return http.post<boolean>('/sys/common/table/config/user/delete', params)
}

/**
 * 鐢ㄦ埛鍒楅厤缃」
 */
export interface UserColumnItem {
  field: string
  visible: boolean
  order: number
}

/**
 * 鐢ㄦ埛鍒楅厤缃弬鏁?
 */
export interface UserColumnConfigParam {
  tableCode: string
  pageSize?: number
  columns: UserColumnItem[]
}

/**
 * 鐢ㄦ埛鍒楅厤缃繑鍥炵粨鏋?
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
 * 鑾峰彇鐢ㄦ埛鍒楅厤缃?
 * <p>
 * 鑾峰彇褰撳墠鐢ㄦ埛瀵规寚瀹氳〃鏍肩殑涓€у寲鍒楅厤缃紝鍖呮嫭鍒楃殑鏄剧ず/闅愯棌鍜屾帓搴忋€?
 * </p>
 * 
 * @param tableCode 琛ㄦ牸缂栫爜
 * @returns 鐢ㄦ埛鍒楅厤缃?
 */
export function getUserColumns(tableCode: string) {
  return http.post<UserColumnConfigResult>('/sys/common/table/config/user/columns', { tableCode }, { silentError: true } as any)
}

/**
 * 淇濆瓨鐢ㄦ埛鍒楅厤缃?
 * <p>
 * 淇濆瓨褰撳墠鐢ㄦ埛瀵规寚瀹氳〃鏍肩殑涓€у寲鍒楅厤缃紝鍖呮嫭鍒楃殑鏄剧ず/闅愯棌鍜屾帓搴忋€?
 * </p>
 * 
 * @param data 鐢ㄦ埛鍒楅厤缃弬鏁?
 * @returns 閰嶇疆 ID
 */
export function saveUserColumns(data: UserColumnConfigParam) {
  return http.post<number>('/sys/common/table/config/user/columns/save', data)
}

/**
 * 閲嶇疆鐢ㄦ埛鍒楅厤缃?
 * <p>
 * 鍒犻櫎褰撳墠鐢ㄦ埛瀵规寚瀹氳〃鏍肩殑涓€у寲鍒楅厤缃紝鎭㈠涓洪粯璁ら厤缃€?
 * </p>
 * 
 * @param tableCode 琛ㄦ牸缂栫爜
 * @returns 鎿嶄綔缁撴灉
 */
export function resetUserColumns(tableCode: string) {
  return http.post<void>('/sys/common/table/config/user/columns/reset', { tableCode })
}
