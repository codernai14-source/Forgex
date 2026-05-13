---
name: frontend-design
description: Forgex Vue3 + TypeScript frontend development guidance. Use when creating Vue components, pages, API wrappers, state management, permissions, i18n, styles, forms, dialogs, dynamic tables, or frontend business logic.
---

# Forgex Frontend Design

## Source Docs

- `Forgex_Doc\前端\README.md`
- `Forgex_Doc\前端\基础设施\README.md`
- `Forgex_Doc\前端\请求与反馈\README.md`
- `Forgex_Doc\前端\配置驱动页面\README.md`
- `Forgex_Doc\前端\组件与页面\README.md`
- `Forgex_Doc\前端\国际化与布局\README.md`

## Rules

- Use Vue 3 `<script setup lang="ts">` and local page conventions.
- Use Ant Design Vue components already present in the project.
- Use `src\api` wrappers and the project HTTP clients from `src\api\http.ts`.
- Pages must call API wrapper functions; do not write raw axios calls or scatter endpoint strings in Vue files.
- API wrappers use `http` for query/detail, `httpSuccess` or `showSuccessMessage` for writes, and `silentHttp` only for background/preload/polling behavior.
- Prefer `FxDynamicTable` for list pages and `BaseFormDialog` for standard forms.
- Use `v-permission` for operation controls.
- Keep user-visible text internationalized in the five locale files.
- Do not manually duplicate success/error messages already handled by the HTTP layer.
- Keep styles scoped or locally constrained unless modifying a shared style system.
- Do not redesign stable management pages unless the user asks for redesign.

## Cross-Layer Check

Frontend changes must match backend API paths, field names, permissions, dictionaries, dynamic table config, and docs.
