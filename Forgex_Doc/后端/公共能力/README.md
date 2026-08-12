# 后端公共能力

> 版本：**V0.8.5**
> 更新时间：**2026-08-12**

本页聚焦后端可横向复用的公共能力，帮助开发者从"组件/机制"而不是"业务模块"角度阅读文档。

## 一、统一返回与异常

| 主题 | 文档 | 说明 |
|---|---|---|
| 统一响应 `R<T>` | [统一返回与国际化](../配置与审计/统一返回与国际化.md) | Controller 统一返回模型、状态码、使用方式 |
| 异常体系 | [统一返回与国际化](../配置与审计/统一返回与国际化.md) | BusinessException / I18nBusinessException / 全局异常处理器 |
| 文档导航入口 | [后端文档导航](../README.md) | 后端正式文档唯一推荐入口（原手册页已重定向） |

## 二、国际化与消息链路

| 主题 | 文档 | 说明 |
|---|---|---|
| 后端国际化 | [统一返回与国际化](../配置与审计/统一返回与国际化.md) | LangContext、I18nMessageService、RMessageI18nAdvice、多语言配置 |
| 消息模板 | [消息模板与 SSE](../配置与审计/消息模板与SSE.md) | 模板消息、接收人、站内消息、SSE 链路 |

## 三、Redis 与缓存

| 主题 | 文档 | 说明 |
|---|---|---|
| Redis 工具 | [Redis 工具](./Redis工具.md) | RedisHelper 统一操作、项目中的缓存场景、Key 规则 |
| 字典缓存 | [数据字典与日志](../配置与审计/数据字典与日志.md) | Caffeine + Redis 二级缓存架构 |
| 缓存策略 | [实现逻辑](./缓存策略实现逻辑.md) / [使用方式](./缓存策略使用方式.md) | Key、TTL、穿透/击穿、失效边界和多租户缓存约束 |

## 四、开发辅助与注解能力

| 主题 | 文档 | 说明 |
|---|---|---|
| 业务编码生成 | [使用方式](./业务编码生成使用方式.md) / [实现逻辑](./业务编码生成实现逻辑.md) | `EncodeUtils` / `EncodeRuleService` 调用 Sys 编码规则 |
| 权限注解 `@RequirePerm` | [认证授权](../身份与权限/认证授权.md) | 按钮级权限校验注解 |
| 自动填充注解 | [后端文档导航](../README.md) | TenantMetaObjectHandler 自动填充（见手册能力总览表） |
| 字段加密注解 `@FieldEncrypt` | [加密功能](../模块专题/加密功能.md) | 字段透明加密/解密 |
| API 接口文档 | [实现逻辑](./API接口文档实现逻辑.md) / [使用方式](./API接口文档使用方式.md) | springdoc-openapi / Swagger 注解维护规范 |
| 内部服务接口开放说明 | [内部服务接口开放说明](./内部服务接口开放说明.md) | Forgex 体系内模块互调契约，不等同于第三方系统接入 |
| Common 模块化拆分与二开 | [Common 模块化拆分与二开指南](./Common模块化拆分与二开指南.md) | Contract/Core/Web/Data/Crypto/Excel/Infra + Domain_Contract + `*_Api` 依赖选型 |
| 性能优化 | [实现逻辑](./性能优化实现逻辑.md) / [使用方式](./性能优化使用方式.md) | 分页、索引、缓存、批量处理和慢查询定位 |

## 五、与业务模块的关系

| 公共能力 | 主要模块归属 | 主要被谁复用 |
|---|---|---|
| `R<T>` / 状态码 / 异常 | `Forgex_Common_Contract` | 所有 Controller / Service |
| 租户/用户/语言上下文 | `Forgex_Common_Core` | 所有业务服务 |
| Web 拦截 / Feign Token 透传 | `Forgex_Common_Web` | 需要 MVC / Feign 的服务 |
| `BaseEntity` / 数据权限 / 租户行级隔离 | `Forgex_Common_Data` | 持久化业务服务 |
| 加密全家桶 | `Forgex_Common_Crypto` | Auth/Sys 及需要字段加密的服务 |
| Excel 导入导出公共能力 | `Forgex_Common_Excel` | Sys 配置页 + 各业务 Handler |
| 审计 / 表格配置 / 许可证 / 全局异常 Advice | `Forgex_Common_Infra` | 平台类服务 |
| 内部 Feign 契约 | `Forgex_*_Api` + `Forgex_Domain_Contract` | 跨服务调用方 |
| 国际化消息链路 | Contract + Infra Advice | `Auth`、`Sys`、`Workflow` 等所有服务 |
| 消息模板 | `Sys` 业务实现 | `Sys`、`Workflow`、其他业务模块 |

二开依赖选型：[Common 模块化拆分与二开指南](./Common模块化拆分与二开指南.md)。

## 六、关联入口

- [后端文档导航](../README.md)
- [后端模块专题](../模块专题/README.md)
- [模块文档映射](../../开发规范/模块文档映射/README.md)

