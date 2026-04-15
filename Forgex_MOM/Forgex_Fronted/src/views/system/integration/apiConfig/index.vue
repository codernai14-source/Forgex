<template>
  <div class="api-config-management">
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline" :model="queryForm">
        <a-form-item :label="t('integration.apiConfig.apiCode')">
          <a-input
            v-model:value="queryForm.apiCode"
            :placeholder="t('integration.apiConfig.form.apiCode')"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item :label="t('integration.apiConfig.apiName')">
          <a-input
            v-model:value="queryForm.apiName"
            :placeholder="t('integration.apiConfig.form.apiName')"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>

        <a-form-item :label="t('integration.apiConfig.operationType')">
          <a-select
            v-model:value="queryForm.operationType"
            :placeholder="t('integration.apiConfig.form.operationType')"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="CREATE">创建</a-select-option>
            <a-select-option value="UPDATE">更新</a-select-option>
            <a-select-option value="DELETE">删除</a-select-option>
            <a-select-option value="READ">查询</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('integration.apiConfig.callMethod')">
          <a-select
            v-model:value="queryForm.callMethod"
            :placeholder="t('integration.apiConfig.form.callMethod')"
            allow-clear
            style="width: 150px"
          >
            <a-select-option value="SYNC">同步</a-select-option>
            <a-select-option value="ASYNC">异步</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item :label="t('integration.apiConfig.status')">
          <a-select
            v-model:value="queryForm.status"
            :placeholder="t('integration.apiConfig.form.status')"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="1">启用</a-select-option>
            <a-select-option :value="0">停用</a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">{{ t('common.search') }}</a-button>
            <a-button @click="handleReset">{{ t('common.reset') }}</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <div style="margin-bottom: 16px">
        <a-space>
          <a-button v-permission="'integration:apiConfig:add'" type="primary" @click="openAddDialog">
            {{ t('integration.apiConfig.add') }}
          </a-button>
          <a-button
            v-permission="'integration:apiConfig:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
        </a-space>
      </div>

      <div ref="tableWrapRef" class="table-wrap">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          :scroll="tableScroll"
          :row-key="rowKey"
          :row-selection="{ selectedRowKeys, onChange: handleSelectionChange }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'operationType'">
              <a-tag v-if="record.operationType === 'CREATE'" color="green">创建</a-tag>
              <a-tag v-else-if="record.operationType === 'UPDATE'" color="blue">更新</a-tag>
              <a-tag v-else-if="record.operationType === 'DELETE'" color="red">删除</a-tag>
              <a-tag v-else color="default">查询</a-tag>
            </template>

            <template v-else-if="column.key === 'callMethod'">
              <a-tag v-if="record.callMethod === 'SYNC'" color="blue">同步</a-tag>
              <a-tag v-else color="orange">异步</a-tag>
            </template>

            <template v-else-if="column.key === 'status'">
              <a-switch
                v-permission="'integration:apiConfig:edit'"
                :checked="record.status === 1"
                :loading="record.statusLoading"
                @change="(checked: boolean) => handleToggleStatus(record.id!, checked ? 1 : 0)"
              />
            </template>

            <template v-else-if="column.key === 'action'">
              <a-space>
                <a v-permission="'integration:apiConfig:edit'" @click="openEditDialog(record)">{{ t('common.edit') }}</a>
                <a v-permission="'integration:apiConfig:delete'" style="color: #ff4d4f" @click="handleDelete(record.id!)">{{ t('common.delete') }}</a>
                <a v-permission="'integration:apiConfig:paramConfig'" @click="openParamConfigDialog(record)">参数配置</a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <ApiConfigFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :config-id="currentConfigId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import ApiConfigFormDialog from './components/ApiConfigFormDialog.vue'
import {
  batchDeleteApiConfigs,
  deleteApiConfig,
  getApiConfigList,
  toggleApiConfigStatus,
} from '@/api/system/integration'
import type { ApiConfigItem } from '@/api/system/integration'
import { getI18nValue } from '@/utils/i18n'

const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const dataSource = ref<ApiConfigItem[]>([])
const selectedRowKeys = ref<number[]>([])
const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentConfigId = ref<number | undefined>()

let computeScrollYRafPending = false
let tableWrapObserver: MutationObserver | null = null

const queryForm = reactive({
  apiCode: '',
  apiName: '',
  operationType: undefined as string | undefined,
  callMethod: undefined as string | undefined,
  status: undefined as number | undefined,
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => t('common.total', { total }),
  hideOnSinglePage: false,
})

const columns = computed(() => [
  { title: t('integration.apiConfig.apiCode'), dataIndex: 'apiCode', key: 'apiCode', width: 180 },
  { title: t('integration.apiConfig.apiName'), dataIndex: 'apiName', key: 'apiName', width: 220, ellipsis: true },
  { title: '操作方向', dataIndex: 'operationType', key: 'operationType', width: 120 },
  { title: '调用方式', dataIndex: 'callMethod', key: 'callMethod', width: 100 },
  { title: '状态', dataIndex: 'status', key: 'status', width: 100, align: 'center' as const },
  { title: t('system.user.createBy'), dataIndex: 'createBy', key: 'createBy', width: 120 },
  { title: t('common.createTime'), dataIndex: 'createTime', key: 'createTime', width: 180 },
  { title: t('common.action'), key: 'action', width: 200, fixed: 'right' as const },
])

const tableScroll = computed(() => ({ x: 1210, y: autoScrollY.value }))
const rowKey = (record: ApiConfigItem) => record.id ?? record.apiCode

async function fetchData() {
  loading.value = true
  try {
    const result = await getApiConfigList({
      ...queryForm,
      current: pagination.current,
      pageSize: pagination.pageSize,
    })
    dataSource.value = result.records || []
    pagination.total = result.total || 0
    await nextTick()
    scheduleComputeAutoScrollY()
  } catch (error) {
    console.error('load api config failed:', error)
  } finally {
    loading.value = false
  }
}

function onResizeOrScroll() {
  scheduleComputeAutoScrollY()
}

function scheduleComputeAutoScrollY() {
  if (computeScrollYRafPending) return
  computeScrollYRafPending = true
  requestAnimationFrame(() => {
    computeScrollYRafPending = false
    computeAutoScrollY()
  })
}

function computeAutoScrollY() {
  const wrapEl = tableWrapRef.value
  if (!wrapEl) {
    autoScrollY.value = undefined
    return
  }
  const rect = wrapEl.getBoundingClientRect()
  if (!Number.isFinite(rect.height) || rect.height <= 0) {
    autoScrollY.value = undefined
    return
  }
  const paginationEl = wrapEl.querySelector('.ant-pagination') as HTMLElement | null
  const paginationHeight = paginationEl ? paginationEl.getBoundingClientRect().height : 0
  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0
  const nextY = Math.floor(rect.height - paginationHeight - headerHeight - 36)
  autoScrollY.value = nextY > 100 ? nextY : undefined
}

function handleSearch() {
  pagination.current = 1
  fetchData()
}

function handleReset() {
  queryForm.apiCode = ''
  queryForm.apiName = ''
  queryForm.operationType = undefined
  queryForm.callMethod = undefined
  queryForm.status = undefined
  pagination.current = 1
  fetchData()
}

function handleTableChange(pag: { current?: number; pageSize?: number }) {
  pagination.current = pag.current || 1
  pagination.pageSize = pag.pageSize || 20
  fetchData()
}

function handleSelectionChange(keys: number[]) {
  selectedRowKeys.value = keys
}

function openAddDialog() {
  isEdit.value = false
  currentConfigId.value = undefined
  dialogVisible.value = true
}

async function openEditDialog(record: ApiConfigItem) {
  isEdit.value = true
  currentConfigId.value = record.id
  await nextTick()
  dialogVisible.value = true
}

function handleFormSuccess() {
  dialogVisible.value = false
  fetchData()
}

function handleDelete(id: number) {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('common.confirmDeleteMessage'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteApiConfig(id)
      fetchData()
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
      await batchDeleteApiConfigs(selectedRowKeys.value)
      selectedRowKeys.value = []
      fetchData()
    },
  })
}

async function handleToggleStatus(id: number, status: number) {
  const record = dataSource.value.find((item) => item.id === id)
  if (!record) return
  record.statusLoading = true
  try {
    await toggleApiConfigStatus(id, status)
    record.status = status
  } finally {
    record.statusLoading = false
  }
}

function openParamConfigDialog(record: ApiConfigItem) {
  message.info(`参数配置功能开发中 - API: ${record.apiCode}`)
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', onResizeOrScroll)
  tableWrapObserver = new MutationObserver(() => scheduleComputeAutoScrollY())
  if (tableWrapRef.value) {
    tableWrapObserver.observe(tableWrapRef.value, { childList: true, subtree: true })
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResizeOrScroll)
  tableWrapObserver?.disconnect()
})
</script>

<style scoped lang="less">
.api-config-management {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  min-height: 0;
}

.query-card :deep(.ant-card-body) {
  padding-bottom: 8px;
}

.table-card {
  flex: 1;
  min-height: 0;
}

.table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.table-wrap {
  flex: 1;
  min-height: 0;
}
</style>
