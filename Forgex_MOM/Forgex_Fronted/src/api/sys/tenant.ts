import http from '../http'

export function listTenantIgnore() {
  return http.get('/sys/tenant-ignore')
}

export function reloadTenantIgnore() {
  return http.post('/sys/tenant-ignore/reload')
}

