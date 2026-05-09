# Forgex

<div align="center" style="display:flex;flex-wrap:wrap;justify-content:center;align-items:center;gap:3rem;margin:10px auto 14px;width:100%;box-sizing:border-box;">
  <a href="https://gitee.com/coder_nai/forgex/stargazers" title="Gitee Stars" style="display:inline-block;margin:4px clamp(14px,3vw,32px);"><img src="https://gitee.com/coder_nai/forgex/badge/star.svg?theme=dark" alt="Gitee Stars"/></a>
  <a href="https://gitee.com/coder_nai/forgex/members" title="Gitee Forks" style="display:inline-block;margin:4px clamp(14px,3vw,32px);"><img src="https://gitee.com/coder_nai/forgex/badge/fork.svg?theme=dark" alt="Gitee Forks"/></a>
</div>

> 面向企业级生产制造行业的全栈开发脚手架与业务中台底座
> 文档版本：**V0.6.5**

Forgex 是一个为制造业数字化项目准备的企业级全栈脚手架，重点服务需要集成 MES、WMS、QMS、设备接入、生产协同、报表分析、审批流于一体的生产制造企业MOM系统。它不是只提供登录页、菜单页和 CRUD 模板的通用后台，而是把生产制造项目里反复出现的组织权限、租户隔离、动态表格、数据字典、工作流、消息通知、报表、导入导出、文件上传、系统集成和私有化交付能力提前沉淀成平台底座。

## 演示地址

- 在线演示：<http://175.27.135.204:18080/login>

想要体验 Forgex 的朋友，可以通过下方不同角色的邀请码自行注册账号，分别体验普通用户、部门经理和系统审计员视角下的菜单权限、业务入口和系统能力。

- 普通用户邀请码：`D83F9B1E`
- 部门经理邀请码：`C40EDD46`
- 系统审计员邀请码：`948F2D80`

Forgex 内置对外用户注册系统，企业可以按角色、岗位或业务场景发放邀请码，为内部新人、项目成员或试用人员开放自助注册入口。新人通过邀请码完成账号创建、角色绑定和基础权限开通，无需系统管理员逐个手工创建用户，适合企业内部推广、试点上线、培训演示和分批启用场景。

## 为什么不一样

很多团队需要的不仅是「如何更快做出标准增删改查页面」，还包括上线后长期命题：组织与权限持续调整、表格与字典随业务膨胀、**多语种展示与服务端提示一致**、**多租户隔离**、**审批与报表**、**原生移动办公**、**外部系统集成**，以及 **Windows / Linux 私有化现场** 的安装、升级与配置沉淀。Forgex 面向制造企业与 MOM 类项目，把上述能力尽量收拢到 **同一套微服务 + Web 管理端 + Android 骨架 + 构建交付工程** 中，降低重复造轮子与现场拼接成本。

### 更丰富的平台能力（国际化、多租户、流程、报表、安卓、集成等）

- **深度国际化**：Web 端默认 **简体 / 繁体 / 英文 / 日文 / 韩文**；与服务端 `LangContext`、模块化提示（如 `fx_i18n_message`）、字典与动态表格等元数据的 **JSON 多语 + 多级回退** 协同，减少「界面已切英文、报错仍是中文」的割裂；并提供「多语言输入」等企业维护形态。
- **多租户与多组织**：租户上下文透传、数据隔离、忽略规则、公共配置回退与租户侧基础配置，适配集团工厂、外包交付或 SaaS 式分域。
- **审批与工作流**：流程建模、发起与办理、待办 / 已办、业务回调，支撑质检、异常、报工、采购等制造企业常见签核链路扩展。
- **报表中心**：报表分类、数据源、模板管理与 **UReport2 / JimuReport** 接入，便于将统计与台账能力落在平台层而非零散脚本。
- **原生 Android**：`Forgex_Mobile_Android`（Kotlin、Jetpack Compose、Hilt、Retrofit、DataStore）、dev/test/prod 多环境与业务模块雏形，与同套 **网关 / 认证** 语义对齐，便于现场巡检、移动端待办延伸。
- **集成平台**：第三方系统、授权、API 定义、参数结构、出站调用、异步执行与调用日志，减少与 ERP、OA 等系统点对点硬编码胶水层。
- **列表与表单工程化**：`FxDynamicTable`、列与用户个性化偏好、字典渲染；Excel 导入导出、模板与下拉 Provider；头像 / Logo / 业务附件等多存储策略。
- **消息、任务与审计**：模板消息、站内消息、SSE；分布式任务调度；登录日志、操作日志与审计字段习惯约定。

以上为「为什么不一样」中的能力维度概括；逐项接口与配置细节见文末文档入口与各模块 README。

### 更贴合私有化交付的部署方式（Windows / Linux）

Forgex 在 **源代码之外** 提供与现场运维衔接的打包物与脚本链路，而非仅依赖开发人员本机拼凑环境：

- **Windows 交付**：`Forgex_Build` 产出可视化安装与升级路径相关的 **ZIP 包**，内含前端静态资源、后端 JAR、**内置 Nginx**、**自带 Windows JRE**、**控制中心**、**授权请求客户端**、`Nacos` 配置快照、数据库 **初始化脚本**与 **upgrade** 脚本，适合工厂内网一体机或桌面服务端快速落地。
- **Linux 交付**：提供 **Tar 包**、`install.sh`、Nginx 配置模板与同套升级与数据库脚本，可与 Docker Compose、systemd 或客户现有运维规范组合部署。

具体操作命令与目录说明仍以本文「部署方式」章节与 [部署文档](./Forgex_Doc/部署/README.md) 为准。

### 更体系化的文档（实现逻辑 × 使用方式）

`Forgex_Doc` 为正式对外文档中心，按 **前端、后端、安卓、数据库、部署、开发规范** 等分册维护；前端重要能力多采用 **「实现逻辑 + 使用方式」** 成对编写，便于二开接手、测试与运维排障对齐同一事实来源。总入口：[文档中心](./Forgex_Doc/README.md)。

### 深度国际化（不止两份 `locale` JSON）

Forgex 的国际化目标是 **用户在界面改语言以后，字典标签、表格列头、服务端错误提示和业务配置尽量一起走同一套语言链路**，尽量避免「界面是英文，提示仍是中文」的割裂：

- **Web 前端**：Vue I18n 多语种语言包（简 / 繁 / 英 / 日 / 韩），并与 Ant Design Vue 语种资源协同。
- **后端统一语境**：请求链路通过语言上下文传递到各微服务，`fx_i18n_message` 等机制支撑 **模块化提示编码 + 多语 JSON**，并带有多级回退顺序（当前语种 → 主语言 → 中文等）。
- **数据与元数据**：数据字典标签、动态表格等平台级配置支持基于 JSON 的多语展示文本，适配「同一套字典要在五个语种下可读」的运营需求。
- **产品化录入**：前端提供「多语言输入」等企业录入形态（详见 [国际化与布局](./Forgex_Doc/前端/国际化与布局/README.md)），便于业务字段在多语环境下维护。
- **旧页面兼容**：对历史硬编码文案提供兼容迁移路径（如 `legacyI18n` 相关约定），新项目则推荐统一走标准 `t(...)` / 服务端提示链路。

Forgex 将 **界面语种、字典与平台元数据文案、服务端统一提示** 放在同一链路中设计，更贴合跨国集团与多语种运维并存的交付场景。

### 三端协同：Web × 微服务后端 × Android

Forgex 的「三端」指：**Web 管理端**、**可独立演进的 Java 微服务集群**、**原生 Android 客户端骨架**，共用认证与网关语义：

- **Web**：`Forgex_Fronted`（Vue 3 + TypeScript + Vite）。
- **后端**：以 `Forgex_Gateway` 为统一入口，`Forgex_Auth` / `Forgex_Sys` / `Forgex_Workflow` / `Forgex_Integration` / `Forgex_Report` 等拆分职责（详见下文项目结构）。
- **Android**：`Forgex_Mobile_Android` dev/test/prod 多环境与设备类型约定，示例模块覆盖登录、首页、工作流、消息、个人中心等，可直接作为企业 App 工程起点。

三端对齐后，团队在 **会话与权限语义、发版边界、现场安全策略** 上更容易统一规划，同时为 **原生系统能力（推送、离线、系统集成）** 保留清晰扩展面。

### 页面样式与交互体验（精致现代 B 端）

在 Ant Design Vue 体系之上，Forgex 对「第一眼观感」做了工程化收口：

- **主题系统**：围绕 Design Token 的浅色 / 深色主题与语义色梯度，Brand 色可系统化替换（详见 `Forgex_Fronted/src/theme/README.md`）。
- **布局与导航**：支持多种菜单与顶栏形态，适配不同组织架构下的信息架构偏好。
- **工作台个性化**：首页支持组件拖拽排序、组件尺寸与个人显隐偏好，更接近业务人员日常「自己的驾驶舱」而非固定死板的示例页。
- **数据密集场景**：在列表、查询、导出、字典渲染上以 `FxDynamicTable`、公共弹窗体系等为主线，兼顾 **效率与一致性**。

详见 [前端文档](./Forgex_Doc/前端/README.md) 与 [主题说明](./Forgex_MOM/Forgex_Fronted/src/theme/README.md)。

## 功能能力

### 后端能力

| 模块 | 能力 |
|---|---|
| 认证与授权 | 登录、注册、登出、验证码、密码加密、权限校验、动态路由、第三方登录预留 |
| 用户与组织 | 用户、角色、部门、岗位、菜单、角色授权、人员授权 |
| 多租户 | 租户隔离、租户上下文传递、租户忽略配置、公共配置回退 |
| 数据字典 | 树形字典、字典标签、多语言字典值、二级缓存 |
| 动态表格 | 表格配置、列配置、查询配置、用户个性化列配置 |
| 导入导出 | Excel 导入、Excel 导出、模板下载、下拉选项 Provider |
| 文件上传 | 本地、OSS、MinIO 存储策略，头像、Logo、业务文件归属记录 |
| 工作流 | 流程配置、发起审批、审批处理、待办/已办、业务回调 |
| 报表中心 | 报表分类、数据源、模板管理、UReport2/JimuReport 集成 |
| 集成平台 | 第三方系统、授权配置、API 配置、参数映射、调用日志 |
| 消息通知 | 站内消息、模板消息、SSE 推送 |
| 审计与日志 | 登录日志、操作日志、审计字段自动填充 |

### 前端能力

| 模块 | 能力 |
|---|---|
| 管理端框架 | Vue 3、TypeScript、Vite、Ant Design Vue、Pinia、Vue Router |
| 请求与反馈 | 统一 HTTP 客户端、自动成功/失败提示、静默请求模式 |
| 配置驱动页面 | `FxDynamicTable`、列设置、字典渲染、分页排序、用户列偏好 |
| 公共组件 | 公共弹窗、字典标签、图标选择器、部门树、导入组件 |
| 国际化 | 简体中文、繁体中文、英文、日文、韩文 |
| 个性化布局 | 个人首页拖拽布局、组件排序、尺寸调整、显隐控制 |
| 认证入口 | 登录、注册、邀请码注册、不同角色体验入口 |

### 移动端骨架

| 模块 | 能力 |
|---|---|
| Android 工程 | Kotlin、Jetpack Compose、Hilt、Retrofit、DataStore |
| 基础模块 | 登录、首页、工作流、消息、个人中心 |
| 环境支持 | dev/test/prod 多环境配置、设备类型识别 |

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|---|---|---|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.6 | 应用框架 |
| Spring Cloud | 2025.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2025.0.0.0-preview | 微服务套件 |
| Sa-Token | 1.44.0 | 权限认证 |
| MyBatis-Plus | 3.5.14 | ORM |
| MyBatis-Plus-Join | 1.5.4 | 联表查询 |
| Dynamic Datasource | 4.3.1 | 动态数据源 |
| Snail-Job | 1.8.1 | 分布式任务调度 |
| FastExcel | 1.3.0 | Excel 处理 |
| UReport2 | 2.2.10 | 报表引擎 |
| JimuReport | 1.9.0 | 积木报表 |

### 前端

| 技术 | 版本 | 用途 |
|---|---|---|
| Vue | 3.5.26 | 前端框架 |
| TypeScript | 5.6.3 | 类型系统 |
| Vite | 5.4.3 | 构建工具 |
| Ant Design Vue | 4.2.6 | UI 组件库 |
| Pinia | 3.0.4 | 状态管理 |
| Vue Router | 4.3.0 | 路由管理 |
| Vue I18n | 9.14.0 | 国际化 |
| Formily | 2.3.7 | 表单能力 |
| ECharts | 6.0.0 | 图表 |
| Three.js | 0.182.0 | 3D 渲染 |

## 项目结构

```text
forgex
├─ Forgex_Doc                    # 文档中心
├─ Forgex_Build                  # 构建、打包、部署与升级工程
├─ Forgex_MOM                    # 主工程
│  ├─ Forgex_Backend             # 后端微服务
│  │  ├─ Forgex_Gateway          # 网关服务
│  │  ├─ Forgex_Auth             # 认证服务
│  │  ├─ Forgex_Sys              # 系统服务
│  │  ├─ Forgex_Basic            # 基础资料服务
│  │  ├─ Forgex_Job              # 任务调度服务
│  │  ├─ Forgex_Workflow         # 工作流服务
│  │  ├─ Forgex_Integration      # 集成平台服务
│  │  └─ Forgex_Report           # 报表服务
│  ├─ Forgex_Fronted             # Web 管理端
│  └─ Forgex_Mobile_Android      # Android 移动端骨架
└─ logs                          # 本地日志目录
```

## 启动方式

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x
- RocketMQ 5.x

### 初始化数据库

数据库初始化脚本位于 `Forgex_Doc/部署/数据库初始化脚本`，升级包中的升级 SQL 位于 `database-upgrade/`。首次部署先导入初始化脚本；已有环境升级时，先备份数据库，再按升级包说明和 SQL 文件名顺序执行需要的升级脚本。

### 启动后端

```bash
cd Forgex_MOM/Forgex_Backend
mvn clean install
```

按实际场景启动以下服务：

- `Forgex_Gateway`
- `Forgex_Auth`
- `Forgex_Sys`
- `Forgex_Basic`
- `Forgex_Job`
- `Forgex_Workflow`
- `Forgex_Integration`
- `Forgex_Report`

### 启动前端

```bash
cd Forgex_MOM/Forgex_Fronted
npm install
npm run dev
```

默认本地地址：

- 前端：`http://localhost:5173`
- 网关：`http://localhost:8000`

### 构建 Android

```bash
cd Forgex_MOM/Forgex_Mobile_Android
gradlew.bat :app:assembleDevDebug
```

## 部署方式

### Windows 交付

`Forgex_Build` 提供 Windows 交付包和安装脚本，交付包包含前端静态资源、后端服务 JAR、Windows JRE、内置 Nginx、控制中心、授权请求客户端、Nacos 配置、数据库初始化脚本和数据库升级脚本。

```powershell
cd Forgex_Build
powershell -ExecutionPolicy Bypass -File build-all.ps1 -Version 0.6.5 -AllowDistFallback
```

构建后主要产物：

- `Forgex_Build/dist/windows/Forgex-Windows-Package-0.6.5.zip`
- `Forgex_Build/dist/linux/forgex-linux-bundle-0.6.5.tar.gz`

Windows 首次部署时，解压交付包后按安装器或 `scripts` 目录中的脚本完成安装、数据库导入、Nacos 配置导入和服务启动。已有环境升级时，使用新包中的 `scripts/upgrade.bat` 或 `scripts/upgrade.ps1` 替换应用文件，并在数据库备份后按需执行 `database-upgrade` 里的 SQL。

### Linux 交付

Linux 交付包包含前端、后端服务、Nginx 配置模板、Nacos 配置、授权客户端和部署脚本。解压后通过 `install.sh` 初始化目录和环境变量，再结合 Docker Compose 或现场服务管理方式启动后端服务。

```bash
tar -zxvf forgex-linux-bundle-0.6.5.tar.gz
cd forgex-linux-bundle-0.6.5
./install.sh ACME_PROD yanshi
```

详细部署说明见 [部署文档](./Forgex_Doc/部署/README.md)。

## 文档入口

- [文档中心首页](./Forgex_Doc/README.md)
- [开发规范](./Forgex_Doc/开发规范/README.md)
- [后端文档](./Forgex_Doc/后端/README.md)
- [前端文档](./Forgex_Doc/前端/README.md)
- [安卓端文档](./Forgex_Doc/安卓端/README.md)
- [数据库文档](./Forgex_Doc/数据库/README.md)
- [部署文档](./Forgex_Doc/部署/README.md)

## 联系方式

- QQ：3096821283
- Email：coder_nai@163.com

## 许可证

[Apache 2.0](./LICENSE)
