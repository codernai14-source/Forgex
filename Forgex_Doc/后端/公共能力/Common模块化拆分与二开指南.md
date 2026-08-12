# Common 模块化拆分与二开指南

> 分类：后端 / 公共能力
> 版本：**V0.8.5**
> 更新时间：**2026-08-12**
> 适用范围：业务服务依赖选型、内部 Feign 契约扩展、公共能力复用

本文说明当前 `Forgex_Common` 单体拆分后的模块地图、依赖规则与二开接入方式。目标是让普通模型或新同学按「按需依赖」正确扩展，而不是重新依赖大聚合包。

## 1. 结论先看

1. **Java 包名基本不变**：仍是 `com.forgex.common.*` / `com.forgex.common.api.*`。
2. **变的是 Maven 归属**：能力与契约拆到独立 artifact。
3. **`Forgex_Common` 仅作迁移期兼容聚合**：自身不再放实现源码，**新代码禁止新增对它的依赖**。
4. **内部 Feign 归属提供方 `*_Api`**，共享 DTO 归属 `Forgex_Domain_Contract`。
5. HTTP 路径、统一返回 `R<T>`、请求头（租户/用户/语言）、服务端口语义保持不变。

## 2. 模块地图

### 2.1 公共能力模块

| Maven 模块 | 主要职责 | 典型类 / 包 | 依赖约束 |
|---|---|---|---|
| `Forgex_Common_Contract` | 统一返回、状态码、公共异常、提示契约 | `R`、`StatusCode`、`BusinessException`、`I18nBusinessException`、`CommonPrompt`、`I18nPrompt` | 只允许轻量依赖，不拉 Web/MyBatis/Feign/加密/Excel |
| `Forgex_Common_Core` | 上下文、轻量枚举、基础 Param | `TenantContext`、`UserContext`、`LangContext`、`BaseGetParam`、部分 Prompt 枚举 | 不依赖业务模块与重型 Starter |
| `Forgex_Common_Web` | Web 拦截、Feign Token 透传自动配置 | `LangWebInterceptor`、`UserTenantWebInterceptor`、`FeignTokenInterceptor`、`FeignAutoConfiguration` | 可依赖 Contract/Core + Spring Web |
| `Forgex_Common_Data` | 实体基类、租户/数据权限持久化拦截 | `BaseEntity`、`TenantContextInterceptor`、`DataPermissionInterceptor`、`@DataScope` | 可依赖 Contract/Core + MyBatis-Plus |
| `Forgex_Common_Crypto` | 密码、传输、字段、文件加密 | `CryptoProviders`、`@FieldEncrypt`、`FieldEncryptInterceptor` | 可依赖 Contract/Core；字段加密可依赖 Data |
| `Forgex_Common_Excel` | Excel 导入导出公共模型与执行链路 | `ExcelConfigService`、`FxExcelImportHandler`、`TemplateOptionProvider` | 可依赖 Contract/Core + POI/FastExcel |
| `Forgex_Common_Infra` | 许可证、审计、字典、表格配置、MQ、通知、全局异常/i18n Advice 等运行时能力 | `GlobalExceptionHandler`、`RMessageI18nAdvice`、`OperationLogAspect`、table/dict/license | 按能力复用；不要把它当「万能 Common」 |
| `Forgex_Common` | **兼容聚合**（无业务源码） | 仅 `pom.xml` 聚合若干 Api + Infra + Web | 仅迁移期；新代码不要再依赖 |

### 2.2 契约与业务域 API 模块

| Maven 模块 | 内容 | 包名（保持不变） |
|---|---|---|
| `Forgex_Domain_Contract` | 跨服务共享 DTO、必要注解（如 `@AutoFillUsername`） | `com.forgex.common.api.dto`、`com.forgex.common.api.annotation`、部分 `domain.dto` |
| `Forgex_Auth_Api` | Auth Feign | `AuthFeignClient`、`AuthPermClient` |
| `Forgex_Sys_Api` | Sys Feign | `SysUserFeignClient`、`SysTenantFeignClient`、`EncodeRuleFeignClient` 等 |
| `Forgex_Basic_Api` | Basic / 主数据相关 Feign | `BasicSupplierQueryFeignClient`、`BasicUnitConversionFeignClient`、若干 Sync Client |
| `Forgex_Job_Api` | Job 相关 Feign | `CalendarReminderFeignClient` |
| `Forgex_Workflow_Api` | Workflow Feign | `WorkflowExecutionFeignClient`（含 `/start`、`/timeout/scan`） |
| `Forgex_Integration_Api` | Integration 内部 Feign | `IntegrationInternal*FeignClient` |

说明：

- Feign **接口**在 `*_Api`；Aspect / `FeignConfig` / 示例类等运行时辅助仍可能在 `Forgex_Common_Infra`。
- `Forgex_Common` 聚合 **不一定包含全部 `*_Api`**（例如 `Forgex_Sys_Api` 常由业务或 Infra 按需引入），不要假设「依赖 Common = 拿到全部 Feign」。

## 3. 依赖规则（二开必守）

### 3.1 允许的方向

```text
业务服务 -> 按需 Forgex_{Xxx}_Api -> Common_Contract / Domain_Contract
业务服务 -> 按需 Common_Web / Data / Crypto / Excel / Infra / Core
Common_* 能力模块 -> Common_Contract / Common_Core
```

### 3.2 禁止事项

1. Common 能力模块依赖任一业务实现模块（Auth/Sys/Basic/...）。
2. 一个 `*_Api` 依赖另一个 `*_Api`；确需共享模型时下沉到 `Forgex_Domain_Contract`。
3. `Common_Core` / `Common_Contract` 引入 Spring Boot Starter、OpenFeign、MyBatis、Redis、MQ、POI、BouncyCastle 等重依赖。
4. **新代码为了省事重新依赖 `Forgex_Common` 聚合包**。
5. 把新 Feign Client 再塞回已清空的单体 Common 源码树。

### 3.3 业务服务怎么选依赖

| 你需要的能力 | 依赖 |
|---|---|
| 只返回 `R` / 抛业务异常 | `Forgex_Common_Contract`（必要时 + `Core`） |
| 读租户/用户/语言上下文 | `Forgex_Common_Core` |
| MVC 拦截 / Feign Token 透传 | `Forgex_Common_Web` |
| `BaseEntity`、租户行级隔离、数据权限 | `Forgex_Common_Data` |
| 密码/字段/传输加密 | `Forgex_Common_Crypto` |
| Excel 导入导出公共能力 | `Forgex_Common_Excel` |
| 表格配置、审计、许可证、全局异常 Advice 等 | `Forgex_Common_Infra` |
| 调 Sys 用户/编码规则 | `Forgex_Sys_Api`（+ 需要的 Domain DTO） |
| 调工作流内部发起/超时扫描 | `Forgex_Workflow_Api` |
| 调 Auth 权限内部接口 | `Forgex_Auth_Api` |

参考：`Forgex_Sys/pom.xml` 已改为精确依赖 Contract/Core/Web/Data/Crypto/Excel/Infra，而不是单体 Common。

## 4. 新增内部服务契约（二开步骤）

1. 确认是「模块互调」还是「第三方接入」。第三方走 `Forgex_Integration`，不要写进内部 Feign。
2. 共享请求/响应 DTO 放到 `Forgex_Domain_Contract`（包名可继续用 `com.forgex.common.api.dto`）。
3. Feign Client 放到**提供方**对应的 `Forgex_{服务}_Api`。
4. 提供方业务模块实现 Controller（通常带 `/internal/...`），路径与 Feign 声明一致。
5. 消费方 POM 只加该 `*_Api`（及 Domain_Contract），不要拉全量聚合。
6. 补齐 `@Tag` / `@Operation` / `@Schema`，保证 springdoc 可读。

示例（工作流内部发起，路径已存在）：

```java
import com.forgex.common.api.dto.WorkflowExecutionStartRequestDTO;
import com.forgex.common.api.feign.WorkflowExecutionFeignClient;
import com.forgex.common.web.R;

// 消费方依赖：Forgex_Workflow_Api + Forgex_Domain_Contract + Forgex_Common_Contract
R<Long> response = workflowExecutionFeignClient.startExecution(request);
```

对应模块路径：

- Feign：`Forgex_Workflow_Api/.../WorkflowExecutionFeignClient.java`
- DTO：`Forgex_Domain_Contract/.../WorkflowExecutionStartRequestDTO.java`

## 5. 旧文档路径怎么读

遇到文档仍写 `Forgex_Common/src/main/java/com/forgex/common/...` 时，按类职责映射：

| 旧写法 | 新归属 |
|---|---|
| `.../common/web/R.java`、异常、Prompt | `Forgex_Common_Contract` |
| `.../common/tenant/TenantContext.java` | `Forgex_Common_Core` |
| `.../common/api/dto/*.java` | `Forgex_Domain_Contract` |
| `.../common/api/feign/*FeignClient.java` | 对应 `Forgex_*_Api` |
| `.../common/service/excel/*` | `Forgex_Common_Excel` |
| `.../common/crypto/*` | `Forgex_Common_Crypto` |
| `.../common/web/GlobalExceptionHandler.java` | `Forgex_Common_Infra` |

## 6. 关联文档

- [内部服务接口开放说明](./内部服务接口开放说明.md)
- [内部服务接口与第三方接口平台说明](../../开发规范/规范文档/内部服务接口与第三方接口平台说明.md)
- [项目架构设计文档](../../开发规范/架构设计/项目架构设计文档.md)
- [后端文档导航](../README.md)
- [工作流使用方式](../模块专题/工作流使用方式.md)（Feign 发起与超时扫描）
- [导入导出实现逻辑](../模块专题/导入导出实现逻辑.md)（Excel 模块路径）
