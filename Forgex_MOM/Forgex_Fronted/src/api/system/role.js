import http from '../http';
/**
 * 获取角色列表
 * @param params 查询参数
 * @returns 角色列表
 */
export const getRoleList = (params) => {
    return http.post('/sys/role/list', params || {});
};
/**
 * 分页查询角色
 * @param params 分页查询参数
 * @returns 分页结果
 */
export const getRolePage = (params) => {
    return http.post('/sys/role/page', params);
};
/**
 * 根据ID获取角色详情
 * @param id 角色ID
 * @returns 角色详情
 */
export const getRoleById = (id) => {
    return http.post('/sys/role/detail', { id });
};
/**
 * 新增角色
 * @param data 角色数据
 * @returns 结果
 */
export const addRole = (data) => {
    return http.post('/sys/role/create', data);
};
/**
 * 更新角色
 * @param data 角色数据
 * @returns 结果
 */
export const updateRole = (data) => {
    return http.post('/sys/role/update', data);
};
/**
 * 删除角色
 * @param id 角色ID
 * @returns 结果
 */
export const deleteRole = (id) => {
    return http.post('/sys/role/delete', { id });
};
/**
 * 批量删除角色
 * @param ids 角色ID列表
 * @returns 结果
 */
export const batchDeleteRoles = (ids) => {
    return http.post('/sys/role/batchDelete', { ids });
};
/**
 * 获取角色授权数据（包含所有菜单权限和已拥有的权限）
 * @param params 参数 { roleId, tenantId }
 * @returns 授权数据
 */
export const getRoleAuthData = (params) => {
    return http.post('/sys/role/menu/authData', params);
};
/**
 * 获取角色菜单列表
 * @param params 参数 { roleId, tenantId }
 * @returns 菜单列表
 */
export const listRoleMenus = (params) => {
    return http.post('/sys/role/menu/list', params);
};
/**
 * 角色授权菜单
 * @param data 授权数据 { roleId, tenantId, menuIds }
 * @returns 结果
 */
export const grantRoleMenus = (data) => {
    return http.post('/sys/role/menu/grant', data);
};
/**
 * 获取角色已授权的用户列表
 * @param params 参数 { roleId, tenantId, pageNum, pageSize }
 * @returns 用户列表
 */
export const getRoleAuthorizedUsers = (params) => {
    return http.post('/sys/role/user/list', params);
};
/**
 * 角色授权用户
 * @param data 授权数据 { roleId, tenantId, userIds }
 * @returns 结果
 */
export const grantRoleUsers = (data) => {
    return http.post('/sys/role/user/grant', data);
};
/**
 * 角色取消授权用户
 * @param data 取消授权数据 { roleId, tenantId, userIds }
 * @returns 结果
 */
export const revokeRoleUsers = (data) => {
    return http.post('/sys/role/user/revoke', data);
};
