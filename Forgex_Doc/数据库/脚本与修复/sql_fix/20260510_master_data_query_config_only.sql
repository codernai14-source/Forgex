-- Fix query-area backend configuration for customer, supplier, and material master pages.
-- Safe to rerun.
-- Target database: forgex_common
-- This script is ASCII-only to avoid client encoding syntax errors.

SET NAMES utf8mb4;
USE `forgex_common`;

SET @OPERATOR = 'codex';
SET @NOW = NOW();

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'CustomerMasterTable',
       '{"zh-CN":"Customer Management","en-US":"Customer Management","zh-TW":"Customer Management","ja-JP":"Customer Management","ko-KR":"Customer Management"}',
       'BUSINESS', 'id', 10, 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `deleted` = 0
);

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'SupplierMasterTable',
       '{"zh-CN":"Supplier Management","en-US":"Supplier Management","zh-TW":"Supplier Management","ja-JP":"Supplier Management","ko-KR":"Supplier Management"}',
       'BUSINESS', 'id', 10, 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `deleted` = 0
);

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'MaterialTable',
       '{"zh-CN":"Material Management","en-US":"Material Management","zh-TW":"Material Management","ja-JP":"Material Management","ko-KR":"Material Management"}',
       'BUSINESS', 'id', 10, 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','customerCode','{"zh-CN":"Customer Code","en-US":"Customer Code","zh-TW":"Customer Code","ja-JP":"Customer Code","ko-KR":"Customer Code"}','left',140,NULL,0,1,'customerCode',1,'input','like',NULL,NULL,NULL,1,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerCode' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','customerFullName','{"zh-CN":"Customer Name","en-US":"Customer Name","zh-TW":"Customer Name","ja-JP":"Customer Name","ko-KR":"Customer Name"}','left',220,NULL,1,1,'customerFullName',1,'input','like',NULL,NULL,NULL,2,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerFullName' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','customerValueLevel','{"zh-CN":"Value Level","en-US":"Value Level","zh-TW":"Value Level","ja-JP":"Value Level","ko-KR":"Value Level"}','center',120,NULL,0,1,'customerValueLevel',1,'select','eq','customer_value_level',NULL,NULL,3,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerValueLevel' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','customerCreditLevel','{"zh-CN":"Credit Level","en-US":"Credit Level","zh-TW":"Credit Level","ja-JP":"Credit Level","ko-KR":"Credit Level"}','center',120,NULL,0,1,'customerCreditLevel',1,'select','eq','customer_credit_level',NULL,NULL,4,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerCreditLevel' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','businessStatus','{"zh-CN":"Business Status","en-US":"Business Status","zh-TW":"Business Status","ja-JP":"Business Status","ko-KR":"Business Status"}','center',120,NULL,0,1,'businessStatus',1,'select','eq','customer_business_status',NULL,NULL,5,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'businessStatus' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','approvalStatus','{"zh-CN":"Approval Status","en-US":"Approval Status","zh-TW":"Approval Status","ja-JP":"Approval Status","ko-KR":"Approval Status"}','center',120,NULL,0,1,'approvalStatus',1,'select','eq','customer_approval_status','tag',NULL,6,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'approvalStatus' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','status','{"zh-CN":"Status","en-US":"Status","zh-TW":"Status","ja-JP":"Status","ko-KR":"Status"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',NULL,7,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'status' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'CustomerMasterTable','action','{"zh-CN":"Action","en-US":"Action","zh-TW":"Action","ja-JP":"Action","ko-KR":"Action"}','center',260,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'action' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','supplierCode','{"zh-CN":"Supplier Code","en-US":"Supplier Code","zh-TW":"Supplier Code","ja-JP":"Supplier Code","ko-KR":"Supplier Code"}','left',140,NULL,0,1,'supplierCode',1,'input','like',NULL,NULL,NULL,1,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'supplierCode' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','supplierFullName','{"zh-CN":"Supplier Name","en-US":"Supplier Name","zh-TW":"Supplier Name","ja-JP":"Supplier Name","ko-KR":"Supplier Name"}','left',220,NULL,1,1,'supplierFullName',1,'input','like',NULL,NULL,NULL,2,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'supplierFullName' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','cooperationStatus','{"zh-CN":"Cooperation Status","en-US":"Cooperation Status","zh-TW":"Cooperation Status","ja-JP":"Cooperation Status","ko-KR":"Cooperation Status"}','center',120,NULL,0,1,'cooperationStatus',1,'select','eq','supplier_cooperation_status','tag',NULL,3,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'cooperationStatus' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','creditLevel','{"zh-CN":"Credit Level","en-US":"Credit Level","zh-TW":"Credit Level","ja-JP":"Credit Level","ko-KR":"Credit Level"}','center',110,NULL,0,1,'creditLevel',1,'select','eq','supplier_credit_level',NULL,NULL,4,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'creditLevel' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','riskLevel','{"zh-CN":"Risk Level","en-US":"Risk Level","zh-TW":"Risk Level","ja-JP":"Risk Level","ko-KR":"Risk Level"}','center',110,NULL,0,1,'riskLevel',1,'select','eq','supplier_risk_level','tag',NULL,5,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'riskLevel' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','supplierLevel','{"zh-CN":"Supplier Level","en-US":"Supplier Level","zh-TW":"Supplier Level","ja-JP":"Supplier Level","ko-KR":"Supplier Level"}','center',120,NULL,0,1,'supplierLevel',1,'select','eq','supplier_level',NULL,NULL,6,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'supplierLevel' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','reviewStatus','{"zh-CN":"Review Status","en-US":"Review Status","zh-TW":"Review Status","ja-JP":"Review Status","ko-KR":"Review Status"}','center',120,NULL,0,1,'reviewStatus',1,'select','eq','supplier_review_status','tag',NULL,7,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'reviewStatus' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SupplierMasterTable','action','{"zh-CN":"Action","en-US":"Action","zh-TW":"Action","ja-JP":"Action","ko-KR":"Action"}','center',260,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'action' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','materialCode','{"zh-CN":"Material Code","en-US":"Material Code","zh-TW":"Material Code","ja-JP":"Material Code","ko-KR":"Material Code"}','left',140,NULL,0,1,'materialCode',1,'input','like',NULL,NULL,NULL,1,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'materialCode' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','materialName','{"zh-CN":"Material Name","en-US":"Material Name","zh-TW":"Material Name","ja-JP":"Material Name","ko-KR":"Material Name"}','left',180,NULL,1,1,'materialName',1,'input','like',NULL,NULL,NULL,2,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'materialName' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','materialType','{"zh-CN":"Material Type","en-US":"Material Type","zh-TW":"Material Type","ja-JP":"Material Type","ko-KR":"Material Type"}','center',130,NULL,0,1,'materialType',1,'select','eq','material_type','tag',NULL,3,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'materialType' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','materialCategory','{"zh-CN":"Material Category","en-US":"Material Category","zh-TW":"Material Category","ja-JP":"Material Category","ko-KR":"Material Category"}','left',140,NULL,0,1,'materialCategory',1,'input','like',NULL,NULL,NULL,4,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'materialCategory' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','status','{"zh-CN":"Status","en-US":"Status","zh-TW":"Status","ja-JP":"Status","ko-KR":"Status"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',NULL,5,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'status' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','approvalStatus','{"zh-CN":"Approval Status","en-US":"Approval Status","zh-TW":"Approval Status","ja-JP":"Approval Status","ko-KR":"Approval Status"}','center',120,NULL,0,1,'approvalStatus',1,'select','eq','material_approval_status','tag',NULL,6,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'approvalStatus' AND `deleted` = 0);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'MaterialTable','action','{"zh-CN":"Action","en-US":"Action","zh-TW":"Action","ja-JP":"Action","ko-KR":"Action"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `fx_table_column_config` WHERE `tenant_id` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'action' AND `deleted` = 0);

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like', `dict_code` = NULL, `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` IN ('customerCode','customerFullName');

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_value_level', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerValueLevel';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_credit_level', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'customerCreditLevel';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_business_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'businessStatus';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_approval_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'approvalStatus';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'common_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'CustomerMasterTable' AND `field` = 'status';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like', `dict_code` = NULL, `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` IN ('supplierCode','supplierFullName');

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_cooperation_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'cooperationStatus';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_credit_level', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'creditLevel';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_risk_level', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'riskLevel';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_level', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'supplierLevel';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_review_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'SupplierMasterTable' AND `field` = 'reviewStatus';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like', `dict_code` = NULL, `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'MaterialTable' AND `field` IN ('materialCode','materialName','materialCategory');

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'material_type', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'materialType';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'common_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'status';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'material_approval_status', `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0 AND `table_code` = 'MaterialTable' AND `field` = 'approvalStatus';

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT cfg.`tenant_id`, pub.`table_code`, pub.`field`, pub.`title_i18n_json`, pub.`align`, pub.`width`, pub.`fixed`, pub.`ellipsis`, pub.`sortable`, pub.`sorter_field`, pub.`queryable`, pub.`query_type`, pub.`query_operator`, pub.`dict_code`, pub.`render_type`, pub.`perm_key`, pub.`order_num`, pub.`enabled`, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM `fx_table_config` cfg
JOIN `fx_table_column_config` pub
  ON pub.`tenant_id` = 0
 AND pub.`table_code` = cfg.`table_code`
 AND pub.`deleted` = 0
WHERE cfg.`tenant_id` <> 0
  AND cfg.`deleted` = 0
  AND cfg.`table_code` IN ('CustomerMasterTable','SupplierMasterTable','MaterialTable')
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` tgt
    WHERE tgt.`tenant_id` = cfg.`tenant_id`
      AND tgt.`table_code` = pub.`table_code`
      AND tgt.`field` = pub.`field`
      AND tgt.`deleted` = 0
  );

UPDATE `fx_user_table_config`
SET `query_config` = NULL, `update_time` = @NOW, `update_by` = @OPERATOR
WHERE `deleted` = 0
  AND `table_code` IN ('CustomerMasterTable','SupplierMasterTable','MaterialTable')
  AND (`query_config` IS NULL OR TRIM(`query_config`) = '' OR TRIM(`query_config`) = '[]');

SELECT `table_code`, `tenant_id`, COUNT(*) AS `query_field_count`
FROM `fx_table_column_config`
WHERE `deleted` = 0
  AND `enabled` = 1
  AND `queryable` = 1
  AND `table_code` IN ('CustomerMasterTable','SupplierMasterTable','MaterialTable')
GROUP BY `table_code`, `tenant_id`
ORDER BY `table_code`, `tenant_id`;
