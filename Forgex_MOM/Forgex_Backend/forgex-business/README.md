# forgex-business 业务服务分组

> 分类：后端 / 架构
> 版本：**V1.1.0**
> 更新时间：**2026-09-19**
> 适用范围：平台内置业务服务（基础数据）与企业二开独立业务服务的开发接入

本目录是**业务服务分组**，包含两类业务服务：

| 形态 | 工程 | 构建方式 | 说明 |
|---|---|---|---|
| 平台内置业务服务 | `forgex-business-basic/`（`Forgex_Basic` + `Forgex_Basic_Api`） | 在平台 Reactor 内（根 `pom.xml` 聚合） | 基础数据与物料管理，随平台一起交付，是业务域的参考实现 |
| 企业独立业务服务 | `company-order-service/`（示例工程） | **不在**平台 Reactor，独立 parent/构建/部署 | 公司二开服务的模板，演示只依赖公共包即可独立启动 |

`artifactId`、Java 包名、Spring 服务名、JAR 名与端口均保持不变；`Forgex_Basic` 从 `forgex-admin` 迁到本分组只是物理归类调整，运行行为与交付产物不受影响。

## 1. 依赖边界（必守）

**平台内置业务服务**（`Forgex_Basic`，在 Reactor 内）：

| 层 | 依赖 |
|---|---|
| 公共能力 | `Forgex_Common_*` 按需精确依赖（Contract/Core/Web/Data/Excel/Infra 等） |
| 平台治理运行时/客户端 | `forgex-admin-runtime` + `forgex-admin-client`（随平台交付的服务可用） |
| 平台契约 | `Forgex_Sys_Api` 等，通过 Feign 服务发现调用，不依赖平台实现 JAR |

**企业独立业务服务**（如 `company-order-service`，脱离本仓库建仓）：

| 层 | 依赖 | 说明 |
|---|---|---|
| 纯公共层 | `Forgex_Common_Contract` / `Core` / `Domain_Contract` / `Crypto` | 不依赖平台服务，不要求 Forgex 数据库 |
| 启动能力 | `forgex-common-starter` | MVC + JSON 契约 + 业务异常兜底，不绑定平台 |
| 可选平台契约 | `Forgex_Sys_Api`、`Forgex_Workflow_Api`、`Forgex_Auth_Api` 等 | **确实调用平台接口时才引入**，通过服务发现调用 |
| 平台实现（禁止） | `forgex-admin-runtime`、`Forgex_Sys`、`Forgex_Basic` 等服务 JAR | 企业服务不得依赖平台实现与平台库表 |

企业服务版本统一由 `forgex-common-bom` 管理：

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
</dependencies>
```

## 2. 独立启动前提（企业服务）

1. 从公司私服获取 `forgex-common-*` 系列构件（或本地 `mvn -f forgex-common/pom.xml -DskipTests install` 安装）。
2. 业务服务自带启动类，只扫描**自己的业务包**（如 `com.company.order`），公共能力经 Starter 自动装配，不要扫描 `com.forgex` 平台实现包。
3. 不引入平台数据库（`forgex_*`）也能启动：用户补全、编码规则、审计等能力在缺省时自动退避（SPI 无实现即降级）。
4. 需要接入平台时：注册到同一 Nacos，通过 `*_Api` + Feign 服务发现调用，并在启动类上 `@EnableFeignClients(clients = {...})` **显式启用**所需客户端。

## 3. 与平台的调用规范

- **统一契约**：请求/响应遵循 `R<T>`（`code == 200` 为成功）。
- **共享请求头**（网关与 Feign 拦截器自动透传，业务侧不要自行消费/改写）：
  - `X-Tenant-Id`：租户 ID
  - `X-User-Id`：用户 ID
  - `X-Lang`：语言
- **外部流量**：统一经 `forgex-admin-gateway` 接入；业务服务不直接暴露公网/局域网入口。
- **内部调用**：服务发现 + Feign（`*_Api` 契约）；不要把 Gateway 当内部 SDK 使用。
- **内部端点**：平台侧 `.../internal/**` 端点仅供服务间调用；部分受控内部接口（如 Sys 备份）要求一次性 Redis 工单票据，票据由提供方签发、限时限次。
- **平台能力扩展点**（公共 SPI，业务可自行实现替代默认退避）：
  - `com.forgex.common.spi.UserDirectory` — 用户展示名补全
  - `com.forgex.common.spi.EncodeRuleProvider` — 编码规则生成
  - `com.forgex.common.audit.OperationLogRecorder` — 操作日志上报

## 4. 参考样例：company-order-service

| 场景 | 命令 | 验证点 |
|---|---|---|
| 仅公共包独立构建 | `mvn -f forgex-business/company-order-service/pom.xml package` | 不出现任何 `forgex-admin-*` / `Forgex_Sys` 依赖 |
| 可选接入平台用户接口 | `mvn -f forgex-business/company-order-service/pom.xml -Pplatform-api package` | `src/platform-api` 源集参与编译，`SysUserFeignClient` 可用 |

样例展示了两种形态：默认只有业务 Controller + Starter；`platform-api` profile 演示按需引入 `Forgex_Sys_Api` 并用 Feign 直连平台（`FORGEX_SYS_URL` 可配）。平台内置业务服务的代码组织与平台契约接入方式可参考 `forgex-business-basic/Forgex_Basic`。

## 5. 新建业务服务清单

1. 复制 `company-order-service` 为模板，改 `groupId`/`artifactId`/包名/端口。
2. `dependencyManagement` 引入 `forgex-common-bom`，依赖只加 `forgex-common-starter` + 实际需要的 `*_Api`。
3. 启动类只扫描业务包；需要 Feign 时显式 `@EnableFeignClients(clients = {...})`。
4. 注册中心/配置中心使用平台同一 Nacos 命名空间；数据库使用业务自己的库。
5. 部署形态与平台服务一致（可执行 JAR + WinSW/docker-compose），交付脚本见 `Forgex_Build`。

## 6. 关联文档

- [Common 模块化拆分与二开指南](../../../Forgex_Doc/后端/公共能力/Common模块化拆分与二开指南.md)
- [内部服务接口开放说明](../../../Forgex_Doc/后端/公共能力/内部服务接口开放说明.md)
- [后端文档导航](../../../Forgex_Doc/后端/README.md)
