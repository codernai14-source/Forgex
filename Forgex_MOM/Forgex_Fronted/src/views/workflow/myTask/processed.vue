<template>
  <div class="page-wrap">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'WfProcessedTaskTable'"
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
        <a-descriptions-item :label="t('workflow.myTask.endTime')" v-if="currentRecord?.endTime">
          {{ formatDateTime(currentRecord.endTime) }}
        </a-descriptions-item>
      </a-descriptions>

      <a-divider />

      <div class="form-content-detail">
        <h4>{{ t('workflow.myTask.formContent') }}</h4>
        <pre>{{ formatFormContent(currentRecord?.formContent) }}</pre>
      </div>
    </a-drawer>

    <a-modal
      v-model:open="historyModalVisible"
      :title="t('workflow.myTask.historyTitle')"
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
                {{ getHistoryText(item.approveStatus) }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-info">
                <span>{{ t('workflow.myTask.handler') }}: {{ item.approverName }}</span>
                <span>{{ t('workflow.myTask.approveTime') }}: {{ formatDateTime(item.approveTime) }}</span>
              </div>
              <div class="history-comment" v-if="item.comment">
                <strong>{{ t('workflow.myTask.comment') }}: </strong>{{ item.comment }}
              </div>
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  HistoryOutlined,
} from '@ant-design/icons-vue'
import {
  pageMyProcessed,
  type WfExecutionDTO,
} from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import dayjs from 'dayjs'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')

const tableRef = ref()
const loading = ref(false)

const currentRecord = ref<WfExecutionDTO | null>(null)
const detailDrawerVisible = ref(false)
const historyModalVisible = ref(false)
const historyList = ref<any[]>([])

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

    const data = await pageMyProcessed(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total }
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.loadProcessedFailed'))
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

function getHistoryColor(approveStatus?: number): string {
  const colorMap: Record<number, string> = {
    0: 'gray',
    1: 'green',
    2: 'red',
  }
  return colorMap[approveStatus || 0] || 'gray'
}

function getHistoryText(approveStatus?: number): string {
  const textMap: Record<number, string> = {
    0: t('workflow.myTask.historyStatus.started'),
    1: t('workflow.myTask.historyStatus.approved'),
    2: t('workflow.myTask.historyStatus.rejected'),
  }
  return textMap[approveStatus || 0] || t('workflow.myTask.unknownStatus')
}

function getHistoryIcon(approveStatus?: number) {
  const iconMap: Record<number, any> = {
    1: CheckCircleOutlined,
    2: CloseCircleOutlined,
  }
  return iconMap[approveStatus || 0] || ClockCircleOutlined
}

function handleViewDetail(record: WfExecutionDTO) {
  currentRecord.value = record
  detailDrawerVisible.value = true
}

async function handleViewHistory(record: WfExecutionDTO) {
  currentRecord.value = record
  historyModalVisible.value = true

  try {
    historyList.value = [
      {
        nodeName: t('workflow.myTask.historyStartNode'),
        approverName: record.initiatorName,
        approveTime: record.startTime,
        approveStatus: 0,
        comment: t('workflow.myTask.historyComment.started'),
      },
      {
        nodeName: record.currentNodeName || t('workflow.myTask.historyCurrentNodeFallback'),
        approverName: t('workflow.myTask.systemRecord'),
        approveTime: record.endTime || record.updateTime || record.startTime,
        approveStatus: record.status === 3 ? 2 : 1,
        comment: record.status === 3 ? t('workflow.myTask.historyComment.rejected') : t('workflow.myTask.historyComment.finished'),
      },
    ]
  } catch (error: any) {
    message.error(error.message || t('workflow.myTask.loadHistoryFailed'))
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
