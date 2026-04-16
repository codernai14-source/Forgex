/**
 * API 调用记录查询参数
 * 
 * @description 用于 API 调用记录列表查询的参数对象
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
export interface ApiCallLogQuery {
  /** 当前页码 */
  current?: number
  /** 每页大小 */
  size?: number
  /** 接口名称（模糊查询） */
  interfaceName?: string
  /** 接口编码（模糊查询） */
  interfaceCode?: string
  /** 调用状态：0-失败，1-成功 */
  status?: number
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 调用方系统编码 */
  callerSystemCode?: string
  /** 调用方系统名称 */
  callerSystemName?: string
  /** 最小耗时（毫秒） */
  minCostTime?: number
  /** 最大耗时（毫秒） */
  maxCostTime?: number
}

/**
 * API 调用记录详情
 * 
 * @description API 调用记录的详细信息
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
export interface ApiCallLogDetail {
  /** 主键 ID */
  id: number
  /** 租户 ID */
  tenantId?: number
  /** 接口名称 */
  interfaceName: string
  /** 接口编码 */
  interfaceCode: string
  /** 请求 URL */
  requestUrl: string
  /** 请求方法：GET/POST/PUT/DELETE 等 */
  requestMethod: string
  /** 请求头（JSON 字符串） */
  requestHeaders?: string
  /** 请求参数（JSON 字符串） */
  requestParams?: string
  /** 请求体（JSON 字符串） */
  requestBody?: string
  /** 响应状态码 */
  responseStatus: number
  /** 响应头（JSON 字符串） */
  responseHeaders?: string
  /** 响应体（JSON 字符串） */
  responseBody?: string
  /** 调用状态：0-失败，1-成功 */
  status: number
  /** 调用方系统编码 */
  callerSystemCode?: string
  /** 调用方系统名称 */
  callerSystemName?: string
  /** 调用耗时（毫秒） */
  costTime: number
  /** 错误信息 */
  errorMessage?: string
  /** 错误堆栈 */
  errorStack?: string
  /** IP 地址 */
  ip?: string
  /** User-Agent */
  userAgent?: string
  /** 调用时间 */
  callTime: string
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

/**
 * API 调用记录列表项
 * 
 * @description API 调用记录列表展示的数据项
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
export interface ApiCallLogItem extends Omit<ApiCallLogDetail, 'requestHeaders' | 'requestBody' | 'responseHeaders' | 'responseBody' | 'errorStack'> {
}

/**
 * API 调用记录分页结果
 * 
 * @description API 调用记录分页查询结果
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
export interface ApiCallLogPageResult {
  /** 数据列表 */
  records: ApiCallLogItem[]
  /** 总记录数 */
  total: number
}
