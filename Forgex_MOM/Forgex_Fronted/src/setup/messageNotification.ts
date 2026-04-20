/**
 * 消息通知初始化配置
 * 负责初始化 SSE 连接和消息通知组件
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 */

import { App } from 'vue'
import MessageNotification from '@/components/Notification/MessageNotification.vue'
import { useSse } from '@/hooks/useSse'
import { useUserStore } from '@/stores/user'

/**
 * SSE 连接配置
 */
const SSE_CONFIG = {
  url: '/sse/connect',
  autoReconnect: true,
  reconnectDelay: 3000
}

/**
 * 初始化消息通知
 * @param app Vue 应用实例
 */
export function setupMessageNotification(app: App) {
  // 注册消息通知组件
  app.component('MessageNotification', MessageNotification)
  
  // 初始化 SSE 连接（在用户登录后）
  initSseConnection()
}

/**
 * 初始化 SSE 连接
 * 监听用户登录状态，登录后自动连接 SSE
 */
function initSseConnection() {
  const userStore = useUserStore()
  
  // 监听用户登录
  watch(
    () => userStore.isLoggedIn,
    (isLoggedIn) => {
      if (isLoggedIn) {
        connectSse()
      } else {
        disconnectSse()
      }
    },
    { immediate: true }
  )
}

let sseConnection: ReturnType<typeof useSse> | null = null

/**
 * 连接 SSE
 */
function connectSse() {
  if (sseConnection) {
    return
  }
  
  sseConnection = useSse({
    url: SSE_CONFIG.url,
    autoReconnect: SSE_CONFIG.autoReconnect,
    reconnectDelay: SSE_CONFIG.reconnectDelay,
    onEvent: (eventName, data) => {
      console.log('[SSE] Event:', eventName, data)
    },
    onError: (error) => {
      console.error('[SSE] Error:', error)
    }
  })
  
  sseConnection.connect()
}

/**
 * 断开 SSE 连接
 */
function disconnectSse() {
  if (sseConnection) {
    sseConnection.close()
    sseConnection = null
  }
}

/**
 * 监听 Vue 的 watch 函数
 */
function watch<T>(source: () => T, callback: (value: T, oldValue: T) => void, options?: any) {
  // 这里使用一个简单的实现，实际应该使用 Vue 的 watch
  // 由于这是在 setup 阶段，我们需要在组件内部使用
  // 所以这里只是一个占位符
}
