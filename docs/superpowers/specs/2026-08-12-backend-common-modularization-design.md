# Forgex 后端公共模块拆分设计

## 1. 背景与目标

当前 `Forgex_Common` 同时承载内部服务契约、基础工具、Web 自动配置、数据访问、加密、Excel、许可证、消息等能力。所有业务服务都直接依赖该模块，导致第三方加密构建需要处理大量与当前服务无关的类和依赖，构建耗时较长；同时公共模块的职责和依赖边界也不清晰。

本次改造目标：

- 业务服务只依赖实际使用的公共能力和服务契约。
- 内部 API 契约归属接口提供方，避免单一 API 模块持续膨胀。
- 重依赖能力独立，使普通服务不会传递引入加密、Excel、数据库等依赖。
- 保持现有 Java 包名和运行行为，采用渐进迁移降低回归风险。
- 通过构建产物和加密耗时对比验证拆分是否真正有效。

本次不改变业务接口语义、HTTP 路径、共享请求头、统一返回结构、数据库结构和服务端口。

## 2. 目标模块

### 2.1 公共能力模块

| Maven 模块 | 职责 | 主要依赖约束 |
| --- | --- | --- |
| `Forgex_Common_Contract` | `R`、状态码、公共异常、共享请求头常量、跨模块必需的最小基础契约 | 只允许轻量依赖，不依赖 Spring Web、MyBatis、Redis、Feign、加密或 Excel |
| `Forgex_Common_Core` | 无状态工具、上下文对象、基础枚举和基础注解 | 依赖 JDK、Spring Core、Lombok、必要的轻量工具库；不依赖业务模块 |
| `Forgex_Common_Web` | MVC 拦截器、全局异常处理、国际化 Advice、Web 自动配置 | 可依赖 Contract/Core 和 Spring Web |
| `Forgex_Common_Data` | `BaseEntity`、MyBatis-Plus 配置、租户/数据权限拦截器、审计字段填充 | 可依赖 Contract/Core、MyBatis-Plus；不依赖业务 API |
| `Forgex_Common_Crypto` | 密码算法、传输加密、字段透明加密、文件加密 | 可依赖 Contract/Core、BouncyCastle；字段加密适配可依赖 Data |
| `Forgex_Common_Excel` | Excel 导入导出公共模型、处理流程与扩展接口 | 可依赖 Contract/Core、POI/FastExcel；不得依赖具体业务服务实现 |

现有无法合理放入上述模块的能力，例如许可证、Redis、MQ、通知和配置服务，不强塞进 Core。实施阶段根据实际依赖建立独立模块，或暂留兼容模块，直到边界明确。判断标准是：能力是否带来独立的重型第三方依赖，以及消费者是否可以按需选择。

### 2.2 业务域 API 模块

为接口提供方建立契约模块：

- `Forgex_Auth_Api`
- `Forgex_Sys_Api`
- `Forgex_Basic_Api`
- `Forgex_Workflow_Api`
- `Forgex_Integration_Api`

每个 API 模块只保存该服务对其他内部服务开放的 Feign Client、请求/响应 DTO、接口路径常量和必要注解。契约由提供方维护，消费者按需依赖。

API 模块允许依赖 `Forgex_Common_Contract`；如 Feign 定义需要，可依赖 OpenFeign API。API 模块禁止依赖提供方的业务实现模块、数据库模块、Web 自动配置、加密和 Excel。

当前 `com.forgex.common.api` 下的契约根据 Feign Client 的目标服务迁移到对应 API 模块。暂时保持 Java 包名不变，先完成 Maven 边界拆分，避免同时进行大规模 import 重命名。

## 3. 依赖规则

允许的总体方向：

```text
业务服务 -> 按需业务域 API -> Common_Contract
业务服务 -> 按需公共能力 -> Common_Contract / Common_Core
Common_Web / Data / Crypto / Excel -> Common_Contract / Common_Core
Common_Core -> JDK 和轻量基础库
```

禁止以下依赖：

- Common 模块依赖任一业务实现模块。
- 一个业务域 API 模块依赖另一个业务域 API 模块；确有共享模型时下沉到最小公共契约，或由调用方转换。
- Common Core 引入 Spring Boot Starter、OpenFeign、MyBatis、Redis、MQ、POI、BouncyCastle 等重依赖。
- 业务服务为了少写依赖而重新依赖包含全部能力的聚合模块。
- 通过 optional 依赖掩盖源码层面的强耦合。

Maven Enforcer 或 ArchUnit 应加入边界测试，阻止后续重新形成循环依赖和公共大模块。

## 4. 兼容和迁移策略

现有 `Forgex_Common` 暂时保留为兼容聚合模块，其自身不再保存实现源码，仅依赖拆分后的公共能力和业务 API 模块。它只服务于迁移期，不能被新代码新增依赖。

迁移分四批：

1. 建立 Contract、Core 和各业务域 API 模块，移动低耦合契约，保持包名和类名不变。
2. 建立 Web、Data、Crypto、Excel 等能力模块，按编译依赖迁移实现和资源文件，分别声明最小依赖。
3. 逐个业务服务将 `Forgex_Common` 替换为精确依赖。每迁移一个服务，执行该服务测试、启动上下文测试和依赖树检查。
4. 全部业务服务不再直接依赖兼容模块后，删除 `Forgex_Common` 聚合模块，并更新文档与构建脚本。

第一批优先选择依赖面较窄的服务验证模式，再迁移 Sys、Basic 等高耦合服务。Gateway 的 WebFlux/Sa-Token 依赖需要单独检查，不能直接复用 MVC 自动配置。

## 5. 自动配置与资源

当前 `AutoConfiguration.imports` 必须按能力拆到所属模块，避免引入某个 JAR 就意外启用无关配置。每项自动配置使用条件注解约束所需类、Bean 和配置项。

资源文件按所有者迁移：

- `ip2region` 数据文件跟随 IP 地址解析能力。
- 公共日志配置和 banner 不放入 Core，避免普通库污染应用资源；由各应用或专门 starter 管理。
- Mapper XML、国际化资源和 Spring 元数据必须随实现模块移动并通过打包测试验证。

## 6. 错误处理与兼容契约

- `R<T>` 字段、状态码和序列化行为保持不变。
- `X-Tenant-Id`、`X-User-Id`、`X-Lang` 等共享头保持不变。
- Feign Client 名称、服务名、路径和 DTO JSON 字段保持不变。
- 自动配置拆分后，如果某服务缺少必需模块，应在编译期或启动期给出明确错误，不能静默降级关键能力。
- 可选能力使用 `@ConditionalOnClass`、`@ConditionalOnBean` 或明确配置开关，避免 ClassNotFoundException。

## 7. 验证标准

功能验证：

- 后端 Reactor 全量编译和测试通过。
- 各服务 Spring 上下文可加载，Feign Client 可注册。
- 统一返回、国际化、租户上下文、数据权限、加密和 Excel 的现有测试通过。
- 使用 `mvn dependency:tree` 验证服务不再引入未使用的重能力。
- 使用边界测试验证禁止依赖规则。

构建效果验证：

- 记录改造前后至少三次同环境的第三方加密构建耗时，比较中位数。
- 检查每个服务最终待加密的 class 数、JAR 数和总体积。
- 若第三方工具扫描 Spring Boot fat JAR 的 `BOOT-INF/lib`，则 Maven 拆分本身不足以降低耗时。此时构建流程必须调整为复用已加密公共制品，或只处理业务服务的 `BOOT-INF/classes`；该调整需依据加密工具能力单独实施。

## 8. 完成条件

- 所有业务服务均使用精确 Maven 依赖，不再依赖兼容 `Forgex_Common`。
- 公共模块之间无循环依赖，Core 无重型依赖。
- 内部服务契约按提供方归属，消费者不会因无关领域 API 变更而重新构建。
- 全量测试和服务启动验证通过。
- 第三方加密构建耗时得到可量化改善；若未改善，已通过扫描范围证明确认原因并完成相应构建流程调整。
- 架构和后端文档中的旧 `Forgex_Common` 路径及职责说明完成更新。
