<template>
  <div class="encode-rule-management">
    <fx-dynamic-table
      ref="tableRef"
      table-code="EncodeRuleTable"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button
            v-permission="'sys:encodeRule:add'"
            type="primary"
            @click="openAddDialog"
          >
            {{ t('system.encodeRule.add') }}
          </a-button>
          <a-button
            v-permission="'sys:encodeRule:delete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
          <a-button
            v-permission="'sys:encodeRule:test'"
            @click="openTestDialog"
          >
            {{ t('system.encodeRule.test') }}
          </a-button>
        </a-space>
      </template>

      <template #isEnabled="{ record }">
        <a-tag v-if="record.isEnabled" color="success">
          {{ getEnabledLabel(true) || t('system.encodeRule.statusActive') }}
        </a-tag>
        <a-tag v-else color="default">
          {{ getEnabledLabel(false) || t('system.encodeRule.statusInactive') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a
            v-permission="'sys:encodeRule:edit'"
            @click="openEditDialog(record)"
          >
            {{ t('system.encodeRule.edit') }}
          </a>
          <a
            v-permission="'sys:encodeRule:generate'"
            @click="handleGenerateEncode(record)"
          >
            {{ t('system.encodeRule.generate') }}
          </a>
          <a
            v-permission="'sys:encodeRule:delete'"
            style="color: #ff4d4f"
            @click="handleDelete(record.id)"
          >
            {{ t('system.encodeRule.delete') }}
          </a>
        </a-space>
      </template>
    </fx-dynamic-table>

    <EncodeRuleFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :rule-id="currentRuleId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 编码规则管理页面
 *
 * 功能：
 * 1. 查询编码规则列表
 * 2. 新增、编辑、删除编码规则
 * 3. 测试和生成编码
 */
import { computed, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import { encodeRuleApi } from '@/api/system/encodeRule'
import type { EncodeRule } from '@/api/system/encodeRule'
import { useDict } from '@/hooks/useDict'
import EncodeRuleFormDialog from './components/EncodeRuleFormDialog.vue'

const { t } = useI18n()

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentRuleId = ref<string>()
const selectedRowKeys = ref<string[]>([])
const tableRef = ref()

const { dictItems: encodeRuleEnabledOptions } = useDict('encode_rule_enabled')

const dictOptions = computed<Record<string, any[]>>(() => ({
  isEnabled: encodeRuleEnabledOptions.value || [],
  encode_rule_enabled: encodeRuleEnabledOptions.value || [],
}))

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const params: any = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...payload.query,
  }

  if (Object.prototype.hasOwnProperty.call(params, 'businessType') && !Object.prototype.hasOwnProperty.call(params, 'module')) {
    params.module = params.businessType
    delete params.businessType
  }

  if (Object.prototype.hasOwnProperty.call(params, 'status') && !Object.prototype.hasOwnProperty.call(params, 'isEnabled')) {
    params.isEnabled = params.status === 1 || params.status === true || params.status === '1' || params.status === 'true'
    delete params.status
  }

  if (Object.prototype.hasOwnProperty.call(params, 'isEnabled')) {
    if (params.isEnabled === 1 || params.isEnabled === '1' || params.isEnabled === 'true') {
      params.isEnabled = true
    } else if (params.isEnabled === 0 || params.isEnabled === '0' || params.isEnabled === 'false') {
      params.isEnabled = false
    }
  }

  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const data = await encodeRuleApi.pageEncodeRules(params)
  const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
  return { records: data.records || [], total }
}

function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

function getEnabledLabel(value?: boolean) {
  return encodeRuleEnabledOptions.value.find((item: { label: string; value: string | number | boolean }) => {
    if (item.value === true || item.value === 'true' || item.value === 1 || item.value === '1') {
      return value === true
    }
    if (item.value === false || item.value === 'false' || item.value === 0 || item.value === '0') {
      return value === false
    }
    return false
  })?.label
}

function openAddDialog() {
  isEdit.value = false
  currentRuleId.value = undefined
  dialogVisible.value = true
}

function openEditDialog(record: EncodeRule) {
  isEdit.value = true
  currentRuleId.value = record.id
  dialogVisible.value = true
}

function openTestDialog() {
  message.info(t('system.encodeRule.testNotImplemented'))
}

function handleFormSuccess() {
  tableRef.value?.refresh?.()
}

async function handleDelete(id: string) {
  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.encodeRule.message.deleteConfirm'),
    onOk: async () => {
      await encodeRuleApi.deleteEncodeRule(id)
      tableRef.value?.refresh?.()
    },
  })
}

async function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return

  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.encodeRule.message.batchDeleteConfirm'),
    onOk: async () => {
      await encodeRuleApi.batchDeleteEncodeRules(selectedRowKeys.value)
      selectedRowKeys.value = []
      tableRef.value?.refresh?.()
    },
  })
}

async function handleGenerateEncode(record: EncodeRule) {
  try {
    const result = await encodeRuleApi.generateEncode({
      ruleCode: record.ruleCode!,
    })
    message.success(`${t('system.encodeRule.generateSuccess')}: ${result}`)
  } catch (error) {
    console.error('生成编码失败:', error)
    message.error(t('system.encodeRule.generateFailed'))
  }
}

onMounted(() => {
  // 依赖 FxDynamicTable 在挂载后自行拉取列表，这里保留空钩子便于后续扩展。
})
</script>

<style scoped lang="less">
.encode-rule-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
