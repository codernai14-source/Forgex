<template>
  <div class="codegen-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="CodegenConfigTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :show-query-form="true"
      :show-column-setting="false"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button type="primary" @click="openAddDrawer">
            {{ t('system.codegen.add') }}
          </a-button>
        </a-space>
      </template>

      <template #pageType="{ record }">
        <a-tag :color="record.pageType === 'MASTER_DETAIL' ? 'blue' : 'green'">
          {{ record.pageType === 'MASTER_DETAIL' ? t('system.codegen.pageTypeMasterDetail') : t('system.codegen.pageTypeSingle') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="openEditDrawer(record.id)">{{ t('common.edit') }}</a>
          <a @click="handleGenerate(record.id)">{{ t('system.codegen.generate') }}</a>
          <a @click="handleDownload(record.id)">{{ t('system.codegen.downloadZip') }}</a>
          <a style="color: #ff4d4f" @click="handleDelete(record.id)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <CodegenConfigDrawer
      v-model:open="drawerVisible"
      :config-id="currentId"
      @success="handleDrawerSuccess"
    />

    <CodegenPreviewModal
      v-model:open="previewVisible"
      :title="previewTitle"
      :files="previewFiles"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import CodegenConfigDrawer from './components/CodegenConfigDrawer.vue'
import CodegenPreviewModal from './components/CodegenPreviewModal.vue'
import { deleteCodegenConfig, getCodegenConfigPage } from '@/api/system/codegenConfig'
import { downloadCodegenZipByConfigId, previewCodegenByConfigId, type CodegenPreviewFile } from '@/api/system/codegen'
import type { FxTableConfig } from '@/api/system/tableConfig'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref()
const drawerVisible = ref(false)
const previewVisible = ref(false)
const currentId = ref<number>()
const previewTitle = ref('')
const previewFiles = ref<CodegenPreviewFile[]>([])

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'CodegenConfigTable',
  tableName: t('system.codegen.listTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'configName', title: t('system.codegen.columns.configName'), width: 180, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'pageType', title: t('system.codegen.columns.pageType'), width: 120, align: 'center', queryable: true, queryType: 'select', queryOperator: 'eq' },
    { field: 'moduleName', title: t('system.codegen.columns.moduleName'), width: 120, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'bizName', title: t('system.codegen.columns.bizName'), width: 140, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'entityName', title: t('system.codegen.columns.entityName'), width: 140 },
    { field: 'mainTableName', title: t('system.codegen.columns.mainTableName'), width: 160, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'datasourceName', title: t('system.codegen.columns.datasourceName'), width: 160 },
    { field: 'author', title: t('system.codegen.columns.author'), width: 120 },
    { field: 'updateTime', title: t('system.codegen.columns.updateTime'), width: 180, align: 'center' },
    { field: 'lastGenerateTime', title: t('system.codegen.columns.lastGenerateTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 240, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'configName', label: t('system.codegen.columns.configName'), queryType: 'input', queryOperator: 'like' },
    { field: 'moduleName', label: t('system.codegen.columns.moduleName'), queryType: 'input', queryOperator: 'like' },
    { field: 'bizName', label: t('system.codegen.columns.bizName'), queryType: 'input', queryOperator: 'like' },
    { field: 'mainTableName', label: t('system.codegen.columns.mainTableName'), queryType: 'input', queryOperator: 'like' },
  ],
  version: 1,
}))

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const result = await getCodegenConfigPage({
    ...payload.query,
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
  })
  return {
    records: result.records || [],
    total: result.total || 0,
  }
}

function openAddDrawer() {
  currentId.value = undefined
  drawerVisible.value = true
}

function openEditDrawer(id?: number) {
  currentId.value = id
  drawerVisible.value = true
}

async function handleGenerate(id?: number) {
  if (!id) {
    return
  }
  const result = await previewCodegenByConfigId(id)
  previewTitle.value = t('system.codegen.generate')
  previewFiles.value = result.files || []
  previewVisible.value = true
  message.success(t('system.codegen.generateSuccess'))
  tableRef.value?.refresh?.()
}

async function handleDownload(id?: number) {
  if (!id) {
    return
  }
  const { blob, fileName } = await downloadCodegenZipByConfigId(id)
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  link.click()
  URL.revokeObjectURL(url)
  tableRef.value?.refresh?.()
}

function handleDelete(id?: number) {
  if (!id) {
    return
  }
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('system.codegen.deleteConfirm'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteCodegenConfig(id)
      tableRef.value?.refresh?.()
    },
  })
}

function handleDrawerSuccess() {
  tableRef.value?.refresh?.()
}
</script>

<style scoped lang="less">
.codegen-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
</style>
