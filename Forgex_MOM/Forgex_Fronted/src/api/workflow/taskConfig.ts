/**
 * 工作流 - 审批任务配置 API
 * 
 * 提供审批任务配置的 CRUD、状态管理等功能
 * 
 * @author Forgex
 * @version 1.0.0
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
 * 
 * 执行步骤：
 * 1. 接收分页查询参数
 * 2. 调用后端分页接口
 * 3. 返回分页结果
 * 
 * @param params 分页查询参数
 * @param params.pageNum 页码，默认 1
 * @param params.pageSize 每页条数，默认 10
 * @param params.taskName 任务名称（可选，模糊查询）
 * @param params.taskCode 任务编码（可选，模糊查询）
 * @param params.status 状态（可选，0=禁用，1=启用）
 * @returns 分页结果，包含 records（配置列表）和 total（总数）
 * @throws 查询失败时抛出异常
 */
export function getTaskConfigPage(params: WfTaskConfigQueryParam & { pageNum: number; pageSize: number }) {
  return http.post<{ records: WfTaskConfigDTO[]; total: number }>('/wf/task/config/page', params)
}

/**
 * 查询审批任务配置列表
 * 
 * 执行步骤：
 * 1. 接收查询参数（可选）
 * 2. 调用后端列表接口
 * 3. 返回配置列表
 * 
 * @param params 查询参数（可选）
 * @returns 配置列表
 * @throws 查询失败时抛出异常
 */
export function listTaskConfig(params: WfTaskConfigQueryParam) {
  return http.post<WfTaskConfigDTO[]>('/wf/task/config/list', params)
}

/**
 * 获取审批任务配置详情
 * 
 * 执行步骤：
 * 1. 接收配置 ID
 * 2. 调用后端详情查询接口
 * 3. 返回配置完整信息
 * 
 * @param params 查询参数
 * @param params.id 配置 ID
 * @returns 配置详情对象
 * @throws 查询失败时抛出异常
 */
export function getTaskConfig(params: { id: number }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/get', params)
}

/**
 * 根据任务编码获取审批任务配置
 * 
 * 执行步骤：
 * 1. 接收任务编码
 * 2. 调用后端详情查询接口（按编码）
 * 3. 返回配置详情
 * 
 * @param params 查询参数
 * @param params.taskCode 任务编码
 * @returns 配置详情对象
 * @throws 查询失败时抛出异常
 */
export function getTaskConfigByCode(params: { taskCode: string }) {
  return http.post<WfTaskConfigDTO>('/wf/task/config/getByCode', params)
}

/**
 * 新增审批任务配置
 * 
 * 执行步骤：
 * 1. 接收配置数据
 * 2. 调用后端创建接口
 * 3. 返回创建的配置 ID
 * 
 * @param params 配置参数
 * @param params.taskName 任务名称
 * @param params.taskNameI18nJson 多语言 JSON（可选）
 * @param params.taskCode 任务编码
 * @param params.interpreterBean 解释器 Bean 名称（可选）
 * @param params.formType 表单类型（1=自定义，2=低代码）
 * @param params.formPath 表单路径（可选）
 * @param params.formContent 表单内容（可选）
 * @param params.status 状态（可选，默认 1）
 * @param params.remark 备注（可选）
 * @returns 配置 ID
 * @throws 创建失败时抛出异常
 */
export function createTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<number>('/wf/task/config/create', params)
}

/**
 * 更新审批任务配置
 * 
 * 执行步骤：
 * 1. 接收配置数据（必须包含 id）
 * 2. 调用后端更新接口
 * 3. 返回更新结果
 * 
 * @param params 配置参数
 * @param params.id 配置 ID（必填）
 * @param params.taskName 任务名称
 * @param params.taskNameI18nJson 多语言 JSON（可选）
 * @param params.taskCode 任务编码
 * @param params.interpreterBean 解释器 Bean 名称（可选）
 * @param params.formType 表单类型
 * @param params.formPath 表单路径（可选）
 * @param params.formContent 表单内容（可选）
 * @param params.status 状态
 * @param params.remark 备注（可选）
 * @returns 是否成功
 * @throws 更新失败时抛出异常
 */
export function updateTaskConfig(params: WfTaskConfigSaveParam) {
  return http.post<boolean>('/wf/task/config/update', params)
}

/**
 * 删除审批任务配置
 * 
 * 执行步骤：
 * 1. 接收配置 ID
 * 2. 调用后端删除接口
 * 3. 返回删除结果
 * 
 * @param params 删除参数
 * @param params.id 配置 ID
 * @returns 是否成功
 * @throws 删除失败时抛出异常
 */
export function deleteTaskConfig(params: { id: number }) {
  return http.post<boolean>('/wf/task/config/delete', params)
}

/**
 * 启用/禁用审批任务配置
 * 
 * 执行步骤：
 * 1. 接收配置 ID 和状态
 * 2. 调用后端状态更新接口
 * 3. 返回更新结果
 * 
 * @param params 更新参数
 * @param params.id 配置 ID
 * @param params.status 状态（0=禁用，1=启用）
 * @returns 是否成功
 * @throws 更新失败时抛出异常
 */
export function updateTaskConfigStatus(params: { id: number; status: number }) {
  return http.post<boolean>('/wf/task/config/updateStatus', params)
}
