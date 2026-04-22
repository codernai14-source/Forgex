
<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="LabelBindingTable"
    >
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="handleAdd">
            <PlusOutlined /> 新增绑定
          </a-button>
          <a-button @click="handleMatchTemplate">
            <ThunderboltOutlined /> 智能匹配
          </a-button>
        </a-space>
      </template>

      <template #priority="{ record }">
        <a-tag :color="getPriorityColor(record.priority)">
          {{ getPriorityText(record.priority) }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-button type="link" size="small" danger @click="handleDelete(record)">删除</a-button>
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
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, ThunderboltOutlined } from '@ant-design/icons-vue'
import { labelBindingApi } from '@/api/label/binding'
import BindingFormDialog from './components/BindingFormDialog.vue'

const tableRef = ref()
const formVisible = ref(false)
const currentBinding = ref<any>(null)

function loadData(params: any) {
  return labelBindingApi.page(params)
}

function handleAdd() {
  console.log('新增绑定')
  currentBinding.value = null
  formVisible.value = true
}

function handleEdit(record: any) {
  console.log('编辑绑定:', record)
  currentBinding.value = record
  formVisible.value = true
}

function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定删除该绑定关系吗？（${record.bindingType}: ${record.bindingValue}）`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await labelBindingApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

async function handleMatchTemplate() {
  try {
    await labelBindingApi.matchTemplate({})
    message.success('智能匹配执行成功')
    tableRef.value?.reload()
  } catch (error) {
    message.error('智能匹配失败')
  }
}

function handleFormSuccess() {
  tableRef.value?.reload()
}

function getPriorityText(priority: number) {
  if (priority === 1) return '高'
  if (priority === 2) return '中'
  return '低'
}

function getPriorityColor(priority: number) {
  if (priority === 1) return 'red'
  if (priority === 2) return 'orange'
  return 'blue'
}
</script>

<style scoped lang="less">
.page-container {
  padding: 16px;
  height: 100%;
}
</style>
