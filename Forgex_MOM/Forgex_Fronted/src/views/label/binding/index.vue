
<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="LabelBindingTable"
        row-key="id"
        :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> {{ t('label.binding.addBinding') }}
          </a-button>
          <a-button @click="handleMatchTemplate">
            <ThunderboltOutlined /> {{ t('label.binding.smartMatch') }}
          </a-button>
          <a-button v-permission="'label:binding:batchDelete'" danger :disabled="!selectedCount" @click="handleBatchDelete">{{ t('common.batchDelete') }}</a-button>
        </a-space>
      </template>

      <template #priority="{ record }">
        <a-tag :color="getPriorityColor(record.priority)">
          {{ getPriorityText(record.priority) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleEdit(record)">{{ t('common.edit') }}</a-button>
          <a-button type="link" size="small" danger @click="handleDelete(record)">{{ t('common.delete') }}</a-button>
        </a-space>
      </template>
    </FxDynamicTable>

    <BindingFormDialog
        v-model:visible="formVisible"
        :binding-data="currentBinding"
        @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { labelBindingApi } from '@/api/label/binding'
import { useBatchTableSelection } from '@/hooks/useBatchTableSelection'
import BindingFormDialog from './components/BindingFormDialog.vue'

const { t } = useI18n()
const tableRef = ref()
const { selectedRowKeys, selectedCount, rowSelection, clearSelection } = useBatchTableSelection<number>()
const formVisible = ref(false)
const currentBinding = ref<any>(null)

function loadData(params: any) {
  return labelBindingApi.page(params)
}

function handleAdd() {
  currentBinding.value = null
  formVisible.value = true
}

function handleEdit(record: any) {
  currentBinding.value = record
  formVisible.value = true
}

function handleDelete(record: any) {
  Modal.confirm({
    title: t('message.deleteConfirmTitle'),
    content: t('label.binding.deleteConfirm', { type: record.bindingType, value: record.bindingValue }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await labelBindingApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

function handleBatchDelete() {
  if (!selectedCount.value) return
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedCount.value }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await labelBindingApi.batchDelete(selectedRowKeys.value)
      clearSelection()
      tableRef.value?.reload()
    }
  })
}

async function handleMatchTemplate() {
  try {
    await labelBindingApi.matchTemplate({})
    message.success(t('label.binding.smartMatchSuccess'))
    tableRef.value?.reload()
  } catch (error) {
    message.error(t('label.binding.smartMatchFailed'))
  }
}

function handleFormSuccess() {
  tableRef.value?.reload()
}

function getPriorityText(priority: number) {
  if (priority === 1) return t('label.binding.priorityHigh')
  if (priority === 2) return t('label.binding.priorityMedium')
  return t('label.binding.priorityLow')
}

function getPriorityColor(priority: number) {
  if (priority === 1) return 'red'
  if (priority === 2) return 'orange'
  return 'blue'
}
</script>

<style scoped lang="less" src="@/styles/views/label/binding/index.less"></style>


