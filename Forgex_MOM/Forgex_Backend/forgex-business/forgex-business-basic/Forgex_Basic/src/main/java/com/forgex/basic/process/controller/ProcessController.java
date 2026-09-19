package com.forgex.basic.process.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.process.domain.dto.ProcessDTO;
import com.forgex.basic.process.domain.entity.BasicProcess;
import com.forgex.basic.process.domain.param.ProcessPageParam;
import com.forgex.basic.process.service.IProcessService;
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
 * 工序主数据控制器。
 * <p>
 * 提供工序的分页查询、列表查询、工段级联下拉、详情查询、新增、修改、删除和批量删除接口。
 * 接口路径前缀 {@code /basic/process}。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-06-19
 */
@Tag(name = "工序主数据", description = "工序主数据管理接口")
@RestController
@RequestMapping("/basic/process")
@RequiredArgsConstructor
public class ProcessController {

    private final IProcessService processService;

    /**
     * 分页查询工序。
     *
     * @param param 分页查询参数
     * @return 工序分页结果
     */
    @Operation(summary = "分页查询工序")
    @RequirePerm("basic:process:query")
    @PostMapping("/page")
    public R<Page<ProcessDTO>> page(@RequestBody(required = false) ProcessPageParam param) {
        return R.ok(processService.page(param));
    }

    /**
     * 查询工序列表（不分页）。
     *
     * @param param 查询参数
     * @return 工序列表
     */
    @Operation(summary = "查询工序列表")
    @PostMapping("/list")
    public R<List<ProcessDTO>> list(@RequestBody(required = false) ProcessPageParam param) {
        return R.ok(processService.list(param));
    }

    /**
     * 根据工段 ID 查询工序列表（下拉数据源）。
     *
     * @param params 请求参数，键：workSectionId
     * @return 工序列表
     */
    @Operation(summary = "按工段查询工序")
    @PostMapping("/listByWorkSection")
    public R<List<ProcessDTO>> listByWorkSection(@RequestBody Map<String, Object> params) {
        return R.ok(processService.listByWorkSection(toLong(params.get("workSectionId"))));
    }

    /**
     * 获取工序详情。
     *
     * @param params 请求参数，键：id
     * @return 工序详情
     */
    @Operation(summary = "获取工序详情")
    @RequirePerm("basic:process:query")
    @PostMapping("/detail")
    public R<ProcessDTO> detail(@RequestBody Map<String, Object> params) {
        return R.ok(processService.getDetailById(toLong(params.get("id"))));
    }

    /**
     * 新增工序。
     *
     * @param param 工序实体参数
     * @return 新增工序的主键 ID
     */
    @Operation(summary = "新增工序")
    @RequirePerm("basic:process:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicProcess param) {
        return R.ok(CommonPrompt.CREATE_SUCCESS, processService.create(param));
    }

    /**
     * 修改工序。
     *
     * @param param 工序实体参数
     * @return 是否处理成功
     */
    @Operation(summary = "修改工序")
    @RequirePerm("basic:process:edit")
    @PostMapping("/update")
    public R<Boolean> update(@RequestBody BasicProcess param) {
        return R.ok(CommonPrompt.UPDATE_SUCCESS, processService.update(param));
    }

    /**
     * 删除工序。
     *
     * @param params 请求参数，键：id
     * @return 是否处理成功
     */
    @Operation(summary = "删除工序")
    @RequirePerm("basic:process:delete")
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, processService.delete(toLong(params.get("id"))));
    }

    /**
     * 批量删除工序。
     *
     * @param params 请求参数，键：ids
     * @return 是否处理成功
     */
    @Operation(summary = "批量删除工序")
    @RequirePerm("basic:process:batchDelete")
    @PostMapping("/batchDelete")
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> params) {
        return R.ok(CommonPrompt.DELETE_SUCCESS, processService.batchDelete(params.get("ids")));
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
