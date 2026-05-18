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
              <FxIcon v-if="menu.icon" :name="menu.icon" class="mini-menu-icon" />
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
          @submenu-title-click="onSubmenuTitleClick"
        />
      </a-menu>
      <button
        type="button"
        class="app-sidebar-collapse-btn"
        :class="{ 'app-sidebar-collapse-btn--collapsed': collapsed }"
        @click="onCollapse(!collapsed)"
      >
        <LeftOutlined />
      </button>
    </a-layout-sider>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import FxIcon from '@/components/common/FxIcon.vue'
import {
  AppstoreOutlined,
  FolderOutlined,
  FileOutlined,
  LeftOutlined,
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
  for (const child of menu.children || []) {
    const found = findFirstNavigableMenu(child)
    if (found) {
      return found
    }
  }
  if (!menu.children?.length && !['catalog', 'module', 'button'].includes(menu.type) && menu.path) {
    return menu
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
      emit('menu-click', firstTarget.path)
    }
    return
  }

  emit('menu-click', clickedMenu.path || clickedMenu.key)
}

function onMenuClick(info: any) {
  const key = String(info.key || '')
  const clickedMenu = findMenuByKey(currentMenus.value, key) || findMenuByKey(props.menus, key)
  if (!key) {
    return
  }

  if (clickedMenu?.children?.length) {
    const firstTarget = findFirstNavigableMenu(clickedMenu)
    if (firstTarget?.path) {
      selectedKeys.value = [firstTarget.path]
      emit('menu-click', firstTarget.path)
    }
    return
  }

  emit('menu-click', clickedMenu?.path || key)
}

function onSubmenuTitleClick(menu: MenuItem) {
  const firstTarget = findFirstNavigableMenu(menu)
  if (firstTarget?.path) {
    selectedKeys.value = [firstTarget.path]
    emit('menu-click', firstTarget.path)
  }
}

function onOpenChange(keys: string[]) {
  openKeys.value = keys
}

function onCollapse(collapsed: boolean) {
  emit('collapse-change', collapsed)
}
</script>
<style scoped lang="less" src="@/styles/layout/components/app-sidebar.less"></style>
