<template>
  <div class="fx-dynamic-table">
    <!-- 查询区域卡片，保留较紧凑的内边距 -->
    <a-card
      v-if="resolvedShowQueryForm && config && config.queryFields?.length"
      :key="`query-${configVersion}`"
      :bordered="false"
      class="fx-card fx-query-card"
      data-guide-id="fx-table-query"
      :body-style="{ padding: '12px' }"
    >
      <a-form layout="inline" :model="queryModel" class="fx-query-form">
        <div class="fx-query-row">
          <!-- 第一行查询条件，最多展示 3 项 -->
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

          <!-- 查询操作区 -->
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

            <!-- 查询与重置按钮 -->
            <a-space>
              <a-button type="primary" @click="handleQuery">{{ t('common.search') }}</a-button>
              <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
            </a-space>
          </div>
        </div>

        <!-- 展开后显示的附加查询条件 -->
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

    <!-- 表格区域卡片，移除默认 body padding 以扩大可用空间 -->
    <a-card
      :bordered="false"
      class="fx-card fx-table-card"
      :body-style="{ padding: '0' }"
    >
      <!-- 工具栏与列设置同一行展示 -->
      <div
        v-if="hasToolbarSlot || showColumnSettingBar"
        class="fx-table-toolbar-row"
        data-guide-id="fx-table-toolbar"
      >
        <div v-if="hasToolbarSlot" class="fx-table-toolbar-left" data-guide-id="fx-table-toolbar-left">
          <slot name="toolbar" />
        </div>
        <div
          v-if="showColumnSettingBar"
          class="fx-table-toolbar-right"
          data-guide-id="fx-table-column-setting"
        >
          <ColumnSettingButton
            :table-code="tableCode"
            :columns="columnSettingColumns"
            @change="handleColumnChange"
          />
        </div>
      </div>

      <!-- 数据表格 -->
      <div ref="tableContentRef" class="fx-table-content" data-guide-id="fx-table-content">
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
            <!-- 自定义单元格插槽 -->
            <template #bodyCell="scope">
              <slot v-if="$slots[scope.column?.key]" :name="scope.column.key" v-bind="scope" />
              <slot v-else name="bodyCell" v-bind="scope" />
            </template>
          </a-table>
        </div>

        <div
          v-if="resolvedPaginationConfig"
          ref="paginationRef"
          class="fx-table-pagination"
          data-guide-id="fx-table-pagination"
          :style="paginationAreaStyle"
        >
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
 * 动态表格组件。
 * <p>
 * 基于后端表格配置自动生成查询表单与表格列，支持字典选项、列设置与自定义插槽。
 * 样式抽取至 {@code src/styles/fx-dynamic-table.less}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.1
 */
import { computed, getCurrentInstance, onBeforeUnmount, onMounted, nextTick, reactive, ref, watch, h, resolveComponent, withDefaults, useSlots } from 'vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { DownOutlined } from '@ant-design/icons-vue'
import { getTableConfig, type FxTableConfig, type FxTableColumn } from '@/api/system/tableConfig'
import { useI18n } from 'vue-i18n'
import ColumnSettingButton from './ColumnSettingButton.vue'

/**
 * 字典选项类型（下拉框等）。
 */
type DictOption = {
  /** 显示文本 */
  label: string
  /** 选项值 */
  value: any
}

/**
 * 组件属性定义。
 * <p>
 * 列设置默认开启：当 {@code showColumnSetting} 未传时视为 true，避免出现
 * {@code length && undefined} 导致的误判。
 * </p>
 */
const props = withDefaults(
  defineProps<{
    /**
     * 表格编码，用于拉取后端表格配置。
     */
    tableCode: string

    /**
     * 行主键字段名，或返回主键的函数。
     */
    rowKey?: string | ((record: any) => string)

    /**
     * 字典选项映射，用于查询区与列展示时的下拉数据。
     */
    dictOptions?: Record<string, DictOption[]>

    /**
     * 前端兜底配置：当接口获取表格配置失败或与后端合并时使用。
     */
    fallbackConfig?: Partial<FxTableConfig>

    /**
     * 与 {@link fallbackConfig} 同义的动态覆盖配置（优先与 fallbackConfig 二选一传入即可）。
     */
    dynamicTableConfig?: Partial<FxTableConfig>

    /**
     * 列表数据请求函数。
     *
     * @param payload 请求参数，含分页、查询条件、排序
     * @returns 包含 records 与 total 的 Promise
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

    /**
     * 是否展示查询表单区域；未传或为 true 时展示，为 false 时隐藏。
     */
    showQueryForm?: boolean

    /**
     * 是否展示列设置按钮；默认 true。
     * <p>默认展示列设置，属于表格公共能力。</p>
     */
    showColumnSetting?: boolean

    /**
     * 已废弃（旧版按视口比例分配高度），仅为兼容保留，请勿新业务依赖。
     */
    tableHeightRatio?: number
  }>(),
  {
    /** 列设置属于表格公共能力，默认展示，不依赖菜单按钮权限 */
    showColumnSetting: true,
  },
)

const { t, locale } = useI18n()

const slots = useSlots()

/**
 * 是否传入了名为 toolbar 的插槽。
 */
const hasToolbarSlot = computed(() => !!slots.toolbar)

/**
 * 当前合并后的表格配置（后端 + 前端覆盖）。
 */
const config = ref<FxTableConfig>()

/**
 * 列设置弹层使用的列数据源。
 * <p>
 * 必须用 computed 固定数组引用，避免模板里写 {@code config?.columns ?? []}
 * 每次渲染新建数组导致列设置频繁重绘。
 * </p>
 */
const columnSettingColumns = computed(() => config.value?.columns ?? [])

/**
 * 是否渲染列设置工具条（依赖配置中是否存在列定义）。
 */
const showColumnSettingBar = computed(
  () => !!config.value?.columns?.length && props.showColumnSetting !== false,
)

/**
 * 配置版本号，用于强制刷新表格与查询区的 key。
 */
const configVersion = ref(0)

const ATag = resolveComponent('a-tag') as any

/**
 * 获取页面传入的本地表格配置。
 */
function getLocalConfig() {
  return props.dynamicTableConfig || props.fallbackConfig
}

/**
 * 将局部配置补齐为组件内部可使用的完整配置。
 *
 * @param source 页面传入的局部配置
 * @returns 完整表格配置
 */
function buildLocalConfig(source?: Partial<FxTableConfig>): FxTableConfig {
  return {
    tableCode: source?.tableCode || props.tableCode,
    tableName: source?.tableName || props.tableCode,
    tableType: source?.tableType || 'NORMAL',
    rowKey: source?.rowKey || 'id',
    defaultPageSize: source?.defaultPageSize || 20,
    defaultSortJson: source?.defaultSortJson,
    columns: source?.columns ? [...source.columns] : [],
    queryFields: source?.queryFields ? [...source.queryFields] : [],
    version: source?.version || 1,
  }
}

/**
 * 尝试将字典翻译 JSON 字符串解析并渲染为 Tag。
 *
 * @param dictText 字典展示文本，可能为 JSON 字符串或对象
 * @returns VNode 或 undefined（无法解析时）
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
  return h(ATag, { color: parsed?.color || 'blue', style }, () => label)
}

/**
 * 将字典相关字段统一渲染为 Tag；若无字典文案则用兜底字段。
 *
 * @param dictText 优先展示的字典文本（可为 JSON）
 * @param fallbackText 兜底展示文本（如原始字段值）
 * @returns 渲染结果（VNode 或原始值）
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
          return h(ATag, { color: parsed?.color || 'blue', style }, () => label)
        }
      } catch (e) {
        return h(ATag, { color: 'blue' }, () => dictText)
      }
    }

    return h(ATag, { color: 'blue' }, () => dictText)
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
    return h(ATag, { color: parsed?.color || 'blue', style }, () => label)
  }

  return fallbackText
}

/** 内部 loading，可与外部 props.loading 合并 */
const loading = ref(false)

const resolvedLoading = computed(() => (props.loading === undefined ? loading.value : props.loading))

/**
 * 是否展示查询区域：{@code showQueryForm !== false} 时展示（未传视为展示）。
 */
const resolvedShowQueryForm = computed(() => props.showQueryForm !== false)

/** 表格行数据 */
const tableData = ref<any[]>([])

/** 底部分页状态 */
const pagination = reactive({
  /** 当前页码 */
  current: 1,
  /** 每页条数 */
  pageSize: 20,
  /** 总条数 */
  total: 0,
})

/**
 * 是否显式传入了 pagination 属性。
 * <p>
 * pagination 类型含字面量 {@code false}，Vue 有时会误判未传为 false，故用 vnode.props 判断。
 * </p>
 *
 * @returns 父组件是否在标签上写了 pagination
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

const tableContentRef = ref<HTMLElement | null>(null)
const tableWrapRef = ref<HTMLElement | null>(null)
const paginationRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
const lastSorter = ref<any>(undefined)

let computeScrollYRafPending = false
let mutationObserver: MutationObserver | null = null

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

/** 查询表单双向绑定模型 */
const queryModel = reactive<Record<string, any>>({})

/** 查询区第二行是否展开 */
const isQueryExpanded = ref(false)

/** 解析后的表格 rowKey */
const resolvedRowKey = computed(() => props.rowKey || config.value?.rowKey || 'id')

/**
 * Ant Design Table 列配置（含字典列自定义渲染）。
 */
const tableColumns = computed(() => {
  // 依赖 configVersion 触发重算
  const _ = configVersion.value
  const cols: FxTableColumn[] = config.value?.columns || []

  // 将操作列固定到最后一列
  const actionCol = cols.find(c => c.field === 'action')
  const otherCols = cols.filter(c => c.field !== 'action')
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

    // dictField：后端单独字典字段，优先级高于 dictCode
    if (c.dictField) {
      column.customRender = ({ record }: any) => {
        const dictText = record?.[c.dictField]
        return renderTagByDictText(dictText, record?.[c.field])
      }
    } else if (c.dictCode) {
      column.customRender = ({ record }: any) => {
        const autoDictField = `${c.field}Text`
        // 优先使用后端生成的 xxxText（可能为 JSON 或对象）
        const fromTextField = renderTagByDictText(record?.[autoDictField], undefined)
        if (fromTextField !== undefined) {
          return fromTextField
        }

        const value = record?.[c.field]
        // 兼容字典 JSON 直接落在原字段上的情况
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
 * 表体滚动容器样式：占满卡片内剩余高度（与是否分页无关）。
 */
const tableWrapStyle = computed(() => {
  return { flex: '1 1 auto', minHeight: 0 } as any
})

/**
 * 分页条外层样式：贴在内容区底部。
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

/**
 * 根据容器高度计算纵向滚动高度 scroll.y（未显式传入 scroll.y 时）。
 */
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

  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0

  // tableWrapRef 仅包含表格区域（不含 toolbar / pagination），扣除表头与少量缓冲
  const buffer = 2
  const y = Math.floor(available - headerHeight - buffer)

  // 过小则不设纵向滚动，避免表格被压扁
  const nextY = y > 100 ? y : undefined
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

/**
 * 将 Ant Design Table 的 sorter 规范为 { field, order }。
 *
 * @param sorter 表格 onChange 传入的排序对象或数组
 * @returns 规范化后的排序参数
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
 * 拉取并合并表格配置，初始化 queryModel 与分页大小。
 *
 * @throws 不向外抛出；失败时使用 fallback 或空配置并打日志
 */
async function loadConfig() {
  const localConfig = getLocalConfig()
  try {
    const backendConfig = await getTableConfig({ tableCode: props.tableCode })

    // 新对象引用，确保 Vue 能检测到深层替换
    config.value = {
      ...backendConfig,
      columns: backendConfig.columns ? [...backendConfig.columns] : [],
      queryFields: backendConfig.queryFields ? [...backendConfig.queryFields] : [],
    }

    configVersion.value++
  } catch (e) {
    config.value = buildLocalConfig(localConfig)
    if (!localConfig?.columns?.length) {
      console.error('[FxDynamicTable] 获取表格配置失败:', e)
    } else {
      console.warn('[FxDynamicTable] 后端表格配置不可用，已使用本地配置:', props.tableCode)
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
 * 规范化列定义：补齐 field，过滤无效项。
 *
 * @param cols 原始列数组
 * @returns 规范化后的列
 */
function normalizeColumns(cols: any[]) {
  return (Array.isArray(cols) ? cols : [])
    .map((c: any) => {
      const field = c?.field ?? c?.dataIndex ?? c?.key
      return { ...(c as any), field } as FxTableColumn
    })
    .filter((c: any) => !!c?.field)
}

/**
 * 将 queryModel 转为接口所需查询对象（日期范围格式化为字符串）。
 *
 * @returns 已过滤空值的查询参数
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
 * 执行查询：调用 request，更新表格数据与分页总数，并重算滚动高度。
 *
 * @param sorter 可选排序状态
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
    pagination.total = typeof total === 'number' ? total : parseInt(String(total || '0'), 10)
  } finally {
    loading.value = false
    await nextTick()
    scheduleComputeAutoScrollY()
  }
}

/**
 * 重置查询条件与页码后重新查询。
 */
function handleReset() {
  for (const k of Object.keys(queryModel)) {
    queryModel[k] = undefined
  }
  pagination.current = 1
  handleQuery(lastSorter.value)
}

/**
 * 获取当前查询参数（已规范化）。
 *
 * @returns 查询对象副本
 */
function getQuery() {
  return normalizeQuery()
}

/**
 * 获取当前分页状态。
 *
 * @returns current、pageSize、total
 */
function getPage() {
  return {
    current: pagination.current,
    pageSize: pagination.pageSize,
    total: pagination.total,
  }
}

/**
 * 使用当前页与查询条件重新加载（等同 refresh）。
 *
 * @returns 查询 Promise
 */
function reload() {
  return handleQuery()
}

/**
 * 与 reload 相同，保留别名以兼容旧调用方。
 *
 * @returns 查询 Promise
 */
function refresh() {
  return handleQuery()
}

defineExpose({ getQuery, getPage, reload, refresh })

/**
 * 列设置变更：写回 config.columns 并 bump 版本。
 *
 * @param columns 用户调整后的列配置
 */
function handleColumnChange(columns: FxTableColumn[]) {
  if (config.value) {
    config.value = {
      ...config.value,
      columns: [...columns],
    }
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
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

watch(
  () => locale.value,
  async () => {
    await loadConfig()
    await handleQuery(lastSorter.value)
  },
)

watch(
  pagination,
  async () => {
    await nextTick()
    scheduleComputeAutoScrollY()
  },
  { deep: true },
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
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

onMounted(async () => {
  window.addEventListener('resize', onResizeOrScroll, { passive: true })

  await loadConfig()
  await handleQuery()

  // 仅在子树节点增删时重算高度，避免 style/class 抖动死循环
  const wrapEl = tableWrapRef.value
  if (wrapEl) {
    mutationObserver = new MutationObserver(() => {
      scheduleComputeAutoScrollY()
    })

    mutationObserver.observe(wrapEl, {
      childList: true,
      subtree: true,
    })
  }

  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 100)

  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 300)
})

onBeforeUnmount(() => {
  mutationObserver?.disconnect()
  mutationObserver = null
  window.removeEventListener('resize', onResizeOrScroll)
})
</script>

<style scoped lang="less" src="@/styles/fx-dynamic-table.less"></style>
