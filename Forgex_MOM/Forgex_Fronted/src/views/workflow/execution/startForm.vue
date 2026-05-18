<template>
  <div class="start-form-page">
    <div class="page-head">
      <div>
        <a-button type="link" class="page-head__back" @click="navigateAndCloseCurrent(approvalRoutePaths.executionStartList)">
          {{ t('workflow.execution.startForm.backToList') }}
        </a-button>
        <h2>{{ taskConfig?.taskName || t('workflow.execution.startForm.titleFallback') }}</h2>
        <p>{{ taskConfig?.remark || t('workflow.execution.startForm.descFallback') }}</p>
      </div>

      <a-space>
        <a-tag v-if="taskConfig" :color="taskConfig.formType === 1 ? 'blue' : 'green'">
          {{ taskConfig.formType === 1 ? t('workflow.execution.startForm.customForm') : t('workflow.execution.startForm.lowCodeForm') }}
        </a-tag>
        <a-tag v-if="taskConfig" color="gold">{{ taskConfig.taskCode }}</a-tag>
      </a-space>
    </div>

    <a-alert
      v-if="errorMessage"
      type="error"
      show-icon
      :message="errorMessage"
      class="error-alert"
    >
      <template #description>
        <a-button type="link" @click="navigateAndCloseCurrent(approvalRoutePaths.executionStartList)">{{ t('workflow.execution.startForm.backToSelection') }}</a-button>
      </template>
    </a-alert>

    <div v-else class="form-shell">
      <a-spin :spinning="loading">
        <div class="form-shell__inner">
          <div class="form-shell__summary">
            <div class="summary-item">
              <span>{{ t('workflow.execution.startForm.summaryTaskName') }}</span>
              <strong>{{ taskConfig?.taskName }}</strong>
            </div>
            <div class="summary-item">
              <span>{{ t('workflow.execution.startForm.summaryTaskCode') }}</span>
              <strong>{{ taskConfig?.taskCode }}</strong>
            </div>
            <div class="summary-item">
              <span>{{ t('workflow.execution.startForm.summaryRemark') }}</span>
              <strong>{{ taskConfig?.remark || t('workflow.execution.startForm.summaryRemarkEmpty') }}</strong>
            </div>
          </div>

          <div class="form-shell__body">
            <component
              :is="dynamicFormComponent"
              v-if="taskConfig?.formType === 1 && dynamicFormComponent"
              ref="dynamicFormRef"
              v-model="customFormData"
            />

            <a-alert
              v-else-if="taskConfig?.formType === 1"
              type="warning"
              show-icon
              :message="t('workflow.execution.startForm.unregisteredFormComponent')"
              :description="taskConfig?.formPath || ''"
            />

            <LowCodeFormRenderer
              v-else-if="taskConfig?.formType === 2 && lowCodeSchema.fields.length"
              ref="lowCodeFormRef"
              v-model="lowCodeFormData"
              :schema="lowCodeSchema"
            />

            <a-alert
              v-else-if="taskConfig?.formType === 2"
              type="warning"
              show-icon
              :message="t('workflow.execution.startForm.lowCodeSchemaEmpty')"
            />

            <div v-if="needsSelectedApprovers" class="selected-approver-panel">
              <div class="selected-approver-panel__title">{{ t('workflow.execution.startForm.selectedApprovers') }}</div>
              <ReceiverSelector v-model:modelValue="selectedApproverModel" />
            </div>
          </div>

          <div class="form-shell__footer">
            <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
            <a-button type="primary" :loading="submitting" @click="handleSubmit">
              {{ t('workflow.execution.startForm.submit') }}
            </a-button>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { type LocationQueryRaw, useRoute, useRouter } from 'vue-router'
import { approvalRoutePaths, TAB_CLOSE_QUERY_KEY } from '@/router/approvalRoutePaths'
import { startExecution, type WfExecutionStartParam } from '@/api/workflow/execution'
import { getTaskConfigByCode, type WfTaskConfigDTO } from '@/api/workflow/taskConfig'
import { workflowFormRegistry } from './formRegistry'
import LowCodeFormRenderer from './components/LowCodeFormRenderer.vue'
import { normalizeLowCodeFormSchema } from '../taskConfig/components/lowCodeSchema'
import ReceiverSelector from '@/components/common/ReceiverSelector.vue'

interface DynamicFormExpose {
  validate?: () => Promise<Record<string, any> | void>
  reset?: () => void
  startWorkflow?: (context: {
    taskCode: string
    formContent: string
    formData: Record<string, any>
    selectedApprovers: number[]
  }) => Promise<number | void>
}

interface ReceiverModel {
  receiverType?: string
  receiverIds: string[]
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n({ useScope: 'global' })

const taskConfig = ref<WfTaskConfigDTO | null>(null)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const customFormData = ref<Record<string, any>>({})
const lowCodeFormData = ref<Record<string, any>>({})
const dynamicFormRef = ref<DynamicFormExpose>()
const lowCodeFormRef = ref<DynamicFormExpose>()
const selectedApproverModel = ref<ReceiverModel>({ receiverType: 'USER', receiverIds: [] })

const taskCode = computed(() => String(route.params.taskCode || '').trim())
const lowCodeSchema = computed(() => normalizeLowCodeFormSchema(taskConfig.value?.formContent))
const needsSelectedApprovers = computed(() => Boolean(taskConfig.value?.requiresSelectedApprovers))
const dynamicFormComponent = computed(() => {
  if (!taskConfig.value?.formPath) {
    return null
  }
  return workflowFormRegistry[taskConfig.value.formPath] || null
})

function navigateAndCloseCurrent(path: string, query: LocationQueryRaw = {}) {
  router.push({
    path,
    query: {
      ...query,
      [TAB_CLOSE_QUERY_KEY]: route.path,
    }
  })
}

async function loadTaskConfig() {
  if (!taskCode.value) {
    errorMessage.value = t('workflow.execution.startForm.missingTaskCode')
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''
    const result = await getTaskConfigByCode({ taskCode: taskCode.value })
    taskConfig.value = result
    lowCodeFormData.value = {}

    if (result.formType === 1 && result.formPath && !workflowFormRegistry[result.formPath]) {
      errorMessage.value = `${t('workflow.execution.startForm.unregisteredFormComponent')}：${result.formPath}`
    }
  } catch (error: any) {
    errorMessage.value = error.message || t('workflow.execution.startForm.taskNotFound')
  } finally {
    loading.value = false
  }
}

function handleReset() {
  customFormData.value = {}
  lowCodeFormData.value = {}
  selectedApproverModel.value = { receiverType: 'USER', receiverIds: [] }
  dynamicFormRef.value?.reset?.()
  lowCodeFormRef.value?.reset?.()
}

async function handleSubmit() {
  if (!taskConfig.value) {
    return
  }

  try {
    let formContent = ''
    if (taskConfig.value.formType === 1) {
      if (!dynamicFormComponent.value) {
        message.error(t('workflow.execution.startForm.missingFrontendForm'))
        return
      }

      const validatedFormData = await dynamicFormRef.value?.validate?.()
      const payload = validatedFormData || customFormData.value
      if (!payload || typeof payload !== 'object' || Object.keys(payload).length === 0) {
        message.warning(t('workflow.execution.startForm.fillFormFirst'))
        return
      }
      customFormData.value = payload
      formContent = JSON.stringify(payload)
    } else {
      const payload = await lowCodeFormRef.value?.validate?.()
      formContent = JSON.stringify(payload || lowCodeFormData.value || {})
    }

    submitting.value = true
    const params: WfExecutionStartParam = {
      taskCode: taskConfig.value.taskCode,
      formContent,
      selectedApprovers: selectedApproverModel.value.receiverIds
        .map(item => Number(item))
        .filter(item => Number.isFinite(item) && item > 0)
    }
    if (needsSelectedApprovers.value && !params.selectedApprovers?.length) {
      message.warning(t('workflow.execution.startForm.selectAtLeastOneApprover'))
      return
    }
    const executionId = dynamicFormRef.value?.startWorkflow
      ? await dynamicFormRef.value.startWorkflow({
        taskCode: params.taskCode,
        formContent: params.formContent,
        formData: customFormData.value,
        selectedApprovers: params.selectedApprovers || [],
      })
      : await startExecution(params)
    navigateAndCloseCurrent(approvalRoutePaths.myInitiated, executionId ? { executionId } : {})
  } catch (error: any) {
    if (error?.errorFields) {
      return
    }
    message.error(error.message || t('workflow.execution.startForm.submitFailed'))
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTaskConfig()
})
</script>

<style scoped lang="less" src="@/styles/views/workflow/execution/start-form.less"></style>
