package com.forgex.sys.domain.param;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 帮助资源管理分页查询参数。
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-09-11
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysHelpResourcePageParam extends BaseGetParam {

    /** 标题，模糊查询。 */
    private String title;

    /** 文档类型：MANUAL / VIDEO。 */
    private String docType;

    /** 范围：GLOBAL / MENU。 */
    private String scopeType;

    /** 来源：FILE / EXTERNAL_URL。 */
    private String sourceType;

    /** 工作区菜单路径，模糊查询。 */
    private String menuPath;

    /** 状态：0=禁用，1=启用。 */
    private Integer status;
}
