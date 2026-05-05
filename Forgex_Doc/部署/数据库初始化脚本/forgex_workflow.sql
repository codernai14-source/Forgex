/*
 Navicat Premium Dump SQL

 Source Server         : bendi-127
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : forgex_workflow

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 03/05/2026 18:03:58
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_message_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template`;
CREATE TABLE `sys_message_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `template_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0.0',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NOTICE',
  `status` tinyint NULL DEFAULT 1,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_message_template`(`tenant_id` ASC, `template_code` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_message_template
-- ----------------------------
INSERT INTO `sys_message_template` VALUES (1, 'WF_APPROVAL_START', '??????', '{\"zh-CN\":\"??????\",\"en-US\":\"Approval Start\"}', '1.0.0', 'NOTICE', 1, '???????????????', 0, '2026-04-02 14:50:27', 'system', '2026-04-02 14:50:27', 'system', 0);
INSERT INTO `sys_message_template` VALUES (2, 'WF_APPROVAL_PASS', '??????', '{\"zh-CN\":\"??????\",\"en-US\":\"Approval Pass\"}', '1.0.0', 'NOTICE', 1, '?????????????', 0, '2026-04-02 14:50:27', 'system', '2026-04-02 14:50:27', 'system', 0);
INSERT INTO `sys_message_template` VALUES (3, 'WF_APPROVAL_REJECT', '??????', '{\"zh-CN\":\"??????\",\"en-US\":\"Approval Reject\"}', '1.0.0', 'WARNING', 1, '?????????????', 0, '2026-04-02 14:50:27', 'system', '2026-04-02 14:50:27', 'system', 0);
INSERT INTO `sys_message_template` VALUES (4, 'WF_APPROVAL_FINISH', '??????', '{\"zh-CN\":\"??????\",\"en-US\":\"Approval Finish\"}', '1.0.0', 'NOTICE', 1, '?????????????', 0, '2026-04-02 14:50:27', 'system', '2026-04-02 14:50:27', 'system', 0);
INSERT INTO `sys_message_template` VALUES (5, 'WF_PENDING', '审批待办通知', '{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\"}', '1.0.0', 'NOTICE', 1, '当审批流程流转到某个节点时，通知待审批人有新的待办任务', NULL, '2026-04-06 19:44:59', NULL, '2026-04-06 19:44:59', NULL, 0);

-- ----------------------------
-- Table structure for sys_message_template_content
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_content`;
CREATE TABLE `sys_message_template_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `content_title_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `content_body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `content_body_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_message_template_content_tpl`(`template_id` ASC, `platform` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_message_template_content
-- ----------------------------
INSERT INTO `sys_message_template_content` VALUES (1, 1, 'INTERNAL', '?????????????????', NULL, '?????${taskName}\n????${initiatorName}\n?????${startTime}\n???${linkUrl}', NULL, '${linkUrl}', 0, '2026-04-02 14:50:27', 0);
INSERT INTO `sys_message_template_content` VALUES (2, 2, 'INTERNAL', '?????????????', NULL, '?????${taskName}\n????${approverName}\n?????${comment}\n???${linkUrl}', NULL, '${linkUrl}', 0, '2026-04-02 14:50:27', 0);
INSERT INTO `sys_message_template_content` VALUES (3, 3, 'INTERNAL', '??????????????', NULL, '?????${taskName}\n????${approverName}\n?????${rejectReason}\n???${linkUrl}', NULL, '${linkUrl}', 0, '2026-04-02 14:50:27', 0);
INSERT INTO `sys_message_template_content` VALUES (4, 4, 'INTERNAL', '???????????????', NULL, '?????${taskName}\n?????${finishStatus}\n?????${finishTime}\n???${linkUrl}', NULL, '${linkUrl}', 0, '2026-04-02 14:50:27', 0);

-- ----------------------------
-- Table structure for sys_message_template_receiver
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_receiver`;
CREATE TABLE `sys_message_template_receiver`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `receiver_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `tenant_id` bigint NOT NULL DEFAULT 0,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_message_template_receiver_tpl`(`template_id` ASC, `receiver_type` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_message_template_receiver
-- ----------------------------

-- ----------------------------
-- Table structure for wf_my_task
-- ----------------------------
DROP TABLE IF EXISTS `wf_my_task`;
CREATE TABLE `wf_my_task`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` bigint NOT NULL,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `node_id` bigint NULL DEFAULT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例 ID',
  `execution_detail_id` bigint NULL DEFAULT NULL COMMENT '审批执行明细 ID',
  `approver_id` bigint NULL DEFAULT NULL COMMENT '当前审批人 ID',
  `deadline_time` datetime NULL DEFAULT NULL COMMENT '审批截止时间',
  `approver_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` int NOT NULL DEFAULT 0,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_my_task_exec`(`tenant_id` ASC, `execution_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_wf_my_task_instance`(`approval_instance_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_wf_my_task_approver`(`approver_id` ASC, `status` ASC, `deadline_time` ASC) USING BTREE,
  INDEX `idx_wf_my_task_exec_detail`(`execution_id` ASC, `execution_detail_id` ASC, `node_id` ASC, `status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_my_task
-- ----------------------------
INSERT INTO `wf_my_task` VALUES (1, 1, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-05 17:21:04');
INSERT INTO `wf_my_task` VALUES (2, 2, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-06 17:49:16');
INSERT INTO `wf_my_task` VALUES (3, 3, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-06 23:26:13');
INSERT INTO `wf_my_task` VALUES (4, 4, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-06 23:50:15');
INSERT INTO `wf_my_task` VALUES (5, 5, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-07 08:47:28');
INSERT INTO `wf_my_task` VALUES (6, 6, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-07 11:34:44');
INSERT INTO `wf_my_task` VALUES (7, 7, '请假审批', 15, '管理员审批', NULL, NULL, NULL, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-09 10:13:33');
INSERT INTO `wf_my_task` VALUES (8, 13, 'xa', 37, '审批节点', 1, 30, 1993479637244170242, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-22 19:50:41');
INSERT INTO `wf_my_task` VALUES (9, 13, 'xa', 37, '审批节点', 2, 30, 1993479637244170253, NULL, '[1993479637244170253]', 1, 1993479636925403138, '2026-04-22 19:50:41');
INSERT INTO `wf_my_task` VALUES (10, 13, 'xa', 37, '审批节点', NULL, NULL, NULL, NULL, '[1993479637244170242,1993479637244170253]', 1, 1993479636925403138, '2026-04-22 19:50:41');
INSERT INTO `wf_my_task` VALUES (11, 13, 'xa', 37, '审批节点', NULL, NULL, NULL, NULL, '[1993479637244170242,1993479637244170253]', 1, 1993479636925403138, '2026-04-22 19:50:41');
INSERT INTO `wf_my_task` VALUES (12, 14, 'xa', 37, '审批节点', 3, 33, 1993479637244170242, NULL, '[1993479637244170242]', 1, 1993479636925403138, '2026-04-22 23:29:25');
INSERT INTO `wf_my_task` VALUES (13, 14, 'xa', 37, '审批节点', 4, 33, 1993479637244170253, NULL, '[1993479637244170253]', 1, 1993479636925403138, '2026-04-22 23:29:25');
INSERT INTO `wf_my_task` VALUES (14, 14, 'xa', 37, '审批节点', NULL, NULL, NULL, NULL, '[1993479637244170242,1993479637244170253]', 1, 1993479636925403138, '2026-04-22 23:29:25');
INSERT INTO `wf_my_task` VALUES (15, 14, 'xa', 37, '审批节点', NULL, NULL, NULL, NULL, '[1993479637244170242,1993479637244170253]', 1, 1993479636925403138, '2026-04-22 23:29:25');

-- ----------------------------
-- Table structure for wf_task_approval_action_log
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_approval_action_log`;
CREATE TABLE `wf_task_approval_action_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` bigint NOT NULL COMMENT '审批执行ID',
  `execution_detail_id` bigint NULL DEFAULT NULL COMMENT '审批执行明细ID',
  `node_id` bigint NULL DEFAULT NULL COMMENT '节点ID',
  `approval_instance_id` bigint NULL DEFAULT NULL COMMENT '审批实例ID',
  `action_type` int NOT NULL COMMENT '动作类型：1通过 2驳回 3转交 4加签 5委托 6超时通过 7超时转交 8系统关闭',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `target_user_id` bigint NULL DEFAULT NULL COMMENT '目标用户ID',
  `target_user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标用户姓名',
  `action_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '动作说明',
  `action_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '动作快照JSON',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_approval_action_execution`(`execution_id` ASC, `node_id` ASC) USING BTREE,
  INDEX `idx_wf_task_approval_action_instance`(`approval_instance_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流审批动作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_approval_action_log
-- ----------------------------
INSERT INTO `wf_task_approval_action_log` VALUES (1, 13, 30, 37, 2, 1, 1993479637244170253, '孙明岩', 1993479637244170253, '用户1993479637244170253', 'cs', '{\"actionType\":1,\"activated\":true,\"approveTime\":1776867816416,\"approverId\":1993479637244170253,\"approverName\":\"用户1993479637244170253\",\"approverSourceType\":3,\"comment\":\"cs\",\"createTime\":1776858641000,\"deleted\":0,\"executionDetailId\":30,\"executionId\":13,\"id\":2,\"instanceNo\":\"13-37-2\",\"nodeId\":37,\"sourceRuleId\":5,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1993479637311279107],\\\"approverType\\\":3}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":5,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1993479636925403138,\"updateTime\":1776858641000}', 1993479636925403138, '2026-04-22 22:23:36', 0);
INSERT INTO `wf_task_approval_action_log` VALUES (2, 14, 33, 37, 4, 1, 1993479637244170253, '孙明岩', 1993479637244170253, '用户1993479637244170253', 'cs', '{\"actionType\":1,\"activated\":true,\"approveTime\":1776871796732,\"approverId\":1993479637244170253,\"approverName\":\"用户1993479637244170253\",\"approverSourceType\":3,\"comment\":\"cs\",\"createTime\":1776871765000,\"deleted\":0,\"executionDetailId\":33,\"executionId\":14,\"id\":4,\"instanceNo\":\"14-37-2\",\"nodeId\":37,\"sourceRuleId\":5,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1993479637311279107],\\\"approverType\\\":3}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":5,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1993479636925403138,\"updateTime\":1776871765000}', 1993479636925403138, '2026-04-22 23:29:56', 0);

-- ----------------------------
-- Table structure for wf_task_approval_instance
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_approval_instance`;
CREATE TABLE `wf_task_approval_instance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` bigint NOT NULL COMMENT '审批执行ID',
  `execution_detail_id` bigint NOT NULL COMMENT '审批执行明细ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `instance_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审批实例编号',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审批人姓名',
  `approver_source_type` int NULL DEFAULT NULL COMMENT '审批来源类型',
  `source_rule_id` bigint NULL DEFAULT NULL COMMENT '来源规则ID',
  `source_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '来源快照JSON',
  `status` int NOT NULL DEFAULT 0 COMMENT '实例状态：0待处理 1已通过 2已驳回 3已转交 4已关闭',
  `action_type` int NULL DEFAULT NULL COMMENT '动作类型',
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理意见',
  `approve_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `deadline_time` datetime NULL DEFAULT NULL COMMENT '截止时间',
  `activated` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否激活',
  `delegate_from_user_id` bigint NULL DEFAULT NULL COMMENT '委托来源用户ID',
  `transfer_from_user_id` bigint NULL DEFAULT NULL COMMENT '转交来源用户ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wf_task_approval_instance_no`(`instance_no` ASC) USING BTREE,
  INDEX `idx_wf_task_approval_instance_execution`(`execution_id` ASC, `node_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_wf_task_approval_instance_approver`(`approver_id` ASC, `status` ASC, `activated` ASC) USING BTREE,
  INDEX `idx_wf_task_approval_instance_deadline`(`deadline_time` ASC, `status` ASC, `activated` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流审批运行实例表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_approval_instance
-- ----------------------------
INSERT INTO `wf_task_approval_instance` VALUES (1, 13, 30, 37, '13-37-1', 1993479637244170242, '用户1993479637244170242', 3, 5, '{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}', 4, 8, NULL, NULL, NULL, 1, NULL, NULL, 1993479636925403138, '2026-04-22 19:50:41', '2026-04-22 19:50:41', 0);
INSERT INTO `wf_task_approval_instance` VALUES (2, 13, 30, 37, '13-37-2', 1993479637244170253, '用户1993479637244170253', 3, 5, '{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}', 1, 1, 'cs', '2026-04-22 22:23:36', NULL, 1, NULL, NULL, 1993479636925403138, '2026-04-22 19:50:41', '2026-04-22 19:50:41', 0);
INSERT INTO `wf_task_approval_instance` VALUES (3, 14, 33, 37, '14-37-1', 1993479637244170242, '用户1993479637244170242', 3, 5, '{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}', 4, 8, NULL, NULL, NULL, 1, NULL, NULL, 1993479636925403138, '2026-04-22 23:29:25', '2026-04-22 23:29:25', 0);
INSERT INTO `wf_task_approval_instance` VALUES (4, 14, 33, 37, '14-37-2', 1993479637244170253, '用户1993479637244170253', 3, 5, '{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}', 1, 1, 'cs', '2026-04-22 23:29:57', NULL, 1, NULL, NULL, 1993479636925403138, '2026-04-22 23:29:25', '2026-04-22 23:29:25', 0);

-- ----------------------------
-- Table structure for wf_task_config
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_config`;
CREATE TABLE `wf_task_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT 0,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `task_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'general' COMMENT '审批分类编码',
  `interpreter_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `callback_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???? HTTP ????',
  `callback_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????? Bean ??',
  `form_type` int NULL DEFAULT NULL,
  `form_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `form_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` int NOT NULL DEFAULT 1,
  `version` int NOT NULL DEFAULT 1,
  `config_stage` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT '配置阶段：DRAFT/PUBLISHED/ARCHIVED',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `start_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `approve_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reject_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `finish_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `link_base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_task_code_version`(`tenant_id` ASC, `task_code` ASC, `version` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_task_code_stage`(`tenant_id` ASC, `task_code` ASC, `config_stage` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_config
-- ----------------------------
INSERT INTO `wf_task_config` VALUES (1, 1993479636925403138, '2026-04-04 18:36:04', NULL, '2026-04-07 20:18:40', '1993479637244170242', 0, '请假审批', '{\"zh-CN\":\"请假审批\",\"en-US\":\"Leave Approval\",\"zh-TW\":\"請假審批\",\"ja-JP\":\"休暇承認\",\"ko-KR\":\"휴가 승인\"}', 'LEAVE_APPROVAL_DEMO', 'general', 'leaveApprovalInterpreter', NULL, NULL, 1, '/workflow/form/leave', NULL, 1, 1, 'ARCHIVED', '演示流程：用户发起 -> admin 审核 -> 结束', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (4, 1993479636925403138, '2026-04-04 21:49:50', '1993479637244170242', '2026-04-07 20:18:40', '1993479637244170242', 0, '请假审批', '{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}', 'LEAVE_APPROVAL_DEMO', 'general', 'leaveApprovalInterpreter', NULL, NULL, 1, '/workflow/form/leave', NULL, 1, 2, 'PUBLISHED', '演示流程：用户发起 -> admin 审核 -> 结束', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (5, 1993479636925403138, '2026-04-05 11:48:34', '1993479637244170242', '2026-04-07 20:18:40', '1993479637244170242', 0, '请假审批', '{\"zh-CN\":\"请假审批\",\"en-US\":\"Leave Approval\",\"zh-TW\":\"請假審批\",\"ja-JP\":\"休暇承認\",\"ko-KR\":\"휴가 승인\"}', 'LEAVE_APPROVAL_DEMO', 'general', 'leaveApprovalInterpreter', NULL, NULL, 1, '/workflow/form/leave', '{\"version\":\"1.0.0\",\"fields\":[]}', 1, 3, 'DRAFT', '演示流程：用户发起 -> admin 审核 -> 结束', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (6, 1993479636925403138, '2026-04-15 17:42:42', '1993479637244170242', '2026-04-15 17:42:42', '1993479637244170242', 0, 'xa', NULL, 'cs', 'general', 'cs', NULL, NULL, 2, NULL, '{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"input\",\"field\":\"Fl98mo2ne143afc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Ffahmo2ne143agc\",\"name\":\"ref_Fer6mo2ne143ahc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false},{\"type\":\"input\",\"field\":\"field_1\",\"title\":\"字段 1\",\"col\":{\"span\":12},\"_fc_id\":\"id_Fby5mo2mrz4iabc\",\"name\":\"ref_Fa42mo2mrz4iacc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}', 1, 1, 'ARCHIVED', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (7, 1993479636925403138, '2026-04-17 16:52:52', '1993479637244170242', '2026-04-17 16:52:52', '1993479637244170242', 0, 'xa', NULL, 'cs', 'general', 'cs', NULL, NULL, 2, NULL, '{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"input\",\"field\":\"Fl98mo2ne143afc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Ffahmo2ne143agc\",\"name\":\"ref_Fer6mo2ne143ahc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false},{\"type\":\"input\",\"field\":\"field_1\",\"title\":\"字段 1\",\"col\":{\"span\":12},\"_fc_id\":\"id_Fby5mo2mrz4iabc\",\"name\":\"ref_Fa42mo2mrz4iacc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}', 1, 2, 'ARCHIVED', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (8, 1993479636925403138, '2026-04-20 10:47:01', '1993479637244170242', '2026-04-20 10:47:01', '1993479637244170242', 0, 'xa', NULL, 'cs', 'general', 'cs', NULL, NULL, 2, NULL, '{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"fcRow\",\"children\":[{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"F5r6mo9gbopqamc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Fgb2mo9gbopqanc\",\"name\":\"ref_F8zmmo9gbopqaoc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fb59mo9gbfinaic\",\"name\":\"ref_Fi5emo9gbfinajc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"},{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"Fynrmo9gbqa5apc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_F7xamo9gbqa5aqc\",\"name\":\"ref_Ff2cmo9gbqa5arc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fhc4mo9gbfinakc\",\"name\":\"ref_Fx8dmo9gbfinalc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"}],\"_fc_id\":\"id_Fgu2mo9gbfinagc\",\"name\":\"ref_Fe2lmo9gbfinahc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"fcRow\"}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}', 1, 3, 'PUBLISHED', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (9, 1993479636925403138, '2026-04-22 10:47:07', '1993479637244170242', '2026-04-22 10:47:07', '1993479637244170242', 0, 'xa', NULL, 'cs', 'general', 'cs', NULL, NULL, 2, NULL, '{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"fcRow\",\"children\":[{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"F5r6mo9gbopqamc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Fgb2mo9gbopqanc\",\"name\":\"ref_F8zmmo9gbopqaoc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fb59mo9gbfinaic\",\"name\":\"ref_Fi5emo9gbfinajc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"},{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"Fynrmo9gbqa5apc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_F7xamo9gbqa5aqc\",\"name\":\"ref_Ff2cmo9gbqa5arc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fhc4mo9gbfinakc\",\"name\":\"ref_Fx8dmo9gbfinalc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"}],\"_fc_id\":\"id_Fgu2mo9gbfinagc\",\"name\":\"ref_Fe2lmo9gbfinahc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"fcRow\"}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}', 1, 4, 'DRAFT', NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (10, 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 18:52:32', 'system', 0, '供应商资质审查', '{\"en-US\": \"Supplier Qualification Review\", \"zh-CN\": \"供应商资质审查\"}', 'SUPPLIER_QUALIFICATION_REVIEW', 'basic_supplier', NULL, 'http://forgex-basic/basic/supplier/workflow/callback', NULL, 1, '/basic/supplier/review', NULL, 1, 1, 'PUBLISHED', '供应商主数据资质审查流程：发起 -> 审批 -> 回写审查状态', NULL, NULL, NULL, NULL, NULL);
INSERT INTO `wf_task_config` VALUES (11, 1993479636925403138, '2026-04-30 10:58:17', '1993479637244170242', '2026-04-30 10:58:17', '1993479637244170242', 0, '供应商资质审查', '{\"en-US\": \"Supplier Qualification Review\", \"zh-CN\": \"供应商资质审查\"}', 'SUPPLIER_QUALIFICATION_REVIEW', 'basic_supplier', NULL, 'http://forgex-basic/basic/supplier/workflow/callback', NULL, 1, '/basic/supplier/review', NULL, 1, 2, 'DRAFT', '供应商主数据资质审查流程：发起 -> 审批 -> 回写审查状态', NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for wf_task_execution
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_execution`;
CREATE TABLE `wf_task_execution`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_config_id` bigint NOT NULL,
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `initiator_id` bigint NOT NULL,
  `initiator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `current_node_id` bigint NULL DEFAULT NULL,
  `current_node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `form_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `start_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime NULL DEFAULT NULL,
  `status` int NOT NULL DEFAULT 1,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_execution_task`(`tenant_id` ASC, `task_config_id` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wf_task_execution_initiator`(`tenant_id` ASC, `initiator_id` ASC, `status` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_execution
-- ----------------------------
INSERT INTO `wf_task_execution` VALUES (1, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 16, '审批结束', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-05\",\"endDate\":\"2026-04-05\",\"leaveDays\":1,\"reason\":\"cs\",\"handoverPerson\":\"\",\"contactPhone\":\"15678933321\"}', '2026-04-05 17:21:04', '2026-04-05 17:21:28', 2, 1993479636925403138, '2026-04-05 17:21:04', '2026-04-05 17:21:04', 0);
INSERT INTO `wf_task_execution` VALUES (2, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 15, '管理员审批', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-06 17:49:16', '2026-04-06 21:03:34', 3, 1993479636925403138, '2026-04-06 17:49:16', '2026-04-06 17:49:16', 0);
INSERT INTO `wf_task_execution` VALUES (3, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 15, '管理员审批', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"xx\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-06 23:26:13', '2026-04-06 23:47:08', 3, 1993479636925403138, '2026-04-06 23:26:12', '2026-04-06 23:26:12', 0);
INSERT INTO `wf_task_execution` VALUES (4, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 16, '审批结束', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"22\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-06 23:50:15', '2026-04-06 23:51:01', 2, 1993479636925403138, '2026-04-06 23:50:15', '2026-04-06 23:50:15', 0);
INSERT INTO `wf_task_execution` VALUES (5, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 15, '管理员审批', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-07\",\"endDate\":\"2026-04-07\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-07 08:47:28', '2026-04-07 10:57:45', 3, 1993479636925403138, '2026-04-07 08:47:28', '2026-04-07 08:47:28', 0);
INSERT INTO `wf_task_execution` VALUES (6, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 15, '管理员审批', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-07\",\"endDate\":\"2026-04-07\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-07 11:34:45', '2026-04-07 11:35:13', 3, 1993479636925403138, '2026-04-07 11:34:44', '2026-04-07 11:34:44', 0);
INSERT INTO `wf_task_execution` VALUES (7, 4, 'LEAVE_APPROVAL_DEMO', '请假审批', 1993479637244170242, 'admin', 16, '审批结束', '{\"leaveType\":\"personal\",\"startDate\":\"2026-04-09\",\"endDate\":\"2026-04-09\",\"leaveDays\":1,\"reason\":\"cs\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}', '2026-04-09 10:13:33', '2026-04-09 10:14:09', 2, 1993479636925403138, '2026-04-09 10:13:33', '2026-04-09 10:13:33', 0);
INSERT INTO `wf_task_execution` VALUES (8, 6, 'cs', 'xa', 1993479637244170242, 'admin', 24, '结束', '{\"Fl98mo2ne143afc\":\"1\",\"field_1\":\"1\"}', '2026-04-17 16:58:32', '2026-04-17 16:58:32', 2, 1993479636925403138, '2026-04-17 16:58:31', '2026-04-17 16:58:31', 0);
INSERT INTO `wf_task_execution` VALUES (13, 8, 'cs', 'xa', 1993479637244170242, 'admin', 36, '结束', '{\"F5r6mo9gbopqamc\":\"1\",\"Fynrmo9gbqa5apc\":\"1\"}', '2026-04-22 19:50:41', '2026-04-22 22:23:37', 2, 1993479636925403138, '2026-04-22 19:50:41', '2026-04-22 19:50:41', 0);
INSERT INTO `wf_task_execution` VALUES (14, 8, 'cs', 'xa', 1993479637244170253, '孙明岩', 36, '结束', '{\"F5r6mo9gbopqamc\":\"x\",\"Fynrmo9gbqa5apc\":\"x\"}', '2026-04-22 23:29:25', '2026-04-22 23:29:57', 2, 1993479636925403138, '2026-04-22 23:29:25', '2026-04-22 23:29:25', 0);

-- ----------------------------
-- Table structure for wf_task_execution_approver
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_execution_approver`;
CREATE TABLE `wf_task_execution_approver`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_detail_id` bigint NOT NULL,
  `execution_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `approver_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `reject_type` int NULL DEFAULT NULL,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_execution_approver_exec`(`tenant_id` ASC, `execution_id` ASC, `node_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_execution_approver
-- ----------------------------
INSERT INTO `wf_task_execution_approver` VALUES (1, 2, 1, 15, '[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-05 17:21:27\",\"approverName\":\"admin\"}]', NULL, 1993479636925403138, '2026-04-05 17:21:27', '2026-04-05 17:21:27');
INSERT INTO `wf_task_execution_approver` VALUES (2, 5, 2, 15, '[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-06 21:03:34\",\"approverName\":\"admin\"}]', 1, 1993479636925403138, '2026-04-06 21:03:34', '2026-04-06 21:03:34');
INSERT INTO `wf_task_execution_approver` VALUES (3, 7, 3, 15, '[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-06 23:47:07\",\"approverName\":\"admin\"}]', 1, 1993479636925403138, '2026-04-06 23:47:07', '2026-04-06 23:47:07');
INSERT INTO `wf_task_execution_approver` VALUES (4, 9, 4, 15, '[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"1\",\"approveTime\":\"2026-04-06 23:51:00\",\"approverName\":\"admin\"}]', NULL, 1993479636925403138, '2026-04-06 23:51:00', '2026-04-06 23:51:00');
INSERT INTO `wf_task_execution_approver` VALUES (5, 12, 5, 15, '[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-07 10:57:44\",\"approverName\":\"admin\"}]', 1, 1993479636925403138, '2026-04-07 10:57:44', '2026-04-07 10:57:44');
INSERT INTO `wf_task_execution_approver` VALUES (6, 14, 6, 15, '[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"1\",\"approveTime\":\"2026-04-07 11:35:12\",\"approverName\":\"admin\"}]', 1, 1993479636925403138, '2026-04-07 11:35:12', '2026-04-07 11:35:12');
INSERT INTO `wf_task_execution_approver` VALUES (7, 16, 7, 15, '[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-09 10:14:08\",\"approverName\":\"admin\"}]', NULL, 1993479636925403138, '2026-04-09 10:14:08', '2026-04-09 10:14:08');
INSERT INTO `wf_task_execution_approver` VALUES (8, 30, 13, 37, '[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 19:51:02\",\"approverName\":\"admin\"},{\"approveStatus\":1,\"approverId\":1993479637244170253,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 22:23:36\",\"approverName\":\"孙明岩\"}]', NULL, 1993479636925403138, '2026-04-22 19:51:02', '2026-04-22 19:51:02');
INSERT INTO `wf_task_execution_approver` VALUES (9, 33, 14, 37, '[{\"approveStatus\":1,\"approverId\":1993479637244170253,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 23:29:56\",\"approverName\":\"孙明岩\"}]', NULL, 1993479636925403138, '2026-04-22 23:29:56', '2026-04-22 23:29:56');

-- ----------------------------
-- Table structure for wf_task_execution_detail
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_execution_detail`;
CREATE TABLE `wf_task_execution_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `current_status` int NOT NULL DEFAULT 0,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_execution_detail_exec`(`tenant_id` ASC, `execution_id` ASC, `node_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 35 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_execution_detail
-- ----------------------------
INSERT INTO `wf_task_execution_detail` VALUES (1, 1, 14, '发起请假', 1, 1993479636925403138, '2026-04-05 17:21:04', '2026-04-05 17:21:04');
INSERT INTO `wf_task_execution_detail` VALUES (2, 1, 15, '管理员审批', 1, 1993479636925403138, '2026-04-05 17:21:04', '2026-04-05 17:21:04');
INSERT INTO `wf_task_execution_detail` VALUES (3, 1, 16, '审批结束', 1, 1993479636925403138, '2026-04-05 17:21:27', '2026-04-05 17:21:27');
INSERT INTO `wf_task_execution_detail` VALUES (4, 2, 14, '发起请假', 1, 1993479636925403138, '2026-04-06 17:49:16', '2026-04-06 17:49:16');
INSERT INTO `wf_task_execution_detail` VALUES (5, 2, 15, '管理员审批', 2, 1993479636925403138, '2026-04-06 17:49:16', '2026-04-06 17:49:16');
INSERT INTO `wf_task_execution_detail` VALUES (6, 3, 14, '发起请假', 1, 1993479636925403138, '2026-04-06 23:26:12', '2026-04-06 23:26:12');
INSERT INTO `wf_task_execution_detail` VALUES (7, 3, 15, '管理员审批', 2, 1993479636925403138, '2026-04-06 23:26:13', '2026-04-06 23:26:13');
INSERT INTO `wf_task_execution_detail` VALUES (8, 4, 14, '发起请假', 1, 1993479636925403138, '2026-04-06 23:50:15', '2026-04-06 23:50:15');
INSERT INTO `wf_task_execution_detail` VALUES (9, 4, 15, '管理员审批', 1, 1993479636925403138, '2026-04-06 23:50:15', '2026-04-06 23:50:15');
INSERT INTO `wf_task_execution_detail` VALUES (10, 4, 16, '审批结束', 1, 1993479636925403138, '2026-04-06 23:51:00', '2026-04-06 23:51:00');
INSERT INTO `wf_task_execution_detail` VALUES (11, 5, 14, '发起请假', 1, 1993479636925403138, '2026-04-07 08:47:28', '2026-04-07 08:47:28');
INSERT INTO `wf_task_execution_detail` VALUES (12, 5, 15, '管理员审批', 2, 1993479636925403138, '2026-04-07 08:47:28', '2026-04-07 08:47:28');
INSERT INTO `wf_task_execution_detail` VALUES (13, 6, 14, '发起请假', 1, 1993479636925403138, '2026-04-07 11:34:44', '2026-04-07 11:34:44');
INSERT INTO `wf_task_execution_detail` VALUES (14, 6, 15, '管理员审批', 2, 1993479636925403138, '2026-04-07 11:34:44', '2026-04-07 11:34:44');
INSERT INTO `wf_task_execution_detail` VALUES (15, 7, 14, '发起请假', 1, 1993479636925403138, '2026-04-09 10:13:33', '2026-04-09 10:13:33');
INSERT INTO `wf_task_execution_detail` VALUES (16, 7, 15, '管理员审批', 1, 1993479636925403138, '2026-04-09 10:13:33', '2026-04-09 10:13:33');
INSERT INTO `wf_task_execution_detail` VALUES (17, 7, 16, '审批结束', 1, 1993479636925403138, '2026-04-09 10:14:08', '2026-04-09 10:14:08');
INSERT INTO `wf_task_execution_detail` VALUES (18, 8, 23, '开始', 1, 1993479636925403138, '2026-04-17 16:58:31', '2026-04-17 16:58:31');
INSERT INTO `wf_task_execution_detail` VALUES (19, 8, 25, '审批节点', 1, 1993479636925403138, '2026-04-17 16:58:31', '2026-04-17 16:58:31');
INSERT INTO `wf_task_execution_detail` VALUES (20, 8, 24, '结束', 1, 1993479636925403138, '2026-04-17 16:58:31', '2026-04-17 16:58:31');
INSERT INTO `wf_task_execution_detail` VALUES (29, 13, 35, '开始', 1, 1993479636925403138, '2026-04-22 19:50:41', '2026-04-22 19:50:41');
INSERT INTO `wf_task_execution_detail` VALUES (30, 13, 37, '审批节点', 1, 1993479636925403138, '2026-04-22 19:50:41', '2026-04-22 19:50:41');
INSERT INTO `wf_task_execution_detail` VALUES (31, 13, 36, '结束', 1, 1993479636925403138, '2026-04-22 22:23:36', '2026-04-22 22:23:36');
INSERT INTO `wf_task_execution_detail` VALUES (32, 14, 35, '开始', 1, 1993479636925403138, '2026-04-22 23:29:25', '2026-04-22 23:29:25');
INSERT INTO `wf_task_execution_detail` VALUES (33, 14, 37, '审批节点', 1, 1993479636925403138, '2026-04-22 23:29:25', '2026-04-22 23:29:25');
INSERT INTO `wf_task_execution_detail` VALUES (34, 14, 36, '结束', 1, 1993479636925403138, '2026-04-22 23:29:56', '2026-04-22 23:29:56');

-- ----------------------------
-- Table structure for wf_task_node_approver
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_node_approver`;
CREATE TABLE `wf_task_node_approver`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `node_config_id` bigint NOT NULL,
  `approver_type` int NOT NULL,
  `approver_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_node_approver_node`(`tenant_id` ASC, `node_config_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_node_approver
-- ----------------------------
INSERT INTO `wf_task_node_approver` VALUES (1, 2, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-04 18:36:04', 0);
INSERT INTO `wf_task_node_approver` VALUES (2, 5, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-04 21:10:44', 0);
INSERT INTO `wf_task_node_approver` VALUES (3, 8, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_approver` VALUES (4, 9, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_approver` VALUES (5, 12, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-05 11:48:24', 1);
INSERT INTO `wf_task_node_approver` VALUES (6, 15, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-05 11:48:26', 0);
INSERT INTO `wf_task_node_approver` VALUES (7, 18, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-05 11:48:34', 0);
INSERT INTO `wf_task_node_approver` VALUES (8, 21, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-05 14:36:01', 0);
INSERT INTO `wf_task_node_approver` VALUES (9, 25, 3, '[1993479637311279000]', 1993479636925403138, '2026-04-17 16:52:47', 0);
INSERT INTO `wf_task_node_approver` VALUES (10, 28, 3, '[1993479637311279000]', 1993479636925403138, '2026-04-17 16:52:52', 1);
INSERT INTO `wf_task_node_approver` VALUES (11, 31, 3, '[1993479637311279107]', 1993479636925403138, '2026-04-17 17:29:01', 0);
INSERT INTO `wf_task_node_approver` VALUES (12, 34, 3, '[1993479637311279107]', 1993479636925403138, '2026-04-20 10:47:01', 1);
INSERT INTO `wf_task_node_approver` VALUES (13, 37, 3, '[1993479637311279107]', 1993479636925403138, '2026-04-22 10:46:59', 0);
INSERT INTO `wf_task_node_approver` VALUES (14, 40, 3, '[1993479637311279107]', 1993479636925403138, '2026-04-22 10:47:06', 0);
INSERT INTO `wf_task_node_approver` VALUES (15, 42, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-26 16:26:06', 0);
INSERT INTO `wf_task_node_approver` VALUES (16, 45, 1, '[1993479637244170242]', 1993479636925403138, '2026-04-30 10:58:17', 0);

-- ----------------------------
-- Table structure for wf_task_node_config
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_node_config`;
CREATE TABLE `wf_task_node_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_config_id` bigint NOT NULL,
  `node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设计器稳定节点标识',
  `node_type` int NOT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `node_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `node_level` int NULL DEFAULT NULL,
  `pre_level` int NULL DEFAULT NULL,
  `pre_node_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `next_level` int NULL DEFAULT NULL,
  `next_node_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `approve_type` int NULL DEFAULT NULL,
  `branch_conditions` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `canvas_x` decimal(10, 2) NULL DEFAULT NULL COMMENT '画布 X 坐标',
  `canvas_y` decimal(10, 2) NULL DEFAULT NULL COMMENT '画布 Y 坐标',
  `order_num` int NULL DEFAULT 0,
  `tenant_id` bigint NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_node_config_task`(`tenant_id` ASC, `task_config_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_task_config_node_key`(`task_config_id` ASC, `node_key` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_node_config
-- ----------------------------
INSERT INTO `wf_task_node_config` VALUES (1, 1, NULL, 1, '发起请假', '{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}', 1, 0, '[]', 2, '[2]', NULL, NULL, NULL, NULL, 1, 1993479636925403138, '2026-04-04 18:36:04', 0);
INSERT INTO `wf_task_node_config` VALUES (2, 1, NULL, 3, '管理员审批', '{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}', 2, 1, '[1]', 3, '[3]', 2, NULL, NULL, NULL, 2, 1993479636925403138, '2026-04-04 18:36:04', 0);
INSERT INTO `wf_task_node_config` VALUES (3, 1, NULL, 2, '审批结束', '{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}', 3, 2, '[2]', 0, '[]', NULL, NULL, NULL, NULL, 3, 1993479636925403138, '2026-04-04 18:36:04', 0);
INSERT INTO `wf_task_node_config` VALUES (4, 1, 'start', 1, '发起请假', '{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}', 1, 0, '[]', 2, '[5]', NULL, NULL, 120.00, 240.00, 1, 1993479636925403138, '2026-04-04 21:10:44', 0);
INSERT INTO `wf_task_node_config` VALUES (5, 1, 'approve_2', 3, '管理员审批', '{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}', 2, 1, '[4]', 3, '[6]', 2, NULL, 420.00, 240.00, 2, 1993479636925403138, '2026-04-04 21:10:44', 0);
INSERT INTO `wf_task_node_config` VALUES (6, 1, 'end', 2, '审批结束', '{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}', 3, 2, '[5]', 0, '[]', NULL, NULL, 720.00, 240.00, 3, 1993479636925403138, '2026-04-04 21:10:44', 0);
INSERT INTO `wf_task_node_config` VALUES (7, 4, 'node_1', 1, '发起请假', '{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}', 1, 0, '[]', 2, '[8,9]', NULL, NULL, 0.00, 0.00, 1, 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_config` VALUES (8, 4, 'node_2', 3, '管理员审批', '{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}', 2, 1, '[7]', 3, '[10]', 2, NULL, 0.00, 0.00, 2, 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_config` VALUES (9, 4, 'approve_2', 3, '管理员审批', '{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}', 2, 1, '[7]', 3, '[10]', 2, NULL, 420.00, 240.00, 3, 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_config` VALUES (10, 4, 'node_3', 2, '审批结束', '{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}', 3, 2, '[8,9]', 0, '[]', NULL, NULL, 0.00, 0.00, 4, 1993479636925403138, '2026-04-04 21:49:49', 1);
INSERT INTO `wf_task_node_config` VALUES (11, 4, 'node_1', 1, '发起请假', '{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}', 1, 0, '[]', 2, '[12]', NULL, NULL, 188.52, -245.96, 1, 1993479636925403138, '2026-04-05 11:48:23', 1);
INSERT INTO `wf_task_node_config` VALUES (12, 4, 'node_2', 3, '管理员审批', '{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}', 2, 1, '[11]', 3, '[13]', 2, NULL, 157.02, -8.06, 2, 1993479636925403138, '2026-04-05 11:48:23', 1);
INSERT INTO `wf_task_node_config` VALUES (13, 4, 'node_3', 2, '审批结束', '{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}', 3, 2, '[12]', 0, '[]', NULL, NULL, 171.40, 293.51, 3, 1993479636925403138, '2026-04-05 11:48:23', 1);
INSERT INTO `wf_task_node_config` VALUES (14, 4, 'node_1', 1, '发起请假', '{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}', 1, 0, '[]', 2, '[15]', NULL, NULL, 188.52, -245.96, 1, 1993479636925403138, '2026-04-05 11:48:26', 0);
INSERT INTO `wf_task_node_config` VALUES (15, 4, 'node_2', 3, '管理员审批', '{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}', 2, 1, '[14]', 3, '[16]', 2, NULL, 157.02, -8.06, 2, 1993479636925403138, '2026-04-05 11:48:26', 0);
INSERT INTO `wf_task_node_config` VALUES (16, 4, 'node_3', 2, '审批结束', '{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}', 3, 2, '[15]', 0, '[]', NULL, NULL, 171.40, 293.51, 3, 1993479636925403138, '2026-04-05 11:48:26', 0);
INSERT INTO `wf_task_node_config` VALUES (17, 5, 'node_1', 1, '发起请假', '{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}', 1, 0, '[]', 2, '[18]', NULL, NULL, 188.52, -245.96, 1, 1993479636925403138, '2026-04-05 11:48:34', 0);
INSERT INTO `wf_task_node_config` VALUES (18, 5, 'node_2', 3, '管理员审批', '{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}', 2, 1, '[17]', 3, '[19]', 2, NULL, 157.02, -8.06, 2, 1993479636925403138, '2026-04-05 11:48:34', 0);
INSERT INTO `wf_task_node_config` VALUES (19, 5, 'node_3', 2, '审批结束', '{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}', 3, 2, '[18]', 0, '[]', NULL, NULL, 171.40, 293.51, 3, 1993479636925403138, '2026-04-05 11:48:34', 0);
INSERT INTO `wf_task_node_config` VALUES (20, 4, 'start', 1, '发起请假', '{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}', 1, 0, '[]', 2, '[21]', NULL, NULL, 120.00, 240.00, 1, 1993479636925403138, '2026-04-05 14:36:01', 0);
INSERT INTO `wf_task_node_config` VALUES (21, 4, 'approve_2', 3, '管理员审批', '{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}', 2, 1, '[20]', 3, '[22]', 2, NULL, 420.00, 240.00, 2, 1993479636925403138, '2026-04-05 14:36:01', 0);
INSERT INTO `wf_task_node_config` VALUES (22, 4, 'end', 2, '审批结束', '{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}', 3, 2, '[21]', 0, '[]', NULL, NULL, 720.00, 240.00, 3, 1993479636925403138, '2026-04-05 14:36:01', 0);
INSERT INTO `wf_task_node_config` VALUES (23, 6, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[25]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-17 16:52:47', 0);
INSERT INTO `wf_task_node_config` VALUES (24, 6, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[25]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-17 16:52:47', 0);
INSERT INTO `wf_task_node_config` VALUES (25, 6, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[23]', 3, '[24]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-17 16:52:47', 0);
INSERT INTO `wf_task_node_config` VALUES (26, 7, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[28]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-17 16:52:52', 1);
INSERT INTO `wf_task_node_config` VALUES (27, 7, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[28]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-17 16:52:52', 1);
INSERT INTO `wf_task_node_config` VALUES (28, 7, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[26]', 3, '[27]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-17 16:52:52', 1);
INSERT INTO `wf_task_node_config` VALUES (29, 7, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[31]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-17 17:29:01', 0);
INSERT INTO `wf_task_node_config` VALUES (30, 7, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[31]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-17 17:29:01', 0);
INSERT INTO `wf_task_node_config` VALUES (31, 7, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[29]', 3, '[30]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-17 17:29:01', 0);
INSERT INTO `wf_task_node_config` VALUES (32, 8, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[34]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-20 10:47:01', 1);
INSERT INTO `wf_task_node_config` VALUES (33, 8, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[34]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-20 10:47:01', 1);
INSERT INTO `wf_task_node_config` VALUES (34, 8, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[32]', 3, '[33]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-20 10:47:01', 1);
INSERT INTO `wf_task_node_config` VALUES (35, 8, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[37]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-22 10:46:59', 0);
INSERT INTO `wf_task_node_config` VALUES (36, 8, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[37]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-22 10:46:59', 0);
INSERT INTO `wf_task_node_config` VALUES (37, 8, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[35]', 3, '[36]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-22 10:46:59', 0);
INSERT INTO `wf_task_node_config` VALUES (38, 9, 'start', 1, '开始', '{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}', 1, 0, '[]', 2, '[40]', NULL, NULL, 58.33, 124.70, 1, 1993479636925403138, '2026-04-22 10:47:06', 0);
INSERT INTO `wf_task_node_config` VALUES (39, 9, 'end', 2, '结束', '{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}', 3, 2, '[40]', 0, '[]', NULL, NULL, 38.50, 554.52, 2, 1993479636925403138, '2026-04-22 10:47:06', 0);
INSERT INTO `wf_task_node_config` VALUES (40, 9, 'approve_1776415906033_voso', 3, '审批节点', '{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}', 2, 1, '[38]', 3, '[39]', 2, NULL, 22.37, 317.49, 3, 1993479636925403138, '2026-04-22 10:47:06', 0);
INSERT INTO `wf_task_node_config` VALUES (41, 10, 'start', 1, '发起审查', '{\"en-US\": \"Start Review\", \"zh-CN\": \"发起审查\"}', 1, 0, '[]', 2, '[42]', NULL, NULL, 120.00, 240.00, 1, 1993479636925403138, '2026-04-26 16:26:06', 0);
INSERT INTO `wf_task_node_config` VALUES (42, 10, 'approve_1', 3, '资质审查', '{\"en-US\": \"Qualification Review\", \"zh-CN\": \"资质审查\"}', 2, 1, '[41]', 3, '[43]', 2, NULL, 420.00, 240.00, 2, 1993479636925403138, '2026-04-26 16:26:06', 0);
INSERT INTO `wf_task_node_config` VALUES (43, 10, 'end', 2, '审查结束', '{\"en-US\": \"Review End\", \"zh-CN\": \"审查结束\"}', 3, 2, '[42]', 0, '[]', NULL, NULL, 720.00, 240.00, 3, 1993479636925403138, '2026-04-26 16:26:06', 0);
INSERT INTO `wf_task_node_config` VALUES (44, 11, 'start', 1, '发起审查', '{\"en-US\":\"发起审查\",\"zh-CN\":\"发起审查\"}', 1, 0, '[]', 2, '[45]', NULL, NULL, 120.00, 240.00, 1, 1993479636925403138, '2026-04-30 10:58:17', 0);
INSERT INTO `wf_task_node_config` VALUES (45, 11, 'approve_1', 3, '资质审查', '{\"en-US\":\"资质审查\",\"zh-CN\":\"资质审查\"}', 2, 1, '[44]', 3, '[46]', 2, NULL, 420.00, 240.00, 2, 1993479636925403138, '2026-04-30 10:58:17', 0);
INSERT INTO `wf_task_node_config` VALUES (46, 11, 'end', 2, '审查结束', '{\"en-US\":\"审查结束\",\"zh-CN\":\"审查结束\"}', 3, 2, '[45]', 0, '[]', NULL, NULL, 720.00, 240.00, 3, 1993479636925403138, '2026-04-30 10:58:17', 0);

-- ----------------------------
-- Table structure for wf_task_node_rule
-- ----------------------------
DROP TABLE IF EXISTS `wf_task_node_rule`;
CREATE TABLE `wf_task_node_rule`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `node_config_id` bigint NOT NULL COMMENT '节点配置ID',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规则名称',
  `rule_type` int NOT NULL COMMENT '规则类型：1静态审批 2发起人自选 3多级上级 4动态追加',
  `approve_mode` int NOT NULL COMMENT '审批模式',
  `approval_threshold` decimal(10, 2) NULL DEFAULT NULL COMMENT '会签阈值',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `timeout_hours` int NULL DEFAULT NULL COMMENT '超时小时数',
  `timeout_action` int NULL DEFAULT NULL COMMENT '超时动作：1提醒 2自动通过 3自动转交',
  `allow_initiator_select` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许发起人自选审批人',
  `superior_level` int NULL DEFAULT NULL COMMENT '上级层级',
  `allow_add_sign` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许加签',
  `allow_transfer` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许转交',
  `allow_delegate` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许委托',
  `allow_recall` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否允许发起人撤回',
  `fallback_approver_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '兜底审批人ID列表JSON',
  `extra_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '扩展配置JSON',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_wf_task_node_rule_node`(`node_config_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_wf_task_node_rule_tenant`(`tenant_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工作流节点运行规则表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wf_task_node_rule
-- ----------------------------
INSERT INTO `wf_task_node_rule` VALUES (1, 25, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-17 16:52:47', '2026-04-17 16:52:47', 0);
INSERT INTO `wf_task_node_rule` VALUES (2, 28, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-17 16:52:52', '2026-04-17 17:29:01', 1);
INSERT INTO `wf_task_node_rule` VALUES (3, 31, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-17 17:29:01', '2026-04-17 17:29:01', 0);
INSERT INTO `wf_task_node_rule` VALUES (4, 34, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-20 10:47:01', '2026-04-22 10:46:58', 1);
INSERT INTO `wf_task_node_rule` VALUES (5, 37, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-22 10:46:59', '2026-04-22 10:46:59', 0);
INSERT INTO `wf_task_node_rule` VALUES (6, 40, '默认规则', 1, 2, NULL, 1, NULL, NULL, 0, 1, 0, 0, 0, 1, '[]', NULL, 1993479636925403138, '2026-04-22 10:47:06', '2026-04-22 10:47:06', 0);

SET FOREIGN_KEY_CHECKS = 1;
