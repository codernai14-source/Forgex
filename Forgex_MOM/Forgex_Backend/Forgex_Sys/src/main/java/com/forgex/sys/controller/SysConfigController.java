package com.forgex.sys.controller;

import com.forgex.common.config.ConfigService;
import com.forgex.common.web.R;
import com.forgex.sys.domain.config.CaptchaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器。
 * <p>
 * 提供登录相关配置的读取与更新接口（验证码配置）。
 * Controller 仅负责参数接收与结果返回，配置持久化由配置服务完成。
 */
@RestController
@RequestMapping("/sys/config")
public class SysConfigController {
    @Autowired
    private ConfigService configService;

    /**
     * 获取登录验证码配置。
     * @return 验证码配置
     */
    @GetMapping("/login-captcha")
    public R<CaptchaConfig> getLoginCaptcha() {
        CaptchaConfig c = configService.getJson("login.captcha", CaptchaConfig.class, CaptchaConfig.defaults());
        return R.ok(c);
    }

    /**
     * 更新登录验证码配置。
     * @param body 配置体（JSON）
     * @return 是否成功
     */
    @PutMapping("/login-captcha")
    public R<Boolean> setLoginCaptcha(@RequestBody Map<String, Object> body) {
        configService.setJson("login.captcha", body);
        return R.ok(true);
    }
}
