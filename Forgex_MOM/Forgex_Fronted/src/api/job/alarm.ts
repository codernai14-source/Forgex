import http from '../http'
import { JOB_API_BASE } from './types'
import type { JobAlarmLog, JobAlarmRule, JobPageParam, PageResult } from './types'

export function getJobAlarmRulePage(data: JobPageParam) {
  return http.post<PageResult<JobAlarmRule>>(`${JOB_API_BASE}/alarm/page`, data)
}

export function getJobAlarmRuleDetail(id: number) {
  return http.post<JobAlarmRule>(`${JOB_API_BASE}/alarm/detail`, { id })
}

export function saveJobAlarmRule(data: JobAlarmRule) {
  return http.post<number>(`${JOB_API_BASE}/alarm/save`, data)
}

export function deleteJobAlarmRule(id: number) {
  return http.post<void>(`${JOB_API_BASE}/alarm/delete`, { id })
}

export function getJobAlarmLogPage(data: JobPageParam) {
  return http.post<PageResult<JobAlarmLog>>(`${JOB_API_BASE}/alarm-log/page`, data)
}
