import http from '../http';
/**
 * 获取用户布局样式
 * @param body 参数
 * @returns 布局样式
 */
export function getUserLayoutStyle(body) {
    return http.post('/sys/user-style/get-layout', body);
}
/**
 * 保存用户布局样式
 * @param body 样式数据
 * @returns 结果
 */
export function saveUserLayoutStyle(body) {
    return http.post('/sys/user-style/save-layout', body);
}
