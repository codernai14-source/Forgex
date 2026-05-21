-- 标签模块菜单与权限补充脚本
-- 适用库：forgex_admin
-- 说明：脚本可重复执行。用于补齐标签类型、标签字段页面菜单，以及当前标签模块后端权限。

USE `forgex_admin`;

SET @NOW := NOW();
SET @OPERATOR := '20260515_label_menu_permission_seed';
SET @PUBLIC_TENANT_ID := 1993479636925403138;

SET @LABEL_PARENT_ID := COALESCE(
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND path = 'label' ORDER BY id ASC LIMIT 1),
  754
);

SET @LABEL_MODULE_ID := COALESCE(
  (SELECT module_id FROM `sys_menu` WHERE id = @LABEL_PARENT_ID LIMIT 1),
  5
);

SET @ADMIN_ROLE_ID := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @PUBLIC_TENANT_ID AND role_key = 'admin' ORDER BY id ASC LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id ASC LIMIT 1),
  1993479637311279107
);

-- 避免历史固定 ID 数据导致 AUTO_INCREMENT 落后于 MAX(id)，从而继续撞主键。
SET @LABEL_TYPE_MENU_ID := 775;
SET @LABEL_FIELD_MENU_ID := 776;

SET @NEXT_MENU_AI := GREATEST(
  COALESCE((SELECT MAX(id) + 1 FROM `sys_menu`), 1),
  @LABEL_FIELD_MENU_ID + 1
);

SET @SQL := CONCAT('ALTER TABLE `sys_menu` AUTO_INCREMENT = ', @NEXT_MENU_AI);
PREPARE stmt FROM @SQL;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 标签类型 / 标签字段菜单
-- ----------------------------
INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @LABEL_TYPE_MENU_ID, @PUBLIC_TENANT_ID, 'PUBLIC', @LABEL_MODULE_ID, @LABEL_PARENT_ID, 'menu', 'type', '标签类型',
       JSON_OBJECT('zh-CN','标签类型','zh-TW','標籤類型','en-US','Label Type','ja-JP','ラベルタイプ','ko-KR','라벨 유형'),
       'TagsOutlined', 'LabelType', 'label:type:view', 5, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 2, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = @LABEL_PARENT_ID AND m.component_key = 'LabelType'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @LABEL_FIELD_MENU_ID, @PUBLIC_TENANT_ID, 'PUBLIC', @LABEL_MODULE_ID, @LABEL_PARENT_ID, 'menu', 'field', '标签字段',
       JSON_OBJECT('zh-CN','标签字段','zh-TW','標籤字段','en-US','Label Field','ja-JP','ラベルフィールド','ko-KR','라벨 필드'),
       'ProfileOutlined', 'LabelField', 'label:field:view', 6, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 2, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = @LABEL_PARENT_ID AND m.component_key = 'LabelField'
);

-- ----------------------------
-- 标签模块权限
-- ----------------------------
INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.permission_name, seed.permission_key, seed.url, 'POST', 0, @NOW, @NOW, 0
FROM (
  SELECT '标签类型查询' permission_name, 'label:type:query' permission_key, '/basic/label/type/page' url UNION ALL
  SELECT '标签类型新增', 'label:type:add', '/basic/label/type/add' UNION ALL
  SELECT '标签类型编辑', 'label:type:edit', '/basic/label/type/update' UNION ALL
  SELECT '标签类型删除', 'label:type:delete', '/basic/label/type/delete' UNION ALL
  SELECT '标签类型启停', 'label:type:enable', '/basic/label/type/enable' UNION ALL
  SELECT '标签字段查询', 'label:field:query', '/basic/label/field/page' UNION ALL
  SELECT '标签字段新增', 'label:field:add', '/basic/label/field/add' UNION ALL
  SELECT '标签字段编辑', 'label:field:edit', '/basic/label/field/update' UNION ALL
  SELECT '标签字段删除', 'label:field:delete', '/basic/label/field/delete' UNION ALL
  SELECT '标签字段启停', 'label:field:enable', '/basic/label/field/enable' UNION ALL
  SELECT '标签字段导入', 'label:field:import', '/basic/label/field/import' UNION ALL
  SELECT '标签模板查询', 'label:template:query', '/basic/label/template/page' UNION ALL
  SELECT '标签模板新增', 'label:template:add', '/basic/label/template/add' UNION ALL
  SELECT '标签模板编辑', 'label:template:edit', '/basic/label/template/update' UNION ALL
  SELECT '标签模板删除', 'label:template:delete', '/basic/label/template/delete' UNION ALL
  SELECT '标签模板批量删除', 'label:template:batchDelete', '/basic/label/template/batchDelete' UNION ALL
  SELECT '标签模板设为默认', 'label:template:setDefault', '/basic/label/template/setDefault' UNION ALL
  SELECT '标签模板预览', 'label:template:preview', '/basic/label/template/preview' UNION ALL
  SELECT '标签模板设计详情', 'label:template:designDetail', '/basic/label/template/design/detail' UNION ALL
  SELECT '标签模板设计保存', 'label:template:designSave', '/basic/label/template/design/save' UNION ALL
  SELECT '标签打印查询', 'label:print:query', '/basic/label/print/preview' UNION ALL
  SELECT '标签打印执行', 'label:print:execute', '/basic/label/print/execute' UNION ALL
  SELECT '标签打印渲染', 'label:print:render', '/basic/label/print/render' UNION ALL
  SELECT '标签打印重打', 'label:print:reprint', '/basic/label/record/reprint' UNION ALL
  SELECT '标签打印记录查询', 'label:record:query', '/basic/label/record/page' UNION ALL
  SELECT '标签打印记录详情', 'label:record:detail', '/basic/label/record/detail' UNION ALL
  SELECT '标签绑定查询', 'label:binding:query', '/basic/label/binding/page' UNION ALL
  SELECT '标签绑定新增', 'label:binding:add', '/basic/label/binding/add' UNION ALL
  SELECT '标签绑定编辑', 'label:binding:edit', '/basic/label/binding/update' UNION ALL
  SELECT '标签绑定删除', 'label:binding:delete', '/basic/label/binding/delete' UNION ALL
  SELECT '标签绑定匹配', 'label:binding:match', '/basic/label/binding/match'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.deleted = 0 AND p.permission_key = seed.permission_key
);

-- ----------------------------
-- 默认管理员角色授权
-- ----------------------------
INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT DISTINCT @PUBLIC_TENANT_ID, @ADMIN_ROLE_ID, m.id
FROM `sys_menu` m
WHERE m.deleted = 0
  AND (
    m.component_key IN ('LabelType', 'LabelField')
    OR m.perm_key LIKE 'label:type:%'
    OR m.perm_key LIKE 'label:field:%'
    OR m.perm_key LIKE 'label:template:%'
    OR m.perm_key LIKE 'label:print:%'
    OR m.perm_key LIKE 'label:record:%'
    OR m.perm_key LIKE 'label:binding:%'
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @PUBLIC_TENANT_ID
      AND rm.role_id = @ADMIN_ROLE_ID
      AND rm.menu_id = m.id
  );

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @ADMIN_ROLE_ID, p.id
FROM `sys_permission` p
WHERE p.deleted = 0
  AND (
    p.permission_key LIKE 'label:type:%'
    OR p.permission_key LIKE 'label:field:%'
    OR p.permission_key LIKE 'label:template:%'
    OR p.permission_key LIKE 'label:print:%'
    OR p.permission_key LIKE 'label:record:%'
    OR p.permission_key LIKE 'label:binding:%'
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.role_id = @ADMIN_ROLE_ID
      AND rp.permission_id = p.id
  );
