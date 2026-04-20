package com.forgex.basic.customer.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.customer.domain.dto.CustomerDTO;
import com.forgex.basic.customer.domain.param.CustomerPageParam;
import com.forgex.basic.customer.service.ICustomerService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 客户管理 Controller
 * <p>
 * 提供客户管理的 HTTP 接口，包括客户的分页查询、详情获取、列表查询等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 * @see ICustomerService
 */
@Tag(name = "客户管理", description = "客户管理接口")
@RestController
@RequestMapping("/basic/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;

    /**
     * 分页查询客户列表
     * <p>
     * 根据查询条件分页获取客户列表。
     * </p>
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 客户分页列表
     */
    @Operation(summary = "分页查询客户列表", description = "根据条件分页查询客户列表")
    @RequirePerm("basic:customer:query")
    @PostMapping("/page")
    public R<Page<CustomerDTO>> page(@RequestBody CustomerPageParam param) {
        Page<CustomerDTO> page = customerService.page(param);
        return R.ok(page);
    }

    /**
     * 查询客户列表（不分页）
     * <p>
     * 查询所有启用的客户列表，用于下拉框选择。
     * 用于下拉框选择时不需要特殊权限
     * </p>
     *
     * @param param 查询参数，包含筛选条件
     * @return 客户列表
     */
    @Operation(summary = "查询客户列表", description = "查询所有启用的客户列表，用于下拉框选择")
    @PostMapping("/list")
    public R<List<CustomerDTO>> list(@RequestBody(required = false) CustomerPageParam param) {
        List<CustomerDTO> list = customerService.list(param);
        return R.ok(list);
    }

    /**
     * 获取客户详情
     * <p>
     * 根据客户 ID 获取详细信息。
     * </p>
     *
     * @param params 参数，包含客户 ID
     * @return 客户详情
     */
    @Operation(summary = "获取客户详情", description = "根据 ID 获取客户详细信息")
    @RequirePerm("basic:customer:query")
    @PostMapping("/detail")
    public R<CustomerDTO> detail(@RequestBody Map<String, Object> params) {
        Long id = (Long) params.get("id");
        CustomerDTO detail = customerService.getDetailById(id);
        return R.ok(detail);
    }
}
