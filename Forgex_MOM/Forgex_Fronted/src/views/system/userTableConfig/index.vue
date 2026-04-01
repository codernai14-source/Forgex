<template>
  <div class="user-table-config-management">
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline">
        <a-form-item label="表格编码">
          <a-input
            v-model:value="queryForm.tableCode"
            placeholder="请输入表格编码"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              查询
            </a-button>
            <a-button @click="handleReset">
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>
    
    <a-card :bordered="false" class="table-card">
      <div style="margin-bottom: 16px;">
        <a-alert
          message="提示"
          description="用户级别表格配置优先级高于租户配置和公共配置。删除用户配置后，将使用租户或公共配置。"
          type="info"
          show-icon
        />
      </div>
      
      <div ref="tableWrapRef" class="table-wrap">
        <a-table
          :columns="columns"
          :data-source="dataSource"
          :loading="loading"
          :pagination="pagination"
          :scroll="tableScroll"
          :row-key="rowKey"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'pageSize'">
              <a-tag color="blue">{{ record.pageSize || 20 }}</a-tag>
            </template>
            
            <template v-else-if="column.key === 'version'">
              <a-tag>{{ record.version || 1 }}</a-tag>
            </template>
            
            <template v-else-if="column.key === 'action'">
              <a-space>
                <a @click="openEditDialog(record)">
                  编辑
                </a>
                <a
                  style="color: #ff4d4f;"
                  @click="handleDelete(record)"
                >
                  删除配置
                </a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>
    
    <!-- 编辑对话框 -->
    <a-modal
      v-model:open="dialogVisible"
      title="编辑用户表格配置"
      width="1000px"
      :confirm-loading="saving"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="表格编码">
          <a-input v-model:value="formData.tableCode" disabled />
        </a-form-item>
        
        <a-form-item label="分页大小">
          <a-input-number
            v-model:value="formData.pageSize"
            :min="1"
            :max="100"
            style="width: 100%;"
          />
        </a-form-item>
        
        <a-divider orientation="left">列配置</a-divider>
        
        <a-table
          :columns="columnTableColumns"
          :data-source="formData.columns || []"
          :pagination="false"
          :scroll="{ x: 1200 }"
          size="small"
          row-key="field"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'enabled'">
              <a-checkbox v-model:checked="record.enabled" />
            </template>
            
            <template v-else-if="column.key === 'orderNum'">
              <a-input-number v-model:value="record.orderNum" :min="0" style="width: 80px;" />
            </template>
            
            <template v-else-if="column.key === 'width'">
              <a-input-number v-model:value="record.width" :min="1" style="width: 80px;" />
            </template>
            
            <template v-else-if="column.key === 'align'">
              <a-select v-model:value="record.align" style="width: 100px;">
                <a-select-option value="left">left</a-select-option>
                <a-select-option value="center">center</a-select-option>
                <a-select-option value="right">right</a-select-option>
              </a-select>
            </template>
            
            <template v-else-if="column.key === 'fixed'">
              <a-select v-model:value="record.fixed" allow-clear style="width: 100px;">
                <a-select-option value="left">left</a-select-option>
                <a-select-option value="right">right</a-select-option>
              </a-select>
            </template>
          </template>
        </a-table>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 用户级别表格配置管理页面
 * 
 * 功能：
 * 1. 查询用户级别表格配置列表
 * 2. 编辑用户个性化配置
 * 3. 删除用户配置（恢复为租户配置）
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
import { ref, reactive, onBeforeUnmount, onMounted, computed, nextTick } from 'vue'
import { message, Modal } from 'ant-design-vue'
import {
  getUserTableConfig,
  saveUserTableConfig,
  deleteUserTableConfig,
  type FxTableConfig,
  type FxTableColumn
} from '@/api/system/tableConfig'

/**
 * 用户表格配置数据接口
 */
interface UserTableConfigItem {
  /** 表格编码 */
  tableCode: string
  /** 表格名称 */
  tableName?: string
  /** 分页大小 */
  pageSize: number
  /** 版本号 */
  version: number
  /** 列配置 */
  columns?: FxTableColumn[]
  /** 创建时间 */
  createTime?: string
  /** 更新时间 */
  updateTime?: string
}

const loading = ref(false)
const saving = ref(false)
const dataSource = ref<UserTableConfigItem[]>([])
const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
let computeScrollYRafPending = false

const queryForm = reactive({
  tableCode: ''
})

const pagination = reactive({
  current: 1,
  pageSize: 20,
  total: 0,
  showSizeChanger: true,
  showQuickJumper: true,
  showTotal: (total: number) => `共 ${total} 条`,
  hideOnSinglePage: false
})

const columns = computed(() => [
  {
    title: '表格编码',
    dataIndex: 'tableCode',
    key: 'tableCode',
    width: 200
  },
  {
    title: '表格名称',
    dataIndex: 'tableName',
    key: 'tableName',
    width: 200,
    ellipsis: true
  },
  {
    title: '分页大小',
    key: 'pageSize',
    width: 100,
    align: 'center' as const
  },
  {
    title: '版本号',
    key: 'version',
    width: 80,
    align: 'center' as const
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180
  },
  {
    title: '更新时间',
    dataIndex: 'updateTime',
    key: 'updateTime',
    width: 180
  },
  {
    title: '操作',
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

const rowKey = (record: UserTableConfigItem) => record.tableCode

const dialogVisible = ref(false)
const formRef = ref()

const formData = reactive({
  tableCode: '',
  pageSize: 20,
  columns: [] as FxTableColumn[]
})

const columnTableColumns = [
  {
    title: '字段',
    dataIndex: 'field',
    key: 'field',
    width: 150
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 150
  },
  {
    title: '对齐',
    key: 'align',
    width: 100
  },
  {
    title: '宽度',
    key: 'width',
    width: 80
  },
  {
    title: '固定',
    key: 'fixed',
    width: 80
  },
  {
    title: '显示',
    key: 'enabled',
    width: 60,
    align: 'center' as const
  },
  {
    title: '排序',
    key: 'orderNum',
    width: 80
  }
]

/**
 * 获取当前用户 ID 和租户 ID
 */
const getCurrentUserId = () => {
  const userInfo = sessionStorage.getItem('userInfo')
  if (userInfo) {
    try {
      const user = JSON.parse(userInfo)
      return user.id
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
  return null
}

const getCurrentTenantId = () => {
  return parseInt(sessionStorage.getItem('tenantId') || '0')
}

/**
 * 获取用户表格配置列表
 * 
 * 注意：这是一个示例实现，实际需要从后端获取列表
 * 目前通过单个查询模拟列表数据
 */
const fetchData = async () => {
  loading.value = true
  try {
    const userId = getCurrentUserId()
    const tenantId = getCurrentTenantId()
    
    if (!userId || !tenantId) {
      message.error('未登录或租户信息缺失')
      return
    }
    
    // 这里应该调用列表接口，暂时模拟数据
    // 实际应该调用：getUserTableConfigList(params)
    const result = await getUserTableConfig({
      tableCode: queryForm.tableCode || 'ALL',
      tenantId,
      userId
    })
    
    if (result) {
      // 模拟列表数据
      dataSource.value = [{
        tableCode: result.tableCode,
        tableName: '示例表格',
        pageSize: result.defaultPageSize || 20,
        version: result.version || 1,
        columns: result.columns,
        createTime: '2026-04-01 12:00:00',
        updateTime: '2026-04-01 12:00:00'
      }]
      pagination.total = 1
    } else {
      dataSource.value = []
      pagination.total = 0
    }
  } catch (error) {
    console.error('获取用户表格配置列表失败:', error)
    dataSource.value = []
    pagination.total = 0
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

  const buffer = 36
  const y = Math.floor(available - paginationHeight - headerHeight - buffer)
  const nextY = y > 100 ? y : undefined
  if (autoScrollY.value === nextY) return
  autoScrollY.value = nextY
}

const handleSearch = () => {
  pagination.current = 1
  fetchData()
}

const handleReset = () => {
  queryForm.tableCode = ''
  pagination.current = 1
  fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  fetchData()
}

const openEditDialog = async (record: UserTableConfigItem) => {
  const userId = getCurrentUserId()
  const tenantId = getCurrentTenantId()
  
  if (!userId || !tenantId) {
    message.error('未登录或租户信息缺失')
    return
  }
  
  try {
    loading.value = true
    const config = await getUserTableConfig({
      tableCode: record.tableCode,
      tenantId,
      userId
    })
    
    if (config) {
      Object.assign(formData, {
        tableCode: config.tableCode,
        pageSize: config.defaultPageSize || 20,
        columns: config.columns || []
      })
      dialogVisible.value = true
    } else {
      message.warning('未找到该配置')
    }
  } catch (error) {
    console.error('加载配置详情失败:', error)
    message.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  try {
    const userId = getCurrentUserId()
    const tenantId = getCurrentTenantId()
    
    if (!userId || !tenantId) {
      message.error('未登录或租户信息缺失')
      return
    }
    
    saving.value = true
    
    await saveUserTableConfig({
      tableCode: formData.tableCode,
      tenantId,
      userId: userId,
      pageSize: formData.pageSize,
      columnConfig: JSON.stringify(formData.columns)
    })
    
    message.success('保存成功')
    dialogVisible.value = false
    fetchData()
  } catch (error) {
    console.error('保存用户表格配置失败:', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  dialogVisible.value = false
}

const handleDelete = (record: UserTableConfigItem) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除表格 [${record.tableCode}] 的用户配置吗？删除后将恢复使用租户或公共配置。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        const userId = getCurrentUserId()
        const tenantId = getCurrentTenantId()
        
        if (!userId || !tenantId) {
          message.error('未登录或租户信息缺失')
          return
        }
        
        await deleteUserTableConfig({
          tableCode: record.tableCode,
          tenantId,
          userId
        })
        
        message.success('删除成功')
        fetchData()
      } catch (error) {
        console.error('删除用户表格配置失败:', error)
        message.error('删除失败')
      }
    }
  })
}

onMounted(() => {
  fetchData()
  window.addEventListener('resize', onResizeOrScroll, { passive: true })
  
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
    
    onBeforeUnmount(() => {
      observer.disconnect()
    })
  }
  
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
.user-table-config-management {
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
  margin-bottom: 16px;
  background: var(--fx-bg-container, #ffffff);
  border-radius: var(--fx-radius-lg, 8px);
}

.table-card :deep(.ant-card-body) {
  display: flex;
  flex-direction: column;
  min-height: 0;
  flex: 1;
  height: 100%;
  padding: 0;
}

.table-card > div:first-child {
  padding: 12px 16px 0 16px;
}

.table-wrap {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-height: 0;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 0 16px 24px 16px;
}

.table-wrap :deep(.ant-table-wrapper) {
  flex: 1 1 auto;
  min-height: 0;
}

.table-wrap :deep(.ant-pagination) {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
}
</style>
