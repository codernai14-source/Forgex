package com.forgex.sys.controller;

import com.forgex.common.license.LicenseManager;
import com.forgex.common.license.LicenseRequestInfo;
import com.forgex.common.license.LicenseRuntimeInfo;
import com.forgex.common.web.R;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 软件授权控制器
 * <p>
 * 提供授权状态、授权请求信息与授权历史的读取能力，供安装后验收和系统管理页面使用。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/sys/license")
public class LicenseController {

    private final LicenseManager licenseManager;

    public LicenseController(LicenseManager licenseManager) {
        this.licenseManager = licenseManager;
    }

    /**
     * 获取当前授权状态。
     *
     * @return 授权状态
     */
    @GetMapping("/status")
    public R<LicenseRuntimeInfo> getStatus() {
        return R.ok(licenseManager.current());
    }

    /**
     * 获取授权请求信息。
     *
     * @return 授权请求信息
     */
    @GetMapping("/request-info")
    public R<LicenseRequestInfo> getRequestInfo() {
        return R.ok(licenseManager.getRequestInfo());
    }

    /**
     * 获取授权历史记录。
     *
     * @return 授权历史列表
     */
    @GetMapping("/logs")
    public R<List<Map<String, Object>>> getLogs() {
        return R.ok(licenseManager.readHistory());
    }

    /**
     * 强制刷新授权缓存。
     *
     * @return 最新授权状态
     */
    @PostMapping("/refresh")
    public R<LicenseRuntimeInfo> refresh() {
        return R.ok(licenseManager.refresh());
    }
}
