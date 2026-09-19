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
package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantIgnore;
import com.forgex.sys.mapper.SysTenantIgnoreMapper;
import com.forgex.sys.service.SysTenantIgnoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 租户忽略配置服务实现。
 * <p>
 * 从 admin 库读取启用的忽略规则并写入注册表，支持热更新。
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysTenantIgnoreServiceImpl implements SysTenantIgnoreService {
    @Autowired
    private SysTenantIgnoreMapper mapper;

    /**
     * 获取所有租户忽略规则列表。
     * @return 租户忽略规则列表
     */
    @Override
    public List<SysTenantIgnore> list() {
        return mapper.selectList(new LambdaQueryWrapper<>());
    }
    
    /**
     * 创建租户忽略规则。
     * @param entity 租户忽略规则实体
     * @return 创建结果
     */
    @Override
    public Boolean create(SysTenantIgnore entity) {
        return mapper.insert(entity) > 0;
    }
    
    /**
     * 更新租户忽略规则。
     * @param entity 租户忽略规则实体
     * @return 更新结果
     */
    @Override
    public Boolean update(SysTenantIgnore entity) {
        return mapper.updateById(entity) > 0;
    }
    
    /**
     * 删除租户忽略规则。
     * @param id 租户忽略规则ID
     * @return 删除结果
     */
    @Override
    public Boolean delete(Long id) {
        return mapper.deleteById(id) > 0;
    }

    /**
     * 重新加载忽略规则。
     * @return 是否成功
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
