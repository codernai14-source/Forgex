<template>
  <div class="leave-form">
    <div class="leave-form__intro">
      <div>
        <p class="leave-form__eyebrow">Leave Demo</p>
        <h3>请假申请单</h3>
      </div>
      <div class="leave-form__summary">
        <span>预计请假</span>
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
        <a-form-item label="请假类型" name="leaveType">
          <a-select
            v-model:value="formState.leaveType"
            placeholder="请选择请假类型"
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

        <a-form-item label="结束日期" name="endDate">
          <a-date-picker
            v-model:value="formState.endDate"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            placeholder="请选择结束日期"
          />
        </a-form-item>

        <a-form-item label="紧急联系电话" name="contactPhone">
          <a-input
            v-model:value="formState.contactPhone"
            placeholder="请输入联系手机号"
          />
        </a-form-item>

        <a-form-item label="工作交接人" name="handoverPerson">
          <a-input
            v-model:value="formState.handoverPerson"
            placeholder="请输入交接人"
          />
        </a-form-item>

        <a-form-item label="请假天数">
          <a-input :value="`${formState.leaveDays || 0} 天`" disabled />
        </a-form-item>
      </div>

      <a-form-item label="请假原因" name="reason">
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
import { reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'

export interface LeaveFormModel {
  leaveType: string
  startDate: string
  endDate: string
  leaveDays: number
  reason: string
  handoverPerson: string
  contactPhone: string
}

const leaveTypeOptions = [
  { label: '事假', value: 'personal' },
  { label: '病假', value: 'sick' },
  { label: '年假', value: 'annual' },
  { label: '调休', value: 'adjust' }
]

const props = defineProps<{
  modelValue?: Partial<LeaveFormModel>
}>()

const emit = defineEmits<{
  (event: 'update:modelValue', value: LeaveFormModel): void
}>()

const formRef = ref()

const createDefaultState = (): LeaveFormModel => ({
  leaveType: '',
  startDate: '',
  endDate: '',
  leaveDays: 0,
  reason: '',
  handoverPerson: '',
  contactPhone: ''
})

const formState = reactive<LeaveFormModel>({
  ...createDefaultState(),
  ...(props.modelValue || {})
})

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startDate: [{ required: true, message: '请选择开始日期', trigger: 'change' }],
  endDate: [{ required: true, message: '请选择结束日期', trigger: 'change' }],
  contactPhone: [{ required: true, message: '请输入联系电话', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入请假原因', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  value => {
    Object.assign(formState, createDefaultState(), value || {})
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
  border: 1px solid rgba(255, 255, 255, 0.06);
  border-radius: 20px;
  background:
    radial-gradient(circle at top right, rgba(245, 166, 35, 0.14), transparent 30%),
    linear-gradient(180deg, rgba(31, 38, 52, 0.94), rgba(20, 24, 33, 0.96));
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
  color: rgba(255, 255, 255, 0.52);
}

.leave-form__intro h3 {
  margin: 0;
  font-size: 22px;
  color: #ffffff;
}

.leave-form__summary {
  min-width: 120px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.05);
  text-align: right;
}

.leave-form__summary span {
  display: block;
  margin-bottom: 6px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.52);
}

.leave-form__summary strong {
  font-size: 24px;
  color: #ffcb7a;
}

.leave-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

.leave-form__body :deep(.ant-form-item-label > label) {
  color: rgba(255, 255, 255, 0.82);
}

.leave-form__body :deep(.ant-input),
.leave-form__body :deep(.ant-input-number),
.leave-form__body :deep(.ant-picker),
.leave-form__body :deep(.ant-select-selector),
.leave-form__body :deep(.ant-input-affix-wrapper) {
  border-color: rgba(255, 255, 255, 0.08);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.03);
}

.leave-form__body :deep(.ant-input),
.leave-form__body :deep(.ant-picker input),
.leave-form__body :deep(.ant-select-selection-item),
.leave-form__body :deep(.ant-select-selection-placeholder),
.leave-form__body :deep(.ant-input:disabled) {
  color: #eef3ff;
}

.leave-form__body :deep(.ant-input-disabled) {
  background: rgba(255, 255, 255, 0.02);
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
