---
name: forgex-development-standards
description: Forgex development standards skill for architecture, naming, Git workflow, database field rules, Java comments, MyBatis-Plus usage, dynamic table configuration, i18n prompt messages, module documentation mapping, and consistency checks. Use before creating or changing Forgex code, schemas, APIs, tables, Java comments, dynamic table columns, or docs.
---

# Forgex Development Standards

## Source Documents

Load the relevant formal doc before editing:

- Index: `Forgex_Doc\开发规范\README.md`
- Architecture: `Forgex_Doc\开发规范\架构设计\项目架构设计文档.md`
- Module docs mapping: `Forgex_Doc\开发规范\模块文档映射\README.md`
- Git workflow: `Forgex_Doc\开发规范\规范文档\Git 提交与开发规范.md`
- MyBatis-Plus: `Forgex_Doc\开发规范\规范文档\MyBatis-Plus 使用规范.md`
- Java comments: `Forgex_Doc\开发规范\规范文档\代码注释规范.md`
- Database fields: `Forgex_Doc\开发规范\规范文档\数据库字段统一规范文档.md`
- Dynamic table: `Forgex_Doc\开发规范\规范文档\公共表格组件使用说明与实现逻辑.md`
- I18n prompts: `Forgex_Doc\开发规范\规范文档\国际化提示消息使用指南.md`
- Tests: `Forgex_Doc\开发规范\规范文档\单元测试与集成测试规范.md`

## Baseline Architecture

- Backend code lives under `Forgex_MOM\Forgex_Backend`.
- Frontend code lives under `Forgex_MOM\Forgex_Fronted`.
- Formal docs live under `Forgex_Doc`; do not use old `doc/开发规范` paths as current truth.
- Keep package/module ownership consistent with nearby code.

Backend features normally follow:

- `controller`
- `service`
- `service\impl`
- `mapper`
- `domain\entity`
- `domain\param`
- `domain\dto`
- `domain\response` or local VO naming already used by the module

Frontend management pages normally follow:

- `src\views\<module>`
- `src\api\<module>.ts`
- `src\components` only when reusable
- `src\locales\zh-CN`, `zh-TW`, `en-US`, `ja-JP`, `ko-KR` for user-visible text

## Required Rules

- Prefer existing patterns in the same module over generic examples.
- Backend controller responses use the project unified wrapper such as `R<T>`.
- Backend success/failure messages use project Prompt/i18n patterns such as `CommonPrompt`, module Prompt enums, `R.ok(...)`, `R.fail(...)`, and i18n business exceptions. Do not return hard-coded Chinese strings.
- Permission-protected endpoints use the existing permission annotation and matching frontend `v-permission` code.
- Prefer MyBatis-Plus `BaseMapper`, `ServiceImpl`, `LambdaQueryWrapper`, and MPJ for normal CRUD and joins.
- Do not add annotation SQL such as `@Select`, `@Update`, `@Insert`, or `@Delete`. If handwritten SQL is unavoidable, use XML under the module mapper resources and keep namespace aligned.
- Entity-backed backend features normally use `Entity extends BaseEntity`, query/page Param extends `BaseGetParam`, `Mapper extends BaseMapper<Entity>`, `I<Service> extends IService<Entity>`, and `ServiceImpl extends ServiceImpl<Mapper, Entity> implements I<Service>`, unless nearby code shows a deliberate module exception.
- New and changed Java code must include effective Javadoc-style comments on classes, methods, and fields, plus concise method-body comments for each meaningful business logic segment.
- Database fields must reuse the standard names, types, lengths, and comments from the database field spec.
- Do not introduce alternate names for the same business concept across DB, Java, API, frontend, and docs.
- Frontend list pages should use `FxDynamicTable` unless the existing page or requirement clearly uses another pattern.
- Frontend form dialogs should reuse `BaseFormDialog` or the established local dialog pattern.
- User-visible frontend text and backend prompt messages must go through the project i18n/prompt mechanism.
- When adding menus, buttons, prompts, fields, or column titles, keep the five frontend language files in sync unless the existing module intentionally uses a different translation helper.
- Frontend pages call business API wrappers in `src\api`; pages must not directly create axios calls or scatter endpoint strings.
- API wrappers use the project HTTP clients from `src\api\http.ts`: `http`, `httpSuccess`, or `silentHttp` according to operation type.
- Preserve UTF-8 for Chinese text on Windows.

## Backend Interface Contract

For a standard CRUD or management feature:

- Controller exposes methods returning `R<T>`.
- Controller delegates business logic to Service; do not put persistence orchestration in Controller.
- Service interface defines business methods using Param/DTO/Response types.
- Service implementation handles validation, uniqueness checks, tenant/data-scope, transactions, mapper calls, external calls, and response assembly.
- Mapper extends `BaseMapper<Entity>` and only declares XML-backed custom methods when wrappers are insufficient.
- Entity maps table fields and extends `BaseEntity` for normal business tables.
- Page/query Param extends `BaseGetParam` when it participates in paging/query conventions.

## Frontend API Contract

For frontend/backend integration:

- Add or update `src\api\<module>.ts` typed interfaces and functions.
- Keep frontend request/response interface names and fields aligned with Java Param/DTO/Response names.
- Use `http` for normal query/detail, `httpSuccess` or `showSuccessMessage` for save/delete/submit operations, and `silentHttp` only for polling/preload/non-interrupting work.
- Rely on the HTTP layer for success/error prompts unless the UX intentionally needs custom handling.
- After save/delete/submit, close dialogs and refresh `FxDynamicTable` or local list through the existing page pattern.

## Companion Skills

- `forgex-frontend-guide` for Vue, API wrappers, locales, dynamic table usage, dialogs, and UI behavior.
- `forgex-backend-guide` for Java modules, services, controllers, mappers, Feign, tenant, workflow, and domain objects.
- `database-design` for MySQL schema or SQL script work.
- `mybatis-plus-usage` for mapper/service data access.
- `code-comment-standard` for Java Javadoc and comments.
- `dynamic-table-config` for `FxDynamicTable` and `fx_table_column_config`.
- `workflow-module-usage` for approval/workflow integration.
- `data-dictionary-module` for dictionaries and option lists.

## Documentation Discipline

Every feature-level change needs a documentation check through `Forgex_Doc\开发规范\模块文档映射\README.md`.

- Update behavior and implementation notes when the task changes behavior, API, UI, schema, config, or usage.
- Do not change document version metadata unless explicitly requested.
- Mention validation that was run, or why it could not be run.
