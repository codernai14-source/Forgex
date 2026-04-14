<template>
  <BaseFormDialog
    v-model:open="dialogVisible"
    :title="isEdit ? t('system.excel.editImportConfig') : t('system.excel.addImportConfig')"
    :width="1200"
    :loading="loading"
    @submit="handleSubmit"
  >
    <a-form
      ref="formRef"
      :model="formData"
      :label-col="{ span: 5 }"
      :wrapper-col="{ span: 19 }"
    >
      <a-card size="small" :bordered="false" class="mb-16">
        <template #title>
          <span class="card-title">{{ t('system.excel.baseConfig') }}</span>
        </template>

        <a-form-item
          :label="t('system.excel.tableName')"
          name="tableName"
          :rules="{ required: true, message: t('system.excel.message.pleaseInputTableName') }"
        >
          <a-input
            v-model:value="formData.tableName"
            :placeholder="t('system.excel.pleaseInputTableName')"
          />
        </a-form-item>

        <a-form-item
          :label="t('system.excel.tableCode')"
          name="tableCode"
          :rules="{ required: true, message: t('system.excel.message.pleaseInputTableCode') }"
        >
          <a-input
            v-model:value="formData.tableCode"
            :placeholder="t('system.excel.pleaseInputTableCode')"
          />
        </a-form-item>

        <a-form-item :label="t('system.excel.title')" name="title">
          <a-input
            v-model:value="formData.title"
            :placeholder="t('system.excel.pleaseInputTitle')"
          />
        </a-form-item>

        <a-form-item :label="t('system.excel.subtitle')" name="subtitle">
          <a-input
            v-model:value="formData.subtitle"
            :placeholder="t('system.excel.pleaseInputSubtitle')"
          />
        </a-form-item>

        <a-form-item :label="t('system.excel.version')" name="version">
          <a-input-number
            v-model:value="formData.version"
            :min="1"
            :step="1"
            style="width: 100%"
          />
        </a-form-item>
      </a-card>

      <a-card size="small" :bordered="false" class="mt-16">
        <template #title>
          <span class="card-title">{{ t('system.excel.importFields') }}</span>
        </template>

        <FieldConfigList v-model="formData.fields" />
      </a-card>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FieldConfigList from '@/components/excel/FieldConfigList.vue'

/**
 * 导入配置编辑弹窗
 *
 * 功能：
 * 1. 维护基础配置
 * 2. 维护导入字段配置
 * 3. 对外暴露表单数据供父组件提交
 */
interface Props {
  /** 弹窗是否打开 */
  open?: boolean
  /** 是否为编辑模式 */
  isEdit?: boolean
  /** 编辑详情数据 */
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  data: () => ({}),
})

const emit = defineEmits<{
  /** 更新弹窗显示状态 */
  (e: 'update:open', value: boolean): void
  /** 提交表单 */
  (e: 'submit'): void
}>()

const { t } = useI18n()

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const loading = ref(false)

const formData = reactive({
  id: undefined,
  tableName: '',
  tableCode: '',
  title: '',
  subtitle: '',
  version: 1,
  fields: [] as any[],
})

function initFormData() {
  formData.id = undefined
  formData.tableName = ''
  formData.tableCode = ''
  formData.title = ''
  formData.subtitle = ''
  formData.version = 1
  formData.fields = []
}

function loadEditData(data: any) {
  if (!data) return

  formData.id = data.id
  formData.tableName = data.tableName || ''
  formData.tableCode = data.tableCode || ''
  formData.title = data.title || ''
  formData.subtitle = data.subtitle || ''
  formData.version = data.version || 1
  formData.fields = (data.fields || []).map((field: any, index: number) => ({
    ...field,
    _key: field.id ? `field-${field.id}` : `field-${index}-${Date.now()}`,
  }))
}

defineExpose({
  formData,
})

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    emit('submit')
  } catch (error) {
    console.error('导入配置表单校验失败:', error)
  }
}

watch(
  () => props.open,
  (newValue) => {
    dialogVisible.value = newValue
    if (!newValue) return

    if (props.isEdit && props.data) {
      loadEditData(props.data)
    } else {
      initFormData()
    }
  },
  { immediate: true },
)

watch(
  () => dialogVisible.value,
  (newValue) => {
    emit('update:open', newValue)
  },
)
</script>

<style scoped lang="less">
.mb-16 {
  margin-bottom: 16px;
}

.mt-16 {
  margin-top: 16px;
}

.card-title {
  font-weight: 500;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.85);
}
</style>
