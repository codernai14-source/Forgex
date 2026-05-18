export interface PageResult<T> {
  records: T[]
  total: number
  current?: number
  size?: number
}

export const JOB_API_BASE = '/job'

export interface JobPageParam {
  pageNum?: number
  pageSize?: number
  jobCode?: string
  jobName?: string
  jobGroup?: string
  jobType?: number
  scheduleType?: number
  status?: number
}

export interface JobTask {
  id?: number
  jobCode?: string
  jobName?: string
  jobGroup?: string
  jobType?: number
  scheduleType?: number
  cronExpression?: string
  intervalSeconds?: number
  beanName?: string
  methodName?: string
  httpUrl?: string
  httpMethod?: string
  httpHeaders?: string
  scriptType?: string
  scriptPath?: string
  scriptArgs?: string
  mqTopic?: string
  mqTags?: string
  workflowId?: number
  jobParams?: string
  status?: number
  blockStrategy?: number
  timeoutSeconds?: number
  maxRetryCount?: number
  retryIntervalSeconds?: number
  shardTotal?: number
  broadcastEnabled?: number
  nextTriggerTime?: string
  lastTriggerTime?: string
  lastStatus?: number
  triggerCount?: number
  remark?: string
}

export interface JobLog {
  id?: number
  jobId?: number
  jobCode?: string
  jobName?: string
  triggerType?: number
  fireTime?: string
  startTime?: string
  endTime?: string
  durationMs?: number
  status?: number
  instanceId?: string
  requestParams?: string
  resultMessage?: string
  errorStack?: string
  retryCount?: number
  shardIndex?: number
  shardTotal?: number
  requestId?: string
}

export interface JobInstance {
  id?: number
  instanceId?: string
  serviceName?: string
  ip?: string
  port?: number
  pid?: string
  status?: number
  runningCount?: number
  lastHeartbeatTime?: string
  startTime?: string
  maintenance?: number
}

export interface JobRetry {
  id?: number
  jobId?: number
  logId?: number
  jobCode?: string
  bizType?: string
  bizId?: string
  retryCount?: number
  maxRetryCount?: number
  nextRetryTime?: string
  status?: number
  lastError?: string
  handleRemark?: string
}

export interface JobAlarmRule {
  id?: number
  ruleName?: string
  jobId?: number
  jobCode?: string
  alarmType?: number
  thresholdCount?: number
  windowMinutes?: number
  notifyType?: string
  notifyTarget?: string
  status?: number
  remark?: string
}

export interface JobAlarmLog {
  id?: number
  ruleId?: number
  jobId?: number
  jobCode?: string
  logId?: number
  alarmType?: number
  sendStatus?: number
  notifyType?: string
  notifyTarget?: string
  content?: string
  errorMessage?: string
}

export interface JobWorkflow {
  id?: number
  workflowCode?: string
  workflowName?: string
  status?: number
  graphJson?: string
  remark?: string
}

export interface JobWorkflowExecution {
  id?: number
  workflowId?: number
  workflowCode?: string
  rootLogId?: number
  status?: number
  startTime?: string
  endTime?: string
  nodeStatusJson?: string
  resultMessage?: string
}

export interface JobDashboardSummary {
  totalTasks?: number
  enabledTasks?: number
  todayExecutions?: number
  successExecutions?: number
  failedExecutions?: number
  timeoutExecutions?: number
  onlineInstances?: number
  successRate?: number
}

export interface JobTrend {
  time?: string
  success?: number
  failed?: number
  timeout?: number
}
