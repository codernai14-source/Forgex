<template>
  <div class="api-call-log-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ApiCallLogTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :scroll="{ x: 1800 }"
      :show-query-form="true"
      :dynamic-table-config="fallbackConfig"
    >
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'error'">
          {{ record.status === 1 ? t('common.success') : t('common.failed') }}
        </a-tag>
      </template>

      <template #responseStatus="{ record }">
        <a-tag :color="getResponseStatusColor(record.responseStatus)">
          {{ record.responseStatus || '-' }}
        </a-tag>
      </template>

      <template #costTime="{ record }">
        <span :style="{ color: getCostTimeColor(record.costTime) }">
          {{ record.costTime ?? '-' }}ms
        </span>
      </template>

      <template #callTime="{ record }">
        {{ formatDateTime(record.callTime) }}
      </template>

      <template #action="{ record }">
        <a-button type="link" size="small" @click="handleViewDetail(record)">
          {{ t('common.detail') }}
        </a-button>
      </template>
    </FxDynamicTable>

    <ApiCallLogDetailDialog ref="detailDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { ApiCallLogItem, ApiCallLogQuery } from './types'
import ApiCallLogDetailDialog from './components/ApiCallLogDetailDialog.vue'
import { getApiCallLogList } from '@/api/system/integration'

const { t } = useI18n()
const tableRef = ref<any>()
const detailDialogRef = ref<any>()

/**
 * 字典选项
 */
const dictOptions = computed(() => ({
  status: [
    { label: t('common.success'), value: 1 },
    { label: t('common.failed'), value: 0 },
  ],
  requestMethod: [
    { label: 'GET', value: 'GET' },
    { label: 'POST', value: 'POST' },
    { label: 'PUT', value: 'PUT' },
    { label: 'DELETE', value: 'DELETE' },
    { label: 'PATCH', value: 'PATCH' },
    { label: 'HEAD', value: 'HEAD' },
    { label: 'OPTIONS', value: 'OPTIONS' },
  ],
}))

/**
 * 表格配置
 */
const fallbackConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ApiCallLogTable',
  tableName: t('integration.apiCallLog.tableName', 'API 调用记录'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'interfaceName', title: t('integration.apiCallLog.interfaceName', '接口名称'), width: 200, align: 'left', ellipsis: true },
    { field: 'interfaceCode', title: t('integration.apiCallLog.interfaceCode', '接口编码'), width: 180, align: 'left', ellipsis: true },
    { field: 'requestMethod', title: t('integration.apiCallLog.requestMethod', '请求方法'), width: 120, align: 'center' },
    { field: 'requestUrl', title: t('integration.apiCallLog.requestUrl', '请求 URL'), width: 250, align: 'left', ellipsis: true },
    { field: 'callerSystemName', title: t('integration.apiCallLog.callerSystem', '调用系统'), width: 150, align: 'left', ellipsis: true },
    { field: 'status', title: t('integration.apiCallLog.callStatus', '调用状态'), width: 100, align: 'center' },
    { field: 'responseStatus', title: t('integration.apiCallLog.responseStatus', '响应状态'), width: 120, align: 'center' },
    { field: 'costTime', title: t('integration.apiCallLog.costTime', '耗时'), width: 100, align: 'center' },
    { field: 'ip', title: t('integration.apiCallLog.ip', 'IP 地址'), width: 140, align: 'left' },
    { field: 'callTime', title: t('integration.apiCallLog.callTime', '调用时间'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action', '操作'), width: 100, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'interfaceName', label: t('integration.apiCallLog.interfaceName', '接口名称'), queryType: 'input', queryOperator: 'like' },
    { field: 'interfaceCode', label: t('integration.apiCallLog.interfaceCode', '接口编码'), queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: t('integration.apiCallLog.callStatus', '调用状态'), queryType: 'select', queryOperator: 'eq' },
    { field: 'callTime', label: t('integration.apiCallLog.callTime', '调用时间'), queryType: 'dateRange', queryOperator: 'between' },
  ],
  version: 1,
}))

/**
 * 处理表格数据请求
 * 
 * @param payload - 请求参数
 * @returns 分页数据
 */
const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const params: ApiCallLogQuery = {
    current: payload.page.current,
    size: payload.page.pageSize,
    ...payload.query,
  }

  // 处理时间范围
  if (Array.isArray(params.callTime) && params.callTime.length === 2) {
    params.startTime = params.callTime[0]
    params.endTime = params.callTime[1]
    delete params.callTime
  }

  // 处理排序
  if (payload.sorter?.field) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const res: any = await getApiCallLogList(params)
  return {
    records: (res.records || []) as ApiCallLogItem[],
    total: typeof res.total === 'number' ? res.total : parseInt(String(res.total || '0'), 10),
  }
}

/**
 * 查看详情
 * 
 * @param record - 记录
 */
const handleViewDetail = (record: ApiCallLogItem) => {
  detailDialogRef.value?.open(record)
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
 * 格式化日期时间
 * 
 * @param dateTime - 日期时间字符串
 * @returns 格式化后的日期时间字符串
 */
const formatDateTime = (dateTime?: string) => {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}
</script>

<style scoped lang="less">
.api-call-log-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
