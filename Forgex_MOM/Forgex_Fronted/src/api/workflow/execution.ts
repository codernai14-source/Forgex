/**
 * 工作流 - 审批执行 API
 */
import http from '../http'

/**
 * 审批执行 DTO
 */
export interface WfExecutionDTO {
  /**
   * 主键
   */
  id: number

  /**
   * 审批任务配置表 ID
   */
  taskConfigId: number

  /**
   * 审批任务编码
   */
  taskCode: string

  /**
   * 审批任务名称
   */
  taskName: string

  /**
   * 发起人 ID
   */
  initiatorId: number

  /**
   * 发起人名称
   */
  initiatorName: string

  /**
   * 当前节点 ID
   */
  currentNodeId?: number

  /**
   * 当前节点名称
   */
  currentNodeName?: string

  /**
   * 表单内容（JSON）
   */
  formContent: string

  /**
   * 发起时间
   */
  startTime: string

  /**
   * 结束时间
   */
  endTime?: string

  /**
   * 状态
   * 0=未审批
   * 1=审批中
   * 2=审批完成
   * 3=驳回
   */
  status: number

  /**
   * 状态标签
   */
  statusDesc?: string

  /**
   * 租户 ID
   */
  tenantId: number

  /**
   * 创建时间
   */
  createTime: string

  /**
   * 更新时间
   */
  updateTime?: string
}

/**
 * 发起审批参数
 */
export interface WfExecutionStartParam {
  /**
   * 审批任务编码
   */
  taskCode: string

  /**
   * 表单内容（JSON）
   */
  formContent: string
}

/**
 * 审批处理参数
 */
export interface WfExecutionApproveParam {
  /**
   * 审批执行 ID
   */
  executionId: number

  /**
   * 审批状态
   * 1=同意
   * 2=不同意
   */
  approveStatus: number

  /**
   * 审批意见
   */
  comment?: string

  /**
   * 驳回类型
   * 1=驳回任务
   * 2=返回上一节点
   * （不同意时必填）
   */
  rejectType?: number
}

/**
 * 审批执行查询参数
 */
export interface WfExecutionQueryParam {
  /**
   * 页码
   */
  pageNum?: number

  /**
   * 每页条数
   */
  pageSize?: number

  /**
   * 任务编码（模糊查询）
   */
  taskCode?: string

  /**
   * 任务名称（模糊查询）
   */
  taskName?: string

  /**
   * 状态
   * 0=未审批
   * 1=审批中
   * 2=审批完成
   * 3=驳回
   */
  status?: number

  /**
   * 发起人 ID
   */
  initiatorId?: number

  /**
   * 当前审批人 ID
   */
  currentApproverId?: number

  /**
   * 我已处理：审批操作时间下限（含），格式 yyyy-MM-dd HH:mm:ss
   */
  approveTimeBegin?: string

  /**
   * 我已处理：审批操作时间上限（含）
   */
  approveTimeEnd?: string
}

/**
 * 审批工作台首页汇总
 */
export interface WfDashboardSummaryVO {
  /**
   * 待我处理
   */
  pending: WfExecutionDTO[]

  /**
   * 昨日我已处理
   */
  yesterdayProcessed: WfExecutionDTO[]

  /**
   * 抄送待阅
   */
  cc: WfExecutionDTO[]
}

/**
 * 发起审批
 * @param params 发起审批参数
 * @returns 执行 ID
 */
export function startExecution(params: WfExecutionStartParam) {
  return http.post<number>('/wf/execution/start', params)
}

/**
 * 审批同意
 * @param params 审批参数
 * @returns 是否成功
 */
export function approve(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/approve', params)
}

/**
 * 审批驳回
 * @param params 审批参数
 * @returns 是否成功
 */
export function reject(params: WfExecutionApproveParam) {
  return http.post<boolean>('/wf/execution/reject', params)
}

/**
 * 撤销审批
 * @param params 参数（executionId）
 * @returns 是否成功
 */
export function cancelExecution(params: { executionId: number }) {
  return http.post<boolean>('/wf/execution/cancel', params)
}

/**
 * 获取审批详情
 * @param params 参数（executionId）
 * @returns 审批详情
 */
export function getExecutionDetail(params: { executionId: number }) {
  return http.post<WfExecutionDTO>('/wf/execution/detail', params)
}

/**
 * 分页查询我发起的审批
 * @param params 查询参数
 * @returns 分页结果
 */
export function pageMyInitiated(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/initiated', params)
}

/**
 * 分页查询我待处理的审批
 * @param params 查询参数
 * @returns 分页结果
 */
export function pageMyPending(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/pending', params)
}

/**
 * 分页查询我已处理的审批
 * @param params 查询参数
 * @returns 分页结果
 */
export function pageMyProcessed(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/processed', params)
}

/**
 * 分页查询抄送给我的待阅任务
 * @param params 查询参数
 * @returns 分页结果
 */
export function pageMyCc(params: WfExecutionQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfExecutionDTO[]; total: number }>('/wf/execution/my/cc', params)
}

/**
 * 审批工作台首页汇总（待办、昨日已处理、抄送待阅）
 * @returns 汇总数据
 */
export function loadDashboardSummary() {
  return http.post<WfDashboardSummaryVO>('/wf/execution/dashboard/summary', {}, { silentError: true } as any)
}
