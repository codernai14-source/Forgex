<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfMyCcTaskTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #ccTime="{ record }">
        {{ formatDateTime(record.ccTime || record.startTime) }}
      </template>

      <template #status="{ record }">
        <DictTag :value="record.status" :items="executionStatusOptions" :fallback-text="getStatusText(record.status)" />
      </template>

      <template #ccUnread="{ record }">
        <span class="read-status">
          <span v-if="record.ccUnread" class="unread-dot" />
          <a-tag :color="record.ccUnread ? 'orange' : 'default'">
            {{ record.ccUnread ? t('workflow.myTask.unread') : t('workflow.myTask.read') }}
          </a-tag>
        </span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            <template #icon><EyeOutlined /></template>
            {{ t('workflow.myTask.detail') }}
          </a-button>
          <a-button type="link" size="small" @click="handleViewTrace(record)">
            <template #icon><HistoryOutlined /></template>
            {{ t('workflow.myTask.trace') }}
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

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
/**
 * 我的抄送页面。
 * <p>
 * 能进菜单即可看表；不要用 {@code v-permission} 包整页。
 * 前端按钮权限只收集 {@code type=button}，本页 {@code wf:myTask:cc} 挂在菜单上，
 * 包整页会把表格 display:none，看起来像空白页。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see pageMyCc
 * @see FxDynamicTable
 */
import { computed, ref } from 'vue'
import dayjs from 'dayjs'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { EyeOutlined, HistoryOutlined } from '@ant-design/icons-vue'
import {
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  markReadCc,
  pageMyCc,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionDTO,
  type WorkflowId,
} from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import WorkflowDetailDrawer from './WorkflowDetailDrawer.vue'
import WorkflowTracePanel from './WorkflowTracePanel.vue'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')

const tableRef = ref()
const loading = ref(false)
const detailDrawerVisible = ref(false)
const traceVisible = ref(false)
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
}) => {
  try {
    loading.value = true
    const data = await pageMyCc({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query,
    })
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    message.error(error?.message || t('workflow.myTask.loadCcFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function getStatusText(status?: number) {
  return getDictItemLabel(executionStatusOptions.value, status, t('workflow.myTask.unknownStatus'))
}

function formatDateTime(value?: string) {
  return value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
}

async function loadTrace(executionId: WorkflowId) {
  const [detail, instances, logs] = await Promise.all([
    getExecutionDetail({ executionId }),
    listApprovalInstances({ executionId }),
    listApprovalActionLogs({ executionId }),
  ])
  currentRecord.value = detail || null
  currentInstances.value = instances || []
  currentActionLogs.value = logs || []
}

async function markCurrentRead(executionId: WorkflowId) {
  try {
    await markReadCc({ executionId })
    tableRef.value?.refresh?.()
  } catch {
    // 已读失败不阻断详情查看
  }
}

async function handleViewDetail(record: WfExecutionDTO) {
  await loadTrace(record.id)
  detailDrawerVisible.value = true
  await markCurrentRead(record.id)
}

async function handleViewTrace(record: WfExecutionDTO) {
  await loadTrace(record.id)
  traceVisible.value = true
  await markCurrentRead(record.id)
}
</script>

<style scoped lang="less">
.page-wrap {
  padding: 16px;
  height: 100%;
  min-height: 0;
  box-sizing: border-box;
}

.read-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #fa8c16;
  flex-shrink: 0;
}
</style>
