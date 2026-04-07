<template>
  <div class="system-dashboard">
    <!-- 顶部统计卡片 -->
    <a-row :gutter="16" class="stats-row">
      <a-col :span="6">
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
              <span class="stat-suffix">人</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
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
              <span class="stat-suffix">个</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
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
              <span class="stat-suffix">个</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
      <a-col :span="6">
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
              <span class="stat-suffix">人</span>
            </template>
          </a-statistic>
        </a-card>
      </a-col>
    </a-row>

    <!-- 图表区域 -->
    <a-row :gutter="16" class="chart-row">
      <!-- CPU 使用率仪表盘 -->
      <a-col :span="8">
        <a-card :title="$t('system.dashboard.cpuUsage')" :bordered="false" class="chart-card">
          <div ref="cpuChartRef" class="echart-container"></div>
        </a-card>
      </a-col>

      <!-- 服务内存/缓存占用饼图 -->
      <a-col :span="8">
        <a-card :title="$t('system.dashboard.memoryUsage')" :bordered="false" class="chart-card">
          <div ref="memoryChartRef" class="echart-container memory-chart-echart"></div>
        </a-card>
      </a-col>

      <!-- JVM 内存分区占用柱状图 -->
      <a-col :span="8">
        <a-card :title="$t('system.dashboard.moduleMemoryUsage')" :bordered="false" class="chart-card">
          <div ref="moduleChartRef" class="echart-container"></div>
        </a-card>
      </a-col>
    </a-row>

    <!-- 3D 地图和系统信息 -->
    <a-row :gutter="16" class="chart-row">
      <!-- 3D 地图 -->
      <a-col :span="14">
        <a-card :title="$t('system.dashboard.location')" :bordered="false" class="chart-card">
          <div ref="mapChartRef" class="echart-container map-container"></div>
        </a-card>
      </a-col>

      <!-- 服务器系统信息 -->
      <a-col :span="10">
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
      </a-col>
    </a-row>

    <!-- 最近日志 -->
    <a-row :gutter="16" class="chart-row">
      <!-- 最近操作日志 -->
      <a-col :span="12">
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
      </a-col>

      <!-- 最近登录日志 -->
      <a-col :span="12">
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
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'
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

const { t } = useI18n()

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
    title: '操作时间',
    dataIndex: 'operationTime',
    key: 'operationTime',
    width: 160
  },
  {
    title: '操作人',
    dataIndex: 'operatorName',
    key: 'operatorName',
    width: 100
  },
  {
    title: '操作模块',
    dataIndex: 'operationModule',
    key: 'operationModule',
    width: 120
  },
  {
    title: '操作内容',
    dataIndex: 'operationDescription',
    key: 'operationDescription',
    ellipsis: true
  },
  {
    title: '状态',
    dataIndex: 'status',
    key: 'status',
    width: 80
  }
]

// 登录日志
const loginLogs = ref<any[]>([])
const loginLogColumns = [
  {
    title: '登录时间',
    dataIndex: 'loginTime',
    key: 'loginTime',
    width: 160
  },
  {
    title: '用户名',
    dataIndex: 'username',
    key: 'username',
    width: 100
  },
  {
    title: 'IP 地址',
    dataIndex: 'ipAddress',
    key: 'ipAddress',
    width: 140
  },
  {
    title: '登录地点',
    dataIndex: 'loginLocation',
    key: 'loginLocation',
    ellipsis: true
  },
  {
    title: '状态',
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
 * 获取主题配色
 */
const getThemeColors = () => {
  const colors = {
    // 主题色
    primary: '#1890ff',
    success: '#52c41a',
    warning: '#faad14',
    purple: '#722ed1',
    cyan: '#13c2c2',
    blue: '#1890ff',
    green: '#52c41a',
    orange: '#fa8c16',
    yellow: '#fadb14',
    
    // 根据主题调整的颜色
    bgColor: isDark.value ? '#000000' : '#ffffff',
    cardBg: isDark.value ? '#141414' : '#ffffff',
    textColor: isDark.value ? '#ffffff' : '#000000',
    textColorSecondary: isDark.value ? 'rgba(255, 255, 255, 0.65)' : 'rgba(0, 0, 0, 0.65)',
    borderColor: isDark.value ? '#303030' : '#f0f0f0',
    axisLineColor: isDark.value ? 'rgba(255, 255, 255, 0.2)' : 'rgba(0, 0, 0, 0.1)',
    
    // 图表背景色
    chartBg: isDark.value ? '#141414' : '#ffffff'
  }
  
  return colors
}

/**
 * 加载统计数据
 */
const loadStatistics = async () => {
  const tenantId = sessionStorage.getItem('tenantId')
  if (!tenantId) {
    message.warning('租户信息缺失')
    return
  }

  try {
    const data = await getDashboardStatistics({ tenantId })
    statistics.value = data || statistics.value
  } catch (error) {
    console.error('加载统计数据失败:', error)
    message.error('加载统计数据失败')
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
    console.error('加载服务器信息失败:', error)
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
    console.error('加载 JVM 内存分区数据失败:', error)
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
    console.error('加载服务内存数据失败:', error)
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
    console.error('加载操作日志失败:', error)
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
    console.error('加载登录日志失败:', error)
  }
}

/**
 * 初始化 CPU 使用率仪表盘
 */
const initCpuChart = () => {
  if (!cpuChartRef.value) return

  cpuChart = echarts.init(cpuChartRef.value)

  const colors = getThemeColors()
  const option: EChartsOption = {
    backgroundColor: colors.chartBg,
    series: [
      {
        type: 'gauge',
        startAngle: 180,
        endAngle: 0,
        min: 0,
        max: 100,
        splitNumber: 5,
        itemStyle: {
          color: colors.primary,
          shadowColor: 'rgba(0,138,255,0.45)',
          shadowBlur: 10,
          shadowOffsetX: 2,
          shadowOffsetY: 2
        },
        progress: {
          show: true,
          roundCap: true,
          width: 18
        },
        pointer: {
          icon: 'path://M2090.36389,615.30999 L2090.36389,615.30999 C2091.48372,615.30999 2092.40383,616.194028 2092.44859,617.312956 L2096.90698,728.755929 C2097.05155,732.369577 2094.2393,735.416212 2090.62566,735.56078 C2090.53845,735.564269 2090.45117,735.566014 2090.36389,735.566014 L2090.36389,735.566014 C2086.74736,735.566014 2083.81557,732.63423 2083.81557,729.017692 C2083.81557,728.930412 2083.81732,728.84314 2083.82081,728.755929 L2088.27916,617.312956 C2088.32399,616.194028 2089.24411,615.30999 2090.36389,615.30999 Z',
          length: '75%',
          width: 16,
          offsetCenter: [0, '5%']
        },
        axisLine: {
          roundCap: true,
          lineStyle: {
            width: 18,
            color: [[1, colors.primary]]
          }
        },
        axisTick: {
          splitNumber: 2,
          lineStyle: {
            width: 2,
            color: isDark.value ? '#666666' : '#999999'
          }
        },
        splitLine: {
          length: 12,
          lineStyle: {
            width: 3,
            color: isDark.value ? '#666666' : '#999999'
          }
        },
        axisLabel: {
          distance: 25,
          color: isDark.value ? '#ffffff' : '#666666',
          fontSize: 12
        },
        anchor: {
          show: true,
          showAbove: true,
          size: 25,
          itemStyle: {
            borderWidth: 10,
            color: colors.primary
          }
        },
        // 卡片标题已说明「CPU 使用率」，此处关闭避免与百分比数字、指针重叠
        title: {
          show: false
        },
        detail: {
          valueAnimation: true,
          fontSize: 20,
          offsetCenter: [0, '58%'],
          formatter: '{value}%',
          color: colors.primary
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
      {
        data: [
          {
            value: Number(safe.toFixed(2)),
            name: 'CPU'
          }
        ],
        detail: {
          color: colors.primary
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
  const option: EChartsOption = {
    backgroundColor: colors.chartBg,
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} MB ({d}%)',
      backgroundColor: isDark.value ? 'rgba(0, 0, 0, 0.9)' : 'rgba(255, 255, 255, 0.9)',
      textColor: isDark.value ? '#ffffff' : '#333333',
      borderColor: isDark.value ? '#333333' : '#dddddd'
    },
    legend: {
      orient: 'horizontal',
      left: 'center',
      bottom: 8,
      itemGap: 14,
      textStyle: {
        fontSize: 12,
        color: isDark.value ? '#ffffff' : '#333333'
      }
    },
    series: [
      {
        name: '内存使用',
        type: 'pie',
        center: ['50%', '44%'],
        // 放大环形：相对容器更大，底部预留给图例
        radius: ['38%', '66%'],
        avoidLabelOverlap: true,
        // 过窄扇区仍显示标签（仅 3 块时一般可读）
        minShowLabelAngle: 2,
        itemStyle: {
          borderRadius: 5,
          borderColor: colors.chartBg,
          borderWidth: 2
        },
        // 扇区上直接展示占用比例（与 tooltip 中 {d}% 一致）
        label: {
          show: true,
          position: 'inside',
          formatter: '{d}%',
          fontSize: 15,
          fontWeight: 700,
          color: '#ffffff',
          textShadowColor: 'rgba(0,0,0,0.35)',
          textShadowBlur: 4
        },
        labelLine: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16
          }
        },
        data: [],
        color: isDark.value 
          ? ['#1890ff', '#52c41a', '#fa8c16']
          : ['#1890ff', '#52c41a', '#fa8c16']
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
  const option: EChartsOption = {
    backgroundColor: colors.chartBg,
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      backgroundColor: isDark.value ? 'rgba(0, 0, 0, 0.9)' : 'rgba(255, 255, 255, 0.9)',
      borderColor: isDark.value ? '#333333' : '#dddddd',
      textStyle: {
        color: isDark.value ? '#ffffff' : '#333333'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      name: t('system.dashboard.memoryMb'),
      nameTextStyle: {
        color: isDark.value ? '#ffffff' : '#333333'
      },
      axisLine: {
        show: true,
        lineStyle: {
          color: isDark.value ? '#444444' : '#dddddd'
        }
      },
      axisLabel: {
        color: isDark.value ? '#ffffff' : '#666666'
      },
      splitLine: {
        show: true,
        lineStyle: {
          color: isDark.value ? '#333333' : '#eeeeee'
        }
      }
    },
    yAxis: {
      type: 'category',
      data: [],
      axisLine: {
        show: true,
        lineStyle: {
          color: isDark.value ? '#444444' : '#dddddd'
        }
      },
      axisLabel: {
        color: isDark.value ? '#ffffff' : '#333333'
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
        barWidth: '50%',
        itemStyle: {
          borderRadius: [0, 4, 4, 0],
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1890ff' },
            { offset: 0.5, color: '#13c2c2' },
            { offset: 1, color: '#1890ff' }
          ])
        },
        label: {
          show: true,
          position: 'right',
          color: isDark.value ? '#ffffff' : '#333333',
          fontSize: 12
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

  const moduleNames = moduleUsageData.value.map(item => String(item.moduleName ?? ''))
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
  const label = serverInfo.value.mapLocationName || '服务器位置'

  let geoReady = false
  try {
    const res = await fetch(CHINA_GEO_JSON_URL)
    if (res.ok) {
      const geoJson = await res.json()
      echarts.registerMap('china', geoJson as any)
      geoReady = true
    }
  } catch (e) {
    console.warn('[Dashboard] 中国地图数据加载失败，将显示占位提示', e)
  }

  if (geoReady) {
    const option: EChartsOption = {
      backgroundColor: colors.chartBg,
      tooltip: {
        trigger: 'item',
        backgroundColor: isDark.value ? 'rgba(0, 0, 0, 0.9)' : 'rgba(255, 255, 255, 0.9)',
        borderColor: isDark.value ? '#333333' : '#dddddd',
        textStyle: {
          color: isDark.value ? '#ffffff' : '#333333'
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
          name: '位置',
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
        text: '地图数据加载失败\n请检查网络或稍后重试',
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
    if (before !== isDark.value) {
      void refreshAllCharts()
    }
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
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

<style scoped lang="less">
/* 使用 MainLayout 注入的 --fx-* 变量，与 ConfigProvider 主题一致 */
.system-dashboard {
  padding: 16px;
  background: var(--fx-layout-bg, #f3f4f6);
  min-height: calc(100vh - 84px);
  transition: background 0.3s;

  .stats-row {
    margin-bottom: 16px;

    .stat-card {
      border-radius: var(--fx-radius-lg, 8px);
      box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));
      transition: all 0.3s;
      background: var(--fx-bg-container, #ffffff);

      &:hover {
        box-shadow: var(--fx-shadow-secondary, 0 4px 16px rgba(0, 0, 0, 0.12));
        transform: translateY(-2px);
      }

      :deep(.ant-statistic-title) {
        font-size: 14px;
        color: var(--fx-text-secondary, rgba(0, 0, 0, 0.65));
      }

      :deep(.ant-statistic-content) {
        font-size: 24px;
        font-weight: 600;
        color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
      }

      .stat-suffix {
        font-size: 14px;
        margin-left: 4px;
        color: var(--fx-text-secondary, rgba(0, 0, 0, 0.65));
      }
    }
  }

  .chart-row {
    margin-bottom: 16px;

    .chart-card {
      border-radius: var(--fx-radius-lg, 8px);
      box-shadow: var(--fx-shadow, 0 2px 8px rgba(0, 0, 0, 0.08));
      height: 100%;
      background: var(--fx-bg-container, #ffffff);
      color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));

      :deep(.ant-card-head) {
        font-size: 16px;
        font-weight: 600;
        border-bottom: 1px solid var(--fx-border-color, #e5e7eb);
        color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
        background: transparent;
      }

      :deep(.ant-card-body) {
        background: var(--fx-bg-container, #ffffff);
        color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
      }

      .echart-container {
        height: 300px;
        width: 100%;
      }

      /** 服务内存饼图：略增高容器，便于环形放大且与底部图例留白 */
      .memory-chart-echart {
        height: 380px;
        min-height: 380px;
      }

      .map-container {
        height: 400px;
        overflow: hidden;
        border-radius: var(--fx-radius, 6px);
        background: var(--fx-bg-container, #ffffff);
      }

      :deep(.ant-descriptions-item-label) {
        font-weight: 500;
        width: 120px;
        color: var(--fx-text-secondary, rgba(0, 0, 0, 0.65));
      }

      :deep(.ant-descriptions-item-content) {
        word-break: break-all;
        color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
      }

      :deep(.ant-table) {
        background: transparent;

        .ant-table-thead > tr > th {
          background: var(--fx-fill-alter, #f9fafb);
          color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
          border-color: var(--fx-border-color, #e5e7eb);
        }

        .ant-table-tbody > tr {
          &:hover {
            background: var(--fx-fill, #f3f4f6);
          }
        }

        .ant-table-tbody > tr > td {
          color: var(--fx-text-primary, rgba(0, 0, 0, 0.85));
          border-color: var(--fx-border-color, #e5e7eb);
        }
      }
    }
  }
}
</style>
