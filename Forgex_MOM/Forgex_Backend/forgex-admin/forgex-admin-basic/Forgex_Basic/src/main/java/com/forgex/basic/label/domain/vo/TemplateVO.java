package com.forgex.basic.label.domain.vo;

import com.forgex.common.api.annotation.AutoFillUsername;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签模板视图对象。
 */
@Data
@Schema(description = "标签模板VO")
public class TemplateVO {

    private Long id;
    private String templateCode;
    private String templateName;
    private String templateType;
    private Long typeId;
    private Integer paperWidth;
    private Integer paperHeight;
    private String paperSize;
    private Integer templateVersion;
    private Boolean isDefault;
    private Boolean isEnabled;
    private String templateContent;
    private String description;
    private Integer status;
    private Long factoryId;
    private Long tenantId;
    private String createBy;

    @AutoFillUsername(userIdField = "createBy")
    private String createByName;

    private LocalDateTime createTime;
    private String updateBy;
    private LocalDateTime updateTime;
}
