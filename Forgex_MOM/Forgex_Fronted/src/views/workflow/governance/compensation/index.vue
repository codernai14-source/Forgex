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
          :message="t('workflow.compensation.title')"
          :description="t('workflow.compensation.description')"
        />
      </template>

      <template #status="{ record }">
        <DictTag :value="record.status" :items="executionStatusOptions" :fallback-text="getStatusText(record.status)" />
      </template>

      <template #governanceTag="{ record }">
        <a-space wrap size="small">
          <a-tag v-if="record.timeoutFlag" color="orange">{{ t('workflow.compensation.tags.timeoutRetry') }}</a-tag>
          <a-tag v-if="hasInactiveCandidate(record)" color="blue">{{ t('workflow.compensation.tags.inactiveCandidate') }}</a-tag>
          <a-tag v-if="record.delegated" color="cyan">{{ t('workflow.compensation.tags.delegated') }}</a-tag>
          <a-tag v-if="record.transferred" color="purple">{{ t('workflow.compensation.tags.transferred') }}</a-tag>
        </a-space>
      </template>

      <template #startTime="{ record }">
        {{ formatDateTime(record.startTime) }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            {{ t('workflow.compensation.viewDetail') }}
          </a-button>
          <a-button type="link" size="small" @click="handleCompensate(record)">
            {{ t('workflow.compensation.compensate') }}
          </a-button>
          <a-button type="link" size="small" @click="handleRetryTimeout(record)">
            {{ t('workflow.compensation.retryTimeout') }}
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <a-drawer
      v-model:open="detailDrawerVisible"
      :title="t('workflow.compensation.detailTitle')"
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
            <span class="summary-label">{{ t('workflow.compensation.activePendingCount') }}</span>
            <span class="summary-value">{{ currentRecord.activeInstanceCount ?? 0 }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">{{ t('workflow.dashboard.timeoutFlag') }}</span>
            <span class="summary-value">{{ currentRecord.timeoutFlag ? t('common.yes') : t('common.no') }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">{{ t('workflow.compensation.delegatedHit') }}</span>
            <span class="summary-value">{{ currentRecord.delegated ? t('common.yes') : t('common.no') }}</span>
          </div>
          <div class="summary-item">
            <span class="summary-label">{{ t('workflow.compensation.transferredHit') }}</span>
            <span class="summary-value">{{ currentRecord.transferred ? t('common.yes') : t('common.no') }}</span>
          </div>
        </div>

        <div class="governance-tags">
          <a-tag v-if="currentRecord.timeoutFlag" color="orange">{{ t('workflow.compensation.detailTags.timeout') }}</a-tag>
          <a-tag v-if="hasInactiveCandidate(currentRecord)" color="blue">{{ t('workflow.compensation.detailTags.inactive') }}</a-tag>
          <a-tag v-if="currentRecord.delegated" color="cyan">{{ t('workflow.compensation.detailTags.delegated') }}</a-tag>
          <a-tag v-if="currentRecord.transferred" color="purple">{{ t('workflow.compensation.detailTags.transferred') }}</a-tag>
        </div>

        <div v-if="currentRecord.latestActionSummary" class="latest-action">
          {{ t('workflow.dashboard.latestAction', { action: currentRecord.latestActionSummary }) }}
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
    message.error(error.message || t('workflow.compensation.loadListFailed'))
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
    message.error(error.message || t('workflow.compensation.loadDetailFailed'))
  }
}

async function handleCompensate(record: WfExecutionDTO) {
  Modal.confirm({
    title: t('workflow.compensation.confirmCompensateTitle'),
    content: t('workflow.compensation.confirmCompensateContent', { taskName: record.taskName }),
    async onOk() {
      try {
        await compensateExecution({ executionId: record.id })
        message.success(t('workflow.compensation.compensateSuccess'))
        await refreshCurrentRecord(record)
      } catch (error: any) {
        message.error(error.message || t('workflow.compensation.compensateFailed'))
      }
    },
  })
}

async function handleRetryTimeout(record: WfExecutionDTO) {
  Modal.confirm({
    title: t('workflow.compensation.confirmRetryTitle'),
    content: t('workflow.compensation.confirmRetryContent', { taskName: record.taskName }),
    async onOk() {
      try {
        await retryTimeoutJobs({ executionId: record.id })
        message.success(t('workflow.compensation.retrySuccess'))
        await refreshCurrentRecord(record)
      } catch (error: any) {
        message.error(error.message || t('workflow.compensation.retryFailed'))
      }
    },
  })
}
</script>

<style scoped lang="less" src="@/styles/views/workflow/governance/compensation/index.less"></style>
