/*
  Forgex 数据库初始化脚本 - forgex_workflow
  来源       : 开发基准库全量导出（结构 + 初始数据）
  生成时间   : 2026-09-18 16:39
  字符集     : utf8mb4
  说明       : 脚本自带 CREATE DATABASE IF NOT EXISTS 与 USE，可独立导入；
               表使用 DROP TABLE IF EXISTS + CREATE，重复导入等效重建。
  导入方式   : 1) 手动: mysql -u<user> -p < forgex_workflow.sql
               2) 交付包: import-database.ps1 / import-database.sh 按 common→admin→history→job→workflow→scada→integration 顺序导入
*/
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: forgex_workflow
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
-- Current Database: `forgex_workflow`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `forgex_workflow` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `forgex_workflow`;

--
-- Table structure for table `sys_message_template`
--

DROP TABLE IF EXISTS `sys_message_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `template_version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1.0.0',
  `message_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'NOTICE',
  `status` tinyint DEFAULT '1',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_sys_message_template` (`tenant_id`,`template_code`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template`
--

LOCK TABLES `sys_message_template` WRITE;
/*!40000 ALTER TABLE `sys_message_template` DISABLE KEYS */;
INSERT INTO `sys_message_template` VALUES (1,'WF_APPROVAL_START','??????','{\"zh-CN\":\"??????\",\"en-US\":\"Approval Start\"}','1.0.0','NOTICE',1,'???????????????',0,'2026-04-02 14:50:27','system','2026-04-02 14:50:27','system',0),(2,'WF_APPROVAL_PASS','??????','{\"zh-CN\":\"??????\",\"en-US\":\"Approval Pass\"}','1.0.0','NOTICE',1,'?????????????',0,'2026-04-02 14:50:27','system','2026-04-02 14:50:27','system',0),(3,'WF_APPROVAL_REJECT','??????','{\"zh-CN\":\"??????\",\"en-US\":\"Approval Reject\"}','1.0.0','WARNING',1,'?????????????',0,'2026-04-02 14:50:27','system','2026-04-02 14:50:27','system',0),(4,'WF_APPROVAL_FINISH','??????','{\"zh-CN\":\"??????\",\"en-US\":\"Approval Finish\"}','1.0.0','NOTICE',1,'?????????????',0,'2026-04-02 14:50:27','system','2026-04-02 14:50:27','system',0),(5,'WF_PENDING','审批待办通知','{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\"}','1.0.0','NOTICE',1,'当审批流程流转到某个节点时，通知待审批人有新的待办任务',NULL,'2026-04-06 19:44:59',NULL,'2026-04-06 19:44:59',NULL,0);
/*!40000 ALTER TABLE `sys_message_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message_template_content`
--

DROP TABLE IF EXISTS `sys_message_template_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template_content` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `platform` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `content_title_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `content_body` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `content_body_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `link_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_message_template_content_tpl` (`template_id`,`platform`,`tenant_id`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template_content`
--

LOCK TABLES `sys_message_template_content` WRITE;
/*!40000 ALTER TABLE `sys_message_template_content` DISABLE KEYS */;
INSERT INTO `sys_message_template_content` VALUES (1,1,'INTERNAL','?????????????????',NULL,'?????${taskName}\n????${initiatorName}\n?????${startTime}\n???${linkUrl}',NULL,'${linkUrl}',0,'2026-04-02 14:50:27',0),(2,2,'INTERNAL','?????????????',NULL,'?????${taskName}\n????${approverName}\n?????${comment}\n???${linkUrl}',NULL,'${linkUrl}',0,'2026-04-02 14:50:27',0),(3,3,'INTERNAL','??????????????',NULL,'?????${taskName}\n????${approverName}\n?????${rejectReason}\n???${linkUrl}',NULL,'${linkUrl}',0,'2026-04-02 14:50:27',0),(4,4,'INTERNAL','???????????????',NULL,'?????${taskName}\n?????${finishStatus}\n?????${finishTime}\n???${linkUrl}',NULL,'${linkUrl}',0,'2026-04-02 14:50:27',0);
/*!40000 ALTER TABLE `sys_message_template_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message_template_receiver`
--

DROP TABLE IF EXISTS `sys_message_template_receiver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template_receiver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `template_id` bigint NOT NULL,
  `receiver_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `receiver_ids` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `tenant_id` bigint NOT NULL DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_message_template_receiver_tpl` (`template_id`,`receiver_type`,`tenant_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template_receiver`
--

LOCK TABLES `sys_message_template_receiver` WRITE;
/*!40000 ALTER TABLE `sys_message_template_receiver` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_message_template_receiver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_my_task`
--

DROP TABLE IF EXISTS `wf_my_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_my_task` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` bigint NOT NULL,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `node_id` bigint DEFAULT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `approval_instance_id` bigint DEFAULT NULL COMMENT '审批实例 ID',
  `execution_detail_id` bigint DEFAULT NULL COMMENT '审批执行明细 ID',
  `approver_id` bigint DEFAULT NULL COMMENT '当前审批人 ID',
  `deadline_time` datetime DEFAULT NULL COMMENT '审批截止时间',
  `approver_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `status` int NOT NULL DEFAULT '0',
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_my_task_exec` (`tenant_id`,`execution_id`,`status`) USING BTREE,
  KEY `idx_wf_my_task_instance` (`approval_instance_id`,`status`) USING BTREE,
  KEY `idx_wf_my_task_approver` (`approver_id`,`status`,`deadline_time`) USING BTREE,
  KEY `idx_wf_my_task_exec_detail` (`execution_id`,`execution_detail_id`,`node_id`,`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_my_task`
--

LOCK TABLES `wf_my_task` WRITE;
/*!40000 ALTER TABLE `wf_my_task` DISABLE KEYS */;
INSERT INTO `wf_my_task` VALUES (1,1,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-05 17:21:04'),(2,2,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-06 17:49:16'),(3,3,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-06 23:26:13'),(4,4,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-06 23:50:15'),(5,5,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-07 08:47:28'),(6,6,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-07 11:34:44'),(7,7,'请假审批',15,'管理员审批',NULL,NULL,NULL,NULL,'[1993479637244170242]',1,1,'2026-04-09 10:13:33'),(8,13,'xa',37,'审批节点',1,30,1993479637244170242,NULL,'[1993479637244170242]',1,1,'2026-04-22 19:50:41'),(9,13,'xa',37,'审批节点',2,30,1993479637244170253,NULL,'[1993479637244170253]',1,1,'2026-04-22 19:50:41'),(10,13,'xa',37,'审批节点',NULL,NULL,NULL,NULL,'[1993479637244170242,1993479637244170253]',1,1,'2026-04-22 19:50:41'),(11,13,'xa',37,'审批节点',NULL,NULL,NULL,NULL,'[1993479637244170242,1993479637244170253]',1,1,'2026-04-22 19:50:41'),(12,14,'xa',37,'审批节点',3,33,1993479637244170242,NULL,'[1993479637244170242]',1,1,'2026-04-22 23:29:25'),(13,14,'xa',37,'审批节点',4,33,1993479637244170253,NULL,'[1993479637244170253]',1,1,'2026-04-22 23:29:25'),(14,14,'xa',37,'审批节点',NULL,NULL,NULL,NULL,'[1993479637244170242,1993479637244170253]',1,1,'2026-04-22 23:29:25'),(15,14,'xa',37,'审批节点',NULL,NULL,NULL,NULL,'[1993479637244170242,1993479637244170253]',1,1,'2026-04-22 23:29:25'),(16,15,'请假审批',15,'管理员审批',5,36,1993479637244170242,NULL,'[1993479637244170242]',1,1,'2026-06-26 19:53:31'),(17,16,'请假审批',51,'管理员审批',6,38,1,NULL,'[1]',1,1,'2026-06-26 19:56:43'),(18,17,'请假审批',51,'管理员审批',7,41,1,NULL,'[1]',1,1,'2026-08-19 21:56:22');
/*!40000 ALTER TABLE `wf_my_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_approval_action_log`
--

DROP TABLE IF EXISTS `wf_task_approval_action_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_approval_action_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` bigint NOT NULL COMMENT '审批执行ID',
  `execution_detail_id` bigint DEFAULT NULL COMMENT '审批执行明细ID',
  `node_id` bigint DEFAULT NULL COMMENT '节点ID',
  `approval_instance_id` bigint DEFAULT NULL COMMENT '审批实例ID',
  `action_type` int NOT NULL COMMENT '动作类型：1通过 2驳回 3转交 4加签 5委托 6超时通过 7超时转交 8系统关闭',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID',
  `operator_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人姓名',
  `target_user_id` bigint DEFAULT NULL COMMENT '目标用户ID',
  `target_user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '目标用户姓名',
  `action_comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '动作说明',
  `action_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '动作快照JSON',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_approval_action_execution` (`execution_id`,`node_id`) USING BTREE,
  KEY `idx_wf_task_approval_action_instance` (`approval_instance_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工作流审批动作日志表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_approval_action_log`
--

LOCK TABLES `wf_task_approval_action_log` WRITE;
/*!40000 ALTER TABLE `wf_task_approval_action_log` DISABLE KEYS */;
INSERT INTO `wf_task_approval_action_log` VALUES (1,13,30,37,2,1,1993479637244170253,'孙明岩',1993479637244170253,'用户1993479637244170253','cs','{\"actionType\":1,\"activated\":true,\"approveTime\":1776867816416,\"approverId\":1993479637244170253,\"approverName\":\"用户1993479637244170253\",\"approverSourceType\":3,\"comment\":\"cs\",\"createTime\":1776858641000,\"deleted\":0,\"executionDetailId\":30,\"executionId\":13,\"id\":2,\"instanceNo\":\"13-37-2\",\"nodeId\":37,\"sourceRuleId\":5,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1993479637311279107],\\\"approverType\\\":3}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":5,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1993479636925403138,\"updateTime\":1776858641000}',1,'2026-04-22 22:23:36',0),(2,14,33,37,4,1,1993479637244170253,'孙明岩',1993479637244170253,'用户1993479637244170253','cs','{\"actionType\":1,\"activated\":true,\"approveTime\":1776871796732,\"approverId\":1993479637244170253,\"approverName\":\"用户1993479637244170253\",\"approverSourceType\":3,\"comment\":\"cs\",\"createTime\":1776871765000,\"deleted\":0,\"executionDetailId\":33,\"executionId\":14,\"id\":4,\"instanceNo\":\"14-37-2\",\"nodeId\":37,\"sourceRuleId\":5,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1993479637311279107],\\\"approverType\\\":3}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":5,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1993479636925403138,\"updateTime\":1776871765000}',1,'2026-04-22 23:29:56',0),(3,16,38,51,6,1,1,'admin',1,'用户1','cs','{\"actionType\":1,\"activated\":true,\"approveTime\":1782475055334,\"approverId\":1,\"approverName\":\"用户1\",\"approverSourceType\":1,\"comment\":\"cs\",\"createTime\":1782475003000,\"deleted\":0,\"executionDetailId\":38,\"executionId\":16,\"id\":6,\"instanceNo\":\"16-51-1\",\"nodeId\":51,\"sourceRuleId\":7,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1],\\\"approverType\\\":1}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":7,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1,\"updateTime\":1782475003000}',1,'2026-06-26 19:57:35',0),(4,17,41,51,7,1,1,'admin',1,'用户1','批量同意','{\"actionType\":1,\"activated\":true,\"approveTime\":1787147829782,\"approverId\":1,\"approverName\":\"用户1\",\"approverSourceType\":1,\"comment\":\"批量同意\",\"createTime\":1787147782000,\"deleted\":0,\"executionDetailId\":41,\"executionId\":17,\"id\":7,\"instanceNo\":\"17-51-1\",\"nodeId\":51,\"sourceRuleId\":7,\"sourceSnapshot\":\"{\\\"allowAddSign\\\":false,\\\"allowDelegate\\\":false,\\\"allowInitiatorSelect\\\":false,\\\"allowRecall\\\":true,\\\"allowTransfer\\\":false,\\\"approveMode\\\":2,\\\"approvers\\\":[{\\\"approverIds\\\":[1],\\\"approverType\\\":1}],\\\"fallbackApproverIds\\\":[],\\\"id\\\":7,\\\"ruleName\\\":\\\"默认规则\\\",\\\"ruleType\\\":1,\\\"sortOrder\\\":1,\\\"superiorLevel\\\":1}\",\"status\":1,\"tenantId\":1,\"updateTime\":1787147782000}',1,'2026-08-19 21:57:09',0);
/*!40000 ALTER TABLE `wf_task_approval_action_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_approval_instance`
--

DROP TABLE IF EXISTS `wf_task_approval_instance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_approval_instance` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `execution_id` bigint NOT NULL COMMENT '审批执行ID',
  `execution_detail_id` bigint NOT NULL COMMENT '审批执行明细ID',
  `node_id` bigint NOT NULL COMMENT '节点ID',
  `instance_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '审批实例编号',
  `approver_id` bigint NOT NULL COMMENT '审批人ID',
  `approver_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '审批人姓名',
  `approver_source_type` int DEFAULT NULL COMMENT '审批来源类型',
  `source_rule_id` bigint DEFAULT NULL COMMENT '来源规则ID',
  `source_snapshot` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '来源快照JSON',
  `status` int NOT NULL DEFAULT '0' COMMENT '实例状态：0待处理 1已通过 2已驳回 3已转交 4已关闭',
  `action_type` int DEFAULT NULL COMMENT '动作类型',
  `comment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '处理意见',
  `approve_time` datetime DEFAULT NULL COMMENT '处理时间',
  `deadline_time` datetime DEFAULT NULL COMMENT '截止时间',
  `activated` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否激活',
  `delegate_from_user_id` bigint DEFAULT NULL COMMENT '委托来源用户ID',
  `transfer_from_user_id` bigint DEFAULT NULL COMMENT '转交来源用户ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wf_task_approval_instance_no` (`instance_no`) USING BTREE,
  KEY `idx_wf_task_approval_instance_execution` (`execution_id`,`node_id`,`status`) USING BTREE,
  KEY `idx_wf_task_approval_instance_approver` (`approver_id`,`status`,`activated`) USING BTREE,
  KEY `idx_wf_task_approval_instance_deadline` (`deadline_time`,`status`,`activated`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工作流审批运行实例表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_approval_instance`
--

LOCK TABLES `wf_task_approval_instance` WRITE;
/*!40000 ALTER TABLE `wf_task_approval_instance` DISABLE KEYS */;
INSERT INTO `wf_task_approval_instance` VALUES (1,13,30,37,'13-37-1',1993479637244170242,'用户1993479637244170242',3,5,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',4,8,NULL,NULL,NULL,1,NULL,NULL,1,'2026-04-22 19:50:41','2026-05-16 18:28:15',0),(2,13,30,37,'13-37-2',1993479637244170253,'用户1993479637244170253',3,5,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',1,1,'cs','2026-04-22 22:23:36',NULL,1,NULL,NULL,1,'2026-04-22 19:50:41','2026-05-16 18:28:15',0),(3,14,33,37,'14-37-1',1993479637244170242,'用户1993479637244170242',3,5,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',4,8,NULL,NULL,NULL,1,NULL,NULL,1,'2026-04-22 23:29:25','2026-05-16 18:28:15',0),(4,14,33,37,'14-37-2',1993479637244170253,'用户1993479637244170253',3,5,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1993479637311279107],\"approverType\":3}],\"fallbackApproverIds\":[],\"id\":5,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',1,1,'cs','2026-04-22 23:29:57',NULL,1,NULL,NULL,1,'2026-04-22 23:29:25','2026-05-16 18:28:15',0),(5,15,36,15,'15-15-1',1993479637244170242,'用户1993479637244170242',1,NULL,NULL,0,NULL,NULL,NULL,NULL,1,NULL,NULL,1,'2026-06-26 19:53:31','2026-06-26 19:53:31',0),(6,16,38,51,'16-51-1',1,'用户1',1,7,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1],\"approverType\":1}],\"fallbackApproverIds\":[],\"id\":7,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',1,1,'cs','2026-06-26 19:57:35',NULL,1,NULL,NULL,1,'2026-06-26 19:56:43','2026-06-26 19:56:43',0),(7,17,41,51,'17-51-1',1,'用户1',1,7,'{\"allowAddSign\":false,\"allowDelegate\":false,\"allowInitiatorSelect\":false,\"allowRecall\":true,\"allowTransfer\":false,\"approveMode\":2,\"approvers\":[{\"approverIds\":[1],\"approverType\":1}],\"fallbackApproverIds\":[],\"id\":7,\"ruleName\":\"默认规则\",\"ruleType\":1,\"sortOrder\":1,\"superiorLevel\":1}',1,1,'批量同意','2026-08-19 21:57:10',NULL,1,NULL,NULL,1,'2026-08-19 21:56:22','2026-08-19 21:56:22',0);
/*!40000 ALTER TABLE `wf_task_approval_instance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_cc_record`
--

DROP TABLE IF EXISTS `wf_task_cc_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_cc_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `execution_id` bigint NOT NULL COMMENT '审批执行单ID',
  `execution_detail_id` bigint NOT NULL COMMENT '本轮节点进入对应的执行明细ID',
  `node_id` bigint NOT NULL COMMENT '节点配置ID',
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '抄送节点名称快照',
  `cc_user_id` bigint NOT NULL COMMENT '被抄送用户ID',
  `cc_user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '被抄送用户名称快照',
  `cc_source_type` int DEFAULT NULL COMMENT '来源类型：1=用户，2=部门，3=角色，4=岗位',
  `source_snapshot` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '解析来源快照JSON',
  `read_status` tinyint NOT NULL DEFAULT '0' COMMENT '已读状态：0=未读，1=已读',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `notify_status` tinyint NOT NULL DEFAULT '0' COMMENT '通知状态：0=未发送，1=成功，2=失败',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_wf_task_cc_detail_user` (`execution_detail_id`,`cc_user_id`,`deleted`) USING BTREE,
  KEY `idx_wf_task_cc_user_time` (`tenant_id`,`cc_user_id`,`deleted`,`create_time`) USING BTREE,
  KEY `idx_wf_task_cc_execution` (`tenant_id`,`execution_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='审批节点抄送运行时记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_cc_record`
--

LOCK TABLES `wf_task_cc_record` WRITE;
/*!40000 ALTER TABLE `wf_task_cc_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_task_cc_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_config`
--

DROP TABLE IF EXISTS `wf_task_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `task_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'general' COMMENT '审批分类编码',
  `interpreter_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `callback_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???? HTTP ????',
  `callback_bean` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????? Bean ??',
  `form_type` int DEFAULT NULL,
  `form_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `form_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `status` int NOT NULL DEFAULT '1',
  `version` int NOT NULL DEFAULT '1',
  `config_stage` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED' COMMENT '配置阶段：DRAFT/PUBLISHED/ARCHIVED',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `start_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `approve_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `reject_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `finish_message_template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `link_base_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_task_code_version` (`tenant_id`,`task_code`,`version`,`deleted`) USING BTREE,
  KEY `idx_task_code_stage` (`tenant_id`,`task_code`,`config_stage`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_config`
--

LOCK TABLES `wf_task_config` WRITE;
/*!40000 ALTER TABLE `wf_task_config` DISABLE KEYS */;
INSERT INTO `wf_task_config` VALUES (1,1,'2026-04-04 18:36:04','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'请假审批','{\"zh-CN\":\"请假审批\",\"en-US\":\"Leave Approval\",\"zh-TW\":\"請假審批\",\"ja-JP\":\"休暇承認\",\"ko-KR\":\"휴가 승인\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave',NULL,1,1,'ARCHIVED','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL),(4,1,'2026-04-04 21:49:50','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'请假审批','{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave',NULL,1,2,'ARCHIVED','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL),(5,1,'2026-04-05 11:48:34','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'请假审批','{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave','{\"version\":\"1.0.0\",\"fields\":[]}',1,3,'ARCHIVED','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL),(6,1,'2026-04-15 17:42:42','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'xa',NULL,'cs','general','cs',NULL,NULL,2,NULL,'{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"input\",\"field\":\"Fl98mo2ne143afc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Ffahmo2ne143agc\",\"name\":\"ref_Fer6mo2ne143ahc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false},{\"type\":\"input\",\"field\":\"field_1\",\"title\":\"字段 1\",\"col\":{\"span\":12},\"_fc_id\":\"id_Fby5mo2mrz4iabc\",\"name\":\"ref_Fa42mo2mrz4iacc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}',1,1,'ARCHIVED',NULL,NULL,NULL,NULL,NULL,NULL),(7,1,'2026-04-17 16:52:52','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'xa',NULL,'cs','general','cs',NULL,NULL,2,NULL,'{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"input\",\"field\":\"Fl98mo2ne143afc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Ffahmo2ne143agc\",\"name\":\"ref_Fer6mo2ne143ahc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false},{\"type\":\"input\",\"field\":\"field_1\",\"title\":\"字段 1\",\"col\":{\"span\":12},\"_fc_id\":\"id_Fby5mo2mrz4iabc\",\"name\":\"ref_Fa42mo2mrz4iacc\",\"_fc_drag_tag\":\"input\",\"display\":true,\"hidden\":false}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}',1,2,'ARCHIVED',NULL,NULL,NULL,NULL,NULL,NULL),(8,1,'2026-04-20 10:47:01','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'xa',NULL,'cs','general','cs',NULL,NULL,2,NULL,'{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"fcRow\",\"children\":[{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"F5r6mo9gbopqamc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Fgb2mo9gbopqanc\",\"name\":\"ref_F8zmmo9gbopqaoc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fb59mo9gbfinaic\",\"name\":\"ref_Fi5emo9gbfinajc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"},{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"Fynrmo9gbqa5apc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_F7xamo9gbqa5aqc\",\"name\":\"ref_Ff2cmo9gbqa5arc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fhc4mo9gbfinakc\",\"name\":\"ref_Fx8dmo9gbfinalc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"}],\"_fc_id\":\"id_Fgu2mo9gbfinagc\",\"name\":\"ref_Fe2lmo9gbfinahc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"fcRow\"}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}',1,3,'PUBLISHED',NULL,NULL,NULL,NULL,NULL,NULL),(9,1,'2026-04-22 10:47:07','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'xa',NULL,'cs','general','cs',NULL,NULL,2,NULL,'{\"version\":\"3.0.0\",\"formCreateVersion\":\"ant-design-vue\",\"designerType\":\"form-create\",\"rule\":[{\"type\":\"fcRow\",\"children\":[{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"F5r6mo9gbopqamc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_Fgb2mo9gbopqanc\",\"name\":\"ref_F8zmmo9gbopqaoc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fb59mo9gbfinaic\",\"name\":\"ref_Fi5emo9gbfinajc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"},{\"type\":\"col\",\"props\":{\"span\":12},\"children\":[{\"type\":\"input\",\"field\":\"Fynrmo9gbqa5apc\",\"title\":\"输入框\",\"info\":\"\",\"$required\":false,\"_fc_id\":\"id_F7xamo9gbqa5aqc\",\"name\":\"ref_Ff2cmo9gbqa5arc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"input\"}],\"_fc_id\":\"id_Fhc4mo9gbfinakc\",\"name\":\"ref_Fx8dmo9gbfinalc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"col\"}],\"_fc_id\":\"id_Fgu2mo9gbfinagc\",\"name\":\"ref_Fe2lmo9gbfinahc\",\"display\":true,\"hidden\":false,\"_fc_drag_tag\":\"fcRow\"}],\"option\":{\"form\":{\"layout\":\"vertical\",\"labelAlign\":\"right\",\"size\":\"middle\",\"colon\":false,\"labelCol\":{\"style\":{\"width\":\"120px\"}},\"wrapperCol\":{\"span\":24}},\"row\":{\"gutter\":16},\"submitBtn\":{\"show\":false,\"innerText\":\"提交\"},\"resetBtn\":{\"show\":false,\"innerText\":\"重置\"}}}',1,4,'DRAFT',NULL,NULL,NULL,NULL,NULL,NULL),(10,1,'2026-04-26 16:26:06','system','2026-05-16 18:28:15','system',0,'供应商资质审查','{\"en-US\": \"Supplier Qualification Review\", \"zh-CN\": \"供应商资质审查\"}','SUPPLIER_QUALIFICATION_REVIEW','basic_supplier',NULL,'http://forgex-basic/basic/supplier/workflow/callback',NULL,1,'/basic/supplier/review',NULL,1,1,'PUBLISHED','供应商主数据资质审查流程：发起 -> 审批 -> 回写审查状态',NULL,NULL,NULL,NULL,NULL),(11,1,'2026-04-30 10:58:17','1993479637244170242','2026-05-16 18:28:15','1993479637244170242',0,'供应商资质审查','{\"en-US\": \"Supplier Qualification Review\", \"zh-CN\": \"供应商资质审查\"}','SUPPLIER_QUALIFICATION_REVIEW','basic_supplier',NULL,'http://forgex-basic/basic/supplier/workflow/callback',NULL,1,'/basic/supplier/review',NULL,1,2,'DRAFT','供应商主数据资质审查流程：发起 -> 审批 -> 回写审查状态',NULL,NULL,NULL,NULL,NULL),(12,2,'2026-05-13 09:10:00',NULL,'2026-05-16 18:28:15',NULL,0,'请假审批','{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave',NULL,1,1,'PUBLISHED','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL),(13,1,'2026-08-19 21:57:30','1','2026-08-19 21:57:30','1',0,'请假审批','{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave','{\"version\":\"1.0.0\",\"fields\":[]}',1,4,'PUBLISHED','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL),(14,1,'2026-09-18 10:34:37','1','2026-09-18 10:34:37','1',0,'请假审批','{\"en-US\":\"Leave Approval\",\"zh-CN\":\"请假审批\"}','LEAVE_APPROVAL_DEMO','general','leaveApprovalInterpreter',NULL,NULL,1,'/workflow/form/leave','{\"version\":\"1.0.0\",\"fields\":[]}',1,5,'DRAFT','演示流程：用户发起 -> admin 审核 -> 结束',NULL,NULL,NULL,NULL,NULL);
/*!40000 ALTER TABLE `wf_task_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_execution`
--

DROP TABLE IF EXISTS `wf_task_execution`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_execution` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_config_id` bigint NOT NULL,
  `task_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `task_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `initiator_id` bigint NOT NULL,
  `initiator_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `current_node_id` bigint DEFAULT NULL,
  `current_node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `form_content` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `start_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `end_time` datetime DEFAULT NULL,
  `status` int NOT NULL DEFAULT '1',
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_execution_task` (`tenant_id`,`task_config_id`,`status`,`deleted`) USING BTREE,
  KEY `idx_wf_task_execution_initiator` (`tenant_id`,`initiator_id`,`status`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_execution`
--

LOCK TABLES `wf_task_execution` WRITE;
/*!40000 ALTER TABLE `wf_task_execution` DISABLE KEYS */;
INSERT INTO `wf_task_execution` VALUES (1,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',16,'审批结束','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-05\",\"endDate\":\"2026-04-05\",\"leaveDays\":1,\"reason\":\"cs\",\"handoverPerson\":\"\",\"contactPhone\":\"15678933321\"}','2026-04-05 17:21:04','2026-04-05 17:21:28',2,1,'2026-04-05 17:21:04','2026-05-16 18:28:15',0),(2,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',15,'管理员审批','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-06 17:49:16','2026-04-06 21:03:34',3,1,'2026-04-06 17:49:16','2026-05-16 18:28:15',0),(3,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',15,'管理员审批','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"xx\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-06 23:26:13','2026-04-06 23:47:08',3,1,'2026-04-06 23:26:12','2026-05-16 18:28:15',0),(4,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',16,'审批结束','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-06\",\"endDate\":\"2026-04-06\",\"leaveDays\":1,\"reason\":\"22\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-06 23:50:15','2026-04-06 23:51:01',2,1,'2026-04-06 23:50:15','2026-05-16 18:28:15',0),(5,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',15,'管理员审批','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-07\",\"endDate\":\"2026-04-07\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-07 08:47:28','2026-04-07 10:57:45',3,1,'2026-04-07 08:47:28','2026-05-16 18:28:15',0),(6,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',15,'管理员审批','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-07\",\"endDate\":\"2026-04-07\",\"leaveDays\":1,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-07 11:34:45','2026-04-07 11:35:13',3,1,'2026-04-07 11:34:44','2026-05-16 18:28:15',0),(7,4,'LEAVE_APPROVAL_DEMO','请假审批',1993479637244170242,'admin',16,'审批结束','{\"leaveType\":\"personal\",\"startDate\":\"2026-04-09\",\"endDate\":\"2026-04-09\",\"leaveDays\":1,\"reason\":\"cs\",\"handoverPerson\":\"1\",\"contactPhone\":\"15678933321\"}','2026-04-09 10:13:33','2026-04-09 10:14:09',2,1,'2026-04-09 10:13:33','2026-05-16 18:28:15',0),(8,6,'cs','xa',1993479637244170242,'admin',24,'结束','{\"Fl98mo2ne143afc\":\"1\",\"field_1\":\"1\"}','2026-04-17 16:58:32','2026-04-17 16:58:32',2,1,'2026-04-17 16:58:31','2026-05-16 18:28:15',0),(13,8,'cs','xa',1993479637244170242,'admin',36,'结束','{\"F5r6mo9gbopqamc\":\"1\",\"Fynrmo9gbqa5apc\":\"1\"}','2026-04-22 19:50:41','2026-04-22 22:23:37',2,1,'2026-04-22 19:50:41','2026-05-16 18:28:15',0),(14,8,'cs','xa',1993479637244170253,'孙明岩',36,'结束','{\"F5r6mo9gbopqamc\":\"x\",\"Fynrmo9gbqa5apc\":\"x\"}','2026-04-22 23:29:25','2026-04-22 23:29:57',2,1,'2026-04-22 23:29:25','2026-05-16 18:28:15',0),(15,4,'LEAVE_APPROVAL_DEMO','请假审批',1,'admin',15,'管理员审批','{\"leaveType\":\"personal\",\"startDate\":\"2026-06-26\",\"endDate\":\"2026-06-27\",\"leaveDays\":2,\"reason\":\"1\",\"handoverPerson\":\"1\",\"contactPhone\":\"15679559886\"}','2026-06-26 19:53:32','2026-06-26 19:54:44',3,1,'2026-06-26 19:53:31','2026-06-26 19:53:31',0),(16,5,'LEAVE_APPROVAL_DEMO','请假审批',1,'admin',52,'审批结束','{\"leaveType\":\"personal\",\"startDate\":\"2026-06-26\",\"endDate\":\"2026-06-27\",\"leaveDays\":2,\"reason\":\"c\",\"handoverPerson\":\"1\",\"contactPhone\":\"15679559886\"}','2026-06-26 19:56:44','2026-06-26 19:57:36',2,1,'2026-06-26 19:56:43','2026-06-26 19:56:43',0),(17,5,'LEAVE_APPROVAL_DEMO','请假审批',1,'admin',52,'审批结束','{\"leaveType\":\"personal\",\"startDate\":\"2026-08-26\",\"endDate\":\"2026-08-26\",\"leaveDays\":1,\"reason\":\"ss\",\"handoverPerson\":\"\",\"contactPhone\":\"ss\"}','2026-08-19 21:56:23','2026-08-19 21:57:10',2,1,'2026-08-19 21:56:22','2026-08-19 21:56:22',0);
/*!40000 ALTER TABLE `wf_task_execution` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_execution_approver`
--

DROP TABLE IF EXISTS `wf_task_execution_approver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_execution_approver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_detail_id` bigint NOT NULL,
  `execution_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `approver_detail` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `reject_type` int DEFAULT NULL,
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_execution_approver_exec` (`tenant_id`,`execution_id`,`node_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_execution_approver`
--

LOCK TABLES `wf_task_execution_approver` WRITE;
/*!40000 ALTER TABLE `wf_task_execution_approver` DISABLE KEYS */;
INSERT INTO `wf_task_execution_approver` VALUES (1,2,1,15,'[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-05 17:21:27\",\"approverName\":\"admin\"}]',NULL,1,'2026-04-05 17:21:27','2026-05-16 18:28:15'),(2,5,2,15,'[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-06 21:03:34\",\"approverName\":\"admin\"}]',1,1,'2026-04-06 21:03:34','2026-05-16 18:28:15'),(3,7,3,15,'[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-06 23:47:07\",\"approverName\":\"admin\"}]',1,1,'2026-04-06 23:47:07','2026-05-16 18:28:15'),(4,9,4,15,'[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"1\",\"approveTime\":\"2026-04-06 23:51:00\",\"approverName\":\"admin\"}]',NULL,1,'2026-04-06 23:51:00','2026-05-16 18:28:15'),(5,12,5,15,'[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-07 10:57:44\",\"approverName\":\"admin\"}]',1,1,'2026-04-07 10:57:44','2026-05-16 18:28:15'),(6,14,6,15,'[{\"approveStatus\":2,\"approverId\":1993479637244170242,\"comment\":\"1\",\"approveTime\":\"2026-04-07 11:35:12\",\"approverName\":\"admin\"}]',1,1,'2026-04-07 11:35:12','2026-05-16 18:28:15'),(7,16,7,15,'[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-09 10:14:08\",\"approverName\":\"admin\"}]',NULL,1,'2026-04-09 10:14:08','2026-05-16 18:28:15'),(8,30,13,37,'[{\"approveStatus\":1,\"approverId\":1993479637244170242,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 19:51:02\",\"approverName\":\"admin\"},{\"approveStatus\":1,\"approverId\":1993479637244170253,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 22:23:36\",\"approverName\":\"孙明岩\"}]',NULL,1,'2026-04-22 19:51:02','2026-05-16 18:28:15'),(9,33,14,37,'[{\"approveStatus\":1,\"approverId\":1993479637244170253,\"comment\":\"cs\",\"approveTime\":\"2026-04-22 23:29:56\",\"approverName\":\"孙明岩\"}]',NULL,1,'2026-04-22 23:29:56','2026-05-16 18:28:15'),(10,38,16,51,'[{\"approveStatus\":1,\"approverId\":1,\"comment\":\"cs\",\"approveTime\":\"2026-06-26 19:57:35\",\"approverName\":\"admin\"}]',NULL,1,'2026-06-26 19:57:35','2026-06-26 19:57:35'),(11,41,17,51,'[{\"approveStatus\":1,\"approverId\":1,\"comment\":\"批量同意\",\"approveTime\":\"2026-08-19 21:57:09\",\"approverName\":\"admin\"}]',NULL,1,'2026-08-19 21:57:09','2026-08-19 21:57:09');
/*!40000 ALTER TABLE `wf_task_execution_approver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_execution_detail`
--

DROP TABLE IF EXISTS `wf_task_execution_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_execution_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `execution_id` bigint NOT NULL,
  `node_id` bigint NOT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `current_status` int NOT NULL DEFAULT '0',
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_execution_detail_exec` (`tenant_id`,`execution_id`,`node_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_execution_detail`
--

LOCK TABLES `wf_task_execution_detail` WRITE;
/*!40000 ALTER TABLE `wf_task_execution_detail` DISABLE KEYS */;
INSERT INTO `wf_task_execution_detail` VALUES (1,1,14,'发起请假',1,1,'2026-04-05 17:21:04','2026-05-16 18:28:15'),(2,1,15,'管理员审批',1,1,'2026-04-05 17:21:04','2026-05-16 18:28:15'),(3,1,16,'审批结束',1,1,'2026-04-05 17:21:27','2026-05-16 18:28:15'),(4,2,14,'发起请假',1,1,'2026-04-06 17:49:16','2026-05-16 18:28:15'),(5,2,15,'管理员审批',2,1,'2026-04-06 17:49:16','2026-05-16 18:28:15'),(6,3,14,'发起请假',1,1,'2026-04-06 23:26:12','2026-05-16 18:28:15'),(7,3,15,'管理员审批',2,1,'2026-04-06 23:26:13','2026-05-16 18:28:15'),(8,4,14,'发起请假',1,1,'2026-04-06 23:50:15','2026-05-16 18:28:15'),(9,4,15,'管理员审批',1,1,'2026-04-06 23:50:15','2026-05-16 18:28:15'),(10,4,16,'审批结束',1,1,'2026-04-06 23:51:00','2026-05-16 18:28:15'),(11,5,14,'发起请假',1,1,'2026-04-07 08:47:28','2026-05-16 18:28:15'),(12,5,15,'管理员审批',2,1,'2026-04-07 08:47:28','2026-05-16 18:28:15'),(13,6,14,'发起请假',1,1,'2026-04-07 11:34:44','2026-05-16 18:28:15'),(14,6,15,'管理员审批',2,1,'2026-04-07 11:34:44','2026-05-16 18:28:15'),(15,7,14,'发起请假',1,1,'2026-04-09 10:13:33','2026-05-16 18:28:15'),(16,7,15,'管理员审批',1,1,'2026-04-09 10:13:33','2026-05-16 18:28:15'),(17,7,16,'审批结束',1,1,'2026-04-09 10:14:08','2026-05-16 18:28:15'),(18,8,23,'开始',1,1,'2026-04-17 16:58:31','2026-05-16 18:28:15'),(19,8,25,'审批节点',1,1,'2026-04-17 16:58:31','2026-05-16 18:28:15'),(20,8,24,'结束',1,1,'2026-04-17 16:58:31','2026-05-16 18:28:15'),(29,13,35,'开始',1,1,'2026-04-22 19:50:41','2026-05-16 18:28:15'),(30,13,37,'审批节点',1,1,'2026-04-22 19:50:41','2026-05-16 18:28:15'),(31,13,36,'结束',1,1,'2026-04-22 22:23:36','2026-05-16 18:28:15'),(32,14,35,'开始',1,1,'2026-04-22 23:29:25','2026-05-16 18:28:15'),(33,14,37,'审批节点',1,1,'2026-04-22 23:29:25','2026-05-16 18:28:15'),(34,14,36,'结束',1,1,'2026-04-22 23:29:56','2026-05-16 18:28:15'),(35,15,14,'发起请假',1,1,'2026-06-26 19:53:31','2026-06-26 19:53:31'),(36,15,15,'管理员审批',0,1,'2026-06-26 19:53:31','2026-06-26 19:53:31'),(37,16,50,'发起请假',1,1,'2026-06-26 19:56:43','2026-06-26 19:56:43'),(38,16,51,'管理员审批',1,1,'2026-06-26 19:56:43','2026-06-26 19:56:43'),(39,16,52,'审批结束',1,1,'2026-06-26 19:57:35','2026-06-26 19:57:35'),(40,17,50,'发起请假',1,1,'2026-08-19 21:56:22','2026-08-19 21:56:22'),(41,17,51,'管理员审批',1,1,'2026-08-19 21:56:22','2026-08-19 21:56:22'),(42,17,52,'审批结束',1,1,'2026-08-19 21:57:10','2026-08-19 21:57:10');
/*!40000 ALTER TABLE `wf_task_execution_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_node_approver`
--

DROP TABLE IF EXISTS `wf_task_node_approver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_node_approver` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `node_config_id` bigint NOT NULL,
  `approver_type` int NOT NULL,
  `approver_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_node_approver_node` (`tenant_id`,`node_config_id`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_node_approver`
--

LOCK TABLES `wf_task_node_approver` WRITE;
/*!40000 ALTER TABLE `wf_task_node_approver` DISABLE KEYS */;
INSERT INTO `wf_task_node_approver` VALUES (1,2,1,'[1993479637244170242]',1,'2026-04-04 18:36:04',0),(2,5,1,'[1993479637244170242]',1,'2026-04-04 21:10:44',0),(3,8,1,'[1993479637244170242]',1,'2026-04-04 21:49:49',1),(4,9,1,'[1993479637244170242]',1,'2026-04-04 21:49:49',1),(5,12,1,'[1993479637244170242]',1,'2026-04-05 11:48:24',1),(6,15,1,'[1993479637244170242]',1,'2026-04-05 11:48:26',0),(7,18,1,'[1993479637244170242]',1,'2026-04-05 11:48:34',1),(8,21,1,'[1]',1,'2026-04-05 14:36:01',0),(9,25,3,'[1993479637311279000]',1,'2026-04-17 16:52:47',0),(10,28,3,'[1993479637311279000]',1,'2026-04-17 16:52:52',1),(11,31,3,'[1993479637311279107]',1,'2026-04-17 17:29:01',0),(12,34,3,'[1993479637311279107]',1,'2026-04-20 10:47:01',1),(13,37,3,'[1993479637311279107]',1,'2026-04-22 10:46:59',0),(14,40,3,'[1993479637311279107]',1,'2026-04-22 10:47:06',0),(15,42,1,'[1993479637244170242]',1,'2026-04-26 16:26:06',0),(16,45,1,'[1993479637244170242]',1,'2026-04-30 10:58:17',0),(17,48,1,'[2]',2,'2026-05-13 09:10:00',0),(18,51,1,'[1]',1,'2026-06-26 19:54:36',0),(19,54,1,'[1]',1,'2026-08-12 19:46:29',0),(20,57,1,'[1]',1,'2026-08-19 21:57:30',1),(21,58,1,'[1]',1,'2026-08-19 21:57:30',1),(22,61,1,'[1]',1,'2026-08-19 21:57:57',0),(23,64,1,'[1]',1,'2026-08-22 14:36:41',0),(24,67,1,'[1]',1,'2026-09-18 10:34:37',0),(25,68,1,'[1]',1,'2026-09-18 10:34:37',0);
/*!40000 ALTER TABLE `wf_task_node_approver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_node_cc`
--

DROP TABLE IF EXISTS `wf_task_node_cc`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_node_cc` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `node_config_id` bigint NOT NULL COMMENT '审批任务节点配置表ID',
  `cc_type` int NOT NULL COMMENT '抄送对象类型：1=用户，2=部门，3=角色，4=岗位',
  `cc_ids` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '抄送对象ID集合（JSON字符串数组）',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_node_cc_node` (`tenant_id`,`node_config_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='审批节点抄送配置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_node_cc`
--

LOCK TABLES `wf_task_node_cc` WRITE;
/*!40000 ALTER TABLE `wf_task_node_cc` DISABLE KEYS */;
/*!40000 ALTER TABLE `wf_task_node_cc` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_node_config`
--

DROP TABLE IF EXISTS `wf_task_node_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_node_config` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `task_config_id` bigint NOT NULL,
  `node_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '设计器稳定节点标识',
  `node_type` int NOT NULL,
  `node_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `node_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `node_level` int DEFAULT NULL,
  `pre_level` int DEFAULT NULL,
  `pre_node_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `next_level` int DEFAULT NULL,
  `next_node_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `approve_type` int DEFAULT NULL,
  `cc_enabled` tinyint NOT NULL DEFAULT '0' COMMENT '是否启用节点抄送：0=否，1=是',
  `branch_conditions` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `canvas_x` decimal(10,2) DEFAULT NULL COMMENT '画布 X 坐标',
  `canvas_y` decimal(10,2) DEFAULT NULL COMMENT '画布 Y 坐标',
  `order_num` int DEFAULT '0',
  `tenant_id` bigint DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_node_config_task` (`tenant_id`,`task_config_id`,`deleted`) USING BTREE,
  KEY `idx_task_config_node_key` (`task_config_id`,`node_key`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_node_config`
--

LOCK TABLES `wf_task_node_config` WRITE;
/*!40000 ALTER TABLE `wf_task_node_config` DISABLE KEYS */;
INSERT INTO `wf_task_node_config` VALUES (1,1,NULL,1,'发起请假','{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}',1,0,'[]',2,'[2]',NULL,0,NULL,NULL,NULL,1,1,'2026-04-04 18:36:04',0),(2,1,NULL,3,'管理员审批','{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}',2,1,'[1]',3,'[3]',2,0,NULL,NULL,NULL,2,1,'2026-04-04 18:36:04',0),(3,1,NULL,2,'审批结束','{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}',3,2,'[2]',0,'[]',NULL,0,NULL,NULL,NULL,3,1,'2026-04-04 18:36:04',0),(4,1,'start',1,'发起请假','{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}',1,0,'[]',2,'[5]',NULL,0,NULL,120.00,240.00,1,1,'2026-04-04 21:10:44',0),(5,1,'approve_2',3,'管理员审批','{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}',2,1,'[4]',3,'[6]',2,0,NULL,420.00,240.00,2,1,'2026-04-04 21:10:44',0),(6,1,'end',2,'审批结束','{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}',3,2,'[5]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-04-04 21:10:44',0),(7,4,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[8,9]',NULL,0,NULL,0.00,0.00,1,1,'2026-04-04 21:49:49',1),(8,4,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[7]',3,'[10]',2,0,NULL,0.00,0.00,2,1,'2026-04-04 21:49:49',1),(9,4,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[7]',3,'[10]',2,0,NULL,420.00,240.00,3,1,'2026-04-04 21:49:49',1),(10,4,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[8,9]',0,'[]',NULL,0,NULL,0.00,0.00,4,1,'2026-04-04 21:49:49',1),(11,4,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[12]',NULL,0,NULL,188.52,-245.96,1,1,'2026-04-05 11:48:23',1),(12,4,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[11]',3,'[13]',2,0,NULL,157.02,-8.06,2,1,'2026-04-05 11:48:23',1),(13,4,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[12]',0,'[]',NULL,0,NULL,171.40,293.51,3,1,'2026-04-05 11:48:23',1),(14,4,'node_1',1,'发起请假','{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}',1,0,'[]',2,'[15]',NULL,0,NULL,188.52,-245.96,1,1,'2026-04-05 11:48:26',0),(15,4,'node_2',3,'管理员审批','{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}',2,1,'[14]',3,'[16]',2,0,NULL,157.02,-8.06,2,1,'2026-04-05 11:48:26',0),(16,4,'node_3',2,'审批结束','{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}',3,2,'[15]',0,'[]',NULL,0,NULL,171.40,293.51,3,1,'2026-04-05 11:48:26',0),(17,5,'node_1',1,'发起请假','{\"zh-CN\":\"发起请假\",\"en-US\":\"Start Request\",\"zh-TW\":\"發起請假\",\"ja-JP\":\"休暇申請開始\",\"ko-KR\":\"휴가 요청 시작\"}',1,0,'[]',2,'[18]',NULL,0,NULL,188.52,-245.96,1,1,'2026-04-05 11:48:34',1),(18,5,'node_2',3,'管理员审批','{\"zh-CN\":\"管理员审批\",\"en-US\":\"Admin Approval\",\"zh-TW\":\"管理員審批\",\"ja-JP\":\"管理者承認\",\"ko-KR\":\"관리자 승인\"}',2,1,'[17]',3,'[19]',2,0,NULL,157.02,-8.06,2,1,'2026-04-05 11:48:34',1),(19,5,'node_3',2,'审批结束','{\"zh-CN\":\"审批结束\",\"en-US\":\"Approval End\",\"zh-TW\":\"審批結束\",\"ja-JP\":\"承認終了\",\"ko-KR\":\"승인 종료\"}',3,2,'[18]',0,'[]',NULL,0,NULL,171.40,293.51,3,1,'2026-04-05 11:48:34',1),(20,4,'start',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[21]',NULL,0,NULL,120.00,240.00,1,1,'2026-04-05 14:36:01',0),(21,4,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[20]',3,'[22]',2,0,NULL,420.00,240.00,2,1,'2026-04-05 14:36:01',0),(22,4,'end',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[21]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-04-05 14:36:01',0),(23,6,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[25]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-17 16:52:47',0),(24,6,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[25]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-17 16:52:47',0),(25,6,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[23]',3,'[24]',2,0,NULL,22.37,317.49,3,1,'2026-04-17 16:52:47',0),(26,7,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[28]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-17 16:52:52',1),(27,7,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[28]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-17 16:52:52',1),(28,7,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[26]',3,'[27]',2,0,NULL,22.37,317.49,3,1,'2026-04-17 16:52:52',1),(29,7,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[31]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-17 17:29:01',0),(30,7,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[31]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-17 17:29:01',0),(31,7,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[29]',3,'[30]',2,0,NULL,22.37,317.49,3,1,'2026-04-17 17:29:01',0),(32,8,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[34]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-20 10:47:01',1),(33,8,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[34]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-20 10:47:01',1),(34,8,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[32]',3,'[33]',2,0,NULL,22.37,317.49,3,1,'2026-04-20 10:47:01',1),(35,8,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[37]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-22 10:46:59',0),(36,8,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[37]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-22 10:46:59',0),(37,8,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[35]',3,'[36]',2,0,NULL,22.37,317.49,3,1,'2026-04-22 10:46:59',0),(38,9,'start',1,'开始','{\"en-US\":\"开始\",\"zh-CN\":\"开始\"}',1,0,'[]',2,'[40]',NULL,0,NULL,58.33,124.70,1,1,'2026-04-22 10:47:06',0),(39,9,'end',2,'结束','{\"en-US\":\"结束\",\"zh-CN\":\"结束\"}',3,2,'[40]',0,'[]',NULL,0,NULL,38.50,554.52,2,1,'2026-04-22 10:47:06',0),(40,9,'approve_1776415906033_voso',3,'审批节点','{\"en-US\":\"审批节点\",\"zh-CN\":\"审批节点\"}',2,1,'[38]',3,'[39]',2,0,NULL,22.37,317.49,3,1,'2026-04-22 10:47:06',0),(41,10,'start',1,'发起审查','{\"en-US\": \"Start Review\", \"zh-CN\": \"发起审查\"}',1,0,'[]',2,'[42]',NULL,0,NULL,120.00,240.00,1,1,'2026-04-26 16:26:06',0),(42,10,'approve_1',3,'资质审查','{\"en-US\": \"Qualification Review\", \"zh-CN\": \"资质审查\"}',2,1,'[41]',3,'[43]',2,0,NULL,420.00,240.00,2,1,'2026-04-26 16:26:06',0),(43,10,'end',2,'审查结束','{\"en-US\": \"Review End\", \"zh-CN\": \"审查结束\"}',3,2,'[42]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-04-26 16:26:06',0),(44,11,'start',1,'发起审查','{\"en-US\":\"发起审查\",\"zh-CN\":\"发起审查\"}',1,0,'[]',2,'[45]',NULL,0,NULL,120.00,240.00,1,1,'2026-04-30 10:58:17',0),(45,11,'approve_1',3,'资质审查','{\"en-US\":\"资质审查\",\"zh-CN\":\"资质审查\"}',2,1,'[44]',3,'[46]',2,0,NULL,420.00,240.00,2,1,'2026-04-30 10:58:17',0),(46,11,'end',2,'审查结束','{\"en-US\":\"审查结束\",\"zh-CN\":\"审查结束\"}',3,2,'[45]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-04-30 10:58:17',0),(47,12,'start',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[48]',NULL,0,NULL,120.00,240.00,1,2,'2026-05-13 09:10:00',0),(48,12,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[47]',3,'[49]',2,0,NULL,420.00,240.00,2,2,'2026-05-13 09:10:00',0),(49,12,'end',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[48]',0,'[]',NULL,0,NULL,720.00,240.00,3,2,'2026-05-13 09:10:00',0),(50,5,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[51]',NULL,0,NULL,188.52,-245.96,1,1,'2026-06-26 19:54:36',0),(51,5,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[50]',3,'[52]',2,0,NULL,157.02,-8.06,2,1,'2026-06-26 19:54:36',0),(52,5,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[51]',0,'[]',NULL,0,NULL,171.40,293.51,3,1,'2026-06-26 19:54:36',0),(53,5,'start',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[54]',NULL,0,NULL,120.00,240.00,1,1,'2026-08-12 19:46:29',0),(54,5,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[53]',3,'[55]',2,0,NULL,420.00,240.00,2,1,'2026-08-12 19:46:29',0),(55,5,'end',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[54]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-08-12 19:46:29',0),(56,13,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[57,58]',NULL,0,NULL,188.52,-245.96,1,1,'2026-08-19 21:57:30',1),(57,13,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[56]',3,'[59]',2,0,NULL,157.02,-8.06,2,1,'2026-08-19 21:57:30',1),(58,13,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[56]',3,'[59]',2,0,NULL,420.00,240.00,3,1,'2026-08-19 21:57:30',1),(59,13,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[57,58]',0,'[]',NULL,0,NULL,171.40,293.51,4,1,'2026-08-19 21:57:30',1),(60,13,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[61]',NULL,0,NULL,188.52,-245.96,1,1,'2026-08-19 21:57:57',0),(61,13,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[60]',3,'[62]',2,0,NULL,157.02,-8.06,2,1,'2026-08-19 21:57:57',0),(62,13,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[61]',0,'[]',NULL,0,NULL,171.40,293.51,3,1,'2026-08-19 21:57:57',0),(63,13,'start',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[64]',NULL,0,NULL,120.00,240.00,1,1,'2026-08-22 14:36:41',0),(64,13,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[63]',3,'[65]',2,0,NULL,420.00,240.00,2,1,'2026-08-22 14:36:41',0),(65,13,'end',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[64]',0,'[]',NULL,0,NULL,720.00,240.00,3,1,'2026-08-22 14:36:41',0),(66,14,'node_1',1,'发起请假','{\"en-US\":\"发起请假\",\"zh-CN\":\"发起请假\"}',1,0,'[]',2,'[67,68]',NULL,0,NULL,188.52,-245.96,1,1,'2026-09-18 10:34:37',0),(67,14,'node_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[66]',3,'[69]',2,0,NULL,157.02,-8.06,2,1,'2026-09-18 10:34:37',0),(68,14,'approve_2',3,'管理员审批','{\"en-US\":\"管理员审批\",\"zh-CN\":\"管理员审批\"}',2,1,'[66]',3,'[69]',2,0,NULL,420.00,240.00,3,1,'2026-09-18 10:34:37',0),(69,14,'node_3',2,'审批结束','{\"en-US\":\"审批结束\",\"zh-CN\":\"审批结束\"}',3,2,'[67,68]',0,'[]',NULL,0,NULL,171.40,293.51,4,1,'2026-09-18 10:34:37',0);
/*!40000 ALTER TABLE `wf_task_node_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `wf_task_node_rule`
--

DROP TABLE IF EXISTS `wf_task_node_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `wf_task_node_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `node_config_id` bigint NOT NULL COMMENT '节点配置ID',
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规则名称',
  `rule_type` int NOT NULL COMMENT '规则类型：1静态审批 2发起人自选 3多级上级 4动态追加',
  `approve_mode` int NOT NULL COMMENT '审批模式',
  `approval_threshold` decimal(10,2) DEFAULT NULL COMMENT '会签阈值',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `timeout_hours` int DEFAULT NULL COMMENT '超时小时数',
  `timeout_action` int DEFAULT NULL COMMENT '超时动作：1提醒 2自动通过 3自动转交',
  `allow_initiator_select` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许发起人自选审批人',
  `superior_level` int DEFAULT NULL COMMENT '上级层级',
  `allow_add_sign` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许加签',
  `allow_transfer` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许转交',
  `allow_delegate` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许委托',
  `allow_recall` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否允许发起人撤回',
  `fallback_approver_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '兜底审批人ID列表JSON',
  `extra_config` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '扩展配置JSON',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除标记',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_wf_task_node_rule_node` (`node_config_id`,`deleted`) USING BTREE,
  KEY `idx_wf_task_node_rule_tenant` (`tenant_id`,`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工作流节点运行规则表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `wf_task_node_rule`
--

LOCK TABLES `wf_task_node_rule` WRITE;
/*!40000 ALTER TABLE `wf_task_node_rule` DISABLE KEYS */;
INSERT INTO `wf_task_node_rule` VALUES (1,25,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-17 16:52:47','2026-05-16 18:28:15',0),(2,28,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-17 16:52:52','2026-05-16 18:28:15',1),(3,31,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-17 17:29:01','2026-05-16 18:28:15',0),(4,34,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-20 10:47:01','2026-05-16 18:28:15',1),(5,37,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-22 10:46:59','2026-05-16 18:28:15',0),(6,40,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-04-22 10:47:06','2026-05-16 18:28:15',0),(7,51,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-06-26 19:54:36','2026-06-26 19:54:36',0),(8,57,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-08-19 21:57:30','2026-08-19 21:57:57',1),(9,61,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-08-19 21:57:57','2026-08-19 21:57:57',0),(10,67,'默认规则',1,2,NULL,1,NULL,NULL,0,1,0,0,0,1,'[]',NULL,1,'2026-09-18 10:34:37','2026-09-18 10:34:37',0);
/*!40000 ALTER TABLE `wf_task_node_rule` ENABLE KEYS */;
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
