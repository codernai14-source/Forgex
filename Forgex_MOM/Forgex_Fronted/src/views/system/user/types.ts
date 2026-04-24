/**
 * 用户管理模块类型定义
 */

export interface User {
  id?: string
  account: string
  username: string
  email: string
  phone: string
  gender: 0 | 1 | 2
  genderText?: string
  entryDate: string
  departmentId: string
  departmentName?: string
  positionId: string
  positionName?: string
  employeeId?: string
  userSource?: number | string
  userSourceText?: string
  roleIds?: string[]
  roleNames?: string[]
  tenantId?: string
  status: boolean | number
  statusText?: string
  createTime?: string
  updateTime?: string
  createBy?: string
  updateBy?: string
  lastLoginTime?: string
  lastLoginIp?: string
  lastLoginRegion?: string
  tenantList?: UserTenant[]
  profile?: UserProfile
}

export interface UserTenant {
  userId: string
  tenantId: string
  tenantName?: string
  prefOrder: number
  isDefault: boolean
  lastUsed?: string
}

export interface UserProfile {
  id?: string
  userId: string
  politicalStatus?: string
  homeAddress?: string
  emergencyContact?: string
  emergencyPhone?: string
  referrer?: string
  education?: string
  birthPlace?: string
  intro?: string
  workHistory?: WorkHistory[]
}

export interface WorkHistory {
  company: string
  position: string
  startDate: string
  endDate: string
  description?: string
}

export interface UserQuery {
  account?: string
  username?: string
  phone?: string
  departmentId?: string
  positionId?: string
  employeeId?: string
  userSource?: number | string
  roleId?: string | string[]
  roleIds?: string[]
  entryDate?: string[]
  entryDateStart?: string
  entryDateEnd?: string
  status?: boolean
  pageNum: number
  pageSize: number
}

export interface Department {
  id: string
  parentId: string
  orgType: 'group' | 'company' | 'subsidiary' | 'department' | 'team'
  orgLevel: number
  deptName: string
  deptCode: string
  leader?: string
  phone?: string
  email?: string
  orderNum: number
  status: number
  children?: Department[]
}

export interface Position {
  id: string
  positionName: string
  positionCode: string
  positionLevel?: number
  orderNum: number
  status: number
  remark?: string
}
