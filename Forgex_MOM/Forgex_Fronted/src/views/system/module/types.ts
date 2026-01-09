/**
 * 模块管理模块 - 类型定义
 */

/**
 * 模块信息
 */
export interface Module {
  id?: string
  code: string
  name: string
  icon?: string
  orderNum: number
  visible: number
  status: number
  tenantId?: string
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
}

/**
 * 模块查询参数
 */
export interface ModuleQuery {
  code?: string
  name?: string
  status?: number
  pageNum: number
  pageSize: number
}
