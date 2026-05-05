<template>
  <BaseFormDialog
    v-model:open="dialogVisible"
    :title="isEdit ? t('system.excel.editImportConfig') : t('system.excel.addImportConfig')"
    :width="1200"
    :loading="loading"
    :mask-closable="true"
    @submit="handleSubmit"
  >
    <a-tabs v-model:activeKey="activeTab" type="card">
      <a-tab-pane key="basic" :tab="t('system.excel.basicDataTab')">
        <a-form
          ref="formRef"
          :model="formData"
          :label-col="{ span: 5 }"
          :wrapper-col="{ span: 19 }"
        >
          <a-row :gutter="16">
            <a-col :span="12">
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
            </a-col>
            <a-col :span="12">
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
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item
                :label="t('system.excel.handlerBeanName')"
                name="handlerBeanName"
                :rules="{ required: true, message: t('system.excel.pleaseInputHandlerBeanName') }"
              >
                <a-input
                  v-model:value="formData.handlerBeanName"
                  :placeholder="t('system.excel.pleaseInputHandlerBeanName')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item
                :label="t('system.excel.importPermission')"
                name="importPermission"
                :rules="{ required: true, message: t('system.excel.pleaseInputImportPermission') }"
              >
                <a-input
                  v-model:value="formData.importPermission"
                  :placeholder="t('system.excel.pleaseInputImportPermission')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.excel.title')" name="title">
                <a-input
                  v-model:value="formData.title"
                  :placeholder="t('system.excel.pleaseInputTitle')"
                />
              </a-form-item>
            </a-col>
            <a-col :span="12">
              <a-form-item :label="t('system.excel.subtitle')" name="subtitle">
                <a-input
                  v-model:value="formData.subtitle"
                  :placeholder="t('system.excel.pleaseInputSubtitle')"
                />
              </a-form-item>
            </a-col>
          </a-row>

          <a-row :gutter="16">
            <a-col :span="12">
              <a-form-item :label="t('system.excel.version')" name="version">
                <a-input-number
                  v-model:value="formData.version"
                  :min="1"
                  :step="1"
                  style="width: 100%"
                />
              </a-form-item>
            </a-col>
          </a-row>
        </a-form>
      </a-tab-pane>

      <a-tab-pane key="fields" :tab="t('system.excel.importFields')">
        <div class="field-toolbar">
          <a-button type="primary" @click="addField">
            {{ t('common.add') }}
          </a-button>
        </div>

        <FieldConfigList ref="fieldConfigListRef" v-model="formData.fields" />
      </a-tab-pane>
    </a-tabs>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FieldConfigList from '@/components/excel/FieldConfigList.vue'

interface Props {
  open?: boolean
  isEdit?: boolean
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  data: () => ({}),
})

const emit = defineEmits<{
  (e: 'update:open', value: boolean): void
  (e: 'submit'): void
}>()

const { t } = useI18n()

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()
const fieldConfigListRef = ref<InstanceType<typeof FieldConfigList> | null>(null)
const loading = ref(false)
const activeTab = ref('basic')

const formData = reactive({
  id: undefined,
  tableName: '',
  tableCode: '',
  handlerBeanName: '',
  importPermission: '',
  title: '',
  subtitle: '',
  version: 1,
  fields: [] as any[],
})

function initFormData() {
  formData.id = undefined
  formData.tableName = ''
  formData.tableCode = ''
  formData.handlerBeanName = ''
  formData.importPermission = ''
  formData.title = ''
  formData.subtitle = ''
  formData.version = 1
  formData.fields = []
  activeTab.value = 'basic'
}

function loadEditData(data: any) {
  if (!data) return

  formData.id = data.id
  formData.tableName = data.tableName || ''
  formData.tableCode = data.tableCode || ''
  formData.handlerBeanName = data.handlerBeanName || ''
  formData.importPermission = data.importPermission || ''
  formData.title = data.title || ''
  formData.subtitle = data.subtitle || ''
  formData.version = data.version || 1
  formData.fields = (data.items || data.fields || []).map((field: any) => normalizeField(field))
}

function normalizeField(field: any) {
  return {
    ...field,
    fieldName: field.importField || field.fieldName || '',
    importField: field.importField || field.fieldName || '',
    sheetCode: field.sheetCode || 'main',
    sheetName: field.sheetName || '',
    dataSourceConfig: field.dataSourceConfig || {
      dataSourceType: field.dataSourceType && field.dataSourceType !== 'NONE' ? field.dataSourceType : '',
      dictCode: field.dictCode || (field.dataSourceType === 'DICT' ? field.dataSourceValue : ''),
      dataSourceJson: field.dataSourceType === 'JSON' ? field.dataSourceValue : '',
      providerCode: field.dataSourceType === 'PROVIDER' ? String(field.dataSourceValue || '').split(':')[0] : '',
      providerField: field.dataSourceType === 'PROVIDER' ? String(field.dataSourceValue || '').split(':')[1] : '',
    },
  }
}

defineExpose({
  formData,
})

async function handleSubmit() {
  try {
    await formRef.value?.validate()
    if (!formData.fields.length) {
      activeTab.value = 'fields'
      return
    }
    emit('submit')
  } catch (error) {
    console.error('import config form validate failed:', error)
  }
}

function addField() {
  fieldConfigListRef.value?.addField?.()
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
.field-toolbar {
  margin-bottom: 16px;
}
</style>
