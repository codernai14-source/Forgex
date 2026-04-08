import http from '../http'

export type LoginBackgroundType = 'video' | 'image' | 'color'
export type LoginStyle = 'cyber' | 'simple' | 'classic'
export type LoginLayout = 'center' | 'split' | 'compact'

export interface SystemBasicConfig {
  systemName: string
  systemLogo: string
  systemVersion: string
  copyright: string
  copyrightLink: string
  loginPageTitle: string
  loginPageSubtitle: string
  loginBackgroundType: LoginBackgroundType
  loginBackgroundVideo: string
  loginBackgroundImage: string
  loginBackgroundColor: string
  loginStyle: LoginStyle
  loginLayout?: LoginLayout
  showOAuthLogin: boolean
  primaryColor: string
  secondaryColor: string
}

export interface CaptchaImageConfig {
  keyPrefix: string
  expireSeconds: number
  width: number
  height: number
  length: number
}

export interface CaptchaSliderConfig {
  secondaryEnabled: boolean
  keyPrefix: string
  secondaryKeyPrefix: string
  tokenExpireSeconds: number
  provider: string
}

export interface CaptchaConfig {
  mode: 'none' | 'image' | 'slider'
  image: CaptchaImageConfig
  slider: CaptchaSliderConfig
}

export interface PasswordPolicyConfig {
  store: string
  defaultPassword: string
  minLength: number
  requireNumbers: boolean
  requireUppercase: boolean
  requireLowercase: boolean
  requireSymbols: boolean
}

export interface LoginSecurityConfig {
  failWindowMinutes: number
  maxFailCount: number
  lockMinutes: number
}

export interface CryptoTransportConfig {
  algorithm: string
  publicKey: string
  privateKey: string
  cipher: string
}

export interface SecurityConfig {
  captcha: CaptchaConfig
  passwordPolicy: PasswordPolicyConfig
  loginSecurity: LoginSecurityConfig
  cryptoTransport: CryptoTransportConfig
}

export interface EmailConfig {
  providerType: 'local' | 'aliyun' | 'qq' | string
  senderAccount: string
  senderPassword: string
  smtpHost: string
  smtpPort: number
  authEnabled: boolean
  sslEnabled: boolean
  starttlsEnabled: boolean
}

export interface FileUploadConfig {
  storageType: 'LOCAL' | 'OSS' | 'MINIO'
  localUploadPath: string
  accessPrefix: string
  providerConfigJson: string
}

export function createDefaultSystemBasicConfig(): SystemBasicConfig {
  return {
    systemName: 'FORGEX_MOM',
    systemLogo: '',
    systemVersion: '1.0.0',
    copyright: '© 2025 FORGEX_MOM',
    copyrightLink: '#',
    loginPageTitle: '欢迎来到FORGEX_MOM！',
    loginPageSubtitle: '',
    loginBackgroundType: 'image',
    loginBackgroundVideo: '/loading.mp4',
    loginBackgroundImage: '/back.jpg',
    loginBackgroundColor: '#0d0221',
    loginStyle: 'cyber',
    loginLayout: 'center',
    showOAuthLogin: true,
    primaryColor: '#05d9e8',
    secondaryColor: '#ff2a6d',
  }
}

export function createDefaultSecurityConfig(): SecurityConfig {
  return {
    captcha: {
      mode: 'none',
      image: {
        keyPrefix: 'captcha:image',
        expireSeconds: 120,
        width: 120,
        height: 40,
        length: 4,
      },
      slider: {
        secondaryEnabled: false,
        keyPrefix: 'captcha:slider',
        secondaryKeyPrefix: 'captcha:secondary',
        tokenExpireSeconds: 120,
        provider: 'redis-token',
      },
    },
    passwordPolicy: {
      store: 'bcrypt',
      defaultPassword: 'Aa123456',
      minLength: 8,
      requireNumbers: true,
      requireUppercase: false,
      requireLowercase: false,
      requireSymbols: false,
    },
    loginSecurity: {
      failWindowMinutes: 15,
      maxFailCount: 5,
      lockMinutes: 30,
    },
    cryptoTransport: {
      algorithm: 'SM2',
      publicKey: '',
      privateKey: '',
      cipher: 'BCD',
    },
  }
}

export function createDefaultEmailConfig(): EmailConfig {
  return {
    providerType: 'local',
    senderAccount: '',
    senderPassword: '',
    smtpHost: '',
    smtpPort: 465,
    authEnabled: true,
    sslEnabled: true,
    starttlsEnabled: true,
  }
}

export function createDefaultFileUploadConfig(): FileUploadConfig {
  return {
    storageType: 'LOCAL',
    localUploadPath: './uploads',
    accessPrefix: '/files',
    providerConfigJson: '',
  }
}

export function getLoginCaptcha() {
  return http.get<CaptchaConfig>('/sys/config/login-captcha')
}

export function putLoginCaptcha(body: Partial<CaptchaConfig>) {
  return http.put('/sys/config/login-captcha', body)
}

export function getSystemBasicConfig() {
  return http.get<SystemBasicConfig>('/sys/config/system-basic')
}

export function setSystemBasicConfig(data: SystemBasicConfig) {
  return http.put('/sys/config/system-basic', data)
}

export function getSecurityConfig() {
  return http.get<SecurityConfig>('/sys/config/security')
}

export function setSecurityConfig(data: SecurityConfig) {
  return http.put('/sys/config/security', data)
}

export function getEmailConfig() {
  return http.get<EmailConfig>('/sys/config/email')
}

export function setEmailConfig(data: EmailConfig) {
  return http.put('/sys/config/email', data)
}

export function getFileUploadConfig() {
  return http.get<FileUploadConfig>('/sys/config/file-upload')
}

export function setFileUploadConfig(data: FileUploadConfig) {
  return http.put('/sys/config/file-upload', data)
}
