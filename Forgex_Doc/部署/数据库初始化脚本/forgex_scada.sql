/*
 Navicat Premium Dump SQL

 Source Server         : bendi-127
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : forgex_scada

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 03/05/2026 18:03:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for scada_alarm
-- ----------------------------
DROP TABLE IF EXISTS `scada_alarm`;
CREATE TABLE `scada_alarm`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alarm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报警编码',
  `alarm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报警类型：HIGH/LOW/DEVICE/COMMUNICATION',
  `alarm_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报警级别：INFO/WARNING/ERROR/CRITICAL',
  `device_id` bigint NULL DEFAULT NULL COMMENT '设备ID',
  `point_id` bigint NULL DEFAULT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点位编码',
  `alarm_value` decimal(20, 4) NULL DEFAULT NULL COMMENT '报警值',
  `threshold_value` decimal(20, 4) NULL DEFAULT NULL COMMENT '阈值',
  `alarm_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '报警信息',
  `alarm_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '报警时间',
  `recover_time` datetime NULL DEFAULT NULL COMMENT '恢复时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RECOVERED/ACKNOWLEDGED',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '处理人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '处理时间',
  `handle_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '处理意见',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device`(`device_id` ASC) USING BTREE,
  INDEX `idx_alarm_time`(`alarm_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '报警记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_alarm
-- ----------------------------

-- ----------------------------
-- Table structure for scada_device
-- ----------------------------
DROP TABLE IF EXISTS `scada_device`;
CREATE TABLE `scada_device`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编码',
  `device_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型',
  `device_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备分类',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '制造商',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `installation_date` date NULL DEFAULT NULL COMMENT '安装日期',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装位置',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NORMAL' COMMENT '状态：NORMAL/MAINTENANCE/Fault/SCRAPPED',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `port` int NULL DEFAULT NULL COMMENT '端口',
  `protocol` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '通信协议：MODBUS/OPC-UA/PROFINET等',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_code`(`device_code` ASC) USING BTREE,
  INDEX `idx_device_type`(`device_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_device
-- ----------------------------

-- ----------------------------
-- Table structure for scada_device_statistics
-- ----------------------------
DROP TABLE IF EXISTS `scada_device_statistics`;
CREATE TABLE `scada_device_statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `run_time` int NULL DEFAULT NULL COMMENT '运行时间(秒)',
  `idle_time` int NULL DEFAULT NULL COMMENT '空闲时间(秒)',
  `fault_time` int NULL DEFAULT NULL COMMENT '故障时间(秒)',
  `maintenance_time` int NULL DEFAULT NULL COMMENT '维护时间(秒)',
  `production_count` decimal(20, 4) NULL DEFAULT NULL COMMENT '生产数量',
  `energy_consumption` decimal(20, 4) NULL DEFAULT NULL COMMENT '能耗',
  `availability` decimal(10, 4) NULL DEFAULT NULL COMMENT '可用性',
  `performance` decimal(10, 4) NULL DEFAULT NULL COMMENT '性能',
  `quality` decimal(10, 4) NULL DEFAULT NULL COMMENT '质量',
  `oee` decimal(10, 4) NULL DEFAULT NULL COMMENT 'OEE(设备综合效率)',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_date`(`device_id` ASC, `stat_date` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备状态统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_device_statistics
-- ----------------------------

-- ----------------------------
-- Table structure for scada_history_data
-- ----------------------------
DROP TABLE IF EXISTS `scada_history_data`;
CREATE TABLE `scada_history_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点位编码',
  `value` decimal(20, 4) NULL DEFAULT NULL COMMENT '数值',
  `quality` tinyint NULL DEFAULT NULL COMMENT '质量码',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_point`(`device_id` ASC, `point_id` ASC) USING BTREE,
  INDEX `idx_collect_time`(`collect_time` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '历史数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_history_data
-- ----------------------------

-- ----------------------------
-- Table structure for scada_maintenance
-- ----------------------------
DROP TABLE IF EXISTS `scada_maintenance`;
CREATE TABLE `scada_maintenance`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `maintenance_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维护类型：PREVENTIVE/CORRECTIVE/PREDICTIVE',
  `maintenance_plan_id` bigint NULL DEFAULT NULL COMMENT '维护计划ID',
  `maintenance_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '维护内容',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration` int NULL DEFAULT NULL COMMENT '持续时间(小时)',
  `maintainer_id` bigint NULL DEFAULT NULL COMMENT '维护人ID',
  `maintainer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '维护人姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED',
  `cost` decimal(20, 2) NULL DEFAULT NULL COMMENT '维护费用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device`(`device_id` ASC) USING BTREE,
  INDEX `idx_maintenance_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备维护记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_maintenance
-- ----------------------------

-- ----------------------------
-- Table structure for scada_point
-- ----------------------------
DROP TABLE IF EXISTS `scada_point`;
CREATE TABLE `scada_point`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位编码',
  `point_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位名称',
  `point_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位类型：AI/AO/DI/DO',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据类型：INT/FLOAT/BOOL/STRING',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `sampling_rate` int NULL DEFAULT NULL COMMENT '采样频率(毫秒)',
  `alarm_enabled` tinyint NULL DEFAULT 0 COMMENT '是否启用报警',
  `alarm_high` decimal(20, 4) NULL DEFAULT NULL COMMENT '报警上限',
  `alarm_low` decimal(20, 4) NULL DEFAULT NULL COMMENT '报警下限',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_device_point`(`device_id` ASC, `point_code` ASC) USING BTREE,
  INDEX `idx_point_type`(`point_type` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '设备点位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_point
-- ----------------------------

-- ----------------------------
-- Table structure for scada_production_data
-- ----------------------------
DROP TABLE IF EXISTS `scada_production_data`;
CREATE TABLE `scada_production_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `production_batch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产批次',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '产品名称',
  `production_count` decimal(20, 4) NULL DEFAULT NULL COMMENT '生产数量',
  `qualified_count` decimal(20, 4) NULL DEFAULT NULL COMMENT '合格数量',
  `defective_count` decimal(20, 4) NULL DEFAULT NULL COMMENT '不合格数量',
  `qualified_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '合格率',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `duration` int NULL DEFAULT NULL COMMENT '生产时长(秒)',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device`(`device_id` ASC) USING BTREE,
  INDEX `idx_production_batch`(`production_batch` ASC) USING BTREE,
  INDEX `idx_start_time`(`start_time` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '生产数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_production_data
-- ----------------------------

-- ----------------------------
-- Table structure for scada_realtime_data
-- ----------------------------
DROP TABLE IF EXISTS `scada_realtime_data`;
CREATE TABLE `scada_realtime_data`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点位编码',
  `value` decimal(20, 4) NULL DEFAULT NULL COMMENT '数值',
  `quality` tinyint NULL DEFAULT NULL COMMENT '质量码：192-好 0-坏',
  `collect_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_device_point`(`device_id` ASC, `point_id` ASC) USING BTREE,
  INDEX `idx_collect_time`(`collect_time` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '实时数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of scada_realtime_data
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
