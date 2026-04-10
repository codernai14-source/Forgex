/**
 * SSE (Server-Sent Events) Store
 * 
 * @description 管理 SSE 连接、消息订阅和推送通知
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'

/**
 * SSE 消息类型
 */
export interface SseMessage {
  /**
   * 消息类型
   */
  type: string
  
  /**
   * 消息数据
   */
  data: any
  
  /**
   * 消息时间戳
   */
  timestamp?: number
}

/**
 * 订阅回调函数类型
 */
type SubscribeCallback = (message: any) => void

export const useSseStore = defineStore('sse', () => {
  // ============ State ============
  
  /**
   * SSE 连接实例
   */
  const eventSource = ref<EventSource | null>(null)
  
  /**
   * 连接状态
   */
  const isConnected = ref(false)
  
  /**
   * 连接 URL
   */
  const connectionUrl = ref<string>('')
  
  /**
   * 订阅回调映射表
   * @description 按消息类型存储订阅回调函数
   */
  const subscribers = ref<Map<string, Set<SubscribeCallback>>>(new Map())
  
  /**
   * 消息历史记录（最近 100 条）
   */
  const messageHistory = ref<SseMessage[]>([])
  
  /**
   * 重连次数
   */
  const reconnectAttempts = ref(0)
  
  /**
   * 最大重连次数
   */
  const maxReconnectAttempts = 5
  
  /**
   * 重连延迟（毫秒）
   */
  const reconnectDelay = 3000
  
  // ============ Computed ============
  
  /**
   * 是否可以重连
   */
  const canReconnect = computed(() => reconnectAttempts.value < maxReconnectAttempts)
  
  // ============ Actions ============
  
  /**
   * 建立 SSE 连接
   * 
   * @param url SSE 服务端 URL
   * @returns 是否成功建立连接
   * 
   * @example
   * ```ts
   * const sseStore = useSseStore()
   * sseStore.connect('/api/sse/connect')
   * ```
   */
  function connect(url: string): boolean {
    // 如果已连接，先断开
    if (eventSource.value) {
      disconnect()
    }
    
    // 检查用户是否已登录
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      console.warn('[SSE] 用户未登录，无法建立 SSE 连接')
      return false
    }
    
    try {
      // 创建 EventSource 连接
      connectionUrl.value = url
      eventSource.value = new EventSource(url)
      
      // 监听连接打开事件
      eventSource.value.onopen = () => {
        console.log('[SSE] 连接已建立')
        isConnected.value = true
        reconnectAttempts.value = 0
      }
      
      // 监听消息事件
      eventSource.value.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as SseMessage
          handleMessage(message)
        } catch (error) {
          console.error('[SSE] 消息解析失败:', error)
        }
      }
      
      // 监听错误事件
      eventSource.value.onerror = (error) => {
        console.error('[SSE] 连接错误:', error)
        isConnected.value = false
        
        // 尝试重连
        if (canReconnect.value) {
          reconnectAttempts.value++
          console.log(`[SSE] 尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})`)
          
          setTimeout(() => {
            if (connectionUrl.value) {
              connect(connectionUrl.value)
            }
          }, reconnectDelay)
        } else {
          console.warn('[SSE] 达到最大重连次数，停止重连')
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
   * 断开 SSE 连接
   * 
   * @description 关闭 EventSource 连接并清理相关状态
   * 
   * @example
   * ```ts
   * const sseStore = useSseStore()
   * sseStore.disconnect()
   * ```
   */
  function disconnect(): void {
    if (eventSource.value) {
      eventSource.value.close()
      eventSource.value = null
    }
    
    isConnected.value = false
    connectionUrl.value = ''
    reconnectAttempts.value = 0
    
    console.log('[SSE] 连接已断开')
  }
  
  /**
   * 处理接收到的消息
   * 
   * @param message SSE 消息对象
   */
  function handleMessage(message: SseMessage): void {
    // 记录消息历史
    messageHistory.value.push({
      ...message,
      timestamp: Date.now()
    })
    
    // 限制历史记录数量
    if (messageHistory.value.length > 100) {
      messageHistory.value.shift()
    }
    
    // 触发对应类型的订阅回调
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
    
    // 触发通用订阅回调（type 为 '*'）
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
   * 订阅指定类型的消息
   * 
   * @param type 消息类型（如 'message', 'notification', '*' 表示订阅所有消息）
   * @param callback 回调函数
   * @returns 取消订阅的函数
   * 
   * @example
   * ```ts
   * const sseStore = useSseStore()
   * 
   * // 订阅 message 类型消息
   * const unsubscribe = sseStore.subscribe('message', (data) => {
   *   console.log('收到消息:', data)
   * })
   * 
   * // 取消订阅
   * unsubscribe()
   * ```
   */
  function subscribe(type: string, callback: SubscribeCallback): () => void {
    // 获取或创建该类型的订阅集合
    if (!subscribers.value.has(type)) {
      subscribers.value.set(type, new Set())
    }
    
    const callbacks = subscribers.value.get(type)!
    callbacks.add(callback)
    
    // 返回取消订阅函数
    return () => {
      callbacks.delete(callback)
      
      // 如果该类型没有订阅者了，移除整个集合
      if (callbacks.size === 0) {
        subscribers.value.delete(type)
      }
    }
  }
  
  /**
   * 取消所有订阅
   */
  function unsubscribeAll(): void {
    subscribers.value.clear()
  }
  
  /**
   * 清除消息历史记录
   */
  function clearHistory(): void {
    messageHistory.value = []
  }
  
  /**
   * 获取最近的消息
   * 
   * @param count 获取数量，默认 10
   * @param type 消息类型过滤，可选
   * @returns 消息列表
   */
  function getRecentMessages(count: number = 10, type?: string): SseMessage[] {
    let messages = messageHistory.value
    
    if (type) {
      messages = messages.filter(msg => msg.type === type)
    }
    
    return messages.slice(-count)
  }
  
  // ============ 生命周期 ============
  
  /**
   * 组件卸载时自动断开连接
   */
  // 注意：在实际使用中，应在组件的 onUnmounted 中调用 disconnect
  
  return {
    // State
    eventSource,
    isConnected,
    connectionUrl,
    messageHistory,
    reconnectAttempts,
    
    // Computed
    canReconnect,
    
    // Actions
    connect,
    disconnect,
    subscribe,
    unsubscribeAll,
    clearHistory,
    getRecentMessages
  }
})