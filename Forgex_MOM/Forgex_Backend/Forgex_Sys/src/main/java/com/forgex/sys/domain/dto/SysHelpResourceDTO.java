package com.forgex.sys.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 帮助资源对外传输对象。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see com.forgex.sys.domain.entity.SysHelpResource
 */
@Data
public class SysHelpResourceDTO {

    /** 主键。 */
    private Long id;

    /** 租户 ID。 */
    private Long tenantId;

    /** 资源标题。 */
    private String title;

    /** 文档类型。 */
    private String docType;

    /** 范围。 */
    private String scopeType;

    /** 来源。 */
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

    /** 状态。 */
    private Integer status;

    /** 排序号。 */
    private Integer sortOrder;

    /** 备注。 */
    private String remark;

    /** 创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 更新时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
