-- 人员、车间、班次、班组基础数据升级脚本。
-- 脚本支持重复执行。
-- 目标数据库：forgex_admin、forgex_common、forgex_integration。
-- 请使用 UTF-8 / utf8mb4 客户端执行，避免中文注释乱码。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260517_basic_employee_shift_team';
SET @now := NOW();
SET @public_tenant_id := 1993479636925403138;

CREATE TABLE IF NOT EXISTS `basic_workshop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `workshop_code` varchar(50) NOT NULL COMMENT '车间编码',
  `workshop_name` varchar(100) NOT NULL COMMENT '车间名称',
  `factory_id` bigint DEFAULT NULL COMMENT '所属工厂ID',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_workshop_code` (`tenant_id`, `workshop_code`, `deleted`),
  KEY `idx_basic_workshop_factory` (`tenant_id`, `factory_id`, `deleted`),
  KEY `idx_basic_workshop_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='车间主数据表';

CREATE TABLE IF NOT EXISTS `basic_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `employee_no` varchar(50) NOT NULL COMMENT '工号',
  `employee_name` varchar(100) NOT NULL COMMENT '人员姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT 0 COMMENT '性别：0=未知，1=男，2=女',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像地址',
  `entry_date` date DEFAULT NULL COMMENT '入职日期',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '岗位ID',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_employee_no` (`tenant_id`, `employee_no`, `deleted`),
  KEY `idx_basic_employee_dept` (`tenant_id`, `department_id`, `deleted`),
  KEY `idx_basic_employee_position` (`tenant_id`, `position_id`, `deleted`),
  KEY `idx_basic_employee_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='人员主数据表';

CREATE TABLE IF NOT EXISTS `basic_shift` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `shift_name` varchar(100) NOT NULL COMMENT '班次名称',
  `shift_code` varchar(50) NOT NULL COMMENT '班次编码',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_shift_code` (`tenant_id`, `shift_code`, `deleted`),
  KEY `idx_basic_shift_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次主数据表';

CREATE TABLE IF NOT EXISTS `basic_shift_period` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `shift_id` bigint NOT NULL COMMENT '班次ID',
  `time_type` varchar(20) NOT NULL COMMENT '时间类型：WORK=工作，REST=休息',
  `start_time` time NOT NULL COMMENT '开始时间',
  `end_time` time NOT NULL COMMENT '结束时间',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_shift_period_shift` (`tenant_id`, `shift_id`, `deleted`),
  KEY `idx_basic_shift_period_order` (`shift_id`, `sort_order`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班次时段明细表';

CREATE TABLE IF NOT EXISTS `basic_team` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `team_code` varchar(50) NOT NULL COMMENT '班组编码',
  `team_name` varchar(100) NOT NULL COMMENT '班组名称',
  `leader_employee_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `current_shift_id` bigint DEFAULT NULL COMMENT '当前负责班次ID',
  `workshop_id` bigint DEFAULT NULL COMMENT '所属车间ID',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用：0=禁用，1=启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_team_code` (`tenant_id`, `team_code`, `deleted`),
  KEY `idx_basic_team_leader` (`tenant_id`, `leader_employee_id`, `deleted`),
  KEY `idx_basic_team_shift` (`tenant_id`, `current_shift_id`, `deleted`),
  KEY `idx_basic_team_workshop` (`tenant_id`, `workshop_id`, `deleted`),
  KEY `idx_basic_team_status` (`tenant_id`, `status`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组主数据表';

CREATE TABLE IF NOT EXISTS `basic_team_employee` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `team_id` bigint NOT NULL COMMENT '班组ID',
  `employee_id` bigint NOT NULL COMMENT '人员ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_team_employee` (`tenant_id`, `team_id`, `employee_id`, `deleted`),
  KEY `idx_basic_team_employee_team` (`tenant_id`, `team_id`, `deleted`),
  KEY `idx_basic_team_employee_employee` (`tenant_id`, `employee_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班组人员关联表';

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE `sys_user` ADD COLUMN `employee_id` bigint DEFAULT NULL COMMENT ''人员ID'' AFTER `position_id`',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND COLUMN_NAME = 'employee_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @sql := (
  SELECT IF(
    COUNT(*) = 0,
    'CREATE INDEX `idx_employee_id` ON `sys_user` (`employee_id`)',
    'SELECT 1'
  )
  FROM INFORMATION_SCHEMA.STATISTICS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'sys_user'
    AND INDEX_NAME = 'idx_employee_id'
);
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @basic_module_id := COALESCE(
  (SELECT id FROM `sys_module` WHERE deleted = 0 AND code = 'basic' ORDER BY id LIMIT 1),
  (SELECT module_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'BasicDashboard' ORDER BY id LIMIT 1),
  (SELECT module_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'BasicSupplier' ORDER BY id LIMIT 1),
  5
);
SET @basic_parent_id := 0;
SET @admin_role_id := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @public_tenant_id AND role_key = 'admin' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id LIMIT 1)
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, @basic_parent_id, 'menu', item.path, item.name, item.name_i18n_json, item.icon, item.component_key, item.perm_key,
       item.order_num, 1, 1, @now, @script_user, @now, @script_user, 0, 1, 'embedded', NULL
FROM (
  SELECT 'workshop' path, '车间管理' name, '{"zh-CN":"车间管理","zh-TW":"車間管理","en-US":"Workshop","ja-JP":"職場管理","ko-KR":"작업장 관리"}' name_i18n_json, 'HomeOutlined' icon, 'BasicWorkshop' component_key, 'basic:workshop:query' perm_key, 62 order_num
  UNION ALL SELECT 'employee', '人员管理', '{"zh-CN":"人员管理","zh-TW":"人員管理","en-US":"Employee","ja-JP":"人員管理","ko-KR":"인원 관리"}', 'UserOutlined', 'BasicEmployee', 'basic:employee:query', 63
  UNION ALL SELECT 'shift', '班次管理', '{"zh-CN":"班次管理","zh-TW":"班次管理","en-US":"Shift","ja-JP":"シフト管理","ko-KR":"교대조 관리"}', 'ClockCircleOutlined', 'BasicShift', 'basic:shift:query', 64
  UNION ALL SELECT 'team', '班组管理', '{"zh-CN":"班组管理","zh-TW":"班組管理","en-US":"Team","ja-JP":"班組管理","ko-KR":"반 관리"}', 'TeamOutlined', 'BasicTeam', 'basic:team:query', 65
) item
WHERE @basic_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` existing
    WHERE existing.deleted = 0
      AND existing.component_key = item.component_key
  );

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT parent.tenant_id, parent.tenant_type, parent.module_id, parent.id, 'button', item.path, item.name, item.name_i18n_json, NULL, item.component_key,
       item.perm_key, item.order_num, 0, 1, @now, @script_user, @now, @script_user, 0, 3, 'embedded', NULL
FROM (
  SELECT 'BasicWorkshop' parent_key, 'query' path, '车间查询' name, '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}' name_i18n_json, NULL component_key, 'basic:workshop:query' perm_key, 1 order_num
  UNION ALL SELECT 'BasicWorkshop', 'add', '车间新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:workshop:add', 2
  UNION ALL SELECT 'BasicWorkshop', 'edit', '车间编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:workshop:edit', 3
  UNION ALL SELECT 'BasicWorkshop', 'delete', '车间删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:workshop:delete', 4
  UNION ALL SELECT 'BasicEmployee', 'query', '人员查询', '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}', NULL, 'basic:employee:query', 1
  UNION ALL SELECT 'BasicEmployee', 'add', '人员新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:employee:add', 2
  UNION ALL SELECT 'BasicEmployee', 'edit', '人员编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:employee:edit', 3
  UNION ALL SELECT 'BasicEmployee', 'delete', '人员删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:employee:delete', 4
  UNION ALL SELECT 'BasicEmployee', 'syncUser', '同步用户', '{"zh-CN":"同步用户","zh-TW":"同步使用者","en-US":"Sync User","ja-JP":"ユーザー同期","ko-KR":"사용자 동기화"}', NULL, 'basic:employee:syncUser', 5
  UNION ALL SELECT 'BasicEmployee', 'pullThirdParty', '从第三方拉取', '{"zh-CN":"从第三方拉取","zh-TW":"從第三方拉取","en-US":"Pull Third Party","ja-JP":"外部から取得","ko-KR":"외부에서 가져오기"}', NULL, 'basic:employee:pullThirdParty', 6
  UNION ALL SELECT 'BasicEmployee', 'sync', '同步到第三方', '{"zh-CN":"同步到第三方","zh-TW":"同步到第三方","en-US":"Sync Third Party","ja-JP":"外部へ同期","ko-KR":"외부로 동기화"}', NULL, 'basic:employee:sync', 7
  UNION ALL SELECT 'BasicShift', 'query', '班次查询', '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}', NULL, 'basic:shift:query', 1
  UNION ALL SELECT 'BasicShift', 'add', '班次新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:shift:add', 2
  UNION ALL SELECT 'BasicShift', 'edit', '班次编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:shift:edit', 3
  UNION ALL SELECT 'BasicShift', 'delete', '班次删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:shift:delete', 4
  UNION ALL SELECT 'BasicTeam', 'query', '班组查询', '{"zh-CN":"查询","zh-TW":"查詢","en-US":"Query","ja-JP":"検索","ko-KR":"조회"}', NULL, 'basic:team:query', 1
  UNION ALL SELECT 'BasicTeam', 'add', '班组新增', '{"zh-CN":"新增","zh-TW":"新增","en-US":"Add","ja-JP":"追加","ko-KR":"추가"}', NULL, 'basic:team:add', 2
  UNION ALL SELECT 'BasicTeam', 'edit', '班组编辑', '{"zh-CN":"编辑","zh-TW":"編輯","en-US":"Edit","ja-JP":"編集","ko-KR":"편집"}', NULL, 'basic:team:edit', 3
  UNION ALL SELECT 'BasicTeam', 'delete', '班组删除', '{"zh-CN":"删除","zh-TW":"刪除","en-US":"Delete","ja-JP":"削除","ko-KR":"삭제"}', NULL, 'basic:team:delete', 4
) item
JOIN `sys_menu` parent ON parent.deleted = 0 AND parent.component_key = item.parent_key
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` existing
  WHERE existing.deleted = 0
    AND existing.perm_key = item.perm_key
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT item.permission_name, item.permission_key, item.url, 'POST', 0, @now, @now, 0
FROM (
  SELECT '车间查询' permission_name, 'basic:workshop:query' permission_key, '/basic/workshop/page' url
  UNION ALL SELECT '车间新增', 'basic:workshop:add', '/basic/workshop/create'
  UNION ALL SELECT '车间编辑', 'basic:workshop:edit', '/basic/workshop/update'
  UNION ALL SELECT '车间删除', 'basic:workshop:delete', '/basic/workshop/delete'
  UNION ALL SELECT '人员查询', 'basic:employee:query', '/basic/employee/page'
  UNION ALL SELECT '人员新增', 'basic:employee:add', '/basic/employee/create'
  UNION ALL SELECT '人员编辑', 'basic:employee:edit', '/basic/employee/update'
  UNION ALL SELECT '人员删除', 'basic:employee:delete', '/basic/employee/delete'
  UNION ALL SELECT '同步用户', 'basic:employee:syncUser', '/basic/employee/sync-user'
  UNION ALL SELECT '从第三方拉取人员', 'basic:employee:pullThirdParty', '/basic/employee/pull-from-third-party'
  UNION ALL SELECT '同步人员到第三方', 'basic:employee:sync', '/basic/employee/sync-third-party'
  UNION ALL SELECT '班次查询', 'basic:shift:query', '/basic/shift/page'
  UNION ALL SELECT '班次新增', 'basic:shift:add', '/basic/shift/create'
  UNION ALL SELECT '班次编辑', 'basic:shift:edit', '/basic/shift/update'
  UNION ALL SELECT '班次删除', 'basic:shift:delete', '/basic/shift/delete'
  UNION ALL SELECT '班组查询', 'basic:team:query', '/basic/team/page'
  UNION ALL SELECT '班组新增', 'basic:team:add', '/basic/team/create'
  UNION ALL SELECT '班组编辑', 'basic:team:edit', '/basic/team/update'
  UNION ALL SELECT '班组删除', 'basic:team:delete', '/basic/team/delete'
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
  AND p.permission_key IN (
    'basic:workshop:query','basic:workshop:add','basic:workshop:edit','basic:workshop:delete',
    'basic:employee:query','basic:employee:add','basic:employee:edit','basic:employee:delete','basic:employee:syncUser','basic:employee:pullThirdParty','basic:employee:sync',
    'basic:shift:query','basic:shift:add','basic:shift:edit','basic:shift:delete',
    'basic:team:query','basic:team:add','basic:team:edit','basic:team:delete'
  )
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
    m.component_key IN ('BasicWorkshop','BasicEmployee','BasicShift','BasicTeam')
    OR m.perm_key IN (
      'basic:workshop:query','basic:workshop:add','basic:workshop:edit','basic:workshop:delete',
      'basic:employee:query','basic:employee:add','basic:employee:edit','basic:employee:delete','basic:employee:syncUser','basic:employee:pullThirdParty','basic:employee:sync',
      'basic:shift:query','basic:shift:add','basic:shift:edit','basic:shift:delete',
      'basic:team:query','basic:team:add','basic:team:edit','basic:team:delete'
    )
  )
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @public_tenant_id AND rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

USE `forgex_common`;

SET @script_user := '20260517_basic_employee_shift_team';
SET @now := NOW();

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.table_name_i18n_json, 'BUSINESS', 'id', 10, 1, 1, @script_user, @now, @script_user, @now, 0
FROM (
  SELECT 'BasicWorkshopTable' table_code, '{"zh-CN":"车间管理","zh-TW":"車間管理","en-US":"Workshop","ja-JP":"職場管理","ko-KR":"작업장 관리"}' table_name_i18n_json
  UNION ALL SELECT 'BasicEmployeeTable', '{"zh-CN":"人员管理","zh-TW":"人員管理","en-US":"Employee","ja-JP":"人員管理","ko-KR":"인원 관리"}'
  UNION ALL SELECT 'BasicShiftTable', '{"zh-CN":"班次管理","zh-TW":"班次管理","en-US":"Shift","ja-JP":"シフト管理","ko-KR":"교대조 관리"}'
  UNION ALL SELECT 'BasicTeamTable', '{"zh-CN":"班组管理","zh-TW":"班組管理","en-US":"Team","ja-JP":"班組管理","ko-KR":"반 관리"}'
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = item.table_code
    AND existing.deleted = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, item.table_code, item.field, item.title_i18n_json, item.align, item.width, item.fixed, item.ellipsis, item.sortable, item.sorter_field,
       item.queryable, item.query_type, item.query_operator, item.dict_code, item.render_type, NULL, item.order_num, 1, @script_user, @now, @script_user, @now, 0
FROM (
  SELECT 'BasicWorkshopTable' table_code, 'workshopName' field, '{"zh-CN":"车间名称","zh-TW":"車間名稱","en-US":"Workshop","ja-JP":"職場名","ko-KR":"작업장명"}' title_i18n_json, 'left' align, 220 width, NULL fixed, 1 ellipsis, 1 sortable, 'workshopName' sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, NULL render_type, 1 order_num
  UNION ALL SELECT 'BasicWorkshopTable','factoryName','{"zh-CN":"所属工厂","zh-TW":"所屬工廠","en-US":"Factory","ja-JP":"所属工場","ko-KR":"소속 공장"}','left',180,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,2
  UNION ALL SELECT 'BasicWorkshopTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',3
  UNION ALL SELECT 'BasicWorkshopTable','remark','{"zh-CN":"备注","zh-TW":"備註","en-US":"Remark","ja-JP":"備考","ko-KR":"비고"}','left',220,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicWorkshopTable','createTime','{"zh-CN":"创建时间","zh-TW":"建立時間","en-US":"Create Time","ja-JP":"作成日時","ko-KR":"생성 시간"}','center',180,NULL,0,1,'createTime',0,NULL,NULL,NULL,NULL,5
  UNION ALL SELECT 'BasicWorkshopTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  UNION ALL SELECT 'BasicEmployeeTable','employeeName','{"zh-CN":"姓名","zh-TW":"姓名","en-US":"Employee","ja-JP":"氏名","ko-KR":"이름"}','left',220,NULL,1,1,'employeeName',1,'input','like',NULL,NULL,1
  UNION ALL SELECT 'BasicEmployeeTable','phone','{"zh-CN":"手机号","zh-TW":"手機號","en-US":"Phone","ja-JP":"携帯番号","ko-KR":"휴대폰 번호"}','left',140,NULL,0,1,'phone',1,'input','like',NULL,NULL,2
  UNION ALL SELECT 'BasicEmployeeTable','gender','{"zh-CN":"性别","zh-TW":"性別","en-US":"Gender","ja-JP":"性別","ko-KR":"성별"}','center',100,NULL,0,1,'gender',0,NULL,NULL,NULL,NULL,3
  UNION ALL SELECT 'BasicEmployeeTable','departmentName','{"zh-CN":"部门","zh-TW":"部門","en-US":"Department","ja-JP":"部門","ko-KR":"부서"}','left',160,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicEmployeeTable','positionName','{"zh-CN":"岗位","zh-TW":"崗位","en-US":"Position","ja-JP":"職位","ko-KR":"직위"}','left',160,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,5
  UNION ALL SELECT 'BasicEmployeeTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',6
  UNION ALL SELECT 'BasicEmployeeTable','userId','{"zh-CN":"用户同步","zh-TW":"使用者同步","en-US":"User Sync","ja-JP":"ユーザー同期","ko-KR":"사용자 동기화"}','center',110,NULL,0,0,NULL,0,NULL,NULL,NULL,NULL,7
  UNION ALL SELECT 'BasicEmployeeTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',260,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  UNION ALL SELECT 'BasicShiftTable','shiftName','{"zh-CN":"班次名称","zh-TW":"班次名稱","en-US":"Shift","ja-JP":"シフト名","ko-KR":"교대조명"}','left',220,NULL,1,1,'shiftName',1,'input','like',NULL,NULL,1
  UNION ALL SELECT 'BasicShiftTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',2
  UNION ALL SELECT 'BasicShiftTable','remark','{"zh-CN":"备注","zh-TW":"備註","en-US":"Remark","ja-JP":"備考","ko-KR":"비고"}','left',220,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,3
  UNION ALL SELECT 'BasicShiftTable','createTime','{"zh-CN":"创建时间","zh-TW":"建立時間","en-US":"Create Time","ja-JP":"作成日時","ko-KR":"생성 시간"}','center',180,NULL,0,1,'createTime',0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicShiftTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
  UNION ALL SELECT 'BasicTeamTable','teamName','{"zh-CN":"班组名称","zh-TW":"班組名稱","en-US":"Team","ja-JP":"班組名","ko-KR":"반 이름"}','left',220,NULL,1,1,'teamName',1,'input','like',NULL,NULL,1
  UNION ALL SELECT 'BasicTeamTable','leaderEmployeeName','{"zh-CN":"负责人","zh-TW":"負責人","en-US":"Leader","ja-JP":"責任者","ko-KR":"책임자"}','left',160,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,2
  UNION ALL SELECT 'BasicTeamTable','currentShiftName','{"zh-CN":"当前班次","zh-TW":"目前班次","en-US":"Current Shift","ja-JP":"現在のシフト","ko-KR":"현재 교대조"}','left',160,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,3
  UNION ALL SELECT 'BasicTeamTable','workshopName','{"zh-CN":"所属车间","zh-TW":"所屬車間","en-US":"Workshop","ja-JP":"所属職場","ko-KR":"소속 작업장"}','left',160,NULL,1,0,NULL,0,NULL,NULL,NULL,NULL,4
  UNION ALL SELECT 'BasicTeamTable','status','{"zh-CN":"状态","zh-TW":"狀態","en-US":"Status","ja-JP":"状態","ko-KR":"상태"}','center',100,NULL,0,1,'status',1,'select','eq','common_status','tag',5
  UNION ALL SELECT 'BasicTeamTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,99
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
SELECT cfg.`tenant_id`, pub.`table_code`, pub.`field`, pub.`title_i18n_json`, pub.`align`, pub.`width`, pub.`fixed`, pub.`ellipsis`, pub.`sortable`, pub.`sorter_field`, pub.`queryable`, pub.`query_type`, pub.`query_operator`, pub.`dict_code`, pub.`render_type`, pub.`perm_key`, pub.`order_num`, pub.`enabled`, @script_user, @now, @script_user, @now, 0
FROM `fx_table_config` cfg
JOIN `fx_table_column_config` pub
  ON pub.`tenant_id` = 0
 AND pub.`table_code` = cfg.`table_code`
 AND pub.`deleted` = 0
WHERE cfg.`tenant_id` <> 0
  AND cfg.`deleted` = 0
  AND cfg.`table_code` IN ('BasicWorkshopTable','BasicEmployeeTable','BasicShiftTable','BasicTeamTable')
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` tgt
    WHERE tgt.`tenant_id` = cfg.`tenant_id`
      AND tgt.`table_code` = pub.`table_code`
      AND tgt.`field` = pub.`field`
      AND tgt.`deleted` = 0
  );

USE `forgex_integration`;

SET @script_user := '20260517_basic_employee_shift_team';
SET @now := NOW();
SET @tenant_id := 1993479636925403138;

INSERT INTO `fx_api_config` (
  api_code, api_name, api_desc, direction, api_path, processor_bean, call_method,
  http_method, invoke_mode, content_type, target_url, timeout_ms, retry_count,
  retry_interval_ms, max_concurrent, queue_limit, auth_type, auth_config,
  call_count, status, module_code, tenant_id, create_time, create_by,
  update_time, update_by, deleted
)
SELECT item.api_code, item.api_name, item.api_desc, item.direction, item.api_path, item.processor_bean, item.call_method,
       'POST', 'SYNC', 'application/json', NULL, 30000, 0,
       0, 0, 0, NULL, NULL,
       0, 1, 'basic', @tenant_id, @now, @script_user,
       @now, @script_user, 0
FROM (
  SELECT 'basic_employee_sync' api_code, '人员同步到第三方' api_name, '将 Forgex 人员主数据推送到已配置的第三方系统。' api_desc, 'OUTBOUND' direction, NULL api_path, NULL processor_bean, 'HTTP' call_method
  UNION ALL SELECT 'basic_employee_pull', '从第三方拉取人员', '从已配置的第三方系统拉取人员主数据并写入基础数据。', 'OUTBOUND', NULL, NULL, 'HTTP'
  UNION ALL SELECT 'basic_employee_third_party_inbound', '第三方人员入站', '接收第三方系统推送的人员主数据。', 'INBOUND', '/openapi/basic/employee/inbound', 'employeeThirdPartyInboundInterpreter', 'HTTP'
) item
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_api_config` existing
  WHERE existing.deleted = 0
    AND existing.api_code = item.api_code
    AND existing.tenant_id = @tenant_id
);

SET @employee_sync_config_id := (
  SELECT id FROM `fx_api_config`
  WHERE deleted = 0 AND api_code = 'basic_employee_sync' AND tenant_id = @tenant_id
  ORDER BY id LIMIT 1
);
SET @employee_pull_config_id := (
  SELECT id FROM `fx_api_config`
  WHERE deleted = 0 AND api_code = 'basic_employee_pull' AND tenant_id = @tenant_id
  ORDER BY id LIMIT 1
);
SET @employee_inbound_config_id := (
  SELECT id FROM `fx_api_config`
  WHERE deleted = 0 AND api_code = 'basic_employee_third_party_inbound' AND tenant_id = @tenant_id
  ORDER BY id LIMIT 1
);
SET @employee_third_system_id := (
  SELECT COALESCE(
    (SELECT id FROM `fx_third_system` WHERE deleted = 0 AND tenant_id = @tenant_id ORDER BY id ASC LIMIT 1),
    (SELECT id FROM `fx_third_system` WHERE deleted = 0 ORDER BY id ASC LIMIT 1)
  )
);
SET @employee_sync_target_id := (
  SELECT COALESCE(
    (SELECT id FROM `fx_api_outbound_target` WHERE deleted = 0 AND api_config_id = @employee_sync_config_id LIMIT 1),
    (SELECT COALESCE(MAX(id), 0) + 1 FROM `fx_api_outbound_target`)
  )
);
SET @employee_pull_target_id := (
  SELECT COALESCE(
    (SELECT id FROM `fx_api_outbound_target` WHERE deleted = 0 AND api_config_id = @employee_pull_config_id LIMIT 1),
    (SELECT GREATEST(COALESCE(MAX(id), 0) + 1, @employee_sync_target_id + 1) FROM `fx_api_outbound_target`)
  )
);

INSERT INTO `fx_api_outbound_target` (
  id, tenant_id, api_config_id, third_system_id, target_code, target_name, target_url,
  http_method, content_type, invoke_mode, timeout_ms, retry_count, retry_interval_ms,
  order_num, status, remark, create_time, create_by, update_time, update_by, deleted
)
SELECT item.id, @tenant_id, item.api_config_id, ts.id, ts.system_code, ts.system_name, item.target_url,
       'POST', 'application/json', 'SYNC', 30000, 0, 0,
       1, 1, item.remark, @now, @script_user, @now, @script_user, 0
FROM (
  SELECT @employee_sync_target_id id, @employee_sync_config_id api_config_id, '/api/employees/third-party/sync' target_url, '人员同步到第三方的默认目标地址，请按实际第三方系统调整 target_url。' remark
  UNION ALL SELECT @employee_pull_target_id, @employee_pull_config_id, '/api/employees/third-party/pull', '从第三方拉取人员的默认目标地址，请按实际第三方系统调整 target_url。'
) item
JOIN `fx_third_system` ts ON ts.id = @employee_third_system_id
WHERE item.api_config_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `fx_api_outbound_target` existing
    WHERE existing.deleted = 0
      AND existing.api_config_id = item.api_config_id
      AND existing.third_system_id = ts.id
  );

INSERT INTO `fx_api_param_mapping` (
  api_config_id, outbound_target_id, source_field_path, target_field_path,
  transform_rule, default_value, constant_value, target_scope, value_type,
  direction, remark, tenant_id, create_time, create_by, update_time, update_by, deleted
)
SELECT item.api_config_id, item.outbound_target_id, item.source_field_path, item.target_field_path,
       NULL, NULL, NULL, 'BODY', 'SOURCE',
       item.direction, item.remark, @tenant_id, @now, @script_user, @now, @script_user, 0
FROM (
  SELECT @employee_sync_config_id api_config_id, @employee_sync_target_id outbound_target_id, 'tenantId' source_field_path, 'tenantId' target_field_path, 'OUTBOUND' direction, '映射租户ID' remark
  UNION ALL SELECT @employee_sync_config_id, @employee_sync_target_id, 'employees', 'employees', 'OUTBOUND', '映射人员列表'
  UNION ALL SELECT @employee_pull_config_id, @employee_pull_target_id, 'tenantId', 'tenantId', 'OUTBOUND', '映射租户ID'
  UNION ALL SELECT @employee_pull_config_id, @employee_pull_target_id, 'employees', 'employees', 'INBOUND', '映射人员列表'
  UNION ALL SELECT @employee_inbound_config_id, NULL, 'tenantId', 'tenantId', 'INBOUND', '映射入站租户ID'
  UNION ALL SELECT @employee_inbound_config_id, NULL, 'employees', 'employees', 'INBOUND', '映射入站人员列表'
) item
WHERE item.api_config_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `fx_api_param_mapping` existing
    WHERE existing.deleted = 0
      AND existing.api_config_id = item.api_config_id
      AND ((existing.outbound_target_id = item.outbound_target_id) OR (existing.outbound_target_id IS NULL AND item.outbound_target_id IS NULL))
      AND existing.source_field_path = item.source_field_path
      AND existing.target_field_path = item.target_field_path
      AND existing.direction = item.direction
  );

SELECT 'basic_employee_shift_team_upgrade_done' AS result;
