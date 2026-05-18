-- Forgex 计量单位与币种汇率初始化脚本
-- 目标库：forgex_admin
-- 内容：
--   1. 为公共租户和 default 租户补齐重量、计数、长度、体积计量单位类型。
--   2. 迁移旧默认单位到对应类型，补齐常见单位及固定换算关系。
--   3. 补齐常见币种，并写入 CNY -> 外币的 BOOKING 有效汇率。
-- 汇率来源：Frankfurter API，数据日期 2026-05-15，base=CNY。
-- 可重复执行：种子数据按租户、编码、币种对和生效日期防重。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @OPERATOR := '20260518_seed_basic_units_and_currency_rates';
SET @RATE_DATE := '2026-05-15';
SET @FOREVER_DATE := '9999-12-31';
SET @TENANT_ID := COALESCE(
  (SELECT id FROM `sys_tenant` WHERE tenant_code = 'default' AND deleted = 0 ORDER BY id LIMIT 1),
  (SELECT tenant_id FROM `sys_module` WHERE code = 'sys' AND deleted = 0 ORDER BY id LIMIT 1),
  1
);

DROP TEMPORARY TABLE IF EXISTS `tmp_seed_tenant`;
CREATE TEMPORARY TABLE `tmp_seed_tenant` (
  `seed_no` int NOT NULL,
  `tenant_id` bigint NOT NULL,
  PRIMARY KEY (`tenant_id`)
) ENGINE=Memory;

INSERT INTO `tmp_seed_tenant` (`seed_no`, `tenant_id`) VALUES (1, 0)
ON DUPLICATE KEY UPDATE `seed_no` = VALUES(`seed_no`);

INSERT INTO `tmp_seed_tenant` (`seed_no`, `tenant_id`)
SELECT 2, @TENANT_ID
WHERE @TENANT_ID <> 0
ON DUPLICATE KEY UPDATE `seed_no` = VALUES(`seed_no`);

DROP TEMPORARY TABLE IF EXISTS `tmp_unit_type_seed`;
CREATE TEMPORARY TABLE `tmp_unit_type_seed` (
  `sort_no` int NOT NULL,
  `unit_type_code` varchar(50) NOT NULL,
  `unit_type_name` varchar(100) NOT NULL,
  PRIMARY KEY (`unit_type_code`)
) ENGINE=Memory;

INSERT INTO `tmp_unit_type_seed` (`sort_no`, `unit_type_code`, `unit_type_name`) VALUES
(1, 'WEIGHT', '重量'),
(2, 'COUNT', '计数'),
(3, 'LENGTH', '长度'),
(4, 'VOLUME', '体积');

INSERT INTO `basic_unit_type` (
  `id`, `tenant_id`, `unit_type_code`, `unit_type_name`, `parent_id`, `level_path`,
  `create_time`, `create_by`, `update_time`, `update_by`, `deleted`
)
SELECT
  202605180000000000 + st.seed_no * 1000000 + uts.sort_no AS id,
  st.tenant_id,
  uts.unit_type_code,
  uts.unit_type_name,
  0 AS parent_id,
  CONCAT('0/', 202605180000000000 + st.seed_no * 1000000 + uts.sort_no) AS level_path,
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
INNER JOIN `tmp_unit_type_seed` uts ON 1 = 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `basic_unit_type` bute
  WHERE bute.tenant_id = st.tenant_id
    AND bute.unit_type_code = uts.unit_type_code
    AND bute.deleted = 0
);

UPDATE `basic_unit_type` bute
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = bute.tenant_id
INNER JOIN `tmp_unit_type_seed` uts ON uts.unit_type_code = bute.unit_type_code
SET bute.unit_type_name = uts.unit_type_name,
    bute.parent_id = 0,
    bute.level_path = COALESCE(NULLIF(bute.level_path, ''), CONCAT('0/', bute.id)),
    bute.update_time = NOW(),
    bute.update_by = @OPERATOR
WHERE bute.deleted = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_unit_seed`;
CREATE TEMPORARY TABLE `tmp_unit_seed` (
  `sort_no` int NOT NULL,
  `unit_type_code` varchar(50) NOT NULL,
  `unit_code` varchar(64) NOT NULL,
  `unit_name` varchar(100) NOT NULL,
  `unit_symbol` varchar(32) DEFAULT NULL,
  `unit_category` varchar(64) NOT NULL,
  `base_factor` decimal(30,12) DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`unit_code`)
) ENGINE=Memory;

INSERT INTO `tmp_unit_seed` (
  `sort_no`, `unit_type_code`, `unit_code`, `unit_name`, `unit_symbol`, `unit_category`, `base_factor`, `remark`
) VALUES
(10, 'WEIGHT', 'T', '吨', 't', '重量', 1000.000000000000, '1 T = 1000 KG'),
(20, 'WEIGHT', 'KG', '千克', 'kg', '重量', 1.000000000000, '重量基准单位'),
(30, 'WEIGHT', 'G', '克', 'g', '重量', 0.001000000000, '1 G = 0.001 KG'),
(40, 'WEIGHT', 'MG', '毫克', 'mg', '重量', 0.000001000000, '1 MG = 0.000001 KG'),
(50, 'WEIGHT', 'LB', '磅', 'lb', '重量', 0.453592370000, '1 LB = 0.45359237 KG'),
(60, 'WEIGHT', 'OZ', '盎司', 'oz', '重量', 0.028349523125, '1 OZ = 0.028349523125 KG'),
(110, 'COUNT', 'PCS', '个', 'pcs', '计数', 1.000000000000, '计数基准单位'),
(120, 'COUNT', 'PAIR', '双', 'pair', '计数', 2.000000000000, '1 PAIR = 2 PCS'),
(130, 'COUNT', 'DOZ', '打', 'doz', '计数', 12.000000000000, '1 DOZ = 12 PCS'),
(140, 'COUNT', 'HUNDRED', '百', 'hundred', '计数', 100.000000000000, '1 HUNDRED = 100 PCS'),
(150, 'COUNT', 'THOUSAND', '千', 'thousand', '计数', 1000.000000000000, '1 THOUSAND = 1000 PCS'),
(160, 'COUNT', 'SET', '套', 'set', '计数', NULL, '套装数量随业务定义，不维护固定换算'),
(170, 'COUNT', 'BOX', '箱', 'box', '计数', NULL, '箱规随物料/包装定义，不维护固定换算'),
(210, 'LENGTH', 'KM', '千米', 'km', '长度', 1000.000000000000, '1 KM = 1000 M'),
(220, 'LENGTH', 'M', '米', 'm', '长度', 1.000000000000, '长度基准单位'),
(230, 'LENGTH', 'DM', '分米', 'dm', '长度', 0.100000000000, '1 DM = 0.1 M'),
(240, 'LENGTH', 'CM', '厘米', 'cm', '长度', 0.010000000000, '1 CM = 0.01 M'),
(250, 'LENGTH', 'MM', '毫米', 'mm', '长度', 0.001000000000, '1 MM = 0.001 M'),
(260, 'LENGTH', 'IN', '英寸', 'in', '长度', 0.025400000000, '1 IN = 0.0254 M'),
(270, 'LENGTH', 'FT', '英尺', 'ft', '长度', 0.304800000000, '1 FT = 0.3048 M'),
(310, 'VOLUME', 'M3', '立方米', 'm3', '体积', 1000.000000000000, '1 M3 = 1000 L'),
(320, 'VOLUME', 'L', '升', 'L', '体积', 1.000000000000, '体积基准单位'),
(330, 'VOLUME', 'ML', '毫升', 'ml', '体积', 0.001000000000, '1 ML = 0.001 L'),
(340, 'VOLUME', 'GAL_US', '美制加仑', 'gal', '体积', 3.785411784000, '1 US gal = 3.785411784 L'),
(350, 'VOLUME', 'QT_US', '美制夸脱', 'qt', '体积', 0.946352946000, '1 US qt = 0.946352946 L');

DROP TEMPORARY TABLE IF EXISTS `tmp_unit_seed_target`;
CREATE TEMPORARY TABLE `tmp_unit_seed_target` (
  `sort_no` int NOT NULL,
  `unit_type_code` varchar(50) NOT NULL,
  `unit_code` varchar(64) NOT NULL,
  `base_factor` decimal(30,12) DEFAULT NULL,
  PRIMARY KEY (`unit_code`)
) ENGINE=Memory;

INSERT INTO `tmp_unit_seed_target` (`sort_no`, `unit_type_code`, `unit_code`, `base_factor`)
SELECT `sort_no`, `unit_type_code`, `unit_code`, `base_factor`
FROM `tmp_unit_seed`;

-- 旧版本将常用单位挂在 DEFAULT 类型下。每个租户每个标准编码只保留一条活动记录，其余软删，
-- 避免后端按 unit_code 查询时出现多条，也避免迁移 unit_type_id 时触发唯一索引冲突。
DROP TEMPORARY TABLE IF EXISTS `tmp_keep_unit`;
CREATE TEMPORARY TABLE `tmp_keep_unit` (
  `tenant_id` bigint NOT NULL,
  `unit_code` varchar(64) NOT NULL,
  `keep_unit_id` bigint NOT NULL,
  PRIMARY KEY (`tenant_id`, `unit_code`)
) ENGINE=Memory;

INSERT INTO `tmp_keep_unit` (`tenant_id`, `unit_code`, `keep_unit_id`)
SELECT bu.tenant_id, bu.unit_code, MIN(bu.id) AS keep_unit_id
FROM `basic_unit` bu
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = bu.tenant_id
INNER JOIN `tmp_unit_seed` us ON us.unit_code = bu.unit_code
WHERE bu.deleted = 0
GROUP BY bu.tenant_id, bu.unit_code;

UPDATE `basic_unit` old_unit
INNER JOIN `tmp_keep_unit` keep_unit
        ON keep_unit.tenant_id = old_unit.tenant_id
       AND keep_unit.unit_code = old_unit.unit_code
SET old_unit.deleted = 1,
    old_unit.update_time = NOW(),
    old_unit.update_by = @OPERATOR
WHERE old_unit.deleted = 0
  AND old_unit.id <> keep_unit.keep_unit_id;

-- 将现有同编码单位迁移到正确类型，并补齐显示字段。
UPDATE `basic_unit` bu
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = bu.tenant_id
INNER JOIN `tmp_unit_seed` us ON us.unit_code = bu.unit_code
INNER JOIN `basic_unit_type` bute
        ON bute.tenant_id = st.tenant_id
       AND bute.unit_type_code = us.unit_type_code
       AND bute.deleted = 0
SET bu.unit_type_id = bute.id,
    bu.unit_name = us.unit_name,
    bu.unit_symbol = us.unit_symbol,
    bu.unit_category = us.unit_category,
    bu.conversion_rate = COALESCE(CAST(us.base_factor AS DECIMAL(18,6)), bu.conversion_rate),
    bu.status = 1,
    bu.sort_order = us.sort_no,
    bu.remark = us.remark,
    bu.update_time = NOW(),
    bu.update_by = @OPERATOR
WHERE bu.deleted = 0;

INSERT INTO `basic_unit` (
  `unit_code`, `unit_name`, `unit_symbol`, `unit_category`, `conversion_rate`, `status`,
  `sort_order`, `remark`, `tenant_id`, `unit_type_id`, `create_time`, `create_by`,
  `update_time`, `update_by`, `deleted`
)
SELECT
  us.unit_code,
  us.unit_name,
  us.unit_symbol,
  us.unit_category,
  COALESCE(CAST(us.base_factor AS DECIMAL(18,6)), 1.000000),
  1,
  us.sort_no,
  us.remark,
  st.tenant_id,
  bute.id,
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
INNER JOIN `tmp_unit_seed` us ON 1 = 1
INNER JOIN `basic_unit_type` bute
        ON bute.tenant_id = st.tenant_id
       AND bute.unit_type_code = us.unit_type_code
       AND bute.deleted = 0
WHERE NOT EXISTS (
  SELECT 1
  FROM `basic_unit` bu
  WHERE bu.tenant_id = st.tenant_id
    AND bu.unit_code = us.unit_code
    AND bu.deleted = 0
);

-- 为所有固定比例单位生成有向换算关系。SET/BOX 不参与固定换算。
INSERT INTO `basic_unit_conversion` (
  `id`, `tenant_id`, `unit_id`, `target_unit_id`, `conversion_value`,
  `create_time`, `create_by`, `update_time`, `update_by`, `deleted`
)
SELECT
  202605180300000000 + st.seed_no * 100000000 + src_seed.sort_no * 1000 + target_seed.sort_no AS id,
  st.tenant_id,
  src_unit.id AS unit_id,
  target_unit.id AS target_unit_id,
  CAST(src_seed.base_factor / target_seed.base_factor AS DECIMAL(30,12)) AS conversion_value,
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
INNER JOIN `tmp_unit_seed` src_seed
        ON src_seed.base_factor IS NOT NULL
INNER JOIN `tmp_unit_seed_target` target_seed
        ON target_seed.unit_type_code = src_seed.unit_type_code
       AND target_seed.base_factor IS NOT NULL
       AND target_seed.unit_code <> src_seed.unit_code
INNER JOIN `basic_unit` src_unit
        ON src_unit.tenant_id = st.tenant_id
       AND src_unit.unit_code = src_seed.unit_code
       AND src_unit.deleted = 0
INNER JOIN `basic_unit` target_unit
        ON target_unit.tenant_id = st.tenant_id
       AND target_unit.unit_code = target_seed.unit_code
       AND target_unit.deleted = 0
WHERE NOT EXISTS (
  SELECT 1
  FROM `basic_unit_conversion` buc
  WHERE buc.tenant_id = st.tenant_id
    AND buc.unit_id = src_unit.id
    AND buc.target_unit_id = target_unit.id
    AND buc.deleted = 0
)
AND NOT EXISTS (
  SELECT 1
  FROM `basic_unit_conversion` buc_id
  WHERE buc_id.id = 202605180300000000 + st.seed_no * 100000000 + src_seed.sort_no * 1000 + target_seed.sort_no
);

DROP TEMPORARY TABLE IF EXISTS `tmp_currency_seed`;
CREATE TEMPORARY TABLE `tmp_currency_seed` (
  `currency_code` char(3) NOT NULL,
  `currency_num_code` char(3) DEFAULT NULL,
  `currency_name_cn` varchar(50) NOT NULL,
  `currency_name_en` varchar(100) NOT NULL,
  `currency_symbol` varchar(10) DEFAULT NULL,
  `decimal_digits` tinyint NOT NULL,
  `is_base_currency` tinyint(1) NOT NULL,
  `country_region` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`currency_code`)
) ENGINE=Memory;

INSERT INTO `tmp_currency_seed` (
  `currency_code`, `currency_num_code`, `currency_name_cn`, `currency_name_en`,
  `currency_symbol`, `decimal_digits`, `is_base_currency`, `country_region`
) VALUES
('CNY', '156', '人民币', 'Chinese Yuan', '¥', 2, 1, '中国'),
('USD', '840', '美元', 'US Dollar', '$', 2, 0, '美国'),
('EUR', '978', '欧元', 'Euro', '€', 2, 0, '欧元区'),
('JPY', '392', '日元', 'Japanese Yen', '¥', 0, 0, '日本'),
('KRW', '410', '韩元', 'South Korean Won', '₩', 0, 0, '韩国'),
('GBP', '826', '英镑', 'Pound Sterling', '£', 2, 0, '英国'),
('HKD', '344', '港币', 'Hong Kong Dollar', 'HK$', 2, 0, '中国香港'),
('AUD', '036', '澳元', 'Australian Dollar', 'A$', 2, 0, '澳大利亚'),
('CAD', '124', '加元', 'Canadian Dollar', 'C$', 2, 0, '加拿大'),
('SGD', '702', '新加坡元', 'Singapore Dollar', 'S$', 2, 0, '新加坡'),
('CHF', '756', '瑞士法郎', 'Swiss Franc', 'CHF', 2, 0, '瑞士');

INSERT INTO `mdm_currency` (
  `tenant_id`, `currency_code`, `currency_num_code`, `currency_name_cn`, `currency_name_en`,
  `currency_symbol`, `decimal_digits`, `is_base_currency`, `country_region`, `status`,
  `remark`, `create_time`, `create_by`, `update_time`, `update_by`, `deleted`
)
SELECT
  st.tenant_id,
  cs.currency_code,
  cs.currency_num_code,
  cs.currency_name_cn,
  cs.currency_name_en,
  cs.currency_symbol,
  cs.decimal_digits,
  cs.is_base_currency,
  cs.country_region,
  1,
  '常见币种初始化',
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
INNER JOIN `tmp_currency_seed` cs ON 1 = 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `mdm_currency` mc
  WHERE mc.tenant_id = st.tenant_id
    AND mc.currency_code = cs.currency_code
    AND mc.deleted = 0
);

UPDATE `mdm_currency` mc
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = mc.tenant_id
INNER JOIN `tmp_currency_seed` cs ON cs.currency_code = mc.currency_code
SET mc.currency_num_code = cs.currency_num_code,
    mc.currency_name_cn = cs.currency_name_cn,
    mc.currency_name_en = cs.currency_name_en,
    mc.currency_symbol = cs.currency_symbol,
    mc.decimal_digits = cs.decimal_digits,
    mc.is_base_currency = cs.is_base_currency,
    mc.country_region = cs.country_region,
    mc.status = 1,
    mc.update_time = NOW(),
    mc.update_by = @OPERATOR
WHERE mc.deleted = 0;

INSERT INTO `mdm_exchange_rate_type` (
  `tenant_id`, `rate_type_code`, `rate_type_name`, `business_scene`, `is_default`, `status`,
  `create_time`, `create_by`, `update_time`, `update_by`, `deleted`
)
SELECT
  st.tenant_id,
  'BOOKING',
  '记账汇率',
  '财务总账核算',
  1,
  1,
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
WHERE NOT EXISTS (
  SELECT 1
  FROM `mdm_exchange_rate_type` mert
  WHERE mert.tenant_id = st.tenant_id
    AND mert.rate_type_code = 'BOOKING'
    AND mert.deleted = 0
);

UPDATE `mdm_exchange_rate_type` mert
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = mert.tenant_id
SET mert.rate_type_name = '记账汇率',
    mert.business_scene = '财务总账核算',
    mert.is_default = 1,
    mert.status = 1,
    mert.update_time = NOW(),
    mert.update_by = @OPERATOR
WHERE mert.rate_type_code = 'BOOKING'
  AND mert.deleted = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_exchange_rate_seed`;
CREATE TEMPORARY TABLE `tmp_exchange_rate_seed` (
  `target_currency_code` char(3) NOT NULL,
  `exchange_rate` decimal(20,8) NOT NULL,
  PRIMARY KEY (`target_currency_code`)
) ENGINE=Memory;

INSERT INTO `tmp_exchange_rate_seed` (`target_currency_code`, `exchange_rate`) VALUES
('USD', 0.14683000),
('EUR', 0.12627000),
('JPY', 23.28000000),
('KRW', 220.19000000),
('GBP', 0.10992000),
('HKD', 1.14990000),
('AUD', 0.20538000),
('CAD', 0.20197000),
('SGD', 0.18794000),
('CHF', 0.11546000);

INSERT INTO `mdm_currency_exchange_rate` (
  `tenant_id`, `source_currency_code`, `target_currency_code`, `rate_type_code`,
  `effective_date`, `exchange_rate`, `expire_date`, `approve_status`, `approve_user`,
  `approve_time`, `org_id`, `remark`, `create_time`, `create_by`, `update_time`,
  `update_by`, `deleted`
)
SELECT
  st.tenant_id,
  'CNY',
  ers.target_currency_code,
  'BOOKING',
  @RATE_DATE,
  ers.exchange_rate,
  @FOREVER_DATE,
  1,
  @OPERATOR,
  NOW(),
  NULL,
  'Frankfurter API latest rates, base=CNY, date=2026-05-15',
  NOW(),
  @OPERATOR,
  NOW(),
  @OPERATOR,
  0
FROM `tmp_seed_tenant` st
INNER JOIN `tmp_exchange_rate_seed` ers ON 1 = 1
WHERE NOT EXISTS (
  SELECT 1
  FROM `mdm_currency_exchange_rate` mcer
  WHERE mcer.tenant_id = st.tenant_id
    AND mcer.source_currency_code = 'CNY'
    AND mcer.target_currency_code = ers.target_currency_code
    AND mcer.rate_type_code = 'BOOKING'
    AND mcer.effective_date = @RATE_DATE
    AND mcer.org_id IS NULL
    AND mcer.deleted = 0
);

UPDATE `mdm_currency_exchange_rate` mcer
INNER JOIN `tmp_seed_tenant` st ON st.tenant_id = mcer.tenant_id
INNER JOIN `tmp_exchange_rate_seed` ers ON ers.target_currency_code = mcer.target_currency_code
SET mcer.exchange_rate = ers.exchange_rate,
    mcer.expire_date = @FOREVER_DATE,
    mcer.approve_status = 1,
    mcer.approve_user = COALESCE(mcer.approve_user, @OPERATOR),
    mcer.approve_time = COALESCE(mcer.approve_time, NOW()),
    mcer.remark = 'Frankfurter API latest rates, base=CNY, date=2026-05-15',
    mcer.update_time = NOW(),
    mcer.update_by = @OPERATOR
WHERE mcer.source_currency_code = 'CNY'
  AND mcer.rate_type_code = 'BOOKING'
  AND mcer.effective_date = @RATE_DATE
  AND mcer.org_id IS NULL
  AND mcer.deleted = 0;

SELECT 'basic_unit_type' AS table_name, tenant_id, COUNT(*) AS row_count
FROM `basic_unit_type`
WHERE tenant_id IN (0, @TENANT_ID)
  AND unit_type_code IN ('WEIGHT', 'COUNT', 'LENGTH', 'VOLUME')
  AND deleted = 0
GROUP BY tenant_id
UNION ALL
SELECT 'basic_unit', tenant_id, COUNT(*)
FROM `basic_unit`
WHERE tenant_id IN (0, @TENANT_ID)
  AND unit_code IN (
    'T', 'KG', 'G', 'MG', 'LB', 'OZ', 'PCS', 'PAIR', 'DOZ', 'HUNDRED', 'THOUSAND',
    'SET', 'BOX', 'KM', 'M', 'DM', 'CM', 'MM', 'IN', 'FT', 'M3', 'L', 'ML', 'GAL_US', 'QT_US'
  )
  AND deleted = 0
GROUP BY tenant_id
UNION ALL
SELECT 'basic_unit_conversion', tenant_id, COUNT(*)
FROM `basic_unit_conversion`
WHERE tenant_id IN (0, @TENANT_ID)
  AND deleted = 0
GROUP BY tenant_id
UNION ALL
SELECT 'mdm_currency', tenant_id, COUNT(*)
FROM `mdm_currency`
WHERE tenant_id IN (0, @TENANT_ID)
  AND currency_code IN ('CNY', 'USD', 'EUR', 'JPY', 'KRW', 'GBP', 'HKD', 'AUD', 'CAD', 'SGD', 'CHF')
  AND deleted = 0
GROUP BY tenant_id
UNION ALL
SELECT 'mdm_currency_exchange_rate', tenant_id, COUNT(*)
FROM `mdm_currency_exchange_rate`
WHERE tenant_id IN (0, @TENANT_ID)
  AND source_currency_code = 'CNY'
  AND target_currency_code IN ('USD', 'EUR', 'JPY', 'KRW', 'GBP', 'HKD', 'AUD', 'CAD', 'SGD', 'CHF')
  AND rate_type_code = 'BOOKING'
  AND effective_date = @RATE_DATE
  AND approve_status = 1
  AND deleted = 0
GROUP BY tenant_id;
