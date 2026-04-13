/**
 * 菜单管理API
 */

import http from '../http'
import type { Menu, MenuQuery } from '@/views/system/menu/types'

/**
 * 分页查询菜单列表
 */
export function getMenuPage(params: {
  pageNum: number
  pageSize: number
} & MenuQuery) {
  return http.post('/sys/menu/page', params)
}

/**
 * 查询菜单列表
 */
export function getMenuList(params?: MenuQuery) {
  return http.post('/sys/menu/list', params || {})
}

/**
 * 获取菜单树
 */
export function getMenuTree(data: {
  tenantId?: number
  moduleId?: number
}) {
  return http.post('/sys/menu/tree', data)
}

/**
 * 根据ID获取菜单详情
 */
export function getMenuById(id: string) {
  return http.post('/sys/menu/detail', { id })
}

/**
 * 新增菜单
 */
export function addMenu(data: Menu) {
  return http.post('/sys/menu/create', data)
}

/**
 * 更新菜单
 */
export function updateMenu(data: Menu) {
  return http.post('/sys/menu/update', data)
}

/**
 * 删除菜单
 */
export function deleteMenu(id: string) {
  return http.post('/sys/menu/delete', { id })
}

/**
 * 批量删除菜单
 */
export function batchDeleteMenus(ids: string[]) {
  return http.post('/sys/menu/batchDelete', { ids })
}

export function getCMenuTree(data: {
  tenantId?: number
  moduleId?: number
}) {
  return http.post('/sys/c-menu/tree', data)
}

export function getCMenuById(id: string) {
  return http.post('/sys/c-menu/detail', { id })
}

export function addCMenu(data: Menu) {
  return http.post('/sys/c-menu/create', data)
}

export function updateCMenu(data: Menu) {
  return http.post('/sys/c-menu/update', data)
}

export function deleteCMenu(id: string) {
  return http.post('/sys/c-menu/delete', { id })
}

export function batchDeleteCMenus(ids: string[]) {
  return http.post('/sys/c-menu/batchDelete', { ids })
}

/**
 * 获取菜单树（别名，兼容旧代码）
 */
export const listMenusTree = getMenuTree
