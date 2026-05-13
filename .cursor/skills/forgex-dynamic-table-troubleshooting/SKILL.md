---
name: forgex-dynamic-table-troubleshooting
description: Forgex dynamic table troubleshooting skill. Use for FxDynamicTable columns not showing, missing fields, column setting save failures, table-code mismatches, user preference merge issues, backend response gaps, or field-chain inconsistencies.
---

# Forgex Dynamic Table Troubleshooting

## Fast Diagnosis

Most table display bugs are field-chain bugs. Check in this order:

1. Correct `table-code`.
2. Config row exists and `visible`/status allows display.
3. `field` exactly matches backend response.
4. Backend query/DTO/VO/service mapping returns the field.
5. Frontend API type and custom slot match the same field.
6. User column preference is not hiding or overriding the column.
7. Column settings save uses the complete base column list.

## Resources

- Use `dynamic-table-config\troubleshooting.md` for the full checklist.
- Use `dynamic-table-config\reference.md` for implementation details.
- Use `Forgex_Doc\前端\配置驱动页面\FxDynamicTable使用方式.md`.
- Use `Forgex_Doc\开发规范\规范文档\公共表格组件使用说明与实现逻辑.md`.

## Report Format

When giving a fix or diagnosis, include:

- broken link in the chain
- exact file/config row involved
- minimal fix
- validation performed
