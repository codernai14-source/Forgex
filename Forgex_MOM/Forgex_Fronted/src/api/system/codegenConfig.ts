import http from '../http'
import type { CodegenRequest } from './codegen'

export interface CodegenConfigItem extends CodegenRequest {
  id?: number
  configId?: number
  configName: string
  datasourceName?: string
  remark?: string
  createTime?: string
  updateTime?: string
  lastGenerateTime?: string
}

export interface CodegenConfigQuery {
  configName?: string
  pageType?: string
  moduleName?: string
  bizName?: string
  menuName?: string
  mainTableName?: string
  pageNum?: number
  pageSize?: number
}

export interface CodegenConfigSaveParam extends CodegenRequest {
  id?: number
  configName?: string
  remark?: string
}

export interface CodegenConfigPageResult {
  records: CodegenConfigItem[]
  total: number
  current: number
  size: number
}

export function getCodegenConfigPage(data: CodegenConfigQuery) {
  return http.post<CodegenConfigPageResult>('/sys/codegen/config/page', data)
}

export function getCodegenConfigDetail(id: number) {
  return http.post<CodegenConfigItem>('/sys/codegen/config/detail', { id })
}

export function saveCodegenConfig(data: CodegenConfigSaveParam) {
  return http.post<number>('/sys/codegen/config/save', data)
}

export function deleteCodegenConfig(id: number) {
  return http.post<void>('/sys/codegen/config/delete', { id })
}
