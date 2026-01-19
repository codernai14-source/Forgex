import http from '../http'

export interface SysMessageVO {
  id: number
  senderTenantId: number
  senderUserId: number
  receiverTenantId: number
  receiverUserId: number
  scope: string
  title: string
  content?: string
  linkUrl?: string
  bizType?: string
  status: number
  createTime?: string
  readTime?: string
}

export interface SysMessageSendDTO {
  receiverTenantId?: number
  receiverUserId: number
  scope?: string
  title: string
  content?: string
  linkUrl?: string
  bizType?: string
}

export function sendMessage(dto: SysMessageSendDTO) {
  return http.post('/sys/message/send', dto)
}

export function listUnreadMessages(limit = 20) {
  return http.get('/sys/message/unread', { params: { limit } })
}

export function markMessageRead(id: number) {
  return http.post('/sys/message/read', { id })
}

