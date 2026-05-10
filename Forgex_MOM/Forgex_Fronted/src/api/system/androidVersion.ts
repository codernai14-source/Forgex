import http from '../http'

export interface AndroidVersionItem {
  id?: number
  versionCode: number
  versionName: string
  changelog?: string
  fileName?: string
  fileUrl?: string
  fileSize?: number
  storageType?: string
  status?: number
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
}

export interface AndroidVersionQuery {
  versionName?: string
  status?: number
  pageNum?: number
  pageSize?: number
}

export interface AndroidVersionSaveParam {
  id?: number
  versionCode: number
  versionName: string
  changelog?: string
  status?: number
}

export interface AndroidVersionPageResult {
  records: AndroidVersionItem[]
  total: number
  current: number
  size: number
}

export interface AndroidUploadTask {
  uploadId: string
  fileName: string
  fileSize: number
  chunkSize: number
  totalChunks: number
  uploadedChunks: number[]
  missingChunks: number[]
  uploadedCount: number
  status: 'UPLOADING' | 'MERGING' | 'COMPLETED' | 'CANCELED' | 'FAILED'
  errorMessage?: string
  finalFileUrl?: string
  versionId?: number
  version?: AndroidVersionItem
}

export interface AndroidUploadInitParam {
  fileName: string
  fileSize: number
  chunkSize: number
  totalChunks: number
  fileHash?: string
}

export function getAndroidVersionPage(data: AndroidVersionQuery) {
  return http.post<AndroidVersionPageResult>('/sys/android-version/page', data)
}

export function updateAndroidVersion(data: AndroidVersionSaveParam) {
  return http.post<AndroidVersionItem>('/sys/android-version/update', data)
}

export function deleteAndroidVersion(id: number) {
  return http.post<void>('/sys/android-version/delete', { id })
}

export function getLatestAndroidVersion() {
  return http.post<AndroidVersionItem | null>('/sys/android-version/latest', {})
}

export function uploadAndroidVersion(file: File, data: AndroidVersionSaveParam) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('versionCode', String(data.versionCode))
  formData.append('versionName', data.versionName)
  formData.append('changelog', data.changelog || '')

  return http.post<AndroidVersionItem>('/sys/android-version/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

export function initAndroidVersionUpload(data: AndroidUploadInitParam) {
  return http.post<AndroidUploadTask>('/sys/android-version/upload/init', data, {
    showSuccessMessage: false,
  })
}

export function getAndroidVersionUploadStatus(uploadId: string) {
  return http.post<AndroidUploadTask>('/sys/android-version/upload/status', { uploadId }, {
    showSuccessMessage: false,
  })
}

export function uploadAndroidVersionChunk(
  uploadId: string,
  chunkIndex: number,
  chunk: Blob,
  onUploadProgress?: (progress: number) => void,
) {
  const formData = new FormData()
  formData.append('uploadId', uploadId)
  formData.append('chunkIndex', String(chunkIndex))
  formData.append('chunk', chunk)

  return http.post<AndroidUploadTask>('/sys/android-version/upload/chunk', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    timeout: 0,
    showSuccessMessage: false,
    onUploadProgress: event => {
      if (!event.total) return
      onUploadProgress?.(Math.round((event.loaded / event.total) * 100))
    },
  })
}

export function completeAndroidVersionUpload(uploadId: string, data: AndroidVersionSaveParam) {
  return http.post<AndroidVersionItem>('/sys/android-version/upload/complete', {
    uploadId,
    versionCode: data.versionCode,
    versionName: data.versionName,
    changelog: data.changelog || '',
    status: data.status ?? 1,
  })
}

export function cancelAndroidVersionUpload(uploadId: string) {
  return http.post<void>('/sys/android-version/upload/cancel', { uploadId }, {
    showSuccessMessage: false,
  })
}
