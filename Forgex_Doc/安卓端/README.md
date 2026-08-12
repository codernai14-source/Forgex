# 安卓端文档导航

> 版本：**V0.8.5**
> 更新时间：**2026-08-12**

当前 Android 端已具备完整的模块化工程骨架，登录、租户、菜单链路已接入，且 core/feature 模块均已落地。本页作为 Android 正式入口，统一说明模块职责、技术栈与当前能力。

## 一、当前已明确的基础能力

| 能力 | 当前状态 |
|---|---|
| 工程结构 | 已支持 `app`、`core/*`（11 个）、`feature/*`（13 个）模块化拆分 |
| 技术栈 | Kotlin 1.9.24 + Jetpack Compose（BOM 2024.06.00）+ Hilt 2.52 + Retrofit 2.11 + Room 2.6.1 + DataStore 1.1.1 |
| 构建工具 | Android Gradle Plugin 8.13.2 |
| 环境维度 | 已支持 `dev / test / prod` |
| 设备识别 | 已支持 `MOBILE / TABLET` 策略 |
| 登录链路 | 已接入登录 -> 选租户 -> 菜单路由基础链路 |
| 会话持久化 | 已通过 DataStore 持久化 Token / Tenant |
| 统一结果 | 网络层已与后端 `R<T>` 模型对齐 |

## 二、技术栈概览

| 类别 | 技术 | 版本 |
|---|---|---|
| 语言 | Kotlin | 1.9.24 |
| 构建 | Android Gradle Plugin | 8.13.2 |
| UI | Jetpack Compose（BOM） | 2024.06.00 |
| 依赖注入 | Hilt | 2.52 |
| 网络 | Retrofit | 2.11 |
| 本地数据库 | Room | 2.6.1 |
| 偏好存储 | DataStore Preferences | 1.1.1 |
| 导航 | Navigation Compose | 已接入 |
| 异步任务 | Kotlin Coroutines + Flow | 已接入 |

## 三、模块结构

### 3.1 应用入口

| 模块 | 职责 |
|---|---|
| `app` | 应用入口、导航编排、全局依赖注入容器、Application 初始化 |

### 3.2 core 公共层（11 个模块）

| 模块 | 职责 |
|---|---|
| `core/architecture` | 提供全局架构基类与约定，定义分层规则、基础 ViewModel/Repository 抽象骨架 |
| `core/common` | 通用模型、统一结果包装、基础工具类与常量定义，不含业务逻辑 |
| `core/component` | 复用型 Compose 业务组件（卡片、列表项、状态页等），跨 feature 共享的 UI 组件库 |
| `core/datastore` | 本地会话持久化，基于 Preferences DataStore 保存 Token、Tenant 等会话信息 |
| `core/designsystem` | 设计系统层，统一主题 Token（颜色/字号/间距/圆角）、排版与暗色模式支持 |
| `core/device` | 设备类型识别（MOBILE / TABLET）与屏幕尺寸/方向工具，驱动自适应布局策略 |
| `core/model` | 跨模块共享的数据模型与 DTO 定义，feature 之间通用的领域对象 |
| `core/navigation` | Navigation Compose 路由表约定与导航扩展，集中管理路由常量与跳转辅助 |
| `core/network` | 网络层，Retrofit 封装、OkHttp 拦截器、统一错误处理与后端 `R<T>` 模型对齐 |
| `core/testing` | 测试基础设施，提供 fakes、规则与通用测试工具，降低 feature 单测成本 |
| `core/ui` | 设备识别、通用页面容器与 UI 基础设施，为 feature 提供页面骨架与状态组合 |

### 3.3 feature 功能模块（13 个模块）

| 模块 | 职责 |
|---|---|
| `feature/auth` | 登录、验证码、租户选择与会话建立 |
| `feature/basic` | 基础数据维护（字典、组织、岗位等主数据移动端查看） |
| `feature/equipment` | 设备资产查看与点检/工单扩展位 |
| `feature/home` | 首页导航、快捷入口与工作台聚合 |
| `feature/integration` | 集成平台移动端扩展位（接口监控、任务状态查看） |
| `feature/label` | 标签打印与标签模板移动端管理扩展位 |
| `feature/message` | 消息中心、通知列表与已读回执 |
| `feature/production` | 生产工单、报工与进度查看移动端扩展位 |
| `feature/profile` | 个人中心、个人资料与设置 |
| `feature/quality` | 质量检验与不合格品处理移动端扩展位 |
| `feature/report` | 报表与看板移动端查看扩展位 |
| `feature/warehouse` | 仓储出入库与盘点移动端扩展位 |
| `feature/workflow` | 移动端审批与待办能力，支持流程节点处理 |

## 四、登录与租户链路

当前 Android 工程已明确接入以下链路：

1. `/auth/login` — 账号密码登录
2. `/auth/choose-tenant` — 多租户选择
3. `/sys/menu/routes` — 菜单与路由拉取

默认通过网关 `/api` 前缀访问后端能力，说明移动端与 Web 管理端共享统一网关入口和权限链路。

## 五、当前工程约定

- 配置与环境区分使用 `dev / test / prod`
- 设备类型在运行时识别为 `MOBILE / TABLET`
- 会话数据优先通过 `core/datastore` 的 DataStore 持久化
- 网络层通过 `core/network` 的 Retrofit 承载，与后端 `R<T>` 模型保持一致
- 新增业务功能优先放入 `feature/*`，避免直接堆叠到 `app`
- 跨 feature 共享数据模型放入 `core/model`，共享 UI 组件放入 `core/component`
- 主题与设计 Token 统一由 `core/designsystem` 提供，禁止在 feature 内硬编码样式

## 六、专题文档

| 专题 | 说明 |
|---|---|
| [网络层与统一结果](./网络层与统一结果.md) | Retrofit 封装、OkHttp 拦截器、`R<T>` 模型对齐、错误处理 |
| [DataStore 会话持久化](./DataStore%20会话持久化.md) | Token / Tenant 存储、Preferences DataStore、加密 |
| [Compose 页面容器](./Compose%20页面容器.md) | MOBILE / TABLET 设备识别、通用页面容器、主题 Token |
| [导航与模块规范](./导航与模块规范.md) | Navigation Compose、feature 模块边界、路由表约定 |
| [离线能力](./离线能力.md) | Room 本地库 + WorkManager 同步队列框架说明 |

## 七、与其他文档的关系

- 认证与租户逻辑：参考 [认证授权](../后端/身份与权限/认证授权.md)
- 网关与统一入口：参考 [网关与路由](../后端/模块专题/网关与路由.md)
- 工作流能力：参考 [工作流](../后端/模块专题/工作流.md)
- Android 工程 README：参考 [Android 工程 README](../../Forgex_MOM/Forgex_Mobile_Android/README.md)

本页作为 Android 正式导航入口，专题页在本目录内持续拆分维护。
