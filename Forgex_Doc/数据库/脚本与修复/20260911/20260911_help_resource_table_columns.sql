-- 帮助资源动态表补「格式」「大小」列。
-- 目标库：forgex_common。可重复执行。
-- 请使用 UTF-8 / utf8mb4 客户端执行，避免中文注释乱码。

SET NAMES utf8mb4;

USE `forgex_common`;

SET @script_user := '20260911_help_resource_table_columns';
SET @now := NOW();

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.field, item.title_i18n_json, item.align, item.width, item.fixed, item.ellipsis, item.sortable, item.sorter_field,
       item.queryable, item.query_type, item.query_operator, item.dict_code, item.render_type, NULL, item.order_num, 1,
       @script_user, @now, @script_user, @now, 0
FROM (
  SELECT 'SystemHelpResourceTable' table_code, 'fileExt' field,
         '{"zh-CN":"格式","zh-TW":"格式","en-US":"Format","ja-JP":"形式","ko-KR":"형식"}' title_i18n_json,
         'center' align, 90 width, NULL fixed, 0 ellipsis, 0 sortable, NULL sorter_field,
         0 queryable, NULL query_type, NULL query_operator, NULL dict_code, NULL render_type, 61 order_num
  UNION ALL
  SELECT 'SystemHelpResourceTable', 'fileSize',
         '{"zh-CN":"大小","zh-TW":"大小","en-US":"Size","ja-JP":"サイズ","ko-KR":"크기"}',
         'center', 110, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, 62
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = item.table_code
    AND existing.field = item.field
    AND existing.deleted = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT cfg.`tenant_id`, pub.`table_code`, pub.`field`, pub.`title_i18n_json`, pub.`align`, pub.`width`, pub.`fixed`, pub.`ellipsis`, pub.`sortable`, pub.`sorter_field`,
       pub.`queryable`, pub.`query_type`, pub.`query_operator`, pub.`dict_code`, pub.`render_type`, pub.`perm_key`, pub.`order_num`, pub.`enabled`,
       @script_user, @now, @script_user, @now, 0
FROM `fx_table_config` cfg
JOIN `fx_table_column_config` pub
  ON pub.`tenant_id` = 0
 AND pub.`table_code` = cfg.`table_code`
 AND pub.`deleted` = 0
WHERE cfg.`tenant_id` <> 0
  AND cfg.`deleted` = 0
  AND cfg.`table_code` = 'SystemHelpResourceTable'
  AND pub.`field` IN ('fileExt', 'fileSize')
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` tgt
    WHERE tgt.`tenant_id` = cfg.`tenant_id`
      AND tgt.`table_code` = pub.`table_code`
      AND tgt.`field` = pub.`field`
      AND tgt.`deleted` = 0
  );

SELECT 'help_resource_table_columns_done' AS result;
