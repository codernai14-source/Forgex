<template>
  <div class="table-config-management">
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline">
        <a-form-item :label="t('system.tableConfig.tableCode')">
          <a-input
            v-model:value="queryForm.tableCode"
            :placeholder="t('system.tableConfig.form.tableCode')"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item :label="t('system.tableConfig.tableName')">
          <a-input
            v-model:value="queryForm.tableName"
            :placeholder="t('system.tableConfig.form.tableName')"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item :label="t('system.tableConfig.tableType')">
          <a-select
            v-model:value="queryForm.tableType"
            :placeholder="t('system.tableConfig.form.tableType')"
            allow-clear
            style="width: 150px;"
          >
            <a-select-option value="NORMAL">{{ t('system.tableConfig.tableTypeNormal') }}</a-select-option>
            <a-select-option value="LAZY">{{ t('system.tableConfig.tableTypeLazy') }}</a-select-option>
            <a-select-option value="TREE">{{ t('system.tableConfig.tableTypeTree') }}</a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item :label="t('system.tableConfig.enabled')">
          <a-select
            v-model:value="queryForm.enabled"
            :placeholder="t('system.tableConfig.form.enabled')"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option v-for="option in yesNoOptions" :key="option.value" :value="option.value">
              {{ option.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              {{ t('common.search') }}
            </a-button>
            <a-button @click="handleReset">
              {{ t('common.reset') }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    
    <a-card :bordered="false" class="table-card">
      <div style="margin-bottom: 16px;">
        <a-space>
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
      </div>
      
      <div ref="tableWrapRef" class="table-wrap">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          :scroll="tableScroll"
          :row-key="rowKey"
          :row-selection="{
            selectedRowKeys: selectedRowKeys,
            onChange: handleSelectionChange
          }"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'tableNameI18nJson'">
              <span>{{ getI18nValue(record.tableNameI18nJson, record.tableName) }}</span>
            </template>
            
            <template v-else-if="column.key === 'tableType'">
              <a-tag v-if="record.tableType === 'NORMAL'" color="blue">{{ t('system.tableConfig.tableTypeNormal') }}</a-tag>
              <a-tag v-else-if="record.tableType === 'LAZY'" color="green">{{ t('system.tableConfig.tableTypeLazy') }}</a-tag>
              <a-tag v-else-if="record.tableType === 'TREE'" color="orange">{{ t('system.tableConfig.tableTypeTree') }}</a-tag>
            </template>
            
            <template v-else-if="column.key === 'enabled'">
              <a-switch
                :checked="record.enabled"
                :loading="record.statusLoading"
                @change="(checked: boolean) => handleToggleStatus(record.id!, checked)"
              />
            </template>
            
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a
                  v-permission="'sys:tableConfig:edit'"
                  @click="openEditDialog(record)"
                >
                  {{ t('common.edit') }}
                </a>
                <a
                  v-permission="'sys:tableConfig:delete'"
                  style="color: #ff4d4f;"
                  @click="handleDelete(record.id!)"
                >
                  {{ t('common.delete') }}
                </a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>
    
    <TableConfigFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :config-id="currentConfigId"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 表格配置管理页面
 * 
 * 功能：
 * 1. 表格配置列表查询（分页、搜索）
 * 2. 新增、编辑、删除表格配置
 * 3. 表格配置状态管理
 * 
 * @author Forgex
 * @version 1.0.0
 */
import { ref, reactive, onBeforeUnmount, onMounted, computed, nextTick } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import TableConfigFormDialog from './components/TableConfigFormDialog.vue'
import {
  getTableConfigList,
  deleteTableConfig,
  batchDeleteTableConfig,
  toggleTableConfigStatus
} from '@/api/system/tableConfig'
import type { TableConfigItem } from '@/api/system/tableConfig'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'

// 使用 global 作用域获取全局 i18n 实例，确保国际化生效
const { t } = useI18n({ useScope: 'global' })
const { dictItems: yesNoOptions } = useDict('yes_no')

const loading = ref(false)
const dataSource = ref<TableConfigItem[]>([])
const selectedRowKeys = ref<number[]>([])
const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
let computeScrollYRafPending = false

const queryForm = reactive({
  tableCode: '',
  tableName: '',
  tableType: undefined as string | undefined,
  enabled: undefined as boolean | undefined
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => t('common.total', { total }),
  hideOnSinglePage: false
})

const columns = computed(() => [
  {
    title: t('system.tableConfig.tableCode'),
    dataIndex: 'tableCode',
    key: 'tableCode',
    width: 180
  },
  {
    title: t('system.tableConfig.tableName'),
    dataIndex: 'tableNameI18nJson',
    key: 'tableNameI18nJson',
    width: 200,
    ellipsis: true
  },
  {
    title: t('system.tableConfig.tableType'),
    dataIndex: 'tableType',
    key: 'tableType',
    width: 120
  },
  {
    title: t('system.tableConfig.rowKey'),
    dataIndex: 'rowKey',
    key: 'rowKey',
    width: 120
  },
  {
    title: t('system.tableConfig.defaultPageSize'),
    dataIndex: 'defaultPageSize',
    key: 'defaultPageSize',
    width: 120
  },
  {
    title: t('system.tableConfig.enabled'),
    dataIndex: 'enabled',
    key: 'enabled',
    width: 100,
    align: 'center' as const
  },
  {
    title: t('system.user.createBy'),
    dataIndex: 'createBy',
    key: 'createBy',
    width: 120
  },
  {
    title: t('common.createTime'),
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 150,
    fixed: 'right' as const
  }
])

const tableScrollX = computed(() => {
  return columns.value.reduce((sum: number, col: any) => {
    if (typeof col?.width === 'number' && Number.isFinite(col.width)) {
      return sum + col.width
    }
    return sum + 160
  }, 0)
})

const tableScroll = computed(() => {
  const out: any = { x: tableScrollX.value }
  if (autoScrollY.value !== undefined && autoScrollY.value !== null) {
    out.y = autoScrollY.value
  }
  return out
})

const rowKey = (record: TableConfigItem) => record.id!

const dialogVisible = ref(false)
const isEdit = ref(false)
const currentConfigId = ref<number>()

const fetchData = async () => {
  loading.value = true
  try {
    const params: any = {
      tableCode: queryForm.tableCode || undefined,
      tableName: queryForm.tableName || undefined,
      tableType: queryForm.tableType || undefined,
      enabled: queryForm.enabled !== undefined ? queryForm.enabled : undefined,
      current: pagination.current,
      pageSize: pagination.pageSize
    }
    
    const result = await getTableConfigList(params)
    dataSource.value = result.records
    // 确保 total 是数字类型
    pagination.total = typeof result.total === 'number' ? result.total : parseInt(String(result.total) || '0', 10)
  } catch (error) {
    console.error('获取表格配置列表失败:', error)
  } finally {
    loading.value = false
    await nextTick()
    scheduleComputeAutoScrollY()
  }
}

const onResizeOrScroll = () => {
  void nextTick().then(() => scheduleComputeAutoScrollY())
}

const scheduleComputeAutoScrollY = () => {
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
  if (!Number.isFinite(rect.height)) {
    autoScrollY.value = undefined
    return
  }

  const available = rect.height
  if (!Number.isFinite(available) || available <= 0) {
    autoScrollY.value = undefined
    return
  }

  const paginationEl = wrapEl.querySelector('.ant-pagination') as HTMLElement | null
  const paginationHeight = paginationEl ? paginationEl.getBoundingClientRect().height : 0

  const headerEl = wrapEl.querySelector('.ant-table-thead') as HTMLElement | null
  const headerHeight = headerEl ? headerEl.getBoundingClientRect().height : 0

  // 增加缓冲区，为分页器预留更多空间（margin-top 16px + 额外缓冲 20px）
  const buffer = 36
  const y = Math.floor(available - paginationHeight - headerHeight - buffer)
  const nextY = y > 100 ? y : undefined  // 确保最小高度为100px
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryForm.tableCode = ''
  queryForm.tableName = ''
  queryForm.tableType = undefined
  queryForm.enabled = undefined
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const handleSelectionChange = (keys: number[]) => {
  selectedRowKeys.value = keys
}

const openAddDialog = () => {
  isEdit.value = false
  currentConfigId.value = undefined
  dialogVisible.value = true
}

const openEditDialog = async (record: TableConfigItem) => {
  console.log('打开编辑对话框，记录:', record)
  console.log('记录ID:', record.id)
  
  // 先设置编辑状态和ID
  isEdit.value = true
  currentConfigId.value = record.id
  console.log('设置 currentConfigId:', currentConfigId.value)
  
  // 等待下一个 tick，确保 props 已经更新
  await nextTick()
  
  // 再打开对话框
  dialogVisible.value = true
  console.log('对话框已打开')
}

const handleFormSuccess = () => {
  dialogVisible.value = false
  fetchData()
}

const handleDelete = (id: number) => {
  Modal.confirm({
    title: t('common.confirmDelete'),
    content: t('common.confirmDeleteMessage'),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await deleteTableConfig(id)
        message.success(t('common.deleteSuccess'))
        fetchData()
      } catch (error) {
        console.error('删除表格配置失败:', error)
      }
    }
  })
}

const handleBatchDelete = () => {
  Modal.confirm({
    title: t('common.confirmBatchDelete'),
    content: t('common.confirmBatchDeleteMessage', { count: selectedRowKeys.value.length }),
    okText: t('common.confirm'),
    cancelText: t('common.cancel'),
    onOk: async () => {
      try {
        await batchDeleteTableConfig(selectedRowKeys.value)
        message.success(t('common.deleteSuccess'))
        selectedRowKeys.value = []
        fetchData()
      } catch (error) {
        console.error('批量删除表格配置失败:', error)
      }
    }
  })
}

const handleToggleStatus = async (id: number, enabled: boolean) => {
  const record = dataSource.value.find(item => item.id === id)
  if (record) {
    record.statusLoading = true
    try {
      await toggleTableConfigStatus(id, enabled)
      message.success(t('common.updateSuccess'))
      record.enabled = enabled
    } catch (error) {
      console.error('更新表格配置状态失败:', error)
    } finally {
      record.statusLoading = false
    }
  }
}

// getTableName 函数已被 getI18nValue 替代，已删除

onMounted(() => {
  fetchData()
  window.addEventListener('resize', onResizeOrScroll, { passive: true })
  
  // 添加 MutationObserver 监听 DOM 变化
  const wrapEl = tableWrapRef.value
  if (wrapEl) {
    const observer = new MutationObserver(() => {
      scheduleComputeAutoScrollY()
    })
    
    observer.observe(wrapEl, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['style', 'class']
    })
    
    // 在组件卸载时断开观察
    onBeforeUnmount(() => {
      observer.disconnect()
    })
  }
  
  // 延迟计算，确保 DOM 完全渲染
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 100)
  
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 300)
  
  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 500)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResizeOrScroll as any)
})
</script>

<style scoped>
.table-config-management {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: 16px;
  box-sizing: border-box;
}

.query-card {
  margin-bottom: 16px;
  background: var(--fx-bg-container, #ffffff);
  border-radius: var(--fx-radius-lg, 8px);
}

.query-card :deep(.ant-card-body) {
  padding: 12px;
}

.table-card {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  margin-bottom: 16px;  /* 添加底部间距，避免被 footer 遮挡 */
  background: var(--fx-bg-container, #ffffff);
  border-radius: var(--fx-radius-lg, 8px);
}

.table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
  height: 100%;
  padding: 0;  /* 移除默认 padding */
}

.table-card > div:first-child {
  padding: 12px 16px 0 16px;  /* toolbar 区域的 padding */
}

.table-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 0 16px 24px 16px;  /* 增加底部 padding 到 24px，确保分页器完全显示 */
}

.table-wrap :deep(.ant-table-wrapper) {
  flex: 1 1 auto;
  min-height: 0;
}

.table-wrap :deep(.ant-pagination) {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;  /* 整体右对齐 */
  align-items: center;
  gap: 16px;  /* 元素之间的间距 */
}

.table-wrap :deep(.ant-pagination-total-text) {
  /* 不设置 order，保持默认顺序，显示在页码左边 */
}
</style>
