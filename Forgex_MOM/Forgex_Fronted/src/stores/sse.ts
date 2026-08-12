/**
 * SSE (Server-Sent Events) Store
 *
 * @description Manages SSE connections, subscriptions, message history, and reconnect state.
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useUserStore } from './user'

export interface SseMessage {
  type: string
  data: any
  timestamp?: number
}

type SubscribeCallback = (message: any) => void

export const useSseStore = defineStore('sse', () => {
  const eventSource = ref<EventSource | null>(null)
  const isConnected = ref(false)
  const connectionUrl = ref<string>('')
  const subscribers = ref<Map<string, Set<SubscribeCallback>>>(new Map())
  const messageHistory = ref<SseMessage[]>([])
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 5
  const reconnectDelay = 3000

  const canReconnect = computed(() => reconnectAttempts.value < maxReconnectAttempts)

  function connect(url: string): boolean {
    if (eventSource.value) {
      disconnect()
    }

    const userStore = useUserStore()
    if (!userStore.isLoggedIn) {
      console.warn('[SSE] User is not logged in, cannot establish SSE connection')
      return false
    }

    try {
      connectionUrl.value = url
      eventSource.value = new EventSource(url)

      eventSource.value.onopen = () => {
        console.log('[SSE] Connection established')
        isConnected.value = true
        reconnectAttempts.value = 0
      }

      eventSource.value.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data) as SseMessage
          handleMessage(message)
        } catch (error) {
          console.error('[SSE] Failed to parse message:', error)
        }
      }

      eventSource.value.onerror = (error) => {
        console.error('[SSE] Connection error:', error)
        isConnected.value = false

        if (canReconnect.value) {
          reconnectAttempts.value++
          console.log(`[SSE] Reconnecting (${reconnectAttempts.value}/${maxReconnectAttempts})`)

          setTimeout(() => {
            if (connectionUrl.value) {
              connect(connectionUrl.value)
            }
          }, reconnectDelay)
        } else {
          console.warn('[SSE] Max reconnect attempts reached, stopping reconnect')
          disconnect()
        }
      }

      return true
    } catch (error) {
      console.error('[SSE] Failed to create connection:', error)
      return false
    }
  }

  function disconnect(): void {
    if (eventSource.value) {
      eventSource.value.close()
      eventSource.value = null
    }

    isConnected.value = false
    connectionUrl.value = ''
    reconnectAttempts.value = 0

    console.log('[SSE] Connection closed')
  }

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
          console.error(`[SSE] Callback failed (type: ${message.type}):`, error)
        }
      })
    }

    const globalCallbacks = subscribers.value.get('*')
    if (globalCallbacks && globalCallbacks.size > 0) {
      globalCallbacks.forEach(callback => {
        try {
          callback(message)
        } catch (error) {
          console.error('[SSE] Global callback failed:', error)
        }
      })
    }
  }

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

  function unsubscribeAll(): void {
    subscribers.value.clear()
  }

  function clearHistory(): void {
    messageHistory.value = []
  }

  function getRecentMessages(count: number = 10, type?: string): SseMessage[] {
    let messages = messageHistory.value

    if (type) {
      messages = messages.filter(msg => msg.type === type)
    }

    return messages.slice(-count)
  }

  return {
    eventSource,
    isConnected,
    connectionUrl,
    messageHistory,
    reconnectAttempts,
    canReconnect,
    connect,
    disconnect,
    subscribe,
    unsubscribeAll,
    clearHistory,
    getRecentMessages
  }
})
