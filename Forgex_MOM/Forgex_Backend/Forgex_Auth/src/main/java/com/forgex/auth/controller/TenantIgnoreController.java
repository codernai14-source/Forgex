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
 * <p>
 * 提供租户隔离跳过配置的运行时热更新接口。
 * 允许管理员在不重启应用的情况下，重新加载租户隔离跳过配置。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>提供租户隔离跳过配置的热更新接口</li>
 *   <li>支持运行时重新加载配置</li>
 *   <li>无需重启应用即可生效</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>新增租户隔离跳过配置后，立即生效</li>
 *   <li>修改租户隔离跳过配置后，立即生效</li>
 *   <li>删除租户隔离跳过配置后，立即生效</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see TenantIgnoreService
 * @see com.forgex.auth.config.TenantIgnoreLoader
 */
@RestController
@RequestMapping("/tenant/ignore")
public class TenantIgnoreController {
    /**
     * 租户隔离跳过服务
     */
    @Autowired
    private TenantIgnoreService service;

    /**
     * 热更新跳过配置
     * <p>
     * 重新从数据库加载租户隔离跳过配置，并更新到TenantIgnoreRegistry中。
     * 无需重启应用即可使配置生效。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>调用TenantIgnoreService.reload方法</li>
     *   <li>从数据库重新加载配置</li>
     *   <li>更新TenantIgnoreRegistry</li>
     *   <li>返回操作结果</li>
     * </ol>
     * 
     * @return 操作结果，true表示成功，false表示失败
     */
    @PostMapping("/reload")
    public R<Boolean> reload() {
        return service.reload();
    }
}

