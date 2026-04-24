<template>
  <div class="file-management">
    <FxDynamicTable
      ref="tableRef"
      table-code="SysFileRecordTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :show-query-form="true"
      :show-column-setting="false"
    >
      <template #accessUrl="{ record }">
        <a
          v-if="record.accessUrl"
          :href="record.accessUrl"
          target="_blank"
          rel="noopener noreferrer"
          class="file-link"
        >
          {{ record.accessUrl }}
        </a>
        <span v-else>-</span>
      </template>

      <template #fileSize="{ record }">
        {{ formatFileSize(record.fileSize) }}
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="handleCopy(record.accessUrl)">{{ t('system.file.copyUrl') }}</a>
          <a
            v-if="record.accessUrl"
            :href="record.accessUrl"
            target="_blank"
            rel="noopener noreferrer"
          >
            {{ t('system.file.openFile') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getFileRecordPage, type SysFileRecordItem } from '@/api/system/file'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref()

const dynamicTableConfig = computed(() => ({
  tableCode: 'SysFileRecordTable',
  tableName: t('system.file.title'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'moduleName', title: t('system.file.moduleName'), width: 150, align: 'left', queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'moduleCode', title: t('system.file.moduleCode'), width: 150, align: 'left', queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'originalName', title: t('system.file.fileName'), minWidth: 220, align: 'left', ellipsis: true, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'fileType', title: t('system.file.fileType'), width: 120, align: 'center', queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'accessUrl', title: t('system.file.fileUrl'), minWidth: 320, align: 'left', ellipsis: true },
    { field: 'fileSize', title: t('system.file.fileSize'), width: 120, align: 'right' },
    { field: 'createBy', title: t('system.file.uploadBy'), width: 120, align: 'left' },
    { field: 'createTime', title: t('system.file.uploadTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 150, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'moduleCode', label: t('system.file.moduleCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'moduleName', label: t('system.file.moduleName'), queryType: 'input', queryOperator: 'like' },
    { field: 'originalName', label: t('system.file.fileName'), queryType: 'input', queryOperator: 'like' },
    { field: 'fileType', label: t('system.file.fileType'), queryType: 'input', queryOperator: 'like' },
  ],
  version: 1,
}))

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const query = payload.query || {}
  const result = await getFileRecordPage({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    moduleCode: query.moduleCode,
    moduleName: query.moduleName,
    originalName: query.originalName,
    fileType: query.fileType,
  })

  return {
    records: result.records || [],
    total: result.total || 0,
  }
}

function formatFileSize(size?: number) {
  if (!size || size <= 0) {
    return '0 B'
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  let value = size
  let unitIndex = 0
  while (value >= 1024 && unitIndex < units.length - 1) {
    value /= 1024
    unitIndex += 1
  }
  return `${value >= 100 || unitIndex === 0 ? value.toFixed(0) : value.toFixed(2)} ${units[unitIndex]}`
}

async function handleCopy(url?: string) {
  if (!url) {
    message.warning(t('system.file.emptyUrl'))
    return
  }
  await navigator.clipboard.writeText(url)
  message.success(t('system.file.copied'))
}

defineExpose({
  refresh: () => tableRef.value?.refresh?.(),
})
</script>

<style scoped lang="less">
.file-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.file-link {
  word-break: break-all;
}
</style>
