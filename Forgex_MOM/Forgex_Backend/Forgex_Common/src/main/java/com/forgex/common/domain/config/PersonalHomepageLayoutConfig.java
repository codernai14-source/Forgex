package com.forgex.common.domain.config;

import lombok.Data;

/**
 * 个人首页布局配置
 * <p>
 * 用于描述个人首页门户的栅格布局基础参数，包括列数、行高、间距等。
 * 该配置支持响应式设计，适配不同设备（桌面、平板、手机）。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see PersonalHomepageConfig
 */
@Data
public class PersonalHomepageLayoutConfig {
    
    /**
     * 栅格列数
     * <p>桌面端的栅格列数，用于布局计算。</p>
     */
    private Integer colNum;
    
    /**
     * 行高
     * <p>栅格每行的高度，单位为像素。</p>
     */
    private Integer rowHeight;
    
    /**
     * 横向间距
     * <p>栅格列之间的水平间距，单位为像素。</p>
     */
    private Integer marginX;
    
    /**
     * 纵向间距
     <p>栅格行之间的垂直间距，单位为像素。</p>
     */
    private Integer marginY;
    
    /**
     * 平板栅格列数
     * <p>平板设备的栅格列数，用于响应式布局。</p>
     */
    private Integer tabletColNum;
    
    /**
     * 手机栅格列数
     * <p>移动设备的栅格列数，用于响应式布局。</p>
     */
    private Integer mobileColNum;
    
    /**
     * 创建默认的个人首页布局配置
     *
     * @return 默认的个人首页布局配置对象
     */
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
