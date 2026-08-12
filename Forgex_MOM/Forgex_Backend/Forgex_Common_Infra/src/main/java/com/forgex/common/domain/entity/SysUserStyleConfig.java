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
package com.forgex.common.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 用户页面样式配置实体。
 *
 * <p>映射表 {@code sys_user_style_config}，用于存储用户在某租户下的页面布局与样式偏好。
 * 通过 {@code configKey} 可以区分不同类型的前端样式配置（例如 layout.style）。</p>
 *
 * <p>字段说明：</p>
 * <ul>
 *     <li>{@code userId} 用户ID</li>
 *     <li>{@code tenantId} 继承自 {@link BaseEntity}，租户ID</li>
 *     <li>{@code configKey} 配置键（如 layout.style）</li>
 *     <li>{@code configJson} 配置内容 JSON 字符串</li>
 * </ul>
 *
 * @author Forgex
 * @version 1.0.0
 * @see com.forgex.common.domain.config.LayoutStyleConfig
 */
@Data
@TableName("sys_user_style_config")
public class SysUserStyleConfig extends BaseEntity {

    /** 用户ID */
    private Long userId;

    /** 配置键，例如：layout.style */
    private String configKey;

    /** 配置内容 JSON 文本 */
    private String configJson;
}

