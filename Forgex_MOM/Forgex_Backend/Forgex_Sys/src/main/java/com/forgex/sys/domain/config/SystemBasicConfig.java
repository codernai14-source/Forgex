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
    private String loginBackgroundType = "video";
    
    /**
     * 登录页背景视频
     * 用途：当 backgroundType 为 video 时使用
     */
    private String loginBackgroundVideo = "/jws.mp4";
    
    /**
     * 登录页背景图片
     * 用途：当 backgroundType 为 image 时使用
     * 格式：Base64编码字符串或URL路径
     */
    private String loginBackgroundImage = "";
    
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
     * 是否显示第三方登录
     * 用途：控制登录页是否显示Gitee、微信、钉钉登录按钮
     */
    private Boolean showOAuthLogin = true;
    
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
