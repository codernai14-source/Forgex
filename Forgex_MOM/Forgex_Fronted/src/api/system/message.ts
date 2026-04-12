import http from '../http'

/** 绔欏唴閫氱煡榛樿娑堟伅绫诲瀷锛屼笌琛ㄥ瓧娈?message_type 涓€鑷?*/
export const SYS_MESSAGE_DEFAULT_TYPE = 'NOTICE' as const
/** 绔欏唴娓犻亾榛樿鍊硷紝涓庤〃瀛楁 platform 涓€鑷达紙绔欏唴锛?*/
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
  /** 娑堟伅绫诲瀷锛岄粯璁?NOTICE */
  messageType?: string
  /** 娓犻亾锛岀珯鍐呴粯璁?INTERNAL */
  platform?: string
  title: string
  content?: string
  linkUrl?: string
  bizType?: string
}

/**
 * 鍙戦€佺珯鍐呮秷鎭紱鏈紶 {@link SysMessageSendDTO#messageType} / {@link SysMessageSendDTO#platform} 鏃惰ˉ榻愬簱琛ㄩ潪绌哄瓧娈甸粯璁ゅ€笺€?
 *
 * @param dto 鍙戦€佸弬鏁?
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

export function markMessageRead(id: number, config?: any) {
  return http.post('/sys/message/read', { id }, config)
}

