/**
 * 模块管理模块 - 类型定义
 */

/**
 * 模块信息
 */
export interface Module {
  id?: string
  moduleName: string
  moduleCode: string
  icon?: string
  orderNum: number
  status: number
  remark?: string
  createTime?: string
  updateTime?: string
}

/**
 * 模块查询参数
 */
export interface ModuleQuery {
  moduleName?: string
  moduleCode?: string
  status?: number
  pageNum: number
  pageSize: number
}
