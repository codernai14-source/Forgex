/**
 * 系统配置 API 接口文件
 * 负责与后端系统配置接口进行交互
 * @author Forgex Team
 * @version 1.0.0
 */
import http from '../http'

/**
 * 系统基础配置接口
 * 定义系统基础配置的数据结构
 */
export interface SystemBasicConfig {
  // ==================== 系统基本信息 ===================
  /** 系统名称 */
  systemName: string
  /** 系统Logo（URL路径） */
  systemLogo: string
  /** 系统版本 */
  systemVersion: string
  
  // ==================== 版权信息 ===================
  /** 版权信息 */
  copyright: string
  /** 版权链接 */
  copyrightLink: string
  
  // ==================== 登录页配置 ===================
  /** 登录页标题 */
  loginPageTitle: string
  /** 登录页副标题 */
  loginPageSubtitle: string
  /** 背景类型：video、image、color */
  loginBackgroundType: 'video' | 'image' | 'color'
  /** 登录页背景视频路径（URL） */
  loginBackgroundVideo: string
  /** 登录页背景图片（URL路径） */
  loginBackgroundImage: string
  /** 登录页背景颜色 */
  loginBackgroundColor: string
  /** 登录页风格：cyber、simple、classic */
  loginStyle: 'cyber' | 'simple' | 'classic'
  /** 是否显示第三方登录 */
  showOAuthLogin: boolean
  
  // ==================== 主题配色 ===================
  /** 主色调 */
  primaryColor: string
  /** 辅助色 */
  secondaryColor: string
}

/**
 * 获取登录验证码配置
 * @returns 验证码配置对象
 */
export function getLoginCaptcha() {
  return http.get('/sys/config/login-captcha')
}

/**
 * 更新登录验证码配置
 * @param body 配置数据
 * @returns 是否成功
 */
export function putLoginCaptcha(body: any) {
  return http.put('/sys/config/login-captcha', body)
}

/**
 * 获取全局系统基础配置
 * @returns 系统基础配置对象
 */
export function getSystemBasicConfig() {
  return http.get<SystemBasicConfig>('/sys/config/system-basic')
}

/**
 * 更新全局系统基础配置
 * @param data 系统基础配置对象
 * @returns 是否成功
 */
export function setSystemBasicConfig(data: SystemBasicConfig) {
  return http.put('/sys/config/system-basic', data)
}
