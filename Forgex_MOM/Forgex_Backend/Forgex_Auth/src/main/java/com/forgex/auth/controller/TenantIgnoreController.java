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
package com.forgex.auth.controller;

import com.forgex.auth.service.TenantIgnoreService;
import com.forgex.common.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 租户隔离跳过配置控制器
 * 提供运行时热更新接口
 */
@RestController
@RequestMapping("/tenant/ignore")
public class TenantIgnoreController {
    @Autowired
    private TenantIgnoreService service;

    /**
     * 热更新跳过配置
     * @return 是否成功
     */
    @PostMapping("/reload")
    public R<Boolean> reload() {
        return service.reload();
    }
}

