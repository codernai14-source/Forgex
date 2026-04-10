/**
 * 报表模块 TypeScript 类型定义
 * 定义报表管理、数据源管理相关的类型接口
 * @author Forgex Team
 * @version 1.0
 * @since 2026-04-09
 */

/**
 * 报表引擎类型枚举
 */
export type ReportEngineType = 'UREPORT' | 'JIMU'

/**
 * 报表模板状态枚举
 */
export type ReportStatus = 0 | 1

/**
 * 报表模板类型定义
 * 对应数据库表：fx_report_template
 * @see ReportDatasource - 关联的数据源
 * @see ReportCategory - 关联的分类
 */
export interface ReportTemplate {
  /**
   * 主键 ID（雪花算法生成）
   */
  id: number

  /**
   * 报表名称
   */
  name: string

  /**
   * 报表编码（唯一标识）
   */
  code: string

  /**
   * 报表引擎类型
   * @see ReportEngineType#UREPORT - UReport2 引擎
   * @see ReportEngineType#JIMU - JimuReport 引擎
   */
  engineType: ReportEngineType

  /**
   * 分类 ID
   */
  categoryId?: number

  /**
   * 分类名称（只读，用于展示）
   */
  categoryName?: string

  /**
   * 数据源 ID
   */
  datasourceId?: number

  /**
   * 数据源名称（只读，用于展示）
   */
  datasourceName?: string

  /**
   * 报表内容（JSON 格式，存储报表设计器配置）
   */
  content?: string

  /**
   * 报表状态：0-禁用，1-启用
   * @see ReportStatus#DISABLED - 禁用
   * @see ReportStatus#ENABLED - 启用
   */
  status: ReportStatus

  /**
   * 备注
   */
  remark?: string

  /**
   * 创建时间
   */
  createTime?: string

  /**
   * 更新时间
   */
  updateTime?: string
}

/**
 * 报表分类类型定义
 * 对应数据库表：fx_report_category
 */
export interface ReportCategory {
  /**
   * 主键 ID
   */
  id: number

  /**
   * 分类名称
   */
  name: string

  /**
   * 分类编码
   */
  code?: string

  /**
   * 父分类 ID（0 表示顶级分类）
   */
  parentId?: number

  /**
   * 排序号
   */
  orderNum?: number

  /**
   * 备注
   */
  remark?: string

  /**
   * 创建时间
   */
  createTime?: string

  /**
   * 更新时间
   */
  updateTime?: string

  /**
   * 子分类（树形结构使用）
   */
  children?: ReportCategory[]
}

/**
 * 报表数据源类型定义
 * 对应数据库表：fx_report_datasource
 */
export interface ReportDatasource {
  /**
   * 主键 ID
   */
  id: number

  /**
   * 数据源名称
   */
  name: string

  /**
   * 数据源编码（唯一标识）
   */
  code: string

  /**
   * 数据源类型：mysql, oracle, postgresql, sqlserver 等
   */
  type: string

  /**
   * 数据库 URL
   */
  url: string

  /**
   * 数据库用户名
   */
  username: string

  /**
   * 数据库密码（加密存储）
   */
  password?: string

  /**
   * 数据库驱动类名
   */
  driverClass?: string

  /**
   * 连接池配置（JSON 格式）
   */
  poolConfig?: string

  /**
   * 状态：0-禁用，1-启用
   */
  status: ReportStatus

  /**
   * 备注
   */
  remark?: string

  /**
   * 创建时间
   */
  createTime?: string

  /**
   * 更新时间
   */
  updateTime?: string
}

/**
 * 报表模板查询参数
 */
export interface ReportTemplateParam {
  /**
   * 页码（从 1 开始）
   */
  pageNum: number

  /**
   * 每页大小
   */
  pageSize: number

  /**
   * 报表名称（模糊查询）
   */
  name?: string

  /**
   * 报表编码（模糊查询）
   */
  code?: string

  /**
   * 引擎类型
   */
  engineType?: ReportEngineType

  /**
   * 分类 ID
   */
  categoryId?: number

  /**
   * 状态
   */
  status?: ReportStatus
}

/**
 * 报表分类查询参数
 */
export interface ReportCategoryParam {
  /**
   * 页码（从 1 开始）
   */
  pageNum: number

  /**
   * 每页大小
   */
  pageSize: number

  /**
   * 分类名称（模糊查询）
   */
  name?: string

  /**
   * 分类编码（模糊查询）
   */
  code?: string

  /**
   * 父分类 ID
   */
  parentId?: number
}

/**
 * 报表数据源查询参数
 */
export interface ReportDatasourceParam {
  /**
   * 页码（从 1 开始）
   */
  pageNum: number

  /**
   * 每页大小
   */
  pageSize: number

  /**
   * 数据源名称（模糊查询）
   */
  name?: string

  /**
   * 数据源编码（模糊查询）
   */
  code?: string

  /**
   * 数据源类型
   */
  type?: string

  /**
   * 状态
   */
  status?: ReportStatus
}

/**
 * 分页结果类型
 * @template T - 数据类型
 */
export interface PageResult<T> {
  /**
   * 数据列表
   */
  records: T[]

  /**
   * 总记录数
   */
  total: number

  /**
   * 当前页码
   */
  pageNum: number

  /**
   * 每页大小
   */
  pageSize: number
}

/**
 * 报表预览参数
 */
export interface ReportPreviewParam {
  /**
   * 报表编码
   */
  code: string

  /**
   * 引擎类型
   */
  engineType: ReportEngineType

  /**
   * 报表参数（JSON 格式）
   */
  params?: Record<string, any>
}

/**
 * 报表保存 DTO
 */
export interface ReportSaveDTO {
  /**
   * 主键 ID（新增时不传）
   */
  id?: number

  /**
   * 报表名称
   */
  name: string

  /**
   * 报表编码
   */
  code: string

  /**
   * 引擎类型
   */
  engineType: ReportEngineType

  /**
   * 分类 ID
   */
  categoryId?: number

  /**
   * 数据源 ID
   */
  datasourceId?: number

  /**
   * 报表内容
   */
  content?: string

  /**
   * 状态
   */
  status: ReportStatus

  /**
   * 备注
   */
  remark?: string
}

/**
 * 数据源保存 DTO
 */
export interface DatasourceSaveDTO {
  /**
   * 主键 ID（新增时不传）
   */
  id?: number

  /**
   * 数据源名称
   */
  name: string

  /**
   * 数据源编码
   */
  code: string

  /**
   * 数据源类型
   */
  type: string

  /**
   * 数据库 URL
   */
  url: string

  /**
   * 数据库用户名
   */
  username: string

  /**
   * 数据库密码
   */
  password?: string

  /**
   * 数据库驱动类名
   */
  driverClass?: string

  /**
   * 连接池配置
   */
  poolConfig?: string

  /**
   * 状态
   */
  status: ReportStatus

  /**
   * 备注
   */
  remark?: string
}
