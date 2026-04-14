package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiConfigDTO;
import com.forgex.integration.domain.param.ApiConfigParam;
import com.forgex.integration.service.IApiConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 接口配置管理控制器
 * <p>
 * 提供接口配置的增删改查、启用/停用等 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/integration/api-config")
@RequiredArgsConstructor
@Tag(name = "接口配置管理", description = "提供接口配置的增删改查、启用/停用等功能")
public class ApiConfigController {

    private final IApiConfigService apiConfigService;

    /**
     * 分页查询接口配置列表
     * <p>
     * 支持按接口编码、接口名称、状态、模块编码等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     * @see ApiConfigParam
     * @see ApiConfigDTO
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询接口配置列表", description = "支持按接口编码、接口名称、状态、模块编码等条件查询")
    public R<Page<ApiConfigDTO>> pageApiConfigs(@RequestBody @Validated ApiConfigParam param) {
        Page<ApiConfigDTO> page = apiConfigService.pageApiConfigs(param);
        return R.ok(page);
    }

    /**
     * 查询接口配置列表（不分页）
     * <p>
     * 用于下拉框选择等场景
     * </p>
     *
     * @param param 查询参数
     * @return 接口配置列表
     * @see ApiConfigParam
     * @see ApiConfigDTO
     */
    @PostMapping("/list")
    @Operation(summary = "查询接口配置列表", description = "不分页查询，用于下拉框选择")
    public R<List<ApiConfigDTO>> listApiConfigs(@RequestBody ApiConfigParam param) {
        List<ApiConfigDTO> list = apiConfigService.listApiConfigs(param);
        return R.ok(list);
    }

    /**
     * 根据 ID 获取接口配置详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 配置 ID
     * @return 接口配置详情
     * @see ApiConfigDTO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取接口配置详情", description = "根据 ID 查询接口配置详细信息")
    public R<ApiConfigDTO> getApiConfigDetail(@PathVariable Long id) {
        ApiConfigDTO dto = apiConfigService.getApiConfigById(id);
        return R.ok(dto);
    }

    /**
     * 创建接口配置
     * <p>
     * 自动校验接口编码唯一性
     * </p>
     *
     * @param dto 接口配置信息
     * @return 创建结果
     * @see ApiConfigDTO
     */
    @PostMapping("/create")
    @Operation(summary = "创建接口配置", description = "新增接口配置信息")
    public R<Void> createApiConfig(@RequestBody @Validated ApiConfigDTO dto) {
        apiConfigService.createApiConfig(dto);
        return R.ok();
    }

    /**
     * 更新接口配置
     * <p>
     * 自动校验接口编码唯一性（排除自身）
     * </p>
     *
     * @param dto 接口配置信息
     * @return 更新结果
     * @see ApiConfigDTO
     */
    @PostMapping("/update")
    @Operation(summary = "更新接口配置", description = "修改接口配置信息")
    public R<Void> updateApiConfig(@RequestBody @Validated ApiConfigDTO dto) {
        apiConfigService.updateApiConfig(dto);
        return R.ok();
    }

    /**
     * 删除接口配置
     * <p>
     * 逻辑删除
     * </p>
     *
     * @param id 配置 ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除接口配置", description = "逻辑删除接口配置")
    public R<Void> deleteApiConfig(@PathVariable Long id) {
        apiConfigService.deleteApiConfig(id);
        return R.ok();
    }

    /**
     * 批量删除接口配置
     * <p>
     * 支持批量删除多个配置
     * </p>
     *
     * @param ids 配置 ID 列表
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除接口配置", description = "批量删除多个接口配置")
    public R<Void> batchDeleteApiConfigs(@RequestBody List<Long> ids) {
        apiConfigService.batchDeleteApiConfigs(ids);
        return R.ok();
    }

    /**
     * 启用接口配置
     * <p>
     * 将接口配置状态设置为启用
     * </p>
     *
     * @param id 配置 ID
     * @return 启用结果
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     */
    @PostMapping("/enable/{id}")
    @Operation(summary = "启用接口配置", description = "将接口配置状态设置为启用")
    public R<Void> enableApiConfig(@PathVariable Long id) {
        apiConfigService.enableApiConfig(id);
        return R.ok();
    }

    /**
     * 停用接口配置
     * <p>
     * 将接口配置状态设置为禁用
     * </p>
     *
     * @param id 配置 ID
     * @return 停用结果
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     */
    @PostMapping("/disable/{id}")
    @Operation(summary = "停用接口配置", description = "将接口配置状态设置为禁用")
    public R<Void> disableApiConfig(@PathVariable Long id) {
        apiConfigService.disableApiConfig(id);
        return R.ok();
    }
}
