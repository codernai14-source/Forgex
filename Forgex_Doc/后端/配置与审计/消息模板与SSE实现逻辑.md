# 消息模板与SSE实现逻辑

> 分类：后端 / 配置与审计
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文重点解释四条链路：

- 消息模板配置怎么存
- 模板消息怎么渲染并落站内消息
- SSE 怎么建立长连接并推送
- RocketMQ 异步消息如何回流到模板消息服务

## 代码入口

| 能力 | 代码位置 | 说明 |
|---|---|---|
| 模板管理接口 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/controller/SysMessageTemplateController.java` | 模板分页、详情、保存、删除、拉取公共模板 |
| 站内消息接口 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/controller/SysMessageController.java` | 发送、未读、已读、分页、按模板发送 |
| SSE 建连接口 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/controller/SysMessageSseController.java` | `GET /sys/message/stream` |
| 模板消息发送实现 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/TemplateMessageServiceImpl.java` | 渲染模板、落消息、推 SSE |
| 模板配置同步实现 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/service/template/TemplateConfigServiceImpl.java` | 公共模板同步到租户、响应模板/MQ 模板/导出模板同步 |
| SSE 管理服务 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/SseEmitterService.java` | 建连、心跳、清理、定向推送 |
| 系统通知接口 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/controller/SysNoticeController.java` | 系统通知分页、保存、发布/停用、首页待弹列表、用户确认 |

## 数据模型

消息模板相关至少涉及这些表或实体：

- `SysMessageTemplate`
- `SysMessageTemplateContent`
- `SysMessageTemplateReceiver`
- `SysMessage`
- `SysResponseMessageTemplate`
- `SysMqMessageTemplate`

系统通知使用独立的数据模型：

- `SysNotice`：通知标题、公共/租户范围、富文本 HTML、摘要、状态、生效失效时间、排序和强提醒标识
- `SysNoticeAttachment`：通知附件文件名、URL、大小和类型
- `SysNoticeUserRecord`：用户弹窗确认记录，用于同一用户同一通知只弹一次

公共通知以 `scope = PUBLIC`、`tenant_id = 0` 保存，租户通知以 `scope = TENANT`、当前租户 ID 保存。查询时服务层临时跳过 MyBatis 租户拦截，并由业务条件显式限定“公共或当前租户”，避免公共通知被当前租户拦截条件过滤。

可以把它理解为两层：

1. 配置层，定义“应该发什么、发给谁、发到哪个平台”
2. 运行层，真的生成 `sys_message` 记录并推送到在线用户

## 模板管理接口链路

`SysMessageTemplateController` 提供这些能力：

- `/sys/message-template/page`
- `/sys/message-template/get`
- `/sys/message-template/save`
- `/sys/message-template/delete`
- `/sys/message-template/delete-batch`
- `/sys/message-template/pull-public`

公共模板拉取链路：

```text
租户管理员点击拉取公共模板
  -> /sys/message-template/pull-public
  -> messageTemplateService.pullPublicConfig()
  -> 把 tenant_id = 0 的公共模板复制到当前租户
```

## 模板消息发送链路

模板消息真正执行发送时，主实现是 `TemplateMessageServiceImpl`。

```text
业务调用 sendByTemplate / sendToUser
  -> 按 templateCode 查 SysMessageTemplate
  -> 校验模板是否启用
  -> 查 SysMessageTemplateContent
  -> 解析接收人
  -> 渲染标题与正文占位符
  -> 生成 SysMessage
  -> messageMapper.insert()
  -> sseEmitterService.sendToUser()
```

### 占位符渲染

`TemplateMessageServiceImpl` 使用：

- `${key}` 作为模板占位符
- `fillPlaceholders()` 做内容替换
- 多语言标题/正文为空时，可回退到 i18n JSON 内容

### 平台过滤

当前站内消息发送时只处理：

- `platform = INTERNAL`

## 接收人解析机制

`TemplateMessageServiceImpl.resolveReceiverUserIds()` 会把模板接收人配置解析成最终用户 ID。当前稳定支持：

- `USER`
- `ROLE`
- `DEPT`
- `POSITION`

解析规则：

- `USER`：按配置的用户 ID 过滤当前租户内启用、未删除用户
- `ROLE`：调用用户服务按角色 ID 展开用户
- `DEPT`：调用用户服务按部门 ID 展开用户
- `POSITION`：调用用户服务按岗位 ID 展开用户
- 多条接收人规则会合并去重

以下类型表示“调用时动态传入”，模板配置本身不展开用户：

- `CUSTOM`

MQ 场景下，如果模板存在 `CUSTOM` 接收人且请求中传入 `receiverUserIds`，会优先使用请求中的动态接收人；否则继续尝试解析模板配置。

## 站内消息接口链路

`SysMessageController` 提供：

- `/sys/message/send`
- `/sys/message/unread`
- `/sys/message/unread-count`
- `/sys/message/read`
- `/sys/message/read-all`
- `/sys/message/page`
- `/sys/message/send-by-template`
- `/sys/message/send-to-user`
- `/sys/message/unread-summary`

重要实现特点：

- 接口层先检查 `TenantContext`、`UserContext`
- 未登录直接返回失败
- `send-by-template` 支持批量接收人
- `send-to-user` 是单人便捷接口

## SSE 长连接实现

当前项目里的主 SSE 管理实现是 `SseEmitterService`。

建连链路：

```text
前端发起 GET /sys/message/stream
  -> SysMessageSseController.stream()
  -> 读取 TenantContext / UserContext
  -> sseEmitterService.connect(tenantId, userId)
  -> 返回 SseEmitter
  -> 立即推送 connected 事件
```

连接以 `tenantId:userId` 作为 key，value 是连接列表，因此同一用户可以有多个页面、多终端连接。

心跳与清理：

- 每 30 秒发送一次 `heartbeat`
- 每 5 分钟清理超时无心跳连接

## 模板配置服务的另一条链路

`TemplateConfigServiceImpl` 负责“配置模板的租户同步与模板取值”。

它当前处理三类模板同步：

- `SysResponseMessageTemplate`
- `SysMqMessageTemplate`
- `FxExcelExportConfigTemplate`

查询优先级：

```text
当前租户模板
  -> 若不存在
  -> 公共模板 tenant_id = 0
```

## 与 RocketMQ 的关系

模板消息除了同步发送，还支持 RocketMQ 异步发送。

异步链路是：

```text
业务发送 TemplateMessageRequest 到 MQ
  -> RocketMQ Consumer 收到消息
  -> TemplateMessageServiceImpl.processTemplateMessageFromMq()
  -> 恢复 tenant/user 上下文
  -> 继续走标准模板发送流程
```

## 与前端的对应代码

前端真实入口在：

- `Forgex_MOM/Forgex_Fronted/src/api/message.ts`
- `Forgex_MOM/Forgex_Fronted/src/api/system/message.ts`
- `Forgex_MOM/Forgex_Fronted/src/hooks/useSse.ts`
- `Forgex_MOM/Forgex_Fronted/src/stores/sse.ts`
- `Forgex_MOM/Forgex_Fronted/src/components/Notification/MessageNotification.vue`
- `Forgex_MOM/Forgex_Fronted/src/views/system/messageTemplate/index.vue`
- `Forgex_MOM/Forgex_Fronted/src/views/system/message/index.vue`

## 常见问题与实现边界

### 模板保存成功但发不出去

常见原因：

1. 模板未启用
2. 内容表未配置 `INTERNAL`
3. 接收人配置无法解析出当前租户内启用用户
4. 调用时没有租户上下文

### SSE 建连成功但页面没有新消息

先分开看：

1. 消息是否真的入 `sys_message`
2. `sendToUser()` 是否执行
3. 用户是否用同一个 `tenantId + userId` 建连

## 关联文档

- [消息模板与SSE使用方式](./消息模板与SSE使用方式.md)
- [RocketMQ实现逻辑](../模块专题/RocketMQ实现逻辑.md)
- [消息模板预览与接收人选择实现逻辑](../../前端/组件与页面/消息模板预览与接收人选择实现逻辑.md)
