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
package com.forgex.common.config;

import com.forgex.common.domain.config.GuidePreferenceConfig;
import com.forgex.common.domain.config.LayoutStyleConfig;

/**
 * 用户页面样式配置服务。
 *
 * <p>统一负责用户在各租户下的页面布局与样式配置读写。</p>
 *
 * @author Forgex
 * @version 1.0.0
 */
public interface UserStyleConfigService {

    /**
     * 读取用户在某租户下的布局样式配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 若未配置或解析失败，则返回默认配置
     */
    LayoutStyleConfig getLayoutConfig(Long userId, Long tenantId);

    /**
     * 保存用户在某租户下的布局样式配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param config   布局样式配置对象
     */
    void saveLayoutConfig(Long userId, Long tenantId, LayoutStyleConfig config);

    /**
     * 读取用户在某租户下的引导偏好配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @return 引导偏好配置
     */
    GuidePreferenceConfig getGuidePreference(Long userId, Long tenantId);

    /**
     * 保存用户在某租户下的引导偏好配置。
     *
     * @param userId   用户ID
     * @param tenantId 租户ID
     * @param config   引导偏好配置
     */
    void saveGuidePreference(Long userId, Long tenantId, GuidePreferenceConfig config);
}

