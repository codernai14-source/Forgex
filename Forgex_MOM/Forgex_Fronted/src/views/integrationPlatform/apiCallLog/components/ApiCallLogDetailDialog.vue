<template>
  <a-modal
    v-model:open="visible"
    :title="t('integration.apiCallLog.detailTitle', 'API 调用详情')"
    :width="1000"
    :footer="null"
    destroy-on-close
  >
    <a-descriptions :column="2" bordered class="detail-descriptions">
      <a-descriptions-item :label="t('integration.apiCallLog.interfaceName', '接口名称')">
        {{ detail?.interfaceName || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.interfaceCode', '接口编码')">
        {{ detail?.interfaceCode || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.requestMethod', '请求方法')" :span="2">
        <a-tag color="blue">{{ detail?.requestMethod || '-' }}</a-tag>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.requestUrl', '请求 URL')" :span="2">
        <a-typography-text copyable :copyable="{ text: detail?.requestUrl }">
          {{ detail?.requestUrl || '-' }}
        </a-typography-text>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callerSystem', '调用系统')">
        {{ detail?.callerSystemName || detail?.callerSystemCode || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callStatus', '调用状态')">
        <a-tag :color="detail?.status === 1 ? 'success' : 'error'">
          {{ detail?.status === 1 ? t('common.success') : t('common.failed') }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.responseStatus', '响应状态')">
        <a-tag :color="getResponseStatusColor(detail?.responseStatus)">
          {{ detail?.responseStatus || '-' }}
        </a-tag>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.costTime', '耗时')">
        <span :style="{ color: getCostTimeColor(detail?.costTime) }">
          {{ detail?.costTime ?? '-' }}ms
        </span>
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.ip', 'IP 地址')">
        {{ detail?.ip || '-' }}
      </a-descriptions-item>
      <a-descriptions-item :label="t('integration.apiCallLog.callTime', '调用时间')">
        {{ formatDateTime(detail?.callTime) }}
      </a-descriptions-item>
      <a-descriptions-item
        v-if="detail?.errorMessage"
        :label="t('integration.apiCallLog.errorMessage', '错误信息')"
        :span="2"
      >
        <pre class="error-message-block">{{ detail?.errorMessage }}</pre>
      </a-descriptions-item>
    </a-descriptions>

    <a-divider orientation="left">{{ t('integration.apiCallLog.requestData', '请求数据') }}</a-divider>
    <a-card :bordered="false" class="json-card">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.requestHeaders', '请求头') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.requestHeaders)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block">{{ formatJson(detail?.requestHeaders) }}</pre>
    </a-card>

    <a-card :bordered="false" class="json-card">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.requestParams', '请求参数') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.requestParams)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block">{{ formatJson(detail?.requestParams) }}</pre>
    </a-card>

    <a-card v-if="detail?.requestBody" :bordered="false" class="json-card">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.requestBody', '请求体') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.requestBody)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block">{{ formatJson(detail?.requestBody) }}</pre>
    </a-card>

    <a-divider orientation="left">{{ t('integration.apiCallLog.responseData', '响应数据') }}</a-divider>
    <a-card :bordered="false" class="json-card">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.responseHeaders', '响应头') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.responseHeaders)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block">{{ formatJson(detail?.responseHeaders) }}</pre>
    </a-card>

    <a-card :bordered="false" class="json-card">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.responseBody', '响应体') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.responseBody)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block">{{ formatJson(detail?.responseBody) }}</pre>
    </a-card>

    <a-card v-if="detail?.errorStack" :bordered="false" class="json-card json-card--error">
      <template #title>
        <a-space>
          <span>{{ t('integration.apiCallLog.errorStack', '错误堆栈') }}</span>
          <a-button type="link" size="small" @click="copyToClipboard(detail?.errorStack)">
            {{ t('common.copy') }}
          </a-button>
        </a-space>
      </template>
      <pre class="json-block json-block--error">{{ detail?.errorStack }}</pre>
    </a-card>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import type { ApiCallLogDetail } from '../types'

const { t } = useI18n()

/**
 * 弹窗可见性
 */
const visible = ref(false)

/**
 * 详情数据
 */
const detail = ref<ApiCallLogDetail | null>(null)

/**
 * 打开弹窗并设置详情数据
 * 
 * @param data - API 调用记录详情
 */
const open = (data: ApiCallLogDetail) => {
  detail.value = data
  visible.value = true
}

/**
 * 关闭弹窗
 */
const close = () => {
  visible.value = false
  detail.value = null
}

/**
 * 格式化 JSON 字符串
 * 
 * @param jsonStr - JSON 字符串
 * @returns 格式化后的 JSON 字符串
 */
const formatJson = (jsonStr?: string) => {
  if (!jsonStr) return '-'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}

/**
 * 格式化日期时间
 * 
 * @param dateTime - 日期时间字符串
 * @returns 格式化后的日期时间字符串
 */
const formatDateTime = (dateTime?: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

/**
 * 获取响应状态码颜色
 * 
 * @param status - 响应状态码
 * @returns 颜色值
 */
const getResponseStatusColor = (status?: number) => {
  if (!status) return 'default'
  if (status >= 200 && status < 300) return 'success'
  if (status >= 300 && status < 400) return 'blue'
  if (status >= 400 && status < 500) return 'orange'
  return 'error'
}

/**
 * 获取耗时颜色
 * 
 * @param costTime - 耗时（毫秒）
 * @returns 颜色值
 */
const getCostTimeColor = (costTime?: number) => {
  if (costTime === undefined || costTime === null) return 'inherit'
  if (costTime < 100) return '#52c41a'
  if (costTime < 500) return '#faad14'
  return '#f5222d'
}

/**
 * 复制到剪贴板
 * 
 * @param text - 要复制的文本
 */
const copyToClipboard = async (text?: string) => {
  if (!text) {
    message.warning(t('common.noDataToCopy', '没有可复制的内容'))
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    message.success(t('common.copySuccess'))
  } catch {
    message.error(t('common.copyFailed'))
  }
}

defineExpose({
  open,
  close,
})
</script>

<style scoped lang="less">
.detail-descriptions {
  margin-bottom: 16px;
}

.json-card {
  margin-bottom: 16px;
  
  :deep(.ant-card-head) {
    padding: 8px 16px;
    min-height: auto;
    border-bottom: 1px solid var(--fx-border-color, #d9d9d9);
  }

  :deep(.ant-card-body) {
    padding: 12px;
  }

  &--error {
    :deep(.ant-card-head) {
      background: linear-gradient(180deg, var(--fx-error-bg, #fff2f0), var(--fx-fill-secondary, #f5f5f5));
      border-color: var(--fx-error, #cf1322);
    }
  }
}

.json-block {
  max-height: 300px;
  margin: 0;
  overflow: auto;
  padding: 12px;
  border-radius: var(--fx-radius, 6px);
  border: 1px solid var(--fx-border-color, #d9d9d9);
  background: linear-gradient(180deg, var(--fx-bg-elevated, #ffffff), var(--fx-fill-secondary, #f5f5f5));
  color: var(--fx-text-primary, #1f1f1f);
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 12px;
  line-height: 1.5;

  &--error {
    border-color: var(--fx-error, #cf1322);
    background: linear-gradient(180deg, var(--fx-error-bg, #fff2f0), var(--fx-fill-secondary, #f5f5f5));
    color: var(--fx-error, #cf1322);
  }
}

.error-message-block {
  max-height: 200px;
  margin: 0;
  overflow: auto;
  padding: 10px 12px;
  border-radius: var(--fx-radius, 6px);
  border: 1px solid var(--fx-error, #cf1322);
  background: linear-gradient(180deg, var(--fx-error-bg, #fff2f0), var(--fx-fill-secondary, #f5f5f5));
  color: var(--fx-error, #cf1322);
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, 'Courier New', monospace;
  font-size: 12px;
}
</style>
