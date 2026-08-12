/**
 * 工作流执行与审批相关 API
 */
import http from '../http'

export type WorkflowId = string | number

export interface WfApprovalInstanceDTO {
  id: WorkflowId
  executionId: WorkflowId
  executionDetailId: WorkflowId
  nodeId: WorkflowId
  instanceNo: string
  approverId: WorkflowId
  approverName?: string
  approverSourceType?: number
  sourceRuleId?: WorkflowId
  allowRecall?: boolean
  allowAddSign?: boolean
  allowTransfer?: boolean
  allowDelegate?: boolean
  status: number
  actionType?: number
  comment?: string
  approveTime?: string
  deadlineTime?: string
  activated?: boolean
  delegateFromUserId?: WorkflowId
  transferFromUserId?: WorkflowId
}

export interface WfApprovalActionLogDTO {
  id: WorkflowId
  executionId: WorkflowId
  executionDetailId?: WorkflowId
  nodeId?: WorkflowId
  approvalInstanceId?: WorkflowId
  actionType: number
  operatorId?: WorkflowId
  operatorName?: string
  targetUserId?: WorkflowId
  targetUserName?: string
  actionComment?: string
  actionSnapshot?: string
  createTime?: string
}

export interface WfExecutionDTO {
  id: WorkflowId
  taskConfigId: WorkflowId
  taskCode: string
  taskName: string
  initiatorId: WorkflowId
  initiatorName: string
  currentNodeId?: WorkflowId
  currentNodeName?: string
  formContent: string
  formType?: number
  formPath?: string
  taskFormContent?: string
  startTime: string
  endTime?: string
  status: number
  statusDesc?: string
  tenantId: WorkflowId
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
  selectedApprovers?: WorkflowId[]
}

export interface WfExecutionApproveParam {
  executionId: WorkflowId
  approveStatus: number
  comment?: string
  rejectType?: number
  approvalInstanceId?: WorkflowId
  actionType?: number
  targetApproverId?: WorkflowId
}

export interface WfExecutionTransferParam {
  executionId: WorkflowId
  approvalInstanceId: WorkflowId
  targetApproverId: WorkflowId
  comment?: string
}

export interface WfExecutionAddSignParam {
  executionId: WorkflowId
  approvalInstanceId: WorkflowId
  targetApproverId: WorkflowId
  comment?: string
}

export interface WfExecutionDelegateParam {
  executionId: WorkflowId
  approvalInstanceId: WorkflowId
  targetApproverId: WorkflowId
  comment?: string
}

export interface WfExecutionRecallParam {
  executionId: WorkflowId
  approvalInstanceId: WorkflowId
  comment?: string
}

export interface WfExecutionDelegateSaveParam {
  delegatorUserId: WorkflowId
  delegateUserId: WorkflowId
  comment?: string
}

export interface WfExecutionQueryParam {
  pageNum?: number
  pageSize?: number
  taskCode?: string
  taskName?: string
  status?: number
  initiatorId?: WorkflowId
  currentApproverId?: WorkflowId
  approveTimeBegin?: string
  approveTimeEnd?: string
}

export interface WfExecutionBatchApproveParam {
  executionIds: WorkflowId[]
  approveStatus: number
  comment?: string
}

export interface WfExecutionBatchTransferParam {
  executionIds: WorkflowId[]
  targetApproverId: WorkflowId
  comment?: string
}

export interface WfExecutionRemindParam {
  executionIds: WorkflowId[]
  comment?: string
}

export interface WfExecutionCompensateParam {
  executionId?: WorkflowId
  nodeId?: WorkflowId
  approvalInstanceId?: WorkflowId
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
  initiatorId: WorkflowId
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

export function delegate(params: WfExecutionDelegateParam) {
  return http.post<boolean>('/wf/execution/delegate', params)
}

export function recall(params: WfExecutionRecallParam) {
  return http.post<boolean>('/wf/execution/recall', params)
}

export function saveDelegate(params: WfExecutionDelegateSaveParam) {
  return http.post<boolean>('/wf/execution/delegate/save', params)
}

export function cancelDelegate(params: { delegatorUserId: WorkflowId }) {
  return http.post<boolean>('/wf/execution/delegate/cancel', params)
}

export function cancelExecution(params: { executionId: WorkflowId }) {
  return http.post<boolean>('/wf/execution/cancel', params)
}

export function getExecutionDetail(params: { executionId: WorkflowId }) {
  return http.post<WfExecutionDTO>('/wf/execution/detail', params)
}

export function listApprovalInstances(params: { executionId: WorkflowId }) {
  return http.post<WfApprovalInstanceDTO[]>('/wf/execution/instances', params)
}

export function listApprovalActionLogs(params: { executionId: WorkflowId }) {
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
