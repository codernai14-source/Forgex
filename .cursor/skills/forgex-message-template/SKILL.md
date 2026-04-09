# Forgex 公共消息模板使用指南

## 一、技能概述

本技能提供 Forgex 项目公共消息模板系统的完整使用指导，帮助开发者快速集成消息通知功能。消息模板系统基于 MQ 异步消息机制，实现业务服务与消息服务的解耦，提供灵活、可配置的消息通知能力。

### 1.1 核心功能

- **模板化消息**：通过预定义的模板配置，动态生成消息内容
- **多级配置**：支持公共级、租户级、租户类型级三级配置
- **实时推送**：基于 SSE（Server-Sent Events）实现消息实时推送
- **多样化通知**：支持 5 种通知图标类型，适配不同业务场景
- **灵活的接收人**：支持 5 种接收人类型，包括动态指定和配置读取

### 1.2 架构设计

```
┌─────────────────┐         MQ          ┌─────────────────┐
│  业务服务       │ ──────────────────> │  Sys 服务       │
│  (Auth/Job 等)   │                     │  (消息中心)      │
│                 │                     │                 │
│  - 发送 MQ 消息   │                     │  - 消费 MQ 消息    │
│  - 调用服务接口  │                     │  - 解析模板      │
│                 │                     │  - 保存消息      │
│                 │                     │  - SSE 推送      │
└─────────────────┘                     └─────────────────┘
                                                │
                                                │ SSE
                                                ▼
                                      ┌─────────────────┐
                                      │  前端应用       │
                                      │  (Vue3)         │
                                      │                 │
                                      │  - 监听 SSE     │
                                      │  - 显示通知     │
                                      │  - 消息管理     │
                                      └─────────────────┘
```

### 1.3 模块划分

| 模块名称 | 所在服务 | 职责说明 |
|---------|---------|---------|
| **TemplateMessageRequest** | Forgex_Common | MQ 消息请求封装，包含模板编码、占位符数据、接收人等信息 |
| **TemplateMessageSender** | Forgex_Common | MQ 消息发送器接口，提供统一的发送方法 |
| **TemplateMessageSenderImpl** | Forgex_Common | 基于 RabbitMQ 的消息发送器实现 |
| **TemplateMessageServiceImpl** | Forgex_Sys | 模板消息处理服务，负责模板查询、占位符填充、消息发送 |
| **MessageSummaryService** | Forgex_Sys | 消息汇总服务，负责用户登录时的未读消息汇总推送 |
| **SysMessageController** | Forgex_Sys | 消息管理 API 接口，提供发送、查询、已读更新等功能 |

---

## 二、快速开始

### 2.1 注入消息发送器

在业务服务的 Service 类中注入 `TemplateMessageSender`：

```java
/**
 * 审批通知服务
 */
@Service
public class ApprovalNotificationService {
    
    @Autowired
    private TemplateMessageSender templateMessageSender;
}
```

### 2.2 发送消息（三种方式）

#### 方式一：完整参数发送（推荐）

```java
/**
 * 发送审批通过通知
 * @param taskId 任务 ID
 * @param taskName 任务名称
 * @param approverName 审批人姓名
 */
public void sendApprovalNotification(Long taskId, String taskName, String approverName) {
    // 1. 准备模板数据
    Map<String, Object> dataMap = new HashMap<>();
    dataMap.put("taskName", taskName);
    dataMap.put("approverName", approverName);
    dataMap.put("approvalTime", LocalDateTime.now());
    
    // 2. 动态计算接收人（例如：流程发起人）
    List<Long> receiverIds = workflowService.getTaskCreatorIds(taskId);
    
    // 3. 构建请求对象
    TemplateMessageRequest request = new TemplateMessageRequest();
    request.setTemplateCode("WF_APPROVED");
    request.setDataMap(dataMap);
    request.setReceiverUserIds(receiverIds);
    request.setBizType("APPROVAL");
    request.setPriority(1); // 设置优先级
    
    // 4. 发送到 MQ
    templateMessageSender.sendToMq(request);
}
```

#### 方式二：简化参数发送

```java
/**
 * 发送系统公告（接收人从模板配置读取）
 * @param noticeTitle 公告标题
 * @param noticeContent 公告内容
 */
public void sendSystemNotice(String noticeTitle, String noticeContent) {
    Map<String, Object> dataMap = Map.of(
        "noticeTitle", noticeTitle,
        "noticeContent", noticeContent,
        "publishTime", LocalDateTime.now()
    );
    
    // 接收人从模板配置读取（如 ROLE 类型的所有管理员）
    templateMessageSender.sendToMq("SYSTEM_NOTICE", dataMap);
}
```

#### 方式三：批量发送

```java
/**
 * 批量发送审批待办通知
 * @param tasks 待办任务列表
 */
public void batchSendPendingNotifications(List<Task> tasks) {
    for (Task task : tasks) {
        Map<String, Object> dataMap = Map.of(
            "taskName", task.getName(),
            "createTime", task.getCreateTime(),
            "priority", task.getPriority()
        );
        
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode("WF_PENDING");
        request.setDataMap(dataMap);
        request.setReceiverUserIds(List.of(task.getAssigneeId()));
        request.setBizType("APPROVAL");
        
        templateMessageSender.sendToMq(request);
    }
}
```

---

## 三、模板配置详解

### 3.1 数据库表结构

#### 3.1.1 sys_message_template（消息模板主表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 ID |
| tenant_id | BIGINT | 租户 ID（0 为公共模板） |
| template_code | VARCHAR(64) | 模板编码（唯一标识） |
| template_name | VARCHAR(128) | 模板名称 |
| template_name_i18n_json | JSON | 国际化名称 |
| message_type | VARCHAR(32) | 消息类型（NOTICE/EMAIL/SMS 等） |
| notification_type | VARCHAR(32) | 通知图标类型（error/info/warning/success/custom） |
| config_level | VARCHAR(32) | 配置级别（PUBLIC/TENANT/TENANT_TYPE） |
| tenant_type | VARCHAR(32) | 适用租户类型（MAIN_TENANT/SUB_TENANT/PUBLIC） |
| category | VARCHAR(64) | 模板分类（APPROVAL/SYSTEM/WELCOME/SUMMARY） |
| status | INT | 状态（0-禁用，1-启用） |
| remark | VARCHAR(500) | 备注说明 |

#### 3.1.2 sys_message_template_content（模板内容表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 ID |
| template_id | BIGINT | 模板 ID（外键） |
| platform | VARCHAR(32) | 平台类型（INTERNAL/WEB/APP 等） |
| content_title | VARCHAR(256) | 消息标题（支持占位符） |
| content_title_i18n_json | JSON | 国际化标题 |
| content_body | TEXT | 消息正文（支持占位符） |
| content_body_i18n_json | JSON | 国际化正文 |
| link_url | VARCHAR(512) | 跳转链接 |

#### 3.1.3 sys_message_template_receiver（模板接收人表）

| 字段名 | 类型 | 说明 |
|-------|------|------|
| id | BIGINT | 主键 ID |
| template_id | BIGINT | 模板 ID（外键） |
| receiver_type | VARCHAR(32) | 接收人类型（ROLE/DEPT/POSITION/USER/CUSTOM） |
| receiver_ids | JSON | 接收人 ID 列表 |

### 3.2 配置级别（三级）

| 配置级别 | config_level | 说明 | 适用场景 | 示例 |
|---------|-------------|------|---------|------|
| **公共级** | PUBLIC | 所有租户共享的模板 | 系统级通知、公共消息 | 登录通知、系统公告 |
| **租户级** | TENANT | 特定租户独有的模板 | 租户定制化消息 | 企业定制审批通知 |
| **租户类型级** | TENANT_TYPE | 某类租户共享的模板 | 同类型租户通用消息 | 主租户/子租户特定通知 |

**配置优先级**：租户级 > 租户类型级 > 公共级

### 3.3 通知图标类型（5 种）

| 图标类型 | notification_type | 颜色 | 使用场景 | 前端表现 |
|---------|------------------|------|---------|---------|
| **错误** | error | 红色 | 系统错误、失败通知 | ❌ 错误图标 |
| **信息** | info | 蓝色 | 普通通知、系统消息 | ℹ️ 信息图标 |
| **警告** | warning | 黄色 | 预警提示、待处理 | ⚠️ 警告图标 |
| **成功** | success | 绿色 | 操作成功、完成通知 | ✅ 成功图标 |
| **自定义** | custom | 默认蓝色 | 特殊业务场景 | ℹ️ 信息图标（可自定义） |

### 3.4 接收人类型（5 种）

| 接收人类型 | receiver_type | 说明 | receiver_ids 格式 | 适用场景 |
|-----------|--------------|------|------------------|---------|
| **角色** | ROLE | 按角色发送 | `["1", "2"]`（角色 ID） | 发送给所有管理员 |
| **部门** | DEPT | 按部门发送 | `["101", "102"]`（部门 ID） | 发送给全部门员工 |
| **岗位** | POSITION | 按岗位发送 | `["5", "8"]`（岗位 ID） | 发送给特定岗位人员 |
| **用户** | USER | 指定用户 | `["123", "456"]`（用户 ID） | 发送给特定用户 |
| **自定义** | CUSTOM | 动态指定 | `[]`（空数组，由业务代码计算） | 审批人、动态计算接收人 |

---

## 四、模板配置示例

### 4.1 WF_APPROVED - 审批通过通知

**SQL 配置**：

```sql
-- 1. 创建模板
INSERT INTO sys_message_template (
    tenant_id, template_code, template_name, template_name_i18n_json,
    message_type, notification_type, config_level, tenant_type, category,
    status, remark, create_time, update_time, deleted, create_by, update_by
) VALUES (
    1, 'WF_APPROVED', '审批通过通知',
    '{"zh-CN":"审批通过通知","en-US":"Approval Passed"}',
    'NOTICE', 'success', 'TENANT', 'PUBLIC', 'APPROVAL',
    1, '工作流审批通过时发送的通知', NOW(), NOW(), 0, 'system', 'system'
);

SET @template_id = LAST_INSERT_ID();

-- 2. 配置模板内容
INSERT INTO sys_message_template_content (
    tenant_id, template_id, platform, content_title, content_title_i18n_json,
    content_body, content_body_i18n_json, link_url,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    1, @template_id, 'INTERNAL',
    '✅ 审批通过：${taskName}',
    '{"zh-CN":"✅ 审批通过：${taskName}","en-US":"✅ Approval Passed: ${taskName}"}',
    '您的${taskName}申请已通过审批，审批人：${approverName}，审批时间：${approvalTime}。',
    '{"zh-CN":"您的${taskName}申请已通过审批，审批人：${approverName}，审批时间：${approvalTime}。","en-US":"Your ${taskName} application has been approved by ${approverName} at ${approvalTime}."}',
    '/workflow/tasks/${taskId}',
    NOW(), NOW(), 0, 'system', 'system'
);

-- 3. 配置接收人（CUSTOM 类型，由业务代码动态计算）
INSERT INTO sys_message_template_receiver (
    tenant_id, template_id, receiver_type, receiver_ids,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    1, @template_id, 'CUSTOM', '[]',
    NOW(), NOW(), 0, 'system', 'system'
);
```

**Java 使用示例**：

```java
/**
 * 发送审批通过通知
 * @param taskId 任务 ID
 * @param approverId 审批人 ID
 */
public void sendApprovalPassedNotification(Long taskId, Long approverId) {
    // 1. 获取任务信息
    Task task = workflowService.getTaskById(taskId);
    SysUser approver = userService.getById(approverId);
    
    // 2. 获取任务发起人（接收人）
    List<Long> receiverIds = List.of(task.getCreatorId());
    
    // 3. 准备模板数据
    Map<String, Object> dataMap = Map.of(
        "taskName", task.getName(),
        "approverName", approver.getNickname(),
        "approvalTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
        "taskId", taskId
    );
    
    // 4. 构建请求
    TemplateMessageRequest request = new TemplateMessageRequest();
    request.setTemplateCode("WF_APPROVED");
    request.setDataMap(dataMap);
    request.setReceiverUserIds(receiverIds);
    request.setBizType("APPROVAL");
    
    // 5. 发送到 MQ
    templateMessageSender.sendToMq(request);
}
```

### 4.2 UNREAD_SUMMARY - 未读消息汇总通知

**SQL 配置**：

```sql
-- 1. 创建模板（公共级）
INSERT INTO sys_message_template (
    tenant_id, template_code, template_name, template_name_i18n_json,
    message_type, notification_type, config_level, tenant_type, category,
    status, remark, create_time, update_time, deleted, create_by, update_by
) VALUES (
    0, 'UNREAD_SUMMARY', '未读消息汇总通知',
    '{"zh-CN":"未读消息汇总通知","en-US":"Unread Message Summary"}',
    'NOTICE', 'info', 'PUBLIC', 'PUBLIC', 'SUMMARY',
    1, '用户登录时推送的未读消息汇总通知模板', NOW(), NOW(), 0, 'system', 'system'
);

SET @template_id = LAST_INSERT_ID();

-- 2. 配置模板内容
INSERT INTO sys_message_template_content (
    tenant_id, template_id, platform, content_title, content_title_i18n_json,
    content_body, content_body_i18n_json, link_url,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    0, @template_id, 'INTERNAL',
    '尊敬的${userName}，您有${unreadCount}条消息未读',
    '{"zh-CN":"尊敬的${userName}，您有${unreadCount}条消息未读","en-US":"Dear ${userName}, you have ${unreadCount} unread messages"}',
    '您有${unreadCount}条未读消息，请及时查看。',
    '{"zh-CN":"您有${unreadCount}条未读消息，请及时查看。","en-US":"You have ${unreadCount} unread messages, please check them in time."}',
    '/workspace/message/unread',
    NOW(), NOW(), 0, 'system', 'system'
);

-- 3. 配置接收人（CUSTOM 类型，由登录逻辑动态指定）
INSERT INTO sys_message_template_receiver (
    tenant_id, template_id, receiver_type, receiver_ids,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    0, @template_id, 'CUSTOM', '[]',
    NOW(), NOW(), 0, 'system', 'system'
);
```

**Java 使用示例**：

```java
/**
 * 用户登录时推送未读消息汇总
 * @param userId 用户 ID
 * @param tenantId 租户 ID
 */
@Async
public void pushLoginSummary(Long userId, Long tenantId) {
    try {
        // 推送未读消息汇总
        Long unreadCount = messageSummaryService.pushUnreadSummary(userId, tenantId);
        
        if (unreadCount > 0) {
            log.info("用户{}登录，推送未读消息汇总：{}条", userId, unreadCount);
        }
    } catch (Exception e) {
        log.error("推送未读消息汇总失败", e);
    }
}
```

---

## 五、前端集成

### 5.1 获取未读消息汇总

```typescript
/**
 * 用户登录后获取未读消息汇总
 */
import { getUnreadSummary } from '@/api/message'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

/**
 * 获取未读消息汇总
 */
const fetchUnreadSummary = async () => {
  try {
    const summary = await getUnreadSummary()
    console.log('未读消息汇总:', summary)
    
    // 更新状态
    state.unreadCount = summary.unreadCount
    state.hasUnread = summary.unreadCount > 0
    
    // 显示汇总通知
    if (summary.unreadCount > 0) {
      notification.info({
        message: '未读消息提醒',
        description: `尊敬的${summary.userName}，您有${summary.unreadCount}条消息未读`,
        duration: 5,
        onClick: () => {
          router.push('/workspace/message/unread')
        }
      })
    }
  } catch (error) {
    console.error('获取未读消息汇总失败', error)
  }
}

// 登录后调用
onMounted(() => {
  if (userStore.isLoggedIn) {
    fetchUnreadSummary()
  }
})
```

### 5.2 标记消息已读

```typescript
/**
 * 标记消息已读
 */
import { markRead, markBatchRead } from '@/api/message'

/**
 * 标记单条消息已读
 * @param id 消息 ID
 */
const handleMarkRead = async (id: number) => {
  try {
    await markRead(id)
    message.success('标记成功')
    
    // 更新列表
    updateMessageStatus(id, 1)
    
    // 减少未读数
    state.unreadCount--
  } catch (error) {
    console.error('标记已读失败', error)
    message.error('标记失败')
  }
}

/**
 * 批量标记已读
 * @param ids 消息 ID 列表
 */
const handleBatchMarkRead = async (ids: number[]) => {
  try {
    await markBatchRead(ids)
    message.success(`已标记${ids.length}条消息为已读`)
    
    // 更新列表
    ids.forEach(id => updateMessageStatus(id, 1))
    
    // 更新未读数
    state.unreadCount -= ids.length
  } catch (error) {
    console.error('批量标记失败', error)
    message.error('批量标记失败')
  }
}
```

### 5.3 监听 SSE 推送消息

```typescript
/**
 * 监听 SSE 推送的实时消息
 */
import { useSseStore } from '@/store/modules/sse'
import { notification } from 'ant-design-vue'

const sseStore = useSseStore()

/**
 * 图标类型映射
 */
const iconMap: Record<string, 'success' | 'info' | 'warning' | 'error'> = {
  error: 'error',
  info: 'info',
  warning: 'warning',
  success: 'success',
  custom: 'info'
}

/**
 * 初始化消息监听
 */
const initMessageListener = () => {
  // 订阅 message 事件
  sseStore.subscribe('message', (message: any) => {
    showMessageNotification(message)
  })
}

/**
 * 显示消息通知
 * @param message 消息对象
 */
const showMessageNotification = (message: any) => {
  const { messageType, title, content, linkUrl } = message
  
  // 根据消息类型映射图标
  const icon = iconMap[messageType] || 'info'
  
  // 显示通知
  notification[icon]({
    message: title,
    description: content,
    duration: 4.5,
    placement: 'topRight',
    onClick: () => {
      // 点击通知跳转到消息链接
      if (linkUrl) {
        router.push(linkUrl)
      }
    }
  })
  
  // 更新未读数
  state.unreadCount++
}

// 组件挂载时初始化
onMounted(() => {
  initMessageListener()
})
```

---

## 六、API 接口文档

### 6.1 消息管理接口

**服务地址**：`Forgex_Sys`（端口 8082）

#### 6.1.1 获取未读消息汇总

```http
POST /api/sys/message/unread-summary
Content-Type: application/json
Authorization: {token}

Response:
{
  "code": 200,
  "data": {
    "unreadCount": 5,
    "userName": "张三",
    "summary": "尊敬的张三，您有 5 条消息未读"
  }
}
```

#### 6.1.2 标记消息已读

```http
POST /api/sys/message/read
Content-Type: application/json
Authorization: {token}

Request:
{
  "id": 123
}

Response:
{
  "code": 200,
  "data": true
}
```

#### 6.1.3 批量标记已读

```http
POST /api/sys/message/read-batch
Content-Type: application/json
Authorization: {token}

Request:
{
  "ids": [123, 456, 789]
}

Response:
{
  "code": 200,
  "data": true
}
```

#### 6.1.4 获取未读消息列表

```http
GET /api/sys/message/unread?limit=50
Authorization: {token}

Response:
{
  "code": 200,
  "data": [
    {
      "id": 123,
      "title": "审批通过：请假申请",
      "content": "您的请假申请已通过审批...",
      "messageType": "NOTICE",
      "status": 0,
      "createTime": "2026-04-08 14:30:00"
    }
  ]
}
```

---

## 七、最佳实践

### 7.1 消息发送最佳实践

```java
/**
 * 最佳实践示例
 */
@Service
public class BestPracticeMessageService {
    
    @Autowired
    private TemplateMessageSender templateMessageSender;
    
    /**
     * ✅ 推荐：使用异步发送
     */
    @Async
    public void sendAsyncNotification(Long userId, String message) {
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode("SYSTEM_NOTICE");
        request.setDataMap(Map.of("message", message));
        request.setReceiverUserIds(List.of(userId));
        
        templateMessageSender.sendToMq(request);
    }
    
    /**
     * ✅ 推荐：添加异常处理
     */
    public void sendWithExceptionHandling(Long userId, String templateCode) {
        try {
            templateMessageSender.sendToMq(templateCode, Map.of());
        } catch (Exception e) {
            log.error("发送消息失败：userId={}, templateCode={}", userId, templateCode, e);
            // 记录日志，不影响主流程
        }
    }
    
    /**
     * ✅ 推荐：使用业务类型分类
     */
    public void sendWithBizType(Long userId, String templateCode) {
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode(templateCode);
        request.setBizType("APPROVAL"); // 添加业务类型
        request.setReceiverUserIds(List.of(userId));
        
        templateMessageSender.sendToMq(request);
    }
    
    /**
     * ❌ 避免：同步阻塞发送
     */
    public void sendSyncNotification(Long userId) {
        // 不要在主流程中同步发送消息
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode("NOTICE");
        request.setReceiverUserIds(List.of(userId));
        
        templateMessageSender.sendToMq(request); // 阻塞主流程
    }
    
    /**
     * ❌ 避免：缺少异常处理
     */
    public void sendWithoutHandling(Long userId) {
        TemplateMessageRequest request = new TemplateMessageRequest();
        request.setTemplateCode("NOTICE");
        request.setReceiverUserIds(List.of(userId));
        
        templateMessageSender.sendToMq(request); // 失败会影响主流程
    }
}
```

### 7.2 模板设计原则

1. **模板复用性**
   - 使用占位符（如 `${userName}`）代替具体值
   - 避免在模板中硬编码业务数据
   - 同一类消息使用同一模板

2. **国际化支持**
   - 所有文本内容配置国际化 JSON
   - 至少支持中文（zh-CN）和英文（en-US）
   - 使用 `template_name_i18n_json`、`content_title_i18n_json`、`content_body_i18n_json`

3. **通知类型选择**
   - 成功操作使用 `success`（绿色）
   - 错误失败使用 `error`（红色）
   - 待办预警使用 `warning`（黄色）
   - 普通通知使用 `info`（蓝色）

4. **接收人配置**
   - 固定接收人使用配置类型（ROLE/DEPT/POSITION/USER）
   - 动态接收人使用 CUSTOM 类型
   - 避免在模板中硬编码用户 ID

---

## 八、常见问题

### 8.1 消息发送失败

**问题**：调用 `sendToMq` 后消息未发送

**可能原因**：
1. MQ 服务未启动或配置错误
2. 模板编码不存在
3. 模板未启用（status=0）

**解决方案**：

```java
// 1. 检查 MQ 配置
@Value("${mq.template.queue.name}")
private String queueName;

// 2. 验证模板是否存在
SysMessageTemplate template = templateMapper.selectByCode("WF_APPROVED");
if (template == null) {
    throw new BusinessException("模板不存在：WF_APPROVED");
}

// 3. 检查模板状态
if (template.getStatus() == 0) {
    throw new BusinessException("模板已禁用：WF_APPROVED");
}

// 4. 添加日志
log.info("发送模板消息：templateCode={}, dataMap={}", request.getTemplateCode(), request.getDataMap());
```

### 8.2 占位符未替换

**问题**：消息内容显示 `${userName}` 而非实际值

**可能原因**：
1. `dataMap` 的 Key 与占位符名称不匹配
2. 占位符格式错误（应使用 `${key}`）

**解决方案**：

```java
// ❌ 错误示例
Map<String, Object> dataMap = Map.of(
    "username", "张三"  // 小写，模板中是 userName
);

// ✅ 正确示例
Map<String, Object> dataMap = Map.of(
    "userName", "张三"  // 与模板占位符一致
);

// 检查模板内容
SELECT content_body FROM sys_message_template_content 
WHERE template_id = 1;
-- 应该返回：尊敬的${userName}，您好！
```

### 8.3 接收人未收到消息

**问题**：消息发送成功但接收人未收到

**可能原因**：
1. 接收人类型配置错误
2. `receiver_ids` 格式错误
3. 用户 ID 不存在或被禁用

**解决方案**：

```sql
-- 1. 检查接收人配置
SELECT receiver_type, receiver_ids 
FROM sys_message_template_receiver 
WHERE template_id = 1;

-- 2. 验证 receiver_ids 格式（应为 JSON 数组）
-- ✅ 正确：["123", "456"]
-- ❌ 错误：123,456 或 [123,456]

-- 3. 检查用户状态
SELECT id, status, deleted 
FROM sys_user 
WHERE id IN (123, 456);
-- status 应为 1，deleted 应为 0
```

### 8.4 前端未显示通知

**问题**：后端发送成功但前端未显示通知

**可能原因**：
1. SSE 连接未建立
2. 消息类型不匹配
3. 通知组件未注册

**解决方案**：

```typescript
// 1. 检查 SSE 连接状态
const sseStore = useSseStore()
console.log('SSE 连接状态:', sseStore.isConnected)

// 2. 验证消息监听
sseStore.subscribe('message', (message: any) => {
  console.log('收到消息:', message) // 添加日志
  showNotification(message)
})

// 3. 确认组件注册
// 在 main.ts 中检查：
app.component('MessageNotification', MessageNotification)

// 4. 检查通知类型映射
const iconMap: Record<string, 'success' | 'info' | 'warning' | 'error'> = {
  error: 'error',
  info: 'info',
  warning: 'warning',
  success: 'success',
  custom: 'info' // 自定义类型映射为 info
}
```

---

## 九、扩展指南

### 9.1 如何添加新模板

#### 步骤一：设计模板内容

- 确定模板用途（审批、系统通知、欢迎消息等）
- 设计占位符变量（如 `${userName}`、`${taskName}`）
- 准备多语言版本（zh-CN、en-US 等）

#### 步骤二：数据库配置

```sql
-- 创建消息模板
INSERT INTO sys_message_template (
    tenant_id, template_code, template_name, template_name_i18n_json,
    message_type, notification_type, config_level, tenant_type, category,
    status, remark, create_time, update_time, deleted, create_by, update_by
) VALUES (
    #{tenantId}, #{templateCode}, #{templateName}, #{i18nNameJson},
    'NOTICE', 'info', 'TENANT', 'PUBLIC', 'APPROVAL',
    1, #{remark}, NOW(), NOW(), 0, 'system', 'system'
);

-- 配置模板内容
INSERT INTO sys_message_template_content (
    tenant_id, template_id, platform, content_title, content_title_i18n_json,
    content_body, content_body_i18n_json, link_url,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    #{tenantId}, LAST_INSERT_ID(), 'INTERNAL',
    #{title}, #{i18nTitleJson},
    #{content}, #{i18nContentJson},
    #{linkUrl},
    NOW(), NOW(), 0, 'system', 'system'
);

-- 配置接收人
INSERT INTO sys_message_template_receiver (
    tenant_id, template_id, receiver_type, receiver_ids,
    create_time, update_time, deleted, create_by, update_by
) VALUES (
    #{tenantId}, LAST_INSERT_ID(), #{receiverType}, #{receiverIdsJson},
    NOW(), NOW(), 0, 'system', 'system'
);
```

#### 步骤三：业务代码调用

```java
// 构建 TemplateMessageRequest
TemplateMessageRequest request = new TemplateMessageRequest();
request.setTemplateCode("NEW_TEMPLATE_CODE");
request.setDataMap(Map.of("key", "value"));
request.setReceiverUserIds(List.of(123L));

// 调用发送
templateMessageSender.sendToMq(request);
```

### 9.2 常用占位符列表

| 占位符 | 说明 | 示例值 |
|-------|------|-------|
| `${userName}` | 用户名 | 张三 |
| `${taskName}` | 任务名称 | 请假申请 |
| `${approverName}` | 审批人姓名 | 李四 |
| `${approvalTime}` | 审批时间 | 2026-04-08 14:30:00 |
| `${unreadCount}` | 未读消息数 | 5 |
| `${createTime}` | 创建时间 | 2026-04-08 10:00:00 |
| `${deptName}` | 部门名称 | 技术部 |
| `${roleName}` | 角色名称 | 管理员 |

---

## 十、相关文档

- [Forgex 项目架构规范](../project-structure-and-development-guide.mdc)
- [Forgex 后端实现指南](forgex-backend-implementation/SKILL.md)
- [Forgex 前端开发指南](forgex-frontend/SKILL.md)
- [工作流模块使用指南](workflow-module-usage/SKILL.md)

---

**文档版本**: 1.0  
**创建日期**: 2026-04-08  
**作者**: Forgex Team  
**最后更新**: 2026-04-08
