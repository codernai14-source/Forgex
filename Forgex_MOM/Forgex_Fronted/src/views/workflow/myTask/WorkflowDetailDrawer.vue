<template>
  <a-drawer
    :open="open"
    :title="t('workflow.myTask.detailTitle')"
    :width="920"
    :body-style="{ paddingBottom: '80px' }"
    @update:open="value => emit('update:open', value)"
    @close="emit('update:open', false)"
  >
    <a-tabs v-model:active-key="activeTab">
      <a-tab-pane key="detail" :tab="t('workflow.myTask.detailInfoTab')">
        <a-descriptions bordered :column="2">
          <a-descriptions-item :label="t('workflow.myTask.taskName')">
            {{ record?.taskName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.taskCode')">
            {{ record?.taskCode || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.initiator')">
            {{ record?.initiatorName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.startTime')">
            {{ formatDateTime(record?.startTime) }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.currentNode')">
            {{ record?.currentNodeName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('workflow.myTask.status')">
            <DictTag :value="record?.status" :items="executionStatusOptions" :fallback-text="statusText" />
          </a-descriptions-item>
          <a-descriptions-item v-if="record?.endTime" :label="t('workflow.myTask.endTime')">
            {{ formatDateTime(record.endTime) }}
          </a-descriptions-item>
        </a-descriptions>

        <WorkflowTracePanel
          class="detail-trace"
          :instances="instances"
          :action-logs="actionLogs"
          :show-action-logs="showActionLogs"
        />
      </a-tab-pane>

      <a-tab-pane key="form" :tab="t('workflow.myTask.formDataTab')">
        <WorkflowFormPreview :record="record" />
      </a-tab-pane>
    </a-tabs>
  </a-drawer>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from 'vue-i18n'
import type { WfApprovalActionLogDTO, WfApprovalInstanceDTO, WfExecutionDTO } from '@/api/workflow/execution'
import DictTag from '@/components/common/DictTag.vue'
import { getDictItemLabel, useDict } from '@/hooks/useDict'
import WorkflowFormPreview from './WorkflowFormPreview.vue'
import WorkflowTracePanel from './WorkflowTracePanel.vue'

interface Props {
  open: boolean
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

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
}>()

const { t } = useI18n({ useScope: 'global' })
const { dictItems: executionStatusOptions } = useDict('wf_execution_status')
const activeTab = ref('detail')

const statusText = computed(() =>
  getDictItemLabel(executionStatusOptions.value, props.record?.status, t('workflow.myTask.unknownStatus')),
)

watch(
  () => props.open,
  open => {
    if (open) {
      activeTab.value = 'detail'
    }
  },
)

function formatDateTime(dateTime?: string): string {
  if (!dateTime) return '-'
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm:ss')
}
</script>

<style scoped>
.detail-trace {
  margin-top: 16px;
}
</style>
