package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Android APK chunk upload init request.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Data
public class SysAndroidVersionUploadInitDTO {

    private String fileName;

    private Long fileSize;

    private Long chunkSize;

    private Integer totalChunks;

    private String fileHash;
}
