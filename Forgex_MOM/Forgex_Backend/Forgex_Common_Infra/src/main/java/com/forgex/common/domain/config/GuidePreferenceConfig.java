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
package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 用户引导偏好配置。
 * <p>
 * 用于保存用户在当前租户下的 Tour 漫游引导状态，包括：
 * </p>
 * <ul>
 *   <li>是否开启宝宝模式</li>
 *   <li>各个 guideCode 的引导完成、跳过状态</li>
 *   <li>引导版本与最近更新时间</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @since 2026-04-15
 * @see com.forgex.common.domain.entity.SysUserStyleConfig
 */
@Getter
@Setter
public class GuidePreferenceConfig {

    /** 是否开启宝宝模式 */
    private Boolean babyModeEnabled;

    /** 引导状态映射，key 为 guideCode */
    private Map<String, GuideState> guideStates;

    /**
     * 创建默认引导偏好配置。
     *
     * @return 默认配置对象
     */
    public static GuidePreferenceConfig defaults() {
        GuidePreferenceConfig config = new GuidePreferenceConfig();
        config.setBabyModeEnabled(Boolean.FALSE);
        config.setGuideStates(new LinkedHashMap<>());
        return config;
    }

    /**
     * 单个引导项的状态配置。
     */
    @Getter
    @Setter
    public static class GuideState {

        /** 引导状态：PENDING / SKIPPED / COMPLETED */
        private String status;

        /** 引导版本 */
        private String version;

        /** 最近更新时间，ISO 8601 字符串 */
        private String updatedAt;
    }
}
