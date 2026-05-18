import http from '@/api/http'

export interface TeamEmployee {
  id?: number
  teamId?: number
  employeeId?: number
  employeeNo?: string
  employeeName?: string
}

export interface Team {
  id?: number
  teamCode?: string
  teamName?: string
  leaderEmployeeId?: number
  leaderEmployeeNo?: string
  leaderEmployeeName?: string
  currentShiftId?: number
  currentShiftName?: string
  workshopId?: number
  workshopCode?: string
  workshopName?: string
  status?: boolean
  remark?: string
  employeeList?: TeamEmployee[]
  createTime?: string
  updateTime?: string
}

export interface TeamPageParam {
  pageNum: number
  pageSize: number
  teamCode?: string
  teamName?: string
  leaderEmployeeId?: number
  currentShiftId?: number
  workshopId?: number
  status?: boolean
}

export const teamApi = {
  page(params: TeamPageParam) {
    return http.post('/basic/team/page', params)
  },
  list(params?: Partial<TeamPageParam>) {
    return http.post<Team[]>('/basic/team/list', params || {})
  },
  detail(id: number) {
    return http.post<Team>('/basic/team/detail', { id })
  },
  create(data: Team) {
    return http.post<number>('/basic/team/create', data)
  },
  update(data: Team) {
    return http.post<boolean>('/basic/team/update', data)
  },
  delete(id: number) {
    return http.post<boolean>('/basic/team/delete', { id })
  },
  batchDelete(ids: number[]) {
    return http.post<boolean>('/basic/team/batchDelete', { ids })
  },
}
