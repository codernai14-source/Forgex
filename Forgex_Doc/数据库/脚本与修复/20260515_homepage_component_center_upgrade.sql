-- 首页组件中心增强升级脚本
-- 说明：
-- 1. 新增首页组件目录相关三张表。
-- 2. 新增首页组件目录菜单、按钮权限和默认角色授权。
-- 3. 初始化公共首页组件目录数据。
-- 4. 脚本支持重复执行，所有写入均使用 NOT EXISTS 保护。

SET NAMES utf8mb4;

USE `forgex_admin`;

SET @OPERATOR := 'codex';
SET @NOW := NOW();
SET @SYS_TENANT_ID := COALESCE(
  (SELECT id FROM `sys_tenant` WHERE deleted = 0 AND tenant_type = 'MAIN_TENANT' ORDER BY id LIMIT 1),
  1993479636925403138
);
SET @SYS_MODULE_ID := COALESCE(
  (SELECT id FROM `sys_module` WHERE deleted = 0 AND tenant_id = @SYS_TENANT_ID AND code = 'sys' ORDER BY id LIMIT 1),
  1
);
SET @ONLINE_DEV_PARENT_ID := COALESCE(
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND tenant_id = @SYS_TENANT_ID AND type = 'catalog' AND path = 'onlineDev' ORDER BY id LIMIT 1),
  3000000000000000443
);
SET @ADMIN_ROLE_ID := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @SYS_TENANT_ID AND role_key = 'admin' ORDER BY id LIMIT 1),
  1993479637311279107
);

CREATE TABLE IF NOT EXISTS `sys_homepage_component_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类编码',
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_component_category_code`(`tenant_id` ASC, `module_code` ASC, `category_code` ASC) USING BTREE,
  INDEX `idx_component_category_module`(`module_code` ASC) USING BTREE,
  INDEX `idx_component_category_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页组件分类表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `sys_homepage_component_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `scope_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置层级：PUBLIC/TENANT',
  `component_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件编码',
  `component_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件名称',
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件路径',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `use_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作用说明',
  `default_params_json` json NULL COMMENT '默认参数JSON',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_component_scope_code`(`scope_level` ASC, `tenant_id` ASC, `component_code` ASC) USING BTREE,
  INDEX `idx_component_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_component_scope_tenant`(`scope_level` ASC, `tenant_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页组件公共/租户配置表' ROW_FORMAT=DYNAMIC;

CREATE TABLE IF NOT EXISTS `sys_homepage_component_preference`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `source_component_id` bigint NULL DEFAULT NULL COMMENT '来源组件ID',
  `component_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件编码快照',
  `component_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件名称快照',
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件路径快照',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标快照',
  `use_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作用说明快照',
  `favorite` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否收藏',
  `removed` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否移除',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `params_json` json NULL COMMENT '个人参数JSON',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_component_preference_code`(`tenant_id` ASC, `user_id` ASC, `component_code` ASC) USING BTREE,
  INDEX `idx_component_preference_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_component_preference_user`(`tenant_id` ASC, `user_id` ASC) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='首页组件个人偏好表' ROW_FORMAT=DYNAMIC;

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_homepage_component_category', 1, '首页组件分类配置使用公共租户数据加载', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_homepage_component_category'
);

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_homepage_component_config', 1, '首页组件公共/租户配置由业务层显式控制范围', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_homepage_component_config'
);

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_homepage_component_preference', 1, '首页组件个人偏好表按用户和租户保存快照', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_homepage_component_preference'
);

INSERT INTO `sys_homepage_component_category`
(`tenant_id`,`category_code`,`category_name`,`module_code`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, 'personal_common', '通用组件', 'personal', '个人首页通用组件分类', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_category` c
  WHERE c.tenant_id = 0 AND c.module_code = 'personal' AND c.category_code = 'personal_common'
);

SET @HOMEPAGE_CATEGORY_ID := (
  SELECT c.id
  FROM `sys_homepage_component_category` c
  WHERE c.tenant_id = 0 AND c.module_code = 'personal' AND c.category_code = 'personal_common'
  ORDER BY c.id
  LIMIT 1
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'commonMenus', '常用菜单', 'commonMenus', 'AppstoreOutlined', '系统自动统计的固定 Top 6 菜单', '{"limit":6,"showMore":true}', 1, 10, '首页常用菜单卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'commonMenus'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'myFavorites', '我的收藏', 'myFavorites', 'StarOutlined', '我主动收藏的快捷入口', '{"limit":6,"showMore":true}', 1, 20, '首页收藏卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'myFavorites'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'pendingApprovals', '我收到的审批', 'pendingApprovals', 'CheckCircleOutlined', '我收到的审批待办', '{"limit":6,"showMore":true}', 1, 30, '审批待办卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'pendingApprovals'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'calendar', '日历', 'calendar', 'CalendarOutlined', '本地日历视图', '{}', 1, 40, '日历卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'calendar'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'messages', '我收到的消息', 'messages', 'MessageOutlined', '用户发给我的站内消息', '{"limit":10,"showMore":true}', 1, 50, '消息卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'messages'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'notices', '系统通知', 'notices', 'BellOutlined', '审批与系统类通知', '{"limit":10,"showMore":true}', 1, 60, '系统通知卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'notices'
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, @HOMEPAGE_CATEGORY_ID, 'PUBLIC', 'currentTime', '当前时间', 'currentTime', 'ClockCircleOutlined', '当前日期与时间', '{}', 1, 70, '时间卡片', @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = 'currentTime'
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @SYS_TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @ONLINE_DEV_PARENT_ID, 'menu', 'homepage-component', '首页组件目录',
       JSON_OBJECT('zh-CN','首页组件目录','zh-TW','首頁組件目錄','en-US','Homepage Components','ja-JP','ホームページコンポーネント','ko-KR','홈페이지 컴포넌트'),
       'AppstoreOutlined', 'SystemHomepageComponent', 'sys:homepageComponent:view', 3, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 2, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m
  WHERE m.deleted = 0 AND m.tenant_id = @SYS_TENANT_ID AND m.component_key = 'SystemHomepageComponent'
);

UPDATE `sys_menu`
SET `parent_id` = @ONLINE_DEV_PARENT_ID,
    `path` = 'homepage-component',
    `module_id` = @SYS_MODULE_ID,
    `type` = 'menu',
    `order_num` = 3,
    `menu_level` = 2,
    `visible` = 1,
    `status` = 1,
    `update_time` = @NOW,
    `update_by` = @OPERATOR
WHERE `deleted` = 0
  AND `tenant_id` = @SYS_TENANT_ID
  AND `component_key` = 'SystemHomepageComponent'
  AND (
    `parent_id` <> @ONLINE_DEV_PARENT_ID
    OR `path` <> 'homepage-component'
    OR `order_num` <> 3
    OR `menu_level` <> 2
  );

SET @HOMEPAGE_COMPONENT_MENU_ID := (
  SELECT m.id
  FROM `sys_menu` m
  WHERE m.deleted = 0 AND m.tenant_id = @SYS_TENANT_ID AND m.component_key = 'SystemHomepageComponent'
  ORDER BY m.id
  LIMIT 1
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @SYS_TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @HOMEPAGE_COMPONENT_MENU_ID, 'button', seed.path, seed.name, seed.name_i18n_json,
       NULL, NULL, seed.perm_key, seed.order_num, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM (
  SELECT 'view' AS path, '查看' AS name, JSON_OBJECT('zh-CN','查看','zh-TW','查看','en-US','View','ja-JP','表示','ko-KR','조회') AS name_i18n_json, 'sys:homepageComponent:view' AS perm_key, 1 AS order_num
  UNION ALL SELECT 'add', '新增', JSON_OBJECT('zh-CN','新增','zh-TW','新增','en-US','Add','ja-JP','追加','ko-KR','추가'), 'sys:homepageComponent:add', 2
  UNION ALL SELECT 'edit', '编辑', JSON_OBJECT('zh-CN','编辑','zh-TW','編輯','en-US','Edit','ja-JP','編集','ko-KR','편집'), 'sys:homepageComponent:edit', 3
  UNION ALL SELECT 'delete', '删除', JSON_OBJECT('zh-CN','删除','zh-TW','刪除','en-US','Delete','ja-JP','削除','ko-KR','삭제'), 'sys:homepageComponent:delete', 4
  UNION ALL SELECT 'pull-public', '拉取公共配置', JSON_OBJECT('zh-CN','拉取公共配置','zh-TW','拉取公共配置','en-US','Pull Public Config','ja-JP','公開設定を取得','ko-KR','공용 설정 가져오기'), 'sys:homepageComponent:pullPublic', 5
) seed
WHERE @HOMEPAGE_COMPONENT_MENU_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` btn
    WHERE btn.deleted = 0 AND btn.parent_id = @HOMEPAGE_COMPONENT_MENU_ID AND btn.perm_key = seed.perm_key
  );

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '查看首页组件目录', 'sys:homepageComponent:view', '/sys/homepage/component/page', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:view'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '新增首页组件', 'sys:homepageComponent:add', '/sys/homepage/component/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:add'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '编辑首页组件', 'sys:homepageComponent:edit', '/sys/homepage/component/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:edit'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '删除首页组件', 'sys:homepageComponent:delete', '/sys/homepage/component/delete', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:delete'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '拉取公共首页组件', 'sys:homepageComponent:pullPublic', '/sys/homepage/component/pull-public', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:pullPublic'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '查看可用首页组件', 'sys:homepageComponent:effectiveList', '/sys/homepage/component/effective/list', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:effectiveList'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '收藏首页组件', 'sys:homepageComponent:favorite', '/sys/homepage/component/favorite', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:favorite'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '添加组件到个人首页', 'sys:homepageComponent:addToHomepage', '/sys/homepage/component/add', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:addToHomepage'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '移除首页组件', 'sys:homepageComponent:remove', '/sys/homepage/component/remove', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:remove'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '拉取租户首页组件', 'sys:homepageComponent:pullTenant', '/sys/homepage/component/pull-tenant', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:homepageComponent:pullTenant'
);

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @SYS_TENANT_ID, @ADMIN_ROLE_ID, m.id
FROM `sys_menu` m
WHERE m.deleted = 0
  AND m.tenant_id = @SYS_TENANT_ID
  AND (
    m.id = @ONLINE_DEV_PARENT_ID
    OR
    m.component_key = 'SystemHomepageComponent'
    OR m.perm_key LIKE 'sys:homepageComponent:%'
  )
  AND @ADMIN_ROLE_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @SYS_TENANT_ID AND rm.role_id = @ADMIN_ROLE_ID AND rm.menu_id = m.id
  );

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @ADMIN_ROLE_ID, p.id
FROM `sys_permission` p
WHERE p.deleted = 0
  AND p.permission_key LIKE 'sys:homepageComponent:%'
  AND @ADMIN_ROLE_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.role_id = @ADMIN_ROLE_ID AND rp.permission_id = p.id
  );

-- 2026-05-16 add complete homepage component catalog, split delete perms,
-- personal component config entry, and FxDynamicTable metadata. Safe to rerun.

INSERT INTO `sys_homepage_component_category`
(`tenant_id`,`category_code`,`category_name`,`module_code`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, seed.category_code, seed.category_name, seed.module_code, seed.remark, @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM (
  SELECT 'personal_common' AS category_code, 'Personal Common' AS category_name, 'personal' AS module_code, 'Personal homepage components' AS remark
  UNION ALL SELECT 'basic_common', 'Basic Data', 'basic', 'Basic data homepage components'
  UNION ALL SELECT 'approval_common', 'Approval', 'approval', 'Approval homepage components'
  UNION ALL SELECT 'sys_runtime', 'System Runtime', 'sys', 'System management homepage components'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_category` c
  WHERE c.tenant_id = 0 AND c.module_code = seed.module_code AND c.category_code = seed.category_code
);

INSERT INTO `sys_homepage_component_config`
(`tenant_id`,`category_id`,`scope_level`,`component_code`,`component_name`,`component_path`,`icon`,`use_desc`,`default_params_json`,`enabled`,`order_num`,`remark`,`create_time`,`update_time`,`create_by`,`update_by`,`deleted`)
SELECT 0, cat.id, 'PUBLIC', seed.component_code, seed.component_name, seed.component_path, seed.icon, seed.use_desc, seed.default_params_json, 1, seed.order_num, seed.remark, @NOW, @NOW, @OPERATOR, @OPERATOR, 0
FROM (
  SELECT 'personal' AS module_code, 'personal_common' AS category_code, 'commonMenus' AS component_code, 'Common Menus' AS component_name, 'commonMenus' AS component_path, 'AppstoreOutlined' AS icon, 'Top common menus' AS use_desc, '{"limit":6,"showMore":true}' AS default_params_json, 10 AS order_num, 'Personal homepage common menus' AS remark
  UNION ALL SELECT 'personal','personal_common','myFavorites','My Favorites','myFavorites','StarOutlined','User favorite menus','{"limit":6,"showMore":true}',20,'Personal homepage favorites'
  UNION ALL SELECT 'personal','personal_common','pendingApprovals','Pending Approvals','pendingApprovals','CheckCircleOutlined','Pending approval tasks','{"limit":6,"showMore":true}',30,'Personal homepage pending approvals'
  UNION ALL SELECT 'personal','personal_common','calendar','Calendar','calendar','CalendarOutlined','Calendar widget','{}',40,'Personal homepage calendar'
  UNION ALL SELECT 'personal','personal_common','messages','Messages','messages','MessageOutlined','User messages','{"limit":10,"showMore":true}',50,'Personal homepage messages'
  UNION ALL SELECT 'personal','personal_common','notices','Notices','notices','BellOutlined','System notices','{"limit":10,"showMore":true}',60,'Personal homepage notices'
  UNION ALL SELECT 'personal','personal_common','currentTime','Current Time','currentTime','ClockCircleOutlined','Current date and time','{}',70,'Personal homepage time'
  UNION ALL SELECT 'basic','basic_common','supplierInfo','Supplier Information','supplierInfo','TeamOutlined','Supplier master data','{}',10,'Basic homepage supplier information'
  UNION ALL SELECT 'basic','basic_common','encodeRuleInfo','Encoding Rules','encodeRuleInfo','NumberOutlined','Encoding rule configuration','{}',20,'Basic homepage encode rules'
  UNION ALL SELECT 'approval','approval_common','approvalWeeklyTrend','Approval Weekly Trend','approvalWeeklyTrend','LineChartOutlined','Approval weekly trend','{}',10,'Approval homepage weekly trend'
  UNION ALL SELECT 'approval','approval_common','approvalShortcuts','Approval Shortcuts','approvalShortcuts','ThunderboltOutlined','Approval shortcuts','{}',20,'Approval homepage shortcuts'
  UNION ALL SELECT 'approval','approval_common','approvalUserShare','Approval User Share','approvalUserShare','PieChartOutlined','Approval user share','{}',30,'Approval homepage user share'
  UNION ALL SELECT 'approval','approval_common','approvalTaskConfig','Approval Task Config','approvalTaskConfig','SettingOutlined','Approval task configuration','{}',40,'Approval homepage task config'
  UNION ALL SELECT 'approval','approval_common','approvalPending','Pending Tasks','approvalPending','ClockCircleOutlined','Pending approval tasks','{}',50,'Approval homepage pending tasks'
  UNION ALL SELECT 'approval','approval_common','approvalProcessed','Processed Tasks','approvalProcessed','CheckCircleOutlined','Processed approval tasks','{}',60,'Approval homepage processed tasks'
  UNION ALL SELECT 'approval','approval_common','approvalCc','CC Tasks','approvalCc','MailOutlined','Approval CC tasks','{}',70,'Approval homepage CC tasks'
  UNION ALL SELECT 'sys','sys_runtime','systemStats','System Stats','systemStats','DashboardOutlined','System statistics','{}',10,'System homepage statistics'
  UNION ALL SELECT 'sys','sys_runtime','systemCpu','CPU','systemCpu','FundProjectionScreenOutlined','CPU usage','{}',20,'System homepage CPU'
  UNION ALL SELECT 'sys','sys_runtime','systemMemory','Memory','systemMemory','DatabaseOutlined','Memory usage','{}',30,'System homepage memory'
  UNION ALL SELECT 'sys','sys_runtime','systemJvmMemory','JVM Memory','systemJvmMemory','CodeOutlined','JVM memory usage','{}',40,'System homepage JVM memory'
  UNION ALL SELECT 'sys','sys_runtime','systemMap','System Map','systemMap','GlobalOutlined','System map','{}',50,'System homepage map'
  UNION ALL SELECT 'sys','sys_runtime','systemServerInfo','Server Info','systemServerInfo','CloudServerOutlined','Server information','{}',60,'System homepage server info'
  UNION ALL SELECT 'sys','sys_runtime','systemOperationLogs','Operation Logs','systemOperationLogs','FileTextOutlined','Operation logs','{}',70,'System homepage operation logs'
  UNION ALL SELECT 'sys','sys_runtime','systemLoginLogs','Login Logs','systemLoginLogs','LoginOutlined','Login logs','{}',80,'System homepage login logs'
) seed
INNER JOIN `sys_homepage_component_category` cat
  ON cat.tenant_id = 0 AND cat.module_code = seed.module_code AND cat.category_code = seed.category_code
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_homepage_component_config` c
  WHERE c.scope_level = 'PUBLIC' AND c.tenant_id = 0 AND c.component_code = seed.component_code
);

SET @WORKSPACE_HOME_PARENT_ID := COALESCE(
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND tenant_id = @SYS_TENANT_ID AND component_key = 'PersonalHomepage' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND tenant_id = @SYS_TENANT_ID AND path IN ('home','workspace') ORDER BY id LIMIT 1),
  @ONLINE_DEV_PARENT_ID
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @SYS_TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @WORKSPACE_HOME_PARENT_ID, 'menu', 'home/component-config', 'Personal Homepage Component Config',
       JSON_OBJECT('zh-CN','个人首页组件配置','zh-TW','個人首頁組件配置','en-US','Personal Homepage Components','ja-JP','個人ホームページコンポーネント','ko-KR','개인 홈페이지 컴포넌트'),
       'AppstoreOutlined', 'PersonalHomepageComponentConfig', 'sys:homepageComponent:personalList', 20, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m
  WHERE m.deleted = 0 AND m.tenant_id = @SYS_TENANT_ID AND m.component_key = 'PersonalHomepageComponentConfig'
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @SYS_TENANT_ID, 'PUBLIC', @SYS_MODULE_ID, @HOMEPAGE_COMPONENT_MENU_ID, 'button', seed.path, seed.name, seed.name_i18n_json,
       NULL, NULL, seed.perm_key, seed.order_num, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM (
  SELECT 'delete-tenant' AS path, 'Delete Tenant Component' AS name, JSON_OBJECT('zh-CN','删除租户组件','zh-TW','刪除租戶組件','en-US','Delete Tenant Component','ja-JP','テナントコンポーネント削除','ko-KR','테넌트 컴포넌트 삭제') AS name_i18n_json, 'sys:homepageComponent:deleteTenant' AS perm_key, 41 AS order_num
  UNION ALL SELECT 'delete-public', 'Delete Public Component', JSON_OBJECT('zh-CN','删除公共组件','zh-TW','刪除公共組件','en-US','Delete Public Component','ja-JP','共通コンポーネント削除','ko-KR','공통 컴포넌트 삭제'), 'sys:homepageComponent:deletePublic', 42
  UNION ALL SELECT 'personal-list', 'Personal Component List', JSON_OBJECT('zh-CN','个人组件列表','zh-TW','個人組件列表','en-US','Personal Component List','ja-JP','個人コンポーネント一覧','ko-KR','개인 컴포넌트 목록'), 'sys:homepageComponent:personalList', 43
) seed
WHERE @HOMEPAGE_COMPONENT_MENU_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` btn
    WHERE btn.deleted = 0 AND btn.parent_id = @HOMEPAGE_COMPONENT_MENU_ID AND btn.perm_key = seed.perm_key
  );

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.permission_name, seed.permission_key, seed.url, 'POST', 0, @NOW, @NOW, 0
FROM (
  SELECT 'Delete Tenant Homepage Component' AS permission_name, 'sys:homepageComponent:deleteTenant' AS permission_key, '/sys/homepage/component/delete-tenant' AS url
  UNION ALL SELECT 'Delete Public Homepage Component', 'sys:homepageComponent:deletePublic', '/sys/homepage/component/delete-public'
  UNION ALL SELECT 'Personal Homepage Component List', 'sys:homepageComponent:personalList', '/sys/homepage/component/personal/list'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = seed.permission_key
);

USE `forgex_common`;

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'SystemHomepageComponentTable',
       '{"zh-CN":"首页组件目录","zh-TW":"首頁組件目錄","en-US":"Homepage Component Catalog","ja-JP":"ホームページコンポーネント一覧","ko-KR":"홈페이지 컴포넌트 목록"}',
       'BUSINESS', 'id', 10, 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `deleted` = 0
);

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0,'SystemHomepageComponentTable',seed.field,seed.title_i18n_json,seed.align,seed.width,seed.fixed,seed.ellipsis,seed.sortable,seed.sorter_field,seed.queryable,seed.query_type,seed.query_operator,seed.dict_code,seed.render_type,seed.perm_key,seed.order_num,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM (
  SELECT 'componentCode' AS field, '{"zh-CN":"组件编码","zh-TW":"組件編碼","en-US":"Component Code","ja-JP":"コンポーネントコード","ko-KR":"컴포넌트 코드"}' AS title_i18n_json, 'left' AS align, 180 AS width, NULL AS fixed, 0 AS ellipsis, 1 AS sortable, 'componentCode' AS sorter_field, 1 AS queryable, 'input' AS query_type, 'like' AS query_operator, NULL AS dict_code, NULL AS render_type, NULL AS perm_key, 1 AS order_num
  UNION ALL SELECT 'componentName','{"zh-CN":"组件名称","zh-TW":"組件名稱","en-US":"Component Name","ja-JP":"コンポーネント名","ko-KR":"컴포넌트명"}','left',180,NULL,1,1,'componentName',1,'input','like',NULL,NULL,NULL,2
  UNION ALL SELECT 'categoryName','{"zh-CN":"分类","zh-TW":"分類","en-US":"Category","ja-JP":"カテゴリ","ko-KR":"분류"}','left',160,NULL,0,1,'categoryName',1,'input','like',NULL,'tag',NULL,3
  UNION ALL SELECT 'moduleCode','{"zh-CN":"模块","zh-TW":"模組","en-US":"Module","ja-JP":"モジュール","ko-KR":"모듈"}','center',120,NULL,0,1,'moduleCode',1,'input','eq',NULL,NULL,NULL,4
  UNION ALL SELECT 'scopeLevel','{"zh-CN":"层级","zh-TW":"層級","en-US":"Scope","ja-JP":"スコープ","ko-KR":"범위"}','center',120,NULL,0,1,'scopeLevel',0,NULL,NULL,NULL,'tag',NULL,5
  UNION ALL SELECT 'enabled','{"zh-CN":"启用","zh-TW":"啟用","en-US":"Enabled","ja-JP":"有効","ko-KR":"활성화"}','center',100,NULL,0,1,'enabled',1,'select','eq','common_status','tag',NULL,6
  UNION ALL SELECT 'orderNum','{"zh-CN":"排序","zh-TW":"排序","en-US":"Order","ja-JP":"並び順","ko-KR":"정렬"}','right',100,NULL,0,1,'orderNum',0,NULL,NULL,NULL,NULL,NULL,7
  UNION ALL SELECT 'action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',220,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config`
  WHERE `tenant_id` = 0 AND `table_code` = 'SystemHomepageComponentTable' AND `field` = seed.field AND `deleted` = 0
);

USE `forgex_admin`;

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @SYS_TENANT_ID, @ADMIN_ROLE_ID, m.id
FROM `sys_menu` m
WHERE m.deleted = 0
  AND m.tenant_id = @SYS_TENANT_ID
  AND (m.component_key = 'PersonalHomepageComponentConfig' OR m.perm_key LIKE 'sys:homepageComponent:%')
  AND @ADMIN_ROLE_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @SYS_TENANT_ID AND rm.role_id = @ADMIN_ROLE_ID AND rm.menu_id = m.id
  );

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @ADMIN_ROLE_ID, p.id
FROM `sys_permission` p
WHERE p.deleted = 0
  AND p.permission_key LIKE 'sys:homepageComponent:%'
  AND @ADMIN_ROLE_ID IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.role_id = @ADMIN_ROLE_ID AND rp.permission_id = p.id
  );
