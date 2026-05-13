---
name: forgex-deploy-db-guide
description: Forgex deployment and database environment guide. Use for Nacos configuration, datasource addresses, Redis, RocketMQ, captcha, sa-token, deployment docs, database security/configuration docs, SQL environment questions, initialization scripts, or changes involving Forgex_Doc deployment/database files.
---

# Forgex Deploy And Database Guide

## Source Docs

Use current `Forgex_Doc` paths:

- Deploy index: `Forgex_Doc\部署\README.md`
- Authorization: `Forgex_Doc\部署\授权说明使用方式.md` and `授权说明实现逻辑.md`
- Operations: `Forgex_Doc\部署\运维监控与故障排查指南.md`
- Database index: `Forgex_Doc\数据库\README.md`
- Database design: `Forgex_Doc\数据库\设计规范\数据库设计规范.md`
- Database scripts: `Forgex_Doc\数据库\脚本与修复\README.md`
- Database field spec: `Forgex_Doc\开发规范\规范文档\数据库字段统一规范文档.md`
- Nacos configs: `Forgex_Doc\部署\nacos配置`
- Initial SQL: `Forgex_Doc\部署\数据库初始化脚本`

## Environment Defaults

- Local MySQL defaults: `127.0.0.1:3306`, user `root`, password `123456`.
- Preserve UTF-8 for Chinese SQL comments and docs.
- Do not expose production secrets in docs or code.

## SQL And Script Rules

- Put upgrade/fix scripts under `Forgex_Doc\数据库\脚本与修复`.
- Put full initialization scripts under `Forgex_Doc\部署\数据库初始化脚本`.
- Name one-off upgrade scripts with a concrete date and business purpose, following existing files such as `20260512_material_import_sync_upgrade.sql`.
- Reuse field names, data types, lengths, indexes, comments, tenant fields, and audit fields from the field spec.
- Prefer additive, reviewable scripts. Include rollback notes when the change is risky.
- Do not change formal doc versions unless the user explicitly asks.

## Data Source Discipline

- Keep datasource names and script targets aligned with existing Nacos and initialization files.
- Avoid cross-database joins in application SQL unless the architecture explicitly supports that path.
- For shared data across services, prefer service/API or integration contract patterns already used in the codebase.

## Finish Check

- Confirm SQL targets the intended database.
- Confirm tenant and logical delete behavior where relevant.
- Update `Forgex_Doc\数据库` or `Forgex_Doc\部署` docs when behavior/config changed.
- Report whether scripts were syntax-reviewed or executed.
