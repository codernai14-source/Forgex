package com.forgex.integration.controller;

import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ApiParamConfigDTO;
import com.forgex.integration.domain.param.ApiParamBatchSaveParam;
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
 * 接口参数配置控制器。
 * <p>
 * 提供接口入参、出参参数树的查询、维护、整树保存以及 JSON/Java 实体解析能力。
 * 参数树用于描述接口字段路径、字段类型、节点类型、必填状态和展示顺序。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-04-01
 * @see IApiParamConfigService
 */
@RestController
@RequestMapping("/api/integration/param-config")
@RequiredArgsConstructor
@Tag(name = "接口参数配置管理", description = "接口参数树配置")
public class ApiParamConfigController {

    /**
     * 接口参数配置服务。
     */
    private final IApiParamConfigService apiParamConfigService;

    /**
     * 查询接口参数树。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param direction        参数方向，request/response
     * @return 参数树
     */
    @RequirePerm("integration:api-config:config-param")
    @GetMapping("/tree")
    @Operation(summary = "查询参数树")
    public R<List<ApiParamTreeVO>> listParamTree(@RequestParam Long apiConfigId,
                                                 @RequestParam(required = false) Long outboundTargetId,
                                                 @RequestParam(required = false) String direction) {
        return R.ok(apiParamConfigService.listParamTree(apiConfigId, outboundTargetId, direction));
    }

    /**
     * 查询参数子节点。
     *
     * @param param 查询参数
     * @return 子节点列表
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/children")
    @Operation(summary = "查询子节点")
    public R<List<ApiParamConfigDTO>> listChildren(@RequestBody @Validated ApiParamConfigParam param) {
        return R.ok(apiParamConfigService.listChildren(param));
    }

    /**
     * 查询参数节点详情。
     *
     * @param id 参数节点 ID
     * @return 参数节点详情
     */
    @RequirePerm("integration:api-config:config-param")
    @GetMapping("/detail/{id}")
    @Operation(summary = "查询参数详情")
    public R<ApiParamConfigDTO> getDetail(@PathVariable Long id) {
        return R.ok(apiParamConfigService.getById(id));
    }

    /**
     * 新增参数节点。
     *
     * @param dto 参数节点数据
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/create")
    @Operation(summary = "新增参数节点")
    public R<Void> create(@RequestBody @Validated ApiParamConfigDTO dto) {
        apiParamConfigService.create(dto);
        return R.ok();
    }

    /**
     * 更新参数节点。
     *
     * @param dto 参数节点数据
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/update")
    @Operation(summary = "更新参数节点")
    public R<Void> update(@RequestBody @Validated ApiParamConfigDTO dto) {
        apiParamConfigService.update(dto);
        return R.ok();
    }

    /**
     * 删除参数节点。
     *
     * @param id 参数节点 ID
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除参数节点")
    public R<Void> delete(@PathVariable Long id) {
        apiParamConfigService.delete(id);
        return R.ok();
    }

    /**
     * 批量删除参数节点。
     *
     * @param ids 参数节点 ID 列表
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除参数节点")
    public R<Void> batchDelete(@RequestBody List<Long> ids) {
        apiParamConfigService.batchDelete(ids);
        return R.ok();
    }

    /**
     * 整树保存参数配置。
     *
     * @param request 整树保存请求
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/replace-tree")
    @Operation(summary = "整树保存")
    public R<Void> replaceTree(@RequestBody @Validated ApiParamBatchSaveParam request) {
        apiParamConfigService.replaceTree(
            request.getApiConfigId(),
            request.getOutboundTargetId(),
            request.getDirection(),
            request.getTree()
        );
        return R.ok();
    }

    /**
     * 从 JSON 导入参数树。
     *
     * @param request JSON 导入请求
     * @return 操作结果
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/import-json")
    @Operation(summary = "导入 JSON 参数树")
    public R<Void> importFromJson(@RequestBody ApiParamJsonImportParam request) {
        apiParamConfigService.importFromJson(
            request.getApiConfigId(),
            request.getOutboundTargetId(),
            request.getDirection(),
            request.getEffectiveJsonText()
        );
        return R.ok();
    }

    /**
     * 解析 JSON 文本为参数树预览。
     *
     * @param request 请求体，包含 jsonString
     * @return 参数树预览
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/parse-json")
    @Operation(summary = "解析 JSON")
    public R<List<ApiParamTreeVO>> parseJson(@RequestBody Map<String, String> request) {
        return R.ok(apiParamConfigService.parseJsonToTree(request.get("jsonString")));
    }

    /**
     * 解析 Java 实体源码为参数树预览。
     *
     * @param request 请求体，包含 javaSource
     * @return 参数树预览
     */
    @RequirePerm("integration:api-config:config-param")
    @PostMapping("/parse-java")
    @Operation(summary = "解析 Java 实体")
    public R<List<ApiParamTreeVO>> parseJava(@RequestBody ApiParamJsonImportParam request) {
        return R.ok(apiParamConfigService.parseJavaToTree(request.getJavaSource()));
    }

    /**
     * 按字段路径查询参数节点。
     *
     * @param apiConfigId      接口配置 ID
     * @param outboundTargetId 出站目标 ID，可为空
     * @param fieldPath        字段路径
     * @param direction        参数方向
     * @return 参数节点详情
     */
    @RequirePerm("integration:api-config:config-param")
    @GetMapping("/by-field-path")
    @Operation(summary = "按路径查询参数")
    public R<ApiParamConfigDTO> getByFieldPath(@RequestParam Long apiConfigId,
                                               @RequestParam(required = false) Long outboundTargetId,
                                               @RequestParam String fieldPath,
                                               @RequestParam String direction) {
        return R.ok(apiParamConfigService.getByFieldPath(apiConfigId, outboundTargetId, fieldPath, direction));
    }
}
