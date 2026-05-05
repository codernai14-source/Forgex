# RocketMQ使用方式

> 分类：后端 / 模块专题
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文说明当前项目里 RocketMQ 怎么配、代码怎么用、页面联调时怎么看结果，以及什么时候不该误判为“MQ 有问题”。

## 先明确使用范围

当前项目里，RocketMQ 最明确的使用场景是：

- 模板消息异步发送

## 代码如何使用

### 1. 发送模板消息到 RocketMQ

后端业务代码注入 `TemplateMessageSender` 后，可以这样发：

```java
TemplateMessageRequest request = new TemplateMessageRequest();
request.setTemplateCode("WORKFLOW_APPROVE_NOTICE");
request.setTenantId(1L);
request.setSenderUserId(1001L);
request.setReceiverUserIds(List.of(1002L, 1003L));
request.setBizType("WORKFLOW");
request.setDataMap(Map.of(
        "orderNo", "PO20260417001",
        "status", "待审批"
));

templateMessageSender.sendToMq(request);
```

建议至少传：

- `templateCode`
- `tenantId`
- `receiverUserIds`
- `dataMap`

## 配置如何使用

### 1. 底层连接配置

Nacos 示例可参考：

- `Forgex_Doc/部署/nacos配置/DEFAULT_GROUP/rocketmq.yml`

示例内容至少包括：

```yaml
rocketmq:
  name-server: 127.0.0.1:9876
  producer:
    group: forgex-producer-group
  consumer:
    group: forgex-consumer-group
```

### 2. 业务主题配置

模板消息链路还需要补齐以下业务键：

```yaml
mq:
  template-message:
    enabled: true
  topic:
    template-message: forgex.message.template.topic
  producer:
    template-message-tag: TEMPLATE_MESSAGE
  consumer:
    template-message-group: forgex-sys-template-message-consumer
    template-message-tag: TEMPLATE_MESSAGE
```

## 页面如何联调

RocketMQ 本身没有单独页面，它通常通过消息模板页和消息中心页间接体现结果。

联调页面入口：

- `Forgex_MOM/Forgex_Fronted/src/views/system/messageTemplate/index.vue`
- `Forgex_MOM/Forgex_Fronted/src/views/system/message/index.vue`
- `Forgex_MOM/Forgex_Fronted/src/components/Notification/MessageNotification.vue`

完整联调流程：

1. 在模板配置页先配好模板
2. 后端通过 `TemplateMessageSender.sendToMq()` 发消息
3. RocketMQ 消费后写入 `sys_message`
4. 如果用户在线，页面通过 SSE 收到实时通知
5. 在消息中心页确认消息记录

## 本地开发怎么用

### 方案 1：本地真启 RocketMQ

适合验证完整异步链路。

### 方案 2：本地不启 RocketMQ，只测同步模板消息

如果当前只是验证模板内容和页面展示，可以先走同步模板发送接口：

- `POST /sys/message/send-by-template`
- `POST /sys/message/send-to-user`

## 常见误判

### 误判 1：页面没收到消息，就是 RocketMQ 坏了

不一定。还可能是：

- 模板本身没启用
- 消费者没启动
- 接收人没解析出来
- SSE 没建连

### 误判 2：生产者日志打印成功，消息就一定完成了

生产者成功只说明消息已经发出，不说明消费成功、站内消息已入库或页面已收到。

## 排查清单

| 现象 | 先查什么 |
|---|---|
| 调用 `sendToMq` 时报发送异常 | NameServer、topic、tag、网络连通性 |
| 没有消费者日志 | 消费者服务是否启动，group/tag 是否匹配 |
| 消费有日志但没有站内消息 | `processTemplateMessageFromMq()` 是否报错，模板是否可用 |
| 站内消息有了但前端没弹 | SSE 连接、前端 store、通知组件是否正常 |

## 关联文档

- [RocketMQ实现逻辑](./RocketMQ实现逻辑.md)
- [消息模板与SSE使用方式](../配置与审计/消息模板与SSE使用方式.md)
