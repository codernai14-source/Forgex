# Forgex

> 企业级应用开发脚手架与业务中台预览版  
> 当前版本：**预览版 V0.5.0**

## 项目简介

Forgex 是一个面向企业级场景的全栈开发脚手架，采用前后端分离与微服务架构，目标是把认证、租户、权限、国际化、消息、工作流、动态表格、报表等高频基础能力沉淀为可复用的平台底座。

当前仓库已形成三端协同结构：

- **Web 管理端**：Vue 3 + TypeScript + Vite
- **Backend 微服务**：Spring Boot 3 + Spring Cloud + Spring Cloud Alibaba
- **Android 移动端骨架**：Kotlin + Compose + Hilt + Retrofit + DataStore

## 预览版 V0.5.0 版本重点

V0.5.0 是一个十分重要的预览版。本次重点不只是功能继续完善，更重要的是：

1. **补齐正式文档入口与导航体系**
2. **明确前后端当前已支持的能力清单**
3. **把开发规范、后端、前端、数据库、安卓端、部署文档纳入 `Forgex_Doc` 统一管理**

## 文档导航

### 统一文档中心

- [文档中心首页](./Forgex_Doc/README.md)
- [开发规范](./Forgex_Doc/开发规范/README.md)
- [后端文档](./Forgex_Doc/后端/README.md)
- [前端文档](./Forgex_Doc/前端/README.md)
- [数据库文档](./Forgex_Doc/数据库/README.md)
- [安卓端文档](./Forgex_Doc/安卓端/README.md)
- [部署文档](./Forgex_Doc/部署/README.md)

### 重点专题直达

- [项目架构设计文档](./Forgex_Doc/开发规范/架构设计/项目架构设计文档.md)
- [后端公共能力与核心功能手册](./Forgex_Doc/后端/后端公共能力与核心功能手册.md)
- [前端公共能力与核心功能手册](./Forgex_Doc/前端/前端公共能力与核心功能手册.md)
- [模块文档映射](./Forgex_Doc/开发规范/模块文档映射/README.md)

## 页面预览

### 系统首页

![系统首页](./Forgex_Doc/img/sysshouye.png)

### 登录页面

![登录页面](./Forgex_Doc/img/login.png)

### 个人首页（可拖拽布局）

![个人首页](./Forgex_Doc/img/shouye.png)

### 个人首页拖拽配置

![拖拽配置](./Forgex_Doc/img/shouyetuozhuai.png)

### 系统配置

![系统配置 1](./Forgex_Doc/img/xtpz1.png)

![系统配置 2](./Forgex_Doc/img/xtpz2.png)

### 多语言用户界面

![中文用户](./Forgex_Doc/img/hanyuuser.png)

![英文用户](./Forgex_Doc/img/yingwenuser.png)

### 消息中心

![消息中心](./Forgex_Doc/img/message.png)

### 动态布局

![动态布局](./Forgex_Doc/img/dongtaibuju.png)

### 审批首页

![审批首页](./Forgex_Doc/img/spshouye.png)

## 支持功能清单

### 后端支持功能

| 模块 | 功能 | 状态 | 文档入口 |
|---|---|---|---|
| **认证与授权** | 账号登录、注册、登出 | 已支持 | [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md) |
| | 图片验证码、滑块验证码 | 已支持 | [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md) |
| | 密码策略（bcrypt/SM2/SM4） | 已支持 | [加密功能](./Forgex_Doc/后端/模块专题/加密功能.md) |
| | 权限校验（@RequirePerm） | 已支持 | [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md) |
| | 动态路由生成 | 已支持 | [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md) |
| | 第三方登录（微信、钉钉） | 已支持 | [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md) |
| **多租户** | 租户隔离（行级） | 已支持 | [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md) |
| | 租户上下文传递 | 已支持 | [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md) |
| | 租户忽略配置 | 已支持 | [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md) |
| | 公共配置回退机制 | 已支持 | [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md) |
| **用户与角色** | 用户管理（CRUD） | 已支持 | [用户与角色](./Forgex_Doc/后端/身份与权限/用户与角色.md) |
| | 角色管理、授权 | 已支持 | [用户与角色](./Forgex_Doc/后端/身份与权限/用户与角色.md) |
| | 部门管理、职位管理 | 已支持 | 系统管理模块 |
| **统一返回** | 统一响应 R<T> | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| | 状态码管理 | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| | 业务异常处理 | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| **国际化** | 请求级多语言 | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| | 异常消息翻译 | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| | 支持 5 种语言 | 已支持 | [统一返回与国际化](./Forgex_Doc/后端/配置与审计/统一返回与国际化.md) |
| **通用消息** | 站内消息 | 已支持 | [消息模板与 SSE](./Forgex_Doc/后端/配置与审计/消息模板与 SSE.md) |
| | 模板消息 | 已支持 | [消息模板与 SSE](./Forgex_Doc/后端/配置与审计/消息模板与 SSE.md) |
| | SSE 推送 | 已支持 | [消息模板与 SSE](./Forgex_Doc/后端/配置与审计/消息模板与 SSE.md) |
| **Redis 工具** | RedisHelper 工具类 | 已支持 | [Redis 工具](./Forgex_Doc/后端/公共能力/Redis 工具.md) |
| | 分布式锁 | 已支持 | [Redis 工具](./Forgex_Doc/后端/公共能力/Redis 工具.md) |
| | 缓存注解 | 已支持 | [Redis 工具](./Forgex_Doc/后端/公共能力/Redis 工具.md) |
| **数据字典** | 字典管理（树形） | 已支持 | [数据字典与日志](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| | 字典标签渲染 | 已支持 | [数据字典与日志](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| | 二级缓存架构 | 已支持 | [数据字典与日志](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| **加密功能** | 密码存储加密 | 已支持 | [加密功能](./Forgex_Doc/后端/模块专题/加密功能.md) |
| | 传输加密（SM2） | 已支持 | [加密功能](./Forgex_Doc/后端/模块专题/加密功能.md) |
| | 字段透明加密 | 已支持 | [加密功能](./Forgex_Doc/后端/模块专题/加密功能.md) |
| | KMS 密钥管理 | 已支持 | [加密功能](./Forgex_Doc/后端/模块专题/加密功能.md) |
| **导入导出** | Excel 导出配置 | 已支持 | [导入导出](./Forgex_Doc/后端/模块专题/导入导出.md) |
| | Excel 导入配置 | 已支持 | [导入导出](./Forgex_Doc/后端/模块专题/导入导出.md) |
| | 模板下载 | 已支持 | [导入导出](./Forgex_Doc/后端/模块专题/导入导出.md) |
| | 下拉选项 Provider | 已支持 | [导入导出](./Forgex_Doc/后端/模块专题/导入导出.md) |
| **文件上传** | 本地存储 | 已支持 | [文件上传](./Forgex_Doc/后端/模块专题/文件上传.md) |
| | OSS 对象存储 | 已支持 | [文件上传](./Forgex_Doc/后端/模块专题/文件上传.md) |
| | 工厂模式选择 | 已支持 | [文件上传](./Forgex_Doc/后端/模块专题/文件上传.md) |
| **工作流** | 流程配置 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | 发起审批、审批处理 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | 待办/已办管理 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | 回调注册机制 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| **报表中心** | 报表分类管理 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | 报表数据源管理 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | 报表模板管理 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| | UReport2、JimuReport 集成 | 已支持 | [工作流与报表](./Forgex_Doc/后端/模块专题/工作流与报表.md) |
| **日志审计** | 登录日志 | 已支持 | [数据字典与日志](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| | 操作日志 | 已支持 | [数据字典与日志](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| | 审计字段自动填充 | 已支持 | [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md) |

### 前端支持功能

| 模块 | 功能 | 状态 | 文档入口 |
|---|---|---|---|
| **HTTP 请求** | 统一 HTTP 客户端 | 已支持 | [HTTP 请求与消息提示](./Forgex_Doc/前端/请求与反馈/HTTP 请求与消息提示.md) |
| | 自动成功/失败消息 | 已支持 | [HTTP 请求与消息提示](./Forgex_Doc/前端/请求与反馈/HTTP 请求与消息提示.md) |
| | 静默请求模式 | 已支持 | [HTTP 请求与消息提示](./Forgex_Doc/前端/请求与反馈/HTTP 请求与消息提示.md) |
| **动态表格** | FxDynamicTable 组件 | 已支持 | [FxDynamicTable 与列设置](./Forgex_Doc/前端/配置驱动页面/FxDynamicTable 与列设置.md) |
| | 用户列设置 | 已支持 | [FxDynamicTable 与列设置](./Forgex_Doc/前端/配置驱动页面/FxDynamicTable 与列设置.md) |
| | 字典翻译 | 已支持 | [FxDynamicTable 与列设置](./Forgex_Doc/前端/配置驱动页面/FxDynamicTable 与列设置.md) |
| | 分页排序 | 已支持 | [FxDynamicTable 与列设置](./Forgex_Doc/前端/配置驱动页面/FxDynamicTable 与列设置.md) |
| **公共组件** | BaseFormDialog 弹窗 | 已支持 | [公共弹窗](./Forgex_Doc/前端/请求与反馈/公共弹窗.md) |
| | DictTag 字典标签 | 已支持 | [数据字典文档](./Forgex_Doc/后端/配置与审计/数据字典与日志.md) |
| | IconPicker 图标选择器 | 已支持 | 组件目录 |
| | DeptTree 部门树 | 已支持 | [部门树与组织选择](./Forgex_Doc/前端/组件与页面/部门树与组织选择.md) |
| **多语言输入** | I18nInput 多语言输入框 | 已支持 | [多语言输入与动态布局](./Forgex_Doc/前端/国际化与布局/多语言输入与动态布局.md) |
| | I18nJsonEditor 编辑器 | 已支持 | [多语言输入与动态布局](./Forgex_Doc/前端/国际化与布局/多语言输入与动态布局.md) |
| | 5 种语言支持 | 已支持 | [多语言输入与动态布局](./Forgex_Doc/前端/国际化与布局/多语言输入与动态布局.md) |
| **可拖拽布局** | 个人首页设计器 | 已支持 | [个人首页与可拖拽布局](./Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局.md) |
| | 组件拖拽排序 | 已支持 | [个人首页与可拖拽布局](./Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局.md) |
| | 尺寸调整 | 已支持 | [个人首页与可拖拽布局](./Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局.md) |
| | 显隐控制 | 已支持 | [个人首页与可拖拽布局](./Forgex_Doc/前端/国际化与布局/个人首页与可拖拽布局.md) |
| **消息模板** | TemplatePreview 预览 | 已支持 | 消息模板专题 |
| | ReceiverSelector 接收人选择 | 已支持 | 消息模板专题 |
| **工作流页面** | 发起审批页面 | 已支持 | 工作流模块使用指南 |
| | 待办处理页面 | 已支持 | 工作流模块使用指南 |
| | 已办/发起页面 | 已支持 | 工作流模块使用指南 |
| **报表页面** | 报表查看页面 | 已支持 | 报表模块使用指南 |
| | 报表设计器集成 | 已支持 | 报表模块使用指南 |

### 安卓端支持功能

| 模块 | 功能 | 状态 | 说明 |
|---|---|---|---|
| **工程架构** | 多模块结构 | 已支持 | app、core/*、feature/* |
| | 依赖注入（Hilt） | 已支持 | 统一依赖管理 |
| | 网络请求（Retrofit） | 已支持 | HTTP 客户端封装 |
| | 本地存储（DataStore） | 已支持 | 数据持久化 |
| **基础功能** | 登录模块 | 已支持 | auth 模块 |
| | 首页框架 | 已支持 | home 模块 |
| | 工作流模块 | 已支持 | workflow 模块 |
| | 消息模块 | 已支持 | message 模块 |
| | 个人中心 | 已支持 | profile 模块 |
| **环境支持** | 多环境配置 | 已支持 | dev / test / prod |
| | 设备识别 | 已支持 | MOBILE / TABLET |

## 项目结构

```text
forgex
├─ Forgex_Doc                    # 文档中心
├─ doc                           # 旧文档目录（保留）
├─ Forgex_MOM                    # 主工程
│  ├─ Forgex_Backend             # 后端工程
│  │  ├─ Forgex_Auth             # 认证服务 (8081)
│  │  ├─ Forgex_Sys              # 系统服务 (8082)
│  │  ├─ Forgex_Basic            # 基础服务
│  │  ├─ Forgex_Common           # 公共模块
│  │  ├─ Forgex_Gateway          # 网关服务 (8080)
│  │  ├─ Forgex_Job              # 任务调度服务 (8083)
│  │  ├─ Forgex_Workflow         # 工作流服务 (8084)
│  │  └─ Forgex_Report           # 报表服务 (8085)
│  ├─ Forgex_Fronted             # 前端工程
│  └─ Forgex_Mobile_Android      # Android 移动端
└─ logs                          # 日志目录
```

## 技术栈

### 后端

| 技术 | 版本 | 用途 |
|---|---|---|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.6 | 应用框架 |
| Spring Cloud | 2025.0.0 | 微服务框架 |
| Spring Cloud Alibaba | 2025.0.0.0-preview | 阿里云微服务套件 |
| Sa-Token | 1.44.0 | 权限认证框架 |
| MyBatis-Plus | 3.5.14 | ORM 框架 |
| MyBatis-Plus-Join | 1.5.4 | MP 联表查询扩展 |
| Dynamic Datasource | 4.3.1 | 动态数据源 |
| Snail-Job | 1.8.1 | 分布式任务调度 |
| FastExcel | 1.3.0 | Excel 处理 |
| Hutool | 5.8.23 | Java 工具类库 |
| MapStruct | 1.5.5.Final | 对象映射 |
| springdoc-openapi | 2.6.0 | API 文档 |
| UReport2 | 2.2.10 | 报表引擎 |
| JimuReport | 1.9.0 | 积木报表引擎 |

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
| Formily | 2.3.7 | 表单解决方案 |
| Vue Flow | 1.48.x | 流程图组件 |
| ECharts | 6.0.0 | 图表库 |
| ApexCharts | 5.3.6 | 图表库 |
| Three.js | 0.182.0 | 3D 渲染引擎 |

### 移动端

| 技术 | 用途 |
|---|---|
| Kotlin | 开发语言 |
| Jetpack Compose | UI 框架 |
| Hilt | 依赖注入 |
| Retrofit | 网络请求 |
| DataStore | 数据持久化 |

## 推荐阅读路径

### 后端开发

1. [后端文档导航](./Forgex_Doc/后端/README.md)
2. [后端公共能力与核心功能手册](./Forgex_Doc/后端/后端公共能力与核心功能手册.md)
3. [认证授权](./Forgex_Doc/后端/身份与权限/认证授权.md)
4. [多租户](./Forgex_Doc/后端/租户与上下文/多租户.md)

### 前端开发

1. [前端文档导航](./Forgex_Doc/前端/README.md)
2. [前端公共能力与核心功能手册](./Forgex_Doc/前端/前端公共能力与核心功能手册.md)
3. [FxDynamicTable 与列设置](./Forgex_Doc/前端/配置驱动页面/FxDynamicTable 与列设置.md)
4. [前端 HTTP 请求与消息提示](./Forgex_Doc/前端/请求与反馈/HTTP 请求与消息提示.md)

### 数据库与规范

1. [数据库文档](./Forgex_Doc/数据库/README.md)
2. [开发规范](./Forgex_Doc/开发规范/README.md)

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- Node.js 18+
- MySQL 8.0+
- Redis 6.0+
- Nacos 2.x

### 1. 克隆项目

```bash
git clone <your-repository-url>
cd forgex
```

### 2. 初始化数据库

- 执行 `Forgex_Doc/数据库/脚本与修复` 中整理后的初始化/修复/诊断脚本说明
- 准备 MySQL、Redis、Nacos 等基础环境
- 参考 [数据库文档](./Forgex_Doc/数据库/README.md)

### 3. 启动后端

```bash
cd Forgex_MOM/Forgex_Backend
mvn clean install
```

按需要启动以下服务：

- `Forgex_Gateway`
- `Forgex_Auth`
- `Forgex_Sys`
- `Forgex_Workflow`
- `Forgex_Report`
- 其他业务服务

### 4. 启动前端

```bash
cd Forgex_MOM/Forgex_Fronted
npm install
npm run dev
```

默认访问地址：

- 前端：`http://localhost:5173`
- 网关：`http://localhost:8000`

### 5. 构建 Android 端

```bash
cd Forgex_MOM/Forgex_Mobile_Android
gradlew.bat :app:assembleDevDebug
```

## 适用场景

- 企业管理后台与中台项目快速起步
- 需要多租户、权限体系、国际化支持的 SaaS 系统
- 需要工作流、消息中心、动态表格、导入导出、报表能力的业务系统
- 希望同时推进 Web 管理端与 Android 客户端的项目

## 版本记录

### 预览版 V0.5.0 · 2026-04

- 建立 `Forgex_Doc` 统一文档入口
- 新增开发规范、后端、前端、数据库、安卓端、部署六类文档导航
- 补齐更清晰的功能支持列表与研发阅读路径
- 整理前后端公共能力、重要功能的使用与实现入口
- 将 `doc/开发规范/04-功能模块` 与新文档中心建立跳转关系
- 完善后端多租户、登录注册、加密、导入导出、Redis 工具等文档
- 完善前端 HTTP 工具、多语言输入、公共弹窗、动态表格、部门树等文档

## 许可证

[Apache 2.0](./LICENSE)

## 联系方式

- QQ：3096821283
- Email：coder_nai@163.com
