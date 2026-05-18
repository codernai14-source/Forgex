import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobInstance, JobPageParam, PageResult } from './types'

export function getJobInstancePage(data: JobPageParam) {
  return http.post<PageResult<JobInstance>>(`${JOB_API_BASE}/instance/page`, data)
}

export function changeJobInstanceMaintenance(id: number, maintenance: number) {
  return http.post<void>(`${JOB_API_BASE}/instance/change-maintenance`, { id, maintenance })
}
