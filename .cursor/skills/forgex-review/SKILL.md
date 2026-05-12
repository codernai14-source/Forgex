---
name: forgex-review
description: Forgex code review and troubleshooting skill. Use when reviewing code changes, investigating bugs, checking PRs, validating feature completeness, or looking for behavioral regressions across backend, frontend, database, permissions, i18n, dynamic table, workflow, and docs.
---

# Forgex Review

## Review Stance

Prioritize real defects, regressions, missing cross-layer links, and missing validation. Avoid style-only feedback unless it causes maintainability or behavior risk.

## High-Risk Checks

- Backend endpoint lacks permission or uses mismatched permission code.
- New Java class/method/field lacks effective Javadoc-style comments.
- Method body has no comments around non-trivial validation, tenant/data-scope, persistence, external call, workflow, cache, or response assembly blocks.
- Entity/Param/Service/Mapper does not follow Forgex inheritance/interface conventions without a local reason.
- Controller returns raw data, raw strings, maps, or hard-coded Chinese response messages instead of `R<T>` and prompt/i18n patterns.
- Frontend `v-permission` does not match backend/menu permissions.
- Frontend page bypasses `src\api` wrappers or creates raw axios calls.
- API contract changed but frontend API type or caller did not.
- Database field added but entity/DTO/VO/table config/docs were not updated.
- Dynamic table `field` differs from backend response or slot name.
- Annotation SQL was added instead of wrapper/XML.
- Tenant isolation, logical delete, data scope, or audit field behavior was skipped.
- User-visible text is hard-coded or only one locale was updated.
- Docs changed version metadata without explicit request.
- Behavior changed but matching `Forgex_Doc` page was not updated.

## Output

Lead with findings ordered by severity. Include file/line references when possible, then open questions, then a brief summary and validation notes.
