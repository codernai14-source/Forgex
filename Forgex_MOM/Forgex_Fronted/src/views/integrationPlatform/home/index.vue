<template>
  <div ref="homeRootRef" class="integration-home">
    <ModuleHomepageDesigner module-code="integration" @layout-updated="handleLayoutUpdated">
      <template #integrationSummary>
        <section class="integration-widget integration-summary-widget">
          <header class="integration-widget__header integration-widget__header--summary">
            <div class="integration-widget__title-block">
              <span class="integration-widget__eyebrow">{{ t('integration.home.rangeLabel') }}</span>
              <h2>{{ t('integration.home.title') }}</h2>
            </div>
            <a-button class="integration-widget__refresh" size="small" :loading="loading" @click="loadOverview">
              <template #icon>
                <ReloadOutlined />
              </template>
              {{ t('integration.home.refresh') }}
            </a-button>
          </header>

          <div class="integration-summary-grid">
            <article class="integration-metric">
              <div class="integration-metric__icon integration-metric__icon--system">
                <ApartmentOutlined />
              </div>
              <div class="integration-metric__content">
                <span>{{ t('integration.home.totalThirdSystems') }}</span>
                <strong>{{ formatNumber(summary.totalThirdSystems) }}</strong>
                <small>{{ t('integration.home.enabledCount', { count: formatNumber(summary.enabledThirdSystems) }) }}</small>
              </div>
            </article>

            <article class="integration-metric">
              <div class="integration-metric__icon integration-metric__icon--api">
                <ApiOutlined />
              </div>
              <div class="integration-metric__content">
                <span>{{ t('integration.home.totalApis') }}</span>
                <strong>{{ formatNumber(summary.totalApis) }}</strong>
                <small>
                  {{ t('integration.home.directionSplit', {
                    inbound: formatNumber(summary.inboundApis),
                    outbound: formatNumber(summary.outboundApis),
                  }) }}
                </small>
              </div>
            </article>

            <article class="integration-metric">
              <div class="integration-metric__icon integration-metric__icon--calls">
                <ClockCircleOutlined />
              </div>
              <div class="integration-metric__content">
                <span>{{ t('integration.home.todayCalls') }}</span>
                <strong>{{ formatNumber(summary.todayCalls) }}</strong>
                <small>{{ t('integration.home.periodCalls', { count: formatNumber(summary.totalCalls) }) }}</small>
              </div>
            </article>

            <article class="integration-metric">
              <div class="integration-metric__icon integration-metric__icon--success">
                <CheckCircleOutlined />
              </div>
              <div class="integration-metric__content">
                <span>{{ t('integration.home.successRate') }}</span>
                <strong>{{ formatPercent(summary.successRate) }}</strong>
                <small>
                  {{ t('integration.home.successFailSplit', {
                    success: formatNumber(summary.successCalls),
                    fail: formatNumber(summary.failCalls),
                  }) }}
                </small>
              </div>
            </article>
          </div>
        </section>
      </template>

      <template #integrationStatusComparison>
        <section class="integration-widget integration-chart-widget">
          <WidgetHeader :title="t('integration.home.statusComparison')" />
          <div class="integration-chart-wrap">
            <VChart :ref="setChartRef" class="integration-chart" :option="statusComparisonOption" autoresize />
          </div>
        </section>
      </template>

      <template #integrationStatusPie>
        <section class="integration-widget integration-chart-widget">
          <WidgetHeader :title="t('integration.home.statusPie')" />
          <div class="integration-chart-wrap">
            <VChart :ref="setChartRef" class="integration-chart" :option="statusPieOption" autoresize />
            <a-empty
              v-if="summary.totalCalls === 0"
              class="integration-chart-empty"
              :description="t('integration.home.noCallData')"
              :image="simpleEmptyImage"
            />
          </div>
        </section>
      </template>

      <template #integrationCallTrend>
        <section class="integration-widget integration-chart-widget">
          <WidgetHeader :title="t('integration.home.callTrend')" />
          <div class="integration-chart-wrap">
            <VChart :ref="setChartRef" class="integration-chart" :option="callTrendOption" autoresize />
          </div>
        </section>
      </template>

      <template #integrationTopApis>
        <section class="integration-widget integration-list-widget">
          <WidgetHeader :title="t('integration.home.topApis')" />
          <div v-if="topApis.length" class="integration-rank-list">
            <div v-for="(item, index) in topApis" :key="`${item.apiConfigId || item.apiCode}-${index}`" class="integration-rank-item">
              <div class="integration-rank-item__index">{{ index + 1 }}</div>
              <div class="integration-rank-item__main">
                <strong :title="item.apiName || item.apiCode">{{ item.apiName || item.apiCode || '-' }}</strong>
                <span :title="item.apiCode">{{ item.apiCode || '-' }}</span>
              </div>
              <a-tag :color="directionColor(item.callDirection)">
                {{ directionLabel(item.callDirection) }}
              </a-tag>
              <div class="integration-rank-item__count">
                <strong>{{ formatNumber(item.totalCalls) }}</strong>
                <span>{{ formatPercent(item.successRate) }}</span>
              </div>
            </div>
          </div>
          <a-empty
            v-else
            class="integration-list-empty"
            :description="t('integration.home.noTopApis')"
            :image="simpleEmptyImage"
          />
        </section>
      </template>

      <template #integrationRecentFailures>
        <section class="integration-widget integration-list-widget">
          <WidgetHeader :title="t('integration.home.recentFailures')" />
          <div v-if="recentFailures.length" class="integration-failure-list">
            <div v-for="item in recentFailures" :key="item.id" class="integration-failure-item">
              <div class="integration-failure-item__main">
                <div class="integration-failure-item__title">
                  <strong :title="item.apiName || item.apiCode">{{ item.apiName || item.apiCode || '-' }}</strong>
                  <a-tag color="red">{{ statusLabel(item.callStatus) }}</a-tag>
                </div>
                <p :title="item.errorMessage">{{ item.errorMessage || t('integration.home.noErrorMessage') }}</p>
              </div>
              <div class="integration-failure-item__meta">
                <span>{{ item.callTime || '-' }}</span>
                <span>{{ formatCost(item.costTimeMs) }}</span>
              </div>
            </div>
          </div>
          <a-empty
            v-else
            class="integration-list-empty"
            :description="t('integration.home.noFailures')"
            :image="simpleEmptyImage"
          />
        </section>
      </template>
    </ModuleHomepageDesigner>
  </div>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onBeforeUpdate, onMounted, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { Empty, message } from 'ant-design-vue'
import {
  ApartmentOutlined,
  ApiOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import ModuleHomepageDesigner from '@/components/module-homepage/ModuleHomepageDesigner.vue'
import {
  getIntegrationDashboardOverview,
  type IntegrationDashboardFailureItem,
  type IntegrationDashboardOverview,
  type IntegrationDashboardSummary,
  type IntegrationDashboardTopApi,
} from '@/api/system/integration'

use([BarChart, LineChart, PieChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const WidgetHeader = defineComponent({
  name: 'IntegrationWidgetHeader',
  props: {
    title: {
      type: String,
      required: true,
    },
  },
  setup(props) {
    return () => h('header', { class: 'integration-widget__header' }, [
      h('h2', props.title),
    ])
  },
})

const { t } = useI18n({ useScope: 'global' })
const simpleEmptyImage = Empty.PRESENTED_IMAGE_SIMPLE
const homeRootRef = ref<HTMLElement | null>(null)
const loading = ref(false)
const overview = ref<IntegrationDashboardOverview>(createEmptyOverview())
const chartRefs = ref<any[]>([])

const summary = computed<IntegrationDashboardSummary>(() => overview.value.summary)
const topApis = computed<IntegrationDashboardTopApi[]>(() => overview.value.topApis || [])
const recentFailures = computed<IntegrationDashboardFailureItem[]>(() => overview.value.recentFailures || [])

const statusComparisonData = computed(() => normalizeChartData(overview.value.statusComparison, ['SUCCESS', 'FAIL']))
const statusPieData = computed(() => normalizeChartData(overview.value.statusPie, ['SUCCESS', 'FAIL']))

const chartTextColor = ref('#6b7280')
const chartPrimaryTextColor = ref('#111827')
const chartGridColor = ref('rgba(148, 163, 184, 0.28)')
const chartTooltipBg = ref('#ffffff')
const chartTooltipBorder = ref('#e5e7eb')
const statusColors: Record<string, string> = {
  SUCCESS: '#25c06d',
  FAIL: '#ff5b5b',
  WAITING: '#f6b23f',
  QUEUED: '#4fa3ff',
  RUNNING: '#26c6da',
  RETRY: '#b987ff',
}

let themeObserver: MutationObserver | null = null

const statusComparisonOption = computed(() => {
  const data = statusComparisonData.value
  return {
    color: data.map(item => statusColor(item.name)),
    tooltip: buildTooltipOption('axis'),
    grid: { left: 34, right: 12, top: 20, bottom: 28 },
    xAxis: {
      type: 'category',
      data: data.map(item => statusLabel(item.name)),
      axisLine: { lineStyle: { color: chartGridColor.value } },
      axisTick: { show: false },
      axisLabel: { color: chartTextColor.value },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: chartGridColor.value } },
      axisLabel: { color: chartTextColor.value },
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: 34,
        data: data.map(item => ({
          value: item.value,
          itemStyle: { color: statusColor(item.name), borderRadius: [6, 6, 0, 0] },
        })),
      },
    ],
  }
})

const statusPieOption = computed(() => {
  const data = statusPieData.value
  return {
    color: data.map(item => statusColor(item.name)),
    tooltip: buildTooltipOption('item'),
    legend: {
      bottom: 0,
      icon: 'circle',
      textStyle: { color: chartTextColor.value },
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '66%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        label: {
          color: chartTextColor.value,
          formatter: '{b}',
        },
        labelLine: {
          lineStyle: { color: chartGridColor.value },
        },
        itemStyle: {
          borderColor: chartTooltipBg.value,
          borderWidth: 2,
        },
        data: data.map(item => ({
          name: statusLabel(item.name),
          value: item.value,
        })),
      },
    ],
  }
})

const callTrendOption = computed(() => {
  const trend = overview.value.callTrend || []
  const dates = trend.map(item => item.date)
  return {
    color: ['#4fa3ff', '#25c06d', '#ff5b5b'],
    tooltip: buildTooltipOption('axis'),
    legend: {
      top: 0,
      right: 6,
      textStyle: { color: chartTextColor.value },
      data: [t('integration.home.total'), t('integration.home.success'), t('integration.home.fail')],
    },
    grid: { left: 36, right: 18, top: 40, bottom: 28 },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLine: { lineStyle: { color: chartGridColor.value } },
      axisTick: { show: false },
      axisLabel: { color: chartTextColor.value },
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      splitLine: { lineStyle: { color: chartGridColor.value } },
      axisLabel: { color: chartTextColor.value },
    },
    series: [
      buildLineSeries(t('integration.home.total'), trend.map(item => item.total)),
      buildLineSeries(t('integration.home.success'), trend.map(item => item.success)),
      buildLineSeries(t('integration.home.fail'), trend.map(item => item.fail)),
    ],
  }
})

onMounted(() => {
  syncChartTheme()
  loadOverview()

  themeObserver = new MutationObserver(() => {
    syncChartTheme()
    resizeCharts()
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme', 'style', 'class'],
  })
  if (document.body) {
    themeObserver.observe(document.body, {
      attributes: true,
      attributeFilter: ['data-theme', 'style', 'class'],
    })
  }
  window.addEventListener('resize', resizeCharts)
})

onBeforeUpdate(() => {
  chartRefs.value = []
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null
  window.removeEventListener('resize', resizeCharts)
})

async function loadOverview() {
  loading.value = true
  try {
    overview.value = normalizeOverview(await getIntegrationDashboardOverview())
  } catch (error) {
    message.error(t('integration.common.loadFailed'))
    overview.value = createEmptyOverview()
  } finally {
    loading.value = false
    await nextTick()
    resizeCharts()
  }
}

async function handleLayoutUpdated() {
  await nextTick()
  syncChartTheme()
  resizeCharts()
}

function setChartRef(chart: any) {
  if (chart && !chartRefs.value.includes(chart)) {
    chartRefs.value.push(chart)
  }
}

function resizeCharts() {
  requestAnimationFrame(() => {
    chartRefs.value.forEach(chart => chart?.resize?.())
  })
}

function syncChartTheme() {
  const element = homeRootRef.value || document.documentElement
  const style = getComputedStyle(element)
  chartTextColor.value = readCssVar(style, '--integration-chart-text', '#6b7280')
  chartPrimaryTextColor.value = readCssVar(style, '--integration-chart-text-strong', '#111827')
  chartGridColor.value = readCssVar(style, '--integration-chart-grid', 'rgba(148, 163, 184, 0.28)')
  chartTooltipBg.value = readCssVar(style, '--integration-chart-tooltip-bg', '#ffffff')
  chartTooltipBorder.value = readCssVar(style, '--integration-chart-tooltip-border', '#e5e7eb')
}

function readCssVar(style: CSSStyleDeclaration, name: string, fallback: string) {
  return style.getPropertyValue(name).trim() || fallback
}

function buildTooltipOption(trigger: 'axis' | 'item') {
  return {
    trigger,
    axisPointer: trigger === 'axis' ? { type: 'shadow' } : undefined,
    backgroundColor: chartTooltipBg.value,
    borderColor: chartTooltipBorder.value,
    borderWidth: 1,
    padding: [8, 10],
    extraCssText: 'border-radius:8px;box-shadow:0 8px 24px rgba(15,23,42,0.12);',
    textStyle: { color: chartPrimaryTextColor.value },
  }
}

function createEmptyOverview(): IntegrationDashboardOverview {
  const today = new Date()
  const trend = Array.from({ length: 14 }, (_, index) => {
    const date = new Date(today)
    date.setDate(today.getDate() - 13 + index)
    return {
      date: formatDate(date),
      total: 0,
      success: 0,
      fail: 0,
    }
  })

  return {
    summary: {
      totalThirdSystems: 0,
      enabledThirdSystems: 0,
      totalApis: 0,
      enabledApis: 0,
      inboundApis: 0,
      outboundApis: 0,
      todayCalls: 0,
      totalCalls: 0,
      successCalls: 0,
      failCalls: 0,
      successRate: 0,
    },
    directionStats: [
      { name: 'INBOUND', value: 0 },
      { name: 'OUTBOUND', value: 0 },
    ],
    statusComparison: [
      { name: 'SUCCESS', value: 0 },
      { name: 'FAIL', value: 0 },
    ],
    statusPie: [
      { name: 'SUCCESS', value: 0 },
      { name: 'FAIL', value: 0 },
    ],
    callTrend: trend,
    topApis: [],
    recentFailures: [],
  }
}

function normalizeOverview(data?: IntegrationDashboardOverview): IntegrationDashboardOverview {
  const empty = createEmptyOverview()
  if (!data) {
    return empty
  }

  return {
    summary: {
      ...empty.summary,
      ...(data.summary || {}),
    },
    directionStats: data.directionStats?.length ? data.directionStats : empty.directionStats,
    statusComparison: data.statusComparison?.length ? data.statusComparison : empty.statusComparison,
    statusPie: data.statusPie?.length ? data.statusPie : empty.statusPie,
    callTrend: data.callTrend?.length ? data.callTrend : empty.callTrend,
    topApis: data.topApis || [],
    recentFailures: data.recentFailures || [],
  }
}

function normalizeChartData(data: { name: string; value: number }[] | undefined, fallbackNames: string[]) {
  if (data?.length) {
    return data.map(item => ({ name: item.name, value: Number(item.value || 0) }))
  }
  return fallbackNames.map(name => ({ name, value: 0 }))
}

function buildLineSeries(name: string, data: number[]) {
  return {
    name,
    type: 'line',
    smooth: true,
    symbol: 'circle',
    symbolSize: 6,
    lineStyle: { width: 2 },
    areaStyle: { opacity: 0.08 },
    data,
  }
}

function statusColor(status?: string) {
  return statusColors[status || ''] || '#8a96a8'
}

function statusLabel(status?: string) {
  if (!status) {
    return '-'
  }
  const key = `integration.home.status.${status.toLowerCase()}`
  const label = t(key)
  return label === key ? status : label
}

function directionLabel(direction?: string) {
  if (direction === 'INBOUND') {
    return t('integration.common.inbound')
  }
  if (direction === 'OUTBOUND') {
    return t('integration.common.outbound')
  }
  return direction || '-'
}

function directionColor(direction?: string) {
  if (direction === 'INBOUND') {
    return 'blue'
  }
  if (direction === 'OUTBOUND') {
    return 'purple'
  }
  return 'default'
}

function formatNumber(value?: number) {
  return Number(value || 0).toLocaleString()
}

function formatPercent(value?: number) {
  return `${Number(value || 0).toFixed(2)}%`
}

function formatCost(value?: number) {
  return t('integration.home.costMs', { count: Number(value || 0).toLocaleString() })
}

function formatDate(date: Date) {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}
</script>

<style scoped lang="less" src="@/styles/views/integrationPlatform/home/index.less"></style>
