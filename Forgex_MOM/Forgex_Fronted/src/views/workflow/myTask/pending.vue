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
            {{ t('workflow.myTask.batchApprove') }}
          </a-button>
          <a-button
            :disabled="!selectedExecutionIds.length"
            @click="openBatchTransferDialog"
            v-permission="'wf:execution:transfer'"
          >
            {{ t('workflow.myTask.batchTransfer') }}
          </a-button>
          <a-button
            :disabled="!selectedExecutionIds.length"
            @click="handleBatchRemind"
            v-permission="'wf:execution:remind'"
          >
            {{ t('workflow.myTask.batchRemind') }}
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
            v-if="getAvailableActions(record).length"
            type="link"
            size="small"
            @click="openProcessDialog(record)"
          >
            <template #icon><FormOutlined /></template>
            {{ $t('workflow.myTask.process') }}
          </a-button>
          <a-button type="link" size="small" @click="handleViewDetail(record)">
            <template #icon><EyeOutlined /></template>
            {{ $t('workflow.myTask.detail') }}
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <BaseFormDialog
      v-model:open="approveDialogVisible"
      :title="$t('workflow.myTask.processApproval')"
      :loading="approving"
      :width="720"
      :ok-text="processSubmitText"
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

        <a-form-item :label="t('workflow.myTask.processAction')">
          <a-radio-group v-model:value="approveAction" button-style="solid">
            <a-radio-button v-for="action in availableActions" :key="action" :value="action">
              {{ getActionText(action) }}
            </a-radio-button>
          </a-radio-group>
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

        <a-form-item
          v-if="requiresReceiver"
          :label="t('workflow.myTask.receiver')"
          name="receiverIds"
        >
          <ReceiverSelector v-model="processReceiverModel" />
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
            <WorkflowFormPreview :record="currentRecord" />
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
        <a-form-item :label="t('workflow.myTask.actionScope')">
          <a-input :value="actionScopeText" disabled />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.receiver')" name="receiverIds">
          <ReceiverSelector v-model="actionReceiverModel" />
        </a-form-item>

        <a-form-item :label="t('workflow.myTask.actionComment')" name="comment">
          <a-textarea
            v-model:value="actionFormData.comment"
            :rows="4"
            :placeholder="t('workflow.myTask.actionCommentPlaceholder')"
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { message, type TableProps } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  EyeOutlined,
  FormOutlined,
} from '@ant-design/icons-vue'
import {
  addSign,
  approve,
  batchApprove,
  batchRemind,
  batchTransfer,
  delegate,
  getExecutionDetail,
  listApprovalActionLogs,
  listApprovalInstances,
  pageMyPending,
  reject,
  transfer,
  type WfApprovalActionLogDTO,
  type WfApprovalInstanceDTO,
  type WfExecutionApproveParam,
  type WfExecutionBatchApproveParam,
  type WfExecutionBatchTransferParam,
  type WfExecutionDelegateParam,
  type WfExecutionDTO,
  type WfExecutionRemindParam,
  type WorkflowId,
} from '@/api/workflow/execution'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import { useUserStore } from '@/stores/user'
import { usePermissionStore } from '@/stores/permission'
import WorkflowDetailDrawer from './WorkflowDetailDrawer.vue'
import WorkflowFormPreview from './WorkflowFormPreview.vue'
import {
  idsEqual,
  resolvePendingActions,
  resolveReceiverId,
  type PendingAction,
} from './pendingActionModel.mjs'
import dayjs from 'dayjs'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')
const { dictItems: rejectTypeOptions } = useDict('wf_reject_type')
const userStore = useUserStore()
const permissionStore = usePermissionStore()

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
const approveAction = ref<PendingAction>('approve')
const actionDialogVisible = ref(false)
const selectedExecutionIds = ref<WorkflowId[]>([])
const availableActions = ref<PendingAction[]>([])

const approveFormData = reactive<WfExecutionApproveParam>({
  executionId: 0,
  approveStatus: 1,
  comment: '',
  rejectType: undefined,
})

const actionFormData = reactive<{
  executionIds: WorkflowId[]
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

const processReceiverModel = ref<{
  receiverType?: string
  receiverIds: string[]
}>({
  receiverType: 'USER',
  receiverIds: [],
})

const requiresReceiver = computed(() => ['addSign', 'transfer', 'delegate'].includes(approveAction.value))
const processSubmitText = computed(() => getActionText(approveAction.value))

watch(approveAction, (action) => {
  approveFormData.approveStatus = action === 'reject' ? 2 : 1
  if (action !== 'reject') {
    approveFormData.rejectType = undefined
  }
  if (!['addSign', 'transfer', 'delegate'].includes(action)) {
    processReceiverModel.value = { receiverType: 'USER', receiverIds: [] }
  }
  approveFormRef.value?.clearValidate?.(['rejectType', 'receiverIds'])
})

const approveRules = computed(() => ({
  comment: [{ required: true, message: t('workflow.myTask.commentPlaceholder'), trigger: 'blur' }],
  rejectType: [{ required: true, message: t('workflow.myTask.rejectTypePlaceholder'), trigger: 'change' }],
  receiverIds: [{
    validator: async () => {
      if (!requiresReceiver.value) return
      if (processReceiverModel.value.receiverType !== 'USER') {
        throw new Error(t('workflow.myTask.selectUserReceiver'))
      }
      if (!processReceiverModel.value.receiverIds.length) {
        throw new Error(t('workflow.myTask.selectReceiver'))
      }
    },
    trigger: 'change',
  }],
}))

const actionRules = computed(() => ({
  receiverIds: [
    {
      validator: async () => {
        if (actionReceiverModel.value.receiverType !== 'USER') {
          throw new Error(t('workflow.myTask.selectUserReceiver'))
        }
        if (!actionReceiverModel.value.receiverIds.length) {
          throw new Error(t('workflow.myTask.selectReceiver'))
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
    selectedExecutionIds.value = [...keys]
  },
}))

const actionDialogTitle = computed(() => t('workflow.myTask.batchTransfer'))
const actionScopeText = computed(() => t('workflow.myTask.selectedPendingCount', { count: selectedExecutionIds.value.length }))

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

function openProcessDialog(record: WfExecutionDTO) {
  const actions = getAvailableActions(record)
  if (!actions.length) {
    message.warning(t('workflow.myTask.noAvailableAction'))
    return
  }
  currentRecord.value = record
  availableActions.value = actions
  approveAction.value = actions[0]
  approveFormData.executionId = record.id
  approveFormData.approveStatus = actions[0] === 'reject' ? 2 : 1
  approveFormData.comment = ''
  approveFormData.rejectType = undefined
  processReceiverModel.value = { receiverType: 'USER', receiverIds: [] }
  approveDialogVisible.value = true
}

function handleViewDetail(record: WfExecutionDTO) {
  currentRecord.value = record
  detailDrawerVisible.value = true
  loadExecutionTrace(record.id)
}

function resolveCurrentUserId(): WorkflowId | undefined {
  return userStore.userInfo?.id
}

function findCurrentUserInstance(record: WfExecutionDTO): WfApprovalInstanceDTO | undefined {
  const currentUserId = resolveCurrentUserId()
  if (!currentUserId) {
    return undefined
  }
  return (record.currentApprovalInstances || []).find((item) =>
    idsEqual(item.approverId, currentUserId) &&
    item.status === 0 &&
    item.activated !== false,
  )
}

function getAvailableActions(record: WfExecutionDTO): PendingAction[] {
  const instance = findCurrentUserInstance(record)
  return instance ? resolvePendingActions(instance, permissionStore.hasPermission) : []
}

function getActionText(action: PendingAction): string {
  return t(`workflow.myTask.actionLabels.${action}`)
}

function resetActionDialog() {
  actionFormData.executionIds = []
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
}

function openBatchTransferDialog() {
  if (!selectedExecutionIds.value.length) {
    message.warning(t('workflow.myTask.selectPendingFirst'))
    return
  }
  actionFormData.executionIds = [...selectedExecutionIds.value]
  actionFormData.comment = ''
  actionReceiverModel.value = {
    receiverType: 'USER',
    receiverIds: [],
  }
  actionDialogVisible.value = true
}

async function loadExecutionTrace(executionId: WorkflowId) {
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

    const instance = currentRecord.value ? findCurrentUserInstance(currentRecord.value) : undefined

    const params: WfExecutionApproveParam = {
      executionId: approveFormData.executionId,
      approveStatus: approveAction.value === 'reject' ? 2 : 1,
      comment: approveFormData.comment,
      approvalInstanceId: instance?.id,
    }

    if (approveAction.value === 'reject') {
      params.rejectType = approveFormData.rejectType
    }

    if (approveAction.value === 'approve') {
      await approve(params)
    } else if (approveAction.value === 'reject') {
      await reject(params)
    } else {
      if (!currentRecord.value || !instance) {
        message.warning(t('workflow.myTask.processInstanceMissing'))
        return
      }
      const targetApproverId = resolveReceiverId(processReceiverModel.value.receiverIds)
      if (!targetApproverId) {
        message.warning(t('workflow.myTask.selectReceiver'))
        return
      }
      const actionParams: WfExecutionDelegateParam = {
        executionId: currentRecord.value.id,
        approvalInstanceId: instance.id,
        targetApproverId,
        comment: approveFormData.comment,
      }
      if (approveAction.value === 'addSign') await addSign(actionParams)
      if (approveAction.value === 'transfer') await transfer(actionParams)
      if (approveAction.value === 'delegate') await delegate(actionParams)
    }

    approveDialogVisible.value = false
    approveFormRef.value?.resetFields()
    currentRecord.value = null
    currentInstances.value = []
    currentActionLogs.value = []
    availableActions.value = []
    processReceiverModel.value = { receiverType: 'USER', receiverIds: [] }
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || t('workflow.myTask.approveActionFailed'))
    await tableRef.value?.refresh?.()
  } finally {
    approving.value = false
  }
}

async function handleActionSubmit() {
  try {
    await actionFormRef.value?.validate()
    const targetApproverId = resolveReceiverId(actionReceiverModel.value.receiverIds)
    if (!targetApproverId) {
      message.warning(t('workflow.myTask.selectReceiver'))
      return
    }

    actionSubmitting.value = true

    const params: WfExecutionBatchTransferParam = {
      executionIds: [...actionFormData.executionIds],
      targetApproverId,
      comment: actionFormData.comment,
    }
    await batchTransfer(params)

    message.success(t('workflow.myTask.actionSuccess'))
    actionDialogVisible.value = false
    resetActionDialog()
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || t('workflow.myTask.actionFailed'))
  } finally {
    actionSubmitting.value = false
  }
}

function handleApproveCancel() {
  approveDialogVisible.value = false
  approveFormRef.value?.resetFields()
  availableActions.value = []
  processReceiverModel.value = { receiverType: 'USER', receiverIds: [] }
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
    message.warning(t('workflow.myTask.selectPendingFirst'))
    return
  }
  try {
    const params: WfExecutionBatchApproveParam = {
      executionIds: [...selectedExecutionIds.value],
      approveStatus: 1,
      comment: t('workflow.myTask.batchApprove'),
    }
    await batchApprove(params)
    message.success(t('workflow.myTask.batchApproveSuccess'))
    clearSelection()
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.batchApproveFailed'))
  }
}

async function handleBatchRemind() {
  if (!selectedExecutionIds.value.length) {
    message.warning(t('workflow.myTask.selectPendingFirst'))
    return
  }
  try {
    const params: WfExecutionRemindParam = {
      executionIds: [...selectedExecutionIds.value],
      comment: t('workflow.myTask.batchRemind'),
    }
    await batchRemind(params)
    message.success(t('workflow.myTask.batchRemindSent'))
    clearSelection()
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.batchRemindFailed'))
  }
}

onMounted(() => {
  tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less" src="@/styles/views/workflow/myTask/pending.less"></style>
