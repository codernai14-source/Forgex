<template>
  <div class="operation-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="OperationLogTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :scroll="{ x: 1500 }"
    >
      <template #toolbar>
        <a-button @click="handleExport" v-permission="'sys:operation-log:export'">
          <template #icon><ExportOutlined /></template>
          导出
        </a-button>
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
      title="操作日志详情"
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
        <a-descriptions-item label="请求URL" :span="2">
          <a-typography-text copyable>{{ currentRecord.requestUrl }}</a-typography-text>
        </a-descriptions-item>
        <a-descriptions-item label="IP地址">
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
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  ExportOutlined
} from '@ant-design/icons-vue'
import { pageOperationLog, exportOperationLog } from '@/api/operationLog'
import dayjs from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

const tableRef = ref<any>()

const dictOptions = {
  operationType: [
    { label: '新增', value: 'CREATE' },
    { label: '修改', value: 'UPDATE' },
    { label: '删除', value: 'DELETE' },
    { label: '查询', value: 'QUERY' },
    { label: '导出', value: 'EXPORT' },
    { label: '导入', value: 'IMPORT' },
    { label: '登录', value: 'LOGIN' },
    { label: '登出', value: 'LOGOUT' },
  ],
}

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

    await exportOperationLog(params)
    message.success('导出成功')
  } catch (error) {
    message.error('导出失败')
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

// 获取操作类型文本
const getOperationTypeText = (type: string) => {
  const textMap: Record<string, string> = {
    CREATE: '新增',
    UPDATE: '修改',
    DELETE: '删除',
    QUERY: '查询',
    EXPORT: '导出',
    IMPORT: '导入',
    LOGIN: '登录',
    LOGOUT: '登出'
  }
  return textMap[type] || type
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

// 格式化JSON
const formatJson = (jsonStr: string) => {
  if (!jsonStr) return '-'
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}
</script>

<style scoped lang="less">
.operation-log-container {
  padding: 16px;
}
</style>

