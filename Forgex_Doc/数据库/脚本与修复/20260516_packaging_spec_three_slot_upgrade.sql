-- 包装规格主数据与物料三槽关联升级脚本
-- 适用库：forgex_admin / forgex_common
-- 说明：脚本可重复执行；用于升级 basic_packaging_type、补齐三槽关联表、包装规格类型字典和公共表格配置。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260516_packaging_spec_three_slot_upgrade';
SET @now := NOW();

CREATE TABLE IF NOT EXISTS `basic_packaging_type` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `packaging_code` varchar(50) NOT NULL COMMENT '包装规格编码',
  `packaging_name` varchar(100) NOT NULL COMMENT '包装规格名称',
  `packaging_spec_type` varchar(50) DEFAULT NULL COMMENT '包装规格类型：BOX=箱，BUCKET=桶，ROLL=卷，CASE=盒，BAG=袋',
  `length_value` decimal(18,4) DEFAULT NULL COMMENT '长度',
  `width_value` decimal(18,4) DEFAULT NULL COMMENT '宽度',
  `height_value` decimal(18,4) DEFAULT NULL COMMENT '高度',
  `size_unit_id` bigint DEFAULT NULL COMMENT '尺寸单位 ID',
  `volume_value` decimal(18,4) DEFAULT NULL COMMENT '包装容积',
  `volume_unit_id` bigint DEFAULT NULL COMMENT '容积单位 ID',
  `weight_value` decimal(18,4) DEFAULT NULL COMMENT '重量',
  `weight_unit_id` bigint DEFAULT NULL COMMENT '重量单位 ID',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_packaging_spec_code_tenant` (`tenant_id`,`packaging_code`,`deleted`),
  KEY `idx_packaging_spec_type` (`tenant_id`,`packaging_spec_type`,`status`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='包装规格主数据表';

SET @has_packaging_spec_type := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'packaging_spec_type'
);
SET @ddl := IF(@has_packaging_spec_type = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `packaging_spec_type` varchar(50) DEFAULT NULL COMMENT ''包装规格类型'' AFTER `packaging_name`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

ALTER TABLE `basic_packaging_type`
  MODIFY COLUMN `packaging_spec_type` varchar(50) DEFAULT NULL COMMENT '包装规格类型：BOX=箱，BUCKET=桶，ROLL=卷，CASE=盒，BAG=袋';

SET @has_length_value := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'length_value'
);
SET @ddl := IF(@has_length_value = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `length_value` decimal(18,4) DEFAULT NULL COMMENT ''长度'' AFTER `packaging_spec_type`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_width_value := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'width_value'
);
SET @ddl := IF(@has_width_value = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `width_value` decimal(18,4) DEFAULT NULL COMMENT ''宽度'' AFTER `length_value`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_height_value := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'height_value'
);
SET @ddl := IF(@has_height_value = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `height_value` decimal(18,4) DEFAULT NULL COMMENT ''高度'' AFTER `width_value`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_size_unit_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'size_unit_id'
);
SET @ddl := IF(@has_size_unit_id = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `size_unit_id` bigint DEFAULT NULL COMMENT ''尺寸单位 ID'' AFTER `height_value`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_volume_value := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'volume_value'
);
SET @ddl := IF(@has_volume_value = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `volume_value` decimal(18,4) DEFAULT NULL COMMENT ''包装容积'' AFTER `size_unit_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_volume_unit_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'volume_unit_id'
);
SET @ddl := IF(@has_volume_unit_id = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `volume_unit_id` bigint DEFAULT NULL COMMENT ''容积单位 ID'' AFTER `volume_value`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_weight_value := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'weight_value'
);
SET @ddl := IF(@has_weight_value = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `weight_value` decimal(18,4) DEFAULT NULL COMMENT ''重量'' AFTER `volume_unit_id`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_weight_unit_id := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_packaging_type' AND COLUMN_NAME = 'weight_unit_id'
);
SET @ddl := IF(@has_weight_unit_id = 0,
  'ALTER TABLE `basic_packaging_type` ADD COLUMN `weight_unit_id` bigint DEFAULT NULL COMMENT ''重量单位 ID'' AFTER `weight_value`',
  'SELECT 1'
);
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

UPDATE `basic_packaging_type`
SET
  `packaging_spec_type` = CASE
    WHEN `packaging_spec_type` IN ('BOX', 'BUCKET', 'ROLL', 'CASE', 'BAG') THEN `packaging_spec_type`
    WHEN `packaging_material` IN ('wooden_box', 'carton') THEN 'BOX'
    WHEN `packaging_material` IN ('iron_drum') THEN 'BUCKET'
    WHEN `packaging_material` IN ('plastic_bag') THEN 'BAG'
    ELSE 'BOX'
  END,
  `length_value` = COALESCE(`length_value`, `length_mm`),
  `width_value` = COALESCE(`width_value`, `width_mm`),
  `height_value` = COALESCE(`height_value`, `height_mm`),
  `volume_value` = COALESCE(`volume_value`, `max_load_kg`),
  `weight_value` = COALESCE(`weight_value`, `weight_kg`)
WHERE `deleted` = 0;

CREATE TABLE IF NOT EXISTS `basic_material_packaging_relation` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `material_id` bigint NOT NULL COMMENT '物料 ID',
  `packaging_type_id` bigint NOT NULL COMMENT '包装规格 ID',
  `packaging_slot` varchar(20) NOT NULL COMMENT '包装槽位：SMALL=小包装，MEDIUM=中包装，LARGE=大包装',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_packaging_slot` (`tenant_id`,`material_id`,`packaging_slot`,`deleted`),
  KEY `idx_material_packaging_material` (`tenant_id`,`material_id`,`deleted`),
  KEY `idx_material_packaging_type` (`tenant_id`,`packaging_type_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料包装规格三槽关联表';

SET @basic_module_id := COALESCE(
  (SELECT `id` FROM `sys_module` WHERE `deleted` = 0 AND `code` = 'basic' ORDER BY `id` LIMIT 1),
  (SELECT `module_id` FROM `sys_menu` WHERE `deleted` = 0 AND `component_key` = 'BasicDashboard' ORDER BY `id` LIMIT 1),
  5
);
SET @public_tenant_id := COALESCE(
  (SELECT `tenant_id` FROM `sys_module` WHERE `id` = @basic_module_id LIMIT 1),
  1
);
SET @admin_role_id := (
  SELECT `id`
  FROM `sys_role`
  WHERE `deleted` = 0
    AND `tenant_id` = @public_tenant_id
    AND `role_key` = 'admin'
  ORDER BY `id`
  LIMIT 1
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, 0, 'menu', 'packaging', '包装方式',
       JSON_OBJECT('zh-CN','包装方式','zh-TW','包裝方式','en-US','Packaging','ja-JP','包装方式','ko-KR','포장 방식'),
       'InboxOutlined', 'BasicPackaging', 'basic:packaging:query', 56, 1, 1, @now, @script_user, @now, @script_user, 0, 1, 'embedded', NULL
FROM DUAL
WHERE @basic_module_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `deleted` = 0 AND `component_key` = 'BasicPackaging'
  );

UPDATE `sys_menu`
SET
  `tenant_id` = @public_tenant_id,
  `tenant_type` = 'PUBLIC',
  `module_id` = @basic_module_id,
  `parent_id` = 0,
  `type` = 'menu',
  `path` = 'packaging',
  `name` = '包装方式',
  `name_i18n_json` = JSON_OBJECT('zh-CN','包装方式','zh-TW','包裝方式','en-US','Packaging','ja-JP','包装方式','ko-KR','포장 방식'),
  `icon` = 'InboxOutlined',
  `perm_key` = 'basic:packaging:query',
  `order_num` = 56,
  `visible` = 1,
  `status` = 1,
  `menu_level` = 1,
  `menu_mode` = 'embedded',
  `update_time` = @now,
  `update_by` = @script_user
WHERE `deleted` = 0 AND `component_key` = 'BasicPackaging';

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, parent.`id`, 'button', seed.`path`, seed.`name`, seed.`name_i18n_json`, NULL, NULL,
       seed.`perm_key`, seed.`order_num`, 1, 1, @now, @script_user, @now, @script_user, 0, 2, 'embedded', NULL
FROM (
  SELECT 'query' AS `path`, '包装方式查询' AS `name`, JSON_OBJECT('zh-CN','查询','zh-TW','查詢','en-US','Query','ja-JP','検索','ko-KR','조회') AS `name_i18n_json`, 'basic:packaging:query' AS `perm_key`, 1 AS `order_num`
  UNION ALL SELECT 'add', '包装方式新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'basic:packaging:add', 2
  UNION ALL SELECT 'edit', '包装方式编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'basic:packaging:edit', 3
  UNION ALL SELECT 'delete', '包装方式删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'basic:packaging:delete', 4
) seed
JOIN `sys_menu` parent ON parent.`deleted` = 0 AND parent.`component_key` = 'BasicPackaging'
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_menu` existing
  WHERE existing.`deleted` = 0
    AND existing.`parent_id` = parent.`id`
    AND existing.`perm_key` = seed.`perm_key`
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.`permission_name`, seed.`permission_key`, seed.`url`, 'POST', 0, @now, @now, 0
FROM (
  SELECT '包装方式查询' AS `permission_name`, 'basic:packaging:query' AS `permission_key`, '/basic/packaging/page' AS `url`
  UNION ALL SELECT '包装方式新增', 'basic:packaging:add', '/basic/packaging/create'
  UNION ALL SELECT '包装方式编辑', 'basic:packaging:edit', '/basic/packaging/update'
  UNION ALL SELECT '包装方式删除', 'basic:packaging:delete', '/basic/packaging/delete'
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM `sys_permission` existing
  WHERE existing.`deleted` = 0
    AND existing.`permission_key` = seed.`permission_key`
);

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @public_tenant_id, @admin_role_id, m.`id`
FROM `sys_menu` m
WHERE @admin_role_id IS NOT NULL
  AND m.`deleted` = 0
  AND (m.`component_key` = 'BasicPackaging' OR m.`perm_key` LIKE 'basic:packaging:%')
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.`tenant_id` = @public_tenant_id
      AND rm.`role_id` = @admin_role_id
      AND rm.`menu_id` = m.`id`
  );

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.`id`
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.`deleted` = 0
  AND p.`permission_key` LIKE 'basic:packaging:%'
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_permission` rp
    WHERE rp.`role_id` = @admin_role_id
      AND rp.`permission_id` = p.`id`
  );

SET @dict_root_id := 5000000000000000600;

INSERT INTO `sys_dict`
(`id`,`parent_id`,`dict_name`,`dict_code`,`module_id`,`dict_value`,`dict_value_i18n_json`,`node_path`,`level`,`children_count`,`order_num`,`status`,`remark`,`tenant_id`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`,`tag_style_json`)
SELECT @dict_root_id, 0, '包装规格类型', 'packaging_spec_type', NULL, NULL,
       JSON_OBJECT('zh-CN','包装规格类型','en-US','Packaging Spec Type','zh-TW','包裝規格類型','ja-JP','包装仕様タイプ','ko-KR','포장 규격 유형'),
       'packaging_spec_type', 1, 5, 360, 1, '包装规格主数据类型字典', 0, 0, NOW(), 0, NOW(), 0, NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` WHERE `tenant_id` = 0 AND `dict_code` = 'packaging_spec_type' AND `parent_id` = 0 AND `deleted` = 0
);

SET @dict_root_id := (
  SELECT `id` FROM `sys_dict`
  WHERE `tenant_id` = 0 AND `dict_code` = 'packaging_spec_type' AND `parent_id` = 0 AND `deleted` = 0
  ORDER BY `id` LIMIT 1
);

INSERT INTO `sys_dict`
(`id`,`parent_id`,`dict_name`,`dict_code`,`module_id`,`dict_value`,`dict_value_i18n_json`,`node_path`,`level`,`children_count`,`order_num`,`status`,`remark`,`tenant_id`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`,`tag_style_json`)
SELECT seed.id, @dict_root_id, seed.dict_name, seed.dict_code, NULL, seed.dict_value,
       JSON_OBJECT('zh-CN', seed.zh_cn, 'en-US', seed.en_us, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
       CONCAT('packaging_spec_type/', LOWER(seed.dict_value)), 2, 0, seed.order_num, 1, '包装规格类型', 0, 0, NOW(), 0, NOW(), 0,
       JSON_OBJECT('color', seed.color)
FROM (
  SELECT 5000000000000000601 id, '箱' dict_name, 'packaging_spec_type_box' dict_code, 'BOX' dict_value, '箱' zh_cn, 'Box' en_us, '箱' zh_tw, '箱' ja_jp, '상자' ko_kr, 1 order_num, 'blue' color
  UNION ALL SELECT 5000000000000000602, '桶', 'packaging_spec_type_bucket', 'BUCKET', '桶', 'Bucket', '桶', '桶', '통', 2, 'green'
  UNION ALL SELECT 5000000000000000603, '卷', 'packaging_spec_type_roll', 'ROLL', '卷', 'Roll', '卷', '巻', '롤', 3, 'purple'
  UNION ALL SELECT 5000000000000000604, '盒', 'packaging_spec_type_case', 'CASE', '盒', 'Case', '盒', '盒', '케이스', 4, 'gold'
  UNION ALL SELECT 5000000000000000605, '袋', 'packaging_spec_type_bag', 'BAG', '袋', 'Bag', '袋', '袋', '봉투', 5, 'cyan'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` t
  WHERE t.`tenant_id` = 0 AND t.`parent_id` = @dict_root_id AND t.`dict_value` = seed.dict_value AND t.`deleted` = 0
);

USE `forgex_common`;

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'BasicPackagingTypeTable',
       JSON_OBJECT('zh-CN','包装规格管理','en-US','Packaging Spec Management','zh-TW','包裝規格管理','ja-JP','包装仕様管理','ko-KR','포장 규격 관리'),
       'BUSINESS', 'id', 10, 1, 1, @script_user, NOW(), @script_user, NOW(), 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'BasicPackagingTypeTable' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, seed.table_code, seed.field,
       JSON_OBJECT('zh-CN', seed.zh_cn, 'en-US', seed.en_us, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
       seed.align, seed.width, seed.fixed, seed.ellipsis, seed.sortable, seed.sorter_field, seed.queryable, seed.query_type, seed.query_operator, seed.dict_code, seed.render_type, NULL, seed.order_num, 1, @script_user, NOW(), @script_user, NOW(), 0
FROM (
  SELECT 'BasicPackagingTypeTable' table_code, 'packagingCode' field, '包装规格编码' zh_cn, 'Packaging Spec Code' en_us, '包裝規格代碼' zh_tw, '包装仕様コード' ja_jp, '포장 규격 코드' ko_kr, 'left' align, 150 width, NULL fixed, 0 ellipsis, 1 sortable, 'packagingCode' sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, NULL render_type, 1 order_num
  UNION ALL SELECT 'BasicPackagingTypeTable','packagingName','包装规格名称','Packaging Spec Name','包裝規格名稱','包装仕様名','포장 규격명','left',180,NULL,1,1,'packagingName',1,'input','like',NULL,NULL,2
  UNION ALL SELECT 'BasicPackagingTypeTable','packagingSpecType','包装规格类型','Packaging Spec Type','包裝規格類型','包装仕様タイプ','포장 규격 유형','center',130,NULL,0,1,'packagingSpecType',1,'select','eq','packaging_spec_type','tag',3
  UNION ALL SELECT 'BasicPackagingTypeTable','size','包装尺寸','Packaging Size','包裝尺寸','包装寸法','포장 치수','left',180,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicPackagingTypeTable','volume','包装容积','Packaging Volume','包裝容積','包装容積','포장 부피','left',140,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,5
  UNION ALL SELECT 'BasicPackagingTypeTable','weight','重量','Weight','重量','重量','무게','left',120,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,6
  UNION ALL SELECT 'BasicPackagingTypeTable','status','状态','Status','狀態','状態','상태','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',7
  UNION ALL SELECT 'BasicPackagingTypeTable','sortOrder','排序','Sort','排序','並び順','정렬','center',90,NULL,0,1,'sortOrder',0,NULL,NULL,NULL,NULL,8
  UNION ALL SELECT 'BasicPackagingTypeTable','remark','备注','Remark','備註','備考','비고','left',180,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,9
  UNION ALL SELECT 'BasicPackagingTypeTable','action','操作','Action','操作','操作','작업','center',240,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config` t
  WHERE t.`tenant_id` = 0 AND t.`table_code` = seed.table_code AND t.`field` = seed.field AND t.`deleted` = 0
);

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like', `sorter_field` = `field`, `update_by` = @script_user, `update_time` = NOW()
WHERE `tenant_id` = 0 AND `table_code` = 'BasicPackagingTypeTable' AND `field` IN ('packagingCode','packagingName') AND `deleted` = 0;

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'packaging_spec_type', `render_type` = 'tag', `sorter_field` = 'packagingSpecType', `update_by` = @script_user, `update_time` = NOW()
WHERE `tenant_id` = 0 AND `table_code` = 'BasicPackagingTypeTable' AND `field` = 'packagingSpecType' AND `deleted` = 0;

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'common_status', `render_type` = 'tag', `sorter_field` = 'status', `update_by` = @script_user, `update_time` = NOW()
WHERE `tenant_id` = 0 AND `table_code` = 'BasicPackagingTypeTable' AND `field` = 'status' AND `deleted` = 0;

UPDATE `fx_table_column_config`
SET `enabled` = 0, `queryable` = 0, `update_by` = @script_user, `update_time` = NOW()
WHERE `tenant_id` = 0
  AND `table_code` = 'BasicPackagingTypeTable'
  AND `field` IN ('packagingMaterial', 'lengthMm', 'widthMm', 'heightMm', 'weightKg', 'maxLoadKg', 'unitCost')
  AND `deleted` = 0;
