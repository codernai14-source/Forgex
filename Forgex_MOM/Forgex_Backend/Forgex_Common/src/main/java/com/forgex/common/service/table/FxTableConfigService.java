package com.forgex.common.service.table;

import com.forgex.common.domain.dto.table.FxTableConfigDTO;

public interface FxTableConfigService {
    FxTableConfigDTO getTableConfig(String tableCode, Long tenantId, Long userId);
}

