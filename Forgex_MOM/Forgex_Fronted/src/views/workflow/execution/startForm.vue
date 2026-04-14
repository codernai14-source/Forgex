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

            <div v-else-if="taskConfig?.formType === 2" class="lowcode-form">
              <a-alert
                type="info"
                show-icon
                :message="t('workflow.execution.startForm.lowCodeContent')"
                :description="t('workflow.execution.startForm.lowCodeDesc')"
              />
              <a-textarea
                v-model:value="lowCodeFormText"
                :rows="12"
                :placeholder="t('workflow.execution.startForm.lowCodeJsonPlaceholder')"
              />
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

interface DynamicFormExpose {
  validate?: () => Promise<Record<string, any> | void>
  reset?: () => void
}

const route = useRoute()
const router = useRouter()
const { t } = useI18n({ useScope: 'global' })

const taskConfig = ref<WfTaskConfigDTO | null>(null)
const loading = ref(false)
const submitting = ref(false)
const errorMessage = ref('')
const customFormData = ref<Record<string, any>>({})
const lowCodeFormText = ref('{}')
const dynamicFormRef = ref<DynamicFormExpose>()

const taskCode = computed(() => String(route.params.taskCode || '').trim())
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
    lowCodeFormText.value = result.formContent || '{}'

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
  lowCodeFormText.value = taskConfig.value?.formContent || '{}'
  dynamicFormRef.value?.reset?.()
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
      JSON.parse(lowCodeFormText.value)
      formContent = lowCodeFormText.value
    }

    submitting.value = true
    const params: WfExecutionStartParam = {
      taskCode: taskConfig.value.taskCode,
      formContent
    }
    const executionId = await startExecution(params)
    navigateAndCloseCurrent(approvalRoutePaths.myInitiated, { executionId })
  } catch (error: any) {
    if (error?.message?.includes('JSON')) {
      message.error(t('workflow.execution.startForm.invalidLowCodeJson'))
      return
    }
    if (error?.errorFields) {
      return
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadTaskConfig()
})
</script>

<style scoped>
.start-form-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 16px;
}

.page-head,
.form-shell {
  border-radius: 22px;
  border: 1px solid var(--fx-border-color);
  background: var(--fx-bg-container);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
}

.page-head {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
}

.page-head h2 {
  margin: 8px 0 6px;
  font-size: 26px;
  color: var(--fx-text-primary);
}

.page-head p,
.page-head__back {
  margin: 0;
  padding: 0;
  color: var(--fx-text-secondary);
}

.error-alert {
  background: var(--fx-bg-container);
}

.form-shell__inner {
  padding: 24px;
}

.form-shell__summary {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.summary-item {
  padding: 14px 16px;
  border-radius: 16px;
  background: var(--fx-fill-alter);
}

.summary-item span {
  display: block;
  margin-bottom: 8px;
  font-size: 12px;
  color: var(--fx-text-tertiary);
}

.summary-item strong {
  color: var(--fx-text-primary);
  line-height: 1.5;
}

.form-shell__body {
  min-height: 240px;
}

.lowcode-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.form-shell__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 20px;
}

@media (max-width: 960px) {
  .page-head,
  .form-shell__summary {
    grid-template-columns: 1fr;
    flex-direction: column;
  }
}
</style>
