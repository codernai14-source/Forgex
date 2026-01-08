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
    private Integer isDefault; // 是否默认（1默认 0非默认）
}
