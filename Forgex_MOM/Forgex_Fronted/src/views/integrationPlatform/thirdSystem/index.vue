<template>
  <div class="integration-page">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'ThirdSystemTable'"
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
          <a-button v-permission="'integration:third-system:add'" type="primary" @click="openAddDialog">
            {{ t('integration.thirdSystem.add') }}
          </a-button>
          <a-button
            v-permission="'integration:third-system:batch-delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #status="{ record }">
        <a-switch
          v-permission="'integration:third-system:edit'"
          :checked="record.status === 1"
          :loading="record.statusLoading"
          @change="(checked: boolean) => handleStatusChange(record, checked)"
        />
      </template>

      <template #action="{ record }">
        <a-space>
          <a v-permission="'integration:third-system:edit'" @click="openEditDialog(record)">{{ t('common.edit') }}</a>
          <a v-permission="'integration:third-system:auth'" @click="openAuthDialog(record)">
            {{ t('integration.thirdSystem.auth') }}
          </a>
          <a v-permission="'integration:third-system:delete'" class="danger-link" @click="handleDelete(record.id!)">
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </fx-dynamic-table>

    <ThirdSystemFormDialog
      v-model:open="formVisible"
      :is-edit="isEdit"
      :form-data="currentFormData"
      @submit="handleSubmit"
    />

    <ThirdSystemAuthDialog
      v-model:open="authVisible"
      :system-id="currentSystemId"
      @success="handleAuthSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ThirdSystemFormDialog from './components/ThirdSystemFormDialog.vue'
import ThirdSystemAuthDialog from './components/ThirdSystemAuthDialog.vue'
import {
  addThirdSystem,
  batchDeleteThirdSystems,
  deleteThirdSystem,
  getThirdSystemDetail,
  getThirdSystemList,
  updateThirdSystem,
} from '@/api/system/integration'
import type { ThirdSystemItem, ThirdSystemSubmit } from '@/api/system/integration'

const { t } = useI18n({ useScope: 'global' })

type TableRecord = ThirdSystemItem & { statusLoading?: boolean }

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const formVisible = ref(false)
const authVisible = ref(false)
const isEdit = ref(false)
const currentSystemId = ref<number>()
const selectedRowKeys = ref<number[]>([])

const currentFormData = ref<ThirdSystemSubmit>({
  systemCode: '',
  systemName: '',
  ipAddress: '',
  contactInfo: '',
  remark: '',
  status: 1,
})

const dynamicTableConfig: Partial<FxTableConfig> = {
  columns: [
    { field: 'systemCode', title: t('integration.thirdSystem.systemCode'), width: 180, align: 'left', visible: true },
    { field: 'systemName', title: t('integration.thirdSystem.systemName'), width: 220, align: 'left', visible: true },
    { field: 'ipAddress', title: t('integration.thirdSystem.ipAddress'), width: 200, align: 'left', ellipsis: true, visible: true },
    { field: 'contactInfo', title: t('integration.thirdSystem.contactInfo'), width: 220, align: 'left', ellipsis: true, visible: true },
    { field: 'status', title: t('integration.thirdSystem.status'), width: 120, align: 'center', visible: true },
    { field: 'createTime', title: t('common.createTime'), width: 180, align: 'center', visible: true },
    { field: 'action', title: t('common.action'), width: 180, align: 'center', fixed: 'right', visible: true },
  ],
  queryFields: [
    { field: 'systemCode', label: t('integration.thirdSystem.systemCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'systemName', label: t('integration.thirdSystem.systemName'), queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: t('integration.thirdSystem.status'), queryType: 'select', queryOperator: 'eq', dictCode: 'thirdSystemStatus' },
  ],
}

const dictOptions = {
  thirdSystemStatus: [
    { label: t('integration.common.enabled'), value: 1 },
    { label: t('integration.common.disabled'), value: 0 },
  ],
}

async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const query = { ...payload.query }
  const result = await getThirdSystemList({
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    systemCode: query.systemCode || undefined,
    systemName: query.systemName || undefined,
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

function openAddDialog() {
  isEdit.value = false
  currentFormData.value = {
    systemCode: '',
    systemName: '',
    ipAddress: '',
    contactInfo: '',
    remark: '',
    status: 1,
  }
  formVisible.value = true
}

async function openEditDialog(record: ThirdSystemItem) {
  isEdit.value = true
  currentFormData.value = await getThirdSystemDetail(record.id!)
  formVisible.value = true
}

function openAuthDialog(record: ThirdSystemItem) {
  currentSystemId.value = record.id
  authVisible.value = true
}

async function handleSubmit(payload: ThirdSystemSubmit) {
  if (isEdit.value) {
    await updateThirdSystem(payload)
  } else {
    await addThirdSystem(payload)
  }
  formVisible.value = false
  await tableRef.value?.refresh?.()
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('integration.thirdSystem.confirmDelete'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    async onOk() {
      await deleteThirdSystem(id)
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
      await batchDeleteThirdSystems(selectedRowKeys.value)
      selectedRowKeys.value = []
      await tableRef.value?.refresh?.()
    },
  })
}

async function handleStatusChange(record: TableRecord, checked: boolean) {
  record.statusLoading = true
  try {
    await updateThirdSystem({
      id: record.id,
      systemCode: record.systemCode,
      systemName: record.systemName,
      ipAddress: record.ipAddress,
      contactInfo: record.contactInfo,
      remark: record.remark,
      status: checked ? 1 : 0,
    })
    record.status = checked ? 1 : 0
  } finally {
    record.statusLoading = false
  }
}

async function handleAuthSuccess() {
  authVisible.value = false
  await tableRef.value?.refresh?.()
}
</script>

<style scoped lang="less">
.integration-page {
  min-height: 0;
}

.danger-link {
  color: #ff4d4f;
}
</style>
