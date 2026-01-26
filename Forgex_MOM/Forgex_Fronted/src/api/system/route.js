import http from '../http';
/**
 * 获取用户路由
 * @param body 参数 { account, tenantId }
 * @returns 路由列表
 */
export function getRoutes(body) {
    return http.post('/sys/menu/routes', body);
}
