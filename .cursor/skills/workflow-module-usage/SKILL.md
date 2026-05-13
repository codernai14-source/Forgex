---
name: workflow-module-usage
description: Forgex workflow module usage guide. Use when developing approval flows, workflow task configuration, approval start/handle/callback logic, workflow Feign integration, message notifications, or workflow-related backend/frontend docs.
---

# Forgex Workflow Module

## Source Docs

- `Forgex_Doc\后端\模块专题\工作流使用方式.md`
- `Forgex_Doc\后端\模块专题\工作流实现逻辑.md`
- `Forgex_Doc\后端\模块专题\工作流.md`
- `Forgex_Doc\开发规范\模块文档映射\README.md`

## Rules

- Reuse the existing workflow service/client/DTO contracts before creating new approval abstractions.
- Keep business entity approval status fields aligned with workflow callback outcomes.
- Approval start APIs should validate business state and permission before starting workflow.
- Callback handlers must be idempotent where possible and should not trust incomplete callback payloads.
- Use existing message template/notification patterns for approval messages.
- Document business-specific approval behavior in the matching module docs.

## Finish Check

- Permission code exists for approval operations.
- Frontend action visibility matches backend permission and status.
- Workflow callback updates business state correctly.
- Docs updated without version metadata changes.
