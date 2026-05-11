SET NAMES utf8mb4;

USE forgex_integration;

UPDATE fx_api_config
SET api_name = '用户同步第三方',
    api_desc = '将 Forgex 当前租户用户同步到本地 ERP 测试系统。'
WHERE api_code = 'sys_user_sync';

UPDATE fx_api_config
SET api_name = '用户拉取第三方',
    api_desc = '从本地 ERP 测试系统拉取用户并同步到 Forgex 当前租户。'
WHERE api_code = 'sys_user_pull';

UPDATE fx_api_config
SET api_name = '第三方推送用户',
    api_desc = '接收第三方用户数据并同步写入 Forgex。'
WHERE api_code = 'sys_user_third_party_inbound';

UPDATE fx_third_system
SET system_name = '本地ERP测试系统',
    ip_address = '192.168.3.7:18080'
WHERE system_code = 'cs';

UPDATE fx_api_outbound_target
SET target_name = '本地ERP测试系统',
    target_url = '/api/users/third-party/sync',
    http_method = 'POST',
    remark = 'Forgex 用户同步到本地 ERP 测试系统'
WHERE api_config_id = (SELECT id FROM fx_api_config WHERE api_code = 'sys_user_sync' LIMIT 1);

UPDATE fx_api_outbound_target
SET target_name = '本地ERP测试系统',
    target_url = '/api/users/third-party/pull',
    http_method = 'POST',
    remark = '从本地 ERP 测试系统拉取用户'
WHERE api_config_id = (SELECT id FROM fx_api_config WHERE api_code = 'sys_user_pull' LIMIT 1);
