<template>
  <div class="module-container">
    <fx-dynamic-table
      ref="tableRef"
      table-code="ModuleTable"
      :show-query-form="true"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button
            data-guide-id="sys-module-add"
            v-permission="'sys:module:add'"
            type="primary"
            @click="openAddDialog"
          >
            {{ $t('system.module.addModule') }}
          </a-button>
          <a-button
            data-guide-id="sys-module-batch-delete"
            v-permission="'sys:module:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDeleteConfirm"
          >
            {{ $t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #status="{ record }">
        <a-tag
          v-if="resolveStatusTag(record.status)"
          :color="resolveStatusTag(record.status)?.color"
          :style="resolveStatusTag(record.status)?.style"
        >
          {{ resolveStatusTag(record.status)?.label }}
        </a-tag>
        <span v-else>{{ record.status ?? '-' }}</span>
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
            style="color: #ff4d4f"
            @click="handleDeleteConfirm(record.id)"
          >
            {{ $t('common.delete') }}
          </a>
        </a-space>
      </template>
    </fx-dynamic-table>

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
            <span style="color: #999; font-size: 12px">
              点击输入框右侧的地球图标可配置多语言
            </span>
          </template>
        </a-form-item>

        <a-form-item label="图标" name="icon">
          <IconPicker
            v-model:value="formData.icon"
            placeholder="请选择或输入图标名称"
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
import { computed, onMounted, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import IconPicker from '@/components/common/IconPicker.vue'
import I18nInput from '@/components/common/I18nInput.vue'
import { getModulePage } from '@/api/system/module'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'
import { useModule } from './hooks/useModule'
import { useModuleForm } from './hooks/useModuleForm'

const { dictItems: statusOptions } = useDict('status')
const { dictItems: visibleOptions } = useDict('visible')

const tableRef = ref()

const {
  selectedRowKeys,
  handleDelete,
  handleBatchDelete,
  handleSelectionChange,
} = useModule()

const {
  formRef,
  dialogVisible,
  dialogTitle,
  loading,
  isEdit,
  formData,
  rules,
  openAddDialog,
  openEditDialog,
  handleSubmit,
  handleCancel,
} = useModuleForm()

const dictOptions = computed(() => ({
  status: statusOptions.value,
  visible: visibleOptions.value,
}))

const dynamicTableConfig = computed(() => ({
  tableCode: 'ModuleTable',
  tableName: '模块管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'name', title: '模块名称', minWidth: 160, ellipsis: true },
    { field: 'code', title: '模块编码', width: 140 },
    { field: 'description', title: '描述', minWidth: 160, ellipsis: true },
    { field: 'status', title: '状态', width: 90, dictCode: 'status' },
    { field: 'createBy', title: '创建人', width: 120 },
    { field: 'createTime', title: '创建时间', width: 180 },
    { field: 'updateTime', title: '更新时间', width: 180 },
    { field: 'action', title: '操作', width: 140 },
  ],
  queryFields: [
    { field: 'name', label: '模块名称', queryType: 'input', queryOperator: 'like' },
    { field: 'code', label: '模块编码', queryType: 'input', queryOperator: 'like' },
    { field: 'description', label: '描述', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' },
  ],
  version: 1,
}))

function normalizeModuleStatusRecord(row: any) {
  const status = row?.status
  let normalizedStatus = 0
  if (typeof status === 'boolean') {
    normalizedStatus = status ? 1 : 0
  } else if (status === 1 || status === '1') {
    normalizedStatus = 1
  }

  const visible = row?.visible
  let normalizedVisible = 0
  if (typeof visible === 'boolean') {
    normalizedVisible = visible ? 1 : 0
  } else if (visible === 1 || visible === '1') {
    normalizedVisible = 1
  }

  return {
    ...row,
    status: normalizedStatus,
    visible: normalizedVisible,
  }
}

function resolveStatusTag(value: unknown) {
  const normalizedValue = value === true || value === 1 || value === '1' ? 1 : 0
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }

  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  try {
    const params: any = {
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      ...payload.query,
    }

    if (payload.sorter) {
      params.sortField = payload.sorter.field
      params.sortOrder = payload.sorter.order
    }

    const data = await getModulePage(params)
    const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)

    const processedRecords = (data.records || []).map((item: any) => ({
      ...normalizeModuleStatusRecord(item),
      displayName: getI18nValue(item.nameI18nJson, item.name),
    }))

    return { records: processedRecords, total }
  } catch (error) {
    console.error('加载模块列表失败:', error)
    return { records: [], total: 0 }
  }
}

async function handleFormSubmit() {
  const success = await handleSubmit()
  if (success) {
    await tableRef.value?.refresh?.()
  }
}

function handleDeleteConfirm(id: string) {
  Modal.confirm({
    title: '确认删除',
    content: '确定要删除该模块吗？',
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await handleDelete(id)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDeleteConfirm() {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除选中的 ${selectedRowKeys.value.length} 个模块吗？`,
    okText: '确定',
    cancelText: '取消',
    onOk: async () => {
      await handleBatchDelete()
      await tableRef.value?.refresh?.()
    },
  })
}

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
}
</style>
