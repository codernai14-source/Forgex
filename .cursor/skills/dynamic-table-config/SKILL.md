---
name: dynamic-table-config
description: Forgex public dynamic table configuration guide. Use when developing FxDynamicTable, fixing missing columns, configuring fx_table_column_config, aligning backend/front-end table fields, handling user column preferences, or debugging column setting save behavior.
---

# Forgex Dynamic Table Config

## Source Docs And References

- Formal doc: `Forgex_Doc\开发规范\规范文档\公共表格组件使用说明与实现逻辑.md`
- Frontend docs: `Forgex_Doc\前端\配置驱动页面\FxDynamicTable使用方式.md` and `FxDynamicTable实现逻辑.md`
- Local reference: `reference.md`
- Troubleshooting checklist: `troubleshooting.md`

Read `troubleshooting.md` for missing columns or broken column settings. Read `reference.md` only when implementation detail is needed.

## Core Rules

- `field` must match exactly across DB config, backend response, frontend API type, and frontend slot.
- A configured column does not create data automatically. Backend query/DTO/VO/service assignment must return the field.
- Hidden columns must remain in the complete column set; the visible table can filter them, but the settings dialog and save merge must use the full base column list.
- Public default config uses the project convention for public tenant/config rows; inspect existing SQL before adding rows.
- `title_i18n_json` and other JSON columns must contain valid JSON.
- Do not solve a data-missing problem by hard-coding fake frontend fields.

## Field Chain Checklist

When adding or fixing a column, verify:

- SQL/table config row exists for the correct `table_code`.
- The `field` name is identical everywhere.
- Backend response contains the field.
- DTO/VO and service mapping include the field.
- Frontend API type includes the field.
- Slot name matches `#field` if custom rendering is used.
- Locale title exists for all supported languages when user-visible.

## Finish Check

- Test the affected table load.
- Test column settings open/save if config changed.
- Update docs if table behavior changed.
