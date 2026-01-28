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
package com.forgex.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.auth.domain.entity.SysTenantIgnore;
import com.forgex.auth.mapper.SysTenantIgnoreMapper;
import com.forgex.auth.service.TenantIgnoreService;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import com.forgex.common.web.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户隔离跳过配置服务实现
 */
@Service
public class TenantIgnoreServiceImpl implements TenantIgnoreService {
    @Autowired
    private SysTenantIgnoreMapper mapper;

    /**
     * 重新加载租户忽略规则
     * <p>
     * 从数据库中读取所有启用的租户忽略规则，并更新到租户忽略注册表中
     * </p>
     * 
     * @return 重新加载结果
     */
    @Override
    public R<Boolean> reload() {
        TenantIgnoreRegistry.reset();
        List<SysTenantIgnore> list = mapper.selectList(new LambdaQueryWrapper<SysTenantIgnore>()
                .eq(SysTenantIgnore::getEnabled, true));
        for (SysTenantIgnore c : list) {
            String scope = c.getScope();
            String matcher = c.getMatcher();
            if ("TABLE".equalsIgnoreCase(scope)) {
                TenantIgnoreRegistry.addIgnoreTable(matcher);
            } else if ("SERVICE".equalsIgnoreCase(scope)) {
                TenantIgnoreRegistry.addIgnoreService(matcher);
            } else if ("MAPPER".equalsIgnoreCase(scope)) {
                TenantIgnoreRegistry.addIgnoreMapperMethod(matcher);
            }
        }
        return R.ok(true);
    }
}

