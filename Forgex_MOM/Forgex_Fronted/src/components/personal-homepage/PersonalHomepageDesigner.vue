<template>
  <div class="personal-homepage-designer">
    <div class="designer-hero">
      <div>
        <p class="designer-hero__eyebrow">Personal Workspace</p>
        <h2 class="designer-hero__title">{{ resolvedTitle }}</h2>
        <p class="designer-hero__desc">{{ resolvedDescription }}</p>
      </div>
      <div class="designer-hero__meta">
        <span class="designer-badge">统一默认入口</span>
        <span class="designer-badge designer-badge--soft">{{ scopeLabel }}</span>
      </div>
    </div>

    <div class="designer-toolbar">
      <a-space wrap>
        <a-select
          v-if="mode === 'manage' && showScopeSelector"
          v-model:value="scopeLevel"
          style="width: 160px"
        >
          <a-select-option value="TENANT">租户默认</a-select-option>
          <a-select-option value="PUBLIC">公共默认</a-select-option>
        </a-select>
        <a-button @click="toggleEditMode">
          <template #icon>
            <SettingOutlined />
          </template>
          {{ editMode ? '退出编辑' : '编辑布局' }}
        </a-button>
        <a-button @click="reloadConfig">
          <template #icon>
            <ReloadOutlined />
          </template>
          刷新
        </a-button>
        <a-button v-if="mode === 'current'" @click="resetToDefault">
          <template #icon>
            <UndoOutlined />
          </template>
          恢复默认
        </a-button>
        <a-button type="primary" :loading="saving" @click="saveConfig">
          <template #icon>
            <SaveOutlined />
          </template>
          保存布局
        </a-button>
      </a-space>
      <div class="designer-toolbar__hint">
        <span>{{ editMode ? '拖拽和缩放组件后可直接保存' : '切换到编辑模式后可调整组件布局' }}</span>
      </div>
    </div>

    <div class="designer-content">
      <section class="designer-stage">
        <a-spin :spinning="loading">
          <div v-if="visibleWidgets.length === 0" class="designer-empty">
            <a-empty description="当前没有启用的首页组件" />
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
                      <p class="widget-card__subtitle">{{ getWidgetMeta(item.i).subtitle }}</p>
                    </div>
                  </div>
                  <div class="widget-card__actions">
                    <a-button
                      v-if="shouldShowMore(item.i)"
                      type="link"
                      size="small"
                      @click="openWidgetMore(item.i)"
                    >
                      更多
                    </a-button>
                    <span v-if="editMode" class="widget-card__drag">
                      <DragOutlined />
                    </span>
                  </div>
                </header>

                <div class="widget-card__body">
                  <template v-if="item.i === 'commonMenus'">
                    <div v-if="commonMenus.length" class="menu-grid">
                      <button
                        v-for="menuItem in commonMenus"
                        :key="menuItem.path"
                        type="button"
                        class="menu-grid__item"
                        @click="openMenu(menuItem.path)"
                      >
                        <span class="menu-grid__title">{{ menuItem.title }}</span>
                        <span class="menu-grid__module">{{ menuItem.moduleName }}</span>
                      </button>
                    </div>
                    <a-empty v-else description="暂无常用菜单" />
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
                    <a-empty v-else description="暂无待处理审批" />
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
                        <span class="list-block__meta">{{ record.senderName || '站内消息' }}</span>
                        <span class="list-block__time">{{ record.createTime || '-' }}</span>
                      </button>
                    </div>
                    <a-empty v-else description="暂无未读消息" />
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
                        <span class="list-block__meta">{{ record.bizType || record.type || '系统通知' }}</span>
                        <span class="list-block__time">{{ record.createTime || '-' }}</span>
                      </button>
                    </div>
                    <a-empty v-else description="暂无系统通知" />
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

      <aside class="designer-panel">
        <div class="designer-panel__card">
          <div class="designer-panel__header">
            <h3>组件配置</h3>
            <span>显隐、条数与更多入口</span>
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
                  <span>{{ widget.title }}</span>
                </div>
                <a-switch
                  :checked="widget.visible"
                  :disabled="mode !== 'current' && scopeLevel === 'PUBLIC' && !editMode"
                  @change="updateWidgetVisibility(widget.key, $event)"
                />
              </div>
              <div class="widget-setting__fields">
                <label class="widget-setting__field">
                  <span>显示条数</span>
                  <a-input-number
                    :value="toNumber(widget.params.limit, defaultLimit(widget.key))"
                    :min="0"
                    :max="20"
                    :disabled="!widget.visible"
                    @change="updateWidgetParam(widget.key, 'limit', $event)"
                  />
                </label>
                <label class="widget-setting__field widget-setting__field--switch">
                  <span>显示更多入口</span>
                  <a-switch
                    :checked="toBoolean(widget.params.showMore, defaultShowMore(widget.key))"
                    :disabled="!widget.visible || !hasMoreAction(widget.key)"
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
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
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
  UndoOutlined,
} from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { GridItem, GridLayout } from 'vue-grid-layout-v3'
import {
  createDefaultPersonalHomepageConfig,
  getCurrentPersonalHomepageConfig,
  getManagePersonalHomepageConfig,
  mergePersonalHomepageConfig,
  resetCurrentPersonalHomepageConfig,
  saveCurrentPersonalHomepageConfig,
  saveManagePersonalHomepageConfig,
  type PersonalHomepageConfig,
  type PersonalHomepageScopeLevel,
  type PersonalHomepageWidgetConfig,
} from '@/api/system/personalHomepage'
import { listUnreadMessages, markMessageRead, type SysMessageVO } from '@/api/system/message'
import { pageMyPending, type WfExecutionDTO } from '@/api/workflow/execution'
import { PERSONAL_HOME_PATH } from '@/router'
import { approvalRoutePaths } from '@/router/approvalRoutePaths'
import { usePermissionStore } from '@/stores/permission'

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

interface CommonMenuEntry {
  path: string
  title: string
  moduleCode: string
  moduleName: string
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
const permissionStore = usePermissionStore()

const loading = ref(false)
const saving = ref(false)
const editMode = ref(props.initialEditMode)
const scopeLevel = ref<Exclude<PersonalHomepageScopeLevel, 'USER'>>(props.initialScopeLevel)
const config = ref<PersonalHomepageConfig>(createDefaultPersonalHomepageConfig())
const gridLayout = ref<GridLayoutItem[]>([])
const pendingApprovals = ref<WfExecutionDTO[]>([])
const unreadMessages = ref<SysMessageVO[]>([])
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const now = ref(dayjs())
const syncingGrid = ref(false)
let clockTimer: number | undefined

const mode = computed(() => props.mode)
const showScopeSelector = computed(() => props.showScopeSelector)

const widgetMetaMap: Record<string, { icon: any; subtitle: string }> = {
  commonMenus: { icon: AppstoreOutlined, subtitle: '常看入口和最近访问菜单' },
  pendingApprovals: { icon: CheckCircleOutlined, subtitle: '我收到的审批待办' },
  calendar: { icon: CalendarOutlined, subtitle: '本地日历视图' },
  messages: { icon: MessageOutlined, subtitle: '用户发给我的站内消息' },
  notices: { icon: BellOutlined, subtitle: '审批与系统类通知' },
  currentTime: { icon: ClockCircleOutlined, subtitle: '当前日期与时间' },
}

const resolvedTitle = computed(() => {
  if (props.title) {
    return props.title
  }
  return props.mode === 'manage' ? '个人首页默认配置' : '个人首页'
})

const resolvedDescription = computed(() => {
  if (props.description) {
    return props.description
  }
  return props.mode === 'manage'
    ? '统一维护公共级和租户级默认门户布局，所有用户进入系统后都会以这里为基础。'
    : '进入系统后的唯一默认落地页，支持按个人习惯保存组件布局。'
})

const scopeLabel = computed(() => {
  if (props.mode === 'current') {
    return '用户级'
  }
  return scopeLevel.value === 'PUBLIC' ? '公共级' : '租户级'
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
  const recentRoutes = getRecentRoutes()
  const routeOrder = new Map<string, number>(recentRoutes.map((path, index) => [path, index]))
  const entries = flattenCommonMenus()
  entries.sort((left, right) => {
    const leftOrder = routeOrder.has(left.path) ? routeOrder.get(left.path)! : Number.MAX_SAFE_INTEGER
    const rightOrder = routeOrder.has(right.path) ? routeOrder.get(right.path)! : Number.MAX_SAFE_INTEGER
    if (leftOrder !== rightOrder) {
      return leftOrder - rightOrder
    }
    if (left.moduleName !== right.moduleName) {
      return left.moduleName.localeCompare(right.moduleName)
    }
    return left.title.localeCompare(right.title)
  })
  const limit = toNumber(findWidget('commonMenus')?.params.limit, defaultLimit('commonMenus'))
  return entries.slice(0, Math.max(limit, 0))
})

const inboxMessages = computed(() => {
  const limit = toNumber(findWidget('messages')?.params.limit, defaultLimit('messages'))
  return unreadMessages.value.filter(item => !isNoticeMessage(item)).slice(0, Math.max(limit, 0))
})

const noticeMessages = computed(() => {
  const limit = toNumber(findWidget('notices')?.params.limit, defaultLimit('notices'))
  return unreadMessages.value.filter(isNoticeMessage).slice(0, Math.max(limit, 0))
})

const nowTime = computed(() => now.value.format('HH:mm:ss'))
const nowDate = computed(() => now.value.format('YYYY年MM月DD日 dddd'))
const calendarTitle = computed(() => now.value.format('YYYY年MM月'))
const weekDays = ['一', '二', '三', '四', '五', '六', '日']

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
  return findWidget(widgetKey)?.title || widgetKey
}

function findWidget(widgetKey: string) {
  return config.value.widgets.find(widget => widget.key === widgetKey)
}

function defaultLimit(widgetKey: string) {
  return toNumber(createDefaultPersonalHomepageConfig().widgets.find(item => item.key === widgetKey)?.params.limit, 0)
}

function defaultShowMore(widgetKey: string) {
  return toBoolean(createDefaultPersonalHomepageConfig().widgets.find(item => item.key === widgetKey)?.params.showMore, false)
}

function toNumber(value: unknown, fallback: number) {
  const parsed = Number(value)
  return Number.isFinite(parsed) ? parsed : fallback
}

function toBoolean(value: unknown, fallback: boolean) {
  if (typeof value === 'boolean') {
    return value
  }
  if (value === 'true') {
    return true
  }
  if (value === 'false') {
    return false
  }
  return fallback
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
  } catch (error) {
    console.error('加载个人首页配置失败:', error)
    message.error('加载个人首页配置失败')
    config.value = createDefaultPersonalHomepageConfig()
    syncGridFromConfig()
  } finally {
    loading.value = false
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
    message.success('个人首页配置已保存')
    config.value = payload
    syncGridFromConfig()
  } catch (error) {
    console.error('保存个人首页配置失败:', error)
    message.error('保存个人首页配置失败')
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
    message.success('已恢复为默认布局')
    await reloadConfig()
  } catch (error) {
    console.error('恢复默认布局失败:', error)
    message.error('恢复默认布局失败')
  } finally {
    saving.value = false
  }
}

async function loadWidgetData() {
  await Promise.all([loadPendingApprovals(), loadUnreadMessages()])
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
  return ['commonMenus', 'pendingApprovals', 'messages', 'notices'].includes(widgetKey)
}

function openWidgetMore(widgetKey: string) {
  if (widgetKey === 'commonMenus') {
    window.dispatchEvent(new CustomEvent('fx:open-global-search'))
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

function openApproval(record: WfExecutionDTO) {
  router.push(approvalRoutePaths.myPending).catch(() => {})
}

async function openMessage(record: SysMessageVO) {
  try {
    await markMessageRead(record.id)
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

function isNoticeMessage(messageItem: SysMessageVO) {
  const bizType = String(messageItem.bizType || '').toUpperCase()
  return !messageItem.senderUserId
    || bizType.startsWith('WF_')
    || bizType.includes('NOTICE')
    || bizType.includes('SYSTEM')
}

function getRecentRoutes() {
  try {
    const raw = localStorage.getItem('fx-recent-routes')
    const parsed = raw ? JSON.parse(raw) : []
    if (!Array.isArray(parsed)) {
      return []
    }
    return parsed
      .map(item => String(item || '').split('?')[0])
      .filter(item => item && item.startsWith('/workspace/') && item !== PERSONAL_HOME_PATH)
  } catch (error) {
    console.error('读取最近访问记录失败:', error)
    return []
  }
}

function flattenCommonMenus() {
  const moduleNameMap = new Map<string, string>(
    (Array.isArray(permissionStore.modules) ? permissionStore.modules : []).map((module: any) => [
      String(module.code || ''),
      String(module.name || module.code || ''),
    ]),
  )

  const collected: CommonMenuEntry[] = []
  const seen = new Set<string>()

  function walk(children: any[], moduleCode: string, parentSegments: string[] = []) {
    children.forEach(child => {
      if (!child || child.meta?.hidden) {
        return
      }
      const childPath = String(child.path || '').replace(/^\/+/, '')
      const currentSegments = childPath ? [...parentSegments, childPath] : [...parentSegments]
      if (child.meta?.type === 'catalog') {
        walk(Array.isArray(child.children) ? child.children : [], moduleCode, currentSegments)
        return
      }
      const fullPath = child.path?.startsWith('/')
        ? String(child.path)
        : `/workspace/${moduleCode}/${currentSegments.join('/')}`.replace(/\/+/g, '/')
      if (!fullPath || seen.has(fullPath) || fullPath === PERSONAL_HOME_PATH) {
        return
      }
      seen.add(fullPath)
      collected.push({
        path: fullPath,
        title: String(child.meta?.title || child.name || fullPath),
        moduleCode,
        moduleName: moduleNameMap.get(moduleCode) || moduleCode,
      })
      if (Array.isArray(child.children) && child.children.length > 0) {
        walk(child.children, moduleCode, currentSegments)
      }
    })
  }

  ;(Array.isArray(permissionStore.routes) ? permissionStore.routes : []).forEach((routeItem: any) => {
    const moduleCode = String(routeItem.meta?.module || routeItem.path || '')
    if (!moduleCode) {
      return
    }
    walk(Array.isArray(routeItem.children) ? routeItem.children : [], moduleCode)
  })

  return collected
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
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 16px;
  min-height: 0;
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

.menu-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.menu-grid__item,
.list-block__item {
  width: 100%;
  border: none;
  text-align: left;
  cursor: pointer;
}

.menu-grid__item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-height: 88px;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(180deg, var(--fx-bg-elevated, #f8fafc), color-mix(in srgb, var(--fx-primary-bg, #eff6ff) 45%, var(--fx-bg-container, #ffffff)));
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.menu-grid__item:hover,
.list-block__item:hover {
  transform: translateY(-2px);
}

.menu-grid__title,
.list-block__title {
  font-weight: 600;
  color: var(--fx-text-primary, #111827);
}

.menu-grid__module,
.list-block__meta,
.list-block__time {
  font-size: 12px;
  color: var(--fx-text-secondary, #6b7280);
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
