import http from '../http'

/** 站内通知默认消息类型，与表字段 message_type 一致 */
export const SYS_MESSAGE_DEFAULT_TYPE = 'NOTICE' as const
/** 站内渠道默认值，与表字段 platform 一致（站内） */
export const SYS_MESSAGE_DEFAULT_PLATFORM = 'INTERNAL' as const

export interface SysMessageVO {
  id: number
  senderTenantId: number
  senderUserId: number
  receiverTenantId: number
  receiverUserId: number
  scope: string
  messageType?: string
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
  /** 消息类型，默认 NOTICE */
  messageType?: string
  /** 渠道，站内默认 INTERNAL */
  platform?: string
  title: string
  content?: string
  linkUrl?: string
  bizType?: string
}

/**
 * 发送站内消息；未传 {@link SysMessageSendDTO#messageType} / {@link SysMessageSendDTO#platform} 时补齐库表非空字段默认值。
 *
 * @param dto 发送参数
 */
export function sendMessage(dto: SysMessageSendDTO) {
  return http.post('/sys/message/send', {
    ...dto,
    messageType: dto.messageType ?? SYS_MESSAGE_DEFAULT_TYPE,
    platform: dto.platform ?? SYS_MESSAGE_DEFAULT_PLATFORM,
  })
}

export function listUnreadMessages(limit = 20) {
  return http.get('/sys/message/unread', { params: { limit } })
}

export function markMessageRead(id: number) {
  return http.post('/sys/message/read', { id })
}

