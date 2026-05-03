<template>
  <div class="field-config-list">
    <a-table
      :columns="columns"
      :data-source="fields"
      row-key="_key"
      :pagination="false"
      size="small"
      :scroll="{ x: 1500 }"
    >
      <template #bodyCell="{ column, record, index }">
        <!-- 字段名称 -->
        <template v-if="column.key === 'sheetCode'">
          <a-input
            v-model:value="record.sheetCode"
            :placeholder="t('system.excel.sheetCode')"
            @update:value="handleFieldChange"
          />
        </template>

        <template v-else-if="column.key === 'sheetName'">
          <a-input
            v-model:value="record.sheetName"
            :placeholder="t('system.excel.sheetName')"
            @update:value="handleFieldChange"
          />
        </template>

        <template v-else-if="column.key === 'fieldName'">
          <a-input
            v-model:value="record.fieldName"
            :placeholder="t('system.excel.importField')"
            @update:value="handleFieldChange"
          />
        </template>
        
        <!-- 字段类型 -->
        <template v-else-if="column.key === 'fieldType'">
            <a-select
              v-model:value="record.fieldType"
              :placeholder="t('system.excel.fieldType')"
              style="width: 100%"
              @change="handleFieldChange"
            >
            <a-select-option value="string">{{ t('system.excel.fieldTypes.string') }}</a-select-option>
            <a-select-option value="number">{{ t('system.excel.fieldTypes.number') }}</a-select-option>
            <a-select-option value="date">{{ t('system.excel.fieldTypes.date') }}</a-select-option>
            <a-select-option value="time">{{ t('system.excel.fieldTypes.time') }}</a-select-option>
            <a-select-option value="datetime">{{ t('system.excel.fieldTypes.datetime') }}</a-select-option>
            <a-select-option value="dict">{{ t('system.excel.fieldTypes.dict') }}</a-select-option>
          </a-select>
        </template>
        
        <!-- 数据源配置 -->
        <template v-else-if="column.key === 'dataSourceConfig'">
          <DataSourceConfig v-model="record.dataSourceConfig" />
        </template>
        
        <!-- 是否必填 -->
        <template v-else-if="column.key === 'required'">
          <a-switch
            v-model:checked="record.required"
            @change="handleFieldChange"
          />
        </template>
        
        <!-- 排序号 -->
        <template v-else-if="column.key === 'orderNum'">
          <a-input-number
            v-model:value="record.orderNum"
            :min="0"
            :step="1"
            style="width: 100px"
            @change="handleFieldChange"
          />
        </template>
        
        <!-- 操作 -->
        <template v-else-if="column.key === 'action'">
          <a-space :size="4">
            <a-button type="link" size="small" :disabled="index === 0" @click="moveUp(index)">
              {{ t('common.moveUp', '上移') }}
            </a-button>
            <a-button type="link" size="small" :disabled="index === fields.length - 1" @click="moveDown(index)">
              {{ t('common.moveDown', '下移') }}
            </a-button>
            <a-button type="link" size="small" danger @click="removeField(index)">
              {{ t('common.delete') }}
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import DataSourceConfig from './DataSourceConfig.vue'

/**
 * 字段配置列表编辑组件
 * 
 * 功能：
 * 1. 支持添加、删除字段
 * 2. 支持上移、下移字段顺序
 * 3. 集成数据源配置组件
 * 4. 支持 v-model 双向绑定字段配置数组
 * 
 * 使用示例：
 * <FieldConfigList v-model="form.fields" />
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

interface Props {
  /** v-model 绑定的字段配置数组 */
  modelValue?: Array<{
    fieldName?: string
    fieldType?: string
    dataSourceConfig?: any
    required?: boolean
    orderNum?: number
    [key: string]: any
  }>
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: () => []
})

const emit = defineEmits<{
  /**
   * 字段配置更新事件
   * 触发时机：用户修改字段配置时触发
   * @param value 新的字段配置数组
   */
  'update:modelValue': [value: Array<{
    fieldName?: string
    fieldType?: string
    dataSourceConfig?: any
    required?: boolean
    orderNum?: number
    [key: string]: any
  }>]
}>()

const { t } = useI18n()

/**
 * 表格列配置
 */
const columns = computed(() => [
  {
    title: t('system.excel.sheetCode'),
    key: 'sheetCode',
    dataIndex: 'sheetCode',
    width: 140
  },
  {
    title: t('system.excel.sheetName'),
    key: 'sheetName',
    dataIndex: 'sheetName',
    width: 140
  },
  {
    title: t('system.excel.importField'),
    key: 'fieldName',
    dataIndex: 'fieldName',
    width: 180
  },
  {
    title: t('system.excel.fieldType'),
    key: 'fieldType',
    dataIndex: 'fieldType',
    width: 140
  },
  {
    title: t('common.dataSourceConfig.dataSourceType'),
    key: 'dataSourceConfig',
    dataIndex: 'dataSourceConfig',
    width: 360
  },
  {
    title: t('system.excel.required'),
    key: 'required',
    dataIndex: 'required',
    width: 100,
    align: 'center'
  },
  {
    title: t('common.order'),
    key: 'orderNum',
    dataIndex: 'orderNum',
    width: 100,
    align: 'center'
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 180,
    align: 'center',
    fixed: 'right'
  }
])

// 字段列表
const fields = ref<Array<any & { _key: string }>>([])

/**
 * 解析初始值
 */
const parseInitialValue = () => {
  if (props.modelValue && Array.isArray(props.modelValue)) {
    return props.modelValue.map((item, index) => ({
      ...item,
      _key: item.id ? `field-${item.id}` : `field-${index}-${Date.now()}`
    }))
  }
  return []
}

/**
 * 初始化字段列表
 */
const initFields = () => {
  fields.value = parseInitialValue()
}

/**
 * 处理字段变化
 */
const handleFieldChange = () => {
  emitUpdate()
}

function createField() {
  return {
    fieldName: '',
    importField: '',
    sheetCode: 'main',
    sheetName: '',
    fieldType: 'string',
    dataSourceConfig: {},
    required: false,
    orderNum: fields.value.length,
    _key: `field-${Date.now()}-${Math.random()}`
  }
}

/**
 * 删除字段
 */
const removeField = (index: number) => {
  fields.value.splice(index, 1)
  resetOrder()
  emitUpdate()
}

/**
 * 上移字段
 */
const moveUp = (index: number) => {
  if (index > 0) {
    const temp = fields.value[index]
    fields.value[index] = fields.value[index - 1]
    fields.value[index - 1] = temp
    resetOrder()
    emitUpdate()
  }
}

/**
 * 下移字段
 */
const moveDown = (index: number) => {
  if (index < fields.value.length - 1) {
    const temp = fields.value[index]
    fields.value[index] = fields.value[index + 1]
    fields.value[index + 1] = temp
    resetOrder()
    emitUpdate()
  }
}

function resetOrder() {
  fields.value = fields.value.map((item, index) => ({
    ...item,
    orderNum: index,
  }))
}

/**
 * 触发更新事件
 */
const emitUpdate = () => {
  const cleanFields = fields.value.map(({ _key, ...rest }) => ({
    ...rest,
    importField: rest.importField || rest.fieldName,
    sheetCode: rest.sheetCode || 'main',
  }))
  emit('update:modelValue', cleanFields)
}

defineExpose({
  addField: () => {
    fields.value.push(createField())
    resetOrder()
    emitUpdate()
  },
})

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (newVal !== undefined) {
    initFields()
  }
}, { deep: true, immediate: true })

// 初始化
initFields()
</script>

<style scoped lang="less">
.field-config-list {
  width: 100%;

  :deep(.ant-table) {
    .ant-table-cell {
      vertical-align: top;
    }
  }
}
</style>
