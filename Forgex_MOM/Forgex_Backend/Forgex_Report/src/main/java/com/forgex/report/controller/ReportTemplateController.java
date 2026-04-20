package com.forgex.report.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.web.R;
import com.forgex.report.domain.dto.ReportTemplateDTO;
import com.forgex.report.domain.param.ReportTemplateParam;
import com.forgex.report.service.IReportTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报表模板管理控制器
 * <p>
 * 提供报表模板的 RESTful API 接口
 * 支持 UReport2 和 JimuReport 双引擎
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-09
 * @see IReportTemplateService
 */
@Tag(name = "报表模板管理", description = "UReport2/JimuReport 报表模板管理接口")
@RestController
@RequestMapping("/api/report/template")
@RequiredArgsConstructor
public class ReportTemplateController {

    private final IReportTemplateService templateService;

    /**
     * 分页查询报表模板
     * <p>
     * 需要 report:template:view 权限
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ReportTemplateParam
     * @see ReportTemplateDTO
     */
    @Operation(summary = "分页查询报表模板")
    @SaCheckPermission("report:template:view")
    @GetMapping("/page")
    public R<Page<ReportTemplateDTO>> page(ReportTemplateParam param) {
        return R.ok(templateService.pageByParam(param));
    }

    /**
     * 根据 ID 获取模板详情
     * <p>
     * 需要 report:template:view 权限
     * </p>
     *
     * @param id 模板 ID
     * @return 模板详情
     * @see ReportTemplateDTO
     */
    @Operation(summary = "根据 ID 获取模板详情")
    @SaCheckPermission("report:template:view")
    @GetMapping("/{id}")
    public R<ReportTemplateDTO> getById(@PathVariable Long id) {
        return R.ok(templateService.getById(id));
    }

    /**
     * 根据编码获取模板
     * <p>
     * 需要 report:template:view 权限
     * </p>
     *
     * @param code 模板编码
     * @param engineType 引擎类型
     * @return 模板详情
     * @see ReportTemplateDTO
     */
    @Operation(summary = "根据编码获取模板")
    @SaCheckPermission("report:template:view")
    @GetMapping("/by-code/{code}")
    public R<ReportTemplateDTO> getByCode(@PathVariable String code, 
                                          @RequestParam(required = false) String engineType) {
        return R.ok(templateService.getByCode(code, engineType));
    }

    /**
     * 保存模板
     * <p>
     * 需要 report:template:add 或 report:template:edit 权限
     * </p>
     *
     * @param dto 模板 DTO
     * @return 操作结果
     * @see ReportTemplateDTO
     */
    @Operation(summary = "保存模板")
    @SaCheckPermission({"report:template:add", "report:template:edit"})
    @PostMapping("/save")
    @OperationLog(module = "report", menuPath = "/report/template", operationType = OperationType.UPDATE)
    public R<Void> save(@RequestBody ReportTemplateDTO dto) {
        templateService.save(dto);
        return R.ok();
    }

    /**
     * 更新模板内容
     * <p>
     * 用于报表设计器保存时调用
     * 需要 report:template:design 权限
     * </p>
     *
     * @param id 模板 ID
     * @param content 模板内容
     * @return 操作结果
     */
    @Operation(summary = "更新模板内容")
    @SaCheckPermission("report:template:design")
    @PostMapping("/update-content")
    @OperationLog(module = "report", menuPath = "/report/template", operationType = OperationType.UPDATE)
    public R<Void> updateContent(@RequestParam Long id, @RequestBody String content) {
        templateService.updateContent(id, content);
        return R.ok();
    }

    /**
     * 导出模板文件
     * <p>
     * 需要 report:template:export 权限
     * </p>
     *
     * @param id 模板 ID
     * @return 文件路径
     */
    @Operation(summary = "导出模板文件")
    @SaCheckPermission("report:template:export")
    @PostMapping("/export/{id}")
    public R<String> exportTemplate(@PathVariable Long id) {
        return R.ok(templateService.exportTemplate(id));
    }

    /**
     * 导入模板文件
     * <p>
     * 需要 report:template:import 权限
     * </p>
     *
     * @param filePath 文件路径
     * @param engineType 引擎类型
     * @return 导入后的模板 DTO
     */
    @Operation(summary = "导入模板文件")
    @SaCheckPermission("report:template:import")
    @PostMapping("/import")
    @OperationLog(module = "report", menuPath = "/report/template", operationType = OperationType.UPLOAD)
    public R<ReportTemplateDTO> importTemplate(@RequestParam String filePath, 
                                               @RequestParam String engineType) {
        return R.ok(templateService.importTemplate(filePath, engineType));
    }

    /**
     * 批量删除模板
     * <p>
     * 需要 report:template:delete 权限
     * </p>
     *
     * @param ids 模板 ID 列表
     * @return 操作结果
     */
    @Operation(summary = "批量删除模板")
    @SaCheckPermission("report:template:delete")
    @DeleteMapping("/batch")
    @OperationLog(module = "report", menuPath = "/report/template", operationType = OperationType.DELETE)
    public R<Void> deleteByIds(@RequestBody List<Long> ids) {
        templateService.deleteByIds(ids);
        return R.ok();
    }
}
