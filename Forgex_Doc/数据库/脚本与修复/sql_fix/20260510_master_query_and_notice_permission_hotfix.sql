-- Hotfix: master data query fields and system notice menu permissions.
-- Safe to rerun.

SET NAMES utf8mb4;
SET @OPERATOR = 'codex';
SET @NOW = NOW();
SET @DEFAULT_TENANT_ID = (SELECT COALESCE(MAX(tenant_id), 0) FROM forgex_admin.sys_menu WHERE id = 654 AND deleted = 0);

CREATE TABLE IF NOT EXISTS `forgex_admin`.`sys_notice` (
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

CREATE TABLE IF NOT EXISTS `forgex_admin`.`sys_notice_attachment` (
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

CREATE TABLE IF NOT EXISTS `forgex_admin`.`sys_notice_user_record` (
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

UPDATE `forgex_admin`.`sys_menu`
SET tenant_id = @DEFAULT_TENANT_ID, update_time = @NOW, update_by = @OPERATOR
WHERE id BETWEEN 3000000000000000551 AND 3000000000000000556
  AND deleted = 0;

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000551, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 654, 'menu', 'notice', '系统通知',
       JSON_OBJECT('zh-CN','系统通知','en-US','System Notice','zh-TW','系統通知','ja-JP','システム通知','ko-KR','시스템 알림'),
       'NotificationOutlined', 'SystemNotice', 'sys:notice:view', 6, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 2, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 654 AND path = 'notice'
);

UPDATE `forgex_admin`.`sys_menu`
SET parent_id = 654, path = 'notice', component_key = 'SystemNotice', perm_key = 'sys:notice:view',
    visible = 1, status = 1, update_time = @NOW, update_by = @OPERATOR
WHERE id = 3000000000000000551 AND deleted = 0;

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000552, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '查看系统通知',
       JSON_OBJECT('zh-CN','查看系统通知','en-US','View System Notice','zh-TW','查看系統通知','ja-JP','システム通知表示','ko-KR','시스템 알림 보기'),
       NULL, NULL, 'sys:notice:view', 1, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 3000000000000000551 AND perm_key = 'sys:notice:view'
);

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000553, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '新增系统通知',
       JSON_OBJECT('zh-CN','新增系统通知','en-US','Add System Notice','zh-TW','新增系統通知','ja-JP','システム通知追加','ko-KR','시스템 알림 추가'),
       NULL, NULL, 'sys:notice:add', 2, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 3000000000000000551 AND perm_key = 'sys:notice:add'
);

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000554, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '编辑系统通知',
       JSON_OBJECT('zh-CN','编辑系统通知','en-US','Edit System Notice','zh-TW','編輯系統通知','ja-JP','システム通知編集','ko-KR','시스템 알림 편집'),
       NULL, NULL, 'sys:notice:edit', 3, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 3000000000000000551 AND perm_key = 'sys:notice:edit'
);

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000555, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '删除系统通知',
       JSON_OBJECT('zh-CN','删除系统通知','en-US','Delete System Notice','zh-TW','刪除系統通知','ja-JP','システム通知削除','ko-KR','시스템 알림 삭제'),
       NULL, NULL, 'sys:notice:delete', 4, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 3000000000000000551 AND perm_key = 'sys:notice:delete'
);

INSERT INTO `forgex_admin`.`sys_menu`
(id,tenant_id,tenant_type,module_id,parent_id,type,path,name,name_i18n_json,icon,component_key,perm_key,order_num,visible,status,create_time,create_by,update_time,update_by,deleted,menu_level,menu_mode,external_url)
SELECT 3000000000000000556, @DEFAULT_TENANT_ID, 'PUBLIC', 1, 3000000000000000551, 'button', NULL, '发布/停用系统通知',
       JSON_OBJECT('zh-CN','发布/停用系统通知','en-US','Publish or Disable System Notice','zh-TW','發布/停用系統通知','ja-JP','システム通知公開/無効化','ko-KR','시스템 알림 게시/중지'),
       NULL, NULL, 'sys:notice:publish', 5, 1, 1, @NOW, @OPERATOR, @NOW, @OPERATOR, 0, 3, 'embedded', NULL
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_admin`.`sys_menu` WHERE deleted = 0 AND tenant_id = @DEFAULT_TENANT_ID AND parent_id = 3000000000000000551 AND perm_key = 'sys:notice:publish'
);

INSERT INTO `forgex_admin`.`sys_permission`
(id,permission_name,permission_key,url,method,tenant_id,create_time,update_time,deleted)
SELECT 3000000000000000561, '查看系统通知', 'sys:notice:view', '/sys/notice/page', 'POST', 0, @NOW, @NOW, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_admin`.`sys_permission` WHERE permission_key = 'sys:notice:view');

INSERT INTO `forgex_admin`.`sys_permission`
(id,permission_name,permission_key,url,method,tenant_id,create_time,update_time,deleted)
SELECT 3000000000000000562, '新增系统通知', 'sys:notice:add', '/sys/notice/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_admin`.`sys_permission` WHERE permission_key = 'sys:notice:add');

INSERT INTO `forgex_admin`.`sys_permission`
(id,permission_name,permission_key,url,method,tenant_id,create_time,update_time,deleted)
SELECT 3000000000000000563, '编辑系统通知', 'sys:notice:edit', '/sys/notice/save', 'POST', 0, @NOW, @NOW, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_admin`.`sys_permission` WHERE permission_key = 'sys:notice:edit');

INSERT INTO `forgex_admin`.`sys_permission`
(id,permission_name,permission_key,url,method,tenant_id,create_time,update_time,deleted)
SELECT 3000000000000000564, '删除系统通知', 'sys:notice:delete', '/sys/notice/delete', 'POST', 0, @NOW, @NOW, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_admin`.`sys_permission` WHERE permission_key = 'sys:notice:delete');

INSERT INTO `forgex_admin`.`sys_permission`
(id,permission_name,permission_key,url,method,tenant_id,create_time,update_time,deleted)
SELECT 3000000000000000565, '发布/停用系统通知', 'sys:notice:publish', '/sys/notice/publish', 'POST', 0, @NOW, @NOW, 0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_admin`.`sys_permission` WHERE permission_key = 'sys:notice:publish');

INSERT INTO `forgex_admin`.`sys_role_menu` (tenant_id, role_id, menu_id)
SELECT r.tenant_id, r.id, m.id
FROM `forgex_admin`.`sys_role` r
JOIN `forgex_admin`.`sys_menu` m
  ON m.tenant_id = r.tenant_id
 AND m.deleted = 0
 AND m.id BETWEEN 3000000000000000551 AND 3000000000000000556
WHERE r.deleted = 0
  AND r.role_key = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `forgex_admin`.`sys_role_menu` rm
    WHERE rm.tenant_id = r.tenant_id AND rm.role_id = r.id AND rm.menu_id = m.id
  );

INSERT INTO `forgex_admin`.`sys_role_permission` (role_id, permission_id)
SELECT r.id, p.id
FROM `forgex_admin`.`sys_role` r
JOIN `forgex_admin`.`sys_permission` p
  ON p.deleted = 0
 AND p.permission_key IN ('sys:notice:view','sys:notice:add','sys:notice:edit','sys:notice:delete','sys:notice:publish')
WHERE r.deleted = 0
  AND r.role_key = 'admin'
  AND NOT EXISTS (
    SELECT 1 FROM `forgex_admin`.`sys_role_permission` rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

INSERT INTO `forgex_common`.`fx_table_config`
(id,tenant_id,table_code,table_name_i18n_json,table_type,row_key,default_page_size,default_sort_json,enabled,version,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000581, 0, 'SystemNoticeTable',
       JSON_OBJECT('zh-CN','系统通知','en-US','System Notice','zh-TW','系統通知','ja-JP','システム通知','ko-KR','시스템 알림'),
       'COMMON', 'id', 10, JSON_OBJECT('field','orderNum','order','ascend'), 1, 1, @OPERATOR, @NOW, @OPERATOR, @NOW, 0
FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM `forgex_common`.`fx_table_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable'
);

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000582,0,'SystemNoticeTable','title',JSON_OBJECT('zh-CN','通知标题','en-US','Title','zh-TW','通知標題','ja-JP','通知タイトル','ko-KR','알림 제목'),'left',240,NULL,0,1,'title',1,'input','like',NULL,NULL,NULL,1,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'title');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000583,0,'SystemNoticeTable','scope',JSON_OBJECT('zh-CN','范围','en-US','Scope','zh-TW','範圍','ja-JP','範囲','ko-KR','범위'),'center',100,NULL,0,1,'scope',1,'select','eq','notice_scope',NULL,NULL,2,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'scope');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000584,0,'SystemNoticeTable','status',JSON_OBJECT('zh-CN','状态','en-US','Status','zh-TW','狀態','ja-JP','状態','ko-KR','상태'),'center',110,NULL,0,1,'status',1,'select','eq','notice_status',NULL,NULL,3,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'status');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000585,0,'SystemNoticeTable','forceRemind',JSON_OBJECT('zh-CN','强提醒','en-US','Force Remind','zh-TW','強提醒','ja-JP','強制通知','ko-KR','강조 알림'),'center',90,NULL,0,0,NULL,0,NULL,NULL,NULL,'tag',NULL,4,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'forceRemind');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000586,0,'SystemNoticeTable','startTime',JSON_OBJECT('zh-CN','生效时间','en-US','Start Time','zh-TW','生效時間','ja-JP','開始時間','ko-KR','시작 시간'),'center',180,NULL,0,1,'startTime',1,'dateRange','between',NULL,'date',NULL,5,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'startTime');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000587,0,'SystemNoticeTable','endTime',JSON_OBJECT('zh-CN','失效时间','en-US','End Time','zh-TW','失效時間','ja-JP','終了時間','ko-KR','종료 시간'),'center',180,NULL,0,0,NULL,0,NULL,NULL,NULL,'date',NULL,6,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'endTime');

INSERT INTO `forgex_common`.`fx_table_column_config`
(id,tenant_id,table_code,field,title_i18n_json,align,width,fixed,ellipsis,sortable,sorter_field,queryable,query_type,query_operator,dict_code,render_type,perm_key,order_num,enabled,create_by,create_time,update_by,update_time,deleted)
SELECT 3000000000000000588,0,'SystemNoticeTable','action',JSON_OBJECT('zh-CN','操作','en-US','Action','zh-TW','操作','ja-JP','操作','ko-KR','작업'),'center',240,'right',0,0,NULL,0,NULL,NULL,NULL,NULL,NULL,99,1,@OPERATOR,@NOW,@OPERATOR,@NOW,0
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `forgex_common`.`fx_table_column_config` WHERE deleted = 0 AND tenant_id = 0 AND table_code = 'SystemNoticeTable' AND field = 'action');

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'input', query_operator = 'like', dict_code = NULL, update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field IN ('customerCode','customerFullName');

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'customer_value_level', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'customerValueLevel';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'customer_credit_level', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'customerCreditLevel';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'customer_business_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'businessStatus';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'customer_approval_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'approvalStatus';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'common_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'CustomerMasterTable' AND field = 'status';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'input', query_operator = 'like', dict_code = NULL, update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field IN ('supplierCode','supplierFullName');

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'supplier_cooperation_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'cooperationStatus';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'supplier_credit_level', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'creditLevel';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'supplier_risk_level', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'riskLevel';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'supplier_level', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'supplierLevel';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'supplier_review_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'SupplierMasterTable' AND field = 'reviewStatus';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'input', query_operator = 'like', dict_code = NULL, update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field IN ('materialCode','materialName','materialCategory');

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'material_type', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'materialType';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'common_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'status';

UPDATE `forgex_common`.`fx_table_column_config`
SET queryable = 1, query_type = 'select', query_operator = 'eq', dict_code = 'material_approval_status', update_time = @NOW, update_by = @OPERATOR
WHERE deleted = 0 AND table_code = 'MaterialTable' AND field = 'approvalStatus';

