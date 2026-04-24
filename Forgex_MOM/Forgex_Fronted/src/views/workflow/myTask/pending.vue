<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfPendingTaskTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="rowSelection"
      row-key="id"
      :show-query-form="true"
    >
      <template #toolbar>
        <a-space>
          <a-button
            type="primary"
            :disabled="!selectedExecutionIds.length"
            @click="handleBatchApprove"
            v-permission="'wf:execution:approve'"
          >
            批量同意
          </a-button>
          <a-button
            :disabled="!selectedExecutionIds.length"
            @click="openBatchTransferDialog"
            v-permission="'wf:execution:transfer'"
          >
            批量转交
          </a-button>
          <a-button
            :disabled="!selectedExecutionIds.length"
            @click="handleBatchRemind"
            v-permission="'wf:execution:remind'"
          >
            批量催办
          </a-button>
        </a-space>
      </template>

      <template #status="{ record }">
        <DictTag :value="record.status" :items="executionStatusOptions" :fallback-text="getStatusText(record.status)" />
      </template>

      <template #startTime="{ record }">
        {{ formatDateTime(record.startTime) }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="handleApprove(record)"
            v-permission="'wf:execution:approve'"
          >
            <template #icon><CheckOutlined /></template>
            {{ $t('workflow.myTask.approve') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="handleReject(record)"
            v-permission="'wf:execution:reject'"
          >
            <template #icon><CloseOutlined /></template>
            {{ $t('workflow.myTask.reject') }}
          </a-button>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            <template #icon><EyeOutlined /></template>
            {{ $t('workflow.myTask.detail') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="openTransferDialog(record)"
            v-permission="'wf:execution:transfer'"
          >
            转交
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="openAddSignDialog(record)"
            v-permission="'wf:execution:addSign'"
          >
            加签
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <BaseFormDialog
      v-model:open="approveDialogVisible"
      :title="approveAction === 'approve' ? $t('workflow.myTask.approveAgree') : $t('workflow.myTask.approveReject')"
      :loading="approving"
      :width="600"
      @submit="handleApproveSubmit"
      @cancel="handleApproveCancel"
    >
      <a-form
        ref="approveFormRef"
        :model="approveFormData"
        :rules="approveRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item :label="t('workflow.myTask.taskName')">
          <a-input :value="currentRecord?.taskName" disabled />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.initiator')">
          <a-input :value="currentRecord?.initiatorName" disabled />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.startTime')">
          <a-input :value="formatDateTime(currentRecord?.startTime)" disabled />
        </a-form-item>

        <a-form-item
          v-if="approveAction === 'reject'"
          :label="t('workflow.myTask.rejectType')"
          name="rejectType"
        >
          <a-select
            v-model:value="approveFormData.rejectType"
            :placeholder="t('workflow.myTask.rejectTypePlaceholder')"
          >
            <a-select-option
              v-for="item in rejectTypeSelectOptions"
              :key="String(item.value)"
              :value="item.value"
            >
              {{ item.label }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.comment')" name="comment">
          <a-textarea
            v-model:value="approveFormData.comment"
            :placeholder="t('workflow.myTask.commentPlaceholder')"
            :rows="4"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.formContent')">
          <div class="form-content">
            <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
          </div>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <BaseFormDialog
      v-model:open="actionDialogVisible"
      :title="actionDialogTitle"
      :loading="actionSubmitting"
      :width="620"
      @submit="handleActionSubmit"
      @cancel="handleActionCancel"
    >
      <a-form
        ref="actionFormRef"
        :model="actionFormData"
        :rules="actionRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="处理范围">
          <a-input :value="actionScopeText" disabled />
        </a-form-item>

        <a-form-item label="接收人" name="receiverIds">
          <ReceiverSelector v-model="actionReceiverModel" />
        </a-form-item>

        <a-form-item label="说明" name="comment">
          <a-textarea
            v-model:value="actionFormData.comment"
            :rows="4"
            placeholder="请输入处理说明"
          />
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <WorkflowDetailDrawer
      v-model:open="detailDrawerVisible"
      :record="currentRecord"
      :instances="currentInstances"
      :action-logs="currentActionLogs"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message, type TableProps } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'
import {
  addSign,
  approve,
  batchApprove,
  batchRemind,
  batchTransfer,
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  pageMyPending,
  reject,
  transfer,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionAddSignParam,
  type WfExecutionApproveParam,
  type WfExecutionBatchApproveParam,
  type WfExecutionBatchTransferParam,
  type WfExecutionDTO,
  type WfExecutionRemindParam,
  type WfExecutionTransferParam,
} from '@/api/workflow/execution'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import { useUserStore } from '@/stores/user'
import WorkflowDetailDrawer from './WorkflowDetailDrawer.vue'
import dayjs from 'dayjs'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')
const { dictItems: rejectTypeOptions } = useDict('wf_reject_type')
const userStore = useUserStore()

const tableRef = ref()
const approveFormRef = ref()
const actionFormRef = ref()
const loading = ref(false)
const approving = ref(false)
const actionSubmitting = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const currentInstances = ref<WfApprovalInstanceDTO[]>([])
const currentActionLogs = ref<WfApprovalActionLogDTO[]>([])
const approveDialogVisible = ref(false)
const detailDrawerVisible = ref(false)
const approveAction = ref<'approve' | 'reject'>('approve')
const actionDialogVisible = ref(false)
const actionMode = ref<'transfer' | 'addSign' | 'batchTransfer'>('transfer')
const selectedExecutionIds = ref<number[]>([])
const actionRecord = ref<WfExecutionDTO | null>(null)
const actionInstance = ref<WfApprovalInstanceDTO | null>(null)

const approveFormData = reactive<WfExecutionApproveParam>({
  executionId: 0,
  approveStatus: 1,
  comment: '',
  rejectType: undefined,
})

const actionFormData = reactive<{
  executionIds: number[]
  comment: string
}>({
  executionIds: [],
  comment: '',
})

const actionReceiverModel = ref<{
  receiverType?: string
  receiverIds: string[]
}>({
  receiverType: 'USER',
  receiverIds: [],
})

const approveRules = computed(() => ({
  comment: [{ required: true, message: t('workflow.myTask.commentPlaceholder'), trigger: 'blur' }],
  rejectType: [{ required: true, message: t('workflow.myTask.rejectTypePlaceholder'), trigger: 'change' }],
}))

const actionRules = computed(() => ({
  receiverIds: [
    {
      validator: async () => {
        if (actionReceiverModel.value.receiverType !== 'USER') {
          throw new Error('请选择指定用户作为接收人')
        }
        if (!actionReceiverModel.value.receiverIds.length) {
          throw new Error('请选择接收人')
        }
      },
      trigger: 'change',
    },
  ],
}))

const dictOptions = computed(() => ({
  status: executionStatusOptions.value,
  wf_execution_status: executionStatusOptions.value,
  rejectType: rejectTypeOptions.value,
  wf_reject_type: rejectTypeOptions.value,
}))

const rejectTypeSelectOptions = computed(() =>
  (rejectTypeOptions.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)

const rowSelection = computed<TableProps['rowSelection']>(() => ({
  selectedRowKeys: selectedExecutionIds.value,
  onChange: (keys: (string | number)[]) => {
    selectedExecutionIds.value = keys.map((key) => Number(key))
  },
}))

const actionDialogTitle = computed(() => {
  const titleMap: Record<'transfer' | 'addSign' | 'batchTransfer', string> = {
    transfer: '转交审批',
    addSign: '加签审批人',
    batchTransfer: '批量转交',
  }
  return titleMap[actionMode.value]
})

const actionScopeText = computed(() => {
  if (actionMode.value === 'batchTransfer') {
    return `已选 ${selectedExecutionIds.value.length} 条待办`
  }
  return actionRecord.value?.taskName || '-'
})

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

    const data = await pageMyPending(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.loadPendingFailed'))
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

function handleApprove(record: WfExecutionDTO) {
  currentRecord.value = record
  approveAction.value = 'approve'
  approveFormData.executionId = record.id
  approveFormData.approveStatus = 1
  approveFormData.comment = ''
  approveFormData.rejectType = undefined
  approveDialogVisible.value = true
}

function handleReject(record: WfExecutionDTO) {
  currentRecord.value = record
  approveAction.value = 'reject'
  approveFormData.executionId = record.id
  approveFormData.approveStatus = 2
  approveFormData.comment = ''
  approveFormData.rejectType = undefined
  approveDialogVisible.value = true
}

function handleViewDetail(record: WfExecutionDTO) {
  currentRecord.value = record
  detailDrawerVisible.value = true
  loadExecutionTrace(record.id)
}

function resolveCurrentUserId(): number | undefined {
  return userStore.userInfo?.id ? Number(userStore.userInfo.id) : undefined
}

function findCurrentUserInstance(record: WfExecutionDTO): WfApprovalInstanceDTO | undefined {
  const currentUserId = resolveCurrentUserId()
  if (!currentUserId) {
    return undefined
  }
  return (record.currentApprovalInstances || []).find((item) =>
    item.approverId === currentUserId &&
    item.status === 0 &&
    item.activated !== false,
  )
}

function resetActionDialog() {
  actionRecord.value = null
  actionInstance.value = null
  actionFormData.executionIds = []
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
}

function openTransferDialog(record: WfExecutionDTO) {
  const instance = findCurrentUserInstance(record)
  if (!instance) {
    message.warning('当前待办未找到可转交的审批实例，请刷新后重试')
    return
  }
  actionMode.value = 'transfer'
  actionRecord.value = record
  actionInstance.value = instance
  actionFormData.executionIds = [record.id]
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
  actionDialogVisible.value = true
}

function openAddSignDialog(record: WfExecutionDTO) {
  const instance = findCurrentUserInstance(record)
  if (!instance) {
    message.warning('当前待办未找到可加签的审批实例，请刷新后重试')
    return
  }
  actionMode.value = 'addSign'
  actionRecord.value = record
  actionInstance.value = instance
  actionFormData.executionIds = [record.id]
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
  actionDialogVisible.value = true
}

function openBatchTransferDialog() {
  if (!selectedExecutionIds.value.length) {
    message.warning('请先选择待办')
    return
  }
  actionMode.value = 'batchTransfer'
  actionRecord.value = null
  actionInstance.value = null
  actionFormData.executionIds = [...selectedExecutionIds.value]
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
  actionDialogVisible.value = true
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

async function handleApproveSubmit() {
  try {
    await approveFormRef.value?.validate()
    approving.value = true

    const params: WfExecutionApproveParam = {
      executionId: approveFormData.executionId,
      approveStatus: approveFormData.approveStatus,
      comment: approveFormData.comment,
    }

    if (approveAction.value === 'reject') {
      params.rejectType = approveFormData.rejectType
    }

    if (approveAction.value === 'approve') {
      await approve(params)
    } else {
      await reject(params)
    }

    approveDialogVisible.value = false
    approveFormRef.value?.resetFields()
    currentRecord.value = null
    currentInstances.value = []
    currentActionLogs.value = []
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || t('workflow.myTask.approveActionFailed'))
  } finally {
    approving.value = false
  }
}

async function handleActionSubmit() {
  try {
    await actionFormRef.value?.validate()
    const targetApproverId = Number(actionReceiverModel.value.receiverIds[0])
    if (!targetApproverId) {
      message.warning('请选择接收人')
      return
    }

    actionSubmitting.value = true

    if (actionMode.value === 'transfer') {
      if (!actionRecord.value || !actionInstance.value) {
        message.warning('当前转交数据不完整，请关闭后重试')
        return
      }
      const params: WfExecutionTransferParam = {
        executionId: actionRecord.value.id,
        approvalInstanceId: actionInstance.value.id,
        targetApproverId,
        comment: actionFormData.comment,
      }
      await transfer(params)
    } else if (actionMode.value === 'addSign') {
      if (!actionRecord.value || !actionInstance.value) {
        message.warning('当前加签数据不完整，请关闭后重试')
        return
      }
      const params: WfExecutionAddSignParam = {
        executionId: actionRecord.value.id,
        approvalInstanceId: actionInstance.value.id,
        targetApproverId,
        comment: actionFormData.comment,
      }
      await addSign(params)
    } else {
      const params: WfExecutionBatchTransferParam = {
        executionIds: [...actionFormData.executionIds],
        targetApproverId,
        comment: actionFormData.comment,
      }
      await batchTransfer(params)
    }

    message.success('处理成功')
    actionDialogVisible.value = false
    resetActionDialog()
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || '处理失败')
  } finally {
    actionSubmitting.value = false
  }
}

function handleApproveCancel() {
  approveDialogVisible.value = false
  approveFormRef.value?.resetFields()
}

function handleActionCancel() {
  actionDialogVisible.value = false
  actionFormRef.value?.resetFields()
  resetActionDialog()
}

function clearSelection() {
  selectedExecutionIds.value = []
}

async function handleBatchApprove() {
  if (!selectedExecutionIds.value.length) {
    message.warning('请先选择待办')
    return
  }
  try {
    const params: WfExecutionBatchApproveParam = {
      executionIds: [...selectedExecutionIds.value],
      approveStatus: 1,
      comment: '批量同意',
    }
    await batchApprove(params)
    message.success('批量同意成功')
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    message.error(error.message || '批量同意失败')
  }
}

async function handleBatchRemind() {
  if (!selectedExecutionIds.value.length) {
    message.warning('请先选择待办')
    return
  }
  try {
    const params: WfExecutionRemindParam = {
      executionIds: [...selectedExecutionIds.value],
      comment: '批量催办',
    }
    await batchRemind(params)
    message.success('批量催办已发送')
    clearSelection()
  } catch (error: any) {
    message.error(error.message || '批量催办失败')
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

.form-content,
.form-content-detail {
  margin-top: 16px;
  padding: 16px;
  background: var(--fx-fill-secondary, #f5f5f5);
  border: 1px solid var(--fx-border-color, #d9d9d9);
  border-radius: 4px;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
  font-family: var(--fx-font-family, inherit);
}

.form-content pre,
.form-content-detail pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
  font-family: var(--fx-font-family, inherit);
  font-size: var(--fx-font-size-sm, 12px);
  line-height: 1.5;
}

.form-content-detail h4 {
  margin: 0 0 16px 0;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
  font-size: var(--fx-font-size, 14px);
  font-weight: 600;
}
</style>
