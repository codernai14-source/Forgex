package com.forgex.job.core.executor;

import com.forgex.job.config.JobProperties;
import com.forgex.job.domain.entity.SysJobTask;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 脚本任务调用器。
 *
 * @author Forgex
 * @version 1.0.0
 */
@Component
public class ScriptJobInvoker {

    private final JobProperties properties;

    public ScriptJobInvoker(JobProperties properties) {
        this.properties = properties;
    }

    public JobResult invoke(SysJobTask task) throws Exception {
        if (!properties.getSecurity().isScriptEnabled()) {
            throw new IllegalStateException("script job disabled");
        }
        if (!StringUtils.hasText(task.getScriptPath())) {
            throw new IllegalArgumentException("scriptPath required");
        }
        File script = new File(task.getScriptPath()).getCanonicalFile();
        boolean allowed = properties.getSecurity().getScriptDirectoryWhitelist().stream()
            .filter(StringUtils::hasText)
            .map(path -> {
                try {
                    return new File(path).getCanonicalFile();
                } catch (Exception ex) {
                    return null;
                }
            })
            .anyMatch(dir -> dir != null && script.toPath().startsWith(dir.toPath()));
        if (!allowed) {
            throw new IllegalArgumentException("script path is not in whitelist");
        }
        List<String> command = new ArrayList<>();
        String type = task.getScriptType() == null ? "" : task.getScriptType().toLowerCase();
        if ("powershell".equals(type)) {
            command.add("powershell");
            command.add("-ExecutionPolicy");
            command.add("Bypass");
            command.add("-File");
        } else if ("cmd".equals(type)) {
            command.add("cmd");
            command.add("/c");
        } else {
            command.add("sh");
        }
        command.add(script.getAbsolutePath());
        if (StringUtils.hasText(task.getScriptArgs())) {
            command.add(task.getScriptArgs());
        }
        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        long timeout = task.getTimeoutSeconds() == null ? 60 : task.getTimeoutSeconds();
        boolean finished = process.waitFor(timeout, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return JobResult.failure("script timeout after " + Duration.ofSeconds(timeout));
        }
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String message = output.length() > 1000 ? output.substring(0, 1000) : output;
        return process.exitValue() == 0 ? JobResult.success(message) : JobResult.failure(message);
    }
}
