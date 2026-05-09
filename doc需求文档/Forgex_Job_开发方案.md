# Forgex_Job 任务调度系统开发方案

> **文档版本**: V1.0  
> **创建日期**: 2026-05-09  
> **作者**: ForGexTeam  
> **定位**: Forgex 自研轻量任务调度服务，不使用 Snail-Job / XXL-Job / Quartz  
> **目标**: 先实现 P0 可上线闭环，再分阶段扩展重试、告警、制造业模板和 DAG

---

## 一、实际建议

### 1.1 结论

建议放弃 Snail-Job 接入，删除现有 Snail-Job 依赖，并基于 Forgex 当前技术栈实现轻量调度平台。

理由：

1. 用户明确不希望使用 Snail-Job，以规避潜在版权风险。
2. `Forgex_Job` 已经是独立服务，已有端口、数据源、Nacos、日志和部署清单，不需要引入外部调度中心。
3. 当前首要痛点是“任务统一管理、日志、重试、迁移和运维可见”，不是复刻完整调度框架。
4. Spring Boot 已具备基础 Cron 表达式计算能力，Redisson 可支撑多实例互斥，MySQL 可作为调度事实源。

### 1.2 范围收敛

| 阶段 | 重点 | 不做 |
|---|---|---|
| P0 | Java Bean 任务、Cron/固定间隔、手动触发、日志、实例、权限、前端基础页 | DAG、脚本任务、复杂路由、广播、分片 |
| P1 | 重试/死信、告警、HTTP 任务、低风险存量任务迁移、制造业模板 | 任意命令执行 |
| P2 | DAG、脚本白名单、广播/分片、维护模式 | 审批工作流替代 |

---

## 二、技术选型

| 能力 | 方案 |
|---|---|
| 调度扫描 | `ScheduledExecutorService` 或 Spring `TaskScheduler` 固定周期扫描数据库 |
| Cron 计算 | Spring `org.springframework.scheduling.support.CronExpression` |
| 固定间隔 | 数据库保存 `interval_seconds`，扫描器按 `next_trigger_time` 触发 |
| 集群互斥 | Redisson 分布式锁，Key：`forgex:job:lock:{tenantId}:{jobId}:{fireTime}` |
| 执行隔离 | `ThreadPoolTaskExecutor` / `ThreadPoolExecutor`，独立 job 执行线程池 |
| 任务调用 | Spring Bean 名称 + 方法名，参数使用 JSON |
| 任务日志 | `sys_job_log` 落库，状态机记录完整执行过程 |
| 重试 | `sys_job_retry` + 扫描器二次调度 |
| 实例心跳 | `sys_job_instance` + Redis TTL 可选缓存 |
| 监控 | MyBatis-Plus 聚合查询 + ECharts |
| 实时推送 | 复用 Forgex SSE |
| 事件触发 | RocketMQ 或内部 REST API，P1 实现 |
| 权限/租户 | Sa-Token 权限注解 + Forgex 租户上下文 |

不引入：

- Snail-Job
- XXL-Job
- Quartz
- 其他第三方调度中心

---

## 三、依赖调整

### 3.1 后端父 POM

从 `Forgex_MOM/Forgex_Backend/pom.xml` 删除：

```xml
<snailjob.version>1.8.1</snailjob.version>
```

以及 dependencyManagement 中 Snail-Job 相关依赖声明。

### 3.2 Job 模块 POM

从 `Forgex_MOM/Forgex_Backend/Forgex_Job/pom.xml` 删除：

```xml
<dependency>
    <groupId>com.aizuda</groupId>
    <artifactId>snail-job-client-starter</artifactId>
</dependency>
<dependency>
    <groupId>com.aizuda</groupId>
    <artifactId>snail-job-client-retry-core</artifactId>
</dependency>
<dependency>
    <groupId>com.aizuda</groupId>
    <artifactId>snail-job-client-job-core</artifactId>
</dependency>
```

保留：

- `Forgex_Common`
- Nacos Discovery
- 数据库、Redis、Sa-Token 等通过父 POM 或公共配置继承的能力

如 `Forgex_Job` 当前无法直接使用 MyBatis-Plus、dynamic-datasource、Redisson，需要按项目现有依赖方式显式补齐，但不得引入调度框架。

---

## 四、整体架构

```text
Forgex Frontend
  ├─ /job/dashboard
  ├─ /job/task
  ├─ /job/log
  └─ /job/instance
        │ HTTP / SSE
        ▼
Forgex Gateway
        │
        ▼
Forgex_Job (9004)
  ├─ controller      管理 API
  ├─ service         任务、日志、实例、监控服务
  ├─ scheduler       周期扫描、触发抢占、重试扫描
  ├─ executor        Java Bean 执行、超时控制、上下文恢复
  ├─ lock            Redisson 分布式锁
  ├─ registry        实例注册与心跳
  ├─ alarm           P1 告警
  └─ domain/mapper   MyBatis-Plus 数据访问
        │
        ├─ MySQL forgex_job
        ├─ Redis / Redisson
        └─ RocketMQ (P1)
```

---

## 五、数据库设计

### 5.1 P0 表

#### `sys_job_task`

任务定义表。核心字段：

| 字段 | 类型建议 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `tenant_id` | bigint | 租户 ID |
| `job_code` | varchar(100) | 任务编码 |
| `job_name` | varchar(200) | 任务名称 |
| `job_group` | varchar(100) | 任务分组 |
| `job_type` | tinyint | 任务类型：1=Java Bean，2=HTTP，3=脚本 |
| `schedule_type` | tinyint | 调度类型：1=Cron，2=固定间隔，3=手动 |
| `cron_expression` | varchar(100) | Cron 表达式 |
| `interval_seconds` | int | 固定间隔秒数 |
| `bean_name` | varchar(200) | Spring Bean 名称 |
| `method_name` | varchar(100) | 方法名 |
| `job_params` | json | 默认参数 |
| `status` | tinyint | 状态：0=停用，1=启用，2=暂停 |
| `block_strategy` | tinyint | 阻塞策略 |
| `timeout_seconds` | int | 超时时间 |
| `max_retry_count` | int | 最大重试次数 |
| `retry_interval_seconds` | int | 重试间隔 |
| `next_trigger_time` | datetime | 下次触发时间 |
| `last_trigger_time` | datetime | 上次触发时间 |
| `last_status` | tinyint | 最近执行状态 |
| `trigger_count` | bigint | 累计触发次数 |
| `remark` | varchar(500) | 备注 |

索引：

- `uk_job_code_tenant`：`tenant_id, job_code, deleted`
- `idx_next_trigger`：`status, next_trigger_time`
- `idx_group_status`：`tenant_id, job_group, status`

#### `sys_job_log`

执行日志表。核心字段：

| 字段 | 类型建议 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `tenant_id` | bigint | 租户 ID |
| `job_id` | bigint | 任务 ID |
| `job_code` | varchar(100) | 任务编码冗余 |
| `trigger_type` | tinyint | 触发方式：1=调度，2=手动，3=重试，4=API |
| `fire_time` | datetime | 计划触发时间 |
| `start_time` | datetime | 开始时间 |
| `end_time` | datetime | 结束时间 |
| `duration_ms` | bigint | 耗时 |
| `status` | tinyint | 状态 |
| `instance_id` | varchar(100) | 执行实例 |
| `request_params` | json | 执行参数 |
| `result_message` | varchar(1000) | 结果摘要 |
| `error_stack` | text | 异常堆栈，写入前截断 |
| `retry_count` | int | 当前重试次数 |
| `retry_of_log_id` | bigint | 原始日志 ID |

索引：

- `idx_job_time`：`tenant_id, job_id, start_time`
- `idx_status_time`：`tenant_id, status, start_time`
- `idx_code_time`：`tenant_id, job_code, start_time`

#### `sys_job_instance`

实例表。核心字段：

| 字段 | 类型建议 | 说明 |
|---|---|---|
| `id` | bigint | 主键 |
| `instance_id` | varchar(100) | 实例唯一标识 |
| `service_name` | varchar(100) | 服务名 |
| `ip` | varchar(64) | IP |
| `port` | int | 端口 |
| `pid` | varchar(50) | 进程 ID |
| `status` | tinyint | 在线、离线、维护 |
| `running_count` | int | 执行中任务数 |
| `last_heartbeat_time` | datetime | 最近心跳 |
| `start_time` | datetime | 启动时间 |

### 5.2 P1/P2 表

| 表 | 阶段 | 说明 |
|---|---|---|
| `sys_job_retry` | P1 | 重试与死信记录 |
| `sys_job_alarm_rule` | P1 | 告警规则 |
| `sys_job_alarm_log` | P1 | 告警日志 |
| `sys_job_workflow` | P2 | DAG 定义 |
| `sys_job_workflow_node` | P2 | DAG 节点 |
| `sys_job_workflow_edge` | P2 | DAG 连线 |
| `sys_job_workflow_execution` | P2 | DAG 执行记录 |

---

## 六、后端包结构

```text
com.forgex.job
 ├─ controller
 │   ├─ JobTaskController
 │   ├─ JobLogController
 │   ├─ JobInstanceController
 │   └─ JobDashboardController
 ├─ service
 │   ├─ IJobTaskService / impl
 │   ├─ IJobLogService / impl
 │   ├─ IJobInstanceService / impl
 │   └─ IJobDashboardService / impl
 ├─ domain
 │   ├─ entity
 │   ├─ param
 │   ├─ dto
 │   └─ vo
 ├─ mapper
 ├─ core
 │   ├─ scheduler
 │   │   ├─ JobScheduleScanner
 │   │   └─ JobRetryScanner
 │   ├─ executor
 │   │   ├─ JobExecutor
 │   │   ├─ JavaBeanJobInvoker
 │   │   └─ JobExecutionContext
 │   ├─ lock
 │   │   └─ JobLockService
 │   ├─ registry
 │   │   └─ JobInstanceRegistry
 │   └─ cron
 │       └─ JobTriggerTimeCalculator
 ├─ annotation
 │   └─ FxJobHandler
 ├─ enums
 └─ config
```

命名遵循现有 Forgex 模块习惯，实体、参数、VO 分层清晰，查询优先使用 MyBatis-Plus。

---

## 七、调度核心设计

### 7.1 扫描策略

`JobScheduleScanner` 每 1 秒扫描一次：

1. 查询 `status=启用` 且 `next_trigger_time <= now + lookAheadSeconds` 的任务。
2. 对每条任务按 `tenant_id + job_id + next_trigger_time` 生成锁 Key。
3. 使用 Redisson `tryLock` 抢占，抢占失败说明其他实例已处理。
4. 抢占成功后先推进 `next_trigger_time`，再提交执行，避免执行时间过长导致重复扫描。
5. 写入 `sys_job_log`，状态为执行中。
6. 执行完成后更新日志和任务最近状态。

扫描窗口建议：

| 配置 | 默认值 |
|---|---|
| `scan-interval-ms` | 1000 |
| `look-ahead-seconds` | 1 |
| `lock-wait-ms` | 100 |
| `lock-lease-seconds` | 30 |

### 7.2 下次触发时间计算

`JobTriggerTimeCalculator`：

- Cron：使用 Spring `CronExpression.parse(expression).next(now)`。
- 固定间隔：`next_trigger_time = max(now, last_trigger_time) + interval_seconds`。
- 手动任务：不计算自动触发时间。

不承诺 Quartz 的全部扩展字符。前端和后端都需要校验表达式，非法表达式禁止保存。

### 7.3 任务执行流程

```text
扫描到期任务
  -> Redisson 抢占
  -> 推进 next_trigger_time
  -> 创建执行日志
  -> 线程池执行 Java Bean
  -> 设置租户上下文
  -> 反射调用目标方法
  -> 捕获结果或异常
  -> 更新日志
  -> 更新任务最近状态
  -> 触发重试/告警/SSE
```

### 7.4 Java Bean 调用约定

P0 只支持以下两类方法签名：

```java
public JobResult execute(JobExecutionContext context)
public void execute(JobExecutionContext context)
```

`JobExecutionContext` 包含：

- `tenantId`
- `jobId`
- `jobCode`
- `logId`
- `triggerType`
- `params`
- `fireTime`

返回值统一包装为 `JobResult`，便于写日志和告警。

### 7.5 阻塞策略

同一任务再次触发时，检查是否存在执行中的日志：

| 策略 | 行为 |
|---|---|
| 丢弃后续 | 写一条取消日志或只记录丢弃计数 |
| 并行执行 | 允许新执行进入线程池 |
| 串行等待 | 新执行进入等待状态，P0 可先简化为丢弃或延后到 P1 |

### 7.6 超时控制

执行线程使用 `Future.get(timeout)` 或可取消任务包装。超时后：

1. 日志标记为超时。
2. 触发重试逻辑。
3. 记录告警事件。

Java 线程无法强制安全终止时，应要求任务处理器自行检查中断标记，文档中明确任务实现约束。

---

## 八、重试与死信

P0 可直接基于执行日志做简单重试：失败后计算下一次 retry 时间，由 `JobRetryScanner` 扫描触发。

P1 独立 `sys_job_retry`：

| 字段 | 说明 |
|---|---|
| `job_id` | 任务 ID |
| `log_id` | 原始日志 ID |
| `biz_type` / `biz_id` | 业务幂等键 |
| `retry_count` | 已重试次数 |
| `max_retry_count` | 最大重试次数 |
| `next_retry_time` | 下次重试时间 |
| `status` | 待重试、成功、死信、忽略 |

策略：

- P0：固定间隔。
- P1：指数退避。
- P1：死信人工处理。

---

## 九、实例注册与心跳

`JobInstanceRegistry` 在服务启动后注册实例：

- `instance_id = ip:port:pid`
- 启动时写入或更新 `sys_job_instance`
- 每 5 秒更新心跳和执行中任务数
- 超过 30 秒无心跳视为离线

实例离线不影响已被其他实例抢占的新任务。正在该实例执行中的任务，如果进程崩溃，会由日志状态和超时扫描进行补偿。

---

## 十、前端实现

### 10.1 目录

```text
src/api/job
 ├─ task.ts
 ├─ log.ts
 ├─ instance.ts
 └─ dashboard.ts

src/views/job
 ├─ dashboard/index.vue
 ├─ task/index.vue
 ├─ log/index.vue
 └─ instance/index.vue
```

### 10.2 页面

| 页面 | 实现 |
|---|---|
| 任务大盘 | 指标卡片 + ECharts 趋势 + 最近失败任务 |
| 任务管理 | FxDynamicTable + BaseFormDialog，支持新增、编辑、启停、触发 |
| 执行日志 | FxDynamicTable + 详情抽屉，异常堆栈折叠展示 |
| 执行器实例 | FxDynamicTable，展示在线状态和心跳时间 |

### 10.3 i18n 和权限

- 新增 `src/locales/*/job.ts`。
- 所有按钮使用 `v-permission`。
- 菜单和动态表格列配置需随 SQL 初始化脚本交付。

---

## 十一、配置与部署

### 11.1 Nacos 配置

新增或补充 `job.yml`：

```yaml
forgex:
  job:
    enabled: true
    scheduler:
      scan-interval-ms: 1000
      look-ahead-seconds: 1
      lock-lease-seconds: 30
      misfire-threshold-seconds: 60
    executor:
      core-pool-size: 10
      max-pool-size: 50
      queue-capacity: 1000
      shutdown-await-seconds: 30
    instance:
      heartbeat-interval-ms: 5000
      offline-threshold-seconds: 30
    log:
      stack-max-length: 2000
      retain-days: 30
```

`Forgex_Job/application.yml` 增加：

```yaml
spring:
  config:
    import:
      - nacos:common.yml
      - nacos:${FORGEX_DATASOURCE_CONFIG:datasource-forgex-dev.yml}
      - nacos:redis.yml
      - optional:nacos:rocketmq.yml
      - optional:nacos:sa-token.yml
      - optional:nacos:job.yml
```

### 11.2 Redis Key

| Key | 用途 |
|---|---|
| `forgex:job:lock:{tenantId}:{jobId}:{fireTime}` | 任务触发互斥 |
| `forgex:job:manual:{tenantId}:{jobId}:{requestId}` | 手动触发幂等 |
| `forgex:job:instance:{instanceId}` | 实例心跳缓存，可选 |

### 11.3 部署

- 沿用 `forgex-job` 服务，默认端口 9004。
- 首期建议部署 2 个实例验证锁互斥。
- 数据库初始化脚本需要创建 `forgex_job` 表结构、菜单、权限、动态表格列配置。

---

## 十二、实施计划

| 阶段 | 工期建议 | 交付内容 |
|---|---|---|
| 阶段一 | 1 周 | 移除 Snail-Job 依赖、建表、实体/Mapper/Service、实例心跳 |
| 阶段二 | 1.5 周 | 调度扫描器、Cron 计算、Java Bean 执行器、日志、手动触发 |
| 阶段三 | 1 周 | 前端大盘、任务管理、日志、实例页面，权限和 i18n |
| 阶段四 | 1 周 | 重试/死信、日志清理、低风险存量任务迁移 |
| 阶段五 | 2 周 | 告警、HTTP 任务、制造业模板、DAG 预研 |

### 12.1 P0 任务拆分

| 任务 | 产出 |
|---|---|
| 删除 Snail-Job 依赖 | 父 POM 和 Job POM 无 Snail-Job |
| 数据库脚本 | `sys_job_task`、`sys_job_log`、`sys_job_instance` |
| 后端标准分层 | Controller、Service、Mapper、Entity、Param、VO |
| 调度扫描器 | 到期任务扫描、Redisson 抢占、下次时间推进 |
| Java Bean 执行器 | Bean 调用、参数传递、超时、异常捕获 |
| 日志服务 | 执行中、成功、失败、超时状态闭环 |
| 实例心跳 | 启动注册、定时心跳、离线判断 |
| 前端页面 | dashboard、task、log、instance |

---

## 十三、质量与风险

### 13.1 测试重点

| 测试 | 内容 |
|---|---|
| 单元测试 | Cron 下次时间、固定间隔、状态流转、阻塞策略 |
| 集成测试 | 多实例抢占同一任务只执行一次 |
| 异常测试 | 任务抛异常、超时、进程重启、Redis 暂不可用 |
| 前端测试 | 新增任务、手动触发、查看日志、权限按钮 |

### 13.2 风险与应对

| 风险 | 应对 |
|---|---|
| Spring CronExpression 不支持部分 Quartz 表达式 | 需求文档明确不承诺全部 Quartz 特殊字符，保存时校验 |
| Redis 故障导致无法抢占 | 调度暂停并告警，不降级为无锁执行 |
| 任务执行过慢占满线程池 | 配置线程池队列上限、超时、失败告警 |
| 扫描推进时间和执行失败之间不一致 | 先写日志再执行，失败进入重试，不回退 `next_trigger_time` |
| 租户上下文丢失 | `JobExecutionContext` 显式携带 tenantId，执行前设置、finally 清理 |
| 存量任务迁移影响业务 | 先迁移低风险任务，保留原任务开关和回滚方案 |

---

## 十四、上线检查

1. POM 中无 Snail-Job、XXL-Job、Quartz 依赖。
2. `forgex-job` 服务 9004 可启动并注册 Nacos。
3. `forgex_job` 数据库表、菜单、权限、动态表格配置已初始化。
4. 两实例部署时同一 Cron 任务只执行一次。
5. 手动触发具备幂等 requestId 或短期防重复机制。
6. 执行日志能记录成功、失败、超时。
7. 页面 i18n、权限、动态表格列配置可用。
8. 未迁移的 `@Scheduled` 任务保持原行为。

---

**文档版本**: V1.0  
**创建日期**: 2026-05-09  
**作者**: ForGexTeam
