---
name: forgex-frontend-guide
description: Forgex frontend implementation guide for Vue3, TypeScript, Ant Design Vue, FxDynamicTable, BaseFormDialog, HTTP requests, locales, layout, and components. Use when editing Forgex_MOM/Forgex_Fronted files, frontend pages, dialogs, dynamic tables, i18n text, API wrappers, or frontend feature documentation.
---

# Forgex Frontend Guide

## First Steps

1. Read the existing page/component/API file in the same module before writing new structure.
2. Use the common management-page pattern unless the feature is intentionally custom.
3. Keep frontend fields aligned with backend response fields, dynamic table fields, permissions, and docs.
4. Load the relevant formal docs from `Forgex_Doc\前端`.

## Core Stack

- Vue 3 with `<script setup lang="ts">`
- TypeScript
- Ant Design Vue
- Vite
- Pinia
- Vue Router
- Axios through the project HTTP wrapper

## Core Docs

- Frontend index: `Forgex_Doc\前端\README.md`
- HTTP: `Forgex_Doc\前端\请求与反馈\HTTP请求使用方式.md` and `HTTP请求实现逻辑.md`
- Common dialog: `Forgex_Doc\前端\请求与反馈\公共弹窗使用方式.md` and `公共弹窗实现逻辑.md`
- Dynamic table: `Forgex_Doc\前端\配置驱动页面\FxDynamicTable使用方式.md` and `FxDynamicTable实现逻辑.md`
- Route/menu: `Forgex_Doc\前端\基础设施\路由与菜单使用方式.md` and `路由与菜单实现逻辑.md`
- Permissions: `Forgex_Doc\前端\基础设施\权限指令使用方式.md` and `权限指令实现逻辑.md`
- State: `Forgex_Doc\前端\基础设施\状态管理使用方式.md` and `状态管理实现逻辑.md`
- Form validation: `Forgex_Doc\前端\基础设施\表单验证使用方式.md` and `表单验证实现逻辑.md`
- I18n/layout: `Forgex_Doc\前端\国际化与布局\README.md`
- Module mapping: `Forgex_Doc\开发规范\模块文档映射\README.md`

## Implementation Rules

- Put API calls in `src\api`, not inline in Vue pages.
- Use the project `http`, `httpSuccess`, or `silentHttp` clients from `src\api\http.ts`; do not create a separate axios client.
- Pages must call business API wrapper functions, not scatter raw endpoint strings in Vue components.
- For ordinary list pages, prefer `FxDynamicTable` with a stable `table-code`, `row-key`, and request function.
- The request function for `FxDynamicTable` must return the shape expected by the current component implementation; inspect nearby pages before adapting.
- Prefer `BaseFormDialog` for create/edit/detail forms where the existing page style uses it.
- Use `v-permission` for operation buttons and keep codes identical to backend/menu permissions.
- Use `useI18n` or the existing translation helper used by the page. Do not add hard-coded user-facing text.
- When adding visible text, update the five locale files: `zh-CN`, `zh-TW`, `en-US`, `ja-JP`, `ko-KR`.
- Keep styles local and consistent with Ant Design Vue and existing Less conventions. Avoid rewriting page layout for aesthetic reasons.
- Do not duplicate shared component logic unless the behavior is genuinely page-specific.

## Dynamic Table Field Chain

When a list column is added or fixed, check the complete chain:

- database/table config field
- backend entity/DTO/VO/response field
- service assignment or query selection
- frontend API type
- `FxDynamicTable` column config or `table-code`
- frontend slot name, if custom rendering is used
- locale/i18n title

The column `field` and slot name must match exactly. Similar names are not equivalent.

## API Contract Rules

- Keep request and response interfaces in the API file near the API methods.
- Match existing endpoint style. Many Forgex pages use `POST` endpoints for page/detail/create/update/delete flows.
- First-party business API paths normally start from service public prefixes such as `/sys`, `/basic`, `/wf`, `/integration`, or `/report`; do not duplicate `/api` in wrappers when `http.ts` already handles the API prefix.
- Use `http` for list/detail/query, `httpSuccess` or `showSuccessMessage` for create/update/delete/submit/save operations, and `silentHttp` only for polling, unread counts, preload, or background refresh.
- Do not manually add `message.success` for normal writes when the HTTP layer already handles backend success messages.
- Do not repeatedly inspect `res.code` in pages when `http.ts` already normalizes success/error behavior.
- Define TypeScript request/response interfaces that mirror backend Param/DTO/Response fields.
- Convert response records only at the page boundary when needed; prefer fixing the API contract if the data is wrong.

## Backend Coupling Rules

When backend API changes:

- Update the matching frontend API wrapper in `src\api`.
- Update page request adapters, form models, table slots, and TypeScript types.
- Keep permission codes identical to backend annotations and menu/button configuration.
- Keep dynamic table `field` identical to backend response fields.
- Update locale keys for any new visible text.

## Finish Check

- Validate the changed page with the smallest practical frontend check.
- If behavior changed, update the matching frontend docs.
- Keep docs version metadata unchanged unless the user explicitly asks.
