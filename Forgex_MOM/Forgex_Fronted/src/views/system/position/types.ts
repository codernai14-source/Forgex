export interface Position {
  id: string
  positionName: string
  positionCode: string
  positionLevel?: number
  orderNum?: number
  status: boolean
  remark?: string
  tenantId: string
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
}

export interface PositionQueryParam {
  tenantId: string
  positionName?: string
  positionCode?: string
  status?: boolean
}

export interface PositionSaveParam {
  id?: string
  tenantId: string
  positionName: string
  positionCode: string
  positionLevel?: number
  orderNum?: number
  status?: boolean
  remark?: string
}
