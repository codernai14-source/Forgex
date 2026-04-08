import { onUnmounted, ref } from 'vue'

export interface UseSseOptions<T = any> {
  url: string
  onEvent?: (eventName: string, data: T) => void
  onError?: (e: any) => void
  autoReconnect?: boolean
  reconnectDelay?: number
}

export function useSse<T = any>(options: UseSseOptions<T>) {
  const source = ref<EventSource | null>(null)
  const connecting = ref(false)
  const reconnectTimer = ref<number | null>(null)
  const closedByUser = ref(false)

  const autoReconnect = options.autoReconnect !== false
  const reconnectDelay = Math.max(options.reconnectDelay ?? 3000, 1000)

  function clearReconnectTimer() {
    if (reconnectTimer.value != null && typeof window !== 'undefined') {
      window.clearTimeout(reconnectTimer.value)
    }
    reconnectTimer.value = null
  }

  function scheduleReconnect() {
    if (!autoReconnect || closedByUser.value || reconnectTimer.value != null || typeof window === 'undefined') {
      return
    }
    reconnectTimer.value = window.setTimeout(() => {
      reconnectTimer.value = null
      connect()
    }, reconnectDelay)
  }

  function connect() {
    if (source.value || connecting.value) {
      return
    }

    clearReconnectTimer()
    closedByUser.value = false
    connecting.value = true

    const es = new EventSource(options.url, { withCredentials: true })
    source.value = es

    es.addEventListener('connected', event => {
      connecting.value = false
      options.onEvent?.('connected', (event as MessageEvent).data as any)
    })

    es.addEventListener('heartbeat', () => {})

    es.addEventListener('message', event => {
      try {
        const raw = (event as MessageEvent).data
        const data = typeof raw === 'string' ? JSON.parse(raw) : raw
        options.onEvent?.('message', data)
      } catch (error) {
        console.warn('[useSse] Failed to parse message payload:', error)
        options.onError?.(error)
      }
    })

    es.onerror = error => {
      connecting.value = false
      options.onError?.(error)
      if (source.value === es) {
        try {
          es.close()
        } catch (_) {
        } finally {
          source.value = null
        }
      }
      scheduleReconnect()
    }
  }

  function close() {
    closedByUser.value = true
    connecting.value = false
    clearReconnectTimer()
    if (!source.value) {
      return
    }
    try {
      source.value.close()
    } catch (_) {
    } finally {
      source.value = null
    }
  }

  onUnmounted(() => close())

  return { source, connect, close }
}
