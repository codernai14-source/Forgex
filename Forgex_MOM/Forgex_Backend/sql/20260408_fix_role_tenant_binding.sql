-- =============================================
-- 修复角色租户绑定问题
-- 功能：将所有角色绑定到主租户
-- 作者：Forgex
-- 日期：2026-04-08
-- =============================================

-- 1. 查看当前所有租户信息
SELECT '===== 当前租户列表 =====' AS info;
SELECT 
    id AS tenant_id,
    tenant_name,
    tenant_code,
    status,
    deleted
FROM sys_tenant
ORDER BY id;

-- 2. 查看主租户信息（ID 最小的非删除租户）
SELECT '===== 主租户信息 =====' AS info;
SELECT 
    id AS main_tenant_id,
    tenant_name,
    tenant_code
FROM sys_tenant
WHERE deleted = 0
ORDER BY id
LIMIT 1;

-- 3. 查看当前角色的租户绑定情况
SELECT '===== 角色当前租户绑定情况 =====' AS info;
SELECT 
    r.id AS role_id,
    r.role_name,
    r.role_key,
    r.tenant_id AS current_tenant_id,
    CASE 
        WHEN r.tenant_id IS NULL THEN 'NULL - 需要修复'
        WHEN r.tenant_id = 0 THEN '公共角色'
        WHEN r.tenant_id = (SELECT id FROM sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1) THEN '已绑定主租户'
        ELSE '绑定其他租户'
    END AS binding_status,
    r.deleted
FROM sys_role r
ORDER BY r.id;

-- 4. 修复：将所有角色绑定到主租户
-- 注意：执行前请先确认主租户 ID，这里假设主租户 ID 为 1
SELECT '===== 开始修复角色租户绑定 =====' AS info;

-- 更新所有角色的 tenant_id 为主租户 ID（假设主租户 ID=1）
-- 如果您的主租户 ID 不是 1，请修改下面的 1 为实际的主租户 ID
UPDATE sys_role 
SET tenant_id = 1  -- 修改这里的主租户 ID
WHERE deleted = 0 
  AND (tenant_id IS NULL OR tenant_id != 1);

-- 5. 验证修复结果
SELECT '===== 修复后的角色租户绑定情况 =====' AS info;
SELECT 
    r.id AS role_id,
    r.role_name,
    r.role_key,
    r.tenant_id AS current_tenant_id,
    t.tenant_name,
    '已修复' AS status
FROM sys_role r
LEFT JOIN sys_tenant t ON r.tenant_id = t.id
WHERE r.deleted = 0
ORDER BY r.id;

-- 6. 统计信息
SELECT '===== 修复统计 =====' AS info;
SELECT 
    COUNT(*) AS total_roles,
    COUNT(CASE WHEN tenant_id = 1 THEN 1 END) AS roles_bound_to_main_tenant,
    COUNT(CASE WHEN tenant_id != 1 AND tenant_id IS NOT NULL THEN 1 END) AS roles_bound_to_other_tenant,
    COUNT(CASE WHEN tenant_id IS NULL THEN 1 END) AS roles_with_null_tenant
FROM sys_role
WHERE deleted = 0;

-- =============================================
-- 回滚脚本（如果需要）
-- =============================================
-- ROLLBACK;
-- 或者恢复为公共角色：
-- UPDATE sys_role SET tenant_id = 0 WHERE deleted = 0;
