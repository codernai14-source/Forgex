/*
 工作流模块消息通知表结构

 Date: 2026-04-01
 Author: Forgex Team
 Description: 为工作流模块添加消息模板配置和通知功能
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 为 wf_task_config 表添加消息模板配置字段
-- ----------------------------
ALTER TABLE `wf_task_config` 
ADD COLUMN `start_message_template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批开始消息模板编码' AFTER `remark`,
ADD COLUMN `approve_message_template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批通过消息模板编码' AFTER `start_message_template_code`,
ADD COLUMN `reject_message_template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批驳回消息模板编码' AFTER `approve_message_template_code`,
ADD COLUMN `finish_message_template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批完成消息模板编码' AFTER `reject_message_template_code`,
ADD COLUMN `link_base_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息链接基础 URL' AFTER `finish_message_template_code`;

-- ----------------------------
-- 2. 消息模板主表（如果不存在则创建）
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template`;
CREATE TABLE `sys_message_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码（唯一）',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板名称多语言 JSON',
  `template_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0.0' COMMENT '模板版本',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOTICE' COMMENT '消息类型 (NOTICE=通知，WARNING=警告，ALARM=报警)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 (0=禁用，1=启用)',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户 ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`tenant_id` ASC, `template_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 3. 消息模板内容表（按平台配置内容）
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_content`;
CREATE TABLE `sys_message_template_content` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_id` bigint NOT NULL COMMENT '关联模板 ID',
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息平台 (INTERNAL=站内，WECHAT=企业微信，SMS=短信，EMAIL=邮箱)',
  `content_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题（支持${变量名}占位符）',
  `content_title_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标题多语言 JSON',
  `content_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容（支持占位符）',
  `content_body_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容多语言 JSON',
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接（支持占位符）',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_platform`(`platform` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 4. 消息模板接收人表（配置接收规则）
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_receiver`;
CREATE TABLE `sys_message_template_receiver` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `template_id` bigint NOT NULL COMMENT '关联模板 ID',
  `receiver_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收人类型 (USER=指定人，ROLE=角色，DEPT=部门，POSITION=职位)',
  `receiver_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接收人 ID 集合（JSON 数组格式，如[1,2,3]）',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_receiver_type`(`receiver_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板接收人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 5. 插入工作流模块默认消息模板数据
-- ----------------------------

-- 5.1 审批开始通知模板
INSERT INTO `sys_message_template` (`template_code`, `template_name`, `template_version`, `message_type`, `status`, `remark`) 
VALUES ('WF_APPROVAL_START', '审批开始通知', '1.0.0', 'NOTICE', 1, '审批任务开始时发送通知给审批人');

-- 获取刚插入的模板 ID
SET @start_template_id = LAST_INSERT_ID();

-- 插入模板内容（站内消息）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@start_template_id, 'INTERNAL', 
        '【审批通知】您有新的审批任务待处理', 
        '尊敬的审批人：\n\n您有一个新的审批任务待处理。\n\n任务名称：${taskName}\n发起人：${initiatorName}\n发起时间：${startTime}\n\n请点击下方链接查看详情：\n${linkUrl}',
        '${linkUrl}');

-- 插入模板内容（邮件）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@start_template_id, 'EMAIL', 
        '【审批通知】您有新的审批任务待处理 - ${taskName}', 
        '<html><body>
<h2>审批任务通知</h2>
<p>尊敬的审批人：</p>
<p>您有一个新的审批任务待处理。</p>
<table border="1" cellpadding="5">
  <tr><td><strong>任务名称</strong></td><td>${taskName}</td></tr>
  <tr><td><strong>发起人</strong></td><td>${initiatorName}</td></tr>
  <tr><td><strong>发起时间</strong></td><td>${startTime}</td></tr>
</table>
<p>请点击下方链接查看详情：</p>
<p><a href="${linkUrl}">${linkUrl}</a></p>
</body></html>',
        '${linkUrl}');

-- 插入接收人配置（示例：发送给部门经理角色）
INSERT INTO `sys_message_template_receiver` (`template_id`, `receiver_type`, `receiver_ids`) 
VALUES (@start_template_id, 'ROLE', '[1]');


-- 5.2 审批通过通知模板
INSERT INTO `sys_message_template` (`template_code`, `template_name`, `template_version`, `message_type`, `status`, `remark`) 
VALUES ('WF_APPROVAL_PASS', '审批通过通知', '1.0.0', 'NOTICE', 1, '审批通过时发送通知给发起人');

-- 获取刚插入的模板 ID
SET @pass_template_id = LAST_INSERT_ID();

-- 插入模板内容（站内消息）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@pass_template_id, 'INTERNAL', 
        '【审批进度】您的审批已通过', 
        '尊敬的${initiatorName}：\n\n您的审批任务已通过。\n\n任务名称：${taskName}\n审批人：${approverName}\n审批意见：${comment}\n审批时间：${approveTime}\n\n请点击下方链接查看详情：\n${linkUrl}',
        '${linkUrl}');

-- 插入模板内容（邮件）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@pass_template_id, 'EMAIL', 
        '【审批进度】您的审批已通过 - ${taskName}', 
        '<html><body>
<h2>审批通过通知</h2>
<p>尊敬的${initiatorName}：</p>
<p>您的审批任务已通过。</p>
<table border="1" cellpadding="5">
  <tr><td><strong>任务名称</strong></td><td>${taskName}</td></tr>
  <tr><td><strong>审批人</strong></td><td>${approverName}</td></tr>
  <tr><td><strong>审批意见</strong></td><td>${comment}</td></tr>
  <tr><td><strong>审批时间</strong></td><td>${approveTime}</td></tr>
</table>
<p>请点击下方链接查看详情：</p>
<p><a href="${linkUrl}">${linkUrl}</a></p>
</body></html>',
        '${linkUrl}');

-- 插入接收人配置（发送给发起人）
INSERT INTO `sys_message_template_receiver` (`template_id`, `receiver_type`, `receiver_ids`) 
VALUES (@pass_template_id, 'USER', '[]');


-- 5.3 审批驳回通知模板
INSERT INTO `sys_message_template` (`template_code`, `template_name`, `template_version`, `message_type`, `status`, `remark`) 
VALUES ('WF_APPROVAL_REJECT', '审批驳回通知', '1.0.0', 'WARNING', 1, '审批驳回时发送通知给发起人');

-- 获取刚插入的模板 ID
SET @reject_template_id = LAST_INSERT_ID();

-- 插入模板内容（站内消息）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@reject_template_id, 'INTERNAL', 
        '【审批进度】您的审批已被驳回', 
        '尊敬的${initiatorName}：\n\n您的审批任务已被驳回。\n\n任务名称：${taskName}\n审批人：${approverName}\n驳回原因：${rejectReason}\n驳回时间：${rejectTime}\n\n请点击下方链接查看详情并重新提交：\n${linkUrl}',
        '${linkUrl}');

-- 插入模板内容（邮件）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@reject_template_id, 'EMAIL', 
        '【审批进度】您的审批已被驳回 - ${taskName}', 
        '<html><body>
<h2>审批驳回通知</h2>
<p>尊敬的${initiatorName}：</p>
<p>您的审批任务已被驳回。</p>
<table border="1" cellpadding="5">
  <tr><td><strong>任务名称</strong></td><td>${taskName}</td></tr>
  <tr><td><strong>审批人</strong></td><td>${approverName}</td></tr>
  <tr><td><strong>驳回原因</strong></td><td>${rejectReason}</td></tr>
  <tr><td><strong>驳回时间</strong></td><td>${rejectTime}</td></tr>
</table>
<p>请点击下方链接查看详情并重新提交：</p>
<p><a href="${linkUrl}">${linkUrl}</a></p>
</body></html>',
        '${linkUrl}');

-- 插入接收人配置（发送给发起人）
INSERT INTO `sys_message_template_receiver` (`template_id`, `receiver_type`, `receiver_ids`) 
VALUES (@reject_template_id, 'USER', '[]');


-- 5.4 审批完成通知模板
INSERT INTO `sys_message_template` (`template_code`, `template_name`, `template_version`, `message_type`, `status`, `remark`) 
VALUES ('WF_APPROVAL_FINISH', '审批完成通知', '1.0.0', 'NOTICE', 1, '审批流程完成时发送通知给发起人');

-- 获取刚插入的模板 ID
SET @finish_template_id = LAST_INSERT_ID();

-- 插入模板内容（站内消息）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@finish_template_id, 'INTERNAL', 
        '【审批完成】您的审批流程已完成', 
        '尊敬的${initiatorName}：\n\n您的审批流程已完成。\n\n任务名称：${taskName}\n最终状态：${finishStatus}\n完成时间：${finishTime}\n\n请点击下方链接查看详情：\n${linkUrl}',
        '${linkUrl}');

-- 插入模板内容（邮件）
INSERT INTO `sys_message_template_content` (`template_id`, `platform`, `content_title`, `content_body`, `link_url`) 
VALUES (@finish_template_id, 'EMAIL', 
        '【审批完成】您的审批流程已完成 - ${taskName}', 
        '<html><body>
<h2>审批完成通知</h2>
<p>尊敬的${initiatorName}：</p>
<p>您的审批流程已完成。</p>
<table border="1" cellpadding="5">
  <tr><td><strong>任务名称</strong></td><td>${taskName}</td></tr>
  <tr><td><strong>最终状态</strong></td><td>${finishStatus}</td></tr>
  <tr><td><strong>完成时间</strong></td><td>${finishTime}</td></tr>
</table>
<p>请点击下方链接查看详情：</p>
<p><a href="${linkUrl}">${linkUrl}</a></p>
</body></html>',
        '${linkUrl}');

-- 插入接收人配置（发送给发起人）
INSERT INTO `sys_message_template_receiver` (`template_id`, `receiver_type`, `receiver_ids`) 
VALUES (@finish_template_id, 'USER', '[]');

SET FOREIGN_KEY_CHECKS = 1;
