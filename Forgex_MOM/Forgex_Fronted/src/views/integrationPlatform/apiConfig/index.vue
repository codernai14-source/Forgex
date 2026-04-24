<template>
  <div class="api-config-management">
    <section v-if="editor.mode === 'list'" class="api-config-panel">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'ApiConfigTable'"
        :request="handleRequest"
        :dynamic-table-config="dynamicTableConfig"
        :dict-options="dictOptions"
        :show-query-form="true"
        row-key="id"
        :row-selection="{
          selectedRowKeys,
          onChange: handleSelectionChange,
        }"
      >
        <template #toolbar>
          <a-space>
            <a-button v-permission="'integration:api-config:add'" type="primary" @click="openAddForm">
              {{ t('integration.apiConfig.add') }}
            </a-button>
            <a-button
              v-permission="'integration:api-config:delete'"
              danger
              :disabled="selectedRowKeys.length === 0"
              @click="handleBatchDelete"
            >
              {{ t('common.batchDelete') }}
            </a-button>
          </a-space>
        </template>

        <template #direction="{ record }">
          <a-tag :color="record.direction === 'INBOUND' ? 'blue' : 'cyan'">
            {{ record.direction === 'INBOUND' ? t('integration.common.inbound') : t('integration.common.outbound') }}
          </a-tag>
        </template>

        <template #callMethod="{ record }">
          <a-tag color="purple">{{ record.callMethod || '-' }}</a-tag>
        </template>

        <template #status="{ record }">
          <a-switch
            v-permission="'integration:api-config:edit'"
            :checked="record.status === 1"
            :loading="record.statusLoading"
            @change="(checked: boolean) => handleToggleStatus(record, checked)"
          />
        </template>

        <template #action="{ record }">
          <a-space>
            <a v-permission="'integration:api-config:edit'" @click="openEditForm(record)">{{ t('common.edit') }}</a>
            <a v-permission="'integration:api-config:config-param'" @click="openParamConfig(record)">
              {{ t('integration.apiConfig.paramConfig') }}
            </a>
            <a v-permission="'integration:api-config:delete'" class="danger-link" @click="handleDelete(record.id!)">
              {{ t('common.delete') }}
            </a>
          </a-space>
        </template>
      </fx-dynamic-table>
    </section>

    <section v-else-if="editor.mode === 'form'" class="api-config-panel api-config-panel--editor">
      <div class="panel-header">
        <div>
          <h2>{{ editor.isEdit ? t('integration.apiConfig.edit') : t('integration.apiConfig.add') }}</h2>
          <p>{{ t('integration.apiConfig.title') }}</p>
        </div>
        <a-button @click="backToList">{{ t('common.back') }}</a-button>
      </div>

      <ApiConfigFormDialog
        :open="true"
        :is-edit="editor.isEdit"
        :config-id="editor.apiConfig?.id"
        mode="drawer"
        width="820px"
        @update:open="handleFormOpenChange"
        @success="handleFormSuccess"
      />
    </section>

    <section v-else class="api-config-panel api-config-panel--param">
      <div class="panel-header">
        <div>
          <h2>{{ t('integration.apiConfig.paramConfig') }}</h2>
          <p>{{ editor.apiConfig?.apiName || editor.apiConfig?.apiCode || '-' }}</p>
        </div>
        <a-button @click="backToList">{{ t('common.back') }}</a-button>
      </div>

      <ApiParamConfigDialog
        :open="true"
        :api-config="editor.apiConfig"
        :page-mode="true"
        @update:open="handleParamOpenChange"
      />
    </section>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ApiConfigFormDialog from './components/ApiConfigFormDialog.vue'
import ApiParamConfigDialog from './components/ApiParamConfigDialog.vue'
import {
  batchDeleteApiConfigs,
  deleteApiConfig,
  disableApiConfig,
  enableApiConfig,
  getApiConfigList,
} from '@/api/system/integration'
import type { ApiConfigItem, IntegrationDirection } from '@/api/system/integration'
import type { ApiConfigEditorState } from './types'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const selectedRowKeys = ref<number[]>([])
const editor = reactive<ApiConfigEditorState>({
  mode: 'list',
  isEdit: false,
  apiConfig: undefined,
})

const dictOptions = {
  integrationDirection: [
    { label: t('integration.common.inbound'), value: 'INBOUND' },
    { label: t('integration.common.outbound'), value: 'OUTBOUND' },
  ],
  integrationStatus: [
    { label: t('integration.common.enabled'), value: 1 },
    { label: t('integration.common.disabled'), value: 0 },
  ],
}

const dynamicTableConfig: Partial<FxTableConfig> = {
  columns: [
    { field: 'apiCode', title: t('integration.apiConfig.apiCode'), width: 180, align: 'left', visible: true },
    { field: 'apiName', title: t('integration.apiConfig.apiName'), width: 200, align: 'left', ellipsis: true, visible: true },
    { field: 'direction', title: t('integration.apiConfig.direction'), width: 120, align: 'center', visible: true },
    { field: 'apiPath', title: t('integration.apiConfig.apiPath'), width: 220, align: 'left', ellipsis: true, visible: true },
    { field: 'callMethod', title: t('integration.apiConfig.callMethod'), width: 120, align: 'center', visible: true },
    { field: 'targetUrl', title: t('integration.apiConfig.targetUrl'), width: 240, align: 'left', ellipsis: true, visible: true },
    { field: 'callCount', title: t('integration.apiConfig.callCount'), width: 110, align: 'center', visible: true },
    { field: 'status', title: t('integration.apiConfig.status'), width: 100, align: 'center', visible: true },
    { field: 'createTime', title: t('common.createTime'), width: 180, align: 'center', visible: true },
    { field: 'action', title: t('common.action'), width: 220, align: 'center', fixed: 'right', visible: true },
  ],
  queryFields: [
    { field: 'apiCode', label: t('integration.apiConfig.apiCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'apiName', label: t('integration.apiConfig.apiName'), queryType: 'input', queryOperator: 'like' },
    { field: 'direction', label: t('integration.apiConfig.direction'), queryType: 'select', queryOperator: 'eq', dictCode: 'integrationDirection' },
    { field: 'status', label: t('integration.apiConfig.status'), queryType: 'select', queryOperator: 'eq', dictCode: 'integrationStatus' },
  ],
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const query = { ...payload.query }
  const result = await getApiConfigList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    apiCode: query.apiCode || undefined,
    apiName: query.apiName || undefined,
    direction: (query.direction || undefined) as IntegrationDirection | undefined,
    status: query.status === '' || query.status === undefined ? undefined : Number(query.status),
  })

  return {
    records: (result.records || []).map(item => ({ ...item, statusLoading: false })),
    total: Number(result.total || 0),
  }
}

function handleSelectionChange(keys: number[]) {
  selectedRowKeys.value = keys
}

function openAddForm() {
  editor.mode = 'form'
  editor.isEdit = false
  editor.apiConfig = undefined
}

function openEditForm(record: ApiConfigItem) {
  editor.mode = 'form'
  editor.isEdit = true
  editor.apiConfig = record
}

function openParamConfig(record: ApiConfigItem) {
  editor.mode = 'param'
  editor.isEdit = true
  editor.apiConfig = record
}

function handleFormOpenChange(open: boolean) {
  if (!open) {
    backToList()
  }
}

function handleParamOpenChange(open: boolean) {
  if (!open) {
    backToList()
  }
}

function handleFormSuccess(record?: ApiConfigItem) {
  if (record) {
    editor.apiConfig = record
    editor.isEdit = true
    editor.mode = 'param'
  } else {
    backToList()
  }
  void tableRef.value?.refresh?.()
}

function backToList() {
  editor.mode = 'list'
  editor.apiConfig = undefined
  editor.isEdit = false
}

function backToFormOrList() {
  if (editor.apiConfig?.id) {
    editor.mode = 'form'
    editor.isEdit = true
    return
  }
  backToList()
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('common.confirmDeleteMessage'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    async onOk() {
      await deleteApiConfig(id)
      await tableRef.value?.refresh?.()
    },
  })
}

function handleBatchDelete() {
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    async onOk() {
      await batchDeleteApiConfigs(selectedRowKeys.value)
      selectedRowKeys.value = []
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleToggleStatus(record: ApiConfigItem, checked: boolean) {
  record.statusLoading = true
  try {
    if (checked) {
      await enableApiConfig(record.id!)
      record.status = 1
    } else {
      await disableApiConfig(record.id!)
      record.status = 0
    }
  } finally {
    record.statusLoading = false
  }
}
</script>

<style scoped lang="less">
.api-config-management {
  min-height: 0;
}

.api-config-panel {
  min-height: 0;
}

.api-config-panel--editor,
.api-config-panel--param {
  display: flex;
  min-height: calc(100vh - 170px);
  flex-direction: column;
  gap: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 24px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 20px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));

  h2 {
    margin: 0 0 4px;
    color: var(--fx-text-primary, #111827);
  }

  p {
    margin: 0;
    color: var(--fx-text-secondary, #6b7280);
  }
}

:deep(.api-config-panel--editor .ant-drawer) {
  position: static;
}

:deep(.api-config-panel--editor .ant-drawer-content-wrapper) {
  position: static !important;
  width: min(820px, 100%) !important;
  margin: 0 auto;
  box-shadow: none !important;
}

:deep(.api-config-panel--editor .ant-drawer-content) {
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 20px;
  overflow: hidden;
  background: var(--fx-bg-container, #ffffff);
}

:deep(.api-config-panel--editor .ant-drawer-header) {
  display: none;
}

.danger-link {
  color: #ff4d4f;
}
</style>
