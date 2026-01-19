package com.forgex.sys.service.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.forgex.common.tenant.TenantContext;
import com.forgex.sys.domain.entity.SysFileStorage;
import com.forgex.sys.mapper.SysFileStorageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 文件存储配置服务类
 * <p>提供文件存储配置的查询功能。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Service
@RequiredArgsConstructor
public class SysFileStorageConfigService {

    /**
     * 文件存储Mapper
     */
    private final SysFileStorageMapper fileStorageMapper;

    /**
     * 获取默认的文件存储配置
     * <p>查询当前租户的默认文件存储配置。</p>
     * 
     * @return 文件存储配置，未找到返回null
     */
    public SysFileStorage getDefault() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            return null;
        }
        return fileStorageMapper.selectOne(new LambdaQueryWrapper<SysFileStorage>()
                .eq(SysFileStorage::getTenantId, tenantId)
                .eq(SysFileStorage::getDeleted, false)
                .eq(SysFileStorage::getStatus, true)
                .eq(SysFileStorage::getIsDefault, true)
                .last("limit 1"));
    }
}

