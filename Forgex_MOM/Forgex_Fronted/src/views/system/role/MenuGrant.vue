/**
 * 瑙掕壊鑿滃崟鎺堟潈椤甸潰
 * 
 * 鍔熻兘锛?
 * 1. 鎸夋ā鍧楀睍绀鸿彍鍗曟爲褰㈣〃鏍?
 * 2. 鏀寔鍕鹃€夎彍鍗曟潈闄?
 * 3. 鏀寔鎼滅储銆佸叏閫夈€佸弽閫夈€佹竻绌烘搷浣?
 * 4. 淇濆瓨瑙掕壊鑿滃崟鎺堟潈
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
      <!-- Sidebar: 妯″潡绛涢€?-->
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

      <!-- Content Panel: 鏍戝舰琛ㄦ牸 -->
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
          :fallback-config="fallbackConfig"
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
import { getRoleById, getRoleModuleAuthData, grantRoleMenus } from '@/api/system/role'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'
import { useUserStore } from '@/stores/user'
import type { MenuTreeRecord } from './types'

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

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
const moduleSelectedKeys = ref<Record<string, string[]>>({})
const moduleSelectionInitialized = ref<Record<string, boolean>>({})

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
 * 澶勭悊琛ㄦ牸鏁版嵁璇锋眰
 */
async function handleRequest(params: any) {
  if (!currentTenantId.value || !activeModuleId.value || !roleId.value) {
    return { records: [], total: 0 }
  }

  loading.value = true
  try {
    const tree = await getRoleModuleAuthData(Number(activeModuleId.value), {
      roleId: roleId.value,
    })

    const filteredTree = filterTreeByKeyword(tree || [], searchKeyword.value)
    const normalizedTree = normalizeTreeRows(filteredTree)
    
    allMenus.value = flattenTree(normalizedTree)

    // 每次加载都从后端返回的 checked 字段重新初始化选中状态
    const checkedIds = collectCheckedNodeIds(normalizedTree).map((id) => String(id))
    selectedRowKeys.value = checkedIds
    moduleSelectedKeys.value[activeModuleId.value] = [...checkedIds]
    moduleSelectionInitialized.value[activeModuleId.value] = true

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
 * 鏍规嵁鍏抽敭璇嶈繃婊ゆ爲褰㈢粨鏋?
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
 * 杩囨护宸插嬀閫夌殑鑺傜偣
 */
function collectCheckedNodeIds(nodes: MenuTreeRecord[]): number[] {
  const ids: number[] = []
  nodes.forEach((node) => {
    if (node.checked === true && node.id != null) {
      ids.push(Number(node.id))
    }
    if (node.children && node.children.length > 0) {
      ids.push(...collectCheckedNodeIds(node.children))
    }
  })
  return ids
}

/**
 * 鎵佸钩鍖栨爲缁撴瀯
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
 * 缁熻鏍戣妭鐐规暟閲?
 */
function countTreeNodes(nodes: MenuTreeRecord[] = []): number {
  return nodes.reduce((count, node) => count + 1 + countTreeNodes(node.children || []), 0)
}

/**
 * 瑙勮寖鍖栨爲缁撴瀯鏁版嵁
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
 * 澶勭悊琛岄€夋嫨鍙樺寲
 */
function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map(String)
  if (activeModuleId.value) {
    moduleSelectedKeys.value[activeModuleId.value] = [...selectedRowKeys.value]
  }
}

/**
 * 澶勭悊妯″潡鍒囨崲
 */
async function handleModuleChange(moduleId: string) {
  if (activeModuleId.value) {
    moduleSelectedKeys.value[activeModuleId.value] = [...selectedRowKeys.value]
  }
  activeModuleId.value = moduleId
  selectedRowKeys.value = moduleSelectedKeys.value[moduleId] ? [...moduleSelectedKeys.value[moduleId]] : []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

/**
 * 鍏ㄩ€?
 */
function handleSelectAll() {
  selectedRowKeys.value = allMenus.value.map(m => String(m.id))
  if (activeModuleId.value) {
    moduleSelectedKeys.value[activeModuleId.value] = [...selectedRowKeys.value]
  }
}

/**
 * 鍙嶉€?
 */
function handleSelectInvert() {
  const allIds = allMenus.value.map(m => String(m.id))
  const selectedSet = new Set(selectedRowKeys.value)
  selectedRowKeys.value = allIds.filter(id => !selectedSet.has(id))
  if (activeModuleId.value) {
    moduleSelectedKeys.value[activeModuleId.value] = [...selectedRowKeys.value]
  }
}

/**
 * 娓呯┖
 */
function handleClearAll() {
  selectedRowKeys.value = []
  if (activeModuleId.value) {
    moduleSelectedKeys.value[activeModuleId.value] = []
  }
}

/**
 * 鎼滅储
 */
function handleSearch() {
  tableRef.value?.refresh?.()
}

/**
 * 淇濆瓨鎺堟潈
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
 * 鍔犺浇妯″潡鍒楄〃
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
 * 鍔犺浇瑙掕壊淇℃伅
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
  // 优先从 userStore 获取租户 ID，其次从 sessionStorage 获取
  const tid = userStore.tenantId || sessionStorage.getItem('tenantId')
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
  background: var(--fx-bg-layout, #f9fafb);

  .hero-panel {
    margin-bottom: 24px;
    padding: 24px;
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--fx-primary, #1677ff) 72%, #ffffff 28%) 0%,
      color-mix(in srgb, var(--fx-primary-active, #0958d9) 82%, #020617 18%) 100%
    );
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
    background: var(--fx-bg-container, #ffffff);
    border-radius: 8px;
    border: 1px solid var(--fx-border-color, #e5e7eb);
    box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));

    .sidebar {
      width: 280px;
      border-right: 1px solid var(--fx-border-color, #e5e7eb);
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
          color: var(--fx-text-primary, #111827);
        }
      }

      .filter-item {
        display: block;
        width: 100%;
        padding: 12px 16px;
        margin-bottom: 8px;
        border: none;
        background: var(--fx-bg-elevated, #ffffff);
        color: var(--fx-text-primary, #111827);
        border-radius: 4px;
        text-align: left;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
          background: var(--fx-fill, #f3f4f6);
        }

        &--active {
          background: var(--fx-primary, #1677ff);
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
        gap: 12px;

        :deep(.ant-input-search) {
          width: 320px;
          max-width: 50%;
        }
      }

      :deep(.fx-dynamic-table) {
        flex: 1;
        min-height: 0;
      }

      :deep(.ant-table) {
        background: var(--fx-bg-container, #ffffff);
        color: var(--fx-text-primary, #111827);
      }

      :deep(.ant-table-thead > tr > th) {
        background: var(--fx-fill-alter, #f9fafb);
        color: var(--fx-text-secondary, #4b5563);
      }

      :deep(.ant-table-tbody > tr > td) {
        color: var(--fx-text-primary, #111827);
        border-bottom: 1px solid var(--fx-border-secondary, #f3f4f6);
      }

      :deep(.ant-table-tbody > tr:hover > td) {
        background: var(--fx-fill, #f3f4f6);
      }
    }
  }
}
</style>


