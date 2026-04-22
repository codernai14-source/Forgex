<template>
  <div class="excel-config-container">
    <FxDynamicTable
      ref="tableRef"
      table-code="ExcelExportConfigTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :dict-options="dictOptions"
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

      <template #enabled="{ record }">
        <a-tag :color="normalizeBoolean(record.enabled) ? 'success' : 'default'">
          {{ normalizeBoolean(record.enabled) ? t('common.enabled', '启用') : t('common.disabled', '禁用') }}
        </a-tag>
      </template>

      <template #enableTotal="{ record }">
        <a-tag :color="normalizeBoolean(record.enableTotal) ? 'processing' : 'default'">
          {{ normalizeBoolean(record.enableTotal) ? t('common.yes', '是') : t('common.no', '否') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'sys:excel:exportConfig:edit'" @click="openEdit(record.id)">{{ t('common.edit') }}</a>
          <a
            v-permission="'sys:excel:exportConfig:delete'"
            style="color:#ff4d4f"
            @click="handleDelete(record.id)"
          >
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <BaseFormDialog
      v-model:open="editOpen"
      :title="editForm.id ? `${t('common.edit')}${t('system.excel.exportConfigTitle')}` : `${t('common.add')}${t('system.excel.exportConfigTitle')}`"
      :width="1200"
      :loading="saving"
      @submit="handleSave"
    >
      <a-tabs v-model:activeKey="activeTab" type="card">
        <a-tab-pane key="basic" :tab="t('system.excel.basicDataTab')">
          <a-form ref="basicFormRef" :model="editForm" :rules="basicRules" :label-col="{ span: 5 }" :wrapper-col="{ span: 18 }">
            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item :label="t('system.excel.tableName')" name="tableName">
                  <a-input v-model:value="editForm.tableName" :placeholder="t('system.excel.pleaseInputTableName')" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('system.excel.tableCode')" name="tableCode">
                  <a-input v-model:value="editForm.tableCode" :placeholder="t('system.excel.pleaseInputTableCode')" allow-clear />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item :label="t('system.excel.title')" name="title">
                  <a-input v-model:value="editForm.title" :placeholder="t('system.excel.pleaseInputTitle')" allow-clear />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('system.excel.subtitle')" name="subtitle">
                  <a-input v-model:value="editForm.subtitle" :placeholder="t('system.excel.pleaseInputSubtitle')" allow-clear />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item :label="t('system.excel.exportFormat')" name="exportFormat">
                  <a-select v-model:value="editForm.exportFormat" :placeholder="t('system.excel.pleaseSelectExportFormat')">
                    <a-select-option value="xlsx">xlsx</a-select-option>
                    <a-select-option value="csv">csv</a-select-option>
                  </a-select>
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('system.excel.version')" name="version">
                  <a-input-number v-model:value="editForm.version" :min="1" :step="1" style="width: 100%" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-row :gutter="16">
              <a-col :span="12">
                <a-form-item :label="t('system.excel.enabled')" name="enabled">
                  <a-switch v-model:checked="editForm.enabled" />
                </a-form-item>
              </a-col>
              <a-col :span="12">
                <a-form-item :label="t('system.excel.enableTotal')" name="enableTotal">
                  <a-switch v-model:checked="editForm.enableTotal" />
                </a-form-item>
              </a-col>
            </a-row>

            <a-form-item :label="t('system.excel.headerStyleJson')" name="headerStyleJson">
              <a-textarea v-model:value="editForm.headerStyleJson" :rows="4" :placeholder="headerStylePlaceholder" />
            </a-form-item>
          </a-form>
        </a-tab-pane>

        <a-tab-pane key="fields" :tab="t('system.excel.exportFieldsTab')">
          <div class="field-toolbar">
            <a-button type="primary" @click="addItem">{{ t('common.add') }}</a-button>
          </div>

          <a-table
            :columns="itemColumns"
            :data-source="editForm.items"
            row-key="_k"
            size="small"
            :pagination="false"
            :scroll="{ x: 1400 }"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'exportField'">
                <a-input v-model:value="record.exportField" :placeholder="t('system.excel.exportField')" />
              </template>
              <template v-else-if="column.key === 'fieldName'">
                <a-input v-model:value="record.fieldName" :placeholder="t('system.excel.fieldName')" />
              </template>
              <template v-else-if="column.key === 'i18nJson'">
                <I18nInput
                  v-model="record.i18nJson"
                  mode="simple"
                  :placeholder="i18nPlaceholder"
                />
              </template>
              <template v-else-if="column.key === 'headerStyleJson'">
                <a-textarea v-model:value="record.headerStyleJson" :rows="2" :placeholder="headerStylePlaceholder" />
              </template>
              <template v-else-if="column.key === 'cellStyleJson'">
                <a-textarea v-model:value="record.cellStyleJson" :rows="2" :placeholder="cellStylePlaceholder" />
              </template>
              <template v-else-if="column.key === 'orderNum'">
                <a-input-number v-model:value="record.orderNum" :min="0" style="width: 100px" />
              </template>
              <template v-else-if="column.key === 'action'">
                <a-space :size="4">
                  <a-button type="link" size="small" :disabled="index === 0" @click="moveItem(index, -1)">上移</a-button>
                  <a-button type="link" size="small" :disabled="index === editForm.items.length - 1" @click="moveItem(index, 1)">下移</a-button>
                  <a-button type="link" size="small" danger @click="removeItem(record._k)">删除</a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </a-tab-pane>
      </a-tabs>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal, type FormInstance } from 'ant-design-vue'
import { useDict } from '@/hooks/useDict'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { deleteExportConfig, exportConfigDetail, pageExportConfig, saveExportConfig } from '@/api/system/excel'

const { t } = useI18n()
const { dictItems: yesNoOptions } = useDict('yes_no')

const i18nPlaceholder = '{"zh-CN":"标题"}'
const headerStylePlaceholder = '{"fontWeight":"bold"}'
const cellStylePlaceholder = '{"align":"left"}'

const tableRef = ref()
const basicFormRef = ref<FormInstance>()
const editOpen = ref(false)
const activeTab = ref('basic')
const saving = ref(false)

const itemColumns = computed(() => [
  { title: t('system.excel.exportField'), key: 'exportField', width: 180, fixed: 'left' as const },
  { title: t('system.excel.fieldName'), key: 'fieldName', width: 180 },
  { title: t('system.excel.i18nJson'), key: 'i18nJson', width: 260 },
  { title: t('system.excel.headerStyleJson'), key: 'headerStyleJson', width: 220 },
  { title: t('system.excel.cellStyleJson'), key: 'cellStyleJson', width: 220 },
  { title: t('common.order'), key: 'orderNum', width: 100, align: 'center' as const },
  { title: t('common.action'), key: 'action', width: 180, align: 'center' as const, fixed: 'right' as const },
])

const dictOptions = computed(() => ({
  enabled: yesNoOptions.value,
}))

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ExcelExportConfigTable',
  tableName: t('system.excel.exportConfigTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'tableName', title: t('system.excel.tableName'), minWidth: 180, align: 'left' },
    { field: 'tableCode', title: t('system.excel.tableCode'), width: 180, align: 'left' },
    { field: 'title', title: t('system.excel.title'), minWidth: 160, align: 'left' },
    { field: 'subtitle', title: t('system.excel.subtitle'), minWidth: 160, align: 'left' },
    { field: 'exportFormat', title: t('system.excel.exportFormat'), width: 120, align: 'center' },
    { field: 'enabled', title: t('system.excel.enabled'), width: 120, align: 'center' },
    { field: 'enableTotal', title: t('system.excel.enableTotal'), width: 120, align: 'center' },
    { field: 'action', title: t('common.action'), width: 140, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'tableName', label: t('system.excel.tableName'), queryType: 'input', queryOperator: 'like' },
    { field: 'tableCode', label: t('system.excel.tableCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'enabled', label: t('system.excel.enabled'), queryType: 'select', queryOperator: 'eq', dictCode: 'enabled' },
  ],
  version: 1,
}))

const basicRules = {
  tableName: [{ required: true, message: t('system.excel.message.pleaseInputTableName'), trigger: 'blur' }],
  tableCode: [{ required: true, message: t('system.excel.message.pleaseInputTableCode'), trigger: 'blur' }],
  exportFormat: [{ required: true, message: t('system.excel.pleaseSelectExportFormat'), trigger: 'change' }],
}

const editForm = reactive<any>({
  id: undefined,
  tableName: '',
  tableCode: '',
  title: '',
  subtitle: '',
  exportFormat: 'xlsx',
  enabled: true,
  enableTotal: false,
  headerStyleJson: '',
  version: 1,
  items: [],
})

function createItem(item: any = {}, index = 0) {
  return {
    id: item.id,
    exportField: item.exportField || '',
    fieldName: item.fieldName || '',
    i18nJson: item.i18nJson || '',
    headerStyleJson: item.headerStyleJson || '',
    cellStyleJson: item.cellStyleJson || '',
    orderNum: item.orderNum ?? index,
    _k: `${item.id || 'new'}-${index}-${Date.now()}-${Math.random()}`,
  }
}

function resetEditForm() {
  editForm.id = undefined
  editForm.tableName = ''
  editForm.tableCode = ''
  editForm.title = ''
  editForm.subtitle = ''
  editForm.exportFormat = 'xlsx'
  editForm.enabled = true
  editForm.enableTotal = false
  editForm.headerStyleJson = ''
  editForm.version = 1
  editForm.items = []
  activeTab.value = 'basic'
}

function normalizeBoolean(value: unknown): boolean {
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value === 1
  if (typeof value === 'string') {
    const normalized = value.trim().toLowerCase()
    return normalized === '1' || normalized === 'true'
  }
  return false
}

function normalizeEnabledQuery(value: unknown): boolean | undefined {
  if (value === undefined || value === null || value === '') {
    return undefined
  }
  return normalizeBoolean(value)
}

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
}) => {
  try {
    const tableName = pickQueryValue(payload.query, ['tableName', 'table_name'])
    const tableCode = pickQueryValue(payload.query, ['tableCode', 'table_code'])
    const enabled = normalizeEnabledQuery(payload.query?.enabled)
    const res: any = await pageExportConfig({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName,
      tableCode,
      enabled,
    })
    return {
      records: (res.records || []).map((item: any) => ({
        ...item,
        enabled: normalizeBoolean(item.enabled),
        enableTotal: normalizeBoolean(item.enableTotal),
      })),
      total: res.total || 0,
    }
  } catch (error) {
    console.error('加载导出配置列表失败', error)
    return {
      records: [],
      total: 0,
    }
  }
}

async function openEdit(id?: number) {
  resetEditForm()

  if (id) {
    try {
      const detail: any = await exportConfigDetail({ id })
      editForm.id = detail?.id
      editForm.tableName = detail?.tableName || ''
      editForm.tableCode = detail?.tableCode || ''
      editForm.title = detail?.title || ''
      editForm.subtitle = detail?.subtitle || ''
      editForm.exportFormat = detail?.exportFormat || 'xlsx'
      editForm.enabled = detail?.enabled === undefined ? true : normalizeBoolean(detail.enabled)
      editForm.enableTotal = normalizeBoolean(detail?.enableTotal)
      editForm.headerStyleJson = detail?.headerStyleJson || ''
      editForm.version = detail?.version || 1
      editForm.items = (detail?.items || []).map((item: any, index: number) => createItem(item, index))
    } catch (error) {
      console.error('加载导出配置详情失败', error)
      message.error(t('common.loadFailed'))
      return
    }
  }

  editOpen.value = true
}

function addItem() {
  editForm.items.push(createItem({}, editForm.items.length))
}

function removeItem(key: string) {
  editForm.items = editForm.items.filter((item: any) => item._k !== key)
  resetItemOrder()
}

function moveItem(index: number, step: number) {
  const targetIndex = index + step
  if (targetIndex < 0 || targetIndex >= editForm.items.length) {
    return
  }
  const current = editForm.items[index]
  editForm.items[index] = editForm.items[targetIndex]
  editForm.items[targetIndex] = current
  resetItemOrder()
}

function resetItemOrder() {
  editForm.items = editForm.items.map((item: any, index: number) => ({
    ...item,
    orderNum: index,
  }))
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await deleteExportConfig({ id })
        message.success(t('common.deleted'))
        tableRef.value?.refresh?.()
      } catch (error) {
        console.error('删除导出配置失败', error)
        message.error(t('system.excel.message.deleteConfigFailed'))
      }
    },
  })
}

async function handleSave() {
  try {
    await basicFormRef.value?.validate()
    if (!editForm.items.length) {
      activeTab.value = 'fields'
      message.warning(t('system.excel.pleaseAddExportFields'))
      return
    }

    saving.value = true
    const payload = {
      id: editForm.id,
      tableName: editForm.tableName,
      tableCode: editForm.tableCode,
      title: editForm.title,
      subtitle: editForm.subtitle,
      exportFormat: editForm.exportFormat,
      enabled: editForm.enabled,
      enableTotal: editForm.enableTotal,
      headerStyleJson: editForm.headerStyleJson,
      version: editForm.version,
      items: editForm.items.map((item: any, index: number) => ({
        id: item.id,
        exportField: item.exportField,
        fieldName: item.fieldName,
        i18nJson: item.i18nJson,
        headerStyleJson: item.headerStyleJson,
        cellStyleJson: item.cellStyleJson,
        orderNum: item.orderNum ?? index,
      })),
    }
    await saveExportConfig(payload)
    message.success(t('common.saveSuccess'))
    editOpen.value = false
    tableRef.value?.refresh?.()
  } catch (error) {
    console.error('保存导出配置失败', error)
    message.error(t('system.excel.message.saveConfigFailed'))
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

.field-toolbar {
  margin-bottom: 12px;
}
</style>
