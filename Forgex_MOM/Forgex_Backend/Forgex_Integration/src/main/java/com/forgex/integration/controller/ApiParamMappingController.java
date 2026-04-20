package com.forgex.integration.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.param.ApiParamMappingBatchSaveParam;
import com.forgex.integration.domain.param.ApiParamMappingParam;
import com.forgex.integration.service.IApiParamMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 接口参数映射管理控制器
 * <p>
 * 提供接口参数映射关系的增删改查、批量保存等 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/integration/param-mapping")
@RequiredArgsConstructor
@Tag(name = "接口参数映射管理", description = "提供接口参数映射关系的增删改查、批量保存等功能")
public class ApiParamMappingController {

    private final IApiParamMappingService apiParamMappingService;

    /**
     * 查询参数映射列表
     * <p>
     * 根据接口配置 ID 和映射方向查询所有映射关系
     * 支持按源字段路径、目标字段路径模糊查询
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID（必填）
     *              - direction: 映射方向（必填，REQUEST/RESPONSE）
     *              - sourceFieldPath: 源字段路径（可选，模糊查询）
     *              - targetFieldPath: 目标字段路径（可选，模糊查询）
     * @return 参数映射 DTO 列表
     * @see ApiParamMappingParam
     * @see ApiParamMappingDTO
     * @see com.forgex.integration.service.IApiParamMappingService#listMappings
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/list")
    @Operation(summary = "查询参数映射列表", description = "根据接口配置 ID 和映射方向查询所有映射关系")
    public R<List<ApiParamMappingDTO>> listMappings(@RequestBody @Validated ApiParamMappingParam param) {
        List<ApiParamMappingDTO> mappings = apiParamMappingService.listMappings(param);
        return R.ok(mappings);
    }

    /**
     * 根据 ID 获取参数映射详情
     * <p>
     * 用于编辑时回显数据
     * 返回完整的映射配置信息
     * </p>
     *
     * @param id 参数映射 ID（必填）
     * @return 参数映射 DTO
     * @see ApiParamMappingDTO
     * @see com.forgex.integration.service.IApiParamMappingService#getById
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取参数映射详情", description = "用于编辑时回显数据")
    public R<ApiParamMappingDTO> getDetail(@PathVariable Long id) {
        ApiParamMappingDTO dto = apiParamMappingService.getById(id);
        return R.ok(dto);
    }

    /**
     * 创建参数映射
     * <p>
     * 自动校验映射关系的唯一性
     * 同一接口配置、同一方向、同一源字段路径不能重复
     * </p>
     *
     * @param dto 参数映射 DTO，包含源字段、目标字段、转换规则等
     * @return 创建结果
     * @throws com.forgex.common.exception.BusinessException 当映射关系已存在时抛出
     * @see ApiParamMappingDTO
     * @see com.forgex.integration.service.IApiParamMappingService#create
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/create")
    @Operation(summary = "创建参数映射", description = "新增参数映射，自动校验映射关系的唯一性")
    public R<Void> create(@RequestBody @Validated ApiParamMappingDTO dto) {
        apiParamMappingService.create(dto);
        return R.ok();
    }

    /**
     * 更新参数映射
     * <p>
     * 自动校验映射关系的唯一性（排除自身）
     * 支持更新源字段、目标字段、转换规则等
     * </p>
     *
     * @param dto 参数映射 DTO，ID 必须存在
     * @return 更新结果
     * @throws com.forgex.common.exception.BusinessException 当映射关系不存在或与其他记录冲突时抛出
     * @see ApiParamMappingDTO
     * @see com.forgex.integration.service.IApiParamMappingService#update
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/update")
    @Operation(summary = "更新参数映射", description = "修改参数映射，自动校验映射关系的唯一性")
    public R<Void> update(@RequestBody @Validated ApiParamMappingDTO dto) {
        apiParamMappingService.update(dto);
        return R.ok();
    }

    /**
     * 删除参数映射
     * <p>
     * 删除单个映射关系
     * 物理删除数据
     * </p>
     *
     * @param id 参数映射 ID（必填）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当映射关系不存在时抛出
     * @see com.forgex.integration.service.IApiParamMappingService#delete
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除参数映射", description = "删除单个映射关系")
    public R<Void> delete(@PathVariable Long id) {
        apiParamMappingService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除参数映射
     * <p>
     * 支持批量删除多个映射关系
     * 事务保证：要么全部删除成功，要么全部失败回滚
     * </p>
     *
     * @param ids 参数映射 ID 列表（不能为空）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当批量删除失败时抛出
     * @see com.forgex.integration.service.IApiParamMappingService#batchDelete
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除参数映射", description = "批量删除多个映射关系")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        apiParamMappingService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 批量保存参数映射
     * <p>
     * 先删除指定接口配置和方向的所有映射，再批量插入新映射
     * 用于一次性保存多个映射关系，适用于配置导入等场景
     * 事务保证：要么全部保存成功，要么全部失败回滚
     * </p>
     *
     * @param request 请求参数
     *                - apiConfigId: 接口配置 ID（必填）
     *                - direction: 映射方向（必填，REQUEST/RESPONSE）
     *                - mappings: 参数映射 DTO 列表
     * @return 保存结果
     * @throws com.forgex.common.exception.BusinessException 当批量保存失败时抛出
     * @see ApiParamMappingDTO
     * @see com.forgex.integration.service.IApiParamMappingService#batchSave
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/batch-save")
    @Operation(summary = "批量保存参数映射", description = "一次性保存多个映射关系")
    public R<Void> batchSave(@RequestBody ApiParamMappingBatchSaveParam request) {
        apiParamMappingService.batchSave(
            request.getApiConfigId(),
            request.getDirection(),
            request.getMappings()
        );
        return R.ok();
    }
}
