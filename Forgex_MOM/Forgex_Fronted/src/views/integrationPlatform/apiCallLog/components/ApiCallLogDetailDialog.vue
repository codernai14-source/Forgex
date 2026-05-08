<template>
  <a-modal
    v-model:open="visible"
    :title="t('integration.apiCallLog.detailTitle')"
    :width="980"
    :footer="null"
    destroy-on-close
  >
    <a-spin :spinning="loading">
      <a-descriptions :column="2" bordered>
        <a-descriptions-item label="接口名称">
          {{ detail?.apiName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="接口编码">
          {{ detail?.apiCode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('integration.apiCallLog.callDirection')">
          {{ detail?.callDirection || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('integration.apiCallLog.callStatus')">
          <a-tag :color="detail?.callStatus === 'SUCCESS' ? 'success' : 'error'">
            {{ detail?.callStatus === 'SUCCESS' ? t('common.success') : t('common.failed') }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item :label="t('integration.apiCallLog.callerIp')">
          {{ detail?.callerIp || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('integration.apiCallLog.costTimeMs')">
          {{ detail?.costTimeMs ?? '-' }}ms
        </a-descriptions-item>
        <a-descriptions-item label="链路ID">
          {{ detail?.traceId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="任务ID">
          {{ detail?.taskId || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="调用模式">
          {{ detail?.invokeMode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="响应编码">
          {{ detail?.responseCode || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="结果类型">
          {{ detail?.resultType || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('integration.apiCallLog.callTime')">
          {{ formatDisplayDateTime(detail?.callTime) }}
        </a-descriptions-item>
        <a-descriptions-item v-if="detail?.errorMessage" :label="t('integration.apiCallLog.errorMessage')" :span="2">
          <pre class="json-block json-block--error">{{ detail.errorMessage }}</pre>
        </a-descriptions-item>
      </a-descriptions>

      <div class="payload-section">
        <div class="payload-header">
          <span>组装前参数</span>
          <a-button type="text" size="small" @click="copyPayload(detail?.rawRequestData)">
            复制组装前参数
          </a-button>
        </div>
        <pre class="json-block">{{ formatJson(detail?.rawRequestData) }}</pre>
      </div>

      <div class="payload-section">
        <div class="payload-header">
          <span>组装后参数</span>
          <a-button type="text" size="small" @click="copyPayload(detail?.assembledRequestData)">
            复制组装后参数
          </a-button>
        </div>
        <pre class="json-block">{{ formatJson(detail?.assembledRequestData) }}</pre>
      </div>

      <div class="payload-section">
        <div class="payload-header">
          <span>{{ t('integration.apiCallLog.responseData') }}</span>
          <a-button type="text" size="small" @click="copyPayload(detail?.responseData)">
            复制响应结果
          </a-button>
        </div>
        <pre class="json-block">{{ formatJson(detail?.responseData) }}</pre>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getApiCallLogDetail, type ApiCallLogItem } from '@/api/system/integration'

const { t } = useI18n({ useScope: 'global' })
const visible = ref(false)
const loading = ref(false)
const detail = ref<ApiCallLogItem | null>(null)

async function open(data: ApiCallLogItem) {
  visible.value = true
  loading.value = true
  detail.value = data
  try {
    detail.value = await getApiCallLogDetail(data.id, formatRequestDateTime(data.callTime))
  } catch (error) {
    console.error(error)
    message.error(t('common.getDetailFailed'))
  } finally {
    loading.value = false
  }
}

async function copyPayload(payload?: string) {
  try {
    await navigator.clipboard.writeText(payload || '')
    message.success('复制成功')
  } catch (error) {
    console.error(error)
    message.error('复制失败')
  }
}

function formatJson(value?: string) {
  if (!value) return '暂无数据'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

function formatRequestDateTime(dateTime?: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : ''
}

function formatDisplayDateTime(dateTime?: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

defineExpose({ open })
</script>

<style scoped>
.payload-section {
  margin-top: 16px;
}

.payload-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 500;
}

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
