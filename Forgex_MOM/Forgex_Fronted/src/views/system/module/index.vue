<template>
  <div class="module-container">
    <!-- 搜索栏 -->
    <a-card :bordered="false" style="margin-bottom: 16px">
      <a-form layout="inline">
        <a-form-item label="模块编码">
          <a-input
            v-model:value="queryParams.code"
            placeholder="请输入模块编码"
            allow-clear
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="模块名称">
          <a-input
            v-model:value="queryParams.name"
            placeholder="请输入模块名称"
            allow-clear
            style="width: 200px"
            @press-enter="handleSearch"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="queryParams.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <SearchOutlined /> 搜索
            </a-button>
            <a-button @click="handleReset">
              <ReloadOutlined /> 重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 表格 -->
    <a-card :bordered="false">
      <!-- 操作按钮 -->
      <div style="margin-bottom: 16px">
        <a-space>
          <a-button
            v-permission="'sys:module:add'"
            type="primary"
            @click="openAddDialog"
          >
            <PlusOutlined /> 新增
          </a-button>
          <a-button
            v-permission="'sys:module:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDeleteConfirm"
          >
            <DeleteOutlined /> 批量删除
          </a-button>
        </a-space>
      </div>

      <!-- 数据表格 -->
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'ModuleTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :loading="loading"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: handleSelectionChange
        }"
        row-key="id"
      >
        <template #icon="{ record }">
          <component :is="record.icon" v-if="record.icon" />
          <span v-else>-</span>
        </template>

        <template #visible="{ record }">
          <a-tag :color="record.visible === 1 ? 'success' : 'default'">
            {{ record.visible === 1 ? '显示' : '隐藏' }}
          </a-tag>
        </template>

        <template #status="{ record }">
          <a-tag :color="record.status === 1 ? 'success' : 'error'">
            {{ record.status === 1 ? '启用' : '禁用' }}
          </a-tag>
        </template>

        <template #action="{ record }">
          <a-space>
            <a
              v-permission="'sys:module:edit'"
              @click="openEditDialog(record.id)"
            >
              编辑
            </a>
            <a-divider type="vertical" />
            <a
              v-permission="'sys:module:delete'"
              style="color: #ff4d4f"
              @click="handleDeleteConfirm(record.id)"
            >
              删除
            </a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

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
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { useModule } from './hooks/useModule'
import { useModuleForm } from './hooks/useModuleForm'
import { listModules, deleteModule, batchDeleteModules } from '@/api/system/module'

// 表格相关
const tableRef = ref()

// 使用Hooks
const {
  loading,
  selectedRowKeys,
  queryParams,
  handleSearch,
  handleReset,
  handleDelete,
  handleBatchDelete,
  handleSelectionChange
} = useModule()

const {
  formRef,
  dialogVisible,
  dialogTitle,
  loading: formLoading,
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
  columns: [
    { title: '模块编码', dataIndex: 'code', key: 'code', width: 150 },
    { title: '模块名称', dataIndex: 'name', key: 'name', width: 150 },
    { title: '图标', dataIndex: 'icon', key: 'icon', width: 80 },
    { title: '排序号', dataIndex: 'orderNum', key: 'orderNum', width: 100 },
    { title: '可见性', dataIndex: 'visible', key: 'visible', width: 100 },
    { title: '状态', dataIndex: 'status', key: 'status', width: 100 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '创建人', dataIndex: 'createBy', key: 'createBy', width: 120 },
    { title: '修改时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
    { title: '修改人', dataIndex: 'updateBy', key: 'updateBy', width: 120 },
    { title: '操作', key: 'action', width: 150, fixed: 'right' }
  ]
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
const handleRequest = async (params: any) => {
  try {
    const res = await listModules({ ...queryParams, ...params })
    return {
      success: true,
      data: res.records,
      total: res.total
    }
  } catch (error) {
    return {
      success: false,
      data: [],
      total: 0
    }
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
