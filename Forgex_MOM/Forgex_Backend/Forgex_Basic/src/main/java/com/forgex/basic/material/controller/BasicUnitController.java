
package com.forgex.basic.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.param.UnitPageParam;
import com.forgex.basic.material.service.IBasicUnitService;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 计量单位控制器
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-28
 */
@RestController
@RequestMapping("/basic/unit")
@RequiredArgsConstructor
public class BasicUnitController {

    private final IBasicUnitService unitService;

    @RequirePerm("basic:unit:query")
    @PostMapping("/page")
    public R<Page<BasicUnit>> page(@RequestBody UnitPageParam param) {
        return R.ok(unitService.pageUnits(param));
    }

    @RequirePerm("basic:unit:query")
    @PostMapping("/detail")
    public R<BasicUnit> detail(@RequestBody java.util.Map<String, Long> body) {
        return R.ok(unitService.getById(body.get("id")));
    }

    @RequirePerm("basic:unit:add")
    @PostMapping("/create")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.ADD)
    public R<Long> create(@RequestBody BasicUnit unit) {
        unit.setTenantId(TenantContext.get());
        unitService.save(unit);
        return R.ok(unit.getId());
    }

    @RequirePerm("basic:unit:edit")
    @PostMapping("/update")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.UPDATE)
    public R<Void> update(@RequestBody BasicUnit unit) {
        unitService.updateById(unit);
        return R.ok();
    }

    @RequirePerm("basic:unit:delete")
    @PostMapping("/delete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Void> delete(@RequestBody java.util.Map<String, Long> body) {
        unitService.removeById(body.get("id"));
        return R.ok();
    }

    @RequirePerm("basic:unit:delete")
    @PostMapping("/batchDelete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Void> batchDelete(@RequestBody java.util.Map<String, List<Long>> body) {
        unitService.removeBatchByIds(body.get("ids"));
        return R.ok();
    }

    @PostMapping("/list")
    public R<List<BasicUnit>> list() {
        Long tenantId = TenantContext.get();
        return R.ok(unitService.lambdaQuery()
                .eq(BasicUnit::getTenantId, tenantId)
                .eq(BasicUnit::getStatus, 1)
                .eq(BasicUnit::getDeleted, 0)
                .orderByAsc(BasicUnit::getSortOrder)
                .list());
    }
}
