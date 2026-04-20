/**
 * 第三方系统类型定义
 */

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
 * 第三方系统详情
 */
export interface ThirdSystemDetail {
  id: number
  systemCode: string
  systemName: string
  ipAddress?: string
  contactInfo?: string
  remark?: string
  status: number
  createTime?: string
  updateTime?: string
}

/**
 * 授权信息
 */
export interface AuthorizationInfo {
  id?: number
  thirdSystemId: number
  authType: 'WHITELIST' | 'TOKEN'
  tokenValue?: string
  tokenExpireHours?: number
  whitelistIps?: string
  status?: number
  remark?: string
}
