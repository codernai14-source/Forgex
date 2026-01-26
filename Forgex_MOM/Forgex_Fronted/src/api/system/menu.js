/**
 * 菜单管理API
 */
import http from '../http';
/**
 * 分页查询菜单列表
 */
export function getMenuPage(params) {
    return http.post('/sys/menu/page', params);
}
/**
 * 查询菜单列表
 */
export function getMenuList(params) {
    return http.post('/sys/menu/list', params || {});
}
/**
 * 获取菜单树
 */
export function getMenuTree(data) {
    return http.post('/sys/menu/tree', data);
}
/**
 * 根据ID获取菜单详情
 */
export function getMenuById(id) {
    return http.post('/sys/menu/detail', { id });
}
/**
 * 新增菜单
 */
export function addMenu(data) {
    return http.post('/sys/menu/create', data);
}
/**
 * 更新菜单
 */
export function updateMenu(data) {
    return http.post('/sys/menu/update', data);
}
/**
 * 删除菜单
 */
export function deleteMenu(id) {
    return http.post('/sys/menu/delete', { id });
}
/**
 * 批量删除菜单
 */
export function batchDeleteMenus(ids) {
    return http.post('/sys/menu/batchDelete', { ids });
}
/**
 * 获取菜单树（别名，兼容旧代码）
 */
export const listMenusTree = getMenuTree;
