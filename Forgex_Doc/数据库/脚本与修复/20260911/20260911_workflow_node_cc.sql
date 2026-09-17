-- 审批节点独立抄送：节点开关、抄送配置表、运行时记录、菜单权限、消息模板、动态表。
-- 脚本可重复执行，目标库：forgex_workflow、forgex_admin、forgex_common。
-- 请使用 UTF-8 / utf8mb4 客户端执行，避免中文注释乱码。

SET NAMES utf8mb4;

-- ----------------------------------------------------------------
-- 一、forgex_workflow：字段与表
-- ----------------------------------------------------------------
USE `forgex_workflow`;

SET @col_exists := (
  SELECT COUNT(*)
  FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = 'forgex_workflow'
    AND TABLE_NAME = 'wf_task_node_config'
    AND COLUMN_NAME = 'cc_enabled'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE `wf_task_node_config` ADD COLUMN `cc_enabled` tinyint NOT NULL DEFAULT 0 COMMENT ''是否启用节点抄送：0=否，1=是'' AFTER `approve_type`',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `wf_task_node_cc` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `node_config_id` bigint NOT NULL COMMENT '审批任务节点配置表ID',
  `cc_type` int NOT NULL COMMENT '抄送对象类型：1=用户，2=部门，3=角色，4=岗位',
  `cc_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '抄送对象ID集合（JSON字符串数组）',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_node_cc_node` (`tenant_id` ASC, `node_config_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批节点抄送配置' ROW_FORMAT = Dynamic;

CREATE TABLE IF NOT EXISTS `wf_task_cc_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `execution_id` bigint NOT NULL COMMENT '审批执行单ID',
  `execution_detail_id` bigint NOT NULL COMMENT '本轮节点进入对应的执行明细ID',
  `node_id` bigint NOT NULL COMMENT '节点配置ID',
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '抄送节点名称快照',
  `cc_user_id` bigint NOT NULL COMMENT '被抄送用户ID',
  `cc_user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '被抄送用户名称快照',
  `cc_source_type` int NULL DEFAULT NULL COMMENT '来源类型：1=用户，2=部门，3=角色，4=岗位',
  `source_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '解析来源快照JSON',
  `read_status` tinyint NOT NULL DEFAULT 0 COMMENT '已读状态：0=未读，1=已读',
  `read_time` datetime NULL DEFAULT NULL COMMENT '已读时间',
  `notify_status` tinyint NOT NULL DEFAULT 0 COMMENT '通知状态：0=未发送，1=成功，2=失败',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wf_task_cc_detail_user` (`execution_detail_id` ASC, `cc_user_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wf_task_cc_user_time` (`tenant_id` ASC, `cc_user_id` ASC, `deleted` ASC, `create_time` ASC) USING BTREE,
  INDEX `idx_wf_task_cc_execution` (`tenant_id` ASC, `execution_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '审批节点抄送运行时记录' ROW_FORMAT = Dynamic;

-- ----------------------------------------------------------------
-- 二、forgex_admin：菜单、权限、消息模板
-- ----------------------------------------------------------------
USE `forgex_admin`;

SET @script_user := '20260911_workflow_node_cc';
SET @now := NOW();

SET @wf_tenant_id := COALESCE(
  (SELECT tenant_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'ApprovalMyPending' ORDER BY id LIMIT 1),
  1
);
SET @wf_module_id := COALESCE(
  (SELECT module_id FROM `sys_menu` WHERE deleted = 0 AND component_key = 'ApprovalMyPending' ORDER BY id LIMIT 1),
  3
);
SET @admin_role_id := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @wf_tenant_id AND role_key = 'admin' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id LIMIT 1)
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @wf_tenant_id, 'PUBLIC', @wf_module_id, 0, 'menu', 'my/cc', '我的抄送',
       '{"zh-CN":"我的抄送","zh-TW":"我的抄送","en-US":"My CC","ja-JP":"私のCC","ko-KR":"내 참조"}',
       'MailOutlined', 'ApprovalMyCc', 'wf:myTask:cc', 45, 1, 1,
       @now, @script_user, @now, @script_user, 0, 1, 'embedded', NULL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` existing
  WHERE existing.deleted = 0
    AND existing.component_key = 'ApprovalMyCc'
);

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT '我的抄送查询', 'wf:myTask:cc', '/wf/execution/my/cc', 'POST', 0, @now, @now, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` existing
  WHERE existing.deleted = 0
    AND existing.permission_key = 'wf:myTask:cc'
);

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.id
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.deleted = 0
  AND p.permission_key = 'wf:myTask:cc'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp
    WHERE rp.role_id = @admin_role_id AND rp.permission_id = p.id
  );

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT m.tenant_id, @admin_role_id, m.id
FROM `sys_menu` m
WHERE @admin_role_id IS NOT NULL
  AND m.deleted = 0
  AND m.component_key = 'ApprovalMyCc'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = m.tenant_id AND rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

INSERT INTO `sys_message_template`
(`tenant_id`,`template_code`,`template_name`,`template_name_i18n_json`,`template_version`,`message_type`,`biz_type`,`notification_type`,`config_level`,`tenant_type`,`category`,`status`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`,`update_by`)
SELECT src.tenant_id, 'WF_CC', '审批抄送通知',
       '{"zh-CN":"审批抄送通知","zh-TW":"審批抄送通知","en-US":"Approval CC Notification","ja-JP":"承認CC通知","ko-KR":"승인 참조 알림"}',
       COALESCE(src.template_version, '1.0.0'), 'NOTICE', src.biz_type, 'info',
       src.config_level, src.tenant_type, src.category, 1,
       '当审批节点启用抄送并进入节点时，通知被抄送人',
       @now, @now, 0, @script_user, @script_user
FROM `sys_message_template` src
WHERE src.deleted = 0
  AND src.template_code = 'WF_PENDING'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_message_template` existing
    WHERE existing.deleted = 0
      AND existing.tenant_id = src.tenant_id
      AND existing.template_code = 'WF_CC'
  );

INSERT INTO `sys_message_template`
(`tenant_id`,`template_code`,`template_name`,`template_name_i18n_json`,`template_version`,`message_type`,`biz_type`,`notification_type`,`config_level`,`tenant_type`,`category`,`status`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`,`update_by`)
SELECT 0, 'WF_CC', '审批抄送通知',
       '{"zh-CN":"审批抄送通知","zh-TW":"審批抄送通知","en-US":"Approval CC Notification","ja-JP":"承認CC通知","ko-KR":"승인 참조 알림"}',
       '1.0.0', 'NOTICE', NULL, 'info', 'TENANT', 'PUBLIC', 'APPROVAL', 1,
       '当审批节点启用抄送并进入节点时，通知被抄送人',
       @now, @now, 0, @script_user, @script_user
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_message_template` existing
  WHERE existing.deleted = 0
    AND existing.tenant_id = 0
    AND existing.template_code = 'WF_CC'
);

INSERT INTO `sys_message_template_content`
(`tenant_id`,`template_id`,`platform`,`content_title`,`content_title_i18n_json`,`content_body`,`content_body_i18n_json`,`link_url`,`create_time`,`update_time`,`deleted`,`create_by`,`update_by`)
SELECT t.tenant_id, t.id, 'INTERNAL',
       '【审批抄送】${taskName}',
       '{"zh-CN":"【审批抄送】${taskName}","zh-TW":"【審批抄送】${taskName}","en-US":"[Approval CC] ${taskName}","ja-JP":"【承認CC】${taskName}","ko-KR":"【승인 참조】${taskName}"}',
       '发起人：${initiatorName}\n当前节点：${nodeName}\n发起时间：${startTime}',
       '{"zh-CN":"发起人：${initiatorName}\\n当前节点：${nodeName}\\n发起时间：${startTime}","zh-TW":"發起人：${initiatorName}\\n當前節點：${nodeName}\\n發起時間：${startTime}","en-US":"Initiator: ${initiatorName}\\nCurrent Node: ${nodeName}\\nStart Time: ${startTime}","ja-JP":"申請者：${initiatorName}\\n現在のノード：${nodeName}\\n開始時間：${startTime}","ko-KR":"신청자: ${initiatorName}\\n현재 단계: ${nodeName}\\n시작 시간: ${startTime}"}',
       '/workspace/approval/my/cc',
       @now, @now, 0, @script_user, @script_user
FROM `sys_message_template` t
WHERE t.deleted = 0
  AND t.template_code = 'WF_CC'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_message_template_content` existing
    WHERE existing.deleted = 0
      AND existing.template_id = t.id
      AND existing.platform = 'INTERNAL'
  );

-- ----------------------------------------------------------------
-- 三、forgex_common：抄送动态表
-- ----------------------------------------------------------------
USE `forgex_common`;

SET @script_user := '20260911_workflow_node_cc';
SET @now := NOW();

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`default_sort_json`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 0, 'WfMyCcTaskTable',
       '{"zh-CN":"我的抄送","zh-TW":"我的抄送","en-US":"My CC Tasks","ja-JP":"私のCC","ko-KR":"내 참조"}',
       'NORMAL', 'id', 20, '{"field":"ccTime","order":"desc"}', 1, 1,
       @script_user, @now, @script_user, @now, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` existing
  WHERE existing.tenant_id = 0
    AND existing.table_code = 'WfMyCcTaskTable'
    AND existing.deleted = 0
);

INSERT INTO `fx_table_config`
(`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`default_sort_json`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT cfg.tenant_id, 'WfMyCcTaskTable',
       '{"zh-CN":"我的抄送","zh-TW":"我的抄送","en-US":"My CC Tasks","ja-JP":"私のCC","ko-KR":"내 참조"}',
       'NORMAL', 'id', 20, '{"field":"ccTime","order":"desc"}', 1, 1,
       @script_user, @now, @script_user, @now, 0
FROM `fx_table_config` cfg
WHERE cfg.deleted = 0
  AND cfg.table_code = 'WfPendingTaskTable'
  AND cfg.tenant_id <> 0
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_config` existing
    WHERE existing.tenant_id = cfg.tenant_id
      AND existing.table_code = 'WfMyCcTaskTable'
      AND existing.deleted = 0
  );

INSERT INTO `fx_table_column_config`
(`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT cfg.tenant_id, item.table_code, item.field, item.title_i18n_json, item.align, item.width, item.fixed, item.ellipsis, item.sortable, item.sorter_field,
       item.queryable, item.query_type, item.query_operator, item.dict_code, item.render_type, NULL, item.order_num, 1,
       @script_user, @now, @script_user, @now, 0
FROM `fx_table_config` cfg
JOIN (
  SELECT 'WfMyCcTaskTable' table_code, 'taskName' field, '{"zh-CN":"任务名称","zh-TW":"任務名稱","en-US":"Task Name","ja-JP":"タスク名","ko-KR":"작업명"}' title_i18n_json, 'left' align, 180 width, NULL fixed, 1 ellipsis, 0 sortable, NULL sorter_field, 1 queryable, 'input' query_type, 'like' query_operator, NULL dict_code, 'text' render_type, 10 order_num
  UNION ALL SELECT 'WfMyCcTaskTable','taskCode','{"zh-CN":"任务编码","zh-TW":"任務編碼","en-US":"Task Code","ja-JP":"タスクコード","ko-KR":"작업 코드"}','left',160,NULL,1,0,NULL,1,'input','like',NULL,'text',20
  UNION ALL SELECT 'WfMyCcTaskTable','initiatorName','{"zh-CN":"发起人","zh-TW":"發起人","en-US":"Initiator","ja-JP":"起案者","ko-KR":"기안자"}','left',140,NULL,0,0,NULL,0,NULL,NULL,NULL,'text',30
  UNION ALL SELECT 'WfMyCcTaskTable','ccNodeName','{"zh-CN":"抄送节点","zh-TW":"抄送節點","en-US":"CC Node","ja-JP":"CCノード","ko-KR":"참조 노드"}','left',160,NULL,0,0,NULL,0,NULL,NULL,NULL,'text',40
  UNION ALL SELECT 'WfMyCcTaskTable','status','{"zh-CN":"流程状态","zh-TW":"流程狀態","en-US":"Status","ja-JP":"ステータス","ko-KR":"상태"}','center',100,NULL,0,0,NULL,1,'select','eq','wf_execution_status','text',50
  UNION ALL SELECT 'WfMyCcTaskTable','ccTime','{"zh-CN":"抄送时间","zh-TW":"抄送時間","en-US":"CC Time","ja-JP":"CC日時","ko-KR":"참조 시간"}','center',180,NULL,0,1,'ccTime',0,NULL,NULL,NULL,'text',60
  UNION ALL SELECT 'WfMyCcTaskTable','ccUnread','{"zh-CN":"已读状态","zh-TW":"已讀狀態","en-US":"Read Status","ja-JP":"既読状態","ko-KR":"읽음 상태"}','center',110,NULL,0,0,NULL,0,NULL,NULL,NULL,'slot',70
  UNION ALL SELECT 'WfMyCcTaskTable','action','{"zh-CN":"操作","zh-TW":"操作","en-US":"Action","ja-JP":"操作","ko-KR":"작업"}','center',180,'right',0,0,NULL,0,NULL,NULL,NULL,'slot',999
) item
WHERE cfg.deleted = 0
  AND cfg.table_code = 'WfMyCcTaskTable'
  AND NOT EXISTS (
    SELECT 1 FROM `fx_table_column_config` existing
    WHERE existing.tenant_id = cfg.tenant_id
      AND existing.table_code = item.table_code
      AND existing.field = item.field
      AND existing.deleted = 0
  );
