# Menu Permission Live Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make Web menu and button permissions take effect without re-login through SSE-triggered silent refresh plus a manual refresh command.

**Architecture:** Publish a tenant-scoped `permission-changed` SSE event after permission-related transactions commit. The existing MainLayout SSE connection calls one deduplicated refresh function that fetches `/sys/menu/routes`, replaces dynamic route records and permission state, and redirects only when the current page was revoked. The same function is exposed through a user-menu command.

**Tech Stack:** Spring Boot 3.5 / Java 17 / Spring transaction synchronization / Vue 3 / TypeScript / Pinia / Vue Router / existing SseEmitterService.

---

### Task 1: Backend permission-change event publisher

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/PermissionChangeNotifier.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysRoleMenuServiceImpl.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysUserRoleServiceImpl.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysMenuServiceImpl.java`

- [x] Add a notifier that sends `permission-changed` through `SseEmitterService.sendToTenant` after commit, including tenantId, reason, and timestamp.
- [x] Call the notifier once from role-menu grant/delete, user-role save, and Web menu add/update/delete/batch-delete methods.
- [x] Keep notifications tenant-scoped and avoid publishing when tenantId is absent.

### Task 2: Frontend SSE and refresh coordinator

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/hooks/useSse.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/api/system/route.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/router/index.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/stores/permission.ts`

- [x] Dispatch named SSE events to `onEvent`, including custom event names.
- [x] Add a silent route request variant and an exported `refreshDynamicRoutes` function that updates buttons, modules, routes, and route records atomically from the frontend perspective.
- [x] Make cache restoration reject malformed/empty permission snapshots without clearing a valid in-memory snapshot.

### Task 3: Automatic and manual UI refresh

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/layouts/components/AppHeader.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/layouts/MainLayout.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/zh-CN/layout.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/en-US/layout.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/ja-JP/layout.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/ko-KR/layout.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/zh-TW/layout.ts`

- [x] Add a user-menu command and loading state for manual permission refresh.
- [x] Listen for `permission-changed` in MainLayout and invoke the deduplicated silent refresh.
- [x] Preserve the current route when still authorized, redirect revoked pages to `/workspace/home`, and remove revoked tabs.
- [x] Add localized labels and success/failure messages.

### Task 4: Verification

- [x] Run targeted TypeScript checks/build from `Forgex_MOM/Forgex_Fronted` with `npm run build`.
- [x] Run backend compile from `Forgex_MOM/Forgex_Backend` with `mvn -q -DskipTests compile`.
- [x] Review the diff for unrelated dirty-worktree changes and report any baseline failures separately.
