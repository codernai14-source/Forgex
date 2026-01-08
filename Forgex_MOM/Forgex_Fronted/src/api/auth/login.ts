import http from '../http'

export function login(data: { account: string; password: string; captcha?: string; captchaId?: string }) {
  return http.post('/auth/login', data)
}

export function chooseTenant(data: { tenantId: string; account: string }) {
  return http.post('/auth/choose-tenant', data)
}

export function getPublicKey() {
  return http.get('/auth/crypto/public-key')
}

export function updateTenantPreferences(data: { account: string; ordered: string[]; defaultTenantId?: string }) {
  return http.post('/auth/tenant/preferences', data)
}

