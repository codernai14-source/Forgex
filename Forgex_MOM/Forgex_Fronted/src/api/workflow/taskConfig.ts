import http from '../http'
import { postJsonWithLongs } from '@/utils/longJson'

type RequestConfig = Record<string, any>

export interface WfTaskConfigDTO {
  id: number
  taskName: string
  taskNameI18nJson?: string
  taskCode: string
  categoryCode?: string
  interpreterBean?: string
  formType: number
  formPath?: string
  formContent?: string
  requiresSelectedApprovers?: boolean
  status: number
  version: number
  configStage?: string
  remark?: string
  tenantId?: number
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
}

export interface WfTaskConfigSummaryDTO {
  id: number
  publishedId?: number
  draftId?: number
  taskName: string
  taskCode: string
  categoryCode?: string
  interpreterBean?: string
  formType: number
  formPath?: string
  status: number
  remark?: string
  publishedVersion?: number
  draftVersion?: number
  hasDraft: boolean
  displayStage?: string
  createTime?: string
  updateTime?: string
}

export interface WfTaskConfigSaveParam {
  id?: number
  taskName: string
  taskNameI18nJson?: string
  taskCode: string
  categoryCode?: string
  interpreterBean?: string
  formType: number
  formPath?: string
  formContent?: string
  status?: number
  remark?: string
}

export interface WfTaskConfigQueryParam {
  pageNum?: number
  pageSize?: number
  taskName?: string
  taskCode?: string
  status?: number
}

export interface WfTaskDraftEditorQueryParam {
  id?: number
  taskCode?: string
}

export interface WfTaskDraftEditorDTO {
  draftId: number
  publishedId?: number
  taskCode: string
  categoryCode?: string
  taskName: string
  taskNameI18nJson?: string
  interpreterBean?: string
  formType: number
  formPath?: string
  formContent?: string
  status: number
  remark?: string
  publishedVersion?: number
  draftVersion?: number
  hasPublished: boolean
  configStage?: string
}

export interface WfNodeApproverDTO {
  approverType: number
  approverIds: string[]
}

export interface WfTaskNodeRuleDTO {
  id?: number
  ruleName?: string
  ruleType?: number
  approveMode?: number
  approvalThreshold?: number
  sortOrder?: number
  timeoutHours?: number
  timeoutAction?: number
  allowInitiatorSelect?: boolean
  superiorLevel?: number
  allowAddSign?: boolean
  allowTransfer?: boolean
  allowDelegate?: boolean
  allowRecall?: boolean
  fallbackApproverIds?: string[]
  approvers: WfNodeApproverDTO[]
  extraConfig?: string
}

export interface WfBranchRuleDTO {
  fieldKey: string
  fieldLabel?: string
  operator: string
  value?: string
  nextNodeKey: string
}

export interface WfLowCodeFieldMetaDTO {
  key: string
  label: string
  component?: string
}

export interface WfTaskNodeEditorDTO {
  nodeKey: string
  nodeType: number
  nodeName?: string
  approveType?: number
  canvasX?: number
  canvasY?: number
  defaultBranchNodeKey?: string
  approvers: WfNodeApproverDTO[]
  ruleConfigs?: WfTaskNodeRuleDTO[]
  branchRules: WfBranchRuleDTO[]
}

export interface WfTaskEdgeDTO {
  id?: string
  sourceNodeKey: string
  targetNodeKey: string
}

export interface WfTaskGraphDTO {
  draftId: number
  taskCode: string
  nodes: WfTaskNodeEditorDTO[]
  edges: WfTaskEdgeDTO[]
  availableFormFields?: WfLowCodeFieldMetaDTO[]
}

export interface WfTaskGraphSaveParam {
  draftId: number
  nodes: WfTaskNodeEditorDTO[]
  edges: WfTaskEdgeDTO[]
}

export function getTaskConfigPage(params: WfTaskConfigQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfTaskConfigSummaryDTO[]; total: number }>('/wf/task/config/page', params)
}

export function listTaskConfig(params: WfTaskConfigQueryParam) {
  return http.post<WfTaskConfigDTO[]>('/wf/task/config/list', params)
}

export function getTaskConfig(params: { id: number }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/get', params)
}

export function getTaskConfigByCode(params: { taskCode: string }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/getByCode', params)
}

export function createTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<number>('/wf/task/config/create', params)
}

export function updateTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<boolean>('/wf/task/config/update', params)
}

export function deleteTaskConfig(params: { id: number }) {
  return http.post<boolean>('/wf/task/config/delete', params)
}

export function updateTaskConfigStatus(params: { id: number; status: number }) {
  return http.post<boolean>('/wf/task/config/updateStatus', params)
}

export function getOrCreateDraftEditor(params: WfTaskDraftEditorQueryParam, config?: RequestConfig) {
  return http.post<WfTaskDraftEditorDTO>('/wf/task/config/draft/editor', params, config)
}

export function saveDraftBaseInfo(params: WfTaskConfigSaveParam, config?: RequestConfig) {
  return http.post<WfTaskDraftEditorDTO>('/wf/task/config/draft/base/save', params, config)
}

export function getDraftGraph(params: WfTaskDraftEditorQueryParam, config?: RequestConfig) {
  if (config) {
    return http.post<WfTaskGraphDTO>('/wf/task/config/draft/graph/get', params, config)
  }
  return postJsonWithLongs<WfTaskGraphDTO>(
    '/wf/task/config/draft/graph/get',
    params,
    {
      scalarKeys: ['draftId', 'publishedId'],
      arrayKeys: ['approverIds', 'fallbackApproverIds'],
    },
  )
}

export function saveDraftGraph(params: WfTaskGraphSaveParam, config?: RequestConfig) {
  return http.post<boolean>('/wf/task/config/draft/graph/save', params, config)
}

export function publishDraft(params: WfTaskDraftEditorQueryParam, config?: RequestConfig) {
  return http.post<boolean>('/wf/task/config/draft/publish', params, config)
}
