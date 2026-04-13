import http, { silentHttp } from './http'

/**
 * 濞戝牊浼呴弻銉嚄閸欏倹鏆?
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
 * 濞戝牊浼呯€圭偘缍?
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
 * 濞戝牊浼呭Ч鍥ㄢ偓璁充繆閹? */
export interface MessageSummary {
  unreadCount: number
  userName: string
  summary: string
}

/**
 * 閸掑棝銆夐弻銉嚄濞戝牊浼呭Ο鈩冩緲
 */
export function pageMessageTemplate(params: any) {
  return http.post('/sys/message-template/page', params)
}

/**
 * 閺嶈宓両D閺屻儴顕楀☉鍫熶紖濡剝婢樼拠锔藉剰
 */
export function getMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/get', { id, publicConfig }, { silentError: true } as any)
}

/**
 * 娣囨繂鐡ㄥ☉鍫熶紖濡剝婢橀敍鍫熸煀婢х偞鍨ㄦ穱顔芥暭閿? */
export function saveMessageTemplate(data: any) {
  return http.post('/sys/message-template/save', data)
}

/**
 * 閸掔娀娅庡☉鍫熶紖濡剝婢?
 */
export function deleteMessageTemplate(id: number, publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete', { id, publicConfig })
}

/**
 * 閹靛綊鍣洪崚鐘绘珟濞戝牊浼呭Ο鈩冩緲
 */
export function deleteBatchMessageTemplate(ids: number[], publicConfig: boolean = false) {
  return http.post('/sys/message-template/delete-batch', { ids, publicConfig })
}

/**
 * 濞寸姴楠搁崣鏇㈠礂闁秴甯崇紓鍐惧枟婵椽宕ｉ弽銊ラ柟顓у灡鑶╅柡?
 */
export function pullPublicMessageTemplate() {
  return http.post('/sys/message-template/pull-public')
}

/**
 * 閼惧嘲褰囬張顏囶嚢濞戝牊浼呴弫浼村櫤閿涘牓娼ゆ妯荒佸蹇ョ礆
 */
export function getUnreadMessageCount() {
  return silentHttp.post<number>('/sys/message/unread-count')
}

/**
 * 閼惧嘲褰囬張顏囶嚢濞戝牊浼呭Ч鍥ㄢ偓? */
export function getUnreadSummary() {
  return http.post<MessageSummary>('/sys/message/unread-summary')
}

/**
 * 閺屻儴顕楅張顏囶嚢濞戝牊浼呴崚妤勩€?
 */
export function getUnread消息(limit: number = 20) {
  return http.get<Message[]>('/sys/message/unread', { params: { limit } })
}

/**
 * 閸掑棝銆夐弻銉嚄濞戝牊浼呴崚妤勩€冮敍鍫濆悑鐎硅妫崨钘夋倳閿? */
export function pageMessage(params: any) {
  return http.post('/sys/message/page', params)
}

/**
 * 閸掑棝銆夐弻銉嚄濞戝牊浼呴崚妤勩€冮敍鍫熸煀閸涜棄鎮曢敍? */
export function page消息(params: MessagePageParam) {
  return pageMessage(params)
}

/**
 * 閺嶅洩顔囧☉鍫熶紖瀹歌尪顕伴敍鍫濆悑鐎硅妫崨钘夋倳閿? */
export function markMessageRead(id: number, config?: any) {
  return http.post('/sys/message/read', { id }, config)
}

/**
 * 閺嶅洩顔囧☉鍫熶紖瀹歌尪顕伴敍鍫熸煀閸涜棄鎮曢敍? */
export function markRead(id: number) {
  return markMessageRead(id)
}

/**
 * 閺嶅洩顔囬幍鈧張澶嬬Х閹垰鍑＄拠? */
export function markAllMessageRead(config?: any) {
  return http.post('/sys/message/read-all', {}, config)
}

/**
 * 閹靛綊鍣洪弽鍥唶瀹歌尪顕伴敍鍫濇倵缁旑垱娈忛弮鐘冲闁插繑甯撮崣锝忕礉閹稿宕熼弶鈥宠嫙閸欐垵顦╅悶鍡礆
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
 * 娴ｈ法鏁ゅΟ鈩冩緲閸欐垿鈧焦绉烽幁? */
export function sendByTemplate(data: {
  templateCode: string
  receiverUserIds?: number[]
  receiverUserId?: number
  dataMap?: Record<string, any>
  bizType?: string
}) {
  return http.post<number>('/sys/message/send-by-template', data)
}
