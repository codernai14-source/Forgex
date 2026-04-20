import http, { silentHttp } from './http'

/**
 * 站内消息相关 HTTP 封装。
 * <p>
 * 包含消息模板 CRUD、未读统计、分页查询、已读标记及按模板发送等接口。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 */

/**
 * 站内消息分页查询参数。
 * <p>
 * 用于 {@link pageMessages} 等分页接口的入参约束。
 * </p>
 */
export interface MessagePageParam {
  pageNum: number
  pageSize: number
  messageType?: string
  platform?: string
  status?: number
  title?: string
}

/**
 * 站内消息实体。
 * <p>
 * 与列表、未读列表等接口返回结构一致。
 * </p>
 */
export interface Message {
  id: number
  senderTenantId: number
  senderUserId: number
  receiverTenantId: number
  receiverUserId: number
  scope: string
  messageType: string
  category?: 'SYSTEM' | 'MESSAGE'
  platform: string
  senderName: string
  title: string
  content: string
  linkUrl: string
  bizType: string
  status: number
  createTime: string
  readTime: string | null
}

/**
 * 未读消息汇总信息。
 * <p>
 * 用于角标、摘要展示等场景。
 * </p>
 */
export interface MessageSummary {
  unreadCount: number
  userName: string
  summary: string
}

/**
 * 分页查询消息模板。
 *
 * @param params 查询条件与分页参数
 * @returns 分页结果请求 Promise
 */
export function pageMessageTemplate(params: any) {
  return http.post('/sys/message-template/page', params)
}

/**
 * 根据主键获取消息模板详情。
 *
 * @param id           模板主键
 * @param publicConfig 是否读取公共模板配置
 * @returns 模板详情请求 Promise（静默错误时不弹全局提示）
 */
export function getMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/get', { id, publicConfig }, { silentError: true } as any)
}

/**
 * 保存消息模板（新增或更新）。
 *
 * @param data 模板表单数据
 * @returns 保存结果请求 Promise
 */
export function saveMessageTemplate(data: any) {
  return http.post('/sys/message-template/save', data)
}

/**
 * 删除单条消息模板。
 *
 * @param id           模板主键
 * @param publicConfig 是否操作公共模板
 * @returns 删除结果请求 Promise
 */
export function deleteMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete', { id, publicConfig })
}

/**
 * 批量删除消息模板。
 *
 * @param ids          模板主键列表
 * @param publicConfig 是否操作公共模板
 * @returns 批量删除结果请求 Promise
 */
export function deleteBatchMessageTemplate(ids: number[], publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete-batch', { ids, publicConfig })
}

/**
 * 从公共配置拉取消息模板到当前租户。
 *
 * @returns 拉取结果请求 Promise
 */
export function pullPublicMessageTemplate() {
  return http.post('/sys/message-template/pull-public')
}

/**
 * 获取当前用户未读消息数量。
 * <p>
 * 使用 {@link silentHttp}，失败时不触发全局错误提示。
 * </p>
 *
 * @param category 消息分类：SYSTEM（系统）或 MESSAGE（站内），可选
 * @returns 未读数量
 */
export function getUnreadMessageCount(category?: 'SYSTEM' | 'MESSAGE') {
  return silentHttp.post<number>('/sys/message/unread-count', category ? { category } : {})
}

/**
 * 获取未读消息摘要（未读总数、用户名、摘要文案等）。
 *
 * @returns 摘要数据
 * @see MessageSummary
 */
export function getUnreadSummary() {
  return http.post<MessageSummary>('/sys/message/unread-summary')
}

/**
 * 查询当前用户未读消息列表。
 *
 * @param limit    最大返回条数，默认 20
 * @param category 消息分类过滤，可选
 * @returns 未读消息列表
 * @see Message
 */
export function getUnreadMessages(limit: number = 20, category?: 'SYSTEM' | 'MESSAGE') {
  return http.get<Message[]>('/sys/message/unread', { params: { limit, category } })
}

/**
 * 分页查询站内消息（通用参数，与后端约定一致即可）。
 *
 * @param params 查询与分页参数
 * @returns 分页数据请求 Promise
 */
export function pageMessage(params: any) {
  return http.post('/sys/message/page', params)
}

/**
 * 分页查询站内消息（带类型的查询参数）。
 *
 * @param params {@link MessagePageParam}
 * @returns 分页数据请求 Promise
 * @see pageMessage
 */
export function pageMessages(params: MessagePageParam) {
  return pageMessage(params)
}

/**
 * 将单条消息标记为已读。
 *
 * @param id     消息主键
 * @param config 可选的 axios 请求配置
 * @returns 操作结果请求 Promise
 */
export function markMessageRead(id: number, config?: any) {
  return http.post('/sys/message/read', { id }, config)
}

/**
 * 将单条消息标记为已读（{@link markMessageRead} 的便捷别名）。
 *
 * @param id 消息主键
 * @returns 操作结果请求 Promise
 * @see markMessageRead
 */
export function markRead(id: number) {
  return markMessageRead(id)
}

/**
 * 将当前用户全部消息标记为已读。
 *
 * @param config 可选的 axios 请求配置
 * @returns 操作结果请求 Promise
 */
export function markAllMessageRead(config?: any) {
  return http.post('/sys/message/read-all', {}, config)
}

/**
 * 批量标记消息为已读。
 * <p>
 * 对有效 ID 并行调用 {@link markMessageRead}；全部过滤掉时直接返回 false。
 * </p>
 *
 * @param ids    消息主键列表
 * @param config 可选的 axios 请求配置（逐条请求共用）
 * @returns 是否至少执行了一条已读请求
 */
export async function markBatchRead(ids: number[], config?: any) {
  const validIds = (ids || []).filter((id) => id !== null && id !== undefined)
  if (validIds.length === 0) {
    return false
  }
  await Promise.all(validIds.map((id) => markMessageRead(id, config)))
  return true
}

/**
 * 按模板编码向指定用户发送消息。
 *
 * @param data 模板编码、接收人、模板变量及业务类型等
 * @returns 发送结果（如发送条数）
 */
export function sendByTemplate(data: {
  templateCode: string
  receiverUserIds?: number[]
  receiverUserId?: number
  dataMap?: Record<string, any>
  bizType?: string
}) {
  return http.post<number>('/sys/message/send-by-template', data)
}
