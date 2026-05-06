<template>
  <div
    class="app-sidebar-wrapper fx-guide-sidebar"
    :class="{ 'app-sidebar-wrapper--vertical': isVerticalLayout }"
  >
    <a-layout-sider
      v-if="doubleColumn"
      class="app-sidebar-mini fx-guide-sidebar-mini"
      :collapsed="false"
      :width="104"
    >
      <a-menu
        mode="inline"
        :selected-keys="selectedFirstLevelKeys"
        class="mini-menu fx-guide-sidebar-mini-menu"
        @click="onFirstLevelMenuClick"
      >
        <a-menu-item
          v-for="menu in firstLevelMenus"
          :key="menu.key"
          class="mini-menu-item"
        >
          <div class="mini-menu-content">
            <span class="mini-menu-icon-shell">
              <component v-if="menu.icon" :is="getIcon(menu.icon)" class="mini-menu-icon" />
              <AppstoreOutlined v-else-if="menu.type === 'module'" class="mini-menu-icon" />
              <FolderOutlined v-else-if="menu.children?.length" class="mini-menu-icon" />
              <FileOutlined v-else class="mini-menu-icon" />
            </span>
            <span class="mini-menu-title" :title="menu.title">{{ menu.title }}</span>
          </div>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <a-layout-sider
      v-if="!doubleColumn || hasSecondLevelMenus"
      class="app-sidebar fx-guide-sidebar-main"
      :collapsed="collapsed"
      :collapsible="true"
      :width="mainSiderWidth"
      :collapsed-width="64"
      @collapse="onCollapse"
    >
      <a-menu
        mode="inline"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        :inline-indent="menuInlineIndent"
        class="sidebar-menu fx-guide-sidebar-menu"
        @openChange="onOpenChange"
        @click="onMenuClick"
      >
        <SidebarMenuNode
          v-for="item in currentMenus"
          :key="item.key"
          :item="item"
        />
      </a-menu>
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { getIcon } from '../../utils/icon'
import {
  AppstoreOutlined,
  FolderOutlined,
  FileOutlined,
} from '@ant-design/icons-vue'
import SidebarMenuNode, { type SidebarMenuNodeItem } from './SidebarMenuNode.vue'

interface MenuItem extends SidebarMenuNodeItem {}

interface Module {
  code: string
  name: string
  icon?: string
  order: number
}

interface AppSidebarProps {
  menus: MenuItem[]
  modules?: Module[]
  activeKey?: string
  activeModuleCode?: string
  layoutMode?: 'vertical' | 'vertical-mix' | 'top' | 'mix'
  collapsed?: boolean
  doubleColumn?: boolean
}

const props = withDefaults(defineProps<AppSidebarProps>(), {
  menus: () => [],
  modules: () => [],
  activeKey: '',
  activeModuleCode: '',
  layoutMode: 'vertical',
  collapsed: false,
  doubleColumn: false,
})

const emit = defineEmits<{
  'menu-click': [menuKey: string]
  'collapse-change': [collapsed: boolean]
}>()

const selectedKeys = ref<string[]>([])
const selectedFirstLevelKeys = ref<string[]>([])
const openKeys = ref<string[]>([])

const isVerticalLayout = computed(() => props.layoutMode === 'vertical' && !props.doubleColumn)
const mainSiderWidth = computed(() => (isVerticalLayout.value ? 220 : 180))
const menuInlineIndent = computed(() => (isVerticalLayout.value ? 12 : 16))

const firstLevelMenus = computed(() => (props.doubleColumn ? props.menus : []))

const selectedFirstLevelMenu = computed(() => (
  firstLevelMenus.value.find(menu => menu.key === selectedFirstLevelKeys.value[0]) || null
))

const hasSecondLevelMenus = computed(() => (
  props.doubleColumn && !!selectedFirstLevelMenu.value?.children?.length
))

const currentMenus = computed(() => {
  if (!props.doubleColumn) {
    return props.menus
  }
  return selectedFirstLevelMenu.value?.children || []
})

watch(
  () => [props.doubleColumn, props.activeModuleCode, props.menus],
  () => {
    if (!props.doubleColumn) {
      selectedFirstLevelKeys.value = []
      return
    }

    const firstMenus = firstLevelMenus.value
    if (firstMenus.length === 0) {
      selectedFirstLevelKeys.value = []
      return
    }

    const currentKey = selectedFirstLevelKeys.value[0]
    if (firstMenus.some(menu => menu.key === currentKey)) {
      return
    }

    const preferredMenu = firstMenus.find(menu => menu.children?.length) || firstMenus[0]
    selectedFirstLevelKeys.value = [preferredMenu.key]
  },
  { immediate: true, deep: true },
)

watch(
  () => [props.activeKey, props.menus, props.doubleColumn],
  ([newKey]) => {
    const normalizedKey = String(newKey || '').split('?')[0]
    if (!normalizedKey) {
      selectedKeys.value = []
      openKeys.value = []
      return
    }

    selectedKeys.value = [normalizedKey]
    const menuPath = findMenuPath(props.menus, normalizedKey)
    if (!menuPath?.length) {
      openKeys.value = []
      return
    }

    const firstLevelMenu = menuPath[0]
    if (props.doubleColumn && firstLevelMenu) {
      selectedFirstLevelKeys.value = [firstLevelMenu.key]
    }

    openKeys.value = menuPath
      .slice(0, -1)
      .filter(menu => !props.doubleColumn || menu.key !== firstLevelMenu?.key)
      .map(menu => menu.key)
  },
  { immediate: true, deep: true },
)

function findMenuByKey(menus: MenuItem[], key: string): MenuItem | null {
  for (const menu of menus) {
    if (menu.key === key) {
      return menu
    }
    const found = findMenuByKey(menu.children || [], key)
    if (found) {
      return found
    }
  }
  return null
}

function findMenuPath(menus: MenuItem[], key: string, parentPath: MenuItem[] = []): MenuItem[] | null {
  for (const menu of menus) {
    const currentPath = [...parentPath, menu]
    if (menu.key === key || menu.path === key) {
      return currentPath
    }
    const found = findMenuPath(menu.children || [], key, currentPath)
    if (found) {
      return found
    }
  }
  return null
}

function findFirstNavigableMenu(menu: MenuItem): MenuItem | null {
  if (!menu.children?.length && menu.path) {
    return menu
  }
  for (const child of menu.children || []) {
    const found = findFirstNavigableMenu(child)
    if (found) {
      return found
    }
  }
  return null
}

function onFirstLevelMenuClick(info: any) {
  const key = String(info.key || '')
  const clickedMenu = findMenuByKey(props.menus, key)
  if (!clickedMenu) {
    return
  }

  selectedFirstLevelKeys.value = [clickedMenu.key]
  if (clickedMenu.children?.length) {
    const firstTarget = findFirstNavigableMenu(clickedMenu)
    if (firstTarget?.path) {
      selectedKeys.value = [firstTarget.path]
    }
    return
  }

  emit('menu-click', clickedMenu.path || clickedMenu.key)
}

function onMenuClick(info: any) {
  const key = String(info.key || '')
  const clickedMenu = findMenuByKey(currentMenus.value, key) || findMenuByKey(props.menus, key)
  if (!key || clickedMenu?.children?.length) {
    return
  }

  emit('menu-click', clickedMenu?.path || key)
}

function onOpenChange(keys: string[]) {
  openKeys.value = keys
}

function onCollapse(collapsed: boolean) {
  emit('collapse-change', collapsed)
}
</script>
<style scoped lang="less">
.app-sidebar-wrapper {
  display: flex;
  height: 100%;
  min-height: 0;
  overflow: hidden;
}

.app-sidebar-mini {
  background: var(--fx-sider-mini-bg, #161720);
  border-right: 1px solid rgba(255, 255, 255, 0.06);
  min-height: 0;
  overflow: hidden;
  
  :deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
    min-height: 0;
    padding: 10px 0 14px;
  }
}

.mini-menu {
  flex: 1;
  height: 100%;
  min-height: 0;
  background: transparent;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 100%;

  &::-webkit-scrollbar {
    width: 6px;
  }

  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.1);
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.16);
    border-radius: 3px;

    &:hover {
      background: rgba(255, 255, 255, 0.24);
    }
  }
  
  :deep(.ant-menu-item) {
    display: flex !important;
    align-items: stretch;
    justify-content: center;
    min-height: 76px;
    height: auto;
    padding: 0 !important;
    padding-inline: 0 !important;
    padding-inline-start: 0 !important;
    padding-inline-end: 0 !important;
    margin: 2px 10px 4px;
    color: var(--fx-sidebar-mini-text-color, rgba(224, 227, 237, 0.58)) !important;
    line-height: 1.2;
    transition: color 0.2s ease, background-color 0.2s ease;
    background: transparent !important;
    border-radius: 14px;
    inset-inline-start: 0 !important;
    
    &::after {
      display: none !important;
    }
    
    &:hover {
      color: var(--fx-sidebar-mini-text-hover-color, rgba(244, 246, 255, 0.9)) !important;
      background: var(--fx-sidebar-mini-item-hover-bg, rgba(255, 255, 255, 0.03)) !important;
    }
    
    &.ant-menu-item-selected {
      color: var(--fx-theme-color, var(--fx-primary, #1677ff)) !important;
      background: transparent !important;
    }
    
    .ant-menu-title-content {
      margin: 0 !important;
      width: 100%;
      height: 100%;
      flex: 1 1 auto;
      max-width: 100%;
      display: flex;
      align-items: stretch;
      justify-content: center;
      text-align: center !important;
      color: inherit !important;
      white-space: normal !important;
      overflow: visible !important;
    }
  }
}

.mini-menu-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  width: 100%;
  height: 100%;
  min-height: 76px;
  padding: 8px 10px 10px;
  margin: 0 auto;
  text-align: center;
  color: inherit;
  box-sizing: border-box;
}

.mini-menu-icon-shell {
  width: 34px;
  height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  color: inherit;
  background: transparent;
  transition: background-color 0.2s ease, color 0.2s ease;
  flex-shrink: 0;
}

.mini-menu-icon {
  font-size: 19px;
  margin: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.2s ease;
  color: inherit !important;
  flex-shrink: 0;
}

.mini-menu :deep(.ant-menu-item:hover) .mini-menu-icon-shell {
  background: var(--fx-sidebar-mini-icon-hover-bg, rgba(255, 255, 255, 0.05));
}

.mini-menu :deep(.ant-menu-item-selected) .mini-menu-icon-shell {
  background: rgba(22, 119, 255, 0.2);
  box-shadow: inset 0 0 0 1px rgba(22, 119, 255, 0.3);
  background: color-mix(in srgb, var(--fx-theme-color, var(--fx-primary, #1677ff)) 22%, transparent);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--fx-theme-color, var(--fx-primary, #1677ff)) 46%, transparent);
}

.mini-menu :deep(.ant-menu-item-selected) .mini-menu-icon {
  color: var(--fx-theme-color, var(--fx-primary, #1677ff)) !important;
}

.app-sidebar {
  background: var(--fx-sider-bg, #001529);
  border-right: 1px solid var(--fx-border-color, rgba(255, 255, 255, 0.1));
  min-height: 0;
  overflow: hidden;
  
  :deep(.ant-layout-sider-trigger) {
    background: rgba(0, 0, 0, 0.2);
    color: rgba(255, 255, 255, 0.85);
    
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}

.app-sidebar-wrapper--vertical {
  .sidebar-menu {
    :deep(.ant-menu-item),
    :deep(.ant-menu-submenu-title) {
      padding-inline-end: 12px !important;
    }

    :deep(.ant-menu-item-icon),
    :deep(.ant-menu-submenu-icon) {
      margin-right: 6px;
    }

    :deep(.ant-menu-title-content) {
      min-width: 0;
    }
  }

  .menu-text {
    display: block;
    white-space: nowrap;
    word-break: keep-all;
    -webkit-line-clamp: unset;
  }
}

.sidebar-menu {
  height: 100%;
  min-height: 0;
  background: transparent;
  border-right: none;
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 100vh;
  
  // 婊氬姩鏉℃牱寮?
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-track {
    background: rgba(0, 0, 0, 0.1);
  }
  
  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.2);
    border-radius: 3px;
    
    &:hover {
      background: rgba(255, 255, 255, 0.3);
    }
  }
  
  :deep(.ant-menu-item),
  :deep(.ant-menu-submenu-title) {
    height: 40px;
    line-height: 40px;
    margin: 0;
    color: var(--fx-sidebar-menu-text-color, var(--fx-text-color, rgba(255, 255, 255, 0.65))) !important;
    font-size: var(--fx-font-size, 14px);
    background: transparent !important;
    min-width: 0;
    display: flex;
    align-items: center;
    
    &::after {
      display: none !important;
    }
    
    &:hover {
      color: var(--fx-theme-color, #1677ff) !important;
      background: var(--fx-sidebar-menu-hover-bg, var(--fx-tab-hover-bg, rgba(255, 255, 255, 0.08))) !important;
    }
    
    .ant-menu-item-icon,
    .ant-menu-submenu-icon {
      font-size: calc(var(--fx-font-size, 14px) * 1.15);
      color: inherit !important;
      flex-shrink: 0;
      margin-right: 8px;
    }
    
    .ant-menu-title-content {
      color: inherit !important;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      word-break: keep-all;
      flex: 1;
      display: flex;
      align-items: center;
    }
  }
  
  :deep(.ant-menu-item-selected) {
    color: var(--fx-theme-color, #1677ff) !important;
    background: var(--fx-sidebar-menu-active-bg, var(--fx-tab-bg, rgba(255, 255, 255, 0.12))) !important;
    
    &::after {
      border-right: 3px solid var(--fx-theme-color, #1677ff) !important;
      display: block !important;
    }
  }
  
  :deep(.ant-menu-submenu-selected) {
    color: var(--fx-theme-color, #1677ff) !important;
    
    > .ant-menu-submenu-title {
      color: var(--fx-theme-color, #1677ff) !important;
      background: transparent !important;
    }
  }
  
  :deep(.ant-menu-sub) {
    background: var(--fx-sidebar-menu-sub-bg, rgba(0, 0, 0, 0.2)) !important;
    
    .ant-menu-item {
      background: transparent !important;
      display: flex;
      align-items: center;
      
      &:hover {
        background: var(--fx-sidebar-menu-hover-bg, var(--fx-tab-hover-bg, rgba(255, 255, 255, 0.08))) !important;
      }
      
      .ant-menu-item-icon {
        margin-right: 8px;
      }
      
      .ant-menu-title-content {
        display: flex;
        align-items: center;
      }
    }
  }
  
  // 纭繚瀛愯彍鍗曚篃鑳芥粴鍔?
  :deep(.ant-menu-submenu-popover) {
    max-height: 80vh;
    overflow-y: auto;
    
    // 子菜单滚动条样式
    &::-webkit-scrollbar {
      width: 6px;
    }
    
    &::-webkit-scrollbar-track {
      background: rgba(0, 0, 0, 0.1);
    }
    
    &::-webkit-scrollbar-thumb {
      background: rgba(0, 0, 0, 0.3);
      border-radius: 3px;
      
      &:hover {
        background: rgba(0, 0, 0, 0.4);
      }
    }
  }
}

// 菜单文字样式
.menu-text {
  max-width: 100%;
  line-height: 1.35;
  vertical-align: middle;
  display: -webkit-box;
  overflow: hidden;
  white-space: normal;
  text-overflow: ellipsis;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  word-break: break-all;
}

// 涓€绾ц彍鍗曟爣棰樻牱寮忥紙鏀寔澶氳鏄剧ず锛?
.mini-menu-title {
  font-size: 12px;
  margin-top: 0;
  text-align: center;
  line-height: 1.35;
  font-weight: 600;
  letter-spacing: 0.01em;
  width: 100%;
  // 鏄剧ず鏈€澶?琛岋紝瓒呭嚭鏄剧ず鐪佺暐鍙?
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
  overflow: hidden;
  text-overflow: ellipsis;
  word-break: break-word;
  max-height: calc(1.35em * 2);
  max-width: 100%;
  color: inherit;
}

.mini-menu :deep(.ant-menu-item-selected) .mini-menu-title {
  color: var(--fx-theme-color, var(--fx-primary, #1677ff));
}

:global(html[data-theme='light']) .app-sidebar-mini,
:global(body[data-theme='light']) .app-sidebar-mini {
  border-right-color: var(--fx-border-color, #e5e7eb);
}

:global(html[data-theme='light']) .mini-menu,
:global(body[data-theme='light']) .mini-menu {
  --fx-sidebar-mini-text-color: var(--fx-text-color, #4b5563);
  --fx-sidebar-mini-text-hover-color: var(--fx-theme-color, var(--fx-primary, #1677ff));
  --fx-sidebar-mini-item-hover-bg: var(--fx-fill-secondary, #f9fafb);
  --fx-sidebar-mini-icon-hover-bg: color-mix(in srgb, var(--fx-theme-color, var(--fx-primary, #1677ff)) 10%, transparent);
}

:global(html[data-theme='light']) .app-sidebar,
:global(body[data-theme='light']) .app-sidebar {
  background: var(--fx-sider-bg, #ffffff);
  border-right-color: var(--fx-border-color, #e5e7eb);
}

:global(html[data-theme='light']) .app-sidebar :deep(.ant-layout-sider-trigger),
:global(body[data-theme='light']) .app-sidebar :deep(.ant-layout-sider-trigger) {
  background: var(--fx-fill-secondary, #f9fafb);
  color: var(--fx-text-color, #4b5563);
}

:global(html[data-theme='light']) .sidebar-menu,
:global(body[data-theme='light']) .sidebar-menu {
  --fx-sidebar-menu-text-color: var(--fx-text-color, #4b5563);
  --fx-sidebar-menu-hover-bg: var(--fx-fill-secondary, #f9fafb);
  --fx-sidebar-menu-active-bg: color-mix(in srgb, var(--fx-theme-color, var(--fx-primary, #1677ff)) 10%, #ffffff);
  --fx-sidebar-menu-sub-bg: var(--fx-fill-secondary, #f8fafc);
}

// 鍝嶅簲寮忛€傞厤
@media (max-width: 768px) {
  .app-sidebar-mini {
    display: none;
  }
}
</style>
