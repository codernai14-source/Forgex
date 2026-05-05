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
     * 获取默认配置。
     *
     * @return 默认的个人首页配置对象，包含常用的默认组件
     */
    public static PersonalHomepageConfig defaults() {
        PersonalHomepageConfig config = new PersonalHomepageConfig();
        config.setLayout(PersonalHomepageLayoutConfig.defaults());

        List<PersonalHomepageWidgetConfig> widgets = new ArrayList<>();
        widgets.add(PersonalHomepageWidgetConfig.of("commonMenus", "常用菜单", 0, 0, 6, 4, 10, 6));
        widgets.add(PersonalHomepageWidgetConfig.of("myFavorites", "我的收藏", 0, 4, 6, 4, 20, 6));
        widgets.add(PersonalHomepageWidgetConfig.of("pendingApprovals", "我收到的审批", 6, 0, 6, 4, 30, 6));
        widgets.add(PersonalHomepageWidgetConfig.of("calendar", "日历", 6, 4, 3, 4, 40, 0));
        widgets.add(PersonalHomepageWidgetConfig.of("currentTime", "当前时间", 9, 4, 3, 3, 50, 0));
        widgets.add(PersonalHomepageWidgetConfig.of("messages", "我收到的消息", 0, 8, 6, 4, 60, 8));
        widgets.add(PersonalHomepageWidgetConfig.of("notices", "系统通知", 6, 8, 6, 4, 70, 8));
        config.setWidgets(widgets);
        return config;
    }

    /**
     * 获取默认配置。
     *
     * @param moduleCode 模块编码，空值或 personal 返回个人首页默认配置
     * @return 默认首页配置
     */
    public static PersonalHomepageConfig defaults(String moduleCode) {
        if (moduleCode == null || moduleCode.trim().isEmpty() || "personal".equalsIgnoreCase(moduleCode.trim())) {
            return defaults();
        }
        String normalizedModuleCode = moduleCode.trim().toLowerCase();
        return switch (normalizedModuleCode) {
            case "basic" -> ofWidgets(
                    PersonalHomepageWidgetConfig.of("supplierInfo", "供应商信息", 0, 0, 6, 4, 10, 6),
                    PersonalHomepageWidgetConfig.of("encodeRuleInfo", "编码规则信息", 6, 0, 6, 4, 20, 6)
            );
            case "sys", "system" -> ofWidgets(
                    PersonalHomepageWidgetConfig.of("systemOverview", "系统概览", 0, 0, 6, 4, 10, 6),
                    PersonalHomepageWidgetConfig.of("systemHealth", "运行状态", 6, 0, 6, 4, 20, 6),
                    PersonalHomepageWidgetConfig.of("systemLogs", "操作日志", 0, 4, 6, 4, 30, 6),
                    PersonalHomepageWidgetConfig.of("systemConfig", "系统配置", 6, 4, 6, 4, 40, 6)
            );
            case "approval", "workflow" -> ofWidgets(
                    PersonalHomepageWidgetConfig.of("approvalStats", "审批概览", 0, 0, 6, 4, 10, 6),
                    PersonalHomepageWidgetConfig.of("approvalShortcuts", "审批入口", 6, 0, 6, 4, 20, 6),
                    PersonalHomepageWidgetConfig.of("approvalPending", "我的待办", 0, 4, 6, 4, 30, 6),
                    PersonalHomepageWidgetConfig.of("approvalTaskConfig", "任务配置", 6, 4, 6, 4, 40, 6)
            );
            default -> defaults();
        };
    }

    private static PersonalHomepageConfig ofWidgets(PersonalHomepageWidgetConfig... widgets) {
        PersonalHomepageConfig config = new PersonalHomepageConfig();
        config.setLayout(PersonalHomepageLayoutConfig.defaults());
        config.setWidgets(new ArrayList<>(List.of(widgets)));
        return config;
    }
}
