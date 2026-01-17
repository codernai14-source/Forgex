INSERT INTO fx_table_config
  (tenant_id, table_code, table_name_i18n_json, table_type, row_key, default_page_size, enabled, version, deleted)
VALUES
  (0, 'UserTable', '{"zh-CN":"用户管理","en-US":"User Management"}', 'NORMAL', 'id', 20, 1, 1, 0)
ON DUPLICATE KEY UPDATE
  table_name_i18n_json = VALUES(table_name_i18n_json),
  table_type = VALUES(table_type),
  row_key = VALUES(row_key),
  default_page_size = VALUES(default_page_size),
  enabled = VALUES(enabled),
  version = VALUES(version),
  deleted = VALUES(deleted);

DELETE FROM fx_table_column_config WHERE tenant_id = 0 AND table_code = 'UserTable';

INSERT INTO fx_table_column_config
  (tenant_id, table_code, field, title_i18n_json, align, width, fixed, ellipsis, sortable, sorter_field, queryable, query_type, query_operator, dict_code, render_type, perm_key, order_num, enabled, deleted)
VALUES
  (0, 'UserTable', 'avatar', '{"zh-CN":"头像","en-US":"Avatar"}', 'center', 80, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 10, 1, 0),
  (0, 'UserTable', 'username', '{"zh-CN":"用户名","en-US":"Username"}', 'left', 120, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 20, 1, 0),
  (0, 'UserTable', 'email', '{"zh-CN":"邮箱","en-US":"Email"}', 'left', 180, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 30, 1, 0),
  (0, 'UserTable', 'phone', '{"zh-CN":"手机号","en-US":"Phone"}', 'left', 130, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 40, 1, 0),
  (0, 'UserTable', 'gender', '{"zh-CN":"性别","en-US":"Gender"}', 'left', 80, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 50, 1, 0),
  (0, 'UserTable', 'departmentName', '{"zh-CN":"部门","en-US":"Department"}', 'left', 120, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 60, 1, 0),
  (0, 'UserTable', 'positionName', '{"zh-CN":"岗位","en-US":"Position"}', 'left', 120, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 70, 1, 0),
  (0, 'UserTable', 'entryDate', '{"zh-CN":"入职日期","en-US":"Entry Date"}', 'left', 120, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 80, 1, 0),
  (0, 'UserTable', 'status', '{"zh-CN":"状态","en-US":"Status"}', 'left', 80, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 90, 1, 0),
  (0, 'UserTable', 'lastLoginTime', '{"zh-CN":"最后登录时间","en-US":"Last Login Time"}', 'left', 180, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 100, 1, 0),
  (0, 'UserTable', 'lastLoginIp', '{"zh-CN":"最后登录IP","en-US":"Last Login IP"}', 'left', 150, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 110, 1, 0),
  (0, 'UserTable', 'lastLoginRegion', '{"zh-CN":"最后登录地区","en-US":"Last Login Region"}', 'left', 150, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 120, 1, 0),
  (0, 'UserTable', 'createTime', '{"zh-CN":"创建时间","en-US":"Created Time"}', 'left', 180, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 130, 1, 0),
  (0, 'UserTable', 'createBy', '{"zh-CN":"创建人","en-US":"Created By"}', 'left', 100, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 140, 1, 0),
  (0, 'UserTable', 'updateTime', '{"zh-CN":"更新时间","en-US":"Updated Time"}', 'left', 180, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 150, 1, 0),
  (0, 'UserTable', 'updateBy', '{"zh-CN":"更新人","en-US":"Updated By"}', 'left', 100, NULL, 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 160, 1, 0),
  (0, 'UserTable', 'action', '{"zh-CN":"操作","en-US":"Actions"}', 'left', 260, 'right', 0, 0, NULL, 0, NULL, NULL, NULL, NULL, NULL, 170, 1, 0);
