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

import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.web.R;
import com.forgex.sys.domain.dto.tenant.SysTenantDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantQueryDTO;
import com.forgex.sys.domain.dto.tenant.SysTenantSaveParam;
import com.forgex.sys.service.SysTenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 租户Controller
 * <p>
 * 提供租户管理的HTTP接口，包含租户的增删改查功能
 * </p>
 * 
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.sys.service.SysTenantService
 */
@Slf4j
@RestController
@RequestMapping("/sys/tenant")
@RequiredArgsConstructor
public class SysTenantController {
    
    private final SysTenantService tenantService;
    
    /**
     * 查询租户列表
     * 
     * @param queryDTO 查询参数
     * @return 租户列表
     */
    @PostMapping("/list")
    public R<List<SysTenantDTO>> list(@RequestBody SysTenantQueryDTO queryDTO) {
        List<SysTenantDTO> list = tenantService.list(queryDTO);
        return R.ok(list);
    }
    
    /**
     * 获取租户详情
     *
     * @param params 参数（id）
     * @return 租户详情
     */
    @PostMapping("/get")
    public R<SysTenantDTO> get(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());

        SysTenantDTO tenant = tenantService.getById(id);

        if (tenant == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }

        return R.ok(tenant);
    }
    
    /**
     * 获取主租户
     *
     * @return 主租户信息
     */
    @PostMapping("/getMainTenant")
    public R<SysTenantDTO> getMainTenant() {
        SysTenantDTO tenant = tenantService.getMainTenant();

        if (tenant == null) {
            return R.fail(CommonPrompt.NOT_FOUND);
        }

        return R.ok(tenant);
    }
    
    /**
     * 新增租户
     *
     * @param param 租户参数
     * @return 租户ID
     */
    @PostMapping("/create")
    public R<Long> create(@Validated @RequestBody SysTenantSaveParam param) {
        Long id = tenantService.create(param);
        return R.ok(CommonPrompt.CREATE_SUCCESS, id);
    }

    /**
     * 更新租户
     *
     * @param param 租户参数
     * @return 是否成功
     */
    @PostMapping("/update")
    public R<Boolean> update(@Validated @RequestBody SysTenantSaveParam param) {
        Boolean success = tenantService.update(param);
        return R.ok(CommonPrompt.UPDATE_SUCCESS, success);
    }

    /**
     * 删除租户
     *
     * @param params 参数（id）
     * @return 是否成功
     */
    @PostMapping("/delete")
    public R<Boolean> delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());

        Boolean success = tenantService.delete(id);
        return R.ok(CommonPrompt.DELETE_SUCCESS, success);
    }
}
