package com.forgex.integration.controller;

import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.vo.ApiParamTreeVO;
import com.forgex.integration.service.IApiParamConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 接口参数配置管理控制器
 * <p>
 * 提供接口参数的树形结构管理、增删改查、JSON 导入等 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/integration/param-config")
@RequiredArgsConstructor
@Tag(name = "接口参数配置管理", description = "提供接口参数的树形结构管理、增删改查、JSON 导入等功能")
public class ApiParamConfigController {

    private final IApiParamConfigService apiParamConfigService;

    /**
     * 查询参数配置树形列表
     * <p>
     * 根据接口配置 ID 和参数方向构建完整的树形结构
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param direction 参数方向（REQUEST/RESPONSE）
     * @return 树形结构列表
     */
    @GetMapping("/tree")
    @Operation(summary = "查询参数配置树形列表", description = "根据接口配置 ID 和参数方向构建完整的树形结构")
    public R<List<ApiParamTreeVO>> listParamTree(
        @RequestParam Long apiConfigId,
        @RequestParam(required = false) String direction
    ) {
        List<ApiParamTreeVO> treeList = apiParamConfigService.listParamTree(apiConfigId, direction);
        return R.ok(treeList);
    }

    /**
     * 查询子节点列表
     * <p>
     * 用于懒加载树形节点，根据父节点 ID 查询子节点
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID
     *              - parentId: 父节点 ID（null 表示根节点）
     *              - direction: 参数方向
     * @return 子节点 DTO 列表
     */
    @PostMapping("/children")
    @Operation(summary = "查询子节点列表", description = "用于懒加载树形节点")
    public R<List<ApiParamConfigDTO>> listChildren(@RequestBody @Validated ApiParamConfigParam param) {
        List<ApiParamConfigDTO> children = apiParamConfigService.listChildren(param);
        return R.ok(children);
    }

    /**
     * 根据 ID 获取参数配置详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 参数配置 ID
     * @return 参数配置 DTO
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取参数配置详情", description = "用于编辑时回显数据")
    public R<ApiParamConfigDTO> getDetail(@PathVariable Long id) {
        ApiParamConfigDTO dto = apiParamConfigService.getById(id);
        return R.ok(dto);
    }

    /**
     * 创建参数配置
     * <p>
     * 自动计算字段路径和排序号
     * </p>
     *
     * @param dto 参数配置 DTO
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建参数配置", description = "新增参数配置，自动计算字段路径和排序号")
    public R<Void> create(@RequestBody @Validated ApiParamConfigDTO dto) {
        apiParamConfigService.create(dto);
        return R.ok();
    }

    /**
     * 更新参数配置
     * <p>
     * 自动更新字段路径
     * </p>
     *
     * @param dto 参数配置 DTO
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新参数配置", description = "修改参数配置，自动更新字段路径")
    public R<Void> update(@RequestBody @Validated ApiParamConfigDTO dto) {
        apiParamConfigService.update(dto);
        return R.ok();
    }

    /**
     * 删除参数配置
     * <p>
     * 级联删除所有子节点
     * </p>
     *
     * @param id 参数配置 ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除参数配置", description = "级联删除所有子节点")
    public R<Void> delete(@PathVariable Long id) {
        apiParamConfigService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除参数配置
     * <p>
     * 支持批量删除多个参数配置
     * </p>
     *
     * @param ids 参数配置 ID 列表
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除参数配置", description = "批量删除多个参数配置")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        apiParamConfigService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 从 JSON 导入参数配置
     * <p>
     * 解析 JSON 结构并转换为树形参数配置
     * </p>
     *
     * @param request 请求参数
     *                - apiConfigId: 接口配置 ID
     *                - direction: 参数方向（REQUEST/RESPONSE）
     *                - jsonString: JSON 字符串
     * @return 导入结果
     */
    @PostMapping("/import-json")
    @Operation(summary = "从 JSON 导入参数配置", description = "解析 JSON 结构并转换为树形参数配置")
    public R<Void> importFromJson(@RequestBody Map<String, Object> request) {
        Long apiConfigId = (Long) request.get("apiConfigId");
        String direction = (String) request.get("direction");
        String jsonString = (String) request.get("jsonString");
        
        apiParamConfigService.importFromJson(apiConfigId, direction, jsonString);
        return R.ok();
    }

    /**
     * 解析 JSON 为树形结构
     * <p>
     * 将 JSON 对象解析为参数配置树，用于预览
     * </p>
     *
     * @param request 请求参数
     *                - jsonString: JSON 字符串
     * @return 树形结构 VO 列表
     */
    @PostMapping("/parse-json")
    @Operation(summary = "解析 JSON 为树形结构", description = "将 JSON 对象解析为参数配置树，用于预览")
    public R<List<ApiParamTreeVO>> parseJson(@RequestBody Map<String, String> request) {
        String jsonString = request.get("jsonString");
        List<ApiParamTreeVO> treeList = apiParamConfigService.parseJsonToTree(jsonString);
        return R.ok(treeList);
    }

    /**
     * 根据字段路径查询参数配置
     * <p>
     * 用于参数映射时查找对应的字段
     * </p>
     *
     * @param apiConfigId 接口配置 ID
     * @param fieldPath 字段路径
     * @param direction 参数方向
     * @return 参数配置 DTO
     */
    @GetMapping("/by-field-path")
    @Operation(summary = "根据字段路径查询参数配置", description = "用于参数映射时查找对应的字段")
    public R<ApiParamConfigDTO> getByFieldPath(
        @RequestParam Long apiConfigId,
        @RequestParam String fieldPath,
        @RequestParam String direction
    ) {
        ApiParamConfigDTO dto = apiParamConfigService.getByFieldPath(apiConfigId, fieldPath, direction);
        return R.ok(dto);
    }
}
