<template>
  <div class="api-call-log-page">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'ApiCallLogTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      :show-query-form="true"
      row-key="id"
    >
      <template #callDirection="{ record }">
        <a-tag :color="record.callDirection === 'INBOUND' ? 'blue' : 'cyan'">
          {{ record.callDirection === 'INBOUND' ? t('integration.common.inbound') : t('integration.common.outbound') }}
        </a-tag>
      </template>

      <template #callStatus="{ record }">
        <a-tag :color="record.callStatus === 'SUCCESS' ? 'success' : 'error'">
          {{ record.callStatus === 'SUCCESS' ? t('common.success') : t('common.failed') }}
        </a-tag>
      </template>

      <template #costTimeMs="{ record }">
        <span :style="{ color: getCostTimeColor(record.costTimeMs) }">
          {{ record.costTimeMs ?? '-' }}ms
        </span>
      </template>

      <template #callTime="{ record }">
        {{ formatDateTime(record.callTime) }}
      </template>

      <template #action="{ record }">
        <a @click="handleViewDetail(record)">{{ t('common.detail') }}</a>
      </template>
    </fx-dynamic-table>

    <ApiCallLogDetailDialog ref="detailDialogRef" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ApiCallLogDetailDialog from './components/ApiCallLogDetailDialog.vue'
import { getApiCallLogList } from '@/api/system/integration'
import type { ApiCallLogItem, IntegrationDirection } from '@/api/system/integration'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const detailDialogRef = ref<InstanceType<typeof ApiCallLogDetailDialog>>()

const dictOptions = {
  integrationDirection: [
    { label: t('integration.common.inbound'), value: 'INBOUND' },
    { label: t('integration.common.outbound'), value: 'OUTBOUND' },
  ],
  callStatus: [
    { label: t('common.success'), value: 'SUCCESS' },
    { label: t('common.failed'), value: 'FAIL' },
  ],
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const query = { ...payload.query }
  const callTimeRange = Array.isArray(query.callTime) ? query.callTime : []
  const result = await getApiCallLogList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    apiConfigId: query.apiConfigId ? Number(query.apiConfigId) : undefined,
    callDirection: (query.callDirection || undefined) as IntegrationDirection | undefined,
    callStatus: query.callStatus || undefined,
    callerIp: query.callerIp || undefined,
    startTime: callTimeRange[0] ? dayjs(callTimeRange[0]).format('YYYY-MM-DD HH:mm:ss') : undefined,
    endTime: callTimeRange[1] ? dayjs(callTimeRange[1]).format('YYYY-MM-DD HH:mm:ss') : undefined,
  })

  return {
    records: result.records || [],
    total: Number(result.total || 0),
  }
}

function handleViewDetail(record: ApiCallLogItem) {
  detailDialogRef.value?.open(record)
}

function getCostTimeColor(costTime?: number) {
  if (costTime === undefined || costTime === null) return 'inherit'
  if (costTime < 100) return '#52c41a'
  if (costTime < 500) return '#faad14'
  return '#f5222d'
}

function formatDateTime(dateTime?: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}
</script>

<style scoped lang="less">
.api-call-log-page {
  min-height: 0;
}
</style>
