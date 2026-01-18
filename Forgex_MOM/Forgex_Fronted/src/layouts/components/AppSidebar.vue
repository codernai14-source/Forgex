<template>
  <div class="app-sidebar-wrapper">
    <!-- 双列布局：第一列（一级菜单/目录） -->
    <a-layout-sider
      v-if="doubleColumn"
      class="app-sidebar-mini"
      :collapsed="false"
      :width="100"
    >
      <a-menu
        mode="inline"
        :selected-keys="selectedFirstLevelKeys"
        class="mini-menu"
        @click="onFirstLevelMenuClick"
      >
        <a-menu-item
          v-for="menu in firstLevelMenus"
          :key="menu.key"
          class="mini-menu-item"
        >
          <template #icon>
            <component v-if="menu.icon" :is="getIcon(menu.icon)" />
            <FileOutlined v-else />
          </template>
          <span class="mini-menu-title">{{ menu.title }}</span>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <!-- 主侧边栏（第二列：二三级菜单） -->
    <!-- 第二列宽度减少10%，从200px改为180px -->
    <a-layout-sider
      v-if="!doubleColumn || hasSecondLevelMenus"
      class="app-sidebar"
      :collapsed="collapsed"
      :collapsible="true"
      :width="180"
      :collapsed-width="64"
      @collapse="onCollapse"
    >
      <a-menu
        mode="inline"
        :selected-keys="selectedKeys"
        :open-keys="openKeys"
        class="sidebar-menu"
        @openChange="onOpenChange"
        @click="onMenuClick"
      >
        <template v-for="item in currentMenus" :key="item.key">
          <!-- 目录（有子菜单） -->
          <a-sub-menu v-if="item.children && item.children.length > 0" :key="item.key">
            <template #icon>
              <component v-if="item.icon" :is="getIcon(item.icon)" />
              <FolderOutlined v-else />
            </template>
            <template #title>{{ item.title }}</template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.key"
            >
              <template #icon>
                <component v-if="child.icon" :is="getIcon(child.icon)" />
                <FileOutlined v-else />
              </template>
              <span>{{ child.title }}</span>
            </a-menu-item>
          </a-sub-menu>

          <!-- 菜单（无子菜单） -->
          <a-menu-item v-else :key="item.key">
            <template #icon>
              <component v-if="item.icon" :is="getIcon(item.icon)" />
              <FileOutlined v-else />
            </template>
            <span>{{ item.title }}</span>
          </a-menu-item>
        </template>
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
  FileOutlined
} from '@ant-design/icons-vue'

interface MenuItem {
  key: string
  title: string
  icon?: string
  path: string
  moduleCode: string
  parentKey?: string
  menuLevel?: number  // 菜单层级：1=一级菜单, 2=二级菜单, 3=三级菜单
  children?: MenuItem[]
  type: 'catalog' | 'menu' | 'button'
}

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
  collapsed?: boolean
  doubleColumn?: boolean
}

const props = withDefaults(defineProps<AppSidebarProps>(), {
  menus: () => [],
  modules: () => [],
  activeKey: '',
  activeModuleCode: '',
  collapsed: false,
  doubleColumn: false
})

const emit = defineEmits<{
  'menu-click': [menuKey: string]
  'module-click': [moduleCode: string]
  'collapse-change': [collapsed: boolean]
}>()

// 选中的菜单keys
const selectedKeys = ref<string[]>([])
// 选中的一级菜单keys（用于双列布局）
const selectedFirstLevelKeys = ref<string[]>([])
// 展开的菜单keys
const openKeys = ref<string[]>([])

// 一级菜单列表（用于双列布局的第一列）
const firstLevelMenus = computed(() => {
  if (!props.doubleColumn) {
    return []
  }
  
  // 返回所有一级菜单（menuLevel === 1），包含children
  const menus = props.menus.filter(menu => menu.menuLevel === 1)
  console.log('[AppSidebar] First level menus:', JSON.stringify(menus, null, 2))
  return menus
})

// 是否有二三级菜单（用于判断是否显示第二列）
const hasSecondLevelMenus = computed(() => {
  if (!props.doubleColumn) {
    return false
  }
  
  // 只有点击了目录类型菜单且该菜单有children时，才显示第二列
  // 获取当前选中的一级菜单
  const selectedFirstLevelMenu = firstLevelMenus.value.find(menu => 
    menu.key === selectedFirstLevelKeys.value[0]
  )
  
  if (!selectedFirstLevelMenu) {
    return false
  }
  
  // 如果选中的是目录类型菜单且有children，显示第二列
  const showSecondLevel = selectedFirstLevelMenu.type === 'catalog' && 
                         selectedFirstLevelMenu.children && 
                         selectedFirstLevelMenu.children.length > 0
  
  console.log('[AppSidebar] hasSecondLevelMenus check:', showSecondLevel, selectedFirstLevelMenu.key, selectedFirstLevelMenu.type, selectedFirstLevelMenu.children?.length)
  return showSecondLevel
})

// 当前显示的菜单列表（第二列）
const currentMenus = computed(() => {
  if (!props.doubleColumn) {
    // 单列模式：显示所有菜单
    return props.menus
  }
  
  // 双列模式：只显示当前模块的二三级菜单
  if (!props.activeModuleCode) {
    return []
  }
  
  // 获取当前选中的一级菜单
  // 使用firstLevelMenus.value.find，因为firstLevelMenus包含完整的children结构
  const selectedFirstLevelMenu = firstLevelMenus.value.find(menu => 
    menu.key === selectedFirstLevelKeys.value[0]
  )
  
  if (!selectedFirstLevelMenu) {
    // 如果没有选中的一级菜单，显示所有menuLevel >= 2的菜单
    return props.menus.filter(menu => 
      menu.moduleCode === props.activeModuleCode && 
      menu.menuLevel && 
      menu.menuLevel >= 2
    )
  }
  
  // 显示选中的一级菜单对应的二级菜单
  // 如果选中的是目录类型菜单，显示其所有子菜单
  // 如果选中的是非目录类型菜单，显示所有menuLevel >= 2的菜单
  if (selectedFirstLevelMenu.type === 'catalog') {
    console.log('[AppSidebar] Show children of catalog menu:', selectedFirstLevelMenu.key)
    return selectedFirstLevelMenu.children || []
  } else {
    console.log('[AppSidebar] Show all sub menus:', selectedFirstLevelMenu.key)
    return props.menus.filter(menu => 
      menu.moduleCode === props.activeModuleCode && 
      menu.menuLevel && 
      menu.menuLevel >= 2
    )
  }
})

// 监听 activeKey 变化
watch(
  () => props.activeKey,
  (newKey) => {
    if (newKey) {
      selectedKeys.value = [newKey]
      
      // 自动展开父菜单
      const menu = findMenuByKey(props.menus, newKey)
      if (menu && menu.parentKey) {
        if (!openKeys.value.includes(menu.parentKey)) {
          openKeys.value.push(menu.parentKey)
        }
      }
    }
  },
  { immediate: true }
)

// 查找菜单项
function findMenuByKey(menus: MenuItem[], key: string): MenuItem | null {
  for (const menu of menus) {
    if (menu.key === key) {
      return menu
    }
    if (menu.children) {
      const found = findMenuByKey(menu.children, key)
      if (found) {
        return found
      }
    }
  }
  return null
}

// 查找一级菜单项
function findFirstLevelMenu(key: string): MenuItem | null {
  // 直接从props.menus中查找一级菜单
  return props.menus.find(menu => 
    menu.key === key && 
    menu.menuLevel === 1
  ) || null
}

// 一级菜单点击（双列布局）
const onFirstLevelMenuClick = (info: any) => {
  const key = info.key as string
  console.log('[AppSidebar] First level menu clicked:', key, info)
  if (key) {
    selectedFirstLevelKeys.value = [key]
    
    // 查找当前点击的一级菜单
    const clickedMenu = findFirstLevelMenu(key)
    
    // 只有非目录类型的菜单才触发路由跳转
    // 目录类型菜单只需要更新selectedFirstLevelKeys，让currentMenus自动更新显示对应的二级菜单
    if (clickedMenu && clickedMenu.type !== 'catalog') {
      console.log('[AppSidebar] Emitting menu-click event:', key)
      emit('menu-click', key)
    } else {
      console.log('[AppSidebar] Menu is catalog type, skip routing:', key)
    }
  }
}

// 模块点击
const onModuleClick = (info: any) => {
  const key = info.key as string
  console.log('[AppSidebar] Module clicked:', key)
  if (key) {
    emit('module-click', key)
  }
}

// 菜单点击
const onMenuClick = (info: any) => {
  const key = info.key as string
  console.log('[AppSidebar] Menu clicked:', key, info)
  if (key) {
    console.log('[AppSidebar] Emitting menu-click event:', key)
    emit('menu-click', key)
  }
}

// 菜单展开/收起
const onOpenChange = (keys: string[]) => {
  openKeys.value = keys
}

// 侧边栏折叠/展开
const onCollapse = (collapsed: boolean) => {
  emit('collapse-change', collapsed)
}
</script>

<style scoped lang="less">
.app-sidebar-wrapper {
  display: flex;
  height: 100%;
}

.app-sidebar-mini {
  background: var(--fx-sider-mini-bg, #001529);
  border-right: 1px solid var(--fx-border-color, rgba(255, 255, 255, 0.1));
  
  :deep(.ant-layout-sider-children) {
    display: flex;
    flex-direction: column;
  }
}

.mini-menu {
  flex: 1;
  background: transparent;
  border-right: none;
  
  :deep(.ant-menu-item) {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 68px;
    padding: 10px 8px;
    margin: 4px 0;
    color: var(--fx-text-color, rgba(255, 255, 255, 0.75)) !important;
    line-height: 1.2;
    transition: all 0.2s ease;
    background: transparent !important;
    
    &::after {
      display: none !important;
    }
    
    &:hover {
      color: var(--fx-theme-color, #1677ff) !important;
      background: var(--fx-tab-hover-bg, rgba(255, 255, 255, 0.08)) !important;
      transform: scale(1.02);
    }
    
    &.ant-menu-item-selected {
      color: var(--fx-theme-color, #1677ff) !important;
      background: var(--fx-tab-bg, rgba(255, 255, 255, 0.12)) !important;
    }
    
    .ant-menu-item-icon {
      font-size: 22px;
      margin: 0 0 6px 0;
      display: flex;
      align-items: center;
      justify-content: center;
      transition: transform 0.2s ease;
      color: inherit !important;
    }
    
    .ant-menu-title-content {
      margin: 0;
      width: 100%;
      text-align: center;
      color: inherit !important;
    }
    
    &:hover .ant-menu-item-icon {
      transform: scale(1.1);
    }
  }
}

.mini-menu-title {
  font-size: 13px;
  margin-top: 4px;
  text-align: center;
  line-height: 1.2;
  width: 100%;
  // 允许文字换行，避免四字菜单名显示不全
  word-break: break-all;
  white-space: normal;
  overflow: visible;
  text-overflow: clip;
}

.app-sidebar {
  background: var(--fx-sider-bg, #001529);
  border-right: 1px solid var(--fx-border-color, rgba(255, 255, 255, 0.1));
  
  :deep(.ant-layout-sider-trigger) {
    background: rgba(0, 0, 0, 0.2);
    color: rgba(255, 255, 255, 0.85);
    
    &:hover {
      background: rgba(0, 0, 0, 0.3);
    }
  }
}

.sidebar-menu {
  height: 100%;
  background: transparent;
  border-right: none;
  
  :deep(.ant-menu-item),
  :deep(.ant-menu-submenu-title) {
    height: 40px;
    line-height: 40px;
    margin: 0;
    color: var(--fx-text-color, rgba(255, 255, 255, 0.65)) !important;
    font-size: var(--fx-font-size, 14px);
    background: transparent !important;
    // 确保菜单项有足够宽度显示完整文字
    min-width: 0;
    
    &::after {
      display: none !important;
    }
    
    &:hover {
      color: var(--fx-theme-color, #1677ff) !important;
      background: var(--fx-tab-hover-bg, rgba(255, 255, 255, 0.08)) !important;
    }
    
    .ant-menu-item-icon,
    .ant-menu-submenu-icon {
      font-size: calc(var(--fx-font-size, 14px) * 1.15);
      color: inherit !important;
      flex-shrink: 0;
    }
    
    .ant-menu-title-content {
      color: inherit !important;
      // 允许文字正常显示，不截断
      overflow: visible;
      text-overflow: clip;
      white-space: normal;
      word-break: keep-all;
    }
  }
  
  :deep(.ant-menu-item-selected) {
    color: var(--fx-theme-color, #1677ff) !important;
    background: var(--fx-tab-bg, rgba(255, 255, 255, 0.12)) !important;
    
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
    background: rgba(0, 0, 0, 0.2) !important;
    
    .ant-menu-item {
      background: transparent !important;
      
      &:hover {
        background: var(--fx-tab-hover-bg, rgba(255, 255, 255, 0.08)) !important;
      }
    }
  }
}

// 响应式适配
@media (max-width: 768px) {
  .app-sidebar-mini {
    display: none;
  }
}
</style>
