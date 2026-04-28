package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.forgex.common.security.perm.RequirePerm;
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
     * 返回第三方系统的详细信息，包括系统编码、名称、描述等
     * </p>
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 分页结果，包含第三方系统列表和总数
     * @see ThirdSystemParam
     * @see ThirdSystemDTO
     * @see com.forgex.integration.service.IThirdSystemService#pageThirdSystems
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:view")
    @PostMapping("/page")
    @Operation(summary = "分页查询第三方系统列表", description = "支持按系统编码、系统名称、状态等条件查询")
    public R<Page<ThirdSystemDTO>> pageThirdSystems(@RequestBody @Validated ThirdSystemParam param) {
        Page<ThirdSystemDTO> page = thirdSystemService.pageThirdSystems(param);
        return R.ok(page);
    }

    /**
     * 查询第三方系统列表（不分页）
     * <p>
     * 用于下拉框选择、数据关联等场景
     * 返回所有符合条件的第三方系统
     * </p>
     *
     * @param param 查询参数，包含筛选条件
     * @return 系统列表
     * @see ThirdSystemParam
     * @see ThirdSystemDTO
     * @see com.forgex.integration.service.IThirdSystemService#listThirdSystems
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:view")
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
     * 返回第三方系统的完整信息，包括系统编码、名称、描述、回调地址等
     * </p>
     *
     * @param id 系统 ID（必填）
     * @return 系统详情
     * @see ThirdSystemDTO
     * @see com.forgex.integration.service.IThirdSystemService#getThirdSystemById
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:view")
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
     * 系统编码在整个平台中必须唯一
     * </p>
     *
     * @param dto 系统信息，包含系统编码、名称、描述等
     * @return 创建结果
     * @throws com.forgex.common.exception.I18nBusinessException 当系统编码已存在时抛出
     * @see ThirdSystemDTO
     * @see com.forgex.integration.service.IThirdSystemService#createThirdSystem
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:add")
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
     * 支持更新第三方系统的所有属性
     * </p>
     *
     * @param dto 系统信息，ID 必须存在
     * @return 更新结果
     * @throws com.forgex.common.exception.I18nBusinessException 当系统不存在或与其他记录冲突时抛出
     * @see ThirdSystemDTO
     * @see com.forgex.integration.service.IThirdSystemService#updateThirdSystem
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:edit")
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
     * 如果存在关联的授权记录，则不允许删除
     * </p>
     *
     * @param id 系统 ID（必填）
     * @return 删除结果
     * @throws com.forgex.common.exception.I18nBusinessException 当系统不存在或存在关联授权时抛出
     * @see com.forgex.integration.service.IThirdSystemService#deleteThirdSystem
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:delete")
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
     * 事务保证：要么全部删除成功，要么全部失败回滚
     * 如果任一系统存在关联的授权记录，则全部删除失败
     * </p>
     *
     * @param ids 系统 ID 列表（不能为空）
     * @return 删除结果
     * @throws com.forgex.common.exception.I18nBusinessException 当批量删除失败或存在关联授权时抛出
     * @see com.forgex.integration.service.IThirdSystemService#batchDeleteThirdSystems
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:third-system:batch-delete")
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除第三方系统", description = "批量删除多个第三方系统")
    public R<Void> batchDeleteThirdSystems(@RequestBody List<Long> ids) {
        thirdSystemService.batchDeleteThirdSystems(ids);
        return R.ok();
    }
}
