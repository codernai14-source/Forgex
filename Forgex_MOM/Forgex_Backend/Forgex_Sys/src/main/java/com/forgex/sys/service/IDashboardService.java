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
package com.forgex.sys.service;

import java.util.Map;

/**
 * 仪表盘Service接口
 * 
 * @author coder_nai@163.com
 * @date 2025-01-08
 */
public interface IDashboardService {
    
    /**
     * 获取仪表盘统计数据
     * 
     * @param tenantId 租户ID
     * @return 统计数据
     */
    Map<String, Object> getStatistics(Long tenantId);
}
