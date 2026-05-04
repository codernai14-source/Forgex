-- 公共导入全新升级脚本
-- 适用库：forgex_common
-- 说明：脚本可重复执行；目标 tableCode 的导入字段会重建为本脚本定义的有效配置。

USE `forgex_common`;

SET @script_user := '20260501_common_import_upgrade';
SET @tenant_id := (
    SELECT COALESCE(
        (SELECT tenant_id FROM `fx_excel_import_config` WHERE table_code = 'sys_user' ORDER BY deleted ASC, id ASC LIMIT 1),
        (SELECT tenant_id FROM `forgex_admin`.`sys_tenant` WHERE deleted = 0 ORDER BY id ASC LIMIT 1),
        0
    )
);

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'fx_excel_import_config'
      AND column_name = 'handler_bean_name'
);
SET @ddl := IF(@column_exists = 0,
    'ALTER TABLE `fx_excel_import_config` ADD COLUMN `handler_bean_name` varchar(128) DEFAULT NULL COMMENT ''导入处理器 Bean 名称'' AFTER `table_code`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'fx_excel_import_config'
      AND column_name = 'import_permission'
);
SET @ddl := IF(@column_exists = 0,
    'ALTER TABLE `fx_excel_import_config` ADD COLUMN `import_permission` varchar(128) DEFAULT NULL COMMENT ''导入权限码'' AFTER `handler_bean_name`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'fx_excel_import_config_item'
      AND column_name = 'sheet_code'
);
SET @ddl := IF(@column_exists = 0,
    'ALTER TABLE `fx_excel_import_config_item` ADD COLUMN `sheet_code` varchar(64) DEFAULT NULL COMMENT ''Sheet 编码'' AFTER `config_id`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @column_exists := (
    SELECT COUNT(1)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'fx_excel_import_config_item'
      AND column_name = 'sheet_name'
);
SET @ddl := IF(@column_exists = 0,
    'ALTER TABLE `fx_excel_import_config_item` ADD COLUMN `sheet_name` varchar(128) DEFAULT NULL COMMENT ''Sheet 名称'' AFTER `sheet_code`',
    'SELECT 1'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

INSERT INTO `fx_excel_import_config` (
    tenant_id, table_name, table_code, handler_bean_name, import_permission,
    title, title_i18n_json, subtitle, subtitle_i18n_json, subtitle_style_json,
    version, create_by, create_time, update_by, update_time, deleted
) VALUES (
    @tenant_id, '用户导入', 'sys_user', 'sysUserImportHandler', 'sys:user:import',
    '用户导入模板',
    '{"zh-CN":"用户导入模板","en-US":"User Import Template","zh-TW":"使用者匯入範本","ja-JP":"ユーザーインポートテンプレート","ko-KR":"사용자 가져오기 템플릿"}',
    '系统管理-用户管理',
    '{"zh-CN":"系统管理-用户管理","en-US":"System Management - User Management","zh-TW":"系統管理-使用者管理","ja-JP":"システム管理 - ユーザー管理","ko-KR":"시스템 관리 - 사용자 관리"}',
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
SET @sys_user_config_id := LAST_INSERT_ID();

INSERT INTO `fx_excel_import_config` (
    tenant_id, table_name, table_code, handler_bean_name, import_permission,
    title, title_i18n_json, subtitle, subtitle_i18n_json, subtitle_style_json,
    version, create_by, create_time, update_by, update_time, deleted
) VALUES (
    @tenant_id, '供应商导入', 'basic_supplier', 'basicSupplierImportHandler', 'basic:supplier:import',
    '供应商导入模板',
    '{"zh-CN":"供应商导入模板","en-US":"Supplier Import Template","zh-TW":"供應商匯入範本","ja-JP":"サプライヤーインポートテンプレート","ko-KR":"공급업체 가져오기 템플릿"}',
    '基础数据-供应商管理',
    '{"zh-CN":"基础数据-供应商管理","en-US":"Basic Data - Supplier Management","zh-TW":"基礎資料-供應商管理","ja-JP":"基本データ - サプライヤー管理","ko-KR":"기초 데이터 - 공급업체 관리"}',
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
SET @basic_supplier_config_id := LAST_INSERT_ID();

INSERT INTO `fx_excel_import_config` (
    tenant_id, table_name, table_code, handler_bean_name, import_permission,
    title, title_i18n_json, subtitle, subtitle_i18n_json, subtitle_style_json,
    version, create_by, create_time, update_by, update_time, deleted
) VALUES (
    @tenant_id, '语言类型导入', 'I18nLanguageTypeTable', 'i18nLanguageTypeImportHandler', 'sys:i18nLanguageType:import',
    '语言类型导入模板',
    '{"zh-CN":"语言类型导入模板","en-US":"Language Type Import Template","zh-TW":"語言類型匯入範本","ja-JP":"言語タイプインポートテンプレート","ko-KR":"언어 유형 가져오기 템플릿"}',
    '系统管理-国际化语言类型',
    '{"zh-CN":"系统管理-国际化语言类型","en-US":"System Management - Language Types","zh-TW":"系統管理-國際化語言類型","ja-JP":"システム管理 - 言語タイプ","ko-KR":"시스템 관리 - 언어 유형"}',
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
SET @language_type_config_id := LAST_INSERT_ID();

UPDATE `fx_excel_import_config_item`
SET deleted = 1, update_by = @script_user, update_time = NOW()
WHERE deleted = 0
  AND config_id IN (@sys_user_config_id, @basic_supplier_config_id, @language_type_config_id);

DELETE FROM `fx_excel_import_config_item`
WHERE create_by = @script_user
  AND config_id IN (@sys_user_config_id, @basic_supplier_config_id, @language_type_config_id);

INSERT INTO `fx_excel_import_config_item` (
    tenant_id, config_id, sheet_code, sheet_name, i18n_json, import_field, field_type,
    field_remark, dict_code, data_source_type, data_source_value, depends_on_field_key,
    `separator`, required, order_num, create_by, create_time, update_by, update_time, deleted
) VALUES
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"账号","en-US":"Account","zh-TW":"帳號","ja-JP":"アカウント","ko-KR":"계정"}', 'account', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"用户名","en-US":"User Name","zh-TW":"使用者名稱","ja-JP":"ユーザー名","ko-KR":"사용자명"}', 'username', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"手机号","en-US":"Phone","zh-TW":"手機號","ja-JP":"電話番号","ko-KR":"휴대폰 번호"}', 'phone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"邮箱","en-US":"Email","zh-TW":"電子郵件","ja-JP":"メール","ko-KR":"이메일"}', 'email', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"员工ID","en-US":"Employee ID","zh-TW":"員工ID","ja-JP":"従業員ID","ko-KR":"직원 ID"}', 'employeeId', 'number', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @sys_user_config_id, 'main', '主表', '{"zh-CN":"用户来源","en-US":"User Source","zh-TW":"使用者來源","ja-JP":"ユーザーソース","ko-KR":"사용자 출처"}', 'userSource', 'number', NULL, NULL, 'JSON', '["1","2","3","4"]', NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0);

INSERT INTO `fx_excel_import_config_item` (
    tenant_id, config_id, sheet_code, sheet_name, i18n_json, import_field, field_type,
    field_remark, dict_code, data_source_type, data_source_value, depends_on_field_key,
    `separator`, required, order_num, create_by, create_time, update_by, update_time, deleted
) VALUES
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"供应商编码","en-US":"Supplier Code","zh-TW":"供應商編碼","ja-JP":"サプライヤーコード","ko-KR":"공급업체 코드"}', 'supplierCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"供应商全称","en-US":"Supplier Full Name","zh-TW":"供應商全稱","ja-JP":"サプライヤー正式名称","ko-KR":"공급업체 전체 이름"}', 'supplierFullName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"供应商简称","en-US":"Supplier Short Name","zh-TW":"供應商簡稱","ja-JP":"サプライヤー略称","ko-KR":"공급업체 약칭"}', 'supplierShortName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"Logo URL","en-US":"Logo URL","zh-TW":"Logo URL","ja-JP":"Logo URL","ko-KR":"Logo URL"}', 'logoUrl', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"英文名称","en-US":"English Name","zh-TW":"英文名稱","ja-JP":"英語名","ko-KR":"영문명"}', 'englishName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"当前地址","en-US":"Current Address","zh-TW":"目前地址","ja-JP":"現在住所","ko-KR":"현재 주소"}', 'currentAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"主要联系人","en-US":"Primary Contact","zh-TW":"主要聯絡人","ja-JP":"主担当者","ko-KR":"주 연락처"}', 'primaryContact', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"联系电话","en-US":"Contact Phone","zh-TW":"聯絡電話","ja-JP":"連絡先電話","ko-KR":"연락처 전화"}', 'contactPhone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"合作状态","en-US":"Cooperation Status","zh-TW":"合作狀態","ja-JP":"協力状態","ko-KR":"협력 상태"}', 'cooperationStatus', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 9, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"信用等级","en-US":"Credit Level","zh-TW":"信用等級","ja-JP":"信用レベル","ko-KR":"신용 등급"}', 'creditLevel', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 10, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"风险等级","en-US":"Risk Level","zh-TW":"風險等級","ja-JP":"リスクレベル","ko-KR":"위험 등급"}', 'riskLevel', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 11, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"供应商等级","en-US":"Supplier Level","zh-TW":"供應商等級","ja-JP":"サプライヤーレベル","ko-KR":"공급업체 등급"}', 'supplierLevel', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 12, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"关联租户编码","en-US":"Related Tenant Code","zh-TW":"關聯租戶編碼","ja-JP":"関連テナントコード","ko-KR":"연결 테넌트 코드"}', 'relatedTenantCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 13, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"审核状态","en-US":"Review Status","zh-TW":"審核狀態","ja-JP":"審査ステータス","ko-KR":"검토 상태"}', 'reviewStatus', 'number', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 14, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'main', '主表', '{"zh-CN":"备注","en-US":"Remark","zh-TW":"備註","ja-JP":"備考","ko-KR":"비고"}', 'remark', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 15, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"供应商编码","en-US":"Supplier Code","zh-TW":"供應商編碼","ja-JP":"サプライヤーコード","ko-KR":"공급업체 코드"}', 'supplierCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"法定代表人","en-US":"Legal Representative","zh-TW":"法定代表人","ja-JP":"法定代表者","ko-KR":"법정 대표자"}', 'legalRepresentative', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"注册资本","en-US":"Registered Capital","zh-TW":"註冊資本","ja-JP":"登録資本金","ko-KR":"등록 자본"}', 'registeredCapital', 'decimal', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"成立日期","en-US":"Establishment Date","zh-TW":"成立日期","ja-JP":"設立日","ko-KR":"설립일"}', 'establishmentDate', 'date', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"企业性质","en-US":"Enterprise Nature","zh-TW":"企業性質","ja-JP":"企業形態","ko-KR":"기업 성격"}', 'enterpriseNature', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"行业类别","en-US":"Industry Category","zh-TW":"產業類別","ja-JP":"業種カテゴリ","ko-KR":"산업 분류"}', 'industryCategory', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"注册地址","en-US":"Registered Address","zh-TW":"註冊地址","ja-JP":"登録住所","ko-KR":"등록 주소"}', 'registeredAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"经营地址","en-US":"Business Address","zh-TW":"營業地址","ja-JP":"事業所住所","ko-KR":"사업장 주소"}', 'businessAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"邮箱","en-US":"Email","zh-TW":"電子郵件","ja-JP":"メール","ko-KR":"이메일"}', 'email', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 9, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"税号","en-US":"Tax Number","zh-TW":"稅號","ja-JP":"税番号","ko-KR":"세금 번호"}', 'taxNumber', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 10, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"开户行","en-US":"Bank Name","zh-TW":"開戶行","ja-JP":"銀行名","ko-KR":"은행명"}', 'bankName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 11, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"银行账号","en-US":"Bank Account","zh-TW":"銀行帳號","ja-JP":"銀行口座","ko-KR":"은행 계좌"}', 'bankAccount', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 12, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"发票类型","en-US":"Invoice Type","zh-TW":"發票類型","ja-JP":"請求書タイプ","ko-KR":"송장 유형"}', 'invoiceType', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 13, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'detail', '详情', '{"zh-CN":"默认税率","en-US":"Default Tax Rate","zh-TW":"預設稅率","ja-JP":"デフォルト税率","ko-KR":"기본 세율"}', 'defaultTaxRate', 'decimal', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 14, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'contact', '联系人', '{"zh-CN":"供应商编码","en-US":"Supplier Code","zh-TW":"供應商編碼","ja-JP":"サプライヤーコード","ko-KR":"공급업체 코드"}', 'supplierCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'contact', '联系人', '{"zh-CN":"联系人姓名","en-US":"Contact Name","zh-TW":"聯絡人姓名","ja-JP":"連絡先名","ko-KR":"연락처 이름"}', 'contactName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'contact', '联系人', '{"zh-CN":"联系人电话","en-US":"Contact Phone","zh-TW":"聯絡人電話","ja-JP":"連絡先電話","ko-KR":"연락처 전화"}', 'contactPhone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'contact', '联系人', '{"zh-CN":"联系人职位","en-US":"Contact Position","zh-TW":"聯絡人職位","ja-JP":"連絡先役職","ko-KR":"연락처 직위"}', 'contactPosition', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'contact', '联系人', '{"zh-CN":"联系人邮箱","en-US":"Contact Email","zh-TW":"聯絡人郵件","ja-JP":"連絡先メール","ko-KR":"연락처 이메일"}', 'contactEmail', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"供应商编码","en-US":"Supplier Code","zh-TW":"供應商編碼","ja-JP":"サプライヤーコード","ko-KR":"공급업체 코드"}', 'supplierCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"资质类型","en-US":"Qualification Type","zh-TW":"資質類型","ja-JP":"資格タイプ","ko-KR":"자격 유형"}', 'qualificationType', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"证书编号","en-US":"Certificate No.","zh-TW":"證書編號","ja-JP":"証明書番号","ko-KR":"인증서 번호"}', 'certificateNo', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"签发日期","en-US":"Issue Date","zh-TW":"簽發日期","ja-JP":"発行日","ko-KR":"발급일"}', 'issueDate', 'date', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"到期日期","en-US":"Expire Date","zh-TW":"到期日期","ja-JP":"有効期限","ko-KR":"만료일"}', 'expireDate', 'date', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"附件","en-US":"Attachment","zh-TW":"附件","ja-JP":"添付ファイル","ko-KR":"첨부 파일"}', 'attachment', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @basic_supplier_config_id, 'qualification', '资质', '{"zh-CN":"是否有效","en-US":"Valid","zh-TW":"是否有效","ja-JP":"有効","ko-KR":"유효 여부"}', 'valid', 'boolean', NULL, NULL, 'JSON', '["1","0","true","false"]', NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0);

INSERT INTO `fx_excel_import_config_item` (
    tenant_id, config_id, sheet_code, sheet_name, i18n_json, import_field, field_type,
    field_remark, dict_code, data_source_type, data_source_value, depends_on_field_key,
    `separator`, required, order_num, create_by, create_time, update_by, update_time, deleted
) VALUES
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"语言编码","en-US":"Language Code","zh-TW":"語言編碼","ja-JP":"言語コード","ko-KR":"언어 코드"}', 'langCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"语言名称","en-US":"Language Name","zh-TW":"語言名稱","ja-JP":"言語名","ko-KR":"언어 이름"}', 'langName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"英文名称","en-US":"English Name","zh-TW":"英文名稱","ja-JP":"英語名","ko-KR":"영문명"}', 'langNameEn', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"图标","en-US":"Icon","zh-TW":"圖示","ja-JP":"アイコン","ko-KR":"아이콘"}', 'icon', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"排序号","en-US":"Order No.","zh-TW":"排序號","ja-JP":"表示順","ko-KR":"정렬 번호"}', 'orderNum', 'number', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"是否启用","en-US":"Enabled","zh-TW":"是否啟用","ja-JP":"有効","ko-KR":"활성화 여부"}', 'enabled', 'boolean', NULL, NULL, 'JSON', '["1","0","true","false"]', NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@tenant_id, @language_type_config_id, 'main', '主表', '{"zh-CN":"是否默认","en-US":"Default","zh-TW":"是否預設","ja-JP":"デフォルト","ko-KR":"기본 여부"}', 'isDefault', 'boolean', NULL, NULL, 'JSON', '["1","0","true","false"]', NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0);

INSERT INTO `fx_i18n_message` (
    module, prompt_code, text_i18n_json, enabled, version,
    create_by, create_time, update_by, update_time, deleted, tenant_id
) VALUES
('excel', 'EXCEL_IMPORT_HANDLER_NOT_CONFIGURED', '{"zh-CN":"导入处理器未配置","en-US":"Import handler is not configured","zh-TW":"匯入處理器未設定","ja-JP":"インポートハンドラーが設定されていません","ko-KR":"가져오기 처리기가 구성되지 않았습니다"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_HANDLER_NOT_FOUND', '{"zh-CN":"导入处理器不存在：{0}","en-US":"Import handler does not exist: {0}","zh-TW":"匯入處理器不存在：{0}","ja-JP":"インポートハンドラーが存在しません：{0}","ko-KR":"가져오기 처리기가 없습니다: {0}"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_HANDLER_TYPE_INVALID', '{"zh-CN":"导入处理器类型不正确：{0}","en-US":"Import handler type is invalid: {0}","zh-TW":"匯入處理器類型不正確：{0}","ja-JP":"インポートハンドラーの型が正しくありません：{0}","ko-KR":"가져오기 처리기 유형이 올바르지 않습니다: {0}"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_DATA_EMPTY', '{"zh-CN":"导入数据不能为空","en-US":"Import data cannot be empty","zh-TW":"匯入資料不能為空","ja-JP":"インポートデータは空にできません","ko-KR":"가져오기 데이터는 비워둘 수 없습니다"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_MODE_INVALID', '{"zh-CN":"导入方式无效","en-US":"Import mode is invalid","zh-TW":"匯入方式無效","ja-JP":"インポート方式が無効です","ko-KR":"가져오기 방식이 올바르지 않습니다"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_PERMISSION_NOT_CONFIGURED', '{"zh-CN":"导入权限码未配置","en-US":"Import permission code is not configured","zh-TW":"匯入權限碼未設定","ja-JP":"インポート権限コードが設定されていません","ko-KR":"가져오기 권한 코드가 구성되지 않았습니다"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_PERMISSION_DENIED', '{"zh-CN":"无导入权限","en-US":"No import permission","zh-TW":"無匯入權限","ja-JP":"インポート権限がありません","ko-KR":"가져오기 권한이 없습니다"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0),
('excel', 'EXCEL_IMPORT_EXECUTE_SUCCESS', '{"zh-CN":"导入完成：共{0}条，新增{1}条，修改{2}条，跳过{3}条，失败{4}条","en-US":"Import completed: total {0}, created {1}, updated {2}, skipped {3}, failed {4}","zh-TW":"匯入完成：共{0}筆，新增{1}筆，修改{2}筆，跳過{3}筆，失敗{4}筆","ja-JP":"インポート完了：合計{0}件、追加{1}件、更新{2}件、スキップ{3}件、失敗{4}件","ko-KR":"가져오기 완료: 총 {0}건, 신규 {1}건, 수정 {2}건, 건너뜀 {3}건, 실패 {4}건"}', 1, 1, @script_user, NOW(), @script_user, NOW(), 0, 0)
ON DUPLICATE KEY UPDATE
    text_i18n_json = VALUES(text_i18n_json),
    enabled = VALUES(enabled),
    version = VALUES(version),
    update_by = @script_user,
    update_time = NOW(),
    tenant_id = VALUES(tenant_id);
