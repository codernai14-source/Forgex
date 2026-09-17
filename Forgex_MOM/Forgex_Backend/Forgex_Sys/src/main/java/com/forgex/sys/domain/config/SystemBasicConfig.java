/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with License.
You may obtain a copy of License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.sys.domain.config;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 系统基础配置实体
 * 作用：封装系统全局基础配置，包括系统名称、Logo、版权、登录页样式、主题色等
 * 使用场景：登录页、主布局、主题配置等
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@Accessors(chain = true)
public class SystemBasicConfig {
    
    // ==================== 系统基本信息 ===================
    
    /**
     * 系统名称
     * 用途：显示在登录页、页面标题等位置
     */
    private String systemName = "FORGEX_MOM";
    
    /**
     * 系统Logo
     * 用途：显示在登录页、主布局等位置
     * 格式：Base64编码字符串或URL路径
     */
    private String systemLogo = "";

    /**
     * 浏览器标签标题。
     * 用途：显示在浏览器标签和页面标题栏。
     */
    private String browserTitle = "FORGEX_MOM";

    /**
     * 浏览器标签图标。
     * 格式：Base64 编码字符串或 URL 路径。
     */
    private String browserIcon = "";
    
    /**
     * 系统版本
     * 用途：显示在关于页面、版本信息等位置
     */
    private String systemVersion = "1.0.0";
    
    // ==================== 版权信息 ===================
    
    /**
     * 版权信息
     * 用途：显示在页面底部
     */
    private String copyright = "© 2025 FORGEX_MOM";
    
    /**
     * 版权链接
     * 用途：版权文字的可点击链接
     */
    private String copyrightLink = "#";
    
    // ==================== 登录页配置 ===================
    
    /**
     * 登录页标题
     * 用途：显示在登录页欢迎区域
     */
    private String loginPageTitle = "欢迎来到FORGEX_MOM！";
    
    /**
     * 登录页副标题
     * 用途：显示在登录页标题下方
     */
    private String loginPageSubtitle = "";
    
    /**
     * 登录页背景类型
     * 用途：决定登录页使用视频、图片还是纯色背景
     * 可选值：video、image、color
     */
    private String loginBackgroundType = "image";
    
    /**
     * 登录页背景视频
     * 用途：当 backgroundType 为 video 时使用
     */
    private String loginBackgroundVideo = "/loading.mp4";
    
    /**
     * 登录页背景图片
     * 用途：当 backgroundType 为 image 时使用
     * 格式：Base64编码字符串或URL路径
     */
    private String loginBackgroundImage = "/back.jpg";
    
    /**
     * 登录页背景颜色
     * 用途：当 backgroundType 为 color 时使用
     */
    private String loginBackgroundColor = "#0d0221";
    
    /**
     * 登录页样式风格
     * 用途：决定登录页的UI风格
     * 可选值：cyber、simple、classic
     */
    private String loginStyle = "cyber";

    /**
     * Login page layout mode.
     * Optional values: center / split / compact
     */
    private String loginLayout = "center";

    /** 左右分栏背景铺放模式：fullscreen / separated。 */
    private String loginSplitBackgroundMode = "fullscreen";

    /** 登录标题字体样式配置。 */
    private LoginTitleStyle loginTitleStyle = new LoginTitleStyle();
    private LoginSubtitleStyle loginSubtitleStyle = new LoginSubtitleStyle();

    private Integer loginPageSubtitleFontSize = 13;
    private String loginPageSubtitleColor = "#9ca3af";
    private java.util.List<LoginHeroSlide> loginHeroSlides = new java.util.ArrayList<>();
    private Integer loginHeroIntervalSeconds = 6;

    private String loginFormStyle = "glass-dark";
    private Integer loginFormOpacity = 72;
    private Integer loginFormRadius = 20;
    private LoginShadowStyle loginFormShadow = new LoginShadowStyle("#1e9bff", 16, 45, 0);
    private LoginShadowStyle loginMediaShadow = new LoginShadowStyle("#0f172a", 28, 35, 0);

    @Data
    public static class LoginShadowStyle {
        private Boolean enabled = true;
        private String color;
        private Integer blur;
        private Integer opacity;
        private Integer spread;

        public LoginShadowStyle() { }
        public LoginShadowStyle(String color, Integer blur, Integer opacity, Integer spread) {
            this.color = color;
            this.blur = blur;
            this.opacity = opacity;
            this.spread = spread;
        }
    }

    @Data
    public static class LoginTitleStyle {
        /** 预设字体栈，由前端下拉写入，不接受任意 CSS。 */
        private String fontFamily = "'Orbitron', 'Segoe UI', sans-serif";
        /** 字号，单位 px。 */
        private Integer fontSize = 28;
        /** 颜色模式：solid=单色，gradient=渐变。 */
        private String colorMode = "solid";
        /** 单色模式下的文字颜色。 */
        private String color = "#ffffff";
        /** 渐变起点色。 */
        private String gradientFrom = "#05d9e8";
        /** 渐变终点色。 */
        private String gradientTo = "#ff2a6d";
        /** 渐变角度。 */
        private Integer gradientAngle = 90;
        /** 字重，如 normal / 600 / 700。 */
        private String fontWeight = "600";
        /** 字体样式，normal 或 italic。 */
        private String fontStyle = "normal";
        /** 字间距，单位 px。 */
        private Integer letterSpacing = 0;
    }

    /**
     * 登录页副标题可视化样式。
     * <p>
     * 副标题不做渐变，只保留字体、字号、颜色和基础排版。
     * </p>
     *
     * @author Forgex Team
     * @version 1.0.0
     * @see LoginTitleStyle
     */
    @Data
    public static class LoginSubtitleStyle {
        /** 预设字体栈。 */
        private String fontFamily = "";
        /** 字号，单位 px。 */
        private Integer fontSize = 13;
        /** 文字颜色。 */
        private String color = "#9ca3af";
        /** 字重。 */
        private String fontWeight = "normal";
        /** 字体样式。 */
        private String fontStyle = "normal";
        /** 字间距，单位 px。 */
        private Integer letterSpacing = 0;
    }

    @Data
    public static class LoginHeroSlide {
        private String type = "image";
        private String url = "";
    }
    
    /**
     * 是否显示第三方登录
     * 用途：控制登录页是否显示Gitee、微信、钉钉登录按钮
     */
    private Boolean showOAuthLogin = true;

    /**
     * 是否显示注册入口。
     * 用途：控制登录页是否展示“注册”链接。
     */
    private Boolean showRegisterEntry = true;

    /**
     * 注册入口地址。
     * 用途：支持跳转到站内注册说明页或外部注册系统。
     */
    private String registerUrl = "/register";

    // ==================== 主题配色 ===================
    
    /**
     * 主色调
     * 用途：系统主题的主要颜色
     */
    private String primaryColor = "#05d9e8";
    
    /**
     * 辅助色
     * 用途：系统主题的辅助强调颜色
     */
    private String secondaryColor = "#ff2a6d";
    
    /**
     * 获取默认配置对象
     * 
     * @return 包含默认值的 SystemBasicConfig 实例
     */
    public static SystemBasicConfig defaults() {
        return new SystemBasicConfig();
    }
}
