package com.forgex.sys.domain.param;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 系统通知分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticePageParam extends BaseGetParam {

    /** 通知标题，模糊查询。 */
    private String title;

    /** 通知范围。 */
    private String scope;

    /** 通知状态。 */
    private String status;

    /** 生效时间范围。 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime[] startTime;
}
