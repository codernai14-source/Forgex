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

    @Override
    public R<Boolean> reload() {
        TenantIgnoreRegistry.reset();
        List<SysTenantIgnore> list = mapper.selectList(new LambdaQueryWrapper<SysTenantIgnore>()
                .eq(SysTenantIgnore::getEnabled, 1));
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

