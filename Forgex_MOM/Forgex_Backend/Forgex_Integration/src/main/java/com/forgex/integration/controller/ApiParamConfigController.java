package com.forgex.integration.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.param.ApiParamConfigParam;
import com.forgex.integration.domain.param.ApiParamJsonImportParam;
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
     * 支持查询根节点或指定方向的完整树
     * </p>
     *
     * @param apiConfigId 接口配置 ID（必填）
     * @param direction 参数方向（可选，REQUEST/RESPONSE，不传则查询所有方向）
     * @return 树形结构列表，包含父子关系
     * @see ApiParamTreeVO
     * @see com.forgex.integration.service.IApiParamConfigService#listParamTree
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 支持查询根节点（parentId=null）或指定父节点的子节点
     * </p>
     *
     * @param param 查询参数
     *              - apiConfigId: 接口配置 ID（必填）
     *              - parentId: 父节点 ID（可选，null 表示根节点）
     *              - direction: 参数方向（必填）
     * @return 子节点 DTO 列表
     * @see ApiParamConfigParam
     * @see ApiParamConfigDTO
     * @see com.forgex.integration.service.IApiParamConfigService#listChildren
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 返回参数配置的完整信息
     * </p>
     *
     * @param id 参数配置 ID（必填）
     * @return 参数配置 DTO
     * @see ApiParamConfigDTO
     * @see com.forgex.integration.service.IApiParamConfigService#getById
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 字段路径根据父节点路径和当前字段名自动生成
     * </p>
     *
     * @param dto 参数配置 DTO，包含字段名、字段类型、父节点 ID 等
     * @return 创建结果
     * @throws com.forgex.common.exception.BusinessException 当参数配置已存在时抛出
     * @see ApiParamConfigDTO
     * @see com.forgex.integration.service.IApiParamConfigService#create
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 当字段名或父节点变更时，自动重新计算字段路径
     * </p>
     *
     * @param dto 参数配置 DTO，ID 必须存在
     * @return 更新结果
     * @throws com.forgex.common.exception.BusinessException 当参数配置不存在时抛出
     * @see ApiParamConfigDTO
     * @see com.forgex.integration.service.IApiParamConfigService#update
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 删除父节点时会自动删除其所有子孙节点
     * </p>
     *
     * @param id 参数配置 ID（必填）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当参数配置不存在时抛出
     * @see com.forgex.integration.service.IApiParamConfigService#delete
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 事务保证：要么全部删除成功，要么全部失败回滚
     * 注意：批量删除也会级联删除每个节点的子节点
     * </p>
     *
     * @param ids 参数配置 ID 列表（不能为空）
     * @return 删除结果
     * @throws com.forgex.common.exception.BusinessException 当批量删除失败时抛出
     * @see com.forgex.integration.service.IApiParamConfigService#batchDelete
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 自动创建所有节点，适用于快速导入接口参数结构
     * </p>
     *
     * @param request 请求参数
     *                - apiConfigId: 接口配置 ID（必填）
     *                - direction: 参数方向（必填，REQUEST/RESPONSE）
     *                - jsonString: JSON 字符串（必填）
     * @return 导入结果
     * @throws com.forgex.common.exception.BusinessException 当 JSON 格式错误或导入失败时抛出
     * @see com.forgex.integration.service.IApiParamConfigService#importFromJson
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/import-json")
    @Operation(summary = "从 JSON 导入参数配置", description = "解析 JSON 结构并转换为树形参数配置")
    public R<Void> importFromJson(@RequestBody ApiParamJsonImportParam request) {
        apiParamConfigService.importFromJson(
            request.getApiConfigId(),
            request.getDirection(),
            request.getEffectiveJsonText()
        );
        return R.ok();
    }

    /**
     * 解析 JSON 为树形结构
     * <p>
     * 将 JSON 对象解析为参数配置树，用于预览
     * 不保存到数据库，仅返回解析后的树形结构
     * </p>
     *
     * @param request 请求参数
     *                - jsonString: JSON 字符串（必填）
     * @return 树形结构 VO 列表
     * @throws com.forgex.common.exception.BusinessException 当 JSON 格式错误时抛出
     * @see ApiParamTreeVO
     * @see com.forgex.integration.service.IApiParamConfigService#parseJsonToTree
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
     * 支持精确匹配字段路径
     * </p>
     *
     * @param apiConfigId 接口配置 ID（必填）
     * @param fieldPath 字段路径（必填），如 "user.name"、"data.items[0].id"
     * @param direction 参数方向（必填，REQUEST/RESPONSE）
     * @return 参数配置 DTO，如果不存在返回 null
     * @see ApiParamConfigDTO
     * @see com.forgex.integration.service.IApiParamConfigService#getByFieldPath
     * @see com.forgex.common.web.R
     */
    @RequirePerm("integration:api-config:config-param")
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
