import { computed, ref } from 'vue'
import type { TableProps } from 'ant-design-vue'

export type BatchTableRowKey = string | number

export function useBatchTableSelection<T extends BatchTableRowKey = BatchTableRowKey>() {
  const selectedRowKeys = ref<T[]>([])

  const selectedCount = computed(() => selectedRowKeys.value.length)

  const rowSelection = computed<TableProps['rowSelection']>(() => ({
    selectedRowKeys: selectedRowKeys.value,
    onChange: (keys: BatchTableRowKey[]) => {
      selectedRowKeys.value = keys as T[]
    },
  }))

  function clearSelection() {
    selectedRowKeys.value = []
  }

  return {
    selectedRowKeys,
    selectedCount,
    rowSelection,
    clearSelection,
  }
}
