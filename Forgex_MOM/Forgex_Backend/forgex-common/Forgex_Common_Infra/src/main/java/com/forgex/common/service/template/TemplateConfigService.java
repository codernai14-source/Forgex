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
package com.forgex.common.service.template;

import java.util.Map;

/**
 * 模板配置服务接口
 * <p>
 * 提供模板配置的查询和同步功能，支持租户级别配置。
 * 查询优先级：租户配置 → 公共配置（tenant_id=0）
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public interface TemplateConfigService {
    
    /**
     * 获取模板内容
     * <p>
     * 查询优先级：租户配置 → 公共配置（tenant_id=0）
     * </p>
     *
     * @param tenantId 租户 ID
     * @param templateCode 模板编码
     * @param params 模板参数
     * @return 渲染后的模板内容
     */
    String getTemplateContent(Long tenantId, String templateCode, Map<String, Object> params);
    
    /**
     * 同步模板配置到租户
     * <p>
     * 从源租户（通常为公共配置 tenant_id=0）同步模板到目标租户。
     * </p>
     *
     * @param sourceTenantId 源租户 ID（通常为 0，公共配置）
     * @param targetTenantId 目标租户 ID
     * @param templateCodes 模板编码列表，为 null 时同步所有模板
     */
    void syncTemplatesToTenant(Long sourceTenantId, Long targetTenantId, String... templateCodes);
}
