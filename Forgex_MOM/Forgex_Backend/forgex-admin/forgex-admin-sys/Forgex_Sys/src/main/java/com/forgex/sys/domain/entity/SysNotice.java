package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统通知主表实体。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_notice")
public class SysNotice extends BaseEntity {

    /** 通知标题。 */
    @TableField("title")
    private String title;

    /** 通知范围：PUBLIC=公共，TENANT=租户。 */
    @TableField("scope")
    private String scope;

    /** 富文本 HTML 内容。 */
    @TableField("content_html")
    private String contentHtml;

    /** 纯文本摘要。 */
    @TableField("summary")
    private String summary;

    /** 状态：DRAFT=草稿，PUBLISHED=已发布，DISABLED=已停用。 */
    @TableField("status")
    private String status;

    /** 生效时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("start_time")
    private LocalDateTime startTime;

    /** 失效时间。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("end_time")
    private LocalDateTime endTime;

    /** 排序值，越小越靠前。 */
    @TableField("order_num")
    private Integer orderNum;

    /** 是否强提醒。 */
    @TableField("force_remind")
    private Boolean forceRemind;
}
