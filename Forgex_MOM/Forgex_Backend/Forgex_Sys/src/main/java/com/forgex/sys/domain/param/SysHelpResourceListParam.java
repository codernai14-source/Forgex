package com.forgex.sys.domain.param;

import lombok.Data;

/**
 * 运行时按当前页解析帮助资源的请求参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 */
@Data
public class SysHelpResourceListParam {

    /** 文档类型：MANUAL / VIDEO。 */
    private String docType;

    /** 当前工作区路径，通常来自路由 fullPath。 */
    private String menuPath;
}
