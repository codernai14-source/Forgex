package com.forgex.basic.supplier.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.supplier.domain.dto.SupplierDTO;
import com.forgex.basic.supplier.domain.param.SupplierPageParam;
import com.forgex.basic.supplier.service.ISupplierService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商管理 Controller
 * <p>
 * 提供供应商管理的 HTTP 接口，包括供应商的分页查询、详情获取、列表查询等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 * @see ISupplierService
 */
@Tag(name = "供应商管理", description = "供应商管理接口")
@RestController
@RequestMapping("/basic/supplier")
@RequiredArgsConstructor
public class SupplierController {

    private final ISupplierService supplierService;

    /**
     * 分页查询供应商列表
     * <p>
     * 根据查询条件分页获取供应商列表。
     * </p>
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 供应商分页列表
     */
    @Operation(summary = "分页查询供应商列表", description = "根据条件分页查询供应商列表")
    @RequirePerm("basic:supplier:query")
    @PostMapping("/page")
    public R<Page<SupplierDTO>> page(@RequestBody SupplierPageParam param) {
        Page<SupplierDTO> page = supplierService.page(param);
        return R.ok(page);
    }

    /**
     * 查询供应商列表（不分页）
     * <p>
     * 查询所有启用的供应商列表，用于下拉框选择。
     * 用于下拉框选择时不需要特殊权限
     * </p>
     *
     * @param param 查询参数，包含筛选条件
     * @return 供应商列表
     */
    @Operation(summary = "查询供应商列表", description = "查询所有启用的供应商列表，用于下拉框选择")
    @PostMapping("/list")
    public R<List<SupplierDTO>> list(@RequestBody(required = false) SupplierPageParam param) {
        List<SupplierDTO> list = supplierService.list(param);
        return R.ok(list);
    }

    /**
     * 获取供应商详情
     * <p>
     * 根据供应商 ID 获取详细信息。
     * </p>
     *
     * @param params 参数，包含供应商 ID
     * @return 供应商详情
     */
    @Operation(summary = "获取供应商详情", description = "根据 ID 获取供应商详细信息")
    @RequirePerm("basic:supplier:query")
    @PostMapping("/detail")
    public R<SupplierDTO> detail(@RequestBody Map<String, Object> params) {
        Long id = (Long) params.get("id");
        SupplierDTO detail = supplierService.getDetailById(id);
        return R.ok(detail);
    }
}
