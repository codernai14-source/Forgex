<template>
  <div class="workflow-form-preview">
    <component
      :is="dynamicFormComponent"
      v-if="isCustomForm && dynamicFormComponent"
      v-model="formModel"
      readonly
    />

    <LowCodeFormRenderer
      v-else-if="isLowCodeForm && hasLowCodeSchema"
      v-model="formModel"
      :schema="lowCodeSchema"
      readonly
    />

    <a-alert
      v-else-if="isCustomForm"
      type="warning"
      show-icon
      :message="t('workflow.myTask.unregisteredFormComponent')"
      :description="record?.formPath || '-'"
    />

    <a-alert
      v-else-if="isLowCodeForm"
      type="warning"
      show-icon
      :message="t('workflow.myTask.lowCodeSchemaMissing')"
    />

    <div v-else class="json-fallback">
      <a-alert
        type="info"
        show-icon
        :message="t('workflow.myTask.formPreviewFallback')"
        class="json-fallback__alert"
      />
      <pre>{{ formattedFormContent }}</pre>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { WfExecutionDTO } from '@/api/workflow/execution'
import { getTaskConfig, getTaskConfigByCode, type WfTaskConfigDTO } from '@/api/workflow/taskConfig'
import LowCodeFormRenderer from '@/views/workflow/execution/components/LowCodeFormRenderer.vue'
import { workflowFormRegistry } from '@/views/workflow/execution/formRegistry'
import { normalizeLowCodeFormSchema } from '@/views/workflow/taskConfig/components/lowCodeSchema'

interface Props {
  record?: WfExecutionDTO | null
}

const props = defineProps<Props>()
const { t } = useI18n({ useScope: 'global' })
const formModel = ref<Record<string, any>>({})
const fallbackTaskConfig = ref<WfTaskConfigDTO | null>(null)

const resolvedFormType = computed(() => props.record?.formType ?? fallbackTaskConfig.value?.formType)
const resolvedFormPath = computed(() => props.record?.formPath || fallbackTaskConfig.value?.formPath)
const resolvedTaskFormContent = computed(() => props.record?.taskFormContent || fallbackTaskConfig.value?.formContent)
const formType = computed(() => Number(resolvedFormType.value || 0))
const isCustomForm = computed(() => formType.value === 1)
const isLowCodeForm = computed(() => formType.value === 2)
const lowCodeSchema = computed(() => normalizeLowCodeFormSchema(resolvedTaskFormContent.value))
const hasLowCodeSchema = computed(() => lowCodeSchema.value.fields.length > 0 || lowCodeSchema.value.rule.length > 0)
const dynamicFormComponent = computed(() => {
  const formPath = resolvedFormPath.value
  return formPath ? workflowFormRegistry[formPath] || null : null
})
const formattedFormContent = computed(() => {
  if (!props.record?.formContent) {
    return '{}'
  }
  try {
    return JSON.stringify(JSON.parse(props.record.formContent), null, 2)
  } catch {
    return props.record.formContent
  }
})

watch(
  () => props.record?.formContent,
  value => {
    formModel.value = parseFormContent(value)
  },
  { immediate: true },
)

watch(
  () => [props.record?.id, props.record?.taskConfigId, props.record?.taskCode, props.record?.formType, props.record?.formPath, props.record?.taskFormContent],
  async () => {
    fallbackTaskConfig.value = null
    if (resolvedFormType.value) {
      return
    }
    fallbackTaskConfig.value = await loadTaskConfigFallback()
  },
  { immediate: true },
)

function parseFormContent(value?: string): Record<string, any> {
  if (!value) {
    return {}
  }
  try {
    const parsed = JSON.parse(value)
    return parsed && typeof parsed === 'object' && !Array.isArray(parsed) ? parsed : {}
  } catch {
    return {}
  }
}

async function loadTaskConfigFallback(): Promise<WfTaskConfigDTO | null> {
  if (props.record?.taskConfigId) {
    try {
      return await getTaskConfig({ id: props.record.taskConfigId })
    } catch {
      // ignore and fallback to taskCode
    }
  }

  if (props.record?.taskCode) {
    try {
      return await getTaskConfigByCode({ taskCode: props.record.taskCode })
    } catch {
      return null
    }
  }

  return null
}
</script>

<style scoped>
.workflow-form-preview {
  min-height: 240px;
}

.json-fallback {
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: var(--fx-radius, 8px);
  background: var(--fx-fill-secondary, #f5f5f5);
}

.json-fallback__alert {
  margin-bottom: 12px;
}

.json-fallback pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  color: var(--fx-text-primary, rgba(0, 0, 0, 0.88));
  font-family: var(--fx-font-family-mono, 'Courier New', Courier, monospace);
  font-size: 12px;
  line-height: 1.6;
}
</style>
