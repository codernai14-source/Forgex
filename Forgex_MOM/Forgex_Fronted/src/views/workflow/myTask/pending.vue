<template>
  <div class="page-wrap">
    <!-- 寰呭姙瀹℃壒鍒楄〃 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfPendingTaskTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
      :show-query-form="true"
    >
      <template #status="{ record }">
        <a-tag :color="get状态Color(record.status)">
          {{ get状态Text(record.status) }}
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
          <a-button
            type="link"
            size="small"
            @click="handleViewDetail(record)"
          >
            <template #icon><EyeOutlined /></template>
            {{ $t('workflow.myTask.detail') }}
          </a-button>
        </a-space>
      </template>
    </fx-dynamic-table>

    <!-- 瀹℃壒澶勭悊寮圭獥 -->
    <BaseFormDialog
      v-model:open="approveDialogVisible"
      :title="approveAction === 'approve' ? $t('workflow.myTask.approveAgree') : $t('workflow.myTask.approveReject')"
      :loading="approving"
      :width="600"
      @submit="handleApproveSubmit"
      @cancel="handleApproveCancel"
    >
      <a-form
        ref="approve表单Ref"
        :model="approve表单Data"
        :rules="approveRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 18 }"
      >
        <a-form-item label="瀹℃壒浠诲姟">
          <a-input
            :value="currentRecord?.taskName"
            disabled
          />
        </a-form-item>

        <a-form-item label="发起人">
          <a-input
            :value="currentRecord?.initiatorName"
            disabled
          />
        </a-form-item>

        <a-form-item label="鍙戣捣鏃堕棿">
          <a-input
            :value="formatDateTime(currentRecord?.startTime)"
            disabled
          />
        </a-form-item>

        <a-form-item
          v-if="approveAction === 'reject'"
          label="椹冲洖绫诲瀷"
          name="rejectType"
        >
          <a-select
            v-model:value="approve表单Data.rejectType"
            placeholder="璇烽€夋嫨椹冲洖绫诲瀷"
          >
            <a-select-option :value="1">椹冲洖浠诲姟锛堢洿鎺ョ粨鏉燂級</a-select-option>
            <a-select-option :value="2">杩斿洖涓婁竴鑺傜偣锛堥噸鏂板鎵癸級</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item label="瀹℃壒鎰忚" name="comment">
          <a-textarea
            v-model:value="approve表单Data.comment"
            placeholder="请输入审批意见"
            :rows="4"
          />
        </a-form-item>

        <!-- 琛ㄥ崟鍐呭灞曠ず -->
        <a-form-item label="琛ㄥ崟鍐呭">
          <div class="form-content">
            <pre>{{ format表单Content(currentRecord?.formContent) }}</pre>
          </div>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <!-- 璇︽儏鏌ョ湅寮圭獥 -->
    <a-drawer
      v-model:open="detailDrawerVisible"
      :title="'瀹℃壒璇︽儏'"
      :width="800"
      :body-style="{ paddingBottom: '80px' }"
    >
      <a-descriptions bordered :column="2">
        <a-descriptions-item label="瀹℃壒浠诲姟">
          {{ currentRecord?.taskName }}
        </a-descriptions-item>
        <a-descriptions-item label="浠诲姟缂栫爜">
          {{ currentRecord?.taskCode }}
        </a-descriptions-item>
        <a-descriptions-item label="发起人">
          {{ currentRecord?.initiatorName }}
        </a-descriptions-item>
        <a-descriptions-item label="鍙戣捣鏃堕棿">
          {{ formatDateTime(currentRecord?.startTime) }}
        </a-descriptions-item>
        <a-descriptions-item label="褰撳墠鑺傜偣">
          {{ currentRecord?.currentNodeName || '-' }}
        </a-descriptions-item>
        <a-descriptions-item label="状态">
          <a-tag :color="get状态Color(currentRecord?.status)">
            {{ get状态Text(currentRecord?.status) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>琛ㄥ崟鍐呭</h4>
        <pre>{{ format表单Content(currentRecord?.formContent) }}</pre>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
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
const approve表单Ref = ref()

const loading = ref(false)
const approving = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const approveDialogVisible = ref(false)
const approveAction = ref<'approve' | 'reject'>('approve')
const approve表单Data = reactive<WfExecutionApproveParam>({
  executionId: 0,
  approve状态: 1,
  comment: '',
  rejectType: undefined
})

const approveRules = {
  comment: [{ required: true, message: '请输入审批意见', trigger: 'blur' }],
  rejectType: [{ required: true, message: '请选择驳回类型', trigger: 'change' }]
}

const detailDrawerVisible = ref(false)

// 瀛楀吀閰嶇疆
const dictOptions = computed(() => ({
  status: statusOptions.value
}))

// 澶勭悊琛ㄦ牸鏁版嵁璇锋眰
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
    
    // 澶勭悊鎺掑簭
    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }
    
    const data = await pageMyPending(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    console.error('鍔犺浇寰呭姙鍒楄〃澶辫触', e)
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

// 鑾峰彇鐘舵€侀鑹?
const get状态Color = (status?: number): string => {
  const colorMap: Record<number, string> = {
    0: 'gray',
    1: 'processing',
    2: 'success',
    3: 'error'
  }
  return colorMap[status || 0] || 'default'
}

// 鑾峰彇鐘舵€佹枃鏈?
const get状态Text = (status?: number): string => {
  const textMap: Record<number, string> = {
    0: '未审批',
    1: '审批中',
    2: '审批完成',
    3: '驳回'
  }
  return textMap[status || 0] || '未知'
}

// 鏍煎紡鍖栨棩鏈熸椂闂?
const formatDateTime = (dateTime?: string): string => {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

// 鏍煎紡鍖栬〃鍗曞唴瀹?
const format表单Content = (formContent?: string): string => {
  if (!formContent) return '{}'
  try {
    const obj = JSON.parse(formContent)
    return JSON.stringify(obj, null, 2)
  } catch {
    return formContent
  }
}

// 澶勭悊瀹℃壒鍚屾剰
const handleApprove = (record: WfExecutionDTO) => {
  currentRecord.value = record
  approveAction.value = 'approve'
  approve表单Data.executionId = record.id
  approve表单Data.approve状态 = 1
  approve表单Data.comment = ''
  approve表单Data.rejectType = undefined
  approveDialogVisible.value = true
}

// 澶勭悊瀹℃壒椹冲洖
const handleReject = (record: WfExecutionDTO) => {
  currentRecord.value = record
  approveAction.value = 'reject'
  approve表单Data.executionId = record.id
  approve表单Data.approve状态 = 2
  approve表单Data.comment = ''
  approve表单Data.rejectType = undefined
  approveDialogVisible.value = true
}

// 澶勭悊鏌ョ湅璇︽儏
const handleViewDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

// 鎻愪氦瀹℃壒
const handleApproveSubmit = async () => {
  try {
    await approve表单Ref.value?.validate()
    approving.value = true

    const param: WfExecutionApproveParam = {
      executionId: approve表单Data.executionId,
      approve状态: approve表单Data.approve状态,
      comment: approve表单Data.comment
    }

    if (approveAction.value === 'reject') {
      param.rejectType = approve表单Data.rejectType
    }

    if (approveAction.value === 'approve') {
      await approve(param)
    } else {
      await reject(param)
    }

    approveDialogVisible.value = false
    tableRef.value?.refresh?.()
  } catch (e: any) {
    if (e.errorFields) {
      return
    }
    console.error('瀹℃壒鎿嶄綔澶辫触', e)
  } finally {
    approving.value = false
  }
}

// 鍙栨秷瀹℃壒
const handleApproveCancel = () => {
  approveDialogVisible.value = false
  approve表单Ref.value?.resetFields()
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
