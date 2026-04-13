<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfPendingTaskTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #status="{ record }">
        <a-tag :color="getStatusColor(record.status)">
          {{ getStatusText(record.status) }}
        </a-tag>
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
        <a-form-item label="审批任务">
          <a-input :value="currentRecord?.taskName" disabled />
        </a-form-item>

        <a-form-item label="发起人">
          <a-input :value="currentRecord?.initiatorName" disabled />
        </a-form-item>

        <a-form-item label="发起时间">
          <a-input :value="formatDateTime(currentRecord?.startTime)" disabled />
        </a-form-item>

        <a-form-item
          v-if="approveAction === 'reject'"
          label="驳回类型"
          name="rejectType"
        >
          <a-select
            v-model:value="approveFormData.rejectType"
            placeholder="请选择驳回类型"
          >
            <a-select-option :value="1">驳回结束当前审批流程</a-select-option>
            <a-select-option :value="2">退回上一节点重新审批</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="审批意见" name="comment">
          <a-textarea
            v-model:value="approveFormData.comment"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </a-form-item>

        <a-form-item label="表单内容">
          <div class="form-content">
            <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
          </div>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <a-drawer
      v-model:open="detailDrawerVisible"
      title="审批详情"
      :width="800"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="审批任务">
          {{ currentRecord?.taskName }}
        </a-descriptions-item>
        <a-descriptions-item label="任务编码">
          {{ currentRecord?.taskCode }}
        </a-descriptions-item>
        <a-descriptions-item label="发起人">
          {{ currentRecord?.initiatorName }}
        </a-descriptions-item>
        <a-descriptions-item label="发起时间">
          {{ formatDateTime(currentRecord?.startTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="当前节点">
          {{ currentRecord?.currentNodeName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="getStatusColor(currentRecord?.status)">
            {{ getStatusText(currentRecord?.status) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>表单内容</h4>
        <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined,
} from '@ant-design/icons-vue'
import {
  approve,
  pageMyPending,
  reject,
  type WfExecutionApproveParam,
  type WfExecutionDTO,
} from '@/api/workflow/execution'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import dayjs from 'dayjs'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()
const approveFormRef = ref()
const loading = ref(false)
const approving = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const approveDialogVisible = ref(false)
const detailDrawerVisible = ref(false)
const approveAction = ref<'approve' | 'reject'>('approve')

const approveFormData = reactive<WfExecutionApproveParam>({
  executionId: 0,
  approveStatus: 1,
  comment: '',
  rejectType: undefined,
})

const approveRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
  rejectType: [{ required: true, message: '请选择驳回类型', trigger: 'change' }],
}

const dictOptions = computed(() => ({
  status: statusOptions.value,
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

    const data = await pageMyPending(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    console.error('加载待办审批列表失败', error)
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function getStatusColor(status?: number): string {
  const colorMap: Record<number, string> = {
    0: 'default',
    1: 'processing',
    2: 'success',
    3: 'error',
  }
  return colorMap[status || 0] || 'default'
}

function getStatusText(status?: number): string {
  const textMap: Record<number, string> = {
    0: '待处理',
    1: '审批中',
    2: '审批完成',
    3: '驳回',
  }
  return textMap[status || 0] || '未知'
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
    await tableRef.value?.refresh?.()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    console.error('审批处理失败', error)
  } finally {
    approving.value = false
  }
}

function handleApproveCancel() {
  approveDialogVisible.value = false
  approveFormRef.value?.resetFields()
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
  background-color: #f5f5f5;
  border-radius: 4px;
}

.form-content pre,
.form-content-detail pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  line-height: 1.5;
}

.form-content-detail h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
}
</style>
