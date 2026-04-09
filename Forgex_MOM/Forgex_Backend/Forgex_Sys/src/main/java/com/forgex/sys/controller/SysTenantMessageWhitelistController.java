package com.forgex.sys.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.security.perm.RequirePerm;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantMessageWhitelist;
import com.forgex.sys.mapper.SysTenantMessageWhitelistMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 绉熸埛娑堟伅鐧藉悕鍗曠鐞嗘帶鍒跺櫒
 * <p>
 * 鎻愪緵绉熸埛娑堟伅鐧藉悕鍗曠殑澧炲垹鏀规煡鍔熻兘锛岀敤浜庣鐞嗚法绉熸埛娑堟伅鍙戦€佹潈闄愩€?
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
     * 鍒嗛〉鏌ヨ绉熸埛娑堟伅鐧藉悕鍗?
     * 
     * @param current 褰撳墠椤?
     * @param size 姣忛〉澶у皬
     * @param senderTenantId 鍙戦€佹柟绉熸埛ID锛堝彲閫夛級
     * @param receiverTenantId 鎺ユ敹鏂圭鎴稩D锛堝彲閫夛級
     * @param enabled 鏄惁鍚敤锛堝彲閫夛級
     * @return 鍒嗛〉缁撴灉
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
     * 鏍规嵁ID鏌ヨ鐧藉悕鍗曢厤缃?
     * 
     * @param id 鐧藉悕鍗旾D
     * @return 鐧藉悕鍗曢厤缃?
     */
    @GetMapping("/{id}")
    public R<SysTenantMessageWhitelist> getById(@PathVariable Long id) {
        SysTenantMessageWhitelist whitelist = whitelistMapper.selectById(id);
        return R.ok(whitelist);
    }

    /**
     * 鏂板鐧藉悕鍗曢厤缃?
     * 
     * @param whitelist 鐧藉悕鍗曢厤缃?
     * @return 鎿嶄綔缁撴灉
     */
    @RequirePerm("sys:tenant-message-whitelist:create")
    @PostMapping
    public R<Boolean> save(@RequestBody SysTenantMessageWhitelist whitelist) {
        // 妫€鏌ユ槸鍚﹀凡瀛樺湪鐩稿悓閰嶇疆
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
     * 鏇存柊鐧藉悕鍗曢厤缃?
     * 
     * @param whitelist 鐧藉悕鍗曢厤缃?
     * @return 鎿嶄綔缁撴灉
     */
    @RequirePerm("sys:tenant-message-whitelist:update")
    @PutMapping
    public R<Boolean> update(@RequestBody SysTenantMessageWhitelist whitelist) {
        int rows = whitelistMapper.updateById(whitelist);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 鍒犻櫎鐧藉悕鍗曢厤缃?
     * 
     * @param id 鐧藉悕鍗旾D
     * @return 鎿嶄綔缁撴灉
     */
    @RequirePerm("sys:tenant-message-whitelist:delete")
    @DeleteMapping("/{id}")
    public R<Boolean> delete(@PathVariable Long id) {
        int rows = whitelistMapper.deleteById(id);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 鍚敤/绂佺敤鐧藉悕鍗曢厤缃?
     * 
     * @param id 鐧藉悕鍗旾D
     * @param enabled 鏄惁鍚敤
     * @return 鎿嶄綔缁撴灉
     */
    @RequirePerm("sys:tenant-message-whitelist:update")
    @PutMapping("/{id}/enabled")
    public R<Boolean> updateEnabled(@PathVariable Long id, @RequestParam Boolean enabled) {
        SysTenantMessageWhitelist whitelist = new SysTenantMessageWhitelist();
        whitelist.setId(id);
        whitelist.setEnabled(enabled);
        
        int rows = whitelistMapper.updateById(whitelist);
        return rows > 0 ? R.ok(true) : R.fail(CommonPrompt.OPERATION_FAILED);
    }

    /**
     * 妫€鏌ヨ法绉熸埛娑堟伅鏉冮檺
     * 
     * @param senderTenantId 鍙戦€佹柟绉熸埛ID
     * @param receiverTenantId 鎺ユ敹鏂圭鎴稩D
     * @return 鏄惁鏈夋潈闄?
     */
    @GetMapping("/check-permission")
    public R<Boolean> checkPermission(
            @RequestParam Long senderTenantId,
            @RequestParam Long receiverTenantId) {
        
        // 鍚屼竴绉熸埛锛岀洿鎺ュ厑璁?
        if (senderTenantId.equals(receiverTenantId)) {
            return R.ok(true);
        }
        
        // 瓒呯骇绠＄悊鍛樼鎴凤紙ID=1锛夐粯璁ゆ嫢鏈夋墍鏈夋潈闄?
        if (senderTenantId == 0L) {
            return R.ok(true);
        }
        
        // 鏌ヨ鐧藉悕鍗?
        Long count = whitelistMapper.selectCount(new LambdaQueryWrapper<SysTenantMessageWhitelist>()
                .eq(SysTenantMessageWhitelist::getSenderTenantId, senderTenantId)
                .eq(SysTenantMessageWhitelist::getReceiverTenantId, receiverTenantId)
                .eq(SysTenantMessageWhitelist::getEnabled, true)
                .eq(SysTenantMessageWhitelist::getDeleted, false));
        
        return R.ok(count != null && count > 0);
    }
}




