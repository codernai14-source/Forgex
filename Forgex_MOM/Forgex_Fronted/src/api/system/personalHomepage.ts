import http from '../http'

export type PersonalHomepageScopeLevel = 'PUBLIC' | 'TENANT' | 'USER'
export type ModuleHomepageCode = 'personal' | 'basic' | 'approval' | 'sys'

export interface HomepageConfigRequestOptions {
  moduleCode?: string
}

export interface PersonalHomepageLayoutConfig {
  colNum: number
  rowHeight: number
  marginX: number
  marginY: number
  tabletColNum: number
  mobileColNum: number
}

export interface PersonalHomepageWidgetConfig {
  key: string
  title: string
  visible: boolean
  x: number
  y: number
  w: number
  h: number
  minW: number
  minH: number
  orderNum: number
  params: Record<string, any>
}

export interface PersonalHomepageConfig {
  layout: PersonalHomepageLayoutConfig
  widgets: PersonalHomepageWidgetConfig[]
}

export const PERSONAL_HOMEPAGE_WIDGET_KEYS = [
  'commonMenus',
  'myFavorites',
  'pendingApprovals',
  'calendar',
  'messages',
  'notices',
  'currentTime',
] as const

export const MODULE_HOMEPAGE_WIDGET_KEYS = [
  'supplierInfo',
  'encodeRuleInfo',
  'systemOverview',
  'systemHealth',
  'systemLogs',
  'systemConfig',
  'systemStats',
  'systemCpu',
  'systemMemory',
  'systemJvmMemory',
  'systemMap',
  'systemServerInfo',
  'systemOperationLogs',
  'systemLoginLogs',
  'approvalStats',
  'approvalShortcuts',
  'approvalPending',
  'approvalTaskConfig',
  'approvalWeeklyTrend',
  'approvalUserShare',
  'approvalProcessed',
  'approvalCc',
] as const

const DEFAULT_WIDGET_TITLES: Record<(typeof PERSONAL_HOMEPAGE_WIDGET_KEYS)[number], string> = {
  commonMenus: '常用菜单',
  myFavorites: '我的收藏',
  pendingApprovals: '我收到的审批',
  calendar: '日历',
  messages: '我收到的消息',
  notices: '系统通知',
  currentTime: '当前时间',
}

export function createDefaultPersonalHomepageConfig(): PersonalHomepageConfig {
  return {
    layout: {
      colNum: 12,
      rowHeight: 72,
      marginX: 16,
      marginY: 16,
      tabletColNum: 8,
      mobileColNum: 4,
    },
    widgets: [
      createWidget('commonMenus', 0, 0, 6, 4, 10, 6, true),
      createWidget('myFavorites', 0, 4, 6, 4, 20, 6, true),
      createWidget('pendingApprovals', 6, 0, 6, 4, 30, 6, true),
      createWidget('calendar', 6, 4, 3, 4, 40, 0, false),
      createWidget('currentTime', 9, 4, 3, 3, 50, 0, false),
      createWidget('messages', 0, 8, 6, 4, 60, 8, true),
      createWidget('notices', 6, 8, 6, 4, 70, 8, true),
    ],
  }
}

function createWidget(
  key: (typeof PERSONAL_HOMEPAGE_WIDGET_KEYS)[number],
  x: number,
  y: number,
  w: number,
  h: number,
  orderNum: number,
  limit: number,
  showMore: boolean,
): PersonalHomepageWidgetConfig {
  return {
    key,
    title: DEFAULT_WIDGET_TITLES[key],
    visible: true,
    x,
    y,
    w,
    h,
    minW: Math.min(w, 2),
    minH: Math.min(h, 2),
    orderNum,
    params: {
      limit,
      showMore,
    },
  }
}

function createModuleWidget(
  key: (typeof MODULE_HOMEPAGE_WIDGET_KEYS)[number],
  title: string,
  x: number,
  y: number,
  w: number,
  h: number,
  orderNum: number,
): PersonalHomepageWidgetConfig {
  return {
    key,
    title,
    visible: true,
    x,
    y,
    w,
    h,
    minW: Math.min(w, 3),
    minH: Math.min(h, 2),
    orderNum,
    params: {},
  }
}

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
  return 'personal'
}

export function createDefaultModuleHomepageConfig(moduleCode: string): PersonalHomepageConfig {
  const normalized = normalizeModuleCode(moduleCode)
  const baseLayout = createDefaultPersonalHomepageConfig().layout

  if (normalized === 'basic') {
    return {
      layout: baseLayout,
      widgets: [
        createModuleWidget('supplierInfo', '供应商信息', 0, 0, 6, 4, 10),
        createModuleWidget('encodeRuleInfo', '编码规则信息', 6, 0, 6, 4, 20),
      ],
    }
  }

  if (normalized === 'approval') {
    return {
      layout: baseLayout,
      widgets: [
        createModuleWidget('approvalWeeklyTrend', '近 7 天审批趋势', 0, 0, 12, 5, 10),
        createModuleWidget('approvalShortcuts', '快捷发起审批', 0, 5, 12, 4, 20),
        createModuleWidget('approvalUserShare', '发起人占比', 0, 9, 7, 5, 30),
        createModuleWidget('approvalTaskConfig', '任务配置预览', 7, 9, 5, 5, 40),
        createModuleWidget('approvalPending', '我的待办', 0, 14, 4, 5, 50),
        createModuleWidget('approvalProcessed', '昨日已办', 4, 14, 4, 5, 60),
        createModuleWidget('approvalCc', '抄送我的', 8, 14, 4, 5, 70),
      ],
    }
  }

  if (normalized === 'sys') {
    return {
      layout: baseLayout,
      widgets: [
        createModuleWidget('systemStats', '系统统计', 0, 0, 12, 2, 10),
        createModuleWidget('systemCpu', 'CPU 使用率', 0, 2, 4, 5, 20),
        createModuleWidget('systemMemory', '服务内存占用', 4, 2, 4, 5, 30),
        createModuleWidget('systemJvmMemory', 'JVM 内存分区', 8, 2, 4, 5, 40),
        createModuleWidget('systemMap', '服务器位置', 0, 7, 7, 6, 50),
        createModuleWidget('systemServerInfo', '服务器信息', 7, 7, 5, 6, 60),
        createModuleWidget('systemOperationLogs', '最近操作日志', 0, 13, 6, 5, 70),
        createModuleWidget('systemLoginLogs', '最近登录日志', 6, 13, 6, 5, 80),
      ],
    }
  }

  return createDefaultPersonalHomepageConfig()
}

export function mergePersonalHomepageConfig(config?: Partial<PersonalHomepageConfig> | null): PersonalHomepageConfig {
  const defaults = createDefaultPersonalHomepageConfig()
  const rawWidgets = Array.isArray(config?.widgets) ? config.widgets : []
  const rawWidgetMap = new Map(rawWidgets.map(widget => [String(widget?.key || ''), widget]))

  const widgets = defaults.widgets.map(defaultWidget => {
    const currentWidget = rawWidgetMap.get(defaultWidget.key)
    const mergedWidget = {
      ...defaultWidget,
      ...currentWidget,
      title: String(currentWidget?.title || defaultWidget.title),
      visible: currentWidget?.visible ?? defaultWidget.visible,
      params: {
        ...defaultWidget.params,
        ...(currentWidget?.params || {}),
      },
    }

    // 常用菜单已收敛为固定 Top 6，这里强制覆盖旧配置中的 limit，避免前后端规则不一致。
    if (mergedWidget.key === 'commonMenus') {
      mergedWidget.params.limit = 6
    }

    // 收藏管理页需要稳定入口，这里统一开启“我的收藏”的更多按钮，便于用户进入管理页面。
    if (mergedWidget.key === 'myFavorites') {
      mergedWidget.params.showMore = true
    }

    return mergedWidget
  })

  rawWidgets.forEach(widget => {
    const key = String(widget?.key || '')
    if (!key || widgets.some(item => item.key === key)) {
      return
    }
    widgets.push({
      key,
      title: String(widget?.title || key),
      visible: widget?.visible ?? true,
      x: Number(widget?.x ?? 0),
      y: Number(widget?.y ?? 0),
      w: Number(widget?.w ?? 3),
      h: Number(widget?.h ?? 3),
      minW: Number(widget?.minW ?? 2),
      minH: Number(widget?.minH ?? 2),
      orderNum: Number(widget?.orderNum ?? widgets.length * 10),
      params: { ...(widget?.params || {}) },
    })
  })

  widgets.sort((left, right) => {
    const leftOrder = Number(left.orderNum ?? 0)
    const rightOrder = Number(right.orderNum ?? 0)
    if (leftOrder !== rightOrder) {
      return leftOrder - rightOrder
    }
    return left.key.localeCompare(right.key)
  })

  return {
    layout: {
      ...defaults.layout,
      ...(config?.layout || {}),
    },
    widgets,
  }
}

export function mergeModuleHomepageConfig(
  config?: Partial<PersonalHomepageConfig> | null,
  moduleCode = 'personal',
): PersonalHomepageConfig {
  const normalized = normalizeModuleCode(moduleCode)
  if (normalized === 'personal') {
    return mergePersonalHomepageConfig(config)
  }

  const defaults = createDefaultModuleHomepageConfig(normalized)
  const rawWidgets = Array.isArray(config?.widgets) ? config.widgets : []
  const rawWidgetMap = new Map(rawWidgets.map(widget => [String(widget?.key || ''), widget]))

  const widgets = defaults.widgets.map(defaultWidget => {
    const currentWidget = rawWidgetMap.get(defaultWidget.key)
    return {
      ...defaultWidget,
      ...currentWidget,
      title: String(currentWidget?.title || defaultWidget.title),
      visible: currentWidget?.visible ?? defaultWidget.visible,
      params: {
        ...defaultWidget.params,
        ...(currentWidget?.params || {}),
      },
    }
  })

  rawWidgets.forEach(widget => {
    const key = String(widget?.key || '')
    if (!key || widgets.some(item => item.key === key)) {
      return
    }
    widgets.push({
      key,
      title: String(widget?.title || key),
      visible: widget?.visible ?? true,
      x: Number(widget?.x ?? 0),
      y: Number(widget?.y ?? 0),
      w: Number(widget?.w ?? 6),
      h: Number(widget?.h ?? 4),
      minW: Number(widget?.minW ?? 3),
      minH: Number(widget?.minH ?? 2),
      orderNum: Number(widget?.orderNum ?? widgets.length * 10),
      params: { ...(widget?.params || {}) },
    })
  })

  widgets.sort((left, right) => {
    const leftOrder = Number(left.orderNum ?? 0)
    const rightOrder = Number(right.orderNum ?? 0)
    if (leftOrder !== rightOrder) {
      return leftOrder - rightOrder
    }
    return left.key.localeCompare(right.key)
  })

  return {
    layout: {
      ...defaults.layout,
      ...(config?.layout || {}),
    },
    widgets,
  }
}

export function getCurrentPersonalHomepageConfig(options: HomepageConfigRequestOptions = {}) {
  return http.post<PersonalHomepageConfig>('/sys/homepage/current/get', options)
}

export function saveCurrentPersonalHomepageConfig(
  config: PersonalHomepageConfig,
  options: HomepageConfigRequestOptions = {},
) {
  return http.post<boolean>('/sys/homepage/current/save', { ...options, config })
}

export function resetCurrentPersonalHomepageConfig(options: HomepageConfigRequestOptions = {}) {
  return http.post<boolean>('/sys/homepage/current/reset', options)
}

export function getManagePersonalHomepageConfig(
  scopeLevel: Exclude<PersonalHomepageScopeLevel, 'USER'> = 'TENANT',
  options: HomepageConfigRequestOptions = {},
) {
  return http.post<PersonalHomepageConfig>('/sys/homepage/manage/get', { ...options, scopeLevel })
}

export function saveManagePersonalHomepageConfig(
  scopeLevel: Exclude<PersonalHomepageScopeLevel, 'USER'>,
  config: PersonalHomepageConfig,
  options: HomepageConfigRequestOptions = {},
) {
  return http.post<boolean>('/sys/homepage/manage/save', { ...options, scopeLevel, config })
}

/**
 * 个人首页摘要信息
 */
export interface PersonalHomepageSummaryVO {
  userId: number
  avatar: string
  nickname: string
  account: string
  /**
   * 性别：0=未知，1=男，2=女（与后端 SysUser 一致）
   */
  gender?: number | null
  onlineDurationMinutes: number
  onlineDurationText: string
  greeting: string
  greetingSubtitle: string
}

/**
 * 获取个人首页摘要信息（头像、昵称、在线时长、问候语）
 */
export function getPersonalHomepageSummary() {
  return http.get<PersonalHomepageSummaryVO>('/sys/homepage/summary')
}

/**
 * 个人首页菜单项。
 * <p>
 * 同时用于“常用菜单”和“我的收藏”两类卡片的数据展示。
 * </p>
 */
export interface PersonalMenuEntry {
  /** 菜单完整路由路径。 */
  path: string

  /** 菜单标题。 */
  title: string

  /** 所属模块编码。 */
  moduleCode: string

  /** 所属模块名称。 */
  moduleName: string

  /** 菜单图标名称。 */
  icon?: string

  /** 菜单访问次数，仅常用菜单会返回。 */
  visitCount?: number | null

  /** 当前菜单是否已被用户收藏。 */
  favorite?: boolean

  /** 最近访问时间，仅常用菜单会返回。 */
  lastVisitedAt?: string | null
}

/**
 * 获取当前用户常用菜单。
 *
 * @param limit 兼容保留参数，当前后端固定返回 Top 6
 */
export function getUserCommonMenus(limit = 6) {
  return http.post<PersonalMenuEntry[]>('/sys/menu/personal/common/list', { limit })
}

/**
 * 获取当前用户收藏菜单。
 *
 * @param limit 返回条数，默认取 6 条
 */
export function getUserFavoriteMenus(limit = 6) {
  return http.post<PersonalMenuEntry[]>('/sys/menu/personal/favorites/list', { limit })
}

/**
 * 获取收藏管理页面列表。
 */
export function getUserFavoriteManageMenus() {
  return http.post<PersonalMenuEntry[]>('/sys/menu/personal/favorites/manage/list', {})
}

/**
 * 切换菜单收藏状态。
 *
 * @param path 菜单完整路由路径
 */
export function toggleUserFavoriteMenu(path: string) {
  return http.post<boolean>('/sys/menu/personal/favorites/toggle', { path })
}

/**
 * 批量取消收藏菜单。
 *
 * @param paths 待取消收藏的菜单完整路径列表
 */
export function batchCancelUserFavoriteMenus(paths: string[]) {
  return http.post<number>('/sys/menu/personal/favorites/manage/batch-cancel', { paths })
}

/**
 * 保存收藏菜单排序。
 *
 * @param paths 按目标顺序排列的菜单完整路径列表
 */
export function sortUserFavoriteMenus(paths: string[]) {
  return http.post<boolean>('/sys/menu/personal/favorites/manage/sort', { paths })
}

/**
 * 上报菜单访问记录。
 *
 * @param path 当前访问菜单的完整路由路径
 */
export function reportUserMenuVisit(path: string) {
  return http.post<boolean>('/sys/menu/personal/visit/report', { path })
}

export interface UserMenuOpenReportVO {
  path: string
  openCount: number
  firstOpen: boolean
}

/**
 * 上报菜单打开次数。
 *
 * @param path 当前打开菜单的完整路由路径
 * @param payload 可选菜单冗余信息，后端仍以授权菜单快照为准
 */
export function reportUserMenuOpen(path: string, payload: Partial<PersonalMenuEntry> = {}) {
  return http.post<UserMenuOpenReportVO>('/sys/menu/personal/open/report', { ...payload, path })
}

