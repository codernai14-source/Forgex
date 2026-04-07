package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 个人首页布局配置。
 *
 * <p>用于描述门户栅格的基础参数。</p>
 */
@Data
public class PersonalHomepageLayoutConfig {

    /** 栅格列数 */
    private Integer colNum;

    /** 行高 */
    private Integer rowHeight;

    /** 横向间距 */
    private Integer marginX;

    /** 纵向间距 */
    private Integer marginY;

    /** 平板栅格列数 */
    private Integer tabletColNum;

    /** 手机栅格列数 */
    private Integer mobileColNum;

    public static PersonalHomepageLayoutConfig defaults() {
        PersonalHomepageLayoutConfig config = new PersonalHomepageLayoutConfig();
        config.setColNum(12);
        config.setRowHeight(72);
        config.setMarginX(16);
        config.setMarginY(16);
        config.setTabletColNum(8);
        config.setMobileColNum(4);
        return config;
    }
}
