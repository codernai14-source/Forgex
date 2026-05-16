-- System notice module and master data query config upgrade.
-- Safe to rerun: all data inserts use NOT EXISTS guards.

SET NAMES utf8mb4;

USE forgex_admin;

SET @OPERATOR := 'codex';
SET @NOW := NOW();

CREATE TABLE IF NOT EXISTS `sys_notice` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知主表';

CREATE TABLE IF NOT EXISTS `sys_notice_attachment` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知附件表';

CREATE TABLE IF NOT EXISTS `sys_notice_user_record` (
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知用户记录表';

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_notice', 1, '系统通知表由业务条件处理公共/租户范围', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_notice'
);

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_notice_attachment', 1, '系统通知附件表由通知范围控制可见性', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_notice_attachment'
);

INSERT INTO `sys_tenant_ignore`
(`scope`,`matcher`,`enabled`,`remark`,`create_time`,`update_time`,`deleted`,`create_by`)
SELECT 'TABLE', 'sys_notice_user_record', 1, '系统通知弹窗记录按当前用户和租户显式过滤', @NOW, @NOW, 0, @OPERATOR
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_tenant_ignore` t
  WHERE t.deleted = 0 AND t.scope = 'TABLE' AND t.matcher = 'sys_notice_user_record'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000551, 0, 'PUBLIC', 1, 654, 'menu', 'notice', '系统通知',
       JSON_OBJECT('zh-CN','系统通知','en-US','System Notice','zh-TW','系統通知','ja-JP','システム通知','ko-KR','시스템 알림'),
       'NotificationOutlined', 'SystemNotice', 'sys:notice:view', 5, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 2, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` WHERE deleted = 0 AND parent_id = 654 AND path = 'notice'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000552, 0, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '查看系统通知',
       JSON_OBJECT('zh-CN','查看系统通知','en-US','View System Notice','zh-TW','查看系統通知','ja-JP','システム通知表示','ko-KR','시스템 알림 보기'),
       NULL, NULL, 'sys:notice:view', 1, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = 3000000000000000551 AND m.perm_key = 'sys:notice:view'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000553, 0, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '新增系统通知',
       JSON_OBJECT('zh-CN','新增系统通知','en-US','Add System Notice','zh-TW','新增系統通知','ja-JP','システム通知追加','ko-KR','시스템 알림 추가'),
       NULL, NULL, 'sys:notice:add', 2, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = 3000000000000000551 AND m.perm_key = 'sys:notice:add'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000554, 0, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '编辑系统通知',
       JSON_OBJECT('zh-CN','编辑系统通知','en-US','Edit System Notice','zh-TW','編輯系統通知','ja-JP','システム通知編集','ko-KR','시스템 알림 편집'),
       NULL, NULL, 'sys:notice:edit', 3, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = 3000000000000000551 AND m.perm_key = 'sys:notice:edit'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000555, 0, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '删除系统通知',
       JSON_OBJECT('zh-CN','删除系统通知','en-US','Delete System Notice','zh-TW','刪除系統通知','ja-JP','システム通知削除','ko-KR','시스템 알림 삭제'),
       NULL, NULL, 'sys:notice:delete', 4, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = 3000000000000000551 AND m.perm_key = 'sys:notice:delete'
);

INSERT INTO `sys_menu`
(`id`,`tenant_id`,`tenant_type`,`module_id`,`parent_id`,`type`,`path`,`name`,`name_i18n_json`,`icon`,`component_key`,`perm_key`,`order_num`,`visible`,`status`,`create_time`,`create_by`,`update_time`,`update_by`,`deleted`,`menu_level`,`menu_mode`,`external_url`)
SELECT 3000000000000000556, 0, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '发布/停用系统通知',
       JSON_OBJECT('zh-CN','发布/停用系统通知','en-US','Publish or Disable System Notice','zh-TW','發布/停用系統通知','ja-JP','システム通知公開/無効化','ko-KR','시스템 알림 게시/중지'),
       NULL, NULL, 'sys:notice:publish', 5, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_menu` m WHERE m.deleted = 0 AND m.parent_id = 3000000000000000551 AND m.perm_key = 'sys:notice:publish'
);

INSERT INTO `sys_permission`
(`id`,`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT 3000000000000000561, '查看系统通知', 'sys:notice:view', '/sys/notice/page', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:notice:view'
);

INSERT INTO `sys_permission`
(`id`,`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT 3000000000000000562, '新增系统通知', 'sys:notice:add', '/sys/notice/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:notice:add'
);

INSERT INTO `sys_permission`
(`id`,`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT 3000000000000000563, '编辑系统通知', 'sys:notice:edit', '/sys/notice/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:notice:edit'
);

INSERT INTO `sys_permission`
(`id`,`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT 3000000000000000564, '删除系统通知', 'sys:notice:delete', '/sys/notice/delete', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:notice:delete'
);

INSERT INTO `sys_permission`
(`id`,`permission_name`,`permission_key`,`url`,`method`,`tenant_id`,`create_time`,`update_time`,`deleted`)
SELECT 3000000000000000565, '发布/停用系统通知', 'sys:notice:publish', '/sys/notice/publish', 'POST', 0, @NOW, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_permission` p WHERE p.permission_key = 'sys:notice:publish'
);

DROP TEMPORARY TABLE IF EXISTS tmp_sys_notice_dict_20260510;
CREATE TEMPORARY TABLE tmp_sys_notice_dict_20260510 (
  id bigint NOT NULL PRIMARY KEY,
  parent_id bigint NOT NULL,
  dict_name varchar(100) NOT NULL,
  dict_code varchar(50) NOT NULL,
  dict_value varchar(50) DEFAULT NULL,
  i18n_json text,
  node_path varchar(512) NOT NULL,
  level int NOT NULL,
  children_count int NOT NULL,
  order_num int NOT NULL,
  remark varchar(500) DEFAULT NULL,
  tag_style_json text
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_sys_notice_dict_20260510
(id,parent_id,dict_name,dict_code,dict_value,i18n_json,node_path,level,children_count,order_num,remark,tag_style_json)
VALUES
(3000000000000000571,0,'通知范围','notice_scope',NULL,JSON_OBJECT('zh-CN','通知范围','en-US','Notice Scope','zh-TW','通知範圍','ja-JP','通知範囲','ko-KR','알림 범위'),'notice_scope',1,2,1,'系统通知范围',NULL),
(3000000000000000572,3000000000000000571,'公共','public','PUBLIC',JSON_OBJECT('zh-CN','公共','en-US','Public','zh-TW','公共','ja-JP','公開','ko-KR','공용'),'notice_scope/public',2,0,1,'系统通知范围',JSON_OBJECT('color','blue')),
(3000000000000000573,3000000000000000571,'租户','tenant','TENANT',JSON_OBJECT('zh-CN','租户','en-US','Tenant','zh-TW','租戶','ja-JP','テナント','ko-KR','테넌트'),'notice_scope/tenant',2,0,2,'系统通知范围',JSON_OBJECT('color','green')),
(3000000000000000574,0,'通知状态','notice_status',NULL,JSON_OBJECT('zh-CN','通知状态','en-US','Notice Status','zh-TW','通知狀態','ja-JP','通知状態','ko-KR','알림 상태'),'notice_status',1,3,2,'系统通知状态',NULL),
(3000000000000000575,3000000000000000574,'草稿','draft','DRAFT',JSON_OBJECT('zh-CN','草稿','en-US','Draft','zh-TW','草稿','ja-JP','下書き','ko-KR','초안'),'notice_status/draft',2,0,1,'系统通知状态',JSON_OBJECT('color','default')),
(3000000000000000576,3000000000000000574,'已发布','published','PUBLISHED',JSON_OBJECT('zh-CN','已发布','en-US','Published','zh-TW','已發布','ja-JP','公開済み','ko-KR','게시됨'),'notice_status/published',2,0,2,'系统通知状态',JSON_OBJECT('color','success')),
(3000000000000000577,3000000000000000574,'已停用','disabled','DISABLED',JSON_OBJECT('zh-CN','已停用','en-US','Disabled','zh-TW','已停用','ja-JP','無効','ko-KR','중지됨'),'notice_status/disabled',2,0,3,'系统通知状态',JSON_OBJECT('color','error'));

INSERT INTO `sys_dict`
(`id`,`parent_id`,`dict_name`,`dict_code`,`module_id`,`dict_value`,`dict_value_i18n_json`,`node_path`,`level`,`children_count`,`order_num`,`status`,`remark`,`tenant_id`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`,`tag_style_json`)
SELECT v.id, v.parent_id, v.dict_name, v.dict_code, 1, v.dict_value, v.i18n_json, v.node_path, v.level, v.children_count,
       v.order_num, 1, v.remark, 0, NULL, @NOW, NULL, @NOW, 0, v.tag_style_json
FROM tmp_sys_notice_dict_20260510 v
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_dict` d WHERE d.tenant_id = 0 AND d.node_path = v.node_path AND d.deleted = 0
);

USE forgex_common;

INSERT INTO `fx_table_config`
(`id`,`tenant_id`,`table_code`,`table_name_i18n_json`,`table_type`,`row_key`,`default_page_size`,`default_sort_json`,`enabled`,`version`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT 3000000000000000581, 0, 'SystemNoticeTable',
       JSON_OBJECT('zh-CN','系统通知','en-US','System Notice','zh-TW','系統通知','ja-JP','システム通知','ko-KR','시스템 알림'),
       'COMMON', 'id', 10, JSON_OBJECT('field','orderNum','order','asc'), 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable'
);

DROP TEMPORARY TABLE IF EXISTS tmp_system_notice_table_columns_20260510;
CREATE TEMPORARY TABLE tmp_system_notice_table_columns_20260510 (
  id bigint NOT NULL PRIMARY KEY,
  field varchar(128) NOT NULL,
  title_i18n_json text,
  align varchar(16),
  width int,
  fixed varchar(16),
  ellipsis tinyint NOT NULL DEFAULT 0,
  sortable tinyint NOT NULL DEFAULT 0,
  sorter_field varchar(128),
  queryable tinyint NOT NULL DEFAULT 0,
  query_type varchar(32),
  query_operator varchar(32),
  dict_code varchar(100),
  render_type varchar(32),
  perm_key varchar(100),
  order_num int NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO tmp_system_notice_table_columns_20260510
(id,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num)
VALUES
(3000000000000000582,'title',JSON_OBJECT('zh-CN','通知标题','en-US','Title','zh-TW','通知標題','ja-JP','通知タイトル','ko-KR','알림 제목'),'left',240,NULL,0,1,'title',1,'input','like',NULL,NULL,NULL,1),
(3000000000000000583,'scope',JSON_OBJECT('zh-CN','范围','en-US','Scope','zh-TW','範圍','ja-JP','範囲','ko-KR','범위'),'center',100,NULL,0,1,'scope',1,'select','eq','notice_scope',NULL,NULL,2),
(3000000000000000584,'status',JSON_OBJECT('zh-CN','状态','en-US','Status','zh-TW','狀態','ja-JP','状態','ko-KR','상태'),'center',110,NULL,0,1,'status',1,'select','eq','notice_status',NULL,NULL,3),
(3000000000000000585,'forceRemind',JSON_OBJECT('zh-CN','强提醒','en-US','Force Remind','zh-TW','強提醒','ja-JP','強制通知','ko-KR','강조 알림'),'center',90,NULL,0,0,NULL,0,NULL,NULL,NULL,'tag',NULL,4),
(3000000000000000586,'startTime',JSON_OBJECT('zh-CN','生效时间','en-US','Start Time','zh-TW','生效時間','ja-JP','開始時間','ko-KR','시작 시간'),'center',180,NULL,0,1,'startTime',1,'dateRange','between',NULL,'date',NULL,5),
(3000000000000000587,'endTime',JSON_OBJECT('zh-CN','失效时间','en-US','End Time','zh-TW','失效時間','ja-JP','終了時間','ko-KR','종료 시간'),'center',180,NULL,0,0,NULL,0,NULL,NULL,NULL,'date',NULL,6),
(3000000000000000588,'action',JSON_OBJECT('zh-CN','操作','en-US','Action','zh-TW','操作','ja-JP','操作','ko-KR','작업'),'center',240,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99);

INSERT INTO `fx_table_column_config`
(`id`,`tenant_id`,`table_code`,`field`,`title_i18n_json`,`align`,`width`,`fixed`,`ellipsis`,`sortable`,`sorter_field`,`queryable`,`query_type`,`query_operator`,`dict_code`,`render_type`,`perm_key`,`order_num`,`enabled`,`create_by`,`create_time`,`update_by`,`update_time`,`deleted`)
SELECT v.id, 0, 'SystemNoticeTable', v.field, v.title_i18n_json, v.align, v.width, v.fixed, v.ellipsis, v.sortable, v.sorter_field,
       v.queryable, v.query_type, v.query_operator, v.dict_code, v.render_type, v.perm_key, v.order_num, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM tmp_system_notice_table_columns_20260510 v
WHERE NOT EXISTS (
  SELECT 1 FROM `fx_table_column_config` c
  WHERE c.deleted = 0 AND c.tenant_id = 0 AND c.table_code = 'SystemNoticeTable' AND c.field = v.field
);

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field IN ('customerCode','customerFullName');
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_value_level'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'customerValueLevel';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_credit_level'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'customerCreditLevel';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_business_status'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'businessStatus';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'customer_approval_status'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'approvalStatus';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'common_status'
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'status';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field IN ('supplierCode','supplierFullName');
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_cooperation_status'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'cooperationStatus';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_credit_level'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'creditLevel';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_risk_level'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'riskLevel';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_level'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'supplierLevel';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'supplier_review_status'
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'reviewStatus';

UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'input', `query_operator` = 'like'
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field IN ('materialCode','materialName','materialCategory');
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'material_type'
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'materialType';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'common_status'
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'status';
UPDATE `fx_table_column_config`
SET `queryable` = 1, `query_type` = 'select', `query_operator` = 'eq', `dict_code` = 'material_approval_status'
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'approvalStatus';
