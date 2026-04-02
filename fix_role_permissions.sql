-- =============================================
-- 修复角色管理页面按钮权限缺失问题
-- 执行环境：开发数据库
-- 说明：检查并修复系统管理员角色的角色管理权限
-- =============================================

-- 步骤 1: 查看当前所有租户
SELECT id, tenant_name, tenant_code FROM sys_tenant;

-- 步骤 2: 查看角色管理相关的菜单和按钮
-- 这些是前端 v-permission 需要的权限码
SELECT 
    id,
    parent_id,
    name,
    path,
    perm_key,
    type,
    order_num
FROM sys_menu_perm
WHERE perm_key LIKE 'sys:role:%'
   OR path = 'role'
ORDER BY parent_id, order_num;

-- 预期结果应该包含：
-- - 角色管理菜单 (path='role', perm_key='sys:role:view', type='menu')
-- - 新增角色按钮 (perm_key='sys:role:add', type='button')
-- - 编辑角色按钮 (perm_key='sys:role:edit', type='button')
-- - 删除角色按钮 (perm_key='sys:role:delete', type='button')
-- - 菜单授权按钮 (perm_key='sys:role:authMenu', type='button')
-- - 用户授权按钮 (perm_key='sys:role:authUser', type='button')

-- 步骤 3: 查看系统管理员角色
SELECT 
    id,
    role_name,
    role_key,
    tenant_id
FROM sys_role
WHERE role_key = 'admin';

-- 步骤 4: 检查 admin 角色是否拥有角色管理的所有权限
-- 请将 {admin_role_id} 替换为步骤 3 查询到的 admin 角色 ID
-- 请将 {tenant_id} 替换为步骤 1 查询到的租户 ID
SELECT 
    m.perm_key,
    m.name,
    m.type,
    rmp.role_id
FROM sys_role_menu_perm rmp
JOIN sys_menu_perm m ON rmp.menu_id = m.id
WHERE rmp.role_id = {admin_role_id}
  AND rmp.tenant_id = {tenant_id}
  AND m.perm_key LIKE 'sys:role:%';

-- 如果查询结果缺少以下任何权限，则需要执行步骤 5 的修复脚本：
-- sys:role:view (角色管理菜单)
-- sys:role:add (新增按钮)
-- sys:role:edit (编辑按钮)
-- sys:role:delete (删除按钮)
-- sys:role:authMenu (菜单授权按钮)
-- sys:role:authUser (用户授权按钮)

-- 步骤 5: 【修复脚本】为 admin 角色添加缺失的角色管理权限
-- 请根据实际情况替换以下占位符：
-- {admin_role_id}: 步骤 3 查询到的 admin 角色 ID
-- {tenant_id}: 步骤 1 查询到的租户 ID
-- {role_menu_id}: 步骤 2 查询到的角色管理菜单 ID (path='role' 的记录)
-- {role_add_button_id}: 步骤 2 查询到的新增按钮 ID (perm_key='sys:role:add')
-- {role_edit_button_id}: 步骤 2 查询到的编辑按钮 ID (perm_key='sys:role:edit')
-- {role_delete_button_id}: 步骤 2 查询到的删除按钮 ID (perm_key='sys:role:delete')
-- {role_auth_menu_button_id}: 步骤 2 查询到的菜单授权按钮 ID (perm_key='sys:role:authMenu')
-- {role_auth_user_button_id}: 步骤 2 查询到的用户授权按钮 ID (perm_key='sys:role:authUser')

-- 5.1 添加角色管理菜单权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_menu_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_menu_id} 
      AND tenant_id = {tenant_id}
);

-- 5.2 添加新增角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_add_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_add_button_id} 
      AND tenant_id = {tenant_id}
);

-- 5.3 添加编辑角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_edit_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_edit_button_id} 
      AND tenant_id = {tenant_id}
);

-- 5.4 添加删除角色按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_delete_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_delete_button_id} 
      AND tenant_id = {tenant_id}
);

-- 5.5 添加菜单授权按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_auth_menu_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_auth_menu_button_id} 
      AND tenant_id = {tenant_id}
);

-- 5.6 添加用户授权按钮权限
INSERT INTO sys_role_menu_perm (role_id, menu_id, tenant_id, created_at, updated_at)
SELECT {admin_role_id}, {role_auth_user_button_id}, {tenant_id}, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM sys_role_menu_perm 
    WHERE role_id = {admin_role_id} 
      AND menu_id = {role_auth_user_button_id} 
      AND tenant_id = {tenant_id}
);

-- 步骤 6: 验证修复结果
-- 执行完修复脚本后，重新执行步骤 4 的查询，确认所有权限都已添加

-- 步骤 7: 【重要】清除 Redis 缓存并重新登录
-- 权限数据缓存在 Sa-Token Session 中，需要：
-- 1. 清除 Redis 中该用户的 Session 缓存
-- 2. 重新登录系统
-- 3. 刷新页面（Ctrl+F5 强制刷新）

-- 查看 Redis 缓存键（需要在 Redis 客户端执行）：
-- KEYS *{admin_user_id}*

-- =============================================
-- 快速诊断脚本（自动输出需要替换的 ID）
-- =============================================

-- 获取所有需要的 ID（执行后复制结果替换到上面的脚本中）
SELECT 
    'admin_role_id = ' || r.id AS admin_role_info,
    r.role_name,
    r.role_key,
    r.tenant_id
FROM sys_role r
WHERE r.role_key = 'admin'
LIMIT 1;

-- 获取角色管理菜单及按钮 ID
SELECT 
    'tenant_id = ' || tenant_id AS tenant_info
FROM sys_menu_perm
WHERE perm_key LIKE 'sys:role:%'
LIMIT 1;

SELECT 
    m.id,
    m.name,
    m.perm_key,
    m.type,
    'menu_id = ' || m.id AS id_info
FROM sys_menu_perm m
WHERE m.perm_key IN (
    'sys:role:view',
    'sys:role:add',
    'sys:role:edit',
    'sys:role:delete',
    'sys:role:authMenu',
    'sys:role:authUser'
)
ORDER BY m.parent_id, m.order_num;
