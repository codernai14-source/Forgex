import http from '../http';
/**
 * 获取仪表盘统计数据
 * @param body 参数
 * @returns 统计数据
 */
export function getDashboardStatistics(body) {
    return http.post('/sys/dashboard/statistics', body);
}
