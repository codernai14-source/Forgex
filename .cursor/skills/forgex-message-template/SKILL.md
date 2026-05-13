---
name: forgex-message-template
description: Forgex message template and SSE guidance. Use when working with message templates, notification sending, SSE, unread counts, message APIs, template preview, recipients, backend prompts, MQ-based messaging, or frontend message UI.
---

# Forgex Message Template

## Source Docs

- Backend: `Forgex_Doc\后端\配置与审计\消息模板与SSE使用方式.md`
- Backend implementation: `Forgex_Doc\后端\配置与审计\消息模板与SSE实现逻辑.md`
- Frontend preview/recipient docs: `Forgex_Doc\前端\组件与页面\消息模板预览与接收人选择使用方式.md`
- Module mapping: `Forgex_Doc\开发规范\模块文档映射\README.md`

## Rules

- Reuse the existing template, recipient, message, unread count, and SSE APIs.
- Keep template code, variable names, backend prompt keys, frontend preview data, and docs aligned.
- Do not hard-code localized message text when template/i18n mechanisms exist.
- Consider tenant/public config behavior and whitelist behavior where cross-tenant messages are involved.
- Update usage and implementation docs when message behavior changes.
- Keep doc version metadata unchanged unless explicitly requested.
