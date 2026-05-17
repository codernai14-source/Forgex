-- Forgex 编码规则菜单与包装规格表结构修复脚本
-- 修复内容：
--   1. 编码规则管理改为页面入口，隐藏旧的“规则管理/示例管理/历史记录”等子菜单。
--   2. 补齐 basic_packaging_type.packaging_spec_type 字段，解决物料管理打开时包装规格接口报数据访问异常。

SET NAMES utf8mb4;

USE `forgex_admin`;

START TRANSACTION;

-- 编码规则管理本身保留为页面；旧子菜单不再作为目录子项展示。
UPDATE `sys_menu`
SET `visible` = 0,
    `status` = 0,
    `deleted` = 1,
    `update_by` = '20260517_fix_encode_rule_menu',
    `update_time` = NOW()
WHERE `parent_id` = 210
  AND `type` = 'menu'
  AND `id` IN (216, 218, 220);

-- 保留编码规则管理下的按钮权限，确保新增/编辑/删除/查看/测试/生成编码仍可授权。
UPDATE `sys_menu`
SET `deleted` = 0,
    `visible` = 1,
    `status` = 1,
    `update_by` = '20260517_fix_encode_rule_menu',
    `update_time` = NOW()
WHERE `parent_id` = 210
  AND `type` = 'button'
  AND `id` IN (217, 219, 221, 222, 223, 224);

COMMIT;

-- 包装规格类型字段：后端 BasicPackagingType 已映射该字段，当前库缺字段会导致 SELECT 报错。
SET @column_exists := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = 'forgex_admin'
      AND table_name = 'basic_packaging_type'
      AND column_name = 'packaging_spec_type'
);

SET @sql := IF(
    @column_exists = 0,
    'ALTER TABLE `forgex_admin`.`basic_packaging_type` ADD COLUMN `packaging_spec_type` varchar(50) NULL DEFAULT ''STANDARD'' COMMENT ''包装规格类型'' AFTER `packaging_name`',
    'SELECT ''basic_packaging_type.packaging_spec_type already exists'' AS message'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `basic_packaging_type`
SET `packaging_spec_type` = 'STANDARD'
WHERE `packaging_spec_type` IS NULL
   OR `packaging_spec_type` = '';

SELECT id, name, type, path, component_key, parent_id, visible, status, deleted
FROM `sys_menu`
WHERE id IN (210, 216, 217, 218, 219, 220, 221, 222, 223, 224)
ORDER BY parent_id, order_num, id;

SHOW COLUMNS FROM `basic_packaging_type` LIKE 'packaging_spec_type';
