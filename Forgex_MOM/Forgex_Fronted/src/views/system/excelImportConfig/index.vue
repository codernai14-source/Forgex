<template>
  <div class="excel-config-container">
    <a-card :bordered="false">
      <a-form layout="inline" :model="queryForm">
        <a-form-item label="表名">
          <a-input v-model:value="queryForm.tableName" placeholder="请输入表名" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item label="表编号">
          <a-input v-model:value="queryForm.tableCode" placeholder="请输入表编号" allow-clear style="width: 220px" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button v-permission="'sys:excel:importConfig:list'" type="primary" @click="handleQuery">查询</a-button>
            <a-button @click="handleReset">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <div style="margin-top: 16px;">
        <a-space>
          <a-button v-permission="'sys:excel:importConfig:edit'" type="primary" @click="openEdit()">新增</a-button>
        </a-space>
      </div>

      <a-table
        :columns="columns"
        :data-source="tableData"
        :loading="loading"
        :pagination="{
          current: pagination.current,
          pageSize: pagination.size,
          total: pagination.total,
          showSizeChanger: true,
          showQuickJumper: true,
          showTotal: (total: number) => `共 ${total} 条`,
        }"
        row-key="id"
        @change="handleTableChange"
        style="margin-top: 16px"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a v-permission="'sys:excel:importConfig:edit'" @click="openEdit(record.id)">编辑</a>
              <a v-permission="'sys:excel:template:download'" @click="handleDownload(record.tableCode)">下载模板</a>
              <a v-permission="'sys:excel:importConfig:delete'" style="color:#ff4d4f" @click="handleDelete(record.id)">删除</a>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>

    <a-modal
      v-model:open="editOpen"
      title="导入配置"
      :width="980"
      :confirm-loading="saving"
      @ok="handleSave"
    >
      <a-form :model="editForm" :label-col="{ span: 5 }" :wrapper-col="{ span: 19 }">
        <a-form-item label="表名">
          <a-input v-model:value="editForm.tableName" allow-clear />
        </a-form-item>
        <a-form-item label="表编号">
          <a-input v-model:value="editForm.tableCode" allow-clear />
        </a-form-item>
        <a-form-item label="标题">
          <a-input v-model:value="editForm.title" allow-clear />
        </a-form-item>
        <a-form-item label="小字说明">
          <a-input v-model:value="editForm.subtitle" allow-clear />
        </a-form-item>
      </a-form>

      <a-divider>导入字段</a-divider>
      <div style="margin-bottom: 8px;">
        <a-button @click="addItem">新增字段</a-button>
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
            <a style="color:#ff4d4f" @click="removeItem(record._k)">移除</a>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 导入配置管理页。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
import { reactive, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { deleteImportConfig, downloadTemplate, importConfigDetail, pageImportConfig, saveImportConfig } from '@/api/system/excel'

const queryForm = reactive({ tableName: '', tableCode: '' })
const pagination = reactive({ current: 1, size: 20, total: 0 })
const loading = ref(false)
const tableData = ref<any[]>([])

const columns = [
  { title: '表名', dataIndex: 'tableName', key: 'tableName' },
  { title: '表编号', dataIndex: 'tableCode', key: 'tableCode' },
  { title: '版本', dataIndex: 'version', key: 'version', width: 80 },
  { title: '操作', key: 'action', width: 240 },
]

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

async function handleQuery() {
  loading.value = true
  try {
    const res: any = await pageImportConfig({
      current: pagination.current,
      size: pagination.size,
      tableName: queryForm.tableName || undefined,
      tableCode: queryForm.tableCode || undefined,
    })
    tableData.value = res.records || []
    pagination.total = res.total || 0
  } finally {
    loading.value = false
  }
}

function handleReset() {
  queryForm.tableName = ''
  queryForm.tableCode = ''
  pagination.current = 1
  handleQuery()
}

function handleTableChange(pag: any) {
  pagination.current = pag.current
  pagination.size = pag.pageSize
  handleQuery()
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
    title: '确认删除？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await deleteImportConfig({ id })
      message.success('已删除')
      handleQuery()
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
    message.success('已保存')
    editOpen.value = false
    handleQuery()
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

handleQuery()
</script>

<style scoped lang="less">
.excel-config-container {
  padding: 20px;
}
</style>

