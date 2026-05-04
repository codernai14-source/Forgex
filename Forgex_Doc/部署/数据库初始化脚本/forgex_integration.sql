/*
 Navicat Premium Dump SQL

 Source Server         : bendi-127
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : forgex_integration

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 03/05/2026 18:03:32
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for fx_api_call_log_202604
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_call_log_202604`;
CREATE TABLE `fx_api_call_log_202604`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `api_config_id` bigint NOT NULL COMMENT '接口配置表 ID',
  `call_direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用方向：INBOUND-外对内，OUTBOUND-内调外',
  `caller_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用方 IP',
  `request_data` json NULL COMMENT '请求参数（JSON 格式）',
  `response_data` json NULL COMMENT '响应数据（JSON 格式）',
  `call_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用状态：SUCCESS-成功，FAIL-失败',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误信息',
  `cost_time_ms` int NOT NULL DEFAULT 0 COMMENT '调用耗时（毫秒）',
  `call_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_call_time`(`call_time` ASC) USING BTREE,
  INDEX `idx_call_status`(`call_status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口调用记录表 2026-04' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_call_log_202604
-- ----------------------------

-- ----------------------------
-- Table structure for fx_api_config
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_config`;
CREATE TABLE `fx_api_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `api_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口编码（唯一）',
  `api_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口名称',
  `api_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口描述',
  `direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作方向：INBOUND-外对内，OUTBOUND-内调外',
  `api_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '接口路径（外对内必填）',
  `processor_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理器 bean 名称（外对内必填）',
  `call_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'HTTP' COMMENT '调用方式：HTTP, TCP',
  `http_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP请求方法',
  `invoke_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行模式:SYNC/ASYNC',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容类型',
  `target_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标地址（内调外时填写）',
  `timeout_ms` int NOT NULL DEFAULT 30000 COMMENT '超时时间（毫秒）',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `retry_interval_ms` int NULL DEFAULT 0 COMMENT '重试间隔毫秒',
  `max_concurrent` int NULL DEFAULT 0 COMMENT '单接口最大并发',
  `queue_limit` int NULL DEFAULT 0 COMMENT '单接口等待队列上限',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证方式',
  `auth_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '认证配置JSON',
  `call_count` bigint NOT NULL DEFAULT 0 COMMENT '调用次数',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '所属模块编码',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_api_code`(`api_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_processor_bean`(`processor_bean` ASC) USING BTREE,
  INDEX `idx_api_path`(`api_path` ASC) USING BTREE,
  INDEX `idx_direction`(`direction` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2045801208530558984 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口配置主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_config
-- ----------------------------
INSERT INTO `fx_api_config` VALUES (2045801208530558977, 'cs', 'cs', 'cs', 'INBOUND', 'cs', 'cs', 'HTTP', NULL, NULL, NULL, 'cs', 3000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'cs', 1993479636925403138, '2026-04-19 17:46:32', 'admin', '2026-04-19 17:46:32', 'admin', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558978, 'sys_user_sync', 'Sync Users To Third Party', 'Push current tenant users to configured third-party systems.', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:38:40', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558979, 'sys_user_pull', 'Pull Users From Third Party', 'Pull users from configured third-party systems and sync to current tenant.', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:38:40', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558980, 'sys_user_third_party_inbound', 'Third Party Push Users', 'Receive third-party user payload via public integration endpoint.', 'INBOUND', '/api/integration/public/invoke', 'userThirdPartyInboundInterpreter', 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558981, 'basic_supplier_sync', 'Sync Suppliers To Third Party', 'Push full supplier master data to configured third-party systems.', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'basic', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 18:52:32', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558982, 'basic_supplier_pull', 'Pull Suppliers From Third Party', 'Pull supplier master data from configured third-party systems and write to Basic.', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'basic', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 18:52:32', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558983, 'basic_supplier_master_inbound', 'Third Party Push Suppliers', 'Receive supplier master payload via public integration endpoint.', 'INBOUND', '/api/integration/public/invoke', 'supplierMasterInboundInterpreter', 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'basic', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 18:52:32', 'system', 0);

-- ----------------------------
-- Table structure for fx_api_outbound_target
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_outbound_target`;
CREATE TABLE `fx_api_outbound_target`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NULL DEFAULT 0 COMMENT '租户 ID',
  `api_config_id` bigint NOT NULL COMMENT '接口主配置 ID',
  `third_system_id` bigint NOT NULL COMMENT '第三方系统 ID',
  `target_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统编码',
  `target_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统名称',
  `target_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标地址',
  `http_method` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP 方法',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内容类型',
  `invoke_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行模式',
  `timeout_ms` int NULL DEFAULT 30000 COMMENT '超时时间(ms)',
  `retry_count` int NULL DEFAULT 0 COMMENT '重试次数',
  `retry_interval_ms` int NULL DEFAULT 0 COMMENT '重试间隔(ms)',
  `order_num` int NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '是否删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_third_system_id`(`third_system_id` ASC) USING BTREE,
  INDEX `idx_target_code`(`target_code` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '内调外目标配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_outbound_target
-- ----------------------------
INSERT INTO `fx_api_outbound_target` VALUES (1, 1993479636925403138, 2045801208530558978, 2045037285200052226, 'cs', 'cs', 'http://192.168.0.24/api/user/sync', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, 'Auto generated target for sys_user_sync. Update target_url if needed.', '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_outbound_target` VALUES (2, 1993479636925403138, 2045801208530558979, 2045037285200052226, 'cs', 'cs', 'http://192.168.0.24/api/user/pull', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, 'Auto generated target for sys_user_pull. Update target_url if needed.', '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_outbound_target` VALUES (3, 1993479636925403138, 2045801208530558981, 2045037285200052226, 'cs', 'cs', 'http://192.168.0.24/api/supplier/sync', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, 'Auto generated target for basic_supplier_sync. Update target_url if needed.', '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_outbound_target` VALUES (4, 1993479636925403138, 2045801208530558982, 2045037285200052226, 'cs', 'cs', 'http://192.168.0.24/api/supplier/pull', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, 'Auto generated target for basic_supplier_pull. Update target_url if needed.', '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);

-- ----------------------------
-- Table structure for fx_api_param_config
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_param_config`;
CREATE TABLE `fx_api_param_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `api_config_id` bigint NOT NULL COMMENT '接口配置表 ID',
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT '目标配置 ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父节点 ID（树形结构）',
  `direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '参数方向：REQUEST-请求，RESPONSE-响应',
  `node_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点类型：OBJECT-集合，ARRAY-数组，FIELD-字段',
  `field_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段值（传参的字段名）',
  `field_label` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段显示名称',
  `field_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字段类型（string, number, boolean, array, object）',
  `field_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字段完整路径（如：data.user.name）',
  `required` tinyint NOT NULL DEFAULT 0 COMMENT '是否必填：0-否，1-是',
  `default_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `dict_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典编码（用于翻译）',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_direction`(`direction` ASC) USING BTREE,
  INDEX `idx_field_path`(`field_path`(255) ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2045801872316915750 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_param_config
-- ----------------------------
INSERT INTO `fx_api_param_config` VALUES (2045801783389282306, 2045801208530558977, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:48:49', 'admin', '2026-04-19 17:48:49', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801783448002561, 2045801208530558977, NULL, NULL, 'REQUEST', 'FIELD', 'userId', 'userId', 'number', 'userId', 0, '0', NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:48:49', 'admin', '2026-04-19 17:48:49', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872283361281, 2045801208530558977, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:49:10', 'admin', '2026-04-19 17:49:10', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915713, 2045801208530558977, NULL, NULL, 'RESPONSE', 'FIELD', 'userId', 'userId', 'number', 'userId', 0, '0', NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:49:10', 'admin', '2026-04-19 17:49:10', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915714, 2045801208530558978, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Request root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915715, 2045801208530558978, NULL, 2045801872316915714, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915716, 2045801208530558978, NULL, 2045801872316915714, 'REQUEST', 'FIELD', 'users', 'users', 'array', 'root.users', 1, NULL, NULL, 2, 'User list', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915718, 2045801208530558978, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Response root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915719, 2045801208530558979, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Request root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915720, 2045801208530558979, NULL, 2045801872316915719, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915721, 2045801208530558979, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Response root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915722, 2045801208530558979, NULL, 2045801872316915721, 'RESPONSE', 'FIELD', 'users', 'users', 'array', 'root.users', 0, NULL, NULL, 1, 'Pulled user list', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915723, 2045801208530558980, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Request root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915724, 2045801208530558980, NULL, 2045801872316915723, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915725, 2045801208530558980, NULL, 2045801872316915723, 'REQUEST', 'FIELD', 'users', 'users', 'array', 'root.users', 1, NULL, NULL, 2, 'User list', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915727, 2045801208530558980, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Response root node', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915728, 2045801208530558980, NULL, 2045801872316915727, 'RESPONSE', 'FIELD', 'totalCount', 'totalCount', 'number', 'root.totalCount', 0, NULL, NULL, 1, 'Total records', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915729, 2045801208530558980, NULL, 2045801872316915727, 'RESPONSE', 'FIELD', 'createdCount', 'createdCount', 'number', 'root.createdCount', 0, NULL, NULL, 2, 'Created records', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915730, 2045801208530558980, NULL, 2045801872316915727, 'RESPONSE', 'FIELD', 'updatedCount', 'updatedCount', 'number', 'root.updatedCount', 0, NULL, NULL, 3, 'Updated records', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915731, 2045801208530558980, NULL, 2045801872316915727, 'RESPONSE', 'FIELD', 'failedCount', 'failedCount', 'number', 'root.failedCount', 0, NULL, NULL, 4, 'Failed records', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915732, 2045801208530558981, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier outbound request root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915733, 2045801208530558981, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier outbound response root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915734, 2045801208530558982, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier pull request root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915735, 2045801208530558982, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier pull response root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915736, 2045801208530558983, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier inbound request root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915737, 2045801208530558983, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, 'Supplier inbound response root', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915739, 2045801208530558981, NULL, 2045801872316915732, 'REQUEST', 'ARRAY', 'suppliers', 'suppliers', 'array', 'root.suppliers', 1, NULL, NULL, 2, 'Supplier aggregate list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915740, 2045801208530558981, NULL, 2045801872316915732, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 0, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915741, 2045801208530558981, NULL, 2045801872316915733, 'RESPONSE', 'FIELD', 'status', 'status', 'string', 'root.status', 0, NULL, NULL, 1, 'Execution status', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915742, 2045801208530558982, NULL, 2045801872316915734, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 0, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915743, 2045801208530558982, NULL, 2045801872316915735, 'RESPONSE', 'ARRAY', 'suppliers', 'suppliers', 'array', 'root.suppliers', 1, NULL, NULL, 1, 'Supplier aggregate list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915744, 2045801208530558983, NULL, 2045801872316915736, 'REQUEST', 'ARRAY', 'suppliers', 'suppliers', 'array', 'root.suppliers', 1, NULL, NULL, 2, 'Supplier aggregate list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915745, 2045801208530558983, NULL, 2045801872316915736, 'REQUEST', 'FIELD', 'tenantId', 'tenantId', 'number', 'root.tenantId', 0, NULL, NULL, 1, 'Tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915746, 2045801208530558983, NULL, 2045801872316915737, 'RESPONSE', 'FIELD', 'createdCount', 'createdCount', 'number', 'root.createdCount', 0, NULL, NULL, 2, 'Created records', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915747, 2045801208530558983, NULL, 2045801872316915737, 'RESPONSE', 'FIELD', 'failedCount', 'failedCount', 'number', 'root.failedCount', 0, NULL, NULL, 4, 'Failed records', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915748, 2045801208530558983, NULL, 2045801872316915737, 'RESPONSE', 'FIELD', 'totalCount', 'totalCount', 'number', 'root.totalCount', 0, NULL, NULL, 1, 'Total records', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915749, 2045801208530558983, NULL, 2045801872316915737, 'RESPONSE', 'FIELD', 'updatedCount', 'updatedCount', 'number', 'root.updatedCount', 0, NULL, NULL, 3, 'Updated records', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);

-- ----------------------------
-- Table structure for fx_api_param_mapping
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_param_mapping`;
CREATE TABLE `fx_api_param_mapping`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `api_config_id` bigint NOT NULL COMMENT '接口配置表 ID',
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT '目标配置 ID',
  `source_field_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '源字段路径',
  `target_field_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标字段路径',
  `transform_rule` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '转换规则（JSON 表达式或函数名）',
  `default_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认值',
  `constant_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '常量值',
  `target_scope` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '映射目标区域:BODY/QUERY/HEADER/PATH',
  `value_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '取值类型:SOURCE/DEFAULT/CONSTANT',
  `direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '映射方向：INBOUND-外对内，OUTBOUND-内调外',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_direction`(`direction` ASC) USING BTREE,
  INDEX `idx_source_field`(`source_field_path`(255) ASC) USING BTREE,
  INDEX `idx_target_field`(`target_field_path`(255) ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2045801982157348883 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口参数映射表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_param_mapping
-- ----------------------------
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348865, 2045801208530558977, NULL, 'userId', 'userId', NULL, NULL, NULL, NULL, NULL, 'INBOUND', NULL, 1993479636925403138, '2026-04-19 17:49:36', 'admin', '2026-04-19 17:49:36', 'admin', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348866, 2045801208530558978, 1, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348867, 2045801208530558978, 1, 'users', 'users', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map user list', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348869, 2045801208530558979, 2, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348870, 2045801208530558980, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map tenant id', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348871, 2045801208530558980, NULL, 'users', 'users', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map user list', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348872, 2045801208530558981, NULL, 'suppliers', 'suppliers', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348873, 2045801208530558981, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348874, 2045801208530558982, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348875, 2045801208530558983, NULL, 'suppliers', 'suppliers', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348876, 2045801208530558983, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348879, 2045801208530558981, 3, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348880, 2045801208530558981, 3, 'suppliers', 'suppliers', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348882, 2045801208530558982, 4, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);

-- ----------------------------
-- Table structure for fx_api_task
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_task`;
CREATE TABLE `fx_api_task`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NULL DEFAULT 0,
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_config_id` bigint NOT NULL,
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT '目标配置 ID',
  `target_system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统编码',
  `target_system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统名称',
  `api_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `processor_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `invoke_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `request_payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `assembled_payload` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `retry_count` int NULL DEFAULT 0,
  `max_retry_count` int NULL DEFAULT 0,
  `next_execute_time` datetime NULL DEFAULT NULL,
  `lease_expire_time` datetime NULL DEFAULT NULL,
  `started_time` datetime NULL DEFAULT NULL,
  `finished_time` datetime NULL DEFAULT NULL,
  `result_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_status_next_time`(`status` ASC, `next_execute_time` ASC) USING BTREE,
  INDEX `idx_api_code`(`api_code` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口异步任务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_task
-- ----------------------------

-- ----------------------------
-- Table structure for fx_api_task_result
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_task_result`;
CREATE TABLE `fx_api_task_result`  (
  `id` bigint NOT NULL,
  `tenant_id` bigint NULL DEFAULT 0,
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `api_config_id` bigint NOT NULL,
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT '目标配置 ID',
  `target_system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统编码',
  `target_system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标系统名称',
  `api_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `direction` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `result_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `result_data` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `error_message` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cost_time_ms` int NULL DEFAULT 0,
  `finished_time` datetime NULL DEFAULT NULL,
  `expire_time` datetime NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_result_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_api_code`(`api_code` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口异步任务结果表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_task_result
-- ----------------------------

-- ----------------------------
-- Table structure for fx_third_authorization
-- ----------------------------
DROP TABLE IF EXISTS `fx_third_authorization`;
CREATE TABLE `fx_third_authorization`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `third_system_id` bigint NOT NULL COMMENT '第三方系统 ID',
  `auth_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '授权方式：WHITELIST-白名单，TOKEN-限时 token',
  `token_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Token 值（授权方式为 TOKEN 时必填）',
  `token_expire_hours` int NULL DEFAULT NULL COMMENT 'Token 有效期（小时）',
  `token_expire_time` datetime NULL DEFAULT NULL COMMENT 'Token 过期时间',
  `whitelist_ips` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '白名单 IP 列表（授权方式为 WHITELIST 时使用）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_third_system_id`(`third_system_id` ASC) USING BTREE,
  INDEX `idx_token_value`(`token_value` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方授权表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_third_authorization
-- ----------------------------

-- ----------------------------
-- Table structure for fx_third_system
-- ----------------------------
DROP TABLE IF EXISTS `fx_third_system`;
CREATE TABLE `fx_third_system`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方系统编码（唯一）',
  `system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '第三方系统名称',
  `ip_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '第三方系统 IP（多个用逗号分隔）',
  `contact_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系信息',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_system_code`(`system_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2045037285200052227 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方系统信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_third_system
-- ----------------------------
INSERT INTO `fx_third_system` VALUES (2045037285200052226, 'cs', 'cs', '192.168.0.24', '', '', 1, 1993479636925403138, '2026-04-17 15:10:58', 'admin', '2026-04-17 15:10:58', 'admin', 0);

SET FOREIGN_KEY_CHECKS = 1;
