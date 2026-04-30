<template>
  <div class="excel-import-config">
    <FxDynamicTable
      ref="tableRef"
      table-code="ExcelImportConfigTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      row-key="id"
      :show-query-form="true"
      :pagination="{
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => t('common.total', { total }),
      }"
    >
      <template #toolbar>
        <a-button
          data-guide-id="sys-excel-import-edit"
          type="primary"
          v-permission="'sys:excel:importConfig:edit'"
          @click="openEdit()"
        >
          {{ t('common.add') }}
        </a-button>
      </template>

      <template #action="{ record }">
        <a-space>
          <a
            v-permission="'sys:excel:importConfig:edit'"
            @click="openEdit(record.id)"
          >
            {{ t('common.edit') }}
          </a>
          <a
            v-permission="'sys:excel:template:download'"
            @click="handleDownload(record.tableCode)"
          >
            {{ t('system.excel.downloadTemplate') }}
          </a>
          <a
            v-permission="'sys:excel:importConfig:delete'"
            style="color: #ff4d4f"
            @click="handleDelete(record.id)"
          >
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <ExcelImportConfigModal
      ref="modalRef"
      v-model:open="modalOpen"
      :is-edit="isEdit"
      :data="editData"
      @submit="handleModalSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ExcelImportConfigModal from './components/ExcelImportConfigModal.vue'
import {
  deleteImportConfig,
  downloadTemplate,
  importConfigDetail,
  pageImportConfig,
  saveImportConfig,
} from '@/api/system/excel'

const { t } = useI18n()

const tableRef = ref()
const modalOpen = ref(false)
const isEdit = ref(false)
const editData = ref<any>({})
const modalRef = ref()

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ExcelImportConfigTable',
  tableName: t('system.excel.importConfigTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'tableName', title: t('system.excel.tableName'), minWidth: 180, align: 'left' },
    { field: 'tableCode', title: t('system.excel.tableCode'), width: 180, align: 'left' },
    { field: 'title', title: t('system.excel.title'), minWidth: 180, align: 'left' },
    { field: 'subtitle', title: t('system.excel.subtitle'), minWidth: 180, align: 'left' },
    { field: 'version', title: t('system.excel.version'), width: 100, align: 'center' },
    { field: 'action', title: t('common.action'), width: 220, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'tableName', label: t('system.excel.tableName'), queryType: 'input', queryOperator: 'like' },
    { field: 'tableCode', label: t('system.excel.tableCode'), queryType: 'input', queryOperator: 'like' },
  ],
  version: 1,
}))

function pickQueryValue(query: Record<string, any>, keys: string[]) {
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

    const res: any = await pageImportConfig({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName,
      tableCode,
    })

    return {
      records: res.records || [],
      total: res.total || 0,
    }
  } catch (error) {
    console.error('加载导入配置列表失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

async function openEdit(id?: number) {
  isEdit.value = !!id
  editData.value = {}

  if (id) {
    try {
      const detail: any = await importConfigDetail({ id })
      editData.value = detail || {}
    } catch (error) {
      console.error('加载导入配置详情失败', error)
      message.error(t('common.loadFailed'))
      return
    }
  }

  modalOpen.value = true
}

async function handleModalSubmit() {
  try {
    const formData = modalRef.value?.formData
    if (!formData) {
      message.error('表单数据为空')
      return
    }

    const saveData = {
      id: formData.id,
      tableName: formData.tableName,
      tableCode: formData.tableCode,
      title: formData.title,
      subtitle: formData.subtitle,
      version: formData.version,
      fields: formData.fields.map((field: any) => ({
        fieldName: field.fieldName,
        fieldType: field.fieldType,
        dataSourceConfig: field.dataSourceConfig,
        required: field.required,
        orderNum: field.orderNum,
      })),
    }

    await saveImportConfig(saveData)
    message.success(t('common.saveSuccess'))
    tableRef.value?.refresh?.()
    modalOpen.value = false
  } catch (error) {
    console.error('保存导入配置失败', error)
    message.error(t('system.excel.message.saveConfigFailed'))
  }
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await deleteImportConfig({ id })
        message.success(t('common.deleted'))
        tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除导入配置失败', error)
        message.error(t('system.excel.message.deleteConfigFailed'))
      }
    },
  })
}

async function handleDownload(tableCode: string) {
  try {
    const resp: any = await downloadTemplate({ tableCode })
    const contentType = String(resp.headers?.['content-type'] || '').toLowerCase()
    const blob = new Blob([resp.data], {
      type: resp.headers?.['content-type'] || 'application/octet-stream',
    })

    if (contentType.includes('application/json')) {
      await showBlobError(blob)
      return
    }

    const text = await blob.text()
    if (text && text.trim().startsWith('{')) {
      try {
        const parsed = JSON.parse(text)
        if (parsed && typeof parsed === 'object' && 'code' in parsed && Number(parsed.code) !== 200) {
          message.error(parsed.message || t('system.excel.message.downloadTemplateFailed'))
          return
        }
      } catch {
        // ignore
      }
    }

    const url = window.URL.createObjectURL(blob)
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `import-template-${tableCode}.xlsx`
    document.body.appendChild(anchor)
    anchor.click()
    document.body.removeChild(anchor)
    window.URL.revokeObjectURL(url)
    message.success(t('system.excel.message.downloadTemplateSuccess'))
  } catch (error) {
    console.error('下载模板失败', error)
    message.error(t('system.excel.message.downloadTemplateFailed'))
  }
}

async function showBlobError(blob: Blob) {
  try {
    const text = await blob.text()
    const parsed = JSON.parse(text)
    message.error(parsed.message || t('system.excel.message.downloadTemplateFailed'))
  } catch {
    message.error(t('system.excel.message.downloadTemplateFailed'))
  }
}
</script>

<style scoped lang="less">
.excel-import-config {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
