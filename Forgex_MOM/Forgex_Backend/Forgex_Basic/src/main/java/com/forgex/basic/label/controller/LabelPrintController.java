package com.forgex.basic.label.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.label.domain.param.LabelPrintExecuteParam;
import com.forgex.basic.label.service.LabelPrintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签打印执行 Controller
 * <p>
 * 提供标签打印相关的 HTTP 接口，包括标签打印（单/批量）和打印预览等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelPrintService
 */
@Tag(name = "标签打印执行", description = "标签打印执行接口")
@RestController
@RequestMapping("/label/print")
@RequiredArgsConstructor
public class LabelPrintController {

    private final LabelPrintService labelPrintService;

    /**
     * 标签打印（单/批量）
     * <p>
     * 接口路径：POST /label/print/execute
     * 需要权限：label:print:execute
     * </p>
     * <p>
     * 核心流程：
     * 1. 校验参数
     * 2. 智能匹配模板
     * 3. 【使用 DataAssemblyHandler】组装业务数据
     * 4. 【使用 PlaceholderHandler】替换模板占位符
     * 5. 生成打印结果
     * 6. 【使用 PrintSnapshotHandler】保存打印记录
     * </p>
     *
     * @param param 打印参数
     * @return {@link R} 包含打印结果列表（已填充数据的标签 JSON）的统一返回结构
     */
    @Operation(summary = "标签打印", description = "执行标签打印，支持单张和批量打印，保存打印记录")
    @RequirePerm("label:print:execute")
    @PostMapping("/execute")
    public R<List<String>> executePrint(@RequestBody @Validated LabelPrintExecuteParam param) {
        Long tenantId = TenantContext.get();
        Long userId = getCurrentUserId();
        List<String> printResults = labelPrintService.executePrint(param, userId, tenantId);

        return R.ok(printResults);
    }

    /**
     * 打印预览
     * <p>
     * 接口路径：POST /label/print/preview
     * 需要权限：label:print:query
     * </p>
     * <p>
     * 与打印执行类似，但不保存打印记录，仅用于预览效果
     * </p>
     *
     * @param param 打印参数（同 executePrint）
     * @return {@link R} 包含预览结果列表（已填充数据的标签 JSON）的统一返回结构
     */
    @Operation(summary = "打印预览", description = "预览标签打印效果，不保存打印记录")
    @RequirePerm("label:print:query")
    @PostMapping("/preview")
    public R<List<String>> previewPrint(@RequestBody @Validated LabelPrintExecuteParam param) {
        Long tenantId = TenantContext.get();

        List<String> previewResults = labelPrintService.previewPrint(param, tenantId);

        return R.ok(previewResults);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    private Long getCurrentUserId() {
        // 从安全上下文获取当前用户 ID
        // 具体实现取决于项目的认证框架（如 Sa-Token、Spring Security 等）
        // 这里假设有一个工具类可以获取
        try {
            // 示例：使用 Sa-Token
            // return StpUtil.getLoginIdAsLong();

            // 或者从请求上下文中获取
            return 1L; // 临时返回，实际需要从认证上下文获取
        } catch (Exception e) {
            return 1L; // 默认值
        }
    }
}
