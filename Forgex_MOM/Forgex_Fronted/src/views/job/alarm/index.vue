<template>
  <div class="job-page">
    <FxDynamicTable ref="tableRef" table-code="JobAlarmRuleTable" :request="handleRequest" row-key="id">
      <template #toolbar>
        <a-button v-permission="'job:alarm:add'" type="primary" @click="openDialog()">
          {{ t('job.actions.addAlarm') }}
        </a-button>
      </template>
      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'green' : 'default'">
          {{ record.status === 1 ? t('job.status.enabled') : t('job.status.disabled') }}
        </a-tag>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'job:alarm:edit'" @click="openDialog(record.id)">{{ t('common.edit') }}</a>
          <a v-permission="'job:alarm:delete'" class="danger-link" @click="handleDelete(record.id)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="currentId ? t('job.actions.editAlarm') : t('job.actions.addAlarm')"
      :width="640"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form :model="formState" layout="vertical">
        <a-row :gutter="12">
          <a-col :span="12"><a-form-item :label="t('job.fields.ruleName')" required><a-input v-model:value="formState.ruleName" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.jobCode')"><a-input v-model:value="formState.jobCode" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.alarmType')"><a-input-number v-model:value="formState.alarmType" :min="1" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.thresholdCount')"><a-input-number v-model:value="formState.thresholdCount" :min="1" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.windowMinutes')"><a-input-number v-model:value="formState.windowMinutes" :min="1" style="width: 100%" /></a-form-item></a-col>
          <a-col :span="12"><a-form-item :label="t('job.fields.notifyType')"><a-input v-model:value="formState.notifyType" /></a-form-item></a-col>
          <a-col :span="24"><a-form-item :label="t('job.fields.notifyTarget')"><a-textarea v-model:value="formState.notifyTarget" :rows="2" /></a-form-item></a-col>
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
import { deleteJobAlarmRule, getJobAlarmRuleDetail, getJobAlarmRulePage, saveJobAlarmRule } from '@/api/job/alarm'
import type { JobAlarmRule } from '@/api/job/types'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()
const dialogVisible = ref(false)
const saving = ref(false)
const currentId = ref<number>()
const formState = reactive<JobAlarmRule>({})

async function handleRequest(payload: { page: { current: number; pageSize: number }; query: Record<string, any> }) {
  const result = await getJobAlarmRulePage({ ...payload.query, pageNum: payload.page.current, pageSize: payload.page.pageSize })
  return { records: result.records || [], total: result.total || 0 }
}

async function openDialog(id?: number) {
  currentId.value = id
  Object.keys(formState).forEach(key => delete (formState as any)[key])
  if (id) {
    Object.assign(formState, await getJobAlarmRuleDetail(id))
  } else {
    Object.assign(formState, { alarmType: 1, thresholdCount: 1, windowMinutes: 5, status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  saving.value = true
  try {
    await saveJobAlarmRule({ ...formState, id: currentId.value })
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
      await deleteJobAlarmRule(id)
      tableRef.value?.refresh?.()
    },
  })
}
</script>

<style scoped lang="less" src="@/styles/views/job/alarm/index.less"></style>
