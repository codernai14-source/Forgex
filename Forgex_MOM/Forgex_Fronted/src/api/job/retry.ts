import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobPageParam, JobRetry, PageResult } from './types'

export function getJobRetryPage(data: JobPageParam) {
  return http.post<PageResult<JobRetry>>(`${JOB_API_BASE}/retry/page`, data)
}

export function handleJobRetry(id: number, action: number, remark?: string) {
  return http.post<void>(`${JOB_API_BASE}/retry/handle`, { id, action, remark })
}
