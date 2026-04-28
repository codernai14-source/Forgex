<template>
  <div class="table-config-management">
    <FxDynamicTable
      ref="tableRef"
      table-code="TableConfigTable"
      :request="handleRequest"
      :dict-options="dictOptions"
      :show-query-form="true"
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange
      }"
      :row-key="rowKey"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-radio-group v-model:value="publicConfig" button-style="solid" @change="handleModeChange">
            <a-radio-button :value="false">租户配置</a-radio-button>
            <a-radio-button :value="true">公共配置</a-radio-button>
          </a-radio-group>
          <a-button
            v-if="!publicConfig"
            v-permission="'sys:tableConfig:add'"
            @click="handlePullPublic"
          >
            拉取公共配置
          </a-button>
          <a-button
            v-permission="'sys:tableConfig:add'"
            type="primary"
            @click="openAddDialog"
          >
            {{ t('system.tableConfig.add') }}
          </a-button>
          <a-button
            v-permission="'sys:tableConfig:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #tableNameI18nJson="{ record }">
        <span>{{ getI18nValue(record.tableNameI18nJson, record.tableName) }}</span>
      </template>

      <template #tableType="{ record }">
        <a-tag v-if="record.tableType === 'NORMAL'" color="blue">{{ t('system.tableConfig.tableTypeNormal') }}</a-tag>
        <a-tag v-else-if="record.tableType === 'LAZY'" color="green">{{ t('system.tableConfig.tableTypeLazy') }}</a-tag>
        <a-tag v-else color="orange">{{ t('system.tableConfig.tableTypeTree') }}</a-tag>
      </template>

      <template #enabled="{ record }">
        <a-switch
          v-permission="'sys:tableConfig:edit'"
          :checked="record.enabled"
          :loading="record.statusLoading"
          @change="(checked: boolean) => handleToggleStatus(record.id!, checked)"
        />
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'sys:tableConfig:edit'" @click="openEditDialog(record)">{{ t('common.edit') }}</a>
          <a v-permission="'sys:tableConfig:delete'" style="color: #ff4d4f" @click="handleDelete(record.id!)">
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>

    <TableConfigFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :config-id="currentConfigId"
      :public-config="publicConfig"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import TableConfigFormDialog from './components/TableConfigFormDialog.vue'
import {
  batchDeleteTableConfig,
  deleteTableConfig,
  getTableConfigList,
  pullPublicTableConfig,
  toggleTableConfigStatus,
  type TableConfigItem,
} from '@/api/system/tableConfig'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'

const { t } = useI18n({ useScope: 'global' })
const { dictItems: yesNoOptions } = useDict('yes_no')

const tableRef = ref()
const publicConfig = ref(false)
const selectedRowKeys = ref<number[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentConfigId = ref<number | undefined>()

const dictOptions = computed(() => ({
  enabled: yesNoOptions.value,
  tableType: [
    { label: t('system.tableConfig.tableTypeNormal'), value: 'NORMAL' },
    { label: t('system.tableConfig.tableTypeLazy'), value: 'LAZY' },
    { label: t('system.tableConfig.tableTypeTree'), value: 'TREE' },
  ],
}))

const rowKey = (record: TableConfigItem) => record.id ?? record.tableCode

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) => {
  const query = payload.query || {}
  const result = await getTableConfigList({
    tableCode: query.tableCode,
    tableName: query.tableName,
    tableType: query.tableType,
    enabled: query.enabled,
    isPublicConfig: publicConfig.value,
    current: payload.page.current,
    pageSize: payload.page.pageSize,
  })

  return {
    records: (result.records || []).map((item) => ({ ...item })),
    total: result.total || 0,
  }
}

function handleSelectionChange(keys: number[]) {
  selectedRowKeys.value = keys
}

function handleModeChange() {
  selectedRowKeys.value = []
  tableRef.value?.refresh?.()
}

async function handlePullPublic() {
  try {
    const count = await pullPublicTableConfig()
    message.success(`已拉取 ${Number(count || 0)} 条公共配置`)
    tableRef.value?.refresh?.()
  } catch (error) {
    console.error('pull public table config failed:', error)
  }
}

function openAddDialog() {
  isEdit.value = false
  currentConfigId.value = undefined
  dialogVisible.value = true
}

function openEditDialog(record: TableConfigItem) {
  if (!record.id) {
    message.error('当前记录缺少配置ID，无法加载编辑详情，请先刷新列表或确认后端列表接口已返回id')
    return
  }
  isEdit.value = true
  currentConfigId.value = record.id
  dialogVisible.value = true
}

function handleFormSuccess() {
  dialogVisible.value = false
  tableRef.value?.refresh?.()
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('common.confirmDeleteMessage'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteTableConfig(id, publicConfig.value)
      tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDelete() {
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await batchDeleteTableConfig(selectedRowKeys.value, publicConfig.value)
      selectedRowKeys.value = []
      tableRef.value?.refresh?.()
    },
  })
}

async function handleToggleStatus(id: number, enabled: boolean) {
  try {
    await toggleTableConfigStatus(id, enabled, publicConfig.value)
    tableRef.value?.refresh?.()
  } catch (error) {
    console.error('toggle table config status failed:', error)
  }
}
</script>

<style scoped lang="less">
.table-config-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
