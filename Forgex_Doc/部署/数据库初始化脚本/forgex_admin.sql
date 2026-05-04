/*
 Navicat Premium Dump SQL

 Source Server         : bendi-127
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : localhost:3306
 Source Schema         : forgex_admin

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 03/05/2026 18:03:11
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for basic_customer
-- ----------------------------
DROP TABLE IF EXISTS `basic_customer`;
CREATE TABLE `basic_customer`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `customer_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '客户编码',
  `customer_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户名称，兼容旧字段',
  `customer_value_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户价值等级',
  `customer_credit_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户信用等级',
  `actual_business_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实际经营地址',
  `business_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '经营状态',
  `collection_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收款地址',
  `shipping_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `approval_status` tinyint NOT NULL DEFAULT 0 COMMENT '审批状态：0-未提交 1-审批中 2-已通过 3-已驳回',
  `is_related_tenant` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否关联租户',
  `related_tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联客户租户编码',
  `transport_mode` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '运输方式',
  `customer_short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户简称',
  `customer_full_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户全称',
  `customer_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户类型: DOMESTIC=国内, OVERSEAS=海外',
  `country` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家',
  `enterprise_nature` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业性质',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '税号',
  `bank_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行账号',
  `payment_terms` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付条件',
  `delivery_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交货条件',
  `currency` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'CNY' COMMENT '币种',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_customer_code_tenant`(`customer_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_customer_name`(`customer_name` ASC) USING BTREE,
  INDEX `idx_basic_customer_name`(`customer_full_name` ASC) USING BTREE,
  INDEX `idx_basic_customer_status`(`approval_status` ASC, `status` ASC) USING BTREE,
  INDEX `idx_basic_customer_tenant_code`(`related_tenant_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_customer
-- ----------------------------
INSERT INTO `basic_customer` VALUES (2001, 'CUST001', '华为技术有限公司', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, '华为', '华为技术有限公司', 'DOMESTIC', '中国', NULL, '广东省', '深圳市', '深圳', '采购部', '13900000000', 'xxxxx@xx.com', NULL, NULL, NULL, NULL, 'CNY', 1, NULL, 1993479636925403138, '2026-04-14 10:54:07', '2026-04-29 23:46:40', 'admin', NULL, 0);
INSERT INTO `basic_customer` VALUES (2002, 'CUST002', '苹果公司', NULL, NULL, NULL, NULL, NULL, NULL, 0, 0, NULL, NULL, '苹果', '苹果公司', 'OVERSEAS', '美国', NULL, '加利福尼亚', '库比蒂诺', '洛杉矶', '海外业务部', '13900000001', 'xxxxx@xx.com', NULL, NULL, NULL, NULL, 'CNY', 1, NULL, 1993479636925403138, '2026-04-14 10:54:07', '2026-04-29 23:46:40', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_customer_contact
-- ----------------------------
DROP TABLE IF EXISTS `basic_customer_contact`;
CREATE TABLE `basic_customer_contact`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人职位',
  `contact_phone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系方式',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_customer_contact_customer`(`customer_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_customer_contact
-- ----------------------------

-- ----------------------------
-- Table structure for basic_customer_extra
-- ----------------------------
DROP TABLE IF EXISTS `basic_customer_extra`;
CREATE TABLE `basic_customer_extra`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `official_website` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '企业官网',
  `switchboard_phone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '总机电话',
  `official_email_domain` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '官方邮箱域名',
  `fax_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '传真号码',
  `social_media_account` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公众号或社交媒体账号',
  `equity_penetration_level` int NULL DEFAULT NULL COMMENT '股权穿透层级',
  `holding_relation_flag` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '控股关系标记',
  `related_enterprise_ids` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联企业 ID 列表',
  `group_customer_level` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属集团客户分级',
  `channel_partner_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '渠道伙伴等级',
  `cooperation_auth_start_date` date NULL DEFAULT NULL COMMENT '合作授权期限起',
  `cooperation_auth_end_date` date NULL DEFAULT NULL COMMENT '合作授权期限止',
  `national_industry_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国标行业编码',
  `custom_industry_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自定义行业分类',
  `registered_capital` decimal(18, 2) NULL DEFAULT NULL COMMENT '注册资本',
  `registered_capital_currency` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册资本币种',
  `paid_in_capital` decimal(18, 2) NULL DEFAULT NULL COMMENT '实缴资本',
  `paid_in_capital_currency` char(3) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '实缴资本币种',
  `business_term_start` date NULL DEFAULT NULL COMMENT '营业期限起',
  `business_term_end` date NULL DEFAULT NULL COMMENT '营业期限止',
  `registration_authority` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登记机关',
  `business_scope` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '经营范围',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_customer_extra_customer`(`customer_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户其它信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_customer_extra
-- ----------------------------

-- ----------------------------
-- Table structure for basic_customer_invoice
-- ----------------------------
DROP TABLE IF EXISTS `basic_customer_invoice`;
CREATE TABLE `basic_customer_invoice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `customer_id` bigint NOT NULL COMMENT '客户主表 ID',
  `invoice_full_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发票抬头全称',
  `tax_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '税号',
  `registered_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册地址',
  `registered_phone` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '注册电话',
  `bank_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开户行',
  `bank_account` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行账号',
  `invoice_required` tinyint(1) NOT NULL DEFAULT 0 COMMENT '开票必填',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_customer_invoice_customer`(`customer_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '客户发票抬头表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_customer_invoice
-- ----------------------------

-- ----------------------------
-- Table structure for basic_factory
-- ----------------------------
DROP TABLE IF EXISTS `basic_factory`;
CREATE TABLE `basic_factory`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `factory_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂编码',
  `factory_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '工厂名称',
  `factory_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工厂类型: MANUFACTURING=制造厂, ASSEMBLY=装配厂, WAREHOUSE=仓库',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工厂地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_factory_code_tenant`(`factory_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '工厂编码唯一索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '工厂信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_factory
-- ----------------------------
INSERT INTO `basic_factory` VALUES (1, 'FACTORY_02', '一号工厂', 'MANUFACTURING', NULL, NULL, NULL, 1, 0, NULL, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);
INSERT INTO `basic_factory` VALUES (2, 'FACTORY_01', '二号工厂', 'MANUFACTURING', '广东省深圳市', '管理员', '13800000000', 1, 1, '默认工厂', 1, '2026-04-14 10:54:07', '2026-04-14 10:54:07', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for basic_label_barcode_record
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_barcode_record`;
CREATE TABLE `basic_label_barcode_record`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `barcode_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '条码号/序列号',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型',
  `material_id` bigint NULL DEFAULT NULL COMMENT '物料 ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料名称',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商编码',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户编码',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  `lot_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LOT 号',
  `work_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工单号',
  `engineering_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工程卡号',
  `production_line` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '生产线',
  `business_scene` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务场景：INCOMING=来料，PRODUCTION=生产，OUTBOUND=出库，OTHER=其他',
  `generate_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '条码生成时间',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-失效，1-有效',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `factory_id` bigint NULL DEFAULT NULL COMMENT '工厂 ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_barcode_no_tenant`(`barcode_no` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE,
  INDEX `idx_business_scene`(`business_scene` ASC) USING BTREE,
  INDEX `idx_generate_time`(`generate_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '条码记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_barcode_record
-- ----------------------------
INSERT INTO `basic_label_barcode_record` VALUES (8001, 'B202604140001', 'PRODUCT', 5001, 'MATERIAL001', '电阻 0603 10K', NULL, NULL, 'B2026041401', 'L20260414001', NULL, 'ENG20260414001', NULL, 'PRODUCTION', '2026-04-14 10:54:07', 1, 1, 1001, NULL, '2026-04-14 10:54:07', '2026-04-14 10:54:07', 'admin', NULL, 0);
INSERT INTO `basic_label_barcode_record` VALUES (8002, 'B202604140002', 'INCOMING', 5002, 'MATERIAL002', '电容 0805 10UF', NULL, NULL, 'B2026041402', 'L20260414002', NULL, 'ENG20260414002', NULL, 'INCOMING', '2026-04-14 10:54:07', 1, 1, 1001, NULL, '2026-04-14 10:54:07', '2026-04-14 10:54:07', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_label_print_config
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_print_config`;
CREATE TABLE `basic_label_print_config`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键',
  `config_value` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'STRING' COMMENT '配置类型：STRING=字符串，NUMBER=数字，BOOLEAN=布尔，JSON=JSON 对象',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置说明',
  `scope_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'GLOBAL' COMMENT '作用范围：GLOBAL=全局，FACTORY=工厂，TEMPLATE_TYPE=模板类型',
  `scope_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '作用范围值 (工厂 ID 或模板类型)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key_scope`(`config_key` ASC, `scope_type` ASC, `scope_value` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签打印参数配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_print_config
-- ----------------------------
INSERT INTO `basic_label_print_config` VALUES (1, 'DATE_FORMAT', 'yyyy-MM-dd', 'STRING', '日期显示格式', 'GLOBAL', NULL, 1, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);
INSERT INTO `basic_label_print_config` VALUES (2, 'DATETIME_FORMAT', 'yyyy-MM-dd HH:mm:ss', 'STRING', '日期时间显示格式', 'GLOBAL', NULL, 1, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);
INSERT INTO `basic_label_print_config` VALUES (3, 'NUMBER_DECIMAL_PLACES', '2', 'NUMBER', '数量小数位数', 'GLOBAL', NULL, 1, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);
INSERT INTO `basic_label_print_config` VALUES (4, 'ENABLE_THOUSAND_SEPARATOR', 'true', 'BOOLEAN', '是否启用千分位分隔符', 'GLOBAL', NULL, 1, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);
INSERT INTO `basic_label_print_config` VALUES (5, 'DEFAULT_PRINT_COUNT', '1', 'NUMBER', '默认打印张数', 'GLOBAL', NULL, 1, 1, '2026-04-14 10:20:43', '2026-04-14 10:20:43', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_label_print_exception
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_print_exception`;
CREATE TABLE `basic_label_print_exception`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `error_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误代码',
  `error_message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '错误消息',
  `print_record_id` bigint NULL DEFAULT NULL COMMENT '打印记录 ID',
  `template_id` bigint NULL DEFAULT NULL COMMENT '模板 ID',
  `factory_id` bigint NULL DEFAULT NULL COMMENT '工厂 ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人 ID',
  `exception_time` datetime NULL DEFAULT NULL COMMENT '异常发生时间',
  `stack_trace` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '堆栈信息',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除标识：0-未删除，1-已删除',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_error_code`(`error_code` ASC) USING BTREE,
  INDEX `idx_exception_time`(`exception_time` ASC) USING BTREE,
  INDEX `idx_print_record_id`(`print_record_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签打印异常记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_print_exception
-- ----------------------------

-- ----------------------------
-- Table structure for basic_label_print_record
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_print_record`;
CREATE TABLE `basic_label_print_record`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `print_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '打印流水号',
  `template_id` bigint NOT NULL COMMENT '使用的模板 ID',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码 (冗余)',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称 (冗余)',
  `template_version` int NOT NULL COMMENT '模板版本 (冗余，用于追溯)',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型 (冗余)',
  `print_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NORMAL' COMMENT '打印类型：NORMAL=正常打印，REPRINT=补打',
  `barcode_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '条码号/序列号',
  `serial_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '序列号',
  `engineering_card_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '工程卡号',
  `lot_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'LOT 号',
  `batch_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次号',
  `material_id` bigint NULL DEFAULT NULL COMMENT '物料 ID',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料编码',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商编码',
  `customer_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '客户编码',
  `data_snapshot` json NOT NULL COMMENT '本次打印的数据快照 (用于追溯和补打)',
  `print_result_json` json NOT NULL COMMENT '本次实际返回给前端的打印模板内容 (填充后的完整 JSON)',
  `print_count` int NOT NULL DEFAULT 1 COMMENT '打印张数',
  `factory_id` bigint NULL DEFAULT NULL COMMENT '工厂 ID',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作人 ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作人姓名',
  `print_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打印时间',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_print_no`(`print_no` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_barcode_no`(`barcode_no` ASC) USING BTREE,
  INDEX `idx_serial_no`(`serial_no` ASC) USING BTREE,
  INDEX `idx_engineering_card_no`(`engineering_card_no` ASC) USING BTREE,
  INDEX `idx_lot_no`(`lot_no` ASC) USING BTREE,
  INDEX `idx_material_code`(`material_code` ASC) USING BTREE,
  INDEX `idx_print_time`(`print_time` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签打印记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_print_record
-- ----------------------------
INSERT INTO `basic_label_print_record` VALUES (9001, 'PRINT202604140001', 5001, 'TP_PRODUCT_001', '产品默认标签', 1, 'PRODUCT', 'NORMAL', 'B202604140001', NULL, 'ENG20260414001', 'L20260414001', 'B2026041401', NULL, 'MATERIAL001', NULL, NULL, '{\"lot\": \"L20260414001\", \"material\": \"MATERIAL001\"}', '{\"label\": \"filled data\"}', 1, 1001, 10001, '操作员 A', '2026-04-14 10:54:07', 1, '正常打印', '2026-04-14 10:54:07', '2026-04-14 10:54:07', 0);
INSERT INTO `basic_label_print_record` VALUES (9002, 'PRINT202604140002', 5002, 'TP_INCOMING_001', '来料默认标签', 1, 'INCOMING', 'NORMAL', 'B202604140002', NULL, 'ENG20260414002', 'L20260414002', 'B2026041402', NULL, 'MATERIAL002', NULL, NULL, '{\"material\": \"MATERIAL002\", \"supplier\": \"SUPP001\"}', '{\"label\": \"filled data\"}', 1, 1001, 10001, '操作员 A', '2026-04-14 10:54:07', 1, '正常打印', '2026-04-14 10:54:07', '2026-04-14 10:54:07', 0);

-- ----------------------------
-- Table structure for basic_label_template
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_template`;
CREATE TABLE `basic_label_template`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编码',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板类型：MATERIAL=物料标签，PRODUCT=产品标签，LOT=LOT 标签，INCOMING=来料标签，SUPPLIER=供应商标签，CUSTOMER_MARK=客户唛头，CUSTOMER_LABEL=客户定制标签，WORKSTATION=工位标签，PERSONNEL=人员标签，EQUIPMENT=设备标签，PROCESS_STEP=工步标签，LOCATION=库位标签，SPQ_INNER=SPQ 内箱标签，PQ_OUTER=PQ 外箱标签，OVERSEAS_OUTER=海外外箱标签，SHIPPING_BOX=出货箱数标签，ENG_CARD_PACKAGE=工程卡包装标签',
  `template_version` int NOT NULL DEFAULT 1 COMMENT '模板版本号',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认模板：0-否，1-是',
  `template_content` json NOT NULL COMMENT '模板内容 JSON(包含版式信息、占位符配置等)',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板描述',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `factory_id` bigint NULL DEFAULT NULL COMMENT '工厂 ID(NULL 表示全局模板)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code_version`(`template_code` ASC, `template_version` ASC, `factory_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_template_type`(`template_type` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE,
  INDEX `idx_factory_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签模板主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_template
-- ----------------------------
INSERT INTO `basic_label_template` VALUES (1, 'TP_PRODUCT_001', '产品默认标签', 'PRODUCT', 1, 1, '{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"MATERIAL_CODE\"}, {\"x\": 10, \"y\": 30, \"name\": \"MATERIAL_NAME\"}, {\"x\": 10, \"y\": 60, \"name\": \"BARCODE\"}, {\"x\": 10, \"y\": 120, \"name\": \"LOT_NO\"}]}', '标准产品标签', 1, NULL, -1, '2026-04-14 10:54:07', '2026-04-15 10:36:50', 'admin', 'admin', 0);
INSERT INTO `basic_label_template` VALUES (2, 'TP_INCOMING_001', '来料默认标签', 'INCOMING', 1, 1, '{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"SUPPLIER_CODE\"}, {\"x\": 10, \"y\": 30, \"name\": \"MATERIAL_CODE\"}, {\"x\": 10, \"y\": 60, \"name\": \"BATCH_NO\"}, {\"x\": 10, \"y\": 90, \"name\": \"QUANTITY\"}]}', '标准来料标签', 1, NULL, -1, '2026-04-14 10:54:07', '2026-04-15 10:36:51', 'admin', 'admin', 0);
INSERT INTO `basic_label_template` VALUES (3, 'TP_CUSTOMER_MARK_001', '客户唛头标签', 'CUSTOMER_MARK', 1, 1, '{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"CUSTOMER_NAME\"}, {\"x\": 10, \"y\": 40, \"name\": \"SHIP_TO\"}, {\"x\": 10, \"y\": 80, \"name\": \"BOX_NO\"}]}', '客户外箱唛头', 1, NULL, -1, '2026-04-14 10:54:07', '2026-04-15 10:36:53', 'admin', 'admin', 0);
INSERT INTO `basic_label_template` VALUES (4, 'TP_ENG_PACKAGE_001', '工程卡包装标签', 'ENG_CARD_PACKAGE', 1, 1, '{\"fields\": [{\"x\": 10, \"y\": 10, \"name\": \"ENG_CARD_NO\"}, {\"x\": 10, \"y\": 40, \"name\": \"MATERIAL_NAME\"}, {\"x\": 10, \"y\": 80, \"name\": \"PQ\"}, {\"x\": 10, \"y\": 120, \"name\": \"BOX_NO\"}]}', '工程卡包装专用', 1, NULL, -1, '2026-04-14 10:54:07', '2026-04-15 10:36:55', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for basic_label_template_binding
-- ----------------------------
DROP TABLE IF EXISTS `basic_label_template_binding`;
CREATE TABLE `basic_label_template_binding`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `template_id` bigint NOT NULL COMMENT '模板 ID',
  `binding_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定类型：MATERIAL=按物料，SUPPLIER=按供应商，CUSTOMER=按客户',
  `binding_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '绑定值 (物料编码/供应商编码/客户编码)',
  `priority` int NOT NULL DEFAULT 0 COMMENT '优先级 (数字越大优先级越高)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `factory_id` bigint NULL DEFAULT NULL COMMENT '工厂 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_binding_unique`(`template_id` ASC, `binding_type` ASC, `binding_value` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_template_id`(`template_id` ASC) USING BTREE,
  INDEX `idx_binding_type_value`(`binding_type` ASC, `binding_value` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '标签模板绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_label_template_binding
-- ----------------------------
INSERT INTO `basic_label_template_binding` VALUES (1, 1, 'MATERIAL', 'MATERIAL001', 10, -1, NULL, '2026-04-14 10:54:07', '2026-04-15 11:15:48', 'admin', NULL, 0);
INSERT INTO `basic_label_template_binding` VALUES (2, 2, 'SUPPLIER', 'SUPP001', 10, -1, NULL, '2026-04-14 10:54:07', '2026-04-15 11:15:49', 'admin', NULL, 0);
INSERT INTO `basic_label_template_binding` VALUES (3, 3, 'CUSTOMER', 'CUST001', 10, -1, NULL, '2026-04-14 10:54:07', '2026-04-15 11:15:52', 'admin', NULL, 0);
INSERT INTO `basic_label_template_binding` VALUES (10, 4, 'SUPPLIER', 'SUPP001', 2, -1, NULL, '2026-04-15 11:18:54', '2026-04-15 11:18:54', NULL, NULL, 0);

-- ----------------------------
-- Table structure for basic_material
-- ----------------------------
DROP TABLE IF EXISTS `basic_material`;
CREATE TABLE `basic_material`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `material_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料编码 (租户内唯一)',
  `material_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '物料名称',
  `material_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料类型：RAW_MATERIAL=原材料，FINISHED_GOODS=成品，TOOL=工具，SEMI_FINISHED=半成品，OTHER=其它',
  `material_category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '物料分类 (字典编码，支持多级分类)',
  `specification` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '规格',
  `unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '计量单位',
  `brand` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '品牌',
  `image_url` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '物料图片 URL',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详细描述',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'NO_APPROVAL_REQUIRED' COMMENT '审批状态：无需审批，未审批，已审批，已驳回',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  `model` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '型号',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_code_tenant`(`material_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '物料编码唯一索引',
  INDEX `idx_material_name`(`material_name` ASC) USING BTREE COMMENT '物料名称索引',
  INDEX `idx_material_type`(`material_type` ASC) USING BTREE COMMENT '物料类型索引',
  INDEX `idx_material_category`(`material_category` ASC) USING BTREE COMMENT '物料分类索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户 ID 索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物料管理主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_material
-- ----------------------------
INSERT INTO `basic_material` VALUES (7000000000000000001, 'RAW_001', '钢板 Q235B', 'RAW_MATERIAL', 'metal_sheet', '10mm*1500mm*6000mm', '张', '宝钢', NULL, NULL, '常用原材料', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000002, 'RAW_002', '铜棒 H59', 'RAW_MATERIAL', 'metal_rod', '直径20mm*3000mm', '根', '金田铜业', NULL, NULL, '高精度铜棒', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000003, 'RAW_003', '铝锭 ADC12', 'RAW_MATERIAL', 'metal_ingot', '20kg/锭', 'kg', '中铝', NULL, NULL, '压铸铝合金', 1, 'PENDING', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', NULL, 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000004, 'SEMI_001', '冲压外壳', 'SEMI_FINISHED', 'shell', '450mm*300mm*50mm', '个', NULL, NULL, NULL, '钣金冲压件', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000005, 'SEMI_002', '齿轮组件', 'SEMI_FINISHED', 'gear', '模数2.5 齿数30', '套', NULL, NULL, NULL, '精密齿轮', 1, 'NO_APPROVAL_REQUIRED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', NULL, 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000006, 'SEMI_003', '电机定子', 'SEMI_FINISHED', 'motor_part', '功率5.5KW', '个', NULL, NULL, NULL, '电机核心部件', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000007, 'PROD_001', '减速机 XWD5', 'FINISHED_GOODS', 'reducer', '速比29 功率4KW', '台', '国茂', NULL, NULL, '摆线针轮减速机', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000008, 'PROD_002', '离心泵 ISG50', 'FINISHED_GOODS', 'pump', '流量12.5m3/h 扬程20m', '台', '南方泵业', NULL, NULL, '立式管道离心泵', 1, 'APPROVED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000009, 'PROD_003', '变频器 VFD-EL', 'FINISHED_GOODS', 'inverter', '功率7.5KW 380V', '台', '台达', NULL, NULL, '小型高性能变频器', 1, 'REJECTED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0, NULL);
INSERT INTO `basic_material` VALUES (7000000000000000010, 'OTH_001', '润滑油', 'OTHER', 'consumable', '4L/桶', '桶', '壳牌', NULL, NULL, '设备润滑用油', 1, 'NO_APPROVAL_REQUIRED', 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', NULL, 0, NULL);

-- ----------------------------
-- Table structure for basic_material_extend
-- ----------------------------
DROP TABLE IF EXISTS `basic_material_extend`;
CREATE TABLE `basic_material_extend`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料 ID',
  `module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块标识 (如：PURCHASE=采购，SALE=销售，PRODUCTION=生产)',
  `extend_json` json NOT NULL COMMENT '扩展信息 JSON',
  `min_stock` decimal(18, 4) NULL DEFAULT NULL COMMENT '最低库存',
  `max_stock` decimal(18, 4) NULL DEFAULT NULL COMMENT '最高库存',
  `safety_stock` decimal(18, 4) NULL DEFAULT NULL COMMENT '安全库存',
  `valid_period_value` int NULL DEFAULT NULL COMMENT '有效周期值',
  `valid_period_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '有效周期单位(YEAR=年, MONTH=月, DAY=日, HOUR=时)',
  `stagnant_period_value` int NULL DEFAULT NULL COMMENT '呆滞周期值',
  `stagnant_period_unit` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '呆滞周期单位(YEAR=年, MONTH=月, DAY=日, HOUR=时)',
  `packaging_type_id` bigint NULL DEFAULT NULL COMMENT '包装方式ID(关联未来包装方式管理表)',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_material_id`(`material_id` ASC) USING BTREE COMMENT '物料 ID 索引',
  INDEX `idx_module`(`module` ASC) USING BTREE COMMENT '模块标识索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户 ID 索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '物料扩展信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_material_extend
-- ----------------------------
INSERT INTO `basic_material_extend` VALUES (8000000000000000001, 7000000000000000001, 'PURCHASE', '{\"currency\": \"CNY\", \"supplier\": \"宝钢集团\", \"min_order_qty\": 10, \"payment_terms\": \"月结30天\", \"lead_time_days\": 7, \"purchase_price\": 4500.0}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0);
INSERT INTO `basic_material_extend` VALUES (8000000000000000002, 7000000000000000001, 'INVENTORY', '{\"location\": \"A-01-03\", \"warehouse\": \"原材料仓A\", \"safety_stock\": 50, \"current_stock\": 120, \"unit_weight_kg\": 78.5, \"shelf_life_days\": 0}', 50.0000, 500.0000, 100.0000, 12, 'MONTH', 90, 'DAY', NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:14:41', 'admin', 'admin', 0);
INSERT INTO `basic_material_extend` VALUES (8000000000000000003, 7000000000000000002, 'PURCHASE', '{\"currency\": \"CNY\", \"supplier\": \"金田铜业集团\", \"min_order_qty\": 100, \"payment_terms\": \"款到发货\", \"lead_time_days\": 5, \"purchase_price\": 52000.0}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', NULL, 0);
INSERT INTO `basic_material_extend` VALUES (8000000000000000004, 7000000000000000002, 'INVENTORY', '{\"location\": \"A-02-01\", \"warehouse\": \"原材料仓A\", \"safety_stock\": 200, \"current_stock\": 350, \"unit_weight_kg\": 2.8, \"shelf_life_days\": 0}', 200.0000, 1000.0000, 300.0000, 24, 'MONTH', 180, 'DAY', NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:14:41', 'admin', 'admin', 0);
INSERT INTO `basic_material_extend` VALUES (8000000000000000005, 7000000000000000007, 'PRODUCTION', '{\"bom_version\": \"V2.0\", \"process_route\": \"组装-测试-包装\", \"standard_cost\": 2800.0, \"cycle_time_min\": 45, \"quality_check_points\": [\"噪音测试\", \"温升测试\", \"密封测试\"]}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0);
INSERT INTO `basic_material_extend` VALUES (8000000000000000006, 7000000000000000007, 'SALES', '{\"currency\": \"CNY\", \"tax_rate\": 0.13, \"sale_price\": 3800.0, \"min_sale_price\": 3200.0, \"warranty_months\": 12}', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 1993479636925403138, '2026-04-28 13:12:09', '2026-04-28 13:12:09', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for basic_material_finished_goods
-- ----------------------------
DROP TABLE IF EXISTS `basic_material_finished_goods`;
CREATE TABLE `basic_material_finished_goods`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json NULL COMMENT '成品附属信息JSON(自定义字段配置)',
  `cost` decimal(18, 4) NULL DEFAULT NULL COMMENT '成本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_id_tenant`(`material_id` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '物料ID唯一索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成品附属信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_material_finished_goods
-- ----------------------------
INSERT INTO `basic_material_finished_goods` VALUES (9100000000000000001, 7000000000000000007, '{\"ratio\": 29, \"weight_kg\": 85, \"product_model\": \"XWD5-29-4\", \"efficiency_pct\": 94, \"noise_level_db\": 65, \"rated_power_kw\": 4, \"input_speed_rpm\": 1440, \"rated_torque_nm\": 750, \"warranty_months\": 12, \"output_speed_rpm\": 49.7, \"protection_level\": \"IP54\", \"installation_type\": \"卧式\"}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);
INSERT INTO `basic_material_finished_goods` VALUES (9100000000000000002, 7000000000000000008, '{\"head_m\": 20, \"npsh_m\": 2.5, \"speed_rpm\": 2900, \"weight_kg\": 45, \"flow_rate_m3h\": 12.5, \"product_model\": \"ISG50-160\", \"efficiency_pct\": 68, \"rated_power_kw\": 2.2, \"connection_type\": \"法兰\", \"warranty_months\": 12, \"material_contact\": \"铸铁\", \"max_pressure_mpa\": 1.6}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);
INSERT INTO `basic_material_finished_goods` VALUES (9100000000000000003, 7000000000000000009, '{\"weight_kg\": 5.2, \"communication\": [\"Modbus\", \"RS485\"], \"dimensions_mm\": \"280*150*180\", \"input_voltage\": \"380V\", \"product_model\": \"VFD075EL43A\", \"efficiency_pct\": 96, \"output_voltage\": \"0-380V\", \"rated_power_kw\": 7.5, \"warranty_months\": 18, \"protection_level\": \"IP20\", \"frequency_range_hz\": \"0-400\"}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for basic_material_raw_material
-- ----------------------------
DROP TABLE IF EXISTS `basic_material_raw_material`;
CREATE TABLE `basic_material_raw_material`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json NULL COMMENT '原材料附属信息JSON(自定义字段配置)',
  `current_active_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '当前激活价格',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_id_tenant`(`material_id` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '物料ID唯一索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '原材料附属信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_material_raw_material
-- ----------------------------
INSERT INTO `basic_material_raw_material` VALUES (9000000000000000001, 7000000000000000001, '{\"standard\": \"GB/T 3274-2017\", \"hardness_hb\": 140, \"certification\": [\"材质证明书\", \"出厂合格证\"], \"density_kg_m3\": 7850, \"elongation_pct\": 26, \"material_grade\": \"Q235B\", \"melting_point_c\": 1538, \"surface_treatment\": \"热轧\", \"yield_strength_mpa\": 235, \"tensile_strength_mpa\": \"370-500\"}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);
INSERT INTO `basic_material_raw_material` VALUES (9000000000000000002, 7000000000000000002, '{\"standard\": \"GB/T 4423-2017\", \"hardness_hv\": 120, \"certification\": [\"材质证明书\"], \"density_kg_m3\": 8400, \"elongation_pct\": 30, \"material_grade\": \"H59\", \"melting_point_c\": 900, \"surface_treatment\": \"拉丝\", \"copper_content_pct\": 59, \"yield_strength_mpa\": 220, \"tensile_strength_mpa\": 390}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', NULL, 0);
INSERT INTO `basic_material_raw_material` VALUES (9000000000000000003, 7000000000000000003, '{\"standard\": \"JIS H 5302\", \"hardness_hb\": 80, \"certification\": [\"材质证明书\", \"ROHS报告\"], \"density_kg_m3\": 2700, \"material_grade\": \"ADC12\", \"melting_point_c\": 595, \"surface_treatment\": \"铸造\", \"silicon_content_pct\": 9.5, \"aluminum_content_pct\": 85, \"tensile_strength_mpa\": 230}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_material_semi_finished
-- ----------------------------
DROP TABLE IF EXISTS `basic_material_semi_finished`;
CREATE TABLE `basic_material_semi_finished`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `material_id` bigint NOT NULL COMMENT '物料ID(关联basic_material表)',
  `extend_json` json NULL COMMENT '半成品附属信息JSON(自定义字段配置)',
  `cost` decimal(18, 4) NULL DEFAULT NULL COMMENT '成本',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_material_id_tenant`(`material_id` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '物料ID唯一索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '半成品附属信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_material_semi_finished
-- ----------------------------
INSERT INTO `basic_material_semi_finished` VALUES (9200000000000000001, 7000000000000000004, '{\"thickness_mm\": 1.5, \"process_steps\": [\"下料\", \"冲压成型\", \"去毛刺\", \"表面处理\"], \"material_source\": \"RAW_001\", \"processing_method\": \"冲压\", \"processing_time_min\": 15, \"quality_requirements\": [\"无裂纹\", \"无变形\", \"尺寸合格\"], \"surface_roughness_ra\": 3.2, \"dimensional_tolerance_mm\": 0.1}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);
INSERT INTO `basic_material_semi_finished` VALUES (9200000000000000002, 7000000000000000005, '{\"module\": 2.5, \"teeth_count\": 30, \"process_steps\": [\"车削\", \"滚齿\", \"热处理\", \"磨齿\"], \"accuracy_grade\": \"7级\", \"material_source\": \"RAW_002\", \"processing_method\": \"滚齿加工\", \"pressure_angle_deg\": 20, \"processing_time_min\": 30, \"quality_requirements\": [\"齿形合格\", \"齿向合格\", \"跳动合格\"], \"surface_hardness_hrc\": \"58-62\"}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', NULL, 0);
INSERT INTO `basic_material_semi_finished` VALUES (9200000000000000003, 7000000000000000006, '{\"poles\": 4, \"power_kw\": 5.5, \"winding_type\": \"双层叠绕组\", \"process_steps\": [\"铁芯压装\", \"绕线\", \"嵌线\", \"绝缘处理\", \"浸漆烘干\"], \"turns_per_coil\": 28, \"material_source\": \"外部采购\", \"insulation_class\": \"F级\", \"wire_diameter_mm\": 1.25, \"processing_method\": \"绕线嵌线\", \"processing_time_min\": 60, \"quality_requirements\": [\"绝缘电阻合格\", \"匝间无短路\", \"直流电阻平衡\"]}', NULL, 1993479636925403138, '2026-04-28 13:12:10', '2026-04-28 13:12:10', 'admin', 'admin', 0);

-- ----------------------------
-- Table structure for basic_packaging_type
-- ----------------------------
DROP TABLE IF EXISTS `basic_packaging_type`;
CREATE TABLE `basic_packaging_type`  (
  `id` bigint NOT NULL COMMENT '主键ID(雪花算法)',
  `packaging_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包装方式编码',
  `packaging_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包装方式名称',
  `packaging_material` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '包装材料(纸箱/木箱/托盘/铁桶等)',
  `length_mm` decimal(10, 2) NULL DEFAULT NULL COMMENT '长度(mm)',
  `width_mm` decimal(10, 2) NULL DEFAULT NULL COMMENT '宽度(mm)',
  `height_mm` decimal(10, 2) NULL DEFAULT NULL COMMENT '高度(mm)',
  `weight_kg` decimal(10, 2) NULL DEFAULT NULL COMMENT '包装自重(kg)',
  `max_load_kg` decimal(10, 2) NULL DEFAULT NULL COMMENT '最大承重(kg)',
  `unit_cost` decimal(18, 4) NULL DEFAULT NULL COMMENT '单位成本',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除: 0-未删除, 1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_packaging_code_tenant`(`packaging_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE COMMENT '包装方式编码唯一索引',
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE COMMENT '租户ID索引'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '包装方式表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_packaging_type
-- ----------------------------
INSERT INTO `basic_packaging_type` VALUES (5100000000000000001, 'CTN-A', 'A型纸箱', 'carton', 400.00, 300.00, 200.00, 0.50, 25.00, 2.5000, 1, 1, '标准A型纸箱', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', 'admin', 0);
INSERT INTO `basic_packaging_type` VALUES (5100000000000000002, 'CTN-B', 'B型纸箱', 'carton', 600.00, 400.00, 300.00, 0.80, 40.00, 3.5000, 1, 2, '标准B型纸箱', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', 'admin', 0);
INSERT INTO `basic_packaging_type` VALUES (5100000000000000003, 'WDB-A', 'A型木箱', 'wooden_box', 800.00, 600.00, 500.00, 5.00, 100.00, 45.0000, 1, 3, '重型包装木箱', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', 'admin', 0);
INSERT INTO `basic_packaging_type` VALUES (5100000000000000004, 'PLT-A', '标准托盘', 'pallet', 1200.00, 1000.00, 150.00, 15.00, 1000.00, 120.0000, 1, 4, '1200*1000标准托盘', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', 'admin', 0);
INSERT INTO `basic_packaging_type` VALUES (5100000000000000005, 'DRM-200L', '200L铁桶', 'iron_drum', 580.00, 580.00, 880.00, 20.00, 200.00, 85.0000, 1, 5, '200升标准铁桶', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', 'admin', 0);
INSERT INTO `basic_packaging_type` VALUES (5100000000000000006, 'BAG-50', '50kg塑料袋', 'plastic_bag', 600.00, 400.00, 10.00, 0.10, 50.00, 0.5000, 1, 6, '50kg承重塑料袋', 1993479636925403138, '2026-04-28 13:38:43', '2026-04-28 13:38:43', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_supplier
-- ----------------------------
DROP TABLE IF EXISTS `basic_supplier`;
CREATE TABLE `basic_supplier`  (
  `id` bigint NOT NULL COMMENT '主键 ID(雪花算法)',
  `supplier_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商编码',
  `supplier_full_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????',
  `supplier_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '供应商名称',
  `supplier_short_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商简称',
  `logo_url` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商 Logo 图片访问地址',
  `english_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `current_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `primary_contact` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `supplier_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '供应商类型：RAW_MATERIAL=原材料，PACKAGING=包装，LOGISTICS=物流，SERVICE=服务',
  `country` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国家/地区',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '城市',
  `address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '详细地址',
  `contact_person` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `cooperation_status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????1-?? 2-?? 3-?? 4-??',
  `credit_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????A/B/C/D',
  `risk_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `supplier_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??????1-?? 2-?? 3-??',
  `related_tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????????',
  `review_status` tinyint NOT NULL DEFAULT 1 COMMENT '?????0-???? 1-??? 2-??? 3-???',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `tax_number` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '税号',
  `bank_account` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '银行账号',
  `payment_terms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '付款条件',
  `quality_level` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '质量等级：A/B/C/D',
  `certification` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '认证资质 (多个用逗号分隔)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_supplier_code_tenant`(`supplier_code` ASC, `tenant_id` ASC, `deleted` ASC) USING BTREE,
  UNIQUE INDEX `uk_basic_supplier_code`(`supplier_code` ASC) USING BTREE,
  INDEX `idx_supplier_name`(`supplier_name` ASC) USING BTREE,
  INDEX `idx_basic_supplier_status`(`cooperation_status` ASC, `review_status` ASC) USING BTREE,
  INDEX `idx_basic_supplier_tenant_code`(`related_tenant_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '供应商信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of basic_supplier
-- ----------------------------
INSERT INTO `basic_supplier` VALUES (3001, 'SUPP001', '富士康科技集团', '富士康科技集团', '富士康', NULL, NULL, NULL, '供应商对接人', 'RAW_MATERIAL', '中国', '广东省', '深圳市', NULL, '供应商对接人', '13700000000', NULL, NULL, NULL, NULL, 'sup_supp001', 1, NULL, NULL, NULL, NULL, 'A', NULL, 1, NULL, -1, '2026-04-14 10:54:07', '2026-04-26 16:26:05', 'admin', '1993479637244170242', 0);
INSERT INTO `basic_supplier` VALUES (3002, 'SUPP002', '比亚迪股份有限公司', '比亚迪股份有限公司', '比亚迪', NULL, NULL, NULL, '供应商对接人', 'PACKAGING', '中国', '广东省', '深圳市', NULL, '供应商对接人', '13700000001', NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL, NULL, NULL, 'A', NULL, 1, NULL, 1, '2026-04-14 10:54:07', '2026-04-26 16:26:05', 'admin', NULL, 0);

-- ----------------------------
-- Table structure for basic_supplier_contact
-- ----------------------------
DROP TABLE IF EXISTS `basic_supplier_contact`;
CREATE TABLE `basic_supplier_contact`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `contact_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????',
  `contact_phone` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????',
  `contact_position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????',
  `contact_email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_supplier_contact_supplier`(`supplier_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_supplier_contact_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '?????????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_supplier_contact
-- ----------------------------

-- ----------------------------
-- Table structure for basic_supplier_detail
-- ----------------------------
DROP TABLE IF EXISTS `basic_supplier_detail`;
CREATE TABLE `basic_supplier_detail`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `legal_representative` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `registered_capital` decimal(18, 2) NULL DEFAULT NULL COMMENT '????',
  `establishment_date` date NULL DEFAULT NULL COMMENT '????',
  `enterprise_nature` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '?????1-?? 2-?? 3-?? 4-??',
  `industry_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????????supplier_industry_category',
  `registered_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `business_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `email` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??',
  `tax_number` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??',
  `bank_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `bank_account` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '??????',
  `invoice_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????????supplier_invoice_type',
  `default_tax_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '????',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_supplier_detail_supplier`(`supplier_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_supplier_detail_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '????????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_supplier_detail
-- ----------------------------

-- ----------------------------
-- Table structure for basic_supplier_qualification
-- ----------------------------
DROP TABLE IF EXISTS `basic_supplier_qualification`;
CREATE TABLE `basic_supplier_qualification`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '?? ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '?? ID????????????? 0',
  `supplier_id` bigint NOT NULL COMMENT '????? ID',
  `qualification_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????????supplier_qualification_type',
  `certificate_no` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????',
  `issue_date` date NULL DEFAULT NULL COMMENT '????',
  `expire_date` date NULL DEFAULT NULL COMMENT '????',
  `attachment` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '????? URL',
  `valid` tinyint(1) NOT NULL DEFAULT 1 COMMENT '?????0-??1-?',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '????',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '????',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '???',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '?????0-????1-???',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_basic_supplier_qualification_supplier`(`supplier_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_supplier_qualification_expire`(`expire_date` ASC) USING BTREE,
  INDEX `idx_basic_supplier_qualification_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '????????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_supplier_qualification
-- ----------------------------

-- ----------------------------
-- Table structure for basic_unit
-- ----------------------------
DROP TABLE IF EXISTS `basic_unit`;
CREATE TABLE `basic_unit`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `unit_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位编码',
  `unit_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '单位名称',
  `unit_symbol` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位符号',
  `unit_category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '单位分类',
  `conversion_rate` decimal(18, 6) NOT NULL DEFAULT 1.000000 COMMENT '换算比率',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `unit_type_id` bigint NOT NULL COMMENT '计量单位类型 ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_basic_unit_tenant_type_code_deleted`(`tenant_id` ASC, `unit_type_id` ASC, `unit_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_unit_tenant_status`(`tenant_id` ASC, `status` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_unit_sort`(`sort_order` ASC) USING BTREE,
  INDEX `idx_basic_unit_type`(`tenant_id` ASC, `unit_type_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '计量单位表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_unit
-- ----------------------------
INSERT INTO `basic_unit` VALUES (1, 'PCS', '个', 'pcs', '数量', 1.000000, 1, 10, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (2, 'SET', '套', 'set', '数量', 1.000000, 1, 20, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (3, 'BOX', '箱', 'box', '包装', 1.000000, 1, 30, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (4, 'KG', '千克', 'kg', '重量', 1.000000, 1, 40, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (5, 'G', '克', 'g', '重量', 0.001000, 1, 50, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (6, 'M', '米', 'm', '长度', 1.000000, 1, 60, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (7, 'CM', '厘米', 'cm', '长度', 0.010000, 1, 70, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (8, 'MM', '毫米', 'mm', '长度', 0.001000, 1, 80, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (9, 'L', '升', 'L', '体积', 1.000000, 1, 90, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `basic_unit` VALUES (10, 'ML', '毫升', 'ml', '体积', 0.001000, 1, 100, '系统初始化', 1993479636925403138, 101882303365513228, '2026-04-30 10:22:49', '20260430_fix_pages', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);

-- ----------------------------
-- Table structure for basic_unit_conversion
-- ----------------------------
DROP TABLE IF EXISTS `basic_unit_conversion`;
CREATE TABLE `basic_unit_conversion`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `unit_id` bigint NOT NULL COMMENT '源计量单位 ID',
  `target_unit_id` bigint NOT NULL COMMENT '目标计量单位 ID',
  `conversion_value` decimal(30, 12) NOT NULL COMMENT '转换后数值（1:x）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_basic_unit_conversion_tenant_pair_deleted`(`tenant_id` ASC, `unit_id` ASC, `target_unit_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_unit_conversion_unit`(`tenant_id` ASC, `unit_id` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_unit_conversion_target`(`tenant_id` ASC, `target_unit_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '计量单位换算关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_unit_conversion
-- ----------------------------

-- ----------------------------
-- Table structure for basic_unit_type
-- ----------------------------
DROP TABLE IF EXISTS `basic_unit_type`;
CREATE TABLE `basic_unit_type`  (
  `id` bigint NOT NULL COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户 ID',
  `unit_type_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计量单位类型编码',
  `unit_type_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '计量单位类型',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父 ID',
  `level_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '层级路径',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_basic_unit_type_tenant_code_deleted`(`tenant_id` ASC, `unit_type_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_basic_unit_type_parent`(`tenant_id` ASC, `parent_id` ASC, `deleted` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '计量单位类型表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of basic_unit_type
-- ----------------------------
INSERT INTO `basic_unit_type` VALUES (101882303365513228, 1993479636925403138, 'DEFAULT', '默认类型', 0, '0/101882303365513228.000000000000000000000000000000', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:32:23', '20260502_basic_unit_and_table_upgrade', 0);

-- ----------------------------
-- Table structure for sys_c_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_c_menu`;
CREATE TABLE `sys_c_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户 ID',
  `module_id` bigint NULL DEFAULT NULL COMMENT '所属模块 ID（关联 sys_module）',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父菜单 ID，顶级为 0',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'menu' COMMENT '菜单类型：catalog=目录, menu=菜单, button=按钮',
  `menu_level` int NOT NULL DEFAULT 1 COMMENT '菜单层级：1=一级, 2=二级, 3=三级',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `name_i18n_json` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '国际化名称 JSON',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `component_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件键（原生端页面映射标识）',
  `perm_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限键',
  `menu_mode` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'embedded' COMMENT '菜单模式：embedded=内嵌, external=外联',
  `external_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外联 URL',
  `order_num` int NULL DEFAULT 0 COMMENT '排序号（越小越靠前）',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可见：0=隐藏, 1=显示',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：0=禁用, 1=启用',
  `tenant_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PUBLIC' COMMENT '适用租户类型：MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC',
  `device_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ALL' COMMENT '设备类型：MOBILE=手机, TABLET=Pad, ALL=通用',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除, 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_module_id`(`module_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_device_type`(`device_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'C 端菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_c_menu
-- ----------------------------
INSERT INTO `sys_c_menu` VALUES (1, NULL, NULL, 0, 'catalog', 1, '/workbench', '工作台', '{\"en-US\": \"/workbench\", \"ja-JP\": \"工作台\", \"ko-KR\": \"/workbench\", \"zh-CN\": \"工作台\", \"zh-TW\": \"工作台\"}', 'dashboard', 'WorkbenchScreen', 'c:workbench:view', 'embedded', NULL, 1, 1, 1, 'PUBLIC', 'ALL', '2026-04-11 21:30:46', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0);
INSERT INTO `sys_c_menu` VALUES (2, NULL, NULL, 0, 'catalog', 1, '/workflow', '审批中心', '{\"en-US\": \"/workflow\", \"ja-JP\": \"审批中心\", \"ko-KR\": \"/workflow\", \"zh-CN\": \"审批中心\", \"zh-TW\": \"审批中心\"}', 'approval', 'WorkflowScreen', 'c:workflow:view', 'embedded', NULL, 2, 1, 1, 'PUBLIC', 'ALL', '2026-04-11 21:30:46', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0);
INSERT INTO `sys_c_menu` VALUES (3, NULL, NULL, 0, 'catalog', 1, '/message', '消息中心', '{\"en-US\": \"/message\", \"ja-JP\": \"消息中心\", \"ko-KR\": \"/message\", \"zh-CN\": \"消息中心\", \"zh-TW\": \"消息中心\"}', 'message', 'MessageScreen', 'c:message:view', 'embedded', NULL, 3, 1, 1, 'PUBLIC', 'ALL', '2026-04-11 21:30:46', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0);
INSERT INTO `sys_c_menu` VALUES (4, NULL, NULL, 0, 'catalog', 1, '/profile', '我的', '{\"en-US\": \"/profile\", \"ja-JP\": \"我的\", \"ko-KR\": \"/profile\", \"zh-CN\": \"我的\", \"zh-TW\": \"我的\"}', 'person', 'ProfileScreen', 'c:profile:view', 'embedded', NULL, 4, 1, 1, 'PUBLIC', 'ALL', '2026-04-11 21:30:46', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0);
INSERT INTO `sys_c_menu` VALUES (5, 1993479636925403138, 5, 0, 'catalog', 1, 'basic', '基础信息', '{\"zh-CN\":\"基础信息\",\"en-US\":\"Basic Information\",\"zh-TW\":\"基礎信息\",\"ja-JP\":\"基礎情報\",\"ko-KR\":\"기본 정보\"}', 'DatabaseOutlined', 'BasicModule', 'c:basic:view', 'embedded', NULL, 10, 1, 1, 'PUBLIC', 'ALL', '2026-04-23 10:47:10', 'system', '2026-04-23 10:56:03', 'codex', 0);
INSERT INTO `sys_c_menu` VALUES (6, 1993479636925403138, 5, 5, 'menu', 2, 'basic/info-test', '基础信息测试页', '{\"zh-CN\":\"基础信息测试页\",\"en-US\":\"Basic Info Test Page\",\"zh-TW\":\"基礎信息測試頁\",\"ja-JP\":\"基礎情報テストページ\",\"ko-KR\":\"기본 정보 테스트 페이지\"}', 'ExperimentOutlined', 'BasicInfoTestScreen', 'c:basic:test:view', 'embedded', NULL, 10, 1, 1, 'PUBLIC', 'ALL', '2026-04-23 10:47:10', 'system', '2026-04-23 10:56:03', 'codex', 0);

-- ----------------------------
-- Table structure for sys_codegen_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_codegen_config`;
CREATE TABLE `sys_codegen_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `config_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置名称',
  `datasource_id` bigint NOT NULL COMMENT '数据源ID',
  `datasource_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据源编码',
  `schema_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库/schema名称',
  `page_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '页面类型',
  `main_table_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主表名称',
  `sub_table_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子表名称',
  `main_pk_column` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '主表主键字段',
  `sub_fk_column` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子表外键字段',
  `sub_pk_column` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子表主键字段',
  `module_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `biz_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务编码',
  `entity_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '主实体名称',
  `sub_entity_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '子实体名称',
  `package_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '包名',
  `author` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作者',
  `menu_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `menu_icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `parent_menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父级菜单路径',
  `table_code_prefix` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表格编码前缀',
  `generate_items_json` json NULL COMMENT '生成项JSON',
  `config_json` json NOT NULL COMMENT '完整配置JSON',
  `last_generate_time` datetime NULL DEFAULT NULL COMMENT '最近生成时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_codegen_config_datasource`(`datasource_id` ASC) USING BTREE,
  INDEX `idx_codegen_config_page_type`(`page_type` ASC) USING BTREE,
  INDEX `idx_codegen_config_module_biz`(`module_name` ASC, `biz_name` ASC) USING BTREE,
  INDEX `idx_codegen_config_main_table`(`main_table_name` ASC) USING BTREE,
  INDEX `idx_codegen_config_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成配置记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_codegen_config
-- ----------------------------

-- ----------------------------
-- Table structure for sys_codegen_datasource
-- ----------------------------
DROP TABLE IF EXISTS `sys_codegen_datasource`;
CREATE TABLE `sys_codegen_datasource`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `datasource_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据源编码',
  `datasource_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据源名称',
  `db_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '数据库类型',
  `jdbc_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'JDBC连接地址',
  `username` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '密码',
  `schema_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '默认schema/catalog',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_codegen_datasource_code`(`datasource_code` ASC) USING BTREE,
  INDEX `idx_codegen_datasource_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_codegen_datasource_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '代码生成数据源' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_codegen_datasource
-- ----------------------------
INSERT INTO `sys_codegen_datasource` VALUES (1, 1993479636925403138, 'forgex_admin', 'Forgex Admin', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_admin?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_admin', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (2, 1993479636925403138, 'forgex_common', 'Forgex Common', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_common?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_common', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (3, 1993479636925403138, 'forgex_history', 'Forgex History', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_history?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_history', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (4, 1993479636925403138, 'forgex_integration', 'Forgex Integration', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_integration?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_integration', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (5, 1993479636925403138, 'forgex_job', 'Forgex Job', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_job?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_job', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (6, 1993479636925403138, 'forgex_scada', 'Forgex Scada', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_scada?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_scada', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);
INSERT INTO `sys_codegen_datasource` VALUES (7, 1993479636925403138, 'forgex_workflow', 'Forgex Workflow', 'mysql', 'jdbc:mysql://127.0.0.1:3306/forgex_workflow?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true', 'root', '123456', 'forgex_workflow', 1, '?????', '2026-04-24 14:41:32', 'system', '2026-04-30 10:52:16', '20260430_fix_pages', 0);

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NULL DEFAULT 0 COMMENT '父部门ID',
  `org_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '组织类型：group=集团, company=公司, subsidiary=子公司, department=部门, team=班组',
  `org_level` int NOT NULL COMMENT '组织层级：1=集团, 2=公司, 3=子公司, 4=部门, 5=班组',
  `dept_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `dept_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门编码',
  `leader` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `order_num` int NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标志：0=未删除，1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dept_code`(`dept_code` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_org_type`(`org_type` ASC) USING BTREE,
  INDEX `idx_org_level`(`org_level` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表（支持树状组织架构）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (1, 0, 'group', 1, 'Forgex集团', 'GROUP_FORGEX', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:43', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (2, 1, 'company', 2, 'Forgex制造有限公司', 'COMPANY_MFG', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:43', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (3, 1, 'company', 2, 'Forgex科技有限公司', 'COMPANY_TECH', NULL, NULL, NULL, 2, 1, '2026-01-07 14:41:43', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (4, 2, 'subsidiary', 3, '华东分公司', 'SUB_EAST', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:43', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (5, 2, 'subsidiary', 3, '华南分公司', 'SUB_SOUTH', NULL, NULL, NULL, 2, 1, '2026-01-07 14:41:43', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (6, 2, 'department', 4, '总经办', 'DEPT_GM', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (7, 2, 'department', 4, '技术部', 'DEPT_TECH', NULL, NULL, NULL, 2, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (8, 2, 'department', 4, '生产部', 'DEPT_PROD', NULL, NULL, NULL, 3, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (9, 2, 'department', 4, '质量部', 'DEPT_QC', NULL, NULL, NULL, 4, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (10, 2, 'department', 4, '采购部', 'DEPT_PUR', NULL, NULL, NULL, 5, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (11, 2, 'department', 4, '销售部', 'DEPT_SALES', NULL, NULL, NULL, 6, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (12, 2, 'department', 4, '财务部', 'DEPT_FIN', NULL, NULL, NULL, 7, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (13, 2, 'department', 4, '人力资源部', 'DEPT_HR', NULL, NULL, NULL, 8, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (14, 8, 'team', 5, '生产一班', 'TEAM_PROD_1', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (15, 8, 'team', 5, '生产二班', 'TEAM_PROD_2', NULL, NULL, NULL, 2, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (16, 9, 'team', 5, '质检一组', 'TEAM_QC_1', NULL, NULL, NULL, 1, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (17, 9, 'team', 5, '质检二组', 'TEAM_QC_2', NULL, NULL, NULL, 2, 1, '2026-01-07 14:41:44', '2026-01-18 20:02:19', NULL, NULL, 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (18, 0, 'department', 1, '?????', 'PLATFORM_DEPT', NULL, NULL, NULL, 1, 1, '2026-04-02 14:46:48', '2026-04-05 11:26:48', 'system', '1993479637244170242', 1993479636925403138, 1);
INSERT INTO `sys_department` VALUES (19, 2, 'department', 1, '测试', 'TEST', NULL, NULL, NULL, 0, 1, '2026-04-04 14:34:21', '2026-04-04 14:34:21', '1993479637244170242', '1993479637244170242', 1993479636925403138, 0);
INSERT INTO `sys_department` VALUES (20, 19, 'company', 1, '测试1', 'TEST1', NULL, NULL, NULL, 0, 1, '2026-04-04 14:36:05', '2026-04-11 09:25:05', '1993479637244170242', '1993479637244170242', 1993479636925403138, 1);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '父节点ID（0表示根节点）',
  `dict_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典名称(兼容字段)',
  `dict_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '字典编码',
  `module_id` bigint NULL DEFAULT NULL COMMENT 'module id',
  `dict_value` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典键',
  `dict_value_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '字典显示值国际化(JSON)',
  `node_path` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '节点路径(dict_code路径，用/分割)',
  `level` int NOT NULL DEFAULT 1 COMMENT '层级',
  `children_count` int NOT NULL DEFAULT 0 COMMENT '直接子节点数量',
  `order_num` int NOT NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1启用，0禁用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '修改人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '删除标记：0未删除，1已删除',
  `tag_style_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '标签样式配置JSON，用于字典值标签的样式配置',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_dict_sibling_code`(`tenant_id` ASC, `parent_id` ASC, `dict_code` ASC, `deleted` ASC) USING BTREE,
  UNIQUE INDEX `uk_dict_path`(`tenant_id` ASC, `node_path` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_dict_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_dict_code`(`dict_code` ASC) USING BTREE,
  INDEX `idx_dict_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5000000000000000229 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (4, 0, '登录状态', 'login_status', NULL, NULL, '{\"en-US\": \"Login Status\", \"ja-JP\": \"ログイン状態\", \"ko-KR\": \"로그인 상태\", \"zh-CN\": \"登录状态\", \"zh-TW\": \"登錄狀態\"}', 'login_status', 1, 2, 1, 1, '登录日志状态字典', 1993479636925403138, 1, '2026-01-15 16:54:14', NULL, '2026-01-21 20:40:59', 0, NULL);
INSERT INTO `sys_dict` VALUES (5, 4, '成功', 'success', NULL, '1', '{\"en-US\": \"Success\", \"ja-JP\": \"成功\", \"ko-KR\": \"성공\", \"zh-CN\": \"成功\", \"zh-TW\": \"成功\"}', 'login_status/success', 2, 0, 1, 1, NULL, 1993479636925403138, 1, '2026-01-15 16:54:32', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (6, 4, '失败', 'fail', NULL, '0', '{\"en-US\": \"Fail\", \"ja-JP\": \"失敗\", \"ko-KR\": \"실패\", \"zh-CN\": \"失败\", \"zh-TW\": \"失敗\"}', 'login_status/fail', 2, 0, 2, 1, NULL, 1993479636925403138, 1, '2026-01-15 16:54:54', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (7, 0, '是否', 'yes_no', NULL, NULL, '{\"en-US\": \"Yes/No\", \"ja-JP\": \"はい/いいえ\", \"ko-KR\": \"예/아니오\", \"zh-CN\": \"是否\", \"zh-TW\": \"是否\"}', 'yes_no', 1, 2, 2, 1, '通用是否字典', 1993479636925403138, 1, '2026-01-15 16:55:46', NULL, '2026-01-21 20:40:59', 0, NULL);
INSERT INTO `sys_dict` VALUES (8, 7, '是', 'yes', NULL, '1', '{\"en-US\": \"Yes\", \"ja-JP\": \"はい\", \"ko-KR\": \"예\", \"zh-CN\": \"是\", \"zh-TW\": \"是\"}', 'yes_no/yes', 2, 0, 1, 1, NULL, 1993479636925403138, 1, '2026-01-15 16:56:01', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (9, 7, '否', 'no', NULL, '0', '{\"en-US\": \"No\", \"ja-JP\": \"いいえ\", \"ko-KR\": \"아니오\", \"zh-CN\": \"否\", \"zh-TW\": \"否\"}', 'yes_no/no', 2, 0, 2, 1, NULL, 1993479636925403138, 1, '2026-01-15 16:56:12', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (10, 0, '组织类型', 'org_type', NULL, NULL, '{\"en-US\": \"Organization Type\", \"ja-JP\": \"組織タイプ\", \"ko-KR\": \"조직 유형\", \"zh-CN\": \"组织类型\", \"zh-TW\": \"組織類型\"}', '/org_type', 1, 5, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:16', NULL, '2026-01-19 21:17:05', 0, NULL);
INSERT INTO `sys_dict` VALUES (11, 0, '组织层级', 'org_level', NULL, NULL, '{\"en-US\": \"Organization Level\", \"ja-JP\": \"組織レベル\", \"ko-KR\": \"조직 수준\", \"zh-CN\": \"组织层级\", \"zh-TW\": \"組織層級\"}', '/org_level', 1, 5, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:16', NULL, '2026-01-19 21:17:44', 0, NULL);
INSERT INTO `sys_dict` VALUES (22, 10, '集团', 'group', NULL, 'group', '{\"en-US\": \"Group\", \"ja-JP\": \"グループ\", \"ko-KR\": \"그룹\", \"zh-CN\": \"集团\", \"zh-TW\": \"集團\"}', '/org_type/group', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:49', NULL, '2026-01-21 20:58:18', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (23, 10, '公司', 'company', NULL, 'company', '{\"en-US\": \"Company\", \"ja-JP\": \"会社\", \"ko-KR\": \"회사\", \"zh-CN\": \"公司\", \"zh-TW\": \"公司\"}', '/org_type/company', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:49', NULL, '2026-01-21 20:58:18', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (24, 10, '子公司', 'subsidiary', NULL, 'subsidiary', '{\"en-US\": \"Subsidiary\", \"ja-JP\": \"子会社\", \"ko-KR\": \"자회사\", \"zh-CN\": \"子公司\", \"zh-TW\": \"子公司\"}', '/org_type/subsidiary', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:49', NULL, '2026-01-21 20:58:18', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (25, 10, '部门', 'department', NULL, 'department', '{\"en-US\": \"Department\", \"ja-JP\": \"部門\", \"ko-KR\": \"부서\", \"zh-CN\": \"部门\", \"zh-TW\": \"部門\"}', '/org_type/department', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:49', NULL, '2026-01-21 20:58:18', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (26, 10, '班组', 'team', NULL, 'team', '{\"en-US\": \"Team\", \"ja-JP\": \"チーム\", \"ko-KR\": \"팀\", \"zh-CN\": \"班组\", \"zh-TW\": \"團隊\"}', '/org_type/team', 2, 0, 5, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:49', NULL, '2026-01-21 20:58:18', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (27, 11, '集团', 'level1', NULL, '1', '{\"en-US\": \"Group\", \"ja-JP\": \"グループ\", \"ko-KR\": \"그룹\", \"zh-CN\": \"集团\", \"zh-TW\": \"集團\"}', '/org_level/level1', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:57', NULL, '2026-01-21 20:58:39', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (28, 11, '公司', 'level2', NULL, '2', '{\"en-US\": \"Company\", \"ja-JP\": \"会社\", \"ko-KR\": \"회사\", \"zh-CN\": \"公司\", \"zh-TW\": \"公司\"}', '/org_level/level2', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:57', NULL, '2026-01-21 20:58:39', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (29, 11, '子公司', 'level3', NULL, '3', '{\"en-US\": \"Subsidiary\", \"ja-JP\": \"子会社\", \"ko-KR\": \"자회사\", \"zh-CN\": \"子公司\", \"zh-TW\": \"子公司\"}', '/org_level/level3', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:57', NULL, '2026-01-21 20:58:39', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (30, 11, '部门', 'level4', NULL, '4', '{\"en-US\": \"Department\", \"ja-JP\": \"部門\", \"ko-KR\": \"부서\", \"zh-CN\": \"部门\", \"zh-TW\": \"部門\"}', '/org_level/level4', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:57', NULL, '2026-01-21 20:58:39', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (31, 11, '班组', 'level5', NULL, '5', '{\"en-US\": \"Team\", \"ja-JP\": \"チーム\", \"ko-KR\": \"팀\", \"zh-CN\": \"班组\", \"zh-TW\": \"團隊\"}', '/org_level/level5', 2, 0, 5, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:23:57', NULL, '2026-01-21 20:58:39', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (32, 0, '性别', 'gender', NULL, NULL, '{\"en-US\": \"Gender\", \"ja-JP\": \"性別\", \"ko-KR\": \"성별\", \"zh-CN\": \"性别\", \"zh-TW\": \"性別\"}', '/gender', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:15:30', 0, NULL);
INSERT INTO `sys_dict` VALUES (33, 0, '学历', 'education', NULL, NULL, '{\"en-US\": \"Education\", \"ja-JP\": \"学歴\", \"ko-KR\": \"학력\", \"zh-CN\": \"学历\", \"zh-TW\": \"學歷\"}', '/education', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:14:59', 0, NULL);
INSERT INTO `sys_dict` VALUES (34, 0, '政治面貌', 'political_status', NULL, NULL, '{\"en-US\": \"Political Status\", \"ja-JP\": \"政治的立場\", \"ko-KR\": \"정치적 입장\", \"zh-CN\": \"政治面貌\", \"zh-TW\": \"政治面貌\"}', '/political_status', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:15:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (35, 0, '职位级别', 'position_level', NULL, NULL, '{\"en-US\": \"Position Level\", \"ja-JP\": \"職位レベル\", \"ko-KR\": \"직위 레벨\", \"zh-CN\": \"职位级别\", \"zh-TW\": \"職位級別\"}', '/position_level', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:18:07', 0, NULL);
INSERT INTO `sys_dict` VALUES (36, 0, '菜单模式', 'menu_mode', NULL, NULL, '{\"en-US\": \"Menu Mode\", \"ja-JP\": \"メニューモード\", \"ko-KR\": \"메뉴 모드\", \"zh-CN\": \"菜单模式\", \"zh-TW\": \"選單模式\"}', '/menu_mode', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:16:46', 0, NULL);
INSERT INTO `sys_dict` VALUES (37, 0, '菜单类型', 'menu_type', NULL, NULL, '{\"en-US\": \"Menu Type\", \"ja-JP\": \"メニュータイプ\", \"ko-KR\": \"메뉴 유형\", \"zh-CN\": \"菜单类型\", \"zh-TW\": \"選單類型\"}', '/menu_type', 1, 0, 0, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:17', NULL, '2026-01-19 21:16:32', 0, NULL);
INSERT INTO `sys_dict` VALUES (39, 32, '男', 'male', NULL, '1', '{\"en-US\": \"Male\", \"ja-JP\": \"男性\", \"ko-KR\": \"남성\", \"zh-CN\": \"男\", \"zh-TW\": \"男\"}', '/gender/male', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:28', NULL, '2026-01-21 20:57:47', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (40, 32, '女', 'female', NULL, '2', '{\"en-US\": \"Female\", \"ja-JP\": \"女性\", \"ko-KR\": \"여성\", \"zh-CN\": \"女\", \"zh-TW\": \"女\"}', '/gender/female', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:28', NULL, '2026-01-21 20:57:57', 0, '{\"color\": \"pink\"}');
INSERT INTO `sys_dict` VALUES (41, 33, '小学', 'primary', NULL, 'primary', '{\"en-US\": \"Primary\", \"ja-JP\": \"小学校\", \"ko-KR\": \"초등학교\", \"zh-CN\": \"小学\", \"zh-TW\": \"小學\"}', '/education/primary', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (42, 33, '初中', 'junior', NULL, 'junior', '{\"en-US\": \"Junior\", \"ja-JP\": \"中学校\", \"ko-KR\": \"중학교\", \"zh-CN\": \"初中\", \"zh-TW\": \"初中\"}', '/education/junior', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (43, 33, '高中', 'senior', NULL, 'senior', '{\"en-US\": \"Senior\", \"ja-JP\": \"高等学校\", \"ko-KR\": \"고등학교\", \"zh-CN\": \"高中\", \"zh-TW\": \"高中\"}', '/education/senior', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (44, 33, '职高', 'vocational', NULL, 'vocational', '{\"en-US\": \"Vocational\", \"ja-JP\": \"専門学校\", \"ko-KR\": \"직업학교\", \"zh-CN\": \"职高\", \"zh-TW\": \"職校\"}', '/education/vocational', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (45, 33, '大专', 'college', NULL, 'college', '{\"en-US\": \"College\", \"ja-JP\": \"短期大学\", \"ko-KR\": \"전문대학\", \"zh-CN\": \"大专\", \"zh-TW\": \"大專\"}', '/education/college', 2, 0, 5, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (46, 33, '本科', 'bachelor', NULL, 'bachelor', '{\"en-US\": \"Bachelor\", \"ja-JP\": \"学士\", \"ko-KR\": \"학사\", \"zh-CN\": \"本科\", \"zh-TW\": \"本科\"}', '/education/bachelor', 2, 0, 6, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (47, 33, '硕士', 'master', NULL, 'master', '{\"en-US\": \"Master\", \"ja-JP\": \"修士\", \"ko-KR\": \"석사\", \"zh-CN\": \"硕士\", \"zh-TW\": \"碩士\"}', '/education/master', 2, 0, 7, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (48, 33, '博士', 'doctor', NULL, 'doctor', '{\"en-US\": \"Doctor\", \"ja-JP\": \"博士\", \"ko-KR\": \"박사\", \"zh-CN\": \"博士\", \"zh-TW\": \"博士\"}', '/education/doctor', 2, 0, 8, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:37', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (49, 34, '其他', 'other', NULL, 'other', '{\"en-US\": \"Other\", \"ja-JP\": \"その他\", \"ko-KR\": \"기타\", \"zh-CN\": \"其他\", \"zh-TW\": \"其他\"}', '/political_status/other', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:47', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (50, 34, '共产党员', 'communist', NULL, 'communist', '{\"en-US\": \"Communist Party Member\", \"ja-JP\": \"共産党員\", \"ko-KR\": \"공산당원\", \"zh-CN\": \"共产党员\", \"zh-TW\": \"共產黨員\"}', '/political_status/communist', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:47', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (51, 34, '民主党派', 'democratic', NULL, 'democratic', '{\"en-US\": \"Democratic Party\", \"ja-JP\": \"民主主義政党\", \"ko-KR\": \"민주당\", \"zh-CN\": \"民主党派\", \"zh-TW\": \"民主黨派\"}', '/political_status/democratic', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:47', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (52, 34, '共青团员', 'league', NULL, 'league', '{\"en-US\": \"League Member\", \"ja-JP\": \"共青団員\", \"ko-KR\": \"공청단원\", \"zh-CN\": \"共青团员\", \"zh-TW\": \"共青團員\"}', '/political_status/league', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:47', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (53, 34, '群众', 'masses', NULL, 'masses', '{\"en-US\": \"Masses\", \"ja-JP\": \"大衆\", \"ko-KR\": \"대중\", \"zh-CN\": \"群众\", \"zh-TW\": \"群眾\"}', '/political_status/masses', 2, 0, 5, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:24:47', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (59, 36, '菜单', 'menu', NULL, 'menu', '{\"en-US\": \"Menu\", \"ja-JP\": \"メニュー\", \"ko-KR\": \"메뉴\", \"zh-CN\": \"菜单\", \"zh-TW\": \"選單\"}', '/menu_mode/menu', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:05', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (60, 36, '标签页', 'tab', NULL, 'tab', '{\"en-US\": \"Tab\", \"ja-JP\": \"タブ\", \"ko-KR\": \"탭\", \"zh-CN\": \"标签页\", \"zh-TW\": \"標籤頁\"}', '/menu_mode/tab', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:05', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (61, 36, '弹窗', 'modal', NULL, 'modal', '{\"en-US\": \"Modal\", \"ja-JP\": \"モーダル\", \"ko-KR\": \"모달\", \"zh-CN\": \"弹窗\", \"zh-TW\": \"彈窗\"}', '/menu_mode/modal', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:05', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (62, 36, '抽屉', 'drawer', NULL, 'drawer', '{\"en-US\": \"Drawer\", \"ja-JP\": \"ドロワー\", \"ko-KR\": \"드로워\", \"zh-CN\": \"抽屉\", \"zh-TW\": \"抽屜\"}', '/menu_mode/drawer', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:05', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (63, 36, '外部链接', 'external', NULL, 'external', '{\"en-US\": \"External\", \"ja-JP\": \"外部リンク\", \"ko-KR\": \"외부 링크\", \"zh-CN\": \"外部链接\", \"zh-TW\": \"外部連結\"}', '/menu_mode/external', 2, 0, 5, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:05', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (64, 37, '目录', 'directory', NULL, 'directory', '{\"en-US\": \"Directory\", \"ja-JP\": \"ディレクトリ\", \"ko-KR\": \"디렉토리\", \"zh-CN\": \"目录\", \"zh-TW\": \"目錄\"}', '/menu_type/directory', 2, 0, 1, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:13', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (65, 37, '菜单', 'menu', NULL, 'menu', '{\"en-US\": \"Menu\", \"ja-JP\": \"メニュー\", \"ko-KR\": \"메뉴\", \"zh-CN\": \"菜单\", \"zh-TW\": \"選單\"}', '/menu_type/menu', 2, 0, 2, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:13', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (66, 37, '按钮', 'button', NULL, 'button', '{\"en-US\": \"Button\", \"ja-JP\": \"ボタン\", \"ko-KR\": \"버튼\", \"zh-CN\": \"按钮\", \"zh-TW\": \"按鈕\"}', '/menu_type/button', 2, 0, 3, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:13', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (67, 37, '链接', 'link', NULL, 'link', '{\"en-US\": \"Link\", \"ja-JP\": \"リンク\", \"ko-KR\": \"링크\", \"zh-CN\": \"链接\", \"zh-TW\": \"連結\"}', '/menu_type/link', 2, 0, 4, 1, NULL, 1993479636925403138, NULL, '2026-01-16 13:25:13', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (68, 0, '状态', 'status', NULL, NULL, '{\"en-US\": \"Status\", \"ja-JP\": \"ステータス\", \"ko-KR\": \"상태\", \"zh-CN\": \"状态\", \"zh-TW\": \"狀態\"}', 'status', 1, 0, 100, 1, '通用状态字典', 1993479636925403138, NULL, '2026-01-20 23:10:08', NULL, '2026-01-21 20:40:59', 0, NULL);
INSERT INTO `sys_dict` VALUES (69, 68, '启用', 'enabled', NULL, '1', '{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}', 'status/enabled', 2, 0, 1, 1, '启用状态', 1993479636925403138, NULL, '2026-01-20 23:10:59', NULL, '2026-04-07 09:49:51', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (70, 68, '禁用', 'disabled', NULL, '0', '{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"禁用\", \"zh-TW\": \"停用\"}', 'status/disabled', 2, 0, 2, 1, '禁用状态', 1993479636925403138, NULL, '2026-01-20 23:10:59', NULL, '2026-04-07 09:49:51', 0, '{\"color\": \"red\"}');
INSERT INTO `sys_dict` VALUES (71, 0, '可见性', 'visible', NULL, NULL, '{\"en-US\": \"Visibility\", \"ja-JP\": \"可視性\", \"ko-KR\": \"가시성\", \"zh-CN\": \"可见性\", \"zh-TW\": \"可見性\"}', 'visible', 1, 0, 101, 1, '可见性字典', 1993479636925403138, NULL, '2026-01-20 23:15:56', NULL, '2026-01-21 20:40:59', 0, NULL);
INSERT INTO `sys_dict` VALUES (72, 71, '显示', 'show', NULL, 'true', '{\"en-US\": \"Show\", \"ja-JP\": \"表示\", \"ko-KR\": \"표시\", \"zh-CN\": \"显示\", \"zh-TW\": \"顯示\"}', 'visible/show', 2, 0, 1, 1, '显示状态', 1993479636925403138, NULL, '2026-01-20 23:16:36', NULL, '2026-01-21 20:40:59', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (73, 71, '隐藏', 'hide', NULL, 'false', '{\"en-US\": \"Hide\", \"ja-JP\": \"非表示\", \"ko-KR\": \"숨기기\", \"zh-CN\": \"隐藏\", \"zh-TW\": \"隱藏\"}', 'visible/hide', 2, 0, 2, 1, '隐藏状态', 1993479636925403138, NULL, '2026-01-20 23:16:36', NULL, '2026-01-21 20:40:59', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (77, 32, '未知', 'unknown', NULL, '0', '{\"en-US\": \"Unknown\", \"ja-JP\": \"不明\", \"ko-KR\": \"알 수 없음\", \"zh-CN\": \"未知\", \"zh-TW\": \"未知\"}', '/gender/unknown', 2, 0, 1, 1, '未知性别', 1993479636925403138, NULL, '2026-01-21 20:20:56', NULL, '2026-01-21 20:58:13', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (78, 35, '一级', 'level1', NULL, '1', '{\"en-US\": \"Level 1\", \"ja-JP\": \"1\", \"ko-KR\": \"1\", \"zh-CN\": \"一级\", \"zh-TW\": \"1\"}', '/position_level/1', 2, 0, 1, 1, '一级职位', 1993479636925403138, NULL, '2026-01-21 20:23:55', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (79, 35, '二级', 'level2', NULL, '2', '{\"en-US\": \"Level 2\", \"ja-JP\": \"2\", \"ko-KR\": \"2\", \"zh-CN\": \"二级\", \"zh-TW\": \"2\"}', '/position_level/2', 2, 0, 2, 1, '二级职位', 1993479636925403138, NULL, '2026-01-21 20:23:55', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (80, 35, '三级', 'level3', NULL, '3', '{\"en-US\": \"Level 3\", \"ja-JP\": \"3\", \"ko-KR\": \"3\", \"zh-CN\": \"三级\", \"zh-TW\": \"3\"}', '/position_level/3', 2, 0, 3, 1, '三级职位', 1993479636925403138, NULL, '2026-01-21 20:23:55', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (81, 35, '四级', 'level4', NULL, '4', '{\"en-US\": \"Level 4\", \"ja-JP\": \"4\", \"ko-KR\": \"4\", \"zh-CN\": \"四级\", \"zh-TW\": \"4\"}', '/position_level/4', 2, 0, 4, 1, '四级职位', 1993479636925403138, NULL, '2026-01-21 20:23:55', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (82, 35, '五级', 'level5', NULL, '5', '{\"en-US\": \"Level 5\", \"ja-JP\": \"5\", \"ko-KR\": \"5\", \"zh-CN\": \"五级\", \"zh-TW\": \"5\"}', '/position_level/5', 2, 0, 5, 1, '五级职位', 1993479636925403138, NULL, '2026-01-21 20:23:55', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000001, 0, '编码段类型', 'encode_segment_type', NULL, NULL, '{\"zh-CN\": \"编码段类型\", \"zh-TW\": \"編碼段類型\", \"en-US\": \"Encode Segment Type\", \"ja-JP\": \"コードセグメントタイプ\", \"ko-KR\": \"인코드 세그먼트 유형\"}', 'encode_segment_type', 1, 4, 1, 1, '用于配置编码规则中各段的类型：FIXED=固定字符，DATE=日期，SEQUENCE=序列号，VARIABLE=变量', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-10 11:30:30', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000002, 0, '重置类型', 'reset_type', NULL, NULL, '{\"zh-CN\": \"重置类型\", \"zh-TW\": \"重置類型\", \"en-US\": \"Reset Type\", \"ja-JP\": \"リセットタイプ\", \"ko-KR\": \"재설정 유형\"}', 'reset_type', 1, 4, 2, 1, '用于配置编码规则中序列号的重置周期：NONE=不重置，DAILY=每日重置，MONTHLY=每月重置，YEARLY=每年重置', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-10 11:30:30', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000003, 0, '填充方向', 'padding_side', NULL, NULL, '{\"zh-CN\": \"填充方向\", \"zh-TW\": \"填充方向\", \"en-US\": \"Padding Side\", \"ja-JP\": \"パディング方向\", \"ko-KR\": \"패딩 방향\"}', 'padding_side', 1, 2, 3, 1, '用于配置序列号填充字符的方向：LEFT=左侧填充，RIGHT=右侧填充', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-10 11:30:30', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000011, 5000000000000000001, '固定字符', 'encode_segment_type_fixed', NULL, 'FIXED', '{\"zh-CN\": \"固定字符\", \"zh-TW\": \"固定字符\", \"en-US\": \"Fixed Character\", \"ja-JP\": \"固定文字\", \"ko-KR\": \"고정 문자\"}', 'encode_segment_type/fixed', 2, 0, 1, 1, '固定字符段，编码中显示固定的字符（如前缀、后缀、分隔符等）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000012, 5000000000000000001, '日期', 'encode_segment_type_date', NULL, 'DATE', '{\"zh-CN\": \"日期\", \"zh-TW\": \"日期\", \"en-US\": \"Date\", \"ja-JP\": \"日付\", \"ko-KR\": \"날짜\"}', 'encode_segment_type/date', 2, 0, 2, 1, '日期段，编码中显示日期信息（支持 yyyy、MM、dd、HHmmss 等格式）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000013, 5000000000000000001, '序列号', 'encode_segment_type_sequence', NULL, 'SEQUENCE', '{\"zh-CN\": \"序列号\", \"zh-TW\": \"序列號\", \"en-US\": \"Sequence Number\", \"ja-JP\": \"シーケンス番号\", \"ko-KR\": \"일련 번호\"}', 'encode_segment_type/sequence', 2, 0, 3, 1, '序列号段，编码中显示递增的序列号（支持自定义起始值、长度、填充字符等）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000014, 5000000000000000001, '变量', 'encode_segment_type_variable', NULL, 'VARIABLE', '{\"zh-CN\": \"变量\", \"zh-TW\": \"變量\", \"en-US\": \"Variable\", \"ja-JP\": \"変数\", \"ko-KR\": \"변수\"}', 'encode_segment_type/variable', 2, 0, 4, 1, '变量段，编码中显示动态变量值（如部门编码、人员编码、业务类型等）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000021, 5000000000000000002, '不重置', 'reset_type_none', NULL, 'NONE', '{\"zh-CN\": \"不重置\", \"zh-TW\": \"不重置\", \"en-US\": \"No Reset\", \"ja-JP\": \"リセットしない\", \"ko-KR\": \"재설정 안 함\"}', 'reset_type/none', 2, 0, 1, 1, '序列号不重置，持续递增', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000022, 5000000000000000002, '每日重置', 'reset_type_daily', NULL, 'DAILY', '{\"zh-CN\": \"每日重置\", \"zh-TW\": \"每日重置\", \"en-US\": \"Daily Reset\", \"ja-JP\": \"毎日リセット\", \"ko-KR\": \"매일 재설정\"}', 'reset_type/daily', 2, 0, 2, 1, '序列号每天重置为起始值', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000023, 5000000000000000002, '每月重置', 'reset_type_monthly', NULL, 'MONTHLY', '{\"zh-CN\": \"每月重置\", \"zh-TW\": \"每月重置\", \"en-US\": \"Monthly Reset\", \"ja-JP\": \"毎月リセット\", \"ko-KR\": \"매월 재설정\"}', 'reset_type/monthly', 2, 0, 3, 1, '序列号每月重置为起始值', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000024, 5000000000000000002, '每年重置', 'reset_type_yearly', NULL, 'YEARLY', '{\"zh-CN\": \"每年重置\", \"zh-TW\": \"每年重置\", \"en-US\": \"Yearly Reset\", \"ja-JP\": \"毎年リセット\", \"ko-KR\": \"매년 재설정\"}', 'reset_type/yearly', 2, 0, 4, 1, '序列号每年重置为起始值', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000031, 5000000000000000003, '左侧填充', 'padding_side_left', NULL, 'LEFT', '{\"zh-CN\": \"左侧填充\", \"zh-TW\": \"左側填充\", \"en-US\": \"Left Padding\", \"ja-JP\": \"左パディング\", \"ko-KR\": \"왼쪽 패딩\"}', 'padding_side/left', 2, 0, 1, 1, '填充字符在序列号左侧（如：000123）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000032, 5000000000000000003, '右侧填充', 'padding_side_right', NULL, 'RIGHT', '{\"zh-CN\": \"右侧填充\", \"zh-TW\": \"右側填充\", \"en-US\": \"Right Padding\", \"ja-JP\": \"右パディング\", \"ko-KR\": \"오른쪽 패딩\"}', 'padding_side/right', 2, 0, 2, 1, '填充字符在序列号右侧（如：123000）', 0, NULL, '2026-04-10 11:30:30', NULL, '2026-04-13 15:38:02', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000043, 0, '审批表单类型', 'wf_task_form_type', NULL, NULL, '{\"en-US\": \"wf_task_form_type\", \"ja-JP\": \"审批表单类型\", \"ko-KR\": \"wf_task_form_type\", \"zh-CN\": \"审批表单类型\", \"zh-TW\": \"审批表单类型\"}', 'wf_task_form_type', 1, 2, 301, 1, '审批任务配置页表单类型字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000044, 0, '审批执行状态', 'wf_execution_status', NULL, NULL, '{\"en-US\": \"wf_execution_status\", \"ja-JP\": \"审批执行状态\", \"ko-KR\": \"wf_execution_status\", \"zh-CN\": \"审批执行状态\", \"zh-TW\": \"审批执行状态\"}', 'wf_execution_status', 1, 4, 302, 1, '审批待办/已办/我发起状态字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000045, 0, '审批类型', 'wf_approve_type', NULL, NULL, '{\"en-US\": \"wf_approve_type\", \"ja-JP\": \"审批类型\", \"ko-KR\": \"wf_approve_type\", \"zh-CN\": \"审批类型\", \"zh-TW\": \"审批类型\"}', 'wf_approve_type', 1, 5, 303, 1, '审批节点审批类型字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000046, 0, '审批人来源', 'wf_approver_type', NULL, NULL, '{\"en-US\": \"wf_approver_type\", \"ja-JP\": \"审批人来源\", \"ko-KR\": \"wf_approver_type\", \"zh-CN\": \"审批人来源\", \"zh-TW\": \"审批人来源\"}', 'wf_approver_type', 1, 4, 304, 1, '审批节点审批人来源字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000047, 0, '驳回类型', 'wf_reject_type', NULL, NULL, '{\"en-US\": \"wf_reject_type\", \"ja-JP\": \"驳回类型\", \"ko-KR\": \"wf_reject_type\", \"zh-CN\": \"驳回类型\", \"zh-TW\": \"驳回类型\"}', 'wf_reject_type', 1, 2, 305, 1, '待办审批驳回类型字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000048, 0, '分支操作符', 'wf_branch_operator', NULL, NULL, '{\"en-US\": \"wf_branch_operator\", \"ja-JP\": \"分支操作符\", \"ko-KR\": \"wf_branch_operator\", \"zh-CN\": \"分支操作符\", \"zh-TW\": \"分支操作符\"}', 'wf_branch_operator', 1, 7, 306, 1, '审批分支规则操作符字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000049, 0, '编码规则启用状态', 'encode_rule_enabled', NULL, NULL, '{\"en-US\": \"encode_rule_enabled\", \"ja-JP\": \"编码规则启用状态\", \"ko-KR\": \"encode_rule_enabled\", \"zh-CN\": \"编码规则启用状态\", \"zh-TW\": \"编码规则启用状态\"}', 'encode_rule_enabled', 1, 2, 307, 1, '编码规则状态字典', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000050, 0, '编码规则段类型', 'encode_rule_segment_type', NULL, NULL, '{\"en-US\": \"encode_rule_segment_type\", \"ja-JP\": \"编码规则段类型\", \"ko-KR\": \"encode_rule_segment_type\", \"zh-CN\": \"编码规则段类型\", \"zh-TW\": \"编码规则段类型\"}', 'encode_rule_segment_type', 1, 4, 308, 1, '编码规则段类型字典（与当前前端值域一致）', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000051, 0, '编码规则序列重置类型', 'encode_rule_seq_reset_type', NULL, NULL, '{\"en-US\": \"encode_rule_seq_reset_type\", \"ja-JP\": \"编码规则序列重置类型\", \"ko-KR\": \"encode_rule_seq_reset_type\", \"zh-CN\": \"编码规则序列重置类型\", \"zh-TW\": \"编码规则序列重置类型\"}', 'encode_rule_seq_reset_type', 1, 4, 309, 1, '编码规则序列重置类型字典（与当前前端值域一致）', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000058, 5000000000000000043, '自定义表单', 'custom_form', NULL, '1', '{\"en-US\": \"Custom Form\", \"zh-CN\": \"自定义表单\"}', 'wf_task_form_type/custom_form', 2, 0, 1, 0, '审批任务配置表单类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000059, 5000000000000000043, '低代码表单', 'low_code_form', NULL, '2', '{\"en-US\": \"Low Code Form\", \"zh-CN\": \"低代码表单\"}', 'wf_task_form_type/low_code_form', 2, 0, 2, 0, '审批任务配置表单类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000060, 5000000000000000044, '待处理', 'pending', NULL, '0', '{\"en-US\": \"Pending\", \"zh-CN\": \"待处理\"}', 'wf_execution_status/pending', 2, 0, 1, 0, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000061, 5000000000000000044, '审批中', 'processing', NULL, '1', '{\"en-US\": \"Processing\", \"zh-CN\": \"审批中\"}', 'wf_execution_status/processing', 2, 0, 2, 0, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"processing\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000062, 5000000000000000044, '审批完成', 'finished', NULL, '2', '{\"en-US\": \"Finished\", \"zh-CN\": \"审批完成\"}', 'wf_execution_status/finished', 2, 0, 3, 0, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"success\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000063, 5000000000000000044, '驳回', 'rejected', NULL, '3', '{\"en-US\": \"Rejected\", \"zh-CN\": \"驳回\"}', 'wf_execution_status/rejected', 2, 0, 4, 0, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"error\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000064, 5000000000000000045, '会签', 'countersign', NULL, '1', '{\"en-US\": \"Countersign\", \"zh-CN\": \"会签\"}', 'wf_approve_type/countersign', 2, 0, 1, 0, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000065, 5000000000000000045, '或签', 'or_sign', NULL, '2', '{\"en-US\": \"Any One Approves\", \"zh-CN\": \"或签\"}', 'wf_approve_type/or_sign', 2, 0, 2, 0, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000066, 5000000000000000045, '抄送', 'cc', NULL, '3', '{\"en-US\": \"Carbon Copy\", \"zh-CN\": \"抄送\"}', 'wf_approve_type/cc', 2, 0, 3, 0, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000067, 5000000000000000045, '投票', 'vote', NULL, '4', '{\"en-US\": \"Vote\", \"zh-CN\": \"投票\"}', 'wf_approve_type/vote', 2, 0, 4, 0, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000068, 5000000000000000045, '顺序审批', 'sequential', NULL, '5', '{\"en-US\": \"Sequential Approval\", \"zh-CN\": \"顺序审批\"}', 'wf_approve_type/sequential', 2, 0, 5, 0, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000069, 5000000000000000046, '用户', 'user', NULL, '1', '{\"en-US\": \"User\", \"zh-CN\": \"用户\"}', 'wf_approver_type/user', 2, 0, 1, 0, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000070, 5000000000000000046, '部门', 'department', NULL, '2', '{\"en-US\": \"Department\", \"zh-CN\": \"部门\"}', 'wf_approver_type/department', 2, 0, 2, 0, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000071, 5000000000000000046, '角色', 'role', NULL, '3', '{\"en-US\": \"Role\", \"zh-CN\": \"角色\"}', 'wf_approver_type/role', 2, 0, 3, 0, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000072, 5000000000000000046, '岗位', 'position', NULL, '4', '{\"en-US\": \"Position\", \"zh-CN\": \"岗位\"}', 'wf_approver_type/position', 2, 0, 4, 0, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000073, 5000000000000000047, '驳回结束当前审批流程', 'reject_finish', NULL, '1', '{\"en-US\": \"Reject And Finish\", \"zh-CN\": \"驳回结束当前审批流程\"}', 'wf_reject_type/reject_finish', 2, 0, 1, 0, '待办审批驳回类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000074, 5000000000000000047, '退回上一节点重新审批', 'reject_previous_node', NULL, '2', '{\"en-US\": \"Back To Previous Node\", \"zh-CN\": \"退回上一节点重新审批\"}', 'wf_reject_type/reject_previous_node', 2, 0, 2, 0, '待办审批驳回类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000075, 5000000000000000048, '等于', 'eq', NULL, '=', '{\"en-US\": \"Equals\", \"zh-CN\": \"等于\"}', 'wf_branch_operator/eq', 2, 0, 1, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000076, 5000000000000000048, '不等于', 'ne', NULL, '!=', '{\"en-US\": \"Not Equals\", \"zh-CN\": \"不等于\"}', 'wf_branch_operator/ne', 2, 0, 2, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000077, 5000000000000000048, '大于', 'gt', NULL, '>', '{\"en-US\": \"Greater Than\", \"zh-CN\": \"大于\"}', 'wf_branch_operator/gt', 2, 0, 3, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000078, 5000000000000000048, '大于等于', 'ge', NULL, '>=', '{\"en-US\": \"Greater Or Equal\", \"zh-CN\": \"大于等于\"}', 'wf_branch_operator/ge', 2, 0, 4, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000079, 5000000000000000048, '小于', 'lt', NULL, '<', '{\"en-US\": \"Less Than\", \"zh-CN\": \"小于\"}', 'wf_branch_operator/lt', 2, 0, 5, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000080, 5000000000000000048, '小于等于', 'le', NULL, '<=', '{\"en-US\": \"Less Or Equal\", \"zh-CN\": \"小于等于\"}', 'wf_branch_operator/le', 2, 0, 6, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000081, 5000000000000000048, '包含', 'contains', NULL, 'contains', '{\"en-US\": \"Contains\", \"zh-CN\": \"包含\"}', 'wf_branch_operator/contains', 2, 0, 7, 0, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000082, 5000000000000000049, '启用', 'enabled', NULL, 'true', '{\"en-US\": \"Enabled\", \"zh-CN\": \"启用\"}', 'encode_rule_enabled/enabled', 2, 0, 1, 0, '编码规则启用状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"success\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000083, 5000000000000000049, '禁用', 'disabled', NULL, 'false', '{\"en-US\": \"Disabled\", \"zh-CN\": \"禁用\"}', 'encode_rule_enabled/disabled', 2, 0, 2, 0, '编码规则启用状态', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000084, 5000000000000000050, '固定值', 'fixed', NULL, 'FIXED', '{\"en-US\": \"Fixed\", \"zh-CN\": \"固定值\"}', 'encode_rule_segment_type/fixed', 2, 0, 1, 0, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000085, 5000000000000000050, '日期', 'date', NULL, 'DATE', '{\"en-US\": \"Date\", \"zh-CN\": \"日期\"}', 'encode_rule_segment_type/date', 2, 0, 2, 0, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000086, 5000000000000000050, '流水号', 'seq', NULL, 'SEQ', '{\"en-US\": \"Sequence\", \"zh-CN\": \"流水号\"}', 'encode_rule_segment_type/seq', 2, 0, 3, 0, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000087, 5000000000000000050, '自定义', 'custom', NULL, 'CUSTOM', '{\"en-US\": \"Custom\", \"zh-CN\": \"自定义\"}', 'encode_rule_segment_type/custom', 2, 0, 4, 0, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000088, 5000000000000000051, '永不重置', 'never', NULL, '0', '{\"en-US\": \"Never\", \"zh-CN\": \"永不重置\"}', 'encode_rule_seq_reset_type/never', 2, 0, 1, 0, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000089, 5000000000000000051, '按年重置', 'yearly', NULL, '1', '{\"en-US\": \"Yearly\", \"zh-CN\": \"按年重置\"}', 'encode_rule_seq_reset_type/yearly', 2, 0, 2, 0, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000090, 5000000000000000051, '按月重置', 'monthly', NULL, '2', '{\"en-US\": \"Monthly\", \"zh-CN\": \"按月重置\"}', 'encode_rule_seq_reset_type/monthly', 2, 0, 3, 0, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000091, 5000000000000000051, '按日重置', 'daily', NULL, '3', '{\"en-US\": \"Daily\", \"zh-CN\": \"按日重置\"}', 'encode_rule_seq_reset_type/daily', 2, 0, 4, 0, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 11:54:42', NULL, '2026-04-13 12:00:03', 1, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000121, 5000000000000000043, '自定义表单', 'custom_form', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"自定义表单\", \"ko-KR\": \"1\", \"zh-CN\": \"自定义表单\", \"zh-TW\": \"自定义表单\"}', 'wf_task_form_type/custom_form', 2, 0, 1, 1, '审批任务配置表单类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000122, 5000000000000000043, '低代码表单', 'low_code_form', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"低代码表单\", \"ko-KR\": \"2\", \"zh-CN\": \"低代码表单\", \"zh-TW\": \"低代码表单\"}', 'wf_task_form_type/low_code_form', 2, 0, 2, 1, '审批任务配置表单类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000123, 5000000000000000044, '待处理', 'pending', NULL, '0', '{\"en-US\": \"0\", \"ja-JP\": \"待处理\", \"ko-KR\": \"0\", \"zh-CN\": \"待处理\", \"zh-TW\": \"待处理\"}', 'wf_execution_status/pending', 2, 0, 1, 1, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000124, 5000000000000000044, '审批中', 'processing', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"审批中\", \"ko-KR\": \"1\", \"zh-CN\": \"审批中\", \"zh-TW\": \"审批中\"}', 'wf_execution_status/processing', 2, 0, 2, 1, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\": \"processing\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000125, 5000000000000000044, '审批完成', 'finished', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"审批完成\", \"ko-KR\": \"2\", \"zh-CN\": \"审批完成\", \"zh-TW\": \"审批完成\"}', 'wf_execution_status/finished', 2, 0, 3, 1, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\": \"success\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000126, 5000000000000000044, '驳回', 'rejected', NULL, '3', '{\"en-US\": \"3\", \"ja-JP\": \"驳回\", \"ko-KR\": \"3\", \"zh-CN\": \"驳回\", \"zh-TW\": \"驳回\"}', 'wf_execution_status/rejected', 2, 0, 4, 1, '审批执行状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\": \"error\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000127, 5000000000000000045, '会签', 'countersign', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"会签\", \"ko-KR\": \"1\", \"zh-CN\": \"会签\", \"zh-TW\": \"会签\"}', 'wf_approve_type/countersign', 2, 0, 1, 1, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000128, 5000000000000000045, '或签', 'or_sign', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"或签\", \"ko-KR\": \"2\", \"zh-CN\": \"或签\", \"zh-TW\": \"或签\"}', 'wf_approve_type/or_sign', 2, 0, 2, 1, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000129, 5000000000000000045, '抄送', 'cc', NULL, '3', '{\"en-US\": \"3\", \"ja-JP\": \"抄送\", \"ko-KR\": \"3\", \"zh-CN\": \"抄送\", \"zh-TW\": \"抄送\"}', 'wf_approve_type/cc', 2, 0, 3, 1, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000130, 5000000000000000045, '投票', 'vote', NULL, '4', '{\"en-US\": \"4\", \"ja-JP\": \"投票\", \"ko-KR\": \"4\", \"zh-CN\": \"投票\", \"zh-TW\": \"投票\"}', 'wf_approve_type/vote', 2, 0, 4, 1, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000131, 5000000000000000045, '顺序审批', 'sequential', NULL, '5', '{\"en-US\": \"5\", \"ja-JP\": \"顺序审批\", \"ko-KR\": \"5\", \"zh-CN\": \"顺序审批\", \"zh-TW\": \"顺序审批\"}', 'wf_approve_type/sequential', 2, 0, 5, 1, '审批节点审批类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000132, 5000000000000000046, '用户', 'user', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"用户\", \"ko-KR\": \"1\", \"zh-CN\": \"用户\", \"zh-TW\": \"用户\"}', 'wf_approver_type/user', 2, 0, 1, 1, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000133, 5000000000000000046, '部门', 'department', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"部门\", \"ko-KR\": \"2\", \"zh-CN\": \"部门\", \"zh-TW\": \"部门\"}', 'wf_approver_type/department', 2, 0, 2, 1, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000134, 5000000000000000046, '角色', 'role', NULL, '3', '{\"en-US\": \"3\", \"ja-JP\": \"角色\", \"ko-KR\": \"3\", \"zh-CN\": \"角色\", \"zh-TW\": \"角色\"}', 'wf_approver_type/role', 2, 0, 3, 1, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000135, 5000000000000000046, '岗位', 'position', NULL, '4', '{\"en-US\": \"4\", \"ja-JP\": \"岗位\", \"ko-KR\": \"4\", \"zh-CN\": \"岗位\", \"zh-TW\": \"岗位\"}', 'wf_approver_type/position', 2, 0, 4, 1, '审批节点审批人来源', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000136, 5000000000000000047, '驳回结束当前审批流程', 'reject_finish', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"驳回结束当前审批流程\", \"ko-KR\": \"1\", \"zh-CN\": \"驳回结束当前审批流程\", \"zh-TW\": \"驳回结束当前审批流程\"}', 'wf_reject_type/reject_finish', 2, 0, 1, 1, '待办审批驳回类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000137, 5000000000000000047, '退回上一节点重新审批', 'reject_previous_node', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"退回上一节点重新审批\", \"ko-KR\": \"2\", \"zh-CN\": \"退回上一节点重新审批\", \"zh-TW\": \"退回上一节点重新审批\"}', 'wf_reject_type/reject_previous_node', 2, 0, 2, 1, '待办审批驳回类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000138, 5000000000000000048, '等于', 'eq', NULL, '=', '{\"en-US\": \"=\", \"ja-JP\": \"等于\", \"ko-KR\": \"=\", \"zh-CN\": \"等于\", \"zh-TW\": \"等于\"}', 'wf_branch_operator/eq', 2, 0, 1, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000139, 5000000000000000048, '不等于', 'ne', NULL, '!=', '{\"en-US\": \"!=\", \"ja-JP\": \"不等于\", \"ko-KR\": \"!=\", \"zh-CN\": \"不等于\", \"zh-TW\": \"不等于\"}', 'wf_branch_operator/ne', 2, 0, 2, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000140, 5000000000000000048, '大于', 'gt', NULL, '>', '{\"en-US\": \">\", \"ja-JP\": \"大于\", \"ko-KR\": \">\", \"zh-CN\": \"大于\", \"zh-TW\": \"大于\"}', 'wf_branch_operator/gt', 2, 0, 3, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000141, 5000000000000000048, '大于等于', 'ge', NULL, '>=', '{\"en-US\": \">=\", \"ja-JP\": \"大于等于\", \"ko-KR\": \">=\", \"zh-CN\": \"大于等于\", \"zh-TW\": \"大于等于\"}', 'wf_branch_operator/ge', 2, 0, 4, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000142, 5000000000000000048, '小于', 'lt', NULL, '<', '{\"en-US\": \"<\", \"ja-JP\": \"小于\", \"ko-KR\": \"<\", \"zh-CN\": \"小于\", \"zh-TW\": \"小于\"}', 'wf_branch_operator/lt', 2, 0, 5, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000143, 5000000000000000048, '小于等于', 'le', NULL, '<=', '{\"en-US\": \"<=\", \"ja-JP\": \"小于等于\", \"ko-KR\": \"<=\", \"zh-CN\": \"小于等于\", \"zh-TW\": \"小于等于\"}', 'wf_branch_operator/le', 2, 0, 6, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000144, 5000000000000000048, '包含', 'contains', NULL, 'contains', '{\"en-US\": \"contains\", \"ja-JP\": \"包含\", \"ko-KR\": \"contains\", \"zh-CN\": \"包含\", \"zh-TW\": \"包含\"}', 'wf_branch_operator/contains', 2, 0, 7, 1, '审批分支规则操作符', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000145, 5000000000000000049, '启用', 'enabled', NULL, 'true', '{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"사용\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}', 'encode_rule_enabled/enabled', 2, 0, 1, 1, '编码规则启用状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:54:50', 0, '{\"color\": \"success\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000146, 5000000000000000049, '禁用', 'disabled', NULL, 'false', '{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"사용 안 함\", \"zh-CN\": \"禁用\", \"zh-TW\": \"停用\"}', 'encode_rule_enabled/disabled', 2, 0, 2, 1, '编码规则启用状态', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:54:50', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000147, 5000000000000000050, '固定值', 'fixed', NULL, 'FIXED', '{\"en-US\": \"FIXED\", \"ja-JP\": \"固定值\", \"ko-KR\": \"FIXED\", \"zh-CN\": \"固定值\", \"zh-TW\": \"固定值\"}', 'encode_rule_segment_type/fixed', 2, 0, 1, 1, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000148, 5000000000000000050, '日期', 'date', NULL, 'DATE', '{\"en-US\": \"DATE\", \"ja-JP\": \"日期\", \"ko-KR\": \"DATE\", \"zh-CN\": \"日期\", \"zh-TW\": \"日期\"}', 'encode_rule_segment_type/date', 2, 0, 2, 1, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000149, 5000000000000000050, '流水号', 'seq', NULL, 'SEQ', '{\"en-US\": \"SEQ\", \"ja-JP\": \"流水号\", \"ko-KR\": \"SEQ\", \"zh-CN\": \"流水号\", \"zh-TW\": \"流水号\"}', 'encode_rule_segment_type/seq', 2, 0, 3, 1, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000150, 5000000000000000050, '自定义', 'custom', NULL, 'CUSTOM', '{\"en-US\": \"CUSTOM\", \"ja-JP\": \"自定义\", \"ko-KR\": \"CUSTOM\", \"zh-CN\": \"自定义\", \"zh-TW\": \"自定义\"}', 'encode_rule_segment_type/custom', 2, 0, 4, 1, '编码规则段类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000151, 5000000000000000051, '永不重置', 'never', NULL, '0', '{\"en-US\": \"0\", \"ja-JP\": \"永不重置\", \"ko-KR\": \"0\", \"zh-CN\": \"永不重置\", \"zh-TW\": \"永不重置\"}', 'encode_rule_seq_reset_type/never', 2, 0, 1, 1, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000152, 5000000000000000051, '按年重置', 'yearly', NULL, '1', '{\"en-US\": \"1\", \"ja-JP\": \"按年重置\", \"ko-KR\": \"1\", \"zh-CN\": \"按年重置\", \"zh-TW\": \"按年重置\"}', 'encode_rule_seq_reset_type/yearly', 2, 0, 2, 1, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000153, 5000000000000000051, '按月重置', 'monthly', NULL, '2', '{\"en-US\": \"2\", \"ja-JP\": \"按月重置\", \"ko-KR\": \"2\", \"zh-CN\": \"按月重置\", \"zh-TW\": \"按月重置\"}', 'encode_rule_seq_reset_type/monthly', 2, 0, 3, 1, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000154, 5000000000000000051, '按日重置', 'daily', NULL, '3', '{\"en-US\": \"3\", \"ja-JP\": \"按日重置\", \"ko-KR\": \"3\", \"zh-CN\": \"按日重置\", \"zh-TW\": \"按日重置\"}', 'encode_rule_seq_reset_type/daily', 2, 0, 4, 1, '编码规则序列重置类型', 1993479636925403138, NULL, '2026-04-13 12:00:03', NULL, '2026-04-26 20:56:49', 0, '{\"color\":\"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000155, 0, '审批分类', 'wf_task_category', NULL, NULL, '{\"en-US\": \"wf_task_category\", \"ja-JP\": \"审批分类\", \"ko-KR\": \"wf_task_category\", \"zh-CN\": \"审批分类\", \"zh-TW\": \"审批分类\"}', 'wf_task_category', 1, 0, 310, 1, '审批任务分类字典', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000156, 5000000000000000155, '通用类', 'general', NULL, 'general', '{\"en-US\": \"general\", \"ja-JP\": \"通用类\", \"ko-KR\": \"general\", \"zh-CN\": \"通用类\", \"zh-TW\": \"通用类\"}', 'wf_task_category/general', 2, 0, 1, 1, '审批任务分类字典项', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000157, 5000000000000000155, '人事类', 'hr', NULL, 'hr', '{\"en-US\": \"hr\", \"ja-JP\": \"人事类\", \"ko-KR\": \"hr\", \"zh-CN\": \"人事类\", \"zh-TW\": \"人事类\"}', 'wf_task_category/hr', 2, 0, 2, 1, '审批任务分类字典项', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000158, 5000000000000000155, '合同类', 'contract', NULL, 'contract', '{\"en-US\": \"contract\", \"ja-JP\": \"合同类\", \"ko-KR\": \"contract\", \"zh-CN\": \"合同类\", \"zh-TW\": \"合同类\"}', 'wf_task_category/contract', 2, 0, 3, 1, '审批任务分类字典项', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000159, 5000000000000000155, '财务类', 'finance', NULL, 'finance', '{\"en-US\": \"finance\", \"ja-JP\": \"财务类\", \"ko-KR\": \"finance\", \"zh-CN\": \"财务类\", \"zh-TW\": \"财务类\"}', 'wf_task_category/finance', 2, 0, 4, 1, '审批任务分类字典项', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000160, 5000000000000000155, '项目类', 'project', NULL, 'project', '{\"en-US\": \"project\", \"ja-JP\": \"项目类\", \"ko-KR\": \"project\", \"zh-CN\": \"项目类\", \"zh-TW\": \"项目类\"}', 'wf_task_category/project', 2, 0, 5, 1, '审批任务分类字典项', 1993479636925403138, NULL, '2026-04-20 21:29:27', NULL, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000161, 0, '用户来源', 'user_source', NULL, NULL, '{\"zh-CN\":\"用户来源\",\"en-US\":\"User Source\",\"ja-JP\":\"ユーザーソース\",\"ko-KR\":\"사용자 출처\",\"zh-TW\":\"用戶來源\"}', 'user_source', 1, 4, 101, 1, '用户来源字典', 1993479636925403138, NULL, '2026-04-22 19:10:11', NULL, '2026-04-26 20:30:58', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000162, 5000000000000000161, '站点创建', 'site_created', NULL, '1', '{\"zh-CN\":\"站点创建\",\"en-US\":\"Created In Site\",\"ja-JP\":\"サイト作成\",\"ko-KR\":\"사이트 생성\",\"zh-TW\":\"站點創建\"}', 'user_source/site_created', 2, 0, 1, 1, '站点内创建', 1993479636925403138, NULL, '2026-04-22 19:10:11', NULL, '2026-04-26 20:30:58', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000163, 5000000000000000161, '站点导入', 'site_imported', NULL, '2', '{\"zh-CN\":\"站点导入\",\"en-US\":\"Imported In Site\",\"ja-JP\":\"サイトインポート\",\"ko-KR\":\"사이트 가져오기\",\"zh-TW\":\"站點導入\"}', 'user_source/site_imported', 2, 0, 2, 1, '站点内导入', 1993479636925403138, NULL, '2026-04-22 19:10:11', NULL, '2026-04-26 20:30:58', 0, '{\"color\": \"gold\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000164, 5000000000000000161, '第三方同步', 'third_party_sync', NULL, '3', '{\"zh-CN\":\"第三方同步\",\"en-US\":\"Third Party Sync\",\"ja-JP\":\"サードパーティ同期\",\"ko-KR\":\"타사 동기화\",\"zh-TW\":\"第三方同步\"}', 'user_source/third_party_sync', 2, 0, 3, 1, '第三方系统同步', 1993479636925403138, NULL, '2026-04-22 19:10:11', NULL, '2026-04-26 20:30:58', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000165, 5000000000000000161, '自主注册', 'self_registered', NULL, '4', '{\"zh-CN\":\"自主注册\",\"en-US\":\"Self Registered\",\"ja-JP\":\"自己登録\",\"ko-KR\":\"자체 등록\",\"zh-TW\":\"自主註冊\"}', 'user_source/self_registered', 2, 0, 4, 1, '用户自主注册', 1993479636925403138, NULL, '2026-04-22 19:10:11', NULL, '2026-04-26 20:30:58', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000166, 0, '供应商合作状态', 'supplier_cooperation_status', 5, NULL, '{\"en-US\": \"supplier_cooperation_status\", \"ja-JP\": \"供应商合作状态\", \"ko-KR\": \"supplier_cooperation_status\", \"zh-CN\": \"供应商合作状态\", \"zh-TW\": \"供应商合作状态\"}', 'supplier_cooperation_status', 1, 4, 210, 1, '供应商主数据合作状态', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000167, 0, '供应商信用等级', 'supplier_credit_level', 5, NULL, '{\"en-US\": \"supplier_credit_level\", \"ja-JP\": \"供应商信用等级\", \"ko-KR\": \"supplier_credit_level\", \"zh-CN\": \"供应商信用等级\", \"zh-TW\": \"供应商信用等级\"}', 'supplier_credit_level', 1, 4, 211, 1, '供应商主数据信用等级', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000168, 0, '供应商风险等级', 'supplier_risk_level', 5, NULL, '{\"en-US\": \"supplier_risk_level\", \"ja-JP\": \"供应商风险等级\", \"ko-KR\": \"supplier_risk_level\", \"zh-CN\": \"供应商风险等级\", \"zh-TW\": \"供应商风险等级\"}', 'supplier_risk_level', 1, 0, 212, 1, '供应商主数据风险等级；仅建类型，不预置业务值', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000169, 0, '供应商分级', 'supplier_level', 5, NULL, '{\"en-US\": \"supplier_level\", \"ja-JP\": \"供应商分级\", \"ko-KR\": \"supplier_level\", \"zh-CN\": \"供应商分级\", \"zh-TW\": \"供应商分级\"}', 'supplier_level', 1, 3, 213, 1, '供应商主数据分级', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000170, 0, '供应商审查状态', 'supplier_review_status', 5, NULL, '{\"en-US\": \"supplier_review_status\", \"ja-JP\": \"供应商审查状态\", \"ko-KR\": \"supplier_review_status\", \"zh-CN\": \"供应商审查状态\", \"zh-TW\": \"供应商审查状态\"}', 'supplier_review_status', 1, 4, 214, 1, '供应商资质审查状态', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000171, 0, '供应商企业性质', 'supplier_enterprise_nature', 5, NULL, '{\"en-US\": \"supplier_enterprise_nature\", \"ja-JP\": \"供应商企业性质\", \"ko-KR\": \"supplier_enterprise_nature\", \"zh-CN\": \"供应商企业性质\", \"zh-TW\": \"供应商企业性质\"}', 'supplier_enterprise_nature', 1, 4, 215, 1, '供应商企业性质', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000172, 0, '供应商行业分类', 'supplier_industry_category', 5, NULL, '{\"en-US\": \"supplier_industry_category\", \"ja-JP\": \"供应商行业分类\", \"ko-KR\": \"supplier_industry_category\", \"zh-CN\": \"供应商行业分类\", \"zh-TW\": \"供应商行业分类\"}', 'supplier_industry_category', 1, 0, 216, 1, '供应商行业分类；仅建类型，不预置业务值', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000173, 0, '供应商发票类型', 'supplier_invoice_type', 5, NULL, '{\"en-US\": \"supplier_invoice_type\", \"ja-JP\": \"供应商发票类型\", \"ko-KR\": \"supplier_invoice_type\", \"zh-CN\": \"供应商发票类型\", \"zh-TW\": \"供应商发票类型\"}', 'supplier_invoice_type', 1, 0, 217, 1, '供应商发票类型；仅建类型，不预置业务值', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000174, 0, '供应商资质类型', 'supplier_qualification_type', 5, NULL, '{\"en-US\": \"supplier_qualification_type\", \"ja-JP\": \"供应商资质类型\", \"ko-KR\": \"supplier_qualification_type\", \"zh-CN\": \"供应商资质类型\", \"zh-TW\": \"供应商资质类型\"}', 'supplier_qualification_type', 1, 0, 218, 1, '供应商资质类型；仅建类型，不预置业务值', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000181, 5000000000000000166, '淘汰', 'eliminated', 5, '4', '{\"en-US\": \"4\", \"ja-JP\": \"淘汰\", \"ko-KR\": \"4\", \"zh-CN\": \"淘汰\", \"zh-TW\": \"淘汰\"}', 'supplier_cooperation_status/eliminated', 2, 0, 4, 1, '已淘汰', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"red\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000182, 5000000000000000166, '正式', 'formal', 5, '2', '{\"en-US\": \"2\", \"ja-JP\": \"正式\", \"ko-KR\": \"2\", \"zh-CN\": \"正式\", \"zh-TW\": \"正式\"}', 'supplier_cooperation_status/formal', 2, 0, 2, 1, '正式供应商', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000183, 5000000000000000166, '潜在', 'potential', 5, '1', '{\"en-US\": \"1\", \"ja-JP\": \"潜在\", \"ko-KR\": \"1\", \"zh-CN\": \"潜在\", \"zh-TW\": \"潜在\"}', 'supplier_cooperation_status/potential', 2, 0, 1, 1, '潜在供应商', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000184, 5000000000000000166, '暂停', 'suspended', 5, '3', '{\"en-US\": \"3\", \"ja-JP\": \"暂停\", \"ko-KR\": \"3\", \"zh-CN\": \"暂停\", \"zh-TW\": \"暂停\"}', 'supplier_cooperation_status/suspended', 2, 0, 3, 1, '暂停合作', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"orange\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000185, 5000000000000000167, 'A', 'credit_a', 5, 'A', '{\"en-US\": \"A\", \"ja-JP\": \"A\", \"ko-KR\": \"A\", \"zh-CN\": \"A\", \"zh-TW\": \"A\"}', 'supplier_credit_level/credit_a', 2, 0, 1, 1, '信用等级 A', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000186, 5000000000000000167, 'B', 'credit_b', 5, 'B', '{\"en-US\": \"B\", \"ja-JP\": \"B\", \"ko-KR\": \"B\", \"zh-CN\": \"B\", \"zh-TW\": \"B\"}', 'supplier_credit_level/credit_b', 2, 0, 2, 1, '信用等级 B', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000187, 5000000000000000167, 'C', 'credit_c', 5, 'C', '{\"en-US\": \"C\", \"ja-JP\": \"C\", \"ko-KR\": \"C\", \"zh-CN\": \"C\", \"zh-TW\": \"C\"}', 'supplier_credit_level/credit_c', 2, 0, 3, 1, '信用等级 C', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"orange\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000188, 5000000000000000167, 'D', 'credit_d', 5, 'D', '{\"en-US\": \"D\", \"ja-JP\": \"D\", \"ko-KR\": \"D\", \"zh-CN\": \"D\", \"zh-TW\": \"D\"}', 'supplier_credit_level/credit_d', 2, 0, 4, 1, '信用等级 D', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"red\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000189, 5000000000000000171, '外资', 'foreign', 5, '3', '{\"en-US\": \"3\", \"ja-JP\": \"外资\", \"ko-KR\": \"3\", \"zh-CN\": \"外资\", \"zh-TW\": \"外资\"}', 'supplier_enterprise_nature/foreign', 2, 0, 3, 1, '外资企业', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000190, 5000000000000000171, '合资', 'joint_venture', 5, '4', '{\"en-US\": \"4\", \"ja-JP\": \"合资\", \"ko-KR\": \"4\", \"zh-CN\": \"合资\", \"zh-TW\": \"合资\"}', 'supplier_enterprise_nature/joint_venture', 2, 0, 4, 1, '合资企业', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"cyan\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000191, 5000000000000000171, '民营', 'private', 5, '2', '{\"en-US\": \"2\", \"ja-JP\": \"民营\", \"ko-KR\": \"2\", \"zh-CN\": \"民营\", \"zh-TW\": \"民营\"}', 'supplier_enterprise_nature/private', 2, 0, 2, 1, '民营企业', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000192, 5000000000000000171, '国企', 'state_owned', 5, '1', '{\"en-US\": \"1\", \"ja-JP\": \"国企\", \"ko-KR\": \"1\", \"zh-CN\": \"国企\", \"zh-TW\": \"国企\"}', 'supplier_enterprise_nature/state_owned', 2, 0, 1, 1, '国有企业', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000193, 5000000000000000169, '核心', 'core', 5, '2', '{\"en-US\": \"2\", \"ja-JP\": \"核心\", \"ko-KR\": \"2\", \"zh-CN\": \"核心\", \"zh-TW\": \"核心\"}', 'supplier_level/core', 2, 0, 2, 1, '核心供应商', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000194, 5000000000000000169, '一般', 'normal', 5, '3', '{\"en-US\": \"3\", \"ja-JP\": \"一般\", \"ko-KR\": \"3\", \"zh-CN\": \"一般\", \"zh-TW\": \"一般\"}', 'supplier_level/normal', 2, 0, 3, 1, '一般供应商', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000195, 5000000000000000169, '战略', 'strategic', 5, '1', '{\"en-US\": \"1\", \"ja-JP\": \"战略\", \"ko-KR\": \"1\", \"zh-CN\": \"战略\", \"zh-TW\": \"战略\"}', 'supplier_level/strategic', 2, 0, 1, 1, '战略供应商', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"purple\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000196, 5000000000000000170, '已审查', 'review_approved', 5, '3', '{\"en-US\": \"3\", \"ja-JP\": \"已审查\", \"ko-KR\": \"3\", \"zh-CN\": \"已审查\", \"zh-TW\": \"已审查\"}', 'supplier_review_status/review_approved', 2, 0, 3, 1, '已审查', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"green\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000197, 5000000000000000170, '无需审查', 'review_none', 5, '0', '{\"en-US\": \"0\", \"ja-JP\": \"无需审查\", \"ko-KR\": \"0\", \"zh-CN\": \"无需审查\", \"zh-TW\": \"无需审查\"}', 'supplier_review_status/review_none', 2, 0, 0, 1, '无需审查', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"default\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000198, 5000000000000000170, '未审查', 'review_pending', 5, '1', '{\"en-US\": \"1\", \"ja-JP\": \"未审查\", \"ko-KR\": \"1\", \"zh-CN\": \"未审查\", \"zh-TW\": \"未审查\"}', 'supplier_review_status/review_pending', 2, 0, 1, 1, '未审查', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"orange\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000199, 5000000000000000170, '审查中', 'review_processing', 5, '2', '{\"en-US\": \"2\", \"ja-JP\": \"审查中\", \"ko-KR\": \"2\", \"zh-CN\": \"审查中\", \"zh-TW\": \"审查中\"}', 'supplier_review_status/review_processing', 2, 0, 2, 1, '审查中', 1993479636925403138, 1993479637244170242, '2026-04-26 16:26:06', 1993479637244170242, '2026-04-26 20:56:49', 0, '{\"color\": \"blue\"}');
INSERT INTO `sys_dict` VALUES (5000000000000000212, 0, '调用状态', 'callStatus', NULL, NULL, '{\"en-US\": \"Call Status\", \"ja-JP\": \"呼び出し状態\", \"ko-KR\": \"호출 상태\", \"zh-CN\": \"调用状态\", \"zh-TW\": \"調用狀態\"}', '/callStatus', 1, 2, 205, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000213, 0, '调用方式', 'integrationCallMethod', NULL, NULL, '{\"en-US\": \"Call Method\", \"ja-JP\": \"呼び出し方式\", \"ko-KR\": \"호출 방식\", \"zh-CN\": \"调用方式\", \"zh-TW\": \"調用方式\"}', '/integrationCallMethod', 1, 2, 204, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000214, 0, '接口调用方向', 'integrationDirection', NULL, NULL, '{\"en-US\": \"Integration Direction\", \"ja-JP\": \"連携呼び出し方向\", \"ko-KR\": \"연동 호출 방향\", \"zh-CN\": \"接口调用方向\", \"zh-TW\": \"介面調用方向\"}', '/integrationDirection', 1, 2, 201, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000215, 0, '接口状态', 'integrationStatus', NULL, NULL, '{\"en-US\": \"Integration Status\", \"ja-JP\": \"連携状態\", \"ko-KR\": \"연동 상태\", \"zh-CN\": \"接口状态\", \"zh-TW\": \"介面狀態\"}', '/integrationStatus', 1, 2, 202, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000216, 0, '第三方系统状态', 'thirdSystemStatus', NULL, NULL, '{\"en-US\": \"Third System Status\", \"ja-JP\": \"サードパーティシステム状態\", \"ko-KR\": \"타사 시스템 상태\", \"zh-CN\": \"第三方系统状态\", \"zh-TW\": \"第三方系統狀態\"}', '/thirdSystemStatus', 1, 2, 203, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000219, 5000000000000000212, '失败', 'callStatusFailed', NULL, 'FAILED', '{\"en-US\": \"Failed\", \"ja-JP\": \"失敗\", \"ko-KR\": \"실패\", \"zh-CN\": \"失败\", \"zh-TW\": \"失敗\"}', '/callStatus/callStatusFailed', 2, 0, 2, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000220, 5000000000000000212, '成功', 'callStatusSuccess', NULL, 'SUCCESS', '{\"en-US\": \"Success\", \"ja-JP\": \"成功\", \"ko-KR\": \"성공\", \"zh-CN\": \"成功\", \"zh-TW\": \"成功\"}', '/callStatus/callStatusSuccess', 2, 0, 1, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000221, 5000000000000000213, 'HTTP', 'integrationCallMethodHttp', NULL, 'HTTP', '{\"en-US\": \"HTTP\", \"ja-JP\": \"HTTP\", \"ko-KR\": \"HTTP\", \"zh-CN\": \"HTTP\", \"zh-TW\": \"HTTP\"}', '/integrationCallMethod/integrationCallMethodHttp', 2, 0, 1, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000222, 5000000000000000213, 'TCP', 'integrationCallMethodTcp', NULL, 'TCP', '{\"en-US\": \"TCP\", \"ja-JP\": \"TCP\", \"ko-KR\": \"TCP\", \"zh-CN\": \"TCP\", \"zh-TW\": \"TCP\"}', '/integrationCallMethod/integrationCallMethodTcp', 2, 0, 2, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000223, 5000000000000000214, '外调内', 'integrationDirectionInbound', NULL, 'INBOUND', '{\"en-US\": \"External to Internal\", \"ja-JP\": \"外部から内部\", \"ko-KR\": \"외부에서 내부\", \"zh-CN\": \"外调内\", \"zh-TW\": \"外調內\"}', '/integrationDirection/integrationDirectionInbound', 2, 0, 1, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000224, 5000000000000000214, '内调外', 'integrationDirectionOutbound', NULL, 'OUTBOUND', '{\"en-US\": \"Internal to External\", \"ja-JP\": \"内部から外部\", \"ko-KR\": \"내부에서 외부\", \"zh-CN\": \"内调外\", \"zh-TW\": \"內調外\"}', '/integrationDirection/integrationDirectionOutbound', 2, 0, 2, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000225, 5000000000000000215, '停用', 'integrationStatusDisabled', NULL, '0', '{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"停用\", \"zh-TW\": \"停用\"}', '/integrationStatus/integrationStatusDisabled', 2, 0, 2, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000226, 5000000000000000215, '启用', 'integrationStatusEnabled', NULL, '1', '{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}', '/integrationStatus/integrationStatusEnabled', 2, 0, 1, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000227, 5000000000000000216, '停用', 'thirdSystemStatusDisabled', NULL, '0', '{\"en-US\": \"Disabled\", \"ja-JP\": \"無効\", \"ko-KR\": \"비활성화\", \"zh-CN\": \"停用\", \"zh-TW\": \"停用\"}', '/thirdSystemStatus/thirdSystemStatusDisabled', 2, 0, 2, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);
INSERT INTO `sys_dict` VALUES (5000000000000000228, 5000000000000000216, '启用', 'thirdSystemStatusEnabled', NULL, '1', '{\"en-US\": \"Enabled\", \"ja-JP\": \"有効\", \"ko-KR\": \"활성화\", \"zh-CN\": \"启用\", \"zh-TW\": \"啟用\"}', '/thirdSystemStatus/thirdSystemStatusEnabled', 2, 0, 1, 1, NULL, 0, 0, '2026-04-26 21:50:10', 0, '2026-04-26 21:50:34', 0, NULL);

-- ----------------------------
-- Table structure for sys_file_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_record`;
CREATE TABLE `sys_file_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模块编码',
  `module_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '模块名称',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '原始文件名',
  `stored_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '存储文件名',
  `file_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '文件类型/扩展名',
  `content_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'Content-Type',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小，字节',
  `relative_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '相对路径',
  `access_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '完整访问地址',
  `storage_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '存储类型',
  `storage_config_id` bigint NULL DEFAULT NULL COMMENT '存储配置ID',
  `tenant_id` bigint NOT NULL DEFAULT 0 COMMENT '租户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_module_code`(`module_code` ASC) USING BTREE,
  INDEX `idx_original_name`(`original_name` ASC) USING BTREE,
  INDEX `idx_file_type`(`file_type` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统文件记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_record
-- ----------------------------
INSERT INTO `sys_file_record` VALUES (1, 'sys-config', '系统配置', 'ede35b9f3d5a4de49822247c302157f81.mp4', 'fc9a9b98c783483d8a915f2bd8a8de42.mp4', 'mp4', 'video/mp4', 698029, 'fc9a9b98c783483d8a915f2bd8a8de42.mp4', '/files/fc9a9b98c783483d8a915f2bd8a8de42.mp4', 'LOCAL', NULL, 1993479636925403138, '2026-05-01 14:58:28', '2026-05-01 14:58:28', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (2, 'sys_config_logo', '系统配置Logo', '9f3fed37a1e944eda9448bf7e1a9af47.png', '31c545dd9edc404ba2cd067cc73949d0.png', 'png', 'image/png', 61792, '31c545dd9edc404ba2cd067cc73949d0.png', 'http://192.168.44.1/files/31c545dd9edc404ba2cd067cc73949d0.png', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 16:30:38', '2026-05-02 16:30:38', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (3, 'sys_config_logo', '系统配置Logo', '1c7ee98348414e9ba80f98dbb031ae46.png', '636e0bb093fb47df8f603b23f7b90797.png', 'png', 'image/png', 75493, '636e0bb093fb47df8f603b23f7b90797.png', 'http://192.168.44.1/files/636e0bb093fb47df8f603b23f7b90797.png', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 16:30:55', '2026-05-02 16:30:55', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (4, 'sys_config_logo', '系统配置Logo', '04ea2c9b751e4b2aa15080e764f58c96.png', '16e2b1ea1dcd460fa34a0f02331a949e.png', 'png', 'image/png', 91516, '16e2b1ea1dcd460fa34a0f02331a949e.png', 'http://192.168.121.1:9000/api/files/16e2b1ea1dcd460fa34a0f02331a949e.png', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 19:10:06', '2026-05-02 19:10:06', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (5, 'sys-config', '系统配置', 'ede35b9f3d5a4de49822247c302157f81.mp4', '1522c152ab1f437ba99d572477a6d91e.mp4', 'mp4', 'video/mp4', 698029, '1522c152ab1f437ba99d572477a6d91e.mp4', 'http://192.168.121.1:9000/api/files/1522c152ab1f437ba99d572477a6d91e.mp4', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 19:10:21', '2026-05-02 19:10:21', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (6, 'sys_user_avatar', '用户头像', '0767c88bcfa54a589180a9e634eb3f0e.jpg', 'fef4cdfc3b304e089ede5102c7369e18.jpg', 'jpg', 'image/png', 102147, 'fef4cdfc3b304e089ede5102c7369e18.jpg', 'http://192.168.121.1:9000/api/files/fef4cdfc3b304e089ede5102c7369e18.jpg', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 19:10:35', '2026-05-02 19:10:35', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (7, 'sys_config_logo', '系统配置Logo', '1c7ee98348414e9ba80f98dbb031ae46.png', '5b263b2bdfae4ec6b96983feeeba1ba8.png', 'png', 'image/png', 66696, '5b263b2bdfae4ec6b96983feeeba1ba8.png', 'http://192.168.121.1:9000/api/files/5b263b2bdfae4ec6b96983feeeba1ba8.png', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 20:31:14', '2026-05-02 20:31:14', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (8, 'sys-config', '系统配置', 'ede35b9f3d5a4de49822247c302157f81.mp4', '77ae386c8b694f06b61bda5148e96dde.mp4', 'mp4', 'video/mp4', 698029, '77ae386c8b694f06b61bda5148e96dde.mp4', 'http://192.168.121.1:9000/api/files/77ae386c8b694f06b61bda5148e96dde.mp4', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 20:31:22', '2026-05-02 20:31:22', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (9, 'sys_config_logo', '系统配置Logo', '1c7ee98348414e9ba80f98dbb031ae46.png', '22a3f67f220b4dbd903ebc1ac60ef6b6.png', 'png', 'image/png', 63255, '22a3f67f220b4dbd903ebc1ac60ef6b6.png', 'http://192.168.121.1:9000/api/files/22a3f67f220b4dbd903ebc1ac60ef6b6.png', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 20:31:34', '2026-05-02 20:31:34', '1993479637244170242', '1993479637244170242', 0);
INSERT INTO `sys_file_record` VALUES (10, 'sys-config', '系统配置', 'ede35b9f3d5a4de49822247c302157f81.mp4', '56226ed12dd744659c14cba7ba68e092.mp4', 'mp4', 'video/mp4', 698029, '56226ed12dd744659c14cba7ba68e092.mp4', 'http://192.168.121.1:9000/api/files/56226ed12dd744659c14cba7ba68e092.mp4', 'LOCAL', NULL, 1993479636925403138, '2026-05-02 20:31:45', '2026-05-02 20:31:45', '1993479637244170242', '1993479637244170242', 0);

-- ----------------------------
-- Table structure for sys_file_storage
-- ----------------------------
DROP TABLE IF EXISTS `sys_file_storage`;
CREATE TABLE `sys_file_storage`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `storage_type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储类型(LOCAL/OSS/MINIO)',
  `storage_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储名称',
  `config_json` json NULL COMMENT '配置(JSON)',
  `is_default` tinyint NOT NULL DEFAULT 0 COMMENT '是否默认',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(1启用,0禁用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_type`(`tenant_id` ASC, `storage_type` ASC) USING BTREE,
  INDEX `idx_tenant_default`(`tenant_id` ASC, `is_default` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文件存储配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_file_storage
-- ----------------------------

-- ----------------------------
-- Table structure for sys_invite_code
-- ----------------------------
DROP TABLE IF EXISTS `sys_invite_code`;
CREATE TABLE `sys_invite_code`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `invite_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '邀请码（唯一）',
  `department_id` bigint NOT NULL COMMENT '归属部门ID',
  `position_id` bigint NULL DEFAULT NULL COMMENT '归属职位ID（可选）',
  `role_id` bigint NULL DEFAULT NULL COMMENT '注册后绑定角色ID',
  `expire_time` datetime NOT NULL COMMENT '失效时间',
  `max_register_count` int NOT NULL DEFAULT 1 COMMENT '最大注册人数',
  `used_count` int NOT NULL DEFAULT 0 COMMENT '已注册人数',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1=启用 0=停用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invite_code`(`invite_code` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_position_id`(`position_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邀请码主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_invite_code
-- ----------------------------
INSERT INTO `sys_invite_code` VALUES (1, 1993479636925403138, '52B7ED4C', 1, 12, 1993479637311279112, '2026-04-17 11:35:30', 10, 0, 1, NULL, '1993479637244170242', '2026-04-17 11:35:38', '1993479637244170242', '2026-04-17 11:35:38', 1);

-- ----------------------------
-- Table structure for sys_invite_register_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_invite_register_record`;
CREATE TABLE `sys_invite_register_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `invite_id` bigint NOT NULL COMMENT '邀请码主表ID',
  `invite_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '冗余邀请码',
  `user_id` bigint NOT NULL COMMENT '注册成功用户ID',
  `account` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '注册账号',
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '用户名',
  `department_id` bigint NULL DEFAULT NULL COMMENT '注册落入部门ID',
  `position_id` bigint NULL DEFAULT NULL COMMENT '注册落入职位ID',
  `role_id` bigint NULL DEFAULT NULL COMMENT '注册绑定角色ID',
  `register_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册IP',
  `register_region` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '注册地区',
  `register_time` datetime NOT NULL COMMENT '注册时间',
  `status` int NOT NULL DEFAULT 1 COMMENT '注册结果状态：1=成功 0=失败',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_invite_id`(`invite_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_register_time`(`register_time` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '邀请注册关系记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_invite_register_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_job
-- ----------------------------
DROP TABLE IF EXISTS `sys_job`;
CREATE TABLE `sys_job`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `job_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务名称',
  `cron_expression` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'Cron表达式',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `invoke_target` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '调用目标 (方法路径)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '定时任务表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_job
-- ----------------------------

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `tenant_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'PUBLIC' COMMENT 'Applicable tenant type: MAIN_TENANT/CUSTOMER_TENANT/SUPPLIER_TENANT/PARTNER_TENANT/PUBLIC; PUBLIC means all tenant types',
  `module_id` bigint NULL DEFAULT NULL COMMENT '所属模块ID',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父级菜单ID',
  `type` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：module/catalog/menu/button',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由路径',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `name_i18n_json` json NULL COMMENT '菜单名称国际化(JSON)',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `component_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件Key',
  `perm_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '按钮权限标识',
  `order_num` int NULL DEFAULT NULL COMMENT '排序号',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可见：1可见 0隐藏',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  `menu_level` int NULL DEFAULT 1 COMMENT '菜单层级：1=一级菜单(目录), 2=二级菜单, 3=三级菜单',
  `menu_mode` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'embedded' COMMENT '菜单模式：embedded=内嵌，external=外联',
  `external_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外联URL（当menu_mode=external时使用）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_menu_tenant`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_sys_menu_module`(`module_id` ASC) USING BTREE,
  INDEX `idx_sys_menu_parent`(`parent_id` ASC) USING BTREE,
  INDEX `idx_menu_mode`(`menu_mode` ASC) USING BTREE,
  INDEX `idx_tenant_type`(`tenant_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3000000000000000493 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统菜单/目录/按钮表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 1993479636925403138, 'PUBLIC', 1, 652, 'menu', 'user', '用户管理', '{\"en-US\": \"Users\", \"ja-JP\": \"ユーザー管理\", \"ko-KR\": \"사용자 관리\", \"zh-CN\": \"用户管理\", \"zh-TW\": \"用戶管理\"}', 'UserOutlined', 'SystemUser', 'sys:user:view', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 10:15:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (2, 1993479636925403138, 'PUBLIC', 1, 653, 'menu', 'role', '角色管理', '{\"en-US\": \"Roles\", \"ja-JP\": \"ロール管理\", \"ko-KR\": \"역할 관리\", \"zh-CN\": \"角色管理\", \"zh-TW\": \"角色管理\"}', 'TeamOutlined', 'SystemRole', 'sys:role:view', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 10:15:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'module', '模块管理', '{\"en-US\": \"Modules\", \"ja-JP\": \"モジュール管理\", \"ko-KR\": \"모듈 관리\", \"zh-CN\": \"模块管理\", \"zh-TW\": \"模塊管理\"}', 'AppstoreOutlined', 'SystemModule', 'sys:module:view', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 10:15:53', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (4, 1993479636925403138, 'PUBLIC', 1, 653, 'menu', 'menu', '菜单管理', '{\"en-US\": \"Menus\", \"ja-JP\": \"メニュー管理\", \"ko-KR\": \"메뉴 관리\", \"zh-CN\": \"菜单管理\", \"zh-TW\": \"選單管理\"}', 'MenuOutlined', 'SystemMenu', 'sys:menu:view', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 10:15:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (5, 1993479636925403138, 'PUBLIC', 1, 652, 'menu', 'department', '部门管理', '{\"en-US\": \"Departments\", \"ja-JP\": \"部門管理\", \"ko-KR\": \"부서 관리\", \"zh-CN\": \"部门管理\", \"zh-TW\": \"部門管理\"}', 'ApartmentOutlined', 'SystemDepartment', 'sys:dept:view', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 10:15:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (6, 1993479636925403138, 'PUBLIC', 1, 652, 'menu', 'position', '职位管理', '{\"en-US\": \"Positions\", \"ja-JP\": \"職位管理\", \"ko-KR\": \"직위 관리\", \"zh-CN\": \"职位管理\", \"zh-TW\": \"職位管理\"}', 'IdcardOutlined', 'SystemPosition', 'sys:position:view', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:55:14', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (101, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '新增用户', '{\"en-US\": \"Add User\", \"ja-JP\": \"ユーザー追加\", \"ko-KR\": \"사용자 추가\", \"zh-CN\": \"新增用户\", \"zh-TW\": \"新增用戶\"}', NULL, NULL, 'sys:user:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:55:55', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (102, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '编辑用户', '{\"en-US\": \"Edit User\", \"ja-JP\": \"ユーザー編集\", \"ko-KR\": \"사용자 편집\", \"zh-CN\": \"编辑用户\", \"zh-TW\": \"編輯用戶\"}', NULL, NULL, 'sys:user:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:55:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (103, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '删除用户', '{\"en-US\": \"Delete User\", \"ja-JP\": \"ユーザー削除\", \"ko-KR\": \"사용자 삭제\", \"zh-CN\": \"删除用户\", \"zh-TW\": \"刪除用戶\"}', NULL, NULL, 'sys:user:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:55:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (104, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '批量删除用户', '{\"en-US\": \"Batch Delete Users\", \"ja-JP\": \"一括削除ユーザー\", \"ko-KR\": \"일괄 삭제 사용자\", \"zh-CN\": \"批量删除用户\", \"zh-TW\": \"批量刪除用戶\"}', NULL, NULL, 'sys:user:batchDelete', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (105, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '重置密码', '{\"en-US\": \"Reset Password\", \"ja-JP\": \"パスワードリセット\", \"ko-KR\": \"비밀번호 재설정\", \"zh-CN\": \"重置密码\", \"zh-TW\": \"重置密碼\"}', NULL, NULL, 'sys:user:resetPwd', 5, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (106, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '导出用户', '{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}', NULL, NULL, 'sys:user:export', 6, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (201, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '新增角色', '{\"en-US\": \"Add Role\", \"ja-JP\": \"ロール追加\", \"ko-KR\": \"역할 추가\", \"zh-CN\": \"新增角色\", \"zh-TW\": \"新增角色\"}', NULL, NULL, 'sys:role:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (202, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '编辑角色', '{\"en-US\": \"Edit Role\", \"ja-JP\": \"ロール編集\", \"ko-KR\": \"역할 편집\", \"zh-CN\": \"编辑角色\", \"zh-TW\": \"編輯角色\"}', NULL, NULL, 'sys:role:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:51', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (203, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '删除角色', '{\"en-US\": \"Delete Role\", \"ja-JP\": \"ロール削除\", \"ko-KR\": \"역할 삭제\", \"zh-CN\": \"删除角色\", \"zh-TW\": \"刪除角色\"}', NULL, NULL, 'sys:role:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:51', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (204, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '批量删除角色', '{\"en-US\": \"Batch Delete Roles\", \"ja-JP\": \"一括削除ロール\", \"ko-KR\": \"일괄 삭제 역할\", \"zh-CN\": \"批量删除角色\", \"zh-TW\": \"批量刪除角色\"}', NULL, NULL, 'sys:role:batchDelete', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:51', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (205, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '菜单授权', '{\"en-US\": \"Menu Authorization\", \"ja-JP\": \"メニュー認可\", \"ko-KR\": \"메뉴 인증\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"選單授權\"}', NULL, NULL, 'sys:role:authMenu', 5, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (301, 1993479636925403138, 'PUBLIC', 1, 3, 'button', NULL, '新增模块', '{\"en-US\": \"Add Module\", \"ja-JP\": \"モジュール追加\", \"ko-KR\": \"모듈 추가\", \"zh-CN\": \"新增模块\", \"zh-TW\": \"新增模塊\"}', NULL, NULL, 'sys:module:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:56:53', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (302, 1993479636925403138, 'PUBLIC', 1, 3, 'button', NULL, '编辑模块', '{\"en-US\": \"Edit Module\", \"ja-JP\": \"モジュール編集\", \"ko-KR\": \"모듈 편집\", \"zh-CN\": \"编辑模块\", \"zh-TW\": \"編輯模塊\"}', NULL, NULL, 'sys:module:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:00', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (303, 1993479636925403138, 'PUBLIC', 1, 3, 'button', NULL, '删除模块', '{\"en-US\": \"Delete Module\", \"ja-JP\": \"モジュール削除\", \"ko-KR\": \"모듈 삭제\", \"zh-CN\": \"删除模块\", \"zh-TW\": \"刪除模塊\"}', NULL, NULL, 'sys:module:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:00', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (304, 1993479636925403138, 'PUBLIC', 1, 3, 'button', NULL, '批量删除模块', '{\"en-US\": \"Batch Delete Modules\", \"ja-JP\": \"一括削除モジュール\", \"ko-KR\": \"일괄 삭제 모듈\", \"zh-CN\": \"批量删除模块\", \"zh-TW\": \"批量刪除模塊\"}', NULL, NULL, 'sys:module:batchDelete', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:00', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (401, 1993479636925403138, 'PUBLIC', 1, 4, 'button', NULL, '新增菜单', '{\"en-US\": \"Add Menu\", \"ja-JP\": \"メニュー追加\", \"ko-KR\": \"메뉴 추가\", \"zh-CN\": \"新增菜单\", \"zh-TW\": \"新增選單\"}', NULL, NULL, 'sys:menu:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:01', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (402, 1993479636925403138, 'PUBLIC', 1, 4, 'button', NULL, '编辑菜单', '{\"en-US\": \"Edit Menu\", \"ja-JP\": \"メニュー編集\", \"ko-KR\": \"메뉴 편집\", \"zh-CN\": \"编辑菜单\", \"zh-TW\": \"編輯選單\"}', NULL, NULL, 'sys:menu:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:01', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (403, 1993479636925403138, 'PUBLIC', 1, 4, 'button', NULL, '删除菜单', '{\"en-US\": \"Delete Menu\", \"ja-JP\": \"メニュー削除\", \"ko-KR\": \"메뉴 삭제\", \"zh-CN\": \"删除菜单\", \"zh-TW\": \"刪除選單\"}', NULL, NULL, 'sys:menu:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:10', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (404, 1993479636925403138, 'PUBLIC', 1, 4, 'button', NULL, '批量删除菜单', '{\"en-US\": \"Batch Delete Menus\", \"ja-JP\": \"一括削除メニュー\", \"ko-KR\": \"일괄 삭제 메뉴\", \"zh-CN\": \"批量删除菜单\", \"zh-TW\": \"批量刪除選單\"}', NULL, NULL, 'sys:menu:batchDelete', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:10', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (501, 1993479636925403138, 'PUBLIC', 1, 5, 'button', NULL, '新增部门', '{\"en-US\": \"Add Department\", \"ja-JP\": \"部門追加\", \"ko-KR\": \"부서 추가\", \"zh-CN\": \"新增部门\", \"zh-TW\": \"新增部門\"}', NULL, NULL, 'sys:dept:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:10', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (502, 1993479636925403138, 'PUBLIC', 1, 5, 'button', NULL, '编辑部门', '{\"en-US\": \"Edit Department\", \"ja-JP\": \"部門編集\", \"ko-KR\": \"부서 편집\", \"zh-CN\": \"编辑部门\", \"zh-TW\": \"編輯部門\"}', NULL, NULL, 'sys:dept:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:11', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (503, 1993479636925403138, 'PUBLIC', 1, 5, 'button', NULL, '删除部门', '{\"en-US\": \"Delete Department\", \"ja-JP\": \"部門削除\", \"ko-KR\": \"부서 삭제\", \"zh-CN\": \"删除部门\", \"zh-TW\": \"刪除部門\"}', NULL, NULL, 'sys:dept:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:11', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (601, 1993479636925403138, 'PUBLIC', 1, 6, 'button', NULL, '新增职位', '{\"en-US\": \"Add Position\", \"ja-JP\": \"職位追加\", \"ko-KR\": \"직위 추가\", \"zh-CN\": \"新增职位\", \"zh-TW\": \"新增職位\"}', NULL, NULL, 'sys:position:add', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (602, 1993479636925403138, 'PUBLIC', 1, 6, 'button', NULL, '编辑职位', '{\"en-US\": \"Edit Position\", \"ja-JP\": \"職位編集\", \"ko-KR\": \"직위 편집\", \"zh-CN\": \"编辑职位\", \"zh-TW\": \"編輯職位\"}', NULL, NULL, 'sys:position:edit', 2, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (603, 1993479636925403138, 'PUBLIC', 1, 6, 'button', NULL, '删除职位', '{\"en-US\": \"Delete Position\", \"ja-JP\": \"職位削除\", \"ko-KR\": \"직위 삭제\", \"zh-CN\": \"删除职位\", \"zh-TW\": \"刪除職位\"}', NULL, NULL, 'sys:position:delete', 3, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (604, 1993479636925403138, 'PUBLIC', 1, 6, 'button', NULL, '批量删除职位', '{\"en-US\": \"Batch Delete Positions\", \"ja-JP\": \"一括削除職位\", \"ko-KR\": \"일괄 삭제 직위\", \"zh-CN\": \"批量删除职位\", \"zh-TW\": \"批量刪除職位\"}', NULL, NULL, 'sys:position:batchDelete', 4, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:57:56', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (606, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'dashboard', '系统管理主页', '{\"en-US\": \"Dashboard\", \"ja-JP\": \"ダッシュボード\", \"ko-KR\": \"대시보드\", \"zh-CN\": \"系统管理主页\", \"zh-TW\": \"系統管理主頁\"}', 'DashboardOutlined', 'SystemDashboard', 'sys:dashboard:view', 1, 1, 1, '2026-01-08 10:58:58', NULL, '2026-01-18 18:55:15', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (607, 1993479636925403138, 'PUBLIC', 1, 3000000000000000422, 'menu', 'excelExportConfig', '导出配置', '{\"en-US\": \"Export Config\", \"ja-JP\": \"エクスポート設定\", \"ko-KR\": \"내보내기 설정\", \"zh-CN\": \"导出配置\", \"zh-TW\": \"匯出設定\"}', 'FileExcelOutlined', 'SystemExcelExportConfig', 'sys:excel:exportConfig:view', 20, 1, 1, '2026-01-14 15:09:12', NULL, '2026-04-12 10:48:14', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (608, 1993479636925403138, 'PUBLIC', 1, 3000000000000000422, 'menu', 'excelImportConfig', '导入配置', '{\"en-US\": \"Import Config\", \"ja-JP\": \"インポート設定\", \"ko-KR\": \"가져오기 설정\", \"zh-CN\": \"导入配置\", \"zh-TW\": \"匯入設定\"}', 'FileExcelOutlined', 'SystemExcelImportConfig', 'sys:excel:importConfig:view', 10, 1, 1, '2026-01-14 15:09:24', NULL, '2026-04-12 10:48:14', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (609, 1993479636925403138, 'PUBLIC', 1, 607, 'button', NULL, '查看导出配置', '{\"en-US\": \"View Export Config\", \"ja-JP\": \"エクスポート設定表示\", \"ko-KR\": \"내보내기 설정 보기\", \"zh-CN\": \"查看导出配置\", \"zh-TW\": \"查看匯出設定\"}', NULL, NULL, 'sys:excel:exportConfig:list', 1, 1, 1, '2026-01-14 15:09:30', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (610, 1993479636925403138, 'PUBLIC', 1, 607, 'button', NULL, '编辑导出配置', '{\"en-US\": \"Edit Export Config\", \"ja-JP\": \"エクスポート設定編集\", \"ko-KR\": \"내보내기 설정 편집\", \"zh-CN\": \"编辑导出配置\", \"zh-TW\": \"編輯匯出設定\"}', NULL, NULL, 'sys:excel:exportConfig:edit', 2, 1, 1, '2026-01-14 15:09:41', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (611, 1993479636925403138, 'PUBLIC', 1, 607, 'button', NULL, '删除导出配置', '{\"en-US\": \"Delete Export Config\", \"ja-JP\": \"エクスポート設定削除\", \"ko-KR\": \"내보내기 설정 삭제\", \"zh-CN\": \"删除导出配置\", \"zh-TW\": \"刪除匯出設定\"}', NULL, NULL, 'sys:excel:exportConfig:delete', 3, 1, 1, '2026-01-14 15:09:47', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (612, 1993479636925403138, 'PUBLIC', 1, 608, 'button', NULL, '查看导入配置', '{\"en-US\": \"View Import Config\", \"ja-JP\": \"インポート設定表示\", \"ko-KR\": \"가져오기 설정 보기\", \"zh-CN\": \"查看导入配置\", \"zh-TW\": \"查看匯入設定\"}', NULL, NULL, 'sys:excel:importConfig:list', 1, 1, 1, '2026-01-14 15:09:57', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (613, 1993479636925403138, 'PUBLIC', 1, 608, 'button', NULL, '编辑导入配置', '{\"en-US\": \"Edit Import Config\", \"ja-JP\": \"インポート設定編集\", \"ko-KR\": \"가져오기 설정 편집\", \"zh-CN\": \"编辑导入配置\", \"zh-TW\": \"編輯匯入設定\"}', NULL, NULL, 'sys:excel:importConfig:edit', 2, 1, 1, '2026-01-14 15:10:05', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (614, 1993479636925403138, 'PUBLIC', 1, 608, 'button', NULL, '删除导入配置', '{\"en-US\": \"Delete Import Config\", \"ja-JP\": \"インポート設定削除\", \"ko-KR\": \"가져오기 설정 삭제\", \"zh-CN\": \"删除导入配置\", \"zh-TW\": \"刪除匯入設定\"}', NULL, NULL, 'sys:excel:importConfig:delete', 3, 1, 1, '2026-01-14 15:10:13', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (615, 1993479636925403138, 'PUBLIC', 1, 608, 'button', NULL, '下载导入模板', '{\"en-US\": \"Download Import Template\", \"ja-JP\": \"インポートテンプレートダウンロード\", \"ko-KR\": \"가져오기 템플릿 다운로드\", \"zh-CN\": \"下载导入模板\", \"zh-TW\": \"下載匯入模板\"}', NULL, NULL, 'sys:excel:template:download', 10, 1, 1, '2026-01-14 15:10:21', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (616, 1993479636925403138, 'PUBLIC', 1, 607, 'button', NULL, '导出登录日志', '{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}', NULL, NULL, 'sys:excel:export:loginLog', 11, 1, 1, '2026-01-14 15:10:31', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (617, 1993479636925403138, 'PUBLIC', 1, 607, 'button', NULL, '导出用户', '{\"en-US\": \"Export Users\", \"ja-JP\": \"ユーザーエクスポート\", \"ko-KR\": \"사용자 내보내기\", \"zh-CN\": \"导出用户\", \"zh-TW\": \"匯出用戶\"}', NULL, NULL, 'sys:excel:export:user', 12, 1, 1, '2026-01-14 15:10:41', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (619, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '分配角色', '{\"en-US\": \"Assign Roles\", \"ja-JP\": \"ロール割り当て\", \"ko-KR\": \"역할 할당\", \"zh-CN\": \"分配角色\", \"zh-TW\": \"分配角色\"}', NULL, NULL, 'sys:user:assignRole', 7, 1, 1, '2026-01-14 16:08:56', NULL, '2026-01-18 18:56:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (620, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'dict', '字典管理', '{\"en-US\": \"Dictionary\", \"ja-JP\": \"辞書管理\", \"ko-KR\": \"사전 관리\", \"zh-CN\": \"字典管理\", \"zh-TW\": \"字典管理\"}', 'BookOutlined', 'SystemDict', 'sys:dict:view', 7, 1, 1, '2026-01-16 11:49:59', NULL, '2026-01-18 18:55:47', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (621, 1993479636925403138, 'PUBLIC', 1, 620, 'button', NULL, '新增字典', '{\"en-US\": \"Add Dictionary\", \"ja-JP\": \"辞書追加\", \"ko-KR\": \"사전 추가\", \"zh-CN\": \"新增字典\", \"zh-TW\": \"新增字典\"}', NULL, NULL, 'sys:dict:add', 1, 1, 1, '2026-01-16 11:50:07', NULL, '2026-01-18 18:58:31', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (622, 1993479636925403138, 'PUBLIC', 1, 620, 'button', NULL, '编辑字典', '{\"en-US\": \"Edit Dictionary\", \"ja-JP\": \"辞書編集\", \"ko-KR\": \"사전 편집\", \"zh-CN\": \"编辑字典\", \"zh-TW\": \"編輯字典\"}', NULL, NULL, 'sys:dict:edit', 2, 1, 1, '2026-01-16 11:50:07', NULL, '2026-01-18 18:58:31', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (623, 1993479636925403138, 'PUBLIC', 1, 620, 'button', NULL, '删除字典', '{\"en-US\": \"Delete Dictionary\", \"ja-JP\": \"辞書削除\", \"ko-KR\": \"사전 삭제\", \"zh-CN\": \"删除字典\", \"zh-TW\": \"刪除字典\"}', NULL, NULL, 'sys:dict:delete', 3, 1, 1, '2026-01-16 11:50:07', NULL, '2026-01-18 18:58:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (624, 1993479636925403138, 'PUBLIC', 1, 620, 'button', NULL, '批量删除字典', '{\"en-US\": \"Batch Delete Dictionaries\", \"ja-JP\": \"一括削除辞書\", \"ko-KR\": \"일괄 삭제 사전\", \"zh-CN\": \"批量删除字典\", \"zh-TW\": \"批量刪除字典\"}', NULL, NULL, 'sys:dict:batchDelete', 4, 1, 1, '2026-01-16 11:50:07', NULL, '2026-01-18 18:58:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (625, 1993479636925403138, 'PUBLIC', 1, 620, 'button', NULL, '导出字典', '{\"en-US\": \"Export Dictionary\", \"ja-JP\": \"辞書エクスポート\", \"ko-KR\": \"사전 내보내기\", \"zh-CN\": \"导出字典\", \"zh-TW\": \"匯出字典\"}', NULL, NULL, 'sys:dict:export', 5, 1, 1, '2026-01-16 11:50:07', NULL, '2026-01-18 18:58:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (626, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'dictType', '字典类型管理', '{\"en-US\": \"Dict Types\", \"ja-JP\": \"辞書タイプ\", \"ko-KR\": \"사전 유형\", \"zh-CN\": \"字典类型管理\", \"zh-TW\": \"字典類型管理\"}', 'TagsOutlined', 'SystemDictType', 'sys:dictType:view', 100, 0, 1, '2026-01-16 11:50:11', NULL, '2026-01-18 18:55:47', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (627, 1993479636925403138, 'PUBLIC', 1, 626, 'button', NULL, '新增字典类型', '{\"en-US\": \"Add Dict Type\", \"ja-JP\": \"辞書タイプ追加\", \"ko-KR\": \"사전 유형 추가\", \"zh-CN\": \"新增字典类型\", \"zh-TW\": \"新增字典類型\"}', NULL, NULL, 'sys:dictType:add', 1, 1, 1, '2026-01-16 11:50:24', NULL, '2026-01-18 18:58:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (628, 1993479636925403138, 'PUBLIC', 1, 626, 'button', NULL, '编辑字典类型', '{\"en-US\": \"Edit Dict Type\", \"ja-JP\": \"辞書タイプ編集\", \"ko-KR\": \"사전 유형 편집\", \"zh-CN\": \"编辑字典类型\", \"zh-TW\": \"編輯字典類型\"}', NULL, NULL, 'sys:dictType:edit', 2, 1, 1, '2026-01-16 11:50:24', NULL, '2026-01-18 18:58:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (629, 1993479636925403138, 'PUBLIC', 1, 626, 'button', NULL, '删除字典类型', '{\"en-US\": \"Delete Dict Type\", \"ja-JP\": \"辞書タイプ削除\", \"ko-KR\": \"사전 유형 삭제\", \"zh-CN\": \"删除字典类型\", \"zh-TW\": \"刪除字典類型\"}', NULL, NULL, 'sys:dictType:delete', 3, 1, 1, '2026-01-16 11:50:24', NULL, '2026-01-18 18:58:45', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (630, 1993479636925403138, 'PUBLIC', 1, 626, 'button', NULL, '批量删除字典类型', '{\"en-US\": \"Batch Delete Dict Types\", \"ja-JP\": \"一括削除辞書タイプ\", \"ko-KR\": \"일괄 삭제 사전 유형\", \"zh-CN\": \"批量删除字典类型\", \"zh-TW\": \"批量刪除字典類型\"}', NULL, NULL, 'sys:dictType:batchDelete', 4, 1, 1, '2026-01-16 11:50:24', NULL, '2026-01-18 18:58:45', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (631, 1993479636925403138, 'PUBLIC', 1, 626, 'button', NULL, '导出字典类型', '{\"en-US\": \"Export Dict Types\", \"ja-JP\": \"辞書タイプエクスポート\", \"ko-KR\": \"사전 유형 내보내기\", \"zh-CN\": \"导出字典类型\", \"zh-TW\": \"匯出字典類型\"}', NULL, NULL, 'sys:dictType:export', 5, 1, 1, '2026-01-16 11:50:24', NULL, '2026-01-18 18:58:47', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (632, 1993479636925403138, 'PUBLIC', 1, 3000000000000000423, 'menu', 'tableConfig', '表格配置', '{\"en-US\": \"Table Config\", \"ja-JP\": \"テーブル設定\", \"ko-KR\": \"테이블 설정\", \"zh-CN\": \"表格配置\", \"zh-TW\": \"表格設定\"}', 'TableOutlined', 'SystemTableConfig', 'sys:tableConfig:view', 10, 1, 1, '2026-01-16 11:50:37', NULL, '2026-04-12 10:48:14', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (633, 1993479636925403138, 'PUBLIC', 1, 632, 'button', NULL, '新增表格配置', '{\"en-US\": \"Add Table Config\", \"ja-JP\": \"テーブル設定追加\", \"ko-KR\": \"테이블 설정 추가\", \"zh-CN\": \"新增表格配置\", \"zh-TW\": \"新增表格設定\"}', NULL, NULL, 'sys:tableConfig:add', 1, 1, 1, '2026-01-16 11:50:48', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (634, 1993479636925403138, 'PUBLIC', 1, 632, 'button', NULL, '编辑表格配置', '{\"en-US\": \"Edit Table Config\", \"ja-JP\": \"テーブル設定編集\", \"ko-KR\": \"테이블 설정 편집\", \"zh-CN\": \"编辑表格配置\", \"zh-TW\": \"編輯表格設定\"}', NULL, NULL, 'sys:tableConfig:edit', 2, 1, 1, '2026-01-16 11:50:48', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (635, 1993479636925403138, 'PUBLIC', 1, 632, 'button', NULL, '删除表格配置', '{\"en-US\": \"Delete Table Config\", \"ja-JP\": \"テーブル設定削除\", \"ko-KR\": \"테이블 설정 삭제\", \"zh-CN\": \"删除表格配置\", \"zh-TW\": \"刪除表格設定\"}', NULL, NULL, 'sys:tableConfig:delete', 3, 1, 1, '2026-01-16 11:50:48', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (636, 1993479636925403138, 'PUBLIC', 1, 632, 'button', NULL, '批量删除表格配置', '{\"en-US\": \"Batch Delete Table Configs\", \"ja-JP\": \"一括削除テーブル設定\", \"ko-KR\": \"일괄 삭제 테이블 설정\", \"zh-CN\": \"批量删除表格配置\", \"zh-TW\": \"批量刪除表格設定\"}', NULL, NULL, 'sys:tableConfig:batchDelete', 4, 1, 1, '2026-01-16 11:50:48', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (637, 1993479636925403138, 'PUBLIC', 1, 632, 'button', NULL, '导出表格配置', '{\"en-US\": \"Export Table Config\", \"ja-JP\": \"テーブル設定エクスポート\", \"ko-KR\": \"테이블 설정 내보내기\", \"zh-CN\": \"导出表格配置\", \"zh-TW\": \"匯出表格設定\"}', NULL, NULL, 'sys:tableConfig:export', 5, 1, 1, '2026-01-16 11:50:48', NULL, '2026-04-12 10:48:14', 'codex', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (638, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'loginLog', '登录日志', '{\"en-US\": \"Login Logs\", \"ja-JP\": \"ログインログ\", \"ko-KR\": \"로그인 로그\", \"zh-CN\": \"登录日志\", \"zh-TW\": \"登錄日誌\"}', 'FileTextOutlined', 'SystemLoginLog', 'sys:loginLog:view', 9, 1, 1, '2026-01-16 11:51:14', NULL, '2026-01-18 18:55:48', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (639, 1993479636925403138, 'PUBLIC', 1, 638, 'button', NULL, '删除登录日志', '{\"en-US\": \"Delete Login Log\", \"ja-JP\": \"ログインログ削除\", \"ko-KR\": \"로그인 로그 삭제\", \"zh-CN\": \"删除登录日志\", \"zh-TW\": \"刪除登錄日誌\"}', NULL, NULL, 'sys:loginLog:delete', 1, 1, 1, '2026-01-16 11:51:27', NULL, '2026-01-18 18:58:54', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (640, 1993479636925403138, 'PUBLIC', 1, 638, 'button', NULL, '批量删除登录日志', '{\"en-US\": \"Batch Delete Login Logs\", \"ja-JP\": \"一括削除ログインログ\", \"ko-KR\": \"일괄 삭제 로그인 로그\", \"zh-CN\": \"批量删除登录日志\", \"zh-TW\": \"批量刪除登錄日誌\"}', NULL, NULL, 'sys:loginLog:batchDelete', 2, 1, 1, '2026-01-16 11:51:27', NULL, '2026-01-18 18:58:54', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (641, 1993479636925403138, 'PUBLIC', 1, 638, 'button', NULL, '导出登录日志', '{\"en-US\": \"Export Login Logs\", \"ja-JP\": \"ログインログエクスポート\", \"ko-KR\": \"로그인 로그 내보내기\", \"zh-CN\": \"导出登录日志\", \"zh-TW\": \"匯出登錄日誌\"}', NULL, NULL, 'sys:loginLog:export', 3, 1, 1, '2026-01-16 11:51:27', NULL, '2026-01-18 18:59:17', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (642, 1993479636925403138, 'PUBLIC', 1, 654, 'menu', 'online', '在线用户', '{\"en-US\": \"Online Users\", \"ja-JP\": \"オンラインユーザー\", \"ko-KR\": \"온라인 사용자\", \"zh-CN\": \"在线用户\", \"zh-TW\": \"在線用戶\"}', 'UsergroupAddOutlined', 'SystemOnline', 'sys:online:view', 2, 1, 1, '2026-01-16 11:51:35', NULL, '2026-01-20 22:07:01', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (643, 1993479636925403138, 'PUBLIC', 1, 642, 'button', NULL, '踢下线', '{\"en-US\": \"Kick Offline\", \"ja-JP\": \"オフラインキック\", \"ko-KR\": \"오프라인 강제 종료\", \"zh-CN\": \"踢下线\", \"zh-TW\": \"踢下線\"}', NULL, NULL, 'sys:online:kickout', 1, 1, 1, '2026-01-16 11:51:47', NULL, '2026-01-18 18:59:17', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (644, 1993479636925403138, 'PUBLIC', 1, 642, 'button', NULL, '批量踢下线', '{\"en-US\": \"Batch Kick Offline\", \"ja-JP\": \"一括オフラインキック\", \"ko-KR\": \"일괄 오프라인 강제 종료\", \"zh-CN\": \"批量踢下线\", \"zh-TW\": \"批量踢下線\"}', NULL, NULL, 'sys:online:batchKickout', 2, 1, 1, '2026-01-16 11:51:47', NULL, '2026-01-18 18:59:17', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (645, 1993479636925403138, 'PUBLIC', 1, 642, 'button', NULL, '导出在线用户', '{\"en-US\": \"Export Online Users\", \"ja-JP\": \"オンラインユーザーエクスポート\", \"ko-KR\": \"온라인 사용자 내보내기\", \"zh-CN\": \"导出在线用户\", \"zh-TW\": \"匯出在線用戶\"}', NULL, NULL, 'sys:online:export', 3, 1, 1, '2026-01-16 11:51:47', NULL, '2026-01-18 18:59:18', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (646, 1993479636925403138, 'PUBLIC', 1, 653, 'menu', 'tenant', '租户管理', '{\"en-US\": \"Tenants\", \"ja-JP\": \"テナント管理\", \"ko-KR\": \"테넌트 관리\", \"zh-CN\": \"租户管理\", \"zh-TW\": \"租戶管理\"}', 'TeamOutlined', 'SystemTenant', 'sys:tenant:view', 2, 1, 1, '2026-01-16 11:51:52', NULL, '2026-01-18 18:55:55', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (647, 1993479636925403138, 'PUBLIC', 1, 646, 'button', NULL, '新增租户', '{\"en-US\": \"Add Tenant\", \"ja-JP\": \"テナント追加\", \"ko-KR\": \"테넌트 추가\", \"zh-CN\": \"新增租户\", \"zh-TW\": \"新增租戶\"}', NULL, NULL, 'sys:tenant:add', 1, 1, 1, '2026-01-16 11:52:01', NULL, '2026-01-18 18:59:18', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (648, 1993479636925403138, 'PUBLIC', 1, 646, 'button', NULL, '编辑租户', '{\"en-US\": \"Edit Tenant\", \"ja-JP\": \"テナント編集\", \"ko-KR\": \"테넌트 편집\", \"zh-CN\": \"编辑租户\", \"zh-TW\": \"編輯租戶\"}', NULL, NULL, 'sys:tenant:edit', 2, 1, 1, '2026-01-16 11:52:01', NULL, '2026-01-18 18:59:39', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (649, 1993479636925403138, 'PUBLIC', 1, 646, 'button', NULL, '删除租户', '{\"en-US\": \"Delete Tenant\", \"ja-JP\": \"テナント削除\", \"ko-KR\": \"테넌트 삭제\", \"zh-CN\": \"删除租户\", \"zh-TW\": \"刪除租戶\"}', NULL, NULL, 'sys:tenant:delete', 3, 1, 1, '2026-01-16 11:52:01', NULL, '2026-01-18 18:59:39', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (650, 1993479636925403138, 'PUBLIC', 1, 646, 'button', NULL, '批量删除租户', '{\"en-US\": \"Batch Delete Tenants\", \"ja-JP\": \"一括削除テナント\", \"ko-KR\": \"일괄 삭제 테넌트\", \"zh-CN\": \"批量删除租户\", \"zh-TW\": \"批量刪除租戶\"}', NULL, NULL, 'sys:tenant:batchDelete', 4, 1, 1, '2026-01-16 11:52:01', NULL, '2026-01-18 18:59:42', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (651, 1993479636925403138, 'PUBLIC', 1, 646, 'button', NULL, '导出租户', '{\"en-US\": \"Export Tenants\", \"ja-JP\": \"テナントエクスポート\", \"ko-KR\": \"테넌트 내보내기\", \"zh-CN\": \"导出租户\", \"zh-TW\": \"匯出租戶\"}', NULL, NULL, 'sys:tenant:export', 5, 1, 1, '2026-01-16 11:52:01', NULL, '2026-01-18 18:59:42', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (652, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'organization', '组织架构', '{\"en-US\": \"Organization\", \"ja-JP\": \"組織構成\", \"ko-KR\": \"조직 구조\", \"zh-CN\": \"组织架构\", \"zh-TW\": \"組織架構\"}', 'ApartmentOutlined', 'SystemOrganization', 'sys:organization:view', 2, 1, 1, '2026-01-17 18:30:38', 'system', '2026-01-18 18:55:14', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (653, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'authorization', '授权管理', '{\"en-US\": \"Authorization\", \"ja-JP\": \"認可管理\", \"ko-KR\": \"인증 관리\", \"zh-CN\": \"授权管理\", \"zh-TW\": \"授權管理\"}', 'SafetyOutlined', 'SystemAuthorization', 'sys:authorization:view', 3, 1, 1, '2026-01-17 18:30:48', 'system', '2026-01-18 18:55:14', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (654, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'maintenance', '系统运维', '{\"en-US\": \"System Maintenance\", \"ja-JP\": \"システム運用\", \"ko-KR\": \"시스템 운영\", \"zh-CN\": \"系统运维\", \"zh-TW\": \"系統運維\"}', 'ToolOutlined', 'SystemMaintenance', 'sys:maintenance:view', 11, 1, 1, '2026-01-20 22:05:54', NULL, '2026-01-20 22:05:54', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (655, 1993479636925403138, 'PUBLIC', 1, 654, 'menu', 'config', '系统配置', '{\"en-US\": \"System Config\", \"ja-JP\": \"システム設定\", \"ko-KR\": \"시스템 설정\", \"zh-CN\": \"系统配置\", \"zh-TW\": \"系統設定\"}', 'SettingOutlined', 'SystemConfig', 'sys:config:view', 1, 1, 1, '2026-01-20 22:06:38', NULL, '2026-01-20 22:06:38', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (656, 1993479636925403138, 'PUBLIC', 1, 655, 'button', 'view', '查看配置', '{\"en-US\": \"View Config\", \"ja-JP\": \"設定表示\", \"ko-KR\": \"설정 보기\", \"zh-CN\": \"查看配置\", \"zh-TW\": \"查看設定\"}', NULL, NULL, 'sys:config:view', 1, 1, 1, '2026-01-20 22:07:56', NULL, '2026-01-20 22:07:56', NULL, 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (657, 1993479636925403138, 'PUBLIC', 1, 655, 'button', 'edit', '编辑配置', '{\"en-US\": \"Edit Config\", \"ja-JP\": \"設定編集\", \"ko-KR\": \"설정 편집\", \"zh-CN\": \"编辑配置\", \"zh-TW\": \"編輯設定\"}', NULL, NULL, 'sys:config:edit', 2, 1, 1, '2026-01-20 22:07:56', NULL, '2026-01-20 22:07:56', NULL, 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (667, 1993479636925403138, 'PUBLIC', 1, 654, 'menu', 'operationLog', '操作日志', '{\"en-US\": \"Operation Log\", \"ja-JP\": \"操作ログ\", \"ko-KR\": \"Operation Log\", \"zh-CN\": \"操作日志\", \"zh-TW\": \"操作日誌\"}', 'FileTextOutlined', 'SystemOperationLog', 'sys:operation-log:view', 3, 1, 1, '2026-01-28 20:51:11', NULL, '2026-01-28 20:51:11', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (668, 1993479636925403138, 'PUBLIC', 1, 667, 'button', NULL, '查询', '{\"en-US\": \"Query\", \"ja-JP\": \"クエリ\", \"ko-KR\": \"Query\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}', NULL, NULL, 'sys:operation-log:query', 1, 1, 1, '2026-01-28 20:51:23', NULL, '2026-01-28 20:51:23', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (669, 1993479636925403138, 'PUBLIC', 1, 667, 'button', NULL, '导出', '{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"Export\", \"zh-CN\": \"导出\", \"zh-TW\": \"匯出\"}', NULL, NULL, 'sys:operation-log:export', 2, 1, 1, '2026-01-28 20:51:34', NULL, '2026-01-28 20:51:34', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (670, 1993479636925403138, 'PUBLIC', 1, 654, 'menu', 'tenantMessageWhitelist', '租户消息白名单', '{\"en-US\": \"Tenant Message Whitelist\", \"ja-JP\": \"テナントメッセージホワイトリスト\", \"ko-KR\": \"Tenant Message Whitelist\", \"zh-CN\": \"租户消息白名单\", \"zh-TW\": \"租戶消息白名單\"}', 'SafetyCertificateOutlined', 'SystemTenantMessageWhitelist', 'sys:tenant-message-whitelist:view', 5, 1, 1, '2026-01-28 20:51:44', NULL, '2026-01-28 20:51:44', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (671, 1993479636925403138, 'PUBLIC', 1, 670, 'button', NULL, '新增', '{\"en-US\": \"Create\", \"ja-JP\": \"作成\", \"ko-KR\": \"Create\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'sys:tenant-message-whitelist:create', 1, 1, 1, '2026-01-28 20:51:59', NULL, '2026-01-28 20:51:59', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (672, 1993479636925403138, 'PUBLIC', 1, 670, 'button', NULL, '修改', '{\"en-US\": \"Update\", \"ja-JP\": \"更新\", \"ko-KR\": \"Update\", \"zh-CN\": \"修改\", \"zh-TW\": \"修改\"}', NULL, NULL, 'sys:tenant-message-whitelist:update', 2, 1, 1, '2026-01-28 20:52:11', NULL, '2026-01-28 20:52:11', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (673, 1993479636925403138, 'PUBLIC', 1, 670, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"Delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'sys:tenant-message-whitelist:delete', 3, 1, 1, '2026-01-28 20:52:19', NULL, '2026-01-28 20:52:19', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (674, 1993479636925403138, 'PUBLIC', 1, 654, 'menu', 'messageTemplate', '消息模板', '{\"en-US\": \"Message Template\", \"ja-JP\": \"メッセージテンプレート\", \"ko-KR\": \"Message Template\", \"zh-CN\": \"消息模板\", \"zh-TW\": \"消息模板\"}', 'MailOutlined', 'SystemMessageTemplate', 'sys:message-template:view', 4, 1, 1, '2026-01-28 20:52:26', NULL, '2026-01-28 20:52:26', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (675, 1993479636925403138, 'PUBLIC', 1, 3000000000000000423, 'menu', 'userTableConfig', '用户列设置', '{\"en-US\": \"User Table Config\", \"ja-JP\": \"ユーザー列設定\", \"ko-KR\": \"사용자 열 설정\", \"zh-CN\": \"用户列设置\", \"zh-TW\": \"用戶列設置\"}', 'ColumnWidthOutlined', 'SystemUserTableConfig', 'sys:userTableConfig:view', 20, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-12 10:48:14', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (676, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'taskConfig', '审批任务配置', '{\"en-US\": \"Task Config\", \"ja-JP\": \"承認タスク設定\", \"ko-KR\": \"승인 작업 구성\", \"zh-CN\": \"审批任务配置\", \"zh-TW\": \"審批任務配置\"}', 'SettingOutlined', 'ApprovalTaskConfig', 'wf:taskConfig:view', 10, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (677, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'execution/start', '发起审批', '{\"en-US\": \"Start Approval\", \"ja-JP\": \"承認開始\", \"ko-KR\": \"승인 시작\", \"zh-CN\": \"发起审批\", \"zh-TW\": \"發起審批\"}', 'PlayCircleOutlined', 'ApprovalExecutionStart', 'wf:execution:start', 20, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (678, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'my/pending', '我的待办', '{\"en-US\": \"My Pending\", \"ja-JP\": \"私の保留中\", \"ko-KR\": \"내 대기\", \"zh-CN\": \"我的待办\", \"zh-TW\": \"我的待辦\"}', 'ClockCircleOutlined', 'ApprovalMyPending', 'wf:myTask:pending', 30, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (679, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'my/processed', '我已处理', '{\"en-US\": \"My Processed\", \"ja-JP\": \"処理済み\", \"ko-KR\": \"처리 완료\", \"zh-CN\": \"我已处理\", \"zh-TW\": \"我已處理\"}', 'CheckCircleOutlined', 'ApprovalMyProcessed', 'wf:myTask:processed', 40, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (680, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'my/initiated', '我发起的', '{\"en-US\": \"My Initiated\", \"ja-JP\": \"開始した\", \"ko-KR\": \"시작한\", \"zh-CN\": \"我发起的\", \"zh-TW\": \"我發起的\"}', 'SendOutlined', 'ApprovalMyInitiated', 'wf:myTask:initiated', 50, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (683, 1993479636925403138, 'PUBLIC', 3, 676, 'button', NULL, '新增审批任务', '{\"en-US\": \"Add Task Config\", \"ja-JP\": \"承認タスク追加\", \"ko-KR\": \"승인 작업 추가\", \"zh-CN\": \"新增审批任务\", \"zh-TW\": \"新增審批任務\"}', NULL, NULL, 'wf:taskConfig:add', 1, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (684, 1993479636925403138, 'PUBLIC', 3, 676, 'button', NULL, '配置审批流程', '{\"en-US\": \"Configure Workflow\", \"ja-JP\": \"承認フロー設定\", \"ko-KR\": \"승인 흐름 구성\", \"zh-CN\": \"配置审批流程\", \"zh-TW\": \"配置審批流程\"}', NULL, NULL, 'wf:taskConfig:config', 4, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (685, 1993479636925403138, 'PUBLIC', 3, 676, 'button', NULL, '删除审批任务', '{\"en-US\": \"Delete Task Config\", \"ja-JP\": \"承認タスク削除\", \"ko-KR\": \"승인 작업 삭제\", \"zh-CN\": \"删除审批任务\", \"zh-TW\": \"刪除審批任務\"}', NULL, NULL, 'wf:taskConfig:delete', 3, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (686, 1993479636925403138, 'PUBLIC', 3, 676, 'button', NULL, '编辑审批任务', '{\"en-US\": \"Edit Task Config\", \"ja-JP\": \"承認タスク編集\", \"ko-KR\": \"승인 작업 편집\", \"zh-CN\": \"编辑审批任务\", \"zh-TW\": \"編輯審批任務\"}', NULL, NULL, 'wf:taskConfig:edit', 2, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (687, 1993479636925403138, 'PUBLIC', 3, 678, 'button', NULL, '同意审批', '{\"en-US\": \"Approve\", \"ja-JP\": \"承認\", \"ko-KR\": \"승인\", \"zh-CN\": \"同意审批\", \"zh-TW\": \"同意審批\"}', NULL, NULL, 'wf:execution:approve', 1, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (688, 1993479636925403138, 'PUBLIC', 3, 678, 'button', NULL, '驳回审批', '{\"en-US\": \"Reject\", \"ja-JP\": \"却下\", \"ko-KR\": \"거부\", \"zh-CN\": \"驳回审批\", \"zh-TW\": \"駁回審批\"}', NULL, NULL, 'wf:execution:reject', 2, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (689, 1993479636925403138, 'PUBLIC', 3, 680, 'button', NULL, '撤销审批', '{\"en-US\": \"Cancel Execution\", \"ja-JP\": \"承認取消\", \"ko-KR\": \"승인 취소\", \"zh-CN\": \"撤销审批\", \"zh-TW\": \"撤銷審批\"}', NULL, NULL, 'wf:execution:cancel', 1, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (690, 1993479636925403138, 'PUBLIC', 1, 2, 'button', NULL, '绑定人员', '{\"en-US\": \"sys:role:authUser\", \"ja-JP\": \"绑定人员\", \"ko-KR\": \"sys:role:authUser\", \"zh-CN\": \"绑定人员\", \"zh-TW\": \"绑定人员\"}', NULL, NULL, 'sys:role:authUser', 6, 1, 1, '2026-04-02 16:37:51', 'codex', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (691, 1, 'PUBLIC', 3, 0, 'menu', 'dashboard', '审批工作台', '{\"en-US\": \"Approval Dashboard\", \"ja-JP\": \"承認ダッシュボード\", \"ko-KR\": \"승인 대시보드\", \"zh-CN\": \"审批工作台\", \"zh-TW\": \"審批工作台\"}', 'DashboardOutlined', 'ApprovalDashboard', 'wf:dashboard:view', 5, 1, 1, '2026-04-06 19:49:39', NULL, '2026-04-07 20:18:40', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (692, 1, 'PUBLIC', 4, 0, 'menu', 'role', '角色管理', '{\"en-US\": \"role\", \"ja-JP\": \"角色管理\", \"ko-KR\": \"role\", \"zh-CN\": \"角色管理\", \"zh-TW\": \"角色管理\"}', 'TeamOutlined', 'SystemRole', 'sys:role:view', 2, 1, 1, '2026-04-06 23:16:34', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (693, 1, 'PUBLIC', 4, 692, 'menu', 'menu-grant/:roleId', '菜单授权', '{\"en-US\": \"menu-grant/:roleId\", \"ja-JP\": \"菜单授权\", \"ko-KR\": \"menu-grant/:roleId\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"菜单授权\"}', 'SafetyCertificateOutlined', 'SystemRoleMenuGrant', 'sys:role:authMenu', 1, 0, 1, '2026-04-06 23:16:57', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (694, 1, 'PUBLIC', 4, 692, 'menu', 'user-grant/:roleId', '人员授权', '{\"en-US\": \"user-grant/:roleId\", \"ja-JP\": \"人员授权\", \"ko-KR\": \"user-grant/:roleId\", \"zh-CN\": \"人员授权\", \"zh-TW\": \"人员授权\"}', 'UsergroupAddOutlined', 'SystemRoleUserGrant', 'sys:role:authUser', 2, 0, 1, '2026-04-06 23:16:59', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (695, 1993479636925403138, 'PUBLIC', 1, 653, 'menu', 'menu-grant/:roleId', '菜单授权', '{\"en-US\": \"menu-grant/:roleId\", \"ja-JP\": \"菜单授权\", \"ko-KR\": \"menu-grant/:roleId\", \"zh-CN\": \"菜单授权\", \"zh-TW\": \"菜单授权\"}', 'SafetyCertificateOutlined', 'SystemRoleMenuGrant', 'sys:role:authMenu', 1, 0, 1, '2026-04-06 23:20:21', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (696, 1993479636925403138, 'PUBLIC', 1, 653, 'menu', 'user-grant/:roleId', '人员授权', '{\"en-US\": \"user-grant/:roleId\", \"ja-JP\": \"人员授权\", \"ko-KR\": \"user-grant/:roleId\", \"zh-CN\": \"人员授权\", \"zh-TW\": \"人员授权\"}', 'UsergroupAddOutlined', 'SystemRoleUserGrant', 'sys:role:authUser', 2, 0, 1, '2026-04-06 23:20:23', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (697, 1993479636925403138, 'PUBLIC', 3, 0, 'menu', 'dashboard', '审批工作台', '{\"en-US\": \"Approval Dashboard\", \"ja-JP\": \"承認ダッシュボード\", \"ko-KR\": \"승인 대시보드\", \"zh-CN\": \"审批工作台\", \"zh-TW\": \"審批工作台\"}', 'DashboardOutlined', 'ApprovalDashboard', 'wf:dashboard:view', 5, 1, 1, '2026-04-07 09:48:56', 'system', '2026-04-07 20:18:40', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (698, 1993479636925403138, 'PUBLIC', 5, 0, 'catalog', 'basicInfo', '基础信息', '{\"en-US\": \"Basic Information\", \"ja-JP\": \"基本情報\", \"ko-KR\": \"기본 정보\", \"zh-CN\": \"基础信息\", \"zh-TW\": \"基礎資訊\"}', 'BookOutlined', NULL, 'basic:catalog:view', 10, 0, 0, '2026-04-09 18:10:41', 'system', '2026-04-29 23:46:01', 'system', 1, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (699, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'dashboard', '基础信息主页', '{\"en-US\": \"Basic Dashboard\", \"ja-JP\": \"基本情報ホーム\", \"ko-KR\": \"기본 정보 홈\", \"zh-CN\": \"基础信息主页\", \"zh-TW\": \"基礎資訊首頁\"}', 'DashboardOutlined', 'BasicDashboard', 'basic:dashboard:view', 1, 1, 1, '2026-04-09 18:10:41', 'system', '2026-04-29 22:20:00', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (700, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'encodeRule', '编码规则管理', '{\"en-US\": \"Encoding Rule Management\", \"ja-JP\": \"採番ルール管理\", \"ko-KR\": \"인코딩 규칙 관리\", \"zh-CN\": \"编码规则管理\", \"zh-TW\": \"編碼規則管理\"}', 'CodeOutlined', 'BasicEncodeRule', 'basic:encodeRule:query', 10, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 22:20:00', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (701, 1993479636925403138, 'PUBLIC', 5, 700, 'menu', '/encode/rule', '规则管理', '{\"en-US\": \"Rule Management\", \"ja-JP\": \"ルール管理\", \"ko-KR\": \"규칙 관리\", \"zh-CN\": \"规则管理\", \"zh-TW\": \"規則管理\"}', 'icon-setting', 'system/encodeRule', 'menu:encode:rule', 1, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-12 11:04:41', 'codex', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (702, 1993479636925403138, 'PUBLIC', 5, 700, 'menu', '/encode/example', '示例管理', '{\"en-US\": \"Example Management\", \"ja-JP\": \"サンプル管理\", \"ko-KR\": \"예시 관리\", \"zh-CN\": \"示例管理\", \"zh-TW\": \"示例管理\"}', 'icon-example', 'system/encodeRuleExample', 'menu:encode:example', 2, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-12 11:04:41', 'codex', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (703, 1993479636925403138, 'PUBLIC', 5, 700, 'menu', '/encode/history', '历史记录', '{\"en-US\": \"History Records\", \"ja-JP\": \"履歴レコード\", \"ko-KR\": \"이력 기록\", \"zh-CN\": \"历史记录\", \"zh-TW\": \"歷史記錄\"}', 'icon-history', 'system/encodeHistory', 'menu:encode:history', 3, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-12 11:04:41', 'codex', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (704, 1993479636925403138, 'PUBLIC', 1, 702, 'button', NULL, '新增', '{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'encode:example:add', 1, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (705, 1993479636925403138, 'PUBLIC', 1, 702, 'button', NULL, '编辑', '{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}', NULL, NULL, 'encode:example:edit', 2, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (706, 1993479636925403138, 'PUBLIC', 1, 702, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'encode:example:delete', 3, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (707, 1993479636925403138, 'PUBLIC', 1, 702, 'button', NULL, '查看', '{\"en-US\": \"View\", \"ja-JP\": \"表示\", \"ko-KR\": \"보기\", \"zh-CN\": \"查看\", \"zh-TW\": \"查看\"}', NULL, NULL, 'encode:example:view', 4, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (708, 1993479636925403138, 'PUBLIC', 1, 703, 'button', NULL, '查询', '{\"en-US\": \"Query\", \"ja-JP\": \"照会\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}', NULL, NULL, 'encode:history:query', 1, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (709, 1993479636925403138, 'PUBLIC', 1, 703, 'button', NULL, '导出', '{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"내보내기\", \"zh-CN\": \"导出\", \"zh-TW\": \"導出\"}', NULL, NULL, 'encode:history:export', 2, 0, 0, '2026-04-10 11:29:55', NULL, '2026-04-10 22:27:10', 'codex', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (711, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '新增', '{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'basic:encodeRule:add', 1, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (712, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '编辑', '{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}', NULL, NULL, 'basic:encodeRule:edit', 2, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (713, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'basic:encodeRule:delete', 3, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (714, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '查看', '{\"en-US\": \"View\", \"ja-JP\": \"表示\", \"ko-KR\": \"보기\", \"zh-CN\": \"查看\", \"zh-TW\": \"查看\"}', NULL, NULL, 'basic:encodeRule:query', 4, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (715, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '测试', '{\"en-US\": \"Test\", \"ja-JP\": \"テスト\", \"ko-KR\": \"테스트\", \"zh-CN\": \"测试\", \"zh-TW\": \"測試\"}', NULL, NULL, 'basic:encodeRule:test', 5, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (716, 1993479636925403138, 'PUBLIC', 5, 700, 'button', NULL, '生成编码', '{\"en-US\": \"Generate Code\", \"ja-JP\": \"コード生成\", \"ko-KR\": \"코드 생성\", \"zh-CN\": \"生成编码\", \"zh-TW\": \"生成編碼\"}', NULL, NULL, 'basic:encodeRule:generate', 6, 1, 1, '2026-04-10 11:29:55', NULL, '2026-04-29 21:31:09', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (720, 1993479636925403138, 'PUBLIC', 1, 730, 'menu', 'i18nLanguageType', '语言配置', '{\"en-US\": \"Language Configuration\", \"ja-JP\": \"言語設定\", \"ko-KR\": \"언어 설정\", \"zh-CN\": \"语言配置\", \"zh-TW\": \"語言設定\"}', 'GlobalOutlined', 'SystemI18nLanguageType', 'sys:i18nLanguageType:view', 10, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (721, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '新增', '{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'sys:i18nLanguageType:add', 1, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (722, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '编辑', '{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"수정\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}', NULL, NULL, 'sys:i18nLanguageType:edit', 2, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (723, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'sys:i18nLanguageType:delete', 3, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (724, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '设为默认', '{\"en-US\": \"Set Default\", \"ja-JP\": \"既定に設定\", \"ko-KR\": \"기본값으로 설정\", \"zh-CN\": \"设为默认\", \"zh-TW\": \"設為預設\"}', NULL, NULL, 'sys:i18nLanguageType:setDefault', 4, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (725, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '导入', '{\"en-US\": \"Import\", \"ja-JP\": \"インポート\", \"ko-KR\": \"가져오기\", \"zh-CN\": \"导入\", \"zh-TW\": \"導入\"}', NULL, NULL, 'sys:i18nLanguageType:import', 5, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (726, 1993479636925403138, 'PUBLIC', 1, 720, 'button', NULL, '下载模板', '{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートをダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}', NULL, NULL, 'sys:i18nLanguageType:template:download', 6, 1, 1, '2026-04-10 13:34:03', 'codex', '2026-04-10 22:27:10', 'codex', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (730, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'i18nConfig', '多语言配置', '{\"en-US\": \"I18n Config\", \"ja-JP\": \"多言語設定\", \"ko-KR\": \"다국어 설정\", \"zh-CN\": \"多语言配置\", \"zh-TW\": \"多語言設定\"}', 'TranslationOutlined', NULL, NULL, 85, 1, 1, '2026-04-14 13:59:46', '20260414_fix', '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (731, 1993479636925403138, 'PUBLIC', 1, 730, 'menu', 'i18nMessage', '多语言消息', '{\"en-US\": \"I18n Message\", \"ja-JP\": \"多言語メッセージ\", \"ko-KR\": \"다국어 메시지\", \"zh-CN\": \"多语言消息\", \"zh-TW\": \"多語言訊息\"}', 'MessageOutlined', 'SystemI18nMessage', 'sys:i18nMessage:view', 20, 1, 1, '2026-04-14 13:59:46', '20260414_fix', '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (732, 1993479636925403138, 'PUBLIC', 1, 731, 'button', NULL, '新增多语言消息', '{\"en-US\": \"sys:i18nMessage:add\", \"ja-JP\": \"新增多语言消息\", \"ko-KR\": \"sys:i18nMessage:add\", \"zh-CN\": \"新增多语言消息\", \"zh-TW\": \"新增多语言消息\"}', NULL, NULL, 'sys:i18nMessage:add', 1, 1, 1, '2026-04-14 13:59:46', '20260414_fix', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (733, 1993479636925403138, 'PUBLIC', 1, 731, 'button', NULL, '编辑多语言消息', '{\"en-US\": \"sys:i18nMessage:edit\", \"ja-JP\": \"编辑多语言消息\", \"ko-KR\": \"sys:i18nMessage:edit\", \"zh-CN\": \"编辑多语言消息\", \"zh-TW\": \"编辑多语言消息\"}', NULL, NULL, 'sys:i18nMessage:edit', 2, 1, 1, '2026-04-14 13:59:46', '20260414_fix', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (734, 1993479636925403138, 'PUBLIC', 1, 731, 'button', NULL, '删除多语言消息', '{\"en-US\": \"sys:i18nMessage:delete\", \"ja-JP\": \"删除多语言消息\", \"ko-KR\": \"sys:i18nMessage:delete\", \"zh-CN\": \"删除多语言消息\", \"zh-TW\": \"删除多语言消息\"}', NULL, NULL, 'sys:i18nMessage:delete', 3, 1, 1, '2026-04-14 13:59:46', '20260414_fix', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (735, 1993479636925403138, 'PUBLIC', 1, 652, 'menu', 'inviteCode', '邀请码管理', '{\"en-US\": \"Invite Code Management\", \"ja-JP\": \"招待コード管理\", \"ko-KR\": \"초대 코드 관리\", \"zh-CN\": \"邀请码管理\", \"zh-TW\": \"邀請碼管理\"}', 'KeyOutlined', 'SystemInviteCode', 'sys:invite-code:view', 15, 1, 1, '2026-04-10 19:52:26', 'system', '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (736, 1993479636925403138, 'PUBLIC', 1, 735, 'button', '', '新增邀请码', '{\"en-US\": \"sys:invite-code:add\", \"ja-JP\": \"新增邀请码\", \"ko-KR\": \"sys:invite-code:add\", \"zh-CN\": \"新增邀请码\", \"zh-TW\": \"新增邀请码\"}', NULL, NULL, 'sys:invite-code:add', 1, 1, 1, '2026-04-10 19:52:26', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (737, 1993479636925403138, 'PUBLIC', 1, 735, 'button', '', '停用邀请码', '{\"en-US\": \"sys:invite-code:edit\", \"ja-JP\": \"停用邀请码\", \"ko-KR\": \"sys:invite-code:edit\", \"zh-CN\": \"停用邀请码\", \"zh-TW\": \"停用邀请码\"}', NULL, NULL, 'sys:invite-code:edit', 2, 1, 1, '2026-04-10 19:52:26', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (738, 1993479636925403138, 'PUBLIC', 1, 735, 'button', '', '删除邀请码', '{\"en-US\": \"sys:invite-code:delete\", \"ja-JP\": \"删除邀请码\", \"ko-KR\": \"sys:invite-code:delete\", \"zh-CN\": \"删除邀请码\", \"zh-TW\": \"删除邀请码\"}', NULL, NULL, 'sys:invite-code:delete', 3, 1, 1, '2026-04-10 19:52:26', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (739, 1993479636925403138, 'PUBLIC', 1, 735, 'button', '', '查看使用记录', '{\"en-US\": \"sys:invite-code:record:view\", \"ja-JP\": \"查看使用记录\", \"ko-KR\": \"sys:invite-code:record:view\", \"zh-CN\": \"查看使用记录\", \"zh-TW\": \"查看使用记录\"}', NULL, NULL, 'sys:invite-code:record:view', 4, 1, 1, '2026-04-10 19:52:26', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (740, 1993479636925403138, 'PUBLIC', 6, 0, 'menu', 'integration', '接口平台主页', '{\"en-US\": \"Integration Platform Home\", \"zh-CN\": \"接口平台主页\"}', 'ApiOutlined', 'IntegrationHome', 'integration:home:view', 5, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (741, 1993479636925403138, 'PUBLIC', 6, 0, 'menu', 'thirdSystem', '第三方系统管理', '{\"en-US\": \"Third System Management\", \"ja-JP\": \"サードパーティシステム管理\", \"ko-KR\": \"타사 시스템 관리\", \"zh-CN\": \"第三方系统管理\", \"zh-TW\": \"第三方系統管理\"}', 'ApartmentOutlined', 'IntegrationThirdSystem', 'integration:third-system:view', 10, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (742, 1993479636925403138, 'PUBLIC', 6, 741, 'button', NULL, '新增系统', '{\"en-US\": \"Add System\", \"ja-JP\": \"システム追加\", \"ko-KR\": \"시스템 추가\", \"zh-CN\": \"新增系统\", \"zh-TW\": \"新增系統\"}', NULL, NULL, 'integration:third-system:add', 1, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (743, 1993479636925403138, 'PUBLIC', 6, 741, 'button', NULL, '编辑系统', '{\"en-US\": \"Edit System\", \"ja-JP\": \"システム編集\", \"ko-KR\": \"시스템 편집\", \"zh-CN\": \"编辑系统\", \"zh-TW\": \"編輯系統\"}', NULL, NULL, 'integration:third-system:edit', 2, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (744, 1993479636925403138, 'PUBLIC', 6, 741, 'button', NULL, '删除系统', '{\"en-US\": \"Delete System\", \"ja-JP\": \"システム削除\", \"ko-KR\": \"시스템 삭제\", \"zh-CN\": \"删除系统\", \"zh-TW\": \"刪除系統\"}', NULL, NULL, 'integration:third-system:delete', 3, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (745, 1993479636925403138, 'PUBLIC', 6, 741, 'button', NULL, '批量删除', '{\"en-US\": \"Batch Delete\", \"ja-JP\": \"一括削除\", \"ko-KR\": \"일괄 삭제\", \"zh-CN\": \"批量删除\", \"zh-TW\": \"批次刪除\"}', NULL, NULL, 'integration:third-system:batch-delete', 4, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (746, 1993479636925403138, 'PUBLIC', 6, 741, 'button', NULL, '授权', '{\"en-US\": \"Authorization\", \"ja-JP\": \"認証\", \"ko-KR\": \"권한 설정\", \"zh-CN\": \"授权\", \"zh-TW\": \"授權\"}', NULL, NULL, 'integration:third-system:auth', 5, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (747, 1993479636925403138, 'PUBLIC', 6, 0, 'menu', 'apiConfig', '接口配置管理', '{\"en-US\": \"API Config Management\", \"ja-JP\": \"API設定管理\", \"ko-KR\": \"API 설정 관리\", \"zh-CN\": \"接口配置管理\", \"zh-TW\": \"介面設定管理\"}', 'ApiOutlined', 'IntegrationApiConfig', 'integration:api-config:view', 20, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (748, 1993479636925403138, 'PUBLIC', 6, 747, 'button', NULL, '新增接口', '{\"en-US\": \"Add API\", \"ja-JP\": \"API追加\", \"ko-KR\": \"API 추가\", \"zh-CN\": \"新增接口\", \"zh-TW\": \"新增介面\"}', NULL, NULL, 'integration:api-config:add', 1, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (749, 1993479636925403138, 'PUBLIC', 6, 747, 'button', NULL, '编辑接口', '{\"en-US\": \"Edit API\", \"ja-JP\": \"API編集\", \"ko-KR\": \"API 편집\", \"zh-CN\": \"编辑接口\", \"zh-TW\": \"編輯介面\"}', NULL, NULL, 'integration:api-config:edit', 2, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (750, 1993479636925403138, 'PUBLIC', 6, 747, 'button', NULL, '删除接口', '{\"en-US\": \"Delete API\", \"ja-JP\": \"API削除\", \"ko-KR\": \"API 삭제\", \"zh-CN\": \"删除接口\", \"zh-TW\": \"刪除介面\"}', NULL, NULL, 'integration:api-config:delete', 3, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (751, 1993479636925403138, 'PUBLIC', 6, 747, 'button', NULL, '配置参数', '{\"en-US\": \"Configure Parameters\", \"ja-JP\": \"パラメータ設定\", \"ko-KR\": \"매개변수 설정\", \"zh-CN\": \"配置参数\", \"zh-TW\": \"配置參數\"}', NULL, NULL, 'integration:api-config:config-param', 4, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (752, 1993479636925403138, 'PUBLIC', 6, 747, 'button', NULL, '配置映射', '{\"en-US\": \"Configure Mapping\", \"ja-JP\": \"マッピング設定\", \"ko-KR\": \"매핑 설정\", \"zh-CN\": \"配置映射\", \"zh-TW\": \"配置映射\"}', NULL, NULL, 'integration:api-config:config-mapping', 5, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (753, 1993479636925403138, 'PUBLIC', 6, 0, 'menu', 'apiCallLog', '调用记录查询', '{\"en-US\": \"API Call Log\", \"ja-JP\": \"API呼び出しログ\", \"ko-KR\": \"API 호출 로그\", \"zh-CN\": \"调用记录查询\", \"zh-TW\": \"呼叫記錄查詢\"}', 'FileSearchOutlined', 'IntegrationApiCallLog', 'integration:api-call-log:view', 30, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-27 14:55:15', '20260427_fix_integration_menu_root_level', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (754, 1993479636925403138, 'PUBLIC', 5, 0, 'catalog', 'label', '标签管理', '{\"en-US\": \"Label Management\", \"ja-JP\": \"ラベル管理\", \"ko-KR\": \"라벨 관리\", \"zh-CN\": \"标签管理\", \"zh-TW\": \"標籤管理\"}', 'TagsOutlined', NULL, NULL, 10, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-17 10:50:12', NULL, 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (755, 1993479636925403138, 'PUBLIC', 5, 754, 'menu', 'template', '标签模板', '{\"en-US\": \"Label Template\", \"ja-JP\": \"ラベルテンプレート\", \"ko-KR\": \"라벨 템플릿\", \"zh-CN\": \"标签模板\", \"zh-TW\": \"標籤模板\"}', 'FileTextOutlined', 'LabelTemplate', 'label:template:view', 1, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-17 10:51:24', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (756, 1993479636925403138, 'PUBLIC', 5, 754, 'menu', 'print', '标签打印', '{\"en-US\": \"Label Print\", \"ja-JP\": \"ラベル印刷\", \"ko-KR\": \"라벨 인쇄\", \"zh-CN\": \"标签打印\", \"zh-TW\": \"標籤打印\"}', 'PrinterOutlined', 'LabelPrint', 'label:print:view', 2, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-17 10:51:29', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (757, 1993479636925403138, 'PUBLIC', 5, 754, 'menu', 'record', '打印记录', '{\"en-US\": \"Print Record\", \"ja-JP\": \"印刷記録\", \"ko-KR\": \"인쇄 기록\", \"zh-CN\": \"打印记录\", \"zh-TW\": \"打印記錄\"}', 'HistoryOutlined', 'LabelRecord', 'label:record:view', 3, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-17 10:51:33', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (758, 1993479636925403138, 'PUBLIC', 5, 754, 'menu', 'binding', '模板绑定', '{\"en-US\": \"Template Binding\", \"ja-JP\": \"テンプレートバインディング\", \"ko-KR\": \"템플릿 바인딩\", \"zh-CN\": \"模板绑定\", \"zh-TW\": \"模板綁定\"}', 'LinkOutlined', 'LabelBinding', 'label:binding:view', 4, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-17 10:51:40', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (759, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '新增模板', '{\"en-US\": \"label:template:add\", \"ja-JP\": \"新增模板\", \"ko-KR\": \"label:template:add\", \"zh-CN\": \"新增模板\", \"zh-TW\": \"新增模板\"}', NULL, NULL, 'label:template:add', 1, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (760, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '编辑模板', '{\"en-US\": \"label:template:edit\", \"ja-JP\": \"编辑模板\", \"ko-KR\": \"label:template:edit\", \"zh-CN\": \"编辑模板\", \"zh-TW\": \"编辑模板\"}', NULL, NULL, 'label:template:edit', 2, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (761, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '删除模板', '{\"en-US\": \"label:template:delete\", \"ja-JP\": \"删除模板\", \"ko-KR\": \"label:template:delete\", \"zh-CN\": \"删除模板\", \"zh-TW\": \"删除模板\"}', NULL, NULL, 'label:template:delete', 3, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (762, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '批量删除', '{\"en-US\": \"label:template:batchDelete\", \"ja-JP\": \"批量删除\", \"ko-KR\": \"label:template:batchDelete\", \"zh-CN\": \"批量删除\", \"zh-TW\": \"批量删除\"}', NULL, NULL, 'label:template:batchDelete', 4, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (763, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '查看模板', '{\"en-US\": \"label:template:view\", \"ja-JP\": \"查看模板\", \"ko-KR\": \"label:template:view\", \"zh-CN\": \"查看模板\", \"zh-TW\": \"查看模板\"}', NULL, NULL, 'label:template:view', 5, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (764, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '设为默认', '{\"en-US\": \"label:template:setDefault\", \"ja-JP\": \"设为默认\", \"ko-KR\": \"label:template:setDefault\", \"zh-CN\": \"设为默认\", \"zh-TW\": \"设为默认\"}', NULL, NULL, 'label:template:setDefault', 6, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (765, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '复制模板', '{\"en-US\": \"label:template:copy\", \"ja-JP\": \"复制模板\", \"ko-KR\": \"label:template:copy\", \"zh-CN\": \"复制模板\", \"zh-TW\": \"复制模板\"}', NULL, NULL, 'label:template:copy', 7, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (766, 1993479636925403138, 'PUBLIC', 5, 755, 'button', NULL, '模板管理', '{\"en-US\": \"label:template:manage\", \"ja-JP\": \"模板管理\", \"ko-KR\": \"label:template:manage\", \"zh-CN\": \"模板管理\", \"zh-TW\": \"模板管理\"}', NULL, NULL, 'label:template:manage', 8, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (767, 1993479636925403138, 'PUBLIC', 5, 756, 'button', NULL, '执行打印', '{\"en-US\": \"label:print:execute\", \"ja-JP\": \"执行打印\", \"ko-KR\": \"label:print:execute\", \"zh-CN\": \"执行打印\", \"zh-TW\": \"执行打印\"}', NULL, NULL, 'label:print:execute', 1, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (768, 1993479636925403138, 'PUBLIC', 5, 756, 'button', NULL, '打印设置', '{\"en-US\": \"label:print:settings\", \"ja-JP\": \"打印设置\", \"ko-KR\": \"label:print:settings\", \"zh-CN\": \"打印设置\", \"zh-TW\": \"打印设置\"}', NULL, NULL, 'label:print:settings', 2, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (769, 1993479636925403138, 'PUBLIC', NULL, 757, 'button', NULL, '查看详情', '{\"en-US\": \"label:record:query\", \"ja-JP\": \"查看详情\", \"ko-KR\": \"label:record:query\", \"zh-CN\": \"查看详情\", \"zh-TW\": \"查看详情\"}', NULL, NULL, 'label:record:query', 1, 1, 1, '2026-04-17 13:48:42', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (770, 1993479636925403138, 'PUBLIC', NULL, 757, 'button', NULL, '补打', '{\"en-US\": \"label:print:reprint\", \"ja-JP\": \"补打\", \"ko-KR\": \"label:print:reprint\", \"zh-CN\": \"补打\", \"zh-TW\": \"补打\"}', NULL, NULL, 'label:print:reprint', 2, 1, 1, '2026-04-17 13:48:42', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (771, 1993479636925403138, 'PUBLIC', 5, 758, 'button', NULL, '新增绑定', '{\"en-US\": \"label:binding:add\", \"ja-JP\": \"新增绑定\", \"ko-KR\": \"label:binding:add\", \"zh-CN\": \"新增绑定\", \"zh-TW\": \"新增绑定\"}', NULL, NULL, 'label:binding:add', 1, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (772, 1993479636925403138, 'PUBLIC', 5, 758, 'button', NULL, '编辑绑定', '{\"en-US\": \"label:binding:edit\", \"ja-JP\": \"编辑绑定\", \"ko-KR\": \"label:binding:edit\", \"zh-CN\": \"编辑绑定\", \"zh-TW\": \"编辑绑定\"}', NULL, NULL, 'label:binding:edit', 2, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (773, 1993479636925403138, 'PUBLIC', 5, 758, 'button', NULL, '删除绑定', '{\"en-US\": \"label:binding:delete\", \"ja-JP\": \"删除绑定\", \"ko-KR\": \"label:binding:delete\", \"zh-CN\": \"删除绑定\", \"zh-TW\": \"删除绑定\"}', NULL, NULL, 'label:binding:delete', 3, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (774, 1993479636925403138, 'PUBLIC', 5, 758, 'button', NULL, '绑定管理', '{\"en-US\": \"label:binding:manage\", \"ja-JP\": \"绑定管理\", \"ko-KR\": \"label:binding:manage\", \"zh-CN\": \"绑定管理\", \"zh-TW\": \"绑定管理\"}', NULL, NULL, 'label:binding:manage', 4, 1, 1, '2026-04-17 10:33:40', 'system', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000422, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'excelConfig', 'Excel配置', '{\"en-US\": \"Excel Config\", \"ja-JP\": \"Excel設定\", \"ko-KR\": \"Excel 설정\", \"zh-CN\": \"Excel配置\", \"zh-TW\": \"Excel配置\"}', 'FileExcelOutlined', NULL, NULL, 5, 1, 1, '2026-04-12 10:44:34', 'codex', '2026-04-12 11:11:25', 'codex', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000423, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'pageTableConfig', '页表配置', '{\"en-US\": \"Page Table Config\", \"ja-JP\": \"ページテーブル設定\", \"ko-KR\": \"페이지 테이블 설정\", \"zh-CN\": \"页表配置\", \"zh-TW\": \"頁表配置\"}', 'TableOutlined', NULL, NULL, 8, 1, 1, '2026-04-12 10:44:34', 'codex', '2026-04-12 11:11:25', 'codex', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000424, 1993479636925403138, 'PUBLIC', 1, 0, 'menu', 'file', '文件管理', '{\"en-US\": \"File Management\", \"ja-JP\": \"ファイル管理\", \"ko-KR\": \"파일 관리\", \"zh-CN\": \"文件管理\", \"zh-TW\": \"檔案管理\"}', 'FolderOpenOutlined', 'SystemFile', 'sys:file:view', 95, 1, 1, '2026-04-20 22:37:54', '20260420_init', '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000425, 1993479636925403138, 'PUBLIC', 1, 3000000000000000424, 'button', NULL, '文件上传', '{\"en-US\": \"sys:file:upload\", \"ja-JP\": \"文件上传\", \"ko-KR\": \"sys:file:upload\", \"zh-CN\": \"文件上传\", \"zh-TW\": \"文件上传\"}', NULL, NULL, 'sys:file:upload', 1, 1, 1, '2026-04-20 22:37:54', '20260420_init', '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000426, 0, 'PUBLIC', NULL, 0, 'menu', '/system/codegen', '在线开发', '{\"en-US\": \"/system/codegen\", \"ja-JP\": \"在线开发\", \"ko-KR\": \"/system/codegen\", \"zh-CN\": \"在线开发\", \"zh-TW\": \"在线开发\"}', 'CodeOutlined', 'SystemCodegen', NULL, 91, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000427, 0, 'PUBLIC', NULL, 0, 'menu', '/system/codegenDatasource', '代码生成数据源', '{\"en-US\": \"/system/codegenDatasource\", \"ja-JP\": \"代码生成数据源\", \"ko-KR\": \"/system/codegenDatasource\", \"zh-CN\": \"代码生成数据源\", \"zh-TW\": \"代码生成数据源\"}', 'DatabaseOutlined', 'SystemCodegenDatasource', NULL, 92, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000428, 0, 'PUBLIC', NULL, 3000000000000000426, 'button', NULL, '代码预览', '{\"en-US\": \"sys:codegen:preview\", \"ja-JP\": \"代码预览\", \"ko-KR\": \"sys:codegen:preview\", \"zh-CN\": \"代码预览\", \"zh-TW\": \"代码预览\"}', NULL, NULL, 'sys:codegen:preview', 1, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000429, 0, 'PUBLIC', NULL, 3000000000000000426, 'button', NULL, '下载ZIP', '{\"en-US\": \"sys:codegen:download\", \"ja-JP\": \"下载ZIP\", \"ko-KR\": \"sys:codegen:download\", \"zh-CN\": \"下载ZIP\", \"zh-TW\": \"下载ZIP\"}', NULL, NULL, 'sys:codegen:download', 2, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000430, 0, 'PUBLIC', NULL, 3000000000000000427, 'button', NULL, '查询', '{\"en-US\": \"sys:codegenDatasource:page\", \"ja-JP\": \"查询\", \"ko-KR\": \"sys:codegenDatasource:page\", \"zh-CN\": \"查询\", \"zh-TW\": \"查询\"}', NULL, NULL, 'sys:codegenDatasource:page', 1, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000431, 0, 'PUBLIC', NULL, 3000000000000000427, 'button', NULL, '新增', '{\"en-US\": \"sys:codegenDatasource:save\", \"ja-JP\": \"新增\", \"ko-KR\": \"sys:codegenDatasource:save\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'sys:codegenDatasource:save', 2, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000432, 0, 'PUBLIC', NULL, 3000000000000000427, 'button', NULL, '删除', '{\"en-US\": \"sys:codegenDatasource:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegenDatasource:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}', NULL, NULL, 'sys:codegenDatasource:delete', 3, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000433, 0, 'PUBLIC', NULL, 3000000000000000427, 'button', NULL, '测试连接', '{\"en-US\": \"sys:codegenDatasource:test\", \"ja-JP\": \"测试连接\", \"ko-KR\": \"sys:codegenDatasource:test\", \"zh-CN\": \"测试连接\", \"zh-TW\": \"测试连接\"}', NULL, NULL, 'sys:codegenDatasource:test', 4, 1, 1, '2026-04-21 16:48:30', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000434, 1993479636925403138, 'PUBLIC', 1, 3000000000000000443, 'menu', 'codegen', '代码生成', '{\"en-US\": \"Code Generation\", \"ja-JP\": \"コード生成\", \"ko-KR\": \"코드 생성\", \"zh-CN\": \"代码生成\", \"zh-TW\": \"程式碼生成\"}', 'CodeOutlined', 'SystemCodegen', 'sys:codegen:view', 1, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000435, 1993479636925403138, 'PUBLIC', 1, 3000000000000000443, 'menu', 'codegenDatasource', '代码生成数据源', '{\"en-US\": \"Codegen Datasource\", \"ja-JP\": \"コード生成データソース\", \"ko-KR\": \"코드 생성 데이터소스\", \"zh-CN\": \"代码生成数据源\", \"zh-TW\": \"程式碼生成資料來源\"}', 'DatabaseOutlined', 'SystemCodegenDatasource', 'sys:codegenDatasource:view', 2, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:54:50', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000436, 1993479636925403138, 'PUBLIC', 1, 3000000000000000434, 'button', NULL, '预览', '{\"en-US\": \"sys:codegen:preview\", \"ja-JP\": \"预览\", \"ko-KR\": \"sys:codegen:preview\", \"zh-CN\": \"预览\", \"zh-TW\": \"预览\"}', NULL, NULL, 'sys:codegen:preview', 2, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000437, 1993479636925403138, 'PUBLIC', 1, 3000000000000000434, 'button', NULL, '下载ZIP', '{\"en-US\": \"sys:codegen:download\", \"ja-JP\": \"下载ZIP\", \"ko-KR\": \"sys:codegen:download\", \"zh-CN\": \"下载ZIP\", \"zh-TW\": \"下载ZIP\"}', NULL, NULL, 'sys:codegen:download', 3, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000438, 1993479636925403138, 'PUBLIC', 1, 3000000000000000435, 'button', NULL, '保存', '{\"en-US\": \"sys:codegenDatasource:save\", \"ja-JP\": \"保存\", \"ko-KR\": \"sys:codegenDatasource:save\", \"zh-CN\": \"保存\", \"zh-TW\": \"保存\"}', NULL, NULL, 'sys:codegenDatasource:save', 2, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000439, 1993479636925403138, 'PUBLIC', 1, 3000000000000000435, 'button', NULL, '删除', '{\"en-US\": \"sys:codegenDatasource:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegenDatasource:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}', NULL, NULL, 'sys:codegenDatasource:delete', 3, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000440, 1993479636925403138, 'PUBLIC', 1, 3000000000000000435, 'button', NULL, '测试连接', '{\"en-US\": \"sys:codegenDatasource:test\", \"ja-JP\": \"测试连接\", \"ko-KR\": \"sys:codegenDatasource:test\", \"zh-CN\": \"测试连接\", \"zh-TW\": \"测试连接\"}', NULL, NULL, 'sys:codegenDatasource:test', 4, 1, 1, '2026-04-21 17:07:40', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000441, 1993479636925403138, 'PUBLIC', 1, 3000000000000000434, 'button', NULL, '保存配置', '{\"en-US\": \"sys:codegen:save\", \"ja-JP\": \"保存配置\", \"ko-KR\": \"sys:codegen:save\", \"zh-CN\": \"保存配置\", \"zh-TW\": \"保存配置\"}', NULL, NULL, 'sys:codegen:save', 2, 1, 1, '2026-04-21 18:42:26', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000442, 1993479636925403138, 'PUBLIC', 1, 3000000000000000434, 'button', NULL, '删除', '{\"en-US\": \"sys:codegen:delete\", \"ja-JP\": \"删除\", \"ko-KR\": \"sys:codegen:delete\", \"zh-CN\": \"删除\", \"zh-TW\": \"删除\"}', NULL, NULL, 'sys:codegen:delete', 5, 1, 1, '2026-04-21 18:42:26', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 3, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000443, 1993479636925403138, 'PUBLIC', 1, 0, 'catalog', 'onlineDev', '在线开发', '{\"en-US\": \"onlineDev\", \"ja-JP\": \"在线开发\", \"ko-KR\": \"onlineDev\", \"zh-CN\": \"在线开发\", \"zh-TW\": \"在线开发\"}', 'CodeOutlined', NULL, NULL, 95, 1, 1, '2026-04-22 10:13:32', NULL, '2026-04-26 20:56:49', '20260426_i18n_fix', 0, 1, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000444, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '同步第三方', '{\"en-US\": \"Sync Third Party\", \"ja-JP\": \"第三方同期\", \"ko-KR\": \"제3자 동기화\", \"zh-CN\": \"同步第三方\", \"zh-TW\": \"同步第三方\"}', NULL, NULL, 'sys:user:syncThirdParty', 8, 1, 1, '2026-04-22 19:10:35', NULL, '2026-04-22 19:12:23', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000445, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '从第三方拉取', '{\"en-US\": \"Pull From Third Party\", \"ja-JP\": \"第三方から取得\", \"ko-KR\": \"제3자에서 가져오기\", \"zh-CN\": \"从第三方拉取\", \"zh-TW\": \"從第三方拉取\"}', NULL, NULL, 'sys:user:pullThirdParty', 9, 1, 1, '2026-04-22 19:10:35', NULL, '2026-04-22 19:12:23', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000446, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '导入用户', '{\"en-US\": \"Import Users\", \"ja-JP\": \"ユーザーインポート\", \"ko-KR\": \"사용자 가져오기\", \"zh-CN\": \"导入用户\", \"zh-TW\": \"導入用戶\"}', NULL, NULL, 'sys:user:import', 10, 1, 1, '2026-04-22 19:10:35', NULL, '2026-04-22 19:12:23', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000447, 1993479636925403138, 'PUBLIC', 1, 1, 'button', NULL, '下载模板', '{\"en-US\": \"Download Template\", \"ja-JP\": \"テンプレートダウンロード\", \"ko-KR\": \"템플릿 다운로드\", \"zh-CN\": \"下载模板\", \"zh-TW\": \"下載模板\"}', NULL, NULL, 'sys:user:downloadTemplate', 11, 1, 1, '2026-04-22 19:10:35', NULL, '2026-04-22 19:12:23', NULL, 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000448, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'supplier', '供应商管理', '{\"en-US\": \"Supplier Management\", \"ja-JP\": \"サプライヤー管理\", \"ko-KR\": \"공급업체 관리\", \"zh-CN\": \"供应商管理\", \"zh-TW\": \"供應商管理\"}', 'ApartmentOutlined', 'BasicSupplier', 'basic:supplier:query', 20, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-29 22:20:00', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000449, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '查询', '{\"en-US\": \"Query\", \"ja-JP\": \"検索\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}', NULL, NULL, 'basic:supplier:query', 1, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000450, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '新增', '{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'basic:supplier:add', 2, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000451, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '编辑', '{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}', NULL, NULL, 'basic:supplier:edit', 3, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000452, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'basic:supplier:delete', 4, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000453, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '导入', '{\"en-US\": \"Import\", \"ja-JP\": \"インポート\", \"ko-KR\": \"가져오기\", \"zh-CN\": \"导入\", \"zh-TW\": \"匯入\"}', NULL, NULL, 'basic:supplier:import', 5, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000454, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '导出', '{\"en-US\": \"Export\", \"ja-JP\": \"エクスポート\", \"ko-KR\": \"내보내기\", \"zh-CN\": \"导出\", \"zh-TW\": \"匯出\"}', NULL, NULL, 'basic:supplier:export', 6, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000455, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '生成租户', '{\"en-US\": \"Generate Tenant\", \"ja-JP\": \"テナント生成\", \"ko-KR\": \"테넌트 생성\", \"zh-CN\": \"生成租户\", \"zh-TW\": \"產生租戶\"}', NULL, NULL, 'basic:supplier:generateTenant', 7, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000456, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '发起审查', '{\"en-US\": \"Start Review\", \"ja-JP\": \"審査開始\", \"ko-KR\": \"심사 시작\", \"zh-CN\": \"发起审查\", \"zh-TW\": \"發起審查\"}', NULL, NULL, 'basic:supplier:review', 8, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000457, 1993479636925403138, 'PUBLIC', 5, 3000000000000000448, 'button', NULL, '同步第三方', '{\"en-US\": \"Sync Third Party\", \"ja-JP\": \"サードパーティ同期\", \"ko-KR\": \"타사 동기화\", \"zh-CN\": \"同步第三方\", \"zh-TW\": \"同步第三方\"}', NULL, NULL, 'basic:supplier:sync', 9, 1, 1, '2026-04-26 16:26:06', 'system', '2026-04-26 21:25:41', '20260426_i18n_fix', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000458, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'customer', '客户管理', '{\"en-US\": \"Customer Management\", \"ja-JP\": \"顧客管理\", \"ko-KR\": \"고객 관리\", \"zh-CN\": \"客户管理\", \"zh-TW\": \"客戶管理\"}', 'TeamOutlined', 'BasicCustomer', 'basic:customer:query', 30, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 22:20:00', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000459, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '查询', NULL, NULL, NULL, 'basic:customer:query', 1, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000460, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '新增', NULL, NULL, NULL, 'basic:customer:add', 2, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000461, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '编辑', NULL, NULL, NULL, 'basic:customer:edit', 3, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000462, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '删除', NULL, NULL, NULL, 'basic:customer:delete', 4, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000463, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '生成租户', NULL, NULL, NULL, 'basic:customer:generateTenant', 5, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000464, 1993479636925403138, 'PUBLIC', 5, 3000000000000000458, 'button', NULL, '发起审批', NULL, NULL, NULL, 'basic:customer:approval', 6, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-29 21:31:09', 'system', 0, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000466, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'material', '物料管理', '{\"en-US\": \"Material Management\", \"ja-JP\": \"品目管理\", \"ko-KR\": \"자재 관리\", \"zh-CN\": \"物料管理\", \"zh-TW\": \"物料管理\"}', 'AppstoreOutlined', 'BasicMaterial', 'basic:material:query', 40, 1, 1, '2026-04-29 21:31:09', 'system', '2026-04-30 00:17:44', 'system', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000467, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'menu', 'raw', '原材料', '{\"en-US\": \"Raw Material\", \"ja-JP\": \"原材料\", \"ko-KR\": \"원자재\", \"zh-CN\": \"原材料\", \"zh-TW\": \"原材料\"}', 'InboxOutlined', 'BasicMaterialRaw', 'basic:material:query', 10, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000468, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'menu', 'semi-finished', '半成品', '{\"en-US\": \"Semi-finished Material\", \"ja-JP\": \"半製品\", \"ko-KR\": \"반제품\", \"zh-CN\": \"半成品\", \"zh-TW\": \"半成品\"}', 'BuildOutlined', 'BasicMaterialSemiFinished', 'basic:material:query', 20, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000469, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'menu', 'finished', '成品', '{\"en-US\": \"Finished Goods\", \"ja-JP\": \"完成品\", \"ko-KR\": \"완제품\", \"zh-CN\": \"成品\", \"zh-TW\": \"成品\"}', 'GiftOutlined', 'BasicMaterialFinished', 'basic:material:query', 30, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000470, 1993479636925403138, 'PUBLIC', 5, 3000000000000000469, 'button', NULL, '查询', NULL, NULL, NULL, 'basic:material:query', 1, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000471, 1993479636925403138, 'PUBLIC', 5, 3000000000000000468, 'button', NULL, '查询', NULL, NULL, NULL, 'basic:material:query', 1, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000472, 1993479636925403138, 'PUBLIC', 5, 3000000000000000467, 'button', NULL, '查询', NULL, NULL, NULL, 'basic:material:query', 1, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000473, 1993479636925403138, 'PUBLIC', 5, 3000000000000000469, 'button', NULL, '新增', NULL, NULL, NULL, 'basic:material:add', 2, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000474, 1993479636925403138, 'PUBLIC', 5, 3000000000000000468, 'button', NULL, '新增', NULL, NULL, NULL, 'basic:material:add', 2, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000475, 1993479636925403138, 'PUBLIC', 5, 3000000000000000467, 'button', NULL, '新增', NULL, NULL, NULL, 'basic:material:add', 2, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000476, 1993479636925403138, 'PUBLIC', 5, 3000000000000000469, 'button', NULL, '编辑', NULL, NULL, NULL, 'basic:material:edit', 3, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000477, 1993479636925403138, 'PUBLIC', 5, 3000000000000000468, 'button', NULL, '编辑', NULL, NULL, NULL, 'basic:material:edit', 3, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000478, 1993479636925403138, 'PUBLIC', 5, 3000000000000000467, 'button', NULL, '编辑', NULL, NULL, NULL, 'basic:material:edit', 3, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000479, 1993479636925403138, 'PUBLIC', 5, 3000000000000000469, 'button', NULL, '删除', NULL, NULL, NULL, 'basic:material:delete', 4, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000480, 1993479636925403138, 'PUBLIC', 5, 3000000000000000468, 'button', NULL, '删除', NULL, NULL, NULL, 'basic:material:delete', 4, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000481, 1993479636925403138, 'PUBLIC', 5, 3000000000000000467, 'button', NULL, '删除', NULL, NULL, NULL, 'basic:material:delete', 4, 0, 0, '2026-04-29 21:31:09', 'system', '2026-04-29 23:46:40', 'system', 1, 3, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000485, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'button', NULL, '新增物料', '{\"en-US\": \"Add Material\", \"ja-JP\": \"Add Material\", \"ko-KR\": \"Add Material\", \"zh-CN\": \"新增物料\", \"zh-TW\": \"新增物料\"}', NULL, NULL, 'basic:material:add', 1, 1, 1, '2026-04-29 23:46:40', 'system', '2026-04-29 23:46:40', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000486, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'button', NULL, '编辑物料', '{\"en-US\": \"Edit Material\", \"ja-JP\": \"Edit Material\", \"ko-KR\": \"Edit Material\", \"zh-CN\": \"编辑物料\", \"zh-TW\": \"编辑物料\"}', NULL, NULL, 'basic:material:edit', 2, 1, 1, '2026-04-29 23:46:40', 'system', '2026-04-29 23:46:40', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000487, 1993479636925403138, 'PUBLIC', 5, 3000000000000000466, 'button', NULL, '删除物料', '{\"en-US\": \"Delete Material\", \"ja-JP\": \"Delete Material\", \"ko-KR\": \"Delete Material\", \"zh-CN\": \"删除物料\", \"zh-TW\": \"删除物料\"}', NULL, NULL, 'basic:material:delete', 3, 1, 1, '2026-04-29 23:46:40', 'system', '2026-04-29 23:46:40', 'system', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000488, 1993479636925403138, 'PUBLIC', 5, 0, 'menu', 'unit', '计量单位', '{\"en-US\": \"Units of Measure\", \"ja-JP\": \"計量単位\", \"ko-KR\": \"계량 단위\", \"zh-CN\": \"计量单位\", \"zh-TW\": \"計量單位\"}', 'ColumnWidthOutlined', 'BasicUnit', 'basic:unit:query', 50, 1, 1, '2026-05-02 20:54:41', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0, 1, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000489, 1993479636925403138, 'PUBLIC', 5, 3000000000000000488, 'button', NULL, '查询', '{\"en-US\": \"Query\", \"ja-JP\": \"検索\", \"ko-KR\": \"조회\", \"zh-CN\": \"查询\", \"zh-TW\": \"查詢\"}', NULL, NULL, 'basic:unit:query', 1, 1, 1, '2026-05-02 20:54:41', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000490, 1993479636925403138, 'PUBLIC', 5, 3000000000000000488, 'button', NULL, '新增', '{\"en-US\": \"Add\", \"ja-JP\": \"追加\", \"ko-KR\": \"추가\", \"zh-CN\": \"新增\", \"zh-TW\": \"新增\"}', NULL, NULL, 'basic:unit:add', 2, 1, 1, '2026-05-02 20:54:41', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000491, 1993479636925403138, 'PUBLIC', 5, 3000000000000000488, 'button', NULL, '编辑', '{\"en-US\": \"Edit\", \"ja-JP\": \"編集\", \"ko-KR\": \"편집\", \"zh-CN\": \"编辑\", \"zh-TW\": \"編輯\"}', NULL, NULL, 'basic:unit:edit', 3, 1, 1, '2026-05-02 20:54:41', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0, 2, 'embedded', NULL);
INSERT INTO `sys_menu` VALUES (3000000000000000492, 1993479636925403138, 'PUBLIC', 5, 3000000000000000488, 'button', NULL, '删除', '{\"en-US\": \"Delete\", \"ja-JP\": \"削除\", \"ko-KR\": \"삭제\", \"zh-CN\": \"删除\", \"zh-TW\": \"刪除\"}', NULL, NULL, 'basic:unit:delete', 4, 1, 1, '2026-05-02 20:54:41', '20260502_basic_unit_and_table_upgrade', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0, 2, 'embedded', NULL);

-- ----------------------------
-- Table structure for sys_message
-- ----------------------------
DROP TABLE IF EXISTS `sys_message`;
CREATE TABLE `sys_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `tenant_id` bigint NOT NULL COMMENT '接收方租户ID',
  `sender_tenant_id` bigint NOT NULL COMMENT '发送方租户ID',
  `sender_user_id` bigint NOT NULL COMMENT '发送方用户ID',
  `sender_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '发送人名称(格式:姓名+账号,如张三(admin),系统消息则为系统(admin))',
  `receiver_user_id` bigint NOT NULL COMMENT '接收方用户ID',
  `scope` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息范围(INTERNAL/EXTERNAL)',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板编号',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型(NOTICE=通知,WARNING=警告,ALARM=报警)',
  `category` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息分类：SYSTEM=系统通知，MESSAGE=消息通知',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'INTERNAL' COMMENT '消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '内容',
  `link_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `biz_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `status` tinyint NOT NULL DEFAULT 0 COMMENT '状态(0=未读,1=已读)',
  `read_time` datetime NULL DEFAULT NULL COMMENT '已读时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_user_status`(`tenant_id` ASC, `receiver_user_id` ASC, `status` ASC) USING BTREE,
  INDEX `idx_sender`(`sender_tenant_id` ASC, `sender_user_id` ASC) USING BTREE,
  INDEX `idx_template`(`template_code` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 23 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message
-- ----------------------------
INSERT INTO `sys_message` VALUES (1, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '测试', '测试', '', NULL, 0, NULL, '2026-04-05 16:58:14', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (2, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-05 17:22:02', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (3, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'SYSTEM', 'INTERNAL', '【审批待办】请假审批', '发起人：admin\n当前节点：管理员审批\n发起时间：2026-04-07 11:34:45', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-07 11:34:51', '2026-04-07 11:34:45', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (4, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'WARNING', 'SYSTEM', 'INTERNAL', '【审批驳回】请假审批', '审批名称：请假审批\n审批节点：管理员审批\n处理人：admin\n处理结果：驳回结束\n驳回原因：1', '/workspace/approval/my/initiated', 'WF_REJECTED', 1, '2026-04-07 11:35:23', '2026-04-07 11:35:13', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (5, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-07 11:45:27', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (6, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'SYSTEM', 'INTERNAL', '【审批待办】请假审批', '发起人：admin\n当前节点：管理员审批\n发起时间：2026-04-09 10:13:33', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-09 10:14:00', '2026-04-09 10:13:34', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (7, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'SYSTEM', 'INTERNAL', '【审批完成】请假审批', '审批名称：请假审批\n发起人：admin\n完成时间：2026-04-09 10:14:08\n处理结果：审批完成', '/workspace/approval/my/initiated', 'WF_FINISHED', 1, '2026-04-09 10:33:48', '2026-04-09 10:14:09', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (8, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-10 19:21:16', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (9, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-12 11:19:03', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (10, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-13 15:11:01', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (11, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-13 15:15:25', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (12, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-13 15:15:51', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (13, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-13 15:16:43', '2026-04-14 13:59:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (14, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'x', 'x', '', NULL, 0, NULL, '2026-04-14 14:00:12', '2026-04-14 14:00:12', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (15, 1993479636925403138, 1993479636925403138, 1993479637244170242, 'admin', 1993479637244170200, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', 'cs', 'cs', '', NULL, 0, NULL, '2026-04-14 14:23:17', '2026-04-14 14:23:17', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (16, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批完成】xa', '审批名称：xa\n发起人：admin\n完成时间：2026-04-17 16:58:31\n处理结果：审批完成', '/workspace/approval/my/initiated', 'WF_FINISHED', 1, '2026-04-17 16:58:42', '2026-04-17 16:58:32', '2026-04-17 16:58:42', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (17, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批待办】xa', '发起人：admin\n当前节点：审批节点\n发起时间：2026-04-22 19:50:41', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-22 19:50:47', '2026-04-22 19:50:41', '2026-04-22 19:50:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (18, 1993479636925403138, 1993479636925403138, 1993479637244170242, '用户(1993479637244170242)', 1993479637244170253, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批待办】xa', '发起人：admin\n当前节点：审批节点\n发起时间：2026-04-22 19:50:41', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-22 21:55:46', '2026-04-22 19:50:41', '2026-04-22 21:55:46', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message` VALUES (19, 1993479636925403138, 1993479636925403138, 1993479637244170253, '用户(1993479637244170253)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批完成】xa', '审批名称：xa\n发起人：admin\n完成时间：2026-04-22 22:23:36\n处理结果：审批完成', '/workspace/approval/my/initiated', 'WF_FINISHED', 1, '2026-04-22 23:45:37', '2026-04-22 22:23:37', '2026-04-22 23:45:36', 0, '1993479637244170253', '1993479637244170253');
INSERT INTO `sys_message` VALUES (20, 1993479636925403138, 1993479636925403138, 1993479637244170253, '用户(1993479637244170253)', 1993479637244170242, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批待办】xa', '发起人：孙明岩\n当前节点：审批节点\n发起时间：2026-04-22 23:29:25', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-22 23:45:40', '2026-04-22 23:29:26', '2026-04-22 23:45:40', 0, '1993479637244170253', '1993479637244170253');
INSERT INTO `sys_message` VALUES (21, 1993479636925403138, 1993479636925403138, 1993479637244170253, '用户(1993479637244170253)', 1993479637244170253, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批待办】xa', '发起人：孙明岩\n当前节点：审批节点\n发起时间：2026-04-22 23:29:25', '/workspace/approval/my/pending', 'WF_PENDING', 1, '2026-04-22 23:29:29', '2026-04-22 23:29:26', '2026-04-22 23:29:29', 0, '1993479637244170253', '1993479637244170253');
INSERT INTO `sys_message` VALUES (22, 1993479636925403138, 1993479636925403138, 1993479637244170253, '用户(1993479637244170253)', 1993479637244170253, 'INTERNAL', NULL, 'NOTICE', 'MESSAGE', 'INTERNAL', '【审批完成】xa', '审批名称：xa\n发起人：孙明岩\n完成时间：2026-04-22 23:29:56\n处理结果：审批完成', '/workspace/approval/my/initiated', 'WF_FINISHED', 1, '2026-04-22 23:29:59', '2026-04-22 23:29:57', '2026-04-22 23:29:58', 0, '1993479637244170253', '1993479637244170253');

-- ----------------------------
-- Table structure for sys_message_template
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template`;
CREATE TABLE `sys_message_template`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板编号',
  `template_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模板名称',
  `template_name_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '模板名称多语言JSON',
  `template_version` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '1.0' COMMENT '模板版本',
  `message_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NOTICE' COMMENT '消息类型(NOTICE=通知,WARNING=警告,ALARM=报警)',
  `biz_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务类型',
  `notification_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'info' COMMENT '通知图标类型(error=错误,info=普通,warning=警告,success=成功,custom=自定义)',
  `config_level` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'TENANT' COMMENT '配置级别(PUBLIC=公共级,TENANT=租户级,TENANT_TYPE=租户类型级)',
  `tenant_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '适用租户类型(MAIN_TENANT=主租户,SUB_TENANT=子租户,PUBLIC=所有类型)',
  `category` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模板分类(APPROVAL=审批模板,SYSTEM=系统模板,WELCOME=欢迎模板,SUMMARY=汇总模板)',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态(0=禁用,1=启用)',
  `remark` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_tenant_code`(`tenant_id` ASC, `template_code` ASC) USING BTREE,
  INDEX `idx_tenant_type`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板主表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_template
-- ----------------------------
INSERT INTO `sys_message_template` VALUES (1, 1993479636925403138, 'xxx', '', NULL, '1.0', 'NOTICE', NULL, 'info', 'TENANT', NULL, NULL, 1, '', '2026-03-31 21:03:10', '2026-04-09 15:57:40', 1, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template` VALUES (2, 0, 'WF_PENDING', '审批待办通知', '{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\",\"zh-TW\":\"審批待辦通知\",\"ja-JP\":\"承認保留通知\",\"ko-KR\":\"승인 대기 알림\"}', '1.0.0', 'WARNING', NULL, 'warning', 'TENANT', 'PUBLIC', 'APPROVAL', 1, '当审批流程流转到某个节点时，通知待审批人有新的待办任务', '2026-04-06 19:49:46', '2026-04-09 10:34:32', 0, NULL, NULL);
INSERT INTO `sys_message_template` VALUES (3, 0, 'WF_APPROVED', '审批通过通知', '{\"zh-CN\":\"审批通过通知\",\"en-US\":\"Approval Passed Notification\",\"zh-TW\":\"審批通過通知\",\"ja-JP\":\"承認済み通知\",\"ko-KR\":\"승인 완료 알림\"}', '1.0.0', 'NOTICE', NULL, 'success', 'TENANT', 'PUBLIC', 'APPROVAL', 1, '当审批节点被通过时，通知发起人审批进度', '2026-04-06 19:51:36', '2026-04-09 10:34:32', 0, NULL, NULL);
INSERT INTO `sys_message_template` VALUES (4, 0, 'WF_REJECTED', '审批驳回通知', '{\"zh-CN\":\"审批驳回通知\",\"en-US\":\"Approval Rejected Notification\",\"zh-TW\":\"審批駁回通知\",\"ja-JP\":\"却下通知\",\"ko-KR\":\"승인 거부 알림\"}', '1.0.0', 'ALARM', NULL, 'error', 'TENANT', 'PUBLIC', 'APPROVAL', 1, '当审批被驳回时，通知发起人驳回原因', '2026-04-06 19:51:37', '2026-04-09 10:34:32', 0, NULL, NULL);
INSERT INTO `sys_message_template` VALUES (5, 0, 'WF_FINISHED', '审批完成通知', '{\"zh-CN\":\"审批完成通知\",\"en-US\":\"Approval Completed Notification\",\"zh-TW\":\"審批完成通知\",\"ja-JP\":\"承認完了通知\",\"ko-KR\":\"승인 완료 알림\"}', '1.0.0', 'NOTICE', NULL, 'info', 'TENANT', 'PUBLIC', 'APPROVAL', 1, '当审批流程全部完成时，通知发起人审批结果', '2026-04-06 19:51:38', '2026-04-09 10:34:33', 0, NULL, NULL);
INSERT INTO `sys_message_template` VALUES (12, 0, 'UNREAD_SUMMARY', '未读消息汇总通知', '{\"zh-CN\":\"未读消息汇总通知\",\"en-US\":\"Unread Message Summary\"}', '1.0.0', 'NOTICE', NULL, 'info', 'PUBLIC', 'PUBLIC', 'SUMMARY', 1, '用户登录时推送的未读消息汇总通知模板', '2026-04-09 10:34:33', '2026-04-09 10:34:33', 0, 'system', 'system');
INSERT INTO `sys_message_template` VALUES (13, 1993479636925403138, 'WF_PENDING', '审批待办通知', '{\"zh-CN\":\"审批待办通知\",\"en-US\":\"Approval Pending Notification\",\"zh-TW\":\"審批待辦通知\",\"ja-JP\":\"承認保留通知\",\"ko-KR\":\"승인 대기 알림\"}', '1.0.0', 'WARNING', NULL, 'info', 'TENANT', NULL, NULL, 1, '当审批流程流转到某个节点时，通知待审批人有新的待办任务', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template` VALUES (14, 1993479636925403138, 'WF_APPROVED', '审批通过通知', '{\"zh-CN\":\"审批通过通知\",\"en-US\":\"Approval Passed Notification\",\"zh-TW\":\"審批通過通知\",\"ja-JP\":\"承認済み通知\",\"ko-KR\":\"승인 완료 알림\"}', '1.0.0', 'NOTICE', NULL, 'info', 'TENANT', NULL, NULL, 1, '当审批节点被通过时，通知发起人审批进度', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template` VALUES (15, 1993479636925403138, 'WF_REJECTED', '审批驳回通知', '{\"zh-CN\":\"审批驳回通知\",\"en-US\":\"Approval Rejected Notification\",\"zh-TW\":\"審批駁回通知\",\"ja-JP\":\"却下通知\",\"ko-KR\":\"승인 거부 알림\"}', '1.0.0', 'ALARM', NULL, 'info', 'TENANT', NULL, NULL, 1, '当审批被驳回时，通知发起人驳回原因', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template` VALUES (16, 1993479636925403138, 'WF_FINISHED', '审批完成通知', '{\"zh-CN\":\"审批完成通知\",\"en-US\":\"Approval Completed Notification\",\"zh-TW\":\"審批完成通知\",\"ja-JP\":\"承認完了通知\",\"ko-KR\":\"승인 완료 알림\"}', '1.0.0', 'NOTICE', NULL, 'info', 'TENANT', NULL, NULL, 1, '当审批流程全部完成时，通知发起人审批结果', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template` VALUES (17, 1993479636925403138, 'UNREAD_SUMMARY', '未读消息汇总通知', '{\"zh-CN\":\"未读消息汇总通知\",\"en-US\":\"Unread Message Summary\"}', '1.0.0', 'NOTICE', NULL, 'info', 'TENANT', NULL, NULL, 1, '用户登录时推送的未读消息汇总通知模板', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');

-- ----------------------------
-- Table structure for sys_message_template_content
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_content`;
CREATE TABLE `sys_message_template_content`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板内容ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_id` bigint NOT NULL COMMENT '模板主表ID',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息平台(INTERNAL=站内,WECHAT=企业微信,SMS=短信,EMAIL=邮箱)',
  `content_title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题(支持占位符,如${userName})',
  `content_title_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息标题多语言JSON',
  `content_body` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容(支持占位符,如${userName})',
  `content_body_i18n_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '消息内容多语言JSON',
  `link_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '跳转链接',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template`(`template_id` ASC) USING BTREE,
  INDEX `idx_tenant_platform`(`tenant_id` ASC, `platform` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板内容表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_template_content
-- ----------------------------
INSERT INTO `sys_message_template_content` VALUES (1, 1993479636925403138, 1, 'INTERNAL', '', NULL, '', NULL, '', '2026-03-31 21:03:10', '2026-04-09 15:57:39', 1, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_content` VALUES (2, 0, 2, 'INTERNAL', '【审批待办】${taskName}', '{\"zh-CN\":\"【审批待办】${taskName}\",\"en-US\":\"[Pending Approval] ${taskName}\",\"zh-TW\":\"【審批待辦】${taskName}\",\"ja-JP\":\"【承認保留中】${taskName}\",\"ko-KR\":\"【승인 대기】${taskName}\"}', '发起人：${initiatorName}\n当前节点：${nodeName}\n发起时间：${startTime}', '{\"zh-CN\":\"发起人：${initiatorName}\\n当前节点：${nodeName}\\n发起时间：${startTime}\",\"en-US\":\"Initiator: ${initiatorName}\\nCurrent Node: ${nodeName}\\nStart Time: ${startTime}\",\"zh-TW\":\"發起人：${initiatorName}\\n當前節點：${nodeName}\\n發起時間：${startTime}\",\"ja-JP\":\"申請者：${initiatorName}\\n現在のノード：${nodeName}\\n開始時間：${startTime}\",\"ko-KR\":\"신청자: ${initiatorName}\\n현재 단계: ${nodeName}\\n시작 시간: ${startTime}\"}', '/workspace/approval/my/pending', '2026-04-06 19:49:57', '2026-04-09 10:36:33', 0, NULL, NULL);
INSERT INTO `sys_message_template_content` VALUES (3, 0, 3, 'INTERNAL', '【审批通过】${taskName}', '{\"zh-CN\":\"【审批通过】${taskName}\",\"en-US\":\"[Approved] ${taskName}\",\"zh-TW\":\"【審批通過】${taskName}\",\"ja-JP\":\"【承認済み】${taskName}\",\"ko-KR\":\"【승인됨】${taskName}\"}', '审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n审批意见：${comment}', '{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n审批意见：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nComment: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n審批意見：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n承認意見：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n승인 의견: ${comment}\"}', '/workspace/approval/my/initiated', '2026-04-06 19:51:47', '2026-04-09 10:36:33', 0, NULL, NULL);
INSERT INTO `sys_message_template_content` VALUES (4, 0, 4, 'INTERNAL', '【审批驳回】${taskName}', '{\"zh-CN\":\"【审批驳回】${taskName}\",\"en-US\":\"[Rejected] ${taskName}\",\"zh-TW\":\"【審批駁回】${taskName}\",\"ja-JP\":\"【却下】${taskName}\",\"ko-KR\":\"【거부됨】${taskName}\"}', '审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n驳回原因：${comment}', '{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n驳回原因：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nReason: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n駁回原因：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n却下理由：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n거부 사유: ${comment}\"}', '/workspace/approval/my/initiated', '2026-04-06 19:51:49', '2026-04-09 10:36:33', 0, NULL, NULL);
INSERT INTO `sys_message_template_content` VALUES (5, 0, 5, 'INTERNAL', '【审批完成】${taskName}', '{\"zh-CN\":\"【审批完成】${taskName}\",\"en-US\":\"[Completed] ${taskName}\",\"zh-TW\":\"【審批完成】${taskName}\",\"ja-JP\":\"【承認完了】${taskName}\",\"ko-KR\":\"【완료】${taskName}\"}', '审批名称：${taskName}\n发起人：${initiatorName}\n完成时间：${endTime}\n处理结果：${result}', '{\"zh-CN\":\"审批名称：${taskName}\\n发起人：${initiatorName}\\n完成时间：${endTime}\\n处理结果：${result}\",\"en-US\":\"Task: ${taskName}\\nInitiator: ${initiatorName}\\nCompleted: ${endTime}\\nResult: ${result}\",\"zh-TW\":\"審批名稱：${taskName}\\n發起人：${initiatorName}\\n完成時間：${endTime}\\n處理結果：${result}\",\"ja-JP\":\"承認名：${taskName}\\n申請者：${initiatorName}\\n完了時間：${endTime}\\n処理結果：${result}\",\"ko-KR\":\"승인명: ${taskName}\\n신청자: ${initiatorName}\\n완료 시간: ${endTime}\\n처리 결과: ${result}\"}', '/workspace/approval/my/initiated', '2026-04-06 19:51:50', '2026-04-09 10:36:33', 0, NULL, NULL);
INSERT INTO `sys_message_template_content` VALUES (11, 0, 12, 'INTERNAL', '尊敬的${userName}，您有${unreadCount}条消息未读', '{\"zh-CN\":\"尊敬的${userName}，您有${unreadCount}条消息未读\",\"en-US\":\"Dear ${userName}, you have ${unreadCount} unread messages\"}', '您有${unreadCount}条未读消息，请及时查看。', '{\"zh-CN\":\"您有${unreadCount}条未读消息，请及时查看。\",\"en-US\":\"You have ${unreadCount} unread messages, please check them in time.\"}', '/workspace/message/unread', '2026-04-09 10:34:33', '2026-04-09 10:34:33', 0, 'system', 'system');
INSERT INTO `sys_message_template_content` VALUES (12, 1993479636925403138, 13, 'INTERNAL', '【审批待办】${taskName}', '{\"zh-CN\":\"【审批待办】${taskName}\",\"en-US\":\"[Pending Approval] ${taskName}\",\"zh-TW\":\"【審批待辦】${taskName}\",\"ja-JP\":\"【承認保留中】${taskName}\",\"ko-KR\":\"【승인 대기】${taskName}\"}', '发起人：${initiatorName}\n当前节点：${nodeName}\n发起时间：${startTime}', '{\"zh-CN\":\"发起人：${initiatorName}\\n当前节点：${nodeName}\\n发起时间：${startTime}\",\"en-US\":\"Initiator: ${initiatorName}\\nCurrent Node: ${nodeName}\\nStart Time: ${startTime}\",\"zh-TW\":\"發起人：${initiatorName}\\n當前節點：${nodeName}\\n發起時間：${startTime}\",\"ja-JP\":\"申請者：${initiatorName}\\n現在のノード：${nodeName}\\n開始時間：${startTime}\",\"ko-KR\":\"신청자: ${initiatorName}\\n현재 단계: ${nodeName}\\n시작 시간: ${startTime}\"}', '/workspace/approval/my/pending', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_content` VALUES (13, 1993479636925403138, 14, 'INTERNAL', '【审批通过】${taskName}', '{\"zh-CN\":\"【审批通过】${taskName}\",\"en-US\":\"[Approved] ${taskName}\",\"zh-TW\":\"【審批通過】${taskName}\",\"ja-JP\":\"【承認済み】${taskName}\",\"ko-KR\":\"【승인됨】${taskName}\"}', '审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n审批意见：${comment}', '{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n审批意见：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nComment: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n審批意見：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n承認意見：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n승인 의견: ${comment}\"}', '/workspace/approval/my/initiated', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_content` VALUES (14, 1993479636925403138, 15, 'INTERNAL', '【审批驳回】${taskName}', '{\"zh-CN\":\"【审批驳回】${taskName}\",\"en-US\":\"[Rejected] ${taskName}\",\"zh-TW\":\"【審批駁回】${taskName}\",\"ja-JP\":\"【却下】${taskName}\",\"ko-KR\":\"【거부됨】${taskName}\"}', '审批名称：${taskName}\n审批节点：${nodeName}\n处理人：${approverName}\n处理结果：${result}\n驳回原因：${comment}', '{\"zh-CN\":\"审批名称：${taskName}\\n审批节点：${nodeName}\\n处理人：${approverName}\\n处理结果：${result}\\n驳回原因：${comment}\",\"en-US\":\"Task: ${taskName}\\nNode: ${nodeName}\\nHandler: ${approverName}\\nResult: ${result}\\nReason: ${comment}\",\"zh-TW\":\"審批名稱：${taskName}\\n審批節點：${nodeName}\\n處理人：${approverName}\\n處理結果：${result}\\n駁回原因：${comment}\",\"ja-JP\":\"承認名：${taskName}\\n承認ノード：${nodeName}\\n処理者：${approverName}\\n処理結果：${result}\\n却下理由：${comment}\",\"ko-KR\":\"승인명: ${taskName}\\n승인 단계: ${nodeName}\\n처리자: ${approverName}\\n처리 결과: ${result}\\n거부 사유: ${comment}\"}', '/workspace/approval/my/initiated', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_content` VALUES (15, 1993479636925403138, 16, 'INTERNAL', '【审批完成】${taskName}', '{\"zh-CN\":\"【审批完成】${taskName}\",\"en-US\":\"[Completed] ${taskName}\",\"zh-TW\":\"【審批完成】${taskName}\",\"ja-JP\":\"【承認完了】${taskName}\",\"ko-KR\":\"【완료】${taskName}\"}', '审批名称：${taskName}\n发起人：${initiatorName}\n完成时间：${endTime}\n处理结果：${result}', '{\"zh-CN\":\"审批名称：${taskName}\\n发起人：${initiatorName}\\n完成时间：${endTime}\\n处理结果：${result}\",\"en-US\":\"Task: ${taskName}\\nInitiator: ${initiatorName}\\nCompleted: ${endTime}\\nResult: ${result}\",\"zh-TW\":\"審批名稱：${taskName}\\n發起人：${initiatorName}\\n完成時間：${endTime}\\n處理結果：${result}\",\"ja-JP\":\"承認名：${taskName}\\n申請者：${initiatorName}\\n完了時間：${endTime}\\n処理結果：${result}\",\"ko-KR\":\"승인명: ${taskName}\\n신청자: ${initiatorName}\\n완료 시간: ${endTime}\\n처리 결과: ${result}\"}', '/workspace/approval/my/initiated', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_content` VALUES (16, 1993479636925403138, 17, 'INTERNAL', '尊敬的${userName}，您有${unreadCount}条消息未读', '{\"zh-CN\":\"尊敬的${userName}，您有${unreadCount}条消息未读\",\"en-US\":\"Dear ${userName}, you have ${unreadCount} unread messages\"}', '您有${unreadCount}条未读消息，请及时查看。', '{\"zh-CN\":\"您有${unreadCount}条未读消息，请及时查看。\",\"en-US\":\"You have ${unreadCount} unread messages, please check them in time.\"}', '/workspace/message/unread', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');

-- ----------------------------
-- Table structure for sys_message_template_receiver
-- ----------------------------
DROP TABLE IF EXISTS `sys_message_template_receiver`;
CREATE TABLE `sys_message_template_receiver`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '接收人配置ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `template_id` bigint NOT NULL COMMENT '模板主表ID',
  `receiver_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收类型(ROLE=角色,DEPT=部门,POSITION=职位,USER=指定人)',
  `receiver_ids` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收人ID列表(JSON数组格式,如[1,2,3])',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_template`(`template_id` ASC) USING BTREE,
  INDEX `idx_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息模板接收人表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_message_template_receiver
-- ----------------------------
INSERT INTO `sys_message_template_receiver` VALUES (1, 1993479636925403138, 1, 'USER', '[\"1993479637244170249\"]', '2026-03-31 21:03:10', '2026-04-09 15:57:39', 1, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_receiver` VALUES (2, 0, 7, 'CUSTOM', '[]', '2026-04-08 23:25:39', '2026-04-08 23:25:39', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (3, 0, 2, 'CUSTOM', '[]', '2026-04-08 23:25:39', '2026-04-09 11:47:46', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (4, 0, 3, 'CUSTOM', '[]', '2026-04-08 23:25:39', '2026-04-09 11:47:46', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (5, 0, 4, 'CUSTOM', '[]', '2026-04-08 23:25:39', '2026-04-09 11:47:46', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (6, 0, 5, 'CUSTOM', '[]', '2026-04-08 23:25:39', '2026-04-09 11:47:46', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (7, 0, 8, 'CUSTOM', '[]', '2026-04-08 23:52:24', '2026-04-08 23:52:24', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (8, 0, 9, 'CUSTOM', '[]', '2026-04-08 23:53:25', '2026-04-08 23:53:25', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (9, 0, 10, 'CUSTOM', '[]', '2026-04-08 23:53:44', '2026-04-08 23:53:44', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (11, 0, 12, 'CUSTOM', '[]', '2026-04-09 10:34:33', '2026-04-09 10:34:33', 0, 'system', 'system');
INSERT INTO `sys_message_template_receiver` VALUES (12, 1993479636925403138, 13, 'CUSTOM', '[]', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_receiver` VALUES (13, 1993479636925403138, 14, 'CUSTOM', '[]', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_receiver` VALUES (14, 1993479636925403138, 15, 'CUSTOM', '[]', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_receiver` VALUES (15, 1993479636925403138, 16, 'CUSTOM', '[]', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');
INSERT INTO `sys_message_template_receiver` VALUES (16, 1993479636925403138, 17, 'CUSTOM', '[]', '2026-04-09 15:57:26', '2026-04-09 15:57:26', 0, '1993479637244170242', '1993479637244170242');

-- ----------------------------
-- Table structure for sys_module
-- ----------------------------
DROP TABLE IF EXISTS `sys_module`;
CREATE TABLE `sys_module`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块编码',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '模块名称',
  `name_i18n_json` json NULL COMMENT '模块名称国际化(JSON)',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `order_num` int NULL DEFAULT NULL COMMENT '排序号',
  `visible` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否可见：1可见 0隐藏',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_module_tenant`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统模块表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_module
-- ----------------------------
INSERT INTO `sys_module` VALUES (1, 1993479636925403138, 'sys', '系统管理', '{\"en-US\": \"System\", \"ja-JP\": \"システム管理\", \"ko-KR\": \"시스템 관리\", \"zh-CN\": \"系统管理\", \"zh-TW\": \"系統管理\"}', 'SettingOutlined', 10, 1, 1, '2026-01-08 10:58:58', NULL, '2026-04-07 20:18:40', NULL, 0);
INSERT INTO `sys_module` VALUES (3, 1993479636925403138, 'approval', '审批管理', '{\"en-US\": \"Approval\", \"ja-JP\": \"承認管理\", \"ko-KR\": \"승인 관리\", \"zh-CN\": \"审批管理\", \"zh-TW\": \"審批管理\"}', 'AuditOutlined', 50, 1, 1, '2026-04-02 14:50:02', 'system', '2026-04-07 20:18:40', 'system', 0);
INSERT INTO `sys_module` VALUES (4, 0, 'system', '系统管理', '{\"en-US\": \"System Management\", \"ja-JP\": \"システム管理\", \"ko-KR\": \"시스템 관리\", \"zh-CN\": \"系统管理\", \"zh-TW\": \"系統管理\"}', 'SettingOutlined', 10, 1, 1, '2026-04-06 23:16:09', NULL, '2026-04-14 16:01:37', NULL, 0);
INSERT INTO `sys_module` VALUES (5, 1993479636925403138, 'basic', '基础信息', '{\"en-US\": \"Basic Information\", \"ja-JP\": \"基本情報\", \"ko-KR\": \"기본 정보\", \"zh-CN\": \"基础信息\", \"zh-TW\": \"基礎資訊\"}', 'DatabaseOutlined', 40, 1, 1, '2026-04-09 18:10:41', 'system', '2026-05-02 21:36:58', '20260502_basic_unit_and_table_upgrade', 0);
INSERT INTO `sys_module` VALUES (6, 1993479636925403138, 'integration', '接口平台', '{\"en-US\": \"Integration Platform\", \"zh-CN\": \"接口平台\"}', 'ApiOutlined', 90, 1, 1, '2026-04-14 15:49:25', '20260414_init', '2026-04-21 16:22:51', '20260421_restore_integration_menu', 0);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限名称',
  `permission_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限标识',
  `url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'URL',
  `method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '方法 (GET/POST等)',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `permission_key`(`permission_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 104 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (1, '查看用户', 'sys:user:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (2, '查看角色', 'sys:role:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (3, '查看模块', 'sys:module:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (4, '查看菜单', 'sys:menu:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (5, '查看部门', 'sys:dept:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (6, '查看职位', 'sys:position:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (7, '添加用户', 'sys:user:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (8, '编辑用户', 'sys:user:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (9, '删除用户', 'sys:user:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (10, '批量删除用户', 'sys:user:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (11, '重置密码', 'sys:user:resetPwd', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (12, '导出用户', 'sys:user:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (13, '分配角色', 'sys:user:assignRole', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (14, '添加角色', 'sys:role:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (15, '编辑角色', 'sys:role:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (16, '删除角色', 'sys:role:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (17, '批量删除角色', 'sys:role:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (18, '授权菜单', 'sys:role:authMenu', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (19, '添加模块', 'sys:module:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (20, '编辑模块', 'sys:module:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (21, '删除模块', 'sys:module:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (22, '批量删除模块', 'sys:module:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (23, '添加菜单', 'sys:menu:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (24, '编辑菜单', 'sys:menu:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (25, '删除菜单', 'sys:menu:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (26, '批量删除菜单', 'sys:menu:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (27, '添加部门', 'sys:dept:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (28, '编辑部门', 'sys:dept:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (29, '删除部门', 'sys:dept:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (30, '添加职位', 'sys:position:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (31, '编辑职位', 'sys:position:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (32, '删除职位', 'sys:position:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (33, '批量删除职位', 'sys:position:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (34, '查看仪表盘', 'sys:dashboard:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (35, '查看导出配置', 'sys:excel:exportConfig:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (36, '查看导入配置', 'sys:excel:importConfig:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (37, '列出导出配置', 'sys:excel:exportConfig:list', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (38, '编辑导出配置', 'sys:excel:exportConfig:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (39, '删除导出配置', 'sys:excel:exportConfig:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (40, '列出导入配置', 'sys:excel:importConfig:list', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (41, '编辑导入配置', 'sys:excel:importConfig:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (42, '删除导入配置', 'sys:excel:importConfig:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (43, '下载模板', 'sys:excel:template:download', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (44, '导出登录日志', 'sys:excel:export:loginLog', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (45, '导出用户', 'sys:excel:export:user', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (46, '查看字典', 'sys:dict:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (47, '添加字典', 'sys:dict:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (48, '编辑字典', 'sys:dict:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (49, '删除字典', 'sys:dict:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (50, '批量删除字典', 'sys:dict:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (51, '导出字典', 'sys:dict:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (52, '查看字典类型', 'sys:dictType:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (53, '添加字典类型', 'sys:dictType:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (54, '编辑字典类型', 'sys:dictType:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (55, '删除字典类型', 'sys:dictType:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (56, '批量删除字典类型', 'sys:dictType:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (57, '导出字典类型', 'sys:dictType:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (58, '查看表格配置', 'sys:tableConfig:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (59, '添加表格配置', 'sys:tableConfig:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (60, '编辑表格配置', 'sys:tableConfig:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (61, '删除表格配置', 'sys:tableConfig:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (62, '批量删除表格配置', 'sys:tableConfig:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (63, '导出表格配置', 'sys:tableConfig:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (64, '查看登录日志', 'sys:loginLog:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (65, '删除登录日志', 'sys:loginLog:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (66, '批量删除登录日志', 'sys:loginLog:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (67, '导出登录日志', 'sys:loginLog:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (68, '查看在线用户', 'sys:online:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (69, '踢出用户', 'sys:online:kickout', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (70, '批量踢出用户', 'sys:online:batchKickout', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (71, '导出在线用户', 'sys:online:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (72, '查看租户', 'sys:tenant:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (73, '添加租户', 'sys:tenant:add', NULL, 'POST', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (74, '编辑租户', 'sys:tenant:edit', NULL, 'PUT', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (75, '删除租户', 'sys:tenant:delete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (76, '批量删除租户', 'sys:tenant:batchDelete', NULL, 'DELETE', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (77, '导出租户', 'sys:tenant:export', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (78, '查看组织架构', 'sys:organization:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (79, '查看授权管理', 'sys:authorization:view', NULL, 'GET', 0, '2026-01-18 21:53:57', '2026-01-18 21:53:57', 0);
INSERT INTO `sys_permission` VALUES (92, '编码规则 - 查询', 'encode:rule:query', '/api/sys/encodeRule/**', 'GET', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (93, '编码规则 - 新增', 'encode:rule:add', '/api/sys/encodeRule', 'POST', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (94, '编码规则 - 编辑', 'encode:rule:edit', '/api/sys/encodeRule/**', 'PUT', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (95, '编码规则 - 删除', 'encode:rule:delete', '/api/sys/encodeRule/**', 'DELETE', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (96, '编码规则 - 启用禁用', 'encode:rule:toggle', '/api/sys/encodeRule/toggleStatus', 'PUT', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (97, '编码规则 - 生成编码', 'encode:rule:generate', '/api/sys/encodeRule/generateCode', 'POST', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (98, '编码示例 - 查询', 'encode:example:query', '/api/sys/encodeRuleExample/**', 'GET', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (99, '编码示例 - 新增', 'encode:example:add', '/api/sys/encodeRuleExample', 'POST', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (100, '编码示例 - 编辑', 'encode:example:edit', '/api/sys/encodeRuleExample/**', 'PUT', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (101, '编码示例 - 删除', 'encode:example:delete', '/api/sys/encodeRuleExample/**', 'DELETE', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (102, '历史记录 - 查询', 'encode:history:query', '/api/sys/encodeHistory/**', 'GET', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);
INSERT INTO `sys_permission` VALUES (103, '历史记录 - 导出', 'encode:history:export', '/api/sys/encodeHistory/export', 'POST', 1993479636925403138, '2026-04-10 11:30:05', '2026-04-10 11:30:05', 0);

-- ----------------------------
-- Table structure for sys_position
-- ----------------------------
DROP TABLE IF EXISTS `sys_position`;
CREATE TABLE `sys_position`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `position_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位名称',
  `position_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '职位编码',
  `position_level` int NULL DEFAULT NULL COMMENT '职位级别',
  `order_num` int NULL DEFAULT 0 COMMENT '排序号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0=禁用，1=启用',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '删除标志：0=未删除，1=已删除',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_position_code`(`position_code` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_deleted`(`deleted` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '职位表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_position
-- ----------------------------
INSERT INTO `sys_position` VALUES (1, '总经理', 'POS_GM', 1, 1, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (2, '副总经理', 'POS_DGM', 2, 2, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (3, '部门经理', 'POS_MGR', 3, 3, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (4, '主管', 'POS_SUP', 4, 4, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (5, '组长', 'POS_LEAD', 5, 5, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (6, '高级工程师', 'POS_SR_ENG', 6, 6, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (7, '工程师', 'POS_ENG', 7, 7, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (8, '技术员', 'POS_TECH', 8, 8, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (9, '操作员', 'POS_OP', 9, 9, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (10, '文员', 'POS_CLERK', 10, 10, 1, NULL, '2026-01-07 14:41:44', '2026-01-07 14:41:44', NULL, NULL, 1, 0, NULL);
INSERT INTO `sys_position` VALUES (11, '?????', 'PLATFORM_ADMIN', 1, 1, 1, '??????', '2026-04-02 14:46:48', '2026-04-05 16:48:54', 'system', '1993479637244170242', 1993479636925403138, 1, 18);
INSERT INTO `sys_position` VALUES (12, '测试职位', 'TEST', 1, 0, 1, NULL, '2026-04-04 16:16:28', '2026-04-30 17:29:08', '1993479637244170242', '1993479637244170242', 1993479636925403138, 0, 19);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色标识',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态',
  `data_scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'SELF' COMMENT '数据权限范围：ALL-全部数据,DEPT_AND_CHILD-本部门及下级,DEPT-本部门,SELF-仅本人,CUSTOM-自定义',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_tenant`(`tenant_id` ASC, `role_key` ASC) USING BTREE,
  INDEX `idx_role_key`(`role_key` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1993479637311279116 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1993479637311279107, '系统管理员', 'admin', NULL, 1, 'SELF', 1993479636925403138, '2025-11-26 08:39:17', NULL, '2025-11-26 08:39:17', NULL, 0);
INSERT INTO `sys_role` VALUES (1993479637311279112, '普通用户', 'user', NULL, 1, 'SELF', 1993479636925403138, '2026-01-08 10:58:58', NULL, '2026-01-08 10:58:58', NULL, 0);
INSERT INTO `sys_role` VALUES (1993479637311279113, '部门经理', 'manager', NULL, 1, 'SELF', 1993479636925403138, '2026-01-08 10:58:58', NULL, '2026-01-08 10:58:58', NULL, 0);
INSERT INTO `sys_role` VALUES (1993479637311279114, '系统审计员', 'auditor', NULL, 1, 'SELF', 1993479636925403138, '2026-01-08 10:58:58', NULL, '2026-01-08 10:58:58', NULL, 0);

-- ----------------------------
-- Table structure for sys_role_c_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_c_menu`;
CREATE TABLE `sys_role_c_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `c_menu_id` bigint NOT NULL COMMENT 'C 端菜单 ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_c_menu`(`tenant_id` ASC, `role_id` ASC, `c_menu_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_c_menu_id`(`c_menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'C 端角色菜单关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_c_menu
-- ----------------------------
INSERT INTO `sys_role_c_menu` VALUES (4, NULL, 1993479637311279107, 1);
INSERT INTO `sys_role_c_menu` VALUES (3, NULL, 1993479637311279107, 2);
INSERT INTO `sys_role_c_menu` VALUES (2, NULL, 1993479637311279107, 3);
INSERT INTO `sys_role_c_menu` VALUES (1, NULL, 1993479637311279107, 4);
INSERT INTO `sys_role_c_menu` VALUES (16, NULL, 1993479637311279112, 1);
INSERT INTO `sys_role_c_menu` VALUES (15, NULL, 1993479637311279112, 2);
INSERT INTO `sys_role_c_menu` VALUES (14, NULL, 1993479637311279112, 3);
INSERT INTO `sys_role_c_menu` VALUES (13, NULL, 1993479637311279112, 4);
INSERT INTO `sys_role_c_menu` VALUES (12, NULL, 1993479637311279113, 1);
INSERT INTO `sys_role_c_menu` VALUES (11, NULL, 1993479637311279113, 2);
INSERT INTO `sys_role_c_menu` VALUES (10, NULL, 1993479637311279113, 3);
INSERT INTO `sys_role_c_menu` VALUES (9, NULL, 1993479637311279113, 4);
INSERT INTO `sys_role_c_menu` VALUES (8, NULL, 1993479637311279114, 1);
INSERT INTO `sys_role_c_menu` VALUES (7, NULL, 1993479637311279114, 2);
INSERT INTO `sys_role_c_menu` VALUES (6, NULL, 1993479637311279114, 3);
INSERT INTO `sys_role_c_menu` VALUES (5, NULL, 1993479637311279114, 4);
INSERT INTO `sys_role_c_menu` VALUES (17, 1993479636925403138, 1993479637311279107, 5);
INSERT INTO `sys_role_c_menu` VALUES (21, 1993479636925403138, 1993479637311279107, 6);
INSERT INTO `sys_role_c_menu` VALUES (20, 1993479636925403138, 1993479637311279112, 5);
INSERT INTO `sys_role_c_menu` VALUES (24, 1993479636925403138, 1993479637311279112, 6);
INSERT INTO `sys_role_c_menu` VALUES (19, 1993479636925403138, 1993479637311279113, 5);
INSERT INTO `sys_role_c_menu` VALUES (23, 1993479636925403138, 1993479637311279113, 6);
INSERT INTO `sys_role_c_menu` VALUES (18, 1993479636925403138, 1993479637311279114, 5);
INSERT INTO `sys_role_c_menu` VALUES (22, 1993479636925403138, 1993479637311279114, 6);

-- ----------------------------
-- Table structure for sys_role_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_dept`;
CREATE TABLE `sys_role_dept`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `dept_id` bigint NOT NULL COMMENT '部门ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色-部门关联表（数据权限）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_dept
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `tenant_id` bigint NOT NULL,
  `role_id` bigint NOT NULL,
  `menu_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_menu`(`tenant_id` ASC, `role_id` ASC, `menu_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2047520831801663541 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色菜单授权' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2012030852827447297, 1993479636925403138, 1993479637311279107, 1);
INSERT INTO `sys_role_menu` VALUES (2012030852827447298, 1993479636925403138, 1993479637311279107, 2);
INSERT INTO `sys_role_menu` VALUES (2012030852890361858, 1993479636925403138, 1993479637311279107, 3);
INSERT INTO `sys_role_menu` VALUES (2012030852890361859, 1993479636925403138, 1993479637311279107, 4);
INSERT INTO `sys_role_menu` VALUES (2012030852957470722, 1993479636925403138, 1993479637311279107, 5);
INSERT INTO `sys_role_menu` VALUES (2012030852957470723, 1993479636925403138, 1993479637311279107, 6);
INSERT INTO `sys_role_menu` VALUES (2012030851657236482, 1993479636925403138, 1993479637311279107, 101);
INSERT INTO `sys_role_menu` VALUES (2012030851657236483, 1993479636925403138, 1993479637311279107, 102);
INSERT INTO `sys_role_menu` VALUES (2012030851657236484, 1993479636925403138, 1993479637311279107, 103);
INSERT INTO `sys_role_menu` VALUES (2012030851724345346, 1993479636925403138, 1993479637311279107, 104);
INSERT INTO `sys_role_menu` VALUES (2012030851724345347, 1993479636925403138, 1993479637311279107, 105);
INSERT INTO `sys_role_menu` VALUES (2012030851787259905, 1993479636925403138, 1993479637311279107, 106);
INSERT INTO `sys_role_menu` VALUES (2012030851854368769, 1993479636925403138, 1993479637311279107, 201);
INSERT INTO `sys_role_menu` VALUES (2012030851854368770, 1993479636925403138, 1993479637311279107, 202);
INSERT INTO `sys_role_menu` VALUES (2012030851854368771, 1993479636925403138, 1993479637311279107, 203);
INSERT INTO `sys_role_menu` VALUES (2012030851917283329, 1993479636925403138, 1993479637311279107, 204);
INSERT INTO `sys_role_menu` VALUES (2012030851917283330, 1993479636925403138, 1993479637311279107, 205);
INSERT INTO `sys_role_menu` VALUES (2012030851980197889, 1993479636925403138, 1993479637311279107, 301);
INSERT INTO `sys_role_menu` VALUES (2012030851980197890, 1993479636925403138, 1993479637311279107, 302);
INSERT INTO `sys_role_menu` VALUES (2012030852043112450, 1993479636925403138, 1993479637311279107, 303);
INSERT INTO `sys_role_menu` VALUES (2012030852043112451, 1993479636925403138, 1993479637311279107, 304);
INSERT INTO `sys_role_menu` VALUES (2012030852043112452, 1993479636925403138, 1993479637311279107, 401);
INSERT INTO `sys_role_menu` VALUES (2012030852106027009, 1993479636925403138, 1993479637311279107, 402);
INSERT INTO `sys_role_menu` VALUES (2012030852106027010, 1993479636925403138, 1993479637311279107, 403);
INSERT INTO `sys_role_menu` VALUES (2012030852173135873, 1993479636925403138, 1993479637311279107, 404);
INSERT INTO `sys_role_menu` VALUES (2012030852173135874, 1993479636925403138, 1993479637311279107, 501);
INSERT INTO `sys_role_menu` VALUES (2012030852173135875, 1993479636925403138, 1993479637311279107, 502);
INSERT INTO `sys_role_menu` VALUES (2012030852236050433, 1993479636925403138, 1993479637311279107, 503);
INSERT INTO `sys_role_menu` VALUES (2012030852236050434, 1993479636925403138, 1993479637311279107, 601);
INSERT INTO `sys_role_menu` VALUES (2012030852298964993, 1993479636925403138, 1993479637311279107, 602);
INSERT INTO `sys_role_menu` VALUES (2012030852298964994, 1993479636925403138, 1993479637311279107, 603);
INSERT INTO `sys_role_menu` VALUES (2012030852298964995, 1993479636925403138, 1993479637311279107, 604);
INSERT INTO `sys_role_menu` VALUES (2012030851619487746, 1993479636925403138, 1993479637311279107, 606);
INSERT INTO `sys_role_menu` VALUES (2012030853020385282, 1993479636925403138, 1993479637311279107, 607);
INSERT INTO `sys_role_menu` VALUES (2012030853083299842, 1993479636925403138, 1993479637311279107, 608);
INSERT INTO `sys_role_menu` VALUES (2012030852370268161, 1993479636925403138, 1993479637311279107, 609);
INSERT INTO `sys_role_menu` VALUES (2012030852370268162, 1993479636925403138, 1993479637311279107, 610);
INSERT INTO `sys_role_menu` VALUES (2012030852496097281, 1993479636925403138, 1993479637311279107, 611);
INSERT INTO `sys_role_menu` VALUES (2012030852630315009, 1993479636925403138, 1993479637311279107, 612);
INSERT INTO `sys_role_menu` VALUES (2012030852630315010, 1993479636925403138, 1993479637311279107, 613);
INSERT INTO `sys_role_menu` VALUES (2012030852697423874, 1993479636925403138, 1993479637311279107, 614);
INSERT INTO `sys_role_menu` VALUES (2012030852760338434, 1993479636925403138, 1993479637311279107, 615);
INSERT INTO `sys_role_menu` VALUES (2012030852567400449, 1993479636925403138, 1993479637311279107, 616);
INSERT INTO `sys_role_menu` VALUES (2012030852567400450, 1993479636925403138, 1993479637311279107, 617);
INSERT INTO `sys_role_menu` VALUES (2012030851787259906, 1993479636925403138, 1993479637311279107, 619);
INSERT INTO `sys_role_menu` VALUES (2012030853083299843, 1993479636925403138, 1993479637311279107, 620);
INSERT INTO `sys_role_menu` VALUES (2012030853083299844, 1993479636925403138, 1993479637311279107, 621);
INSERT INTO `sys_role_menu` VALUES (2012030853150408706, 1993479636925403138, 1993479637311279107, 622);
INSERT INTO `sys_role_menu` VALUES (2012030853150408707, 1993479636925403138, 1993479637311279107, 623);
INSERT INTO `sys_role_menu` VALUES (2012030853150408708, 1993479636925403138, 1993479637311279107, 624);
INSERT INTO `sys_role_menu` VALUES (2012030853217517569, 1993479636925403138, 1993479637311279107, 625);
INSERT INTO `sys_role_menu` VALUES (2012030853217517570, 1993479636925403138, 1993479637311279107, 626);
INSERT INTO `sys_role_menu` VALUES (2012030853280432130, 1993479636925403138, 1993479637311279107, 627);
INSERT INTO `sys_role_menu` VALUES (2012030853280432131, 1993479636925403138, 1993479637311279107, 628);
INSERT INTO `sys_role_menu` VALUES (2012030853347540993, 1993479636925403138, 1993479637311279107, 629);
INSERT INTO `sys_role_menu` VALUES (2012030853347540994, 1993479636925403138, 1993479637311279107, 630);
INSERT INTO `sys_role_menu` VALUES (2012030853347540995, 1993479636925403138, 1993479637311279107, 631);
INSERT INTO `sys_role_menu` VALUES (2012030853414649858, 1993479636925403138, 1993479637311279107, 632);
INSERT INTO `sys_role_menu` VALUES (2012030853414649859, 1993479636925403138, 1993479637311279107, 633);
INSERT INTO `sys_role_menu` VALUES (2012030853414649860, 1993479636925403138, 1993479637311279107, 634);
INSERT INTO `sys_role_menu` VALUES (2012030853485953026, 1993479636925403138, 1993479637311279107, 635);
INSERT INTO `sys_role_menu` VALUES (2012030853485953027, 1993479636925403138, 1993479637311279107, 636);
INSERT INTO `sys_role_menu` VALUES (2012030853548867585, 1993479636925403138, 1993479637311279107, 637);
INSERT INTO `sys_role_menu` VALUES (2012030853548867586, 1993479636925403138, 1993479637311279107, 638);
INSERT INTO `sys_role_menu` VALUES (2012030853615976450, 1993479636925403138, 1993479637311279107, 639);
INSERT INTO `sys_role_menu` VALUES (2012030853615976451, 1993479636925403138, 1993479637311279107, 640);
INSERT INTO `sys_role_menu` VALUES (2012030853615976452, 1993479636925403138, 1993479637311279107, 641);
INSERT INTO `sys_role_menu` VALUES (2012030853678891009, 1993479636925403138, 1993479637311279107, 642);
INSERT INTO `sys_role_menu` VALUES (2012030853678891010, 1993479636925403138, 1993479637311279107, 643);
INSERT INTO `sys_role_menu` VALUES (2012030853741805570, 1993479636925403138, 1993479637311279107, 644);
INSERT INTO `sys_role_menu` VALUES (2012030853741805571, 1993479636925403138, 1993479637311279107, 645);
INSERT INTO `sys_role_menu` VALUES (2012030853741805572, 1993479636925403138, 1993479637311279107, 646);
INSERT INTO `sys_role_menu` VALUES (2012030853808914434, 1993479636925403138, 1993479637311279107, 647);
INSERT INTO `sys_role_menu` VALUES (2012030853808914435, 1993479636925403138, 1993479637311279107, 648);
INSERT INTO `sys_role_menu` VALUES (2012030853871828994, 1993479636925403138, 1993479637311279107, 649);
INSERT INTO `sys_role_menu` VALUES (2012030853871828995, 1993479636925403138, 1993479637311279107, 650);
INSERT INTO `sys_role_menu` VALUES (2012030853871828996, 1993479636925403138, 1993479637311279107, 651);
INSERT INTO `sys_role_menu` VALUES (2012030853871828997, 1993479636925403138, 1993479637311279107, 652);
INSERT INTO `sys_role_menu` VALUES (2012030853871828998, 1993479636925403138, 1993479637311279107, 653);
INSERT INTO `sys_role_menu` VALUES (2012030853871829081, 1993479636925403138, 1993479637311279107, 654);
INSERT INTO `sys_role_menu` VALUES (2012030853871829082, 1993479636925403138, 1993479637311279107, 655);
INSERT INTO `sys_role_menu` VALUES (2012030853871829083, 1993479636925403138, 1993479637311279107, 656);
INSERT INTO `sys_role_menu` VALUES (2012030853871829084, 1993479636925403138, 1993479637311279107, 657);
INSERT INTO `sys_role_menu` VALUES (2012030853871829100, 1993479636925403138, 1993479637311279107, 667);
INSERT INTO `sys_role_menu` VALUES (2012030853871829101, 1993479636925403138, 1993479637311279107, 668);
INSERT INTO `sys_role_menu` VALUES (2012030853871829102, 1993479636925403138, 1993479637311279107, 669);
INSERT INTO `sys_role_menu` VALUES (2012030853871829103, 1993479636925403138, 1993479637311279107, 670);
INSERT INTO `sys_role_menu` VALUES (2012030853871829104, 1993479636925403138, 1993479637311279107, 671);
INSERT INTO `sys_role_menu` VALUES (2012030853871829105, 1993479636925403138, 1993479637311279107, 672);
INSERT INTO `sys_role_menu` VALUES (2012030853871829106, 1993479636925403138, 1993479637311279107, 673);
INSERT INTO `sys_role_menu` VALUES (2012030853871829107, 1993479636925403138, 1993479637311279107, 674);
INSERT INTO `sys_role_menu` VALUES (2012030853871829111, 1993479636925403138, 1993479637311279107, 675);
INSERT INTO `sys_role_menu` VALUES (2012030853871829112, 1993479636925403138, 1993479637311279107, 676);
INSERT INTO `sys_role_menu` VALUES (2012030853871829113, 1993479636925403138, 1993479637311279107, 677);
INSERT INTO `sys_role_menu` VALUES (2012030853871829114, 1993479636925403138, 1993479637311279107, 678);
INSERT INTO `sys_role_menu` VALUES (2012030853871829115, 1993479636925403138, 1993479637311279107, 679);
INSERT INTO `sys_role_menu` VALUES (2012030853871829116, 1993479636925403138, 1993479637311279107, 680);
INSERT INTO `sys_role_menu` VALUES (2012030853871829117, 1993479636925403138, 1993479637311279107, 683);
INSERT INTO `sys_role_menu` VALUES (2012030853871829118, 1993479636925403138, 1993479637311279107, 684);
INSERT INTO `sys_role_menu` VALUES (2012030853871829119, 1993479636925403138, 1993479637311279107, 685);
INSERT INTO `sys_role_menu` VALUES (2012030853871829120, 1993479636925403138, 1993479637311279107, 686);
INSERT INTO `sys_role_menu` VALUES (2012030853871829121, 1993479636925403138, 1993479637311279107, 687);
INSERT INTO `sys_role_menu` VALUES (2012030853871829122, 1993479636925403138, 1993479637311279107, 688);
INSERT INTO `sys_role_menu` VALUES (2012030853871829123, 1993479636925403138, 1993479637311279107, 689);
INSERT INTO `sys_role_menu` VALUES (2012030853871829126, 1993479636925403138, 1993479637311279107, 690);
INSERT INTO `sys_role_menu` VALUES (2012030853871829132, 1993479636925403138, 1993479637311279107, 695);
INSERT INTO `sys_role_menu` VALUES (2012030853871829133, 1993479636925403138, 1993479637311279107, 696);
INSERT INTO `sys_role_menu` VALUES (2012030853871829138, 1993479636925403138, 1993479637311279107, 697);
INSERT INTO `sys_role_menu` VALUES (2012030853871829140, 1993479636925403138, 1993479637311279107, 699);
INSERT INTO `sys_role_menu` VALUES (2012030853871829157, 1993479636925403138, 1993479637311279107, 700);
INSERT INTO `sys_role_menu` VALUES (2012030853871829158, 1993479636925403138, 1993479637311279107, 701);
INSERT INTO `sys_role_menu` VALUES (2012030853871829159, 1993479636925403138, 1993479637311279107, 702);
INSERT INTO `sys_role_menu` VALUES (2012030853871829160, 1993479636925403138, 1993479637311279107, 703);
INSERT INTO `sys_role_menu` VALUES (2012030853871829167, 1993479636925403138, 1993479637311279107, 704);
INSERT INTO `sys_role_menu` VALUES (2012030853871829168, 1993479636925403138, 1993479637311279107, 705);
INSERT INTO `sys_role_menu` VALUES (2012030853871829169, 1993479636925403138, 1993479637311279107, 706);
INSERT INTO `sys_role_menu` VALUES (2012030853871829170, 1993479636925403138, 1993479637311279107, 707);
INSERT INTO `sys_role_menu` VALUES (2012030853871829171, 1993479636925403138, 1993479637311279107, 708);
INSERT INTO `sys_role_menu` VALUES (2012030853871829172, 1993479636925403138, 1993479637311279107, 709);
INSERT INTO `sys_role_menu` VALUES (2012030853871829161, 1993479636925403138, 1993479637311279107, 711);
INSERT INTO `sys_role_menu` VALUES (2012030853871829162, 1993479636925403138, 1993479637311279107, 712);
INSERT INTO `sys_role_menu` VALUES (2012030853871829163, 1993479636925403138, 1993479637311279107, 713);
INSERT INTO `sys_role_menu` VALUES (2012030853871829164, 1993479636925403138, 1993479637311279107, 714);
INSERT INTO `sys_role_menu` VALUES (2012030853871829165, 1993479636925403138, 1993479637311279107, 715);
INSERT INTO `sys_role_menu` VALUES (2012030853871829166, 1993479636925403138, 1993479637311279107, 716);
INSERT INTO `sys_role_menu` VALUES (2012030853871829173, 1993479636925403138, 1993479637311279107, 720);
INSERT INTO `sys_role_menu` VALUES (2012030853871829174, 1993479636925403138, 1993479637311279107, 721);
INSERT INTO `sys_role_menu` VALUES (2012030853871829175, 1993479636925403138, 1993479637311279107, 722);
INSERT INTO `sys_role_menu` VALUES (2012030853871829176, 1993479636925403138, 1993479637311279107, 723);
INSERT INTO `sys_role_menu` VALUES (2012030853871829177, 1993479636925403138, 1993479637311279107, 724);
INSERT INTO `sys_role_menu` VALUES (2012030853871829178, 1993479636925403138, 1993479637311279107, 725);
INSERT INTO `sys_role_menu` VALUES (2012030853871829179, 1993479636925403138, 1993479637311279107, 726);
INSERT INTO `sys_role_menu` VALUES (2012030853871829190, 1993479636925403138, 1993479637311279107, 730);
INSERT INTO `sys_role_menu` VALUES (2012030853871829191, 1993479636925403138, 1993479637311279107, 731);
INSERT INTO `sys_role_menu` VALUES (2012030853871829186, 1993479636925403138, 1993479637311279107, 732);
INSERT INTO `sys_role_menu` VALUES (2012030853871829187, 1993479636925403138, 1993479637311279107, 733);
INSERT INTO `sys_role_menu` VALUES (2012030853871829188, 1993479636925403138, 1993479637311279107, 734);
INSERT INTO `sys_role_menu` VALUES (2012030853871829189, 1993479636925403138, 1993479637311279107, 735);
INSERT INTO `sys_role_menu` VALUES (2012030853871829182, 1993479636925403138, 1993479637311279107, 736);
INSERT INTO `sys_role_menu` VALUES (2012030853871829183, 1993479636925403138, 1993479637311279107, 737);
INSERT INTO `sys_role_menu` VALUES (2012030853871829184, 1993479636925403138, 1993479637311279107, 738);
INSERT INTO `sys_role_menu` VALUES (2012030853871829185, 1993479636925403138, 1993479637311279107, 739);
INSERT INTO `sys_role_menu` VALUES (2012030853871829197, 1993479636925403138, 1993479637311279107, 740);
INSERT INTO `sys_role_menu` VALUES (2012030853871829198, 1993479636925403138, 1993479637311279107, 741);
INSERT INTO `sys_role_menu` VALUES (2012030853871829199, 1993479636925403138, 1993479637311279107, 742);
INSERT INTO `sys_role_menu` VALUES (2012030853871829200, 1993479636925403138, 1993479637311279107, 743);
INSERT INTO `sys_role_menu` VALUES (2046477012460679170, 1993479636925403138, 1993479637311279107, 744);
INSERT INTO `sys_role_menu` VALUES (2046477012460679171, 1993479636925403138, 1993479637311279107, 745);
INSERT INTO `sys_role_menu` VALUES (2046477012460679172, 1993479636925403138, 1993479637311279107, 746);
INSERT INTO `sys_role_menu` VALUES (2046477012460679173, 1993479636925403138, 1993479637311279107, 747);
INSERT INTO `sys_role_menu` VALUES (2046477012460679174, 1993479636925403138, 1993479637311279107, 748);
INSERT INTO `sys_role_menu` VALUES (2046477012460679175, 1993479636925403138, 1993479637311279107, 749);
INSERT INTO `sys_role_menu` VALUES (2046477012460679176, 1993479636925403138, 1993479637311279107, 750);
INSERT INTO `sys_role_menu` VALUES (2046477012460679177, 1993479636925403138, 1993479637311279107, 751);
INSERT INTO `sys_role_menu` VALUES (2046477012460679178, 1993479636925403138, 1993479637311279107, 752);
INSERT INTO `sys_role_menu` VALUES (2046477012460679179, 1993479636925403138, 1993479637311279107, 753);
INSERT INTO `sys_role_menu` VALUES (2012030853871829201, 1993479636925403138, 1993479637311279107, 754);
INSERT INTO `sys_role_menu` VALUES (2012030853871829202, 1993479636925403138, 1993479637311279107, 755);
INSERT INTO `sys_role_menu` VALUES (2012030853871829211, 1993479636925403138, 1993479637311279107, 756);
INSERT INTO `sys_role_menu` VALUES (2012030853871829214, 1993479636925403138, 1993479637311279107, 757);
INSERT INTO `sys_role_menu` VALUES (2012030853871829217, 1993479636925403138, 1993479637311279107, 758);
INSERT INTO `sys_role_menu` VALUES (2012030853871829203, 1993479636925403138, 1993479637311279107, 759);
INSERT INTO `sys_role_menu` VALUES (2012030853871829204, 1993479636925403138, 1993479637311279107, 760);
INSERT INTO `sys_role_menu` VALUES (2012030853871829205, 1993479636925403138, 1993479637311279107, 761);
INSERT INTO `sys_role_menu` VALUES (2012030853871829206, 1993479636925403138, 1993479637311279107, 762);
INSERT INTO `sys_role_menu` VALUES (2012030853871829207, 1993479636925403138, 1993479637311279107, 763);
INSERT INTO `sys_role_menu` VALUES (2012030853871829208, 1993479636925403138, 1993479637311279107, 764);
INSERT INTO `sys_role_menu` VALUES (2012030853871829209, 1993479636925403138, 1993479637311279107, 765);
INSERT INTO `sys_role_menu` VALUES (2012030853871829210, 1993479636925403138, 1993479637311279107, 766);
INSERT INTO `sys_role_menu` VALUES (2012030853871829212, 1993479636925403138, 1993479637311279107, 767);
INSERT INTO `sys_role_menu` VALUES (2012030853871829213, 1993479636925403138, 1993479637311279107, 768);
INSERT INTO `sys_role_menu` VALUES (2012030853871829215, 1993479636925403138, 1993479637311279107, 769);
INSERT INTO `sys_role_menu` VALUES (2012030853871829216, 1993479636925403138, 1993479637311279107, 770);
INSERT INTO `sys_role_menu` VALUES (2012030853871829218, 1993479636925403138, 1993479637311279107, 771);
INSERT INTO `sys_role_menu` VALUES (2012030853871829219, 1993479636925403138, 1993479637311279107, 772);
INSERT INTO `sys_role_menu` VALUES (2012030853871829220, 1993479636925403138, 1993479637311279107, 773);
INSERT INTO `sys_role_menu` VALUES (2012030853871829221, 1993479636925403138, 1993479637311279107, 774);
INSERT INTO `sys_role_menu` VALUES (2012030853871829180, 1993479636925403138, 1993479637311279107, 3000000000000000422);
INSERT INTO `sys_role_menu` VALUES (2012030853871829181, 1993479636925403138, 1993479637311279107, 3000000000000000423);
INSERT INTO `sys_role_menu` VALUES (2047520831801663490, 1993479636925403138, 1993479637311279107, 3000000000000000424);
INSERT INTO `sys_role_menu` VALUES (2047520831801663491, 1993479636925403138, 1993479637311279107, 3000000000000000425);
INSERT INTO `sys_role_menu` VALUES (2046512244757876738, 1993479636925403138, 1993479637311279107, 3000000000000000434);
INSERT INTO `sys_role_menu` VALUES (2046512244757876739, 1993479636925403138, 1993479637311279107, 3000000000000000435);
INSERT INTO `sys_role_menu` VALUES (2046512244757876740, 1993479636925403138, 1993479637311279107, 3000000000000000436);
INSERT INTO `sys_role_menu` VALUES (2046512244757876741, 1993479636925403138, 1993479637311279107, 3000000000000000437);
INSERT INTO `sys_role_menu` VALUES (2046512244757876742, 1993479636925403138, 1993479637311279107, 3000000000000000438);
INSERT INTO `sys_role_menu` VALUES (2046512244757876743, 1993479636925403138, 1993479637311279107, 3000000000000000439);
INSERT INTO `sys_role_menu` VALUES (2046512244757876744, 1993479636925403138, 1993479637311279107, 3000000000000000440);
INSERT INTO `sys_role_menu` VALUES (2046516951681298434, 1993479636925403138, 1993479637311279107, 3000000000000000441);
INSERT INTO `sys_role_menu` VALUES (2046516951681298435, 1993479636925403138, 1993479637311279107, 3000000000000000442);
INSERT INTO `sys_role_menu` VALUES (2046516951681298436, 1993479636925403138, 1993479637311279107, 3000000000000000443);
INSERT INTO `sys_role_menu` VALUES (2046516951681298437, 1993479636925403138, 1993479637311279107, 3000000000000000444);
INSERT INTO `sys_role_menu` VALUES (2046516951681298438, 1993479636925403138, 1993479637311279107, 3000000000000000445);
INSERT INTO `sys_role_menu` VALUES (2046516951681298439, 1993479636925403138, 1993479637311279107, 3000000000000000446);
INSERT INTO `sys_role_menu` VALUES (2046516951681298440, 1993479636925403138, 1993479637311279107, 3000000000000000447);
INSERT INTO `sys_role_menu` VALUES (2047520831801663492, 1993479636925403138, 1993479637311279107, 3000000000000000448);
INSERT INTO `sys_role_menu` VALUES (2047520831801663493, 1993479636925403138, 1993479637311279107, 3000000000000000449);
INSERT INTO `sys_role_menu` VALUES (2047520831801663494, 1993479636925403138, 1993479637311279107, 3000000000000000450);
INSERT INTO `sys_role_menu` VALUES (2047520831801663495, 1993479636925403138, 1993479637311279107, 3000000000000000451);
INSERT INTO `sys_role_menu` VALUES (2047520831801663496, 1993479636925403138, 1993479637311279107, 3000000000000000452);
INSERT INTO `sys_role_menu` VALUES (2047520831801663497, 1993479636925403138, 1993479637311279107, 3000000000000000453);
INSERT INTO `sys_role_menu` VALUES (2047520831801663498, 1993479636925403138, 1993479637311279107, 3000000000000000454);
INSERT INTO `sys_role_menu` VALUES (2047520831801663499, 1993479636925403138, 1993479637311279107, 3000000000000000455);
INSERT INTO `sys_role_menu` VALUES (2047520831801663500, 1993479636925403138, 1993479637311279107, 3000000000000000456);
INSERT INTO `sys_role_menu` VALUES (2047520831801663501, 1993479636925403138, 1993479637311279107, 3000000000000000457);
INSERT INTO `sys_role_menu` VALUES (2047520831801663502, 1993479636925403138, 1993479637311279107, 3000000000000000458);
INSERT INTO `sys_role_menu` VALUES (2047520831801663503, 1993479636925403138, 1993479637311279107, 3000000000000000459);
INSERT INTO `sys_role_menu` VALUES (2047520831801663504, 1993479636925403138, 1993479637311279107, 3000000000000000460);
INSERT INTO `sys_role_menu` VALUES (2047520831801663505, 1993479636925403138, 1993479637311279107, 3000000000000000461);
INSERT INTO `sys_role_menu` VALUES (2047520831801663506, 1993479636925403138, 1993479637311279107, 3000000000000000462);
INSERT INTO `sys_role_menu` VALUES (2047520831801663507, 1993479636925403138, 1993479637311279107, 3000000000000000463);
INSERT INTO `sys_role_menu` VALUES (2047520831801663508, 1993479636925403138, 1993479637311279107, 3000000000000000464);
INSERT INTO `sys_role_menu` VALUES (2047520831801663509, 1993479636925403138, 1993479637311279107, 3000000000000000466);
INSERT INTO `sys_role_menu` VALUES (2047520831801663533, 1993479636925403138, 1993479637311279107, 3000000000000000485);
INSERT INTO `sys_role_menu` VALUES (2047520831801663534, 1993479636925403138, 1993479637311279107, 3000000000000000486);
INSERT INTO `sys_role_menu` VALUES (2047520831801663535, 1993479636925403138, 1993479637311279107, 3000000000000000487);
INSERT INTO `sys_role_menu` VALUES (2047520831801663536, 1993479636925403138, 1993479637311279107, 3000000000000000488);
INSERT INTO `sys_role_menu` VALUES (2047520831801663537, 1993479636925403138, 1993479637311279107, 3000000000000000489);
INSERT INTO `sys_role_menu` VALUES (2047520831801663538, 1993479636925403138, 1993479637311279107, 3000000000000000490);
INSERT INTO `sys_role_menu` VALUES (2047520831801663539, 1993479636925403138, 1993479637311279107, 3000000000000000491);
INSERT INTO `sys_role_menu` VALUES (2047520831801663540, 1993479636925403138, 1993479637311279107, 3000000000000000492);
INSERT INTO `sys_role_menu` VALUES (120, 1993479636925403138, 1993479637311279112, 1);
INSERT INTO `sys_role_menu` VALUES (121, 1993479636925403138, 1993479637311279112, 2);
INSERT INTO `sys_role_menu` VALUES (122, 1993479636925403138, 1993479637311279112, 3);
INSERT INTO `sys_role_menu` VALUES (123, 1993479636925403138, 1993479637311279112, 4);
INSERT INTO `sys_role_menu` VALUES (124, 1993479636925403138, 1993479637311279112, 5);
INSERT INTO `sys_role_menu` VALUES (125, 1993479636925403138, 1993479637311279112, 6);
INSERT INTO `sys_role_menu` VALUES (2012030853871829135, 1993479636925403138, 1993479637311279112, 695);
INSERT INTO `sys_role_menu` VALUES (2012030853871829137, 1993479636925403138, 1993479637311279112, 696);
INSERT INTO `sys_role_menu` VALUES (127, 1993479636925403138, 1993479637311279113, 1);
INSERT INTO `sys_role_menu` VALUES (132, 1993479636925403138, 1993479637311279113, 5);
INSERT INTO `sys_role_menu` VALUES (136, 1993479636925403138, 1993479637311279113, 6);
INSERT INTO `sys_role_menu` VALUES (128, 1993479636925403138, 1993479637311279113, 101);
INSERT INTO `sys_role_menu` VALUES (129, 1993479636925403138, 1993479637311279113, 102);
INSERT INTO `sys_role_menu` VALUES (130, 1993479636925403138, 1993479637311279113, 103);
INSERT INTO `sys_role_menu` VALUES (131, 1993479636925403138, 1993479637311279113, 105);
INSERT INTO `sys_role_menu` VALUES (133, 1993479636925403138, 1993479637311279113, 501);
INSERT INTO `sys_role_menu` VALUES (134, 1993479636925403138, 1993479637311279113, 502);
INSERT INTO `sys_role_menu` VALUES (135, 1993479636925403138, 1993479637311279113, 503);

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`role_id`, `permission_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role_position
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_position`;
CREATE TABLE `sys_role_position`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `role_id` bigint NOT NULL COMMENT '角色 ID',
  `position_id` bigint NOT NULL COMMENT '职位 ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_position_id`(`position_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色职位关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_position
-- ----------------------------

-- ----------------------------
-- Table structure for sys_social_login
-- ----------------------------
DROP TABLE IF EXISTS `sys_social_login`;
CREATE TABLE `sys_social_login`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `platform` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '平台(WECHAT/DINGTALK)',
  `open_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'OpenId',
  `union_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'UnionId',
  `bind_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_platform_openid`(`platform` ASC, `open_id` ASC) USING BTREE,
  INDEX `idx_tenant_user`(`tenant_id` ASC, `user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方登录绑定表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_social_login
-- ----------------------------

-- ----------------------------
-- Table structure for sys_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant`;
CREATE TABLE `sys_tenant`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '租户ID',
  `tenant_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '租户名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人账号/ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人账号/ID',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除 (0:未删除, 1:已删除)',
  `tenant_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `logo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID（可为空，用于自关联或扩展）',
  `tenant_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'MAIN_TENANT' COMMENT '租户类别：MAIN_TENANT-主租户，CUSTOMER_TENANT-客户租户，SUPPLIER_TENANT-供应商租户',
  `parent_tenant_id` bigint NULL DEFAULT NULL COMMENT 'Parent tenant ID; NULL means main or top-level tenant',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tenant_code`(`tenant_code` ASC) USING BTREE,
  INDEX `idx_parent_tenant_id`(`parent_tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1993479636925403141 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tenant
-- ----------------------------
INSERT INTO `sys_tenant` VALUES (1993479636925403138, 'Forgex', '默认租户', 1, '2025-11-26 08:39:17', NULL, '2026-04-04 16:12:43', '1993479637244170242', 0, 'default', '/files/ed86214d98ab467498d2cdf3c4b10f20.png', NULL, 'MAIN_TENANT', NULL);
INSERT INTO `sys_tenant` VALUES (1993479636925403140, '富士康科技集团', '供应商主数据自动生成', 1, '2026-04-30 13:41:06', '1993479637244170242', '2026-04-30 13:41:06', '1993479637244170242', 0, 'sup_supp001', NULL, NULL, 'SUPPLIER_TENANT', NULL);

-- ----------------------------
-- Table structure for sys_tenant_ignore
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_ignore`;
CREATE TABLE `sys_tenant_ignore`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `scope` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '作用域：TABLE/SERVICE/MAPPER',
  `matcher` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '匹配内容：表名/全限定类名/全限定类名#方法名',
  `enabled` tinyint NULL DEFAULT 1 COMMENT '是否启用：1启用 0禁用',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_by` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户隔离跳过配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tenant_ignore
-- ----------------------------
INSERT INTO `sys_tenant_ignore` VALUES (1, 'TABLE', 'sys_user', 1, '用户表不带租户字段', '2025-11-21 14:52:02', '2025-11-21 14:52:02', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (2, 'TABLE', 'sys_tenant', 1, '租户表不带租户字段', '2025-11-21 14:52:02', '2025-11-21 14:52:02', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (3, 'TABLE', 'sys_user_tenant', 1, '用户-租户关联表不带租户字段', '2025-11-21 14:52:02', '2025-11-21 14:52:02', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (4, 'TABLE', 'sys_config', 1, '配置表跨租户', '2025-11-22 10:12:39', '2025-11-22 10:12:39', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (5, 'TABLE', 'sys_config', 1, '系统配置表使用公共库，跳过租户隔离', '2025-11-24 10:18:37', '2025-11-24 10:18:37', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (6, 'TABLE', 'sys_tenant_ignore', 1, '忽略租户隔离跳过配置表', '2025-11-24 10:31:35', '2025-11-24 10:31:35', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (7, 'TABLE', 'fx_i18n_message', 1, '返回消息国际化配置表跳过租户隔离', '2026-01-16 10:38:53', '2026-01-16 10:38:53', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (8, 'TABLE', 'fx_table_config', 1, '通用表格配置主表跳过租户隔离', '2026-01-16 10:38:53', '2026-01-16 10:38:53', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (9, 'TABLE', 'fx_table_column_config', 1, '通用表格列配置子表跳过租户隔离', '2026-01-16 10:38:53', '2026-01-16 10:38:53', 0, NULL);
INSERT INTO `sys_tenant_ignore` VALUES (10, 'TABLE', 'fx_i18n_language_type', 1, '多语言配置', '2026-01-18 10:50:44', '2026-01-18 10:50:44', 0, NULL);

-- ----------------------------
-- Table structure for sys_tenant_message_whitelist
-- ----------------------------
DROP TABLE IF EXISTS `sys_tenant_message_whitelist`;
CREATE TABLE `sys_tenant_message_whitelist`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `tenant_id` bigint NOT NULL COMMENT '租户ID',
  `sender_tenant_id` bigint NOT NULL COMMENT '发送方租户ID',
  `receiver_tenant_id` bigint NOT NULL COMMENT '接收方租户ID',
  `enabled` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否启用（1-启用，0-禁用）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '删除标记（0-未删除，1-已删除）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sender_receiver`(`sender_tenant_id` ASC, `receiver_tenant_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_enabled`(`enabled` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '租户消息白名单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_tenant_message_whitelist
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `account` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码 (加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态 (1:启用, 0:禁用)',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '修改人',
  `deleted` tinyint NULL DEFAULT 0 COMMENT '逻辑删除',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `gender` tinyint NULL DEFAULT NULL COMMENT '性别：0=未知，1=男，2=女',
  `entry_date` date NULL DEFAULT NULL COMMENT '入职时间',
  `department_id` bigint NULL DEFAULT NULL COMMENT '所属部门ID',
  `position_id` bigint NULL DEFAULT NULL COMMENT '职位ID',
  `last_login_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `last_login_region` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录地区',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像URL',
  `user_source` tinyint NOT NULL DEFAULT 1 COMMENT '用户来源:1本站新增,2本站导入,3第三方同步,4自行注册',
  `employee_id` bigint NULL DEFAULT NULL COMMENT '关联员工ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_account`(`account` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE,
  INDEX `idx_position_id`(`position_id` ASC) USING BTREE,
  INDEX `idx_user_source`(`user_source` ASC) USING BTREE,
  INDEX `idx_employee_id`(`employee_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1993479637244170254 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1993479637244170242, 'admin', 'admin', '$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq', 'admin@local.com', NULL, 1, '2025-11-26 08:39:17', NULL, '2026-05-02 20:55:13', '1993479637244170242', 0, NULL, 1, '2026-04-04', 1, 11, '0:0:0:0:0:0:0:1', '本地', '2026-05-02 20:55:14', 'http://192.168.121.1:9000/api/files/fef4cdfc3b304e089ede5102c7369e18.jpg', 1, NULL);
INSERT INTO `sys_user` VALUES (1993479637244170249, 'test001', '测试用户1', '$2a$10$TN2WOn63RiPL.8iFvPBRZOxprURcDDWzMKYRcYEG4pu.qwVbxwUI6', 'test001@forgex.com', '13800138001', 1, '2026-01-08 10:58:58', NULL, '2026-04-04 16:05:26', '1993479637244170242', 0, NULL, 1, '2026-01-01', 7, 7, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (1993479637244170250, 'test002', '测试用户2', '$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq', 'test002@forgex.com', '13800138002', 1, '2026-01-08 10:58:58', NULL, '2026-01-08 10:58:58', NULL, 0, NULL, 2, '2026-01-02', 8, 8, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (1993479637244170251, 'test003', '测试用户3', '$2a$10$D9IQgkg4SLm8tktsy75RY.KlJBOeN1d0.VZb1PWSlepMNqQmCTuGq', 'test003@forgex.com', '13800138003', 1, '2026-01-08 10:58:58', NULL, '2026-01-08 10:58:58', NULL, 0, NULL, 1, '2026-01-03', 9, 9, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (1993479637244170252, 'test', 'test用户', '$2a$10$U4qFzeT00nwD4BcdhQQJHeF7cF81bP0VodQVNBegEeHkFe20t2VBe', 'coderr_nai@163.com', '15866912378', 1, '2026-04-04 11:22:02', '1993479637244170242', '2026-04-04 14:29:17', '1993479637244170242', 1, NULL, 1, '2026-04-04', 1, 11, NULL, NULL, NULL, NULL, 1, NULL);
INSERT INTO `sys_user` VALUES (1993479637244170253, 'smy', '孙明岩', '$2a$10$KPeYoW4LXUO7Zmpo/LLZWuujeXwMwtmZllcvbfShIoJrvmRKrF4oK', '', '', 1, '2026-04-10 16:45:45', '1993479637244170242', '2026-04-22 21:55:42', '1993479637244170242', 0, 1993479636925403138, 1, '2026-04-10', 1, 12, '0:0:0:0:0:0:0:1', '本地', '2026-04-22 21:55:43', NULL, 1, NULL);

-- ----------------------------
-- Table structure for sys_user_c_menu_favorite
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_c_menu_favorite`;
CREATE TABLE `sys_user_c_menu_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `c_menu_id` bigint NOT NULL COMMENT 'C 端菜单 ID',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_c_menu`(`tenant_id` ASC, `user_id` ASC, `c_menu_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_c_menu_id`(`c_menu_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户 C 端菜单收藏表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_c_menu_favorite
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user_menu_common
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu_common`;
CREATE TABLE `sys_user_menu_common`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端完整菜单路径',
  `menu_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标题快照',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块名称快照',
  `menu_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `visit_count` int NOT NULL DEFAULT 1 COMMENT '访问次数',
  `last_visited_at` datetime NULL DEFAULT NULL COMMENT '最近访问时间',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_common_menu_path`(`tenant_id` ASC, `user_id` ASC, `menu_path` ASC) USING BTREE,
  INDEX `idx_user_common_menu_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_common_menu_visit`(`tenant_id` ASC, `user_id` ASC, `visit_count` ASC, `last_visited_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2050572167028322306 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户常用菜单访问统计表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_menu_common
-- ----------------------------
INSERT INTO `sys_user_menu_common` VALUES (2043349513253105666, 1993479636925403138, 1993479637244170242, '/workspace/sys/dashboard', '系统管理主页', 'sys', '系统管理', 'DashboardOutlined', 237, '2026-05-02 21:06:16', '2026-04-12 23:24:22', '2026-04-12 23:24:22');
INSERT INTO `sys_user_menu_common` VALUES (2043352022986207233, 1993479636925403138, 1993479637244170242, '/workspace/basic/basicInfo/placeholder', '基础信息占位页', 'basic', '基础数据', 'FormOutlined', 64, '2026-04-29 19:02:28', '2026-04-12 23:34:21', '2026-04-12 23:34:21');
INSERT INTO `sys_user_menu_common` VALUES (2043352046373646338, 1993479636925403138, 1993479637244170242, '/workspace/sys/organization/user', '用户管理', 'sys', '系统管理', 'UserOutlined', 132, '2026-05-02 20:27:37', '2026-04-12 23:34:26', '2026-04-12 23:34:26');
INSERT INTO `sys_user_menu_common` VALUES (2043494047891017730, 1993479636925403138, 1993479637244170242, '/workspace/sys/authorization/menu', 'Menus', 'sys', 'System', 'MenuOutlined', 31, '2026-05-02 20:28:15', '2026-04-13 08:58:42', '2026-04-13 08:58:42');
INSERT INTO `sys_user_menu_common` VALUES (2043494256326955009, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/online', '在线用户', 'sys', '系统管理', 'UsergroupAddOutlined', 29, '2026-05-01 15:27:34', '2026-04-13 08:59:32', '2026-04-13 08:59:32');
INSERT INTO `sys_user_menu_common` VALUES (2043502753055723521, 1993479636925403138, 1993479637244170242, '/workspace/approval/dashboard', '审批工作台', 'approval', '审批管理', 'DashboardOutlined', 84, '2026-05-02 21:06:20', '2026-04-13 09:33:17', '2026-04-13 09:33:17');
INSERT INTO `sys_user_menu_common` VALUES (2046951096131276802, 1993479636925403138, 1993479637244170253, '/workspace/approval/my/pending', '我的待办', 'approval', '审批管理', 'ClockCircleOutlined', 3, '2026-04-22 23:29:29', '2026-04-22 21:55:46', '2026-04-22 21:55:46');
INSERT INTO `sys_user_menu_common` VALUES (2046951442647896066, 1993479636925403138, 1993479637244170253, '/workspace/sys/dashboard', '系统管理主页', 'sys', '系统管理', 'DashboardOutlined', 2, '2026-04-22 21:57:15', '2026-04-22 21:57:09', '2026-04-22 21:57:09');
INSERT INTO `sys_user_menu_common` VALUES (2046951458426867714, 1993479636925403138, 1993479637244170253, '/workspace/approval/dashboard', '审批工作台', 'approval', '审批管理', 'DashboardOutlined', 3, '2026-04-22 23:29:14', '2026-04-22 21:57:13', '2026-04-22 21:57:13');
INSERT INTO `sys_user_menu_common` VALUES (2046958078586277890, 1993479636925403138, 1993479637244170253, '/workspace/approval/execution/start', '发起审批', 'approval', '审批管理', 'PlayCircleOutlined', 1, '2026-04-22 22:23:31', '2026-04-22 22:23:31', '2026-04-22 22:23:31');
INSERT INTO `sys_user_menu_common` VALUES (2046958112874713090, 1993479636925403138, 1993479637244170253, '/workspace/approval/my/processed', '我已处理', 'approval', '审批管理', 'CheckCircleOutlined', 11, '2026-04-22 23:30:13', '2026-04-22 22:23:39', '2026-04-22 22:23:39');
INSERT INTO `sys_user_menu_common` VALUES (2046974804170395649, 1993479636925403138, 1993479637244170253, '/workspace/approval/my/initiated', '我发起的', 'approval', '审批管理', 'SendOutlined', 1, '2026-04-22 23:29:59', '2026-04-22 23:29:59', '2026-04-22 23:29:59');

-- ----------------------------
-- Table structure for sys_user_menu_favorite
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu_favorite`;
CREATE TABLE `sys_user_menu_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '前端完整菜单路径',
  `menu_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标题快照',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块名称快照',
  `menu_icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `order_num` int NULL DEFAULT NULL COMMENT '收藏排序号',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_favorite_menu_path`(`tenant_id` ASC, `user_id` ASC, `menu_path` ASC) USING BTREE,
  INDEX `idx_user_favorite_menu_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_favorite_menu_order`(`tenant_id` ASC, `user_id` ASC, `order_num` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2043498524798832642 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户收藏菜单表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_menu_favorite
-- ----------------------------
INSERT INTO `sys_user_menu_favorite` VALUES (2043498524798832641, 1993479636925403138, 1993479637244170242, '/workspace/approval/my/pending', '我的待办', 'approval', '审批管理', 'ClockCircleOutlined', 1, '2026-04-13 09:16:29');

-- ----------------------------
-- Table structure for sys_user_menu_open_count
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_menu_open_count`;
CREATE TABLE `sys_user_menu_open_count`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
  `tenant_id` bigint NOT NULL COMMENT '租户 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `menu_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单完整路由路径',
  `menu_title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单标题',
  `module_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块编码',
  `module_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '模块名称',
  `menu_icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '菜单图标',
  `open_count` int NOT NULL DEFAULT 0 COMMENT '打开次数',
  `first_open_at` datetime NULL DEFAULT NULL COMMENT '首次打开时间',
  `last_open_at` datetime NULL DEFAULT NULL COMMENT '最近打开时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_sys_user_menu_open_count_user_path`(`tenant_id` ASC, `user_id` ASC, `menu_path` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sys_user_menu_open_count_module`(`tenant_id` ASC, `user_id` ASC, `module_code` ASC, `deleted` ASC) USING BTREE,
  INDEX `idx_sys_user_menu_open_count_last_open`(`last_open_at` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户菜单打开次数表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_menu_open_count
-- ----------------------------
INSERT INTO `sys_user_menu_open_count` VALUES (1, 1993479636925403138, 1993479637244170242, '/workspace/sys/dashboard', '系统管理主页', 'sys', '系统管理', 'DashboardOutlined', 46, '2026-04-29 22:31:28', '2026-05-02 21:06:16', '2026-04-29 22:31:28', '1993479637244170242', '2026-04-29 22:31:28', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (2, 1993479636925403138, 1993479637244170242, '/workspace/sys/organization/user', '用户管理', 'sys', '系统管理', 'UserOutlined', 52, '2026-04-29 22:32:22', '2026-05-02 20:27:37', '2026-04-29 22:32:22', '1993479637244170242', '2026-04-29 22:32:22', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (3, 1993479636925403138, 1993479637244170242, '/workspace/sys/organization/position', '职位管理', 'sys', '系统管理', 'IdcardOutlined', 7, '2026-04-30 00:14:21', '2026-04-30 17:28:53', '2026-04-30 00:14:21', '1993479637244170242', '2026-04-30 00:14:21', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (4, 1993479636925403138, 1993479637244170242, '/workspace/sys/organization/department', '部门管理', 'sys', '系统管理', 'ApartmentOutlined', 4, '2026-04-30 00:14:22', '2026-04-30 17:28:34', '2026-04-30 00:14:22', '1993479637244170242', '2026-04-30 00:14:22', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (5, 1993479636925403138, 1993479637244170242, '/workspace/sys/authorization/tenant', 'Tenants', 'sys', 'System', 'TeamOutlined', 7, '2026-04-30 00:14:37', '2026-05-02 20:28:14', '2026-04-30 00:14:37', '1993479637244170242', '2026-04-30 00:14:37', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (6, 1993479636925403138, 1993479637244170242, '/workspace/sys/organization/inviteCode', '邀请码管理', 'sys', '系统管理', 'KeyOutlined', 4, '2026-04-30 00:14:52', '2026-05-01 21:28:41', '2026-04-30 00:14:52', '1993479637244170242', '2026-04-30 00:14:52', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (7, 1993479636925403138, 1993479637244170242, '/workspace/sys/authorization/role', 'Roles', 'sys', 'System', 'TeamOutlined', 6, '2026-04-30 00:15:00', '2026-05-02 20:28:13', '2026-04-30 00:15:00', '1993479637244170242', '2026-04-30 00:15:00', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (8, 1993479636925403138, 1993479637244170242, '/workspace/sys/authorization/menu', 'Menus', 'sys', 'System', 'MenuOutlined', 4, '2026-04-30 00:15:03', '2026-05-02 20:28:15', '2026-04-30 00:15:03', '1993479637244170242', '2026-04-30 00:15:03', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (9, 1993479636925403138, 1993479637244170242, '/workspace/sys/module', 'Modules', 'sys', 'System', 'AppstoreOutlined', 4, '2026-04-30 00:15:07', '2026-05-02 20:28:20', '2026-04-30 00:15:07', '1993479637244170242', '2026-04-30 00:15:07', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (10, 1993479636925403138, 1993479637244170242, '/workspace/sys/excelConfig/excelImportConfig', '导入配置', 'sys', '系统管理', 'FileExcelOutlined', 3, '2026-04-30 00:15:09', '2026-05-01 21:10:27', '2026-04-30 00:15:09', '1993479637244170242', '2026-04-30 00:15:09', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (11, 1993479636925403138, 1993479637244170242, '/workspace/sys/excelConfig/excelExportConfig', '导出配置', 'sys', '系统管理', 'FileExcelOutlined', 3, '2026-04-30 00:15:10', '2026-05-01 21:10:26', '2026-04-30 00:15:10', '1993479637244170242', '2026-04-30 00:15:10', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (12, 1993479636925403138, 1993479637244170242, '/workspace/sys/dict', 'Dictionary', 'sys', 'System', 'BookOutlined', 5, '2026-04-30 00:15:12', '2026-05-02 20:28:22', '2026-04-30 00:15:12', '1993479637244170242', '2026-04-30 00:15:12', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (13, 1993479636925403138, 1993479637244170242, '/workspace/sys/pageTableConfig/userTableConfig', '用户列设置', 'sys', '系统管理', 'ColumnWidthOutlined', 1, '2026-04-30 00:15:15', '2026-04-30 00:15:15', '2026-04-30 00:15:15', '1993479637244170242', '2026-04-30 00:15:15', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (14, 1993479636925403138, 1993479637244170242, '/workspace/sys/pageTableConfig/tableConfig', 'Table Config', 'sys', 'System', 'TableOutlined', 3, '2026-04-30 00:15:17', '2026-05-02 20:28:30', '2026-04-30 00:15:17', '1993479637244170242', '2026-04-30 00:15:17', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (15, 1993479636925403138, 1993479637244170242, '/workspace/sys/loginLog', '登录日志', 'sys', '系统管理', 'FileTextOutlined', 2, '2026-04-30 00:15:19', '2026-05-02 16:31:34', '2026-04-30 00:15:19', '1993479637244170242', '2026-04-30 00:15:19', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (16, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/online', '在线用户', 'sys', '系统管理', 'UsergroupAddOutlined', 3, '2026-04-30 00:15:21', '2026-05-01 15:27:34', '2026-04-30 00:15:21', '1993479637244170242', '2026-04-30 00:15:21', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (17, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/messageTemplate', '消息模板', 'sys', '系统管理', 'MailOutlined', 2, '2026-04-30 00:15:22', '2026-05-01 15:27:39', '2026-04-30 00:15:22', '1993479637244170242', '2026-04-30 00:15:22', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (18, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/tenantMessageWhitelist', '租户消息白名单', 'sys', '系统管理', 'SafetyCertificateOutlined', 3, '2026-04-30 00:15:23', '2026-05-02 20:31:04', '2026-04-30 00:15:23', '1993479637244170242', '2026-04-30 00:15:23', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (19, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/operationLog', '操作日志', 'sys', '系统管理', 'FileTextOutlined', 3, '2026-04-30 00:15:25', '2026-05-01 15:27:35', '2026-04-30 00:15:25', '1993479637244170242', '2026-04-30 00:15:25', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (20, 1993479636925403138, 1993479637244170242, '/workspace/sys/maintenance/config', '系统配置', 'sys', '系统管理', 'SettingOutlined', 36, '2026-04-30 00:15:27', '2026-05-02 20:31:56', '2026-04-30 00:15:27', '1993479637244170242', '2026-04-30 00:15:27', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (21, 1993479636925403138, 1993479637244170242, '/workspace/sys/i18nConfig/i18nMessage', '多语言消息', 'sys', '系统管理', 'MessageOutlined', 7, '2026-04-30 00:15:29', '2026-05-01 15:59:35', '2026-04-30 00:15:29', '1993479637244170242', '2026-04-30 00:15:29', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (22, 1993479636925403138, 1993479637244170242, '/workspace/sys/i18nConfig/i18nLanguageType', '语言配置', 'sys', '系统管理', 'GlobalOutlined', 2, '2026-04-30 00:15:31', '2026-05-01 15:27:52', '2026-04-30 00:15:31', '1993479637244170242', '2026-04-30 00:15:31', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (23, 1993479636925403138, 1993479637244170242, '/workspace/sys/file', '文件管理', 'sys', '系统管理', 'FolderOpenOutlined', 2, '2026-04-30 00:15:37', '2026-04-30 09:59:55', '2026-04-30 00:15:37', '1993479637244170242', '2026-04-30 00:15:37', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (24, 1993479636925403138, 1993479637244170242, '/workspace/sys/onlineDev/codegenDatasource', '代码生成数据源', 'sys', '系统管理', 'DatabaseOutlined', 4, '2026-04-30 00:15:46', '2026-04-30 10:00:36', '2026-04-30 00:15:46', '1993479637244170242', '2026-04-30 00:15:46', '1993479637244170242', 0);
INSERT INTO `sys_user_menu_open_count` VALUES (25, 1993479636925403138, 1993479637244170242, '/workspace/sys/onlineDev/codegen', '代码生成', 'sys', '系统管理', 'CodeOutlined', 1, '2026-04-30 00:15:48', '2026-04-30 00:15:48', '2026-04-30 00:15:48', '1993479637244170242', '2026-04-30 00:15:48', '1993479637244170242', 0);

-- ----------------------------
-- Table structure for sys_user_profile
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_profile`;
CREATE TABLE `sys_user_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `political_status` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '政治面貌',
  `home_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '家庭住址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `referrer` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '引荐人',
  `education` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学历',
  `work_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '历史工作信息（JSON格式）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `update_by` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除：0=未删除 1=已删除',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `birth_place` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '籍贯',
  `intro` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人简介',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id`(`user_id` ASC) USING BTREE,
  UNIQUE INDEX `uk_profile_user_tenant`(`tenant_id` ASC, `user_id` ASC) USING BTREE,
  INDEX `idx_tenant_id`(`tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户附属信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_profile
-- ----------------------------
INSERT INTO `sys_user_profile` VALUES (1, 1993479637244170249, 'other', '', '', '', '', '', '[]', '2026-01-17 10:43:09', '2026-01-17 10:43:09', '1993479637244170242', '1993479637244170242', 0, 1993479636925403138, '', '');
INSERT INTO `sys_user_profile` VALUES (2, 1993479637244170242, '', '', '', '', '', '', '[]', '2026-04-02 23:53:01', '2026-04-02 23:53:01', '1993479637244170242', '1993479637244170242', 0, 1993479636925403138, '', '');
INSERT INTO `sys_user_profile` VALUES (3, 1993479637244170252, '', '', '', '', '', '', '[]', '2026-04-04 11:22:02', '2026-04-04 11:22:02', '1993479637244170242', '1993479637244170242', 0, 1993479636925403138, '', '');
INSERT INTO `sys_user_profile` VALUES (4, 1993479637244170253, '', '', '', '', '', '', '[]', '2026-04-10 16:45:45', '2026-04-10 16:45:45', '1993479637244170242', '1993479637244170242', 0, 1993479636925403138, '', '');

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_role_uid_rid_tenant`(`user_id` ASC, `role_id` ASC, `tenant_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2042819847144521731 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关联' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1993479637244170242, 1993479637311279107, 1993479636925403138, 1993479637311279108);
INSERT INTO `sys_user_role` VALUES (1993479637244170253, 1993479637311279107, 1993479636925403138, 2042819847144521730);

-- ----------------------------
-- Table structure for sys_user_tenant
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_tenant`;
CREATE TABLE `sys_user_tenant`  (
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `tenant_id` bigint NULL DEFAULT NULL COMMENT '租户ID',
  `id` bigint(20) UNSIGNED ZEROFILL NOT NULL AUTO_INCREMENT COMMENT 'id主键',
  `pref_order` int NULL DEFAULT 0 COMMENT '租户喜好排序',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认租户',
  `last_used` datetime NULL DEFAULT NULL COMMENT '最后使用时间',
  PRIMARY KEY (`id` DESC) USING BTREE,
  UNIQUE INDEX `uk_user_tenant`(`user_id` ASC, `tenant_id` ASC) USING BTREE,
  INDEX `tenant_id`(`tenant_id` ASC) USING BTREE,
  INDEX `idx_user_pref`(`user_id` ASC, `pref_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2042524421577154562 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户租户关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_tenant
-- ----------------------------
INSERT INTO `sys_user_tenant` VALUES (1993479637244170253, 1993479636925403138, 02042524421577154561, 3, 1, '2026-04-22 21:55:43');
INSERT INTO `sys_user_tenant` VALUES (1993479637244170242, 1993479636925403138, 01993479637311279106, 249, 0, '2026-05-02 20:55:14');

SET FOREIGN_KEY_CHECKS = 1;
