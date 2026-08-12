# Forgex Backend Common Modularization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the monolithic `Forgex_Common` dependency with provider-owned API contracts and opt-in common capability modules so each service compiles and encrypts only the classes it uses.

**Architecture:** Preserve existing Java package names while moving source ownership into Maven modules. Domain API modules own Feign clients, shared synchronization payloads live in a DTO-only domain contract, and common runtime capabilities are separated from lightweight contracts. `Forgex_Common` remains a temporary aggregate during the move, then is removed after every service has explicit dependencies.

**Tech Stack:** Java 17, Maven multi-module reactor, Spring Boot 3.5.6, Spring Cloud OpenFeign, MyBatis-Plus, JUnit 5, PowerShell structural verification.

---

### Task 1: Add Executable Module-Boundary Verification

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/scripts/verify-common-module-boundaries.ps1`

- [ ] **Step 1: Write a failing structural verification script**

The script must require the target modules, reject direct service dependencies on `Forgex_Common`, reject forbidden heavy dependencies in Contract/Core POMs, and reject Java sources remaining in the aggregate module.

- [ ] **Step 2: Run the script and verify RED**

Run: `powershell -ExecutionPolicy Bypass -File scripts/verify-common-module-boundaries.ps1`

Expected: non-zero exit because target modules do not exist and services still depend on `Forgex_Common`.

### Task 2: Create the Lightweight Contract and Core Modules

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Contract/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Core/pom.xml`
- Move contract sources from `Forgex_Common/src/main/java/com/forgex/common/{web,exception,i18n}` where they have no runtime Web/database dependency
- Move lightweight sources from `Forgex_Common/src/main/java/com/forgex/common/{base,enums,tenant,util}` where their imports satisfy Core constraints

- [ ] **Step 1: Add both modules to the parent reactor and declare minimal dependencies**

Contract must not contain Spring Boot starters, Feign, MyBatis, Redis, MQ, POI, FastExcel, or BouncyCastle. Core may depend on Contract, Spring Core, Lombok, and proven lightweight libraries only.

- [ ] **Step 2: Move the minimal contract classes while preserving package names**

Move `R`, `StatusCode`, `BusinessException`, `I18nBusinessException`, `I18nPrompt`, `CommonPrompt`, and context/header primitives only after checking their imports.

- [ ] **Step 3: Compile the new modules**

Run: `mvn -pl Forgex_Common_Contract,Forgex_Common_Core -am -DskipTests compile`

Expected: `BUILD SUCCESS`.

### Task 3: Split DTO-Only Domain Contracts and Provider-Owned API Modules

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Domain_Contract/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Auth_Api/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Sys_Api/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Basic_Api/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Job_Api/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Workflow_Api/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Integration_Api/pom.xml`
- Move: `Forgex_Common/src/main/java/com/forgex/common/api/dto/*.java`
- Move: `Forgex_Common/src/main/java/com/forgex/common/api/feign/*.java`

- [ ] **Step 1: Create DTO-only Domain Contract**

Place cross-service synchronization DTOs in `Forgex_Domain_Contract`; it may depend on Common Contract/Core, Lombok, Jackson annotations, and Jakarta Validation but not OpenFeign or runtime starters.

- [ ] **Step 2: Move each Feign client to its provider module**

Use `@FeignClient(name=...)` as source truth: auth clients to Auth API, `forgex-sys` clients to Sys API, `forgex-basic` clients to Basic API, calendar reminder to Job API, workflow execution to Workflow API, and `forgex-integration` clients to Integration API.

- [ ] **Step 3: Move API helper implementation out of the contract surface**

Keep `AutoFillUsernameAspect`, `UserInfoService`, and examples in a runtime capability module; API modules contain only contracts.

- [ ] **Step 4: Compile all API modules**

Run: `mvn -pl Forgex_Auth_Api,Forgex_Sys_Api,Forgex_Basic_Api,Forgex_Job_Api,Forgex_Workflow_Api,Forgex_Integration_Api -am -DskipTests compile`

Expected: `BUILD SUCCESS` and no API-to-service implementation dependency.

### Task 4: Split Runtime Common Capabilities

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Web/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Data/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Crypto/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Excel/pom.xml`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Common_Infra/pom.xml`
- Move remaining sources and owned resources out of `Forgex_Common/src`
- Split: `Forgex_Common/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`

- [ ] **Step 1: Move Web behavior and Feign propagation to Common Web**

Move MVC interceptors, exception advice, response advice, Feign header propagation, and username-fill runtime helpers. Keep WebFlux-only Gateway concerns out of MVC registration.

- [ ] **Step 2: Move persistence behavior to Common Data**

Move base entities, MyBatis configuration, tenant/data-permission interceptors, entities, mappers, and database-backed configuration/service implementations.

- [ ] **Step 3: Move cryptography to Common Crypto**

Move `com.forgex.common.crypto` and `CryptoTransportConfig`; declare BouncyCastle only here. Preserve the existing field and file encryption contracts.

- [ ] **Step 4: Move Excel behavior to Common Excel**

Move Excel DTOs, entities, mappers, enums, providers, and services; declare POI, Commons Compress, and FastExcel only here.

- [ ] **Step 5: Move remaining opt-in infrastructure capabilities**

Move Redis, MQ, notification, license, audit, dictionary, and configuration runtime classes to Common Infra initially. Their existing dependencies stay out of Contract/Core and can be split further later without blocking the build-time fix.

- [ ] **Step 6: Move resources to their owners**

Move Spring auto-configuration metadata with its configuration class and `ip2region_v4.xdb` with IP lookup. Remove shared banner/logback resources from library modules if applications already own equivalents.

- [ ] **Step 7: Compile all common capability modules**

Run: `mvn -pl Forgex_Common_Web,Forgex_Common_Data,Forgex_Common_Crypto,Forgex_Common_Excel,Forgex_Common_Infra -am -DskipTests compile`

Expected: `BUILD SUCCESS`.

### Task 5: Convert `Forgex_Common` into a Temporary Aggregate

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Common/pom.xml`
- Remove moved content from: `Forgex_MOM/Forgex_Backend/Forgex_Common/src`

- [ ] **Step 1: Replace heavy direct dependencies with internal module dependencies**

The aggregate POM depends on all API and capability modules during migration but contains no Java implementation or resources.

- [ ] **Step 2: Compile the complete reactor through the compatibility aggregate**

Run: `mvn -DskipTests clean compile`

Expected: all services compile without duplicate-class errors.

### Task 6: Replace Every Service's Aggregate Dependency

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Sys/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Basic/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Gateway/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Job/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Workflow/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Report/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Integration/pom.xml`

- [ ] **Step 1: Derive explicit dependencies from imported packages and Spring runtime needs**

Each service adds only the API contracts and capability modules it imports or auto-configures. Gateway must not depend on Common Data, Crypto, Excel, or MVC Web.

- [ ] **Step 2: Compile each service with upstream dependencies**

Run one command per service: `mvn -pl <service> -am -DskipTests compile`.

Expected: each command returns `BUILD SUCCESS` before moving to the next service.

- [ ] **Step 3: Verify dependency trees**

Run: `mvn -pl Forgex_Gateway dependency:tree` and equivalent checks for narrow services.

Expected: no accidental Crypto/Excel/Data dependency where unused and no `Forgex_Common` dependency.

### Task 7: Remove the Compatibility Aggregate and Enforce Boundaries

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/pom.xml`
- Remove: `Forgex_MOM/Forgex_Backend/Forgex_Common/pom.xml`
- Modify: `Forgex_MOM/Forgex_Backend/scripts/verify-common-module-boundaries.ps1`

- [ ] **Step 1: Remove `Forgex_Common` from the reactor after its consumer count reaches zero**

Run: `rg -n '<artifactId>Forgex_Common</artifactId>' -g pom.xml`.

Expected: no output.

- [ ] **Step 2: Run structural verification and verify GREEN**

Run: `powershell -ExecutionPolicy Bypass -File scripts/verify-common-module-boundaries.ps1`

Expected: exit code 0 with every boundary check reported as passed.

### Task 8: Update Documentation and Run Full Verification

**Files:**
- Modify: `Forgex_Doc/后端/README.md`
- Modify: `Forgex_Doc/后端/公共能力/内部服务接口开放说明.md`
- Modify documentation files containing stale `Forgex_Common/src` paths

- [ ] **Step 1: Update architecture and usage documentation**

Describe provider-owned API dependencies and capability modules. Replace stale source paths with actual module owners without changing unrelated product documentation.

- [ ] **Step 2: Run clean full tests**

Run: `mvn clean test`

Expected: `BUILD SUCCESS`, zero test failures, and no duplicate classes.

- [ ] **Step 3: Re-run module-boundary verification and inspect Git changes**

Run: `powershell -ExecutionPolicy Bypass -File scripts/verify-common-module-boundaries.ps1`, `git diff --check`, and `git status --short`.

Expected: boundary script passes, diff check is clean, and only intended source/docs plus pre-existing untracked build output are present.

- [ ] **Step 4: Record external encryption validation limitation**

Report Maven module/class/JAR changes. Do not claim an encryption-time improvement without three measurements from the company's third-party encryption pipeline; if it scans `BOOT-INF/lib`, recommend narrowing the encryption target to `BOOT-INF/classes` or reusing encrypted dependency artifacts.
