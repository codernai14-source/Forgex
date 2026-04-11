<template>
  <div class="i18n-json-editor">
    <a-card size="small" :bordered="false">
      <a-row :gutter="16">
        <!-- 中文输入 -->
        <a-col :span="12">
          <a-form-item :label="t('common.i18nJsonEditor.zhCn')" label-align="left">
            <a-textarea
              v-model:value="zhCnValue"
              :placeholder="t('common.i18nJsonEditor.zhCnPlaceholder')"
              :rows="6"
              @update:value="handleInputChange"
            />
          </a-form-item>
        </a-col>
        
        <!-- 英文输入 -->
        <a-col :span="12">
          <a-form-item :label="t('common.i18nJsonEditor.enUs')" label-align="left">
            <a-textarea
              v-model:value="enUsValue"
              :placeholder="t('common.i18nJsonEditor.enUsPlaceholder')"
              :rows="6"
              @update:value="handleInputChange"
            />
          </a-form-item>
        </a-col>
      </a-row>
      
      <!-- JSON 预览 -->
      <a-divider orientation="left">{{ t('common.i18nJsonEditor.jsonPreview') }}</a-divider>
      <pre class="json-preview">{{ jsonPreview }}</pre>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'

/**
 * 国际化 JSON 编辑器组件
 * 
 * 功能：
 * 1. 支持中文、英文双语言输入
 * 2. 实时预览 JSON 格式
 * 3. 支持 v-model 双向绑定 JSON 字符串
 * 
 * 使用示例：
 * <I18nJsonEditor v-model="form.i18nJson" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** v-model 绑定的 JSON 字符串 */
  modelValue?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: ''
})

const emit = defineEmits<{
  /**
   * JSON 值更新事件
   * 触发时机：用户修改任意语言输入时触发
   * @param value 新的 JSON 字符串
   */
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

// 解析初始值
const parseInitialValue = () => {
  try {
    if (props.modelValue) {
      const parsed = JSON.parse(props.modelValue)
      return {
        zhCn: parsed['zh-CN'] || '',
        enUs: parsed['en-US'] || ''
      }
    }
  } catch (error) {
    console.error('解析 JSON 失败:', error)
  }
  return { zhCn: '', enUs: '' }
}

// 输入值
const zhCnValue = ref(parseInitialValue().zhCn)
const enUsValue = ref(parseInitialValue().enUs)

/**
 * 生成 JSON 预览
 */
const jsonPreview = computed(() => {
  const obj: Record<string, string> = {}
  if (zhCnValue.value && zhCnValue.value.trim()) {
    obj['zh-CN'] = zhCnValue.value.trim()
  }
  if (enUsValue.value && enUsValue.value.trim()) {
    obj['en-US'] = enUsValue.value.trim()
  }
  return Object.keys(obj).length > 0 ? JSON.stringify(obj, null, 2) : '{}'
})

/**
 * 处理输入变化
 */
const handleInputChange = () => {
  emit('update:modelValue', jsonPreview.value)
}

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    const { zhCn, enUs } = parseInitialValue()
    zhCnValue.value = zhCn
    enUsValue.value = enUs
  }
}, { deep: true })
</script>

<style scoped lang="less">
.i18n-json-editor {
  width: 100%;
  
  .json-preview {
    background-color: #f5f5f5;
    border: 1px solid #d9d9d9;
    border-radius: 4px;
    padding: 12px;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    color: #333;
    white-space: pre-wrap;
    word-wrap: break-word;
    min-height: 100px;
    margin-top: 8px;
  }
  
  :deep(.ant-form-item) {
    margin-bottom: 12px;
  }
  
  :deep(.ant-form-item-label) {
    padding-bottom: 4px;
  }
}
</style>
