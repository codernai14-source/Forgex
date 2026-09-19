package com.forgex.integration.service;

import com.forgex.integration.domain.model.ApiDefinitionSnapshot;

/**
 * 接口定义快照服务
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface IApiDefinitionService {

    /**
     * 获取snapshot。
     *
     * @param apiCode 接口编码
     * @param direction 方向
     * @return 处理结果
     */
    ApiDefinitionSnapshot getSnapshot(String apiCode, String direction);

    /**
     * 执行接口definition的evict操作。
     *
     * @param apiCode 接口编码
     * @param direction 方向
     */
    void evict(String apiCode, String direction);
}
