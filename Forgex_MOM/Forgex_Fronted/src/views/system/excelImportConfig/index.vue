<template>
  <div class="excel-config-container">
    <a-card :bordered="false">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'ExcelImportConfigTable'"
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
            <a v-permission="'sys:excel:importConfig:edit'" @click="openEdit(record.id)">编辑</a>
            <a v-permission="'sys:excel:template:download'" @click="handleDownload(record.tableCode)">下载模板</a>
            <a v-permission="'sys:excel:importConfig:delete'" style="color:#ff4d4f" @click="handleDelete(record.id)">删除</a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

    <a-modal
      v-model:open="editOpen"
      :title="t('system.excel.importConfigTitle')"
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
      </a-form>

      <a-divider>{{ t('system.excel.importFields') }}</a-divider>
      <div style="margin-bottom: 8px;">
        <a-button @click="addItem">{{ t('common.add') }}</a-button>
      </div>
      <a-table :columns="itemColumns" :data-source="editForm.items" row-key="_k" size="small" :pagination="false">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'importField'">
            <a-input v-model:value="record.importField" />
          </template>
          <template v-else-if="column.key === 'fieldType'">
            <a-select v-model:value="record.fieldType" style="width: 140px">
              <a-select-option value="string">string</a-select-option>
              <a-select-option value="number">number</a-select-option>
              <a-select-option value="date">date</a-select-option>
              <a-select-option value="time">time</a-select-option>
              <a-select-option value="datetime">datetime</a-select-option>
              <a-select-option value="dict">dict</a-select-option>
            </a-select>
          </template>
          <template v-else-if="column.key === 'dictCode'">
            <a-input v-model:value="record.dictCode" placeholder="dictCode" />
          </template>
          <template v-else-if="column.key === 'required'">
            <a-switch v-model:checked="record.required" />
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
import { deleteImportConfig, downloadTemplate, importConfigDetail, pageImportConfig, saveImportConfig } from '@/api/system/excel'

const { t } = useI18n()
const tableRef = ref()

const fallbackConfig = {
  tableCode: 'ExcelImportConfigTable',
  tableName: 'Excel导入配置',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'table_name', title: '表名', width: 200 },
    { field: 'table_code', title: '表格编码', width: 200 },
    { field: 'version', title: '版本', width: 80 },
    { field: 'action', title: '操作', width: 240, fixed: 'right' }
  ],
  queryFields: [
    { field: 'table_name', label: '表名', queryType: 'input', queryOperator: 'like' },
    { field: 'table_code', label: '表格编码', queryType: 'input', queryOperator: 'like' }
  ],
  version: 1,
}

const itemColumns = [
  { title: '导入字段', key: 'importField', width: 240 },
  { title: '字段类型', key: 'fieldType', width: 160 },
  { title: '字典编号', key: 'dictCode', width: 160 },
  { title: '必填', key: 'required', width: 80 },
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
  version: 1,
  items: [],
})

const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    const res: any = await pageImportConfig({
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName: payload.query.table_name,
      tableCode: payload.query.table_code,
    })
    return {
      success: true,
      data: res.records || [],
      total: res.total || 0
    }
  } catch (error) {
    console.error('加载导入配置列表失败:', error)
    return {
      success: false,
      data: [],
      total: 0
    }
  }
}

async function openEdit(id?: number) {
  editForm.id = undefined
  editForm.tableName = ''
  editForm.tableCode = ''
  editForm.title = ''
  editForm.subtitle = ''
  editForm.version = 1
  editForm.items = []

  if (id) {
    const detail: any = await importConfigDetail({ id })
    Object.assign(editForm, detail || {})
    editForm.items = (detail?.items || []).map((x: any, idx: number) => ({ ...x, _k: `${x.id || idx}-${Date.now()}` }))
  }
  editOpen.value = true
}

function addItem() {
  editForm.items.push({ _k: `${Date.now()}-${Math.random()}`, importField: '', fieldType: 'string', dictCode: '', required: false, orderNum: 0 })
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
      await deleteImportConfig({ id })
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
        i18nJson: x.i18nJson,
        importField: x.importField,
        fieldType: x.fieldType,
        dictCode: x.dictCode,
        required: x.required,
        orderNum: x.orderNum,
      })),
    }
    await saveImportConfig(payload)
    message.success(t('common.saved'))
    editOpen.value = false
    tableRef.value?.refresh?.()
  } finally {
    saving.value = false
  }
}

async function handleDownload(tableCode: string) {
  const resp: any = await downloadTemplate({ tableCode })
  const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `import-template-${tableCode}.xlsx`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  window.URL.revokeObjectURL(url)
}
</script>

<style scoped lang="less">
.excel-config-container {
  padding: 20px;
}
</style>
