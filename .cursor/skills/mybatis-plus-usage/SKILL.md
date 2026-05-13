---
name: mybatis-plus-usage
description: MyBatis-Plus and MPJ usage guidance for Forgex. Use when developing data access, Mapper/Service code, LambdaQueryWrapper or MPJ joins, pagination, XML SQL, tenant-aware queries, or reviewing mapper/service database operations.
---

# MyBatis-Plus And MPJ Usage

## Core Rule

Prefer project-standard MyBatis-Plus and MPJ APIs. Do not add annotation SQL.

Forbidden in Mapper interfaces for new code:

- `@Select`
- `@Update`
- `@Insert`
- `@Delete`

If custom SQL is unavoidable, put it in XML under the module mapper resources and keep the namespace/method signatures aligned.

## Standard Patterns

- Mapper extends `BaseMapper<Entity>`.
- Service implementation extends `ServiceImpl<Mapper, Entity>` when the local module uses that pattern.
- Use `LambdaQueryWrapper` for single-table CRUD queries.
- Use `MPJLambdaQueryWrapper` for normal joins when MPJ is already available in the module.
- Use existing page parameter and page result conventions from the touched module.
- Always include tenant, logical delete, status, or data-scope conditions according to the entity/module behavior.

## XML SQL Rules

- XML belongs in the service module resources mapper directory used by that module.
- XML namespace must point to the Mapper interface fully qualified name.
- Result fields must map to DTO/VO fields explicitly when names diverge.
- Avoid `SELECT *` for custom queries that feed DTO/VO responses.
- Keep aliases stable and understandable.

## Query Safety

- Build conditions with wrappers and parameter binding.
- Do not concatenate raw SQL with user input.
- For pagination, include deterministic ordering unless an existing framework layer supplies it.
- For updates/deletes, require a bounded condition and respect logical delete conventions.

## Related Docs

- `Forgex_Doc\开发规范\规范文档\MyBatis-Plus 使用规范.md`
- `Forgex_Doc\开发规范\规范文档\数据库字段统一规范文档.md`
- `Forgex_Doc\数据库\设计规范\数据库设计规范.md`
