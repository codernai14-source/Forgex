package com.forgex.common.domain.config;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 个人首页组件配置
 * <p>
 * 用于描述个人首页门户中单个组件的配置信息，包括组件位置、大小、显示状态等。
 * 该配置支持用户自定义首页布局，实现个性化的工作台。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see PersonalHomepageConfig
 */
@Data
public class PersonalHomepageWidgetConfig {

    /**
     * 组件编码
     * <p>唯一标识一个组件，用于区分不同的首页组件。</p>
     */
    private String key;
    
    /**
     * 组件标题
     * <p>组件的显示标题，用于在首页展示。</p>
     */
    private String title;
    
    /**
     * 是否显示
     * <p>控制组件是否在首页显示，true 表示显示，false 表示隐藏。</p>
     */
    private Boolean visible;
    
    /**
     * 栅格横坐标
     * <p>组件在栅格布局中的 X 轴坐标位置。</p>
     */
    private Integer x;
    
    /**
     * 栅格纵坐标
     * <p>组件在栅格布局中的 Y 轴坐标位置。</p>
     */
    private Integer y;
    
    /**
     * 栅格宽度
     * <p>组件在栅格布局中占据的宽度（以列为单位）。</p>
     */
    private Integer w;
    
    /**
     * 栅格高度
     * <p>组件在栅格布局中占据的高度（以行为单位）。</p>
     */
    private Integer h;
    
    /**
     * 最小宽度
     * <p>组件允许调整的最小宽度（以列为单位）。</p>
     */
    private Integer minW;
    
    /**
     * 最小高度
     * <p>组件允许调整的最小高度（以行为单位）。</p>
     */
    private Integer minH;
    
    /**
     * 排序
     * <p>组件的显示顺序，数值越小越靠前。</p>
     */
    private Integer orderNum;
    
    /**
     * 组件参数
     * <p>组件的自定义参数配置，用于传递组件特定的配置项。</p>
     */
    private Map<String, Object> params;

    /**
     * 创建默认的个人首页组件配置
     *
     * @param key 组件编码
     * @param title 组件标题
     * @param x 栅格横坐标
     * @param y 栅格纵坐标
     * @param w 栅格宽度
     * @param h 栅格高度
     * @param orderNum 排序号
     * @param limit 数据限制数量
     * @return 默认的个人首页组件配置对象
     */
    public static PersonalHomepageWidgetConfig of(String key,
                                                  String title,
                                                  int x,
                                                  int y,
                                                  int w,
                                                  int h,
                                                  int orderNum,
                                                  int limit) {
        PersonalHomepageWidgetConfig config = new PersonalHomepageWidgetConfig();
        config.setKey(key);
        config.setTitle(title);
        config.setVisible(Boolean.TRUE);
        config.setX(x);
        config.setY(y);
        config.setW(w);
        config.setH(h);
        config.setMinW(Math.min(w, 2));
        config.setMinH(Math.min(h, 2));
        config.setOrderNum(orderNum);
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("limit", limit);
        config.setParams(params);
        return config;
    }
}
