# Forgex

> Enterprise application scaffold and business platform preview edition  
> Current version: **Preview V0.4.0**

## Overview

Forgex is a full-stack enterprise scaffold built for multi-module business systems. It combines a Vue-based admin console, Spring Cloud microservices, and an Android client skeleton so teams can start from a reusable platform instead of rebuilding common infrastructure from scratch.

The current repository already includes:

- Web admin console
- Backend microservice suite
- Android mobile project skeleton

---

## Highlights in Preview V0.4.0

### Enterprise authentication and multi-tenant foundation

- Account login, registration, logout, password reset, and admin secure verification
- Tenant selection, tenant preference persistence, tenant isolation, and initialization flow
- Image captcha and slider captcha support
- Public-key endpoint for encrypted credential transmission
- Social login callback flow for WeChat and DingTalk

### Complete system administration toolkit

- User, role, department, position, menu, module, tenant, and dictionary management
- Role-to-user, role-to-position, role-to-department, and menu authorization management
- System config, login logs, operation logs, and online user monitoring
- Invite code management, tenant message whitelist, and tenant ignore configuration
- Profile maintenance and personal homepage layout persistence

### Workflow module is already functional

- Workflow definition management with draft editing and publishing
- Graph-based node configuration flow
- Start, approve, reject, and cancel execution APIs
- My initiated, pending, processed, and CC task views
- Workflow dashboard summary and analytics endpoints
- Callback registration and unregistration for business integration

### Reporting center is available

- Report category management
- Report datasource management with connectivity testing
- Report template save, content update, import, and export
- Integrated with **UReport2** and **JimuReport** for future reporting expansion

### Dynamic tables, Excel config, and code generation

- Generic table configuration center
- User-specific table column preferences
- Excel import/export configuration and template download
- Code generation preview and package download
- Encode rule management and code generation by rule

### Internationalization and messaging

- Supported languages: `zh-CN`, `zh-TW`, `en-US`, `ja-JP`, `ko-KR`
- Language type management, default language selection, import, and template download
- End-to-end i18n message flow for backend and frontend
- Internal messaging, message templates, template-based sending, and user-targeted sending
- SSE message stream support for real-time notifications

### Dashboard and visualization improvements

- System dashboard with statistics, recent operation logs, and recent login logs
- Server info, service memory usage, module memory pools, and JVM heap visibility
- Recent updates improved CPU and memory presentation accuracy
- Frontend visualization stack includes ECharts, ApexCharts, and Three.js

### Android project skeleton is in place

- Modular Android structure with `app`, `core/*`, and `feature/*`
- Login, tenant selection, and menu route bootstrap connected
- Feature modules for `auth`, `home`, `workflow`, `message`, and `profile`
- Environment support for `dev / test / prod` and runtime device detection for mobile/tablet

---

## Repository Structure

```text
Forgex_MOM
├─ Forgex_Backend
│  ├─ Forgex_Auth
│  ├─ Forgex_Sys
│  ├─ Forgex_Basic
│  ├─ Forgex_Common
│  ├─ Forgex_Gateway
│  ├─ Forgex_Job
│  ├─ Forgex_Workflow
│  └─ Forgex_Report
├─ Forgex_Fronted
└─ Forgex_Mobile_Android
```

---

## Tech Stack

### Backend

- Java 17
- Spring Boot 3.5.6
- Spring Cloud 2025.0.0
- Spring Cloud Alibaba 2025.0.0.0-preview
- Sa-Token 1.44.0
- MyBatis-Plus 3.5.14
- Dynamic Datasource 4.3.1
- Snail-Job 1.8.1
- FastExcel 1.3.0
- springdoc-openapi 2.6.0
- UReport2 2.2.10
- JimuReport 1.9.0

### Frontend

- Vue 3.5.26
- TypeScript 5.6.3
- Vite 5.4.3
- Ant Design Vue 4.2.6
- Pinia 3.0.4
- Vue Router 4.3.0
- Vue I18n 9.14.0
- Formily 2.3.7
- Vue Flow
- ECharts 6
- ApexCharts 5
- Three.js 0.182

### Mobile

- Kotlin
- Jetpack Compose
- Hilt
- Retrofit
- DataStore

---

## Implemented Modules

### Backend

- `Forgex_Auth`: authentication, tenant selection, captcha, registration, social login, internal permission APIs
- `Forgex_Sys`: system administration, dashboard, i18n, messaging, Excel config, encode rules, common tables
- `Forgex_Workflow`: workflow configuration, graph editor, execution, task center, analytics, callbacks
- `Forgex_Report`: report categories, datasources, templates
- `Forgex_Gateway`: unified gateway entry
- `Forgex_Job`: distributed job scheduling integration
- `Forgex_Common`: shared infrastructure and utilities
- `Forgex_Basic`: base business extension module

### Frontend

- Auth: login, registration, initialization wizard
- Workspace: personal homepage, profile, dashboards
- System admin: user, role, department, position, menu, module, tenant, dictionary
- Platform features: i18n language types, invite codes, message center, message templates
- Data tools: table config, user table config, Excel import/export config, encode rules
- Workflow: configuration, node editor, execution start, my tasks, workflow dashboard
- Report center: datasource management, designer, preview, template management

### Android

- `feature:auth`
- `feature:home`
- `feature:workflow`
- `feature:message`
- `feature:profile`

---

## Quick Start

### Requirements

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

### Clone

```bash
git clone <your-repository-url>
cd forgex
```

### Backend

```bash
cd Forgex_MOM/Forgex_Backend
mvn clean install
```

Start the required services such as:

- `Forgex_Gateway`
- `Forgex_Auth`
- `Forgex_Sys`
- `Forgex_Workflow`
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

---

## Release Notes

### Preview V0.4.0 · 2026-04

- Consolidated Web, Backend, and Android project structure
- Expanded system admin, authentication, multi-tenant, and i18n capabilities
- Completed the main workflow loop from configuration to execution
- Added reporting center foundations for categories, templates, and datasources
- Improved dashboard visibility for CPU, JVM, and memory usage
- Landed messaging, invite code, tenant initialization, and whitelist features
- Continued enhancement of dynamic tables, Excel config, code generation, and encode rules

---

## License

[Apache 2.0](./LICENSE)

## Contact

- QQ: 3096821283
- Email: coder_nai@163.com
