<template>
  <div class="i18n-input-container">
    <!-- 简单模式：单行输入 / 多行输入 -->
    <div v-if="mode === 'simple'" class="simple-mode">
      <!-- textarea 类型 -->
      <div v-if="type === 'textarea'" class="textarea-wrapper">
        <a-textarea
          ref="simpleTextareaRef"
          v-model:value="defaultValue"
          :placeholder="placeholder"
          :rows="rows"
        />
        <div class="textarea-actions">
          <GlobalOutlined
            class="i18n-icon"
            @click="showModal = true"
            :title="t('common.i18nInput.configureTooltip')"
          />
        </div>
        <!-- 占位符工具栏 -->
        <div v-if="showPlaceholders" class="placeholder-toolbar">
          <a-space wrap>
            <span class="toolbar-label">{{ t('common.i18nInput.placeholderLabel') }}</span>
            <a-tag
              v-for="ph in builtinPlaceholders"
              :key="ph.key"
              color="blue"
              class="placeholder-tag"
              @click="insertPlaceholderAtSimple(ph.key)"
            >
              <template #icon><PlusOutlined /></template>
              {{ ph.label }}
            </a-tag>
          </a-space>
        </div>
      </div>
      <!-- input 类型（默认） -->
      <a-input
        v-else
        v-model:value="defaultValue"
        :placeholder="placeholder"
      >
        <template #suffix>
          <GlobalOutlined 
            class="i18n-icon" 
            @click="showModal = true"
            :title="t('common.i18nInput.configureTooltip')"
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
              <a-tag v-if="record.isDefault" color="blue" size="small">{{ t('common.i18nInput.defaultTag') }}</a-tag>
            </div>
          </template>
          <template v-else-if="column.key === 'value'">
            <a-textarea
              v-if="type === 'textarea'"
              v-model:value="record.value"
              :placeholder="t('common.i18nInput.placeholder', { lang: record.langName })"
              :rows="rows"
            />
            <a-input
              v-else
              v-model:value="record.value"
              :placeholder="t('common.i18nInput.placeholder', { lang: record.langName })"
            />
          </template>
        </template>
      </a-table>
      <!-- 表格模式下的占位符工具栏 -->
      <div v-if="showPlaceholders" class="placeholder-toolbar" style="margin-top: 8px;">
        <a-space wrap>
          <span class="toolbar-label">{{ t('common.i18nInput.placeholderLabel') }}</span>
          <a-tag
            v-for="ph in builtinPlaceholders"
            :key="ph.key"
            color="blue"
            class="placeholder-tag"
          >
            {{ ph.key }}
          </a-tag>
        </a-space>
      </div>
    </div>

      <!-- 弹窗模式 -->
    <a-modal
      v-model:open="showModal"
      :title="t('common.i18nInput.modalTitle')"
      :width="computedModalWidth"
      wrapClassName="i18n-input-modal-wrap"
      :body-style="modalBodyStyle"
      :ok-text="t('common.ok')"
      :cancel-text="t('common.cancel')"
      @ok="handleModalOk"
      @cancel="handleModalCancel"
    >
      <a-spin :spinning="loading">
        <div class="modal-content-layout" :class="{ 'with-sidebar': isPlaceholderSidebarMode }">
          <div class="modal-content-main">
            <a-alert
              :message="t('common.i18nInput.tipTitle')"
              :description="t('common.i18nInput.tipDesc')"
              type="info"
              show-icon
              class="modal-tip-alert"
            />

            <div class="modal-table-scroll-area">
              <a-table
                class="modal-i18n-table"
                :columns="columns"
                :data-source="modalTableData"
                :pagination="false"
                :scroll="modalTableScroll"
                size="middle"
                bordered
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'langName'">
                    <div class="lang-name">
                      <span v-if="record.icon" class="lang-icon">{{ record.icon }}</span>
                      <span>{{ record.langName }}</span>
                      <a-tag v-if="record.isDefault" color="blue" size="small">{{ t('common.i18nInput.defaultTag') }}</a-tag>
                    </div>
                  </template>
                  <template v-else-if="column.key === 'value'">
                    <a-textarea
                      v-if="type === 'textarea'"
                      :ref="(el: any) => setModalTextareaRef(record.langCode, el)"
                      v-model:value="record.value"
                      :placeholder="t('common.i18nInput.placeholder', { lang: record.langName })"
                      :rows="rows"
                      @focus="handleModalFieldFocus(record.langCode)"
                    />
                    <a-input
                      v-else
                      v-model:value="record.value"
                      :placeholder="t('common.i18nInput.placeholder', { lang: record.langName })"
                      @focus="handleModalFieldFocus(record.langCode)"
                    />
                  </template>
                </template>
              </a-table>
            </div>
          </div>

          <aside v-if="isPlaceholderSidebarMode" class="modal-placeholder-sidebar">
            <div class="placeholder-panel">
              <div class="placeholder-panel__header">
                <span class="toolbar-label">{{ t('common.i18nInput.placeholderLabel') }}</span>
              </div>
              <div class="placeholder-panel__list">
                <button
                  v-for="ph in builtinPlaceholders"
                  :key="ph.key"
                  type="button"
                  class="placeholder-action"
                  @click="insertPlaceholderAtModal(ph.key)"
                >
                  <span class="placeholder-action__icon">
                    <PlusOutlined />
                  </span>
                  <span class="placeholder-action__content">
                    <span class="placeholder-action__label">{{ ph.label }}</span>
                    <span class="placeholder-action__key">{{ ph.key }}</span>
                  </span>
                </button>
              </div>
              <div class="placeholder-hint">{{ t('common.i18nInput.placeholderHint') }}</div>
            </div>
          </aside>
        </div>
      </a-spin>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { GlobalOutlined, PlusOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { listEnabledLanguages, type LanguageType } from '@/api/system/i18n'

const { t } = useI18n()

/**
 * 多语言输入组件
 *
 * 功能：
 * 1. 支持三种模式：simple、table、modal
 * 2. 支持 input 和 textarea 两种输入类型
 * 3. 支持占位符快速插入
 * 4. 自动从后端获取启用的语言列表
 * 5. 支持通过 v-model 双向绑定 JSON 字符串
 *
 * 使用示例：
 * <I18nInput v-model="form.nameI18nJson" mode="simple" placeholder="请输入菜单名称" />
 * <I18nInput v-model="form.bodyI18nJson" mode="simple" type="textarea" :rows="4" :show-placeholders="true" />
 */

interface Props {
  /** v-model 绑定值（JSON 字符串） */
  modelValue?: string
  /** 显示模式：simple=简单输入框，table=表格，modal=仅弹窗 */
  mode?: 'simple' | 'table' | 'modal'
  /** 输入类型：input=单行输入，textarea=多行文本 */
  type?: 'input' | 'textarea'
  /** 占位符 */
  placeholder?: string
  /** 默认语言代码 */
  defaultLang?: string
  /** textarea 行数（仅在 type='textarea' 时生效） */
  rows?: number
  /** 是否显示占位符快速插入工具栏 */
  showPlaceholders?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  mode: 'simple',
  type: 'input',
  placeholder: '',
  defaultLang: 'zh-CN',
  rows: 4,
  showPlaceholders: false,
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
const simpleTextareaRef = ref<any>(null)
const modalTextareaRefs = ref<Record<string, any>>({})
const lastFocusedModalLang = ref<string>('')
const viewportHeight = ref(typeof window !== 'undefined' ? window.innerHeight : 900)
const syncingFromModel = ref(false)

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

/**
 * 内置占位符列表
 */
const builtinPlaceholders = computed(() => [
  { key: '${userName}', label: t('common.i18nInput.phUserName') },
  { key: '${userAccount}', label: t('common.i18nInput.phUserAccount') },
  { key: '${tenantName}', label: t('common.i18nInput.phTenantName') },
  { key: '${currentTime}', label: t('common.i18nInput.phCurrentTime') },
  { key: '${title}', label: t('common.i18nInput.phTitle') },
  { key: '${content}', label: t('common.i18nInput.phContent') },
  { key: '${linkUrl}', label: t('common.i18nInput.phLinkUrl') },
])

const isPlaceholderSidebarMode = computed(() => props.showPlaceholders && props.type === 'textarea')

const computedModalWidth = computed(() => {
  if (isPlaceholderSidebarMode.value) {
    return 1280
  }
  if (props.type === 'textarea') {
    return 860
  }
  return 700
})

const modalBodyStyle = computed(() => ({
  overflow: 'hidden',
  padding: '16px 20px',
}))

const modalTableScroll = computed(() => ({
  y: Math.max(viewportHeight.value - (isPlaceholderSidebarMode.value ? 360 : 320), 320),
}))

function updateViewportHeight() {
  if (typeof window === 'undefined') {
    return
  }
  viewportHeight.value = window.innerHeight
}

/**
 * 淇濆瓨寮圭獥涓?textarea 鐨勫紩鐢?
 */
function setModalTextareaRef(langCode: string, el: any) {
  if (el) {
    modalTextareaRefs.value[langCode] = el
  }
}

function handleModalFieldFocus(langCode: string) {
  lastFocusedModalLang.value = langCode
}

/**
  * ??? textarea ?????????????
 */
function insertPlaceholderAtSimple(placeholder: string) {
  const textareaEl = simpleTextareaRef.value?.$el?.querySelector?.('textarea')
    || simpleTextareaRef.value?.resizableTextArea?.textArea
  if (textareaEl) {
    const start = textareaEl.selectionStart ?? defaultValue.value.length
    const end = textareaEl.selectionEnd ?? start
    const text = defaultValue.value
    defaultValue.value = text.substring(0, start) + placeholder + text.substring(end)
    // 恢复光标
    setTimeout(() => {
      textareaEl.focus()
      textareaEl.setSelectionRange(start + placeholder.length, start + placeholder.length)
    }, 0)
  } else {
    // 降级方案：追加到末尾
    defaultValue.value += placeholder
  }
}

/**
 * 在弹窗模式 textarea 中插入占位符
 * 如果没有聚焦过任何语言行，默认插入到第一个（默认语言）行
 */
function insertPlaceholderAtModal(placeholder: string) {
  const targetLang = lastFocusedModalLang.value
    || languages.value.find(l => l.isDefault)?.langCode
    || modalTableData.value[0]?.langCode
  if (!targetLang) return

  const row = modalTableData.value.find(r => r.langCode === targetLang)
  if (!row) return

  const refEl = modalTextareaRefs.value[targetLang]
  const textareaEl = refEl?.$el?.querySelector?.('textarea')
    || refEl?.resizableTextArea?.textArea
  if (textareaEl) {
    const start = textareaEl.selectionStart ?? row.value.length
    const end = textareaEl.selectionEnd ?? start
    row.value = row.value.substring(0, start) + placeholder + row.value.substring(end)
    setTimeout(() => {
      textareaEl.focus()
      textareaEl.setSelectionRange(start + placeholder.length, start + placeholder.length)
    }, 0)
  } else {
    row.value += placeholder
  }
}

/**
 * 表格列配置（标题随语言切换）
 */
const columns = computed(() => [
  {
    title: t('common.i18nInput.columnLang'),
    key: 'langName',
    dataIndex: 'langName',
    width: 200,
  },
  {
    title: t('common.i18nInput.columnValue'),
    key: 'value',
    dataIndex: 'value',
  },
])

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
    message.error(t('common.i18nInput.loadLanguagesFailed'))
  } finally {
    loading.value = false
  }
}

/**
 * 初始化表格数据
 */
const initTableData = () => {
  syncingFromModel.value = true
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
  queueMicrotask(() => {
    syncingFromModel.value = false
  })
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

watch(defaultValue, (value) => {
  if (syncingFromModel.value) {
    return
  }
  handleSimpleChange(value)
})

watch(tableData, () => {
  if (syncingFromModel.value || props.mode !== 'table') {
    return
  }
  handleTableChange()
}, { deep: true })

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
  lastFocusedModalLang.value = ''
  showModal.value = false
}

/**
 * 弹窗取消
 */
const handleModalCancel = () => {
  // 恢复弹窗数据
  modalTableData.value = JSON.parse(JSON.stringify(tableData.value))
  lastFocusedModalLang.value = ''
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
  updateViewportHeight()
  if (typeof window !== 'undefined') {
    window.addEventListener('resize', updateViewportHeight)
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('resize', updateViewportHeight)
  }
})
</script>

<style scoped lang="less" src="@/styles/components/common/i18n-input.less"></style>

<style lang="less" src="@/styles/components/common/i18n-input-global.less"></style>

