package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 菜单实体（用于权限计算的轻量映射）。
 * <p>
 * 仅用于 Auth 模块按 DB 关系计算按钮权限键（permKey），映射表：{@code sys_menu}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 */
@Data
@TableName("sys_menu")
public class SysMenuPerm {

    /**
     * 主键ID。
     */
    private Long id;

    /**
     * 租户ID。
     */
    private Long tenantId;

    /**
     * 菜单类型：dir/menu/button。
     */
    private String type;

    /**
     * 按钮权限键。
     */
    private String permKey;

    /**
     * 逻辑删除标记。
     */
    private Boolean deleted;
}

