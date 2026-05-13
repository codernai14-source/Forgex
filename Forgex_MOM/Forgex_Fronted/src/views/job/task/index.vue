<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobTaskTable" :request="handleRequest" row-key="id">
      <template #toolbar>
        <a-button v-permission="'job:task:add'" type="primary" @click="openDialog()">
          {{ t('job.actions.addTask') }}
        </a-button>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'default'">
          {{ record.status === 1 ? t('job.status.enabled') : t('job.status.disabled') }}
        </a-tag>
      </template>

      <template #lastStatus="{ record }">
        <a-tag :color="statusColor(record.lastStatus)">{{ statusText(record.lastStatus) }}</a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'job:task:edit'" @click="openDialog(record.id)">{{ t('common.edit') }}</a>
          <a v-permission="'job:task:trigger'" @click="handleTrigger(record.id)">{{ t('job.actions.trigger') }}</a>
          <a v-permission="'job:task:changeStatus'" @click="handleChangeStatus(record)">
            {{ record.status === 1 ? t('job.actions.disable') : t('job.actions.enable') }}
          </a>
          <a v-permission="'job:task:delete'" class="danger-link" @click="handleDelete(record.id)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="currentId ? t('job.actions.editTask') : t('job.actions.addTask')"
      :width="760"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form :model="formState" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item :label="t('job.fields.jobCode')" required><a-input v-model:value="formState.jobCode" :disabled="!!currentId" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.jobName')" required><a-input v-model:value="formState.jobName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.jobGroup')"><a-input v-model:value="formState.jobGroup" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.jobType')"><a-select v-model:value="formState.jobType" :options="jobTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.scheduleType')"><a-select v-model:value="formState.scheduleType" :options="scheduleTypeOptions" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.cronExpression')"><a-input v-model:value="formState.cronExpression" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.beanName')"><a-input v-model:value="formState.beanName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.methodName')"><a-input v-model:value="formState.methodName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.httpUrl')"><a-input v-model:value="formState.httpUrl" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.scriptPath')"><a-input v-model:value="formState.scriptPath" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.timeoutSeconds')"><a-input-number v-model:value="formState.timeoutSeconds" :min="1" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.maxRetryCount')"><a-input-number v-model:value="formState.maxRetryCount" :min="0" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('job.fields.jobParams')"><a-textarea v-model:value="formState.jobParams" :rows="3" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('job.fields.remark')"><a-textarea v-model:value="formState.remark" :rows="2" /></a-form-item></a-col>
        </a-row>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { changeJobTaskStatus, deleteJobTask, getJobTaskDetail, getJobTaskPage, saveJobTask, triggerJobTask } from '@/api/job/task'
import type { JobTask } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)
const currentId = ref<number>()
const formState = reactive<JobTask>({})

const jobTypeOptions = [
  { label: 'Java Bean', value: 1 },
  { label: 'HTTP', value: 2 },
  { label: 'Script', value: 3 },
  { label: 'RocketMQ', value: 4 },
  { label: 'DAG', value: 5 },
]
const scheduleTypeOptions = [
  { label: 'Manual', value: 0 },
  { label: 'Cron', value: 1 },
  { label: 'Fixed Interval', value: 2 },
]

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobTaskPage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function openDialog(id?: number) {
  currentId.value = id
  Object.keys(formState).forEach(key => delete (formState as any)[key])
  if (id) {
    Object.assign(formState, await getJobTaskDetail(id))
  } else {
    Object.assign(formState, { jobType: 1, scheduleType: 0, status: 0, shardTotal: 1, timeoutSeconds: 60, maxRetryCount: 0 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await saveJobTask({ ...formState, id: currentId.value })
    dialogVisible.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

function handleDelete(id?: number) {
  if (!id) return
  Modal.confirm({
    title: t('common.confirmDelete'),
    onOk: async () => {
      await deleteJobTask(id)
      tableRef.value?.refresh?.()
    },
  })
}

async function handleTrigger(id?: number) {
  if (!id) return
  await triggerJobTask(id)
  tableRef.value?.refresh?.()
}

async function handleChangeStatus(record: JobTask) {
  if (!record.id) return
  await changeJobTaskStatus(record.id, record.status === 1 ? 0 : 1)
  tableRef.value?.refresh?.()
}

function statusColor(status?: number) {
  if (status === 1) return 'green'
  if (status === 2) return 'red'
  if (status === 3) return 'orange'
  return 'default'
}

function statusText(status?: number) {
  if (status === 1) return t('job.logStatus.success')
  if (status === 2) return t('job.logStatus.failed')
  if (status === 3) return t('job.logStatus.timeout')
  return '-'
}
</script>

<style scoped lang="less" src="@/styles/views/job/task/index.less"></style>
