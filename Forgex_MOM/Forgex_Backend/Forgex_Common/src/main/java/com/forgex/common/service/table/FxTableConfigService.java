package com.forgex.common.service.table;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.forgex.common.domain.dto.table.FxTableConfigDTO;
import com.forgex.common.domain.entity.table.FxTableConfig;

public interface FxTableConfigService {
    FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId);
    
    IPage<FxTableConfigDTO> page(Page<FxTableConfig> page, com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FxTableConfig> wrapper, Long tenantId, Long userId);
}

