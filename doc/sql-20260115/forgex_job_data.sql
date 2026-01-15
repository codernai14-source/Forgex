-- ================================================
-- Forgex Job 数据库初始数据
-- 数据库: forgex_job
-- 说明: 定时任务相关初始数据
-- ================================================

USE forgex_job;

-- ================================================
-- 初始化任务调度器配置
-- ================================================
INSERT INTO sys_job_scheduler (scheduler_name, scheduler_type, config_json, status, remark) VALUES
('默认调度器', 'QUARTZ', '{"threadPoolSize":10,"threadNamePrefix":"job-executor-"}', 1, '系统默认定时任务调度器');

-- ================================================
-- 初始化示例定时任务
-- ================================================
-- 注意：这里需要先从forgex_admin数据库获取租户ID
-- 如果跨库查询不可用，请手动替换以下变量值
SET @TENANT_ID = 1;

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, tenant_id, create_by, remark) VALUES
('清理过期登录日志', 'SYSTEM', 'loginLogService.cleanExpiredLogs', '0 0 2 * * ?', '3', '0', '0', @TENANT_ID, 'system', '每天凌晨2点清理30天前的登录日志'),
('清理操作日志', 'SYSTEM', 'operationLogService.cleanExpiredLogs', '0 0 3 * * ?', '3', '0', '0', @TENANT_ID, 'system', '每天凌晨3点清理90天前的操作日志'),
('系统数据备份', 'SYSTEM', 'backupService.backupDatabase', '0 0 4 * * ?', '3', '0', '0', @TENANT_ID, 'system', '每天凌晨4点进行数据库备份'),
('清理临时文件', 'SYSTEM', 'fileService.cleanTempFiles', '0 0 5 * * ?', '3', '0', '0', @TENANT_ID, 'system', '每天凌晨5点清理临时文件');

-- 完成提示
SELECT 'forgex_job数据库初始数据插入完成！' AS message;
SELECT '已初始化4个示例定时任务，可根据实际需求调整' AS remark;
