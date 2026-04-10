/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.domain.config;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户页面布局与样式配置
 * <p>
 * 用于描述单个用户在某个租户下的前端布局偏好，包括菜单形态、视觉主题、标签栏行为等。
 * 该配置支持用户自定义界面风格，实现个性化的用户体验。
 * </p>
 * <p><strong>配置范围：</strong></p>
 * <ul>
 *   <li>菜单形态：是否左侧双菜单、布局模式等</li>
 *   <li>基础视觉：字体大小、圆角大小、主题模式与主题色、内容宽度</li>
 *   <li>标签栏行为：启用开关、最大数量、拖拽、是否显示图标、标签页风格</li>
 *   <li>全局小部件：搜索、主题切换、语言切换、全屏、通知、侧边栏折叠、刷新</li>
 *   <li>顶栏行为：是否显示、固定/自动隐藏/滚动隐藏、菜单位置</li>
 *   <li>通用体验：水印、页面切换动画、加载指示等</li>
 *   <li>底栏展示：版权信息是否启用</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @since 2026-03-28
 * @see com.forgex.common.domain.entity.SysUserStyleConfig
 */
@Getter
@Setter
public class LayoutStyleConfig {

    /** 是否启用左侧双菜单布局 */
    private Boolean leftDoubleMenu;

    /** 布局模式：vertical/vertical-mix/top/mix */
    private String layoutMode;

    /** 内容区域宽度：fluid/fixed */
    private String contentWidth;

    /** 字体大小（例如：small/medium/large 或 14px/16px） */
    private String fontSize;

    /** 圆角大小（例如：0、4、8，单位 px） */
    private Integer borderRadius;

    /** 主题模式：light/dark/system */
    private String themeMode;

    /** 主题主色，如 #1677ff */
    private String themeColor;

    /** 顶栏是否可见 */
    private Boolean headerVisible;

    /** 顶栏模式：fixed/auto/hide-on-scroll */
    private String headerMode;

    /** 顶栏菜单位置：left/center/right */
    private String headerMenuAlign;

    /** 是否启用标签栏 */
    private Boolean tabBarEnabled;

    /** 标签栏最大数量 */
    private Integer tabBarMax;

    /** 标签栏是否启用拖拽排序 */
    private Boolean tabBarDraggable;

    /** 标签栏是否显示图标 */
    private Boolean tabBarShowIcon;

    /** 标签栏样式：chrome/card */
    private String tabBarStyle;

    /** 小部件：是否启用全局搜索 */
    private Boolean widgetGlobalSearch;

    /** 小部件：是否启用主题切换 */
    private Boolean widgetThemeSwitch;

    /** 小部件：是否启用语言切换 */
    private Boolean widgetLangSwitch;

    /** 小部件：是否启用全屏 */
    private Boolean widgetFullscreen;

    /** 小部件：是否启用通知 */
    private Boolean widgetNotification;

    /** 小部件：是否启用侧边栏折叠切换 */
    private Boolean widgetSiderCollapse;

    /** 小部件：是否启用刷新按钮 */
    private Boolean widgetRefresh;

    /** 是否启用水印 */
    private Boolean watermarkEnabled;

    /** 水印文本内容 */
    private String watermarkText;

    /** 是否启用页面动画 */
    private Boolean animateEnabled;

    /** 是否启用页面加载指示器 */
    private Boolean loadingIndicatorEnabled;

    /** 页面切换动画：horizontal/fade */
    private String pageTransition;

    /** 底栏：是否启用版权信息展示 */
    private Boolean footerCopyrightEnabled;

    /**
     * 构造一个默认的布局样式配置。
     *
     * @return 默认配置对象，适合作为首次登录或未配置时的回退值
     */
    public static LayoutStyleConfig defaults() {
        LayoutStyleConfig cfg = new LayoutStyleConfig();
        cfg.setLeftDoubleMenu(Boolean.FALSE);
        cfg.setLayoutMode("mix");
        cfg.setContentWidth("fluid");
        cfg.setFontSize("14px");
        cfg.setBorderRadius(6);
        cfg.setThemeMode("light");
        cfg.setThemeColor("#1677ff");

        cfg.setHeaderVisible(Boolean.TRUE);
        cfg.setHeaderMode("fixed");
        cfg.setHeaderMenuAlign("left");

        cfg.setTabBarEnabled(Boolean.TRUE);
        cfg.setTabBarMax(10);
        cfg.setTabBarDraggable(Boolean.TRUE);
        cfg.setTabBarShowIcon(Boolean.TRUE);
        cfg.setTabBarStyle("chrome");

        cfg.setWidgetGlobalSearch(Boolean.TRUE);
        cfg.setWidgetThemeSwitch(Boolean.TRUE);
        cfg.setWidgetLangSwitch(Boolean.TRUE);
        cfg.setWidgetFullscreen(Boolean.TRUE);
        cfg.setWidgetNotification(Boolean.TRUE);
        cfg.setWidgetSiderCollapse(Boolean.TRUE);
        cfg.setWidgetRefresh(Boolean.TRUE);

        cfg.setWatermarkEnabled(Boolean.FALSE);
        cfg.setWatermarkText("FORGEX_MOM");
        cfg.setAnimateEnabled(Boolean.TRUE);
        cfg.setLoadingIndicatorEnabled(Boolean.TRUE);
        cfg.setPageTransition("horizontal");

        cfg.setFooterCopyrightEnabled(Boolean.TRUE);
        return cfg;
    }
}
