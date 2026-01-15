-- 初始化字典数据
USE forgex_admin;

-- 1. 性别字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '性别', 'gender', '', '{"zh":"性别","en":"Gender"}', 'gender', 1, 3, 1, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'gender' AND parent_id = 0), '未知', 'unknown', '0', '{"zh":"未知","en":"Unknown"}', 'gender/unknown', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'gender' AND parent_id = 0), '男', 'male', '1', '{"zh":"男","en":"Male"}', 'gender/male', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'gender' AND parent_id = 0), '女', 'female', '2', '{"zh":"女","en":"Female"}', 'gender/female', 2, 0, 3, 1, 'system');

-- 2. 政治面貌字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '政治面貌', 'political_status', '', '{"zh":"政治面貌","en":"Political Status"}', 'political_status', 1, 5, 2, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'political_status' AND parent_id = 0), '群众', 'mass', '群众', '{"zh":"群众","en":"Mass"}', 'political_status/mass', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'political_status' AND parent_id = 0), '共青团员', 'communist_youth_league_member', '共青团员', '{"zh":"共青团员","en":"Communist Youth League Member"}', 'political_status/communist_youth_league_member', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'political_status' AND parent_id = 0), '中共党员', 'communist_party_member', '中共党员', '{"zh":"中共党员","en":"Communist Party Member"}', 'political_status/communist_party_member', 2, 0, 3, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'political_status' AND parent_id = 0), '民主党派', 'democratic_party_member', '民主党派', '{"zh":"民主党派","en":"Democratic Party Member"}', 'political_status/democratic_party_member', 2, 0, 4, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'political_status' AND parent_id = 0), '无党派人士', 'non_partisan_personage', '无党派人士', '{"zh":"无党派人士","en":"Non-partisan Personage"}', 'political_status/non_partisan_personage', 2, 0, 5, 1, 'system');

-- 3. 学历字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '学历', 'education', '', '{"zh":"学历","en":"Education"}', 'education', 1, 8, 3, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '小学', 'primary_school', '小学', '{"zh":"小学","en":"Primary School"}', 'education/primary_school', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '初中', 'junior_high_school', '初中', '{"zh":"初中","en":"Junior High School"}', 'education/junior_high_school', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '高中', 'senior_high_school', '高中', '{"zh":"高中","en":"Senior High School"}', 'education/senior_high_school', 2, 0, 3, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '中专', 'secondary_technical_school', '中专', '{"zh":"中专","en":"Secondary Technical School"}', 'education/secondary_technical_school', 2, 0, 4, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '大专', 'junior_college', '大专', '{"zh":"大专","en":"Junior College"}', 'education/junior_college', 2, 0, 5, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '本科', 'bachelor', '本科', '{"zh":"本科","en":"Bachelor"}', 'education/bachelor', 2, 0, 6, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '硕士', 'master', '硕士', '{"zh":"硕士","en":"Master"}', 'education/master', 2, 0, 7, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'education' AND parent_id = 0), '博士', 'doctor', '博士', '{"zh":"博士","en":"Doctor"}', 'education/doctor', 2, 0, 8, 1, 'system');

-- 4. 组织类型字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '组织类型', 'org_type', '', '{"zh":"组织类型","en":"Organization Type"}', 'org_type', 1, 5, 4, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_type' AND parent_id = 0), '集团', 'group', 'group', '{"zh":"集团","en":"Group"}', 'org_type/group', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_type' AND parent_id = 0), '公司', 'company', 'company', '{"zh":"公司","en":"Company"}', 'org_type/company', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_type' AND parent_id = 0), '子公司', 'subsidiary', 'subsidiary', '{"zh":"子公司","en":"Subsidiary"}', 'org_type/subsidiary', 2, 0, 3, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_type' AND parent_id = 0), '部门', 'department', 'department', '{"zh":"部门","en":"Department"}', 'org_type/department', 2, 0, 4, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_type' AND parent_id = 0), '班组', 'team', 'team', '{"zh":"班组","en":"Team"}', 'org_type/team', 2, 0, 5, 1, 'system');

-- 5. 组织层级字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '组织层级', 'org_level', '', '{"zh":"组织层级","en":"Organization Level"}', 'org_level', 1, 5, 5, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_level' AND parent_id = 0), '集团', 'level_1', '1', '{"zh":"集团","en":"Group"}', 'org_level/level_1', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_level' AND parent_id = 0), '公司', 'level_2', '2', '{"zh":"公司","en":"Company"}', 'org_level/level_2', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_level' AND parent_id = 0), '子公司', 'level_3', '3', '{"zh":"子公司","en":"Subsidiary"}', 'org_level/level_3', 2, 0, 3, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_level' AND parent_id = 0), '部门', 'level_4', '4', '{"zh":"部门","en":"Department"}', 'org_level/level_4', 2, 0, 4, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'org_level' AND parent_id = 0), '班组', 'level_5', '5', '{"zh":"班组","en":"Team"}', 'org_level/level_5', 2, 0, 5, 1, 'system');

-- 6. 菜单类型字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '菜单类型', 'menu_type', '', '{"zh":"菜单类型","en":"Menu Type"}', 'menu_type', 1, 3, 6, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'menu_type' AND parent_id = 0), '目录', 'dir', 'dir', '{"zh":"目录","en":"Directory"}', 'menu_type/dir', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'menu_type' AND parent_id = 0), '菜单', 'menu', 'menu', '{"zh":"菜单","en":"Menu"}', 'menu_type/menu', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'menu_type' AND parent_id = 0), '按钮', 'button', 'button', '{"zh":"按钮","en":"Button"}', 'menu_type/button', 2, 0, 3, 1, 'system');

-- 7. 菜单模式字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '菜单模式', 'menu_mode', '', '{"zh":"菜单模式","en":"Menu Mode"}', 'menu_mode', 1, 2, 7, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'menu_mode' AND parent_id = 0), '内嵌', 'embedded', 'embedded', '{"zh":"内嵌","en":"Embedded"}', 'menu_mode/embedded', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'menu_mode' AND parent_id = 0), '外联', 'external', 'external', '{"zh":"外联","en":"External"}', 'menu_mode/external', 2, 0, 2, 1, 'system');

-- 8. 职位级别字典
INSERT IGNORE INTO sys_dict (tenant_id, parent_id, dict_name, dict_code, dict_value, dict_value_i18n_json, node_path, level, children_count, order_num, status, create_by)
VALUES
-- 父节点
(@TENANT_ID, 0, '职位级别', 'position_level', '', '{"zh":"职位级别","en":"Position Level"}', 'position_level', 1, 5, 8, 1, 'system'),
-- 子节点
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'position_level' AND parent_id = 0), '高级管理层', 'senior', '1', '{"zh":"高级管理层","en":"Senior Management"}', 'position_level/senior', 2, 0, 1, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'position_level' AND parent_id = 0), '中层管理层', 'middle', '2', '{"zh":"中层管理层","en":"Middle Management"}', 'position_level/middle', 2, 0, 2, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'position_level' AND parent_id = 0), '基层管理层', 'junior', '3', '{"zh":"基层管理层","en":"Junior Management"}', 'position_level/junior', 2, 0, 3, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'position_level' AND parent_id = 0), '资深专业人员', 'senior_professional', '4', '{"zh":"资深专业人员","en":"Senior Professional"}', 'position_level/senior_professional', 2, 0, 4, 1, 'system'),
(@TENANT_ID, (SELECT id FROM sys_dict WHERE tenant_id = @TENANT_ID AND dict_code = 'position_level' AND parent_id = 0), '初级专业人员', 'junior_professional', '5', '{"zh":"初级专业人员","en":"Junior Professional"}', 'position_level/junior_professional', 2, 0, 5, 1, 'system');

-- 更新父节点的children_count
UPDATE sys_dict
SET children_count = (SELECT COUNT(*) FROM sys_dict AS child WHERE child.parent_id = sys_dict.id AND child.deleted = 0 AND child.tenant_id = sys_dict.tenant_id)
WHERE parent_id = 0 AND tenant_id = @TENANT_ID;

-- 更新字典表的node_path（如果有缺失）
UPDATE sys_dict AS child
JOIN sys_dict AS parent ON child.parent_id = parent.id
SET child.node_path = CONCAT(parent.node_path, '/', child.dict_code),
    child.level = parent.level + 1
WHERE child.parent_id > 0 AND child.node_path IS NULL;

SELECT '字典数据初始化完成！' AS message;