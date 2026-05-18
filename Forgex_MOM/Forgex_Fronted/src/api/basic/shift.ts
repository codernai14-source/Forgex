import http from '@/api/http'

export interface ShiftPeriod {
  id?: number
  shiftId?: number
  timeType?: 'WORK' | 'REST'
  startTime?: string
  endTime?: string
  sortOrder?: number
}

export interface Shift {
  id?: number
  shiftName?: string
  shiftCode?: string
  status?: boolean
  remark?: string
  periodList?: ShiftPeriod[]
  createTime?: string
  updateTime?: string
}

export interface ShiftPageParam {
  pageNum: number
  pageSize: number
  shiftName?: string
  shiftCode?: string
  status?: boolean
}

export const shiftApi = {
  page(params: ShiftPageParam) {
    return http.post('/basic/shift/page', params)
  },
  list(params?: Partial<ShiftPageParam>) {
    return http.post<Shift[]>('/basic/shift/list', params || {})
  },
  detail(id: number) {
    return http.post<Shift>('/basic/shift/detail', { id })
  },
  create(data: Shift) {
    return http.post<number>('/basic/shift/create', data)
  },
  update(data: Shift) {
    return http.post<boolean>('/basic/shift/update', data)
  },
  delete(id: number) {
    return http.post<boolean>('/basic/shift/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/shift/batchDelete', { ids })
  },
}
