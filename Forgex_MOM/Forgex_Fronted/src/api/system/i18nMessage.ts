import http from '@/api/http'

export interface I18nMessageItem {
  id?: number
  module: string
  promptCode: string
  textI18nJson: string
  enabled: boolean
  version?: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface I18nMessagePageParams {
  pageNum: number
  pageSize: number
  module?: string
  promptCode?: string
  enabled?: boolean
}

export function pageI18nMessages(params: I18nMessagePageParams) {
  return http.post('/sys/i18n/message/page', params)
}

export function getI18nMessage(id: number) {
  return http.post<I18nMessageItem>('/sys/i18n/message/get', { id })
}

export function createI18nMessage(data: I18nMessageItem) {
  return http.post<number>('/sys/i18n/message/create', data)
}

export function updateI18nMessage(data: I18nMessageItem) {
  return http.post<boolean>('/sys/i18n/message/update', data)
}

export function deleteI18nMessage(id: number) {
  return http.post<boolean>('/sys/i18n/message/delete', { id })
}
