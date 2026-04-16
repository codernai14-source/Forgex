/**
 * 工作流执行与审批相关 API
 */
import http from '../http'

export interface WfApprovalInstanceDTO {
  id: number
  executionId: number
  executionDetailId: number
  nodeId: number
  instanceNo: string
  approverId: number
  approverName?: string
  approverSourceType?: number
  sourceRuleId?: number
  status: number
  actionType?: number
  comment?: string
  approveTime?: string
  deadlineTime?: string
  activated?: boolean
  delegateFromUserId?: number
  transferFromUserId?: number
}

export interface WfApprovalActionLogDTO {
  id: number
  executionId: number
  executionDetailId?: number
  nodeId?: number
  approvalInstanceId?: number
  actionType: number
  operatorId?: number
  operatorName?: string
  targetUserId?: number
  targetUserName?: string
  actionComment?: string
  actionSnapshot?: string
  createTime?: string
}

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
  currentApprovalInstances?: WfApprovalInstanceDTO[]
  approvalActionLogs?: WfApprovalActionLogDTO[]
  activeInstanceCount?: number
  timeoutFlag?: boolean
  delegated?: boolean
  transferred?: boolean
  latestActionSummary?: string
}

export interface WfExecutionStartParam {
  taskCode: string
  formContent: string
  selectedApprovers?: number[]
}

export interface WfExecutionApproveParam {
  executionId: number
  approveStatus: number
  comment?: string
  rejectType?: number
  approvalInstanceId?: number
  actionType?: number
  targetApproverId?: number
}

export interface WfExecutionTransferParam {
  executionId: number
  approvalInstanceId: number
  targetApproverId: number
  comment?: string
}

export interface WfExecutionAddSignParam {
  executionId: number
  approvalInstanceId: number
  targetApproverId: number
  comment?: string
}

export interface WfExecutionDelegateSaveParam {
  delegatorUserId: number
  delegateUserId: number
  comment?: string
}

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

export interface WfExecutionBatchApproveParam {
  executionIds: number[]
  approveStatus: number
  comment?: string
}

export interface WfExecutionBatchTransferParam {
  executionIds: number[]
  targetApproverId: number
  comment?: string
}

export interface WfExecutionRemindParam {
  executionIds: number[]
  comment?: string
}

export interface WfExecutionCompensateParam {
  executionId?: number
  nodeId?: number
  approvalInstanceId?: number
  timeBegin?: string
  timeEnd?: string
}

export interface WfDashboardSummaryVO {
  pending: WfExecutionDTO[]
  yesterdayProcessed: WfExecutionDTO[]
  cc: WfExecutionDTO[]
}

export interface WfDashboardWeeklyResultDTO {
  date: string
  approvedCount: number
  rejectedCount: number
}

export interface WfDashboardUserShareDTO {
  initiatorId: number
  initiatorName: string
  count: number
}

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

export function transfer(params: WfExecutionTransferParam) {
  return http.post<boolean>('/wf/execution/transfer', params)
}

export function addSign(params: WfExecutionAddSignParam) {
  return http.post<boolean>('/wf/execution/addSign', params)
}

export function saveDelegate(params: WfExecutionDelegateSaveParam) {
  return http.post<boolean>('/wf/execution/delegate/save', params)
}

export function cancelDelegate(params: { delegatorUserId: number }) {
  return http.post<boolean>('/wf/execution/delegate/cancel', params)
}

export function cancelExecution(params: { executionId: number }) {
  return http.post<boolean>('/wf/execution/cancel', params)
}

export function getExecutionDetail(params: { executionId: number }) {
  return http.post<WfExecutionDTO>('/wf/execution/detail', params)
}

export function listApprovalInstances(params: { executionId: number }) {
  return http.post<WfApprovalInstanceDTO[]>('/wf/execution/instances', params)
}

export function listApprovalActionLogs(params: { executionId: number }) {
  return http.post<WfApprovalActionLogDTO[]>('/wf/execution/actions', params)
}

export function batchApprove(params: WfExecutionBatchApproveParam) {
  return http.post<boolean>('/wf/execution/batch/approve', params)
}

export function batchTransfer(params: WfExecutionBatchTransferParam) {
  return http.post<boolean>('/wf/execution/batch/transfer', params)
}

export function batchRemind(params: WfExecutionRemindParam) {
  return http.post<boolean>('/wf/execution/batch/remind', params)
}

export function compensateExecution(params: WfExecutionCompensateParam) {
  return http.post<boolean>('/wf/execution/compensate', params)
}

export function retryTimeoutJobs(params: WfExecutionCompensateParam) {
  return http.post<boolean>('/wf/execution/timeout/retry', params)
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

export function pageCompensationCenter(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/compensation/page', params)
}

export function loadDashboardSummary() {
  return http.post<WfDashboardSummaryVO>('/wf/execution/dashboard/summary', {}, { silentError: true } as any)
}

export function loadDashboardAnalytics() {
  return http.post<WfDashboardAnalyticsVO>('/wf/execution/dashboard/analytics', {}, { silentError: true } as any)
}
