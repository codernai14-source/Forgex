import http from '../http'

export function getRoutes(body: { account: string; tenantId: string }) {
  return http.post('/sys/menu/routes', body)
}

