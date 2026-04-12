<template>
  <div class="encode-rule-management">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'EncodeRuleTable'"
      :降级方案-config="降级方案Config"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
      <!-- 宸ュ叿鏍忔彃妲?-->
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
      
      <!-- 鐘舵€佸垪鑷畾涔夋覆鏌?-->
      <template #isEnabled="{ record }">
        <a-tag
          v-if="record.isEnabled"
          color="success"
        >
          {{ t('system.encodeRule.statusActive') }}
        </a-tag>
        <a-tag
          v-else
          color="default"
        >
          {{ t('system.encodeRule.statusInactive') }}
        </a-tag>
      </template>
      
      <!-- 鎿嶄綔鍒?-->
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
            style="color: #ff4d4f;"
            @click="handleDelete(record.id)"
          >
            {{ t('system.encodeRule.delete') }}
          </a>
        </a-space>
      </template>
    </fx-dynamic-table>
  
    <!-- 鏂板/缂栬緫寮圭獥 -->
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
 * 缂栫爜瑙勫垯绠＄悊椤甸潰
 * 
 * 鍔熻兘锛?
 * 1. 缂栫爜瑙勫垯鍒楄〃鏌ヨ锛堝垎椤点€佹悳绱級
 * 2. 鏂板銆佺紪杈戙€佸垹闄ょ紪鐮佽鍒?
 * 3. 鐢熸垚缂栫爜銆佹祴璇曡鍒?
 * 4. 鎵归噺鍒犻櫎
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import EncodeRuleFormDialog from './components/EncodeRuleFormDialog.vue'
import { encodeRuleApi } from '@/api/system/encodeRule'
import type { EncodeRule } from '@/api/system/encodeRule'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import type { FxTableConfig } from '@/api/system/tableConfig'

// 鍥介檯鍖?
const { t } = useI18n()

// 寮圭獥鐘舵€?
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentRuleId = ref<string>()

// 閫変腑鐨勮鍒?ID 鍒楄〃
const selectedRowKeys = ref<string[]>([])

// 琛ㄦ牸寮曠敤
const tableRef = ref()

const 降级方案Config = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'EncodeRuleTable',
  tableName: t('system.encodeRule.pageTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 10,
  columns: [
    { field: 'ruleCode', title: t('system.encodeRule.ruleCode'), width: 160, align: 'left' },
    { field: 'ruleName', title: t('system.encodeRule.ruleName'), width: 180, align: 'left' },
    { field: 'module', title: t('system.encodeRule.businessType'), width: 140, align: 'left' },
    { field: 'isEnabled', title: t('system.encodeRule.status'), width: 120, align: 'center' },
    { field: 'remark', title: t('system.encodeRule.remark'), width: 220, align: 'left' },
    { field: 'createTime', title: t('common.createTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 220, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'ruleCode', label: t('system.encodeRule.ruleCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'ruleName', label: t('system.encodeRule.ruleName'), queryType: 'input', queryOperator: 'like' },
    { field: 'module', label: t('system.encodeRule.businessType'), queryType: 'input', queryOperator: 'like' },
    {
      field: 'isEnabled',
      label: t('system.encodeRule.status'),
      queryType: 'select',
      queryOperator: 'eq',
      options: [
        { label: t('system.encodeRule.statusActive'), value: true },
        { label: t('system.encodeRule.statusInactive'), value: false },
      ],
    },
  ],
  version: 1,
}))

// 瀛楀吀閫夐」閰嶇疆
const dictOptions = ref<Record<string, any[]>>({
  isEnabled: [
    { label: t('system.encodeRule.statusActive'), value: true },
    { label: t('system.encodeRule.statusInactive'), value: false }
  ],
  segmentType: [
    { label: t('system.encodeRule.segmentTypeFixed'), value: 'FIXED' },
    { label: t('system.encodeRule.segmentTypeDate'), value: 'DATE' },
    { label: t('system.encodeRule.segmentTypeSeq'), value: 'SEQ' },
    { label: t('system.encodeRule.segmentTypeCustom'), value: 'CUSTOM' }
  ]
})

/**
 * 鏁版嵁璇锋眰鍑芥暟
 */
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
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
  
  // 澶勭悊鎺掑簭
  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }
  
  // http 鎷︽埅鍣ㄥ凡缁忚繑鍥炰簡 data 瀛楁
  const data = await encodeRuleApi.pageEncodeRules(params)
  const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
  return { records: data.records || [], total: total }
}

/**
 * 琛岄€夋嫨鍙樺寲
 */
function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

/**
 * 鎵撳紑鏂板寮圭獥
 */
function openAddDialog() {
  isEdit.value = false
  currentRuleId.value = undefined
  dialogVisible.value = true
}

/**
 * 鎵撳紑缂栬緫寮圭獥
 */
function openEditDialog(record: EncodeRule) {
  isEdit.value = true
  currentRuleId.value = record.id
  dialogVisible.value = true
}

/**
 * 鎵撳紑娴嬭瘯瀵硅瘽妗?
 */
function openTestDialog() {
  // TODO: 瀹炵幇娴嬭瘯瀵硅瘽妗?
  message.info(t('system.encodeRule.testNotImplemented'))
}

/**
 * 琛ㄥ崟鎻愪氦鎴愬姛鍥炶皟
 */
function handleFormSuccess() {
  // 鍒锋柊琛ㄦ牸鏁版嵁
  tableRef.value?.refresh?.()
}

/**
 * 鍒犻櫎缂栫爜瑙勫垯
 */
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

/**
 * 鎵归噺鍒犻櫎缂栫爜瑙勫垯
 */
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

/**
 * 鐢熸垚缂栫爜
 */
async function handleGenerateEncode(record: EncodeRule) {
  try {
    const result = await encodeRuleApi.generateEncode({
      ruleCode: record.ruleCode!,
    })
    message.success(t('system.encodeRule.generateSuccess') + ': ' + result)
  } catch (error) {
    console.error('鐢熸垚缂栫爜澶辫触:', error)
    message.error(t('system.encodeRule.generateFailed'))
  }
}

// 鍒濆鍖?
onMounted(() => {
  // 鍙互鍦ㄨ繖閲屽姞杞介澶栫殑瀛楀吀鏁版嵁
})
</script>

<style scoped lang="less">
.encode-rule-management {
  /* 绉婚櫎 padding: 16px锛堢幇鍦ㄧ敱 MainLayout 鐨?.fx-content-inner 缁熶竴澶勭悊锛?*/
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
