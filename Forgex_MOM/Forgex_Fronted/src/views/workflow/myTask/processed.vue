<template>
  <div class="page-wrap">
    <!-- 宸插鐞嗗鎵瑰垪琛?-->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfProcessedTaskTable'"
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
        </a-space>
      </template>
    </fx-dynamic-table>

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
        <a-descriptions-item label="缁撴潫鏃堕棿" v-if="currentRecord?.endTime">
          {{ formatDateTime(currentRecord.endTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>琛ㄥ崟鍐呭</h4>
        <pre>{{ format表单Content(currentRecord?.formContent) }}</pre>
      </div>
    </a-drawer>

    <!-- 瀹℃壒鍘嗗彶寮圭獥 -->
    <a-modal
      v-model:open="historyModalVisible"
      title="瀹℃壒鍘嗗彶"
      :width="900"
      :footer="null"
    >
      <a-timeline>
        <a-timeline-item
          v-for="(item, index) in historyList"
          :key="index"
          :color="getHistoryColor(item.approve状态)"
        >
          <template #dot>
            <component
              :is="getHistoryIcon(item.approve状态)"
              :style="{ color: getHistoryColor(item.approve状态) }"
            />
          </template>
          <div class="history-item">
            <div class="history-header">
              <span class="history-node">{{ item.nodeName }}</span>
              <span class="history-status" :style="{ color: getHistoryColor(item.approve状态) }">
                {{ getHistory状态Text(item.approve状态) }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-info">
                <span>瀹℃壒浜猴細{{ item.approverName }}</span>
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
  ClockCircleOutlined
} from '@ant-design/icons-vue'
import {
  pageMyProcessed,
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

// 瀹℃壒鍘嗗彶鍒楄〃锛圱ODO: 闇€瑕佷粠鍚庣 API 鑾峰彇锛?
const historyList = ref<any[]>([])

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
    
    const data = await pageMyProcessed(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (e: any) {
    message.error(e.message || '加载已处理列表失败')
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

// 鑾峰彇鍘嗗彶鐘舵€侀鑹?
const getHistoryColor = (approve状态?: number): string => {
  const colorMap: Record<number, string> = {
    1: 'green',
    2: 'red'
  }
  return colorMap[approve状态 || 0] || 'gray'
}

// 鑾峰彇鍘嗗彶鐘舵€佹枃鏈?
const getHistory状态Text = (approve状态?: number): string => {
  const textMap: Record<number, string> = {
    1: '同意',
    2: '驳回'
  }
  return textMap[approve状态 || 0] || '未知'
}

// 鑾峰彇鍘嗗彶鍥炬爣
const getHistoryIcon = (approve状态?: number) => {
  const iconMap: Record<number, any> = {
    1: CheckCircleOutlined,
    2: CloseCircleOutlined
  }
  return iconMap[approve状态 || 0] || ClockCircleOutlined
}

// 澶勭悊鏌ョ湅璇︽儏
const handleViewDetail = (record: WfExecutionDTO) => {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

// 澶勭悊鏌ョ湅瀹℃壒鍘嗗彶
const handleViewHistory = async (record: WfExecutionDTO) => {
  currentRecord.value = record
  historyModalVisible.value = true
  
  // TODO: 璋冪敤鍚庣 API 鑾峰彇瀹℃壒鍘嗗彶
  // 杩欓噷鏄ā鎷熸暟鎹?
  try {
    // const result = await getExecutionHistory({ executionId: record.id })
    // historyList.value = result || []
    
    // 妯℃嫙鏁版嵁
    historyList.value = [
      {
        nodeName: '发起',
        approverName: record.initiatorName,
        approveTime: record.startTime,
        approve状态: 0,
        comment: '发起审批'
      },
      {
        nodeName: '部门经理审批',
        approverName: '张经理',
        approveTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        approve状态: 1,
        comment: '同意'
      }
    ]
  } catch (e: any) {
    message.error(e.message || '鍔犺浇瀹℃壒鍘嗗彶澶辫触')
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
