import http from '../http'

/**
 * Excel 导入导出配置与文件接口。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */

export function pageExportConfig(body: any) {
  return http.post('/sys/excel/exportConfig/page', body)
}

export function exportConfigDetail(body: any) {
  return http.post('/sys/excel/exportConfig/detail', body)
}

export function saveExportConfig(body: any) {
  return http.post('/sys/excel/exportConfig/save', body)
}

export function deleteExportConfig(body: any) {
  return http.post('/sys/excel/exportConfig/delete', body)
}

export function pageImportConfig(body: any) {
  return http.post('/sys/excel/importConfig/page', body)
}

export function importConfigDetail(body: any) {
  return http.post('/sys/excel/importConfig/detail', body)
}

export function saveImportConfig(body: any) {
  return http.post('/sys/excel/importConfig/save', body)
}

export function deleteImportConfig(body: any) {
  return http.post('/sys/excel/importConfig/delete', body)
}

export function downloadTemplate(body: any) {
  return http.post('/sys/excel/template/download', body, { responseType: 'blob' })
}

export function exportLoginLog(body: any) {
  return http.post('/sys/excel/export/loginLog', body, { responseType: 'blob' })
}

export function exportUser(body: any) {
  return http.post('/sys/excel/export/user', body, { responseType: 'blob' })
}

/**
 * 获取 Provider 列表
 * @param body 查询参数
 * @returns Provider 列表
 */
export function getProviderList(body: any) {
  return http.post('/sys/excel/provider/list', body)
}

/**
 * 获取 Provider 字段列表
 * @param body 查询参数，包含 providerCode
 * @returns Provider 字段列表
 */
export function getProviderFields(body: { providerCode: string }) {
  return http.post('/sys/excel/provider/fields', body)
}

