<template>
  <div class="excel-config-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ExcelExportConfigTable"
      :request="handleRequest"
      :降级方案-config="降级方案Config"
      row-key="id"
      :show-query-form="true"
      :pagination="{
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => t('common.total', { total }),
      }"
    >
      <template #toolbar>
        <a-button type="primary" v-permission="'sys:excel:exportConfig:edit'" @click="openEdit()">
          {{ t('common.add') }}
        </a-button>
      </template>
      <template #action="{ record }">
        <a-space>
          <a v-permission="'sys:excel:exportConfig:edit'" @click="openEdit(record.id)">{{ t('common.edit') }}</a>
          <a v-permission="'sys:excel:exportConfig:delete'" style="color:#ff4d4f" @click="handleDelete(record.id)">
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="editOpen"
      :title="t('system.excel.exportConfigTitle')"
      :width="980"
      :loading="saving"
      @submit="handleSave"
    >
      <a-form :model="edit表单" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item :label="t('system.excel.tableName')">
          <a-input v-model:value="edit表单.tableName" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.tableCode')">
          <a-input v-model:value="edit表单.tableCode" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.title')">
          <a-input v-model:value="edit表单.title" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.subtitle')">
          <a-input v-model:value="edit表单.subtitle" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.export表单at')">
          <a-select v-model:value="edit表单.export表单at" style="width: 160px">
            <a-select-option value="xlsx">xlsx</a-select-option>
            <a-select-option value="csv">csv</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>

      <a-divider>{{ t('system.excel.exportFields') }}</a-divider>
      <div style="margin-bottom: 8px;">
        <a-button @click="addItem">{{ t('common.add') }}</a-button>
      </div>
      <a-table :columns="itemColumns" :data-source="edit表单.items" row-key="_k" size="small" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'exportField'">
            <a-input v-model:value="record.exportField" />
          </template>
          <template v-else-if="column.key === 'fieldName'">
            <a-input v-model:value="record.fieldName" />
          </template>
          <template v-else-if="column.key === 'orderNum'">
            <a-input-number v-model:value="record.orderNum" :min="0" style="width: 120px" />
          </template>
          <template v-else-if="column.key === 'action'">
            <a style="color:#ff4d4f" @click="removeItem(record._k)">{{ t('common.remove') }}</a>
          </template>
        </template>
      </a-table>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { deleteExportConfig, exportConfigDetail, pageExportConfig, saveExportConfig } from '@/api/system/excel'

const { t } = useI18n()
const tableRef = ref()

const itemColumns = computed(() => [
  { title: t('system.excel.exportField'), key: 'exportField', width: 260 },
  { title: t('system.excel.fieldName'), key: 'fieldName', width: 260 },
  { title: t('common.order'), key: 'orderNum', width: 120 },
  { title: t('common.action'), key: 'action', width: 80 },
])

const 降级方案Config = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ExcelExportConfigTable',
  tableName: t('system.excel.exportConfigTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'tableName', title: t('system.excel.tableName'), minWidth: 180, align: 'left' },
    { field: 'tableCode', title: t('system.excel.tableCode'), width: 180, align: 'left' },
    { field: 'title', title: t('system.excel.title'), minWidth: 180, align: 'left' },
    { field: 'subtitle', title: t('system.excel.subtitle'), minWidth: 180, align: 'left' },
    { field: 'export表单at', title: t('system.excel.export表单at'), width: 120, align: 'center' },
    { field: 'action', title: t('common.action'), width: 140, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'tableName', label: t('system.excel.tableName'), queryType: 'input', queryOperator: 'like' },
    { field: 'tableCode', label: t('system.excel.tableCode'), queryType: 'input', queryOperator: 'like' },
  ],
  version: 1,
}))

const editOpen = ref(false)
const saving = ref(false)
const edit表单 = reactive<any>({
  id: undefined,
  tableName: '',
  tableCode: '',
  title: '',
  subtitle: '',
  export表单at: 'xlsx',
  enableTotal: false,
  version: 1,
  items: [],
})

function pickQueryValue(query: Record<string, any>, keys: string[]): string | undefined {
  for (const key of keys) {
    const value = query?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      return String(value).trim()
    }
  }
  return undefined
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  try {
    const tableName = pickQueryValue(payload.query, ['tableName', 'table_name'])
    const tableCode = pickQueryValue(payload.query, ['tableCode', 'table_code'])
    const res: any = await pageExportConfig({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName,
      tableCode,
    })
    return {
      success: true,
      data: res.records || [],
      total: res.total || 0,
    }
  } catch (error) {
    console.error('鍔犺浇瀵煎嚭閰嶇疆鍒楄〃澶辫触:', error)
    return {
      success: false,
      data: [],
      total: 0,
    }
  }
}

async function openEdit(id?: number) {
  edit表单.id = undefined
  edit表单.tableName = ''
  edit表单.tableCode = ''
  edit表单.title = ''
  edit表单.subtitle = ''
  edit表单.export表单at = 'xlsx'
  edit表单.enableTotal = false
  edit表单.version = 1
  edit表单.items = []

  if (id) {
    const detail: any = await exportConfigDetail({ id })
    Object.assign(edit表单, detail || {})
    edit表单.items = (detail?.items || []).map((x: any, idx: number) => ({ ...x, _k: `${x.id || idx}-${Date.now()}` }))
  }
  editOpen.value = true
}

function addItem() {
  edit表单.items.push({ _k: `${Date.now()}-${Math.random()}`, exportField: '', fieldName: '', orderNum: 0 })
}

function removeItem(k: string) {
  edit表单.items = edit表单.items.filter((x: any) => x._k !== k)
}

async function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteExportConfig({ id })
      message.success(t('common.deleted'))
      tableRef.value?.refresh?.()
    },
  })
}

async function handleSave() {
  saving.value = true
  try {
    const payload = {
      ...edit表单,
      items: (edit表单.items || []).map((x: any) => ({
        id: x.id,
        exportField: x.exportField,
        fieldName: x.fieldName,
        i18nJson: x.i18nJson,
        headerStyleJson: x.headerStyleJson,
        cellStyleJson: x.cellStyleJson,
        orderNum: x.orderNum,
      })),
    }
    await saveExportConfig(payload)
    message.success(t('common.saved'))
    editOpen.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}
</script>

<style scoped lang="less">
.excel-config-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
