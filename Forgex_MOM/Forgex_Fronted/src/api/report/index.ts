/**
 * 报表模块 API 接口封装
 * 提供报表模板、报表分类、数据源的 CRUD 操作
 * @author Forgex Team
 * @version 1.0
 * @since 2026-04-09
 */
import http from '../http'
import type {
  ReportTemplate,
  ReportTemplateParam,
  ReportCategory,
  ReportCategoryParam,
  ReportDatasource,
  ReportDatasourceParam,
  ReportSaveDTO,
  DatasourceSaveDTO,
  PageResult,
} from './types'

/**
 * 报表模板管理 API
 */

/**
 * 分页查询报表模板
 * @param params 查询参数
 * @returns 分页结果
 * @see ReportTemplateParam - 查询参数类型
 * @see ReportTemplate - 返回数据类型
 */
export function page(params: ReportTemplateParam) {
  return http<PageResult<ReportTemplate>>({
    url: '/report/template/page',
    method: 'get',
    params,
  })
}

/**
 * 根据编码获取报表模板
 * @param code 报表编码
 * @param engineType 引擎类型（可选，默认 UREPORT）
 * @returns 报表模板详情
 * @see ReportTemplate - 返回数据类型
 */
export function getByCode(code: string, engineType = 'UREPORT') {
  return http<ReportTemplate>({
    url: `/report/template/${code}`,
    method: 'get',
    params: { engineType },
  })
}

/**
 * 根据 ID 获取报表模板
 * @param id 报表 ID
 * @returns 报表模板详情
 * @see ReportTemplate - 返回数据类型
 */
export function getById(id: number) {
  return http<ReportTemplate>({
    url: `/report/template/${id}`,
    method: 'get',
  })
}

/**
 * 保存报表模板（新增或更新）
 * @param data 报表数据
 * @returns 操作结果
 * @see ReportSaveDTO - 保存数据类型
 */
export function save(data: ReportSaveDTO) {
  return http({
    url: '/report/template/save',
    method: 'post',
    data,
  })
}

/**
 * 删除报表模板
 * @param id 报表 ID
 * @returns 操作结果
 */
export function remove(id: number) {
  return http({
    url: `/report/template/${id}`,
    method: 'delete',
  })
}

/**
 * 获取报表设计器 URL
 * @param code 报表编码
 * @param engineType 引擎类型
 * @returns 设计器 URL
 */
export function getDesignerUrl(code: string, engineType = 'UREPORT'): string {
  const baseUrl = window.location.origin
  if (engineType === 'UREPORT') {
    return `${baseUrl}/ureport/designer?_u=${encodeURIComponent(`/api/report/template/${code}/content`)}`
  } else if (engineType === 'JIMU') {
    return `${baseUrl}/jimureport/design?reportCode=${code}`
  }
  return ''
}

/**
 * 预览报表
 * @param code 报表编码
 * @param engineType 引擎类型
 * @param params 报表参数
 * @returns 预览 URL
 */
export function getPreviewUrl(code: string, engineType = 'UREPORT', params?: Record<string, any>): string {
  const baseUrl = window.location.origin
  let url = ''
  
  if (engineType === 'UREPORT') {
    url = `${baseUrl}/ureport/preview?_u=${encodeURIComponent(`/api/report/template/${code}/content`)}`
  } else if (engineType === 'JIMU') {
    url = `${baseUrl}/jimureport/preview?reportCode=${code}`
  }
  
  if (params && Object.keys(params).length > 0) {
    const paramStr = Object.entries(params)
      .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(value)}`)
      .join('&')
    url += `&${paramStr}`
  }
  
  return url
}

/**
 * 测试数据源连接
 * @param datasourceId 数据源 ID
 * @returns 测试结果
 */
export function testDatasource(datasourceId: number) {
  return http({
    url: `/report/datasource/test/${datasourceId}`,
    method: 'post',
  })
}

/**
 * 报表分类管理 API
 */

/**
 * 分页查询报表分类
 * @param params 查询参数
 * @returns 分页结果
 * @see ReportCategoryParam - 查询参数类型
 * @see ReportCategory - 返回数据类型
 */
export function pageCategory(params: ReportCategoryParam) {
  return http<PageResult<ReportCategory>>({
    url: '/report/category/page',
    method: 'get',
    params,
  })
}

/**
 * 获取分类树
 * @param params 查询参数
 * @returns 分类树
 * @see ReportCategory - 返回数据类型
 */
export function getCategoryTree(params?: { parentId?: number }) {
  return http<ReportCategory[]>({
    url: '/report/category/tree',
    method: 'get',
    params,
  })
}

/**
 * 根据 ID 获取分类
 * @param id 分类 ID
 * @returns 分类详情
 * @see ReportCategory - 返回数据类型
 */
export function getCategoryById(id: number) {
  return http<ReportCategory>({
    url: `/report/category/${id}`,
    method: 'get',
  })
}

/**
 * 保存分类
 * @param data 分类数据
 * @returns 操作结果
 */
export function saveCategory(data: Partial<ReportCategory>) {
  return http({
    url: '/report/category/save',
    method: 'post',
    data,
  })
}

/**
 * 删除分类
 * @param id 分类 ID
 * @returns 操作结果
 */
export function removeCategory(id: number) {
  return http({
    url: `/report/category/${id}`,
    method: 'delete',
  })
}

/**
 * 报表数据源管理 API
 */

/**
 * 分页查询数据源
 * @param params 查询参数
 * @returns 分页结果
 * @see ReportDatasourceParam - 查询参数类型
 * @see ReportDatasource - 返回数据类型
 */
export function pageDatasource(params: ReportDatasourceParam) {
  return http<PageResult<ReportDatasource>>({
    url: '/report/datasource/page',
    method: 'get',
    params,
  })
}

/**
 * 根据编码获取数据源
 * @param code 数据源编码
 * @returns 数据源详情
 * @see ReportDatasource - 返回数据类型
 */
export function getDatasourceByCode(code: string) {
  return http<ReportDatasource>({
    url: `/report/datasource/${code}`,
    method: 'get',
  })
}

/**
 * 根据 ID 获取数据源
 * @param id 数据源 ID
 * @returns 数据源详情
 * @see ReportDatasource - 返回数据类型
 */
export function getDatasourceById(id: number) {
  return http<ReportDatasource>({
    url: `/report/datasource/${id}`,
    method: 'get',
  })
}

/**
 * 保存数据源（新增或更新）
 * @param data 数据源数据
 * @returns 操作结果
 * @see DatasourceSaveDTO - 保存数据类型
 */
export function saveDatasource(data: DatasourceSaveDTO) {
  return http({
    url: '/report/datasource/save',
    method: 'post',
    data,
  })
}

/**
 * 删除数据源
 * @param id 数据源 ID
 * @returns 操作结果
 */
export function removeDatasource(id: number) {
  return http({
    url: `/report/datasource/${id}`,
    method: 'delete',
  })
}

/**
 * 测试数据源连接
 * @param data 数据源配置（用于测试未保存的数据源）
 * @returns 测试结果
 */
export function testDatasourceConfig(data: DatasourceSaveDTO) {
  return http({
    url: '/report/datasource/test',
    method: 'post',
    data,
  })
}

/**
 * 获取所有可用的数据源列表（用于下拉框）
 * @returns 数据源列表
 * @see ReportDatasource - 返回数据类型
 */
export function getAvailableDatasources() {
  return http<ReportDatasource[]>({
    url: '/report/datasource/list/available',
    method: 'get',
  })
}
