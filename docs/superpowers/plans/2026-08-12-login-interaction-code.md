# Login Interaction Code Implementation Plan

> **For agentic workers:** Implement inline in the current workspace. Do not create commits unless the user explicitly requests one.

**Goal:** Require a five-minute, account-and-terminal-bound, single-use Redis interaction code before initial tenant selection while preserving authenticated tenant switching.

**Architecture:** Add a focused Redis-backed interaction-code service to the Auth module and return a dedicated login result DTO. Gate initial tenant selection through that service, while allowing an existing same-account Sa-Token session to switch tenants. Propagate the contract through the Web and Android clients and remove credential-free Web session recovery.

**Tech Stack:** Java 17, Spring Boot, Sa-Token, Redisson, JUnit 5/Mockito, Vue 3/TypeScript/Vite, Kotlin/Retrofit/Compose.

---

### Task 1: Interaction-code service

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/domain/dto/LoginInteractionContext.java`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/service/LoginInteractionCodeService.java`
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/test/java/com/forgex/auth/service/LoginInteractionCodeServiceTest.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/pom.xml`

- [ ] Add failing tests proving a created code receives a five-minute TTL, matching context is atomically consumed once, and account/user/terminal mismatches are rejected without consumption.
- [ ] Run `mvn -pl Forgex_Auth -Dtest=LoginInteractionCodeServiceTest test` and verify the tests fail because the service does not exist.
- [ ] Implement UUID generation, SHA-256 Redis key derivation, JSON context storage, and `RBucket.compareAndSet(currentValue, null)` consumption.
- [ ] Re-run the focused test and verify it passes.

### Task 2: Backend login response contract

**Files:**
- Create: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/domain/vo/LoginResultVO.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/controller/AuthController.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/service/AuthService.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/service/impl/AuthServiceImpl.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/strategy/login/LoginStrategy.java`
- Modify: both account/password login strategy implementations.

- [ ] Add a failing contract test showing successful credential validation returns `interactionCode` plus `tenants`.
- [ ] Change login method generics from `List<TenantVO>` to `LoginResultVO` through Controller, Service, and strategies.
- [ ] Issue the interaction code only after password and captcha validation succeed, binding it to the resolved user ID, account, and terminal.
- [ ] Run the focused backend tests.

### Task 3: Initial tenant authorization gate

**Files:**
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/domain/param/TenantChoiceParam.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/enums/AuthPromptEnum.java`
- Modify: `Forgex_MOM/Forgex_Backend/Forgex_Auth/src/main/java/com/forgex/auth/service/impl/AuthServiceImpl.java`

- [ ] Add failing tests for missing, invalid, mismatched, and replayed interaction codes, plus existing same-account and different-account session cases.
- [ ] Add `interactionCode` to the request DTO and a localized authentication-expired prompt.
- [ ] After validating the user/tenant binding, require and atomically consume a matching code unless `StpUtil` already holds the same account.
- [ ] Reject an existing session whose login ID differs from the requested account.
- [ ] Run Auth tests and `mvn -pl Forgex_Auth -am -DskipTests compile`.

### Task 4: Web contract and login flow

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/api/auth/login.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/views/auth/login/index.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/api/http.ts`

- [ ] Define `LoginResult` and `TenantChoiceRequest` types centrally in the auth API module.
- [ ] Store the returned interaction code only in the login component's in-memory state and pass it to the first tenant-selection request.
- [ ] Clear the code after tenant selection and reset the login flow when selection reports authentication expiry.
- [ ] Remove the interceptor branch that calls `/auth/choose-tenant` with only stored account and tenant ID.
- [ ] Run `npm run build`.

### Task 5: Android contract and state flow

**Files:**
- Create: `Forgex_MOM/Forgex_Mobile_Android/core/network/src/main/java/com/forgex/mobile/core/network/model/auth/LoginResult.kt`
- Modify: `Forgex_MOM/Forgex_Mobile_Android/core/network/src/main/java/com/forgex/mobile/core/network/api/AuthApi.kt`
- Modify: `Forgex_MOM/Forgex_Mobile_Android/core/network/src/main/java/com/forgex/mobile/core/network/model/auth/TenantChoiceRequest.kt`
- Modify: `Forgex_MOM/Forgex_Mobile_Android/feature/auth/src/main/java/com/forgex/mobile/feature/auth/data/AuthRepository.kt`
- Modify: `Forgex_MOM/Forgex_Mobile_Android/feature/auth/src/main/java/com/forgex/mobile/feature/auth/data/AuthRepositoryImpl.kt`
- Modify: `Forgex_MOM/Forgex_Mobile_Android/feature/auth/src/main/java/com/forgex/mobile/feature/auth/AuthViewModel.kt`

- [ ] Change the Retrofit login response to `LoginResult` and map it to a feature-level result carrying tenants and interaction code.
- [ ] Keep the code only in `AuthUiState`, require it before first tenant confirmation, and pass it through repository to `TenantChoiceRequest`.
- [ ] Clear it on success or selection failure so retry requires credential login.
- [ ] Run `gradlew.bat :core:network:compileDebugKotlin :feature:auth:compileDebugKotlin`.

### Task 6: Final verification

- [ ] Run all focused Auth tests and backend compilation.
- [ ] Run the Web production build.
- [ ] Run Android network and auth feature compilation.
- [ ] Inspect `git diff --check`, `git status --short`, and the final diff for accidental unrelated changes or exposed interaction codes.
- [ ] Do not stage or commit any file.
