# System Branding, Icon Controls, and Message Voice Implementation Plan

> **For agentic workers:** Execute inline in this session; no commit is requested.

**Goal:** Make browser tab branding configurable, expand the common icon picker with color/size controls, and add optional browser speech announcements for incoming messages.

**Architecture:** Extend the existing system basic configuration contract with tab title/favicon fields and apply them from the main layout. Keep icon rendering centralized in `FxIcon`, accepting presentation props while preserving Ant Design and Iconify resolution. Reuse the existing SSE/message event pipeline and add a small speech utility plus a top-header toggle/popover.

**Tech Stack:** Vue 3, TypeScript, Vite, Ant Design Vue, `@iconify/vue`, browser `speechSynthesis`.

---

### Task 1: Pure helpers and regression tests

**Files:**
- Create: `Forgex_MOM/Forgex_Fronted/src/utils/siteBranding.ts`
- Create: `Forgex_MOM/Forgex_Fronted/src/utils/messageSpeech.ts`
- Create: `Forgex_MOM/Forgex_Fronted/tests/branding-icons-speech.test.mjs`

- [ ] Add node-runnable tests for title/favicon normalization, icon size/color normalization, and speech message composition/enable behavior.
- [ ] Run the test file and confirm the expected red failures.
- [ ] Implement the smallest helpers that satisfy the tests.
- [ ] Run the test file again and confirm green.

### Task 2: Configurable site tab branding

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/api/system/config.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/views/system/config/index.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/layouts/MainLayout.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/zh-CN/system/config.ts`

- [ ] Add `browserTitle` and `browserIcon` to the existing system config defaults, normalization, reset, and form.
- [ ] Reuse `AvatarUpload` for the browser icon and show a preview beside the title input.
- [ ] Apply title and favicon after config load and after save without reloading the page.

### Task 3: Expand and style icon library

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/utils/icon.ts`
- Modify: `Forgex_MOM/Forgex_Fronted/src/components/common/FxIcon.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/views/home/component-config/index.vue`

- [ ] Add common open-source Lucide preset names across navigation, business, communication, and status groups.
- [ ] Pass icon color and size from the component configuration into `FxIcon` and preview the result live.
- [ ] Keep existing icon names and fallback behavior backward compatible.

### Task 4: Header voice announcement control

**Files:**
- Modify: `Forgex_MOM/Forgex_Fronted/src/components/Notification/MessageNotification.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/layouts/components/AppHeader.vue`
- Modify: `Forgex_MOM/Forgex_Fronted/src/locales/zh-CN/layout.ts`

- [ ] Add a reusable speech controller using browser `speechSynthesis`, with enabled state persisted in local storage and graceful unsupported-browser behavior.
- [ ] Add a compact speaker button/popover to the header and connect it to the message notification event path.
- [ ] Announce title, content, and a timely-processing reminder, while avoiding speech when disabled or when the payload is empty.

### Task 5: Verification

- [ ] Run the focused node tests.
- [ ] Run `npm run build` from `Forgex_MOM/Forgex_Fronted`.
- [ ] Inspect the final diff and confirm unrelated worktree files remain untouched.
