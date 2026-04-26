/**
 * 鐟欐帟澹婇懣婊冨礋閹哄牊娼堟い鐢告桨
 * 
 * 閸旂喕鍏橀敍?
 * 1. 閹稿膩閸ф鐫嶇粈楦垮綅閸楁洘鐖茶ぐ銏ｃ€冮弽?
 * 2. 閺€顖涘瘮閸曢箖鈧褰嶉崡鏇熸綀闂?
 * 3. 閺€顖涘瘮閹兼粎鍌ㄩ妴浣稿弿闁鈧礁寮介柅澶堚偓浣圭缁岀儤鎼锋担?
 * 4. 娣囨繂鐡ㄧ憴鎺曞閼挎粌宕熼幒鍫熸綀
 * 
 * @author Forgex
 * @version 1.0.0
 */
<template>
  <div class="role-grant-page">
    <!-- 头部面板 -->
    <section class="hero-panel">
      <div>
        <p class="hero-panel__eyebrow">{{ $t('system.role.menuGrant') }}</p>
        <h2 class="hero-panel__title">{{ roleName }}</h2>
        <p class="hero-panel__desc">{{ $t('system.role.menuGrantDesc') }}</p>
      </div>
      <div class="hero-panel__tabs">
        <a-tabs v-model:activeKey="activeTerminal" @change="handleTerminalChange">
          <a-tab-pane key="B" tab="B端菜单" />
          <a-tab-pane key="C" tab="C端菜单" />
        </a-tabs>
      </div>
    </section>

    <!-- 主体区域 -->
    <section class="board">
      <!-- 侧边栏： 濡€虫健缁涙盯鈧?-->
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

      <!-- 内容区域： 閺嶆垵鑸扮悰銊︾壐 -->
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
const terminalModuleSelectedKeys = ref<Record<'B' | 'C', Record<string, string[]>>>({
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
 * 婢跺嫮鎮婄悰銊︾壐閺佺増宓佺拠閿嬬湴
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

    const filteredTree = filterTreeByKeyword(tree || [], searchKeyword.value)
    const normalizedTree = normalizeTreeRows(filteredTree)
    
    allMenus.value = flattenTree(normalizedTree)

    // 姣忔鍔犺浇閮戒粠鍚庣杩斿洖鐨?checked 瀛楁閲嶆柊鍒濆鍖栭€変腑鐘舵€?
    const checkedIds = collectCheckedNodeIds(normalizedTree)
    const cachedKeys = terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value]
    selectedRowKeys.value = cachedKeys ? [...cachedKeys] : checkedIds
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]

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
 * 鏉╁洦鎶ゅ鎻掑瑎闁娈戦懞鍌滃仯
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

/**
 * 缂佺喕顓搁弽鎴ｅΝ閻愯鏆熼柌?
 */
function countTreeNodes(nodes: MenuTreeRecord[] = []): number {
  return nodes.reduce((count, node) => count + 1 + countTreeNodes(node.children || []), 0)
}

/**
 * 鐟欏嫯瀵栭崠鏍ㄧ埐缂佹挻鐎弫鐗堝祦
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
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
  }
}

/**
 * 婢跺嫮鎮婂Ο鈥虫健閸掑洦宕?
 */
async function handleModuleChange(moduleId: string) {
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
  }
  activeModuleId.value = moduleId
  selectedRowKeys.value = terminalModuleSelectedKeys.value[activeTerminal.value][moduleId]
    ? [...terminalModuleSelectedKeys.value[activeTerminal.value][moduleId]]
    : []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

async function handleTerminalChange(terminal: string) {
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
  }
  activeTerminal.value = terminal === 'C' ? 'C' : 'B'
  selectedRowKeys.value = []
  allMenus.value = []
  searchKeyword.value = ''
  await tableRef.value?.refresh?.()
}

/**
 * 閸忋劑鈧?
 */
function handleSelectAll() {
  selectedRowKeys.value = allMenus.value.map(m => String(m.id))
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
  }
}

/**
 * 閸欏秹鈧?
 */
function handleSelectInvert() {
  const allIds = allMenus.value.map(m => String(m.id))
  const selectedSet = new Set(selectedRowKeys.value)
  selectedRowKeys.value = allIds.filter(id => !selectedSet.has(id))
  if (activeModuleId.value) {
    terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
  }
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
 * 娣囨繂鐡ㄩ幒鍫熸綀
 */
async function handleSave() {
  if (!currentTenantId.value || !roleId.value) {
    message.warning(t('system.role.message.missingRoleInfo'))
    return
  }

  try {
    if (activeModuleId.value) {
      terminalModuleSelectedKeys.value[activeTerminal.value][activeModuleId.value] = [...selectedRowKeys.value]
    }
    const menuIds = Array.from(
      new Set(
        Object.values(terminalModuleSelectedKeys.value[activeTerminal.value])
          .flat()
          .filter(id => id !== '')
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

    &__tabs {
      margin-top: 16px;

      :deep(.ant-tabs-nav) {
        margin-bottom: 0;
      }

      :deep(.ant-tabs-tab) {
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


