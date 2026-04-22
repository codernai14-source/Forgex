package com.forgex.integration.enums;

public enum ApiTaskStatusEnum {
    WAITING,
    QUEUED,
    RUNNING,
    SUCCESS,
    FAIL,
    RETRY;

    public boolean isFinished() {
        return this == SUCCESS || this == FAIL;
    }
}
