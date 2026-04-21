import http from '../http'

export type PersonalHomepageScopeLevel = 'PUBLIC' | 'TENANT' | 'USER'

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

export function getCurrentPersonalHomepageConfig() {
  return http.post<PersonalHomepageConfig>('/sys/homepage/current/get', {})
}

export function saveCurrentPersonalHomepageConfig(config: PersonalHomepageConfig) {
  return http.post<boolean>('/sys/homepage/current/save', { config })
}

export function resetCurrentPersonalHomepageConfig() {
  return http.post<boolean>('/sys/homepage/current/reset', {})
}

export function getManagePersonalHomepageConfig(scopeLevel: Exclude<PersonalHomepageScopeLevel, 'USER'> = 'TENANT') {
  return http.post<PersonalHomepageConfig>('/sys/homepage/manage/get', { scopeLevel })
}

export function saveManagePersonalHomepageConfig(
  scopeLevel: Exclude<PersonalHomepageScopeLevel, 'USER'>,
  config: PersonalHomepageConfig,
) {
  return http.post<boolean>('/sys/homepage/manage/save', { scopeLevel, config })
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

