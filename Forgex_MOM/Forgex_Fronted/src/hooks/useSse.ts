/**
 * SSE (Server-Sent Events) Hook
 * 
 * 用于处理服务器发送的事件流，支持自动重连和错误处理。
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { onUnmounted, ref } from 'vue'

/**
 * SSE Hook 配置选项
 */
export interface UseSseOptions<T = any> {
  /** SSE 连接 URL */
  url: string
  /** 事件处理回调 */
  onEvent?: (eventName: string, data: T) => void
  /** 错误处理回调 */
  onError?: (e: any) => void
}

/**
 * 创建 SSE 连接
 * 
 * 执行步骤：
 * 1. 创建 EventSource 连接
 * 2. 监听 connected 事件（连接成功）
 * 3. 监听 message 事件（接收消息）
 * 4. 监听 error 事件（处理错误）
 * 5. 在组件卸载时自动关闭连接
 * 
 * @param options SSE 配置选项
 * @param options.url SSE 连接 URL
 * @param options.onEvent 事件处理回调
 * @param options.onError 错误处理回调
 * @returns 包含 source（EventSource 引用）、connect（连接方法）、close（关闭方法）的对象
 * 
 * @remarks
 * 连接管理：
 * - connect(): 建立 SSE 连接（如果已连接则不重复建立）
 * - close(): 关闭 SSE 连接并清空引用
 * - 组件卸载时自动调用 close()
 * 
 * 事件类型：
 * - 'connected': 连接成功事件
 * - 'message': 普通消息事件
 * - 'error': 错误事件（通过 onerror 捕获）
 * 
 * @example
 * ```typescript
 * // 在组件中使用
 * const { connect, close } = useSse({
 *   url: '/api/sse/notifications',
 *   onEvent: (eventName, data) => {
 *     if (eventName === 'message') {
 *       console.log('收到消息:', data)
 *     }
 *   },
 *   onError: (e) => {
 *     console.error('SSE 错误:', e)
 *   }
 * })
 * 
 * // 手动连接
 * connect()
 * ```
 */
export function useSse<T = any>(options: UseSseOptions<T>) {
  const source = ref<EventSource | null>(null)

  /**
   * 建立 SSE 连接
   * 
   * 执行步骤：
   * 1. 检查是否已存在连接（避免重复建立）
   * 2. 创建新的 EventSource 实例
   * 3. 注册 connected 事件监听器
   * 4. 注册 message 事件监听器（解析 JSON 数据）
   * 5. 注册 error 事件监听器
   */
  function connect() {
    if (source.value) return
    const es = new EventSource(options.url, { withCredentials: true })
    source.value = es

    es.addEventListener('connected', e => {
      options.onEvent?.('connected', (e as MessageEvent).data as any)
    })

    // 后端心跳事件，避免浏览器对未监听命名事件的告警；无需业务处理
    es.addEventListener('heartbeat', () => {})

    es.addEventListener('message', e => {
      try {
        const raw = (e as MessageEvent).data
        const data = typeof raw === 'string' ? JSON.parse(raw) : raw
        options.onEvent?.('message', data)
      } catch (err) {
        options.onError?.(err)
      }
    })

    es.onerror = e => {
      options.onError?.(e)
    }
  }

  /**
   * 关闭 SSE 连接
   * 
   * 执行步骤：
   * 1. 检查是否存在连接
   * 2. 调用 close() 方法关闭连接
   * 3. 清空 source 引用
   */
  function close() {
    if (!source.value) return
    try {
      source.value.close()
    } catch (e) {
    } finally {
      source.value = null
    }
  }

  // 组件卸载时自动关闭连接
  onUnmounted(() => close())

  return { source, connect, close }
}
