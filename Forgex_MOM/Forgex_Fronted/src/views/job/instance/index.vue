<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobInstanceTable" :request="handleRequest" row-key="id">
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'default'">
          {{ record.status === 1 ? t('job.status.online') : t('job.status.offline') }}
        </a-tag>
      </template>
      <template #maintenance="{ record }">
        <a-tag :color="record.maintenance === 1 ? 'orange' : 'green'">
          {{ record.maintenance === 1 ? t('job.status.maintenance') : t('job.status.normal') }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a v-permission="'job:instance:maintenance'" @click="handleMaintenance(record)">
          {{ record.maintenance === 1 ? t('job.actions.exitMaintenance') : t('job.actions.enterMaintenance') }}
        </a>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { changeJobInstanceMaintenance, getJobInstancePage } from '@/api/job/instance'
import type { JobInstance } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobInstancePage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function handleMaintenance(record: JobInstance) {
  if (!record.id) return
  await changeJobInstanceMaintenance(record.id, record.maintenance === 1 ? 0 : 1)
  tableRef.value?.refresh?.()
}
</script>

<style scoped lang="less" src="@/styles/views/job/instance/index.less"></style>
