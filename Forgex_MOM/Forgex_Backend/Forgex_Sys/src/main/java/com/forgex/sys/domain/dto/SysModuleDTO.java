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
package com.forgex.sys.domain.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 模块DTO
 *
 * 模块信息的数据传输对象，用于在控制层和服务层之间传递模块基础信息。
 * 字段包括编码、名称、图标、排序、可见性、状态、租户ID以及创建和更新时间等。
 *
 * @author coder_nai@163.com
 * @date 2025-01-07
 */
@Data
public class SysModuleDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 模块编码
     */
    private String code;

    /**
     * 模块名称
     */
    private String name;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序号
     */
    private Integer orderNum;

    /**
     * 是否可见：false=隐藏，true=显示
     */
    private Boolean visible;

    /**
     * 状态：false=禁用，true=启用
     */
    private Boolean status;

    /**
     * 租户ID
     */
    private Long tenantId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
