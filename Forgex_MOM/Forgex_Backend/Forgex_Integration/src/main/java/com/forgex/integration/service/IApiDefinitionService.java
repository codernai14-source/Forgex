package com.forgex.integration.service;

import com.forgex.integration.domain.model.ApiDefinitionSnapshot;

/**
 * 鎺ュ彛瀹氫箟蹇収鏈嶅姟
 */
public interface IApiDefinitionService {

    ApiDefinitionSnapshot getSnapshot(String apiCode, String direction);

    void evict(String apiCode, String direction);
}
