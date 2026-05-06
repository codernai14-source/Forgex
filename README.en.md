# Forgex

<div align="center" style="display:flex;flex-wrap:wrap;justify-content:center;align-items:center;gap:3rem;margin:10px auto 14px;width:100%;box-sizing:border-box;">
  <a href="https://gitee.com/coder_nai/forgex/stargazers" title="Gitee Stars" style="display:inline-block;margin:4px clamp(14px,3vw,32px);"><img src="https://gitee.com/coder_nai/forgex/badge/star.svg?theme=dark" alt="Gitee Stars"/></a>
  <a href="https://gitee.com/coder_nai/forgex/members" title="Gitee Forks" style="display:inline-block;margin:4px clamp(14px,3vw,32px);"><img src="https://gitee.com/coder_nai/forgex/badge/fork.svg?theme=dark" alt="Gitee Forks"/></a>
</div>

> Full-stack enterprise scaffold and business platform foundation for production manufacturing
> Documentation version: **V0.6.5**

Forgex is an enterprise-grade scaffold for manufacturing digitalization projects such as MES, WMS, QMS, equipment integration, workflow collaboration, reporting, and private deployment. It is not just a generic admin template for login, menus, and CRUD pages. Forgex packages the repetitive production concerns of enterprise systems into a reusable platform foundation: identity, tenants, permissions, dynamic tables, dictionaries, workflow, messaging, reports, import/export, file storage, integration, packaging, deployment, and upgrade support.

## Demo

- Online demo: <http://175.27.135.204:18080/login>

To try Forgex, register with one of the role-based invitation codes below and experience the system as a normal user, department manager, or system auditor.

- Normal user invitation code: `D83F9B1E`
- Department manager invitation code: `C40EDD46`
- System auditor invitation code: `948F2D80`

Forgex includes an external-facing user registration capability for enterprise use. Companies can issue invitation codes by role, position, or business scenario, allowing new employees, project members, or trial users to create accounts by themselves. The invitation code completes account creation, role binding, and basic permission activation, so administrators do not need to manually create every user one by one.

## What Makes It Different

Many teams need more than faster CRUD screens. After go-live, enterprise programs keep evolving: org/permission changes, exploding table/dictionary metadata, **consistent multi-language UI + server messages**, **tenant isolation**, **approvals and reporting**, **native mobile access**, **external integrations**, and repeatable **Windows / Linux private deployment** with upgrades. Forgex targets manufacturing-style MOM delivery and keeps these concerns inside one **microservices + web admin + Android skeleton + build/delivery engineering** story, reducing bespoke glue and repetitive platform work.

### Richer platform capabilities (i18n, multi-tenant, workflow, reports, Android, integration)

- **Deep internationalization**: web defaults for **Simplified & Traditional Chinese, English, Japanese, Korean**; aligns with backend `LangContext`, modular prompts (e.g. `fx_i18n_message`), and **multi-locale JSON + fallback chains** for dictionaries and dynamic table metadata—reducing “English UI, Chinese errors”; includes productized multi-language input patterns for maintainers.
- **Multi-tenant & multi-org**: tenant context propagation, row isolation, ignore rules, public configuration fallback and tenant-facing basics—fits groups, outsourced delivery or domain-separated SaaS.
- **Approval & workflow**: model, start, approve, todo/done queues, business callbacks—extends to QA, exceptions, labor reporting, purchasing-style sign-off paths common on the shop floor.
- **Reporting center**: categories, datasources, template management plus **UReport2 / JimuReport** hooks so analytics lives in the platform instead of scattered one-off scripts.
- **Native Android**: `Forgex_Mobile_Android` (Kotlin, Jetpack Compose, Hilt, Retrofit, DataStore) with dev/test/prod flavors and starter modules aligned to the **same gateway / auth semantics** for roaming tasks and approvals.
- **Integration hub**: third-party systems, authorization, API definitions, parameter mapping, synchronous/asynchronous outbound calls and call logs—less point-to-point glue to ERP/OA/etc.
- **Data-heavy UX engineering**: `FxDynamicTable`, per-user column preferences, dictionary rendering, Excel import/export with templates/providers, avatar/logo/attachment strategies across storage backends.
- **Messaging, jobs & auditing**: templated notices, inbox, SSE push; distributed job scheduling; login/operation trails and auditing field conventions.

This section summarizes *why Forgex* at capability level—see documentation links below for endpoints and operational detail.

### Private deployment paths (Windows / Linux)

Forgex ships **artifacts and scripts**, not “clone and figure out production yourself”:

- **Windows bundles**: `Forgex_Build` produces ZIP packages with web static assets, service JARs, **bundled Nginx**, **Windows JRE**, **control center**, **license request client**, Nacos config snapshots, database **init** and **upgrade** SQL—suited to intranet appliances or desktop servers.
- **Linux bundles**: tarball delivery, `install.sh`, Nginx templates and the same upgrade/database story—works with Docker Compose, systemd, or customer ops standards.

Follow the “Deployment” section in this README and [deployment docs](./Forgex_Doc/部署/README.md) for exact commands and paths.

### Documentation system (how it works × how to use)

`Forgex_Doc` is the official documentation hub, split by **frontend, backend, Android, database, deployment, and engineering standards**. Important frontend topics are often documented as paired **implementation + usage** guides so onboarding, QA, and ops share one source of truth. Start here: [documentation home](./Forgex_Doc/README.md).

### Deep internationalization (more than two locale files)

Forgex aims for a consistent language experience end-to-end—**after a user switches language, dictionary tags, table headers, and server-side prompts should follow the same language story** instead of showing English UI with Chinese error messages.

- **Web**: Vue I18n with multiple first-class locales, integrated with Ant Design Vue locale packs.
- **Backend context**: language is propagated through the request path; `fx_i18n_message` supports **module + prompt code + multi-locale JSON** with ordered fallbacks (current locale → primary language tag → Chinese, etc.).
- **Data & metadata**: dictionary values and platform configuration can carry **JSON i18n text**, which matters when the same master data must read well in five languages.
- **Authoring UX**: the console includes multi-language input patterns for business maintainers (see [i18n & layout docs](./Forgex_Doc/前端/国际化与布局/README.md)).
- **Legacy pages**: compatibility paths exist for historical hard-coded strings; new work should prefer standard `t(...)` and server prompt resolution.

Forgex designs **UI language, dictionary/platform copy, and server prompts** as one coherent story—suited to multinational operations and multilingual maintenance teams.

### Three ends: Web × microservices × Android

Forgex “three ends” means: **web admin**, **Java microservice cluster**, and **native Android client skeleton**, sharing auth/gateway semantics.

- **Web**: `Forgex_Fronted` (Vue 3 + TypeScript + Vite).
- **Backend**: `Forgex_Gateway` as the entry; services such as `Forgex_Auth`, `Forgex_Sys`, `Forgex_Workflow`, `Forgex_Integration`, `Forgex_Report`, etc. (see repository layout below).
- **Android**: `Forgex_Mobile_Android` with dev/test/prod flavors; starter modules for login, home, workflow, messaging, and profile.

Aligning the three ends makes **session/permission semantics, release boundaries, and on-site security policies** easier to govern, while keeping a clear surface for **native capabilities** such as push, offline behavior, and device integration.

### UI quality: a refined enterprise console

On top of Ant Design Vue, Forgex standardizes the “first impression” layer:

- **Theme system**: token-driven light/dark themes and semantic color ramps; brand theming is systematic (see `Forgex_Fronted/src/theme/README.md`).
- **Layouts**: multiple navigation modes for different information architectures.
- **Personal workspace**: draggable home widgets with sizing and per-user visibility—more like a real operator cockpit than a fixed demo dashboard.
- **Data-heavy screens**: `FxDynamicTable`, shared dialogs, dictionary rendering, imports/exports—optimized for throughput **and** visual consistency.

More detail: [frontend docs](./Forgex_Doc/前端/README.md) and [theme README](./Forgex_MOM/Forgex_Fronted/src/theme/README.md).

## Capabilities

| Area | Capabilities |
|---|---|
| Backend | Spring Cloud microservices, authentication, tenant isolation, dictionaries, workflow, reports, integration, messaging, audit logs |
| Frontend | Vue 3 admin console, dynamic tables, reusable dialogs, dictionary tags, department tree, import component, i18n, draggable homepage |
| Mobile | Android skeleton with Kotlin, Jetpack Compose, Hilt, Retrofit, DataStore |
| Delivery | Windows/Linux bundles, startup scripts, upgrade scripts, Nacos files, database scripts, license request client |

## Quick Start

### Requirements

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x
- RocketMQ 5.x

### Backend

```bash
cd Forgex_MOM/Forgex_Backend
mvn clean install
```

Start the required services:

- `Forgex_Gateway`
- `Forgex_Auth`
- `Forgex_Sys`
- `Forgex_Basic`
- `Forgex_Job`
- `Forgex_Workflow`
- `Forgex_Integration`
- `Forgex_Report`

### Frontend

```bash
cd Forgex_MOM/Forgex_Fronted
npm install
npm run dev
```

Default local addresses:

- Frontend: `http://localhost:5173`
- Gateway: `http://localhost:8000`

### Android

```bash
cd Forgex_MOM/Forgex_Mobile_Android
gradlew.bat :app:assembleDevDebug
```

## Deployment

Use `Forgex_Build` to create delivery packages:

```powershell
cd Forgex_Build
powershell -ExecutionPolicy Bypass -File build-all.ps1 -Version 0.6.5 -AllowDistFallback
```

Main outputs:

- `Forgex_Build/dist/windows/Forgex-Windows-Package-0.6.5.zip`
- `Forgex_Build/dist/linux/forgex-linux-bundle-0.6.5.tar.gz`

Database initialization scripts are under `Forgex_Doc/部署/数据库初始化脚本`. Upgrade SQL is packaged under `database-upgrade/`. Back up the database before running upgrade SQL.

## Documentation

- [Documentation home](./Forgex_Doc/README.md)
- [Development standards](./Forgex_Doc/开发规范/README.md)
- [Backend docs](./Forgex_Doc/后端/README.md)
- [Frontend docs](./Forgex_Doc/前端/README.md)
- [Android docs](./Forgex_Doc/安卓端/README.md)
- [Database docs](./Forgex_Doc/数据库/README.md)
- [Deployment docs](./Forgex_Doc/部署/README.md)

## Contact

- QQ: 3096821283
- Email: coder_nai@163.com

## License

[Apache 2.0](./LICENSE)
