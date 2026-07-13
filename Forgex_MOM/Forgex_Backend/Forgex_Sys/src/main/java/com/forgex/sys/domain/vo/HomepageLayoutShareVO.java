package com.forgex.sys.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.forgex.common.domain.config.PersonalHomepageConfig;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 首页布局分享码视图对象。
 */
@Data
public class HomepageLayoutShareVO {

    /**
     * 分享码。
     */
    private String shareCode;

    /**
     * 首页模块编码。
     */
    private String moduleCode;

    /**
     * 首页布局配置。
     */
    private PersonalHomepageConfig config;

    /**
     * 创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
