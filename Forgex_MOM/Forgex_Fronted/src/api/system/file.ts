import http, { type FxRequestConfig } from '../http'

export interface UploadFileOptions extends FxRequestConfig {
  moduleCode?: string
  moduleName?: string
}

export interface SysFileRecordItem {
  id: number
  moduleCode: string
  moduleName: string
  originalName: string
  storedName: string
  fileType: string
  contentType: string
  fileSize: number
  relativePath: string
  accessUrl: string
  storageType: string
  createBy: string
  createTime: string
}

export interface SysFileRecordPageQuery {
  pageNum?: number
  pageSize?: number
  moduleCode?: string
  moduleName?: string
  originalName?: string
  fileType?: string
}

/**
 * 上传文件
 * @param file 文件对象
 * @param config 请求配置项
 */
export function uploadFile(file: File, options: UploadFileOptions = {}) {
  const { moduleCode, moduleName, ...requestConfig } = options
  const fd = new FormData()
  fd.append('file', file)
  if (moduleCode) {
    fd.append('moduleCode', moduleCode)
  }
  if (moduleName) {
    fd.append('moduleName', moduleName)
  }
  return http.post<string>('/sys/file/upload', fd, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    ...requestConfig,
  })
}

export function getFileRecordPage(query: SysFileRecordPageQuery) {
  return http.post<{ records: SysFileRecordItem[]; total: number }>('/sys/file/page', query)
}
