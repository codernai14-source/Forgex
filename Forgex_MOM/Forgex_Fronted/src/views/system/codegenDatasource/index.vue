<!--
 * 代码生成数据源管理页面
 *
 * 功能描述：
 * 1. 维护代码生成专用数据源
 * 2. 支持列表、保存、删除与测试连接
 * 3. 供在线开发页面选择数据源元数据
 *
 * @author Forgex
 * @version 1.0
 * @since 2026-04-21
-->
<template>
  <div class="codegen-datasource-page">
    <FxDynamicTable
      ref="tableRef"
      table-code="CodegenDatasourceTable"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :show-query-form="true"
      :show-column-setting="false"
    >
      <template #toolbar>
        <a-space :size="8">
          <a-button type="primary" @click="openAddDialog">
            {{ t('system.codegenDatasource.add') }}
          </a-button>
        </a-space>
      </template>

      <template #enabled="{ record }">
        <a-tag :color="record.enabled ? 'green' : 'default'">
          {{ record.enabled ? t('common.enabled') : t('common.disabled') }}
        </a-tag>
      </template>

      <template #action="{ record }">
        <a-space>
          <a @click="openEditDialog(record.id)">{{ t('common.edit') }}</a>
          <a @click="handleTestRow(record)">{{ t('system.codegenDatasource.testConnection') }}</a>
          <a style="color: #ff4d4f" @click="handleDelete(record.id)">{{ t('common.delete') }}</a>
        </a-space>
      </template>
    </FxDynamicTable>

    <CodegenDatasourceFormDialog
      v-model:open="dialogVisible"
      :datasource-id="currentId"
      @success="handleDialogSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { Modal } from 'ant-design-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import CodegenDatasourceFormDialog from './components/CodegenDatasourceFormDialog.vue'
import {
  deleteCodegenDatasource,
  getCodegenDatasourcePage,
  testCodegenDatasource,
  type CodegenDatasourceItem,
} from '@/api/system/codegenDatasource'
import type { FxTableConfig } from '@/api/system/tableConfig'

const { t } = useI18n({ useScope: 'global' })
const tableRef = ref()
const dialogVisible = ref(false)
const currentId = ref<number>()

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'CodegenDatasourceTable',
  tableName: t('system.codegenDatasource.title'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'datasourceCode', title: t('system.codegenDatasource.datasourceCode'), width: 160, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'datasourceName', title: t('system.codegenDatasource.datasourceName'), width: 180, queryable: true, queryType: 'input', queryOperator: 'like' },
    { field: 'dbType', title: t('system.codegenDatasource.dbType'), width: 120, queryable: true, queryType: 'input', queryOperator: 'eq' },
    { field: 'jdbcUrl', title: t('system.codegenDatasource.jdbcUrl'), width: 260, ellipsis: true },
    { field: 'username', title: t('system.codegenDatasource.username'), width: 140 },
    { field: 'schemaName', title: t('system.codegenDatasource.schemaName'), width: 140 },
    { field: 'enabled', title: t('common.status'), width: 100, align: 'center' },
    { field: 'remark', title: t('common.remark'), width: 180, ellipsis: true },
    { field: 'updateTime', title: t('common.updateTime'), width: 180, align: 'center' },
    { field: 'action', title: t('common.action'), width: 180, align: 'center', fixed: 'right' },
  ],
  queryFields: [
    { field: 'datasourceCode', label: t('system.codegenDatasource.datasourceCode'), queryType: 'input', queryOperator: 'like' },
    { field: 'datasourceName', label: t('system.codegenDatasource.datasourceName'), queryType: 'input', queryOperator: 'like' },
    { field: 'dbType', label: t('system.codegenDatasource.dbType'), queryType: 'input', queryOperator: 'eq' },
  ],
  version: 1,
}))

/**
 * 表格请求
 */
async function handleRequest(payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
}) {
  const result = await getCodegenDatasourcePage({
    ...payload.query,
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
  })
  return {
    records: result.records || [],
    total: result.total || 0,
  }
}

/**
 * 打开新增弹窗
 */
function openAddDialog() {
  currentId.value = undefined
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗
 */
function openEditDialog(id?: number) {
  currentId.value = id
  dialogVisible.value = true
}

/**
 * 删除数据源
 */
function handleDelete(id?: number) {
  if (!id) {
    return
  }
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('common.confirmDeleteMessage'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      await deleteCodegenDatasource(id)
      tableRef.value?.refresh?.()
    },
  })
}

/**
 * 测试当前行连接
 */
async function handleTestRow(record: CodegenDatasourceItem) {
  await testCodegenDatasource({
    id: record.id,
    jdbcUrl: record.jdbcUrl,
    username: record.username,
    dbType: record.dbType,
  })
}

/**
 * 弹窗保存成功
 */
function handleDialogSuccess() {
  tableRef.value?.refresh?.()
}
</script>

<style scoped lang="less">
.codegen-datasource-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}
</style>
