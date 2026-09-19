# SSE 推送实现逻辑

> 分类：后端 / 配置与审计
> 版本：**V1.0.0**

## 源码位置

- 连接入口：`Forgex_MOM/Forgex_Backend/forgex-admin/forgex-admin-sys/Forgex_Sys/src/main/java/com/forgex/sys/controller/SysMessageSseController.java`
- 按租户和用户管理连接：`Forgex_MOM/Forgex_Backend/forgex-admin/forgex-admin-sys/Forgex_Sys/src/main/java/com/forgex/sys/service/SseEmitterService.java`
- 旧的任务进度推送服务：`Forgex_MOM/Forgex_Backend/forgex-admin/forgex-admin-sys/Forgex_Sys/src/main/java/com/forgex/sys/service/SsePushService.java`
- 前端封装：`Forgex_MOM/Forgex_Fronted/src/hooks/useSse.ts`、`src/stores/sse.ts`

## 当前实现的两条链路

系统中有两套不能混用的实现：

1. `SseEmitterService` 是系统消息链路。`GET /message/stream` 从 `TenantContext` 和 `UserContext` 读取当前租户、用户，把连接按 `tenantId:userId` 保存。同一用户可以有多个浏览器连接；连接超时 30 分钟，每 30 秒发送 `heartbeat`，每 5 分钟清理超过 5 分钟未心跳的连接。连接建立后先发送 `connected` 事件，数据包含 `connectionId`、`timestamp`、`message`。
2. `SsePushService` 是旧的任务进度链路，按任意 `clientId` 保存一个 `SseEmitter`，超时设置为 `0`，并用单线程线程池每 30 秒发一个普通对象心跳。它提供 `push`、`pushProgress`、`pushComplete`、`closeConnection`，没有租户隔离和多连接列表。

## 整体流程

```text
浏览器携带 Cookie 请求 /message/stream
        -> Controller 读取租户和用户上下文
        -> 未登录返回立即关闭的 emitter
        -> SseEmitterService.connect 建立连接并发送 connected
        -> 连接登记到 tenantId:userId 列表
        -> 业务调用 sendToUser/sendToTenant
        -> emitter 发送命名事件
        -> 发送异常、超时或完成时移除连接
        -> 定时任务发送 heartbeat 并清理失效连接
```

`sendToUser(null, userId, event, data)` 会遍历所有租户中该用户的连接；传入租户 ID 时只发送给该租户。`sendToTenant` 可以广播到一个租户，租户 ID 为空时广播到进程内全部连接。连接池是 JVM 内存结构，多实例部署不会自动把事件转发到其他实例。

## 二次开发边界

- 新事件应通过 `sendToUser(tenantId, userId, event, data)` 或 `sendToTenant` 发送，事件名和 JSON 字段需形成稳定契约。
- 多实例部署需要在 `SseEmitterService` 外增加 Redis、MQ 或网关粘性会话；当前源码没有跨实例转发。
- `SsePushService` 的 `clientId` 可能被覆盖，不能把它当作多租户消息服务使用。若继续保留，应补充关闭线程池、连接数限制和租户校验。
- 反向代理必须允许 `text/event-stream` 长连接并关闭缓冲；这属于部署配置，源码未做兜底。

## 未完成项

前端初始化代码中的默认 URL 是 `/sse/connect`，后端实际 URL 是 `/message/stream`，两者当前没有对齐；`messageNotification.ts` 中的 `watch` 还是占位函数。因此“登录后自动收到通知”尚未形成可验收闭环，需要统一 URL、改用真实 Vue `watch`，并补充浏览器登录、断线重连和多实例验证。
