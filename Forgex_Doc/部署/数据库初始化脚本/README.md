# 数据库初始化脚本（全量）

首次部署或全新环境使用：7 个库的结构 + 初始数据，一次导入即可启动。

## 库与导入顺序

| 顺序 | 脚本 | 目标库 |
| :---: | --- | --- |
| 1 | forgex_common.sql | 公共（国际化、配置等） |
| 2 | forgex_admin.sql | 主业务库（用户/菜单/字典/基础信息） |
| 3 | forgex_history.sql | 历史数据 |
| 4 | forgex_job.sql | 调度任务 |
| 5 | forgex_workflow.sql | 工作流 |
| 6 | forgex_scada.sql | SCADA 采集 |
| 7 | forgex_integration.sql | 第三方集成 |

> 交付包内 `import-database.ps1` / `import-database.sh` 会按上述顺序自动导入（仅空库时导入，`-ResetDatabase` 才重建）。

## 手动导入

脚本自带 `CREATE DATABASE IF NOT EXISTS` 与 `USE`，可独立执行：

```bash
mysql -u<user> -p < forgex_common.sql
mysql -u<user> -p < forgex_admin.sql
# ...其余按上表顺序依次导入
```

- 字符集：utf8mb4（表级 utf8mb4_0900_ai_ci，需 MySQL 8.0+）
- 幂等性：表为 `DROP TABLE IF EXISTS + CREATE`，重复导入等效重建（会清空已有数据）
- 初始账号：见 `forgex_admin.sys_user`（含 admin 及测试用户，密码为 bcrypt 哈希）

## 维护说明

- 本目录由开发基准库全量导出刷新，勿手改；结构性变更请先落
  `Forgex_Doc/数据库/脚本与修复/{yyyyMMdd}/` 升级脚本，再统一刷新本目录
- `forgex_erp` 为本地历史遗留库，不属于 7 库架构，不纳入初始化脚本
