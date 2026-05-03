-- 计量单位与公共表格升级脚本
-- 说明：
-- 1. 直接替换单表版 basic_unit 能力，新增 unit_type / unit / conversion 三张表
-- 2. 补齐 BasicUnitTable 公共表格配置
-- 3. 补齐计量单位页面菜单、按钮权限和管理员授权
-- 4. 补齐计量单位页面文案与后端返回消息的五语言国际化消息

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @SCRIPT_USER := '20260502_basic_unit_and_table_upgrade';
SET @TENANT_ID := 0;

CREATE TABLE IF NOT EXISTS `basic_unit_type` (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `unit_type_code` varchar(50) NOT NULL COMMENT '计量单位类型编码',
    `unit_type_name` varchar(100) NOT NULL COMMENT '计量单位类型',
    `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父 ID',
    `level_path` varchar(500) DEFAULT NULL COMMENT '层级路径',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_basic_unit_type_tenant_code_deleted` (`tenant_id`, `unit_type_code`, `deleted`),
    KEY `idx_basic_unit_type_parent` (`tenant_id`, `parent_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位类型表';

CREATE TABLE IF NOT EXISTS `basic_unit` (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `unit_type_id` bigint NOT NULL COMMENT '计量单位类型 ID',
    `unit_code` varchar(50) NOT NULL COMMENT '计量单位编码',
    `unit_name` varchar(100) NOT NULL COMMENT '计量单位名称',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_basic_unit_tenant_type_code_deleted` (`tenant_id`, `unit_type_id`, `unit_code`, `deleted`),
    KEY `idx_basic_unit_type` (`tenant_id`, `unit_type_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位主数据表';

CREATE TABLE IF NOT EXISTS `basic_unit_conversion` (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `unit_id` bigint NOT NULL COMMENT '源计量单位 ID',
    `target_unit_id` bigint NOT NULL COMMENT '目标计量单位 ID',
    `conversion_value` decimal(30,12) NOT NULL COMMENT '转换后数值（1:x）',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_basic_unit_conversion_tenant_pair_deleted` (`tenant_id`, `unit_id`, `target_unit_id`, `deleted`),
    KEY `idx_basic_unit_conversion_unit` (`tenant_id`, `unit_id`, `deleted`),
    KEY `idx_basic_unit_conversion_target` (`tenant_id`, `target_unit_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='计量单位换算关系表';

-- 兼容已经执行过旧版 basic_unit 单表脚本的环境。
-- CREATE TABLE IF NOT EXISTS 不会改造既有表，因此这里显式补齐新三表模型依赖字段。
SET @BASIC_UNIT_TYPE_COL_EXISTS := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_unit'
      AND COLUMN_NAME = 'unit_type_id'
);
SET @BASIC_UNIT_TYPE_COL_SQL := IF(
    @BASIC_UNIT_TYPE_COL_EXISTS = 0,
    'ALTER TABLE `basic_unit` ADD COLUMN `unit_type_id` bigint NULL COMMENT ''计量单位类型 ID'' AFTER `tenant_id`',
    'SELECT 1'
);
PREPARE stmt FROM @BASIC_UNIT_TYPE_COL_SQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TEMPORARY TABLE IF EXISTS `tmp_basic_unit_missing_type_tenant_20260502`;
CREATE TEMPORARY TABLE `tmp_basic_unit_missing_type_tenant_20260502` (
    `seq` bigint NOT NULL AUTO_INCREMENT,
    `tenant_id` bigint NOT NULL,
    PRIMARY KEY (`seq`),
    UNIQUE KEY `uk_tmp_basic_unit_missing_type_tenant` (`tenant_id`)
) ENGINE=Memory DEFAULT CHARSET=utf8mb4;

INSERT INTO `tmp_basic_unit_missing_type_tenant_20260502` (`tenant_id`)
SELECT DISTINCT u.tenant_id
FROM `basic_unit` u
WHERE u.unit_type_id IS NULL OR u.unit_type_id = 0;

SET @BASIC_UNIT_TYPE_ID_BASE := GREATEST(
    UUID_SHORT(),
    COALESCE((SELECT MAX(t.id) FROM `basic_unit_type` t), 0) + 1000
);

INSERT INTO `basic_unit_type` (
    id, tenant_id, unit_type_code, unit_type_name, parent_id, level_path,
    create_by, create_time, update_by, update_time, deleted
)
SELECT
    @BASIC_UNIT_TYPE_ID_BASE + tmp.seq,
    tmp.tenant_id,
    'DEFAULT',
    '默认类型',
    0,
    CONCAT('0/', @BASIC_UNIT_TYPE_ID_BASE + tmp.seq),
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    0
FROM `tmp_basic_unit_missing_type_tenant_20260502` tmp
WHERE NOT EXISTS (
    SELECT 1
    FROM `basic_unit_type` t
    WHERE t.tenant_id = tmp.tenant_id
      AND t.unit_type_code = 'DEFAULT'
      AND t.deleted = 0
);

UPDATE `basic_unit_type`
SET parent_id = 0,
    level_path = CONCAT('0/', id),
    update_by = @SCRIPT_USER,
    update_time = NOW()
WHERE unit_type_code = 'DEFAULT'
  AND deleted = 0
  AND (level_path IS NULL OR level_path = '' OR parent_id IS NULL OR parent_id <> 0);

UPDATE `basic_unit` u
JOIN `basic_unit_type` t
  ON t.tenant_id = u.tenant_id
 AND t.unit_type_code = 'DEFAULT'
 AND t.deleted = 0
SET u.unit_type_id = t.id,
    u.update_by = @SCRIPT_USER,
    u.update_time = NOW()
WHERE u.unit_type_id IS NULL OR u.unit_type_id = 0;

ALTER TABLE `basic_unit`
    MODIFY COLUMN `unit_type_id` bigint NOT NULL COMMENT '计量单位类型 ID';

SET @BASIC_UNIT_TYPE_INDEX_EXISTS := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_unit'
      AND INDEX_NAME = 'uk_basic_unit_tenant_type_code_deleted'
);
SET @BASIC_UNIT_TYPE_INDEX_SQL := IF(
    @BASIC_UNIT_TYPE_INDEX_EXISTS = 0,
    'ALTER TABLE `basic_unit` ADD UNIQUE KEY `uk_basic_unit_tenant_type_code_deleted` (`tenant_id`, `unit_type_id`, `unit_code`, `deleted`)',
    'SELECT 1'
);
PREPARE stmt FROM @BASIC_UNIT_TYPE_INDEX_SQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @BASIC_UNIT_QUERY_INDEX_EXISTS := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_unit'
      AND INDEX_NAME = 'idx_basic_unit_type'
);
SET @BASIC_UNIT_QUERY_INDEX_SQL := IF(
    @BASIC_UNIT_QUERY_INDEX_EXISTS = 0,
    'ALTER TABLE `basic_unit` ADD KEY `idx_basic_unit_type` (`tenant_id`, `unit_type_id`, `deleted`)',
    'SELECT 1'
);
PREPARE stmt FROM @BASIC_UNIT_QUERY_INDEX_SQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @BASIC_UNIT_OLD_INDEX_EXISTS := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_unit'
      AND INDEX_NAME = 'uk_basic_unit_tenant_code'
);
SET @BASIC_UNIT_OLD_INDEX_SQL := IF(
    @BASIC_UNIT_OLD_INDEX_EXISTS > 0,
    'ALTER TABLE `basic_unit` DROP INDEX `uk_basic_unit_tenant_code`',
    'SELECT 1'
);
PREPARE stmt FROM @BASIC_UNIT_OLD_INDEX_SQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

DROP TEMPORARY TABLE IF EXISTS `tmp_basic_unit_missing_type_tenant_20260502`;

USE `forgex_common`;

INSERT INTO `fx_table_config` (
    tenant_id, table_code, table_name_i18n_json, table_type, row_key,
    default_page_size, enabled, version, create_by, create_time, update_by, update_time, deleted
) SELECT
    @TENANT_ID,
    'BasicUnitTable',
    '{"zh-CN":"计量单位主数据","en-US":"Unit Master Data","zh-TW":"計量單位主資料","ja-JP":"計量単位マスタ","ko-KR":"계량 단위 마스터"}',
    'NORMAL',
    'id',
    10,
    1,
    1,
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    0
WHERE NOT EXISTS (
    SELECT 1 FROM fx_table_config
    WHERE tenant_id = @TENANT_ID AND table_code = 'BasicUnitTable' AND deleted = 0
);

SET @BASIC_UNIT_TABLE_ID := (
    SELECT id FROM fx_table_config
    WHERE tenant_id = @TENANT_ID AND table_code = 'BasicUnitTable' AND deleted = 0
    ORDER BY id ASC LIMIT 1
);

DELETE FROM fx_table_column_config
WHERE tenant_id = @TENANT_ID AND table_code = 'BasicUnitTable' AND deleted = 0;

INSERT INTO `fx_table_column_config` (
    tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis,
    sortable, queryable, query_type, query_operator, dict_code, render_type, perm_key,
    order_num, enabled, create_by, create_time, update_by, update_time, deleted
) VALUES
(@TENANT_ID, 'BasicUnitTable', 'unitCode', '{"zh-CN":"单位编码","en-US":"Unit Code","zh-TW":"單位編碼","ja-JP":"単位コード","ko-KR":"단위 코드"}', 'left', 140, NULL, 1, 1, 1, 'input', 'like', NULL, 'text', NULL, 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'BasicUnitTable', 'unitName', '{"zh-CN":"单位名称","en-US":"Unit Name","zh-TW":"單位名稱","ja-JP":"単位名","ko-KR":"단위명"}', 'left', 160, NULL, 1, 1, 1, 'input', 'like', NULL, 'text', NULL, 2, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'BasicUnitTable', 'unitTypeName', '{"zh-CN":"类型名称","en-US":"Type Name","zh-TW":"類型名稱","ja-JP":"タイプ名","ko-KR":"유형명"}', 'left', 160, NULL, 1, 1, 0, NULL, NULL, NULL, 'text', NULL, 3, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'BasicUnitTable', 'remark', '{"zh-CN":"备注","en-US":"Remark","zh-TW":"備註","ja-JP":"備考","ko-KR":"비고"}', 'left', 220, NULL, 1, 1, 0, NULL, NULL, NULL, 'text', NULL, 4, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'BasicUnitTable', 'createTime', '{"zh-CN":"创建时间","en-US":"Create Time","zh-TW":"建立時間","ja-JP":"作成時間","ko-KR":"생성 시간"}', 'center', 180, NULL, 1, 0, 0, NULL, NULL, NULL, 'text', NULL, 5, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'BasicUnitTable', 'action', '{"zh-CN":"操作","en-US":"Action","zh-TW":"操作","ja-JP":"操作","ko-KR":"작업"}', 'center', 220, 'right', 0, 0, 0, NULL, NULL, NULL, 'text', NULL, 99, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0);

DELETE FROM fx_i18n_message
WHERE tenant_id = @TENANT_ID AND module = 'basic' AND prompt_code LIKE 'basic.unit.%' AND deleted = 0;

INSERT INTO fx_i18n_message (
    tenant_id, module, prompt_code, text_i18n_json, enabled, version, create_by, create_time, update_by, update_time, deleted
) VALUES
(@TENANT_ID, 'basic', 'basic.unit.title', '{"zh-CN":"计量单位","en-US":"Units of Measure","zh-TW":"計量單位","ja-JP":"計量単位","ko-KR":"계량 단위"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.typeTree', '{"zh-CN":"单位类型树","en-US":"Unit Type Tree","zh-TW":"單位類型樹","ja-JP":"単位タイプツリー","ko-KR":"단위 유형 트리"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.typeTreeSub', '{"zh-CN":"按类型维护单位主数据","en-US":"Maintain unit master data by type","zh-TW":"按類型維護單位主資料","ja-JP":"タイプ別に単位マスタを管理","ko-KR":"유형별 단위 마스터 데이터 관리"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.selectTypeHint', '{"zh-CN":"请选择左侧计量单位类型","en-US":"Select a unit type on the left","zh-TW":"請選擇左側計量單位類型","ja-JP":"左側の計量単位タイプを選択してください","ko-KR":"왼쪽에서 계량 단위 유형을 선택하세요"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.addUnit', '{"zh-CN":"新增单位","en-US":"Add Unit","zh-TW":"新增單位","ja-JP":"単位追加","ko-KR":"단위 추가"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.conversionConfig', '{"zh-CN":"转换配置","en-US":"Conversion Config","zh-TW":"轉換配置","ja-JP":"換算設定","ko-KR":"환산 설정"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.requiredHint', '{"zh-CN":"请完整填写必填项","en-US":"Please complete all required fields","zh-TW":"請完整填寫必填項","ja-JP":"必須項目をすべて入力してください","ko-KR":"필수 항목을 모두 입력하세요"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0),
(@TENANT_ID, 'basic', 'basic.unit.conversionRequiredHint', '{"zh-CN":"请选择目标单位并填写大于 0 的转换数值","en-US":"Select a target unit and enter a value greater than 0","zh-TW":"請選擇目標單位並填寫大於 0 的轉換數值","ja-JP":"目標単位を選択し、0 より大きい換算値を入力してください","ko-KR":"대상 단위를 선택하고 0보다 큰 환산 값을 입력하세요"}', 1, 1, @SCRIPT_USER, NOW(), @SCRIPT_USER, NOW(), 0);

DROP TEMPORARY TABLE IF EXISTS `tmp_basic_unit_prompt_i18n_20260502`;
CREATE TEMPORARY TABLE `tmp_basic_unit_prompt_i18n_20260502` (
    `prompt_code` varchar(100) NOT NULL PRIMARY KEY,
    `zh_cn` varchar(500) NOT NULL,
    `en_us` varchar(500) NOT NULL,
    `zh_tw` varchar(500) NOT NULL,
    `ja_jp` varchar(500) NOT NULL,
    `ko_kr` varchar(500) NOT NULL
) ENGINE=Memory DEFAULT CHARSET=utf8mb4;

INSERT INTO `tmp_basic_unit_prompt_i18n_20260502` (`prompt_code`, `zh_cn`, `en_us`, `zh_tw`, `ja_jp`, `ko_kr`) VALUES
('UNIT_TYPE_CODE_EXISTS', '计量单位类型编码已存在', 'Unit type code already exists', '計量單位類型編碼已存在', '計量単位タイプコードは既に存在します', '계량 단위 유형 코드가 이미 존재합니다'),
('UNIT_TYPE_NOT_FOUND', '计量单位类型不存在', 'Unit type does not exist', '計量單位類型不存在', '計量単位タイプが存在しません', '계량 단위 유형이 존재하지 않습니다'),
('UNIT_TYPE_HAS_CHILDREN', '计量单位类型下存在子类型，禁止删除', 'Child unit types exist under this unit type, deletion is not allowed', '計量單位類型下存在子類型，禁止刪除', 'この計量単位タイプには子タイプがあるため削除できません', '이 계량 단위 유형에 하위 유형이 있어 삭제할 수 없습니다'),
('UNIT_TYPE_HAS_UNITS', '计量单位类型下存在计量单位，禁止删除', 'Units exist under this unit type, deletion is not allowed', '計量單位類型下存在計量單位，禁止刪除', 'この計量単位タイプには単位があるため削除できません', '이 계량 단위 유형에 단위가 있어 삭제할 수 없습니다'),
('UNIT_CODE_EXISTS', '同类型下计量单位编码已存在', 'Unit code already exists under the same type', '同類型下計量單位編碼已存在', '同じタイプ内に計量単位コードが既に存在します', '동일 유형에 계량 단위 코드가 이미 존재합니다'),
('UNIT_NOT_FOUND', '计量单位不存在', 'Unit does not exist', '計量單位不存在', '計量単位が存在しません', '계량 단위가 존재하지 않습니다'),
('UNIT_HAS_CONVERSION', '计量单位存在换算关系，禁止删除', 'Unit conversion relations exist, deletion is not allowed', '計量單位存在換算關係，禁止刪除', '計量単位に換算関係があるため削除できません', '계량 단위에 환산 관계가 있어 삭제할 수 없습니다'),
('UNIT_CONVERSION_TARGET_INVALID', '目标计量单位必须与源单位属于同一类型', 'Target unit must belong to the same type as the source unit', '目標計量單位必須與來源單位屬於同一類型', '対象計量単位は元単位と同じタイプである必要があります', '대상 계량 단위는 원본 단위와 같은 유형이어야 합니다'),
('UNIT_CONVERSION_RELATION_EXISTS', '计量单位换算关系已存在', 'Unit conversion relation already exists', '計量單位換算關係已存在', '計量単位の換算関係は既に存在します', '계량 단위 환산 관계가 이미 존재합니다'),
('UNIT_CONVERSION_RELATION_NOT_FOUND', '计量单位换算关系不存在', 'Unit conversion relation does not exist', '計量單位換算關係不存在', '計量単位の換算関係が存在しません', '계량 단위 환산 관계가 존재하지 않습니다'),
('UNIT_CONVERT_PARAM_INVALID', '计量单位换算参数不完整', 'Unit conversion parameters are incomplete', '計量單位換算參數不完整', '計量単位換算パラメータが不完全です', '계량 단위 환산 매개변수가 완전하지 않습니다'),
('UNIT_CONVERT_FIELD_NOT_FOUND', '计量单位换算字段不存在：{0}', 'Unit conversion field does not exist: {0}', '計量單位換算欄位不存在：{0}', '計量単位換算フィールドが存在しません：{0}', '계량 단위 환산 필드가 존재하지 않습니다: {0}'),
('UNIT_CONVERT_FIELD_NOT_NUMBER', '计量单位换算字段不是数字：{0}', 'Unit conversion field is not numeric: {0}', '計量單位換算欄位不是數字：{0}', '計量単位換算フィールドは数値ではありません：{0}', '계량 단위 환산 필드가 숫자가 아닙니다: {0}');

INSERT INTO `fx_i18n_message` (
    tenant_id, module, prompt_code, text_i18n_json, enabled, version, create_by, create_time, update_by, update_time, deleted
)
SELECT
    @TENANT_ID,
    'basic',
    seed.prompt_code,
    JSON_OBJECT('zh-CN', seed.zh_cn, 'en-US', seed.en_us, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
    1,
    1,
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    0
FROM `tmp_basic_unit_prompt_i18n_20260502` seed
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_i18n_message` msg
    WHERE msg.tenant_id = @TENANT_ID
      AND msg.module = 'basic'
      AND msg.prompt_code = seed.prompt_code
      AND msg.deleted = 0
);

UPDATE `fx_i18n_message` msg
JOIN `tmp_basic_unit_prompt_i18n_20260502` seed
  ON seed.prompt_code = msg.prompt_code
SET msg.text_i18n_json = JSON_OBJECT('zh-CN', seed.zh_cn, 'en-US', seed.en_us, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
    msg.enabled = 1,
    msg.update_by = @SCRIPT_USER,
    msg.update_time = NOW()
WHERE msg.tenant_id = @TENANT_ID
  AND msg.module = 'basic'
  AND msg.deleted = 0;

DROP TEMPORARY TABLE IF EXISTS `tmp_basic_unit_prompt_i18n_20260502`;

USE `forgex_admin`;

SET @ADMIN_TENANT_ID := COALESCE(
    (SELECT id FROM `sys_tenant` WHERE tenant_code = 'default' AND deleted = 0 ORDER BY id LIMIT 1),
    (SELECT tenant_id FROM `sys_role` WHERE role_key = 'admin' AND deleted = 0 ORDER BY id LIMIT 1),
    (SELECT tenant_id FROM `sys_module` WHERE code = 'basic' AND deleted = 0 ORDER BY id LIMIT 1),
    1
);

INSERT INTO `sys_module` (
    tenant_id, code, name, name_i18n_json, icon, order_num, visible, status,
    create_time, create_by, update_time, update_by, deleted
)
SELECT
    @ADMIN_TENANT_ID,
    'basic',
    '基础信息',
    JSON_OBJECT('zh-CN', '基础信息', 'en-US', 'Basic Information', 'zh-TW', '基礎資訊', 'ja-JP', '基本情報', 'ko-KR', '기본 정보'),
    'DatabaseOutlined',
    40,
    1,
    1,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_module`
    WHERE tenant_id = @ADMIN_TENANT_ID AND code = 'basic' AND deleted = 0
);

UPDATE `sys_module`
SET name = '基础信息',
    name_i18n_json = JSON_OBJECT('zh-CN', '基础信息', 'en-US', 'Basic Information', 'zh-TW', '基礎資訊', 'ja-JP', '基本情報', 'ko-KR', '기본 정보'),
    icon = COALESCE(NULLIF(icon, ''), 'DatabaseOutlined'),
    order_num = 40,
    visible = 1,
    status = 1,
    update_time = NOW(),
    update_by = @SCRIPT_USER
WHERE tenant_id = @ADMIN_TENANT_ID
  AND code = 'basic'
  AND deleted = 0;

SET @BASIC_MODULE_ID := (
    SELECT id FROM `sys_module`
    WHERE tenant_id = @ADMIN_TENANT_ID AND code = 'basic' AND deleted = 0
    ORDER BY id LIMIT 1
);

INSERT INTO `sys_menu` (
    tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
    icon, component_key, perm_key, order_num, visible, status,
    create_time, create_by, update_time, update_by, deleted, menu_level, menu_mode
)
SELECT
    @ADMIN_TENANT_ID,
    'PUBLIC',
    @BASIC_MODULE_ID,
    0,
    'menu',
    'unit',
    '计量单位',
    JSON_OBJECT('zh-CN', '计量单位', 'en-US', 'Units of Measure', 'zh-TW', '計量單位', 'ja-JP', '計量単位', 'ko-KR', '계량 단위'),
    'ColumnWidthOutlined',
    'BasicUnit',
    'basic:unit:query',
    50,
    1,
    1,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    0,
    1,
    'embedded'
WHERE @BASIC_MODULE_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_menu`
      WHERE tenant_id = @ADMIN_TENANT_ID
        AND module_id = @BASIC_MODULE_ID
        AND deleted = 0
        AND (path = 'unit' OR component_key = 'BasicUnit' OR perm_key = 'basic:unit:query')
  );

SET @UNIT_MENU_ID := (
    SELECT id FROM `sys_menu`
    WHERE tenant_id = @ADMIN_TENANT_ID
      AND module_id = @BASIC_MODULE_ID
      AND deleted = 0
      AND (path = 'unit' OR component_key = 'BasicUnit' OR perm_key = 'basic:unit:query')
    ORDER BY id LIMIT 1
);

UPDATE `sys_menu`
SET tenant_type = 'PUBLIC',
    module_id = @BASIC_MODULE_ID,
    parent_id = 0,
    type = 'menu',
    path = 'unit',
    name = '计量单位',
    name_i18n_json = JSON_OBJECT('zh-CN', '计量单位', 'en-US', 'Units of Measure', 'zh-TW', '計量單位', 'ja-JP', '計量単位', 'ko-KR', '계량 단위'),
    icon = 'ColumnWidthOutlined',
    component_key = 'BasicUnit',
    perm_key = 'basic:unit:query',
    order_num = 50,
    visible = 1,
    status = 1,
    deleted = 0,
    menu_level = 1,
    menu_mode = 'embedded',
    update_time = NOW(),
    update_by = @SCRIPT_USER
WHERE id = @UNIT_MENU_ID;

INSERT INTO `sys_menu` (
    tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json,
    icon, component_key, perm_key, order_num, visible, status,
    create_time, create_by, update_time, update_by, deleted, menu_level, menu_mode
)
SELECT
    @ADMIN_TENANT_ID,
    'PUBLIC',
    @BASIC_MODULE_ID,
    @UNIT_MENU_ID,
    'button',
    NULL,
    seed.name,
    JSON_OBJECT('zh-CN', seed.name, 'en-US', seed.en_name, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
    NULL,
    NULL,
    seed.perm_key,
    seed.order_num,
    1,
    1,
    NOW(),
    @SCRIPT_USER,
    NOW(),
    @SCRIPT_USER,
    0,
    2,
    'embedded'
FROM (
    SELECT '查询' AS name, 'Query' AS en_name, '查詢' AS zh_tw, '検索' AS ja_jp, '조회' AS ko_kr, 'basic:unit:query' AS perm_key, 1 AS order_num
    UNION ALL SELECT '新增', 'Add', '新增', '追加', '추가', 'basic:unit:add', 2
    UNION ALL SELECT '编辑', 'Edit', '編輯', '編集', '편집', 'basic:unit:edit', 3
    UNION ALL SELECT '删除', 'Delete', '刪除', '削除', '삭제', 'basic:unit:delete', 4
) seed
WHERE @UNIT_MENU_ID IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_menu` m
      WHERE m.tenant_id = @ADMIN_TENANT_ID
        AND m.parent_id = @UNIT_MENU_ID
        AND m.perm_key = seed.perm_key
        AND m.deleted = 0
  );

UPDATE `sys_menu` m
JOIN (
    SELECT '查询' AS name, 'Query' AS en_name, '查詢' AS zh_tw, '検索' AS ja_jp, '조회' AS ko_kr, 'basic:unit:query' AS perm_key, 1 AS order_num
    UNION ALL SELECT '新增', 'Add', '新增', '追加', '추가', 'basic:unit:add', 2
    UNION ALL SELECT '编辑', 'Edit', '編輯', '編集', '편집', 'basic:unit:edit', 3
    UNION ALL SELECT '删除', 'Delete', '刪除', '削除', '삭제', 'basic:unit:delete', 4
) seed ON seed.perm_key = m.perm_key
SET m.tenant_type = 'PUBLIC',
    m.module_id = @BASIC_MODULE_ID,
    m.parent_id = @UNIT_MENU_ID,
    m.type = 'button',
    m.path = NULL,
    m.name = seed.name,
    m.name_i18n_json = JSON_OBJECT('zh-CN', seed.name, 'en-US', seed.en_name, 'zh-TW', seed.zh_tw, 'ja-JP', seed.ja_jp, 'ko-KR', seed.ko_kr),
    m.icon = NULL,
    m.component_key = NULL,
    m.order_num = seed.order_num,
    m.visible = 1,
    m.status = 1,
    m.deleted = 0,
    m.menu_level = 2,
    m.menu_mode = 'embedded',
    m.update_time = NOW(),
    m.update_by = @SCRIPT_USER
WHERE m.tenant_id = @ADMIN_TENANT_ID
  AND m.parent_id = @UNIT_MENU_ID
  AND m.deleted = 0;

SET @ADMIN_ROLE_ID := (
    SELECT id FROM `sys_role`
    WHERE tenant_id = @ADMIN_TENANT_ID AND role_key = 'admin' AND deleted = 0
    ORDER BY id LIMIT 1
);

INSERT INTO `sys_role_menu` (`tenant_id`, `role_id`, `menu_id`)
SELECT @ADMIN_TENANT_ID, @ADMIN_ROLE_ID, m.id
FROM `sys_menu` m
WHERE @ADMIN_ROLE_ID IS NOT NULL
  AND m.tenant_id = @ADMIN_TENANT_ID
  AND m.deleted = 0
  AND (m.id = @UNIT_MENU_ID OR m.parent_id = @UNIT_MENU_ID)
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.tenant_id = @ADMIN_TENANT_ID
        AND rm.role_id = @ADMIN_ROLE_ID
        AND rm.menu_id = m.id
  );

SELECT 'basic_unit_menu' AS item, id, path, component_key, perm_key, visible, status
FROM `sys_menu`
WHERE tenant_id = @ADMIN_TENANT_ID
  AND id = @UNIT_MENU_ID;

SELECT 'basic_unit_permissions' AS item, COUNT(*) AS total
FROM `sys_menu`
WHERE tenant_id = @ADMIN_TENANT_ID
  AND parent_id = @UNIT_MENU_ID
  AND perm_key IN ('basic:unit:query', 'basic:unit:add', 'basic:unit:edit', 'basic:unit:delete')
  AND deleted = 0;
