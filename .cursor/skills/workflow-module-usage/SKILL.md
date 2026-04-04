---
name: workflow-module-usage
description: Forgex 工作流模块使用指南，包括审批任务配置、发起审批、审批处理、回调通知、消息推送等。当开发审批功能、集成工作流引擎、配置审批流程或实现消息通知时自动应用此技能。
---

# 工作流模块使用技能

## 模块概述

Forgex 工作流模块是一个自研的审批工作流引擎，支持灵活的审批任务配置和流转。

### 核心功能

- **审批任务配置**：创建审批任务，配置任务名称、编码、解释器、表单
- **审批节点配置**：配置审批节点，定义节点类型、层级、前后关系
- **审批人配置**：为审核节点配置审批人（单人/部门/角色/职位）
- **分支条件配置**：为分支节点配置条件表达式和跳转目标
- **发起审批**：用户发起审批任务，填写表单内容
- **审批处理**：审批人处理审批任务（同意/不同意）
- **驳回处理**：支持驳回任务或返回上一节点
- **审批解释器**：任务流转时触发解释器，业务可自定义处理
- **消息通知**：在审批关键节点自动发送消息通知

### 审批类型

| 审批类型 | 编码 | 说明 |
|----------|------|------|
| 会签 | COUNTERSIGN | 所有审批人都同意才通过 |
| 或签 | OR_SIGN | 任一审批人同意即通过 |
| 抄送 | COPY | 仅抄送，不需要审批 |
| 会签投票 | VOTE | 超过半数同意即通过 |
| 逐个审批 | SEQUENTIAL | 按顺序逐个审批 |

### 服务信息

- **服务名称**: forgex-workflow
- **服务端口**: 9005
- **基础路径**: `/wf`

---

## 快速开始

### 创建第一个审批任务

```bash
# 创建请假审批任务
POST http://localhost:9005/wf/task/config/create
Content-Type: application/json

{
  "taskName": "请假审批",
  "taskCode": "LEAVE_APPROVAL",
  "interpreterBean": "leaveApprovalInterpreter",
  "formType": 1,
  "formPath": "/workflow/form/leave",
  "status": 1,
  "remark": "员工请假审批流程"
}
```

### 发起审批

```bash
# 发起请假审批
POST http://localhost:9005/wf/execution/start
Content-Type: application/json

{
  "taskCode": "LEAVE_APPROVAL",
  "formContent": "{\"leaveType\":1,\"startDate\":\"2026-04-10\",\"endDate\":\"2026-04-12\",\"reason\":\"请假事由\"}"
}

# 响应
{
  "code": 200,
  "message": "创建成功",
  "data": 10001  // 执行 ID
}
```

### 查询审批结果

```bash
# 查询审批详情
POST http://localhost:9005/wf/execution/detail
Content-Type: application/json

{
  "executionId": 10001
}
```

---

## 其他服务接入审批

### 方式一：通过审批解释器（推荐）

审批解释器允许业务系统在审批流转的关键节点执行自定义逻辑。

#### 步骤 1：实现 IApprovalInterpreter 接口

```java
package com.example.leave.service;

import com.forgex.workflow.service.interpreter.ApprovalContext;
import com.forgex.workflow.service.interpreter.IApprovalInterpreter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 请假审批解释器
 */
@Slf4j
@Component("leaveApprovalInterpreter")
public class LeaveApprovalInterpreter implements IApprovalInterpreter {
    
    @Autowired
    private LeaveService leaveService;
    
    @Override
    public void onStart(ApprovalContext context) {
        // 审批开始时触发
        log.info("请假审批开始：executionId={}, 发起人={}", 
                context.getExecutionId(), context.getInitiatorName());
        
        // TODO: 可以在此处发送审批开始通知
    }
    
    @Override
    public void onApprove(ApprovalContext context) {
        // 每个节点审批后触发
        if (context.getApproveStatus() == 1) {
            log.info("审批通过：审批人={}", context.getApproverName());
        } else {
            log.info("审批驳回：审批人={}, 原因={}", 
                    context.getApproverName(), context.getComment());
        }
    }
    
    @Override
    public void onReject(ApprovalContext context) {
        // 审批驳回时触发
        log.info("审批驳回：executionId={}, 原因={}", 
                context.getExecutionId(), context.getComment());
    }
    
    @Override
    public void onEnd(ApprovalContext context) {
        // 审批结束时触发
        if (context.getStatus() == 2) {
            // 审批通过，更新请假数据
            log.info("审批完成，更新请假数据");
            // leaveService.updateStatus(leaveId, LeaveStatus.APPROVED);
        } else if (context.getStatus() == 3) {
            // 审批被驳回
            log.info("审批被驳回");
        }
    }
}
```

#### 步骤 2：创建审批任务时指定解释器

```java
WfTaskConfigSaveParam param = new WfTaskConfigSaveParam();
param.setTaskName("请假审批");
param.setTaskCode("LEAVE_APPROVAL");
param.setInterpreterBean("leaveApprovalInterpreter"); // 指定解释器 Bean 名称
param.setFormType(1);
param.setFormPath("/workflow/form/leave");

// 调用工作流服务创建任务
R<Long> result = workflowFeign.createTaskConfig(param);
```

### 方式二：通过回调 URL

如果业务系统不想实现解释器，可以配置回调 URL。

#### 步骤 1：在业务系统中提供回调接口

```java
package com.example.leave.controller;

import com.example.leave.dto.ApprovalCallbackDTO;
import com.forgex.common.web.R;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 工作流回调接口
 */
@Slf4j
@RestController
@RequestMapping("/callback/workflow")
public class WorkflowCallbackController {
    
    /**
     * 审批结果回调
     */
    @PostMapping("/approval/result")
    public R<Void> approvalResult(@RequestBody ApprovalCallbackDTO dto) {
        log.info("收到审批回调：executionId={}, taskCode={}, status={}", 
                dto.getExecutionId(), dto.getTaskCode(), dto.getStatus());
        
        if (dto.getStatus() == 2) {
            // 审批通过，处理业务逻辑
            // handleApprovalPass(dto);
        } else if (dto.getStatus() == 3) {
            // 审批被驳回
            // handleApprovalReject(dto);
        }
        
        return R.ok();
    }
    
    /**
     * 回调数据
     */
    @Data
    public static class ApprovalCallbackDTO {
        /** 执行 ID */
        private Long executionId;
        /** 任务编码 */
        private String taskCode;
        /** 任务名称 */
        private String taskName;
        /** 最终状态：2=审批完成，3=驳回 */
        private Integer status;
        /** 表单内容（JSON） */
        private String formContent;
        /** 发起人 ID */
        private Long initiatorId;
        /** 发起时间 */
        private java.time.LocalDateTime startTime;
        /** 结束时间 */
        private java.time.LocalDateTime endTime;
    }
}
```

#### 步骤 2：注册回调

```bash
# 注册回调
POST http://localhost:9005/wf/callback/register
Content-Type: application/json

{
  "taskCode": "LEAVE_APPROVAL",
  "callbackUrl": "http://leave-service/callback/workflow/approval/result",
  "callbackBean": null  // 与 callbackUrl 二选一
}
```

### 方式三：查询审批结果

业务系统可以主动查询审批结果。

```java
// 查询审批详情
@PostMapping("/wf/execution/detail")
R<WfExecutionDTO> getExecutionDetail(@RequestBody Map<String, Object> params);

// 查询我发起的审批
@PostMapping("/wf/execution/my/initiated")
R<Page<WfExecutionDTO>> pageMyInitiated(@RequestBody WfExecutionQueryParam param);

// 查询我待处理的审批
@PostMapping("/wf/execution/my/pending")
R<Page<WfExecutionDTO>> pageMyPending(@RequestBody WfExecutionQueryParam param);

// 查询我已处理的审批
@PostMapping("/wf/execution/my/processed")
R<Page<WfExecutionDTO>> pageMyProcessed(@RequestBody WfExecutionQueryParam param);
```

---

## 消息通知配置

### 消息类型

| 消息类型 | 模板编码 | 触发时机 | 默认接收人 |
|----------|----------|----------|------------|
| 审批开始通知 | WF_APPROVAL_START | 审批任务发起时 | 审批人 |
| 审批通过通知 | WF_APPROVAL_PASS | 审批节点通过时 | 发起人 |
| 审批驳回通知 | WF_APPROVAL_REJECT | 审批被驳回时 | 发起人 |
| 审批完成通知 | WF_APPROVAL_FINISH | 审批流程结束时 | 发起人 |

### 支持的消息平台

- **INTERNAL**：站内消息（通过 SSE 实时推送）
- **EMAIL**：邮件通知
- **SMS**：短信通知
- **WECHAT**：企业微信（待实现）

### 配置消息模板

```java
WfTaskConfigSaveParam param = new WfTaskConfigSaveParam();
param.setTaskName("请假审批");
param.setTaskCode("LEAVE_APPROVAL");

// 配置消息模板
param.setStartMessageTemplateCode("WF_APPROVAL_START");
param.setApproveMessageTemplateCode("WF_APPROVAL_PASS");
param.setRejectMessageTemplateCode("WF_APPROVAL_REJECT");
param.setFinishMessageTemplateCode("WF_APPROVAL_FINISH");

// 配置消息跳转链接基础 URL
param.setLinkBaseUrl("http://localhost:5173/workflow/approval/detail/");

taskConfigService.create(param);
```

### 消息占位符

#### 通用占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `${executionId}` | 审批执行 ID | 12345 |
| `${taskName}` | 任务名称 | 请假审批 |
| `${taskCode}` | 任务编码 | LEAVE_APPROVAL |
| `${linkUrl}` | 跳转链接 | http://localhost:5173/workflow/approval/detail/12345 |
| `${formContent}` | 表单内容（JSON） | {"leaveType":1,...} |

#### 审批开始消息占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `${initiatorName}` | 发起人名称 | 张三 |
| `${initiatorId}` | 发起人 ID | 1001 |
| `${startTime}` | 发起时间 | 2026-04-10 10:00:00 |
| `${currentApprovers}` | 当前审批人 ID 列表 | [1002,1003] |

#### 审批通过消息占位符

| 占位符 | 说明 | 示例 |
|--------|------|------|
| `${initiatorName}` | 发起人名称 | 张三 |
| `${approverName}` | 审批人名称 | 李四 |
| `${approverId}` | 审批人 ID | 1002 |
| `${comment}` | 审批意见 | 同意 |
| `${approveTime}` | 审批时间 | 2026-04-10 11:00:00 |

### 消息通知流程

```
1. 审批流程触发（开始/通过/驳回/完成）
   ↓
2. 审批解释器（ApprovalMessageNotifier）被调用
   ↓
3. 读取任务配置中的消息模板编码
   ↓
4. 构建消息数据（填充占位符）
   ↓
5. 调用 MessageSenderService 发送消息
   ↓
6. MessageSenderService 根据模板配置：
   - 查询消息模板内容
   - 解析接收人列表
   - 替换占位符
   - 根据平台发送消息（站内/邮件/短信）
```

---

## 审批人配置

### 审批人类型

| 类型 | 编码 | 说明 |
|------|------|------|
| 单人 | 1 | 指定具体用户 |
| 部门 | 2 | 部门内所有用户 |
| 角色 | 3 | 角色下所有用户 |
| 职位 | 4 | 职位下所有用户 |

### 配置示例

```json
{
  "nodeConfigId": 1,
  "approvers": [
    {
      "approverType": 1,
      "approverIds": [10001, 10002]  // 指定用户
    },
    {
      "approverType": 3,
      "approverIds": [1]  // 部门经理角色
    }
  ]
}
```

### 动态审批人

```java
@Override
public void onStart(ApprovalContext context) {
    // 根据请假天数确定审批人
    Leave leave = JSON.parseObject(context.getFormContent(), Leave.class);
    if (leave.getDays() > 7) {
        // 超过 7 天，需要总经理审批
        workflowService.addApprover(context.getExecutionId(), "GENERAL_MANAGER");
    }
}
```

---

## 分支条件配置

### 分支条件 JSON 结构

```json
{
  "conditions": [
    {
      "field": "amount",
      "fieldName": "金额",
      "operator": ">=",
      "value": "10000",
      "nextNodeId": 123,
      "nextNodeName": "总经理审批"
    },
    {
      "field": "amount",
      "fieldName": "金额",
      "operator": "<",
      "value": "10000",
      "nextNodeId": 124,
      "nextNodeName": "部门经理审批"
    }
  ],
  "defaultNodeId": 125,
  "defaultNodeName": "默认审批"
}
```

### 支持的操作符

| 操作符 | 说明 |
|--------|------|
| = | 等于 |
| != | 不等于 |
| > | 大于 |
| >= | 大于等于 |
| < | 小于 |
| <= | 小于等于 |
| in | 包含在 |
| not in | 不包含在 |
| like | 包含 |

### 配置示例

```bash
# 配置报销审批的分支条件
POST http://localhost:9005/wf/node/config/save
Content-Type: application/json

{
  "taskConfigId": 1,
  "nodeType": 5,  // 分支节点
  "nodeName": "金额判断",
  "approveType": null,
  "branchConditions": "{\"conditions\":[{\"field\":\"amount\",\"operator\":\">=\",\"value\":\"10000\",\"nextNodeId\":2},{\"field\":\"amount\",\"operator\":\"<\",\"value\":\"10000\",\"nextNodeId\":3}],\"defaultNodeId\":4}"
}
```

---

## API 接口清单

### 审批任务配置

| 接口 | 方法 | 说明 |
|------|------|------|
| `/wf/task/config/page` | POST | 分页查询审批任务配置 |
| `/wf/task/config/list` | POST | 列表查询审批任务配置 |
| `/wf/task/config/get` | POST | 获取审批任务详情 |
| `/wf/task/config/getByCode` | POST | 根据任务编码获取配置 |
| `/wf/task/config/create` | POST | 创建审批任务配置 |
| `/wf/task/config/update` | POST | 更新审批任务配置 |
| `/wf/task/config/delete` | POST | 删除审批任务配置 |
| `/wf/task/config/updateStatus` | POST | 更新审批任务状态 |

### 审批执行

| 接口 | 方法 | 说明 |
|------|------|------|
| `/wf/execution/start` | POST | 发起审批 |
| `/wf/execution/approve` | POST | 审批同意 |
| `/wf/execution/reject` | POST | 审批驳回 |
| `/wf/execution/cancel` | POST | 撤销审批 |
| `/wf/execution/detail` | POST | 获取审批详情 |
| `/wf/execution/my/initiated` | POST | 我发起的审批 |
| `/wf/execution/my/pending` | POST | 我待处理的审批 |
| `/wf/execution/my/processed` | POST | 我已处理的审批 |

### 回调管理

| 接口 | 方法 | 说明 |
|------|------|------|
| `/wf/callback/register` | POST | 注册回调 |
| `/wf/callback/unregister` | POST | 注销回调 |

---

## 常见问题

### 如何配置会签？

```json
{
  "nodeConfigId": 1,
  "approveType": 1,  // 1=会签
  "approvers": [
    {
      "approverType": 1,
      "approverIds": [10001, 10002, 10003]
    }
  ]
}
```

会签需要所有审批人都同意才会流转到下一节点。

### 如何配置或签？

```json
{
  "nodeConfigId": 1,
  "approveType": 2,  // 2=或签
  "approvers": [
    {
      "approverType": 1,
      "approverIds": [10001, 10002, 10003]
    }
  ]
}
```

或签只要有一个审批人同意就会流转到下一节点。

### 如何实现动态分支？

```java
@Override
public void onStart(ApprovalContext context) {
    Order order = JSON.parseObject(context.getFormContent(), Order.class);
    
    if (order.getAmount() > 100000) {
        // 金额大于 10 万，需要 CEO 审批
        workflowService.setNextNode(context.getExecutionId(), ceoNodeId);
    } else if (order.getAmount() > 50000) {
        // 金额大于 5 万，需要 CFO 审批
        workflowService.setNextNode(context.getExecutionId(), cfoNodeId);
    }
}
```

### 消息没有发送怎么办？

**解决方案**：

1. 检查任务配置是否设置了消息模板编码
2. 检查消息模板是否启用（status=1）
3. 检查消息模板是否配置了接收人
4. 查看日志，确认是否有错误信息

### 消息跳转链接错误怎么办？

**解决方案**：

1. 检查 `link_base_url` 配置是否正确
2. 确保前端路由配置了审批详情页面
3. 确认路由参数名称与前端解析一致

---

## 前端接入示例

### 审批详情页面

```vue
<template>
  <div class="approval-detail">
    <h2>审批详情</h2>
    <a-descriptions title="基本信息">
      <a-descriptions-item label="任务名称">{{ detail.taskName }}</a-descriptions-item>
      <a-descriptions-item label="发起人">{{ detail.initiatorName }}</a-descriptions-item>
      <a-descriptions-item label="当前状态">{{ statusText }}</a-descriptions-item>
    </a-descriptions>
    
    <!-- 表单内容 -->
    <component :is="formComponent" :data="detail.formContent" />
    
    <!-- 审批操作 -->
    <div v-if="canApprove">
      <a-button type="primary" @click="handleApprove">同意</a-button>
      <a-button danger @click="handleReject">驳回</a-button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import { getExecutionDetail } from '@/api/workflow/execution';

const route = useRoute();
const executionId = route.params.id;
const detail = ref({});

onMounted(async () => {
  const result = await getExecutionDetail(executionId);
  detail.value = result.data;
});

const statusText = computed(() => {
  const statusMap = {
    0: '未审批',
    1: '审批中',
    2: '审批完成',
    3: '驳回'
  };
  return statusMap[detail.value.status] || '未知';
});

const canApprove = computed(() => {
  // TODO: 判断当前用户是否是审批人
  return detail.value.status === 1;
});
</script>
```

### 路由配置

```typescript
// router/index.ts
{
  path: '/workflow/approval/detail/:id',
  name: 'WorkflowApprovalDetail',
  component: () => import('@/views/workflow/approval/Detail.vue')
}
```

---

**文档版本**: 1.0  
**创建日期**: 2026-04-01  
**作者**: Forgex Team
