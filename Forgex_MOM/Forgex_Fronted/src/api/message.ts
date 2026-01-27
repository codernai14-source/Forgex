import http from './http'

/**
 * 消息模板API
 */

// 分页查询消息模板
export function pageMessageTemplate(params: any) {
  return http.post('/sys/message-template/page', params)
}

// 根据ID查询消息模板详情
export function getMessageTemplate(id: number) {
  return http.post('/sys/message-template/get', id)
}

// 保存消息模板(新增或修改)
export function saveMessageTemplate(data: any) {
  return http.post('/sys/message-template/save', data)
}

// 删除消息模板
export function deleteMessageTemplate(id: number) {
  return http.post('/sys/message-template/delete', id)
}

// 批量删除消息模板
export function deleteBatchMessageTemplate(ids: number[]) {
  return http.post('/sys/message-template/delete-batch', ids)
}

// 获取未读消息数量
export function getUnreadMessageCount() {
  return http.post('/sys/message/unread-count')
}

// 分页查询消息列表
export function pageMessage(params: any) {
  return http.post('/sys/message/page', params)
}

// 标记消息已读
export function markMessageRead(id: number) {
  return http.post('/sys/message/read', { id })
}

// 标记所有消息已读
export function markAllMessageRead() {
  return http.post('/sys/message/read-all')
}



