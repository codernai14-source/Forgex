<template>
  <div class="page-wrap">
    <FxDynamicTable
      ref="tableRef"
      :table-code="'WfTaskConfigTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      row-key="id"
    >
      <template #toolbar>
        <a-button type="primary" v-permission="'wf:taskConfig:add'" @click="openCreate">
          {{ t('workflow.taskConfig.list.createFlow') }}
        </a-button>
      </template>

      <template #formType="{ record }">
        <a-tag :color="record.formType === 1 ? 'blue' : 'green'">
          {{ getFormTypeLabel(record.formType) }}
        </a-tag>
      </template>

      <template #status="{ record }">
        <a-tag :color="record.status === 1 ? 'success' : 'default'">
          {{ getStatusLabel(record.status) }}
        </a-tag>
      </template>

      <template #version="{ record }">
        <div class="version-cell">
          <a-tag v-if="record.publishedVersion" color="blue">
            {{ t('workflow.taskConfig.list.publishedVersion', { version: record.publishedVersion }) }}
          </a-tag>
          <a-tag v-if="record.draftVersion" color="orange">
            {{ t('workflow.taskConfig.list.draftVersion', { version: record.draftVersion }) }}
          </a-tag>
          <span v-if="!record.publishedVersion && record.draftVersion" class="version-hint">
            {{ t('workflow.taskConfig.list.unpublished') }}
          </span>
          <span v-if="!record.publishedVersion && !record.draftVersion">-</span>
        </div>
      </template>

      <template #remark="{ record }">
        <span class="remark-text">{{ record.remark || '-' }}</span>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            v-permission="'wf:taskConfig:edit'"
            @click="openEdit(record)"
          >
            {{ t('workflow.taskConfig.list.edit') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            v-permission="'wf:taskConfig:config'"
            @click="handleNodeConfig(record)"
          >
            {{ t('workflow.taskConfig.nodeConfig') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            :disabled="!record.hasDraft"
            v-permission="'wf:taskConfig:config'"
            @click="handlePublish(record)"
          >
            {{ t('workflow.taskConfig.list.publish') }}
          </a-button>
          <a-popconfirm
            :title="t('workflow.taskConfig.list.confirmDeleteWithArchive')"
            :ok-text="t('common.confirm')"
            :cancel-text="t('common.cancel')"
            @confirm="handleDelete(record)"
          >
            <a-button type="link" size="small" danger v-permission="'wf:taskConfig:delete'">
              {{ t('common.delete') }}
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="dialogTitle"
      :loading="saving"
      width="720px"
      @submit="handleSave"
      @cancel="handleDialogCancel"
    >
      <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.list.taskName')" name="taskName">
              <a-input v-model:value="formState.taskName" :placeholder="t('workflow.taskConfig.form.taskName')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.list.taskCode')" name="taskCode">
              <a-input
                v-model:value="formState.taskCode"
                :disabled="Boolean(currentEditor?.hasPublished)"
                :placeholder="t('workflow.taskConfig.form.taskCode')"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.list.interpreterBean')" name="interpreterBean">
              <a-input v-model:value="formState.interpreterBean" :placeholder="t('workflow.taskConfig.form.interpreterBean')" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.formType')" name="formType">
              <a-select v-model:value="formState.formType" :placeholder="t('workflow.taskConfig.form.formType')">
                <a-select-option
                  v-for="item in formTypeSelectOptions"
                  :key="String(item.value)"
                  :value="item.value"
                >
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item v-if="formState.formType === 1" :label="t('workflow.taskConfig.formPath')" name="formPath">
          <a-input v-model:value="formState.formPath" :placeholder="t('workflow.taskConfig.form.formPath')" />
        </a-form-item>

        <a-form-item v-else :label="t('workflow.taskConfig.list.formContentJson')" name="formContent">
          <a-textarea
            v-model:value="formState.formContent"
            :rows="6"
            :placeholder="t('workflow.taskConfig.form.formContent')"
          />
        </a-form-item>

        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.status')" name="status">
              <a-radio-group v-model:value="formState.status">
                <a-radio v-for="item in statusSelectOptions" :key="String(item.value)" :value="item.value">
                  {{ item.label }}
                </a-radio>
              </a-radio-group>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item :label="t('workflow.taskConfig.list.versionInfo')">
              <a-space>
                <a-tag v-if="currentEditor?.publishedVersion" color="blue">
                  {{ t('workflow.taskConfig.list.publishedVersion', { version: currentEditor.publishedVersion }) }}
                </a-tag>
                <a-tag v-if="currentEditor?.draftVersion" color="orange">
                  {{ t('workflow.taskConfig.list.draftVersion', { version: currentEditor.draftVersion }) }}
                </a-tag>
                <span v-if="!currentEditor">{{ t('workflow.taskConfig.list.newDraft') }}</span>
              </a-space>
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item :label="t('workflow.taskConfig.list.remark')" name="remark">
          <a-textarea v-model:value="formState.remark" :rows="3" :placeholder="t('workflow.taskConfig.form.remark')" />
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { useDict } from '@/hooks/useDict'
import {
  deleteTaskConfig,
  getOrCreateDraftEditor,
  getTaskConfigPage,
  publishDraft,
  saveDraftBaseInfo,
  type WfTaskConfigSaveParam,
  type WfTaskConfigSummaryDTO,
  type WfTaskDraftEditorDTO,
} from '@/api/workflow/taskConfig'

type TableRequestPayload = {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}

const router = useRouter()
const route = useRoute()
const { t } = useI18n({ useScope: 'global' })
const { dictItems: statusOptions } = useDict('status')
const { dictItems: formTypeOptions } = useDict('wf_task_form_type')

const tableRef = ref<any>()
const formRef = ref<FormInstance>()
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const currentEditor = ref<WfTaskDraftEditorDTO | null>(null)

const formState = reactive<WfTaskConfigSaveParam>({
  taskName: '',
  taskCode: '',
  interpreterBean: '',
  formType: 1,
  formPath: '',
  formContent: '',
  status: 1,
  remark: '',
})

const dictOptions = computed(() => ({
  status: statusOptions.value || [],
  wf_task_form_type: formTypeOptions.value || [],
  formType: formTypeOptions.value || [],
}))

const statusSelectOptions = computed(() =>
  (statusOptions.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)

const formTypeSelectOptions = computed(() =>
  (formTypeOptions.value || []).map((item: { label: string; value: string | number }) => ({
    label: item.label,
    value: Number(item.value),
  })),
)

const rules = computed(() => ({
  taskName: [{ required: true, message: t('workflow.taskConfig.form.taskName'), trigger: 'blur' }],
  taskCode: [{ required: true, message: t('workflow.taskConfig.form.taskCode'), trigger: 'blur' }],
  formType: [{ required: true, message: t('workflow.taskConfig.form.formType'), trigger: 'change' }],
}))

const dialogTitle = computed(() => (formState.id ? t('workflow.taskConfig.list.editDraftBaseInfo') : t('workflow.taskConfig.list.newDraftFlow')))
const silentErrorConfig = { silentError: true }

function resolveDictLabel(options: Array<{ label: string; value: number }>, value: number | undefined, fallback: string) {
  return options.find(item => item.value === Number(value))?.label || fallback
}

function getFormTypeLabel(value?: number) {
  return resolveDictLabel(
    formTypeSelectOptions.value,
    value,
    value === 2 ? t('workflow.taskConfig.lowCodeForm') : t('workflow.taskConfig.customForm')
  )
}

function getStatusLabel(value?: number) {
  return resolveDictLabel(
    statusSelectOptions.value,
    value,
    value === 1 ? t('workflow.dashboard.enabledText') : t('workflow.dashboard.disabledText')
  )
}

function resetFormState() {
  Object.assign(formState, {
    id: undefined,
    taskName: '',
    taskCode: '',
    interpreterBean: '',
    formType: 1,
    formPath: '',
    formContent: '',
    status: 1,
    remark: '',
  })
  currentEditor.value = null
}

function fillForm(editor: WfTaskDraftEditorDTO) {
  currentEditor.value = editor
  Object.assign(formState, {
    id: editor.draftId,
    taskName: editor.taskName,
    taskCode: editor.taskCode,
    interpreterBean: editor.interpreterBean || '',
    formType: editor.formType,
    formPath: editor.formPath || '',
    formContent: editor.formContent || '',
    status: editor.status,
    remark: editor.remark || '',
  })
}

async function reloadTable() {
  await tableRef.value?.reload?.()
}

const handleRequest = async (payload: TableRequestPayload) => {
  try {
    loading.value = true
    const params: Record<string, any> = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query,
    }

    if (payload.sorter?.field) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }

    const result = await getTaskConfigPage(params as any)
    return {
      records: result.records || [],
      total: Number(result.total || 0),
    }
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.loadListFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

function openCreate() {
  resetFormState()
  dialogVisible.value = true
}

async function openEdit(record: WfTaskConfigSummaryDTO) {
  try {
    const editor = await getOrCreateDraftEditor({ taskCode: record.taskCode }, silentErrorConfig)
    fillForm(editor)
    dialogVisible.value = true
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.loadDraftEditorFailed'))
  }
}

async function handleNodeConfig(record: WfTaskConfigSummaryDTO) {
  try {
    const editor = await getOrCreateDraftEditor({ taskCode: record.taskCode }, silentErrorConfig)
    router.push({
      path: approvalRoutePaths.taskConfigNodes(editor.taskCode),
      query: {
        from: route.fullPath,
      },
    })
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.loadNodeDraftFailed'))
  }
}

async function handlePublish(record: WfTaskConfigSummaryDTO) {
  if (!record.hasDraft) {
    message.info(t('workflow.taskConfig.list.noDraftToPublish'))
    return
  }
  try {
    await publishDraft({ taskCode: record.taskCode })
    await reloadTable()
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.publishFailed'))
  }
}

async function handleDelete(record: WfTaskConfigSummaryDTO) {
  try {
    await deleteTaskConfig({ id: record.draftId || record.publishedId || record.id })
    await reloadTable()
  } catch (error: any) {
    message.error(error.message || t('workflow.taskConfig.list.deleteFailed'))
  }
}

async function handleSave() {
  try {
    await formRef.value?.validate()
    saving.value = true
    const editor = await saveDraftBaseInfo({ ...formState })
    fillForm(editor)
    dialogVisible.value = false
    emit('success')
    await reloadTable()
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || t('workflow.taskConfig.list.saveDraftBaseInfoFailed'))
  } finally {
    saving.value = false
  }
}

/**
 * 处理弹窗取消
 */
function handleDialogCancel() {
  dialogVisible.value = false
}
</script>

<style scoped>
.page-wrap {
  height: 100%;
}

.version-cell {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
}

.version-hint {
  color: #d97706;
  font-size: 12px;
}

.remark-text {
  color: #4b5563;
}
</style>
