package com.forgex.basic.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.MaterialPackagingSaveParam;
import com.forgex.basic.material.domain.param.MaterialPackagingSlotSaveParam;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;
import com.forgex.basic.material.domain.vo.MaterialPackagingRelationVO;
import com.forgex.basic.material.domain.vo.PackagingTypeVO;
import com.forgex.basic.material.service.IBasicPackagingTypeService;
import com.forgex.basic.material.service.IMaterialPackagingRelationService;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 包装规格控制器。
 * <p>
 * 提供包装规格主数据 CRUD 与物料三槽包装关联维护接口。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@RestController
@RequestMapping("/packaging")
@RequiredArgsConstructor
public class BasicPackagingTypeController {

    private final IBasicPackagingTypeService packagingTypeService;
    private final IMaterialPackagingRelationService relationService;

    /**
     * 分页查询包装规格。
     *
     * @param param 查询参数
     * @return 包装规格分页列表
     */
    @RequirePerm("basic:packaging:query")
    @PostMapping("/page")
    public R<Page<PackagingTypeVO>> page(@RequestBody PackagingTypePageParam param) {
        return R.ok(packagingTypeService.pagePackagingTypes(TenantContext.get(), param));
    }

    /**
     * 查询包装规格详情。
     *
     * @param body 请求体，包含 id
     * @return 包装规格详情
     */
    @RequirePerm("basic:packaging:query")
    @PostMapping("/detail")
    public R<BasicPackagingType> detail(@RequestBody Map<String, Long> body) {
        return R.ok(packagingTypeService.getById(body.get("id")));
    }

    /**
     * 创建包装规格。
     *
     * @param packagingType 包装规格
     * @return 包装规格 ID
     */
    @RequirePerm("basic:packaging:add")
    @PostMapping("/create")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.ADD)
    public R<Long> create(@RequestBody BasicPackagingType packagingType) {
        return R.ok(packagingTypeService.createPackagingType(TenantContext.get(), packagingType));
    }

    /**
     * 更新包装规格。
     *
     * @param packagingType 包装规格
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:edit")
    @PostMapping("/update")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.UPDATE)
    public R<Void> update(@RequestBody BasicPackagingType packagingType) {
        packagingTypeService.updatePackagingType(TenantContext.get(), packagingType);
        return R.ok();
    }

    /**
     * 删除包装规格。
     *
     * @param body 请求体，包含 id
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:delete")
    @PostMapping("/delete")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.DELETE)
    public R<Void> delete(@RequestBody Map<String, Long> body) {
        packagingTypeService.deletePackagingType(TenantContext.get(), body.get("id"));
        return R.ok();
    }

    /**
     * 批量删除包装规格。
     *
     * @param body 请求体，包含 ids
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:batchDelete")
    @PostMapping("/batchDelete")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.DELETE)
    public R<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        packagingTypeService.batchDeletePackagingTypes(TenantContext.get(), body.get("ids"));
        return R.ok();
    }

    /**
     * 查询可用包装规格列表。
     *
     * @return 可用包装规格列表
     */
    @PostMapping("/list")
    public R<List<PackagingTypeVO>> list() {
        return R.ok(packagingTypeService.listAvailable(TenantContext.get()));
    }

    /**
     * 查询指定物料的包装规格三槽绑定。
     *
     * @param body 请求体，包含 materialId
     * @return 三槽绑定列表
     */
    @RequirePerm("basic:packaging:query")
    @PostMapping("/relation/listByMaterial")
    public R<List<MaterialPackagingRelationVO>> listByMaterial(@RequestBody Map<String, Long> body) {
        Long materialId = body.get("materialId");
        if (materialId == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        return R.ok(relationService.listByMaterial(TenantContext.get(), materialId));
    }

    /**
     * 查询指定包装规格关联的物料。
     *
     * @param body 请求体，包含 packagingTypeId
     * @return 关联物料列表
     */
    @RequirePerm("basic:packaging:query")
    @PostMapping("/relation/listByPackaging")
    public R<List<MaterialPackagingRelationVO>> listByPackaging(@RequestBody Map<String, Long> body) {
        Long packagingTypeId = body.get("packagingTypeId");
        if (packagingTypeId == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }
        return R.ok(relationService.listByPackagingType(TenantContext.get(), packagingTypeId));
    }

    /**
     * 保存指定物料的小、中、大包装规格。
     *
     * @param param 三槽保存参数
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:edit")
    @PostMapping("/relation/saveByMaterial")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.UPDATE)
    public R<Void> saveByMaterial(@RequestBody MaterialPackagingSaveParam param) {
        relationService.saveByMaterial(TenantContext.get(), param);
        return R.ok();
    }

    /**
     * 保存单个物料包装槽位绑定。
     *
     * @param param 单槽保存参数
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:edit")
    @PostMapping("/relation/saveSlot")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.UPDATE)
    public R<Void> saveSlot(@RequestBody MaterialPackagingSlotSaveParam param) {
        relationService.saveSlot(TenantContext.get(), param);
        return R.ok();
    }

    /**
     * 删除物料包装规格关联。
     *
     * @param body 请求体，包含 id
     * @return 统一响应
     */
    @RequirePerm("basic:packaging:edit")
    @PostMapping("/relation/delete")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.DELETE)
    public R<Void> deleteRelation(@RequestBody Map<String, Long> body) {
        relationService.deleteRelation(TenantContext.get(), body.get("id"));
        return R.ok();
    }
}
