package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助资源实体。
 * <p>
 * 对应表 {@code sys_help_resource}，手册与视频共用一行记录，
 * 通过 {@code doc_type}、{@code scope_type}、{@code source_type} 区分用途。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see BaseEntity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_help_resource")
public class SysHelpResource extends BaseEntity {

    /** 资源标题。 */
    @TableField("title")
    private String title;

    /** 文档类型：MANUAL / VIDEO。 */
    @TableField("doc_type")
    private String docType;

    /** 范围：GLOBAL / MENU。 */
    @TableField("scope_type")
    private String scopeType;

    /** 来源：FILE / EXTERNAL_URL。 */
    @TableField("source_type")
    private String sourceType;

    /** 绑定菜单 ID，全局范围为空。 */
    @TableField("menu_id")
    private Long menuId;

    /** 工作区全路径，用于运行时按路由匹配。 */
    @TableField("menu_path")
    private String menuPath;

    /** 原始文件名。 */
    @TableField("file_name")
    private String fileName;

    /** 文件访问地址。 */
    @TableField("file_url")
    private String fileUrl;

    /** 文件扩展名，不含点。 */
    @TableField("file_ext")
    private String fileExt;

    /** 文件大小（字节）。 */
    @TableField("file_size")
    private Long fileSize;

    /** 文件 MIME 类型。 */
    @TableField("content_type")
    private String contentType;

    /** 外链地址。 */
    @TableField("external_url")
    private String externalUrl;

    /** 状态：0=禁用，1=启用。 */
    @TableField("status")
    private Integer status;

    /** 排序号，越小越靠前。 */
    @TableField("sort_order")
    private Integer sortOrder;

    /** 备注。 */
    @TableField("remark")
    private String remark;
}
