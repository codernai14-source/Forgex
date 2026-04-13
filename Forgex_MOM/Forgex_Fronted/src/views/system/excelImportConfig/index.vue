<template>
  <div class="excel-import-config">
    <FxDynamicTable
      ref="tableRef"
      table-code="ExcelImportConfigTable"
      :request="handleRequest"
      :降级方案-config="降级方案Config"
      row-key="id"
      :show-query-form="true"
      :pagination="{
        showSizeChanger: true,
        showQuickJumper: true,
        showTotal: (total: number) => t('common.total', { total }),
      }"
    >
      <template #toolbar>
        <a-button
          type="primary"
          v-permission="'sys:excel:importConfig:edit'"
          @click="openEdit()"
        >
          {{ t('common.add') }}
        </a-button>
      </template>
      
      <template #action="{ record }">
        <a-space>
          <a
            v-permission="'sys:excel:importConfig:edit'"
            @click="openEdit(record.id)"
          >
            {{ t('common.edit') }}
          </a>
          <a
            v-permission="'sys:excel:template:download'"
            @click="handleDownload(record.tableCode)"
          >
            {{ t('system.excel.downloadTemplate') }}
          </a>
          <a
            v-permission="'sys:excel:importConfig:delete'"
            style="color: #ff4d4f"
            @click="handleDelete(record.id)"
          >
            {{ t('common.delete') }}
          </a>
        </a-space>
      </template>
    </FxDynamicTable>
    
    <!-- 缂栬緫寮圭獥 -->
    <ExcelImportConfigModal
      ref="modalRef"
      v-model:open="modalOpen"
      :is-edit="isEdit"
      :data="editData"
      @submit="handleModalSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import type { FxTableConfig } from '@/api/system/tableConfig'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import ExcelImportConfigModal from './components/ExcelImportConfigModal.vue'
import {
  pageImportConfig,
  importConfigDetail,
  saveImportConfig,
  deleteImportConfig,
  downloadTemplate
} from '@/api/system/excel'

/**
 * 瀵煎叆閰嶇疆绠＄悊椤甸潰
 * 
 * 鍔熻兘锛?
 * 1. 瀵煎叆閰嶇疆鍒楄〃鏌ヨ锛堝垎椤点€佹悳绱級
 * 2. 鏂板銆佺紪杈戙€佸垹闄ゅ鍏ラ厤缃?
 * 3. 涓嬭浇瀵煎叆妯℃澘
 * 
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-09
 */

const { t } = useI18n()

// 琛ㄦ牸寮曠敤
const tableRef = ref()

// 寮圭獥鐘舵€?
const modalOpen = ref(false)
const isEdit = ref(false)
const editData = ref<any>({})
const modalRef = ref()

/**
 * 琛ㄦ牸鍒楅厤缃?
 */
const 降级方案Config = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'ExcelImportConfigTable',
  tableName: t('system.excel.importConfigTitle'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    {
      field: 'tableName',
      title: t('system.excel.tableName'),
      minWidth: 180,
      align: 'left'
    },
    {
      field: 'tableCode',
      title: t('system.excel.tableCode'),
      width: 180,
      align: 'left'
    },
    {
      field: 'title',
      title: t('system.excel.title'),
      minWidth: 180,
      align: 'left'
    },
    {
      field: 'subtitle',
      title: t('system.excel.subtitle'),
      minWidth: 180,
      align: 'left'
    },
    {
      field: 'version',
      title: t('system.excel.version'),
      width: 100,
      align: 'center'
    },
    {
      field: 'action',
      title: t('common.action'),
      width: 220,
      align: 'center',
      fixed: 'right'
    }
  ],
  queryFields: [
    {
      field: 'tableName',
      label: t('system.excel.tableName'),
      queryType: 'input',
      queryOperator: 'like'
    },
    {
      field: 'tableCode',
      label: t('system.excel.tableCode'),
      queryType: 'input',
      queryOperator: 'like'
    }
  ],
  version: 1
}))

/**
 * 鏌ヨ鍙傛暟鎻愬彇宸ュ叿鍑芥暟
 */
function pickQueryValue(query: Record<string, any>, keys: string[]): string | undefined {
  for (const key of keys) {
    const value = query?.[key]
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      return String(value).trim()
    }
  }
  return undefined
}

/**
 * 鏁版嵁璇锋眰鍑芥暟
 */
const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  try {
    const tableName = pickQueryValue(payload.query, ['tableName', 'table_name'])
    const tableCode = pickQueryValue(payload.query, ['tableCode', 'table_code'])
    
    const res: any = await pageImportConfig({
      pageNum: payload.page.current,
      pageSize: payload.page.pageSize,
      current: payload.page.current,
      size: payload.page.pageSize,
      tableName,
      tableCode
    })
    
    return {
      records: res.records || [],
      total: res.total || 0
    }
  } catch (error) {
    console.error('鍔犺浇瀵煎叆閰嶇疆鍒楄〃澶辫触:', error)
    return {
      records: [],
      total: 0
    }
  }
}

/**
 * 鎵撳紑缂栬緫寮圭獥
 */
const openEdit = async (id?: number) => {
  isEdit.value = !!id
  editData.value = {}
  
  if (id) {
    try {
      const detail: any = await importConfigDetail({ id })
      editData.value = detail || {}
    } catch (error) {
      console.error('鍔犺浇閰嶇疆璇︽儏澶辫触:', error)
      message.error(t('common.loadFailed'))
      return
    }
  }
  
  modalOpen.value = true
}

/**
 * 寮圭獥鎻愪氦澶勭悊
 */
const handleModalSubmit = async () => {
  try {
    const formData = modalRef.value?.formData
    if (!formData) {
      message.error('琛ㄥ崟鏁版嵁涓虹┖')
      return
    }
    
    // 鏋勫缓淇濆瓨鍙傛暟
    const saveData = {
      id: formData.id,
      tableName: formData.tableName,
      tableCode: formData.tableCode,
      title: formData.title,
      subtitle: formData.subtitle,
      version: formData.version,
      fields: formData.fields.map((field: any) => ({
        fieldName: field.fieldName,
        fieldType: field.fieldType,
        dataSourceConfig: field.dataSourceConfig,
        required: field.required,
        orderNum: field.orderNum
      }))
    }
    
    await saveImportConfig(saveData)
    message.success(t('common.saveSuccess'))
    tableRef.value?.refresh?.()
    modalOpen.value = false
  } catch (error) {
    console.error('淇濆瓨澶辫触:', error)
    message.error(t('system.excel.message.saveConfigFailed'))
  }
}

/**
 * 鍒犻櫎閰嶇疆
 */
const handleDelete = async (id: number) => {
  Modal.confirm({
    title: t('common.confirmDelete'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await deleteImportConfig({ id })
        message.success(t('common.deleted'))
        tableRef.value?.refresh?.()
      } catch (error) {
        console.error('鍒犻櫎澶辫触:', error)
        message.error(t('system.excel.message.deleteConfigFailed'))
      }
    }
  })
}

/**
 * 涓嬭浇妯℃澘
 */
const handleDownload = async (tableCode: string) => {
  try {
    const resp: any = await downloadTemplate({ tableCode })
    const blob = new Blob([resp.data], {
      type: resp.headers?.['content-type'] || 'application/octet-stream'
    })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `import-template-${tableCode}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    message.success(t('system.excel.message.downloadTemplateSuccess'))
  } catch (error) {
    console.error('涓嬭浇妯℃澘澶辫触:', error)
    message.error(t('system.excel.message.downloadTemplateFailed'))
  }
}
</script>

<style scoped lang="less">
.excel-import-config {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}
</style>
