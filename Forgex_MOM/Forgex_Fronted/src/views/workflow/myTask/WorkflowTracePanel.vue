<template>
  <div class="workflow-trace-panel">
    <div class="form-content-detail">
      <h4>{{ t('workflow.myTask.instanceTraceTitle') }}</h4>
      <a-empty v-if="!instances.length" :description="t('workflow.myTask.instanceTraceEmpty')" />
      <a-timeline v-else>
        <a-timeline-item v-for="item in instances" :key="item.id">
          <div class="trace-line">
            <strong>{{ item.approverName || `${t('workflow.myTask.userFallback')}${item.approverId}` }}</strong>
            <span>{{ t('workflow.myTask.instanceNo') }}: {{ item.instanceNo || '-' }}</span>
            <span>{{ t('workflow.myTask.instanceStatus') }}: {{ getWorkflowInstanceStatusText(item.status, t) }}</span>
            <span v-if="item.deadlineTime">{{ t('workflow.myTask.deadlineTime') }}: {{ formatDateTime(item.deadlineTime) }}</span>
            <span v-if="item.comment">{{ t('workflow.myTask.comment') }}: {{ item.comment }}</span>
          </div>
        </a-timeline-item>
      </a-timeline>
    </div>

    <a-divider v-if="showActionLogs" />

    <div v-if="showActionLogs" class="form-content-detail">
      <h4>{{ t('workflow.myTask.actionLogTitle') }}</h4>
      <a-empty v-if="!actionLogs.length" :description="t('workflow.myTask.actionLogEmpty')" />
      <a-timeline v-else>
        <a-timeline-item
          v-for="item in actionLogs"
          :key="item.id"
          :color="getWorkflowTraceColor(item.actionType)"
        >
          <template #dot>
            <component
              :is="getWorkflowTraceIcon(item.actionType)"
              :style="{ color: getWorkflowTraceColor(item.actionType) }"
            />
          </template>
          <div class="history-item">
            <div class="history-header">
              <span class="history-node">{{ item.operatorName || t('workflow.myTask.systemRecord') }}</span>
              <span class="history-status" :style="{ color: getWorkflowTraceColor(item.actionType) }">
                {{ getWorkflowTraceText(item.actionType, t) }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-info">
                <span>{{ t('workflow.myTask.handler') }}: {{ item.operatorName || t('workflow.myTask.systemRecord') }}</span>
                <span>{{ t('workflow.myTask.approveTime') }}: {{ formatDateTime(item.createTime) }}</span>
              </div>
              <div class="history-comment" v-if="item.targetUserName">
                <strong>{{ t('workflow.myTask.targetUser') }}: </strong>{{ item.targetUserName }}
              </div>
              <div class="history-comment" v-if="item.actionComment">
                <strong>{{ t('workflow.myTask.comment') }}: </strong>{{ item.actionComment }}
              </div>
            </div>
          </div>
        </a-timeline-item>
      </a-timeline>
    </div>
  </div>
</template>

<script setup lang="ts">
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import type { WfApprovalActionLogDTO, WfApprovalInstanceDTO } from '@/api/workflow/execution'
import {
  getWorkflowInstanceStatusText,
  getWorkflowTraceColor,
  getWorkflowTraceIcon,
  getWorkflowTraceText,
} from './traceHelper'

interface Props {
  instances?: WfApprovalInstanceDTO[]
  actionLogs?: WfApprovalActionLogDTO[]
  showActionLogs?: boolean
}

withDefaults(defineProps<Props>(), {
  instances: () => [],
  actionLogs: () => [],
  showActionLogs: true,
})

const { t } = useI18n({ useScope: 'global' })

function formatDateTime(dateTime?: string) {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.form-content-detail {
  margin-top: 16px;
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 4px;
}

.form-content-detail h4 {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
}

.trace-line {
  display: flex;
  flex-direction: column;
  gap: 4px;
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
