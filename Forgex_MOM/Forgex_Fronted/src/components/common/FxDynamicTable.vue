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
      <div class="fx-dynamic-table-wrap">
        <a-table
          :columns="tableColumns"
          :data-source="tableData"
          :loading="resolvedLoading"
          :row-selection="rowSelection"
          :pagination="{
            current: pagination.current,
            pageSize: pagination.pageSize,
            total: pagination.total,
            showSizeChanger: true,
            showQuickJumper: true,
            showTotal: (total: number) => String(t('common.total', { total })),
            hideOnSinglePage: false
          }"
          :default-expand-all-rows="defaultExpandAllRows"
          :expandable="expandable"
          :row-key="resolvedRowKey"
          @change="handleTableChange"
          :scroll="{ x: 'max-content' }"
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
import { computed, onMounted, reactive, ref, watch } from 'vue'
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
  defaultExpandAllRows?: boolean
  expandable?: TableProps['expandable']
  showQueryForm?: boolean
}>()

const { t, locale } = useI18n()

/**
 * 表格配置
 */
const config = ref<FxTableConfig>()

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
  
  return finalCols.map(c => ({
    title: c.title,      // 列标题
    dataIndex: c.field,  // 数据字段名
    key: c.field,        // 列主键
    align: c.align,      // 对齐方式
    width: c.width,      // 列宽
    fixed: c.fixed,      // 是否固定
    ellipsis: c.ellipsis,// 是否显示省略号
    sorter: !!c.sortable,// 是否可排序
  }))
})

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
    config.value = await getTableConfig({ tableCode: props.tableCode })
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
  await loadConfig()
  await handleQuery()
})
</script>

<style scoped>
.fx-dynamic-table {
  display: flex;
  flex-direction: column;
  gap: 16px;
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

.fx-table-toolbar {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.fx-dynamic-table-wrap {
  margin-top: 8px;
  width: 100%;
  overflow-x: auto;
  min-width: 0;
}
</style>
