/**
 * 角色管理模块 - 类型定义
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
  updateTime?: string
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
 * 说明：sys_menu表的type字段区分菜单(menu)和按钮(button)
 * 按钮作为菜单的子项，通过parent_id关联
 * sys_role_menu表统一管理菜单和按钮权限
 */
export interface RolePermission {
  roleId: string
  menuIds: string[]      // 菜单和按钮权限ID列表（统一使用sys_role_menu表）
}
