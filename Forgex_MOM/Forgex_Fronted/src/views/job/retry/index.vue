<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobRetryTable" :request="handleRequest" row-key="id">
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'blue' : record.status === 2 ? 'red' : 'green'">
          {{ retryStatusText(record.status) }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'job:retry:handle'" @click="handleRetry(record.id, 1)">{{ t('job.actions.retryNow') }}</a>
          <a v-permission="'job:retry:handle'" class="danger-link" @click="handleRetry(record.id, 2)">{{ t('job.actions.deadLetter') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getJobRetryPage, handleJobRetry } from '@/api/job/retry'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobRetryPage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function handleRetry(id?: number, action = 1) {
  if (!id) return
  await handleJobRetry(id, action)
  tableRef.value?.refresh?.()
}

function retryStatusText(status?: number) {
  if (status === 1) return t('job.retryStatus.waiting')
  if (status === 2) return t('job.retryStatus.dead')
  if (status === 3) return t('job.retryStatus.handled')
  return '-'
}
</script>

<style scoped lang="less" src="@/styles/views/job/retry/index.less"></style>
