<template>
  <div class="excel-config-container">
    <a-card :bordered="false">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'ExcelExportConfigTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        row-key="id"
        :pagination="{
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
        }"
      >
        <template #action="{ record }">
          <a-space>
            <a v-permission="'sys:excel:exportConfig:edit'" @click="openEdit(record.id)">编辑</a>
            <a v-permission="'sys:excel:exportConfig:delete'" style="color:#ff4d4f" @click="handleDelete(record.id)">删除</a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

    <a-modal
      v-model:open="editOpen"
      :title="t('system.excel.exportConfigTitle')"
      :width="980"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form :model="editForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item :label="t('system.excel.tableName')">
          <a-input v-model:value="editForm.tableName" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.tableCode')">
          <a-input v-model:value="editForm.tableCode" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.title')">
          <a-input v-model:value="editForm.title" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.subtitle')">
          <a-input v-model:value="editForm.subtitle" allow-clear />
        </a-form-item>
        <a-form-item :label="t('system.excel.exportFormat')">
          <a-select v-model:value="editForm.exportFormat" style="width: 160px">
            <a-select-option value="xlsx">xlsx</a-select-option>
            <a-select-option value="csv">csv</a-select-option>
          </a-select>
        </a-form-item>
      </a-form>

      <a-divider>{{ t('system.excel.exportFields') }}</a-divider>
      <div style="margin-bottom: 8px;">
        <a-button @click="addItem">{{ t('common.add') }}</a-button>
      </div>
      <a-table :columns="itemColumns" :data-source="editForm.items" row-key="_k" size="small" :pagination="false">
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
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { deleteExportConfig, exportConfigDetail, pageExportConfig, saveExportConfig } from '@/api/system/excel'

const { t } = useI18n()
const tableRef = ref()

const fallbackConfig = {
  tableCode: 'ExcelExportConfigTable',
  tableName: 'Excel导出配置',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'table_name', title: '表名', width: 200 },
    { field: 'table_code', title: '表格编码', width: 200 },
    { field: 'export_format', title: '导出格式', width: 120 },
    { field: 'version', title: '版本', width: 80 },
    { field: 'action', title: '操作', width: 160, fixed: 'right' }
  ],
  queryFields: [
    { field: 'table_name', label: '表名', queryType: 'input', queryOperator: 'like' },
    { field: 'table_code', label: '表格编码', queryType: 'input', queryOperator: 'like' }
  ],
  version: 1,
}

const itemColumns = [
  { title: '导出字段', key: 'exportField', width: 260 },
  { title: '列头名称', key: 'fieldName', width: 260 },
  { title: '顺序', key: 'orderNum', width: 120 },
  { title: '操作', key: 'action', width: 80 },
]

const editOpen = ref(false)
const saving = ref(false)
const editForm = reactive<any>({
  id: undefined,
  tableName: '',
  tableCode: '',
  title: '',
  subtitle: '',
  exportFormat: 'xlsx',
  enableTotal: false,
  version: 1,
  items: [],
})

const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    const res: any = await pageExportConfig({
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName: payload.query.table_name,
      tableCode: payload.query.table_code,
    })
    return { records: res.records || [], total: res.total || 0 }
  } catch (error) {
    console.error('加载导出配置列表失败:', error)
    return { records: [], total: 0 }
  }
}

async function openEdit(id?: number) {
  editForm.id = undefined
  editForm.tableName = ''
  editForm.tableCode = ''
  editForm.title = ''
  editForm.subtitle = ''
  editForm.exportFormat = 'xlsx'
  editForm.enableTotal = false
  editForm.version = 1
  editForm.items = []

  if (id) {
    const detail: any = await exportConfigDetail({ id })
    Object.assign(editForm, detail || {})
    editForm.items = (detail?.items || []).map((x: any, idx: number) => ({ ...x, _k: `${x.id || idx}-${Date.now()}` }))
  }
  editOpen.value = true
}

function addItem() {
  editForm.items.push({ _k: `${Date.now()}-${Math.random()}`, exportField: '', fieldName: '', orderNum: 0 })
}

function removeItem(k: string) {
  editForm.items = editForm.items.filter((x: any) => x._k !== k)
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
      ...editForm,
      items: (editForm.items || []).map((x: any) => ({
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
  padding: 20px;
}
</style>
