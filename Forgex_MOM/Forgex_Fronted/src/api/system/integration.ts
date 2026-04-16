import http from '../http'

/**
 * 第三方系统查询参数
 */
export interface ThirdSystemQuery {
  pageNum?: number
  pageSize?: number
  systemCode?: string
  systemName?: string
  status?: number
}

/**
 * 第三方系统提交数据
 */
export interface ThirdSystemSubmit {
  id?: number
  systemCode: string
  systemName: string
  ipAddress?: string
  contactInfo?: string
  remark?: string
  status?: number
}

/**
 * 第三方系统列表项
 */
export interface ThirdSystemItem {
  id?: number
  systemCode: string
  systemName: string
  ipAddress?: string
  contactInfo?: string
  remark?: string
  status: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

/**
 * 获取第三方系统分页列表
 */
export async function getThirdSystemList(query: ThirdSystemQuery) {
  return http.post('/api/integration/third-system/page', query)
}

/**
 * 获取第三方系统详情
 */
export async function getThirdSystemDetail(id: number) {
  return http.get(`/api/integration/third-system/detail/${id}`)
}

/**
 * 新增第三方系统
 */
export async function addThirdSystem(system: ThirdSystemSubmit) {
  return http.post('/api/integration/third-system/create', system)
}

/**
 * 更新第三方系统
 */
export async function updateThirdSystem(system: ThirdSystemSubmit) {
  return http.post('/api/integration/third-system/update', system)
}

/**
 * 删除第三方系统
 */
export async function deleteThirdSystem(id: number) {
  return http.post(`/api/integration/third-system/delete/${id}`)
}

/**
 * 批量删除第三方系统
 */
export async function batchDeleteThirdSystems(ids: number[]) {
  return http.post('/api/integration/third-system/batch-delete', ids)
}

/**
 * API 接口配置查询参数
 */
export interface ApiConfigQuery {
  pageNum?: number
  pageSize?: number
  apiCode?: string
  apiName?: string
  systemId?: number
  operationType?: string
  callMethod?: string
  status?: number
}

/**
 * API 接口配置提交数据
 */
export interface ApiConfigSubmit {
  id?: number
  apiCode: string
  apiName: string
  systemId: number
  requestPath?: string
  requestMethod?: string
  operationType: string
  callMethod: string
  timeoutSeconds?: number
  retryTimes?: number
  status?: number
  remark?: string
}

/**
 * API 接口配置详情
 */
export interface ApiConfigDetail {
  id: number
  apiCode: string
  apiName: string
  systemId: number
  systemName?: string
  requestPath?: string
  requestMethod?: string
  operationType: string
  callMethod: string
  timeoutSeconds?: number
  retryTimes?: number
  status: number
  remark?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

/**
 * API 接口配置列表项
 */
export interface ApiConfigItem {
  id?: number
  apiCode: string
  apiName: string
  systemId: number
  systemName?: string
  operationType: string
  callMethod: string
  status: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  statusLoading?: boolean
}

/**
 * 获取 API 接口配置分页列表
 */
export async function getApiConfigList(query: ApiConfigQuery) {
  return http.post('/api/integration/api-config/page', query)
}

/**
 * 获取 API 接口配置详情
 */
export async function getApiConfigDetail(id: number) {
  return http.get(`/api/integration/api-config/detail/${id}`)
}

/**
 * 新增 API 接口配置
 */
export async function addApiConfig(apiConfig: ApiConfigSubmit) {
  return http.post('/api/integration/api-config/create', apiConfig)
}

/**
 * 更新 API 接口配置
 */
export async function updateApiConfig(apiConfig: ApiConfigSubmit) {
  return http.post('/api/integration/api-config/update', apiConfig)
}

/**
 * 删除 API 接口配置
 */
export async function deleteApiConfig(id: number) {
  return http.post(`/api/integration/api-config/delete/${id}`)
}

/**
 * 批量删除 API 接口配置
 */
export async function batchDeleteApiConfigs(ids: number[]) {
  return http.post('/api/integration/api-config/batch-delete', ids)
}

/**
 * 切换 API 接口配置状态
 */
export async function toggleApiConfigStatus(id: number, status: number) {
  return http.post('/api/integration/api-config/updateStatus', { id, status })
}

/**
 * API 调用记录查询参数
 */
export interface ApiCallLogQuery {
  current?: number
  size?: number
  interfaceName?: string
  interfaceCode?: string
  status?: number
  startTime?: string
  endTime?: string
  callerSystemCode?: string
  callerSystemName?: string
  minCostTime?: number
  maxCostTime?: number
  sortField?: string
  sortOrder?: string
}

/**
 * API 调用记录详情
 */
export interface ApiCallLogDetail {
  id: number
  tenantId?: number
  interfaceName: string
  interfaceCode: string
  requestUrl: string
  requestMethod: string
  requestHeaders?: string
  requestParams?: string
  requestBody?: string
  responseStatus: number
  responseHeaders?: string
  responseBody?: string
  status: number
  callerSystemCode?: string
  callerSystemName?: string
  costTime: number
  errorMessage?: string
  errorStack?: string
  ip?: string
  userAgent?: string
  callTime: string
  createTime?: string
  updateTime?: string
}

/**
 * API 调用记录列表项
 */
export interface ApiCallLogItem {
  id: number
  tenantId?: number
  interfaceName: string
  interfaceCode: string
  requestUrl: string
  requestMethod: string
  responseStatus: number
  status: number
  callerSystemCode?: string
  callerSystemName?: string
  costTime: number
  ip?: string
  callTime: string
  createTime?: string
  updateTime?: string
}

/**
 * 获取 API 调用记录分页列表
 */
export async function getApiCallLogList(query: ApiCallLogQuery) {
  return http.post('/api/integration/call-log/page', query)
}

/**
 * 获取 API 调用记录详情
 */
export async function getApiCallLogDetail(id: number) {
  return http.get(`/api/integration/call-log/detail/${id}`)
}

/**
 * 统一导出 API 对象
 */
export const integrationApi = {
  getThirdSystemList,
  getThirdSystemDetail,
  addThirdSystem,
  updateThirdSystem,
  deleteThirdSystem,
  batchDeleteThirdSystems,
  getApiConfigList,
  getApiConfigDetail,
  addApiConfig,
  updateApiConfig,
  deleteApiConfig,
  batchDeleteApiConfigs,
  toggleApiConfigStatus,
  getApiCallLogList,
  getApiCallLogDetail
}
