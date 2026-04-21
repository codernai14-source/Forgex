package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.vo.BarcodeVO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.label.domain.dto.LabelBarcodeDTO;
import com.forgex.basic.label.domain.param.BarcodeGenerateParam;
import com.forgex.basic.label.domain.param.BarcodeQueryParam;
import com.forgex.basic.label.service.LabelBarcodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 标签条码管理 Controller
 * <p>
 * 提供标签条码管理的 HTTP 接口，包括条码的分页查询、反查（扫码查询）以及条码生成等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelBarcodeService
 */
@Tag(name = "标签条码管理", description = "标签条码管理接口")
@RestController
@RequestMapping("/label/barcode")
@RequiredArgsConstructor
public class LabelBarcodeController {

    private final LabelBarcodeService labelBarcodeService;

    /**
     * 分页查询条码列表
     * <p>
     * 接口路径：POST /label/barcode/page
     * 需要权限：label:barcode:query
     * </p>
     *
     * @param param 查询参数
     * @return {@link R} 包含条码分页列表的统一返回结构
     */
    @Operation(summary = "分页查询条码列表", description = "支持多条件筛选，包括条码号、物料、业务场景等")
    @RequirePerm("label:barcode:query")
    @PostMapping("/page")
    public R<IPage<BarcodeVO>> page(@RequestBody @Validated BarcodeQueryParam param) {
        Long tenantId = TenantContext.get();
        IPage<BarcodeVO> page = labelBarcodeService.pageBarcodes(param, tenantId);
        return R.ok(page);
    }

    /**
     * 条码反查（扫码查询）
     * <p>
     * 接口路径：POST /label/barcode/queryByBarcode
     * 需要权限：label:barcode:query
     * </p>
     * <p>
     * 核心应用场景：
     * 1. 扫描条码后查询完整信息
     * 2. 追溯条码关联的业务数据
     * 3. 验证条码有效性
     * </p>
     *
     * @param body 请求体参数，包含 barcodeNo
     * @return {@link R} 包含条码详细信息的统一返回结构
     */
    @Operation(summary = "条码反查", description = "根据条码号查询完整信息，支持扫码追溯")
    @RequirePerm("label:barcode:query")
    @PostMapping("/queryByBarcode")
    public R<BarcodeVO> queryByBarcode(@RequestBody Map<String, String> body) {
        String barcodeNo = body.get("barcodeNo");
        if (barcodeNo == null || barcodeNo.trim().isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        BarcodeVO vo = labelBarcodeService.getByBarcodeNo(barcodeNo, tenantId);

        return R.ok(vo);

    }

    /**
     * 条码生成
     * <p>
     * 接口路径：POST /label/barcode/generate
     * 需要权限：label:barcode:add
     * </p>
     * <p>
     * 核心流程：
     * 1. 校验参数合法性
     * 2. 生成唯一条码号（支持多种编码规则）
     * 3. 保存条码记录
     * 4. 返回生成的条码信息
     * </p>
     *
     * @param param 条码生成参数
     * @return {@link R} 包含生成的条码信息的统一返回结构
     */
    @Operation(summary = "条码生成", description = "根据业务规则生成唯一条码号并保存记录")
    @RequirePerm("label:barcode:add")
    @PostMapping("/generate")
    public R<LabelBarcodeDTO> generateBarcode(@RequestBody @Validated BarcodeGenerateParam param) {
        Long tenantId = TenantContext.get();
        Long userId = getCurrentUserId();

        LabelBarcodeDTO dto = labelBarcodeService.generateBarcode(param, userId, tenantId);
        return R.ok(dto);
    }

    /**
     * 获取当前用户 ID
     *
     * @return 用户 ID
     */
    private Long getCurrentUserId() {
        // 从安全上下文获取当前用户 ID
        try {
            return 1L; // 临时返回，实际需要从认证上下文获取
        } catch (Exception e) {
            return 1L;
        }
    }
}

