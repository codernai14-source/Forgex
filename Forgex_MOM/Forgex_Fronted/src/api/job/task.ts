import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobPageParam, JobTask, PageResult } from './types'

export function getJobTaskPage(data: JobPageParam) {
  return http.post<PageResult<JobTask>>(`${JOB_API_BASE}/task/page`, data)
}

export function getJobTaskDetail(id: number) {
  return http.post<JobTask>(`${JOB_API_BASE}/task/detail`, { id })
}

export function saveJobTask(data: JobTask) {
  return http.post<number>(`${JOB_API_BASE}/task/save`, data)
}

export function deleteJobTask(id: number) {
  return http.post<void>(`${JOB_API_BASE}/task/delete`, { id })
}

export function changeJobTaskStatus(id: number, status: number) {
  return http.post<void>(`${JOB_API_BASE}/task/change-status`, { id, status })
}

export function triggerJobTask(id: number, params?: string) {
  return http.post<number>(`${JOB_API_BASE}/task/trigger`, { id, params, requestId: `${id}-${Date.now()}` })
}

export function previewJobTriggerTimes(id: number) {
  return http.post<string[]>(`${JOB_API_BASE}/task/preview-trigger-times`, { id })
}
