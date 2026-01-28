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

import com.forgex.common.web.R;
import com.forgex.sys.domain.param.InitApplyParam;
import com.forgex.sys.domain.vo.InitStatusVO;
import com.forgex.sys.service.InitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 系统初始化控制器。
 * <p>
 * 暴露初始化状态查询与初始化提交接口，Controller 仅做参数接收与结果返回，
 * 业务逻辑委托 {@link com.forgex.sys.service.InitService} 处理。
 */
@RestController
@RequestMapping("/sys/init")
public class InitController {
    @Autowired
    private InitService initService;

    /**
     * 查询是否首次使用（用于登录页跳转初始化向导）。
     * @return 初始化状态视图对象
     */
    @GetMapping("/status")
    public R<InitStatusVO> status() {
        return initService.status();
    }

    /**
     * 提交初始化（清库 + 配置 + 用户/租户/角色与绑定）。
     * @param param 初始化参数
     * @return 是否成功
     */
    @PostMapping("/apply")
    public R<Boolean> apply(@RequestBody InitApplyParam param) {
        return initService.apply(param);
    }
}
