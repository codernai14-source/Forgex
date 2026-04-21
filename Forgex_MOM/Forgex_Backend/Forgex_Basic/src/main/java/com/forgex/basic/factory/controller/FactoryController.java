package com.forgex.basic.factory.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.basic.factory.domain.dto.FactoryDTO;
import com.forgex.basic.factory.domain.param.FactoryPageParam;
import com.forgex.basic.factory.service.FactoryService;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 工厂管理 Controller
 * <p>
 * 提供工厂管理的 HTTP 接口，包括工厂的分页查询、详情获取、列表查询等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-20
 * @see FactoryService
 */
@Tag(name = "工厂管理", description = "工厂管理接口")
@RestController
@RequestMapping("/basic/factory")
@RequiredArgsConstructor
public class FactoryController {

    private final FactoryService factoryService;

    /**
     * 分页查询工厂列表
     * <p>
     * 根据查询条件分页获取工厂列表。
     * </p>
     *
     * @param param 查询参数，包含分页信息和筛选条件
     * @return 工厂分页列表
     */
    @Operation(summary = "分页查询工厂列表", description = "根据条件分页查询工厂列表")
    @RequirePerm("basic:factory:query")
    @PostMapping("/page")
    public R<Page<FactoryDTO>> page(@RequestBody FactoryPageParam param) {
        Page<FactoryDTO> page = factoryService.page(param);
        return R.ok(page);
    }

    /**
     * 查询工厂列表（不分页）
     * <p>
     * 查询所有启用的工厂列表，用于下拉框选择。
     * 用于下拉框选择时不需要特殊权限
     * </p>
     *
     * @param param 查询参数，包含筛选条件
     * @return 工厂列表
     */
    @Operation(summary = "查询工厂列表", description = "查询所有启用的工厂列表，用于下拉框选择")
    @PostMapping("/list")
    public R<List<FactoryDTO>> list(@RequestBody(required = false) FactoryPageParam param) {
        List<FactoryDTO> list = factoryService.list(param);
        return R.ok(list);
    }

    /**
     * 获取工厂详情
     * <p>
     * 根据工厂 ID 获取详细信息。
     * </p>
     *
     * @param params 参数，包含工厂 ID
     * @return 工厂详情
     */
    @Operation(summary = "获取工厂详情", description = "根据 ID 获取工厂详细信息")
    @RequirePerm("basic:factory:query")
    @PostMapping("/detail")
    public R<FactoryDTO> detail(@RequestBody Map<String, Object> params) {
        Long id = (Long) params.get("id");
        FactoryDTO detail = factoryService.getDetailById(id);
        return R.ok(detail);
    }
}
