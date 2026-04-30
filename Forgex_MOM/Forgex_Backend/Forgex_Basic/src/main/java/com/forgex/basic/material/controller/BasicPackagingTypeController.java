package com.forgex.basic.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.material.domain.entity.BasicPackagingType;
import com.forgex.basic.material.domain.param.PackagingTypePageParam;
import com.forgex.basic.material.service.IBasicPackagingTypeService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 包装方式控制器
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@RestController
@RequestMapping("/basic/packaging")
@RequiredArgsConstructor
public class BasicPackagingTypeController {

    private final IBasicPackagingTypeService packagingTypeService;

    @RequirePerm("basic:packaging:query")
    @PostMapping("/page")
    public R<Page<BasicPackagingType>> page(@RequestBody PackagingTypePageParam param) {
        return R.ok(packagingTypeService.pagePackagingTypes(param));
    }

    @RequirePerm("basic:packaging:query")
    @PostMapping("/detail")
    public R<BasicPackagingType> detail(@RequestBody java.util.Map<String, Long> body) {
        return R.ok(packagingTypeService.getById(body.get("id")));
    }

    @RequirePerm("basic:packaging:add")
    @PostMapping("/create")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.ADD)
    public R<Long> create(@RequestBody BasicPackagingType packagingType) {
        packagingType.setTenantId(TenantContext.get());
        packagingTypeService.save(packagingType);
        return R.ok(packagingType.getId());
    }

    @RequirePerm("basic:packaging:edit")
    @PostMapping("/update")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.UPDATE)
    public R<Void> update(@RequestBody BasicPackagingType packagingType) {
        packagingTypeService.updateById(packagingType);
        return R.ok();
    }

    @RequirePerm("basic:packaging:delete")
    @PostMapping("/delete")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.DELETE)
    public R<Void> delete(@RequestBody java.util.Map<String, Long> body) {
        packagingTypeService.removeById(body.get("id"));
        return R.ok();
    }

    @RequirePerm("basic:packaging:delete")
    @PostMapping("/batchDelete")
    @OperationLog(module = "basic", menuPath = "/basic/packaging", operationType = OperationType.DELETE)
    public R<Void> batchDelete(@RequestBody java.util.Map<String, List<Long>> body) {
        packagingTypeService.removeBatchByIds(body.get("ids"));
        return R.ok();
    }

    @PostMapping("/list")
    public R<List<BasicPackagingType>> list() {
        Long tenantId = TenantContext.get();
        return R.ok(packagingTypeService.lambdaQuery()
                .eq(BasicPackagingType::getTenantId, tenantId)
                .eq(BasicPackagingType::getStatus, 1)
                .eq(BasicPackagingType::getDeleted, 0)
                .orderByAsc(BasicPackagingType::getSortOrder)
                .list());
    }
}
