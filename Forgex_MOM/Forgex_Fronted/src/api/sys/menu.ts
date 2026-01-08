import http from '../http'

export function listMenusTree(body: any) {
  return http.post('/sys/menu/tree', body)
}

