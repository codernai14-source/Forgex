package com.forgex.common.domain.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人首页门户配置
 * <p>
 * 用于描述个人首页的整体布局与组件配置，包括栅格布局参数和组件列表。
 * 该配置支持用户自定义首页的布局结构和展示内容，实现个性化的工作台。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see PersonalHomepageLayoutConfig
 * @see PersonalHomepageWidgetConfig
 */
@Data
public class PersonalHomepageConfig {
    
    /**
     * 布局配置
     * <p>包含首页栅格布局的基础参数，如列数、行高、间距等。</p>
     */
    private PersonalHomepageLayoutConfig layout;
    
    /**
     * 组件配置
     * <p>首页中所有组件的配置列表，定义每个组件的位置、大小和显示状态。</p>
     */
    private List<PersonalHomepageWidgetConfig> widgets;
    
    /**
     * 创建默认的个人首页配置
     *
     * @return 默认的个人首页配置对象，包含常用的默认组件
     */
    public static PersonalHomepageConfig defaults() {
        PersonalHomepageConfig config = new PersonalHomepageConfig();
        config.setLayout(PersonalHomepageLayoutConfig.defaults());

        List<PersonalHomepageWidgetConfig> widgets = new ArrayList<>();
        widgets.add(PersonalHomepageWidgetConfig.of("commonMenus", "常用菜单", 0, 0, 6, 4, 10, 8));
        widgets.add(PersonalHomepageWidgetConfig.of("pendingApprovals", "我收到的审批", 6, 0, 6, 4, 20, 6));
        widgets.add(PersonalHomepageWidgetConfig.of("calendar", "日历", 0, 4, 4, 4, 30, 0));
        widgets.add(PersonalHomepageWidgetConfig.of("messages", "我收到的消息", 4, 4, 4, 4, 40, 8));
        widgets.add(PersonalHomepageWidgetConfig.of("notices", "系统通知", 8, 4, 4, 4, 50, 8));
        widgets.add(PersonalHomepageWidgetConfig.of("currentTime", "当前时间", 0, 8, 3, 3, 60, 0));
        config.setWidgets(widgets);
        return config;
    }
}
