package com.forgex.basic.label.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 标签模板设计详情。
 */
@Data
@Schema(description = "标签模板设计详情")
public class LabelTemplateDesignVO {

    private Long id;
    private Long typeId;
    private String templateCode;
    private String templateName;
    private Integer templateVersion;
    private Boolean isEnabled;
    private Integer paperWidth;
    private Integer paperHeight;
    private String paperSize;
    private List<LabelRenderComponentVO> components;
}
