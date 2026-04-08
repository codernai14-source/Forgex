package com.forgex.sys.domain.dto;

import lombok.Data;

/**
 * 操作模板DTO
 * <p>封装操作模板的配置信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
public class SysOperationTemplateDTO {
    /**
     * 模块名称
     */
    private String module;

    /**
     * 操作类型
     */
    private String operationType;

    /**
     * 模板代码
     */
    private String templateCode;

    /**
     * 国际化文本JSON
     */
    private String textI18nJson;

    /**
     * 占位符JSON
     */
    private String placeholdersJson;
}

