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
 * 这里将租户ID以字符串形式返回。前端在回传租户ID时也应使用字符串，
 * 后端在需要时再转换为 Long 进行数据库查询。
 */
@Data
public class TenantVO {
    private String id;          // 租户ID（字符串，防止 Long 精度丢失）
    private String name;      // 租户名称
    private String logo;      // 租户Logo
    private String intro;     // 租户简介
    private Boolean isDefault; // 是否默认（true 默认，false 非默认）
}
