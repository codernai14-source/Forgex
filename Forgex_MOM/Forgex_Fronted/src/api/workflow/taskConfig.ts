/**
 * 工作流 - 审批任务配置 API
 */
import http from '../http'

/**
 * 审批任务配置 DTO
 */
export interface WfTaskConfigDTO {
  /**
   * 主键
   */
  id: number

  /**
   * 审批任务名称
   */
  taskName: string

  /**
   * 任务名称多语言 JSON
   */
  taskNameI18nJson?: string

  /**
   * 审批任务编码
   */
  taskCode: string

  /**
   * 审批任务解释器 Bean 名称
   */
  interpreterBean?: string

  /**
   * 表单类型：1=自定义，2=低代码
   */
  formType: number

  /**
   * 表单类型标签
   */
  formTypeDesc?: string

  /**
   * 表单路径
   */
  formPath?: string

  /**
   * 表单内容
   */
  formContent?: string

  /**
   * 状态：0=禁用，1=启用
   */
  status: number

  /**
   * 状态标签
   */
  statusDesc?: string

  /**
   * 版本号
   */
  version: number

  /**
   * 备注
   */
  remark?: string

  /**
   * 租户 ID
   */
  tenantId: number

  /**
   * 创建时间
   */
  createTime: string

  /**
   * 创建人
   */
  createBy?: string

  /**
   * 更新时间
   */
  updateTime?: string

  /**
   * 更新人
   */
  updateBy?: string
}

/**
 * 审批任务配置保存参数
 */
export interface WfTaskConfigSaveParam {
  /**
   * 主键（修改时必填）
   */
  id?: number

  /**
   * 审批任务名称
   */
  taskName: string

  /**
   * 任务名称多语言 JSON
   */
  taskNameI18nJson?: string

  /**
   * 审批任务编码
   */
  taskCode: string

  /**
   * 审批任务解释器 Bean 名称
   */
  interpreterBean?: string

  /**
   * 表单类型：1=自定义，2=低代码
   */
  formType: number

  /**
   * 表单路径（静态表单路由）
   */
  formPath?: string

  /**
   * 表单内容（低代码表单 JSON）
   */
  formContent?: string

  /**
   * 状态：0=禁用，1=启用
   */
  status?: number

  /**
   * 备注
   */
  remark?: string
}

/**
 * 审批任务配置查询参数
 */
export interface WfTaskConfigQueryParam {
  /**
   * 页码
   */
  pageNum?: number

  /**
   * 每页条数
   */
  pageSize?: number

  /**
   * 任务名称（模糊查询）
   */
  taskName?: string

  /**
   * 任务编码（模糊查询）
   */
  taskCode?: string

  /**
   * 状态：0=禁用，1=启用
   */
  status?: number
}

/**
 * 分页查询审批任务配置列表
 * @param params 查询参数
 * @returns 分页结果
 */
export function getTaskConfigPage(params: WfTaskConfigQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfTaskConfigDTO[]; total: number }>('/wf/task/config/page', params)
}

/**
 * 查询审批任务配置列表
 * @param params 查询参数
 * @returns 配置列表
 */
export function listTaskConfig(params: WfTaskConfigQueryParam) {
  return http.post<WfTaskConfigDTO[]>('/wf/task/config/list', params)
}

/**
 * 获取审批任务配置详情
 * @param params 参数（id）
 * @returns 配置详情
 */
export function getTaskConfig(params: { id: number }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/get', params)
}

/**
 * 根据任务编码获取审批任务配置
 * @param params 参数（taskCode）
 * @returns 配置详情
 */
export function getTaskConfigByCode(params: { taskCode: string }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/getByCode', params)
}

/**
 * 新增审批任务配置
 * @param params 配置参数
 * @returns 配置 ID
 */
export function createTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<number>('/wf/task/config/create', params)
}

/**
 * 更新审批任务配置
 * @param params 配置参数
 * @returns 是否成功
 */
export function updateTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<boolean>('/wf/task/config/update', params)
}

/**
 * 删除审批任务配置
 * @param params 参数（id）
 * @returns 是否成功
 */
export function deleteTaskConfig(params: { id: number }) {
  return http.post<boolean>('/wf/task/config/delete', params)
}

/**
 * 启用/禁用审批任务配置
 * @param params 参数（id, status）
 * @returns 是否成功
 */
export function updateTaskConfigStatus(params: { id: number; status: number }) {
  return http.post<boolean>('/wf/task/config/updateStatus', params)
}
