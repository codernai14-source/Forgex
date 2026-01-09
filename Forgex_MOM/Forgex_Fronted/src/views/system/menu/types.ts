/**
 * 菜单管理模块 - 类型定义
 */

/**
 * 菜单信息
 */
export interface Menu {
  id?: string
  moduleId: string
  moduleName?: string      // 模块名称（关联查询）
  parentId: string
  parentName?: string      // 父菜单名称（关联查询）
  type: string
  menuLevel: number        // 菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单
  path: string
  name: string
  icon?: string
  componentKey?: string
  permKey?: string
  menuMode: 'embedded' | 'external'  // 菜单模式：embedded=内嵌，external=外联
  externalUrl?: string               // 外联URL
  orderNum: number
  visible: boolean
  status: boolean
  createTime?: string
  updateTime?: string
  children?: Menu[]        // 子菜单
}

/**
 * 菜单查询参数
 */
export interface MenuQuery {
  moduleId?: string
  name?: string
  status?: boolean
}

/**
 * 菜单树节点
 */
export interface MenuTreeNode {
  key: string
  title: string
  value: string
  children?: MenuTreeNode[]
}

/**
 * 菜单类型选项
 */
export const MENU_TYPE_OPTIONS = [
  { label: '目录', value: 'catalog' },
  { label: '菜单', value: 'menu' },
  { label: '按钮', value: 'button' },
]

/**
 * 菜单模式选项
 */
export const MENU_MODE_OPTIONS = [
  { label: '内嵌', value: 'embedded' },
  { label: '外联', value: 'external' },
]

/**
 * 菜单层级选项
 */
export const MENU_LEVEL_OPTIONS = [
  { label: '一级菜单(目录)', value: 1 },
  { label: '二级菜单', value: 2 },
  { label: '三级菜单', value: 3 },
]
