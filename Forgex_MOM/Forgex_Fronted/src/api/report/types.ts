/**
 * 报表模块 TypeScript 类型定义
 * 包含报表模板、报表分类、数据源及分页结果等相关类型。
 */

/**
 * 报表引擎类型
 */
export type ReportEngineType = 'UREPORT' | 'JIMU'

/**
 * 报表状态
 */
export type ReportStatus = 0 | 1

/**
 * 报表模板
 */
export interface ReportTemplate {
  id: number
  name: string
  code: string
  engineType: ReportEngineType
  categoryId?: number
  categoryName?: string
  datasourceId?: number
  datasourceName?: string
  content?: string
  status: ReportStatus
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 报表分类
 */
export interface ReportCategory {
  id: number
  name: string
  code?: string
  parentId?: number
  orderNum?: number
  remark?: string
  createTime?: string
  updateTime?: string
  children?: ReportCategory[]
}

/**
 * 报表数据源
 */
export interface ReportDatasource {
  id: number
  name: string
  code: string
  type: string
  url: string
  username: string
  password?: string
  driverClass?: string
  poolConfig?: string
  status: ReportStatus
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 报表模板分页查询参数
 */
export interface ReportTemplateParam {
  pageNum: number
  pageSize: number
  name?: string
  code?: string
  engineType?: ReportEngineType
  categoryId?: number
  status?: ReportStatus
}

/**
 * 报表分类分页查询参数
 */
export interface ReportCategoryParam {
  pageNum: number
  pageSize: number
  name?: string
  code?: string
  parentId?: number
}

/**
 * 报表数据源分页查询参数
 */
export interface ReportDatasourceParam {
  pageNum: number
  pageSize: number
  name?: string
  code?: string
  type?: string
  status?: ReportStatus
}

/**
 * 通用分页结果
 */
export interface PageResult<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 报表预览参数
 */
export interface ReportPreviewParam {
  code: string
  engineType: ReportEngineType
  params?: Record<string, any>
}

/**
 * 报表保存参数
 */
export interface ReportSaveDTO {
  id?: number
  name: string
  code: string
  engineType: ReportEngineType
  categoryId?: number
  datasourceId?: number
  content?: string
  status: ReportStatus
  remark?: string
}

/**
 * 数据源保存参数
 */
export interface DatasourceSaveDTO {
  id?: number
  name: string
  code: string
  type: string
  url: string
  username: string
  password?: string
  driverClass?: string
  poolConfig?: string
  status: ReportStatus
  remark?: string
}
