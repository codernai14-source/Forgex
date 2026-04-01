<template>
  <div class="module-container">
    <!-- 表格 -->
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'ModuleTable'"
      :show-query-form="true"
      :request="handleRequest"
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
              {{ $t('system.module.addModule') }}
            </a-button>
            <a-button
              v-permission="'sys:module:delete'"
              danger
              :disabled="selectedRowKeys.length === 0"
              @click="handleBatchDeleteConfirm"
            >
              {{ $t('common.batchDelete') }}
            </a-button>
          </a-space>
        </template>

        <template #action="{ record }">
          <a-space>
            <a
              v-permission="'sys:module:edit'"
              @click="openEditDialog(record.id)"
            >
              {{ $t('common.edit') }}
            </a>
            <a
              v-permission="'sys:module:delete'"
              style="color: #ff4d4f;"
              @click="handleDeleteConfirm(record.id)"
            >
              {{ $t('common.delete') }}
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

        <a-form-item label="模块名称" name="nameI18nJson">
          <I18nInput
            v-model="formData.nameI18nJson"
            mode="simple"
            placeholder="请输入模块名称"
          />
          <template #extra>
            <span style="color: #999; font-size: 12px;">
              点击输入框右侧的地球图标可配置多语言
            </span>
          </template>
        </a-form-item>

        <a-form-item label="图标" name="icon">
          <IconPicker v-model:value="formData.icon" placeholder="请选择或输入图标名称" :maxlength="100" />
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

        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
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
import IconPicker from '@/components/common/IconPicker.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { useModule } from './hooks/useModule'
import { useModuleForm } from './hooks/useModuleForm'
import { listModules, getModulePage, deleteModule, batchDeleteModules } from '@/api/system/module'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'

const { dictItems: statusOptions } = useDict('status')
const { dictItems: visibleOptions } = useDict('visible')

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

// 字典配置
const dictOptions = ref({
  status: statusOptions,
  visible: visibleOptions
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
    
    // 处理多语言显示
    const processedRecords = (data.records || []).map((item: any) => ({
      ...item,
      displayName: getI18nValue(item.nameI18nJson, item.name)
    }))
    
    return { records: processedRecords, total: total }
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
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;
}

.module-container :deep(.fx-dynamic-table) {
  flex: 1 1 auto;
  min-height: 0;
}
</style>
