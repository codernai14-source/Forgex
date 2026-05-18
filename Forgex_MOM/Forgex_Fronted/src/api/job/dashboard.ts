import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobDashboardSummary, JobLog, JobTrend } from './types'

export function getJobDashboardSummary() {
  return http.get<JobDashboardSummary>(`${JOB_API_BASE}/dashboard/summary`)
}

export function getJobDashboardTrend() {
  return http.get<JobTrend[]>(`${JOB_API_BASE}/dashboard/trend`)
}

export function getJobRecentFailures() {
  return http.get<JobLog[]>(`${JOB_API_BASE}/dashboard/recent-failures`)
}

export function getJobTopLogs() {
  return http.get<JobLog[]>(`${JOB_API_BASE}/dashboard/top`)
}
