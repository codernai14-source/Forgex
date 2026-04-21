<template>
  <div class="leave-form">
    <div class="leave-form__intro">
      <div>
        <p class="leave-form__eyebrow">{{ t('workflow.execution.leaveForm.eyebrow') }}</p>
        <h3>{{ t('workflow.execution.leaveForm.title') }}</h3>
      </div>
      <div class="leave-form__summary">
        <span>{{ t('workflow.execution.leaveForm.summaryDays') }}</span>
        <strong>{{ t('workflow.execution.leaveForm.summaryDaysValue', { days: formState.leaveDays || 0 }) }}</strong>
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
        <a-form-item :label="t('workflow.execution.leaveForm.leaveType')" name="leaveType">
          <a-select
            v-model:value="formState.leaveType"
            :placeholder="t('workflow.execution.leaveForm.leaveTypePlaceholder')"
            :options="leaveTypeOptions"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.execution.leaveForm.startDate')" name="startDate">
          <a-date-picker
            v-model:value="formState.startDate"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            :placeholder="t('workflow.execution.leaveForm.startDatePlaceholder')"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.execution.leaveForm.endDate')" name="endDate">
          <a-date-picker
            v-model:value="formState.endDate"
            style="width: 100%"
            value-format="YYYY-MM-DD"
            format="YYYY-MM-DD"
            :placeholder="t('workflow.execution.leaveForm.endDatePlaceholder')"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.execution.leaveForm.contactPhone')" name="contactPhone">
          <a-input
            v-model:value="formState.contactPhone"
            :placeholder="t('workflow.execution.leaveForm.contactPhonePlaceholder')"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.execution.leaveForm.handoverPerson')" name="handoverPerson">
          <a-input
            v-model:value="formState.handoverPerson"
            :placeholder="t('workflow.execution.leaveForm.handoverPersonPlaceholder')"
          />
        </a-form-item>

        <a-form-item :label="t('workflow.execution.leaveForm.leaveDays')">
          <a-input :value="t('workflow.execution.leaveForm.summaryDaysValue', { days: formState.leaveDays || 0 })" disabled />
        </a-form-item>
      </div>

      <a-form-item :label="t('workflow.execution.leaveForm.reason')" name="reason">
        <a-textarea
          v-model:value="formState.reason"
          :rows="5"
          :placeholder="t('workflow.execution.leaveForm.reasonPlaceholder')"
        />
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
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

const { t } = useI18n({ useScope: 'global' })

const leaveTypeOptions = computed(() => [
  { label: t('workflow.execution.leaveForm.leaveTypes.personal'), value: 'personal' },
  { label: t('workflow.execution.leaveForm.leaveTypes.sick'), value: 'sick' },
  { label: t('workflow.execution.leaveForm.leaveTypes.annual'), value: 'annual' },
  { label: t('workflow.execution.leaveForm.leaveTypes.adjust'), value: 'adjust' },
])

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
  contactPhone: '',
})

const formState = reactive<LeaveFormModel>({
  ...createDefaultState(),
  ...(props.modelValue || {}),
})

const syncingFromParent = ref(false)

const rules = computed(() => ({
  leaveType: [{ required: true, message: t('workflow.execution.leaveForm.leaveTypePlaceholder'), trigger: 'change' }],
  startDate: [{ required: true, message: t('workflow.execution.leaveForm.startDatePlaceholder'), trigger: 'change' }],
  endDate: [{ required: true, message: t('workflow.execution.leaveForm.endDatePlaceholder'), trigger: 'change' }],
  contactPhone: [{ required: true, message: t('workflow.execution.leaveForm.contactPhonePlaceholder'), trigger: 'blur' }],
  reason: [{ required: true, message: t('workflow.execution.leaveForm.reasonPlaceholder'), trigger: 'blur' }],
}))

watch(
  () => props.modelValue,
  (value) => {
    syncingFromParent.value = true
    Object.assign(formState, createDefaultState(), value || {})
    nextTick(() => {
      syncingFromParent.value = false
    })
  },
  { deep: true },
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
  { immediate: true },
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

async function validate() {
  await formRef.value?.validate()

  if (formState.leaveDays <= 0) {
    message.error(t('workflow.execution.leaveForm.dateRangeInvalid'))
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
  reset,
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
  margin: 0 0 8px;
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--fx-text-secondary);
}

.leave-form__intro h3 {
  margin: 0;
  font-size: 24px;
  color: var(--fx-text-color);
}

.leave-form__summary {
  min-width: 180px;
  padding: 16px 18px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(22, 119, 255, 0.12), rgba(82, 196, 26, 0.12));
  text-align: right;
}

.leave-form__summary span {
  display: block;
  margin-bottom: 8px;
  font-size: 13px;
  color: var(--fx-text-secondary);
}

.leave-form__summary strong {
  font-size: 24px;
  color: var(--fx-text-color);
}

.leave-form__body {
  margin-top: 12px;
}

.leave-form__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .leave-form__intro {
    flex-direction: column;
    align-items: stretch;
  }

  .leave-form__summary {
    text-align: left;
  }

  .leave-form__grid {
    grid-template-columns: 1fr;
  }
}
</style>
