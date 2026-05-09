# Forgex_Job 任务调度系统需求文档

> **文档版本**: V1.0
> **创建日期**: 2026-05-09
> **作者**: ForGexTeam
> **状态**: 初稿修订

---

## 一、项目现状核对

本次修订基于当前仓库实际情况，并按“不使用 Snail-Job / XXL-Job / Quartz 等第三方调度框架”的约束重新收敛范围。版权风险需要由法务或项目负责人最终确认，工程方案按规避 Snail-Job 依赖处理。

### 1.1 已存在基础

| 项目项 | 当前状态 | 结论 |
|---|---|---|
| 后端模块 | `Forgex_MOM/Forgex_Backend/Forgex_Job` 已存在 | 继续作为独立任务调度服务建设 |
| 服务端口 | `application.yml` 与构建清单均约定 `forgex-job` 端口 `9004` | 不另建服务 |
| 数据源 | Nacos 数据源配置已预留 `forgex_job` | 调度业务表落在 job 库 |
| 依赖 | 后端父 POM 与 `Forgex_Job/pom.xml` 当前已引入 Snail-Job 1.8.1 client 依赖 | 需要移除，避免版权和依赖边界风险 |
| 启动类 | `ForgexJobApplication` 已启用注册发现、Feign、异步、定时任务 | 可承接调度扫描、任务执行、管理 API |
| 前端基础 | 已有 Vue3、Ant Design Vue、FxDynamicTable、ECharts、SSE、Vue Flow | 可按现有管理页模式新增 job 页面 |
| 部署清单 | `Forgex_Build/manifest/services.yml` 已登记 job 服务 | 需要补齐 job 配置和数据库脚本，不改服务拓扑 |

### 1.2 当前定时任务分布

| 模块 | 类/服务 | 当前用途 | 迁移建议 |
|---|---|---|---|
| `Forgex_Common` | `LicenseRefreshScheduler` | 授权刷新 | 暂不迁移，授权链路不依赖业务调度平台 |
| `Forgex_Sys` | `SysAndroidVersionUploadTaskServiceImpl` | 安卓上传临时任务清理 | P1 迁入 Job 平台 |
| `Forgex_Sys` | `SseEmitterService` 2 处 | SSE 心跳与连接清理 | 暂不迁移，属于连接保活基础设施 |
| `Forgex_Integration` | `ApiCallLogTableServiceImpl` | 调用日志分表保障 | P1 迁入 Job 平台 |
| `Forgex_Integration` | `ApiLogBufferServiceImpl` | 日志缓冲刷盘 | 暂不迁移，短周期高频任务保留本地调度 |
| `Forgex_Integration` | `ApiTaskConsumer` | 异步任务消费 | 建议改为 MQ/线程池模型，不作为普通 Cron 迁移 |
| `Forgex_Integration` | `ApiTaskServiceImpl` | 异步任务扫描 | P1 评估迁入或改为 MQ 消费 |

### 1.3 核心建议

1. **移除 Snail-Job 依赖**：从后端父 POM 和 `Forgex_Job/pom.xml` 删除 Snail-Job 相关依赖，文档和代码不再以 Snail-Job 为前提。
2. **做 Forgex 自研轻量调度平台，不做完整 XXL-Job 复刻**：P0 覆盖任务注册、Cron 调度、手动触发、执行日志、重试、实例心跳、权限和基础前端页面。
3. **调度核心保持可验证**：使用数据库任务表作为事实源，使用 Spring/JDK 定时扫描，使用 Redisson 分布式锁避免多实例重复触发。
4. **分阶段迁移存量任务**：先迁移清理、归档、同步类低风险任务；SSE 心跳、授权刷新、毫秒级消费扫描暂不迁移。
5. **DAG、脚本任务、复杂路由放到后续阶段**：避免 P0 范围过大导致基础平台迟迟不能上线。

---

## 二、建设目标

### 2.1 总目标

建设 `Forgex_Job` 任务调度服务，统一承载 Forgex 中可集中管理的定时任务、手动任务、异步补偿任务和同步任务，并与现有网关、权限、多租户、国际化、SSE、动态表格、部署体系保持一致。

### 2.2 技术边界

| 边界 | 说明 |
|---|---|
| 不使用第三方调度框架 | 不引入 Snail-Job、XXL-Job、Quartz |
| 可以使用项目已有基础设施 | Redisson、RocketMQ、Nacos、Sa-Token、MyBatis-Plus、SSE 属于现有项目能力 |
| Cron 解析优先使用 Spring 自带能力 | 避免自研复杂 Cron 边界；不满足的表达式能力先不承诺 |
| 数据库为调度事实源 | 任务定义、下次触发时间、执行日志、重试记录都落库 |
| 分布式互斥由 Redis 锁保障 | 多实例部署时同一任务同一触发点只允许一个实例成功抢占 |

### 2.3 不做事项

| 不做项 | 原因 |
|---|---|
| P0 不复刻完整 XXL-Job/Snail-Job 功能 | 成本和测试面过大，当前先要可运维闭环 |
| P0 不支持所有 Quartz 特殊 Cron 字符 | 不引入 Quartz，Cron 表达式能力以 Spring `CronExpression` 实际支持范围为准 |
| 不一次性替换所有 `@Scheduled` | 部分任务是基础设施心跳或授权刷新，集中调度反而增加故障半径 |
| P0 不实现完整 DAG 工作流编排 | 项目已有 `Forgex_Workflow` 审批工作流，Job 的 DAG 应定位为数据/批处理编排 |
| P0 不开放任意 Shell/CMD 执行 | 安全风险高，需白名单、目录限制和审计后再开放 |

---

## 三、角色与权限

| 角色 | 职责 |
|---|---|
| 系统管理员 | 任务配置、执行器管理、权限配置、日志清理 |
| 运维人员 | 查看实例、处理失败任务、查看告警与运行指标 |
| 开发人员 | 注册任务处理器、调试任务、查看执行日志 |
| 生产主管 | 查看任务运行结果、手动触发授权范围内的同步/报表任务 |

| 权限键 | 说明 | 优先级 |
|---|---|---|
| `job:dashboard:view` | 查看调度大盘 | P0 |
| `job:task:list` | 查看任务列表 | P0 |
| `job:task:add` | 新增任务 | P0 |
| `job:task:edit` | 编辑任务 | P0 |
| `job:task:delete` | 删除任务 | P0 |
| `job:task:trigger` | 手动触发任务 | P0 |
| `job:task:pause` | 暂停/恢复任务 | P0 |
| `job:log:list` | 查看执行日志 | P0 |
| `job:log:clean` | 清理执行日志 | P1 |
| `job:instance:list` | 查看执行器实例 | P0 |
| `job:alarm:list` | 查看告警规则和告警日志 | P1 |
| `job:alarm:edit` | 编辑告警规则 | P1 |
| `job:workflow:list` | 查看 DAG 编排 | P2 |
| `job:workflow:edit` | 编辑 DAG 编排 | P2 |
| `job:retry:list` | 查看重试和死信 | P1 |
| `job:retry:handle` | 处理死信 | P1 |

---

## 四、功能需求

### 4.1 任务管理

| 编号 | 需求描述 | 优先级 |
|---|---|:---:|
| JOB-001 | 支持任务列表查询，按任务编码、名称、组、类型、状态筛选 | P0 |
| JOB-002 | 支持新增、编辑、删除任务配置 | P0 |
| JOB-003 | 任务编码 `job_code` 在同一租户下唯一，创建后不可修改 | P0 |
| JOB-004 | 支持任务启用、停用、暂停、恢复 | P0 |
| JOB-005 | 支持手动触发一次任务 | P0 |
| JOB-006 | 支持查看下次触发时间、最近执行状态、最近执行时间、累计执行次数 | P0 |
| JOB-007 | 支持复制任务配置快速创建新任务 | P1 |
| JOB-008 | 支持任务分类，首期分类包括系统维护、集成同步、报表统计、制造执行、其他 | P1 |
| JOB-009 | 任务名称和描述支持多语言字段或 I18nKey | P1 |

### 4.2 任务类型与调度方式

| 编号 | 需求描述 | 优先级 |
|---|---|:---:|
| JOB-020 | 支持 Java Bean 任务，按 Spring Bean 名称和方法名执行 | P0 |
| JOB-021 | 支持 Cron 表达式调度，表达式能力以 Spring `CronExpression` 为准 | P0 |
| JOB-022 | 支持固定间隔任务 | P0 |
| JOB-023 | 支持仅手动触发模式 | P0 |
| JOB-024 | 支持 HTTP 任务，调用内部或白名单外部地址 | P1 |
| JOB-025 | 支持 API 触发任务 | P1 |
| JOB-026 | 支持 RocketMQ 事件触发任务 | P1 |
| JOB-027 | 支持 Shell/CMD/PowerShell 脚本任务，但必须启用白名单目录、命令审计和参数转义 | P2 |
| JOB-028 | 前端支持未来 5 次触发时间预览 | P2 |

### 4.3 执行控制

| 编号 | 需求描述 | 优先级 |
|---|---|:---:|
| JOB-040 | 支持任务超时时间配置 | P0 |
| JOB-041 | 支持失败重试次数和重试间隔配置 | P0 |
| JOB-042 | 支持阻塞策略：丢弃后续、并行执行、串行等待 | P0 |
| JOB-043 | 支持任务参数 JSON | P0 |
| JOB-044 | 支持执行结果摘要和异常堆栈截断保存 | P0 |
| JOB-045 | 支持错过触发补偿策略：立即补偿一次或放弃 | P1 |
| JOB-046 | 支持分片执行 | P2 |
| JOB-047 | 支持广播执行 | P2 |
| JOB-048 | 支持优先级 | P2 |

### 4.4 执行日志

| 编号 | 需求描述 | 优先级 |
|---|---|:---:|
| JOB-060 | 每次触发记录执行日志，包含任务、实例、触发方式、状态、开始时间、结束时间、耗时、结果摘要 | P0 |
| JOB-061 | 日志状态包括待执行、执行中、成功、失败、超时、取消 | P0 |
| JOB-062 | 日志列表支持任务编码、状态、触发方式、时间范围筛选 | P0 |
| JOB-063 | 日志详情展示异常堆栈、请求参数、执行结果、重试关联 | P0 |
| JOB-064 | 支持日志清理策略，默认保留 30 天 | P1 |
| JOB-065 | 从任务管理页跳转到该任务日志 | P0 |

### 4.5 重试、实例、告警和监控

| 编号 | 需求描述 | 优先级 |
|---|---|:---:|
| JOB-070 | 任务失败后按配置自动重试 | P0 |
| JOB-071 | 支持固定间隔和指数退避两类重试策略 | P1 |
| JOB-072 | 重试耗尽后进入死信或失败待处理状态 | P1 |
| JOB-073 | 死信支持重新执行、忽略、备注处理 | P1 |
| JOB-080 | 展示在线执行器实例、IP、端口、启动时间、最后心跳时间 | P0 |
| JOB-081 | 展示实例执行中任务数和最近失败数 | P1 |
| JOB-082 | 支持实例下线后不再抢占新任务 | P0 |
| JOB-090 | 任务失败、超时、连续失败时触发告警 | P1 |
| JOB-091 | 通知优先复用 Forgex 消息模板与 SSE 站内推送 | P1 |
| JOB-100 | 展示任务总数、运行中、今日执行数、成功率、失败数、在线实例数 | P0 |
| JOB-101 | 展示最近 24 小时执行趋势 | P0 |
| JOB-102 | 展示 TOP10 失败任务和 TOP10 耗时任务 | P1 |

### 4.6 存量任务迁移

| 编号 | 任务 | 迁移优先级 | 说明 |
|---|---|:---:|---|
| JOB-110 | 安卓上传临时任务清理 | P1 | 可迁入 Job 统一管理 |
| JOB-111 | 集成平台调用日志分表保障 | P1 | 可迁入 Job 统一管理 |
| JOB-112 | 集成平台任务扫描 | P1 | 先评估是否改 RocketMQ 消费 |
| JOB-113 | API 日志缓冲刷盘 | P2 | 高频短周期任务，先保留本地实现 |
| JOB-114 | SSE 心跳与连接清理 | 暂不迁移 | 基础设施保活任务 |
| JOB-115 | 授权刷新 | 暂不迁移 | 授权链路需降低外部依赖 |

### 4.7 制造业预置任务与 DAG

制造业任务先作为模板和示例，不作为 P0 强制上线内容。

| 场景 | 任务编码建议 | 优先级 |
|---|---|:---:|
| ERP 生产订单同步 | `erpProductionOrderSync` | P1 |
| 报工数据回传 ERP | `workReportSync` | P1 |
| BOM 数据同步 | `bomSync` | P1 |
| 安全库存预警 | `inventorySafetyAlarm` | P1 |
| 质检任务触发 | `qualityInspectionTrigger` | P1 |
| 设备保养计划检查 | `equipmentMaintenanceCheck` | P2 |
| OEE 计算 | `oeeCalculation` | P2 |
| 生产日报生成 | `productionDailyReport` | P2 |

DAG 编排定位为 P2，不与审批工作流混用。P2 再支持 Vue Flow 批处理 DAG 设计器、环检测、节点执行历史和条件节点。

---

## 五、前端页面需求

| 页面 | 路由建议 | 组件模式 | 优先级 |
|---|---|---|:---:|
| 任务大盘 | `/job/dashboard` | ECharts + 指标卡片 | P0 |
| 任务管理 | `/job/task` | FxDynamicTable + BaseFormDialog | P0 |
| 执行日志 | `/job/log` | FxDynamicTable + 详情抽屉 | P0 |
| 执行器实例 | `/job/instance` | FxDynamicTable | P0 |
| 重试/死信 | `/job/retry` | FxDynamicTable + Tab | P1 |
| 告警规则 | `/job/alarm` | FxDynamicTable + BaseFormDialog | P1 |
| 告警日志 | `/job/alarm-log` | FxDynamicTable | P1 |
| DAG 编排 | `/job/workflow` | FxDynamicTable + Vue Flow | P2 |

前端要求：

1. 列表页使用 `FxDynamicTable`，表格编码需要写入动态列配置。
2. 新增、编辑使用 `BaseFormDialog`。
3. 操作按钮使用 `v-permission`。
4. 页面文本接入 5 语言资源，不硬编码中文。
5. API 封装放在 `src/api/job`，页面放在 `src/views/job`。

---

## 六、数据与接口要求

### 6.1 数据库

任务调度业务表落在 `forgex_job` 数据库。表命名建议使用 `sys_job_*`，并遵循 Forgex 通用字段规范：`id`、`tenant_id`、`create_time`、`update_time`、`create_by`、`update_by`、`deleted`。

| 表 | 说明 | 优先级 |
|---|---|:---:|
| `sys_job_task` | 任务定义 | P0 |
| `sys_job_log` | 执行日志 | P0 |
| `sys_job_instance` | 执行器实例 | P0 |
| `sys_job_retry` | 重试记录 | P1 |
| `sys_job_alarm_rule` | 告警规则 | P1 |
| `sys_job_alarm_log` | 告警发送日志 | P1 |
| `sys_job_workflow` | DAG 定义 | P2 |
| `sys_job_workflow_node` | DAG 节点 | P2 |
| `sys_job_workflow_edge` | DAG 连线 | P2 |
| `sys_job_workflow_execution` | DAG 执行实例 | P2 |

### 6.2 接口

接口前缀建议为 `/job`，经 Gateway 转发至 `forgex-job`。

| API | 说明 | 优先级 |
|---|---|:---:|
| `POST /job/task/page` | 任务分页 | P0 |
| `POST /job/task/save` | 新增/编辑任务 | P0 |
| `POST /job/task/delete` | 删除任务 | P0 |
| `POST /job/task/trigger` | 手动触发 | P0 |
| `POST /job/task/change-status` | 启用、停用、暂停、恢复 | P0 |
| `POST /job/log/page` | 日志分页 | P0 |
| `GET /job/log/detail/{id}` | 日志详情 | P0 |
| `POST /job/instance/page` | 实例分页 | P0 |
| `GET /job/dashboard/summary` | 大盘摘要 | P0 |

---

## 七、非功能需求

| 编号 | 需求 | 目标 |
|---|---|---|
| NFR-001 | 调度可靠性 | 单实例异常不影响其他实例执行 |
| NFR-002 | 调度精度 | 普通业务任务误差控制在秒级，P0 不承诺毫秒级 |
| NFR-003 | 日志查询 | 10 万级日志数据分页查询 1 秒内返回，必要时加时间范围索引 |
| NFR-004 | 租户隔离 | 任务、日志、告警按 `tenant_id` 隔离 |
| NFR-005 | 权限控制 | 后端接口和前端按钮均校验权限 |
| NFR-006 | 安全 | HTTP/Shell 类任务必须限制白名单，异常堆栈截断保存 |
| NFR-007 | 可观测 | 每次执行必须有日志，失败必须可追溯 |
| NFR-008 | 可部署 | 延续现有 9004 job 服务、Nacos、构建清单和授权模块配置 |

---

## 八、验收标准

### 8.1 P0 验收

1. `Forgex_Job` 服务可独立启动并注册到 Nacos。
2. 项目中不再依赖 Snail-Job、XXL-Job、Quartz。
3. 至少支持 Java Bean 任务的创建、编辑、启停、手动触发、Cron 调度和固定间隔调度。
4. 多实例部署时，同一任务同一触发点不会重复执行。
5. 每次执行产生执行日志，日志可分页查询和查看详情。
6. 任务大盘展示核心指标和 24 小时趋势。
7. 前端任务管理、日志、实例、大盘页面可用，并接入权限和 i18n。
8. 任务、日志、实例数据具备租户隔离。
9. 不影响现有 Sys、Integration、Common 模块的本地定时任务运行。

### 8.2 P1 验收

1. 迁移至少两个低风险存量任务到 Job 平台。
2. 支持失败重试、死信处理和基础告警。
3. 支持 HTTP 任务和 RocketMQ 事件触发任务。
4. 制造业预置任务模板可创建并手动触发。

### 8.3 P2 验收

1. DAG 批处理编排可视化编辑、保存、执行和查看历史。
2. 脚本任务在白名单和审计约束下可用。
3. 支持分片、广播、维护模式等增强能力。

---

## 九、术语表

| 术语 | 说明 |
|---|---|
| 调度扫描器 | `Forgex_Job` 内部周期扫描可触发任务的组件 |
| 执行器 | 实际执行任务代码的 `forgex-job` 实例 |
| 任务处理器 | Java Bean 中承载具体业务逻辑的方法或类 |
| 分布式锁 | 用于多实例互斥抢占任务的 Redis 锁 |
| 死信 | 重试耗尽后等待人工处理的失败记录 |
| DAG | 有向无环图，用于数据处理或批处理任务编排，不等同于审批工作流 |

---

**文档版本**: V1.0
**创建日期**: 2026-05-09
**作者**: ForGexTeam
**审核人**: 待定
