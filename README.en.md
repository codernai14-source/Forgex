# Forgex

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

Many scaffolds focus on generating a CRUD page faster. Forgex focuses on what happens after an enterprise project goes live: permissions keep changing, table fields grow, customer sites need private deployment, manufacturing systems need integration, and upgrades may include SQL changes. These are the problems Forgex moves into the platform layer.

Forgex helps with:

- **Complex enterprise permissions**: authentication, roles, menus, role-user grants, dynamic routes, permission keys, and audit logs.
- **Fast-changing manufacturing pages**: `FxDynamicTable` moves columns, queries, sorting, dictionary rendering, and user column preferences into configurable table metadata.
- **Tenant and organization isolation**: tenant context, row-level isolation, tenant ignore rules, and public configuration fallback.
- **External system integration**: third-party systems, authorization, API configuration, parameter structures, parameter mapping, outbound calls, async execution, and call logs.
- **Workflow and reporting**: approval flows, task handling, callbacks, report categories, datasources, templates, UReport2, and JimuReport.
- **Private delivery**: Windows/Linux packages, embedded Nginx, Windows JRE, control center, license request client, Nacos configuration, and database initialization/upgrade scripts.

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
