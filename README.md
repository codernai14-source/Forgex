# Forgex

> 企业级应用开发脚手架与业务中台预览版  
> 当前版本：**预览版 V0.4.0**

## 项目简介

Forgex 是一个面向企业级场景的全栈开发脚手架，采用前后端分离与微服务架构，目标是把企业项目里高频且重复的基础能力沉淀为可复用的产品底座。

当前代码仓库已经形成三端协同结构：

- Web 管理端：Vue 3 + TypeScript + Vite
- Backend 微服务：Spring Boot 3 + Spring Cloud + Spring Cloud Alibaba
- Android 移动端骨架：Kotlin + Compose + Hilt + Retrofit + DataStore

---

## V0.4.0 功能亮点

### 1. 企业级认证与多租户体系

- 支持账号登录、注册、登出、重置密码、管理员安全校验
- 支持租户选择、租户偏好保存、多租户隔离与租户初始化流程
- 支持图片验证码与滑块验证码
- 支持国密公钥下发，便于前端密码加密传输
- 支持第三方登录接入，当前已落地微信、钉钉回调链路

### 2. 系统管理能力完整可用

- 用户、角色、部门、岗位、菜单、模块、租户等核心主数据管理
- 角色授权、角色绑定用户、角色绑定岗位、角色绑定部门等权限分配能力
- 字典管理、系统配置、登录日志、操作日志、在线用户管理
- 邀请码管理、租户消息白名单、租户忽略配置等平台治理能力
- 个人资料维护、密码修改、个人首页配置与布局保存

### 3. 工作流模块已经落地

- 流程任务配置、分页查询、启停控制
- 流程草稿编辑、基础信息保存、流程图保存、发布
- 发起审批、审批通过、驳回、撤销
- 我的发起、我的待办、我的已办、我的抄送
- 工作流仪表盘统计与分析接口
- 回调注册 / 注销机制，方便与业务模块集成

### 4. 报表中心已具备基础闭环

- 报表分类管理
- 报表数据源管理，支持按编码查询与连接测试
- 报表模板管理，支持保存、内容更新、导入、导出
- 已集成 **UReport2** 与 **JimuReport**，为复杂报表场景预留扩展能力

### 5. 动态表格、导入导出与代码生成

- 通用表格配置中心与用户个性化列设置
- 通用表数据查询接口，便于低代码化页面组装
- Excel 导出配置、导入配置、模板下载、提供器列表
- 登录日志与用户数据导出能力已接入
- 代码生成预览与下载接口已经具备，可用于提升 CRUD 场景开发效率
- 编码规则管理与规则生成能力已落地

### 6. 国际化与消息中心持续增强

- 当前已覆盖 `zh-CN`、`zh-TW`、`en-US`、`ja-JP`、`ko-KR`
- 语言类型管理、默认语言设置、翻译导入、模板下载
- 前后端多语言消息链路打通，支持动态切换
- 站内消息、模板消息、按模板发送、按用户发送
- SSE 消息流接口已接入，可支持实时消息提醒

### 7. 仪表盘与可视化体验升级

- 系统仪表盘支持统计总览、最近操作日志、最近登录日志
- 支持服务器信息、服务内存占用、模块内存池占用、JVM 堆信息展示
- 最近版本已优化 CPU/内存展示逻辑，统计口径更贴近真实运行情况
- 前端集成 ECharts、ApexCharts、Three.js，可支撑更丰富的数据可视化页面

### 8. 移动端工程骨架已成型

- Android 端采用模块化结构：`app`、`core/*`、`feature/*`
- 已接入登录、选租户、菜单路由链路
- 已拆分 `auth`、`home`、`workflow`、`message`、`profile` 五类移动能力模块
- 支持 `dev / test / prod` 多环境与 `MOBILE / TABLET` 设备识别策略

---

## 项目结构

```text
Forgex_MOM
├─ Forgex_Backend
│  ├─ Forgex_Auth       认证授权服务
│  ├─ Forgex_Sys        系统管理服务
│  ├─ Forgex_Basic      基础业务服务
│  ├─ Forgex_Common     公共能力模块
│  ├─ Forgex_Gateway    网关服务
│  ├─ Forgex_Job        分布式任务调度接入
│  ├─ Forgex_Workflow   工作流服务
│  └─ Forgex_Report     报表服务
├─ Forgex_Fronted       Web 管理端
└─ Forgex_Mobile_Android Android 移动端骨架
```

---

## 技术栈

### 后端

- Java 17
- Spring Boot 3.5.6
- Spring Cloud 2025.0.0
- Spring Cloud Alibaba 2025.0.0.0-preview
- Sa-Token 1.44.0
- MyBatis-Plus 3.5.14
- MyBatis-Plus-Join 1.5.4
- Dynamic Datasource 4.3.1
- Snail-Job 1.8.1
- FastExcel 1.3.0
- Hutool 5.8.23
- MapStruct 1.5.5.Final
- springdoc-openapi 2.6.0
- UReport2 2.2.10
- JimuReport 1.9.0

### 前端

- Vue 3.5.26
- TypeScript 5.6.3
- Vite 5.4.3
- Ant Design Vue 4.2.6
- Pinia 3.0.4
- Vue Router 4.3.0
- Vue I18n 9.14.0
- Formily 2.3.7
- Vue Flow 1.48.x
- ECharts 6.0.0
- ApexCharts 5.3.6
- Three.js 0.182.0

### 移动端

- Kotlin
- Jetpack Compose
- Hilt
- Retrofit
- DataStore

---

## 已实现模块概览

### 后端模块

- `Forgex_Auth`：登录、注册、验证码、租户选择、语言切换、第三方登录、权限内部接口
- `Forgex_Sys`：系统管理、消息中心、国际化、仪表盘、导入导出、编码规则、公共表格、初始化向导
- `Forgex_Workflow`：流程配置、流程图设计、审批执行、工作台分析、流程回调
- `Forgex_Report`：报表分类、模板、数据源、模板导入导出
- `Forgex_Gateway`：统一 API 入口与网关转发
- `Forgex_Job`：分布式任务调度能力接入
- `Forgex_Common`：通用响应、公共配置、工具能力
- `Forgex_Basic`：基础业务扩展模块

### 前端页面

- 认证中心：登录、注册、系统初始化向导
- 工作台：个人首页、个人资料、系统仪表盘、基础仪表盘
- 系统管理：用户、角色、部门、岗位、菜单、模块、租户、字典
- 平台能力：国际化语言、邀请码、消息中心、消息模板、租户消息白名单
- 数据能力：动态表格配置、用户表格配置、Excel 导入导出配置、编码规则
- 工作流：流程配置、节点编排、流程发起、我的待办 / 已办 / 发起 / 抄送、流程仪表盘
- 报表中心：数据源、模板管理、设计器、预览

### 移动端模块

- `feature:auth`
- `feature:home`
- `feature:workflow`
- `feature:message`
- `feature:profile`

---

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

- 执行 `Forgex_MOM/Forgex_Backend/sql` 或 `doc` 下对应初始化脚本
- 准备 MySQL、Redis、Nacos 等基础环境

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

---

## 适用场景

- 企业管理后台与中台项目快速起步
- 需要多租户、权限体系、国际化支持的 SaaS 系统
- 需要工作流、消息中心、动态表格、导入导出能力的业务系统
- 希望同时推进 Web 管理端与 Android 客户端的项目

---

## 版本记录

### 预览版 V0.4.0 · 2026-04

- 完成三端仓库结构整合：Web、Backend、Android
- 系统管理、认证、多租户、国际化链路持续完善
- 工作流模块形成从流程配置到审批执行的完整闭环
- 报表中心上线分类、模板、数据源基础能力
- 仪表盘新增更细粒度的 CPU / JVM / 内存占用展示
- 消息中心、邀请码、租户初始化、租户白名单等平台能力落地
- 动态表格、Excel 配置、代码生成、编码规则能力进一步完善

---

## 许可证

[Apache 2.0](./LICENSE)

## 联系方式

- QQ：3096821283
- Email：coder_nai@163.com
