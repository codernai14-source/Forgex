/**
 * HTTP请求配置文件
 * 负责创建和配置axios实例，实现请求/响应拦截器、错误处理等
 * @author Forgex Team
 * @version 1.0.0
 */
import axios, { type AxiosRequestConfig, type AxiosInstance } from 'axios'
import { message, Modal } from 'ant-design-vue'
import i18n, { getLocale } from '../locales'

/**
 * Forgex 请求扩展配置
 *
 * 在 AxiosRequestConfig 基础上补充了前端统一提示控制参数：
 * - showSuccessMessage: 是否弹出后端成功消息
 * - silentError: 是否静默业务错误提示
 * - customErrorMessage: 自定义错误提示文案
 */
export interface FxRequestConfig extends AxiosRequestConfig {
  showSuccessMessage?: boolean
  silentError?: boolean
  customErrorMessage?: string
}

/**
 * 统一请求客户端接口
 *
 * 用于对外暴露明确语义的请求客户端：
 * - `http`：普通请求，不自动弹成功消息
 * - `httpSuccess`：成功时自动弹后端 message
 * - `silentHttp`：静默请求，不显示全局 loading，也不自动提示
 */
export interface FxHttpClient {
  request<T = any>(config: FxRequestConfig): Promise<T>
  get<T = any>(url: string, config?: FxRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: FxRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: FxRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: FxRequestConfig): Promise<T>
  patch<T = any>(url: string, data?: any, config?: FxRequestConfig): Promise<T>
}

const sessionLoginStateKeys = ['account', 'tenantId', 'permissions']
const localLoginCacheKeys = ['fx-dynamic-routes', 'fx-dynamic-modules']
const backendToastSuppressWindow = 300
const originalMessageSuccess = typeof message.success === 'function' ? message.success.bind(message) : undefined
const originalMessageError = typeof message.error === 'function' ? message.error.bind(message) : undefined
const recentBackendToast = {
  type: '' as '' | 'success' | 'error',
  timestamp: 0,
}
let backendToastDepth = 0

function shouldSuppressFrontendMessage(type: 'success' | 'error'): boolean {
  return backendToastDepth === 0
    && recentBackendToast.type === type
    && Date.now() - recentBackendToast.timestamp <= backendToastSuppressWindow
}

function callOriginalMessage(type: 'success' | 'error', content: any) {
  return type === 'success'
    ? originalMessageSuccess?.(content)
    : originalMessageError?.(content)
}

function showBackendMessage(type: 'success' | 'error', content: string) {
  if (!content) {
    return undefined
  }

  recentBackendToast.type = type
  recentBackendToast.timestamp = Date.now()
  backendToastDepth++
  try {
    return callOriginalMessage(type, content)
  } finally {
    backendToastDepth = Math.max(0, backendToastDepth - 1)
  }
}

if (!(message as any).__fxBackendToastPatched) {
  const wrapMessageMethod = (type: 'success' | 'error') => {
    return (content: any, ...rest: any[]) => {
      if (shouldSuppressFrontendMessage(type)) {
        return undefined
      }

      const original = type === 'success' ? originalMessageSuccess : originalMessageError
      return original?.(content, ...rest)
    }
  }

  ;(message as any).success = wrapMessageMethod('success')
  ;(message as any).error = wrapMessageMethod('error')
  ;(message as any).__fxBackendToastPatched = true
}

/**
 * 需要重新登录的错误码列表
 * 当后端返回这些错误码时，前端需要重新登录
 */
const reloadCodes = [602] // 602: 未登录或登录过期

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

function clearClientLoginState() {
  sessionLoginStateKeys.forEach(key => sessionStorage.removeItem(key))
  localLoginCacheKeys.forEach(key => localStorage.removeItem(key))
}

function redirectToLogin() {
  clearClientLoginState()
  loginBack.open = false

  if (window.location.pathname === '/login') {
    window.location.reload()
    return
  }

  window.location.replace(new URL('/login', window.location.origin).toString())
}

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

function extractBackendMessage(payload: any): string {
  const candidates = [payload?.message, payload?.msg]
  for (const candidate of candidates) {
    if (typeof candidate === 'string') {
      const trimmed = candidate.trim()
      if (trimmed) {
        return trimmed
      }
    }
  }
  return ''
}

function extractErrorMessage(err: any): string {
  const respData = err?.response?.data
  const backendMessage = extractBackendMessage(respData)
  if (backendMessage) {
    return backendMessage
  }

  const errorMessage = typeof err?.message === 'string' ? err.message.trim() : ''
  if (errorMessage) {
    return errorMessage
  }

  return t('message.gatewayError')
}

/**
 * 判断接口路径是否需要显示成功消息
 * 
 * 根据 URL 路径中的关键词判断：
 * - 写操作（新增、修改、删除、提交等）显示成功消息
 * - 读操作（查询、列表、详情等）不显示成功消息
 * 
 * @param url 接口 URL
 * @returns true-需要显示成功消息，false-不需要显示
 */
function shouldShowSuccessByPath(url: string): boolean {
  const urlLower = url.toLowerCase()
  
  // 需要显示成功消息的关键词（写操作）
  const showSuccessKeywords = [
    // 新增类
    '/create', '/add', '/insert', '/save', '/new',
    // 修改类
    '/update', '/edit', '/modify', '/change',
    // 删除类
    '/delete', '/remove', '/batchdelete', '/batch-delete',
    // 状态变更类
    '/updatestatus', '/changestatus', '/enable', '/disable',
    // 授权类
    '/auth', '/grant', '/assign', '/savebyuser', '/savebyrole',
    // 密码类
    '/resetpassword', '/changepassword', '/updatepassword',
    // 提交类
    '/submit', '/apply', '/approve', '/audit',
    // 发送类
    '/send', '/publish',
    // 上传类
    '/upload', '/import',
    // 其他写操作
    '/choose-tenant', '/register', '/logout', '/changelanguage'
  ]
  
  // 不需要显示成功消息的关键词（读操作）
  const hideSuccessKeywords = [
    // 查询列表类
    '/page', '/list', '/query', '/search', '/find',
    // 查询详情类
    '/detail', '/get', '/info', '/tree', '/byusername',
    // 下载导出类
    '/download', '/export', '/template',
    // 检查验证类
    '/count', '/check', '/exists', '/verify', '/validate',
    // 配置查询类
    '/config', '/settings', '/preferences',
    // 其他读操作
    '/routes', '/menu', '/permission', '/statistic', '/dashboard',
    '/captcha', '/public-key', '/unread', '/read', '/read-all',
    '/kickout', '/online', '/loginlog', '/operationlog',
    '/provider', '/fields', '/layout', '/basic', '/security', '/email', '/file-upload'
  ]
  
  // 优先匹配需要显示的关键词
  for (const keyword of showSuccessKeywords) {
    if (urlLower.includes(keyword)) {
      return true
    }
  }
  
  // 如果匹配到不需要显示的关键词，则隐藏
  for (const keyword of hideSuccessKeywords) {
    if (urlLower.includes(keyword)) {
      return false
    }
  }
  
  // 默认：POST 请求且后端返回了 message 则显示
  return true
}

/**
 * 判断是否需要显示成功消息
 * 
 * 判断逻辑：
 * 1. 必须是 http 实例（非静默模式）
 * 2. 后端必须返回了 message
 * 3. 根据接口路径或显式配置决定是否显示
 * 
 * @param resp 响应对象
 * @param httpInstance HTTP 实例
 * @param backendMessage 后端返回的 message
 * @returns true-显示成功消息，false-不显示
 */
function shouldShowSuccessMessage(resp: any, httpInstance: any, backendMessage: string): boolean {
  // 非 http 实例或没有 backendMessage，不显示
  if (httpInstance !== http || !backendMessage) {
    return false
  }

  const cfgAny = resp?.config as any
  // 如果显式配置了 showSuccessMessage，则优先使用配置
  if (cfgAny?.showSuccessMessage !== undefined) {
    return cfgAny.showSuccessMessage === true
  }
  
  // 根据接口路径判断是否需要显示成功消息
  const url = resp.config?.url || ''
  return shouldShowSuccessByPath(url)
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
    const msg = extractBackendMessage(data)
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
          redirectToLogin()
        },
        afterClose: () => {
          loginBack.open = false
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
    const backendMessage = extractBackendMessage(data)
    if (!silentError) {
      showBackendMessage('error', customErrorMessage || backendMessage || t('message.operationFailed'))
    }
    return Promise.reject(data)
  }

  const backendMessage = extractBackendMessage(data)
  if (shouldShowSuccessMessage(resp, httpInstance, backendMessage)) {
    showBackendMessage('success', backendMessage)
  }

  // 响应成功，返回业务数据
  return data.data
}

/**
 * 通用错误处理函数
 */
function handleError(err: any) {
  showBackendMessage('error', extractErrorMessage(err))
  return Promise.reject(err?.response?.data ?? err)
}

function mergeRequestConfig(defaults?: FxRequestConfig, config?: FxRequestConfig): FxRequestConfig {
  return {
    ...(defaults || {}),
    ...(config || {}),
    headers: {
      ...((defaults?.headers as Record<string, any>) || {}),
      ...((config?.headers as Record<string, any>) || {})
    }
  }
}

function createHttpClient(instance: AxiosInstance, defaults?: FxRequestConfig): FxHttpClient {
  return {
    request<T = any>(config: FxRequestConfig) {
      return instance.request<T, T>(mergeRequestConfig(defaults, config))
    },
    get<T = any>(url: string, config?: FxRequestConfig) {
      return instance.get<T, T>(url, mergeRequestConfig(defaults, config))
    },
    delete<T = any>(url: string, config?: FxRequestConfig) {
      return instance.delete<T, T>(url, mergeRequestConfig(defaults, config))
    },
    post<T = any>(url: string, data?: any, config?: FxRequestConfig) {
      return instance.post<T, T>(url, data, mergeRequestConfig(defaults, config))
    },
    put<T = any>(url: string, data?: any, config?: FxRequestConfig) {
      return instance.put<T, T>(url, data, mergeRequestConfig(defaults, config))
    },
    patch<T = any>(url: string, data?: any, config?: FxRequestConfig) {
      return instance.patch<T, T>(url, data, mergeRequestConfig(defaults, config))
    }
  }
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

/**
 * 默认请求客户端
 *
 * 适合列表、详情、下拉、联动查询等请求。
 * 特点：
 * - 显示全局 loading
 * - 自动处理错误提示
 * - 对于写操作（POST/PUT/DELETE/PATCH），如果后端返回了 message，自动显示成功消息
 * - 对于读操作（GET），不自动显示成功消息
 * - 可通过配置 showSuccessMessage: true/false 手动控制
 */
const httpClient = createHttpClient(http, {
  showSuccessMessage: undefined // 不显式设置，由 shouldShowSuccessMessage 根据请求方法和后端 message 自动判断
})

/**
 * 成功提示请求客户端
 *
 * 适合新增、修改、删除、授权、保存配置等写操作。
 * 特点：
 * - 显示全局 loading
 * - 自动处理错误提示
 * - 成功时自动弹出后端返回的 message
 */
const httpSuccess = createHttpClient(http, {
  showSuccessMessage: true
})

/**
 * 静默请求客户端
 *
 * 适合消息角标、轮询、草稿恢复、预读取等不希望打扰用户的请求。
 * 特点：
 * - 不显示全局 loading
 * - 默认静默错误提示
 * - 不自动弹成功消息
 */
const silentHttpClient = createHttpClient(silentHttp, {
  showSuccessMessage: false,
  silentError: true
})

export default httpClient
export { httpSuccess, silentHttpClient as silentHttp }
