package com.forgex.basic.controller;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.web.R;
import com.forgex.basic.domain.dto.MaterialDTO;
import com.forgex.basic.domain.param.MaterialPageParam;
import com.forgex.basic.domain.response.MaterialDetailResponse;
import com.forgex.basic.domain.vo.MaterialVO;
import com.forgex.basic.service.IMaterialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物料管理 Controller
 * <p>
 * 提供物料管理的 HTTP 接口，包括物料的分页查询、详情获取、新增、修改、删除等操作。
 * 所有接口统一使用 POST 方法，参数统一封装为对象。
 * </p>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #page(MaterialPageParam)} - 分页查询物料列表</li>
 *   <li>{@link #detail(Map)} - 根据 ID 获取物料详情</li>
 *   <li>{@link #create(MaterialDTO)} - 新增物料</li>
 *   <li>{@link #update(MaterialDTO)} - 更新物料</li>
 *   <li>{@link #delete(Map)} - 删除物料</li>
 * </ul>
 * <p>接口说明：</p>
 * <ul>
 *   <li>所有接口路径：/basic/material/*</li>
 *   <li>所有接口均为 POST 请求</li>
 *   <li>需要登录认证</li>
 *   <li>新增、修改、删除操作需要对应权限</li>
 *   <li>返回格式统一为 {@link R} 类型</li>
 * </ul>
 *
 * @author LiDaoMoM
 * @version 1.0
 * @since 2026-04-09
 * @see IMaterialService
 */
@Tag(name = "物料管理", description = "物料管理接口")
@RestController
@RequestMapping("/basic/material")
@RequiredArgsConstructor
public class BasicMaterialController {

    private final IMaterialService materialService;

    /**
     * 分页查询物料列表
     * <p>
     * 接口路径：POST /basic/material/page
     * 需要权限：basic:material:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收分页参数和查询条件（pageNum/pageSize/物料编码/物料名称等）</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>调用 Service 层分页查询物料</li>
     *   <li>返回分页结果，包含物料列表和总数</li>
     * </ol>
     *
     * @param param 查询参数
     *              - pageNum: 页码，从 1 开始
     *              - pageSize: 每页条数，默认 10
     *              - materialCode: 物料编码（可选，模糊查询）
     *              - materialName: 物料名称（可选，模糊查询）
     *              - materialType: 物料类型（可选，精确匹配）
     *              - materialCategory: 物料分类（可选，精确匹配）
     *              - status: 状态（可选）
     *              - approvalStatus: 审批状态（可选）
     * @return {@link R} 包含物料分页列表的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 物料分页列表（IPage&lt;MaterialVO&gt;）
     */
    @Operation(summary = "分页查询物料列表", description = "支持多条件筛选，返回物料 VO 列表")
    @RequirePerm("basic:material:query")
    @PostMapping("/page")
    public R<IPage<MaterialVO>> page(@RequestBody @Validated MaterialPageParam param) {
        Long tenantId = TenantContext.get();
        IPage<MaterialVO> page = materialService.pageMaterials(tenantId, param);
        return R.ok(page);
    }

    /**
     * 根据 ID 查询物料详情
     * <p>
     * 接口路径：POST /basic/material/detail
     * 需要权限：basic:material:query
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中提取物料 ID</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>调用 Service 层查询物料详情（包含扩展信息）</li>
     *   <li>返回物料详情响应</li>
     * </ol>
     *
     * @param body 请求体参数
     *             - id: 物料 ID（必填）
     * @return {@link R} 包含物料详情的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 物料详情（MaterialDetailResponse）
     */
    @Operation(summary = "查询物料详情", description = "返回物料主表信息和所有扩展信息")
    @RequirePerm("basic:material:query")
    @PostMapping("/detail")
    public R<MaterialDetailResponse> detail(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        MaterialDetailResponse response = materialService.getMaterialDetail(tenantId, id);
        return R.ok(response);
    }

    /**
     * 创建物料
     * <p>
     * 接口路径：POST /basic/material/create
     * 需要权限：basic:material:add
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收物料 DTO 参数</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>校验参数合法性</li>
     *   <li>调用 Service 层创建物料</li>
     *   <li>如果未提供物料编码，自动生成</li>
     *   <li>保存物料主表和扩展信息</li>
     *   <li>返回新创建的物料 ID</li>
     * </ol>
     *
     * @param dto 物料 DTO
     *            - materialCode: 物料编码（可选，不填则自动生成）
     *            - materialName: 物料名称（必填）
     *            - materialType: 物料类型（必填）
     *            - materialCategory: 物料分类（必填）
     *            - unit: 单位（可选）
     *            - specification: 规格型号（可选）
     *            - status: 状态（可选，默认启用）
     *            - extendList: 扩展信息列表（可选）
     * @return {@link R} 包含物料 ID 的统一返回结构
     *         - code: 状态码（200=成功）
     *         - data: 物料 ID（Long）
     */
    @Operation(summary = "创建物料", description = "支持自动生成物料编码，支持保存扩展信息")
    @RequirePerm("basic:material:add")
    @PostMapping("/create")
    public R<Long> create(@RequestBody @Validated MaterialDTO dto) {
        Long tenantId = TenantContext.get();
        Long materialId = materialService.createMaterial(tenantId, dto);
        return R.ok(materialId);
    }

    /**
     * 更新物料
     * <p>
     * 接口路径：POST /basic/material/update
     * 需要权限：basic:material:edit
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>接收物料 DTO 参数</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>校验物料是否存在</li>
     *   <li>校验物料编码唯一性（排除自身）</li>
     *   <li>调用 Service 层更新物料</li>
     *   <li>更新物料主表和扩展信息</li>
     * </ol>
     *
     * @param dto 物料 DTO
     *            - id: 物料 ID（必填）
     *            - materialCode: 物料编码（可选）
     *            - materialName: 物料名称（可选）
     *            - materialType: 物料类型（可选）
     *            - materialCategory: 物料分类（可选）
     *            - unit: 单位（可选）
     *            - specification: 规格型号（可选）
     *            - status: 状态（可选）
     *            - extendList: 扩展信息列表（可选）
     * @return {@link R} 统一返回结构
     *         - code: 状态码（200=成功）
     *         - message: 操作结果消息
     */
    @Operation(summary = "更新物料", description = "支持更新主表信息和扩展信息")
    @RequirePerm("basic:material:edit")
    @PostMapping("/update")
    public R<Void> update(@RequestBody @Validated MaterialDTO dto) {
        if (dto.getId() == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        materialService.updateMaterial(tenantId, dto);
        return R.ok();
    }

    /**
     * 删除物料
     * <p>
     * 接口路径：POST /basic/material/delete
     * 需要权限：basic:material:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中提取物料 ID</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>校验物料是否存在</li>
     *   <li>调用 Service 层逻辑删除物料</li>
     *   <li>同时删除关联的扩展信息</li>
     * </ol>
     *
     * @param body 请求体参数
     *             - id: 物料 ID（必填）
     * @return {@link R} 统一返回结构
     *         - code: 状态码（200=成功）
     *         - message: 操作结果消息
     */
    @Operation(summary = "删除物料", description = "逻辑删除，同时删除关联的扩展信息")
    @RequirePerm("basic:material:delete")
    @PostMapping("/delete")
    public R<Void> delete(@RequestBody Map<String, Long> body) {
        Long id = body.get("id");
        if (id == null) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        materialService.deleteMaterial(tenantId, id);
        return R.ok();
    }

    /**
     * 批量删除物料
     * <p>
     * 接口路径：POST /basic/material/batchDelete
     * 需要权限：basic:material:delete
     * </p>
     * <p>执行步骤：</p>
     * <ol>
     *   <li>从请求体中提取物料 ID 列表</li>
     *   <li>从 TenantContext 获取当前租户 ID</li>
     *   <li>遍历 ID 列表，逐个删除物料</li>
     * </ol>
     *
     * @param body 请求体参数
     *             - ids: 物料 ID 列表（必填）
     * @return {@link R} 统一返回结构
     *         - code: 状态码（200=成功）
     *         - message: 操作结果消息
     */
    @Operation(summary = "批量删除物料", description = "批量逻辑删除物料")
    @RequirePerm("basic:material:delete")
    @PostMapping("/batchDelete")
    public R<Void> batchDelete(@RequestBody Map<String, List<Long>> body) {
        List<Long> ids = body.get("ids");
        if (ids == null || ids.isEmpty()) {
            return R.fail(CommonPrompt.PARAM_EMPTY);
        }

        Long tenantId = TenantContext.get();
        for (Long id : ids) {
            materialService.deleteMaterial(tenantId, id);
        }

        return R.ok();
    }
}
