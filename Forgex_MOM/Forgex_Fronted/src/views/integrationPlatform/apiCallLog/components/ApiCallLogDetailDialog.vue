<template>
  <a-modal
    v-model:open="visible"
    :title="t('integration.apiCallLog.detailTitle')"
    :width="980"
    :footer="null"
    destroy-on-close
  >
    <a-descriptions :column="2" bordered>
      <a-descriptions-item :label="t('integration.apiCallLog.apiConfigId')">
        {{ detail?.apiConfigId || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callDirection')">
        {{ detail?.callDirection || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callerIp')">
        {{ detail?.callerIp || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callStatus')">
        <a-tag :color="detail?.callStatus === 'SUCCESS' ? 'success' : 'error'">
          {{ detail?.callStatus === 'SUCCESS' ? t('common.success') : t('common.failed') }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.costTimeMs')">
        {{ detail?.costTimeMs ?? '-' }}ms
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callTime')">
        {{ formatDateTime(detail?.callTime) }}
      </a-descriptions-item>
      <a-descriptions-item v-if="detail?.errorMessage" :label="t('integration.apiCallLog.errorMessage')" :span="2">
        <pre class="json-block json-block--error">{{ detail.errorMessage }}</pre>
      </a-descriptions-item>
    </a-descriptions>

    <a-divider orientation="left">{{ t('integration.apiCallLog.requestData') }}</a-divider>
    <pre class="json-block">{{ formatJson(detail?.requestData) }}</pre>

    <a-divider orientation="left">{{ t('integration.apiCallLog.responseData') }}</a-divider>
    <pre class="json-block">{{ formatJson(detail?.responseData) }}</pre>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import type { ApiCallLogItem } from '@/api/system/integration'

const { t } = useI18n({ useScope: 'global' })
const visible = ref(false)
const detail = ref<ApiCallLogItem | null>(null)

function open(data: ApiCallLogItem) {
  detail.value = data
  visible.value = true
}

function formatJson(value?: string) {
  if (!value) return '-'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function formatDateTime(dateTime?: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

defineExpose({ open })
</script>

<style scoped>
.json-block {
  max-height: 320px;
  margin: 0;
  overflow: auto;
  padding: 12px;
  border: 1px solid var(--fx-border-color, #d9d9d9);
  border-radius: 8px;
  background: var(--fx-fill-secondary, #f8fafc);
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 12px;
}

.json-block--error {
  color: #cf1322;
  background: #fff2f0;
  border-color: #ffa39e;
}
</style>
