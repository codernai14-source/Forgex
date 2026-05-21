package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiCallLogDTO;
import com.forgex.integration.enums.IntegrationPromptEnum;
import com.forgex.integration.domain.param.ApiCallLogParam;
import com.forgex.integration.service.IApiCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 接口调用记录控制器
 *
 * @author Forgex Team
 */
@RestController
@RequestMapping("/call-log")
@RequiredArgsConstructor
@Tag(name = "接口调用记录", description = "接口调用记录查询与统计")
public class ApiCallLogController {

    private static final DateTimeFormatter CALL_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IApiCallLogService apiCallLogService;

    @RequirePerm("integration:api-call-log:view")
    @PostMapping("/page")
    @Operation(summary = "分页查询调用记录")
    public R<Page<ApiCallLogDTO>> pageCallLogs(@RequestBody @Validated ApiCallLogParam param) {
        return R.ok(apiCallLogService.pageCallLogs(param));
    }

    @RequirePerm("integration:api-call-log:view")
    @PostMapping("/list")
    @Operation(summary = "查询调用记录列表")
    public R<List<ApiCallLogDTO>> listCallLogs(@RequestBody ApiCallLogParam param) {
        return R.ok(apiCallLogService.listCallLogs(param));
    }

    @RequirePerm("integration:api-call-log:view")
    @GetMapping("/detail/{id}")
    @Operation(summary = "查询调用记录详情")
    public R<ApiCallLogDTO> getCallLogDetail(
        @PathVariable Long id,
        @RequestParam String callTime
    ) {
        ApiCallLogDTO dto = apiCallLogService.getCallLogById(id, parseCallTime(callTime));
        if (dto == null) {
            return R.fail(IntegrationPromptEnum.CALL_LOG_NOT_FOUND);
        }
        return R.ok(dto);
    }

    @RequirePerm("integration:api-call-log:view")
    @GetMapping("/count")
    @Operation(summary = "统计调用次数")
    public R<Long> countCallLogs(
        @RequestParam(required = false) Long apiConfigId,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime
    ) {
        return R.ok(apiCallLogService.countCallLogs(apiConfigId, startTime, endTime));
    }

    @RequirePerm("integration:api-call-log:view")
    @GetMapping("/success-rate")
    @Operation(summary = "统计调用成功率")
    public R<Double> calculateSuccessRate(
        @RequestParam(required = false) Long apiConfigId,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime
    ) {
        return R.ok(apiCallLogService.calculateSuccessRate(apiConfigId, startTime, endTime));
    }

    private LocalDateTime parseCallTime(String callTime) {
        return LocalDateTime.parse(callTime, CALL_TIME_FORMATTER);
    }
}
