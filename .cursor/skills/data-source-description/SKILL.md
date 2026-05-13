---
name: data-source-description
description: Forgex datasource description skill. Use when explaining or changing database sources, datasource routing, database names, Nacos datasource configuration, cross-database access, or SQL script target databases.
---

# Forgex Data Source Description

## Source Docs

- `Forgex_Doc\部署\nacos配置`
- `Forgex_Doc\部署\数据库初始化脚本`
- `Forgex_Doc\数据库\README.md`
- `Forgex_Doc\数据库\脚本与修复\README.md`
- `Forgex_Doc\部署\README.md`

## Rules

- Inspect current Nacos config and initialization scripts before naming a datasource.
- Confirm the target database before writing or reviewing SQL.
- Avoid cross-database joins unless the current architecture explicitly supports the path.
- Prefer service or integration contracts for cross-service data access.
- Do not embed production credentials in docs or code.

## Local Defaults

- Host: `127.0.0.1`
- Port: `3306`
- User: `root`
- Password: `123456`
