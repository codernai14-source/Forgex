package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * Android APK chunk upload complete request.
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-07
 */
@Data
public class SysAndroidVersionUploadCompleteDTO {

    private String uploadId;

    private Integer versionCode;

    private String versionName;

    private String changelog;

    private Integer status;
}
