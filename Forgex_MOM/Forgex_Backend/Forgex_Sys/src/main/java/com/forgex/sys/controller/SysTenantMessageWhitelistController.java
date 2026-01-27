package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantMessageWhitelist;
import com.forgex.sys.mapper.SysTenantMessageWhitelistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 租户消息白名单管理控制器
 * <p>
 * 提供租户消息白名单的增删改查功能，用于管理跨租户消息发送权限。
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/tenant-message-whitelist")
@RequiredArgsConstructor
public class SysTenantMessageWhitelistController {

    private final SysTenantMessageWhitelistMapper whitelistMapper;

    /**
     * 分页查询租户消息白名单
     * 
     * @param current 当前页
     * @param size 每页大小
     * @param senderTenantId 发送方租户ID（可选）
     * @param receiverTenantId 接收方租户ID（可选）
     * @param enabled 是否启用（可选）
     * @return 分页结果
     */
    @GetMapping("/page")
    public R<Page<SysTenantMessageWhitelist>> page(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) Long senderTenantId,
            @RequestParam(required = false) Long receiverTenantId,
            @RequestParam(required = false) Boolean enabled) {
        
        LambdaQueryWrapper<SysTenantMessageWhitelist> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(senderTenantId != null, SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
               .eq(receiverTenantId != null, SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
               .eq(enabled != null, SysTenantMessageWhitelist::getEnabled, enabled)
               .orderByDesc(SysTenantMessageWhitelist::getCreateTime);
        
        Page<SysTenantMessageWhitelist> page = new Page<>(current, size);
        Page<SysTenantMessageWhitelist> result = whitelistMapper.selectPage(page, wrapper);
        
        return R.ok(result);
    }

    /**
     * 根据ID查询白名单配置
     * 
     * @param id 白名单ID
     * @return 白名单配置
     */
    @GetMapping("/{id}")
    public R<SysTenantMessageWhitelist> getById(@PathVariable Long id) {
        SysTenantMessageWhitelist whitelist = whitelistMapper.selectById(id);
        return R.ok(whitelist);
    }

    /**
     * 新增白名单配置
     * 
     * @param whitelist 白名单配置
     * @return 操作结果
     */
    @PostMapping
    public R<Boolean> save(@RequestBody SysTenantMessageWhitelist whitelist) {
        // 检查是否已存在相同配置
        Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                .eq(SysTenantMessageWhitelist::getSenderTenantId, whitelist.getSenderTenantId())
                .eq(SysTenantMessageWhitelist::getReceiverTenantId, whitelist.getReceiverTenantId())
                .eq(SysTenantMessageWhitelist::getDeleted, false));
        
        if (count != null && count > 0) {
            return R.fail(CommonPrompt.ALREADY_EXISTS);
        }
        
        int rows = whitelistMapper.insert(whitelist);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 更新白名单配置
     * 
     * @param whitelist 白名单配置
     * @return 操作结果
     */
    @PutMapping
    public R<Boolean> update(@RequestBody SysTenantMessageWhitelist whitelist) {
        int rows = whitelistMapper.updateById(whitelist);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 删除白名单配置
     * 
     * @param id 白名单ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        int rows = whitelistMapper.deleteById(id);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 启用/禁用白名单配置
     * 
     * @param id 白名单ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PutMapping("/{id}/enabled")
    public R<Boolean> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        SysTenantMessageWhitelist whitelist = new SysTenantMessageWhitelist();
        whitelist.setId(id);
        whitelist.setEnabled(enabled);
        
        int rows = whitelistMapper.updateById(whitelist);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 检查跨租户消息权限
     * 
     * @param senderTenantId 发送方租户ID
     * @param receiverTenantId 接收方租户ID
     * @return 是否有权限
     */
    @GetMapping("/check-permission")
    public R<Boolean> checkPermission(
            @RequestParam Long senderTenantId,
            @RequestParam Long receiverTenantId) {
        
        // 同一租户，直接允许
        if (senderTenantId.equals(receiverTenantId)) {
            return R.ok(true);
        }
        
        // 超级管理员租户（ID=1）默认拥有所有权限
        if (senderTenantId == 1L) {
            return R.ok(true);
        }
        
        // 查询白名单
        Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                .eq(SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
                .eq(SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
                .eq(SysTenantMessageWhitelist::getEnabled, true)
                .eq(SysTenantMessageWhitelist::getDeleted, false));
        
        return R.ok(count != null && count > 0);
    }
}



