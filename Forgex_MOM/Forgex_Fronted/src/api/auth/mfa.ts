import http from '../http'
import type { LoginResult } from './login'

/**
 * MFA 绑定信息。
 */
export interface MfaBindResult {
  otpAuthUri: string
  secret: string
  recoveryCodes: string[]
}

/**
 * 开始绑定 MFA。
 */
export function bindMfa() {
  return http.post<MfaBindResult>('/auth/mfa/bind')
}

/**
 * 确认绑定。
 *
 * @param code 动态码
 */
export function confirmMfa(code: string) {
  return http.post('/auth/mfa/confirm', { code })
}

/**
 * 解绑 MFA。
 */
export function unbindMfa() {
  return http.post('/auth/mfa/unbind')
}

/**
 * 登录阶段校验动态码。
 *
 * @param challengeId 挑战票据
 * @param code 动态码或恢复码
 */
export function verifyMfa(challengeId: string, code: string) {
  return http.post<LoginResult>('/auth/mfa/verify', { challengeId, code }, { silentError: true })
}

/**
 * 强制改密后继续登录。
 *
 * @param ticket 票据
 * @param newPassword 加密后的新口令
 */
export function forceChangePassword(ticket: string, newPassword: string) {
  return http.post<LoginResult>('/auth/password/force-change', { ticket, newPassword }, { silentError: true })
}
