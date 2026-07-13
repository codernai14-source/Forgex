<template>
  <div class="personal-homepage-designer">
    <!-- Hero 閸栫尨绱伴弽瑙勫祦濡€崇础閺勫墽銇氭稉宥呮倱閸愬懎顔?-->
    <div class="designer-hero">
      <!-- 闁哄拋鍣ｉ埀顒佺鑶╃€殿喖楠忕槐浼村及閸撗佷粵闁活潿鍔嶉崺娑㈠箺濡娲ｅǎ鍥ｅ墲娴?-->
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
              {{ $t('personalHomepage.summary.onlineDuration') }}: {{ summary?.onlineDurationText || t('personalHomepage.summary.zeroMinutes') }}
            </span>
          </div>
        </div>
      </div>
      <!-- 缁狅紕鎮婂Ο鈥崇础閿涙碍妯夌粈鐑樼垼妫版ê鎷伴幓蹇氬牚 -->
      <div v-else>
        <p class="designer-hero__eyebrow">{{ $t('personalHomepage.hero.eyebrow') }}</p>
        <h2 class="designer-hero__title">{{ resolvedTitle }}</h2>
        <p class="designer-hero__desc">{{ resolvedDescription }}</p>
      </div>
      <div class="designer-hero__actions">
        <a-select
          v-if="mode === 'manage' && showScopeSelector"
          v-model:value="scopeLevel"
          class="designer-hero__scope"
        >
          <a-select-option value="TENANT">{{ $t('personalHomepage.hero.badge.tenant') }}</a-select-option>
          <a-select-option value="PUBLIC">{{ $t('personalHomepage.hero.badge.public') }}</a-select-option>
        </a-select>
        <a-space wrap>
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
          <a-button v-if="editMode" :loading="sharing" @click="createShareCode">
            <template #icon>
              <ShareAltOutlined />
            </template>
            {{ $t('personalHomepage.share.create') }}
          </a-button>
          <a-button v-if="editMode" @click="openImportLayout">
            <template #icon>
              <ImportOutlined />
            </template>
            {{ $t('personalHomepage.share.import') }}
          </a-button>
          <a-button v-if="editMode && mode === 'current'" @click="resetToDefault">
            <template #icon>
              <UndoOutlined />
            </template>
            {{ $t('personalHomepage.toolbar.resetDefault') }}
          </a-button>
          <a-button v-if="editMode && mode === 'current'" @click="openComponentLibrary">
            <template #icon>
              <AppstoreOutlined />
            </template>
            {{ $t('personalHomepage.toolbar.componentLibrary') }}
          </a-button>
          <a-button v-if="editMode && mode === 'current'" @click="openPersonalComponentConfig">
            <template #icon>
              <SettingOutlined />
            </template>
            {{ $t('personalHomepage.toolbar.componentConfig') }}
          </a-button>
          <a-button v-if="editMode" type="primary" :loading="saving" @click="saveConfig">
            <template #icon>
              <SaveOutlined />
            </template>
            {{ $t('personalHomepage.toolbar.saveLayout') }}
          </a-button>
        </a-space>
      </div>
    </div>

    <!-- 鐎规悶鍎遍崣鍧楀冀?-->

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
                          <span class="menu-grid__title">{{ getMenuTitle(menuItem) }}</span>
                          <span class="menu-grid__module">{{ getMenuModuleName(menuItem) }}</span>
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
                          <span class="menu-grid__title">{{ getMenuTitle(menuItem) }}</span>
                          <span class="menu-grid__module">{{ getMenuModuleName(menuItem) }}</span>
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
                        @click="openNotice(record)"
                      >
                        <span class="list-block__title">{{ record.title }}</span>
                        <span class="list-block__meta">{{ getNoticeScopeLabel(record.scope) }}</span>
                        <span class="list-block__time">{{ formatNoticeTime(record) }}</span>
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
                  <template v-else>
                    <div class="custom-widget">
                      <div class="custom-widget__icon">
                        <FxIcon :name="getComponentWidgetMeta(item.i)?.icon" :size="24" />
                      </div>
                      <div class="custom-widget__content">
                        <div class="custom-widget__title">{{ getWidgetTitle(item.i) }}</div>
                        <div class="custom-widget__desc">
                          {{ getComponentWidgetMeta(item.i)?.useDesc || getWidgetSubtitle(item.i) || $t('personalHomepage.library.customPlaceholder') }}
                        </div>
                        <div class="custom-widget__code">{{ item.i }}</div>
                      </div>
                    </div>
                  </template>
                </div>
              </article>
            </GridItem>
          </GridLayout>
        </a-spin>
      </section>

      <!-- 闂佹澘绉堕悿鍡涙閵忊剝绶查柨娑欑煯缁酣宕烽妸褏妞介弶鍫熷灦閳ь兛鐒﹀Ο澶岀矆?-->
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

    <a-modal v-model:open="shareModalOpen" :title="$t('personalHomepage.share.shareTitle')" :footer="null">
      <a-input-group compact>
        <a-input v-model:value="shareCode" readonly style="width: calc(100% - 88px)" />
        <a-button style="width: 88px" @click="copyShareCode">
          {{ $t('personalHomepage.share.copy') }}
        </a-button>
      </a-input-group>
    </a-modal>

    <a-modal
      v-model:open="importModalOpen"
      :title="$t('personalHomepage.share.importTitle')"
      :ok-text="$t('personalHomepage.share.apply')"
      :ok-button-props="{ disabled: !importPreview }"
      :confirm-loading="importLoading"
      @ok="applyImportLayout"
    >
      <a-space direction="vertical" style="width: 100%" :size="12">
        <a-input-search
          v-model:value="importCode"
          allow-clear
          :placeholder="$t('personalHomepage.share.inputPlaceholder')"
          :loading="importLoading"
          @search="previewImportLayout"
        />
        <a-descriptions v-if="importPreview" size="small" bordered :column="1">
          <a-descriptions-item :label="$t('personalHomepage.share.shareCode')">
            {{ importPreview.shareCode }}
          </a-descriptions-item>
          <a-descriptions-item :label="$t('personalHomepage.share.moduleCode')">
            {{ importPreview.moduleCode }}
          </a-descriptions-item>
          <a-descriptions-item :label="$t('personalHomepage.share.createTime')">
            {{ importPreview.createTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-space>
    </a-modal>

    <a-drawer
      v-model:open="componentLibraryOpen"
      :title="$t('personalHomepage.library.title')"
      width="720"
      destroy-on-close
      placement="right"
      :body-style="{ padding: '16px' }"
    >
      <a-space direction="vertical" style="width: 100%" :size="12">
        <a-input-search
          v-model:value="componentSearchKeyword"
          allow-clear
          :placeholder="$t('personalHomepage.library.searchPlaceholder')"
          @search="loadComponentLibrary"
        />
        <a-space wrap>
          <a-radio-group v-model:value="componentScopeFilter" button-style="solid" @change="loadComponentLibrary">
            <a-radio-button value="ALL">{{ $t('personalHomepage.library.scopeAll') }}</a-radio-button>
            <a-radio-button value="PUBLIC">{{ $t('personalHomepage.library.scopePublic') }}</a-radio-button>
            <a-radio-button value="TENANT">{{ $t('personalHomepage.library.scopeTenant') }}</a-radio-button>
            <a-radio-button value="USER">{{ $t('personalHomepage.library.scopeUser') }}</a-radio-button>
          </a-radio-group>
          <a-button :loading="libraryLoading" @click="loadComponentLibrary">
            <template #icon>
              <ReloadOutlined />
            </template>
            {{ $t('personalHomepage.toolbar.refresh') }}
          </a-button>
        </a-space>
        <a-spin :spinning="libraryLoading">
          <div v-if="componentGroups.length === 0" class="designer-empty">
            <a-empty :description="$t('personalHomepage.library.empty')" />
          </div>
          <div v-else class="component-library">
            <a-collapse ghost :bordered="false">
              <a-collapse-panel v-for="group in componentGroups" :key="group.key">
                <template #header>
                  <div class="component-library__group-header">
                    <h4>{{ group.label }}</h4>
                    <span>{{ group.items.length }}</span>
                  </div>
                </template>
                <div class="component-library__grid">
                  <article
                    v-for="componentItem in group.items"
                    :key="componentItem.componentCode"
                    class="component-library__item"
                    :class="{
                      'component-library__item--selected': componentItem.selected && !componentItem.removed,
                      'component-library__item--removed': componentItem.removed,
                    }"
                  >
                    <header class="component-library__item-header">
                      <div class="component-library__icon">
                        <FxIcon :name="componentItem.icon" :size="18" />
                      </div>
                      <div class="component-library__title">
                        <strong>{{ componentItem.componentName }}</strong>
                        <span>{{ componentItem.componentCode }}</span>
                      </div>
                    </header>
                    <p class="component-library__desc">{{ componentItem.useDesc || componentItem.remark || '-' }}</p>
                    <div class="component-library__meta">
                      <a-tag v-if="componentItem.scopeLevel">{{ componentItem.scopeLevel }}</a-tag>
                      <a-tag v-if="componentItem.favorite" color="gold">{{ $t('personalHomepage.library.favorite') }}</a-tag>
                      <a-tag v-if="componentItem.selected && !componentItem.removed" color="green">{{ $t('personalHomepage.library.selected') }}</a-tag>
                      <a-tag v-if="componentItem.removed" color="red">{{ $t('personalHomepage.library.removed') }}</a-tag>
                    </div>
                    <div class="component-library__actions">
                      <a-button size="small" @click="toggleFavorite(componentItem)">
                        <template #icon>
                          <StarFilled v-if="componentItem.favorite" />
                          <StarOutlined v-else />
                        </template>
                      </a-button>
                      <a-button
                        size="small"
                        type="primary"
                        :disabled="componentItem.selected && !componentItem.removed"
                        @click="addComponentToHomepage(componentItem)"
                      >
                        <template #icon>
                          <PlusOutlined />
                        </template>
                      </a-button>
                      <a-button
                        size="small"
                        danger
                        :disabled="componentItem.removed"
                        @click="removeComponentFromHomepage(componentItem)"
                      >
                        <template #icon>
                          <DeleteOutlined />
                        </template>
                      </a-button>
                    </div>
                  </article>
                </div>
              </a-collapse-panel>
            </a-collapse>
          </div>
        </a-spin>
      </a-space>
    </a-drawer>

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
  DeleteOutlined,
  DragOutlined,
  ImportOutlined,
  MessageOutlined,
  PlusOutlined,
  ReloadOutlined,
  SaveOutlined,
  SettingOutlined,
  ShareAltOutlined,
  StarFilled,
  StarOutlined,
  UndoOutlined,
  UserOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { GridItem, GridLayout } from 'vue-grid-layout-v3'
import { message } from 'ant-design-vue'
import { normalizeMediaUrl } from '@/utils/media'
import {
  addHomepageComponent,
  createHomepageLayoutShare,
  createDefaultPersonalHomepageConfig,
  favoriteHomepageComponent,
  getCurrentPersonalHomepageConfig,
  getManagePersonalHomepageConfig,
  getPersonalHomepageSummary,
  getUserCommonMenus,
  getUserFavoriteMenus,
  listEffectiveHomepageComponents,
  mergePersonalHomepageConfig,
  previewHomepageLayoutShare,
  removeHomepageComponent,
  resetCurrentPersonalHomepageConfig,
  saveCurrentPersonalHomepageConfig,
  saveManagePersonalHomepageConfig,
  toggleUserFavoriteMenu,
  type HomepageComponentVO,
  type HomepageLayoutShareVO,
  type PersonalHomepageConfig,
  type PersonalMenuEntry,
  type PersonalHomepageScopeLevel,
  type PersonalHomepageSummaryVO,
} from '@/api/system/personalHomepage'
import FxIcon from '@/components/common/FxIcon.vue'
import { listUnreadMessages, markMessageRead, type SysMessageVO } from '@/api/system/message'
import { noticeApi, type SysNotice } from '@/api/system/notice'
import { pageMyPending, type WfExecutionDTO } from '@/api/workflow/execution'
import { FAVORITE_MANAGEMENT_PATH, PERSONAL_HOME_PATH } from '@/router'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import { dispatchMenuFavoritesRefresh, listenMenuFavoritesRefresh } from '@/composables/useMenuFavorites'
import { useUserStore } from '@/stores/user'
import { getIcon } from '@/utils/icon'
import { resolveMenuDisplayName, resolveModuleDisplayName } from '@/utils/menuI18n'

const { t, locale } = useI18n()

interface PersonalHomepageDesignerProps {
  mode: 'current' | 'manage'
  moduleCode?: string
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
  moduleCode: 'personal',
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
const activeNotices = ref<SysNotice[]>([])
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const now = ref(dayjs())
const syncingGrid = ref(false)
const summary = ref<PersonalHomepageSummaryVO | null>(null)
const componentLibraryOpen = ref(false)
const libraryLoading = ref(false)
const componentSearchKeyword = ref('')
const componentScopeFilter = ref<'ALL' | 'PUBLIC' | 'TENANT' | 'USER'>('ALL')
const componentLibrary = ref<HomepageComponentVO[]>([])
const shareModalOpen = ref(false)
const shareCode = ref('')
const sharing = ref(false)
const importModalOpen = ref(false)
const importCode = ref('')
const importPreview = ref<HomepageLayoutShareVO | null>(null)
const importLoading = ref(false)
let clockTimer: number | undefined
let stopMenuFavoritesRefresh: (() => void) | null = null
const MAX_COMMON_MENU_COUNT = 6

/**
 * 鐟欙絾鐎介悽銊﹀煕婢舵潙鍎氶崷鏉挎絻閿涘奔绗?{@link MainLayout} 濞戞搩鍙冮妴濠囨焾閵娿儯浠堥柛宥呯箺椤宕氬▎搴ｇ闁归晲妞掔粩鎾嚊濞ｎ兘鍋?
 * <p>闁烩晝顭堥顔炬崉椤栨氨绐炲ù鍏间亢钘熼柛蹇嬪妺鐠愮喓绱旈幋婵嗗綘 `/api` 闁告挸绉剁槐鎴︽晬鐏炶偐鈹掑ù?a-avatar 闁烩晛鐡ㄧ敮鎾礉閻樼儤绁伴柕?/p>
 *
 * @param raw 闁告艾娴烽顒勫箣?Store 濞戞搩鍘惧▓鎴﹀储閻斿娼楅悹渚灠缁?
 * @returns 闁告瑯鍨甸顒€效閸屾粍鐣遍悗鐟版湰閺?URL闁挎稒绋掑Λ銈夊极閸喐顦у☉鎾规閳规牜鈧稒顨堥浣圭▔?
 */
function resolveUserAvatarSrc(raw?: string | null): string {
  return normalizeMediaUrl(raw)
}

/**
 * 濞戞搩浜欏Ч澶嬶純閺嶎厹鈧?Hero 闁告牕鎼惈宥囩矆閾忚鏆忓鑸垫綑閸庢岸鏁嶉崼鐔稿枀閻熸洑鐒︾敮鎾矗閿濆嫮鍠橀柛蹇撶墳缁辨繄绱撻崫鍕╀杭闁哄啳娉涘ú鏍焻閳ь剟宕氶弶璺ㄧЪ闁告挸绉跺▍銉ㄣ亹閺囩姵鏆忛柟纾嬫腹娣囧﹪骞侀銈囩闁?
 */
const heroAvatarSrc = computed(() => {
  const raw = summary.value?.avatar || userStore.userInfo?.avatar
  return resolveUserAvatarSrc(raw)
})

/**
 * 闂傚偆鍠栭埀顒佺懆椤曘垻浠﹂弴鐘粵闁告艾绋勭槐浼村箺濡娲ｉ柡鍕暩琚ㄥù鍏济崢娑㈡晬鐏炶棄寰撴繛鍡忊偓鑼剁 Store 濞戞搩鍘惧▓鎴︽偨閵婏箑鐓曢柛姘Р閳ь兛娴囨径鍕矗閺嬵偀鍋?
 */
const displayNameForHero = computed(() => {
  return (
    summary.value?.nickname ||
    userStore.userInfo?.username ||
    userStore.userInfo?.account ||
    ''
  )
})

/** 闂傚偆鍠栭埀顒佺懄濡炲倸鈻撶喊澶岀獥闁哄啠鏅滃▍?/ 濞戞挸顑呭畷?/ 濠㈣埖绮撳Λ鍧楁晬閸粎鐟㈤柛姘捣椤忣剟骞楀Ο娆炬矗闁哄牆绉存慨鐔煎籍閼告鍞介柛鎺撳笒閸ㄥ孩绋夐埀顒勬嚊鏉堝墽绀?*/
type GreetingPhase = 'morning' | 'afternoon' | 'evening'

/**
 * 闁哄秷顫夊畵浣姐亹閹惧啿顤呴柡鍫墮濠€鎾籍閸洘锛熼悹渚婄磿閻ｅ姊婚灏栧亾濞嗘劖顦ф繛鍫㈠仯閳?
 *
 * @see now 閻㈣鲸妞傞柦鐔风暰閺冭泛鍩涢弬甯礉鐠恒劍妞傚▓鍏哥窗閼奉亜濮╅弴瀛樻煀閺傚洦顢?
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
 * 濞戞搩鍘介弸鍐捶閻戞ɑ鐝☉鎾愁儑濞堟垹绮旈幏宀€姣橀柛姘捣缁辨垿鏁嶉崼婵嗗弗闁?/ 濠靛倸鍟块敍瀣晬婢舵稓骞㈤柡鍫簽閻擄繝骞€瑜嶉崺鍡涘籍閺堜絻绀嬬紒灞句航閳?
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
 * 闁搞儰绮欏顖炲礌閺嶏箑鐦滈梻鍌ゅ枛閳ь剚鐟ㄩ銏ゆ晬濮橆剛姣€闁轰緡鍓涘▓鎴炴叏閹捐櫕鍊崇紒澶嬪閻?+ 闁告帒妫欏鍌氣枔閻㈠憡锛栭柛濠冪懁缁楀瞼绱掗幘瀛樺皢閻犲浂鍙忕槐閬嶆嚐鏉堛劍鐎柛?Mr./Ms./闁哄啰濮疯ⅷ閻犲鎸风粭浣虹矓瀹ュ懎缍栫€殿喖绻堥埀?
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
 * 濞寸姴锕ュΛ鈺呭籍閵夛附鍩傚☉鎾冲濡诧箓寮甸悢宄邦棇闁哄秴娲。浠嬫晬閸粎鐟㈤柣锝呯焸濞兼壆鎷犻鈾€鏋呭☉鎾亾闁肩柉鎻槐姘跺Υ?
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

const widgetDefaults: Record<string, { x: number; y: number; w: number; h: number; orderNum: number; minW: number; minH: number }> = {
  commonMenus: { x: 0, y: 0, w: 6, h: 4, orderNum: 10, minW: 2, minH: 2 },
  myFavorites: { x: 0, y: 4, w: 6, h: 4, orderNum: 20, minW: 2, minH: 2 },
  pendingApprovals: { x: 6, y: 0, w: 6, h: 4, orderNum: 30, minW: 2, minH: 2 },
  calendar: { x: 6, y: 4, w: 3, h: 4, orderNum: 40, minW: 2, minH: 2 },
  currentTime: { x: 9, y: 4, w: 3, h: 3, orderNum: 50, minW: 2, minH: 2 },
  messages: { x: 0, y: 8, w: 6, h: 4, orderNum: 60, minW: 2, minH: 2 },
  notices: { x: 6, y: 8, w: 6, h: 4, orderNum: 70, minW: 2, minH: 2 },
}

// 閸ヤ粙妾崠鏍电窗缂佸嫪娆㈤弽鍥暯
const widgetTitleMap: Record<string, string> = {
  commonMenus: 'personalHomepage.components.commonMenus.title',
  myFavorites: 'personalHomepage.components.myFavorites.title',
  pendingApprovals: 'personalHomepage.components.pendingApprovals.title',
  calendar: 'personalHomepage.components.calendar.title',
  messages: 'personalHomepage.components.messages.title',
  notices: 'personalHomepage.components.notices.title',
  currentTime: 'personalHomepage.components.currentTime.title',
}

// 闁搞儰绮欏顖炲礌閺嶇數绐楃紓浣稿濞嗐垽宕滈娑氬灱濡?
const widgetSubtitleMap: Record<string, string> = {
  commonMenus: 'personalHomepage.components.commonMenus.subtitle',
  myFavorites: 'personalHomepage.components.myFavorites.subtitle',
  pendingApprovals: 'personalHomepage.components.pendingApprovals.subtitle',
  calendar: 'personalHomepage.components.calendar.subtitle',
  messages: 'personalHomepage.components.messages.subtitle',
  notices: 'personalHomepage.components.notices.subtitle',
  currentTime: 'personalHomepage.components.currentTime.subtitle',
}

// 闁搞儰绮欏顖炲礌閺嶇數绐楃紒宀冩婵悂骞€娴ｈ鐎俊?
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

const homepageModuleCode = computed(() => props.moduleCode || 'personal')

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

const componentGroups = computed(() => {
  const groups = new Map<string, { key: string; label: string; items: HomepageComponentVO[] }>()
  for (const item of componentLibrary.value) {
    if (componentScopeFilter.value !== 'ALL' && item.scopeLevel !== componentScopeFilter.value) {
      continue
    }
    const keyword = componentSearchKeyword.value.trim().toLowerCase()
    if (keyword) {
      const matched = [item.componentCode, item.componentName, item.useDesc, item.categoryName, item.categoryCode]
        .filter(Boolean)
        .some(text => String(text).toLowerCase().includes(keyword))
      if (!matched) {
        continue
      }
    }
    const key = item.categoryCode || item.categoryName || 'default'
    if (!groups.has(key)) {
      groups.set(key, {
        key,
        label: item.categoryName || item.categoryCode || t('personalHomepage.library.defaultGroup'),
        items: [],
      })
    }
    groups.get(key)!.items.push(item)
  }
  return Array.from(groups.values()).map(group => ({
    ...group,
    items: group.items.sort((left, right) => {
      const leftOrder = Number(left.orderNum ?? 0)
      const rightOrder = Number(right.orderNum ?? 0)
      if (leftOrder !== rightOrder) {
        return leftOrder - rightOrder
      }
      return left.componentCode.localeCompare(right.componentCode)
    }),
  }))
})

const currentColNum = computed(() => {
  if (viewportWidth.value < 768) {
    return config.value.layout.mobileColNum || 4
  }
  if (viewportWidth.value < 1280) {
    return config.value.layout.tabletColNum || 8
  }
  return config.value.layout.colNum || 12
})

const currentRowHeight = computed(() => config.value.layout.rowHeight || 64)

const gridMargin = computed<[number, number]>(() => [
  config.value.layout.marginX || 10,
  config.value.layout.marginY || 10,
])

const commonMenus = computed(() => commonMenuItems.value.slice(0, MAX_COMMON_MENU_COUNT))

const favoriteMenus = computed(() => {
  const limit = Math.min(
    Math.max(toNumber(findWidget('myFavorites')?.params.limit, defaultLimit('myFavorites')), 0),
    MAX_COMMON_MENU_COUNT,
  )
  return favoriteMenuItems.value.slice(0, limit)
})

const favoriteMenuPathSet = computed(() => new Set(favoriteMenuItems.value.map(item => String(item.path || ''))))

const inboxMessages = computed(() => {
  const limit = toNumber(findWidget('messages')?.params.limit, defaultLimit('messages'))
  return unreadMessages.value.slice(0, Math.max(limit, 0))
})

const noticeMessages = computed(() => {
  const limit = toNumber(findWidget('notices')?.params.limit, defaultLimit('notices'))
  return activeNotices.value.slice(0, Math.max(limit, 0))
})

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

function getComponentWidgetMeta(componentCode: string) {
  return componentLibrary.value.find(item => item.componentCode === componentCode) || null
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
  requestAnimationFrame(() => {
    syncGridFromConfig()
  })
}

async function reloadConfig() {
  loading.value = true
  try {
    const options = { moduleCode: homepageModuleCode.value }
    const remoteConfig = props.mode === 'current'
      ? await getCurrentPersonalHomepageConfig(options)
      : await getManagePersonalHomepageConfig(scopeLevel.value, options)
    config.value = mergePersonalHomepageConfig(remoteConfig)
    syncGridFromConfig()
    await loadWidgetData()
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
    const options = { moduleCode: homepageModuleCode.value }
    if (props.mode === 'current') {
      await saveCurrentPersonalHomepageConfig(payload, options)
    } else {
      await saveManagePersonalHomepageConfig(scopeLevel.value, payload, options)
    }
    config.value = payload
    syncGridFromConfig()
    message.success(t('personalHomepage.message.saveSuccess'))
  } catch (error) {
    console.error('保存个人首页配置失败:', error)
    message.error(t('personalHomepage.message.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function createShareCode() {
  sharing.value = true
  try {
    const payload = mergePersonalHomepageConfig(config.value)
    const result = await createHomepageLayoutShare({
      moduleCode: homepageModuleCode.value,
      config: payload,
    })
    shareCode.value = result.shareCode
    shareModalOpen.value = true
  } finally {
    sharing.value = false
  }
}

function openImportLayout() {
  importCode.value = ''
  importPreview.value = null
  importModalOpen.value = true
}

async function previewImportLayout() {
  const code = importCode.value.trim()
  if (!code) {
    importPreview.value = null
    return
  }
  importLoading.value = true
  try {
    importPreview.value = await previewHomepageLayoutShare({
      shareCode: code,
      moduleCode: homepageModuleCode.value,
    })
  } finally {
    importLoading.value = false
  }
}

function applyImportLayout() {
  if (!importPreview.value?.config) {
    return
  }
  config.value = mergePersonalHomepageConfig(importPreview.value.config)
  syncGridFromConfig()
  importModalOpen.value = false
  message.success(t('personalHomepage.share.applySuccess'))
}

async function copyShareCode() {
  if (!shareCode.value) {
    return
  }
  await navigator.clipboard?.writeText(shareCode.value)
  message.success(t('personalHomepage.share.copySuccess'))
}

function openComponentLibrary() {
  componentLibraryOpen.value = true
  loadComponentLibrary()
}

function openPersonalComponentConfig() {
  router.push('/workspace/home/component-config').catch(() => {})
}

async function loadComponentLibrary() {
  if (props.mode !== 'current') {
    componentLibrary.value = []
    return
  }
  libraryLoading.value = true
  try {
    const list = await listEffectiveHomepageComponents({
      moduleCode: homepageModuleCode.value,
      keyword: componentSearchKeyword.value || undefined,
      scopeLevel: componentScopeFilter.value === 'ALL' ? undefined : componentScopeFilter.value,
    })
    componentLibrary.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('load homepage component library failed:', error)
    componentLibrary.value = []
  } finally {
    libraryLoading.value = false
  }
}

function getWidgetDefaultLayout(widgetKey: string) {
  const maxY = Math.max(0, ...config.value.widgets.map(item => Number(item.y || 0) + Number(item.h || 3)))
  return widgetDefaults[widgetKey] || {
    x: 0,
    y: maxY,
    w: 6,
    h: 4,
    orderNum: Math.max(0, ...config.value.widgets.map(item => Number(item.orderNum || 0))) + 10,
    minW: 3,
    minH: 2,
  }
}

function safeParseParams(params?: string) {
  if (!params) {
    return {}
  }
  try {
    return JSON.parse(params)
  } catch (error) {
    return {}
  }
}

function ensureWidgetExists(componentItem: HomepageComponentVO) {
  const existing = findWidget(componentItem.componentCode)
  if (existing) {
    return existing
  }
  const defaults = getWidgetDefaultLayout(componentItem.componentCode)
  const widget = {
    key: componentItem.componentCode,
    title: componentItem.componentName,
    visible: true,
    x: defaults.x,
    y: defaults.y,
    w: defaults.w,
    h: defaults.h,
    minW: defaults.minW,
    minH: defaults.minH,
    orderNum: defaults.orderNum,
    params: safeParseParams(componentItem.params),
  }
  config.value.widgets = [...config.value.widgets, widget]
  syncGridFromConfig()
  return widget
}

async function toggleFavorite(componentItem: HomepageComponentVO) {
  try {
    await favoriteHomepageComponent({
      componentCode: componentItem.componentCode,
      favorite: !componentItem.favorite,
      moduleCode: homepageModuleCode.value,
    })
    await loadComponentLibrary()
  } catch (error) {
    console.error('toggle homepage component favorite failed:', error)
  }
}

async function addComponentToHomepage(componentItem: HomepageComponentVO) {
  try {
    await addHomepageComponent({ componentCode: componentItem.componentCode, moduleCode: homepageModuleCode.value })
    const widget = ensureWidgetExists(componentItem)
    widget.visible = true
    widget.title = componentItem.componentName
    syncGridFromConfig()
    await loadComponentLibrary()
  } catch (error) {
    console.error('add homepage component failed:', error)
  }
}

async function removeComponentFromHomepage(componentItem: HomepageComponentVO) {
  try {
    await removeHomepageComponent({ componentCode: componentItem.componentCode, moduleCode: homepageModuleCode.value })
    config.value.widgets = config.value.widgets.map(widget => (
      widget.key === componentItem.componentCode
        ? { ...widget, visible: false }
        : widget
    ))
    syncGridFromConfig()
    await loadComponentLibrary()
  } catch (error) {
    console.error('remove homepage component failed:', error)
  }
}

async function resetToDefault() {
  if (props.mode !== 'current') {
    return
  }
  saving.value = true
  try {
    await resetCurrentPersonalHomepageConfig({ moduleCode: homepageModuleCode.value })
    await reloadConfig()
    message.success(t('personalHomepage.message.resetSuccess'))
  } catch (error) {
    console.error('恢复默认布局失败:', error)
    message.error(t('personalHomepage.message.resetFailed'))
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
    loadActiveNotices(),
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

async function refreshMenuFavoriteWidgets() {
  await Promise.all([
    loadCommonMenus(),
    loadFavoriteMenus(),
  ])
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
  )
  try {
    const list = await listUnreadMessages(requestLimit, 'MESSAGE')
    unreadMessages.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载未读消息失败:', error)
    unreadMessages.value = []
  }
}

async function loadActiveNotices() {
  const requestLimit = Math.max(
    10,
    toNumber(findWidget('notices')?.params.limit, defaultLimit('notices')),
  )
  try {
    const list = await noticeApi.activeList({ maxCount: requestLimit })
    activeNotices.value = Array.isArray(list) ? list : []
  } catch (error) {
    console.error('加载系统通知失败:', error)
    activeNotices.value = []
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
  if (widgetKey === 'messages') {
    window.dispatchEvent(new CustomEvent('fx:open-message-drawer', { detail: { tab: 'MESSAGE' } }))
    return
  }
  if (widgetKey === 'notices') {
    window.dispatchEvent(new CustomEvent('fx:open-message-drawer', { detail: { tab: 'SYSTEM' } }))
  }
}

function openMenu(path: string) {
  if (!path || path === PERSONAL_HOME_PATH) {
    return
  }
  router.push(path).catch(() => {})
}

function getMenuTitle(menuItem: PersonalMenuEntry) {
  return resolveMenuDisplayName({
    path: menuItem.path,
    title: menuItem.title,
    moduleCode: menuItem.moduleCode,
    moduleName: menuItem.moduleName,
  })
}

function getMenuModuleName(menuItem: PersonalMenuEntry) {
  return resolveModuleDisplayName(menuItem.moduleCode, menuItem.moduleName)
}

async function handleToggleFavorite(menuItem: PersonalMenuEntry) {
  const path = String(menuItem?.path || '')
  if (!path) {
    return
  }
  try {
    const nextFavorite = await toggleUserFavoriteMenu(path)
    dispatchMenuFavoritesRefresh(path, typeof nextFavorite === 'boolean' ? nextFavorite : undefined)
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
  window.dispatchEvent(new CustomEvent('fx:open-message-drawer', { detail: { tab: 'MESSAGE' } }))
}

function openNotice(record: SysNotice) {
  if (!record?.id) {
    return
  }
  window.dispatchEvent(new CustomEvent('fx:open-message-drawer', { detail: { tab: 'SYSTEM' } }))
}

function getNoticeScopeLabel(scope?: string) {
  if (scope === 'PUBLIC') {
    return t('system.notice.center.scopePublic')
  }
  if (scope === 'TENANT') {
    return t('system.notice.center.scopeTenant')
  }
  return t('personalHomepage.components.notices.systemType')
}

function formatNoticeTime(record: SysNotice) {
  return formatDateTime(record.startTime || record.createTime)
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

function handleSystemNoticeRefresh() {
  loadActiveNotices()
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
  window.addEventListener('fx:system-notice-refresh', handleSystemNoticeRefresh as EventListener)
  stopMenuFavoritesRefresh = listenMenuFavoritesRefresh(() => {
    void refreshMenuFavoriteWidgets()
  })
})

onUnmounted(() => {
  if (clockTimer) {
    window.clearInterval(clockTimer)
  }
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('fx:message-received', handleMessageEvent as EventListener)
  window.removeEventListener('fx:system-notice-refresh', handleSystemNoticeRefresh as EventListener)
  stopMenuFavoritesRefresh?.()
  stopMenuFavoritesRefresh = null
})
</script>

<style scoped lang="less" src="@/styles/components/personal-homepage/personal-homepage-designer.less"></style>
