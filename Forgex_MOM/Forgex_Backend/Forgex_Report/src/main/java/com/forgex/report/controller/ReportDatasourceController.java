package com.forgex.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.web.R;
import com.forgex.report.domain.dto.ReportDatasourceDTO;
import com.forgex.report.domain.param.ReportDatasourceParam;
import com.forgex.report.service.IReportDatasourceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表数据源管理控制器
 * <p>
 * 提供报表数据源的 RESTful API 接口
 * </p>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see IReportDatasourceService
 */
@Tag(name = "报表数据源管理", description = "报表数据源管理接口")
@RestController
@RequestMapping("/api/report/datasource")
@RequiredArgsConstructor
public class ReportDatasourceController {

    private final IReportDatasourceService datasourceService;

    /**
     * 分页查询报表数据源
     * <p>
     * 需要 report:datasource:view 权限
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ReportDatasourceParam
     * @see ReportDatasourceDTO
     */
    @Operation(summary = "分页查询报表数据源")
    @SaCheckPermission("report:datasource:view")
    @GetMapping("/page")
    public R<Page<ReportDatasourceDTO>> page(ReportDatasourceParam param) {
        return R.ok(datasourceService.pageByParam(param));
    }

    /**
     * 查询所有启用的数据源列表
     * <p>
     * 用于下拉框选择
     * </p>
     *
     * @return 数据源列表
     * @see ReportDatasourceDTO
     */
    @Operation(summary = "查询所有启用的数据源列表")
    @SaCheckPermission("report:datasource:view")
    @GetMapping("/list")
    public R<List<ReportDatasourceDTO>> list() {
        return R.ok(datasourceService.listEnabled());
    }

    /**
     * 根据 ID 获取数据源详情
     * <p>
     * 需要 report:datasource:view 权限
     * </p>
     *
     * @param id 数据源 ID
     * @return 数据源详情
     * @see ReportDatasourceDTO
     */
    @Operation(summary = "根据 ID 获取数据源详情")
    @SaCheckPermission("report:datasource:view")
    @GetMapping("/{id}")
    public R<ReportDatasourceDTO> getById(@PathVariable Long id) {
        return R.ok(datasourceService.getById(id));
    }

    /**
     * 根据编码获取数据源
     * <p>
     * 需要 report:datasource:view 权限
     * </p>
     *
     * @param code 数据源编码
     * @return 数据源详情
     * @see ReportDatasourceDTO
     */
    @Operation(summary = "根据编码获取数据源")
    @SaCheckPermission("report:datasource:view")
    @GetMapping("/by-code/{code}")
    public R<ReportDatasourceDTO> getByCode(@PathVariable String code) {
        return R.ok(datasourceService.getByCode(code));
    }

    /**
     * 保存数据源
     * <p>
     * 需要 report:datasource:add 或 report:datasource:edit 权限
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 操作结果
     * @see ReportDatasourceDTO
     */
    @Operation(summary = "保存数据源")
    @SaCheckPermission({"report:datasource:add", "report:datasource:edit"})
    @PostMapping("/save")
    @OperationLog(module = "report", menuPath = "/report/datasource", operationType = OperationType.UPDATE)
    public R<Void> save(@RequestBody ReportDatasourceDTO dto) {
        datasourceService.save(dto);
        return R.ok();
    }

    /**
     * 测试数据源连接
     * <p>
     * 需要 report:datasource:test 权限
     * </p>
     *
     * @param dto 数据源 DTO
     * @return 连接结果
     */
    @Operation(summary = "测试数据源连接")
    @SaCheckPermission("report:datasource:test")
    @PostMapping("/test")
    public R<Boolean> testConnection(@RequestBody ReportDatasourceDTO dto) {
        return R.ok(datasourceService.testConnection(dto));
    }

    /**
     * 批量删除数据源
     * <p>
     * 需要 report:datasource:delete 权限
     * </p>
     *
     * @param ids 数据源 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除数据源")
    @SaCheckPermission("report:datasource:delete")
    @DeleteMapping("/batch")
    @OperationLog(module = "report", menuPath = "/report/datasource", operationType = OperationType.DELETE)
    public R<Void> deleteByIds(@RequestBody List<Long> ids) {
        datasourceService.deleteByIds(ids);
        return R.ok();
    }
}
