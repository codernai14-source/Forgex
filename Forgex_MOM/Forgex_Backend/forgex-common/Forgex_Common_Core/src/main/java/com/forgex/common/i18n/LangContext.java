package com.forgex.common.i18n;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 语言上下文管理类
 * <p>
 * 基于ThreadLocal实现的语言上下文管理器，用于在请求处理过程中存储和获取当前语言设置。
 * 支持从HTTP请求头自动解析语言，并提供语言标准化处理。
 * </p>
 * <p><strong>主要功能：</strong></p>
 * <ul>
 *   <li>基于ThreadLocal存储当前请求的语言设置</li>
 *   <li>支持从请求头（X-Lang、Accept-Language）自动解析语言</li>
 *   <li>提供语言标准化处理，支持多种语言格式</li>
 *   <li>支持默认语言回退机制</li>
 * </ul>
 * <p><strong>语言标准化规则：</strong></p>
 * <ul>
 *   <li>zh、zh-cn → zh-CN</li>
 *   <li>zh-tw、zh-hk → zh-TW</li>
 *   <li>en、en-us → en-US</li>
 *   <li>其他语言保持原样（如ja-JP、ko-KR）</li>
 * </ul>
 * <p><strong>使用示例：</strong></p>
 * <pre>{@code
 * // 设置语言
 * LangContext.set("en-US");
 * 
 * // 获取当前语言
 * String lang = LangContext.get();
 * 
 * // 清理上下文（通常在请求结束时调用）
 * LangContext.clear();
 * }</pre>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see LangWebInterceptor
 */
public final class LangContext {
    /**
     * 语言请求头名称
     * <p>用于从HTTP请求头中获取语言设置。</p>
     */
    public static final String HEADER_LANG = "X-Lang";

    /**
     * 默认语言
     * <p>当无法从请求中解析语言时使用的默认语言。</p>
     */
    public static final String DEFAULT_LANG = "zh-CN";

    /**
     * ThreadLocal持有器
     * <p>用于存储当前线程的语言设置。</p>
     */
    private static final ThreadLocal<String> HOLDER = new ThreadLocal<>();

    /**
     * 私有构造函数
     * <p>防止实例化，所有方法都是静态方法。</p>
     */
    private LangContext() {}

    /**
     * 设置当前语言
     * <p>
     * 将语言设置存储到ThreadLocal中。如果语言为空，则使用默认语言。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>检查语言是否为空</li>
     *   <li>如果为空，设置默认语言</li>
     *   <li>如果不为空，进行语言标准化处理</li>
     *   <li>将标准化后的语言存储到ThreadLocal</li>
     * </ol>
     * 
     * @param lang 语言代码，如"zh-CN"、"en-US"
     */
    public static void set(String lang) {
        // 检查语言是否为空
        if (!StringUtils.hasText(lang)) {
            // 如果为空，设置默认语言
            HOLDER.set(DEFAULT_LANG);
            return;
        }
        // 如果不为空，进行语言标准化处理并存储
        HOLDER.set(normalize(lang));
    }

    /**
     * 获取当前语言
     * <p>
     * 从ThreadLocal中获取当前语言设置。如果未设置，则尝试从当前请求中解析。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>从ThreadLocal中获取语言</li>
     *   <li>如果存在，直接返回</li>
     *   <li>如果不存在，尝试从当前请求中解析</li>
     *   <li>如果解析成功，存储到ThreadLocal并返回</li>
     *   <li>如果解析失败，返回默认语言</li>
     * </ol>
     * 
     * @return 当前语言代码，如"zh-CN"、"en-US"
     */
    public static String get() {
        // 从ThreadLocal中获取语言
        String lang = HOLDER.get();
        
        // 如果存在，直接返回
        if (StringUtils.hasText(lang)) {
            return lang;
        }

        // 尝试从当前请求中解析
        String resolved = resolveFromRequest();
        
        // 如果解析成功，存储到ThreadLocal并返回
        if (StringUtils.hasText(resolved)) {
            HOLDER.set(resolved);
            return resolved;
        }

        // 如果解析失败，返回默认语言
        return DEFAULT_LANG;
    }

    /**
     * 清理当前语言上下文
     * <p>
     * 从ThreadLocal中移除当前语言设置。通常在请求结束时调用，
     * 以防止内存泄漏和线程复用导致的数据混乱。
     * </p>
     */
    public static void clear() {
        // 从ThreadLocal中移除当前语言设置
        HOLDER.remove();
    }

    /**
     * 从当前请求中解析语言
     * <p>
     * 尝试从当前HTTP请求的请求头中解析语言设置。
     * 优先使用X-Lang请求头，其次使用Accept-Language请求头。
     * </p>
     * <p><strong>执行流程：</strong></p>
     * <ol>
     *   <li>获取当前请求属性</li>
     *   <li>如果请求属性不存在，返回null</li>
     *   <li>获取HTTP请求对象</li>
     *   <li>尝试从X-Lang请求头获取语言</li>
     *   <li>如果不存在，尝试从Accept-Language请求头获取</li>
     *   <li>如果仍然不存在，返回null</li>
     *   <li>对获取的语言进行标准化处理并返回</li>
     * </ol>
     * 
     * @return 解析后的语言代码，解析失败返回null
     */
    private static String resolveFromRequest() {
        try {
            // 获取当前请求属性
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
                // 请求属性不存在，返回null
                return null;
            }
            
            // 获取HTTP请求对象
            HttpServletRequest request = attrs.getRequest();
            if (request == null) {
                // 请求对象为空，返回null
                return null;
            }

            // 尝试从X-Lang请求头获取语言
            String lang = request.getHeader(HEADER_LANG);
            
            // 如果不存在，尝试从Accept-Language请求头获取
            if (!StringUtils.hasText(lang)) {
                lang = request.getHeader("Accept-Language");
            }
            
            // 如果仍然不存在，返回null
            if (!StringUtils.hasText(lang)) {
                return null;
            }
            
            // 对获取的语言进行标准化处理并返回
            return normalize(lang);
        } catch (Exception ignored) {
            // 异常时返回null
            return null;
        }
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
    private static String normalize(String raw) {
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

