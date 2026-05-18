import http from '@/api/http'

export interface Workshop {
  id?: number
  workshopCode?: string
  workshopName?: string
  factoryId?: number
  factoryCode?: string
  factoryName?: string
  status?: boolean
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface WorkshopPageParam {
  pageNum: number
  pageSize: number
  workshopCode?: string
  workshopName?: string
  factoryId?: number
  status?: boolean
}

export const workshopApi = {
  page(params: WorkshopPageParam) {
    return http.post('/basic/workshop/page', params)
  },
  list(params?: Partial<WorkshopPageParam>) {
    return http.post<Workshop[]>('/basic/workshop/list', params || {})
  },
  detail(id: number) {
    return http.post<Workshop>('/basic/workshop/detail', { id })
  },
  create(data: Workshop) {
    return http.post<number>('/basic/workshop/create', data)
  },
  update(data: Workshop) {
    return http.post<boolean>('/basic/workshop/update', data)
  },
  delete(id: number) {
    return http.post<boolean>('/basic/workshop/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/workshop/batchDelete', { ids })
  },
}
