<!--
 * 低代码表单渲染器
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-04-15
-->
<template>
  <a-form ref="formRef" :model="innerValue" layout="vertical">
    <a-row :gutter="16">
      <a-col v-for="field in schema.fields" :key="field.key" :span="field.component === 'textarea' ? 24 : 12">
        <a-form-item :label="field.label || field.key" :name="field.key" :rules="buildRules(field)">
          <a-input
            v-if="field.component === 'input'"
            v-model:value="innerValue[field.key]"
            :placeholder="field.placeholder"
          />
          <a-textarea
            v-else-if="field.component === 'textarea'"
            v-model:value="innerValue[field.key]"
            :rows="4"
            :placeholder="field.placeholder"
          />
          <a-input-number
            v-else-if="field.component === 'number'"
            v-model:value="innerValue[field.key]"
            :placeholder="field.placeholder"
            style="width: 100%"
          />
          <a-select
            v-else-if="field.component === 'select'"
            v-model:value="innerValue[field.key]"
            :placeholder="field.placeholder"
            allow-clear
          >
            <a-select-option v-for="option in field.options || []" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
          <a-date-picker
            v-else-if="field.component === 'date'"
            v-model:value="innerValue[field.key]"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
          <a-input
            v-else
            v-model:value="innerValue[field.key]"
            :placeholder="field.placeholder"
          />
        </a-form-item>
      </a-col>
    </a-row>
  </a-form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import type { LowCodeFieldSchema, LowCodeFormSchema } from '@/views/workflow/taskConfig/components/lowCodeSchema'

interface Props {
  schema: LowCodeFormSchema
  modelValue?: Record<string, any>
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, any>): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({})
})

const emit = defineEmits<Emits>()
const formRef = ref<FormInstance>()
const innerValue = ref<Record<string, any>>({})

watch(
  () => [props.schema, props.modelValue],
  () => {
    const nextValue: Record<string, any> = {}
    props.schema.fields.forEach(field => {
      nextValue[field.key] = props.modelValue?.[field.key] ?? field.defaultValue ?? undefined
    })
    innerValue.value = nextValue
  },
  { immediate: true, deep: true }
)

watch(
  innerValue,
  value => {
    emit('update:modelValue', { ...value })
  },
  { deep: true }
)

function buildRules(field: LowCodeFieldSchema) {
  return field.required
    ? [{ required: true, message: `${field.label || field.key}不能为空`, trigger: 'change' }]
    : []
}

async function validate() {
  await formRef.value?.validate()
  return { ...innerValue.value }
}

function reset() {
  const nextValue: Record<string, any> = {}
  props.schema.fields.forEach(field => {
    nextValue[field.key] = field.defaultValue ?? undefined
  })
  innerValue.value = nextValue
}

defineExpose({
  validate,
  reset
})
</script>
