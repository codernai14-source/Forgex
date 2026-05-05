package com.forgex.integration.enums;

/**
 * 接口任务状态枚举。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public enum ApiTaskStatusEnum {
    WAITING,
    QUEUED,
    RUNNING,
    SUCCESS,
    FAIL,
    RETRY;

    /**
     * 处理isfinished。
     *
     * @return 是否处理成功
     */
    public boolean isFinished() {
        return this == SUCCESS || this == FAIL;
    }
}
