# Forgex 版本更新记录 (CHANGELOG)

> 本文件记录 Forgex 自首个公开发行版以来的核心功能演进与重大变更。
> 部署相关说明见 [部署文档](./README.md)。

---

## V0.8.5 (2026-08-12) — 公共模块拆分与工作流增强

### 架构

- **Forgex_Common 模块化拆分**：拆为 `Common_Contract` / `Core` / `Web` / `Data` / `Crypto` / `Excel` / `Infra`，共享 DTO 下沉 `Domain_Contract`，内部 Feign 按提供方归属各 `*_Api`
- 业务服务改为**精确 Maven 依赖**；`Forgex_Common` 仅保留为迁移期兼容聚合（无实现源码）
- 正式文档同步：新增《Common 模块化拆分与二开指南》，更新架构、内部服务契约与模块映射

### 工作流

- **审批人撤回**（`/wf/execution/recall`，区别于发起人撤销 cancel）
- **委托**：待办单条委托 + 委托设置；节点 `allowDelegate` 与权限双校验
- **超时扫描 Job**：`workflowTimeoutScanJob` → Feign `/wf/execution/internal/timeout/scan`
- **抄送列表**、待办动作「权限 ∩ 节点能力」策略（`ApprovalNodeActionPolicy` / `pendingActionModel`）
- 待办加签/转交/委托按钮权限种子脚本：`20260812_workflow_pending_action_permissions.sql`

### 组织与用户

- 用户表新增 **直属上级** `sys_user.superior_user_id`
- 内部解析 `POST /sys/user/internal/resolveSuperiorUserIds`，工作流支持 `ApproverType.SUPERIOR`
- 说明：管理端表单与用户 Excel 导入暂未接入上级字段（需 API/SQL 维护）

### 认证

- 登录与选租户拆分：**`interactionCode` 短期交互码**，选租户时校验并消费

### 导入导出 / 编码规则

- Excel 公共能力归属 `Forgex_Common_Excel`；用户导入专节明确 `tableCode=sys_user` 与 COVER 语义
- 编码规则明细校验/渲染拆分（`EncodeRuleDetailValidator` / `EncodeRuleRenderer`）

### 文档

- `Forgex_Doc` 文档版本统一升至 **V0.8.5**
- 纠正工作流「无直属上级」等过时结论；补齐二开入口与脚本登记

---

## V0.8.0 (2026-06) — 正式生产版 🎯

### 新增功能

- **工厂建模四级主数据**：车间 → 产线 → 工段 → 工序，完整 MOM 制造层级建模
- **首页组件中心**：可拖拽工作台增强，组件显隐/排序/尺寸记忆，布局分享与仪表盘增强
- **标签模板重构**：标签类型/字段/模板管理、标签绑定、打印记录全链路
- **集成看板增强**：集成平台首页仪表盘，第三方系统对接态势可视化
- **代码生成增强**：在线代码生成器安全增强、在线开发计划
- **Nacos 3.2.2 升级**：注册中心与配置中心版本升级

### 体验优化

- 全局 Loading 延迟优化，完善局部加载体验
- 布局深色分层、标签栏能力与样式优化
- 基础数据批量删除功能
- 敏感信息传输加密

### 国际化

- 五语言全面完善（简中/繁中/英/日/韩）
- 前端静态文本迁移、后端 i18n 增强

---

## V0.7.0 (2026-05) — 体验版

### 新增功能

- 基础数据批量删除、FxDynamicTable 增强
- 代码生成模块安全增强
- 个人首页拖拽布局（组件排序、尺寸调整、显隐控制）
- 日历提醒与基础主数据工作历
- 包装规格三槽位方案

### 体验优化

- 布局仪表盘增强
- 公共 Excel 导入组件完善

### 安全

- 敏感信息传输加密机制

---

## V0.6.5 (2026-04) — 演示版

### 新增功能

- **物料管理完整链路**：成品管理、原材料管理、半成品管理、包装方式管理、计量单位管理
- **供应商评审**：供应商主数据管理与评审流程
- **Android 版本管理**：移动端版本发布与升级机制
- 演示环境注册流程完善（邀请码注册）
- 国际化五语言基础支持

### 交付增强

- 部署与升级方案完善（Windows/Linux 双路径）
- 轻量升级与日志清理机制
- 演示环境服务器部署

---

## V0.5.x (2026-03) — 基础版

### 核心能力

- **RBAC 权限闭环**：用户、角色、部门、岗位、菜单、权限指令
- **多租户架构**：租户隔离、上下文传递、忽略配置、公共配置回退
- **工作流引擎**：流程配置、发起审批、办理、待办/已办、业务回调
- **报表中心**：报表分类、数据源、模板管理、UReport2 / JimuReport 集成
- **导入导出**：Excel 导入/导出、模板下载、下拉选项 Provider
- **文件存储**：本地/OSS/MinIO 多后端策略
- **消息通知**：站内消息、模板消息、SSE 推送
- **操作审计**：登录日志、操作日志、审计字段自动填充
- **数据字典**：树形字典、多语言字典值、二级缓存

### 工程基础

- Spring Cloud 微服务架构（Gateway + 8 业务服务）
- Vue 3 + TypeScript + Ant Design Vue 前端管理端
- Kotlin + Jetpack Compose Android 骨架
- Forgex_Build 构建与部署工程
- Forgex_Doc 文档体系
