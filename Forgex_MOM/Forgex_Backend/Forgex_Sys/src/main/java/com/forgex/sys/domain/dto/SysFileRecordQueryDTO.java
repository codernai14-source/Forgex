package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * File record query DTO.
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileRecordQueryDTO extends BaseGetParam {

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 模块名称。
     */
    private String moduleName;

    /**
     * original名称。
     */
    private String originalName;

    /**
     * 文件类型。
     */
    private String fileType;
}
