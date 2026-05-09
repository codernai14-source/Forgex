import http from '../http'

export enum TenantInitTaskStatusEnum {
  PENDING = 'PENDING',
  RUNNING = 'RUNNING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
}

export const TenantInitTaskStatusI18nKeys: Record<TenantInitTaskStatusEnum, string> = {
  [TenantInitTaskStatusEnum.PENDING]: 'system.tenant.initProgress.status.pending',
  [TenantInitTaskStatusEnum.RUNNING]: 'system.tenant.initProgress.status.running',
  [TenantInitTaskStatusEnum.SUCCESS]: 'system.tenant.initProgress.status.success',
  [TenantInitTaskStatusEnum.FAILED]: 'system.tenant.initProgress.status.failed',
}

export const TenantInitTaskStatusColors: Record<TenantInitTaskStatusEnum, string> = {
  [TenantInitTaskStatusEnum.PENDING]: 'gray',
  [TenantInitTaskStatusEnum.RUNNING]: 'blue',
  [TenantInitTaskStatusEnum.SUCCESS]: 'green',
  [TenantInitTaskStatusEnum.FAILED]: 'red',
}

export interface TenantInitTaskDTO {
  id: number
  tenantId: number
  tenantName: string
  tenantType: string
  status: TenantInitTaskStatusEnum
  progress: number
  currentStep: string
  errorMessage?: string
  startTime?: string
  endTime?: string
  createTime: string
  updateTime: string
}

export interface ProgressPushData {
  taskId: string
  progress: number
  currentStep: string
  timestamp: number
}

export function getTaskDetail(taskId: number) {
  return http.get<TenantInitTaskDTO>(`/sys/tenant/init/task/detail/${taskId}`)
}

export function getTaskByTenantId(tenantId: number) {
  return http.get<TenantInitTaskDTO>(`/sys/tenant/init/task/by-tenant/${tenantId}`)
}

export function subscribeTaskProgress(taskId: number): EventSource {
  return new EventSource(`${import.meta.env.VITE_API_BASE_URL || '/api'}/sys/tenant/init/task/progress/${taskId}`)
}
