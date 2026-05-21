-- Forgex_Job JavaBean 冒烟测试任务
-- 用途：本地或测试环境验证任务配置、手动触发、执行日志、失败、异常和超时链路。
-- 注意：生产环境不需要执行本脚本。

SET @TENANT_ID := COALESCE((SELECT id FROM forgex_admin.sys_tenant WHERE deleted = 0 ORDER BY id LIMIT 1), 0);

INSERT INTO forgex_job.sys_job_task (
  tenant_id, job_code, job_name, job_group, job_type, schedule_type, bean_name, method_name, job_params,
  status, block_strategy, timeout_seconds, max_retry_count, retry_interval_seconds, shard_total, broadcast_enabled,
  remark, create_time, create_by, update_time, update_by, deleted
) VALUES (
  @TENANT_ID, 'job_smoke_test', 'Job Smoke Test', 'system-test', 1, 3, 'jobSmokeTestHandler', 'execute',
  '{"mode":"success","message":"manual smoke test"}',
  0, 1, 10, 1, 10, 1, 0,
  'Smoke test task for JavaBean success, failure, exception and timeout flow.',
  NOW(), '20260513_job_smoke_test_task', NOW(), '20260513_job_smoke_test_task', 0
) ON DUPLICATE KEY UPDATE
  job_name = VALUES(job_name),
  job_group = VALUES(job_group),
  job_type = VALUES(job_type),
  schedule_type = VALUES(schedule_type),
  bean_name = VALUES(bean_name),
  method_name = VALUES(method_name),
  job_params = VALUES(job_params),
  status = 0,
  block_strategy = VALUES(block_strategy),
  timeout_seconds = VALUES(timeout_seconds),
  max_retry_count = VALUES(max_retry_count),
  retry_interval_seconds = VALUES(retry_interval_seconds),
  shard_total = VALUES(shard_total),
  remark = VALUES(remark),
  update_time = NOW(),
  update_by = '20260513_job_smoke_test_task';
