package com.forgex.basic.material.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.material.domain.entity.BasicMaterialExtendConfig;
import com.forgex.basic.material.domain.param.MaterialExtendFieldSortParam;
import com.forgex.basic.material.domain.param.MaterialExtendFieldStatusParam;
import com.forgex.basic.material.domain.param.MaterialExtendSchemaQueryParam;
import com.forgex.basic.material.domain.response.MaterialExtendConfigVO;
import com.forgex.basic.material.domain.response.MaterialExtendSchemaVO;
import com.forgex.basic.material.service.IMaterialExtendConfigService;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 物料附属字段结构控制器。
 * <p>
 * 提供附属字段配置、启停、排序和 schema 查询接口。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-05-12
 */
@Tag(name = "物料附属字段结构", description = "物料附属字段结构配置接口")
@RestController
@RequestMapping("/material/extend-schema")
@RequiredArgsConstructor
public class MaterialExtendSchemaController {

    private final IMaterialExtendConfigService extendConfigService;

    /**
     * 分页查询字段配置。
     *
     * @param param 查询参数
     * @return 字段配置分页
     */
    @Operation(summary = "分页查询字段配置")
    @RequirePerm({"basic:material:query", "basic:material:extendConfig:query"})
    @PostMapping("/page")
    public R<IPage<MaterialExtendConfigVO>> page(@RequestBody(required = false) MaterialExtendSchemaQueryParam param) {
        MaterialExtendSchemaQueryParam safeParam = param == null ? new MaterialExtendSchemaQueryParam() : param;
        return R.ok(extendConfigService.pageExtendConfigs(
                TenantContext.get(),
                safeParam.getModule(),
                safeParam.getMaterialType(),
                safeParam.getPageNum(),
                safeParam.getPageSize()
        ));
    }

    /**
     * 查询指定模块和物料类型的 schema。
     *
     * @param param 查询参数
     * @return schema 视图
     */
    @Operation(summary = "查询字段结构")
    @RequirePerm({"basic:material:query", "basic:material:extendConfig:query"})
    @PostMapping("/schema")
    public R<MaterialExtendSchemaVO> schema(@RequestBody MaterialExtendSchemaQueryParam param) {
        return R.ok(extendConfigService.getSchema(TenantContext.get(), param.getModule(), param.getMaterialType()));
    }

    /**
     * 查询字段详情。
     *
     * @param body 请求体，包含 id
     * @return 字段详情
     */
    @Operation(summary = "查询字段详情")
    @RequirePerm({"basic:material:query", "basic:material:extendConfig:query"})
    @PostMapping("/detail")
    public R<MaterialExtendConfigVO> detail(@RequestBody Map<String, Long> body) {
        Long id = body == null ? null : body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        return R.ok(extendConfigService.getConfigById(TenantContext.get(), id));
    }

    /**
     * 创建字段配置。
     *
     * @param config 字段配置
     * @return 字段配置 ID
     */
    @Operation(summary = "创建字段配置")
    @RequirePerm("basic:material:extendConfig:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody BasicMaterialExtendConfig config) {
        return R.ok(extendConfigService.createConfig(TenantContext.get(), config));
    }

    /**
     * 更新字段配置。
     *
     * @param config 字段配置
     * @return 统一响应
     */
    @Operation(summary = "更新字段配置")
    @RequirePerm("basic:material:extendConfig:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody BasicMaterialExtendConfig config) {
        if (config == null || config.getId() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        extendConfigService.updateConfig(TenantContext.get(), config);
        return R.ok();
    }

    /**
     * 删除字段配置。
     *
     * @param body 请求体，包含 id
     * @return 统一响应
     */
    @Operation(summary = "删除字段配置")
    @RequirePerm("basic:material:extendConfig:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Long> body) {
        Long id = body == null ? null : body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        extendConfigService.deleteConfig(TenantContext.get(), id);
        return R.ok();
    }

    /**
     * 更新字段状态。
     *
     * @param param 状态参数
     * @return 统一响应
     */
    @Operation(summary = "启停字段配置")
    @RequirePerm("basic:material:extendConfig:edit")
    @PostMapping("/status")
    public R<Void> status(@RequestBody MaterialExtendFieldStatusParam param) {
        if (param == null || param.getId() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        extendConfigService.updateConfigStatus(TenantContext.get(), param.getId(), param.getStatus());
        return R.ok();
    }

    /**
     * 保存字段排序。
     *
     * @param param 排序参数
     * @return 统一响应
     */
    @Operation(summary = "保存字段排序")
    @RequirePerm("basic:material:extendConfig:edit")
    @PostMapping("/sort")
    public R<Void> sort(@RequestBody MaterialExtendFieldSortParam param) {
        extendConfigService.sortConfigs(TenantContext.get(), param == null ? null : param.getItems());
        return R.ok();
    }
}
