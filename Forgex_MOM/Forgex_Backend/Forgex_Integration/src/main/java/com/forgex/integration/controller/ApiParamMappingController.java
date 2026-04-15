package com.forgex.integration.controller;

import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiParamMappingDTO;
import com.forgex.integration.domain.param.ApiParamMappingParam;
import com.forgex.integration.service.IApiParamMappingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID
     *              - direction: 映射方向
     *              - sourceFieldPath: 源字段路径（模糊查询）
     *              - targetFieldPath: 目标字段路径（模糊查询）
     * @return 参数映射 DTO 列表
     */
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
     * </p>
     *
     * @param id 参数映射 ID
     * @return 参数映射 DTO
     */
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
     * </p>
     *
     * @param dto 参数映射 DTO
     * @return 创建结果
     */
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
     * </p>
     *
     * @param dto 参数映射 DTO
     * @return 更新结果
     */
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
     * </p>
     *
     * @param id 参数映射 ID
     * @return 删除结果
     */
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
     * </p>
     *
     * @param ids 参数映射 ID 列表
     * @return 删除结果
     */
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
     * 用于一次性保存多个映射关系
     * </p>
     *
     * @param request 请求参数
     *                - apiConfigId: 接口配置 ID
     *                - direction: 映射方向
     *                - mappings: 参数映射 DTO 列表
     * @return 保存结果
     */
    @PostMapping("/batch-save")
    @Operation(summary = "批量保存参数映射", description = "一次性保存多个映射关系")
    public R<Void> batchSave(@RequestBody Map<String, Object> request) {
        Long apiConfigId = (Long) request.get("apiConfigId");
        String direction = (String) request.get("direction");
        @SuppressWarnings("unchecked")
        List<ApiParamMappingDTO> mappings = (List<ApiParamMappingDTO>) request.get("mappings");
        
        apiParamMappingService.batchSave(apiConfigId, direction, mappings);
        return R.ok();
    }
}
