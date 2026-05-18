package com.forgex.job.enums;

import com.forgex.common.i18n.I18nPrompt;
import lombok.Getter;

/**
 * Job 模块国际化提示码。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Getter
public enum JobPromptEnum implements I18nPrompt {
    JOB_PARAM_REQUIRED("JOB_PARAM_REQUIRED", "任务参数不能为空"),
    JOB_NOT_FOUND("JOB_NOT_FOUND", "任务不存在"),
    JOB_CODE_EXISTS("JOB_CODE_EXISTS", "任务编码已存在"),
    JOB_CODE_IMMUTABLE("JOB_CODE_IMMUTABLE", "任务编码不允许修改"),
    JOB_CRON_INVALID("JOB_CRON_INVALID", "Cron 表达式不合法"),
    JOB_JSON_INVALID("JOB_JSON_INVALID", "JSON 参数不合法"),
    JOB_HANDLER_NOT_FOUND("JOB_HANDLER_NOT_FOUND", "任务处理器不存在"),
    JOB_TRIGGER_FAILED("JOB_TRIGGER_FAILED", "任务触发失败"),
    JOB_HTTP_NOT_ALLOWED("JOB_HTTP_NOT_ALLOWED", "HTTP 地址未在白名单内"),
    JOB_SCRIPT_NOT_ALLOWED("JOB_SCRIPT_NOT_ALLOWED", "脚本执行未开启或命令未在白名单内"),
    JOB_WORKFLOW_CYCLE("JOB_WORKFLOW_CYCLE", "DAG 编排存在环"),
    JOB_WORKFLOW_NOT_FOUND("JOB_WORKFLOW_NOT_FOUND", "DAG 编排不存在"),
    JOB_RETRY_NOT_FOUND("JOB_RETRY_NOT_FOUND", "重试记录不存在"),
    JOB_ALARM_NOT_FOUND("JOB_ALARM_NOT_FOUND", "告警规则不存在");

    private final String promptCode;
    private final String defaultTemplate;

    JobPromptEnum(String promptCode, String defaultTemplate) {
        this.promptCode = promptCode;
        this.defaultTemplate = defaultTemplate;
    }

    @Override
    public String getModule() {
        return "job";
    }

}
