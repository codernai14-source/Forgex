/**
 * 报表模块 API 封装
 * 提供报表模板、报表分类和数据源相关接口。
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
 * 分页查询报表模板
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
 */
export function getById(id: number) {
  return http<ReportTemplate>({
    url: `/report/template/${id}`,
    method: 'get',
  })
}

/**
 * 保存报表模板
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
 */
export function remove(id: number) {
  return http({
    url: `/report/template/${id}`,
    method: 'delete',
  })
}

/**
 * 获取报表设计器地址
 */
export function getDesignerUrl(code: string, engineType = 'UREPORT'): string {
  const baseUrl = window.location.origin
  if (engineType === 'UREPORT') {
    return `${baseUrl}/ureport/designer?_u=${encodeURIComponent(`/api/report/template/${code}/content`)}`
  }
  if (engineType === 'JIMU') {
    return `${baseUrl}/jimureport/design?reportCode=${code}`
  }
  return ''
}

/**
 * 获取报表预览地址
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
 * 测试已保存的数据源连接
 */
export function testDatasource(datasourceId: number) {
  return http({
    url: `/report/datasource/test/${datasourceId}`,
    method: 'post',
  })
}

/**
 * 分页查询报表分类
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
 */
export function getCategoryById(id: number) {
  return http<ReportCategory>({
    url: `/report/category/${id}`,
    method: 'get',
  })
}

/**
 * 保存报表分类
 */
export function saveCategory(data: Partial<ReportCategory>) {
  return http({
    url: '/report/category/save',
    method: 'post',
    data,
  })
}

/**
 * 删除报表分类
 */
export function removeCategory(id: number) {
  return http({
    url: `/report/category/${id}`,
    method: 'delete',
  })
}

/**
 * 分页查询数据源
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
 */
export function getDatasourceByCode(code: string) {
  return http<ReportDatasource>({
    url: `/report/datasource/${code}`,
    method: 'get',
  })
}

/**
 * 根据 ID 获取数据源
 */
export function getDatasourceById(id: number) {
  return http<ReportDatasource>({
    url: `/report/datasource/${id}`,
    method: 'get',
  })
}

/**
 * 保存数据源
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
 */
export function removeDatasource(id: number) {
  return http({
    url: `/report/datasource/${id}`,
    method: 'delete',
  })
}

/**
 * 测试未保存的数据源配置
 */
export function testDatasourceConfig(data: DatasourceSaveDTO) {
  return http({
    url: '/report/datasource/test',
    method: 'post',
    data,
  })
}

/**
 * 获取可用数据源列表
 */
export function getAvailableDatasources() {
  return http<ReportDatasource[]>({
    url: '/report/datasource/list/available',
    method: 'get',
  })
}
