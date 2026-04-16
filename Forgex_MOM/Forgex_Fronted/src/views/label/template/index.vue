
<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        :request="loadData"
        table-code="label_template_table"
        :row-selection="rowSelection"
    >
      <template #toolbar>
        <a-button type="primary" @click="handleAdd">
          <PlusOutlined /> 新增模板
        </a-button>
        <a-button danger @click="handleBatchDelete" :disabled="!selectedRowKeys.length">
          <DeleteOutlined /> 批量删除
        </a-button>
      </template>

      <!-- 自定义列渲染 -->
      <template #isDefault="{ record }">
        <a-tag v-if="record.isDefault" color="green">是</a-tag>
        <a-tag v-else color="default">否</a-tag>
      </template>

      <template #status="{ record }">
        <DictTag dict-code="common_status" :value="record.status" />
      </template>

      <!-- 行操作 -->
      <template #action="{ record }">
        <a-space>
          <a @click="handleView(record)">查看</a>
          <a @click="handleEdit(record)">编辑</a>
          <a-dropdown>
            <a class="ant-dropdown-link" @click.prevent>
              更多 <DownOutlined />
            </a>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleSetDefault(record)">设为默认</a-menu-item>
                <a-menu-item @click="handleCopy(record)">复制模板</a-menu-item>
                <a-menu-divider />
                <a-menu-item danger @click="handleDelete(record)">删除</a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </template>
    </FxDynamicTable>

    <!-- 新增/编辑弹窗 -->
    <TemplateFormDialog
        v-model:visible="formVisible"
        :template-data="currentTemplate"
        @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons-vue'
import { labelTemplateApi } from '@/api/label/template'
import TemplateFormDialog from './components/TemplateFormDialog.vue'

const tableRef = ref()
const formVisible = ref(false)
const currentTemplate = ref<any>(null)
const selectedRowKeys = ref<number[]>([])
const rowSelection = ref({
  selectedRowKeys: selectedRowKeys.value,
  onChange: (keys: number[]) => {
    selectedRowKeys.value = keys
    rowSelection.value.selectedRowKeys = keys
  }
})

// 加载数据
function loadData(params: any) {
  return labelTemplateApi.page(params)
}

// 新增
function handleAdd() {
  currentTemplate.value = null
  formVisible.value = true
}

// 编辑
function handleEdit(record: any) {
  currentTemplate.value = record
  formVisible.value = true
}

// 查看
function handleView(record: any) {
  // TODO: 跳转到详情页或打开详情弹窗
  message.info('查看功能待实现')
}

// 删除
function handleDelete(record: any) {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除模板"${record.templateName}"吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await labelTemplateApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  Modal.confirm({
    title: '确认批量删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个模板吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await labelTemplateApi.batchDelete(selectedRowKeys.value)
      selectedRowKeys.value = []
      tableRef.value?.reload()
    }
  })
}

// 设为默认
function handleSetDefault(record: any) {
  Modal.confirm({
    title: '确认设置',
    content: `确定要将"${record.templateName}"设为该类型的默认模板吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await labelTemplateApi.setDefault(record.id, record.templateType)
      tableRef.value?.reload()
    }
  })
}

// 复制模板
function handleCopy(record: any) {
  // TODO: 实现复制模板逻辑
  message.info('复制功能待实现')
}

// 表单提交成功
function handleFormSuccess() {
  selectedRowKeys.value = []
  rowSelection.value.selectedRowKeys = []
  tableRef.value?.reload()
}
</script>

<style scoped lang="less">
.page-container {
  padding: 16px;
  height: 100%;
}
</style>
