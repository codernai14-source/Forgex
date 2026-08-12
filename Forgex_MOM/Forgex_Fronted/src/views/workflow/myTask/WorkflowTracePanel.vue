<template>
  <div class="workflow-trace-panel">
    <div class="form-content-detail">
      <h4>{{ t('workflow.myTask.instanceTraceTitle') }}</h4>
      <a-empty v-if="!timelineItems.length" :description="t('workflow.myTask.instanceTraceEmpty')" />
      <a-timeline v-else>
        <a-timeline-item
          v-for="item in timelineItems"
          :key="item.key"
          :color="item.color"
        >
          <template #dot>
            <component
              :is="item.icon"
              :style="{ color: item.color }"
            />
          </template>
          <div class="history-item">
            <div class="history-header">
              <span class="history-node">{{ item.title }}</span>
              <span class="history-status" :style="{ color: item.color }">
                {{ item.statusText }}
              </span>
            </div>
            <div class="history-content">
              <div class="history-info">
                <span>{{ item.operatorLabel }}: {{ item.operatorName || '-' }}</span>
                <span>{{ item.timeLabel }}: {{ formatDateTime(item.time) }}</span>
              </div>
              <div class="history-comment" v-if="item.instanceNo">
                <strong>{{ t('workflow.myTask.instanceNo') }}: </strong>{{ item.instanceNo }}
              </div>
              <div class="history-comment" v-if="item.targetUserName">
                <strong>{{ t('workflow.myTask.targetUser') }}: </strong>{{ item.targetUserName }}
              </div>
              <div class="history-comment" v-if="item.comment">
                <strong>{{ t('workflow.myTask.comment') }}: </strong>{{ item.comment }}
              </div>
            </div>
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
import { computed } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import type { Component } from 'vue'
import type { WfApprovalActionLogDTO, WfApprovalInstanceDTO, WfExecutionDTO } from '@/api/workflow/execution'
import {
  getWorkflowInstanceStatusText,
  getWorkflowTraceColor,
  getWorkflowTraceIcon,
  getWorkflowTraceText,
} from './traceHelper'

interface Props {
  record?: WfExecutionDTO | null
  instances?: WfApprovalInstanceDTO[]
  actionLogs?: WfApprovalActionLogDTO[]
  showActionLogs?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  record: null,
  instances: () => [],
  actionLogs: () => [],
  showActionLogs: true,
})

const { t } = useI18n({ useScope: 'global' })

interface TimelineItem {
  key: string
  time?: string
  title: string
  statusText: string
  color: string
  icon: Component
  operatorLabel: string
  operatorName?: string
  timeLabel: string
  instanceNo?: string
  targetUserName?: string
  comment?: string
  order: number
}

const timelineItems = computed<TimelineItem[]>(() => {
  const items: TimelineItem[] = []

  if (props.record?.startTime) {
    items.push({
      key: `start-${props.record.id}`,
      time: props.record.startTime,
      title: props.record.taskName || t('workflow.myTask.historyStartNode'),
      statusText: getWorkflowTraceText(0, t),
      color: getWorkflowTraceColor(0),
      icon: getWorkflowTraceIcon(0),
      operatorLabel: t('workflow.myTask.initiator'),
      operatorName: props.record.initiatorName,
      timeLabel: t('workflow.myTask.startTime'),
      comment: t('workflow.myTask.historyComment.started'),
      order: 0,
    })
  }

  props.instances.forEach((item, index) => {
    const actionType = item.actionType || instanceStatusToActionType(item.status)
    items.push({
      key: `instance-${item.id || index}`,
      time: item.approveTime || item.deadlineTime,
      title: props.record?.currentNodeName || t('workflow.myTask.historyCurrentNodeFallback'),
      statusText: getWorkflowInstanceStatusText(item.status, t),
      color: getWorkflowTraceColor(actionType),
      icon: getWorkflowTraceIcon(actionType),
      operatorLabel: t('workflow.myTask.handler'),
      operatorName: item.approverName || t('workflow.myTask.userFallback'),
      timeLabel: t('workflow.myTask.approveTime'),
      instanceNo: item.instanceNo,
      comment: item.comment,
      order: 10 + index,
    })
  })

  props.actionLogs.forEach((item, index) => {
    if (item.approvalInstanceId && props.instances.some(instance => instance.id === item.approvalInstanceId)) {
      return
    }
    items.push({
      key: `log-${item.id || index}`,
      time: item.createTime,
      title: item.operatorName || t('workflow.myTask.systemRecord'),
      statusText: getWorkflowTraceText(item.actionType, t),
      color: getWorkflowTraceColor(item.actionType),
      icon: getWorkflowTraceIcon(item.actionType),
      operatorLabel: t('workflow.myTask.handler'),
      operatorName: item.operatorName || t('workflow.myTask.systemRecord'),
      timeLabel: t('workflow.myTask.approveTime'),
      targetUserName: item.targetUserName,
      comment: item.actionComment,
      order: 100 + index,
    })
  })

  if (props.record?.endTime && props.record.status !== 1) {
    items.push({
      key: `end-${props.record.id}`,
      time: props.record.endTime,
      title: props.record.taskName || t('workflow.myTask.historyCurrentNodeFallback'),
      statusText: getExecutionStatusText(props.record.status),
      color: props.record.status === 2 ? 'green' : 'red',
      icon: getWorkflowTraceIcon(props.record.status === 2 ? 1 : 2),
      operatorLabel: t('workflow.myTask.handler'),
      operatorName: t('workflow.myTask.systemRecord'),
      timeLabel: t('workflow.myTask.endTime'),
      comment: getEndComment(props.record.status),
      order: 1000,
    })
  }

  return items.sort((left, right) => {
    const leftTime = left.time ? dayjs(left.time).valueOf() : Number.MAX_SAFE_INTEGER
    const rightTime = right.time ? dayjs(right.time).valueOf() : Number.MAX_SAFE_INTEGER
    return leftTime === rightTime ? left.order - right.order : leftTime - rightTime
  })
})

function formatDateTime(dateTime?: string) {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}

function instanceStatusToActionType(status?: number): number {
  if (status === 1) return 1
  if (status === 2) return 2
  if (status === 3) return 3
  if (status === 4) return 8
  return 0
}

function getExecutionStatusText(status?: number): string {
  if (status === 2) return t('workflow.dashboard.status.done')
  if (status === 3 && props.actionLogs.some(item => item.actionType === 9)) return t('workflow.myTask.historyStatus.canceled')
  if (status === 3) return t('workflow.dashboard.status.rejected')
  return t('workflow.myTask.unknownStatus')
}

function getEndComment(status?: number): string {
  if (status === 2) return t('workflow.myTask.historyComment.finished')
  if (status === 3 && props.actionLogs.some(item => item.actionType === 9)) return t('workflow.myTask.historyComment.canceled')
  if (status === 3) return t('workflow.myTask.historyComment.rejected')
  return ''
}
</script>

<style scoped lang="less" src="@/styles/views/workflow/myTask/workflow-trace-panel.less"></style>
