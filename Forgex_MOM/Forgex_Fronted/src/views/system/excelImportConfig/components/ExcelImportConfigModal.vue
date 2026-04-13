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
      <!-- 鍩虹閰嶇疆 -->
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
      
      <!-- 瀛楁閰嶇疆 -->
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
 * 瀵煎叆閰嶇疆缂栬緫寮圭獥缁勪欢
 * 
 * 鍔熻兘锛?
 * 1. 鍖呭惈涓婚厤缃〃鍗曞拰瀛楁閰嶇疆鍒楄〃
 * 2. 鏀寔鏂板鍜岀紪杈戞ā寮?
 * 3. 琛ㄥ崟楠岃瘉
 * 4. 鏁版嵁鎻愪氦澶勭悊
 * 
 * 浣跨敤绀轰緥锛?
 * <ExcelImportConfigModal v-model:open="visible" :is-edit="isEdit" :data="editData" @success="handleSuccess" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** 瀵硅瘽妗嗘槸鍚︽墦寮€ */
  open?: boolean
  /** 鏄惁涓虹紪杈戞ā寮?*/
  isEdit?: boolean
  /** 缂栬緫鏃剁殑鍒濆鏁版嵁 */
  data?: any
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  isEdit: false,
  data: () => ({})
})

const emit = defineEmits<{
  /**
   * 鏇存柊瀵硅瘽妗嗘墦寮€鐘舵€?
   */
  (e: 'update:open', value: boolean): void
  /**
   * 鎻愪氦浜嬩欢
   */
  (e: 'submit'): void
}>()

const { t } = useI18n()

// 瀵硅瘽妗嗙姸鎬?
const dialogVisible = ref(false)

// 琛ㄥ崟寮曠敤
const formRef = ref<FormInstance>()

// 鍔犺浇鐘舵€?
const loading = ref(false)

// 琛ㄥ崟鏁版嵁
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
 * 鍒濆鍖栬〃鍗曟暟鎹?
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
 * 鍔犺浇缂栬緫鏁版嵁
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

// 鏆撮湶琛ㄥ崟鏁版嵁缁欑埗缁勪欢
defineExpose({
  formData
})

/**
 * 澶勭悊鎻愪氦
 */
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
    
    // 瑙﹀彂鐖剁粍浠剁殑鎻愪氦浜嬩欢锛岀敱鐖剁粍浠跺鐞嗕繚瀛橀€昏緫
    emit('submit')
  } catch (error) {
    console.error('琛ㄥ崟楠岃瘉澶辫触:', error)
  }
}

/**
 * 鐩戝惉鎵撳紑鐘舵€佸彉鍖?
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
 * 鐩戝惉瀵硅瘽妗嗗叧闂?
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
