<template>
  <div class="i18n-input-container">
    <!-- 简单模式：单行输入 -->
    <div v-if="mode === 'simple'" class="simple-mode">
      <a-input
        v-model:value="defaultValue"
        :placeholder="placeholder"
        @update:value="handleSimpleChange"
      >
        <template #suffix>
          <GlobalOutlined 
            class="i18n-icon" 
            @click="showModal = true"
            title="配置多语言"
          />
        </template>
      </a-input>
    </div>

    <!-- 表格模式：直接显示表格 -->
    <div v-else-if="mode === 'table'" class="table-mode">
      <a-table
        :columns="columns"
        :data-source="tableData"
        :pagination="false"
        :loading="loading"
        size="small"
        bordered
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'langName'">
            <div class="lang-name">
              <span v-if="record.icon" class="lang-icon">{{ record.icon }}</span>
              <span>{{ record.langName }}</span>
              <a-tag v-if="record.isDefault" color="blue" size="small">默认</a-tag>
            </div>
          </template>
          <template v-else-if="column.key === 'value'">
            <a-input
              v-model:value="record.value"
              :placeholder="`请输入${record.langName}翻译`"
              @update:value="handleTableChange"
            />
          </template>
        </template>
      </a-table>
    </div>

    <!-- 弹窗模式 -->
    <a-modal
      v-model:open="showModal"
      title="多语言配置"
      width="700px"
      :ok-text="'确定'"
      :cancel-text="'取消'"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-spin :spinning="loading">
        <a-alert
          message="提示"
          description="请为每种语言配置对应的翻译文本。未填写的语言将使用默认值。"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />
        
        <a-table
          :columns="columns"
          :data-source="modalTableData"
          :pagination="false"
          size="middle"
          bordered
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'langName'">
              <div class="lang-name">
                <span v-if="record.icon" class="lang-icon">{{ record.icon }}</span>
                <span>{{ record.langName }}</span>
                <a-tag v-if="record.isDefault" color="blue" size="small">默认</a-tag>
              </div>
            </template>
            <template v-else-if="column.key === 'value'">
              <a-input
                v-model:value="record.value"
                :placeholder="`请输入${record.langName}翻译`"
              />
            </template>
          </template>
        </a-table>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { GlobalOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { listEnabledLanguages, type LanguageType } from '@/api/system/i18n'

/**
 * 多语言输入组件
 * 
 * 功能：
 * 1. 支持三种模式：simple（简单输入框+弹窗）、table（直接表格）、modal（仅弹窗）
 * 2. 自动从后端获取支持的语言列表
 * 3. 支持 v-model 双向绑定 JSON 字符串
 * 4. 用户友好的表格输入界面
 * 
 * 使用示例：
 * <I18nInput v-model="form.nameI18nJson" mode="simple" placeholder="请输入菜单名称" />
 * <I18nInput v-model="form.dictValueI18nJson" mode="table" />
 */

interface Props {
  /** v-model 绑定的值（JSON 字符串） */
  modelValue?: string
  /** 显示模式：simple=简单输入框, table=表格, modal=仅弹窗 */
  mode?: 'simple' | 'table' | 'modal'
  /** 占位符 */
  placeholder?: string
  /** 默认语言代码 */
  defaultLang?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  mode: 'simple',
  placeholder: '请输入',
  defaultLang: 'zh-CN'
})

const emit = defineEmits<{
  /**
   * 多语言值更新事件
   * 触发时机：用户修改任意语言的翻译内容时触发
   * @param value 新的多语言 JSON 字符串
   */
  'update:modelValue': [value: string]
}>()

// 状态
const loading = ref(false)
const showModal = ref(false)
const languages = ref<LanguageType[]>([])
const defaultValue = ref('')

// 表格数据
interface TableRow {
  langCode: string
  langName: string
  icon: string
  isDefault: boolean
  value: string
}

const tableData = ref<TableRow[]>([])
const modalTableData = ref<TableRow[]>([])

// 表格列配置
const columns = [
  {
    title: '语言',
    key: 'langName',
    dataIndex: 'langName',
    width: 200
  },
  {
    title: '翻译内容',
    key: 'value',
    dataIndex: 'value'
  }
]

/**
 * 加载语言列表
 */
const loadLanguages = async () => {
  loading.value = true
  try {
    const res = await listEnabledLanguages()
    languages.value = res || []
    initTableData()
  } catch (error) {
    console.error('加载语言列表失败:', error)
    message.error('加载语言列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 初始化表格数据
 */
const initTableData = () => {
  const i18nObj = parseI18nJson(props.modelValue)
  
  const data = languages.value.map(lang => ({
    langCode: lang.langCode,
    langName: lang.langName,
    icon: lang.icon || '',
    isDefault: lang.isDefault,
    value: i18nObj[lang.langCode] || ''
  }))
  
  tableData.value = data
  modalTableData.value = JSON.parse(JSON.stringify(data))
  
  // 设置默认值（用于简单模式）
  const defaultLang = languages.value.find(l => l.isDefault)
  if (defaultLang) {
    defaultValue.value = i18nObj[defaultLang.langCode] || ''
  }
}

/**
 * 解析 I18n JSON 字符串
 */
const parseI18nJson = (jsonStr: string): Record<string, string> => {
  if (!jsonStr) return {}
  try {
    return JSON.parse(jsonStr)
  } catch (error) {
    console.error('解析 I18n JSON 失败:', error)
    return {}
  }
}

/**
 * 生成 I18n JSON 字符串
 */
const generateI18nJson = (data: TableRow[]): string => {
  const obj: Record<string, string> = {}
  data.forEach(row => {
    if (row.value && row.value.trim()) {
      obj[row.langCode] = row.value.trim()
    }
  })
  return Object.keys(obj).length > 0 ? JSON.stringify(obj) : ''
}

/**
 * 简单模式输入变化
 */
const handleSimpleChange = (value?: string) => {
  if (typeof value === 'string') {
    defaultValue.value = value
  }
  // 更新默认语言的值
  const defaultLang = languages.value.find(l => l.isDefault)
  if (defaultLang) {
    const row = tableData.value.find(r => r.langCode === defaultLang.langCode)
    if (row) {
      row.value = defaultValue.value
      emitChange()
    }
  }
}

/**
 * 表格模式输入变化
 */
const handleTableChange = () => {
  queueMicrotask(() => {
    emitChange()
  })
}

/**
 * 弹窗确定
 */
const handleModalOk = () => {
  // 将弹窗数据同步到主数据
  tableData.value = JSON.parse(JSON.stringify(modalTableData.value))
  
  // 更新默认值
  const defaultLang = languages.value.find(l => l.isDefault)
  if (defaultLang) {
    const row = tableData.value.find(r => r.langCode === defaultLang.langCode)
    if (row) {
      defaultValue.value = row.value
    }
  }
  
  emitChange()
  showModal.value = false
}

/**
 * 弹窗取消
 */
const handleModalCancel = () => {
  // 恢复弹窗数据
  modalTableData.value = JSON.parse(JSON.stringify(tableData.value))
  showModal.value = false
}

/**
 * 触发值变化
 */
const emitChange = () => {
  const jsonStr = generateI18nJson(tableData.value)
  emit('update:modelValue', jsonStr)
}

/**
 * 监听外部值变化
 */
watch(() => props.modelValue, (newVal) => {
  if (languages.value.length > 0) {
    initTableData()
  }
})

/**
 * 组件挂载
 */
onMounted(() => {
  loadLanguages()
})
</script>

<style scoped lang="less">
.i18n-input-container {
  width: 100%;
  
  .simple-mode {
    .i18n-icon {
      color: var(--fx-primary, #1890ff);
      cursor: pointer;
      font-size: 16px;
      transition: all 0.3s;
      
      &:hover {
        color: var(--fx-primary-hover, #40a9ff);
        transform: scale(1.1);
      }
    }
  }
  
  .table-mode,
  .modal-mode {
    .lang-name {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .lang-icon {
        font-size: 18px;
      }
    }
  }
}

:deep(.ant-table) {
  .lang-name {
    display: flex;
    align-items: center;
    gap: 8px;
    
    .lang-icon {
      font-size: 18px;
    }
  }
}
</style>

