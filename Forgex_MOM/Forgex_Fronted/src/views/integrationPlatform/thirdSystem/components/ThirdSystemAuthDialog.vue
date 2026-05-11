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
          <a-radio value="WHITELIST">{{ t('integration.thirdSystem.form.authWhitelist') }}</a-radio>
          <a-radio value="TOKEN_WHITELIST">{{ t('integration.thirdSystem.form.authTokenWhitelist') }}</a-radio>
        </a-radio-group>
      </a-form-item>

      <template v-if="supportToken">
        <a-row :gutter="16">
          <a-col :span="24">
            <a-form-item :label="t('integration.thirdSystem.form.tokenValue')" name="tokenValue">
              <a-input
                v-model:value="formState.tokenValue"
                :placeholder="t('integration.thirdSystem.form.tokenValuePlaceholder')"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item :label="t('integration.thirdSystem.form.tokenExpireType')" name="tokenExpireType">
              <a-select v-model:value="formState.tokenExpireType">
                <a-select-option value="DAY">{{ t('integration.thirdSystem.form.expireDay') }}</a-select-option>
                <a-select-option value="MONTH">{{ t('integration.thirdSystem.form.expireMonth') }}</a-select-option>
                <a-select-option value="YEAR">{{ t('integration.thirdSystem.form.expireYear') }}</a-select-option>
                <a-select-option value="CUSTOM">{{ t('integration.thirdSystem.form.expireCustom') }}</a-select-option>
                <a-select-option value="FOREVER">{{ t('integration.thirdSystem.form.expireForever') }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>

          <a-col v-if="needExpireValue" :span="8">
            <a-form-item :label="t('integration.thirdSystem.form.tokenExpireValue')" name="tokenExpireValue">
              <a-input-number
                v-model:value="formState.tokenExpireValue"
                :min="1"
                :max="999"
                :placeholder="t('integration.thirdSystem.form.tokenExpireValuePlaceholder')"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>

          <a-col v-if="formState.tokenExpireType === 'CUSTOM'" :span="16">
            <a-form-item :label="t('integration.thirdSystem.form.tokenExpireTime')" name="tokenExpireTime">
              <a-date-picker
                v-model:value="tokenExpireDate"
                show-time
                value-format="YYYY-MM-DD HH:mm:ss"
                style="width: 100%"
              />
            </a-form-item>
          </a-col>
        </a-row>

        <a-button @click="handleGenerateToken">{{ t('integration.thirdSystem.form.generateToken') }}</a-button>
      </template>

      <template v-if="supportWhitelist">
        <a-alert
          type="info"
          show-icon
          class="auth-tip"
          :message="t('integration.thirdSystem.form.whitelistTip')"
        />
        <a-form-item :label="t('integration.thirdSystem.form.whitelistIps')" name="whitelistIps">
          <a-textarea
            v-model:value="formState.whitelistIps"
            :rows="4"
            :placeholder="t('integration.thirdSystem.form.whitelistIpsPlaceholder')"
          />
        </a-form-item>
      </template>

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
  tokenExpireType: 'DAY',
  tokenExpireValue: 1,
  tokenExpireTime: '',
  whitelistIps: '',
  status: 1,
  remark: '',
})

const supportToken = computed(() => formState.authType === 'TOKEN' || formState.authType === 'TOKEN_WHITELIST')
const supportWhitelist = computed(() => formState.authType === 'WHITELIST' || formState.authType === 'TOKEN_WHITELIST')
const needExpireValue = computed(() => ['DAY', 'MONTH', 'YEAR'].includes(formState.tokenExpireType || ''))
const tokenExpireDate = computed({
  get: () => formState.tokenExpireTime,
  set: (value?: string) => {
    formState.tokenExpireTime = value || ''
  },
})

const rules = computed(() => ({
  authType: [{ required: true, message: t('integration.thirdSystem.form.authType'), trigger: 'change' }],
  whitelistIps: supportWhitelist.value
    ? [{ required: true, message: t('integration.thirdSystem.form.whitelistIps'), trigger: 'blur' }]
    : [],
  tokenExpireValue: supportToken.value && needExpireValue.value
    ? [{ required: true, message: t('integration.thirdSystem.form.tokenExpireValuePlaceholder'), trigger: 'blur' }]
    : [],
  tokenExpireTime: supportToken.value && formState.tokenExpireType === 'CUSTOM'
    ? [{ required: true, message: t('integration.thirdSystem.form.tokenExpireTimePlaceholder'), trigger: 'change' }]
    : [],
}))

watch(
  () => props.open,
  async open => {
    if (!open || !props.systemId) {
      return
    }
    formState.thirdSystemId = props.systemId
    formState.id = undefined
    formState.authType = 'TOKEN'
    formState.tokenValue = ''
    formState.tokenExpireType = 'DAY'
    formState.tokenExpireValue = 1
    formState.tokenExpireHours = undefined
    formState.tokenExpireTime = ''
    formState.whitelistIps = ''
    formState.status = 1
    formState.remark = ''

    const detail = await getThirdAuthorizationBySystemId(props.systemId)
    if (detail) {
      Object.assign(formState, detail)
    }
  },
  { immediate: true },
)

async function handleGenerateToken() {
  if (!props.systemId) {
    return
  }
  const tokenValue = await generateThirdAuthorizationToken(props.systemId, buildPayload())
  const detail = await getThirdAuthorizationBySystemId(props.systemId)
  if (detail) {
    Object.assign(formState, detail)
  }
  formState.tokenValue = tokenValue
}

async function handleSubmit() {
  if (!props.systemId) {
    return
  }
  try {
    loading.value = true
    await formRef.value?.validate()
    const payload = buildPayload()
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

function buildPayload(): ThirdAuthorizationSubmit {
  return {
    ...formState,
    thirdSystemId: props.systemId || 0,
    tokenValue: supportToken.value ? formState.tokenValue : '',
    tokenExpireType: supportToken.value ? formState.tokenExpireType : undefined,
    tokenExpireValue: supportToken.value && needExpireValue.value ? formState.tokenExpireValue : undefined,
    tokenExpireTime: supportToken.value && formState.tokenExpireType === 'CUSTOM' ? formState.tokenExpireTime : undefined,
    whitelistIps: supportWhitelist.value ? formState.whitelistIps?.trim() : '',
  }
}
</script>

<style scoped lang="less">
.auth-tip {
  margin-bottom: 16px;
}
</style>
