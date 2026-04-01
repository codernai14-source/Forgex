<template>
  <div class="page-wrap">
    <!-- 待办审批列表 -->
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
            同意
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="handleReject(record)"
            v-permission="'wf:execution:reject'"
          >
            <template #icon><CloseOutlined /></template>
            驳回
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="handleViewDetail(record)"
          >
            <template #icon><EyeOutlined /></template>
            详情
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <!-- 审批处理弹窗 -->
    <BaseFormDialog
      v-model:open="approveDialogVisible"
      :title="approveAction === 'approve' ? '审批同意' : '审批驳回'"
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
          <a-input
            v-model:value="currentRecord?.taskName"
            disabled
          />
        </a-form-item>

        <a-form-item label="发起人">
          <a-input
            v-model:value="currentRecord?.initiatorName"
            disabled
          />
        </a-form-item>

        <a-form-item label="发起时间">
          <a-input
            v-model:value="formatDateTime(currentRecord?.startTime)"
            disabled
          />
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
            <a-select-option :value="1">驳回任务（直接结束）</a-select-option>
            <a-select-option :value="2">返回上一节点（重新审批）</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="审批意见" name="comment">
          <a-textarea
            v-model:value="approveFormData.comment"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </a-form-item>

        <!-- 表单内容展示 -->
        <a-form-item label="表单内容">
          <div class="form-content">
            <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
          </div>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <!-- 详情查看弹窗 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      :title="'审批详情'"
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
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  CheckOutlined,
  CloseOutlined,
  EyeOutlined
} from '@ant-design/icons-vue'
import {
  pageMyPending,
  approve,
  reject,
  type WfExecutionDTO,
  type WfExecutionApproveParam
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
const approveAction = ref<'approve' | 'reject'>('approve')
const approveFormData = reactive<WfExecutionApproveParam>({
  executionId: 0,
  approveStatus: 1,
  comment: '',
  rejectType: undefined
})

const approveRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
  rejectType: [{ required: true, message: '请选择驳回类型', trigger: 'change' }]
}

const detailDrawerVisible = ref(false)

// 字典配置
const dictOptions = computed(() => ({
  status: statusOptions.value
}))

// 处理表格数据请求
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    loading.value = true
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query
    }
    
    // 处理排序
    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }
    
    const data = await pageMyPending(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    message.error(e.message || '加载待办列表失败')
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

// 获取状态颜色
const getStatusColor = (status?: number): string => {
  const colorMap: Record<number, string> = {
    0: 'gray',
    1: 'processing',
    2: 'success',
    3: 'error'
  }
  return colorMap[status || 0] || 'default'
}

// 获取状态文本
const getStatusText = (status?: number): string => {
  const textMap: Record<number, string> = {
    0: '未审批',
    1: '审批中',
    2: '审批完成',
    3: '驳回'
  }
  return textMap[status || 0] || '未知'
}

// 格式化日期时间
const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 格式化表单内容
const formatFormContent = (formContent?: string): string => {
  if (!formContent) return '{}'
  try {
    const obj = JSON.parse(formContent)
    return JSON.stringify(obj, null, 2)
  } catch {
    return formContent
  }
}

// 处理审批同意
const handleApprove = (record: WfExecutionDTO) => {
  currentRecord.value = record
  approveAction.value = 'approve'
  approveFormData.executionId = record.id
  approveFormData.approveStatus = 1
  approveFormData.comment = ''
  approveFormData.rejectType = undefined
  approveDialogVisible.value = true
}

// 处理审批驳回
const handleReject = (record: WfExecutionDTO) => {
  currentRecord.value = record
  approveAction.value = 'reject'
  approveFormData.executionId = record.id
  approveFormData.approveStatus = 2
  approveFormData.comment = ''
  approveFormData.rejectType = undefined
  approveDialogVisible.value = true
}

// 处理查看详情
const handleViewDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

// 提交审批
const handleApproveSubmit = async () => {
  try {
    await approveFormRef.value?.validate()
    approving.value = true

    const param: WfExecutionApproveParam = {
      executionId: approveFormData.executionId,
      approveStatus: approveFormData.approveStatus,
      comment: approveFormData.comment
    }

    if (approveAction.value === 'reject') {
      param.rejectType = approveFormData.rejectType
    }

    if (approveAction.value === 'approve') {
      await approve(param)
      message.success('审批同意成功')
    } else {
      await reject(param)
      message.success('审批驳回成功')
    }

    approveDialogVisible.value = false
    tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    message.error(e.message || '审批操作失败')
  } finally {
    approving.value = false
  }
}

// 取消审批
const handleApproveCancel = () => {
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
