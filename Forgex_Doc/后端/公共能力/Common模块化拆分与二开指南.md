# Common 模块化拆分与二开指南

> 分类：后端 / 公共能力
> 版本：**V1.1.0**
> 更新时间：**2026-09-19**
> 适用范围：业务服务依赖选型、内部 Feign 契约扩展、公共能力复用、企业二开独立接入

本文说明 `Forgex_Common` 单体拆分后的模块地图、依赖规则与二开接入方式。目标是让普通模型或新同学按「按需依赖」正确扩展，而不是重新依赖大聚合包。

## 1. 结论先看

1. **Java 包名基本不变**：仍是 `com.forgex.common.*` / `com.forgex.common.api.*`。
2. **变的是 Maven 归属**：能力与契约拆到独立 artifact。
3. **`Forgex_Common` 仅作迁移期兼容聚合**：自身不再放实现源码，**新代码禁止新增对它的依赖**。
4. **内部 Feign 归属提供方 `*_Api`**，共享 DTO 归属 `Forgex_Domain_Contract`。
5. HTTP 路径、统一返回 `R<T>`、请求头（租户/用户/语言）、服务端口语义保持不变。
6. **物理目录已分组**（2026-09 架构升级）：`forgex-common/`（可独立发布的公共基础）、`forgex-admin/`（平台治理服务及其 API）、`forgex-business/`（业务服务分组：平台内置基础数据 `Forgex_Basic` 与企业二开扩展区）。Maven `artifactId`、Java 包名、服务名、JAR 名均保持不变。
7. **公共层不再隐式绑定平台**：用户/编码规则/审计通过 SPI（`UserDirectory`、`EncodeRuleProvider`、`OperationLogRecorder`）接入，平台 Feign 实现在 `forgex-admin-client`，按需引入。

## 2. 模块地图

后端物理分组（`Forgex_MOM/Forgex_Backend/`）：

```text
forgex-common/                  # 可独立构建、可发布到公司私服的公共基础
├── forgex-common-parent        # 公共独立构建父工程（版本与依赖管理）
├── forgex-common-bom           # 发布用 BOM（业务服务 import 用）
├── forgex-common-starter       # 独立业务服务轻量启动能力
├── Forgex_Common_Contract / Core / Web / Data / Crypto / Excel / Infra
├── Forgex_Domain_Contract      # 跨服务 DTO + 审计契约 + SPI
└── Forgex_Common               # 迁移期兼容聚合（无源码）
forgex-admin/                   # 平台治理层
├── forgex-admin-client         # 平台 Feign 适配（UserDirectory、远程审计）按需引入
├── forgex-admin-runtime        # 平台库表/字典缓存/动态表格等治理运行时（仅随平台交付的服务依赖）
├── forgex-admin-{auth,sys,job,workflow,integration,report,gateway}/...
│   └── Forgex_{Xxx}[_Api]
└── ...
forgex-business/                # 业务服务分组（在平台 Reactor 内）
├── forgex-business-basic/      # 平台内置基础数据服务（业务域参考实现）
│   ├── Forgex_Basic_Api
│   └── Forgex_Basic
└── company-order-service/      # 企业独立服务示例（不在 Reactor，独立构建部署）
```

### 2.1 公共能力模块

| Maven 模块 | 主要职责 | 典型类 / 包 | 依赖约束 |
|---|---|---|---|
| `Forgex_Common_Contract` | 统一返回、状态码、公共异常、提示契约 | `R`、`StatusCode`、`BusinessException`、`I18nBusinessException`、`CommonPrompt`、`I18nPrompt` | 只允许轻量依赖，不拉 Web/MyBatis/Feign/加密/Excel |
| `Forgex_Common_Core` | 上下文、轻量枚举、基础 Param | `TenantContext`、`UserContext`、`LangContext`、`BaseGetParam`、部分 Prompt 枚举 | 不依赖业务模块与重型 Starter |
| `Forgex_Common_Web` | Web 拦截、Feign Token 透传、JSON 契约自动配置 | `LangWebInterceptor`、`UserTenantWebInterceptor`、`FeignTokenInterceptor`、`JacksonConfig`、`FeignAutoConfiguration` | 可依赖 Contract/Core + Spring Web |
| `Forgex_Common_Data` | 实体基类、租户/数据权限持久化拦截 | `BaseEntity`、`TenantContextInterceptor`、`DataPermissionInterceptor`、`@DataScope` | 可依赖 Contract/Core + MyBatis-Plus |
| `Forgex_Common_Crypto` | 密码、传输、字段、文件加密 | `CryptoProviders`、`@FieldEncrypt`、`FieldEncryptInterceptor` | 可依赖 Contract/Core；字段加密可依赖 Data |
| `Forgex_Common_Excel` | Excel 导入导出公共模型与执行链路 | `ExcelConfigService`、`FxExcelImportHandler`、`TemplateOptionProvider` | 可依赖 Contract/Core + POI/FastExcel |
| `Forgex_Common_Infra` | 可复用运行时能力与扩展点：审计切面、许可证、MQ、通知、全局异常等 | `GlobalExceptionHandler`、`OperationLogAspect`、`AutoFillUsernameAspect`、license | 不依赖任何平台 API/实现；平台库表能力已移至 `forgex-admin-runtime` |
| `forgex-common-starter` | 独立业务服务轻量启动：MVC + JSON 契约 + 业务异常兜底 | `ForgexCommonAutoConfiguration`、`CommonBusinessExceptionHandler` | 只依赖 Contract/Core/Web；不引入平台实现 |
| `Forgex_Common` | **兼容聚合**（无业务源码） | 仅 `pom.xml` 聚合若干 Api + Infra + Web | 仅迁移期；新代码不要再依赖 |

平台治理专属（**不属于可发布公共层**，业务服务不要依赖）：

| Maven 模块 | 主要职责 |
|---|---|
| `forgex-admin-runtime` | 平台库表实体/Mapper（配置、字典缓存、i18n、动态表格、Excel 存储配置、消息模板）与 `MybatisPlusConfig` 等治理运行时 |
| `forgex-admin-client` | 平台 Feign 适配：`FeignUserDirectory`、`RemoteOperationLogRecorder`，配合 `AdminClientAutoConfiguration` 显式注册 |

公共扩展点（SPI，位于 `Forgex_Domain_Contract`，包名不变）：

| SPI / 契约 | 用途 | 公共层默认行为 | 平台实现 |
|---|---|---|---|
| `com.forgex.common.spi.UserDirectory` | 按用户名/ID 补全展示名 | `ObjectProvider` 缺省无实现时退避 | `forgex-admin-client` 的 `FeignUserDirectory` |
| `com.forgex.common.spi.EncodeRuleProvider` | 编码规则生成 | 无实现时编码生成降级 | `Forgex_Sys_Api` 的 `EncodeRuleFeignClient` |
| `com.forgex.common.audit.OperationLogRecorder` | 操作日志上报 | 本地空实现 / marker 排除 | `forgex-admin-client` 的 `RemoteOperationLogRecorder` |

### 2.2 契约与业务域 API 模块

| Maven 模块 | 内容 | 包名（保持不变） |
|---|---|---|
| `Forgex_Domain_Contract` | 跨服务共享 DTO、审计契约（`OperationLog`/`OperationLogRecorder`）、SPI（`UserDirectory`、`EncodeRuleProvider`） | `com.forgex.common.api.dto`、`com.forgex.common.audit`、`com.forgex.common.spi` |
| `Forgex_Auth_Api` | Auth Feign | `AuthFeignClient`、`AuthPermClient` |
| `Forgex_Sys_Api` | Sys Feign | `SysUserFeignClient`、`SysTenantFeignClient`、`EncodeRuleFeignClient`、`SysBasicSupportFeignClient`、`SysBackupJobFeignClient`、`OperationLogFeignClient` 等 |
| `Forgex_Basic_Api` | Basic / 主数据相关 Feign | `BasicSupplierQueryFeignClient`、`BasicUnitConversionFeignClient`、若干 Sync Client |
| `Forgex_Job_Api` | Job 相关 Feign | `CalendarReminderFeignClient` |
| `Forgex_Workflow_Api` | Workflow Feign | `WorkflowExecutionFeignClient`（含 `/start`、`/timeout/scan`） |
| `Forgex_Integration_Api` | Integration 内部 Feign | `IntegrationInternal*FeignClient` |

说明：

- Feign **接口**在 `*_Api`；消费方在自己的启动类上用 `@EnableFeignClients(clients = {...})` **显式启用**需要的客户端，公共层不再全局扫描平台客户端。
- `Forgex_Common` 聚合 **不一定包含全部 `*_Api`**（例如 `Forgex_Sys_Api` 常由业务或 Infra 按需引入），不要假设「依赖 Common = 拿到全部 Feign」。
- `Forgex_Job` 已不再依赖 `Forgex_Sys` 实现：备份/归档通过 `SysBackupJobFeignClient` + 一次性 Redis 工单票据调用 Sys 内部受控接口。`Forgex_Basic` 已不再 `@Import` Sys 实现类，改走 `SysBasicSupportFeignClient`。

## 3. 依赖规则（二开必守）

### 3.1 允许的方向

```text
业务服务 -> 按需 Forgex_{Xxx}_Api -> Common_Contract / Domain_Contract
业务服务 -> 按需 Common_Web / Data / Crypto / Excel / Infra / Core
Common_* 能力模块 -> Common_Contract / Common_Core
```

### 3.2 禁止事项

1. Common 能力模块依赖任一业务实现模块（Auth/Sys/Basic/...）或平台治理模块（`forgex-admin-runtime` / `forgex-admin-client`）。
2. 一个 `*_Api` 依赖另一个 `*_Api`；确需共享模型时下沉到 `Forgex_Domain_Contract`。
3. `Common_Core` / `Common_Contract` 引入 Spring Boot Starter、OpenFeign、MyBatis、Redis、MQ、POI、BouncyCastle 等重依赖。
4. **新代码为了省事重新依赖 `Forgex_Common` 聚合包**。
5. 把新 Feign Client 再塞回已清空的单体 Common 源码树。
6. 企业业务服务依赖 `forgex-admin-runtime`（平台库表）或任何 `Forgex_{Xxx}` 服务实现 JAR。
7. 公共层源码出现 `@FeignClient`、`@TableName` 或平台库表访问（`scripts/verify-common-module-boundaries.ps1` 会拦截）。

### 3.3 业务服务怎么选依赖

**平台内置服务**（admin 分组的 Auth/Sys/Job/Workflow/Integration/Report/Gateway，业务分组的 Basic）按能力精确选：

| 你需要的能力 | 依赖 |
|---|---|
| 只返回 `R` / 抛业务异常 | `Forgex_Common_Contract`（必要时 + `Core`） |
| 读租户/用户/语言上下文 | `Forgex_Common_Core` |
| MVC 拦截 / Feign Token 透传 | `Forgex_Common_Web` |
| `BaseEntity`、租户行级隔离、数据权限 | `Forgex_Common_Data` |
| 密码/字段/传输加密 | `Forgex_Common_Crypto` |
| Excel 导入导出公共能力 | `Forgex_Common_Excel` |
| 审计切面、许可证、MQ、全局异常 Advice 等 | `Forgex_Common_Infra` |
| 平台库表（配置/字典缓存/动态表格/i18n 落库） | `forgex-admin-runtime`（仅随平台交付的服务） |
| 用户名补全 / 远程审计的 Feign 实现 | `forgex-admin-client`（仅随平台交付的服务或显式接入平台的企业服务） |
| 调 Sys 用户/编码规则/字典校验 | `Forgex_Sys_Api`（+ 需要的 Domain DTO） |
| 调工作流内部发起/超时扫描 | `Forgex_Workflow_Api` |
| 调 Auth 权限内部接口 | `Forgex_Auth_Api` |

**企业独立业务服务**（公司二开，脱离平台仓库构建）统一走 BOM + Starter：

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.forgex</groupId>
            <artifactId>forgex-common-bom</artifactId>
            <version>${forgex.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>com.forgex</groupId>
        <artifactId>forgex-common-starter</artifactId>
    </dependency>
    <!-- 仅当确实调用平台接口时才引入对应契约，例如：
    <dependency>
        <groupId>com.forgex</groupId>
        <artifactId>Forgex_Sys_Api</artifactId>
    </dependency>
    -->
</dependencies>
```

完整可运行样例见 `Forgex_MOM/Forgex_Backend/forgex-business/company-order-service/`（默认仅公共包可独立启动；`-Pplatform-api` 演示可选接入平台用户接口）。

参考：`Forgex_Sys/pom.xml` 已改为精确依赖 Contract/Core/Web/Data/Crypto/Excel/Infra + runtime/client，而不是单体 Common。

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
| `.../common/config/JacksonConfig.java` | `Forgex_Common_Web` |
| `.../common/domain/entity/{SysConfig,table/*,dict/*,i18n/*}.java`、`.../common/mapper/*`、`.../common/service/{table,i18n}/impl/*` | `forgex-admin-runtime`（平台治理运行时） |
| `.../common/audit/OperationLog*.java`（契约） | `Forgex_Domain_Contract` |
| `.../common/audit/OperationLogFeignClient.java` | `Forgex_Sys_Api` |
| `.../common/audit/RemoteOperationLogRecorder.java` | `forgex-admin-client` |
| 旧目录 `Forgex_Backend/Forgex_{Xxx}/` | `Forgex_Backend/forgex-admin/forgex-admin-{xxx}/Forgex_{Xxx}/`（服务）；`forgex-common/`（公共） |

## 6. 关联文档

- [内部服务接口开放说明](./内部服务接口开放说明.md)
- [内部服务接口说明](../../开发规范/规范文档/内部服务接口说明.md)
- [项目架构设计文档](../../开发规范/架构设计/项目架构设计文档.md)
- [后端文档导航](../README.md)
- [工作流使用方式](../模块专题/工作流使用方式.md)（Feign 发起与超时扫描）
- [导入导出实现逻辑](../模块专题/导入导出实现逻辑.md)（Excel 模块路径）
- 企业业务服务样例：`Forgex_MOM/Forgex_Backend/forgex-business/README.md`
