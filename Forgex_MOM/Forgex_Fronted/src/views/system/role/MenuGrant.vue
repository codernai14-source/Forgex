/**
 * 角色菜单授权页面
 * 
 * 閸旂喕鍏橀敍?
 * 1. 閹稿膩閸ф鐫嶇粈楦垮綅閸楁洘鐖茶ぐ銏ｃ€冮弽?
 * 2. 閺€顖涘瘮閸曢箖鈧褰嶉崡鏇熸綀闂?
 * 3. 閺€顖涘瘮閹兼粎鍌ㄩ妴浣稿弿闁鈧礁寮介柅澶堚偓浣圭缁岀儤鎼锋担?
 * 4. 淇濆瓨瑙掕壊鑿滃崟鎺堟潈
 * 
 * @author Forgex
 * @version 1.0.0
 */
<template>
  <div class="role-grant-page">
    <!-- 头部面板 -->
    <section class="hero-panel">
      <div class="hero-panel__main">
        <div class="hero-panel__copy">
          <p class="hero-panel__eyebrow">{{ $t('system.role.menuGrant') }}</p>
          <p class="hero-panel__desc">{{ $t('system.role.menuGrantDesc') }}</p>
        </div>
        <div class="hero-panel__role">
          <span class="hero-panel__role-label">{{ $t('system.role.roleName') }}</span>
          <strong class="hero-panel__role-name">{{ roleName || '-' }}</strong>
        </div>
      </div>
      <div class="hero-panel__tabs">
        <a-tabs :active-key="activeTerminal" @change="handleTerminalChange">
          <a-tab-pane key="B" tab="B端菜单" />
          <a-tab-pane key="C" tab="C端菜单" />
        </a-tabs>
      </div>
    </section>

    <!-- 主体区域 -->
    <section class="board">
      <!-- 侧边栏： 濡€虫健缁涙盯鈧?-->
      <aside class="sidebar" data-guide-id="sys-role-menu-grant-module-filter">
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

      <!-- 内容区域：树形表格 -->
      <section class="content-panel">
        <div class="toolbar">
          <a-space>
            <a-button data-guide-id="sys-role-menu-grant-save" type="primary" @click="handleSave">
              <template #icon><SaveOutlined /></template>
              {{ $t('system.role.saveGrant') }}
            </a-button>
            <a-button data-guide-id="sys-role-menu-grant-select-all" @click="handleSelectAll">{{ $t('system.role.selectAll') }}</a-button>
            <a-button data-guide-id="sys-role-menu-grant-invert" @click="handleSelectInvert">{{ $t('system.role.selectInvert') }}</a-button>
            <a-button data-guide-id="sys-role-menu-grant-clear" @click="handleClearAll">{{ $t('system.role.clearAll') }}</a-button>
          </a-space>
          <a-input-search
            data-guide-id="sys-role-menu-grant-search"
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
            onChange: handleSelectionChange,
            checkStrictly: false
          }"
          row-key="id"
          :pagination="false"
          :show-query-form="false"
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
              v-if="resolve状态Tag(record.status)"
              :color="resolve状态Tag(record.status)?.color"
              :style="resolve状态Tag(record.status)?.style"
            >
              {{ resolve状态Tag(record.status)?.label }}
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
import {
  getRoleById,
  getRoleCModuleAuthData,
  getRoleModuleAuthData,
  grantRoleCMenus,
  grantRoleMenus,
  listRoleCMenus,
  listRoleMenus,
} from '@/api/system/role'
import { useDict } from '@/hooks/useDict'
import { getI18nValue } from '@/utils/i18n'
import { useUserStore } from '@/stores/user'
import type { MenuTreeRecord } from './types'

interface RoleMenuGrantProps {
  roleId?: string | number
  roleName?: string
  tenantId?: string
  embedded?: boolean
}

const props = defineProps<RoleMenuGrantProps>()
const emit = defineEmits<{
  (e: 'saved'): void
  (e: 'back'): void
}>()

const { t } = useI18n()
const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isEmbedded = computed(() => props.embedded === true)

const roleId = ref<string>('')
const roleName = ref<string>('')
const activeTerminal = ref<'B' | 'C'>('B')
const modules = ref<any[]>([])
const activeModuleId = ref<string>('')
const tableRef = ref()
const loading = ref(false)
const searchKeyword = ref('')
const selectedRowKeys = ref<string[]>([])
const currentTenantId = ref<string>('')
const allMenus = ref<MenuTreeRecord[]>([])
const currentModuleTree = ref<MenuTreeRecord[]>([])
const terminalModuleSelectedKeys = ref<Record<'B' | 'C', Record<string, string[]>>>({
  B: {},
  C: {},
})
const terminalModuleKnownMenuKeys = ref<Record<'B' | 'C', Record<string, string[]>>>({
  B: {},
  C: {},
})

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

function normalize状态ForTag(value: unknown): number {
  if (value === true || value === 1 || value === '1') {
    return 1
  }
  if (value === false || value === 0 || value === '0') {
    return 0
  }
  return value ? 1 : 0
}

function resolve状态Tag(value: unknown) {
  const normalizedValue = normalize状态ForTag(value)
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
  if (!currentTenantId.value || !activeModuleId.value || !roleId.value) {
    return { records: [], total: 0 }
  }

  loading.value = true
  try {
    const tree = await (
      activeTerminal.value === 'C' ? getRoleCModuleAuthData : getRoleModuleAuthData
    )(activeModuleId.value, {
      roleId: roleId.value,
    })

    const normalizedTree = normalizeTreeRows(tree || [])
    currentModuleTree.value = normalizedTree
    allMenus.value = flattenTree(normalizedTree)
    terminalModuleKnownMenuKeys.value[activeTerminal.value][activeModuleId.value] = allMenus.value
      .map(menu => String(menu.id ?? ''))
      .filter(id => id !== '')

    const filteredTree = filterTreeByKeyword(normalizedTree, searchKeyword.value)

    // 姣忔鍔犺浇閮戒粠鍚庣杩斿洖鐨?checked 瀛楁閲嶆柊鍒濆鍖栭€変腑鐘舵€?
    const checkedIds = collectCheckedNodeIds(normalizedTree)
    const cachedKeys = terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value]
    selectedRowKeys.value = cachedKeys
      ? expandSelectedKeys(cachedKeys, normalizedTree)
      : expandSelectedKeys(checkedIds, normalizedTree)
    syncActiveModuleSelection()

    return {
      records: filteredTree,
      total: countTreeNodes(filteredTree),
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
 * 閺嶈宓侀崗鎶芥暛鐠囧秷绻冨銈嗙埐瑜般垻绮ㄩ弸?
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
function collectCheckedNodeIds(nodes: MenuTreeRecord[]): string[] {
  const ids: string[] = []
  nodes.forEach((node) => {
    if (node.checked === true && node.id != null) {
      ids.push(String(node.id))
    }
    if (node.children && node.children.length > 0) {
      ids.push(...collectCheckedNodeIds(node.children))
    }
  })
  return ids
}

/**
 * 閹典礁閽╅崠鏍ㄧ埐缂佹挻鐎?
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

function buildNodeMap(tree: MenuTreeRecord[]): Map<string, MenuTreeRecord> {
  const map = new Map<string, MenuTreeRecord>()
  flattenTree(tree).forEach((node) => {
    if (node.id != null) {
      map.set(String(node.id), node)
    }
  })
  return map
}

function collectNodeAndDescendantIds(node?: MenuTreeRecord): string[] {
  if (!node || node.id == null) {
    return []
  }

  const ids = [String(node.id)]
  ;(node.children || []).forEach((child) => {
    ids.push(...collectNodeAndDescendantIds(child))
  })
  return ids
}

function expandSelectedKeys(keys: Array<string | number>, tree: MenuTreeRecord[] = currentModuleTree.value): string[] {
  if (!keys.length) {
    return []
  }

  const nodeMap = buildNodeMap(tree)
  const expandedIds = keys.flatMap((key) => {
    const id = String(key)
    const node = nodeMap.get(id)
    return node ? collectNodeAndDescendantIds(node) : [id]
  })
  return Array.from(new Set(expandedIds.filter(id => id !== '')))
}

function syncActiveModuleSelection(
  keys: Array<string | number> = selectedRowKeys.value,
  terminal: 'B' | 'C' = activeTerminal.value,
  moduleId: string = activeModuleId.value,
  tree: MenuTreeRecord[] = currentModuleTree.value
) {
  if (!moduleId) {
    return
  }

  const expandedKeys = expandSelectedKeys(keys, tree)
  terminalModuleSelectedKeys.value[terminal][moduleId] = expandedKeys

  if (terminal === activeTerminal.value && moduleId === activeModuleId.value) {
    selectedRowKeys.value = expandedKeys
  }
}

/**
 * 缂佺喕顓搁弽鎴ｅΝ閻愯鏆熼柌?
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
    status: normalize状态ForTag(node.status),
    children: normalizeTreeRows(node.children || []),
  }))
}

/**
 * 婢跺嫮鎮婄悰宀勨偓澶嬪閸欐ê瀵?
 */
function handleSelectionChange(keys: Array<string | number>) {
  selectedRowKeys.value = keys.map(String)
  syncActiveModuleSelection()
}

/**
 * 婢跺嫮鎮婂Ο鈥虫健閸掑洦宕?
 */
async function handleModuleChange(moduleId: string) {
  syncActiveModuleSelection()
  activeModuleId.value = moduleId
  currentModuleTree.value = []
  allMenus.value = []
  selectedRowKeys.value = terminalModuleSelectedKeys.value[activeTerminal.value][moduleId]
    ? [...terminalModuleSelectedKeys.value[activeTerminal.value][moduleId]]
    : []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

async function handleTerminalChange(terminal: string) {
  const previousTerminal = activeTerminal.value
  syncActiveModuleSelection(selectedRowKeys.value, previousTerminal)
  const nextTerminal = terminal === 'C' ? 'C' : 'B'
  activeTerminal.value = nextTerminal
  selectedRowKeys.value = activeModuleId.value && terminalModuleSelectedKeys.value[nextTerminal][activeModuleId.value]
    ? [...terminalModuleSelectedKeys.value[nextTerminal][activeModuleId.value]]
    : []
  allMenus.value = []
  currentModuleTree.value = []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

/**
 * 閸忋劑鈧?
 */
function handleSelectAll() {
  selectedRowKeys.value = allMenus.value.map(m => String(m.id))
  syncActiveModuleSelection()
}

/**
 * 閸欏秹鈧?
 */
function handleSelectInvert() {
  const allIds = allMenus.value.map(m => String(m.id))
  const selectedSet = new Set(selectedRowKeys.value)
  selectedRowKeys.value = allIds.filter(id => !selectedSet.has(id))
  syncActiveModuleSelection()
}

/**
 * 濞撳懐鈹?
 */
function handleClearAll() {
  selectedRowKeys.value = []
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = []
  }
}

/**
 * 閹兼粎鍌?
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
    syncActiveModuleSelection()

    const existingMenuIds = await (activeTerminal.value === 'C' ? listRoleCMenus : listRoleMenus)({
      roleId: roleId.value,
      tenantId: currentTenantId.value,
    })
    const managedMenuIds = new Set(
      Object.values(terminalModuleKnownMenuKeys.value[activeTerminal.value])
        .flat()
        .map(String)
    )
    const selectedMenuIds = Object.values(terminalModuleSelectedKeys.value[activeTerminal.value])
      .flat()
      .map(String)
      .filter(id => id !== '')
    const preservedMenuIds = (existingMenuIds || [])
      .map(String)
      .filter(id => id !== '' && !managedMenuIds.has(id))
    const menuIds = Array.from(
      new Set(
        [...preservedMenuIds, ...selectedMenuIds]
      )
    )
    
    await (activeTerminal.value === 'C' ? grantRoleCMenus : grantRoleMenus)({
      roleId: roleId.value,
      tenantId: currentTenantId.value,
      menuIds: menuIds,
    })
    
    emit('saved')
    if (isEmbedded.value) {
      return
    }
    router.back()
  } catch (error: any) {
    console.error('save grant failed:', error)
  }
}

/**
 * 閸旂姾娴囧Ο鈥虫健閸掓銆?
 */
async function loadModules() {
  if (!currentTenantId.value) {
    return
  }
  try {
    const res = await listModules({ tenantId: currentTenantId.value })
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
 * 閸旂姾娴囩憴鎺曞娣団剝浼?
 */
async function loadRoleInfo() {
  if (props.roleName) {
    roleName.value = props.roleName
    return
  }
  if (!roleId.value) {
    return
  }
  try {
    const role = await getRoleById(roleId.value)
    roleName.value = role?.roleName || ''
  } catch (error) {
    console.error('load role info failed:', error)
  }
}

onMounted(async () => {
  const tid = props.tenantId || userStore.tenantId || sessionStorage.getItem('tenantId')
  if (tid) {
    currentTenantId.value = tid
  }

  roleId.value = String(props.roleId ?? route.params.roleId ?? '')
  if (props.roleName) {
    roleName.value = props.roleName
  }
  
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
    margin-bottom: 12px;
    padding: 14px 16px 10px;
    background: linear-gradient(
      135deg,
      color-mix(in srgb, var(--fx-primary, #1677ff) 72%, #ffffff 28%) 0%,
      color-mix(in srgb, var(--fx-primary-active, #0958d9) 82%, #020617 18%) 100%
    );
    border-radius: 8px;
    color: #fff;

    &__main {
      display: flex;
      align-items: center;
      justify-content: space-between;
      gap: 16px;
    }

    &__copy {
      min-width: 0;
    }

    &__eyebrow {
      margin: 0 0 4px;
      font-size: 14px;
      font-weight: 600;
      opacity: 0.9;
    }

    &__desc {
      font-size: 14px;
      opacity: 0.8;
      margin: 0;
    }

    &__role {
      flex: 0 0 auto;
      min-width: 180px;
      max-width: 320px;
      padding: 8px 12px;
      border: 1px solid rgba(255, 255, 255, 0.28);
      border-radius: 6px;
      background: rgba(255, 255, 255, 0.12);
      text-align: right;
    }

    &__role-label {
      display: block;
      margin-bottom: 2px;
      font-size: 12px;
      line-height: 1.3;
      opacity: 0.76;
    }

    &__role-name {
      display: block;
      overflow: hidden;
      color: #fff;
      font-size: 16px;
      line-height: 1.4;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    &__tabs {
      margin-top: 8px;

      :deep(.ant-tabs-nav) {
        margin-bottom: 0;
        &::before {
          border-bottom-color: rgba(255, 255, 255, 0.24);
        }
      }

      :deep(.ant-tabs-tab) {
        padding: 8px 0;
        color: rgba(255, 255, 255, 0.75);
      }

      :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
        color: #fff;
      }
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
      width: 240px;
      border-right: 1px solid var(--fx-border-color, #e5e7eb);
      padding: 12px;
      display: flex;
      flex-direction: column;

      .panel {
        flex: 1;
        overflow-y: auto;

        &__title {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 12px;
          color: var(--fx-text-primary, #111827);
        }
      }

      .filter-item {
        display: block;
        width: 100%;
        padding: 9px 12px;
        margin-bottom: 6px;
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
      padding: 12px;
      display: flex;
      flex-direction: column;
      min-width: 0;

      .toolbar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 10px;
        gap: 12px;

        :deep(.ant-input-search) {
          width: 320px;
          max-width: 50%;
        }
      }

      :deep(.fx-dynamic-table) {
        flex: 1;
        min-height: 0;
        gap: 0;
      }

      :deep(.fx-table-toolbar-row) {
        padding: 6px 8px;
      }

      :deep(.fx-table-card) {
        flex: 1;
        min-height: 0;
      }

      :deep(.fx-table-card > .ant-card-body) {
        height: 100%;
      }

      :deep(.fx-table-content) {
        height: 100%;
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

  @media (max-width: 768px) {
    padding: 12px;

    .hero-panel {
      &__main {
        align-items: stretch;
        flex-direction: column;
        gap: 10px;
      }

      &__role {
        max-width: none;
        text-align: left;
      }
    }

    .board {
      flex-direction: column;

      .sidebar {
        width: 100%;
        border-right: 0;
        border-bottom: 1px solid var(--fx-border-color, #e5e7eb);
      }

      .content-panel {
        .toolbar {
          align-items: stretch;
          flex-direction: column;

          :deep(.ant-input-search) {
            width: 100%;
            max-width: none;
          }
        }
      }
    }
  }
}
</style>
