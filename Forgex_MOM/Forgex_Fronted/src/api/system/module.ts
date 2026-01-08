import http from '../http'
import type { Module, ModuleQuery } from '@/views/system/module/types'

/**
 * 分页查询模块列表
 */
export function getModulePage(params: ModuleQuery) {
  return http.post('/sys/module/page', params)
}

/**
 * 查询所有模块列表
 */
export function getModuleList(params?: ModuleQuery) {
  return http.post('/sys/module/list', params || {})
}

/**
 * 根据ID获取模块详情
 */
export function getModuleById(id: string) {
  return http.post('/sys/module/detail', { id })
}

/**
 * 新增模块
 */
export function addModule(data: Module) {
  return http.post('/sys/module/create', data)
}

/**
 * 更新模块
 */
export function updateModule(data: Module) {
  return http.post('/sys/module/update', data)
}

/**
 * 删除模块
 */
export function deleteModule(id: string) {
  return http.post('/sys/module/delete', { id })
}

/**
 * 批量删除模块
 */
export function batchDeleteModules(ids: string[]) {
  return http.post('/sys/module/batchDelete', { ids })
}
