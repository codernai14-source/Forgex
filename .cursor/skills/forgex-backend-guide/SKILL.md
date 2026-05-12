---
name: forgex-backend-guide
description: Forgex backend implementation guide for Java modules, controllers, services, DTOs, entities, mappers, Feign clients, tenants, workflow, code generation, supplier master data, integration platform, Redis, RocketMQ, and backend feature documents. Use when editing Forgex_MOM/Forgex_Backend or backend docs.
---

# Forgex Backend Guide

## First Steps

1. Identify the service: `Forgex_Sys`, `Forgex_Basic`, `Forgex_Integration`, `Forgex_Workflow`, `Forgex_Report`, `Forgex_Auth`, `Forgex_Job`, `Forgex_Gateway`, or `Forgex_Common`.
2. Read nearby controllers/services/mappers in the same module before copying examples.
3. Load matching docs from `Forgex_Doc\后端` and `Forgex_Doc\开发规范`.
4. Keep cross-layer names aligned with database fields, DTO/VO fields, API params, frontend API types, permissions, and docs.

## Core Docs

- Backend index: `Forgex_Doc\后端\README.md`
- Identity and permissions: `Forgex_Doc\后端\身份与权限\README.md`
- Tenant context: `Forgex_Doc\后端\租户与上下文\README.md`
- Configuration and audit: `Forgex_Doc\后端\配置与审计\README.md`
- Public capabilities: `Forgex_Doc\后端\公共能力\README.md`
- Module topics: `Forgex_Doc\后端\模块专题\README.md`
- Module mapping: `Forgex_Doc\开发规范\模块文档映射\README.md`

## Implementation Rules

- Controller methods return the unified response wrapper, normally `R<T>`.
- Successful Controller responses use the existing `R.ok(...)`, `R.ok(prompt)`, `R.okWithArgs(...)`, or related project helpers. Failed business flows use `R.fail(...)` or throw the project i18n business exception according to nearby code.
- Do not return raw strings, raw maps, or hard-coded Chinese messages from Controller methods for business responses.
- Use the project exception and prompt/i18n mechanism for business failures.
- Permissioned endpoints must have matching backend permission annotation and frontend/menu permission code.
- Keep request style consistent with the module. Many existing management APIs use `POST` with `@RequestBody`; do not convert them to REST style only for taste.
- Use validated Param/DTO objects when the existing module does so; avoid loose `Map` for new API contracts unless nearby code uses it for compatibility.
- Service interfaces normally extend `IService<Entity>` when they manage an entity with MyBatis-Plus.
- Service implementations should reuse `ServiceImpl<Mapper, Entity>` when the module uses MyBatis-Plus service style.
- Mapper interfaces normally extend `BaseMapper<Entity>`.
- Entity classes for persisted business tables normally extend `com.forgex.common.base.BaseEntity` unless the table is a deliberate special case. Page/query Param classes normally extend `BaseGetParam` where paging/filter behavior is needed.
- Use `LambdaQueryWrapper`/`MPJLambdaQueryWrapper` for ordinary conditions and joins.
- Do not add annotation SQL. Put unavoidable custom SQL in XML mapper files.
- Add transactions only around real write workflows and use rollback rules consistent with nearby code.
- For Feign/internal APIs, put shared DTOs and clients in `Forgex_Common` only when they are truly cross-service contracts.
- Respect tenant isolation. Use ignore-tenant behavior only when the existing capability or doc says the data is public.
- JavaDoc comments are mandatory for new/changed classes, public methods, and fields. Method bodies must contain concise comments for each meaningful business block.

## Domain And Naming

Follow local package conventions first. The common shape is:

- `domain\entity`
- `domain\param`
- `domain\dto`
- `domain\response`
- `mapper`
- `service`
- `service\impl`
- `controller`

For standard entity-backed features, define contracts like this unless nearby code differs:

- `Entity extends BaseEntity`
- `PageParam extends BaseGetParam`
- `Mapper extends BaseMapper<Entity>`
- `I<Business>Service extends IService<Entity>`
- `<Business>ServiceImpl extends ServiceImpl<Mapper, Entity> implements I<Business>Service`
- `Controller` injects the service and returns `R<T>`

For business fields, reuse the same semantic names across:

- table columns
- entity fields
- params
- DTO/VO/response fields
- frontend API types
- dynamic table config
- docs

## API Contract And Frontend Handoff

- Define stable Param/DTO/Response objects for new API contracts.
- Keep request fields, response fields, TypeScript API interfaces, and dynamic table fields identical unless there is an explicit adapter.
- For page/detail/create/update/delete flows, follow the local endpoint style and response type used by the module.
- If the endpoint is consumed by the frontend, update `Forgex_MOM\Forgex_Fronted\src\api\...` with typed wrapper functions.
- Write frontend-facing docs or update the matching `Forgex_Doc` usage page when API behavior changes.

## Comment Requirements

When adding or modifying Java code:

- Add Javadoc to each new/changed class.
- Add Javadoc to each new/changed public/protected method with `@param`, `@return`, and `@throws` where applicable.
- Add comments to each declared entity/DTO/Param/Response field.
- Add short method-body comments before validation, query, permission/tenant checks, transformation, persistence, external calls, cache refresh, workflow updates, and response assembly.
- Ensure comments describe current behavior and business meaning.

## Feature-Specific Docs

Use these current doc pairs when relevant:

- Unified return/i18n: `Forgex_Doc\后端\配置与审计\统一返回与国际化使用方式.md` and `统一返回与国际化实现逻辑.md`
- Data dictionary/log: `Forgex_Doc\后端\配置与审计\数据字典与日志使用方式.md` and `数据字典与日志实现逻辑.md`
- Message template/SSE: `Forgex_Doc\后端\配置与审计\消息模板与SSE使用方式.md` and `消息模板与SSE实现逻辑.md`
- Auth: `Forgex_Doc\后端\身份与权限\认证授权使用方式.md` and `认证授权实现逻辑.md`
- User/role: `Forgex_Doc\后端\身份与权限\用户与角色使用方式.md` and `用户与角色实现逻辑.md`
- Tenant: `Forgex_Doc\后端\租户与上下文\多租户使用方式.md` and `多租户实现逻辑.md`
- Basic data/material: `Forgex_Doc\后端\模块专题\基础数据与物料使用方式.md` and `基础数据与物料实现逻辑.md`
- Integration platform: `Forgex_Doc\后端\模块专题\集成平台使用方式.md` and `集成平台实现逻辑.md`
- Workflow: `Forgex_Doc\后端\模块专题\工作流使用方式.md` and `工作流实现逻辑.md`
- Reports: `Forgex_Doc\后端\模块专题\报表中心使用方式.md` and `报表中心实现逻辑.md`
- Import/export: `Forgex_Doc\后端\模块专题\导入导出使用方式.md` and `导入导出实现逻辑.md`
- RocketMQ: `Forgex_Doc\后端\模块专题\RocketMQ使用方式.md` and `RocketMQ实现逻辑.md`
- Redis/cache: `Forgex_Doc\后端\公共能力\Redis工具使用方式.md`, `Redis工具实现逻辑.md`, `缓存策略使用方式.md`

## Finish Check

Before finishing backend work:

- Confirm permissions, tenant handling, prompt/i18n keys, DTO fields, and database columns line up.
- Update matching `Forgex_Doc` files when behavior changed.
- Do not change doc version metadata unless requested.
- Run the smallest practical compile/test for the touched module, or report why it was not run.
