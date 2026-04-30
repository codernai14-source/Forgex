<template>
  <div class="operation-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="OperationLogTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :scroll="{ x: 1800 }"
      :show-query-form="true"
      :dynamic-table-config="fallbackConfig"
    >
      <template #toolbar>
        <a-button data-guide-id="sys-operation-log-export" @click="handleExport" v-permission="'sys:operation-log:export'">
          <template #icon><ExportOutlined /></template>
          {{ t('common.export') }}
        </a-button>
      </template>

      <template #username="{ record }">
        <a-tag color="blue">{{ record.username || '-' }}</a-tag>
      </template>

      <template #account="{ record }">
        <span>{{ resolveAccount(record) }}</span>
      </template>

      <template #requestParams="{ record }">
        <a-space size="small">
          <a-button type="link" size="small" @click="openPayloadPreview('request', record)">查看详情</a-button>
          <a-tooltip title="复制请求参数">
            <a-button type="text" size="small" @click="copyPayload(record.requestParams)">
              <template #icon><CopyOutlined /></template>
            </a-button>
          </a-tooltip>
        </a-space>
      </template>

      <template #responseResult="{ record }">
        <a-space size="small">
          <a-button type="link" size="small" @click="openPayloadPreview('response', record)">查看详情</a-button>
          <a-tooltip title="复制响应结果">
            <a-button type="text" size="small" @click="copyPayload(record.responseResult)">
              <template #icon><CopyOutlined /></template>
            </a-button>
          </a-tooltip>
        </a-space>
      </template>

      <template #operationType="{ record }">
        <a-tag :color="getOperationTypeColor(record.operationType)">
          {{ getOperationTypeText(record.operationType) }}
        </a-tag>
      </template>

      <template #responseStatus="{ record }">
        <a-tag :color="record.responseStatus === 200 ? 'success' : 'error'">{{ record.responseStatus ?? '-' }}</a-tag>
      </template>

      <template #costTime="{ record }">
        <span :style="{ color: getCostTimeColor(record.costTime) }">{{ record.costTime }}ms</span>
      </template>

      <template #operationTime="{ record }">
        {{ formatDateTime(record.operationTime) }}
      </template>

      <template #action="{ record }">
        <a-button type="link" size="small" @click="handleViewDetail(record)">详情</a-button>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="payloadVisible" :title="payloadTitle" :width="820" :footer="null">
      <pre class="detail-json-block">{{ payloadContent }}</pre>
    </a-modal>

    <a-modal v-model:open="detailVisible" :title="t('operationLog.detailTitle', '操作日志详情')" :width="900" :footer="null">
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="用户名称">{{ currentRecord.username || '-' }}</a-descriptions-item>
        <a-descriptions-item label="账号">{{ resolveAccount(currentRecord) }}</a-descriptions-item>
        <a-descriptions-item label="操作时间">{{ formatDateTime(currentRecord.operationTime) }}</a-descriptions-item>
        <a-descriptions-item label="模块">{{ currentRecord.module || '-' }}</a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOperationTypeColor(currentRecord.operationType)">
            {{ getOperationTypeText(currentRecord.operationType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求方法">{{ currentRecord.requestMethod || '-' }}</a-descriptions-item>
        <a-descriptions-item label="响应状态">
          <a-tag :color="currentRecord.responseStatus === 200 ? 'success' : 'error'">
            {{ currentRecord.responseStatus ?? '-' }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求 URL" :span="2">
          <a-typography-text copyable>{{ currentRecord.requestUrl || '-' }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="IP 地址">{{ currentRecord.ip || '-' }}</a-descriptions-item>
        <a-descriptions-item label="耗时">
          <span :style="{ color: getCostTimeColor(currentRecord.costTime) }">{{ currentRecord.costTime }}ms</span>
        </a-descriptions-item>
        <a-descriptions-item label="操作详情" :span="2" v-if="currentRecord.detailText">
          {{ currentRecord.detailText }}
        </a-descriptions-item>
        <a-descriptions-item label="错误堆栈" :span="2" v-if="currentRecord.errorStack">
          <pre class="detail-json-block detail-json-block--error">{{ currentRecord.errorStack }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="User-Agent" :span="2">
          {{ currentRecord.userAgent || '-' }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { CopyOutlined, ExportOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import { exportOperationLog, pageOperationLog } from '@/api/operationLog'

const { t } = useI18n()

const tableRef = ref<any>()
const detailVisible = ref(false)
const payloadVisible = ref(false)
const currentRecord = ref<any>(null)
const payloadTitle = ref('')
const payloadContent = ref('-')

const dictOptions = computed(() => ({
  operationType: [
    { label: '新增', value: 'ADD' },
    { label: '新增', value: 'CREATE' },
    { label: '修改', value: 'EDIT' },
    { label: '修改', value: 'UPDATE' },
    { label: '删除', value: 'DELETE' },
    { label: '查询', value: 'QUERY' },
    { label: '导出', value: 'EXPORT' },
    { label: '导入', value: 'IMPORT' },
    { label: '登录', value: 'LOGIN' },
    { label: '登出', value: 'LOGOUT' },
  ],
}))

const fallbackConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'OperationLogTable',
  tableName: '操作日志',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'username', title: '用户名称', width: 120, align: 'left', ellipsis: true },
    { field: 'account', title: '账号', width: 140, align: 'center', ellipsis: true },
    { field: 'module', title: '模块', width: 150, align: 'left', ellipsis: true },
    { field: 'operationType', title: '操作类型', width: 100, align: 'center' },
    { field: 'requestMethod', title: '请求方法', width: 100, align: 'center' },
    { field: 'requestUrl', title: '请求 URL', width: 220, align: 'left', ellipsis: true },
    { field: 'requestParams', title: '请求参数', width: 140, align: 'center' },
    { field: 'responseStatus', title: '响应状态', width: 100, align: 'center' },
    { field: 'responseResult', title: '响应结果', width: 140, align: 'center' },
    { field: 'costTime', title: '耗时', width: 90, align: 'center' },
    { field: 'ip', title: 'IP 地址', width: 140, align: 'left' },
    { field: 'operationTime', title: '操作时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 100, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'username', label: '用户名称', queryType: 'input', queryOperator: 'like' },
    { field: 'account', label: '账号', queryType: 'input', queryOperator: 'like' },
    { field: 'module', label: '模块', queryType: 'input', queryOperator: 'like' },
    { field: 'operationTime', label: '操作时间', queryType: 'dateRange', queryOperator: 'between' },
  ],
  version: 1,
}))

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const params: any = {
    current: payload.page.current,
    size: payload.page.pageSize,
    ...payload.query,
  }
  if (Array.isArray(params.operationTime) && params.operationTime.length === 2) {
    params.startTime = params.operationTime[0]
    params.endTime = params.operationTime[1]
    delete params.operationTime
  }
  if (payload.sorter?.field) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const res: any = await pageOperationLog(params)
  return {
    records: (res.records || []).map((item: any) => ({
      ...item,
      responseStatus: item.responseStatus ?? item['response状态'],
    })),
    total: Number(res.total || 0),
  }
}

function resolveAccount(record: any) {
  return record?.account || record?.username || record?.userId || '-'
}

function openPayloadPreview(type: 'request' | 'response', record: any) {
  payloadTitle.value = type === 'request' ? '请求参数' : '响应结果'
  payloadContent.value = formatJson(type === 'request' ? record.requestParams : record.responseResult)
  payloadVisible.value = true
}

function handleViewDetail(record: any) {
  currentRecord.value = {
    ...record,
    responseStatus: record.responseStatus ?? record['response状态'],
  }
  detailVisible.value = true
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

async function handleExport() {
  try {
    const query = tableRef.value?.getQuery?.() || {}
    const params: any = { ...query }
    if (Array.isArray(params.operationTime) && params.operationTime.length === 2) {
      params.startTime = params.operationTime[0]
      params.endTime = params.operationTime[1]
      delete params.operationTime
    }
    const resp: any = await exportOperationLog({ tableCode: 'OperationLogTable', query: params })
    const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'text/csv;charset=UTF-8' })
    const url = window.URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `operation-log-${Date.now()}.csv`
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    window.URL.revokeObjectURL(url)
    message.success(t('common.exportSuccess'))
  } catch (error) {
    console.error(error)
    message.error(t('common.exportFailed'))
  }
}

function getOperationTypeColor(type: string) {
  return ({
    ADD: 'green',
    CREATE: 'green',
    EDIT: 'blue',
    UPDATE: 'blue',
    DELETE: 'red',
    QUERY: 'default',
    EXPORT: 'purple',
    IMPORT: 'orange',
    LOGIN: 'cyan',
    LOGOUT: 'default',
  } as Record<string, string>)[type] || 'default'
}

function getOperationTypeText(type: string) {
  const option = dictOptions.value.operationType.find((item: any) => item.value === type)
  return option?.label || type
}

function getCostTimeColor(costTime: number) {
  if (costTime < 100) return '#52c41a'
  if (costTime < 500) return '#faad14'
  return '#f5222d'
}

function formatDateTime(dateTime: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function formatJson(jsonStr: string) {
  if (!jsonStr) return '-'
  try {
    return JSON.stringify(JSON.parse(jsonStr), null, 2)
  } catch {
    return jsonStr
  }
}
</script>

<style scoped lang="less">
.operation-log-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.detail-json-block {
  max-height: 420px;
  margin: 0;
  overflow: auto;
  padding: 10px 12px;
  border-radius: var(--fx-radius, 6px);
  border: 1px solid var(--fx-border-color, #d9d9d9);
  background: linear-gradient(180deg, var(--fx-bg-elevated, #ffffff), var(--fx-fill-secondary, #f5f5f5));
  color: var(--fx-text-primary, #1f1f1f);
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, 'Courier New', monospace;
}

.detail-json-block--error {
  max-height: 300px;
  border-color: var(--fx-error, #cf1322);
  background: linear-gradient(180deg, var(--fx-error-bg, #fff2f0), var(--fx-fill-secondary, #f5f5f5));
  color: var(--fx-error, #cf1322);
}
</style>
