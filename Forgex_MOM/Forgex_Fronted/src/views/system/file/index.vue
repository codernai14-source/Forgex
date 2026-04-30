<template>
  <div class="file-management">
    <FxDynamicTable
      ref="tableRef"
      table-code="SysFileRecordTable"
      :request="handleRequest"
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
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { getFileRecordPage, type SysFileRecordItem } from '@/api/system/file'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref()

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
