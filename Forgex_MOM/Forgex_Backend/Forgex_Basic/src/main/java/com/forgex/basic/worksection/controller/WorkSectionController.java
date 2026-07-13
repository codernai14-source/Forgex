package com.forgex.basic.worksection.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.worksection.domain.dto.WorkSectionDTO;
import com.forgex.basic.worksection.domain.entity.BasicWorkSection;
import com.forgex.basic.worksection.domain.param.WorkSectionPageParam;
import com.forgex.basic.worksection.service.IWorkSectionService;
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
 * 工段主数据控制器。
 * <p>
 * 提供工段的分页查询、列表查询、车间/产线级联下拉、详情查询、新增、修改、删除和批量删除接口。
 * 接口路径前缀 {@code /basic/workSection}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Tag(name = "工段主数据", description = "工段主数据管理接口")
@RestController
@RequestMapping("/basic/workSection")
@RequiredArgsConstructor
public class WorkSectionController {

    private final IWorkSectionService workSectionService;

    /**
     * 分页查询工段。
     *
     * @param param 分页查询参数
     * @return 工段分页结果
     */
    @Operation(summary = "分页查询工段")
    @RequirePerm("basic:workSection:query")
    @PostMapping("/page")
    public R<Page<WorkSectionDTO>> page(@RequestBody(required = false) WorkSectionPageParam param) {
        return R.ok(workSectionService.page(param));
    }

    /**
     * 查询工段列表（不分页）。
     *
     * @param param 查询参数
     * @return 工段列表
     */
    @Operation(summary = "查询工段列表")
    @PostMapping("/list")
    public R<List<WorkSectionDTO>> list(@RequestBody(required = false) WorkSectionPageParam param) {
        return R.ok(workSectionService.list(param));
    }

    /**
     * 根据车间 ID 查询工段列表（下拉数据源）。
     *
     * @param params 请求参数，键：workshopId
     * @return 工段列表
     */
    @Operation(summary = "按车间查询工段")
    @PostMapping("/listByWorkshop")
    public R<List<WorkSectionDTO>> listByWorkshop(@RequestBody Map<String, Object> params) {
        return R.ok(workSectionService.listByWorkshop(toLong(params.get("workshopId"))));
    }

    /**
     * 根据产线 ID 查询工段列表（下拉数据源）。
     *
     * @param params 请求参数，键：productionLineId
     * @return 工段列表
     */
    @Operation(summary = "按产线查询工段")
    @PostMapping("/listByProductionLine")
    public R<List<WorkSectionDTO>> listByProductionLine(@RequestBody Map<String, Object> params) {
        return R.ok(workSectionService.listByProductionLine(toLong(params.get("productionLineId"))));
    }

    /**
     * 获取工段详情。
     *
     * @param params 请求参数，键：id
     * @return 工段详情
     */
    @Operation(summary = "获取工段详情")
    @RequirePerm("basic:workSection:query")
    @PostMapping("/detail")
    public R<WorkSectionDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(workSectionService.getDetailById(toLong(params.get("id"))));
    }

    /**
     * 新增工段。
     *
     * @param param 工段实体参数
     * @return 新增工段的主键 ID
     */
    @Operation(summary = "新增工段")
    @RequirePerm("basic:workSection:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicWorkSection param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, workSectionService.create(param));
    }

    /**
     * 修改工段。
     *
     * @param param 工段实体参数
     * @return 是否处理成功
     */
    @Operation(summary = "修改工段")
    @RequirePerm("basic:workSection:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody BasicWorkSection param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, workSectionService.update(param));
    }

    /**
     * 删除工段。
     *
     * @param params 请求参数，键：id
     * @return 是否处理成功
     */
    @Operation(summary = "删除工段")
    @RequirePerm("basic:workSection:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, workSectionService.delete(toLong(params.get("id"))));
    }

    /**
     * 批量删除工段。
     *
     * @param params 请求参数，键：ids
     * @return 是否处理成功
     */
    @Operation(summary = "批量删除工段")
    @RequirePerm("basic:workSection:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, workSectionService.batchDelete(params.get("ids")));
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
