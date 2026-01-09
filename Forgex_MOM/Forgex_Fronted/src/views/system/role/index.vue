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
          <a-button type="primary" @click="openAdd" v-permission="'sys:role:add'">
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

      <a-table
        :columns="columns"
        :data-source="roles"
        :loading="loading"
        :row-selection="{
          selectedRowKeys,
          onChange: onSelectChange
        }"
        row-key="id"
        :pagination="false"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <a-tag :color="record.status === true ? 'green' : 'red'">
              {{ record.status === true ? '启用' : '禁用' }}
            </a-tag>
          </template>
          <template v-else-if="column.key === 'action'">
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
                ok-text="确定"
                cancel-text="取消"
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
        </template>
      </a-table>
    </a-card>

    <!-- 新增/编辑弹窗 -->
    <a-modal
      v-model:open="visible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      :confirm-loading="formLoading"
      @ok="handleSubmit"
      @cancel="handleCancel"
      width="600px"
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
    </a-modal>

    <!-- 菜单授权抽屉 -->
    <a-drawer
      v-model:open="grantVisible"
      :title="`角色菜单授权 - ${grantRole?.roleName || ''}`"
      placement="right"
      width="420"
    >
      <div v-if="!currentTenantId" style="color:#ef4444;">
        当前租户信息缺失，请从登录页重新进入系统。
      </div>
      <div v-else>
        <a-spin :spinning="loadingMenus">
          <div class="toolbar">
            <a-space>
              <a-button size="small" @click="checkAll">全选</a-button>
              <a-button size="small" @click="clearAll">清空</a-button>
            </a-space>
          </div>
          <a-tree
            checkable
            v-model:checkedKeys="checkedKeys"
            :tree-data="treeData"
            :field-names="treeFieldNames"
            :defaultExpandAll="true"
          />
        </a-spin>
        <div class="drawer-footer">
          <a-space>
            <a-button @click="grantVisible = false">取消</a-button>
            <a-button type="primary" :loading="saving" @click="saveGrant">
              保存授权
            </a-button>
          </a-space>
        </div>
      </div>
    </a-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  ReloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'
import { listRoleMenus, grantRoleMenus, getRoleAuthData } from '@/api/sys/role'
import { listMenusTree } from '@/api/sys/menu'
import { useRole } from './hooks/useRole'
import { useRoleForm } from './hooks/useRoleForm'
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

// 表格列定义
const columns = [
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

// 使用Hooks
const {
  loading,
  roles,
  searchForm,
  selectedRowKeys,
  loadRoles,
  handleSearch,
  handleReset,
  handleDelete,
  handleBatchDelete,
  onSelectChange
} = useRole()

const {
  formRef,
  visible,
  loading: formLoading,
  isEdit,
  formData,
  rules,
  openAdd,
  openEdit,
  handleSubmit,
  handleCancel
} = useRoleForm(loadRoles)

// 菜单授权相关
const grantVisible = ref(false)
const grantRole = ref<Role | null>(null)
const loadingMenus = ref(false)
const saving = ref(false)
const menus = ref<MenuEntity[]>([])
const treeData = ref<TreeNode[]>([])
const checkedKeys = ref<string[]>([])
const currentTenantId = ref<string | null>(null)

const treeFieldNames = {
  key: 'key',
  title: 'title',
  children: 'children'
}

/**
 * 打开菜单授权抽屉
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
    // 获取所有菜单树（包括按钮）
    const menuList = await listMenusTree({ tenantId: currentTenantId.value })
    menus.value = Array.isArray(menuList) ? menuList : []
    
    // 调试：打印菜单数据
    console.log('=== 菜单授权调试 ===')
    console.log('总菜单数:', menus.value.length)
    console.log('按钮数量:', menus.value.filter(m => m.type === 'button').length)
    console.log('菜单数量:', menus.value.filter(m => m.type === 'menu').length)
    console.log('目录数量:', menus.value.filter(m => m.type === 'catalog').length)
    console.log('所有菜单数据:', menus.value)
    
    // 构建树形数据
    treeData.value = buildTree(menus.value)
    console.log('树形数据:', treeData.value)
    
    // 获取已授权的菜单ID
    const grantedIds = await listRoleMenus({ 
      tenantId: currentTenantId.value, 
      roleId: grantRole.value.id 
    })
    
    // 设置选中的节点（只选中叶子节点，避免父节点自动选中子节点）
    const leafIds = getLeafNodeIds(menus.value, grantedIds || [])
    checkedKeys.value = leafIds.map((id: any) => String(id))
  } catch (e) {
    message.error('加载菜单授权数据失败')
  } finally {
    loadingMenus.value = false
  }
}

/**
 * 获取叶子节点ID（只返回没有子节点的菜单ID）
 */
function getLeafNodeIds(allMenus: MenuEntity[], grantedIds: any[]): any[] {
  const grantedSet = new Set(grantedIds.map((id: any) => String(id)))
  const parentIds = new Set<string>()
  
  // 找出所有有子节点的父节点
  allMenus.forEach(menu => {
    if (menu.parentId && menu.parentId !== '0') {
      parentIds.add(menu.parentId)
    }
  })
  
  // 只返回叶子节点（没有子节点的节点）
  return allMenus
    .filter(menu => grantedSet.has(String(menu.id)) && !parentIds.has(String(menu.id)))
    .map(menu => menu.id)
}

/**
 * 构建菜单树
 */
function buildTree(list: MenuEntity[]): TreeNode[] {
  const map = new Map<string, TreeNode>()
  const roots: TreeNode[] = []

  list.forEach(m => {
    const node: TreeNode = {
      key: m.id,
      title: m.name
    }
    map.set(m.id, node)
  })

  list.forEach(m => {
    const node = map.get(m.id)!
    const parentId = m.parentId || '0'
    if (parentId === '0') {
      roots.push(node)
    } else {
      const parent = map.get(parentId)
      if (parent) {
        if (!parent.children) {
          parent.children = []
        }
        parent.children.push(node)
      } else {
        roots.push(node)
      }
    }
  })

  return roots
}

/**
 * 全选
 */
function checkAll() {
  checkedKeys.value = menus.value.map(m => m.id)
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

.toolbar {
  margin-bottom: 12px;
}

.drawer-footer {
  margin-top: 16px;
  text-align: right;
}
</style>
