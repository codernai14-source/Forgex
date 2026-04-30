<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('integration.apiConfig.edit') : t('integration.apiConfig.add')"
    :loading="loading"
    :mode="mode"
    :width="width"
    @submit="handleSubmit"
  >
    <a-form ref="formRef" :model="formState" :rules="rules" layout="vertical">
      <div class="section-title">{{ t('integration.apiConfig.baseInfo') }}</div>

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
            <a-select
              v-model:value="formState.direction"
              :disabled="isEdit"
              :placeholder="t('integration.apiConfig.form.direction')"
            >
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

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('integration.apiConfig.apiPath')" name="apiPath">
            <a-input v-model:value="formState.apiPath" :placeholder="t('integration.apiConfig.form.apiPath')" />
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

      <a-form-item v-if="formState.direction === 'INBOUND'" :label="t('integration.apiConfig.processorBean')" name="processorBean">
        <a-input v-model:value="formState.processorBean" :placeholder="t('integration.apiConfig.form.processorBean')" />
      </a-form-item>

      <template v-if="formState.direction === 'INBOUND'">
        <div class="section-title">{{ t('integration.apiConfig.invokeConfig') }}</div>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.invokeMode')" name="invokeMode">
              <a-select v-model:value="formState.invokeMode" :placeholder="t('integration.apiConfig.form.invokeMode')">
                <a-select-option value="SYNC">{{ t('integration.apiConfig.sync') }}</a-select-option>
                <a-select-option value="ASYNC">{{ t('integration.apiConfig.async') }}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.httpMethod')" name="httpMethod">
              <a-select v-model:value="formState.httpMethod" :placeholder="t('integration.apiConfig.form.httpMethod')">
                <a-select-option value="POST">POST</a-select-option>
                <a-select-option value="GET">GET</a-select-option>
                <a-select-option value="PUT">PUT</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.contentType')" name="contentType">
              <a-select v-model:value="formState.contentType" :placeholder="t('integration.apiConfig.form.contentType')">
                <a-select-option value="application/json">application/json</a-select-option>
                <a-select-option value="application/x-www-form-urlencoded">application/x-www-form-urlencoded</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>

        <a-row :gutter="16">
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.timeoutMs')" name="timeoutMs">
              <a-input-number v-model:value="formState.timeoutMs" :min="100" :step="100" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.retryCount')" name="retryCount">
              <a-input-number v-model:value="formState.retryCount" :min="0" :step="1" style="width: 100%" />
            </a-form-item>
          </a-col>
          <a-col :span="8">
            <a-form-item :label="t('integration.apiConfig.retryIntervalMs')" name="retryIntervalMs">
              <a-input-number v-model:value="formState.retryIntervalMs" :min="0" :step="100" style="width: 100%" />
            </a-form-item>
          </a-col>
        </a-row>
      </template>

      <template v-else>
        <div class="section-title section-title--with-action">
          <span>{{ t('integration.apiConfig.outboundTargets') }}</span>
          <a-button type="dashed" size="small" @click="addTarget">{{ t('integration.apiConfig.addTarget') }}</a-button>
        </div>

        <a-alert
          type="info"
          show-icon
          class="target-alert"
          :message="t('integration.apiConfig.targetHint')"
        />

        <div
          v-for="(target, index) in formState.outboundTargets"
          :key="target.id || `target-${index}`"
          class="target-card"
        >
          <div class="target-card__header">
            <span>{{ t('integration.apiConfig.targetConfig') }} {{ index + 1 }}</span>
            <a-button danger type="link" size="small" @click="removeTarget(index)">
              {{ t('common.delete') }}
            </a-button>
          </div>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.thirdSystem')" :name="['outboundTargets', index, 'thirdSystemId']">
                <a-select
                  v-model:value="target.thirdSystemId"
                  :options="thirdSystemOptions"
                  allow-clear
                  show-search
                  :placeholder="t('integration.apiConfig.form.thirdSystem')"
                  @change="value => handleTargetSystemChange(target, value)"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.status')" :name="['outboundTargets', index, 'status']">
                <a-radio-group v-model:value="target.status">
                  <a-radio :value="1">{{ t('integration.common.enabled') }}</a-radio>
                  <a-radio :value="0">{{ t('integration.common.disabled') }}</a-radio>
                </a-radio-group>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.targetCode')" :name="['outboundTargets', index, 'targetCode']">
                <a-input v-model:value="target.targetCode" :placeholder="t('integration.apiConfig.form.targetCode')" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.targetName')" :name="['outboundTargets', index, 'targetName']">
                <a-input v-model:value="target.targetName" :placeholder="t('integration.apiConfig.form.targetName')" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.targetUrl')" :name="['outboundTargets', index, 'targetUrl']">
                <a-input v-model:value="target.targetUrl" :placeholder="t('integration.apiConfig.form.targetUrl')" />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('integration.apiConfig.invokeMode')" :name="['outboundTargets', index, 'invokeMode']">
                <a-select v-model:value="target.invokeMode" :placeholder="t('integration.apiConfig.form.invokeMode')">
                  <a-select-option value="SYNC">{{ t('integration.apiConfig.sync') }}</a-select-option>
                  <a-select-option value="ASYNC">{{ t('integration.apiConfig.async') }}</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.httpMethod')" :name="['outboundTargets', index, 'httpMethod']">
                <a-select v-model:value="target.httpMethod" :placeholder="t('integration.apiConfig.form.httpMethod')">
                  <a-select-option value="POST">POST</a-select-option>
                  <a-select-option value="GET">GET</a-select-option>
                  <a-select-option value="PUT">PUT</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.contentType')" :name="['outboundTargets', index, 'contentType']">
                <a-select v-model:value="target.contentType" :placeholder="t('integration.apiConfig.form.contentType')">
                  <a-select-option value="application/json">application/json</a-select-option>
                  <a-select-option value="application/x-www-form-urlencoded">application/x-www-form-urlencoded</a-select-option>
                </a-select>
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.timeoutMs')" :name="['outboundTargets', index, 'timeoutMs']">
                <a-input-number v-model:value="target.timeoutMs" :min="100" :step="100" style="width: 100%" />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.retryCount')" :name="['outboundTargets', index, 'retryCount']">
                <a-input-number v-model:value="target.retryCount" :min="0" :step="1" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.retryIntervalMs')" :name="['outboundTargets', index, 'retryIntervalMs']">
                <a-input-number v-model:value="target.retryIntervalMs" :min="0" :step="100" style="width: 100%" />
              </a-form-item>
            </a-col>
            <a-col :span="8">
              <a-form-item :label="t('integration.apiConfig.remark')" :name="['outboundTargets', index, 'remark']">
                <a-input v-model:value="target.remark" :placeholder="t('integration.apiConfig.form.remark')" />
              </a-form-item>
            </a-col>
          </a-row>
        </div>
      </template>

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
import { message } from 'ant-design-vue'
import type { FormInstance } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { listModules } from '@/api/system/module'
import { listThirdSystems, addApiConfig, getApiConfigDetail, updateApiConfig } from '@/api/system/integration'
import type {
  ApiConfigItem,
  ApiConfigSubmit,
  ApiOutboundTargetSubmit,
  ThirdSystemItem,
} from '@/api/system/integration'
import type { Module } from '@/views/system/module/types'

interface Props {
  open: boolean
  isEdit: boolean
  configId?: number
  mode?: 'modal' | 'drawer'
  width?: number | string
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success', record?: ApiConfigItem): void
}

const props = withDefaults(defineProps<Props>(), {
  width: 960,
})

const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const formRef = ref<FormInstance>()
const moduleOptions = ref<Array<{ label: string; value: string }>>([])
const thirdSystemOptions = ref<Array<{ label: string; value: number; record: ThirdSystemItem }>>([])

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
  httpMethod: 'POST',
  invokeMode: 'SYNC',
  contentType: 'application/json',
  targetUrl: '',
  timeoutMs: 3000,
  retryCount: 0,
  retryIntervalMs: 1000,
  moduleCode: '',
  status: 1,
  outboundTargets: [],
})

const rules = computed(() => ({
  apiCode: [{ required: true, message: t('integration.apiConfig.form.apiCode'), trigger: 'blur' }],
  apiName: [{ required: true, message: t('integration.apiConfig.form.apiName'), trigger: 'blur' }],
  direction: [{ required: true, message: t('integration.apiConfig.form.direction'), trigger: 'change' }],
  callMethod: [{ required: true, message: t('integration.apiConfig.form.callMethod'), trigger: 'change' }],
  invokeMode: formState.direction === 'INBOUND'
    ? [{ required: true, message: t('integration.apiConfig.form.invokeMode'), trigger: 'change' }]
    : [],
}))

watch(
  () => props.open,
  async open => {
    if (!open) {
      return
    }
    await Promise.all([loadModuleOptions(), loadThirdSystems()])
    if (props.isEdit && props.configId) {
      const detail = await getApiConfigDetail(props.configId)
      applyDetail(detail)
    } else {
      resetForm()
    }
  },
  { immediate: true },
)

watch(
  () => formState.direction,
  direction => {
    if (direction === 'OUTBOUND' && (!formState.outboundTargets || formState.outboundTargets.length === 0)) {
      formState.outboundTargets = [createTarget()]
    }
  },
)

function resetForm() {
  Object.assign(formState, {
    apiCode: '',
    apiName: '',
    apiDesc: '',
    direction: 'INBOUND',
    apiPath: '',
    processorBean: '',
    callMethod: 'HTTP',
    httpMethod: 'POST',
    invokeMode: 'SYNC',
    contentType: 'application/json',
    targetUrl: '',
    timeoutMs: 3000,
    retryCount: 0,
    retryIntervalMs: 1000,
    moduleCode: '',
    status: 1,
    outboundTargets: [],
  })
}

function applyDetail(detail: ApiConfigItem) {
  Object.assign(formState, {
    ...detail,
    httpMethod: detail.httpMethod || 'POST',
    invokeMode: detail.invokeMode || 'SYNC',
    contentType: detail.contentType || 'application/json',
    timeoutMs: detail.timeoutMs ?? 3000,
    retryCount: detail.retryCount ?? 0,
    retryIntervalMs: detail.retryIntervalMs ?? 1000,
    outboundTargets: (detail.outboundTargets || []).map(target => ({
      ...target,
      httpMethod: target.httpMethod || 'POST',
      contentType: target.contentType || 'application/json',
      invokeMode: target.invokeMode || 'SYNC',
      timeoutMs: target.timeoutMs ?? 3000,
      retryCount: target.retryCount ?? 0,
      retryIntervalMs: target.retryIntervalMs ?? 1000,
      status: target.status ?? 1,
    })),
  })
  if (formState.direction === 'OUTBOUND' && (!formState.outboundTargets || formState.outboundTargets.length === 0) && detail.targetUrl) {
    formState.outboundTargets = [createTarget({
      targetUrl: detail.targetUrl,
      httpMethod: detail.httpMethod,
      contentType: detail.contentType,
      invokeMode: detail.invokeMode,
      timeoutMs: detail.timeoutMs,
      retryCount: detail.retryCount,
      retryIntervalMs: detail.retryIntervalMs,
      status: detail.status,
    })]
  }
}

async function loadModuleOptions() {
  if (moduleOptions.value.length) {
    return
  }
  const modules = await listModules({ pageNum: 1, pageSize: 999 })
  moduleOptions.value = (modules || []).map((item: Module) => ({
    label: `${item.name}${item.code ? ` (${item.code})` : ''}`,
    value: item.code,
  }))
}

async function loadThirdSystems() {
  const list = await listThirdSystems({ pageNum: 1, pageSize: 999, status: 1 })
  thirdSystemOptions.value = (list || []).map(item => ({
    label: `${item.systemName}${item.systemCode ? ` (${item.systemCode})` : ''}`,
    value: item.id!,
    record: item,
  }))
}

function createTarget(partial: Partial<ApiOutboundTargetSubmit> = {}): ApiOutboundTargetSubmit {
  return {
    thirdSystemId: undefined,
    targetCode: '',
    targetName: '',
    targetUrl: '',
    httpMethod: 'POST',
    contentType: 'application/json',
    invokeMode: 'SYNC',
    timeoutMs: 3000,
    retryCount: 0,
    retryIntervalMs: 1000,
    orderNum: (formState.outboundTargets?.length || 0) + 1,
    status: 1,
    remark: '',
    ...partial,
  }
}

function addTarget() {
  if (!formState.outboundTargets) {
    formState.outboundTargets = []
  }
  formState.outboundTargets.push(createTarget())
}

function removeTarget(index: number) {
  formState.outboundTargets?.splice(index, 1)
  formState.outboundTargets?.forEach((target, idx) => {
    target.orderNum = idx + 1
  })
}

function handleTargetSystemChange(target: ApiOutboundTargetSubmit, value?: number) {
  const option = thirdSystemOptions.value.find(item => item.value === value)
  if (!option) {
    return
  }
  target.targetCode = target.targetCode || option.record.systemCode
  target.targetName = target.targetName || option.record.systemName
}

function normalizeTargets() {
  return (formState.outboundTargets || [])
    .filter(target => target.thirdSystemId || target.targetUrl || target.targetCode)
    .map((target, index) => ({
      ...target,
      targetCode: target.targetCode?.trim() || '',
      targetName: target.targetName?.trim() || '',
      targetUrl: target.targetUrl?.trim() || '',
      httpMethod: target.httpMethod || 'POST',
      contentType: target.contentType || 'application/json',
      invokeMode: target.invokeMode || 'SYNC',
      timeoutMs: target.timeoutMs ?? 3000,
      retryCount: target.retryCount ?? 0,
      retryIntervalMs: target.retryIntervalMs ?? 1000,
      orderNum: index + 1,
      status: target.status ?? 1,
    }))
}

async function handleSubmit() {
  try {
    loading.value = true
    await formRef.value?.validate()

    const payload: ApiConfigSubmit = {
      ...formState,
      apiCode: formState.apiCode.trim(),
      apiName: formState.apiName.trim(),
      apiDesc: formState.apiDesc?.trim() || '',
      apiPath: formState.apiPath?.trim() || '',
      processorBean: formState.direction === 'INBOUND' ? formState.processorBean?.trim() || '' : '',
      targetUrl: formState.direction === 'OUTBOUND' ? '' : formState.targetUrl?.trim() || '',
      outboundTargets: formState.direction === 'OUTBOUND' ? normalizeTargets() : [],
    }

    if (formState.direction === 'OUTBOUND' && (!payload.outboundTargets || payload.outboundTargets.length === 0)) {
      message.error(t('integration.apiConfig.form.targetRequired'))
      return
    }

    if (props.isEdit && props.configId) {
      const saved = await updateApiConfig({ ...payload, id: props.configId })
      emit('success', saved)
    } else {
      const saved = await addApiConfig(payload)
      emit('success', saved)
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="less">
.section-title {
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: var(--fx-text-primary, #1f2937);
}

.section-title--with-action {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.target-alert {
  margin-bottom: 16px;
}

.target-card {
  margin-bottom: 16px;
  padding: 16px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 14px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #ffffff), var(--fx-fill-secondary, #fafafa));
  color: var(--fx-text-primary, #1f2937);
}

.target-card__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-weight: 600;
}

:deep(.ant-form-item-label > label),
:deep(.ant-input),
:deep(.ant-input-number),
:deep(.ant-select-selector),
:deep(.ant-radio-wrapper) {
  color: var(--fx-text-primary, #1f2937);
}
</style>
