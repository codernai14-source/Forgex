<!--
 * 代码生成数据源表单弹窗
 *
 * 功能描述：
 * 1. 支持代码生成数据源新增与编辑
 * 2. 支持表单内直接测试连接
 * 3. 复用系统统一表单弹窗样式
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-04-21
-->
<template>
  <BaseFormDialog
    v-model:open="dialogVisible"
    :title="dialogTitle"
    :loading="submitLoading"
    width="760px"
    @submit="handleSubmit"
    @cancel="handleCancel"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 18 }"
    >
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.datasourceCode')" name="datasourceCode">
            <a-input
              v-model:value="formData.datasourceCode"
              :maxlength="50"
              :disabled="isEdit"
              :placeholder="t('system.codegenDatasource.form.datasourceCode')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.datasourceName')" name="datasourceName">
            <a-input
              v-model:value="formData.datasourceName"
              :maxlength="100"
              :placeholder="t('system.codegenDatasource.form.datasourceName')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.dbType')" name="dbType">
            <a-select
              v-model:value="formData.dbType"
              :options="dbTypeOptions"
              :placeholder="t('system.codegenDatasource.form.dbType')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('common.status')" name="enabled">
            <a-radio-group v-model:value="formData.enabled">
              <a-radio :value="true">{{ t('common.enabled') }}</a-radio>
              <a-radio :value="false">{{ t('common.disabled') }}</a-radio>
            </a-radio-group>
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('system.codegenDatasource.jdbcUrl')" name="jdbcUrl">
        <a-input
          v-model:value="formData.jdbcUrl"
          :maxlength="300"
          :placeholder="t('system.codegenDatasource.form.jdbcUrl')"
        />
      </a-form-item>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.username')" name="username">
            <a-input
              v-model:value="formData.username"
              :maxlength="100"
              :placeholder="t('system.codegenDatasource.form.username')"
            />
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.password')" name="password">
            <a-input-password
              v-model:value="formData.password"
              :maxlength="100"
              :placeholder="t('system.codegenDatasource.form.password')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item :label="t('system.codegenDatasource.schemaName')" name="schemaName">
            <a-input
              v-model:value="formData.schemaName"
              :maxlength="100"
              :placeholder="t('system.codegenDatasource.form.schemaName')"
            />
          </a-form-item>
        </a-col>
      </a-row>

      <a-form-item :label="t('common.remark')" name="remark">
        <a-textarea
          v-model:value="formData.remark"
          :rows="3"
          :maxlength="200"
          :placeholder="t('system.codegenDatasource.form.remark')"
        />
      </a-form-item>

      <a-form-item :wrapper-col="{ offset: 5, span: 18 }">
        <a-space>
          <a-button :loading="testLoading" @click="handleTestConnection">
            {{ t('system.codegenDatasource.testConnection') }}
          </a-button>
        </a-space>
      </a-form-item>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { message, type FormInstance, type Rule } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import {
  getCodegenDatasourceDetail,
  saveCodegenDatasource,
  testCodegenDatasource,
  type CodegenDatasourceSaveParam,
} from '@/api/system/codegenDatasource'

interface Props {
  open: boolean
  datasourceId?: number
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'success'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()
const { t } = useI18n({ useScope: 'global' })

const formRef = ref<FormInstance>()
const submitLoading = ref(false)
const testLoading = ref(false)

const formData = reactive<CodegenDatasourceSaveParam>({
  datasourceCode: '',
  datasourceName: '',
  dbType: 'MYSQL',
  jdbcUrl: '',
  username: '',
  password: '',
  schemaName: '',
  enabled: true,
  remark: '',
})

const dbTypeOptions = [
  { label: 'MySQL', value: 'MYSQL' },
  { label: 'PostgreSQL', value: 'POSTGRESQL' },
  { label: 'Oracle', value: 'ORACLE' },
  { label: 'SQL Server', value: 'SQLSERVER' },
]

const dialogVisible = computed({
  get: () => props.open,
  set: (value: boolean) => emit('update:open', value),
})

const isEdit = computed(() => !!props.datasourceId)
const dialogTitle = computed(() =>
  isEdit.value ? t('system.codegenDatasource.edit') : t('system.codegenDatasource.add'),
)

const formRules: Record<string, Rule[]> = {
  datasourceCode: [{ required: true, message: t('system.codegenDatasource.form.datasourceCode'), trigger: 'blur' }],
  datasourceName: [{ required: true, message: t('system.codegenDatasource.form.datasourceName'), trigger: 'blur' }],
  dbType: [{ required: true, message: t('system.codegenDatasource.form.dbType'), trigger: 'change' }],
  jdbcUrl: [{ required: true, message: t('system.codegenDatasource.form.jdbcUrl'), trigger: 'blur' }],
  username: [{ required: true, message: t('system.codegenDatasource.form.username'), trigger: 'blur' }],
}

/**
 * 重置表单
 */
function resetForm() {
  formData.id = undefined
  formData.datasourceCode = ''
  formData.datasourceName = ''
  formData.dbType = 'MYSQL'
  formData.jdbcUrl = ''
  formData.username = ''
  formData.password = ''
  formData.schemaName = ''
  formData.enabled = true
  formData.remark = ''
}

/**
 * 加载详情
 */
async function loadDetail() {
  if (!props.datasourceId) {
    return
  }
  const detail = await getCodegenDatasourceDetail(props.datasourceId)
  formData.id = detail.id
  formData.datasourceCode = detail.datasourceCode
  formData.datasourceName = detail.datasourceName
  formData.dbType = detail.dbType
  formData.jdbcUrl = detail.jdbcUrl
  formData.username = detail.username
  formData.password = ''
  formData.schemaName = detail.schemaName || ''
  formData.enabled = !!detail.enabled
  formData.remark = detail.remark || ''
}

/**
 * 测试连接
 */
async function handleTestConnection() {
  await formRef.value?.validateFields(['dbType', 'jdbcUrl', 'username'])
  testLoading.value = true
  try {
    await testCodegenDatasource({
      id: formData.id,
      jdbcUrl: formData.jdbcUrl,
      username: formData.username,
      password: formData.password,
      dbType: formData.dbType,
    })
    message.success(t('system.codegenDatasource.testSuccess'))
  } finally {
    testLoading.value = false
  }
}

/**
 * 提交表单
 */
async function handleSubmit() {
  await formRef.value?.validate()
  submitLoading.value = true
  try {
    await saveCodegenDatasource({ ...formData })
    emit('success')
    dialogVisible.value = false
  } finally {
    submitLoading.value = false
  }
}

/**
 * 关闭弹窗
 */
function handleCancel() {
  dialogVisible.value = false
}

watch(
  () => props.open,
  async (visible) => {
    if (!visible) {
      resetForm()
      formRef.value?.resetFields()
      return
    }

    resetForm()
    if (props.datasourceId) {
      await loadDetail()
    }
  },
)
</script>
