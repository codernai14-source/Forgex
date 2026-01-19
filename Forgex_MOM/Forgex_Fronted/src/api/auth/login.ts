import http from '../http'

export function login(data: { account: string; password: string; captcha?: string; captchaId?: string }) {
  return http.post('/auth/login', data)
}

export interface ChosenUserInfo {
  id: number
  account: string
  username: string
  email?: string
  phone?: string
  avatar?: string
  status?: boolean
  tenantId?: number
}

export function chooseTenant(data: { tenantId: string; account: string }) {
  return http.post<ChosenUserInfo>('/auth/choose-tenant', data)
}

export function getPublicKey() {
  return http.get('/auth/crypto/public-key')
}

export function updateTenantPreferences(data: { account: string; ordered: string[]; defaultTenantId?: string }) {
  return http.post('/auth/tenant/preferences', data)
}

export function changeLanguage(data: { lang: string }) {
  return http.post('/auth/changeLanguage', data)
}

export function logout() {
  return http.post('/auth/logout')
}

export function getSocialAuthorizeUrl(platform: 'WECHAT' | 'DINGTALK') {
  return http.get<string>('/auth/social/authorizeUrl', { params: { platform } })
}

