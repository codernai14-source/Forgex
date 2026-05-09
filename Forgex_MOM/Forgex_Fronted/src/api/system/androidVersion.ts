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
