<!--
 * 消息通知组件
 * 监听 SSE 推送的消息，使用 Ant Design Vue 的 notification 组件显示
 * 
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-04-08
-->

<template>
  <div class="message-notification">
    <!-- 组件内容，实际显示由 notification 组件处理 -->
  </div>
</template>

<script setup lang="ts">
import { notification } from 'ant-design-vue'
import { useSseStore } from '@/stores/sse'
import { useRouter } from 'vue-router'
import { onMounted, onUnmounted } from 'vue'
import { speakMessage } from '@/utils/messageSpeech'

const router = useRouter()
const sseStore = useSseStore()

/**
 * 取消订阅函数引用
 * @description 用于组件卸载时取消 SSE 消息订阅
 */
let unsubscribe: (() => void) | null = null
const handledMessageKeys = new Set<string>()

function handleIncomingMessage(message: any, shouldNotify = true) {
  if (!message || typeof message !== 'object') return

  const key = String(message?.id || `${message?.title || ''}:${message?.content || ''}`).trim()
  if (key && handledMessageKeys.has(key)) return
  if (key) {
    handledMessageKeys.add(key)
    if (handledMessageKeys.size > 100) {
      handledMessageKeys.delete(handledMessageKeys.values().next().value as string)
    }
  }
  if (shouldNotify) {
    showNotification(message)
  }
  speakMessage(message)
}

function handleMessageReceivedEvent(event: Event) {
  const detail = (event as CustomEvent<any>).detail
  if (!detail || typeof detail !== 'object') return
  handleIncomingMessage(detail, false)
}

/**
 * 图标类型映射
 * error: 错误通知（红色）
 * info: 普通通知（蓝色）
 * warning: 警告通知（黄色）
 * success: 成功通知（绿色）
 * custom: 自定义通知（默认蓝色）
 */
const iconMap: Record<string, 'success' | 'info' | 'warning' | 'error'> = {
  error: 'error',
  info: 'info',
  warning: 'warning',
  success: 'success',
  custom: 'info'
}

/**
 * 组件挂载时订阅 SSE 消息
 */
onMounted(() => {
  // 订阅 message 类型消息，并保存取消订阅函数
  unsubscribe = sseStore.subscribe('message', (message: any) => {
    handleIncomingMessage(message)
  })
  window.addEventListener('fx:message-received', handleMessageReceivedEvent as EventListener)
})

/**
 * 组件卸载时取消订阅
 * @description 防止内存泄漏和重复订阅
 */
onUnmounted(() => {
  if (unsubscribe) {
    unsubscribe()
    unsubscribe = null
  }
  window.removeEventListener('fx:message-received', handleMessageReceivedEvent as EventListener)
})

/**
 * 显示通知
 * @param message 消息对象
 */
const showNotification = (message: any) => {
  const { messageType, title, content, linkUrl } = message
  if (!title && !content) return
  
  // 根据消息类型映射图标
  const icon = iconMap[messageType] || 'info'
  
  // 显示通知
  notification[icon]({
    message: title,
    description: content,
    duration: 4.5,
    placement: 'topRight',
    onClick: () => {
      // 点击通知跳转到消息链接
      if (linkUrl) {
        router.push(linkUrl)
      }
    }
  })
}

/**
 * 显示成功通知
 * @param title 标题
 * @param description 描述
 */
const showSuccess = (title: string, description: string) => {
  notification.success({
    message: title,
    description,
    duration: 4.5
  })
}

/**
 * 显示错误通知
 * @param title 标题
 * @param description 描述
 */
const showError = (title: string, description: string) => {
  notification.error({
    message: title,
    description,
    duration: 4.5
  })
}

/**
 * 显示警告通知
 * @param title 标题
 * @param description 描述
 */
const showWarning = (title: string, description: string) => {
  notification.warning({
    message: title,
    description,
    duration: 4.5
  })
}

/**
 * 显示信息通知
 * @param title 标题
 * @param description 描述
 */
const showInfo = (title: string, description: string) => {
  notification.info({
    message: title,
    description,
    duration: 4.5
  })
}

// 暴露方法供外部调用
defineExpose({
  showSuccess,
  showError,
  showWarning,
  showInfo
})
</script>

<style scoped lang="less" src="@/styles/components/Notification/message-notification.less"></style>
