<template>
  <a-modal
    v-model:open="visible"
    centered
    :width="760"
    :mask-closable="false"
    :keyboard="false"
    :footer="null"
    class="system-notice-popup"
    @cancel="ackCurrent"
  >
    <div v-if="currentNotice" class="notice-popup">
      <div class="notice-popup__head">
        <a-tag :color="currentNotice.scope === 'PUBLIC' ? 'blue' : 'green'">
          {{ scopeText(currentNotice.scope) }}
        </a-tag>
        <h2>{{ currentNotice.title }}</h2>
        <span class="notice-popup__time">{{ currentNotice.startTime || currentNotice.createTime || '' }}</span>
      </div>

      <div class="notice-popup__content" v-html="currentNotice.contentHtml"></div>

      <div v-if="currentNotice.attachments?.length" class="notice-popup__attachments">
        <div class="notice-popup__attachments-title">{{ t('system.notice.popup.attachment') }}</div>
        <a
          v-for="item in currentNotice.attachments"
          :key="`${item.fileUrl}-${item.fileName}`"
          :href="normalizeMediaUrl(item.fileUrl || '')"
          target="_blank"
          rel="noopener noreferrer"
        >
          {{ item.fileName || item.fileUrl }}
        </a>
      </div>

      <div class="notice-popup__footer">
        <a-button type="primary" @click="ackCurrent">{{ t('system.notice.popup.acknowledge') }}</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { noticeApi, type NoticeScope, type SysNotice } from '@/api/system/notice'
import { PERSONAL_HOME_PATH } from '@/router'
import { normalizeMediaUrl } from '@/utils/media'

const { t } = useI18n()
const route = useRoute()
const visible = ref(false)
const notices = ref<SysNotice[]>([])
const activeIndex = ref(0)
const loading = ref(false)
let refreshTimer: number | null = null
let visibilityHandler: (() => void) | null = null
let refreshHandler: ((event?: Event) => void) | null = null

const currentNotice = computed(() => notices.value[activeIndex.value])

function clearRefreshTimer() {
  if (refreshTimer != null && typeof window !== 'undefined') {
    window.clearTimeout(refreshTimer)
  }
  refreshTimer = null
}

function schedulePopupRefresh() {
  if (typeof window === 'undefined') return
  clearRefreshTimer()
  refreshTimer = window.setTimeout(() => {
    refreshTimer = null
    void loadPopupNotices()
  }, 300)
}

function shouldRefreshPopup() {
  return route.path === PERSONAL_HOME_PATH && typeof document !== 'undefined' && !document.hidden
}

function handleSystemNoticeRefreshEvent() {
  if (shouldRefreshPopup()) {
    schedulePopupRefresh()
  }
}

function handleTenantChangedEvent() {
  notices.value = []
  activeIndex.value = 0
  visible.value = false
  if (shouldRefreshPopup()) {
    schedulePopupRefresh()
  }
}

function handleVisibilityChange() {
  if (!document.hidden && shouldRefreshPopup()) {
    schedulePopupRefresh()
  }
}

function handleRouteChange(path: string) {
  if (path === PERSONAL_HOME_PATH) {
    schedulePopupRefresh()
  }
}

watch(
  () => route.path,
  (path) => handleRouteChange(path),
  { immediate: true },
)

async function loadPopupNotices() {
  if (loading.value || visible.value) {
    return
  }
  loading.value = true
  try {
    const result = await noticeApi.popupList()
    notices.value = Array.isArray(result) ? result : []
    activeIndex.value = 0
    visible.value = notices.value.length > 0
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    refreshHandler = handleSystemNoticeRefreshEvent
    visibilityHandler = handleVisibilityChange
    window.addEventListener('fx:system-notice-refresh', refreshHandler as EventListener)
    document.addEventListener('visibilitychange', visibilityHandler)
    window.addEventListener('fx:tenant-changed', handleTenantChangedEvent as EventListener)
  }
})

onBeforeUnmount(() => {
  clearRefreshTimer()
  if (typeof window !== 'undefined') {
    if (refreshHandler) {
      window.removeEventListener('fx:system-notice-refresh', refreshHandler as EventListener)
    }
    if (visibilityHandler) {
      document.removeEventListener('visibilitychange', visibilityHandler)
    }
    window.removeEventListener('fx:tenant-changed', handleTenantChangedEvent as EventListener)
  }
})

async function ackCurrent() {
  const notice = currentNotice.value
  if (notice?.id) {
    await noticeApi.ackPopup(notice.id)
  }
  if (activeIndex.value < notices.value.length - 1) {
    activeIndex.value += 1
    visible.value = true
    return
  }
  visible.value = false
  notices.value = []
  activeIndex.value = 0
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent('fx:system-notice-refresh'))
  }
}

function scopeText(scope?: NoticeScope) {
  return scope === 'PUBLIC' ? t('system.notice.popup.publicScope') : t('system.notice.popup.tenantScope')
}
</script>

<style scoped lang="less" src="@/styles/components/system/system-notice-popup.less"></style>
