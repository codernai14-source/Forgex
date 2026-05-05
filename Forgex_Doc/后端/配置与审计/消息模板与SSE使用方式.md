# 消息模板与SSE使用方式

> 分类：后端 / 配置与审计
> 版本：**V0.6.5**
> 更新时间：**2026-04-17**

本文从“配置模板”“代码如何发送”“页面如何使用”“实时推送如何排查”四个维度写使用方式。

## 页面如何使用

### 1. 消息模板配置页

前端页面入口：

- `Forgex_MOM/Forgex_Fronted/src/views/system/messageTemplate/index.vue`

接口入口：

- `Forgex_MOM/Forgex_Fronted/src/api/system/message.ts`
- `Forgex_MOM/Forgex_Fronted/src/api/message.ts`

页面上通常要完成这些操作：

1. 查看模板分页
2. 新增或编辑模板主信息
3. 配置模板标题、正文、多语言内容
4. 配置接收人
5. 启用或停用模板
6. 从公共模板拉取到当前租户

### 2. 消息中心页面

前端页面入口：

- `Forgex_MOM/Forgex_Fronted/src/views/system/message/index.vue`

页面能力通常包括：

- 查看消息分页
- 查看未读消息
- 标记已读
- 全部已读

### 3. 顶部通知与实时提醒

前端代码入口：

- `Forgex_MOM/Forgex_Fronted/src/components/Notification/MessageNotification.vue`
- `Forgex_MOM/Forgex_Fronted/src/hooks/useSse.ts`
- `Forgex_MOM/Forgex_Fronted/src/stores/sse.ts`

## 代码如何使用

### 1. 直接按模板发送

单用户发送：

```java
Long messageId = templateMessageService.sendToUser(
        "WORKFLOW_APPROVE_NOTICE",
        1001L,
        Map.of("orderNo", "PO20260417001", "status", "待审批"),
        "WORKFLOW"
);
```

多用户发送：

```java
int count = templateMessageService.sendByTemplate(
        "WORKFLOW_APPROVE_NOTICE",
        List.of(1001L, 1002L),
        Map.of("orderNo", "PO20260417001"),
        "WORKFLOW"
);
```

### 2. 通过接口发送

后端已经提供现成接口：

- `POST /sys/message/send-by-template`
- `POST /sys/message/send-to-user`

### 3. 使用模板接收人配置

如果模板的接收人已经在后台配置好，可以调用：

```java
int count = templateMessageService.sendByTemplate("SYSTEM_NOTICE", dataMap);
```

但当前最稳定的接收人类型是：

- `USER`
- `CUSTOM`

### 4. 拉取公共模板

租户初始化时可调用：

```text
POST /sys/message-template/pull-public
```

## 模板配置建议

### 模板编码怎么定

推荐使用稳定编码，不要把展示文案直接当模板编码。

示例：

- `WORKFLOW_APPROVE_NOTICE`
- `ORDER_FINISH_NOTICE`
- `LOGIN_REMIND_NOTICE`

### 标题和正文怎么写

推荐使用占位符：

```text
标题：单据 ${orderNo} 待审批
正文：申请人 ${userName} 提交的单据 ${orderNo} 当前状态为 ${status}
```

### 接收人怎么配

建议按当前实现能力使用：

- 固定几个人：配 `USER`
- 调用时动态传：配 `CUSTOM`

## SSE 如何使用

### 前端建连方式

登录后调用：

```text
GET /sys/message/stream
```

然后监听至少三类事件：

- `connected`
- `heartbeat`
- `message`

### 前端代码接入位置

推荐统一接到：

- `useSse.ts`
- `stores/sse.ts`

## 常见联调方式

### 场景 1：新增模板并验证

1. 在模板配置页新增模板
2. 选择 `INTERNAL` 内容
3. 配接收人或准备接口动态传人
4. 调用 `send-to-user` 或 `send-by-template`
5. 去消息中心确认记录
6. 看顶部通知是否实时出现

### 场景 2：公共模板同步到租户

1. 在公共租户维护模板
2. 当前租户点击“拉取公共模板”
3. 再在租户模板页确认是否出现同编码模板
4. 发送测试消息验证

## 常见问题排查

| 现象 | 排查方向 |
|---|---|
| 模板配置页保存成功但发送没消息 | 模板状态、内容平台是否为 `INTERNAL`、接收人是否可解析 |
| 页面收到 SSE connected 但收不到 message | 消息是否成功入库、用户上下文是否一致 |
| 公共模板拉取后页面没看到 | 当前页面是否过滤了公共/租户配置、是否已有覆盖模板 |
| 未读数不更新 | 检查前端是否监听 `message` 后刷新未读接口 |
| 标题正文占位符没替换 | 检查 `dataMap` key 是否和模板占位符完全一致 |

## 关联文档

- [消息模板与SSE实现逻辑](./消息模板与SSE实现逻辑.md)
- [RocketMQ使用方式](../模块专题/RocketMQ使用方式.md)
- [消息模板预览与接收人选择使用方式](../../前端/组件与页面/消息模板预览与接收人选择使用方式.md)
