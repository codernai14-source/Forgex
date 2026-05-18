<template>
  <div class="job-dashboard-page">
    <a-row :gutter="[12, 12]">
      <a-col v-for="item in cards" :key="item.key" :xs="24" :sm="12" :lg="8" :xl="4">
        <a-card :bordered="false" class="job-stat-card">
          <div class="stat-label">{{ item.label }}</div>
          <div class="stat-value">{{ item.value }}</div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[12, 12]" class="job-dashboard-row">
      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" :title="t('job.dashboard.trend')">
          <a-table :columns="trendColumns" :data-source="trend" :pagination="false" row-key="time" size="small" />
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card :bordered="false" :title="t('job.dashboard.recentFailures')">
          <a-table :columns="failureColumns" :data-source="failures" :pagination="false" row-key="id" size="small" />
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { getJobDashboardSummary, getJobDashboardTrend, getJobRecentFailures } from '@/api/job/dashboard'
import type { JobDashboardSummary, JobLog, JobTrend } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const summary = ref<JobDashboardSummary>({})
const trend = ref<JobTrend[]>([])
const failures = ref<JobLog[]>([])

const cards = computed(() => [
  { key: 'totalTasks', label: t('job.dashboard.totalTaskCount'), value: summary.value.totalTasks ?? 0 },
  { key: 'enabledTasks', label: t('job.dashboard.enabledTaskCount'), value: summary.value.enabledTasks ?? 0 },
  { key: 'onlineInstances', label: t('job.dashboard.runningInstanceCount'), value: summary.value.onlineInstances ?? 0 },
  { key: 'successExecutions', label: t('job.dashboard.todaySuccessCount'), value: summary.value.successExecutions ?? 0 },
  { key: 'failedExecutions', label: t('job.dashboard.todayFailCount'), value: summary.value.failedExecutions ?? 0 },
  { key: 'timeoutExecutions', label: t('job.dashboard.waitingRetryCount'), value: summary.value.timeoutExecutions ?? 0 },
])

const trendColumns = computed(() => [
  { title: t('job.fields.date'), dataIndex: 'time', key: 'time' },
  { title: t('job.fields.successCount'), dataIndex: 'success', key: 'success' },
  { title: t('job.fields.failCount'), dataIndex: 'failed', key: 'failed' },
  { title: t('job.fields.timeoutCount'), dataIndex: 'timeout', key: 'timeout' },
])

const failureColumns = computed(() => [
  { title: t('job.fields.jobCode'), dataIndex: 'jobCode', key: 'jobCode' },
  { title: t('job.fields.jobName'), dataIndex: 'jobName', key: 'jobName' },
  { title: t('job.fields.resultMessage'), dataIndex: 'resultMessage', key: 'resultMessage', ellipsis: true },
  { title: t('job.fields.endTime'), dataIndex: 'endTime', key: 'endTime' },
])

onMounted(async () => {
  const [summaryRes, trendRes, failureRes] = await Promise.all([
    getJobDashboardSummary(),
    getJobDashboardTrend(),
    getJobRecentFailures(),
  ])
  summary.value = summaryRes || {}
  trend.value = trendRes || []
  failures.value = failureRes || []
})
</script>

<style scoped lang="less" src="@/styles/views/job/dashboard/index.less"></style>
