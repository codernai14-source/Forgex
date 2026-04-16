import http from '../http'

/** 站内消息默认类型，未传 messageType 时使用。 */
export const SYS_MESSAGE_DEFAULT_TYPE = 'NOTICE' as const
/** 站内消息默认平台，未传 platform 时使用。 */
export const SYS_MESSAGE_DEFAULT_PLATFORM = 'INTERNAL' as const

export interface SysMessageVO {
  id: number
  senderTenantId: number
  senderUserId: number
  receiverTenantId: number
  receiverUserId: number
  scope: string
  messageType?: string
  category?: 'SYSTEM' | 'MESSAGE'
  platform?: string
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
  /** 消息类型，默认 NOTICE。 */
  messageType?: string
  /** 消息平台，默认 INTERNAL。 */
  platform?: string
  category?: 'SYSTEM' | 'MESSAGE'
  title: string
  content?: string
  linkUrl?: string
  bizType?: string
}

/**
 * 发送站内消息；未传 {@link SysMessageSendDTO#messageType} /
 * {@link SysMessageSendDTO#platform} 时自动补齐默认值。
 */
export function sendMessage(dto: SysMessageSendDTO) {
  return http.post('/sys/message/send', {
    ...dto,
    messageType: dto.messageType ?? SYS_MESSAGE_DEFAULT_TYPE,
    platform: dto.platform ?? SYS_MESSAGE_DEFAULT_PLATFORM,
    category: dto.category ?? 'MESSAGE',
  })
}

export function listUnreadMessages(limit = 20, category?: 'SYSTEM' | 'MESSAGE') {
  return http.get('/sys/message/unread', { params: { limit, category } })
}

export function getUnreadMessageCount(category?: 'SYSTEM' | 'MESSAGE') {
  return http.post<number>('/sys/message/unread-count', category ? { category } : {})
}

export function markMessageRead(id: number, config?: any) {
  return http.post('/sys/message/read', { id }, config)
}
