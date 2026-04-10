<template>
  <div class="encode-rule-management">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'EncodeRuleTable'"
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
      
      <!-- 状态列自定义渲染 -->
      <template #status="{ record }">
        <a-tag
          v-if="record.status === 1"
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
      
      <!-- 操作列 -->
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
  
    <!-- 新增/编辑弹窗 -->
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
 * 1. 编码规则列表查询（分页、搜索）
 * 2. 新增、编辑、删除编码规则
 * 3. 生成编码、测试规则
 * 4. 批量删除
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import EncodeRuleFormDialog from './components/EncodeRuleFormDialog.vue'
import { encodeRuleApi } from '@/api/system/encodeRule'
import type { EncodeRule, EncodeRuleQuery } from '@/api/system/encodeRule'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'

// 国际化
const { t } = useI18n()

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentRuleId = ref<string>()

// 选中的规则 ID 列表
const selectedRowKeys = ref<string[]>([])

// 表格引用
const tableRef = ref()

// 字典选项配置
const dictOptions = ref<Record<string, any[]>>({
  status: [
    { label: t('system.encodeRule.statusActive'), value: 1 },
    { label: t('system.encodeRule.statusInactive'), value: 0 }
  ],
  segmentType: [
    { label: t('system.encodeRule.segmentTypeFixed'), value: 'FIXED' },
    { label: t('system.encodeRule.segmentTypeDate'), value: 'DATE' },
    { label: t('system.encodeRule.segmentTypeSeq'), value: 'SEQ' },
    { label: t('system.encodeRule.segmentTypeCustom'), value: 'CUSTOM' }
  ]
})

/**
 * 数据请求函数
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
  
  // 处理排序
  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }
  
  // http 拦截器已经返回了 data 字段
  const data = await encodeRuleApi.pageEncodeRules(params)
  const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
  return { records: data.records || [], total: total }
}

/**
 * 行选择变化
 */
function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

/**
 * 打开新增弹窗
 */
function openAddDialog() {
  isEdit.value = false
  currentRuleId.value = undefined
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗
 */
function openEditDialog(record: EncodeRule) {
  isEdit.value = true
  currentRuleId.value = record.id
  dialogVisible.value = true
}

/**
 * 打开测试对话框
 */
function openTestDialog() {
  // TODO: 实现测试对话框
  message.info(t('system.encodeRule.testNotImplemented'))
}

/**
 * 表单提交成功回调
 */
function handleFormSuccess() {
  // 刷新表格数据
  tableRef.value?.refresh?.()
}

/**
 * 删除编码规则
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
 * 批量删除编码规则
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
 * 生成编码
 */
async function handleGenerateEncode(record: EncodeRule) {
  try {
    const result = await encodeRuleApi.generateEncode({
      ruleCode: record.ruleCode!,
    })
    message.success(t('system.encodeRule.generateSuccess') + ': ' + result.encodeCode)
  } catch (error) {
    console.error('生成编码失败:', error)
    message.error(t('system.encodeRule.generateFailed'))
  }
}

// 初始化
onMounted(() => {
  // 可以在这里加载额外的字典数据
})
</script>

<style scoped lang="less">
.encode-rule-management {
  /* 移除 padding: 16px（现在由 MainLayout 的 .fx-content-inner 统一处理） */
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
