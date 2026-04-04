package com.forgex.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 语言Web拦截器
 * <p>
 * 实现Spring的HandlerInterceptor接口，用于在请求处理前后管理语言上下文。
 * 从HTTP请求头中提取语言设置，并设置到LangContext中。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>从请求头（X-Lang、Accept-Language）提取语言设置</li>
 *   <li>将语言设置存储到LangContext中</li>
 *   <li>请求结束后清理LangContext，防止内存泄漏</li>
 *   <li>支持语言标准化处理</li>
 * </ul>
 * <p><strong>拦截流程：</strong></p>
 * <ol>
 *   <li>请求到达时，从请求头提取语言</li>
 *   <li>对语言进行标准化处理</li>
 *   <li>将语言设置到LangContext</li>
 *   <li>请求处理完成后，清理LangContext</li>
 * </ol>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see LangContext
 * @see org.springframework.web.servlet.HandlerInterceptor
 */
public class LangWebInterceptor implements HandlerInterceptor {
    /**
     * 请求预处理
     * <p>
     * 在请求处理前从请求头提取语言设置，并设置到LangContext中。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>尝试从X-Lang请求头获取语言</li>
     *   <li>如果不存在，尝试从Accept-Language请求头获取</li>
     *   <li>如果获取到语言，进行标准化处理并设置到LangContext</li>
     *   <li>如果未获取到语言，设置默认语言到LangContext</li>
     *   <li>返回true继续执行后续拦截器和处理器</li>
     * </ol>
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @return true表示继续执行，false表示中断请求
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        // 尝试从X-Lang请求头获取语言
        String lang = request.getHeader(LangContext.HEADER_LANG);
        
        // 如果不存在，尝试从Accept-Language请求头获取
        if (!StringUtils.hasText(lang)) {
            lang = request.getHeader("Accept-Language");
        }
        
        // 如果获取到语言，进行标准化处理并设置到LangContext
        if (StringUtils.hasText(lang)) {
            lang = normalize(lang);
            LangContext.set(lang);
        } else {
            // 如果未获取到语言，设置默认语言
            LangContext.set(LangContext.DEFAULT_LANG);
        }
        
        // 返回true继续执行后续拦截器和处理器
        return true;
    }

    /**
     * 请求完成后的清理
     * <p>
     * 在请求处理完成后清理LangContext，防止ThreadLocal内存泄漏。
     * 无论请求是否成功，都会执行清理操作。
     * </p>
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象
     * @param ex 请求处理过程中抛出的异常（如果有）
     */
    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) {
        // 清理LangContext，防止ThreadLocal内存泄漏
        LangContext.clear();
    }

    /**
     * 标准化语言代码
     * <p>
     * 将各种格式的语言代码标准化为统一的格式。
     * 支持处理逗号分隔、分号分隔、下划线分隔等格式。
     * </p>
     * <p><strong>标准化规则：</strong></p>
     * <ul>
     *   <li>去除前后空格</li>
     *   <li>截取第一个逗号前的部分</li>
     *   <li>截取第一个分号前的部分</li>
     *   <li>将下划线替换为连字符</li>
     *   <li>zh、zh-cn → zh-CN</li>
     *   <li>zh-tw、zh-hk → zh-TW</li>
     *   <li>en、en-us → en-US</li>
     * </ul>
     * 
     * @param raw 原始语言代码
     * @return 标准化后的语言代码
     */
    private String normalize(String raw) {
        // 去除前后空格
        String v = raw.trim();
        
        // 截取第一个逗号前的部分
        int comma = v.indexOf(',');
        if (comma > 0) {
            v = v.substring(0, comma);
        }
        
        // 截取第一个分号前的部分
        int semi = v.indexOf(';');
        if (semi > 0) {
            v = v.substring(0, semi);
        }
        
        // 将下划线替换为连字符
        v = v.replace('_', '-');
        
        // 标准化中文简体
        if (v.equalsIgnoreCase("zh") || v.equalsIgnoreCase("zh-cn")) {
            return "zh-CN";
        }
        
        // 标准化中文繁体
        if (v.equalsIgnoreCase("zh-tw") || v.equalsIgnoreCase("zh-hk")) {
            return "zh-TW";
        }
        
        // 标准化英文
        if (v.equalsIgnoreCase("en") || v.equalsIgnoreCase("en-us")) {
            return "en-US";
        }
        
        // 其他语言保持原样
        return v;
    }
}

