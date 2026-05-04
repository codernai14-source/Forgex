/**
 * SSE (Server-Sent Events) Store
 *
 * @description 管理 SSE 连接、订阅回调、消息历史和重连状态。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'

/**
 * SSE 消息结构。
 */
export interface SseMessage {
  /**
   * 消息类型。
   */
  type: string

  /**
   * 消息数据。
   */
  data: any

  /**
   * 消息时间戳。
   */
  timestamp?: number
}

/**
 * 订阅回调函数类型。
 */
type SubscribeCallback = (message: any) => void

export const useSseStore = defineStore('sse', () => {
  // ============ State ============

  /**
   * SSE 连接实例。
   */
  const eventSource = ref<EventSource | null>(null)

  /**
   * 连接状态。
   */
  const isConnected = ref(false)

  /**
   * 连接 URL。
   */
  const connectionUrl = ref<string>('')

  /**
   * 订阅回调集合。
   */
  const subscribers = ref<Map<string, Set<SubscribeCallback>>>(new Map())

  /**
   * 消息历史记录，最多保留 100 条。
   */
  const messageHistory = ref<SseMessage[]>([])

  /**
   * 重连尝试次数。
   */
  const reconnectAttempts = ref(0)

  /**
   * 最大重连次数。
   */
  const maxReconnectAttempts = 5

  /**
   * 重连延迟，单位毫秒。
   */
  const reconnectDelay = 3000

  // ============ Computed ============

  /**
   * 是否可以重连。
   */
  const canReconnect = computed(() => reconnectAttempts.value < maxReconnectAttempts)

  // ============ 操作 ============

  /**
   * 建立 SSE 连接。
   *
   * @param url SSE 连接 URL。
   * @returns 是否成功发起连接。
   */
  function connect(url: string): boolean {
    // 连接前先关闭已有连接。
    if (eventSource.value) {
      disconnect()
    }

    // 未登录时不建立 SSE 连接。
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      console.warn('[SSE] 用户未登录，无法建立 SSE 连接')
      return false
    }

    try {
      connectionUrl.value = url
      eventSource.value = new EventSource(url)

      eventSource.value.onopen = () => {
        console.log('[SSE] 连接已建立')
        isConnected.value = true
        reconnectAttempts.value = 0
      }

      eventSource.value.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as SseMessage
          handleMessage(message)
        } catch (error) {
          console.error('[SSE] 消息解析失败:', error)
        }
      }

      eventSource.value.onerror = (error) => {
        console.error('[SSE] 连接错误:', error)
        isConnected.value = false

        if (canReconnect.value) {
          reconnectAttempts.value++
          console.log(`[SSE] 尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})`)

          setTimeout(() => {
            if (connectionUrl.value) {
              connect(connectionUrl.value)
            }
          }, reconnectDelay)
        } else {
          console.warn('[SSE] 已达到最大重连次数，停止重连')
          disconnect()
        }
      }

      return true
    } catch (error) {
      console.error('[SSE] 创建连接失败:', error)
      return false
    }
  }

  /**
   * 断开 SSE 连接。
   */
  function disconnect(): void {
    if (eventSource.value) {
      eventSource.value.close()
      eventSource.value = null
    }

    isConnected.value = false
    connectionUrl.value = ''
    reconnectAttempts.value = 0

    console.log('[SSE] 连接已关闭')
  }

  /**
   * 处理接收到的消息。
   *
   * @param message SSE 消息。
   */
  function handleMessage(message: SseMessage): void {
    messageHistory.value.push({
      ...message,
      timestamp: Date.now()
    })

    if (messageHistory.value.length > 100) {
      messageHistory.value.shift()
    }

    const callbacks = subscribers.value.get(message.type)
    if (callbacks && callbacks.size > 0) {
      callbacks.forEach(callback => {
        try {
          callback(message.data)
        } catch (error) {
          console.error(`[SSE] 回调执行失败 (type: ${message.type}):`, error)
        }
      })
    }

    const globalCallbacks = subscribers.value.get('*')
    if (globalCallbacks && globalCallbacks.size > 0) {
      globalCallbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('[SSE] 通用回调执行失败:', error)
        }
      })
    }
  }

  /**
   * 订阅指定类型消息。
   *
   * @param type 消息类型，例如 'message'、'notification' 或 '*'。
   * @param callback 回调函数。
   * @returns 取消订阅函数。
   */
  function subscribe(type: string, callback: SubscribeCallback): () => void {
    if (!subscribers.value.has(type)) {
      subscribers.value.set(type, new Set())
    }

    const callbacks = subscribers.value.get(type)!
    callbacks.add(callback)

    return () => {
      callbacks.delete(callback)

      if (callbacks.size === 0) {
        subscribers.value.delete(type)
      }
    }
  }

  /**
   * 取消全部订阅。
   */
  function unsubscribeAll(): void {
    subscribers.value.clear()
  }

  /**
   * 清空消息历史。
   */
  function clearHistory(): void {
    messageHistory.value = []
  }

  /**
   * 获取最近消息。
   *
   * @param count 返回数量，默认 10。
   * @param type 消息类型过滤。
   * @returns 消息列表。
   */
  function getRecent消息(count: number = 10, type?: string): SseMessage[] {
    let messages = messageHistory.value

    if (type) {
      messages = messages.filter(msg => msg.type === type)
    }

    return messages.slice(-count)
  }

  return {
    // State
    eventSource,
    isConnected,
    connectionUrl,
    messageHistory,
    reconnectAttempts,

    // Computed
    canReconnect,

    // 操作
    connect,
    disconnect,
    subscribe,
    unsubscribeAll,
    clearHistory,
    getRecent消息
  }
})
