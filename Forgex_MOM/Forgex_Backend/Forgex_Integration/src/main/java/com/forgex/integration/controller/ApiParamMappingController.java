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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 接口参数映射控制器。
 * <p>
 * 提供接口字段映射关系的查询、新增、更新、删除、批量删除和批量保存能力。
 * 映射关系用于定义源字段路径到目标字段路径的转换关系。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiParamMappingService
 */
@RestController
@RequestMapping("/api/integration/param-mapping")
@RequiredArgsConstructor
@Tag(name = "接口参数映射管理", description = "接口字段映射")
public class ApiParamMappingController {

    /**
     * 接口参数映射服务。
     */
    private final IApiParamMappingService apiParamMappingService;

    /**
     * 查询字段映射列表。
     *
     * @param param 查询参数
     * @return 字段映射列表
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/list")
    @Operation(summary = "查询映射")
    public R<List<ApiParamMappingDTO>> listMappings(@RequestBody @Validated ApiParamMappingParam param) {
        return R.ok(apiParamMappingService.listMappings(param));
    }

    /**
     * 查询字段映射详情。
     *
     * @param id 映射 ID
     * @return 字段映射详情
     */
    @RequirePerm("integration:api-config:config-mapping")
    @GetMapping("/detail/{id}")
    @Operation(summary = "查询映射详情")
    public R<ApiParamMappingDTO> getDetail(@PathVariable Long id) {
        return R.ok(apiParamMappingService.getById(id));
    }

    /**
     * 新增字段映射。
     *
     * @param dto 字段映射 DTO
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/create")
    @Operation(summary = "新增映射")
    public R<Void> create(@RequestBody @Validated ApiParamMappingDTO dto) {
        apiParamMappingService.create(dto);
        return R.ok();
    }

    /**
     * 更新字段映射。
     *
     * @param dto 字段映射 DTO
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/update")
    @Operation(summary = "更新映射")
    public R<Void> update(@RequestBody @Validated ApiParamMappingDTO dto) {
        apiParamMappingService.update(dto);
        return R.ok();
    }

    /**
     * 删除字段映射。
     *
     * @param id 映射 ID
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除映射")
    public R<Void> delete(@PathVariable Long id) {
        apiParamMappingService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除数据。
     *
     * @param ids 映射 ID 列表
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除映射")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        apiParamMappingService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 批量保存字段映射。
     *
     * @param request 批量保存请求
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-mapping")
    @PostMapping("/batch-save")
    @Operation(summary = "批量保存映射")
    public R<Void> batchSave(@RequestBody ApiParamMappingBatchSaveParam request) {
        apiParamMappingService.batchSave(
            request.getApiConfigId(),
            request.getOutboundTargetId(),
            request.getDirection(),
            request.getMappings()
        );
        return R.ok();
    }
}
