package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiCallLogDTO;
import com.forgex.integration.domain.param.ApiCallLogParam;
import com.forgex.integration.service.IApiCallLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接口调用记录管理控制器
 * <p>
 * 提供接口调用记录的分页查询、详情查询、统计等 RESTful API 接口
 * 支持按时间范围、接口配置、调用状态等条件查询
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see com.forgex.integration.service.IApiCallLogService
 * @see com.forgex.integration.domain.dto.ApiCallLogDTO
 */
@RestController
@RequestMapping("/api/integration/call-log")
@RequiredArgsConstructor
@Tag(name = "接口调用记录管理", description = "提供接口调用记录的查询、统计等功能")
public class ApiCallLogController {

    private final IApiCallLogService apiCallLogService;

    /**
     * 分页查询调用记录列表
     * <p>
     * 支持按时间范围、接口配置 ID、调用方向、调用状态等条件查询
     * 自动根据时间范围查询对应的月份表
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ApiCallLogParam
     * @see ApiCallLogDTO
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询调用记录", description = "支持按时间范围、接口配置、调用状态等条件查询")
    public R<Page<ApiCallLogDTO>> pageCallLogs(@RequestBody @Validated ApiCallLogParam param) {
        Page<ApiCallLogDTO> page = apiCallLogService.pageCallLogs(param);
        return R.ok(page);
    }

    /**
     * 查询调用记录列表（不分页）
     * <p>
     * 用于导出 Excel 等场景
     * </p>
     *
     * @param param 查询参数
     * @return 调用记录列表
     * @see ApiCallLogParam
     * @see ApiCallLogDTO
     */
    @PostMapping("/list")
    @Operation(summary = "查询调用记录列表", description = "不分页查询，用于导出等场景")
    public R<List<ApiCallLogDTO>> listCallLogs(@RequestBody ApiCallLogParam param) {
        List<ApiCallLogDTO> list = apiCallLogService.listCallLogs(param);
        return R.ok(list);
    }

    /**
     * 根据 ID 查询调用记录详情
     * <p>
     * 根据调用时间自动从对应的月份表中查询详情
     * </p>
     *
     * @param id 调用记录 ID
     * @param callTime 调用时间（用于确定查询哪个月份表）
     * @return 调用记录详情
     * @see ApiCallLogDTO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "查询调用记录详情", description = "根据 ID 和调用时间查询详细信息")
    public R<ApiCallLogDTO> getCallLogDetail(
        @PathVariable Long id,
        @RequestParam LocalDateTime callTime
    ) {
        ApiCallLogDTO dto = apiCallLogService.getCallLogById(id, callTime);
        if (dto == null) {
            R<ApiCallLogDTO> result = R.fail();
            result.setMessage("调用记录不存在");
            return result;
        }
        return R.ok(dto);
    }

    /**
     * 统计调用次数
     * <p>
     * 统计指定接口在时间范围内的调用次数
     * </p>
     *
     * @param apiConfigId 接口配置 ID（可选，不传则统计所有接口）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 调用次数
     */
    @GetMapping("/count")
    @Operation(summary = "统计调用次数", description = "统计指定接口在时间范围内的调用次数")
    public R<Long> countCallLogs(
        @RequestParam(required = false) Long apiConfigId,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime
    ) {
        long count = apiCallLogService.countCallLogs(apiConfigId, startTime, endTime);
        return R.ok(count);
    }

    /**
     * 统计调用成功率
     * <p>
     * 计算指定接口在时间范围内的调用成功率
     * </p>
     *
     * @param apiConfigId 接口配置 ID（可选，不传则统计所有接口）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率（0-100 之间的数值）
     */
    @GetMapping("/success-rate")
    @Operation(summary = "统计调用成功率", description = "计算指定接口在时间范围内的调用成功率")
    public R<Double> calculateSuccessRate(
        @RequestParam(required = false) Long apiConfigId,
        @RequestParam LocalDateTime startTime,
        @RequestParam LocalDateTime endTime
    ) {
        double successRate = apiCallLogService.calculateSuccessRate(apiConfigId, startTime, endTime);
        return R.ok(successRate);
    }
}
