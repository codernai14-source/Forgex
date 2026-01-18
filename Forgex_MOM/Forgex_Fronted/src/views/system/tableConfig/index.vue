<template>
  <div class="table-config-management">
    <a-card :bordered="false" style="margin-bottom: 16px;">
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
            <a-select-option :value="true">{{ t('common.enabled') }}</a-select-option>
            <a-select-option :value="false">{{ t('common.disabled') }}</a-select-option>
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
    
    <a-card :bordered="false">
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
      
      <a-table
        :columns="columns"
        :data-source="dataSource"
        :loading="loading"
        :pagination="pagination"
        :row-key="rowKey"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: handleSelectionChange
        }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'tableNameI18nJson'">
            <span>{{ getTableName(record.tableNameI18nJson) }}</span>
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
import { ref, reactive, onMounted } from 'vue'
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

const { t } = useI18n()

const loading = ref(false)
const dataSource = ref<TableConfigItem[]>([])
const selectedRowKeys = ref<number[]>([])

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
  showTotal: (total: number) => t('common.total', { total })
})

const columns = [
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
    title: t('system.tableConfig.createBy'),
    dataIndex: 'createBy',
    key: 'createBy',
    width: 100
  },
  {
    title: t('system.tableConfig.createTime'),
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
]

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
    pagination.total = result.total
  } catch (error) {
    console.error('获取表格配置列表失败:', error)
  } finally {
    loading.value = false
  }
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

const openEditDialog = (record: TableConfigItem) => {
  isEdit.value = true
  currentConfigId.value = record.id
  dialogVisible.value = true
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

const getTableName = (i18nJson: string | undefined) => {
  if (!i18nJson) return ''
  try {
    const i18nData = JSON.parse(i18nJson)
    return i18nData['zh-CN'] || i18nData['en-US'] || Object.values(i18nData)[0] || ''
  } catch {
    return i18nJson
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.table-config-management {
  padding: 16px;
}
</style>
