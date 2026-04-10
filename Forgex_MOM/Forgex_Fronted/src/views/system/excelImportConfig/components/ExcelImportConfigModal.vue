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
      <!-- 基础配置 -->
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
        
        <a-form-item
          :label="t('system.excel.title')"
          name="title"
        >
          <a-input
            v-model:value="formData.title"
            :placeholder="t('system.excel.pleaseInputTitle')"
          />
        </a-form-item>
        
        <a-form-item
          :label="t('system.excel.subtitle')"
          name="subtitle"
        >
          <a-input
            v-model:value="formData.subtitle"
            :placeholder="t('system.excel.pleaseInputSubtitle')"
          />
        </a-form-item>
        
        <a-form-item
          :label="t('system.excel.version')"
          name="version"
        >
          <a-input-number
            v-model:value="formData.version"
            :min="1"
            :step="1"
            style="width: 100%"
          />
        </a-form-item>
      </a-card>
      
      <!-- 字段配置 -->
      <a-card size="small" :bordered="false" class="mt-16">
        <template #title>
          <span class="card-title">{{ t('system.excel.importFields') }}</span>
        </template>
        
        <FieldConfigList
          v-model="formData.fields"
        />
      </a-card>
    </a-form>
  </BaseFormDialog>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import type { FormInstance } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import FieldConfigList from '@/components/excel/FieldConfigList.vue'

/**
 * 导入配置编辑弹窗组件
 * 
 * 功能：
 * 1. 包含主配置表单和字段配置列表
 * 2. 支持新增和编辑模式
 * 3. 表单验证
 * 4. 数据提交处理
 * 
 * 使用示例：
 * <ExcelImportConfigModal v-model:open="visible" :is-edit="isEdit" :data="editData" @success="handleSuccess" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** 对话框是否打开 */
  open?: boolean
  /** 是否为编辑模式 */
  isEdit?: boolean
  /** 编辑时的初始数据 */
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  data: () => ({})
})

const emit = defineEmits<{
  /**
   * 更新对话框打开状态
   */
  (e: 'update:open', value: boolean): void
  /**
   * 提交事件
   */
  (e: 'submit'): void
}>()

const { t } = useI18n()

// 对话框状态
const dialogVisible = ref(false)

// 表单引用
const formRef = ref<FormInstance>()

// 加载状态
const loading = ref(false)

// 表单数据
const formData = reactive({
  id: undefined,
  tableName: '',
  tableCode: '',
  title: '',
  subtitle: '',
  version: 1,
  fields: []
})

/**
 * 初始化表单数据
 */
const initFormData = () => {
  formData.id = undefined
  formData.tableName = ''
  formData.tableCode = ''
  formData.title = ''
  formData.subtitle = ''
  formData.version = 1
  formData.fields = []
}

/**
 * 加载编辑数据
 */
const loadEditData = (data: any) => {
  if (data) {
    formData.id = data.id
    formData.tableName = data.tableName || ''
    formData.tableCode = data.tableCode || ''
    formData.title = data.title || ''
    formData.subtitle = data.subtitle || ''
    formData.version = data.version || 1
    formData.fields = (data.fields || []).map((field: any, index: number) => ({
      ...field,
      _key: field.id ? `field-${field.id}` : `field-${index}-${Date.now()}`
    }))
  }
}

// 暴露表单数据给父组件
defineExpose({
  formData
})

/**
 * 处理提交
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    // 触发父组件的提交事件，由父组件处理保存逻辑
    emit('submit')
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

/**
 * 监听打开状态变化
 */
watch(() => props.open, (newVal) => {
  dialogVisible.value = newVal
  if (newVal) {
    if (props.isEdit && props.data) {
      loadEditData(props.data)
    } else {
      initFormData()
    }
  }
}, { immediate: true })

/**
 * 监听对话框关闭
 */
watch(() => dialogVisible.value, (newVal) => {
  emit('update:open', newVal)
})
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
