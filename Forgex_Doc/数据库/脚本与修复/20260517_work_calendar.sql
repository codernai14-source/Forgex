-- 工作日历页面与提醒能力升级脚本
-- 适用库：forgex_admin / forgex_job
-- 说明：脚本可重复执行，用于新增工作日历基础表、提醒任务表、菜单权限和默认消息模板。
SET NAMES utf8mb4;

USE `forgex_admin`;

SET @script_user := '20260517_work_calendar';
SET @now := NOW();
SET @public_tenant_id := 1993479636925403138;

CREATE TABLE IF NOT EXISTS `basic_work_calendar_day` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID，固定为 0',
  `calendar_date` date NOT NULL COMMENT '日期',
  `year_value` int NOT NULL COMMENT '年',
  `month_value` int NOT NULL COMMENT '月',
  `day_value` int NOT NULL COMMENT '日',
  `date_type` tinyint NOT NULL DEFAULT 1 COMMENT '日期类型：1 工作日，2 公休日，3 法定节假日，4 调休工作日，5 自定义假期，6 活动日',
  `holiday_name` varchar(100) DEFAULT NULL COMMENT '节假日名称',
  `custom_week` varchar(50) DEFAULT NULL COMMENT '自定义周别',
  `public_week` varchar(50) DEFAULT NULL COMMENT '公共周别',
  `is_holiday_synced` tinyint NOT NULL DEFAULT 0 COMMENT '是否同步法定节假日',
  `holiday_source_year` int DEFAULT NULL COMMENT '节假日来源年份',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_work_calendar_day_date` (`calendar_date`, `deleted`),
  KEY `idx_work_calendar_day_month` (`year_value`, `month_value`, `date_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作日历日期主数据';

CREATE TABLE IF NOT EXISTS `basic_work_calendar_tenant_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `record_type` varchar(50) NOT NULL COMMENT '记录类型',
  `event_title` varchar(200) NOT NULL COMMENT '日程标题',
  `event_content` text COMMENT '日程内容',
  `start_time` datetime NOT NULL COMMENT '开始日期时间',
  `end_time` datetime NOT NULL COMMENT '结束日期时间',
  `notify_user_ids` json DEFAULT NULL COMMENT '通知人 ID 集合',
  `remind_minutes` int DEFAULT 15 COMMENT '提前提醒分钟数',
  `remind_time` datetime DEFAULT NULL COMMENT '提示时间',
  `message_template_code` varchar(64) DEFAULT 'CALENDAR_REMINDER' COMMENT '消息模板编码',
  `source_user_id` bigint DEFAULT NULL COMMENT '推送来源用户 ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`),
  KEY `idx_work_calendar_tenant_event_time` (`tenant_id`, `start_time`, `end_time`, `deleted`),
  KEY `idx_work_calendar_tenant_event_type` (`tenant_id`, `record_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作日历租户日程';

CREATE TABLE IF NOT EXISTS `basic_work_calendar_user_event` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `owner_user_id` bigint NOT NULL COMMENT '所属用户 ID',
  `record_type` varchar(50) NOT NULL COMMENT '记录类型',
  `event_title` varchar(200) NOT NULL COMMENT '日程标题',
  `event_content` text COMMENT '日程内容',
  `start_time` datetime NOT NULL COMMENT '开始日期时间',
  `end_time` datetime NOT NULL COMMENT '结束日期时间',
  `notify_user_ids` json DEFAULT NULL COMMENT '通知人 ID 集合',
  `remind_minutes` int DEFAULT 15 COMMENT '提前提醒分钟数',
  `remind_time` datetime DEFAULT NULL COMMENT '提示时间',
  `message_template_code` varchar(64) DEFAULT 'CALENDAR_REMINDER' COMMENT '消息模板编码',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`),
  KEY `idx_work_calendar_user_event_time` (`tenant_id`, `owner_user_id`, `start_time`, `end_time`, `deleted`),
  KEY `idx_work_calendar_user_event_type` (`tenant_id`, `owner_user_id`, `record_type`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作日历个人日程';

SET @basic_parent_id := COALESCE(
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND path = 'basic' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_menu` WHERE deleted = 0 AND name IN ('基础信息', '基础资料') ORDER BY id LIMIT 1)
);
SET @basic_module_id := COALESCE((SELECT module_id FROM `sys_menu` WHERE id = @basic_parent_id LIMIT 1), 2);
SET @admin_role_id := COALESCE(
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND tenant_id = @public_tenant_id AND role_key = 'admin' ORDER BY id LIMIT 1),
  (SELECT id FROM `sys_role` WHERE deleted = 0 AND role_key = 'admin' ORDER BY id LIMIT 1)
);

INSERT INTO `sys_menu`
(`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT @public_tenant_id, 'PUBLIC', @basic_module_id, @basic_parent_id, 'menu', 'workCalendar', '工作日历',
       JSON_OBJECT('zh-CN','工作日历','zh-TW','工作日曆','en-US','Work Calendar','ja-JP','稼働カレンダー','ko-KR','작업 달력'),
       'CalendarOutlined', 'BasicWorkCalendar', 'basic:workCalendar:query', 80, 1, 1, @now, @script_user, @now, @script_user, 0, 2, 'embedded', NULL
FROM DUAL
WHERE @basic_parent_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE deleted = 0 AND component_key = 'BasicWorkCalendar'
  );

INSERT INTO `sys_permission`
(`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT seed.permission_name, seed.permission_key, seed.url, 'POST', 0, @now, @now, 0
FROM (
  SELECT '工作日历查询' permission_name, 'basic:workCalendar:query' permission_key, '/basic/work-calendar/month' url UNION ALL
  SELECT '工作日历新增', 'basic:workCalendar:add', '/basic/work-calendar/event/save' UNION ALL
  SELECT '工作日历编辑', 'basic:workCalendar:edit', '/basic/work-calendar/event/save' UNION ALL
  SELECT '工作日历删除', 'basic:workCalendar:delete', '/basic/work-calendar/event/delete' UNION ALL
  SELECT '工作日历推送租户', 'basic:workCalendar:pushTenant', '/basic/work-calendar/event/push-tenant'
) seed
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.deleted = 0 AND p.permission_key = seed.permission_key
);

INSERT INTO `sys_role_permission` (`role_id`,`permission_id`)
SELECT @admin_role_id, p.id
FROM `sys_permission` p
WHERE @admin_role_id IS NOT NULL
  AND p.deleted = 0
  AND p.permission_key LIKE 'basic:workCalendar:%'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_permission` rp WHERE rp.role_id = @admin_role_id AND rp.permission_id = p.id
  );

INSERT INTO `sys_role_menu` (`tenant_id`,`role_id`,`menu_id`)
SELECT @public_tenant_id, @admin_role_id, m.id
FROM `sys_menu` m
WHERE @admin_role_id IS NOT NULL
  AND m.deleted = 0
  AND m.component_key = 'BasicWorkCalendar'
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` rm
    WHERE rm.tenant_id = @public_tenant_id AND rm.role_id = @admin_role_id AND rm.menu_id = m.id
  );

INSERT INTO `sys_message_template`
(`tenant_id`,`template_code`,`template_name`,`template_name_i18n_json`,`template_version`,`message_type`,`biz_type`,`notification_type`,`config_level`,`tenant_type`,`category`,`status`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`,`update_by`)
SELECT 0, 'CALENDAR_REMINDER', '工作日历提醒',
       JSON_OBJECT('zh-CN','工作日历提醒','en-US','Work Calendar Reminder','zh-TW','工作日曆提醒','ja-JP','稼働カレンダー通知','ko-KR','작업 달력 알림'),
       '1.0.0', 'NOTICE', 'CALENDAR', 'info', 'PUBLIC', 'PUBLIC', 'SYSTEM', 1, '工作日历日程到期提醒', @now, @now, 0, @script_user, @script_user
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_message_template` WHERE deleted = 0 AND tenant_id = 0 AND template_code = 'CALENDAR_REMINDER'
);

SET @calendar_template_id := (
  SELECT id FROM `sys_message_template`
  WHERE deleted = 0 AND tenant_id = 0 AND template_code = 'CALENDAR_REMINDER'
  ORDER BY id LIMIT 1
);

INSERT INTO `sys_message_template_content`
(`tenant_id`,`template_id`,`platform`,`content_title`,`content_title_i18n_json`,`content_body`,`content_body_i18n_json`,`link_url`,`create_time`,`update_time`,`deleted`,`create_by`,`update_by`)
SELECT 0, @calendar_template_id, 'INTERNAL', '【日程提醒】${title}',
       JSON_OBJECT('zh-CN','【日程提醒】${title}','en-US','[Calendar] ${title}','zh-TW','【日程提醒】${title}','ja-JP','【予定通知】${title}','ko-KR','【일정 알림】${title}'),
       '类型：${recordType}\n开始：${startTime}\n结束：${endTime}',
       JSON_OBJECT('zh-CN','类型：${recordType}\n开始：${startTime}\n结束：${endTime}','en-US','Type: ${recordType}\nStart: ${startTime}\nEnd: ${endTime}'),
       '/workspace/basic/workCalendar', @now, @now, 0, @script_user, @script_user
FROM DUAL
WHERE @calendar_template_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM `sys_message_template_content`
    WHERE deleted = 0 AND tenant_id = 0 AND template_id = @calendar_template_id AND platform = 'INTERNAL'
  );

USE `forgex_job`;

CREATE TABLE IF NOT EXISTS `sys_calendar_reminder_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `source_type` varchar(50) NOT NULL COMMENT '来源类型：USER_EVENT、TENANT_EVENT',
  `source_id` bigint NOT NULL COMMENT '来源日程 ID',
  `owner_user_id` bigint DEFAULT NULL COMMENT '日程拥有者 ID',
  `title` varchar(200) NOT NULL COMMENT '提醒标题',
  `record_type` varchar(50) DEFAULT NULL COMMENT '记录类型',
  `start_time` datetime DEFAULT NULL COMMENT '日程开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '日程结束时间',
  `remind_time` datetime NOT NULL COMMENT '提醒时间',
  `notify_user_ids` json NOT NULL COMMENT '通知人 ID 集合',
  `template_code` varchar(64) NOT NULL DEFAULT 'CALENDAR_REMINDER' COMMENT '消息模板编码',
  `template_data` json DEFAULT NULL COMMENT '模板变量',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态：0 待发送，1 发送中，2 已发送，3 失败，4 已取消',
  `send_count` int NOT NULL DEFAULT 0 COMMENT '发送次数',
  `max_retry_count` int NOT NULL DEFAULT 3 COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `sent_time` datetime DEFAULT NULL COMMENT '发送完成时间',
  `fail_reason` text COMMENT '失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`),
  KEY `idx_calendar_reminder_due` (`tenant_id`, `status`, `remind_time`, `next_retry_time`, `deleted`),
  KEY `idx_calendar_reminder_source` (`tenant_id`, `source_type`, `source_id`, `deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日历提醒任务清单';
