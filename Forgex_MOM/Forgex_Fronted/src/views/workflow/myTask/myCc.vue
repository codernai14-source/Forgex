<template>
  <div v-permission="'wf:myTask:cc'" class="page-wrap">
    <a-card :bordered="false" class="query-card" :body-style="{ padding: '12px 16px' }">
      <a-form layout="inline" :model="queryForm" class="query-form">
        <a-form-item :label="t('workflow.myTask.taskName')">
          <a-input v-model:value="queryForm.taskName" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item :label="t('workflow.myTask.taskCode')">
          <a-input v-model:value="queryForm.taskCode" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item :label="t('workflow.myTask.status')">
          <a-select v-model:value="queryForm.status" allow-clear style="width: 180px">
            <a-select-option :value="1">{{ t('workflow.dashboard.status.processing') }}</a-select-option>
            <a-select-option :value="2">{{ t('workflow.myTask.historyStatus.approved') }}</a-select-option>
            <a-select-option :value="3">{{ t('workflow.myTask.historyStatus.rejected') }}</a-select-option>
            <a-select-option :value="4">{{ t('workflow.myTask.historyStatus.canceled') }}</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">{{ t('common.search') }}</a-button>
            <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card" :body-style="{ padding: '0' }">
      <div class="table-toolbar">
        <div class="table-title">{{ t('workflow.myTask.myCcTitle') }}</div>
      </div>
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="scope">
          <template v-if="scope.column.key === 'status'">
            <DictTag :value="scope.record.status" :items="executionStatusOptions" :fallback-text="getStatusText(scope.record.status)" />
          </template>
          <template v-else-if="scope.column.key === 'startTime'">
            {{ formatDateTime(scope.record.startTime) }}
          </template>
          <template v-else-if="scope.column.key === 'endTime'">
            {{ formatDateTime(scope.record.endTime) }}
          </template>
          <template v-else-if="scope.column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleViewDetail(scope.record)">
                <template #icon><EyeOutlined /></template>
                {{ t('workflow.myTask.detail') }}
              </a-button>
              <a-button type="link" size="small" @click="handleViewTrace(scope.record)">
                <template #icon><HistoryOutlined /></template>
                {{ t('workflow.myTask.trace') }}
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <WorkflowDetailDrawer
      v-model:open="detailDrawerVisible"
      :record="currentRecord"
      :instances="currentInstances"
      :action-logs="currentActionLogs"
      :show-action-logs="true"
    />

    <a-modal v-model:open="traceVisible" :title="t('workflow.myTask.historyTitle')" :width="920" :footer="null">
      <WorkflowTracePanel :record="currentRecord" :instances="currentInstances" :action-logs="currentActionLogs" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { EyeOutlined, HistoryOutlined } from '@ant-design/icons-vue'
import {
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  pageMyCc,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionDTO,
} from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import WorkflowDetailDrawer from './WorkflowDetailDrawer.vue'
import WorkflowTracePanel from './WorkflowTracePanel.vue'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')

const loading = ref(false)
const dataSource = ref<WfExecutionDTO[]>([])
const pagination = reactive({ current: 1, pageSize: 20, total: 0, showSizeChanger: true, showQuickJumper: true })
const detailDrawerVisible = ref(false)
const traceVisible = ref(false)
const currentRecord = ref<WfExecutionDTO | null>(null)
const currentInstances = ref<WfApprovalInstanceDTO[]>([])
const currentActionLogs = ref<WfApprovalActionLogDTO[]>([])
const queryForm = reactive({ taskName: '', taskCode: '', status: undefined as number | undefined })

const columns = computed(() => ([
  { title: t('workflow.myTask.taskName'), dataIndex: 'taskName', key: 'taskName', ellipsis: true, width: 180 },
  { title: t('workflow.myTask.taskCode'), dataIndex: 'taskCode', key: 'taskCode', ellipsis: true, width: 180 },
  { title: t('workflow.myTask.initiator'), dataIndex: 'initiatorName', key: 'initiatorName', width: 120 },
  { title: t('workflow.myTask.currentNode'), dataIndex: 'currentNodeName', key: 'currentNodeName', width: 140 },
  { title: t('workflow.myTask.status'), dataIndex: 'status', key: 'status', width: 120 },
  { title: t('workflow.myTask.startTime'), dataIndex: 'startTime', key: 'startTime', width: 180 },
  { title: t('workflow.myTask.endTime'), dataIndex: 'endTime', key: 'endTime', width: 180 },
  { title: t('common.actions'), key: 'action', fixed: 'right', width: 180 },
]))

function getStatusText(status?: number) {
  return getDictItemLabel(executionStatusOptions.value, status, t('workflow.myTask.unknownStatus'))
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

function buildParams() {
  const params: any = { pageNum: pagination.current, pageSize: pagination.pageSize }
  if (queryForm.taskName) params.taskName = queryForm.taskName
  if (queryForm.taskCode) params.taskCode = queryForm.taskCode
  if (queryForm.status !== undefined) params.status = queryForm.status
  return params
}

async function loadData() {
  try {
    loading.value = true
    const data = await pageMyCc(buildParams())
    dataSource.value = data.records || []
    pagination.total = Number(data.total || 0)
  } catch (error: any) {
    message.error(error?.message || t('workflow.myTask.loadCcFailed'))
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  pagination.current = 1
  await loadData()
}

async function handleReset() {
  queryForm.taskName = ''
  queryForm.taskCode = ''
  queryForm.status = undefined
  await handleSearch()
}

async function loadTrace(executionId: number) {
  const [detail, instances, logs] = await Promise.all([
    getExecutionDetail({ executionId }),
    listApprovalInstances({ executionId }),
    listApprovalActionLogs({ executionId }),
  ])
  currentRecord.value = detail || null
  currentInstances.value = instances || []
  currentActionLogs.value = logs || []
}

async function handleViewDetail(record: WfExecutionDTO) {
  await loadTrace(record.id)
  detailDrawerVisible.value = true
}

async function handleViewTrace(record: WfExecutionDTO) {
  await loadTrace(record.id)
  traceVisible.value = true
}

async function handleTableChange(pag: any) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || pagination.pageSize
  await loadData()
}

onMounted(loadData)
</script>

<style scoped lang="less">
.page-wrap {
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  box-sizing: border-box;
}

.query-card,
.table-card {
  border-radius: 8px;
}

.table-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
}

.table-title {
  font-weight: 600;
  font-size: 14px;
}
</style>
