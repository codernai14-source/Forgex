import http from '../http'

export function getDashboardStatistics(body: any) {
  return http.post('/sys/dashboard/statistics', body)
}
