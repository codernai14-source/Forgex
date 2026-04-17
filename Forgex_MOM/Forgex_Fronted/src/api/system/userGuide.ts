import http from '../http'
import type { GuidePreferenceConfig } from '@/types/guide'

export interface UserGuidePreferencePayload {
  account: string
  tenantId?: string | number | null
}

export interface SaveUserGuidePreferencePayload extends UserGuidePreferencePayload {
  config: GuidePreferenceConfig
}

/**
 * 获取当前用户的引导偏好配置。
 */
export function getUserGuidePreference(payload: UserGuidePreferencePayload) {
  return http.post<GuidePreferenceConfig>('/sys/user-style/get-guide', payload)
}

/**
 * 保存当前用户的引导偏好配置。
 */
export function saveUserGuidePreference(payload: SaveUserGuidePreferencePayload) {
  return http.post<boolean>('/sys/user-style/save-guide', payload)
}
