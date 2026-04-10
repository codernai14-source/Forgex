<template>
  <div class="data-source-config">
    <!-- 数据源类型选择 -->
    <a-form-item :label="t('common.dataSourceConfig.dataSourceType')" name="dataSourceType">
      <a-select
        v-model:value="dataSourceType"
        :placeholder="t('common.dataSourceConfig.selectType')"
        @change="handleTypeChange"
      >
        <a-select-option value="DICT">{{ t('common.dataSourceConfig.typeDict') }}</a-select-option>
        <a-select-option value="JSON">{{ t('common.dataSourceConfig.typeJson') }}</a-select-option>
        <a-select-option value="PROVIDER">{{ t('common.dataSourceConfig.typeProvider') }}</a-select-option>
      </a-select>
    </a-form-item>
    
    <!-- DICT 类型：字典选择器 -->
    <a-form-item
      v-if="dataSourceType === 'DICT'"
      :label="t('common.dataSourceConfig.dictCode')"
      name="dictCode"
    >
      <a-select
        v-model:value="config.dictCode"
        :placeholder="t('common.dataSourceConfig.selectDict')"
        show-search
        :filter-option="filterDictOption"
        @change="handleConfigChange"
      >
        <a-select-option
          v-for="dict in dictList"
          :key="dict.dictCode"
          :value="dict.dictCode"
        >
          {{ dict.dictName }} ({{ dict.dictCode }})
        </a-select-option>
      </a-select>
    </a-form-item>
    
    <!-- JSON 类型：数组编辑器 -->
    <div v-if="dataSourceType === 'JSON'" class="json-editor-wrapper">
      <a-form-item
        :label="t('common.dataSourceConfig.dataSourceJson')"
        name="dataSourceJson"
      >
        <JsonArrayEditor
          v-model="config.dataSourceJson"
        />
      </a-form-item>
    </div>
    
    <!-- PROVIDER 类型：Provider 选择器 + 级联字段选择 -->
    <template v-if="dataSourceType === 'PROVIDER'">
      <a-form-item
        :label="t('common.dataSourceConfig.providerCode')"
        name="providerCode"
      >
        <a-select
          v-model:value="config.providerCode"
          :placeholder="t('common.dataSourceConfig.selectProvider')"
          show-search
          :filter-option="filterProviderOption"
          @change="handleProviderChange"
        >
          <a-select-option
            v-for="provider in providerList"
            :key="provider.providerCode"
            :value="provider.providerCode"
          >
            {{ provider.providerName }} ({{ provider.providerCode }})
          </a-select-option>
        </a-select>
      </a-form-item>
      
      <a-form-item
        v-if="config.providerCode"
        :label="t('common.dataSourceConfig.providerField')"
        name="providerField"
      >
        <a-select
          v-model:value="config.providerField"
          :placeholder="t('common.dataSourceConfig.selectProviderField')"
          show-search
          @change="handleConfigChange"
        >
          <a-select-option
            v-for="field in currentProviderFields"
            :key="field.fieldName"
            :value="field.fieldName"
          >
            {{ field.fieldLabel }} ({{ field.fieldName }})
          </a-select-option>
        </a-select>
      </a-form-item>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import JsonArrayEditor from './JsonArrayEditor.vue'
import { getDictList } from '@/api/system/dict'
import { getProviderList, getProviderFields } from '@/api/system/excel'

/**
 * 数据源配置组件
 * 
 * 功能：
 * 1. 根据数据源类型显示不同配置界面
 * 2. 支持 DICT（字典选择器）
 * 3. 支持 JSON（数组编辑器）
 * 4. 支持 PROVIDER（Provider 选择器 + 级联字段选择）
 * 5. 支持 v-model 双向绑定配置对象
 * 
 * 使用示例：
 * <DataSourceConfig v-model="form.dataSourceConfig" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** v-model 绑定的配置对象 */
  modelValue?: {
    dataSourceType?: string
    dictCode?: string
    dataSourceJson?: string
    providerCode?: string
    providerField?: string
  }
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => ({})
})

const emit = defineEmits<{
  /**
   * 配置值更新事件
   * 触发时机：用户修改任意配置项时触发
   * @param value 新的配置对象
   */
  'update:modelValue': [value: {
    dataSourceType?: string
    dictCode?: string
    dataSourceJson?: string
    providerCode?: string
    providerField?: string
  }]
}>()

const { t } = useI18n()

// 数据源类型
const dataSourceType = ref<string>(props.modelValue?.dataSourceType || '')

// 配置对象
const config = ref({
  dictCode: props.modelValue?.dictCode || '',
  dataSourceJson: props.modelValue?.dataSourceJson || '',
  providerCode: props.modelValue?.providerCode || '',
  providerField: props.modelValue?.providerField || ''
})

// 字典列表
const dictList = ref<Array<{ dictCode: string; dictName: string }>>([])

// Provider 列表
const providerList = ref<Array<{ providerCode: string; providerName: string }>>([])

// 当前 Provider 的字段列表
const currentProviderFields = ref<Array<{ fieldName: string; fieldLabel: string }>>([])

/**
 * 字典列表加载
 */
const loadDictList = async () => {
  try {
    const res = await getDictList({})
    dictList.value = res || []
  } catch (error) {
    console.error('加载字典列表失败:', error)
  }
}

/**
 * Provider 列表加载
 */
const loadProviderList = async () => {
  try {
    const res = await getProviderList({})
    providerList.value = res || []
  } catch (error) {
    console.error('加载 Provider 列表失败:', error)
  }
}

/**
 * 加载 Provider 字段列表
 */
const loadProviderFields = async (providerCode: string) => {
  try {
    const res = await getProviderFields({ providerCode })
    currentProviderFields.value = res || []
  } catch (error) {
    console.error('加载 Provider 字段列表失败:', error)
    currentProviderFields.value = []
  }
}

/**
 * 过滤字典选项
 */
const filterDictOption = (input: string, option: any) => {
  const text = (option.children as string).toLowerCase()
  return text.includes(input.toLowerCase())
}

/**
 * 过滤 Provider 选项
 */
const filterProviderOption = (input: string, option: any) => {
  const text = (option.children as string).toLowerCase()
  return text.includes(input.toLowerCase())
}

/**
 * 处理类型变化
 */
const handleTypeChange = (value: string) => {
  // 切换类型时清空其他类型的配置
  if (value !== 'DICT') {
    config.value.dictCode = ''
  }
  if (value !== 'JSON') {
    config.value.dataSourceJson = ''
  }
  if (value !== 'PROVIDER') {
    config.value.providerCode = ''
    config.value.providerField = ''
    currentProviderFields.value = []
  }
  
  emitConfigChange()
}

/**
 * 处理 Provider 变化
 */
const handleProviderChange = (value: string) => {
  config.value.providerField = ''
  if (value) {
    loadProviderFields(value)
  } else {
    currentProviderFields.value = []
  }
  emitConfigChange()
}

/**
 * 处理配置变化
 */
const handleConfigChange = () => {
  emitConfigChange()
}

/**
 * 触发配置变化
 */
const emitConfigChange = () => {
  emit('update:modelValue', {
    dataSourceType: dataSourceType.value,
    dictCode: config.value.dictCode,
    dataSourceJson: config.value.dataSourceJson,
    providerCode: config.value.providerCode,
    providerField: config.value.providerField
  })
}

/**
 * 监听配置对象内部变化
 */
watch(() => config.value, {
  handler: () => {
    emitConfigChange()
  },
  deep: true
})

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal) {
    dataSourceType.value = newVal.dataSourceType || ''
    config.value = {
      dictCode: newVal.dictCode || '',
      dataSourceJson: newVal.dataSourceJson || '',
      providerCode: newVal.providerCode || '',
      providerField: newVal.providerField || ''
    }
  }
}, { deep: true })

// 初始化
onMounted(() => {
  loadDictList()
  loadProviderList()
})
</script>

<style scoped lang="less">
.data-source-config {
  width: 100%;
  
  .json-editor-wrapper {
    width: 100%;
  }
  
  :deep(.ant-form-item) {
    margin-bottom: 16px;
  }
}
</style>
