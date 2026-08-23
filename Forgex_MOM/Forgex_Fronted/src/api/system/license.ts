import http from '../http'

export type LicenseStatus = 'VALID' | 'EXPIRING_SOON' | 'GRACE' | 'EXPIRED' | 'INVALID' | 'UNLICENSED'

export interface LicenseRuntimeInfo {
  status?: LicenseStatus
  valid?: boolean
  message?: string
  expireAt?: string
  graceDays?: number
  remainingDays?: number
  licenseId?: string
}

export function getLicenseStatus() {
  return http.get<LicenseRuntimeInfo>('/sys/license/status', { loadingMode: 'silent', silentError: true })
}
