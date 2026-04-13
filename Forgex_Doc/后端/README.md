# 后端文档导航

> 版本：**预览版 V0.5.0**  
> 更新时间：**2026-04-13**

本目录聚焦 Forgex 后端的**公共能力**、**核心功能模块**与**实现入口**，用于帮助开发者快速判断：功能是否支持、由哪个模块负责、对应文档在哪里。

## 一、先看什么

1. [后端公共能力与核心功能手册](./后端公共能力与核心功能手册.md)
2. [项目架构设计文档](../开发规范/架构设计/项目架构设计文档.md)
3. 模块级详细文档（见下表）

## 二、能力总览

| 能力域 | 当前支持情况 | 主要模块 | 文档入口 |
|---|---|---|---|
| 统一响应 `R<T>` | 已支持 | `Forgex_Common` | [公共能力手册](./后端公共能力与核心功能手册.md) |
| 业务异常 / 国际化异常 | 已支持 | `Forgex_Common` | [公共能力手册](./后端公共能力与核心功能手册.md) |
| 请求级国际化 | 已支持 | `Forgex_Common`、`Forgex_Sys` | [后端国际化使用指南](../../doc/开发规范/04-功能模块/07-公共组件/后端国际化使用指南.md) |
| 多租户上下文 / 忽略配置 | 已支持 | `Forgex_Common`、`Forgex_Auth`、`Forgex_Sys` | [多租户模块文档](../../doc/开发规范/04-功能模块/02-多租户/多租户模块文档.md) |
| 认证、验证码、租户选择 | 已支持 | `Forgex_Auth` | [认证授权模块文档](../../doc/开发规范/04-功能模块/01-认证授权/认证授权模块文档.md) |
| 用户管理 | 已支持 | `Forgex_Sys` | [用户管理模块文档](../../doc/开发规范/04-功能模块/03-用户管理/用户管理模块文档.md) |
| 数据字典 / 标签样式 | 已支持 | `Forgex_Sys` | [数据字典模块文档](../../doc/开发规范/04-功能模块/05-数据字典/数据字典模块文档.md) |
| 登录日志 / 操作日志 | 已支持 | `Forgex_Auth`、`Forgex_Sys` | [日志系统模块文档](../../doc/开发规范/04-功能模块/06-日志系统/日志系统模块文档.md) |
| 动态表格配置中心 | 已支持 | `Forgex_Common`、`Forgex_Sys` | [公共能力手册](./后端公共能力与核心功能手册.md) |
| 消息模板 / 站内消息 / SSE | 已支持 | `Forgex_Sys` | [公共消息模板使用指南](../../doc/开发规范/使用指南/公共消息模板使用指南.md) |
| 工作流配置与审批 | 已支持核心闭环 | `Forgex_Workflow` | [工作流模块使用指南](../../doc/开发规范/使用指南/工作流模块使用指南.md) |
| 报表分类 / 模板 / 数据源 | 已支持基础能力 | `Forgex_Report` | 根 README 的报表能力说明 |
| 编码规则 / 导入导出 / 代码生成 | 已支持平台级能力 | `Forgex_Sys` | 根 README、系统管理相关专题 |

## 三、建议阅读路径

### 1. 做 Controller / Service 开发

- 统一返回：`R`
- 异常：`BusinessException`、`I18nBusinessException`
- 提示消息：`CommonPrompt` / 各模块 Prompt 枚举
- 国际化：`LangContext` + `RMessageI18nAdvice`

对应入口： [后端公共能力与核心功能手册](./后端公共能力与核心功能手册.md)

### 2. 做租户与权限相关功能

- 登录与租户选择：认证授权文档
- 请求上下文：架构设计文档
- 数据隔离与忽略：多租户模块文档

### 3. 做配置驱动类功能

- 动态表格：公共能力手册 + 公共表格实现说明
- 字典：数据字典模块文档
- 消息模板：公共消息模板使用指南

## 四、当前后端公共方法/能力沉淀重点

| 主题 | 关键类 / 关键入口 |
|---|---|
| 统一响应 | `com.forgex.common.web.R` |
| 状态码 | `com.forgex.common.web.StatusCode` |
| 通用提示枚举 | `com.forgex.common.i18n.CommonPrompt` |
| 国际化上下文 | `LangContext`、`LangWebInterceptor`、`I18nMessageService` |
| 异常体系 | `BusinessException`、`I18nBusinessException` |
| 多租户上下文 | `TenantContext`、`UserContext`、`TenantIgnoreRegistry` |
| 动态表格 | `CommonTableController`、`FxTableConfigService`、`FxUserTableConfigService` |
| 权限校验 | `@RequirePerm`、`PermissionInterceptor` |
| 日志 | 登录日志/操作日志相关服务与注解/AOP |

## 五、说明

本目录负责输出 V0.5.0 的**清晰能力列表**和**正式导航入口**；更细的接口、字段、流程细节，继续保留在 `doc/开发规范/04-功能模块` 和 `doc/开发规范/使用指南` 中。

