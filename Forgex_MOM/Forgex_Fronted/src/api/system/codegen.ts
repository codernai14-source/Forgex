import axios from 'axios'
import http from '../http'
import { getLocale } from '@/locales'

export type CodegenPageType = 'SINGLE' | 'MASTER_DETAIL' | 'TREE_SINGLE' | 'TREE_DOUBLE'
export type CodegenGenerateItem = 'backend' | 'frontend' | 'sql' | 'android'

/**
 * 元数据选项
 */
export interface CodegenMetaOption {
  label: string
  value: string
}

/**
 * 代码生成字段配置
 */
export interface CodegenColumnConfig {
  columnName: string
  dataType?: string
  columnType?: string
  columnComment?: string
  isPrimaryKey?: boolean
  isAutoIncrement?: boolean
  isNullable?: boolean
  defaultValue?: string
  characterMaximumLength?: number
  numericPrecision?: number
  numericScale?: number
  javaFieldName?: string
  javaType?: string
  javaTypeFullName?: string
  needImport?: boolean
  isBaseField?: boolean
  columnNameEn?: string
  queryType?: string
  queryOperator?: string
  dictCode?: string
  formType?: string
  required?: boolean
  width?: number
  align?: string
  sortable?: boolean
  renderType?: string
  queryable?: boolean
  tableShow?: boolean
  formShow?: boolean
  defaultSort?: boolean
  sortDirection?: 'ASC' | 'DESC'
}

/**
 * 代码生成请求
 */
export interface CodegenRequest {
  configId?: number
  datasourceId: number
  datasourceCode?: string
  schemaName: string
  pageType: CodegenPageType
  mainTableName: string
  treeTableName?: string
  subTableName?: string
  mainPkColumn?: string
  treePkColumn?: string
  treeParentColumn?: string
  treeLabelColumn?: string
  treeSortColumn?: string
  treeFilterColumn?: string
  subFkColumn?: string
  subPkColumn?: string
  moduleName: string
  bizName: string
  entityName: string
  treeEntityName?: string
  subEntityName?: string
  packageName: string
  author: string
  menuName: string
  menuIcon?: string
  parentMenuPath?: string
  tableCodePrefix?: string
  androidFeatureKey?: string
  generateItems: CodegenGenerateItem[]
  mainColumns: CodegenColumnConfig[]
  treeColumns?: CodegenColumnConfig[]
  subColumns?: CodegenColumnConfig[]
}

/**
 * 预览文件
 */
export interface CodegenPreviewFile {
  path: string
  name: string
  category: string
  content: string
}

/**
 * 预览结果
 */
export interface CodegenPreviewResult {
  zipFileName: string
  files: CodegenPreviewFile[]
}

export function listCodegenSchemas(datasourceId: number) {
  return http.post<CodegenMetaOption[]>('/sys/codegen/meta/databases', { datasourceId })
}

export function listCodegenTables(datasourceId: number, schemaName: string) {
  return http.post<CodegenMetaOption[]>('/sys/codegen/meta/tables', { datasourceId, schemaName })
}

export function listCodegenColumns(datasourceId: number, schemaName: string, tableName: string) {
  return http.post<CodegenColumnConfig[]>('/sys/codegen/meta/columns', { datasourceId, schemaName, tableName })
}

export function previewCodegen(data: CodegenRequest) {
  return http.post<CodegenPreviewResult>('/sys/codegen/preview', data)
}

export function previewCodegenByConfigId(configId: number) {
  return http.post<CodegenPreviewResult>('/sys/codegen/preview', { configId })
}

export async function downloadCodegenZip(data: CodegenRequest) {
  const locale = getLocale()
  const tenantId = sessionStorage.getItem('tenantId')
  const response = await axios.post('/api/sys/codegen/download', data, {
    responseType: 'blob',
    withCredentials: true,
    headers: {
      'Content-Type': 'application/json',
      'Accept-Language': locale,
      'X-Lang': locale,
      ...(tenantId ? { 'X-Tenant-Id': tenantId } : {}),
    },
  })

  const disposition = response.headers['content-disposition'] || ''
  const matched = disposition.match(/filename\*?=(?:UTF-8''|")?([^";]+)/i)
  const fileName = matched ? decodeURIComponent(matched[1].replace(/"/g, '')) : 'codegen.zip'
  return {
    blob: response.data as Blob,
    fileName,
  }
}

export function downloadCodegenZipByConfigId(configId: number) {
  return downloadCodegenZip({ configId } as CodegenRequest)
}
