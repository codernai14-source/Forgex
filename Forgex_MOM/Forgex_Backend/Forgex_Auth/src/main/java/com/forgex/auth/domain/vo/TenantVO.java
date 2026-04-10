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
package com.forgex.auth.domain.vo;

import lombok.Data;

/**
 * 租户返回对象（用于前端展示）
 * <p>
 * 说明：为避免前端 JavaScript 在处理 Long 类型 ID 时出现精度丢失问题，
 * 这里将租户 ID 以字符串形式返回。前端在回传租户 ID 时也应使用字符串，
 * 后端在需要时再转换为 Long 进行数据库查询。
 * </p>
 *
 * @author coder_nai
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.common.enums.TenantTypeEnum
 * @see com.forgex.auth.controller.AuthController
 */
@Data
public class TenantVO {

    /**
     * 租户 ID（字符串，防止 Long 精度丢失）
     * <p>
     * JavaScript 中 Number 类型为双精度浮点数，无法精确表示超过 2^53-1 的整数，
     * 因此将 Long 类型 ID 转换为字符串返回前端
     * </p>
     */
    private String id;

    /**
     * 租户名称
     * <p>租户的显示名称</p>
     */
    private String name;

    /**
     * 租户 Logo
     * <p>租户的 Logo 图片 URL 地址</p>
     */
    private String logo;

    /**
     * 租户简介
     * <p>租户的简短介绍信息</p>
     */
    private String intro;

    /**
     * 是否默认租户
     * <p>
     * true：默认租户<br>
     * false：非默认租户
     * </p>
     */
    private Boolean isDefault;

    /**
     * 租户类别
     * <p>租户的分类类型标识</p>
     */
    private String tenantType;
}
