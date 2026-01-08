/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
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
