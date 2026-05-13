---
name: database-design
description: MySQL database design guidance for Forgex. Use when creating tables, adding fields, writing SQL scripts, designing schema, indexes, migration scripts, tenant fields, audit fields, or optimizing database queries.
---

# Forgex Database Design

## Source Docs

Read the current docs before schema work:

- `Forgex_Doc\数据库\README.md`
- `Forgex_Doc\数据库\设计规范\数据库设计规范.md`
- `Forgex_Doc\数据库\脚本与修复\README.md`
- `Forgex_Doc\开发规范\规范文档\数据库字段统一规范文档.md`
- `Forgex_Doc\开发规范\模块文档映射\README.md`

## Field Rules

- Reuse existing standard field names, data types, lengths, defaults, and comments.
- Keep one business meaning to one field name across tables. Do not create aliases such as `mat_name` when the project uses `material_name`.
- Business tables normally need audit fields and logical delete fields according to the field spec.
- Tenant-isolated tables must include the tenant field used by the current module.
- Chinese comments must be UTF-8 and business-readable.

## Script Rules

- Store upgrade/fix scripts in `Forgex_Doc\数据库\脚本与修复`.
- Store initialization scripts in `Forgex_Doc\部署\数据库初始化脚本`.
- Follow existing date-prefixed naming for one-off upgrade scripts.
- Keep scripts idempotent where practical.
- Use explicit column lists for inserts.
- Avoid destructive changes unless explicitly requested and documented.
- Include indexes needed by common filters, joins, permission/tenant conditions, and unique business constraints.

## Query Rules

- Prefer MyBatis-Plus wrappers or XML SQL through mapper files.
- Do not use annotation SQL in Mapper interfaces.
- Avoid unbounded updates/deletes.
- Avoid cross-database joins unless there is an explicit established pattern.
- Do not log or document production credentials.

## Documentation

Schema changes normally require docs under `Forgex_Doc\数据库` and may require backend/frontend docs if API or UI behavior changes. Do not change doc version metadata unless explicitly requested.
