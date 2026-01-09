import http from '../http'

export function listRoles(body: any) {
  return http.post('/sys/role/list', body)
}

export function listRoleMenus(body: any) {
  return http.post('/sys/role/menu/list', body)
}

export function getRoleAuthData(body: any) {
  return http.post('/sys/role/menu/authData', body)
}

export function grantRoleMenus(body: any) {
  return http.post('/sys/role/menu/grant', body)
}
