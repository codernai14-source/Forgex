<template>
  <div class="app-sidebar-wrapper fx-guide-sidebar">
    <!-- 鍙屽垪甯冨眬锛氱涓€鍒楋紙涓€绾ц彍鍗?鐩綍锛?-->
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
              <FileOutlined v-else class="mini-menu-icon" />
            </span>
            <span class="mini-menu-title" :title="menu.title">{{ menu.title }}</span>
          </div>
        </a-menu-item>
      </a-menu>
    </a-layout-sider>

    <!-- 涓讳晶杈规爮锛堢浜屽垪锛氫簩涓夌骇鑿滃崟锛?-->
    <!-- 绗簩鍒楀搴﹀噺灏?0%锛屼粠200px鏀逛负180px -->
    <a-layout-sider
      v-if="!doubleColumn || hasSecondLevelMenus"
      class="app-sidebar fx-guide-sidebar-main"
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
        class="sidebar-menu fx-guide-sidebar-menu"
        @openChange="onOpenChange"
        @click="onMenuClick"
      >
        <template v-for="item in currentMenus" :key="item.key">
          <!-- 鐩綍锛堟湁瀛愯彍鍗曪級 -->
          <a-sub-menu v-if="item.children && item.children.length > 0" :key="item.key">
            <template #icon>
              <component v-if="item.icon" :is="getIcon(item.icon)" />
              <FolderOutlined v-else />
            </template>
            <template #title>
              <span class="menu-text" :title="item.title">{{ item.title }}</span>
            </template>
            <a-menu-item
              v-for="child in item.children"
              :key="child.key"
            >
              <template #icon>
                <component v-if="child.icon" :is="getIcon(child.icon)" />
                <FileOutlined v-else />
              </template>
              <span class="menu-text" :title="child.title">{{ child.title }}</span>
            </a-menu-item>
          </a-sub-menu>

          <!-- 菜单（无子菜单） -->
          <a-menu-item v-else :key="item.key">
            <template #icon>
              <component v-if="item.icon" :is="getIcon(item.icon)" />
              <FileOutlined v-else />
            </template>
            <span class="menu-text" :title="item.title">{{ item.title }}</span>
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
  menuLevel?: number  // 鑿滃崟灞傜骇锛?=涓€绾ц彍鍗? 2=浜岀骇鑿滃崟, 3=涓夌骇鑿滃崟
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
  /** 鑿滃崟椤规暟缁勶紝鍖呭惈鎵€鏈夎彍鍗曠殑灞傜骇缁撴瀯鏁版嵁 */
  menus: MenuItem[]
  /** 妯″潡鍒楄〃锛岀敤浜庡弻鍒楀竷灞€妯″紡鏄剧ず妯″潡瀵艰埅 */
  modules?: Module[]
  /** 褰撳墠婵€娲荤殑鑿滃崟 key锛岀敤浜庨珮浜樉绀洪€変腑鐨勮彍鍗曢」 */
  activeKey?: string
  /** 褰撳墠婵€娲荤殑妯″潡 code锛岀敤浜庡弻鍒楀竷灞€妯″紡 */
  activeModuleCode?: string
  /** 侧边栏是否折叠，true 琛ㄧず鎶樺彔鐘舵€?*/
  collapsed?: boolean
  /** 鏄惁鍚敤鍙屽垪甯冨眬锛宼rue 琛ㄧず鏄剧ず涓ゅ垪渚ц竟鏍?*/
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
  /**
   * 菜单点击事件
   * 瑙﹀彂鏃舵満锛氱敤鎴风偣鍑昏彍鍗曢」鏃惰Е鍙?
   * @param menuKey 琚偣鍑荤殑鑿滃崟 key
   */
  'menu-click': [menuKey: string]
  /**
   * 妯″潡鐐瑰嚮浜嬩欢
   * 瑙﹀彂鏃舵満锛氱敤鎴风偣鍑绘ā鍧楀鑸椂瑙﹀彂
   * @param moduleCode 琚偣鍑荤殑妯″潡 code
   */
  'module-click': [moduleCode: string]
  /**
   * 渚ц竟鏍忔姌鍙犵姸鎬佸彉鍖栦簨浠?
   * 瑙﹀彂鏃舵満锛氱敤鎴风偣鍑绘姌鍙犳寜閽椂瑙﹀彂
   * @param collapsed 鏂扮殑鎶樺彔鐘舵€?
   */
  'collapse-change': [collapsed: boolean]
}>()

// 选中的菜单keys
const selectedKeys = ref<string[]>([])
// 选中的一级菜单keys锛堢敤浜庡弻鍒楀竷灞€锛?
const selectedFirstLevelKeys = ref<string[]>([])
// 展开的菜单keys
const openKeys = ref<string[]>([])

// 涓€绾ц彍鍗曞垪琛紙鐢ㄤ簬鍙屽垪甯冨眬鐨勭涓€鍒楋級
const firstLevelMenus = computed(() => {
  if (!props.doubleColumn) {
    return []
  }
  
  // 杩斿洖鎵€鏈変竴绾ц彍鍗曪紙menuLevel === 1），包含children
  // 濡傛灉娌℃湁menuLevel灞炴€э紝涔熻涓轰竴绾ц彍鍗?
  const menus = props.menus.filter(menu => 
    menu.menuLevel === 1 || 
    (menu.parentKey === undefined && !menu.children) || 
    (menu.parentKey === undefined && menu.children && menu.type === 'menu')
  )
  return menus
})

// 鏄惁鏈変簩涓夌骇鑿滃崟锛堢敤浜庡垽鏂槸鍚︽樉绀虹浜屽垪锛?
const hasSecondLevelMenus = computed(() => {
  if (!props.doubleColumn) {
    return false
  }
  
  // 鍙湁鐐瑰嚮浜嗙洰褰曠被鍨嬭彍鍗曚笖璇ヨ彍鍗曟湁children鏃讹紝鎵嶆樉绀虹浜屽垪
  // 鑾峰彇褰撳墠閫変腑鐨勪竴绾ц彍鍗?
  const selectedFirstLevelMenu = firstLevelMenus.value.find(menu => 
    menu.key === selectedFirstLevelKeys.value[0]
  )
  
  if (!selectedFirstLevelMenu) {
    return false
  }
  
  // 濡傛灉閫変腑鐨勬槸鐩綍绫诲瀷鑿滃崟涓旀湁children锛屾樉绀虹浜屽垪
  const showSecondLevel = selectedFirstLevelMenu.type === 'catalog' && 
                         selectedFirstLevelMenu.children && 
                         selectedFirstLevelMenu.children.length > 0
  
  return showSecondLevel
})

// 褰撳墠鏄剧ず鐨勮彍鍗曞垪琛紙绗簩鍒楋級
const currentMenus = computed(() => {
  if (!props.doubleColumn) {
    // 鍗曞垪妯″紡锛氭樉绀烘墍鏈夎彍鍗?
    return props.menus
  }
  
  // 双列模式：只显示当前模块的二三级菜单
  if (!props.activeModuleCode) {
    return []
  }
  
  // 鑾峰彇褰撳墠閫変腑鐨勪竴绾ц彍鍗?
  // 浣跨敤firstLevelMenus.value.find锛屽洜涓篺irstLevelMenus包含完整的children缁撴瀯
  const selectedFirstLevelMenu = firstLevelMenus.value.find(menu => 
    menu.key === selectedFirstLevelKeys.value[0]
  )
  
  if (!selectedFirstLevelMenu) {
    // 濡傛灉娌℃湁閫変腑鐨勪竴绾ц彍鍗曪紝鏄剧ず鎵€鏈塵enuLevel >= 2鐨勮彍鍗?
    return props.menus.filter(menu => 
      menu.moduleCode === props.activeModuleCode && 
      menu.menuLevel && 
      menu.menuLevel >= 2
    )
  }
  
  // 鏄剧ず閫変腑鐨勪竴绾ц彍鍗曞搴旂殑浜岀骇鑿滃崟
  // 濡傛灉閫変腑鐨勬槸鐩綍绫诲瀷鑿滃崟锛屾樉绀哄叾鎵€鏈夊瓙鑿滃崟
  // 濡傛灉閫変腑鐨勬槸闈炵洰褰曠被鍨嬭彍鍗曪紝鏄剧ず鎵€鏈塵enuLevel >= 2鐨勮彍鍗?
  if (selectedFirstLevelMenu.type === 'catalog') {
    return selectedFirstLevelMenu.children || []
  } else {
    return props.menus.filter(menu => 
      menu.moduleCode === props.activeModuleCode && 
      menu.menuLevel && 
      menu.menuLevel >= 2
    )
  }
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
    const exists = firstMenus.some(menu => menu.key === currentKey)
    if (exists) {
      return
    }

    const preferredCatalog = firstMenus.find(menu =>
      menu.type === 'catalog' &&
      Array.isArray(menu.children) &&
      menu.children.length > 0,
    )

    selectedFirstLevelKeys.value = [preferredCatalog?.key || firstMenus[0].key]
  },
  { immediate: true, deep: true },
)

// 鐩戝惉 activeKey 鍙樺寲
watch(
  () => [props.activeKey, props.menus],
  ([newKey]) => {
    const normalizedKey = String(newKey || '').split('?')[0]
    if (!normalizedKey) {
      selectedKeys.value = []
      openKeys.value = []
      return
    }
    selectedKeys.value = [normalizedKey]
      
      // 鑷姩灞曞紑鐖惰彍鍗?
      const menuPath = findMenuPath(props.menus, normalizedKey)
      if (!menuPath || menuPath.length === 0) {
        openKeys.value = []
        return
      }

      const firstLevelMenu = menuPath.find(menu => menu.menuLevel === 1) || menuPath[0]
      if (firstLevelMenu) {
        selectedFirstLevelKeys.value = [firstLevelMenu.key]
      }

      openKeys.value = menuPath
        .slice(0, -1)
        .filter(menu => menu.key !== firstLevelMenu?.key)
        .map(menu => menu.key)
  },
  { immediate: true }
)

// 鏌ユ壘鑿滃崟椤?
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

function findMenuPath(menus: MenuItem[], key: string, parentPath: MenuItem[] = []): MenuItem[] | null {
  for (const menu of menus) {
    const currentPath = [...parentPath, menu]
    if (menu.key === key) {
      return currentPath
    }
    if (menu.children && menu.children.length > 0) {
      const found = findMenuPath(menu.children, key, currentPath)
      if (found) {
        return found
      }
    }
  }
  return null
}

/**
 * 鎴柇鏂囨湰骞舵坊鍔犵渷鐣ュ彿
 * @param text 鍘熷鏂囨湰
 * @param maxLength 鏈€澶ч暱搴?
 * @returns 鎴柇鍚庣殑鏂囨湰
 */
// 鏌ユ壘涓€绾ц彍鍗曢」
function findFirstLevelMenu(key: string): MenuItem | null {
  // 鐩存帴浠巔rops.menus涓煡鎵句竴绾ц彍鍗?
  return props.menus.find(menu => 
    menu.key === key && 
    menu.menuLevel === 1
  ) || null
}

// 涓€绾ц彍鍗曠偣鍑伙紙鍙屽垪甯冨眬锛?
const onFirstLevelMenuClick = (info: any) => {
  const key = info.key as string
  if (key) {
    selectedFirstLevelKeys.value = [key]
    
    // 鏌ユ壘褰撳墠鐐瑰嚮鐨勪竴绾ц彍鍗?
    const clickedMenu = findFirstLevelMenu(key)
    
    // 鍙湁闈炵洰褰曠被鍨嬬殑鑿滃崟鎵嶈Е鍙戣矾鐢辫烦杞?
    // 鐩綍绫诲瀷鑿滃崟鍙渶瑕佹洿鏂皊electedFirstLevelKeys锛岃currentMenus鑷姩鏇存柊鏄剧ず瀵瑰簲鐨勪簩绾ц彍鍗?
    if (clickedMenu && clickedMenu.type !== 'catalog') {
      emit('menu-click', key)
    }
  }
}

// 妯″潡鐐瑰嚮
const onModuleClick = (info: any) => {
  const key = info.key as string
  if (key) {
    emit('module-click', key)
  }
}

// 菜单点击
const onMenuClick = (info: any) => {
  const key = info.key as string
  if (key) {
    emit('menu-click', key)
  }
}

// 鑿滃崟灞曞紑/鏀惰捣
const onOpenChange = (keys: string[]) => {
  openKeys.value = keys
}

// 渚ц竟鏍忔姌鍙?灞曞紑
const onCollapse = (collapsed: boolean) => {
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
