<template>
  <a-config-provider :theme="antdTheme" :locale="antdLocale">
    <a-layout 
      ref="layoutRootRef"
      class="fx-main-layout" 
      :style="rootStyle"
      :data-font-size="layoutConfig.fontSize"
    >
    <!-- 使用新的 AppHeader 组件 -->
  <AppHeader
      v-if="showHeader"
      :logo="headerLogo"
      :title="headerTitle"
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
      @refresh="refreshPage"
      @message-click="openMessageDrawer"
      @user-menu-click="onUserMenuClick"
      @settings-click="settingOpen = true"
    />

    <a-layout class="fx-main-content-wrapper">
      <!-- 使用新的 AppSidebar 组件 -->
      <AppSidebar
        v-if="shouldShowSidebar"
        :menus="sidebarMenus"
        :modules="moduleList"
        :active-key="normalizeWorkspacePath(route.fullPath)"
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
          {{ systemConfig.copyright }}
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

    <!-- 消息通知抽屉 -->
    <a-drawer
      v-model:open="messageDrawerOpen"
      title="消息通知"
      placement="right"
      width="400"
      class="fx-message-drawer"
    >
      <a-spin :spinning="messageLoading">
        <div v-if="messageList.length === 0" class="fx-message-empty">
          <a-empty description="暂无消息" />
        </div>
        <div v-else class="fx-message-list">
          <div
            v-for="msg in messageList"
            :key="msg.id"
            class="fx-message-item"
          >
            <div class="fx-message-title">{{ msg.title }}</div>
            <div class="fx-message-content">{{ msg.content }}</div>
            <div class="fx-message-time">{{ msg.createTime }}</div>
          </div>
        </div>
      </a-spin>
    </a-drawer>

    <!-- 设置抽屉 -->
    <a-drawer
      v-model:open="settingOpen"
      placement="right"
      width="480"
      class="fx-setting-drawer"
      :get-container="getSettingDrawerContainer"
      :root-style="settingDrawerRootStyle"
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
              
              <!-- 主题模式卡片选择器 -->
              <div class="fx-setting-section">
                <div class="fx-card-grid fx-card-grid--mode">
                  <button
                    v-for="mode in themeModeOptions"
                    :key="mode.value"
                    type="button"
                    class="fx-mode-card"
                    :class="{ 'fx-mode-card--active': layoutConfig.themeMode === mode.value }"
                    @click="layoutConfig.themeMode = mode.value"
                  >
                    <component :is="mode.icon" class="fx-mode-card__icon" />
                    <span class="fx-mode-card__label">{{ mode.label }}</span>
                  </button>
                </div>
              </div>

              <!-- 主题颜色卡片选择器 -->
              <div class="fx-setting-section">
                <div class="fx-setting-title fx-setting-title--sub">内置主题</div>
                <div class="fx-card-grid fx-card-grid--color">
                  <button
                    v-for="color in themeColorOptions"
                    :key="color.value"
                    type="button"
                    class="fx-color-card"
                    :class="{ 'fx-color-card--active': layoutConfig.themeColor === color.value }"
                    @click="layoutConfig.themeColor = color.value"
                  >
                    <span class="fx-color-card__swatch" :style="{ background: color.value }"></span>
                    <span class="fx-color-card__label">{{ color.label }}</span>
                  </button>
                </div>
              </div>

              <!-- 字体大小滑块 -->
              <div class="fx-setting-row fx-setting-row--slider">
                <span>{{ t('layout.fontSize') }}</span>
                <div class="fx-setting-slider">
                  <a-slider v-model:value="fontSizeSliderValue" :min="FONT_SIZE_MIN" :max="FONT_SIZE_MAX" />
                  <span class="fx-setting-slider__value">{{ fontSizeSliderValue }}px</span>
                </div>
              </div>

              <!-- 圆角大小滑块 -->
              <div class="fx-setting-row">
                <span>{{ t('layout.borderRadius') }}</span>
                <a-slider v-model:value="layoutConfig.borderRadius" :min="0" :max="16" style="width: 180px" />
              </div>
            </div>
          </a-tab-pane>
          <a-tab-pane :tab="t('layout.tabLayout')" key="layout">
            <div class="fx-setting-block">
              <div class="fx-setting-title">{{ t('layout.menuLayout') }}</div>
              
              <!-- 布局模式卡片选择器 -->
              <div class="fx-setting-section">
                <span class="fx-setting-label">{{ t('layout.layoutMode') }}</span>
                <div class="fx-card-grid fx-card-grid--layout">
                  <button
                    v-for="mode in layoutModeOptions"
                    :key="mode.value"
                    type="button"
                    class="fx-layout-card"
                    :class="{ 'fx-layout-card--active': layoutConfig.layoutMode === mode.value }"
                    @click="layoutConfig.layoutMode = mode.value"
                  >
                    <div class="fx-layout-card__preview" v-html="mode.preview"></div>
                    <span class="fx-layout-card__label">{{ mode.label }}</span>
                  </button>
                </div>
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

    <a-modal
      v-model:open="messageSendOpen"
      title="发送站内消息"
      :confirm-loading="false"
      @ok="handleMessageSend"
    >
      <a-form layout="vertical">
        <a-form-item label="接收用户" required>
          <a-input
            v-model:value="selectedUserName"
            readonly
            placeholder="点击选择用户"
            @click="openUserSelectModal"
          >
            <template #suffix>
              <SearchOutlined style="color: var(--fx-text-tertiary)" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item label="标题" required>
          <a-input v-model:value="messageSendForm.title" placeholder="请输入消息标题" />
        </a-form-item>

        <a-form-item label="内容" required>
          <a-textarea v-model:value="messageSendForm.content" :rows="4" placeholder="请输入消息内容" />
        </a-form-item>

        <a-form-item label="跳转链接">
          <a-input v-model:value="messageSendForm.linkUrl" placeholder="可选，点击消息后跳转的链接" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 用户选择弹窗 -->
    <a-modal
      v-model:open="userSelectOpen"
      title="选择用户"
      width="600px"
      @ok="confirmUserSelect"
    >
      <a-input-search
        v-model:value="userSearchKeyword"
        placeholder="搜索用户名或账号"
        style="margin-bottom: 16px"
        @search="searchUsers"
      />
      <a-table
        :data-source="userSelectList"
        :columns="userSelectColumns"
        :row-selection="{
          selectedRowKeys: selectedUserIds,
          onChange: onUserSelectChange,
          type: 'radio'
        }"
        row-key="id"
        :loading="userSelectLoading"
        :pagination="{ pageSize: 10 }"
        size="small"
      />
    </a-modal>
  </a-layout>
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, notification } from 'ant-design-vue'
import enUS from 'ant-design-vue/es/locale/en_US'
import jaJP from 'ant-design-vue/es/locale/ja_JP'
import koKR from 'ant-design-vue/es/locale/ko_KR'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import zhTW from 'ant-design-vue/es/locale/zh_TW'
import { PERSONAL_HOME_PATH, dynamicModules, dynamicRoutes, injectDynamicRoutes } from '../router'
import { getUserLayoutStyle, saveUserLayoutStyle } from '../api/system/userStyle'
import { changeLanguage } from '../api/auth/login'
import { getRoutes } from '../api/system/route'
import { getSystemBasicConfig } from '../api/system/config'
import { setLocale, type LocaleCode } from '../locales'
import { listUnreadMessages, markMessageRead, sendMessage, type SysMessageVO } from '../api/system/message'
import { getUserList } from '../api/system/user'
import { useSse } from '../hooks/useSse'

import {
  SearchOutlined,
  BellOutlined,
  SettingOutlined,
  DownOutlined,
  UserOutlined,
  KeyOutlined,
  MailOutlined,
  LogoutOutlined,
  AppstoreOutlined,
  SyncOutlined,
  HighlightOutlined,
  EyeInvisibleOutlined,
  DesktopOutlined
} from '@ant-design/icons-vue'

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
import type { SystemBasicConfig } from '../api/system/config'

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

const layoutRootRef = ref<HTMLElement | { $el?: unknown } | null>(null)
const layoutConfig = ref<LayoutConfig>({ ...DEFAULT_LAYOUT_CONFIG })
const settingOpen = ref(false)
const messageDrawerOpen = ref(false)
const messageList = ref<any[]>([])
const messageLoading = ref(false)
const FONT_SIZE_MIN = 14
const FONT_SIZE_MAX = 28
const FONT_SIZE_DEFAULT = 14
const settingDrawerRootStyle = {
  position: 'absolute',
} as const

function clamp(value: number, min: number, max: number): number {
  return Math.min(Math.max(value, min), max)
}

function parseFontSizeValue(fontSize: unknown): number {
  if (typeof fontSize === 'number' && Number.isFinite(fontSize)) {
    return clamp(Math.round(fontSize), FONT_SIZE_MIN, FONT_SIZE_MAX)
  }
  if (typeof fontSize === 'string') {
    const parsed = Number.parseInt(fontSize, 10)
    if (Number.isFinite(parsed)) {
      return clamp(parsed, FONT_SIZE_MIN, FONT_SIZE_MAX)
    }
  }
  return FONT_SIZE_DEFAULT
}

function normalizeFontSize(fontSize: unknown): string {
  return `${parseFontSizeValue(fontSize)}px`
}

function resolveLayoutRootElement(): HTMLElement | null {
  const current = layoutRootRef.value
  if (!current) {
    return null
  }
  if (current instanceof HTMLElement) {
    return current
  }
  if ('$el' in current && current.$el instanceof HTMLElement) {
    return current.$el
  }
  return null
}

/**
 * 将设置抽屉挂到主布局容器内，确保 drawer 内仍能继承 `--fx-*` 主题变量。
 */
function getSettingDrawerContainer(): HTMLElement {
  const layoutRoot =
    resolveLayoutRootElement()
    ?? (typeof document !== 'undefined' ? document.querySelector('.fx-main-layout') : null)
  if (layoutRoot instanceof HTMLElement) {
    return layoutRoot
  }
  return document.body
}

/**
 * 主题模式选项配置
 * <p>
 * 提供三种主题模式：浅色、暗色、跟随系统
 * 每个选项包含值、标签和对应图标
 * </p>
 */
const themeModeOptions = [
  { value: 'light', label: '浅色', icon: HighlightOutlined },
  { value: 'dark', label: '暗色', icon: EyeInvisibleOutlined },
  { value: 'system', label: '跟随系统', icon: DesktopOutlined },
]

/**
 * 主题颜色选项配置
 * <p>
 * 提供 7 种预设主题色，每种颜色包含 HEX 值和中文标签
 * 颜色经过精心挑选，确保视觉舒适度和可访问性
 * </p>
 */
const themeColorOptions = [
  { value: '#1677ff', label: '默认' },
  { value: '#7c5cff', label: '紫罗兰' },
  { value: '#ec4899', label: '樱花粉' },
  { value: '#f6c445', label: '柠檬黄' },
  { value: '#5b8ff9', label: '天蓝色' },
  { value: '#34d399', label: '浅绿色' },
  { value: '#71717a', label: '锌色灰' },
  { value: '#14b8a6', label: '深绿色' },
  { value: '#1d4ed8', label: '深蓝色' },
  { value: '#f97316', label: '橙黄色' },
  { value: '#e11d48', label: '玫瑰红' },
  { value: '#525252', label: '中性色' },
  { value: '#475569', label: '石板灰' },
  { value: '#6b7280', label: '中灰色' },
]

const fontSizeSliderValue = computed({
  get: () => parseFontSizeValue(layoutConfig.value.fontSize),
  set: (value: number) => {
    layoutConfig.value.fontSize = `${clamp(Math.round(value), FONT_SIZE_MIN, FONT_SIZE_MAX)}px`
  },
})

/**
 * 布局模式选项配置
 * <p>
 * 提供四种布局模式，每种模式包含预览 SVG 图标和中文标签
 * 预览图使用简化的布局结构示意
 * </p>
 */
const layoutModeOptions = [
  {
    value: 'vertical',
    label: '垂直',
    preview: `<svg viewBox="0 0 80 56" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="0.5" y="0.5" width="79" height="55" rx="4" stroke="currentColor" stroke-opacity="0.15" fill="transparent"/>
      <rect x="2" y="2" width="20" height="52" rx="2" fill="currentColor" fill-opacity="0.12"/>
      <rect x="24" y="2" width="54" height="8" rx="1" fill="currentColor" fill-opacity="0.08"/>
      <rect x="24" y="12" width="54" height="42" rx="1" fill="currentColor" fill-opacity="0.04"/>
      <rect x="6" y="10" width="12" height="3" rx="1" fill="currentColor" fill-opacity="0.35"/>
      <rect x="6" y="16" width="10" height="2" rx="1" fill="currentColor" fill-opacity="0.2"/>
      <rect x="6" y="21" width="10" height="2" rx="1" fill="currentColor" fill-opacity="0.2"/>
    </svg>`,
  },
  {
    value: 'vertical-mix',
    label: '垂直双列',
    preview: `<svg viewBox="0 0 80 56" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="0.5" y="0.5" width="79" height="55" rx="4" stroke="currentColor" stroke-opacity="0.15" fill="transparent"/>
      <rect x="2" y="2" width="12" height="52" rx="2" fill="currentColor" fill-opacity="0.12"/>
      <rect x="16" y="2" width="18" height="52" rx="2" fill="currentColor" fill-opacity="0.08"/>
      <rect x="36" y="2" width="42" height="52" rx="2" fill="currentColor" fill-opacity="0.04"/>
      <rect x="4" y="8" width="8" height="3" rx="1" fill="currentColor" fill-opacity="0.35"/>
      <rect x="19" y="8" width="12" height="2" rx="1" fill="currentColor" fill-opacity="0.25"/>
      <rect x="19" y="13" width="10" height="2" rx="1" fill="currentColor" fill-opacity="0.18"/>
    </svg>`,
  },
  {
    value: 'top',
    label: '水平',
    preview: `<svg viewBox="0 0 80 56" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="0.5" y="0.5" width="79" height="55" rx="4" stroke="currentColor" stroke-opacity="0.15" fill="transparent"/>
      <rect x="2" y="2" width="76" height="8" rx="2" fill="currentColor" fill-opacity="0.12"/>
      <rect x="2" y="12" width="76" height="42" rx="2" fill="currentColor" fill-opacity="0.04"/>
      <rect x="6" y="4" width="16" height="4" rx="1" fill="currentColor" fill-opacity="0.35"/>
      <rect x="28" y="4" width="12" height="4" rx="1" fill="currentColor" fill-opacity="0.2"/>
      <rect x="44" y="4" width="12" height="4" rx="1" fill="currentColor" fill-opacity="0.2"/>
    </svg>`,
  },
  {
    value: 'mix',
    label: '混合',
    preview: `<svg viewBox="0 0 80 56" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect x="0.5" y="0.5" width="79" height="55" rx="4" stroke="currentColor" stroke-opacity="0.15" fill="transparent"/>
      <rect x="2" y="2" width="76" height="8" rx="2" fill="currentColor" fill-opacity="0.12"/>
      <rect x="2" y="12" width="18" height="42" rx="2" fill="currentColor" fill-opacity="0.08"/>
      <rect x="22" y="12" width="56" height="42" rx="2" fill="currentColor" fill-opacity="0.04"/>
      <rect x="6" y="4" width="14" height="4" rx="1" fill="currentColor" fill-opacity="0.35"/>
      <rect x="26" y="4" width="10" height="4" rx="1" fill="currentColor" fill-opacity="0.2"/>
      <rect x="5" y="16" width="12" height="2" rx="1" fill="currentColor" fill-opacity="0.25"/>
      <rect x="5" y="21" width="10" height="2" rx="1" fill="currentColor" fill-opacity="0.18"/>
    </svg>`,
  },
]
const siderCollapsed = ref(false)
const openKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const activeModuleCode = ref<string>('')
const tabs = ref<{ key: string; title: string; closable: boolean }[]>([])
const activeTabKey = ref<string>('')
const RECENT_ROUTE_STORAGE_KEY = 'fx-recent-routes'
const MAX_RECENT_ROUTE_COUNT = 20
const globalSearchVisible = ref(false)
const currentLocale = ref<string>((localStorage.getItem('fx-locale') as string) || (locale.value as string))
const currentAccount = ref<string>(sessionStorage.getItem('account') || '')
const messageSendOpen = ref(false)
const messageSendForm = ref({
  receiverTenantId: Number(sessionStorage.getItem('tenantId') || '') || undefined,
  receiverUserId: undefined as unknown as number,
  scope: 'INTERNAL',
  title: '',
  content: '',
  linkUrl: '',
  bizType: '',
})

// 用户选择相关
const userSelectOpen = ref(false)
const userSelectList = ref<any[]>([])
const userSelectLoading = ref(false)
const selectedUserIds = ref<string[]>([])
const selectedUserName = ref('')
const userSearchKeyword = ref('')

const userSelectColumns = [
  { title: '用户名', dataIndex: 'username', width: 120 },
  { title: '账号', dataIndex: 'account', width: 120 },
  { title: '部门', dataIndex: 'departmentName', ellipsis: true }
]

const antdLocale = computed(() => {
  const key = String(currentLocale.value || locale.value || '')
  if (key === 'en-US') return enUS
  if (key === 'zh-TW') return zhTW
  if (key === 'ja-JP') return jaJP
  if (key === 'ko-KR') return koKR
  return zhCN
})

const systemConfig = ref<SystemBasicConfig>({
  systemName: 'FORGEX_MOM',
  systemLogo: '',
  systemVersion: '1.0.0',
  copyright: '© 2025 FORGEX_MOM',
  copyrightLink: '#',
  loginPageTitle: '欢迎来到FORGEX_MOM！',
  loginPageSubtitle: '',
  loginBackgroundType: 'image',
  loginBackgroundVideo: '/loading.mp4',
  loginBackgroundImage: '/back.jpg',
  loginBackgroundColor: '#0d0221',
  loginStyle: 'cyber',
  showOAuthLogin: true,
  primaryColor: '#05d9e8',
  secondaryColor: '#ff2a6d'
})

function formatMediaUrl(value: string): string {
  const url = String(value || '')
  if (!url) return ''
  if (url.startsWith('data:') || url.startsWith('http://') || url.startsWith('https://')) {
    return url
  }
  if (url.startsWith('/files/')) {
    return `/api${url}`
  }
  if (url.startsWith('/')) {
    return url.startsWith('/api/') ? url : url
  }
  return `/api/${url}`
}

const headerLogo = computed(() => formatMediaUrl(systemConfig.value.systemLogo))
const headerTitle = computed(() => String(systemConfig.value.systemName || 'Forgex MOM'))

locale.value = currentLocale.value as any

// 解析实际的主题模式（处理 system 模式）
const resolvedMode = computed(() => 
  resolveThemeMode(layoutConfig.value.themeMode, systemTheme.value)
)

function normalizeWorkspacePath(path: string) {
  return String(path || '').split('?')[0]
}

function buildFixedTabs() {
  return [{
    key: PERSONAL_HOME_PATH,
    title: resolveTabTitle(PERSONAL_HOME_PATH),
    closable: false,
  }]
}

function ensureFixedTabs(tabList: { key: string; title: string; closable: boolean }[]) {
  const fixedTabs = buildFixedTabs()
  const otherTabs = tabList.filter(tab => tab.key !== PERSONAL_HOME_PATH)
  return [...fixedTabs, ...otherTabs]
}

function getRecentRoutes(): string[] {
  try {
    const raw = localStorage.getItem(RECENT_ROUTE_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    if (!Array.isArray(parsed)) {
      return []
    }
    return parsed
      .map(item => normalizeWorkspacePath(String(item || '')))
      .filter(item => item && item !== PERSONAL_HOME_PATH)
  } catch (error) {
    console.error('[MainLayout] Failed to parse recent routes:', error)
    return []
  }
}

function saveRecentRoutes(routes: string[]) {
  try {
    localStorage.setItem(RECENT_ROUTE_STORAGE_KEY, JSON.stringify(routes.slice(0, MAX_RECENT_ROUTE_COUNT)))
  } catch (error) {
    console.error('[MainLayout] Failed to save recent routes:', error)
  }
}

function updateRecentRoutes(path: string) {
  const normalizedPath = normalizeWorkspacePath(path)
  if (!normalizedPath.startsWith('/workspace') || normalizedPath === PERSONAL_HOME_PATH) {
    return
  }
  const nextRoutes = [normalizedPath, ...getRecentRoutes().filter(item => item !== normalizedPath)]
  saveRecentRoutes(nextRoutes)
}

function removeRecentRoute(path: string) {
  const normalizedPath = normalizeWorkspacePath(path)
  saveRecentRoutes(getRecentRoutes().filter(item => item !== normalizedPath))
}

function resolveNextTabKey(closedKey?: string) {
  const availableTabs = new Set(ensureFixedTabs(tabs.value).map(tab => tab.key))
  const recentKey = getRecentRoutes().find(item => item !== closedKey && availableTabs.has(item))
  return recentKey || PERSONAL_HOME_PATH
}

function removeTabsByKeys(keys: string[]) {
  if (keys.length === 0) {
    return
  }
  keys.forEach(removeRecentRoute)
  tabs.value = ensureFixedTabs(tabs.value.filter(tab => !keys.includes(tab.key)))
}

watch(
  resolvedMode,
  mode => {
    document.documentElement.setAttribute('data-theme', mode)
  },
  { immediate: true },
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

/**
 * 将后端/路由返回的标题转换为当前语言的显示文案。
 * <p>
 * 兼容两种数据来源：
 * <ul>
 *   <li>后端已按语言解析后的“直出文本”（直接返回原值）</li>
 *   <li>后端返回的是 i18n key（如 system.xxx / common.xxx），则使用 t() 翻译</li>
 * </ul>
 * </p>
 *
 * @param rawTitle 原始标题（可能为 i18n key 或直出文本）
 * @return 当前语言下的标题文本
 */
function resolveMenuTitle(rawTitle: unknown): string {
  const title = String(rawTitle ?? '')
  if (!title) {
    return ''
  }
  if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
    return t(title)
  }
  return title
}

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
        const title = resolveMenuTitle((c.meta && c.meta.title) || c.name || childPath)
        const fullPath = `/workspace/${code}/${childPath}`.replace(/\/+/g, '/')
        items.push({ title, fullPath })
      }
    }
    result.push({ code, name, items })
  }
  return result
})

const shouldShowSidebar = computed(() => {
  const currentPath = normalizeWorkspacePath(route.fullPath)
  if (layoutConfig.value.layoutMode === 'top') {
    return false
  }
  if (currentPath === PERSONAL_HOME_PATH) {
    return false
  }
  if (layoutConfig.value.layoutMode === 'mix' || layoutConfig.value.layoutMode === 'vertical-mix') {
    return sidebarMenus.value.length > 0
  }
  return true
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
        const title = resolveMenuTitle((child.meta && child.meta.title) || child.name || childPath)
        const icon = (child.meta && child.meta.icon) || ''
        const type = (child.meta && child.meta.type) || 'menu'
        const menuLevel = (child.meta && child.meta.menuLevel) || 1
        
        // 构建菜单项，保留children结构以支持多级菜单
        const menuItem: any = {
          key: fullPath,
          title,
          icon,
          path: fullPath,
          moduleCode: activeModuleCode.value,
          type,
          menuLevel,
          children: []
        }
        
        // 如果有子菜单，递归构建children
        if (child.children && Array.isArray(child.children)) {
          menuItem.children = child.children.map((grandchild: any) => {
            const grandchildPath = String(grandchild.path || '')
            // 构建完整路径：/workspace/{moduleCode}/{childPath}/{grandchildPath}
            // 注意：如果grandchild.path已经是完整路径，需要特殊处理
            const grandchildFullPath = grandchildPath.startsWith('/') 
              ? grandchildPath 
              : `/workspace/${activeModuleCode.value}/${childPath}/${grandchildPath}`.replace(/\/+/g, '/')
            const grandchildTitle = resolveMenuTitle((grandchild.meta && grandchild.meta.title) || grandchild.name || grandchildPath)
            const grandchildIcon = (grandchild.meta && grandchild.meta.icon) || ''
            const grandchildType = (grandchild.meta && grandchild.meta.type) || 'menu'
            const grandchildMenuLevel = (grandchild.meta && grandchild.meta.menuLevel) || 2
            
            return {
              key: grandchildFullPath,
              title: grandchildTitle,
              icon: grandchildIcon,
              path: grandchildFullPath,
              moduleCode: activeModuleCode.value,
              type: grandchildType,
              menuLevel: grandchildMenuLevel,
              parentKey: fullPath,
              children: []
            }
          })
        }
        
        result.push(menuItem)
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
          const title = resolveMenuTitle((child.meta && child.meta.title) || child.name || childPath)
          const icon = (child.meta && child.meta.icon) || ''
          const type = (child.meta && child.meta.type) || 'menu'
          const menuLevel = (child.meta && child.meta.menuLevel) || 1
          
          // 构建菜单项，保留children结构以支持多级菜单
          const menuItem: any = {
            key: fullPath,
            title,
            icon,
            path: fullPath,
            moduleCode,
            type,
            menuLevel,
            children: []
          }
          
          // 如果有子菜单，递归构建children
          if (child.children && Array.isArray(child.children)) {
            menuItem.children = child.children.map((grandchild: any) => {
              const grandchildPath = String(grandchild.path || '')
              // 构建完整路径：/workspace/{moduleCode}/{childPath}/{grandchildPath}
              // 注意：如果grandchild.path已经是完整路径，需要特殊处理
              const grandchildFullPath = grandchildPath.startsWith('/') 
                ? grandchildPath 
                : `/workspace/${moduleCode}/${childPath}/${grandchildPath}`.replace(/\/+/g, '/')
              const grandchildTitle = resolveMenuTitle((grandchild.meta && grandchild.meta.title) || grandchild.name || grandchildPath)
              const grandchildIcon = (grandchild.meta && grandchild.meta.icon) || ''
              const grandchildType = (grandchild.meta && grandchild.meta.type) || 'menu'
              const grandchildMenuLevel = (grandchild.meta && grandchild.meta.menuLevel) || 2
              
              return {
                key: grandchildFullPath,
                title: grandchildTitle,
                icon: grandchildIcon,
                path: grandchildFullPath,
                moduleCode,
                type: grandchildType,
                menuLevel: grandchildMenuLevel,
                parentKey: fullPath,
                children: []
              }
            })
          }
          
          result.push(menuItem)
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
      // 非 / 开头的相对路径，同样补充 /api/
      avatar = `/api/${avatar}`
    }
  }
  
  return {
    account: info.account,
    name: info.username || info.account,
    avatar
  }
})

// 监听语言变化，更新标签标题
watch(
  () => locale.value,
  (newLocale) => {
    if (newLocale) {
      updateAllTabTitles()
    }
  }
)

/**
 * 解析标签标题（各模块工作台 dashboard 带模块名前缀，避免多个「首页」无法区分）
 *
 * @param tabKey 标签路由 key，无 query
 * @returns 展示标题
 */
function resolveTabTitle(tabKey: string): string {
  const clean = tabKey.split('?')[0]
  const m = clean.match(/^\/workspace\/([^/]+)\/dashboard$/)
  if (m) {
    return buildModuleDashboardTitle(m[1])
  }
  return buildTitleFromRoute(clean)
}

/**
 * 各模块「工作台」页签标题：模块名 · 路由标题，避免与系统模块工作台重复显示为两个「首页」。
 *
 * @param moduleCode 模块编码，如 sys、approval
 * @returns 展示用标题
 */
function buildModuleDashboardTitle(moduleCode: string): string {
  const base = buildTitleFromRoute(`/workspace/${moduleCode}/dashboard`)
  const mod = (Array.isArray(dynamicModules.value) ? dynamicModules.value : []).find(
    (x: any) => String(x.code || '') === moduleCode
  )
  if (mod && mod.name) {
    return `${mod.name} · ${base}`
  }
  return base
}

/**
 * 更新所有标签的标题
 */
function updateAllTabTitles() {
  tabs.value = ensureFixedTabs(tabs.value.map(tab => ({
    ...tab,
    title: resolveTabTitle(tab.key),
    closable: tab.key !== PERSONAL_HOME_PATH,
  })))
}

watch(
  () => route.fullPath,
  (path) => {
    const cleanPath = normalizeWorkspacePath(path)
    selectedKeys.value = [cleanPath]
    
    // 优先从路由 meta 中获取模块代码
    let moduleCode = ''
    if (route.meta && route.meta.module) {
      moduleCode = String(route.meta.module)
    } else {
      // 降级策略：从 URL 路径中解析
      const parts = cleanPath.split('/').filter(Boolean)
      if (parts.length >= 2 && parts[0] === 'workspace') {
        const candidate = parts[1]
        const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []
        if (modules.some((item: any) => String(item.code || '') === candidate)) {
          moduleCode = candidate
        }
      }
    }

    activeModuleCode.value = cleanPath === PERSONAL_HOME_PATH ? '' : moduleCode
    openKeys.value = activeModuleCode.value ? [activeModuleCode.value] : []

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
    fontSize: normalizeFontSize(cfg.fontSize),
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
  if (!menuKey) return
  if (menuKey !== route.fullPath) {
    router.push(menuKey).catch(() => {})
  }
}

function refreshPage() {
  router.replace({ path: '/redirect', query: { to: route.fullPath, t: Date.now() } }).catch(() => {})
}

/**
 * 打开消息通知抽屉
 * <p>
 * 加载当前用户收到的消息列表，并打开抽屉显示
 * </p>
 */
async function openMessageDrawer() {
  messageDrawerOpen.value = true
  await loadMessages()
}

function handleOpenMessageDrawerEvent() {
  openMessageDrawer()
}

function handleOpenGlobalSearchEvent() {
  globalSearchVisible.value = true
}

/**
 * 加载消息列表
 */
async function loadMessages() {
  messageLoading.value = true
  try {
    const list = await listUnreadMessages(20)
    messageList.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载消息列表失败:', error)
  } finally {
    messageLoading.value = false
  }
}

function onModuleClick(moduleCode: string) {
  if (!moduleCode) return
  activeModuleCode.value = moduleCode
  // 切换模块时，跳转到该模块的第一个菜单项
  const mod = moduleMenus.value.find(m => m.code === moduleCode)
  if (mod && mod.items.length > 0) {
    const firstItem = mod.items[0]
    if (firstItem.fullPath !== route.fullPath) {
      router.push(firstItem.fullPath).catch(() => {})
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
  // 保存原始语言设置，以便失败时恢复
  const originalLocale = currentLocale.value
  
  try {
    console.log('[MainLayout] 语言切换开始:', val)
    
    // 1. 调用setLocale函数更新语言设置
    setLocale(val as LocaleCode)
    
    // 2. 更新本地状态
    currentLocale.value = val
    appStore.setLocale(val as LocaleCode)
    
    // 3. 调用后端API更新语言设置
    await changeLanguage({ lang: val })
    
    // 4. 重新获取菜单数据（关键！后端返回的是翻译后的文本，需要重新获取）
    const account = sessionStorage.getItem('account')
    const tenantId = sessionStorage.getItem('tenantId')
    
    if (account && tenantId) {
      try {
        console.log('[MainLayout] 重新获取菜单数据...')
        const routes = await getRoutes({ account, tenantId })
        if (routes) {
          // 重新注入动态路由
          await injectDynamicRoutes(routes)
          // 更新所有标签标题
          updateAllTabTitles()
          console.log('[MainLayout] 菜单数据更新成功')
        }
      } catch (menuError) {
        console.error('[MainLayout] 重新获取菜单数据失败:', menuError)
      }
    }
    
    console.log('[MainLayout] 语言切换成功:', val)
    
    // 5. 语言切换成功提示
    message.success(t('common.success'))
    
    // 注意：FxDynamicTable 组件会通过 watch(locale) 自动重新加载配置
  } catch (e) {
    console.error('[MainLayout] 语言切换失败:', e)
    // 语言切换失败时，恢复到原来的语言设置
    setLocale(originalLocale as LocaleCode)
    currentLocale.value = originalLocale
    appStore.setLocale(originalLocale as LocaleCode)
    message.error(t('common.failed'))
  }
}

function onUserMenuClick(key: string) {
  if (!key) return
  
  if (key === 'logout') {
    // 调用userStore的logout方法，会自动调用后端登出接口
    userStore.logout()
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

  if (key === 'messageSend') {
    messageSendOpen.value = true
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
  const pathWithoutQuery = normalizeWorkspacePath(path)
  
  // 仍然使用 activeTabKey 来高亮当前 tab，但 key 本身存储的是无参数路径
  // 这样无论 query 怎么变，tab 都是同一个
  // activeTabKey.value = pathWithoutQuery 
  // 注意：Antdv Tabs 的 activeKey 必须匹配 TabPane 的 key
  // 如果我们希望 tab 保持激活状态，我们需要让 activeTabKey 也指向无参数路径
  activeTabKey.value = pathWithoutQuery

  let nextTabs = ensureFixedTabs(
    tabs.value.map(tab => ({
      ...tab,
      title: resolveTabTitle(tab.key),
      closable: tab.key !== PERSONAL_HOME_PATH,
    })),
  )
  if (pathWithoutQuery !== PERSONAL_HOME_PATH) {
    if (!nextTabs.some(tab => tab.key === pathWithoutQuery)) {
      nextTabs.push({
        key: pathWithoutQuery,
        title: resolveTabTitle(pathWithoutQuery),
        closable: true,
      })
    }

    const maxTabs = Math.max(layoutConfig.value.tabBarMax || 10, 1)
    while (nextTabs.length > maxTabs) {
      const removeIndex = nextTabs.findIndex(tab => tab.closable && tab.key !== pathWithoutQuery)
      if (removeIndex === -1) {
        break
      }
      removeRecentRoute(nextTabs[removeIndex].key)
      nextTabs.splice(removeIndex, 1)
    }
  }

  tabs.value = ensureFixedTabs(nextTabs)
  updateRecentRoutes(pathWithoutQuery)
}

function buildTitleFromRoute(path: string): string {
  // 确保传入的 path 不包含 query
  const pathWithoutQuery = path.split('?')[0]
  
  // 1. 尝试直接从路由表中查找（支持静态路由和动态路由）
  const resolved = router.resolve(pathWithoutQuery)
  const matchedRouteWithTitle = [...resolved.matched].reverse().find(item => item.meta && item.meta.title)
  if (matchedRouteWithTitle?.meta?.title) {
    const title = matchedRouteWithTitle.meta.title as string
    if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
      return t(title)
    }
    return title
  }

  const allRoutes = router.getRoutes()
  const match = allRoutes.find(r => r.path === pathWithoutQuery)
  if (match && match.meta && match.meta.title) {
    const title = match.meta.title as string
    // 如果title是国际化key，使用t函数翻译
    if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
      return t(title)
    }
    return title
  }

  // 2. 如果没找到，尝试通过模块结构查找（降级策略）
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  const clean = pathWithoutQuery.replace(/^\/workspace\//, '')
  const parts = clean.split('/').filter(Boolean)
  if (parts.length < 1) {
    return t('common.home')
  }
  const moduleCode = parts[0]
  const childPath = parts[1]
  const topRoute = routes.find((r: any) => r.path === moduleCode || (r.meta && r.meta.module === moduleCode))
  if (topRoute && Array.isArray(topRoute.children) && childPath) {
    const child = topRoute.children.find((c: any) => String(c.path || '') === childPath)
    if (child && child.meta && child.meta.title) {
      const title = child.meta.title
      // 如果title是国际化key，使用t函数翻译
      if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
        return t(title)
      }
      return title
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
  if (!key || key === PERSONAL_HOME_PATH) return
  const idx = tabs.value.findIndex(t => t.key === key)
  if (idx === -1) return
  const isActive = activeTabKey.value === key
  removeTabsByKeys([key])
  if (!isActive) return
  router.push(resolveNextTabKey(key)).catch(() => {})
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
    const removedKeys = tabs.value
      .filter(t => t.key !== key && t.key !== PERSONAL_HOME_PATH)
      .map(t => t.key)
    removeTabsByKeys(removedKeys)
    if (key !== route.fullPath) {
      router.push(key).catch(() => {})
    }
  } else if (action === 'left' && key) {
    const idx = tabs.value.findIndex(t => t.key === key)
    if (idx > 0) {
      const removedKeys = tabs.value.slice(0, idx).filter(t => t.closable).map(t => t.key)
      removeTabsByKeys(removedKeys)
    }
  } else if (action === 'right' && key) {
    const idx = tabs.value.findIndex(t => t.key === key)
    if (idx !== -1 && idx < tabs.value.length - 1) {
      const removedKeys = tabs.value.slice(idx + 1).filter(t => t.closable).map(t => t.key)
      removeTabsByKeys(removedKeys)
    }
  } else if (action === 'all') {
    removeTabsByKeys(tabs.value.filter(t => t.closable).map(t => t.key))
    router.push(PERSONAL_HOME_PATH).catch(() => {})
  }

  if (!tabs.value.some(t => t.key === activeTabKey.value)) {
    router.push(resolveNextTabKey()).catch(() => {})
  }
}

function openMessageNotification(m: SysMessageVO) {
  const key = `sys-msg-${m.id}`
  notification.open({
    key,
    message: m.title,
    description: m.content || '',
    duration: 6,
    onClick: async () => {
      try {
        await markMessageRead(m.id)
      } catch (_) {}
      notification.close(key)
      if (m.linkUrl) {
        router.push(m.linkUrl).catch(() => {})
      }
    },
  })
}

async function handleMessageSend() {
  if (!messageSendForm.value.receiverUserId) {
    message.warning('请选择接收用户')
    return
  }
  if (!messageSendForm.value.title) {
    message.warning('请输入消息标题')
    return
  }
  if (!messageSendForm.value.content) {
    message.warning('请输入消息内容')
    return
  }

  try {
    const newId = await sendMessage({
      receiverTenantId: Number(sessionStorage.getItem('tenantId')),
      receiverUserId: Number(messageSendForm.value.receiverUserId),
      scope: 'INTERNAL',
      title: messageSendForm.value.title,
      content: messageSendForm.value.content,
      linkUrl: messageSendForm.value.linkUrl,
    } as any)
    message.success('发送成功')
    // 发给本人时立即弹出 Notification（SSE 可能因网关缓冲未即时到达；发给他人仅对方客户端展示）
    const selfId = userStore.userInfo?.id
    if (newId != null && selfId != null && Number(messageSendForm.value.receiverUserId) === selfId) {
      const selfMessage = {
        id: newId as number,
        title: messageSendForm.value.title,
        content: messageSendForm.value.content || '',
        linkUrl: messageSendForm.value.linkUrl,
      } as SysMessageVO
      openMessageNotification(selfMessage)
      window.dispatchEvent(new CustomEvent('fx:message-received', { detail: selfMessage }))
    }
    messageSendOpen.value = false
    // 重置表单
    messageSendForm.value.title = ''
    messageSendForm.value.content = ''
    messageSendForm.value.linkUrl = ''
    messageSendForm.value.receiverUserId = undefined
    selectedUserName.value = ''
    selectedUserIds.value = []
  } catch (_) {}
}

/**
 * 打开用户选择弹窗
 */
async function openUserSelectModal() {
  userSelectOpen.value = true
  await searchUsers()
}

/**
 * 搜索用户
 */
async function searchUsers() {
  userSelectLoading.value = true
  try {
    const tenantId = sessionStorage.getItem('tenantId')
    const res = await getUserList({
      tenantId,
      pageNum: 1,
      pageSize: 100,
      username: userSearchKeyword.value || undefined
    })
    userSelectList.value = res.records || []
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userSelectLoading.value = false
  }
}

/**
 * 用户选择变化
 */
function onUserSelectChange(keys: string[]) {
  selectedUserIds.value = keys
}

/**
 * 确认用户选择
 */
function confirmUserSelect() {
  if (selectedUserIds.value.length === 0) {
    message.warning('请选择用户')
    return
  }
  const selectedUser = userSelectList.value.find(u => String(u.id) === selectedUserIds.value[0])
  if (selectedUser) {
    messageSendForm.value.receiverUserId = selectedUser.id
    selectedUserName.value = `${selectedUser.username} (${selectedUser.account})`
  }
  userSelectOpen.value = false
}

const { connect: connectMessageSse, close: closeMessageSse } = useSse<SysMessageVO>({
  url: '/api/sys/message/stream',
  onEvent: (name, data) => {
    if (name !== 'message') return
    if (!data || !(data as any).id) return
    openMessageNotification(data as any)
    window.dispatchEvent(new CustomEvent('fx:message-received', { detail: data }))
  },
})

onMounted(async () => {
  if (typeof window !== 'undefined') {
    window.addEventListener('scroll', handleScroll)
    window.addEventListener('fx:open-message-drawer', handleOpenMessageDrawerEvent)
    window.addEventListener('fx:open-global-search', handleOpenGlobalSearchEvent)
  }
  loadLayout()
  updateTabsByRoute(route.fullPath)
  
  try {
    const config = await getSystemBasicConfig()
    if (config) {
      systemConfig.value = { ...config }
    }
  } catch (_) {}

  try {
    const unread = await listUnreadMessages(10)
    if (Array.isArray(unread)) {
      for (const m of unread) {
        openMessageNotification(m as any)
      }
    }
  } catch (_) {}

  connectMessageSse()
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    window.removeEventListener('scroll', handleScroll)
    window.removeEventListener('fx:open-message-drawer', handleOpenMessageDrawerEvent)
    window.removeEventListener('fx:open-global-search', handleOpenGlobalSearchEvent)
  }
  closeMessageSse()
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
/* 关键修改开始 */
.fx-content {
  flex: 1;
  overflow: hidden; /* 改为 hidden，防止整个内容区滚动 */
  padding: 0;       /* 移除默认 padding（如果有的话） */
}
.fx-content-inner {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;            /* 统一页面内边距（可根据需要调整） */
  box-sizing: border-box;   /* 让 padding 包含在 height 100% 内 */
  position: relative;       /* 为 watermark 绝对定位提供参考 */
}

/* ==================== 卡片选择器样式（Vben5 风格）==================== */

.fx-setting-section {
  margin-bottom: 20px;
}

.fx-setting-label {
  display: block;
  font-size: 13px;
  font-weight: 500;
  margin-bottom: 12px;
  color: var(--fx-text-primary, #1f2937);
}

.fx-setting-title--sub {
  margin-top: 8px;
  margin-bottom: 14px;
}

.fx-card-grid {
  display: grid;
  gap: 10px;
  
  &--mode {
    grid-template-columns: repeat(3, 1fr);
  }
  
  &--color {
    grid-template-columns: repeat(3, 1fr);
  }
  
  &--layout {
    grid-template-columns: repeat(2, 1fr);
  }
}

/* 主题模式卡片 */
.fx-setting-row--slider {
  align-items: center;
}

.fx-setting-slider {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;

  :deep(.ant-slider) {
    flex: 1;
    margin: 0;
  }
}

.fx-setting-slider__value {
  min-width: 48px;
  color: var(--fx-text-secondary, #6b7280);
  font-size: 12px;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.fx-mode-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px 8px;
  border: 2px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 12px;
  background: var(--fx-bg-container, #ffffff);
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;

  &:hover {
    border-color: var(--fx-primary, #1677ff);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
  }

  &--active {
    border-color: var(--fx-primary, #1677ff);
    background: var(--fx-primary-bg, rgba(22, 119, 255, 0.06));

    .fx-mode-card__icon {
      color: var(--fx-primary, #1677ff);
    }

    .fx-mode-card__label {
      color: var(--fx-primary, #1677ff);
      font-weight: 600;
    }
  }

  &__icon {
    font-size: 22px;
    color: var(--fx-text-secondary, #6b7280);
    transition: color 0.25s ease;
  }

  &__label {
    font-size: 12px;
    color: var(--fx-text-secondary, #6b7280);
    transition: all 0.25s ease;
  }
}

/* 主题颜色卡片 */
.fx-color-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 8px;
  border: 2px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 12px;
  background: var(--fx-bg-container, #ffffff);
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  &--active {
    border-color: var(--fx-text-primary, #1f2937);

    .fx-color-card__label {
      font-weight: 600;
      color: var(--fx-text-primary, #1f2937);
    }
  }

  &__swatch {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    border: 2px solid rgba(0, 0, 0, 0.08);
    transition: transform 0.25s ease, box-shadow 0.25s ease;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }

  &:hover &__swatch {
    transform: scale(1.1);
  }

  &--active &__swatch {
    transform: scale(1.05);
    box-shadow: 0 0 0 3px rgba(22, 119, 255, 0.2), 0 2px 8px rgba(0, 0, 0, 0.18);
  }

  &__label {
    font-size: 11px;
    color: var(--fx-text-tertiary, #6b7280);
    transition: all 0.25s ease;
  }
}

/* 布局模式卡片 */
.fx-layout-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 8px;
  border: 2px solid var(--fx-border-color, rgba(148, 163, 184, 0.2));
  border-radius: 12px;
  background: var(--fx-bg-container, #ffffff);
  cursor: pointer;
  transition: all 0.25s ease;
  outline: none;

  &:hover {
    border-color: var(--fx-primary, #1677ff);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 119, 255, 0.15);
  }

  &--active {
    border-color: var(--fx-primary, #1677ff);
    background: var(--fx-primary-bg, rgba(22, 119, 255, 0.06));

    .fx-layout-card__label {
      color: var(--fx-primary, #1677ff);
      font-weight: 600;
    }

    .fx-layout-card__preview {
      color: var(--fx-primary, #1677ff);
    }
  }

  &__preview {
    width: 72px;
    height: 48px;
    color: var(--fx-text-tertiary, #9ca3af);
    transition: color 0.25s ease;
    
    :deep(svg) {
      width: 100%;
      height: 100%;
    }
  }

  &__label {
    font-size: 12px;
    color: var(--fx-text-secondary, #6b7280);
    transition: all 0.25s ease;
  }
}

/* 消息通知抽屉 */
.fx-message-drawer {
  :deep(.ant-drawer-body) {
    padding: 16px;
  }
}

.fx-message-empty {
  padding: 40px 0;
  text-align: center;
}

.fx-message-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.fx-message-item {
  padding: 12px;
  border-radius: 8px;
  background: var(--fx-bg-elevated, #f5f5f5);
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: var(--fx-primary-bg, rgba(22, 119, 255, 0.08));
  }
}

.fx-message-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--fx-text-primary, #1f2937);
  margin-bottom: 4px;
}

.fx-message-content {
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.fx-message-time {
  font-size: 12px;
  color: var(--fx-text-tertiary, #9ca3af);
  margin-top: 6px;
}
</style>
