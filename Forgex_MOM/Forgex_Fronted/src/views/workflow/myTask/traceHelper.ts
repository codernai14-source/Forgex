import type { Component } from 'vue'
import {
  CheckCircleOutlined,
  ClockCircleOutlined,
  CloseCircleOutlined,
  HistoryOutlined,
  PlusOutlined,
  SwapOutlined,
} from '@ant-design/icons-vue'

export function getWorkflowTraceColor(actionType?: number): string {
  const colorMap: Record<number, string> = {
    0: 'gray',
    1: 'green',
    2: 'red',
    3: 'orange',
    4: 'blue',
    5: 'purple',
    6: 'gold',
    7: 'cyan',
    8: 'default',
  }
  return colorMap[actionType || 0] || 'gray'
}

export function getWorkflowTraceText(actionType: number | undefined, t: (key: string) => string): string {
  const textMap: Record<number, string> = {
    0: t('workflow.myTask.historyStatus.started'),
    1: t('workflow.myTask.historyStatus.approved'),
    2: t('workflow.myTask.historyStatus.rejected'),
    3: t('workflow.myTask.historyStatus.transferred'),
    4: t('workflow.myTask.historyStatus.addSigned'),
    5: t('workflow.myTask.historyStatus.delegated'),
    6: t('workflow.myTask.historyStatus.timeoutApproved'),
    7: t('workflow.myTask.historyStatus.timeoutTransferred'),
    8: t('workflow.myTask.historyStatus.systemClosed'),
  }
  return textMap[actionType || 0] || t('workflow.myTask.unknownStatus')
}

export function getWorkflowInstanceStatusText(status: number | undefined, t: (key: string) => string): string {
  const statusMap: Record<number, string> = {
    0: t('workflow.dashboard.status.pending'),
    1: t('workflow.myTask.historyStatus.approved'),
    2: t('workflow.myTask.historyStatus.rejected'),
    3: t('workflow.myTask.historyStatus.transferred'),
    4: t('workflow.myTask.historyStatus.systemClosed'),
  }
  return statusMap[status || 0] || t('workflow.myTask.unknownStatus')
}

export function getWorkflowTraceIcon(actionType?: number): Component {
  const iconMap: Record<number, Component> = {
    1: CheckCircleOutlined,
    2: CloseCircleOutlined,
    3: SwapOutlined,
    4: PlusOutlined,
    5: HistoryOutlined,
    6: ClockCircleOutlined,
    7: SwapOutlined,
    8: ClockCircleOutlined,
  }
  return iconMap[actionType || 0] || ClockCircleOutlined
}
