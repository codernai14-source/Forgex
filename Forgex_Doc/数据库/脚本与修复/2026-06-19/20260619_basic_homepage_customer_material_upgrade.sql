-- 基础信息首页组件、客户能力与物料自动编码升级
-- 说明：
-- 1. 基础信息首页组件库保留供应商、客户、工作日历，退役编码规则首页组件。
-- 2. 客户管理补充导入、导出、从第三方拉取、同步第三方按钮权限，并授予 admin。
-- 3. 集成平台补充客户默认同步/拉取 API code。
-- 4. 脚本支持重复执行。
SET NAMES utf8mb4;

SET @script_user := '20260619_basic_homepage_customer_material_upgrade';

USE `forgex_common`;

SET @customer_import_tenant_id := (
    SELECT COALESCE(
        (SELECT tenant_id FROM `fx_excel_import_config` WHERE table_code = 'basic_customer' ORDER BY deleted ASC, id ASC LIMIT 1),
        (SELECT tenant_id FROM `forgex_admin`.`sys_tenant` WHERE deleted = 0 ORDER BY id ASC LIMIT 1),
        0
    )
);

INSERT INTO `fx_excel_import_config` (
    tenant_id, table_name, table_code, handler_bean_name, import_permission,
    title, title_i18n_json, subtitle, subtitle_i18n_json, subtitle_style_json,
    version, create_by, create_time, update_by, update_time, deleted
) VALUES (
    @customer_import_tenant_id, '客户导入', 'basic_customer', 'basicCustomerImportHandler', 'basic:customer:import',
    '客户导入模板',
    '{"zh-CN":"客户导入模板","en-US":"Customer Import Template","zh-TW":"客戶匯入範本","ja-JP":"顧客インポートテンプレート","ko-KR":"고객 가져오기 템플릿"}',
    '基础数据-客户管理',
    '{"zh-CN":"基础数据-客户管理","en-US":"Basic Data - Customer Management","zh-TW":"基礎資料-客戶管理","ja-JP":"基本データ - 顧客管理","ko-KR":"기초 데이터 - 고객 관리"}',
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
    update_by = @script_user,
    update_time = NOW(),
    deleted = 0;

SET @basic_customer_import_config_id := LAST_INSERT_ID();

UPDATE `fx_excel_import_config_item`
SET deleted = 1, update_by = @script_user, update_time = NOW()
WHERE deleted = 0
  AND config_id = @basic_customer_import_config_id;

DELETE FROM `fx_excel_import_config_item`
WHERE create_by = @script_user
  AND config_id = @basic_customer_import_config_id;

INSERT INTO `fx_excel_import_config_item` (
    tenant_id, config_id, sheet_code, sheet_name, i18n_json, import_field, field_type,
    field_remark, dict_code, data_source_type, data_source_value, depends_on_field_key,
    `separator`, required, order_num, create_by, create_time, update_by, update_time, deleted
) VALUES
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户编码","en-US":"Customer Code","zh-TW":"客戶編碼","ja-JP":"顧客コード","ko-KR":"고객 코드"}', 'customerCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户全称","en-US":"Customer Full Name","zh-TW":"客戶全稱","ja-JP":"顧客正式名称","ko-KR":"고객 전체 이름"}', 'customerFullName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户简称","en-US":"Customer Short Name","zh-TW":"客戶簡稱","ja-JP":"顧客略称","ko-KR":"고객 약칭"}', 'customerShortName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户名称","en-US":"Customer Name","zh-TW":"客戶名稱","ja-JP":"顧客名","ko-KR":"고객 이름"}', 'customerName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户价值等级","en-US":"Customer Value Level","zh-TW":"客戶價值等級","ja-JP":"顧客価値レベル","ko-KR":"고객 가치 등급"}', 'customerValueLevel', 'string', NULL, NULL, 'JSON', '["STRATEGIC","KEY","NORMAL","LOW"]', NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"客户信用等级","en-US":"Customer Credit Level","zh-TW":"客戶信用等級","ja-JP":"顧客信用レベル","ko-KR":"고객 신용 등급"}', 'customerCreditLevel', 'string', NULL, NULL, 'JSON', '["A","B","C","D"]', NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"经营状态","en-US":"Business Status","zh-TW":"經營狀態","ja-JP":"営業状態","ko-KR":"영업 상태"}', 'businessStatus', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"实际经营地址","en-US":"Actual Business Address","zh-TW":"實際經營地址","ja-JP":"実営業住所","ko-KR":"실제 영업 주소"}', 'actualBusinessAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"收款地址","en-US":"Collection Address","zh-TW":"收款地址","ja-JP":"集金住所","ko-KR":"수금 주소"}', 'collectionAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 9, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"发货地址","en-US":"Shipping Address","zh-TW":"發貨地址","ja-JP":"出荷住所","ko-KR":"배송 주소"}', 'shippingAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 10, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"审批状态","en-US":"Approval Status","zh-TW":"審批狀態","ja-JP":"承認状態","ko-KR":"승인 상태"}', 'approvalStatus', 'number', NULL, NULL, 'JSON', '["0","1","2","3"]', NULL, ',', 0, 11, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"是否关联租户","en-US":"Related Tenant","zh-TW":"是否關聯租戶","ja-JP":"関連テナント有無","ko-KR":"관련 테넌트 여부"}', 'isRelatedTenant', 'boolean', NULL, NULL, 'JSON', '["1","0","true","false","是","否"]', NULL, ',', 0, 12, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"关联租户编码","en-US":"Related Tenant Code","zh-TW":"關聯租戶編碼","ja-JP":"関連テナントコード","ko-KR":"관련 테넌트 코드"}', 'relatedTenantCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 13, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"运输方式","en-US":"Transport Mode","zh-TW":"運輸方式","ja-JP":"輸送方式","ko-KR":"운송 방식"}', 'transportMode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 14, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"付款条款","en-US":"Payment Terms","zh-TW":"付款條款","ja-JP":"支払条件","ko-KR":"결제 조건"}', 'paymentTerms', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 15, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"国家","en-US":"Country","zh-TW":"國家","ja-JP":"国","ko-KR":"국가"}', 'country', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 16, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"企业性质","en-US":"Enterprise Nature","zh-TW":"企業性質","ja-JP":"企業形態","ko-KR":"기업 성격"}', 'enterpriseNature', 'string', NULL, NULL, 'JSON', '["DOMESTIC","OVERSEAS","STATE_OWNED","PRIVATE","FOREIGN","JOINT_VENTURE"]', NULL, ',', 0, 17, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"状态","en-US":"Status","zh-TW":"狀態","ja-JP":"状態","ko-KR":"상태"}', 'status', 'number', NULL, NULL, 'JSON', '["1","0"]', NULL, ',', 0, 18, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'main', '主表', '{"zh-CN":"备注","en-US":"Remark","zh-TW":"備註","ja-JP":"備考","ko-KR":"비고"}', 'remark', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 19, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'contact', '联系人', '{"zh-CN":"客户编码","en-US":"Customer Code","zh-TW":"客戶編碼","ja-JP":"顧客コード","ko-KR":"고객 코드"}', 'customerCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'contact', '联系人', '{"zh-CN":"联系人姓名","en-US":"Contact Name","zh-TW":"聯絡人姓名","ja-JP":"連絡先名","ko-KR":"연락처 이름"}', 'contactName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'contact', '联系人', '{"zh-CN":"联系人职位","en-US":"Contact Position","zh-TW":"聯絡人職位","ja-JP":"連絡先役職","ko-KR":"연락처 직위"}', 'contactPosition', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'contact', '联系人', '{"zh-CN":"联系人电话","en-US":"Contact Phone","zh-TW":"聯絡人電話","ja-JP":"連絡先電話","ko-KR":"연락처 전화"}', 'contactPhone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"客户编码","en-US":"Customer Code","zh-TW":"客戶編碼","ja-JP":"顧客コード","ko-KR":"고객 코드"}', 'customerCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"发票抬头全称","en-US":"Invoice Full Name","zh-TW":"發票抬頭全稱","ja-JP":"請求書正式名称","ko-KR":"송장 전체 이름"}', 'invoiceFullName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"税号","en-US":"Tax Number","zh-TW":"稅號","ja-JP":"税番号","ko-KR":"세금 번호"}', 'taxNumber', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"注册地址","en-US":"Registered Address","zh-TW":"註冊地址","ja-JP":"登録住所","ko-KR":"등록 주소"}', 'registeredAddress', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"注册电话","en-US":"Registered Phone","zh-TW":"註冊電話","ja-JP":"登録電話","ko-KR":"등록 전화"}', 'registeredPhone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"开户行","en-US":"Bank Name","zh-TW":"開戶行","ja-JP":"銀行名","ko-KR":"은행명"}', 'bankName', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"银行账号","en-US":"Bank Account","zh-TW":"銀行帳號","ja-JP":"銀行口座","ko-KR":"은행 계좌"}', 'bankAccount', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'invoice', '发票信息', '{"zh-CN":"是否需要发票","en-US":"Invoice Required","zh-TW":"是否需要發票","ja-JP":"請求書要否","ko-KR":"송장 필요 여부"}', 'invoiceRequired', 'boolean', NULL, NULL, 'JSON', '["1","0","true","false","是","否"]', NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"客户编码","en-US":"Customer Code","zh-TW":"客戶編碼","ja-JP":"顧客コード","ko-KR":"고객 코드"}', 'customerCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 1, 1, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"企业官网","en-US":"Official Website","zh-TW":"企業官網","ja-JP":"公式サイト","ko-KR":"공식 웹사이트"}', 'officialWebsite', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 2, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"总机电话","en-US":"Switchboard Phone","zh-TW":"總機電話","ja-JP":"代表電話","ko-KR":"대표 전화"}', 'switchboardPhone', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 3, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"官方邮箱域名","en-US":"Official Email Domain","zh-TW":"官方郵箱域名","ja-JP":"公式メールドメイン","ko-KR":"공식 이메일 도메인"}', 'officialEmailDomain', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 4, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"传真","en-US":"Fax","zh-TW":"傳真","ja-JP":"FAX","ko-KR":"팩스"}', 'faxNumber', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 5, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"渠道伙伴等级","en-US":"Channel Partner Level","zh-TW":"渠道夥伴等級","ja-JP":"チャネルパートナーレベル","ko-KR":"채널 파트너 등급"}', 'channelPartnerLevel', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 6, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"国民行业代码","en-US":"National Industry Code","zh-TW":"國民行業代碼","ja-JP":"標準産業コード","ko-KR":"국가 산업 코드"}', 'nationalIndustryCode', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 7, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"注册资本","en-US":"Registered Capital","zh-TW":"註冊資本","ja-JP":"登録資本金","ko-KR":"등록 자본"}', 'registeredCapital', 'decimal', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 8, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"注册资本币种","en-US":"Registered Capital Currency","zh-TW":"註冊資本幣種","ja-JP":"登録資本通貨","ko-KR":"등록 자본 통화"}', 'registeredCapitalCurrency', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 9, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"实缴资本","en-US":"Paid-in Capital","zh-TW":"實繳資本","ja-JP":"払込資本金","ko-KR":"납입 자본"}', 'paidInCapital', 'decimal', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 10, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"实缴资本币种","en-US":"Paid-in Capital Currency","zh-TW":"實繳資本幣種","ja-JP":"払込資本通貨","ko-KR":"납입 자본 통화"}', 'paidInCapitalCurrency', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 11, @script_user, NOW(), @script_user, NOW(), 0),
(@customer_import_tenant_id, @basic_customer_import_config_id, 'extra', '扩展信息', '{"zh-CN":"经营范围","en-US":"Business Scope","zh-TW":"經營範圍","ja-JP":"事業範囲","ko-KR":"사업 범위"}', 'businessScope', 'string', NULL, NULL, 'NONE', NULL, NULL, ',', 0, 12, @script_user, NOW(), @script_user, NOW(), 0);

USE `forgex_admin`;

SET @now := NOW();
SET @basic_module_id := (
    SELECT id FROM `sys_module`
    WHERE deleted = 0 AND code = 'basic'
    ORDER BY id ASC
    LIMIT 1
);
SET @customer_menu_id := (
    SELECT id FROM `sys_menu`
    WHERE deleted = 0
      AND (perm_key = 'basic:customer:query' OR component_key = 'BasicCustomer' OR path = 'customer')
    ORDER BY id ASC
    LIMIT 1
);

INSERT INTO `sys_homepage_component_category`
(`tenant_id`,`category_code`,`category_name`,`module_code`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, 'basic_common', 'Basic Data', 'basic', 'Basic data homepage components', @now, @now, @script_user, @script_user, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_homepage_component_category` c
    WHERE c.tenant_id = 0 AND c.module_code = 'basic' AND c.category_code = 'basic_common'
);

SET @basic_component_category_id := (
    SELECT id FROM `sys_homepage_component_category`
    WHERE tenant_id = 0 AND module_code = 'basic' AND category_code = 'basic_common'
    ORDER BY id ASC
    LIMIT 1
);

UPDATE `sys_homepage_component_config`
SET enabled = 0,
    deleted = 1,
    update_time = @now,
    update_by = @script_user,
    remark = CONCAT(COALESCE(remark, ''), CASE WHEN COALESCE(remark, '') = '' THEN '' ELSE '; ' END, 'Retired from basic homepage on 2026-06-19')
WHERE scope_level = 'PUBLIC'
  AND tenant_id = 0
  AND component_code = 'encodeRuleInfo'
  AND deleted = 0;

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @basic_component_category_id, 'PUBLIC', seed.component_code, seed.component_name, seed.component_path,
       seed.icon, seed.use_desc, '{}', 1, seed.order_num, seed.remark, @now, @now, @script_user, @script_user, 0
FROM (
    SELECT 'supplierInfo' AS component_code, 'Supplier Information' AS component_name, 'supplierInfo' AS component_path,
           'TeamOutlined' AS icon, 'Supplier master data and admission maintenance' AS use_desc,
           10 AS order_num, 'Basic homepage supplier information' AS remark
    UNION ALL SELECT 'customerInfo', 'Customer Information', 'customerInfo',
           'TeamOutlined', 'Customer master data and integration maintenance',
           20, 'Basic homepage customer information'
    UNION ALL SELECT 'workCalendarInfo', 'Work Calendar', 'workCalendarInfo',
           'CalendarOutlined', 'Workday and holiday maintenance',
           30, 'Basic homepage work calendar'
) seed
WHERE @basic_component_category_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_homepage_component_config` c
      WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = seed.component_code
  );

UPDATE `sys_homepage_component_config` c
JOIN (
    SELECT 'supplierInfo' AS component_code, 'Supplier Information' AS component_name, 'supplierInfo' AS component_path,
           'TeamOutlined' AS icon, 'Supplier master data and admission maintenance' AS use_desc,
           10 AS order_num, 'Basic homepage supplier information' AS remark
    UNION ALL SELECT 'customerInfo', 'Customer Information', 'customerInfo',
           'TeamOutlined', 'Customer master data and integration maintenance',
           20, 'Basic homepage customer information'
    UNION ALL SELECT 'workCalendarInfo', 'Work Calendar', 'workCalendarInfo',
           'CalendarOutlined', 'Workday and holiday maintenance',
           30, 'Basic homepage work calendar'
) seed ON seed.component_code = c.component_code
SET c.category_id = @basic_component_category_id,
    c.component_name = seed.component_name,
    c.component_path = seed.component_path,
    c.icon = seed.icon,
    c.use_desc = seed.use_desc,
    c.enabled = 1,
    c.deleted = 0,
    c.order_num = seed.order_num,
    c.remark = seed.remark,
    c.update_time = @now,
    c.update_by = @script_user
WHERE c.scope_level = 'PUBLIC'
  AND c.tenant_id = 0
  AND @basic_component_category_id IS NOT NULL
  AND c.component_code IN ('supplierInfo', 'customerInfo', 'workCalendarInfo');

INSERT INTO `sys_menu`
(tenant_id, tenant_type, module_id, parent_id, type, path, name, name_i18n_json, icon, component_key,
 perm_key, order_num, visible, status, create_time, create_by, update_time, update_by, deleted, menu_level, menu_mode, external_url)
SELECT COALESCE(parent_menu.tenant_id, 0), COALESCE(parent_menu.tenant_type, 'PUBLIC'), COALESCE(parent_menu.module_id, @basic_module_id),
       @customer_menu_id, 'button', NULL, seed.name, seed.name_i18n_json, NULL, seed.component_key,
       seed.perm_key, seed.order_num, 0, 1, @now, @script_user, @now, @script_user, 0, 3, 'embedded', NULL
FROM (
    SELECT '导入客户' AS name,
           JSON_OBJECT('zh-CN','导入客户','zh-TW','匯入客戶','en-US','Import Customer','ja-JP','顧客インポート','ko-KR','고객 가져오기') AS name_i18n_json,
           'BasicCustomerImport' AS component_key, 'basic:customer:import' AS perm_key, 7 AS order_num
    UNION ALL SELECT '导出客户',
           JSON_OBJECT('zh-CN','导出客户','zh-TW','匯出客戶','en-US','Export Customer','ja-JP','顧客エクスポート','ko-KR','고객 내보내기'),
           'BasicCustomerExport', 'basic:customer:export', 8
    UNION ALL SELECT '从第三方拉取客户',
           JSON_OBJECT('zh-CN','从第三方拉取客户','zh-TW','從第三方拉取客戶','en-US','Pull Customers From Third Party','ja-JP','外部から顧客取得','ko-KR','타사에서 고객 가져오기'),
           'BasicCustomerPullThirdParty', 'basic:customer:pullThirdParty', 9
    UNION ALL SELECT '同步客户到第三方',
           JSON_OBJECT('zh-CN','同步客户到第三方','zh-TW','同步客戶到第三方','en-US','Sync Customers To Third Party','ja-JP','外部へ顧客同期','ko-KR','타사로 고객 동기화'),
           'BasicCustomerSyncThirdParty', 'basic:customer:sync', 10
) seed
LEFT JOIN `sys_menu` parent_menu ON parent_menu.id = @customer_menu_id
WHERE @customer_menu_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM `sys_menu` existing
      WHERE existing.deleted = 0 AND existing.perm_key = seed.perm_key
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
        'basic:customer:import',
        'basic:customer:export',
        'basic:customer:pullThirdParty',
        'basic:customer:sync'
    )
WHERE (base_menu.perm_key = 'basic:customer:query' OR base_menu.component_key = 'BasicCustomer' OR base_menu.path = 'customer')
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.role_id = rm_base.role_id
        AND rm.menu_id = m.id
        AND rm.tenant_id = rm_base.tenant_id
  );

INSERT INTO `sys_role_menu` (tenant_id, role_id, menu_id)
SELECT DISTINCT r.tenant_id, r.id, m.id
FROM `sys_role` r
JOIN `sys_menu` m ON m.deleted = 0
    AND m.tenant_id = r.tenant_id
    AND m.perm_key IN (
        'basic:customer:import',
        'basic:customer:export',
        'basic:customer:pullThirdParty',
        'basic:customer:sync'
    )
WHERE r.deleted = 0
  AND r.role_key = 'admin'
  AND NOT EXISTS (
      SELECT 1 FROM `sys_role_menu` rm
      WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id
  );

USE `forgex_integration`;

INSERT INTO `fx_api_config` (
    api_code, api_name, api_desc, direction, api_path, processor_bean, call_method,
    http_method, invoke_mode, content_type, target_url, timeout_ms, retry_count,
    retry_interval_ms, max_concurrent, queue_limit, auth_type, auth_config,
    call_count, status, module_code, tenant_id, create_time, create_by,
    update_time, update_by, deleted
)
SELECT seed.api_code, seed.api_name, seed.api_desc, 'OUTBOUND', NULL, NULL, 'HTTP',
       'POST', 'SYNC', 'application/json', NULL, 30000, 0,
       0, 0, 0, NULL, NULL,
       0, 1, 'basic', 0, NOW(), @script_user,
       NOW(), @script_user, 0
FROM (
    SELECT 'basic_customer_sync' AS api_code, 'Customer Sync To Third Party' AS api_name,
           'Push Forgex customer master data to configured third-party systems.' AS api_desc
    UNION ALL SELECT 'basic_customer_pull', 'Pull Customers From Third Party',
           'Pull customer master data from configured third-party systems and write to Basic.'
) seed
WHERE NOT EXISTS (
    SELECT 1 FROM `fx_api_config` existing
    WHERE existing.deleted = 0 AND existing.api_code = seed.api_code
);
