/**
 * 工作流执行与审批相关 API
 */
import http from '../http'

/**
 * 审批执行记录
 */
export interface WfExecutionDTO {
  id: number
  taskConfigId: number
  taskCode: string
  taskName: string
  initiatorId: number
  initiatorName: string
  currentNodeId?: number
  currentNodeName?: string
  formContent: string
  startTime: string
  endTime?: string
  status: number
  statusDesc?: string
  tenantId: number
  createTime: string
  updateTime?: string
}

/**
 * 发起审批参数
 */
export interface WfExecutionStartParam {
  taskCode: string
  formContent: string
}

/**
 * 审批处理参数
 */
export interface WfExecutionApproveParam {
  executionId: number
  approveStatus: number
  comment?: string
  rejectType?: number
}

/**
 * 审批查询参数
 */
export interface WfExecutionQueryParam {
  pageNum?: number
  pageSize?: number
  taskCode?: string
  taskName?: string
  status?: number
  initiatorId?: number
  currentApproverId?: number
  approveTimeBegin?: string
  approveTimeEnd?: string
}

/**
 * 审批工作台摘要
 */
export interface WfDashboardSummaryVO {
  pending: WfExecutionDTO[]
  yesterdayProcessed: WfExecutionDTO[]
  cc: WfExecutionDTO[]
}

/**
 * 近 7 日审批结果
 */
export interface WfDashboardWeeklyResultDTO {
  date: string
  approvedCount: number
  rejectedCount: number
}

/**
 * 发起人审批占比
 */
export interface WfDashboardUserShareDTO {
  initiatorId: number
  initiatorName: string
  count: number
}

/**
 * 审批工作台统计分析
 */
export interface WfDashboardAnalyticsVO {
  weeklyResults: WfDashboardWeeklyResultDTO[]
  userShares: WfDashboardUserShareDTO[]
}

export function startExecution(params: WfExecutionStartParam) {
  return http.post<number>('/wf/execution/start', params)
}

export function approve(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/approve', params)
}

export function reject(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/reject', params)
}

export function cancelExecution(params: { executionId: number }) {
  return http.post<boolean>('/wf/execution/cancel', params)
}

export function getExecutionDetail(params: { executionId: number }) {
  return http.post<WfExecutionDTO>('/wf/execution/detail', params)
}

export function pageMyInitiated(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/initiated', params)
}

export function pageMyPending(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/pending', params)
}

export function pageMyProcessed(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/processed', params)
}

export function pageMyCc(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/cc', params)
}

export function loadDashboardSummary() {
  return http.post<WfDashboardSummaryVO>('/wf/execution/dashboard/summary', {}, { silentError: true } as any)
}

export function loadDashboardAnalytics() {
  return http.post<WfDashboardAnalyticsVO>('/wf/execution/dashboard/analytics', {}, { silentError: true } as any)
}
