<template>
  <div class="json-array-editor">
    <a-card size="small" :bordered="false">
      <div class="header-actions">
        <span class="title">{{ t('common.jsonArrayEditor.optionsList') }}</span>
        <a-button type="primary" size="small" @click="addOption">
          <PlusOutlined />
          {{ t('common.jsonArrayEditor.addOption') }}
        </a-button>
      </div>
      
      <a-table
        :columns="columns"
        :data-source="options"
        :pagination="false"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record, index }">
          <!-- 值列 -->
          <template v-if="column.key === 'value'">
            <a-input
              v-model:value="record.value"
              :placeholder="t('common.jsonArrayEditor.valuePlaceholder')"
              @update:value="handleInputChange"
            />
          </template>
          
          <!-- 标签列 -->
          <template v-else-if="column.key === 'label'">
            <a-input
              v-model:value="record.label"
              :placeholder="t('common.jsonArrayEditor.labelPlaceholder')"
              @update:value="handleInputChange"
            />
          </template>
          
 <!-- 操作列 -->
          <template v-else-if="column.key === 'action'">
            <a
              type="link"
              danger
              size="small"
              @click="removeOption(index)"
            >
              <DeleteOutlined />
              {{ t('common.remove') }}
            </a>
          </template>
        </template>
      </a-table>
      
      <!-- JSON 预览 -->
      <a-divider orientation="left">{{ t('common.jsonArrayEditor.jsonPreview') }}</a-divider>
      <pre class="json-preview">{{ jsonPreview }}</pre>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons-vue'

/**
 * JSON 数组编辑器组件
 * 
 * 功能：
 * 1. 支持添加、删除选项
 * 2. 支持 value|label 格式
 * 3. 实时预览 JSON 格式
 * 4. 支持 v-model 双向绑定 JSON 字符串
 * 
 * 使用示例：
 * <JsonArrayEditor v-model="form.dataSourceJson" />
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
   * 触发时机：用户修改选项列表时触发
   * @param value 新的 JSON 字符串
   */
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

/**
 * 表格列配置
 */
const columns = [
  {
    title: t('common.jsonArrayEditor.columnValue'),
    key: 'value',
    dataIndex: 'value',
    width: 200
  },
  {
    title: t('common.jsonArrayEditor.columnLabel'),
    key: 'label',
    dataIndex: 'label',
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 100,
    align: 'center'
  }
]

// 选项列表
const options = ref<Array<{ value: string; label: string }>>([])

/**
 * 解析初始值
 */
const parseInitialValue = () => {
  try {
    if (props.modelValue) {
      const parsed = JSON.parse(props.modelValue)
      if (Array.isArray(parsed)) {
        return parsed.map((item, index) => ({
          value: item.value || '',
          label: item.label || '',
          _key: `option-${index}-${Date.now()}`
        }))
      }
    }
  } catch (error) {
    console.error('解析 JSON 失败:', error)
  }
  return []
}

/**
 * 初始化选项列表
 */
const initOptions = () => {
  options.value = parseInitialValue()
}

/**
 * 生成 JSON 预览
 */
const jsonPreview = computed(() => {
  const cleanOptions = options.value.map(({ value, label }) => ({
    value: value.trim(),
    label: label.trim()
  })).filter(item => item.value || item.label)
  
  return cleanOptions.length > 0 ? JSON.stringify(cleanOptions, null, 2) : '[]'
})

/**
 * 处理输入变化
 */
const handleInputChange = () => {
  emit('update:modelValue', jsonPreview.value)
}

/**
 * 添加选项
 */
const addOption = () => {
  options.value.push({
    value: '',
    label: '',
    _key: `option-${Date.now()}-${Math.random()}`
  })
  handleInputChange()
}

/**
 * 删除选项
 */
const removeOption = (index: number) => {
  options.value.splice(index, 1)
  handleInputChange()
}

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal !== undefined) {
    initOptions()
  }
}, { deep: true, immediate: true })

// 初始化
initOptions()
</script>

<style scoped lang="less">
.json-array-editor {
  width: 100%;
  
  .header-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .title {
      font-weight: 500;
      font-size: 14px;
      color: rgba(0, 0, 0, 0.85);
    }
  }
  
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
}
</style>
