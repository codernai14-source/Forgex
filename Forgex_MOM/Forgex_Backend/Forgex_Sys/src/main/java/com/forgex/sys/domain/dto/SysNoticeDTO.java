package com.forgex.sys.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 系统通知 DTO。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
public class SysNoticeDTO {

    /** 主键 ID。 */
    private Long id;

    /** 租户 ID。 */
    private Long tenantId;

    /** 通知标题。 */
    private String title;

    /** 通知范围。 */
    private String scope;

    /** 富文本 HTML 内容。 */
    private String contentHtml;

    /** 纯文本摘要。 */
    private String summary;

    /** 通知状态。 */
    private String status;

    /** 生效时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /** 失效时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /** 排序值。 */
    private Integer orderNum;

    /** 是否强提醒。 */
    private Boolean forceRemind;

    /** 附件列表。 */
    private List<SysNoticeAttachmentDTO> attachments = new ArrayList<>();

    /** 创建时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /** 更新时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
