package com.forgex.integration.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.web.R;
import com.forgex.integration.domain.dto.ThirdAuthorizationDTO;
import com.forgex.integration.domain.param.ThirdAuthorizationParam;
import com.forgex.integration.service.IThirdAuthorizationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 第三方授权管理控制器
 * <p>
 * 提供第三方授权的增删改查、Token 生成、Token 校验、白名单校验等 RESTful API 接口
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0
 * @since 2026-04-14
 */
@RestController
@RequestMapping("/api/integration/third-authorization")
@RequiredArgsConstructor
@Tag(name = "第三方授权管理", description = "提供第三方授权的增删改查、Token 生成、Token 校验等功能")
public class ThirdAuthorizationController {

    private final IThirdAuthorizationService thirdAuthorizationService;

    /**
     * 分页查询第三方授权列表
     * <p>
     * 支持按第三方系统 ID、授权方式、状态等条件查询
     * </p>
     *
     * @param param 查询参数
     * @return 分页结果
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询第三方授权列表", description = "支持按第三方系统 ID、授权方式、状态等条件查询")
    public R<Page<ThirdAuthorizationDTO>> pageThirdAuthorizations(@RequestBody @Validated ThirdAuthorizationParam param) {
        Page<ThirdAuthorizationDTO> page = thirdAuthorizationService.pageThirdAuthorizations(param);
        return R.ok(page);
    }

    /**
     * 查询第三方授权列表（不分页）
     * <p>
     * 用于下拉框选择等场景
     * </p>
     *
     * @param param 查询参数
     * @return 授权列表
     */
    @PostMapping("/list")
    @Operation(summary = "查询第三方授权列表", description = "不分页查询，用于下拉框选择")
    public R<List<ThirdAuthorizationDTO>> listThirdAuthorizations(@RequestBody ThirdAuthorizationParam param) {
        List<ThirdAuthorizationDTO> list = thirdAuthorizationService.listThirdAuthorizations(param);
        return R.ok(list);
    }

    /**
     * 根据 ID 获取第三方授权详情
     * <p>
     * 用于编辑时回显数据
     * </p>
     *
     * @param id 授权 ID
     * @return 授权详情
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取第三方授权详情", description = "根据 ID 查询授权详细信息")
    public R<ThirdAuthorizationDTO> getThirdAuthorizationDetail(@PathVariable Long id) {
        ThirdAuthorizationDTO dto = thirdAuthorizationService.getThirdAuthorizationById(id);
        return R.ok(dto);
    }

    /**
     * 根据第三方系统 ID 获取授权信息
     * <p>
     * 用于查看某个第三方系统的授权配置
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @return 授权信息
     */
    @GetMapping("/by-system/{thirdSystemId}")
    @Operation(summary = "根据第三方系统 ID 获取授权信息", description = "查询指定第三方系统的授权配置")
    public R<ThirdAuthorizationDTO> getByThirdSystemId(@PathVariable Long thirdSystemId) {
        ThirdAuthorizationDTO dto = thirdAuthorizationService.getByThirdSystemId(thirdSystemId);
        return R.ok(dto);
    }

    /**
     * 创建第三方授权
     * <p>
     * 自动校验第三方系统是否已存在授权配置
     * </p>
     *
     * @param dto 授权信息
     * @return 创建结果
     */
    @PostMapping("/create")
    @Operation(summary = "创建第三方授权", description = "新增第三方授权信息")
    public R<Void> createThirdAuthorization(@RequestBody @Validated ThirdAuthorizationDTO dto) {
        thirdAuthorizationService.createThirdAuthorization(dto);
        return R.ok();
    }

    /**
     * 更新第三方授权
     * <p>
     * 自动校验授权是否存在
     * </p>
     *
     * @param dto 授权信息
     * @return 更新结果
     */
    @PostMapping("/update")
    @Operation(summary = "更新第三方授权", description = "修改第三方授权信息")
    public R<Void> updateThirdAuthorization(@RequestBody @Validated ThirdAuthorizationDTO dto) {
        thirdAuthorizationService.updateThirdAuthorization(dto);
        return R.ok();
    }

    /**
     * 删除第三方授权
     * <p>
     * 逻辑删除授权记录
     * </p>
     *
     * @param id 授权 ID
     * @return 删除结果
     */
    @PostMapping("/delete/{id}")
    @Operation(summary = "删除第三方授权", description = "逻辑删除第三方授权")
    public R<Void> deleteThirdAuthorization(@PathVariable Long id) {
        thirdAuthorizationService.deleteThirdAuthorization(id);
        return R.ok();
    }

    /**
     * 批量删除第三方授权
     * <p>
     * 支持批量删除多个授权记录
     * </p>
     *
     * @param ids 授权 ID 列表
     * @return 删除结果
     */
    @PostMapping("/batch-delete")
    @Operation(summary = "批量删除第三方授权", description = "批量删除多个第三方授权")
    public R<Void> batchDeleteThirdAuthorizations(@RequestBody List<Long> ids) {
        thirdAuthorizationService.batchDeleteThirdAuthorizations(ids);
        return R.ok();
    }

    /**
     * 生成 Token
     * <p>
     * 为指定第三方系统生成新的 Token，并设置过期时间
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @param expireHours Token 有效期（小时），null 表示永不过期
     * @return 生成的 Token 值
     */
    @PostMapping("/generate-token/{thirdSystemId}")
    @Operation(summary = "生成 Token", description = "为指定第三方系统生成新的 Token")
    public R<String> generateToken(@PathVariable Long thirdSystemId, 
                                   @RequestParam(required = false) Integer expireHours) {
        String tokenValue = thirdAuthorizationService.generateToken(thirdSystemId, expireHours);
        R<String> result = R.ok(tokenValue);
        result.setMessage("Token 生成成功");
        return result;
    }

    /**
     * 校验 Token
     * <p>
     * 校验 Token 是否有效（是否存在、是否过期、是否启用）
     * </p>
     *
     * @param tokenValue Token 值
     * @return true-有效，false-无效
     */
    @GetMapping("/validate-token")
    @Operation(summary = "校验 Token", description = "校验 Token 是否有效")
    public R<Boolean> validateToken(@RequestParam String tokenValue) {
        boolean isValid = thirdAuthorizationService.validateToken(tokenValue);
        R<Boolean> result = R.ok(isValid);
        result.setMessage(isValid ? "Token 有效" : "Token 无效");
        return result;
    }

    /**
     * 校验 IP 白名单
     * <p>
     * 校验指定 IP 是否在第三方系统的白名单中
     * </p>
     *
     * @param thirdSystemId 第三方系统 ID
     * @param ipAddress IP 地址，为空时从请求中自动获取
     * @return true-在白名单中，false-不在白名单中
     */
    @GetMapping("/check-ip-whitelist/{thirdSystemId}")
    @Operation(summary = "校验 IP 白名单", description = "校验指定 IP 是否在白名单中")
    public R<Boolean> checkIpWhitelist(@PathVariable Long thirdSystemId,
                                       @RequestParam(required = false) String ipAddress,
                                       HttpServletRequest request) {
        // 如果没有传入 IP 地址，从请求中获取
        if (ipAddress == null || ipAddress.trim().isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        
        boolean inWhitelist = thirdAuthorizationService.checkIpWhitelist(thirdSystemId, ipAddress);
        R<Boolean> result = R.ok(inWhitelist);
        result.setMessage(inWhitelist ? "IP 在白名单中" : "IP 不在白名单中");
        return result;
    }

    /**
     * 刷新 Token 有效期
     * <p>
     * 更新 Token 的过期时间，适用于 Token 续期场景
     * </p>
     *
     * @param tokenValue Token 值
     * @param expireHours 新的有效期（小时）
     * @return 刷新结果
     */
    @PostMapping("/refresh-token")
    @Operation(summary = "刷新 Token 有效期", description = "更新 Token 的过期时间")
    public R<Void> refreshTokenExpire(@RequestParam String tokenValue,
                                      @RequestParam Integer expireHours) {
        thirdAuthorizationService.refreshTokenExpire(tokenValue, expireHours);
        return R.ok();
    }
}
