package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 菜单实体（用于权限计算的轻量映射）
 * <p>
 * 仅用于 Auth 模块按 DB 关系计算按钮权限键（permKey），映射表：{@code sys_menu}。
 * </p>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @since 2026-01-08
 * @see com.forgex.auth.security.StpInterfaceImpl
 * @see com.forgex.auth.mapper.SysMenuPermMapper
 */
@Data
@TableName("sys_menu")
public class SysMenuPerm {

    /**
     * 主键 ID（雪花算法生成）
     */
    private Long id;

    /**
     * 租户 ID
     * <p>用于多租户隔离，标识该菜单所属租户</p>
     */
    private Long tenantId;

    /**
     * 菜单类型
     * <p>
     * 可选值：
     * <ul>
     *     <li>dir：目录</li>
     *     <li>menu：菜单</li>
     *     <li>button：按钮</li>
     * </ul>
     * </p>
     */
    private String type;

    /**
     * 按钮权限键
     * <p>用于权限校验的权限标识符，例如：sys:user:add</p>
     */
    private String permKey;

    /**
     * 逻辑删除标记
     * <p>
     * true：已删除<br>
     * false：未删除
     * </p>
     */
    private Boolean deleted;
}
