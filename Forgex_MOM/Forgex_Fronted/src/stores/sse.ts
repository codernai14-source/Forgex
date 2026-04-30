/**
 * SSE (Server-Sent Events) Store
 * 
 * @description 绠＄悊 SSE 杩炴帴銆佹秷鎭闃呭拰鎺ㄩ€侀€氱煡
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'

/**
 * SSE 娑堟伅绫诲瀷
 */
export interface SseMessage {
  /**
   * 娑堟伅绫诲瀷
   */
  type: string
  
  /**
   * 娑堟伅鏁版嵁
   */
  data: any
  
  /**
   * 娑堟伅鏃堕棿鎴?
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
   * SSE 杩炴帴瀹炰緥
   */
  const eventSource = ref<EventSource | null>(null)
  
  /**
   * 杩炴帴鐘舵€?
   */
  const isConnected = ref(false)
  
  /**
   * 杩炴帴 URL
   */
  const connectionUrl = ref<string>('')
  
  /**
   * 璁㈤槄鍥炶皟鏄犲皠琛?
   * @description 鎸夋秷鎭被鍨嬪瓨鍌ㄨ闃呭洖璋冨嚱鏁?
   */
  const subscribers = ref<Map<string, Set<SubscribeCallback>>>(new Map())
  
  /**
   * 娑堟伅鍘嗗彶璁板綍锛堟渶杩?100 鏉★級
   */
  const messageHistory = ref<SseMessage[]>([])
  
  /**
   * 閲嶈繛娆℃暟
   */
  const reconnectAttempts = ref(0)
  
  /**
   * 鏈€澶ч噸杩炴鏁?
   */
  const maxReconnectAttempts = 5
  
  /**
   * 閲嶈繛寤惰繜锛堟绉掞級
   */
  const reconnectDelay = 3000
  
  // ============ Computed ============
  
  /**
   * 鏄惁鍙互閲嶈繛
   */
  const canReconnect = computed(() => reconnectAttempts.value < maxReconnectAttempts)
  
  // ============ 操作 ============
  
  /**
   * 寤虹珛 SSE 杩炴帴
   * 
   * @param url SSE 鏈嶅姟绔?URL
   * @returns 鏄惁鎴愬姛寤虹珛杩炴帴
   * 
   * @example
   * ```ts
   * const sseStore = useSseStore()
   * sseStore.connect('/api/sse/connect')
   * ```
   */
  function connect(url: string): boolean {
    // 濡傛灉宸茶繛鎺ワ紝鍏堟柇寮€
    if (eventSource.value) {
      disconnect()
    }
    
    // 妫€鏌ョ敤鎴锋槸鍚﹀凡鐧诲綍
    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      console.warn('[SSE] 鐢ㄦ埛鏈櫥褰曪紝鏃犳硶寤虹珛 SSE 杩炴帴')
      return false
    }
    
    try {
      // 鍒涘缓 EventSource 杩炴帴
      connectionUrl.value = url
      eventSource.value = new EventSource(url)
      
      // 监听连接打开事件
      eventSource.value.onopen = () => {
        console.log('[SSE] 连接已建立')
        isConnected.value = true
        reconnectAttempts.value = 0
      }
      
      // 鐩戝惉娑堟伅浜嬩欢
      eventSource.value.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as SseMessage
          handleMessage(message)
        } catch (error) {
          console.error('[SSE] 娑堟伅瑙ｆ瀽澶辫触:', error)
        }
      }
      
      // 鐩戝惉閿欒浜嬩欢
      eventSource.value.onerror = (error) => {
        console.error('[SSE] 杩炴帴閿欒:', error)
        isConnected.value = false
        
        // 灏濊瘯閲嶈繛
        if (canReconnect.value) {
          reconnectAttempts.value++
          console.log(`[SSE] 灏濊瘯閲嶈繛 (${reconnectAttempts.value}/${maxReconnectAttempts})`)
          
          setTimeout(() => {
            if (connectionUrl.value) {
              connect(connectionUrl.value)
            }
          }, reconnectDelay)
        } else {
          console.warn('[SSE] 杈惧埌鏈€澶ч噸杩炴鏁帮紝鍋滄閲嶈繛')
          disconnect()
        }
      }
      
      return true
    } catch (error) {
      console.error('[SSE] 鍒涘缓杩炴帴澶辫触:', error)
      return false
    }
  }
  
  /**
   * 鏂紑 SSE 杩炴帴
   * 
   * @description 鍏抽棴 EventSource 杩炴帴骞舵竻鐞嗙浉鍏崇姸鎬?
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
    
    console.log('[SSE] 杩炴帴宸叉柇寮€')
  }
  
  /**
   * 处理接收到的消息
   * 
   * @param message SSE 娑堟伅瀵硅薄
   */
  function handleMessage(message: SseMessage): void {
    // 璁板綍娑堟伅鍘嗗彶
    messageHistory.value.push({
      ...message,
      timestamp: Date.now()
    })
    
    // 限制历史记录数量
    if (messageHistory.value.length > 100) {
      messageHistory.value.shift()
    }
    
    // 瑙﹀彂瀵瑰簲绫诲瀷鐨勮闃呭洖璋?
    const callbacks = subscribers.value.get(message.type)
    if (callbacks && callbacks.size > 0) {
      callbacks.forEach(callback => {
        try {
          callback(message.data)
        } catch (error) {
          console.error(`[SSE] 鍥炶皟鎵ц澶辫触 (type: ${message.type}):`, error)
        }
      })
    }
    
    // 触发通用订阅回调（type 涓?'*'锛?
    const globalCallbacks = subscribers.value.get('*')
    if (globalCallbacks && globalCallbacks.size > 0) {
      globalCallbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('[SSE] 閫氱敤鍥炶皟鎵ц澶辫触:', error)
        }
      })
    }
  }
  
  /**
   * 璁㈤槄鎸囧畾绫诲瀷鐨勬秷鎭?
   * 
   * @param type 娑堟伅绫诲瀷锛堝 'message', 'notification', '*' 琛ㄧず璁㈤槄鎵€鏈夋秷鎭級
   * @param callback 回调函数
   * @returns 鍙栨秷璁㈤槄鐨勫嚱鏁?
   * 
   * @example
   * ```ts
   * const sseStore = useSseStore()
   * 
   * // 璁㈤槄 message 绫诲瀷娑堟伅
   * const unsubscribe = sseStore.subscribe('message', (data) => {
   *   console.log('鏀跺埌娑堟伅:', data)
   * })
   * 
   * // 鍙栨秷璁㈤槄
   * unsubscribe()
   * ```
   */
  function subscribe(type: string, callback: SubscribeCallback): () => void {
    // 鑾峰彇鎴栧垱寤鸿绫诲瀷鐨勮闃呴泦鍚?
    if (!subscribers.value.has(type)) {
      subscribers.value.set(type, new Set())
    }
    
    const callbacks = subscribers.value.get(type)!
    callbacks.add(callback)
    
    // 返回取消订阅函数
    return () => {
      callbacks.delete(callback)
      
      // 濡傛灉璇ョ被鍨嬫病鏈夎闃呰€呬簡锛岀Щ闄ゆ暣涓泦鍚?
      if (callbacks.size === 0) {
        subscribers.value.delete(type)
      }
    }
  }
  
  /**
   * 鍙栨秷鎵€鏈夎闃?
   */
  function unsubscribeAll(): void {
    subscribers.value.clear()
  }
  
  /**
   * 娓呴櫎娑堟伅鍘嗗彶璁板綍
   */
  function clearHistory(): void {
    messageHistory.value = []
  }
  
  /**
   * 鑾峰彇鏈€杩戠殑娑堟伅
   * 
   * @param count 鑾峰彇鏁伴噺锛岄粯璁?10
   * @param type 娑堟伅绫诲瀷杩囨护锛屽彲閫?
   * @returns 娑堟伅鍒楄〃
   */
  function getRecent消息(count: number = 10, type?: string): SseMessage[] {
    let messages = messageHistory.value
    
    if (type) {
      messages = messages.filter(msg => msg.type === type)
    }
    
    return messages.slice(-count)
  }
  
  // ============ 生命周期 ============
  
  /**
   * 缁勪欢鍗歌浇鏃惰嚜鍔ㄦ柇寮€杩炴帴
   */
  // 娉ㄦ剰锛氬湪瀹為檯浣跨敤涓紝搴斿湪缁勪欢鐨?onUnmounted 涓皟鐢?disconnect
  
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
