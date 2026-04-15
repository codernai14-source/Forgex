package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.label.domain.dto.LabelTemplateDTO;
import com.forgex.basic.label.domain.param.BatchIdsParam;
import com.forgex.basic.label.domain.param.IdParam;
import com.forgex.basic.label.domain.param.LabelTemplateQueryParam;
import com.forgex.basic.label.domain.param.LabelTemplateSaveParam;
import com.forgex.basic.label.domain.param.LabelTemplateUpdateParam;
import com.forgex.basic.label.domain.vo.TemplateVO;
import com.forgex.basic.label.service.LabelTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 标签模板管理 Controller
 * <p>
 * 提供标签模板管理的 HTTP 接口，包括模板的分页查询、详情获取、新增、修改、删除、设置默认等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelTemplateService
 */
@Tag(name = "标签模板管理", description = "标签模板管理接口")
@RestController
@RequestMapping("/label/template")
@RequiredArgsConstructor
public class LabelTemplateController {

    private final LabelTemplateService labelTemplateService;

    /**
     * 分页查询模板列表
     * <p>
     * 接口路径：POST /label/template/page
     * 需要权限：label:template:query
     * </p>
     *
     * @param param 查询参数
     * @return {@link R} 包含模板分页列表的统一返回结构
     */
    @Operation(summary = "分页查询模板列表", description = "支持多条件筛选，返回模板 VO 列表")
    @RequirePerm("label:template:query")
    @PostMapping("/page")
    public R<IPage<TemplateVO>> page(@RequestBody @Validated LabelTemplateQueryParam param) {
        Long tenantId = TenantContext.get();
        IPage<TemplateVO> page = labelTemplateService.pageTemplates(param, tenantId);
        return R.ok(page);
    }

    /**
     * 查询模板详情
     * <p>
     * 接口路径：POST /label/template/detail
     * 需要权限：label:template:query
     * </p>
     *
     * @param body 请求体参数，包含 id
     * @return {@link R} 包含模板详情的统一返回结构
     */
    @Operation(summary = "查询模板详情", description = "根据 ID 查询模板详细信息")
    @RequirePerm("label:template:query")
    @PostMapping("/detail")
    public R<LabelTemplateDTO> detail(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        LabelTemplateDTO dto = labelTemplateService.getTemplateById(id, tenantId);
        return R.ok(dto);
    }

    /**
     * 新增模板
     * <p>
     * 接口路径：POST /label/template/add
     * 需要权限：label:template:add
     * </p>
     *
     * @param param 模板保存参数
     * @return {@link R} 包含模板 ID 的统一返回结构
     */
    @Operation(summary = "新增模板", description = "创建新的标签模板")
    @RequirePerm("label:template:add")
    @PostMapping("/add")
    public R<Long> add(@RequestBody @Validated LabelTemplateSaveParam param) {
        Long tenantId = TenantContext.get();
        Long templateId = labelTemplateService.addTemplate(param, tenantId);
        return R.ok(templateId);
    }

    /**
     * 修改模板（新版本）
     * <p>
     * 接口路径：POST /label/template/update
     * 需要权限：label:template:edit
     * </p>
     *
     * @param param 模板更新参数
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "修改模板", description = "更新模板信息并创建新版本")
    @RequirePerm("label:template:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated LabelTemplateUpdateParam param) {
        Long tenantId = TenantContext.get();
        labelTemplateService.updateTemplate(param, tenantId);
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除模板
     * <p>
     * 接口路径：POST /label/template/delete
     * 需要权限：label:template:delete
     * </p>
     *
     * @param param ID 参数
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "删除模板", description = "逻辑删除指定模板")
    @RequirePerm("label:template:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @Validated IdParam param) {
        Long tenantId = TenantContext.get();
        labelTemplateService.deleteTemplate(param.getId(), tenantId);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 批量删除模板
     * <p>
     * 接口路径：POST /label/template/batchDelete
     * 需要权限：label:template:delete
     * </p>
     *
     * @param param 批量 ID 参数
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "批量删除模板", description = "批量逻辑删除多个模板")
    @RequirePerm("label:template:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody @Validated BatchIdsParam param) {
        Long tenantId = TenantContext.get();
        labelTemplateService.batchDeleteTemplates(param.getIds(), tenantId);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 设置默认模板
     * <p>
     * 接口路径：POST /label/template/setDefault
     * 需要权限：label:template:edit
     * </p>
     *
     * @param body 请求体参数，包含 id 和 templateType
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "设置默认模板", description = "将指定模板设置为该类型的默认模板")
    @RequirePerm("label:template:edit")
    @PostMapping("/setDefault")
    public R<Void> setDefault(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? Long.valueOf(body.get("id").toString()) : null;
        String templateType = body.get("templateType") != null ? body.get("templateType").toString() : null;

        if (id == null || templateType == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        labelTemplateService.setDefaultTemplate(id, templateType, tenantId);
        return R.ok();
    }
}
