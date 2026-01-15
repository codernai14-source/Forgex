<template>
  <a-card :bordered="false">
    <a-form v-if="config" layout="inline" :model="queryModel">
      <template v-for="q in config.queryFields" :key="q.field">
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
          <a-input
            v-else
            v-model:value="(queryModel as any)[q.field]"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
      </template>
      <a-form-item>
        <a-space>
          <a-button type="primary" @click="handleQuery">查询</a-button>
          <a-button @click="handleReset">重置</a-button>
          <slot name="toolbar" />
        </a-space>
      </a-form-item>
    </a-form>

    <a-table
      :columns="tableColumns"
      :data-source="tableData"
      :loading="loading"
      :pagination="{
        current: pagination.current,
        pageSize: pagination.pageSize,
        total: pagination.total,
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => `共 ${total} 条`,
      }"
      :row-key="resolvedRowKey"
      @change="handleTableChange"
      style="margin-top: 16px"
    >
      <template #bodyCell="scope">
        <slot name="bodyCell" v-bind="scope" />
      </template>
    </a-table>
  </a-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import type { TableProps } from 'ant-design-vue'
import dayjs from 'dayjs'
import { getTableConfig, type FxTableConfig, type FxTableColumn } from '@/api/system/tableConfig'

type DictOption = { label: string; value: any }

const props = defineProps<{
  tableCode: string
  rowKey?: string | ((record: any) => string)
  dictOptions?: Record<string, DictOption[]>
  fallbackConfig?: Partial<FxTableConfig>
  request: (payload: {
    page: { current: number; pageSize: number }
    query: Record<string, any>
    sorter?: { field?: string; order?: string }
  }) => Promise<{ records: any[]; total: number }>
}>()

const config = ref<FxTableConfig>()
const loading = ref(false)
const tableData = ref<any[]>([])

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
})

const queryModel = reactive<Record<string, any>>({})

const resolvedRowKey = computed(() => props.rowKey || config.value?.rowKey || 'id')

const tableColumns = computed(() => {
  const cols: FxTableColumn[] = config.value?.columns || []
  return cols.map(c => ({
    title: c.title,
    dataIndex: c.field,
    key: c.field,
    align: c.align,
    width: c.width,
    fixed: c.fixed,
    ellipsis: c.ellipsis,
    sorter: !!c.sortable,
  }))
})

function normalizeSorter(sorter: any) {
  if (!sorter) return undefined
  if (Array.isArray(sorter)) sorter = sorter[0]
  const field = sorter?.field || sorter?.columnKey
  const order = sorter?.order
  if (!field && !order) return undefined
  return { field, order }
}

async function loadConfig() {
  try {
    config.value = await getTableConfig({ tableCode: props.tableCode })
  } catch (e) {
    if (props.fallbackConfig) {
      config.value = props.fallbackConfig as any
    } else {
      throw e
    }
  }
  pagination.pageSize = config.value?.defaultPageSize || pagination.pageSize
  for (const q of config.value?.queryFields || []) {
    if (!(q.field in queryModel)) {
      queryModel[q.field] = undefined
    }
  }
}

function normalizeQuery() {
  const out: Record<string, any> = {}
  if (!config.value) return out
  for (const q of config.value.queryFields || []) {
    const v = queryModel[q.field]
    if (v === undefined || v === null || v === '') continue
    if (q.queryType === 'dateRange' && Array.isArray(v) && v.length === 2) {
      out[q.field] = [dayjs(v[0]).format('YYYY-MM-DD HH:mm:ss'), dayjs(v[1]).format('YYYY-MM-DD HH:mm:ss')]
      continue
    }
    out[q.field] = v
  }
  return out
}

async function handleQuery(sorter?: any) {
  loading.value = true
  try {
    const res = await props.request({
      page: { current: pagination.current, pageSize: pagination.pageSize },
      query: normalizeQuery(),
      sorter: normalizeSorter(sorter),
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  for (const k of Object.keys(queryModel)) {
    queryModel[k] = undefined
  }
  pagination.current = 1
  handleQuery()
}

function getQuery() {
  return normalizeQuery()
}

function getPage() {
  return { current: pagination.current, pageSize: pagination.pageSize, total: pagination.total }
}

function reload() {
  return handleQuery()
}

defineExpose({ getQuery, getPage, reload })

const handleTableChange: TableProps['onChange'] = (pag, _filters, sorter) => {
  pagination.current = pag?.current || 1
  pagination.pageSize = pag?.pageSize || pagination.pageSize
  handleQuery(sorter)
}

watch(
  () => props.tableCode,
  async () => {
    pagination.current = 1
    await loadConfig()
    await handleQuery()
  },
)

onMounted(async () => {
  await loadConfig()
  await handleQuery()
})
</script>
