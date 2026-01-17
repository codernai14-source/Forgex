-- 清理 UserTable 的重复表格配置（保留 tenant_id=0 公共配置）
-- 说明：
-- - 表结构 tenant_id 为 NOT NULL，本项目约定 tenant_id=0 作为公共配置
-- - 若历史上写入了非 0 的重复配置，会导致前端读取到多套列配置，出现列名/按钮异常
-- - 本脚本采用逻辑删除（deleted=1），可回滚

START TRANSACTION;

UPDATE fx_table_column_config
SET deleted = 1
WHERE table_code = 'UserTable'
  AND tenant_id <> 0
  AND deleted = 0;

UPDATE fx_table_config
SET deleted = 1
WHERE table_code = 'UserTable'
  AND tenant_id <> 0
  AND deleted = 0;

COMMIT;

