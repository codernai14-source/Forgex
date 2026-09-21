/*
  Forgex 数据库初始化脚本 - forgex_scada
  来源       : 开发基准库全量导出（结构 + 初始数据）
  生成时间   : 2026-09-18 16:39
  字符集     : utf8mb4
  说明       : 脚本自带 CREATE DATABASE IF NOT EXISTS 与 USE，可独立导入；
               表使用 DROP TABLE IF EXISTS + CREATE，重复导入等效重建。
  导入方式   : 1) 手动: mysql -u<user> -p < forgex_scada.sql
               2) 交付包: import-database.ps1 / import-database.sh 按 common→admin→history→job→workflow→scada→integration 顺序导入
*/
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: forgex_scada
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `forgex_scada`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `forgex_scada` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `forgex_scada`;

--
-- Table structure for table `scada_alarm`
--

DROP TABLE IF EXISTS `scada_alarm`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_alarm` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `alarm_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报警编码',
  `alarm_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '报警类型：HIGH/LOW/DEVICE/COMMUNICATION',
  `alarm_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '报警级别：INFO/WARNING/ERROR/CRITICAL',
  `device_id` bigint DEFAULT NULL COMMENT '设备ID',
  `point_id` bigint DEFAULT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '点位编码',
  `alarm_value` decimal(20,4) DEFAULT NULL COMMENT '报警值',
  `threshold_value` decimal(20,4) DEFAULT NULL COMMENT '阈值',
  `alarm_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '报警信息',
  `alarm_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '报警时间',
  `recover_time` datetime DEFAULT NULL COMMENT '恢复时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RECOVERED/ACKNOWLEDGED',
  `operator_id` bigint DEFAULT NULL COMMENT '处理人ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理意见',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device` (`device_id`) USING BTREE,
  KEY `idx_alarm_time` (`alarm_time`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='报警记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_alarm`
--

LOCK TABLES `scada_alarm` WRITE;
/*!40000 ALTER TABLE `scada_alarm` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_alarm` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_device`
--

DROP TABLE IF EXISTS `scada_device`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_device` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备编码',
  `device_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备名称',
  `device_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '设备类型',
  `device_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '设备分类',
  `manufacturer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '制造商',
  `model` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '型号',
  `serial_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '序列号',
  `installation_date` date DEFAULT NULL COMMENT '安装日期',
  `location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '安装位置',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'NORMAL' COMMENT '状态：NORMAL/MAINTENANCE/Fault/SCRAPPED',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP地址',
  `port` int DEFAULT NULL COMMENT '端口',
  `protocol` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '通信协议：MODBUS/OPC-UA/PROFINET等',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_code` (`device_code`) USING BTREE,
  KEY `idx_device_type` (`device_type`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_device`
--

LOCK TABLES `scada_device` WRITE;
/*!40000 ALTER TABLE `scada_device` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_device` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_device_statistics`
--

DROP TABLE IF EXISTS `scada_device_statistics`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_device_statistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `stat_date` date NOT NULL COMMENT '统计日期',
  `run_time` int DEFAULT NULL COMMENT '运行时间(秒)',
  `idle_time` int DEFAULT NULL COMMENT '空闲时间(秒)',
  `fault_time` int DEFAULT NULL COMMENT '故障时间(秒)',
  `maintenance_time` int DEFAULT NULL COMMENT '维护时间(秒)',
  `production_count` decimal(20,4) DEFAULT NULL COMMENT '生产数量',
  `energy_consumption` decimal(20,4) DEFAULT NULL COMMENT '能耗',
  `availability` decimal(10,4) DEFAULT NULL COMMENT '可用性',
  `performance` decimal(10,4) DEFAULT NULL COMMENT '性能',
  `quality` decimal(10,4) DEFAULT NULL COMMENT '质量',
  `oee` decimal(10,4) DEFAULT NULL COMMENT 'OEE(设备综合效率)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_date` (`device_id`,`stat_date`) USING BTREE,
  KEY `idx_stat_date` (`stat_date`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备状态统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_device_statistics`
--

LOCK TABLES `scada_device_statistics` WRITE;
/*!40000 ALTER TABLE `scada_device_statistics` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_device_statistics` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_history_data`
--

DROP TABLE IF EXISTS `scada_history_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_history_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '点位编码',
  `value` decimal(20,4) DEFAULT NULL COMMENT '数值',
  `quality` tinyint DEFAULT NULL COMMENT '质量码',
  `collect_time` datetime NOT NULL COMMENT '采集时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_point` (`device_id`,`point_id`) USING BTREE,
  KEY `idx_collect_time` (`collect_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='历史数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_history_data`
--

LOCK TABLES `scada_history_data` WRITE;
/*!40000 ALTER TABLE `scada_history_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_history_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_maintenance`
--

DROP TABLE IF EXISTS `scada_maintenance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_maintenance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `maintenance_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '维护类型：PREVENTIVE/CORRECTIVE/PREDICTIVE',
  `maintenance_plan_id` bigint DEFAULT NULL COMMENT '维护计划ID',
  `maintenance_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '维护内容',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int DEFAULT NULL COMMENT '持续时间(小时)',
  `maintainer_id` bigint DEFAULT NULL COMMENT '维护人ID',
  `maintainer_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '维护人姓名',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PENDING' COMMENT '状态：PENDING/IN_PROGRESS/COMPLETED',
  `cost` decimal(20,2) DEFAULT NULL COMMENT '维护费用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device` (`device_id`) USING BTREE,
  KEY `idx_maintenance_time` (`start_time`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备维护记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_maintenance`
--

LOCK TABLES `scada_maintenance` WRITE;
/*!40000 ALTER TABLE `scada_maintenance` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_maintenance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_point`
--

DROP TABLE IF EXISTS `scada_point`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_point` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位编码',
  `point_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位名称',
  `point_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '点位类型：AI/AO/DI/DO',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '数据类型：INT/FLOAT/BOOL/STRING',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '单位',
  `address` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '地址',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `sampling_rate` int DEFAULT NULL COMMENT '采样频率(毫秒)',
  `alarm_enabled` tinyint DEFAULT '0' COMMENT '是否启用报警',
  `alarm_high` decimal(20,4) DEFAULT NULL COMMENT '报警上限',
  `alarm_low` decimal(20,4) DEFAULT NULL COMMENT '报警下限',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_device_point` (`device_id`,`point_code`) USING BTREE,
  KEY `idx_point_type` (`point_type`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='设备点位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_point`
--

LOCK TABLES `scada_point` WRITE;
/*!40000 ALTER TABLE `scada_point` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_point` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_production_data`
--

DROP TABLE IF EXISTS `scada_production_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_production_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `production_batch` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生产批次',
  `product_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品编码',
  `product_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '产品名称',
  `production_count` decimal(20,4) DEFAULT NULL COMMENT '生产数量',
  `qualified_count` decimal(20,4) DEFAULT NULL COMMENT '合格数量',
  `defective_count` decimal(20,4) DEFAULT NULL COMMENT '不合格数量',
  `qualified_rate` decimal(10,4) DEFAULT NULL COMMENT '合格率',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int DEFAULT NULL COMMENT '生产时长(秒)',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device` (`device_id`) USING BTREE,
  KEY `idx_production_batch` (`production_batch`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='生产数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_production_data`
--

LOCK TABLES `scada_production_data` WRITE;
/*!40000 ALTER TABLE `scada_production_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_production_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `scada_realtime_data`
--

DROP TABLE IF EXISTS `scada_realtime_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scada_realtime_data` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` bigint NOT NULL COMMENT '设备ID',
  `point_id` bigint NOT NULL COMMENT '点位ID',
  `point_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '点位编码',
  `value` decimal(20,4) DEFAULT NULL COMMENT '数值',
  `quality` tinyint DEFAULT NULL COMMENT '质量码：192-好 0-坏',
  `collect_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '采集时间',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_device_point` (`device_id`,`point_id`) USING BTREE,
  KEY `idx_collect_time` (`collect_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='实时数据表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `scada_realtime_data`
--

LOCK TABLES `scada_realtime_data` WRITE;
/*!40000 ALTER TABLE `scada_realtime_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `scada_realtime_data` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-18 16:39:30
