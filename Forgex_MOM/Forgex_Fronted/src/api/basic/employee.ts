import http from '@/api/http'

export interface Employee {
  id?: number
  employeeNo?: string
  employeeName?: string
  phone?: string
  email?: string
  gender?: number
  avatar?: string
  entryDate?: string
  departmentId?: number
  departmentName?: string
  positionId?: number
  positionName?: string
  status?: boolean
  remark?: string
  userId?: number
  createTime?: string
  updateTime?: string
}

export interface EmployeePageParam {
  pageNum: number
  pageSize: number
  employeeNo?: string
  employeeName?: string
  phone?: string
  departmentId?: number
  positionId?: number
  status?: boolean
}

export interface EmployeeSyncUserResult {
  totalCount: number
  createdCount: number
  updatedCount: number
  failedEmployeeNos: string[]
}

export interface EmployeeThirdPartyInvoke {
  apiCode?: string
  tenantId?: number
  payload?: Record<string, any>
}

export interface EmployeeThirdPartySyncResult {
  totalCount: number
  createdCount: number
  updatedCount: number
  failedCount: number
  failedEmployeeNos: string[]
}

export const employeeApi = {
  page(params: EmployeePageParam) {
    return http.post('/basic/employee/page', params)
  },
  list(params?: Partial<EmployeePageParam>) {
    return http.post<Employee[]>('/basic/employee/list', params || {})
  },
  detail(id: number) {
    return http.post<Employee>('/basic/employee/detail', { id })
  },
  create(data: Employee) {
    return http.post<number>('/basic/employee/create', data)
  },
  update(data: Employee) {
    return http.post<boolean>('/basic/employee/update', data)
  },
  delete(id: number) {
    return http.post<boolean>('/basic/employee/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/employee/batchDelete', { ids })
  },
  syncUser(id: number) {
    return http.post<EmployeeSyncUserResult>('/basic/employee/sync-user', { id })
  },
  batchSyncUser(ids: number[]) {
    return http.post<EmployeeSyncUserResult>('/basic/employee/batch-sync-user', { ids })
  },
  syncThirdParty(request?: EmployeeThirdPartyInvoke) {
    return http.post<EmployeeThirdPartySyncResult>('/basic/employee/sync-third-party', request || { payload: {} })
  },
  pullFromThirdParty(request?: EmployeeThirdPartyInvoke) {
    return http.post<EmployeeThirdPartySyncResult>('/basic/employee/pull-from-third-party', request || { payload: {} })
  },
}
