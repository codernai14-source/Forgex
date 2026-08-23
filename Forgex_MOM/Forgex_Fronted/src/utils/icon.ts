/**
 * 图标工具函数
 * 用于动态加载和使用 Ant Design Vue Icons
 */
import * as Icons from '@ant-design/icons-vue'
import { Component } from 'vue'

const legacyIconMap: Record<string, string> = {
  table: 'TableOutlined',
  cluster: 'ClusterOutlined',
  setting: 'SettingOutlined',
  appstore: 'AppstoreOutlined',
}

export const ICONIFY_PRESET_NAMES = [
  'lucide:layout-dashboard',
  'lucide:settings',
  'lucide:users',
  'lucide:user',
  'lucide:building-2',
  'lucide:factory',
  'lucide:boxes',
  'lucide:package',
  'lucide:tag',
  'lucide:printer',
  'lucide:file-text',
  'lucide:database',
  'lucide:table-2',
  'lucide:workflow',
  'lucide:shield-check',
  'lucide:key-round',
  'lucide:bell',
  'lucide:search',
  'lucide:link',
  'lucide:plug',
  'lucide:chart-no-axes-combined',
  'lucide:clipboard-list',
  'lucide:circle-gauge',
  'lucide:wrench',
  'lucide:book-open',
  'lucide:languages',
  'lucide:monitor',
  'lucide:moon',
  'lucide:sun',
  'lucide:folder',
  'lucide:file',
  'lucide:home',
  'lucide:menu',
  'lucide:circle-plus',
  'lucide:circle-check',
  'lucide:circle-x',
  'lucide:triangle-alert',
  'lucide:circle-help',
  'lucide:calendar-days',
  'lucide:clock-3',
  'lucide:mail',
  'lucide:phone',
  'lucide:message-circle',
  'lucide:send',
  'lucide:download',
  'lucide:upload',
  'lucide:cloud',
  'lucide:cloud-upload',
  'lucide:chart-bar',
  'lucide:pie-chart',
  'lucide:banknote',
  'lucide:shopping-cart',
  'lucide:truck',
  'lucide:clipboard-check',
  'lucide:shield',
  'lucide:circle-user-round',
  'lucide:users-round',
  'lucide:heart',
  'lucide:star',
  'lucide:bookmark',
  'lucide:more-horizontal',
] as const

export function isIconifyName(iconName?: string): boolean {
  if (!iconName) {
    return false
  }
  return /^[a-z0-9-]+:[a-z0-9][a-z0-9-:]*$/i.test(iconName.trim())
}

/**
 * 根据图标名称获取图标组件
 * @param iconName 图标名称，直接使用 Ant Design 的完整组件名（如 'UserOutlined'）
 * @returns 图标组件或 null
 */
export function getIcon(iconName?: string): Component | null {
  if (!iconName) {
    return null
  }

  const normalizedName = iconName.trim()
  if (isIconifyName(normalizedName)) {
    return null
  }

  let resolvedName = normalizedName

  if (!(resolvedName in Icons) && legacyIconMap[normalizedName]) {
    resolvedName = legacyIconMap[normalizedName]
  }

  const icon = Icons[resolvedName as keyof typeof Icons]

  if (!icon) {
    console.warn(`图标 "${iconName}" 对应的组件不存在`)
    return null
  }
  
  return icon as Component
}

/**
 * 检查图标是否存在
 * @param iconName 图标名称
 * @returns 是否存在
 */
export function hasIcon(iconName?: string): boolean {
  if (!iconName) {
    return false
  }
  const normalizedName = iconName.trim()
  const legacyName = legacyIconMap[normalizedName]
  return isIconifyName(normalizedName) || normalizedName in Icons || Boolean(legacyName && legacyName in Icons)
}

/**
 * 获取所有可用的图标名称列表
 * @returns 图标名称数组
 */
export function getAllIconNames(): string[] {
  return Object.keys(Icons)
}

/**
 * 常用图标映射
 * 用于快速访问常用图标
 */
export const CommonIcons = {
  // 用户相关
  user: 'UserOutlined',
  userAdd: 'UserAddOutlined',
  team: 'TeamOutlined',
  
  // 系统相关
  setting: 'SettingOutlined',
  dashboard: 'DashboardOutlined',
  menu: 'MenuOutlined',
  
  // 操作相关
  edit: 'EditOutlined',
  delete: 'DeleteOutlined',
  plus: 'PlusOutlined',
  search: 'SearchOutlined',
  
  // 状态相关
  check: 'CheckOutlined',
  close: 'CloseOutlined',
  warning: 'WarningOutlined',
  info: 'InfoCircleOutlined',
  
  // 导航相关
  home: 'HomeOutlined',
  folder: 'FolderOutlined',
  file: 'FileOutlined',
  
  // 其他
  lock: 'LockOutlined',
  unlock: 'UnlockOutlined',
  eye: 'EyeOutlined',
  eyeInvisible: 'EyeInvisibleOutlined',
} as const
