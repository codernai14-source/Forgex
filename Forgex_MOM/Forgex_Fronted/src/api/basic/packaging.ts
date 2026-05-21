import http from '@/api/http'

export interface PackagingType {
  id?: number
  packagingCode: string
  packagingName: string
  packagingSpecType?: string
  lengthValue?: number | string | null
  widthValue?: number | string | null
  heightValue?: number | string | null
  sizeUnitId?: number | null
  sizeUnitName?: string
  volumeValue?: number | string | null
  volumeUnitId?: number | null
  volumeUnitName?: string
  weightValue?: number | string | null
  weightUnitId?: number | null
  weightUnitName?: string
  status?: number
  sortOrder?: number
  remark?: string
}

export interface PackagingTypePageParam {
  pageNum: number
  pageSize: number
  packagingCode?: string
  packagingName?: string
  packagingSpecType?: string
  status?: number
}

export interface MaterialPackagingRelation {
  id?: number
  materialId?: number
  materialCode?: string
  materialName?: string
  materialType?: string
  packagingTypeId?: number
  packagingCode?: string
  packagingName?: string
  packagingSpecType?: string
  packagingSlot?: PackagingSlot
  packagingSlotName?: string
}

export type PackagingSlot = 'SMALL' | 'MEDIUM' | 'LARGE'

export interface MaterialPackagingSaveParam {
  materialId: number
  smallPackagingTypeId?: number | null
  mediumPackagingTypeId?: number | null
  largePackagingTypeId?: number | null
}

export interface MaterialPackagingSlotSaveParam {
  materialId: number
  packagingTypeId?: number | null
  packagingSlot: PackagingSlot
}

export function getPackagingTypePage(data: PackagingTypePageParam) {
  return http.post('/basic/packaging/page', data)
}

export function getPackagingTypeDetail(id: number) {
  return http.post<PackagingType>('/basic/packaging/detail', { id })
}

export function createPackagingType(data: PackagingType) {
  return http.post<number>('/basic/packaging/create', data)
}

export function updatePackagingType(data: PackagingType) {
  return http.post('/basic/packaging/update', data)
}

export function deletePackagingType(id: number) {
  return http.post('/basic/packaging/delete', { id })
}

export function batchDeletePackagingType(ids: number[]) {
  return http.post('/basic/packaging/batchDelete', { ids })
}

export function getAllPackagingTypes() {
  return http.post<PackagingType[]>('/basic/packaging/list')
}

export function listPackagingByMaterial(materialId: number) {
  return http.post<MaterialPackagingRelation[]>('/basic/packaging/relation/listByMaterial', { materialId })
}

export function listPackagingByPackaging(packagingTypeId: number) {
  return http.post<MaterialPackagingRelation[]>('/basic/packaging/relation/listByPackaging', { packagingTypeId })
}

export function savePackagingByMaterial(data: MaterialPackagingSaveParam) {
  return http.post('/basic/packaging/relation/saveByMaterial', data)
}

export function savePackagingSlot(data: MaterialPackagingSlotSaveParam) {
  return http.post('/basic/packaging/relation/saveSlot', data)
}

export function deletePackagingRelation(id: number) {
  return http.post('/basic/packaging/relation/delete', { id })
}
