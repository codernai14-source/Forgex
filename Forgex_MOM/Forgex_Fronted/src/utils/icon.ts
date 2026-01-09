/**
 * 图标工具函数
 * 用于动态加载和使用 Ant Design Vue Icons
 */
import * as Icons from '@ant-design/icons-vue'
import { Component } from 'vue'

/**
 * 图标名称映射表
 * 将后端返回的简短名称映射到 Ant Design 图标组件名
 */
const iconNameMap: Record<string, string> = {
  // 用户相关
  'user': 'UserOutlined',
  'userAdd': 'UserAddOutlined',
  'team': 'TeamOutlined',
  
  // 系统相关
  'setting': 'SettingOutlined',
  'dashboard': 'DashboardOutlined',
  'menu': 'MenuOutlined',
  'appstore': 'AppstoreOutlined',
  
  // 操作相关
  'edit': 'EditOutlined',
  'delete': 'DeleteOutlined',
  'plus': 'PlusOutlined',
  'search': 'SearchOutlined',
  
  // 状态相关
  'check': 'CheckOutlined',
  'close': 'CloseOutlined',
  'warning': 'WarningOutlined',
  'info': 'InfoCircleOutlined',
  
  // 导航相关
  'home': 'HomeOutlined',
  'folder': 'FolderOutlined',
  'file': 'FileOutlined',
  
  // 其他
  'lock': 'LockOutlined',
  'unlock': 'UnlockOutlined',
  'eye': 'EyeOutlined',
  'eyeInvisible': 'EyeInvisibleOutlined',
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
  
  // 先通过映射表将简短名称转换为完整组件名
  const mappedName = iconNameMap[iconName] || iconName
  
  // 再从 Icons 对象中获取对应的图标组件
  const icon = Icons[mappedName as keyof typeof Icons]
  
  if (!icon) {
    console.warn(`图标 "${iconName}" 对应的组件 "${mappedName}" 不存在`)
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
  return iconName in Icons
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
