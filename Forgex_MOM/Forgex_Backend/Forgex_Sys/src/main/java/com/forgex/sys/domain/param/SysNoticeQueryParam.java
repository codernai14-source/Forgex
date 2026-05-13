package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统通知列表查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysNoticeQueryParam extends BaseGetParam {

    /**
     * 生效通知返回数量上限。
     */
    private Integer maxCount;
}
