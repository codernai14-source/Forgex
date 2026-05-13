-- 物料管理导入与第三方同步升级脚本
-- 适用库：forgex_common / forgex_admin / forgex_integration
-- 说明：脚本可重复执行；用于补齐物料公共导入模板、按钮权限、附属字段结构表和接口平台默认 API 编码。

USE `forgex_admin`;

SET @script_user := '20260512_material_import_sync_upgrade';

CREATE TABLE IF NOT EXISTS `basic_material_extend_schema` (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `module` varchar(50) NOT NULL COMMENT '模块编码',
    `material_type` varchar(50) NOT NULL COMMENT '物料类型',
    `schema_json` json DEFAULT NULL COMMENT '字段结构 JSON',
    `version` int NOT NULL DEFAULT 1 COMMENT '结构版本号',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_material_extend_schema_scope` (`tenant_id`, `module`, `material_type`, `deleted`),
    KEY `idx_material_extend_schema_scope` (`tenant_id`, `module`, `material_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料附属字段结构表';

CREATE TABLE IF NOT EXISTS `basic_material_extend_config` (
    `id` bigint NOT NULL COMMENT '主键 ID',
    `module` varchar(50) NOT NULL COMMENT '模块编码：PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售',
    `material_type` varchar(50) NOT NULL DEFAULT 'RAW_MATERIAL' COMMENT '物料类型',
    `field_name` varchar(100) NOT NULL COMMENT '字段名称',
    `field_label` varchar(100) NOT NULL COMMENT '字段标签',
    `field_type` varchar(50) NOT NULL DEFAULT 'STRING' COMMENT '字段类型',
    `field_options` json DEFAULT NULL COMMENT '字段选项 JSON',
    `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填：0=否，1=是',
    `validation_rule` varchar(500) DEFAULT NULL COMMENT '校验规则',
    `default_value` varchar(500) DEFAULT NULL COMMENT '默认值',
    `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
    `remark` varchar(500) DEFAULT NULL COMMENT '备注',
    `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
    `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
    `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
    `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_material_extend_config_field` (`tenant_id`, `module`, `material_type`, `field_name`, `deleted`),
    KEY `idx_material_extend_config_scope` (`tenant_id`, `module`, `material_type`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料扩展字段配置表';

SET @has_material_type := (
    SELECT COUNT(1)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_material_extend_config'
      AND COLUMN_NAME = 'material_type'
);
SET @ddl := IF(@has_material_type = 0,
    'ALTER TABLE `basic_material_extend_config` ADD COLUMN `material_type` varchar(50) NOT NULL DEFAULT ''RAW_MATERIAL'' COMMENT ''物料类型'' AFTER `module`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE `basic_material_extend_config`
SET material_type = 'RAW_MATERIAL'
WHERE material_type IS NULL OR material_type = '';

SET @has_extend_config_scope_index := (
    SELECT COUNT(1)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'basic_material_extend_config'
      AND INDEX_NAME = 'idx_material_extend_config_scope'
);
SET @ddl := IF(@has_extend_config_scope_index = 0,
    'CREATE INDEX `idx_material_extend_config_scope` ON `basic_material_extend_config` (`tenant_id`, `module`, `material_type`, `status`, `deleted`)',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `basic_material_extend_config` (
    id, module, material_type, field_name, field_label, field_type, field_options,
    required, validation_rule, default_value, order_num, remark, status,
    tenant_id, create_time, update_time, create_by, update_by, deleted
)
SELECT
    CAST(CONCAT(DATE_FORMAT(NOW(3), '%Y%m%d%H%i%s'), LPAD(ROW_NUMBER() OVER (ORDER BY tenant_id, module, material_type, field_name), 5, '0')) AS UNSIGNED),
    module,
    material_type,
    field_name,
    CASE field_name
        WHEN 'min_order_qty' THEN '最小起订量'
        WHEN 'supplier' THEN '默认供应商'
        WHEN 'lead_time_days' THEN '采购提前期(天)'
        WHEN 'purchase_price' THEN '采购价格'
        WHEN 'currency' THEN '币种'
        WHEN 'payment_terms' THEN '付款条件'
        WHEN 'location' THEN '库位'
        WHEN 'warehouse' THEN '仓库'
        WHEN 'safety_stock' THEN '安全库存'
        WHEN 'current_stock' THEN '当前库存'
        WHEN 'unit_weight_kg' THEN '单位重量(kg)'
        WHEN 'shelf_life_days' THEN '保质期(天)'
        WHEN 'bom_version' THEN 'BOM版本'
        WHEN 'process_route' THEN '工艺路线'
        WHEN 'standard_cost' THEN '标准成本'
        WHEN 'cycle_time_min' THEN '节拍(分钟)'
        WHEN 'quality_check_points' THEN '质检点'
        WHEN 'sale_price' THEN '销售价格'
        WHEN 'min_sale_price' THEN '最低售价'
        WHEN 'tax_rate' THEN '税率'
        WHEN 'warranty_months' THEN '质保期(月)'
        ELSE field_name
    END,
    field_type,
    NULL,
    0,
    NULL,
    NULL,
    ROW_NUMBER() OVER (PARTITION BY tenant_id, module, material_type ORDER BY field_name) * 10,
    '由历史物料附属 JSON 自动抽取',
    1,
    tenant_id,
    NOW(),
    NOW(),
    @script_user,
    @script_user,
    0
FROM (
    SELECT
        raw.tenant_id,
        raw.module,
        raw.material_type,
        raw.field_name,
        CASE
            WHEN SUM(CASE WHEN raw.json_type IN ('INTEGER', 'DOUBLE', 'DECIMAL') THEN 0 ELSE 1 END) = 0 THEN 'NUMBER'
            WHEN SUM(CASE WHEN raw.json_type = 'BOOLEAN' THEN 0 ELSE 1 END) = 0 THEN 'BOOLEAN'
            WHEN SUM(CASE WHEN raw.json_type = 'ARRAY' THEN 1 ELSE 0 END) > 0 THEN 'MULTI_SELECT'
            ELSE 'STRING'
        END AS field_type
    FROM (
        SELECT
            e.tenant_id,
            e.module,
            COALESCE(NULLIF(m.material_type, ''), 'RAW_MATERIAL') AS material_type,
            JSON_UNQUOTE(keys_table.field_name) AS field_name,
            JSON_TYPE(JSON_EXTRACT(e.extend_json, CONCAT('$."', REPLACE(JSON_UNQUOTE(keys_table.field_name), '"', '\\"'), '"'))) AS json_type
        FROM `basic_material_extend` e
        JOIN `basic_material` m ON m.id = e.material_id
            AND m.tenant_id = e.tenant_id
            AND m.deleted = 0
        JOIN JSON_TABLE(JSON_KEYS(e.extend_json), '$[*]' COLUMNS (field_name json PATH '$')) keys_table
        WHERE e.deleted = 0
          AND JSON_TYPE(e.extend_json) = 'OBJECT'
    ) raw
    WHERE raw.field_name IS NOT NULL
      AND raw.field_name <> ''
    GROUP BY raw.tenant_id, raw.module, raw.material_type, raw.field_name
) fields
WHERE NOT EXISTS (
    SELECT 1
    FROM `basic_material_extend_config` existing
    WHERE existing.tenant_id = fields.tenant_id
      AND existing.module = fields.module
      AND existing.material_type = fields.material_type
      AND existing.field_name = fields.field_name
      AND existing.deleted = 0
);

INSERT INTO `basic_material_extend_schema` (
    id, tenant_id, module, material_type, schema_json, version, status,
    order_num, remark, create_time, create_by, update_time, update_by, deleted
)
SELECT
    CAST(CONCAT(DATE_FORMAT(NOW(3), '%Y%m%d%H%i%s'), LPAD(ROW_NUMBER() OVER (ORDER BY tenant_id, module, material_type), 5, '0')) AS UNSIGNED),
    tenant_id,
    module,
    material_type,
    JSON_ARRAYAGG(JSON_OBJECT(
        'id', id,
        'module', module,
        'materialType', material_type,
        'fieldName', field_name,
        'fieldLabel', field_label,
        'fieldType', field_type,
        'fieldOptions', field_options,
        'required', required,
        'validationRule', validation_rule,
        'defaultValue', default_value,
        'orderNum', order_num,
        'status', status
    )),
    1,
    1,
    0,
    '由物料扩展字段配置自动初始化',
    NOW(),
    @script_user,
    NOW(),
    @script_user,
    0
FROM `basic_material_extend_config`
WHERE deleted = 0
GROUP BY tenant_id, module, material_type
ON DUPLICATE KEY UPDATE
    schema_json = VALUES(schema_json),
    version = version + 1,
    status = 1,
    update_by = @script_user,
    update_time = NOW();

USE `forgex_common`;

SET @script_user := '20260512_material_import_sync_upgrade';
SET @tenant_id := (
    SELECT COALESCE(
        (SELECT tenant_id FROM `fx_excel_import_config` WHERE table_code = 'basic_material' ORDER BY deleted ASC, id ASC LIMIT 1),
        (SELECT tenant_id FROM `forgex_admin`.`sys_tenant` WHERE deleted = 0 ORDER BY id ASC LIMIT 1),
        0
    )
);

INSERT INTO `fx_excel_import_config` (
    tenant_id, table_name, table_code, handler_bean_name, import_permission,
    title, title_i18n_json, subtitle, subtitle_i18n_json, subtitle_style_json,
    version, create_by, create_time, update_by, update_time, deleted
) VALUES (
    @tenant_id, '物料导入', 'basic_material', 'basicMaterialImportHandler', 'basic:material:import',
    '物料导入模板',
    '{"zh-CN":"物料导入模板","en-US":"Material Import Template","zh-TW":"物料匯入範本","ja-JP":"品目インポートテンプレート","ko-KR":"자재 가져오기 템플릿"}',
    '基础数据-物料管理',
    '{"zh-CN":"基础数据-物料管理","en-US":"Basic Data - Material Management","zh-TW":"基礎資料-物料管理","ja-JP":"基本データ - 品目管理","ko-KR":"기본 데이터 - 자재 관리"}',
    '{"backgroundColor":"#8EC67F","wrapText":true,"fontSize":12}',
    1, @script_user, NOW(), @script_user, NOW(), 0
) ON DUPLICATE KEY UPDATE
    id = LAST_INSERT_ID(id),
    table_name = VALUES(table_name),
    handler_bean_name = VALUES(handler_bean_name),
    import_permission = VALUES(import_permission),
    title = VALUES(title),
    title_i18n_json = VALUES(title_i18n_json),
    subtitle = VALUES(subtitle),
    subtitle_i18n_json = VALUES(subtitle_i18n_json),
    subtitle_style_json = VALUES(subtitle_style_json),
    version = VALUES(version),
    update_by = @script_user,
    update_time = NOW(),
    deleted = 0;
SET @basic_material_config_id := LAST_INSERT_ID();

UPDATE `fx_excel_import_config_item`
SET deleted = 1, update_by = @script_user, update_time = NOW()
WHERE deleted = 0
  AND config_id = @basic_material_config_id;

DELETE FROM `fx_excel_import_config_item`
WHERE create_by = @script_user
  AND config_id = @basic_material_config_id;

INSERT INTO `fx_excel_import_config_item` (
    tenant_id, config_id, sheet_code, sheet_name, i18n_json, import_field, field_type,
    field_remark, dict_code, data_source_type, data_source_value, depends_on_field_key,
    `separator`, required, order_num, create_by, create_time, update_by, update_time, deleted
) VALUES
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"物料编码","en-US":"Material Code","zh-TW":"物料代碼","ja-JP":"品目コード","ko-KR":"자재 코드"}', 'materialCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"物料名称","en-US":"Material Name","zh-TW":"物料名稱","ja-JP":"品目名","ko-KR":"자재명"}', 'materialName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"物料类型","en-US":"Material Type","zh-TW":"物料類型","ja-JP":"品目タイプ","ko-KR":"자재 유형"}', 'materialType', 'string', NULL, NULL, 'JSON', '["RAW_MATERIAL","SEMI_FINISHED","FINISHED_GOODS","OTHER"]', NULL, ',', 1, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"物料分类","en-US":"Material Category","zh-TW":"物料分類","ja-JP":"品目カテゴリ","ko-KR":"자재 분류"}', 'materialCategory', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"规格型号","en-US":"Specification","zh-TW":"規格型號","ja-JP":"仕様","ko-KR":"규격"}', 'specification', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"计量单位","en-US":"Unit","zh-TW":"計量單位","ja-JP":"計量単位","ko-KR":"계량 단위"}', 'unit', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"品牌","en-US":"Brand","zh-TW":"品牌","ja-JP":"ブランド","ko-KR":"브랜드"}', 'brand', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"图片地址","en-US":"Image URL","zh-TW":"圖片地址","ja-JP":"画像URL","ko-KR":"이미지 URL"}', 'imageUrl', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"状态","en-US":"Status","zh-TW":"狀態","ja-JP":"状態","ko-KR":"상태"}', 'status', 'number', NULL, NULL, 'JSON', '["1","0"]', NULL, ',', 0, 9, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"审批状态","en-US":"Approval Status","zh-TW":"審批狀態","ja-JP":"承認状態","ko-KR":"승인 상태"}', 'approvalStatus', 'string', NULL, NULL, 'JSON', '["NO_APPROVAL_REQUIRED","PENDING","APPROVED","REJECTED"]', NULL, ',', 0, 10, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"备注","en-US":"Remark","zh-TW":"備註","ja-JP":"備考","ko-KR":"비고"}', 'remark', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 11, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'main', '主表', '{"zh-CN":"详细描述","en-US":"Description","zh-TW":"詳細描述","ja-JP":"詳細説明","ko-KR":"상세 설명"}', 'description', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 12, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'extend', '附属信息', '{"zh-CN":"物料编码","en-US":"Material Code","zh-TW":"物料代碼","ja-JP":"品目コード","ko-KR":"자재 코드"}', 'materialCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'extend', '附属信息', '{"zh-CN":"所属模块","en-US":"Module","zh-TW":"所屬模組","ja-JP":"モジュール","ko-KR":"모듈"}', 'module', 'string', NULL, NULL, 'JSON', '["PURCHASE","INVENTORY","PRODUCTION","SALES"]', NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_material_config_id, 'extend', '附属信息', '{"zh-CN":"扩展数据JSON","en-US":"Extension JSON","zh-TW":"擴展資料JSON","ja-JP":"拡張JSON","ko-KR":"확장 JSON"}', 'extendJson', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0);

USE `forgex_admin`;

SET @basic_material_menu_id := (
    SELECT id FROM `sys_menu`
    WHERE deleted = 0
      AND (perm_key = 'basic:material:query' OR component_key = 'BasicMaterial' OR path = 'material')
    ORDER BY id ASC
    LIMIT 1
);

INSERT INTO `sys_menu` (
    tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json, icon, component_key,
    perm_key, order_num, visible, status, create_time, create_by, update_time, update_by, deleted, menu_level, menu_mode, external_url
)
SELECT COALESCE(parent_menu.tenant_id, 0), COALESCE(parent_menu.tenant_type, 'PUBLIC'), parent_menu.module_id,
       @basic_material_menu_id, 'button', NULL, item.menu_name, item.name_i18n_json, NULL, item.component_key,
       item.perm_key, item.order_num, 0, 1, NOW(), @script_user, NOW(), @script_user, 0, 3, 'embedded', NULL
FROM (
    SELECT '导入物料' AS menu_name, '{"zh-CN":"导入物料","en-US":"Import Material","zh-TW":"匯入物料","ja-JP":"品目インポート","ko-KR":"자재 가져오기"}' AS name_i18n_json, 'BasicMaterialImport' AS component_key, 'basic:material:import' AS perm_key, 41 AS order_num
    UNION ALL SELECT '从第三方拉取物料', '{"zh-CN":"从第三方拉取物料","en-US":"Pull Material From Third Party","zh-TW":"從第三方拉取物料","ja-JP":"外部から品目取得","ko-KR":"타사에서 자재 가져오기"}', 'BasicMaterialPullThirdParty', 'basic:material:pullThirdParty', 42
    UNION ALL SELECT '同步第三方物料', '{"zh-CN":"同步第三方物料","en-US":"Sync Material To Third Party","zh-TW":"同步第三方物料","ja-JP":"外部へ品目同期","ko-KR":"타사로 자재 동기화"}', 'BasicMaterialSyncThirdParty', 'basic:material:sync', 43
    UNION ALL SELECT '附属字段查询', '{"zh-CN":"附属字段查询","en-US":"Extend Field Query","zh-TW":"附屬欄位查詢","ja-JP":"付属項目照会","ko-KR":"부속 필드 조회"}', 'BasicMaterialExtendConfigQuery', 'basic:material:extendConfig:query', 44
    UNION ALL SELECT '附属字段新增', '{"zh-CN":"附属字段新增","en-US":"Extend Field Add","zh-TW":"附屬欄位新增","ja-JP":"付属項目追加","ko-KR":"부속 필드 추가"}', 'BasicMaterialExtendConfigAdd', 'basic:material:extendConfig:add', 45
    UNION ALL SELECT '附属字段编辑', '{"zh-CN":"附属字段编辑","en-US":"Extend Field Edit","zh-TW":"附屬欄位編輯","ja-JP":"付属項目編集","ko-KR":"부속 필드 편집"}', 'BasicMaterialExtendConfigEdit', 'basic:material:extendConfig:edit', 46
    UNION ALL SELECT '附属字段删除', '{"zh-CN":"附属字段删除","en-US":"Extend Field Delete","zh-TW":"附屬欄位刪除","ja-JP":"付属項目削除","ko-KR":"부속 필드 삭제"}', 'BasicMaterialExtendConfigDelete', 'basic:material:extendConfig:delete', 47
) item
LEFT JOIN `sys_menu` parent_menu ON parent_menu.id = @basic_material_menu_id
WHERE @basic_material_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_menu` existing
      WHERE existing.deleted = 0
        AND existing.perm_key = item.perm_key
  );

INSERT INTO `sys_role_menu` (tenant_id, role_id, menu_id)
SELECT DISTINCT rm_base.tenant_id, rm_base.role_id, m.id
FROM `sys_role_menu` rm_base
JOIN `sys_menu` base_menu ON base_menu.deleted = 0
    AND base_menu.id = rm_base.menu_id
    AND base_menu.tenant_id = rm_base.tenant_id
JOIN `sys_menu` m ON m.deleted = 0
    AND m.tenant_id = rm_base.tenant_id
    AND m.perm_key IN (
      'basic:material:import',
      'basic:material:pullThirdParty',
      'basic:material:sync',
      'basic:material:extendConfig:query',
      'basic:material:extendConfig:add',
      'basic:material:extendConfig:edit',
      'basic:material:extendConfig:delete'
    )
WHERE (
      base_menu.perm_key = 'basic:material:query'
      OR base_menu.component_key = 'BasicMaterial'
      OR base_menu.path = 'material'
  )
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.role_id = rm_base.role_id
        AND rm.menu_id = m.id
        AND rm.tenant_id = rm_base.tenant_id
  );

USE `forgex_integration`;

INSERT INTO `fx_api_config` (
    api_code, api_name, api_desc, direction, api_path, processor_bean, call_method,
    http_method, invoke_mode, content_type, target_url, timeout_ms, retry_count,
    retry_interval_ms, max_concurrent, queue_limit, auth_type, auth_config,
    call_count, status, module_code, tenant_id, create_time, create_by,
    update_time, update_by, deleted
)
SELECT item.api_code, item.api_name, item.api_desc, 'OUTBOUND', NULL, NULL, 'HTTP',
       'POST', 'SYNC', 'application/json', NULL, 30000, 0,
       0, 0, 0, NULL, NULL,
       0, 1, 'basic', @tenant_id, NOW(), @script_user,
       NOW(), @script_user, 0
FROM (
    SELECT 'basic_material_sync' AS api_code, 'Material Sync To Third Party' AS api_name, 'Push Forgex material master data to configured third-party systems.' AS api_desc
    UNION ALL SELECT 'basic_material_pull', 'Pull Materials From Third Party', 'Pull material master data from configured third-party systems and write to Basic.'
) item
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_api_config` existing
    WHERE existing.deleted = 0
      AND existing.api_code = item.api_code
      AND existing.tenant_id = @tenant_id
);

SET @basic_material_sync_config_id := (
    SELECT id FROM `fx_api_config`
    WHERE deleted = 0
      AND api_code = 'basic_material_sync'
      AND tenant_id = @tenant_id
    LIMIT 1
);
SET @basic_material_pull_config_id := (
    SELECT id FROM `fx_api_config`
    WHERE deleted = 0
      AND api_code = 'basic_material_pull'
      AND tenant_id = @tenant_id
    LIMIT 1
);
SET @basic_material_third_system_id := (
    SELECT COALESCE(
        (SELECT id FROM `fx_third_system` WHERE deleted = 0 AND tenant_id = @tenant_id ORDER BY id ASC LIMIT 1),
        (SELECT id FROM `fx_third_system` WHERE deleted = 0 ORDER BY id ASC LIMIT 1)
    )
);
SET @basic_material_sync_target_id := (
    SELECT COALESCE(
        (SELECT id FROM `fx_api_outbound_target` WHERE deleted = 0 AND api_config_id = @basic_material_sync_config_id LIMIT 1),
        (SELECT COALESCE(MAX(id), 0) + 1 FROM `fx_api_outbound_target`)
    )
);
SET @basic_material_pull_target_id := (
    SELECT COALESCE(
        (SELECT id FROM `fx_api_outbound_target` WHERE deleted = 0 AND api_config_id = @basic_material_pull_config_id LIMIT 1),
        (SELECT GREATEST(COALESCE(MAX(id), 0) + 1, @basic_material_sync_target_id + 1) FROM `fx_api_outbound_target`)
    )
);

INSERT INTO `fx_api_outbound_target` (
    id, tenant_id, api_config_id, third_system_id, target_code, target_name, target_url,
    http_method, content_type, invoke_mode, timeout_ms, retry_count, retry_interval_ms,
    order_num, status, remark, create_time, create_by, update_time, update_by, deleted
)
SELECT item.id, @tenant_id, item.api_config_id, ts.id, ts.system_code, ts.system_name, item.target_url,
       'POST', 'application/json', 'SYNC', 30000, 0, 0,
       1, 1, item.remark, NOW(), @script_user, NOW(), @script_user, 0
FROM (
    SELECT @basic_material_sync_target_id AS id, @basic_material_sync_config_id AS api_config_id, '/api/materials/third-party/sync' AS target_url, 'Default target for basic_material_sync. Update target_url if needed.' AS remark
    UNION ALL SELECT @basic_material_pull_target_id, @basic_material_pull_config_id, '/api/materials/third-party/pull', 'Default target for basic_material_pull. Update target_url if needed.'
) item
JOIN `fx_third_system` ts ON ts.id = @basic_material_third_system_id
WHERE item.api_config_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `fx_api_outbound_target` existing
      WHERE existing.deleted = 0
        AND existing.api_config_id = item.api_config_id
        AND existing.third_system_id = ts.id
  );

DELETE FROM `fx_api_param_mapping`
WHERE create_by = @script_user
  AND api_config_id IN (@basic_material_sync_config_id, @basic_material_pull_config_id);

INSERT INTO `fx_api_param_mapping` (
    api_config_id, outbound_target_id, source_field_path, target_field_path,
    transform_rule, default_value, constant_value, target_scope, value_type,
    direction, remark, tenant_id, create_time, create_by, update_time, update_by, deleted
)
SELECT item.api_config_id, item.outbound_target_id, item.source_field_path, item.target_field_path,
       NULL, NULL, NULL, 'BODY', 'SOURCE',
       item.direction, item.remark, @tenant_id, NOW(), @script_user, NOW(), @script_user, 0
FROM (
    SELECT @basic_material_sync_config_id AS api_config_id, @basic_material_sync_target_id AS outbound_target_id,
           'tenantId' AS source_field_path, 'tenantId' AS target_field_path, 'OUTBOUND' AS direction, 'Map tenant id' AS remark
    UNION ALL SELECT @basic_material_sync_config_id, @basic_material_sync_target_id, 'materials', 'materials', 'OUTBOUND', 'Map material list'
    UNION ALL SELECT @basic_material_pull_config_id, @basic_material_pull_target_id, 'tenantId', 'tenantId', 'OUTBOUND', 'Map tenant id'
    UNION ALL SELECT @basic_material_pull_config_id, @basic_material_pull_target_id, 'materials', 'materials', 'INBOUND', 'Map material list'
) item
WHERE item.api_config_id IS NOT NULL
  AND item.outbound_target_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `fx_api_param_mapping` existing
      WHERE existing.deleted = 0
        AND existing.api_config_id = item.api_config_id
        AND ((existing.outbound_target_id = item.outbound_target_id) OR (existing.outbound_target_id IS NULL AND item.outbound_target_id IS NULL))
        AND existing.source_field_path = item.source_field_path
        AND existing.target_field_path = item.target_field_path
        AND existing.direction = item.direction
  );
