package com.forgex.job.core.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgex.job.annotation.FxJobHandler;
import com.forgex.job.core.executor.JobExecutionContext;
import com.forgex.job.core.executor.JobResult;
import org.springframework.util.StringUtils;

/**
 * Job 冒烟测试处理器。
 * <p>
 * 用于验证 Java Bean 任务从任务配置、手动触发、执行日志到大盘统计的完整链路。
 * </p>
 *
 * @author Forgex
 * @version 1.0.0
 */
@FxJobHandler("jobSmokeTestHandler")
public class JobSmokeTestHandler {

    private final ObjectMapper objectMapper;

    public JobSmokeTestHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 执行冒烟测试任务。
     * <p>
     * 支持参数：
     * 1. 空参数或 {@code {"mode":"success"}}：返回成功。
     * 2. {@code {"mode":"fail"}}：返回失败结果，用于测试失败日志和重试记录。
     * 3. {@code {"mode":"exception"}}：抛出异常，用于测试异常堆栈。
     * 4. {@code {"mode":"sleep","sleepMs":3000}}：休眠指定毫秒，用于测试耗时和超时。
     * </p>
     *
     * @param context Job 执行上下文
     * @return 执行结果
     */
    public JobResult execute(JobExecutionContext context) {
        SmokeParam param = parseParam(context.getParams());
        if ("sleep".equalsIgnoreCase(param.mode) && param.sleepMs > 0) {
            sleep(param.sleepMs);
        }
        if ("exception".equalsIgnoreCase(param.mode)) {
            throw new IllegalStateException("Job smoke test exception");
        }
        if ("fail".equalsIgnoreCase(param.mode)) {
            return JobResult.failure("Job smoke test failed by params");
        }
        return JobResult.success(buildSuccessMessage(context, param));
    }

    private SmokeParam parseParam(String params) {
        SmokeParam param = new SmokeParam();
        if (!StringUtils.hasText(params)) {
            return param;
        }
        String trimmed = params.trim();
        if (!trimmed.startsWith("{")) {
            param.mode = trimmed;
            return param;
        }
        try {
            JsonNode root = objectMapper.readTree(trimmed);
            param.mode = root.path("mode").asText("success");
            param.sleepMs = root.path("sleepMs").asLong(0);
            param.message = root.path("message").asText("");
            return param;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Job smoke test params must be JSON or simple mode", ex);
        }
    }

    private void sleep(long sleepMs) {
        try {
            Thread.sleep(sleepMs);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Job smoke test interrupted", ex);
        }
    }

    private String buildSuccessMessage(JobExecutionContext context, SmokeParam param) {
        String message = StringUtils.hasText(param.message) ? param.message : "Job smoke test success";
        return message
            + ", jobCode=" + context.getJobCode()
            + ", logId=" + context.getLogId()
            + ", tenantId=" + context.getTenantId();
    }

    private static class SmokeParam {
        private String mode = "success";
        private long sleepMs;
        private String message;
    }
}
