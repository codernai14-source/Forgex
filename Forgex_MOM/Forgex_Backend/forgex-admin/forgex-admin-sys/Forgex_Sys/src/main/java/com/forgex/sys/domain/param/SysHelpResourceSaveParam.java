package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 帮助资源新增或更新参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see com.forgex.sys.domain.entity.SysHelpResource
 */
@Data
public class SysHelpResourceSaveParam {

    /** 主键，更新时必填。 */
    private Long id;

    /** 资源标题。 */
    private String title;

    /** 文档类型：MANUAL / VIDEO。 */
    private String docType;

    /** 范围：GLOBAL / MENU。 */
    private String scopeType;

    /** 来源：FILE / EXTERNAL_URL。 */
    private String sourceType;

    /** 绑定菜单 ID。 */
    private Long menuId;

    /** 工作区全路径。 */
    private String menuPath;

    /** 原始文件名。 */
    private String fileName;

    /** 文件访问地址。 */
    private String fileUrl;

    /** 文件扩展名。 */
    private String fileExt;

    /** 文件大小（字节）。 */
    private Long fileSize;

    /** 文件 MIME 类型。 */
    private String contentType;

    /** 外链地址。 */
    private String externalUrl;

    /** 状态：0=禁用，1=启用。 */
    private Integer status;

    /** 排序号。 */
    private Integer sortOrder;

    /** 备注。 */
    private String remark;
}
