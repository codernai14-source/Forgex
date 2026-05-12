---
name: forgex-self-check
description: Forgex development self-check skill. Use before finishing Forgex changes to verify backend/frontend/database/docs consistency, permissions, i18n, dynamic table fields, MyBatis-Plus usage, comments, tests, and documentation sync.
---

# Forgex Self Check

## Checklist

Before finishing:

- Relevant companion skill was used.
- Existing module patterns were followed.
- Backend route, permission, DTO/VO/Param, service, mapper, and entity are aligned.
- Java classes, methods, and fields have effective Javadoc-style comments.
- Method bodies include concise comments for each meaningful business logic segment.
- Normal entities extend `BaseEntity`; normal page/query params extend `BaseGetParam`; services/mappers follow `IService`, `ServiceImpl`, and `BaseMapper` conventions unless the module intentionally differs.
- Controller returns `R<T>` and uses prompt/i18n response helpers or i18n business exceptions instead of hard-coded Chinese messages.
- Frontend API type, page usage, `v-permission`, dynamic table field, and locale keys are aligned.
- Frontend pages call `src\api` wrapper functions and do not create raw axios calls or scattered endpoint strings.
- API wrappers choose `http`, `httpSuccess`, or `silentHttp` according to operation type.
- Database fields follow `Forgex_Doc\开发规范\规范文档\数据库字段统一规范文档.md`.
- No annotation SQL was introduced.
- Dynamic table changes keep `field` names identical through the full chain.
- User-visible frontend text has five locale entries when applicable.
- Backend prompt/i18n keys are present when used.
- Feature docs were checked through `Forgex_Doc\开发规范\模块文档映射\README.md`.
- Document version metadata was not changed unless requested.
- Smallest practical validation was run or explicitly reported as not run.

## Docs

Use current `Forgex_Doc` paths, not old `doc/开发规范` paths.
