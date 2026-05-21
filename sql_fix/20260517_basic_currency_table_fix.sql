-- Forgex 币种管理缺表修复脚本
-- 修复内容：
--   1. 补齐币种管理后端依赖的 mdm_currency、mdm_exchange_rate_type、
--      mdm_currency_exchange_rate、mdm_exchange_rate_log 表。
--   2. 按当前租户补充 CNY 与 BOOKING 汇率类型种子数据，避免页面首次打开为空。
-- 可重复执行：建表使用 IF NOT EXISTS，种子数据使用 NOT EXISTS 防重。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @OPERATOR := '20260517_currency_table_fix';
SET @TENANT_ID := COALESCE(
  (SELECT id FROM `sys_tenant` WHERE tenant_code = 'default' AND deleted = 0 ORDER BY id LIMIT 1),
  (SELECT tenant_id FROM `sys_module` WHERE code = 'sys' AND deleted = 0 ORDER BY id LIMIT 1),
  1
);

CREATE TABLE IF NOT EXISTS `mdm_currency` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `currency_code` char(3) NOT NULL COMMENT 'ISO 4217 三位字母编码',
  `currency_num_code` char(3) DEFAULT NULL COMMENT 'ISO 4217 三位数字编码',
  `currency_name_cn` varchar(50) DEFAULT NULL COMMENT '币种中文名称',
  `currency_name_en` varchar(100) DEFAULT NULL COMMENT '币种英文名称',
  `currency_symbol` varchar(10) DEFAULT NULL COMMENT '币种符号',
  `decimal_digits` tinyint NOT NULL DEFAULT 2 COMMENT '金额小数位',
  `is_base_currency` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否本位币',
  `country_region` varchar(100) DEFAULT NULL COMMENT '国家或地区',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mdm_currency_tenant_code_deleted` (`tenant_id`, `currency_code`, `deleted`),
  KEY `idx_mdm_currency_status` (`status`, `deleted`),
  KEY `idx_mdm_currency_tenant` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='币种主数据表';

CREATE TABLE IF NOT EXISTS `mdm_exchange_rate_type` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `rate_type_code` varchar(32) NOT NULL COMMENT '汇率类型编码',
  `rate_type_name` varchar(50) DEFAULT NULL COMMENT '汇率类型名称',
  `business_scene` varchar(255) DEFAULT NULL COMMENT '适用业务场景',
  `is_default` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否默认',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_mdm_rate_type_tenant_code_deleted` (`tenant_id`, `rate_type_code`, `deleted`),
  KEY `idx_mdm_rate_type_tenant` (`tenant_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率类型表';

CREATE TABLE IF NOT EXISTS `mdm_currency_exchange_rate` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `source_currency_code` char(3) NOT NULL COMMENT '源币种编码',
  `target_currency_code` char(3) NOT NULL COMMENT '目标币种编码',
  `rate_type_code` varchar(32) NOT NULL COMMENT '汇率类型编码',
  `effective_date` date NOT NULL COMMENT '生效日期',
  `exchange_rate` decimal(20,8) NOT NULL COMMENT '汇率值',
  `expire_date` date NOT NULL DEFAULT '9999-12-31' COMMENT '失效日期',
  `approve_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态：0-待审批 1-已生效 2-已驳回 3-审批中',
  `approve_user` varchar(64) DEFAULT NULL COMMENT '审批人',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `org_id` bigint DEFAULT NULL COMMENT '组织 ID',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_mdm_rate_dimension` (`tenant_id`, `source_currency_code`, `target_currency_code`, `rate_type_code`, `org_id`, `effective_date`, `expire_date`),
  KEY `idx_mdm_rate_status` (`tenant_id`, `approve_status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率明细表';

CREATE TABLE IF NOT EXISTS `mdm_exchange_rate_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `rate_id` bigint NOT NULL COMMENT '汇率记录 ID',
  `operation_type` varchar(32) NOT NULL COMMENT '操作类型',
  `operation_content` text DEFAULT NULL COMMENT '操作内容',
  `operator_ip` varchar(64) DEFAULT NULL COMMENT '操作者 IP',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_mdm_rate_log_rate` (`tenant_id`, `rate_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='汇率操作日志表';

INSERT INTO `mdm_currency` (
  `tenant_id`, `currency_code`, `currency_num_code`, `currency_name_cn`, `currency_name_en`,
  `currency_symbol`, `decimal_digits`, `is_base_currency`, `country_region`, `status`,
  `remark`, `create_by`, `update_by`, `deleted`
)
SELECT seed.tenant_id, 'CNY', '156', '人民币', 'Chinese Yuan',
       '¥', 2, 1, '中国', 1, '系统初始化本位币', @OPERATOR, @OPERATOR, 0
FROM (
  SELECT 0 AS tenant_id
  UNION
  SELECT @TENANT_ID
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM `mdm_currency` c
  WHERE c.tenant_id = seed.tenant_id
    AND c.currency_code = 'CNY'
    AND c.deleted = 0
);

INSERT INTO `mdm_exchange_rate_type` (
  `tenant_id`, `rate_type_code`, `rate_type_name`, `business_scene`, `is_default`, `status`,
  `create_by`, `update_by`, `deleted`
)
SELECT seed.tenant_id, 'BOOKING', '记账汇率', '财务总账核算', 1, 1, @OPERATOR, @OPERATOR, 0
FROM (
  SELECT 0 AS tenant_id
  UNION
  SELECT @TENANT_ID
) seed
WHERE NOT EXISTS (
  SELECT 1
  FROM `mdm_exchange_rate_type` t
  WHERE t.tenant_id = seed.tenant_id
    AND t.rate_type_code = 'BOOKING'
    AND t.deleted = 0
);

SELECT 'mdm_currency' AS table_name, COUNT(*) AS row_count FROM `mdm_currency`
UNION ALL
SELECT 'mdm_exchange_rate_type', COUNT(*) FROM `mdm_exchange_rate_type`
UNION ALL
SELECT 'mdm_currency_exchange_rate', COUNT(*) FROM `mdm_currency_exchange_rate`
UNION ALL
SELECT 'mdm_exchange_rate_log', COUNT(*) FROM `mdm_exchange_rate_log`;
