# 安卓端文档导航

> 版本：**V0.6.0**  
> 更新时间：**2026-04-15**

当前 Android 端已具备模块化工程骨架，且登录、租户、菜单链路已经接入。本页作为 Android 正式入口，统一说明模块职责与当前能力。

## 一、当前已明确的基础能力

| 能力 | 当前状态 |
|---|---|
| 工程结构 | 已支持 `app`、`core/*`、`feature/*` 模块化拆分 |
| 技术栈 | 已采用 Kotlin + Compose + Hilt + Retrofit + DataStore |
| 环境维度 | 已支持 `dev / test / prod` |
| 设备识别 | 已支持 `MOBILE / TABLET` 策略 |
| 登录链路 | 已接入登录 -> 选租户 -> 菜单路由基础链路 |

## 二、模块结构

### 2.1 应用入口与公共层

| 模块 | 作用 |
|---|---|
| `app` | 应用入口、导航编排、全局依赖注入 |
| `core/common` | 通用模型、统一结果包装、基础工具 |
| `core/network` | 网络层、拦截器、请求封装 |
| `core/datastore` | 本地会话持久化，保存 Token、Tenant 等信息 |
| `core/ui` | 设备识别、通用页面容器与 UI 基础设施 |

### 2.2 功能模块

| 模块 | 作用 |
|---|---|
| `feature/auth` | 登录、验证码、租户选择 |
| `feature/home` | 首页导航与入口承载 |
| `feature/workflow` | 移动端审批与待办能力扩展位 |
| `feature/message` | 消息中心扩展位 |
| `feature/profile` | 个人中心与个人资料 |

## 三、登录与租户链路

当前 Android 工程 README 已明确接入以下链路：

1. `/auth/login`
2. `/auth/choose-tenant`
3. `/sys/menu/routes`

默认通过网关 `/api` 前缀访问后端能力，说明移动端与 Web 管理端共享统一网关入口和权限链路。

## 四、当前工程约定

- 配置与环境区分使用 `dev / test / prod`
- 设备类型在运行时识别为 `MOBILE / TABLET`
- 会话数据优先通过 `DataStore` 持久化
- 网络层通过 `Retrofit` 承载，后续应与后端 `R<T>` 模型保持一致
- 新增业务功能优先放入 `feature/*`，避免直接堆叠到 `app`

## 五、当前参考资料

- [Android 工程 README](../../Forgex_MOM/Forgex_Mobile_Android/README.md)

## 六、后续优先补充的文档主题

1. 网络层与统一结果包装
2. 登录/验证码/租户选择链路
3. DataStore 会话持久化
4. Compose 公共页面容器
5. 路由与模块导航规范

## 七、与其他文档的关系

- 认证与租户逻辑：参考 [认证授权](../后端/身份与权限/认证授权.md)
- 网关与统一入口：参考 [网关与路由](../后端/模块专题/网关与路由.md)
- 工作流能力：参考 [工作流](../后端/模块专题/工作流.md)

当前本页已作为 Android 正式导航入口，后续继续在本目录内拆分专题页。
