<template>
  <div class="leave-form">
    <div class="leave-form__intro">
      <div>
        <p class="leave-form__eyebrow">Leave Demo</p>
        <h3>请假申请单</h3>
      </div>
      <div class="leave-form__summary">
        <span>棰勮璇峰亣</span>
        <strong>{{ formState.leaveDays || 0 }} 天</strong>
      </div>
    </div>

    <a-form
      ref="formRef"
      layout="vertical"
      :model="formState"
      :rules="rules"
      class="leave-form__body"
    >
      <div class="leave-form__grid">
        <a-form-item label="璇峰亣绫诲瀷" name="leaveType">
          <a-select
            v-model:value="formState.leaveType"
            placeholder="璇烽€夋嫨璇峰亣绫诲瀷"
            :options="leaveTypeOptions"
          />
        </a-form-item>

        <a-form-item label="开始日期" name="startDate">
          <a-date-picker
            v-model:value="formState.startDate"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="请选择开始日期"
          />
        </a-form-item>

        <a-form-item label="缁撴潫鏃ユ湡" name="endDate">
          <a-date-picker
            v-model:value="formState.endDate"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="璇烽€夋嫨缁撴潫鏃ユ湡"
          />
        </a-form-item>

        <a-form-item label="紧急联系电话" name="contactPhone">
          <a-input
            v-model:value="formState.contactPhone"
            placeholder="璇疯緭鍏ヨ仈绯绘墜鏈哄彿"
          />
        </a-form-item>

        <a-form-item label="工作交接人" name="handoverPerson">
          <a-input
            v-model:value="formState.handoverPerson"
            placeholder="璇疯緭鍏ヤ氦鎺ヤ汉"
          />
        </a-form-item>

        <a-form-item label="璇峰亣澶╂暟">
          <a-input :value="`${formState.leaveDays || 0} 天`" disabled />
        </a-form-item>
      </div>

      <a-form-item label="璇峰亣鍘熷洜" name="reason">
        <a-textarea
          v-model:value="formState.reason"
          :rows="5"
          placeholder="请输入请假原因，建议补充时间安排和工作交接情况"
        />
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

export interface Leave表单Model {
  leaveType: string
  startDate: string
  endDate: string
  leaveDays: number
  reason: string
  handoverPerson: string
  contactPhone: string
}

const leaveTypeOptions = [
  { label: '浜嬪亣', value: 'personal' },
  { label: '鐥呭亣', value: 'sick' },
  { label: '骞村亣', value: 'annual' },
  { label: '璋冧紤', value: 'adjust' }
]

const props = defineProps<{
  modelValue?: Partial<Leave表单Model>
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: Leave表单Model): void
}>()

const formRef = ref()

const createDefaultState = (): Leave表单Model => ({
  leaveType: '',
  startDate: '',
  endDate: '',
  leaveDays: 0,
  reason: '',
  handoverPerson: '',
  contactPhone: ''
})

const formState = reactive<Leave表单Model>({
  ...createDefaultState(),
  ...(props.modelValue || {})
})

/**
 * 涓?true 鏃惰〃绀烘浠庣埗缁勪欢鍚屾 props锛岄伩鍏?v-model 涓?props 浜掔浉瑙﹀彂閫犳垚閫掑綊鏇存柊锛圓nt Design Vue Spin 浼氭姤 Maximum recursive updates锛夈€?
 */
const syncingFromParent = ref(false)

const rules = {
  leaveType: [{ required: true, message: '璇烽€夋嫨璇峰亣绫诲瀷', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '璇烽€夋嫨缁撴潫鏃ユ湡', trigger: 'change' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  value => {
    syncingFromParent.value = true
    Object.assign(formState, createDefaultState(), value || {})
    nextTick(() => {
      syncingFromParent.value = false
    })
  },
  { deep: true }
)

watch(
  () => [formState.startDate, formState.endDate],
  () => {
    if (formState.startDate && formState.endDate) {
      const start = dayjs(formState.startDate)
      const end = dayjs(formState.endDate)

      if (end.isBefore(start, 'day')) {
        formState.leaveDays = 0
        return
      }

      formState.leaveDays = end.diff(start, 'day') + 1
      return
    }

    formState.leaveDays = 0
  },
  { immediate: true }
)

watch(
  formState,
  () => {
    if (syncingFromParent.value) {
      return
    }
    emit('update:modelValue', { ...formState })
  },
  { deep: true }
)

async function validate() {
  await formRef.value?.validate()

  if (formState.leaveDays <= 0) {
    message.error('结束日期不能早于开始日期')
    throw new Error('leave-date-range-invalid')
  }

  return { ...formState }
}

function reset() {
  Object.assign(formState, createDefaultState())
  formRef.value?.resetFields()
}

defineExpose({
  validate,
  reset
})
</script>

<style scoped>
.leave-form {
  padding: 20px;
  border: 1px solid var(--fx-border-color);
  border-radius: 20px;
  background: var(--fx-bg-container);
}

.leave-form__intro {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
}

.leave-form__eyebrow {
  margin: 0 0 6px;
  font-size: 12px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--fx-text-tertiary);
}

.leave-form__intro h3 {
  margin: 0;
  font-size: 22px;
  color: var(--fx-text-primary);
}

.leave-form__summary {
  min-width: 120px;
  padding: 12px 14px;
  border-radius: 16px;
  background: var(--fx-fill-alter);
  text-align: right;
}

.leave-form__summary span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: var(--fx-text-tertiary);
}

.leave-form__summary strong {
  font-size: 24px;
  color: var(--fx-primary);
}

.leave-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.leave-form__body :deep(.ant-form-item-label > label) {
  color: var(--fx-text-secondary);
}

.leave-form__body :deep(.ant-input),
.leave-form__body :deep(.ant-input-number),
.leave-form__body :deep(.ant-picker),
.leave-form__body :deep(.ant-select-selector),
.leave-form__body :deep(.ant-input-affix-wrapper) {
  border-color: var(--fx-border-color);
  border-radius: 12px;
  background: var(--fx-bg-elevated);
}

.leave-form__body :deep(.ant-input),
.leave-form__body :deep(.ant-picker input),
.leave-form__body :deep(.ant-select-selection-item),
.leave-form__body :deep(.ant-select-selection-placeholder),
.leave-form__body :deep(.ant-input:disabled) {
  color: var(--fx-text-primary);
}

.leave-form__body :deep(.ant-input-disabled) {
  background: var(--fx-fill-alter);
}

@media (max-width: 768px) {
  .leave-form {
    padding: 16px;
  }

  .leave-form__intro,
  .leave-form__grid {
    grid-template-columns: 1fr;
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
