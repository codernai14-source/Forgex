# 消息模板与 SSE

> 分类：后端 / 配置与审计  
> 版本：**V0.5.0**  
> 更新时间：**2026-04-13**

## 一、概述

Forgex 消息模块提供 **消息模板管理**、**站内消息**、**SSE 实时推送** 三大能力，支持工作流审批通知、系统公告、业务事件等场景。

## 二、消息模板

### 2.1 模板表结构

**消息模板表 `sys_message_template`：**

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `template_code` | 模板编码（唯一标识） |
| `template_name` | 模板名称 |
| `template_content` | 模板内容（支持 `{0}`, `{1}` 占位符） |
| `template_type` | 模板类型（站内消息/邮件/短信等） |
| `receiver_type` | 接收人类型（指定用户/角色/部门等） |
| `tenant_id` | 租户 ID（0 为公共模板） |
| `enabled` | 是否启用 |

### 2.2 公共模板与租户模板

```
查询优先级：
1. 当前租户的模板 (tenant_id = currentTenantId)
2. 公共模板 (tenant_id = 0)
```

### 2.3 模板管理接口

| 接口 | 说明 |
|---|---|
| `POST /sys/messageTemplate/page` | 分页查询消息模板 |
| `POST /sys/messageTemplate/detail` | 模板详情 |
| `POST /sys/messageTemplate/save` | 保存模板 |
| `POST /sys/messageTemplate/delete` | 删除模板 |
| `POST /sys/messageTemplate/pullPublic` | 从公共模板拉取到当前租户 |

### 2.4 模板渲染

```java
// 模板内容："{0}的工单{1}已审批通过"
// 参数：["张三", "WO-2026-001"]
// 渲染结果："张三的工单WO-2026-001已审批通过"
```

## 三、接口返回消息模板

### 3.1 概述

`sys_response_message_template` 表用于定制 Controller 返回消息的多语言模板，与 `R<T>` 配合使用。

### 3.2 表结构

| 字段 | 说明 |
|---|---|
| `template_code` | 模板编码，与 `I18nPrompt.promptCode` 对应 |
| `template_content` | 消息模板内容 |
| `lang` | 语言代码 |
| `tenant_id` | 租户 ID |
| `enabled` | 是否启用 |

### 3.3 查询逻辑

```
TemplateConfigServiceImpl.resolveResponseMessage(templateCode, params)
  → 先查当前租户模板
  → 未找到则查公共模板 (tenant_id = 0)
  → 找到则渲染参数后返回
  → 未找到则返回 templateCode 原文
```

### 3.4 模板同步

支持将公共模板同步到指定租户：

```java
templateConfigService.syncResponseMessageTemplates(
    sourceTenantId,   // 源租户（通常为 0）
    targetTenantId,   // 目标租户
    templateCodes...  // 可选，指定模板编码
);
```

## 四、站内消息

### 4.1 消息表 `sys_message`

| 字段 | 说明 |
|---|---|
| `id` | 主键 |
| `title` | 消息标题 |
| `content` | 消息内容 |
| `type` | 消息类型（通知/审批/系统等） |
| `sender_id` | 发送人 ID |
| `receiver_id` | 接收人 ID |
| `tenant_id` | 租户 ID |
| `status` | 状态（0=未读, 1=已读） |
| `create_time` | 创建时间 |
| `read_time` | 阅读时间 |

### 4.2 消息接口

| 接口 | 说明 |
|---|---|
| `GET /sys/message/unread` | 获取未读消息列表 |
| `POST /sys/message/page` | 分页查询消息 |
| `POST /sys/message/read` | 标记单条消息已读 |
| `POST /sys/message/read-all` | 标记所有消息已读 |

## 五、SSE 实时推送

### 5.1 概述

SSE（Server-Sent Events）用于实时推送站内消息通知到前端，无需轮询。

### 5.2 连接建立

```
GET /sys/sse/connect
Content-Type: text/event-stream

前端通过 EventSource 建立长连接：
const es = new EventSource('/sys/sse/connect', {
  headers: { Authorization: 'Bearer xxx' }
});
es.onmessage = (event) => {
  const msg = JSON.parse(event.data);
  // 处理消息
};
```

### 5.3 推送流程

```
业务事件触发（如工作流审批通过）
  → 创建站内消息记录
  → 查找目标用户的 SSE 连接
  → 推送消息数据
  → 前端收到消息后更新未读数/弹出通知
```

### 5.4 与工作流联动

工作流审批完成后，通过消息模板生成通知并推送给相关人员：

```
审批通过 → 查找消息模板 → 渲染内容 → 保存站内消息 → SSE 推送
```

## 六、MQ 消息模板

### 6.1 概述

`sys_mq_message_template` 表管理 RocketMQ 等消息队列的消息模板。

### 6.2 用途

- 业务事件通知的异步发送
- 跨服务消息模板统一管理
- 支持模板同步（公共 → 租户）

## 七、关联文档

- [统一返回与国际化](./统一返回与国际化.md)
- [后端公共能力与核心功能手册](../后端公共能力与核心功能手册.md)
- [工作流与报表](../模块专题/工作流与报表.md)
