package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 帮助资源上传结果。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class SysHelpUploadResultDTO {

    /** 访问 URL。 */
    private String fileUrl;

    /** 原始文件名。 */
    private String fileName;

    /** 扩展名，不含点。 */
    private String fileExt;

    /** 文件大小（字节）。 */
    private Long fileSize;

    /** MIME 类型。 */
    private String contentType;
}
