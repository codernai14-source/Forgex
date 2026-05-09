package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 安卓版本分页查询参数
 * <p>
 * 继承 BaseGetParam 获取分页能力，支持按版本名称和状态筛选
 * </p>
 *
 * @author ForGexTeam
 * @version 1.0.0
 * @since 2026-05-05
 * @see BaseGetParam
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysAndroidVersionQueryDTO extends BaseGetParam {

    /**
     * 版本名称（模糊查询）
     */
    private String versionName;

    /**
     * 状态筛选：1-启用 0-禁用
     */
    private Integer status;
}
