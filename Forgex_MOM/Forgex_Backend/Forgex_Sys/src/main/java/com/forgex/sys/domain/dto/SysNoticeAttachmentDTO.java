package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 系统通知附件 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
public class SysNoticeAttachmentDTO {

    /** 主键 ID。 */
    private Long id;

    /** 通知 ID。 */
    private Long noticeId;

    /** 文件名。 */
    private String fileName;

    /** 文件地址。 */
    private String fileUrl;

    /** 文件大小。 */
    private Long fileSize;

    /** 文件类型。 */
    private String fileType;
}
