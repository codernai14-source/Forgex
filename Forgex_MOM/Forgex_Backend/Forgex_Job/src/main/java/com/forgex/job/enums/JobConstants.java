package com.forgex.job.enums;

/**
 * Job 模块常量。
 *
 * @author Forgex
 * @version 1.0.0
 */
public final class JobConstants {

    public static final int JOB_TYPE_JAVA_BEAN = 1;
    public static final int JOB_TYPE_HTTP = 2;
    public static final int JOB_TYPE_SCRIPT = 3;
    public static final int JOB_TYPE_ROCKETMQ = 4;
    public static final int JOB_TYPE_WORKFLOW = 5;

    public static final int SCHEDULE_CRON = 1;
    public static final int SCHEDULE_INTERVAL = 2;
    public static final int SCHEDULE_MANUAL = 3;

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_PAUSED = 2;

    public static final int LOG_PENDING = 0;
    public static final int LOG_RUNNING = 1;
    public static final int LOG_SUCCESS = 2;
    public static final int LOG_FAILED = 3;
    public static final int LOG_TIMEOUT = 4;
    public static final int LOG_CANCELED = 5;

    public static final int TRIGGER_SCHEDULE = 1;
    public static final int TRIGGER_MANUAL = 2;
    public static final int TRIGGER_RETRY = 3;
    public static final int TRIGGER_API = 4;
    public static final int TRIGGER_EVENT = 5;

    public static final int BLOCK_DISCARD = 1;
    public static final int BLOCK_PARALLEL = 2;
    public static final int BLOCK_SERIAL = 3;

    public static final int INSTANCE_ONLINE = 1;
    public static final int INSTANCE_OFFLINE = 2;
    public static final int INSTANCE_MAINTENANCE = 3;

    public static final int RETRY_WAITING = 1;
    public static final int RETRY_SUCCESS = 2;
    public static final int RETRY_DEAD = 3;
    public static final int RETRY_IGNORED = 4;

    public static final int WORKFLOW_DRAFT = 0;
    public static final int WORKFLOW_PUBLISHED = 1;

    private JobConstants() {
    }
}
