package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.label.domain.param.IdParam;
import com.forgex.basic.label.domain.param.LabelReprintParam;
import com.forgex.basic.label.domain.param.PrintRecordQueryParam;
import com.forgex.basic.label.domain.vo.PrintRecordVO;
import com.forgex.basic.label.service.LabelPrintService;
import com.forgex.basic.label.service.LabelRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 标签打印记录与补打 Controller
 * <p>
 * 提供标签打印记录查询、详情查看以及标签补打等操作的 HTTP 接口。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelRecordService
 * @see LabelPrintService
 */
@Tag(name = "标签打印记录与补打", description = "标签打印记录查询与补打接口")
@RestController
@RequestMapping("/label/record")
@RequiredArgsConstructor
public class LabelRecordController {

    private final LabelRecordService labelRecordService;
    private final LabelPrintService labelPrintService;

    /**
     * 分页查询打印记录列表
     * <p>
     * 接口路径：POST /label/record/page
     * 需要权限：label:record:query
     * </p>
     *
     * @param param 查询参数
     * @return {@link R} 包含打印记录分页列表的统一返回结构
     */
    @Operation(summary = "分页查询打印记录列表", description = "支持多条件筛选，包括时间范围、物料、批次等")
    @RequirePerm("label:record:query")
    @PostMapping("/page")
    public R<IPage<PrintRecordVO>> page(@RequestBody @Validated PrintRecordQueryParam param) {
        Long tenantId = TenantContext.get();
        IPage<PrintRecordVO> page = labelRecordService.pageRecords(param, tenantId);
        return R.ok(page);
    }

    /**
     * 查询打印记录详情
     * <p>
     * 接口路径：POST /label/record/detail
     * 需要权限：label:record:query
     * </p>
     *
     * @param body 请求体参数，包含 id
     * @return {@link R} 包含打印记录详情的统一返回结构
     */
    @Operation(summary = "查询打印记录详情", description = "根据 ID 查询打印记录详细信息，包含数据快照")
    @RequirePerm("label:record:query")
    @PostMapping("/detail")
    public R<PrintRecordVO> detail(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        PrintRecordVO vo = labelRecordService.getRecordDetail(id, tenantId);
        return R.ok(vo);
    }

    /**
     * 标签补打
     * <p>
     * 接口路径：POST /label/print/reprint
     * 需要权限：label:print:reprint
     * </p>
     * <p>
     * 核心特性：
     * 1. 基于历史打印记录的快照原样还原
     * 2. 不重新查询业务数据，确保补打内容与首次打印一致
     * 3. 记录补打次数和补打原因
     * 4. 生成新的打印流水号
     * </p>
     *
     * @param param 补打参数
     * @return {@link R} 包含补打结果列表的统一返回结构
     */
    @Operation(summary = "标签补打", description = "基于历史快照进行标签补打，确保内容一致性")
    @RequirePerm("label:print:reprint")
    @PostMapping("/reprint")
    public R<List<String>> reprint(@RequestBody @Validated LabelReprintParam param) {
        Long tenantId = TenantContext.get();
        Long userId = getCurrentUserId();

        List<String> reprintResults = labelPrintService.reprintLabel(
                param.getRecordId(),
                param.getReprintCount(),
                userId,
                tenantId
        );

        return R.ok(reprintResults);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    private Long getCurrentUserId() {
        // 从安全上下文获取当前用户 ID
        try {
            // 示例：使用 Sa-Token
            // return StpUtil.getLoginIdAsLong();
            return 1L; // 临时返回，实际需要从认证上下文获取
        } catch (Exception e) {
            return 1L;
        }
    }
}

