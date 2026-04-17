<template>
  <div class="fx-dynamic-table">
    <!-- 查询区域卡片，保留较紧凑的内边距 -->
    <a-card
      v-if="resolvedShowQuery表单 && config && config.queryFields?.length"
      :key="`query-${configVersion}`"
      :bordered="false"
      class="fx-card fx-query-card"
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
      <div ref="tableContentRef" class="fx-table-content">
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
 * 基于后端表格配置自动生成查询表单与表格列，支持字典选项和自定义插槽。
 *
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
 * 字典选项类型。
 */
type DictOption = { 
  label: string; // 显示文本
  value: any;   // 实际值
}

/**
 * 组件属性。
 * <p>
 * 列设置默认开启：当 {@code showColumnSetting} 未传时视为 true，避免出现
 * {@code length && undefined} 导致的误判。
 * </p>
 */
const props = withDefaults(
  defineProps<{
  /**
   * 琛ㄦ牸缂栫爜锛岀敤浜庤幏鍙栬〃鏍奸厤缃?
   */
  tableCode: string
  
  /**
   * 琛屼富閿瓧娈靛悕鎴栧嚱鏁?
   */
  rowKey?: string | ((record: any) => string)
  
  /**
   * 瀛楀吀閫夐」閰嶇疆锛岀敤浜庝笅鎷夐€夋嫨妗?
   */
  dictOptions?: Record<string, DictOption[]>
  
  /**
   * 闄嶇骇閰嶇疆锛屽綋鑾峰彇琛ㄦ牸閰嶇疆澶辫触鏃朵娇鐢?
   */
  降级方案Config?: Partial<FxTableConfig>
  dynamicTableConfig?: Partial<FxTableConfig>
  
  /**
   * 鏁版嵁璇锋眰鍑芥暟
   * @param payload 璇锋眰鍙傛暟锛屽寘鍚垎椤点€佹煡璇㈡潯浠跺拰鎺掑簭淇℃伅
   * @returns Promise<{ records: any[]; total: number }> 鍖呭惈璁板綍鍒楄〃鍜屾€绘潯鏁扮殑Promise
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
  showQuery表单?: boolean
  showQueryForm?: boolean
  
  /**
   * 鏄惁鏄剧ず鍒楄缃寜閽?
   * <p>榛樿涓?true锛屾樉绀哄垪璁剧疆鎸夐挳銆?/p>
   */
  showColumnSetting?: boolean

  // 宸蹭笉鍐嶄娇鐢紙鏃х増姣斾緥鍒嗛厤锛夛紝淇濈暀浠呬负鍏煎鎬?
  tableHeightRatio?: number
  }>(),
  {
      /** 列设置属于表格公共能力，默认展示，不依赖菜单按钮权限 */
    showColumnSetting: true,
  }
)

const { t, locale } = useI18n()

const slots = useSlots()

/**
 * 是否传入了工具栏插槽。
 */
const hasToolbarSlot = computed(() => !!slots.toolbar)

/**
 * 当前表格配置。
 */
const config = ref<FxTableConfig>()

/**
 * 列设置面板使用的列数据源。
 * <p>
 * 必须通过 computed 固定引用，避免模板中使用 {@code config?.columns ?? []}
 * 时反复创建新数组，导致列设置弹层频繁重渲染。
 * </p>
 */
const columnSettingColumns = computed(() => config.value?.columns ?? [])

/**
 * 是否显示列设置区域。
 */
const showColumnSettingBar = computed(
  () => !!config.value?.columns?.length && props.showColumnSetting !== false
)

/**
 * 配置版本号，用于触发相关计算属性刷新。
 */
const configVersion = ref(0)

const ATag = resolveComponent('a-tag') as any

/**
 * 尝试把字典翻译 JSON 文本渲染为 Tag。
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
 * 将字典文本统一渲染为 Tag。
 */
function renderTagByDictText(dictText: any, 降级方案Text: any) {
  if (dictText === undefined || dictText === null || dictText === '') {
    const fromFallback = tryRenderTagFromDictJson(降级方案Text)
    if (fromFallback !== undefined) {
      return fromFallback
    }
    return 降级方案Text
  }

  if (typeof dictText === 'string') {
    const trimmed = dictText.trim()
    if (trimmed.startsWith('{') && trimmed.endsWith('}')) {
      try {
        const parsed = JSON.parse(trimmed)
        if (parsed && typeof parsed === 'object' && !Array.isArray(parsed)) {
          const label = parsed?.label ?? parsed?.name ?? 降级方案Text ?? dictText
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
    const label = parsed?.label ?? parsed?.name ?? 降级方案Text
    const style =
      parsed?.borderColor || parsed?.backgroundColor
        ? {
            borderColor: parsed?.borderColor,
            backgroundColor: parsed?.backgroundColor,
          }
        : undefined
    return h(ATag, { color: parsed?.color || 'blue', style }, () => label)
  }

  return 降级方案Text
}

/**
 * 内部加载状态。
 */
const loading = ref(false)

const resolvedLoading = computed(() => (props.loading === undefined ? loading.value : props.loading))
const resolvedShowQuery表单 = computed(() => {
  if (props.showQueryForm !== undefined) {
    return props.showQueryForm !== false
  }
  return props.showQuery表单 !== false
})

/**
 * 表格数据。
 */
const tableData = ref<any[]>([])

/**
 * 分页信息。
 */
const pagination = reactive({
  current: 1,     // 当前页码
  pageSize: 20,   // 每页条数
  total: 0,       // 总条数
})

/**
 * 判断当前组件是否显式传入了 pagination 属性。
 * <p>
 * 因为 pagination 的 TS 类型包含字面量 {@code false}，Vue 在某些情况下会把它
 * 推断成 Boolean prop，导致未传值时也被识别为 false，所以这里需要额外判断。
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

/**
 * 鏌ヨ妯″瀷
 */
const queryModel = reactive<Record<string, any>>({})

/**
 * 鎼滅储鍖哄煙灞曞紑鐘舵€?
 */
const isQueryExpanded = ref(false)

/**
 * 瑙ｆ瀽鍚庣殑琛屼富閿?
 */
const resolvedRowKey = computed(() => props.rowKey || config.value?.rowKey || 'id')

/**
 * 琛ㄦ牸鍒楅厤缃?
 */
const tableColumns = computed(() => {
  // 渚濊禆 configVersion 寮哄埗鍒锋柊
  const _ = configVersion.value
  const cols: FxTableColumn[] = config.value?.columns || []
  
  console.log('[FxDynamicTable] tableColumns computed 琚皟鐢紝version:', configVersion.value, 'columns:', cols.length, '绗竴鍒?', cols[0]?.title)
  
  // 灏嗘搷浣滃垪绉诲埌鏈€鍚?
  const actionCol = cols.find(c => c.field === 'action')
  const otherCols = cols.filter(c => c.field !== 'action')
  
  // 閲嶆柊缁勫悎鍒楋紝灏嗘搷浣滃垪鏀惧湪鏈€鍚?
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
    
    // 濡傛灉鍒楁湁瀛楀吀瀛楁閰嶇疆锛屾坊鍔犺嚜瀹氫箟娓叉煋锛堜紭鍏堢骇鏇撮珮锛?
    if (c.dictField) {
      column.customRender = ({ record }: any) => {
        const dictText = record?.[c.dictField]
        return renderTagByDictText(dictText, record?.[c.field])
      }
    }
    // 濡傛灉鍒楁湁瀛楀吀閰嶇疆锛屾坊鍔犺嚜瀹氫箟娓叉煋
    else if (c.dictCode) {
      column.customRender = ({ record }: any) => {
        const autoDictField = `${c.field}Text`
        // 浼樺厛浣跨敤鍚庣鑷姩缈昏瘧鐢熸垚鐨?xxxText 瀛楁锛堝彲鑳戒负 JSON 瀛楃涓叉垨瀵硅薄锛?
        const fromTextField = renderTagByDictText(record?.[autoDictField], undefined)
        if (fromTextField !== undefined) {
          return fromTextField
        }

        const value = record?.[c.field]
        // 鍏煎鍚庣鐩存帴鎶婂瓧鍏?JSON 鏀惧湪鍘熷瓧娈典笂鐨勬儏鍐碉紙渚嬪 gender/status 鐩存帴杩斿洖 JSON 瀛楃涓诧級
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
 * 鍏抽敭淇敼锛氳〃鏍煎尯鍩熷缁堝崰婊″墿浣欑┖闂达紙鏃犺鏄惁鏈夊垎椤碉級
 */
const tableWrapStyle = computed(() => {
  return { flex: '1 1 auto', minHeight: 0 } as any
})

/**
 * 鍒嗛〉鍖哄煙甯冨眬鏍峰紡锛堜繚鎸佷笉鍙橈級
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

  // 鑾峰彇琛ㄦ牸澶撮儴楂樺害
  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0

  // tableWrapRef 浠呭寘鍚〃鏍煎尯鍩燂紙涓嶅寘鍚?toolbar / pagination锛夛紝鍥犳杩欓噷鍙渶鎵ｉ櫎琛ㄥご涓庡皯閲忕紦鍐?
  const buffer = 2
  const y = Math.floor(available - headerHeight - buffer)
  
  const nextY = y > 100 ? y : undefined  // 纭繚鏈€灏忛珮搴︿负100px锛岄伩鍏嶈〃鏍艰繃灏?
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

/**
 * 鏍煎紡鍖栨帓搴忎俊鎭?
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
 * 鍔犺浇琛ㄦ牸閰嶇疆
 */
async function loadConfig() {
  console.log('[FxDynamicTable] loadConfig 寮€濮嬶紝tableCode:', props.tableCode, 'locale:', locale.value)
  try {
    const backendConfig = await getTableConfig({ tableCode: props.tableCode })
    console.log('[FxDynamicTable] 鍚庣杩斿洖閰嶇疆:', backendConfig)
    const mergedConfig = mergeConfigs(backendConfig, props.dynamicTableConfig ?? props.降级方案Config)
    
    // 寮哄埗鍒涘缓鏂板璞★紝纭繚 Vue 鍝嶅簲寮忕郴缁熻兘妫€娴嬪埌鍙樺寲
    config.value = {
      ...mergedConfig,
      columns: mergedConfig.columns ? [...mergedConfig.columns] : [],
      queryFields: mergedConfig.queryFields ? [...mergedConfig.queryFields] : []
    }
    
    // 澧炲姞鐗堟湰鍙凤紝寮哄埗鍒锋柊 computed
    configVersion.value++
    
    console.log('[FxDynamicTable] 閰嶇疆鏇存柊瀹屾垚锛宑olumns:', config.value.columns?.length, 'queryFields:', config.value.queryFields?.length, 'version:', configVersion.value)
  } catch (e) {
    console.error('[FxDynamicTable] 鑾峰彇琛ㄦ牸閰嶇疆澶辫触:', e)
    if (props.dynamicTableConfig || props.降级方案Config) {
      config.value = { ...(props.dynamicTableConfig ?? props.降级方案Config) } as any
    } else {
      config.value = {
        tableCode: props.tableCode,
        tableName: '榛樿琛ㄦ牸',
        tableType: 'NORMAL',
        rowKey: 'id',
        defaultPageSize: 20,
        columns: [],
        queryFields: [],
        version: 1
      }
      console.warn('[FxDynamicTable] 浣跨敤榛樿绌洪厤缃紝琛ㄦ牸鍙兘鏃犳硶姝ｅ父鏄剧ず')
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
 * 鍚堝苟琛ㄦ牸閰嶇疆
 */
function mergeConfigs(backendConfig: FxTableConfig | undefined, 降级方案Config: Partial<FxTableConfig> | undefined): FxTableConfig {
  if (!backendConfig) {
    return 降级方案Config as any
  }
  
  if (!降级方案Config) {
    return backendConfig
  }
  
  const merged: FxTableConfig = { ...(降级方案Config as any), ...(backendConfig as any) }

  const backendColumns = normalizeColumns(backendConfig.columns || [])
  const 降级方案Columns = normalizeColumns((降级方案Config.columns || []) as any[])

  if (降级方案Columns.length) {
    const 降级方案Map = new Map<string, FxTableColumn>()
    for (const fc of 降级方案Columns) {
      降级方案Map.set(fc.field, fc)
    }

    const mergedColumns: FxTableColumn[] = backendColumns.map(bc => {
      const fc = 降级方案Map.get(bc.field)
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
    for (const fc of 降级方案Columns) {
      if (!backendFieldSet.has(fc.field)) {
        mergedColumns.push(fc)
      }
    }

    merged.columns = mergedColumns
  } else {
    merged.columns = backendColumns
  }

  const backendQueryFields = backendConfig.queryFields || []
  const 降级方案QueryFields = (降级方案Config.queryFields || []) as any[]

  if (降级方案QueryFields.length) {
    const 降级方案Map = new Map<string, any>()
    for (const fq of 降级方案QueryFields) {
      降级方案Map.set(fq.field, fq)
    }
    const mergedQueryFields = backendQueryFields.map((bq: any) => {
      const fq = 降级方案Map.get(bq.field)
      return { ...(fq as any), ...(bq as any) }
    })
    const backendFieldSet = new Set(backendQueryFields.map((q: any) => q.field))
    for (const fq of 降级方案QueryFields) {
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
 * 鏍煎紡鍖栨煡璇㈡潯浠?
 * 
 * 鎵ц姝ラ锛?
 * 1. 閬嶅巻閰嶇疆涓殑鎵€鏈夋煡璇㈠瓧娈?
 * 2. 鑾峰彇姣忎釜瀛楁鐨勫€?
 * 3. 璺宠繃绌哄€硷紙undefined銆乶ull銆佺┖瀛楃涓诧級
 * 4. 澶勭悊鏃ユ湡鑼冨洿瀛楁锛岃浆鎹负鏍囧噯鏍煎紡
 * 5. 杩斿洖鏍煎紡鍖栧悗鐨勬煡璇㈠璞?
 * 
 * @returns 鏍煎紡鍖栧悗鐨勬煡璇㈡潯浠跺璞?
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
 * 澶勭悊鏌ヨ
 * 
 * 鎵ц姝ラ锛?
 * 1. 璁剧疆鍔犺浇鐘舵€佷负 true
 * 2. 璋冪敤 props.request 鏂规硶锛屼紶鍏ュ垎椤点€佹煡璇㈡潯浠躲€佹帓搴忎俊鎭?
 * 3. 浠庡搷搴斾腑鎻愬彇 records 鍜?total
 * 4. 鏇存柊琛ㄦ牸鏁版嵁鍜屽垎椤垫€绘暟
 * 5. 閲嶇疆鍔犺浇鐘舵€?
 * 6. 閲嶆柊璁＄畻琛ㄦ牸婊氬姩楂樺害
 * 
 * @param sorter 鎺掑簭淇℃伅锛堝彲閫夛級
 * @throws 璇锋眰澶辫触鏃舵姏鍑哄紓甯?
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
 * 澶勭悊閲嶇疆
 * 
 * 鎵ц姝ラ锛?
 * 1. 娓呯┖鎵€鏈夋煡璇㈡潯浠?
 * 2. 閲嶇疆鍒嗛〉鍒扮涓€椤?
 * 3. 浣跨敤涓婃鐨勬帓搴忎俊鎭噸鏂版煡璇?
 */
function handleReset() {
  for (const k of Object.keys(queryModel)) {
    queryModel[k] = undefined
  }
  pagination.current = 1
  handleQuery(lastSorter.value)
}

/**
 * 鑾峰彇褰撳墠鏌ヨ鏉′欢
 * 
 * @returns 褰撳墠鏌ヨ鏉′欢瀵硅薄
 */
function getQuery() {
  return normalizeQuery()
}

/**
 * 鑾峰彇褰撳墠鍒嗛〉淇℃伅
 * 
 * @returns 鍒嗛〉淇℃伅瀵硅薄锛屽寘鍚?current锛堝綋鍓嶉〉锛夈€乸ageSize锛堟瘡椤垫潯鏁帮級銆乼otal锛堟€绘暟锛?
 */
function getPage() {
  return { 
    current: pagination.current, 
    pageSize: pagination.pageSize, 
    total: pagination.total 
  }
}

/**
 * 閲嶆柊鍔犺浇鏁版嵁
 * 
 * 鎵ц姝ラ锛?
 * 1. 璋冪敤 handleQuery 鏂规硶锛屼娇鐢ㄥ綋鍓嶅垎椤靛拰鏌ヨ鏉′欢
 * 
 * @returns Promise
 */
function reload() {
  return handleQuery()
}

/**
 * 鍒锋柊鏁版嵁
 * 
 * 鎵ц姝ラ锛?
 * 1. 璋冪敤 handleQuery 鏂规硶锛屼娇鐢ㄥ綋鍓嶅垎椤靛拰鏌ヨ鏉′欢
 * 
 * @returns Promise
 */
function refresh() {
  return handleQuery()
}

// 鏆撮湶缁欑埗缁勪欢浣跨敤鐨勬柟娉?
defineExpose({ getQuery, getPage, reload, refresh })

/**
 * 澶勭悊鍒楅厤缃彉鏇?
 * <p>
 * 褰撶敤鎴烽€氳繃鍒楄缃寜閽慨鏀瑰垪閰嶇疆鍚庯紝鏇存柊琛ㄦ牸鍒楁樉绀恒€?
 * </p>
 *
 * @param columns 鏇存柊鍚庣殑鍒楅厤缃?
 */
function handleColumnChange(columns: FxTableColumn[]) {
  if (config.value) {
    // 鏇存柊閰嶇疆涓殑鍒?
    config.value = {
      ...config.value,
      columns: [...columns]
    }
    // 寮哄埗鍒锋柊 computed
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
    // 閰嶇疆鍔犺浇鍚庨噸鏂拌绠楄〃鏍奸珮搴?
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

// 鐩戝惉璇█鍙樺寲 - 浣跨敤鍑芥暟褰㈠紡纭繚鑳芥纭洃鍚?
watch(
  () => locale.value,
  async (newLocale, oldLocale) => {
    console.log('[FxDynamicTable] 璇█鍒囨崲锛岄噸鏂板姞杞介厤缃?', newLocale, '(鏃?', oldLocale, ')')
    // 閲嶆柊鍔犺浇閰嶇疆
    await loadConfig()
    // 閲嶆柊鍔犺浇鏁版嵁
    await handleQuery(lastSorter.value)
  },
)

watch(
  pagination,
  async () => {
    // 鍒嗛〉淇℃伅鍙樺寲鏃堕噸鏂拌绠楄〃鏍奸珮搴︼紝纭繚琛ㄦ牸姝ｇ‘鏄剧ず
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
    // 鏁版嵁鍙樺寲鏃堕噸鏂拌绠楄〃鏍奸珮搴?
    await nextTick()
    scheduleComputeAutoScrollY()
  },
)

onMounted(async () => {
  window.addEventListener('resize', onResizeOrScroll, { passive: true })

  await loadConfig()
  await handleQuery()
  
  // 监听表格结构变化，仅在节点增删时重算高度，避免 style/class 抖动造成死循环。
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
  
  // 寤惰繜璁＄畻锛岀‘淇?DOM 瀹屽叏娓叉煋
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

/* 宸ュ叿鏍?+ 鍒楄缃細鍚屼竴琛岋紝宸﹀榻愭搷浣?/ 鍙冲榻愬垪璁剧疆 */
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

/* 琛ㄦ牸鍐呭鍖猴細涓庝笂鏂规寜閽鐣欏嚭闂磋窛锛坢argin-bottom 宸插湪 toolbar-row锛?*/
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

/* 鍒嗛〉鍣ㄩ棿璺濊皟鏁?*/
.fx-table-pagination {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  justify-content: flex-end;
  padding: 12px 0 0 0;  /* 椤堕儴12px闂磋窛锛屽叾浠栨柟鍚?锛堝灞傚凡鏈塸adding锛?*/
}

.fx-table-pagination :deep(.ant-pagination) {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;  /* 鏁翠綋鍙冲榻?*/
  gap: 16px;  /* 鍏冪礌涔嬮棿鐨勯棿璺?*/
}

.fx-table-pagination :deep(.ant-pagination-total-text) {
  /* 涓嶈缃壒娈婃牱寮忥紝淇濇寔榛樿椤哄簭锛屾樉绀哄湪椤电爜宸﹁竟 */
}
</style>
