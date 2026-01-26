import http from '../http';
export function getTableConfig(data) {
    return http.post('/sys/common/table/config/get', data, { silentError: true });
}
export function getTableConfigList(params) {
    return http.post('/sys/common/table/config/list', params);
}
export function getTableConfigDetail(id) {
    return http.get(`/sys/common/table/config/${id}`);
}
export function createTableConfig(data) {
    return http.post('/sys/common/table/config', data);
}
export function updateTableConfig(data) {
    return http.put(`/sys/common/table/config/${data.id}`, data);
}
export function deleteTableConfig(id) {
    return http.delete(`/sys/common/table/config/${id}`);
}
export function batchDeleteTableConfig(ids) {
    return http.delete('/sys/common/table/config/batch', { data: ids });
}
export function toggleTableConfigStatus(id, enabled) {
    return http.put(`/sys/common/table/config/${id}/status`, { enabled });
}
