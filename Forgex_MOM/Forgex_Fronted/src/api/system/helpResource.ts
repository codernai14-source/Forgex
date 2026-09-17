import http, { httpSuccess, silentHttp } from '../http'

export type HelpDocType = 'MANUAL' | 'VIDEO'
export type HelpScopeType = 'GLOBAL' | 'MENU'
export type HelpSourceType = 'FILE' | 'EXTERNAL_URL'

export interface SysHelpResource {
  id?: number
  tenantId?: number
  title: string
  docType: HelpDocType
  scopeType: HelpScopeType
  sourceType: HelpSourceType
  menuId?: number | null
  menuPath?: string
  fileName?: string
  fileUrl?: string
  fileExt?: string
  fileSize?: number
  contentType?: string
  externalUrl?: string
  status?: number
  sortOrder?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface SysHelpResourcePageParam {
  pageNum: number
  pageSize: number
  title?: string
  docType?: HelpDocType
  scopeType?: HelpScopeType
  sourceType?: HelpSourceType
  menuPath?: string
  status?: number
}

export interface SysHelpPageResolve {
  resolvedScope: HelpScopeType
  menuPath: string
  items: SysHelpResource[]
}

export interface SysHelpUploadResult {
  fileUrl: string
  fileName: string
  fileExt: string
  fileSize: number
  contentType?: string
}

export interface SysHelpContact {
  phone?: string
  email?: string
  wechatQrUrl?: string
  address?: string
  workTime?: string
  remark?: string
}

export const helpResourceApi = {
  page(params: SysHelpResourcePageParam) {
    return http.post<{ records: SysHelpResource[]; total: number }>('/sys/help-resource/page', params)
  },
  create(data: SysHelpResource) {
    return httpSuccess.post<number>('/sys/help-resource/create', data)
  },
  /**
   * 批量上传时创建元数据，避免每条都弹成功提示。
   *
   * @param data 资源元数据
   * @returns 新建 ID
   */
  createQuiet(data: SysHelpResource) {
    return http.post<number>('/sys/help-resource/create', data)
  },
  update(data: SysHelpResource) {
    return httpSuccess.post('/sys/help-resource/update', data)
  },
  delete(id: number) {
    return httpSuccess.post('/sys/help-resource/delete', { id })
  },
  batchDelete(ids: number[]) {
    return httpSuccess.post('/sys/help-resource/batch-delete', { ids })
  },
  changeStatus(id: number, status: number) {
    return httpSuccess.post('/sys/help-resource/change-status', { id, status })
  },
  upload(file: File, docType: string) {
    const fd = new FormData()
    fd.append('file', file)
    fd.append('docType', docType)
    return http.post<SysHelpUploadResult>('/sys/help-resource/upload', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  listForPage(docType: HelpDocType, menuPath: string) {
    return http.post<SysHelpPageResolve>('/sys/help-resource/list-for-page', { docType, menuPath })
  },
  getContact(silent = true) {
    const client = silent ? silentHttp : http
    return client.post<SysHelpContact>('/sys/help-contact/get', {})
  },
  saveContact(data: SysHelpContact) {
    return httpSuccess.post('/sys/help-contact/save', data)
  },
  uploadQr(file: File) {
    const fd = new FormData()
    fd.append('file', file)
    return http.post<SysHelpUploadResult>('/sys/help-contact/upload-qr', fd, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}
