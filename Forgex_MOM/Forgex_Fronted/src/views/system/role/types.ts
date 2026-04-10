/**
 * 角色管理模块 - 类型定义
 * 
 * @author Forgex
 * @version 1.0.0
 */

/**
 * 角色信息
 */
export interface Role {
  id?: string
  roleName: string
  roleCode: string
  description?: string
  status: number
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
}

/**
 * 角色查询参数
 */
export interface RoleQuery {
  roleName?: string
  roleCode?: string
  status?: number
  pageNum: number
  pageSize: number
}

/**
 * 角色权限配置
 * 说明：sys_menu 表的 type 字段区分菜单 (menu) 和按钮 (button)
 * 按钮作为菜单的子项，通过 parent_id 关联
 * sys_role_menu 表统一管理菜单和按钮权限
 */
export interface RolePermission {
  roleId: string
  menuIds: string[]      // 菜单和按钮权限 ID 列表（统一使用 sys_role_menu 表）
}

/**
 * 菜单树形记录类型
 * 用于树形表格展示
 */
export interface MenuTreeRecord {
  id?: string | number
  moduleId?: string | number
  parentId?: string | number
  type?: string
  menuMode?: string
  menuLevel?: number
  name?: string
  nameI18nJson?: string
  path?: string
  icon?: string
  componentKey?: string
  permKey?: string
  externalUrl?: string
  orderNum?: number
  visible?: boolean | number | string
  status?: boolean | number | string
  checked?: boolean
  children?: MenuTreeRecord[]
}

/**
 * 角色授权记录
 * 用于人员授权列表
 */
export interface RoleGrantVO {
  id: string
  roleId: string
  grantType: 'USER' | 'DEPARTMENT' | 'POSITION'
  grantObject: string
  grantObjectId: string
  createTime?: string
  createBy?: string
  updateTime?: string
  updateBy?: string
}

/**
 * 人员授权参数
 */
export interface GrantBatchParams {
  roleId: number
  tenantId: string
  grantType?: string
  userIds?: number[]
  departmentIds?: string[]
  positionIds?: string[]
}
