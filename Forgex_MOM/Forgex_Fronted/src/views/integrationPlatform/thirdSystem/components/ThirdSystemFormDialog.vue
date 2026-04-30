<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('integration.thirdSystem.edit') : t('integration.thirdSystem.add')"
    :loading="loading"
    width="720px"
    @submit="handleSubmit"
  >
    <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('integration.thirdSystem.systemCode')" name="systemCode">
            <a-input
              v-model:value="formState.systemCode"
              :disabled="isEdit"
              :placeholder="t('integration.thirdSystem.form.systemCode')"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item :label="t('integration.thirdSystem.systemName')" name="systemName">
            <a-input v-model:value="formState.systemName" :placeholder="t('integration.thirdSystem.form.systemName')" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('integration.thirdSystem.ipAddress')" name="ipAddress">
        <a-input
          v-model:value="formState.ipAddress"
          :placeholder="t('integration.thirdSystem.form.ipAddress')"
        />
      </a-form-item>

      <a-form-item :label="t('integration.thirdSystem.contactInfo')" name="contactInfo">
        <a-input v-model:value="formState.contactInfo" :placeholder="t('integration.thirdSystem.form.contactInfo')" />
      </a-form-item>

      <a-form-item :label="t('integration.thirdSystem.remark')" name="remark">
        <a-textarea v-model:value="formState.remark" :rows="3" :placeholder="t('integration.thirdSystem.form.remark')" />
      </a-form-item>

      <a-form-item :label="t('integration.thirdSystem.status')" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">{{ t('integration.common.enabled') }}</a-radio>
          <a-radio :value="0">{{ t('integration.common.disabled') }}</a-radio>
        </a-radio-group>
      </a-form-item>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import type { ThirdSystemSubmit } from '@/api/system/integration'

interface Props {
  open: boolean
  isEdit?: boolean
  formData?: ThirdSystemSubmit
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'submit', payload: ThirdSystemSubmit): void
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  formData: () => ({
    systemCode: '',
    systemName: '',
    ipAddress: '',
    contactInfo: '',
    remark: '',
    status: 1,
  }),
})

const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const formRef = ref<FormInstance>()

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const formState = reactive<ThirdSystemSubmit>({
  systemCode: '',
  systemName: '',
  ipAddress: '',
  contactInfo: '',
  remark: '',
  status: 1,
})

const singleIpPattern = /^([0-9a-zA-Z:.%-]+)?$/

const rules = computed(() => ({
  systemCode: [
    { required: true, message: t('integration.thirdSystem.form.systemCode'), trigger: 'blur' },
  ],
  systemName: [
    { required: true, message: t('integration.thirdSystem.form.systemName'), trigger: 'blur' },
  ],
  ipAddress: [
    {
      validator: async (_rule: unknown, value: string | undefined) => {
        const raw = (value || '').trim()
        if (!raw) {
          return
        }
        if (raw.includes(',') || raw.includes('，') || raw.includes('\n') || raw.includes('\r')) {
          throw new Error(t('integration.thirdSystem.form.singleIpOnly'))
        }
        if (!singleIpPattern.test(raw)) {
          throw new Error(t('integration.thirdSystem.form.singleIpOnly'))
        }
      },
      trigger: 'blur',
    },
  ],
}))

watch(
  () => props.formData,
  value => {
    Object.assign(formState, {
      systemCode: '',
      systemName: '',
      ipAddress: '',
      contactInfo: '',
      remark: '',
      status: 1,
      ...(value || {}),
    })
  },
  { immediate: true, deep: true },
)

async function handleSubmit() {
  try {
    loading.value = true
    await formRef.value?.validate()
    emit('submit', { ...formState, ipAddress: formState.ipAddress?.trim() || '' })
  } finally {
    loading.value = false
  }
}
</script>
