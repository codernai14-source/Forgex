package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统通知附件实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice_attachment")
public class SysNoticeAttachment extends BaseEntity {

    /** 通知 ID。 */
    @TableField("notice_id")
    private Long noticeId;

    /** 原始文件名。 */
    @TableField("file_name")
    private String fileName;

    /** 文件访问地址。 */
    @TableField("file_url")
    private String fileUrl;

    /** 文件大小，单位字节。 */
    @TableField("file_size")
    private Long fileSize;

    /** 文件类型或 MIME 类型。 */
    @TableField("file_type")
    private String fileType;
}
