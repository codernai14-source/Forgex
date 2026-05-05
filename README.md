# Forgex

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

很多脚手架解决的是“怎么更快生成一张表的增删改查”，Forgex 更关注“企业项目上线以后怎么持续交付和维护”。生产制造行业的系统通常不是单表页面，而是多组织、多角色、多租户、多语言、多系统集成、多端协同和现场私有化部署并存。Forgex 把这些真实交付问题前置处理，减少团队在每个项目里重复搭权限、重复写表格配置、重复做导入导出、重复接审批和报表、重复处理部署脚本的成本。

Forgex 主要解决这些问题：

- **企业权限复杂**：内置认证授权、角色菜单授权、角色人员授权、动态路由、权限标识和审计日志，适合多部门、多岗位、多角色协同。
- **制造业务字段多、页面变化快**：通过 `FxDynamicTable` 和公共表格列配置，把列表列、查询项、排序、字典渲染和用户个性化列设置从页面代码里抽出来，降低后续二开和现场调整成本。
- **多租户和多组织隔离难维护**：提供租户上下文、租户数据隔离、公共配置回退和租户级基础数据配置能力。
- **企业系统需要对接外部平台**：集成平台内置第三方系统、授权、API 配置、参数结构、参数映射、多目标出站调用、同步/异步执行和调用日志，便于对接 ERP、OA 等系统。
- **制造现场需要流程和报表**：内置工作流、任务处理、回调机制、报表中心、UReport2/JimuReport 集成，支撑审批、质检、异常处理、生产报工等场景扩展。
- **项目交付不只是一份源码**：提供 Windows/Linux 交付包、内置 Nginx、Windows JRE、控制中心、授权请求客户端、Nacos 配置和数据库初始化/升级脚本，方便私有化部署和升级。

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
