<template>
  <div class="approval-dashboard">
    <a-spin :spinning="loading">
      <div class="dashboard-header">
        <h2 class="title">{{ $t('workflow.dashboard.title') }}</h2>
        <p class="subtitle">{{ $t('workflow.dashboard.subtitle') }}</p>
      </div>

      <a-row :gutter="[16, 16]">
        <!-- 待我处理 -->
        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-pending" :bordered="false">
            <template #title>
              <span class="card-title">
                <BellOutlined class="card-icon" />
                {{ $t('workflow.dashboard.pendingTitle') }}
              </span>
            </template>
            <template #extra>
              <a @click="goMore('pending')">{{ $t('workflow.dashboard.more') }}</a>
            </template>
            <div v-if="!summary.pending?.length" class="empty-hint">
              {{ $t('workflow.dashboard.empty') }}
            </div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.pending"
                :key="'p-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag :color="getStatusColor(item.status)" size="small">
                    {{ getStatusText(item.status) }}
                  </a-tag>
                </div>
                <div class="task-meta">
                  <span>{{ item.initiatorName }}</span>
                  <span>{{ formatDateTime(item.startTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>

        <!-- 昨日已处理 -->
        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-done" :bordered="false">
            <template #title>
              <span class="card-title">
                <CheckCircleOutlined class="card-icon" />
                {{ $t('workflow.dashboard.yesterdayTitle') }}
              </span>
            </template>
            <template #extra>
              <a @click="goMore('processed')">{{ $t('workflow.dashboard.more') }}</a>
            </template>
            <div v-if="!summary.yesterdayProcessed?.length" class="empty-hint">
              {{ $t('workflow.dashboard.empty') }}
            </div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.yesterdayProcessed"
                :key="'y-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag :color="getStatusColor(item.status)" size="small">
                    {{ getStatusText(item.status) }}
                  </a-tag>
                </div>
                <div class="task-meta">
                  <span>{{ item.initiatorName }}</span>
                  <span>{{ formatDateTime(item.startTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>

        <!-- 抄送我的 -->
        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-cc" :bordered="false">
            <template #title>
              <span class="card-title">
                <SendOutlined class="card-icon" />
                {{ $t('workflow.dashboard.ccTitle') }}
              </span>
            </template>
            <template #extra>
              <a @click="goMore('pending')">{{ $t('workflow.dashboard.ccHint') }}</a>
            </template>
            <div v-if="!summary.cc?.length" class="empty-hint">
              {{ $t('workflow.dashboard.empty') }}
            </div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.cc"
                :key="'c-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag color="blue" size="small">{{ $t('workflow.dashboard.ccTag') }}</a-tag>
                </div>
                <div class="task-meta">
                  <span>{{ item.initiatorName }}</span>
                  <span>{{ formatDateTime(item.startTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>
      </a-row>
    </a-spin>

    <a-drawer
      v-model:open="detailDrawerVisible"
      :title="$t('workflow.myTask.detail')"
      :width="720"
      :body-style="{ paddingBottom: '80px' }"
    >
      <template v-if="currentRecord">
        <a-descriptions bordered :column="2">
          <a-descriptions-item :label="$t('workflow.execution.taskName')">
            {{ currentRecord.taskName }}
          </a-descriptions-item>
          <a-descriptions-item label="任务编码">
            {{ currentRecord.taskCode }}
          </a-descriptions-item>
          <a-descriptions-item label="发起人">
            {{ currentRecord.initiatorName }}
          </a-descriptions-item>
          <a-descriptions-item label="发起时间">
            {{ formatDateTime(currentRecord.startTime) }}
          </a-descriptions-item>
          <a-descriptions-item label="当前节点">
            {{ currentRecord.currentNodeName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag :color="getStatusColor(currentRecord.status)">
              {{ getStatusText(currentRecord.status) }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <a-divider />
        <div class="form-block">
          <h4>{{ $t('workflow.taskConfig.formContent') }}</h4>
          <pre>{{ formatFormContent(currentRecord.formContent) }}</pre>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  BellOutlined,
  CheckCircleOutlined,
  SendOutlined
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { loadDashboardSummary, type WfExecutionDTO, type WfDashboardSummaryVO } from '@/api/workflow/execution'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'

const { t } = useI18n()
const router = useRouter()

const loading = ref(false)
const summary = reactive<WfDashboardSummaryVO>({
  pending: [],
  yesterdayProcessed: [],
  cc: []
})

const detailDrawerVisible = ref(false)
const currentRecord = ref<WfExecutionDTO | null>(null)

const fetchSummary = async () => {
  loading.value = true
  try {
    const data = await loadDashboardSummary()
    summary.pending = data?.pending ?? []
    summary.yesterdayProcessed = data?.yesterdayProcessed ?? []
    summary.cc = data?.cc ?? []
  } catch (e: unknown) {
    const err = e as { message?: string }
    message.error(err?.message || t('workflow.dashboard.loadFailed'))
    summary.pending = []
    summary.yesterdayProcessed = []
    summary.cc = []
  } finally {
    loading.value = false
  }
}

const goMore = (target: 'pending' | 'processed') => {
  if (target === 'pending') {
    router.push(approvalRoutePaths.myPending)
  } else {
    router.push(approvalRoutePaths.myProcessed)
  }
}

const openDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

const getStatusColor = (status?: number): string => {
  const colorMap: Record<number, string> = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'error'
  }
  return colorMap[status ?? 0] ?? 'default'
}

const getStatusText = (status?: number): string => {
  const key = status ?? 0
  const map: Record<number, string> = {
    0: t('workflow.dashboard.status.pending'),
    1: t('workflow.dashboard.status.processing'),
    2: t('workflow.dashboard.status.done'),
    3: t('workflow.dashboard.status.rejected')
  }
  return map[key] ?? '-'
}

const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

const formatFormContent = (formContent?: string): string => {
  if (!formContent) return '{}'
  try {
    const obj = JSON.parse(formContent)
    return JSON.stringify(obj, null, 2)
  } catch {
    return formContent
  }
}

onMounted(() => {
  fetchSummary()
})
</script>

<style scoped>
.approval-dashboard {
  padding: 16px;
  min-height: 100%;
  box-sizing: border-box;
  background: linear-gradient(180deg, #f6f8fc 0%, #f0f2f5 100%);
}

.dashboard-header {
  margin-bottom: 20px;
}

.title {
  margin: 0 0 8px 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2937;
}

.subtitle {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.dash-card {
  border-radius: 12px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
  min-height: 320px;
}

.dash-card-pending {
  border-top: 3px solid #2563eb;
}

.dash-card-done {
  border-top: 3px solid #059669;
}

.dash-card-cc {
  border-top: 3px solid #7c3aed;
}

.card-title {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.card-icon {
  font-size: 18px;
}

.task-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.task-item {
  padding: 12px 10px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s;
  border-bottom: 1px solid #f0f0f0;
}

.task-item:last-child {
  border-bottom: none;
}

.task-item:hover {
  background: #f8fafc;
}

.task-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}

.task-name {
  font-weight: 500;
  color: #111827;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #6b7280;
}

.empty-hint {
  color: #9ca3af;
  text-align: center;
  padding: 32px 8px;
  font-size: 14px;
}

.form-block {
  margin-top: 8px;
}

.form-block h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
}

.form-block pre {
  margin: 0;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
  white-space: pre-wrap;
  word-break: break-all;
  font-size: 12px;
}
</style>
