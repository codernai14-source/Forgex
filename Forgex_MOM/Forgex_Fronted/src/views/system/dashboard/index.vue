<template>
  <ModuleHomepageDesigner
    module-code="sys"
    :hidden-widget-keys="legacyWidgetKeys"
    @layout-updated="handleHomepageLayoutUpdated"
  >
    <template #systemStats>
      <div class="system-dashboard system-dashboard--stats">
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            :title="$t('system.dashboard.userCount')"
            :value="statistics.userCount"
            :value-style="{ color: '#1890ff' }"
          >
            <template #prefix>
              <UserOutlined />
            </template>
            <template #suffix>
              <span class="stat-suffix">{{ $t('system.dashboard.peopleUnit') }}</span>
            </template>
          </a-statistic>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            :title="$t('system.dashboard.roleCount')"
            :value="statistics.roleCount"
            :value-style="{ color: '#52c41a' }"
          >
            <template #prefix>
              <TeamOutlined />
            </template>
            <template #suffix>
              <span class="stat-suffix">{{ $t('system.dashboard.itemUnit') }}</span>
            </template>
          </a-statistic>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            :title="$t('system.dashboard.menuCount')"
            :value="statistics.menuCount"
            :value-style="{ color: '#faad14' }"
          >
            <template #prefix>
              <AppstoreOutlined />
            </template>
            <template #suffix>
              <span class="stat-suffix">{{ $t('system.dashboard.itemUnit') }}</span>
            </template>
          </a-statistic>
        </a-card>
        <a-card :bordered="false" class="stat-card">
          <a-statistic
            :title="$t('system.dashboard.onlineUsers')"
            :value="statistics.onlineUsers"
            :value-style="{ color: '#722ed1' }"
          >
            <template #prefix>
              <WifiOutlined />
            </template>
            <template #suffix>
              <span class="stat-suffix">{{ $t('system.dashboard.peopleUnit') }}</span>
            </template>
          </a-statistic>
        </a-card>
      </div>
    </template>

    <template #systemCpu>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.cpuUsage')" :bordered="false" class="chart-card chart-card--viz">
          <div ref="cpuChartRef" class="echart-container"></div>
        </a-card>
      </div>
    </template>

    <template #systemMemory>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.memoryUsage')" :bordered="false" class="chart-card chart-card--viz">
          <div ref="memoryChartRef" class="echart-container memory-chart-echart"></div>
        </a-card>
      </div>
    </template>

    <template #systemJvmMemory>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.moduleMemoryUsage')" :bordered="false" class="chart-card chart-card--viz">
          <div ref="moduleChartRef" class="echart-container"></div>
        </a-card>
      </div>
    </template>

    <template #systemMap>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.location')" :bordered="false" class="chart-card">
          <div ref="mapChartRef" class="echart-container map-container"></div>
        </a-card>
      </div>
    </template>

    <template #systemServerInfo>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.serverInfo')" :bordered="false" class="chart-card">
          <a-descriptions :column="1" size="small">
            <a-descriptions-item :label="$t('system.dashboard.appVersion')">
              {{ serverInfo.appVersion }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.osName')">
              {{ serverInfo.osName }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.osKernelVersion')">
              {{ serverInfo.osKernelVersion }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.osArch')">
              {{ serverInfo.osArch }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.totalMemory')">
              {{ serverInfo.totalMemory }} GB
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.usedMemory')">
              {{ serverInfo.usedMemory }} GB
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.availableMemory')">
              {{ serverInfo.availableMemory }} GB
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.jvmHeapMax')">
              {{ serverInfo.jvmHeapMaxGb }} GB
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.javaVersion')">
              {{ serverInfo.javaVersion }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.jvmName')">
              {{ serverInfo.jvmName }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.cpuCores')">
              {{ serverInfo.cpuCores }} {{ $t('system.dashboard.cores') }}
            </a-descriptions-item>
            <a-descriptions-item :label="$t('system.dashboard.cpuModel')">
              {{ serverInfo.cpuModel }}
            </a-descriptions-item>
          </a-descriptions>
        </a-card>
      </div>
    </template>

    <template #systemOperationLogs>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.recentOperationLogs')" :bordered="false" class="chart-card">
          <a-table
            :columns="operationLogColumns"
            :data-source="operationLogs"
            :pagination="false"
            size="small"
            :scroll="{ y: 240 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'operationTime'">
                {{ dayjs(record.operationTime).format('YYYY-MM-DD HH:mm:ss') }}
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 0 ? 'success' : 'error'">
                  {{ record.status === 0 ? $t('common.success') : $t('common.failed') }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </template>

    <template #systemLoginLogs>
      <div class="system-dashboard">
        <a-card :title="$t('system.dashboard.recentLoginLogs')" :bordered="false" class="chart-card">
          <a-table
            :columns="loginLogColumns"
            :data-source="loginLogs"
            :pagination="false"
            size="small"
            :scroll="{ y: 240 }"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'loginTime'">
                {{ dayjs(record.loginTime).format('YYYY-MM-DD HH:mm:ss') }}
              </template>
              <template v-if="column.key === 'status'">
                <a-tag :color="record.status === 0 ? 'success' : 'error'">
                  {{ record.status === 0 ? $t('common.success') : $t('common.failed') }}
                </a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </div>
    </template>
  </ModuleHomepageDesigner>
</template>

<script setup lang="ts">
import { nextTick, ref, onMounted, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import * as echarts from 'echarts'
import type { EChartsOption } from 'echarts'
import {
UserOutlined,
  TeamOutlined,
  AppstoreOutlined,
  WifiOutlined
} from '@ant-design/icons-vue'
import {
  getDashboardStatistics,
  getServerInfo,
  getModuleMemoryUsage,
  getServiceMemoryUsage,
  getRecentOperationLogs,
  getRecentLoginLogs
} from '@/api/system/dashboard'
import ModuleHomepageDesigner from '@/components/module-homepage/ModuleHomepageDesigner.vue'
import { resolveModuleDisplayName } from '@/utils/menuI18n'

const { t } = useI18n()
const legacyWidgetKeys = ['systemOverview', 'systemHealth', 'systemLogs', 'systemConfig']

/**
 * 是否与主布局深色模式一致（读取 document[data-theme]，与 MainLayout 中 layoutConfig 同步）
 */
const isDark = ref(false)

/**
 * 从本地布局配置解析主题（与 MainLayout 中 resolveThemeMode 逻辑一致）
 *
 * @returns 'light' | 'dark'
 */
function resolveLayoutThemeMode(): 'light' | 'dark' {
  try {
    const raw = localStorage.getItem('fx-layout-config')
    if (raw) {
      const cfg = JSON.parse(raw) as { themeMode?: string }
      const mode = cfg?.themeMode
      if (mode === 'dark') {
        return 'dark'
      }
      if (mode === 'light') {
        return 'light'
      }
      if (mode === 'system') {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
      }
    }
  } catch {
    // ignore
  }
  const attr = document.documentElement.getAttribute('data-theme')
  return attr === 'dark' ? 'dark' : 'light'
}

function syncDashboardTheme() {
  isDark.value = resolveLayoutThemeMode() === 'dark'
}

function getThemeToken(name: string, fallback: string) {
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return value || fallback
}

let themeObserver: MutationObserver | null = null

// 统计数据
const statistics = ref({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  onlineUsers: 0
})

// 服务器信息（字段与后端 DashboardServiceImpl#getServerInfo 对齐）
const serverInfo = ref({
  appVersion: '',
  osName: '',
  osKernelVersion: '',
  osArch: '',
  totalMemory: 0,
  usedMemory: 0,
  availableMemory: 0,
  physicalTotalGb: 0 as number,
  physicalUsedGb: 0 as number,
  physicalFreeGb: 0 as number,
  jvmHeapMaxGb: 0 as number,
  javaVersion: '',
  jvmName: '',
  cpuCores: 0,
  cpuModel: '',
  cpuUsage: 0,
  mapLongitude: 116.407526,
  mapLatitude: 39.90403,
  mapLocationName: ''
})

// 模块使用数据
const moduleUsageData = ref<any[]>([])

// 服务内存数据
const serviceMemoryData = ref<any[]>([])

// 操作日志
const operationLogs = ref<any[]>([])
const operationLogColumns = [
  {
    title: t('system.dashboard.operationTime'),
    dataIndex: 'operationTime',
    key: 'operationTime',
    width: 160
  },
  {
    title: t('system.dashboard.operator'),
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 100
  },
  {
    title: t('system.dashboard.operationModule'),
    dataIndex: 'operationModule',
    key: 'operationModule',
    width: 120
  },
  {
    title: t('system.dashboard.operationContent'),
    dataIndex: 'operationDescription',
    key: 'operationDescription',
    ellipsis: true
  },
  {
    title: t('common.status'),
    dataIndex: 'status',
    key: 'status',
    width: 80
  }
]

// 登录日志
const loginLogs = ref<any[]>([])
const loginLogColumns = [
  {
    title: t('system.dashboard.loginTime'),
    dataIndex: 'loginTime',
    key: 'loginTime',
    width: 160
  },
  {
    title: t('system.dashboard.username'),
    dataIndex: 'username',
    key: 'username',
    width: 100
  },
  {
    title: t('system.dashboard.ipAddress'),
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 140
  },
  {
    title: t('system.dashboard.loginLocation'),
    dataIndex: 'loginLocation',
    key: 'loginLocation',
    ellipsis: true
  },
  {
    title: t('common.status'),
    dataIndex: 'status',
    key: 'status',
    width: 80
  }
]

// ECharts 实例引用
const cpuChartRef = ref<HTMLElement | null>(null)
const memoryChartRef = ref<HTMLElement | null>(null)
const moduleChartRef = ref<HTMLElement | null>(null)
const mapChartRef = ref<HTMLElement | null>(null)

let cpuChart: echarts.ECharts | null = null
let memoryChart: echarts.ECharts | null = null
let moduleChart: echarts.ECharts | null = null
let mapChart: echarts.ECharts | null = null

/**
 * JVM 柱状图渐变（大厂仪表盘常用横向高光渐变）
 *
 * @param colors 主题色板
 * @returns ECharts 线性渐变对象
 */
function buildJvmBarGradient(colors: ReturnType<typeof getThemeColors>) {
  return new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: colors.jvmBarStart },
    { offset: 0.45, color: colors.primary },
    { offset: 1, color: colors.jvmBarEnd }
  ])
}

/**
 * 获取主题配色（含图表语义色，便于仪表盘统一层级）
 */
const getThemeColors = () => {
  const colors = {
    // 主题色
    primary: '#1890ff',
    primaryStrong: '#096dd9',
    success: '#52c41a',
    warning: '#faad14',
    purple: '#722ed1',
    cyan: '#13c2c2',
    teal: '#36cfc9',
    blue: '#1890ff',
    green: '#52c41a',
    orange: '#fa8c16',
    yellow: '#fadb14',

    /** JVM 条形渐变两端（略偏冷色与青色，增强科技感） */
    jvmBarStart: '#0958d9',
    jvmBarEnd: '#5cdbd3',

    /** CPU 表盘底色轨道 */
    gaugeTrack: '',
    
    // 根据主题调整的颜色
    bgColor: getThemeToken('--fx-layout-bg', isDark.value ? '#0f172a' : '#ffffff'),
    cardBg: getThemeToken('--fx-bg-container', isDark.value ? '#141414' : '#ffffff'),
    textColor: getThemeToken('--fx-text-primary', isDark.value ? '#ffffff' : '#000000'),
    textColorSecondary: getThemeToken('--fx-text-secondary', isDark.value ? 'rgba(255, 255, 255, 0.65)' : 'rgba(0, 0, 0, 0.65)'),
    borderColor: getThemeToken('--fx-border-color', isDark.value ? '#303030' : '#f0f0f0'),
    axisLineColor: isDark.value ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.1)',
    splitLineColor: getThemeToken('--fx-border-secondary', isDark.value ? '#2f3540' : '#eeeeee'),
    tooltipBg: isDark.value ? 'rgba(0, 0, 0, 0.9)' : 'rgba(255, 255, 255, 0.96)',
    
    // 图表背景色
    chartBg: getThemeToken('--fx-bg-container', isDark.value ? '#141414' : '#ffffff')
  }

  colors.gaugeTrack = isDark.value ? 'rgba(255, 255, 255, 0.14)' : '#e9eef5'

  return colors
}

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    message.warning(t('system.dashboard.tenantInfoMissing'))
    return
  }

  try {
    const data = await getDashboardStatistics({ tenantId })
    statistics.value = data || statistics.value
  } catch (error) {
    console.error('[Dashboard] Failed to load statistics:', error)
    message.error(t('system.dashboard.loadStatisticsFailed'))
  }
}

/**
 * 加载服务器信息
 */
const loadServerInfo = async () => {
  try {
    const data = await getServerInfo()
    if (data && typeof data === 'object') {
      serverInfo.value = { ...serverInfo.value, ...data }
    }
    const cpu = Number(serverInfo.value.cpuUsage)
    if (cpuChart) {
      updateCpuChart(Number.isFinite(cpu) ? cpu : 0)
    }
  } catch (error) {
    console.error('[Dashboard] Failed to load server info:', error)
  }
}

/**
 * 加载 JVM 内存分区占用（柱状图）
 */
const loadModuleMemoryUsage = async () => {
  try {
    const data = await getModuleMemoryUsage()
    moduleUsageData.value = Array.isArray(data) ? data : []
    updateModuleChart()
  } catch (error) {
    console.error('[Dashboard] Failed to load JVM memory data:', error)
  }
}

/**
 * 加载服务内存数据
 */
const loadServiceMemoryUsage = async () => {
  try {
    const data = await getServiceMemoryUsage()
    serviceMemoryData.value = data || []
    updateMemoryChart()
  } catch (error) {
    console.error('[Dashboard] Failed to load service memory data:', error)
  }
}

/**
 * 加载最近操作日志
 */
const loadRecentOperationLogs = async () => {
  try {
    const data = await getRecentOperationLogs({ size: 5 })
    operationLogs.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('[Dashboard] Failed to load operation logs:', error)
  }
}

/**
 * 加载最近登录日志
 */
const loadRecentLoginLogs = async () => {
  try {
    const data = await getRecentLoginLogs({ size: 5 })
    loginLogs.value = Array.isArray(data) ? data : []
  } catch (error) {
    console.error('[Dashboard] Failed to load login logs:', error)
  }
}

/**
 * 初始化 CPU 使用率仪表盘（双层语义：浅色轨道 + 渐变进度与柔和投影）
 */
const initCpuChart = () => {
  if (!cpuChartRef.value) return

  cpuChart = echarts.init(cpuChartRef.value)

  const colors = getThemeColors()
  const gaugeGradient = new echarts.graphic.LinearGradient(0, 0, 1, 0, [
    { offset: 0, color: colors.primaryStrong },
    { offset: 0.55, color: colors.primary },
    { offset: 1, color: colors.teal }
  ])

  const option: EChartsOption = {
    backgroundColor: 'transparent',
    animationDuration: 680,
    animationEasing: 'cubicOut',
    series: [
      {
        type: 'gauge',
        z: 1,
        min: 0,
        max: 100,
        splitNumber: 5,
        radius: '108%',
        center: ['50%', '68%'],
        startAngle: 180,
        endAngle: 0,
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 22,
            color: [[1, colors.gaugeTrack]]
          }
        },
        axisTick: {
          splitNumber: 2,
          distance: -28,
          length: 7,
          lineStyle: {
            width: 1,
            color: colors.axisLineColor
          }
        },
        splitLine: {
          distance: -32,
          length: 11,
          lineStyle: {
            width: 2,
            color: colors.axisLineColor,
            cap: 'round'
          }
        },
        axisLabel: {
          distance: -42,
          color: colors.textColorSecondary,
          fontSize: 11,
          fontWeight: 500
        },
        pointer: { show: false },
        anchor: { show: false },
        detail: { show: false },
        progress: { show: false },
        title: { show: false }
      },
      {
        type: 'gauge',
        z: 2,
        min: 0,
        max: 100,
        splitNumber: 5,
        radius: '108%',
        center: ['50%', '68%'],
        startAngle: 180,
        endAngle: 0,
        itemStyle: {
          shadowBlur: 22,
          shadowColor: 'rgba(24, 144, 255, 0.35)'
        },
        progress: {
          show: true,
          overlap: false,
          roundCap: true,
          width: 22,
          clip: false,
          itemStyle: {
            borderWidth: 0,
            color: gaugeGradient
          }
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 22,
            color: [[1, 'transparent']]
          }
        },
        axisTick: { show: false },
        splitLine: { show: false },
        axisLabel: { show: false },
        pointer: {
          icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.27916,617.312956 C2088.32399,616.194028 2089.24411,615.30999 2090.36389,615.30999 Z',
          length: '72%',
          width: 13,
          offsetCenter: [0, '-4%'],
          itemStyle: {
            color: '#ffffff',
            shadowBlur: 10,
            shadowColor: 'rgba(15, 23, 42, 0.35)'
          }
        },
        anchor: {
          show: true,
          showAbove: true,
          size: 18,
          itemStyle: {
            borderWidth: 6,
            borderColor: '#ffffff',
            color: colors.primary
          }
        },
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 22,
          fontWeight: 700,
          offsetCenter: [0, '52%'],
          formatter: '{value}%',
          color: colors.textColor,
          fontFamily:
            'Inter, system-ui, -apple-system, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif'
        },
        data: [
          {
            value: 0,
            name: 'CPU'
          }
        ]
      }
    ]
  }

  cpuChart.setOption(option)
}

/**
 * 更新 CPU 仪表盘数据
 */
const updateCpuChart = (cpuUsage: number) => {
  if (!cpuChart) return

  const colors = getThemeColors()
  const v = Number(cpuUsage)
  const safe = Number.isFinite(v) ? Math.min(100, Math.max(0, v)) : 0
  cpuChart.setOption({
    series: [
      {},
      {
        data: [
          {
            value: Number(safe.toFixed(2)),
            name: 'CPU'
          }
        ],
        detail: {
          color: colors.textColor
        },
        anchor: {
          itemStyle: {
            color: colors.primary
          }
        }
      }
    ]
  })
}

/**
 * 初始化内存使用饼图
 */
const initMemoryChart = () => {
  if (!memoryChartRef.value) return

  memoryChart = echarts.init(memoryChartRef.value)

  const colors = getThemeColors()

  const sliceColors = isDark.value
    ? [
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#1677ff' },
          { offset: 1, color: '#69b1ff' }
        ]),
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#389e0d' },
          { offset: 1, color: '#95de64' }
        ]),
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#d48806' },
          { offset: 1, color: '#ffc069' }
        ])
      ]
    : [
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#0958d9' },
          { offset: 1, color: '#4096ff' }
        ]),
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#237804' },
          { offset: 1, color: '#73d13d' }
        ]),
        new echarts.graphic.LinearGradient(0, 0, 1, 1, [
          { offset: 0, color: '#d46b08' },
          { offset: 1, color: '#ffc069' }
        ])
      ]

  const option: EChartsOption = {
    backgroundColor: 'transparent',
    animationDuration: 760,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} MB ({d}%)',
      backgroundColor: colors.tooltipBg,
      borderWidth: 1,
      borderColor: colors.borderColor,
      padding: [10, 14],
      extraCssText: 'border-radius:10px;box-shadow:0 8px 24px rgba(15,23,42,0.12);',
      textStyle: {
        color: colors.textColor,
        fontSize: 13
      }
    },
    legend: {
      orient: 'horizontal',
      left: 'center',
      bottom: 6,
      itemGap: 20,
      itemWidth: 10,
      itemHeight: 10,
      icon: 'roundRect',
      textStyle: {
        fontSize: 12,
        color: colors.textColorSecondary,
        fontWeight: 500,
        padding: [0, 0, 0, 4]
      }
    },
    series: [
      {
        name: t('system.dashboard.memoryUsage'),
        type: 'pie',
        center: ['50%', '46%'],
        radius: ['44%', '70%'],
        padAngle: 1.2,
        avoidLabelOverlap: true,
        minShowLabelAngle: 4,
        itemStyle: {
          borderRadius: 8,
          borderColor: colors.chartBg,
          borderWidth: 3,
          shadowBlur: isDark.value ? 18 : 14,
          shadowColor: isDark.value ? 'rgba(0, 0, 0, 0.45)' : 'rgba(15, 23, 42, 0.08)'
        },
        label: {
          show: true,
          position: 'inside',
          formatter: '{d}%',
          fontSize: 14,
          fontWeight: 700,
          color: '#ffffff',
          textShadowColor: 'rgba(0, 0, 0, 0.45)',
          textShadowBlur: 6
        },
        labelLine: {
          show: false
        },
        emphasis: {
          scale: true,
          scaleSize: 6,
          itemStyle: {
            shadowBlur: 28,
            shadowOffsetY: 4,
            shadowColor: 'rgba(24, 144, 255, 0.35)'
          },
          label: {
            fontSize: 15
          }
        },
        data: [],
        color: sliceColors
      }
    ]
  }

  memoryChart.setOption(option)
}

/**
 * 更新内存饼图数据
 */
const updateMemoryChart = () => {
  if (!memoryChart) return

  const data = serviceMemoryData.value.map(item => ({
    name: item.serviceName,
    value: item.memoryUsage
  }))

  memoryChart.setOption({
    series: [
      {
        data: data
      }
    ]
  })
}

/**
 * 初始化模块使用柱状图
 */
const initModuleChart = () => {
  if (!moduleChartRef.value) return

  moduleChart = echarts.init(moduleChartRef.value)

  const colors = getThemeColors()
  const barGradient = buildJvmBarGradient(colors)

  const option: EChartsOption = {
    backgroundColor: 'transparent',
    animationDuration: 620,
    animationEasing: 'cubicOut',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: isDark.value ? 'rgba(255,255,255,0.06)' : 'rgba(24, 144, 255, 0.08)'
        }
      },
      backgroundColor: colors.tooltipBg,
      borderWidth: 1,
      borderColor: colors.borderColor,
      padding: [10, 14],
      extraCssText: 'border-radius:10px;box-shadow:0 8px 24px rgba(15,23,42,0.12);',
      textStyle: {
        color: colors.textColor,
        fontSize: 13
      }
    },
    grid: {
      left: '4%',
      right: '5%',
      bottom: '6%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: t('system.dashboard.memoryMb'),
      nameGap: 10,
      nameTextStyle: {
        color: colors.textColorSecondary,
        fontSize: 12,
        fontWeight: 500,
        align: 'left'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: colors.axisLineColor,
          width: 1
        }
      },
      axisLabel: {
        color: colors.textColorSecondary,
        fontSize: 11,
        margin: 10
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: colors.splitLineColor,
          type: [4, 6]
        }
      }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLine: {
        show: false
      },
      axisTick: {
        show: false
      },
      axisLabel: {
        color: colors.textColorSecondary,
        fontSize: 11,
        fontWeight: 500,
        margin: 14,
        formatter(value: string) {
          const text = String(value || '')
          return text.length > 18 ? `${text.slice(0, 18)}…` : text
        }
      },
      splitLine: {
        show: false
      }
    },
    series: [
      {
        name: t('system.dashboard.memoryMb'),
        type: 'bar',
        data: [],
        barCategoryGap: '38%',
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [0, 8, 8, 0],
          color: barGradient,
          shadowBlur: 16,
          shadowColor: 'rgba(24, 144, 255, 0.22)',
          shadowOffsetY: 3
        },
        emphasis: {
          itemStyle: {
            shadowBlur: 26,
            shadowColor: 'rgba(24, 144, 255, 0.45)'
          }
        },
        label: {
          show: true,
          position: 'right',
          distance: 10,
          color: colors.textColorSecondary,
          fontSize: 11,
          fontWeight: 600
        }
      }
    ]
  }

  moduleChart.setOption(option)
}

/**
 * 更新模块柱状图数据
 */
const updateModuleChart = () => {
  if (!moduleChart) return

  const moduleNames = moduleUsageData.value.map(item => resolveModuleDisplayName(String(item.moduleCode ?? ''), item.moduleName))
  const memoryMb = moduleUsageData.value.map(item => Number(item.memoryUsageMb) || 0)

  moduleChart.setOption({
    xAxis: {
      name: t('system.dashboard.memoryMb')
    },
    series: [
      {
        name: t('system.dashboard.memoryMb'),
        data: memoryMb
      }
    ],
    yAxis: {
      data: moduleNames
    }
  })
}

/** 阿里云 DataV 中国边界 GeoJSON（与后端默认坐标一致时可标注真实位置） */
const CHINA_GEO_JSON_URL = 'https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json'

/**
 * 初始化中国地图并标注服务器位置（经纬度来自后端配置）
 */
const initMapChart = async () => {
  if (!mapChartRef.value) {
    return
  }

  mapChart?.dispose()
  mapChart = echarts.init(mapChartRef.value)

  const colors = getThemeColors()
  const lng = Number(serverInfo.value.mapLongitude)
  const lat = Number(serverInfo.value.mapLatitude)
  const label = serverInfo.value.mapLocationName || t('system.dashboard.serverLocation')

  let geoReady = false
  try {
    const res = await fetch(CHINA_GEO_JSON_URL)
    if (res.ok) {
      const geoJson = await res.json()
      echarts.registerMap('china', geoJson as any)
      geoReady = true
    }
  } catch (e) {
    console.warn('[Dashboard] Failed to load China map data, fallback placeholder will be shown', e)
  }

  if (geoReady) {
    const option: EChartsOption = {
      backgroundColor: colors.chartBg,
      tooltip: {
        trigger: 'item',
        backgroundColor: colors.tooltipBg,
        borderColor: colors.borderColor,
        textStyle: {
          color: colors.textColor
        }
      },
      geo: {
        map: 'china',
        roam: true,
        zoom: 1.1,
        label: {
          show: false,
          color: colors.textColorSecondary
        },
        itemStyle: {
          areaColor: isDark.value ? '#2a3340' : '#e6f4ff',
          borderColor: isDark.value ? '#4a5568' : '#91caff'
        },
        emphasis: {
          label: { color: colors.textColor },
          itemStyle: { areaColor: isDark.value ? '#3d4a5c' : '#bae0ff' }
        }
      },
      series: [
        {
          name: t('system.dashboard.location'),
          type: 'effectScatter',
          coordinateSystem: 'geo',
          data: [
            {
              name: label,
              value: [lng, lat, 1]
            }
          ],
          symbolSize: 14,
          showEffectOn: 'render',
          rippleEffect: {
            brushType: 'stroke',
            scale: 3.5
          },
          label: {
            show: true,
            formatter: '{b}',
            position: 'right',
            color: isDark.value ? '#ffffff' : '#1f2937',
            fontSize: 13,
            backgroundColor: isDark.value ? 'rgba(0,0,0,0.55)' : 'rgba(255,255,255,0.9)',
            padding: [4, 8],
            borderRadius: 4
          },
          itemStyle: {
            color: '#faad14',
            shadowBlur: 10,
            shadowColor: 'rgba(0,0,0,0.35)'
          },
          zlevel: 2
        }
      ]
    }
    mapChart.setOption(option)
  } else {
    mapChart.setOption({
      backgroundColor: colors.chartBg,
      title: {
        text: t('system.dashboard.mapLoadFailed'),
        left: 'center',
        top: 'middle',
        textStyle: {
          color: colors.textColorSecondary,
          fontSize: 14,
          lineHeight: 22
        }
      },
      graphic: [
        {
          type: 'text',
          left: 'center',
          top: '58%',
          style: {
            text: `${label}  ${Number.isFinite(lng) ? lng.toFixed(4) : '-'} , ${Number.isFinite(lat) ? lat.toFixed(4) : '-'}`,
            fill: colors.textColor,
            fontSize: 13
          }
        }
      ]
    })
  }
}

/**
 * 刷新所有图表（主题切换或数据更新时调用）
 */
const refreshAllCharts = async () => {
  cpuChart?.dispose()
  memoryChart?.dispose()
  moduleChart?.dispose()
  mapChart?.dispose()
  cpuChart = null
  memoryChart = null
  moduleChart = null
  mapChart = null

  initCpuChart()
  initMemoryChart()
  initModuleChart()
  await initMapChart()
  const cpu = Number(serverInfo.value.cpuUsage)
  updateCpuChart(Number.isFinite(cpu) ? cpu : 0)
  updateMemoryChart()
  updateModuleChart()
}

/**
 * 窗口大小变化时重新渲染图表
 */
const handleResize = () => {
  cpuChart?.resize()
  memoryChart?.resize()
  moduleChart?.resize()
  mapChart?.resize()
}

async function handleHomepageLayoutUpdated() {
  await nextTick()

  if (!cpuChartRef.value && cpuChart) {
    cpuChart.dispose()
    cpuChart = null
  }
  if (!memoryChartRef.value && memoryChart) {
    memoryChart.dispose()
    memoryChart = null
  }
  if (!moduleChartRef.value && moduleChart) {
    moduleChart.dispose()
    moduleChart = null
  }
  if (!mapChartRef.value && mapChart) {
    mapChart.dispose()
    mapChart = null
  }

  if (cpuChartRef.value && !cpuChart) {
    initCpuChart()
  }
  if (memoryChartRef.value && !memoryChart) {
    initMemoryChart()
  }
  if (moduleChartRef.value && !moduleChart) {
    initModuleChart()
  }
  if (mapChartRef.value && !mapChart) {
    await initMapChart()
  }

  const cpu = Number(serverInfo.value.cpuUsage)
  updateCpuChart(Number.isFinite(cpu) ? cpu : 0)
  updateMemoryChart()
  updateModuleChart()
  handleResize()
}

/**
 * 加载所有数据
 */
const loadAllData = async () => {
  await Promise.all([
    loadStatistics(),
    loadServerInfo(),
    loadModuleMemoryUsage(),
    loadServiceMemoryUsage(),
    loadRecentOperationLogs(),
    loadRecentLoginLogs()
  ])
}

onMounted(async () => {
  syncDashboardTheme()
  themeObserver = new MutationObserver(() => {
    const before = isDark.value
    syncDashboardTheme()
    if (before !== isDark.value || document.documentElement.style.length > 0) {
      void refreshAllCharts()
    }
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme', 'style']
  })

  await loadAllData()
  await refreshAllCharts()

  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  themeObserver?.disconnect()
  themeObserver = null

  cpuChart?.dispose()
  memoryChart?.dispose()
  moduleChart?.dispose()
  mapChart?.dispose()

  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="less" src="@/styles/views/system/dashboard/index.less"></style>
