-- ================================================
-- 配置返回消息表和数据表配置跳过租户隔离
-- ================================================

USE forgex_common;

-- ================================================
-- 配置返回消息表跳过租户隔离
-- ================================================
INSERT INTO sys_tenant_ignore (scope, matcher, enabled, remark, create_by, create_time, update_time, deleted) VALUES
('TABLE', 'fx_i18n_message', 1, '返回消息国际化配置表跳过租户隔离', 'system', NOW(), NOW(), 0);

-- ================================================
-- 配置数据表配置跳过租户隔离
-- ================================================
INSERT INTO sys_tenant_ignore (scope, matcher, enabled, remark, create_by, create_time, update_time, deleted) VALUES
('TABLE', 'fx_table_config', 1, '通用表格配置主表跳过租户隔离', 'system', NOW(), NOW(), 0),
('TABLE', 'fx_table_column_config', 1, '通用表格列配置子表跳过租户隔离', 'system', NOW(), NOW(), 0);

SELECT '租户隔离跳过配置完成！' AS message;
