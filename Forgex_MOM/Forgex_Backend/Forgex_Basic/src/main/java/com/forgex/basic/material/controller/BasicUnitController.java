package com.forgex.basic.material.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.material.domain.entity.BasicUnit;
import com.forgex.basic.material.domain.entity.BasicUnitType;
import com.forgex.basic.material.domain.param.UnitConversionSaveParam;
import com.forgex.basic.material.domain.param.UnitPageParam;
import com.forgex.basic.material.domain.param.UnitTypeParam;
import com.forgex.basic.material.domain.vo.UnitConversionVO;
import com.forgex.basic.material.domain.vo.UnitTypeTreeVO;
import com.forgex.basic.material.domain.vo.UnitVO;
import com.forgex.basic.material.service.IBasicUnitService;
import com.forgex.common.audit.OperationLog;
import com.forgex.common.audit.OperationType;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 计量单位管理控制器。
 * <p>
 * 提供计量单位类型树、单位主数据和换算关系维护接口。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-02
 */
@RestController
@RequestMapping("/basic/unit")
@RequiredArgsConstructor
public class BasicUnitController {

    private final IBasicUnitService unitService;

    @RequirePerm("basic:unit:query")
    @PostMapping("/type/tree")
    public R<List<UnitTypeTreeVO>> typeTree() {
        return R.ok(unitService.typeTree());
    }

    @RequirePerm("basic:unit:query")
    @PostMapping("/type/detail")
    public R<BasicUnitType> typeDetail(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.typeDetail(id(body)));
    }

    @RequirePerm("basic:unit:add")
    @PostMapping("/type/create")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.ADD)
    public R<Long> createType(@RequestBody UnitTypeParam param) {
        return R.ok(unitService.createType(param));
    }

    @RequirePerm("basic:unit:edit")
    @PostMapping("/type/update")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.UPDATE)
    public R<Boolean> updateType(@RequestBody UnitTypeParam param) {
        return R.ok(unitService.updateType(param));
    }

    @RequirePerm("basic:unit:delete")
    @PostMapping("/type/delete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Boolean> deleteType(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.deleteType(id(body)));
    }

    @RequirePerm("basic:unit:query")
    @PostMapping("/page")
    public R<Page<UnitVO>> page(@RequestBody(required = false) UnitPageParam param) {
        return R.ok(unitService.pageUnits(param));
    }

    @PostMapping("/list")
    public R<List<UnitVO>> list(@RequestBody(required = false) UnitPageParam param) {
        return R.ok(unitService.listUnits(param));
    }

    @RequirePerm("basic:unit:query")
    @PostMapping("/detail")
    public R<UnitVO> detail(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.detail(id(body)));
    }

    @RequirePerm("basic:unit:add")
    @PostMapping("/create")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.ADD)
    public R<Long> create(@RequestBody BasicUnit unit) {
        return R.ok(unitService.create(unit));
    }

    @RequirePerm("basic:unit:edit")
    @PostMapping("/update")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.UPDATE)
    public R<Boolean> update(@RequestBody BasicUnit unit) {
        return R.ok(unitService.updateUnit(unit));
    }

    @RequirePerm("basic:unit:delete")
    @PostMapping("/delete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Boolean> delete(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.deleteUnit(id(body)));
    }

    @RequirePerm("basic:unit:delete")
    @PostMapping("/batchDelete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Boolean> batchDelete(@RequestBody Map<String, List<Long>> body) {
        return R.ok(unitService.batchDeleteUnits(body.get("ids")));
    }

    @RequirePerm("basic:unit:query")
    @PostMapping("/conversion/list")
    public R<List<UnitConversionVO>> listConversions(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.listConversions(Long.valueOf(String.valueOf(body.get("unitId")))));
    }

    @RequirePerm("basic:unit:edit")
    @PostMapping("/conversion/save")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.UPDATE)
    public R<Boolean> saveConversions(@RequestBody UnitConversionSaveParam param) {
        return R.ok(unitService.saveConversions(param));
    }

    @RequirePerm("basic:unit:edit")
    @PostMapping("/conversion/delete")
    @OperationLog(module = "basic", menuPath = "/basic/unit", operationType = OperationType.DELETE)
    public R<Boolean> deleteConversion(@RequestBody Map<String, Object> body) {
        return R.ok(unitService.deleteConversion(id(body)));
    }

    private Long id(Map<String, Object> body) {
        Object value = body == null ? null : body.get("id");
        return value == null ? null : Long.valueOf(String.valueOf(value));
    }
}
