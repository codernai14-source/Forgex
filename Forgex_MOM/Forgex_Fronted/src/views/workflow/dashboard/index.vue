<template>
  <div class="approval-dashboard">
    <a-spin :spinning="pageLoading">
      <div class="dashboard-header">
        <h2 class="title">{{ t('workflow.dashboard.title') }}</h2>
        <p class="subtitle">{{ t('workflow.dashboard.subtitle') }}</p>
      </div>

      <a-row :gutter="[16, 16]">
        <a-col :span="24">
          <a-card class="section-card section-card-chart" :bordered="false">
            <template #title>
              <span class="card-title">
                <BarChartOutlined class="card-icon" />
                {{ t('workflow.dashboard.weeklyTrendTitle') }}
              </span>
            </template>
            <p class="section-intro">{{ t('workflow.dashboard.weeklyTrendSubtitle') }}</p>
            <div ref="weeklyChartRef" class="chart-block"></div>
          </a-card>
        </a-col>

        <a-col v-if="canStartExecution" :span="24">
          <a-card class="section-card" :bordered="false">
            <template #title>
              <span class="card-title">
                <ThunderboltOutlined class="card-icon" />
                {{ t('workflow.dashboard.shortcutTitle') }}
              </span>
            </template>
            <template #extra>
              <a-button type="link" size="small" @click="goToStartCenter">
                <template #icon><ArrowRightOutlined /></template>
                {{ t('workflow.dashboard.more') }}
              </a-button>
            </template>
            <p class="section-intro">{{ t('workflow.dashboard.shortcutSubtitle') }}</p>
            <a-empty
              v-if="!shortcutTasks.length && !shortcutLoading"
              class="section-empty"
              :description="t('workflow.dashboard.shortcutEmpty')"
            />
            <div v-else class="shortcut-grid">
              <button
                v-for="task in shortcutTasks"
                :key="task.taskCode"
                type="button"
                class="shortcut-card"
                @click="handleOpenShortcut(task)"
              >
                <div class="shortcut-card__icon" :class="`shortcut-card__icon--${getTaskAccent(task)}`">
                  <component :is="getTaskIcon(task)" />
                </div>
                <div class="shortcut-card__body">
                  <div class="shortcut-card__name">{{ task.taskName }}</div>
                  <p class="shortcut-card__remark">
                    {{ task.remark || t('workflow.dashboard.shortcutFallback') }}
                  </p>
                  <div class="shortcut-card__footer">
                    <span>{{ task.taskCode }}</span>
                    <span class="shortcut-card__action">
                      {{ t('workflow.dashboard.shortcutAction') }}
                      <ArrowRightOutlined />
                    </span>
                  </div>
                </div>
              </button>
            </div>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="canViewTaskConfig ? 14 : 24">
          <a-card class="section-card section-card-chart" :bordered="false">
            <template #title>
              <span class="card-title">
                <PieChartOutlined class="card-icon" />
                {{ t('workflow.dashboard.userShareTitle') }}
              </span>
            </template>
            <p class="section-intro">{{ t('workflow.dashboard.userShareSubtitle') }}</p>
            <div ref="shareChartRef" class="chart-block chart-block-pie"></div>
          </a-card>
        </a-col>

        <a-col v-if="canViewTaskConfig" :xs="24" :lg="10">
          <a-card class="section-card" :bordered="false">
            <template #title>
              <span class="card-title">
                <SettingOutlined class="card-icon" />
                {{ t('workflow.dashboard.taskConfigPreviewTitle') }}
              </span>
            </template>
            <template #extra>
              <a-button type="link" size="small" @click="goToTaskConfigList">
                <template #icon><ArrowRightOutlined /></template>
                {{ t('workflow.dashboard.more') }}
              </a-button>
            </template>
            <p class="section-intro">{{ t('workflow.dashboard.taskConfigPreviewSubtitle') }}</p>
            <a-empty
              v-if="!taskConfigPreview.length && !taskConfigLoading"
              class="section-empty"
              :description="t('workflow.dashboard.taskConfigEmpty')"
            />
            <ul v-else class="config-list">
              <li v-for="item in taskConfigPreview" :key="item.id" class="config-item">
                <div class="config-item__head">
                  <div class="config-item__main">
                    <div class="config-item__name">{{ item.taskName }}</div>
                    <div class="config-item__code">{{ item.taskCode }}</div>
                  </div>
                  <div class="config-item__tags">
                    <a-tag :color="item.formType === 1 ? 'blue' : 'green'">
                      {{ item.formType === 1 ? t('workflow.taskConfig.customForm') : t('workflow.taskConfig.lowCodeForm') }}
                    </a-tag>
                    <a-tag :color="item.status === 1 ? 'success' : 'default'">
                      {{ item.status === 1 ? t('workflow.dashboard.enabledText') : t('workflow.dashboard.disabledText') }}
                    </a-tag>
                  </div>
                </div>
                <div class="config-item__meta">
                  <span>{{ getVersionText(item) }}</span>
                  <span>{{ formatDateTime(item.updateTime || item.createTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-pending" :bordered="false">
            <template #title>
              <span class="card-title">
                <BellOutlined class="card-icon" />
                {{ t('workflow.dashboard.pendingTitle') }}
              </span>
            </template>
            <template #extra>
              <a-button type="link" size="small" @click="goMore('pending')">
                <template #icon><ArrowRightOutlined /></template>
                {{ t('workflow.dashboard.more') }}
              </a-button>
            </template>
            <div v-if="!summary.pending?.length" class="empty-hint">{{ t('workflow.dashboard.empty') }}</div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.pending"
                :key="'p-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag :color="getStatusColor(item.status)" size="small">{{ getStatusText(item.status) }}</a-tag>
                </div>
                <div class="task-meta">
                  <span>{{ item.initiatorName }}</span>
                  <span>{{ formatDateTime(item.startTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-done" :bordered="false">
            <template #title>
              <span class="card-title">
                <CheckCircleOutlined class="card-icon" />
                {{ t('workflow.dashboard.yesterdayTitle') }}
              </span>
            </template>
            <template #extra>
              <a-button type="link" size="small" @click="goMore('processed')">
                <template #icon><ArrowRightOutlined /></template>
                {{ t('workflow.dashboard.more') }}
              </a-button>
            </template>
            <div v-if="!summary.yesterdayProcessed?.length" class="empty-hint">{{ t('workflow.dashboard.empty') }}</div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.yesterdayProcessed"
                :key="'y-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag :color="getStatusColor(item.status)" size="small">{{ getStatusText(item.status) }}</a-tag>
                </div>
                <div class="task-meta">
                  <span>{{ item.initiatorName }}</span>
                  <span>{{ formatDateTime(item.startTime) }}</span>
                </div>
              </li>
            </ul>
          </a-card>
        </a-col>

        <a-col :xs="24" :lg="8">
          <a-card class="dash-card dash-card-cc" :bordered="false">
            <template #title>
              <span class="card-title">
                <SendOutlined class="card-icon" />
                {{ t('workflow.dashboard.ccTitle') }}
              </span>
            </template>
            <template #extra>
              <a-button type="link" size="small" @click="goMore('pending')">
                <template #icon><ArrowRightOutlined /></template>
                {{ t('workflow.dashboard.ccHint') }}
              </a-button>
            </template>
            <div v-if="!summary.cc?.length" class="empty-hint">{{ t('workflow.dashboard.empty') }}</div>
            <ul v-else class="task-list">
              <li
                v-for="item in summary.cc"
                :key="'c-' + item.id"
                class="task-item"
                @click="openDetail(item)"
              >
                <div class="task-main">
                  <span class="task-name">{{ item.taskName }}</span>
                  <a-tag color="blue" size="small">{{ t('workflow.dashboard.ccTag') }}</a-tag>
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
      :title="t('workflow.myTask.detail')"
      :width="720"
      :body-style="{ paddingBottom: '80px' }"
    >
      <template v-if="currentRecord">
        <a-descriptions bordered :column="2">
          <a-descriptions-item :label="t('workflow.execution.taskName')">
            {{ currentRecord.taskName }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.dashboard.detailTaskCode')">{{ currentRecord.taskCode }}</a-descriptions-item>
          <a-descriptions-item :label="t('workflow.dashboard.detailInitiator')">{{ currentRecord.initiatorName }}</a-descriptions-item>
          <a-descriptions-item :label="t('workflow.dashboard.detailStartTime')">{{ formatDateTime(currentRecord.startTime) }}</a-descriptions-item>
          <a-descriptions-item :label="t('workflow.dashboard.detailCurrentNode')">{{ currentRecord.currentNodeName || '-' }}</a-descriptions-item>
          <a-descriptions-item :label="t('workflow.dashboard.detailStatus')">
            <a-tag :color="getStatusColor(currentRecord.status)">{{ getStatusText(currentRecord.status) }}</a-tag>
          </a-descriptions-item>
        </a-descriptions>
        <a-divider />
        <div class="form-block">
          <h4>{{ t('workflow.taskConfig.formContent') }}</h4>
          <pre>{{ formatFormContent(currentRecord.formContent) }}</pre>
        </div>
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  AppstoreOutlined,
  ArrowRightOutlined,
  BarChartOutlined,
  BellOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  FileTextOutlined,
  PieChartOutlined,
  SendOutlined,
  SettingOutlined,
  TeamOutlined,
  ThunderboltOutlined
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import dayjs from 'dayjs'
import {
  loadDashboardAnalytics,
  loadDashboardSummary,
  type WfDashboardAnalyticsVO,
  type WfDashboardSummaryVO,
  type WfDashboardWeeklyResultDTO,
  type WfExecutionDTO
} from '@/api/workflow/execution'
import {
  getTaskConfigPage,
  listTaskConfig,
  type WfTaskConfigDTO,
  type WfTaskConfigSummaryDTO
} from '@/api/workflow/taskConfig'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import { usePermissionStore } from '@/stores/permission'
import './index.less'

const SHORTCUT_LIMIT = 5
const TASK_CONFIG_PREVIEW_LIMIT = 6
const RECENT_TASK_STORAGE_KEY = 'workflow-recent-task-codes'

const { t } = useI18n()
const router = useRouter()
const permissionStore = usePermissionStore()

const summaryLoading = ref(false)
const analyticsLoading = ref(false)
const shortcutLoading = ref(false)
const taskConfigLoading = ref(false)

const summary = reactive<WfDashboardSummaryVO>({ pending: [], yesterdayProcessed: [], cc: [] })
const analytics = reactive<WfDashboardAnalyticsVO>({ weeklyResults: createEmptyWeeklyResults(), userShares: [] })
const shortcutTasks = ref<WfTaskConfigDTO[]>([])
const taskConfigPreview = ref<WfTaskConfigSummaryDTO[]>([])
const recentTaskCodes = ref<string[]>(loadRecentTaskCodes())
const detailDrawerVisible = ref(false)
const currentRecord = ref<WfExecutionDTO | null>(null)
const weeklyChartRef = ref<HTMLDivElement | null>(null)
const shareChartRef = ref<HTMLDivElement | null>(null)

let weeklyChart: echarts.ECharts | null = null
let shareChart: echarts.ECharts | null = null
let themeObserver: MutationObserver | null = null

function hasAccessibleRoute(path: string) {
  const resolved = router.resolve(path)
  return Boolean(resolved?.matched?.length)
}

const canStartExecution = computed(() =>
  permissionStore.hasPermission('wf:execution:start') || hasAccessibleRoute(approvalRoutePaths.executionStartList),
)
const canViewTaskConfig = computed(() =>
  permissionStore.hasPermission('wf:taskConfig:view') || hasAccessibleRoute(approvalRoutePaths.taskConfigList),
)
const pageLoading = computed(() => summaryLoading.value || analyticsLoading.value || shortcutLoading.value || taskConfigLoading.value)

function createEmptyWeeklyResults(): WfDashboardWeeklyResultDTO[] {
  return Array.from({ length: 7 }, (_, index) => ({
    date: dayjs().subtract(6 - index, 'day').format('YYYY-MM-DD'),
    approvedCount: 0,
    rejectedCount: 0
  }))
}

function resolveCssVar(name: string, fallback: string) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim() || fallback
}

function loadRecentTaskCodes(): string[] {
  try {
    const rawValue = localStorage.getItem(RECENT_TASK_STORAGE_KEY)
    if (!rawValue) {
      return []
    }
    const parsed = JSON.parse(rawValue)
    return Array.isArray(parsed) ? parsed.filter(item => typeof item === 'string') : []
  } catch {
    return []
  }
}

function buildWeeklyChartOption(): EChartsOption {
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: {
      top: 0,
      right: 0,
      textStyle: { color: resolveCssVar('--fx-text-secondary', '#6b7280') },
      data: [t('workflow.dashboard.approvedLegend'), t('workflow.dashboard.rejectedLegend')]
    },
    grid: { top: 48, left: 16, right: 16, bottom: 12, containLabel: true },
    xAxis: {
      type: 'category',
      data: analytics.weeklyResults.map(item => dayjs(item.date).format('MM-DD')),
      axisLine: { lineStyle: { color: resolveCssVar('--fx-border-color', '#e5e7eb') } },
      axisLabel: { color: resolveCssVar('--fx-text-secondary', '#6b7280') },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLine: { show: false },
      axisLabel: { color: resolveCssVar('--fx-text-secondary', '#6b7280') },
      splitLine: { lineStyle: { color: resolveCssVar('--fx-border-color', '#e5e7eb'), type: 'dashed' } }
    },
    series: [
      {
        name: t('workflow.dashboard.approvedLegend'),
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#2563eb' },
            { offset: 1, color: '#60a5fa' }
          ])
        },
        data: analytics.weeklyResults.map(item => item.approvedCount)
      },
      {
        name: t('workflow.dashboard.rejectedLegend'),
        type: 'bar',
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#f97316' },
            { offset: 1, color: '#fb7185' }
          ])
        },
        data: analytics.weeklyResults.map(item => item.rejectedCount)
      }
    ]
  }
}

function buildUserShareOption(): EChartsOption {
  if (!analytics.userShares.length) {
    return {
      title: {
        text: t('workflow.dashboard.userShareEmpty'),
        left: 'center',
        top: 'center',
        textStyle: { fontSize: 14, fontWeight: 500, color: resolveCssVar('--fx-text-secondary', '#6b7280') }
      },
      series: []
    }
  }

  return {
    tooltip: { trigger: 'item' },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 0,
      top: 'middle',
      height: 220,
      textStyle: { color: resolveCssVar('--fx-text-secondary', '#6b7280') }
    },
    series: [
      {
        name: t('workflow.dashboard.userShareTitle'),
        type: 'pie',
        radius: ['48%', '72%'],
        center: ['34%', '54%'],
        label: { show: false },
        emphasis: {
          scale: true,
          label: {
            show: true,
            color: resolveCssVar('--fx-text-primary', '#111827'),
            formatter: '{b}\n{d}%'
          }
        },
        itemStyle: {
          borderColor: resolveCssVar('--fx-bg-container', '#ffffff'),
          borderWidth: 4
        },
        data: analytics.userShares.map(item => ({ name: item.initiatorName, value: item.count }))
      }
    ]
  }
}

function refreshCharts() {
  if (weeklyChartRef.value) {
    weeklyChart = weeklyChart || echarts.init(weeklyChartRef.value)
    weeklyChart.setOption(buildWeeklyChartOption(), true)
  }
  if (shareChartRef.value) {
    shareChart = shareChart || echarts.init(shareChartRef.value)
    shareChart.setOption(buildUserShareOption(), true)
  }
}

async function fetchSummary() {
  summaryLoading.value = true
  try {
    const data = await loadDashboardSummary()
    summary.pending = data?.pending ?? []
    summary.yesterdayProcessed = data?.yesterdayProcessed ?? []
    summary.cc = data?.cc ?? []
  } catch (error: any) {
    message.error(error.message || t('workflow.dashboard.loadFailed'))
    summary.pending = []
    summary.yesterdayProcessed = []
    summary.cc = []
  } finally {
    summaryLoading.value = false
  }
}

async function fetchAnalytics() {
  analyticsLoading.value = true
  try {
    const data = await loadDashboardAnalytics()
    analytics.weeklyResults = data?.weeklyResults?.length ? data.weeklyResults : createEmptyWeeklyResults()
    analytics.userShares = data?.userShares ?? []
  } catch (error: any) {
    message.error(error.message || t('workflow.dashboard.analyticsLoadFailed'))
    analytics.weeklyResults = createEmptyWeeklyResults()
    analytics.userShares = []
  } finally {
    analyticsLoading.value = false
  }
}

async function fetchShortcutTasks() {
  if (!canStartExecution.value) return
  shortcutLoading.value = true
  try {
    const records = (await listTaskConfig({ status: 1 })) || []
    shortcutTasks.value = [...records]
      .sort((left, right) => {
        const leftRecentIndex = recentTaskCodes.value.indexOf(left.taskCode)
        const rightRecentIndex = recentTaskCodes.value.indexOf(right.taskCode)
        if (leftRecentIndex !== rightRecentIndex) {
          if (leftRecentIndex === -1) return 1
          if (rightRecentIndex === -1) return -1
          return leftRecentIndex - rightRecentIndex
        }
        const leftTime = dayjs(left.updateTime || left.createTime || 0).valueOf()
        const rightTime = dayjs(right.updateTime || right.createTime || 0).valueOf()
        return rightTime - leftTime || left.taskName.localeCompare(right.taskName)
      })
      .slice(0, SHORTCUT_LIMIT)
  } catch (error: any) {
    message.error(error.message || t('workflow.dashboard.shortcutLoadFailed'))
    shortcutTasks.value = []
  } finally {
    shortcutLoading.value = false
  }
}

async function fetchTaskConfigPreview() {
  if (!canViewTaskConfig.value) return
  taskConfigLoading.value = true
  try {
    const result = await getTaskConfigPage({ pageNum: 1, pageSize: TASK_CONFIG_PREVIEW_LIMIT })
    taskConfigPreview.value = result?.records ?? []
  } catch (error: any) {
    message.error(error.message || t('workflow.dashboard.taskConfigLoadFailed'))
    taskConfigPreview.value = []
  } finally {
    taskConfigLoading.value = false
  }
}

async function loadDashboardData() {
  const loaders = [fetchSummary(), fetchAnalytics()]
  if (canStartExecution.value) loaders.push(fetchShortcutTasks())
  if (canViewTaskConfig.value) loaders.push(fetchTaskConfigPreview())
  await Promise.all(loaders)
  await nextTick()
  refreshCharts()
}

const goMore = (target: 'pending' | 'processed') => {
  router.push(target === 'pending' ? approvalRoutePaths.myPending : approvalRoutePaths.myProcessed)
}

const goToStartCenter = () => router.push(approvalRoutePaths.executionStartList)
const goToTaskConfigList = () => router.push(approvalRoutePaths.taskConfigList)
const openDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}
const handleOpenShortcut = (task: WfTaskConfigDTO) => router.push(approvalRoutePaths.executionStartForm(task.taskCode))

function getStatusColor(status?: number) {
  return ({ 0: 'default', 1: 'processing', 2: 'success', 3: 'error' } as Record<number, string>)[status ?? 0] ?? 'default'
}

function getStatusText(status?: number) {
  return ({
    0: t('workflow.dashboard.status.pending'),
    1: t('workflow.dashboard.status.processing'),
    2: t('workflow.dashboard.status.done'),
    3: t('workflow.dashboard.status.rejected')
  } as Record<number, string>)[status ?? 0] ?? '-'
}

function formatDateTime(dateTime?: string) {
  return dateTime ? dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function formatFormContent(formContent?: string) {
  if (!formContent) return '{}'
  try {
    return JSON.stringify(JSON.parse(formContent), null, 2)
  } catch {
    return formContent
  }
}

function getTaskCategory(task: Pick<WfTaskConfigDTO, 'taskCode' | 'taskName' | 'remark'>) {
  const content = `${task.taskName}${task.taskCode}${task.remark || ''}`.toLowerCase()
  if (content.includes('请假') || content.includes('leave') || content.includes('hr')) return 'hr'
  if (content.includes('合同') || content.includes('contract')) return 'contract'
  if (content.includes('财务') || content.includes('expense') || content.includes('付款')) return 'finance'
  if (content.includes('采购') || content.includes('项目')) return 'project'
  return 'general'
}

function getTaskIcon(task: Pick<WfTaskConfigDTO, 'taskCode' | 'taskName' | 'remark'>) {
  const category = getTaskCategory(task)
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO' || category === 'hr') return CalendarOutlined
  if (category === 'contract' || category === 'finance') return FileTextOutlined
  if (category === 'project') return TeamOutlined
  return AppstoreOutlined
}

function getTaskAccent(task: Pick<WfTaskConfigDTO, 'taskCode' | 'taskName' | 'remark'>) {
  if (task.taskCode === 'LEAVE_APPROVAL_DEMO') return 'orange'
  switch (getTaskCategory(task)) {
    case 'hr':
      return 'blue'
    case 'finance':
      return 'green'
    case 'contract':
      return 'pink'
    default:
      return 'violet'
  }
}

function getVersionText(record: WfTaskConfigSummaryDTO) {
  if (record.publishedVersion && record.draftVersion) {
    return `${t('workflow.dashboard.publishedVersion')} v${record.publishedVersion} / ${t('workflow.dashboard.draftVersion')} v${record.draftVersion}`
  }
  if (record.publishedVersion) return `${t('workflow.dashboard.publishedVersion')} v${record.publishedVersion}`
  if (record.draftVersion) return `${t('workflow.dashboard.draftVersion')} v${record.draftVersion}`
  return t('workflow.dashboard.unpublished')
}

function handleResize() {
  weeklyChart?.resize()
  shareChart?.resize()
}

onMounted(async () => {
  themeObserver = new MutationObserver(() => refreshCharts())
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
  await loadDashboardData()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  window.removeEventListener('resize', handleResize)
  weeklyChart?.dispose()
  shareChart?.dispose()
})
</script>
