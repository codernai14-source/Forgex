# Forgex Common/Admin Architecture Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Each task is committed and pushed independently after its validation gate.

**Goal:** Restructure the Forgex backend into a publishable `forgex-common` platform foundation, an `forgex-admin` governance layer, and independently deployable business services without changing Java package names, service IDs, API paths, or released artifact names.

**Architecture:** Add Maven aggregator layers and physically group modules under `forgex-common`, `forgex-admin`, and `forgex-business`, while preserving the existing module artifactIds during migration. Split common runtime capabilities from platform adapters: common modules expose stable contracts, context, web/data/crypto/Excel infrastructure, and extension points; explicit `forgex-admin-*-api` modules provide optional platform Feign contracts; admin services provide the implementations and governance endpoints. Business services depend on common capabilities and only the API contracts they call.

**Tech Stack:** Java 17, Maven 3.9.x, Spring Boot 3.5.6, Spring Cloud 2025.0.0, Spring Cloud Alibaba 2025.0.0.0-preview, OpenFeign, Nacos, MyBatis-Plus, Dynamic Datasource, Redis/Redisson, PowerShell delivery scripts.

**Spec:** The approved architecture in the conversation immediately preceding this plan: `forgex-common` is independently publishable; `forgex-admin-*` contains platform governance services and API contracts; business services can start independently and call platform services through explicit API dependencies.

## Global Constraints

- Preserve Java package names (`com.forgex.*`), Spring application names, context paths, ports, gateway route IDs, database names, and service artifact filenames.
- Preserve unrelated existing worktree changes; do not stage them in architecture commits.
- Do not make the common foundation implicitly scan or require `forgex-sys`, `forgex-auth`, or another admin implementation.
- API modules contain only DTOs, annotations, and Feign contracts; no database, heavy runtime starter, or service implementation dependency.
- Common starter defaults must be usable by a standalone business service; platform-specific adapters must be opt-in dependencies or admin-service-only dependencies.
- Every implementation batch must pass its focused checks before commit, then push the current branch to `origin`.
- No database schema changes are part of this migration.

## Target Layout

```text
Forgex_MOM/Forgex_Backend/
├── pom.xml
├── forgex-common/
│   ├── pom.xml
│   ├── forgex-common-bom/                 # new published dependency management POM
│   ├── Forgex_Common_Contract/
│   ├── Forgex_Common_Core/
│   ├── Forgex_Domain_Contract/
│   ├── Forgex_Common_Web/
│   ├── Forgex_Common_Data/
│   ├── Forgex_Common_Crypto/
│   ├── Forgex_Common_Excel/
│   ├── Forgex_Common_Infra/
│   └── Forgex_Common/                     # migration compatibility aggregate
├── forgex-admin/
│   ├── pom.xml
│   ├── forgex-admin-auth/                 # aggregate: API + service
│   │   ├── Forgex_Auth_Api/
│   │   └── Forgex_Auth/
│   ├── forgex-admin-sys/
│   │   ├── Forgex_Sys_Api/
│   │   └── Forgex_Sys/
│   ├── forgex-admin-basic/
│   │   ├── Forgex_Basic_Api/
│   │   └── Forgex_Basic/
│   ├── forgex-admin-job/
│   │   ├── Forgex_Job_Api/
│   │   └── Forgex_Job/
│   ├── forgex-admin-workflow/
│   │   ├── Forgex_Workflow_Api/
│   │   └── Forgex_Workflow/
│   ├── forgex-admin-integration/
│   │   ├── Forgex_Integration_Api/
│   │   └── Forgex_Integration/
│   ├── forgex-admin-report/
│   │   └── Forgex_Report/
│   └── forgex-admin-gateway/
│       └── Forgex_Gateway/
├── forgex-business/
│   └── README.md                          # extension boundary and example
└── scripts/
```

The existing `Forgex_Basic` remains in the admin aggregate in the first migration because it is currently shipped as a platform service and is referenced by the release manifest. Its long-term business classification is documented and can be moved later without changing its artifactId.

### Task 1: Freeze Baseline and Add the Plan

**Files:**
- Create: `.cursor/plans/2026-09-19-forgex-common-admin-architecture-upgrade.md`
- Test: repository status and current branch snapshot

- [ ] Record the current branch, remote, status, and module list in the task log.
- [ ] Confirm no existing modified file is under a path that will be moved; if one is, preserve it and include it explicitly in the move map.
- [ ] Commit only this plan file with message `docs: plan common admin architecture upgrade`.
- [ ] Push the current branch to `origin`.

### Task 2: Create Aggregator POMs and Move Common Modules

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/forgex-common/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/forgex-common/forgex-common-bom/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/pom.xml`
- Move: the eight existing common implementation modules, `Forgex_Domain_Contract`, and compatibility `Forgex_Common` under `forgex-common/`
- Modify: moved module parent `relativePath` values where required

**Interfaces:**
- `forgex-common/pom.xml` aggregates all common modules and does not change their artifactIds.
- `forgex-common-bom` manages `${forgex.version}` and imports the common contract/core/domain/web/data/crypto/excel/infra artifacts plus no admin service implementation.

- [ ] Move directories using filesystem moves so Git records renames rather than recreating source files.
- [ ] Add the common aggregator with modules in dependency order and a parent pointing to the root POM.
- [ ] Add the BOM as a `pom` artifact with dependency management for all common artifacts and a version property for the platform API line.
- [ ] Change the root `<modules>` list to include `forgex-common` only for this group and remove direct common child entries.
- [ ] Update moved POM parent paths and leave artifactIds unchanged.
- [ ] Run `mvn -q -pl forgex-common -am -DskipTests validate` and the common boundary script adapted to the new paths.
- [ ] Commit only common aggregator/POM/move changes with message `refactor: group common modules under forgex-common`.
- [ ] Push the current branch.

### Task 3: Create the Admin Aggregator and Preserve Service Coordinates

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/forgex-admin/pom.xml`
- Create: one aggregate POM under each `forgex-admin-*` directory
- Move: current Auth/Sys/Basic/Job/Workflow/Integration/Report/Gateway modules and their API modules under the admin group
- Modify: root and moved POM parent paths

- [ ] Add `forgex-admin/pom.xml` and one aggregate POM for each service family.
- [ ] Keep `Forgex_Auth`, `Forgex_Sys`, `Forgex_Basic`, `Forgex_Job`, `Forgex_Workflow`, `Forgex_Integration`, `Forgex_Report`, and `Forgex_Gateway` artifactIds and Spring metadata unchanged.
- [ ] Keep each `*_Api` artifactId unchanged while changing its physical location to the matching `forgex-admin-xxx` aggregate.
- [ ] Make the root POM aggregate `forgex-common` and `forgex-admin`, with no direct leaf module entries.
- [ ] Run the full Maven reactor validation with tests skipped first, then focused module tests where available.
- [ ] Commit only admin aggregator/move/POM changes with message `refactor: group platform modules under forgex-admin`.
- [ ] Push the current branch.

### Task 4: Make Common Runtime Publishable and Platform Adapters Explicit

**Files:**
- Create or modify: common starter and auto-configuration metadata under `forgex-common`
- Modify: `Forgex_Common_Infra/pom.xml`, Feign configuration, audit recorder, user-info services, encode-rule integration, and related tests
- Modify: admin API POMs and admin service POMs
- Create: platform adapter module(s) only where the existing implementation cannot remain in common without an admin dependency

**Interfaces:**
- Common extension points: `UserDirectory`, `AuditPublisher`, and `EncodeRuleProvider` (exact names may follow existing local naming after source inspection).
- Admin adapters: Feign-backed implementations activated only when the matching admin API dependency/configuration is present.
- Common starter: opt-in or conditional auto-configuration that does not instantiate admin Feign clients by default.

- [ ] Inventory every Java import and bean in common infra that references `Forgex_Sys_Api`, `Forgex_Auth_Api`, a `com.forgex.sys` implementation, or a platform database table.
- [ ] Extract interfaces into common contract/core or a small common SPI module with no platform implementation dependency.
- [ ] Move Feign-backed user lookup, operation-log remote recording, and encode-rule remote integration behind conditional admin adapters.
- [ ] Make `@EnableFeignClients` scan explicit client packages from the consuming service instead of globally scanning platform clients from common.
- [ ] Remove `Forgex_Sys` implementation dependencies from `Forgex_Basic` and `Forgex_Job`; replace them with API contracts or SPI calls.
- [ ] Add a standalone sample configuration/test application proving a business service can start with common starter and no admin service classpath.
- [ ] Run dependency-tree checks proving common artifacts do not transitively include admin service implementations.
- [ ] Commit with message `refactor: decouple common runtime from admin services` and push.

### Task 5: Update Build, Release, IDE, and Documentation Paths

**Files:**
- Modify: `Forgex_Build/manifest/services.yml`
- Modify: `Forgex_Build/collect-artifacts.ps1`, installer upgrade/install mappings, license diagnostics paths, and any backend build scripts
- Modify: `.idea` metadata only if generated project metadata requires it; do not edit unrelated workspace change records
- Modify: common module guide, internal service API guide, backend module map, and deployment notes
- Create: `forgex-business/README.md` with the company business-service template and dependency examples

- [ ] Change all physical module paths to the new directories while preserving artifact names and service IDs.
- [ ] Ensure packaging searches the new service target directories and still collects exactly eight runnable service JARs.
- [ ] Update the common boundary verifier to resolve grouped paths and to validate admin implementation dependencies separately from publishable common dependencies.
- [ ] Document the dependency matrix: pure common, common starter, optional admin API, and admin service implementation.
- [ ] Document service-to-service headers, Feign usage, internal endpoint rules, and standalone startup prerequisites.
- [ ] Commit with message `build: align delivery paths with common admin grouping` and push.

### Task 6: Verify Full Reactor, Packaging, and Independent Business Startup

**Files:**
- Modify only failing tests/configuration discovered during verification.
- Create: focused architecture verification scripts/tests if an existing test location is not sufficient.

- [ ] Run `mvn -q -DskipTests compile` from `Forgex_MOM/Forgex_Backend`.
- [ ] Run `powershell -ExecutionPolicy Bypass -File scripts/verify-common-module-boundaries.ps1` from the backend root.
- [ ] Run `mvn -q -pl forgex-common -am test` and record any baseline failures separately from migration regressions.
- [ ] Run the relevant backend packaging path and verify all eight JARs are collected from the new directories.
- [ ] Run a dependency-isolation check for a standalone business sample: common starter present, no admin service artifact present, no platform implementation bean instantiated.
- [ ] Run `git diff --check`, inspect staged paths, and verify no unrelated pre-existing change is staged.
- [ ] Commit verification fixes separately, push, and report exact validation results and any remaining runtime limitations.

## Commit and Push Rules

Each task commit must be created with explicit paths, for example:

```powershell
git add -- <architecture paths for this task>
git diff --cached --check
git diff --cached --name-only
git commit -m "<task message>"
git push origin develop-jgUpdate
```

Never use `git add -A` or `git commit -am` because the worktree contains unrelated user changes.

