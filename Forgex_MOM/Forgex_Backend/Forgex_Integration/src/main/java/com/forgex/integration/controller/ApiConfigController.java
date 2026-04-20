package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.security.perm.RequirePerm;
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
     * 返回接口配置的详细信息，包括请求方式、请求路径、超时时间等
     * </p>
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 分页结果，包含接口配置列表和总数
     * @see ApiConfigParam
     * @see ApiConfigDTO
     * @see com.forgex.integration.service.IApiConfigService#pageApiConfigs
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:view")
    @PostMapping("/page")
    @Operation(summary = "分页查询接口配置列表", description = "支持按接口编码、接口名称、状态、模块编码等条件查询")
    public R<Page<ApiConfigDTO>> pageApiConfigs(@RequestBody @Validated ApiConfigParam param) {
        Page<ApiConfigDTO> page = apiConfigService.pageApiConfigs(param);
        return R.ok(page);
    }

    /**
     * 查询接口配置列表（不分页）
     * <p>
     * 用于下拉框选择、数据关联等场景
     * 返回所有符合条件的接口配置
     * </p>
     *
     * @param param 查询参数，包含筛选条件
     * @return 接口配置列表
     * @see ApiConfigParam
     * @see ApiConfigDTO
     * @see com.forgex.integration.service.IApiConfigService#listApiConfigs
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:view")
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
     * 返回完整的接口配置信息，包括请求配置、响应配置、超时设置等
     * </p>
     *
     * @param id 配置 ID（必填）
     * @return 接口配置详情
     * @see ApiConfigDTO
     * @see com.forgex.integration.service.IApiConfigService#getApiConfigById
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:view")
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
     * 接口编码在同一模块下必须唯一
     * </p>
     *
     * @param dto 接口配置信息，包含接口编码、名称、请求方式等
     * @return 创建结果
     * @throws com.forgex.common.exception.BusinessException 当接口编码已存在时抛出
     * @see ApiConfigDTO
     * @see com.forgex.integration.service.IApiConfigService#createApiConfig
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:add")
    @PostMapping("/create")
    @Operation(summary = "创建接口配置", description = "新增接口配置信息")
    public R<ApiConfigDTO> createApiConfig(@RequestBody @Validated ApiConfigDTO dto) {
        return R.ok(apiConfigService.createApiConfig(dto));
    }

    /**
     * 更新接口配置
     * <p>
     * 自动校验接口编码唯一性（排除自身）
     * 支持更新接口配置的所有属性
     * </p>
     *
     * @param dto 接口配置信息，ID 必须存在
     * @return 更新结果
     * @throws com.forgex.common.exception.BusinessException 当接口配置不存在或与其他记录冲突时抛出
     * @see ApiConfigDTO
     * @see com.forgex.integration.service.IApiConfigService#updateApiConfig
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:edit")
    @PostMapping("/update")
    @Operation(summary = "更新接口配置", description = "修改接口配置信息")
    public R<ApiConfigDTO> updateApiConfig(@RequestBody @Validated ApiConfigDTO dto) {
        return R.ok(apiConfigService.updateApiConfig(dto));
    }

    /**
     * 删除接口配置
     * <p>
     * 逻辑删除，不会物理删除数据
     * 删除后该接口配置将无法使用
     * </p>
     *
     * @param id 配置 ID（必填）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当接口配置不存在时抛出
     * @see com.forgex.integration.service.IApiConfigService#deleteApiConfig
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:delete")
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
     * 事务保证：要么全部删除成功，要么全部失败回滚
     * </p>
     *
     * @param ids 配置 ID 列表（不能为空）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当批量删除失败时抛出
     * @see com.forgex.integration.service.IApiConfigService#batchDeleteApiConfigs
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:delete")
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
     * 启用后该接口可以正常调用
     * </p>
     *
     * @param id 配置 ID（必填）
     * @return 启用结果
     * @throws com.forgex.common.exception.BusinessException 当接口配置不存在时抛出
     * @see com.forgex.common.constant.SystemConstants#STATUS_NORMAL
     * @see com.forgex.integration.service.IApiConfigService#enableApiConfig
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:edit")
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
     * 停用后该接口将无法调用
     * </p>
     *
     * @param id 配置 ID（必填）
     * @return 停用结果
     * @throws com.forgex.common.exception.BusinessException 当接口配置不存在时抛出
     * @see com.forgex.common.constant.SystemConstants#STATUS_DISABLED
     * @see com.forgex.integration.service.IApiConfigService#disableApiConfig
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:edit")
    @PostMapping("/disable/{id}")
    @Operation(summary = "停用接口配置", description = "将接口配置状态设置为禁用")
    public R<Void> disableApiConfig(@PathVariable Long id) {
        apiConfigService.disableApiConfig(id);
        return R.ok();
    }
}
