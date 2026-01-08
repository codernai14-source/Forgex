import axios from 'axios'
import { message, Modal } from 'ant-design-vue'

const reloadCodes = [401]

const http = axios.create({ baseURL: '/api', timeout: 15000, withCredentials: true })
let activeReq = 0

http.interceptors.request.use(cfg => {
  activeReq++
  const gl = (window as any).__globalLoader
  let enableLoader = true
  try {
    const raw = localStorage.getItem('fx-layout-config')
    if (raw) {
      const cfgObj = JSON.parse(raw)
      if (cfgObj && cfgObj.loadingIndicatorEnabled === false) {
        enableLoader = false
      }
    }
  } catch (e) {}
  if (enableLoader && gl && typeof gl.show === 'function') gl.show()
  return cfg
})

const loginBack = { open: false }

function needReload(code: number) { return reloadCodes.includes(code) }

http.interceptors.response.use(
  resp => {
    if (resp.config.responseType === 'blob') {
      if (resp.status === 200) return resp
      message.warning('文件下载失败或此文件不存在')
      return Promise.reject(resp)
    }
    const data = resp.data || {}
    const code = data.code
    if (needReload(code)) {
      if (!loginBack.open) {
        loginBack.open = true
        Modal.error({ title: '提示：', okText: '重新登录', content: '登录已失效，请重新登录', onOk: () => { loginBack.open = false; location.reload() } })
      }
      return Promise.reject(data)
    }
    if (code !== 200) {
      const customErrorMessage = (resp.config as any).customErrorMessage
      message.error(customErrorMessage || data.message || '请求失败')
      return Promise.reject(data)
    }
    return data.data
  },
  err => {
    message.error('网关错误')
    return Promise.reject(err)
  }
)

http.interceptors.response.use(
  r => {
    activeReq = Math.max(0, activeReq - 1)
    if (activeReq === 0) {
      const gl = (window as any).__globalLoader
      if (gl && typeof gl.hide === 'function') gl.hide()
    }
    return r
  },
  e => {
    activeReq = Math.max(0, activeReq - 1)
    if (activeReq === 0) {
      const gl = (window as any).__globalLoader
      if (gl && typeof gl.hide === 'function') gl.hide()
    }
    return Promise.reject(e)
  }
)

export default http
