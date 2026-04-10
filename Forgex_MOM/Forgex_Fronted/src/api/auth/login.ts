import http from '../http'

/**
 * 登录 API
 * @param data 登录请求参数
 * @param data.account 账号
 * @param data.password 密码（RSA 加密后的密文）
 * @param data.captcha 验证码（可选，滑块验证通过后可不传）
 * @param data.captchaId 验证码 ID（可选，滑块验证通过后可不传）
 * @returns 登录结果，包含 token、refreshToken 等信息
 * @throws 登录失败时抛出异常
 */
export function login(data: { account: string; password: string; captcha?: string; captchaId?: string }) {
  return http.post('/auth/login', data)
}

/**
 * 租户选择结果信息
 */
export interface ChosenUserInfo {
  /** 用户 ID */
  id: number
  /** 账号 */
  account: string
  /** 用户名 */
  username: string
  /** 邮箱（可选） */
  email?: string
  /** 手机号（可选） */
  phone?: string
  /** 头像 URL（可选） */
  avatar?: string
  /** 状态（可选） */
  status?: boolean
  /** 租户 ID（可选） */
  tenantId?: number
}

/**
 * 选择租户 API
 * @param data 选择租户参数
 * @param data.tenantId 租户 ID
 * @param data.account 账号
 * @returns 选择租户后的用户信息
 * @throws 选择失败时抛出异常
 */
export function chooseTenant(data: { tenantId: string; account: string }) {
  return http.post<ChosenUserInfo>('/auth/choose-tenant', data)
}

/**
 * 获取 RSA 公钥 API
 * @returns 公钥字符串
 * @throws 获取失败时抛出异常
 */
export function getPublicKey() {
  return http.get('/auth/crypto/public-key')
}

/**
 * 更新租户偏好设置 API
 * @param data 更新参数
 * @param data.account 账号
 * @param data.ordered 有序租户 ID 列表（JSON 字符串）
 * @param data.defaultTenantId 默认租户 ID（可选）
 * @returns 更新结果
 * @throws 更新失败时抛出异常
 */
export function updateTenantPreferences(data: { account: string; ordered: string[]; defaultTenantId?: string }) {
  return http.post('/auth/tenant/preferences', data)
}

/**
 * 切换语言 API
 * @param data 切换参数
 * @param data.lang 目标语言代码（如 zh-CN、en-US）
 * @returns 切换结果
 * @throws 切换失败时抛出异常
 */
export function changeLanguage(data: { lang: string }) {
  return http.post('/auth/changeLanguage', data)
}

/**
 * 登出 API
 * @returns 登出结果
 * @throws 登出失败时抛出异常
 */
export function logout() {
  return http.post('/auth/logout')
}

/**
 * 获取社交授权 URL API
 * @param platform 社交平台类型
 * @returns 授权 URL 字符串
 * @throws 获取失败时抛出异常
 */
export function getSocialAuthorizeUrl(platform: 'WECHAT' | 'DINGTALK') {
  return http.get<string>('/auth/social/authorizeUrl', { params: { platform } })
}

/**
 * 邀请码注册 API
 * @param data 注册参数
 * @returns 注册结果
 */
export function register(data: {
  account: string
  username: string
  password: string
  phone?: string
  email?: string
  inviteCode: string
  captcha?: string
  captchaId?: string
}) {
  return http.post('/auth/register', data)
}

/**
 * 校验邀请码 API
 * @param inviteCode 邀请码
 * @returns 校验结果
 */
export function checkInviteCode(inviteCode: string) {
  return http.post('/auth/register/check-invite', { inviteCode })
}

