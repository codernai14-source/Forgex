
<template>
  <div class="page-container">
    <FxDynamicTable
        ref="tableRef"
        table-code="LabelTemplateTable"
        :request="loadData"
        :dict-options="dictOptions"
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
        <a-tag v-if="record.status === 1" color="green">启用</a-tag>
        <a-tag v-else-if="record.status === 0" color="red">禁用</a-tag>
        <a-tag v-else color="default">未知</a-tag>
      </template>

      <!-- 行操作 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleView(record)">查看</a-button>
          <a-button type="link" size="small" @click="handleEdit(record)">编辑</a-button>
          <a-dropdown>
            <a-button type="link" size="small">
              更多 <DownOutlined />
            </a-button>
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

    <!-- 查看详情抽屉 -->
    <TemplateDetailDrawer
        v-model:visible="detailVisible"
        :template-data="currentTemplate"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons-vue'
import { labelTemplateApi } from '@/api/label/template'
import TemplateFormDialog from './components/TemplateFormDialog.vue'
import TemplateDetailDrawer from './components/TemplateDetailDrawer.vue'

const tableRef = ref()
const formVisible = ref(false)
const detailVisible = ref(false)
const currentTemplate = ref<any>(null)
const selectedRowKeys = ref<number[]>([])

/**
 * 字典选项配置
 */
const dictOptions = ref({
  common_status: [
    { label: '启用', value: 1, color: 'green' },
    { label: '禁用', value: 0, color: 'red' }
  ],
  template_type: [
    { label: '产品标签', value: 'PRODUCT', color: 'blue' },
    { label: '物料标签', value: 'MATERIAL', color: 'orange' },
    { label: '批次标签', value: 'BATCH', color: 'purple' }
  ]
})

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
  console.log('新增模板')
  currentTemplate.value = null
  formVisible.value = true
}

// 编辑
function handleEdit(record: any) {
  console.log('编辑模板:', record)
  currentTemplate.value = record
  formVisible.value = true
}

// 查看
function handleView(record: any) {
  console.log('查看模板:', record)
  currentTemplate.value = record
  detailVisible.value = true
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
