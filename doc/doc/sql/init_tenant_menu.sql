-- ============================================
-- 初始化租户管理菜单
-- 创建时间：2026-01-11
-- 说明：添加租户管理相关菜单到系统中
-- ============================================

-- 1. 使用已知的系统模块ID
SET @sys_module_id = 1;

-- 2. 新增租户管理菜单目录
INSERT INTO sys_menu (
    module_id, 
    parent_id, 
    type, 
    path, 
    name, 
    icon, 
    component_key, 
    perm_key, 
    order_num, 
    tenant_id
) VALUES (
    @sys_module_id, 
    0, 
    'catalog', 
    '/tenant', 
    '租户管理', 
    'cluster', 
    'system-tenant', 
    'sys:tenant', 
    10, 
    NULL
);

-- 3. 获取刚创建的租户管理目录ID
SET @tenant_catalog_id = LAST_INSERT_ID();

-- 4. 新增租户列表菜单
INSERT INTO sys_menu (
    module_id, 
    parent_id, 
    type, 
    path, 
    name, 
    icon, 
    component_key, 
    perm_key, 
    order_num, 
    tenant_id
) VALUES (
    @sys_module_id, 
    @tenant_catalog_id, 
    'menu', 
    '/tenant/index', 
    '租户列表', 
    'table', 
    'system-tenant-index', 
    'sys:tenant:list', 
    1, 
    NULL
);

-- 5. 新增租户管理按钮权限
-- 新增租户按钮
INSERT INTO sys_menu (
    module_id, 
    parent_id, 
    type, 
    path, 
    name, 
    perm_key, 
    order_num, 
    visible, 
    tenant_id
) VALUES (
    @sys_module_id, 
    @tenant_catalog_id, 
    'button', 
    '', 
    '新增租户', 
    'sys:tenant:add', 
    1, 
    0, 
    NULL
);

-- 编辑租户按钮
INSERT INTO sys_menu (
    module_id, 
    parent_id, 
    type, 
    path, 
    name, 
    perm_key, 
    order_num, 
    visible, 
    tenant_id
) VALUES (
    @sys_module_id, 
    @tenant_catalog_id, 
    'button', 
    '', 
    '编辑租户', 
    'sys:tenant:edit', 
    2, 
    0, 
    NULL
);

-- 删除租户按钮
INSERT INTO sys_menu (
    module_id, 
    parent_id, 
    type, 
    path, 
    name, 
    perm_key, 
    order_num, 
    visible, 
    tenant_id
) VALUES (
    @sys_module_id, 
    @tenant_catalog_id, 
    'button', 
    '', 
    '删除租户', 
    'sys:tenant:delete', 
    3, 
    0, 
    NULL
);

SELECT '租户管理菜单初始化完成' AS result;