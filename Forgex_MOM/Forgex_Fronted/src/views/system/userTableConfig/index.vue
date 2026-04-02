<template>
  <div class="user-table-config-management">
    <a-card :bordered="false" class="query-card">
      <a-form layout="inline">
        <a-form-item label="表格编码">
          <a-input
            v-model:value="queryForm.tableCode"
            placeholder="请输入表格编码"
            allow-clear
            style="width: 220px"
          />
        </a-form-item>

        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              {{ t('common.search') }}
            </a-button>
            <a-button @click="handleResetQuery">
              {{ t('common.reset') }}
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <a-card :bordered="false" class="table-card">
      <div class="card-tip">
        <a-alert
          message="说明"
          description="当前页面维护当前登录用户的列偏好设置。页面和按钮是否可见仍然严格取决于角色菜单授权；这里只负责保存已授权页面的个性化列显示。"
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
          :row-key="rowKey"
          :scroll="tableScroll"
          @change="handleTableChange"
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'tableNameI18nJson'">
              <span>{{ getI18nValue(record.tableNameI18nJson, record.tableCode) }}</span>
            </template>

            <template v-else-if="column.key === 'userConfigured'">
              <a-tag :color="record.userConfigured ? 'green' : 'default'">
                {{ record.userConfigured ? '已配置' : '未配置' }}
              </a-tag>
            </template>

            <template v-else-if="column.key === 'userPageSize'">
              <a-tag color="blue">{{ record.userPageSize || record.defaultPageSize || 20 }}</a-tag>
            </template>

            <template v-else-if="column.key === 'action'">
              <a-space>
                <a @click="openEditDialog(record)">
                  {{ t('common.edit') }}
                </a>
                <a
                  :style="{ color: record.userConfigured ? '#ff4d4f' : '#999999' }"
                  @click="handleResetUserConfig(record)"
                >
                  重置
                </a>
              </a-space>
            </template>
          </template>
        </a-table>
      </div>
    </a-card>

    <a-modal
      v-model:open="dialogVisible"
      title="编辑用户列设置"
      width="960px"
      :confirm-loading="saving"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        :model="formData"
        :label-col="{ span: 5 }"
        :wrapper-col="{ span: 17 }"
      >
        <a-form-item label="表格编码">
          <a-input v-model:value="formData.tableCode" disabled />
        </a-form-item>

        <a-form-item label="每页条数">
          <a-input-number
            v-model:value="formData.pageSize"
            :min="1"
            :max="200"
            style="width: 180px"
          />
        </a-form-item>
      </a-form>

      <a-table
        :columns="columnTableColumns"
        :data-source="formData.columns"
        :pagination="false"
        :scroll="{ x: 720, y: 420 }"
        size="small"
        row-key="field"
      >
        <template #bodyCell="{ column, record, index }">
          <template v-if="column.key === 'visible'">
            <a-switch v-model:checked="record.visible" />
          </template>

          <template v-else-if="column.key === 'order'">
            <a-input-number
              v-model:value="record.order"
              :min="1"
              :max="formData.columns.length"
              style="width: 100px"
            />
          </template>

          <template v-else-if="column.key === 'move'">
            <a-space>
              <a-button size="small" :disabled="index === 0" @click="moveColumn(index, -1)">
                上移
              </a-button>
              <a-button
                size="small"
                :disabled="index === formData.columns.length - 1"
                @click="moveColumn(index, 1)"
              >
                下移
              </a-button>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { message, Modal } from 'ant-design-vue'
import {
  getTableConfig,
  getTableConfigList,
  getUserColumns,
  resetUserColumns,
  saveUserColumns,
  type FxTableColumn,
  type TableConfigItem,
  type UserColumnConfigResult,
  type UserColumnItem,
} from '@/api/system/tableConfig'
import { getI18nValue } from '@/utils/i18n'

interface UserTableConfigRow extends TableConfigItem {
  userConfigured: boolean
  userPageSize: number
  userVersion?: number
  userUpdateTime?: string
}

interface EditableColumnItem {
  field: string
  title: string
  visible: boolean
  order: number
}

const { t } = useI18n({ useScope: 'global' })

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const dataSource = ref<UserTableConfigRow[]>([])

const tableWrapRef = ref<HTMLElement | null>(null)
const autoScrollY = ref<number | undefined>(undefined)
let computeScrollYRafPending = false

const queryForm = reactive({
  tableCode: '',
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

const formData = reactive({
  tableCode: '',
  pageSize: 20,
  columns: [] as EditableColumnItem[],
})

const columns = computed(() => [
  {
    title: '表格编码',
    dataIndex: 'tableCode',
    key: 'tableCode',
    width: 220,
  },
  {
    title: '表格名称',
    dataIndex: 'tableNameI18nJson',
    key: 'tableNameI18nJson',
    width: 220,
    ellipsis: true,
  },
  {
    title: '默认每页条数',
    dataIndex: 'defaultPageSize',
    key: 'defaultPageSize',
    width: 130,
    align: 'center' as const,
  },
  {
    title: '用户每页条数',
    key: 'userPageSize',
    width: 130,
    align: 'center' as const,
  },
  {
    title: '配置状态',
    key: 'userConfigured',
    width: 120,
    align: 'center' as const,
  },
  {
    title: '配置版本',
    dataIndex: 'userVersion',
    key: 'userVersion',
    width: 100,
    align: 'center' as const,
  },
  {
    title: '最后更新时间',
    dataIndex: 'userUpdateTime',
    key: 'userUpdateTime',
    width: 180,
  },
  {
    title: t('common.action'),
    key: 'action',
    width: 150,
    fixed: 'right' as const,
  },
])

const columnTableColumns = [
  {
    title: '字段',
    dataIndex: 'field',
    key: 'field',
    width: 180,
  },
  {
    title: '标题',
    dataIndex: 'title',
    key: 'title',
    width: 220,
    ellipsis: true,
  },
  {
    title: '显示',
    key: 'visible',
    width: 90,
    align: 'center' as const,
  },
  {
    title: '排序',
    key: 'order',
    width: 120,
    align: 'center' as const,
  },
  {
    title: '调整顺序',
    key: 'move',
    width: 180,
  },
]

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

const rowKey = (record: UserTableConfigRow) => record.id!

function normalizeColumns(columnsToSort: EditableColumnItem[]) {
  columnsToSort.sort((a, b) => a.order - b.order)
  columnsToSort.forEach((item, index) => {
    item.order = index + 1
  })
}

function buildEditableColumns(baseColumns: FxTableColumn[], userConfig?: UserColumnConfigResult | null) {
  const userColumnMap = new Map<string, FxTableColumn>()
  userConfig?.columns?.forEach((column) => {
    userColumnMap.set(column.field, column)
  })

  const editableColumns = baseColumns.map((column, index) => {
    const userColumn = userColumnMap.get(column.field)
    return {
      field: column.field,
      title: column.title,
      visible: userColumn?.visible ?? column.visible ?? true,
      order: userColumn?.order ?? column.order ?? index + 1,
    }
  })

  normalizeColumns(editableColumns)
  return editableColumns
}

async function loadUserConfigSummary(record: TableConfigItem): Promise<UserTableConfigRow> {
  try {
    const userConfig = await getUserColumns(record.tableCode)
    const userConfigured = Array.isArray(userConfig?.columns) && userConfig.columns.length > 0

    return {
      ...record,
      userConfigured,
      userPageSize: userConfig?.pageSize || record.defaultPageSize || 20,
      userVersion: userConfig?.version,
      userUpdateTime: userConfig?.updateTime,
    }
  } catch (error) {
    console.error('load user table config summary failed', record.tableCode, error)
    return {
      ...record,
      userConfigured: false,
      userPageSize: record.defaultPageSize || 20,
      userVersion: undefined,
      userUpdateTime: undefined,
    }
  }
}

const fetchData = async () => {
  loading.value = true
  try {
    const result = await getTableConfigList({
      tableCode: queryForm.tableCode || undefined,
      current: pagination.current,
      pageSize: pagination.pageSize,
    })

    const records = Array.isArray(result.records) ? result.records : []
    const rows = await Promise.all(records.map(loadUserConfigSummary))

    dataSource.value = rows
    pagination.total =
      typeof result.total === 'number' ? result.total : parseInt(String(result.total || 0), 10)
  } catch (error) {
    console.error('fetch user table config page failed', error)
    dataSource.value = []
    pagination.total = 0
  } finally {
    loading.value = false
    await nextTick()
    scheduleComputeAutoScrollY()
  }
}

const handleSearch = () => {
  pagination.current = 1
  void fetchData()
}

const handleResetQuery = () => {
  queryForm.tableCode = ''
  pagination.current = 1
  void fetchData()
}

const handleTableChange = (pag: any) => {
  pagination.current = pag.current
  pagination.pageSize = pag.pageSize
  void fetchData()
}

const openEditDialog = async (record: UserTableConfigRow) => {
  loading.value = true
  try {
    const [baseConfig, userConfig] = await Promise.all([
      getTableConfig({ tableCode: record.tableCode }),
      getUserColumns(record.tableCode).catch(() => null),
    ])

    if (!baseConfig?.columns?.length) {
      message.error('未找到对应的基础表格配置')
      return
    }

    formData.tableCode = record.tableCode
    formData.pageSize = userConfig?.pageSize || baseConfig.defaultPageSize || 20
    formData.columns = buildEditableColumns(baseConfig.columns, userConfig)
    dialogVisible.value = true
  } catch (error) {
    console.error('open user table config dialog failed', error)
    message.error('加载用户列设置失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!formData.tableCode) {
    message.error('表格编码不能为空')
    return
  }

  if (!formData.columns.length) {
    message.error('列配置不能为空')
    return
  }

  normalizeColumns(formData.columns)

  const payloadColumns: UserColumnItem[] = formData.columns.map((item) => ({
    field: item.field,
    visible: item.visible,
    order: item.order,
  }))

  saving.value = true
  try {
    await saveUserColumns({
      tableCode: formData.tableCode,
      pageSize: formData.pageSize,
      columns: payloadColumns,
    })
    message.success('保存成功')
    dialogVisible.value = false
    await fetchData()
  } catch (error) {
    console.error('save user table config failed', error)
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  dialogVisible.value = false
}

const handleResetUserConfig = (record: UserTableConfigRow) => {
  Modal.confirm({
    title: '确认重置',
    content: `确认重置表格 [${record.tableCode}] 的当前用户列设置吗？重置后将恢复为租户或默认表格配置。`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      try {
        await resetUserColumns(record.tableCode)
        message.success('重置成功')
        await fetchData()
      } catch (error) {
        console.error('reset user table config failed', error)
        message.error('重置失败')
      }
    },
  })
}

const moveColumn = (index: number, direction: -1 | 1) => {
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= formData.columns.length) {
    return
  }

  const [current] = formData.columns.splice(index, 1)
  formData.columns.splice(targetIndex, 0, current)
  normalizeColumns(formData.columns)
}

const onResizeOrScroll = () => {
  void nextTick().then(() => scheduleComputeAutoScrollY())
}

const scheduleComputeAutoScrollY = () => {
  if (computeScrollYRafPending) {
    return
  }
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
  autoScrollY.value = y > 100 ? y : undefined
}

onMounted(() => {
  void fetchData()
  window.addEventListener('resize', onResizeOrScroll, { passive: true })

  setTimeout(() => {
    scheduleComputeAutoScrollY()
  }, 100)
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

.card-tip {
  padding: 16px 16px 0;
}

.table-wrap {
  flex: 1;
  min-height: 0;
  padding: 16px;
}
</style>
