---
name: code-comment-standard
description: Java code comment standard for Forgex. Use when writing Java code, adding Javadoc, reviewing comments, creating classes/methods/fields, or refactoring comments. Follow Forgex Javadoc conventions and use Chinese comments.
---

# Forgex Code Comment Standard

## Source Doc

Use `Forgex_Doc\开发规范\规范文档\代码注释规范.md` as the formal rule.

## Hard Requirements

- All newly created or materially changed Java classes must carry valid Javadoc-style class comments.
- All public and protected methods must carry valid Javadoc-style method comments.
- All entity, DTO, Param, Response/VO, enum, constant, and Feign contract fields must carry meaningful field comments unless the field is inherited and not declared in the file.
- Method bodies must include short block comments for each meaningful business logic segment. Use them to mark validation, uniqueness checks, tenant/data-scope handling, third-party calls, workflow state changes, persistence, cache refresh, and response assembly.
- Comments must be effective comments, not placeholders. Do not write empty text such as "处理逻辑", "TODO", "业务处理", or comments that only repeat the method name.
- Comments must be kept in sync with code when logic changes.

## Principles

- Use Chinese comments for Java business code.
- Comments should explain business intent, constraints, side effects, and integration behavior.
- Do not add noisy comments for trivial assignments, but do annotate each method's business stages.
- Keep comments consistent with actual behavior; stale comments are worse than no comments.
- Preserve existing module style for author/version/since fields when present.

## Required Comment Areas

- Public classes and important package-level extension points.
- Public controller/service methods.
- Entity, Param, DTO, Response/VO, enum, and Feign DTO fields.
- Complex transaction flows, tenant/data-scope logic, external integration calls, workflow callbacks, cache invalidation, and retry/compensation behavior.

## Class Comment Shape

For business classes, include:

- business purpose
- responsibility boundary
- important integration or tenant notes when relevant
- author/version/since only when the local module already uses those tags

## Method Comment Shape

For public/protected methods, include:

- what the method does in business terms
- `@param` for every parameter
- `@return` for non-void methods
- `@throws` for business exceptions that callers or maintainers must understand

## Method Body Comment Shape

Inside methods, use concise comments before logic groups, for example:

- 参数校验
- 查询并校验业务对象
- 校验租户或权限边界
- 组装第三方系统请求
- 持久化主表和子表
- 刷新缓存或动态表格配置
- 返回统一响应数据

Avoid a comment on every single line. The required granularity is "one comment per business block", not "one comment per statement".

## Javadoc Guidelines

- Class comments should state the business role of the class.
- Method comments should include purpose, key parameters, return meaning, and exceptions only when useful.
- Field comments should match database column comments when the field maps to a table.
- Avoid referencing unrelated base classes or examples. Inspect nearby code and follow the local convention.

## Finish Check

- Comments match implementation.
- Chinese text is UTF-8.
- No copied example comments remain.
- Public API comments align with docs if behavior changed.
