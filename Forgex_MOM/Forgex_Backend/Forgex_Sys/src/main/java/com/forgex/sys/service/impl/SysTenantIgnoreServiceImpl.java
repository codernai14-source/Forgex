package com.forgex.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantIgnoreRegistry;
import com.forgex.common.web.R;
import com.forgex.sys.domain.entity.SysTenantIgnore;
import com.forgex.sys.mapper.SysTenantIgnoreMapper;
import com.forgex.sys.service.SysTenantIgnoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 租户忽略配置服务实现。
 * <p>
 * 从 admin 库读取启用的忽略规则并写入注册表，支持热更新。
 */
@Service
public class SysTenantIgnoreServiceImpl implements SysTenantIgnoreService {
    @Autowired
    private SysTenantIgnoreMapper mapper;

    /**
     * 重新加载忽略规则。
     * @return 是否成功
     */
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
