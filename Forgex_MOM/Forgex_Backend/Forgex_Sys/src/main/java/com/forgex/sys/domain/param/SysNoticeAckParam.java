package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 系统通知弹窗确认参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-10
 */
@Data
public class SysNoticeAckParam {

    /** 通知 ID。 */
    private Long noticeId;
}
