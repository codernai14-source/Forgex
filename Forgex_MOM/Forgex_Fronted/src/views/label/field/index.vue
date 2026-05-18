<template>
  <div class="page-container">
    <FxDynamicTable ref="tableRef" table-code="LabelFieldTable" :request="loadData" row-key="id" :row-selection="rowSelection">
      <template #toolbar>
        <a-space>
          <a-button v-permission="'label:field:batchDelete'" danger :disabled="!selectedCount" @click="handleBatchDelete">{{ t('common.batchDelete') }}</a-button>
          <a-button type="primary" @click="openCreate"><PlusOutlined /> 新增</a-button>
          <a-button @click="importVisible = true">导入</a-button>
        </a-space>
      </template>

      <template #isEnabled="{ record }">
        <a-tag :color="record.isEnabled ? 'green' : 'default'">
          {{ record.isEnabled ? '启用' : '停用' }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="openEdit(record)">编辑</a>
          <a @click="toggleEnabled(record)">{{ record.isEnabled ? '停用' : '启用' }}</a>
          <a class="danger-link" @click="handleDelete(record)">删除</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <a-modal v-model:open="editorVisible" :title="editorTitle" @ok="handleSubmit">
      <a-form :model="form" layout="vertical">
        <a-form-item label="字段编码" required><a-input v-model:value="form.fieldCode" /></a-form-item>
        <a-form-item label="字段名称" required><a-input v-model:value="form.fieldName" /></a-form-item>
        <a-form-item label="字段类型" required>
          <a-select v-model:value="form.fieldType">
            <a-select-option value="STRING">STRING</a-select-option>
            <a-select-option value="NUMBER">NUMBER</a-select-option>
            <a-select-option value="DATE">DATE</a-select-option>
            <a-select-option value="DATETIME">DATETIME</a-select-option>
            <a-select-option value="BOOLEAN">BOOLEAN</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="模块" required>
          <a-select v-model:value="form.moduleId" show-search :options="moduleOptions" />
        </a-form-item>
        <a-form-item label="是否启用"><a-switch v-model:checked="form.isEnabled" /></a-form-item>
      </a-form>
    </a-modal>

    <CommonImportDialog v-model:open="importVisible" table-code="LabelFieldImportTable" @success="tableRef?.reload()" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined } from '@ant-design/icons-vue'
import CommonImportDialog from '@/components/excel/CommonImportDialog.vue'
import { labelFieldApi } from '@/api/label/field'
import { getModuleList } from '@/api/system/module'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const editorVisible = ref(false)
const editorTitle = ref('新增标签字段')
const importVisible = ref(false)
const editingId = ref<number | null>(null)
const form = ref<any>({ fieldCode: '', fieldName: '', fieldType: 'STRING', moduleId: undefined, isEnabled: true })
const moduleOptions = ref<any[]>([])

function loadData(params: any) {
  return labelFieldApi.page(params)
}

async function loadModules() {
  const res = await getModuleList({})
  moduleOptions.value = (res || []).map((item: any) => ({ label: `${item.code} - ${item.name}`, value: item.id }))
}

function openCreate() {
  editingId.value = null
  form.value = { fieldCode: '', fieldName: '', fieldType: 'STRING', moduleId: undefined, isEnabled: true }
  editorTitle.value = '新增标签字段'
  editorVisible.value = true
}

function openEdit(record: any) {
  editingId.value = record.id
  form.value = { fieldCode: record.fieldCode, fieldName: record.fieldName, fieldType: record.fieldType, moduleId: record.moduleId, isEnabled: !!record.isEnabled }
  editorTitle.value = '编辑标签字段'
  editorVisible.value = true
}

async function handleSubmit() {
  if (editingId.value) {
    await labelFieldApi.update({ id: editingId.value, ...form.value })
  } else {
    await labelFieldApi.add(form.value)
  }
  editorVisible.value = false
  tableRef.value?.reload()
}

function toggleEnabled(record: any) {
  Modal.confirm({
    title: record.isEnabled ? '确认停用？' : '确认启用？',
    onOk: async () => {
      await labelFieldApi.enable(record.id, !record.isEnabled)
      tableRef.value?.reload()
    }
  })
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除？',
    onOk: async () => {
      await labelFieldApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    onOk: async () => {
      await labelFieldApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      tableRef.value?.reload()
    }
  })
}

onMounted(loadModules)
</script>
