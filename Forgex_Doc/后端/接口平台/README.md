# 接口平台

> 分类：后端 / 接口平台模块  
> 版本：**V1.0.0**

接口平台源码模块为 `Forgex_MOM/Forgex_Backend/Forgex_Integration`，负责把内部业务数据转换为第三方请求，也负责接收第三方推送并转换为内部处理器参数。文档按完整功能组织；配置、授权、参数映射、调用、日志和内部集成分别有独立文件，但一次联调仍应按本文的顺序完成。

## 文档地图

| 功能 | 文档 |
|---|---|
| 模块总览与接入顺序 | 本文 |
| 接口配置与出站目标 | [接口配置.md](./接口配置.md) |
| 参数树与字段映射 | [参数配置与映射.md](./参数配置与映射.md) |
| 第三方系统登记 | [第三方系统.md](./第三方系统.md) |
| Token、白名单和过期策略 | [第三方授权.md](./第三方授权.md) |
| 入站公共接口 | [公共入站接口.md](./公共入站接口.md) |
| 内部调用第三方 | [出站调用.md](./出站调用.md) |
| 异步任务与结果 | [异步任务.md](./异步任务.md) |
| 调用日志、动态月表和首页统计 | [调用日志与监控.md](./调用日志与监控.md) |
| 客户、员工、物料、供应商、用户集成 | [内部业务集成.md](./内部业务集成.md) |
| 错误码、国际化和排障 | [错误处理与国际化.md](./错误处理与国际化.md) |

## 源码分层

- Controller：`Forgex_Integration/src/main/java/com/forgex/integration/controller`
- 配置快照：`service/impl/ApiDefinitionServiceImpl.java`
- 网关编排：`service/impl/ApiGatewayServiceImpl.java`
- 参数组装：`service/impl/ApiParamAssemblerImpl.java`
- 出站 HTTP：`service/impl/ApiOutboundExecutorImpl.java`
- 异步任务：`service/impl/ApiTaskServiceImpl.java`、`ApiTaskConsumer.java`
- 日志：`ApiCallLogServiceImpl.java`、`ApiCallLogTableServiceImpl.java`、`ApiLogBufferServiceImpl.java`
- Web API：`Forgex_MOM/Forgex_Fronted/src/api/system/integration.ts`

## 一次完整接入

1. 登记第三方系统，记录 `thirdSystemId`。
2. 按入站或出站方向创建 `ApiConfig`，确认 `apiCode` 唯一。
3. 出站接口配置一个或多个 `ApiOutboundTarget`；入站接口配置 `processorBean`。
4. 为请求和响应导入参数树，数组节点必须有叶子字段。
5. 在参数映射中逐字段配置 `BODY/QUERY/HEADER/PATH` 和取值类型。
6. 入站接口配置 `authType` 与授权；出站接口用 `IntegrationFacade` 调用。
7. 先用同步接口联调，再启用异步；从调用日志的 `traceId/taskId` 验证链路。

## 统一约定

- 管理接口默认使用 `POST + JSON`，查询详情和树读取使用源码声明的 `GET`。
- Web 前端调用 `src/api/http.ts`，路径从 `/integration` 开始，不重复写 `/api`。
- 后端返回 `R<T>`；`code == 200` 才表示成功，错误文案来自 `IntegrationPromptEnum` 和 `fx_i18n_message`。
- `root` 只是参数树根节点；运行时 JSON 不需要包一层 `root`。
- Long ID 在 Web 侧按字符串比较，不能转 Number。

## 当前边界

平台已经实现配置 CRUD、参数树导入、字段映射、入站同步/异步执行、出站多目标调用和动态调用日志。源码没有提供通用的“在线连通性测试”接口，也没有在保存映射时验证第三方字段语义；这两项需要后续增加校验服务和测试接口，不能在文档中当作现成功能。
