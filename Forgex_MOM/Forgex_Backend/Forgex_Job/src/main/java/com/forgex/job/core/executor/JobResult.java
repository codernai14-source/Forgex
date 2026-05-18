package com.forgex.job.core.executor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Job 执行结果。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobResult {
    private boolean success;
    private String message;

    public static JobResult success(String message) {
        return new JobResult(true, message);
    }

    public static JobResult failure(String message) {
        return new JobResult(false, message);
    }
}
