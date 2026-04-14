<template>
  <div class="user-management">
    <fx-dynamic-table
      ref="tableRef"
      :table-code="'UserTable'"
      :show-query-form="true"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys: selectedRowKeys,
        onChange: handleSelectionChange
      }"
    >
        <!-- 操作按钮 -->
        <template #toolbar>
          <a-space :size="8">
            <a-button
              v-permission="'sys:user:add'"
              type="primary"
              @click="openAddDialog"
            >
              {{ t('system.user.add') }}
            </a-button>
            <a-button
              v-permission="'sys:user:delete'"
              danger
              :disabled="selectedRowKeys.length === 0"
              @click="handleBatchDelete"
            >
              {{ t('common.batchDelete') }}
            </a-button>
            <a-button
              v-permission="'sys:user:export'"
              @click="handleExport"
            >
              {{ t('system.user.export') }}
            </a-button>
          </a-space>
        </template>
        
        <template #avatar="{ record }">
          <a-avatar :src="record.avatar ? (record.avatar.startsWith('http') || record.avatar.startsWith('data:') ? record.avatar : (record.avatar.startsWith('/api') ? record.avatar : '/api' + (record.avatar.startsWith('/') ? '' : '/') + record.avatar)) : ''">
            <template #icon><UserOutlined /></template>
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

        <template #action="{ record }">
          <a-space>
            <a
              v-permission="'sys:user:edit'"
              @click="openEditDialog(record)"
            >
              {{ t('system.user.edit') }}
            </a>
            <a
              v-permission="'sys:user:edit'"
              @click="toggleUser状态(record.id, !record.status)"
            >
              {{ record.status ? t('system.user.statusInactive') : t('system.user.statusActive') }}
            </a>
            <a
              v-permission="'sys:user:resetPwd'"
              @click="confirmResetPassword(record.id)"
            >
              {{ t('system.user.resetPassword') }}
            </a>
            <a
              v-permission="'sys:user:assignRole'"
              @click="openAssignRoleDialog(record)"
            >
              {{ t('system.user.assignRole') }}
            </a>
            <a
              v-permission="'sys:user:delete'"
              style="color: #ff4d4f;"
              @click="handleDelete(record.id)"
            >
              {{ t('system.user.delete') }}
            </a>
          </a-space>
        </template>
      </fx-dynamic-table>
    
    <!-- 用户表单/编辑对话框 -->
    <UserFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :user-id="currentUserId"
      @success="handleFormSuccess"
    />

    <!-- 角色分配对话框 -->
    <UserRoleAssignDialog
      v-model:open="assignRoleDialogVisible"
      :user-id="assignRoleUserId"
      :user-name="assignRoleUserName"
      :user-account="assignRoleUserAccount"
      @success="handleAssignRoleSuccess"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * 用户管理页面
 *
 * 功能：
 * 1. 用户列表查询（分页、搜索）
 * 2. 新增、编辑、删除用户
 * 3. 重置密码、分配角色
 * 4. 批量删除、导出用户
 *
 * @author Forgex
 * @version 1.0.0
 */
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { UserOutlined } from '@ant-design/icons-vue'
import { message, Modal } from 'ant-design-vue'
import UserFormDialog from './components/UserFormDialog.vue'
import UserRoleAssignDialog from './components/UserRoleAssignDialog.vue'
import { userApi } from '@/api/system/user'
import { getDepartmentTree } from '@/api/system/department'
import { listPositions } from '@/api/system/position'
import { getRoleList } from '@/api/system/role'
import type { Department, Position, User } from './types'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { exportUsers } from '@/api/system/user'

// 国际化
const { t } = useI18n()

// 部门树数据
const departmentTreeData = ref<Department[]>([])

// 职位列表
const positionList = ref<Position[]>([])

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const currentUserId = ref<string>()

const assignRoleDialogVisible = ref(false)
const assignRoleUserId = ref<string>()
const assignRoleUserName = ref<string>()
const assignRoleUserAccount = ref<string>()

// 选中的用户ID列表
const selectedRowKeys = ref<string[]>([])



// 表格引用
const tableRef = ref()

// 字典选项配置
const dictOptions = ref<Record<string, any[]>>({
  departmentId: [],
  positionId: [],
  roleId: [],
  role_ids: [],
  status: [
    { label: t('system.user.statusActive'), value: true },
    { label: t('system.user.statusInactive'), value: false }
  ]
})

function normalizeRoleFilterValues(value: unknown): string[] {
  if (value === undefined || value === null || value === '') {
    return []
  }
  const values = Array.isArray(value) ? value : [value]
  const uniqueValues = new Set<string>()
  values.forEach((item) => {
    const normalized = String(item).trim()
    if (normalized) {
      uniqueValues.add(normalized)
    }
  })
  return Array.from(uniqueValues)
}

/**
 * 数据请求函数
 */
const handleRequest = async (payload: { 
  page: { current: number; pageSize: number }; 
  query: Record<string, any>; 
  sorter?: { field?: string; order?: string } 
}) => {
  const query = { ...payload.query }
  const entryDateRange = Array.isArray(query.entryDate) ? query.entryDate : []
  if (entryDateRange.length === 2) {
    query.entryDateStart = String(entryDateRange[0]).slice(0, 10)
    query.entryDateEnd = String(entryDateRange[1]).slice(0, 10)
  }
  delete query.entryDate

  const roleFilterValues = normalizeRoleFilterValues(query.roleIds ?? query.roleId ?? query.role_ids)
  if (roleFilterValues.length > 0) {
    query.roleIds = roleFilterValues
  }
  delete query.roleId
  delete query.role_ids

  const params: any = {
    pageNum: payload.page.current,
    pageSize: payload.page.pageSize,
    ...query,
  }
  
  // 处理排序
  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }
  
  // http拦截器已经返回了data字段
  const data = await userApi.getUserList(params)
  const total = typeof data.total === 'number' ? data.total : parseInt(String(data.total) || '0', 10)
  return { records: data.records || [], total: total }
}

/**
 * 导出用户数据
 */
async function handleExport() {
  try {
    // 获取当前查询条件
    const currentQuery = tableRef.value?.getQuery?.() || {}
    const resp: any = await exportUsers(currentQuery)
    const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `sys-user-${Date.now()}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
  } catch (e) {
    message.error(t('common.failed'))
  }
}

/**
 * 行选择变化
 */
function handleSelectionChange(keys: string[]) {
  selectedRowKeys.value = keys
}

/**
 * 行操作
 */
function handleRowAction(action: string, record: any) {
  // 可根据需要添加行操作处理
}

/**
 * 打开新增弹窗
 */
function openAddDialog() {
  isEdit.value = false
  currentUserId.value = undefined
  dialogVisible.value = true
}

/**
 * 打开编辑弹窗
 */
function openEditDialog(record: any) {
  isEdit.value = true
  currentUserId.value = record.id
  dialogVisible.value = true
}

/**
 * 打开分配角色弹窗
 */
function openAssignRoleDialog(record: any) {
  assignRoleUserId.value = record.id
  assignRoleUserName.value = record.username
  assignRoleUserAccount.value = record.account
  assignRoleDialogVisible.value = true
}

/**
 * 分配角色成功回调
 */
function handleAssignRoleSuccess() {
  // 刷新表格数据
  tableRef.value?.refresh?.()
}

/**
 * 表单提交成功回调
 */
function handleFormSuccess() {
  // 刷新表格数据
  tableRef.value?.refresh?.()
}

/**
 * 删除用户
 */
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

/**
 * 批量删除用户
 */
async function handleBatchDelete() {
  if (selectedRowKeys.value.length === 0) return
  Modal.confirm({
    title: t('common.confirm'),
    content: t('system.user.message.batchDeleteConfirm'),
    onOk: async () => {
      await userApi.batchDeleteUsers(selectedRowKeys.value)
      selectedRowKeys.value = []
      tableRef.value?.refresh?.()
    },
  })
}

/**
 * 重置密码
 */
async function handleResetPassword(id: string) {
  // 实现重置密码逻辑
  // 可参考原本 useUser hook 的实现
}

/**
 * 更新用户状态
 */
async function handleUpdate状态(id: string, status: boolean) {
  // 实现更新状态逻辑
  // 可参考原本 useUser hook 的实现
}

/**
 * 将部门树转换为下拉选项（扁平化）
 */
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

/**
 * 加载部门树数据
 */
async function loadDepartmentTree() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    departmentTreeData.value = []
    dictOptions.value.departmentId = []
    return
  }
  try {
    const result = await getDepartmentTree({ tenantId })
    departmentTreeData.value = result || []
    // 转换为下拉选项
    dictOptions.value.departmentId = flattenDepartmentTree(result || [])
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

/**
 * 加载职位列表
 */
async function loadPositionList() {
  try {
    const result = await listPositions({})
    positionList.value = result || []
    // 转换为下拉选项
    dictOptions.value.positionId = (result || []).map((item: any) => ({
      label: item.positionName,
      value: item.id
    }))
  } catch (error) {
    console.error('加载职位列表失败:', error)
  }
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

async function toggleUser状态(id: string, status: boolean) {
  await userApi.updateUser状态(id, status)
  tableRef.value?.refresh?.()
}

async function loadRoleList() {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    dictOptions.value.roleId = []
    return
  }
  try {
    const result = await getRoleList({ tenantId })
    const roleOptions = (result || []).map((item: any) => ({
      label: [item.roleName, item.roleCode || item.roleKey].filter(Boolean).join(' / '),
      value: item.id
    }))
    dictOptions.value.roleId = roleOptions
    dictOptions.value.role_ids = roleOptions
  } catch (error) {
    console.error('加载角色列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadDepartmentTree()
  loadPositionList()
  loadRoleList()
})
</script>

<style scoped lang="less">
.user-management {
  /* 移除 padding: 16px（现在由 MainLayout 的 .fx-content-inner 统一处理） */
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  overflow: hidden;
  /* 如果你仍想在页面内部加点间距，可以在这里加 margin 或在 FxDynamicTable 外包一层 div 加 padding */
}
</style>
