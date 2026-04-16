<template>
  <div class="personal-homepage-designer">
    <!-- Hero 鍖猴細鏍规嵁妯″紡鏄剧ず涓嶅悓鍐呭 -->
    <div class="designer-hero">
      <!-- 鏅€氭ā寮忥細鏄剧ず鐢ㄦ埛鎽樿淇℃伅 -->
      <div v-if="mode === 'current'" class="designer-hero__user">
        <div class="designer-hero__avatar">
          <a-avatar :size="64" :src="heroAvatarSrc || undefined">
            <template #icon>
              <UserOutlined />
            </template>
          </a-avatar>
        </div>
        <div class="designer-hero__info">
          <h2 class="designer-hero__greeting">
            {{ heroGreetingLine }}
          </h2>
          <p class="designer-hero__subtitle">
            <span>{{ heroDateSubtitle }}</span>
          </p>
          <div class="designer-hero__stats">
            <span class="designer-hero__stat">
              <ClockCircleOutlined />
              {{ $t('personalHomepage.summary.onlineDuration') }}：{{ summary?.onlineDurationText || '0分钟' }}
            </span>
          </div>
        </div>
      </div>
      <!-- 绠＄悊妯″紡锛氭樉绀烘爣棰樺拰鎻忚堪 -->
      <div v-else>
        <p class="designer-hero__eyebrow">{{ $t('personalHomepage.hero.eyebrow') }}</p>
        <h2 class="designer-hero__title">{{ resolvedTitle }}</h2>
        <p class="designer-hero__desc">{{ resolvedDescription }}</p>
      </div>
      <div class="designer-hero__meta">
        <span class="designer-badge">{{ $t('personalHomepage.hero.badge.default') }}</span>
        <span class="designer-badge designer-badge--soft">{{ scopeLabel }}</span>
      </div>
    </div>

    <!-- 宸ュ叿鏍?-->
    <div class="designer-toolbar">
      <a-space wrap>
        <a-select
          v-if="mode === 'manage' && showScopeSelector"
          v-model:value="scopeLevel"
          style="width: 160px"
        >
          <a-select-option value="TENANT">{{ $t('personalHomepage.hero.badge.tenant') }}</a-select-option>
          <a-select-option value="PUBLIC">{{ $t('personalHomepage.hero.badge.public') }}</a-select-option>
        </a-select>
        <a-button @click="toggleEditMode">
          <template #icon>
            <SettingOutlined />
          </template>
          {{ editMode ? $t('personalHomepage.toolbar.exitMode') : $t('personalHomepage.toolbar.editMode') }}
        </a-button>
        <a-button @click="reloadConfig">
          <template #icon>
            <ReloadOutlined />
          </template>
          {{ $t('personalHomepage.toolbar.refresh') }}
        </a-button>
        <!-- 浠呭湪缂栬緫鎬佹樉绀轰互涓嬫寜閽?-->
        <a-button v-if="editMode && mode === 'current'" @click="resetToDefault">
          <template #icon>
            <UndoOutlined />
          </template>
          {{ $t('personalHomepage.toolbar.resetDefault') }}
        </a-button>
        <a-button v-if="editMode" type="primary" :loading="saving" @click="saveConfig">
          <template #icon>
            <SaveOutlined />
          </template>
          {{ $t('personalHomepage.toolbar.saveLayout') }}
        </a-button>
      </a-space>
      <div class="designer-toolbar__hint">
        <span>{{ editMode ? $t('personalHomepage.toolbar.hint.edit') : $t('personalHomepage.toolbar.hint.view') }}</span>
      </div>
    </div>

    <div class="designer-content">
      <section class="designer-stage">
        <a-spin :spinning="loading">
          <div v-if="visibleWidgets.length === 0" class="designer-empty">
            <a-empty :description="$t('personalHomepage.empty')" />
          </div>
          <GridLayout
            v-else
            v-model:layout="gridLayout"
            class="designer-grid"
            :col-num="currentColNum"
            :row-height="currentRowHeight"
            :margin="gridMargin"
            :is-draggable="editMode"
            :is-resizable="editMode"
            :vertical-compact="true"
            :use-css-transforms="true"
            :is-bounded="true"
          >
            <GridItem
              v-for="item in gridLayout"
              :key="item.i"
              :x="item.x"
              :y="item.y"
              :w="item.w"
              :h="item.h"
              :i="item.i"
              :min-w="item.minW"
              :min-h="item.minH"
            >
              <article class="widget-card">
                <header class="widget-card__header">
                  <div class="widget-card__title-wrap">
                    <component :is="getWidgetMeta(item.i).icon" class="widget-card__icon" />
                    <div>
                      <h3 class="widget-card__title">{{ getWidgetTitle(item.i) }}</h3>
                      <p class="widget-card__subtitle">{{ getWidgetSubtitle(item.i) }}</p>
                    </div>
                  </div>
                  <div class="widget-card__actions">
                    <a-button
                      v-if="shouldShowMore(item.i)"
                      type="link"
                      size="small"
                      @click="openWidgetMore(item.i)"
                    >
                      {{ $t('personalHomepage.widget.more') }}
                    </a-button>
                    <span v-if="editMode" class="widget-card__drag">
                      <DragOutlined />
                    </span>
                  </div>
                </header>

                <div
                  class="widget-card__body"
                  :class="{ 'widget-card__body--scrollable': item.i === 'commonMenus' || item.i === 'myFavorites' }"
                >
                  <template v-if="item.i === 'commonMenus'">
                    <div v-if="commonMenus.length" class="menu-grid">
                      <button
                        v-for="menuItem in commonMenus"
                        :key="menuItem.path"
                        type="button"
                        class="menu-grid__item"
                        @click="openMenu(menuItem.path)"
                      >
                        <span
                          class="menu-grid__favorite-btn"
                          :title="isFavoriteMenu(menuItem.path) ? t('personalHomepage.components.myFavorites.remove') : t('personalHomepage.components.myFavorites.add')"
                          @click.stop.prevent="handleToggleFavorite(menuItem)"
                        >
                          <StarFilled v-if="isFavoriteMenu(menuItem.path)" />
                          <StarOutlined v-else />
                        </span>
                        <span class="menu-grid__icon-wrap">
                          <component :is="getMenuIcon(menuItem.icon)" class="menu-grid__icon" />
                        </span>
                        <span class="menu-grid__content">
                          <span class="menu-grid__title">{{ menuItem.title }}</span>
                          <span class="menu-grid__module">{{ menuItem.moduleName }}</span>
                        </span>
                      </button>
                    </div>
                    <a-empty v-else :description="getWidgetEmptyText('commonMenus')" />
                  </template>

                  <template v-else-if="item.i === 'myFavorites'">
                    <div v-if="favoriteMenus.length" class="menu-grid">
                      <button
                        v-for="menuItem in favoriteMenus"
                        :key="menuItem.path"
                        type="button"
                        class="menu-grid__item menu-grid__item--favorite"
                        @click="openMenu(menuItem.path)"
                      >
                        <span
                          class="menu-grid__favorite-btn menu-grid__favorite-btn--active"
                          :title="t('personalHomepage.components.myFavorites.remove')"
                          @click.stop.prevent="handleToggleFavorite(menuItem)"
                        >
                          <StarFilled />
                        </span>
                        <span class="menu-grid__icon-wrap">
                          <component :is="getMenuIcon(menuItem.icon)" class="menu-grid__icon" />
                        </span>
                        <span class="menu-grid__content">
                          <span class="menu-grid__title">{{ menuItem.title }}</span>
                          <span class="menu-grid__module">{{ menuItem.moduleName }}</span>
                        </span>
                      </button>
                    </div>
                    <a-empty v-else :description="getWidgetEmptyText('myFavorites')" />
                  </template>

                  <template v-else-if="item.i === 'pendingApprovals'">
                    <div v-if="pendingApprovals.length" class="list-block">
                      <button
                        v-for="record in pendingApprovals"
                        :key="record.id"
                        type="button"
                        class="list-block__item"
                        @click="openApproval(record)"
                      >
                        <span class="list-block__title">{{ record.taskName }}</span>
                        <span class="list-block__meta">{{ record.initiatorName || '-' }}</span>
                        <span class="list-block__time">{{ formatDateTime(record.startTime) }}</span>
                      </button>
                    </div>
                    <a-empty v-else :description="getWidgetEmptyText('pendingApprovals')" />
                  </template>

                  <template v-else-if="item.i === 'calendar'">
                    <div class="calendar-widget">
                      <div class="calendar-widget__month">
                        {{ calendarTitle }}
                      </div>
                      <div class="calendar-widget__weekdays">
                        <span v-for="weekday in weekDays" :key="weekday">{{ weekday }}</span>
                      </div>
                      <div class="calendar-widget__days">
                        <span
                          v-for="day in calendarDays"
                          :key="day.key"
                          :class="[
                            'calendar-widget__day',
                            { 'calendar-widget__day--muted': !day.currentMonth, 'calendar-widget__day--today': day.isToday },
                          ]"
                        >
                          {{ day.label }}
                        </span>
                      </div>
                    </div>
                  </template>

                  <template v-else-if="item.i === 'messages'">
                    <div v-if="inboxMessages.length" class="list-block">
                      <button
                        v-for="record in inboxMessages"
                        :key="record.id"
                        type="button"
                        class="list-block__item"
                        @click="openMessage(record)"
                      >
                        <span class="list-block__title">{{ record.title }}</span>
                        <span class="list-block__meta">{{ record.senderName || $t('personalHomepage.components.messages.systemSender') }}</span>
                        <span class="list-block__time">{{ record.createTime || '-' }}</span>
                      </button>
                    </div>
                    <a-empty v-else :description="getWidgetEmptyText('messages')" />
                  </template>

                  <template v-else-if="item.i === 'notices'">
                    <div v-if="noticeMessages.length" class="list-block">
                      <button
                        v-for="record in noticeMessages"
                        :key="record.id"
                        type="button"
                        class="list-block__item"
                        @click="openMessage(record)"
                      >
                        <span class="list-block__title">{{ record.title }}</span>
                        <span class="list-block__meta">{{ record.bizType || record.type || $t('personalHomepage.components.notices.systemType') }}</span>
                        <span class="list-block__time">{{ record.createTime || '-' }}</span>
                      </button>
                    </div>
                    <a-empty v-else :description="getWidgetEmptyText('notices')" />
                  </template>

                  <template v-else-if="item.i === 'currentTime'">
                    <div class="clock-widget">
                      <div class="clock-widget__time">{{ nowTime }}</div>
                      <div class="clock-widget__date">{{ nowDate }}</div>
                    </div>
                  </template>
                </div>
              </article>
            </GridItem>
          </GridLayout>
        </a-spin>
      </section>

      <!-- 閰嶇疆闈㈡澘锛氫粎鍦ㄧ紪杈戞€佹樉绀?-->
      <aside v-if="editMode" class="designer-panel">
        <div class="designer-panel__card">
          <div class="designer-panel__header">
            <h3>{{ $t('personalHomepage.panel.title') }}</h3>
            <span>{{ $t('personalHomepage.panel.subtitle') }}</span>
          </div>
          <div class="designer-panel__body">
            <div
              v-for="widget in orderedWidgets"
              :key="widget.key"
              class="widget-setting"
              :class="{ 'widget-setting--disabled': !widget.visible }"
            >
              <div class="widget-setting__top">
                <div class="widget-setting__title">
                  <component :is="getWidgetMeta(widget.key).icon" />
                  <span>{{ getWidgetTitle(widget.key) }}</span>
                </div>
                <a-switch
                  :checked="widget.visible"
                  :disabled="mode !== 'current' && scopeLevel === 'PUBLIC' && !editMode"
                  @change="updateWidgetVisibility(widget.key, $event)"
                />
              </div>
              <div class="widget-setting__fields">
                <label class="widget-setting__field">
                  <span>{{ $t('personalHomepage.widget.limit') }}</span>
                  <a-input-number
                    :value="toNumber(widget.params.limit, defaultLimit(widget.key))"
                    :min="0"
                    :max="getWidgetLimitMax(widget.key)"
                    :disabled="!widget.visible || isFixedLimitWidget(widget.key)"
                    @change="updateWidgetParam(widget.key, 'limit', $event)"
                  />
                </label>
                <label class="widget-setting__field widget-setting__field--switch">
                  <span>{{ $t('personalHomepage.widget.showMore') }}</span>
                  <a-switch
                    :checked="toBoolean(widget.params.showMore, defaultShowMore(widget.key))"
                    :disabled="!widget.visible || !hasMoreAction(widget.key) || isFixedMoreActionWidget(widget.key)"
                    @change="updateWidgetParam(widget.key, 'showMore', $event)"
                  />
                </label>
              </div>
            </div>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from 'vue'
import type { Component } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import {
  AppstoreOutlined,
  BellOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DragOutlined,
  MessageOutlined,
  ReloadOutlined,
  SaveOutlined,
  SettingOutlined,
  StarFilled,
  StarOutlined,
  UndoOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { GridItem, GridLayout } from 'vue-grid-layout-v3'
import { normalizeMediaUrl } from '@/utils/media'
import {
  createDefaultPersonalHomepageConfig,
  getCurrentPersonalHomepageConfig,
  getManagePersonalHomepageConfig,
  getPersonalHomepageSummary,
  getUserCommonMenus,
  getUserFavoriteMenus,
  mergePersonalHomepageConfig,
  resetCurrentPersonalHomepageConfig,
  saveCurrentPersonalHomepageConfig,
  saveManagePersonalHomepageConfig,
  toggleUserFavoriteMenu,
  type PersonalHomepageConfig,
  type PersonalMenuEntry,
  type PersonalHomepageScopeLevel,
  type PersonalHomepageSummaryVO,
} from '@/api/system/personalHomepage'
import { listUnreadMessages, markMessageRead, SYS_MESSAGE_DEFAULT_TYPE, type SysMessageVO } from '@/api/system/message'
import { pageMyPending, type WfExecutionDTO } from '@/api/workflow/execution'
import { FAVORITE_MANAGEMENT_PATH, PERSONAL_HOME_PATH } from '@/router'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import { useUserStore } from '@/stores/user'
import { getIcon } from '@/utils/icon'

const { t, locale } = useI18n()

interface PersonalHomepageDesignerProps {
  mode: 'current' | 'manage'
  title?: string
  description?: string
  initialScopeLevel?: Exclude<PersonalHomepageScopeLevel, 'USER'>
  showScopeSelector?: boolean
  initialEditMode?: boolean
}

interface GridLayoutItem {
  x: number
  y: number
  w: number
  h: number
  i: string
  minW: number
  minH: number
}

interface CalendarCell {
  key: string
  label: number
  currentMonth: boolean
  isToday: boolean
}

const props = withDefaults(defineProps<PersonalHomepageDesignerProps>(), {
  title: '',
  description: '',
  initialScopeLevel: 'TENANT',
  showScopeSelector: false,
  initialEditMode: false,
})

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const saving = ref(false)
const editMode = ref(props.initialEditMode)
const scopeLevel = ref<Exclude<PersonalHomepageScopeLevel, 'USER'>>(props.initialScopeLevel)
const config = ref<PersonalHomepageConfig>(createDefaultPersonalHomepageConfig())
const gridLayout = ref<GridLayoutItem[]>([])
const commonMenuItems = ref<PersonalMenuEntry[]>([])
const favoriteMenuItems = ref<PersonalMenuEntry[]>([])
const pendingApprovals = ref<WfExecutionDTO[]>([])
const unreadMessages = ref<SysMessageVO[]>([])
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const now = ref(dayjs())
const syncingGrid = ref(false)
const summary = ref<PersonalHomepageSummaryVO | null>(null)
let clockTimer: number | undefined
const MAX_COMMON_MENU_COUNT = 6

/**
 * 瑙ｆ瀽鐢ㄦ埛澶村儚鍦板潃锛屼笌 {@link MainLayout} 涓《閮ㄥご鍍忚鍒欎繚鎸佷竴鑷淬€?
 * <p>鐩稿璺緞浼氳ˉ鍏ㄤ负缃戝叧 `/api` 鍓嶇紑锛屼究浜?a-avatar 鐩存帴鍔犺浇銆?/p>
 *
 * @param raw 鍚庣鎴?Store 涓殑鍘熷璺緞
 * @returns 鍙姹傜殑瀹屾暣 URL锛涙棤鏁堟椂涓虹┖瀛楃涓?
 */
function resolveUserAvatarSrc(raw?: string | null): string {
  return normalizeMediaUrl(raw)
}

/**
 * 涓汉棣栭〉 Hero 鍖哄睍绀虹敤澶村儚锛堟憳瑕佹帴鍙ｄ紭鍏堬紝缂哄け鏃跺洖閫€鍒板綋鍓嶇櫥褰曠敤鎴蜂俊鎭級銆?
 */
const heroAvatarSrc = computed(() => {
  const raw = summary.value?.avatar || userStore.userInfo?.avatar
  return resolveUserAvatarSrc(raw)
})

/**
 * 闂€欒灞曠ず鍚嶏細鎽樿鏄电О浼樺厛锛屽叾娆′负 Store 涓殑鐢ㄦ埛鍚嶃€佽处鍙枫€?
 */
const displayNameForHero = computed(() => {
  return (
    summary.value?.nickname ||
    userStore.userInfo?.username ||
    userStore.userInfo?.account ||
    ''
  )
})

/** 闂€欐椂娈碉細鏃╂櫒 / 涓嬪崍 / 澶滈棿锛堜笌鍚庣鎽樿鏈嶅姟鏃舵鍒掑垎涓€鑷达級 */
type GreetingPhase = 'morning' | 'afternoon' | 'evening'

/**
 * 鏍规嵁褰撳墠鏈湴鏃堕棿璁＄畻闂€欐椂娈点€?
 *
 * @see now 鐢辨椂閽熷畾鏃跺埛鏂帮紝璺ㄦ椂娈典細鑷姩鏇存柊鏂囨
 */
const greetingPhase = computed<GreetingPhase>(() => {
  const hour = now.value.hour()
  if (hour >= 6 && hour < 12) {
    return 'morning'
  }
  if (hour >= 12 && hour < 18) {
    return 'afternoon'
  }
  return 'evening'
})

/**
 * 涓枃鍦烘櫙涓嬬殑绉拌皳鍚庣紑锛堝厛鐢?/ 濂冲＋锛夛紱鏈煡鎬у埆鏃朵负绌恒€?
 */
const honorificZh = computed(() => {
  const g = summary.value?.gender
  if (g === 1) {
    return t('personalHomepage.summary.greeting.honorificMale')
  }
  if (g === 2) {
    return t('personalHomepage.summary.greeting.honorificFemale')
  }
  return ''
})

/**
 * 鍥介檯鍖栦富闂€欒锛氬皧鏁殑濮撳悕绉拌皳 + 鍒嗘椂娈甸棶鍊欎笌缁撴潫璇紱鑻辨枃鍒?Mr./Ms./鏃犵О璋撲笁绉嶅彞寮忋€?
 */
const heroGreetingLine = computed(() => {
  const name = displayNameForHero.value
  const phase = greetingPhase.value
  const lead = t(`personalHomepage.summary.greeting.lead.${phase}`)
  const closing = t(`personalHomepage.summary.greeting.closing.${phase}`)
  if (locale.value === 'en-US') {
    const g = summary.value?.gender
    if (g === 1) {
      return t('personalHomepage.summary.greeting.lineEnMale', { name, lead, closing })
    }
    if (g === 2) {
      return t('personalHomepage.summary.greeting.lineEnFemale', { name, lead, closing })
    }
    return t('personalHomepage.summary.greeting.lineEnNeutral', { name, lead, closing })
  }
  return t('personalHomepage.summary.greeting.lineZh', {
    name,
    honorific: honorificZh.value,
    lead,
    closing,
  })
})

/**
 * 浠婃棩鏃ユ湡涓庢槦鏈熷壇鏍囬锛堜笌鐣岄潰璇█涓€鑷达級銆?
 */
const heroDateSubtitle = computed(() => {
  const d = now.value
  const idx = d.day()
  const weekday = t(`personalHomepage.summary.weekday.${idx}`)
  const month = d.month() + 1
  const day = d.date()
  if (locale.value === 'en-US') {
    return t('personalHomepage.summary.todayLineEn', { weekday, month, day })
  }
  return t('personalHomepage.summary.todayLineZh', { weekday, month, day })
})

const mode = computed(() => props.mode)
const showScopeSelector = computed(() => props.showScopeSelector)

const widgetMetaMap: Record<string, { icon: any }> = {
  commonMenus: { icon: AppstoreOutlined },
  myFavorites: { icon: StarOutlined },
  pendingApprovals: { icon: CheckCircleOutlined },
  calendar: { icon: CalendarOutlined },
  messages: { icon: MessageOutlined },
  notices: { icon: BellOutlined },
  currentTime: { icon: ClockCircleOutlined },
}

// 鍥介檯鍖栵細缁勪欢鏍囬
const widgetTitleMap: Record<string, string> = {
  commonMenus: 'personalHomepage.components.commonMenus.title',
  myFavorites: 'personalHomepage.components.myFavorites.title',
  pendingApprovals: 'personalHomepage.components.pendingApprovals.title',
  calendar: 'personalHomepage.components.calendar.title',
  messages: 'personalHomepage.components.messages.title',
  notices: 'personalHomepage.components.notices.title',
  currentTime: 'personalHomepage.components.currentTime.title',
}

// 鍥介檯鍖栵細缁勪欢鍓爣棰?
const widgetSubtitleMap: Record<string, string> = {
  commonMenus: 'personalHomepage.components.commonMenus.subtitle',
  myFavorites: 'personalHomepage.components.myFavorites.subtitle',
  pendingApprovals: 'personalHomepage.components.pendingApprovals.subtitle',
  calendar: 'personalHomepage.components.calendar.subtitle',
  messages: 'personalHomepage.components.messages.subtitle',
  notices: 'personalHomepage.components.notices.subtitle',
  currentTime: 'personalHomepage.components.currentTime.subtitle',
}

// 鍥介檯鍖栵細绌虹姸鎬佹枃妗?
const widgetEmptyMap: Record<string, string> = {
  commonMenus: 'personalHomepage.components.commonMenus.empty',
  myFavorites: 'personalHomepage.components.myFavorites.empty',
  pendingApprovals: 'personalHomepage.components.pendingApprovals.empty',
  messages: 'personalHomepage.components.messages.empty',
  notices: 'personalHomepage.components.notices.empty',
}

const resolvedTitle = computed(() => {
  if (props.title) {
    return props.title
  }
  return props.mode === 'manage' ? t('personalHomepage.hero.titleManage') : t('personalHomepage.hero.title')
})

const resolvedDescription = computed(() => {
  if (props.description) {
    return props.description
  }
  return props.mode === 'manage' ? t('personalHomepage.hero.descManage') : t('personalHomepage.hero.desc')
})

const scopeLabel = computed(() => {
  if (props.mode === 'current') {
    return t('personalHomepage.hero.badge.user')
  }
  return scopeLevel.value === 'PUBLIC' ? t('personalHomepage.hero.badge.public') : t('personalHomepage.hero.badge.tenant')
})

const orderedWidgets = computed(() => {
  return [...config.value.widgets].sort((left, right) => {
    const leftOrder = Number(left.orderNum ?? 0)
    const rightOrder = Number(right.orderNum ?? 0)
    if (leftOrder !== rightOrder) {
      return leftOrder - rightOrder
    }
    return left.key.localeCompare(right.key)
  })
})

const visibleWidgets = computed(() => orderedWidgets.value.filter(widget => widget.visible))

const currentColNum = computed(() => {
  if (viewportWidth.value < 768) {
    return config.value.layout.mobileColNum || 4
  }
  if (viewportWidth.value < 1280) {
    return config.value.layout.tabletColNum || 8
  }
  return config.value.layout.colNum || 12
})

const currentRowHeight = computed(() => config.value.layout.rowHeight || 72)

const gridMargin = computed<[number, number]>(() => [
  config.value.layout.marginX || 16,
  config.value.layout.marginY || 16,
])

const commonMenus = computed(() => {
  return commonMenuItems.value.slice(0, MAX_COMMON_MENU_COUNT)
})

const favoriteMenus = computed(() => {
  const limit = Math.min(
    Math.max(toNumber(findWidget('myFavorites')?.params.limit, defaultLimit('myFavorites')), 0),
    MAX_COMMON_MENU_COUNT,
  )
  return favoriteMenuItems.value.slice(0, limit)
})

const favoriteMenuPathSet = computed(() => {
  return new Set(favoriteMenuItems.value.map(item => String(item.path || '')))
})

const inboxMessages = computed(() => {
  const limit = toNumber(findWidget('messages')?.params.limit, defaultLimit('messages'))
  return unreadMessages.value.filter(item => !isNoticeMessage(item)).slice(0, Math.max(limit, 0))
})

const noticeMessages = computed(() => {
  const limit = toNumber(findWidget('notices')?.params.limit, defaultLimit('notices'))
  return unreadMessages.value.filter(isNoticeMessage).slice(0, Math.max(limit, 0))
})

function isNoticeMessage(messageItem: SysMessageVO) {
  const messageType = String(messageItem?.messageType || SYS_MESSAGE_DEFAULT_TYPE).toUpperCase()
  return ['NOTICE', 'WARNING', 'ALARM'].includes(messageType)
}

function formatIntlDate(value: Date, options: Intl.DateTimeFormatOptions) {
  const localeValue = String(locale.value || 'zh-CN')
  try {
    return new Intl.DateTimeFormat(localeValue, options).format(value)
  } catch (_) {
    return new Intl.DateTimeFormat('zh-CN', options).format(value)
  }
}

const nowTime = computed(() => now.value.format('HH:mm:ss'))
const nowDate = computed(() => {
  const current = now.value.toDate()
  return formatIntlDate(current, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long',
  })
})
const calendarTitle = computed(() => {
  return formatIntlDate(now.value.toDate(), {
    year: 'numeric',
    month: 'long',
  })
})
const weekDays = computed(() => {
  const monday = new Date(Date.UTC(2026, 0, 5))
  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday)
    date.setUTCDate(monday.getUTCDate() + index)
    return formatIntlDate(date, { weekday: 'short' })
  })
})

const calendarDays = computed<CalendarCell[]>(() => {
  const today = now.value
  const firstDay = today.startOf('month')
  const firstWeekday = (firstDay.day() + 6) % 7
  const startDay = firstDay.subtract(firstWeekday, 'day')
  return Array.from({ length: 42 }, (_, index) => {
    const current = startDay.add(index, 'day')
    return {
      key: current.format('YYYY-MM-DD'),
      label: current.date(),
      currentMonth: current.month() === today.month(),
      isToday: current.format('YYYY-MM-DD') === today.format('YYYY-MM-DD'),
    }
  })
})

function getWidgetMeta(widgetKey: string) {
  return widgetMetaMap[widgetKey] || widgetMetaMap.commonMenus
}

function getWidgetTitle(widgetKey: string) {
  const i18nKey = widgetTitleMap[widgetKey]
  return i18nKey ? t(i18nKey) : findWidget(widgetKey)?.title || widgetKey
}

function getWidgetSubtitle(widgetKey: string) {
  const i18nKey = widgetSubtitleMap[widgetKey]
  return i18nKey ? t(i18nKey) : ''
}

function getWidgetEmptyText(widgetKey: string) {
  const i18nKey = widgetEmptyMap[widgetKey]
  return i18nKey ? t(i18nKey) : ''
}

function findWidget(widgetKey: string) {
  return config.value.widgets.find(widget => widget.key === widgetKey)
}

function defaultLimit(widgetKey: string) {
  return toNumber(createDefaultPersonalHomepageConfig().widgets.find(item => item.key === widgetKey)?.params.limit, 0)
}

function getWidgetLimitMax(widgetKey: string) {
  return widgetKey === 'commonMenus' || widgetKey === 'myFavorites' ? MAX_COMMON_MENU_COUNT : 20
}

function isFixedLimitWidget(widgetKey: string) {
  return widgetKey === 'commonMenus'
}

function isFixedMoreActionWidget(widgetKey: string) {
  return widgetKey === 'myFavorites'
}

function getMenuIcon(iconName?: string): Component {
  return getIcon(iconName) || AppstoreOutlined
}

function isFavoriteMenu(path: string) {
  return favoriteMenuPathSet.value.has(String(path || ''))
}

function defaultShowMore(widgetKey: string) {
  return toBoolean(createDefaultPersonalHomepageConfig().widgets.find(item => item.key === widgetKey)?.params.showMore, false)
}

function toNumber(value: unknown, fallbackValue: number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallbackValue
}

function toBoolean(value: unknown, fallbackValue: boolean) {
  if (typeof value === 'boolean') {
    return value
  }
  if (value === 'true') {
    return true
  }
  if (value === 'false') {
    return false
  }
  return fallbackValue
}

function toggleEditMode() {
  editMode.value = !editMode.value
}

async function reloadConfig() {
  loading.value = true
  try {
    const remoteConfig = props.mode === 'current'
      ? await getCurrentPersonalHomepageConfig()
      : await getManagePersonalHomepageConfig(scopeLevel.value)
    config.value = mergePersonalHomepageConfig(remoteConfig)
    syncGridFromConfig()
    await loadWidgetData()
    // 鍔犺浇鎽樿淇℃伅
    if (props.mode === 'current') {
      await loadSummary()
    }
  } catch (error) {
    console.error('加载个人首页配置失败:', error)
    config.value = createDefaultPersonalHomepageConfig()
    syncGridFromConfig()
  } finally {
    loading.value = false
  }
}

async function loadSummary() {
  try {
    const data = await getPersonalHomepageSummary()
    summary.value = data
  } catch (error) {
    console.error('加载首页摘要信息失败:', error)
  }
}

async function saveConfig() {
  saving.value = true
  try {
    const payload = mergePersonalHomepageConfig(config.value)
    if (props.mode === 'current') {
      await saveCurrentPersonalHomepageConfig(payload)
    } else {
      await saveManagePersonalHomepageConfig(scopeLevel.value, payload)
    }
    config.value = payload
    syncGridFromConfig()
  } catch (error) {
    console.error('保存个人首页配置失败:', error)
  } finally {
    saving.value = false
  }
}

async function resetToDefault() {
  if (props.mode !== 'current') {
    return
  }
  saving.value = true
  try {
    await resetCurrentPersonalHomepageConfig()
    await reloadConfig()
  } catch (error) {
    console.error('恢复默认布局失败:', error)
  } finally {
    saving.value = false
  }
}

async function loadWidgetData() {
  await Promise.all([
    loadCommonMenus(),
    loadFavoriteMenus(),
    loadPendingApprovals(),
    loadUnreadMessages(),
  ])
}

async function loadCommonMenus() {
  if (props.mode !== 'current') {
    commonMenuItems.value = []
    return
  }
  try {
    const list = await getUserCommonMenus(MAX_COMMON_MENU_COUNT)
    commonMenuItems.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载常用菜单失败:', error)
    commonMenuItems.value = []
  }
}

async function loadFavoriteMenus() {
  if (props.mode !== 'current') {
    favoriteMenuItems.value = []
    return
  }
  try {
    const list = await getUserFavoriteMenus(MAX_COMMON_MENU_COUNT)
    favoriteMenuItems.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载收藏菜单失败:', error)
    favoriteMenuItems.value = []
  }
}

async function loadPendingApprovals() {
  const pendingWidget = findWidget('pendingApprovals')
  if (!pendingWidget?.visible) {
    pendingApprovals.value = []
    return
  }
  const limit = Math.max(toNumber(pendingWidget.params.limit, defaultLimit('pendingApprovals')), 0)
  try {
    const page = await pageMyPending({ pageNum: 1, pageSize: Math.max(limit, 1) })
    pendingApprovals.value = Array.isArray(page?.records) ? page.records : []
  } catch (error) {
    console.error('加载待审批列表失败:', error)
    pendingApprovals.value = []
  }
}

async function loadUnreadMessages() {
  const requestLimit = Math.max(
    10,
    toNumber(findWidget('messages')?.params.limit, defaultLimit('messages')),
    toNumber(findWidget('notices')?.params.limit, defaultLimit('notices')),
  )
  try {
    const list = await listUnreadMessages(requestLimit)
    unreadMessages.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载未读消息失败:', error)
    unreadMessages.value = []
  }
}

function syncGridFromConfig() {
  syncingGrid.value = true
  const colNum = Math.max(currentColNum.value, 1)
  gridLayout.value = visibleWidgets.value.map(widget => ({
    i: widget.key,
    x: Math.min(Math.max(widget.x, 0), Math.max(colNum - 1, 0)),
    y: Math.max(widget.y, 0),
    w: Math.min(Math.max(widget.w, widget.minW || 1), colNum),
    h: Math.max(widget.h, widget.minH || 1),
    minW: Math.max(widget.minW || 1, 1),
    minH: Math.max(widget.minH || 1, 1),
  }))
  nextTick(() => {
    syncingGrid.value = false
  })
}

function syncConfigFromGrid(layoutItems: GridLayoutItem[]) {
  const itemMap = new Map(layoutItems.map((item, index) => [item.i, { ...item, orderNum: (index + 1) * 10 }]))
  config.value.widgets = config.value.widgets.map(widget => {
    const item = itemMap.get(widget.key)
    if (!item) {
      return widget
    }
    return {
      ...widget,
      x: item.x,
      y: item.y,
      w: item.w,
      h: item.h,
      minW: item.minW,
      minH: item.minH,
      orderNum: item.orderNum,
    }
  })
}

function updateWidgetVisibility(widgetKey: string, checked: boolean) {
  config.value.widgets = config.value.widgets.map(widget => (
    widget.key === widgetKey
      ? { ...widget, visible: checked }
      : widget
  ))
  syncGridFromConfig()
}

function updateWidgetParam(widgetKey: string, field: string, value: unknown) {
  if (widgetKey === 'commonMenus' && field === 'limit') {
    return
  }
  if (widgetKey === 'myFavorites' && field === 'showMore') {
    return
  }
  config.value.widgets = config.value.widgets.map(widget => {
    if (widget.key !== widgetKey) {
      return widget
    }
    return {
      ...widget,
      params: {
        ...(widget.params || {}),
        [field]: value,
      },
    }
  })
}

function shouldShowMore(widgetKey: string) {
  const widget = findWidget(widgetKey)
  return !!widget?.visible
    && toBoolean(widget.params.showMore, defaultShowMore(widgetKey))
    && hasMoreAction(widgetKey)
}

function hasMoreAction(widgetKey: string) {
  return ['commonMenus', 'myFavorites', 'pendingApprovals', 'messages', 'notices'].includes(widgetKey)
}

function openWidgetMore(widgetKey: string) {
  if (widgetKey === 'commonMenus') {
    window.dispatchEvent(new CustomEvent('fx:open-global-search'))
    return
  }
  if (widgetKey === 'myFavorites') {
    router.push(FAVORITE_MANAGEMENT_PATH).catch(() => {})
    return
  }
  if (widgetKey === 'pendingApprovals') {
    router.push(approvalRoutePaths.myPending).catch(() => {})
    return
  }
  if (widgetKey === 'messages' || widgetKey === 'notices') {
    window.dispatchEvent(new CustomEvent('fx:open-message-drawer'))
  }
}

function openMenu(path: string) {
  if (!path || path === PERSONAL_HOME_PATH) {
    return
  }
  router.push(path).catch(() => {})
}

async function handleToggleFavorite(menuItem: PersonalMenuEntry) {
  const path = String(menuItem?.path || '')
  if (!path) {
    return
  }
  try {
    await toggleUserFavoriteMenu(path)
    await loadFavoriteMenus()
  } catch (error) {
    console.error('切换收藏菜单失败:', error)
  }
}

function openApproval(record: WfExecutionDTO) {
  router.push(approvalRoutePaths.myPending).catch(() => {})
}

async function openMessage(record: SysMessageVO) {
  try {
    await markMessageRead(record.id, { showSuccessMessage: false })
  } catch (error) {
    console.error('标记消息已读失败:', error)
  }
  unreadMessages.value = unreadMessages.value.filter(item => item.id !== record.id)
  if (record.linkUrl) {
    router.push(record.linkUrl).catch(() => {})
    return
  }
  window.dispatchEvent(new CustomEvent('fx:open-message-drawer'))
}

function formatDateTime(value?: string) {
  if (!value) {
    return '-'
  }
  return dayjs(value).format('MM-DD HH:mm')
}


function handleResize() {
  viewportWidth.value = window.innerWidth
}

function handleMessageEvent(event: Event) {
  const detail = (event as CustomEvent<SysMessageVO | undefined>).detail
  if (detail && detail.id) {
    unreadMessages.value = [detail, ...unreadMessages.value.filter(item => item.id !== detail.id)]
  }
  loadUnreadMessages()
  if (detail && (String(detail.bizType || '').toUpperCase().startsWith('WF_') || String(detail.linkUrl || '').includes('/workspace/approval/'))) {
    loadPendingApprovals()
  }
}

watch(scopeLevel, () => {
  if (props.mode === 'manage') {
    reloadConfig()
  }
})

watch(
  () => currentColNum.value,
  () => {
    syncGridFromConfig()
  },
)

watch(
  () => gridLayout.value,
  value => {
    if (syncingGrid.value) {
      return
    }
    syncConfigFromGrid(value)
  },
  { deep: true },
)

watch(
  () => [
    findWidget('pendingApprovals')?.params.limit,
    findWidget('messages')?.params.limit,
    findWidget('notices')?.params.limit,
  ],
  () => {
    loadWidgetData()
  },
)

onMounted(() => {
  reloadConfig()
  clockTimer = window.setInterval(() => {
    now.value = dayjs()
  }, 1000)
  window.addEventListener('resize', handleResize)
  window.addEventListener('fx:message-received', handleMessageEvent as EventListener)
})

onUnmounted(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('fx:message-received', handleMessageEvent as EventListener)
})
</script>

<style scoped lang="less">
.personal-homepage-designer {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 100%;
  color: var(--fx-text-primary, #111827);
}

.designer-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 28px;
  border-radius: 24px;
  border: 1px solid color-mix(in srgb, var(--fx-primary, #1677ff) 14%, var(--fx-border-color, #d9d9d9));
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--fx-primary, #1677ff) 16%, transparent), transparent 40%),
    linear-gradient(135deg, var(--fx-bg-container, #ffffff), var(--fx-bg-elevated, #f8fafc));
  box-shadow: var(--fx-shadow-secondary, 0 12px 32px rgba(15, 23, 42, 0.08));
}

.designer-hero__user {
  display: flex;
  align-items: center;
  gap: 20px;
}

.designer-hero__avatar {
  flex-shrink: 0;
}

.designer-hero__info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.designer-hero__greeting {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  line-height: 1.2;
}

.designer-hero__subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--fx-text-secondary, #6b7280);
  display: flex;
  align-items: center;
  gap: 8px;
}

.designer-hero__stats {
  display: flex;
  gap: 16px;
  margin-top: 8px;
}

.designer-hero__stat {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: var(--fx-text-secondary, #6b7280);
}

.designer-hero__eyebrow {
  margin: 0 0 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--fx-primary, #1677ff);
}

.designer-hero__title {
  margin: 0;
  font-size: 28px;
  line-height: 1.1;
}

.designer-hero__desc {
  max-width: 720px;
  margin: 12px 0 0;
  color: var(--fx-text-secondary, #6b7280);
}

.designer-hero__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.designer-badge {
  padding: 8px 12px;
  border-radius: 999px;
  background: var(--fx-primary, #1677ff);
  color: #fff;
  font-size: 12px;
  font-weight: 600;
}

.designer-badge--soft {
  background: var(--fx-primary-bg, rgba(22, 119, 255, 0.12));
  color: var(--fx-primary, #1677ff);
}

.designer-toolbar,
.designer-panel__card {
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 20px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 32px rgba(15, 23, 42, 0.08));
}

.designer-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 20px;
}

.designer-toolbar__hint {
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.designer-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
  min-height: 0;
}

.designer-content:has(.designer-panel) {
  grid-template-columns: minmax(0, 1fr) 320px;
}

.designer-stage {
  min-width: 0;
  padding: 18px;
  border-radius: 24px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--fx-bg-layout, #f8fafc) 88%, transparent), var(--fx-bg-layout, #f8fafc)),
    linear-gradient(135deg, color-mix(in srgb, var(--fx-primary, #1677ff) 6%, transparent), transparent 55%);
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #d9d9d9) 80%, transparent);
}

.designer-grid {
  min-height: 720px;
}

.designer-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 420px;
  border: 1px dashed var(--fx-border-color, #d1d5db);
  border-radius: 20px;
  background: var(--fx-bg-container, #ffffff);
}

.widget-card {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 18px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 10px 24px rgba(15, 23, 42, 0.08));
  overflow: hidden;
}

.widget-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px 12px;
  border-bottom: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 85%, transparent);
}

.widget-card__title-wrap {
  display: flex;
  gap: 10px;
  min-width: 0;
}

.widget-card__icon {
  margin-top: 2px;
  font-size: 18px;
  color: var(--fx-primary, #1677ff);
}

.widget-card__title {
  margin: 0;
  font-size: 16px;
}

.widget-card__subtitle {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.widget-card__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.widget-card__actions .ant-btn-link {
  color: var(--fx-primary, #1677ff);
  transition: color 0.3s ease;
}

.widget-card__actions .ant-btn-link:hover {
  color: var(--fx-primary-hover, #4096ff);
}

.widget-card__actions .ant-btn-link:active {
  color: var(--fx-primary-active, #0958d9);
}

.widget-card__drag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: var(--fx-bg-elevated, #f3f4f6);
  color: var(--fx-text-secondary, #6b7280);
}

.widget-card__body {
  flex: 1;
  min-height: 0;
  padding: 16px 18px 18px;
}

.widget-card__body--scrollable {
  overflow-y: auto;
}

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  align-content: start;
}

.menu-grid__item,
.list-block__item {
  width: 100%;
  border: none;
  text-align: left;
  cursor: pointer;
}

.menu-grid__item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  min-height: 72px;
  padding: 12px 14px;
  border-radius: 18px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #f8fafc), var(--fx-bg-container, #ffffff));
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 82%, transparent);
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.3s ease, background 0.3s ease;
}

.menu-grid__item:hover,
.list-block__item:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 24px color-mix(in srgb, var(--fx-primary, #1677ff) 12%, rgba(0, 0, 0, 0.08));
  border-color: color-mix(in srgb, var(--fx-primary, #1677ff) 36%, var(--fx-border-color, #e5e7eb));
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--fx-bg-elevated, #f8fafc) 88%, var(--fx-primary-bg, #eff6ff)),
    var(--fx-bg-container, #ffffff)
  );
}

.menu-grid__favorite-btn {
  position: absolute;
  top: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 999px;
  color: var(--fx-text-tertiary, #9ca3af);
  background: color-mix(in srgb, var(--fx-bg-container, #ffffff) 88%, transparent);
  transition: color 0.2s ease, background 0.2s ease, transform 0.2s ease;

  &:hover {
    color: var(--fx-theme-color, #1677ff);
    background: color-mix(in srgb, var(--fx-primary-bg, #eff6ff) 82%, transparent);
    transform: scale(1.05);
  }
}

.menu-grid__favorite-btn--active {
  color: var(--fx-theme-color, #1677ff);
  background: color-mix(in srgb, var(--fx-primary-bg, #eff6ff) 82%, transparent);
}

.menu-grid__item--favorite {
  border-color: color-mix(in srgb, var(--fx-primary, #1677ff) 24%, var(--fx-border-color, #e5e7eb));
}

.menu-grid__icon-wrap {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  color: var(--fx-primary, #1677ff);
  background: color-mix(in srgb, var(--fx-primary-bg, #eff6ff) 72%, var(--fx-bg-elevated, #f8fafc));
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent);
}

.menu-grid__icon {
  font-size: 18px;
}

.menu-grid__content {
  display: flex;
  flex: 1;
  min-width: 0;
  flex-direction: column;
  gap: 4px;
}

.menu-grid__title,
.list-block__title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  color: var(--fx-text-primary, #111827);
}

.menu-grid__module,
.list-block__meta,
.list-block__time {
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.menu-grid__module {
  display: inline-flex;
  align-items: center;
  align-self: flex-start;
  max-width: 100%;
  padding: 2px 10px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--fx-fill-secondary, #f3f4f6) 88%, transparent);
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 72%, transparent);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.list-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.list-block__item {
  display: grid;
  gap: 4px;
  padding: 12px 14px;
  border-radius: 14px;
  background: var(--fx-bg-elevated, #f8fafc);
  border: 1px solid color-mix(in srgb, var(--fx-primary, #1677ff) 15%, var(--fx-border-color, #e5e7eb));
  transition: transform 0.2s ease, box-shadow 0.2s ease, border-color 0.3s ease, background 0.3s ease;
}

.calendar-widget {
  display: flex;
  flex-direction: column;
  gap: 12px;
  height: 100%;
}

.calendar-widget__month {
  font-size: 18px;
  font-weight: 700;
}

.calendar-widget__weekdays,
.calendar-widget__days {
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  gap: 8px;
}

.calendar-widget__weekdays {
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.calendar-widget__weekdays span {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: center;
}

.calendar-widget__day {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 38px;
  border-radius: 12px;
  background: var(--fx-bg-elevated, #f8fafc);
}

.calendar-widget__day--muted {
  color: var(--fx-text-tertiary, #9ca3af);
}

.calendar-widget__day--today {
  background: var(--fx-primary, #1677ff);
  color: #fff;
  font-weight: 700;
}

.clock-widget {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  height: 100%;
  padding: 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, var(--fx-primary, #1677ff), color-mix(in srgb, var(--fx-primary, #1677ff) 68%, #0ea5e9));
  color: #fff;
}

.clock-widget__time {
  font-size: 38px;
  font-weight: 700;
  line-height: 1;
}

.clock-widget__date {
  font-size: 14px;
  opacity: 0.92;
}

.designer-panel__card {
  display: flex;
  flex-direction: column;
  min-height: 100%;
}

.designer-panel__header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid var(--fx-border-color, #e5e7eb);
}

.designer-panel__header h3 {
  margin: 0;
  font-size: 16px;
}

.designer-panel__header span {
  display: block;
  margin-top: 6px;
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.designer-panel__body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
}

.widget-setting {
  padding: 14px 14px 12px;
  border-radius: 16px;
  background: var(--fx-bg-elevated, #f8fafc);
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 80%, transparent);
}

.widget-setting--disabled {
  opacity: 0.72;
}

.widget-setting__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.widget-setting__title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.widget-setting__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 14px;
}

.widget-setting__field {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
}

.widget-setting__field--switch {
  justify-content: space-between;
}

@media (max-width: 1280px) {
  .designer-content {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .designer-hero,
  .designer-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .menu-grid {
    grid-template-columns: 1fr;
  }

  .widget-setting__fields {
    grid-template-columns: 1fr;
  }
}
</style>
