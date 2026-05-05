package com.forgex.sys.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * File record view object.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysFileRecordVO {

    /**
     * 主键 ID。
     */
    private Long id;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 模块名称。
     */
    private String moduleName;

    /**
     * original名称。
     */
    private String originalName;

    /**
     * stored名称。
     */
    private String storedName;

    /**
     * 文件类型。
     */
    private String fileType;

    /**
     * 内容类型。
     */
    private String contentType;

    /**
     * 文件size。
     */
    private Long fileSize;

    /**
     * relative路径。
     */
    private String relativePath;

    /**
     * accessurl。
     */
    private String accessUrl;

    /**
     * storage类型。
     */
    private String storageType;

    /**
     * 创建人。
     */
    private String createBy;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
