package com.forgex.sys.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * File record view object.
 */
@Data
public class SysFileRecordVO {

    private Long id;

    private String moduleCode;

    private String moduleName;

    private String originalName;

    private String storedName;

    private String fileType;

    private String contentType;

    private Long fileSize;

    private String relativePath;

    private String accessUrl;

    private String storageType;

    private String createBy;

    private LocalDateTime createTime;
}
