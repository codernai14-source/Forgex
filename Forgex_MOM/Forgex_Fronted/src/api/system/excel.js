import http from '../http';
/**
 * Excel 导入导出配置与文件接口。
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
export function pageExportConfig(body) {
    return http.post('/sys/excel/exportConfig/page', body);
}
export function exportConfigDetail(body) {
    return http.post('/sys/excel/exportConfig/detail', body);
}
export function saveExportConfig(body) {
    return http.post('/sys/excel/exportConfig/save', body);
}
export function deleteExportConfig(body) {
    return http.post('/sys/excel/exportConfig/delete', body);
}
export function pageImportConfig(body) {
    return http.post('/sys/excel/importConfig/page', body);
}
export function importConfigDetail(body) {
    return http.post('/sys/excel/importConfig/detail', body);
}
export function saveImportConfig(body) {
    return http.post('/sys/excel/importConfig/save', body);
}
export function deleteImportConfig(body) {
    return http.post('/sys/excel/importConfig/delete', body);
}
export function downloadTemplate(body) {
    return http.post('/sys/excel/template/download', body, { responseType: 'blob' });
}
export function exportLoginLog(body) {
    return http.post('/sys/excel/export/loginLog', body, { responseType: 'blob' });
}
export function exportUser(body) {
    return http.post('/sys/excel/export/user', body, { responseType: 'blob' });
}
