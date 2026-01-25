<template>
  <div class="fx-dynamic-table">
    <!-- 搜索区域卡片 -->
    <a-card
      v-if="resolvedShowQueryForm && config && config.queryFields?.length"
      :bordered="false"
      class="fx-card fx-query-card"
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
                  v-else-if="q.queryType === 'select'"
                  v-model:value="(queryModel as any)[q.field]"
                  allow-clear
                  style="width: 200px"
                >
                  <a-select-option
                    v-for="opt in (dictOptions?.[q.field] || [])"
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
                {{ isQueryExpanded ? '收起' : '展开' }}
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
                v-else-if="q.queryType === 'select'"
                v-model:value="(queryModel as any)[q.field]"
                allow-clear
                style="width: 200px"
              >
                <a-select-option
                  v-for="opt in (dictOptions?.[q.field] || [])"
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
    
    <!-- 表格区域卡片 -->
    <a-card :bordered="false" class="fx-card fx-table-card">
      <div v-if="$slots.toolbar" class="fx-table-toolbar">
        <slot name="toolbar" />
      </div>
      
      <!-- 数据表格 -->
      <div ref="tableWrapRef" class="fx-dynamic-table-wrap">
        <a-table
          :columns="tableColumns"
          :data-source="tableData"
          :loading="resolvedLoading"
          :row-selection="rowSelection"
          :pagination="resolvedPagination"
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
import { computed, onBeforeUnmount, onMounted, nextTick, reactive, ref, watch, h, resolveComponent } from 'vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { DownOutlined } from '@ant-design/icons-vue'
import { getTableConfig, type FxTableConfig, type FxTableColumn } from '@/api/system/tableConfig'
import { useI18n } from 'vue-i18n'

/**
 * 字典选项类型
 */
type DictOption = { 
  label: string; // 显示文本
  value: any;   // 实际值
}

/**
 * 组件属性
 */
const props = defineProps<{
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
}>()

const { t, locale } = useI18n()

/**
 * 表格配置
 */
const config = ref<FxTableConfig>()

const ATag = resolveComponent('a-tag') as any

/**
 * 尝试将字典翻译的 JSON 文本渲染为 Tag。
 * <p>
 * 该方法只在能够明确识别为“字典翻译 JSON”时才返回 Tag 节点；
 * 否则返回 undefined，交由上层逻辑按普通文本处理。
 * </p>
 *
 * @param dictText 字典翻译文本，可能为 JSON 字符串或对象
 * @return 能渲染为 Tag 时返回 VNode，否则返回 undefined
 * @throws 无显式抛出异常，解析失败时返回 undefined
 * @see renderTagByDictText
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
 * <p>
 * 支持以下输入：
 * <ul>
 *   <li>JSON字符串：包含 label/color 等信息</li>
 *   <li>对象：包含 label/color 等信息</li>
 *   <li>普通字符串：按默认蓝色 Tag 渲染</li>
 * </ul>
 * 当 dictText 为空时，会尝试从 fallbackText 中识别并渲染字典 JSON。
 * </p>
 *
 * @param dictText 字典翻译字段值
 * @param fallbackText 兜底显示内容（通常为原始字段值）
 * @return 渲染后的 VNode 或字符串
 * @throws 无显式抛出异常，解析失败时按文本回退
 * @see tryRenderTagFromDictJson
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

const resolvedPagination = computed(() => {
  if (props.pagination === false) return false
  const base = {
    current: pagination.current,
    pageSize: pagination.pageSize,
    total: pagination.total,
    showSizeChanger: true,
    showQuickJumper: true,
    showTotal: (total: number) => String(t('common.total', { total })),
    hideOnSinglePage: false,
  } as const
  if (!props.pagination) return base
  return { ...base, ...(props.pagination as any), hideOnSinglePage: false }
})

const rowSelection = computed(() => props.rowSelection)
const defaultExpandAllRows = computed(() => props.defaultExpandAllRows)
const expandable = computed(() => props.expandable)

const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)

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
  const cols: FxTableColumn[] = config.value?.columns || []
  
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

  const paginationEl = wrapEl.querySelector('.ant-pagination') as HTMLElement | null
  const paginationHeight = paginationEl ? paginationEl.getBoundingClientRect().height : 0

  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0

  const buffer = 16
  const y = Math.floor(available - paginationHeight - headerHeight - buffer)
  const nextY = y > 0 ? y : undefined
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

/**
 * 格式化排序信息
 * @param sorter 排序信息
 * @returns 格式化后的排序信息
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
  try {
    // 从后端获取表格配置
    const backendConfig = await getTableConfig({ tableCode: props.tableCode })
    
    // 合并后端配置和fallback配置，fallback配置优先
    config.value = mergeConfigs(backendConfig, props.fallbackConfig)
  } catch (e) {
    console.error('获取表格配置失败:', e)
    // 如果获取失败，使用降级配置
    if (props.fallbackConfig) {
      config.value = props.fallbackConfig as any
    } else {
      // 如果没有降级配置，使用默认空配置，避免组件崩溃
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
      console.warn('使用默认空配置，表格可能无法正常显示')
    }
  }

  if (config.value?.columns?.length) {
    config.value.columns = normalizeColumns(config.value.columns as any)
  }

  // 设置默认页码大小
  pagination.pageSize = config.value?.defaultPageSize || pagination.pageSize
  // 初始化查询模型
  for (const q of config.value?.queryFields || []) {
    if (!(q.field in queryModel)) {
      queryModel[q.field] = undefined
    }
  }
}

/**
 * 合并表格配置，fallback配置优先
 * @param backendConfig 后端配置
 * @param fallbackConfig 降级配置
 * @returns 合并后的配置
 */
function mergeConfigs(backendConfig: FxTableConfig | undefined, fallbackConfig: Partial<FxTableConfig> | undefined): FxTableConfig {
  // 如果没有后端配置，直接使用fallback配置
  if (!backendConfig) {
    return fallbackConfig as any
  }
  
  // 如果没有fallback配置，直接使用后端配置
  if (!fallbackConfig) {
    return backendConfig
  }
  
  const merged: FxTableConfig = { ...(backendConfig as any), ...(fallbackConfig as any) }

  const backendColumns = normalizeColumns(backendConfig.columns || [])
  const fallbackColumns = normalizeColumns((fallbackConfig.columns || []) as any[])

  if (fallbackColumns.length) {
    const backendMap = new Map<string, FxTableColumn>()
    for (const bc of backendColumns) {
      backendMap.set(bc.field, bc)
    }

    // 以 fallbackColumns 为主顺序；同 field 的列按 “后端 + fallback 覆盖” 合并
    const mergedColumns: FxTableColumn[] = fallbackColumns.map(fc => {
      const bc = backendMap.get(fc.field)
      const mergedCol: any = { ...(bc as any), ...(fc as any) }

      if ('fixed' in (fc as any)) {
        mergedCol.fixed = (fc as any).fixed
      } else {
        mergedCol.fixed = undefined
      }

      return mergedCol
    })

    // 将后端中 fallback 未声明的列补齐（避免丢列）
    const fallbackFieldSet = new Set(fallbackColumns.map(c => c.field))
    for (const bc of backendColumns) {
      if (!fallbackFieldSet.has(bc.field)) {
        mergedColumns.push(bc)
      }
    }

    merged.columns = mergedColumns
  } else {
    merged.columns = backendColumns
  }

  const backendQueryFields = backendConfig.queryFields || []
  const fallbackQueryFields = (fallbackConfig.queryFields || []) as any[]

  if (fallbackQueryFields.length) {
    const backendMap = new Map<string, any>()
    for (const bq of backendQueryFields) {
      backendMap.set(bq.field, bq)
    }
    // 以 fallbackQueryFields 为主顺序；同 field 的查询项按 “后端 + fallback 覆盖” 合并
    const mergedQueryFields = fallbackQueryFields.map(fq => {
      const bq = backendMap.get(fq.field)
      return { ...(bq as any), ...(fq as any) }
    })
    // 将后端中 fallback 未声明的查询项补齐
    const fallbackFieldSet = new Set(fallbackQueryFields.map(q => q.field))
    for (const bq of backendQueryFields) {
      if (!fallbackFieldSet.has(bq.field)) {
        mergedQueryFields.push(bq)
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
 * @returns 格式化后的查询条件
 */
function normalizeQuery() {
  const out: Record<string, any> = {}
  if (!config.value) return out
  for (const q of config.value.queryFields || []) {
    const v = queryModel[q.field]
    // 跳过空值
    if (v === undefined || v === null || v === '') continue
    // 格式化日期范围
    if (q.queryType === 'dateRange' && Array.isArray(v) && v.length === 2) {
      out[q.field] = [dayjs(v[0]).format('YYYY-MM-DD HH:mm:ss'), dayjs(v[1]).format('YYYY-MM-DD HH:mm:ss')]
      continue
    }
    out[q.field] = v
  }
  return out
}

/**
 * 处理查询
 * @param sorter 排序信息
 */
async function handleQuery(sorter?: any) {
  loading.value = true
  try {
    // 发送请求获取数据
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
 */
function handleReset() {
  // 清空查询条件
  for (const k of Object.keys(queryModel)) {
    queryModel[k] = undefined
  }
  // 重置页码
  pagination.current = 1
  // 重新查询
  handleQuery()
}

/**
 * 获取当前查询条件
 * @returns 当前查询条件
 */
function getQuery() {
  return normalizeQuery()
}

/**
 * 获取当前分页信息
 * @returns 当前分页信息
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
 * @returns Promise<void>
 */
function reload() {
  return handleQuery()
}

// 暴露方法给父组件
function refresh() {
  return handleQuery()
}

defineExpose({ getQuery, getPage, reload, refresh })

/**
 * 处理表格变化
 * @param pag 分页信息
 * @param _filters 过滤条件
 * @param sorter 排序信息
 */
const handleTableChange: TableProps['onChange'] = (pag, _filters, sorter) => {
  pagination.current = pag?.current || 1
  pagination.pageSize = pag?.pageSize || pagination.pageSize
  handleQuery(sorter)
}

// 监听表格编码变化
watch(
  () => props.tableCode,
  async () => {
    pagination.current = 1
    await loadConfig()
    await handleQuery()
  },
)

watch(
  () => locale.value,
  async () => {
    await loadConfig()
  },
)

// 组件挂载时初始化
onMounted(async () => {
  window.addEventListener('resize', onResizeOrScroll, { passive: true })

  await loadConfig()
  await handleQuery()
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResizeOrScroll as any)
})

watch(
  () => isQueryExpanded.value,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)
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
  flex: 1;
  min-height: 0;
}

.fx-table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
}

.fx-table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.fx-dynamic-table-wrap {
  margin-top: 8px;
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
  min-width: 0;
  flex: 1;
  min-height: 0;
}

.fx-dynamic-table-wrap :deep(.ant-table) {
  max-width: 100%;
}
</style>
