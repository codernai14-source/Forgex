/**
 * 租户初始化任务 API
 * 提供租户初始化任务的查询和进度订阅功能
 * @author Forgex Team
 * @version 1.0.0
 */
import http from '../http'

/**
 * 租户初始化任务状态枚举
 */
export enum TenantInitTaskStatusEnum {
  /** 等待中 */
  PENDING = 'PENDING',
  /** 进行中 */
  RUNNING = 'RUNNING',
  /** 成功 */
  SUCCESS = 'SUCCESS',
  /** 失败 */
  FAILED = 'FAILED'
}

/**
 * 租户初始化任务状态标签映射
 */
export const TenantInitTaskStatusLabels: Record<TenantInitTaskStatusEnum, string> = {
  [TenantInitTaskStatusEnum.PENDING]: '等待中',
  [TenantInitTaskStatusEnum.RUNNING]: '进行中',
  [TenantInitTaskStatusEnum.SUCCESS]: '成功',
  [TenantInitTaskStatusEnum.FAILED]: '失败'
}

/**
 * 租户初始化任务状态颜色映射
 */
export const TenantInitTaskStatusColors: Record<TenantInitTaskStatusEnum, string> = {
  [TenantInitTaskStatusEnum.PENDING]: 'gray',
  [TenantInitTaskStatusEnum.RUNNING]: 'blue',
  [TenantInitTaskStatusEnum.SUCCESS]: 'green',
  [TenantInitTaskStatusEnum.FAILED]: 'red'
}

/**
 * 租户初始化任务 DTO
 */
export interface TenantInitTaskDTO {
  /** 任务 ID */
  id: number
  /** 租户 ID */
  tenantId: number
  /** 租户名称 */
  tenantName: string
  /** 租户类型 */
  tenantType: string
  /** 状态 */
  status: TenantInitTaskStatusEnum
  /** 进度百分比（0-100） */
  progress: number
  /** 当前步骤描述 */
  currentStep: string
  /** 错误信息 */
  errorMessage?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
  /** 创建时间 */
  createTime: string
  /** 更新时间 */
  updateTime: string
}

/**
 * SSE 进度推送数据结构
 */
export interface ProgressPushData {
  /** 任务 ID */
  taskId: string
  /** 进度百分比 */
  progress: number
  /** 当前步骤描述 */
  currentStep: string
  /** 时间戳 */
  timestamp: number
}

/**
 * 获取租户初始化任务详情
 * 
 * @param taskId 任务 ID
 * @returns 任务详情
 */
export function getTaskDetail(taskId: number) {
  return http.get<TenantInitTaskDTO>(`/sys/tenant/init/task/detail/${taskId}`)
}

/**
 * 根据租户 ID 获取任务详情
 * 
 * @param tenantId 租户 ID
 * @returns 任务详情
 */
export function getTaskByTenantId(tenantId: number) {
  return http.get<TenantInitTaskDTO>(`/sys/tenant/init/task/by-tenant/${tenantId}`)
}

/**
 * 创建 SSE 连接订阅进度
 * 
 * @param taskId 任务 ID
 * @returns EventSource 实例
 */
export function subscribeTaskProgress(taskId: number): EventSource {
  const eventSource = new EventSource(
    `${import.meta.env.VITE_API_BASE_URL || '/api'}/sys/tenant/init/task/progress/${taskId}`
  )
  return eventSource
}
