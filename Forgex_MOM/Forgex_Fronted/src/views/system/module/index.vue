<template>
  <div class="module-container">
    <!-- 表格 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'ModuleTable'"
      :show-query-form="true"
      :request="handleRequest"
      :fallback-config="fallbackConfig"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
        <!-- 工具栏插槽 -->
        <template #toolbar>
          <a-space :size="8">
            <a-button
              v-permission="'sys:module:add'"
              type="primary"
              @click="openAddDialog"
            >
              新增
            </a-button>
            <a-button
              v-permission="'sys:module:delete'"
              danger
              :disabled="selectedRowKeys.length === 0"
              @click="handleBatchDeleteConfirm"
            >
              批量删除
            </a-button>
          </a-space>
        </template>

        <template #action="{ record }">
          <a-space>
            <a
              v-permission="'sys:module:edit'"
              @click="openEditDialog(record.id)"
            >
              编辑
            </a>
            <a
              v-permission="'sys:module:delete'"
              style="color: #ff4d4f;"
              @click="handleDeleteConfirm(record.id)"
            >
              删除
            </a>
          </a-space>
        </template>
      </fx-dynamic-table>

    <!-- 新增/编辑对话框 -->
    <BaseFormDialog
      v-model:open="dialogVisible"
      :title="dialogTitle"
      :loading="loading"
      @submit="handleFormSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="模块编码" name="code">
          <a-input
            v-model:value="formData.code"
            placeholder="请输入模块编码"
            :disabled="isEdit"
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="模块名称" name="name">
          <a-input
            v-model:value="formData.name"
            placeholder="请输入模块名称"
            :maxlength="50"
          />
        </a-form-item>

        <a-form-item label="图标" name="icon">
          <a-input
            v-model:value="formData.icon"
            placeholder="请输入图标名称"
            :maxlength="100"
          />
        </a-form-item>

        <a-form-item label="排序号" name="orderNum">
          <a-input-number
            v-model:value="formData.orderNum"
            placeholder="请输入排序号"
            :min="0"
            style="width: 100%"
          />
        </a-form-item>

        <a-form-item label="是否可见" name="visible">
          <a-radio-group v-model:value="formData.visible">
            <a-radio :value="1">显示</a-radio>
            <a-radio :value="0">隐藏</a-radio>
          </a-radio-group>
        </a-form-item>

        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="1">启用</a-radio>
            <a-radio :value="0">禁用</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </BaseFormDialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { useModule } from './hooks/useModule'
import { useModuleForm } from './hooks/useModuleForm'
import { listModules, getModulePage, deleteModule, batchDeleteModules } from '@/api/system/module'

// 表格相关
const tableRef = ref()

// 使用Hooks
const {
  selectedRowKeys,
  handleDelete,
  handleBatchDelete,
  handleSelectionChange
} = useModule()

const {
  formRef,
  dialogVisible,
  dialogTitle,
  isEdit,
  formData,
  rules,
  openAddDialog,
  openEditDialog,
  handleSubmit,
  handleCancel
} = useModuleForm()

// fallback配置
const fallbackConfig = ref({
  tableCode: 'ModuleTable',
  tableName: '模块管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'id', title: 'ID', width: 80, align: 'center' },
    { field: 'code', title: '模块编码', width: 150 },
    { field: 'name', title: '模块名称', width: 150 },
    { field: 'icon', title: '图标', width: 80 },
    { field: 'orderNum', title: '排序号', width: 100 },
    { field: 'visible', title: '可见性', width: 100 },
    { field: 'status', title: '状态', width: 100 },
    { field: 'createTime', title: '创建时间', width: 180 },
    { field: 'updateTime', title: '修改时间', width: 180 },
    { field: 'action', title: '操作', width: 150, fixed: 'right' }
  ],
  queryFields: [
    { field: 'code', label: '模块编码', queryType: 'input', queryOperator: 'like' },
    { field: 'name', label: '模块名称', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq' }
  ],
  version: 1
})

// 字典配置
const dictOptions = ref({
  status: {
    1: { text: '启用', color: 'success' },
    0: { text: '禁用', color: 'error' }
  },
  visible: {
    1: { text: '显示', color: 'success' },
    0: { text: '隐藏', color: 'default' }
  }
})

/**
 * 处理表格数据请求
 */
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query
    }
    
    // 处理排序
    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }
    
    const data = await getModulePage(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
    return { records: data.records || [], total: total }
  } catch (error) {
    console.error('加载模块列表失败:', error)
    return { records: [], total: 0 }
  }
}

/**
 * 表单提交
 */
const handleFormSubmit = async () => {
  const success = await handleSubmit()
  if (success) {
    await tableRef.value?.refresh?.()
  }
}

/**
 * 删除确认
 */
const handleDeleteConfirm = (id: string) => {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该模块吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await handleDelete(id)
      await tableRef.value?.refresh?.()
    }
  })
}

/**
 * 批量删除确认
 */
const handleBatchDeleteConfirm = () => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个模块吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await handleBatchDelete()
      await tableRef.value?.refresh?.()
    }
  })
}

// 初始化加载数据
onMounted(() => {
  tableRef.value?.refresh?.()
})
</script>

<style scoped lang="less">
.module-container {
  padding: 16px;
}
</style>
