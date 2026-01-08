/**
 * 职位管理 - 类型定义
 */

export interface Position {
  id?: number
  code: string
  name: string
  level?: number
  description?: string
  status: number
  orderNum?: number
  createTime?: string
  updateTime?: string
}

export interface PositionQueryParams {
  code?: string
  name?: string
  status?: number
  pageNum?: number
  pageSize?: number
}
