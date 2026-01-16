<template>
  <a-config-provider :theme="antdTheme">
    <a-layout 
      class="fx-main-layout" 
      :style="rootStyle"
      :data-font-size="layoutConfig.fontSize"
    >
    <!-- 使用新的 AppHeader 组件 -->
    <AppHeader
      v-if="showHeader"
      :layout-mode="layoutConfig.layoutMode"
      :modules="moduleList"
      :active-module-code="activeModuleCode"
      :show-search="layoutConfig.widgetGlobalSearch"
      :show-lang-switch="layoutConfig.widgetLangSwitch"
      :show-refresh="layoutConfig.widgetRefresh"
      :user="currentUser"
      @module-click="onModuleClick"
      @search-click="globalSearchVisible = true"
      @locale-change="onLocaleChange"
      @refresh-click="refreshPage"
      @user-menu-click="onUserMenuClick"
      @settings-click="settingOpen = true"
    />

    <a-layout class="fx-main-content-wrapper">
      <!-- 使用新的 AppSidebar 组件 -->
      <AppSidebar
        v-if="layoutConfig.layoutMode !== 'top'"
        :menus="sidebarMenus"
        :modules="moduleList"
        :active-key="route.fullPath"
        :active-module-code="activeModuleCode"
        :collapsed="siderCollapsed"
        :double-column="layoutConfig.leftDoubleMenu || layoutConfig.layoutMode === 'vertical-mix' || layoutConfig.layoutMode === 'mix'"
        @module-click="onModuleClick"
        @menu-click="onMenuClick"
        @collapse-change="onCollapse"
      />

      <a-layout class="fx-content-layout">
        <!-- 使用新的 AppTabBar 组件 -->
        <AppTabBar
          v-if="layoutConfig.tabBarEnabled"
          :tabs="tabs"
          :active-key="activeTabKey"
          :draggable="layoutConfig.tabBarDraggable"
          @tab-click="onTabClick"
          @tab-close="onTabClose"
          @tab-drag="onTabDrag"
          @tab-refresh="onTabRefresh"
          @tabs-close="onTabsClose"
        />

        <a-layout-content class="fx-content">
          <div class="fx-content-inner">
            <div v-if="layoutConfig.watermarkEnabled" class="fx-watermark-container">
              <div class="fx-watermark" v-for="i in 12" :key="i">
                {{ layoutConfig.watermarkText }}
              </div>
            </div>
            <router-view v-slot="{ Component, route }">
              <transition
                v-if="layoutConfig.animateEnabled"
                :name="pageTransitionName"
              >
                <div class="fx-page-wrapper" :key="route.fullPath">
                  <component :is="Component" />
                </div>
              </transition>
              <div v-else class="fx-page-wrapper" :key="route.fullPath">
                <component :is="Component" />
              </div>
            </router-view>
          </div>
        </a-layout-content>
        
        <div v-if="layoutConfig.footerCopyrightEnabled" class="fx-footer">
          © 2025 FORGEX_MOM
        </div>
      </a-layout>
    </a-layout>

    <!-- 使用新的 GlobalSearch 组件 -->
    <GlobalSearch
      :visible="globalSearchVisible"
      :menus="searchMenus"
      @update:visible="globalSearchVisible = $event"
      @select="onGlobalSearchSelect"
    />

    <!-- 设置抽屉 -->
    <a-drawer
      v-model:open="settingOpen"
      placement="right"
      width="480"
      class="fx-setting-drawer"
    >
      <template #title>
        <div class="fx-setting-header">
          <div class="fx-setting-header-main">
            {{ t('layout.settingTitle') }}
          </div>
          <div class="fx-setting-header-sub">
            {{ t('layout.settingSubtitle') }}
          </div>
        </div>
      </template>
      <div class="fx-setting-drawer-body">
        <a-tabs class="fx-setting-tabs">
          <a-tab-pane :tab="t('layout.tabAppearance')" key="appearance">
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.theme') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.themeMode') }}</span>
                <a-select v-model:value="layoutConfig.themeMode" style="width: 160px">
                  <a-select-option value="light">{{ t('layout.themeLight') }}</a-select-option>
                  <a-select-option value="dark">{{ t('layout.themeDark') }}</a-select-option>
                  <a-select-option value="system">{{ t('layout.themeSystem') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.themeColor') }}</span>
                <a-select v-model:value="layoutConfig.themeColor" style="width: 200px">
                  <a-select-option value="#1677ff">{{ t('layout.themeColorDawnBlue') }}</a-select-option>
                  <a-select-option value="#722ED1">{{ t('layout.themeColorTwilightPurple') }}</a-select-option>
                  <a-select-option value="#13C2C2">{{ t('layout.themeColorCyan') }}</a-select-option>
                  <a-select-option value="#52C41A">{{ t('layout.themeColorAuroraGreen') }}</a-select-option>
                  <a-select-option value="#FA8C16">{{ t('layout.themeColorSunsetOrange') }}</a-select-option>
                  <a-select-option value="#F5222D">{{ t('layout.themeColorVolcanoRed') }}</a-select-option>
                  <a-select-option value="#8C8C8C">{{ t('layout.themeColorNeutralGray') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.fontSize') }}</span>
                <a-select v-model:value="layoutConfig.fontSize" style="width: 120px">
                  <a-select-option value="13px">{{ t('layout.fontSizeSmall') }}</a-select-option>
                  <a-select-option value="14px">{{ t('layout.fontSizeDefault') }}</a-select-option>
                  <a-select-option value="16px">{{ t('layout.fontSizeLarge') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.borderRadius') }}</span>
                <a-slider v-model:value="layoutConfig.borderRadius" :min="0" :max="16" style="width: 180px" />
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="t('layout.tabLayout')" key="layout">
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.menuLayout') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.layoutMode') }}</span>
                <a-select v-model:value="layoutConfig.layoutMode" style="width: 200px">
                  <a-select-option value="vertical">{{ t('layout.layoutVertical') }}</a-select-option>
                  <a-select-option value="vertical-mix">{{ t('layout.layoutVerticalMix') }}</a-select-option>
                  <a-select-option value="top">{{ t('layout.layoutTop') }}</a-select-option>
                  <a-select-option value="mix">{{ t('layout.layoutMix') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.leftDoubleMenu') }}</span>
                <a-switch v-model:checked="layoutConfig.leftDoubleMenu" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.contentWidth') }}</span>
                <a-select v-model:value="layoutConfig.contentWidth" style="width: 200px">
                  <a-select-option value="fluid">{{ t('layout.contentWidthFluid') }}</a-select-option>
                  <a-select-option value="fixed">{{ t('layout.contentWidthFixed') }}</a-select-option>
                </a-select>
              </div>
            </div>
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.header') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.headerVisible') }}</span>
                <a-switch v-model:checked="layoutConfig.headerVisible" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.headerMode') }}</span>
                <a-select v-model:value="layoutConfig.headerMode" style="width: 200px">
                  <a-select-option value="fixed">{{ t('layout.headerModeFixed') }}</a-select-option>
                  <a-select-option value="auto">{{ t('layout.headerModeAuto') }}</a-select-option>
                  <a-select-option value="hide-on-scroll">{{ t('layout.headerModeHideOnScroll') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.headerMenuAlign') }}</span>
                <a-select v-model:value="layoutConfig.headerMenuAlign" style="width: 200px">
                  <a-select-option value="left">{{ t('layout.headerMenuAlignLeft') }}</a-select-option>
                  <a-select-option value="center">{{ t('layout.headerMenuAlignCenter') }}</a-select-option>
                  <a-select-option value="right">{{ t('layout.headerMenuAlignRight') }}</a-select-option>
                </a-select>
              </div>
            </div>
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.tabBar') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.tabBarEnabled') }}</span>
                <a-switch v-model:checked="layoutConfig.tabBarEnabled" />
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="t('layout.tabCommon')" key="common">
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.common') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.watermark') }}</span>
                <a-switch v-model:checked="layoutConfig.watermarkEnabled" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.watermarkText') }}</span>
                <a-input v-model:value="layoutConfig.watermarkText" style="width: 200px" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.formMode') }}</span>
                <a-select v-model:value="appStore.formMode" style="width: 200px">
                  <a-select-option value="modal">{{ t('layout.formModeModal') }}</a-select-option>
                  <a-select-option value="drawer">{{ t('layout.formModeDrawer') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.animateEnabled') }}</span>
                <a-switch v-model:checked="layoutConfig.animateEnabled" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.pageTransition') }}</span>
                <a-select v-model:value="layoutConfig.pageTransition" style="width: 200px">
                  <a-select-option value="horizontal">{{ t('layout.pageTransitionHorizontal') }}</a-select-option>
                  <a-select-option value="fade">{{ t('layout.pageTransitionFade') }}</a-select-option>
                </a-select>
              </div>
            </div>
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.widgets') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.widgetRefresh') }}</span>
                <a-switch v-model:checked="layoutConfig.widgetRefresh" />
              </div>
            </div>
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.footer') }}</div>
              <div class="fx-setting-row">
                <span>{{ t('layout.footerCopyright') }}</span>
                <a-switch v-model:checked="layoutConfig.footerCopyrightEnabled" />
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
        <div class="fx-setting-footer">
          <a-divider />
          <div class="fx-setting-footer-actions">
            <a-button @click="resetLayout">{{ t('layout.reset') }}</a-button>
            <a-button type="primary" @click="saveLayout">{{ t('layout.save') }}</a-button>
          </div>
        </div>
      </div>
    </a-drawer>
  </a-layout>
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import { dynamicModules, dynamicRoutes, injectDynamicRoutes } from '../router'
import { getUserLayoutStyle, saveUserLayoutStyle } from '../api/system/userStyle'
import { changeLanguage } from '../api/auth/login'
import { getRoutes } from '../api/system/route'
import { setLocale } from '../locales'

import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import AppTabBar from './components/AppTabBar.vue'
import GlobalSearch from './components/GlobalSearch.vue'
// 导入新的主题系统
import { useSystemTheme, resolveThemeMode } from '../composables/useSystemTheme'
import { useAntdTheme } from '../theme/antdTheme'
import { lightTokens, darkTokens } from '../theme/tokens'
import { generateCSSVariablesWithCache } from '../theme/cssVariables'
import type { LayoutConfig } from '../theme/types'
import { useAppStore } from '../stores/app'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const appStore = useAppStore()
const userStore = useUserStore()

// 使用系统主题检测
const { systemTheme } = useSystemTheme()

interface LayoutConfig {
  leftDoubleMenu: boolean
  layoutMode: 'vertical' | 'vertical-mix' | 'top' | 'mix'
  contentWidth: 'fluid' | 'fixed'
  fontSize: string
  borderRadius: number
  themeMode: 'light' | 'dark' | 'system'
  themeColor: string
  headerVisible: boolean
  headerMode: 'fixed' | 'auto' | 'hide-on-scroll'
  headerMenuAlign: 'left' | 'center' | 'right'
  tabBarEnabled: boolean
  tabBarMax: number
  tabBarDraggable: boolean
  tabBarShowIcon: boolean
  tabBarStyle: 'chrome' | 'card'
  widgetGlobalSearch: boolean
  widgetThemeSwitch: boolean
  widgetLangSwitch: boolean
  widgetFullscreen: boolean
  widgetNotification: boolean
  widgetSiderCollapse: boolean
  widgetRefresh: boolean
  watermarkEnabled: boolean
  watermarkText: string
  animateEnabled: boolean
  loadingIndicatorEnabled: boolean
  pageTransition: 'horizontal' | 'fade'
  footerCopyrightEnabled: boolean
}

const DEFAULT_LAYOUT_CONFIG: LayoutConfig = {
  leftDoubleMenu: false,
  layoutMode: 'mix',
  contentWidth: 'fluid',
  fontSize: '14px',
  borderRadius: 6,
  themeMode: 'light',
  themeColor: '#1677ff',
  headerVisible: true,
  headerMode: 'fixed',
  headerMenuAlign: 'left',
  tabBarEnabled: true,
  tabBarMax: 10,
  tabBarDraggable: true,
  tabBarShowIcon: true,
  tabBarStyle: 'chrome',
  widgetGlobalSearch: true,
  widgetThemeSwitch: true,
  widgetLangSwitch: true,
  widgetFullscreen: true,
  widgetNotification: true,
  widgetSiderCollapse: true,
  widgetRefresh: true,
  watermarkEnabled: false,
  watermarkText: 'FORGEX_MOM',
  animateEnabled: true,
  loadingIndicatorEnabled: true,
  pageTransition: 'horizontal',
  footerCopyrightEnabled: true
}

const layoutConfig = ref<LayoutConfig>({ ...DEFAULT_LAYOUT_CONFIG })
const settingOpen = ref(false)
const siderCollapsed = ref(false)
const openKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const activeModuleCode = ref<string>('')
const tabs = ref<{ key: string; title: string; closable: boolean }[]>([])
const activeTabKey = ref<string>('')
const globalSearchVisible = ref(false)
const currentLocale = ref<string>((localStorage.getItem('fx-locale') as string) || (locale.value as string))
const currentAccount = ref<string>(sessionStorage.getItem('account') || '')

locale.value = currentLocale.value as any

// 解析实际的主题模式（处理 system 模式）
const resolvedMode = computed(() => 
  resolveThemeMode(layoutConfig.value.themeMode, systemTheme.value)
)

// 使用新的主题系统生成 Ant Design 主题配置
const { computedTheme: antdTheme } = useAntdTheme(layoutConfig, resolvedMode)

// 使用新的 CSS 变量生成器
const rootStyle = computed(() => {
  const mode = resolvedMode.value
  const tokens = mode === 'dark' ? darkTokens : lightTokens
  return generateCSSVariablesWithCache(tokens, layoutConfig.value)
})

const pageTransitionName = computed(() =>
  layoutConfig.value.pageTransition === 'fade' ? 'fx-fade' : 'fx-horizontal'
)

const headerHiddenByScroll = ref(false)
const lastScrollY = ref(typeof window !== 'undefined' ? window.scrollY || 0 : 0)

const showHeader = computed(() => layoutConfig.value.headerVisible && !headerHiddenByScroll.value)

function handleScroll() {
  if (layoutConfig.value.headerMode !== 'hide-on-scroll') {
    headerHiddenByScroll.value = false
    if (typeof window !== 'undefined') {
      lastScrollY.value = window.scrollY || 0
    }
    return
  }
  if (typeof window === 'undefined') return
  const current = window.scrollY || 0
  const diff = current - lastScrollY.value
  if (diff > 20) {
    headerHiddenByScroll.value = true
  } else if (diff < -20) {
    headerHiddenByScroll.value = false
  }
  lastScrollY.value = current
}

const moduleMenus = computed(() => {
  const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  const result: { code: string; name: string; items: { title: string; fullPath: string }[] }[] = []
  for (const mod of modules) {
    const code = String(mod.code || '')
    const name = String(mod.name || code)
    if (!code) continue
    const topRoute = routes.find((r: any) => (r.meta && r.meta.module) === code || r.path === code)
    const items: { title: string; fullPath: string }[] = []
    if (topRoute && Array.isArray(topRoute.children)) {
      for (const c of topRoute.children) {
        const childPath = String(c.path || '')
        const title = (c.meta && c.meta.title) || c.name || childPath
        const fullPath = `/workspace/${code}/${childPath}`.replace(/\/+/g, '/')
        items.push({ title, fullPath })
      }
    }
    result.push({ code, name, items })
  }
  return result
})

// 为 GlobalSearch 组件转换菜单数据
const searchMenus = computed(() => {
  const result: any[] = []
  for (const mod of moduleMenus.value) {
    for (const item of mod.items) {
      result.push({
        key: item.fullPath,
        title: item.title,
        path: item.fullPath,
        moduleCode: mod.code,
        moduleName: mod.name,
        type: 'menu'
      })
    }
  }
  return result
})

// 模块列表（包含图标）
const moduleList = computed(() => {
  const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []
  return modules.map((mod: any) => ({
    code: String(mod.code || ''),
    name: String(mod.name || mod.code || ''),
    icon: mod.icon || 'appstore',
    order: mod.order || 0
  }))
})

// 侧边栏菜单数据（转换为 AppSidebar 期望的格式）
const sidebarMenus = computed(() => {
  const result: any[] = []
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  
  // 在混合模式下，需要构建两级菜单结构
  if (layoutConfig.value.layoutMode === 'mix' && activeModuleCode.value) {
    // 找到当前模块的路由
    const topRoute = routes.find((r: any) => 
      r.path === activeModuleCode.value || (r.meta && r.meta.module === activeModuleCode.value)
    )
    
    if (topRoute && Array.isArray(topRoute.children)) {
      // 构建一级菜单（目录）和二三级菜单
      for (const child of topRoute.children) {
        const childPath = String(child.path || '')
        const fullPath = `/workspace/${activeModuleCode.value}/${childPath}`.replace(/\/+/g, '/')
        const title = (child.meta && child.meta.title) || child.name || childPath
        const icon = (child.meta && child.meta.icon) || ''
        const type = (child.meta && child.meta.type) || 'menu'
        const menuLevel = (child.meta && child.meta.menuLevel) || 1
        
        result.push({
          key: fullPath,
          title,
          icon,
          path: fullPath,
          moduleCode: activeModuleCode.value,
          type,
          menuLevel,
          children: [] // 暂时不支持多级菜单
        })
      }
    }
  } else {
    // 非混合模式，显示所有菜单
    for (const topRoute of routes) {
      const moduleCode = topRoute.path
      
      if (topRoute && Array.isArray(topRoute.children)) {
        for (const child of topRoute.children) {
          const childPath = String(child.path || '')
          const fullPath = `/workspace/${moduleCode}/${childPath}`.replace(/\/+/g, '/')
          const title = (child.meta && child.meta.title) || child.name || childPath
          const icon = (child.meta && child.meta.icon) || ''
          const type = (child.meta && child.meta.type) || 'menu'
          const menuLevel = (child.meta && child.meta.menuLevel) || 1
          
          result.push({
            key: fullPath,
            title,
            icon,
            path: fullPath,
            moduleCode,
            type,
            menuLevel,
            children: []
          })
        }
      }
    }
  }
  
  return result
})

// 当前用户信息
const currentUser = computed(() => {
  const info = userStore.userInfo || {
    account: currentAccount.value,
    username: currentAccount.value,
    avatar: ''
  }
  
  // 处理头像 URL
  let avatar = info.avatar || ''
  if (avatar) {
    if (avatar.startsWith('http') || avatar.startsWith('data:')) {
      // 已经是绝对路径或 base64，保持不变
    } else if (avatar.startsWith('/api')) {
      // 已经是 /api 开头，保持不变
    } else if (avatar.startsWith('/')) {
      // 相对路径，补充 /api
      avatar = `/api${avatar}`
    } else {
      // 相对路径，补充 /api/
      avatar = `/api/${avatar}`
    }
  }
  
  return {
    account: info.account,
    name: info.username || info.account,
    avatar
  }
})

watch(
  () => route.fullPath,
  (path) => {
    selectedKeys.value = [path]
    
    // 优先从路由 meta 中获取模块代码
    if (route.meta && route.meta.module) {
      const code = route.meta.module as string
      openKeys.value = [code]
      activeModuleCode.value = code
      console.log('[MainLayout] Active module from meta:', code)
    } else {
      // 降级策略：从 URL 路径中解析
      const parts = path.split('/').filter(Boolean)
      if (parts.length >= 2 && parts[0] === 'workspace') {
        const code = parts[1]
        openKeys.value = [code]
        activeModuleCode.value = code
        console.log('[MainLayout] Active module from path:', code)
      }
    }
    
    updateTabsByRoute(path)
  },
  { immediate: true }
)

function normalizeLayoutConfig(raw: any): LayoutConfig {
  const cfg = { ...DEFAULT_LAYOUT_CONFIG, ...(raw || {}) }
  return {
    leftDoubleMenu: !!cfg.leftDoubleMenu,
    layoutMode: cfg.layoutMode === 'vertical' || cfg.layoutMode === 'vertical-mix' || cfg.layoutMode === 'top' || cfg.layoutMode === 'mix' ? cfg.layoutMode : DEFAULT_LAYOUT_CONFIG.layoutMode,
    contentWidth: cfg.contentWidth === 'fixed' ? 'fixed' : 'fluid',
    fontSize: typeof cfg.fontSize === 'string' ? cfg.fontSize : DEFAULT_LAYOUT_CONFIG.fontSize,
    borderRadius: typeof cfg.borderRadius === 'number' ? cfg.borderRadius : DEFAULT_LAYOUT_CONFIG.borderRadius,
    themeMode: cfg.themeMode === 'dark' || cfg.themeMode === 'system' ? cfg.themeMode : 'light',
    themeColor: typeof cfg.themeColor === 'string' && cfg.themeColor ? cfg.themeColor : DEFAULT_LAYOUT_CONFIG.themeColor,
    headerVisible: cfg.headerVisible !== false,
    headerMode: cfg.headerMode === 'auto' || cfg.headerMode === 'hide-on-scroll' ? cfg.headerMode : 'fixed',
    headerMenuAlign: cfg.headerMenuAlign === 'center' || cfg.headerMenuAlign === 'right' ? cfg.headerMenuAlign : 'left',
    tabBarEnabled: !!cfg.tabBarEnabled,
    tabBarMax: typeof cfg.tabBarMax === 'number' && cfg.tabBarMax > 0 ? cfg.tabBarMax : DEFAULT_LAYOUT_CONFIG.tabBarMax,
    tabBarDraggable: !!cfg.tabBarDraggable,
    tabBarShowIcon: !!cfg.tabBarShowIcon,
    tabBarStyle: cfg.tabBarStyle === 'card' ? 'card' : 'chrome',
    widgetGlobalSearch: !!cfg.widgetGlobalSearch,
    widgetThemeSwitch: !!cfg.widgetThemeSwitch,
    widgetLangSwitch: !!cfg.widgetLangSwitch,
    widgetFullscreen: !!cfg.widgetFullscreen,
    widgetNotification: !!cfg.widgetNotification,
    widgetSiderCollapse: !!cfg.widgetSiderCollapse,
    widgetRefresh: !!cfg.widgetRefresh,
    watermarkEnabled: !!cfg.watermarkEnabled,
    watermarkText: typeof cfg.watermarkText === 'string' && cfg.watermarkText ? cfg.watermarkText : DEFAULT_LAYOUT_CONFIG.watermarkText,
    animateEnabled: cfg.animateEnabled !== false,
    loadingIndicatorEnabled: cfg.loadingIndicatorEnabled !== false,
    pageTransition: cfg.pageTransition === 'fade' ? 'fade' : 'horizontal',
    footerCopyrightEnabled: !!cfg.footerCopyrightEnabled
  }
}

function onCollapse(v: boolean) {
  siderCollapsed.value = v
}

function onOpenChange(keys: string[]) {
  openKeys.value = keys
}

function onMenuClick(menuKey: string) {
  console.log('[MainLayout] Menu clicked:', menuKey)
  if (!menuKey) return
  if (menuKey !== route.fullPath) {
    console.log('[MainLayout] Navigating to:', menuKey)
    router.push(menuKey)
  }
}

function refreshPage() {
  router.replace({ path: '/redirect', query: { to: route.fullPath, t: Date.now() } })
}

function onModuleClick(moduleCode: string) {
  if (!moduleCode) return
  activeModuleCode.value = moduleCode
  // 切换模块时，跳转到该模块的第一个菜单项
  const mod = moduleMenus.value.find(m => m.code === moduleCode)
  if (mod && mod.items.length > 0) {
    const firstItem = mod.items[0]
    if (firstItem.fullPath !== route.fullPath) {
      router.push(firstItem.fullPath)
    }
  }
}

function resetLayout() {
  layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
}

async function loadLayout() {
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  if (!account || !tenantId) {
    layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
    return
  }
  try {
    const cached = localStorage.getItem('fx-layout-config')
    if (cached) {
      try {
        const parsed = JSON.parse(cached)
        layoutConfig.value = normalizeLayoutConfig(parsed)
      } catch (e) {}
    }
    const res = await getUserLayoutStyle({ account, tenantId })
    if (res) {
      layoutConfig.value = normalizeLayoutConfig(res)
      localStorage.setItem('fx-layout-config', JSON.stringify(layoutConfig.value))
    } else {
      layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
      localStorage.setItem('fx-layout-config', JSON.stringify(layoutConfig.value))
    }
  } catch (e) {
    if (!localStorage.getItem('fx-layout-config')) {
      layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
      localStorage.setItem('fx-layout-config', JSON.stringify(layoutConfig.value))
    }
  }
}

async function saveLayout() {
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  if (!account || !tenantId) return
  const normalized = normalizeLayoutConfig(layoutConfig.value)
  layoutConfig.value = normalized
  try {
    localStorage.setItem('fx-layout-config', JSON.stringify(normalized))
  } catch (e) {}
  try {
    await saveUserLayoutStyle({ account, tenantId, config: normalized })
    settingOpen.value = false
  } catch (e) {
    // 交由全局拦截器处理错误提示
  }
}

function onGlobalSearchSelect(menuKey: string, path: string) {
  if (path && path !== route.fullPath) {
    router.push(path)
  }
  globalSearchVisible.value = false
}

async function onLocaleChange(val: string) {
  try {
    // 1. 调用setLocale函数更新语言设置，该函数会：
    //    - 更新vue-i18n的locale值
    //    - 将语言设置保存到localStorage
    //    - 更新HTML的lang属性
    setLocale(val as 'zh-CN' | 'en-US')
    
    // 2. 更新本地状态
    currentLocale.value = val
    appStore.setLocale(val as 'zh-CN' | 'en-US')
    
    // 3. 调用后端API更新语言设置
    await changeLanguage({ lang: val })
    
    // 4. 重新获取菜单数据，确保国际化生效
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    if (account && tenantId) {
      try {
        // 重新获取菜单数据
        const routes = await getRoutes({ account, tenantId })
        if (routes) {
          // 重新注入动态路由
          await injectDynamicRoutes(routes)
          // 清除localStorage中的旧菜单缓存
          localStorage.removeItem('fx-dynamic-routes')
          localStorage.removeItem('fx-dynamic-modules')
          // 刷新当前页面，确保所有组件都能获取到最新的国际化数据
          // 使用router.replace配合redirect页面实现无刷新重载
          router.replace({ 
            path: '/redirect', 
            query: { 
              to: route.fullPath, 
              t: Date.now() 
            } 
          })
        }
      } catch (menuError) {
        console.error('重新获取菜单数据失败:', menuError)
        // 菜单获取失败时，不刷新页面，保持当前状态
        message.warning('菜单数据更新失败，请手动刷新页面')
      }
    }
  } catch (e) {
    console.error('语言切换失败:', e)
    // 语言切换失败时，保持当前的语言设置
    message.error('语言切换失败，请稍后重试')
  }
}

function onUserMenuClick(key: string) {
  if (!key) return
  
  if (key === 'logout') {
    sessionStorage.removeItem('account')
    sessionStorage.removeItem('tenantId')
    localStorage.removeItem('fx-layout-config')
    message.success('已退出登录')
    router.replace('/login')
    return
  }
  
  if (key === 'profile') {
    router.push('/workspace/profile')
    return
  }
  
  if (key === 'password') {
    router.push('/workspace/profile?tab=security')
    return
  }
  
  if (key === 'resetPassword') {
    message.info('重置密码功能暂未实现')
  }
}

function updateTabsByRoute(path: string) {
  if (!path.startsWith('/workspace')) {
    return
  }
  
  // 移除查询参数，确保 tab key 唯一且不包含参数
  // 例如：/workspace/profile?tab=security -> /workspace/profile
  const pathWithoutQuery = path.split('?')[0]
  
  // 仍然使用 activeTabKey 来高亮当前 tab，但 key 本身存储的是无参数路径
  // 这样无论 query 怎么变，tab 都是同一个
  // activeTabKey.value = pathWithoutQuery 
  // 注意：Antdv Tabs 的 activeKey 必须匹配 TabPane 的 key
  // 如果我们希望 tab 保持激活状态，我们需要让 activeTabKey 也指向无参数路径
  activeTabKey.value = pathWithoutQuery

  const clean = pathWithoutQuery.replace(/^\/workspace\//, '')
  const parts = clean.split('/').filter(Boolean)
  if (parts.length >= 1) {
    const moduleCode = parts[0]
    
    // 排除 profile 模块，不自动添加仪表盘
    if (moduleCode === 'profile') {
      // 不添加仪表盘，直接处理 profile tab
    } else {
      const dashboardPath = `/workspace/${moduleCode}/dashboard`
      const idx = tabs.value.findIndex(t => t.key === dashboardPath)
      if (idx === -1) {
        const dashboardTitle = buildTitleFromRoute(dashboardPath)
        const max = layoutConfig.value.tabBarMax || 10
        if (tabs.value.length >= max) {
          const removeIdx = tabs.value.findIndex(t => t.key !== activeTabKey.value)
          if (removeIdx >= 0) {
            tabs.value.splice(removeIdx, 1)
          } else {
            tabs.value.shift()
          }
        }
        tabs.value.unshift({
          key: dashboardPath,
          title: dashboardTitle,
          closable: false
        })
      } else if (idx > 0) {
        const [homeTab] = tabs.value.splice(idx, 1)
        tabs.value.unshift({ ...homeTab, closable: false })
      }
    }
  }

  const exists = tabs.value.find(t => t.key === pathWithoutQuery)
  if (exists) {
    return
  }
  const title = buildTitleFromRoute(pathWithoutQuery)
  const max = layoutConfig.value.tabBarMax || 10
  if (tabs.value.length >= max) {
    const idx = tabs.value.findIndex(t => t.key !== activeTabKey.value)
    if (idx >= 0) {
      tabs.value.splice(idx, 1)
    } else {
      tabs.value.shift()
    }
  }
  tabs.value.push({
    key: pathWithoutQuery,
    title,
    closable: tabs.value.length > 0
  })
}

function buildTitleFromRoute(path: string): string {
  // 确保传入的 path 不包含 query
  const pathWithoutQuery = path.split('?')[0]
  
  // 1. 尝试直接从路由表中查找（支持静态路由和动态路由）
  const allRoutes = router.getRoutes()
  const match = allRoutes.find(r => r.path === pathWithoutQuery)
  if (match && match.meta && match.meta.title) {
    return match.meta.title as string
  }

  // 2. 如果没找到，尝试通过模块结构查找（降级策略）
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  const clean = pathWithoutQuery.replace(/^\/workspace\//, '')
  const parts = clean.split('/').filter(Boolean)
  if (parts.length < 1) {
    return '首页'
  }
  const moduleCode = parts[0]
  const childPath = parts[1]
  const topRoute = routes.find((r: any) => r.path === moduleCode || (r.meta && r.meta.module === moduleCode))
  if (topRoute && Array.isArray(topRoute.children) && childPath) {
    const child = topRoute.children.find((c: any) => String(c.path || '') === childPath)
    if (child && child.meta && child.meta.title) {
      return child.meta.title
    }
    if (child && child.name) {
      return child.name
    }
  }
  const mod = (Array.isArray(dynamicModules.value) ? dynamicModules.value : []).find((m: any) => String(m.code || '') === moduleCode)
  if (mod && mod.name) {
    return mod.name
  }
  return clean || pathWithoutQuery
}

function onTabClick(tab: { key: string }) {
  if (tab.key && tab.key !== route.fullPath) {
    router.push(tab.key)
  }
}

function onTabClose(tab: { key: string }) {
  const key = tab.key
  const idx = tabs.value.findIndex(t => t.key === key)
  if (idx === -1) return
  const isActive = activeTabKey.value === key
  tabs.value.splice(idx, 1)
  if (!isActive) return
  const next = tabs.value[idx] || tabs.value[idx - 1]
  if (next) {
    router.push(next.key)
  } else {
    router.push('/workspace')
  }
}

function onTabDrag(fromIndex: number, toIndex: number) {
  if (!layoutConfig.value.tabBarDraggable) return
  if (fromIndex === -1 || toIndex === -1 || fromIndex === toIndex) return
  const moved = tabs.value.splice(fromIndex, 1)[0]
  tabs.value.splice(toIndex, 0, moved)
}

function onTabRefresh(tab: { key: string }) {
  const key = tab.key
  if (key === route.fullPath) {
    refreshPage()
  } else {
    router.push(key).then(() => {
      refreshPage()
    })
  }
}

function onTabsClose(action: 'others' | 'left' | 'right' | 'all', tab?: { key: string }) {
  const key = tab?.key
  
  if (action === 'others' && key) {
    tabs.value = tabs.value.filter(t => t.key === key)
    if (key !== route.fullPath) {
      router.push(key)
    }
  } else if (action === 'left' && key) {
    const idx = tabs.value.findIndex(t => t.key === key)
    if (idx > 0) {
      tabs.value = tabs.value.slice(idx)
    }
  } else if (action === 'right' && key) {
    const idx = tabs.value.findIndex(t => t.key === key)
    if (idx !== -1 && idx < tabs.value.length - 1) {
      tabs.value = tabs.value.slice(0, idx + 1)
    }
  } else if (action === 'all') {
    tabs.value = []
    router.push('/workspace')
  }
}

onMounted(() => {
  if (typeof window !== 'undefined') {
    window.addEventListener('scroll', handleScroll)
  }
  loadLayout()
  updateTabsByRoute(route.fullPath)
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('scroll', handleScroll)
  }
})
</script>

<style scoped lang="less">
@import '../styles/main-layout.less';
</style>


<style scoped lang="less">
.fx-main-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.fx-main-content-wrapper {
  flex: 1;
  overflow: hidden;
}

.fx-content-layout {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.fx-content {
  flex: 1;
  overflow: auto;
}
</style>
