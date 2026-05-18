---
name: forgex-dynamic-table-usage
description: Forgex FxDynamicTable usage guide. Use when building list pages, wiring table-code/request/row-key, configuring dynamic columns, dictionary translation, pagination, sorting, search forms, action slots, or user column settings.
---

# FxDynamicTable Usage

## Source Docs And References

- Formal usage doc: `Forgex_Doc\前端\配置驱动页面\FxDynamicTable使用方式.md`
- Formal implementation doc: `Forgex_Doc\前端\配置驱动页面\FxDynamicTable实现逻辑.md`
- Standard doc: `Forgex_Doc\开发规范\规范文档\公共表格组件使用说明与实现逻辑.md`
- Local API/reference notes: `reference.md`
- Local examples: `examples.md`

Load `examples.md` only when you need a concrete page pattern. Load `reference.md` only when you need implementation detail.

## Default Pattern

For ordinary management lists:

- Use `<FxDynamicTable>`.
- Provide a stable `table-code`.
- Provide a stable `row-key`, usually `id`.
- Provide a request function that adapts the project API response to the shape expected by the current component.
- Put operation links/buttons in the action slot and protect them with `v-permission`.
- Keep custom slots named exactly after the column `field`.

## Field And Config Rules

- The column `field` must match backend response fields exactly.
- Dynamic table config does not replace backend data work; backend DTO/VO/service/query must return the field.
- Dictionary rendering should use the established dict fields or translation fields already supported by the component.
- Column settings must preserve the complete base column list, including hidden columns.
- `table-code` should be unique and stable for the business table/page.
- New user-visible column titles need i18n entries.

## Request Function Rules

- Inspect an existing page in the same module before writing the request adapter.
- Keep pagination, sorting, and search params compatible with the backend API.
- Do not swallow errors that the project HTTP wrapper already handles.
- Refresh through the table instance method/pattern used by nearby pages.

## Finish Check

- Table loads data.
- Search/pagination still work.
- Custom slots render.
- Column settings open and save if config was changed.
- Frontend API type and backend response are aligned.
