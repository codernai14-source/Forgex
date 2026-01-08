import http from '../http'

export function listRoles(body: any) {
  return http.post('/sys/role/list', body)
}

export function listRoleMenus(body: any) {
  return http.post('/sys/role/menu/list', body)
}

export function grantRoleMenus(body: any) {
  return http.post('/sys/role/menu/grant', body)
}

