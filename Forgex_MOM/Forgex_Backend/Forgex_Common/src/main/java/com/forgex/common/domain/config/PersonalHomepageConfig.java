package com.forgex.common.domain.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人首页门户配置。
 */
@Data
public class PersonalHomepageConfig {

    /** 布局配置 */
    private PersonalHomepageLayoutConfig layout;

    /** 组件配置 */
    private List<PersonalHomepageWidgetConfig> widgets;

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
