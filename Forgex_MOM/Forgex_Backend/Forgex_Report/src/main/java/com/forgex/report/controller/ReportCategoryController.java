package com.forgex.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.web.R;
import com.forgex.report.domain.dto.ReportCategoryDTO;
import com.forgex.report.domain.param.ReportCategoryParam;
import com.forgex.report.service.IReportCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表分类管理控制器
 * <p>
 * 提供报表分类的 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IReportCategoryService
 */
@Tag(name = "报表分类管理", description = "报表分类管理接口")
@RestController
@RequestMapping("/api/report/category")
@RequiredArgsConstructor
public class ReportCategoryController {

    private final IReportCategoryService categoryService;

    /**
     * 分页查询报表分类
     * <p>
     * 需要 report:category:view 权限
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ReportCategoryParam
     * @see ReportCategoryDTO
     */
    @Operation(summary = "分页查询报表分类")
    @SaCheckPermission("report:category:view")
    @GetMapping("/page")
    public R<Page<ReportCategoryDTO>> page(ReportCategoryParam param) {
        return R.ok(categoryService.pageByParam(param));
    }

    /**
     * 查询所有分类列表
     * <p>
     * 用于下拉框选择
     * </p>
     *
     * @return 分类列表
     * @see ReportCategoryDTO
     */
    @Operation(summary = "查询所有分类列表")
    @SaCheckPermission("report:category:view")
    @GetMapping("/list")
    public R<List<ReportCategoryDTO>> list() {
        return R.ok(categoryService.listAll());
    }

    /**
     * 根据 ID 获取分类详情
     * <p>
     * 需要 report:category:view 权限
     * </p>
     *
     * @param id 分类 ID
     * @return 分类详情
     * @see ReportCategoryDTO
     */
    @Operation(summary = "根据 ID 获取分类详情")
    @SaCheckPermission("report:category:view")
    @GetMapping("/{id}")
    public R<ReportCategoryDTO> getById(@PathVariable Long id) {
        return R.ok(categoryService.getById(id));
    }

    /**
     * 保存分类
     * <p>
     * 需要 report:category:add 或 report:category:edit 权限
     * </p>
     *
     * @param dto 分类 DTO
     * @return 操作结果
     * @see ReportCategoryDTO
     */
    @Operation(summary = "保存分类")
    @SaCheckPermission({"report:category:add", "report:category:edit"})
    @PostMapping("/save")
    @OperationLog(module = "report", menuPath = "/report/category", operationType = OperationType.UPDATE)
    public R<Void> save(@RequestBody ReportCategoryDTO dto) {
        categoryService.save(dto);
        return R.ok();
    }

    /**
     * 批量删除分类
     * <p>
     * 需要 report:category:delete 权限
     * </p>
     *
     * @param ids 分类 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除分类")
    @SaCheckPermission("report:category:delete")
    @DeleteMapping("/batch")
    @OperationLog(module = "report", menuPath = "/report/category", operationType = OperationType.DELETE)
    public R<Void> deleteByIds(@RequestBody List<Long> ids) {
        categoryService.deleteByIds(ids);
        return R.ok();
    }
}
