package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 首页布局分享码实体。
 */
@Data
@TableName("sys_homepage_layout_share")
public class SysHomepageLayoutShare extends BaseEntity {

    /**
     * 分享码。
     */
    private String shareCode;

    /**
     * 首页模块编码。
     */
    private String moduleCode;

    /**
     * 首页布局配置 JSON。
     */
    private String configJson;
}
