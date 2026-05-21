import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobPageParam, JobWorkflow, JobWorkflowExecution, PageResult } from './types'

export function getJobWorkflowPage(data: JobPageParam) {
  return http.post<PageResult<JobWorkflow>>(`${JOB_API_BASE}/workflow/page`, data)
}

export function getJobWorkflowDetail(id: number) {
  return http.post<JobWorkflow>(`${JOB_API_BASE}/workflow/detail`, { id })
}

export function saveJobWorkflow(data: JobWorkflow) {
  return http.post<number>(`${JOB_API_BASE}/workflow/save`, data)
}

export function publishJobWorkflow(id: number) {
  return http.post<void>(`${JOB_API_BASE}/workflow/publish`, { id })
}

export function executeJobWorkflow(id: number) {
  return http.post<number>(`${JOB_API_BASE}/workflow/execute`, { id })
}

export function getJobWorkflowExecutionPage(data: JobPageParam) {
  return http.post<PageResult<JobWorkflowExecution>>(`${JOB_API_BASE}/workflow/execution-page`, data)
}
