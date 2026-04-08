-- =============================================
-- 检查角色租户绑定状态
-- 功能：查看所有角色的租户绑定情况
-- 作者：Forgex
-- 日期：2026-04-08
-- =============================================

-- 1. 查看所有租户
SELECT '===== 租户列表 =====' AS info;
SELECT 
    id AS tenant_id,
    tenant_name AS 租户名称，
    tenant_code AS 租户编码，
    CASE status 
        WHEN 1 THEN '正常'
        WHEN 0 THEN '禁用'
        ELSE status 
    END AS 状态
FROM sys_tenant
WHERE deleted = 0
ORDER BY id;

-- 2. 查看所有角色及其租户绑定
SELECT '===== 角色租户绑定情况 =====' AS info;
SELECT 
    r.id AS 角色 ID,
    r.role_name AS 角色名称，
    r.role_key AS 角色键，
    r.description AS 描述，
    r.tenant_id AS 绑定租户 ID,
    CASE 
        WHEN r.tenant_id IS NULL THEN '❌ 未绑定租户（异常）'
        WHEN r.tenant_id = 0 THEN '⚠️ 公共角色（所有租户可用）'
        WHEN r.tenant_id = (SELECT id FROM sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1) THEN '✅ 已绑定主租户'
        ELSE '⚠️ 绑定其他租户'
    END AS 绑定状态，
    t.tenant_name AS 所属租户名称
FROM sys_role r
LEFT JOIN sys_tenant t ON r.tenant_id = t.id
WHERE r.deleted = 0
ORDER BY r.id;

-- 3. 查看角色 - 用户关联情况
SELECT '===== 角色 - 用户关联情况 =====' AS info;
SELECT 
    r.id AS 角色 ID,
    r.role_name AS 角色名称，
    r.tenant_id AS 角色租户 ID,
    COUNT(ur.id) AS 关联用户数，
    GROUP_CONCAT(DISTINCT u.username SEPARATOR ', ') AS 关联用户
FROM sys_role r
LEFT JOIN sys_user_role ur ON r.id = ur.role_id AND ur.deleted = 0
LEFT JOIN sys_user u ON ur.user_id = u.id AND u.deleted = 0
WHERE r.deleted = 0
GROUP BY r.id, r.role_name, r.tenant_id
ORDER BY r.id;

-- 4. 查看角色 - 菜单权限关联情况
SELECT '===== 角色 - 菜单权限关联情况 =====' AS info;
SELECT 
    r.id AS 角色 ID,
    r.role_name AS 角色名称，
    r.tenant_id AS 角色租户 ID,
    COUNT(rm.id) AS 关联菜单数
FROM sys_role r
LEFT JOIN sys_role_menu rm ON r.id = rm.role_id AND rm.deleted = 0
WHERE r.deleted = 0
GROUP BY r.id, r.role_name, r.tenant_id
ORDER BY r.id;

-- 5. 异常检测
SELECT '===== 异常情况检测 =====' AS info;

-- 5.1 检测未绑定租户的角色
SELECT '未绑定租户的角色（tenant_id 为 NULL）:' AS 异常类型;
SELECT id, role_name, role_key 
FROM sys_role 
WHERE deleted = 0 AND tenant_id IS NULL;

-- 5.2 检测绑定到不存在租户的角色
SELECT '绑定到不存在租户的角色:' AS 异常类型;
SELECT r.id, r.role_name, r.tenant_id 
FROM sys_role r
LEFT JOIN sys_tenant t ON r.tenant_id = t.id
WHERE r.deleted = 0 
  AND r.tenant_id IS NOT NULL 
  AND r.tenant_id > 0 
  AND t.id IS NULL;

-- 6. 统计摘要
SELECT '===== 统计摘要 =====' AS info;
SELECT 
    (SELECT COUNT(*) FROM sys_tenant WHERE deleted = 0) AS 活跃租户数，
    (SELECT COUNT(*) FROM sys_role WHERE deleted = 0) AS 总角色数，
    (SELECT COUNT(*) FROM sys_role WHERE deleted = 0 AND tenant_id IS NULL) AS 未绑定租户角色数，
    (SELECT COUNT(*) FROM sys_role WHERE deleted = 0 AND tenant_id = 0) AS 公共角色数，
    (SELECT COUNT(*) FROM sys_role WHERE deleted = 0 AND tenant_id = (SELECT id FROM sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1)) AS 绑定主租户角色数;
