---
name: forgex-backend-implementation
description: Forgex backend business implementation skill. Use for Java controller/service/mapper/domain implementation, API contracts, permissions, MyBatis-Plus queries, Feign clients, transactions, and backend-only feature work.
---

# Forgex Backend Implementation

## Scope

Implement backend business logic in the existing Forgex module style. For broader backend guidance, use `forgex-backend-guide`.

## Implementation Rules

- Read nearby code in the same service before adding new classes.
- Keep the standard layer flow: controller, service, service impl, mapper, domain objects.
- Controller responses use `R<T>`.
- Successful responses use project helpers such as `R.ok(...)` and prompt-based variants; business failures use `R.fail(...)` or i18n business exceptions. Do not return hard-coded Chinese response messages.
- Use existing permission annotation patterns for protected endpoints.
- Keep API endpoint style consistent with the local module; many management APIs use POST and request bodies.
- Prefer MyBatis-Plus and MPJ wrappers.
- Do not add annotation SQL. Use XML for unavoidable custom SQL.
- Add transactions only for write workflows that need atomicity.
- Keep tenant isolation, data scope, logical delete, and audit fields consistent with the entity and module.
- Keep backend prompt/i18n keys consistent with existing enums/resources.
- Normal entity-backed code uses `Entity extends BaseEntity`, `PageParam extends BaseGetParam`, `Mapper extends BaseMapper<Entity>`, `I<Service> extends IService<Entity>`, and `ServiceImpl extends ServiceImpl<Mapper, Entity> implements I<Service>` unless local code intentionally differs.
- Add effective Javadoc to classes, public/protected methods, and declared fields.
- Add concise method-body comments before each meaningful business logic segment.

## Cross-Layer Checklist

When adding fields or endpoints, align:

- database column/script
- entity
- Param/DTO/VO/Response
- mapper query or service assignment
- controller route
- permission code
- frontend API type and page usage, if touched
- docs

## Interface Definition Rules

- Controller exposes transport-level API and delegates business logic to Service.
- Service interface defines business operations using Param/DTO/Response types, not controller-specific maps for new contracts.
- Service implementation owns validation, uniqueness, tenant/data-scope, persistence orchestration, external calls, workflow state changes, cache refresh, and response assembly.
- Mapper owns only persistence access and extends `BaseMapper<Entity>`.
- Feign/internal contracts use shared DTOs in `Forgex_Common` only when cross-service reuse is real.

## Finish Check

- Run compile/test for the touched backend module when practical.
- Update matching docs through `forgex-feature-self-review` if behavior changed.
