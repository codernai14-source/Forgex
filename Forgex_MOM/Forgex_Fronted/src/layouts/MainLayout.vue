<template>
  <a-config-provider :theme="antdTheme" :locale="antdLocale">
    <a-layout 
      ref="layoutRootRef"
      class="fx-main-layout" 
      :style="rootStyle"
      :data-font-size="layoutConfig.fontSize"
      :data-table-row-density="layoutConfig.tableRowDensity"
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
      :tenant-options="tenantOptions"
      :current-tenant-id="currentTenantId"
      :tenant-loading="tenantLoading"
      :switching-tenant-id="switchingTenantId"
      :permission-refreshing="permissionRefreshing"
      @module-click="onModuleClick"
      @search-click="globalSearchVisible = true"
      @locale-change="onLocaleChange"
      @refresh="refreshPage"
      @message-click="openMessageDrawer"
      @user-menu-click="onUserMenuClick"
      @tenant-change="onTenantChange"
      @settings-click="settingOpen = true"
    />

    <a-layout class="fx-main-content-wrapper">
      <!-- 使用新的侧边栏组件 -->
        <AppSidebar
          v-if="shouldShowSidebar"
          :menus="sidebarMenus"
          :modules="moduleList"
          :active-key="normalizeWorkspacePath(route.fullPath)"
          :active-module-code="activeModuleCode"
          :layout-mode="layoutConfig.layoutMode"
          :collapsed="siderCollapsed"
          :double-column="sidebarDoubleColumn"
          :favorite-paths="menuFavoritePathList"
          :favorite-loading-path="menuFavoriteTogglingPath"
          @module-click="onModuleClick"
          @menu-click="onMenuClick"
          @favorite-toggle="onSidebarFavoriteToggle"
          @collapse-change="onCollapse"
        />

      <a-layout class="fx-content-layout">
        <!-- 使用新的 AppTabBar 组件 -->
        <AppTabBar
          v-if="layoutConfig.tabBarEnabled"
          :tabs="tabs"
          :active-key="activeTabKey"
          :draggable="layoutConfig.tabBarDraggable"
          :favorite-paths="menuFavoritePathList"
          @tab-click="onTabClick"
          @tab-close="onTabClose"
          @tab-pin="onTabPin"
          @tab-favorite="onTabFavorite"
          @tab-drag="onTabDrag"
          @tab-refresh="onTabRefresh"
          @tabs-close="onTabsClose"
        />

        <a-layout-content class="fx-content" @contextmenu="onContentContextMenu">
         <div class="fx-content-inner">
            <div v-if="layoutConfig.watermarkEnabled" class="fx-watermark-container">
              <div class="fx-watermark" v-for="i in 12" :key="i">
                {{ layoutConfig.watermarkText }}
              </div>
            </div>
            <div class="fx-guide-content">
              <router-view v-slot="{ Component, route: currentRoute }">
                <transition
                  v-if="layoutConfig.animateEnabled"
                  :name="pageTransitionName"
                  mode="out-in"
                >
                  <div v-if="Component" class="fx-page-wrapper" :key="currentRoute.fullPath">
                    <component :is="Component" />
                  </div>
                </transition>
                <div v-else-if="Component" class="fx-page-wrapper" :key="currentRoute.fullPath">
                  <component :is="Component" />
                </div>
              </router-view>
            </div>
          </div>
          <Transition name="fx-back-top-fade">
            <a-button
              v-if="showBackTop"
              class="fx-back-top"
              shape="circle"
              type="primary"
              :title="t('common.backTop')"
              @click="scrollCurrentPageToTop"
            >
              <template #icon>
                <ArrowUpOutlined />
              </template>
            </a-button>
          </Transition>
        </a-layout-content>
      </a-layout>
    </a-layout>

    <div v-if="layoutConfig.footerCopyrightEnabled" class="fx-footer">
      {{ systemConfig.copyright }}
    </div>

    <a-dropdown
      v-model:open="contentFavoriteMenuVisible"
      :trigger="[]"
      :get-popup-container="getSettingDrawerContainer"
    >
      <div
        :style="{
          position: 'fixed',
          left: contentFavoriteMenuPosition.x + 'px',
          top: contentFavoriteMenuPosition.y + 'px',
          width: '1px',
          height: '1px'
        }"
      />
      <template #overlay>
        <a-menu @click="onContentFavoriteMenuClick">
          <a-menu-item key="favorite" :disabled="!currentFavoritePath || !!menuFavoriteTogglingPath">
            <StarFilled v-if="isCurrentPathFavorite" />
            <StarOutlined v-else />
            <span>{{ isCurrentPathFavorite ? '取消收藏本页' : '收藏本页' }}</span>
          </a-menu-item>
        </a-menu>
      </template>
    </a-dropdown>

    <!-- 使用新的 GlobalSearch 组件 -->
    <GlobalSearch
      :visible="globalSearchVisible"
      :menus="searchMenus"
      @update:visible="globalSearchVisible = $event"
      @select="onGlobalSearchSelect"
    />

    <SystemNoticePopup />

    <a-drawer
      v-model:open="horizontalMenuDrawerOpen"
      placement="top"
      :height="360"
      class="fx-horizontal-menu-drawer"
      :get-container="getSettingDrawerContainer"
      :root-style="settingDrawerRootStyle"
    >
      <template #title>
        <div class="fx-horizontal-menu-drawer__header">
          <div class="fx-horizontal-menu-drawer__title">{{ activeModuleName }}</div>
          <div class="fx-horizontal-menu-drawer__subtitle">{{ t('layout.horizontalModuleMenuHint') }}</div>
        </div>
      </template>
      <div class="fx-horizontal-module-panel__grid" data-guide-id="fx-horizontal-module-panel">
        <button
          v-for="item in horizontalModuleMenus"
          :key="item.key"
          type="button"
          class="fx-horizontal-menu-card"
          :class="{ 'fx-horizontal-menu-card--active': isHorizontalMenuActive(item) }"
          @click="onHorizontalMenuClick(item)"
        >
          <span class="fx-horizontal-menu-card__icon">
            <FxIcon v-if="item.icon" :name="item.icon" />
            <component v-else :is="resolveMenuIcon(item)" />
          </span>
          <span class="fx-horizontal-menu-card__body">
            <span class="fx-horizontal-menu-card__title" :title="item.title">{{ item.title }}</span>
            <span
              v-if="item.children?.length"
              class="fx-horizontal-menu-card__meta"
            >
              {{ t('layout.horizontalModuleMenuCount', { count: countNavigableMenus(item.children) }) }}
            </span>
          </span>
          <span
            v-if="item.children?.length"
            class="fx-horizontal-menu-card__children"
            @click.stop
          >
            <button
              v-for="child in getHorizontalMenuChildren(item)"
              :key="child.key"
              type="button"
              class="fx-horizontal-menu-card__child"
              :class="{ 'fx-horizontal-menu-card__child--active': isHorizontalMenuActive(child) }"
              @click="onHorizontalMenuClick(child)"
            >
              {{ child.title }}
            </button>
          </span>
        </button>
      </div>
    </a-drawer>

    <FxGuideTour
      ref="systemGuideTourRef"
      :guide-code="currentSystemGuideCode"
      :version="currentSystemGuideVersion"
      :steps="systemGuideSteps"
      :auto-start="systemGuideAutoStart"
      :start-key="systemGuideStartKey"
      :skip-text="t('common.guide.skip')"
      :show-skip-all="true"
      @open="handleSystemGuideOpen"
      @close="handleSystemGuideClose"
      @finish="handleSystemGuideFinish"
      @skip="handleSystemGuideSkip"
      @skip-all="handleSystemGuideSkipAll"
    />

    <!-- 消息通知抽屉 -->
    <a-drawer
      v-model:open="messageDrawerOpen"
      :title="t('layout.messageCenter.title')"
      placement="right"
      width="520"
      class="fx-message-drawer"
    >
      <a-spin :spinning="messageLoading">
        <a-tabs v-model:activeKey="activeMessageTab" @change="handleMessageTabChange">
          <a-tab-pane key="SYSTEM">
            <template #tab>
              <span>{{ t('layout.messageCenter.system') }}</span>
              <a-badge :count="messageCounts.SYSTEM" :number-style="{ backgroundColor: '#1677ff' }" />
            </template>
          </a-tab-pane>
          <a-tab-pane key="MESSAGE">
            <template #tab>
              <span>{{ t('layout.messageCenter.message') }}</span>
              <a-badge :count="messageCounts.MESSAGE" :number-style="{ backgroundColor: '#52c41a' }" />
            </template>
          </a-tab-pane>
        </a-tabs>

        <div v-if="currentDrawerList.length === 0" class="fx-message-empty">
          <a-empty :description="activeMessageTab === 'SYSTEM' ? t('layout.messageCenter.emptySystem') : t('layout.messageCenter.emptyMessage')" />
        </div>
        <div v-else class="fx-message-list">
          <div
            v-for="msg in currentDrawerList"
            :key="msg.id"
            class="fx-message-item"
            @click="activeMessageTab === 'SYSTEM' ? handleNoticeItemClick(msg as SysNotice) : handleMessageItemClick(msg as SysMessageVO)"
          >
            <div class="fx-message-item__header">
              <div class="fx-message-title">{{ msg.title }}</div>
              <a-tag :color="activeMessageTab === 'SYSTEM' ? 'blue' : 'green'">
                {{ activeMessageTab === 'SYSTEM' ? t('layout.messageCenter.system') : t('layout.messageCenter.message') }}
              </a-tag>
            </div>
            <div class="fx-message-content">{{ activeMessageTab === 'SYSTEM' ? ((msg as SysNotice).summary || (msg as SysNotice).contentHtml || '') : (msg as SysMessageVO).content }}</div>
            <div class="fx-message-time">{{ activeMessageTab === 'SYSTEM' ? formatNoticeDisplayTime(msg as SysNotice) : (msg as SysMessageVO).createTime }}</div>
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
                    @click="onThemeModeChange(mode.value)"
                  >
                    <component :is="mode.icon" class="fx-mode-card__icon" />
                    <span class="fx-mode-card__label">{{ mode.label }}</span>
                  </button>
                </div>
              </div>

              <!-- 主题颜色卡片选择器 -->
              <div class="fx-setting-section">
                <div class="fx-setting-title fx-setting-title--sub">{{ t('layout.themePreset') }}</div>
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
                </div>
              </div>

              <!-- 圆角大小滑块 -->
              <div class="fx-setting-row fx-setting-row--slider">
                <span>{{ t('layout.borderRadius') }}</span>
                <div class="fx-setting-slider">
                  <a-slider v-model:value="layoutConfig.borderRadius" :min="0" :max="16" />
                </div>
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
                <a-select v-model:value="layoutConfig.contentWidth" class="fx-setting-control">
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
                <a-select v-model:value="layoutConfig.headerMode" class="fx-setting-control">
                  <a-select-option value="fixed">{{ t('layout.headerModeFixed') }}</a-select-option>
                  <a-select-option value="auto">{{ t('layout.headerModeAuto') }}</a-select-option>
                  <a-select-option value="hide-on-scroll">{{ t('layout.headerModeHideOnScroll') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.headerMenuAlign') }}</span>
                <a-select v-model:value="layoutConfig.headerMenuAlign" class="fx-setting-control">
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
              <div class="fx-setting-row">
                <span class="fx-setting-label-with-help">
                  {{ t('layout.tabBarMax') }}
                  <a-tooltip :title="t('layout.tabBarMaxHint')">
                    <QuestionCircleOutlined class="fx-setting-help-icon" />
                  </a-tooltip>
                </span>
                <a-input-number
                  v-model:value="layoutConfig.tabBarMax"
                  :min="0"
                  :max="200"
                  :precision="0"
                  class="fx-setting-control"
                />
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
                <a-input v-model:value="layoutConfig.watermarkText" class="fx-setting-control" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.formMode') }}</span>
                <a-select v-model:value="appStore.formMode" class="fx-setting-control">
                  <a-select-option value="modal">{{ t('layout.formModeModal') }}</a-select-option>
                  <a-select-option value="drawer">{{ t('layout.formModeDrawer') }}</a-select-option>
                </a-select>
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.tableRowDensity') }}</span>
                <a-segmented
                  v-model:value="layoutConfig.tableRowDensity"
                  class="fx-setting-segmented"
                  :options="tableRowDensityOptions"
                />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.animateEnabled') }}</span>
                <a-switch v-model:checked="layoutConfig.animateEnabled" />
              </div>
              <div class="fx-setting-row">
                <span>{{ t('layout.pageTransition') }}</span>
                <a-select v-model:value="layoutConfig.pageTransition" class="fx-setting-control">
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
      :title="t('layout.messageCenter.sendTitle')"
      :confirm-loading="false"
      @ok="handleMessageSend"
    >
      <a-form layout="vertical">
        <a-form-item :label="t('layout.messageCenter.receiverUser')" required>
          <a-input
            v-model:value="selectedUserName"
            readonly
            :placeholder="t('layout.messageCenter.selectUserPlaceholder')"
            @click="openUserSelectModal"
          >
            <template #suffix>
              <SearchOutlined :style="{ color: 'var(--fx-text-tertiary)' }" />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item :label="t('layout.messageCenter.messageTitle')" required>
          <a-input v-model:value="messageSendForm.title" :placeholder="t('layout.messageCenter.messageTitlePlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('layout.messageCenter.content')" required>
          <a-textarea v-model:value="messageSendForm.content" :rows="4" :placeholder="t('layout.messageCenter.contentPlaceholder')" />
        </a-form-item>

        <a-form-item :label="t('layout.messageCenter.linkUrl')">
          <a-input v-model:value="messageSendForm.linkUrl" :placeholder="t('layout.messageCenter.linkUrlPlaceholder')" />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 用户选择弹窗 -->
    <a-modal
      v-model:open="userSelectOpen"
      :title="t('layout.messageCenter.selectUserTitle')"
      width="600px"
      @ok="confirmUserSelect"
    >
      <a-input-search
        v-model:value="userSearchKeyword"
        :placeholder="t('layout.messageCenter.searchUserPlaceholder')"
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
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRoute, useRouter, type LocationQueryRaw } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message, notification } from 'ant-design-vue'
import enUS from 'ant-design-vue/es/locale/en_US'
import jaJP from 'ant-design-vue/es/locale/ja_JP'
import koKR from 'ant-design-vue/es/locale/ko_KR'
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import zhTW from 'ant-design-vue/es/locale/zh_TW'
import { FAVORITE_MANAGEMENT_PATH, PERSONAL_HOME_PATH, dynamicModules, dynamicRoutes, injectDynamicRoutes, refreshDynamicRoutes } from '../router'
import { getUserLayoutStyle, saveUserLayoutStyle } from '../api/system/userStyle'
import { changeLanguage, chooseTenant, listCurrentTenants, type TenantOption } from '../api/auth/login'
import { getRoutes } from '../api/system/route'
import { TAB_CLOSE_QUERY_KEY } from '../router/approvalRoutePaths'
import { getSystemBasicConfig } from '../api/system/config'
import { getLicenseStatus } from '../api/system/license'
import { setLocale, type LocaleCode } from '../locales'
import { getUnreadMessageCount, listUnreadMessages, markMessageRead, sendMessage, type SysMessageVO } from '../api/system/message'
import { noticeApi, type SysNotice } from '../api/system/notice'
import { reportUserMenuOpen, reportUserMenuVisit } from '../api/system/personalHomepage'
import { getUserList } from '../api/system/user'
import { useSse } from '../hooks/useSse'
import { resolveMenuTitle, resolveModuleDisplayName } from '../utils/menuI18n'

import {
  SearchOutlined,
  HighlightOutlined,
  EyeInvisibleOutlined,
  DesktopOutlined,
  AppstoreOutlined,
  FolderOutlined,
  FileOutlined,
  ArrowUpOutlined,
  QuestionCircleOutlined,
  StarFilled,
  StarOutlined,
} from '@ant-design/icons-vue'

import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'
import AppTabBar from './components/AppTabBar.vue'
import GlobalSearch from './components/GlobalSearch.vue'
import FxGuideTour from '../components/common/FxGuideTour.vue'
import FxIcon from '../components/common/FxIcon.vue'
import SystemNoticePopup from '../components/system/SystemNoticePopup.vue'
// 导入新的主题系统
import { useSystemTheme, resolveThemeMode } from '../composables/useSystemTheme'
import { normalizeFavoritePath, useMenuFavorites } from '../composables/useMenuFavorites'
import { useAntdTheme } from '../theme/antdTheme'
import { lightTokens, darkTokens } from '../theme/tokens'
import { generateCSSVariablesWithCache } from '../theme/cssVariables'
import { normalizeMediaUrl } from '../utils/media'
import { applySiteBranding } from '../utils/siteBranding'
import { useAppStore } from '../stores/app'
import { useGuideStore } from '../stores/guide'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import { resolveSystemPageGuide } from '../guide/systemPageGuides'
import type { SystemBasicConfig } from '../api/system/config'
import type { FxGuideStep } from '../types/guide'

const router = useRouter()
const route = useRoute()
const { t, locale } = useI18n()
const isFallbackRoute = computed(() => route.path.startsWith('/workspace/fallback/'))
const appStore = useAppStore()
const guideStore = useGuideStore()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const permissionRefreshing = ref(false)
let permissionRefreshPromise: Promise<boolean> | null = null
const licenseGraceNoticeKey = 'fx-license-grace-notice'

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

interface MessageSendForm {
  receiverTenantId?: number
  receiverUserId?: number
  scope: 'INTERNAL'
  title: string
  content: string
  linkUrl: string
  bizType: string
}

interface LayoutTab {
  key: string
  path: string
  title: string
  icon?: string
  closable: boolean
  /** 用户固定的标签：不参与自动淘汰，默认不可关闭（需右键取消固定） */
  pinned?: boolean
}

interface ThemeModeOption {
  value: LayoutConfig['themeMode']
  label: string
  icon: unknown
}

interface LayoutModeOption {
  value: LayoutConfig['layoutMode']
  label: string
  preview: string
}

type MessageCategory = 'SYSTEM' | 'MESSAGE'
type SidebarMenuType = 'module' | 'catalog' | 'menu' | 'button'

interface SidebarMenuItem {
  key: string
  title: string
  icon?: string
  path: string
  moduleCode: string
  moduleName?: string
  parentKey?: string
  menuLevel: number
  children?: SidebarMenuItem[]
  type: SidebarMenuType
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
  /** 0 表示不限制打开标签数量，避免误认为「标签消失」仅为溢出菜单可见 */
  tabBarMax: 0,
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
  tableRowDensity: 'normal',
  pageTransition: 'horizontal',
  footerCopyrightEnabled: true
}

const layoutRootRef = ref<HTMLElement | { $el?: unknown } | null>(null)
const layoutConfig = ref<LayoutConfig>({ ...DEFAULT_LAYOUT_CONFIG })
const systemGuideTourRef = ref<InstanceType<typeof FxGuideTour> | null>(null)
const settingOpen = ref(false)
const horizontalMenuDrawerOpen = ref(false)
const messageDrawerOpen = ref(false)
const messageLoading = ref(false)
const activeMessageTab = ref<MessageCategory>('SYSTEM')
const messageCounts = ref<Record<MessageCategory, number>>({
  SYSTEM: 0,
  MESSAGE: 0,
})
const messageLists = ref<Record<MessageCategory, SysMessageVO[]>>({
  MESSAGE: [],
  SYSTEM: [],
})
const systemNoticeList = ref<SysNotice[]>([])
const FONT_SIZE_MIN = 14
const FONT_SIZE_MAX = 28
const FONT_SIZE_DEFAULT = 14
const THEME_REVEAL_DURATION = 520
const settingDrawerRootStyle = {
  position: 'absolute',
} as const
const systemGuideAutoStart = ref(false)
const systemGuideReady = ref(false)
const currentSystemGuideCode = ref('system.main')
const currentSystemGuideVersion = ref('v1')
const currentSystemPageGuideSteps = ref<FxGuideStep[]>([])
const pendingSystemFirstOpenGuide = ref(false)
const systemGuideStartKey = ref(0)

const systemMainGuideSteps = computed<FxGuideStep[]>(() => [
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.welcome'),
    placement: 'center',
    useMask: false,
    category: 'intro',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.header'),
    target: '.fx-guide-header',
    placement: 'bottom',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.sidebar'),
    target: '.fx-guide-sidebar',
    placement: 'right',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.tabbar'),
    target: '.fx-guide-tabbar',
    placement: 'bottom',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.search'),
    target: '.fx-guide-search-trigger',
    placement: 'bottom',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.message'),
    target: '.fx-guide-message-trigger',
    placement: 'bottom',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.settings'),
    target: '.fx-guide-settings-trigger',
    placement: 'bottom',
    category: 'navigation',
  },
  {
    title: t('layout.guide.system.title'),
    description: t('layout.guide.system.steps.content'),
    target: '.fx-guide-content',
    placement: 'top',
    category: 'navigation',
  },
])

const systemGuideSteps = computed<FxGuideStep[]>(() => {
  if (currentSystemGuideCode.value === 'system.main') {
    return systemMainGuideSteps.value
  }
  return currentSystemPageGuideSteps.value
})

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
 * 将设置抽屉挂载到主布局容器内，确保 drawer 内仍能继承 `--fx-*` 主题变量。
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
const themeModeOptions = computed<ThemeModeOption[]>(() => [
  { value: 'light', label: t('layout.themeLight'), icon: HighlightOutlined },
  { value: 'dark', label: t('layout.themeDark'), icon: EyeInvisibleOutlined },
  { value: 'system', label: t('layout.themeSystem'), icon: DesktopOutlined },
])

/**
 * 主题颜色选项配置
 * <p>
 * 提供 7 种预设主题色，每种颜色包含 HEX 值和中文标签
 * 颜色经过精心挑选，确保视觉舒适度和可访问性
 * </p>
 */
const themeColorOptions = computed(() => [
  { value: '#1677ff', label: t('layout.themeColorDawnBlue') },
  { value: '#7c5cff', label: t('layout.themeColorTwilightPurple') },
  { value: '#ec4899', label: t('layout.themeColorRosePink') },
  { value: '#f6c445', label: t('layout.themeColorLemonYellow') },
  { value: '#5b8ff9', label: t('layout.themeColorSkyBlue') },
  { value: '#34d399', label: t('layout.themeColorMintGreen') },
  { value: '#71717a', label: t('layout.themeColorZincGray') },
  { value: '#14b8a6', label: t('layout.themeColorTealGreen') },
  { value: '#1d4ed8', label: t('layout.themeColorRoyalBlue') },
  { value: '#f97316', label: t('layout.themeColorAmberOrange') },
  { value: '#e11d48', label: t('layout.themeColorRoseRed') },
  { value: '#525252', label: t('layout.themeColorNeutralGray') },
  { value: '#475569', label: t('layout.themeColorSlateGray') },
  { value: '#6b7280', label: t('layout.themeColorCoolGray') },
])

const fontSizeSliderValue = computed({
  get: () => parseFontSizeValue(layoutConfig.value.fontSize),
  set: (value: number) => {
    layoutConfig.value.fontSize = `${clamp(Math.round(value), FONT_SIZE_MIN, FONT_SIZE_MAX)}px`
  },
})

const tableRowDensityOptions = computed(() => [
  { label: t('layout.tableRowDensityComfortable'), value: 'comfortable' },
  { label: t('layout.tableRowDensityNormal'), value: 'normal' },
  { label: t('layout.tableRowDensityCompact'), value: 'compact' },
])

/**
 * 布局模式选项配置
 * <p>
 * 提供四种布局模式，每种模式包含预览 SVG 图标和中文标签
 * 预览图使用简化的布局结构示意
 * </p>
 */
const layoutModeOptions = computed<LayoutModeOption[]>(() => [
  {
    value: 'vertical',
    label: t('layout.layoutVertical'),
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
    label: t('layout.layoutVerticalMix'),
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
    label: t('layout.layoutTop'),
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
    label: t('layout.layoutMix'),
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
])
const siderCollapsed = ref(false)
const openKeys = ref<string[]>([])
const selectedKeys = ref<string[]>([])
const activeModuleCode = ref<string>('')
const tabs = ref<LayoutTab[]>([])
const activeTabKey = ref<string>('')
const {
  favoritePathSet: menuFavoritePathSet,
  togglingPath: menuFavoriteTogglingPath,
  loadFavorites: loadMenuFavorites,
  isFavorite: isMenuFavorite,
  toggleFavorite: toggleMenuFavorite,
} = useMenuFavorites()
const contentFavoriteMenuVisible = ref(false)
const contentFavoriteMenuPosition = ref({ x: 0, y: 0 })
/** 用户固定的标签路径列表（与 {@link LayoutTab#pinned} 同步） */
const PINNED_TAB_KEYS_STORAGE_KEY = 'fx-pinned-tab-keys'

const RECENT_ROUTE_STORAGE_KEY = 'fx-recent-routes'
const ROUTE_VISIT_STATS_STORAGE_KEY = 'fx-route-visit-stats'
const MAX_RECENT_ROUTE_COUNT = 20
const MAX_ROUTE_VISIT_STATS_COUNT = 200
const HORIZONTAL_MENU_CHILD_LIMIT = 6
const globalSearchVisible = ref(false)
const currentLocale = ref<string>((localStorage.getItem('fx-locale') as string) || (locale.value as string))
const skipThemeRevealWatcher = ref(false)

function applyThemeVariablesToElement(target: HTMLElement, styleMap: Record<string, unknown>) {
  Object.entries(styleMap).forEach(([key, value]) => {
    if (!key.startsWith('--fx-') || value == null) {
      return
    }
    target.style.setProperty(key, String(value))
  })
}

function syncThemeVariablesToDocument(styleMap: Record<string, unknown>) {
  if (typeof document === 'undefined') {
    return
  }

  applyThemeVariablesToElement(document.documentElement, styleMap)
}

function applyDocumentTheme(mode: 'light' | 'dark') {
  if (typeof document === 'undefined') {
    return
  }

  document.documentElement.setAttribute('data-theme', mode)
  document.documentElement.style.colorScheme = mode
  if (document.body) {
    document.body.setAttribute('data-theme', mode)
    document.body.style.colorScheme = mode
  }
}

function getLayoutElement(): HTMLElement | null {
  const target = layoutRootRef.value
  if (!target) {
    return null
  }
  if (target instanceof HTMLElement) {
    return target
  }
  const el = target.$el
  return el instanceof HTMLElement ? el : null
}

function runThemeRevealTransition(
  previousMode: 'light' | 'dark',
  nextMode: 'light' | 'dark',
  applyNextTheme?: () => void,
) {
  if (typeof document === 'undefined' || typeof window === 'undefined') {
    applyNextTheme?.()
    applyDocumentTheme(nextMode)
    return
  }

  const layoutEl = getLayoutElement()
  if (!layoutEl) {
    applyNextTheme?.()
    applyDocumentTheme(nextMode)
    return
  }

  const nextTokens = nextMode === 'dark' ? darkTokens : lightTokens
  const nextStyleMap = generateCSSVariablesWithCache(nextTokens, {
    ...layoutConfig.value,
    themeMode: nextMode,
  })
  const overlay = document.createElement('div')
  const snapshot = layoutEl.cloneNode(true) as HTMLElement
  overlay.className = 'fx-theme-reveal-overlay'
  overlay.setAttribute('aria-hidden', 'true')
  overlay.dataset.theme = nextMode
  overlay.style.background = nextTokens.colorBgBase
  overlay.style.colorScheme = nextMode
  overlay.style.clipPath = 'circle(0 at 100% 0)'
  overlay.style.webkitClipPath = 'circle(0 at 100% 0)'
  applyThemeVariablesToElement(overlay, nextStyleMap as Record<string, unknown>)
  snapshot.classList.add('fx-theme-reveal-overlay__content')
  snapshot.setAttribute('aria-hidden', 'true')
  applyThemeVariablesToElement(snapshot, nextStyleMap as Record<string, unknown>)
  overlay.appendChild(snapshot)
  document.body.appendChild(overlay)

  const width = window.innerWidth
  const height = window.innerHeight
  const radius = Math.ceil(Math.sqrt(width * width + height * height) * 1.05)
  const cleanup = () => {
    overlay.remove()
  }
  const commitTheme = () => {
    layoutEl.classList.add('fx-theme-reveal-running')
    applyThemeVariablesToElement(document.documentElement, nextStyleMap as Record<string, unknown>)
    applyNextTheme?.()
    applyDocumentTheme(nextMode)
    requestAnimationFrame(() => {
      layoutEl.classList.remove('fx-theme-reveal-running')
      cleanup()
    })
  }

  requestAnimationFrame(() => {
    const supportsClipPath = typeof CSS !== 'undefined'
      && typeof CSS.supports === 'function'
      && CSS.supports('clip-path', 'circle(0 at 100% 0)')
    if (supportsClipPath) {
      overlay.style.transition = [
        `clip-path ${THEME_REVEAL_DURATION}ms cubic-bezier(0.22, 1, 0.36, 1)`,
        `-webkit-clip-path ${THEME_REVEAL_DURATION}ms cubic-bezier(0.22, 1, 0.36, 1)`,
      ].join(', ')
      requestAnimationFrame(() => {
        overlay.style.clipPath = `circle(${radius}px at 100% 0)`
        overlay.style.webkitClipPath = `circle(${radius}px at 100% 0)`
      })
      window.setTimeout(commitTheme, THEME_REVEAL_DURATION + 80)
      return
    }

    overlay.style.transition = 'opacity 220ms ease'
    overlay.style.opacity = '1'
    window.setTimeout(commitTheme, 240)
  })
}

function onThemeModeChange(mode: LayoutConfig['themeMode']) {
  if (layoutConfig.value.themeMode === mode) {
    return
  }

  const previousMode = resolvedMode.value
  const nextMode = resolveThemeMode(mode, systemTheme.value)
  if (previousMode === nextMode) {
    layoutConfig.value.themeMode = mode
    return
  }

  skipThemeRevealWatcher.value = true
  runThemeRevealTransition(previousMode, nextMode, () => {
    layoutConfig.value.themeMode = mode
  })
}
const currentAccount = ref<string>(sessionStorage.getItem('account') || '')
const currentTenantId = ref<string>(sessionStorage.getItem('tenantId') || '')
const tenantOptions = ref<TenantOption[]>([])
const tenantLoading = ref(false)
const switchingTenantId = ref('')
const messageSendOpen = ref(false)
const messageSendForm = ref<MessageSendForm>({
  receiverTenantId: Number(sessionStorage.getItem('tenantId') || '') || undefined,
  receiverUserId: undefined,
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
const selectedUserAccount = ref('')
const userSearchKeyword = ref('')

const currentMessageList = computed(() => messageLists.value.MESSAGE || [])
const currentDrawerList = computed<Array<SysMessageVO | SysNotice>>(() => (
  activeMessageTab.value === 'SYSTEM'
    ? systemNoticeList.value
    : currentMessageList.value
))

const userSelectColumns = computed(() => [
  { title: t('layout.messageCenter.userName'), dataIndex: 'username', width: 120 },
  { title: t('layout.messageCenter.account'), dataIndex: 'account', width: 120 },
  { title: t('layout.messageCenter.department'), dataIndex: 'departmentName', ellipsis: true },
])

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
  browserTitle: 'FORGEX_MOM',
  browserIcon: '',
  systemVersion: '1.0.0',
  copyright: '© 2025 FORGEX_MOM',
  copyrightLink: '#',
  loginPageTitle: 'FORGEX_MOM',
  loginPageSubtitle: '',
  loginBackgroundType: 'image',
  loginBackgroundVideo: '/loading.mp4',
  loginBackgroundImage: '/back.jpg',
  loginBackgroundColor: '#0d0221',
  loginStyle: 'cyber',
  showOAuthLogin: true,
  showRegisterEntry: true,
  registerUrl: '/register',
  primaryColor: '#05d9e8',
  secondaryColor: '#ff2a6d'
})

function formatMediaUrl(value: string): string {
  return normalizeMediaUrl(value)
}

const headerLogo = computed(() => formatMediaUrl(systemConfig.value.systemLogo))
const headerTitle = computed(() => String(systemConfig.value.systemName || 'Forgex MOM'))

watch(
  () => [systemConfig.value.browserTitle, systemConfig.value.browserIcon, systemConfig.value.systemName],
  () => {
    applySiteBranding({
      title: systemConfig.value.browserTitle || systemConfig.value.systemName,
      icon: formatMediaUrl(systemConfig.value.browserIcon),
    })
  },
  { immediate: true },
)

locale.value = currentLocale.value as any

// 解析实际的主题模式（处理 system 模式）
const resolvedMode = computed(() => 
  resolveThemeMode(layoutConfig.value.themeMode, systemTheme.value)
)

function normalizeWorkspacePath(path: string) {
  return String(path || '').split('?')[0]
}

const menuFavoritePathList = computed(() => Array.from(menuFavoritePathSet.value))
const currentFavoritePath = computed(() => normalizeFavoritePath(route.fullPath || route.path))
const isCurrentPathFavorite = computed(() => isMenuFavorite(currentFavoritePath.value))

function shouldIgnoreContentContextMenu(target: EventTarget | null) {
  if (!(target instanceof HTMLElement)) {
    return false
  }
  return Boolean(target.closest([
    '.ant-dropdown',
    '.ant-modal',
    '.ant-drawer',
    '.ant-select-dropdown',
    '.ant-picker-dropdown',
    '.app-tabbar',
    '.app-sidebar-wrapper',
    'input',
    'textarea',
    '[contenteditable="true"]',
  ].join(',')))
}

function onContentContextMenu(event: MouseEvent) {
  if (!currentFavoritePath.value || shouldIgnoreContentContextMenu(event.target)) {
    return
  }
  event.preventDefault()
  contentFavoriteMenuPosition.value = { x: event.clientX, y: event.clientY }
  contentFavoriteMenuVisible.value = true
}

async function toggleFavoritePath(path?: string | null) {
  const normalized = normalizeFavoritePath(path)
  if (!normalized) {
    return
  }
  try {
    const favorite = await toggleMenuFavorite(normalized)
    message.success(favorite ? '已收藏本页' : '已取消收藏')
  } catch (error) {
    console.error('[MainLayout] Toggle favorite failed:', error)
    message.error('收藏操作失败')
  }
}

function onContentFavoriteMenuClick(info: any) {
  if (info?.key === 'favorite') {
    void toggleFavoritePath(currentFavoritePath.value)
  }
  contentFavoriteMenuVisible.value = false
}

function onSidebarFavoriteToggle(path: string) {
  void toggleFavoritePath(path)
}

function onTabFavorite(tab: LayoutTab) {
  void toggleFavoritePath(tab?.path || tab?.key)
}

function closeContentFavoriteMenu() {
  contentFavoriteMenuVisible.value = false
}

function handleDocumentPointerDown(event: MouseEvent) {
  if (contentFavoriteMenuVisible.value) {
    const target = event.target as HTMLElement | null
    if (target?.closest('.ant-dropdown')) {
      return
    }
    closeContentFavoriteMenu()
  }
}

function handleDocumentKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    closeContentFavoriteMenu()
  }
}

function getPendingClosedTabKeys(query: Record<string, unknown>) {
  const rawValue = query[TAB_CLOSE_QUERY_KEY]
  const rawKeys = Array.isArray(rawValue) ? rawValue : [rawValue]
  return [...new Set(
    rawKeys
      .map(item => normalizeWorkspacePath(String(item || '')))
      .filter(item => item.startsWith('/workspace'))
  )]
}

function stripTabCloseQuery(query: Record<string, unknown>) {
  const nextQuery = { ...query }
  delete nextQuery[TAB_CLOSE_QUERY_KEY]
  return nextQuery
}

function consumePendingTabCloseSignal() {
  const currentPathKey = normalizeWorkspacePath(route.path || route.fullPath)
  const closeKeys = getPendingClosedTabKeys(route.query as Record<string, unknown>)
    .filter(key => key !== currentPathKey)
  if (closeKeys.length === 0) {
    return
  }

  removeTabsByKeys(closeKeys)

  const nextQuery = stripTabCloseQuery(route.query as Record<string, unknown>) as LocationQueryRaw
  const nextRoute = { path: route.path, query: nextQuery, hash: route.hash }
  if (router.resolve(nextRoute as any).fullPath !== route.fullPath) {
    router.replace(nextRoute as any).catch(() => {})
  }
}

function isApprovalStartFormPath(path: string) {
  return /^\/workspace\/approval\/execution\/start\/[^/]+$/.test(normalizeWorkspacePath(path))
}

function shouldAutoClosePreviousTab(previousPath: string, currentPath: string) {
  const normalizedCurrentPath = normalizeWorkspacePath(currentPath)
  return isApprovalStartFormPath(previousPath) && (
    normalizedCurrentPath === '/workspace/approval/execution/start' ||
    normalizedCurrentPath === '/workspace/approval/taskConfig'
  )
}

function buildFixedTabs() {
  return [{
    key: PERSONAL_HOME_PATH,
    path: PERSONAL_HOME_PATH,
    title: resolveTabTitle(PERSONAL_HOME_PATH),
    icon: resolveTabIcon(PERSONAL_HOME_PATH),
    closable: false,
  }]
}

function ensureFixedTabs(tabList: LayoutTab[]) {
  const fixedTabs = buildFixedTabs()
  const otherTabs = tabList.filter(tab => tab.key !== PERSONAL_HOME_PATH)
  return [...fixedTabs, ...otherTabs]
}

/** 单个用户本地最多持久化的固定标签数量上限 */
const MAX_PINNED_TAB_KEYS = 80

/**
 * 读取本地持久化的固定标签路径列表。
 *
 * @returns 已规范化且位于 {@code /workspace} 下的路径，不包含个人首页
 */
function getPinnedTabKeys(): string[] {
  try {
    const raw = localStorage.getItem(PINNED_TAB_KEYS_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : []
    if (!Array.isArray(parsed)) {
      return []
    }
    return [...new Set(
      parsed
        .map((item: unknown) => normalizeWorkspacePath(String(item || '')))
        .filter(item => item.startsWith('/workspace') && item !== PERSONAL_HOME_PATH),
    )].slice(0, MAX_PINNED_TAB_KEYS)
  } catch (error) {
    console.error('[MainLayout] Failed to parse pinned tabs:', error)
    return []
  }
}

/**
 * 持久化固定标签路径列表。
 *
 * @param keys 路径 key 列表（会先规范化、去重并裁剪上限）
 */
function savePinnedTabKeys(keys: string[]) {
  try {
    const normalized = [...new Set(keys.map(k => normalizeWorkspacePath(String(k || ''))))]
      .filter(k => k.startsWith('/workspace') && k !== PERSONAL_HOME_PATH)
      .slice(0, MAX_PINNED_TAB_KEYS)
    localStorage.setItem(PINNED_TAB_KEYS_STORAGE_KEY, JSON.stringify(normalized))
  } catch (error) {
    console.error('[MainLayout] Failed to save pinned tabs:', error)
  }
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

function getRouteVisitStats(): Record<string, { count: number; lastVisitedAt: number }> {
  try {
    const raw = localStorage.getItem(ROUTE_VISIT_STATS_STORAGE_KEY)
    const parsed = raw ? JSON.parse(raw) : {}
    if (!parsed || typeof parsed !== 'object' || Array.isArray(parsed)) {
      return {}
    }
    return Object.entries(parsed).reduce<Record<string, { count: number; lastVisitedAt: number }>>((acc, [path, value]) => {
      const normalizedPath = normalizeWorkspacePath(String(path || ''))
      if (!normalizedPath.startsWith('/workspace') || normalizedPath === PERSONAL_HOME_PATH) {
        return acc
      }
      const count = Number((value as any)?.count ?? 0)
      const lastVisitedAt = Number((value as any)?.lastVisitedAt ?? 0)
      if (!Number.isFinite(count) || count <= 0) {
        return acc
      }
      acc[normalizedPath] = {
        count,
        lastVisitedAt: Number.isFinite(lastVisitedAt) ? lastVisitedAt : 0,
      }
      return acc
    }, {})
  } catch (error) {
    console.error('[MainLayout] Failed to parse route visit stats:', error)
    return {}
  }
}

function saveRouteVisitStats(stats: Record<string, { count: number; lastVisitedAt: number }>) {
  try {
    const entries = Object.entries(stats)
      .sort((a, b) => (b[1]?.lastVisitedAt || 0) - (a[1]?.lastVisitedAt || 0))
      .slice(0, MAX_ROUTE_VISIT_STATS_COUNT)
    localStorage.setItem(ROUTE_VISIT_STATS_STORAGE_KEY, JSON.stringify(Object.fromEntries(entries)))
  } catch (error) {
    console.error('[MainLayout] Failed to save route visit stats:', error)
  }
}

function updateRecentRoutes(path: string) {
  const normalizedPath = normalizeWorkspacePath(path)
  if (!normalizedPath.startsWith('/workspace') || normalizedPath === PERSONAL_HOME_PATH) {
    return
  }
  const nextRoutes = [normalizedPath, ...getRecentRoutes().filter(item => item !== normalizedPath)]
  saveRecentRoutes(nextRoutes)

  const currentStats = getRouteVisitStats()
  const previousStat = currentStats[normalizedPath]
  currentStats[normalizedPath] = {
    count: Math.max(Number(previousStat?.count || 0), 0) + 1,
    lastVisitedAt: Date.now(),
  }
  saveRouteVisitStats(currentStats)

  reportUserMenuVisit(normalizedPath).catch(() => {})
  reportSystemMenuOpenIfNeeded(normalizedPath)
}

async function reportSystemMenuOpenIfNeeded(path: string) {
  const normalizedPath = normalizeWorkspacePath(path)
  if (!normalizedPath.startsWith('/workspace/sys/')) {
    return
  }

  const currentGuideConfig = resolveSystemPageGuide(normalizedPath)
  const currentGuideState = guideStore.getMergedGuideState(currentGuideConfig.guideCode)
  if ((currentGuideState?.status === 'PENDING' || guideStore.babyModeEnabled)
    && guideStore.shouldAutoStartSystemPageGuide(currentGuideConfig.guideCode, currentGuideConfig.version)) {
    reportUserMenuOpen(normalizedPath).catch(() => {})
    await startSystemGuide(currentGuideConfig)
    return
  }

  try {
    const result = await reportUserMenuOpen(normalizedPath)
    if (!result?.firstOpen) {
      return
    }
    const guideConfig = resolveSystemPageGuide(result.path || normalizedPath)
    if (!guideStore.shouldAutoStartSystemPageGuide(guideConfig.guideCode, guideConfig.version)) {
      return
    }
    await startSystemGuide(guideConfig)
  } catch (_) {
  }
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
  const pinnedRemain = getPinnedTabKeys().filter(k => !keys.includes(k))
  savePinnedTabKeys(pinnedRemain)
  tabs.value = ensureFixedTabs(tabs.value.filter(tab => !keys.includes(tab.key)))
}

watch(
  resolvedMode,
  (mode, previousMode) => {
    if (skipThemeRevealWatcher.value) {
      skipThemeRevealWatcher.value = false
      applyDocumentTheme(mode)
      return
    }
    if (previousMode && previousMode !== mode) {
      runThemeRevealTransition(previousMode, mode)
      return
    }
    applyDocumentTheme(mode)
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

watch(
  rootStyle,
  styleMap => {
    syncThemeVariablesToDocument(styleMap as Record<string, unknown>)
  },
  { immediate: true },
)

const pageTransitionName = computed(() =>
  layoutConfig.value.pageTransition === 'fade' ? 'fx-fade' : 'fx-horizontal'
)

const headerHiddenByScroll = ref(false)
const lastScrollY = ref(typeof window !== 'undefined' ? window.scrollY || 0 : 0)
const showBackTop = ref(false)
let pageScrollEl: HTMLElement | null = null

const showHeader = computed(() => layoutConfig.value.headerVisible && !headerHiddenByScroll.value)

function resolveCurrentPageScrollEl() {
  if (typeof document === 'undefined') {
    return null
  }
  return document.querySelector('.fx-page-wrapper') as HTMLElement | null
}

function updateBackTopVisible() {
  const el = resolveCurrentPageScrollEl()
  showBackTop.value = !!el && el.scrollTop > 240
}

function handlePageScroll() {
  updateBackTopVisible()
}

function bindCurrentPageScroll() {
  const nextEl = resolveCurrentPageScrollEl()
  if (pageScrollEl === nextEl) {
    updateBackTopVisible()
    return
  }
  pageScrollEl?.removeEventListener('scroll', handlePageScroll)
  pageScrollEl = nextEl
  pageScrollEl?.addEventListener('scroll', handlePageScroll, { passive: true })
  updateBackTopVisible()
}

function scrollCurrentPageToTop() {
  const el = pageScrollEl || resolveCurrentPageScrollEl()
  el?.scrollTo({ top: 0, behavior: 'smooth' })
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

type ModuleRouteNode = {
  name?: string
  path?: string
  meta?: Record<string, any>
  children?: ModuleRouteNode[]
}

function getModuleRouteTree(moduleCode: string): ModuleRouteNode | undefined {
  const routes = Array.isArray(dynamicRoutes.value) ? dynamicRoutes.value : []
  return routes.find((item: any) => item?.path === moduleCode || item?.meta?.module === moduleCode)
}

function buildModuleMenuPath(moduleCode: string, parentSegments: string[], menuPath: string) {
  const normalizedPath = String(menuPath || '').trim()
  if (!normalizedPath) {
    return {
      fullPath: '',
      segments: parentSegments
    }
  }
  if (normalizedPath.startsWith('/')) {
    return {
      fullPath: normalizedPath,
      segments: normalizedPath.split('/').filter(Boolean)
    }
  }
  const currentSegments = normalizedPath.split('/').filter(Boolean)
  const segments = [...parentSegments, ...currentSegments]
  return {
    fullPath: `/workspace/${moduleCode}/${segments.join('/')}`.replace(/\/+/g, '/'),
    segments
  }
}

function findFirstNavigableMenuPath(
  moduleCode: string,
  menus: ModuleRouteNode[],
  options: { preferDashboard?: boolean } = {},
  parentSegments: string[] = []
): string {
  for (const menu of menus) {
    const menuPath = String(menu?.path || '')
    const { fullPath, segments } = buildModuleMenuPath(moduleCode, parentSegments, menuPath)
    const children = Array.isArray(menu?.children) ? menu.children : []
    const isCatalog = String(menu?.meta?.type || '').toLowerCase() === 'catalog'
    const isDashboard = segments[segments.length - 1] === 'dashboard'

    if (options.preferDashboard && !isCatalog && fullPath && isDashboard) {
      return fullPath
    }

    if (children.length > 0) {
      const childTarget = findFirstNavigableMenuPath(moduleCode, children, options, segments)
      if (childTarget) {
        return childTarget
      }
    }

    if (!isCatalog && fullPath) {
      return fullPath
    }
  }
  return ''
}

function resolveModuleEntryPath(moduleCode: string): string {
  const moduleRoute = getModuleRouteTree(moduleCode)
  const children = Array.isArray(moduleRoute?.children) ? moduleRoute.children : []
  if (children.length === 0) {
    return ''
  }
  return (
    findFirstNavigableMenuPath(moduleCode, children, { preferDashboard: true }) ||
    findFirstNavigableMenuPath(moduleCode, children)
  )
}

function buildSearchMenuNodes(
  moduleCode: string,
  nodes: ModuleRouteNode[] = [],
  parentSegments: string[] = [],
  moduleName = '',
): any[] {
  const result: any[] = []

  for (const node of nodes) {
    const hidden = node?.meta?.hidden === true
    if (hidden) {
      continue
    }

    const nodePath = String(node?.path || '')
    const title = resolveMenuTitle((node?.meta && node.meta.title) || nodePath)
    const icon = String(node?.meta?.icon || '')
    const type = String(node?.meta?.type || 'menu').toLowerCase()
    const { fullPath, segments } = buildModuleMenuPath(moduleCode, parentSegments, nodePath)
    const children = buildSearchMenuNodes(
      moduleCode,
      Array.isArray(node?.children) ? node.children : [],
      segments,
      moduleName,
    )

    if (!title) {
      result.push(...children)
      continue
    }

    result.push({
      key: fullPath || `${moduleCode}:${segments.join('/') || title}`,
      title,
      icon,
      path: fullPath,
      moduleCode,
      moduleName,
      type: type === 'catalog' ? 'dir' : 'menu',
      children,
    })
  }

  return result
}

const shouldShowSidebar = computed(() => {
  const currentPath = normalizeWorkspacePath(route.fullPath)
  if (layoutConfig.value.layoutMode === 'top') {
    return false
  }
  if (currentPath === PERSONAL_HOME_PATH || currentPath === FAVORITE_MANAGEMENT_PATH) {
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
  const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []

  for (const mod of modules) {
    const moduleCode = String(mod?.code || '')
    if (!moduleCode) {
      continue
    }

    const moduleName = resolveModuleDisplayName(moduleCode, mod?.name)
    const moduleRoute = getModuleRouteTree(moduleCode)
    const children = buildSearchMenuNodes(
      moduleCode,
      Array.isArray(moduleRoute?.children) ? moduleRoute.children : [],
      [],
      moduleName,
    )
    if (children.length === 0) {
      continue
    }

    result.push({
      key: `module:${moduleCode}`,
      title: moduleName,
      path: '',
      moduleCode,
      moduleName,
      type: 'dir',
      children,
    })
  }

  return result
})

// 模块列表（包含图标）
const moduleList = computed(() => {
  const modules = Array.isArray(dynamicModules.value) ? dynamicModules.value : []
  return modules.map((mod: any) => ({
    code: String(mod.code || ''),
    name: resolveModuleDisplayName(String(mod.code || ''), mod.name || mod.code),
    icon: mod.icon || 'appstore',
    order: mod.order || 0
  }))
})

// 侧边栏菜单数据（转换为侧边栏组件期望的格式）
const activeModuleName = computed(() => {
  const module = moduleList.value.find(item => item.code === activeModuleCode.value)
  return module?.name || ''
})

function normalizeMenuType(rawType: unknown, hasChildren = false): SidebarMenuType {
  const type = String(rawType || '').toLowerCase()
  if (type === 'button') {
    return 'button'
  }
  if (type === 'catalog' || type === 'dir' || type === 'directory') {
    return 'catalog'
  }
  return hasChildren ? 'catalog' : 'menu'
}

function buildSidebarMenuNodes(
  moduleCode: string,
  nodes: ModuleRouteNode[] = [],
  parentSegments: string[] = [],
  parentKey?: string,
  moduleName = '',
  level = 1,
): SidebarMenuItem[] {
  const result: SidebarMenuItem[] = []

  for (const node of nodes) {
    if (node?.meta?.hidden === true) {
      continue
    }

    const nodePath = String(node?.path || '')
    const { fullPath, segments } = buildModuleMenuPath(moduleCode, parentSegments, nodePath)
    const childItems = buildSidebarMenuNodes(
      moduleCode,
      Array.isArray(node?.children) ? node.children : [],
      segments,
      fullPath || parentKey,
      moduleName,
      level + 1,
    )
    const title = resolveMenuTitle((node?.meta && node.meta.title) || node.name || nodePath)
    const type = normalizeMenuType(node?.meta?.type, childItems.length > 0)

    if (!title) {
      result.push(...childItems)
      continue
    }

    result.push({
      key: fullPath || `${moduleCode}:${segments.join('/') || title}`,
      title,
      icon: String(node?.meta?.icon || ''),
      path: fullPath,
      moduleCode,
      moduleName,
      parentKey,
      menuLevel: Number(node?.meta?.menuLevel || level),
      type,
      children: childItems,
    })
  }

  return result
}

function getModuleMenus(moduleCode: string): SidebarMenuItem[] {
  const moduleRoute = getModuleRouteTree(moduleCode)
  const moduleName = resolveModuleDisplayName(moduleCode, moduleRoute?.meta?.title || moduleCode)
  return buildSidebarMenuNodes(
    moduleCode,
    Array.isArray(moduleRoute?.children) ? moduleRoute.children : [],
    [],
    undefined,
    moduleName,
    1,
  )
}

function buildModuleRootMenu(module: { code: string; name: string; icon?: string }): SidebarMenuItem {
  const children = getModuleMenus(module.code)
  return {
    key: `module:${module.code}`,
    title: module.name,
    icon: module.icon || 'appstore',
    path: '',
    moduleCode: module.code,
    moduleName: module.name,
    menuLevel: 1,
    type: 'module',
    children,
  }
}

const sidebarMenus = computed<SidebarMenuItem[]>(() => {
  const mode = layoutConfig.value.layoutMode
  if (mode === 'mix' || mode === 'top') {
    return activeModuleCode.value ? getModuleMenus(activeModuleCode.value) : []
  }

  return moduleList.value
    .filter(module => module.code)
    .map(module => buildModuleRootMenu(module))
})

// 当前用户信息
const sidebarDoubleColumn = computed(() => (
  layoutConfig.value.layoutMode === 'vertical-mix' ||
  layoutConfig.value.layoutMode === 'mix'
))

const horizontalModuleMenus = computed(() => (
  layoutConfig.value.layoutMode === 'top' && activeModuleCode.value
    ? getModuleMenus(activeModuleCode.value)
    : []
))

function flattenNavigableMenus(menus: SidebarMenuItem[] = []): SidebarMenuItem[] {
  const result: SidebarMenuItem[] = []
  for (const item of menus) {
    if (item.type !== 'catalog' && item.type !== 'module' && item.path) {
      result.push(item)
    }
    if (item.children?.length) {
      result.push(...flattenNavigableMenus(item.children))
    }
  }
  return result
}

function countNavigableMenus(menus: SidebarMenuItem[] = []) {
  return flattenNavigableMenus(menus).length
}

function getHorizontalMenuChildren(item: SidebarMenuItem) {
  return flattenNavigableMenus(item.children || []).slice(0, HORIZONTAL_MENU_CHILD_LIMIT)
}

function resolveFirstNavigableMenu(item: SidebarMenuItem): SidebarMenuItem | null {
  if (item.type !== 'catalog' && item.type !== 'module' && item.path) {
    return item
  }
  const children = item.children || []
  for (const child of children) {
    const found = resolveFirstNavigableMenu(child)
    if (found) {
      return found
    }
  }
  return null
}

function onHorizontalMenuClick(item: SidebarMenuItem) {
  const target = resolveFirstNavigableMenu(item)
  if (target?.path) {
    horizontalMenuDrawerOpen.value = false
    onMenuClick(target.path)
  }
}

function isHorizontalMenuActive(item: SidebarMenuItem) {
  const currentPath = normalizeWorkspacePath(route.fullPath)
  if (item.path && currentPath === item.path) {
    return true
  }
  return flattenNavigableMenus(item.children || []).some(child => child.path === currentPath)
}

function resolveMenuIcon(item: SidebarMenuItem) {
  if (item.type === 'module') {
    return AppstoreOutlined
  }
  return item.children?.length ? FolderOutlined : FileOutlined
}

const currentUser = computed(() => {
  const info = userStore.userInfo || {
    account: currentAccount.value,
    username: currentAccount.value,
    avatar: ''
  }
  
  // 处理头像 URL
  const avatar = normalizeMediaUrl(info.avatar)

  return {
    account: info.account,
    name: info.username || info.account,
    avatar
  }
})

async function loadTenantOptions() {
  tenantLoading.value = true
  try {
    const list = await listCurrentTenants()
    tenantOptions.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('[MainLayout] 加载可切换租户失败:', error)
    tenantOptions.value = []
  } finally {
    tenantLoading.value = false
  }
}

async function onTenantChange(tenantId: string) {
  const targetTenantId = String(tenantId || '')
  if (!targetTenantId || targetTenantId === currentTenantId.value || switchingTenantId.value) {
    return
  }

  const account = currentUser.value.account || currentAccount.value || sessionStorage.getItem('account') || ''
  if (!account) {
    message.warning(t('layout.tenant.currentAccountMissing'))
    return
  }

  const targetTenant = tenantOptions.value.find(item => String(item.id) === targetTenantId)
  switchingTenantId.value = targetTenantId
  try {
    const result = await chooseTenant({ account, tenantId: targetTenantId })
    const nextTenantId = String(result?.tenantId || targetTenantId)

    userStore.setUserInfo({
      account: result?.account || account,
      username: result?.username || result?.account || account,
      email: result?.email,
      phone: result?.phone,
      avatar: result?.avatar,
      tenantId: nextTenantId,
      tenantName: targetTenant?.name
    })
    currentAccount.value = result?.account || account
    currentTenantId.value = nextTenantId
    sessionStorage.setItem('account', currentAccount.value)
    sessionStorage.setItem('tenantId', nextTenantId)

    const routesRes = await getRoutes({ account: currentAccount.value, tenantId: nextTenantId })
    permissionStore.setPermissions(routesRes?.buttons || [])
    permissionStore.setRoutes(routesRes?.routes || [])
    permissionStore.setModules(routesRes?.modules || [])
    await injectDynamicRoutes(routesRes)

    tabs.value = buildFixedTabs()
    activeTabKey.value = PERSONAL_HOME_PATH
    selectedKeys.value = [PERSONAL_HOME_PATH]
    activeModuleCode.value = ''
    openKeys.value = []
    messageSendForm.value.receiverTenantId = Number(nextTenantId) || undefined
    window.dispatchEvent(new CustomEvent('fx:tenant-changed', { detail: { tenantId: nextTenantId } }))

    await loadTenantOptions()
    await router.push(PERSONAL_HOME_PATH)
    message.success(t('layout.tenant.switchSuccess'))
  } catch (error) {
    console.error('[MainLayout] 切换租户失败:', error)
    message.error(t('layout.tenant.switchFailed'))
  } finally {
    switchingTenantId.value = ''
  }
}

function canAutoStartSystemGuide() {
  if (!systemGuideReady.value) {
    return false
  }
  if (guideStore.systemPageGuideDisabled && !guideStore.babyModeEnabled) {
    return false
  }
  if (currentSystemGuideCode.value !== 'system.main') {
    return pendingSystemFirstOpenGuide.value
      && guideStore.shouldAutoStartSystemPageGuide(currentSystemGuideCode.value, currentSystemGuideVersion.value)
  }
  if (route.path !== PERSONAL_HOME_PATH) {
    return false
  }
  currentSystemGuideVersion.value = 'v1'
  return guideStore.shouldAutoStartSystemPageGuide('system.main', 'v1')
}

function syncSystemGuideAutoStart() {
  systemGuideAutoStart.value = canAutoStartSystemGuide()
}

async function startSystemGuide(config: { guideCode: string; version: string; steps: FxGuideStep[] }) {
  currentSystemGuideCode.value = config.guideCode
  currentSystemGuideVersion.value = config.version
  currentSystemPageGuideSteps.value = config.steps
  pendingSystemFirstOpenGuide.value = true
  await nextTick()
  systemGuideStartKey.value += 1
  syncSystemGuideAutoStart()
}

function handleSystemGuideOpen() {
  guideStore.startGuide(currentSystemGuideCode.value)
}

function handleSystemGuideClose() {
  systemGuideAutoStart.value = false
  pendingSystemFirstOpenGuide.value = false
  guideStore.finishCurrentGuide()
}

async function handleSystemGuideFinish(guideCode = currentSystemGuideCode.value, version = 'v1') {
  await guideStore.markGuideCompleted(guideCode, version)
  systemGuideAutoStart.value = false
  pendingSystemFirstOpenGuide.value = false
  guideStore.finishCurrentGuide()
}

async function handleSystemGuideSkip(guideCode = currentSystemGuideCode.value, version = 'v1') {
  await guideStore.markGuideSkipped(guideCode, version)
  systemGuideAutoStart.value = false
  pendingSystemFirstOpenGuide.value = false
  guideStore.finishCurrentGuide()
}

async function handleSystemGuideSkipAll(guideCode = currentSystemGuideCode.value, version = 'v1') {
  await guideStore.markGuideSkipped(guideCode, version)
  await guideStore.setSystemPageGuideDisabled(true)
  systemGuideAutoStart.value = false
  pendingSystemFirstOpenGuide.value = false
  guideStore.finishCurrentGuide()
}

async function loadGuidePreference() {
  await guideStore.loadPreference()
  systemGuideReady.value = true
  const normalizedPath = normalizeWorkspacePath(route.fullPath || route.path)
  if (normalizedPath.startsWith('/workspace/sys/')) {
    const currentGuideConfig = resolveSystemPageGuide(normalizedPath)
    const currentGuideState = guideStore.getMergedGuideState(currentGuideConfig.guideCode)
    if ((currentGuideState?.status === 'PENDING' || guideStore.babyModeEnabled)
      && guideStore.shouldAutoStartSystemPageGuide(currentGuideConfig.guideCode, currentGuideConfig.version)) {
      await startSystemGuide(currentGuideConfig)
      return
    }
  }
  syncSystemGuideAutoStart()
}

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
 * 解析标签标题（各模块工作台 dashboard 带模块名前缀，避免多个"首页"无法区分）
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

function resolveTabIcon(tabKey: string): string {
  const clean = normalizeWorkspacePath(tabKey)
  const resolved = router.resolve(clean)
  const matchedRouteWithIcon = [...resolved.matched].reverse().find(item => item.meta && item.meta.icon)
  if (matchedRouteWithIcon?.meta?.icon) {
    return String(matchedRouteWithIcon.meta.icon)
  }
  const moduleCode = clean.match(/^\/workspace\/([^/]+)/)?.[1]
  const module = moduleList.value.find(item => item.code === moduleCode)
  return module?.icon || 'appstore'
}

/**
 * 各模块"工作台"页签标题
 * <p>
 * 直接返回路由标题，不添加模块名前缀
 * </p>
 *
 * @param moduleCode 模块编码，如 sys、approval
 * @returns 展示用标题
 */
function buildModuleDashboardTitle(moduleCode: string): string {
  return buildTitleFromRoute(`/workspace/${moduleCode}/dashboard`)
}

/**
 * 更新所有标签的标题
 */
function updateAllTabTitles() {
  const pinnedSet = new Set(getPinnedTabKeys())
  tabs.value = ensureFixedTabs(tabs.value.map(tab => ({
    ...tab,
    pinned: pinnedSet.has(tab.key),
    title: resolveTabTitle(tab.key),
    icon: resolveTabIcon(tab.key),
    closable: tab.key !== PERSONAL_HOME_PATH && !pinnedSet.has(tab.key),
  })))
}

watch(
  () => route.fullPath,
  (path, previousPath) => {
    closeContentFavoriteMenu()
    consumePendingTabCloseSignal()
    if (shouldAutoClosePreviousTab(String(previousPath || ''), path)) {
      removeTabsByKeys([normalizeWorkspacePath(String(previousPath || ''))])
    }
    const cleanPath = normalizeWorkspacePath(path)
    selectedKeys.value = [cleanPath]
    if (!cleanPath.startsWith('/workspace/sys/')) {
      currentSystemGuideCode.value = 'system.main'
      currentSystemGuideVersion.value = 'v1'
      currentSystemPageGuideSteps.value = []
      pendingSystemFirstOpenGuide.value = false
    }
    
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
    syncSystemGuideAutoStart()
    void nextTick(bindCurrentPageScroll)
  },
  { immediate: true }
)

watch(
  () => guideStore.preference,
  () => {
    syncSystemGuideAutoStart()
  },
  { deep: true }
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
    tabBarMax: typeof cfg.tabBarMax === 'number' && Number.isFinite(cfg.tabBarMax) && cfg.tabBarMax >= 0 && cfg.tabBarMax <= 200
      ? Math.floor(cfg.tabBarMax)
      : DEFAULT_LAYOUT_CONFIG.tabBarMax,
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
    tableRowDensity: cfg.tableRowDensity === 'comfortable' || cfg.tableRowDensity === 'compact'
      ? cfg.tableRowDensity
      : 'normal',
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
 * 刷新当前会话菜单权限，并在当前页面被撤权时回到个人主页。
 *
 * @param showMessage 是否显示手动操作结果
 * @returns 是否刷新成功
 */
async function refreshMenuPermissions(showMessage = false): Promise<boolean> {
  if (permissionRefreshPromise) {
    return permissionRefreshPromise
  }

  const currentPath = route.fullPath
  permissionRefreshing.value = true
  permissionRefreshPromise = (async () => {
    try {
      const refreshed = await refreshDynamicRoutes({ silent: true })
      if (!refreshed) {
        throw new Error('permission refresh returned no route payload')
      }

      const revokedTabs = tabs.value
        .filter(tab => tab.key !== PERSONAL_HOME_PATH)
        .filter(tab => router.resolve(tab.key).name === 'WorkspaceNotFound')
        .map(tab => tab.key)
      removeTabsByKeys(revokedTabs)

      const currentResolved = router.resolve(currentPath)
      if (currentResolved.name === 'WorkspaceNotFound') {
        await router.replace(PERSONAL_HOME_PATH)
      } else {
        updateTabsByRoute(route.fullPath)
      }

      if (showMessage) {
        message.success(t('layout.user.refreshPermissionsSuccess'))
      }
      return true
    } catch (error) {
      console.error('[MainLayout] 菜单权限刷新失败:', error)
      if (showMessage) {
        message.error(t('layout.user.refreshPermissionsFailed'))
      }
      return false
    } finally {
      permissionRefreshing.value = false
    }
  })().finally(() => {
    permissionRefreshPromise = null
  })

  return permissionRefreshPromise
}

/**
 * 打开消息通知抽屉
 * <p>
 * 加载当前用户收到的消息列表，并打开抽屉显示
 * </p>
 */
async function openMessageDrawer() {
  messageDrawerOpen.value = true
  await refreshMessageCenter(true)
}

function handleOpenMessageDrawerEvent(event?: Event) {
  const detail = (event as CustomEvent<{ tab?: MessageCategory } | undefined>)?.detail
  if (detail?.tab) {
    activeMessageTab.value = normalizeMessageCategory(detail.tab)
  }
  openMessageDrawer()
}

function handleOpenGlobalSearchEvent() {
  globalSearchVisible.value = true
}

/**
 * 加载消息列表
 */
async function loadMessages(category: MessageCategory = activeMessageTab.value) {
  messageLoading.value = true
  try {
    if (category === 'SYSTEM') {
      const list = await noticeApi.activeList({ maxCount: 20 })
      systemNoticeList.value = Array.isArray(list) ? list : []
      return
    }
    const list = await listUnreadMessages(20, 'MESSAGE')
    messageLists.value = {
      ...messageLists.value,
      MESSAGE: Array.isArray(list) ? list : [],
    }
  } catch (error) {
    console.error('加载消息列表失败:', error)
  } finally {
    messageLoading.value = false
  }
}

function normalizeMessageCategory(category?: string): MessageCategory {
  return category === 'SYSTEM' ? 'SYSTEM' : 'MESSAGE'
}

function dispatchMessageRefreshEvent() {
  if (typeof window === 'undefined') {
    return
  }
  window.dispatchEvent(new CustomEvent('fx:message-refresh', {
    detail: {
      ...messageCounts.value,
      total: messageCounts.value.SYSTEM + messageCounts.value.MESSAGE,
    },
  }))
}

async function refreshMessageCounts() {
  try {
    const [systemNoticeListResult, messageCount] = await Promise.all([
      noticeApi.activeList({ maxCount: 100 }),
      getUnreadMessageCount('MESSAGE'),
    ])
    messageCounts.value = {
      SYSTEM: Array.isArray(systemNoticeListResult) ? systemNoticeListResult.length : 0,
      MESSAGE: Number(messageCount || 0),
    }
    if (Array.isArray(systemNoticeListResult)) {
      systemNoticeList.value = systemNoticeListResult
    }
    dispatchMessageRefreshEvent()
  } catch (error) {
    console.error('刷新未读消息数量失败:', error)
  }
}

async function refreshMessageCenter(reloadCurrentList = false) {
  await refreshMessageCounts()
  if (reloadCurrentList || messageDrawerOpen.value) {
    await loadMessages(activeMessageTab.value)
  }
}

function removeMessageFromList(messageId: number, category: MessageCategory) {
  const currentList = messageLists.value.MESSAGE || []
  messageLists.value = {
    ...messageLists.value,
    MESSAGE: currentList.filter(item => item.id !== messageId),
  }
}

function prependMessageToList(messageRecord: SysMessageVO) {
  const category = normalizeMessageCategory(messageRecord.category)
  if (category === 'SYSTEM') {
    return
  }
  const currentList = messageLists.value[category] || []
  if (currentList.some(item => item.id === messageRecord.id)) {
    return
  }
  messageLists.value = {
    ...messageLists.value,
    [category]: [{
      ...messageRecord,
      category,
      createTime: messageRecord.createTime || new Date().toLocaleString('zh-CN', { hour12: false }),
    }, ...currentList].slice(0, 20),
  }
}

async function handleMessageTabChange(key: string) {
  activeMessageTab.value = normalizeMessageCategory(key)
  await loadMessages(activeMessageTab.value)
}

async function handleMessageItemClick(messageRecord: SysMessageVO) {
  const category: MessageCategory = 'MESSAGE'
  try {
    await markMessageRead(messageRecord.id, { showSuccessMessage: false })
    removeMessageFromList(messageRecord.id, category)
    messageCounts.value = {
      ...messageCounts.value,
      [category]: Math.max(0, Number(messageCounts.value[category] || 0) - 1),
    }
    dispatchMessageRefreshEvent()
    refreshMessageCounts().catch(() => {})
    if (messageRecord.linkUrl) {
      router.push(messageRecord.linkUrl).catch(() => {})
    }
  } catch (error) {
    console.error('读取消息失败:', error)
  }
}

function handleNoticeItemClick(noticeRecord: SysNotice) {
  if (!noticeRecord?.id) {
    return
  }
  router.push({ name: 'SystemNotice' }).catch(() => {})
  messageDrawerOpen.value = false
}

function formatNoticeDisplayTime(noticeRecord: SysNotice) {
  return noticeRecord.startTime || noticeRecord.createTime || ''
}

function handleMessageReceivedEvent(event: Event) {
  const detail = (event as CustomEvent<SysMessageVO | undefined>).detail
  if (!detail || !detail.id) {
    refreshMessageCenter(messageDrawerOpen.value).catch(() => {})
    return
  }
  const normalized = {
    ...detail,
    category: normalizeMessageCategory(detail.category),
  } as SysMessageVO
  if (messageDrawerOpen.value && activeMessageTab.value === normalized.category) {
    prependMessageToList(normalized)
  }
  refreshMessageCenter(messageDrawerOpen.value).catch(() => {})
}

function handleSystemNoticeRefreshEvent() {
  refreshMessageCenter(messageDrawerOpen.value).catch(() => {})
}

function onModuleClick(moduleCode: string) {
  if (!moduleCode) return
  activeModuleCode.value = moduleCode
  if (layoutConfig.value.layoutMode === 'top') {
    horizontalMenuDrawerOpen.value = true
    return
  }
  // 切换模块时，跳转到该模块的第一个菜单项
  const targetPath = resolveModuleEntryPath(moduleCode)
  if (targetPath && targetPath !== route.fullPath) {
    router.push(targetPath).catch(() => {})
  }
}

function resetLayout() {
  layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
}

function applyCachedLayoutConfig() {
  const cached = localStorage.getItem('fx-layout-config')
  if (!cached) {
    return false
  }
  try {
    const parsed = JSON.parse(cached)
    layoutConfig.value = normalizeLayoutConfig(parsed)
    return true
  } catch (e) {
    return false
  }
}

async function loadLayout() {
  const account = sessionStorage.getItem('account')
  const tenantId = sessionStorage.getItem('tenantId')
  const hasCachedLayout = applyCachedLayoutConfig()
  if (!account || !tenantId) {
    if (!hasCachedLayout) {
      layoutConfig.value = { ...DEFAULT_LAYOUT_CONFIG }
    }
    return
  }
  try {
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
  if (!val || val === currentLocale.value) {
    return
  }
  // 保存原始语言设置，以便失败时恢复
  const originalLocale = currentLocale.value
  
  try {
    console.log('[MainLayout] 语言切换开始:', val)
    
    // 1. 调用 setLocale 函数更新语言设置
    setLocale(val as LocaleCode)
    
    // 2. 更新本地状态
    currentLocale.value = val
    appStore.setLocale(val as LocaleCode)
    
    // 3. 调用后端 API 更新语言设置
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

    if (typeof window !== 'undefined') {
      window.location.reload()
      return
    }
  } catch (e) {
    console.error('[MainLayout] 语言切换失败:', e)
    // 语言切换失败时，恢复到原来的语言设置
    setLocale(originalLocale as LocaleCode)
    currentLocale.value = originalLocale
    appStore.setLocale(originalLocale as LocaleCode)
  }
}

function onUserMenuClick(key: string) {
  if (!key) return
  
  if (key === 'logout') {
    // 调用 userStore 的 logout 方法，会自动调用后端登出接口
    userStore.logout()
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

  if (key === 'guide') {
    router.push('/workspace/profile?tab=guide')
    return
  }

  if (key === 'messageSend') {
    messageSendOpen.value = true
    return
  }

  if (key === 'refreshPermissions') {
    void refreshMenuPermissions(true)
    return
  }
  
  if (key === 'resetPassword') {
    message.info(t('layout.user.resetPasswordNotReady'))
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

  const pinnedSet = new Set(getPinnedTabKeys())

  let nextTabs = ensureFixedTabs(
    tabs.value.map(tab => ({
      ...tab,
      pinned: pinnedSet.has(tab.key),
      title: resolveTabTitle(tab.key),
      icon: resolveTabIcon(tab.key),
      closable: tab.key !== PERSONAL_HOME_PATH && !pinnedSet.has(tab.key),
    })),
  )
  if (pathWithoutQuery !== PERSONAL_HOME_PATH) {
    if (!nextTabs.some(tab => tab.key === pathWithoutQuery)) {
      nextTabs.push({
        key: pathWithoutQuery,
        path: pathWithoutQuery,
        title: resolveTabTitle(pathWithoutQuery),
        icon: resolveTabIcon(pathWithoutQuery),
        pinned: pinnedSet.has(pathWithoutQuery),
        closable: pathWithoutQuery !== PERSONAL_HOME_PATH && !pinnedSet.has(pathWithoutQuery),
      })
    }

    const maxTabs = layoutConfig.value.tabBarMax
    if (typeof maxTabs === 'number' && maxTabs > 0) {
      while (nextTabs.length > maxTabs) {
        const removeIndex = nextTabs.findIndex(
          tab => tab.closable && tab.key !== pathWithoutQuery && !tab.pinned,
        )
        if (removeIndex === -1) {
          break
        }
        removeRecentRoute(nextTabs[removeIndex].key)
        nextTabs.splice(removeIndex, 1)
      }
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
      return resolveMenuTitle(title)
    }
    return title
  }

  const allRoutes = router.getRoutes()
  const match = allRoutes.find(r => r.path === pathWithoutQuery)
  if (match && match.meta && match.meta.title) {
    const title = match.meta.title as string
    // 如果 title 是国际化 key，使用 t 函数翻译
    if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
      return resolveMenuTitle(title)
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
      // 如果 title 是国际化 key，使用 t 函数翻译
      if (title.startsWith('system.') || title.startsWith('common.') || title.includes('.')) {
        return resolveMenuTitle(title)
      }
      return title
    }
    if (child && child.name) {
      return child.name
    }
  }
  const mod = (Array.isArray(dynamicModules.value) ? dynamicModules.value : []).find((m: any) => String(m.code || '') === moduleCode)
  if (mod) {
    return resolveModuleDisplayName(moduleCode, mod.name)
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
  const meta = tabs.value.find(t => t.key === key)
  if (meta?.pinned) {
    return
  }
  const idx = tabs.value.findIndex(t => t.key === key)
  if (idx === -1) return
  const isActive = activeTabKey.value === key
  const nextKey = resolveNextTabKey(key)
  if (!isActive) {
    removeTabsByKeys([key])
    return
  }

  activeTabKey.value = nextKey
  router.push(nextKey)
    .then(() => {
      removeTabsByKeys([key])
    })
    .catch(() => {
      activeTabKey.value = key
    })
}

function onTabDrag(fromIndex: number, toIndex: number) {
  if (!layoutConfig.value.tabBarDraggable) return
  if (fromIndex === -1 || toIndex === -1 || fromIndex === toIndex) return
  const moved = tabs.value.splice(fromIndex, 1)[0]
  tabs.value.splice(toIndex, 0, moved)
}

function onTabPin(tab: LayoutTab) {
  if (!tab?.key || tab.key === PERSONAL_HOME_PATH) {
    return
  }
  const path = normalizeWorkspacePath(tab.key)
  const nextPinned = !tab.pinned
  const keys = new Set(getPinnedTabKeys())
  if (nextPinned) {
    keys.add(path)
  } else {
    keys.delete(path)
  }
  savePinnedTabKeys([...keys])
  tabs.value = tabs.value.map(t => {
    if (t.key !== path) {
      return t
    }
    return {
      ...t,
      pinned: nextPinned,
      closable: path !== PERSONAL_HOME_PATH && !nextPinned,
    }
  })
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
      .filter(t => t.key !== key && t.key !== PERSONAL_HOME_PATH && !t.pinned)
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
        await handleMessageItemClick(m)
      } catch (_) {}
      notification.close(key)
    },
  })
}

async function checkLicenseGracePeriod() {
  try {
    const status = await getLicenseStatus()
    if (!status || (status.status !== 'GRACE' && status.status !== 'EXPIRING_SOON')) return
    const noticeKey = `${status.status}:${status.expireAt || ''}`
    if (sessionStorage.getItem(licenseGraceNoticeKey) === noticeKey) return
    sessionStorage.setItem(licenseGraceNoticeKey, noticeKey)
    const isChinese = String(locale.value).toLowerCase().startsWith('zh')
    const inGrace = status.status === 'GRACE'
    notification.warning({
      key: 'fx-license-grace',
      message: isChinese ? (inGrace ? '授权已进入宽限期' : '授权即将到期') : (inGrace ? 'License is in the grace period' : 'License is expiring soon'),
      description: status.expireAt
        ? (isChinese ? `到期时间：${status.expireAt}` : `Expires at: ${status.expireAt}`)
        : (isChinese ? '请联系授权人续期' : 'Please contact the issuer for renewal'),
      placement: 'bottomRight',
      duration: 0,
    })
  } catch (_) {
    // 授权状态检查失败不影响主业务页面加载。
  }
}

async function handleMessageSend() {
  if (!messageSendForm.value.receiverUserId) {
    message.warning(t('layout.messageCenter.selectReceiverWarning'))
    return
  }
  if (!messageSendForm.value.title) {
    message.warning(t('layout.messageCenter.titleRequired'))
    return
  }
  if (!messageSendForm.value.content) {
    message.warning(t('layout.messageCenter.contentRequired'))
    return
  }

  try {
    const isSendToSelf =
      (!!selectedUserAccount.value && selectedUserAccount.value === currentUser.value.account)
      || (
        userStore.userInfo?.id != null
        && Number(messageSendForm.value.receiverUserId) === Number(userStore.userInfo.id)
      )

    const newId = await sendMessage({
      receiverTenantId: Number(sessionStorage.getItem('tenantId')),
      receiverUserId: Number(messageSendForm.value.receiverUserId),
      scope: 'INTERNAL',
      category: 'MESSAGE',
      title: messageSendForm.value.title,
      content: messageSendForm.value.content,
      linkUrl: messageSendForm.value.linkUrl,
    } as any)
    message.success(t('layout.messageCenter.sendSuccess'))
    await refreshMessageCenter(messageDrawerOpen.value)
    // 发给本人时立即弹出 Notification（SSE 可能因网关缓冲未即时到达；发给他人仅对方客户端展示）
    if (newId != null && isSendToSelf) {
      const selfMessage = {
        id: newId as number,
        category: 'MESSAGE',
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
    selectedUserAccount.value = ''
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
    message.warning(t('layout.messageCenter.selectUserWarning'))
    return
  }
  const selectedUser = userSelectList.value.find(u => String(u.id) === selectedUserIds.value[0])
  if (selectedUser) {
    messageSendForm.value.receiverUserId = selectedUser.id
    selectedUserName.value = `${selectedUser.username} (${selectedUser.account})`
    selectedUserAccount.value = selectedUser.account || ''
  }
  userSelectOpen.value = false
}

const { connect: connectMessageSse, close: closeMessageSse } = useSse<SysMessageVO>({
  url: '/api/sys/message/stream',
  onEvent: (name, data) => {
    if (name === 'permission-changed') {
      void refreshMenuPermissions(false)
      return
    }
    if (name !== 'message') return
    if ((data as any)?.category === 'SYSTEM_NOTICE_REFRESH') {
      window.dispatchEvent(new CustomEvent('fx:system-notice-refresh', { detail: data }))
      return
    }
    if (!data || !(data as any).id) return
    openMessageNotification(data as any)
    window.dispatchEvent(new CustomEvent('fx:message-received', { detail: data }))
  },
})

onMounted(async () => {
  void loadMenuFavorites()
  if (typeof window !== 'undefined') {
    document.addEventListener('mousedown', handleDocumentPointerDown)
    document.addEventListener('keydown', handleDocumentKeydown)
    window.addEventListener('scroll', handleScroll)
    window.addEventListener('fx:open-message-drawer', handleOpenMessageDrawerEvent)
    window.addEventListener('fx:open-global-search', handleOpenGlobalSearchEvent)
    window.addEventListener('fx:message-received', handleMessageReceivedEvent as EventListener)
    window.addEventListener('fx:system-notice-refresh', handleSystemNoticeRefreshEvent as EventListener)
  }
  try {
    if (isFallbackRoute.value) {
      await loadLayout()
      updateTabsByRoute(route.fullPath)
      await nextTick()
      bindCurrentPageScroll()
      return
    }

    await Promise.all([
      loadLayout(),
      loadGuidePreference(),
      loadTenantOptions(),
    ])
    updateTabsByRoute(route.fullPath)
    await nextTick()
    bindCurrentPageScroll()

    try {
      const config = await getSystemBasicConfig()
      if (config) {
        systemConfig.value = { ...config }
      }
    } catch (_) {}

    await refreshMessageCounts()
    await checkLicenseGracePeriod()

    try {
      const normalUnread = await listUnreadMessages(10, 'MESSAGE')
      ;[...(Array.isArray(normalUnread) ? normalUnread : [])]
        .forEach((m) => openMessageNotification(m as SysMessageVO))
    } catch (_) {}

    connectMessageSse()
  } finally {
    const bootstrapLoader = (window as any).__globalLoader
    const transitionLoader = (window as any).__fxLoginTransitionLoader
    if (bootstrapLoader && typeof bootstrapLoader.hide === 'function') {
      window.requestAnimationFrame(() => {
        bootstrapLoader.hide()
        transitionLoader?.hide?.()
      })
    } else {
      transitionLoader?.hide?.()
    }
  }
})

onUnmounted(() => {
  if (typeof window !== 'undefined') {
    document.removeEventListener('mousedown', handleDocumentPointerDown)
    document.removeEventListener('keydown', handleDocumentKeydown)
    window.removeEventListener('scroll', handleScroll)
    window.removeEventListener('fx:open-message-drawer', handleOpenMessageDrawerEvent)
    window.removeEventListener('fx:open-global-search', handleOpenGlobalSearchEvent)
    window.removeEventListener('fx:message-received', handleMessageReceivedEvent as EventListener)
    window.removeEventListener('fx:system-notice-refresh', handleSystemNoticeRefreshEvent as EventListener)
  }
  pageScrollEl?.removeEventListener('scroll', handlePageScroll)
  pageScrollEl = null
  closeMessageSse()
})
</script>

<style scoped lang="less" src="@/styles/layout/main-layout.less"></style>

