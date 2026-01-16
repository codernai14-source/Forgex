<template>
  <div class="page-wrap">
    <!-- 搜索区域 -->
    <a-card class="search-card" :bordered="false">
      <a-form layout="inline" :model="searchForm">
        <a-form-item label="角色编码">
          <a-input
            v-model:value="searchForm.roleCode"
            placeholder="请输入角色编码"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="角色名称">
          <a-input
            v-model:value="searchForm.roleName"
            placeholder="请输入角色名称"
            allow-clear
            style="width: 200px"
          />
        </a-form-item>
        <a-form-item label="状态">
          <a-select
            v-model:value="searchForm.status"
            placeholder="请选择状态"
            allow-clear
            style="width: 120px"
          >
            <a-select-option :value="true">启用</a-select-option>
            <a-select-option :value="false">禁用</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" @click="handleSearch">
              <template #icon><SearchOutlined /></template>
              搜索
            </a-button>
            <a-button @click="handleReset">
              <template #icon><ReloadOutlined /></template>
              重置
            </a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-card>

    <!-- 操作按钮和表格 -->
    <a-card class="table-card" :bordered="false">
      <template #title>
        <a-space>
          <a-button type="primary" @click="openAdd" v-permission="'sys:role:create'">
            <template #icon><PlusOutlined /></template>
            新增角色
          </a-button>
          <a-button
            danger
            :disabled="selectedRowKeys.length === 0"
            @click="handleBatchDelete"
            v-permission="'sys:role:delete'"
          >
            <template #icon><DeleteOutlined /></template>
            批量删除
          </a-button>
        </a-space>
      </template>

      <fx-dynamic-table
        ref="tableRef"
        :table-code="'RoleTable'"
        :request="handleRequest"
        :fallback-config="fallbackConfig"
        :dict-options="dictOptions"
        :row-selection="{
          selectedRowKeys,
          onChange: onSelectChange
        }"
        row-key="id"
        :pagination="false"
      >
        <template #status="{ record }">
          <a-tag :color="record.status === true ? 'green' : 'red'">
            {{ record.status === true ? '启用' : '禁用' }}
          </a-tag>
        </template>
        <template #action="{ record }">
          <a-space>
            <a-button
              type="link"
              size="small"
              @click="openEdit(record)"
              v-permission="'sys:role:edit'"
            >
              编辑
            </a-button>
            <a-button
              type="link"
              size="small"
              @click="openGrant(record)"
              v-permission="'sys:role:authMenu'"
            >
              菜单授权
            </a-button>
            <a-popconfirm
              title="确定要删除这个角色吗？"
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
                删除
              </a-button>
            </a-popconfirm>
          </a-space>
        </template>
      </fx-dynamic-table>
    </a-card>

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
        <a-form-item label="状态" name="status">
          <a-radio-group v-model:value="formData.status">
            <a-radio :value="true">启用</a-radio>
            <a-radio :value="false">禁用</a-radio>
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
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { grantRoleMenus, getRoleAuthData, listRoles, deleteRole, batchDeleteRoles } from '@/api/system/role'
import type { Role } from './types'

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

// 搜索表单
const searchForm = ref({
  roleCode: '',
  roleName: '',
  status: undefined
})

// 选中的行
const selectedRowKeys = ref<string[]>([])

// 表格相关
const tableRef = ref()
const loading = ref(false)

// fallback配置
const fallbackConfig = ref({
  columns: [
    { title: '角色编码', dataIndex: 'roleCode', key: 'roleCode', width: 150 },
    { title: '角色名称', dataIndex: 'roleName', key: 'roleName', width: 150 },
    { title: '描述', dataIndex: 'description', key: 'description', ellipsis: true },
    { title: '状态', dataIndex: 'status', key: 'status', width: 80 },
    { title: '创建时间', dataIndex: 'createTime', key: 'createTime', width: 180 },
    { title: '创建人', dataIndex: 'createBy', key: 'createBy', width: 120 },
    { title: '修改时间', dataIndex: 'updateTime', key: 'updateTime', width: 180 },
    { title: '修改人', dataIndex: 'updateBy', key: 'updateBy', width: 120 },
    { title: '操作', key: 'action', fixed: 'right', width: 240 }
  ]
})

// 字典配置
const dictOptions = ref({
  status: {
    1: { text: '启用', color: 'green' },
    0: { text: '禁用', color: 'red' }
  }
})

/**
 * 处理表格数据请求
 */
const handleRequest = async (params: any) => {
  loading.value = true
  try {
    const res = await listRoles({
      ...searchForm.value,
      ...params
    })
    return {
      success: true,
      data: res.data,
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
 * 搜索
 */
const handleSearch = async () => {
  await tableRef.value?.refresh()
}

/**
 * 重置搜索
 */
const handleReset = async () => {
  searchForm.value = {
    roleCode: '',
    roleName: '',
    status: undefined
  }
  await tableRef.value?.refresh()
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
    await tableRef.value?.refresh()
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
    await tableRef.value?.refresh()
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
  await tableRef.value?.refresh()
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
// const menus = ref<MenuEntity[]>([]) // Not needed as raw list anymore if using authData directly mapping to tree
const allMenus = ref<MenuEntity[]>([]) // Store flattened menus for leaf calculation
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
    
    // 调用新接口获取授权数据
    const res = await getRoleAuthData({ 
      roleId: grantRole.value.id,
      tenantId: currentTenantId.value
    }) as any
    
    const { allPermissions, ownedPermissions, grantedMenuIds } = res
    
    // 设置树形数据
    treeData.value = allPermissions || []
    ownedTreeData.value = ownedPermissions || []
    
    // 扁平化所有菜单以计算叶子节点 (for checkable tree)
    allMenus.value = flattenTree(treeData.value)
    
    // 设置选中的节点（只选中叶子节点，避免父节点自动选中子节点）
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
  
  // 过滤出被授权且是叶子节点（没有children或children为空）的节点
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
  // 选中所有节点ID
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
    // 获取所有选中的节点（包括半选中的父节点）
    const allCheckedKeys = [...checkedKeys.value]
    
    // 将字符串ID转换为数字
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
  await loadRoles()
})
</script>

<style scoped>
.page-wrap {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.table-card {
  margin-bottom: 16px;
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
</style>
