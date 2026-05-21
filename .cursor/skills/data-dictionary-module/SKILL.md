---
name: data-dictionary-module
description: Forgex data dictionary module guidance. Use when developing dictionary management, enum/options, dropdown values, dict tags, dictionary caching, dictCode usage, backend dictionary APIs, or frontend dictionary display.
---

# Forgex Data Dictionary Module

## Source Docs

- `Forgex_Doc\后端\配置与审计\数据字典与日志使用方式.md`
- `Forgex_Doc\后端\配置与审计\数据字典与日志实现逻辑.md`
- `Forgex_Doc\开发规范\模块文档映射\README.md`

## Rules

- Reuse the existing system dictionary model and APIs before adding new tables.
- Keep dictionary code, item value, label, status, tenant scope, and cache behavior aligned with existing implementation.
- Frontend dictionary display must use the existing dictionary translation pattern or dynamic table dictionary support.
- Do not hard-code option labels in pages when a dictionary exists.
- If a new dictionary affects UI text, update locale/docs as needed.
- If backend prompt messages are involved, use the existing prompt/i18n mechanism.

## Finish Check

- Dictionary code exists and is documented.
- Frontend and backend use the same code/value semantics.
- Cache invalidation or refresh behavior is considered.
