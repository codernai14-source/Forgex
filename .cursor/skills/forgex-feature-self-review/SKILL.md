---
name: forgex-feature-self-review
description: Forgex feature self-review and documentation sync skill. Use before completing any Forgex bug fix, feature update, code generation change, supplier master data change, dialog behavior change, backend/frontend behavior change, API contract update, database script, or documentation update. Ensures matching feature docs are updated while document versions are not changed unless explicitly requested.
---

# Forgex Feature Self Review

## Required Review

Before finishing a Forgex change:

1. Identify affected modules and docs through `Forgex_Doc\开发规范\模块文档映射\README.md`.
2. Confirm the implementation follows the relevant frontend/backend/database/deploy skill.
3. Check cross-layer consistency:
   - API path
   - permission code
   - route/menu key
   - database column
   - entity/DTO/VO/Param field
   - frontend API type
   - dynamic table field
   - dictionary code
   - i18n key or backend prompt enum
   - documentation text
4. Update the corresponding formal doc when behavior, API, UI, schema, config, workflow, integration, or usage changed.
5. Do not change document `版本`, `version`, or update-date metadata unless the user explicitly requests a version change.
6. Run the smallest practical validation for the changed layer.
7. Report validation that could not be run.

## Documentation Targets

- Frontend UI/API behavior: `Forgex_Doc\前端\...`
- Backend capability behavior: `Forgex_Doc\后端\...`
- Database design or upgrade scripts: `Forgex_Doc\数据库\...`
- Deployment, Nacos, middleware, authorization: `Forgex_Doc\部署\...`
- Standards or process changes: `Forgex_Doc\开发规范\...`

## Version Discipline

Feature updates should update matching docs without changing version metadata. This includes title metadata such as:

- `版本`
- `version`
- `更新时间`
- `updated`

Only change those fields when the user explicitly asks for a version/date bump.

## Final Response Checklist

Mention:

- code areas changed
- docs changed, if any
- that doc versions were left unchanged when docs were updated
- validation command and result, or why validation was skipped
