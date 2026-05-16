import http, { silentHttp } from '../http'

export type NoticeScope = 'PUBLIC' | 'TENANT'
export type NoticeStatus = 'DRAFT' | 'PUBLISHED' | 'DISABLED'

export interface SysNoticeAttachment {
  id?: number
  noticeId?: number
  fileName?: string
  fileUrl?: string
  fileSize?: number
  fileType?: string
}

export interface SysNotice {
  id?: number
  tenantId?: number
  title: string
  scope: NoticeScope
  contentHtml: string
  summary?: string
  status?: NoticeStatus
  startTime?: string
  endTime?: string
  orderNum?: number
  forceRemind?: boolean
  attachments?: SysNoticeAttachment[]
  createTime?: string
  updateTime?: string
}

export interface SysNoticePageParam {
  pageNum: number
  pageSize: number
  title?: string
  scope?: NoticeScope
  status?: NoticeStatus
  startTime?: string[]
}

export const noticeApi = {
  page(params: SysNoticePageParam) {
    return http.post<{ records: SysNotice[]; total: number }>('/sys/notice/page', params)
  },
  detail(id: number) {
    return http.post<SysNotice>('/sys/notice/detail', { id })
  },
  save(data: SysNotice) {
    return http.post<number>('/sys/notice/save', data)
  },
  delete(id: number) {
    return http.post('/sys/notice/delete', { id })
  },
  publish(id: number) {
    return http.post('/sys/notice/publish', { id })
  },
  disable(id: number) {
    return http.post('/sys/notice/disable', { id })
  },
  popupList() {
    return silentHttp.post<SysNotice[]>('/sys/notice/popup/list', {})
  },
  ackPopup(noticeId: number) {
    return silentHttp.post('/sys/notice/popup/ack', { noticeId })
  },
}
