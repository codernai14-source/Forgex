-- ============================================
-- 模板绑定数据字典配置脚本
-- 目标数据库：forgex_admin
-- 表：sys_dict
-- 用途：配置绑定类型和优先级的字典数据
-- ============================================

-- 1. 绑定类型字典（binding_type）
-- 检查绑定类型字典是否存在
SET @binding_type_dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'binding_type' AND parent_id = 0 LIMIT 1);

-- 如果不存在，创建绑定类型字典根节点
INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, 0, '绑定类型', 'binding_type', NULL, 10, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'binding_type' AND parent_id = 0);

-- 获取绑定类型字典ID
SET @binding_type_dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'binding_type' AND parent_id = 0 LIMIT 1);

-- 插入绑定类型字典项
INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @binding_type_dict_id, '按物料', NULL, 'MATERIAL', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @binding_type_dict_id AND dict_value = 'MATERIAL');

INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @binding_type_dict_id, '按供应商', NULL, 'SUPPLIER', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @binding_type_dict_id AND dict_value = 'SUPPLIER');

INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @binding_type_dict_id, '按客户', NULL, 'CUSTOMER', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @binding_type_dict_id AND dict_value = 'CUSTOMER');

-- 2. 优先级字典（priority）
-- 检查优先级字典是否存在
SET @priority_dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'priority' AND parent_id = 0 LIMIT 1);

-- 如果不存在，创建优先级字典根节点
INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, 0, '优先级', 'priority', NULL, 20, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE dict_code = 'priority' AND parent_id = 0);

-- 获取优先级字典ID
SET @priority_dict_id = (SELECT id FROM sys_dict WHERE dict_code = 'priority' AND parent_id = 0 LIMIT 1);

-- 插入优先级字典项
INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @priority_dict_id, '高', NULL, '1', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @priority_dict_id AND dict_value = '1');

INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @priority_dict_id, '中', NULL, '2', 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @priority_dict_id AND dict_value = '2');

INSERT INTO sys_dict (tenant_id, create_time, create_by, update_time, update_by, deleted, parent_id, dict_name, dict_code, dict_value, order_num, status)
SELECT 0, NOW(), 'system', NOW(), 'system', false, @priority_dict_id, '低', NULL, '3', 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_dict WHERE parent_id = @priority_dict_id AND dict_value = '3');

SELECT '模板绑定数据字典配置完成！' AS result;
