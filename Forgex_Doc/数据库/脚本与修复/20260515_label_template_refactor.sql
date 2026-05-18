-- 标签模板四表重构脚本
-- 适用库：forgex_admin / forgex_common
-- 说明：脚本可重复执行。旧 basic_label_template.template_content 保留兼容，但新设计以 detail 表为准。

USE `forgex_admin`;

SET @script_user := '20260515_label_template_refactor';

CREATE TABLE IF NOT EXISTS `basic_label_type` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `type_code` varchar(64) NOT NULL COMMENT '标签类型编码',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=否，1=是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_label_type_code` (`tenant_id`, `type_code`, `deleted`),
  KEY `idx_basic_label_type_tenant_enabled` (`tenant_id`, `is_enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签类型表';

CREATE TABLE IF NOT EXISTS `basic_label_field` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `field_code` varchar(100) NOT NULL COMMENT '字段编码',
  `field_name` varchar(100) NOT NULL COMMENT '字段名称',
  `field_type` varchar(32) NOT NULL COMMENT '字段类型：STRING/NUMBER/DATE/DATETIME/BOOLEAN',
  `module_id` bigint NOT NULL COMMENT '模块 ID，关联 sys_module.id',
  `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=否，1=是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_label_field_code` (`tenant_id`, `module_id`, `field_code`, `deleted`),
  KEY `idx_basic_label_field_module` (`tenant_id`, `module_id`, `is_enabled`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签字段表';

CREATE TABLE IF NOT EXISTS `basic_label_template_detail` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `template_id` bigint NOT NULL COMMENT '模板 ID',
  `component_type` varchar(32) NOT NULL COMMENT '组件类型',
  `position_x` int NOT NULL DEFAULT 0 COMMENT '组件 X 坐标，单位 mm',
  `position_y` int NOT NULL DEFAULT 0 COMMENT '组件 Y 坐标，单位 mm',
  `component_width` int NOT NULL DEFAULT 10 COMMENT '组件宽度，单位 mm',
  `component_height` int NOT NULL DEFAULT 6 COMMENT '组件高度，单位 mm',
  `component_content` text DEFAULT NULL COMMENT '组件内容',
  `data_source` varchar(32) NOT NULL DEFAULT 'FIXED' COMMENT '数据来源：FIXED/FIELD',
  `field_code` varchar(100) DEFAULT NULL COMMENT '业务字段编码',
  `style_json` json DEFAULT NULL COMMENT '组件样式 JSON',
  `sort_no` int NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_label_template_detail_tpl` (`tenant_id`, `template_id`, `deleted`, `sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签模板组件详情表';

SET @has_type_id := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_label_template' AND COLUMN_NAME = 'type_id');
SET @ddl := IF(@has_type_id = 0, 'ALTER TABLE `basic_label_template` ADD COLUMN `type_id` bigint DEFAULT NULL COMMENT ''标签类型 ID'' AFTER `template_name`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_paper_width := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_label_template' AND COLUMN_NAME = 'paper_width');
SET @ddl := IF(@has_paper_width = 0, 'ALTER TABLE `basic_label_template` ADD COLUMN `paper_width` int NOT NULL DEFAULT 100 COMMENT ''纸张宽度，单位 mm'' AFTER `template_version`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_paper_height := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_label_template' AND COLUMN_NAME = 'paper_height');
SET @ddl := IF(@has_paper_height = 0, 'ALTER TABLE `basic_label_template` ADD COLUMN `paper_height` int NOT NULL DEFAULT 60 COMMENT ''纸张高度，单位 mm'' AFTER `paper_width`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_paper_size := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_label_template' AND COLUMN_NAME = 'paper_size');
SET @ddl := IF(@has_paper_size = 0, 'ALTER TABLE `basic_label_template` ADD COLUMN `paper_size` varchar(32) NOT NULL DEFAULT ''CUSTOM'' COMMENT ''纸张规格'' AFTER `paper_height`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_is_enabled := (SELECT COUNT(1) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'basic_label_template' AND COLUMN_NAME = 'is_enabled');
SET @ddl := IF(@has_is_enabled = 0, 'ALTER TABLE `basic_label_template` ADD COLUMN `is_enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT ''是否启用：0=否，1=是'' AFTER `paper_size`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

INSERT INTO `basic_label_type` (`id`, `tenant_id`, `type_code`, `type_name`, `is_enabled`, `create_by`, `update_by`, `deleted`)
SELECT 202605150001, 0, 'GENERAL', '通用标签', 1, @script_user, @script_user, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `basic_label_type` WHERE `tenant_id` = 0 AND `type_code` = 'GENERAL' AND `deleted` = 0
);

USE `forgex_common`;

SET @script_user := '20260515_label_template_refactor';

INSERT INTO `fx_table_config` (`tenant_id`, `table_code`, `table_name_i18n_json`, `table_type`, `row_key`, `default_page_size`, `enabled`, `version`, `create_by`, `update_by`, `deleted`)
SELECT 0, v.table_code,
       JSON_OBJECT('zh-CN', v.table_name, 'en-US', v.en_name, 'zh-TW', v.table_name, 'ja-JP', v.table_name, 'ko-KR', v.table_name),
       'BUSINESS', 'id', 10, 1, 1, @script_user, @script_user, 0
FROM (
  SELECT 'LabelTypeTable' table_code, '标签类型' table_name, 'Label Type' en_name
  UNION ALL SELECT 'LabelFieldTable', '标签字段', 'Label Field'
  UNION ALL SELECT 'LabelTemplateTable', '标签模板', 'Label Template'
) v
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` t WHERE t.tenant_id = 0 AND t.table_code = v.table_code
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`queryable`,`query_type`,`query_operator`,`dict_code`,`order_num`,`enabled`,`create_by`,`update_by`,`deleted`)
SELECT 0, c.table_code, c.field, JSON_OBJECT('zh-CN', c.title, 'en-US', c.title, 'zh-TW', c.title, 'ja-JP', c.title, 'ko-KR', c.title),
       'center', c.width, c.fixed, 0, 0, c.queryable, c.query_type, c.query_operator, c.dict_code, c.order_num, 1, @script_user, @script_user, 0
FROM (
  SELECT 'LabelTypeTable' table_code, 'typeCode' field, '类型编码' title, 160 width, NULL fixed, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, 1 order_num
  UNION ALL SELECT 'LabelTypeTable','typeName','类型名称',180,NULL,1,'input','like',NULL,2
  UNION ALL SELECT 'LabelTypeTable','isEnabled','启用',90,NULL,1,'select','eq',NULL,3
  UNION ALL SELECT 'LabelTypeTable','createTime','创建时间',180,NULL,0,NULL,NULL,NULL,8
  UNION ALL SELECT 'LabelTypeTable','action','操作',180,'right',0,NULL,NULL,NULL,99
  UNION ALL SELECT 'LabelFieldTable','fieldCode','字段编码',160,NULL,1,'input','like',NULL,1
  UNION ALL SELECT 'LabelFieldTable','fieldName','字段名称',180,NULL,1,'input','like',NULL,2
  UNION ALL SELECT 'LabelFieldTable','fieldType','字段类型',120,NULL,1,'select','eq',NULL,3
  UNION ALL SELECT 'LabelFieldTable','moduleName','模块',180,NULL,0,NULL,NULL,NULL,4
  UNION ALL SELECT 'LabelFieldTable','isEnabled','启用',90,NULL,1,'select','eq',NULL,5
  UNION ALL SELECT 'LabelFieldTable','createTime','创建时间',180,NULL,0,NULL,NULL,NULL,8
  UNION ALL SELECT 'LabelFieldTable','action','操作',220,'right',0,NULL,NULL,NULL,99
  UNION ALL SELECT 'LabelTemplateTable','templateCode','模板编码',160,NULL,1,'input','like',NULL,1
  UNION ALL SELECT 'LabelTemplateTable','templateName','模板名称',180,NULL,1,'input','like',NULL,2
  UNION ALL SELECT 'LabelTemplateTable','paperSize','纸张规格',100,NULL,0,NULL,NULL,NULL,3
  UNION ALL SELECT 'LabelTemplateTable','paperWidth','宽(mm)',90,NULL,0,NULL,NULL,NULL,4
  UNION ALL SELECT 'LabelTemplateTable','paperHeight','高(mm)',90,NULL,0,NULL,NULL,NULL,5
  UNION ALL SELECT 'LabelTemplateTable','isEnabled','启用',90,NULL,1,'select','eq',NULL,6
  UNION ALL SELECT 'LabelTemplateTable','createTime','创建时间',180,NULL,0,NULL,NULL,NULL,8
  UNION ALL SELECT 'LabelTemplateTable','action','操作',240,'right',0,NULL,NULL,NULL,99
) c
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config` t
  WHERE t.tenant_id = 0 AND t.table_code = c.table_code AND t.field = c.field AND t.deleted = 0
);

INSERT INTO `fx_excel_import_config`
(`tenant_id`,`table_code`,`table_name`,`handler_bean_name`,`import_permission`,`title`,`title_i18n_json`,`subtitle`,`subtitle_i18n_json`,`subtitle_style_json`,`version`,`create_by`,`update_by`,`deleted`)
SELECT 0, 'LabelFieldImportTable', '标签字段导入', 'labelFieldImportHandler', 'label:field:import',
       '标签字段导入模板',
       JSON_OBJECT('zh-CN','标签字段导入模板','en-US','Label Field Import Template','zh-TW','标签字段导入模板','ja-JP','标签字段导入模板','ko-KR','标签字段导入模板'),
       '标签字段',
       JSON_OBJECT('zh-CN','字段编码、字段名称、字段类型、模块ID必填','en-US','fieldCode, fieldName, fieldType and moduleId are required','zh-TW','字段编码、字段名称、字段类型、模块ID必填','ja-JP','字段编码、字段名称、字段类型、模块ID必填','ko-KR','字段编码、字段名称、字段类型、模块ID必填'),
       JSON_OBJECT('backgroundColor', '#8EC67F', 'wrapText', true, 'fontSize', 12),
       1, @script_user, @script_user, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_excel_import_config` WHERE `tenant_id` = 0 AND `table_code` = 'LabelFieldImportTable'
);

SET @label_field_import_config_id := (
  SELECT id FROM `fx_excel_import_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'LabelFieldImportTable' AND `deleted` = 0
  ORDER BY id DESC LIMIT 1
);

INSERT INTO `fx_excel_import_config_item`
(`tenant_id`,`config_id`,`sheet_code`,`sheet_name`,`import_field`,`i18n_json`,`field_type`,`required`,`order_num`,`create_by`,`update_by`,`deleted`)
SELECT 0, @label_field_import_config_id, 'main', '标签字段', x.import_field,
       JSON_OBJECT('zh-CN', x.title, 'en-US', x.import_field, 'zh-TW', x.title, 'ja-JP', x.title, 'ko-KR', x.title),
       x.field_type, x.required, x.order_num, @script_user, @script_user, 0
FROM (
  SELECT 'fieldCode' import_field, '字段编码' title, 'string' field_type, 1 required, 1 order_num
  UNION ALL SELECT 'fieldName', '字段名称', 'string', 1, 2
  UNION ALL SELECT 'fieldType', '字段类型', 'string', 1, 3
  UNION ALL SELECT 'moduleId', '模块ID', 'number', 1, 4
  UNION ALL SELECT 'isEnabled', '是否启用', 'boolean', 0, 5
) x
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_excel_import_config_item` item
  WHERE item.tenant_id = 0 AND item.config_id = @label_field_import_config_id AND item.import_field = x.import_field AND item.deleted = 0
);
