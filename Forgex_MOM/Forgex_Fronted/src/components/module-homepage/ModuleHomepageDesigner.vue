<template>
  <div class="module-homepage-designer">
    <div class="module-homepage-hero" data-guide-id="module-homepage-hero">
      <div>
        <p class="module-homepage-hero__eyebrow">{{ moduleMeta.name }}</p>
        <h2 class="module-homepage-hero__title">{{ resolvedTitle }}</h2>
        <p class="module-homepage-hero__desc">{{ resolvedDescription }}</p>
      </div>
      <div class="module-homepage-hero__meta">
        <span class="module-homepage-badge">{{ scopeLabel }}</span>
        <span class="module-homepage-badge module-homepage-badge--soft">
          {{ editMode ? t('personalHomepage.module.mode.editing') : t('personalHomepage.module.mode.view') }}
        </span>
      </div>
    </div>

    <div class="module-homepage-toolbar" data-guide-id="module-homepage-toolbar">
      <a-space wrap>
        <a-select
          v-if="mode === 'manage' && showScopeSelector"
          v-model:value="scopeLevel"
          style="width: 160px"
        >
          <a-select-option value="TENANT">{{ t('personalHomepage.hero.badge.tenant') }}</a-select-option>
          <a-select-option value="PUBLIC">{{ t('personalHomepage.hero.badge.public') }}</a-select-option>
        </a-select>
        <a-button @click="toggleEditMode">
          <template #icon>
            <SettingOutlined />
          </template>
          {{ editMode ? t('personalHomepage.module.toolbar.exitConfig') : t('personalHomepage.toolbar.editMode') }}
        </a-button>
        <a-button @click="reloadConfig">
          <template #icon>
            <ReloadOutlined />
          </template>
          {{ t('personalHomepage.toolbar.refresh') }}
        </a-button>
        <a-button v-if="editMode && mode === 'current'" @click="resetToDefault">
          <template #icon>
            <UndoOutlined />
          </template>
          {{ t('personalHomepage.toolbar.resetDefault') }}
        </a-button>
        <a-button v-if="editMode" type="primary" :loading="saving" @click="saveConfig">
          <template #icon>
            <SaveOutlined />
          </template>
          {{ t('personalHomepage.toolbar.saveLayout') }}
        </a-button>
      </a-space>
      <span class="module-homepage-toolbar__hint">
        {{ editMode ? t('personalHomepage.module.toolbar.hint.edit') : t('personalHomepage.module.toolbar.hint.view') }}
      </span>
    </div>

    <div class="module-homepage-content" :class="{ 'module-homepage-content--editing': editMode }">
      <section class="module-homepage-stage" data-guide-id="module-homepage-stage">
        <a-spin :spinning="loading">
          <a-empty v-if="visibleWidgets.length === 0" :description="t('personalHomepage.module.empty')" />
          <GridLayout
            v-else
            v-model:layout="gridLayout"
            class="module-homepage-grid"
            data-guide-id="module-homepage-grid"
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
              <div v-if="$slots[item.i]" class="module-widget module-widget--custom">
                <span v-if="editMode" class="module-widget__drag module-widget__drag--floating">
                  <DragOutlined />
                </span>
                <slot
                  :name="item.i"
                  :widget="getWidgetConfig(item.i)"
                  :edit-mode="editMode"
                />
              </div>
              <article v-else class="module-widget">
                <header class="module-widget__header">
                  <div class="module-widget__title-wrap">
                    <component :is="getWidgetMeta(item.i).icon" class="module-widget__icon" />
                    <div>
                      <h3 class="module-widget__title">{{ getWidgetTitle(item.i) }}</h3>
                      <p class="module-widget__subtitle">{{ getWidgetMeta(item.i).subtitle }}</p>
                    </div>
                  </div>
                  <span v-if="editMode" class="module-widget__drag">
                    <DragOutlined />
                  </span>
                </header>

                <div class="module-widget__body">
                  <div class="module-widget__summary">
                    <span>{{ getWidgetMeta(item.i).summary }}</span>
                    <a-button
                      v-if="getWidgetMeta(item.i).path"
                      :data-guide-id="`module-homepage-widget-enter-${item.i}`"
                      type="link"
                      size="small"
                      @click="openPath(getWidgetMeta(item.i).path)"
                    >
                      {{ t('personalHomepage.module.action.enter') }}
                      <template #icon>
                        <ArrowRightOutlined />
                      </template>
                    </a-button>
                  </div>
                  <div class="module-widget__stats">
                    <div
                      v-for="stat in getWidgetMeta(item.i).stats"
                      :key="stat.label"
                      class="module-widget__stat"
                    >
                      <span>{{ stat.label }}</span>
                      <strong>{{ stat.value }}</strong>
                    </div>
                  </div>
                </div>
              </article>
            </GridItem>
          </GridLayout>
        </a-spin>
      </section>

      <aside v-if="editMode" class="module-homepage-panel">
        <div class="module-homepage-panel__card">
          <div class="module-homepage-panel__header">
            <h3>{{ t('personalHomepage.panel.title') }}</h3>
            <span>{{ visibleWidgets.length }}/{{ configurableWidgets.length }}</span>
          </div>
          <div class="module-homepage-widget-settings">
            <div
              v-for="widget in configurableWidgets"
              :key="widget.key"
              class="module-homepage-widget-setting"
            >
              <div class="module-homepage-widget-setting__top">
                <span>
                  <component :is="getWidgetMeta(widget.key).icon" />
                  {{ getWidgetTitle(widget.key) }}
                </span>
                <a-switch :checked="widget.visible" @change="updateWidgetVisibility(widget.key, $event)" />
              </div>
              <div class="module-homepage-widget-setting__fields">
                <label>
                  {{ t('personalHomepage.module.panel.width') }}
                  <a-input-number
                    :value="widget.w"
                    :min="widget.minW || 1"
                    :max="currentColNum"
                    size="small"
                    @change="value => updateWidgetSize(widget.key, 'w', value)"
                  />
                </label>
                <label>
                  {{ t('personalHomepage.module.panel.height') }}
                  <a-input-number
                    :value="widget.h"
                    :min="widget.minH || 1"
                    :max="8"
                    size="small"
                    @change="value => updateWidgetSize(widget.key, 'h', value)"
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
import type { Component } from 'vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import {
  ApartmentOutlined,
  AppstoreOutlined,
  ArrowRightOutlined,
  AuditOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  CodeOutlined,
  DashboardOutlined,
  DragOutlined,
  FileTextOutlined,
  HddOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
  SaveOutlined,
  SettingOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UndoOutlined,
} from '@ant-design/icons-vue'
import { GridItem, GridLayout } from 'vue-grid-layout-v3'
import {
  createDefaultModuleHomepageConfig,
  getCurrentPersonalHomepageConfig,
  getManagePersonalHomepageConfig,
  mergeModuleHomepageConfig,
  resetCurrentPersonalHomepageConfig,
  saveCurrentPersonalHomepageConfig,
  saveManagePersonalHomepageConfig,
  type ModuleHomepageCode,
  type PersonalHomepageConfig,
  type PersonalHomepageScopeLevel,
} from '@/api/system/personalHomepage'

interface Props {
  moduleCode: ModuleHomepageCode | string
  mode?: 'current' | 'manage'
  title?: string
  description?: string
  initialScopeLevel?: Exclude<PersonalHomepageScopeLevel, 'USER'>
  showScopeSelector?: boolean
  initialEditMode?: boolean
  hiddenWidgetKeys?: string[]
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

interface WidgetMeta {
  title: string
  subtitle: string
  summary: string
  icon: Component
  path?: string
  stats: Array<{ label: string; value: string }>
}

const props = withDefaults(defineProps<Props>(), {
  mode: 'current',
  title: '',
  description: '',
  initialScopeLevel: 'TENANT',
  showScopeSelector: false,
  initialEditMode: false,
  hiddenWidgetKeys: () => [],
})

const emit = defineEmits<{
  layoutUpdated: []
}>()

const router = useRouter()
const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const editMode = ref(props.initialEditMode)
const scopeLevel = ref<Exclude<PersonalHomepageScopeLevel, 'USER'>>(props.initialScopeLevel)
const viewportWidth = ref(typeof window === 'undefined' ? 1440 : window.innerWidth)
const config = ref<PersonalHomepageConfig>(createDefaultModuleHomepageConfig(props.moduleCode))
const gridLayout = ref<GridLayoutItem[]>([])
const syncingGrid = ref(false)

const normalizedModuleCode = computed<ModuleHomepageCode>(() => normalizeModuleCode(props.moduleCode))

const moduleMeta = computed(() => {
  const map: Record<ModuleHomepageCode, { name: string; title: string; desc: string }> = {
    personal: {
      name: t('personalHomepage.module.modules.personal.name'),
      title: t('personalHomepage.module.modules.personal.title'),
      desc: t('personalHomepage.module.modules.personal.desc'),
    },
    basic: {
      name: t('personalHomepage.module.modules.basic.name'),
      title: t('personalHomepage.module.modules.basic.title'),
      desc: t('personalHomepage.module.modules.basic.desc'),
    },
    approval: {
      name: t('personalHomepage.module.modules.approval.name'),
      title: t('personalHomepage.module.modules.approval.title'),
      desc: t('personalHomepage.module.modules.approval.desc'),
    },
    sys: {
      name: t('personalHomepage.module.modules.sys.name'),
      title: t('personalHomepage.module.modules.sys.title'),
      desc: t('personalHomepage.module.modules.sys.desc'),
    },
  }
  return map[normalizedModuleCode.value] || map.basic
})

const resolvedTitle = computed(() => props.title || moduleMeta.value.title)
const resolvedDescription = computed(() => props.description || moduleMeta.value.desc)
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

const hiddenWidgetKeySet = computed(() => new Set(props.hiddenWidgetKeys || []))
const configurableWidgets = computed(() => orderedWidgets.value.filter(widget => !hiddenWidgetKeySet.value.has(widget.key)))
const visibleWidgets = computed(() => configurableWidgets.value.filter(widget => widget.visible))

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

function normalizeModuleCode(moduleCode?: string | null): ModuleHomepageCode {
  const normalized = String(moduleCode || 'personal').trim().toLowerCase()
  if (!normalized || normalized === 'personal') {
    return 'personal'
  }
  if (normalized === 'system') {
    return 'sys'
  }
  if (normalized === 'workflow') {
    return 'approval'
  }
  if (normalized === 'basic' || normalized === 'approval' || normalized === 'sys') {
    return normalized
  }
  return 'basic'
}

function getWidgetMeta(widgetKey: string): WidgetMeta {
  const metaMap: Record<string, WidgetMeta> = {
    supplierInfo: {
      title: t('personalHomepage.module.widgets.supplierInfo.title'),
      subtitle: t('personalHomepage.module.widgets.supplierInfo.subtitle'),
      summary: t('personalHomepage.module.widgets.supplierInfo.summary'),
      icon: ApartmentOutlined,
      path: '/workspace/basic/supplier',
      stats: [
        { label: t('personalHomepage.module.stats.masterData'), value: t('personalHomepage.module.stats.supplierArchive') },
        { label: t('personalHomepage.module.stats.approval'), value: t('personalHomepage.module.stats.admissionChange') },
      ],
    },
    encodeRuleInfo: {
      title: t('personalHomepage.module.widgets.encodeRuleInfo.title'),
      subtitle: t('personalHomepage.module.widgets.encodeRuleInfo.subtitle'),
      summary: t('personalHomepage.module.widgets.encodeRuleInfo.summary'),
      icon: CodeOutlined,
      path: '/workspace/basic/encodeRule',
      stats: [
        { label: t('personalHomepage.module.stats.rule'), value: t('personalHomepage.module.stats.byModule') },
        { label: t('personalHomepage.module.stats.capability'), value: t('personalHomepage.module.stats.testGenerate') },
      ],
    },
    systemOverview: {
      title: t('personalHomepage.module.widgets.systemOverview.title'),
      subtitle: t('personalHomepage.module.widgets.systemOverview.subtitle'),
      summary: t('personalHomepage.module.widgets.systemOverview.summary'),
      icon: DashboardOutlined,
      path: '/workspace/sys/user',
      stats: [
        { label: t('personalHomepage.module.stats.user'), value: t('personalHomepage.module.stats.accountManage') },
        { label: t('personalHomepage.module.stats.role'), value: t('personalHomepage.module.stats.authConfig') },
      ],
    },
    systemHealth: {
      title: t('personalHomepage.module.widgets.systemHealth.title'),
      subtitle: t('personalHomepage.module.widgets.systemHealth.subtitle'),
      summary: t('personalHomepage.module.widgets.systemHealth.summary'),
      icon: SafetyCertificateOutlined,
      path: '/workspace/sys/online',
      stats: [
        { label: t('personalHomepage.module.stats.status'), value: t('personalHomepage.module.stats.onlineSession') },
        { label: t('personalHomepage.module.stats.security'), value: t('personalHomepage.module.stats.loginAudit') },
      ],
    },
    systemLogs: {
      title: t('personalHomepage.module.widgets.systemLogs.title'),
      subtitle: t('personalHomepage.module.widgets.systemLogs.subtitle'),
      summary: t('personalHomepage.module.widgets.systemLogs.summary'),
      icon: FileTextOutlined,
      path: '/workspace/sys/log/operation',
      stats: [
        { label: t('personalHomepage.module.stats.audit'), value: t('personalHomepage.module.stats.operationLog') },
        { label: t('personalHomepage.module.stats.trace'), value: t('personalHomepage.module.stats.loginRecord') },
      ],
    },
    systemConfig: {
      title: t('personalHomepage.module.widgets.systemConfig.title'),
      subtitle: t('personalHomepage.module.widgets.systemConfig.subtitle'),
      summary: t('personalHomepage.module.widgets.systemConfig.summary'),
      icon: SettingOutlined,
      path: '/workspace/sys/config',
      stats: [
        { label: t('personalHomepage.module.stats.scope'), value: t('personalHomepage.module.stats.publicTenant') },
        { label: t('personalHomepage.module.stats.config'), value: t('personalHomepage.module.stats.systemParams') },
      ],
    },
    approvalStats: {
      title: t('personalHomepage.module.widgets.approvalStats.title'),
      subtitle: t('personalHomepage.module.widgets.approvalStats.subtitle'),
      summary: t('personalHomepage.module.widgets.approvalStats.summary'),
      icon: AuditOutlined,
      path: '/workspace/approval/my/pending',
      stats: [
        { label: t('personalHomepage.module.stats.pending'), value: t('personalHomepage.module.stats.myTasks') },
        { label: t('personalHomepage.module.stats.processed'), value: t('personalHomepage.module.stats.processRecord') },
      ],
    },
    approvalShortcuts: {
      title: t('personalHomepage.module.widgets.approvalShortcuts.title'),
      subtitle: t('personalHomepage.module.widgets.approvalShortcuts.subtitle'),
      summary: t('personalHomepage.module.widgets.approvalShortcuts.summary'),
      icon: ThunderboltOutlined,
      path: '/workspace/approval/execution/start',
      stats: [
        { label: t('personalHomepage.module.stats.entry'), value: t('personalHomepage.module.stats.startApproval') },
        { label: t('personalHomepage.module.stats.flow'), value: t('personalHomepage.module.stats.taskTemplate') },
      ],
    },
    approvalPending: {
      title: t('personalHomepage.module.widgets.approvalPending.title'),
      subtitle: t('personalHomepage.module.widgets.approvalPending.subtitle'),
      summary: t('personalHomepage.module.widgets.approvalPending.summary'),
      icon: CheckCircleOutlined,
      path: '/workspace/approval/my/pending',
      stats: [
        { label: t('personalHomepage.module.stats.status'), value: t('personalHomepage.module.stats.pendingApproval') },
        { label: t('personalHomepage.module.stats.action'), value: t('personalHomepage.module.stats.approveReject') },
      ],
    },
    approvalTaskConfig: {
      title: t('personalHomepage.module.widgets.approvalTaskConfig.title'),
      subtitle: t('personalHomepage.module.widgets.approvalTaskConfig.subtitle'),
      summary: t('personalHomepage.module.widgets.approvalTaskConfig.summary'),
      icon: TeamOutlined,
      path: '/workspace/approval/taskConfig',
      stats: [
        { label: t('personalHomepage.module.stats.task'), value: t('personalHomepage.module.stats.flowConfig') },
        { label: t('personalHomepage.module.stats.node'), value: t('personalHomepage.module.stats.approvalRule') },
      ],
    },
  }
  const widget = config.value.widgets.find(item => item.key === widgetKey)
  return metaMap[widgetKey] || {
    title: widget?.title || widgetKey,
    subtitle: t('personalHomepage.module.widgets.custom.subtitle'),
    summary: t('personalHomepage.module.widgets.custom.summary'),
    icon: AppstoreOutlined,
    stats: [
      { label: t('personalHomepage.module.stats.status'), value: t('personalHomepage.module.stats.enabled') },
      { label: t('personalHomepage.module.stats.type'), value: t('personalHomepage.module.stats.extension') },
    ],
  }
}

function getWidgetTitle(widgetKey: string) {
  return getWidgetMeta(widgetKey).title
}

function getWidgetConfig(widgetKey: string) {
  return config.value.widgets.find(item => item.key === widgetKey)
}

function emitLayoutUpdated() {
  requestAnimationFrame(() => {
    emit('layoutUpdated')
  })
}

function syncGridFromConfig() {
  syncingGrid.value = true
  const colNum = Math.max(currentColNum.value, 1)
  gridLayout.value = visibleWidgets.value.map(widget => ({
    i: widget.key,
    x: Math.min(Math.max(Number(widget.x ?? 0), 0), Math.max(colNum - 1, 0)),
    y: Math.max(Number(widget.y ?? 0), 0),
    w: Math.min(Math.max(Number(widget.w ?? 6), Number(widget.minW ?? 1)), colNum),
    h: Math.max(Number(widget.h ?? 4), Number(widget.minH ?? 1)),
    minW: Math.max(Number(widget.minW ?? 1), 1),
    minH: Math.max(Number(widget.minH ?? 1), 1),
  }))
  requestAnimationFrame(() => {
    syncingGrid.value = false
    emit('layoutUpdated')
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
  emitLayoutUpdated()
}

async function reloadConfig() {
  loading.value = true
  try {
    const options = { moduleCode: normalizedModuleCode.value }
    const remoteConfig = props.mode === 'current'
      ? await getCurrentPersonalHomepageConfig(options)
      : await getManagePersonalHomepageConfig(scopeLevel.value, options)
    config.value = mergeModuleHomepageConfig(remoteConfig, normalizedModuleCode.value)
    syncGridFromConfig()
  } catch (error) {
    console.error('加载模块首页配置失败:', error)
    config.value = createDefaultModuleHomepageConfig(normalizedModuleCode.value)
    syncGridFromConfig()
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  saving.value = true
  try {
    const payload = mergeModuleHomepageConfig(config.value, normalizedModuleCode.value)
    const options = { moduleCode: normalizedModuleCode.value }
    if (props.mode === 'current') {
      await saveCurrentPersonalHomepageConfig(payload, options)
    } else {
      await saveManagePersonalHomepageConfig(scopeLevel.value, payload, options)
    }
    config.value = payload
    syncGridFromConfig()
    message.success(t('personalHomepage.module.message.saveSuccess'))
  } catch (error) {
    console.error('保存模块首页配置失败:', error)
    message.error(t('personalHomepage.module.message.saveFailed'))
  } finally {
    saving.value = false
  }
}

async function resetToDefault() {
  saving.value = true
  try {
    await resetCurrentPersonalHomepageConfig({ moduleCode: normalizedModuleCode.value })
    await reloadConfig()
    message.success(t('personalHomepage.message.resetSuccess'))
  } catch (error) {
    console.error('恢复模块首页默认布局失败:', error)
    message.error(t('personalHomepage.message.resetFailed'))
  } finally {
    saving.value = false
  }
}

function toggleEditMode() {
  editMode.value = !editMode.value
  requestAnimationFrame(() => {
    syncGridFromConfig()
  })
}

function updateWidgetVisibility(widgetKey: string, checked: any) {
  config.value.widgets = config.value.widgets.map(widget => (
    widget.key === widgetKey ? { ...widget, visible: checked === true } : widget
  ))
  syncGridFromConfig()
}

function updateWidgetSize(widgetKey: string, field: 'w' | 'h', value: number | string | null) {
  const numericValue = Math.max(Number(value || 1), 1)
  config.value.widgets = config.value.widgets.map(widget => (
    widget.key === widgetKey ? { ...widget, [field]: numericValue } : widget
  ))
  syncGridFromConfig()
}

function openPath(path?: string) {
  if (!path) {
    return
  }
  router.push(path).catch(() => {})
}

function handleResize() {
  viewportWidth.value = window.innerWidth
}

watch(scopeLevel, () => {
  if (props.mode === 'manage') {
    reloadConfig()
  }
})

watch(
  () => props.moduleCode,
  () => {
    config.value = createDefaultModuleHomepageConfig(normalizedModuleCode.value)
    reloadConfig()
  },
)

watch(
  () => currentColNum.value,
  () => {
    syncGridFromConfig()
  },
)

watch(
  () => props.hiddenWidgetKeys,
  () => {
    syncGridFromConfig()
  },
  { deep: true },
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

onMounted(() => {
  reloadConfig()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.module-homepage-designer {
  min-height: 100%;
  padding: 20px;
  color: var(--fx-text-primary, #111827);
}

.module-homepage-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 18px;
  margin-bottom: 16px;
  padding: 22px 24px;
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 84%, transparent);
  border-radius: 8px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 12px 28px rgba(15, 23, 42, 0.08));
}

.module-homepage-hero__eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  color: var(--fx-primary, #1677ff);
  font-weight: 600;
}

.module-homepage-hero__title {
  margin: 0 0 8px;
  font-size: 24px;
  line-height: 1.25;
}

.module-homepage-hero__desc {
  margin: 0;
  color: var(--fx-text-secondary, #6b7280);
}

.module-homepage-hero__meta {
  display: inline-flex;
  flex-wrap: wrap;
  gap: 8px;
}

.module-homepage-badge {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 10px;
  border-radius: 6px;
  color: var(--fx-primary, #1677ff);
  background: color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent);
  font-size: 12px;
  font-weight: 600;
}

.module-homepage-badge--soft {
  color: var(--fx-text-secondary, #6b7280);
  background: var(--fx-bg-elevated, #f8fafc);
}

.module-homepage-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 8px;
  background: var(--fx-bg-container, #ffffff);
}

.module-homepage-toolbar__hint {
  color: var(--fx-text-secondary, #6b7280);
  font-size: 13px;
}

.module-homepage-content {
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  gap: 16px;
  align-items: start;
}

.module-homepage-content--editing {
  grid-template-columns: minmax(0, 1fr) 320px;
}

.module-homepage-stage {
  min-width: 0;
}

.module-homepage-grid {
  min-height: 360px;
}

.module-widget {
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--fx-border-color, #e5e7eb) 84%, transparent);
  border-radius: 8px;
  background: var(--fx-bg-container, #ffffff);
  box-shadow: var(--fx-shadow-secondary, 0 10px 24px rgba(15, 23, 42, 0.07));
}

.module-widget__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--fx-border-color, #e5e7eb);
}

.module-widget__title-wrap {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.module-widget__icon,
.module-widget__drag {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: color-mix(in srgb, var(--fx-primary, #1677ff) 10%, transparent);
  color: var(--fx-primary, #1677ff);
  flex: 0 0 auto;
}

.module-widget__drag {
  background: var(--fx-bg-elevated, #f8fafc);
  color: var(--fx-text-secondary, #6b7280);
}

.module-widget--custom {
  position: relative;
  overflow: visible;
  border: 0;
  background: transparent;
  box-shadow: none;
}

.module-widget--custom > :deep(*) {
  min-width: 0;
  height: 100%;
}

.module-widget__drag--floating {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 5;
  cursor: move;
  box-shadow: var(--fx-shadow-secondary, 0 8px 18px rgba(15, 23, 42, 0.12));
}

.module-widget__title {
  margin: 0;
  font-size: 15px;
  line-height: 1.35;
}

.module-widget__subtitle {
  margin: 4px 0 0;
  color: var(--fx-text-secondary, #6b7280);
  font-size: 12px;
}

.module-widget__body {
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: 0;
  padding: 16px 18px 18px;
}

.module-widget__summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  color: var(--fx-text-secondary, #6b7280);
  line-height: 1.6;
}

.module-widget__stats {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.module-widget__stat {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
  padding: 12px;
  border-radius: 8px;
  background: var(--fx-bg-elevated, #f8fafc);
}

.module-widget__stat span {
  color: var(--fx-text-secondary, #6b7280);
  font-size: 12px;
}

.module-widget__stat strong {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 15px;
}

.module-homepage-panel {
  position: sticky;
  top: 12px;
}

.module-homepage-panel__card {
  border: 1px solid var(--fx-border-color, #e5e7eb);
  border-radius: 8px;
  background: var(--fx-bg-container, #ffffff);
}

.module-homepage-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  border-bottom: 1px solid var(--fx-border-color, #e5e7eb);
}

.module-homepage-panel__header h3 {
  margin: 0;
  font-size: 15px;
}

.module-homepage-widget-settings {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 14px;
}

.module-homepage-widget-setting {
  padding: 12px;
  border-radius: 8px;
  background: var(--fx-bg-elevated, #f8fafc);
}

.module-homepage-widget-setting__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.module-homepage-widget-setting__top span {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  font-weight: 600;
}

.module-homepage-widget-setting__fields {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.module-homepage-widget-setting__fields label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: var(--fx-text-secondary, #6b7280);
  font-size: 12px;
}

@media (max-width: 1280px) {
  .module-homepage-content,
  .module-homepage-content--editing {
    grid-template-columns: 1fr;
  }

  .module-homepage-panel {
    position: static;
  }
}

@media (max-width: 768px) {
  .module-homepage-designer {
    padding: 12px;
  }

  .module-homepage-hero,
  .module-homepage-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
