import http from '@/api/http'

export interface WorkCalendarDay {
  id?: number
  calendarDate: string
  yearValue: number
  monthValue: number
  dayValue: number
  dateType: number
  holidayName?: string
  customWeek?: string
  publicWeek?: string
  holidaySynced?: boolean
  holidaySourceYear?: number
  remark?: string
}

export interface WorkCalendarEvent {
  id?: number
  scope?: 'USER' | 'TENANT'
  recordType: string
  eventTitle: string
  eventContent?: string
  startTime: string
  endTime: string
  notifyUserIds?: number[]
  remindMinutes?: number
  remindTime?: string
  messageTemplateCode?: string
  ownerUserId?: number
  sourceUserId?: number
  remark?: string
}

export interface WorkCalendarMonthResult {
  year: number
  month: number
  syncHoliday: boolean
  days: WorkCalendarDay[]
  events: WorkCalendarEvent[]
}

export interface WorkCalendarMonthParam {
  year: number
  month: number
  syncHoliday?: boolean
  calendarScopes?: string[]
}

export const workCalendarApi = {
  month(params: WorkCalendarMonthParam) {
    return http.post<WorkCalendarMonthResult>('/basic/work-calendar/month', params)
  },
  saveEvent(data: WorkCalendarEvent) {
    return http.post<number>('/basic/work-calendar/event/save', data)
  },
  pushTenant(data: WorkCalendarEvent) {
    return http.post<number>('/basic/work-calendar/event/push-tenant', data)
  },
  deleteEvent(id: number, scope: string) {
    return http.post<boolean>('/basic/work-calendar/event/delete', { id, scope })
  },
  updateDay(data: Partial<WorkCalendarDay> & { calendarDate: string }) {
    return http.post<boolean>('/basic/work-calendar/day/update', data)
  },
}
