<template>
  <div class="low-code-form-renderer">
    <component
      :is="runtimeFormComponent"
      ref="formRef"
      :rule="renderRule"
      :option="renderOption"
      :model-value="innerValue"
      :api="formApi"
      @update:modelValue="handleModelUpdate"
      @update:api="handleApiUpdate"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import formCreate, { type Api } from '@form-create/ant-design-vue'
import type { LowCodeFormSchema } from '@/views/workflow/taskConfig/components/lowCodeSchema'
import { normalizeLowCodeFormSchema } from '@/views/workflow/taskConfig/components/lowCodeSchema'

interface Props {
  schema: LowCodeFormSchema
  modelValue?: Record<string, any>
}

interface Emits {
  (e: 'update:modelValue', value: Record<string, any>): void
}

interface FormCreateComponentExpose {
  fapi?: Api
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({}),
})

const emit = defineEmits<Emits>()
const formRef = ref<FormCreateComponentExpose>()
const formApi = ref<Api>()
const innerValue = ref<Record<string, any>>({})
const FormCreateComponent = (formCreate as any).$form()

const normalizedSchema = computed(() => normalizeLowCodeFormSchema(props.schema))
const runtimeFormComponent = computed(() => FormCreateComponent)
const renderRule = computed(() => normalizedSchema.value.rule || [])
const renderOption = computed(() => ({
  ...(normalizedSchema.value.option || {}),
  submitBtn: false,
  resetBtn: false,
})
)

watch(
  () => props.modelValue,
  value => {
    innerValue.value = { ...(value || {}) }
  },
  { immediate: true, deep: true }
)

function handleModelUpdate(value: Record<string, any>) {
  innerValue.value = { ...(value || {}) }
  emit('update:modelValue', { ...innerValue.value })
}

function handleApiUpdate(api?: Api) {
  if (api) {
    formApi.value = api
  }
}

async function validate() {
  const api = formApi.value || formRef.value?.fapi
  if (api?.validate) {
    await api.validate()
  }
  return { ...innerValue.value }
}

function reset() {
  const api = formApi.value || formRef.value?.fapi
  api?.resetFields?.()
  innerValue.value = {}
  emit('update:modelValue', {})
}

defineExpose({
  validate,
  reset,
})
</script>

<style scoped>
.low-code-form-renderer {
  width: 100%;
}

.low-code-form-renderer :deep(.form-create) {
  width: 100%;
}

.low-code-form-renderer :deep(.ant-form-item:last-child) {
  margin-bottom: 0;
}
</style>
