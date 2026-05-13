---
name: forgex-project-guide
description: Forgex project navigation and documentation entry skill. Use whenever working in D:\mine_product\forgex, changing Forgex_MOM code, reading Forgex_Doc, deciding which Forgex frontend/backend/deployment/database documents to load, or preparing follow-up Forgex feature work.
---

# Forgex Project Guide

## Core Rule

Treat `Forgex_Doc` as the source of truth. Use skills as navigation and guardrails, not as a replacement for current code and formal docs.

Before changing Forgex code or docs:

1. Identify the affected module and layer.
2. Load the most specific companion skill.
3. Read the matching `Forgex_Doc` entry or nearby existing code before implementing.
4. Keep edits scoped and preserve unrelated worktree changes.
5. For behavior changes, run `forgex-feature-self-review` before finishing.

## Repository Map

- Root: `D:\mine_product\forgex`
- Formal docs: `Forgex_Doc`
- Backend: `Forgex_MOM\Forgex_Backend`
- Frontend: `Forgex_MOM\Forgex_Fronted`
- Build/deploy support: `Forgex_Build`

Backend services currently include:

- `Forgex_Common`
- `Forgex_Gateway`
- `Forgex_Auth`
- `Forgex_Sys`
- `Forgex_Job`
- `Forgex_Basic`
- `Forgex_Integration`
- `Forgex_Workflow`
- `Forgex_Report`

## Skill Selection

- Frontend page, Vue component, API wrapper, locale, dialog, table: use `forgex-frontend-guide`.
- Java controller, service, entity, mapper, DTO/VO/Param, Feign, tenant, workflow, supplier, code generation: use `forgex-backend-guide`.
- Naming, comments, schema, MyBatis-Plus, module boundaries, i18n prompts, dynamic table columns: use `forgex-development-standards`.
- Database schema, SQL script, datasource, Nacos, Redis, RocketMQ, deployment path, authorization config: use `forgex-deploy-db-guide`.
- Dynamic table fields, `fx_table_column_config`, column settings, missing table columns: use `dynamic-table-config`.
- Workflow approval integration: use `workflow-module-usage`.
- Data dictionary or option list integration: use `data-dictionary-module`.
- Code review or risk check: use `forgex-review`.

## Documentation Entry Points

Start from these docs, then follow the local README or module mapping:

- `Forgex_Doc\README.md`
- `Forgex_Doc\开发规范\README.md`
- `Forgex_Doc\开发规范\模块文档映射\README.md`
- `Forgex_Doc\开发规范\规范文档\README.md`
- `Forgex_Doc\前端\README.md`
- `Forgex_Doc\后端\README.md`
- `Forgex_Doc\数据库\README.md`
- `Forgex_Doc\部署\README.md`

## Documentation Discipline

- Update matching feature docs when behavior, API, UI, config, workflow, schema, or usage changes.
- Do not change `版本`, `version`, or update-date metadata unless the user explicitly asks.
- Keep docs factual and scoped to the changed behavior.
- If docs and code disagree, inspect current code and nearby docs, then update the stale artifact rather than inventing a third convention.
