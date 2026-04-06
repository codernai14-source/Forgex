package com.forgex.common.domain.config;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 个人首页组件配置。
 */
@Data
public class PersonalHomepageWidgetConfig {

    /** 组件编码 */
    private String key;

    /** 组件标题 */
    private String title;

    /** 是否显示 */
    private Boolean visible;

    /** 栅格横坐标 */
    private Integer x;

    /** 栅格纵坐标 */
    private Integer y;

    /** 栅格宽度 */
    private Integer w;

    /** 栅格高度 */
    private Integer h;

    /** 最小宽度 */
    private Integer minW;

    /** 最小高度 */
    private Integer minH;

    /** 排序 */
    private Integer orderNum;

    /** 组件参数 */
    private Map<String, Object> params;

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
