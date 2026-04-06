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
  'pendingApprovals',
  'calendar',
  'messages',
  'notices',
  'currentTime',
] as const

const DEFAULT_WIDGET_TITLES: Record<(typeof PERSONAL_HOMEPAGE_WIDGET_KEYS)[number], string> = {
  commonMenus: '常用菜单',
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
      createWidget('commonMenus', 0, 0, 6, 4, 10, 8, true),
      createWidget('pendingApprovals', 6, 0, 6, 4, 20, 6, true),
      createWidget('calendar', 0, 4, 4, 4, 30, 0, false),
      createWidget('messages', 4, 4, 4, 4, 40, 8, true),
      createWidget('notices', 8, 4, 4, 4, 50, 8, true),
      createWidget('currentTime', 0, 8, 3, 3, 60, 0, false),
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
