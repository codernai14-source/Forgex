<template>
  <div class="supplier-review-form">
    <div class="supplier-review-form__header">
      <div>
        <p class="supplier-review-form__eyebrow">{{ t('workflow.supplierReview.eyebrow') }}</p>
        <h3>{{ formState.supplierFullName || t('workflow.supplierReview.title') }}</h3>
        <span>{{ formState.supplierCode || t('workflow.supplierReview.selectSupplier') }}</span>
      </div>
      <a-tag :color="reviewStatusColor(formState.currentReviewStatus)">
        {{ reviewStatusText(formState.currentReviewStatus) }}
      </a-tag>
    </div>

    <a-form
      ref="formRef"
      layout="vertical"
      :model="formState"
      :rules="rules"
      class="supplier-review-form__body"
    >
      <a-form-item v-if="!readonly" :label="t('workflow.supplierReview.pendingSupplier')" name="supplierId">
        <a-select
          v-model:value="formState.supplierId"
          :loading="loadingSuppliers"
          :options="supplierSelectOptions"
          :filter-option="filterSupplierOption"
          show-search
          allow-clear
          :placeholder="t('workflow.supplierReview.selectSupplier')"
          @change="handleSupplierChange"
        />
      </a-form-item>

      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item :label="t('workflow.supplierReview.supplierId')">{{ displayValue(formState.supplierId) }}</a-descriptions-item>
        <a-descriptions-item :label="t('workflow.supplierReview.supplierCode')">{{ displayValue(formState.supplierCode) }}</a-descriptions-item>
        <a-descriptions-item :label="t('workflow.supplierReview.supplierName')">{{ displayValue(formState.supplierFullName) }}</a-descriptions-item>
        <a-descriptions-item :label="t('workflow.supplierReview.reviewStatusLabel')">
          <a-tag :color="reviewStatusColor(formState.currentReviewStatus)">
            {{ reviewStatusText(formState.currentReviewStatus) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <div class="supplier-review-form__section">
        <div class="supplier-review-form__section-title">{{ t('workflow.supplierReview.qualificationSummary') }}</div>
        <a-empty v-if="!qualificationItems.length" :description="t('workflow.supplierReview.noQualification')" />
        <div v-else class="supplier-review-form__summary">
          <a-tag v-for="item in qualificationItems" :key="item">{{ item }}</a-tag>
        </div>
      </div>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import { supplierApi, type Supplier } from '@/api/basic/supplier'

export interface SupplierReviewFormModel {
  supplierId?: number
  supplierCode?: string
  supplierFullName?: string
  qualificationSummary?: string
  currentReviewStatus?: number
}

interface StartWorkflowContext {
  taskCode: string
  formContent: string
  formData: Record<string, any>
  selectedApprovers: number[]
}

interface SelectOption {
  label: string
  value: number
  searchText: string
}

const props = defineProps<{
  modelValue?: Partial<SupplierReviewFormModel>
  readonly?: boolean
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: SupplierReviewFormModel): void
}>()

const { t } = useI18n({ useScope: 'global' })
const readonly = computed(() => Boolean(props.readonly))
const formRef = ref()
const supplierOptions = ref<Supplier[]>([])
const loadingSuppliers = ref(false)
const syncingFromParent = ref(false)

const createDefaultState = (): SupplierReviewFormModel => ({
  supplierId: undefined,
  supplierCode: '',
  supplierFullName: '',
  qualificationSummary: '',
  currentReviewStatus: undefined,
})

const formState = reactive<SupplierReviewFormModel>({
  ...createDefaultState(),
  ...(props.modelValue || {}),
})

const rules = {
  supplierId: [{ required: true, message: t('workflow.supplierReview.selectSupplier'), trigger: 'change' }],
}

const supplierSelectOptions = computed<SelectOption[]>(() => supplierOptions.value
  .filter(item => item.id)
  .map(item => {
    const code = item.supplierCode || '-'
    const name = item.supplierFullName || item.supplierName || '-'
    return {
      label: `${name} (${code})`,
      value: Number(item.id),
      searchText: `${name} ${code}`,
    }
  }))

const qualificationItems = computed(() => splitSummary(formState.qualificationSummary))

watch(
  () => props.modelValue,
  value => {
    syncingFromParent.value = true
    Object.assign(formState, createDefaultState(), value || {})
    nextTick(() => {
      syncingFromParent.value = false
    })
  },
  { deep: true },
)

watch(
  formState,
  () => {
    if (syncingFromParent.value) {
      return
    }
    emit('update:modelValue', { ...formState })
  },
  { deep: true },
)

onMounted(() => {
  if (!readonly.value) {
    loadSupplierOptions()
  }
})

async function loadSupplierOptions() {
  loadingSuppliers.value = true
  try {
    supplierOptions.value = await supplierApi.list({ reviewStatus: 1 })
  } catch (error: any) {
    message.error(error?.message || t('workflow.supplierReview.loadSuppliersFailed'))
  } finally {
    loadingSuppliers.value = false
  }
}

async function handleSupplierChange(value?: number) {
  if (!value) {
    Object.assign(formState, createDefaultState())
    return
  }

  try {
    const detail = await supplierApi.detail({ id: Number(value) })
    applySupplier(detail)
  } catch (error: any) {
    message.error(error?.message || t('workflow.supplierReview.loadSupplierDetailFailed'))
  }
}

function applySupplier(supplier: Supplier) {
  formState.supplierId = supplier.id
  formState.supplierCode = supplier.supplierCode
  formState.supplierFullName = supplier.supplierFullName || supplier.supplierName
  formState.currentReviewStatus = supplier.reviewStatus
  formState.qualificationSummary = buildQualificationSummary(supplier)
}

async function validate() {
  await formRef.value?.validate()
  if (!formState.supplierId) {
    message.warning(t('workflow.supplierReview.selectSupplier'))
    throw new Error('supplier-required')
  }
  return { ...formState }
}

async function startWorkflow(context: StartWorkflowContext) {
  await validate()
  return supplierApi.startReview(Number(formState.supplierId), context.selectedApprovers)
}

function reset() {
  Object.assign(formState, createDefaultState())
  formRef.value?.resetFields()
}

function buildQualificationSummary(supplier: Supplier) {
  const list = supplier.qualificationList || []
  if (!list.length) {
    return t('workflow.supplierReview.noQualification')
  }
  return list
    .map(item => `${item.qualificationType || '-'}/${item.certificateNo || '-'}/${item.valid === false ? t('workflow.supplierReview.invalid') : t('workflow.supplierReview.valid')}`)
    .join('; ')
}

function splitSummary(value?: string) {
  return String(value || '')
    .split(';')
    .map(item => item.trim())
    .filter(item => item && item !== t('workflow.supplierReview.noQualification'))
}

function filterSupplierOption(input: string, option?: SelectOption) {
  return String(option?.searchText || '').toLowerCase().includes(input.toLowerCase())
}

function displayValue(value?: string | number) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function reviewStatusText(value?: number) {
  const map: Record<number, string> = {
    0: t('workflow.supplierReview.reviewStatus.noReviewRequired'),
    1: t('workflow.supplierReview.reviewStatus.notReviewed'),
    2: t('workflow.supplierReview.reviewStatus.reviewing'),
    3: t('workflow.supplierReview.reviewStatus.reviewed'),
  }
  return map[Number(value)] || '-'
}

function reviewStatusColor(value?: number) {
  const map: Record<number, string> = {
    0: 'default',
    1: 'orange',
    2: 'processing',
    3: 'green',
  }
  return map[Number(value)] || 'default'
}

defineExpose({
  validate,
  reset,
  startWorkflow,
})
</script>

<style scoped lang="less" src="@/styles/views/workflow/form/supplier-review-form.less"></style>
