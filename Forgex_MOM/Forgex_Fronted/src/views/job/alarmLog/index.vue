<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobAlarmLogTable" :request="handleRequest" row-key="id">
      <template #sendStatus="{ record }">
        <a-tag :color="record.sendStatus === 1 ? 'green' : 'red'">
          {{ record.sendStatus === 1 ? t('job.status.sent') : t('job.status.failed') }}
        </a-tag>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getJobAlarmLogPage } from '@/api/job/alarm'
import { useI18n } from 'vue-i18n'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobAlarmLogPage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}
</script>

<style scoped lang="less" src="@/styles/views/job/alarmLog/index.less"></style>
