<template>
  <div class="supplier-review-form">
    <div class="supplier-review-form__header">
      <div>
        <p class="supplier-review-form__eyebrow">Supplier Review</p>
        <h3>{{ formState.supplierFullName || '供应商资质审查' }}</h3>
        <span>{{ formState.supplierCode || '请选择待审供应商' }}</span>
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
      <a-form-item v-if="!readonly" label="待审供应商" name="supplierId">
        <a-select
          v-model:value="formState.supplierId"
          :loading="loadingSuppliers"
          :options="supplierSelectOptions"
          :filter-option="filterSupplierOption"
          show-search
          allow-clear
          placeholder="请选择待审供应商"
          @change="handleSupplierChange"
        />
      </a-form-item>

      <a-descriptions bordered :column="2" size="small">
        <a-descriptions-item label="供应商ID">{{ displayValue(formState.supplierId) }}</a-descriptions-item>
        <a-descriptions-item label="供应商编码">{{ displayValue(formState.supplierCode) }}</a-descriptions-item>
        <a-descriptions-item label="供应商名称">{{ displayValue(formState.supplierFullName) }}</a-descriptions-item>
        <a-descriptions-item label="审查状态">
          <a-tag :color="reviewStatusColor(formState.currentReviewStatus)">
            {{ reviewStatusText(formState.currentReviewStatus) }}
          </a-tag>
        </a-descriptions-item>
      </a-descriptions>

      <div class="supplier-review-form__section">
        <div class="supplier-review-form__section-title">资质摘要</div>
        <a-empty v-if="!qualificationItems.length" description="暂无资质" />
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
  supplierId: [{ required: true, message: '请选择待审供应商', trigger: 'change' }],
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
    message.error(error?.message || '加载待审供应商失败')
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
    message.error(error?.message || '加载供应商详情失败')
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
    message.warning('请选择待审供应商')
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
    return '暂无资质'
  }
  return list
    .map(item => `${item.qualificationType || '-'}/${item.certificateNo || '-'}/${item.valid === false ? '无效' : '有效'}`)
    .join('; ')
}

function splitSummary(value?: string) {
  return String(value || '')
    .split(';')
    .map(item => item.trim())
    .filter(item => item && item !== '暂无资质')
}

function filterSupplierOption(input: string, option?: SelectOption) {
  return String(option?.searchText || '').toLowerCase().includes(input.toLowerCase())
}

function displayValue(value?: string | number) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function reviewStatusText(value?: number) {
  const map: Record<number, string> = {
    0: '无需审查',
    1: '未审查',
    2: '审查中',
    3: '已审查',
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

<style scoped>
.supplier-review-form {
  padding: 20px;
  border: 1px solid var(--fx-border-color);
  border-radius: 20px;
  background: var(--fx-bg-container);
}

.supplier-review-form__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.supplier-review-form__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  color: var(--fx-text-secondary);
  text-transform: uppercase;
}

.supplier-review-form__header h3 {
  margin: 0 0 6px;
  font-size: 24px;
  color: var(--fx-text-color);
}

.supplier-review-form__header span {
  color: var(--fx-text-secondary);
}

.supplier-review-form__body {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.supplier-review-form__section {
  padding: 16px;
  border: 1px solid var(--fx-border-color);
  border-radius: 12px;
  background: var(--fx-fill-alter);
}

.supplier-review-form__section-title {
  margin-bottom: 12px;
  font-weight: 600;
  color: var(--fx-text-primary);
}

.supplier-review-form__summary {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

@media (max-width: 768px) {
  .supplier-review-form__header {
    flex-direction: column;
  }
}
</style>
