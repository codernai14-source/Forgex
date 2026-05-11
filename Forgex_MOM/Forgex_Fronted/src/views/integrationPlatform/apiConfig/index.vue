<template>
  <div class="api-config-management">
    <section v-if="editor.mode === 'param'" class="api-config-panel api-config-panel--param">
      <div class="panel-header">
        <div>
          <h2>{{ t('integration.apiConfig.paramConfig') }}</h2>
          <p>{{ editor.apiConfig?.apiName || editor.apiConfig?.apiCode || '-' }}</p>
        </div>
        <a-button @click="backToList">{{ t('common.back') }}</a-button>
      </div>

      <a-spin :spinning="paramConfigLoading">
        <ApiParamConfigDialog
          v-if="editor.apiConfig"
          class="api-config-param-content"
          :open="true"
          :api-config="editor.apiConfig"
          :page-mode="true"
          @update:open="handleParamOpenChange"
        />
      </a-spin>
    </section>
    <section v-else class="api-config-panel">
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'ApiConfigTable'"
        :request="handleRequest"
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

        <template #apiPath="{ record }">
          <a-space direction="vertical" :size="2" class="path-cell">
            <template v-if="record.direction === 'INBOUND'">
              <span>{{ record.apiPath || '/integration/public/invoke' }}</span>
            </template>
            <template v-else>
              <span
                v-for="(path, index) in getOutboundTargetPaths(record)"
                :key="`${record.id || record.apiCode}-path-${index}`"
              >
                {{ path }}
              </span>
              <span v-if="getOutboundTargetPaths(record).length === 0">-</span>
            </template>
          </a-space>
        </template>

        <template #targetUrl="{ record }">
          <a-space direction="vertical" :size="2" class="target-cell">
            <template v-if="record.direction === 'OUTBOUND'">
              <span
                v-for="(url, index) in getOutboundTargetUrls(record)"
                :key="`${record.id || record.apiCode}-url-${index}`"
              >
                {{ url }}
              </span>
              <span v-if="getOutboundTargetUrls(record).length === 0">{{ record.targetUrl || '-' }}</span>
            </template>
            <template v-else>
              <span>{{ record.targetUrl || '-' }}</span>
            </template>
          </a-space>
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

    <ApiConfigFormDialog
      v-model:open="formDialogVisible"
      :is-edit="editor.isEdit"
      :config-id="editor.apiConfig?.id"
      :width="960"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ApiConfigFormDialog from './components/ApiConfigFormDialog.vue'
import ApiParamConfigDialog from './components/ApiParamConfigDialog.vue'
import {
  batchDeleteApiConfigs,
  deleteApiConfig,
  disableApiConfig,
  enableApiConfig,
  getApiConfigDetail,
  getApiConfigList,
} from '@/api/system/integration'
import type { ApiConfigItem, IntegrationDirection } from '@/api/system/integration'
import type { ApiConfigEditorState } from './types'

const { t } = useI18n({ useScope: 'global' })

const tableRef = ref<InstanceType<typeof FxDynamicTable>>()
const selectedRowKeys = ref<number[]>([])
const formDialogVisible = ref(false)
const paramConfigLoading = ref(false)
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
  editor.mode = 'list'
  editor.isEdit = false
  editor.apiConfig = undefined
  formDialogVisible.value = true
}

function openEditForm(record: ApiConfigItem) {
  editor.mode = 'list'
  editor.isEdit = true
  editor.apiConfig = record
  formDialogVisible.value = true
}

function handleParamOpenChange(open: boolean) {
  if (!open) {
    backToList()
  }
}

function handleFormSuccess(record?: ApiConfigItem) {
  formDialogVisible.value = false
  editor.apiConfig = record
  editor.isEdit = false
  editor.mode = 'list'
  void tableRef.value?.refresh?.()
}

async function openParamConfig(record: ApiConfigItem) {
  editor.mode = 'param'
  editor.isEdit = true
  editor.apiConfig = undefined
  paramConfigLoading.value = true
  try {
    const detail = record.id ? await getApiConfigDetail(record.id) : record
    editor.apiConfig = {
      ...record,
      ...detail,
      outboundTargets: detail.outboundTargets || record.outboundTargets || [],
    }
  } catch {
    message.error(t('integration.common.loadFailed'))
    editor.apiConfig = record
  } finally {
    paramConfigLoading.value = false
  }
}

function backToList() {
  editor.mode = 'list'
  editor.apiConfig = undefined
  editor.isEdit = false
}

function getOutboundTargetUrls(record: ApiConfigItem) {
  const urls = (record.outboundTargets || [])
    .map(item => item.targetUrl?.trim())
    .filter((value): value is string => Boolean(value))
  return urls.length ? urls : (record.targetUrl ? [record.targetUrl] : [])
}

function getOutboundTargetPaths(record: ApiConfigItem) {
  return getOutboundTargetUrls(record).map(url => {
    try {
      const parsed = new URL(url)
      return `${parsed.pathname}${parsed.search}${parsed.hash}` || '/'
    } catch {
      return url
    }
  })
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

<style scoped lang="less" src="@/styles/integration-api-config.less"></style>
