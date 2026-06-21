# Doc vs Source Check

> Updated: 2026-06-21

## Resolved In This Check

- `Forgex_Doc/后端/README.md`: aligned service ports with backend `application.yml`.
- `Forgex_Doc/部署/README.md`: aligned report port, Nacos namespace default, and Nacos username/password environment variables with backend `application.yml`.
- `Forgex_Doc/部署/授权说明使用方式.md`: aligned Windows install port example with backend ports.
- `Forgex_Doc/部署/运维监控与故障排查指南.md`: aligned report health-check port with backend source.
- `Forgex_Doc/后端/模块专题/代码生成使用方式.md`: documented `TREE_SINGLE`, `TREE_DOUBLE`, `android`, VO output, and DICT/API option source fields now present in source.
- `Forgex_Doc/后端/模块专题/代码生成实现逻辑.md`: documented the four current codegen strategies, VO templates, Android templates, and option-source normalization.
- `Forgex_MOM/Forgex_Backend/Forgex_Integration/README.md`: aligned service port and SQL paths with current repository layout.
- `Forgex_Doc/数据库` and related module docs: replaced old `doc/sql` references with current `Forgex_Doc/部署/数据库初始化脚本` or `Forgex_Doc/数据库/脚本与修复` paths.

## Remaining Conflicts Not Committed

- `Forgex_Build/manifest/services.yml`: `report.defaultPort` is still `8084`, while `Forgex_MOM/Forgex_Backend/Forgex_Report/src/main/resources/application.yml` defaults to `9006`. This file is under `Forgex_Build`; per the current request, `Forgex_Build` changes are excluded from the commit.
