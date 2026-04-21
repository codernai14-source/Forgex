<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('integration.apiConfig.edit') : t('integration.apiConfig.add')"
    :loading="loading"
    width="760px"
    @submit="handleSubmit"
  >
    <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.apiCode')" name="apiCode">
            <a-input
              v-model:value="formState.apiCode"
              :disabled="isEdit"
              :placeholder="t('integration.apiConfig.form.apiCode')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.apiName')" name="apiName">
            <a-input v-model:value="formState.apiName" :placeholder="t('integration.apiConfig.form.apiName')" />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('integration.apiConfig.apiDesc')" name="apiDesc">
        <a-textarea v-model:value="formState.apiDesc" :rows="2" :placeholder="t('integration.apiConfig.form.apiDesc')" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.direction')" name="direction">
            <a-select v-model:value="formState.direction" :placeholder="t('integration.apiConfig.form.direction')">
              <a-select-option value="INBOUND">{{ t('integration.common.inbound') }}</a-select-option>
              <a-select-option value="OUTBOUND">{{ t('integration.common.outbound') }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.callMethod')" name="callMethod">
            <a-select v-model:value="formState.callMethod" :placeholder="t('integration.apiConfig.form.callMethod')">
              <a-select-option value="HTTP">{{ t('integration.common.http') }}</a-select-option>
              <a-select-option value="TCP">{{ t('integration.common.tcp') }}</a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('integration.apiConfig.apiPath')" name="apiPath">
        <a-input v-model:value="formState.apiPath" :placeholder="t('integration.apiConfig.form.apiPath')" />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.processorBean')" name="processorBean">
            <a-input v-model:value="formState.processorBean" :placeholder="t('integration.apiConfig.form.processorBean')" />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.moduleCode')" name="moduleCode">
            <a-select
              v-model:value="formState.moduleCode"
              :options="moduleOptions"
              allow-clear
              show-search
              :placeholder="t('integration.apiConfig.form.moduleCode')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="16">
          <a-form-item :label="t('integration.apiConfig.targetUrl')" name="targetUrl">
            <a-input v-model:value="formState.targetUrl" :placeholder="t('integration.apiConfig.form.targetUrl')" />
          </a-form-item>
        </a-col>
        <a-col :span="8">
          <a-form-item :label="t('integration.apiConfig.timeoutMs')" name="timeoutMs">
            <a-input-number
              v-model:value="formState.timeoutMs"
              :min="100"
              :step="100"
              style="width: 100%"
              :placeholder="t('integration.apiConfig.form.timeoutMs')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('integration.apiConfig.status')" name="status">
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
import { listModules } from '@/api/system/module'
import {
  addApiConfig,
  getApiConfigDetail,
  updateApiConfig,
} from '@/api/system/integration'
import type { ApiConfigItem, ApiConfigSubmit } from '@/api/system/integration'
import type { Module } from '@/views/system/module/types'

interface Props {
  open: boolean
  isEdit: boolean
  configId?: number
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success', record?: ApiConfigItem): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const formRef = ref<FormInstance>()
const moduleOptions = ref<Array<{ label: string; value: string }>>([])

const visible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const formState = reactive<ApiConfigSubmit>({
  apiCode: '',
  apiName: '',
  apiDesc: '',
  direction: 'INBOUND',
  apiPath: '',
  processorBean: '',
  callMethod: 'HTTP',
  targetUrl: '',
  timeoutMs: 3000,
  moduleCode: '',
  status: 1,
})

const rules = computed(() => ({
  apiCode: [{ required: true, message: t('integration.apiConfig.form.apiCode'), trigger: 'blur' }],
  apiName: [{ required: true, message: t('integration.apiConfig.form.apiName'), trigger: 'blur' }],
  direction: [{ required: true, message: t('integration.apiConfig.form.direction'), trigger: 'change' }],
  callMethod: [{ required: true, message: t('integration.apiConfig.form.callMethod'), trigger: 'change' }],
}))

watch(
  () => props.open,
  async (open) => {
    if (!open) {
      return
    }
    if (props.isEdit && props.configId) {
      const detail = await getApiConfigDetail(props.configId)
      Object.assign(formState, detail)
    } else {
      Object.assign(formState, {
        apiCode: '',
        apiName: '',
        apiDesc: '',
        direction: 'INBOUND',
        apiPath: '',
        processorBean: '',
        callMethod: 'HTTP',
        targetUrl: '',
        timeoutMs: 3000,
        moduleCode: '',
        status: 1,
      })
    }
  },
  { immediate: true }
)

watch(
  () => props.open,
  async (open) => {
    if (!open || moduleOptions.value.length) {
      return
    }
    const modules = await listModules({ pageNum: 1, pageSize: 999 })
    moduleOptions.value = (modules || []).map((item: Module) => ({
      label: `${item.name}${item.code ? ` (${item.code})` : ''}`,
      value: item.code,
    }))
  },
  { immediate: true }
)

async function handleSubmit() {
  try {
    loading.value = true
    await formRef.value?.validate()
    if (props.isEdit && props.configId) {
      const saved = await updateApiConfig({ ...formState, id: props.configId })
      emit('success', saved)
    } else {
      const saved = await addApiConfig({ ...formState })
      emit('success', saved)
    }
  } finally {
    loading.value = false
  }
}
</script>
