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

 Date: 12/05/2026 14:00:29
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
-- Table structure for fx_api_call_log_202605
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_call_log_202605`;
CREATE TABLE `fx_api_call_log_202605`  (
  `id` bigint NOT NULL COMMENT 'primary key',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'tenant id',
  `api_config_id` bigint NOT NULL COMMENT 'api config id',
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT 'outbound target id',
  `target_system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'target system code',
  `target_system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'target system name',
  `api_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'api code',
  `call_direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'call direction',
  `caller_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'caller ip',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'trace id',
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'task id',
  `invoke_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'invoke mode',
  `request_data` json NULL COMMENT 'request payload',
  `raw_request_data` json NULL COMMENT 'raw request payload',
  `assembled_request_data` json NULL COMMENT 'assembled request payload',
  `response_data` json NULL COMMENT 'response payload',
  `response_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'response code',
  `call_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'call status',
  `result_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'result type',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'error message',
  `cost_time_ms` int NOT NULL DEFAULT 0 COMMENT 'cost time ms',
  `call_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'call time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'create by',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'update by',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT 'deleted flag',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_call_time`(`call_time` ASC) USING BTREE,
  INDEX `idx_call_status`(`call_status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'api call log table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_call_log_202605
-- ----------------------------
INSERT INTO `fx_api_call_log_202605` VALUES (2052288573608427522, 1993479636925403138, 2045801208530558979, 2, 'cs', 'cs', 'sys_user_pull', 'OUTBOUND', NULL, 'ffa2673f687642d1b21eb76dabf2ec1f', NULL, 'SYNC', '{\"tenantId\": \"1993479636925403138\"}', '{\"tenantId\": \"1993479636925403138\"}', '{\"body\": {\"tenantId\": \"1993479636925403138\"}, \"query\": {}, \"headers\": {}, \"targetUrl\": \"http://192.168.0.24/api/user/pull\", \"pathVariables\": {}, \"outboundTargetId\": \"2\", \"targetSystemCode\": \"cs\", \"targetSystemName\": \"cs\"}', NULL, '200', 'FAIL', 'SYSTEM_FAIL', 'I/O error on POST request for \"http://192.168.0.24/api/user/pull\": Connect to http://192.168.0.24:80 failed: Connection timed out: getsockopt', 21136, '2026-05-07 15:24:59', '2026-05-07 15:25:00', NULL, '2026-05-07 15:25:00', NULL, 0);
INSERT INTO `fx_api_call_log_202605` VALUES (2052288628256014338, 1993479636925403138, 2045801208530558978, 1, 'cs', 'cs', 'sys_user_sync', 'OUTBOUND', NULL, '5992d173fa314a949eb76b64aa93b35d', NULL, 'SYNC', '{\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/files/9030846f35724bc59aeade03f334db99.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}', '{\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/files/9030846f35724bc59aeade03f334db99.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}', '{\"body\": {\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/files/9030846f35724bc59aeade03f334db99.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}, \"query\": {}, \"headers\": {}, \"targetUrl\": \"http://192.168.0.24/api/user/sync\", \"pathVariables\": {}, \"outboundTargetId\": \"1\", \"targetSystemCode\": \"cs\", \"targetSystemName\": \"cs\"}', NULL, '200', 'FAIL', 'SYSTEM_FAIL', 'I/O error on POST request for \"http://192.168.0.24/api/user/sync\": Connect to http://192.168.0.24:80 failed: Connection timed out: getsockopt', 21067, '2026-05-07 15:25:13', '2026-05-07 15:25:13', NULL, '2026-05-07 15:25:13', NULL, 0);
INSERT INTO `fx_api_call_log_202605` VALUES (2053499941946408961, 1993479636925403138, 2045801208530558978, 1, 'cs', '本地ERP测试系统', 'sys_user_sync', 'OUTBOUND', NULL, '0fda5bf5fc814529ab2051e711ac38ef', NULL, 'SYNC', '{\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/sys/files/f67a6d20025643c6984ba7ea1f71ff28.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}', '{\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/sys/files/f67a6d20025643c6984ba7ea1f71ff28.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}', '{\"body\": {\"users\": [{\"id\": \"1993479637244170242\", \"email\": \"admin@local.com\", \"phone\": null, \"avatar\": \"http://192.168.121.1:9000/api/sys/files/f67a6d20025643c6984ba7ea1f71ff28.jpg\", \"gender\": 1, \"status\": true, \"account\": \"admin\", \"tenantId\": null, \"username\": \"admin\", \"employeeId\": null, \"positionId\": \"11\", \"userSource\": 1, \"departmentId\": \"1\"}, {\"id\": \"1993479637244170253\", \"email\": \"\", \"phone\": \"\", \"avatar\": null, \"gender\": 1, \"status\": true, \"account\": \"smy\", \"tenantId\": \"1993479636925403138\", \"username\": \"孙明岩\", \"employeeId\": null, \"positionId\": \"12\", \"userSource\": 1, \"departmentId\": \"1\"}], \"tenantId\": \"1993479636925403138\"}, \"query\": {}, \"headers\": {}, \"targetUrl\": \"/api/users/third-party/sync\", \"pathVariables\": {}, \"outboundTargetId\": \"1\", \"targetSystemCode\": \"cs\", \"targetSystemName\": \"本地ERP测试系统\"}', '\"{\\\"code\\\":200,\\\"message\\\":\\\"success\\\",\\\"data\\\":{\\\"totalCount\\\":2,\\\"createdCount\\\":2,\\\"updatedCount\\\":0,\\\"failedAccounts\\\":[]},\\\"timestamp\\\":\\\"2026-05-10T23:38:32.1970764\\\"}\"', '200', 'SUCCESS', 'SUCCESS', NULL, 349, '2026-05-10 23:38:32', '2026-05-10 23:38:33', NULL, '2026-05-10 23:38:33', NULL, 0);

-- ----------------------------
-- Table structure for fx_api_call_log_202606
-- ----------------------------
DROP TABLE IF EXISTS `fx_api_call_log_202606`;
CREATE TABLE `fx_api_call_log_202606`  (
  `id` bigint NOT NULL COMMENT 'primary key',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT 'tenant id',
  `api_config_id` bigint NOT NULL COMMENT 'api config id',
  `outbound_target_id` bigint NULL DEFAULT NULL COMMENT 'outbound target id',
  `target_system_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'target system code',
  `target_system_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'target system name',
  `api_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'api code',
  `call_direction` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'call direction',
  `caller_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'caller ip',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'trace id',
  `task_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'task id',
  `invoke_mode` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'invoke mode',
  `request_data` json NULL COMMENT 'request payload',
  `raw_request_data` json NULL COMMENT 'raw request payload',
  `assembled_request_data` json NULL COMMENT 'assembled request payload',
  `response_data` json NULL COMMENT 'response payload',
  `response_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'response code',
  `call_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'call status',
  `result_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'result type',
  `error_message` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'error message',
  `cost_time_ms` int NOT NULL DEFAULT 0 COMMENT 'cost time ms',
  `call_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'call time',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'create time',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'create by',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'update time',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'update by',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT 'deleted flag',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_api_config_id`(`api_config_id` ASC) USING BTREE,
  INDEX `idx_call_time`(`call_time` ASC) USING BTREE,
  INDEX `idx_call_status`(`call_status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'api call log table' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_call_log_202606
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
INSERT INTO `fx_api_config` VALUES (2045801208530558978, 'sys_user_sync', '用户同步第三方', '将 Forgex 用户字段按字段级映射转换为 ERP commonUsers 结构后同步。', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:38:40', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558979, 'sys_user_pull', '用户拉取第三方', '从 ERP commonUsers 结构拉取用户，并按字段级映射转换回 Forgex users 结构。', 'OUTBOUND', NULL, NULL, 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, NULL, NULL, 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:38:40', 'system', 0);
INSERT INTO `fx_api_config` VALUES (2045801208530558980, 'sys_user_third_party_inbound', '第三方推送用户', '接收第三方 commonUsers 结构并按字段级映射写入 Forgex 用户。', 'INBOUND', '/api/integration/public/invoke', 'userThirdPartyInboundInterpreter', 'HTTP', 'POST', 'SYNC', 'application/json', NULL, 30000, 0, 0, 0, 0, 'TOKEN', '{\"thirdSystemId\": 2045037285200052226}', 0, 1, 'sys', 1993479636925403138, '2026-04-22 15:19:15', 'system', '2026-04-22 15:19:15', 'system', 0);
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
INSERT INTO `fx_api_outbound_target` VALUES (1, 1993479636925403138, 2045801208530558978, 2045037285200052226, 'cs', '本地ERP测试系统', '/api/users/third-party/sync', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, 'Forgex 用户字段级映射同步到 ERP commonUsers', '2026-04-22 15:19:15', 'system', '2026-05-11 09:35:51', 'system', 0);
INSERT INTO `fx_api_outbound_target` VALUES (2, 1993479636925403138, 2045801208530558979, 2045037285200052226, 'cs', '本地ERP测试系统', '/api/users/third-party/pull', 'POST', 'application/json', 'SYNC', 30000, 0, 0, 1, 1, '从 ERP commonUsers 拉取并字段级映射回 Forgex users', '2026-04-22 15:19:15', 'system', '2026-05-11 09:35:51', 'system', 0);
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
) ENGINE = InnoDB AUTO_INCREMENT = 2045910000000000234 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口参数配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_param_config
-- ----------------------------
INSERT INTO `fx_api_param_config` VALUES (2045801783389282306, 2045801208530558977, NULL, NULL, 'REQUEST', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:48:49', 'admin', '2026-04-19 17:48:49', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801783448002561, 2045801208530558977, NULL, NULL, 'REQUEST', 'FIELD', 'userId', 'userId', 'number', 'userId', 0, '0', NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:48:49', 'admin', '2026-04-19 17:48:49', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872283361281, 2045801208530558977, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'root', 'object', 'root', 0, NULL, NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:49:10', 'admin', '2026-04-19 17:49:10', 'admin', 0);
INSERT INTO `fx_api_param_config` VALUES (2045801872316915713, 2045801208530558977, NULL, NULL, 'RESPONSE', 'FIELD', 'userId', 'userId', 'number', 'userId', 0, '0', NULL, 0, NULL, 1993479636925403138, '2026-04-19 17:49:10', 'admin', '2026-04-19 17:49:10', 'admin', 0);
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
INSERT INTO `fx_api_param_config` VALUES (2045910000000000001, 2045801208530558978, 1, NULL, 'REQUEST', 'OBJECT', 'root', 'ERP同步请求根节点', 'object', 'root', 0, NULL, NULL, 0, '第三方 ERP /api/users/third-party/sync 请求体', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000002, 2045801208530558978, 1, 2045910000000000001, 'REQUEST', 'FIELD', 'companyId', '公司ID', 'number', 'root.companyId', 1, NULL, NULL, 1, 'ERP 租户字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000003, 2045801208530558978, 1, 2045910000000000001, 'REQUEST', 'FIELD', 'batchNo', '批次号', 'string', 'root.batchNo', 0, NULL, NULL, 2, 'ERP 批次字段，可配置常量/默认值', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000004, 2045801208530558978, 1, 2045910000000000001, 'REQUEST', 'ARRAY', 'commonUsers', 'ERP用户集合', 'array', 'root.commonUsers', 1, NULL, NULL, 3, '对方接口用户集合，不叫 users', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000005, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'code', 'ERP账号编码', 'string', 'root.commonUsers.code', 1, NULL, NULL, 1, '对方账号字段，不叫 account', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000006, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'name', 'ERP姓名', 'string', 'root.commonUsers.name', 1, NULL, NULL, 2, '对方名称字段，不叫 username', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000007, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'mail', 'ERP邮箱', 'string', 'root.commonUsers.mail', 0, NULL, NULL, 3, '对方邮箱字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000008, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'mobile', 'ERP手机', 'string', 'root.commonUsers.mobile', 0, NULL, NULL, 4, '对方手机字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000009, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'enable', 'ERP启用标识', 'boolean', 'root.commonUsers.enable', 0, NULL, NULL, 5, '对方启用字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000010, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'sex', 'ERP性别', 'number', 'root.commonUsers.sex', 0, NULL, NULL, 6, '对方性别字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000011, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'orgId', 'ERP组织ID', 'number', 'root.commonUsers.orgId', 0, NULL, NULL, 7, '对方组织字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000012, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'jobId', 'ERP岗位ID', 'number', 'root.commonUsers.jobId', 0, NULL, NULL, 8, '对方岗位字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000013, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'staffId', 'ERP员工ID', 'number', 'root.commonUsers.staffId', 0, NULL, NULL, 9, '对方员工字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000014, 2045801208530558978, 1, 2045910000000000004, 'REQUEST', 'FIELD', 'sourceType', 'ERP来源类型', 'number', 'root.commonUsers.sourceType', 0, NULL, NULL, 10, '对方来源字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000021, 2045801208530558978, 1, NULL, 'RESPONSE', 'OBJECT', 'root', 'Forgex同步来源根节点', 'object', 'root', 0, NULL, NULL, 0, 'Forgex 导出的用户源数据', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000022, 2045801208530558978, 1, 2045910000000000021, 'RESPONSE', 'FIELD', 'tenantId', '租户ID', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Forgex 源字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000023, 2045801208530558978, 1, 2045910000000000021, 'RESPONSE', 'ARRAY', 'users', '用户集合', 'array', 'root.users', 1, NULL, NULL, 2, 'Forgex 用户集合', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000024, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'account', '账号', 'string', 'root.users.account', 1, NULL, NULL, 1, 'Forgex 账号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000025, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'username', '用户名', 'string', 'root.users.username', 1, NULL, NULL, 2, 'Forgex 用户名', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000026, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'email', '邮箱', 'string', 'root.users.email', 0, NULL, NULL, 3, 'Forgex 邮箱', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000027, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'phone', '手机号', 'string', 'root.users.phone', 0, NULL, NULL, 4, 'Forgex 手机号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000028, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'status', '启用状态', 'boolean', 'root.users.status', 0, NULL, NULL, 5, 'Forgex 状态', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000029, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'gender', '性别', 'number', 'root.users.gender', 0, NULL, NULL, 6, 'Forgex 性别', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000030, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'departmentId', '部门ID', 'number', 'root.users.departmentId', 0, NULL, NULL, 7, 'Forgex 部门', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000031, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'positionId', '岗位ID', 'number', 'root.users.positionId', 0, NULL, NULL, 8, 'Forgex 岗位', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000032, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'employeeId', '员工ID', 'number', 'root.users.employeeId', 0, NULL, NULL, 9, 'Forgex 员工', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000033, 2045801208530558978, 1, 2045910000000000023, 'RESPONSE', 'FIELD', 'userSource', '用户来源', 'number', 'root.users.userSource', 0, NULL, NULL, 10, 'Forgex 来源', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000101, 2045801208530558979, 2, NULL, 'REQUEST', 'OBJECT', 'root', 'Forgex拉取目标根节点', 'object', 'root', 0, NULL, NULL, 0, '映射回 Forgex 的目标结构', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000102, 2045801208530558979, 2, 2045910000000000101, 'REQUEST', 'ARRAY', 'users', '用户集合', 'array', 'root.users', 1, NULL, NULL, 1, 'Forgex 用户集合', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000103, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'account', '账号', 'string', 'root.users.account', 1, NULL, NULL, 1, 'Forgex 账号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000104, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'username', '用户名', 'string', 'root.users.username', 1, NULL, NULL, 2, 'Forgex 用户名', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000105, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'email', '邮箱', 'string', 'root.users.email', 0, NULL, NULL, 3, 'Forgex 邮箱', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000106, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'phone', '手机号', 'string', 'root.users.phone', 0, NULL, NULL, 4, 'Forgex 手机', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000107, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'status', '启用状态', 'boolean', 'root.users.status', 0, NULL, NULL, 5, 'Forgex 状态', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000108, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'gender', '性别', 'number', 'root.users.gender', 0, NULL, NULL, 6, 'Forgex 性别', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000109, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'departmentId', '部门ID', 'number', 'root.users.departmentId', 0, NULL, NULL, 7, 'Forgex 部门', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000110, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'positionId', '岗位ID', 'number', 'root.users.positionId', 0, NULL, NULL, 8, 'Forgex 岗位', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000111, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'employeeId', '员工ID', 'number', 'root.users.employeeId', 0, NULL, NULL, 9, 'Forgex 员工', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000112, 2045801208530558979, 2, 2045910000000000102, 'REQUEST', 'FIELD', 'userSource', '用户来源', 'number', 'root.users.userSource', 0, NULL, NULL, 10, 'Forgex 来源', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000121, 2045801208530558979, 2, NULL, 'RESPONSE', 'OBJECT', 'root', 'ERP拉取来源根节点', 'object', 'root', 0, NULL, NULL, 0, 'ERP 拉取请求和响应结构', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000122, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'FIELD', 'tenantId', '租户ID', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Forgex 拉取请求源字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', '2026-05-11 09:49:23', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000123, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'FIELD', 'companyId', '公司ID', 'number', 'root.companyId', 1, NULL, NULL, 3, 'ERP 拉取请求字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', '2026-05-11 09:49:23', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000124, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'FIELD', 'includeStop', '包含停用', 'boolean', 'root.includeStop', 0, NULL, NULL, 4, 'ERP 拉取请求字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', '2026-05-11 09:49:23', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000125, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'FIELD', 'total', 'ERP总数', 'number', 'root.total', 0, NULL, NULL, 5, 'ERP 响应字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', '2026-05-11 09:49:23', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000126, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'ARRAY', 'commonUsers', 'ERP用户集合', 'array', 'root.commonUsers', 1, NULL, NULL, 6, 'ERP 响应集合，不叫 users', 1993479636925403138, '2026-05-11 09:35:51', 'system', '2026-05-11 09:49:23', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000127, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'code', 'ERP账号编码', 'string', 'root.commonUsers.code', 1, NULL, NULL, 1, '对方账号字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000128, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'name', 'ERP姓名', 'string', 'root.commonUsers.name', 1, NULL, NULL, 2, '对方名称字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000129, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'mail', 'ERP邮箱', 'string', 'root.commonUsers.mail', 0, NULL, NULL, 3, '对方邮箱字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000130, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'mobile', 'ERP手机', 'string', 'root.commonUsers.mobile', 0, NULL, NULL, 4, '对方手机字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000131, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'enable', 'ERP启用标识', 'boolean', 'root.commonUsers.enable', 0, NULL, NULL, 5, '对方启用字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000132, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'sex', 'ERP性别', 'number', 'root.commonUsers.sex', 0, NULL, NULL, 6, '对方性别字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000133, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'orgId', 'ERP组织ID', 'number', 'root.commonUsers.orgId', 0, NULL, NULL, 7, '对方组织字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000134, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'jobId', 'ERP岗位ID', 'number', 'root.commonUsers.jobId', 0, NULL, NULL, 8, '对方岗位字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000135, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'staffId', 'ERP员工ID', 'number', 'root.commonUsers.staffId', 0, NULL, NULL, 9, '对方员工字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000136, 2045801208530558979, 2, 2045910000000000126, 'RESPONSE', 'FIELD', 'sourceType', 'ERP来源类型', 'number', 'root.commonUsers.sourceType', 0, NULL, NULL, 10, '对方来源字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000137, 2045801208530558979, 2, 2045910000000000121, 'RESPONSE', 'FIELD', 'includeDisabled', '包含禁用', 'boolean', 'root.includeDisabled', 0, NULL, NULL, 2, 'Forgex 拉取请求源字段', 1993479636925403138, '2026-05-11 09:49:23', 'system', '2026-05-11 09:49:45', 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000201, 2045801208530558980, NULL, NULL, 'REQUEST', 'OBJECT', 'root', '第三方推送请求根节点', 'object', 'root', 0, NULL, NULL, 0, '第三方推送 commonUsers 请求体', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000202, 2045801208530558980, NULL, 2045910000000000201, 'REQUEST', 'FIELD', 'companyId', '公司ID', 'number', 'root.companyId', 1, NULL, NULL, 1, '第三方租户字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000203, 2045801208530558980, NULL, 2045910000000000201, 'REQUEST', 'ARRAY', 'commonUsers', '第三方用户集合', 'array', 'root.commonUsers', 1, NULL, NULL, 2, '第三方用户集合，不叫 users', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000204, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'code', '第三方账号编码', 'string', 'root.commonUsers.code', 1, NULL, NULL, 1, '第三方账号字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000205, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'name', '第三方姓名', 'string', 'root.commonUsers.name', 1, NULL, NULL, 2, '第三方姓名字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000206, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'mail', '第三方邮箱', 'string', 'root.commonUsers.mail', 0, NULL, NULL, 3, '第三方邮箱字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000207, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'mobile', '第三方手机', 'string', 'root.commonUsers.mobile', 0, NULL, NULL, 4, '第三方手机字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000208, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'enable', '第三方启用标识', 'boolean', 'root.commonUsers.enable', 0, NULL, NULL, 5, '第三方启用字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000209, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'sex', '第三方性别', 'number', 'root.commonUsers.sex', 0, NULL, NULL, 6, '第三方性别字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000210, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'orgId', '第三方组织ID', 'number', 'root.commonUsers.orgId', 0, NULL, NULL, 7, '第三方组织字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000211, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'jobId', '第三方岗位ID', 'number', 'root.commonUsers.jobId', 0, NULL, NULL, 8, '第三方岗位字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000212, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'staffId', '第三方员工ID', 'number', 'root.commonUsers.staffId', 0, NULL, NULL, 9, '第三方员工字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000213, 2045801208530558980, NULL, 2045910000000000203, 'REQUEST', 'FIELD', 'sourceType', '第三方来源类型', 'number', 'root.commonUsers.sourceType', 0, NULL, NULL, 10, '第三方来源字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000221, 2045801208530558980, NULL, NULL, 'RESPONSE', 'OBJECT', 'root', 'Forgex入站目标根节点', 'object', 'root', 0, NULL, NULL, 0, '映射给入站处理器的 Forgex 结构', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000222, 2045801208530558980, NULL, 2045910000000000221, 'RESPONSE', 'FIELD', 'tenantId', '租户ID', 'number', 'root.tenantId', 1, NULL, NULL, 1, 'Forgex 入站目标字段', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000223, 2045801208530558980, NULL, 2045910000000000221, 'RESPONSE', 'ARRAY', 'users', '用户集合', 'array', 'root.users', 1, NULL, NULL, 2, 'Forgex 入站目标用户集合', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000224, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'account', '账号', 'string', 'root.users.account', 1, NULL, NULL, 1, 'Forgex 账号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000225, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'username', '用户名', 'string', 'root.users.username', 1, NULL, NULL, 2, 'Forgex 用户名', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000226, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'email', '邮箱', 'string', 'root.users.email', 0, NULL, NULL, 3, 'Forgex 邮箱', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000227, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'phone', '手机号', 'string', 'root.users.phone', 0, NULL, NULL, 4, 'Forgex 手机', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000228, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'status', '启用状态', 'boolean', 'root.users.status', 0, NULL, NULL, 5, 'Forgex 状态', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000229, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'gender', '性别', 'number', 'root.users.gender', 0, NULL, NULL, 6, 'Forgex 性别', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000230, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'departmentId', '部门ID', 'number', 'root.users.departmentId', 0, NULL, NULL, 7, 'Forgex 部门', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000231, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'positionId', '岗位ID', 'number', 'root.users.positionId', 0, NULL, NULL, 8, 'Forgex 岗位', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000232, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'employeeId', '员工ID', 'number', 'root.users.employeeId', 0, NULL, NULL, 9, 'Forgex 员工', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_config` VALUES (2045910000000000233, 2045801208530558980, NULL, 2045910000000000223, 'RESPONSE', 'FIELD', 'userSource', '用户来源', 'number', 'root.users.userSource', 0, NULL, NULL, 10, 'Forgex 来源', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);

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
) ENGINE = InnoDB AUTO_INCREMENT = 2045910000000001212 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口参数映射表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_api_param_mapping
-- ----------------------------
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348865, 2045801208530558977, NULL, 'userId', 'userId', NULL, NULL, NULL, NULL, NULL, 'INBOUND', NULL, 1993479636925403138, '2026-04-19 17:49:36', 'admin', '2026-04-19 17:49:36', 'admin', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348872, 2045801208530558981, NULL, 'suppliers', 'suppliers', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348873, 2045801208530558981, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348874, 2045801208530558982, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348875, 2045801208530558983, NULL, 'suppliers', 'suppliers', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348876, 2045801208530558983, NULL, 'tenantId', 'tenantId', NULL, NULL, NULL, NULL, 'SOURCE', 'INBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348879, 2045801208530558981, 3, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348880, 2045801208530558981, 3, 'suppliers', 'suppliers', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map supplier list', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045801982157348882, 2045801208530558982, 4, 'tenantId', 'tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', 'Map tenant id', 1993479636925403138, '2026-04-26 16:26:06', 'system', '2026-04-26 16:26:06', 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001001, 2045801208530558978, 1, 'root.tenantId', 'root.companyId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '租户ID -> 公司ID', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001002, 2045801208530558978, 1, 'root.users.account', 'root.commonUsers.code', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '账号 -> 对方 code', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001003, 2045801208530558978, 1, 'root.users.username', 'root.commonUsers.name', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '用户名 -> 对方 name', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001004, 2045801208530558978, 1, 'root.users.email', 'root.commonUsers.mail', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '邮箱 -> 对方 mail', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001005, 2045801208530558978, 1, 'root.users.phone', 'root.commonUsers.mobile', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '手机号 -> 对方 mobile', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001006, 2045801208530558978, 1, 'root.users.status', 'root.commonUsers.enable', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '状态 -> 对方 enable', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001007, 2045801208530558978, 1, 'root.users.gender', 'root.commonUsers.sex', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '性别 -> 对方 sex', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001008, 2045801208530558978, 1, 'root.users.departmentId', 'root.commonUsers.orgId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '部门 -> 对方 orgId', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001009, 2045801208530558978, 1, 'root.users.positionId', 'root.commonUsers.jobId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '岗位 -> 对方 jobId', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001010, 2045801208530558978, 1, 'root.users.employeeId', 'root.commonUsers.staffId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '员工 -> 对方 staffId', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001011, 2045801208530558978, 1, 'root.users.userSource', 'root.commonUsers.sourceType', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '来源 -> 对方 sourceType', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001012, 2045801208530558979, 2, 'root.tenantId', 'root.companyId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '租户ID -> 公司ID', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001013, 2045801208530558979, 2, 'root.includeDisabled', 'root.includeStop', NULL, NULL, NULL, 'BODY', 'SOURCE', 'OUTBOUND', '包含禁用 -> 包含停用', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001101, 2045801208530558979, 2, 'root.commonUsers.code', 'root.users.account', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 code -> 账号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001102, 2045801208530558979, 2, 'root.commonUsers.name', 'root.users.username', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 name -> 用户名', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001103, 2045801208530558979, 2, 'root.commonUsers.mail', 'root.users.email', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 mail -> 邮箱', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001104, 2045801208530558979, 2, 'root.commonUsers.mobile', 'root.users.phone', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 mobile -> 手机号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001105, 2045801208530558979, 2, 'root.commonUsers.enable', 'root.users.status', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 enable -> 状态', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001106, 2045801208530558979, 2, 'root.commonUsers.sex', 'root.users.gender', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 sex -> 性别', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001107, 2045801208530558979, 2, 'root.commonUsers.orgId', 'root.users.departmentId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 orgId -> 部门', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001108, 2045801208530558979, 2, 'root.commonUsers.jobId', 'root.users.positionId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 jobId -> 岗位', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001109, 2045801208530558979, 2, 'root.commonUsers.staffId', 'root.users.employeeId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 staffId -> 员工', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001110, 2045801208530558979, 2, 'root.commonUsers.sourceType', 'root.users.userSource', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 sourceType -> 来源', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001201, 2045801208530558980, NULL, 'root.companyId', 'root.tenantId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '公司ID -> 租户ID', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001202, 2045801208530558980, NULL, 'root.commonUsers.code', 'root.users.account', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 code -> 账号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001203, 2045801208530558980, NULL, 'root.commonUsers.name', 'root.users.username', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 name -> 用户名', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001204, 2045801208530558980, NULL, 'root.commonUsers.mail', 'root.users.email', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 mail -> 邮箱', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001205, 2045801208530558980, NULL, 'root.commonUsers.mobile', 'root.users.phone', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 mobile -> 手机号', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001206, 2045801208530558980, NULL, 'root.commonUsers.enable', 'root.users.status', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 enable -> 状态', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001207, 2045801208530558980, NULL, 'root.commonUsers.sex', 'root.users.gender', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 sex -> 性别', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001208, 2045801208530558980, NULL, 'root.commonUsers.orgId', 'root.users.departmentId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 orgId -> 部门', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001209, 2045801208530558980, NULL, 'root.commonUsers.jobId', 'root.users.positionId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 jobId -> 岗位', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001210, 2045801208530558980, NULL, 'root.commonUsers.staffId', 'root.users.employeeId', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 staffId -> 员工', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);
INSERT INTO `fx_api_param_mapping` VALUES (2045910000000001211, 2045801208530558980, NULL, 'root.commonUsers.sourceType', 'root.users.userSource', NULL, NULL, NULL, 'BODY', 'SOURCE', 'INBOUND', '对方 sourceType -> 来源', 1993479636925403138, '2026-05-11 09:35:51', 'system', NULL, 'system', 0);

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
  `token_expire_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Token有效期类型: HOURS/DAY/MONTH/YEAR/CUSTOM/FOREVER',
  `token_expire_value` int NULL DEFAULT NULL COMMENT 'Token有效期数值',
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
) ENGINE = InnoDB AUTO_INCREMENT = 2053726901834760194 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方授权表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fx_third_authorization
-- ----------------------------
INSERT INTO `fx_third_authorization` VALUES (2053726901834760193, 2045037285200052226, 'TOKEN', 'd8a9bd18f81f4f8ab1db225375a064dc', 'YEAR', 1, 8760, '2027-05-11 20:29:46', '', 1, '', 1993479636925403138, '2026-05-11 14:40:24', 'admin', '2026-05-11 20:29:46', 'admin', 0);

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
INSERT INTO `fx_third_system` VALUES (2045037285200052226, 'cs', '本地ERP测试系统', '192.168.3.7:18080', '', '', 1, 1993479636925403138, '2026-04-17 15:10:58', 'admin', '2026-05-10 22:37:36', 'admin', 0);

SET FOREIGN_KEY_CHECKS = 1;
