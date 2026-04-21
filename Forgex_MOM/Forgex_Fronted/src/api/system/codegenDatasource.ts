import http from '../http'

/**
 * 代码生成数据源信息
 */
export interface CodegenDatasourceItem {
  id?: number
  datasourceCode: string
  datasourceName: string
  dbType: string
  jdbcUrl: string
  username: string
  schemaName?: string
  enabled: boolean
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 代码生成数据源分页查询参数
 */
export interface CodegenDatasourceQuery {
  datasourceCode?: string
  datasourceName?: string
  dbType?: string
  enabled?: boolean
  pageNum?: number
  pageSize?: number
}

/**
 * 代码生成数据源保存参数
 */
export interface CodegenDatasourceSaveParam {
  id?: number
  datasourceCode: string
  datasourceName: string
  dbType: string
  jdbcUrl: string
  username: string
  password?: string
  schemaName?: string
  enabled: boolean
  remark?: string
}

/**
 * 测试连接参数
 */
export interface CodegenDatasourceTestParam {
  id?: number
  jdbcUrl: string
  username: string
  password?: string
  dbType: string
}

/**
 * 分页结果
 */
export interface CodegenDatasourcePageResult {
  records: CodegenDatasourceItem[]
  total: number
  current: number
  size: number
}

/**
 * 查询数据源分页列表
 */
export function getCodegenDatasourcePage(data: CodegenDatasourceQuery) {
  return http.post<CodegenDatasourcePageResult>('/sys/codegen/datasource/page', data)
}

/**
 * 查询启用中的数据源列表
 */
export function listCodegenDatasources() {
  return http.post<CodegenDatasourceItem[]>('/sys/codegen/datasource/list', {})
}

/**
 * 查询数据源详情
 */
export function getCodegenDatasourceDetail(id: number) {
  return http.post<CodegenDatasourceItem>('/sys/codegen/datasource/detail', { id })
}

/**
 * 保存数据源
 */
export function saveCodegenDatasource(data: CodegenDatasourceSaveParam) {
  return http.post<number>('/sys/codegen/datasource/save', data)
}

/**
 * 删除数据源
 */
export function deleteCodegenDatasource(id: number) {
  return http.post<void>('/sys/codegen/datasource/delete', { id })
}

/**
 * 测试数据源连接
 */
export function testCodegenDatasource(data: CodegenDatasourceTestParam) {
  return http.post<boolean>('/sys/codegen/datasource/test', data)
}
