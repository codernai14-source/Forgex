<template>
  <BaseFormDialog
    v-model:open="visible"
    :title="isEdit ? t('integration.apiConfig.edit') : t('integration.apiConfig.add')"
    :loading="loading"
    width="800px"
    @submit="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 6 }"
      :wrapper-col="{ span: 16 }"
    >
      <a-form-item :label="t('integration.apiConfig.apiCode')" name="apiCode">
        <a-input
          v-model:value="formData.apiCode"
          :placeholder="t('integration.apiConfig.form.apiCode')"
          :disabled="isEdit"
        />
      </a-form-item>

      <a-form-item :label="t('integration.apiConfig.apiName')" name="apiName">
        <a-input
          v-model:value="formData.apiName"
          :placeholder="t('integration.apiConfig.form.apiName')"
        />
      </a-form-item>

      <a-form-item :label="t('integration.apiConfig.systemId')" name="systemId">
        <a-select
          v-model:value="formData.systemId"
          :placeholder="t('integration.apiConfig.form.systemId')"
          show-search
          :filter-option="filterSystemOption"
        >
          <a-select-option
            v-for="system in thirdSystemList"
            :key="system.id"
            :value="system.id"
          >
            {{ system.systemName }} ({{ system.systemCode }})
          </a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item :label="请求路径" name="requestPath">
        <a-input
          v-model:value="formData.requestPath"
          placeholder="请输入请求路径，如：/api/user/create"
        />
      </a-form-item>

      <a-form-item :label="请求方法" name="requestMethod">
        <a-select
          v-model:value="formData.requestMethod"
          placeholder="请选择请求方法"
          allow-clear
        >
          <a-select-option value="GET">GET</a-select-option>
          <a-select-option value="POST">POST</a-select-option>
          <a-select-option value="PUT">PUT</a-select-option>
          <a-select-option value="DELETE">DELETE</a-select-option>
          <a-select-option value="PATCH">PATCH</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item :label="操作方向" name="operationType">
        <a-select
          v-model:value="formData.operationType"
          :placeholder="t('integration.apiConfig.form.operationType')"
        >
          <a-select-option value="CREATE">创建</a-select-option>
          <a-select-option value="UPDATE">更新</a-select-option>
          <a-select-option value="DELETE">删除</a-select-option>
          <a-select-option value="READ">查询</a-select-option>
        </a-select>
      </a-form-item>

      <a-form-item :label="调用方式" name="callMethod">
        <a-select
          v-model:value="formData.callMethod"
          :placeholder="t('integration.apiConfig.form.callMethod')"
        >
          <a-select-option value="SYNC">同步</a-select-option>
          <a-select-option value="ASYNC">异步</a-select-option>
        </a-select>
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="超时时间 (秒)" name="timeoutSeconds">
            <a-input-number
              v-model:value="formData.timeoutSeconds"
              :min="1"
              :max="300"
              style="width: 100%"
              placeholder="默认 30 秒"
            />
          </a-form-item>
        </a-col>

        <a-col :span="12">
          <a-form-item :label="重试次数" name="retryTimes">
            <a-input-number
              v-model:value="formData.retryTimes"
              :min="0"
              :max="5"
              style="width: 100%"
              placeholder="默认 0 次"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="状态" name="status">
        <a-switch v-model:checked="statusSwitchValue" :checked-value="1" :checked-children="'启用'" :un-checked-children="'停用'" />
      </a-form-item>

      <a-form-item :label="备注说明" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          :placeholder="t('integration.apiConfig.form.remark')"
          :rows="3"
        />
      </a-form-item>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import {
  addApiConfig,
  getApiConfigDetail,
  updateApiConfig,
  getThirdSystemList,
} from '@/api/system/integration'
import type { ApiConfigSubmit, ApiConfigDetail, ThirdSystemItem } from '@/api/system/integration'

interface Props {
  open: boolean
  isEdit: boolean
  configId?: number
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n()

const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

const loading = ref(false)
const formRef = ref()
const thirdSystemList = ref<ThirdSystemItem[]>([])

const formData = reactive<ApiConfigSubmit>({
  apiCode: '',
  apiName: '',
  systemId: undefined as unknown as number,
  requestPath: '',
  requestMethod: 'POST',
  operationType: 'CREATE',
  callMethod: 'SYNC',
  timeoutSeconds: 30,
  retryTimes: 0,
  status: 1,
  remark: '',
})

const statusSwitchValue = computed({
  get: () => formData.status === 1,
  set: (value) => {
    formData.status = value ? 1 : 0
  },
})

const formRules = {
  apiCode: [
    { required: true, message: t('integration.apiConfig.form.apiCode'), trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_:]+$/,
      message: 'API 编码只能包含字母、数字、下划线和冒号',
      trigger: 'blur',
    },
  ],
  apiName: [{ required: true, message: t('integration.apiConfig.form.apiName'), trigger: 'blur' }],
  systemId: [{ required: true, message: t('integration.apiConfig.form.systemId'), trigger: 'change' }],
  operationType: [{ required: true, message: t('integration.apiConfig.form.operationType'), trigger: 'change' }],
  callMethod: [{ required: true, message: t('integration.apiConfig.form.callMethod'), trigger: 'change' }],
}

function filterSystemOption(input: string, option: any) {
  const text = (option.children as string) || ''
  return text.toLowerCase().includes(input.toLowerCase())
}

async function loadThirdSystems() {
  try {
    const result = await getThirdSystemList({ pageNum: 1, pageSize: 100, status: 1 })
    thirdSystemList.value = result.records || []
  } catch (error) {
    console.error('load third systems failed:', error)
  }
}

async function handleSubmit() {
  try {
    await formRef.value?.validate()

    loading.value = true
    const submitData: ApiConfigSubmit = {
      ...formData,
    }

    if (props.isEdit && props.configId) {
      submitData.id = props.configId
      await updateApiConfig(submitData)
    } else {
      await addApiConfig(submitData)
    }

    emit('success')
  } catch (error) {
    console.error('submit api config failed:', error)
  } finally {
    loading.value = false
  }
}

function handleCancel() {
  emit('update:open', false)
}

async function loadConfigDetail() {
  if (!(props.isEdit && props.configId)) return
  loading.value = true
  try {
    const detail = await getApiConfigDetail(props.configId)
    Object.assign(formData, {
      ...detail,
    })
  } catch (error) {
    console.error('load api config detail failed:', error)
    message.error('加载 API 配置详情失败')
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(formData, {
    apiCode: '',
    apiName: '',
    systemId: undefined as unknown as number,
    requestPath: '',
    requestMethod: 'POST',
    operationType: 'CREATE',
    callMethod: 'SYNC',
    timeoutSeconds: 30,
    retryTimes: 0,
    status: 1,
    remark: '',
  })
}

watch(
  () => props.open,
  (open) => {
    if (!open) return
    nextTick(() => {
      if (props.isEdit) {
        loadConfigDetail()
      } else {
        resetForm()
      }
    })
  },
)

onMounted(() => {
  loadThirdSystems()
})
</script>

<style scoped>
:deep(.ant-form-item-label > label) {
  font-weight: 500;
}
</style>
