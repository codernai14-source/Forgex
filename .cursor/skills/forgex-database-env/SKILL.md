---
name: forgex-database-env
description: Forgex database environment skill. Use for local MySQL defaults, datasource routing, database names, script targets, Nacos datasource config, database initialization scripts, and environment-specific SQL questions.
---

# Forgex Database Environment

## Defaults

- Local MySQL: `127.0.0.1:3306`
- Local user: `root`
- Local password: `123456`

## Docs

- `Forgex_Doc\部署\nacos配置`
- `Forgex_Doc\部署\数据库初始化脚本`
- `Forgex_Doc\数据库\README.md`
- `Forgex_Doc\数据库\脚本与修复\README.md`

## Rules

- Confirm target database before writing SQL.
- Do not expose production credentials.
- Keep datasource names aligned with Nacos and initialization scripts.
