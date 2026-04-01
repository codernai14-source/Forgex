<template>
  <div class="page-wrap">
    <!-- 操作按钮和表格 -->
    <fx-dynamic-table
      ref="tableRef"
      class="role-table"
      :table-code="'RoleTable'"
      :request="handleRequest"
      :dict-options="dictOptions"
      :row-selection="{
        selectedRowKeys,
        onChange: onSelectChange
      }"
      row-key="id"
    >
      <template #toolbar>
        <a-space>
          <a-button type="primary" @click="openAdd" v-permission="'sys:role:add'">
            <template #icon><PlusOutlined /></template>
            {{ $t('common.add') }}{{ $t('system.role.roleName') }}
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
            v-permission="'sys:role:delete'"
          >
            <template #icon><DeleteOutlined /></template>
            {{ $t('common.batchDelete') }}
          </a-button>
        </a-space>
      </template>

      <template #action="{ record }">
        <a-space>
          <a-button
            type="link"
            size="small"
            @click="openEdit(record)"
            v-permission="'sys:role:edit'"
          >
            {{ $t('common.edit') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="openGrant(record)"
            v-permission="'sys:role:authMenu'"
          >
            {{ $t('system.role.menuAuth') }}
          </a-button>
          <a-button
            type="link"
            size="small"
            @click="openUserGrant(record)"
            v-permission="'sys:role:authUser'"
          >
            {{ $t('system.role.userAuth') }}
          </a-button>
          <a-popconfirm
            :title="$t('system.role.message.deleteConfirm')"
            :ok-text="$t('common.confirm')"
            :cancel-text="$t('common.cancel')"
            @confirm="handleDelete(record.id)"
          >
            <a-button
              type="link"
              size="small"
              danger
              v-permission="'sys:role:delete'"
            >
              {{ $t('common.delete') }}
            </a-button>
          </a-popconfirm>
        </a-space>
      </template>
    </fx-dynamic-table>

    <!-- 新增/编辑弹窗 -->
    <BaseFormDialog
      v-model:open="visible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
    >
      <a-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 16 }"
      >
        <a-form-item label="角色编码" name="roleCode">
          <a-input
            v-model:value="formData.roleCode"
            placeholder="请输入角色编码"
            :disabled="isEdit"
          />
        </a-form-item>
        <a-form-item label="角色名称" name="roleName">
          <a-input
            v-model:value="formData.roleName"
            placeholder="请输入角色名称"
          />
        </a-form-item>
        <a-form-item label="描述" name="description">
          <a-textarea
            v-model:value="formData.description"
            placeholder="请输入描述"
            :rows="4"
          />
        </a-form-item>
        <a-form-item :label="$t('common.status')" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">{{ $t('common.enabled') }}</a-radio>
            <a-radio :value="false">{{ $t('common.disabled') }}</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </BaseFormDialog>

    <!-- 菜单授权弹窗 -->
    <a-modal
      v-model:open="grantVisible"
      :title="`角色菜单授权 - ${grantRole?.roleName || ''}`"
      width="800px"
      :confirm-loading="saving"
      @ok="saveGrant"
      @cancel="grantVisible = false"
    >
      <div v-if="!currentTenantId" style="color:#ef4444;">
        当前租户信息缺失，请从登录页重新进入系统。
      </div>
      <div v-else>
        <a-spin :spinning="loadingMenus">
          <a-row :gutter="16">
            <!-- 左侧：全部权限（可勾选） -->
            <a-col :span="12">
              <a-card title="选择权限" :bordered="false" class="auth-card">
                <template #extra>
                  <a-space>
                    <a-button size="small" @click="checkAll">全选</a-button>
                    <a-button size="small" @click="clearAll">清空</a-button>
                  </a-space>
                </template>
                <div class="tree-container">
                  <a-tree
                    checkable
                    v-model:checkedKeys="checkedKeys"
                    :tree-data="treeData"
                    :field-names="treeFieldNames"
                    :defaultExpandAll="true"
                    :height="400"
                  />
                </div>
              </a-card>
            </a-col>
            
            <!-- 右侧：已拥有权限（只读） -->
            <a-col :span="12">
              <a-card title="已拥有权限" :bordered="false" class="auth-card">
                <div class="tree-container">
                  <a-tree
                    :tree-data="ownedTreeData"
                    :field-names="treeFieldNames"
                    :defaultExpandAll="true"
                    :height="400"
                    :selectable="false"
                  />
                </div>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>
      </div>
    </a-modal>

    <!-- 授权人员弹窗 -->
    <a-modal
      v-model:open="userGrantVisible"
      :title="`角色授权人员 - ${grantRole?.roleName || ''}`"
      width="900px"
      :confirm-loading="userGrantSaving"
      @ok="saveUserGrant"
      @cancel="userGrantVisible = false"
    >
      <div v-if="!currentTenantId" style="color:#ef4444;">
        当前租户信息缺失，请从登录页重新进入系统。
      </div>
      <div v-else>
        <a-spin :spinning="loadingUsers">
          <a-row :gutter="16">
            <!-- 左侧：可授权的用户 -->
            <a-col :span="12">
              <a-card title="可授权用户" :bordered="false" class="auth-card">
                <template #extra>
                  <a-space>
                    <a-button size="small" @click="selectAllUsers">全选</a-button>
                    <a-button size="small" @click="clearSelectedUsers">清空</a-button>
                  </a-space>
                </template>
                <div class="user-list-container">
                  <a-tabs v-model:activeKey="userGrantTab">
                    <a-tab-pane key="user" tab="选择用户">
                      <a-input-search
                        v-model:value="userSearchKeyword"
                        placeholder="搜索用户"
                        allow-clear
                        style="margin-bottom: 8px"
                        @search="searchUsers"
                      />
                      <div class="user-list">
                        <a-checkbox-group v-model:value="selectedUserIds">
                          <div v-for="user in filteredUsers" :key="user.id" class="user-item">
                            <a-checkbox :value="user.id">
                              <div class="user-info">
                                <span class="user-name">{{ user.username }}</span>
                                <span class="user-dept">{{ user.departmentName || '-' }}</span>
                              </div>
                            </a-checkbox>
                          </div>
                        </a-checkbox-group>
                      </div>
                    </a-tab-pane>
                    <a-tab-pane key="department" tab="选择部门">
                      <a-tree
                        checkable
                        v-model:checkedKeys="selectedDepartmentIds"
                        :tree-data="departmentTreeData"
                        :field-names="departmentTreeFieldNames"
                        :defaultExpandAll="true"
                        :height="400"
                      />
                    </a-tab-pane>
                  </a-tabs>
                </div>
              </a-card>
            </a-col>
            
            <!-- 右侧：已授权的用户 -->
            <a-col :span="12">
              <a-card title="已授权用户" :bordered="false" class="auth-card">
                <div class="user-list-container">
                  <div class="user-list">
                    <div v-for="user in authorizedUsers" :key="user.id" class="user-item authorized">
                      <div class="user-info">
                        <span class="user-name">{{ user.username }}</span>
                        <span class="user-dept">{{ user.departmentName || '-' }}</span>
                      </div>
                      <a-button
                        type="link"
                        size="small"
                        danger
                        @click="removeUserGrant(user.id)"
                      >
                        移除
                      </a-button>
                    </div>
                  </div>
                </div>
              </a-card>
            </a-col>
          </a-row>
        </a-spin>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
/**
 * 角色管理页面
 * 
 * 功能：
 * 1. 角色列表查询（分页、搜索）
 * 2. 新增、编辑、删除角色
 * 3. 角色菜单授权
 * 
 * @author Forgex
 * @version 1.0.0
 */
import BaseFormDialog from '@/components/common/BaseFormDialog.vue'
import { computed, onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { grantRoleMenus, getRoleAuthData, getRolePage, deleteRole, batchDeleteRoles, getRoleAuthorizedUsers, grantRoleUsers, revokeRoleUsers } from '@/api/system/role'
import { getUserList } from '@/api/system/user'
import { getDepartmentTree } from '@/api/system/department'
import { useDict } from '@/hooks/useDict'
import type { Role } from './types'

const { dictItems: statusOptions } = useDict('status')
const dictOptions = computed(() => ({ status: statusOptions.value }))

interface MenuEntity {
  id: string
  tenantId: string
  moduleId: string
  parentId: string
  type: string
  path?: string
  name: string
  icon?: string
  componentKey?: string
  permKey?: string
  orderNum?: number
  visible?: number
  status?: number
}

interface TreeNode {
  key: string
  title: string
  children?: TreeNode[]
}

// 选中的行
const selectedRowKeys = ref<string[]>([])

// 表格相关
const tableRef = ref()
const loading = ref(false)

// fallback配置
const fallbackConfig = ref({
  tableCode: 'RoleTable',
  tableName: '角色管理',
  tableType: 'NORMAL',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'roleCode', title: '角色编码', width: 150 },
    { field: 'roleName', title: '角色名称', width: 150 },
    { field: 'description', title: '描述', ellipsis: true },
    { field: 'status', title: '状态', width: 80, dictCode: 'status' },
    { field: 'createTime', title: '创建时间', width: 180 },
    { field: 'createBy', title: '创建人', width: 120 },
    { field: 'updateTime', title: '修改时间', width: 180 },
    { field: 'updateBy', title: '修改人', width: 120 },
    { field: 'action', title: '操作', width: 240 }
  ],
  queryFields: [
    { field: 'roleCode', label: '角色编码', queryType: 'input', queryOperator: 'like' },
    { field: 'roleName', label: '角色名称', queryType: 'input', queryOperator: 'like' },
    { field: 'status', label: '状态', queryType: 'select', queryOperator: 'eq', dictCode: 'status' }
  ],
  version: 1
})

/**
 * 处理表格数据请求
 */
/**
 * 将接口返回的 status（布尔或数字）规范为与字典 {@code status} 一致的 0/1，便于 FxDynamicTable 字典列渲染。
 */
function normalizeRoleStatusRecord(row: any) {
  const s = row?.status
  let num: number
  if (typeof s === 'boolean') {
    num = s ? 1 : 0
  } else if (s === 1 || s === '1' || s === true) {
    num = 1
  } else {
    num = 0
  }
  return { ...row, status: num }
}

const handleRequest = async (params: any) => {
  loading.value = true
  try {
    const res = await getRolePage({
      ...params.query,
      ...params.page
    })
    const records = (res.records || []).map((r: any) => normalizeRoleStatusRecord(r))
    return {
      success: true,
      data: records,
      total: res.total
    }
  } catch (error) {
    message.error('获取角色列表失败')
    return {
      success: false,
      data: [],
      total: 0
    }
  } finally {
    loading.value = false
  }
}

/**
 * 行选择变化
 */
const onSelectChange = (keys: string[]) => {
  selectedRowKeys.value = keys
}

/**
 * 删除角色
 */
const handleDelete = async (id: string) => {
  try {
    await deleteRole(id)
    message.success('删除成功')
    await tableRef.value?.refresh?.()
  } catch (error) {
    message.error('删除失败')
  }
}

/**
 * 批量删除角色
 */
const handleBatchDelete = async () => {
  if (selectedRowKeys.value.length === 0) {
    message.warning('请选择要删除的角色')
    return
  }
  try {
    await batchDeleteRoles(selectedRowKeys.value)
    message.success('批量删除成功')
    await tableRef.value?.refresh?.()
    selectedRowKeys.value = []
  } catch (error) {
    message.error('批量删除失败')
  }
}

// 表单相关
const formRef = ref()
const visible = ref(false)
const formLoading = ref(false)
const isEdit = ref(false)
const formData = ref({
  roleCode: '',
  roleName: '',
  description: '',
  status: true
})

const rules = ref({
  roleCode: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  roleName: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ]
})

/**
 * 打开新增弹窗
 */
const openAdd = () => {
  isEdit.value = false
  formData.value = {
    roleCode: '',
    roleName: '',
    description: '',
    status: true
  }
  visible.value = true
}

/**
 * 打开编辑弹窗
 */
const openEdit = (record: Role) => {
  isEdit.value = true
  formData.value = { ...record }
  visible.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  // 这里需要根据实际情况实现表单提交逻辑
  await tableRef.value?.refresh?.()
  visible.value = false
}

/**
 * 取消表单
 */
const handleCancel = () => {
  visible.value = false
}

// 菜单授权相关
const grantVisible = ref(false)
const grantRole = ref<Role | null>(null)
const loadingMenus = ref(false)
const saving = ref(false)
const allMenus = ref<MenuEntity[]>([])
const treeData = ref<MenuEntity[]>([])
const ownedTreeData = ref<MenuEntity[]>([])
const checkedKeys = ref<string[]>([])
const currentTenantId = ref<string | null>(null)

const treeFieldNames = {
  key: 'id',
  title: 'name',
  children: 'children'
}

/**
 * 打开菜单授权弹窗
 */
async function openGrant(role: Role) {
  grantRole.value = role
  grantVisible.value = true
  await loadMenusAndGrants()
}

/**
 * 加载菜单和已授权的菜单
 */
async function loadMenusAndGrants() {
  if (!currentTenantId.value || !grantRole.value) {
    return
  }
  try {
    loadingMenus.value = true
    
    const res = await getRoleAuthData({ 
      roleId: grantRole.value.id,
      tenantId: currentTenantId.value
    }) as any
    
    const { allPermissions, ownedPermissions, grantedMenuIds } = res
    
    treeData.value = allPermissions || []
    ownedTreeData.value = ownedPermissions || []
    
    allMenus.value = flattenTree(treeData.value)
    
    const leafIds = getLeafNodeIds(allMenus.value, grantedMenuIds || [])
    checkedKeys.value = leafIds.map((id: any) => String(id))
    
  } catch (e) {
    console.error(e)
    message.error('加载菜单授权数据失败')
  } finally {
    loadingMenus.value = false
  }
}

/**
 * 扁平化树结构
 */
function flattenTree(tree: MenuEntity[]): MenuEntity[] {
  let res: MenuEntity[] = []
  tree.forEach(node => {
    res.push(node)
    if (node.children && node.children.length > 0) {
      res = res.concat(flattenTree(node.children))
    }
  })
  return res
}

/**
 * 获取叶子节点ID（只返回没有子节点的菜单ID）
 */
function getLeafNodeIds(allMenus: MenuEntity[], grantedIds: any[]): any[] {
  const grantedSet = new Set(grantedIds.map((id: any) => String(id)))
  
  return allMenus
    .filter(menu => {
      const isGranted = grantedSet.has(String(menu.id))
      const isLeaf = !menu.children || menu.children.length === 0
      return isGranted && isLeaf
    })
    .map(menu => menu.id)
}

/**
 * 全选
 */
function checkAll() {
  checkedKeys.value = allMenus.value.map(m => String(m.id))
}

/**
 * 清空
 */
function clearAll() {
  checkedKeys.value = []
}

/**
 * 保存授权
 */
async function saveGrant() {
  if (!currentTenantId.value || !grantRole.value) {
    message.warning('租户或角色信息缺失')
    return
  }
  try {
    saving.value = true
    const allCheckedKeys = [...checkedKeys.value]
    
    const menuIds = allCheckedKeys.map(id => Number(id))
    
    await grantRoleMenus({
      tenantId: currentTenantId.value,
      roleId: grantRole.value.id,
      menuIds: menuIds
    })
    message.success('授权成功')
    grantVisible.value = false
  } catch (e) {
    message.error('授权失败')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }
  await loadDepartmentTree()
})

// 授权人员相关
const userGrantVisible = ref(false)
const userGrantSaving = ref(false)
const loadingUsers = ref(false)
const userGrantTab = ref('user')
const userSearchKeyword = ref('')
const allUsers = ref<any[]>([])
const selectedUserIds = ref<number[]>([])
const selectedDepartmentIds = ref<string[]>([])
const authorizedUsers = ref<any[]>([])
const departmentTreeData = ref<any[]>([])

const departmentTreeFieldNames = {
  key: 'id',
  title: 'deptName',
  children: 'children'
}

const filteredUsers = computed(() => {
  if (!userSearchKeyword.value) {
    return allUsers.value.filter(user => !authorizedUsers.value.find(auth => auth.id === user.id))
  }
  const keyword = userSearchKeyword.value.toLowerCase()
  return allUsers.value.filter(user => 
    !authorizedUsers.value.find(auth => auth.id === user.id) &&
    (user.username?.toLowerCase().includes(keyword) || user.email?.toLowerCase().includes(keyword))
  )
})

/**
 * 加载部门树
 */
async function loadDepartmentTree() {
  try {
    const result = await getDepartmentTree({ tenantId: currentTenantId.value || '' })
    departmentTreeData.value = result || []
  } catch (error) {
    console.error('加载部门树失败:', error)
  }
}

/**
 * 加载所有用户
 */
async function loadAllUsers() {
  try {
    const result = await getUserList({ 
      tenantId: currentTenantId.value || '',
      pageNum: 1,
      pageSize: 1000
    })
    allUsers.value = result.records || []
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
  }
}

/**
 * 加载已授权的用户
 */
async function loadAuthorizedUsers() {
  if (!currentTenantId.value || !grantRole.value) {
    return
  }
  try {
    loadingUsers.value = true
    const result = await getRoleAuthorizedUsers({
      roleId: grantRole.value.id,
      tenantId: currentTenantId.value,
      pageNum: 1,
      pageSize: 1000
    })
    authorizedUsers.value = result.records || []
  } catch (error) {
    console.error('加载已授权用户失败:', error)
    message.error('加载已授权用户失败')
  } finally {
    loadingUsers.value = false
  }
}

/**
 * 打开授权人员弹窗
 */
async function openUserGrant(role: Role) {
  grantRole.value = role
  userGrantVisible.value = true
  selectedUserIds.value = []
  selectedDepartmentIds.value = []
  userSearchKeyword.value = ''
  await Promise.all([
    loadAllUsers(),
    loadAuthorizedUsers()
  ])
}

/**
 * 搜索用户
 */
function searchUsers() {
}

/**
 * 全选用户
 */
function selectAllUsers() {
  selectedUserIds.value = filteredUsers.value.map(user => user.id)
}

/**
 * 清空选中的用户
 */
function clearSelectedUsers() {
  selectedUserIds.value = []
}

/**
 * 移除用户授权
 */
async function removeUserGrant(userId: number) {
  if (!currentTenantId.value || !grantRole.value) {
    return
  }
  try {
    await revokeRoleUsers({
      roleId: grantRole.value.id,
      tenantId: currentTenantId.value,
      userIds: [userId]
    })
    message.success('移除授权成功')
    await loadAuthorizedUsers()
  } catch (error) {
    console.error('移除授权失败:', error)
    message.error('移除授权失败')
  }
}

/**
 * 保存用户授权
 */
async function saveUserGrant() {
  if (!currentTenantId.value || !grantRole.value) {
    message.warning('租户或角色信息缺失')
    return
  }
  
  let userIdsToAdd: number[] = []
  
  if (userGrantTab.value === 'user') {
    userIdsToAdd = selectedUserIds.value
  } else {
    const deptUsers = await getUsersByDepartments(selectedDepartmentIds.value)
    userIdsToAdd = deptUsers.map(user => user.id)
  }
  
  if (userIdsToAdd.length === 0) {
    message.warning('请选择要授权的用户')
    return
  }
  
  try {
    userGrantSaving.value = true
    await grantRoleUsers({
      roleId: grantRole.value.id,
      tenantId: currentTenantId.value,
      userIds: userIdsToAdd
    })
    message.success('授权成功')
    await loadAuthorizedUsers()
    selectedUserIds.value = []
    selectedDepartmentIds.value = []
  } catch (error) {
    console.error('授权失败:', error)
    message.error('授权失败')
  } finally {
    userGrantSaving.value = false
  }
}

/**
 * 根据部门ID获取用户
 */
async function getUsersByDepartments(deptIds: string[]): Promise<any[]> {
  if (deptIds.length === 0) {
    return []
  }
  
  const users: any[] = []
  for (const deptId of deptIds) {
    const deptUsers = allUsers.value.filter(user => user.departmentId === Number(deptId))
    users.push(...deptUsers)
  }
  
  return users
}
</script>

<style scoped>
.page-wrap {
  padding: 16px;
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.role-table {
  flex: 1;
  min-height: 0;
}

.auth-card {
  height: 100%;
  /* Remove hardcoded background */
  /* background: #f9f9f9; */
}

.tree-container {
  height: 400px;
  overflow-y: auto;
  /* Use theme variable for background or transparent */
  background: var(--component-background);
  padding: 8px;
  border: 1px solid var(--border-color-split);
  border-radius: 4px;
}

.user-list-container {
  height: 400px;
  overflow-y: auto;
  background: var(--component-background);
  padding: 8px;
  border: 1px solid var(--border-color-split);
  border-radius: 4px;
}

.user-list {
  max-height: 340px;
  overflow-y: auto;
}

.user-item {
  padding: 8px;
  border-bottom: 1px solid var(--border-color-split);
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-item:hover {
  background: var(--hover-bg-color);
}

.user-item.authorized {
  background: var(--selected-bg-color);
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-name {
  font-weight: 500;
  color: var(--text-color);
}

.user-dept {
  font-size: 12px;
  color: var(--text-color-secondary);
}
</style>
