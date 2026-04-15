package com.forgex.basic.label.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.basic.label.domain.vo.BindingVO;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.label.domain.dto.LabelBindingDTO;
import com.forgex.basic.label.domain.param.IdParam;
import com.forgex.basic.label.domain.param.LabelBindingMatchParam;
import com.forgex.basic.label.domain.param.LabelBindingSaveParam;
import com.forgex.basic.label.service.LabelBindingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 标签模板绑定管理 Controller
 * <p>
 * 提供标签模板绑定管理的 HTTP 接口，包括绑定关系的分页查询、新增、修改、删除以及智能匹配模板等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 * @see LabelBindingService
 */
@Tag(name = "标签模板绑定管理", description = "标签模板绑定管理接口")
@RestController
@RequestMapping("/label/binding")
@RequiredArgsConstructor
public class LabelBindingController {

    private final LabelBindingService labelBindingService;

    /**
     * 分页查询绑定关系列表
     * <p>
     * 接口路径：POST /label/binding/page
     * 需要权限：label:binding:query
     * </p>
     *
     * @param body 请求体参数
     * @return {@link R} 包含绑定关系分页列表的统一返回结构
     */
    @Operation(summary = "分页查询绑定关系列表", description = "支持多条件筛选，返回绑定关系 VO 列表")
    @RequirePerm("label:binding:query")
    @PostMapping("/page")
    public R<IPage<BindingVO>> page(@RequestBody Map<String, Object> body) {
        Integer pageNum = body.get("pageNum") != null ? Integer.valueOf(body.get("pageNum").toString()) : 1;
        Integer pageSize = body.get("pageSize") != null ? Integer.valueOf(body.get("pageSize").toString()) : 10;
        Long templateId = body.get("templateId") != null ? Long.valueOf(body.get("templateId").toString()) : null;
        String bindingType = body.get("bindingType") != null ? body.get("bindingType").toString() : null;
        String bindingValue = body.get("bindingValue") != null ? body.get("bindingValue").toString() : null;
        Long factoryId = body.get("factoryId") != null ? Long.valueOf(body.get("factoryId").toString()) : null;

        Long tenantId = TenantContext.get();
        IPage<BindingVO> page = labelBindingService.pageBindings(pageNum, pageSize, templateId, bindingType, bindingValue, factoryId, tenantId);
        return R.ok(page);
    }

    /**
     * 新增绑定关系
     * <p>
     * 接口路径：POST /label/binding/add
     * 需要权限：label:binding:add
     * </p>
     *
     * @param param 绑定保存参数
     * @return {@link R} 包含绑定 ID 的统一返回结构
     */
    @Operation(summary = "新增绑定关系", description = "创建新的模板绑定关系")
    @RequirePerm("label:binding:add")
    @PostMapping("/add")
    public R<Long> add(@RequestBody @Validated LabelBindingSaveParam param) {
        Long tenantId = TenantContext.get();
        Long bindingId = labelBindingService.addBinding(param, tenantId);
        return R.ok(bindingId);
    }

    /**
     * 修改绑定关系
     * <p>
     * 接口路径：POST /label/binding/update
     * 需要权限：label:binding:edit
     * </p>
     *
     * @param body 请求体参数
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "修改绑定关系", description = "更新绑定关系的优先级等信息")
    @RequirePerm("label:binding:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody Map<String, Object> body) {
        Long id = body.get("id") != null ? Long.valueOf(body.get("id").toString()) : null;
        Integer priority = body.get("priority") != null ? Integer.valueOf(body.get("priority").toString()) : null;
        Long factoryId = body.get("factoryId") != null ? Long.valueOf(body.get("factoryId").toString()) : null;

        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        labelBindingService.updateBinding(id, priority, factoryId, tenantId);
        return R.ok(CommonPrompt.UPDATE_SUCCESS);
    }

    /**
     * 删除绑定关系
     * <p>
     * 接口路径：POST /label/binding/delete
     * 需要权限：label:binding:delete
     * </p>
     *
     * @param param ID 参数
     * @return {@link R} 统一返回结构
     */
    @Operation(summary = "删除绑定关系", description = "逻辑删除指定绑定关系")
    @RequirePerm("label:binding:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody @Validated IdParam param) {
        Long tenantId = TenantContext.get();
        labelBindingService.deleteBinding(param.getId(), tenantId);
        return R.ok(CommonPrompt.DELETE_SUCCESS);
    }

    /**
     * 智能匹配模板（打印核心）
     * <p>
     * 接口路径：POST /label/template/match
     * 需要权限：label:template:query
     * </p>
     * <p>
     * 根据业务条件自动匹配最合适的模板，匹配优先级：
     * 1. 按物料精确匹配
     * 2. 按供应商匹配
     * 3. 按客户匹配
     * 4. 返回默认模板
     * </p>
     *
     * @param param 匹配参数
     * @return {@link R} 包含匹配到的模板 ID 的统一返回结构
     */
    @Operation(summary = "智能匹配模板", description = "根据业务条件自动匹配最合适的模板")
    @RequirePerm("label:template:query")
    @PostMapping("/match")
    public R<Long> matchTemplate(@RequestBody @Validated LabelBindingMatchParam param) {
        Long tenantId = TenantContext.get();
        Long templateId = labelBindingService.matchTemplate(
                param.getFactoryId(),
                param.getTemplateType(),
                param.getMaterialId(),
                param.getSupplierId(),
                param.getCustomerId(),
                tenantId
        );
        return R.ok(templateId);
    }
}

