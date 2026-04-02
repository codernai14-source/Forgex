-- 检查角色管理相关权限是否分配给系统管理员角色
-- 执行前请替换 {tenant_id} 为实际的租户 ID

-- 1. 查看当前租户 ID
SELECT id, tenant_name, tenant_code FROM sys_tenant;

-- 2. 查看角色管理菜单及其按钮权限
SELECT 
    m.id,
    m.name,
    m.path,
    m.perm_key,
    m.type,
    m.parent_id
FROM sys_menu m
WHERE m.path LIKE '%role%' 
   OR m.perm_key LIKE 'sys:role:%'
ORDER BY m.parent_id, m.order_num;

-- 3. 查看系统管理员角色（role_key = 'admin'）
SELECT 
    r.id,
    r.role_name,
    r.role_key,
    r.tenant_id
FROM sys_role r
WHERE r.role_key = 'admin';

-- 4. 检查系统管理员角色是否关联了角色管理相关菜单权限
-- 请将 {admin_role_id} 替换为步骤 3 查询到的 admin 角色 ID
-- 请将 {tenant_id} 替换为实际的租户 ID
SELECT 
    rmp.role_id,
    rmp.menu_id,
    m.name AS menu_name,
    m.perm_key AS menu_perm_key,
    m.type AS menu_type
FROM sys_role_menu_perm rmp
JOIN sys_menu m ON rmp.menu_id = m.id
WHERE rmp.role_id = {admin_role_id}
  AND rmp.tenant_id = {tenant_id}
  AND (m.path LIKE '%role%' OR m.perm_key LIKE 'sys:role:%')
ORDER BY m.order_num;

-- 5. 检查 admin 用户是否关联了 admin 角色
-- 请将 {tenant_id} 替换为实际的租户 ID
SELECT 
    urp.user_id,
    urp.role_id,
    u.username,
    r.role_name,
    r.role_key
FROM sys_user_role_perm urp
JOIN sys_user u ON urp.user_id = u.id
JOIN sys_role r ON urp.role_id = r.id
WHERE urp.tenant_id = {tenant_id}
  AND r.role_key = 'admin';

-- 6. 【修复脚本】如果缺少权限，执行以下 SQL 重新分配
-- 注意：需要根据实际查询结果替换 ID 值

-- 6.1 首先获取角色管理菜单 ID（如果还没有关联）
-- 请将 {tenant_id} 替换为实际的租户 ID
SELECT id FROM sys_menu 
WHERE path = 'role' AND tenant_id = {tenant_id};

-- 6.2 获取角色管理的所有按钮权限 ID
-- 请将 {tenant_id} 替换为实际的租户 ID，将 {role_menu_id} 替换为角色管理菜单的 parent_id
SELECT id, name, perm_key FROM sys_menu 
WHERE parent_id = {role_menu_id} 
  AND tenant_id = {tenant_id}
  AND type = 'button';

-- 6.3 为 admin 角色添加角色管理菜单及按钮权限
-- 请将以下占位符替换为实际 ID：
-- {admin_role_id}: admin 角色 ID
-- {tenant_id}: 租户 ID
-- {role_menu_id}: 角色管理菜单 ID
-- {role_add_button_id}: 新增角色按钮 ID
-- {role_edit_button_id}: 编辑角色按钮 ID
-- {role_delete_button_id}: 删除角色按钮 ID
-- {role_auth_menu_button_id}: 菜单授权按钮 ID
-- {role_auth_user_button_id}: 用户授权按钮 ID

-- 添加角色管理菜单权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_menu_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_menu_id} 
      AND tenant_id = {tenant_id}
);

-- 添加新增角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_add_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_add_button_id} 
      AND tenant_id = {tenant_id}
);

-- 添加编辑角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_edit_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_edit_button_id} 
      AND tenant_id = {tenant_id}
);

-- 添加删除角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_delete_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_delete_button_id} 
      AND tenant_id = {tenant_id}
);

-- 添加菜单授权按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_auth_menu_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_auth_menu_button_id} 
      AND tenant_id = {tenant_id}
);

-- 添加用户授权按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_auth_user_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_auth_user_button_id} 
      AND tenant_id = {tenant_id}
);
