<template>
  <a-layout-header class="app-header fx-guide-header">
    <!-- 左侧：Logo + 系统名称 -->
    <div class="app-header-left fx-guide-header-brand">
      <div class="app-logo">
        <img v-if="logo" :src="logo" alt="Logo" class="logo-image" @error="onLogoError" />
        <AppstoreOutlined v-else class="logo-icon" />
      </div>
      <div class="app-title">{{ title || 'Forgex MOM' }}</div>
    </div>

    <!-- 中间：模块导航（仅混合布局模式显示） -->
    <div
      v-if="showModuleNav"
      class="app-header-middle fx-guide-module-nav"
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
            <FxIcon v-if="mod.icon" :name="mod.icon" />
            <AppstoreOutlined v-else />
          </template>
          <span>{{ mod.name }}</span>
        </a-menu-item>
      </a-menu>
    </div>

    <!-- 右侧：搜索 + 工具按钮 + 用户菜单 -->
    <div class="app-header-right fx-guide-header-actions">
      <a-space :size="12">
        <!-- 全局搜索按钮 -->
        <a-button
          v-if="showSearch"
          type="text"
          class="header-btn fx-guide-search-trigger"
          @click="onSearchClick"
        >
          <template #icon>
            <SearchOutlined />
          </template>
          <span class="search-text">{{ t('layout.search') }}</span>
          <span class="search-shortcut">Ctrl+K</span>
        </a-button>

        <!-- 消息通知 -->
        <a-badge :count="unreadCount" :overflow-count="99">
          <a-button
            type="text"
            class="header-btn fx-guide-message-trigger"
            @click="onMessageClick"
          >
            <template #icon>
              <BellOutlined />
            </template>
          </a-button>
        </a-badge>

        <!-- 语言切换 -->
        <a-dropdown v-if="showLangSwitch" placement="bottomRight" trigger="click">
          <a-button
            type="text"
            class="header-btn header-btn--icon"
            :title="currentLanguageLabel"
            :loading="languageList.length === 0"
          >
            <template #icon>
              <img
                :src="LANG_SWITCH_ICON_SRC"
                alt=""
                aria-hidden="true"
                class="lang-switch-icon"
              />
            </template>
          </a-button>
          <template #overlay>
            <a-menu :selected-keys="[currentLocale]" @click="onLanguageMenuClick">
              <a-menu-item
                v-for="lang in languageList"
                :key="lang.langCode"
              >
                <div class="lang-menu-item">
                  <span class="lang-menu-item__label">
                    <span v-if="lang.icon">{{ lang.icon }} </span>{{ getLanguageLabel(lang) }}
                  </span>
                  <CheckOutlined v-if="currentLocale === lang.langCode" class="lang-menu-item__check" />
                </div>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>

        <!-- 安卓版本按钮 -->
        <a-popover
          v-if="showRefresh"
          v-model:open="androidPopoverOpen"
          placement="bottomRight"
          trigger="click"
          @openChange="handleAndroidPopoverChange"
        >
          <template #content>
            <div class="android-version-popover">
              <div v-if="androidLoading" class="android-version-popover__loading">
                {{ t('layout.android.loading') }}
              </div>
              <div v-else-if="latestAndroidVersion" class="android-version-popover__content">
                <div class="android-version-popover__title">
                  Android {{ latestAndroidVersion.versionName }}
                </div>
                <div class="android-version-popover__meta">
                  {{ t('layout.android.versionCode', { code: latestAndroidVersion.versionCode }) }}
                </div>
                <div v-if="latestAndroidVersion.changelog" class="android-version-popover__changelog">
                  {{ latestAndroidVersion.changelog }}
                </div>
                <div v-if="qrCodeDataUrl" class="android-version-popover__qr">
                  <img :src="qrCodeDataUrl" alt="Android QR Code" />
                </div>
                <a
                  v-if="latestAndroidVersion.fileUrl"
                  :href="normalizeMediaUrl(latestAndroidVersion.fileUrl)"
                  target="_blank"
                  class="android-version-popover__link"
                >
                  {{ t('layout.android.downloadApk') }}
                </a>
              </div>
              <div v-else class="android-version-popover__empty">
                {{ t('layout.android.empty') }}
              </div>
            </div>
          </template>

          <a-button
            type="text"
            class="header-btn fx-guide-refresh-trigger"
          >
            <template #icon>
              <AndroidOutlined />
            </template>
          </a-button>
        </a-popover>

        <!-- 布局设置按钮 -->
        <a-button
          type="text"
          class="header-btn fx-guide-settings-trigger"
          @click="onSettingsClick"
        >
          <template #icon>
            <SettingOutlined />
          </template>
        </a-button>

        <!-- 租户切换按钮 -->
        <a-dropdown
          placement="bottomRight"
          trigger="click"
          :disabled="tenantLoading || tenantOptions.length === 0"
        >
          <a-button
            type="text"
            class="header-btn header-btn--icon tenant-switch-btn"
            :title="tenantSwitchTitle"
            :loading="tenantLoading"
          >
            <template #icon>
              <ApartmentOutlined />
            </template>
          </a-button>
          <template #overlay>
            <a-menu :selected-keys="currentTenantId ? [`tenant:${currentTenantId}`] : []" @click="onTenantMenuClick">
              <a-menu-item
                v-for="tenant in tenantOptions"
                :key="`tenant:${tenant.id}`"
                :disabled="tenant.id === currentTenantId || switchingTenantId === tenant.id"
              >
                <div class="tenant-menu-item">
                  <span class="tenant-menu-item__name">{{ tenant.name || tenant.id }}</span>
                  <CheckOutlined v-if="tenant.id === currentTenantId" class="tenant-menu-item__check" />
                </div>
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>

        <!-- 用户下拉菜单 -->
        <a-dropdown placement="bottomRight">
          <div class="user-dropdown-trigger fx-guide-user-menu">
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
            <span class="user-name">{{ user.name || user.account || t('layout.user.notLoggedIn') }}</span>
            <DownOutlined class="user-dropdown-icon" />
          </div>
          <template #overlay>
            <a-menu @click="onUserMenuClick">
              <a-menu-item key="profile">
                <UserOutlined />
                <span>{{ t('layout.user.profile') }}</span>
              </a-menu-item>
              <a-menu-item key="password">
                <KeyOutlined />
                <span>{{ t('layout.user.changePassword') }}</span>
              </a-menu-item>
              <a-menu-item key="guide">
                <InfoCircleOutlined />
                <span>{{ t('layout.user.guideSettings') }}</span>
              </a-menu-item>
              <a-menu-item key="messageSend">
                <MailOutlined />
                <span>{{ t('layout.user.sendMessage') }}</span>
              </a-menu-item>
              <a-menu-divider />
              <a-menu-item key="logout">
                <LogoutOutlined />
                <span>{{ t('layout.user.logout') }}</span>
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
import { useI18n } from 'vue-i18n'
import FxIcon from '@/components/common/FxIcon.vue'
import {
  SearchOutlined,
  BellOutlined,
  CheckOutlined,
  SettingOutlined,
  DownOutlined,
  UserOutlined,
  KeyOutlined,
  ApartmentOutlined,
  InfoCircleOutlined,
  MailOutlined,
  LogoutOutlined,
  AppstoreOutlined,
  AndroidOutlined
} from '@ant-design/icons-vue'
import { getUnreadMessageCount } from '../../api/message'
import { listEnabledLanguages, type LanguageType } from '../../api/system/i18n'
import { getLatestAndroidVersion, type AndroidVersionItem } from '../../api/system/androidVersion'
import type { LocaleCode } from '../../locales'
import { getLanguageDisplayName, LANG_SWITCH_ICON_SRC } from '@/utils/language'
import { normalizeMediaUrl } from '@/utils/media'
import QRCode from 'qrcode/lib/browser'

const { t } = useI18n()

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

interface TenantOption {
  id: string
  name: string
}

interface AppHeaderProps {
  /** Logo 图片 URL，为空时显示默认图标 */
  logo?: string
  /** 系统标题，默认为 'Forgex MOM' */
  title?: string
  /** 模块列表，用于顶部模块导航显示 */
  modules?: Module[]
  /** 当前激活的模块 code，用于高亮显示 */
  activeModuleCode?: string
  /** 布局模式：vertical=垂直，vertical-mix=混合，top=顶部，mix=混合 */
  layoutMode?: 'vertical' | 'vertical-mix' | 'top' | 'mix'
  /** 是否显示搜索按钮，默认 true */
  showSearch?: boolean
  /** 是否显示语言切换下拉框，默认 true */
  showLangSwitch?: boolean
  /** 是否显示刷新按钮，默认 true */
  showRefresh?: boolean
  /** 当前登录用户信息，包含头像、姓名、账号等 */
  user: User
  tenantOptions?: TenantOption[]
  currentTenantId?: string
  tenantLoading?: boolean
  switchingTenantId?: string
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
  user: () => ({ account: '', name: '', avatar: '' }),
  tenantOptions: () => [],
  currentTenantId: '',
  tenantLoading: false,
  switchingTenantId: ''
})

const emit = defineEmits<{
  /**
   * 模块点击事件
   * 触发时机：用户点击模块导航时触发
   * @param moduleCode 被点击的模块 code
   */
  'module-click': [moduleCode: string]
  /**
   * 搜索按钮点击事件
   * 触发时机：用户点击搜索按钮时触发
   */
  'search-click': []
  /**
   * 设置按钮点击事件
   * 触发时机：用户点击设置按钮时触发
   */
  'settings-click': []
  /**
   * 用户菜单点击事件
   * 触发时机：用户点击用户下拉菜单项时触发
   * @param key 菜单项 key，如 profile、password、logout
   */
  'user-menu-click': [key: string]
  'tenant-change': [tenantId: string]
  /**
   * 语言切换事件
   * 触发时机：用户切换语言时触发
   * @param locale 新的语言代码
   */
  'locale-change': [locale: LocaleCode]
  /**
   * 刷新事件
   * 触发时机：用户点击刷新按钮时触发
   */
  'refresh': []
  /**
   * 消息按钮点击事件
   * 触发时机：用户点击消息通知按钮时触发
   */
  'message-click': []
}>()

// 当前语言
const currentLocale = ref<LocaleCode>((localStorage.getItem('fx-locale') as LocaleCode) || 'zh-CN')

// 语言列表
const languageList = ref<LanguageType[]>([])
const androidPopoverOpen = ref(false)
const androidLoading = ref(false)
const latestAndroidVersion = ref<AndroidVersionItem | null>(null)
const qrCodeDataUrl = ref('')

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

const loadLatestAndroidVersion = async () => {
  androidLoading.value = true
  try {
    const version = await getLatestAndroidVersion()
    latestAndroidVersion.value = version
    qrCodeDataUrl.value = version?.fileUrl
      ? await QRCode.toDataURL(normalizeMediaUrl(version.fileUrl), { width: 180, margin: 1 })
      : ''
  } catch (error) {
    latestAndroidVersion.value = null
    qrCodeDataUrl.value = ''
    console.error('Failed to load latest android version:', error)
  } finally {
    androidLoading.value = false
  }
}

// 组件挂载时加载语言列表和未读消息数量
onMounted(() => {
  loadLanguageList()
  loadUnreadCount()

  if (typeof window !== 'undefined') {
    window.addEventListener('fx:message-refresh', loadUnreadCount as EventListener)
  }
  
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
  if (typeof window !== 'undefined') {
    window.removeEventListener('fx:message-refresh', loadUnreadCount as EventListener)
  }
})

// 是否显示模块导航
const showModuleNav = computed(() => {
  return (props.layoutMode === 'mix' || props.layoutMode === 'top') && props.modules.length > 0
})

const currentLanguageLabel = computed(() => {
  return getLanguageDisplayName(languageList.value.find((lang) => lang.langCode === currentLocale.value))
})

const tenantSwitchTitle = computed(() => {
  if (props.tenantLoading) {
    return t('layout.tenant.loading')
  }
  if (props.tenantOptions.length === 0) {
    return t('layout.tenant.empty')
  }
  return t('layout.tenant.switch')
})

function getLanguageLabel(language: LanguageType): string {
  return getLanguageDisplayName(language)
}

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
    if (key.startsWith('tenant:')) {
      emit('tenant-change', key.slice('tenant:'.length))
      return
    }
    emit('user-menu-click', key)
  }
}

const onTenantMenuClick = (info: any) => {
  const key = String(info?.key || '')
  if (key.startsWith('tenant:')) {
    emit('tenant-change', key.slice('tenant:'.length))
  }
}

// 语言切换
const onLocaleChange = (locale: LocaleCode) => {
  currentLocale.value = locale
  emit('locale-change', locale)
}

const onLanguageMenuClick = (info: any) => {
  const locale = String(info?.key || '') as LocaleCode
  if (!locale || locale === currentLocale.value) {
    return
  }
  onLocaleChange(locale)
}

const handleAndroidPopoverChange = (open: boolean) => {
  androidPopoverOpen.value = open
  if (open) {
    loadLatestAndroidVersion()
  }
}

// 消息点击
const onMessageClick = () => {
  emit('message-click')
}
</script>

<style scoped lang="less" src="@/styles/layout/components/app-header.less"></style>
