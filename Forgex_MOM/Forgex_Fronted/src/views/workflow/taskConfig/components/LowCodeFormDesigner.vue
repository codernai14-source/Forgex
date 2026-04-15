<!--
 * 低代码表单设计器
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-04-15
-->
<template>
  <div class="low-code-designer">
    <div class="low-code-designer__intro">
      <div>
        <h3>{{ t('workflow.taskConfig.lowCodeDesigner.title') }}</h3>
        <p>{{ t('workflow.taskConfig.lowCodeDesigner.desc') }}</p>
      </div>
      <a-space>
        <a-button @click="handlePreview">{{ t('workflow.taskConfig.lowCodeDesigner.preview') }}</a-button>
        <a-button type="primary" @click="handleAddField">{{ t('workflow.taskConfig.lowCodeDesigner.addField') }}</a-button>
      </a-space>
    </div>

    <a-empty v-if="!localSchema.fields.length" :description="t('workflow.taskConfig.lowCodeDesigner.empty')" />

    <div v-else class="low-code-designer__list">
      <div
        v-for="(field, index) in localSchema.fields"
        :key="`${field.key || 'field'}-${index}`"
        class="designer-field-card"
      >
        <div class="designer-field-card__header">
          <strong>{{ field.label || t('workflow.taskConfig.lowCodeDesigner.unnamedField') }}</strong>
          <a-space>
            <a-button size="small" @click="moveField(index, -1)" :disabled="index === 0">{{ t('common.previous') }}</a-button>
            <a-button size="small" @click="moveField(index, 1)" :disabled="index === localSchema.fields.length - 1">{{ t('common.next') }}</a-button>
            <a-button size="small" danger @click="removeField(index)">{{ t('common.delete') }}</a-button>
          </a-space>
        </div>

        <a-row :gutter="12">
          <a-col :span="8">
            <a-form-item :label="t('workflow.taskConfig.lowCodeDesigner.fieldLabel')">
              <a-input :value="field.label" @update:value="updateField(index, 'label', String($event || ''))" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('workflow.taskConfig.lowCodeDesigner.fieldKey')">
              <a-input :value="field.key" @update:value="updateField(index, 'key', normalizeFieldKey(String($event || '')))" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('workflow.taskConfig.lowCodeDesigner.component')">
              <a-select :value="field.component" @update:value="updateField(index, 'component', String($event || 'input'))">
                <a-select-option v-for="item in componentOptions" :key="item.value" :value="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="12">
          <a-col :span="18">
            <a-form-item :label="t('workflow.taskConfig.lowCodeDesigner.placeholder')">
              <a-input :value="field.placeholder" @update:value="updateField(index, 'placeholder', String($event || ''))" />
            </a-form-item>
          </a-col>
          <a-col :span="6">
            <a-form-item :label="t('workflow.taskConfig.lowCodeDesigner.required')">
              <a-switch :checked="Boolean(field.required)" @change="updateField(index, 'required', Boolean($event))" />
            </a-form-item>
          </a-col>
        </a-row>

        <a-form-item
          v-if="field.component === 'select'"
          :label="t('workflow.taskConfig.lowCodeDesigner.options')"
        >
          <a-textarea
            :rows="3"
            :value="formatOptions(field.options)"
            :placeholder="t('workflow.taskConfig.lowCodeDesigner.optionsPlaceholder')"
            @update:value="updateField(index, 'options', parseOptions(String($event || '')))"
          />
        </a-form-item>
      </div>
    </div>

    <a-modal v-model:open="previewVisible" :footer="null" :title="t('workflow.taskConfig.lowCodeDesigner.previewTitle')" width="720px">
      <LowCodeFormRenderer :schema="localSchema" />
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import LowCodeFormRenderer from '@/views/workflow/execution/components/LowCodeFormRenderer.vue'
import {
  DEFAULT_LOW_CODE_FORM_SCHEMA,
  type LowCodeFieldOption,
  type LowCodeFieldSchema,
  type LowCodeFormSchema,
  normalizeLowCodeFormSchema
} from './lowCodeSchema'

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'schema-change', value: LowCodeFormSchema): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const previewVisible = ref(false)
const localSchema = ref<LowCodeFormSchema>(normalizeLowCodeFormSchema(props.modelValue))

const componentOptions = computed(() => [
  { label: t('workflow.taskConfig.lowCodeDesigner.components.input'), value: 'input' },
  { label: t('workflow.taskConfig.lowCodeDesigner.components.textarea'), value: 'textarea' },
  { label: t('workflow.taskConfig.lowCodeDesigner.components.number'), value: 'number' },
  { label: t('workflow.taskConfig.lowCodeDesigner.components.select'), value: 'select' },
  { label: t('workflow.taskConfig.lowCodeDesigner.components.date'), value: 'date' }
])

watch(
  () => props.modelValue,
  value => {
    localSchema.value = normalizeLowCodeFormSchema(value)
  }
)

watch(
  localSchema,
  value => {
    const payload = JSON.stringify(value)
    emit('update:modelValue', payload)
    emit('schema-change', value)
  },
  { deep: true }
)

function handleAddField() {
  localSchema.value = {
    ...localSchema.value,
    fields: [
      ...localSchema.value.fields,
      {
        key: `field_${localSchema.value.fields.length + 1}`,
        label: t('workflow.taskConfig.lowCodeDesigner.defaultFieldLabel', { index: localSchema.value.fields.length + 1 }),
        component: 'input',
        required: false
      }
    ]
  }
}

function updateField<K extends keyof LowCodeFieldSchema>(index: number, key: K, value: LowCodeFieldSchema[K]) {
  const nextFields = [...localSchema.value.fields]
  nextFields[index] = {
    ...nextFields[index],
    [key]: value
  }
  localSchema.value = {
    ...localSchema.value,
    fields: nextFields
  }
}

function moveField(index: number, delta: number) {
  const target = index + delta
  if (target < 0 || target >= localSchema.value.fields.length) {
    return
  }
  const nextFields = [...localSchema.value.fields]
  const [current] = nextFields.splice(index, 1)
  nextFields.splice(target, 0, current)
  localSchema.value = {
    ...localSchema.value,
    fields: nextFields
  }
}

function removeField(index: number) {
  localSchema.value = {
    ...localSchema.value,
    fields: localSchema.value.fields.filter((_, fieldIndex) => fieldIndex !== index)
  }
}

function handlePreview() {
  previewVisible.value = true
}

function normalizeFieldKey(value: string) {
  return value
    .trim()
    .replace(/\s+/g, '_')
    .replace(/[^a-zA-Z0-9_]/g, '_')
    .replace(/_+/g, '_')
}

function formatOptions(options?: LowCodeFieldOption[]) {
  return (options || []).map(option => `${option.label}:${option.value}`).join('\n')
}

function parseOptions(value: string): LowCodeFieldOption[] {
  return value
    .split('\n')
    .map(item => item.trim())
    .filter(Boolean)
    .map(item => {
      const [label, optionValue] = item.split(':')
      return {
        label: String(label || '').trim(),
        value: String(optionValue || label || '').trim()
      }
    })
    .filter(item => item.label && item.value)
}

if (!props.modelValue) {
  emit('update:modelValue', JSON.stringify(DEFAULT_LOW_CODE_FORM_SCHEMA))
}
</script>

<style scoped>
.low-code-designer {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.low-code-designer__intro {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  padding: 18px 20px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 18px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #fff), var(--fx-fill-secondary, #f8fafc));
}

.low-code-designer__intro h3 {
  margin: 0 0 6px;
}

.low-code-designer__intro p {
  margin: 0;
  color: var(--fx-text-secondary, #64748b);
}

.low-code-designer__list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.designer-field-card {
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 18px;
  background: var(--fx-bg-container, #fff);
}

.designer-field-card__header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  align-items: center;
  margin-bottom: 12px;
}
</style>
