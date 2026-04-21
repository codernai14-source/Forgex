package com.forgex.sys.domain.dto;

import com.forgex.common.base.BaseGetParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * File record query DTO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SysFileRecordQueryDTO extends BaseGetParam {

    private String moduleCode;

    private String moduleName;

    private String originalName;

    private String fileType;
}
