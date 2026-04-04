<template>
  <div class="page-wrap">
    <!-- 我发起的审批列表 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfInitiatedTaskTable'"
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

      <template #endTime="{ record }">
        {{ record.endTime ? formatDateTime(record.endTime) : '-' }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="handleViewDetail(record)"
          >
            <template #icon><EyeOutlined /></template>
            {{ $t('workflow.myTask.detail') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="handleViewHistory(record)"
          >
            <template #icon><HistoryOutlined /></template>
            {{ $t('workflow.myTask.history') }}
          </a-button>
          <a-popconfirm
            title="确定要撤销这个审批吗？"
            :ok-text="$t('common.confirm')"
            :cancel-text="$t('common.cancel')"
            @confirm="handleCancel(record)"
            v-if="record.status === 0 || record.status === 1"
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
        <a-descriptions-item label="结束时间" v-if="currentRecord?.endTime">
          {{ formatDateTime(currentRecord.endTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>表单内容</h4>
        <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
      </div>
    </a-drawer>

    <!-- 审批历史弹窗 -->
    <a-modal
      v-model:open="historyModalVisible"
      title="审批历史"
      :width="900"
      :footer="null"
    >
      <a-timeline>
        <a-timeline-item
          v-for="(item, index) in historyList"
          :key="index"
          :color="getHistoryColor(item.approveStatus)"
        >
          <template #dot>
            <component
              :is="getHistoryIcon(item.approveStatus)"
              :style="{ color: getHistoryColor(item.approveStatus) }"
            />
          </template>
          <div class="history-item">
            <div class="history-header">
              <span class="history-node">{{ item.nodeName }}</span>
              <span class="history-status" :style="{ color: getHistoryColor(item.approveStatus) }">
                {{ getHistoryStatusText(item.approveStatus) }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-info">
                <span>审批人：{{ item.approverName }}</span>
                <span>审批时间：{{ formatDateTime(item.approveTime) }}</span>
              </div>
              <div class="history-comment" v-if="item.comment">
                <strong>审批意见：</strong>{{ item.comment }}
              </div>
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  EyeOutlined,
  HistoryOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
  StopOutlined
} from '@ant-design/icons-vue'
import {
  pageMyInitiated,
  cancelExecution,
  type WfExecutionDTO
} from '@/api/workflow/execution'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { useDict } from '@/hooks/useDict'
import dayjs from 'dayjs'

const { dictItems: statusOptions } = useDict('status')

const tableRef = ref()

const loading = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const detailDrawerVisible = ref(false)
const historyModalVisible = ref(false)

// 审批历史列表（TODO: 需要从后端 API 获取）
const historyList = ref<any[]>([])

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
    
    const data = await pageMyInitiated(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    message.error(e.message || '加载发起列表失败')
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

// 获取历史状态颜色
const getHistoryColor = (approveStatus?: number): string => {
  const colorMap: Record<number, string> = {
    1: 'green',
    2: 'red'
  }
  return colorMap[approveStatus || 0] || 'gray'
}

// 获取历史状态文本
const getHistoryStatusText = (approveStatus?: number): string => {
  const textMap: Record<number, string> = {
    1: '同意',
    2: '驳回'
  }
  return textMap[approveStatus || 0] || '未知'
}

// 获取历史图标
const getHistoryIcon = (approveStatus?: number) => {
  const iconMap: Record<number, any> = {
    1: CheckCircleOutlined,
    2: CloseCircleOutlined
  }
  return iconMap[approveStatus || 0] || ClockCircleOutlined
}

// 处理查看详情
const handleViewDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

// 处理查看审批历史
const handleViewHistory = async (record: WfExecutionDTO) => {
  currentRecord.value = record
  historyModalVisible.value = true
  
  // TODO: 调用后端 API 获取审批历史
  try {
    // const result = await getExecutionHistory({ executionId: record.id })
    // historyList.value = result || []
    
    // 模拟数据
    historyList.value = [
      {
        nodeName: '发起',
        approverName: record.initiatorName,
        approveTime: record.startTime,
        approveStatus: 0,
        comment: '发起审批'
      }
    ]
  } catch (e: any) {
    message.error(e.message || '加载审批历史失败')
  }
}

// 处理撤销审批
const handleCancel = async (record: WfExecutionDTO) => {
  try {
    await cancelExecution({ executionId: record.id })
    message.success('撤销成功')
    await tableRef.value?.refresh?.()
  } catch (e: any) {
    message.error(e.message || '撤销失败')
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
  background-color: #f5f5f5;
  border-radius: 4px;
}

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

.history-item {
  margin-top: 8px;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-node {
  font-size: 14px;
  font-weight: 600;
}

.history-status {
  font-size: 13px;
}

.history-content {
  background-color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
}

.history-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #666;
  margin-bottom: 8px;
}

.history-comment {
  font-size: 13px;
  color: #333;
}
</style>
