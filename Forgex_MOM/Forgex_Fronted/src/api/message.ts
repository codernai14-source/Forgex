import http, { silentHttp } from './http'

/**
 * 娑堟伅鏌ヨ鍙傛暟
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
 * 娑堟伅瀹炰綋
 */
export interface Message {
  id: number
  senderTenantId: number
  senderUserId: number
  receiverTenantId: number
  receiverUserId: number
  scope: string
  messageType: string
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
 * 娑堟伅姹囨€讳俊鎭? */
export interface MessageSummary {
  unreadCount: number
  userName: string
  summary: string
}

/**
 * 鍒嗛〉鏌ヨ娑堟伅妯℃澘
 */
export function pageMessageTemplate(params: any) {
  return http.post('/sys/message-template/page', params)
}

/**
 * 鏍规嵁ID鏌ヨ娑堟伅妯℃澘璇︽儏
 */
export function getMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/get', { id, publicConfig }, { silentError: true } as any)
}

/**
 * 淇濆瓨娑堟伅妯℃澘锛堟柊澧炴垨淇敼锛? */
export function saveMessageTemplate(data: any) {
  return http.post('/sys/message-template/save', data)
}

/**
 * 鍒犻櫎娑堟伅妯℃澘
 */
export function deleteMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete', { id, publicConfig })
}

/**
 * 鎵归噺鍒犻櫎娑堟伅妯℃澘
 */
export function deleteBatchMessageTemplate(ids: number[], publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete-batch', { ids, publicConfig })
}

/**
 * 娴犲骸鍙曢崗閬嶅帳缂冾喗濯洪崣鏍ㄧХ閹垱膩閺?
 */
export function pullPublicMessageTemplate() {
  return http.post('/sys/message-template/pull-public')
}

/**
 * 鑾峰彇鏈娑堟伅鏁伴噺锛堥潤榛樻ā寮忥級
 */
export function getUnreadMessageCount() {
  return silentHttp.post<number>('/sys/message/unread-count')
}

/**
 * 鑾峰彇鏈娑堟伅姹囨€? */
export function getUnreadSummary() {
  return http.post<MessageSummary>('/sys/message/unread-summary')
}

/**
 * 鏌ヨ鏈娑堟伅鍒楄〃
 */
export function getUnreadMessages(limit: number = 20) {
  return http.get<Message[]>('/sys/message/unread', { params: { limit } })
}

/**
 * 鍒嗛〉鏌ヨ娑堟伅鍒楄〃锛堝吋瀹规棫鍛藉悕锛? */
export function pageMessage(params: any) {
  return http.post('/sys/message/page', params)
}

/**
 * 鍒嗛〉鏌ヨ娑堟伅鍒楄〃锛堟柊鍛藉悕锛? */
export function pageMessages(params: MessagePageParam) {
  return pageMessage(params)
}

/**
 * 鏍囪娑堟伅宸茶锛堝吋瀹规棫鍛藉悕锛? */
export function markMessageRead(id: number) {
  return http.post('/sys/message/read', { id })
}

/**
 * 鏍囪娑堟伅宸茶锛堟柊鍛藉悕锛? */
export function markRead(id: number) {
  return markMessageRead(id)
}

/**
 * 鏍囪鎵€鏈夋秷鎭凡璇? */
export function markAllMessageRead() {
  return http.post('/sys/message/read-all')
}

/**
 * 鎵归噺鏍囪宸茶锛堝悗绔殏鏃犳壒閲忔帴鍙ｏ紝鎸夊崟鏉″苟鍙戝鐞嗭級
 */
export async function markBatchRead(ids: number[]) {
  const validIds = (ids || []).filter((id) => id !== null && id !== undefined)
  if (validIds.length === 0) {
    return false
  }
  await Promise.all(validIds.map((id) => markMessageRead(id)))
  return true
}

/**
 * 浣跨敤妯℃澘鍙戦€佹秷鎭? */
export function sendByTemplate(data: {
  templateCode: string
  receiverUserIds?: number[]
  receiverUserId?: number
  dataMap?: Record<string, any>
  bizType?: string
}) {
  return http.post<number>('/sys/message/send-by-template', data)
}
