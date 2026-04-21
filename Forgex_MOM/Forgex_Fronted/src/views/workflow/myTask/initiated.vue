<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfInitiatedTaskTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #status="{ record }">
        <DictTag :value="record.status" :items="executionStatusOptions" :fallback-text="getStatusText(record.status)" />
      </template>

      <template #startTime="{ record }">
        {{ formatDateTime(record.startTime) }}
      </template>

      <template #endTime="{ record }">
        {{ record.endTime ? formatDateTime(record.endTime) : '-' }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            <template #icon><EyeOutlined /></template>
            {{ $t('workflow.myTask.detail') }}
          </a-button>
          <a-button type="link" size="small" @click="handleViewHistory(record)">
            <template #icon><HistoryOutlined /></template>
            {{ $t('workflow.myTask.history') }}
          </a-button>
          <a-popconfirm
            v-if="record.status === 0 || record.status === 1"
            :title="t('workflow.myTask.confirmCancel')"
            :ok-text="$t('common.confirm')"
            :cancel-text="$t('common.cancel')"
            @confirm="handleCancel(record)"
          >
            <a-button
              type="link"
              size="small"
              danger
              v-permission="'wf:execution:cancel'"
            >
              <template #icon><StopOutlined /></template>
              {{ $t('workflow.myTask.cancelApproval') }}
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </fx-dynamic-table>

    <a-drawer
      v-model:open="detailDrawerVisible"
      :title="t('workflow.myTask.detailTitle')"
      :width="800"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item :label="t('workflow.myTask.taskName')">
          {{ currentRecord?.taskName }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('workflow.myTask.taskCode')">
          {{ currentRecord?.taskCode }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('workflow.myTask.initiator')">
          {{ currentRecord?.initiatorName }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('workflow.myTask.startTime')">
          {{ formatDateTime(currentRecord?.startTime) }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('workflow.myTask.currentNode')">
          {{ currentRecord?.currentNodeName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item :label="t('workflow.myTask.status')">
          <DictTag :value="currentRecord?.status" :items="executionStatusOptions" :fallback-text="getStatusText(currentRecord?.status)" />
        </a-descriptions-item>
        <a-descriptions-item v-if="currentRecord?.endTime" :label="t('workflow.myTask.endTime')">
          {{ formatDateTime(currentRecord.endTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>{{ t('workflow.myTask.formContent') }}</h4>
        <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
      </div>

      <a-divider />

      <WorkflowTracePanel :instances="currentInstances" :show-action-logs="false" />
    </a-drawer>

    <a-modal
      v-model:open="historyModalVisible"
      :title="t('workflow.myTask.historyTitle')"
      :width="900"
      :footer="null"
    >
      <WorkflowTracePanel :instances="[]" :action-logs="currentActionLogs" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { EyeOutlined, HistoryOutlined, StopOutlined } from '@ant-design/icons-vue'
import {
  cancelExecution,
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  pageMyInitiated,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionDTO,
} from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import WorkflowTracePanel from './WorkflowTracePanel.vue'
import dayjs from 'dayjs'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')

const tableRef = ref()
const loading = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const currentInstances = ref<WfApprovalInstanceDTO[]>([])
const currentActionLogs = ref<WfApprovalActionLogDTO[]>([])
const detailDrawerVisible = ref(false)
const historyModalVisible = ref(false)

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
    loading.value = true
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query,
    }

    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }

    const data = await pageMyInitiated(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.loadInitiatedFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
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

function handleViewDetail(record: WfExecutionDTO) {
  currentRecord.value = record
  detailDrawerVisible.value = true
  loadExecutionTrace(record.id)
}

async function handleViewHistory(record: WfExecutionDTO) {
  currentRecord.value = record
  historyModalVisible.value = true
  await loadExecutionTrace(record.id)
}

async function loadExecutionTrace(executionId: number) {
  try {
    const [detail, instances, logs] = await Promise.all([
      getExecutionDetail({ executionId }),
      listApprovalInstances({ executionId }),
      listApprovalActionLogs({ executionId }),
    ])
    currentRecord.value = detail || currentRecord.value
    currentInstances.value = instances || []
    currentActionLogs.value = logs || []
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.loadHistoryFailed'))
  }
}

async function handleCancel(record: WfExecutionDTO) {
  try {
    await cancelExecution({ executionId: record.id })
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.cancelFailed'))
  }
}

onMounted(() => {
  tableRef.value?.refresh?.()
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.form-content-detail {
  margin-top: 16px;
  padding: 16px;
  background: var(--fx-fill-secondary, #f5f5f5);
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: var(--fx-radius, 8px);
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
}

.form-content-detail pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  line-height: 1.5;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
}

.form-content-detail h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
}
</style>
