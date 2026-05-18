import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobLog, JobPageParam, PageResult } from './types'

export function getJobLogPage(data: JobPageParam) {
  return http.post<PageResult<JobLog>>(`${JOB_API_BASE}/log/page`, data)
}

export function getJobLogDetail(id: number) {
  return http.get<JobLog>(`${JOB_API_BASE}/log/detail/${id}`)
}
