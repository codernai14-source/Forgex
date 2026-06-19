-- 工厂建模升级脚本：产线 / 工段 / 工序 + 车间扩展字段 + 字典 + 菜单权限 + 表格列配置。
-- 脚本可重复执行，目标数据库：forgex_admin、forgex_common。
-- 兼容 MySQL 5.7 / 8.0。
-- 请使用 UTF-8 / utf8mb4 客户端执行，避免中文注释乱码。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260619_factory_modeling';
SET @now := NOW();

-- ----------------------------------------------------------------
-- 一、建表：basic_production_line
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `basic_production_line` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `production_line_code` varchar(50) NOT NULL COMMENT '产线编码',
  `production_line_name` varchar(100) NOT NULL COMMENT '产线名称',
  `workshop_id` bigint DEFAULT NULL COMMENT '所属车间ID',
  `workshop_code` varchar(50) DEFAULT NULL COMMENT '所属车间编码（快照）',
  `workshop_name` varchar(100) DEFAULT NULL COMMENT '所属车间名称（快照）',
  `production_line_type` varchar(50) DEFAULT NULL COMMENT '产线类型（字典：prod_line_type）',
  `manager_employee_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `manager_employee_no` varchar(50) DEFAULT NULL COMMENT '负责人工号（快照）',
  `manager_employee_name` varchar(100) DEFAULT NULL COMMENT '负责人姓名（快照）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_production_line_code` (`tenant_id`, `production_line_code`, `deleted`),
  KEY `idx_basic_production_line_workshop` (`tenant_id`, `workshop_id`, `deleted`),
  KEY `idx_basic_production_line_type` (`tenant_id`, `production_line_type`, `deleted`),
  KEY `idx_basic_production_line_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产线主数据表';

-- ----------------------------------------------------------------
-- 二、建表：basic_work_section
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `basic_work_section` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `work_section_code` varchar(50) NOT NULL COMMENT '工段编码',
  `work_section_name` varchar(100) NOT NULL COMMENT '工段名称',
  `workshop_id` bigint DEFAULT NULL COMMENT '所属车间ID',
  `workshop_code` varchar(50) DEFAULT NULL COMMENT '所属车间编码（快照）',
  `workshop_name` varchar(100) DEFAULT NULL COMMENT '所属车间名称（快照）',
  `production_line_id` bigint DEFAULT NULL COMMENT '所属产线ID',
  `production_line_code` varchar(50) DEFAULT NULL COMMENT '所属产线编码（快照）',
  `production_line_name` varchar(100) DEFAULT NULL COMMENT '所属产线名称（快照）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '顺序号',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_work_section_code` (`tenant_id`, `work_section_code`, `deleted`),
  KEY `idx_basic_work_section_workshop` (`tenant_id`, `workshop_id`, `deleted`),
  KEY `idx_basic_work_section_prod_line` (`tenant_id`, `production_line_id`, `deleted`),
  KEY `idx_basic_work_section_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工段主数据表';

-- ----------------------------------------------------------------
-- 三、建表：basic_process
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `basic_process` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `process_code` varchar(50) NOT NULL COMMENT '工序编码',
  `process_name` varchar(100) NOT NULL COMMENT '工序名称',
  `work_section_id` bigint DEFAULT NULL COMMENT '所属工段ID',
  `work_section_code` varchar(50) DEFAULT NULL COMMENT '所属工段编码（快照）',
  `work_section_name` varchar(100) DEFAULT NULL COMMENT '所属工段名称（快照）',
  `production_line_id` bigint DEFAULT NULL COMMENT '所属产线ID（冗余快照）',
  `production_line_code` varchar(50) DEFAULT NULL COMMENT '所属产线编码（冗余快照）',
  `production_line_name` varchar(100) DEFAULT NULL COMMENT '所属产线名称（冗余快照）',
  `workshop_id` bigint DEFAULT NULL COMMENT '所属车间ID（冗余快照）',
  `workshop_code` varchar(50) DEFAULT NULL COMMENT '所属车间编码（冗余快照）',
  `workshop_name` varchar(100) DEFAULT NULL COMMENT '所属车间名称（冗余快照）',
  `process_type` varchar(50) DEFAULT NULL COMMENT '工序类型（字典：process_type）',
  `report_type` varchar(50) DEFAULT NULL COMMENT '报工方式（字典：report_type）',
  `qc_trigger_point` varchar(50) DEFAULT NULL COMMENT '质检触发点（字典：qc_trigger_point）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '顺序号',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_process_code` (`tenant_id`, `process_code`, `deleted`),
  KEY `idx_basic_process_work_section` (`tenant_id`, `work_section_id`, `deleted`),
  KEY `idx_basic_process_prod_line` (`tenant_id`, `production_line_id`, `deleted`),
  KEY `idx_basic_process_workshop` (`tenant_id`, `workshop_id`, `deleted`),
  KEY `idx_basic_process_type` (`tenant_id`, `process_type`, `deleted`),
  KEY `idx_basic_process_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工序主数据表';

-- ----------------------------------------------------------------
-- 四、扩展 basic_workshop：增加车间类型 / 负责人
-- ----------------------------------------------------------------
SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `basic_workshop` ADD COLUMN `workshop_type` varchar(50) DEFAULT NULL COMMENT ''车间类型（字典：workshop_type）'' AFTER `factory_id`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'basic_workshop'
    AND COLUMN_NAME = 'workshop_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `basic_workshop` ADD COLUMN `workshop_manager_id` bigint DEFAULT NULL COMMENT ''负责人ID'' AFTER `workshop_type`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'basic_workshop'
    AND COLUMN_NAME = 'workshop_manager_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `basic_workshop` ADD COLUMN `workshop_manager_name` varchar(100) DEFAULT NULL COMMENT ''负责人姓名（快照）'' AFTER `workshop_manager_id`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'basic_workshop'
    AND COLUMN_NAME = 'workshop_manager_name'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 车间表索引
SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'CREATE INDEX `idx_basic_workshop_type` ON `basic_workshop` (`tenant_id`, `workshop_type`, `deleted`)',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'basic_workshop'
    AND INDEX_NAME = 'idx_basic_workshop_type'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------------------------------------------
-- 五、字典初始化：prod_line_type / workshop_type / process_type / report_type / qc_trigger_point
-- 字典表 sys_dict 支持树形（parent_id / node_path），字典值用 dict_value + i18n_json 表达。
-- ----------------------------------------------------------------
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT v.tenant_id, 0, v.dict_name, v.dict_code, v.module_id, NULL, NULL, v.dict_code, 1, 0, v.order_num, 1, v.remark, @script_user, @now, @script_user, @now, 0, NULL
FROM (
  SELECT 0 tenant_id, '产线类型' dict_name, 'prod_line_type' dict_code, 1 module_id, 1 order_num, '产线类型根字典' remark
  UNION ALL SELECT 0, '车间类型', 'workshop_type', 1, 2, '车间类型根字典'
  UNION ALL SELECT 0, '工序类型', 'process_type', 1, 3, '工序类型根字典'
  UNION ALL SELECT 0, '报工方式', 'report_type', 1, 4, '报工方式根字典'
  UNION ALL SELECT 0, '质检触发点', 'qc_trigger_point', 1, 5, '质检触发点根字典'
) v
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = v.tenant_id AND d.dict_code = v.dict_code AND d.parent_id = 0 AND d.deleted = 0
);

-- 字典项：prod_line_type
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT 0, p.id, item.dict_name, p.dict_code, p.module_id, item.dict_value, item.i18n_json,
       CONCAT(p.dict_code, '/', item.dict_value), 2, 0, item.order_num, 1, item.remark, @script_user, @now, @script_user, @now, 0, item.tag_style
FROM (
  SELECT '装配产线' dict_name, 'ASSEMBLY' dict_value, '{"zh-CN":"装配产线","zh-TW":"裝配線","en-US":"Assembly","ja-JP":"組立ライン","ko-KR":"조립 라인"}' i18n_json, 1 order_num, '装配类产线' remark, NULL tag_style
  UNION ALL SELECT '焊接产线', 'WELDING', '{"zh-CN":"焊接产线","zh-TW":"焊接線","en-US":"Welding","ja-JP":"溶接ライン","ko-KR":"용접 라인"}', 2, '焊接类产线', NULL
  UNION ALL SELECT '机加工产线', 'MACHINING', '{"zh-CN":"机加工产线","zh-TW":"機加工線","en-US":"Machining","ja-JP":"機械加工ライン","ko-KR":"기계 가공 라인"}', 3, '机加工类产线', NULL
  UNION ALL SELECT '喷涂产线', 'PAINTING', '{"zh-CN":"喷涂产线","zh-TW":"塗裝線","en-US":"Painting","ja-JP":"塗装ライン","ko-KR":"도장 라인"}', 4, '喷涂类产线', NULL
  UNION ALL SELECT '包装产线', 'PACKAGING', '{"zh-CN":"包装产线","zh-TW":"包裝線","en-US":"Packaging","ja-JP":"包装ライン","ko-KR":"포장 라인"}', 5, '包装类产线', NULL
) item
JOIN `sys_dict` p ON p.tenant_id = 0 AND p.dict_code = 'prod_line_type' AND p.parent_id = 0 AND p.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = 0 AND d.parent_id = p.id AND d.dict_value = item.dict_value AND d.deleted = 0
);

-- 字典项：workshop_type
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT 0, p.id, item.dict_name, p.dict_code, p.module_id, item.dict_value, item.i18n_json,
       CONCAT(p.dict_code, '/', item.dict_value), 2, 0, item.order_num, 1, item.remark, @script_user, @now, @script_user, @now, 0, NULL
FROM (
  SELECT '原材料车间' dict_name, 'RAW' dict_value, '{"zh-CN":"原材料车间","zh-TW":"原材料車間","en-US":"Raw Workshop","ja-JP":"原材料職場","ko-KR":"원재료 작업장"}' i18n_json, 1 order_num, '原材料加工车间' remark
  UNION ALL SELECT '加工车间', 'PROCESSING', '{"zh-CN":"加工车间","zh-TW":"加工車間","en-US":"Processing","ja-JP":"加工職場","ko-KR":"가공 작업장"}', 2, '加工车间'
  UNION ALL SELECT '装配车间', 'ASSEMBLY', '{"zh-CN":"装配车间","zh-TW":"裝配車間","en-US":"Assembly","ja-JP":"組立職場","ko-KR":"조립 작업장"}', 3, '装配车间'
  UNION ALL SELECT '包装车间', 'PACKAGING', '{"zh-CN":"包装车间","zh-TW":"包裝車間","en-US":"Packaging","ja-JP":"包装職場","ko-KR":"포장 작업장"}', 4, '包装车间'
  UNION ALL SELECT '辅助车间', 'AUXILIARY', '{"zh-CN":"辅助车间","zh-TW":"輔助車間","en-US":"Auxiliary","ja-JP":"補助職場","ko-KR":"보조 작업장"}', 5, '辅助车间'
) item
JOIN `sys_dict` p ON p.tenant_id = 0 AND p.dict_code = 'workshop_type' AND p.parent_id = 0 AND p.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = 0 AND d.parent_id = p.id AND d.dict_value = item.dict_value AND d.deleted = 0
);

-- 字典项：process_type
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT 0, p.id, item.dict_name, p.dict_code, p.module_id, item.dict_value, item.i18n_json,
       CONCAT(p.dict_code, '/', item.dict_value), 2, 0, item.order_num, 1, item.remark, @script_user, @now, @script_user, @now, 0, NULL
FROM (
  SELECT '准备工序' dict_name, 'PREPARATION' dict_value, '{"zh-CN":"准备工序","zh-TW":"準備工序","en-US":"Preparation","ja-JP":"準備工程","ko-KR":"준비 공정"}' i18n_json, 1 order_num, '开工前的准备工序' remark
  UNION ALL SELECT '加工工序', 'PROCESSING', '{"zh-CN":"加工工序","zh-TW":"加工工序","en-US":"Processing","ja-JP":"加工工程","ko-KR":"가공 공정"}', 2, '加工工序'
  UNION ALL SELECT '装配工序', 'ASSEMBLY', '{"zh-CN":"装配工序","zh-TW":"裝配工序","en-US":"Assembly","ja-JP":"組立工程","ko-KR":"조립 공정"}', 3, '装配工序'
  UNION ALL SELECT '检验工序', 'INSPECTION', '{"zh-CN":"检验工序","zh-TW":"檢驗工序","en-US":"Inspection","ja-JP":"検査工程","ko-KR":"검사 공정"}', 4, '检验工序'
  UNION ALL SELECT '包装工序', 'PACKAGING', '{"zh-CN":"包装工序","zh-TW":"包裝工序","en-US":"Packaging","ja-JP":"包装工程","ko-KR":"포장 공정"}', 5, '包装工序'
) item
JOIN `sys_dict` p ON p.tenant_id = 0 AND p.dict_code = 'process_type' AND p.parent_id = 0 AND p.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = 0 AND d.parent_id = p.id AND d.dict_value = item.dict_value AND d.deleted = 0
);

-- 字典项：report_type
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT 0, p.id, item.dict_name, p.dict_code, p.module_id, item.dict_value, item.i18n_json,
       CONCAT(p.dict_code, '/', item.dict_value), 2, 0, item.order_num, 1, item.remark, @script_user, @now, @script_user, @now, 0, NULL
FROM (
  SELECT '按件报工' dict_name, 'BY_QUANTITY' dict_value, '{"zh-CN":"按件报工","zh-TW":"按件報工","en-US":"By Quantity","ja-JP":"数量別","ko-KR":"수량별"}' i18n_json, 1 order_num, '按完成数量报工' remark
  UNION ALL SELECT '按工时报工', 'BY_HOUR', '{"zh-CN":"按工时报工","zh-TW":"按工時報工","en-US":"By Hour","ja-JP":"時間別","ko-KR":"시간별"}', 2, '按工时报工'
  UNION ALL SELECT '按工序报工', 'BY_PROCESS', '{"zh-CN":"按工序报工","zh-TW":"按工序報工","en-US":"By Process","ja-JP":"工程別","ko-KR":"공정별"}', 3, '按工序报工'
  UNION ALL SELECT '按批次报工', 'BY_BATCH', '{"zh-CN":"按批次报工","zh-TW":"按批次報工","en-US":"By Batch","ja-JP":"バッチ別","ko-KR":"배치별"}', 4, '按批次报工'
) item
JOIN `sys_dict` p ON p.tenant_id = 0 AND p.dict_code = 'report_type' AND p.parent_id = 0 AND p.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = 0 AND d.parent_id = p.id AND d.dict_value = item.dict_value AND d.deleted = 0
);

-- 字典项：qc_trigger_point
INSERT INTO `sys_dict`
(`tenant_id`, `parent_id`, `dict_name`, `dict_code`, `module_id`, `dict_value`, `dict_value_i18n_json`, `node_path`, `level`, `children_count`, `order_num`, `status`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`, `deleted`, `tag_style_json`)
SELECT 0, p.id, item.dict_name, p.dict_code, p.module_id, item.dict_value, item.i18n_json,
       CONCAT(p.dict_code, '/', item.dict_value), 2, 0, item.order_num, 1, item.remark, @script_user, @now, @script_user, @now, 0, NULL
FROM (
  SELECT '无触发' dict_name, 'NONE' dict_value, '{"zh-CN":"无触发","zh-TW":"無觸發","en-US":"None","ja-JP":"なし","ko-KR":"없음"}' i18n_json, 1 order_num, '不需要质检' remark
  UNION ALL SELECT '工序开始', 'START', '{"zh-CN":"工序开始","zh-TW":"工序開始","en-US":"Start","ja-JP":"工程開始","ko-KR":"공정 시작"}', 2, '工序开始时触发质检'
  UNION ALL SELECT '工序结束', 'END', '{"zh-CN":"工序结束","zh-TW":"工序結束","en-US":"End","ja-JP":"工程終了","ko-KR":"공정 종료"}', 3, '工序结束时触发质检'
  UNION ALL SELECT '首件检验', 'FIRST_PIECE', '{"zh-CN":"首件检验","zh-TW":"首件檢驗","en-US":"First Piece","ja-JP":"初品検査","ko-KR":"초물 검사"}', 4, '首件检验'
  UNION ALL SELECT '巡检', 'PATROL', '{"zh-CN":"巡检","zh-TW":"巡檢","en-US":"Patrol","ja-JP":"巡検","ko-KR":"순회 검사"}', 5, '巡检触发'
) item
JOIN `sys_dict` p ON p.tenant_id = 0 AND p.dict_code = 'qc_trigger_point' AND p.parent_id = 0 AND p.deleted = 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d
  WHERE d.tenant_id = 0 AND d.parent_id = p.id AND d.dict_value = item.dict_value AND d.deleted = 0
);

-- ----------------------------------------------------------------
-- 六、菜单与按钮权限初始化
-- ----------------------------------------------------------------
SET @basic_module_id := COALESCE(
  (SELECT id FROM `sys_module` WHERE deleted = 0 AND code = 'basic' ORDER BY id LIMIT 1),
  (SELECT module_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'BasicSupplier' ORDER BY id LIMIT 1),
  5
);
SET @public_tenant_id := COALESCE(
  (SELECT tenant_id FROM `sys_module` WHERE id = @basic_module_id LIMIT 1),
  1
);
SET @admin_role_id := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @public_tenant_id AND role_key = 'admin' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id LIMIT 1)
);

-- 一级菜单：产线 / 工段 / 工序
INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, 0, 'menu', item.path, item.name, item.name_i18n_json, item.icon, item.component_key, item.perm_key,
       item.order_num, 1, 1, @now, @script_user, @now, @script_user, 0, 1, 'embedded', NULL
FROM (
  SELECT 'productionLine' path, '产线管理' name, '{"zh-CN":"产线管理","zh-TW":"產線管理","en-US":"Production Line","ja-JP":"生産ライン管理","ko-KR":"생산 라인 관리"}' name_i18n_json, 'NodeIndexOutlined' icon, 'BasicProductionLine' component_key, 'basic:productionLine:query' perm_key, 66 order_num
  UNION ALL SELECT 'workSection', '工段管理', '{"zh-CN":"工段管理","zh-TW":"工段管理","en-US":"Work Section","ja-JP":"工区管理","ko-KR":"작업 구간 관리"}', 'PartitionOutlined', 'BasicWorkSection', 'basic:workSection:query', 67
  UNION ALL SELECT 'process', '工序管理', '{"zh-CN":"工序管理","zh-TW":"工序管理","en-US":"Process","ja-JP":"工程管理","ko-KR":"공정 관리"}', 'OrderedListOutlined', 'BasicProcess', 'basic:process:query', 68
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` existing
  WHERE existing.deleted = 0
    AND existing.component_key = item.component_key
);

-- 按钮权限
INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT parent.tenant_id, parent.tenant_type, parent.module_id, parent.id, 'button', item.path, item.name, item.name_i18n_json, NULL, item.component_key,
       item.perm_key, item.order_num, 0, 1, @now, @script_user, @now, @script_user, 0, 2, 'embedded', NULL
FROM (
  -- 产线
  SELECT 'BasicProductionLine' parent_key, 'query' path, '产线查询' name, '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}' name_i18n_json, NULL component_key, 'basic:productionLine:query' perm_key, 1 order_num
  UNION ALL SELECT 'BasicProductionLine', 'add', '产线新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:productionLine:add', 2
  UNION ALL SELECT 'BasicProductionLine', 'edit', '产线编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:productionLine:edit', 3
  UNION ALL SELECT 'BasicProductionLine', 'delete', '产线删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:productionLine:delete', 4
  UNION ALL SELECT 'BasicProductionLine', 'batchDelete', '批量删除', '{"zh-CN":"批量删除","zh-TW":"批次刪除","en-US":"Batch Delete","ja-JP":"一括削除","ko-KR":"일괄 삭제"}', NULL, 'basic:productionLine:batchDelete', 5
  -- 工段
  UNION ALL SELECT 'BasicWorkSection', 'query', '工段查询', '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}', NULL, 'basic:workSection:query', 1
  UNION ALL SELECT 'BasicWorkSection', 'add', '工段新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:workSection:add', 2
  UNION ALL SELECT 'BasicWorkSection', 'edit', '工段编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:workSection:edit', 3
  UNION ALL SELECT 'BasicWorkSection', 'delete', '工段删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:workSection:delete', 4
  UNION ALL SELECT 'BasicWorkSection', 'batchDelete', '批量删除', '{"zh-CN":"批量删除","zh-TW":"批次刪除","en-US":"Batch Delete","ja-JP":"一括削除","ko-KR":"일괄 삭제"}', NULL, 'basic:workSection:batchDelete', 5
  -- 工序
  UNION ALL SELECT 'BasicProcess', 'query', '工序查询', '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}', NULL, 'basic:process:query', 1
  UNION ALL SELECT 'BasicProcess', 'add', '工序新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:process:add', 2
  UNION ALL SELECT 'BasicProcess', 'edit', '工序编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:process:edit', 3
  UNION ALL SELECT 'BasicProcess', 'delete', '工序删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:process:delete', 4
  UNION ALL SELECT 'BasicProcess', 'batchDelete', '批量删除', '{"zh-CN":"批量删除","zh-TW":"批次刪除","en-US":"Batch Delete","ja-JP":"一括削除","ko-KR":"일괄 삭제"}', NULL, 'basic:process:batchDelete', 5
  -- 车间新增按钮：listByFactory
  UNION ALL SELECT 'BasicWorkshop', 'listByFactory', '按工厂查询车间', '{"zh-CN":"按工厂查询","zh-TW":"按工廠查詢","en-US":"List By Factory","ja-JP":"工場別一覧","ko-KR":"공장별 조회"}', NULL, 'basic:workshop:listByFactory', 6
) item
JOIN `sys_menu` parent ON parent.deleted = 0 AND parent.component_key = item.parent_key
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` existing
  WHERE existing.deleted = 0
    AND existing.parent_id = parent.id
    AND existing.perm_key = item.perm_key
);

-- 接口权限
INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT item.permission_name, item.permission_key, item.url, 'POST', 0, @now, @now, 0
FROM (
  -- 产线
  SELECT '产线查询' permission_name, 'basic:productionLine:query' permission_key, '/basic/productionLine/page' url
  UNION ALL SELECT '产线列表', 'basic:productionLine:list', '/basic/productionLine/list'
  UNION ALL SELECT '按车间拉取产线下拉', 'basic:productionLine:listByWorkshop', '/basic/productionLine/listByWorkshop'
  UNION ALL SELECT '产线详情', 'basic:productionLine:detail', '/basic/productionLine/detail'
  UNION ALL SELECT '产线新增', 'basic:productionLine:add', '/basic/productionLine/create'
  UNION ALL SELECT '产线编辑', 'basic:productionLine:edit', '/basic/productionLine/update'
  UNION ALL SELECT '产线删除', 'basic:productionLine:delete', '/basic/productionLine/delete'
  UNION ALL SELECT '产线批量删除', 'basic:productionLine:batchDelete', '/basic/productionLine/batchDelete'
  -- 工段
  UNION ALL SELECT '工段查询', 'basic:workSection:query', '/basic/workSection/page'
  UNION ALL SELECT '工段列表', 'basic:workSection:list', '/basic/workSection/list'
  UNION ALL SELECT '按车间拉取工段下拉', 'basic:workSection:listByWorkshop', '/basic/workSection/listByWorkshop'
  UNION ALL SELECT '按产线拉取工段下拉', 'basic:workSection:listByProductionLine', '/basic/workSection/listByProductionLine'
  UNION ALL SELECT '工段详情', 'basic:workSection:detail', '/basic/workSection/detail'
  UNION ALL SELECT '工段新增', 'basic:workSection:add', '/basic/workSection/create'
  UNION ALL SELECT '工段编辑', 'basic:workSection:edit', '/basic/workSection/update'
  UNION ALL SELECT '工段删除', 'basic:workSection:delete', '/basic/workSection/delete'
  UNION ALL SELECT '工段批量删除', 'basic:workSection:batchDelete', '/basic/workSection/batchDelete'
  -- 工序
  UNION ALL SELECT '工序查询', 'basic:process:query', '/basic/process/page'
  UNION ALL SELECT '工序列表', 'basic:process:list', '/basic/process/list'
  UNION ALL SELECT '按工段拉取工序下拉', 'basic:process:listByWorkSection', '/basic/process/listByWorkSection'
  UNION ALL SELECT '工序详情', 'basic:process:detail', '/basic/process/detail'
  UNION ALL SELECT '工序新增', 'basic:process:add', '/basic/process/create'
  UNION ALL SELECT '工序编辑', 'basic:process:edit', '/basic/process/update'
  UNION ALL SELECT '工序删除', 'basic:process:delete', '/basic/process/delete'
  UNION ALL SELECT '工序批量删除', 'basic:process:batchDelete', '/basic/process/batchDelete'
  -- 车间扩展
  UNION ALL SELECT '按工厂查询车间', 'basic:workshop:listByFactory', '/basic/workshop/listByFactory'
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` existing
  WHERE existing.deleted = 0
    AND existing.permission_key = item.permission_key
);

-- 角色权限
INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.id
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.deleted = 0
  AND p.permission_key IN (
    'basic:productionLine:query','basic:productionLine:list','basic:productionLine:listByWorkshop','basic:productionLine:detail',
    'basic:productionLine:add','basic:productionLine:edit','basic:productionLine:delete','basic:productionLine:batchDelete',
    'basic:workSection:query','basic:workSection:list','basic:workSection:listByWorkshop','basic:workSection:listByProductionLine','basic:workSection:detail',
    'basic:workSection:add','basic:workSection:edit','basic:workSection:delete','basic:workSection:batchDelete',
    'basic:process:query','basic:process:list','basic:process:listByWorkSection','basic:process:detail',
    'basic:process:add','basic:process:edit','basic:process:delete','basic:process:batchDelete',
    'basic:workshop:listByFactory'
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.role_id = @admin_role_id AND rp.permission_id = p.id
  );

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @public_tenant_id, @admin_role_id, m.id
FROM `sys_menu` m
WHERE @admin_role_id IS NOT NULL
  AND m.deleted = 0
  AND (
    m.component_key IN ('BasicProductionLine','BasicWorkSection','BasicProcess')
    OR m.perm_key IN (
      'basic:productionLine:query','basic:productionLine:add','basic:productionLine:edit','basic:productionLine:delete','basic:productionLine:batchDelete',
      'basic:workSection:query','basic:workSection:add','basic:workSection:edit','basic:workSection:delete','basic:workSection:batchDelete',
      'basic:process:query','basic:process:add','basic:process:edit','basic:process:delete','basic:process:batchDelete',
      'basic:workshop:listByFactory'
    )
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @public_tenant_id AND rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

-- ----------------------------------------------------------------
-- 七、forgex_common：表格配置
-- ----------------------------------------------------------------
USE `forgex_common`;

SET @script_user := '20260619_factory_modeling';
SET @now := NOW();

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.table_name_i18n_json, 'BUSINESS', 'id', 10, 1, 1, @script_user, @now, @script_user, @now, 0
FROM (
  SELECT 'BasicProductionLineTable' table_code, '{"zh-CN":"产线管理","zh-TW":"產線管理","en-US":"Production Line","ja-JP":"生産ライン管理","ko-KR":"생산 라인 관리"}' table_name_i18n_json
  UNION ALL SELECT 'BasicWorkSectionTable', '{"zh-CN":"工段管理","zh-TW":"工段管理","en-US":"Work Section","ja-JP":"工区管理","ko-KR":"작업 구간 관리"}'
  UNION ALL SELECT 'BasicProcessTable', '{"zh-CN":"工序管理","zh-TW":"工序管理","en-US":"Process","ja-JP":"工程管理","ko-KR":"공정 관리"}'
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = item.table_code
    AND existing.deleted = 0
);

-- 表格列配置
INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.field, item.title_i18n_json, item.align, item.width, item.fixed, item.ellipsis, item.sortable, item.sorter_field,
       item.queryable, item.query_type, item.query_operator, item.dict_code, item.render_type, NULL, item.order_num, 1, @script_user, @now, @script_user, @now, 0
FROM (
  -- 产线
  SELECT 'BasicProductionLineTable' table_code, 'productionLineName' field, '{"zh-CN":"产线名称","zh-TW":"產線名稱","en-US":"Production Line","ja-JP":"生産ライン名","ko-KR":"생산 라인명"}' title_i18n_json, 'left' align, 220 width, NULL fixed, 1 ellipsis, 1 sortable, 'productionLineName' sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, NULL render_type, 1 order_num
  UNION ALL SELECT 'BasicProductionLineTable','workshopName','{"zh-CN":"所属车间","zh-TW":"所屬車間","en-US":"Workshop","ja-JP":"所属職場","ko-KR":"소속 작업장"}','left',180,NULL,1,0,NULL,1,'select','eq',NULL,NULL,2
  UNION ALL SELECT 'BasicProductionLineTable','productionLineType','{"zh-CN":"产线类型","zh-TW":"產線類型","en-US":"Line Type","ja-JP":"ライン種別","ko-KR":"라인 유형"}','center',140,NULL,0,0,NULL,1,'select','eq','prod_line_type','tag',3
  UNION ALL SELECT 'BasicProductionLineTable','managerEmployeeName','{"zh-CN":"负责人","zh-TW":"負責人","en-US":"Manager","ja-JP":"責任者","ko-KR":"책임자"}','left',140,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicProductionLineTable','sortOrder','{"zh-CN":"排序号","zh-TW":"排序","en-US":"Sort","ja-JP":"並び順","ko-KR":"정렬"}','center',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,5
  UNION ALL SELECT 'BasicProductionLineTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',6
  UNION ALL SELECT 'BasicProductionLineTable','remark','{"zh-CN":"备注","zh-TW":"備註","en-US":"Remark","ja-JP":"備考","ko-KR":"비고"}','left',220,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,7
  UNION ALL SELECT 'BasicProductionLineTable','createTime','{"zh-CN":"创建时间","zh-TW":"建立時間","en-US":"Create Time","ja-JP":"作成日時","ko-KR":"생성 시간"}','center',180,NULL,0,1,'createTime',0,NULL,NULL,NULL,NULL,8
  UNION ALL SELECT 'BasicProductionLineTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  -- 工段
  UNION ALL SELECT 'BasicWorkSectionTable','workSectionName','{"zh-CN":"工段名称","zh-TW":"工段名稱","en-US":"Work Section","ja-JP":"工区名","ko-KR":"작업 구간명"}','left',220,NULL,1,1,'workSectionName',1,'input','like',NULL,NULL,1
  UNION ALL SELECT 'BasicWorkSectionTable','workshopName','{"zh-CN":"所属车间","zh-TW":"所屬車間","en-US":"Workshop","ja-JP":"所属職場","ko-KR":"소속 작업장"}','left',180,NULL,1,0,NULL,1,'select','eq',NULL,NULL,2
  UNION ALL SELECT 'BasicWorkSectionTable','productionLineName','{"zh-CN":"所属产线","zh-TW":"所屬產線","en-US":"Production Line","ja-JP":"所属ライン","ko-KR":"소속 라인"}','left',180,NULL,1,0,NULL,1,'select','eq',NULL,NULL,3
  UNION ALL SELECT 'BasicWorkSectionTable','sortOrder','{"zh-CN":"顺序号","zh-TW":"順序","en-US":"Sort","ja-JP":"順序","ko-KR":"순서"}','center',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicWorkSectionTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',5
  UNION ALL SELECT 'BasicWorkSectionTable','remark','{"zh-CN":"备注","zh-TW":"備註","en-US":"Remark","ja-JP":"備考","ko-KR":"비고"}','left',220,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,6
  UNION ALL SELECT 'BasicWorkSectionTable','createTime','{"zh-CN":"创建时间","zh-TW":"建立時間","en-US":"Create Time","ja-JP":"作成日時","ko-KR":"생성 시간"}','center',180,NULL,0,1,'createTime',0,NULL,NULL,NULL,NULL,7
  UNION ALL SELECT 'BasicWorkSectionTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  -- 工序
  UNION ALL SELECT 'BasicProcessTable','processName','{"zh-CN":"工序名称","zh-TW":"工序名稱","en-US":"Process","ja-JP":"工程名","ko-KR":"공정명"}','left',220,NULL,1,1,'processName',1,'input','like',NULL,NULL,1
  UNION ALL SELECT 'BasicProcessTable','workSectionName','{"zh-CN":"所属工段","zh-TW":"所屬工段","en-US":"Work Section","ja-JP":"所属工区","ko-KR":"소속 작업 구간"}','left',180,NULL,1,0,NULL,1,'select','eq',NULL,NULL,2
  UNION ALL SELECT 'BasicProcessTable','processType','{"zh-CN":"工序类型","zh-TW":"工序類型","en-US":"Process Type","ja-JP":"工程種別","ko-KR":"공정 유형"}','center',140,NULL,0,0,NULL,1,'select','eq','process_type','tag',3
  UNION ALL SELECT 'BasicProcessTable','reportType','{"zh-CN":"报工方式","zh-TW":"報工方式","en-US":"Report Type","ja-JP":"報告方式","ko-KR":"보고 방식"}','center',140,NULL,0,0,NULL,1,'select','eq','report_type','tag',4
  UNION ALL SELECT 'BasicProcessTable','qcTriggerPoint','{"zh-CN":"质检触发点","zh-TW":"質檢觸發點","en-US":"QC Trigger","ja-JP":"検査トリガ","ko-KR":"검사 트리거"}','center',140,NULL,0,0,NULL,1,'select','eq','qc_trigger_point','tag',5
  UNION ALL SELECT 'BasicProcessTable','sortOrder','{"zh-CN":"顺序号","zh-TW":"順序","en-US":"Sort","ja-JP":"順序","ko-KR":"순서"}','center',100,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,6
  UNION ALL SELECT 'BasicProcessTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',7
  UNION ALL SELECT 'BasicProcessTable','remark','{"zh-CN":"备注","zh-TW":"備註","en-US":"Remark","ja-JP":"備考","ko-KR":"비고"}','left',220,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,8
  UNION ALL SELECT 'BasicProcessTable','createTime','{"zh-CN":"创建时间","zh-TW":"建立時間","en-US":"Create Time","ja-JP":"作成日時","ko-KR":"생성 시간"}','center',180,NULL,0,1,'createTime',0,NULL,NULL,NULL,NULL,9
  UNION ALL SELECT 'BasicProcessTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  -- 车间表追加 2 列
  UNION ALL SELECT 'BasicWorkshopTable','workshopType','{"zh-CN":"车间类型","zh-TW":"車間類型","en-US":"Workshop Type","ja-JP":"職場種別","ko-KR":"작업장 유형"}','center',140,NULL,0,0,NULL,1,'select','eq','workshop_type','tag',2
  UNION ALL SELECT 'BasicWorkshopTable','workshopManagerName','{"zh-CN":"负责人","zh-TW":"負責人","en-US":"Manager","ja-JP":"責任者","ko-KR":"책임자"}','left',140,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,3
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = item.table_code
    AND existing.field = item.field
    AND existing.deleted = 0
);

-- 非公共租户复制列配置
INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT cfg.`tenant_id`, pub.`table_code`, pub.`field`, pub.`title_i18n_json`, pub.`align`, pub.`width`, pub.`fixed`, pub.`ellipsis`, pub.`sortable`, pub.`sorter_field`, pub.`queryable`, pub.`query_type`, pub.`query_operator`, pub.`dict_code`, pub.`render_type`, pub.`perm_key`, pub.`order_num`, pub.`enabled`, @script_user, @now, @script_user, @now, 0
FROM `fx_table_config` cfg
JOIN `fx_table_column_config` pub
  ON pub.`tenant_id` = 0
 AND pub.`table_code` = cfg.`table_code`
 AND pub.`deleted` = 0
WHERE cfg.`tenant_id` <> 0
  AND cfg.`deleted` = 0
  AND cfg.`table_code` IN ('BasicProductionLineTable','BasicWorkSectionTable','BasicProcessTable','BasicWorkshopTable')
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` tgt
    WHERE tgt.`tenant_id` = cfg.`tenant_id`
      AND tgt.`table_code` = pub.`table_code`
      AND tgt.`field` = pub.`field`
      AND tgt.`deleted` = 0
  );

SELECT 'factory_modeling_upgrade_done' AS result;
