<template>
  <a-layout-header class="app-header">
    <!-- 左侧：Logo + 系统名称 -->
    <div class="app-header-left">
      <div class="app-logo">
        <img v-if="logo" :src="logo" alt="Logo" class="logo-image" @error="onLogoError" />
        <AppstoreOutlined v-else class="logo-icon" />
      </div>
      <div class="app-title">{{ title || 'Forgex MOM' }}</div>
    </div>

    <!-- 中间：模块导航（仅混合布局模式显示） -->
    <div
      v-if="showModuleNav"
      class="app-header-middle"
    >
      <a-menu
        mode="horizontal"
        :selected-keys="props.activeModuleCode ? [props.activeModuleCode] : []"
        class="module-menu"
        @click="onModuleClick"
      >
        <a-menu-item
          v-for="mod in modules"
          :key="mod.code"
          class="module-menu-item"
        >
          <template #icon>
            <component v-if="mod.icon" :is="getIcon(mod.icon)" />
            <AppstoreOutlined v-else />
          </template>
          <span>{{ mod.name }}</span>
        </a-menu-item>
      </a-menu>
    </div>

    <!-- 右侧：搜索 + 工具按钮 + 用户菜单 -->
    <div class="app-header-right">
      <a-space :size="12">
        <!-- 全局搜索按钮 -->
        <a-button
          v-if="showSearch"
          type="text"
          class="header-btn"
          @click="onSearchClick"
        >
          <template #icon>
            <SearchOutlined />
          </template>
          <span class="search-text">搜索</span>
          <span class="search-shortcut">Ctrl+K</span>
        </a-button>

        <!-- 消息通知 -->
        <a-badge :count="unreadCount" :overflow-count="99">
          <a-button
            type="text"
            class="header-btn"
            @click="onMessageClick"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </a-button>
        </a-badge>

        <!-- 语言切换 -->
        <a-select
          v-if="showLangSwitch"
          v-model:value="currentLocale"
          size="small"
          class="lang-select"
          @change="onLocaleChange"
          :loading="languageList.length === 0"
        >
          <a-select-option
            v-for="lang in languageList"
            :key="lang.langCode"
            :value="lang.langCode"
          >
            <span v-if="lang.icon">{{ lang.icon }} </span>{{ lang.langName }}
          </a-select-option>
        </a-select>

        <!-- 刷新按钮 -->
        <a-button
          v-if="showRefresh"
          type="text"
          class="header-btn"
          @click="onRefresh"
        >
          <template #icon>
            <SyncOutlined />
          </template>
        </a-button>

        <!-- 布局设置按钮 -->
        <a-button
          type="text"
          class="header-btn"
          @click="onSettingsClick"
        >
          <template #icon>
            <SettingOutlined />
          </template>
        </a-button>

        <!-- 用户下拉菜单 -->
        <a-dropdown placement="bottomRight">
          <div class="user-dropdown-trigger">
            <a-avatar
              v-if="user.avatar"
              :src="user.avatar"
              :size="32"
              class="user-avatar"
            />
            <a-avatar
              v-else
              :size="32"
              class="user-avatar user-avatar-default"
            >
              {{ userInitial }}
            </a-avatar>
            <span class="user-name">{{ user.name || user.account || '未登录' }}</span>
            <DownOutlined class="user-dropdown-icon" />
          </div>
          <template #overlay>
            <a-menu @click="onUserMenuClick">
              <a-menu-item key="profile">
                <UserOutlined />
                <span>个人信息</span>
              </a-menu-item>
              <a-menu-item key="password">
                <KeyOutlined />
                <span>修改密码</span>
              </a-menu-item>
              <a-menu-item key="messageSend">
                <MailOutlined />
                <span>发送消息</span>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout">
                <LogoutOutlined />
                <span>退出登录</span>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </a-space>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { getIcon } from '../../utils/icon'
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
  SyncOutlined
} from '@ant-design/icons-vue'
import { getUnreadMessageCount } from '../../api/message'
import { listEnabledLanguages, type LanguageType } from '../../api/system/i18n'
import type { LocaleCode } from '../../locales'

interface Module {
  code: string
  name: string
  icon?: string
  order: number
}

interface User {
  account: string
  name?: string
  avatar?: string
}

interface AppHeaderProps {
  logo?: string
  title?: string
  modules?: Module[]
  activeModuleCode?: string
  layoutMode?: 'vertical' | 'vertical-mix' | 'top' | 'mix'
  showSearch?: boolean
  showLangSwitch?: boolean
  showRefresh?: boolean
  user: User
}

const props = withDefaults(defineProps<AppHeaderProps>(), {
  logo: '',
  title: 'Forgex MOM',
  modules: () => [],
  activeModuleCode: '',
  layoutMode: 'mix',
  showSearch: true,
  showLangSwitch: true,
  showRefresh: true,
  user: () => ({ account: '', name: '', avatar: '' })
})

const emit = defineEmits<{
  'module-click': [moduleCode: string]
  'search-click': []
  'settings-click': []
  'user-menu-click': [key: string]
  'locale-change': [locale: LocaleCode]
  'refresh': []
  'message-click': []
}>()

// 当前语言
const currentLocale = ref<LocaleCode>((localStorage.getItem('fx-locale') as LocaleCode) || 'zh-CN')

// 语言列表
const languageList = ref<LanguageType[]>([])

// 未读消息数量
const unreadCount = ref(0)

// 定时器
let unreadCountTimer: any = null

// 加载语言列表
const loadLanguageList = async () => {
  try {
    const languages = await listEnabledLanguages()
    languageList.value = languages || []
  } catch (error) {
    console.error('Failed to load language list:', error)
  }
}

// 加载未读消息数量
const loadUnreadCount = async () => {
  try {
    const count = await getUnreadMessageCount()
    unreadCount.value = count || 0
  } catch (error) {
    console.error('Failed to load unread message count:', error)
  }
}

// 组件挂载时加载语言列表和未读消息数量
onMounted(() => {
  loadLanguageList()
  loadUnreadCount()
  
  // 每30秒刷新一次未读消息数量
  unreadCountTimer = setInterval(() => {
    loadUnreadCount()
  }, 30000)
})

// 组件卸载时清除定时器
onUnmounted(() => {
  if (unreadCountTimer) {
    clearInterval(unreadCountTimer)
  }
})

// 是否显示模块导航
const showModuleNav = computed(() => {
  return (props.layoutMode === 'mix' || props.layoutMode === 'top') && props.modules.length > 0
})

// 用户名首字母
const userInitial = computed(() => {
  const name = props.user.name || props.user.account || ''
  if (!name) return 'U'
  return name.charAt(0).toUpperCase()
})

// Logo 加载失败处理
const onLogoError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.style.display = 'none'
}

// 模块点击
const onModuleClick = (info: any) => {
  const key = info.key as string
  if (key) {
    emit('module-click', key)
  }
}

// 搜索点击
const onSearchClick = () => {
  emit('search-click')
}

// 设置点击
const onSettingsClick = () => {
  emit('settings-click')
}

// 用户菜单点击
const onUserMenuClick = (info: any) => {
  const key = info.key as string
  if (key) {
    emit('user-menu-click', key)
  }
}

// 语言切换
const onLocaleChange = (locale: string) => {
  emit('locale-change', locale)
}

// 刷新
const onRefresh = () => {
  emit('refresh')
}

// 消息点击
const onMessageClick = () => {
  emit('message-click')
}
</script>

<style scoped lang="less">
.app-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 16px;
  background: var(--fx-header-bg, #ffffff);
  // 移除边框，由 main-layout.less 中的 .fx-header 统一管理
  // border-bottom: 1px solid var(--fx-border-color, #f0f0f0);
  // box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.app-header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-logo {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
}

.logo-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.logo-icon {
  font-size: 24px;
  color: var(--fx-theme-color, #1677ff);
}

.app-title {
  font-size: 18px;
  font-weight: 600;
  color: var(--fx-header-color, #1f2937);
  white-space: nowrap;
}

.app-header-middle {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  margin: 0 24px;
  overflow: hidden;
}

.module-menu {
  border-bottom: none;
  background: transparent;
  width: 100%;
  min-width: 0;
  
  :deep(.ant-menu-item) {
    border-bottom: none;
    font-size: var(--fx-font-size, 14px);
    min-width: auto;
    max-width: none;
    overflow: visible;
    
    &:hover {
      color: var(--fx-theme-color, #1677ff);
    }
    
    &.ant-menu-item-selected {
      color: var(--fx-theme-color, #1677ff);
      border-bottom: none;
    }
    
    .ant-menu-title-content {
      overflow: visible;
      text-overflow: clip;
      white-space: nowrap;
    }
  }
}

.module-menu-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.module-icon {
  font-size: calc(var(--fx-font-size, 14px) * 1.15);
}

.app-header-right {
  display: flex;
  align-items: center;
}

.header-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--fx-header-color, #4b5563);
  background: transparent;
  
  &:hover {
    color: var(--fx-theme-color, #1677ff);
    background: var(--fx-header-btn-hover-bg, rgba(22, 119, 255, 0.08));
  }
}

.search-text {
  font-size: var(--fx-font-size, 14px);
}

.search-shortcut {
  padding: 2px 6px;
  font-size: calc(var(--fx-font-size, 14px) * 0.85);
  color: var(--fx-header-shortcut-color, #9ca3af);
  background: var(--fx-header-shortcut-bg, #f3f4f6);
  border-radius: 4px;
}

.lang-select {
  width: 120px;
}

.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: background 0.2s;
  
  &:hover {
    background: rgba(0, 0, 0, 0.04);
  }
}

.user-avatar {
  flex-shrink: 0;
}

.user-avatar-default {
  background: linear-gradient(135deg, var(--fx-theme-color, #1677ff), #4b5563);
  color: #ffffff;
  font-weight: 600;
}

.user-name {
  font-size: var(--fx-font-size, 14px);
  color: var(--fx-header-color, #1f2937);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.user-dropdown-icon {
  font-size: calc(var(--fx-font-size, 14px) * 0.85);
  color: #9ca3af;
}

// 响应式适配
@media (max-width: 768px) {
  .app-header {
    padding: 0 12px;
  }
  
  .app-title {
    font-size: 16px;
  }
  
  .search-text,
  .search-shortcut {
    display: none;
  }
  
  .user-name {
    display: none;
  }
  
  .app-header-middle {
    display: none;
  }
}
</style>
