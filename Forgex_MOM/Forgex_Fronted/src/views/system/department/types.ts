export interface Department {
  id: string
  parentId: string
  orgType: string
  orgLevel: number
  deptName: string
  deptCode: string
  leader?: string
  phone?: string
  email?: string
  orderNum?: number
  status: boolean
  tenantId: string
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
  children?: Department[]
}

export interface DepartmentQueryParam {
  tenantId: string
  parentId?: string
  deptName?: string
  deptCode?: string
  orgType?: string
  status?: boolean
}

export interface DepartmentSaveParam {
  id?: string
  tenantId: string
  parentId?: string
  orgType: string
  orgLevel: number
  deptName: string
  deptCode: string
  leader?: string
  phone?: string
  email?: string
  orderNum?: number
  status?: boolean
}
