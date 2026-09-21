/*
  Forgex 数据库初始化脚本 - forgex_job
  来源       : 开发基准库全量导出（结构 + 初始数据）
  生成时间   : 2026-09-18 16:39
  字符集     : utf8mb4
  说明       : 脚本自带 CREATE DATABASE IF NOT EXISTS 与 USE，可独立导入；
               表使用 DROP TABLE IF EXISTS + CREATE，重复导入等效重建。
  导入方式   : 1) 手动: mysql -u<user> -p < forgex_job.sql
               2) 交付包: import-database.ps1 / import-database.sh 按 common→admin→history→job→workflow→scada→integration 顺序导入
*/
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: forgex_job
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
-- Current Database: `forgex_job`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `forgex_job` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `forgex_job`;

--
-- Table structure for table `sys_calendar_reminder_task`
--

DROP TABLE IF EXISTS `sys_calendar_reminder_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_calendar_reminder_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `source_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '来源类型：USER_EVENT、TENANT_EVENT',
  `source_id` bigint NOT NULL COMMENT '来源日程 ID',
  `owner_user_id` bigint DEFAULT NULL COMMENT '日程拥有者 ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '提醒标题',
  `record_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '记录类型',
  `start_time` datetime DEFAULT NULL COMMENT '日程开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '日程结束时间',
  `remind_time` datetime NOT NULL COMMENT '提醒时间',
  `notify_user_ids` json NOT NULL COMMENT '通知人 ID 集合',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'CALENDAR_REMINDER' COMMENT '消息模板编码',
  `template_data` json DEFAULT NULL COMMENT '模板变量',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0 待发送，1 发送中，2 已发送，3 失败，4 已取消',
  `send_count` int NOT NULL DEFAULT '0' COMMENT '发送次数',
  `max_retry_count` int NOT NULL DEFAULT '3' COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `sent_time` datetime DEFAULT NULL COMMENT '发送完成时间',
  `fail_reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '失败原因',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0 未删除，1 已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_calendar_reminder_due` (`tenant_id`,`status`,`remind_time`,`next_retry_time`,`deleted`) USING BTREE,
  KEY `idx_calendar_reminder_source` (`tenant_id`,`source_type`,`source_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='日历提醒任务清单';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_calendar_reminder_task`
--

LOCK TABLES `sys_calendar_reminder_task` WRITE;
/*!40000 ALTER TABLE `sys_calendar_reminder_task` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_calendar_reminder_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job`
--

DROP TABLE IF EXISTS `sys_job`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组名',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Cron执行表达式',
  `misfire_policy` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '3' COMMENT '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
  `concurrent` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '是否并发执行（0允许 1禁止）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '0' COMMENT '状态（0正常 1暂停）',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建者',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新者',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注信息',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_job_group` (`job_group`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务调度表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_alarm_log`
--

DROP TABLE IF EXISTS `sys_job_alarm_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_alarm_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `rule_id` bigint DEFAULT NULL COMMENT '规则ID',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `job_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务编码',
  `log_id` bigint DEFAULT NULL COMMENT '日志ID',
  `alarm_type` tinyint DEFAULT NULL COMMENT '告警类型',
  `send_status` tinyint NOT NULL DEFAULT '0' COMMENT '发送状态',
  `notify_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '通知方式',
  `notify_target` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '通知目标',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '告警内容',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_alarm_log_job` (`tenant_id`,`job_code`,`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job告警日志';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_alarm_log`
--

LOCK TABLES `sys_job_alarm_log` WRITE;
/*!40000 ALTER TABLE `sys_job_alarm_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_alarm_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_alarm_rule`
--

DROP TABLE IF EXISTS `sys_job_alarm_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_alarm_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `rule_name` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '规则名称',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `job_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务编码',
  `alarm_type` tinyint NOT NULL DEFAULT '1' COMMENT '告警类型',
  `threshold_count` int NOT NULL DEFAULT '1' COMMENT '阈值次数',
  `window_minutes` int NOT NULL DEFAULT '5' COMMENT '统计窗口分钟',
  `notify_type` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '通知方式',
  `notify_target` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '通知目标',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_alarm_rule_job` (`tenant_id`,`job_code`,`status`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job告警规则';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_alarm_rule`
--

LOCK TABLES `sys_job_alarm_rule` WRITE;
/*!40000 ALTER TABLE `sys_job_alarm_rule` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_alarm_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_instance`
--

DROP TABLE IF EXISTS `sys_job_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `instance_id` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '实例ID',
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '服务名',
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'IP地址',
  `port` int DEFAULT NULL COMMENT '端口',
  `pid` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '进程ID',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态',
  `running_count` int NOT NULL DEFAULT '0' COMMENT '运行中数量',
  `last_heartbeat_time` datetime DEFAULT NULL COMMENT '最后心跳时间',
  `start_time` datetime DEFAULT NULL COMMENT '启动时间',
  `maintenance` tinyint NOT NULL DEFAULT '0' COMMENT '维护模式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sys_job_instance` (`tenant_id`,`instance_id`,`deleted`) USING BTREE,
  KEY `idx_sys_job_instance_heartbeat` (`tenant_id`,`last_heartbeat_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=54 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job执行器实例';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_instance`
--

LOCK TABLES `sys_job_instance` WRITE;
/*!40000 ALTER TABLE `sys_job_instance` DISABLE KEYS */;
INSERT INTO `sys_job_instance` VALUES (1,-1,'192.168.3.7:9004:18292','forgex-job','192.168.3.7',9004,'18292',1,0,'2026-05-13 13:02:31','2026-05-13 11:52:02',0,'2026-05-13 11:52:02',NULL,'2026-05-13 11:52:02',NULL,0),(2,-1,'192.168.3.7:9004:29156','forgex-job','192.168.3.7',9004,'29156',1,0,'2026-05-13 14:16:36','2026-05-13 13:03:23',0,'2026-05-13 13:03:23',NULL,'2026-05-13 13:03:23',NULL,0),(3,-1,'192.168.3.7:9004:1012','forgex-job','192.168.3.7',9004,'1012',1,0,'2026-05-13 15:24:39','2026-05-13 14:17:18',0,'2026-05-13 14:17:18',NULL,'2026-05-13 14:17:18',NULL,0),(4,-1,'198.18.0.1:9004:33464','forgex-job','198.18.0.1',9004,'33464',1,0,'2026-05-15 18:38:00','2026-05-15 16:30:01',0,'2026-05-15 16:30:01',NULL,'2026-05-15 16:30:01',NULL,0),(5,-1,'198.18.0.1:9004:10144','forgex-job','198.18.0.1',9004,'10144',1,0,'2026-05-15 23:05:39','2026-05-15 18:39:57',0,'2026-05-15 18:39:57',NULL,'2026-05-15 18:39:57',NULL,0),(6,-1,'192.168.3.7:9004:31144','forgex-job','192.168.3.7',9004,'31144',1,0,'2026-05-16 21:34:24','2026-05-16 16:03:15',0,'2026-05-16 16:03:15',NULL,'2026-05-16 16:03:15',NULL,0),(7,-1,'198.18.0.1:9004:34404','forgex-job','198.18.0.1',9004,'34404',1,0,'2026-05-16 22:57:03','2026-05-16 21:41:35',0,'2026-05-16 21:41:35',NULL,'2026-05-16 21:41:35',NULL,0),(8,-1,'198.18.0.1:9004:35248','forgex-job','198.18.0.1',9004,'35248',1,0,'2026-05-17 18:34:28','2026-05-17 10:05:37',0,'2026-05-17 10:05:37',NULL,'2026-05-17 10:05:37',NULL,0),(9,-1,'192.168.3.7:9004:31784','forgex-job','192.168.3.7',9004,'31784',1,0,'2026-05-18 20:09:09','2026-05-18 19:44:17',0,'2026-05-18 19:44:17',NULL,'2026-05-18 19:44:17',NULL,0),(10,-1,'192.168.3.7:9004:41248','forgex-job','192.168.3.7',9004,'41248',1,0,'2026-05-18 22:57:50','2026-05-18 20:10:08',0,'2026-05-18 20:10:08',NULL,'2026-05-18 20:10:08',NULL,0),(11,-1,'192.168.3.7:9004:44708','forgex-job','192.168.3.7',9004,'44708',1,0,'2026-05-18 23:08:42','2026-05-18 22:59:27',0,'2026-05-18 22:59:27',NULL,'2026-05-18 22:59:27',NULL,0),(12,-1,'192.168.3.7:9004:43384','forgex-job','192.168.3.7',9004,'43384',1,0,'2026-05-18 23:37:30','2026-05-18 23:14:55',0,'2026-05-18 23:14:55',NULL,'2026-05-18 23:14:55',NULL,0),(13,-1,'192.168.3.7:9004:66396','forgex-job','192.168.3.7',9004,'66396',1,0,'2026-05-23 18:25:58','2026-05-23 14:06:30',0,'2026-05-23 14:06:30',NULL,'2026-05-23 14:06:30',NULL,0),(14,-1,'198.18.0.1:9004:42004','forgex-job','198.18.0.1',9004,'42004',1,0,'2026-06-07 19:58:25','2026-06-07 19:21:24',0,'2026-06-07 19:21:24',NULL,'2026-06-07 19:21:24',NULL,0),(15,-1,'198.18.0.1:9004:47364','forgex-job','198.18.0.1',9004,'47364',1,0,'2026-06-07 20:33:48','2026-06-07 19:59:30',0,'2026-06-07 19:59:30',NULL,'2026-06-07 19:59:30',NULL,0),(16,-1,'198.18.0.1:9004:35780','forgex-job','198.18.0.1',9004,'35780',1,0,'2026-06-07 21:27:11','2026-06-07 20:35:00',0,'2026-06-07 20:35:00',NULL,'2026-06-07 20:35:00',NULL,0),(17,-1,'198.18.0.1:9004:54836','forgex-job','198.18.0.1',9004,'54836',1,0,'2026-06-07 23:43:08','2026-06-07 21:28:39',0,'2026-06-07 21:28:39',NULL,'2026-06-07 21:28:39',NULL,0),(18,-1,'198.18.0.1:9004:38228','forgex-job','198.18.0.1',9004,'38228',1,0,'2026-06-19 14:13:21','2026-06-19 10:27:46',0,'2026-06-19 10:27:46',NULL,'2026-06-19 10:27:46',NULL,0),(19,-1,'198.18.0.1:9004:42380','forgex-job','198.18.0.1',9004,'42380',1,0,'2026-06-19 18:47:04','2026-06-19 14:14:39',0,'2026-06-19 14:14:39',NULL,'2026-06-19 14:14:39',NULL,0),(20,-1,'198.18.0.1:9004:34084','forgex-job','198.18.0.1',9004,'34084',1,0,'2026-06-19 20:23:34','2026-06-19 18:49:50',0,'2026-06-19 18:49:50',NULL,'2026-06-19 18:49:50',NULL,0),(21,-1,'198.18.0.1:9004:39520','forgex-job','198.18.0.1',9004,'39520',1,0,'2026-06-19 21:46:50','2026-06-19 20:25:15',0,'2026-06-19 20:25:15',NULL,'2026-06-19 20:25:15',NULL,0),(22,-1,'192.168.1.8:9004:16428','forgex-job','192.168.1.8',9004,'16428',1,0,'2026-06-20 23:18:43','2026-06-20 23:09:02',0,'2026-06-20 23:09:02',NULL,'2026-06-20 23:09:02',NULL,0),(23,-1,'192.168.1.8:9004:47372','forgex-job','192.168.1.8',9004,'47372',1,0,'2026-06-20 23:38:32','2026-06-20 23:21:21',0,'2026-06-20 23:21:21',NULL,'2026-06-20 23:21:21',NULL,0),(24,-1,'192.168.1.7:9004:64400','forgex-job','192.168.1.7',9004,'64400',1,0,'2026-06-20 23:42:26','2026-06-20 23:39:30',0,'2026-06-20 23:39:30',NULL,'2026-06-20 23:39:30',NULL,0),(25,-1,'172.31.144.1:9004:3020','forgex-job','172.31.144.1',9004,'3020',1,0,'2026-06-21 15:28:51','2026-06-21 12:58:02',0,'2026-06-21 12:58:02',NULL,'2026-06-21 12:58:02',NULL,0),(26,-1,'192.168.3.7:9004:40780','forgex-job','192.168.3.7',9004,'40780',1,0,'2026-06-21 22:55:35','2026-06-21 19:53:31',0,'2026-06-21 19:53:31',NULL,'2026-06-21 19:53:31',NULL,0),(27,-1,'192.168.3.7:9004:36812','forgex-job','192.168.3.7',9004,'36812',1,0,'2026-06-21 23:13:30','2026-06-21 22:56:29',0,'2026-06-21 22:56:29',NULL,'2026-06-21 22:56:29',NULL,0),(28,-1,'198.18.0.1:9004:19180','forgex-job','198.18.0.1',9004,'19180',1,0,'2026-06-24 16:09:39','2026-06-24 16:07:13',0,'2026-06-24 16:07:13',NULL,'2026-06-24 16:07:13',NULL,0),(29,-1,'198.18.0.1:9004:53440','forgex-job','198.18.0.1',9004,'53440',1,0,'2026-06-26 21:53:12','2026-06-26 19:28:46',0,'2026-06-26 19:28:46',NULL,'2026-06-26 19:28:46',NULL,0),(30,-1,'198.18.0.1:9004:40964','forgex-job','198.18.0.1',9004,'40964',1,0,'2026-07-13 10:34:44','2026-07-13 10:18:46',0,'2026-07-13 10:18:46',NULL,'2026-07-13 10:18:46',NULL,0),(31,-1,'198.18.0.1:9004:24072','forgex-job','198.18.0.1',9004,'24072',1,0,'2026-07-13 13:01:49','2026-07-13 10:38:49',0,'2026-07-13 10:38:49',NULL,'2026-07-13 10:38:49',NULL,0),(32,-1,'198.18.0.1:9004:47628','forgex-job','198.18.0.1',9004,'47628',1,0,'2026-07-13 14:54:38','2026-07-13 13:03:30',0,'2026-07-13 13:03:31',NULL,'2026-07-13 13:03:31',NULL,0),(33,-1,'198.18.0.1:9004:56668','forgex-job','198.18.0.1',9004,'56668',1,0,'2026-07-13 17:49:30','2026-07-13 14:55:40',0,'2026-07-13 14:55:40',NULL,'2026-07-13 14:55:40',NULL,0),(34,-1,'192.168.0.191:9004:51428','forgex-job','192.168.0.191',9004,'51428',1,0,'2026-07-14 11:17:18','2026-07-14 09:24:14',0,'2026-07-14 09:24:14',NULL,'2026-07-14 09:24:14',NULL,0),(35,-1,'198.18.0.1:9004:39336','forgex-job','198.18.0.1',9004,'39336',1,0,'2026-08-12 22:13:18','2026-08-12 19:46:20',0,'2026-08-12 19:46:20',NULL,'2026-08-12 19:46:20',NULL,0),(36,-1,'198.18.0.1:9004:40508','forgex-job','198.18.0.1',9004,'40508',1,0,'2026-08-12 23:04:28','2026-08-12 22:14:54',0,'2026-08-12 22:14:54',NULL,'2026-08-12 22:14:54',NULL,0),(37,-1,'198.18.0.1:9004:10996','forgex-job','198.18.0.1',9004,'10996',1,0,'2026-08-19 22:31:54','2026-08-19 21:52:36',0,'2026-08-19 21:52:36',NULL,'2026-08-19 21:52:36',NULL,0),(38,-1,'192.168.3.7:9004:37084','forgex-job','192.168.3.7',9004,'37084',1,0,'2026-08-22 21:49:33','2026-08-22 14:36:51',0,'2026-08-22 14:36:51',NULL,'2026-08-22 14:36:51',NULL,0),(39,-1,'198.18.0.1:9004:17428','forgex-job','198.18.0.1',9004,'17428',1,0,'2026-08-23 11:43:41','2026-08-23 10:43:59',0,'2026-08-23 10:43:59',NULL,'2026-08-23 10:43:59',NULL,0),(40,-1,'198.18.0.1:9004:41292','forgex-job','198.18.0.1',9004,'41292',1,0,'2026-08-23 18:10:10','2026-08-23 11:45:00',0,'2026-08-23 11:45:00',NULL,'2026-08-23 11:45:00',NULL,0),(41,-1,'192.168.3.7:9004:36672','forgex-job','192.168.3.7',9004,'36672',1,0,'2026-09-11 09:32:00','2026-09-11 09:02:53',0,'2026-09-11 09:02:53',NULL,'2026-09-11 09:02:53',NULL,0),(42,-1,'192.168.3.7:9004:38976','forgex-job','192.168.3.7',9004,'38976',1,0,'2026-09-11 09:41:47','2026-09-11 09:32:48',0,'2026-09-11 09:32:48',NULL,'2026-09-11 09:32:48',NULL,0),(43,-1,'192.168.3.7:9004:16968','forgex-job','192.168.3.7',9004,'16968',1,0,'2026-09-11 09:58:09','2026-09-11 09:42:59',0,'2026-09-11 09:42:59',NULL,'2026-09-11 09:42:59',NULL,0),(44,-1,'192.168.3.7:9004:10100','forgex-job','192.168.3.7',9004,'10100',1,0,'2026-09-11 13:03:07','2026-09-11 09:59:27',0,'2026-09-11 09:59:27',NULL,'2026-09-11 09:59:27',NULL,0),(45,-1,'192.168.3.7:9004:30472','forgex-job','192.168.3.7',9004,'30472',1,0,'2026-09-11 14:17:21','2026-09-11 13:04:41',0,'2026-09-11 13:04:41',NULL,'2026-09-11 13:04:41',NULL,0),(46,-1,'192.168.3.7:9004:38540','forgex-job','192.168.3.7',9004,'38540',1,0,'2026-09-11 22:26:16','2026-09-11 14:18:24',0,'2026-09-11 14:18:24',NULL,'2026-09-11 14:18:24',NULL,0),(47,-1,'192.168.3.7:9004:52116','forgex-job','192.168.3.7',9004,'52116',1,0,'2026-09-17 23:44:11','2026-09-17 20:56:19',0,'2026-09-17 20:56:19',NULL,'2026-09-17 20:56:19',NULL,0),(48,0,'192.168.3.7:9004:44156','forgex-job','192.168.3.7',9004,'44156',1,0,'2026-09-18 10:16:01','2026-09-18 10:16:01',0,'2026-09-18 10:16:01',NULL,'2026-09-18 10:16:01',NULL,0),(50,0,'192.168.3.7:9004:15868','forgex-job','192.168.3.7',9004,'15868',1,0,'2026-09-18 10:34:08','2026-09-18 10:34:08',0,'2026-09-18 10:34:08',NULL,'2026-09-18 10:34:08',NULL,0),(52,0,'192.168.3.7:9004:43432','forgex-job','192.168.3.7',9004,'43432',1,0,'2026-09-18 10:41:49','2026-09-18 10:39:49',0,'2026-09-18 10:39:49',NULL,'2026-09-18 10:39:49',NULL,0),(53,0,'192.168.3.7:9004:37772','forgex-job','192.168.3.7',9004,'37772',1,0,'2026-09-18 16:39:25','2026-09-18 11:00:17',0,'2026-09-18 11:00:17',NULL,'2026-09-18 11:00:17',NULL,0);
/*!40000 ALTER TABLE `sys_job_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_log`
--

DROP TABLE IF EXISTS `sys_job_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务日志ID',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `trigger_type` tinyint NOT NULL DEFAULT '1' COMMENT '????',
  `fire_time` datetime DEFAULT NULL COMMENT '??????',
  `job_group` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务组名',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标字符串',
  `job_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '日志信息',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0 待执行，1 执行中，2 成功，3 失败，4 超时，5 取消',
  `instance_id` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??ID',
  `request_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `result_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `error_stack` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '????',
  `retry_of_log_id` bigint DEFAULT NULL COMMENT '????ID',
  `shard_index` int DEFAULT '0' COMMENT '????',
  `shard_total` int DEFAULT '1' COMMENT '????',
  `request_id` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??ID',
  `exception_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '异常信息',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '????',
  `duration_ms` bigint DEFAULT NULL COMMENT '????',
  `stop_time` datetime DEFAULT NULL COMMENT '结束时间',
  `cost_time` bigint DEFAULT NULL COMMENT '执行时长(毫秒)',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `job_id` bigint NOT NULL DEFAULT '0' COMMENT '??ID',
  `job_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_job_name` (`job_name`) USING BTREE,
  KEY `idx_job_group` (`job_group`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_start_time` (`start_time`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE,
  KEY `idx_sys_job_log_job_time` (`tenant_id`,`job_id`,`fire_time`) USING BTREE,
  KEY `idx_sys_job_log_status` (`tenant_id`,`status`,`create_time`) USING BTREE,
  KEY `idx_sys_job_log_request` (`tenant_id`,`request_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务执行日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_log`
--

LOCK TABLES `sys_job_log` WRITE;
/*!40000 ALTER TABLE `sys_job_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_retry`
--

DROP TABLE IF EXISTS `sys_job_retry`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_retry` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `job_id` bigint NOT NULL COMMENT '任务ID',
  `log_id` bigint NOT NULL COMMENT '日志ID',
  `job_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码',
  `biz_type` varchar(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务类型',
  `biz_id` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务ID',
  `retry_count` int NOT NULL DEFAULT '0' COMMENT '已重试次数',
  `max_retry_count` int NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `next_retry_time` datetime DEFAULT NULL COMMENT '下次重试时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1 待重试，2 死信，3 已处理',
  `last_error` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '最后错误',
  `handle_remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_retry_next` (`tenant_id`,`status`,`next_retry_time`) USING BTREE,
  KEY `idx_sys_job_retry_log` (`tenant_id`,`log_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job重试与死信';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_retry`
--

LOCK TABLES `sys_job_retry` WRITE;
/*!40000 ALTER TABLE `sys_job_retry` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_retry` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_scheduler`
--

DROP TABLE IF EXISTS `sys_job_scheduler`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_scheduler` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scheduler_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器名称',
  `scheduler_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调度器类型：QUARTZ/XXL-JOB',
  `config_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '配置信息JSON',
  `status` tinyint DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='任务调度器配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_scheduler`
--

LOCK TABLES `sys_job_scheduler` WRITE;
/*!40000 ALTER TABLE `sys_job_scheduler` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_scheduler` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_task`
--

DROP TABLE IF EXISTS `sys_job_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `job_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务编码',
  `job_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `job_group` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '任务分组',
  `job_type` tinyint NOT NULL DEFAULT '1' COMMENT '任务类型：1 JavaBean，2 HTTP，3 Script，4 RocketMQ，5 DAG',
  `schedule_type` tinyint NOT NULL DEFAULT '0' COMMENT '调度类型：0 手动，1 Cron，2 固定间隔',
  `cron_expression` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Cron 表达式',
  `interval_seconds` int DEFAULT NULL COMMENT '固定间隔秒数',
  `bean_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Spring Bean 名称',
  `method_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '方法名',
  `http_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'HTTP 地址',
  `http_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'HTTP 方法',
  `http_headers` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT 'HTTP 请求头 JSON',
  `script_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '脚本类型',
  `script_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '脚本路径',
  `script_args` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '脚本参数',
  `mq_topic` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'RocketMQ Topic',
  `mq_tags` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'RocketMQ Tags',
  `workflow_id` bigint DEFAULT NULL COMMENT 'DAG 编排ID',
  `job_params` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '任务参数 JSON',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态：0 停用，1 启用',
  `block_strategy` tinyint NOT NULL DEFAULT '1' COMMENT '阻塞策略',
  `timeout_seconds` int NOT NULL DEFAULT '60' COMMENT '超时时间秒',
  `max_retry_count` int NOT NULL DEFAULT '0' COMMENT '最大重试次数',
  `retry_interval_seconds` int DEFAULT '60' COMMENT '重试间隔秒',
  `shard_total` int NOT NULL DEFAULT '1' COMMENT '分片总数',
  `broadcast_enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否广播',
  `next_trigger_time` datetime DEFAULT NULL COMMENT '下次触发时间',
  `last_trigger_time` datetime DEFAULT NULL COMMENT '上次触发时间',
  `last_status` tinyint DEFAULT NULL COMMENT '上次执行状态',
  `trigger_count` bigint NOT NULL DEFAULT '0' COMMENT '触发次数',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sys_job_task_code` (`tenant_id`,`job_code`,`deleted`) USING BTREE,
  KEY `idx_sys_job_task_next` (`status`,`next_trigger_time`) USING BTREE,
  KEY `idx_sys_job_task_group` (`tenant_id`,`job_group`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job任务定义';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_task`
--

LOCK TABLES `sys_job_task` WRITE;
/*!40000 ALTER TABLE `sys_job_task` DISABLE KEYS */;
INSERT INTO `sys_job_task` VALUES (1,1,'job_smoke_test','Job Smoke Test','system-test',1,3,NULL,NULL,'jobSmokeTestHandler','execute',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,'{\"mode\":\"success\",\"message\":\"manual smoke test\"}',0,1,10,1,10,1,0,NULL,NULL,NULL,0,'Smoke test task for JavaBean success, failure, exception and timeout flow.','2026-05-13 20:56:35','codex','2026-05-16 20:24:22','codex',0);
/*!40000 ALTER TABLE `sys_job_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_workflow`
--

DROP TABLE IF EXISTS `sys_job_workflow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_workflow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `workflow_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编排编码',
  `workflow_name` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编排名称',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `graph_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '图定义JSON',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sys_job_workflow_code` (`tenant_id`,`workflow_code`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job DAG编排';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_workflow`
--

LOCK TABLES `sys_job_workflow` WRITE;
/*!40000 ALTER TABLE `sys_job_workflow` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_workflow` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_workflow_edge`
--

DROP TABLE IF EXISTS `sys_job_workflow_edge`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_workflow_edge` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `edge_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '连线编码',
  `source_node_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '源节点编码',
  `target_node_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标节点编码',
  `condition_expression` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '条件表达式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_workflow_edge` (`tenant_id`,`workflow_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job DAG连线';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_workflow_edge`
--

LOCK TABLES `sys_job_workflow_edge` WRITE;
/*!40000 ALTER TABLE `sys_job_workflow_edge` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_workflow_edge` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_workflow_execution`
--

DROP TABLE IF EXISTS `sys_job_workflow_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_workflow_execution` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `workflow_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '编排编码',
  `root_log_id` bigint DEFAULT NULL COMMENT '根日志ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `node_status_json` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '节点状态JSON',
  `result_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '执行结果',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_workflow_execution` (`tenant_id`,`workflow_id`,`create_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job DAG执行记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_workflow_execution`
--

LOCK TABLES `sys_job_workflow_execution` WRITE;
/*!40000 ALTER TABLE `sys_job_workflow_execution` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_workflow_execution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_job_workflow_node`
--

DROP TABLE IF EXISTS `sys_job_workflow_node`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_job_workflow_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `workflow_id` bigint NOT NULL COMMENT '编排ID',
  `node_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点编码',
  `node_name` varchar(160) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '节点名称',
  `job_id` bigint DEFAULT NULL COMMENT '任务ID',
  `node_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '节点类型',
  `node_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '节点配置',
  `position_x` int DEFAULT '0' COMMENT 'X坐标',
  `position_y` int DEFAULT '0' COMMENT 'Y坐标',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_job_workflow_node` (`tenant_id`,`workflow_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='Job DAG节点';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job_workflow_node`
--

LOCK TABLES `sys_job_workflow_node` WRITE;
/*!40000 ALTER TABLE `sys_job_workflow_node` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job_workflow_node` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-18 16:39:29
