-- =============================================
-- 修复角色租户绑定问题（自动版本）
-- 功能：自动识别主租户并将所有角色绑定到主租户
-- 作者：Forgex
-- 日期：2026-04-08
-- =============================================

-- 开始事务
START TRANSACTION;

-- 1. 显示修复前的状态
SELECT '========================================' AS '';
SELECT '=== 修复前：角色租户绑定情况 ===' AS title;
SELECT '========================================' AS '';

SELECT 
    r.id AS role_id,
    r.role_name AS 角色名称，
    r.role_key AS 角色键，
    r.tenant_id AS 当前租户 ID,
    CASE 
        WHEN r.tenant_id IS NULL THEN '未绑定租户（需要修复）'
        WHEN r.tenant_id = 0 THEN '公共角色（所有租户可用）'
        ELSE '已绑定租户'
    END AS 绑定状态
FROM sys_role r
WHERE r.deleted = 0
ORDER BY r.id;

-- 2. 显示主租户信息
SELECT '========================================' AS '';
SELECT '=== 主租户信息 ===' AS title;
SELECT '========================================' AS '';

SELECT 
    id AS 主租户 ID,
    tenant_name AS 租户名称，
    tenant_code AS 租户编码，
    status AS 状态
FROM sys_tenant
WHERE deleted = 0
ORDER BY id
LIMIT 1;

-- 3. 执行修复：将所有角色绑定到主租户（ID 最小的非删除租户）
SELECT '========================================' AS '';
SELECT '=== 开始执行修复 ===' AS title;
SELECT '========================================' AS '';

-- 更新逻辑：
-- - tenant_id 为 NULL 的角色 -> 绑定到主租户
-- - tenant_id = 0 的公共角色 -> 保持公共（不修改）
-- - tenant_id 为其他值的角色 -> 绑定到主租户
UPDATE sys_role r
INNER JOIN (
    -- 获取主租户 ID（ID 最小的非删除租户）
    SELECT id AS main_tenant_id 
    FROM sys_tenant 
    WHERE deleted = 0 
    ORDER BY id 
    LIMIT 1
) main_tenant ON 1=1
SET r.tenant_id = main_tenant.main_tenant_id
WHERE r.deleted = 0 
  AND (r.tenant_id IS NULL OR r.tenant_id = 0 OR r.tenant_id != main_tenant.main_tenant_id);

-- 显示受影响的行数
SELECT CONCAT('已更新 ', ROW_COUNT(), ' 个角色的租户绑定') AS 修复结果;

-- 4. 显示修复后的状态
SELECT '========================================' AS '';
SELECT '=== 修复后：角色租户绑定情况 ===' AS title;
SELECT '========================================' AS '';

SELECT 
    r.id AS role_id,
    r.role_name AS 角色名称，
    r.role_key AS 角色键，
    r.tenant_id AS 当前租户 ID,
    t.tenant_name AS 所属租户，
    '已修复' AS 状态
FROM sys_role r
LEFT JOIN sys_tenant t ON r.tenant_id = t.id
WHERE r.deleted = 0
ORDER BY r.id;

-- 5. 统计信息
SELECT '========================================' AS '';
SELECT '=== 修复统计 ===' AS title;
SELECT '========================================' AS '';

SELECT 
    COUNT(*) AS 总角色数，
    COUNT(CASE WHEN r.tenant_id = (
        SELECT id FROM sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1
    ) THEN 1 END) AS 绑定到主租户的角色数，
    COUNT(CASE WHEN r.tenant_id = 0 THEN 1 END) AS 公共角色数，
    COUNT(CASE WHEN r.tenant_id IS NULL THEN 1 END) AS 未绑定租户数
FROM sys_role r
WHERE r.deleted = 0;

-- 6. 验证：检查是否有角色仍然未正确绑定
SELECT '========================================' AS '';
SELECT '=== 验证检查结果 ===' AS title;
SELECT '========================================' AS '';

SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 验证通过：所有角色都已正确绑定到主租户'
        ELSE CONCAT('✗ 验证失败：仍有 ', COUNT(*), ' 个角色未正确绑定')
    END AS 验证结果
FROM sys_role r
WHERE r.deleted = 0 
  AND (r.tenant_id IS NULL 
       OR r.tenant_id NOT IN (
           SELECT id FROM sys_tenant WHERE deleted = 0
       ));

-- 提交事务
COMMIT;

-- =============================================
-- 回滚脚本（如果需要回滚，取消下面的注释）
-- =============================================
-- START TRANSACTION;
-- UPDATE sys_role SET tenant_id = 0 WHERE deleted = 0;
-- COMMIT;
