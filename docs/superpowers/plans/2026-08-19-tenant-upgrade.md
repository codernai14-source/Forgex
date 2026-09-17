# Tenant Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enforce the main-tenant-only parent hierarchy, make tenant initialization atomic, notify the parent administrator with generated credentials, and improve the creation dialog.

**Architecture:** Keep tenant creation in `SysTenantServiceImpl` as the transaction boundary. Make initialization synchronous and return the generated administrator credentials, then send a template message in the parent tenant context after initialization succeeds. Keep the existing task/progress records for successful initialization and constrain the frontend selector to the main tenant.

**Tech Stack:** Java 17, Spring Boot, MyBatis-Plus, Vue 3, TypeScript, Ant Design Vue, MySQL 8.

---

### Task 1: Parent hierarchy validation

**Files:**
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/domain/dto/tenant/SysTenantSaveParam.java`
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysTenantServiceImpl.java`
- Test `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/test/java/com/forgex/sys/service/impl/SysTenantServiceImplTest.java`

- [ ] Write tests proving non-main tenants require a parent and the parent must be a non-deleted main tenant.
- [ ] Run the focused test and observe the expected failure.
- [ ] Add the parent field to the save contract and enforce the rules in the service for create and update.
- [ ] Run the focused test and verify it passes.

### Task 2: Atomic initialization and credential result

**Files:**
- Create `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/domain/dto/tenant/TenantInitResult.java`
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/ITenantInitService.java`
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/TenantInitServiceImpl.java`
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysTenantServiceImpl.java`

- [ ] Add a result object carrying administrator user ID, account, and initial password.
- [ ] Remove asynchronous execution from the initialization entry point so it joins the tenant creation transaction.
- [ ] Ensure every initialization failure is rethrown after task status logging, allowing the outer transaction to roll back the tenant and all initialized rows.
- [ ] Preserve the successful task/progress updates and return generated credentials.
- [ ] Compile the Sys module and run existing initialization/message tests.

### Task 3: Parent administrator notification

**Files:**
- Modify `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/SysTenantServiceImpl.java`
- Create `Forgex_Doc/数据库/脚本与修复/2026-08-19/20260819_tenant_create_message_template.sql`
- Create `Forgex_Doc/数据库/脚本与修复/2026-08-19/20260819_tenant_create_message_template_rollback.sql`

- [ ] Resolve enabled parent administrators and send template `SYS_TENANT_CREATED` with tenant name, code, account, and password.
- [ ] Temporarily use the parent tenant context for message persistence and restore the caller context in `finally`.
- [ ] Add idempotent MySQL seed and rollback SQL for the template and internal content.
- [ ] Ensure message failure is logged without converting a successfully initialized tenant into a half-created state.

### Task 4: Creation dialog and selector

**Files:**
- Modify `Forgex_MOM/Forgex_Fronted/src/views/system/tenant/index.vue`
- Modify `Forgex_MOM/Forgex_Fronted/src/api/system/tenant.ts` if request typing needs adjustment
- Modify `Forgex_MOM/Forgex_Fronted/src/styles/views/system/tenant/index.less`

- [ ] Load only the main tenant for the parent selector.
- [ ] Make parent required for non-main creation, clear it for main tenants, and keep it read-only during edit.
- [ ] Rework the dialog into visually separated identity, hierarchy, and status sections with responsive spacing.
- [ ] Build the frontend and verify the tenant page compiles.

### Task 5: Verification

- [ ] Run focused backend tests.
- [ ] Run `mvn -q -DskipTests compile` in `Forgex_MOM/Forgex_Backend`.
- [ ] Run `npm run build` in `Forgex_MOM/Forgex_Fronted`.
- [ ] Inspect `git diff` and confirm no commit was created.
