package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 帮助资源启停参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class SysHelpResourceStatusParam {

    /** 资源主键。 */
    private Long id;

    /** 目标状态：0=禁用，1=启用。 */
    private Integer status;
}
