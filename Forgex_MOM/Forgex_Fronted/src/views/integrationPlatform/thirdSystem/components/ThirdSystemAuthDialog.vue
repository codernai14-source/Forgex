<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="t('integration.thirdSystem.auth')"
    :loading="loading"
    width="760px"
    @submit="handleSubmit"
  >
    <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
      <a-form-item :label="t('integration.thirdSystem.form.authType')" name="authType">
        <a-radio-group v-model:value="formState.authType">
          <a-radio value="TOKEN">Token</a-radio>
          <a-radio value="WHITELIST">IP Whitelist</a-radio>
        </a-radio-group>
      </a-form-item>

      <template v-if="formState.authType === 'TOKEN'">
        <a-row :gutter="16">
          <a-col :span="16">
            <a-form-item :label="t('integration.thirdSystem.form.tokenValue')" name="tokenValue">
              <a-input
                v-model:value="formState.tokenValue"
                :placeholder="t('integration.thirdSystem.form.tokenValuePlaceholder')"
              />
            </a-form-item>
          </a-col>

          <a-col :span="8">
            <a-form-item :label="t('integration.thirdSystem.form.tokenExpireHours')" name="tokenExpireHours">
              <a-input-number
                v-model:value="formState.tokenExpireHours"
                :min="1"
                :max="8760"
                :placeholder="t('integration.thirdSystem.form.tokenExpireHoursPlaceholder')"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-button @click="handleGenerateToken">Generate Token</a-button>
      </template>

      <a-form-item v-else :label="t('integration.thirdSystem.form.whitelistIps')" name="whitelistIps">
        <a-textarea
          v-model:value="formState.whitelistIps"
          :rows="4"
          :placeholder="t('integration.thirdSystem.form.whitelistIpsPlaceholder')"
        />
      </a-form-item>

      <a-form-item :label="t('integration.thirdSystem.status')" name="status">
        <a-radio-group v-model:value="formState.status">
          <a-radio :value="1">{{ t('integration.common.enabled') }}</a-radio>
          <a-radio :value="0">{{ t('integration.common.disabled') }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <a-form-item :label="t('integration.thirdSystem.remark')" name="remark">
        <a-textarea v-model:value="formState.remark" :rows="3" :placeholder="t('integration.thirdSystem.form.remark')" />
      </a-form-item>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import {
  addThirdAuthorization,
  generateThirdAuthorizationToken,
  getThirdAuthorizationBySystemId,
  updateThirdAuthorization,
} from '@/api/system/integration'
import type { ThirdAuthorizationSubmit } from '@/api/system/integration'

interface Props {
  open: boolean
  systemId?: number
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  systemId: undefined,
})

const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const formRef = ref<FormInstance>()

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const formState = reactive<ThirdAuthorizationSubmit>({
  thirdSystemId: 0,
  authType: 'TOKEN',
  tokenValue: '',
  tokenExpireHours: 24,
  whitelistIps: '',
  status: 1,
  remark: '',
})

const rules = computed(() => ({
  authType: [{ required: true, message: t('integration.thirdSystem.form.authType'), trigger: 'change' }],
  whitelistIps: formState.authType === 'WHITELIST'
    ? [{ required: true, message: t('integration.thirdSystem.form.whitelistIps'), trigger: 'blur' }]
    : [],
}))

watch(
  () => props.open,
  async (open) => {
    if (!open || !props.systemId) {
      return
    }
    formState.thirdSystemId = props.systemId
    formState.id = undefined
    formState.authType = 'TOKEN'
    formState.tokenValue = ''
    formState.tokenExpireHours = 24
    formState.whitelistIps = ''
    formState.status = 1
    formState.remark = ''

    const detail = await getThirdAuthorizationBySystemId(props.systemId)
    if (detail) {
      Object.assign(formState, detail)
    }
  },
  { immediate: true }
)

async function handleGenerateToken() {
  if (!props.systemId) {
    return
  }
  formState.tokenValue = await generateThirdAuthorizationToken(props.systemId, formState.tokenExpireHours)
}

async function handleSubmit() {
  if (!props.systemId) {
    return
  }
  try {
    loading.value = true
    await formRef.value?.validate()
    const payload: ThirdAuthorizationSubmit = {
      ...formState,
      thirdSystemId: props.systemId,
    }
    if (payload.id) {
      await updateThirdAuthorization(payload)
    } else {
      await addThirdAuthorization(payload)
    }
    emit('success')
  } finally {
    loading.value = false
  }
}
</script>
