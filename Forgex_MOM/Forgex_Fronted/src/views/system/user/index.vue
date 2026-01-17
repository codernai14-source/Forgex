<template>
  <div class="user-management">
    <!-- 搜索栏 -->
    <a-card :bordered="false" style="margin-bottom: 16px;">
      <a-form layout="inline">
        <a-form-item :label="t('system.user.username')">
          <a-input
            v-model:value="queryForm.username"
            :placeholder="t('system.user.form.username')"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item :label="t('system.user.phone')">
          <a-input
            v-model:value="queryForm.phone"
            :placeholder="t('system.user.form.phone')"
            allow-clear
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item :label="t('system.user.department')">
          <a-tree-select
            v-model:value="queryForm.departmentId"
            :placeholder="t('system.user.form.department')"
            allow-clear
            tree-default-expand-all
            :tree-data="departmentTreeData"
            :field-names="{ label: 'deptName', value: 'id', children: 'children' }"
            style="width: 200px;"
          />
        </a-form-item>
        
        <a-form-item :label="t('system.user.position')">
          <a-select
            v-model:value="queryForm.positionId"
            :placeholder="t('system.user.form.position')"
            allow-clear
            style="width: 200px;"
          >
            <a-select-option
              v-for="pos in positionList"
              :key="pos.id"
              :value="pos.id"
            >
              {{ pos.positionName }}
            </a-select-option>
          </a-select>
        </a-form-item>
        
        <a-form-item :label="t('system.user.status')">
          <a-select
            v-model:value="queryForm.status"
            :placeholder="t('system.user.form.status')"
            allow-clear
            style="width: 120px;"
          >
            <a-select-option :value="true">{{ t('system.user.statusActive') }}</a-select-option>
            <a-select-option :value="false">{{ t('system.user.statusInactive') }}</a-select-option>
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
    
    <!-- 操作栏 -->
    <a-card :bordered="false">
      <div style="margin-bottom: 16px;">
        <a-space>
          <a-button
            v-permission="'sys:user:create'"
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
      </div>
      
      <!-- 表格 -->
      <fx-dynamic-table
        ref="tableRef"
        :table-code="'UserTable'"
        :show-query-form="false"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :row-selection="{
          selectedRowKeys: selectedRowKeys,
          onChange: handleSelectionChange
        }"
      >
        <template #avatar="{ record }">
          <a-avatar :src="record.avatar ? (record.avatar.startsWith('http') || record.avatar.startsWith('data:') ? record.avatar : (record.avatar.startsWith('/api') ? record.avatar : '/api' + (record.avatar.startsWith('/') ? '' : '/') + record.avatar)) : ''">
            <template #icon><UserOutlined /></template>
          </a-avatar>
        </template>
        
        <template #gender="{ record }">
          <a-tag v-if="record.gender === 1" color="blue">{{ t('system.user.genderOptions.male') }}</a-tag>
          <a-tag v-else-if="record.gender === 2" color="pink">{{ t('system.user.genderOptions.female') }}</a-tag>
          <a-tag v-else color="default">{{ t('system.user.genderOptions.unknown') }}</a-tag>
        </template>
        
        <template #status="{ record }">
          <a-tag v-if="record.status === true" color="success">{{ t('system.user.statusActive') }}</a-tag>
          <a-tag v-else color="error">{{ t('system.user.statusInactive') }}</a-tag>
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
              @click="handleUpdateStatus(record.id, !record.status)"
            >
              {{ record.status ? t('system.user.statusInactive') : t('system.user.statusActive') }}
            </a>
            <a
              v-permission="'sys:user:resetPwd'"
              @click="handleResetPassword(record.id)"
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
    </a-card>
    
    <!-- 新增/编辑弹窗 -->
    <UserFormDialog
      v-model:open="dialogVisible"
      :is-edit="isEdit"
      :user-id="currentUserId"
      @success="handleFormSuccess"
    />

    <!-- 分配角色弹窗 -->
    <UserRoleAssignDialog
      v-model:open="assignRoleDialogVisible"
      :user-id="assignRoleUserId"
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
 * 3. 用户状态管理、密码重置
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

// 选中的用户ID列表
const selectedRowKeys = ref<string[]>([])

// 搜索表单
const queryForm = ref({
  username: '',
  phone: '',
  departmentId: '',
  positionId: '',
  status: undefined
})

// 表格引用
const tableRef = ref()

// 字典选项配置
const dictOptions = ref({
  // 可根据需要添加字典选项
})

// 降级配置
const fallbackConfig = {
  tableCode: 'UserTable',
  tableName: t('system.user.userManagement'),
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'avatar', title: t('system.user.avatar'), width: 80, align: 'center' },
    { field: 'username', title: t('system.user.username'), width: 120 },
    { field: 'email', title: t('system.user.email'), width: 180 },
    { field: 'phone', title: t('system.user.phone'), width: 130 },
    { field: 'gender', title: t('system.user.gender'), width: 80 },
    { field: 'departmentName', title: t('system.user.department'), width: 120 },
    { field: 'positionName', title: t('system.user.position'), width: 120 },
    { field: 'entryDate', title: t('system.user.entryDate'), width: 120 },
    { field: 'status', title: t('system.user.status'), width: 80 },
    { field: 'lastLoginTime', title: t('system.user.lastLoginTime'), width: 180 },
    { field: 'lastLoginIp', title: t('system.user.lastLoginIp'), width: 150 },
    { field: 'lastLoginRegion', title: t('system.user.lastLoginRegion'), width: 150 },
    { field: 'createTime', title: t('system.user.createTime'), width: 180 },
    { field: 'createBy', title: t('system.user.createBy'), width: 100 },
    { field: 'updateTime', title: t('system.user.updateTime'), width: 180 },
    { field: 'updateBy', title: t('system.user.updateBy'), width: 100 },
    { field: 'action', title: t('system.user.action'), width: 260, fixed: 'right' },
  ],
  queryFields: [
    { field: 'username', label: t('system.user.username'), queryType: 'input', queryOperator: 'like' },
    { field: 'phone', label: t('system.user.phone'), queryType: 'input', queryOperator: 'like' },
    { field: 'departmentId', label: t('system.user.department'), queryType: 'treeSelect', queryOperator: 'eq' },
    { field: 'positionId', label: t('system.user.position'), queryType: 'select', queryOperator: 'eq' },
    { field: 'status', label: t('system.user.status'), queryType: 'select', queryOperator: 'eq' },
  ],
  version: 1,
}

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
    ...queryForm.value,
  }
  
  // 处理排序
  if (payload.sorter) {
    params.sortField = payload.sorter.field
    params.sortOrder = payload.sorter.order
  }
  
  // http拦截器已经返回了data字段
  const data = await userApi.getUserList(params)
  return { records: data.records || [], total: data.total || 0 }
}

function handleSearch() {
  tableRef.value?.refresh?.()
}

function handleReset() {
  queryForm.value = {
    username: '',
    phone: '',
    departmentId: '',
    positionId: '',
    status: undefined,
  }
  tableRef.value?.refresh?.()
}

async function handleExport() {
  try {
    const resp: any = await exportUsers(queryForm.value)
    const blob = new Blob([resp.data], { type: resp.headers?.['content-type'] || 'application/octet-stream' })
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `sys-user-${Date.now()}.xlsx`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    window.URL.revokeObjectURL(url)
    message.success(t('common.success'))
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
      message.success(t('system.user.message.deleteSuccess'))
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
      message.success(t('system.user.message.deleteSuccess'))
      tableRef.value?.refresh?.()
    },
  })
}

/**
 * 重置密码
 */
async function handleResetPassword(id: string) {
  // 实现重置密码逻辑
  // 可参考原有useUser hook的实现
}

/**
 * 更新用户状态
 */
async function handleUpdateStatus(id: string, status: boolean) {
  // 实现更新状态逻辑
  // 可参考原有useUser hook的实现
}

/**
 * 加载部门树数据
 */
async function loadDepartmentTree() {
  try {
    const result = await getDepartmentTree({ tenantId: '1' })
    departmentTreeData.value = result || []
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
  } catch (error) {
    console.error('加载职位列表失败:', error)
  }
}

// 初始化
onMounted(() => {
  loadDepartmentTree()
  loadPositionList()
})
</script>

<style scoped lang="less">
.user-management {
  padding: 16px;
}
</style>
