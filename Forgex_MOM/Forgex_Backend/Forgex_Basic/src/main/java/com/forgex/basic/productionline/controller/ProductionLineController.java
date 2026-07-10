package com.forgex.basic.productionline.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.productionline.domain.dto.ProductionLineDTO;
import com.forgex.basic.productionline.domain.entity.BasicProductionLine;
import com.forgex.basic.productionline.domain.param.ProductionLinePageParam;
import com.forgex.basic.productionline.service.IProductionLineService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 产线主数据控制器。
 * <p>
 * 提供产线的分页查询、列表查询、车间级联下拉、详情查询、新增、修改、删除和批量删除接口。
 * 接口路径前缀 {@code /basic/productionLine}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Tag(name = "产线主数据", description = "产线主数据管理接口")
@RestController
@RequestMapping("/basic/productionLine")
@RequiredArgsConstructor
public class ProductionLineController {

    private final IProductionLineService productionLineService;

    /**
     * 分页查询产线。
     *
     * @param param 分页查询参数
     * @return 产线分页结果
     */
    @Operation(summary = "分页查询产线")
    @RequirePerm("basic:productionLine:query")
    @PostMapping("/page")
    public R<Page<ProductionLineDTO>> page(@RequestBody(required = false) ProductionLinePageParam param) {
        return R.ok(productionLineService.page(param));
    }

    /**
     * 查询产线列表（不分页）。
     *
     * @param param 查询参数
     * @return 产线列表
     */
    @Operation(summary = "查询产线列表")
    @PostMapping("/list")
    public R<List<ProductionLineDTO>> list(@RequestBody(required = false) ProductionLinePageParam param) {
        return R.ok(productionLineService.list(param));
    }

    /**
     * 根据车间 ID 查询产线列表（下拉数据源）。
     *
     * @param params 请求参数，键：workshopId
     * @return 产线列表
     */
    @Operation(summary = "按车间查询产线")
    @PostMapping("/listByWorkshop")
    public R<List<ProductionLineDTO>> listByWorkshop(@RequestBody Map<String, Object> params) {
        Long workshopId = toLong(params.get("workshopId"));
        return R.ok(productionLineService.listByWorkshop(workshopId));
    }

    /**
     * 获取产线详情。
     *
     * @param params 请求参数，键：id
     * @return 产线详情
     */
    @Operation(summary = "获取产线详情")
    @RequirePerm("basic:productionLine:query")
    @PostMapping("/detail")
    public R<ProductionLineDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(productionLineService.getDetailById(toLong(params.get("id"))));
    }

    /**
     * 新增产线。
     *
     * @param param 产线实体参数
     * @return 新增产线的主键 ID
     */
    @Operation(summary = "新增产线")
    @RequirePerm("basic:productionLine:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicProductionLine param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, productionLineService.create(param));
    }

    /**
     * 修改产线。
     *
     * @param param 产线实体参数
     * @return 是否处理成功
     */
    @Operation(summary = "修改产线")
    @RequirePerm("basic:productionLine:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody BasicProductionLine param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, productionLineService.update(param));
    }

    /**
     * 删除产线。
     *
     * @param params 请求参数，键：id
     * @return 是否处理成功
     */
    @Operation(summary = "删除产线")
    @RequirePerm("basic:productionLine:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, productionLineService.delete(toLong(params.get("id"))));
    }

    /**
     * 批量删除产线。
     *
     * @param params 请求参数，键：ids
     * @return 是否处理成功
     */
    @Operation(summary = "批量删除产线")
    @RequirePerm("basic:productionLine:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, productionLineService.batchDelete(params.get("ids")));
    }

    /**
     * 将任意对象安全转换为 Long。
     */
    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        return Long.valueOf(String.valueOf(value));
    }
}
