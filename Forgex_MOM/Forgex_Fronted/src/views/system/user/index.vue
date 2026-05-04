<template>
  <div class="user-management">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'UserTable'"
      :show-query-form="true"
      :request="handleRequest"
      :dynamic-table-config="dynamicTableConfig"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys,
        onChange: handleSelectionChange,
      }"
    >
      <template #toolbar>
        <a-space :size="8" wrap>
          <a-button data-guide-id="sys-user-add" v-permission="'sys:user:add'" type="primary" @click="openAddDialog">
            {{ t('system.user.add') }}
          </a-button>
          <a-button data-guide-id="sys-user-sync-third-party" v-permission="'sys:user:syncThirdParty'" @click="handleSyncThirdParty">
            同步第三方
          </a-button>
          <a-button data-guide-id="sys-user-pull-third-party" v-permission="'sys:user:pullThirdParty'" @click="handlePullThirdParty">
            从第三方拉取
          </a-button>
          <a-button data-guide-id="sys-user-import" v-permission="'sys:user:import'" @click="importDialogVisible = true">
            {{ t('system.excel.commonImport.title') }}
          </a-button>
          <a-button
            data-guide-id="sys-user-batch-delete"
            v-permission="'sys:user:batchDelete'"
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
          >
            {{ t('common.batchDelete') }}
          </a-button>
          <a-button data-guide-id="sys-user-export" v-permission="'sys:user:export'" @click="handleExport">
            {{ t('system.user.export') }}
          </a-button>
        </a-space>
      </template>

      <template #avatar="{ record }">
        <a-avatar :src="normalizeMediaUrl(record.avatar)">
          <template #icon>
            <UserOutlined />
          </template>
        </a-avatar>
      </template>

      <template #role_ids="{ record }">
        <a-space v-if="Array.isArray(record.roleNames) && record.roleNames.length > 0" wrap :size="[4, 4]">
          <a-tag v-for="roleName in record.roleNames" :key="`${record.id}-${roleName}`">
            {{ roleName }}
          </a-tag>
        </a-space>
        <span v-else>-</span>
      </template>

      <template #roleId="{ record }">
        <a-space v-if="Array.isArray(record.roleNames) && record.roleNames.length > 0" wrap :size="[4, 4]">
          <a-tag v-for="roleName in record.roleNames" :key="`${record.id}-legacy-${roleName}`">
            {{ roleName }}
          </a-tag>
        </a-space>
        <span v-else>-</span>
      </template>

      <template #status="{ record }">
        <a-tag :color="normalizeUserStatus(record.status) ? 'success' : 'default'">
          {{ record.statusText || (normalizeUserStatus(record.status) ? t('system.user.statusActive') : t('system.user.statusInactive')) }}
        </a-tag>
      </template>

      <template #userSource="{ record }">
        <a-tag>{{ record.userSourceText || getUserSourceLabel(record.userSource) || '-' }}</a-tag>
      </template>

      <template #action="{ record }">
        <div class="user-action-cell">
          <FxActionGroup :actions="getUserRowActions(record)" :max-inline="5" />
        </div>
      </template>
    </fx-dynamic-table>

    <UserFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :user-id="currentUserId"
      @success="handleFormSuccess"
    />

    <UserRoleAssignDialog
      v-model:open="assignRoleDialogVisible"
      :user-id="assignRoleUserId"
      :user-name="assignRoleUserName"
      :user-account="assignRoleUserAccount"
      @success="handleAssignRoleSuccess"
    />

    <CommonImportDialog
      v-model:open="importDialogVisible"
      table-code="sys_user"
      @success="handleImportSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { Modal, message } from 'ant-design-vue'
import { UserOutlined } from '@ant-design/icons-vue'
import { normalizeMediaUrl } from '@/utils/media'
import FxActionGroup, { type ActionItem } from '@/components/common/FxActionGroup.vue'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import CommonImportDialog from '@/components/excel/CommonImportDialog.vue'
import UserFormDialog from './components/UserFormDialog.vue'
import UserRoleAssignDialog from './components/UserRoleAssignDialog.vue'
import { useDict, getDictItemLabel } from '@/hooks/useDict'
import { getDepartmentTree } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getRoleList } from '@/api/system/role'
import { exportUsers, userApi } from '@/api/system/user'
import { downloadBlobResponse, normalizeUserQuery, normalizeUserStatus } from '@/utils/user'
import type { FxTableConfig } from '@/api/system/tableConfig'
import type { Department, Position, UserQuery } from './types'

const { t } = useI18n()
const { dictItems: userSourceOptions } = useDict('user_source')

const departmentTreeData = ref<Department[]>([])
const positionList = ref<Position[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref<string>()
const assignRoleDialogVisible = ref(false)
const assignRoleUserId = ref<string>()
const assignRoleUserName = ref<string>()
const assignRoleUserAccount = ref<string>()
const selectedRowKeys = ref<string[]>([])
const tableRef = ref()
const importDialogVisible = ref(false)

const dictOptions = ref<Record<string, any[]>>({
  departmentId: [],
  positionId: [],
  roleId: [],
  role_ids: [],
  userSource: [],
  status: [
    { label: t('system.user.statusActive'), value: true },
    { label: t('system.user.statusInactive'), value: false },
  ],
})

const dynamicTableConfig = computed<Partial<FxTableConfig>>(() => ({
  tableCode: 'UserTable',
  columns: [
    { field: 'action', title: t('common.action'), width: 260, align: 'center', fixed: 'right' },
  ],
}))

watch(userSourceOptions, (value) => {
  dictOptions.value.userSource = value || []
}, { immediate: true })

/**
 * 根据字典配置获取用户来源显示文本，未匹配时回退为默认占位。
 */
function getUserSourceLabel(value: unknown) {
  return getDictItemLabel(userSourceOptions.value, value, '')
}

const handleRequest = async (payload: {
  page: { current: number; pageSize: number }
  query: Record<string, any>
  sorter?: { field?: string; order?: string }
}) => {
  const query = normalizeUserQuery(payload.query) as Partial<UserQuery>
  const params: any = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...query,
  }
  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }

  const data = await userApi.getUserList(params)
  const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
  return { records: data.records || [], total }
}

async function handleExport() {
  try {
    // 导出时沿用动态表格当前筛选条件。
    const currentQuery = normalizeUserQuery(tableRef.value?.getQuery?.() || {}) as Partial<UserQuery>
    const resp: any = await exportUsers(currentQuery)
    downloadBlobResponse(resp, `sys-user-${Date.now()}.xlsx`)
  } catch {
    message.error(t('common.failed'))
  }
}

async function handleSyncThirdParty() {
  await userApi.syncThirdParty('sys_user_sync')
}

async function handlePullThirdParty() {
  await userApi.pullFromThirdParty('sys_user_pull')
  tableRef.value?.refresh?.()
}

function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

function handleImportSuccess() {
  tableRef.value?.refresh?.()
}

function openAddDialog() {
  isEdit.value = false
  currentUserId.value = undefined
  dialogVisible.value = true
}

function openEditDialog(record: any) {
  isEdit.value = true
  currentUserId.value = record.id
  dialogVisible.value = true
}

function openAssignRoleDialog(record: any) {
  assignRoleUserId.value = record.id
  assignRoleUserName.value = record.username
  assignRoleUserAccount.value = record.account
  assignRoleDialogVisible.value = true
}

function getUserRowActions(record: any): ActionItem[] {
  return [
    {
      key: 'edit',
      label: t('system.user.edit'),
      permission: 'sys:user:edit',
      guideId: 'sys-user-row-edit',
      onClick: () => openEditDialog(record),
    },
    {
      key: 'status',
      label: normalizeUserStatus(record.status)
        ? t('system.user.statusInactive')
        : t('system.user.statusActive'),
      permission: 'sys:user:edit',
      guideId: 'sys-user-row-status',
      onClick: () => toggleUserStatus(record.id, !normalizeUserStatus(record.status)),
    },
    {
      key: 'resetPwd',
      label: t('system.user.resetPassword'),
      permission: 'sys:user:resetPwd',
      guideId: 'sys-user-row-reset-password',
      onClick: () => confirmResetPassword(record.id),
    },
    {
      key: 'assignRole',
      label: t('system.user.assignRole'),
      permission: 'sys:user:assignRole',
      guideId: 'sys-user-row-assign-role',
      onClick: () => openAssignRoleDialog(record),
    },
    {
      key: 'delete',
      label: t('system.user.delete'),
      permission: 'sys:user:delete',
      danger: true,
      guideId: 'sys-user-row-delete',
      onClick: () => handleDelete(record.id),
    },
  ]
}

function handleAssignRoleSuccess() {
  tableRef.value?.refresh?.()
}

function handleFormSuccess() {
  tableRef.value?.refresh?.()
}

async function handleDelete(id: string) {
  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.user.message.deleteConfirm'),
    onOk: async () => {
      await userApi.deleteUser(id)
      tableRef.value?.refresh?.()
    },
  })
}

async function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.user.message.batchDeleteConfirm', { count: selectedRowKeys.value.length }),
    onOk: async () => {
      await userApi.batchDeleteUsers(selectedRowKeys.value)
      selectedRowKeys.value = []
      tableRef.value?.refresh?.()
    },
  })
}

function flattenDepartmentTree(tree: Department[], prefix = ''): any[] {
  const result: any[] = []
  for (const node of tree) {
    const label = prefix ? `${prefix} / ${node.deptName}` : node.deptName
    result.push({ label, value: node.id })
    if (node.children && node.children.length > 0) {
      result.push(...flattenDepartmentTree(node.children, label))
    }
  }
  return result
}

async function loadDepartmentTree() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    departmentTreeData.value = []
    dictOptions.value.departmentId = []
    return
  }
  const result = await getDepartmentTree({ tenantId })
  departmentTreeData.value = result || []
  dictOptions.value.departmentId = flattenDepartmentTree(result || [])
}

async function loadPositionList() {
  const result = await listPositions({})
  positionList.value = result || []
  dictOptions.value.positionId = (result || []).map((item: any) => ({
    label: item.positionName,
    value: item.id,
  }))
}

async function confirmResetPassword(id: string) {
  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.user.message.resetPasswordConfirm'),
    onOk: async () => {
      await userApi.resetPassword(id)
    },
  })
}

async function toggleUserStatus(id: string, status: boolean) {
  await userApi.updateUserStatus(id, status)
  tableRef.value?.refresh?.()
}

async function loadRoleList() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    dictOptions.value.roleId = []
    dictOptions.value.role_ids = []
    return
  }
  const result = await getRoleList({ tenantId })
  const roleOptions = (result || []).map((item: any) => ({
    label: [item.roleName, item.roleCode || item.roleKey].filter(Boolean).join(' / '),
    value: item.id,
  }))
  dictOptions.value.roleId = roleOptions
  dictOptions.value.role_ids = roleOptions
}

onMounted(async () => {
  await loadDepartmentTree()
  await loadPositionList()
  await loadRoleList()
})
</script>

<style scoped lang="less" src="@/styles/system-user.less"></style>
