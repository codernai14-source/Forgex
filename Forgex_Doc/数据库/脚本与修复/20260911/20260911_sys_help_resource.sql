-- 帮助中心：建表、联系配置、菜单权限、动态表格、上传扩展名。
-- 脚本可重复执行，目标库：forgex_admin、forgex_common。
-- 请使用 UTF-8 / utf8mb4 客户端执行，避免中文注释乱码。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260911_sys_help_resource';
SET @now := NOW();

-- ----------------------------------------------------------------
-- 一、建表：sys_help_resource
-- ----------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sys_help_resource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `title` varchar(200) NOT NULL COMMENT '资源标题',
  `doc_type` varchar(20) NOT NULL COMMENT '文档类型：MANUAL=手册，VIDEO=视频',
  `scope_type` varchar(20) NOT NULL COMMENT '范围：GLOBAL=全局，MENU=菜单覆盖',
  `source_type` varchar(20) NOT NULL DEFAULT 'FILE' COMMENT '来源：FILE=上传文件，EXTERNAL_URL=外链',
  `menu_id` bigint DEFAULT NULL COMMENT '绑定菜单 ID，GLOBAL 时为空',
  `menu_path` varchar(255) DEFAULT NULL COMMENT '工作区全路径，便于按路由匹配',
  `file_name` varchar(255) DEFAULT NULL COMMENT '原始文件名',
  `file_url` varchar(500) DEFAULT NULL COMMENT '文件访问地址',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '文件扩展名，不含点',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `content_type` varchar(100) DEFAULT NULL COMMENT '文件 MIME 类型',
  `external_url` varchar(1000) DEFAULT NULL COMMENT '外链地址',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号，越小越靠前',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_help_type_scope_status` (`doc_type`, `scope_type`, `status`, `deleted`),
  KEY `idx_help_menu_path` (`menu_path`, `doc_type`, `status`),
  KEY `idx_help_tenant_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帮助资源（手册/视频）';

-- ----------------------------------------------------------------
-- 二、菜单与按钮：挂在系统运维目录
-- ----------------------------------------------------------------
SET @sys_module_id := COALESCE(
  (SELECT id FROM `sys_module` WHERE deleted = 0 AND code IN ('sys', 'system') ORDER BY id LIMIT 1),
  (SELECT module_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'SystemMaintenance' AND type = 'catalog' ORDER BY id LIMIT 1),
  1
);
SET @public_tenant_id := COALESCE(
  (SELECT tenant_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'SystemMaintenance' AND type = 'catalog' ORDER BY id LIMIT 1),
  1
);
SET @admin_role_id := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @public_tenant_id AND role_key = 'admin' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id LIMIT 1)
);
SET @maintenance_parent_id := COALESCE(
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'SystemMaintenance' AND type = 'catalog' AND tenant_id = @public_tenant_id ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'SystemMaintenance' AND type = 'catalog' ORDER BY id LIMIT 1)
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @sys_module_id, @maintenance_parent_id, 'menu', 'helpResource', '帮助资源',
       '{"zh-CN":"帮助资源","zh-TW":"幫助資源","en-US":"Help Resources","ja-JP":"ヘルプ資源","ko-KR":"도움말 자원"}',
       'QuestionCircleOutlined', 'SystemHelpResource', 'sys:help:view', 12, 1, 1,
       @now, @script_user, @now, @script_user, 0, 2, 'embedded', NULL
WHERE @maintenance_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` existing
    WHERE existing.deleted = 0
      AND existing.component_key = 'SystemHelpResource'
  );

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT parent.tenant_id, parent.tenant_type, parent.module_id, parent.id, 'button', item.path, item.name, item.name_i18n_json,
       NULL, NULL, item.perm_key, item.order_num, 0, 1, @now, @script_user, @now, @script_user, 0, 3, 'embedded', NULL
FROM (
  SELECT 'add' path, '新增帮助资源' name, '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}' name_i18n_json, 'sys:help:add' perm_key, 1 order_num
  UNION ALL SELECT 'edit', '编辑帮助资源', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', 'sys:help:edit', 2
  UNION ALL SELECT 'delete', '删除帮助资源', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', 'sys:help:delete', 3
  UNION ALL SELECT 'contactEdit', '编辑联系方式', '{"zh-CN":"编辑联系方式","zh-TW":"編輯聯絡方式","en-US":"Edit Contact","ja-JP":"連絡先編集","ko-KR":"연락처 편집"}', 'sys:help:contact:edit', 4
) item
JOIN `sys_menu` parent ON parent.deleted = 0 AND parent.component_key = 'SystemHelpResource'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` existing
  WHERE existing.deleted = 0
    AND existing.parent_id = parent.id
    AND existing.perm_key = item.perm_key
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT item.permission_name, item.permission_key, item.url, 'POST', 0, @now, @now, 0
FROM (
  SELECT '帮助资源查询' permission_name, 'sys:help:view' permission_key, '/sys/help-resource/page' url
  UNION ALL SELECT '帮助资源新增', 'sys:help:add', '/sys/help-resource/create'
  UNION ALL SELECT '帮助资源编辑', 'sys:help:edit', '/sys/help-resource/update'
  UNION ALL SELECT '帮助资源删除', 'sys:help:delete', '/sys/help-resource/delete'
  UNION ALL SELECT '帮助联系方式编辑', 'sys:help:contact:edit', '/sys/help-contact/save'
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` existing
  WHERE existing.deleted = 0
    AND existing.permission_key = item.permission_key
);

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.id
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.deleted = 0
  AND p.permission_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit')
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
    m.component_key = 'SystemHelpResource'
    OR m.perm_key IN ('sys:help:view', 'sys:help:add', 'sys:help:edit', 'sys:help:delete', 'sys:help:contact:edit')
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @public_tenant_id AND rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

-- ----------------------------------------------------------------
-- 三、forgex_common：联系配置、上传扩展名、动态表格
-- ----------------------------------------------------------------
USE `forgex_common`;

SET @script_user := '20260911_sys_help_resource';
SET @now := NOW();

INSERT INTO `sys_config` (`tenant_id`, `config_key`, `config_value`, `create_time`, `update_time`, `deleted`, `remark`)
SELECT 0, 'system.help.contact',
       '{"phone":"","email":"","wechatQrUrl":"","address":"","workTime":"","remark":""}',
       @now, @now, 0, '帮助中心联系我们'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_config` existing
  WHERE existing.config_key = 'system.help.contact'
    AND existing.deleted = 0
);

INSERT INTO `sys_config` (`tenant_id`, `config_key`, `config_value`, `create_time`, `update_time`, `deleted`, `remark`)
SELECT 0, 'system.help.upload',
       '{"videoMaxSizeMb":200}',
       @now, @now, 0, '帮助中心上传限制'
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_config` existing
  WHERE existing.config_key = 'system.help.upload'
    AND existing.deleted = 0
);

UPDATE `sys_config`
SET `config_value` = JSON_SET(
      CAST(`config_value` AS JSON),
      '$.allowedExtensions',
      JSON_MERGE_PRESERVE(
        COALESCE(JSON_EXTRACT(CAST(`config_value` AS JSON), '$.allowedExtensions'), JSON_ARRAY()),
        JSON_ARRAY('md', 'mp4')
      )
    ),
    `update_time` = @now
WHERE `config_key` = 'file.upload.settings'
  AND `deleted` = 0
  AND (
    JSON_EXTRACT(CAST(`config_value` AS JSON), '$.allowedExtensions') IS NULL
    OR JSON_SEARCH(CAST(`config_value` AS JSON), 'one', 'md', NULL, '$.allowedExtensions[*]') IS NULL
    OR JSON_SEARCH(CAST(`config_value` AS JSON), 'one', 'mp4', NULL, '$.allowedExtensions[*]') IS NULL
  );

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'SystemHelpResourceTable',
       '{"zh-CN":"帮助资源","zh-TW":"幫助資源","en-US":"Help Resources","ja-JP":"ヘルプ資源","ko-KR":"도움말 자원"}',
       'BUSINESS', 'id', 10, 1, 1, @script_user, @now, @script_user, @now, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = 'SystemHelpResourceTable'
    AND existing.deleted = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.field, item.title_i18n_json, item.align, item.width, item.fixed, item.ellipsis, item.sortable, item.sorter_field,
       item.queryable, item.query_type, item.query_operator, item.dict_code, item.render_type, NULL, item.order_num, 1, @script_user, @now, @script_user, @now, 0
FROM (
  SELECT 'SystemHelpResourceTable' table_code, 'title' field, '{"zh-CN":"标题","zh-TW":"標題","en-US":"Title","ja-JP":"タイトル","ko-KR":"제목"}' title_i18n_json, 'left' align, 220 width, NULL fixed, 1 ellipsis, 1 sortable, 'title' sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, NULL render_type, 1 order_num
  UNION ALL SELECT 'SystemHelpResourceTable','docType','{"zh-CN":"类型","zh-TW":"類型","en-US":"Type","ja-JP":"種別","ko-KR":"유형"}','center',100,NULL,0,0,NULL,1,'select','eq',NULL,'tag',2
  UNION ALL SELECT 'SystemHelpResourceTable','scopeType','{"zh-CN":"范围","zh-TW":"範圍","en-US":"Scope","ja-JP":"範囲","ko-KR":"범위"}','center',100,NULL,0,0,NULL,1,'select','eq',NULL,'tag',3
  UNION ALL SELECT 'SystemHelpResourceTable','sourceType','{"zh-CN":"来源","zh-TW":"來源","en-US":"Source","ja-JP":"ソース","ko-KR":"출처"}','center',110,NULL,0,0,NULL,1,'select','eq',NULL,'tag',4
  UNION ALL SELECT 'SystemHelpResourceTable','menuPath','{"zh-CN":"菜单路径","zh-TW":"選單路徑","en-US":"Menu Path","ja-JP":"メニューパス","ko-KR":"메뉴 경로"}','left',240,NULL,1,0,NULL,1,'input','like',NULL,NULL,5
  UNION ALL SELECT 'SystemHelpResourceTable','fileName','{"zh-CN":"文件名","zh-TW":"檔名","en-US":"File Name","ja-JP":"ファイル名","ko-KR":"파일명"}','left',200,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,6
  UNION ALL SELECT 'SystemHelpResourceTable','sortOrder','{"zh-CN":"排序号","zh-TW":"排序","en-US":"Sort","ja-JP":"並び順","ko-KR":"정렬"}','center',90,NULL,0,1,'sortOrder',0,NULL,NULL,NULL,NULL,7
  UNION ALL SELECT 'SystemHelpResourceTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',90,NULL,0,1,'status',1,'select','eq','common_status','tag',8
  UNION ALL SELECT 'SystemHelpResourceTable','updateTime','{"zh-CN":"更新时间","zh-TW":"更新時間","en-US":"Update Time","ja-JP":"更新日時","ko-KR":"수정 시간"}','center',180,NULL,0,1,'updateTime',0,NULL,NULL,NULL,NULL,9
  UNION ALL SELECT 'SystemHelpResourceTable','fileExt','{"zh-CN":"格式","zh-TW":"格式","en-US":"Format","ja-JP":"形式","ko-KR":"형식"}','center',90,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,61
  UNION ALL SELECT 'SystemHelpResourceTable','fileSize','{"zh-CN":"大小","zh-TW":"大小","en-US":"Size","ja-JP":"サイズ","ko-KR":"크기"}','center',110,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,62
  UNION ALL SELECT 'SystemHelpResourceTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',200,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
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
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` tgt
    WHERE tgt.`tenant_id` = cfg.`tenant_id`
      AND tgt.`table_code` = pub.`table_code`
      AND tgt.`field` = pub.`field`
      AND tgt.`deleted` = 0
  );

SELECT 'sys_help_resource_upgrade_done' AS result;
