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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 系统模块实体。
 * <p>
 * 映射表：{@code sys_module}。用于记录系统模块的基本信息。
 * 字段：
 * - {@code code} 模块编码；
 * - {@code name} 模块名称；
 * - {@code icon} 模块图标；
 * - {@code orderNum} 排序号；
 * - {@code visible} 是否可见；
 * - {@code status} 状态。
 */
@Data
@TableName("sys_module")
public class SysModule extends BaseEntity {
    /** 模块编码 */
    private String code;
    /** 模块名称 */
    private String name;
    private String nameI18nJson;
    /** 模块图标 */
    private String icon;
    /** 排序号 */
    private Integer orderNum;
    /** 是否可见：false=隐藏，true=显示 */
    private Boolean visible;
    /** 状态：false=禁用，true=启用 */
    private Boolean status;
}
