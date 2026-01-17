-- ================================================
-- 菜单结构整合脚本
-- 创建时间：2026-01-17
-- 说明：将菜单重新组织为二级目录结构
--       - 组织架构：用户管理、部门管理、职位管理
--       - 授权管理：角色管理、租户管理、菜单管理
-- ================================================

USE forgex_admin;

-- 开始事务
START TRANSACTION;

-- ================================================
-- 步骤1：创建组织架构二级目录
-- ================================================
INSERT INTO sys_menu (
    tenant_id,
    module_id,
    parent_id,
    type,
    path,
    name,
    icon,
    component_key,
    perm_key,
    order_num,
    visible,
    status,
    menu_level,
    menu_mode,
    create_by,
    create_time,
    update_by,
    update_time
) VALUES (
    1,
    1,
    0,
    'catalog',
    'organization',
    '组织架构',
    'ApartmentOutlined',
    'SystemOrganization',
    'sys:organization:view',
    2,
    1,
    1,
    1,
    'embedded',
    'system',
    NOW(),
    'system',
    NOW()
);

SET @ORG_ID = LAST_INSERT_ID();

-- ================================================
-- 步骤2：创建授权管理二级目录
-- ================================================
INSERT INTO sys_menu (
    tenant_id,
    module_id,
    parent_id,
    type,
    path,
    name,
    icon,
    component_key,
    perm_key,
    order_num,
    visible,
    status,
    menu_level,
    menu_mode,
    create_by,
    create_time,
    update_by,
    update_time
) VALUES (
    1,
    1,
    0,
    'catalog',
    'authorization',
    '授权管理',
    'SafetyOutlined',
    'SystemAuthorization',
    'sys:authorization:view',
    3,
    1,
    1,
    1,
    'embedded',
    'system',
    NOW(),
    'system',
    NOW()
);

SET @AUTH_ID = LAST_INSERT_ID();

-- ================================================
-- 步骤3：更新组织架构下的菜单parent_id和排序
-- 用户管理(id:1)、部门管理(id:5)、职位管理(id:6)
-- ================================================
UPDATE sys_menu 
SET parent_id = @ORG_ID,
    menu_level = 2,
    update_time = NOW()
WHERE id = 1;

UPDATE sys_menu 
SET parent_id = @ORG_ID,
    menu_level = 2,
    order_num = 2,
    update_time = NOW()
WHERE id = 5;

UPDATE sys_menu 
SET parent_id = @ORG_ID,
    menu_level = 2,
    order_num = 3,
    update_time = NOW()
WHERE id = 6;

-- ================================================
-- 步骤4：更新授权管理下的菜单parent_id和排序
-- 角色管理(id:2)、租户管理(id:646)、菜单管理(id:4)
-- ================================================
UPDATE sys_menu 
SET parent_id = @AUTH_ID,
    menu_level = 2,
    order_num = 1,
    update_time = NOW()
WHERE id = 2;

UPDATE sys_menu 
SET parent_id = @AUTH_ID,
    menu_level = 2,
    order_num = 2,
    update_time = NOW()
WHERE id = 646;

UPDATE sys_menu 
SET parent_id = @AUTH_ID,
    menu_level = 2,
    order_num = 3,
    update_time = NOW()
WHERE id = 4;

-- ================================================
-- 步骤5：更新其他一级菜单的order_num
-- ================================================
UPDATE sys_menu 
SET order_num = 4,
    update_time = NOW()
WHERE id = 3;  -- 模块管理

UPDATE sys_menu 
SET order_num = 5,
    update_time = NOW()
WHERE id = 607;  -- 导出配置

UPDATE sys_menu 
SET order_num = 6,
    update_time = NOW()
WHERE id = 608;  -- 导入配置

UPDATE sys_menu 
SET order_num = 7,
    update_time = NOW()
WHERE id = 620;  -- 字典管理

UPDATE sys_menu 
SET order_num = 8,
    update_time = NOW()
WHERE id = 632;  -- 表格配置

UPDATE sys_menu 
SET order_num = 9,
    update_time = NOW()
WHERE id = 638;  -- 登录日志

UPDATE sys_menu 
SET order_num = 10,
    update_time = NOW()
WHERE id = 642;  -- 在线用户

-- ================================================
-- 步骤6：更新按钮权限的parent_id
-- 由于父菜单ID没有变化，按钮的parent_id保持不变
-- 只需要确保menu_level正确设置为2（按钮在二级菜单下）
-- ================================================
UPDATE sys_menu 
SET menu_level = 2,
    update_time = NOW()
WHERE parent_id IN (1, 2, 4, 5, 6, 646)
  AND type = 'button';

-- ================================================
-- 步骤7：将新创建的两个目录添加到系统管理员角色的权限中
-- ================================================
INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 1, id, @ORG_ID FROM sys_role WHERE role_key = 'admin';

INSERT INTO sys_role_menu (tenant_id, role_id, menu_id)
SELECT 1, id, @AUTH_ID FROM sys_role WHERE role_key = 'admin';

-- 提交事务
COMMIT;

-- ================================================
-- 执行结果展示
-- ================================================
SELECT '菜单结构整合完成！' AS message;

-- 查看新的菜单结构
SELECT 
    id,
    parent_id,
    type,
    menu_level,
    path,
    name,
    icon,
    order_num
FROM sys_menu 
WHERE module_id = 1
ORDER BY order_num, id;

-- ================================================
-- 回滚脚本（如需回滚，请执行以下SQL）
-- ================================================
/*
START TRANSACTION;

-- 恢复组织架构下菜单的parent_id和order_num
UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 10
WHERE id = 1;

UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 50
WHERE id = 5;

UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 60
WHERE id = 6;

-- 恢复授权管理下菜单的parent_id和order_num
UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 20
WHERE id = 2;

UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 140
WHERE id = 646;

UPDATE sys_menu 
SET parent_id = 0,
    menu_level = 1,
    order_num = 40
WHERE id = 4;

-- 恢复其他一级菜单的order_num
UPDATE sys_menu SET order_num = 30 WHERE id = 3;   -- 模块管理
UPDATE sys_menu SET order_num = 70 WHERE id = 607;  -- 导出配置
UPDATE sys_menu SET order_num = 80 WHERE id = 608;  -- 导入配置
UPDATE sys_menu SET order_num = 90 WHERE id = 620;  -- 字典管理
UPDATE sys_menu SET order_num = 110 WHERE id = 632;  -- 表格配置
UPDATE sys_menu SET order_num = 120 WHERE id = 638;  -- 登录日志
UPDATE sys_menu SET order_num = 130 WHERE id = 642;  -- 在线用户

-- 删除组织架构目录
DELETE FROM sys_menu WHERE id = @ORG_ID;

-- 删除授权管理目录
DELETE FROM sys_menu WHERE id = @AUTH_ID;

-- 删除角色菜单关联
DELETE FROM sys_role_menu WHERE menu_id = @ORG_ID OR menu_id = @AUTH_ID;

COMMIT;
*/
