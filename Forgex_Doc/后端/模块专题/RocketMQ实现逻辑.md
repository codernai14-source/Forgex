# RocketMQ实现逻辑

> 分类：后端 / 模块专题
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文说明当前 Forgex 项目里 RocketMQ 的真实作用范围、发送消费链路以及配置入口。当前项目中最清晰、最成型的 RocketMQ 业务落点，是“模板消息异步发送”。

## 代码入口

| 能力 | 代码位置 | 说明 |
|---|---|---|
| MQ 消息体 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/mq/message/TemplateMessageRequest.java` | 模板消息异步载体 |
| MQ 发送接口 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/mq/message/TemplateMessageSender.java` | 发送能力抽象 |
| RocketMQ 发送实现 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/mq/message/impl/TemplateMessageSenderImpl.java` | 基于 `RocketMQTemplate` 发送 |
| MQ 基础发送抽象 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/util/mq/MqSender.java` | 更通用的 MQ 抽象 |
| MQ 空实现 | `Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/util/mq/NoopMqSender.java` | 未接入真实 MQ 时兜底 |
| RocketMQ 消费者 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/mq/TemplateMessageRocketMqConsumer.java` | 消费模板消息 |
| 模板消息消费处理 | `Forgex_MOM/Forgex_Backend/Forgex_Sys/src/main/java/com/forgex/sys/service/impl/TemplateMessageServiceImpl.java` | MQ 消费后继续走模板消息流程 |
| Nacos 示例配置 | `Forgex_Doc/部署/nacos配置/DEFAULT_GROUP/rocketmq.yml` | NameServer、生产者、消费者基础配置 |

## 当前 RocketMQ 的业务定位

当前项目中最明确的业务链路是：

```text
业务构造 TemplateMessageRequest
  -> TemplateMessageSenderImpl.sendToMq()
  -> RocketMQ Topic
  -> TemplateMessageRocketMqConsumer.onMessage()
  -> TemplateMessageServiceImpl.processTemplateMessageFromMq()
  -> sys_message 入库
  -> SSE 推送在线用户
```

RocketMQ 在这里承担的是“模板消息异步削峰与解耦”，不是替代模板消息服务本身。

## 发送端实现

`TemplateMessageSenderImpl` 是当前项目的核心发送实现。

### 生效条件

该 Bean 同时满足以下条件才会装配：

1. 容器中存在 `RocketMQTemplate`
2. 配置 `mq.template-message.enabled=true`

### 发送行为

发送时会做这些事情：

1. 校验请求对象和 `templateCode`
2. 若未传 `tenantId`，尝试从 `TenantContext` 取
3. 若未传 `senderUserId`，尝试从 `UserContext` 取
4. 把请求序列化为 JSON
5. 拼接 `topic[:tag]`
6. 通过 `rocketMQTemplate.convertAndSend()` 发出

默认配置：

- topic：`mq.topic.template-message`，默认 `forgex.message.template.topic`
- tag：`mq.producer.template-message-tag`，默认 `TEMPLATE_MESSAGE`

## 消费端实现

消费者类是 `TemplateMessageRocketMqConsumer`。

监听配置：

- topic：`${mq.topic.template-message:forgex.message.template.topic}`
- consumerGroup：`${mq.consumer.template-message-group:forgex-sys-template-message-consumer}`
- selectorExpression：`${mq.consumer.template-message-tag:TEMPLATE_MESSAGE}`

消费流程：

```text
收到 String 类型 JSON 消息
  -> ObjectMapper 反序列化为 TemplateMessageRequest
  -> 调用 templateMessageService.processTemplateMessageFromMq(request)
```

## MQ 消费后的上下文恢复

`TemplateMessageServiceImpl.processTemplateMessageFromMq()` 会：

1. 取出旧的 `TenantContext`、`UserContext`
2. 用请求里的 `tenantId`、`senderUserId` 临时恢复上下文
3. 按模板消息标准流程继续处理
4. 最后恢复原上下文

这一步解决了异步消费线程没有原始请求上下文的问题，是多租户消息正确落地的关键。

## RocketMQ 配置入口

当前文档仓库里的示例配置在：

- `Forgex_Doc/部署/nacos配置/DEFAULT_GROUP/rocketmq.yml`

已有示例键包括：

- `rocketmq.name-server`
- `rocketmq.access-key`
- `rocketmq.secret-key`
- `rocketmq.producer.group`
- `rocketmq.consumer.group`

模板消息链路还会额外依赖：

- `mq.template-message.enabled`
- `mq.topic.template-message`
- `mq.producer.template-message-tag`
- `mq.consumer.template-message-group`
- `mq.consumer.template-message-tag`

## 常见问题排查

### 发送端没生效

重点检查：

1. 容器里是否有 `RocketMQTemplate`
2. `mq.template-message.enabled` 是否为 `true`
3. topic / tag 是否正确

### 消息发到 MQ 但页面没有通知

分段检查：

1. Consumer 是否启动
2. `TemplateMessageRocketMqConsumer` 是否收到消息
3. `processTemplateMessageFromMq()` 是否正常执行
4. `sys_message` 是否入库
5. SSE 是否已建连

## 关联文档

- [RocketMQ使用方式](./RocketMQ使用方式.md)
- [消息模板与SSE实现逻辑](../配置与审计/消息模板与SSE实现逻辑.md)
