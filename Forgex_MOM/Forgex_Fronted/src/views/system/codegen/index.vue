<template>
  <div class="codegen-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="CodegenConfigTable"
      :request="handleRequest"
      :show-query-form="true"
      :show-column-setting="false"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button data-guide-id="sys-codegen-add" type="primary" @click="openAddDrawer">
            {{ t('system.codegen.add') }}
          </a-button>
        </a-space>
      </template>

      <template #pageType="{ record }">
        <a-tag :color="pageTypeColorMap[record.pageType] || 'default'">
          {{ pageTypeLabel(record.pageType) }}
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
import { ref } from 'vue'
import { Modal, message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import CodegenConfigDrawer from './components/CodegenConfigDrawer.vue'
import CodegenPreviewModal from './components/CodegenPreviewModal.vue'
import { deleteCodegenConfig, getCodegenConfigPage } from '@/api/system/codegenConfig'
import { downloadCodegenZipByConfigId, previewCodegenByConfigId, type CodegenPreviewFile } from '@/api/system/codegen'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref()
const drawerVisible = ref(false)
const previewVisible = ref(false)
const currentId = ref<number>()
const previewTitle = ref('')
const previewFiles = ref<CodegenPreviewFile[]>([])
const pageTypeColorMap: Record<string, string> = {
  SINGLE: 'green',
  MASTER_DETAIL: 'blue',
  TREE_SINGLE: 'gold',
  TREE_DOUBLE: 'cyan',
}

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

function pageTypeLabel(pageType?: string) {
  switch (pageType) {
    case 'MASTER_DETAIL':
      return t('system.codegen.pageTypeMasterDetail')
    case 'TREE_SINGLE':
      return t('system.codegen.pageTypeTreeSingle')
    case 'TREE_DOUBLE':
      return t('system.codegen.pageTypeTreeDouble')
    default:
      return t('system.codegen.pageTypeSingle')
  }
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
