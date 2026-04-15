package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ThirdSystemDTO;
import com.forgex.integration.domain.param.ThirdSystemParam;
import com.forgex.integration.service.IThirdSystemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 第三方系统管理控制器
 * <p>
 * 提供第三方系统的增删改查等 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/integration/third-system")
@RequiredArgsConstructor
@Tag(name = "第三方系统管理", description = "提供第三方系统的增删改查等功能")
public class ThirdSystemController {

    private final IThirdSystemService thirdSystemService;

    /**
     * 分页查询第三方系统列表
     * <p>
     * 支持按系统编码、系统名称、状态等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询第三方系统列表", description = "支持按系统编码、系统名称、状态等条件查询")
    public R<Page<ThirdSystemDTO>> pageThirdSystems(@RequestBody @Validated ThirdSystemParam param) {
        Page<ThirdSystemDTO> page = thirdSystemService.pageThirdSystems(param);
        return R.ok(page);
    }

    /**
     * 查询第三方系统列表（不分页）
     * <p>
     * 用于下拉框选择等场景
     * </p>
     *
     * @param param 查询参数
     * @return 系统列表
     */
    @PostMapping("/list")
    @Operation(summary = "查询第三方系统列表", description = "不分页查询，用于下拉框选择")
    public R<List<ThirdSystemDTO>> listThirdSystems(@RequestBody ThirdSystemParam param) {
        List<ThirdSystemDTO> list = thirdSystemService.listThirdSystems(param);
        return R.ok(list);
    }

    /**
     * 根据 ID 获取第三方系统详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 系统 ID
     * @return 系统详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取第三方系统详情", description = "根据 ID 查询系统详细信息")
    public R<ThirdSystemDTO> getThirdSystemDetail(@PathVariable Long id) {
        ThirdSystemDTO dto = thirdSystemService.getThirdSystemById(id);
        return R.ok(dto);
    }

    /**
     * 创建第三方系统
     * <p>
     * 自动校验系统编码唯一性
     * </p>
     *
     * @param dto 系统信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建第三方系统", description = "新增第三方系统信息")
    public R<Void> createThirdSystem(@RequestBody @Validated ThirdSystemDTO dto) {
        thirdSystemService.createThirdSystem(dto);
        return R.ok();
    }

    /**
     * 更新第三方系统
     * <p>
     * 自动校验系统编码唯一性（排除自身）
     * </p>
     *
     * @param dto 系统信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新第三方系统", description = "修改第三方系统信息")
    public R<Void> updateThirdSystem(@RequestBody @Validated ThirdSystemDTO dto) {
        thirdSystemService.updateThirdSystem(dto);
        return R.ok();
    }

    /**
     * 删除第三方系统
     * <p>
     * 逻辑删除，同时检查是否有关联的授权记录
     * </p>
     *
     * @param id 系统 ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除第三方系统", description = "逻辑删除第三方系统")
    public R<Void> deleteThirdSystem(@PathVariable Long id) {
        thirdSystemService.deleteThirdSystem(id);
        return R.ok();
    }

    /**
     * 批量删除第三方系统
     * <p>
     * 支持批量删除多个系统
     * </p>
     *
     * @param ids 系统 ID 列表
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除第三方系统", description = "批量删除多个第三方系统")
    public R<Void> batchDeleteThirdSystems(@RequestBody List<Long> ids) {
        thirdSystemService.batchDeleteThirdSystems(ids);
        return R.ok();
    }
}
