<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfCompensationCenterTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #toolbar>
        <a-alert
          type="info"
          show-icon
          message="补偿中心"
          description="集中处理未激活待办实例与超时待办实例，作为审批流三期治理能力的运营入口。"
        />
      </template>

      <template #status="{ record }">
        <DictTag :value="record.status" :items="executionStatusOptions" :fallback-text="getStatusText(record.status)" />
      </template>

      <template #governanceTag="{ record }">
        <a-space wrap size="small">
          <a-tag v-if="record.timeoutFlag" color="orange">超时待重试</a-tag>
          <a-tag v-if="hasInactiveCandidate(record)" color="blue">未激活待补偿</a-tag>
          <a-tag v-if="record.delegated" color="cyan">命中委托</a-tag>
          <a-tag v-if="record.transferred" color="purple">命中转交</a-tag>
        </a-space>
      </template>

      <template #startTime="{ record }">
        {{ formatDateTime(record.startTime) }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            查看详情
          </a-button>
          <a-button type="link" size="small" @click="handleCompensate(record)">
            补偿激活
          </a-button>
          <a-button type="link" size="small" @click="handleRetryTimeout(record)">
            重试超时
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <a-drawer
      v-model:open="detailDrawerVisible"
      title="补偿详情"
      :width="920"
      :body-style="{ paddingBottom: '80px' }"
    >
      <template v-if="currentRecord">
        <a-descriptions bordered :column="2">
          <a-descriptions-item :label="t('workflow.myTask.taskName')">
            {{ currentRecord.taskName }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.taskCode')">
            {{ currentRecord.taskCode }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.initiator')">
            {{ currentRecord.initiatorName }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.startTime')">
            {{ formatDateTime(currentRecord.startTime) }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.currentNode')">
            {{ currentRecord.currentNodeName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.status')">
            <DictTag
              :value="currentRecord.status"
              :items="executionStatusOptions"
              :fallback-text="getStatusText(currentRecord.status)"
            />
          </a-descriptions-item>
        </a-descriptions>

        <a-divider />

        <div class="summary-grid">
          <div class="summary-item">
            <span class="summary-label">激活待办数</span>
            <span class="summary-value">{{ currentRecord.activeInstanceCount ?? 0 }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">超时标记</span>
            <span class="summary-value">{{ currentRecord.timeoutFlag ? '是' : '否' }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">命中委托</span>
            <span class="summary-value">{{ currentRecord.delegated ? '是' : '否' }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">命中转交</span>
            <span class="summary-value">{{ currentRecord.transferred ? '是' : '否' }}</span>
          </div>
        </div>

        <div class="governance-tags">
          <a-tag v-if="currentRecord.timeoutFlag" color="orange">当前执行单存在超时实例</a-tag>
          <a-tag v-if="hasInactiveCandidate(currentRecord)" color="blue">当前执行单存在未激活待办实例</a-tag>
          <a-tag v-if="currentRecord.delegated" color="cyan">轨迹中存在委托动作</a-tag>
          <a-tag v-if="currentRecord.transferred" color="purple">轨迹中存在转交动作</a-tag>
        </div>

        <div v-if="currentRecord.latestActionSummary" class="latest-action">
          最近动作：{{ currentRecord.latestActionSummary }}
        </div>

        <a-divider />

        <div class="form-content-detail">
          <h4>{{ t('workflow.myTask.formContent') }}</h4>
          <pre>{{ formatFormContent(currentRecord.formContent) }}</pre>
        </div>

        <a-divider />

        <WorkflowTracePanel :instances="currentInstances" :action-logs="currentActionLogs" />
      </template>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * 审批流补偿中心页。
 * 1. 承接未激活实例补偿与超时重试两类治理动作。
 * 2. 复用执行单详情、实例轨迹和动作日志展示能力。
 * 3. 为三期运营治理提供统一人工处理入口。
 */
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import dayjs from 'dayjs'
import {
  compensateExecution,
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  pageCompensationCenter,
  retryTimeoutJobs,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionDTO,
} from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import WorkflowTracePanel from '../../myTask/WorkflowTracePanel.vue'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')

const tableRef = ref()
const detailDrawerVisible = ref(false)
const currentRecord = ref<WfExecutionDTO | null>(null)
const currentInstances = ref<WfApprovalInstanceDTO[]>([])
const currentActionLogs = ref<WfApprovalActionLogDTO[]>([])

const dictOptions = computed(() => ({
  status: executionStatusOptions.value,
  wf_execution_status: executionStatusOptions.value,
}))

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query,
    }

    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }

    const data = await pageCompensationCenter(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    message.error(error.message || '加载补偿中心列表失败')
    return { records: [], total: 0 }
  }
}

function getStatusText(status?: number): string {
  return getDictItemLabel(executionStatusOptions.value, status, t('workflow.myTask.unknownStatus'))
}

function formatDateTime(dateTime?: string): string {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

function formatFormContent(formContent?: string): string {
  if (!formContent) return '{}'
  try {
    return JSON.stringify(JSON.parse(formContent), null, 2)
  } catch {
    return formContent
  }
}

function hasInactiveCandidate(record: WfExecutionDTO): boolean {
  return !record.activeInstanceCount || record.activeInstanceCount <= 0
}

async function loadExecutionDetail(executionId: number) {
  const [detail, instances, logs] = await Promise.all([
    getExecutionDetail({ executionId }),
    listApprovalInstances({ executionId }),
    listApprovalActionLogs({ executionId }),
  ])
  currentRecord.value = detail || null
  currentInstances.value = instances || []
  currentActionLogs.value = logs || []
}

async function refreshCurrentRecord(record: WfExecutionDTO) {
  await tableRef.value?.refresh?.()
  if (detailDrawerVisible.value && currentRecord.value?.id === record.id) {
    await loadExecutionDetail(record.id)
  }
}

async function handleViewDetail(record: WfExecutionDTO) {
  try {
    await loadExecutionDetail(record.id)
    detailDrawerVisible.value = true
  } catch (error: any) {
    message.error(error.message || '加载补偿详情失败')
  }
}

async function handleCompensate(record: WfExecutionDTO) {
  Modal.confirm({
    title: '确认执行补偿激活',
    content: `将尝试激活执行单【${record.taskName}】的首个未激活待办实例。`,
    async onOk() {
      try {
        await compensateExecution({ executionId: record.id })
        message.success('补偿激活已执行')
        await refreshCurrentRecord(record)
      } catch (error: any) {
        message.error(error.message || '补偿激活失败')
      }
    },
  })
}

async function handleRetryTimeout(record: WfExecutionDTO) {
  Modal.confirm({
    title: '确认执行超时重试',
    content: `将尝试处理执行单【${record.taskName}】下所有已超时实例。`,
    async onOk() {
      try {
        await retryTimeoutJobs({ executionId: record.id })
        message.success('超时重试已执行')
        await refreshCurrentRecord(record)
      } catch (error: any) {
        message.error(error.message || '超时重试失败')
      }
    },
  })
}
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.summary-item {
  padding: 12px 16px;
  border-radius: 10px;
  background: #f7f8fa;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-label {
  color: #666;
  font-size: 13px;
}

.summary-value {
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.governance-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 16px;
}

.latest-action {
  margin-top: 12px;
  color: #1f2937;
  font-size: 14px;
}

.form-content-detail {
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.form-content-detail h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
}

.form-content-detail pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  line-height: 1.5;
}
</style>
