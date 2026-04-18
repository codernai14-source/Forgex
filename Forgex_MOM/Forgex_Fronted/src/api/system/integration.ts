import http from '../http'

export type IntegrationDirection = 'INBOUND' | 'OUTBOUND'
export type ParamDirection = 'REQUEST' | 'RESPONSE'
export type AuthorizationType = 'WHITELIST' | 'TOKEN'
export type CallMethod = 'HTTP' | 'TCP'
export type NodeType = 'OBJECT' | 'ARRAY' | 'FIELD'

export interface ThirdSystemQuery {
  pageNum?: number
  pageSize?: number
  systemCode?: string
  systemName?: string
  status?: number
}

export interface ThirdSystemSubmit {
  id?: number
  systemCode: string
  systemName: string
  ipAddress?: string
  contactInfo?: string
  remark?: string
  status?: number
}

export interface ThirdSystemItem extends ThirdSystemSubmit {
  createTime?: string
  updateTime?: string
}

export interface ThirdAuthorizationQuery {
  thirdSystemId?: number
  authType?: AuthorizationType
  status?: number
}

export interface ThirdAuthorizationSubmit {
  id?: number
  thirdSystemId: number
  authType: AuthorizationType
  tokenValue?: string
  tokenExpireHours?: number
  whitelistIps?: string
  status?: number
  remark?: string
}

export interface ThirdAuthorizationItem extends ThirdAuthorizationSubmit {
  systemName?: string
  tokenExpireTime?: string
  createTime?: string
  updateTime?: string
}

export interface ApiConfigQuery {
  pageNum?: number
  pageSize?: number
  apiCode?: string
  apiName?: string
  direction?: IntegrationDirection
  moduleCode?: string
  status?: number
}

export interface ApiConfigSubmit {
  id?: number
  apiCode: string
  apiName: string
  apiDesc?: string
  direction: IntegrationDirection
  apiPath?: string
  processorBean?: string
  callMethod: CallMethod
  targetUrl?: string
  timeoutMs?: number
  moduleCode?: string
  status?: number
}

export interface ApiConfigItem extends ApiConfigSubmit {
  callCount?: number
  createBy?: string
  updateBy?: string
  createTime?: string
  updateTime?: string
  statusLoading?: boolean
}

export interface ApiParamConfigItem {
  id?: number
  apiConfigId: number
  parentId?: number | null
  direction: ParamDirection
  nodeType: NodeType
  fieldName: string
  fieldType?: string
  fieldPath: string
  required?: number
  defaultValue?: string
  dictCode?: string
  orderNum?: number
  remark?: string
  children?: ApiParamConfigItem[]
}

export interface JsonImportParam {
  apiConfigId: number
  direction: ParamDirection
  jsonString: string
}

export interface ApiParamMappingQuery {
  apiConfigId: number
  direction: IntegrationDirection
  sourceFieldPath?: string
  targetFieldPath?: string
}

export interface ApiParamMappingItem {
  id?: number
  apiConfigId: number
  direction: IntegrationDirection
  sourceFieldPath: string
  targetFieldPath: string
  transformRule?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ApiParamMappingBatchSaveParam {
  apiConfigId: number
  direction: IntegrationDirection
  mappings: ApiParamMappingItem[]
}

export interface ApiCallLogQuery {
  pageNum?: number
  pageSize?: number
  apiConfigId?: number
  callDirection?: IntegrationDirection
  callStatus?: string
  startTime?: string
  endTime?: string
  callerIp?: string
}

export interface ApiCallLogItem {
  id: number
  apiConfigId?: number
  callDirection?: IntegrationDirection
  callerIp?: string
  requestData?: string
  responseData?: string
  callStatus?: string
  errorMessage?: string
  costTimeMs?: number
  callTime?: string
  createBy?: string
  updateBy?: string
  createTime?: string
  updateTime?: string
}

export function getThirdSystemList(query: ThirdSystemQuery) {
  return http.post<{ records: ThirdSystemItem[]; total: number }>('/integration/third-system/page', query)
}

export function listThirdSystems(query: ThirdSystemQuery) {
  return http.post<ThirdSystemItem[]>('/integration/third-system/list', query)
}

export function getThirdSystemDetail(id: number) {
  return http.get<ThirdSystemItem>(`/integration/third-system/detail/${id}`)
}

export function addThirdSystem(system: ThirdSystemSubmit) {
  return http.post('/integration/third-system/create', system)
}

export function updateThirdSystem(system: ThirdSystemSubmit) {
  return http.post('/integration/third-system/update', system)
}

export function deleteThirdSystem(id: number) {
  return http.post(`/integration/third-system/delete/${id}`)
}

export function batchDeleteThirdSystems(ids: number[]) {
  return http.post('/integration/third-system/batch-delete', ids)
}

export function getThirdAuthorizationList(query: ThirdAuthorizationQuery) {
  return http.post<ThirdAuthorizationItem[]>('/integration/third-authorization/list', query)
}

export function getThirdAuthorizationDetail(id: number) {
  return http.get<ThirdAuthorizationItem>(`/integration/third-authorization/detail/${id}`)
}

export function getThirdAuthorizationBySystemId(thirdSystemId: number) {
  return http.get<ThirdAuthorizationItem | null>(`/integration/third-authorization/by-system/${thirdSystemId}`)
}

export function addThirdAuthorization(payload: ThirdAuthorizationSubmit) {
  return http.post('/integration/third-authorization/create', payload)
}

export function updateThirdAuthorization(payload: ThirdAuthorizationSubmit) {
  return http.post('/integration/third-authorization/update', payload)
}

export function generateThirdAuthorizationToken(thirdSystemId: number, expireHours?: number) {
  const query = expireHours ? `?expireHours=${expireHours}` : ''
  return http.post<string>(`/integration/third-authorization/generate-token/${thirdSystemId}${query}`)
}

export function getApiConfigList(query: ApiConfigQuery) {
  return http.post<{ records: ApiConfigItem[]; total: number }>('/integration/api-config/page', query)
}

export function listApiConfigs(query: ApiConfigQuery) {
  return http.post<ApiConfigItem[]>('/integration/api-config/list', query)
}

export function getApiConfigDetail(id: number) {
  return http.get<ApiConfigItem>(`/integration/api-config/detail/${id}`)
}

export function addApiConfig(apiConfig: ApiConfigSubmit) {
  return http.post('/integration/api-config/create', apiConfig)
}

export function updateApiConfig(apiConfig: ApiConfigSubmit) {
  return http.post('/integration/api-config/update', apiConfig)
}

export function deleteApiConfig(id: number) {
  return http.post(`/integration/api-config/delete/${id}`)
}

export function batchDeleteApiConfigs(ids: number[]) {
  return http.post('/integration/api-config/batch-delete', ids)
}

export function enableApiConfig(id: number) {
  return http.post(`/integration/api-config/enable/${id}`)
}

export function disableApiConfig(id: number) {
  return http.post(`/integration/api-config/disable/${id}`)
}

export function getApiParamTree(apiConfigId: number, direction?: ParamDirection) {
  const search = new URLSearchParams()
  search.set('apiConfigId', String(apiConfigId))
  if (direction) {
    search.set('direction', direction)
  }
  return http.get<ApiParamConfigItem[]>(`/integration/param-config/tree?${search.toString()}`)
}

export function parseApiParamJson(payload: Pick<JsonImportParam, 'jsonString'>) {
  return http.post<ApiParamConfigItem[]>('/integration/param-config/parse-json', payload)
}

export function importApiParamJson(payload: JsonImportParam) {
  return http.post('/integration/param-config/import-json', payload)
}

export function getApiParamMappings(payload: ApiParamMappingQuery) {
  return http.post<ApiParamMappingItem[]>('/integration/param-mapping/list', payload)
}

export function batchSaveApiParamMappings(payload: ApiParamMappingBatchSaveParam) {
  return http.post('/integration/param-mapping/batch-save', payload)
}

export function getApiCallLogList(query: ApiCallLogQuery) {
  return http.post<{ records: ApiCallLogItem[]; total: number }>('/integration/call-log/page', query)
}

export function getApiCallLogDetail(id: number, callTime: string) {
  const search = new URLSearchParams({ callTime })
  return http.get<ApiCallLogItem>(`/integration/call-log/detail/${id}?${search.toString()}`)
}

export const integrationApi = {
  getThirdSystemList,
  listThirdSystems,
  getThirdSystemDetail,
  addThirdSystem,
  updateThirdSystem,
  deleteThirdSystem,
  batchDeleteThirdSystems,
  getThirdAuthorizationList,
  getThirdAuthorizationDetail,
  getThirdAuthorizationBySystemId,
  addThirdAuthorization,
  updateThirdAuthorization,
  generateThirdAuthorizationToken,
  getApiConfigList,
  listApiConfigs,
  getApiConfigDetail,
  addApiConfig,
  updateApiConfig,
  deleteApiConfig,
  batchDeleteApiConfigs,
  enableApiConfig,
  disableApiConfig,
  getApiParamTree,
  parseApiParamJson,
  importApiParamJson,
  getApiParamMappings,
  batchSaveApiParamMappings,
  getApiCallLogList,
  getApiCallLogDetail,
}
