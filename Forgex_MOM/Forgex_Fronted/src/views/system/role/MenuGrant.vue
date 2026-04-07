/**
 * 角色菜单授权页面
 * 
 * 功能：
 * 1. 按模块展示菜单树形表格
 * 2. 支持勾选菜单权限
 * 3. 支持搜索、全选、反选、清空操作
 * 4. 保存角色菜单授权
 * 
 * @author Forgex
 * @version 1.0.0
 */
<template>
  <div class="role-grant-page">
    <!-- Hero Panel -->
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ $t('system.role.menuGrant') }}</p>
        <h2 class="hero-panel__title">{{ roleName }}</h2>
        <p class="hero-panel__desc">{{ $t('system.role.menuGrantDesc') }}</p>
      </div>
    </section>

    <!-- Board -->
    <section class="board">
      <!-- Sidebar: 模块筛选 -->
      <aside class="sidebar">
        <div class="panel">
          <div class="panel__title">{{ $t('system.role.moduleFilter') }}</div>
          <button
            v-for="module in modules"
            :key="String(module.id)"
            class="filter-item"
            :class="{ 'filter-item--active': activeModuleId === String(module.id) }"
            @click="handleModuleChange(String(module.id))"
          >
            <span>{{ module.name }}</span>
          </button>
        </div>
      </aside>

      <!-- Content Panel: 树形表格 -->
      <section class="content-panel">
        <div class="toolbar">
          <a-space>
            <a-button type="primary" @click="handleSave">
              <template #icon><SaveOutlined /></template>
              {{ $t('system.role.saveGrant') }}
            </a-button>
            <a-button @click="handleSelectAll">{{ $t('system.role.selectAll') }}</a-button>
            <a-button @click="handleSelectInvert">{{ $t('system.role.selectInvert') }}</a-button>
            <a-button @click="handleClearAll">{{ $t('system.role.clearAll') }}</a-button>
          </a-space>
          <a-input-search
            v-model:value="searchKeyword"
            :placeholder="$t('system.role.searchMenu')"
            @search="handleSearch"
          />
        </div>

        <fx-dynamic-table
          ref="tableRef"
          table-code="RoleMenuGrantTable"
          :request="handleRequest"
          :row-selection="{
            selectedRowKeys,
            onChange: handleSelectionChange
          }"
          row-key="id"
          :pagination="false"
          :default-expand-all-rows="true"
        >
          <template #type="{ record }">
            <a-tag v-if="record.type === 'catalog'" color="blue">{{ $t('system.menu.typeDirectory') }}</a-tag>
            <a-tag v-else-if="record.type === 'menu'" color="green">{{ $t('system.menu.typeMenu') }}</a-tag>
            <a-tag v-else-if="record.type === 'button'" color="orange">{{ $t('system.menu.typeButton') }}</a-tag>
            <span v-else>-</span>
          </template>
          <template #status="{ record }">
            <a-tag
              v-if="resolveStatusTag(record.status)"
              :color="resolveStatusTag(record.status)?.color"
              :style="resolveStatusTag(record.status)?.style"
            >
              {{ resolveStatusTag(record.status)?.label }}
            </a-tag>
            <span v-else>{{ record.status ?? '-' }}</span>
          </template>
        </fx-dynamic-table>
      </section>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { SaveOutlined } from '@ant-design/icons-vue'
import { useI18n } from 'vue-i18n'
import FxDynamicTable from '@/components/common/FxDynamicTable.vue'
import { listModules } from '@/api/system/module'
import { getMenuTree, grantRoleMenus } from '@/api/system/menu'
import { getRoleById } from '@/api/system/role'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'
import type { MenuTreeRecord } from './types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()

const roleId = ref<string>('')
const roleName = ref<string>('')
const modules = ref<any[]>([])
const activeModuleId = ref<string>('')
const tableRef = ref()
const loading = ref(false)
const searchKeyword = ref('')
const selectedRowKeys = ref<string[]>([])
const currentTenantId = ref<string>('')
const allMenus = ref<MenuTreeRecord[]>([])

const { dictItems: statusOptions } = useDict('status')

const fallbackConfig = computed(() => ({
  tableCode: 'RoleMenuGrantTable',
  tableName: t('system.role.menuGrant'),
  tableType: 'TREE',
  rowKey: 'id',
  defaultPageSize: 20,
  columns: [
    { field: 'name', title: t('system.menu.menuName'), minWidth: 200, ellipsis: true },
    { field: 'type', title: t('system.menu.menuType'), width: 100 },
    { field: 'path', title: t('system.menu.path'), minWidth: 180, ellipsis: true },
    { field: 'permKey', title: t('system.menu.permission'), minWidth: 180, ellipsis: true },
    { field: 'status', title: t('system.menu.status'), width: 90, dictCode: 'status' },
  ],
  version: 1,
}))

function normalizeStatusForTag(value: unknown): number {
  if (value === true || value === 1 || value === '1') {
    return 1
  }
  if (value === false || value === 0 || value === '0') {
    return 0
  }
  return value ? 1 : 0
}

function resolveStatusTag(value: unknown) {
  const normalizedValue = normalizeStatusForTag(value)
  const dictItem = statusOptions.value.find((item) => String(item?.value) === String(normalizedValue))
  if (!dictItem) {
    return null
  }

  const style =
    dictItem.tagStyle?.borderColor || dictItem.tagStyle?.backgroundColor
      ? {
          borderColor: dictItem.tagStyle?.borderColor,
          backgroundColor: dictItem.tagStyle?.backgroundColor,
        }
      : undefined

  return {
    label: dictItem.label,
    color: dictItem.tagStyle?.color || dictItem.color || 'blue',
    style,
  }
}

/**
 * 处理表格数据请求
 */
async function handleRequest(params: any) {
  if (!currentTenantId.value || !activeModuleId.value) {
    return { records: [], total: 0 }
  }

  loading.value = true
  try {
    const tree = await getMenuTree({
      tenantId: Number(currentTenantId.value),
      moduleId: Number(activeModuleId.value),
    })

    const filteredTree = filterTreeByKeyword(tree || [], searchKeyword.value)
    const normalizedTree = normalizeTreeRows(filteredTree)
    
    allMenus.value = flattenTree(normalizedTree)
    
    const selectedIds = selectedRowKeys.value.map(id => Number(id))
    const checkedNodes = filterCheckedNodes(normalizedTree, selectedIds)
    selectedRowKeys.value = getLeafNodeIds(checkedNodes).map(id => String(id))

    return {
      records: normalizedTree,
      total: countTreeNodes(normalizedTree),
    }
  } catch (error) {
    console.error('load menu tree failed:', error)
    message.error(t('system.menu.message.loadListFailed'))
    return { records: [], total: 0 }
  } finally {
    loading.value = false
  }
}

/**
 * 根据关键词过滤树形结构
 */
function filterTreeByKeyword(nodes: MenuTreeRecord[], keyword: string): MenuTreeRecord[] {
  if (!keyword) {
    return nodes
  }
  
  const text = keyword.trim().toLowerCase()
  if (!text) {
    return nodes
  }

  return nodes.reduce<MenuTreeRecord[]>((result, node) => {
    const displayName = getI18nValue(node.nameI18nJson, node.name).toLowerCase()
    const match = displayName.includes(text)
    const children = filterTreeByKeyword(node.children || [], keyword)
    
    if (match || children.length > 0) {
      result.push({ ...node, children })
    }
    return result
  }, [])
}

/**
 * 过滤已勾选的节点
 */
function filterCheckedNodes(nodes: MenuTreeRecord[], selectedIds: number[]): MenuTreeRecord[] {
  return nodes.reduce<MenuTreeRecord[]>((result, node) => {
    const isSelected = selectedIds.includes(Number(node.id))
    const children = node.children ? filterCheckedNodes(node.children, selectedIds) : []
    
    if (isSelected || children.length > 0) {
      result.push({ ...node, children })
    }
    return result
  }, [])
}

/**
 * 获取叶子节点 ID
 */
function getLeafNodeIds(nodes: MenuTreeRecord[]): number[] {
  const ids: number[] = []
  nodes.forEach(node => {
    if (!node.children || node.children.length === 0) {
      ids.push(Number(node.id))
    } else {
      ids.push(...getLeafNodeIds(node.children))
    }
  })
  return ids
}

/**
 * 扁平化树结构
 */
function flattenTree(tree: MenuTreeRecord[]): MenuTreeRecord[] {
  let res: MenuTreeRecord[] = []
  tree.forEach(node => {
    res.push(node)
    if (node.children && node.children.length > 0) {
      res = res.concat(flattenTree(node.children))
    }
  })
  return res
}

/**
 * 统计树节点数量
 */
function countTreeNodes(nodes: MenuTreeRecord[] = []): number {
  return nodes.reduce((count, node) => count + 1 + countTreeNodes(node.children || []), 0)
}

/**
 * 规范化树结构数据
 */
function normalizeTreeRows(nodes: MenuTreeRecord[] = []): MenuTreeRecord[] {
  return nodes.map((node) => ({
    ...node,
    id: node.id != null ? String(node.id) : node.id,
    moduleId: node.moduleId != null ? String(node.moduleId) : node.moduleId,
    parentId: node.parentId != null ? String(node.parentId) : '0',
    name: getI18nValue(node.nameI18nJson, node.name),
    status: normalizeStatusForTag(node.status),
    children: normalizeTreeRows(node.children || []),
  }))
}

/**
 * 处理行选择变化
 */
function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map(String)
}

/**
 * 处理模块切换
 */
async function handleModuleChange(moduleId: string) {
  activeModuleId.value = moduleId
  selectedRowKeys.value = []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

/**
 * 全选
 */
function handleSelectAll() {
  selectedRowKeys.value = allMenus.value.map(m => String(m.id))
}

/**
 * 反选
 */
function handleSelectInvert() {
  const allIds = allMenus.value.map(m => String(m.id))
  const selectedSet = new Set(selectedRowKeys.value)
  selectedRowKeys.value = allIds.filter(id => !selectedSet.has(id))
}

/**
 * 清空
 */
function handleClearAll() {
  selectedRowKeys.value = []
}

/**
 * 搜索
 */
function handleSearch() {
  tableRef.value?.refresh?.()
}

/**
 * 保存授权
 */
async function handleSave() {
  if (!currentTenantId.value || !roleId.value) {
    message.warning(t('system.role.message.missingRoleInfo'))
    return
  }

  try {
    const menuIds = selectedRowKeys.value.map(id => Number(id))
    
    await grantRoleMenus({
      roleId: Number(roleId.value),
      tenantId: currentTenantId.value,
      menuIds: menuIds,
    })
    
    message.success(t('system.role.message.saveGrantSuccess'))
    router.back()
  } catch (error: any) {
    console.error('save grant failed:', error)
    if (!error?.code) {
      message.error(t('system.role.message.saveGrantFailed'))
    }
  }
}

/**
 * 加载模块列表
 */
async function loadModules() {
  if (!currentTenantId.value) {
    return
  }
  try {
    const res = await listModules({ tenantId: Number(currentTenantId.value) })
    modules.value = res || []
    if (modules.value.length > 0) {
      activeModuleId.value = String(modules.value[0].id)
      await tableRef.value?.refresh?.()
    }
  } catch (error) {
    console.error('load modules failed:', error)
    message.error(t('system.menu.message.loadModulesFailed'))
  }
}

/**
 * 加载角色信息
 */
async function loadRoleInfo() {
  if (!roleId.value) {
    return
  }
  try {
    const role = await getRoleById(Number(roleId.value))
    roleName.value = role?.roleName || ''
  } catch (error) {
    console.error('load role info failed:', error)
  }
}

onMounted(async () => {
  const tid = sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }

  roleId.value = String(route.params.roleId || '')
  
  await Promise.all([
    loadModules(),
    loadRoleInfo(),
  ])
})
</script>

<style scoped lang="less">
.role-grant-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background: var(--bg-color);

  .hero-panel {
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 8px;
    color: #fff;

    &__eyebrow {
      font-size: 14px;
      opacity: 0.9;
      margin-bottom: 8px;
    }

    &__title {
      font-size: 28px;
      font-weight: 600;
      margin: 0 0 8px 0;
    }

    &__desc {
      font-size: 14px;
      opacity: 0.8;
      margin: 0;
    }
  }

  .board {
    display: flex;
    flex: 1;
    min-height: 0;
    background: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

    .sidebar {
      width: 280px;
      border-right: 1px solid var(--border-color);
      padding: 16px;
      display: flex;
      flex-direction: column;

      .panel {
        flex: 1;
        overflow-y: auto;

        &__title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 16px;
          color: var(--text-color);
        }
      }

      .filter-item {
        display: block;
        width: 100%;
        padding: 12px 16px;
        margin-bottom: 8px;
        border: none;
        background: var(--bg-color);
        border-radius: 4px;
        text-align: left;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: var(--hover-bg-color);
        }

        &--active {
          background: var(--primary-color);
          color: #fff;
        }
      }
    }

    .content-panel {
      flex: 1;
      padding: 16px;
      display: flex;
      flex-direction: column;
      min-width: 0;

      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 16px;
      }

      :deep(.fx-dynamic-table) {
        flex: 1;
        min-height: 0;
      }
    }
  }
}
</style>
