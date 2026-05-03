import http from '@/api/http'

export interface UnitTypeNode {
  id?: number
  unitTypeCode: string
  unitTypeName: string
  parentId?: number
  levelPath?: string
  children?: UnitTypeNode[]
}

export interface UnitTypeParam {
  id?: number
  unitTypeCode: string
  unitTypeName: string
  parentId?: number
}

export interface UnitMaster {
  id?: number
  unitTypeId?: number
  unitTypeCode?: string
  unitTypeName?: string
  unitCode: string
  unitName: string
  remark?: string
}

export interface UnitPageParam {
  pageNum?: number
  pageSize?: number
  unitTypeId?: number
  unitCode?: string
  unitName?: string
}

export interface UnitConversion {
  id?: number
  unitId?: number
  unitCode?: string
  unitName?: string
  targetUnitId?: number
  targetUnitCode?: string
  targetUnitName?: string
  conversionValue?: number | string
}

export interface UnitConversionSaveParam {
  unitId: number
  conversions: Array<{
    id?: number
    targetUnitId?: number
    conversionValue?: number | string
  }>
}

export const unitApi = {
  typeTree() {
    return http.post<UnitTypeNode[]>('/basic/unit/type/tree', {})
  },
  typeDetail(id: number) {
    return http.post<UnitTypeNode>('/basic/unit/type/detail', { id })
  },
  createType(data: UnitTypeParam) {
    return http.post<number>('/basic/unit/type/create', data)
  },
  updateType(data: UnitTypeParam) {
    return http.post<boolean>('/basic/unit/type/update', data)
  },
  deleteType(id: number) {
    return http.post<boolean>('/basic/unit/type/delete', { id })
  },
  page(data: UnitPageParam) {
    return http.post<any>('/basic/unit/page', data)
  },
  list(data?: UnitPageParam) {
    return http.post<UnitMaster[]>('/basic/unit/list', data || {})
  },
  detail(id: number) {
    return http.post<UnitMaster>('/basic/unit/detail', { id })
  },
  create(data: UnitMaster) {
    return http.post<number>('/basic/unit/create', data)
  },
  update(data: UnitMaster) {
    return http.post<boolean>('/basic/unit/update', data)
  },
  delete(id: number) {
    return http.post<boolean>('/basic/unit/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/unit/batchDelete', { ids })
  },
  listConversions(unitId: number) {
    return http.post<UnitConversion[]>('/basic/unit/conversion/list', { unitId })
  },
  saveConversions(data: UnitConversionSaveParam) {
    return http.post<boolean>('/basic/unit/conversion/save', data)
  },
  deleteConversion(id: number) {
    return http.post<boolean>('/basic/unit/conversion/delete', { id })
  },
}

export function getUnitPage(data: UnitPageParam) {
  return unitApi.page(data)
}

export function getUnitDetail(id: number) {
  return unitApi.detail(id)
}

export function createUnit(data: UnitMaster) {
  return unitApi.create(data)
}

export function updateUnit(data: UnitMaster) {
  return unitApi.update(data)
}

export function deleteUnit(id: number) {
  return unitApi.delete(id)
}

export function batchDeleteUnit(ids: number[]) {
  return unitApi.batchDelete(ids)
}

export function getAllUnits() {
  return unitApi.list({})
}
