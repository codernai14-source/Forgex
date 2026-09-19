package com.forgex.sys.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * Android APK chunk upload task view object.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Data
public class SysAndroidVersionUploadTaskVO {

    private String uploadId;

    private String fileName;

    private Long fileSize;

    private Long chunkSize;

    private Integer totalChunks;

    private List<Integer> uploadedChunks;

    private List<Integer> missingChunks;

    private Integer uploadedCount;

    private String status;

    private String errorMessage;

    private String finalFileUrl;

    private Long versionId;

    private SysAndroidVersionVO version;
}
