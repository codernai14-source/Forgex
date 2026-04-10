<template>
  <div class="operation-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="OperationLogTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :scroll="{ x: 1800 }"
      :show-query-form="true"
      :fallback-config="fallbackConfig"
    >
      <template #toolbar>
        <a-button @click="handleExport" v-permission="'sys:operation-log:export'">
          <template #icon><ExportOutlined /></template>
          {{ t('common.export') }}
        </a-button>
      </template>

      <template #username="{ record }">
        <a-tag color="blue">
          {{ record.username || '-' }}
        </a-tag>
      </template>

      <template #userId="{ record }">
        <span>{{ record.userId || '-' }}</span>
      </template>

      <template #requestParams="{ record }">
        <a-typography-paragraph
          :copyable="{ text: record.requestParams }"
          :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }"
          style="max-width: 300px; margin-bottom: 0;"
        >
          {{ formatJsonPreview(record.requestParams) }}
        </a-typography-paragraph>
      </template>

      <template #responseResult="{ record }">
        <a-typography-paragraph
          :copyable="{ text: record.responseResult }"
          :ellipsis="{ rows: 2, expandable: true, symbol: '展开' }"
          style="max-width: 300px; margin-bottom: 0;"
        >
          {{ formatJsonPreview(record.responseResult) }}
        </a-typography-paragraph>
      </template>

      <template #operationType="{ record }">
        <a-tag :color="getOperationTypeColor(record.operationType)">
          {{ getOperationTypeText(record.operationType) }}
        </a-tag>
      </template>

      <template #responseStatus="{ record }">
        <a-tag :color="record.responseStatus === 200 ? 'success' : 'error'">
          {{ record.responseStatus }}
        </a-tag>
      </template>

      <template #costTime="{ record }">
        <span :style="{ color: getCostTimeColor(record.costTime) }">
          {{ record.costTime }}ms
        </span>
      </template>

      <template #operationTime="{ record }">
        {{ formatDateTime(record.operationTime) }}
      </template>

      <template #action="{ record }">
        <a-button type="link" size="small" @click="handleViewDetail(record)">
          详情
        </a-button>
      </template>
    </FxDynamicTable>

    <!-- 详情弹窗 -->
    <a-modal
      v-model:open="detailVisible"
      :title="t('operationLog.detailTitle', '操作日志详情')"
      :width="900"
      :footer="null"
    >
      <a-descriptions :column="2" bordered v-if="currentRecord">
        <a-descriptions-item label="用户名">
          {{ currentRecord.username }}
        </a-descriptions-item>
        <a-descriptions-item label="操作时间">
          {{ formatDateTime(currentRecord.operationTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="模块名称">
          {{ currentRecord.module }}
        </a-descriptions-item>
        <a-descriptions-item label="操作类型">
          <a-tag :color="getOperationTypeColor(currentRecord.operationType)">
            {{ getOperationTypeText(currentRecord.operationType) }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求方法">
          {{ currentRecord.requestMethod }}
        </a-descriptions-item>
        <a-descriptions-item label="响应状态">
          <a-tag :color="currentRecord.responseStatus === 200 ? 'success' : 'error'">
            {{ currentRecord.responseStatus }}
          </a-tag>
        </a-descriptions-item>
        <a-descriptions-item label="请求 URL" :span="2">
          <a-typography-text copyable>{{ currentRecord.requestUrl }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="IP 地址">
          {{ currentRecord.ip }}
        </a-descriptions-item>
        <a-descriptions-item label="耗时">
          <span :style="{ color: getCostTimeColor(currentRecord.costTime) }">
            {{ currentRecord.costTime }}ms
          </span>
        </a-descriptions-item>
        <a-descriptions-item label="请求参数" :span="2">
          <pre style="max-height: 200px; overflow: auto; background: #f5f5f5; padding: 8px; border-radius: 4px;">{{ formatJson(currentRecord.requestParams) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="响应结果" :span="2" v-if="currentRecord.responseResult">
          <pre style="max-height: 200px; overflow: auto; background: #f5f5f5; padding: 8px; border-radius: 4px;">{{ formatJson(currentRecord.responseResult) }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="错误堆栈" :span="2" v-if="currentRecord.errorStack">
          <pre style="max-height: 300px; overflow: auto; background: #fff2f0; padding: 8px; border-radius: 4px; color: #cf1322;">{{ currentRecord.errorStack }}</pre>
        </a-descriptions-item>
        <a-descriptions-item label="操作详情" :span="2" v-if="currentRecord.detailText">
          {{ currentRecord.detailText }}
        </a-descriptions-item>
        <a-descriptions-item label="User-Agent" :span="2">
          {{ currentRecord.userAgent }}
        </a-descriptions-item>
      </a-descriptions>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 操作日志管理页面
 * 
 * 功能：
 * 1. 操作日志列表查询（分页、搜索）
 * 2. 支持按用户名、账号、模块、操作时间搜索
 * 3. 显示接口传参、接口返回、账号信息
 * 4. 导出操作日志
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  ExportOutlined
} from '@ant-design/icons-vue'
import { pageOperationLog, exportOperationLog } from '@/api/operationLog'
import dayjs from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'

const { t } = useI18n()
const tableRef = ref<any>()

const dictOptions = computed(() => ({
  operationType: [
    { label: t('common.create'), value: 'CREATE' },
    { label: t('common.update'), value: 'UPDATE' },
    { label: t('common.delete'), value: 'DELETE' },
    { label: t('common.search'), value: 'QUERY' },
    { label: t('common.export'), value: 'EXPORT' },
    { label: t('common.import'), value: 'IMPORT' },
    { label: t('common.login.login'), value: 'LOGIN' },
    { label: t('common.logout', '登出'), value: 'LOGOUT' },
  ],
}))

/**
 * 回退配置（当数据库中没有配置时使用）
 */
const fallbackConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'OperationLogTable',
  tableName: '操作日志',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'username', title: '用户名', width: 120, align: 'left', ellipsis: true },
    { field: 'userId', title: '账号', width: 100, align: 'center' },
    { field: 'module', title: '模块', width: 150, align: 'left', ellipsis: true },
    { field: 'operationType', title: '操作类型', width: 100, align: 'center' },
    { field: 'requestMethod', title: '请求方法', width: 100, align: 'center' },
    { field: 'requestUrl', title: '请求 URL', width: 200, align: 'left', ellipsis: true },
    { field: 'requestParams', title: '请求参数', width: 200, align: 'left', ellipsis: true },
    { field: 'responseStatus', title: '响应状态', width: 100, align: 'center' },
    { field: 'responseResult', title: '响应结果', width: 200, align: 'left', ellipsis: true },
    { field: 'costTime', title: '耗时', width: 90, align: 'center' },
    { field: 'ip', title: 'IP 地址', width: 140, align: 'left' },
    { field: 'operationTime', title: '操作时间', width: 180, align: 'center' },
    { field: 'action', title: '操作', width: 100, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'username', label: '用户名', queryType: 'input', queryOperator: 'like' },
    { field: 'userId', label: '账号', queryType: 'input', queryOperator: 'eq' },
    { field: 'module', label: '模块', queryType: 'input', queryOperator: 'like' },
    { field: 'operationTime', label: '操作时间', queryType: 'dateRange', queryOperator: 'between' },
  ],
  version: 1,
}))

// 详情弹窗
const detailVisible = ref(false)
const currentRecord = ref<any>(null)

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
  const total = typeof res.total === 'number' ? res.total : parseInt(String(res.total) || '0', 10)
  return { records: res.records || [], total }
}

// 查看详情
const handleViewDetail = (record: any) => {
  currentRecord.value = record
  detailVisible.value = true
}

// 导出
const handleExport = async () => {
  try {
    const query = tableRef.value?.getQuery?.() || {}
    const params: any = { ...query }

    if (Array.isArray(params.operationTime) && params.operationTime.length === 2) {
      params.startTime = params.operationTime[0]
      params.endTime = params.operationTime[1]
      delete params.operationTime
    }

    const resp: any = await exportOperationLog({
      tableCode: 'OperationLogTable',
      query: params
    })
    const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'text/csv;charset=UTF-8' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `operation-log-${Date.now()}.csv`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    message.success(t('common.exportSuccess'))
  } catch (error) {
    message.error(t('common.exportFailed'))
    console.error(error)
  }
}

// 获取操作类型颜色
const getOperationTypeColor = (type: string) => {
  const colorMap: Record<string, string> = {
    CREATE: 'green',
    UPDATE: 'blue',
    DELETE: 'red',
    QUERY: 'default',
    EXPORT: 'purple',
    IMPORT: 'orange',
    LOGIN: 'cyan',
    LOGOUT: 'default'
  }
  return colorMap[type] || 'default'
}

// 获取操作类型文本（使用 dictOptions 的翻译）
const getOperationTypeText = (type: string) => {
  const option = dictOptions.value.operationType.find((item: any) => item.value === type)
  return option ? option.label : type
}

// 获取耗时颜色
const getCostTimeColor = (costTime: number) => {
  if (costTime < 100) return '#52c41a'
  if (costTime < 500) return '#faad14'
  return '#f5222d'
}

// 格式化日期时间
const formatDateTime = (dateTime: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

// 格式化 JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// 格式化 JSON 预览（截取前 100 个字符）
const formatJsonPreview = (jsonStr: string) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    const formatted = JSON.stringify(obj, null, 2)
    return formatted.length > 100 ? formatted.substring(0, 100) + '...' : formatted
  } catch (e) {
    return jsonStr.length > 100 ? jsonStr.substring(0, 100) + '...' : jsonStr
  }
}
</script>

<style scoped lang="less">
/**
 * 操作日志页面样式
 */
.operation-log-container {
  /* 移除 padding: 16px（现在由 MainLayout 的 .fx-content-inner 统一处理） */
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>

