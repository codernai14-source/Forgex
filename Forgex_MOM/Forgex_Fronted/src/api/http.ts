/**
 * HTTP请求配置文件
 * 负责创建和配置axios实例，实现请求/响应拦截器、错误处理等
 * @author Forgex Team
 * @version 1.0.0
 */
import axios from 'axios'
import { message, Modal } from 'ant-design-vue'
import i18n, { getLocale } from '../locales'

/**
 * 需要重新登录的错误码列表
 * 当后端返回这些错误码时，前端需要重新登录
 */
const reloadCodes = [401] // 401: 未授权

/**
 * 创建带拦截器的axios实例（带全局遮罩）
 * 用于处理普通API请求，会显示全局loading
 */
const http = axios.create({
  baseURL: '/api',        // API基础路径
  timeout: 15000,         // 请求超时时间：15秒
  withCredentials: true   // 允许携带跨域凭证
})

/**
 * 创建静默axios实例（不带全局遮罩）
 * 用于后台轮询、自动刷新等不需要用户感知的接口
 */
const silentHttp = axios.create({
  baseURL: '/api',        // API基础路径
  timeout: 15000,         // 请求超时时间：15秒
  withCredentials: true   // 允许携带跨域凭证
})

/**
 * 创建原始axios实例
 * 用于处理特殊请求（如租户恢复），避免拦截器循环调用
 */
const rawHttp = axios.create({
  baseURL: '/api',        // API基础路径
  timeout: 15000,         // 请求超时时间：15秒
  withCredentials: true   // 允许携带跨域凭证
})

/**
 * 活跃请求计数器
 * 用于控制全局加载指示器的显示和隐藏
 */
let activeReq = 0

/**
 * 登录弹窗状态
 * 用于防止重复显示登录失效弹窗
 */
const loginBack = { open: false }

/**
 * 检查错误码是否需要重新登录
 * 
 * @param code 后端返回的错误码
 * @returns boolean 是否需要重新登录
 */
function needReload(code: number): boolean {
  return reloadCodes.includes(code)
}

/**
 * 获取多语言文案
 *
 * @param key i18n key
 * @returns 翻译后的文本
 */
function t(key: string): string {
  return String(i18n.global.t(key))
}

/**
 * 请求拦截器（带全局遮罩）
 * 在发送请求前执行，主要用于：
 * 1. 增加活跃请求计数
 * 2. 根据配置显示全局加载指示器
 */
http.interceptors.request.use(cfg => {
  // 增加活跃请求计数
  activeReq++
  
  // 获取全局加载器实例
  const gl = (window as any).__globalLoader
  let enableLoader = true
  
  // 检查本地存储中的加载指示器配置
  try {
    const raw = localStorage.getItem('fx-layout-config')
    if (raw) {
      const cfgObj = JSON.parse(raw)
      if (cfgObj && cfgObj.loadingIndicatorEnabled === false) {
        enableLoader = false
      }
    }
  } catch (e) {
    // 解析失败时默认显示加载指示器
  }
  
  // 如果允许显示且加载器存在，则显示加载指示器
  if (enableLoader && gl && typeof gl.show === 'function') {
    gl.show()
  }
  
  // 添加语言头
  const locale = getLocale()
  const headersAny: any = cfg.headers || {}
  if (typeof headersAny.set === 'function') {
    headersAny.set('Accept-Language', locale)
    headersAny.set('X-Lang', locale)
  } else {
    headersAny['Accept-Language'] = locale
    headersAny['X-Lang'] = locale
  }
  cfg.headers = headersAny
  
  return cfg
})

/**
 * 请求拦截器（静默模式，不带全局遮罩）
 * 用于后台接口，不显示全局加载指示器
 */
silentHttp.interceptors.request.use(cfg => {
  // 添加语言头
  const locale = getLocale()
  const headersAny: any = cfg.headers || {}
  if (typeof headersAny.set === 'function') {
    headersAny.set('Accept-Language', locale)
    headersAny.set('X-Lang', locale)
  } else {
    headersAny['Accept-Language'] = locale
    headersAny['X-Lang'] = locale
  }
  cfg.headers = headersAny
  
  return cfg
})

/**
 * 通用响应处理函数
 * 处理响应数据，主要用于：
 * 1. 处理文件下载响应
 * 2. 处理业务错误码
 * 3. 处理登录失效情况
 * 4. 处理租户恢复逻辑
 */
async function handleResponse(resp: any, httpInstance: any) {
  // 处理文件下载响应
  if (resp.config.responseType === 'blob') {
    if (resp.status === 200) {
      return resp // 文件下载成功，直接返回响应
    }
    message.warning(t('message.downloadFailedOrNotFound'))
    return Promise.reject(resp)
  }
  
  // 处理JSON响应
  const data = resp.data || {}
  const code = data.code
  
  // 检查是否需要重新登录
  if (needReload(code)) {
    const cfg = resp.config as any
    const msg = String(data?.message || '')
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    // 检查是否可以恢复租户
    const canRecoverTenant = 
      !cfg.__fxTenantRecovered &&          // 未尝试过恢复租户
      !!account &&                          // 存在账号信息
      !!tenantId &&                         // 存在租户ID
      (msg.includes('租户未选择') || msg.includes('租户') || msg.includes('Tenant')) // 错误信息包含租户相关内容
    
    if (canRecoverTenant) {
      cfg.__fxTenantRecovered = true // 标记已尝试恢复租户
      try {
        // 尝试恢复租户
        const r = await rawHttp.post('/auth/choose-tenant', { account, tenantId })
        if (r?.data?.code === 200) {
          // 租户恢复成功，重新发送原请求
          return httpInstance.request(cfg)
        }
      } catch (e) {
        // 恢复租户失败，继续处理登录失效
      }
    }
    
    // 显示登录失效弹窗
    if (!loginBack.open) {
      loginBack.open = true
      Modal.error({
        title: `${t('message.tipTitle')}:`,
        okText: t('message.relogin'),
        content: t('message.sessionExpired'),
        onOk: () => {
          loginBack.open = false
          location.reload() // 刷新页面，跳转到登录页
        }
      })
    }
    
    return Promise.reject(data)
  }
  
  // 处理其他业务错误
  if (code !== 200) {
    const cfgAny = resp.config as any
    const customErrorMessage = cfgAny.customErrorMessage
    const silentError = cfgAny.silentError === true
    if (!silentError) {
      message.error(customErrorMessage || data.message || t('message.operationFailed'))
    }
    return Promise.reject(data)
  }
  
  // 响应成功，显示成功消息（如果有）
  if (code === 200 && data.message) {
    message.success(data.message)
  }
  
  // 响应成功，返回业务数据
  return data.data
}

/**
 * 通用错误处理函数
 */
function handleError(err: any) {
  message.error(t('message.gatewayError'))
  return Promise.reject(err)
}

/**
 * 响应拦截器 - 业务逻辑处理（带全局遮罩）
 */
http.interceptors.response.use(
  async resp => handleResponse(resp, http),
  handleError
)

/**
 * 响应拦截器 - 业务逻辑处理（静默模式）
 */
silentHttp.interceptors.response.use(
  async resp => handleResponse(resp, silentHttp),
  err => Promise.reject(err) // 静默模式不显示错误提示
)

/**
 * 响应拦截器 - 加载指示器处理（仅用于带全局遮罩的http实例）
 * 用于控制全局加载指示器的隐藏，无论请求成功或失败
 */
http.interceptors.response.use(
  r => {
    // 减少活跃请求计数（确保不小于0）
    activeReq = Math.max(0, activeReq - 1)
    
    // 当所有请求完成时，隐藏加载指示器
    if (activeReq === 0) {
      const gl = (window as any).__globalLoader
      if (gl && typeof gl.hide === 'function') {
        gl.hide()
      }
    }
    
    return r
  },
  e => {
    // 错误情况下也需要减少活跃请求计数
    activeReq = Math.max(0, activeReq - 1)
    
    // 当所有请求完成时，隐藏加载指示器
    if (activeReq === 0) {
      const gl = (window as any).__globalLoader
      if (gl && typeof gl.hide === 'function') {
        gl.hide()
      }
    }
    
    return Promise.reject(e)
  }
)

export default http
export { silentHttp }
