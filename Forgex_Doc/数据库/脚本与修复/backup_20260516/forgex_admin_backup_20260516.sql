-- MySQL dump 10.13  Distrib 8.0.41, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: forgex_admin
-- ------------------------------------------------------
-- Server version	8.0.41

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
-- Table structure for table `basic_customer`
--

DROP TABLE IF EXISTS `basic_customer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_customer` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `customer_code` varchar(64) NOT NULL COMMENT '客户编码',
  `customer_name` varchar(200) DEFAULT NULL COMMENT '客户名称，兼容旧字段',
  `customer_value_level` varchar(32) DEFAULT NULL COMMENT '客户价值等级',
  `customer_credit_level` varchar(32) DEFAULT NULL COMMENT '客户信用等级',
  `actual_business_address` varchar(500) DEFAULT NULL COMMENT '实际经营地址',
  `business_status` varchar(32) DEFAULT NULL COMMENT '经营状态',
  `collection_address` varchar(500) DEFAULT NULL COMMENT '收款地址',
  `shipping_address` varchar(500) DEFAULT NULL COMMENT '收货地址',
  `approval_status` tinyint NOT NULL DEFAULT '0' COMMENT '审批状态：0-未提交 1-审批中 2-已通过 3-已驳回',
  `is_related_tenant` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否关联租户',
  `related_tenant_code` varchar(100) DEFAULT NULL COMMENT '关联客户租户编码',
  `transport_mode` varchar(100) DEFAULT NULL COMMENT '运输方式',
  `customer_short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户简称',
  `customer_full_name` varchar(200) DEFAULT NULL COMMENT '客户全称',
  `customer_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户类型: DOMESTIC=国内, OVERSEAS=海外',
  `country` varchar(100) DEFAULT NULL COMMENT '国家',
  `enterprise_nature` varchar(32) DEFAULT NULL COMMENT '企业性质',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系邮箱',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '税号',
  `bank_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行账号',
  `payment_terms` varchar(200) DEFAULT NULL COMMENT '支付条件',
  `delivery_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '交货条件',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'CNY' COMMENT '币种',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_customer_code_tenant` (`customer_code`,`tenant_id`,`deleted`) USING BTREE,
  KEY `idx_customer_name` (`customer_name`) USING BTREE,
  KEY `idx_basic_customer_name` (`customer_full_name`),
  KEY `idx_basic_customer_status` (`approval_status`,`status`),
  KEY `idx_basic_customer_tenant_code` (`related_tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='客户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_customer`
--

LOCK TABLES `basic_customer` WRITE;
/*!40000 ALTER TABLE `basic_customer` DISABLE KEYS */;
INSERT INTO `basic_customer` VALUES (2001,'CUST001','华为技术有限公司',NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,'华为','华为技术有限公司','DOMESTIC','中国',NULL,'广东省','深圳市','深圳','采购部','13900000000','xxxxx@xx.com',NULL,NULL,NULL,NULL,'CNY',1,NULL,1993479636925403138,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0),(2002,'CUST002','苹果公司',NULL,NULL,NULL,NULL,NULL,NULL,0,0,NULL,NULL,'苹果','苹果公司','OVERSEAS','美国',NULL,'加利福尼亚','库比蒂诺','洛杉矶','海外业务部','13900000001','xxxxx@xx.com',NULL,NULL,NULL,NULL,'CNY',1,NULL,1993479636925403138,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_customer` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_customer_contact`
--

DROP TABLE IF EXISTS `basic_customer_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_customer_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `contact_name` varchar(100) DEFAULT NULL COMMENT '联系人',
  `contact_position` varchar(100) DEFAULT NULL COMMENT '联系人职位',
  `contact_phone` varchar(100) DEFAULT NULL COMMENT '联系方式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_customer_contact_customer` (`customer_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户联系人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_customer_contact`
--

LOCK TABLES `basic_customer_contact` WRITE;
/*!40000 ALTER TABLE `basic_customer_contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_customer_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_customer_extra`
--

DROP TABLE IF EXISTS `basic_customer_extra`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_customer_extra` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `official_website` varchar(500) DEFAULT NULL COMMENT '企业官网',
  `switchboard_phone` varchar(100) DEFAULT NULL COMMENT '总机电话',
  `official_email_domain` varchar(200) DEFAULT NULL COMMENT '官方邮箱域名',
  `fax_number` varchar(100) DEFAULT NULL COMMENT '传真号码',
  `social_media_account` varchar(500) DEFAULT NULL COMMENT '公众号或社交媒体账号',
  `equity_penetration_level` int DEFAULT NULL COMMENT '股权穿透层级',
  `holding_relation_flag` varchar(64) DEFAULT NULL COMMENT '控股关系标记',
  `related_enterprise_ids` varchar(1000) DEFAULT NULL COMMENT '关联企业 ID 列表',
  `group_customer_level` varchar(64) DEFAULT NULL COMMENT '所属集团客户分级',
  `channel_partner_level` varchar(32) DEFAULT NULL COMMENT '渠道伙伴等级',
  `cooperation_auth_start_date` date DEFAULT NULL COMMENT '合作授权期限起',
  `cooperation_auth_end_date` date DEFAULT NULL COMMENT '合作授权期限止',
  `national_industry_code` varchar(64) DEFAULT NULL COMMENT '国标行业编码',
  `custom_industry_category` varchar(100) DEFAULT NULL COMMENT '自定义行业分类',
  `registered_capital` decimal(18,2) DEFAULT NULL COMMENT '注册资本',
  `registered_capital_currency` char(3) DEFAULT NULL COMMENT '注册资本币种',
  `paid_in_capital` decimal(18,2) DEFAULT NULL COMMENT '实缴资本',
  `paid_in_capital_currency` char(3) DEFAULT NULL COMMENT '实缴资本币种',
  `business_term_start` date DEFAULT NULL COMMENT '营业期限起',
  `business_term_end` date DEFAULT NULL COMMENT '营业期限止',
  `registration_authority` varchar(200) DEFAULT NULL COMMENT '登记机关',
  `business_scope` text COMMENT '经营范围',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_customer_extra_customer` (`customer_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户其它信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_customer_extra`
--

LOCK TABLES `basic_customer_extra` WRITE;
/*!40000 ALTER TABLE `basic_customer_extra` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_customer_extra` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_customer_invoice`
--

DROP TABLE IF EXISTS `basic_customer_invoice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_customer_invoice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `invoice_full_name` varchar(200) DEFAULT NULL COMMENT '发票抬头全称',
  `tax_number` varchar(100) DEFAULT NULL COMMENT '税号',
  `registered_address` varchar(500) DEFAULT NULL COMMENT '注册地址',
  `registered_phone` varchar(100) DEFAULT NULL COMMENT '注册电话',
  `bank_name` varchar(200) DEFAULT NULL COMMENT '开户行',
  `bank_account` varchar(512) DEFAULT NULL COMMENT '银行账号',
  `invoice_required` tinyint(1) NOT NULL DEFAULT '0' COMMENT '开票必填',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_customer_invoice_customer` (`customer_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='客户发票抬头表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_customer_invoice`
--

LOCK TABLES `basic_customer_invoice` WRITE;
/*!40000 ALTER TABLE `basic_customer_invoice` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_customer_invoice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_factory`
--

DROP TABLE IF EXISTS `basic_factory`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_factory` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `factory_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂编码',
  `factory_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂名称',
  `factory_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工厂类型: MANUFACTURING=制造厂, ASSEMBLY=装配厂, WAREHOUSE=仓库',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工厂地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_factory_code_tenant` (`factory_code`,`tenant_id`,`deleted`) USING BTREE COMMENT '工厂编码唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='工厂信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_factory`
--

LOCK TABLES `basic_factory` WRITE;
/*!40000 ALTER TABLE `basic_factory` DISABLE KEYS */;
INSERT INTO `basic_factory` VALUES (1,'FACTORY_02','一号工厂','MANUFACTURING',NULL,NULL,NULL,1,0,NULL,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0),(2,'FACTORY_01','二号工厂','MANUFACTURING','广东省深圳市','管理员','13800000000',1,1,'默认工厂',1,'2026-04-14 10:54:07','2026-04-14 10:54:07','admin','admin',0);
/*!40000 ALTER TABLE `basic_factory` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_barcode_record`
--

DROP TABLE IF EXISTS `basic_label_barcode_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_barcode_record` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `barcode_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条码号/序列号',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型',
  `material_id` bigint DEFAULT NULL COMMENT '物料 ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物料名称',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商编码',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户编码',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '批次号',
  `lot_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'LOT 号',
  `work_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工单号',
  `engineering_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工程卡号',
  `production_line` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '生产线',
  `business_scene` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务场景：INCOMING=来料，PRODUCTION=生产，OUTBOUND=出库，OTHER=其他',
  `generate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '条码生成时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-失效，1-有效',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `factory_id` bigint DEFAULT NULL COMMENT '工厂 ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_barcode_no_tenant` (`barcode_no`,`tenant_id`,`deleted`) USING BTREE,
  KEY `idx_material_code` (`material_code`) USING BTREE,
  KEY `idx_business_scene` (`business_scene`) USING BTREE,
  KEY `idx_generate_time` (`generate_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='条码记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_barcode_record`
--

LOCK TABLES `basic_label_barcode_record` WRITE;
/*!40000 ALTER TABLE `basic_label_barcode_record` DISABLE KEYS */;
INSERT INTO `basic_label_barcode_record` VALUES (8001,'B202604140001','PRODUCT',5001,'MATERIAL001','电阻 0603 10K',NULL,NULL,'B2026041401','L20260414001',NULL,'ENG20260414001',NULL,'PRODUCTION','2026-04-14 10:54:07',1,1,1001,NULL,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0),(8002,'B202604140002','INCOMING',5002,'MATERIAL002','电容 0805 10UF',NULL,NULL,'B2026041402','L20260414002',NULL,'ENG20260414002',NULL,'INCOMING','2026-04-14 10:54:07',1,1,1001,NULL,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_label_barcode_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_field`
--

DROP TABLE IF EXISTS `basic_label_field`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_field` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `field_code` varchar(100) NOT NULL COMMENT '字段编码',
  `field_name` varchar(100) NOT NULL COMMENT '字段名称',
  `field_type` varchar(32) NOT NULL COMMENT '字段类型：STRING/NUMBER/DATE/DATETIME/BOOLEAN',
  `module_id` bigint NOT NULL COMMENT '模块 ID，关联 sys_module.id',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=否，1=是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_label_field_code` (`tenant_id`,`module_id`,`field_code`,`deleted`),
  KEY `idx_basic_label_field_module` (`tenant_id`,`module_id`,`is_enabled`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签字段表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_field`
--

LOCK TABLES `basic_label_field` WRITE;
/*!40000 ALTER TABLE `basic_label_field` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_label_field` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_print_config`
--

DROP TABLE IF EXISTS `basic_label_print_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_print_config` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'STRING' COMMENT '配置类型：STRING=字符串，NUMBER=数字，BOOLEAN=布尔，JSON=JSON 对象',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '配置说明',
  `scope_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'GLOBAL' COMMENT '作用范围：GLOBAL=全局，FACTORY=工厂，TEMPLATE_TYPE=模板类型',
  `scope_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '作用范围值 (工厂 ID 或模板类型)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_config_key_scope` (`config_key`,`scope_type`,`scope_value`,`tenant_id`,`deleted`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='标签打印参数配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_print_config`
--

LOCK TABLES `basic_label_print_config` WRITE;
/*!40000 ALTER TABLE `basic_label_print_config` DISABLE KEYS */;
INSERT INTO `basic_label_print_config` VALUES (1,'DATE_FORMAT','yyyy-MM-dd','STRING','日期显示格式','GLOBAL',NULL,1,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0),(2,'DATETIME_FORMAT','yyyy-MM-dd HH:mm:ss','STRING','日期时间显示格式','GLOBAL',NULL,1,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0),(3,'NUMBER_DECIMAL_PLACES','2','NUMBER','数量小数位数','GLOBAL',NULL,1,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0),(4,'ENABLE_THOUSAND_SEPARATOR','true','BOOLEAN','是否启用千分位分隔符','GLOBAL',NULL,1,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0),(5,'DEFAULT_PRINT_COUNT','1','NUMBER','默认打印张数','GLOBAL',NULL,1,1,'2026-04-14 10:20:43','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_label_print_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_print_exception`
--

DROP TABLE IF EXISTS `basic_label_print_exception`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_print_exception` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `error_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误代码',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '错误消息',
  `print_record_id` bigint DEFAULT NULL COMMENT '打印记录 ID',
  `template_id` bigint DEFAULT NULL COMMENT '模板 ID',
  `factory_id` bigint DEFAULT NULL COMMENT '工厂 ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `exception_time` datetime DEFAULT NULL COMMENT '异常发生时间',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '堆栈信息',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_error_code` (`error_code`) USING BTREE,
  KEY `idx_exception_time` (`exception_time`) USING BTREE,
  KEY `idx_print_record_id` (`print_record_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='标签打印异常记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_print_exception`
--

LOCK TABLES `basic_label_print_exception` WRITE;
/*!40000 ALTER TABLE `basic_label_print_exception` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_label_print_exception` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_print_record`
--

DROP TABLE IF EXISTS `basic_label_print_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_print_record` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `print_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '打印流水号',
  `template_id` bigint NOT NULL COMMENT '使用的模板 ID',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码 (冗余)',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称 (冗余)',
  `template_version` int NOT NULL COMMENT '模板版本 (冗余，用于追溯)',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型 (冗余)',
  `print_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '打印类型：NORMAL=正常打印，REPRINT=补打',
  `barcode_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '条码号/序列号',
  `serial_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '序列号',
  `engineering_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '工程卡号',
  `lot_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'LOT 号',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '批次号',
  `material_id` bigint DEFAULT NULL COMMENT '物料 ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物料编码',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商编码',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '客户编码',
  `data_snapshot` json NOT NULL COMMENT '本次打印的数据快照 (用于追溯和补打)',
  `print_result_json` json NOT NULL COMMENT '本次实际返回给前端的打印模板内容 (填充后的完整 JSON)',
  `print_count` int NOT NULL DEFAULT '1' COMMENT '打印张数',
  `factory_id` bigint DEFAULT NULL COMMENT '工厂 ID',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人 ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '操作人姓名',
  `print_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打印时间',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_print_no` (`print_no`,`tenant_id`) USING BTREE,
  KEY `idx_template_id` (`template_id`) USING BTREE,
  KEY `idx_barcode_no` (`barcode_no`) USING BTREE,
  KEY `idx_serial_no` (`serial_no`) USING BTREE,
  KEY `idx_engineering_card_no` (`engineering_card_no`) USING BTREE,
  KEY `idx_lot_no` (`lot_no`) USING BTREE,
  KEY `idx_material_code` (`material_code`) USING BTREE,
  KEY `idx_print_time` (`print_time`) USING BTREE,
  KEY `idx_operator_id` (`operator_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='标签打印记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_print_record`
--

LOCK TABLES `basic_label_print_record` WRITE;
/*!40000 ALTER TABLE `basic_label_print_record` DISABLE KEYS */;
INSERT INTO `basic_label_print_record` VALUES (9001,'PRINT202604140001',5001,'TP_PRODUCT_001','产品默认标签',1,'PRODUCT','NORMAL','B202604140001',NULL,'ENG20260414001','L20260414001','B2026041401',NULL,'MATERIAL001',NULL,NULL,'{\"lot\": \"L20260414001\", \"material\": \"MATERIAL001\"}','{\"label\": \"filled data\"}',1,1001,10001,'操作员 A','2026-04-14 10:54:07',1,'正常打印','2026-04-14 10:54:07','2026-04-14 10:54:07',0),(9002,'PRINT202604140002',5002,'TP_INCOMING_001','来料默认标签',1,'INCOMING','NORMAL','B202604140002',NULL,'ENG20260414002','L20260414002','B2026041402',NULL,'MATERIAL002',NULL,NULL,'{\"material\": \"MATERIAL002\", \"supplier\": \"SUPP001\"}','{\"label\": \"filled data\"}',1,1001,10001,'操作员 A','2026-04-14 10:54:07',1,'正常打印','2026-04-14 10:54:07','2026-04-14 10:54:07',0);
/*!40000 ALTER TABLE `basic_label_print_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_template`
--

DROP TABLE IF EXISTS `basic_label_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_template` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `type_id` bigint DEFAULT NULL COMMENT '标签类型 ID',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型：MATERIAL=物料标签，PRODUCT=产品标签，LOT=LOT 标签，INCOMING=来料标签，SUPPLIER=供应商标签，CUSTOMER_MARK=客户唛头，CUSTOMER_LABEL=客户定制标签，WORKSTATION=工位标签，PERSONNEL=人员标签，EQUIPMENT=设备标签，PROCESS_STEP=工步标签，LOCATION=库位标签，SPQ_INNER=SPQ 内箱标签，PQ_OUTER=PQ 外箱标签，OVERSEAS_OUTER=海外外箱标签，SHIPPING_BOX=出货箱数标签，ENG_CARD_PACKAGE=工程卡包装标签',
  `template_version` int NOT NULL DEFAULT '1' COMMENT '模板版本号',
  `paper_width` int NOT NULL DEFAULT '100' COMMENT '纸张宽度，单位 mm',
  `paper_height` int NOT NULL DEFAULT '60' COMMENT '纸张高度，单位 mm',
  `paper_size` varchar(32) NOT NULL DEFAULT 'CUSTOM' COMMENT '纸张规格',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=否，1=是',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认模板：0-否，1-是',
  `template_content` json NOT NULL COMMENT '模板内容 JSON(包含版式信息、占位符配置等)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模板描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `factory_id` bigint DEFAULT NULL COMMENT '工厂 ID(NULL 表示全局模板)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_template_code_version` (`template_code`,`template_version`,`factory_id`,`deleted`) USING BTREE,
  KEY `idx_template_type` (`template_type`) USING BTREE,
  KEY `idx_is_default` (`is_default`) USING BTREE,
  KEY `idx_factory_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='标签模板主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_template`
--

LOCK TABLES `basic_label_template` WRITE;
/*!40000 ALTER TABLE `basic_label_template` DISABLE KEYS */;
INSERT INTO `basic_label_template` VALUES (1,'TP_PRODUCT_001','产品默认标签',NULL,'PRODUCT',1,100,60,'CUSTOM',1,1,'{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"MATERIAL_CODE\"}, {\"x\": 10, \"y\": 30, \"name\": \"MATERIAL_NAME\"}, {\"x\": 10, \"y\": 60, \"name\": \"BARCODE\"}, {\"x\": 10, \"y\": 120, \"name\": \"LOT_NO\"}]}','标准产品标签',1,NULL,-1,'2026-04-14 10:54:07','2026-04-15 10:36:50','admin','admin',0),(2,'TP_INCOMING_001','来料默认标签',NULL,'INCOMING',1,100,60,'CUSTOM',1,1,'{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"SUPPLIER_CODE\"}, {\"x\": 10, \"y\": 30, \"name\": \"MATERIAL_CODE\"}, {\"x\": 10, \"y\": 60, \"name\": \"BATCH_NO\"}, {\"x\": 10, \"y\": 90, \"name\": \"QUANTITY\"}]}','标准来料标签',1,NULL,-1,'2026-04-14 10:54:07','2026-04-15 10:36:51','admin','admin',0),(3,'TP_CUSTOMER_MARK_001','客户唛头标签',NULL,'CUSTOMER_MARK',1,100,60,'CUSTOM',1,1,'{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"CUSTOMER_NAME\"}, {\"x\": 10, \"y\": 40, \"name\": \"SHIP_TO\"}, {\"x\": 10, \"y\": 80, \"name\": \"BOX_NO\"}]}','客户外箱唛头',1,NULL,-1,'2026-04-14 10:54:07','2026-04-15 10:36:53','admin','admin',0),(4,'TP_ENG_PACKAGE_001','工程卡包装标签',NULL,'ENG_CARD_PACKAGE',1,100,60,'CUSTOM',1,1,'{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"ENG_CARD_NO\"}, {\"x\": 10, \"y\": 40, \"name\": \"MATERIAL_NAME\"}, {\"x\": 10, \"y\": 80, \"name\": \"PQ\"}, {\"x\": 10, \"y\": 120, \"name\": \"BOX_NO\"}]}','工程卡包装专用',1,NULL,-1,'2026-04-14 10:54:07','2026-04-15 10:36:55','admin','admin',0);
/*!40000 ALTER TABLE `basic_label_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_template_binding`
--

DROP TABLE IF EXISTS `basic_label_template_binding`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_template_binding` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `template_id` bigint NOT NULL COMMENT '模板 ID',
  `binding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定类型：MATERIAL=按物料，SUPPLIER=按供应商，CUSTOMER=按客户',
  `binding_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定值 (物料编码/供应商编码/客户编码)',
  `priority` int NOT NULL DEFAULT '0' COMMENT '优先级 (数字越大优先级越高)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `factory_id` bigint DEFAULT NULL COMMENT '工厂 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_binding_unique` (`template_id`,`binding_type`,`binding_value`,`tenant_id`,`deleted`) USING BTREE,
  KEY `idx_template_id` (`template_id`) USING BTREE,
  KEY `idx_binding_type_value` (`binding_type`,`binding_value`) USING BTREE,
  KEY `idx_priority` (`priority`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='标签模板绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_template_binding`
--

LOCK TABLES `basic_label_template_binding` WRITE;
/*!40000 ALTER TABLE `basic_label_template_binding` DISABLE KEYS */;
INSERT INTO `basic_label_template_binding` VALUES (1,1,'MATERIAL','MATERIAL001',10,-1,NULL,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0),(2,2,'SUPPLIER','SUPP001',10,-1,NULL,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0),(3,3,'CUSTOMER','CUST001',10,-1,NULL,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0),(10,4,'SUPPLIER','SUPP001',2,-1,NULL,'2026-04-15 11:18:54','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_label_template_binding` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_template_detail`
--

DROP TABLE IF EXISTS `basic_label_template_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_template_detail` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `template_id` bigint NOT NULL COMMENT '模板 ID',
  `component_type` varchar(32) NOT NULL COMMENT '组件类型',
  `position_x` int NOT NULL DEFAULT '0' COMMENT '组件 X 坐标，单位 mm',
  `position_y` int NOT NULL DEFAULT '0' COMMENT '组件 Y 坐标，单位 mm',
  `component_width` int NOT NULL DEFAULT '10' COMMENT '组件宽度，单位 mm',
  `component_height` int NOT NULL DEFAULT '6' COMMENT '组件高度，单位 mm',
  `component_content` text COMMENT '组件内容',
  `data_source` varchar(32) NOT NULL DEFAULT 'FIXED' COMMENT '数据来源：FIXED/FIELD',
  `field_code` varchar(100) DEFAULT NULL COMMENT '业务字段编码',
  `style_json` json DEFAULT NULL COMMENT '组件样式 JSON',
  `sort_no` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  KEY `idx_basic_label_template_detail_tpl` (`tenant_id`,`template_id`,`deleted`,`sort_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签模板组件详情表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_template_detail`
--

LOCK TABLES `basic_label_template_detail` WRITE;
/*!40000 ALTER TABLE `basic_label_template_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_label_template_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_label_type`
--

DROP TABLE IF EXISTS `basic_label_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_label_type` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `type_code` varchar(64) NOT NULL COMMENT '标签类型编码',
  `type_name` varchar(100) NOT NULL COMMENT '类型名称',
  `is_enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用：0=否，1=是',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_label_type_code` (`tenant_id`,`type_code`,`deleted`),
  KEY `idx_basic_label_type_tenant_enabled` (`tenant_id`,`is_enabled`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_label_type`
--

LOCK TABLES `basic_label_type` WRITE;
/*!40000 ALTER TABLE `basic_label_type` DISABLE KEYS */;
INSERT INTO `basic_label_type` VALUES (202605150001,0,'GENERAL','通用标签',1,'2026-05-15 18:39:13','20260515_label_template_refactor','2026-05-15 18:39:13','20260515_label_template_refactor',0);
/*!40000 ALTER TABLE `basic_label_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material`
--

DROP TABLE IF EXISTS `basic_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料编码 (租户内唯一)',
  `material_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料名称',
  `material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物料类型：RAW_MATERIAL=原材料，FINISHED_GOODS=成品，TOOL=工具，SEMI_FINISHED=半成品，OTHER=其它',
  `material_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '物料分类 (字典编码，支持多级分类)',
  `specification` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '计量单位',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '品牌',
  `image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '物料图片 URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '详细描述',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'NO_APPROVAL_REQUIRED' COMMENT '审批状态：无需审批，未审批，已审批，已驳回',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '型号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_material_code_tenant` (`material_code`,`tenant_id`,`deleted`) USING BTREE COMMENT '物料编码唯一索引',
  KEY `idx_material_name` (`material_name`) USING BTREE COMMENT '物料名称索引',
  KEY `idx_material_type` (`material_type`) USING BTREE COMMENT '物料类型索引',
  KEY `idx_material_category` (`material_category`) USING BTREE COMMENT '物料分类索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户 ID 索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='物料管理主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material`
--

LOCK TABLES `basic_material` WRITE;
/*!40000 ALTER TABLE `basic_material` DISABLE KEYS */;
INSERT INTO `basic_material` VALUES (7000000000000000001,'RAW_001','钢板 Q235B','RAW_MATERIAL','metal_sheet','10mm*1500mm*6000mm','张','宝钢',NULL,NULL,'常用原材料',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000002,'RAW_002','铜棒 H59','RAW_MATERIAL','metal_rod','直径20mm*3000mm','根','金田铜业',NULL,NULL,'高精度铜棒',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000003,'RAW_003','铝锭 ADC12','RAW_MATERIAL','metal_ingot','20kg/锭','kg','中铝',NULL,NULL,'压铸铝合金',1,'PENDING',1993479636925403138,'2026-04-28 13:12:09','2026-05-06 17:29:21','admin','1993479637244170242',0,NULL),(7000000000000000004,'SEMI_001','冲压外壳','SEMI_FINISHED','shell','450mm*300mm*50mm','个',NULL,NULL,NULL,'钣金冲压件',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000005,'SEMI_002','齿轮组件','SEMI_FINISHED','gear','模数2.5 齿数30','套',NULL,NULL,NULL,'精密齿轮',1,'NO_APPROVAL_REQUIRED',1993479636925403138,'2026-04-28 13:12:09','2026-05-06 17:29:21','admin','1993479637244170242',0,NULL),(7000000000000000006,'SEMI_003','电机定子','SEMI_FINISHED','motor_part','功率5.5KW','个',NULL,NULL,NULL,'电机核心部件',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000007,'PROD_001','减速机 XWD5','FINISHED_GOODS','reducer','速比29 功率4KW','台','国茂',NULL,NULL,'摆线针轮减速机',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000008,'PROD_002','离心泵 ISG50','FINISHED_GOODS','pump','流量12.5m3/h 扬程20m','台','南方泵业',NULL,NULL,'立式管道离心泵',1,'APPROVED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000009,'PROD_003','变频器 VFD-EL','FINISHED_GOODS','inverter','功率7.5KW 380V','台','台达',NULL,NULL,'小型高性能变频器',1,'REJECTED',1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0,NULL),(7000000000000000010,'OTH_001','润滑油','OTHER','consumable','4L/桶','桶','壳牌',NULL,NULL,'设备润滑用油',1,'NO_APPROVAL_REQUIRED',1993479636925403138,'2026-04-28 13:12:09','2026-05-06 17:29:21','admin','1993479637244170242',0,NULL);
/*!40000 ALTER TABLE `basic_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_extend`
--

DROP TABLE IF EXISTS `basic_material_extend`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_extend` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料 ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块标识 (如：PURCHASE=采购，SALE=销售，PRODUCTION=生产)',
  `extend_json` json NOT NULL COMMENT '扩展信息 JSON',
  `min_stock` decimal(18,4) DEFAULT NULL COMMENT '最低库存',
  `max_stock` decimal(18,4) DEFAULT NULL COMMENT '最高库存',
  `safety_stock` decimal(18,4) DEFAULT NULL COMMENT '安全库存',
  `valid_period_value` int DEFAULT NULL COMMENT '有效周期值',
  `valid_period_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '有效周期单位(YEAR=年, MONTH=月, DAY=日, HOUR=时)',
  `stagnant_period_value` int DEFAULT NULL COMMENT '呆滞周期值',
  `stagnant_period_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '呆滞周期单位(YEAR=年, MONTH=月, DAY=日, HOUR=时)',
  `packaging_type_id` bigint DEFAULT NULL COMMENT '包装方式ID(关联未来包装方式管理表)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_material_id` (`material_id`) USING BTREE COMMENT '物料 ID 索引',
  KEY `idx_module` (`module`) USING BTREE COMMENT '模块标识索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户 ID 索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='物料扩展信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_extend`
--

LOCK TABLES `basic_material_extend` WRITE;
/*!40000 ALTER TABLE `basic_material_extend` DISABLE KEYS */;
INSERT INTO `basic_material_extend` VALUES (8000000000000000001,7000000000000000001,'PURCHASE','{\"currency\": \"CNY\", \"supplier\": \"宝钢集团\", \"min_order_qty\": 10, \"payment_terms\": \"月结30天\", \"lead_time_days\": 7, \"purchase_price\": 4500.0}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0),(8000000000000000002,7000000000000000001,'INVENTORY','{\"location\": \"A-01-03\", \"warehouse\": \"原材料仓A\", \"safety_stock\": 50, \"current_stock\": 120, \"unit_weight_kg\": 78.5, \"shelf_life_days\": 0}',50.0000,500.0000,100.0000,12,'MONTH',90,'DAY',NULL,1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:14:41','admin','admin',0),(8000000000000000003,7000000000000000002,'PURCHASE','{\"currency\": \"CNY\", \"supplier\": \"金田铜业集团\", \"min_order_qty\": 100, \"payment_terms\": \"款到发货\", \"lead_time_days\": 5, \"purchase_price\": 52000.0}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1993479636925403138,'2026-04-28 13:12:09','2026-05-06 17:29:21','admin','1993479637244170242',0),(8000000000000000004,7000000000000000002,'INVENTORY','{\"location\": \"A-02-01\", \"warehouse\": \"原材料仓A\", \"safety_stock\": 200, \"current_stock\": 350, \"unit_weight_kg\": 2.8, \"shelf_life_days\": 0}',200.0000,1000.0000,300.0000,24,'MONTH',180,'DAY',NULL,1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:14:41','admin','admin',0),(8000000000000000005,7000000000000000007,'PRODUCTION','{\"bom_version\": \"V2.0\", \"process_route\": \"组装-测试-包装\", \"standard_cost\": 2800.0, \"cycle_time_min\": 45, \"quality_check_points\": [\"噪音测试\", \"温升测试\", \"密封测试\"]}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0),(8000000000000000006,7000000000000000007,'SALES','{\"currency\": \"CNY\", \"tax_rate\": 0.13, \"sale_price\": 3800.0, \"min_sale_price\": 3200.0, \"warranty_months\": 12}',NULL,NULL,NULL,NULL,NULL,NULL,NULL,NULL,1993479636925403138,'2026-04-28 13:12:09','2026-04-28 13:12:09','admin','admin',0);
/*!40000 ALTER TABLE `basic_material_extend` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_extend_config`
--

DROP TABLE IF EXISTS `basic_material_extend_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_extend_config` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `module` varchar(50) NOT NULL COMMENT '模块编码：PURCHASE=采购，INVENTORY=库存，PRODUCTION=生产，SALES=销售',
  `material_type` varchar(50) NOT NULL DEFAULT 'RAW_MATERIAL' COMMENT '物料类型',
  `field_name` varchar(100) NOT NULL COMMENT '字段名称',
  `field_label` varchar(100) NOT NULL COMMENT '字段标签',
  `field_type` varchar(50) NOT NULL DEFAULT 'STRING' COMMENT '字段类型',
  `field_options` json DEFAULT NULL COMMENT '字段选项 JSON',
  `required` tinyint NOT NULL DEFAULT '0' COMMENT '是否必填：0=否，1=是',
  `validation_rule` varchar(500) DEFAULT NULL COMMENT '校验规则',
  `default_value` varchar(500) DEFAULT NULL COMMENT '默认值',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_extend_config_field` (`tenant_id`,`module`,`material_type`,`field_name`,`deleted`),
  KEY `idx_material_extend_config_scope` (`tenant_id`,`module`,`material_type`,`status`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料扩展字段配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_extend_config`
--

LOCK TABLES `basic_material_extend_config` WRITE;
/*!40000 ALTER TABLE `basic_material_extend_config` DISABLE KEYS */;
INSERT INTO `basic_material_extend_config` VALUES (2026051219362100001,'INVENTORY','RAW_MATERIAL','current_stock','当前库存','NUMBER',NULL,0,NULL,NULL,10,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100002,'INVENTORY','RAW_MATERIAL','location','库位','STRING',NULL,0,NULL,NULL,20,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100003,'INVENTORY','RAW_MATERIAL','safety_stock','安全库存','NUMBER',NULL,0,NULL,NULL,30,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100004,'INVENTORY','RAW_MATERIAL','shelf_life_days','保质期(天)','NUMBER',NULL,0,NULL,NULL,40,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100005,'INVENTORY','RAW_MATERIAL','unit_weight_kg','单位重量(kg)','NUMBER',NULL,0,NULL,NULL,50,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100006,'INVENTORY','RAW_MATERIAL','warehouse','仓库','STRING',NULL,0,NULL,NULL,60,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100007,'PRODUCTION','FINISHED_GOODS','bom_version','BOM版本','STRING',NULL,0,NULL,NULL,10,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100008,'PRODUCTION','FINISHED_GOODS','cycle_time_min','节拍(分钟)','NUMBER',NULL,0,NULL,NULL,20,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100009,'PRODUCTION','FINISHED_GOODS','process_route','工艺路线','STRING',NULL,0,NULL,NULL,30,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100010,'PRODUCTION','FINISHED_GOODS','quality_check_points','质检点','MULTI_SELECT',NULL,0,NULL,NULL,40,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100011,'PRODUCTION','FINISHED_GOODS','standard_cost','标准成本','NUMBER',NULL,0,NULL,NULL,50,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100012,'PURCHASE','RAW_MATERIAL','currency','币种','STRING',NULL,0,NULL,NULL,10,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100013,'PURCHASE','RAW_MATERIAL','lead_time_days','采购提前期(天)','NUMBER',NULL,0,NULL,NULL,20,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100014,'PURCHASE','RAW_MATERIAL','min_order_qty','最小起订量','NUMBER',NULL,0,NULL,NULL,30,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100015,'PURCHASE','RAW_MATERIAL','payment_terms','付款条件','STRING',NULL,0,NULL,NULL,40,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100016,'PURCHASE','RAW_MATERIAL','purchase_price','采购价格','NUMBER',NULL,0,NULL,NULL,50,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100017,'PURCHASE','RAW_MATERIAL','supplier','默认供应商','STRING',NULL,0,NULL,NULL,60,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100018,'SALES','FINISHED_GOODS','currency','币种','STRING',NULL,0,NULL,NULL,10,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100019,'SALES','FINISHED_GOODS','min_sale_price','最低售价','NUMBER',NULL,0,NULL,NULL,20,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100020,'SALES','FINISHED_GOODS','sale_price','销售价格','NUMBER',NULL,0,NULL,NULL,30,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100021,'SALES','FINISHED_GOODS','tax_rate','税率','NUMBER',NULL,0,NULL,NULL,40,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0),(2026051219362100022,'SALES','FINISHED_GOODS','warranty_months','质保期(月)','NUMBER',NULL,0,NULL,NULL,50,'由历史物料附属 JSON 自动抽取',1,1993479636925403138,'2026-05-12 19:36:21','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','20260512_material_import_sync_upgrade',0);
/*!40000 ALTER TABLE `basic_material_extend_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_extend_schema`
--

DROP TABLE IF EXISTS `basic_material_extend_schema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_extend_schema` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `module` varchar(50) NOT NULL COMMENT '模块编码',
  `material_type` varchar(50) NOT NULL COMMENT '物料类型',
  `schema_json` json DEFAULT NULL COMMENT '字段结构 JSON',
  `version` int NOT NULL DEFAULT '1' COMMENT '结构版本号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_material_extend_schema_scope` (`tenant_id`,`module`,`material_type`,`deleted`),
  KEY `idx_material_extend_schema_scope` (`tenant_id`,`module`,`material_type`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='物料附属字段结构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_extend_schema`
--

LOCK TABLES `basic_material_extend_schema` WRITE;
/*!40000 ALTER TABLE `basic_material_extend_schema` DISABLE KEYS */;
INSERT INTO `basic_material_extend_schema` VALUES (2026051219362100001,1993479636925403138,'INVENTORY','RAW_MATERIAL','[{\"id\": 2026051219362100001, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 10, \"required\": 0, \"fieldName\": \"current_stock\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"当前库存\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100002, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 20, \"required\": 0, \"fieldName\": \"location\", \"fieldType\": \"STRING\", \"fieldLabel\": \"库位\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100003, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 30, \"required\": 0, \"fieldName\": \"safety_stock\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"安全库存\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100004, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 40, \"required\": 0, \"fieldName\": \"shelf_life_days\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"保质期(天)\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100005, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 50, \"required\": 0, \"fieldName\": \"unit_weight_kg\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"单位重量(kg)\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100006, \"module\": \"INVENTORY\", \"status\": 1, \"orderNum\": 60, \"required\": 0, \"fieldName\": \"warehouse\", \"fieldType\": \"STRING\", \"fieldLabel\": \"仓库\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}]',3,1,0,'由物料扩展字段配置自动初始化','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','2026-05-12 21:31:12','20260512_material_import_sync_upgrade',0),(2026051219362100002,1993479636925403138,'PRODUCTION','FINISHED_GOODS','[{\"id\": 2026051219362100007, \"module\": \"PRODUCTION\", \"status\": 1, \"orderNum\": 10, \"required\": 0, \"fieldName\": \"bom_version\", \"fieldType\": \"STRING\", \"fieldLabel\": \"BOM版本\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100008, \"module\": \"PRODUCTION\", \"status\": 1, \"orderNum\": 20, \"required\": 0, \"fieldName\": \"cycle_time_min\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"节拍(分钟)\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100009, \"module\": \"PRODUCTION\", \"status\": 1, \"orderNum\": 30, \"required\": 0, \"fieldName\": \"process_route\", \"fieldType\": \"STRING\", \"fieldLabel\": \"工艺路线\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100010, \"module\": \"PRODUCTION\", \"status\": 1, \"orderNum\": 40, \"required\": 0, \"fieldName\": \"quality_check_points\", \"fieldType\": \"MULTI_SELECT\", \"fieldLabel\": \"质检点\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100011, \"module\": \"PRODUCTION\", \"status\": 1, \"orderNum\": 50, \"required\": 0, \"fieldName\": \"standard_cost\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"标准成本\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}]',3,1,0,'由物料扩展字段配置自动初始化','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','2026-05-12 21:31:12','20260512_material_import_sync_upgrade',0),(2026051219362100003,1993479636925403138,'PURCHASE','RAW_MATERIAL','[{\"id\": 2026051219362100012, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 10, \"required\": 0, \"fieldName\": \"currency\", \"fieldType\": \"STRING\", \"fieldLabel\": \"币种\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100013, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 20, \"required\": 0, \"fieldName\": \"lead_time_days\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"采购提前期(天)\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100014, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 30, \"required\": 0, \"fieldName\": \"min_order_qty\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"最小起订量\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100015, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 40, \"required\": 0, \"fieldName\": \"payment_terms\", \"fieldType\": \"STRING\", \"fieldLabel\": \"付款条件\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100016, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 50, \"required\": 0, \"fieldName\": \"purchase_price\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"采购价格\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}, {\"id\": 2026051219362100017, \"module\": \"PURCHASE\", \"status\": 1, \"orderNum\": 60, \"required\": 0, \"fieldName\": \"supplier\", \"fieldType\": \"STRING\", \"fieldLabel\": \"默认供应商\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"RAW_MATERIAL\", \"validationRule\": null}]',3,1,0,'由物料扩展字段配置自动初始化','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','2026-05-12 21:31:12','20260512_material_import_sync_upgrade',0),(2026051219362100004,1993479636925403138,'SALES','FINISHED_GOODS','[{\"id\": 2026051219362100018, \"module\": \"SALES\", \"status\": 1, \"orderNum\": 10, \"required\": 0, \"fieldName\": \"currency\", \"fieldType\": \"STRING\", \"fieldLabel\": \"币种\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100019, \"module\": \"SALES\", \"status\": 1, \"orderNum\": 20, \"required\": 0, \"fieldName\": \"min_sale_price\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"最低售价\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100020, \"module\": \"SALES\", \"status\": 1, \"orderNum\": 30, \"required\": 0, \"fieldName\": \"sale_price\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"销售价格\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100021, \"module\": \"SALES\", \"status\": 1, \"orderNum\": 40, \"required\": 0, \"fieldName\": \"tax_rate\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"税率\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}, {\"id\": 2026051219362100022, \"module\": \"SALES\", \"status\": 1, \"orderNum\": 50, \"required\": 0, \"fieldName\": \"warranty_months\", \"fieldType\": \"NUMBER\", \"fieldLabel\": \"质保期(月)\", \"defaultValue\": null, \"fieldOptions\": null, \"materialType\": \"FINISHED_GOODS\", \"validationRule\": null}]',3,1,0,'由物料扩展字段配置自动初始化','2026-05-12 19:36:21','20260512_material_import_sync_upgrade','2026-05-12 21:31:12','20260512_material_import_sync_upgrade',0);
/*!40000 ALTER TABLE `basic_material_extend_schema` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_finished_goods`
--

DROP TABLE IF EXISTS `basic_material_finished_goods`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_finished_goods` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json DEFAULT NULL COMMENT '成品附属信息JSON(自定义字段配置)',
  `cost` decimal(18,4) DEFAULT NULL COMMENT '成本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_material_id_tenant` (`material_id`,`tenant_id`,`deleted`) USING BTREE COMMENT '物料ID唯一索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='成品附属信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_finished_goods`
--

LOCK TABLES `basic_material_finished_goods` WRITE;
/*!40000 ALTER TABLE `basic_material_finished_goods` DISABLE KEYS */;
INSERT INTO `basic_material_finished_goods` VALUES (9100000000000000001,7000000000000000007,'{\"ratio\": 29, \"weight_kg\": 85, \"product_model\": \"XWD5-29-4\", \"efficiency_pct\": 94, \"noise_level_db\": 65, \"rated_power_kw\": 4, \"input_speed_rpm\": 1440, \"rated_torque_nm\": 750, \"warranty_months\": 12, \"output_speed_rpm\": 49.7, \"protection_level\": \"IP54\", \"installation_type\": \"卧式\"}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0),(9100000000000000002,7000000000000000008,'{\"head_m\": 20, \"npsh_m\": 2.5, \"speed_rpm\": 2900, \"weight_kg\": 45, \"flow_rate_m3h\": 12.5, \"product_model\": \"ISG50-160\", \"efficiency_pct\": 68, \"rated_power_kw\": 2.2, \"connection_type\": \"法兰\", \"warranty_months\": 12, \"material_contact\": \"铸铁\", \"max_pressure_mpa\": 1.6}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0),(9100000000000000003,7000000000000000009,'{\"weight_kg\": 5.2, \"communication\": [\"Modbus\", \"RS485\"], \"dimensions_mm\": \"280*150*180\", \"input_voltage\": \"380V\", \"product_model\": \"VFD075EL43A\", \"efficiency_pct\": 96, \"output_voltage\": \"0-380V\", \"rated_power_kw\": 7.5, \"warranty_months\": 18, \"protection_level\": \"IP20\", \"frequency_range_hz\": \"0-400\"}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0);
/*!40000 ALTER TABLE `basic_material_finished_goods` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_raw_material`
--

DROP TABLE IF EXISTS `basic_material_raw_material`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_raw_material` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json DEFAULT NULL COMMENT '原材料附属信息JSON(自定义字段配置)',
  `current_active_price` decimal(18,4) DEFAULT NULL COMMENT '当前激活价格',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_material_id_tenant` (`material_id`,`tenant_id`,`deleted`) USING BTREE COMMENT '物料ID唯一索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='原材料附属信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_raw_material`
--

LOCK TABLES `basic_material_raw_material` WRITE;
/*!40000 ALTER TABLE `basic_material_raw_material` DISABLE KEYS */;
INSERT INTO `basic_material_raw_material` VALUES (9000000000000000001,7000000000000000001,'{\"standard\": \"GB/T 3274-2017\", \"hardness_hb\": 140, \"certification\": [\"材质证明书\", \"出厂合格证\"], \"density_kg_m3\": 7850, \"elongation_pct\": 26, \"material_grade\": \"Q235B\", \"melting_point_c\": 1538, \"surface_treatment\": \"热轧\", \"yield_strength_mpa\": 235, \"tensile_strength_mpa\": \"370-500\"}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0),(9000000000000000002,7000000000000000002,'{\"standard\": \"GB/T 4423-2017\", \"hardness_hv\": 120, \"certification\": [\"材质证明书\"], \"density_kg_m3\": 8400, \"elongation_pct\": 30, \"material_grade\": \"H59\", \"melting_point_c\": 900, \"surface_treatment\": \"拉丝\", \"copper_content_pct\": 59, \"yield_strength_mpa\": 220, \"tensile_strength_mpa\": 390}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-05-06 17:29:21','admin','1993479637244170242',0),(9000000000000000003,7000000000000000003,'{\"standard\": \"JIS H 5302\", \"hardness_hb\": 80, \"certification\": [\"材质证明书\", \"ROHS报告\"], \"density_kg_m3\": 2700, \"material_grade\": \"ADC12\", \"melting_point_c\": 595, \"surface_treatment\": \"铸造\", \"silicon_content_pct\": 9.5, \"aluminum_content_pct\": 85, \"tensile_strength_mpa\": 230}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_material_raw_material` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_material_semi_finished`
--

DROP TABLE IF EXISTS `basic_material_semi_finished`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_material_semi_finished` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json DEFAULT NULL COMMENT '半成品附属信息JSON(自定义字段配置)',
  `cost` decimal(18,4) DEFAULT NULL COMMENT '成本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_material_id_tenant` (`material_id`,`tenant_id`,`deleted`) USING BTREE COMMENT '物料ID唯一索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='半成品附属信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_material_semi_finished`
--

LOCK TABLES `basic_material_semi_finished` WRITE;
/*!40000 ALTER TABLE `basic_material_semi_finished` DISABLE KEYS */;
INSERT INTO `basic_material_semi_finished` VALUES (9200000000000000001,7000000000000000004,'{\"thickness_mm\": 1.5, \"process_steps\": [\"下料\", \"冲压成型\", \"去毛刺\", \"表面处理\"], \"material_source\": \"RAW_001\", \"processing_method\": \"冲压\", \"processing_time_min\": 15, \"quality_requirements\": [\"无裂纹\", \"无变形\", \"尺寸合格\"], \"surface_roughness_ra\": 3.2, \"dimensional_tolerance_mm\": 0.1}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0),(9200000000000000002,7000000000000000005,'{\"module\": 2.5, \"teeth_count\": 30, \"process_steps\": [\"车削\", \"滚齿\", \"热处理\", \"磨齿\"], \"accuracy_grade\": \"7级\", \"material_source\": \"RAW_002\", \"processing_method\": \"滚齿加工\", \"pressure_angle_deg\": 20, \"processing_time_min\": 30, \"quality_requirements\": [\"齿形合格\", \"齿向合格\", \"跳动合格\"], \"surface_hardness_hrc\": \"58-62\"}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-05-06 17:29:21','admin','1993479637244170242',0),(9200000000000000003,7000000000000000006,'{\"poles\": 4, \"power_kw\": 5.5, \"winding_type\": \"双层叠绕组\", \"process_steps\": [\"铁芯压装\", \"绕线\", \"嵌线\", \"绝缘处理\", \"浸漆烘干\"], \"turns_per_coil\": 28, \"material_source\": \"外部采购\", \"insulation_class\": \"F级\", \"wire_diameter_mm\": 1.25, \"processing_method\": \"绕线嵌线\", \"processing_time_min\": 60, \"quality_requirements\": [\"绝缘电阻合格\", \"匝间无短路\", \"直流电阻平衡\"]}',NULL,1993479636925403138,'2026-04-28 13:12:10','2026-04-28 13:12:10','admin','admin',0);
/*!40000 ALTER TABLE `basic_material_semi_finished` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_packaging_type`
--

DROP TABLE IF EXISTS `basic_packaging_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_packaging_type` (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `packaging_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包装方式编码',
  `packaging_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包装方式名称',
  `packaging_material` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '包装材料(纸箱/木箱/托盘/铁桶等)',
  `length_mm` decimal(10,2) DEFAULT NULL COMMENT '长度(mm)',
  `width_mm` decimal(10,2) DEFAULT NULL COMMENT '宽度(mm)',
  `height_mm` decimal(10,2) DEFAULT NULL COMMENT '高度(mm)',
  `weight_kg` decimal(10,2) DEFAULT NULL COMMENT '包装自重(kg)',
  `max_load_kg` decimal(10,2) DEFAULT NULL COMMENT '最大承重(kg)',
  `unit_cost` decimal(18,4) DEFAULT NULL COMMENT '单位成本',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` int DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_packaging_code_tenant` (`packaging_code`,`tenant_id`,`deleted`) USING BTREE COMMENT '包装方式编码唯一索引',
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE COMMENT '租户ID索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='包装方式表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_packaging_type`
--

LOCK TABLES `basic_packaging_type` WRITE;
/*!40000 ALTER TABLE `basic_packaging_type` DISABLE KEYS */;
INSERT INTO `basic_packaging_type` VALUES (5100000000000000001,'CTN-A','A型纸箱','carton',400.00,300.00,200.00,0.50,25.00,2.5000,1,1,'标准A型纸箱',1993479636925403138,'2026-04-28 13:38:43','2026-04-28 13:38:43','admin','admin',0),(5100000000000000002,'CTN-B','B型纸箱','carton',600.00,400.00,300.00,0.80,40.00,3.5000,1,2,'标准B型纸箱',1993479636925403138,'2026-04-28 13:38:43','2026-04-28 13:38:43','admin','admin',0),(5100000000000000003,'WDB-A','A型木箱','wooden_box',800.00,600.00,500.00,5.00,100.00,45.0000,1,3,'重型包装木箱',1993479636925403138,'2026-04-28 13:38:43','2026-04-28 13:38:43','admin','admin',0),(5100000000000000004,'PLT-A','标准托盘','pallet',1200.00,1000.00,150.00,15.00,1000.00,120.0000,1,4,'1200*1000标准托盘',1993479636925403138,'2026-04-28 13:38:43','2026-04-28 13:38:43','admin','admin',0),(5100000000000000005,'DRM-200L','200L铁桶','iron_drum',580.00,580.00,880.00,20.00,200.00,85.0000,1,5,'200升标准铁桶',1993479636925403138,'2026-04-28 13:38:43','2026-04-28 13:38:43','admin','admin',0),(5100000000000000006,'BAG-50','50kg塑料袋','plastic_bag',600.00,400.00,10.00,0.10,50.00,0.5000,1,6,'50kg承重塑料袋',1993479636925403138,'2026-04-28 13:38:43','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_packaging_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_supplier`
--

DROP TABLE IF EXISTS `basic_supplier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_supplier` (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编码',
  `supplier_full_name` varchar(200) DEFAULT NULL COMMENT '?????',
  `supplier_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `supplier_short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商简称',
  `logo_url` varchar(1000) DEFAULT NULL COMMENT '供应商 Logo 图片访问地址',
  `english_name` varchar(200) DEFAULT NULL COMMENT '???',
  `current_address` varchar(500) DEFAULT NULL COMMENT '???',
  `primary_contact` varchar(100) DEFAULT NULL COMMENT '????',
  `supplier_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '供应商类型：RAW_MATERIAL=原材料，PACKAGING=包装，LOGISTICS=物流，SERVICE=服务',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '国家/地区',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '详细地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `cooperation_status` varchar(32) DEFAULT NULL COMMENT '?????1-?? 2-?? 3-?? 4-??',
  `credit_level` varchar(32) DEFAULT NULL COMMENT '?????A/B/C/D',
  `risk_level` varchar(32) DEFAULT NULL COMMENT '????',
  `supplier_level` varchar(32) DEFAULT NULL COMMENT '??????1-?? 2-?? 3-??',
  `related_tenant_code` varchar(100) DEFAULT NULL COMMENT '?????????',
  `review_status` tinyint NOT NULL DEFAULT '1' COMMENT '?????0-???? 1-??? 2-??? 3-???',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系邮箱',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '税号',
  `bank_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '银行账号',
  `payment_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '付款条件',
  `quality_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '质量等级：A/B/C/D',
  `certification` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '认证资质 (多个用逗号分隔)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_supplier_code_tenant` (`supplier_code`,`tenant_id`,`deleted`) USING BTREE,
  UNIQUE KEY `uk_basic_supplier_code` (`supplier_code`),
  KEY `idx_supplier_name` (`supplier_name`) USING BTREE,
  KEY `idx_basic_supplier_status` (`cooperation_status`,`review_status`),
  KEY `idx_basic_supplier_tenant_code` (`related_tenant_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='供应商信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_supplier`
--

LOCK TABLES `basic_supplier` WRITE;
/*!40000 ALTER TABLE `basic_supplier` DISABLE KEYS */;
INSERT INTO `basic_supplier` VALUES (3001,'SUPP001','富士康科技集团','富士康科技集团','富士康',NULL,NULL,NULL,'供应商对接人','RAW_MATERIAL','中国','广东省','深圳市',NULL,'供应商对接人','13700000000',NULL,NULL,NULL,NULL,'sup_supp001',1,NULL,NULL,NULL,NULL,'A',NULL,1,NULL,-1,'2026-04-14 10:54:07','2026-04-26 16:26:05','admin','1993479637244170242',0),(3002,'SUPP002','比亚迪股份有限公司','比亚迪股份有限公司','比亚迪',NULL,NULL,NULL,'供应商对接人','PACKAGING','中国','广东省','深圳市',NULL,'供应商对接人','13700000001',NULL,NULL,NULL,NULL,NULL,1,NULL,NULL,NULL,NULL,'A',NULL,1,NULL,1,'2026-04-14 10:54:07','2026-05-06 17:29:21','admin','1993479637244170242',0);
/*!40000 ALTER TABLE `basic_supplier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_supplier_contact`
--

DROP TABLE IF EXISTS `basic_supplier_contact`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_supplier_contact` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `contact_name` varchar(100) DEFAULT NULL COMMENT '?????',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '?????',
  `contact_position` varchar(100) DEFAULT NULL COMMENT '?????',
  `contact_email` varchar(128) DEFAULT NULL COMMENT '?????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`),
  KEY `idx_basic_supplier_contact_supplier` (`supplier_id`,`deleted`),
  KEY `idx_basic_supplier_contact_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='?????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_supplier_contact`
--

LOCK TABLES `basic_supplier_contact` WRITE;
/*!40000 ALTER TABLE `basic_supplier_contact` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_supplier_contact` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_supplier_detail`
--

DROP TABLE IF EXISTS `basic_supplier_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_supplier_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `legal_representative` varchar(100) DEFAULT NULL COMMENT '????',
  `registered_capital` decimal(18,2) DEFAULT NULL COMMENT '????',
  `establishment_date` date DEFAULT NULL COMMENT '????',
  `enterprise_nature` varchar(32) DEFAULT NULL COMMENT '?????1-?? 2-?? 3-?? 4-??',
  `industry_category` varchar(64) DEFAULT NULL COMMENT '????????supplier_industry_category',
  `registered_address` varchar(500) DEFAULT NULL COMMENT '????',
  `business_address` varchar(500) DEFAULT NULL COMMENT '????',
  `email` varchar(128) DEFAULT NULL COMMENT '??',
  `tax_number` varchar(100) DEFAULT NULL COMMENT '??',
  `bank_name` varchar(200) DEFAULT NULL COMMENT '????',
  `bank_account` varchar(512) DEFAULT NULL COMMENT '??????',
  `invoice_type` varchar(32) DEFAULT NULL COMMENT '????????supplier_invoice_type',
  `default_tax_rate` decimal(5,2) DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`),
  KEY `idx_basic_supplier_detail_supplier` (`supplier_id`,`deleted`),
  KEY `idx_basic_supplier_detail_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_supplier_detail`
--

LOCK TABLES `basic_supplier_detail` WRITE;
/*!40000 ALTER TABLE `basic_supplier_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_supplier_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_supplier_qualification`
--

DROP TABLE IF EXISTS `basic_supplier_qualification`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_supplier_qualification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `qualification_type` varchar(64) DEFAULT NULL COMMENT '????????supplier_qualification_type',
  `certificate_no` varchar(100) DEFAULT NULL COMMENT '????',
  `issue_date` date DEFAULT NULL COMMENT '????',
  `expire_date` date DEFAULT NULL COMMENT '????',
  `attachment` varchar(1000) DEFAULT NULL COMMENT '????? URL',
  `valid` tinyint(1) NOT NULL DEFAULT '1' COMMENT '?????0-??1-?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`),
  KEY `idx_basic_supplier_qualification_supplier` (`supplier_id`,`deleted`),
  KEY `idx_basic_supplier_qualification_expire` (`expire_date`),
  KEY `idx_basic_supplier_qualification_tenant_id` (`tenant_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_supplier_qualification`
--

LOCK TABLES `basic_supplier_qualification` WRITE;
/*!40000 ALTER TABLE `basic_supplier_qualification` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_supplier_qualification` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_unit`
--

DROP TABLE IF EXISTS `basic_unit`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_unit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `unit_code` varchar(64) NOT NULL COMMENT '单位编码',
  `unit_name` varchar(100) NOT NULL COMMENT '单位名称',
  `unit_symbol` varchar(32) DEFAULT NULL COMMENT '单位符号',
  `unit_category` varchar(64) DEFAULT NULL COMMENT '单位分类',
  `conversion_rate` decimal(18,6) NOT NULL DEFAULT '1.000000' COMMENT '换算比率',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `unit_type_id` bigint NOT NULL COMMENT '计量单位类型 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_unit_tenant_type_code_deleted` (`tenant_id`,`unit_type_id`,`unit_code`,`deleted`),
  KEY `idx_basic_unit_tenant_status` (`tenant_id`,`status`,`deleted`),
  KEY `idx_basic_unit_sort` (`sort_order`),
  KEY `idx_basic_unit_type` (`tenant_id`,`unit_type_id`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_unit`
--

LOCK TABLES `basic_unit` WRITE;
/*!40000 ALTER TABLE `basic_unit` DISABLE KEYS */;
INSERT INTO `basic_unit` VALUES (1,'PCS','个','pcs','数量',1.000000,1,10,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(2,'SET','套','set','数量',1.000000,1,20,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(3,'BOX','箱','box','包装',1.000000,1,30,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(4,'KG','千克','kg','重量',1.000000,1,40,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(5,'G','克','g','重量',0.001000,1,50,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(6,'M','米','m','长度',1.000000,1,60,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(7,'CM','厘米','cm','长度',0.010000,1,70,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(8,'MM','毫米','mm','长度',0.001000,1,80,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(9,'L','升','L','体积',1.000000,1,90,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0),(10,'ML','毫升','ml','体积',0.001000,1,100,'系统初始化',1993479636925403138,101882303365513228,'2026-04-30 10:22:49','20260430_fix_pages','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0);
/*!40000 ALTER TABLE `basic_unit` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_unit_conversion`
--

DROP TABLE IF EXISTS `basic_unit_conversion`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_unit_conversion` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `unit_id` bigint NOT NULL COMMENT '源计量单位 ID',
  `target_unit_id` bigint NOT NULL COMMENT '目标计量单位 ID',
  `conversion_value` decimal(30,12) NOT NULL COMMENT '转换后数值（1:x）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_unit_conversion_tenant_pair_deleted` (`tenant_id`,`unit_id`,`target_unit_id`,`deleted`),
  KEY `idx_basic_unit_conversion_unit` (`tenant_id`,`unit_id`,`deleted`),
  KEY `idx_basic_unit_conversion_target` (`tenant_id`,`target_unit_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位换算关系表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_unit_conversion`
--

LOCK TABLES `basic_unit_conversion` WRITE;
/*!40000 ALTER TABLE `basic_unit_conversion` DISABLE KEYS */;
/*!40000 ALTER TABLE `basic_unit_conversion` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `basic_unit_type`
--

DROP TABLE IF EXISTS `basic_unit_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `basic_unit_type` (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户 ID',
  `unit_type_code` varchar(50) NOT NULL COMMENT '计量单位类型编码',
  `unit_type_name` varchar(100) NOT NULL COMMENT '计量单位类型',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父 ID',
  `level_path` varchar(500) DEFAULT NULL COMMENT '层级路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_basic_unit_type_tenant_code_deleted` (`tenant_id`,`unit_type_code`,`deleted`),
  KEY `idx_basic_unit_type_parent` (`tenant_id`,`parent_id`,`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='计量单位类型表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `basic_unit_type`
--

LOCK TABLES `basic_unit_type` WRITE;
/*!40000 ALTER TABLE `basic_unit_type` DISABLE KEYS */;
INSERT INTO `basic_unit_type` VALUES (101882303365513228,1993479636925403138,'DEFAULT','默认类型',0,'0/101882303365513228.000000000000000000000000000000','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade','2026-05-02 21:32:23','20260502_basic_unit_and_table_upgrade',0);
/*!40000 ALTER TABLE `basic_unit_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_android_version`
--

DROP TABLE IF EXISTS `sys_android_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_android_version` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `version_code` int NOT NULL COMMENT '版本号(内部递增)',
  `version_name` varchar(50) NOT NULL COMMENT '版本名称',
  `changelog` text COMMENT '更新日志',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '文件访问URL',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小(字节)',
  `storage_type` varchar(32) NOT NULL DEFAULT 'LOCAL' COMMENT '存储类型',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1启用,0禁用)',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_android_version_status` (`status`) USING BTREE,
  KEY `idx_sys_android_version_tenant` (`tenant_id`) USING BTREE,
  KEY `idx_sys_android_version_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='安卓版本管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_android_version`
--

LOCK TABLES `sys_android_version` WRITE;
/*!40000 ALTER TABLE `sys_android_version` DISABLE KEYS */;
INSERT INTO `sys_android_version` VALUES (1,1,'1.0.0','初始化安卓安装包','app-dev-debug.apk','http://192.168.121.1:9000/api/sys/files/3ea255bc5a3b497e8fb59e7206eaa6cd.apk',66423128,'LOCAL',1,1993479636925403138,'2026-05-07 10:36:22','1993479637244170242','2026-05-07 10:36:22','1993479637244170242',0),(2,1,'1.0.0','','3ea255bc5a3b497e8fb59e7206eaa6cd.apk','http://192.168.121.1:9000/api/sys/files/81deda3768654a059e1015715e821a1d.apk',66423128,'LOCAL',1,1993479636925403138,'2026-05-07 22:25:24','1993479637244170242','2026-05-12 19:37:22','1993479637244170242',1),(3,1,'1.0.0','','3ea255bc5a3b497e8fb59e7206eaa6cd.apk','http://192.168.121.1:9000/api/sys/files/69bc71b5ad7a4d229604ac7e029c45f8.apk',66423128,'LOCAL',1,1993479636925403138,'2026-05-07 22:26:14','1993479637244170242','2026-05-12 19:37:20','1993479637244170242',1);
/*!40000 ALTER TABLE `sys_android_version` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_android_version_upload_task`
--

DROP TABLE IF EXISTS `sys_android_version_upload_task`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_android_version_upload_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '??ID',
  `upload_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '????ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '?????',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '????????',
  `chunk_size` bigint NOT NULL DEFAULT '0' COMMENT '???????',
  `total_chunks` int NOT NULL DEFAULT '0' COMMENT '????',
  `uploaded_chunks` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '???????',
  `uploaded_count` int NOT NULL DEFAULT '0' COMMENT '??????',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'UPLOADING' COMMENT '????',
  `file_hash` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '??SHA-256',
  `temp_dir` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '????',
  `merged_file_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '?????????',
  `final_file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????URL',
  `version_id` bigint DEFAULT NULL COMMENT '??????ID',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `version_code` int DEFAULT NULL COMMENT '???',
  `version_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '????',
  `changelog` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '????',
  `expire_time` datetime DEFAULT NULL COMMENT '????',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '??ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '???',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '????',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_upload_id` (`upload_id`) USING BTREE,
  KEY `idx_file_hash` (`file_hash`) USING BTREE,
  KEY `idx_status_expire` (`status`,`expire_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='??????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_android_version_upload_task`
--

LOCK TABLES `sys_android_version_upload_task` WRITE;
/*!40000 ALTER TABLE `sys_android_version_upload_task` DISABLE KEYS */;
INSERT INTO `sys_android_version_upload_task` VALUES (1,'2e745256f91a46edae20f0c44fb20a9f','app-dev-debug.apk',66423128,8388608,8,'0,1,2,3,4,5,6,7',8,'COMPLETED','31d53d40b9b8234ccfb8d7698762b36316dce439910ccfca20bdbf94ab07847b','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\2e745256f91a46edae20f0c44fb20a9f','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\2e745256f91a46edae20f0c44fb20a9f\\app-dev-debug.apk','http://192.168.121.1:9000/api/sys/files/3ea255bc5a3b497e8fb59e7206eaa6cd.apk',1,NULL,1,'1.0.0','初始化安卓安装包','2026-05-08 10:31:31',1993479636925403138,'2026-05-07 10:31:30','2026-05-07 10:31:30','1993479637244170242','1993479637244170242',0),(2,'76e77bdb4d0741feacf12285bb0220ea','3ea255bc5a3b497e8fb59e7206eaa6cd.apk',66423128,8388608,8,'0,1,2,3,4,5,6,7',8,'COMPLETED','31d53d40b9b8234ccfb8d7698762b36316dce439910ccfca20bdbf94ab07847b','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\76e77bdb4d0741feacf12285bb0220ea','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\76e77bdb4d0741feacf12285bb0220ea\\3ea255bc5a3b497e8fb59e7206eaa6cd.apk','http://192.168.121.1:9000/api/sys/files/81deda3768654a059e1015715e821a1d.apk',2,NULL,1,'1.0.0','','2026-05-08 22:25:23',1993479636925403138,'2026-05-07 22:25:20','2026-05-07 22:25:20','1993479637244170242','1993479637244170242',0),(3,'1e0fdf2cd5124f28bf26e4c23a145657','3ea255bc5a3b497e8fb59e7206eaa6cd.apk',66423128,8388608,8,'0,1,2,3,4,5,6,7',8,'COMPLETED','31d53d40b9b8234ccfb8d7698762b36316dce439910ccfca20bdbf94ab07847b','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\1e0fdf2cd5124f28bf26e4c23a145657','D:\\mine_product\\forgex\\forgex\\data\\tmp\\android-version\\1e0fdf2cd5124f28bf26e4c23a145657\\3ea255bc5a3b497e8fb59e7206eaa6cd.apk','http://192.168.121.1:9000/api/sys/files/69bc71b5ad7a4d229604ac7e029c45f8.apk',3,NULL,1,'1.0.0','','2026-05-08 22:26:13',1993479636925403138,'2026-05-07 22:26:10','2026-05-07 22:26:10','1993479637244170242','1993479637244170242',0);
/*!40000 ALTER TABLE `sys_android_version_upload_task` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_c_menu`
--

DROP TABLE IF EXISTS `sys_c_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_c_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  `module_id` bigint DEFAULT NULL COMMENT '所属模块 ID（关联 sys_module）',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单 ID，顶级为 0',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'menu' COMMENT '菜单类型：catalog=目录, menu=菜单, button=按钮',
  `menu_level` int NOT NULL DEFAULT '1' COMMENT '菜单层级：1=一级, 2=二级, 3=三级',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `name_i18n_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '国际化名称 JSON',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `component_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '组件键（原生端页面映射标识）',
  `perm_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '权限键',
  `menu_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'embedded' COMMENT '菜单模式：embedded=内嵌, external=外联',
  `external_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '外联 URL',
  `order_num` int DEFAULT '0' COMMENT '排序号（越小越靠前）',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可见：0=隐藏, 1=显示',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：0=禁用, 1=启用',
  `tenant_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PUBLIC' COMMENT '适用租户类型：MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC',
  `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ALL' COMMENT '设备类型：MOBILE=手机, TABLET=Pad, ALL=通用',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0=未删除, 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_module_id` (`module_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_type` (`type`) USING BTREE,
  KEY `idx_device_type` (`device_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='C 端菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_c_menu`
--

LOCK TABLES `sys_c_menu` WRITE;
/*!40000 ALTER TABLE `sys_c_menu` DISABLE KEYS */;
INSERT INTO `sys_c_menu` VALUES (1,NULL,NULL,0,'catalog',1,'/workbench','工作台','{\"en-US\": \"/workbench\", \"ja-JP\": \"工作台\", \"ko-KR\": \"/workbench\", \"zh-CN\": \"工作台\", \"zh-TW\": \"工作台\"}','dashboard','WorkbenchScreen','c:workbench:view','embedded',NULL,1,1,1,'PUBLIC','ALL','2026-04-11 21:30:46','system','2026-04-26 20:56:49','20260426_i18n_fix',0),(2,NULL,NULL,0,'catalog',1,'/workflow','审批中心','{\"en-US\": \"/workflow\", \"ja-JP\": \"审批中心\", \"ko-KR\": \"/workflow\", \"zh-CN\": \"审批中心\", \"zh-TW\": \"审批中心\"}','approval','WorkflowScreen','c:workflow:view','embedded',NULL,2,1,1,'PUBLIC','ALL','2026-04-11 21:30:46','system','2026-04-26 20:56:49','20260426_i18n_fix',0),(3,NULL,NULL,0,'catalog',1,'/message','消息中心','{\"en-US\": \"/message\", \"ja-JP\": \"消息中心\", \"ko-KR\": \"/message\", \"zh-CN\": \"消息中心\", \"zh-TW\": \"消息中心\"}','message','MessageScreen','c:message:view','embedded',NULL,3,1,1,'PUBLIC','ALL','2026-04-11 21:30:46','system','2026-04-26 20:56:49','20260426_i18n_fix',0),(4,NULL,NULL,0,'catalog',1,'/profile','我的','{\"en-US\": \"/profile\", \"ja-JP\": \"我的\", \"ko-KR\": \"/profile\", \"zh-CN\": \"我的\", \"zh-TW\": \"我的\"}','person','ProfileScreen','c:profile:view','embedded',NULL,4,1,1,'PUBLIC','ALL','2026-04-11 21:30:46','system','2026-04-26 20:56:49','20260426_i18n_fix',0),(5,1993479636925403138,5,0,'catalog',1,'basic','基础信息','{\"en-US\": \"Basic Information\", \"ja-JP\": \"基礎情報\", \"ko-KR\": \"기본 정보\", \"zh-CN\": \"基础信息\", \"zh-TW\": \"基礎信息\"}','DatabaseOutlined','BasicModule','c:basic:view','embedded',NULL,10,1,1,'PUBLIC','ALL','2026-04-23 10:47:10','system','2026-04-23 10:56:03','codex',0),(6,1993479636925403138,5,5,'menu',2,'basic/info-test','基础信息测试页','{\"en-US\": \"Basic Info Test Page\", \"ja-JP\": \"基礎情報テストページ\", \"ko-KR\": \"기본 정보 테스트 페이지\", \"zh-CN\": \"基础信息测试页\", \"zh-TW\": \"基礎信息測試頁\"}','ExperimentOutlined','BasicInfoTestScreen','c:basic:test:view','embedded',NULL,10,1,1,'PUBLIC','ALL','2026-04-23 10:47:10','system','2026-04-23 10:56:03','codex',0);
/*!40000 ALTER TABLE `sys_c_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_codegen_config`
--

DROP TABLE IF EXISTS `sys_codegen_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_codegen_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `config_name` varchar(128) NOT NULL COMMENT '配置名称',
  `datasource_id` bigint NOT NULL COMMENT '数据源ID',
  `datasource_code` varchar(64) DEFAULT NULL COMMENT '数据源编码',
  `schema_name` varchar(128) NOT NULL COMMENT '数据库/schema名称',
  `page_type` varchar(32) NOT NULL COMMENT '页面类型',
  `main_table_name` varchar(128) NOT NULL COMMENT '主表名称',
  `sub_table_name` varchar(128) DEFAULT NULL COMMENT '子表名称',
  `main_pk_column` varchar(128) DEFAULT NULL COMMENT '主表主键字段',
  `sub_fk_column` varchar(128) DEFAULT NULL COMMENT '子表外键字段',
  `sub_pk_column` varchar(128) DEFAULT NULL COMMENT '子表主键字段',
  `module_name` varchar(64) NOT NULL COMMENT '模块编码',
  `biz_name` varchar(64) NOT NULL COMMENT '业务编码',
  `entity_name` varchar(128) NOT NULL COMMENT '主实体名称',
  `sub_entity_name` varchar(128) DEFAULT NULL COMMENT '子实体名称',
  `package_name` varchar(255) NOT NULL COMMENT '包名',
  `author` varchar(128) NOT NULL COMMENT '作者',
  `menu_name` varchar(128) NOT NULL COMMENT '菜单名称',
  `menu_icon` varchar(128) DEFAULT NULL COMMENT '菜单图标',
  `parent_menu_path` varchar(255) DEFAULT NULL COMMENT '父级菜单路径',
  `table_code_prefix` varchar(64) DEFAULT NULL COMMENT '表格编码前缀',
  `generate_items_json` json DEFAULT NULL COMMENT '生成项JSON',
  `config_json` json NOT NULL COMMENT '完整配置JSON',
  `last_generate_time` datetime DEFAULT NULL COMMENT '最近生成时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_codegen_config_datasource` (`datasource_id`) USING BTREE,
  KEY `idx_codegen_config_page_type` (`page_type`) USING BTREE,
  KEY `idx_codegen_config_module_biz` (`module_name`,`biz_name`) USING BTREE,
  KEY `idx_codegen_config_main_table` (`main_table_name`) USING BTREE,
  KEY `idx_codegen_config_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代码生成配置记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_codegen_config`
--

LOCK TABLES `sys_codegen_config` WRITE;
/*!40000 ALTER TABLE `sys_codegen_config` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_codegen_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_codegen_datasource`
--

DROP TABLE IF EXISTS `sys_codegen_datasource`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_codegen_datasource` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `datasource_code` varchar(64) NOT NULL COMMENT '数据源编码',
  `datasource_name` varchar(128) NOT NULL COMMENT '数据源名称',
  `db_type` varchar(32) NOT NULL COMMENT '数据库类型',
  `jdbc_url` varchar(500) NOT NULL COMMENT 'JDBC连接地址',
  `username` varchar(128) NOT NULL COMMENT '用户名',
  `password` varchar(255) DEFAULT NULL COMMENT '密码',
  `schema_name` varchar(128) DEFAULT NULL COMMENT '默认schema/catalog',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_codegen_datasource_code` (`datasource_code`) USING BTREE,
  KEY `idx_codegen_datasource_enabled` (`enabled`) USING BTREE,
  KEY `idx_codegen_datasource_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='代码生成数据源';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_codegen_datasource`
--

LOCK TABLES `sys_codegen_datasource` WRITE;
/*!40000 ALTER TABLE `sys_codegen_datasource` DISABLE KEYS */;
INSERT INTO `sys_codegen_datasource` VALUES (1,1993479636925403138,'forgex_admin','Forgex Admin','mysql','jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_admin',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(2,1993479636925403138,'forgex_common','Forgex Common','mysql','jdbc:mysql://127.0.0.1:3306/forgex_common?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_common',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(3,1993479636925403138,'forgex_history','Forgex History','mysql','jdbc:mysql://127.0.0.1:3306/forgex_history?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_history',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(4,1993479636925403138,'forgex_integration','Forgex Integration','mysql','jdbc:mysql://127.0.0.1:3306/forgex_integration?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_integration',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(5,1993479636925403138,'forgex_job','Forgex Job','mysql','jdbc:mysql://127.0.0.1:3306/forgex_job?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_job',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(6,1993479636925403138,'forgex_scada','Forgex Scada','mysql','jdbc:mysql://127.0.0.1:3306/forgex_scada?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_scada',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0),(7,1993479636925403138,'forgex_workflow','Forgex Workflow','mysql','jdbc:mysql://127.0.0.1:3306/forgex_workflow?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true','root','123456','forgex_workflow',1,'?????','2026-04-24 14:41:32','system','2026-04-30 10:52:16','20260430_fix_pages',0);
/*!40000 ALTER TABLE `sys_codegen_datasource` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_department`
--

DROP TABLE IF EXISTS `sys_department`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `org_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组',
  `org_level` int NOT NULL COMMENT '组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门编码',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '部门负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `order_num` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dept_code` (`dept_code`,`tenant_id`) USING BTREE,
  KEY `idx_parent_id` (`parent_id`) USING BTREE,
  KEY `idx_org_type` (`org_type`) USING BTREE,
  KEY `idx_org_level` (`org_level`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='部门表（支持树状组织架构）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_department`
--

LOCK TABLES `sys_department` WRITE;
/*!40000 ALTER TABLE `sys_department` DISABLE KEYS */;
INSERT INTO `sys_department` VALUES (1,0,'group',1,'Forgex集团','GROUP_FORGEX',NULL,NULL,NULL,1,1,'2026-01-07 14:41:43','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(2,1,'company',2,'Forgex制造有限公司','COMPANY_MFG',NULL,NULL,NULL,1,1,'2026-01-07 14:41:43','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(3,1,'company',2,'Forgex科技有限公司','COMPANY_TECH',NULL,NULL,NULL,2,1,'2026-01-07 14:41:43','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(4,2,'subsidiary',3,'华东分公司','SUB_EAST',NULL,NULL,NULL,1,1,'2026-01-07 14:41:43','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(5,2,'subsidiary',3,'华南分公司','SUB_SOUTH',NULL,NULL,NULL,2,1,'2026-01-07 14:41:43','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(6,2,'department',4,'总经办','DEPT_GM',NULL,NULL,NULL,1,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(7,2,'department',4,'技术部','DEPT_TECH',NULL,NULL,NULL,2,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(8,2,'department',4,'生产部','DEPT_PROD',NULL,NULL,NULL,3,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(9,2,'department',4,'质量部','DEPT_QC',NULL,NULL,NULL,4,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(10,2,'department',4,'采购部','DEPT_PUR',NULL,NULL,NULL,5,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(11,2,'department',4,'销售部','DEPT_SALES',NULL,NULL,NULL,6,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(12,2,'department',4,'财务部','DEPT_FIN',NULL,NULL,NULL,7,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(13,2,'department',4,'人力资源部','DEPT_HR',NULL,NULL,NULL,8,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(14,8,'team',5,'生产一班','TEAM_PROD_1',NULL,NULL,NULL,1,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(15,8,'team',5,'生产二班','TEAM_PROD_2',NULL,NULL,NULL,2,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(16,9,'team',5,'质检一组','TEAM_QC_1',NULL,NULL,NULL,1,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(17,9,'team',5,'质检二组','TEAM_QC_2',NULL,NULL,NULL,2,1,'2026-01-07 14:41:44','2026-05-06 17:29:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(18,0,'department',1,'?????','PLATFORM_DEPT',NULL,NULL,NULL,1,1,'2026-04-02 14:46:48','2026-04-05 11:26:48','system','1993479637244170242',1993479636925403138,1),(19,2,'department',1,'测试','TEST',NULL,NULL,NULL,0,1,'2026-04-04 14:34:21','2026-04-04 14:34:21','1993479637244170242','1993479637244170242',1993479636925403138,0),(20,19,'company',1,'测试1','TEST1',NULL,NULL,NULL,0,1,'2026-04-04 14:36:05','2026-04-11 09:25:05','1993479637244170242','1993479637244170242',1993479636925403138,1);
/*!40000 ALTER TABLE `sys_department` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_dict`
--

DROP TABLE IF EXISTS `sys_dict`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_dict` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父节点ID（0表示根节点）',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称(兼容字段)',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `module_id` bigint DEFAULT NULL COMMENT 'module id',
  `dict_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '字典键',
  `dict_value_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '字典显示值国际化(JSON)',
  `node_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点路径(dict_code路径，用/分割)',
  `level` int NOT NULL DEFAULT '1' COMMENT '层级',
  `children_count` int NOT NULL DEFAULT '0' COMMENT '直接子节点数量',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态：1启用，0禁用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '删除标记：0未删除，1已删除',
  `tag_style_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '标签样式配置JSON，用于字典值标签的样式配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_dict_sibling_code` (`tenant_id`,`parent_id`,`dict_code`,`deleted`) USING BTREE,
  UNIQUE KEY `uk_dict_path` (`tenant_id`,`node_path`,`deleted`) USING BTREE,
  KEY `idx_dict_parent` (`parent_id`) USING BTREE,
  KEY `idx_dict_code` (`dict_code`) USING BTREE,
  KEY `idx_dict_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5000000000000000261 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='数据字典表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_dict`
--

LOCK TABLES `sys_dict` WRITE;
/*!40000 ALTER TABLE `sys_dict` DISABLE KEYS */;
INSERT INTO `sys_dict` VALUES (4,0,'登录状态','login_status',NULL,NULL,'{\"en-US\": \"Login Status\", \"ja-JP\": \"ログイン状態\", \"ko-KR\": \"로그인 상태\", \"zh-CN\": \"登录状态\", \"zh-TW\": \"登錄狀態\"}','login_status',1,2,1,1,'登录日志状态字典',1993479636925403138,1,'2026-01-15 16:54:14',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5,4,'成功','success',NULL,'1','{\"en-US\": \"Success\", \"ja-JP\": \"成功\", \"ko-KR\": \"성공\", \"zh-CN\": \"成功\", \"zh-TW\": \"成功\"}','login_status/success',2,0,1,1,NULL,1993479636925403138,1,'2026-01-15 16:54:32',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(6,4,'失败','fail',NULL,'0','{\"en-US\": \"Fail\", \"ja-JP\": \"失敗\", \"ko-KR\": \"실패\", \"zh-CN\": \"失败\", \"zh-TW\": \"失敗\"}','login_status/fail',2,0,2,1,NULL,1993479636925403138,1,'2026-01-15 16:54:54',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(7,0,'是否','yes_no',NULL,NULL,'{\"en-US\": \"Yes/No\", \"ja-JP\": \"はい/いいえ\", \"ko-KR\": \"예/아니오\", \"zh-CN\": \"是否\", \"zh-TW\": \"是否\"}','yes_no',1,2,2,1,'通用是否字典',1993479636925403138,1,'2026-01-15 16:55:46',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(8,7,'是','yes',NULL,'1','{\"en-US\": \"Yes\", \"ja-JP\": \"はい\", \"ko-KR\": \"예\", \"zh-CN\": \"是\", \"zh-TW\": \"是\"}','yes_no/yes',2,0,1,1,NULL,1993479636925403138,1,'2026-01-15 16:56:01',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(9,7,'否','no',NULL,'0','{\"en-US\": \"No\", \"ja-JP\": \"いいえ\", \"ko-KR\": \"아니오\", \"zh-CN\": \"否\", \"zh-TW\": \"否\"}','yes_no/no',2,0,2,1,NULL,1993479636925403138,1,'2026-01-15 16:56:12',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(10,0,'组织类型','org_type',NULL,NULL,'{\"en-US\": \"Organization Type\", \"ja-JP\": \"組織タイプ\", \"ko-KR\": \"조직 유형\", \"zh-CN\": \"组织类型\", \"zh-TW\": \"組織類型\"}','/org_type',1,5,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:16',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(11,0,'组织层级','org_level',NULL,NULL,'{\"en-US\": \"Organization Level\", \"ja-JP\": \"組織レベル\", \"ko-KR\": \"조직 수준\", \"zh-CN\": \"组织层级\", \"zh-TW\": \"組織層級\"}','/org_level',1,5,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:16',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(22,10,'集团','group',NULL,'group','{\"en-US\": \"Group\", \"ja-JP\": \"グループ\", \"ko-KR\": \"그룹\", \"zh-CN\": \"集团\", \"zh-TW\": \"集團\"}','/org_type/group',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:49',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(23,10,'公司','company',NULL,'company','{\"en-US\": \"Company\", \"ja-JP\": \"会社\", \"ko-KR\": \"회사\", \"zh-CN\": \"公司\", \"zh-TW\": \"公司\"}','/org_type/company',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:49',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(24,10,'子公司','subsidiary',NULL,'subsidiary','{\"en-US\": \"Subsidiary\", \"ja-JP\": \"子会社\", \"ko-KR\": \"자회사\", \"zh-CN\": \"子公司\", \"zh-TW\": \"子公司\"}','/org_type/subsidiary',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:49',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(25,10,'部门','department',NULL,'department','{\"en-US\": \"Department\", \"ja-JP\": \"部門\", \"ko-KR\": \"부서\", \"zh-CN\": \"部门\", \"zh-TW\": \"部門\"}','/org_type/department',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:49',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(26,10,'班组','team',NULL,'team','{\"en-US\": \"Team\", \"ja-JP\": \"チーム\", \"ko-KR\": \"팀\", \"zh-CN\": \"班组\", \"zh-TW\": \"團隊\"}','/org_type/team',2,0,5,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:49',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(27,11,'集团','level1',NULL,'1','{\"en-US\": \"Group\", \"ja-JP\": \"グループ\", \"ko-KR\": \"그룹\", \"zh-CN\": \"集团\", \"zh-TW\": \"集團\"}','/org_level/level1',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:57',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"purple\"}'),(28,11,'公司','level2',NULL,'2','{\"en-US\": \"Company\", \"ja-JP\": \"会社\", \"ko-KR\": \"회사\", \"zh-CN\": \"公司\", \"zh-TW\": \"公司\"}','/org_level/level2',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:57',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"purple\"}'),(29,11,'子公司','level3',NULL,'3','{\"en-US\": \"Subsidiary\", \"ja-JP\": \"子会社\", \"ko-KR\": \"자회사\", \"zh-CN\": \"子公司\", \"zh-TW\": \"子公司\"}','/org_level/level3',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:57',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"purple\"}'),(30,11,'部门','level4',NULL,'4','{\"en-US\": \"Department\", \"ja-JP\": \"部門\", \"ko-KR\": \"부서\", \"zh-CN\": \"部门\", \"zh-TW\": \"部門\"}','/org_level/level4',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:57',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"purple\"}'),(31,11,'班组','level5',NULL,'5','{\"en-US\": \"Team\", \"ja-JP\": \"チーム\", \"ko-KR\": \"팀\", \"zh-CN\": \"班组\", \"zh-TW\": \"團隊\"}','/org_level/level5',2,0,5,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:23:57',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"purple\"}'),(32,0,'性别','gender',NULL,NULL,'{\"en-US\": \"Gender\", \"ja-JP\": \"性別\", \"ko-KR\": \"성별\", \"zh-CN\": \"性别\", \"zh-TW\": \"性別\"}','/gender',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(33,0,'学历','education',NULL,NULL,'{\"en-US\": \"Education\", \"ja-JP\": \"学歴\", \"ko-KR\": \"학력\", \"zh-CN\": \"学历\", \"zh-TW\": \"學歷\"}','/education',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(34,0,'政治面貌','political_status',NULL,NULL,'{\"en-US\": \"Political Status\", \"ja-JP\": \"政治的立場\", \"ko-KR\": \"정치적 입장\", \"zh-CN\": \"政治面貌\", \"zh-TW\": \"政治面貌\"}','/political_status',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(35,0,'职位级别','position_level',NULL,NULL,'{\"en-US\": \"Position Level\", \"ja-JP\": \"職位レベル\", \"ko-KR\": \"직위 레벨\", \"zh-CN\": \"职位级别\", \"zh-TW\": \"職位級別\"}','/position_level',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(36,0,'菜单模式','menu_mode',NULL,NULL,'{\"en-US\": \"Menu Mode\", \"ja-JP\": \"メニューモード\", \"ko-KR\": \"메뉴 모드\", \"zh-CN\": \"菜单模式\", \"zh-TW\": \"選單模式\"}','/menu_mode',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(37,0,'菜单类型','menu_type',NULL,NULL,'{\"en-US\": \"Menu Type\", \"ja-JP\": \"メニュータイプ\", \"ko-KR\": \"메뉴 유형\", \"zh-CN\": \"菜单类型\", \"zh-TW\": \"選單類型\"}','/menu_type',1,0,0,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:17',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(39,32,'男','male',NULL,'1','{\"en-US\": \"Male\", \"ja-JP\": \"男性\", \"ko-KR\": \"남성\", \"zh-CN\": \"男\", \"zh-TW\": \"男\"}','/gender/male',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:28',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"blue\"}'),(40,32,'女','female',NULL,'2','{\"en-US\": \"Female\", \"ja-JP\": \"女性\", \"ko-KR\": \"여성\", \"zh-CN\": \"女\", \"zh-TW\": \"女\"}','/gender/female',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:28',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"pink\"}'),(41,33,'小学','primary',NULL,'primary','{\"en-US\": \"Primary\", \"ja-JP\": \"小学校\", \"ko-KR\": \"초등학교\", \"zh-CN\": \"小学\", \"zh-TW\": \"小學\"}','/education/primary',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(42,33,'初中','junior',NULL,'junior','{\"en-US\": \"Junior\", \"ja-JP\": \"中学校\", \"ko-KR\": \"중학교\", \"zh-CN\": \"初中\", \"zh-TW\": \"初中\"}','/education/junior',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(43,33,'高中','senior',NULL,'senior','{\"en-US\": \"Senior\", \"ja-JP\": \"高等学校\", \"ko-KR\": \"고등학교\", \"zh-CN\": \"高中\", \"zh-TW\": \"高中\"}','/education/senior',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(44,33,'职高','vocational',NULL,'vocational','{\"en-US\": \"Vocational\", \"ja-JP\": \"専門学校\", \"ko-KR\": \"직업학교\", \"zh-CN\": \"职高\", \"zh-TW\": \"職校\"}','/education/vocational',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(45,33,'大专','college',NULL,'college','{\"en-US\": \"College\", \"ja-JP\": \"短期大学\", \"ko-KR\": \"전문대학\", \"zh-CN\": \"大专\", \"zh-TW\": \"大專\"}','/education/college',2,0,5,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(46,33,'本科','bachelor',NULL,'bachelor','{\"en-US\": \"Bachelor\", \"ja-JP\": \"学士\", \"ko-KR\": \"학사\", \"zh-CN\": \"本科\", \"zh-TW\": \"本科\"}','/education/bachelor',2,0,6,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(47,33,'硕士','master',NULL,'master','{\"en-US\": \"Master\", \"ja-JP\": \"修士\", \"ko-KR\": \"석사\", \"zh-CN\": \"硕士\", \"zh-TW\": \"碩士\"}','/education/master',2,0,7,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(48,33,'博士','doctor',NULL,'doctor','{\"en-US\": \"Doctor\", \"ja-JP\": \"博士\", \"ko-KR\": \"박사\", \"zh-CN\": \"博士\", \"zh-TW\": \"博士\"}','/education/doctor',2,0,8,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:37',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(49,34,'其他','other',NULL,'other','{\"en-US\": \"Other\", \"ja-JP\": \"その他\", \"ko-KR\": \"기타\", \"zh-CN\": \"其他\", \"zh-TW\": \"其他\"}','/political_status/other',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:47',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(50,34,'共产党员','communist',NULL,'communist','{\"en-US\": \"Communist Party Member\", \"ja-JP\": \"共産党員\", \"ko-KR\": \"공산당원\", \"zh-CN\": \"共产党员\", \"zh-TW\": \"共產黨員\"}','/political_status/communist',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:47',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(51,34,'民主党派','democratic',NULL,'democratic','{\"en-US\": \"Democratic Party\", \"ja-JP\": \"民主主義政党\", \"ko-KR\": \"민주당\", \"zh-CN\": \"民主党派\", \"zh-TW\": \"民主黨派\"}','/political_status/democratic',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:47',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(52,34,'共青团员','league',NULL,'league','{\"en-US\": \"League Member\", \"ja-JP\": \"共青団員\", \"ko-KR\": \"공청단원\", \"zh-CN\": \"共青团员\", \"zh-TW\": \"共青團員\"}','/political_status/league',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:47',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(53,34,'群众','masses',NULL,'masses','{\"en-US\": \"Masses\", \"ja-JP\": \"大衆\", \"ko-KR\": \"대중\", \"zh-CN\": \"群众\", \"zh-TW\": \"群眾\"}','/political_status/masses',2,0,5,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:24:47',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(59,36,'菜单','menu',NULL,'menu','{\"en-US\": \"Menu\", \"ja-JP\": \"メニュー\", \"ko-KR\": \"메뉴\", \"zh-CN\": \"菜单\", \"zh-TW\": \"選單\"}','/menu_mode/menu',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:05',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(60,36,'标签页','tab',NULL,'tab','{\"en-US\": \"Tab\", \"ja-JP\": \"タブ\", \"ko-KR\": \"탭\", \"zh-CN\": \"标签页\", \"zh-TW\": \"標籤頁\"}','/menu_mode/tab',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:05',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(61,36,'弹窗','modal',NULL,'modal','{\"en-US\": \"Modal\", \"ja-JP\": \"モーダル\", \"ko-KR\": \"모달\", \"zh-CN\": \"弹窗\", \"zh-TW\": \"彈窗\"}','/menu_mode/modal',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:05',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(62,36,'抽屉','drawer',NULL,'drawer','{\"en-US\": \"Drawer\", \"ja-JP\": \"ドロワー\", \"ko-KR\": \"드로워\", \"zh-CN\": \"抽屉\", \"zh-TW\": \"抽屜\"}','/menu_mode/drawer',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:05',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(63,36,'外部链接','external',NULL,'external','{\"en-US\": \"External\", \"ja-JP\": \"外部リンク\", \"ko-KR\": \"외부 링크\", \"zh-CN\": \"外部链接\", \"zh-TW\": \"外部連結\"}','/menu_mode/external',2,0,5,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:05',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(64,37,'目录','directory',NULL,'directory','{\"en-US\": \"Directory\", \"ja-JP\": \"ディレクトリ\", \"ko-KR\": \"디렉토리\", \"zh-CN\": \"目录\", \"zh-TW\": \"目錄\"}','/menu_type/directory',2,0,1,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:13',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(65,37,'菜单','menu',NULL,'menu','{\"en-US\": \"Menu\", \"ja-JP\": \"メニュー\", \"ko-KR\": \"메뉴\", \"zh-CN\": \"菜单\", \"zh-TW\": \"選單\"}','/menu_type/menu',2,0,2,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:13',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(66,37,'按钮','button',NULL,'button','{\"en-US\": \"Button\", \"ja-JP\": \"ボタン\", \"ko-KR\": \"버튼\", \"zh-CN\": \"按钮\", \"zh-TW\": \"按鈕\"}','/menu_type/button',2,0,3,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:13',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(67,37,'链接','link',NULL,'link','{\"en-US\": \"Link\", \"ja-JP\": \"リンク\", \"ko-KR\": \"링크\", \"zh-CN\": \"链接\", \"zh-TW\": \"連結\"}','/menu_type/link',2,0,4,1,NULL,1993479636925403138,1993479637244170242,'2026-01-16 13:25:13',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(68,0,'状态','status',NULL,NULL,'{\"en-US\": \"Status\", \"ja-JP\": \"ステータス\", \"ko-KR\": \"상태\", \"zh-CN\": \"状态\", \"zh-TW\": \"狀態\"}','status',1,0,100,1,'通用状态字典',1993479636925403138,1993479637244170242,'2026-01-20 23:10:08',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(69,68,'启用','enabled',NULL,'1','{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}','status/enabled',2,0,1,1,'启用状态',1993479636925403138,1993479637244170242,'2026-01-20 23:10:59',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"green\"}'),(70,68,'禁用','disabled',NULL,'0','{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"禁用\", \"zh-TW\": \"停用\"}','status/disabled',2,0,2,1,'禁用状态',1993479636925403138,1993479637244170242,'2026-01-20 23:10:59',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"red\"}'),(71,0,'可见性','visible',NULL,NULL,'{\"en-US\": \"Visibility\", \"ja-JP\": \"可視性\", \"ko-KR\": \"가시성\", \"zh-CN\": \"可见性\", \"zh-TW\": \"可見性\"}','visible',1,0,101,1,'可见性字典',1993479636925403138,1993479637244170242,'2026-01-20 23:15:56',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(72,71,'显示','show',NULL,'true','{\"en-US\": \"Show\", \"ja-JP\": \"表示\", \"ko-KR\": \"표시\", \"zh-CN\": \"显示\", \"zh-TW\": \"顯示\"}','visible/show',2,0,1,1,'显示状态',1993479636925403138,1993479637244170242,'2026-01-20 23:16:36',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"green\"}'),(73,71,'隐藏','hide',NULL,'false','{\"en-US\": \"Hide\", \"ja-JP\": \"非表示\", \"ko-KR\": \"숨기기\", \"zh-CN\": \"隐藏\", \"zh-TW\": \"隱藏\"}','visible/hide',2,0,2,1,'隐藏状态',1993479636925403138,1993479637244170242,'2026-01-20 23:16:36',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"default\"}'),(77,32,'未知','unknown',NULL,'0','{\"en-US\": \"Unknown\", \"ja-JP\": \"不明\", \"ko-KR\": \"알 수 없음\", \"zh-CN\": \"未知\", \"zh-TW\": \"未知\"}','/gender/unknown',2,0,1,1,'未知性别',1993479636925403138,1993479637244170242,'2026-01-21 20:20:56',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"default\"}'),(78,35,'一级','level1',NULL,'1','{\"en-US\": \"Level 1\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"一级\", \"zh-TW\": \"1\"}','/position_level/1',2,0,1,1,'一级职位',1993479636925403138,1993479637244170242,'2026-01-21 20:23:55',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(79,35,'二级','level2',NULL,'2','{\"en-US\": \"Level 2\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"二级\", \"zh-TW\": \"2\"}','/position_level/2',2,0,2,1,'二级职位',1993479636925403138,1993479637244170242,'2026-01-21 20:23:55',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(80,35,'三级','level3',NULL,'3','{\"en-US\": \"Level 3\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"三级\", \"zh-TW\": \"3\"}','/position_level/3',2,0,3,1,'三级职位',1993479636925403138,1993479637244170242,'2026-01-21 20:23:55',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(81,35,'四级','level4',NULL,'4','{\"en-US\": \"Level 4\", \"ja-JP\": \"4\", \"ko-KR\": \"4\", \"zh-CN\": \"四级\", \"zh-TW\": \"4\"}','/position_level/4',2,0,4,1,'四级职位',1993479636925403138,1993479637244170242,'2026-01-21 20:23:55',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(82,35,'五级','level5',NULL,'5','{\"en-US\": \"Level 5\", \"ja-JP\": \"5\", \"ko-KR\": \"5\", \"zh-CN\": \"五级\", \"zh-TW\": \"5\"}','/position_level/5',2,0,5,1,'五级职位',1993479636925403138,1993479637244170242,'2026-01-21 20:23:55',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(3000000000000000571,0,'通知范围','notice_scope',1,NULL,'{\"en-US\": \"Notice Scope\", \"ja-JP\": \"通知範囲\", \"ko-KR\": \"알림 범위\", \"zh-CN\": \"通知范围\", \"zh-TW\": \"通知範圍\"}','notice_scope',1,2,1,1,'系统通知范围',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,NULL),(3000000000000000572,3000000000000000571,'公共','public',1,'PUBLIC','{\"en-US\": \"Public\", \"ja-JP\": \"公開\", \"ko-KR\": \"공용\", \"zh-CN\": \"公共\", \"zh-TW\": \"公共\"}','notice_scope/public',2,0,1,1,'系统通知范围',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,'{\"color\": \"blue\"}'),(3000000000000000573,3000000000000000571,'租户','tenant',1,'TENANT','{\"en-US\": \"Tenant\", \"ja-JP\": \"テナント\", \"ko-KR\": \"테넌트\", \"zh-CN\": \"租户\", \"zh-TW\": \"租戶\"}','notice_scope/tenant',2,0,2,1,'系统通知范围',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,'{\"color\": \"green\"}'),(3000000000000000574,0,'通知状态','notice_status',1,NULL,'{\"en-US\": \"Notice Status\", \"ja-JP\": \"通知状態\", \"ko-KR\": \"알림 상태\", \"zh-CN\": \"通知状态\", \"zh-TW\": \"通知狀態\"}','notice_status',1,3,2,1,'系统通知状态',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,NULL),(3000000000000000575,3000000000000000574,'草稿','draft',1,'DRAFT','{\"en-US\": \"Draft\", \"ja-JP\": \"下書き\", \"ko-KR\": \"초안\", \"zh-CN\": \"草稿\", \"zh-TW\": \"草稿\"}','notice_status/draft',2,0,1,1,'系统通知状态',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,'{\"color\": \"default\"}'),(3000000000000000576,3000000000000000574,'已发布','published',1,'PUBLISHED','{\"en-US\": \"Published\", \"ja-JP\": \"公開済み\", \"ko-KR\": \"게시됨\", \"zh-CN\": \"已发布\", \"zh-TW\": \"已發布\"}','notice_status/published',2,0,2,1,'系统通知状态',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,'{\"color\": \"success\"}'),(3000000000000000577,3000000000000000574,'已停用','disabled',1,'DISABLED','{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"중지됨\", \"zh-CN\": \"已停用\", \"zh-TW\": \"已停用\"}','notice_status/disabled',2,0,3,1,'系统通知状态',0,NULL,'2026-05-10 21:26:44',NULL,'2026-05-10 21:26:44',0,'{\"color\": \"error\"}'),(5000000000000000001,0,'编码段类型','encode_segment_type',NULL,NULL,'{\"zh-CN\": \"编码段类型\", \"zh-TW\": \"編碼段類型\", \"en-US\": \"Encode Segment Type\", \"ja-JP\": \"コードセグメントタイプ\", \"ko-KR\": \"인코드 세그먼트 유형\"}','encode_segment_type',1,4,1,1,'用于配置编码规则中各段的类型：FIXED=固定字符，DATE=日期，SEQUENCE=序列号，VARIABLE=变量',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000002,0,'重置类型','reset_type',NULL,NULL,'{\"zh-CN\": \"重置类型\", \"zh-TW\": \"重置類型\", \"en-US\": \"Reset Type\", \"ja-JP\": \"リセットタイプ\", \"ko-KR\": \"재설정 유형\"}','reset_type',1,4,2,1,'用于配置编码规则中序列号的重置周期：NONE=不重置，DAILY=每日重置，MONTHLY=每月重置，YEARLY=每年重置',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000003,0,'填充方向','padding_side',NULL,NULL,'{\"zh-CN\": \"填充方向\", \"zh-TW\": \"填充方向\", \"en-US\": \"Padding Side\", \"ja-JP\": \"パディング方向\", \"ko-KR\": \"패딩 방향\"}','padding_side',1,2,3,1,'用于配置序列号填充字符的方向：LEFT=左侧填充，RIGHT=右侧填充',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000011,5000000000000000001,'固定字符','encode_segment_type_fixed',NULL,'FIXED','{\"en-US\": \"Fixed Character\", \"ja-JP\": \"固定文字\", \"ko-KR\": \"고정 문자\", \"zh-CN\": \"固定字符\", \"zh-TW\": \"固定字符\"}','encode_segment_type/fixed',2,0,1,1,'固定字符段，编码中显示固定的字符（如前缀、后缀、分隔符等）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000012,5000000000000000001,'日期','encode_segment_type_date',NULL,'DATE','{\"en-US\": \"Date\", \"ja-JP\": \"日付\", \"ko-KR\": \"날짜\", \"zh-CN\": \"日期\", \"zh-TW\": \"日期\"}','encode_segment_type/date',2,0,2,1,'日期段，编码中显示日期信息（支持 yyyy、MM、dd、HHmmss 等格式）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000013,5000000000000000001,'序列号','encode_segment_type_sequence',NULL,'SEQUENCE','{\"en-US\": \"Sequence Number\", \"ja-JP\": \"シーケンス番号\", \"ko-KR\": \"일련 번호\", \"zh-CN\": \"序列号\", \"zh-TW\": \"序列號\"}','encode_segment_type/sequence',2,0,3,1,'序列号段，编码中显示递增的序列号（支持自定义起始值、长度、填充字符等）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000014,5000000000000000001,'变量','encode_segment_type_variable',NULL,'VARIABLE','{\"en-US\": \"Variable\", \"ja-JP\": \"変数\", \"ko-KR\": \"변수\", \"zh-CN\": \"变量\", \"zh-TW\": \"變量\"}','encode_segment_type/variable',2,0,4,1,'变量段，编码中显示动态变量值（如部门编码、人员编码、业务类型等）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000021,5000000000000000002,'不重置','reset_type_none',NULL,'NONE','{\"en-US\": \"No Reset\", \"ja-JP\": \"リセットしない\", \"ko-KR\": \"재설정 안 함\", \"zh-CN\": \"不重置\", \"zh-TW\": \"不重置\"}','reset_type/none',2,0,1,1,'序列号不重置，持续递增',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000022,5000000000000000002,'每日重置','reset_type_daily',NULL,'DAILY','{\"en-US\": \"Daily Reset\", \"ja-JP\": \"毎日リセット\", \"ko-KR\": \"매일 재설정\", \"zh-CN\": \"每日重置\", \"zh-TW\": \"每日重置\"}','reset_type/daily',2,0,2,1,'序列号每天重置为起始值',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000023,5000000000000000002,'每月重置','reset_type_monthly',NULL,'MONTHLY','{\"en-US\": \"Monthly Reset\", \"ja-JP\": \"毎月リセット\", \"ko-KR\": \"매월 재설정\", \"zh-CN\": \"每月重置\", \"zh-TW\": \"每月重置\"}','reset_type/monthly',2,0,3,1,'序列号每月重置为起始值',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000024,5000000000000000002,'每年重置','reset_type_yearly',NULL,'YEARLY','{\"en-US\": \"Yearly Reset\", \"ja-JP\": \"毎年リセット\", \"ko-KR\": \"매년 재설정\", \"zh-CN\": \"每年重置\", \"zh-TW\": \"每年重置\"}','reset_type/yearly',2,0,4,1,'序列号每年重置为起始值',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000031,5000000000000000003,'左侧填充','padding_side_left',NULL,'LEFT','{\"en-US\": \"Left Padding\", \"ja-JP\": \"左パディング\", \"ko-KR\": \"왼쪽 패딩\", \"zh-CN\": \"左侧填充\", \"zh-TW\": \"左側填充\"}','padding_side/left',2,0,1,1,'填充字符在序列号左侧（如：000123）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000032,5000000000000000003,'右侧填充','padding_side_right',NULL,'RIGHT','{\"en-US\": \"Right Padding\", \"ja-JP\": \"右パディング\", \"ko-KR\": \"오른쪽 패딩\", \"zh-CN\": \"右侧填充\", \"zh-TW\": \"右側填充\"}','padding_side/right',2,0,2,1,'填充字符在序列号右侧（如：123000）',0,1993479637244170242,'2026-04-10 11:30:30',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\":\"blue\"}'),(5000000000000000043,0,'审批表单类型','wf_task_form_type',NULL,NULL,'{\"en-US\": \"wf_task_form_type\", \"ja-JP\": \"审批表单类型\", \"ko-KR\": \"wf_task_form_type\", \"zh-CN\": \"审批表单类型\", \"zh-TW\": \"审批表单类型\"}','wf_task_form_type',1,2,301,1,'审批任务配置页表单类型字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000044,0,'审批执行状态','wf_execution_status',NULL,NULL,'{\"en-US\": \"wf_execution_status\", \"ja-JP\": \"审批执行状态\", \"ko-KR\": \"wf_execution_status\", \"zh-CN\": \"审批执行状态\", \"zh-TW\": \"审批执行状态\"}','wf_execution_status',1,4,302,1,'审批待办/已办/我发起状态字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000045,0,'审批类型','wf_approve_type',NULL,NULL,'{\"en-US\": \"wf_approve_type\", \"ja-JP\": \"审批类型\", \"ko-KR\": \"wf_approve_type\", \"zh-CN\": \"审批类型\", \"zh-TW\": \"审批类型\"}','wf_approve_type',1,5,303,1,'审批节点审批类型字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000046,0,'审批人来源','wf_approver_type',NULL,NULL,'{\"en-US\": \"wf_approver_type\", \"ja-JP\": \"审批人来源\", \"ko-KR\": \"wf_approver_type\", \"zh-CN\": \"审批人来源\", \"zh-TW\": \"审批人来源\"}','wf_approver_type',1,4,304,1,'审批节点审批人来源字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000047,0,'驳回类型','wf_reject_type',NULL,NULL,'{\"en-US\": \"wf_reject_type\", \"ja-JP\": \"驳回类型\", \"ko-KR\": \"wf_reject_type\", \"zh-CN\": \"驳回类型\", \"zh-TW\": \"驳回类型\"}','wf_reject_type',1,2,305,1,'待办审批驳回类型字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000048,0,'分支操作符','wf_branch_operator',NULL,NULL,'{\"en-US\": \"wf_branch_operator\", \"ja-JP\": \"分支操作符\", \"ko-KR\": \"wf_branch_operator\", \"zh-CN\": \"分支操作符\", \"zh-TW\": \"分支操作符\"}','wf_branch_operator',1,7,306,1,'审批分支规则操作符字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000049,0,'编码规则启用状态','encode_rule_enabled',NULL,NULL,'{\"en-US\": \"encode_rule_enabled\", \"ja-JP\": \"编码规则启用状态\", \"ko-KR\": \"encode_rule_enabled\", \"zh-CN\": \"编码规则启用状态\", \"zh-TW\": \"编码规则启用状态\"}','encode_rule_enabled',1,2,307,1,'编码规则状态字典',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000050,0,'编码规则段类型','encode_rule_segment_type',NULL,NULL,'{\"en-US\": \"encode_rule_segment_type\", \"ja-JP\": \"编码规则段类型\", \"ko-KR\": \"encode_rule_segment_type\", \"zh-CN\": \"编码规则段类型\", \"zh-TW\": \"编码规则段类型\"}','encode_rule_segment_type',1,4,308,1,'编码规则段类型字典（与当前前端值域一致）',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000051,0,'编码规则序列重置类型','encode_rule_seq_reset_type',NULL,NULL,'{\"en-US\": \"encode_rule_seq_reset_type\", \"ja-JP\": \"编码规则序列重置类型\", \"ko-KR\": \"encode_rule_seq_reset_type\", \"zh-CN\": \"编码规则序列重置类型\", \"zh-TW\": \"编码规则序列重置类型\"}','encode_rule_seq_reset_type',1,4,309,1,'编码规则序列重置类型字典（与当前前端值域一致）',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000058,5000000000000000043,'自定义表单','custom_form',NULL,'1','{\"en-US\": \"Custom Form\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"自定义表单\", \"zh-TW\": \"1\"}','wf_task_form_type/custom_form',2,0,1,0,'审批任务配置表单类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000059,5000000000000000043,'低代码表单','low_code_form',NULL,'2','{\"en-US\": \"Low Code Form\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"低代码表单\", \"zh-TW\": \"2\"}','wf_task_form_type/low_code_form',2,0,2,0,'审批任务配置表单类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000060,5000000000000000044,'待处理','pending',NULL,'0','{\"en-US\": \"Pending\", \"ja-JP\": \"0\", \"ko-KR\": \"0\", \"zh-CN\": \"待处理\", \"zh-TW\": \"0\"}','wf_execution_status/pending',2,0,1,0,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"default\"}'),(5000000000000000061,5000000000000000044,'审批中','processing',NULL,'1','{\"en-US\": \"Processing\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"审批中\", \"zh-TW\": \"1\"}','wf_execution_status/processing',2,0,2,0,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"processing\"}'),(5000000000000000062,5000000000000000044,'审批完成','finished',NULL,'2','{\"en-US\": \"Finished\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"审批完成\", \"zh-TW\": \"2\"}','wf_execution_status/finished',2,0,3,0,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"success\"}'),(5000000000000000063,5000000000000000044,'驳回','rejected',NULL,'3','{\"en-US\": \"Rejected\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"驳回\", \"zh-TW\": \"3\"}','wf_execution_status/rejected',2,0,4,0,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"error\"}'),(5000000000000000064,5000000000000000045,'会签','countersign',NULL,'1','{\"en-US\": \"Countersign\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"会签\", \"zh-TW\": \"1\"}','wf_approve_type/countersign',2,0,1,0,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000065,5000000000000000045,'或签','or_sign',NULL,'2','{\"en-US\": \"Any One Approves\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"或签\", \"zh-TW\": \"2\"}','wf_approve_type/or_sign',2,0,2,0,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000066,5000000000000000045,'抄送','cc',NULL,'3','{\"en-US\": \"Carbon Copy\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"抄送\", \"zh-TW\": \"3\"}','wf_approve_type/cc',2,0,3,0,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000067,5000000000000000045,'投票','vote',NULL,'4','{\"en-US\": \"Vote\", \"ja-JP\": \"4\", \"ko-KR\": \"4\", \"zh-CN\": \"投票\", \"zh-TW\": \"4\"}','wf_approve_type/vote',2,0,4,0,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000068,5000000000000000045,'顺序审批','sequential',NULL,'5','{\"en-US\": \"Sequential Approval\", \"ja-JP\": \"5\", \"ko-KR\": \"5\", \"zh-CN\": \"顺序审批\", \"zh-TW\": \"5\"}','wf_approve_type/sequential',2,0,5,0,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000069,5000000000000000046,'用户','user',NULL,'1','{\"en-US\": \"User\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"用户\", \"zh-TW\": \"1\"}','wf_approver_type/user',2,0,1,0,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000070,5000000000000000046,'部门','department',NULL,'2','{\"en-US\": \"Department\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"部门\", \"zh-TW\": \"2\"}','wf_approver_type/department',2,0,2,0,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000071,5000000000000000046,'角色','role',NULL,'3','{\"en-US\": \"Role\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"角色\", \"zh-TW\": \"3\"}','wf_approver_type/role',2,0,3,0,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000072,5000000000000000046,'岗位','position',NULL,'4','{\"en-US\": \"Position\", \"ja-JP\": \"4\", \"ko-KR\": \"4\", \"zh-CN\": \"岗位\", \"zh-TW\": \"4\"}','wf_approver_type/position',2,0,4,0,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000073,5000000000000000047,'驳回结束当前审批流程','reject_finish',NULL,'1','{\"en-US\": \"Reject And Finish\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"驳回结束当前审批流程\", \"zh-TW\": \"1\"}','wf_reject_type/reject_finish',2,0,1,0,'待办审批驳回类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000074,5000000000000000047,'退回上一节点重新审批','reject_previous_node',NULL,'2','{\"en-US\": \"Back To Previous Node\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"退回上一节点重新审批\", \"zh-TW\": \"2\"}','wf_reject_type/reject_previous_node',2,0,2,0,'待办审批驳回类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000075,5000000000000000048,'等于','eq',NULL,'=','{\"en-US\": \"Equals\", \"ja-JP\": \"=\", \"ko-KR\": \"=\", \"zh-CN\": \"等于\", \"zh-TW\": \"=\"}','wf_branch_operator/eq',2,0,1,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000076,5000000000000000048,'不等于','ne',NULL,'!=','{\"en-US\": \"Not Equals\", \"ja-JP\": \"!=\", \"ko-KR\": \"!=\", \"zh-CN\": \"不等于\", \"zh-TW\": \"!=\"}','wf_branch_operator/ne',2,0,2,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000077,5000000000000000048,'大于','gt',NULL,'>','{\"en-US\": \"Greater Than\", \"ja-JP\": \">\", \"ko-KR\": \">\", \"zh-CN\": \"大于\", \"zh-TW\": \">\"}','wf_branch_operator/gt',2,0,3,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000078,5000000000000000048,'大于等于','ge',NULL,'>=','{\"en-US\": \"Greater Or Equal\", \"ja-JP\": \">=\", \"ko-KR\": \">=\", \"zh-CN\": \"大于等于\", \"zh-TW\": \">=\"}','wf_branch_operator/ge',2,0,4,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000079,5000000000000000048,'小于','lt',NULL,'<','{\"en-US\": \"Less Than\", \"ja-JP\": \"<\", \"ko-KR\": \"<\", \"zh-CN\": \"小于\", \"zh-TW\": \"<\"}','wf_branch_operator/lt',2,0,5,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000080,5000000000000000048,'小于等于','le',NULL,'<=','{\"en-US\": \"Less Or Equal\", \"ja-JP\": \"<=\", \"ko-KR\": \"<=\", \"zh-CN\": \"小于等于\", \"zh-TW\": \"<=\"}','wf_branch_operator/le',2,0,6,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000081,5000000000000000048,'包含','contains',NULL,'contains','{\"en-US\": \"Contains\", \"ja-JP\": \"contains\", \"ko-KR\": \"contains\", \"zh-CN\": \"包含\", \"zh-TW\": \"contains\"}','wf_branch_operator/contains',2,0,7,0,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000082,5000000000000000049,'启用','enabled',NULL,'true','{\"en-US\": \"Enabled\", \"ja-JP\": \"true\", \"ko-KR\": \"true\", \"zh-CN\": \"启用\", \"zh-TW\": \"true\"}','encode_rule_enabled/enabled',2,0,1,0,'编码规则启用状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"success\"}'),(5000000000000000083,5000000000000000049,'禁用','disabled',NULL,'false','{\"en-US\": \"Disabled\", \"ja-JP\": \"false\", \"ko-KR\": \"false\", \"zh-CN\": \"禁用\", \"zh-TW\": \"false\"}','encode_rule_enabled/disabled',2,0,2,0,'编码规则启用状态',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,'{\"color\": \"default\"}'),(5000000000000000084,5000000000000000050,'固定值','fixed',NULL,'FIXED','{\"en-US\": \"Fixed\", \"ja-JP\": \"FIXED\", \"ko-KR\": \"FIXED\", \"zh-CN\": \"固定值\", \"zh-TW\": \"FIXED\"}','encode_rule_segment_type/fixed',2,0,1,0,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000085,5000000000000000050,'日期','date',NULL,'DATE','{\"en-US\": \"Date\", \"ja-JP\": \"DATE\", \"ko-KR\": \"DATE\", \"zh-CN\": \"日期\", \"zh-TW\": \"DATE\"}','encode_rule_segment_type/date',2,0,2,0,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000086,5000000000000000050,'流水号','seq',NULL,'SEQ','{\"en-US\": \"Sequence\", \"ja-JP\": \"SEQ\", \"ko-KR\": \"SEQ\", \"zh-CN\": \"流水号\", \"zh-TW\": \"SEQ\"}','encode_rule_segment_type/seq',2,0,3,0,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000087,5000000000000000050,'自定义','custom',NULL,'CUSTOM','{\"en-US\": \"Custom\", \"ja-JP\": \"CUSTOM\", \"ko-KR\": \"CUSTOM\", \"zh-CN\": \"自定义\", \"zh-TW\": \"CUSTOM\"}','encode_rule_segment_type/custom',2,0,4,0,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000088,5000000000000000051,'永不重置','never',NULL,'0','{\"en-US\": \"Never\", \"ja-JP\": \"0\", \"ko-KR\": \"0\", \"zh-CN\": \"永不重置\", \"zh-TW\": \"0\"}','encode_rule_seq_reset_type/never',2,0,1,0,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000089,5000000000000000051,'按年重置','yearly',NULL,'1','{\"en-US\": \"Yearly\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"按年重置\", \"zh-TW\": \"1\"}','encode_rule_seq_reset_type/yearly',2,0,2,0,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000090,5000000000000000051,'按月重置','monthly',NULL,'2','{\"en-US\": \"Monthly\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"按月重置\", \"zh-TW\": \"2\"}','encode_rule_seq_reset_type/monthly',2,0,3,0,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000091,5000000000000000051,'按日重置','daily',NULL,'3','{\"en-US\": \"Daily\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"按日重置\", \"zh-TW\": \"3\"}','encode_rule_seq_reset_type/daily',2,0,4,0,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 11:54:42',1993479637244170242,'2026-05-08 12:03:51',1,NULL),(5000000000000000121,5000000000000000043,'自定义表单','custom_form',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"自定义表单\", \"ko-KR\": \"1\", \"zh-CN\": \"自定义表单\", \"zh-TW\": \"自定义表单\"}','wf_task_form_type/custom_form',2,0,1,1,'审批任务配置表单类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000122,5000000000000000043,'低代码表单','low_code_form',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"低代码表单\", \"ko-KR\": \"2\", \"zh-CN\": \"低代码表单\", \"zh-TW\": \"低代码表单\"}','wf_task_form_type/low_code_form',2,0,2,1,'审批任务配置表单类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000123,5000000000000000044,'待处理','pending',NULL,'0','{\"en-US\": \"0\", \"ja-JP\": \"待处理\", \"ko-KR\": \"0\", \"zh-CN\": \"待处理\", \"zh-TW\": \"待处理\"}','wf_execution_status/pending',2,0,1,1,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"default\"}'),(5000000000000000124,5000000000000000044,'审批中','processing',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"审批中\", \"ko-KR\": \"1\", \"zh-CN\": \"审批中\", \"zh-TW\": \"审批中\"}','wf_execution_status/processing',2,0,2,1,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"processing\"}'),(5000000000000000125,5000000000000000044,'审批完成','finished',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"审批完成\", \"ko-KR\": \"2\", \"zh-CN\": \"审批完成\", \"zh-TW\": \"审批完成\"}','wf_execution_status/finished',2,0,3,1,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"success\"}'),(5000000000000000126,5000000000000000044,'驳回','rejected',NULL,'3','{\"en-US\": \"3\", \"ja-JP\": \"驳回\", \"ko-KR\": \"3\", \"zh-CN\": \"驳回\", \"zh-TW\": \"驳回\"}','wf_execution_status/rejected',2,0,4,1,'审批执行状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"error\"}'),(5000000000000000127,5000000000000000045,'会签','countersign',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"会签\", \"ko-KR\": \"1\", \"zh-CN\": \"会签\", \"zh-TW\": \"会签\"}','wf_approve_type/countersign',2,0,1,1,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000128,5000000000000000045,'或签','or_sign',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"或签\", \"ko-KR\": \"2\", \"zh-CN\": \"或签\", \"zh-TW\": \"或签\"}','wf_approve_type/or_sign',2,0,2,1,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000129,5000000000000000045,'抄送','cc',NULL,'3','{\"en-US\": \"3\", \"ja-JP\": \"抄送\", \"ko-KR\": \"3\", \"zh-CN\": \"抄送\", \"zh-TW\": \"抄送\"}','wf_approve_type/cc',2,0,3,1,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000130,5000000000000000045,'投票','vote',NULL,'4','{\"en-US\": \"4\", \"ja-JP\": \"投票\", \"ko-KR\": \"4\", \"zh-CN\": \"投票\", \"zh-TW\": \"投票\"}','wf_approve_type/vote',2,0,4,1,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000131,5000000000000000045,'顺序审批','sequential',NULL,'5','{\"en-US\": \"5\", \"ja-JP\": \"顺序审批\", \"ko-KR\": \"5\", \"zh-CN\": \"顺序审批\", \"zh-TW\": \"顺序审批\"}','wf_approve_type/sequential',2,0,5,1,'审批节点审批类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000132,5000000000000000046,'用户','user',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"用户\", \"ko-KR\": \"1\", \"zh-CN\": \"用户\", \"zh-TW\": \"用户\"}','wf_approver_type/user',2,0,1,1,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000133,5000000000000000046,'部门','department',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"部门\", \"ko-KR\": \"2\", \"zh-CN\": \"部门\", \"zh-TW\": \"部门\"}','wf_approver_type/department',2,0,2,1,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000134,5000000000000000046,'角色','role',NULL,'3','{\"en-US\": \"3\", \"ja-JP\": \"角色\", \"ko-KR\": \"3\", \"zh-CN\": \"角色\", \"zh-TW\": \"角色\"}','wf_approver_type/role',2,0,3,1,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000135,5000000000000000046,'岗位','position',NULL,'4','{\"en-US\": \"4\", \"ja-JP\": \"岗位\", \"ko-KR\": \"4\", \"zh-CN\": \"岗位\", \"zh-TW\": \"岗位\"}','wf_approver_type/position',2,0,4,1,'审批节点审批人来源',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000136,5000000000000000047,'驳回结束当前审批流程','reject_finish',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"驳回结束当前审批流程\", \"ko-KR\": \"1\", \"zh-CN\": \"驳回结束当前审批流程\", \"zh-TW\": \"驳回结束当前审批流程\"}','wf_reject_type/reject_finish',2,0,1,1,'待办审批驳回类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000137,5000000000000000047,'退回上一节点重新审批','reject_previous_node',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"退回上一节点重新审批\", \"ko-KR\": \"2\", \"zh-CN\": \"退回上一节点重新审批\", \"zh-TW\": \"退回上一节点重新审批\"}','wf_reject_type/reject_previous_node',2,0,2,1,'待办审批驳回类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000138,5000000000000000048,'等于','eq',NULL,'=','{\"en-US\": \"=\", \"ja-JP\": \"等于\", \"ko-KR\": \"=\", \"zh-CN\": \"等于\", \"zh-TW\": \"等于\"}','wf_branch_operator/eq',2,0,1,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000139,5000000000000000048,'不等于','ne',NULL,'!=','{\"en-US\": \"!=\", \"ja-JP\": \"不等于\", \"ko-KR\": \"!=\", \"zh-CN\": \"不等于\", \"zh-TW\": \"不等于\"}','wf_branch_operator/ne',2,0,2,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000140,5000000000000000048,'大于','gt',NULL,'>','{\"en-US\": \">\", \"ja-JP\": \"大于\", \"ko-KR\": \">\", \"zh-CN\": \"大于\", \"zh-TW\": \"大于\"}','wf_branch_operator/gt',2,0,3,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000141,5000000000000000048,'大于等于','ge',NULL,'>=','{\"en-US\": \">=\", \"ja-JP\": \"大于等于\", \"ko-KR\": \">=\", \"zh-CN\": \"大于等于\", \"zh-TW\": \"大于等于\"}','wf_branch_operator/ge',2,0,4,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000142,5000000000000000048,'小于','lt',NULL,'<','{\"en-US\": \"<\", \"ja-JP\": \"小于\", \"ko-KR\": \"<\", \"zh-CN\": \"小于\", \"zh-TW\": \"小于\"}','wf_branch_operator/lt',2,0,5,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000143,5000000000000000048,'小于等于','le',NULL,'<=','{\"en-US\": \"<=\", \"ja-JP\": \"小于等于\", \"ko-KR\": \"<=\", \"zh-CN\": \"小于等于\", \"zh-TW\": \"小于等于\"}','wf_branch_operator/le',2,0,6,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000144,5000000000000000048,'包含','contains',NULL,'contains','{\"en-US\": \"contains\", \"ja-JP\": \"包含\", \"ko-KR\": \"contains\", \"zh-CN\": \"包含\", \"zh-TW\": \"包含\"}','wf_branch_operator/contains',2,0,7,1,'审批分支规则操作符',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000145,5000000000000000049,'启用','enabled',NULL,'true','{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"사용\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}','encode_rule_enabled/enabled',2,0,1,1,'编码规则启用状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"success\"}'),(5000000000000000146,5000000000000000049,'禁用','disabled',NULL,'false','{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"사용 안 함\", \"zh-CN\": \"禁用\", \"zh-TW\": \"停用\"}','encode_rule_enabled/disabled',2,0,2,1,'编码规则启用状态',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\": \"default\"}'),(5000000000000000147,5000000000000000050,'固定值','fixed',NULL,'FIXED','{\"en-US\": \"FIXED\", \"ja-JP\": \"固定值\", \"ko-KR\": \"FIXED\", \"zh-CN\": \"固定值\", \"zh-TW\": \"固定值\"}','encode_rule_segment_type/fixed',2,0,1,1,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000148,5000000000000000050,'日期','date',NULL,'DATE','{\"en-US\": \"DATE\", \"ja-JP\": \"日期\", \"ko-KR\": \"DATE\", \"zh-CN\": \"日期\", \"zh-TW\": \"日期\"}','encode_rule_segment_type/date',2,0,2,1,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000149,5000000000000000050,'流水号','seq',NULL,'SEQ','{\"en-US\": \"SEQ\", \"ja-JP\": \"流水号\", \"ko-KR\": \"SEQ\", \"zh-CN\": \"流水号\", \"zh-TW\": \"流水号\"}','encode_rule_segment_type/seq',2,0,3,1,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000150,5000000000000000050,'自定义','custom',NULL,'CUSTOM','{\"en-US\": \"CUSTOM\", \"ja-JP\": \"自定义\", \"ko-KR\": \"CUSTOM\", \"zh-CN\": \"自定义\", \"zh-TW\": \"自定义\"}','encode_rule_segment_type/custom',2,0,4,1,'编码规则段类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000151,5000000000000000051,'永不重置','never',NULL,'0','{\"en-US\": \"0\", \"ja-JP\": \"永不重置\", \"ko-KR\": \"0\", \"zh-CN\": \"永不重置\", \"zh-TW\": \"永不重置\"}','encode_rule_seq_reset_type/never',2,0,1,1,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000152,5000000000000000051,'按年重置','yearly',NULL,'1','{\"en-US\": \"1\", \"ja-JP\": \"按年重置\", \"ko-KR\": \"1\", \"zh-CN\": \"按年重置\", \"zh-TW\": \"按年重置\"}','encode_rule_seq_reset_type/yearly',2,0,2,1,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000153,5000000000000000051,'按月重置','monthly',NULL,'2','{\"en-US\": \"2\", \"ja-JP\": \"按月重置\", \"ko-KR\": \"2\", \"zh-CN\": \"按月重置\", \"zh-TW\": \"按月重置\"}','encode_rule_seq_reset_type/monthly',2,0,3,1,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000154,5000000000000000051,'按日重置','daily',NULL,'3','{\"en-US\": \"3\", \"ja-JP\": \"按日重置\", \"ko-KR\": \"3\", \"zh-CN\": \"按日重置\", \"zh-TW\": \"按日重置\"}','encode_rule_seq_reset_type/daily',2,0,4,1,'编码规则序列重置类型',1993479636925403138,1993479637244170242,'2026-04-13 12:00:03',1993479637244170242,'2026-05-06 17:29:21',0,'{\"color\":\"blue\"}'),(5000000000000000155,0,'审批分类','wf_task_category',NULL,NULL,'{\"en-US\": \"wf_task_category\", \"ja-JP\": \"审批分类\", \"ko-KR\": \"wf_task_category\", \"zh-CN\": \"审批分类\", \"zh-TW\": \"审批分类\"}','wf_task_category',1,0,310,1,'审批任务分类字典',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000156,5000000000000000155,'通用类','general',NULL,'general','{\"en-US\": \"general\", \"ja-JP\": \"通用类\", \"ko-KR\": \"general\", \"zh-CN\": \"通用类\", \"zh-TW\": \"通用类\"}','wf_task_category/general',2,0,1,1,'审批任务分类字典项',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000157,5000000000000000155,'人事类','hr',NULL,'hr','{\"en-US\": \"hr\", \"ja-JP\": \"人事类\", \"ko-KR\": \"hr\", \"zh-CN\": \"人事类\", \"zh-TW\": \"人事类\"}','wf_task_category/hr',2,0,2,1,'审批任务分类字典项',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000158,5000000000000000155,'合同类','contract',NULL,'contract','{\"en-US\": \"contract\", \"ja-JP\": \"合同类\", \"ko-KR\": \"contract\", \"zh-CN\": \"合同类\", \"zh-TW\": \"合同类\"}','wf_task_category/contract',2,0,3,1,'审批任务分类字典项',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000159,5000000000000000155,'财务类','finance',NULL,'finance','{\"en-US\": \"finance\", \"ja-JP\": \"财务类\", \"ko-KR\": \"finance\", \"zh-CN\": \"财务类\", \"zh-TW\": \"财务类\"}','wf_task_category/finance',2,0,4,1,'审批任务分类字典项',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000160,5000000000000000155,'项目类','project',NULL,'project','{\"en-US\": \"project\", \"ja-JP\": \"项目类\", \"ko-KR\": \"project\", \"zh-CN\": \"项目类\", \"zh-TW\": \"项目类\"}','wf_task_category/project',2,0,5,1,'审批任务分类字典项',1993479636925403138,1993479637244170242,'2026-04-20 21:29:27',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000161,0,'用户来源','user_source',NULL,NULL,'{\"zh-CN\":\"用户来源\",\"en-US\":\"User Source\",\"ja-JP\":\"ユーザーソース\",\"ko-KR\":\"사용자 출처\",\"zh-TW\":\"用戶來源\"}','user_source',1,4,101,1,'用户来源字典',1993479636925403138,1993479637244170242,'2026-04-22 19:10:11',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000162,5000000000000000161,'站点创建','site_created',NULL,'1','{\"en-US\": \"Created In Site\", \"ja-JP\": \"サイト作成\", \"ko-KR\": \"사이트 생성\", \"zh-CN\": \"站点创建\", \"zh-TW\": \"站點創建\"}','user_source/site_created',2,0,1,1,'站点内创建',1993479636925403138,1993479637244170242,'2026-04-22 19:10:11',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"blue\"}'),(5000000000000000163,5000000000000000161,'站点导入','site_imported',NULL,'2','{\"en-US\": \"Imported In Site\", \"ja-JP\": \"サイトインポート\", \"ko-KR\": \"사이트 가져오기\", \"zh-CN\": \"站点导入\", \"zh-TW\": \"站點導入\"}','user_source/site_imported',2,0,2,1,'站点内导入',1993479636925403138,1993479637244170242,'2026-04-22 19:10:11',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"gold\"}'),(5000000000000000164,5000000000000000161,'第三方同步','third_party_sync',NULL,'3','{\"en-US\": \"Third Party Sync\", \"ja-JP\": \"サードパーティ同期\", \"ko-KR\": \"타사 동기화\", \"zh-CN\": \"第三方同步\", \"zh-TW\": \"第三方同步\"}','user_source/third_party_sync',2,0,3,1,'第三方系统同步',1993479636925403138,1993479637244170242,'2026-04-22 19:10:11',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"green\"}'),(5000000000000000165,5000000000000000161,'自主注册','self_registered',NULL,'4','{\"en-US\": \"Self Registered\", \"ja-JP\": \"自己登録\", \"ko-KR\": \"자체 등록\", \"zh-CN\": \"自主注册\", \"zh-TW\": \"自主註冊\"}','user_source/self_registered',2,0,4,1,'用户自主注册',1993479636925403138,1993479637244170242,'2026-04-22 19:10:11',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"purple\"}'),(5000000000000000166,0,'供应商合作状态','supplier_cooperation_status',5,NULL,'{\"en-US\": \"supplier_cooperation_status\", \"ja-JP\": \"供应商合作状态\", \"ko-KR\": \"supplier_cooperation_status\", \"zh-CN\": \"供应商合作状态\", \"zh-TW\": \"供应商合作状态\"}','supplier_cooperation_status',1,4,210,1,'供应商主数据合作状态',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000167,0,'供应商信用等级','supplier_credit_level',5,NULL,'{\"en-US\": \"supplier_credit_level\", \"ja-JP\": \"供应商信用等级\", \"ko-KR\": \"supplier_credit_level\", \"zh-CN\": \"供应商信用等级\", \"zh-TW\": \"供应商信用等级\"}','supplier_credit_level',1,4,211,1,'供应商主数据信用等级',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000168,0,'供应商风险等级','supplier_risk_level',5,NULL,'{\"en-US\": \"supplier_risk_level\", \"ja-JP\": \"供应商风险等级\", \"ko-KR\": \"supplier_risk_level\", \"zh-CN\": \"供应商风险等级\", \"zh-TW\": \"供应商风险等级\"}','supplier_risk_level',1,0,212,1,'供应商主数据风险等级；仅建类型，不预置业务值',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000169,0,'供应商分级','supplier_level',5,NULL,'{\"en-US\": \"supplier_level\", \"ja-JP\": \"供应商分级\", \"ko-KR\": \"supplier_level\", \"zh-CN\": \"供应商分级\", \"zh-TW\": \"供应商分级\"}','supplier_level',1,3,213,1,'供应商主数据分级',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000170,0,'供应商审查状态','supplier_review_status',5,NULL,'{\"en-US\": \"supplier_review_status\", \"ja-JP\": \"供应商审查状态\", \"ko-KR\": \"supplier_review_status\", \"zh-CN\": \"供应商审查状态\", \"zh-TW\": \"供应商审查状态\"}','supplier_review_status',1,4,214,1,'供应商资质审查状态',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000171,0,'供应商企业性质','supplier_enterprise_nature',5,NULL,'{\"en-US\": \"supplier_enterprise_nature\", \"ja-JP\": \"供应商企业性质\", \"ko-KR\": \"supplier_enterprise_nature\", \"zh-CN\": \"供应商企业性质\", \"zh-TW\": \"供应商企业性质\"}','supplier_enterprise_nature',1,4,215,1,'供应商企业性质',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000172,0,'供应商行业分类','supplier_industry_category',5,NULL,'{\"en-US\": \"supplier_industry_category\", \"ja-JP\": \"供应商行业分类\", \"ko-KR\": \"supplier_industry_category\", \"zh-CN\": \"供应商行业分类\", \"zh-TW\": \"供应商行业分类\"}','supplier_industry_category',1,0,216,1,'供应商行业分类；仅建类型，不预置业务值',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000173,0,'供应商发票类型','supplier_invoice_type',5,NULL,'{\"en-US\": \"supplier_invoice_type\", \"ja-JP\": \"供应商发票类型\", \"ko-KR\": \"supplier_invoice_type\", \"zh-CN\": \"供应商发票类型\", \"zh-TW\": \"供应商发票类型\"}','supplier_invoice_type',1,0,217,1,'供应商发票类型；仅建类型，不预置业务值',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000174,0,'供应商资质类型','supplier_qualification_type',5,NULL,'{\"en-US\": \"supplier_qualification_type\", \"ja-JP\": \"供应商资质类型\", \"ko-KR\": \"supplier_qualification_type\", \"zh-CN\": \"供应商资质类型\", \"zh-TW\": \"供应商资质类型\"}','supplier_qualification_type',1,0,218,1,'供应商资质类型；仅建类型，不预置业务值',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,NULL),(5000000000000000181,5000000000000000166,'淘汰','eliminated',5,'4','{\"en-US\": \"4\", \"ja-JP\": \"淘汰\", \"ko-KR\": \"4\", \"zh-CN\": \"淘汰\", \"zh-TW\": \"淘汰\"}','supplier_cooperation_status/eliminated',2,0,4,1,'已淘汰',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"red\"}'),(5000000000000000182,5000000000000000166,'正式','formal',5,'2','{\"en-US\": \"2\", \"ja-JP\": \"正式\", \"ko-KR\": \"2\", \"zh-CN\": \"正式\", \"zh-TW\": \"正式\"}','supplier_cooperation_status/formal',2,0,2,1,'正式供应商',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"green\"}'),(5000000000000000183,5000000000000000166,'潜在','potential',5,'1','{\"en-US\": \"1\", \"ja-JP\": \"潜在\", \"ko-KR\": \"1\", \"zh-CN\": \"潜在\", \"zh-TW\": \"潜在\"}','supplier_cooperation_status/potential',2,0,1,1,'潜在供应商',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"blue\"}'),(5000000000000000184,5000000000000000166,'暂停','suspended',5,'3','{\"en-US\": \"3\", \"ja-JP\": \"暂停\", \"ko-KR\": \"3\", \"zh-CN\": \"暂停\", \"zh-TW\": \"暂停\"}','supplier_cooperation_status/suspended',2,0,3,1,'暂停合作',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"orange\"}'),(5000000000000000185,5000000000000000167,'A','credit_a',5,'A','{\"en-US\": \"A\", \"ja-JP\": \"A\", \"ko-KR\": \"A\", \"zh-CN\": \"A\", \"zh-TW\": \"A\"}','supplier_credit_level/credit_a',2,0,1,1,'信用等级 A',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"green\"}'),(5000000000000000186,5000000000000000167,'B','credit_b',5,'B','{\"en-US\": \"B\", \"ja-JP\": \"B\", \"ko-KR\": \"B\", \"zh-CN\": \"B\", \"zh-TW\": \"B\"}','supplier_credit_level/credit_b',2,0,2,1,'信用等级 B',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"blue\"}'),(5000000000000000187,5000000000000000167,'C','credit_c',5,'C','{\"en-US\": \"C\", \"ja-JP\": \"C\", \"ko-KR\": \"C\", \"zh-CN\": \"C\", \"zh-TW\": \"C\"}','supplier_credit_level/credit_c',2,0,3,1,'信用等级 C',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"orange\"}'),(5000000000000000188,5000000000000000167,'D','credit_d',5,'D','{\"en-US\": \"D\", \"ja-JP\": \"D\", \"ko-KR\": \"D\", \"zh-CN\": \"D\", \"zh-TW\": \"D\"}','supplier_credit_level/credit_d',2,0,4,1,'信用等级 D',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"red\"}'),(5000000000000000189,5000000000000000171,'外资','foreign',5,'3','{\"en-US\": \"3\", \"ja-JP\": \"外资\", \"ko-KR\": \"3\", \"zh-CN\": \"外资\", \"zh-TW\": \"外资\"}','supplier_enterprise_nature/foreign',2,0,3,1,'外资企业',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"purple\"}'),(5000000000000000190,5000000000000000171,'合资','joint_venture',5,'4','{\"en-US\": \"4\", \"ja-JP\": \"合资\", \"ko-KR\": \"4\", \"zh-CN\": \"合资\", \"zh-TW\": \"合资\"}','supplier_enterprise_nature/joint_venture',2,0,4,1,'合资企业',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"cyan\"}'),(5000000000000000191,5000000000000000171,'民营','private',5,'2','{\"en-US\": \"2\", \"ja-JP\": \"民营\", \"ko-KR\": \"2\", \"zh-CN\": \"民营\", \"zh-TW\": \"民营\"}','supplier_enterprise_nature/private',2,0,2,1,'民营企业',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"green\"}'),(5000000000000000192,5000000000000000171,'国企','state_owned',5,'1','{\"en-US\": \"1\", \"ja-JP\": \"国企\", \"ko-KR\": \"1\", \"zh-CN\": \"国企\", \"zh-TW\": \"国企\"}','supplier_enterprise_nature/state_owned',2,0,1,1,'国有企业',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"blue\"}'),(5000000000000000193,5000000000000000169,'核心','core',5,'2','{\"en-US\": \"2\", \"ja-JP\": \"核心\", \"ko-KR\": \"2\", \"zh-CN\": \"核心\", \"zh-TW\": \"核心\"}','supplier_level/core',2,0,2,1,'核心供应商',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"blue\"}'),(5000000000000000194,5000000000000000169,'一般','normal',5,'3','{\"en-US\": \"3\", \"ja-JP\": \"一般\", \"ko-KR\": \"3\", \"zh-CN\": \"一般\", \"zh-TW\": \"一般\"}','supplier_level/normal',2,0,3,1,'一般供应商',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"default\"}'),(5000000000000000195,5000000000000000169,'战略','strategic',5,'1','{\"en-US\": \"1\", \"ja-JP\": \"战略\", \"ko-KR\": \"1\", \"zh-CN\": \"战略\", \"zh-TW\": \"战略\"}','supplier_level/strategic',2,0,1,1,'战略供应商',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"purple\"}'),(5000000000000000196,5000000000000000170,'已审查','review_approved',5,'3','{\"en-US\": \"3\", \"ja-JP\": \"已审查\", \"ko-KR\": \"3\", \"zh-CN\": \"已审查\", \"zh-TW\": \"已审查\"}','supplier_review_status/review_approved',2,0,3,1,'已审查',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"green\"}'),(5000000000000000197,5000000000000000170,'无需审查','review_none',5,'0','{\"en-US\": \"0\", \"ja-JP\": \"无需审查\", \"ko-KR\": \"0\", \"zh-CN\": \"无需审查\", \"zh-TW\": \"无需审查\"}','supplier_review_status/review_none',2,0,0,1,'无需审查',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"default\"}'),(5000000000000000198,5000000000000000170,'未审查','review_pending',5,'1','{\"en-US\": \"1\", \"ja-JP\": \"未审查\", \"ko-KR\": \"1\", \"zh-CN\": \"未审查\", \"zh-TW\": \"未审查\"}','supplier_review_status/review_pending',2,0,1,1,'未审查',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"orange\"}'),(5000000000000000199,5000000000000000170,'审查中','review_processing',5,'2','{\"en-US\": \"2\", \"ja-JP\": \"审查中\", \"ko-KR\": \"2\", \"zh-CN\": \"审查中\", \"zh-TW\": \"审查中\"}','supplier_review_status/review_processing',2,0,2,1,'审查中',1993479636925403138,1993479637244170242,'2026-04-26 16:26:06',1993479637244170242,'2026-04-26 20:56:49',0,'{\"color\": \"blue\"}'),(5000000000000000212,0,'调用状态','callStatus',NULL,NULL,'{\"en-US\": \"Call Status\", \"ja-JP\": \"呼び出し状態\", \"ko-KR\": \"호출 상태\", \"zh-CN\": \"调用状态\", \"zh-TW\": \"調用狀態\"}','/callStatus',1,2,205,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000213,0,'调用方式','integrationCallMethod',NULL,NULL,'{\"en-US\": \"Call Method\", \"ja-JP\": \"呼び出し方式\", \"ko-KR\": \"호출 방식\", \"zh-CN\": \"调用方式\", \"zh-TW\": \"調用方式\"}','/integrationCallMethod',1,2,204,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000214,0,'接口调用方向','integrationDirection',NULL,NULL,'{\"en-US\": \"Integration Direction\", \"ja-JP\": \"連携呼び出し方向\", \"ko-KR\": \"연동 호출 방향\", \"zh-CN\": \"接口调用方向\", \"zh-TW\": \"介面調用方向\"}','/integrationDirection',1,2,201,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000215,0,'接口状态','integrationStatus',NULL,NULL,'{\"en-US\": \"Integration Status\", \"ja-JP\": \"連携状態\", \"ko-KR\": \"연동 상태\", \"zh-CN\": \"接口状态\", \"zh-TW\": \"介面狀態\"}','/integrationStatus',1,2,202,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000216,0,'第三方系统状态','thirdSystemStatus',NULL,NULL,'{\"en-US\": \"Third System Status\", \"ja-JP\": \"サードパーティシステム状態\", \"ko-KR\": \"타사 시스템 상태\", \"zh-CN\": \"第三方系统状态\", \"zh-TW\": \"第三方系統狀態\"}','/thirdSystemStatus',1,2,203,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000219,5000000000000000212,'失败','callStatusFailed',NULL,'FAILED','{\"en-US\": \"Failed\", \"ja-JP\": \"失敗\", \"ko-KR\": \"실패\", \"zh-CN\": \"失败\", \"zh-TW\": \"失敗\"}','/callStatus/callStatusFailed',2,0,2,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000220,5000000000000000212,'成功','callStatusSuccess',NULL,'SUCCESS','{\"en-US\": \"Success\", \"ja-JP\": \"成功\", \"ko-KR\": \"성공\", \"zh-CN\": \"成功\", \"zh-TW\": \"成功\"}','/callStatus/callStatusSuccess',2,0,1,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000221,5000000000000000213,'HTTP','integrationCallMethodHttp',NULL,'HTTP','{\"en-US\": \"HTTP\", \"ja-JP\": \"HTTP\", \"ko-KR\": \"HTTP\", \"zh-CN\": \"HTTP\", \"zh-TW\": \"HTTP\"}','/integrationCallMethod/integrationCallMethodHttp',2,0,1,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000222,5000000000000000213,'TCP','integrationCallMethodTcp',NULL,'TCP','{\"en-US\": \"TCP\", \"ja-JP\": \"TCP\", \"ko-KR\": \"TCP\", \"zh-CN\": \"TCP\", \"zh-TW\": \"TCP\"}','/integrationCallMethod/integrationCallMethodTcp',2,0,2,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000223,5000000000000000214,'外调内','integrationDirectionInbound',NULL,'INBOUND','{\"en-US\": \"External to Internal\", \"ja-JP\": \"外部から内部\", \"ko-KR\": \"외부에서 내부\", \"zh-CN\": \"外调内\", \"zh-TW\": \"外調內\"}','/integrationDirection/integrationDirectionInbound',2,0,1,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000224,5000000000000000214,'内调外','integrationDirectionOutbound',NULL,'OUTBOUND','{\"en-US\": \"Internal to External\", \"ja-JP\": \"内部から外部\", \"ko-KR\": \"내부에서 외부\", \"zh-CN\": \"内调外\", \"zh-TW\": \"內調外\"}','/integrationDirection/integrationDirectionOutbound',2,0,2,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000225,5000000000000000215,'停用','integrationStatusDisabled',NULL,'0','{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"停用\", \"zh-TW\": \"停用\"}','/integrationStatus/integrationStatusDisabled',2,0,2,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000226,5000000000000000215,'启用','integrationStatusEnabled',NULL,'1','{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}','/integrationStatus/integrationStatusEnabled',2,0,1,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000227,5000000000000000216,'停用','thirdSystemStatusDisabled',NULL,'0','{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"停用\", \"zh-TW\": \"停用\"}','/thirdSystemStatus/thirdSystemStatusDisabled',2,0,2,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000228,5000000000000000216,'启用','thirdSystemStatusEnabled',NULL,'1','{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}','/thirdSystemStatus/thirdSystemStatusEnabled',2,0,1,1,NULL,0,1993479637244170242,'2026-04-26 21:50:10',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000250,0,'用户来源','user_source',NULL,NULL,'{\"zh-CN\":\"用户来源\",\"en-US\":\"User Source\",\"ja-JP\":\"ユーザーソース\",\"ko-KR\":\"사용자 출처\",\"zh-TW\":\"用戶來源\"}','user_source',1,4,101,1,'用户来源字典',0,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000251,0,'用户来源','user_source',NULL,NULL,'{\"zh-CN\":\"用户来源\",\"en-US\":\"User Source\",\"ja-JP\":\"ユーザーソース\",\"ko-KR\":\"사용자 출처\",\"zh-TW\":\"用戶來源\"}','user_source',1,4,101,1,'用户来源字典',1993479636925403140,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-06 17:29:21',0,NULL),(5000000000000000253,5000000000000000250,'第三方同步','third_party_sync',NULL,'3','{\"en-US\": \"Third Party Sync\", \"ja-JP\": \"サードパーティ同期\", \"ko-KR\": \"타사 동기화\", \"zh-CN\": \"第三方同步\", \"zh-TW\": \"第三方同步\"}','user_source/third_party_sync',2,0,3,1,'第三方系统同步',0,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"green\"}'),(5000000000000000254,5000000000000000250,'站点导入','site_imported',NULL,'2','{\"en-US\": \"Imported In Site\", \"ja-JP\": \"サイトインポート\", \"ko-KR\": \"사이트 가져오기\", \"zh-CN\": \"站点导入\", \"zh-TW\": \"站點導入\"}','user_source/site_imported',2,0,2,1,'站点内导入',0,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"gold\"}'),(5000000000000000255,5000000000000000250,'站点创建','site_created',NULL,'1','{\"en-US\": \"Created In Site\", \"ja-JP\": \"サイト作成\", \"ko-KR\": \"사이트 생성\", \"zh-CN\": \"站点创建\", \"zh-TW\": \"站點創建\"}','user_source/site_created',2,0,1,1,'站点内创建',0,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"blue\"}'),(5000000000000000256,5000000000000000250,'自主注册','self_registered',NULL,'4','{\"en-US\": \"Self Registered\", \"ja-JP\": \"自己登録\", \"ko-KR\": \"자체 등록\", \"zh-CN\": \"自主注册\", \"zh-TW\": \"自主註冊\"}','user_source/self_registered',2,0,4,1,'用户自主注册',0,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"purple\"}'),(5000000000000000257,5000000000000000251,'第三方同步','third_party_sync',NULL,'3','{\"en-US\": \"Third Party Sync\", \"ja-JP\": \"サードパーティ同期\", \"ko-KR\": \"타사 동기화\", \"zh-CN\": \"第三方同步\", \"zh-TW\": \"第三方同步\"}','user_source/third_party_sync',2,0,3,1,'第三方系统同步',1993479636925403140,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"green\"}'),(5000000000000000258,5000000000000000251,'站点导入','site_imported',NULL,'2','{\"en-US\": \"Imported In Site\", \"ja-JP\": \"サイトインポート\", \"ko-KR\": \"사이트 가져오기\", \"zh-CN\": \"站点导入\", \"zh-TW\": \"站點導入\"}','user_source/site_imported',2,0,2,1,'站点内导入',1993479636925403140,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"gold\"}'),(5000000000000000259,5000000000000000251,'站点创建','site_created',NULL,'1','{\"en-US\": \"Created In Site\", \"ja-JP\": \"サイト作成\", \"ko-KR\": \"사이트 생성\", \"zh-CN\": \"站点创建\", \"zh-TW\": \"站點創建\"}','user_source/site_created',2,0,1,1,'站点内创建',1993479636925403140,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"blue\"}'),(5000000000000000260,5000000000000000251,'自主注册','self_registered',NULL,'4','{\"en-US\": \"Self Registered\", \"ja-JP\": \"自己登録\", \"ko-KR\": \"자체 등록\", \"zh-CN\": \"自主注册\", \"zh-TW\": \"自主註冊\"}','user_source/self_registered',2,0,4,1,'用户自主注册',1993479636925403140,1993479637244170242,'2026-05-05 15:42:44',1993479637244170242,'2026-05-08 12:03:51',0,'{\"color\": \"purple\"}');
/*!40000 ALTER TABLE `sys_dict` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_file_record`
--

DROP TABLE IF EXISTS `sys_file_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模块编码',
  `module_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模块名称',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '原始文件名',
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '存储文件名',
  `file_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文件类型/扩展名',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'Content-Type',
  `file_size` bigint NOT NULL DEFAULT '0' COMMENT '文件大小，字节',
  `relative_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '相对路径',
  `access_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '完整访问地址',
  `storage_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '存储类型',
  `storage_config_id` bigint DEFAULT NULL COMMENT '存储配置ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_module_code` (`module_code`) USING BTREE,
  KEY `idx_original_name` (`original_name`) USING BTREE,
  KEY `idx_file_type` (`file_type`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统文件记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_record`
--

LOCK TABLES `sys_file_record` WRITE;
/*!40000 ALTER TABLE `sys_file_record` DISABLE KEYS */;
INSERT INTO `sys_file_record` VALUES (1,'sys-config','系统配置','ede35b9f3d5a4de49822247c302157f81.mp4','fc9a9b98c783483d8a915f2bd8a8de42.mp4','mp4','video/mp4',698029,'fc9a9b98c783483d8a915f2bd8a8de42.mp4','/files/fc9a9b98c783483d8a915f2bd8a8de42.mp4','LOCAL',NULL,1993479636925403138,'2026-05-01 14:58:28','2026-05-01 14:58:28','1993479637244170242','1993479637244170242',0),(2,'sys_config_logo','系统配置Logo','9f3fed37a1e944eda9448bf7e1a9af47.png','31c545dd9edc404ba2cd067cc73949d0.png','png','image/png',61792,'31c545dd9edc404ba2cd067cc73949d0.png','http://192.168.44.1/files/31c545dd9edc404ba2cd067cc73949d0.png','LOCAL',NULL,1993479636925403138,'2026-05-02 16:30:38','2026-05-02 16:30:38','1993479637244170242','1993479637244170242',0),(3,'sys_config_logo','系统配置Logo','1c7ee98348414e9ba80f98dbb031ae46.png','636e0bb093fb47df8f603b23f7b90797.png','png','image/png',75493,'636e0bb093fb47df8f603b23f7b90797.png','http://192.168.44.1/files/636e0bb093fb47df8f603b23f7b90797.png','LOCAL',NULL,1993479636925403138,'2026-05-02 16:30:55','2026-05-02 16:30:55','1993479637244170242','1993479637244170242',0),(4,'sys_config_logo','系统配置Logo','04ea2c9b751e4b2aa15080e764f58c96.png','16e2b1ea1dcd460fa34a0f02331a949e.png','png','image/png',91516,'16e2b1ea1dcd460fa34a0f02331a949e.png','http://192.168.121.1:9000/api/files/16e2b1ea1dcd460fa34a0f02331a949e.png','LOCAL',NULL,1993479636925403138,'2026-05-02 19:10:06','2026-05-02 19:10:06','1993479637244170242','1993479637244170242',0),(5,'sys-config','系统配置','ede35b9f3d5a4de49822247c302157f81.mp4','1522c152ab1f437ba99d572477a6d91e.mp4','mp4','video/mp4',698029,'1522c152ab1f437ba99d572477a6d91e.mp4','http://192.168.121.1:9000/api/files/1522c152ab1f437ba99d572477a6d91e.mp4','LOCAL',NULL,1993479636925403138,'2026-05-02 19:10:21','2026-05-02 19:10:21','1993479637244170242','1993479637244170242',0),(6,'sys_user_avatar','用户头像','0767c88bcfa54a589180a9e634eb3f0e.jpg','fef4cdfc3b304e089ede5102c7369e18.jpg','jpg','image/png',102147,'fef4cdfc3b304e089ede5102c7369e18.jpg','http://192.168.121.1:9000/api/files/fef4cdfc3b304e089ede5102c7369e18.jpg','LOCAL',NULL,1993479636925403138,'2026-05-02 19:10:35','2026-05-02 19:10:35','1993479637244170242','1993479637244170242',0),(7,'sys_config_logo','系统配置Logo','1c7ee98348414e9ba80f98dbb031ae46.png','5b263b2bdfae4ec6b96983feeeba1ba8.png','png','image/png',66696,'5b263b2bdfae4ec6b96983feeeba1ba8.png','http://192.168.121.1:9000/api/files/5b263b2bdfae4ec6b96983feeeba1ba8.png','LOCAL',NULL,1993479636925403138,'2026-05-02 20:31:14','2026-05-02 20:31:14','1993479637244170242','1993479637244170242',0),(8,'sys-config','系统配置','ede35b9f3d5a4de49822247c302157f81.mp4','77ae386c8b694f06b61bda5148e96dde.mp4','mp4','video/mp4',698029,'77ae386c8b694f06b61bda5148e96dde.mp4','http://192.168.121.1:9000/api/files/77ae386c8b694f06b61bda5148e96dde.mp4','LOCAL',NULL,1993479636925403138,'2026-05-02 20:31:22','2026-05-02 20:31:22','1993479637244170242','1993479637244170242',0),(9,'sys_config_logo','系统配置Logo','1c7ee98348414e9ba80f98dbb031ae46.png','22a3f67f220b4dbd903ebc1ac60ef6b6.png','png','image/png',63255,'22a3f67f220b4dbd903ebc1ac60ef6b6.png','http://192.168.121.1:9000/api/files/22a3f67f220b4dbd903ebc1ac60ef6b6.png','LOCAL',NULL,1993479636925403138,'2026-05-02 20:31:34','2026-05-02 20:31:34','1993479637244170242','1993479637244170242',0),(10,'sys-config','系统配置','ede35b9f3d5a4de49822247c302157f81.mp4','56226ed12dd744659c14cba7ba68e092.mp4','mp4','video/mp4',698029,'56226ed12dd744659c14cba7ba68e092.mp4','http://192.168.121.1:9000/api/files/56226ed12dd744659c14cba7ba68e092.mp4','LOCAL',NULL,1993479636925403138,'2026-05-02 20:31:45','2026-05-02 20:31:45','1993479637244170242','1993479637244170242',0),(11,'sys_user_avatar','用户头像','4c1db7e0b9f04e5a9376a84a358e4145.jpg','9030846f35724bc59aeade03f334db99.jpg','jpg','image/png',117142,'9030846f35724bc59aeade03f334db99.jpg','http://192.168.121.1:9000/api/files/9030846f35724bc59aeade03f334db99.jpg','LOCAL',NULL,1993479636925403138,'2026-05-07 08:42:08','2026-05-07 08:42:08','1993479637244170242','1993479637244170242',0),(12,'sys_user_avatar','用户头像','4c1db7e0b9f04e5a9376a84a358e4145.jpg','7ad8237eedee4712b9586dafb05aa394.jpg','jpg','image/png',117379,'7ad8237eedee4712b9586dafb05aa394.jpg','http://192.168.121.1:9000/api/files/7ad8237eedee4712b9586dafb05aa394.jpg','LOCAL',NULL,1993479636925403138,'2026-05-07 08:42:24','2026-05-07 08:42:24','1993479637244170242','1993479637244170242',0),(13,'sys_android_version','安卓版本','app-dev-debug.apk','3ea255bc5a3b497e8fb59e7206eaa6cd.apk','apk','application/vnd.android.package-archive',66423128,'3ea255bc5a3b497e8fb59e7206eaa6cd.apk','http://192.168.121.1:9000/api/sys/files/3ea255bc5a3b497e8fb59e7206eaa6cd.apk','LOCAL',NULL,1993479636925403138,'2026-05-07 10:36:22','2026-05-07 10:36:22','1993479637244170242','1993479637244170242',0),(14,'sys_config_logo','系统配置Logo','1c7ee98348414e9ba80f98dbb031ae46.png','7fc8cbcea33b4280ad667ccdbef8c4e9.png','png','image/png',63158,'7fc8cbcea33b4280ad667ccdbef8c4e9.png','http://192.168.121.1:9000/api/sys/files/7fc8cbcea33b4280ad667ccdbef8c4e9.png','LOCAL',NULL,1993479636925403138,'2026-05-07 22:03:57','2026-05-07 22:03:57','1993479637244170242','1993479637244170242',0),(15,'sys-config','系统配置','0d1f188c7e2f4963910482725c323aea.mp4','13474b01f5214286bf6b4019d227c061.mp4','mp4','video/mp4',720966,'13474b01f5214286bf6b4019d227c061.mp4','http://192.168.121.1:9000/api/sys/files/13474b01f5214286bf6b4019d227c061.mp4','LOCAL',NULL,1993479636925403138,'2026-05-07 22:04:09','2026-05-07 22:04:09','1993479637244170242','1993479637244170242',0),(16,'sys_user_avatar','用户头像','4c1db7e0b9f04e5a9376a84a358e4145.jpg','f67a6d20025643c6984ba7ea1f71ff28.jpg','jpg','image/png',114325,'f67a6d20025643c6984ba7ea1f71ff28.jpg','http://192.168.121.1:9000/api/sys/files/f67a6d20025643c6984ba7ea1f71ff28.jpg','LOCAL',NULL,1993479636925403138,'2026-05-07 22:04:20','2026-05-07 22:04:20','1993479637244170242','1993479637244170242',0),(17,'sys_android_version','安卓版本','3ea255bc5a3b497e8fb59e7206eaa6cd.apk','81deda3768654a059e1015715e821a1d.apk','apk','application/vnd.android.package-archive',66423128,'81deda3768654a059e1015715e821a1d.apk','http://192.168.121.1:9000/api/sys/files/81deda3768654a059e1015715e821a1d.apk','LOCAL',NULL,1993479636925403138,'2026-05-07 22:25:24','2026-05-07 22:25:24','1993479637244170242','1993479637244170242',0),(18,'sys_android_version','安卓版本','3ea255bc5a3b497e8fb59e7206eaa6cd.apk','69bc71b5ad7a4d229604ac7e029c45f8.apk','apk','application/vnd.android.package-archive',66423128,'69bc71b5ad7a4d229604ac7e029c45f8.apk','http://192.168.121.1:9000/api/sys/files/69bc71b5ad7a4d229604ac7e029c45f8.apk','LOCAL',NULL,1993479636925403138,'2026-05-07 22:26:14','2026-05-07 22:26:14','1993479637244170242','1993479637244170242',0),(19,'sys_notice','系统通知图片','image.png','115c1fa130894e15bb2a04b87c383c9c.png','png','image/png',1005704,'115c1fa130894e15bb2a04b87c383c9c.png','http://192.168.121.1:9000/api/sys/files/115c1fa130894e15bb2a04b87c383c9c.png','LOCAL',NULL,1993479636925403138,'2026-05-11 09:58:09','2026-05-11 09:58:09','1993479637244170242','1993479637244170242',0);
/*!40000 ALTER TABLE `sys_file_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_file_storage`
--

DROP TABLE IF EXISTS `sys_file_storage`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_file_storage` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `storage_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储类型(LOCAL/OSS/MINIO)',
  `storage_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '存储名称',
  `config_json` json DEFAULT NULL COMMENT '配置(JSON)',
  `is_default` tinyint NOT NULL DEFAULT '0' COMMENT '是否默认',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1启用,0禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_type` (`tenant_id`,`storage_type`) USING BTREE,
  KEY `idx_tenant_default` (`tenant_id`,`is_default`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='文件存储配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_file_storage`
--

LOCK TABLES `sys_file_storage` WRITE;
/*!40000 ALTER TABLE `sys_file_storage` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_file_storage` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_homepage_component_category`
--

DROP TABLE IF EXISTS `sys_homepage_component_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_homepage_component_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `category_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类编码',
  `category_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_component_category_code` (`tenant_id`,`module_code`,`category_code`) USING BTREE,
  KEY `idx_component_category_module` (`module_code`) USING BTREE,
  KEY `idx_component_category_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='首页组件分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_homepage_component_category`
--

LOCK TABLES `sys_homepage_component_category` WRITE;
/*!40000 ALTER TABLE `sys_homepage_component_category` DISABLE KEYS */;
INSERT INTO `sys_homepage_component_category` VALUES (1,0,'personal_common','通用组件','personal','个人首页通用组件分类','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0);
/*!40000 ALTER TABLE `sys_homepage_component_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_homepage_component_config`
--

DROP TABLE IF EXISTS `sys_homepage_component_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_homepage_component_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `scope_level` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置层级：PUBLIC/TENANT',
  `component_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件编码',
  `component_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件名称',
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件路径',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `use_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '作用说明',
  `default_params_json` json DEFAULT NULL COMMENT '默认参数JSON',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_component_scope_code` (`scope_level`,`tenant_id`,`component_code`) USING BTREE,
  KEY `idx_component_category` (`category_id`) USING BTREE,
  KEY `idx_component_scope_tenant` (`scope_level`,`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='首页组件公共/租户配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_homepage_component_config`
--

LOCK TABLES `sys_homepage_component_config` WRITE;
/*!40000 ALTER TABLE `sys_homepage_component_config` DISABLE KEYS */;
INSERT INTO `sys_homepage_component_config` VALUES (1,0,1,'PUBLIC','commonMenus','常用菜单','commonMenus','AppstoreOutlined','系统自动统计的固定 Top 6 菜单','{\"limit\": 6, \"showMore\": true}',1,10,'首页常用菜单卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(2,0,1,'PUBLIC','myFavorites','我的收藏','myFavorites','StarOutlined','我主动收藏的快捷入口','{\"limit\": 6, \"showMore\": true}',1,20,'首页收藏卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(3,0,1,'PUBLIC','pendingApprovals','我收到的审批','pendingApprovals','CheckCircleOutlined','我收到的审批待办','{\"limit\": 6, \"showMore\": true}',1,30,'审批待办卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(4,0,1,'PUBLIC','calendar','日历','calendar','CalendarOutlined','本地日历视图','{}',1,40,'日历卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(5,0,1,'PUBLIC','messages','我收到的消息','messages','MessageOutlined','用户发给我的站内消息','{\"limit\": 10, \"showMore\": true}',1,50,'消息卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(6,0,1,'PUBLIC','notices','系统通知','notices','BellOutlined','审批与系统类通知','{\"limit\": 10, \"showMore\": true}',1,60,'系统通知卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(7,0,1,'PUBLIC','currentTime','当前时间','currentTime','ClockCircleOutlined','当前日期与时间','{}',1,70,'时间卡片','2026-05-15 18:39:01','2026-05-15 18:39:01','codex','codex',0),(8,1993479636925403138,1,'TENANT','commonMenus','常用菜单','commonMenus','AppstoreOutlined','系统自动统计的固定 Top 6 菜单','{\"limit\": 6, \"showMore\": true}',1,10,'首页常用菜单卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(9,1993479636925403138,1,'TENANT','myFavorites','我的收藏','myFavorites','StarOutlined','我主动收藏的快捷入口','{\"limit\": 6, \"showMore\": true}',1,20,'首页收藏卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(10,1993479636925403138,1,'TENANT','pendingApprovals','我收到的审批','pendingApprovals','CheckCircleOutlined','我收到的审批待办','{\"limit\": 6, \"showMore\": true}',1,30,'审批待办卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(11,1993479636925403138,1,'TENANT','calendar','日历','calendar','CalendarOutlined','本地日历视图','{}',1,40,'日历卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(12,1993479636925403138,1,'TENANT','messages','我收到的消息','messages','MessageOutlined','用户发给我的站内消息','{\"limit\": 10, \"showMore\": true}',1,50,'消息卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(13,1993479636925403138,1,'TENANT','notices','系统通知','notices','BellOutlined','审批与系统类通知','{\"limit\": 10, \"showMore\": true}',1,60,'系统通知卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0),(14,1993479636925403138,1,'TENANT','currentTime','当前时间','currentTime','ClockCircleOutlined','当前日期与时间','{}',1,70,'时间卡片','2026-05-15 21:19:34','2026-05-15 21:19:34','1993479637244170242','1993479637244170242',0);
/*!40000 ALTER TABLE `sys_homepage_component_config` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_homepage_component_preference`
--

DROP TABLE IF EXISTS `sys_homepage_component_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_homepage_component_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `source_component_id` bigint DEFAULT NULL COMMENT '来源组件ID',
  `component_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件编码快照',
  `component_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件名称快照',
  `component_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组件路径快照',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标快照',
  `use_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '作用说明快照',
  `favorite` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否收藏',
  `removed` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否移除',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序号',
  `params_json` json DEFAULT NULL COMMENT '个人参数JSON',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_component_preference_code` (`tenant_id`,`user_id`,`component_code`) USING BTREE,
  KEY `idx_component_preference_category` (`category_id`) USING BTREE,
  KEY `idx_component_preference_user` (`tenant_id`,`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='首页组件个人偏好表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_homepage_component_preference`
--

LOCK TABLES `sys_homepage_component_preference` WRITE;
/*!40000 ALTER TABLE `sys_homepage_component_preference` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_homepage_component_preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_invite_code`
--

DROP TABLE IF EXISTS `sys_invite_code`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_invite_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `invite_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邀请码（唯一）',
  `department_id` bigint NOT NULL COMMENT '归属部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '归属职位ID（可选）',
  `role_id` bigint DEFAULT NULL COMMENT '注册后绑定角色ID',
  `expire_time` datetime NOT NULL COMMENT '失效时间',
  `max_register_count` int NOT NULL DEFAULT '1' COMMENT '最大注册人数',
  `used_count` int NOT NULL DEFAULT '0' COMMENT '已注册人数',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1=启用 0=停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_invite_code` (`invite_code`) USING BTREE,
  KEY `idx_department_id` (`department_id`) USING BTREE,
  KEY `idx_position_id` (`position_id`) USING BTREE,
  KEY `idx_expire_time` (`expire_time`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='邀请码主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_invite_code`
--

LOCK TABLES `sys_invite_code` WRITE;
/*!40000 ALTER TABLE `sys_invite_code` DISABLE KEYS */;
INSERT INTO `sys_invite_code` VALUES (1,1993479636925403138,'52B7ED4C',1,12,NULL,'2026-04-17 11:35:30',10,0,1,NULL,'1993479637244170242','2026-04-17 11:35:38','1993479637244170242','2026-04-17 11:35:38',1);
/*!40000 ALTER TABLE `sys_invite_code` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_invite_register_record`
--

DROP TABLE IF EXISTS `sys_invite_register_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_invite_register_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `invite_id` bigint NOT NULL COMMENT '邀请码主表ID',
  `invite_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冗余邀请码',
  `user_id` bigint NOT NULL COMMENT '注册成功用户ID',
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '注册账号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名',
  `department_id` bigint DEFAULT NULL COMMENT '注册落入部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '注册落入职位ID',
  `role_id` bigint DEFAULT NULL COMMENT '注册绑定角色ID',
  `register_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '注册IP',
  `register_region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '注册地区',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  `status` int NOT NULL DEFAULT '1' COMMENT '注册结果状态：1=成功 0=失败',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_invite_id` (`invite_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_register_time` (`register_time`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='邀请注册关系记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_invite_register_record`
--

LOCK TABLES `sys_invite_register_record` WRITE;
/*!40000 ALTER TABLE `sys_invite_register_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_invite_register_record` ENABLE KEYS */;
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
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Cron表达式',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标 (方法路径)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='定时任务表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_job`
--

LOCK TABLES `sys_job` WRITE;
/*!40000 ALTER TABLE `sys_job` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_job` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_menu`
--

DROP TABLE IF EXISTS `sys_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `tenant_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'PUBLIC' COMMENT 'Applicable tenant type: MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC; PUBLIC means all tenant types',
  `module_id` bigint DEFAULT NULL COMMENT '所属模块ID',
  `parent_id` bigint DEFAULT NULL COMMENT '父级菜单ID',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：module/catalog/menu/button',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '路由路径',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单名称',
  `name_i18n_json` json DEFAULT NULL COMMENT '菜单名称国际化(JSON)',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `component_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '前端组件Key',
  `perm_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '按钮权限标识',
  `order_num` int DEFAULT NULL COMMENT '排序号',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可见：1可见 0隐藏',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0未删除 1已删除',
  `menu_level` int DEFAULT '1' COMMENT '菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单',
  `menu_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'embedded' COMMENT '菜单模式：embedded=内嵌，external=外联',
  `external_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '外联URL（当menu_mode=external时使用）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_menu_tenant` (`tenant_id`) USING BTREE,
  KEY `idx_sys_menu_module` (`module_id`) USING BTREE,
  KEY `idx_sys_menu_parent` (`parent_id`) USING BTREE,
  KEY `idx_menu_mode` (`menu_mode`) USING BTREE,
  KEY `idx_tenant_type` (`tenant_type`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3000000000000000735 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统菜单/目录/按钮表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_menu`
--

LOCK TABLES `sys_menu` WRITE;
/*!40000 ALTER TABLE `sys_menu` DISABLE KEYS */;
INSERT INTO `sys_menu` VALUES (1,1993479636925403138,'PUBLIC',1,652,'menu','user','用户管理','{\"en-US\": \"Users\", \"ja-JP\": \"ユーザー管理\", \"ko-KR\": \"사용자 관리\", \"zh-CN\": \"用户管理\", \"zh-TW\": \"用戶管理\"}','UserOutlined','SystemUser','sys:user:view',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(2,1993479636925403138,'PUBLIC',1,653,'menu','role','角色管理','{\"en-US\": \"Roles\", \"ja-JP\": \"ロール管理\", \"ko-KR\": \"역할 관리\", \"zh-CN\": \"角色管理\", \"zh-TW\": \"角色管理\"}','TeamOutlined','SystemRole','sys:role:view',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(3,1993479636925403138,'PUBLIC',1,0,'menu','module','模块管理','{\"en-US\": \"Modules\", \"ja-JP\": \"モジュール管理\", \"ko-KR\": \"모듈 관리\", \"zh-CN\": \"模块管理\", \"zh-TW\": \"模塊管理\"}','AppstoreOutlined','SystemModule','sys:module:view',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(4,1993479636925403138,'PUBLIC',1,653,'menu','menu','菜单管理','{\"en-US\": \"Menus\", \"ja-JP\": \"メニュー管理\", \"ko-KR\": \"메뉴 관리\", \"zh-CN\": \"菜单管理\", \"zh-TW\": \"選單管理\"}','MenuOutlined','SystemMenu','sys:menu:view',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(5,1993479636925403138,'PUBLIC',1,652,'menu','department','部门管理','{\"en-US\": \"Departments\", \"ja-JP\": \"部門管理\", \"ko-KR\": \"부서 관리\", \"zh-CN\": \"部门管理\", \"zh-TW\": \"部門管理\"}','ApartmentOutlined','SystemDepartment','sys:dept:view',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(6,1993479636925403138,'PUBLIC',1,652,'menu','position','职位管理','{\"en-US\": \"Positions\", \"ja-JP\": \"職位管理\", \"ko-KR\": \"직위 관리\", \"zh-CN\": \"职位管理\", \"zh-TW\": \"職位管理\"}','IdcardOutlined','SystemPosition','sys:position:view',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(101,1993479636925403138,'PUBLIC',1,1,'button',NULL,'新增用户','{\"en-US\": \"Add User\", \"ja-JP\": \"ユーザー追加\", \"ko-KR\": \"사용자 추가\", \"zh-CN\": \"新增用户\", \"zh-TW\": \"新增用戶\"}',NULL,NULL,'sys:user:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(102,1993479636925403138,'PUBLIC',1,1,'button',NULL,'编辑用户','{\"en-US\": \"Edit User\", \"ja-JP\": \"ユーザー編集\", \"ko-KR\": \"사용자 편집\", \"zh-CN\": \"编辑用户\", \"zh-TW\": \"編輯用戶\"}',NULL,NULL,'sys:user:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(103,1993479636925403138,'PUBLIC',1,1,'button',NULL,'删除用户','{\"en-US\": \"Delete User\", \"ja-JP\": \"ユーザー削除\", \"ko-KR\": \"사용자 삭제\", \"zh-CN\": \"删除用户\", \"zh-TW\": \"刪除用戶\"}',NULL,NULL,'sys:user:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(104,1993479636925403138,'PUBLIC',1,1,'button',NULL,'批量删除用户','{\"en-US\": \"Batch Delete Users\", \"ja-JP\": \"一括削除ユーザー\", \"ko-KR\": \"일괄 삭제 사용자\", \"zh-CN\": \"批量删除用户\", \"zh-TW\": \"批量刪除用戶\"}',NULL,NULL,'sys:user:batchDelete',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(105,1993479636925403138,'PUBLIC',1,1,'button',NULL,'重置密码','{\"en-US\": \"Reset Password\", \"ja-JP\": \"パスワードリセット\", \"ko-KR\": \"비밀번호 재설정\", \"zh-CN\": \"重置密码\", \"zh-TW\": \"重置密碼\"}',NULL,NULL,'sys:user:resetPwd',5,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(106,1993479636925403138,'PUBLIC',1,1,'button',NULL,'导出用户','{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}',NULL,NULL,'sys:user:export',6,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(201,1993479636925403138,'PUBLIC',1,2,'button',NULL,'新增角色','{\"en-US\": \"Add Role\", \"ja-JP\": \"ロール追加\", \"ko-KR\": \"역할 추가\", \"zh-CN\": \"新增角色\", \"zh-TW\": \"新增角色\"}',NULL,NULL,'sys:role:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(202,1993479636925403138,'PUBLIC',1,2,'button',NULL,'编辑角色','{\"en-US\": \"Edit Role\", \"ja-JP\": \"ロール編集\", \"ko-KR\": \"역할 편집\", \"zh-CN\": \"编辑角色\", \"zh-TW\": \"編輯角色\"}',NULL,NULL,'sys:role:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(203,1993479636925403138,'PUBLIC',1,2,'button',NULL,'删除角色','{\"en-US\": \"Delete Role\", \"ja-JP\": \"ロール削除\", \"ko-KR\": \"역할 삭제\", \"zh-CN\": \"删除角色\", \"zh-TW\": \"刪除角色\"}',NULL,NULL,'sys:role:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(204,1993479636925403138,'PUBLIC',1,2,'button',NULL,'批量删除角色','{\"en-US\": \"Batch Delete Roles\", \"ja-JP\": \"一括削除ロール\", \"ko-KR\": \"일괄 삭제 역할\", \"zh-CN\": \"批量删除角色\", \"zh-TW\": \"批量刪除角色\"}',NULL,NULL,'sys:role:batchDelete',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(205,1993479636925403138,'PUBLIC',1,2,'button',NULL,'菜单授权','{\"en-US\": \"Menu Authorization\", \"ja-JP\": \"メニュー認可\", \"ko-KR\": \"메뉴 인증\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"選單授權\"}',NULL,NULL,'sys:role:authMenu',5,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(301,1993479636925403138,'PUBLIC',1,3,'button',NULL,'新增模块','{\"en-US\": \"Add Module\", \"ja-JP\": \"モジュール追加\", \"ko-KR\": \"모듈 추가\", \"zh-CN\": \"新增模块\", \"zh-TW\": \"新增模塊\"}',NULL,NULL,'sys:module:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(302,1993479636925403138,'PUBLIC',1,3,'button',NULL,'编辑模块','{\"en-US\": \"Edit Module\", \"ja-JP\": \"モジュール編集\", \"ko-KR\": \"모듈 편집\", \"zh-CN\": \"编辑模块\", \"zh-TW\": \"編輯模塊\"}',NULL,NULL,'sys:module:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(303,1993479636925403138,'PUBLIC',1,3,'button',NULL,'删除模块','{\"en-US\": \"Delete Module\", \"ja-JP\": \"モジュール削除\", \"ko-KR\": \"모듈 삭제\", \"zh-CN\": \"删除模块\", \"zh-TW\": \"刪除模塊\"}',NULL,NULL,'sys:module:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(304,1993479636925403138,'PUBLIC',1,3,'button',NULL,'批量删除模块','{\"en-US\": \"Batch Delete Modules\", \"ja-JP\": \"一括削除モジュール\", \"ko-KR\": \"일괄 삭제 모듈\", \"zh-CN\": \"批量删除模块\", \"zh-TW\": \"批量刪除模塊\"}',NULL,NULL,'sys:module:batchDelete',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(401,1993479636925403138,'PUBLIC',1,4,'button',NULL,'新增菜单','{\"en-US\": \"Add Menu\", \"ja-JP\": \"メニュー追加\", \"ko-KR\": \"메뉴 추가\", \"zh-CN\": \"新增菜单\", \"zh-TW\": \"新增選單\"}',NULL,NULL,'sys:menu:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(402,1993479636925403138,'PUBLIC',1,4,'button',NULL,'编辑菜单','{\"en-US\": \"Edit Menu\", \"ja-JP\": \"メニュー編集\", \"ko-KR\": \"메뉴 편집\", \"zh-CN\": \"编辑菜单\", \"zh-TW\": \"編輯選單\"}',NULL,NULL,'sys:menu:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(403,1993479636925403138,'PUBLIC',1,4,'button',NULL,'删除菜单','{\"en-US\": \"Delete Menu\", \"ja-JP\": \"メニュー削除\", \"ko-KR\": \"메뉴 삭제\", \"zh-CN\": \"删除菜单\", \"zh-TW\": \"刪除選單\"}',NULL,NULL,'sys:menu:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(404,1993479636925403138,'PUBLIC',1,4,'button',NULL,'批量删除菜单','{\"en-US\": \"Batch Delete Menus\", \"ja-JP\": \"一括削除メニュー\", \"ko-KR\": \"일괄 삭제 메뉴\", \"zh-CN\": \"批量删除菜单\", \"zh-TW\": \"批量刪除選單\"}',NULL,NULL,'sys:menu:batchDelete',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(501,1993479636925403138,'PUBLIC',1,5,'button',NULL,'新增部门','{\"en-US\": \"Add Department\", \"ja-JP\": \"部門追加\", \"ko-KR\": \"부서 추가\", \"zh-CN\": \"新增部门\", \"zh-TW\": \"新增部門\"}',NULL,NULL,'sys:dept:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(502,1993479636925403138,'PUBLIC',1,5,'button',NULL,'编辑部门','{\"en-US\": \"Edit Department\", \"ja-JP\": \"部門編集\", \"ko-KR\": \"부서 편집\", \"zh-CN\": \"编辑部门\", \"zh-TW\": \"編輯部門\"}',NULL,NULL,'sys:dept:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(503,1993479636925403138,'PUBLIC',1,5,'button',NULL,'删除部门','{\"en-US\": \"Delete Department\", \"ja-JP\": \"部門削除\", \"ko-KR\": \"부서 삭제\", \"zh-CN\": \"删除部门\", \"zh-TW\": \"刪除部門\"}',NULL,NULL,'sys:dept:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(601,1993479636925403138,'PUBLIC',1,6,'button',NULL,'新增职位','{\"en-US\": \"Add Position\", \"ja-JP\": \"職位追加\", \"ko-KR\": \"직위 추가\", \"zh-CN\": \"新增职位\", \"zh-TW\": \"新增職位\"}',NULL,NULL,'sys:position:add',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(602,1993479636925403138,'PUBLIC',1,6,'button',NULL,'编辑职位','{\"en-US\": \"Edit Position\", \"ja-JP\": \"職位編集\", \"ko-KR\": \"직위 편집\", \"zh-CN\": \"编辑职位\", \"zh-TW\": \"編輯職位\"}',NULL,NULL,'sys:position:edit',2,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(603,1993479636925403138,'PUBLIC',1,6,'button',NULL,'删除职位','{\"en-US\": \"Delete Position\", \"ja-JP\": \"職位削除\", \"ko-KR\": \"직위 삭제\", \"zh-CN\": \"删除职位\", \"zh-TW\": \"刪除職位\"}',NULL,NULL,'sys:position:delete',3,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(604,1993479636925403138,'PUBLIC',1,6,'button',NULL,'批量删除职位','{\"en-US\": \"Batch Delete Positions\", \"ja-JP\": \"一括削除職位\", \"ko-KR\": \"일괄 삭제 직위\", \"zh-CN\": \"批量删除职位\", \"zh-TW\": \"批量刪除職位\"}',NULL,NULL,'sys:position:batchDelete',4,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(606,1993479636925403138,'PUBLIC',1,0,'menu','dashboard','系统管理主页','{\"en-US\": \"Dashboard\", \"ja-JP\": \"ダッシュボード\", \"ko-KR\": \"대시보드\", \"zh-CN\": \"系统管理主页\", \"zh-TW\": \"系統管理主頁\"}','DashboardOutlined','SystemDashboard','sys:dashboard:view',1,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(607,1993479636925403138,'PUBLIC',1,3000000000000000422,'menu','excelExportConfig','导出配置','{\"en-US\": \"Export Config\", \"ja-JP\": \"エクスポート設定\", \"ko-KR\": \"내보내기 설정\", \"zh-CN\": \"导出配置\", \"zh-TW\": \"匯出設定\"}','FileExcelOutlined','SystemExcelExportConfig','sys:excel:exportConfig:view',20,1,1,'2026-01-14 15:09:12','1993479637244170242','2026-05-06 17:29:21','codex',0,2,'embedded',NULL),(608,1993479636925403138,'PUBLIC',1,3000000000000000422,'menu','excelImportConfig','导入配置','{\"en-US\": \"Import Config\", \"ja-JP\": \"インポート設定\", \"ko-KR\": \"가져오기 설정\", \"zh-CN\": \"导入配置\", \"zh-TW\": \"匯入設定\"}','FileExcelOutlined','SystemExcelImportConfig','sys:excel:importConfig:view',10,1,1,'2026-01-14 15:09:24','1993479637244170242','2026-05-06 17:29:21','codex',0,2,'embedded',NULL),(609,1993479636925403138,'PUBLIC',1,607,'button',NULL,'查看导出配置','{\"en-US\": \"View Export Config\", \"ja-JP\": \"エクスポート設定表示\", \"ko-KR\": \"내보내기 설정 보기\", \"zh-CN\": \"查看导出配置\", \"zh-TW\": \"查看匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:list',1,1,1,'2026-01-14 15:09:30','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(610,1993479636925403138,'PUBLIC',1,607,'button',NULL,'编辑导出配置','{\"en-US\": \"Edit Export Config\", \"ja-JP\": \"エクスポート設定編集\", \"ko-KR\": \"내보내기 설정 편집\", \"zh-CN\": \"编辑导出配置\", \"zh-TW\": \"編輯匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:edit',2,1,1,'2026-01-14 15:09:41','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(611,1993479636925403138,'PUBLIC',1,607,'button',NULL,'删除导出配置','{\"en-US\": \"Delete Export Config\", \"ja-JP\": \"エクスポート設定削除\", \"ko-KR\": \"내보내기 설정 삭제\", \"zh-CN\": \"删除导出配置\", \"zh-TW\": \"刪除匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:delete',3,1,1,'2026-01-14 15:09:47','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(612,1993479636925403138,'PUBLIC',1,608,'button',NULL,'查看导入配置','{\"en-US\": \"View Import Config\", \"ja-JP\": \"インポート設定表示\", \"ko-KR\": \"가져오기 설정 보기\", \"zh-CN\": \"查看导入配置\", \"zh-TW\": \"查看匯入設定\"}',NULL,NULL,'sys:excel:importConfig:list',1,1,1,'2026-01-14 15:09:57','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(613,1993479636925403138,'PUBLIC',1,608,'button',NULL,'编辑导入配置','{\"en-US\": \"Edit Import Config\", \"ja-JP\": \"インポート設定編集\", \"ko-KR\": \"가져오기 설정 편집\", \"zh-CN\": \"编辑导入配置\", \"zh-TW\": \"編輯匯入設定\"}',NULL,NULL,'sys:excel:importConfig:edit',2,1,1,'2026-01-14 15:10:05','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(614,1993479636925403138,'PUBLIC',1,608,'button',NULL,'删除导入配置','{\"en-US\": \"Delete Import Config\", \"ja-JP\": \"インポート設定削除\", \"ko-KR\": \"가져오기 설정 삭제\", \"zh-CN\": \"删除导入配置\", \"zh-TW\": \"刪除匯入設定\"}',NULL,NULL,'sys:excel:importConfig:delete',3,1,1,'2026-01-14 15:10:13','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(615,1993479636925403138,'PUBLIC',1,608,'button',NULL,'下载导入模板','{\"en-US\": \"Download Import Template\", \"ja-JP\": \"インポートテンプレートダウンロード\", \"ko-KR\": \"가져오기 템플릿 다운로드\", \"zh-CN\": \"下载导入模板\", \"zh-TW\": \"下載匯入模板\"}',NULL,NULL,'sys:excel:template:download',10,1,1,'2026-01-14 15:10:21','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(616,1993479636925403138,'PUBLIC',1,607,'button',NULL,'导出登录日志','{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}',NULL,NULL,'sys:excel:export:loginLog',11,1,1,'2026-01-14 15:10:31','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(617,1993479636925403138,'PUBLIC',1,607,'button',NULL,'导出用户','{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}',NULL,NULL,'sys:excel:export:user',12,1,1,'2026-01-14 15:10:41','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(619,1993479636925403138,'PUBLIC',1,1,'button',NULL,'分配角色','{\"en-US\": \"Assign Roles\", \"ja-JP\": \"ロール割り当て\", \"ko-KR\": \"역할 할당\", \"zh-CN\": \"分配角色\", \"zh-TW\": \"分配角色\"}',NULL,NULL,'sys:user:assignRole',7,1,1,'2026-01-14 16:08:56','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(620,1993479636925403138,'PUBLIC',1,0,'menu','dict','字典管理','{\"en-US\": \"Dictionary\", \"ja-JP\": \"辞書管理\", \"ko-KR\": \"사전 관리\", \"zh-CN\": \"字典管理\", \"zh-TW\": \"字典管理\"}','BookOutlined','SystemDict','sys:dict:view',7,1,1,'2026-01-16 11:49:59','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(621,1993479636925403138,'PUBLIC',1,620,'button',NULL,'新增字典','{\"en-US\": \"Add Dictionary\", \"ja-JP\": \"辞書追加\", \"ko-KR\": \"사전 추가\", \"zh-CN\": \"新增字典\", \"zh-TW\": \"新增字典\"}',NULL,NULL,'sys:dict:add',1,1,1,'2026-01-16 11:50:07','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(622,1993479636925403138,'PUBLIC',1,620,'button',NULL,'编辑字典','{\"en-US\": \"Edit Dictionary\", \"ja-JP\": \"辞書編集\", \"ko-KR\": \"사전 편집\", \"zh-CN\": \"编辑字典\", \"zh-TW\": \"編輯字典\"}',NULL,NULL,'sys:dict:edit',2,1,1,'2026-01-16 11:50:07','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(623,1993479636925403138,'PUBLIC',1,620,'button',NULL,'删除字典','{\"en-US\": \"Delete Dictionary\", \"ja-JP\": \"辞書削除\", \"ko-KR\": \"사전 삭제\", \"zh-CN\": \"删除字典\", \"zh-TW\": \"刪除字典\"}',NULL,NULL,'sys:dict:delete',3,1,1,'2026-01-16 11:50:07','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(624,1993479636925403138,'PUBLIC',1,620,'button',NULL,'批量删除字典','{\"en-US\": \"Batch Delete Dictionaries\", \"ja-JP\": \"一括削除辞書\", \"ko-KR\": \"일괄 삭제 사전\", \"zh-CN\": \"批量删除字典\", \"zh-TW\": \"批量刪除字典\"}',NULL,NULL,'sys:dict:batchDelete',4,1,1,'2026-01-16 11:50:07','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(625,1993479636925403138,'PUBLIC',1,620,'button',NULL,'导出字典','{\"en-US\": \"Export Dictionary\", \"ja-JP\": \"辞書エクスポート\", \"ko-KR\": \"사전 내보내기\", \"zh-CN\": \"导出字典\", \"zh-TW\": \"匯出字典\"}',NULL,NULL,'sys:dict:export',5,1,1,'2026-01-16 11:50:07','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(626,1993479636925403138,'PUBLIC',1,0,'menu','dictType','字典类型管理','{\"en-US\": \"Dict Types\", \"ja-JP\": \"辞書タイプ\", \"ko-KR\": \"사전 유형\", \"zh-CN\": \"字典类型管理\", \"zh-TW\": \"字典類型管理\"}','TagsOutlined','SystemDictType','sys:dictType:view',100,0,1,'2026-01-16 11:50:11','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(627,1993479636925403138,'PUBLIC',1,626,'button',NULL,'新增字典类型','{\"en-US\": \"Add Dict Type\", \"ja-JP\": \"辞書タイプ追加\", \"ko-KR\": \"사전 유형 추가\", \"zh-CN\": \"新增字典类型\", \"zh-TW\": \"新增字典類型\"}',NULL,NULL,'sys:dictType:add',1,1,1,'2026-01-16 11:50:24','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(628,1993479636925403138,'PUBLIC',1,626,'button',NULL,'编辑字典类型','{\"en-US\": \"Edit Dict Type\", \"ja-JP\": \"辞書タイプ編集\", \"ko-KR\": \"사전 유형 편집\", \"zh-CN\": \"编辑字典类型\", \"zh-TW\": \"編輯字典類型\"}',NULL,NULL,'sys:dictType:edit',2,1,1,'2026-01-16 11:50:24','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(629,1993479636925403138,'PUBLIC',1,626,'button',NULL,'删除字典类型','{\"en-US\": \"Delete Dict Type\", \"ja-JP\": \"辞書タイプ削除\", \"ko-KR\": \"사전 유형 삭제\", \"zh-CN\": \"删除字典类型\", \"zh-TW\": \"刪除字典類型\"}',NULL,NULL,'sys:dictType:delete',3,1,1,'2026-01-16 11:50:24','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(630,1993479636925403138,'PUBLIC',1,626,'button',NULL,'批量删除字典类型','{\"en-US\": \"Batch Delete Dict Types\", \"ja-JP\": \"一括削除辞書タイプ\", \"ko-KR\": \"일괄 삭제 사전 유형\", \"zh-CN\": \"批量删除字典类型\", \"zh-TW\": \"批量刪除字典類型\"}',NULL,NULL,'sys:dictType:batchDelete',4,1,1,'2026-01-16 11:50:24','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(631,1993479636925403138,'PUBLIC',1,626,'button',NULL,'导出字典类型','{\"en-US\": \"Export Dict Types\", \"ja-JP\": \"辞書タイプエクスポート\", \"ko-KR\": \"사전 유형 내보내기\", \"zh-CN\": \"导出字典类型\", \"zh-TW\": \"匯出字典類型\"}',NULL,NULL,'sys:dictType:export',5,1,1,'2026-01-16 11:50:24','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(632,1993479636925403138,'PUBLIC',1,3000000000000000423,'menu','tableConfig','表格配置','{\"en-US\": \"Table Config\", \"ja-JP\": \"テーブル設定\", \"ko-KR\": \"테이블 설정\", \"zh-CN\": \"表格配置\", \"zh-TW\": \"表格設定\"}','TableOutlined','SystemTableConfig','sys:tableConfig:view',10,1,1,'2026-01-16 11:50:37','1993479637244170242','2026-05-06 17:29:21','codex',0,2,'embedded',NULL),(633,1993479636925403138,'PUBLIC',1,632,'button',NULL,'新增表格配置','{\"en-US\": \"Add Table Config\", \"ja-JP\": \"テーブル設定追加\", \"ko-KR\": \"테이블 설정 추가\", \"zh-CN\": \"新增表格配置\", \"zh-TW\": \"新增表格設定\"}',NULL,NULL,'sys:tableConfig:add',1,1,1,'2026-01-16 11:50:48','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(634,1993479636925403138,'PUBLIC',1,632,'button',NULL,'编辑表格配置','{\"en-US\": \"Edit Table Config\", \"ja-JP\": \"テーブル設定編集\", \"ko-KR\": \"테이블 설정 편집\", \"zh-CN\": \"编辑表格配置\", \"zh-TW\": \"編輯表格設定\"}',NULL,NULL,'sys:tableConfig:edit',2,1,1,'2026-01-16 11:50:48','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(635,1993479636925403138,'PUBLIC',1,632,'button',NULL,'删除表格配置','{\"en-US\": \"Delete Table Config\", \"ja-JP\": \"テーブル設定削除\", \"ko-KR\": \"테이블 설정 삭제\", \"zh-CN\": \"删除表格配置\", \"zh-TW\": \"刪除表格設定\"}',NULL,NULL,'sys:tableConfig:delete',3,1,1,'2026-01-16 11:50:48','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(636,1993479636925403138,'PUBLIC',1,632,'button',NULL,'批量删除表格配置','{\"en-US\": \"Batch Delete Table Configs\", \"ja-JP\": \"一括削除テーブル設定\", \"ko-KR\": \"일괄 삭제 테이블 설정\", \"zh-CN\": \"批量删除表格配置\", \"zh-TW\": \"批量刪除表格設定\"}',NULL,NULL,'sys:tableConfig:batchDelete',4,1,1,'2026-01-16 11:50:48','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(637,1993479636925403138,'PUBLIC',1,632,'button',NULL,'导出表格配置','{\"en-US\": \"Export Table Config\", \"ja-JP\": \"テーブル設定エクスポート\", \"ko-KR\": \"테이블 설정 내보내기\", \"zh-CN\": \"导出表格配置\", \"zh-TW\": \"匯出表格設定\"}',NULL,NULL,'sys:tableConfig:export',5,1,1,'2026-01-16 11:50:48','1993479637244170242','2026-05-06 17:29:21','codex',0,3,'embedded',NULL),(638,1993479636925403138,'PUBLIC',1,0,'menu','loginLog','登录日志','{\"en-US\": \"Login Logs\", \"ja-JP\": \"ログインログ\", \"ko-KR\": \"로그인 로그\", \"zh-CN\": \"登录日志\", \"zh-TW\": \"登錄日誌\"}','FileTextOutlined','SystemLoginLog','sys:loginLog:view',9,1,1,'2026-01-16 11:51:14','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(639,1993479636925403138,'PUBLIC',1,638,'button',NULL,'删除登录日志','{\"en-US\": \"Delete Login Log\", \"ja-JP\": \"ログインログ削除\", \"ko-KR\": \"로그인 로그 삭제\", \"zh-CN\": \"删除登录日志\", \"zh-TW\": \"刪除登錄日誌\"}',NULL,NULL,'sys:loginLog:delete',1,1,1,'2026-01-16 11:51:27','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(640,1993479636925403138,'PUBLIC',1,638,'button',NULL,'批量删除登录日志','{\"en-US\": \"Batch Delete Login Logs\", \"ja-JP\": \"一括削除ログインログ\", \"ko-KR\": \"일괄 삭제 로그인 로그\", \"zh-CN\": \"批量删除登录日志\", \"zh-TW\": \"批量刪除登錄日誌\"}',NULL,NULL,'sys:loginLog:batchDelete',2,1,1,'2026-01-16 11:51:27','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(641,1993479636925403138,'PUBLIC',1,638,'button',NULL,'导出登录日志','{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}',NULL,NULL,'sys:loginLog:export',3,1,1,'2026-01-16 11:51:27','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(642,1993479636925403138,'PUBLIC',1,654,'menu','online','在线用户','{\"en-US\": \"Online Users\", \"ja-JP\": \"オンラインユーザー\", \"ko-KR\": \"온라인 사용자\", \"zh-CN\": \"在线用户\", \"zh-TW\": \"在線用戶\"}','UsergroupAddOutlined','SystemOnline','sys:online:view',2,1,1,'2026-01-16 11:51:35','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(643,1993479636925403138,'PUBLIC',1,642,'button',NULL,'踢下线','{\"en-US\": \"Kick Offline\", \"ja-JP\": \"オフラインキック\", \"ko-KR\": \"오프라인 강제 종료\", \"zh-CN\": \"踢下线\", \"zh-TW\": \"踢下線\"}',NULL,NULL,'sys:online:kickout',1,1,1,'2026-01-16 11:51:47','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(644,1993479636925403138,'PUBLIC',1,642,'button',NULL,'批量踢下线','{\"en-US\": \"Batch Kick Offline\", \"ja-JP\": \"一括オフラインキック\", \"ko-KR\": \"일괄 오프라인 강제 종료\", \"zh-CN\": \"批量踢下线\", \"zh-TW\": \"批量踢下線\"}',NULL,NULL,'sys:online:batchKickout',2,1,1,'2026-01-16 11:51:47','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(645,1993479636925403138,'PUBLIC',1,642,'button',NULL,'导出在线用户','{\"en-US\": \"Export Online Users\", \"ja-JP\": \"オンラインユーザーエクスポート\", \"ko-KR\": \"온라인 사용자 내보내기\", \"zh-CN\": \"导出在线用户\", \"zh-TW\": \"匯出在線用戶\"}',NULL,NULL,'sys:online:export',3,1,1,'2026-01-16 11:51:47','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(646,1993479636925403138,'PUBLIC',1,653,'menu','tenant','租户管理','{\"en-US\": \"Tenants\", \"ja-JP\": \"テナント管理\", \"ko-KR\": \"테넌트 관리\", \"zh-CN\": \"租户管理\", \"zh-TW\": \"租戶管理\"}','TeamOutlined','SystemTenant','sys:tenant:view',2,1,1,'2026-01-16 11:51:52','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(647,1993479636925403138,'PUBLIC',1,646,'button',NULL,'新增租户','{\"en-US\": \"Add Tenant\", \"ja-JP\": \"テナント追加\", \"ko-KR\": \"테넌트 추가\", \"zh-CN\": \"新增租户\", \"zh-TW\": \"新增租戶\"}',NULL,NULL,'sys:tenant:add',1,1,1,'2026-01-16 11:52:01','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(648,1993479636925403138,'PUBLIC',1,646,'button',NULL,'编辑租户','{\"en-US\": \"Edit Tenant\", \"ja-JP\": \"テナント編集\", \"ko-KR\": \"테넌트 편집\", \"zh-CN\": \"编辑租户\", \"zh-TW\": \"編輯租戶\"}',NULL,NULL,'sys:tenant:edit',2,1,1,'2026-01-16 11:52:01','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(649,1993479636925403138,'PUBLIC',1,646,'button',NULL,'删除租户','{\"en-US\": \"Delete Tenant\", \"ja-JP\": \"テナント削除\", \"ko-KR\": \"테넌트 삭제\", \"zh-CN\": \"删除租户\", \"zh-TW\": \"刪除租戶\"}',NULL,NULL,'sys:tenant:delete',3,1,1,'2026-01-16 11:52:01','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(650,1993479636925403138,'PUBLIC',1,646,'button',NULL,'批量删除租户','{\"en-US\": \"Batch Delete Tenants\", \"ja-JP\": \"一括削除テナント\", \"ko-KR\": \"일괄 삭제 테넌트\", \"zh-CN\": \"批量删除租户\", \"zh-TW\": \"批量刪除租戶\"}',NULL,NULL,'sys:tenant:batchDelete',4,1,1,'2026-01-16 11:52:01','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(651,1993479636925403138,'PUBLIC',1,646,'button',NULL,'导出租户','{\"en-US\": \"Export Tenants\", \"ja-JP\": \"テナントエクスポート\", \"ko-KR\": \"테넌트 내보내기\", \"zh-CN\": \"导出租户\", \"zh-TW\": \"匯出租戶\"}',NULL,NULL,'sys:tenant:export',5,1,1,'2026-01-16 11:52:01','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(652,1993479636925403138,'PUBLIC',1,0,'catalog','organization','组织架构','{\"en-US\": \"Organization\", \"ja-JP\": \"組織構成\", \"ko-KR\": \"조직 구조\", \"zh-CN\": \"组织架构\", \"zh-TW\": \"組織架構\"}','ApartmentOutlined','SystemOrganization','sys:organization:view',2,1,1,'2026-01-17 18:30:38','system','2026-01-18 18:55:14','system',0,1,'embedded',NULL),(653,1993479636925403138,'PUBLIC',1,0,'catalog','authorization','授权管理','{\"en-US\": \"Authorization\", \"ja-JP\": \"認可管理\", \"ko-KR\": \"인증 관리\", \"zh-CN\": \"授权管理\", \"zh-TW\": \"授權管理\"}','SafetyOutlined','SystemAuthorization','sys:authorization:view',3,1,1,'2026-01-17 18:30:48','system','2026-01-18 18:55:14','system',0,1,'embedded',NULL),(654,1993479636925403138,'PUBLIC',1,0,'catalog','maintenance','系统运维','{\"en-US\": \"System Maintenance\", \"ja-JP\": \"システム運用\", \"ko-KR\": \"시스템 운영\", \"zh-CN\": \"系统运维\", \"zh-TW\": \"系統運維\"}','ToolOutlined','SystemMaintenance','sys:maintenance:view',11,1,1,'2026-01-20 22:05:54','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(655,1993479636925403138,'PUBLIC',1,654,'menu','config','系统配置','{\"en-US\": \"System Config\", \"ja-JP\": \"システム設定\", \"ko-KR\": \"시스템 설정\", \"zh-CN\": \"系统配置\", \"zh-TW\": \"系統設定\"}','SettingOutlined','SystemConfig','sys:config:view',1,1,1,'2026-01-20 22:06:38','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(656,1993479636925403138,'PUBLIC',1,655,'button','view','查看配置','{\"en-US\": \"View Config\", \"ja-JP\": \"設定表示\", \"ko-KR\": \"설정 보기\", \"zh-CN\": \"查看配置\", \"zh-TW\": \"查看設定\"}',NULL,NULL,'sys:config:view',1,1,1,'2026-01-20 22:07:56','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,3,'embedded',NULL),(657,1993479636925403138,'PUBLIC',1,655,'button','edit','编辑配置','{\"en-US\": \"Edit Config\", \"ja-JP\": \"設定編集\", \"ko-KR\": \"설정 편집\", \"zh-CN\": \"编辑配置\", \"zh-TW\": \"編輯設定\"}',NULL,NULL,'sys:config:edit',2,1,1,'2026-01-20 22:07:56','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,3,'embedded',NULL),(667,1993479636925403138,'PUBLIC',1,654,'menu','operationLog','操作日志','{\"en-US\": \"Operation Log\", \"ja-JP\": \"操作ログ\", \"ko-KR\": \"Operation Log\", \"zh-CN\": \"操作日志\", \"zh-TW\": \"操作日誌\"}','FileTextOutlined','SystemOperationLog','sys:operation-log:view',3,1,1,'2026-01-28 20:51:11','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(668,1993479636925403138,'PUBLIC',1,667,'button',NULL,'查询','{\"en-US\": \"Query\", \"ja-JP\": \"クエリ\", \"ko-KR\": \"Query\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'sys:operation-log:query',1,1,1,'2026-01-28 20:51:23','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(669,1993479636925403138,'PUBLIC',1,667,'button',NULL,'导出','{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"Export\", \"zh-CN\": \"导出\", \"zh-TW\": \"匯出\"}',NULL,NULL,'sys:operation-log:export',2,1,1,'2026-01-28 20:51:34','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(670,1993479636925403138,'PUBLIC',1,654,'menu','tenantMessageWhitelist','租户消息白名单','{\"en-US\": \"Tenant Message Whitelist\", \"ja-JP\": \"テナントメッセージホワイトリスト\", \"ko-KR\": \"Tenant Message Whitelist\", \"zh-CN\": \"租户消息白名单\", \"zh-TW\": \"租戶消息白名單\"}','SafetyCertificateOutlined','SystemTenantMessageWhitelist','sys:tenant-message-whitelist:view',5,1,1,'2026-01-28 20:51:44','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(671,1993479636925403138,'PUBLIC',1,670,'button',NULL,'新增','{\"en-US\": \"Create\", \"ja-JP\": \"作成\", \"ko-KR\": \"Create\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'sys:tenant-message-whitelist:create',1,1,1,'2026-01-28 20:51:59','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(672,1993479636925403138,'PUBLIC',1,670,'button',NULL,'修改','{\"en-US\": \"Update\", \"ja-JP\": \"更新\", \"ko-KR\": \"Update\", \"zh-CN\": \"修改\", \"zh-TW\": \"修改\"}',NULL,NULL,'sys:tenant-message-whitelist:update',2,1,1,'2026-01-28 20:52:11','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(673,1993479636925403138,'PUBLIC',1,670,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"Delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:tenant-message-whitelist:delete',3,1,1,'2026-01-28 20:52:19','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(674,1993479636925403138,'PUBLIC',1,654,'menu','messageTemplate','消息模板','{\"en-US\": \"Message Template\", \"ja-JP\": \"メッセージテンプレート\", \"ko-KR\": \"Message Template\", \"zh-CN\": \"消息模板\", \"zh-TW\": \"消息模板\"}','MailOutlined','SystemMessageTemplate','sys:message-template:view',4,1,1,'2026-01-28 20:52:26','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(675,1993479636925403138,'PUBLIC',1,3000000000000000423,'menu','userTableConfig','用户列设置','{\"en-US\": \"User Table Config\", \"ja-JP\": \"ユーザー列設定\", \"ko-KR\": \"사용자 열 설정\", \"zh-CN\": \"用户列设置\", \"zh-TW\": \"用戶列設置\"}','ColumnWidthOutlined','SystemUserTableConfig','sys:userTableConfig:view',20,1,1,'2026-04-02 14:50:02','system','2026-04-12 10:48:14','codex',0,2,'embedded',NULL),(676,1993479636925403138,'PUBLIC',3,0,'menu','taskConfig','审批任务配置','{\"en-US\": \"Task Config\", \"ja-JP\": \"承認タスク設定\", \"ko-KR\": \"승인 작업 구성\", \"zh-CN\": \"审批任务配置\", \"zh-TW\": \"審批任務配置\"}','SettingOutlined','ApprovalTaskConfig','wf:taskConfig:view',10,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(677,1993479636925403138,'PUBLIC',3,0,'menu','execution/start','发起审批','{\"en-US\": \"Start Approval\", \"ja-JP\": \"承認開始\", \"ko-KR\": \"승인 시작\", \"zh-CN\": \"发起审批\", \"zh-TW\": \"發起審批\"}','PlayCircleOutlined','ApprovalExecutionStart','wf:execution:start',20,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(678,1993479636925403138,'PUBLIC',3,0,'menu','my/pending','我的待办','{\"en-US\": \"My Pending\", \"ja-JP\": \"私の保留中\", \"ko-KR\": \"내 대기\", \"zh-CN\": \"我的待办\", \"zh-TW\": \"我的待辦\"}','ClockCircleOutlined','ApprovalMyPending','wf:myTask:pending',30,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(679,1993479636925403138,'PUBLIC',3,0,'menu','my/processed','我已处理','{\"en-US\": \"My Processed\", \"ja-JP\": \"処理済み\", \"ko-KR\": \"처리 완료\", \"zh-CN\": \"我已处理\", \"zh-TW\": \"我已處理\"}','CheckCircleOutlined','ApprovalMyProcessed','wf:myTask:processed',40,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(680,1993479636925403138,'PUBLIC',3,0,'menu','my/initiated','我发起的','{\"en-US\": \"My Initiated\", \"ja-JP\": \"開始した\", \"ko-KR\": \"시작한\", \"zh-CN\": \"我发起的\", \"zh-TW\": \"我發起的\"}','SendOutlined','ApprovalMyInitiated','wf:myTask:initiated',50,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(683,1993479636925403138,'PUBLIC',3,676,'button',NULL,'新增审批任务','{\"en-US\": \"Add Task Config\", \"ja-JP\": \"承認タスク追加\", \"ko-KR\": \"승인 작업 추가\", \"zh-CN\": \"新增审批任务\", \"zh-TW\": \"新增審批任務\"}',NULL,NULL,'wf:taskConfig:add',1,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(684,1993479636925403138,'PUBLIC',3,676,'button',NULL,'配置审批流程','{\"en-US\": \"Configure Workflow\", \"ja-JP\": \"承認フロー設定\", \"ko-KR\": \"승인 흐름 구성\", \"zh-CN\": \"配置审批流程\", \"zh-TW\": \"配置審批流程\"}',NULL,NULL,'wf:taskConfig:config',4,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(685,1993479636925403138,'PUBLIC',3,676,'button',NULL,'删除审批任务','{\"en-US\": \"Delete Task Config\", \"ja-JP\": \"承認タスク削除\", \"ko-KR\": \"승인 작업 삭제\", \"zh-CN\": \"删除审批任务\", \"zh-TW\": \"刪除審批任務\"}',NULL,NULL,'wf:taskConfig:delete',3,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(686,1993479636925403138,'PUBLIC',3,676,'button',NULL,'编辑审批任务','{\"en-US\": \"Edit Task Config\", \"ja-JP\": \"承認タスク編集\", \"ko-KR\": \"승인 작업 편집\", \"zh-CN\": \"编辑审批任务\", \"zh-TW\": \"編輯審批任務\"}',NULL,NULL,'wf:taskConfig:edit',2,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(687,1993479636925403138,'PUBLIC',3,678,'button',NULL,'同意审批','{\"en-US\": \"Approve\", \"ja-JP\": \"承認\", \"ko-KR\": \"승인\", \"zh-CN\": \"同意审批\", \"zh-TW\": \"同意審批\"}',NULL,NULL,'wf:execution:approve',1,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(688,1993479636925403138,'PUBLIC',3,678,'button',NULL,'驳回审批','{\"en-US\": \"Reject\", \"ja-JP\": \"却下\", \"ko-KR\": \"거부\", \"zh-CN\": \"驳回审批\", \"zh-TW\": \"駁回審批\"}',NULL,NULL,'wf:execution:reject',2,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(689,1993479636925403138,'PUBLIC',3,680,'button',NULL,'撤销审批','{\"en-US\": \"Cancel Execution\", \"ja-JP\": \"承認取消\", \"ko-KR\": \"승인 취소\", \"zh-CN\": \"撤销审批\", \"zh-TW\": \"撤銷審批\"}',NULL,NULL,'wf:execution:cancel',1,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0,3,'embedded',NULL),(690,1993479636925403138,'PUBLIC',1,2,'button',NULL,'绑定人员','{\"en-US\": \"sys:role:authUser\", \"ja-JP\": \"绑定人员\", \"ko-KR\": \"sys:role:authUser\", \"zh-CN\": \"绑定人员\", \"zh-TW\": \"绑定人员\"}',NULL,NULL,'sys:role:authUser',6,1,1,'2026-04-02 16:37:51','codex','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(691,1,'PUBLIC',3,0,'menu','dashboard','审批工作台','{\"en-US\": \"Approval Dashboard\", \"ja-JP\": \"承認ダッシュボード\", \"ko-KR\": \"승인 대시보드\", \"zh-CN\": \"审批工作台\", \"zh-TW\": \"審批工作台\"}','DashboardOutlined','ApprovalDashboard','wf:dashboard:view',5,1,1,'2026-04-06 19:49:39','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(692,1,'PUBLIC',4,0,'menu','role','角色管理','{\"en-US\": \"role\", \"ja-JP\": \"角色管理\", \"ko-KR\": \"role\", \"zh-CN\": \"角色管理\", \"zh-TW\": \"角色管理\"}','TeamOutlined','SystemRole','sys:role:view',2,1,1,'2026-04-06 23:16:34','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,1,'embedded',NULL),(693,1,'PUBLIC',4,692,'menu','menu-grant/:roleId','菜单授权','{\"en-US\": \"menu-grant/:roleId\", \"ja-JP\": \"菜单授权\", \"ko-KR\": \"menu-grant/:roleId\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"菜单授权\"}','SafetyCertificateOutlined','SystemRoleMenuGrant','sys:role:authMenu',1,0,1,'2026-04-06 23:16:57','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(694,1,'PUBLIC',4,692,'menu','user-grant/:roleId','人员授权','{\"en-US\": \"user-grant/:roleId\", \"ja-JP\": \"人员授权\", \"ko-KR\": \"user-grant/:roleId\", \"zh-CN\": \"人员授权\", \"zh-TW\": \"人员授权\"}','UsergroupAddOutlined','SystemRoleUserGrant','sys:role:authUser',2,0,1,'2026-04-06 23:16:59','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(695,1993479636925403138,'PUBLIC',1,653,'menu','menu-grant/:roleId','菜单授权','{\"en-US\": \"menu-grant/:roleId\", \"ja-JP\": \"菜单授权\", \"ko-KR\": \"menu-grant/:roleId\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"菜单授权\"}','SafetyCertificateOutlined','SystemRoleMenuGrant','sys:role:authMenu',1,0,1,'2026-04-06 23:20:21','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(696,1993479636925403138,'PUBLIC',1,653,'menu','user-grant/:roleId','人员授权','{\"en-US\": \"user-grant/:roleId\", \"ja-JP\": \"人员授权\", \"ko-KR\": \"user-grant/:roleId\", \"zh-CN\": \"人员授权\", \"zh-TW\": \"人员授权\"}','UsergroupAddOutlined','SystemRoleUserGrant','sys:role:authUser',2,0,1,'2026-04-06 23:20:23','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(697,1993479636925403138,'PUBLIC',3,0,'menu','dashboard','审批工作台','{\"en-US\": \"Approval Dashboard\", \"ja-JP\": \"承認ダッシュボード\", \"ko-KR\": \"승인 대시보드\", \"zh-CN\": \"审批工作台\", \"zh-TW\": \"審批工作台\"}','DashboardOutlined','ApprovalDashboard','wf:dashboard:view',5,1,1,'2026-04-07 09:48:56','system','2026-04-07 20:18:40','system',0,1,'embedded',NULL),(698,1993479636925403138,'PUBLIC',5,0,'catalog','basicInfo','基础信息','{\"en-US\": \"Basic Information\", \"ja-JP\": \"基本情報\", \"ko-KR\": \"기본 정보\", \"zh-CN\": \"基础信息\", \"zh-TW\": \"基礎資訊\"}','BookOutlined',NULL,'basic:catalog:view',10,0,0,'2026-04-09 18:10:41','system','2026-04-29 23:46:01','system',1,1,'embedded',NULL),(699,1993479636925403138,'PUBLIC',5,0,'menu','dashboard','基础信息主页','{\"en-US\": \"Basic Dashboard\", \"ja-JP\": \"基本情報ホーム\", \"ko-KR\": \"기본 정보 홈\", \"zh-CN\": \"基础信息主页\", \"zh-TW\": \"基礎資訊首頁\"}','DashboardOutlined','BasicDashboard','basic:dashboard:view',1,1,1,'2026-04-09 18:10:41','system','2026-04-29 22:20:00','system',0,1,'embedded',NULL),(700,1993479636925403138,'PUBLIC',5,0,'menu','encodeRule','编码规则管理','{\"en-US\": \"Encoding Rule Management\", \"ja-JP\": \"採番ルール管理\", \"ko-KR\": \"인코딩 규칙 관리\", \"zh-CN\": \"编码规则管理\", \"zh-TW\": \"編碼規則管理\"}','CodeOutlined','BasicEncodeRule','basic:encodeRule:query',10,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,1,'embedded',NULL),(701,1993479636925403138,'PUBLIC',5,700,'menu','/encode/rule','规则管理','{\"en-US\": \"Rule Management\", \"ja-JP\": \"ルール管理\", \"ko-KR\": \"규칙 관리\", \"zh-CN\": \"规则管理\", \"zh-TW\": \"規則管理\"}','icon-setting','system/encodeRule','menu:encode:rule',1,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,2,'embedded',NULL),(702,1993479636925403138,'PUBLIC',5,700,'menu','/encode/example','示例管理','{\"en-US\": \"Example Management\", \"ja-JP\": \"サンプル管理\", \"ko-KR\": \"예시 관리\", \"zh-CN\": \"示例管理\", \"zh-TW\": \"示例管理\"}','icon-example','system/encodeRuleExample','menu:encode:example',2,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,2,'embedded',NULL),(703,1993479636925403138,'PUBLIC',5,700,'menu','/encode/history','历史记录','{\"en-US\": \"History Records\", \"ja-JP\": \"履歴レコード\", \"ko-KR\": \"이력 기록\", \"zh-CN\": \"历史记录\", \"zh-TW\": \"歷史記錄\"}','icon-history','system/encodeHistory','menu:encode:history',3,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,2,'embedded',NULL),(704,1993479636925403138,'PUBLIC',1,702,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'encode:example:add',1,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(705,1993479636925403138,'PUBLIC',1,702,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'encode:example:edit',2,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(706,1993479636925403138,'PUBLIC',1,702,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'encode:example:delete',3,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(707,1993479636925403138,'PUBLIC',1,702,'button',NULL,'查看','{\"en-US\": \"View\", \"ja-JP\": \"表示\", \"ko-KR\": \"보기\", \"zh-CN\": \"查看\", \"zh-TW\": \"查看\"}',NULL,NULL,'encode:example:view',4,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(708,1993479636925403138,'PUBLIC',1,703,'button',NULL,'查询','{\"en-US\": \"Query\", \"ja-JP\": \"照会\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'encode:history:query',1,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(709,1993479636925403138,'PUBLIC',1,703,'button',NULL,'导出','{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"내보내기\", \"zh-CN\": \"导出\", \"zh-TW\": \"導出\"}',NULL,NULL,'encode:history:export',2,0,0,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','codex',1,3,'embedded',NULL),(711,1993479636925403138,'PUBLIC',5,700,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:encodeRule:add',1,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(712,1993479636925403138,'PUBLIC',5,700,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'basic:encodeRule:edit',2,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(713,1993479636925403138,'PUBLIC',5,700,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'basic:encodeRule:delete',3,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(714,1993479636925403138,'PUBLIC',5,700,'button',NULL,'查看','{\"en-US\": \"View\", \"ja-JP\": \"表示\", \"ko-KR\": \"보기\", \"zh-CN\": \"查看\", \"zh-TW\": \"查看\"}',NULL,NULL,'basic:encodeRule:query',4,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(715,1993479636925403138,'PUBLIC',5,700,'button',NULL,'测试','{\"en-US\": \"Test\", \"ja-JP\": \"テスト\", \"ko-KR\": \"테스트\", \"zh-CN\": \"测试\", \"zh-TW\": \"測試\"}',NULL,NULL,'basic:encodeRule:test',5,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(716,1993479636925403138,'PUBLIC',5,700,'button',NULL,'生成编码','{\"en-US\": \"Generate Code\", \"ja-JP\": \"コード生成\", \"ko-KR\": \"코드 생성\", \"zh-CN\": \"生成编码\", \"zh-TW\": \"生成編碼\"}',NULL,NULL,'basic:encodeRule:generate',6,1,1,'2026-04-10 11:29:55','1993479637244170242','2026-05-06 17:29:21','system',0,2,'embedded',NULL),(720,1993479636925403138,'PUBLIC',1,730,'menu','i18nLanguageType','语言配置','{\"en-US\": \"Language Configuration\", \"ja-JP\": \"言語設定\", \"ko-KR\": \"언어 설정\", \"zh-CN\": \"语言配置\", \"zh-TW\": \"語言設定\"}','GlobalOutlined','SystemI18nLanguageType','sys:i18nLanguageType:view',10,1,1,'2026-04-10 13:34:03','codex','2026-04-26 20:54:50','20260426_i18n_fix',0,2,'embedded',NULL),(721,1993479636925403138,'PUBLIC',1,720,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'sys:i18nLanguageType:add',1,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(722,1993479636925403138,'PUBLIC',1,720,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'sys:i18nLanguageType:edit',2,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(723,1993479636925403138,'PUBLIC',1,720,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:i18nLanguageType:delete',3,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(724,1993479636925403138,'PUBLIC',1,720,'button',NULL,'设为默认','{\"en-US\": \"Set Default\", \"ja-JP\": \"既定に設定\", \"ko-KR\": \"기본값으로 설정\", \"zh-CN\": \"设为默认\", \"zh-TW\": \"設為預設\"}',NULL,NULL,'sys:i18nLanguageType:setDefault',4,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(725,1993479636925403138,'PUBLIC',1,720,'button',NULL,'导入','{\"en-US\": \"Import\", \"ja-JP\": \"インポート\", \"ko-KR\": \"가져오기\", \"zh-CN\": \"导入\", \"zh-TW\": \"導入\"}',NULL,NULL,'sys:i18nLanguageType:import',5,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(726,1993479636925403138,'PUBLIC',1,720,'button',NULL,'下载模板','{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートをダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}',NULL,NULL,'sys:i18nLanguageType:template:download',6,1,1,'2026-04-10 13:34:03','codex','2026-04-10 22:27:10','codex',0,2,'embedded',NULL),(730,1993479636925403138,'PUBLIC',1,0,'catalog','i18nConfig','多语言配置','{\"en-US\": \"I18n Config\", \"ja-JP\": \"多言語設定\", \"ko-KR\": \"다국어 설정\", \"zh-CN\": \"多语言配置\", \"zh-TW\": \"多語言設定\"}','TranslationOutlined',NULL,NULL,85,1,1,'2026-04-14 13:59:46','20260414_fix','2026-04-26 20:54:50','20260426_i18n_fix',0,1,'embedded',NULL),(731,1993479636925403138,'PUBLIC',1,730,'menu','i18nMessage','多语言消息','{\"en-US\": \"I18n Message\", \"ja-JP\": \"多言語メッセージ\", \"ko-KR\": \"다국어 메시지\", \"zh-CN\": \"多语言消息\", \"zh-TW\": \"多語言訊息\"}','MessageOutlined','SystemI18nMessage','sys:i18nMessage:view',20,1,1,'2026-04-14 13:59:46','20260414_fix','2026-04-26 20:54:50','20260426_i18n_fix',0,2,'embedded',NULL),(732,1993479636925403138,'PUBLIC',1,731,'button',NULL,'新增多语言消息','{\"en-US\": \"sys:i18nMessage:add\", \"ja-JP\": \"新增多语言消息\", \"ko-KR\": \"sys:i18nMessage:add\", \"zh-CN\": \"新增多语言消息\", \"zh-TW\": \"新增多语言消息\"}',NULL,NULL,'sys:i18nMessage:add',1,1,1,'2026-04-14 13:59:46','20260414_fix','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(733,1993479636925403138,'PUBLIC',1,731,'button',NULL,'编辑多语言消息','{\"en-US\": \"sys:i18nMessage:edit\", \"ja-JP\": \"编辑多语言消息\", \"ko-KR\": \"sys:i18nMessage:edit\", \"zh-CN\": \"编辑多语言消息\", \"zh-TW\": \"编辑多语言消息\"}',NULL,NULL,'sys:i18nMessage:edit',2,1,1,'2026-04-14 13:59:46','20260414_fix','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(734,1993479636925403138,'PUBLIC',1,731,'button',NULL,'删除多语言消息','{\"en-US\": \"sys:i18nMessage:delete\", \"ja-JP\": \"删除多语言消息\", \"ko-KR\": \"sys:i18nMessage:delete\", \"zh-CN\": \"删除多语言消息\", \"zh-TW\": \"删除多语言消息\"}',NULL,NULL,'sys:i18nMessage:delete',3,1,1,'2026-04-14 13:59:46','20260414_fix','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(735,1993479636925403138,'PUBLIC',1,652,'menu','inviteCode','邀请码管理','{\"en-US\": \"Invite Code Management\", \"ja-JP\": \"招待コード管理\", \"ko-KR\": \"초대 코드 관리\", \"zh-CN\": \"邀请码管理\", \"zh-TW\": \"邀請碼管理\"}','KeyOutlined','SystemInviteCode','sys:invite-code:view',15,1,1,'2026-04-10 19:52:26','system','2026-04-26 20:54:50','20260426_i18n_fix',0,2,'embedded',NULL),(736,1993479636925403138,'PUBLIC',1,735,'button','','新增邀请码','{\"en-US\": \"sys:invite-code:add\", \"ja-JP\": \"新增邀请码\", \"ko-KR\": \"sys:invite-code:add\", \"zh-CN\": \"新增邀请码\", \"zh-TW\": \"新增邀请码\"}',NULL,NULL,'sys:invite-code:add',1,1,1,'2026-04-10 19:52:26','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(737,1993479636925403138,'PUBLIC',1,735,'button','','停用邀请码','{\"en-US\": \"sys:invite-code:edit\", \"ja-JP\": \"停用邀请码\", \"ko-KR\": \"sys:invite-code:edit\", \"zh-CN\": \"停用邀请码\", \"zh-TW\": \"停用邀请码\"}',NULL,NULL,'sys:invite-code:edit',2,1,1,'2026-04-10 19:52:26','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(738,1993479636925403138,'PUBLIC',1,735,'button','','删除邀请码','{\"en-US\": \"sys:invite-code:delete\", \"ja-JP\": \"删除邀请码\", \"ko-KR\": \"sys:invite-code:delete\", \"zh-CN\": \"删除邀请码\", \"zh-TW\": \"删除邀请码\"}',NULL,NULL,'sys:invite-code:delete',3,1,1,'2026-04-10 19:52:26','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(739,1993479636925403138,'PUBLIC',1,735,'button','','查看使用记录','{\"en-US\": \"sys:invite-code:record:view\", \"ja-JP\": \"查看使用记录\", \"ko-KR\": \"sys:invite-code:record:view\", \"zh-CN\": \"查看使用记录\", \"zh-TW\": \"查看使用记录\"}',NULL,NULL,'sys:invite-code:record:view',4,1,1,'2026-04-10 19:52:26','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(740,1993479636925403138,'PUBLIC',6,0,'menu','integration','接口平台主页','{\"en-US\": \"Integration Platform Home\", \"ja-JP\": \"接口平台主页\", \"ko-KR\": \"接口平台主页\", \"zh-CN\": \"接口平台主页\", \"zh-TW\": \"接口平台主页\"}','ApiOutlined','IntegrationHome','integration:home:view',5,1,1,'2026-04-14 15:49:25','20260414_init','2026-05-08 12:03:51','20260427_fix_integration_menu_root_level',0,1,'embedded',NULL),(741,1993479636925403138,'PUBLIC',6,0,'menu','thirdSystem','第三方系统管理','{\"en-US\": \"Third System Management\", \"ja-JP\": \"サードパーティシステム管理\", \"ko-KR\": \"타사 시스템 관리\", \"zh-CN\": \"第三方系统管理\", \"zh-TW\": \"第三方系統管理\"}','ApartmentOutlined','IntegrationThirdSystem','integration:third-system:view',10,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,1,'embedded',NULL),(742,1993479636925403138,'PUBLIC',6,741,'button',NULL,'新增系统','{\"en-US\": \"Add System\", \"ja-JP\": \"システム追加\", \"ko-KR\": \"시스템 추가\", \"zh-CN\": \"新增系统\", \"zh-TW\": \"新增系統\"}',NULL,NULL,'integration:third-system:add',1,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(743,1993479636925403138,'PUBLIC',6,741,'button',NULL,'编辑系统','{\"en-US\": \"Edit System\", \"ja-JP\": \"システム編集\", \"ko-KR\": \"시스템 편집\", \"zh-CN\": \"编辑系统\", \"zh-TW\": \"編輯系統\"}',NULL,NULL,'integration:third-system:edit',2,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(744,1993479636925403138,'PUBLIC',6,741,'button',NULL,'删除系统','{\"en-US\": \"Delete System\", \"ja-JP\": \"システム削除\", \"ko-KR\": \"시스템 삭제\", \"zh-CN\": \"删除系统\", \"zh-TW\": \"刪除系統\"}',NULL,NULL,'integration:third-system:delete',3,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(745,1993479636925403138,'PUBLIC',6,741,'button',NULL,'批量删除','{\"en-US\": \"Batch Delete\", \"ja-JP\": \"一括削除\", \"ko-KR\": \"일괄 삭제\", \"zh-CN\": \"批量删除\", \"zh-TW\": \"批次刪除\"}',NULL,NULL,'integration:third-system:batch-delete',4,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(746,1993479636925403138,'PUBLIC',6,741,'button',NULL,'授权','{\"en-US\": \"Authorization\", \"ja-JP\": \"認証\", \"ko-KR\": \"권한 설정\", \"zh-CN\": \"授权\", \"zh-TW\": \"授權\"}',NULL,NULL,'integration:third-system:auth',5,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(747,1993479636925403138,'PUBLIC',6,0,'menu','apiConfig','接口配置管理','{\"en-US\": \"API Config Management\", \"ja-JP\": \"API設定管理\", \"ko-KR\": \"API 설정 관리\", \"zh-CN\": \"接口配置管理\", \"zh-TW\": \"介面設定管理\"}','ApiOutlined','IntegrationApiConfig','integration:api-config:view',20,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,1,'embedded',NULL),(748,1993479636925403138,'PUBLIC',6,747,'button',NULL,'新增接口','{\"en-US\": \"Add API\", \"ja-JP\": \"API追加\", \"ko-KR\": \"API 추가\", \"zh-CN\": \"新增接口\", \"zh-TW\": \"新增介面\"}',NULL,NULL,'integration:api-config:add',1,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(749,1993479636925403138,'PUBLIC',6,747,'button',NULL,'编辑接口','{\"en-US\": \"Edit API\", \"ja-JP\": \"API編集\", \"ko-KR\": \"API 편집\", \"zh-CN\": \"编辑接口\", \"zh-TW\": \"編輯介面\"}',NULL,NULL,'integration:api-config:edit',2,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(750,1993479636925403138,'PUBLIC',6,747,'button',NULL,'删除接口','{\"en-US\": \"Delete API\", \"ja-JP\": \"API削除\", \"ko-KR\": \"API 삭제\", \"zh-CN\": \"删除接口\", \"zh-TW\": \"刪除介面\"}',NULL,NULL,'integration:api-config:delete',3,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(751,1993479636925403138,'PUBLIC',6,747,'button',NULL,'配置参数','{\"en-US\": \"Configure Parameters\", \"ja-JP\": \"パラメータ設定\", \"ko-KR\": \"매개변수 설정\", \"zh-CN\": \"配置参数\", \"zh-TW\": \"配置參數\"}',NULL,NULL,'integration:api-config:config-param',4,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(752,1993479636925403138,'PUBLIC',6,747,'button',NULL,'配置映射','{\"en-US\": \"Configure Mapping\", \"ja-JP\": \"マッピング設定\", \"ko-KR\": \"매핑 설정\", \"zh-CN\": \"配置映射\", \"zh-TW\": \"配置映射\"}',NULL,NULL,'integration:api-config:config-mapping',5,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,2,'embedded',NULL),(753,1993479636925403138,'PUBLIC',6,0,'menu','apiCallLog','调用记录查询','{\"en-US\": \"API Call Log\", \"ja-JP\": \"API呼び出しログ\", \"ko-KR\": \"API 호출 로그\", \"zh-CN\": \"调用记录查询\", \"zh-TW\": \"呼叫記錄查詢\"}','FileSearchOutlined','IntegrationApiCallLog','integration:api-call-log:view',30,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-27 14:55:15','20260427_fix_integration_menu_root_level',0,1,'embedded',NULL),(754,1993479636925403138,'PUBLIC',5,0,'catalog','label','标签管理','{\"en-US\": \"Label Management\", \"ja-JP\": \"ラベル管理\", \"ko-KR\": \"라벨 관리\", \"zh-CN\": \"标签管理\", \"zh-TW\": \"標籤管理\"}','TagsOutlined',NULL,NULL,10,1,1,'2026-04-17 10:33:40','system','2026-05-06 17:29:21','1993479637244170242',0,1,'embedded',NULL),(755,1993479636925403138,'PUBLIC',5,754,'menu','template','标签模板','{\"en-US\": \"Label Template\", \"ja-JP\": \"ラベルテンプレート\", \"ko-KR\": \"라벨 템플릿\", \"zh-CN\": \"标签模板\", \"zh-TW\": \"標籤模板\"}','FileTextOutlined','LabelTemplate','label:template:view',1,1,1,'2026-04-17 10:33:40','system','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(756,1993479636925403138,'PUBLIC',5,754,'menu','print','标签打印','{\"en-US\": \"Label Print\", \"ja-JP\": \"ラベル印刷\", \"ko-KR\": \"라벨 인쇄\", \"zh-CN\": \"标签打印\", \"zh-TW\": \"標籤打印\"}','PrinterOutlined','LabelPrint','label:print:view',2,1,1,'2026-04-17 10:33:40','system','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(757,1993479636925403138,'PUBLIC',5,754,'menu','record','打印记录','{\"en-US\": \"Print Record\", \"ja-JP\": \"印刷記録\", \"ko-KR\": \"인쇄 기록\", \"zh-CN\": \"打印记录\", \"zh-TW\": \"打印記錄\"}','HistoryOutlined','LabelRecord','label:record:view',3,1,1,'2026-04-17 10:33:40','system','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(758,1993479636925403138,'PUBLIC',5,754,'menu','binding','模板绑定','{\"en-US\": \"Template Binding\", \"ja-JP\": \"テンプレートバインディング\", \"ko-KR\": \"템플릿 바인딩\", \"zh-CN\": \"模板绑定\", \"zh-TW\": \"模板綁定\"}','LinkOutlined','LabelBinding','label:binding:view',4,1,1,'2026-04-17 10:33:40','system','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(759,1993479636925403138,'PUBLIC',5,755,'button',NULL,'新增模板','{\"en-US\": \"label:template:add\", \"ja-JP\": \"新增模板\", \"ko-KR\": \"label:template:add\", \"zh-CN\": \"新增模板\", \"zh-TW\": \"新增模板\"}',NULL,NULL,'label:template:add',1,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(760,1993479636925403138,'PUBLIC',5,755,'button',NULL,'编辑模板','{\"en-US\": \"label:template:edit\", \"ja-JP\": \"编辑模板\", \"ko-KR\": \"label:template:edit\", \"zh-CN\": \"编辑模板\", \"zh-TW\": \"编辑模板\"}',NULL,NULL,'label:template:edit',2,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(761,1993479636925403138,'PUBLIC',5,755,'button',NULL,'删除模板','{\"en-US\": \"label:template:delete\", \"ja-JP\": \"删除模板\", \"ko-KR\": \"label:template:delete\", \"zh-CN\": \"删除模板\", \"zh-TW\": \"删除模板\"}',NULL,NULL,'label:template:delete',3,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(762,1993479636925403138,'PUBLIC',5,755,'button',NULL,'批量删除','{\"en-US\": \"label:template:batchDelete\", \"ja-JP\": \"批量删除\", \"ko-KR\": \"label:template:batchDelete\", \"zh-CN\": \"批量删除\", \"zh-TW\": \"批量删除\"}',NULL,NULL,'label:template:batchDelete',4,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(763,1993479636925403138,'PUBLIC',5,755,'button',NULL,'查看模板','{\"en-US\": \"label:template:view\", \"ja-JP\": \"查看模板\", \"ko-KR\": \"label:template:view\", \"zh-CN\": \"查看模板\", \"zh-TW\": \"查看模板\"}',NULL,NULL,'label:template:view',5,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(764,1993479636925403138,'PUBLIC',5,755,'button',NULL,'设为默认','{\"en-US\": \"label:template:setDefault\", \"ja-JP\": \"设为默认\", \"ko-KR\": \"label:template:setDefault\", \"zh-CN\": \"设为默认\", \"zh-TW\": \"设为默认\"}',NULL,NULL,'label:template:setDefault',6,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(765,1993479636925403138,'PUBLIC',5,755,'button',NULL,'复制模板','{\"en-US\": \"label:template:copy\", \"ja-JP\": \"复制模板\", \"ko-KR\": \"label:template:copy\", \"zh-CN\": \"复制模板\", \"zh-TW\": \"复制模板\"}',NULL,NULL,'label:template:copy',7,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(766,1993479636925403138,'PUBLIC',5,755,'button',NULL,'模板管理','{\"en-US\": \"label:template:manage\", \"ja-JP\": \"模板管理\", \"ko-KR\": \"label:template:manage\", \"zh-CN\": \"模板管理\", \"zh-TW\": \"模板管理\"}',NULL,NULL,'label:template:manage',8,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(767,1993479636925403138,'PUBLIC',5,756,'button',NULL,'执行打印','{\"en-US\": \"label:print:execute\", \"ja-JP\": \"执行打印\", \"ko-KR\": \"label:print:execute\", \"zh-CN\": \"执行打印\", \"zh-TW\": \"执行打印\"}',NULL,NULL,'label:print:execute',1,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(768,1993479636925403138,'PUBLIC',5,756,'button',NULL,'打印设置','{\"en-US\": \"label:print:settings\", \"ja-JP\": \"打印设置\", \"ko-KR\": \"label:print:settings\", \"zh-CN\": \"打印设置\", \"zh-TW\": \"打印设置\"}',NULL,NULL,'label:print:settings',2,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(769,1993479636925403138,'PUBLIC',NULL,757,'button',NULL,'查看详情','{\"en-US\": \"label:record:query\", \"ja-JP\": \"查看详情\", \"ko-KR\": \"label:record:query\", \"zh-CN\": \"查看详情\", \"zh-TW\": \"查看详情\"}',NULL,NULL,'label:record:query',1,1,1,'2026-04-17 13:48:42','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(770,1993479636925403138,'PUBLIC',NULL,757,'button',NULL,'补打','{\"en-US\": \"label:print:reprint\", \"ja-JP\": \"补打\", \"ko-KR\": \"label:print:reprint\", \"zh-CN\": \"补打\", \"zh-TW\": \"补打\"}',NULL,NULL,'label:print:reprint',2,1,1,'2026-04-17 13:48:42','system','2026-04-26 20:56:49','20260426_i18n_fix',0,3,'embedded',NULL),(771,1993479636925403138,'PUBLIC',5,758,'button',NULL,'新增绑定','{\"en-US\": \"label:binding:add\", \"ja-JP\": \"新增绑定\", \"ko-KR\": \"label:binding:add\", \"zh-CN\": \"新增绑定\", \"zh-TW\": \"新增绑定\"}',NULL,NULL,'label:binding:add',1,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(772,1993479636925403138,'PUBLIC',5,758,'button',NULL,'编辑绑定','{\"en-US\": \"label:binding:edit\", \"ja-JP\": \"编辑绑定\", \"ko-KR\": \"label:binding:edit\", \"zh-CN\": \"编辑绑定\", \"zh-TW\": \"编辑绑定\"}',NULL,NULL,'label:binding:edit',2,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(773,1993479636925403138,'PUBLIC',5,758,'button',NULL,'删除绑定','{\"en-US\": \"label:binding:delete\", \"ja-JP\": \"删除绑定\", \"ko-KR\": \"label:binding:delete\", \"zh-CN\": \"删除绑定\", \"zh-TW\": \"删除绑定\"}',NULL,NULL,'label:binding:delete',3,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(774,1993479636925403138,'PUBLIC',5,758,'button',NULL,'绑定管理','{\"en-US\": \"label:binding:manage\", \"ja-JP\": \"绑定管理\", \"ko-KR\": \"label:binding:manage\", \"zh-CN\": \"绑定管理\", \"zh-TW\": \"绑定管理\"}',NULL,NULL,'label:binding:manage',4,1,1,'2026-04-17 10:33:40','system','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(775,1993479636925403138,'PUBLIC',5,754,'menu','type','标签类型','{\"en-US\": \"Label Type\", \"ja-JP\": \"ラベルタイプ\", \"ko-KR\": \"라벨 유형\", \"zh-CN\": \"标签类型\", \"zh-TW\": \"標籤類型\"}','TagsOutlined','LabelType','label:type:view',5,1,1,'2026-05-15 21:13:24','20260515_label_menu_permission_seed','2026-05-15 21:13:24','20260515_label_menu_permission_seed',0,2,'embedded',NULL),(776,1993479636925403138,'PUBLIC',5,754,'menu','field','标签字段','{\"en-US\": \"Label Field\", \"ja-JP\": \"ラベルフィールド\", \"ko-KR\": \"라벨 필드\", \"zh-CN\": \"标签字段\", \"zh-TW\": \"標籤字段\"}','ProfileOutlined','LabelField','label:field:view',6,1,1,'2026-05-15 21:13:24','20260515_label_menu_permission_seed','2026-05-15 21:13:24','20260515_label_menu_permission_seed',0,2,'embedded',NULL),(3000000000000000422,1993479636925403138,'PUBLIC',1,0,'catalog','excelConfig','Excel配置','{\"en-US\": \"Excel Config\", \"ja-JP\": \"Excel設定\", \"ko-KR\": \"Excel 설정\", \"zh-CN\": \"Excel配置\", \"zh-TW\": \"Excel配置\"}','FileExcelOutlined',NULL,NULL,5,1,1,'2026-04-12 10:44:34','codex','2026-04-12 11:11:25','codex',0,1,'embedded',NULL),(3000000000000000423,1993479636925403138,'PUBLIC',1,0,'catalog','pageTableConfig','页表配置','{\"en-US\": \"Page Table Config\", \"ja-JP\": \"ページテーブル設定\", \"ko-KR\": \"페이지 테이블 설정\", \"zh-CN\": \"页表配置\", \"zh-TW\": \"頁表配置\"}','TableOutlined',NULL,NULL,8,1,1,'2026-04-12 10:44:34','codex','2026-04-12 11:11:25','codex',0,1,'embedded',NULL),(3000000000000000424,1993479636925403138,'PUBLIC',1,0,'menu','file','文件管理','{\"en-US\": \"File Management\", \"ja-JP\": \"ファイル管理\", \"ko-KR\": \"파일 관리\", \"zh-CN\": \"文件管理\", \"zh-TW\": \"檔案管理\"}','FolderOpenOutlined','SystemFile','sys:file:view',95,1,1,'2026-04-20 22:37:54','20260420_init','2026-04-26 20:54:50','20260426_i18n_fix',0,1,'embedded',NULL),(3000000000000000425,1993479636925403138,'PUBLIC',1,3000000000000000424,'button',NULL,'文件上传','{\"en-US\": \"sys:file:upload\", \"ja-JP\": \"文件上传\", \"ko-KR\": \"sys:file:upload\", \"zh-CN\": \"文件上传\", \"zh-TW\": \"文件上传\"}',NULL,NULL,'sys:file:upload',1,1,1,'2026-04-20 22:37:54','20260420_init','2026-04-26 20:56:49','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000426,0,'PUBLIC',NULL,0,'menu','/system/codegen','在线开发','{\"en-US\": \"/system/codegen\", \"ja-JP\": \"在线开发\", \"ko-KR\": \"/system/codegen\", \"zh-CN\": \"在线开发\", \"zh-TW\": \"在线开发\"}','CodeOutlined','SystemCodegen',NULL,91,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000427,0,'PUBLIC',NULL,0,'menu','/system/codegenDatasource','代码生成数据源','{\"en-US\": \"/system/codegenDatasource\", \"ja-JP\": \"代码生成数据源\", \"ko-KR\": \"/system/codegenDatasource\", \"zh-CN\": \"代码生成数据源\", \"zh-TW\": \"代码生成数据源\"}','DatabaseOutlined','SystemCodegenDatasource',NULL,92,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000428,0,'PUBLIC',NULL,3000000000000000426,'button',NULL,'代码预览','{\"en-US\": \"sys:codegen:preview\", \"ja-JP\": \"代码预览\", \"ko-KR\": \"sys:codegen:preview\", \"zh-CN\": \"代码预览\", \"zh-TW\": \"代码预览\"}',NULL,NULL,'sys:codegen:preview',1,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000429,0,'PUBLIC',NULL,3000000000000000426,'button',NULL,'下载ZIP','{\"en-US\": \"sys:codegen:download\", \"ja-JP\": \"下载ZIP\", \"ko-KR\": \"sys:codegen:download\", \"zh-CN\": \"下载ZIP\", \"zh-TW\": \"下载ZIP\"}',NULL,NULL,'sys:codegen:download',2,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000430,0,'PUBLIC',NULL,3000000000000000427,'button',NULL,'查询','{\"en-US\": \"sys:codegenDatasource:page\", \"ja-JP\": \"查询\", \"ko-KR\": \"sys:codegenDatasource:page\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}',NULL,NULL,'sys:codegenDatasource:page',1,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000431,0,'PUBLIC',NULL,3000000000000000427,'button',NULL,'新增','{\"en-US\": \"sys:codegenDatasource:save\", \"ja-JP\": \"新增\", \"ko-KR\": \"sys:codegenDatasource:save\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'sys:codegenDatasource:save',2,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000432,0,'PUBLIC',NULL,3000000000000000427,'button',NULL,'删除','{\"en-US\": \"sys:codegenDatasource:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegenDatasource:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'sys:codegenDatasource:delete',3,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000433,0,'PUBLIC',NULL,3000000000000000427,'button',NULL,'测试连接','{\"en-US\": \"sys:codegenDatasource:test\", \"ja-JP\": \"测试连接\", \"ko-KR\": \"sys:codegenDatasource:test\", \"zh-CN\": \"测试连接\", \"zh-TW\": \"测试连接\"}',NULL,NULL,'sys:codegenDatasource:test',4,1,1,'2026-04-21 16:48:30','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000434,1993479636925403138,'PUBLIC',1,3000000000000000443,'menu','codegen','代码生成','{\"en-US\": \"Code Generation\", \"ja-JP\": \"コード生成\", \"ko-KR\": \"코드 생성\", \"zh-CN\": \"代码生成\", \"zh-TW\": \"程式碼生成\"}','CodeOutlined','SystemCodegen','sys:codegen:view',1,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000435,1993479636925403138,'PUBLIC',1,3000000000000000443,'menu','codegenDatasource','代码生成数据源','{\"en-US\": \"Codegen Datasource\", \"ja-JP\": \"コード生成データソース\", \"ko-KR\": \"코드 생성 데이터소스\", \"zh-CN\": \"代码生成数据源\", \"zh-TW\": \"程式碼生成資料來源\"}','DatabaseOutlined','SystemCodegenDatasource','sys:codegenDatasource:view',2,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000436,1993479636925403138,'PUBLIC',1,3000000000000000434,'button',NULL,'预览','{\"en-US\": \"sys:codegen:preview\", \"ja-JP\": \"预览\", \"ko-KR\": \"sys:codegen:preview\", \"zh-CN\": \"预览\", \"zh-TW\": \"预览\"}',NULL,NULL,'sys:codegen:preview',2,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000437,1993479636925403138,'PUBLIC',1,3000000000000000434,'button',NULL,'下载ZIP','{\"en-US\": \"sys:codegen:download\", \"ja-JP\": \"下载ZIP\", \"ko-KR\": \"sys:codegen:download\", \"zh-CN\": \"下载ZIP\", \"zh-TW\": \"下载ZIP\"}',NULL,NULL,'sys:codegen:download',3,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000438,1993479636925403138,'PUBLIC',1,3000000000000000435,'button',NULL,'保存','{\"en-US\": \"sys:codegenDatasource:save\", \"ja-JP\": \"保存\", \"ko-KR\": \"sys:codegenDatasource:save\", \"zh-CN\": \"保存\", \"zh-TW\": \"保存\"}',NULL,NULL,'sys:codegenDatasource:save',2,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000439,1993479636925403138,'PUBLIC',1,3000000000000000435,'button',NULL,'删除','{\"en-US\": \"sys:codegenDatasource:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegenDatasource:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'sys:codegenDatasource:delete',3,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000440,1993479636925403138,'PUBLIC',1,3000000000000000435,'button',NULL,'测试连接','{\"en-US\": \"sys:codegenDatasource:test\", \"ja-JP\": \"测试连接\", \"ko-KR\": \"sys:codegenDatasource:test\", \"zh-CN\": \"测试连接\", \"zh-TW\": \"测试连接\"}',NULL,NULL,'sys:codegenDatasource:test',4,1,1,'2026-04-21 17:07:40','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000441,1993479636925403138,'PUBLIC',1,3000000000000000434,'button',NULL,'保存配置','{\"en-US\": \"sys:codegen:save\", \"ja-JP\": \"保存配置\", \"ko-KR\": \"sys:codegen:save\", \"zh-CN\": \"保存配置\", \"zh-TW\": \"保存配置\"}',NULL,NULL,'sys:codegen:save',2,1,1,'2026-04-21 18:42:26','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000442,1993479636925403138,'PUBLIC',1,3000000000000000434,'button',NULL,'删除','{\"en-US\": \"sys:codegen:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegen:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'sys:codegen:delete',5,1,1,'2026-04-21 18:42:26','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,3,NULL,NULL),(3000000000000000443,1993479636925403138,'PUBLIC',1,0,'catalog','onlineDev','在线开发','{\"en-US\": \"onlineDev\", \"ja-JP\": \"在线开发\", \"ko-KR\": \"onlineDev\", \"zh-CN\": \"在线开发\", \"zh-TW\": \"在线开发\"}','CodeOutlined',NULL,NULL,95,1,1,'2026-04-22 10:13:32','1993479637244170242','2026-05-06 17:29:21','20260426_i18n_fix',0,1,NULL,NULL),(3000000000000000444,1993479636925403138,'PUBLIC',1,1,'button',NULL,'同步第三方','{\"en-US\": \"Sync Third Party\", \"ja-JP\": \"第三方同期\", \"ko-KR\": \"제3자 동기화\", \"zh-CN\": \"同步第三方\", \"zh-TW\": \"同步第三方\"}',NULL,NULL,'sys:user:syncThirdParty',8,1,1,'2026-04-22 19:10:35','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(3000000000000000445,1993479636925403138,'PUBLIC',1,1,'button',NULL,'从第三方拉取','{\"en-US\": \"Pull From Third Party\", \"ja-JP\": \"第三方から取得\", \"ko-KR\": \"제3자에서 가져오기\", \"zh-CN\": \"从第三方拉取\", \"zh-TW\": \"從第三方拉取\"}',NULL,NULL,'sys:user:pullThirdParty',9,1,1,'2026-04-22 19:10:35','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(3000000000000000446,1993479636925403138,'PUBLIC',1,1,'button',NULL,'导入用户','{\"en-US\": \"Import Users\", \"ja-JP\": \"ユーザーインポート\", \"ko-KR\": \"사용자 가져오기\", \"zh-CN\": \"导入用户\", \"zh-TW\": \"導入用戶\"}',NULL,NULL,'sys:user:import',10,1,1,'2026-04-22 19:10:35','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(3000000000000000447,1993479636925403138,'PUBLIC',1,1,'button',NULL,'下载模板','{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}',NULL,NULL,'sys:user:downloadTemplate',11,1,1,'2026-04-22 19:10:35','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,2,'embedded',NULL),(3000000000000000448,1993479636925403138,'PUBLIC',5,0,'menu','supplier','供应商管理','{\"en-US\": \"Supplier Management\", \"ja-JP\": \"サプライヤー管理\", \"ko-KR\": \"공급업체 관리\", \"zh-CN\": \"供应商管理\", \"zh-TW\": \"供應商管理\"}','ApartmentOutlined','BasicSupplier','basic:supplier:query',20,1,1,'2026-04-26 16:26:06','system','2026-04-29 22:20:00','system',0,1,'embedded',NULL),(3000000000000000449,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'查询','{\"en-US\": \"Query\", \"ja-JP\": \"検索\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'basic:supplier:query',1,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000450,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:supplier:add',2,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000451,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'basic:supplier:edit',3,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000452,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'basic:supplier:delete',4,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000453,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'导入','{\"en-US\": \"Import\", \"ja-JP\": \"インポート\", \"ko-KR\": \"가져오기\", \"zh-CN\": \"导入\", \"zh-TW\": \"匯入\"}',NULL,NULL,'basic:supplier:import',5,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000454,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'导出','{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"내보내기\", \"zh-CN\": \"导出\", \"zh-TW\": \"匯出\"}',NULL,NULL,'basic:supplier:export',6,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000455,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'生成租户','{\"en-US\": \"Generate Tenant\", \"ja-JP\": \"テナント生成\", \"ko-KR\": \"테넌트 생성\", \"zh-CN\": \"生成租户\", \"zh-TW\": \"產生租戶\"}',NULL,NULL,'basic:supplier:generateTenant',7,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000456,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'发起审查','{\"en-US\": \"Start Review\", \"ja-JP\": \"審査開始\", \"ko-KR\": \"심사 시작\", \"zh-CN\": \"发起审查\", \"zh-TW\": \"發起審查\"}',NULL,NULL,'basic:supplier:review',8,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000457,1993479636925403138,'PUBLIC',5,3000000000000000448,'button',NULL,'同步第三方','{\"en-US\": \"Sync Third Party\", \"ja-JP\": \"サードパーティ同期\", \"ko-KR\": \"타사 동기화\", \"zh-CN\": \"同步第三方\", \"zh-TW\": \"同步第三方\"}',NULL,NULL,'basic:supplier:sync',9,1,1,'2026-04-26 16:26:06','system','2026-04-26 21:25:41','20260426_i18n_fix',0,2,'embedded',NULL),(3000000000000000458,1993479636925403138,'PUBLIC',5,0,'menu','customer','客户管理','{\"en-US\": \"Customer Management\", \"ja-JP\": \"顧客管理\", \"ko-KR\": \"고객 관리\", \"zh-CN\": \"客户管理\", \"zh-TW\": \"客戶管理\"}','TeamOutlined','BasicCustomer','basic:customer:query',30,1,1,'2026-04-29 21:31:09','system','2026-04-29 22:20:00','system',0,1,'embedded',NULL),(3000000000000000459,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'查询','{\"en-US\": \"查询\", \"ja-JP\": \"查询\", \"ko-KR\": \"查询\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}',NULL,NULL,'basic:customer:query',1,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000460,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'新增','{\"en-US\": \"新增\", \"ja-JP\": \"新增\", \"ko-KR\": \"新增\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:customer:add',2,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000461,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'编辑','{\"en-US\": \"编辑\", \"ja-JP\": \"编辑\", \"ko-KR\": \"编辑\", \"zh-CN\": \"编辑\", \"zh-TW\": \"编辑\"}',NULL,NULL,'basic:customer:edit',3,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000462,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'删除','{\"en-US\": \"删除\", \"ja-JP\": \"删除\", \"ko-KR\": \"删除\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'basic:customer:delete',4,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000463,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'生成租户','{\"en-US\": \"生成租户\", \"ja-JP\": \"生成租户\", \"ko-KR\": \"生成租户\", \"zh-CN\": \"生成租户\", \"zh-TW\": \"生成租户\"}',NULL,NULL,'basic:customer:generateTenant',5,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000464,1993479636925403138,'PUBLIC',5,3000000000000000458,'button',NULL,'发起审批','{\"en-US\": \"发起审批\", \"ja-JP\": \"发起审批\", \"ko-KR\": \"发起审批\", \"zh-CN\": \"发起审批\", \"zh-TW\": \"发起审批\"}',NULL,NULL,'basic:customer:approval',6,1,1,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',0,3,'embedded',NULL),(3000000000000000466,1993479636925403138,'PUBLIC',5,0,'menu','material','物料管理','{\"en-US\": \"Material Management\", \"ja-JP\": \"品目管理\", \"ko-KR\": \"자재 관리\", \"zh-CN\": \"物料管理\", \"zh-TW\": \"物料管理\"}','AppstoreOutlined','BasicMaterial','basic:material:query',40,1,1,'2026-04-29 21:31:09','system','2026-04-30 00:17:44','system',0,1,'embedded',NULL),(3000000000000000467,1993479636925403138,'PUBLIC',5,3000000000000000466,'menu','raw','原材料','{\"en-US\": \"Raw Material\", \"ja-JP\": \"原材料\", \"ko-KR\": \"원자재\", \"zh-CN\": \"原材料\", \"zh-TW\": \"原材料\"}','InboxOutlined','BasicMaterialRaw','basic:material:query',10,0,0,'2026-04-29 21:31:09','system','2026-04-29 23:46:40','system',1,2,'embedded',NULL),(3000000000000000468,1993479636925403138,'PUBLIC',5,3000000000000000466,'menu','semi-finished','半成品','{\"en-US\": \"Semi-finished Material\", \"ja-JP\": \"半製品\", \"ko-KR\": \"반제품\", \"zh-CN\": \"半成品\", \"zh-TW\": \"半成品\"}','BuildOutlined','BasicMaterialSemiFinished','basic:material:query',20,0,0,'2026-04-29 21:31:09','system','2026-04-29 23:46:40','system',1,2,'embedded',NULL),(3000000000000000469,1993479636925403138,'PUBLIC',5,3000000000000000466,'menu','finished','成品','{\"en-US\": \"Finished Goods\", \"ja-JP\": \"完成品\", \"ko-KR\": \"완제품\", \"zh-CN\": \"成品\", \"zh-TW\": \"成品\"}','GiftOutlined','BasicMaterialFinished','basic:material:query',30,0,0,'2026-04-29 21:31:09','system','2026-04-29 23:46:40','system',1,2,'embedded',NULL),(3000000000000000470,1993479636925403138,'PUBLIC',5,3000000000000000469,'button',NULL,'查询','{\"en-US\": \"查询\", \"ja-JP\": \"查询\", \"ko-KR\": \"查询\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}',NULL,NULL,'basic:material:query',1,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000471,1993479636925403138,'PUBLIC',5,3000000000000000468,'button',NULL,'查询','{\"en-US\": \"查询\", \"ja-JP\": \"查询\", \"ko-KR\": \"查询\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}',NULL,NULL,'basic:material:query',1,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000472,1993479636925403138,'PUBLIC',5,3000000000000000467,'button',NULL,'查询','{\"en-US\": \"查询\", \"ja-JP\": \"查询\", \"ko-KR\": \"查询\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}',NULL,NULL,'basic:material:query',1,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000473,1993479636925403138,'PUBLIC',5,3000000000000000469,'button',NULL,'新增','{\"en-US\": \"新增\", \"ja-JP\": \"新增\", \"ko-KR\": \"新增\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:material:add',2,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000474,1993479636925403138,'PUBLIC',5,3000000000000000468,'button',NULL,'新增','{\"en-US\": \"新增\", \"ja-JP\": \"新增\", \"ko-KR\": \"新增\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:material:add',2,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000475,1993479636925403138,'PUBLIC',5,3000000000000000467,'button',NULL,'新增','{\"en-US\": \"新增\", \"ja-JP\": \"新增\", \"ko-KR\": \"新增\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:material:add',2,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000476,1993479636925403138,'PUBLIC',5,3000000000000000469,'button',NULL,'编辑','{\"en-US\": \"编辑\", \"ja-JP\": \"编辑\", \"ko-KR\": \"编辑\", \"zh-CN\": \"编辑\", \"zh-TW\": \"编辑\"}',NULL,NULL,'basic:material:edit',3,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000477,1993479636925403138,'PUBLIC',5,3000000000000000468,'button',NULL,'编辑','{\"en-US\": \"编辑\", \"ja-JP\": \"编辑\", \"ko-KR\": \"编辑\", \"zh-CN\": \"编辑\", \"zh-TW\": \"编辑\"}',NULL,NULL,'basic:material:edit',3,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000478,1993479636925403138,'PUBLIC',5,3000000000000000467,'button',NULL,'编辑','{\"en-US\": \"编辑\", \"ja-JP\": \"编辑\", \"ko-KR\": \"编辑\", \"zh-CN\": \"编辑\", \"zh-TW\": \"编辑\"}',NULL,NULL,'basic:material:edit',3,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000479,1993479636925403138,'PUBLIC',5,3000000000000000469,'button',NULL,'删除','{\"en-US\": \"删除\", \"ja-JP\": \"删除\", \"ko-KR\": \"删除\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'basic:material:delete',4,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000480,1993479636925403138,'PUBLIC',5,3000000000000000468,'button',NULL,'删除','{\"en-US\": \"删除\", \"ja-JP\": \"删除\", \"ko-KR\": \"删除\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'basic:material:delete',4,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000481,1993479636925403138,'PUBLIC',5,3000000000000000467,'button',NULL,'删除','{\"en-US\": \"删除\", \"ja-JP\": \"删除\", \"ko-KR\": \"删除\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'basic:material:delete',4,0,0,'2026-04-29 21:31:09','system','2026-05-08 12:03:51','system',1,3,'embedded',NULL),(3000000000000000485,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'新增物料','{\"en-US\": \"Add Material\", \"ja-JP\": \"Add Material\", \"ko-KR\": \"Add Material\", \"zh-CN\": \"新增物料\", \"zh-TW\": \"新增物料\"}',NULL,NULL,'basic:material:add',1,1,1,'2026-04-29 23:46:40','system','2026-04-29 23:46:40','system',0,2,'embedded',NULL),(3000000000000000486,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'编辑物料','{\"en-US\": \"Edit Material\", \"ja-JP\": \"Edit Material\", \"ko-KR\": \"Edit Material\", \"zh-CN\": \"编辑物料\", \"zh-TW\": \"编辑物料\"}',NULL,NULL,'basic:material:edit',2,1,1,'2026-04-29 23:46:40','system','2026-04-29 23:46:40','system',0,2,'embedded',NULL),(3000000000000000487,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'删除物料','{\"en-US\": \"Delete Material\", \"ja-JP\": \"Delete Material\", \"ko-KR\": \"Delete Material\", \"zh-CN\": \"删除物料\", \"zh-TW\": \"删除物料\"}',NULL,NULL,'basic:material:delete',3,1,1,'2026-04-29 23:46:40','system','2026-04-29 23:46:40','system',0,2,'embedded',NULL),(3000000000000000488,1993479636925403138,'PUBLIC',5,0,'menu','unit','计量单位','{\"en-US\": \"Units of Measure\", \"ja-JP\": \"計量単位\", \"ko-KR\": \"계량 단위\", \"zh-CN\": \"计量单位\", \"zh-TW\": \"計量單位\"}','ColumnWidthOutlined','BasicUnit','basic:unit:query',50,1,1,'2026-05-02 20:54:41','20260502_basic_unit_and_table_upgrade','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0,1,'embedded',NULL),(3000000000000000489,1993479636925403138,'PUBLIC',5,3000000000000000488,'button',NULL,'查询','{\"en-US\": \"Query\", \"ja-JP\": \"検索\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'basic:unit:query',1,1,1,'2026-05-02 20:54:41','20260502_basic_unit_and_table_upgrade','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0,2,'embedded',NULL),(3000000000000000490,1993479636925403138,'PUBLIC',5,3000000000000000488,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'basic:unit:add',2,1,1,'2026-05-02 20:54:41','20260502_basic_unit_and_table_upgrade','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0,2,'embedded',NULL),(3000000000000000491,1993479636925403138,'PUBLIC',5,3000000000000000488,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'basic:unit:edit',3,1,1,'2026-05-02 20:54:41','20260502_basic_unit_and_table_upgrade','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0,2,'embedded',NULL),(3000000000000000492,1993479636925403138,'PUBLIC',5,3000000000000000488,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'basic:unit:delete',4,1,1,'2026-05-02 20:54:41','20260502_basic_unit_and_table_upgrade','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0,2,'embedded',NULL),(3000000000000000493,1993479636925403138,'PUBLIC',1,654,'menu','androidVersion','安卓版本管理','{\"en-US\": \"Android Version\", \"ja-JP\": \"Androidバージョン管理\", \"ko-KR\": \"안드로이드 버전 관리\", \"zh-CN\": \"安卓版本管理\", \"zh-TW\": \"安卓版本管理\"}','AndroidOutlined','SystemAndroidVersion','sys:androidVersion:view',6,1,1,'2026-05-05 23:01:45','20260505_android_version_management','2026-05-05 23:01:45','20260505_android_version_management',0,2,'embedded',NULL),(3000000000000000494,1993479636925403138,'PUBLIC',1,3000000000000000493,'button','view','查询','{\"en-US\": \"Query\", \"ja-JP\": \"照会\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'sys:androidVersion:view',1,1,1,'2026-05-05 23:01:45','20260505_android_version_management','2026-05-05 23:01:45','20260505_android_version_management',0,3,'embedded',NULL),(3000000000000000495,1993479636925403138,'PUBLIC',1,3000000000000000493,'button','add','上传版本','{\"en-US\": \"Upload Version\", \"ja-JP\": \"バージョンアップロード\", \"ko-KR\": \"버전 업로드\", \"zh-CN\": \"上传版本\", \"zh-TW\": \"上傳版本\"}',NULL,NULL,'sys:androidVersion:add',2,1,1,'2026-05-05 23:01:45','20260505_android_version_management','2026-05-05 23:01:45','20260505_android_version_management',0,3,'embedded',NULL),(3000000000000000496,1993479636925403138,'PUBLIC',1,3000000000000000493,'button','edit','编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'sys:androidVersion:edit',3,1,1,'2026-05-05 23:01:45','20260505_android_version_management','2026-05-05 23:01:45','20260505_android_version_management',0,3,'embedded',NULL),(3000000000000000497,1993479636925403138,'PUBLIC',1,3000000000000000493,'button','delete','删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:androidVersion:delete',4,1,1,'2026-05-05 23:01:45','20260505_android_version_management','2026-05-05 23:01:45','20260505_android_version_management',0,3,'embedded',NULL),(3000000000000000551,1993479636925403138,'PUBLIC',1,654,'menu','notice','系统通知','{\"en-US\": \"System Notice\", \"ja-JP\": \"システム通知\", \"ko-KR\": \"시스템 알림\", \"zh-CN\": \"系统通知\", \"zh-TW\": \"系統通知\"}','NotificationOutlined','SystemNotice','sys:notice:view',5,1,1,'2026-05-10 21:13:26','codex','2026-05-10 22:56:46','codex',0,2,'embedded',NULL),(3000000000000000552,1993479636925403138,'PUBLIC',1,3000000000000000551,'button',NULL,'查看系统通知','{\"en-US\": \"View System Notice\", \"ja-JP\": \"システム通知表示\", \"ko-KR\": \"시스템 알림 보기\", \"zh-CN\": \"查看系统通知\", \"zh-TW\": \"查看系統通知\"}',NULL,NULL,'sys:notice:view',1,1,1,'2026-05-10 21:26:44','codex','2026-05-10 22:56:46','codex',0,3,'embedded',NULL),(3000000000000000553,1993479636925403138,'PUBLIC',1,3000000000000000551,'button',NULL,'新增系统通知','{\"en-US\": \"Add System Notice\", \"ja-JP\": \"システム通知追加\", \"ko-KR\": \"시스템 알림 추가\", \"zh-CN\": \"新增系统通知\", \"zh-TW\": \"新增系統通知\"}',NULL,NULL,'sys:notice:add',2,1,1,'2026-05-10 21:26:44','codex','2026-05-10 22:56:46','codex',0,3,'embedded',NULL),(3000000000000000554,1993479636925403138,'PUBLIC',1,3000000000000000551,'button',NULL,'编辑系统通知','{\"en-US\": \"Edit System Notice\", \"ja-JP\": \"システム通知編集\", \"ko-KR\": \"시스템 알림 편집\", \"zh-CN\": \"编辑系统通知\", \"zh-TW\": \"編輯系統通知\"}',NULL,NULL,'sys:notice:edit',3,1,1,'2026-05-10 21:26:44','codex','2026-05-10 22:56:46','codex',0,3,'embedded',NULL),(3000000000000000555,1993479636925403138,'PUBLIC',1,3000000000000000551,'button',NULL,'删除系统通知','{\"en-US\": \"Delete System Notice\", \"ja-JP\": \"システム通知削除\", \"ko-KR\": \"시스템 알림 삭제\", \"zh-CN\": \"删除系统通知\", \"zh-TW\": \"刪除系統通知\"}',NULL,NULL,'sys:notice:delete',4,1,1,'2026-05-10 21:26:44','codex','2026-05-10 22:56:46','codex',0,3,'embedded',NULL),(3000000000000000556,1993479636925403138,'PUBLIC',1,3000000000000000551,'button',NULL,'发布/停用系统通知','{\"en-US\": \"Publish or Disable System Notice\", \"ja-JP\": \"システム通知公開/無効化\", \"ko-KR\": \"시스템 알림 게시/중지\", \"zh-CN\": \"发布/停用系统通知\", \"zh-TW\": \"發布/停用系統通知\"}',NULL,NULL,'sys:notice:publish',5,1,1,'2026-05-10 21:26:44','codex','2026-05-10 22:56:46','codex',0,3,'embedded',NULL),(3000000000000000557,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'导入物料','{\"en-US\": \"Import Material\", \"ja-JP\": \"品目インポート\", \"ko-KR\": \"자재 가져오기\", \"zh-CN\": \"导入物料\", \"zh-TW\": \"匯入物料\"}',NULL,'BasicMaterialImport','basic:material:import',41,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000558,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'从第三方拉取物料','{\"en-US\": \"Pull Material From Third Party\", \"ja-JP\": \"外部から品目取得\", \"ko-KR\": \"타사에서 자재 가져오기\", \"zh-CN\": \"从第三方拉取物料\", \"zh-TW\": \"從第三方拉取物料\"}',NULL,'BasicMaterialPullThirdParty','basic:material:pullThirdParty',42,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000559,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'同步第三方物料','{\"en-US\": \"Sync Material To Third Party\", \"ja-JP\": \"外部へ品目同期\", \"ko-KR\": \"타사로 자재 동기화\", \"zh-CN\": \"同步第三方物料\", \"zh-TW\": \"同步第三方物料\"}',NULL,'BasicMaterialSyncThirdParty','basic:material:sync',43,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000560,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'附属字段查询','{\"en-US\": \"Extend Field Query\", \"ja-JP\": \"付属項目照会\", \"ko-KR\": \"부속 필드 조회\", \"zh-CN\": \"附属字段查询\", \"zh-TW\": \"附屬欄位查詢\"}',NULL,'BasicMaterialExtendConfigQuery','basic:material:extendConfig:query',44,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000561,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'附属字段新增','{\"en-US\": \"Extend Field Add\", \"ja-JP\": \"付属項目追加\", \"ko-KR\": \"부속 필드 추가\", \"zh-CN\": \"附属字段新增\", \"zh-TW\": \"附屬欄位新增\"}',NULL,'BasicMaterialExtendConfigAdd','basic:material:extendConfig:add',45,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000562,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'附属字段编辑','{\"en-US\": \"Extend Field Edit\", \"ja-JP\": \"付属項目編集\", \"ko-KR\": \"부속 필드 편집\", \"zh-CN\": \"附属字段编辑\", \"zh-TW\": \"附屬欄位編輯\"}',NULL,'BasicMaterialExtendConfigEdit','basic:material:extendConfig:edit',46,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000563,1993479636925403138,'PUBLIC',5,3000000000000000466,'button',NULL,'附属字段删除','{\"en-US\": \"Extend Field Delete\", \"ja-JP\": \"付属項目削除\", \"ko-KR\": \"부속 필드 삭제\", \"zh-CN\": \"附属字段删除\", \"zh-TW\": \"附屬欄位刪除\"}',NULL,'BasicMaterialExtendConfigDelete','basic:material:extendConfig:delete',47,0,1,'2026-05-12 19:07:29','20260512_material_import_sync_upgrade','2026-05-12 19:07:29','20260512_material_import_sync_upgrade',0,3,'embedded',NULL),(3000000000000000564,1993479636925403140,'PUBLIC',7,3000000000000000635,'menu','user','用户管理','{\"en-US\": \"Users\", \"ja-JP\": \"ユーザー管理\", \"ko-KR\": \"사용자 관리\", \"zh-CN\": \"用户管理\", \"zh-TW\": \"用戶管理\"}','UserOutlined','SystemUser','sys:user:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000565,1993479636925403140,'PUBLIC',7,3000000000000000636,'menu','role','角色管理','{\"en-US\": \"Roles\", \"ja-JP\": \"ロール管理\", \"ko-KR\": \"역할 관리\", \"zh-CN\": \"角色管理\", \"zh-TW\": \"角色管理\"}','TeamOutlined','SystemRole','sys:role:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000566,1993479636925403140,'PUBLIC',7,0,'menu','module','模块管理','{\"en-US\": \"Modules\", \"ja-JP\": \"モジュール管理\", \"ko-KR\": \"모듈 관리\", \"zh-CN\": \"模块管理\", \"zh-TW\": \"模塊管理\"}','AppstoreOutlined','SystemModule','sys:module:view',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000567,1993479636925403140,'PUBLIC',7,3000000000000000636,'menu','menu','菜单管理','{\"en-US\": \"Menus\", \"ja-JP\": \"メニュー管理\", \"ko-KR\": \"메뉴 관리\", \"zh-CN\": \"菜单管理\", \"zh-TW\": \"選單管理\"}','MenuOutlined','SystemMenu','sys:menu:view',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000568,1993479636925403140,'PUBLIC',7,3000000000000000635,'menu','department','部门管理','{\"en-US\": \"Departments\", \"ja-JP\": \"部門管理\", \"ko-KR\": \"부서 관리\", \"zh-CN\": \"部门管理\", \"zh-TW\": \"部門管理\"}','ApartmentOutlined','SystemDepartment','sys:dept:view',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000569,1993479636925403140,'PUBLIC',7,3000000000000000635,'menu','position','职位管理','{\"en-US\": \"Positions\", \"ja-JP\": \"職位管理\", \"ko-KR\": \"직위 관리\", \"zh-CN\": \"职位管理\", \"zh-TW\": \"職位管理\"}','IdcardOutlined','SystemPosition','sys:position:view',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000570,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'新增用户','{\"en-US\": \"Add User\", \"ja-JP\": \"ユーザー追加\", \"ko-KR\": \"사용자 추가\", \"zh-CN\": \"新增用户\", \"zh-TW\": \"新增用戶\"}',NULL,NULL,'sys:user:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000571,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'编辑用户','{\"en-US\": \"Edit User\", \"ja-JP\": \"ユーザー編集\", \"ko-KR\": \"사용자 편집\", \"zh-CN\": \"编辑用户\", \"zh-TW\": \"編輯用戶\"}',NULL,NULL,'sys:user:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000572,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'删除用户','{\"en-US\": \"Delete User\", \"ja-JP\": \"ユーザー削除\", \"ko-KR\": \"사용자 삭제\", \"zh-CN\": \"删除用户\", \"zh-TW\": \"刪除用戶\"}',NULL,NULL,'sys:user:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000573,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'批量删除用户','{\"en-US\": \"Batch Delete Users\", \"ja-JP\": \"一括削除ユーザー\", \"ko-KR\": \"일괄 삭제 사용자\", \"zh-CN\": \"批量删除用户\", \"zh-TW\": \"批量刪除用戶\"}',NULL,NULL,'sys:user:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000574,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'重置密码','{\"en-US\": \"Reset Password\", \"ja-JP\": \"パスワードリセット\", \"ko-KR\": \"비밀번호 재설정\", \"zh-CN\": \"重置密码\", \"zh-TW\": \"重置密碼\"}',NULL,NULL,'sys:user:resetPwd',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000575,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'导出用户','{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}',NULL,NULL,'sys:user:export',6,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000576,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'新增角色','{\"en-US\": \"Add Role\", \"ja-JP\": \"ロール追加\", \"ko-KR\": \"역할 추가\", \"zh-CN\": \"新增角色\", \"zh-TW\": \"新增角色\"}',NULL,NULL,'sys:role:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000577,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'编辑角色','{\"en-US\": \"Edit Role\", \"ja-JP\": \"ロール編集\", \"ko-KR\": \"역할 편집\", \"zh-CN\": \"编辑角色\", \"zh-TW\": \"編輯角色\"}',NULL,NULL,'sys:role:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000578,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'删除角色','{\"en-US\": \"Delete Role\", \"ja-JP\": \"ロール削除\", \"ko-KR\": \"역할 삭제\", \"zh-CN\": \"删除角色\", \"zh-TW\": \"刪除角色\"}',NULL,NULL,'sys:role:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000579,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'批量删除角色','{\"en-US\": \"Batch Delete Roles\", \"ja-JP\": \"一括削除ロール\", \"ko-KR\": \"일괄 삭제 역할\", \"zh-CN\": \"批量删除角色\", \"zh-TW\": \"批量刪除角色\"}',NULL,NULL,'sys:role:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000580,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'菜单授权','{\"en-US\": \"Menu Authorization\", \"ja-JP\": \"メニュー認可\", \"ko-KR\": \"메뉴 인증\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"選單授權\"}',NULL,NULL,'sys:role:authMenu',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000581,1993479636925403140,'PUBLIC',7,3000000000000000566,'button',NULL,'新增模块','{\"en-US\": \"Add Module\", \"ja-JP\": \"モジュール追加\", \"ko-KR\": \"모듈 추가\", \"zh-CN\": \"新增模块\", \"zh-TW\": \"新增模塊\"}',NULL,NULL,'sys:module:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000582,1993479636925403140,'PUBLIC',7,3000000000000000566,'button',NULL,'编辑模块','{\"en-US\": \"Edit Module\", \"ja-JP\": \"モジュール編集\", \"ko-KR\": \"모듈 편집\", \"zh-CN\": \"编辑模块\", \"zh-TW\": \"編輯模塊\"}',NULL,NULL,'sys:module:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000583,1993479636925403140,'PUBLIC',7,3000000000000000566,'button',NULL,'删除模块','{\"en-US\": \"Delete Module\", \"ja-JP\": \"モジュール削除\", \"ko-KR\": \"모듈 삭제\", \"zh-CN\": \"删除模块\", \"zh-TW\": \"刪除模塊\"}',NULL,NULL,'sys:module:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000584,1993479636925403140,'PUBLIC',7,3000000000000000566,'button',NULL,'批量删除模块','{\"en-US\": \"Batch Delete Modules\", \"ja-JP\": \"一括削除モジュール\", \"ko-KR\": \"일괄 삭제 모듈\", \"zh-CN\": \"批量删除模块\", \"zh-TW\": \"批量刪除模塊\"}',NULL,NULL,'sys:module:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000585,1993479636925403140,'PUBLIC',7,3000000000000000567,'button',NULL,'新增菜单','{\"en-US\": \"Add Menu\", \"ja-JP\": \"メニュー追加\", \"ko-KR\": \"메뉴 추가\", \"zh-CN\": \"新增菜单\", \"zh-TW\": \"新增選單\"}',NULL,NULL,'sys:menu:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000586,1993479636925403140,'PUBLIC',7,3000000000000000567,'button',NULL,'编辑菜单','{\"en-US\": \"Edit Menu\", \"ja-JP\": \"メニュー編集\", \"ko-KR\": \"메뉴 편집\", \"zh-CN\": \"编辑菜单\", \"zh-TW\": \"編輯選單\"}',NULL,NULL,'sys:menu:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000587,1993479636925403140,'PUBLIC',7,3000000000000000567,'button',NULL,'删除菜单','{\"en-US\": \"Delete Menu\", \"ja-JP\": \"メニュー削除\", \"ko-KR\": \"메뉴 삭제\", \"zh-CN\": \"删除菜单\", \"zh-TW\": \"刪除選單\"}',NULL,NULL,'sys:menu:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000588,1993479636925403140,'PUBLIC',7,3000000000000000567,'button',NULL,'批量删除菜单','{\"en-US\": \"Batch Delete Menus\", \"ja-JP\": \"一括削除メニュー\", \"ko-KR\": \"일괄 삭제 메뉴\", \"zh-CN\": \"批量删除菜单\", \"zh-TW\": \"批量刪除選單\"}',NULL,NULL,'sys:menu:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000589,1993479636925403140,'PUBLIC',7,3000000000000000568,'button',NULL,'新增部门','{\"en-US\": \"Add Department\", \"ja-JP\": \"部門追加\", \"ko-KR\": \"부서 추가\", \"zh-CN\": \"新增部门\", \"zh-TW\": \"新增部門\"}',NULL,NULL,'sys:dept:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000590,1993479636925403140,'PUBLIC',7,3000000000000000568,'button',NULL,'编辑部门','{\"en-US\": \"Edit Department\", \"ja-JP\": \"部門編集\", \"ko-KR\": \"부서 편집\", \"zh-CN\": \"编辑部门\", \"zh-TW\": \"編輯部門\"}',NULL,NULL,'sys:dept:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000591,1993479636925403140,'PUBLIC',7,3000000000000000568,'button',NULL,'删除部门','{\"en-US\": \"Delete Department\", \"ja-JP\": \"部門削除\", \"ko-KR\": \"부서 삭제\", \"zh-CN\": \"删除部门\", \"zh-TW\": \"刪除部門\"}',NULL,NULL,'sys:dept:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000592,1993479636925403140,'PUBLIC',7,3000000000000000569,'button',NULL,'新增职位','{\"en-US\": \"Add Position\", \"ja-JP\": \"職位追加\", \"ko-KR\": \"직위 추가\", \"zh-CN\": \"新增职位\", \"zh-TW\": \"新增職位\"}',NULL,NULL,'sys:position:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000593,1993479636925403140,'PUBLIC',7,3000000000000000569,'button',NULL,'编辑职位','{\"en-US\": \"Edit Position\", \"ja-JP\": \"職位編集\", \"ko-KR\": \"직위 편집\", \"zh-CN\": \"编辑职位\", \"zh-TW\": \"編輯職位\"}',NULL,NULL,'sys:position:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000594,1993479636925403140,'PUBLIC',7,3000000000000000569,'button',NULL,'删除职位','{\"en-US\": \"Delete Position\", \"ja-JP\": \"職位削除\", \"ko-KR\": \"직위 삭제\", \"zh-CN\": \"删除职位\", \"zh-TW\": \"刪除職位\"}',NULL,NULL,'sys:position:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000595,1993479636925403140,'PUBLIC',7,3000000000000000569,'button',NULL,'批量删除职位','{\"en-US\": \"Batch Delete Positions\", \"ja-JP\": \"一括削除職位\", \"ko-KR\": \"일괄 삭제 직위\", \"zh-CN\": \"批量删除职位\", \"zh-TW\": \"批量刪除職位\"}',NULL,NULL,'sys:position:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000596,1993479636925403140,'PUBLIC',7,0,'menu','dashboard','系统管理主页','{\"en-US\": \"Dashboard\", \"ja-JP\": \"ダッシュボード\", \"ko-KR\": \"대시보드\", \"zh-CN\": \"系统管理主页\", \"zh-TW\": \"系統管理主頁\"}','DashboardOutlined','SystemDashboard','sys:dashboard:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000597,1993479636925403140,'PUBLIC',7,3000000000000000666,'menu','excelExportConfig','导出配置','{\"en-US\": \"Export Config\", \"ja-JP\": \"エクスポート設定\", \"ko-KR\": \"내보내기 설정\", \"zh-CN\": \"导出配置\", \"zh-TW\": \"匯出設定\"}','FileExcelOutlined','SystemExcelExportConfig','sys:excel:exportConfig:view',20,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000598,1993479636925403140,'PUBLIC',7,3000000000000000666,'menu','excelImportConfig','导入配置','{\"en-US\": \"Import Config\", \"ja-JP\": \"インポート設定\", \"ko-KR\": \"가져오기 설정\", \"zh-CN\": \"导入配置\", \"zh-TW\": \"匯入設定\"}','FileExcelOutlined','SystemExcelImportConfig','sys:excel:importConfig:view',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000599,1993479636925403140,'PUBLIC',7,3000000000000000597,'button',NULL,'查看导出配置','{\"en-US\": \"View Export Config\", \"ja-JP\": \"エクスポート設定表示\", \"ko-KR\": \"내보내기 설정 보기\", \"zh-CN\": \"查看导出配置\", \"zh-TW\": \"查看匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:list',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000600,1993479636925403140,'PUBLIC',7,3000000000000000597,'button',NULL,'编辑导出配置','{\"en-US\": \"Edit Export Config\", \"ja-JP\": \"エクスポート設定編集\", \"ko-KR\": \"내보내기 설정 편집\", \"zh-CN\": \"编辑导出配置\", \"zh-TW\": \"編輯匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000601,1993479636925403140,'PUBLIC',7,3000000000000000597,'button',NULL,'删除导出配置','{\"en-US\": \"Delete Export Config\", \"ja-JP\": \"エクスポート設定削除\", \"ko-KR\": \"내보내기 설정 삭제\", \"zh-CN\": \"删除导出配置\", \"zh-TW\": \"刪除匯出設定\"}',NULL,NULL,'sys:excel:exportConfig:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000602,1993479636925403140,'PUBLIC',7,3000000000000000598,'button',NULL,'查看导入配置','{\"en-US\": \"View Import Config\", \"ja-JP\": \"インポート設定表示\", \"ko-KR\": \"가져오기 설정 보기\", \"zh-CN\": \"查看导入配置\", \"zh-TW\": \"查看匯入設定\"}',NULL,NULL,'sys:excel:importConfig:list',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000603,1993479636925403140,'PUBLIC',7,3000000000000000598,'button',NULL,'编辑导入配置','{\"en-US\": \"Edit Import Config\", \"ja-JP\": \"インポート設定編集\", \"ko-KR\": \"가져오기 설정 편집\", \"zh-CN\": \"编辑导入配置\", \"zh-TW\": \"編輯匯入設定\"}',NULL,NULL,'sys:excel:importConfig:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000604,1993479636925403140,'PUBLIC',7,3000000000000000598,'button',NULL,'删除导入配置','{\"en-US\": \"Delete Import Config\", \"ja-JP\": \"インポート設定削除\", \"ko-KR\": \"가져오기 설정 삭제\", \"zh-CN\": \"删除导入配置\", \"zh-TW\": \"刪除匯入設定\"}',NULL,NULL,'sys:excel:importConfig:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000605,1993479636925403140,'PUBLIC',7,3000000000000000598,'button',NULL,'下载导入模板','{\"en-US\": \"Download Import Template\", \"ja-JP\": \"インポートテンプレートダウンロード\", \"ko-KR\": \"가져오기 템플릿 다운로드\", \"zh-CN\": \"下载导入模板\", \"zh-TW\": \"下載匯入模板\"}',NULL,NULL,'sys:excel:template:download',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000606,1993479636925403140,'PUBLIC',7,3000000000000000597,'button',NULL,'导出登录日志','{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}',NULL,NULL,'sys:excel:export:loginLog',11,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000607,1993479636925403140,'PUBLIC',7,3000000000000000597,'button',NULL,'导出用户','{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}',NULL,NULL,'sys:excel:export:user',12,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000608,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'分配角色','{\"en-US\": \"Assign Roles\", \"ja-JP\": \"ロール割り当て\", \"ko-KR\": \"역할 할당\", \"zh-CN\": \"分配角色\", \"zh-TW\": \"分配角色\"}',NULL,NULL,'sys:user:assignRole',7,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000609,1993479636925403140,'PUBLIC',7,0,'menu','dict','字典管理','{\"en-US\": \"Dictionary\", \"ja-JP\": \"辞書管理\", \"ko-KR\": \"사전 관리\", \"zh-CN\": \"字典管理\", \"zh-TW\": \"字典管理\"}','BookOutlined','SystemDict','sys:dict:view',7,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000610,1993479636925403140,'PUBLIC',7,3000000000000000609,'button',NULL,'新增字典','{\"en-US\": \"Add Dictionary\", \"ja-JP\": \"辞書追加\", \"ko-KR\": \"사전 추가\", \"zh-CN\": \"新增字典\", \"zh-TW\": \"新增字典\"}',NULL,NULL,'sys:dict:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000611,1993479636925403140,'PUBLIC',7,3000000000000000609,'button',NULL,'编辑字典','{\"en-US\": \"Edit Dictionary\", \"ja-JP\": \"辞書編集\", \"ko-KR\": \"사전 편집\", \"zh-CN\": \"编辑字典\", \"zh-TW\": \"編輯字典\"}',NULL,NULL,'sys:dict:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000612,1993479636925403140,'PUBLIC',7,3000000000000000609,'button',NULL,'删除字典','{\"en-US\": \"Delete Dictionary\", \"ja-JP\": \"辞書削除\", \"ko-KR\": \"사전 삭제\", \"zh-CN\": \"删除字典\", \"zh-TW\": \"刪除字典\"}',NULL,NULL,'sys:dict:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000613,1993479636925403140,'PUBLIC',7,3000000000000000609,'button',NULL,'批量删除字典','{\"en-US\": \"Batch Delete Dictionaries\", \"ja-JP\": \"一括削除辞書\", \"ko-KR\": \"일괄 삭제 사전\", \"zh-CN\": \"批量删除字典\", \"zh-TW\": \"批量刪除字典\"}',NULL,NULL,'sys:dict:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000614,1993479636925403140,'PUBLIC',7,3000000000000000609,'button',NULL,'导出字典','{\"en-US\": \"Export Dictionary\", \"ja-JP\": \"辞書エクスポート\", \"ko-KR\": \"사전 내보내기\", \"zh-CN\": \"导出字典\", \"zh-TW\": \"匯出字典\"}',NULL,NULL,'sys:dict:export',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000615,1993479636925403140,'PUBLIC',7,0,'menu','dictType','字典类型管理','{\"en-US\": \"Dict Types\", \"ja-JP\": \"辞書タイプ\", \"ko-KR\": \"사전 유형\", \"zh-CN\": \"字典类型管理\", \"zh-TW\": \"字典類型管理\"}','TagsOutlined','SystemDictType','sys:dictType:view',100,0,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000616,1993479636925403140,'PUBLIC',7,3000000000000000615,'button',NULL,'新增字典类型','{\"en-US\": \"Add Dict Type\", \"ja-JP\": \"辞書タイプ追加\", \"ko-KR\": \"사전 유형 추가\", \"zh-CN\": \"新增字典类型\", \"zh-TW\": \"新增字典類型\"}',NULL,NULL,'sys:dictType:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000617,1993479636925403140,'PUBLIC',7,3000000000000000615,'button',NULL,'编辑字典类型','{\"en-US\": \"Edit Dict Type\", \"ja-JP\": \"辞書タイプ編集\", \"ko-KR\": \"사전 유형 편집\", \"zh-CN\": \"编辑字典类型\", \"zh-TW\": \"編輯字典類型\"}',NULL,NULL,'sys:dictType:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000618,1993479636925403140,'PUBLIC',7,3000000000000000615,'button',NULL,'删除字典类型','{\"en-US\": \"Delete Dict Type\", \"ja-JP\": \"辞書タイプ削除\", \"ko-KR\": \"사전 유형 삭제\", \"zh-CN\": \"删除字典类型\", \"zh-TW\": \"刪除字典類型\"}',NULL,NULL,'sys:dictType:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000619,1993479636925403140,'PUBLIC',7,3000000000000000615,'button',NULL,'批量删除字典类型','{\"en-US\": \"Batch Delete Dict Types\", \"ja-JP\": \"一括削除辞書タイプ\", \"ko-KR\": \"일괄 삭제 사전 유형\", \"zh-CN\": \"批量删除字典类型\", \"zh-TW\": \"批量刪除字典類型\"}',NULL,NULL,'sys:dictType:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000620,1993479636925403140,'PUBLIC',7,3000000000000000615,'button',NULL,'导出字典类型','{\"en-US\": \"Export Dict Types\", \"ja-JP\": \"辞書タイプエクスポート\", \"ko-KR\": \"사전 유형 내보내기\", \"zh-CN\": \"导出字典类型\", \"zh-TW\": \"匯出字典類型\"}',NULL,NULL,'sys:dictType:export',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000621,1993479636925403140,'PUBLIC',7,3000000000000000667,'menu','tableConfig','表格配置','{\"en-US\": \"Table Config\", \"ja-JP\": \"テーブル設定\", \"ko-KR\": \"테이블 설정\", \"zh-CN\": \"表格配置\", \"zh-TW\": \"表格設定\"}','TableOutlined','SystemTableConfig','sys:tableConfig:view',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000622,1993479636925403140,'PUBLIC',7,3000000000000000621,'button',NULL,'新增表格配置','{\"en-US\": \"Add Table Config\", \"ja-JP\": \"テーブル設定追加\", \"ko-KR\": \"테이블 설정 추가\", \"zh-CN\": \"新增表格配置\", \"zh-TW\": \"新增表格設定\"}',NULL,NULL,'sys:tableConfig:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000623,1993479636925403140,'PUBLIC',7,3000000000000000621,'button',NULL,'编辑表格配置','{\"en-US\": \"Edit Table Config\", \"ja-JP\": \"テーブル設定編集\", \"ko-KR\": \"테이블 설정 편집\", \"zh-CN\": \"编辑表格配置\", \"zh-TW\": \"編輯表格設定\"}',NULL,NULL,'sys:tableConfig:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000624,1993479636925403140,'PUBLIC',7,3000000000000000621,'button',NULL,'删除表格配置','{\"en-US\": \"Delete Table Config\", \"ja-JP\": \"テーブル設定削除\", \"ko-KR\": \"테이블 설정 삭제\", \"zh-CN\": \"删除表格配置\", \"zh-TW\": \"刪除表格設定\"}',NULL,NULL,'sys:tableConfig:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000625,1993479636925403140,'PUBLIC',7,3000000000000000621,'button',NULL,'批量删除表格配置','{\"en-US\": \"Batch Delete Table Configs\", \"ja-JP\": \"一括削除テーブル設定\", \"ko-KR\": \"일괄 삭제 테이블 설정\", \"zh-CN\": \"批量删除表格配置\", \"zh-TW\": \"批量刪除表格設定\"}',NULL,NULL,'sys:tableConfig:batchDelete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000626,1993479636925403140,'PUBLIC',7,3000000000000000621,'button',NULL,'导出表格配置','{\"en-US\": \"Export Table Config\", \"ja-JP\": \"テーブル設定エクスポート\", \"ko-KR\": \"테이블 설정 내보내기\", \"zh-CN\": \"导出表格配置\", \"zh-TW\": \"匯出表格設定\"}',NULL,NULL,'sys:tableConfig:export',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000627,1993479636925403140,'PUBLIC',7,0,'menu','loginLog','登录日志','{\"en-US\": \"Login Logs\", \"ja-JP\": \"ログインログ\", \"ko-KR\": \"로그인 로그\", \"zh-CN\": \"登录日志\", \"zh-TW\": \"登錄日誌\"}','FileTextOutlined','SystemLoginLog','sys:loginLog:view',9,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000628,1993479636925403140,'PUBLIC',7,3000000000000000627,'button',NULL,'删除登录日志','{\"en-US\": \"Delete Login Log\", \"ja-JP\": \"ログインログ削除\", \"ko-KR\": \"로그인 로그 삭제\", \"zh-CN\": \"删除登录日志\", \"zh-TW\": \"刪除登錄日誌\"}',NULL,NULL,'sys:loginLog:delete',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000629,1993479636925403140,'PUBLIC',7,3000000000000000627,'button',NULL,'批量删除登录日志','{\"en-US\": \"Batch Delete Login Logs\", \"ja-JP\": \"一括削除ログインログ\", \"ko-KR\": \"일괄 삭제 로그인 로그\", \"zh-CN\": \"批量删除登录日志\", \"zh-TW\": \"批量刪除登錄日誌\"}',NULL,NULL,'sys:loginLog:batchDelete',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000630,1993479636925403140,'PUBLIC',7,3000000000000000627,'button',NULL,'导出登录日志','{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}',NULL,NULL,'sys:loginLog:export',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000631,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','online','在线用户','{\"en-US\": \"Online Users\", \"ja-JP\": \"オンラインユーザー\", \"ko-KR\": \"온라인 사용자\", \"zh-CN\": \"在线用户\", \"zh-TW\": \"在線用戶\"}','UsergroupAddOutlined','SystemOnline','sys:online:view',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000632,1993479636925403140,'PUBLIC',7,3000000000000000631,'button',NULL,'踢下线','{\"en-US\": \"Kick Offline\", \"ja-JP\": \"オフラインキック\", \"ko-KR\": \"오프라인 강제 종료\", \"zh-CN\": \"踢下线\", \"zh-TW\": \"踢下線\"}',NULL,NULL,'sys:online:kickout',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000633,1993479636925403140,'PUBLIC',7,3000000000000000631,'button',NULL,'批量踢下线','{\"en-US\": \"Batch Kick Offline\", \"ja-JP\": \"一括オフラインキック\", \"ko-KR\": \"일괄 오프라인 강제 종료\", \"zh-CN\": \"批量踢下线\", \"zh-TW\": \"批量踢下線\"}',NULL,NULL,'sys:online:batchKickout',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000634,1993479636925403140,'PUBLIC',7,3000000000000000631,'button',NULL,'导出在线用户','{\"en-US\": \"Export Online Users\", \"ja-JP\": \"オンラインユーザーエクスポート\", \"ko-KR\": \"온라인 사용자 내보내기\", \"zh-CN\": \"导出在线用户\", \"zh-TW\": \"匯出在線用戶\"}',NULL,NULL,'sys:online:export',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000635,1993479636925403140,'PUBLIC',7,0,'catalog','organization','组织架构','{\"en-US\": \"Organization\", \"ja-JP\": \"組織構成\", \"ko-KR\": \"조직 구조\", \"zh-CN\": \"组织架构\", \"zh-TW\": \"組織架構\"}','ApartmentOutlined','SystemOrganization','sys:organization:view',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000636,1993479636925403140,'PUBLIC',7,0,'catalog','authorization','授权管理','{\"en-US\": \"Authorization\", \"ja-JP\": \"認可管理\", \"ko-KR\": \"인증 관리\", \"zh-CN\": \"授权管理\", \"zh-TW\": \"授權管理\"}','SafetyOutlined','SystemAuthorization','sys:authorization:view',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000637,1993479636925403140,'PUBLIC',7,0,'catalog','maintenance','系统运维','{\"en-US\": \"System Maintenance\", \"ja-JP\": \"システム運用\", \"ko-KR\": \"시스템 운영\", \"zh-CN\": \"系统运维\", \"zh-TW\": \"系統運維\"}','ToolOutlined','SystemMaintenance','sys:maintenance:view',11,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000638,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','config','系统配置','{\"en-US\": \"System Config\", \"ja-JP\": \"システム設定\", \"ko-KR\": \"시스템 설정\", \"zh-CN\": \"系统配置\", \"zh-TW\": \"系統設定\"}','SettingOutlined','SystemConfig','sys:config:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000639,1993479636925403140,'PUBLIC',7,3000000000000000638,'button','view','查看配置','{\"en-US\": \"View Config\", \"ja-JP\": \"設定表示\", \"ko-KR\": \"설정 보기\", \"zh-CN\": \"查看配置\", \"zh-TW\": \"查看設定\"}',NULL,NULL,'sys:config:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000640,1993479636925403140,'PUBLIC',7,3000000000000000638,'button','edit','编辑配置','{\"en-US\": \"Edit Config\", \"ja-JP\": \"設定編集\", \"ko-KR\": \"설정 편집\", \"zh-CN\": \"编辑配置\", \"zh-TW\": \"編輯設定\"}',NULL,NULL,'sys:config:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000641,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','operationLog','操作日志','{\"en-US\": \"Operation Log\", \"ja-JP\": \"操作ログ\", \"ko-KR\": \"Operation Log\", \"zh-CN\": \"操作日志\", \"zh-TW\": \"操作日誌\"}','FileTextOutlined','SystemOperationLog','sys:operation-log:view',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000642,1993479636925403140,'PUBLIC',7,3000000000000000641,'button',NULL,'查询','{\"en-US\": \"Query\", \"ja-JP\": \"クエリ\", \"ko-KR\": \"Query\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'sys:operation-log:query',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000643,1993479636925403140,'PUBLIC',7,3000000000000000641,'button',NULL,'导出','{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"Export\", \"zh-CN\": \"导出\", \"zh-TW\": \"匯出\"}',NULL,NULL,'sys:operation-log:export',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000644,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','messageTemplate','消息模板','{\"en-US\": \"Message Template\", \"ja-JP\": \"メッセージテンプレート\", \"ko-KR\": \"Message Template\", \"zh-CN\": \"消息模板\", \"zh-TW\": \"消息模板\"}','MailOutlined','SystemMessageTemplate','sys:message-template:view',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000645,1993479636925403140,'PUBLIC',7,3000000000000000667,'menu','userTableConfig','用户列设置','{\"en-US\": \"User Table Config\", \"ja-JP\": \"ユーザー列設定\", \"ko-KR\": \"사용자 열 설정\", \"zh-CN\": \"用户列设置\", \"zh-TW\": \"用戶列設置\"}','ColumnWidthOutlined','SystemUserTableConfig','sys:userTableConfig:view',20,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000646,1993479636925403140,'PUBLIC',7,3000000000000000565,'button',NULL,'绑定人员','{\"en-US\": \"sys:role:authUser\", \"ja-JP\": \"绑定人员\", \"ko-KR\": \"sys:role:authUser\", \"zh-CN\": \"绑定人员\", \"zh-TW\": \"绑定人员\"}',NULL,NULL,'sys:role:authUser',6,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000647,1993479636925403140,'PUBLIC',7,3000000000000000636,'menu','menu-grant/:roleId','菜单授权','{\"en-US\": \"menu-grant/:roleId\", \"ja-JP\": \"菜单授权\", \"ko-KR\": \"menu-grant/:roleId\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"菜单授权\"}','SafetyCertificateOutlined','SystemRoleMenuGrant','sys:role:authMenu',1,0,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000648,1993479636925403140,'PUBLIC',7,3000000000000000636,'menu','user-grant/:roleId','人员授权','{\"en-US\": \"user-grant/:roleId\", \"ja-JP\": \"人员授权\", \"ko-KR\": \"user-grant/:roleId\", \"zh-CN\": \"人员授权\", \"zh-TW\": \"人员授权\"}','UsergroupAddOutlined','SystemRoleUserGrant','sys:role:authUser',2,0,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000649,1993479636925403140,'PUBLIC',7,3000000000000000656,'menu','i18nLanguageType','语言配置','{\"en-US\": \"Language Configuration\", \"ja-JP\": \"言語設定\", \"ko-KR\": \"언어 설정\", \"zh-CN\": \"语言配置\", \"zh-TW\": \"語言設定\"}','GlobalOutlined','SystemI18nLanguageType','sys:i18nLanguageType:view',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000650,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'sys:i18nLanguageType:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000651,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'sys:i18nLanguageType:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000652,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:i18nLanguageType:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000653,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'设为默认','{\"en-US\": \"Set Default\", \"ja-JP\": \"既定に設定\", \"ko-KR\": \"기본값으로 설정\", \"zh-CN\": \"设为默认\", \"zh-TW\": \"設為預設\"}',NULL,NULL,'sys:i18nLanguageType:setDefault',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000654,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'导入','{\"en-US\": \"Import\", \"ja-JP\": \"インポート\", \"ko-KR\": \"가져오기\", \"zh-CN\": \"导入\", \"zh-TW\": \"導入\"}',NULL,NULL,'sys:i18nLanguageType:import',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000655,1993479636925403140,'PUBLIC',7,3000000000000000649,'button',NULL,'下载模板','{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートをダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}',NULL,NULL,'sys:i18nLanguageType:template:download',6,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000656,1993479636925403140,'PUBLIC',7,0,'catalog','i18nConfig','多语言配置','{\"en-US\": \"I18n Config\", \"ja-JP\": \"多言語設定\", \"ko-KR\": \"다국어 설정\", \"zh-CN\": \"多语言配置\", \"zh-TW\": \"多語言設定\"}','TranslationOutlined',NULL,NULL,85,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000657,1993479636925403140,'PUBLIC',7,3000000000000000656,'menu','i18nMessage','多语言消息','{\"en-US\": \"I18n Message\", \"ja-JP\": \"多言語メッセージ\", \"ko-KR\": \"다국어 메시지\", \"zh-CN\": \"多语言消息\", \"zh-TW\": \"多語言訊息\"}','MessageOutlined','SystemI18nMessage','sys:i18nMessage:view',20,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000658,1993479636925403140,'PUBLIC',7,3000000000000000657,'button',NULL,'新增多语言消息','{\"en-US\": \"sys:i18nMessage:add\", \"ja-JP\": \"新增多语言消息\", \"ko-KR\": \"sys:i18nMessage:add\", \"zh-CN\": \"新增多语言消息\", \"zh-TW\": \"新增多语言消息\"}',NULL,NULL,'sys:i18nMessage:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000659,1993479636925403140,'PUBLIC',7,3000000000000000657,'button',NULL,'编辑多语言消息','{\"en-US\": \"sys:i18nMessage:edit\", \"ja-JP\": \"编辑多语言消息\", \"ko-KR\": \"sys:i18nMessage:edit\", \"zh-CN\": \"编辑多语言消息\", \"zh-TW\": \"编辑多语言消息\"}',NULL,NULL,'sys:i18nMessage:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000660,1993479636925403140,'PUBLIC',7,3000000000000000657,'button',NULL,'删除多语言消息','{\"en-US\": \"sys:i18nMessage:delete\", \"ja-JP\": \"删除多语言消息\", \"ko-KR\": \"sys:i18nMessage:delete\", \"zh-CN\": \"删除多语言消息\", \"zh-TW\": \"删除多语言消息\"}',NULL,NULL,'sys:i18nMessage:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000661,1993479636925403140,'PUBLIC',7,3000000000000000635,'menu','inviteCode','邀请码管理','{\"en-US\": \"Invite Code Management\", \"ja-JP\": \"招待コード管理\", \"ko-KR\": \"초대 코드 관리\", \"zh-CN\": \"邀请码管理\", \"zh-TW\": \"邀請碼管理\"}','KeyOutlined','SystemInviteCode','sys:invite-code:view',15,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000662,1993479636925403140,'PUBLIC',7,3000000000000000661,'button','','新增邀请码','{\"en-US\": \"sys:invite-code:add\", \"ja-JP\": \"新增邀请码\", \"ko-KR\": \"sys:invite-code:add\", \"zh-CN\": \"新增邀请码\", \"zh-TW\": \"新增邀请码\"}',NULL,NULL,'sys:invite-code:add',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000663,1993479636925403140,'PUBLIC',7,3000000000000000661,'button','','停用邀请码','{\"en-US\": \"sys:invite-code:edit\", \"ja-JP\": \"停用邀请码\", \"ko-KR\": \"sys:invite-code:edit\", \"zh-CN\": \"停用邀请码\", \"zh-TW\": \"停用邀请码\"}',NULL,NULL,'sys:invite-code:edit',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000664,1993479636925403140,'PUBLIC',7,3000000000000000661,'button','','删除邀请码','{\"en-US\": \"sys:invite-code:delete\", \"ja-JP\": \"删除邀请码\", \"ko-KR\": \"sys:invite-code:delete\", \"zh-CN\": \"删除邀请码\", \"zh-TW\": \"删除邀请码\"}',NULL,NULL,'sys:invite-code:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000665,1993479636925403140,'PUBLIC',7,3000000000000000661,'button','','查看使用记录','{\"en-US\": \"sys:invite-code:record:view\", \"ja-JP\": \"查看使用记录\", \"ko-KR\": \"sys:invite-code:record:view\", \"zh-CN\": \"查看使用记录\", \"zh-TW\": \"查看使用记录\"}',NULL,NULL,'sys:invite-code:record:view',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000666,1993479636925403140,'PUBLIC',7,0,'catalog','excelConfig','Excel配置','{\"en-US\": \"Excel Config\", \"ja-JP\": \"Excel設定\", \"ko-KR\": \"Excel 설정\", \"zh-CN\": \"Excel配置\", \"zh-TW\": \"Excel配置\"}','FileExcelOutlined',NULL,NULL,5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000667,1993479636925403140,'PUBLIC',7,0,'catalog','pageTableConfig','页表配置','{\"en-US\": \"Page Table Config\", \"ja-JP\": \"ページテーブル設定\", \"ko-KR\": \"페이지 테이블 설정\", \"zh-CN\": \"页表配置\", \"zh-TW\": \"頁表配置\"}','TableOutlined',NULL,NULL,8,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000668,1993479636925403140,'PUBLIC',7,0,'menu','file','文件管理','{\"en-US\": \"File Management\", \"ja-JP\": \"ファイル管理\", \"ko-KR\": \"파일 관리\", \"zh-CN\": \"文件管理\", \"zh-TW\": \"檔案管理\"}','FolderOpenOutlined','SystemFile','sys:file:view',95,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,'embedded',NULL),(3000000000000000669,1993479636925403140,'PUBLIC',7,3000000000000000668,'button',NULL,'文件上传','{\"en-US\": \"sys:file:upload\", \"ja-JP\": \"文件上传\", \"ko-KR\": \"sys:file:upload\", \"zh-CN\": \"文件上传\", \"zh-TW\": \"文件上传\"}',NULL,NULL,'sys:file:upload',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000670,1993479636925403140,'PUBLIC',7,3000000000000000679,'menu','codegen','代码生成','{\"en-US\": \"Code Generation\", \"ja-JP\": \"コード生成\", \"ko-KR\": \"코드 생성\", \"zh-CN\": \"代码生成\", \"zh-TW\": \"程式碼生成\"}','CodeOutlined','SystemCodegen','sys:codegen:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000671,1993479636925403140,'PUBLIC',7,3000000000000000679,'menu','codegenDatasource','代码生成数据源','{\"en-US\": \"Codegen Datasource\", \"ja-JP\": \"コード生成データソース\", \"ko-KR\": \"코드 생성 데이터소스\", \"zh-CN\": \"代码生成数据源\", \"zh-TW\": \"程式碼生成資料來源\"}','DatabaseOutlined','SystemCodegenDatasource','sys:codegenDatasource:view',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000672,1993479636925403140,'PUBLIC',7,3000000000000000670,'button',NULL,'预览','{\"en-US\": \"sys:codegen:preview\", \"ja-JP\": \"预览\", \"ko-KR\": \"sys:codegen:preview\", \"zh-CN\": \"预览\", \"zh-TW\": \"预览\"}',NULL,NULL,'sys:codegen:preview',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000673,1993479636925403140,'PUBLIC',7,3000000000000000670,'button',NULL,'下载ZIP','{\"en-US\": \"sys:codegen:download\", \"ja-JP\": \"下载ZIP\", \"ko-KR\": \"sys:codegen:download\", \"zh-CN\": \"下载ZIP\", \"zh-TW\": \"下载ZIP\"}',NULL,NULL,'sys:codegen:download',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000674,1993479636925403140,'PUBLIC',7,3000000000000000671,'button',NULL,'保存','{\"en-US\": \"sys:codegenDatasource:save\", \"ja-JP\": \"保存\", \"ko-KR\": \"sys:codegenDatasource:save\", \"zh-CN\": \"保存\", \"zh-TW\": \"保存\"}',NULL,NULL,'sys:codegenDatasource:save',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000675,1993479636925403140,'PUBLIC',7,3000000000000000671,'button',NULL,'删除','{\"en-US\": \"sys:codegenDatasource:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegenDatasource:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'sys:codegenDatasource:delete',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000676,1993479636925403140,'PUBLIC',7,3000000000000000671,'button',NULL,'测试连接','{\"en-US\": \"sys:codegenDatasource:test\", \"ja-JP\": \"测试连接\", \"ko-KR\": \"sys:codegenDatasource:test\", \"zh-CN\": \"测试连接\", \"zh-TW\": \"测试连接\"}',NULL,NULL,'sys:codegenDatasource:test',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000677,1993479636925403140,'PUBLIC',7,3000000000000000670,'button',NULL,'保存配置','{\"en-US\": \"sys:codegen:save\", \"ja-JP\": \"保存配置\", \"ko-KR\": \"sys:codegen:save\", \"zh-CN\": \"保存配置\", \"zh-TW\": \"保存配置\"}',NULL,NULL,'sys:codegen:save',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000678,1993479636925403140,'PUBLIC',7,3000000000000000670,'button',NULL,'删除','{\"en-US\": \"sys:codegen:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegen:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}',NULL,NULL,'sys:codegen:delete',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,NULL,NULL),(3000000000000000679,1993479636925403140,'PUBLIC',7,0,'catalog','onlineDev','在线开发','{\"en-US\": \"onlineDev\", \"ja-JP\": \"在线开发\", \"ko-KR\": \"onlineDev\", \"zh-CN\": \"在线开发\", \"zh-TW\": \"在线开发\"}','CodeOutlined',NULL,NULL,95,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,1,NULL,NULL),(3000000000000000680,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'同步第三方','{\"en-US\": \"Sync Third Party\", \"ja-JP\": \"第三方同期\", \"ko-KR\": \"제3자 동기화\", \"zh-CN\": \"同步第三方\", \"zh-TW\": \"同步第三方\"}',NULL,NULL,'sys:user:syncThirdParty',8,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000681,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'从第三方拉取','{\"en-US\": \"Pull From Third Party\", \"ja-JP\": \"第三方から取得\", \"ko-KR\": \"제3자에서 가져오기\", \"zh-CN\": \"从第三方拉取\", \"zh-TW\": \"從第三方拉取\"}',NULL,NULL,'sys:user:pullThirdParty',9,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000682,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'导入用户','{\"en-US\": \"Import Users\", \"ja-JP\": \"ユーザーインポート\", \"ko-KR\": \"사용자 가져오기\", \"zh-CN\": \"导入用户\", \"zh-TW\": \"導入用戶\"}',NULL,NULL,'sys:user:import',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000683,1993479636925403140,'PUBLIC',7,3000000000000000564,'button',NULL,'下载模板','{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}',NULL,NULL,'sys:user:downloadTemplate',11,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000684,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','androidVersion','安卓版本管理','{\"en-US\": \"Android Version\", \"ja-JP\": \"Androidバージョン管理\", \"ko-KR\": \"안드로이드 버전 관리\", \"zh-CN\": \"安卓版本管理\", \"zh-TW\": \"安卓版本管理\"}','AndroidOutlined','SystemAndroidVersion','sys:androidVersion:view',6,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000685,1993479636925403140,'PUBLIC',7,3000000000000000684,'button','view','查询','{\"en-US\": \"Query\", \"ja-JP\": \"照会\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}',NULL,NULL,'sys:androidVersion:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000686,1993479636925403140,'PUBLIC',7,3000000000000000684,'button','add','上传版本','{\"en-US\": \"Upload Version\", \"ja-JP\": \"バージョンアップロード\", \"ko-KR\": \"버전 업로드\", \"zh-CN\": \"上传版本\", \"zh-TW\": \"上傳版本\"}',NULL,NULL,'sys:androidVersion:add',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000687,1993479636925403140,'PUBLIC',7,3000000000000000684,'button','edit','编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'sys:androidVersion:edit',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000688,1993479636925403140,'PUBLIC',7,3000000000000000684,'button','delete','删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:androidVersion:delete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000689,1993479636925403140,'PUBLIC',7,3000000000000000637,'menu','notice','系统通知','{\"en-US\": \"System Notice\", \"ja-JP\": \"システム通知\", \"ko-KR\": \"시스템 알림\", \"zh-CN\": \"系统通知\", \"zh-TW\": \"系統通知\"}','NotificationOutlined','SystemNotice','sys:notice:view',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,2,'embedded',NULL),(3000000000000000690,1993479636925403140,'PUBLIC',7,3000000000000000689,'button',NULL,'查看系统通知','{\"en-US\": \"View System Notice\", \"ja-JP\": \"システム通知表示\", \"ko-KR\": \"시스템 알림 보기\", \"zh-CN\": \"查看系统通知\", \"zh-TW\": \"查看系統通知\"}',NULL,NULL,'sys:notice:view',1,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000691,1993479636925403140,'PUBLIC',7,3000000000000000689,'button',NULL,'新增系统通知','{\"en-US\": \"Add System Notice\", \"ja-JP\": \"システム通知追加\", \"ko-KR\": \"시스템 알림 추가\", \"zh-CN\": \"新增系统通知\", \"zh-TW\": \"新增系統通知\"}',NULL,NULL,'sys:notice:add',2,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000692,1993479636925403140,'PUBLIC',7,3000000000000000689,'button',NULL,'编辑系统通知','{\"en-US\": \"Edit System Notice\", \"ja-JP\": \"システム通知編集\", \"ko-KR\": \"시스템 알림 편집\", \"zh-CN\": \"编辑系统通知\", \"zh-TW\": \"編輯系統通知\"}',NULL,NULL,'sys:notice:edit',3,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000693,1993479636925403140,'PUBLIC',7,3000000000000000689,'button',NULL,'删除系统通知','{\"en-US\": \"Delete System Notice\", \"ja-JP\": \"システム通知削除\", \"ko-KR\": \"시스템 알림 삭제\", \"zh-CN\": \"删除系统通知\", \"zh-TW\": \"刪除系統通知\"}',NULL,NULL,'sys:notice:delete',4,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000694,1993479636925403140,'PUBLIC',7,3000000000000000689,'button',NULL,'发布/停用系统通知','{\"en-US\": \"Publish or Disable System Notice\", \"ja-JP\": \"システム通知公開/無効化\", \"ko-KR\": \"시스템 알림 게시/중지\", \"zh-CN\": \"发布/停用系统通知\", \"zh-TW\": \"發布/停用系統通知\"}',NULL,NULL,'sys:notice:publish',5,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:18:18','20260512_supplier_tenant_login_fix',0,3,'embedded',NULL),(3000000000000000695,1993479636925403138,'PUBLIC',1,0,'catalog','job','定时任务','{\"en-US\": \"Scheduled Jobs\", \"ja-JP\": \"定時ジョブ\", \"ko-KR\": \"예약 작업\", \"zh-CN\": \"定时任务\", \"zh-TW\": \"定時任務\"}','ClockCircleOutlined',NULL,'job:dashboard:view',90,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,1,'embedded',NULL),(3000000000000000696,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','dashboard','任务大盘','{\"en-US\": \"Job Dashboard\", \"ja-JP\": \"ジョブダッシュボード\", \"ko-KR\": \"작업 대시보드\", \"zh-CN\": \"任务大盘\", \"zh-TW\": \"任務大盤\"}','DashboardOutlined','JobDashboard','job:dashboard:view',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000697,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','task','任务管理','{\"en-US\": \"Job Tasks\", \"ja-JP\": \"ジョブ管理\", \"ko-KR\": \"작업 관리\", \"zh-CN\": \"任务管理\", \"zh-TW\": \"任務管理\"}','ScheduleOutlined','JobTask','job:task:list',2,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000698,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','log','执行日志','{\"en-US\": \"Execution Logs\", \"ja-JP\": \"実行ログ\", \"ko-KR\": \"실행 로그\", \"zh-CN\": \"执行日志\", \"zh-TW\": \"執行日誌\"}','FileTextOutlined','JobLog','job:log:list',3,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000699,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','instance','执行器实例','{\"en-US\": \"Executor Instances\", \"ja-JP\": \"実行インスタンス\", \"ko-KR\": \"실행기 인스턴스\", \"zh-CN\": \"执行器实例\", \"zh-TW\": \"執行器實例\"}','ClusterOutlined','JobInstance','job:instance:list',4,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000700,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','retry','重试/死信','{\"en-US\": \"Retry / Dead Letter\", \"ja-JP\": \"リトライ/デッドレター\", \"ko-KR\": \"재시도/데드레터\", \"zh-CN\": \"重试/死信\", \"zh-TW\": \"重試/死信\"}','ReloadOutlined','JobRetry','job:retry:list',5,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000701,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','alarm','告警规则','{\"en-US\": \"Alarm Rules\", \"ja-JP\": \"アラームルール\", \"ko-KR\": \"알람 규칙\", \"zh-CN\": \"告警规则\", \"zh-TW\": \"告警規則\"}','BellOutlined','JobAlarm','job:alarm:list',6,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000702,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','alarm-log','告警日志','{\"en-US\": \"Alarm Logs\", \"ja-JP\": \"アラームログ\", \"ko-KR\": \"알람 로그\", \"zh-CN\": \"告警日志\", \"zh-TW\": \"告警日誌\"}','AlertOutlined','JobAlarmLog','job:alarm:list',7,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000703,1993479636925403138,'PUBLIC',1,3000000000000000695,'menu','workflow','DAG 编排','{\"en-US\": \"DAG Workflow\", \"ja-JP\": \"DAG 編成\", \"ko-KR\": \"DAG 편성\", \"zh-CN\": \"DAG 编排\", \"zh-TW\": \"DAG 編排\"}','BranchesOutlined','JobWorkflow','job:workflow:list',8,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,2,'embedded',NULL),(3000000000000000711,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','add','新增','{\"en-US\": \"Add\", \"zh-CN\": \"新增\"}',NULL,NULL,'job:task:add',2,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000712,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','change-status','启停','{\"en-US\": \"Change Status\", \"zh-CN\": \"启停\"}',NULL,NULL,'job:task:changeStatus',5,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000713,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','delete','删除','{\"en-US\": \"Delete\", \"zh-CN\": \"删除\"}',NULL,NULL,'job:task:delete',4,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000714,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','edit','编辑','{\"en-US\": \"Edit\", \"zh-CN\": \"编辑\"}',NULL,NULL,'job:task:edit',3,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000715,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','trigger','触发','{\"en-US\": \"Trigger\", \"zh-CN\": \"触发\"}',NULL,NULL,'job:task:trigger',6,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000716,1993479636925403138,'PUBLIC',1,3000000000000000697,'button','view','查看','{\"en-US\": \"View\", \"zh-CN\": \"查看\"}',NULL,NULL,'job:task:view',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000717,1993479636925403138,'PUBLIC',1,3000000000000000698,'button','view','查看','{\"en-US\": \"View\", \"zh-CN\": \"查看\"}',NULL,NULL,'job:log:view',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000718,1993479636925403138,'PUBLIC',1,3000000000000000699,'button','maintenance','维护模式','{\"en-US\": \"Maintenance\", \"zh-CN\": \"维护模式\"}',NULL,NULL,'job:instance:maintenance',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000719,1993479636925403138,'PUBLIC',1,3000000000000000700,'button','handle','处理','{\"en-US\": \"Handle\", \"zh-CN\": \"处理\"}',NULL,NULL,'job:retry:handle',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000720,1993479636925403138,'PUBLIC',1,3000000000000000701,'button','add','新增','{\"en-US\": \"Add\", \"zh-CN\": \"新增\"}',NULL,NULL,'job:alarm:add',2,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000721,1993479636925403138,'PUBLIC',1,3000000000000000701,'button','delete','删除','{\"en-US\": \"Delete\", \"zh-CN\": \"删除\"}',NULL,NULL,'job:alarm:delete',4,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000722,1993479636925403138,'PUBLIC',1,3000000000000000701,'button','edit','编辑','{\"en-US\": \"Edit\", \"zh-CN\": \"编辑\"}',NULL,NULL,'job:alarm:edit',3,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000723,1993479636925403138,'PUBLIC',1,3000000000000000701,'button','view','查看','{\"en-US\": \"View\", \"zh-CN\": \"查看\"}',NULL,NULL,'job:alarm:view',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000724,1993479636925403138,'PUBLIC',1,3000000000000000703,'button','add','新增','{\"en-US\": \"Add\", \"zh-CN\": \"新增\"}',NULL,NULL,'job:workflow:add',2,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000725,1993479636925403138,'PUBLIC',1,3000000000000000703,'button','edit','编辑','{\"en-US\": \"Edit\", \"zh-CN\": \"编辑\"}',NULL,NULL,'job:workflow:edit',3,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000726,1993479636925403138,'PUBLIC',1,3000000000000000703,'button','execute','执行','{\"en-US\": \"Execute\", \"zh-CN\": \"执行\"}',NULL,NULL,'job:workflow:execute',5,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000727,1993479636925403138,'PUBLIC',1,3000000000000000703,'button','publish','发布','{\"en-US\": \"Publish\", \"zh-CN\": \"发布\"}',NULL,NULL,'job:workflow:publish',4,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000728,1993479636925403138,'PUBLIC',1,3000000000000000703,'button','view','查看','{\"en-US\": \"View\", \"zh-CN\": \"查看\"}',NULL,NULL,'job:workflow:view',1,1,1,'2026-05-13 11:38:57','20260513_init_forgex_job','2026-05-13 19:09:41','20260513_init_forgex_job',0,3,'embedded',NULL),(3000000000000000729,1993479636925403138,'PUBLIC',1,3000000000000000443,'menu','homepage-component','首页组件目录','{\"en-US\": \"Homepage Components\", \"ja-JP\": \"ホームページコンポーネント\", \"ko-KR\": \"홈페이지 컴포넌트\", \"zh-CN\": \"首页组件目录\", \"zh-TW\": \"首頁組件目錄\"}','AppstoreOutlined','SystemHomepageComponent','sys:homepageComponent:view',3,1,1,'2026-05-15 18:39:01','codex','2026-05-15 21:04:23','codex',0,2,'embedded',NULL),(3000000000000000730,1993479636925403138,'PUBLIC',1,3000000000000000729,'button','view','查看','{\"en-US\": \"View\", \"ja-JP\": \"表示\", \"ko-KR\": \"조회\", \"zh-CN\": \"查看\", \"zh-TW\": \"查看\"}',NULL,NULL,'sys:homepageComponent:view',1,1,1,'2026-05-15 18:39:01','codex','2026-05-15 18:39:01','codex',0,3,'embedded',NULL),(3000000000000000731,1993479636925403138,'PUBLIC',1,3000000000000000729,'button','add','新增','{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}',NULL,NULL,'sys:homepageComponent:add',2,1,1,'2026-05-15 18:39:01','codex','2026-05-15 18:39:01','codex',0,3,'embedded',NULL),(3000000000000000732,1993479636925403138,'PUBLIC',1,3000000000000000729,'button','edit','编辑','{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}',NULL,NULL,'sys:homepageComponent:edit',3,1,1,'2026-05-15 18:39:01','codex','2026-05-15 18:39:01','codex',0,3,'embedded',NULL),(3000000000000000733,1993479636925403138,'PUBLIC',1,3000000000000000729,'button','delete','删除','{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}',NULL,NULL,'sys:homepageComponent:delete',4,1,1,'2026-05-15 18:39:01','codex','2026-05-15 18:39:01','codex',0,3,'embedded',NULL),(3000000000000000734,1993479636925403138,'PUBLIC',1,3000000000000000729,'button','pull-public','拉取公共配置','{\"en-US\": \"Pull Public Config\", \"ja-JP\": \"公開設定を取得\", \"ko-KR\": \"공용 설정 가져오기\", \"zh-CN\": \"拉取公共配置\", \"zh-TW\": \"拉取公共配置\"}',NULL,NULL,'sys:homepageComponent:pullPublic',5,1,1,'2026-05-15 18:39:01','codex','2026-05-15 18:39:01','codex',0,3,'embedded',NULL);
/*!40000 ALTER TABLE `sys_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message`
--

DROP TABLE IF EXISTS `sys_message`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` bigint NOT NULL COMMENT '接收方租户ID',
  `sender_tenant_id` bigint NOT NULL COMMENT '发送方租户ID',
  `sender_user_id` bigint NOT NULL COMMENT '发送方用户ID',
  `sender_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送人名称(格式:姓名+账号,如张三(admin),系统消息则为系统(admin))',
  `receiver_user_id` bigint NOT NULL COMMENT '接收方用户ID',
  `scope` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息范围(INTERNAL/EXTERNAL)',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模板编号',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型(NOTICE=通知,WARNING=警告,ALARM=报警)',
  `category` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息分类：SYSTEM=系统通知，MESSAGE=消息通知',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INTERNAL' COMMENT '消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '内容',
  `link_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '跳转链接',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务类型',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态(0=未读,1=已读)',
  `read_time` datetime DEFAULT NULL COMMENT '已读时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_user_status` (`tenant_id`,`receiver_user_id`,`status`) USING BTREE,
  KEY `idx_sender` (`sender_tenant_id`,`sender_user_id`) USING BTREE,
  KEY `idx_template` (`template_code`) USING BTREE,
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message`
--

LOCK TABLES `sys_message` WRITE;
/*!40000 ALTER TABLE `sys_message` DISABLE KEYS */;
INSERT INTO `sys_message` VALUES (1,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','测试','测试','',NULL,0,NULL,'2026-04-05 16:58:14','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(2,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-05 17:22:02','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(3,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'NOTICE','SYSTEM','INTERNAL','【审批待办】请假审批','发起人：admin\n当前节点：管理员审批\n发起时间：2026-04-07 11:34:45','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-07 11:34:51','2026-04-07 11:34:45','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(4,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'WARNING','SYSTEM','INTERNAL','【审批驳回】请假审批','审批名称：请假审批\n审批节点：管理员审批\n处理人：admin\n处理结果：驳回结束\n驳回原因：1','/workspace/approval/my/initiated','WF_REJECTED',1,'2026-04-07 11:35:23','2026-04-07 11:35:13','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(5,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-07 11:45:27','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(6,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'NOTICE','SYSTEM','INTERNAL','【审批待办】请假审批','发起人：admin\n当前节点：管理员审批\n发起时间：2026-04-09 10:13:33','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-09 10:14:00','2026-04-09 10:13:34','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(7,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'NOTICE','SYSTEM','INTERNAL','【审批完成】请假审批','审批名称：请假审批\n发起人：admin\n完成时间：2026-04-09 10:14:08\n处理结果：审批完成','/workspace/approval/my/initiated','WF_FINISHED',1,'2026-04-09 10:33:48','2026-04-09 10:14:09','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(8,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-10 19:21:16','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(9,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-12 11:19:03','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(10,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-13 15:11:01','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(11,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-13 15:15:25','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(12,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-13 15:15:51','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(13,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-13 15:16:43','2026-04-14 13:59:46',0,'1993479637244170242','1993479637244170242'),(14,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','x','x','',NULL,0,NULL,'2026-04-14 14:00:12','2026-04-14 14:00:12',0,'1993479637244170242','1993479637244170242'),(15,1993479636925403138,1993479636925403138,1993479637244170242,'admin',1993479637244170200,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','cs','cs','',NULL,0,NULL,'2026-04-14 14:23:17','2026-04-14 14:23:17',0,'1993479637244170242','1993479637244170242'),(16,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批完成】xa','审批名称：xa\n发起人：admin\n完成时间：2026-04-17 16:58:31\n处理结果：审批完成','/workspace/approval/my/initiated','WF_FINISHED',1,'2026-04-17 16:58:42','2026-04-17 16:58:32','2026-04-17 16:58:42',0,'1993479637244170242','1993479637244170242'),(17,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170242,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批待办】xa','发起人：admin\n当前节点：审批节点\n发起时间：2026-04-22 19:50:41','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-22 19:50:47','2026-04-22 19:50:41','2026-04-22 19:50:46',0,'1993479637244170242','1993479637244170242'),(18,1993479636925403138,1993479636925403138,1993479637244170242,'用户(1993479637244170242)',1993479637244170253,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批待办】xa','发起人：admin\n当前节点：审批节点\n发起时间：2026-04-22 19:50:41','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-22 21:55:46','2026-04-22 19:50:41','2026-04-22 21:55:46',0,'1993479637244170242','1993479637244170242'),(19,1993479636925403138,1993479636925403138,1993479637244170253,'用户(1993479637244170253)',1993479637244170242,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批完成】xa','审批名称：xa\n发起人：admin\n完成时间：2026-04-22 22:23:36\n处理结果：审批完成','/workspace/approval/my/initiated','WF_FINISHED',1,'2026-04-22 23:45:37','2026-04-22 22:23:37','2026-04-22 23:45:36',0,'1993479637244170253','1993479637244170253'),(20,1993479636925403138,1993479636925403138,1993479637244170253,'用户(1993479637244170253)',1993479637244170242,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批待办】xa','发起人：孙明岩\n当前节点：审批节点\n发起时间：2026-04-22 23:29:25','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-22 23:45:40','2026-04-22 23:29:26','2026-04-22 23:45:40',0,'1993479637244170253','1993479637244170253'),(21,1993479636925403138,1993479636925403138,1993479637244170253,'用户(1993479637244170253)',1993479637244170253,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批待办】xa','发起人：孙明岩\n当前节点：审批节点\n发起时间：2026-04-22 23:29:25','/workspace/approval/my/pending','WF_PENDING',1,'2026-04-22 23:29:29','2026-04-22 23:29:26','2026-04-22 23:29:29',0,'1993479637244170253','1993479637244170253'),(22,1993479636925403138,1993479636925403138,1993479637244170253,'用户(1993479637244170253)',1993479637244170253,'INTERNAL',NULL,'NOTICE','MESSAGE','INTERNAL','【审批完成】xa','审批名称：xa\n发起人：孙明岩\n完成时间：2026-04-22 23:29:56\n处理结果：审批完成','/workspace/approval/my/initiated','WF_FINISHED',1,'2026-04-22 23:29:59','2026-04-22 23:29:57','2026-04-22 23:29:58',0,'1993479637244170253','1993479637244170253');
/*!40000 ALTER TABLE `sys_message` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message_template`
--

DROP TABLE IF EXISTS `sys_message_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '模板名称多语言JSON',
  `template_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT '1.0' COMMENT '模板版本',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NOTICE' COMMENT '消息类型(NOTICE=通知,WARNING=警告,ALARM=报警)',
  `biz_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '业务类型',
  `notification_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'info' COMMENT '通知图标类型(error=错误,info=普通,warning=警告,success=成功,custom=自定义)',
  `config_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TENANT' COMMENT '配置级别(PUBLIC=公共级,TENANT=租户级,TENANT_TYPE=租户类型级)',
  `tenant_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '适用租户类型(MAIN_TENANT=主租户,SUB_TENANT=子租户,PUBLIC=所有类型)',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模板分类(APPROVAL=审批模板,SYSTEM=系统模板,WELCOME=欢迎模板,SUMMARY=汇总模板)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0=禁用,1=启用)',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_tenant_code` (`tenant_id`,`template_code`) USING BTREE,
  KEY `idx_tenant_type` (`tenant_id`) USING BTREE,
  KEY `idx_status` (`status`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=18 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息模板主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template`
--

LOCK TABLES `sys_message_template` WRITE;
/*!40000 ALTER TABLE `sys_message_template` DISABLE KEYS */;
INSERT INTO `sys_message_template` VALUES (1,1993479636925403138,'xxx','',NULL,'1.0','NOTICE',NULL,'info','TENANT',NULL,NULL,1,'','2026-03-31 21:03:10','2026-04-09 15:57:40',1,'1993479637244170242','1993479637244170242'),(2,0,'WF_PENDING','审批待办通知','{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\",\"zh-TW\":\"審批待辦通知\",\"ja-JP\":\"承認保留通知\",\"ko-KR\":\"승인 대기 알림\"}','1.0.0','WARNING',NULL,'warning','TENANT','PUBLIC','APPROVAL',1,'当审批流程流转到某个节点时，通知待审批人有新的待办任务','2026-04-06 19:49:46','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(3,0,'WF_APPROVED','审批通过通知','{\"zh-CN\":\"审批通过通知\",\"en-US\":\"Approval Passed Notification\",\"zh-TW\":\"審批通過通知\",\"ja-JP\":\"承認済み通知\",\"ko-KR\":\"승인 완료 알림\"}','1.0.0','NOTICE',NULL,'success','TENANT','PUBLIC','APPROVAL',1,'当审批节点被通过时，通知发起人审批进度','2026-04-06 19:51:36','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(4,0,'WF_REJECTED','审批驳回通知','{\"zh-CN\":\"审批驳回通知\",\"en-US\":\"Approval Rejected Notification\",\"zh-TW\":\"審批駁回通知\",\"ja-JP\":\"却下通知\",\"ko-KR\":\"승인 거부 알림\"}','1.0.0','ALARM',NULL,'error','TENANT','PUBLIC','APPROVAL',1,'当审批被驳回时，通知发起人驳回原因','2026-04-06 19:51:37','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(5,0,'WF_FINISHED','审批完成通知','{\"zh-CN\":\"审批完成通知\",\"en-US\":\"Approval Completed Notification\",\"zh-TW\":\"審批完成通知\",\"ja-JP\":\"承認完了通知\",\"ko-KR\":\"승인 완료 알림\"}','1.0.0','NOTICE',NULL,'info','TENANT','PUBLIC','APPROVAL',1,'当审批流程全部完成时，通知发起人审批结果','2026-04-06 19:51:38','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(12,0,'UNREAD_SUMMARY','未读消息汇总通知','{\"zh-CN\":\"未读消息汇总通知\",\"en-US\":\"Unread Message Summary\"}','1.0.0','NOTICE',NULL,'info','PUBLIC','PUBLIC','SUMMARY',1,'用户登录时推送的未读消息汇总通知模板','2026-04-09 10:34:33','2026-04-09 10:34:33',0,'system','system'),(13,1993479636925403138,'WF_PENDING','审批待办通知','{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\",\"zh-TW\":\"審批待辦通知\",\"ja-JP\":\"承認保留通知\",\"ko-KR\":\"승인 대기 알림\"}','1.0.0','WARNING',NULL,'info','TENANT',NULL,NULL,1,'当审批流程流转到某个节点时，通知待审批人有新的待办任务','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(14,1993479636925403138,'WF_APPROVED','审批通过通知','{\"zh-CN\":\"审批通过通知\",\"en-US\":\"Approval Passed Notification\",\"zh-TW\":\"審批通過通知\",\"ja-JP\":\"承認済み通知\",\"ko-KR\":\"승인 완료 알림\"}','1.0.0','NOTICE',NULL,'info','TENANT',NULL,NULL,1,'当审批节点被通过时，通知发起人审批进度','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(15,1993479636925403138,'WF_REJECTED','审批驳回通知','{\"zh-CN\":\"审批驳回通知\",\"en-US\":\"Approval Rejected Notification\",\"zh-TW\":\"審批駁回通知\",\"ja-JP\":\"却下通知\",\"ko-KR\":\"승인 거부 알림\"}','1.0.0','ALARM',NULL,'info','TENANT',NULL,NULL,1,'当审批被驳回时，通知发起人驳回原因','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(16,1993479636925403138,'WF_FINISHED','审批完成通知','{\"zh-CN\":\"审批完成通知\",\"en-US\":\"Approval Completed Notification\",\"zh-TW\":\"審批完成通知\",\"ja-JP\":\"承認完了通知\",\"ko-KR\":\"승인 완료 알림\"}','1.0.0','NOTICE',NULL,'info','TENANT',NULL,NULL,1,'当审批流程全部完成时，通知发起人审批结果','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(17,1993479636925403138,'UNREAD_SUMMARY','未读消息汇总通知','{\"zh-CN\":\"未读消息汇总通知\",\"en-US\":\"Unread Message Summary\"}','1.0.0','NOTICE',NULL,'info','TENANT',NULL,NULL,1,'用户登录时推送的未读消息汇总通知模板','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242');
/*!40000 ALTER TABLE `sys_message_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message_template_content`
--

DROP TABLE IF EXISTS `sys_message_template_content`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template_content` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板内容ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_id` bigint NOT NULL COMMENT '模板主表ID',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱)',
  `content_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '消息标题(支持占位符,如${userName})',
  `content_title_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '消息标题多语言JSON',
  `content_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容(支持占位符,如${userName})',
  `content_body_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '消息内容多语言JSON',
  `link_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '跳转链接',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_template` (`template_id`) USING BTREE,
  KEY `idx_tenant_platform` (`tenant_id`,`platform`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息模板内容表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template_content`
--

LOCK TABLES `sys_message_template_content` WRITE;
/*!40000 ALTER TABLE `sys_message_template_content` DISABLE KEYS */;
INSERT INTO `sys_message_template_content` VALUES (1,1993479636925403138,1,'INTERNAL','',NULL,'',NULL,'','2026-03-31 21:03:10','2026-04-09 15:57:39',1,'1993479637244170242','1993479637244170242'),(2,0,2,'INTERNAL','【审批待办】${taskName}','{\"zh-CN\":\"【审批待办】${taskName}\",\"en-US\":\"[Pending Approval] ${taskName}\",\"zh-TW\":\"【審批待辦】${taskName}\",\"ja-JP\":\"【承認保留中】${taskName}\",\"ko-KR\":\"【승인 대기】${taskName}\"}','发起人：${initiatorName}\n当前节点：${nodeName}\n发起时间：${startTime}','{\"zh-CN\":\"发起人：${initiatorName}\\n当前节点：${nodeName}\\n发起时间：${startTime}\",\"en-US\":\"Initiator: ${initiatorName}\\nCurrent Node: ${nodeName}\\nStart Time: ${startTime}\",\"zh-TW\":\"發起人：${initiatorName}\\n當前節點：${nodeName}\\n發起時間：${startTime}\",\"ja-JP\":\"申請者：${initiatorName}\\n現在のノード：${nodeName}\\n開始時間：${startTime}\",\"ko-KR\":\"신청자: ${initiatorName}\\n현재 단계: ${nodeName}\\n시작 시간: ${startTime}\"}','/workspace/approval/my/pending','2026-04-06 19:49:57','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(3,0,3,'INTERNAL','【审批通过】${taskName}','{\"zh-CN\":\"【审批通过】${taskName}\",\"en-US\":\"[Approved] ${taskName}\",\"zh-TW\":\"【審批通過】${taskName}\",\"ja-JP\":\"【承認済み】${taskName}\",\"ko-KR\":\"【승인됨】${taskName}\"}','审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n审批意见：${comment}','{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n审批意见：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nComment: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n審批意見：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n承認意見：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n승인 의견: ${comment}\"}','/workspace/approval/my/initiated','2026-04-06 19:51:47','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(4,0,4,'INTERNAL','【审批驳回】${taskName}','{\"zh-CN\":\"【审批驳回】${taskName}\",\"en-US\":\"[Rejected] ${taskName}\",\"zh-TW\":\"【審批駁回】${taskName}\",\"ja-JP\":\"【却下】${taskName}\",\"ko-KR\":\"【거부됨】${taskName}\"}','审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n驳回原因：${comment}','{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n驳回原因：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nReason: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n駁回原因：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n却下理由：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n거부 사유: ${comment}\"}','/workspace/approval/my/initiated','2026-04-06 19:51:49','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(5,0,5,'INTERNAL','【审批完成】${taskName}','{\"zh-CN\":\"【审批完成】${taskName}\",\"en-US\":\"[Completed] ${taskName}\",\"zh-TW\":\"【審批完成】${taskName}\",\"ja-JP\":\"【承認完了】${taskName}\",\"ko-KR\":\"【완료】${taskName}\"}','审批名称：${taskName}\n发起人：${initiatorName}\n完成时间：${endTime}\n处理结果：${result}','{\"zh-CN\":\"审批名称：${taskName}\\n发起人：${initiatorName}\\n完成时间：${endTime}\\n处理结果：${result}\",\"en-US\":\"Task: ${taskName}\\nInitiator: ${initiatorName}\\nCompleted: ${endTime}\\nResult: ${result}\",\"zh-TW\":\"審批名稱：${taskName}\\n發起人：${initiatorName}\\n完成時間：${endTime}\\n處理結果：${result}\",\"ja-JP\":\"承認名：${taskName}\\n申請者：${initiatorName}\\n完了時間：${endTime}\\n処理結果：${result}\",\"ko-KR\":\"승인명: ${taskName}\\n신청자: ${initiatorName}\\n완료 시간: ${endTime}\\n처리 결과: ${result}\"}','/workspace/approval/my/initiated','2026-04-06 19:51:50','2026-05-06 17:29:21',0,'1993479637244170242','1993479637244170242'),(11,0,12,'INTERNAL','尊敬的${userName}，您有${unreadCount}条消息未读','{\"zh-CN\":\"尊敬的${userName}，您有${unreadCount}条消息未读\",\"en-US\":\"Dear ${userName}, you have ${unreadCount} unread messages\"}','您有${unreadCount}条未读消息，请及时查看。','{\"zh-CN\":\"您有${unreadCount}条未读消息，请及时查看。\",\"en-US\":\"You have ${unreadCount} unread messages, please check them in time.\"}','/workspace/message/unread','2026-04-09 10:34:33','2026-04-09 10:34:33',0,'system','system'),(12,1993479636925403138,13,'INTERNAL','【审批待办】${taskName}','{\"zh-CN\":\"【审批待办】${taskName}\",\"en-US\":\"[Pending Approval] ${taskName}\",\"zh-TW\":\"【審批待辦】${taskName}\",\"ja-JP\":\"【承認保留中】${taskName}\",\"ko-KR\":\"【승인 대기】${taskName}\"}','发起人：${initiatorName}\n当前节点：${nodeName}\n发起时间：${startTime}','{\"zh-CN\":\"发起人：${initiatorName}\\n当前节点：${nodeName}\\n发起时间：${startTime}\",\"en-US\":\"Initiator: ${initiatorName}\\nCurrent Node: ${nodeName}\\nStart Time: ${startTime}\",\"zh-TW\":\"發起人：${initiatorName}\\n當前節點：${nodeName}\\n發起時間：${startTime}\",\"ja-JP\":\"申請者：${initiatorName}\\n現在のノード：${nodeName}\\n開始時間：${startTime}\",\"ko-KR\":\"신청자: ${initiatorName}\\n현재 단계: ${nodeName}\\n시작 시간: ${startTime}\"}','/workspace/approval/my/pending','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(13,1993479636925403138,14,'INTERNAL','【审批通过】${taskName}','{\"zh-CN\":\"【审批通过】${taskName}\",\"en-US\":\"[Approved] ${taskName}\",\"zh-TW\":\"【審批通過】${taskName}\",\"ja-JP\":\"【承認済み】${taskName}\",\"ko-KR\":\"【승인됨】${taskName}\"}','审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n审批意见：${comment}','{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n审批意见：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nComment: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n審批意見：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n承認意見：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n승인 의견: ${comment}\"}','/workspace/approval/my/initiated','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(14,1993479636925403138,15,'INTERNAL','【审批驳回】${taskName}','{\"zh-CN\":\"【审批驳回】${taskName}\",\"en-US\":\"[Rejected] ${taskName}\",\"zh-TW\":\"【審批駁回】${taskName}\",\"ja-JP\":\"【却下】${taskName}\",\"ko-KR\":\"【거부됨】${taskName}\"}','审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n驳回原因：${comment}','{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n驳回原因：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nReason: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n駁回原因：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n却下理由：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n거부 사유: ${comment}\"}','/workspace/approval/my/initiated','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(15,1993479636925403138,16,'INTERNAL','【审批完成】${taskName}','{\"zh-CN\":\"【审批完成】${taskName}\",\"en-US\":\"[Completed] ${taskName}\",\"zh-TW\":\"【審批完成】${taskName}\",\"ja-JP\":\"【承認完了】${taskName}\",\"ko-KR\":\"【완료】${taskName}\"}','审批名称：${taskName}\n发起人：${initiatorName}\n完成时间：${endTime}\n处理结果：${result}','{\"zh-CN\":\"审批名称：${taskName}\\n发起人：${initiatorName}\\n完成时间：${endTime}\\n处理结果：${result}\",\"en-US\":\"Task: ${taskName}\\nInitiator: ${initiatorName}\\nCompleted: ${endTime}\\nResult: ${result}\",\"zh-TW\":\"審批名稱：${taskName}\\n發起人：${initiatorName}\\n完成時間：${endTime}\\n處理結果：${result}\",\"ja-JP\":\"承認名：${taskName}\\n申請者：${initiatorName}\\n完了時間：${endTime}\\n処理結果：${result}\",\"ko-KR\":\"승인명: ${taskName}\\n신청자: ${initiatorName}\\n완료 시간: ${endTime}\\n처리 결과: ${result}\"}','/workspace/approval/my/initiated','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(16,1993479636925403138,17,'INTERNAL','尊敬的${userName}，您有${unreadCount}条消息未读','{\"zh-CN\":\"尊敬的${userName}，您有${unreadCount}条消息未读\",\"en-US\":\"Dear ${userName}, you have ${unreadCount} unread messages\"}','您有${unreadCount}条未读消息，请及时查看。','{\"zh-CN\":\"您有${unreadCount}条未读消息，请及时查看。\",\"en-US\":\"You have ${unreadCount} unread messages, please check them in time.\"}','/workspace/message/unread','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242');
/*!40000 ALTER TABLE `sys_message_template_content` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_message_template_receiver`
--

DROP TABLE IF EXISTS `sys_message_template_receiver`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_message_template_receiver` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接收人配置ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_id` bigint NOT NULL COMMENT '模板主表ID',
  `receiver_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收类型(ROLE=角色,DEPT=部门,POSITION=职位,USER=指定人)',
  `receiver_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收人ID列表(JSON数组格式,如[1,2,3])',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_template` (`template_id`) USING BTREE,
  KEY `idx_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='消息模板接收人表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_message_template_receiver`
--

LOCK TABLES `sys_message_template_receiver` WRITE;
/*!40000 ALTER TABLE `sys_message_template_receiver` DISABLE KEYS */;
INSERT INTO `sys_message_template_receiver` VALUES (1,1993479636925403138,1,'USER','[\"1993479637244170249\"]','2026-03-31 21:03:10','2026-04-09 15:57:39',1,'1993479637244170242','1993479637244170242'),(2,0,7,'CUSTOM','[]','2026-04-08 23:25:39','2026-04-08 23:25:39',0,'system','system'),(3,0,2,'CUSTOM','[]','2026-04-08 23:25:39','2026-04-09 11:47:46',0,'system','system'),(4,0,3,'CUSTOM','[]','2026-04-08 23:25:39','2026-04-09 11:47:46',0,'system','system'),(5,0,4,'CUSTOM','[]','2026-04-08 23:25:39','2026-04-09 11:47:46',0,'system','system'),(6,0,5,'CUSTOM','[]','2026-04-08 23:25:39','2026-04-09 11:47:46',0,'system','system'),(7,0,8,'CUSTOM','[]','2026-04-08 23:52:24','2026-04-08 23:52:24',0,'system','system'),(8,0,9,'CUSTOM','[]','2026-04-08 23:53:25','2026-04-08 23:53:25',0,'system','system'),(9,0,10,'CUSTOM','[]','2026-04-08 23:53:44','2026-04-08 23:53:44',0,'system','system'),(11,0,12,'CUSTOM','[]','2026-04-09 10:34:33','2026-04-09 10:34:33',0,'system','system'),(12,1993479636925403138,13,'CUSTOM','[]','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(13,1993479636925403138,14,'CUSTOM','[]','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(14,1993479636925403138,15,'CUSTOM','[]','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(15,1993479636925403138,16,'CUSTOM','[]','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242'),(16,1993479636925403138,17,'CUSTOM','[]','2026-04-09 15:57:26','2026-04-09 15:57:26',0,'1993479637244170242','1993479637244170242');
/*!40000 ALTER TABLE `sys_message_template_receiver` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_module`
--

DROP TABLE IF EXISTS `sys_module`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_module` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称',
  `name_i18n_json` json DEFAULT NULL COMMENT '模块名称国际化(JSON)',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '图标',
  `order_num` int DEFAULT NULL COMMENT '排序号',
  `visible` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否可见：1可见 0隐藏',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sys_module_tenant` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='系统模块表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_module`
--

LOCK TABLES `sys_module` WRITE;
/*!40000 ALTER TABLE `sys_module` DISABLE KEYS */;
INSERT INTO `sys_module` VALUES (1,1993479636925403138,'sys','系统管理','{\"en-US\": \"System\", \"ja-JP\": \"システム管理\", \"ko-KR\": \"시스템 관리\", \"zh-CN\": \"系统管理\", \"zh-TW\": \"系統管理\"}','SettingOutlined',10,1,1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0),(3,1993479636925403138,'approval','审批管理','{\"en-US\": \"Approval\", \"ja-JP\": \"承認管理\", \"ko-KR\": \"승인 관리\", \"zh-CN\": \"审批管理\", \"zh-TW\": \"審批管理\"}','AuditOutlined',50,1,1,'2026-04-02 14:50:02','system','2026-04-07 20:18:40','system',0),(4,0,'system','系统管理','{\"en-US\": \"System Management\", \"ja-JP\": \"システム管理\", \"ko-KR\": \"시스템 관리\", \"zh-CN\": \"系统管理\", \"zh-TW\": \"系統管理\"}','SettingOutlined',10,1,1,'2026-04-06 23:16:09','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0),(5,1993479636925403138,'basic','基础信息','{\"en-US\": \"Basic Information\", \"ja-JP\": \"基本情報\", \"ko-KR\": \"기본 정보\", \"zh-CN\": \"基础信息\", \"zh-TW\": \"基礎資訊\"}','DatabaseOutlined',40,1,1,'2026-04-09 18:10:41','system','2026-05-02 21:36:58','20260502_basic_unit_and_table_upgrade',0),(6,1993479636925403138,'integration','接口平台','{\"en-US\": \"Integration Platform\", \"zh-CN\": \"接口平台\"}','ApiOutlined',90,1,1,'2026-04-14 15:49:25','20260414_init','2026-04-21 16:22:51','20260421_restore_integration_menu',0),(7,1993479636925403140,'sys','系统管理','{\"en-US\": \"System\", \"ja-JP\": \"システム管理\", \"ko-KR\": \"시스템 관리\", \"zh-CN\": \"系统管理\", \"zh-TW\": \"系統管理\"}','SettingOutlined',10,1,1,'2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:16:01','20260512_supplier_tenant_login_fix',0);
/*!40000 ALTER TABLE `sys_module` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice`
--

DROP TABLE IF EXISTS `sys_notice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID，0表示公共通知',
  `title` varchar(200) NOT NULL COMMENT '通知标题',
  `scope` varchar(20) NOT NULL DEFAULT 'TENANT' COMMENT '通知范围：PUBLIC/TENANT',
  `content_html` longtext NOT NULL COMMENT '富文本HTML内容',
  `summary` varchar(500) DEFAULT NULL COMMENT '摘要',
  `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/DISABLED',
  `start_time` datetime DEFAULT NULL COMMENT '生效时间',
  `end_time` datetime DEFAULT NULL COMMENT '失效时间',
  `order_num` int NOT NULL DEFAULT '0' COMMENT '排序值',
  `force_remind` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否强提醒',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_notice_tenant_id` (`tenant_id`),
  KEY `idx_sys_notice_status_scope` (`status`,`scope`),
  KEY `idx_sys_notice_order_num` (`order_num`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知主表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice`
--

LOCK TABLES `sys_notice` WRITE;
/*!40000 ALTER TABLE `sys_notice` DISABLE KEYS */;
INSERT INTO `sys_notice` VALUES (1,1993479636925403138,'号外号外','TENANT','<p>猜测测试八嘎雅鹿</p><p><img src=\"http://192.168.121.1:9000/api/sys/files/115c1fa130894e15bb2a04b87c383c9c.png\" alt=\"image.png\" data-href=\"http://192.168.121.1:9000/api/sys/files/115c1fa130894e15bb2a04b87c383c9c.png\" style=\"\"/></p>','参数','DRAFT','2026-05-10 09:57:48','2026-05-30 09:57:53',0,1,'2026-05-11 09:58:11','1993479637244170242','2026-05-11 09:58:11','1993479637244170242',0),(2,1993479636925403138,'cs','TENANT','<p>1</p>','1','PUBLISHED','2026-05-10 09:58:52','2026-05-28 09:58:56',0,0,'2026-05-11 09:59:10','1993479637244170242','2026-05-11 09:59:10','1993479637244170242',0),(3,1993479636925403138,'1','TENANT','<p>1</p>','1','PUBLISHED','2026-05-06 09:59:26','2026-05-30 09:59:29',0,1,'2026-05-11 09:59:41','1993479637244170242','2026-05-11 09:59:41','1993479637244170242',0);
/*!40000 ALTER TABLE `sys_notice` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice_attachment`
--

DROP TABLE IF EXISTS `sys_notice_attachment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_attachment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `notice_id` bigint NOT NULL COMMENT '通知ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_url` varchar(500) NOT NULL COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  KEY `idx_sys_notice_attachment_notice_id` (`notice_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知附件表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice_attachment`
--

LOCK TABLES `sys_notice_attachment` WRITE;
/*!40000 ALTER TABLE `sys_notice_attachment` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice_attachment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_notice_user_record`
--

DROP TABLE IF EXISTS `sys_notice_user_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_notice_user_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户ID',
  `notice_id` bigint NOT NULL COMMENT '通知ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `popup_time` datetime DEFAULT NULL COMMENT '弹出时间',
  `ack_time` datetime DEFAULT NULL COMMENT '确认时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_notice_user_record` (`tenant_id`,`user_id`,`notice_id`),
  KEY `idx_sys_notice_user_record_notice_id` (`notice_id`),
  KEY `idx_sys_notice_user_record_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知用户记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_notice_user_record`
--

LOCK TABLES `sys_notice_user_record` WRITE;
/*!40000 ALTER TABLE `sys_notice_user_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_notice_user_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_permission`
--

DROP TABLE IF EXISTS `sys_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_permission` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'URL',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '方法 (GET/POST等)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `permission_key` (`permission_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=3000000000000000632 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='权限表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_permission`
--

LOCK TABLES `sys_permission` WRITE;
/*!40000 ALTER TABLE `sys_permission` DISABLE KEYS */;
INSERT INTO `sys_permission` VALUES (1,'查看用户','sys:user:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(2,'查看角色','sys:role:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(3,'查看模块','sys:module:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(4,'查看菜单','sys:menu:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(5,'查看部门','sys:dept:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(6,'查看职位','sys:position:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(7,'添加用户','sys:user:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(8,'编辑用户','sys:user:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(9,'删除用户','sys:user:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(10,'批量删除用户','sys:user:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(11,'重置密码','sys:user:resetPwd',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(12,'导出用户','sys:user:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(13,'分配角色','sys:user:assignRole',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(14,'添加角色','sys:role:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(15,'编辑角色','sys:role:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(16,'删除角色','sys:role:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(17,'批量删除角色','sys:role:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(18,'授权菜单','sys:role:authMenu',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(19,'添加模块','sys:module:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(20,'编辑模块','sys:module:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(21,'删除模块','sys:module:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(22,'批量删除模块','sys:module:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(23,'添加菜单','sys:menu:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(24,'编辑菜单','sys:menu:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(25,'删除菜单','sys:menu:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(26,'批量删除菜单','sys:menu:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(27,'添加部门','sys:dept:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(28,'编辑部门','sys:dept:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(29,'删除部门','sys:dept:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(30,'添加职位','sys:position:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(31,'编辑职位','sys:position:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(32,'删除职位','sys:position:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(33,'批量删除职位','sys:position:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(34,'查看仪表盘','sys:dashboard:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(35,'查看导出配置','sys:excel:exportConfig:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(36,'查看导入配置','sys:excel:importConfig:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(37,'列出导出配置','sys:excel:exportConfig:list',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(38,'编辑导出配置','sys:excel:exportConfig:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(39,'删除导出配置','sys:excel:exportConfig:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(40,'列出导入配置','sys:excel:importConfig:list',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(41,'编辑导入配置','sys:excel:importConfig:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(42,'删除导入配置','sys:excel:importConfig:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(43,'下载模板','sys:excel:template:download',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(44,'导出登录日志','sys:excel:export:loginLog',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(45,'导出用户','sys:excel:export:user',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(46,'查看字典','sys:dict:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(47,'添加字典','sys:dict:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(48,'编辑字典','sys:dict:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(49,'删除字典','sys:dict:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(50,'批量删除字典','sys:dict:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(51,'导出字典','sys:dict:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(52,'查看字典类型','sys:dictType:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(53,'添加字典类型','sys:dictType:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(54,'编辑字典类型','sys:dictType:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(55,'删除字典类型','sys:dictType:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(56,'批量删除字典类型','sys:dictType:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(57,'导出字典类型','sys:dictType:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(58,'查看表格配置','sys:tableConfig:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(59,'添加表格配置','sys:tableConfig:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(60,'编辑表格配置','sys:tableConfig:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(61,'删除表格配置','sys:tableConfig:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(62,'批量删除表格配置','sys:tableConfig:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(63,'导出表格配置','sys:tableConfig:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(64,'查看登录日志','sys:loginLog:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(65,'删除登录日志','sys:loginLog:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(66,'批量删除登录日志','sys:loginLog:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(67,'导出登录日志','sys:loginLog:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(68,'查看在线用户','sys:online:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(69,'踢出用户','sys:online:kickout',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(70,'批量踢出用户','sys:online:batchKickout',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(71,'导出在线用户','sys:online:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(72,'查看租户','sys:tenant:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(73,'添加租户','sys:tenant:add',NULL,'POST',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(74,'编辑租户','sys:tenant:edit',NULL,'PUT',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(75,'删除租户','sys:tenant:delete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(76,'批量删除租户','sys:tenant:batchDelete',NULL,'DELETE',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(77,'导出租户','sys:tenant:export',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(78,'查看组织架构','sys:organization:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(79,'查看授权管理','sys:authorization:view',NULL,'GET',0,'2026-01-18 21:53:57','2026-01-18 21:53:57',0),(92,'编码规则 - 查询','encode:rule:query','/api/sys/encodeRule/**','GET',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(93,'编码规则 - 新增','encode:rule:add','/api/sys/encodeRule','POST',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(94,'编码规则 - 编辑','encode:rule:edit','/api/sys/encodeRule/**','PUT',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(95,'编码规则 - 删除','encode:rule:delete','/api/sys/encodeRule/**','DELETE',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(96,'编码规则 - 启用禁用','encode:rule:toggle','/api/sys/encodeRule/toggleStatus','PUT',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(97,'编码规则 - 生成编码','encode:rule:generate','/api/sys/encodeRule/generateCode','POST',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(98,'编码示例 - 查询','encode:example:query','/api/sys/encodeRuleExample/**','GET',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(99,'编码示例 - 新增','encode:example:add','/api/sys/encodeRuleExample','POST',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(100,'编码示例 - 编辑','encode:example:edit','/api/sys/encodeRuleExample/**','PUT',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(101,'编码示例 - 删除','encode:example:delete','/api/sys/encodeRuleExample/**','DELETE',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(102,'历史记录 - 查询','encode:history:query','/api/sys/encodeHistory/**','GET',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(103,'历史记录 - 导出','encode:history:export','/api/sys/encodeHistory/export','POST',1993479636925403138,'2026-04-10 11:30:05','2026-04-10 11:30:05',0),(104,'查看安卓版本','sys:androidVersion:view',NULL,'POST',0,'2026-05-05 23:01:45','2026-05-05 23:01:45',0),(105,'新增安卓版本','sys:androidVersion:add',NULL,'POST',0,'2026-05-05 23:01:45','2026-05-05 23:01:45',0),(106,'编辑安卓版本','sys:androidVersion:edit',NULL,'POST',0,'2026-05-05 23:01:45','2026-05-05 23:01:45',0),(107,'删除安卓版本','sys:androidVersion:delete',NULL,'POST',0,'2026-05-05 23:01:45','2026-05-05 23:01:45',0),(3000000000000000561,'查看系统通知','sys:notice:view','/sys/notice/page','POST',0,'2026-05-10 21:26:44','2026-05-10 21:26:44',0),(3000000000000000562,'新增系统通知','sys:notice:add','/sys/notice/save','POST',0,'2026-05-10 21:26:44','2026-05-10 21:26:44',0),(3000000000000000563,'编辑系统通知','sys:notice:edit','/sys/notice/save','POST',0,'2026-05-10 21:26:44','2026-05-10 21:26:44',0),(3000000000000000564,'删除系统通知','sys:notice:delete','/sys/notice/delete','POST',0,'2026-05-10 21:26:44','2026-05-10 21:26:44',0),(3000000000000000565,'发布/停用系统通知','sys:notice:publish','/sys/notice/publish','POST',0,'2026-05-10 21:26:44','2026-05-10 21:26:44',0),(3000000000000000566,'Job Dashboard View','job:dashboard:view',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000567,'Job Task List','job:task:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000568,'Job Task View','job:task:view',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000569,'Job Task Add','job:task:add',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000570,'Job Task Edit','job:task:edit',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000571,'Job Task Delete','job:task:delete',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000572,'Job Task Change Status','job:task:changeStatus',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000573,'Job Task Trigger','job:task:trigger',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000574,'Job Log List','job:log:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000575,'Job Log View','job:log:view',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000576,'Job Instance List','job:instance:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000577,'Job Instance Maintenance','job:instance:maintenance',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000578,'Job Retry List','job:retry:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000579,'Job Retry Handle','job:retry:handle',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000580,'Job Alarm List','job:alarm:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000581,'Job Alarm View','job:alarm:view',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000582,'Job Alarm Add','job:alarm:add',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000583,'Job Alarm Edit','job:alarm:edit',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000584,'Job Alarm Delete','job:alarm:delete',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000585,'Job Workflow List','job:workflow:list',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000586,'Job Workflow View','job:workflow:view',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000587,'Job Workflow Add','job:workflow:add',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000588,'Job Workflow Edit','job:workflow:edit',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000589,'Job Workflow Publish','job:workflow:publish',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000590,'Job Workflow Execute','job:workflow:execute',NULL,'POST',0,'2026-05-13 11:38:57','2026-05-13 11:38:57',0),(3000000000000000591,'查看首页组件目录','sys:homepageComponent:view','/sys/homepage/component/page','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000592,'新增首页组件','sys:homepageComponent:add','/sys/homepage/component/save','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000593,'编辑首页组件','sys:homepageComponent:edit','/sys/homepage/component/save','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000594,'删除首页组件','sys:homepageComponent:delete','/sys/homepage/component/delete','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000595,'拉取公共首页组件','sys:homepageComponent:pullPublic','/sys/homepage/component/pull-public','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000596,'查看可用首页组件','sys:homepageComponent:effectiveList','/sys/homepage/component/effective/list','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000597,'收藏首页组件','sys:homepageComponent:favorite','/sys/homepage/component/favorite','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000598,'添加组件到个人首页','sys:homepageComponent:addToHomepage','/sys/homepage/component/add','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000599,'移除首页组件','sys:homepageComponent:remove','/sys/homepage/component/remove','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000600,'拉取租户首页组件','sys:homepageComponent:pullTenant','/sys/homepage/component/pull-tenant','POST',0,'2026-05-15 18:39:01','2026-05-15 18:39:01',0),(3000000000000000601,'标签类型查询','label:type:query','/basic/label/type/page','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000602,'标签类型新增','label:type:add','/basic/label/type/add','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000603,'标签类型编辑','label:type:edit','/basic/label/type/update','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000604,'标签类型删除','label:type:delete','/basic/label/type/delete','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000605,'标签类型启停','label:type:enable','/basic/label/type/enable','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000606,'标签字段查询','label:field:query','/basic/label/field/page','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000607,'标签字段新增','label:field:add','/basic/label/field/add','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000608,'标签字段编辑','label:field:edit','/basic/label/field/update','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000609,'标签字段删除','label:field:delete','/basic/label/field/delete','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000610,'标签字段启停','label:field:enable','/basic/label/field/enable','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000611,'标签字段导入','label:field:import','/basic/label/field/import','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000612,'标签模板查询','label:template:query','/basic/label/template/page','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000613,'标签模板新增','label:template:add','/basic/label/template/add','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000614,'标签模板编辑','label:template:edit','/basic/label/template/update','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000615,'标签模板删除','label:template:delete','/basic/label/template/delete','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000616,'标签模板批量删除','label:template:batchDelete','/basic/label/template/batchDelete','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000617,'标签模板设为默认','label:template:setDefault','/basic/label/template/setDefault','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000618,'标签模板预览','label:template:preview','/basic/label/template/preview','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000619,'标签模板设计详情','label:template:designDetail','/basic/label/template/design/detail','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000620,'标签模板设计保存','label:template:designSave','/basic/label/template/design/save','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000621,'标签打印查询','label:print:query','/basic/label/print/preview','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000622,'标签打印执行','label:print:execute','/basic/label/print/execute','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000623,'标签打印渲染','label:print:render','/basic/label/print/render','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000624,'标签打印重打','label:print:reprint','/basic/label/record/reprint','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000625,'标签打印记录查询','label:record:query','/basic/label/record/page','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000626,'标签打印记录详情','label:record:detail','/basic/label/record/detail','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000627,'标签绑定查询','label:binding:query','/basic/label/binding/page','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000628,'标签绑定新增','label:binding:add','/basic/label/binding/add','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000629,'标签绑定编辑','label:binding:edit','/basic/label/binding/update','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000630,'标签绑定删除','label:binding:delete','/basic/label/binding/delete','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0),(3000000000000000631,'标签绑定匹配','label:binding:match','/basic/label/binding/match','POST',0,'2026-05-15 21:13:24','2026-05-15 21:13:24',0);
/*!40000 ALTER TABLE `sys_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_position`
--

DROP TABLE IF EXISTS `sys_position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_position` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位名称',
  `position_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位编码',
  `position_level` int DEFAULT NULL COMMENT '职位级别',
  `order_num` int DEFAULT '0' COMMENT '排序号',
  `status` tinyint DEFAULT '1' COMMENT '状态：0=禁用，1=启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `deleted` tinyint DEFAULT '0' COMMENT '删除标志：0=未删除，1=已删除',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_position_code` (`position_code`,`tenant_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_deleted` (`deleted`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='职位表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_position`
--

LOCK TABLES `sys_position` WRITE;
/*!40000 ALTER TABLE `sys_position` DISABLE KEYS */;
INSERT INTO `sys_position` VALUES (1,'总经理','POS_GM',1,1,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(2,'副总经理','POS_DGM',2,2,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(3,'部门经理','POS_MGR',3,3,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(4,'主管','POS_SUP',4,4,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(5,'组长','POS_LEAD',5,5,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(6,'高级工程师','POS_SR_ENG',6,6,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(7,'工程师','POS_ENG',7,7,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(8,'技术员','POS_TECH',8,8,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(9,'操作员','POS_OP',9,9,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(10,'文员','POS_CLERK',10,10,1,NULL,'2026-01-07 14:41:44','2026-05-06 17:27:43','1993479637244170242','1993479637244170242',1,0,NULL),(11,'?????','PLATFORM_ADMIN',1,1,1,'??????','2026-04-02 14:46:48','2026-04-05 16:48:54','system','1993479637244170242',1993479636925403138,1,18),(12,'测试职位','TEST',1,0,1,NULL,'2026-04-04 16:16:28','2026-04-30 17:29:08','1993479637244170242','1993479637244170242',1993479636925403138,0,19);
/*!40000 ALTER TABLE `sys_position` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role`
--

DROP TABLE IF EXISTS `sys_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态',
  `data_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'SELF' COMMENT '数据权限范围：ALL-全部数据,DEPT_AND_CHILD-本部门及下级,DEPT-本部门,SELF-仅本人,CUSTOM-自定义',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint DEFAULT '0',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_tenant` (`tenant_id`,`role_key`) USING BTREE,
  KEY `idx_role_key` (`role_key`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1993479637311279117 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role`
--

LOCK TABLES `sys_role` WRITE;
/*!40000 ALTER TABLE `sys_role` DISABLE KEYS */;
INSERT INTO `sys_role` VALUES (1993479637311279107,'系统管理员','admin',NULL,1,'SELF',1993479636925403138,'2025-11-26 08:39:17','1993479637244170242','2026-05-06 17:26:58','1993479637244170242',0),(1993479637311279112,'普通用户','user',NULL,1,'SELF',1993479636925403138,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:26:58','1993479637244170242',0),(1993479637311279113,'部门经理','manager',NULL,1,'SELF',1993479636925403138,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:26:58','1993479637244170242',0),(1993479637311279114,'系统审计员','auditor',NULL,1,'SELF',1993479636925403138,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:26:58','1993479637244170242',0),(1993479637311279116,'系统管理员','admin','系统管理员，拥有当前供应商租户权限',1,'ALL',1993479636925403140,'2026-05-12 23:18:18','20260512_supplier_tenant_login_fix','2026-05-12 23:27:41','20260512_supplier_tenant_login_fix',0);
/*!40000 ALTER TABLE `sys_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_c_menu`
--

DROP TABLE IF EXISTS `sys_role_c_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_c_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `c_menu_id` bigint NOT NULL COMMENT 'C 端菜单 ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_role_c_menu` (`tenant_id`,`role_id`,`c_menu_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_c_menu_id` (`c_menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='C 端角色菜单关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_c_menu`
--

LOCK TABLES `sys_role_c_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_c_menu` DISABLE KEYS */;
INSERT INTO `sys_role_c_menu` VALUES (4,NULL,1993479637311279107,1),(3,NULL,1993479637311279107,2),(2,NULL,1993479637311279107,3),(1,NULL,1993479637311279107,4),(16,NULL,1993479637311279112,1),(15,NULL,1993479637311279112,2),(14,NULL,1993479637311279112,3),(13,NULL,1993479637311279112,4),(12,NULL,1993479637311279113,1),(11,NULL,1993479637311279113,2),(10,NULL,1993479637311279113,3),(9,NULL,1993479637311279113,4),(8,NULL,1993479637311279114,1),(7,NULL,1993479637311279114,2),(6,NULL,1993479637311279114,3),(5,NULL,1993479637311279114,4),(17,1993479636925403138,1993479637311279107,5),(21,1993479636925403138,1993479637311279107,6),(20,1993479636925403138,1993479637311279112,5),(24,1993479636925403138,1993479637311279112,6),(19,1993479636925403138,1993479637311279113,5),(23,1993479636925403138,1993479637311279113,6),(18,1993479636925403138,1993479637311279114,5),(22,1993479636925403138,1993479637311279114,6);
/*!40000 ALTER TABLE `sys_role_c_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_dept`
--

DROP TABLE IF EXISTS `sys_role_dept`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_dept` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_dept_id` (`dept_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色-部门关联表（数据权限）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_dept`
--

LOCK TABLES `sys_role_dept` WRITE;
/*!40000 ALTER TABLE `sys_role_dept` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_dept` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_menu`
--

DROP TABLE IF EXISTS `sys_role_menu`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_role_menu` (`tenant_id`,`role_id`,`menu_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2047520831801663726 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色菜单授权';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_menu`
--

LOCK TABLES `sys_role_menu` WRITE;
/*!40000 ALTER TABLE `sys_role_menu` DISABLE KEYS */;
INSERT INTO `sys_role_menu` VALUES (2012030852827447297,1993479636925403138,1993479637311279107,1),(2012030852827447298,1993479636925403138,1993479637311279107,2),(2012030852890361858,1993479636925403138,1993479637311279107,3),(2012030852890361859,1993479636925403138,1993479637311279107,4),(2012030852957470722,1993479636925403138,1993479637311279107,5),(2012030852957470723,1993479636925403138,1993479637311279107,6),(2012030851657236482,1993479636925403138,1993479637311279107,101),(2012030851657236483,1993479636925403138,1993479637311279107,102),(2012030851657236484,1993479636925403138,1993479637311279107,103),(2012030851724345346,1993479636925403138,1993479637311279107,104),(2012030851724345347,1993479636925403138,1993479637311279107,105),(2012030851787259905,1993479636925403138,1993479637311279107,106),(2012030851854368769,1993479636925403138,1993479637311279107,201),(2012030851854368770,1993479636925403138,1993479637311279107,202),(2012030851854368771,1993479636925403138,1993479637311279107,203),(2012030851917283329,1993479636925403138,1993479637311279107,204),(2012030851917283330,1993479636925403138,1993479637311279107,205),(2012030851980197889,1993479636925403138,1993479637311279107,301),(2012030851980197890,1993479636925403138,1993479637311279107,302),(2012030852043112450,1993479636925403138,1993479637311279107,303),(2012030852043112451,1993479636925403138,1993479637311279107,304),(2012030852043112452,1993479636925403138,1993479637311279107,401),(2012030852106027009,1993479636925403138,1993479637311279107,402),(2012030852106027010,1993479636925403138,1993479637311279107,403),(2012030852173135873,1993479636925403138,1993479637311279107,404),(2012030852173135874,1993479636925403138,1993479637311279107,501),(2012030852173135875,1993479636925403138,1993479637311279107,502),(2012030852236050433,1993479636925403138,1993479637311279107,503),(2012030852236050434,1993479636925403138,1993479637311279107,601),(2012030852298964993,1993479636925403138,1993479637311279107,602),(2012030852298964994,1993479636925403138,1993479637311279107,603),(2012030852298964995,1993479636925403138,1993479637311279107,604),(2012030851619487746,1993479636925403138,1993479637311279107,606),(2012030853020385282,1993479636925403138,1993479637311279107,607),(2012030853083299842,1993479636925403138,1993479637311279107,608),(2012030852370268161,1993479636925403138,1993479637311279107,609),(2012030852370268162,1993479636925403138,1993479637311279107,610),(2012030852496097281,1993479636925403138,1993479637311279107,611),(2012030852630315009,1993479636925403138,1993479637311279107,612),(2012030852630315010,1993479636925403138,1993479637311279107,613),(2012030852697423874,1993479636925403138,1993479637311279107,614),(2012030852760338434,1993479636925403138,1993479637311279107,615),(2012030852567400449,1993479636925403138,1993479637311279107,616),(2012030852567400450,1993479636925403138,1993479637311279107,617),(2012030851787259906,1993479636925403138,1993479637311279107,619),(2012030853083299843,1993479636925403138,1993479637311279107,620),(2012030853083299844,1993479636925403138,1993479637311279107,621),(2012030853150408706,1993479636925403138,1993479637311279107,622),(2012030853150408707,1993479636925403138,1993479637311279107,623),(2012030853150408708,1993479636925403138,1993479637311279107,624),(2012030853217517569,1993479636925403138,1993479637311279107,625),(2012030853217517570,1993479636925403138,1993479637311279107,626),(2012030853280432130,1993479636925403138,1993479637311279107,627),(2012030853280432131,1993479636925403138,1993479637311279107,628),(2012030853347540993,1993479636925403138,1993479637311279107,629),(2012030853347540994,1993479636925403138,1993479637311279107,630),(2012030853347540995,1993479636925403138,1993479637311279107,631),(2012030853414649858,1993479636925403138,1993479637311279107,632),(2012030853414649859,1993479636925403138,1993479637311279107,633),(2012030853414649860,1993479636925403138,1993479637311279107,634),(2012030853485953026,1993479636925403138,1993479637311279107,635),(2012030853485953027,1993479636925403138,1993479637311279107,636),(2012030853548867585,1993479636925403138,1993479637311279107,637),(2012030853548867586,1993479636925403138,1993479637311279107,638),(2012030853615976450,1993479636925403138,1993479637311279107,639),(2012030853615976451,1993479636925403138,1993479637311279107,640),(2012030853615976452,1993479636925403138,1993479637311279107,641),(2012030853678891009,1993479636925403138,1993479637311279107,642),(2012030853678891010,1993479636925403138,1993479637311279107,643),(2012030853741805570,1993479636925403138,1993479637311279107,644),(2012030853741805571,1993479636925403138,1993479637311279107,645),(2012030853741805572,1993479636925403138,1993479637311279107,646),(2012030853808914434,1993479636925403138,1993479637311279107,647),(2012030853808914435,1993479636925403138,1993479637311279107,648),(2012030853871828994,1993479636925403138,1993479637311279107,649),(2012030853871828995,1993479636925403138,1993479637311279107,650),(2012030853871828996,1993479636925403138,1993479637311279107,651),(2012030853871828997,1993479636925403138,1993479637311279107,652),(2012030853871828998,1993479636925403138,1993479637311279107,653),(2012030853871829081,1993479636925403138,1993479637311279107,654),(2012030853871829082,1993479636925403138,1993479637311279107,655),(2012030853871829083,1993479636925403138,1993479637311279107,656),(2012030853871829084,1993479636925403138,1993479637311279107,657),(2012030853871829100,1993479636925403138,1993479637311279107,667),(2012030853871829101,1993479636925403138,1993479637311279107,668),(2012030853871829102,1993479636925403138,1993479637311279107,669),(2012030853871829103,1993479636925403138,1993479637311279107,670),(2012030853871829104,1993479636925403138,1993479637311279107,671),(2012030853871829105,1993479636925403138,1993479637311279107,672),(2012030853871829106,1993479636925403138,1993479637311279107,673),(2012030853871829107,1993479636925403138,1993479637311279107,674),(2012030853871829111,1993479636925403138,1993479637311279107,675),(2012030853871829112,1993479636925403138,1993479637311279107,676),(2012030853871829113,1993479636925403138,1993479637311279107,677),(2012030853871829114,1993479636925403138,1993479637311279107,678),(2012030853871829115,1993479636925403138,1993479637311279107,679),(2012030853871829116,1993479636925403138,1993479637311279107,680),(2012030853871829117,1993479636925403138,1993479637311279107,683),(2012030853871829118,1993479636925403138,1993479637311279107,684),(2012030853871829119,1993479636925403138,1993479637311279107,685),(2012030853871829120,1993479636925403138,1993479637311279107,686),(2012030853871829121,1993479636925403138,1993479637311279107,687),(2012030853871829122,1993479636925403138,1993479637311279107,688),(2012030853871829123,1993479636925403138,1993479637311279107,689),(2012030853871829126,1993479636925403138,1993479637311279107,690),(2012030853871829132,1993479636925403138,1993479637311279107,695),(2012030853871829133,1993479636925403138,1993479637311279107,696),(2012030853871829138,1993479636925403138,1993479637311279107,697),(2012030853871829140,1993479636925403138,1993479637311279107,699),(2012030853871829157,1993479636925403138,1993479637311279107,700),(2012030853871829158,1993479636925403138,1993479637311279107,701),(2012030853871829159,1993479636925403138,1993479637311279107,702),(2012030853871829160,1993479636925403138,1993479637311279107,703),(2012030853871829167,1993479636925403138,1993479637311279107,704),(2012030853871829168,1993479636925403138,1993479637311279107,705),(2012030853871829169,1993479636925403138,1993479637311279107,706),(2012030853871829170,1993479636925403138,1993479637311279107,707),(2012030853871829171,1993479636925403138,1993479637311279107,708),(2012030853871829172,1993479636925403138,1993479637311279107,709),(2012030853871829161,1993479636925403138,1993479637311279107,711),(2012030853871829162,1993479636925403138,1993479637311279107,712),(2012030853871829163,1993479636925403138,1993479637311279107,713),(2012030853871829164,1993479636925403138,1993479637311279107,714),(2012030853871829165,1993479636925403138,1993479637311279107,715),(2012030853871829166,1993479636925403138,1993479637311279107,716),(2012030853871829173,1993479636925403138,1993479637311279107,720),(2012030853871829174,1993479636925403138,1993479637311279107,721),(2012030853871829175,1993479636925403138,1993479637311279107,722),(2012030853871829176,1993479636925403138,1993479637311279107,723),(2012030853871829177,1993479636925403138,1993479637311279107,724),(2012030853871829178,1993479636925403138,1993479637311279107,725),(2012030853871829179,1993479636925403138,1993479637311279107,726),(2012030853871829190,1993479636925403138,1993479637311279107,730),(2012030853871829191,1993479636925403138,1993479637311279107,731),(2012030853871829186,1993479636925403138,1993479637311279107,732),(2012030853871829187,1993479636925403138,1993479637311279107,733),(2012030853871829188,1993479636925403138,1993479637311279107,734),(2012030853871829189,1993479636925403138,1993479637311279107,735),(2012030853871829182,1993479636925403138,1993479637311279107,736),(2012030853871829183,1993479636925403138,1993479637311279107,737),(2012030853871829184,1993479636925403138,1993479637311279107,738),(2012030853871829185,1993479636925403138,1993479637311279107,739),(2012030853871829197,1993479636925403138,1993479637311279107,740),(2012030853871829198,1993479636925403138,1993479637311279107,741),(2012030853871829199,1993479636925403138,1993479637311279107,742),(2012030853871829200,1993479636925403138,1993479637311279107,743),(2046477012460679170,1993479636925403138,1993479637311279107,744),(2046477012460679171,1993479636925403138,1993479637311279107,745),(2046477012460679172,1993479636925403138,1993479637311279107,746),(2046477012460679173,1993479636925403138,1993479637311279107,747),(2046477012460679174,1993479636925403138,1993479637311279107,748),(2046477012460679175,1993479636925403138,1993479637311279107,749),(2046477012460679176,1993479636925403138,1993479637311279107,750),(2046477012460679177,1993479636925403138,1993479637311279107,751),(2046477012460679178,1993479636925403138,1993479637311279107,752),(2046477012460679179,1993479636925403138,1993479637311279107,753),(2012030853871829201,1993479636925403138,1993479637311279107,754),(2012030853871829202,1993479636925403138,1993479637311279107,755),(2012030853871829211,1993479636925403138,1993479637311279107,756),(2012030853871829214,1993479636925403138,1993479637311279107,757),(2012030853871829217,1993479636925403138,1993479637311279107,758),(2012030853871829203,1993479636925403138,1993479637311279107,759),(2012030853871829204,1993479636925403138,1993479637311279107,760),(2012030853871829205,1993479636925403138,1993479637311279107,761),(2012030853871829206,1993479636925403138,1993479637311279107,762),(2012030853871829207,1993479636925403138,1993479637311279107,763),(2012030853871829208,1993479636925403138,1993479637311279107,764),(2012030853871829209,1993479636925403138,1993479637311279107,765),(2012030853871829210,1993479636925403138,1993479637311279107,766),(2012030853871829212,1993479636925403138,1993479637311279107,767),(2012030853871829213,1993479636925403138,1993479637311279107,768),(2012030853871829215,1993479636925403138,1993479637311279107,769),(2012030853871829216,1993479636925403138,1993479637311279107,770),(2012030853871829218,1993479636925403138,1993479637311279107,771),(2012030853871829219,1993479636925403138,1993479637311279107,772),(2012030853871829220,1993479636925403138,1993479637311279107,773),(2012030853871829221,1993479636925403138,1993479637311279107,774),(2047520831801663724,1993479636925403138,1993479637311279107,775),(2047520831801663725,1993479636925403138,1993479637311279107,776),(2012030853871829180,1993479636925403138,1993479637311279107,3000000000000000422),(2012030853871829181,1993479636925403138,1993479637311279107,3000000000000000423),(2047520831801663490,1993479636925403138,1993479637311279107,3000000000000000424),(2047520831801663491,1993479636925403138,1993479637311279107,3000000000000000425),(2046512244757876738,1993479636925403138,1993479637311279107,3000000000000000434),(2046512244757876739,1993479636925403138,1993479637311279107,3000000000000000435),(2046512244757876740,1993479636925403138,1993479637311279107,3000000000000000436),(2046512244757876741,1993479636925403138,1993479637311279107,3000000000000000437),(2046512244757876742,1993479636925403138,1993479637311279107,3000000000000000438),(2046512244757876743,1993479636925403138,1993479637311279107,3000000000000000439),(2046512244757876744,1993479636925403138,1993479637311279107,3000000000000000440),(2046516951681298434,1993479636925403138,1993479637311279107,3000000000000000441),(2046516951681298435,1993479636925403138,1993479637311279107,3000000000000000442),(2046516951681298436,1993479636925403138,1993479637311279107,3000000000000000443),(2046516951681298437,1993479636925403138,1993479637311279107,3000000000000000444),(2046516951681298438,1993479636925403138,1993479637311279107,3000000000000000445),(2046516951681298439,1993479636925403138,1993479637311279107,3000000000000000446),(2046516951681298440,1993479636925403138,1993479637311279107,3000000000000000447),(2047520831801663492,1993479636925403138,1993479637311279107,3000000000000000448),(2047520831801663493,1993479636925403138,1993479637311279107,3000000000000000449),(2047520831801663494,1993479636925403138,1993479637311279107,3000000000000000450),(2047520831801663495,1993479636925403138,1993479637311279107,3000000000000000451),(2047520831801663496,1993479636925403138,1993479637311279107,3000000000000000452),(2047520831801663497,1993479636925403138,1993479637311279107,3000000000000000453),(2047520831801663498,1993479636925403138,1993479637311279107,3000000000000000454),(2047520831801663499,1993479636925403138,1993479637311279107,3000000000000000455),(2047520831801663500,1993479636925403138,1993479637311279107,3000000000000000456),(2047520831801663501,1993479636925403138,1993479637311279107,3000000000000000457),(2047520831801663502,1993479636925403138,1993479637311279107,3000000000000000458),(2047520831801663503,1993479636925403138,1993479637311279107,3000000000000000459),(2047520831801663504,1993479636925403138,1993479637311279107,3000000000000000460),(2047520831801663505,1993479636925403138,1993479637311279107,3000000000000000461),(2047520831801663506,1993479636925403138,1993479637311279107,3000000000000000462),(2047520831801663507,1993479636925403138,1993479637311279107,3000000000000000463),(2047520831801663508,1993479636925403138,1993479637311279107,3000000000000000464),(2047520831801663509,1993479636925403138,1993479637311279107,3000000000000000466),(2047520831801663533,1993479636925403138,1993479637311279107,3000000000000000485),(2047520831801663534,1993479636925403138,1993479637311279107,3000000000000000486),(2047520831801663535,1993479636925403138,1993479637311279107,3000000000000000487),(2047520831801663536,1993479636925403138,1993479637311279107,3000000000000000488),(2047520831801663537,1993479636925403138,1993479637311279107,3000000000000000489),(2047520831801663538,1993479636925403138,1993479637311279107,3000000000000000490),(2047520831801663539,1993479636925403138,1993479637311279107,3000000000000000491),(2047520831801663540,1993479636925403138,1993479637311279107,3000000000000000492),(2047520831801663541,1993479636925403138,1993479637311279107,3000000000000000493),(2047520831801663542,1993479636925403138,1993479637311279107,3000000000000000494),(2047520831801663543,1993479636925403138,1993479637311279107,3000000000000000495),(2047520831801663544,1993479636925403138,1993479637311279107,3000000000000000496),(2047520831801663545,1993479636925403138,1993479637311279107,3000000000000000497),(2047520831801663546,1993479636925403138,1993479637311279107,3000000000000000551),(2047520831801663547,1993479636925403138,1993479637311279107,3000000000000000552),(2047520831801663548,1993479636925403138,1993479637311279107,3000000000000000553),(2047520831801663549,1993479636925403138,1993479637311279107,3000000000000000554),(2047520831801663550,1993479636925403138,1993479637311279107,3000000000000000555),(2047520831801663551,1993479636925403138,1993479637311279107,3000000000000000556),(2047520831801663552,1993479636925403138,1993479637311279107,3000000000000000557),(2047520831801663553,1993479636925403138,1993479637311279107,3000000000000000558),(2047520831801663554,1993479636925403138,1993479637311279107,3000000000000000559),(2047520831801663555,1993479636925403138,1993479637311279107,3000000000000000560),(2047520831801663556,1993479636925403138,1993479637311279107,3000000000000000561),(2047520831801663557,1993479636925403138,1993479637311279107,3000000000000000562),(2047520831801663558,1993479636925403138,1993479637311279107,3000000000000000563),(2047520831801663690,1993479636925403138,1993479637311279107,3000000000000000695),(2047520831801663691,1993479636925403138,1993479637311279107,3000000000000000696),(2047520831801663692,1993479636925403138,1993479637311279107,3000000000000000697),(2047520831801663693,1993479636925403138,1993479637311279107,3000000000000000698),(2047520831801663694,1993479636925403138,1993479637311279107,3000000000000000699),(2047520831801663695,1993479636925403138,1993479637311279107,3000000000000000700),(2047520831801663696,1993479636925403138,1993479637311279107,3000000000000000701),(2047520831801663697,1993479636925403138,1993479637311279107,3000000000000000702),(2047520831801663698,1993479636925403138,1993479637311279107,3000000000000000703),(2047520831801663699,1993479636925403138,1993479637311279107,3000000000000000711),(2047520831801663700,1993479636925403138,1993479637311279107,3000000000000000712),(2047520831801663701,1993479636925403138,1993479637311279107,3000000000000000713),(2047520831801663702,1993479636925403138,1993479637311279107,3000000000000000714),(2047520831801663703,1993479636925403138,1993479637311279107,3000000000000000715),(2047520831801663704,1993479636925403138,1993479637311279107,3000000000000000716),(2047520831801663705,1993479636925403138,1993479637311279107,3000000000000000717),(2047520831801663706,1993479636925403138,1993479637311279107,3000000000000000718),(2047520831801663707,1993479636925403138,1993479637311279107,3000000000000000719),(2047520831801663708,1993479636925403138,1993479637311279107,3000000000000000720),(2047520831801663709,1993479636925403138,1993479637311279107,3000000000000000721),(2047520831801663710,1993479636925403138,1993479637311279107,3000000000000000722),(2047520831801663711,1993479636925403138,1993479637311279107,3000000000000000723),(2047520831801663712,1993479636925403138,1993479637311279107,3000000000000000724),(2047520831801663713,1993479636925403138,1993479637311279107,3000000000000000725),(2047520831801663714,1993479636925403138,1993479637311279107,3000000000000000726),(2047520831801663715,1993479636925403138,1993479637311279107,3000000000000000727),(2047520831801663716,1993479636925403138,1993479637311279107,3000000000000000728),(2047520831801663717,1993479636925403138,1993479637311279107,3000000000000000729),(2047520831801663718,1993479636925403138,1993479637311279107,3000000000000000730),(2047520831801663719,1993479636925403138,1993479637311279107,3000000000000000731),(2047520831801663720,1993479636925403138,1993479637311279107,3000000000000000732),(2047520831801663721,1993479636925403138,1993479637311279107,3000000000000000733),(2047520831801663722,1993479636925403138,1993479637311279107,3000000000000000734),(120,1993479636925403138,1993479637311279112,1),(121,1993479636925403138,1993479637311279112,2),(122,1993479636925403138,1993479637311279112,3),(123,1993479636925403138,1993479637311279112,4),(124,1993479636925403138,1993479637311279112,5),(125,1993479636925403138,1993479637311279112,6),(2012030853871829135,1993479636925403138,1993479637311279112,695),(2012030853871829137,1993479636925403138,1993479637311279112,696),(127,1993479636925403138,1993479637311279113,1),(132,1993479636925403138,1993479637311279113,5),(136,1993479636925403138,1993479637311279113,6),(128,1993479636925403138,1993479637311279113,101),(129,1993479636925403138,1993479637311279113,102),(130,1993479636925403138,1993479637311279113,103),(131,1993479636925403138,1993479637311279113,105),(133,1993479636925403138,1993479637311279113,501),(134,1993479636925403138,1993479637311279113,502),(135,1993479636925403138,1993479637311279113,503),(2047520831801663559,1993479636925403140,1993479637311279116,3000000000000000564),(2047520831801663560,1993479636925403140,1993479637311279116,3000000000000000565),(2047520831801663561,1993479636925403140,1993479637311279116,3000000000000000566),(2047520831801663562,1993479636925403140,1993479637311279116,3000000000000000567),(2047520831801663563,1993479636925403140,1993479637311279116,3000000000000000568),(2047520831801663564,1993479636925403140,1993479637311279116,3000000000000000569),(2047520831801663565,1993479636925403140,1993479637311279116,3000000000000000570),(2047520831801663566,1993479636925403140,1993479637311279116,3000000000000000571),(2047520831801663567,1993479636925403140,1993479637311279116,3000000000000000572),(2047520831801663568,1993479636925403140,1993479637311279116,3000000000000000573),(2047520831801663569,1993479636925403140,1993479637311279116,3000000000000000574),(2047520831801663570,1993479636925403140,1993479637311279116,3000000000000000575),(2047520831801663571,1993479636925403140,1993479637311279116,3000000000000000576),(2047520831801663572,1993479636925403140,1993479637311279116,3000000000000000577),(2047520831801663573,1993479636925403140,1993479637311279116,3000000000000000578),(2047520831801663574,1993479636925403140,1993479637311279116,3000000000000000579),(2047520831801663575,1993479636925403140,1993479637311279116,3000000000000000580),(2047520831801663576,1993479636925403140,1993479637311279116,3000000000000000581),(2047520831801663577,1993479636925403140,1993479637311279116,3000000000000000582),(2047520831801663578,1993479636925403140,1993479637311279116,3000000000000000583),(2047520831801663579,1993479636925403140,1993479637311279116,3000000000000000584),(2047520831801663580,1993479636925403140,1993479637311279116,3000000000000000585),(2047520831801663581,1993479636925403140,1993479637311279116,3000000000000000586),(2047520831801663582,1993479636925403140,1993479637311279116,3000000000000000587),(2047520831801663583,1993479636925403140,1993479637311279116,3000000000000000588),(2047520831801663584,1993479636925403140,1993479637311279116,3000000000000000589),(2047520831801663585,1993479636925403140,1993479637311279116,3000000000000000590),(2047520831801663586,1993479636925403140,1993479637311279116,3000000000000000591),(2047520831801663587,1993479636925403140,1993479637311279116,3000000000000000592),(2047520831801663588,1993479636925403140,1993479637311279116,3000000000000000593),(2047520831801663589,1993479636925403140,1993479637311279116,3000000000000000594),(2047520831801663590,1993479636925403140,1993479637311279116,3000000000000000595),(2047520831801663591,1993479636925403140,1993479637311279116,3000000000000000596),(2047520831801663592,1993479636925403140,1993479637311279116,3000000000000000597),(2047520831801663593,1993479636925403140,1993479637311279116,3000000000000000598),(2047520831801663594,1993479636925403140,1993479637311279116,3000000000000000599),(2047520831801663595,1993479636925403140,1993479637311279116,3000000000000000600),(2047520831801663596,1993479636925403140,1993479637311279116,3000000000000000601),(2047520831801663597,1993479636925403140,1993479637311279116,3000000000000000602),(2047520831801663598,1993479636925403140,1993479637311279116,3000000000000000603),(2047520831801663599,1993479636925403140,1993479637311279116,3000000000000000604),(2047520831801663600,1993479636925403140,1993479637311279116,3000000000000000605),(2047520831801663601,1993479636925403140,1993479637311279116,3000000000000000606),(2047520831801663602,1993479636925403140,1993479637311279116,3000000000000000607),(2047520831801663603,1993479636925403140,1993479637311279116,3000000000000000608),(2047520831801663604,1993479636925403140,1993479637311279116,3000000000000000609),(2047520831801663605,1993479636925403140,1993479637311279116,3000000000000000610),(2047520831801663606,1993479636925403140,1993479637311279116,3000000000000000611),(2047520831801663607,1993479636925403140,1993479637311279116,3000000000000000612),(2047520831801663608,1993479636925403140,1993479637311279116,3000000000000000613),(2047520831801663609,1993479636925403140,1993479637311279116,3000000000000000614),(2047520831801663610,1993479636925403140,1993479637311279116,3000000000000000615),(2047520831801663611,1993479636925403140,1993479637311279116,3000000000000000616),(2047520831801663612,1993479636925403140,1993479637311279116,3000000000000000617),(2047520831801663613,1993479636925403140,1993479637311279116,3000000000000000618),(2047520831801663614,1993479636925403140,1993479637311279116,3000000000000000619),(2047520831801663615,1993479636925403140,1993479637311279116,3000000000000000620),(2047520831801663616,1993479636925403140,1993479637311279116,3000000000000000621),(2047520831801663617,1993479636925403140,1993479637311279116,3000000000000000622),(2047520831801663618,1993479636925403140,1993479637311279116,3000000000000000623),(2047520831801663619,1993479636925403140,1993479637311279116,3000000000000000624),(2047520831801663620,1993479636925403140,1993479637311279116,3000000000000000625),(2047520831801663621,1993479636925403140,1993479637311279116,3000000000000000626),(2047520831801663622,1993479636925403140,1993479637311279116,3000000000000000627),(2047520831801663623,1993479636925403140,1993479637311279116,3000000000000000628),(2047520831801663624,1993479636925403140,1993479637311279116,3000000000000000629),(2047520831801663625,1993479636925403140,1993479637311279116,3000000000000000630),(2047520831801663626,1993479636925403140,1993479637311279116,3000000000000000631),(2047520831801663627,1993479636925403140,1993479637311279116,3000000000000000632),(2047520831801663628,1993479636925403140,1993479637311279116,3000000000000000633),(2047520831801663629,1993479636925403140,1993479637311279116,3000000000000000634),(2047520831801663630,1993479636925403140,1993479637311279116,3000000000000000635),(2047520831801663631,1993479636925403140,1993479637311279116,3000000000000000636),(2047520831801663632,1993479636925403140,1993479637311279116,3000000000000000637),(2047520831801663633,1993479636925403140,1993479637311279116,3000000000000000638),(2047520831801663634,1993479636925403140,1993479637311279116,3000000000000000639),(2047520831801663635,1993479636925403140,1993479637311279116,3000000000000000640),(2047520831801663636,1993479636925403140,1993479637311279116,3000000000000000641),(2047520831801663637,1993479636925403140,1993479637311279116,3000000000000000642),(2047520831801663638,1993479636925403140,1993479637311279116,3000000000000000643),(2047520831801663639,1993479636925403140,1993479637311279116,3000000000000000644),(2047520831801663640,1993479636925403140,1993479637311279116,3000000000000000645),(2047520831801663641,1993479636925403140,1993479637311279116,3000000000000000646),(2047520831801663642,1993479636925403140,1993479637311279116,3000000000000000647),(2047520831801663643,1993479636925403140,1993479637311279116,3000000000000000648),(2047520831801663644,1993479636925403140,1993479637311279116,3000000000000000649),(2047520831801663645,1993479636925403140,1993479637311279116,3000000000000000650),(2047520831801663646,1993479636925403140,1993479637311279116,3000000000000000651),(2047520831801663647,1993479636925403140,1993479637311279116,3000000000000000652),(2047520831801663648,1993479636925403140,1993479637311279116,3000000000000000653),(2047520831801663649,1993479636925403140,1993479637311279116,3000000000000000654),(2047520831801663650,1993479636925403140,1993479637311279116,3000000000000000655),(2047520831801663651,1993479636925403140,1993479637311279116,3000000000000000656),(2047520831801663652,1993479636925403140,1993479637311279116,3000000000000000657),(2047520831801663653,1993479636925403140,1993479637311279116,3000000000000000658),(2047520831801663654,1993479636925403140,1993479637311279116,3000000000000000659),(2047520831801663655,1993479636925403140,1993479637311279116,3000000000000000660),(2047520831801663656,1993479636925403140,1993479637311279116,3000000000000000661),(2047520831801663657,1993479636925403140,1993479637311279116,3000000000000000662),(2047520831801663658,1993479636925403140,1993479637311279116,3000000000000000663),(2047520831801663659,1993479636925403140,1993479637311279116,3000000000000000664),(2047520831801663660,1993479636925403140,1993479637311279116,3000000000000000665),(2047520831801663661,1993479636925403140,1993479637311279116,3000000000000000666),(2047520831801663662,1993479636925403140,1993479637311279116,3000000000000000667),(2047520831801663663,1993479636925403140,1993479637311279116,3000000000000000668),(2047520831801663664,1993479636925403140,1993479637311279116,3000000000000000669),(2047520831801663665,1993479636925403140,1993479637311279116,3000000000000000670),(2047520831801663666,1993479636925403140,1993479637311279116,3000000000000000671),(2047520831801663667,1993479636925403140,1993479637311279116,3000000000000000672),(2047520831801663668,1993479636925403140,1993479637311279116,3000000000000000673),(2047520831801663669,1993479636925403140,1993479637311279116,3000000000000000674),(2047520831801663670,1993479636925403140,1993479637311279116,3000000000000000675),(2047520831801663671,1993479636925403140,1993479637311279116,3000000000000000676),(2047520831801663672,1993479636925403140,1993479637311279116,3000000000000000677),(2047520831801663673,1993479636925403140,1993479637311279116,3000000000000000678),(2047520831801663674,1993479636925403140,1993479637311279116,3000000000000000679),(2047520831801663675,1993479636925403140,1993479637311279116,3000000000000000680),(2047520831801663676,1993479636925403140,1993479637311279116,3000000000000000681),(2047520831801663677,1993479636925403140,1993479637311279116,3000000000000000682),(2047520831801663678,1993479636925403140,1993479637311279116,3000000000000000683),(2047520831801663679,1993479636925403140,1993479637311279116,3000000000000000684),(2047520831801663680,1993479636925403140,1993479637311279116,3000000000000000685),(2047520831801663681,1993479636925403140,1993479637311279116,3000000000000000686),(2047520831801663682,1993479636925403140,1993479637311279116,3000000000000000687),(2047520831801663683,1993479636925403140,1993479637311279116,3000000000000000688),(2047520831801663684,1993479636925403140,1993479637311279116,3000000000000000689),(2047520831801663685,1993479636925403140,1993479637311279116,3000000000000000690),(2047520831801663686,1993479636925403140,1993479637311279116,3000000000000000691),(2047520831801663687,1993479636925403140,1993479637311279116,3000000000000000692),(2047520831801663688,1993479636925403140,1993479637311279116,3000000000000000693),(2047520831801663689,1993479636925403140,1993479637311279116,3000000000000000694);
/*!40000 ALTER TABLE `sys_role_menu` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_permission`
--

DROP TABLE IF EXISTS `sys_role_permission`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_permission` (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`,`permission_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色权限关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_permission`
--

LOCK TABLES `sys_role_permission` WRITE;
/*!40000 ALTER TABLE `sys_role_permission` DISABLE KEYS */;
INSERT INTO `sys_role_permission` VALUES (1993479637311279107,3000000000000000561),(1993479637311279107,3000000000000000562),(1993479637311279107,3000000000000000563),(1993479637311279107,3000000000000000564),(1993479637311279107,3000000000000000565),(1993479637311279107,3000000000000000591),(1993479637311279107,3000000000000000592),(1993479637311279107,3000000000000000593),(1993479637311279107,3000000000000000594),(1993479637311279107,3000000000000000595),(1993479637311279107,3000000000000000596),(1993479637311279107,3000000000000000597),(1993479637311279107,3000000000000000598),(1993479637311279107,3000000000000000599),(1993479637311279107,3000000000000000600),(1993479637311279107,3000000000000000601),(1993479637311279107,3000000000000000602),(1993479637311279107,3000000000000000603),(1993479637311279107,3000000000000000604),(1993479637311279107,3000000000000000605),(1993479637311279107,3000000000000000606),(1993479637311279107,3000000000000000607),(1993479637311279107,3000000000000000608),(1993479637311279107,3000000000000000609),(1993479637311279107,3000000000000000610),(1993479637311279107,3000000000000000611),(1993479637311279107,3000000000000000612),(1993479637311279107,3000000000000000613),(1993479637311279107,3000000000000000614),(1993479637311279107,3000000000000000615),(1993479637311279107,3000000000000000616),(1993479637311279107,3000000000000000617),(1993479637311279107,3000000000000000618),(1993479637311279107,3000000000000000619),(1993479637311279107,3000000000000000620),(1993479637311279107,3000000000000000621),(1993479637311279107,3000000000000000622),(1993479637311279107,3000000000000000623),(1993479637311279107,3000000000000000624),(1993479637311279107,3000000000000000625),(1993479637311279107,3000000000000000626),(1993479637311279107,3000000000000000627),(1993479637311279107,3000000000000000628),(1993479637311279107,3000000000000000629),(1993479637311279107,3000000000000000630),(1993479637311279107,3000000000000000631);
/*!40000 ALTER TABLE `sys_role_permission` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_role_position`
--

DROP TABLE IF EXISTS `sys_role_position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_role_position` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `position_id` bigint NOT NULL COMMENT '职位 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_role_id` (`role_id`) USING BTREE,
  KEY `idx_position_id` (`position_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='角色职位关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_role_position`
--

LOCK TABLES `sys_role_position` WRITE;
/*!40000 ALTER TABLE `sys_role_position` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_role_position` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_social_login`
--

DROP TABLE IF EXISTS `sys_social_login`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_social_login` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `platform` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台(WECHAT/DINGTALK)',
  `open_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OpenId',
  `union_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT 'UnionId',
  `bind_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_platform_openid` (`platform`,`open_id`) USING BTREE,
  KEY `idx_tenant_user` (`tenant_id`,`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='第三方登录绑定表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_social_login`
--

LOCK TABLES `sys_social_login` WRITE;
/*!40000 ALTER TABLE `sys_social_login` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_social_login` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_tenant`
--

DROP TABLE IF EXISTS `sys_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '描述',
  `status` tinyint DEFAULT '1' COMMENT '状态 (1:启用, 0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人账号/ID',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人账号/ID',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 (0:未删除, 1:已删除)',
  `tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `logo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci,
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID（可为空，用于自关联或扩展）',
  `tenant_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT 'MAIN_TENANT' COMMENT '租户类别：MAIN_TENANT-主租户，CUSTOMER_TENANT-客户租户，SUPPLIER_TENANT-供应商租户',
  `parent_tenant_id` bigint DEFAULT NULL COMMENT 'Parent tenant ID; NULL means main or top-level tenant',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `tenant_code` (`tenant_code`) USING BTREE,
  KEY `idx_parent_tenant_id` (`parent_tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1993479636925403141 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='租户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tenant`
--

LOCK TABLES `sys_tenant` WRITE;
/*!40000 ALTER TABLE `sys_tenant` DISABLE KEYS */;
INSERT INTO `sys_tenant` VALUES (1993479636925403138,'Forgex','默认租户',1,'2025-11-26 08:39:17','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,'default','/files/ed86214d98ab467498d2cdf3c4b10f20.png',NULL,'MAIN_TENANT',NULL),(1993479636925403140,'富士康科技集团','供应商主数据自动生成',1,'2026-04-30 13:41:06','1993479637244170242','2026-04-30 13:41:06','1993479637244170242',0,'sup_supp001',NULL,NULL,'SUPPLIER_TENANT',NULL);
/*!40000 ALTER TABLE `sys_tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_tenant_ignore`
--

DROP TABLE IF EXISTS `sys_tenant_ignore`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant_ignore` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作用域：TABLE/SERVICE/MAPPER',
  `matcher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配内容：表名/全限定类名/全限定类名#方法名',
  `enabled` tinyint DEFAULT '1' COMMENT '是否启用：1启用 0禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='租户隔离跳过配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tenant_ignore`
--

LOCK TABLES `sys_tenant_ignore` WRITE;
/*!40000 ALTER TABLE `sys_tenant_ignore` DISABLE KEYS */;
INSERT INTO `sys_tenant_ignore` VALUES (1,'TABLE','sys_user',1,'用户表不带租户字段','2025-11-21 14:52:02','2025-11-21 14:52:02',0,NULL),(2,'TABLE','sys_tenant',1,'租户表不带租户字段','2025-11-21 14:52:02','2025-11-21 14:52:02',0,NULL),(3,'TABLE','sys_user_tenant',1,'用户-租户关联表不带租户字段','2025-11-21 14:52:02','2025-11-21 14:52:02',0,NULL),(4,'TABLE','sys_config',1,'配置表跨租户','2025-11-22 10:12:39','2025-11-22 10:12:39',0,NULL),(5,'TABLE','sys_config',1,'系统配置表使用公共库，跳过租户隔离','2025-11-24 10:18:37','2025-11-24 10:18:37',0,NULL),(6,'TABLE','sys_tenant_ignore',1,'忽略租户隔离跳过配置表','2025-11-24 10:31:35','2025-11-24 10:31:35',0,NULL),(7,'TABLE','fx_i18n_message',1,'返回消息国际化配置表跳过租户隔离','2026-01-16 10:38:53','2026-01-16 10:38:53',0,NULL),(8,'TABLE','fx_table_config',1,'通用表格配置主表跳过租户隔离','2026-01-16 10:38:53','2026-01-16 10:38:53',0,NULL),(9,'TABLE','fx_table_column_config',1,'通用表格列配置子表跳过租户隔离','2026-01-16 10:38:53','2026-01-16 10:38:53',0,NULL),(10,'TABLE','fx_i18n_language_type',1,'多语言配置','2026-01-18 10:50:44','2026-01-18 10:50:44',0,NULL),(11,'TABLE','sys_notice',1,'系统通知表由业务条件处理公共/租户范围','2026-05-10 21:13:26','2026-05-10 21:13:26',0,'codex'),(12,'TABLE','sys_notice_attachment',1,'系统通知附件表由通知范围控制可见性','2026-05-10 21:13:26','2026-05-10 21:13:26',0,'codex'),(13,'TABLE','sys_notice_user_record',1,'系统通知弹窗记录按当前用户和租户显式过滤','2026-05-10 21:13:26','2026-05-10 21:13:26',0,'codex'),(14,'TABLE','sys_homepage_component_category',1,'首页组件分类配置使用公共租户数据加载','2026-05-15 18:39:01','2026-05-15 18:39:01',0,'codex'),(15,'TABLE','sys_homepage_component_config',1,'首页组件公共/租户配置由业务层显式控制范围','2026-05-15 18:39:01','2026-05-15 18:39:01',0,'codex'),(16,'TABLE','sys_homepage_component_preference',1,'首页组件个人偏好表按用户和租户保存快照','2026-05-15 18:39:01','2026-05-15 18:39:01',0,'codex');
/*!40000 ALTER TABLE `sys_tenant_ignore` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_tenant_menu_copy_rule`
--

DROP TABLE IF EXISTS `sys_tenant_menu_copy_rule`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant_menu_copy_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_type` varchar(50) NOT NULL COMMENT '??????',
  `perm_prefix` varchar(255) NOT NULL COMMENT '?????????',
  `enabled` tinyint NOT NULL DEFAULT '1' COMMENT '?????0=???1=??',
  `remark` varchar(500) DEFAULT NULL COMMENT '??',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(50) DEFAULT NULL COMMENT '???',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(50) DEFAULT NULL COMMENT '???',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '?????0=????1=???',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_tenant_menu_copy_rule` (`tenant_type`,`perm_prefix`,`deleted`),
  KEY `idx_tenant_menu_copy_rule_type` (`tenant_type`,`enabled`,`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='?????????';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tenant_menu_copy_rule`
--

LOCK TABLES `sys_tenant_menu_copy_rule` WRITE;
/*!40000 ALTER TABLE `sys_tenant_menu_copy_rule` DISABLE KEYS */;
INSERT INTO `sys_tenant_menu_copy_rule` VALUES (1,'SUPPLIER_TENANT','sys:tenant',1,'供应商租户不复制租户管理相关菜单','2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:27:41','20260512_supplier_tenant_login_fix',0),(2,'CUSTOMER_TENANT','sys:tenant',1,'客户租户不复制租户管理相关菜单','2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:27:41','20260512_supplier_tenant_login_fix',0),(3,'PARTNER_TENANT','sys:tenant',1,'伙伴租户不复制租户管理相关菜单','2026-05-12 23:16:01','20260512_supplier_tenant_login_fix','2026-05-12 23:27:41','20260512_supplier_tenant_login_fix',0);
/*!40000 ALTER TABLE `sys_tenant_menu_copy_rule` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_tenant_message_whitelist`
--

DROP TABLE IF EXISTS `sys_tenant_message_whitelist`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_tenant_message_whitelist` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `sender_tenant_id` bigint NOT NULL COMMENT '发送方租户ID',
  `receiver_tenant_id` bigint NOT NULL COMMENT '接收方租户ID',
  `enabled` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用（1-启用，0-禁用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_sender_receiver` (`sender_tenant_id`,`receiver_tenant_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_enabled` (`enabled`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='租户消息白名单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_tenant_message_whitelist`
--

LOCK TABLES `sys_tenant_message_whitelist` WRITE;
/*!40000 ALTER TABLE `sys_tenant_message_whitelist` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_tenant_message_whitelist` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码 (加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '1' COMMENT '状态 (1:启用, 0:禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `gender` tinyint DEFAULT NULL COMMENT '性别：0=未知，1=男，2=女',
  `entry_date` date DEFAULT NULL COMMENT '入职时间',
  `department_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `position_id` bigint DEFAULT NULL COMMENT '职位ID',
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '最后登录IP',
  `last_login_region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '最后登录地区',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '用户头像URL',
  `user_source` tinyint NOT NULL DEFAULT '1' COMMENT '用户来源:1本站新增,2本站导入,3第三方同步,4自行注册',
  `employee_id` bigint DEFAULT NULL COMMENT '关联员工ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_account` (`account`) USING BTREE,
  KEY `idx_username` (`username`) USING BTREE,
  KEY `idx_department_id` (`department_id`) USING BTREE,
  KEY `idx_position_id` (`position_id`) USING BTREE,
  KEY `idx_user_source` (`user_source`),
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1993479637244170255 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1993479637244170242,'admin','admin','$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq','admin@local.com',NULL,1,'2025-11-26 08:39:17','1993479637244170242','2026-05-16 16:18:09','1993479637244170242',0,NULL,1,'2026-04-04',1,11,'0:0:0:0:0:0:0:1','本地','2026-05-16 16:18:10','http://192.168.121.1:9000/api/sys/files/f67a6d20025643c6984ba7ea1f71ff28.jpg',1,NULL),(1993479637244170249,'test001','测试用户1','$2a$10$TN2WOn63RiPL.8iFvPBRZOxprURcDDWzMKYRcYEG4pu.qwVbxwUI6','test001@forgex.com','13800138001',1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,NULL,1,'2026-01-01',7,7,NULL,NULL,NULL,NULL,1,NULL),(1993479637244170250,'test002','测试用户2','$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq','test002@forgex.com','13800138002',1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,NULL,2,'2026-01-02',8,8,NULL,NULL,NULL,NULL,1,NULL),(1993479637244170251,'test003','测试用户3','$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq','test003@forgex.com','13800138003',1,'2026-01-08 10:58:58','1993479637244170242','2026-05-06 17:29:21','1993479637244170242',0,NULL,1,'2026-01-03',9,9,NULL,NULL,NULL,NULL,1,NULL),(1993479637244170252,'test','test用户','$2a$10$U4qFzeT00nwD4BcdhQQJHeF7cF81bP0VodQVNBegEeHkFe20t2VBe','coderr_nai@163.com','15866912378',1,'2026-04-04 11:22:02','1993479637244170242','2026-04-04 14:29:17','1993479637244170242',1,NULL,1,'2026-04-04',1,11,NULL,NULL,NULL,NULL,1,NULL),(1993479637244170253,'smy','孙明岩','$2a$10$KPeYoW4LXUO7Zmpo/LLZWuujeXwMwtmZllcvbfShIoJrvmRKrF4oK','','',1,'2026-04-10 16:45:45','1993479637244170242','2026-04-22 21:55:42','1993479637244170242',0,1993479636925403138,1,'2026-04-10',1,12,'0:0:0:0:0:0:0:1','本地','2026-04-22 21:55:43',NULL,1,NULL),(1993479637244170254,'admin_supsupp001_3140','系统管理员','$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq','admin_supsupp001_3140@tenant.local',NULL,1,'2026-05-12 23:18:18','20260512_supplier_tenant_login_fix','2026-05-12 23:30:11','20260512_supplier_tenant_login_fix',0,1993479636925403140,NULL,NULL,NULL,NULL,'0:0:0:0:0:0:0:1','本地','2026-05-12 23:30:12',NULL,1,NULL);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_c_menu_favorite`
--

DROP TABLE IF EXISTS `sys_user_c_menu_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_c_menu_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `c_menu_id` bigint NOT NULL COMMENT 'C 端菜单 ID',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_c_menu` (`tenant_id`,`user_id`,`c_menu_id`) USING BTREE,
  KEY `idx_user_id` (`user_id`) USING BTREE,
  KEY `idx_c_menu_id` (`c_menu_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户 C 端菜单收藏表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_c_menu_favorite`
--

LOCK TABLES `sys_user_c_menu_favorite` WRITE;
/*!40000 ALTER TABLE `sys_user_c_menu_favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `sys_user_c_menu_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_menu_common`
--

DROP TABLE IF EXISTS `sys_user_menu_common`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_menu_common` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端完整菜单路径',
  `menu_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单标题快照',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块名称快照',
  `menu_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `visit_count` int NOT NULL DEFAULT '1' COMMENT '访问次数',
  `last_visited_at` datetime DEFAULT NULL COMMENT '最近访问时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_common_menu_path` (`tenant_id`,`user_id`,`menu_path`) USING BTREE,
  KEY `idx_user_common_menu_user` (`user_id`) USING BTREE,
  KEY `idx_user_common_menu_visit` (`tenant_id`,`user_id`,`visit_count`,`last_visited_at`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2055572806103883779 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户常用菜单访问统计表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_menu_common`
--

LOCK TABLES `sys_user_menu_common` WRITE;
/*!40000 ALTER TABLE `sys_user_menu_common` DISABLE KEYS */;
INSERT INTO `sys_user_menu_common` VALUES (2043349513253105666,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard','系统管理主页','sys','系统管理','DashboardOutlined',427,'2026-05-16 16:31:24','2026-04-12 23:24:22','2026-04-12 23:24:22'),(2043352022986207233,1993479636925403138,1993479637244170242,'/workspace/basic/basicInfo/placeholder','基础信息占位页','basic','基础数据','FormOutlined',64,'2026-04-29 19:02:28','2026-04-12 23:34:21','2026-04-12 23:34:21'),(2043352046373646338,1993479636925403138,1993479637244170242,'/workspace/sys/organization/user','用户管理','sys','系统管理','UserOutlined',174,'2026-05-15 20:29:38','2026-04-12 23:34:26','2026-04-12 23:34:26'),(2043494047891017730,1993479636925403138,1993479637244170242,'/workspace/sys/authorization/menu','菜单管理','sys','系统管理','MenuOutlined',41,'2026-05-10 20:50:22','2026-04-13 08:58:42','2026-04-13 08:58:42'),(2043494256326955009,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/online','在线用户','sys','系统管理','UsergroupAddOutlined',33,'2026-05-10 20:01:28','2026-04-13 08:59:32','2026-04-13 08:59:32'),(2043502753055723521,1993479636925403138,1993479637244170242,'/workspace/approval/dashboard','审批工作台','approval','审批管理','DashboardOutlined',108,'2026-05-15 20:43:11','2026-04-13 09:33:17','2026-04-13 09:33:17'),(2046951096131276802,1993479636925403138,1993479637244170253,'/workspace/approval/my/pending','我的待办','approval','审批管理','ClockCircleOutlined',3,'2026-04-22 23:29:29','2026-04-22 21:55:46','2026-04-22 21:55:46'),(2046951442647896066,1993479636925403138,1993479637244170253,'/workspace/sys/dashboard','系统管理主页','sys','系统管理','DashboardOutlined',2,'2026-04-22 21:57:15','2026-04-22 21:57:09','2026-04-22 21:57:09'),(2046951458426867714,1993479636925403138,1993479637244170253,'/workspace/approval/dashboard','审批工作台','approval','审批管理','DashboardOutlined',3,'2026-04-22 23:29:14','2026-04-22 21:57:13','2026-04-22 21:57:13'),(2046958078586277890,1993479636925403138,1993479637244170253,'/workspace/approval/execution/start','发起审批','approval','审批管理','PlayCircleOutlined',1,'2026-04-22 22:23:31','2026-04-22 22:23:31','2026-04-22 22:23:31'),(2046958112874713090,1993479636925403138,1993479637244170253,'/workspace/approval/my/processed','我已处理','approval','审批管理','CheckCircleOutlined',11,'2026-04-22 23:30:13','2026-04-22 22:23:39','2026-04-22 22:23:39'),(2046974804170395649,1993479636925403138,1993479637244170253,'/workspace/approval/my/initiated','我发起的','approval','审批管理','SendOutlined',1,'2026-04-22 23:29:59','2026-04-22 23:29:59','2026-04-22 23:29:59'),(2054222628616499202,1993479636925403140,1993479637244170254,'/workspace/sys/dashboard','系统管理主页','sys','系统管理','DashboardOutlined',2,'2026-05-12 23:30:28','2026-05-12 23:30:15','2026-05-12 23:30:15'),(2054222712720683009,1993479636925403140,1993479637244170254,'/workspace/sys/authorization/role','角色管理','sys','系统管理','TeamOutlined',2,'2026-05-12 23:30:39','2026-05-12 23:30:35','2026-05-12 23:30:35'),(2054222847001325569,1993479636925403140,1993479637244170254,'/workspace/sys/onlineDev/codegenDatasource','代码生成数据源','sys','系统管理','DatabaseOutlined',1,'2026-05-12 23:31:07','2026-05-12 23:31:07','2026-05-12 23:31:07'),(2054222851136909313,1993479636925403140,1993479637244170254,'/workspace/sys/onlineDev/codegen','代码生成','sys','系统管理','CodeOutlined',1,'2026-05-12 23:31:08','2026-05-12 23:31:08','2026-05-12 23:31:08'),(2054222857008934913,1993479636925403140,1993479637244170254,'/workspace/sys/file','文件管理','sys','系统管理','FolderOpenOutlined',1,'2026-05-12 23:31:09','2026-05-12 23:31:09','2026-05-12 23:31:09'),(2054222882577412098,1993479636925403140,1993479637244170254,'/workspace/sys/maintenance/notice','系统通知','sys','系统管理','NotificationOutlined',1,'2026-05-12 23:31:15','2026-05-12 23:31:15','2026-05-12 23:31:15');
/*!40000 ALTER TABLE `sys_user_menu_common` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_menu_favorite`
--

DROP TABLE IF EXISTS `sys_user_menu_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_menu_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端完整菜单路径',
  `menu_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单标题快照',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '模块名称快照',
  `menu_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '菜单图标',
  `order_num` int DEFAULT NULL COMMENT '收藏排序号',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_favorite_menu_path` (`tenant_id`,`user_id`,`menu_path`) USING BTREE,
  KEY `idx_user_favorite_menu_user` (`user_id`) USING BTREE,
  KEY `idx_user_favorite_menu_order` (`tenant_id`,`user_id`,`order_num`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2043498524798832642 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户收藏菜单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_menu_favorite`
--

LOCK TABLES `sys_user_menu_favorite` WRITE;
/*!40000 ALTER TABLE `sys_user_menu_favorite` DISABLE KEYS */;
INSERT INTO `sys_user_menu_favorite` VALUES (2043498524798832641,1993479636925403138,1993479637244170242,'/workspace/approval/my/pending','我的待办','approval','审批管理','ClockCircleOutlined',1,'2026-04-13 09:16:29');
/*!40000 ALTER TABLE `sys_user_menu_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_menu_open_count`
--

DROP TABLE IF EXISTS `sys_user_menu_open_count`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_menu_open_count` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) NOT NULL COMMENT '菜单完整路由路径',
  `menu_title` varchar(128) DEFAULT NULL COMMENT '菜单标题',
  `module_code` varchar(64) DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(128) DEFAULT NULL COMMENT '模块名称',
  `menu_icon` varchar(128) DEFAULT NULL COMMENT '菜单图标',
  `open_count` int NOT NULL DEFAULT '0' COMMENT '打开次数',
  `first_open_at` datetime DEFAULT NULL COMMENT '首次打开时间',
  `last_open_at` datetime DEFAULT NULL COMMENT '最近打开时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_menu_open_count_user_path` (`tenant_id`,`user_id`,`menu_path`,`deleted`),
  KEY `idx_sys_user_menu_open_count_module` (`tenant_id`,`user_id`,`module_code`,`deleted`),
  KEY `idx_sys_user_menu_open_count_last_open` (`last_open_at`)
) ENGINE=InnoDB AUTO_INCREMENT=58 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户菜单打开次数表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_menu_open_count`
--

LOCK TABLES `sys_user_menu_open_count` WRITE;
/*!40000 ALTER TABLE `sys_user_menu_open_count` DISABLE KEYS */;
INSERT INTO `sys_user_menu_open_count` VALUES (1,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard','系统管理主页','sys','系统管理','DashboardOutlined',236,'2026-04-29 22:31:28','2026-05-16 16:31:24','2026-04-29 22:31:28','1993479637244170242','2026-04-29 22:31:28','1993479637244170242',0),(2,1993479636925403138,1993479637244170242,'/workspace/sys/organization/user','用户管理','sys','系统管理','UserOutlined',94,'2026-04-29 22:32:22','2026-05-15 20:29:38','2026-04-29 22:32:22','1993479637244170242','2026-04-29 22:32:22','1993479637244170242',0),(3,1993479636925403138,1993479637244170242,'/workspace/sys/organization/position','职位管理','sys','系统管理','IdcardOutlined',27,'2026-04-30 00:14:21','2026-05-10 23:22:15','2026-04-30 00:14:21','1993479637244170242','2026-04-30 00:14:21','1993479637244170242',0),(4,1993479636925403138,1993479637244170242,'/workspace/sys/organization/department','部门管理','sys','系统管理','ApartmentOutlined',22,'2026-04-30 00:14:22','2026-05-15 20:29:44','2026-04-30 00:14:22','1993479637244170242','2026-04-30 00:14:22','1993479637244170242',0),(5,1993479636925403138,1993479637244170242,'/workspace/sys/authorization/tenant','租户管理','sys','系统管理','TeamOutlined',18,'2026-04-30 00:14:37','2026-05-10 20:43:22','2026-04-30 00:14:37','1993479637244170242','2026-04-30 00:14:37','1993479637244170242',0),(6,1993479636925403138,1993479637244170242,'/workspace/sys/organization/inviteCode','邀请码管理','sys','系统管理','KeyOutlined',9,'2026-04-30 00:14:52','2026-05-10 20:43:19','2026-04-30 00:14:52','1993479637244170242','2026-04-30 00:14:52','1993479637244170242',0),(7,1993479636925403138,1993479637244170242,'/workspace/sys/authorization/role','角色管理','sys','系统管理','TeamOutlined',19,'2026-04-30 00:15:00','2026-05-12 20:00:25','2026-04-30 00:15:00','1993479637244170242','2026-04-30 00:15:00','1993479637244170242',0),(8,1993479636925403138,1993479637244170242,'/workspace/sys/authorization/menu','菜单管理','sys','系统管理','MenuOutlined',15,'2026-04-30 00:15:03','2026-05-10 20:50:22','2026-04-30 00:15:03','1993479637244170242','2026-04-30 00:15:03','1993479637244170242',0),(9,1993479636925403138,1993479637244170242,'/workspace/sys/module','模块管理','sys','系统管理','AppstoreOutlined',12,'2026-04-30 00:15:07','2026-05-15 21:16:42','2026-04-30 00:15:07','1993479637244170242','2026-04-30 00:15:07','1993479637244170242',0),(10,1993479636925403138,1993479637244170242,'/workspace/sys/excelConfig/excelImportConfig','导入配置','sys','系统管理','FileExcelOutlined',17,'2026-04-30 00:15:09','2026-05-11 09:56:03','2026-04-30 00:15:09','1993479637244170242','2026-04-30 00:15:09','1993479637244170242',0),(11,1993479636925403138,1993479637244170242,'/workspace/sys/excelConfig/excelExportConfig','导出配置','sys','系统管理','FileExcelOutlined',9,'2026-04-30 00:15:10','2026-05-10 20:02:04','2026-04-30 00:15:10','1993479637244170242','2026-04-30 00:15:10','1993479637244170242',0),(12,1993479636925403138,1993479637244170242,'/workspace/sys/dict','字典管理','sys','系统管理','BookOutlined',10,'2026-04-30 00:15:12','2026-05-09 17:55:26','2026-04-30 00:15:12','1993479637244170242','2026-04-30 00:15:12','1993479637244170242',0),(13,1993479636925403138,1993479637244170242,'/workspace/sys/pageTableConfig/userTableConfig','用户列设置','sys','系统管理','ColumnWidthOutlined',2,'2026-04-30 00:15:15','2026-05-09 17:55:41','2026-04-30 00:15:15','1993479637244170242','2026-04-30 00:15:15','1993479637244170242',0),(14,1993479636925403138,1993479637244170242,'/workspace/sys/pageTableConfig/tableConfig','테이블 설정','sys','시스템 관리','TableOutlined',6,'2026-04-30 00:15:17','2026-05-09 17:56:34','2026-04-30 00:15:17','1993479637244170242','2026-04-30 00:15:17','1993479637244170242',0),(15,1993479636925403138,1993479637244170242,'/workspace/sys/loginLog','登录日志','sys','系统管理','FileTextOutlined',14,'2026-04-30 00:15:19','2026-05-15 16:42:33','2026-04-30 00:15:19','1993479637244170242','2026-04-30 00:15:19','1993479637244170242',0),(16,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/online','在线用户','sys','系统管理','UsergroupAddOutlined',7,'2026-04-30 00:15:21','2026-05-10 20:01:28','2026-04-30 00:15:21','1993479637244170242','2026-04-30 00:15:21','1993479637244170242',0),(17,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/messageTemplate','消息模板','sys','系统管理','MailOutlined',6,'2026-04-30 00:15:22','2026-05-10 20:01:44','2026-04-30 00:15:22','1993479637244170242','2026-04-30 00:15:22','1993479637244170242',0),(18,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/tenantMessageWhitelist','租户消息白名单','sys','系统管理','SafetyCertificateOutlined',7,'2026-04-30 00:15:23','2026-05-07 09:36:13','2026-04-30 00:15:23','1993479637244170242','2026-04-30 00:15:23','1993479637244170242',0),(19,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/operationLog','操作日志','sys','系统管理','FileTextOutlined',8,'2026-04-30 00:15:25','2026-05-11 11:17:46','2026-04-30 00:15:25','1993479637244170242','2026-04-30 00:15:25','1993479637244170242',0),(20,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/config','系统配置','sys','系统管理','SettingOutlined',103,'2026-04-30 00:15:27','2026-05-15 21:04:56','2026-04-30 00:15:27','1993479637244170242','2026-04-30 00:15:27','1993479637244170242',0),(21,1993479636925403138,1993479637244170242,'/workspace/sys/i18nConfig/i18nMessage','多语言消息','sys','系统管理','MessageOutlined',9,'2026-04-30 00:15:29','2026-05-07 10:36:54','2026-04-30 00:15:29','1993479637244170242','2026-04-30 00:15:29','1993479637244170242',0),(22,1993479636925403138,1993479637244170242,'/workspace/sys/i18nConfig/i18nLanguageType','语言配置','sys','系统管理','GlobalOutlined',2,'2026-04-30 00:15:31','2026-05-01 15:27:52','2026-04-30 00:15:31','1993479637244170242','2026-04-30 00:15:31','1993479637244170242',0),(23,1993479636925403138,1993479637244170242,'/workspace/sys/file','文件管理','sys','系统管理','FolderOpenOutlined',5,'2026-04-30 00:15:37','2026-05-06 19:52:52','2026-04-30 00:15:37','1993479637244170242','2026-04-30 00:15:37','1993479637244170242',0),(24,1993479636925403138,1993479637244170242,'/workspace/sys/onlineDev/codegenDatasource','代码生成数据源','sys','系统管理','DatabaseOutlined',5,'2026-04-30 00:15:46','2026-05-06 19:52:23','2026-04-30 00:15:46','1993479637244170242','2026-04-30 00:15:46','1993479637244170242',0),(25,1993479636925403138,1993479637244170242,'/workspace/sys/onlineDev/codegen','代码生成','sys','系统管理','CodeOutlined',1,'2026-04-30 00:15:48','2026-04-30 00:15:48','2026-04-30 00:15:48','1993479637244170242','2026-04-30 00:15:48','1993479637244170242',0),(26,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/androidVersion','安卓版本管理','sys','系统管理','AndroidOutlined',12,'2026-05-07 09:22:43','2026-05-12 19:37:06','2026-05-07 09:22:43','1993479637244170242','2026-05-07 09:22:43','1993479637244170242',0),(27,1993479636925403138,1993479637244170242,'/workspace/sys/maintenance/notice','系统通知','sys','系统管理','NotificationOutlined',3,'2026-05-10 22:35:47','2026-05-11 09:58:45','2026-05-10 22:35:47','1993479637244170242','2026-05-10 22:35:47','1993479637244170242',0),(28,1993479636925403140,1993479637244170254,'/workspace/sys/dashboard','系统管理主页','sys','系统管理','DashboardOutlined',2,'2026-05-12 23:30:15','2026-05-12 23:30:28','2026-05-12 23:30:15','1993479637244170254','2026-05-12 23:30:15','1993479637244170254',0),(29,1993479636925403140,1993479637244170254,'/workspace/sys/organization/user','用户管理','sys','系统管理','UserOutlined',1,'2026-05-12 23:30:22','2026-05-12 23:30:22','2026-05-12 23:30:22','1993479637244170254','2026-05-12 23:30:22','1993479637244170254',0),(30,1993479636925403140,1993479637244170254,'/workspace/sys/organization/department','部门管理','sys','系统管理','ApartmentOutlined',1,'2026-05-12 23:30:30','2026-05-12 23:30:30','2026-05-12 23:30:30','1993479637244170254','2026-05-12 23:30:30','1993479637244170254',0),(31,1993479636925403140,1993479637244170254,'/workspace/sys/organization/position','职位管理','sys','系统管理','IdcardOutlined',1,'2026-05-12 23:30:31','2026-05-12 23:30:31','2026-05-12 23:30:31','1993479637244170254','2026-05-12 23:30:31','1993479637244170254',0),(32,1993479636925403140,1993479637244170254,'/workspace/sys/organization/inviteCode','邀请码管理','sys','系统管理','KeyOutlined',1,'2026-05-12 23:30:32','2026-05-12 23:30:32','2026-05-12 23:30:32','1993479637244170254','2026-05-12 23:30:32','1993479637244170254',0),(33,1993479636925403140,1993479637244170254,'/workspace/sys/authorization/role','角色管理','sys','系统管理','TeamOutlined',2,'2026-05-12 23:30:35','2026-05-12 23:30:39','2026-05-12 23:30:35','1993479637244170254','2026-05-12 23:30:35','1993479637244170254',0),(34,1993479636925403140,1993479637244170254,'/workspace/sys/authorization/menu','菜单管理','sys','系统管理','MenuOutlined',1,'2026-05-12 23:30:36','2026-05-12 23:30:36','2026-05-12 23:30:36','1993479637244170254','2026-05-12 23:30:36','1993479637244170254',0),(35,1993479636925403140,1993479637244170254,'/workspace/sys/i18nConfig/i18nMessage','多语言消息','sys','系统管理','MessageOutlined',1,'2026-05-12 23:30:55','2026-05-12 23:30:55','2026-05-12 23:30:55','1993479637244170254','2026-05-12 23:30:55','1993479637244170254',0),(36,1993479636925403140,1993479637244170254,'/workspace/sys/i18nConfig/i18nLanguageType','语言配置','sys','系统管理','GlobalOutlined',1,'2026-05-12 23:31:03','2026-05-12 23:31:03','2026-05-12 23:31:03','1993479637244170254','2026-05-12 23:31:03','1993479637244170254',0),(37,1993479636925403140,1993479637244170254,'/workspace/sys/onlineDev/codegenDatasource','代码生成数据源','sys','系统管理','DatabaseOutlined',1,'2026-05-12 23:31:07','2026-05-12 23:31:07','2026-05-12 23:31:07','1993479637244170254','2026-05-12 23:31:07','1993479637244170254',0),(38,1993479636925403140,1993479637244170254,'/workspace/sys/onlineDev/codegen','代码生成','sys','系统管理','CodeOutlined',1,'2026-05-12 23:31:08','2026-05-12 23:31:08','2026-05-12 23:31:08','1993479637244170254','2026-05-12 23:31:08','1993479637244170254',0),(39,1993479636925403140,1993479637244170254,'/workspace/sys/file','文件管理','sys','系统管理','FolderOpenOutlined',1,'2026-05-12 23:31:09','2026-05-12 23:31:09','2026-05-12 23:31:09','1993479637244170254','2026-05-12 23:31:09','1993479637244170254',0),(40,1993479636925403140,1993479637244170254,'/workspace/sys/maintenance/notice','系统通知','sys','系统管理','NotificationOutlined',1,'2026-05-12 23:31:15','2026-05-12 23:31:15','2026-05-12 23:31:15','1993479637244170254','2026-05-12 23:31:15','1993479637244170254',0),(41,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/dashboard','任务大盘','sys','系统管理','DashboardOutlined',1,'2026-05-13 11:53:22','2026-05-13 11:53:22','2026-05-13 11:53:22','1993479637244170242','2026-05-13 11:53:22','1993479637244170242',0),(42,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/task','任务管理','sys','系统管理','ScheduleOutlined',1,'2026-05-13 11:53:26','2026-05-13 11:53:26','2026-05-13 11:53:26','1993479637244170242','2026-05-13 11:53:26','1993479637244170242',0),(43,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/log','执行日志','sys','系统管理','FileTextOutlined',1,'2026-05-13 11:53:27','2026-05-13 11:53:27','2026-05-13 11:53:27','1993479637244170242','2026-05-13 11:53:27','1993479637244170242',0),(44,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/instance','执行器实例','sys','系统管理','ClusterOutlined',1,'2026-05-13 11:53:28','2026-05-13 11:53:28','2026-05-13 11:53:28','1993479637244170242','2026-05-13 11:53:28','1993479637244170242',0),(45,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/retry','重试/死信','sys','系统管理','ReloadOutlined',1,'2026-05-13 11:53:29','2026-05-13 11:53:29','2026-05-13 11:53:29','1993479637244170242','2026-05-13 11:53:29','1993479637244170242',0),(46,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/alarm','告警规则','sys','系统管理','BellOutlined',1,'2026-05-13 11:53:30','2026-05-13 11:53:30','2026-05-13 11:53:30','1993479637244170242','2026-05-13 11:53:30','1993479637244170242',0),(47,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/alarm-log','告警日志','sys','系统管理','AlertOutlined',1,'2026-05-13 11:53:31','2026-05-13 11:53:31','2026-05-13 11:53:31','1993479637244170242','2026-05-13 11:53:31','1993479637244170242',0),(48,1993479636925403138,1993479637244170242,'/workspace/sys/dashboard/job/workflow','DAG 编排','sys','系统管理','BranchesOutlined',1,'2026-05-13 11:53:32','2026-05-13 11:53:32','2026-05-13 11:53:32','1993479637244170242','2026-05-13 11:53:32','1993479637244170242',0),(49,1993479636925403138,1993479637244170242,'/workspace/sys/job/dashboard','任务大盘','sys','系统管理','DashboardOutlined',11,'2026-05-13 15:00:32','2026-05-15 21:21:31','2026-05-13 15:00:32','1993479637244170242','2026-05-13 15:00:32','1993479637244170242',0),(50,1993479636925403138,1993479637244170242,'/workspace/sys/onlineDev/homepage-component','首页组件目录','sys','系统管理','AppstoreOutlined',11,'2026-05-15 21:05:17','2026-05-16 16:55:22','2026-05-15 21:05:17','1993479637244170242','2026-05-15 21:05:17','1993479637244170242',0),(51,1993479636925403138,1993479637244170242,'/workspace/sys/job/log','执行日志','sys','系统管理','FileTextOutlined',2,'2026-05-15 21:20:45','2026-05-15 21:21:24','2026-05-15 21:20:45','1993479637244170242','2026-05-15 21:20:45','1993479637244170242',0),(52,1993479636925403138,1993479637244170242,'/workspace/sys/job/task','任务管理','sys','系统管理','ScheduleOutlined',1,'2026-05-15 21:20:48','2026-05-15 21:20:48','2026-05-15 21:20:48','1993479637244170242','2026-05-15 21:20:48','1993479637244170242',0),(53,1993479636925403138,1993479637244170242,'/workspace/sys/job/instance','执行器实例','sys','系统管理','ClusterOutlined',1,'2026-05-15 21:21:27','2026-05-15 21:21:27','2026-05-15 21:21:27','1993479637244170242','2026-05-15 21:21:27','1993479637244170242',0),(54,1993479636925403138,1993479637244170242,'/workspace/sys/job/retry','重试/死信','sys','系统管理','ReloadOutlined',1,'2026-05-15 21:21:27','2026-05-15 21:21:27','2026-05-15 21:21:27','1993479637244170242','2026-05-15 21:21:27','1993479637244170242',0),(55,1993479636925403138,1993479637244170242,'/workspace/sys/job/alarm','告警规则','sys','系统管理','BellOutlined',1,'2026-05-15 21:21:28','2026-05-15 21:21:28','2026-05-15 21:21:28','1993479637244170242','2026-05-15 21:21:28','1993479637244170242',0),(56,1993479636925403138,1993479637244170242,'/workspace/sys/job/alarm-log','告警日志','sys','系统管理','AlertOutlined',1,'2026-05-15 21:21:29','2026-05-15 21:21:29','2026-05-15 21:21:29','1993479637244170242','2026-05-15 21:21:29','1993479637244170242',0),(57,1993479636925403138,1993479637244170242,'/workspace/sys/job/workflow','DAG 编排','sys','系统管理','BranchesOutlined',1,'2026-05-15 21:21:30','2026-05-15 21:21:30','2026-05-15 21:21:30','1993479637244170242','2026-05-15 21:21:30','1993479637244170242',0);
/*!40000 ALTER TABLE `sys_user_menu_open_count` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_profile`
--

DROP TABLE IF EXISTS `sys_user_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '政治面貌',
  `home_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '紧急联系人电话',
  `referrer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '引荐人',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '学历',
  `work_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci COMMENT '历史工作信息（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) DEFAULT '0' COMMENT '逻辑删除：0=未删除 1=已删除',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `birth_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '籍贯',
  `intro` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `uk_user_id` (`user_id`) USING BTREE,
  UNIQUE KEY `uk_profile_user_tenant` (`tenant_id`,`user_id`) USING BTREE,
  KEY `idx_tenant_id` (`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户附属信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_profile`
--

LOCK TABLES `sys_user_profile` WRITE;
/*!40000 ALTER TABLE `sys_user_profile` DISABLE KEYS */;
INSERT INTO `sys_user_profile` VALUES (1,1993479637244170249,'other','','','','','','[]','2026-01-17 10:43:09','2026-01-17 10:43:09','1993479637244170242','1993479637244170242',0,1993479636925403138,'',''),(2,1993479637244170242,'','','','','','','[]','2026-04-02 23:53:01','2026-04-02 23:53:01','1993479637244170242','1993479637244170242',0,1993479636925403138,'',''),(3,1993479637244170252,'','','','','','','[]','2026-04-04 11:22:02','2026-04-04 11:22:02','1993479637244170242','1993479637244170242',0,1993479636925403138,'',''),(4,1993479637244170253,'','','','','','','[]','2026-04-10 16:45:45','2026-04-10 16:45:45','1993479637244170242','1993479637244170242',0,1993479636925403138,'','');
/*!40000 ALTER TABLE `sys_user_profile` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_role`
--

DROP TABLE IF EXISTS `sys_user_role`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_role` (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `idx_user_role_uid_rid_tenant` (`user_id`,`role_id`,`tenant_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2042819847144521732 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户角色关联';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_role`
--

LOCK TABLES `sys_user_role` WRITE;
/*!40000 ALTER TABLE `sys_user_role` DISABLE KEYS */;
INSERT INTO `sys_user_role` VALUES (1993479637244170242,1993479637311279107,1993479636925403138,1993479637311279108),(1993479637244170253,1993479637311279107,1993479636925403138,2042819847144521730),(1993479637244170254,1993479637311279116,1993479636925403140,2042819847144521731);
/*!40000 ALTER TABLE `sys_user_role` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user_tenant`
--

DROP TABLE IF EXISTS `sys_user_tenant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user_tenant` (
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `tenant_id` bigint DEFAULT NULL COMMENT '租户ID',
  `id` bigint(20) unsigned zerofill NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `pref_order` int DEFAULT '0' COMMENT '租户喜好排序',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认租户',
  `last_used` datetime DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id` DESC) USING BTREE,
  UNIQUE KEY `uk_user_tenant` (`user_id`,`tenant_id`) USING BTREE,
  KEY `tenant_id` (`tenant_id`) USING BTREE,
  KEY `idx_user_pref` (`user_id`,`pref_order`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=2042524421577154563 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci ROW_FORMAT=DYNAMIC COMMENT='用户租户关联表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user_tenant`
--

LOCK TABLES `sys_user_tenant` WRITE;
/*!40000 ALTER TABLE `sys_user_tenant` DISABLE KEYS */;
INSERT INTO `sys_user_tenant` VALUES (1993479637244170254,1993479636925403140,02042524421577154562,2,1,'2026-05-12 23:30:12'),(1993479637244170253,1993479636925403138,02042524421577154561,3,1,'2026-04-22 21:55:43'),(1993479637244170242,1993479636925403138,01993479637311279106,279,0,'2026-05-16 16:18:10');
/*!40000 ALTER TABLE `sys_user_tenant` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping events for database 'forgex_admin'
--

--
-- Dumping routines for database 'forgex_admin'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-05-16 17:20:25
