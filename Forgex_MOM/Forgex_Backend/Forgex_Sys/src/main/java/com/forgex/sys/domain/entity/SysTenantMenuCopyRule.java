package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 租户菜单复制规则实体
 * <p>
 * 映射表：{@code sys_tenant_menu_copy_rule}。用于配置在新增租户初始化菜单时，
 * 针对不同租户类型需要排除复制的权限前缀（通常对应 SysMenu 的 {@code permKey} 前缀），
 * 从而控制哪些菜单与功能按钮不被复制到新租户。
 * </p>
 *
 * <p>
 * 典型使用场景：
 * <ul>
 *     <li>为客户租户、供应商租户排除“租户管理”相关菜单与按钮（如 {@code sys:tenant} 前缀）。</li>
 *     <li>根据 {@code tenantType} 精细化控制不同租户类型的初始化菜单集。</li>
 * </ul>
 * </p>
 *
 * @author coder_nai
 * @version 1.0
 * @see com.forgex.common.enums.TenantTypeEnum
 * @see com.forgex.sys.domain.entity.SysMenu
 */
@Data
@TableName("sys_tenant_menu_copy_rule")
public class SysTenantMenuCopyRule extends BaseEntity {

    /**
     * 租户类型编码
     * <p>
     * 对应 {@link com.forgex.common.enums.TenantTypeEnum#getCode()}，
     * 例如：{@code MAIN_TENANT}、{@code CUSTOMER_TENANT}、{@code SUPPLIER_TENANT}。
     * </p>
     */
    private String tenantType;

    /**
     * 权限前缀
     * <p>
     * 对应 SysMenu 的 {@code permKey} 前缀，例如：
     * <ul>
     *     <li>{@code sys:tenant}：排除租户管理目录、菜单与按钮；</li>
     *     <li>{@code sys:xxx}：排除其它系统管理功能。</li>
     * </ul>
     * 在菜单复制时只要权限键以该前缀开头即会被排除。
     * </p>
     */
    private String permPrefix;

    /**
     * 是否启用
     * <p>
     * {@code true} 表示规则生效，{@code false} 表示规则暂不生效。
     * </p>
     */
    private Boolean enabled;

    /**
     * 规则备注
     * <p>
     * 可记录该规则的业务含义、适用场景等说明信息。
     * </p>
     */
    private String remark;

    /**
     * 租户ID（非持久化）
     * <p>
     * 该表为全局管理库配置表，不参与业务租户隔离，因此不持久化租户ID字段，
     * 仅通过 {@link TableField#exist()} 标记为非持久化属性，避免与 {@link BaseEntity} 默认映射冲突。
     * </p>
     */
    @TableField(exist = false)
    private Long tenantId;
}

