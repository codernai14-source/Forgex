<template>
  <div class="module-homepage-designer">
    <div class="module-homepage-hero" data-guide-id="module-homepage-hero">
      <div>
        <p class="module-homepage-hero__eyebrow">{{ moduleMeta.name }}</p>
        <h2 class="module-homepage-hero__title">{{ resolvedTitle }}</h2>
        <p class="module-homepage-hero__desc">{{ resolvedDescription }}</p>
      </div>
      <div class="module-homepage-hero__actions">
        <a-select
          v-if="mode === 'manage' && showScopeSelector"
          v-model:value="scopeLevel"
          class="module-homepage-hero__scope"
        >
          <a-select-option value="TENANT">{{ t('personalHomepage.hero.badge.tenant') }}</a-select-option>
          <a-select-option value="PUBLIC">{{ t('personalHomepage.hero.badge.public') }}</a-select-option>
        </a-select>
        <a-space wrap>
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
          <a-button v-if="editMode" :loading="sharing" @click="createShareCode">
            <template #icon>
              <ShareAltOutlined />
            </template>
            {{ t('personalHomepage.share.create') }}
          </a-button>
          <a-button v-if="editMode" @click="openImportLayout">
            <template #icon>
              <ImportOutlined />
            </template>
            {{ t('personalHomepage.share.import') }}
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
      </div>
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
                  <div v-if="isBasicBusinessWidget(item.i)" class="module-widget__business">
                    <template v-if="item.i === 'supplierInfo'">
                      <div v-if="supplierPreviewLoading" class="module-widget__loading">
                        <a-skeleton active :paragraph="{ rows: 3 }" />
                      </div>
                      <div v-else-if="supplierPreviewList.length" class="master-preview-list">
                        <div v-for="supplier in supplierPreviewList" :key="supplier.id || supplier.supplierCode" class="master-preview-row">
                          <div class="master-preview-row__main">
                            <strong>{{ supplier.supplierFullName || supplier.supplierName || supplier.supplierCode || '-' }}</strong>
                            <span>{{ supplier.supplierShortName || supplier.supplierCode || '-' }}</span>
                          </div>
                          <div class="master-preview-row__meta">
                            <span>{{ supplier.primaryContact || supplier.contactPerson || '-' }}</span>
                            <a-tag :color="hasSupplierTenant(supplier) ? 'green' : 'default'">
                              {{ hasSupplierTenant(supplier) ? '已协作' : '未协作' }}
                            </a-tag>
                          </div>
                        </div>
                      </div>
                      <a-empty v-else class="module-widget__empty" :description="'暂无供应商档案'" />
                    </template>

                    <template v-else-if="item.i === 'customerInfo'">
                      <div v-if="customerPreviewLoading" class="module-widget__loading">
                        <a-skeleton active :paragraph="{ rows: 3 }" />
                      </div>
                      <div v-else-if="customerPreviewList.length" class="master-preview-list">
                        <div v-for="customer in customerPreviewList" :key="customer.id || customer.customerCode" class="master-preview-row">
                          <div class="master-preview-row__main">
                            <strong>{{ customer.customerFullName || customer.customerName || customer.customerCode || '-' }}</strong>
                            <span>{{ customer.customerShortName || customer.customerCode || '-' }}</span>
                          </div>
                          <div class="master-preview-row__meta">
                            <span>{{ firstCustomerContact(customer) }}</span>
                            <a-tag :color="hasCustomerTenant(customer) ? 'green' : 'default'">
                              {{ hasCustomerTenant(customer) ? '已协作' : '未协作' }}
                            </a-tag>
                          </div>
                        </div>
                      </div>
                      <a-empty v-else class="module-widget__empty" :description="'暂无客户档案'" />
                    </template>

                    <template v-else-if="item.i === 'workCalendarInfo'">
                      <div v-if="calendarPreviewLoading" class="module-widget__loading">
                        <a-skeleton active :paragraph="{ rows: 4 }" />
                      </div>
                      <div v-else class="calendar-preview">
                        <div class="calendar-preview__summary">
                          <strong>{{ calendarPreviewTitle }}</strong>
                          <span>工作日 {{ calendarPreviewCounts.workday }} 天 · 假日 {{ calendarPreviewCounts.offday }} 天 · 调班 {{ calendarPreviewCounts.makeup }} 天</span>
                        </div>
                        <div class="calendar-preview__weekdays">
                          <span v-for="weekday in calendarWeekdays" :key="weekday">{{ weekday }}</span>
                        </div>
                        <div class="calendar-preview__grid">
                          <div
                            v-for="cell in calendarPreviewCells"
                            :key="cell.date"
                            class="calendar-preview__cell"
                            :class="[
                              `calendar-preview__cell--${cell.typeMeta.tone}`,
                              {
                                'calendar-preview__cell--muted': !cell.currentMonth,
                                'calendar-preview__cell--today': cell.isToday,
                                'calendar-preview__cell--has-event': cell.eventCount,
                              },
                            ]"
                            :title="cell.tooltip"
                          >
                            <span class="calendar-preview__cell-head">
                              <strong>{{ cell.day }}</strong>
                              <em>{{ cell.typeMeta.label }}</em>
                            </span>
                            <span class="calendar-preview__cell-detail">{{ cell.detail }}</span>
                            <i v-if="cell.eventCount" class="calendar-preview__event-count">{{ cell.eventCount }}</i>
                          </div>
                        </div>
                      </div>
                    </template>
                  </div>
                  <div v-else class="module-widget__stats">
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

    <a-modal v-model:open="shareModalOpen" :title="t('personalHomepage.share.shareTitle')" :footer="null">
      <a-input-group compact>
        <a-input v-model:value="shareCode" readonly style="width: calc(100% - 88px)" />
        <a-button style="width: 88px" @click="copyShareCode">
          {{ t('personalHomepage.share.copy') }}
        </a-button>
      </a-input-group>
    </a-modal>

    <a-modal
      v-model:open="importModalOpen"
      :title="t('personalHomepage.share.importTitle')"
      :ok-text="t('personalHomepage.share.apply')"
      :ok-button-props="{ disabled: !importPreview }"
      :confirm-loading="importLoading"
      @ok="applyImportLayout"
    >
      <a-space direction="vertical" style="width: 100%" :size="12">
        <a-input-search
          v-model:value="importCode"
          allow-clear
          :placeholder="t('personalHomepage.share.inputPlaceholder')"
          :loading="importLoading"
          @search="previewImportLayout"
        />
        <a-descriptions v-if="importPreview" size="small" bordered :column="1">
          <a-descriptions-item :label="t('personalHomepage.share.shareCode')">
            {{ importPreview.shareCode }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('personalHomepage.share.moduleCode')">
            {{ importPreview.moduleCode }}
          </a-descriptions-item>
          <a-descriptions-item :label="t('personalHomepage.share.createTime')">
            {{ importPreview.createTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>
      </a-space>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import type { Component } from 'vue'
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import {
  ApartmentOutlined,
  ApiOutlined,
  AppstoreOutlined,
  ArrowRightOutlined,
  AuditOutlined,
  BarChartOutlined,
  CalendarOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DashboardOutlined,
  DragOutlined,
  FileTextOutlined,
  HddOutlined,
  ImportOutlined,
  LineChartOutlined,
  OrderedListOutlined,
  PieChartOutlined,
  ReloadOutlined,
  SafetyCertificateOutlined,
  SaveOutlined,
  SettingOutlined,
  ShareAltOutlined,
  TeamOutlined,
  ThunderboltOutlined,
  UndoOutlined,
  WarningOutlined,
} from '@ant-design/icons-vue'
import { GridItem, GridLayout } from 'vue-grid-layout-v3'
import {
  createHomepageLayoutShare,
  createDefaultModuleHomepageConfig,
  getCurrentPersonalHomepageConfig,
  getManagePersonalHomepageConfig,
  mergeModuleHomepageConfig,
  previewHomepageLayoutShare,
  resetCurrentPersonalHomepageConfig,
  saveCurrentPersonalHomepageConfig,
  saveManagePersonalHomepageConfig,
  type HomepageLayoutShareVO,
  type ModuleHomepageCode,
  type PersonalHomepageConfig,
  type PersonalHomepageScopeLevel,
} from '@/api/system/personalHomepage'
import { supplierApi, type Supplier } from '@/api/basic/supplier'
import { customerApi, type Customer } from '@/api/basic/customer'
import { workCalendarApi, type WorkCalendarDay, type WorkCalendarEvent } from '@/api/basic/workCalendar'

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
const shareModalOpen = ref(false)
const shareCode = ref('')
const sharing = ref(false)
const importModalOpen = ref(false)
const importCode = ref('')
const importPreview = ref<HomepageLayoutShareVO | null>(null)
const importLoading = ref(false)
const supplierPreviewList = ref<Supplier[]>([])
const customerPreviewList = ref<Customer[]>([])
const calendarPreviewDays = ref<WorkCalendarDay[]>([])
const calendarPreviewEvents = ref<WorkCalendarEvent[]>([])
const supplierPreviewLoading = ref(false)
const customerPreviewLoading = ref(false)
const calendarPreviewLoading = ref(false)

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
      name: t('approval.title'),
      title: t('approval.title'),
      desc: t('personalHomepage.module.modules.approval.desc'),
    },
    sys: {
      name: t('sys.title'),
      title: t('sys.title'),
      desc: t('personalHomepage.module.modules.sys.desc'),
    },
    integration: {
      name: t('integration.title'),
      title: t('integration.home.title'),
      desc: t('personalHomepage.module.modules.integration.desc'),
    },
  }
  return map[normalizedModuleCode.value] || map.basic
})

const resolvedTitle = computed(() => props.title || moduleMeta.value.title)
const resolvedDescription = computed(() => props.description || moduleMeta.value.desc)

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

const currentRowHeight = computed(() => config.value.layout.rowHeight || 64)

const gridMargin = computed<[number, number]>(() => [
  config.value.layout.marginX || 10,
  config.value.layout.marginY || 10,
])

const today = dayjs()
const calendarWeekdays = ['一', '二', '三', '四', '五', '六', '日']
const calendarDateTypeMetaMap: Record<number, { label: string; tone: string; group: 'workday' | 'offday' | 'makeup' }> = {
  1: { label: '工作日', tone: 'workday', group: 'workday' },
  2: { label: '周末', tone: 'rest', group: 'offday' },
  3: { label: '法定假', tone: 'holiday', group: 'offday' },
  4: { label: '调班', tone: 'makeup', group: 'makeup' },
  5: { label: '自休', tone: 'rest', group: 'offday' },
  6: { label: '活动', tone: 'activity', group: 'workday' },
}
const calendarPreviewTitle = computed(() => `${today.year()} 年 ${today.month() + 1} 月`)
const calendarPreviewDayMap = computed(() => Object.fromEntries(calendarPreviewDays.value.map(item => [item.calendarDate, item])))
const calendarPreviewEventMap = computed<Record<string, WorkCalendarEvent[]>>(() => {
  const map: Record<string, WorkCalendarEvent[]> = {}
  calendarPreviewEvents.value.forEach(event => {
    const start = dayjs(event.startTime)
    const end = dayjs(event.endTime)
    if (!start.isValid() || !end.isValid()) {
      return
    }
    let cursor = start.startOf('day')
    const last = end.startOf('day')
    while (cursor.isBefore(last) || cursor.isSame(last)) {
      const key = cursor.format('YYYY-MM-DD')
      if (!map[key]) {
        map[key] = []
      }
      map[key].push(event)
      cursor = cursor.add(1, 'day')
    }
  })
  return map
})
const calendarPreviewCells = computed(() => {
  const first = today.startOf('month')
  const gridStart = first.subtract((first.day() + 6) % 7, 'day')
  return Array.from({ length: 42 }).map((_, index) => {
    const date = gridStart.add(index, 'day')
    const key = date.format('YYYY-MM-DD')
    const dayInfo = calendarPreviewDayMap.value[key]
    const events = calendarPreviewEventMap.value[key] || []
    const typeMeta = calendarDateTypeMeta(dayInfo?.dateType, date)
    const eventTitle = events[0]?.eventTitle
    const detail = dayInfo?.holidayName || eventTitle || dayInfo?.publicWeek || typeMeta.label
    return {
      date: key,
      day: date.date(),
      currentMonth: date.month() === today.month(),
      isToday: date.isSame(today, 'day'),
      dayInfo,
      detail,
      eventCount: events.length,
      typeMeta,
      tooltip: [key, typeMeta.label, dayInfo?.holidayName, eventTitle, events.length > 1 ? `共 ${events.length} 个事件` : '']
        .filter(Boolean)
        .join(' · '),
    }
  })
})
const calendarPreviewCounts = computed(() => {
  return calendarPreviewDays.value.reduce((counts, item) => {
    counts[calendarDateTypeMeta(item.dateType).group] += 1
    return counts
  }, { workday: 0, offday: 0, makeup: 0 })
})

function normalizeModuleCode(code: string): ModuleHomepageCode {
  const normalized = String(code || 'basic').trim().toLowerCase()
  if (normalized === 'sys' || normalized === 'system') {
    return 'sys'
  }
  if (normalized === 'approval' || normalized === 'workflow') {
    return 'approval'
  }
  if (normalized === 'personal') {
    return 'personal'
  }
  if (normalized === 'integration') {
    return 'integration'
  }
  if (normalized === 'basic') {
    return 'basic'
  }
  return 'basic'
}

function isBasicBusinessWidget(widgetKey: string) {
  return ['supplierInfo', 'customerInfo', 'workCalendarInfo'].includes(widgetKey)
}

function hasSupplierTenant(supplier: Supplier) {
  return Boolean(supplier.hasRelatedTenant || supplier.relatedTenantCode)
}

function hasCustomerTenant(customer: Customer) {
  return Boolean(customer.hasRelatedTenant || customer.isRelatedTenant || customer.relatedTenantCode)
}

function firstCustomerContact(customer: Customer) {
  return customer.contactList?.[0]?.contactName || '-'
}

function calendarDateTypeMeta(type?: number, date = today) {
  const normalizedType = Number(type)
  if (calendarDateTypeMetaMap[normalizedType]) {
    return calendarDateTypeMetaMap[normalizedType]
  }
  const isWeekend = [0, 6].includes(date.day())
  return isWeekend ? calendarDateTypeMetaMap[2] : calendarDateTypeMetaMap[1]
}

async function loadBasicWidgetPreviewData() {
  if (normalizedModuleCode.value !== 'basic') {
    return
  }
  await Promise.allSettled([
    loadSupplierPreview(),
    loadCustomerPreview(),
    loadCalendarPreview(),
  ])
}

async function loadSupplierPreview() {
  supplierPreviewLoading.value = true
  try {
    const result = await supplierApi.page({ pageNum: 1, pageSize: 3 })
    supplierPreviewList.value = result.records || []
  } catch (error) {
    supplierPreviewList.value = []
  } finally {
    supplierPreviewLoading.value = false
  }
}

async function loadCustomerPreview() {
  customerPreviewLoading.value = true
  try {
    const result: any = await customerApi.page({ pageNum: 1, pageSize: 3 })
    const records: Customer[] = result?.records || []
    const detailResults = await Promise.allSettled(
      records.map(customer => {
        if (!customer.id) {
          return Promise.resolve(customer)
        }
        return customerApi.detail({ id: Number(customer.id) })
      }),
    )
    customerPreviewList.value = records.map((customer, index) => {
      const detailResult = detailResults[index]
      if (detailResult?.status === 'fulfilled' && detailResult.value) {
        return {
          ...customer,
          ...detailResult.value,
        }
      }
      return customer
    })
  } catch (error) {
    customerPreviewList.value = []
  } finally {
    customerPreviewLoading.value = false
  }
}

async function loadCalendarPreview() {
  calendarPreviewLoading.value = true
  try {
    const result = await workCalendarApi.month({
      year: today.year(),
      month: today.month() + 1,
      syncHoliday: true,
      calendarScopes: ['USER', 'TENANT'],
    })
    calendarPreviewDays.value = result.days || []
    calendarPreviewEvents.value = result.events || []
  } catch (error) {
    calendarPreviewDays.value = []
    calendarPreviewEvents.value = []
  } finally {
    calendarPreviewLoading.value = false
  }
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
    customerInfo: {
      title: t('personalHomepage.module.widgets.customerInfo.title'),
      subtitle: t('personalHomepage.module.widgets.customerInfo.subtitle'),
      summary: t('personalHomepage.module.widgets.customerInfo.summary'),
      icon: TeamOutlined,
      path: '/workspace/basic/customer',
      stats: [
        { label: t('personalHomepage.module.stats.masterData'), value: t('personalHomepage.module.stats.customerArchive') },
        { label: t('personalHomepage.module.stats.integration'), value: t('personalHomepage.module.stats.thirdPartySync') },
      ],
    },
    workCalendarInfo: {
      title: t('personalHomepage.module.widgets.workCalendarInfo.title'),
      subtitle: t('personalHomepage.module.widgets.workCalendarInfo.subtitle'),
      summary: t('personalHomepage.module.widgets.workCalendarInfo.summary'),
      icon: CalendarOutlined,
      path: '/workspace/basic/workCalendar',
      stats: [
        { label: t('personalHomepage.module.stats.calendar'), value: t('personalHomepage.module.stats.workdayMaintain') },
        { label: t('personalHomepage.module.stats.event'), value: t('personalHomepage.module.stats.holidayShift') },
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
    integrationSummary: {
      title: t('personalHomepage.module.widgets.integrationSummary.title'),
      subtitle: t('personalHomepage.module.widgets.integrationSummary.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationSummary.summary'),
      icon: DashboardOutlined,
      path: '/workspace/integration/integration',
      stats: [
        { label: t('personalHomepage.module.stats.capability'), value: t('personalHomepage.module.stats.externalApi') },
        { label: t('personalHomepage.module.stats.status'), value: t('personalHomepage.module.stats.enabled') },
      ],
    },
    integrationStatusComparison: {
      title: t('personalHomepage.module.widgets.integrationStatusComparison.title'),
      subtitle: t('personalHomepage.module.widgets.integrationStatusComparison.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationStatusComparison.summary'),
      icon: BarChartOutlined,
      path: '/workspace/integration/apiCallLog',
      stats: [
        { label: t('personalHomepage.module.stats.success'), value: t('personalHomepage.module.stats.callSuccess') },
        { label: t('personalHomepage.module.stats.fail'), value: t('personalHomepage.module.stats.callFail') },
      ],
    },
    integrationStatusPie: {
      title: t('personalHomepage.module.widgets.integrationStatusPie.title'),
      subtitle: t('personalHomepage.module.widgets.integrationStatusPie.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationStatusPie.summary'),
      icon: PieChartOutlined,
      path: '/workspace/integration/apiCallLog',
      stats: [
        { label: t('personalHomepage.module.stats.status'), value: t('personalHomepage.module.stats.callStatus') },
        { label: t('personalHomepage.module.stats.trace'), value: t('personalHomepage.module.stats.callTrace') },
      ],
    },
    integrationCallTrend: {
      title: t('personalHomepage.module.widgets.integrationCallTrend.title'),
      subtitle: t('personalHomepage.module.widgets.integrationCallTrend.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationCallTrend.summary'),
      icon: LineChartOutlined,
      path: '/workspace/integration/apiCallLog',
      stats: [
        { label: t('personalHomepage.module.stats.trend'), value: t('personalHomepage.module.stats.last14Days') },
        { label: t('personalHomepage.module.stats.call'), value: t('personalHomepage.module.stats.callCount') },
      ],
    },
    integrationTopApis: {
      title: t('personalHomepage.module.widgets.integrationTopApis.title'),
      subtitle: t('personalHomepage.module.widgets.integrationTopApis.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationTopApis.summary'),
      icon: OrderedListOutlined,
      path: '/workspace/integration/apiConfig',
      stats: [
        { label: t('personalHomepage.module.stats.api'), value: t('personalHomepage.module.stats.hotApi') },
        { label: t('personalHomepage.module.stats.successRate'), value: t('personalHomepage.module.stats.quality') },
      ],
    },
    integrationRecentFailures: {
      title: t('personalHomepage.module.widgets.integrationRecentFailures.title'),
      subtitle: t('personalHomepage.module.widgets.integrationRecentFailures.subtitle'),
      summary: t('personalHomepage.module.widgets.integrationRecentFailures.summary'),
      icon: WarningOutlined,
      path: '/workspace/integration/apiCallLog',
      stats: [
        { label: t('personalHomepage.module.stats.exception'), value: t('personalHomepage.module.stats.failureTrace') },
        { label: t('personalHomepage.module.stats.audit'), value: t('personalHomepage.module.stats.callAudit') },
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
    loadBasicWidgetPreviewData()
  } catch (error) {
    console.error('加载模块首页配置失败:', error)
    config.value = createDefaultModuleHomepageConfig(normalizedModuleCode.value)
    syncGridFromConfig()
    loadBasicWidgetPreviewData()
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

async function createShareCode() {
  sharing.value = true
  try {
    const payload = mergeModuleHomepageConfig(config.value, normalizedModuleCode.value)
    const result = await createHomepageLayoutShare({
      moduleCode: normalizedModuleCode.value,
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
      moduleCode: normalizedModuleCode.value,
    })
  } finally {
    importLoading.value = false
  }
}

function applyImportLayout() {
  if (!importPreview.value?.config) {
    return
  }
  config.value = mergeModuleHomepageConfig(importPreview.value.config, normalizedModuleCode.value)
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

<style scoped lang="less" src="@/styles/components/module-homepage/module-homepage-designer.less"></style>
