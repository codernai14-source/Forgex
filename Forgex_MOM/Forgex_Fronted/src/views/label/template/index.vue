
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
          <PlusOutlined /> {{ t('label.template.addTemplate') }}
        </a-button>
        <a-button danger @click="handleBatchDelete" :disabled="!selectedRowKeys.length">
          <DeleteOutlined /> {{ t('common.batchDelete') }}
        </a-button>
      </template>

      <!-- 自定义列渲染 -->
      <template #isDefault="{ record }">
        <a-tag v-if="record.isDefault" color="green">{{ t('common.yes') }}</a-tag>
        <a-tag v-else color="default">{{ t('common.no') }}</a-tag>
      </template>

      <template #status="{ record }">
        <a-tag v-if="record.status === 1" color="green">{{ t('common.enabled') }}</a-tag>
        <a-tag v-else-if="record.status === 0" color="red">{{ t('common.disabled') }}</a-tag>
        <a-tag v-else color="default">{{ t('common.unknown') }}</a-tag>
      </template>

      <!-- 行操作 -->
      <template #action="{ record }">
        <a-space>
          <a-button type="link" size="small" @click="handleView(record)">{{ t('common.view') }}</a-button>
          <a-button type="link" size="small" @click="handleEdit(record)">{{ t('common.edit') }}</a-button>
          <a-dropdown>
            <a-button type="link" size="small">
              {{ t('common.more') }} <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu>
                <a-menu-item @click="handleSetDefault(record)">{{ t('label.template.setDefault') }}</a-menu-item>
                <a-menu-item @click="handleCopy(record)">{{ t('label.template.copyTemplate') }}</a-menu-item>
                <a-menu-divider />
                <a-menu-item danger @click="handleDelete(record)">{{ t('common.delete') }}</a-menu-item>
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
import { computed, ref } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PlusOutlined, DeleteOutlined, DownOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import { labelTemplateApi } from '@/api/label/template'
import TemplateFormDialog from './components/TemplateFormDialog.vue'
import TemplateDetailDrawer from './components/TemplateDetailDrawer.vue'const { t } = useI18n()
const tableRef = ref()
const formVisible = ref(false)
const detailVisible = ref(false)
const currentTemplate = ref<any>(null)
const selectedRowKeys = ref<number[]>([])

/**
 * 字典选项配置
 */
const dictOptions = computed(() => ({
  common_status: [
    { label: t('common.enabled'), value: 1, color: 'green' },
    { label: t('common.disabled'), value: 0, color: 'red' }
  ],
  template_type: [
    { label: t('label.templateTypes.PRODUCT'), value: 'PRODUCT', color: 'blue' },
    { label: t('label.templateTypes.MATERIAL'), value: 'MATERIAL', color: 'orange' },
    { label: t('label.templateTypes.BATCH'), value: 'BATCH', color: 'purple' }
  ]
}))

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
  currentTemplate.value = record
  detailVisible.value = true
}

// 删除
function handleDelete(record: any) {
  Modal.confirm({
    title: t('message.deleteConfirmTitle'),
    content: t('label.template.deleteConfirm', { name: record.templateName }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await labelTemplateApi.delete(record.id)
      tableRef.value?.reload()
    }
  })
}

// 批量删除
function handleBatchDelete() {
  Modal.confirm({
    title: t('message.deleteConfirmTitle'),
    content: t('label.template.batchDeleteConfirm', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
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
    title: t('label.template.setDefaultConfirmTitle'),
    content: t('label.template.setDefaultConfirm', { name: record.templateName }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await labelTemplateApi.setDefault(record.id, record.templateType)
      tableRef.value?.reload()
    }
  })
}

// 复制模板
function handleCopy(record: any) {
  // TODO: 实现复制模板逻辑
  message.info(t('label.template.copyPending'))
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

