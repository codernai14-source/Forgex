---
name: forgex-frontend
description: Forgex frontend focused implementation skill. Use for Vue3 + TypeScript + Ant Design Vue page work, forms, lists, dialogs, API wiring, FxDynamicTable, permission buttons, locales, and frontend-only bug fixes in Forgex_MOM/Forgex_Fronted.
---

# Forgex Frontend Focus

## Scope

Handle frontend work only. For backend/API/schema changes, also use `forgex-backend-guide` or the relevant database skill.

## Rules

- Read the existing page and neighboring pages first.
- Use `src\api` wrappers and the project HTTP client.
- Prefer `FxDynamicTable` for management list pages.
- Prefer `BaseFormDialog` for standard create/edit/detail dialogs.
- Keep `v-permission` codes aligned with backend/menu permissions.
- Add or update five locales for user-visible text: `zh-CN`, `zh-TW`, `en-US`, `ja-JP`, `ko-KR`.
- Keep API interfaces aligned with backend DTO/VO fields.
- Avoid broad redesigns or component rewrites unless requested.

## References

- `forgex-frontend-guide`
- `dynamic-table-config`
- `Forgex_Doc\前端\README.md`
- `Forgex_Doc\开发规范\模块文档映射\README.md`
