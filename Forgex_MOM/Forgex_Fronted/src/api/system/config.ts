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
  minLength: number
  requireNumbers: boolean
  requireUppercase: boolean
  requireLowercase: boolean
  requireSymbols: boolean
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
  cryptoTransport: CryptoTransportConfig
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
    loginBackgroundType: 'video',
    loginBackgroundVideo: '/jws.mp4',
    loginBackgroundImage: '',
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
      minLength: 8,
      requireNumbers: true,
      requireUppercase: false,
      requireLowercase: false,
      requireSymbols: false,
    },
    cryptoTransport: {
      algorithm: 'SM2',
      publicKey: '',
      privateKey: '',
      cipher: 'BCD',
    },
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

export function getFileUploadConfig() {
  return http.get<FileUploadConfig>('/sys/config/file-upload')
}

export function setFileUploadConfig(data: FileUploadConfig) {
  return http.put('/sys/config/file-upload', data)
}
