<template>
  <div class="fx-dynamic-table">
<!-- 搜索区域卡片（保留少量内边距，看起来更舒适） -->
    <a-card
      v-if="resolvedShowQueryForm && config && config.queryFields?.length"
      :key="`query-${configVersion}`"
      :bordered="false"
      class="fx-card fx-query-card"
      :body-style="{ padding: '12px' }"
    >
      <a-form layout="inline" :model="queryModel" class="fx-query-form">
        <div class="fx-query-row">
          <!-- 搜索条件第一行（最多3个） -->
          <div class="fx-query-row-content">
            <template v-for="(q, index) in config.queryFields.slice(0, 3)" :key="q.field">
              <a-form-item :label="q.label">
                <a-input
                  v-if="q.queryType === 'input'"
                  v-model:value="(queryModel as any)[q.field]"
                  allow-clear
                  style="width: 200px"
                />
                <a-select
                  v-else-if="q.queryType === 'select' || q.queryType === 'multiSelect'"
                  v-model:value="(queryModel as any)[q.field]"
                  allow-clear
                  :mode="q.queryType === 'multiSelect' ? 'multiple' : undefined"
                  style="width: 200px"
                >
                  <a-select-option
                    v-for="opt in (dictOptions?.[q.dictCode || q.field] || [])"
                    :key="String(opt.value)"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </a-select-option>
                </a-select>
                <a-range-picker
                  v-else-if="q.queryType === 'dateRange'"
                  v-model:value="(queryModel as any)[q.field]"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 380px"
                />
                <a-range-picker
                  v-else-if="q.queryType === 'date' || q.queryType === 'datetime' || q.queryType === 'time'"
                  v-model:value="(queryModel as any)[q.field]"
                  show-time
                  format="YYYY-MM-DD HH:mm:ss"
                  style="width: 380px"
                />
                <a-input
                  v-else
                  v-model:value="(queryModel as any)[q.field]"
                  allow-clear
                  style="width: 200px"
                />
              </a-form-item>
            </template>
          </div>
          
          <!-- 搜索和重置按钮 -->
          <div class="fx-query-actions-row">
            <!-- 展开/收起按钮 -->
            <div v-if="config.queryFields.length > 3" class="fx-query-toggle">
              <a-button
                type="text"
                @click="isQueryExpanded = !isQueryExpanded"
                size="small"
              >
                {{ isQueryExpanded ? t('common.collapse') : t('common.expand') }}
                <DownOutlined :rotate="isQueryExpanded ? 180 : 0" />
              </a-button>
            </div>
            
            <!-- 搜索和重置按钮 -->
            <a-space>
              <a-button type="primary" @click="handleQuery">{{ t('common.search') }}</a-button>
              <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
            </a-space>
          </div>
        </div>
        
        <!-- 额外搜索条件（展开时显示） -->
        <div v-if="isQueryExpanded" class="fx-query-row fx-query-row-extra">
          <template v-for="(q, index) in config.queryFields.slice(3)" :key="q.field">
            <a-form-item :label="q.label">
              <a-input
                v-if="q.queryType === 'input'"
                v-model:value="(queryModel as any)[q.field]"
                allow-clear
                style="width: 200px"
              />
              <a-select
                v-else-if="q.queryType === 'select' || q.queryType === 'multiSelect'"
                v-model:value="(queryModel as any)[q.field]"
                allow-clear
                :mode="q.queryType === 'multiSelect' ? 'multiple' : undefined"
                style="width: 200px"
              >
                <a-select-option
                  v-for="opt in (dictOptions?.[q.dictCode || q.field] || [])"
                  :key="String(opt.value)"
                  :value="opt.value"
                >
                  {{ opt.label }}
                </a-select-option>
              </a-select>
              <a-range-picker
                v-else-if="q.queryType === 'dateRange'"
                v-model:value="(queryModel as any)[q.field]"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 380px"
              />
              <a-range-picker
                v-else-if="q.queryType === 'date' || q.queryType === 'datetime' || q.queryType === 'time'"
                v-model:value="(queryModel as any)[q.field]"
                show-time
                format="YYYY-MM-DD HH:mm:ss"
                style="width: 380px"
              />
              <a-input
                v-else
                v-model:value="(queryModel as any)[q.field]"
                allow-clear
                style="width: 200px"
              />
            </a-form-item>
          </template>
        </div>
      </a-form>
    </a-card>
    
<!-- 表格区域卡片（关键：去除 card body 的默认 24px padding，让表格区域最大化） -->
    <a-card 
      :bordered="false" 
      class="fx-card fx-table-card"
      :body-style="{ padding: '0' }"
    >
      <!-- 工具栏与列设置同一行：左操作按钮、右列设置，与下方表格保留间距 -->
      <div
        v-if="hasToolbarSlot || showColumnSettingBar"
        class="fx-table-toolbar-row"
      >
        <div v-if="hasToolbarSlot" class="fx-table-toolbar-left">
          <slot name="toolbar" />
        </div>
        <div
          v-if="showColumnSettingBar"
          class="fx-table-toolbar-right"
        >
          <ColumnSettingButton
            :table-code="tableCode"
            :columns="columnSettingColumns"
            @change="handleColumnChange"
          />
        </div>
      </div>
      
      <!-- 数据表格 -->
      <div class="fx-table-content">
        <div ref="tableWrapRef" class="fx-dynamic-table-wrap" :style="tableWrapStyle">
          <a-table
            :key="`table-${configVersion}`"
            :columns="tableColumns"
            :data-source="tableData"
            :loading="resolvedLoading"
            :row-selection="rowSelection"
            :pagination="false"
            :default-expand-all-rows="defaultExpandAllRows"
            :expandable="expandable"
            :row-key="resolvedRowKey"
            @change="handleTableChange"
            :scroll="resolvedScroll"
          >
          <!-- 自定义单元格内容插槽 -->
          <template #bodyCell="scope">
            <slot v-if="$slots[scope.column?.key]" :name="scope.column.key" v-bind="scope" />
            <slot v-else name="bodyCell" v-bind="scope" />
          </template>
        </a-table>
        </div>

        <div v-if="resolvedPaginationConfig" class="fx-table-pagination" :style="paginationAreaStyle">
          <a-pagination
            v-bind="resolvedPaginationConfig"
            @change="handlePaginationChange"
            @showSizeChange="handlePaginationShowSizeChange"
          />
        </div>
      </div>
    </a-card>
  </div>
</template>

<script setup lang="ts">
/**
 * 动态表格组件
 * 基于配置自动生成查询表单和表格，支持字典选项和自定义列
 * @author Forgex Team
 * @version 1.0.0
 */
import { computed, getCurrentInstance, onBeforeUnmount, onMounted, nextTick, reactive, ref, watch, h, resolveComponent, withDefaults, useSlots } from 'vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { DownOutlined } from '@ant-design/icons-vue'
import { getTableConfig, type FxTableConfig, type FxTableColumn } from '@/api/system/tableConfig'
import { useI18n } from 'vue-i18n'
import ColumnSettingButton from './ColumnSettingButton.vue'

/**
 * 字典选项类型
 */
type DictOption = { 
  label: string; // 显示文本
  value: any;   // 实际值
}

/**
 * 组件属性
 * <p>列设置默认开启：{@code showColumnSetting} 未传时必须为 true，避免表达式 {@code length && undefined} 恒为假。</p>
 */
const props = withDefaults(
  defineProps<{
  /**
   * 表格编码，用于获取表格配置
   */
  tableCode: string
  
  /**
   * 行主键字段名或函数
   */
  rowKey?: string | ((record: any) => string)
  
  /**
   * 字典选项配置，用于下拉选择框
   */
  dictOptions?: Record<string, DictOption[]>
  
  /**
   * 降级配置，当获取表格配置失败时使用
   */
  fallbackConfig?: Partial<FxTableConfig>
  
  /**
   * 数据请求函数
   * @param payload 请求参数，包含分页、查询条件和排序信息
   * @returns Promise<{ records: any[]; total: number }> 包含记录列表和总条数的Promise
   */
  request: (payload: {
    page: { current: number; pageSize: number }
    query: Record<string, any>
    sorter?: { field?: string; order?: string }
  }) => Promise<{ records: any[]; total: number }>

  loading?: boolean
  pagination?: TableProps['pagination'] | false
  rowSelection?: TableProps['rowSelection']
  scroll?: TableProps['scroll']
  defaultExpandAllRows?: boolean
  expandable?: TableProps['expandable']
  showQueryForm?: boolean
  
  /**
   * 是否显示列设置按钮
   * <p>默认为 true，显示列设置按钮。</p>
   */
  showColumnSetting?: boolean

  // 已不再使用（旧版比例分配），保留仅为兼容性
  tableHeightRatio?: number
  }>(),
  {
    /** 列设置属于表格公共能力，默认显示，不依赖菜单按钮权限 */
    showColumnSetting: true,
  }
)

const { t, locale } = useI18n()

const slots = useSlots()

/**
 * 是否传入工具栏插槽（与列设置并排展示）
 */
const hasToolbarSlot = computed(() => !!slots.toolbar)

/**
 * 表格配置
 */
const config = ref<FxTableConfig>()

/**
 * 列设置面板使用的列数据源。
 * <p>
 * 必须使用 computed 固定引用：模板里若写 {@code config?.columns ?? []}，在 {@code columns} 缺失时每次渲染都会生成新的空数组，
 * 会导致 {@link ColumnSettingButton} 的 props 引用变化、下拉层被反复卸载或无法保持展开。
 * </p>
 */
const columnSettingColumns = computed(() => config.value?.columns ?? [])

/**
 * 是否展示列设置区域（按钮条右侧）
 */
const showColumnSettingBar = computed(
  () => !!config.value?.columns?.length && props.showColumnSetting !== false
)

/**
 * 配置版本号，用于强制刷新 computed
 */
const configVersion = ref(0)

const ATag = resolveComponent('a-tag') as any

/**
 * 尝试将字典翻译的 JSON 文本渲染为 Tag。
 */
function tryRenderTagFromDictJson(dictText: any) {
  if (dictText === undefined || dictText === null || dictText === '') {
    return undefined
  }

  let parsed: any = undefined
  if (typeof dictText === 'string') {
    const trimmed = dictText.trim()
    if (!(trimmed.startsWith('{') && trimmed.endsWith('}'))) {
      return undefined
    }
    try {
      parsed = JSON.parse(trimmed)
    } catch (e) {
      return undefined
    }
  } else if (typeof dictText === 'object') {
    parsed = dictText
  } else {
    return undefined
  }

  if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
    return undefined
  }

  const label = parsed?.label ?? parsed?.name
  if (label === undefined || label === null || label === '') {
    return undefined
  }

  const style =
    parsed?.borderColor || parsed?.backgroundColor
      ? {
          borderColor: parsed?.borderColor,
          backgroundColor: parsed?.backgroundColor,
        }
      : undefined
  return h(ATag, { color: parsed?.color || 'blue', style }, label)
}

/**
 * 将字典翻译字段渲染为 Tag。
 */
function renderTagByDictText(dictText: any, fallbackText: any) {
  if (dictText === undefined || dictText === null || dictText === '') {
    const fromFallback = tryRenderTagFromDictJson(fallbackText)
    if (fromFallback !== undefined) {
      return fromFallback
    }
    return fallbackText
  }

  if (typeof dictText === 'string') {
    const trimmed = dictText.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
          const label = parsed?.label ?? parsed?.name ?? fallbackText ?? dictText
          const style =
            parsed?.borderColor || parsed?.backgroundColor
              ? {
                  borderColor: parsed?.borderColor,
                  backgroundColor: parsed?.backgroundColor,
                }
              : undefined
          return h(ATag, { color: parsed?.color || 'blue', style }, label)
        }
      } catch (e) {
        return h(ATag, { color: 'blue' }, dictText)
      }
    }

    return h(ATag, { color: 'blue' }, dictText)
  }

  if (typeof dictText === 'object') {
    const parsed = dictText as any
    const label = parsed?.label ?? parsed?.name ?? fallbackText
    const style =
      parsed?.borderColor || parsed?.backgroundColor
        ? {
            borderColor: parsed?.borderColor,
            backgroundColor: parsed?.backgroundColor,
          }
        : undefined
    return h(ATag, { color: parsed?.color || 'blue', style }, label)
  }

  return fallbackText
}

/**
 * 加载状态
 */
const loading = ref(false)

const resolvedLoading = computed(() => (props.loading === undefined ? loading.value : props.loading))
const resolvedShowQueryForm = computed(() => props.showQueryForm !== false)

/**
 * 表格数据
 */
const tableData = ref<any[]>([])

/**
 * 分页信息
 */
const pagination = reactive({
  current: 1,     // 当前页码
  pageSize: 20,   // 每页条数
  total: 0,       // 总条数
})

/**
 * 当前组件是否显式传入了 pagination 属性。
 * <p>
 * 由于 pagination 的 TS 类型包含字面量 false，Vue 可能会将其推导为 Boolean prop，
 * 从而在未传入时 props.pagination 也会变成 false，导致默认分页被错误关闭。
 * </p>
 *
 * @return 是否显式传入 pagination
 */
const isPaginationPropPassed = computed(() => {
  const instance = getCurrentInstance()
  const vnodeProps: any = instance?.vnode?.props
  if (!vnodeProps) return false
  return Object.prototype.hasOwnProperty.call(vnodeProps, 'pagination')
})

const resolvedPaginationConfig = computed(() => {
  if (isPaginationPropPassed.value && props.pagination === false) return undefined

  const base = {
    current: pagination.current,
    pageSize: pagination.pageSize,
    total: pagination.total,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => String(t('common.total', { total })),
    hideOnSinglePage: false,
  } as const

  if (!isPaginationPropPassed.value || props.pagination === undefined) return base as any
  if (typeof props.pagination === 'object') {
    return { ...base, ...(props.pagination as any), hideOnSinglePage: false } as any
  }
  return base as any
})

const rowSelection = computed(() => props.rowSelection)
const defaultExpandAllRows = computed(() => props.defaultExpandAllRows)
const expandable = computed(() => props.expandable)

const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
const lastSorter = ref<any>(undefined)

let computeScrollYRafPending = false

const scheduleComputeAutoScrollY = () => {
  if (computeScrollYRafPending) return
  computeScrollYRafPending = true
  requestAnimationFrame(() => {
    computeScrollYRafPending = false
    computeAutoScrollY()
  })
}

const onResizeOrScroll = () => {
  void nextTick().then(() => scheduleComputeAutoScrollY())
}

/**
 * 查询模型
 */
const queryModel = reactive<Record<string, any>>({})

/**
 * 搜索区域展开状态
 */
const isQueryExpanded = ref(false)

/**
 * 解析后的行主键
 */
const resolvedRowKey = computed(() => props.rowKey || config.value?.rowKey || 'id')

/**
 * 表格列配置
 */
const tableColumns = computed(() => {
  // 依赖 configVersion 强制刷新
  const _ = configVersion.value
  const cols: FxTableColumn[] = config.value?.columns || []
  
  console.log('[FxDynamicTable] tableColumns computed 被调用，version:', configVersion.value, 'columns:', cols.length, '第一列:', cols[0]?.title)
  
  // 将操作列移到最后
  const actionCol = cols.find(c => c.field === 'action')
  const otherCols = cols.filter(c => c.field !== 'action')
  
  // 重新组合列，将操作列放在最后
  const finalCols = actionCol ? [...otherCols, actionCol] : otherCols
  
  return finalCols.map(c => {
    const column: any = {
      title: c.title,
      dataIndex: c.field,
      key: c.field,
      align: c.align,
      width: c.width,
      fixed: c.field === 'action' ? (c.fixed || 'right') : c.fixed,
      ellipsis: c.ellipsis,
      sorter: !!c.sortable,
    }
    
    // 如果列有字典字段配置，添加自定义渲染（优先级更高）
    if (c.dictField) {
      column.customRender = ({ record }: any) => {
        const dictText = record?.[c.dictField]
        return renderTagByDictText(dictText, record?.[c.field])
      }
    }
    // 如果列有字典配置，添加自定义渲染
    else if (c.dictCode) {
      column.customRender = ({ record }: any) => {
        const autoDictField = `${c.field}Text`
        // 优先使用后端自动翻译生成的 xxxText 字段（可能为 JSON 字符串或对象）
        const fromTextField = renderTagByDictText(record?.[autoDictField], undefined)
        if (fromTextField !== undefined) {
          return fromTextField
        }

        const value = record?.[c.field]
        // 兼容后端直接把字典 JSON 放在原字段上的情况（例如 gender/status 直接返回 JSON 字符串）
        const fromValueJson = tryRenderTagFromDictJson(value)
        if (fromValueJson !== undefined) {
          return fromValueJson
        }

        const dictItems = props.dictOptions?.[c.dictCode] || []
        const dictItem = dictItems.find((item: any) => String(item.value) === String(value))

        if (dictItem) {
          const style =
            dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
              ? {
                  borderColor: dictItem.tagStyle?.borderColor,
                  backgroundColor: dictItem.tagStyle?.backgroundColor,
                }
              : undefined
          return h(
            ATag,
            { color: dictItem.tagStyle?.color || dictItem.color || 'blue', style },
            dictItem.label,
          )
        }
        return value
      }
    }
    
    return column
  })
})

/**
 * 关键修改：表格区域始终占满剩余空间（无论是否有分页）
 */
const tableWrapStyle = computed(() => {
  return { flex: '1 1 auto', minHeight: 0 } as any
})

/**
 * 分页区域布局样式（保持不变）
 */
const paginationAreaStyle = computed(() => {
  if (!resolvedPaginationConfig.value) return undefined
  return { flex: '0 0 auto', marginTop: 'auto' } as any
})

const resolvedScroll = computed(() => {
  const baseScroll = props.scroll || {}
  const columns = tableColumns.value || []

  let totalWidth = 0
  for (const col of columns as any[]) {
    const w = col?.width
    if (typeof w === 'number' && Number.isFinite(w)) {
      totalWidth += w
      continue
    }
    if (typeof w === 'string') {
      const parsed = Number.parseFloat(w)
      if (Number.isFinite(parsed)) {
        totalWidth += parsed
        continue
      }
    }
    totalWidth += 160
  }

  const x = (baseScroll as any)?.x ?? totalWidth
  const baseY = (baseScroll as any)?.y
  const y = baseY === undefined ? autoScrollY.value : baseY

  const out: any = { ...(baseScroll as any), x }
  if (y !== undefined && y !== null && y !== '') {
    out.y = y
  }
  return out as any
})

function computeAutoScrollY() {
  const baseScroll = props.scroll as any
  if (baseScroll && baseScroll.y !== undefined) {
    if (autoScrollY.value !== undefined) {
      autoScrollY.value = undefined
    }
    return
  }

  const wrapEl = tableWrapRef.value
  if (!wrapEl) {
    autoScrollY.value = undefined
    return
  }

  const rect = wrapEl.getBoundingClientRect()
  if (!Number.isFinite(rect.height)) {
    autoScrollY.value = undefined
    return
  }

  const available = rect.height
  if (!Number.isFinite(available) || available <= 0) {
    autoScrollY.value = undefined
    return
  }

  // 获取表格头部高度
  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0

  // tableWrapRef 仅包含表格区域（不包含 toolbar / pagination），因此这里只需扣除表头与少量缓冲
  const buffer = 2
  const y = Math.floor(available - headerHeight - buffer)
  
  const nextY = y > 100 ? y : undefined  // 确保最小高度为100px，避免表格过小
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

/**
 * 格式化排序信息
 */
function normalizeSorter(sorter: any) {
  if (!sorter) return undefined
  if (Array.isArray(sorter)) sorter = sorter[0]
  const field = sorter?.field || sorter?.columnKey
  const order = sorter?.order
  if (!field && !order) return undefined
  return { field, order }
}

/**
 * 加载表格配置
 */
async function loadConfig() {
  console.log('[FxDynamicTable] loadConfig 开始，tableCode:', props.tableCode, 'locale:', locale.value)
  try {
    const backendConfig = await getTableConfig({ tableCode: props.tableCode })
    console.log('[FxDynamicTable] 后端返回配置:', backendConfig)
    const mergedConfig = mergeConfigs(backendConfig, props.fallbackConfig)
    
    // 强制创建新对象，确保 Vue 响应式系统能检测到变化
    config.value = {
      ...mergedConfig,
      columns: mergedConfig.columns ? [...mergedConfig.columns] : [],
      queryFields: mergedConfig.queryFields ? [...mergedConfig.queryFields] : []
    }
    
    // 增加版本号，强制刷新 computed
    configVersion.value++
    
    console.log('[FxDynamicTable] 配置更新完成，columns:', config.value.columns?.length, 'queryFields:', config.value.queryFields?.length, 'version:', configVersion.value)
  } catch (e) {
    console.error('[FxDynamicTable] 获取表格配置失败:', e)
    if (props.fallbackConfig) {
      config.value = { ...props.fallbackConfig } as any
    } else {
      config.value = {
        tableCode: props.tableCode,
        tableName: '默认表格',
        tableType: 'NORMAL',
        rowKey: 'id',
        defaultPageSize: 20,
        columns: [],
        queryFields: [],
        version: 1
      }
      console.warn('[FxDynamicTable] 使用默认空配置，表格可能无法正常显示')
    }
    configVersion.value++
  }

  if (config.value?.columns?.length) {
    config.value.columns = normalizeColumns(config.value.columns as any)
  }

  pagination.pageSize = config.value?.defaultPageSize || pagination.pageSize
  for (const q of config.value?.queryFields || []) {
    if (!(q.field in queryModel)) {
      queryModel[q.field] = undefined
    }
  }
}

/**
 * 合并表格配置
 */
function mergeConfigs(backendConfig: FxTableConfig | undefined, fallbackConfig: Partial<FxTableConfig> | undefined): FxTableConfig {
  if (!backendConfig) {
    return fallbackConfig as any
  }
  
  if (!fallbackConfig) {
    return backendConfig
  }
  
  const merged: FxTableConfig = { ...(fallbackConfig as any), ...(backendConfig as any) }

  const backendColumns = normalizeColumns(backendConfig.columns || [])
  const fallbackColumns = normalizeColumns((fallbackConfig.columns || []) as any[])

  if (fallbackColumns.length) {
    const fallbackMap = new Map<string, FxTableColumn>()
    for (const fc of fallbackColumns) {
      fallbackMap.set(fc.field, fc)
    }

    const mergedColumns: FxTableColumn[] = backendColumns.map(bc => {
      const fc = fallbackMap.get(bc.field)
      const mergedCol: any = { ...(fc as any), ...(bc as any) }

      if ('fixed' in (bc as any)) {
        mergedCol.fixed = (bc as any).fixed
      } else if ('fixed' in (fc as any)) {
        mergedCol.fixed = (fc as any).fixed
      } else {
        mergedCol.fixed = undefined
      }

      return mergedCol
    })

    const backendFieldSet = new Set(backendColumns.map(c => c.field))
    for (const fc of fallbackColumns) {
      if (!backendFieldSet.has(fc.field)) {
        mergedColumns.push(fc)
      }
    }

    merged.columns = mergedColumns
  } else {
    merged.columns = backendColumns
  }

  const backendQueryFields = backendConfig.queryFields || []
  const fallbackQueryFields = (fallbackConfig.queryFields || []) as any[]

  if (fallbackQueryFields.length) {
    const fallbackMap = new Map<string, any>()
    for (const fq of fallbackQueryFields) {
      fallbackMap.set(fq.field, fq)
    }
    const mergedQueryFields = backendQueryFields.map((bq: any) => {
      const fq = fallbackMap.get(bq.field)
      return { ...(fq as any), ...(bq as any) }
    })
    const backendFieldSet = new Set(backendQueryFields.map((q: any) => q.field))
    for (const fq of fallbackQueryFields) {
      if (!backendFieldSet.has(fq.field)) {
        mergedQueryFields.push(fq)
      }
    }
    merged.queryFields = mergedQueryFields as any
  } else {
    merged.queryFields = backendQueryFields
  }

  return merged
}

function normalizeColumns(cols: any[]) {
  return (Array.isArray(cols) ? cols : []).map((c: any) => {
    const field = c?.field ?? c?.dataIndex ?? c?.key
    return { ...(c as any), field } as FxTableColumn
  }).filter((c: any) => !!c?.field)
}

/**
 * 格式化查询条件
 * 
 * 执行步骤：
 * 1. 遍历配置中的所有查询字段
 * 2. 获取每个字段的值
 * 3. 跳过空值（undefined、null、空字符串）
 * 4. 处理日期范围字段，转换为标准格式
 * 5. 返回格式化后的查询对象
 * 
 * @returns 格式化后的查询条件对象
 */
function normalizeQuery() {
  const out: Record<string, any> = {}
  if (!config.value) return out
  for (const q of config.value.queryFields || []) {
    const v = queryModel[q.field]
    if (v === undefined || v === null || v === '') continue
    if (Array.isArray(v) && v.length === 0) continue
    if ((q.queryType === 'dateRange' || q.queryType === 'date' || q.queryType === 'datetime' || q.queryType === 'time') && Array.isArray(v) && v.length === 2) {
      out[q.field] = [dayjs(v[0]).format('YYYY-MM-DD HH:mm:ss'), dayjs(v[1]).format('YYYY-MM-DD HH:mm:ss')]
      continue
    }
    out[q.field] = v
  }
  return out
}

/**
 * 处理查询
 * 
 * 执行步骤：
 * 1. 设置加载状态为 true
 * 2. 调用 props.request 方法，传入分页、查询条件、排序信息
 * 3. 从响应中提取 records 和 total
 * 4. 更新表格数据和分页总数
 * 5. 重置加载状态
 * 6. 重新计算表格滚动高度
 * 
 * @param sorter 排序信息（可选）
 * @throws 请求失败时抛出异常
 */
async function handleQuery(sorter?: any) {
  loading.value = true
  try {
    const res = await props.request({
      page: { current: pagination.current, pageSize: pagination.pageSize },
      query: normalizeQuery(),
      sorter: normalizeSorter(sorter),
    })
    const anyRes: any = res as any
    const records = anyRes?.records ?? anyRes?.data ?? []
    const total = anyRes?.total ?? 0
    tableData.value = Array.isArray(records) ? records : []
    pagination.total = typeof total === 'number' ? total : parseInt(String(total) || '0', 10)
  } finally {
    loading.value = false
    await nextTick()
    scheduleComputeAutoScrollY()
  }
}

/**
 * 处理重置
 * 
 * 执行步骤：
 * 1. 清空所有查询条件
 * 2. 重置分页到第一页
 * 3. 使用上次的排序信息重新查询
 */
function handleReset() {
  for (const k of Object.keys(queryModel)) {
    queryModel[k] = undefined
  }
  pagination.current = 1
  handleQuery(lastSorter.value)
}

/**
 * 获取当前查询条件
 * 
 * @returns 当前查询条件对象
 */
function getQuery() {
  return normalizeQuery()
}

/**
 * 获取当前分页信息
 * 
 * @returns 分页信息对象，包含 current（当前页）、pageSize（每页条数）、total（总数）
 */
function getPage() {
  return { 
    current: pagination.current, 
    pageSize: pagination.pageSize, 
    total: pagination.total 
  }
}

/**
 * 重新加载数据
 * 
 * 执行步骤：
 * 1. 调用 handleQuery 方法，使用当前分页和查询条件
 * 
 * @returns Promise
 */
function reload() {
  return handleQuery()
}

/**
 * 刷新数据
 * 
 * 执行步骤：
 * 1. 调用 handleQuery 方法，使用当前分页和查询条件
 * 
 * @returns Promise
 */
function refresh() {
  return handleQuery()
}

// 暴露给父组件使用的方法
defineExpose({ getQuery, getPage, reload, refresh })

/**
 * 处理列配置变更
 * <p>
 * 当用户通过列设置按钮修改列配置后，更新表格列显示。
 * </p>
 *
 * @param columns 更新后的列配置
 */
function handleColumnChange(columns: FxTableColumn[]) {
  if (config.value) {
    // 更新配置中的列
    config.value = {
      ...config.value,
      columns: [...columns]
    }
    // 强制刷新 computed
    configVersion.value++
  }
}

const handleTableChange: TableProps['onChange'] = (pag, _filters, sorter) => {
  lastSorter.value = sorter
  handleQuery(sorter)
}

function handlePaginationChange(page: number, pageSize: number) {
  pagination.current = page
  if (pageSize && pageSize !== pagination.pageSize) {
    pagination.pageSize = pageSize
  }
  handleQuery(lastSorter.value)
}

function handlePaginationShowSizeChange(current: number, size: number) {
  pagination.current = 1
  pagination.pageSize = size
  handleQuery(lastSorter.value)
}

watch(
  () => props.tableCode,
  async () => {
    pagination.current = 1
    await loadConfig()
    await handleQuery(lastSorter.value)
    // 配置加载后重新计算表格高度
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

// 监听语言变化 - 使用函数形式确保能正确监听
watch(
  () => locale.value,
  async (newLocale, oldLocale) => {
    console.log('[FxDynamicTable] 语言切换，重新加载配置:', newLocale, '(旧:', oldLocale, ')')
    // 重新加载配置
    await loadConfig()
    // 重新加载数据
    await handleQuery(lastSorter.value)
  },
)

watch(
  pagination,
  async () => {
    // 分页信息变化时重新计算表格高度，确保表格正确显示
    await nextTick()
    scheduleComputeAutoScrollY()
  },
  { deep: true }
)

watch(
  () => isQueryExpanded.value,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

watch(
  () => tableData.value.length,
  async () => {
    // 数据变化时重新计算表格高度
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

onMounted(async () => {
  window.addEventListener('resize', onResizeOrScroll, { passive: true })

  await loadConfig()
  await handleQuery()
  
  // 添加 MutationObserver 监听 DOM 变化，确保表格高度正确计算
  const wrapEl = tableWrapRef.value
  if (wrapEl) {
    const observer = new MutationObserver(() => {
      scheduleComputeAutoScrollY()
    })
    
    observer.observe(wrapEl, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
    
    // 在组件卸载时断开观察
    onBeforeUnmount(() => {
      observer.disconnect()
    })
  }
  
  // 延迟计算，确保 DOM 完全渲染
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 100)
  
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 300)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResizeOrScroll as any)
})
</script>

<style scoped>
.fx-dynamic-table {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.fx-card {
  background: var(--fx-bg-container, #ffffff);
  border-radius: var(--fx-radius-lg, 8px);
}

.fx-query-form {
  display: flex;
  flex-direction: column;
  width: 100%;
}

.fx-query-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  flex-wrap: nowrap;
  margin-bottom: 0;
}

.fx-query-row-content {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  flex: 1;
  max-width: calc(100% - 200px);
}

.fx-query-row-content :deep(.ant-form-item) {
  margin: 0;
  margin-bottom: 0;
}

.fx-query-actions-row {
  display: flex;
  align-items: center;
  gap: 16px;
  white-space: nowrap;
}

.fx-query-toggle {
  display: flex;
  align-items: center;
}

.fx-query-row-extra {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--fx-border-color, #e8e8e8);
  width: 100%;
  justify-content: flex-start;
}

.fx-query-row-extra :deep(.ant-form-item) {
  margin: 0;
  margin-bottom: 16px;
}

.fx-table-card {
  flex: 1 1 auto;
  min-height: 0;
}

.fx-table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1 1 auto;
  height: 100%;
}

/* 工具栏 + 列设置：同一行，左对齐操作 / 右对齐列设置 */
.fx-table-toolbar-row {
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 12px;
  padding: 12px 16px 0 16px;
  margin-bottom: 16px;
  flex-shrink: 0;
  overflow-x: auto;
}

.fx-table-toolbar-left {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  flex: 1;
  min-width: 0;
  gap: 8px;
}

.fx-table-toolbar-right {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  margin-left: auto;
}

/* 表格内容区：与上方按钮行留出间距（margin-bottom 已在 toolbar-row） */
.fx-table-content {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  height: 100%;
  padding: 0 16px 16px 16px;
  box-sizing: border-box;
}

.fx-dynamic-table-wrap {
  display: flex;
  flex-direction: column;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  min-width: 0;
  min-height: 0;
}

.fx-dynamic-table-wrap :deep(.ant-spin-nested-loading),
.fx-dynamic-table-wrap :deep(.ant-spin-container),
.fx-dynamic-table-wrap :deep(.ant-table),
.fx-dynamic-table-wrap :deep(.ant-table-container) {
  flex: 1 1 auto;
  min-height: 0;
}

.fx-dynamic-table-wrap :deep(.ant-table) {
  max-width: 100%;
}

/* 分页器间距调整 */
.fx-table-pagination {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-end;
  padding: 12px 0 0 0;  /* 顶部12px间距，其他方向0（外层已有padding） */
}

.fx-table-pagination :deep(.ant-pagination) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;  /* 整体右对齐 */
  gap: 16px;  /* 元素之间的间距 */
}

.fx-table-pagination :deep(.ant-pagination-total-text) {
  /* 不设置特殊样式，保持默认顺序，显示在页码左边 */
}
</style>
