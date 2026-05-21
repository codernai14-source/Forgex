<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobLogTable" :request="handleRequest" row-key="id">
      <template #status="{ record }">
        <a-tag :color="statusColor(record.status)">{{ statusText(record.status) }}</a-tag>
      </template>
      <template #action="{ record }">
        <a v-permission="'job:log:view'" @click="openDetail(record.id)">{{ t('common.detail') }}</a>
      </template>
    </FxDynamicTable>

    <BaseFormDialog v-model:open="detailVisible" :title="t('job.actions.logDetail')" :width="760" readonly>
      <a-descriptions v-if="detail" bordered :column="2" size="small">
        <a-descriptions-item :label="t('job.fields.jobCode')">{{ detail.jobCode }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.jobName')">{{ detail.jobName }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.status')">{{ statusText(detail.status) }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.durationMs')">{{ detail.durationMs }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.instanceId')">{{ detail.instanceId }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.requestId')">{{ detail.requestId }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.resultMessage')" :span="2">{{ detail.resultMessage }}</a-descriptions-item>
        <a-descriptions-item :label="t('job.fields.errorStack')" :span="2">
          <pre class="log-stack">{{ detail.errorStack }}</pre>
        </a-descriptions-item>
      </a-descriptions>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { getJobLogDetail, getJobLogPage } from '@/api/job/log'
import type { JobLog } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()
const detailVisible = ref(false)
const detail = ref<JobLog>()

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobLogPage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function openDetail(id?: number) {
  if (!id) return
  detail.value = await getJobLogDetail(id)
  detailVisible.value = true
}

function statusColor(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'red'
  if (status === 3) return 'orange'
  return 'blue'
}

function statusText(status?: number) {
  if (status === 0) return t('job.logStatus.running')
  if (status === 1) return t('job.logStatus.success')
  if (status === 2) return t('job.logStatus.failed')
  if (status === 3) return t('job.logStatus.timeout')
  return '-'
}
</script>

<style scoped lang="less" src="@/styles/views/job/log/index.less"></style>
