SET NAMES utf8mb4;
USE forgex_integration;

SET @sync_config_id := (SELECT id FROM fx_api_config WHERE api_code = 'sys_user_sync' AND deleted = 0 LIMIT 1);
SET @pull_config_id := (SELECT id FROM fx_api_config WHERE api_code = 'sys_user_pull' AND deleted = 0 LIMIT 1);
SET @inbound_config_id := (SELECT id FROM fx_api_config WHERE api_code = 'sys_user_third_party_inbound' AND deleted = 0 LIMIT 1);
SET @sync_target_id := (SELECT id FROM fx_api_outbound_target WHERE api_config_id = @sync_config_id AND deleted = 0 LIMIT 1);
SET @pull_target_id := (SELECT id FROM fx_api_outbound_target WHERE api_config_id = @pull_config_id AND deleted = 0 LIMIT 1);
SET @tenant_id := COALESCE((SELECT tenant_id FROM fx_api_config WHERE id = @sync_config_id LIMIT 1), 0);
SET @third_system_id := (SELECT id FROM fx_third_system WHERE system_code = 'cs' AND deleted = 0 LIMIT 1);

DELETE FROM fx_api_param_config WHERE api_config_id IN (@sync_config_id, @pull_config_id, @inbound_config_id);
DELETE FROM fx_api_param_mapping WHERE api_config_id IN (@sync_config_id, @pull_config_id, @inbound_config_id);

UPDATE fx_api_config
SET api_name = '用户同步第三方',
    api_desc = '将 Forgex 用户字段按字段级映射转换为 ERP commonUsers 结构后同步。',
    module_code = 'sys'
WHERE id = @sync_config_id;
UPDATE fx_api_config
SET api_name = '用户拉取第三方',
    api_desc = '从 ERP commonUsers 结构拉取用户，并按字段级映射转换回 Forgex users 结构。',
    module_code = 'sys'
WHERE id = @pull_config_id;
UPDATE fx_api_config
SET api_name = '第三方推送用户',
    api_desc = '接收第三方 commonUsers 结构并按字段级映射写入 Forgex 用户。',
    module_code = 'sys',
    auth_type = 'TOKEN',
    auth_config = JSON_OBJECT('thirdSystemId', @third_system_id)
WHERE id = @inbound_config_id;

UPDATE fx_third_system
SET system_name = '本地ERP测试系统',
    ip_address = '192.168.3.7:18080'
WHERE system_code = 'cs' AND deleted = 0;
UPDATE fx_api_outbound_target
SET target_name = '本地ERP测试系统', target_url = '/api/users/third-party/sync', http_method = 'POST', content_type = 'application/json', invoke_mode = 'SYNC', remark = 'Forgex 用户字段级映射同步到 ERP commonUsers'
WHERE id = @sync_target_id;
UPDATE fx_api_outbound_target
SET target_name = '本地ERP测试系统', target_url = '/api/users/third-party/pull', http_method = 'POST', content_type = 'application/json', invoke_mode = 'SYNC', remark = '从 ERP commonUsers 拉取并字段级映射回 Forgex users'
WHERE id = @pull_target_id;

-- 用户同步第三方：左侧 REQUEST = ERP 目标请求结构，右侧 RESPONSE = Forgex 来源结构
INSERT INTO fx_api_param_config (id, api_config_id, outbound_target_id, parent_id, direction, node_type, field_name, field_label, field_type, field_path, required, order_num, remark, tenant_id, create_by, update_by, deleted) VALUES
(2045910000000000001,@sync_config_id,@sync_target_id,NULL,'REQUEST','OBJECT','root','ERP同步请求根节点','object','root',0,0,'第三方 ERP /api/users/third-party/sync 请求体',@tenant_id,'system','system',0),
(2045910000000000002,@sync_config_id,@sync_target_id,2045910000000000001,'REQUEST','FIELD','companyId','公司ID','number','root.companyId',1,1,'ERP 租户字段',@tenant_id,'system','system',0),
(2045910000000000003,@sync_config_id,@sync_target_id,2045910000000000001,'REQUEST','FIELD','batchNo','批次号','string','root.batchNo',0,2,'ERP 批次字段，可配置常量/默认值',@tenant_id,'system','system',0),
(2045910000000000004,@sync_config_id,@sync_target_id,2045910000000000001,'REQUEST','ARRAY','commonUsers','ERP用户集合','array','root.commonUsers',1,3,'对方接口用户集合，不叫 users',@tenant_id,'system','system',0),
(2045910000000000005,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','code','ERP账号编码','string','root.commonUsers.code',1,1,'对方账号字段，不叫 account',@tenant_id,'system','system',0),
(2045910000000000006,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','name','ERP姓名','string','root.commonUsers.name',1,2,'对方名称字段，不叫 username',@tenant_id,'system','system',0),
(2045910000000000007,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','mail','ERP邮箱','string','root.commonUsers.mail',0,3,'对方邮箱字段',@tenant_id,'system','system',0),
(2045910000000000008,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','mobile','ERP手机','string','root.commonUsers.mobile',0,4,'对方手机字段',@tenant_id,'system','system',0),
(2045910000000000009,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','enable','ERP启用标识','boolean','root.commonUsers.enable',0,5,'对方启用字段',@tenant_id,'system','system',0),
(2045910000000000010,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','sex','ERP性别','number','root.commonUsers.sex',0,6,'对方性别字段',@tenant_id,'system','system',0),
(2045910000000000011,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','orgId','ERP组织ID','number','root.commonUsers.orgId',0,7,'对方组织字段',@tenant_id,'system','system',0),
(2045910000000000012,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','jobId','ERP岗位ID','number','root.commonUsers.jobId',0,8,'对方岗位字段',@tenant_id,'system','system',0),
(2045910000000000013,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','staffId','ERP员工ID','number','root.commonUsers.staffId',0,9,'对方员工字段',@tenant_id,'system','system',0),
(2045910000000000014,@sync_config_id,@sync_target_id,2045910000000000004,'REQUEST','FIELD','sourceType','ERP来源类型','number','root.commonUsers.sourceType',0,10,'对方来源字段',@tenant_id,'system','system',0),
(2045910000000000021,@sync_config_id,@sync_target_id,NULL,'RESPONSE','OBJECT','root','Forgex同步来源根节点','object','root',0,0,'Forgex 导出的用户源数据',@tenant_id,'system','system',0),
(2045910000000000022,@sync_config_id,@sync_target_id,2045910000000000021,'RESPONSE','FIELD','tenantId','租户ID','number','root.tenantId',1,1,'Forgex 源字段',@tenant_id,'system','system',0),
(2045910000000000023,@sync_config_id,@sync_target_id,2045910000000000021,'RESPONSE','ARRAY','users','用户集合','array','root.users',1,2,'Forgex 用户集合',@tenant_id,'system','system',0),
(2045910000000000024,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','account','账号','string','root.users.account',1,1,'Forgex 账号',@tenant_id,'system','system',0),
(2045910000000000025,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','username','用户名','string','root.users.username',1,2,'Forgex 用户名',@tenant_id,'system','system',0),
(2045910000000000026,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','email','邮箱','string','root.users.email',0,3,'Forgex 邮箱',@tenant_id,'system','system',0),
(2045910000000000027,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','phone','手机号','string','root.users.phone',0,4,'Forgex 手机号',@tenant_id,'system','system',0),
(2045910000000000028,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','status','启用状态','boolean','root.users.status',0,5,'Forgex 状态',@tenant_id,'system','system',0),
(2045910000000000029,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','gender','性别','number','root.users.gender',0,6,'Forgex 性别',@tenant_id,'system','system',0),
(2045910000000000030,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','departmentId','部门ID','number','root.users.departmentId',0,7,'Forgex 部门',@tenant_id,'system','system',0),
(2045910000000000031,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','positionId','岗位ID','number','root.users.positionId',0,8,'Forgex 岗位',@tenant_id,'system','system',0),
(2045910000000000032,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','employeeId','员工ID','number','root.users.employeeId',0,9,'Forgex 员工',@tenant_id,'system','system',0),
(2045910000000000033,@sync_config_id,@sync_target_id,2045910000000000023,'RESPONSE','FIELD','userSource','用户来源','number','root.users.userSource',0,10,'Forgex 来源',@tenant_id,'system','system',0);

-- 用户拉取第三方：左侧 REQUEST = Forgex 目标结构，右侧 RESPONSE = ERP 拉取请求/响应来源结构
INSERT INTO fx_api_param_config (id, api_config_id, outbound_target_id, parent_id, direction, node_type, field_name, field_label, field_type, field_path, required, order_num, remark, tenant_id, create_by, update_by, deleted) VALUES
(2045910000000000101,@pull_config_id,@pull_target_id,NULL,'REQUEST','OBJECT','root','Forgex拉取目标根节点','object','root',0,0,'映射回 Forgex 的目标结构',@tenant_id,'system','system',0),
(2045910000000000102,@pull_config_id,@pull_target_id,2045910000000000101,'REQUEST','ARRAY','users','用户集合','array','root.users',1,1,'Forgex 用户集合',@tenant_id,'system','system',0),
(2045910000000000103,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','account','账号','string','root.users.account',1,1,'Forgex 账号',@tenant_id,'system','system',0),
(2045910000000000104,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','username','用户名','string','root.users.username',1,2,'Forgex 用户名',@tenant_id,'system','system',0),
(2045910000000000105,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','email','邮箱','string','root.users.email',0,3,'Forgex 邮箱',@tenant_id,'system','system',0),
(2045910000000000106,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','phone','手机号','string','root.users.phone',0,4,'Forgex 手机',@tenant_id,'system','system',0),
(2045910000000000107,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','status','启用状态','boolean','root.users.status',0,5,'Forgex 状态',@tenant_id,'system','system',0),
(2045910000000000108,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','gender','性别','number','root.users.gender',0,6,'Forgex 性别',@tenant_id,'system','system',0),
(2045910000000000109,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','departmentId','部门ID','number','root.users.departmentId',0,7,'Forgex 部门',@tenant_id,'system','system',0),
(2045910000000000110,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','positionId','岗位ID','number','root.users.positionId',0,8,'Forgex 岗位',@tenant_id,'system','system',0),
(2045910000000000111,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','employeeId','员工ID','number','root.users.employeeId',0,9,'Forgex 员工',@tenant_id,'system','system',0),
(2045910000000000112,@pull_config_id,@pull_target_id,2045910000000000102,'REQUEST','FIELD','userSource','用户来源','number','root.users.userSource',0,10,'Forgex 来源',@tenant_id,'system','system',0),
(2045910000000000121,@pull_config_id,@pull_target_id,NULL,'RESPONSE','OBJECT','root','ERP拉取来源根节点','object','root',0,0,'ERP 拉取请求和响应结构',@tenant_id,'system','system',0),
(2045910000000000122,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','FIELD','tenantId','租户ID','number','root.tenantId',1,1,'Forgex 拉取请求源字段',@tenant_id,'system','system',0),
(2045910000000000137,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','FIELD','includeDisabled','包含禁用','boolean','root.includeDisabled',0,2,'Forgex 拉取请求源字段',@tenant_id,'system','system',0),
(2045910000000000123,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','FIELD','companyId','公司ID','number','root.companyId',1,3,'ERP 拉取请求字段',@tenant_id,'system','system',0),
(2045910000000000124,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','FIELD','includeStop','包含停用','boolean','root.includeStop',0,4,'ERP 拉取请求字段',@tenant_id,'system','system',0),
(2045910000000000125,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','FIELD','total','ERP总数','number','root.total',0,5,'ERP 响应字段',@tenant_id,'system','system',0),
(2045910000000000126,@pull_config_id,@pull_target_id,2045910000000000121,'RESPONSE','ARRAY','commonUsers','ERP用户集合','array','root.commonUsers',1,6,'ERP 响应集合，不叫 users',@tenant_id,'system','system',0),
(2045910000000000127,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','code','ERP账号编码','string','root.commonUsers.code',1,1,'对方账号字段',@tenant_id,'system','system',0),
(2045910000000000128,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','name','ERP姓名','string','root.commonUsers.name',1,2,'对方名称字段',@tenant_id,'system','system',0),
(2045910000000000129,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','mail','ERP邮箱','string','root.commonUsers.mail',0,3,'对方邮箱字段',@tenant_id,'system','system',0),
(2045910000000000130,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','mobile','ERP手机','string','root.commonUsers.mobile',0,4,'对方手机字段',@tenant_id,'system','system',0),
(2045910000000000131,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','enable','ERP启用标识','boolean','root.commonUsers.enable',0,5,'对方启用字段',@tenant_id,'system','system',0),
(2045910000000000132,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','sex','ERP性别','number','root.commonUsers.sex',0,6,'对方性别字段',@tenant_id,'system','system',0),
(2045910000000000133,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','orgId','ERP组织ID','number','root.commonUsers.orgId',0,7,'对方组织字段',@tenant_id,'system','system',0),
(2045910000000000134,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','jobId','ERP岗位ID','number','root.commonUsers.jobId',0,8,'对方岗位字段',@tenant_id,'system','system',0),
(2045910000000000135,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','staffId','ERP员工ID','number','root.commonUsers.staffId',0,9,'对方员工字段',@tenant_id,'system','system',0),
(2045910000000000136,@pull_config_id,@pull_target_id,2045910000000000126,'RESPONSE','FIELD','sourceType','ERP来源类型','number','root.commonUsers.sourceType',0,10,'对方来源字段',@tenant_id,'system','system',0);

-- 第三方推送用户：左侧 REQUEST = 第三方 commonUsers，右侧 RESPONSE = Forgex 入站处理结构
INSERT INTO fx_api_param_config (id, api_config_id, outbound_target_id, parent_id, direction, node_type, field_name, field_label, field_type, field_path, required, order_num, remark, tenant_id, create_by, update_by, deleted) VALUES
(2045910000000000201,@inbound_config_id,NULL,NULL,'REQUEST','OBJECT','root','第三方推送请求根节点','object','root',0,0,'第三方推送 commonUsers 请求体',@tenant_id,'system','system',0),
(2045910000000000202,@inbound_config_id,NULL,2045910000000000201,'REQUEST','FIELD','companyId','公司ID','number','root.companyId',1,1,'第三方租户字段',@tenant_id,'system','system',0),
(2045910000000000203,@inbound_config_id,NULL,2045910000000000201,'REQUEST','ARRAY','commonUsers','第三方用户集合','array','root.commonUsers',1,2,'第三方用户集合，不叫 users',@tenant_id,'system','system',0),
(2045910000000000204,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','code','第三方账号编码','string','root.commonUsers.code',1,1,'第三方账号字段',@tenant_id,'system','system',0),
(2045910000000000205,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','name','第三方姓名','string','root.commonUsers.name',1,2,'第三方姓名字段',@tenant_id,'system','system',0),
(2045910000000000206,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','mail','第三方邮箱','string','root.commonUsers.mail',0,3,'第三方邮箱字段',@tenant_id,'system','system',0),
(2045910000000000207,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','mobile','第三方手机','string','root.commonUsers.mobile',0,4,'第三方手机字段',@tenant_id,'system','system',0),
(2045910000000000208,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','enable','第三方启用标识','boolean','root.commonUsers.enable',0,5,'第三方启用字段',@tenant_id,'system','system',0),
(2045910000000000209,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','sex','第三方性别','number','root.commonUsers.sex',0,6,'第三方性别字段',@tenant_id,'system','system',0),
(2045910000000000210,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','orgId','第三方组织ID','number','root.commonUsers.orgId',0,7,'第三方组织字段',@tenant_id,'system','system',0),
(2045910000000000211,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','jobId','第三方岗位ID','number','root.commonUsers.jobId',0,8,'第三方岗位字段',@tenant_id,'system','system',0),
(2045910000000000212,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','staffId','第三方员工ID','number','root.commonUsers.staffId',0,9,'第三方员工字段',@tenant_id,'system','system',0),
(2045910000000000213,@inbound_config_id,NULL,2045910000000000203,'REQUEST','FIELD','sourceType','第三方来源类型','number','root.commonUsers.sourceType',0,10,'第三方来源字段',@tenant_id,'system','system',0),
(2045910000000000221,@inbound_config_id,NULL,NULL,'RESPONSE','OBJECT','root','Forgex入站目标根节点','object','root',0,0,'映射给入站处理器的 Forgex 结构',@tenant_id,'system','system',0),
(2045910000000000222,@inbound_config_id,NULL,2045910000000000221,'RESPONSE','FIELD','tenantId','租户ID','number','root.tenantId',1,1,'Forgex 入站目标字段',@tenant_id,'system','system',0),
(2045910000000000223,@inbound_config_id,NULL,2045910000000000221,'RESPONSE','ARRAY','users','用户集合','array','root.users',1,2,'Forgex 入站目标用户集合',@tenant_id,'system','system',0),
(2045910000000000224,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','account','账号','string','root.users.account',1,1,'Forgex 账号',@tenant_id,'system','system',0),
(2045910000000000225,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','username','用户名','string','root.users.username',1,2,'Forgex 用户名',@tenant_id,'system','system',0),
(2045910000000000226,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','email','邮箱','string','root.users.email',0,3,'Forgex 邮箱',@tenant_id,'system','system',0),
(2045910000000000227,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','phone','手机号','string','root.users.phone',0,4,'Forgex 手机',@tenant_id,'system','system',0),
(2045910000000000228,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','status','启用状态','boolean','root.users.status',0,5,'Forgex 状态',@tenant_id,'system','system',0),
(2045910000000000229,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','gender','性别','number','root.users.gender',0,6,'Forgex 性别',@tenant_id,'system','system',0),
(2045910000000000230,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','departmentId','部门ID','number','root.users.departmentId',0,7,'Forgex 部门',@tenant_id,'system','system',0),
(2045910000000000231,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','positionId','岗位ID','number','root.users.positionId',0,8,'Forgex 岗位',@tenant_id,'system','system',0),
(2045910000000000232,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','employeeId','员工ID','number','root.users.employeeId',0,9,'Forgex 员工',@tenant_id,'system','system',0),
(2045910000000000233,@inbound_config_id,NULL,2045910000000000223,'RESPONSE','FIELD','userSource','用户来源','number','root.users.userSource',0,10,'Forgex 来源',@tenant_id,'system','system',0);

-- 字段级映射：同步 Forgex -> ERP commonUsers
INSERT INTO fx_api_param_mapping (id, api_config_id, outbound_target_id, source_field_path, target_field_path, target_scope, value_type, direction, remark, tenant_id, create_by, update_by, deleted) VALUES
(2045910000000001001,@sync_config_id,@sync_target_id,'root.tenantId','root.companyId','BODY','SOURCE','OUTBOUND','租户ID -> 公司ID',@tenant_id,'system','system',0),
(2045910000000001002,@sync_config_id,@sync_target_id,'root.users.account','root.commonUsers.code','BODY','SOURCE','OUTBOUND','账号 -> 对方 code',@tenant_id,'system','system',0),
(2045910000000001003,@sync_config_id,@sync_target_id,'root.users.username','root.commonUsers.name','BODY','SOURCE','OUTBOUND','用户名 -> 对方 name',@tenant_id,'system','system',0),
(2045910000000001004,@sync_config_id,@sync_target_id,'root.users.email','root.commonUsers.mail','BODY','SOURCE','OUTBOUND','邮箱 -> 对方 mail',@tenant_id,'system','system',0),
(2045910000000001005,@sync_config_id,@sync_target_id,'root.users.phone','root.commonUsers.mobile','BODY','SOURCE','OUTBOUND','手机号 -> 对方 mobile',@tenant_id,'system','system',0),
(2045910000000001006,@sync_config_id,@sync_target_id,'root.users.status','root.commonUsers.enable','BODY','SOURCE','OUTBOUND','状态 -> 对方 enable',@tenant_id,'system','system',0),
(2045910000000001007,@sync_config_id,@sync_target_id,'root.users.gender','root.commonUsers.sex','BODY','SOURCE','OUTBOUND','性别 -> 对方 sex',@tenant_id,'system','system',0),
(2045910000000001008,@sync_config_id,@sync_target_id,'root.users.departmentId','root.commonUsers.orgId','BODY','SOURCE','OUTBOUND','部门 -> 对方 orgId',@tenant_id,'system','system',0),
(2045910000000001009,@sync_config_id,@sync_target_id,'root.users.positionId','root.commonUsers.jobId','BODY','SOURCE','OUTBOUND','岗位 -> 对方 jobId',@tenant_id,'system','system',0),
(2045910000000001010,@sync_config_id,@sync_target_id,'root.users.employeeId','root.commonUsers.staffId','BODY','SOURCE','OUTBOUND','员工 -> 对方 staffId',@tenant_id,'system','system',0),
(2045910000000001011,@sync_config_id,@sync_target_id,'root.users.userSource','root.commonUsers.sourceType','BODY','SOURCE','OUTBOUND','来源 -> 对方 sourceType',@tenant_id,'system','system',0),
-- 拉取请求 Forgex -> ERP pull request
(2045910000000001012,@pull_config_id,@pull_target_id,'root.tenantId','root.companyId','BODY','SOURCE','OUTBOUND','租户ID -> 公司ID',@tenant_id,'system','system',0),
(2045910000000001013,@pull_config_id,@pull_target_id,'root.includeDisabled','root.includeStop','BODY','SOURCE','OUTBOUND','包含禁用 -> 包含停用',@tenant_id,'system','system',0),
-- 拉取响应 ERP commonUsers -> Forgex users
(2045910000000001101,@pull_config_id,@pull_target_id,'root.commonUsers.code','root.users.account','BODY','SOURCE','INBOUND','对方 code -> 账号',@tenant_id,'system','system',0),
(2045910000000001102,@pull_config_id,@pull_target_id,'root.commonUsers.name','root.users.username','BODY','SOURCE','INBOUND','对方 name -> 用户名',@tenant_id,'system','system',0),
(2045910000000001103,@pull_config_id,@pull_target_id,'root.commonUsers.mail','root.users.email','BODY','SOURCE','INBOUND','对方 mail -> 邮箱',@tenant_id,'system','system',0),
(2045910000000001104,@pull_config_id,@pull_target_id,'root.commonUsers.mobile','root.users.phone','BODY','SOURCE','INBOUND','对方 mobile -> 手机号',@tenant_id,'system','system',0),
(2045910000000001105,@pull_config_id,@pull_target_id,'root.commonUsers.enable','root.users.status','BODY','SOURCE','INBOUND','对方 enable -> 状态',@tenant_id,'system','system',0),
(2045910000000001106,@pull_config_id,@pull_target_id,'root.commonUsers.sex','root.users.gender','BODY','SOURCE','INBOUND','对方 sex -> 性别',@tenant_id,'system','system',0),
(2045910000000001107,@pull_config_id,@pull_target_id,'root.commonUsers.orgId','root.users.departmentId','BODY','SOURCE','INBOUND','对方 orgId -> 部门',@tenant_id,'system','system',0),
(2045910000000001108,@pull_config_id,@pull_target_id,'root.commonUsers.jobId','root.users.positionId','BODY','SOURCE','INBOUND','对方 jobId -> 岗位',@tenant_id,'system','system',0),
(2045910000000001109,@pull_config_id,@pull_target_id,'root.commonUsers.staffId','root.users.employeeId','BODY','SOURCE','INBOUND','对方 staffId -> 员工',@tenant_id,'system','system',0),
(2045910000000001110,@pull_config_id,@pull_target_id,'root.commonUsers.sourceType','root.users.userSource','BODY','SOURCE','INBOUND','对方 sourceType -> 来源',@tenant_id,'system','system',0),
-- 入站推送 ERP commonUsers -> Forgex users
(2045910000000001201,@inbound_config_id,NULL,'root.companyId','root.tenantId','BODY','SOURCE','INBOUND','公司ID -> 租户ID',@tenant_id,'system','system',0),
(2045910000000001202,@inbound_config_id,NULL,'root.commonUsers.code','root.users.account','BODY','SOURCE','INBOUND','对方 code -> 账号',@tenant_id,'system','system',0),
(2045910000000001203,@inbound_config_id,NULL,'root.commonUsers.name','root.users.username','BODY','SOURCE','INBOUND','对方 name -> 用户名',@tenant_id,'system','system',0),
(2045910000000001204,@inbound_config_id,NULL,'root.commonUsers.mail','root.users.email','BODY','SOURCE','INBOUND','对方 mail -> 邮箱',@tenant_id,'system','system',0),
(2045910000000001205,@inbound_config_id,NULL,'root.commonUsers.mobile','root.users.phone','BODY','SOURCE','INBOUND','对方 mobile -> 手机号',@tenant_id,'system','system',0),
(2045910000000001206,@inbound_config_id,NULL,'root.commonUsers.enable','root.users.status','BODY','SOURCE','INBOUND','对方 enable -> 状态',@tenant_id,'system','system',0),
(2045910000000001207,@inbound_config_id,NULL,'root.commonUsers.sex','root.users.gender','BODY','SOURCE','INBOUND','对方 sex -> 性别',@tenant_id,'system','system',0),
(2045910000000001208,@inbound_config_id,NULL,'root.commonUsers.orgId','root.users.departmentId','BODY','SOURCE','INBOUND','对方 orgId -> 部门',@tenant_id,'system','system',0),
(2045910000000001209,@inbound_config_id,NULL,'root.commonUsers.jobId','root.users.positionId','BODY','SOURCE','INBOUND','对方 jobId -> 岗位',@tenant_id,'system','system',0),
(2045910000000001210,@inbound_config_id,NULL,'root.commonUsers.staffId','root.users.employeeId','BODY','SOURCE','INBOUND','对方 staffId -> 员工',@tenant_id,'system','system',0),
(2045910000000001211,@inbound_config_id,NULL,'root.commonUsers.sourceType','root.users.userSource','BODY','SOURCE','INBOUND','对方 sourceType -> 来源',@tenant_id,'system','system',0);

SELECT c.api_code,c.api_name,c.api_desc FROM fx_api_config c WHERE c.api_code IN ('sys_user_sync','sys_user_pull','sys_user_third_party_inbound');
SELECT c.api_code,p.outbound_target_id,p.direction,COUNT(*) AS param_count FROM fx_api_config c JOIN fx_api_param_config p ON p.api_config_id=c.id AND p.deleted=0 WHERE c.api_code IN ('sys_user_sync','sys_user_pull','sys_user_third_party_inbound') GROUP BY c.api_code,p.outbound_target_id,p.direction ORDER BY c.api_code,p.direction;
SELECT c.api_code,m.outbound_target_id,m.direction,COUNT(*) AS mapping_count FROM fx_api_config c JOIN fx_api_param_mapping m ON m.api_config_id=c.id AND m.deleted=0 WHERE c.api_code IN ('sys_user_sync','sys_user_pull','sys_user_third_party_inbound') GROUP BY c.api_code,m.outbound_target_id,m.direction ORDER BY c.api_code,m.direction;
