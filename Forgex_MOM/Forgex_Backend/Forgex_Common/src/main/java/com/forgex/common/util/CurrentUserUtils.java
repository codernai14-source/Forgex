package com.forgex.common.util;

import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.tenant.TenantContext;
import com.forgex.common.tenant.UserContext;
import org.springframework.util.StringUtils;

/**
 * 当前登录人工具类。
 * <p>
 * 优先从线程上下文 {@link UserContext}/{@link TenantContext} 读取（网关透传 Header 场景），\n
 * 若上下文为空，则尝试从 Sa-Token Session 中读取（Auth 场景）。\n
 * </p>
 * <p>使用场景：</p>
 * <ul>
 *   <li>网关透传场景：从UserContext和TenantContext读取用户信息</li>
 *   <li>Auth服务场景：从Sa-Token Session中读取用户信息</li>
 * </ul>
 * <p>注意事项：</p>
 * <ul>
 *   <li>工具类为静态方法调用，无需实例化</li>
 *   <li>获取不到用户信息时返回null，调用方需进行空值判断</li>
 * </ul>
 *
 * @author coder_nai@163.com
 * @version 1.0.0
 * @see com.forgex.common.tenant.UserContext
 * @see com.forgex.common.tenant.TenantContext
 * @see cn.dev33.satoken.stp.StpUtil
 */
public class CurrentUserUtils {

    /**
     * Session中存储用户ID的键名
     * <p>
     * 与Sa-Token Session中的键名保持一致
     * </p>
     */
    private static final String KEY_USER_ID = "LOGIN_USER_ID";
    
    /**
     * Session中存储租户ID的键名
     * <p>
     * 与Sa-Token Session中的键名保持一致
     * </p>
     */
    private static final String KEY_TENANT_ID = "LOGIN_TENANT_ID";
    
    /**
     * Session中存储账号的键名
     * <p>
     * 与Sa-Token Session中的键名保持一致
     * </p>
     */
    private static final String KEY_ACCOUNT = "LOGIN_ACCOUNT";

    /**
     * 私有构造方法，防止实例化
     */
    private CurrentUserUtils() {
    }

    /**
     * 获取当前用户ID。
     * <p>
     * 优先从UserContext获取，若为空则从Sa-Token Session中获取。
     * </p>
     * <p>获取流程：</p>
     * <ol>
     *   <li>优先从UserContext读取（网关透传场景）</li>
     *   <li>UserContext为空时，从Sa-Token Session读取</li>
     *   <li>读取失败或解析失败时返回null</li>
     * </ol>
     *
     * @return 用户ID，未登录或获取失败返回null
     * @see UserContext#get()
     * @see cn.dev33.satoken.stp.StpUtil#getSession()
     */
    public static Long getUserId() {
        // 优先从UserContext获取用户ID
        Long uid = UserContext.get();
        if (uid != null) {
            return uid;
        }
        // UserContext为空时，从Sa-Token Session获取
        try {
            Object v = StpUtil.getSession(false).get(KEY_USER_ID);
            return parseLong(v);
        } catch (Exception ignored) {
            // Session不存在或读取失败，返回null
            return null;
        }
    }

    /**
     * 获取当前租户ID。
     * <p>
     * 优先从TenantContext获取，若为空则从Sa-Token Session中获取。
     * </p>
     * <p>获取流程：</p>
     * <ol>
     *   <li>优先从TenantContext读取（网关透传场景）</li>
     *   <li>TenantContext为空时，从Sa-Token Session读取</li>
     *   <li>读取失败或解析失败时返回null</li>
     * </ol>
     *
     * @return 租户ID，未登录或获取失败返回null
     * @see TenantContext#get()
     * @see cn.dev33.satoken.stp.StpUtil#getSession()
     */
    public static Long getTenantId() {
        // 优先从TenantContext获取租户ID
        Long tid = TenantContext.get();
        if (tid != null) {
            return tid;
        }
        // TenantContext为空时，从Sa-Token Session获取
        try {
            Object v = StpUtil.getSession(false).get(KEY_TENANT_ID);
            return parseLong(v);
        } catch (Exception ignored) {
            // Session不存在或读取失败，返回null
            return null;
        }
    }

    /**
     * 获取当前账号。
     * <p>
     * 从Sa-Token Session中获取登录账号。
     * </p>
     *
     * @return 账号，未登录或获取失败返回null
     * @see cn.dev33.satoken.stp.StpUtil#getSession()
     */
    public static String getAccount() {
        try {
            // 从Session中获取账号
            Object v = StpUtil.getSession(false).get(KEY_ACCOUNT);
            // 判断值是否为空字符串
            if (v != null && StringUtils.hasText(String.valueOf(v))) {
                return String.valueOf(v);
            }
        } catch (Exception ignored) {
            // Session不存在或读取失败，返回null
        }
        return null;
    }

    /**
     * 将对象解析为Long类型。
     * <p>
     * 支持Number、String类型，其他类型返回null。
     * </p>
     * <p>解析规则：</p>
     * <ul>
     *   <li>Number类型：直接调用longValue()方法</li>
     *   <li>String类型：尝试使用Long.valueOf()解析</li>
     *   <li>其他类型或解析失败：返回null</li>
     * </ul>
     *
     * @param obj 待解析对象
     * @return Long值，解析失败返回null
     */
    private static Long parseLong(Object obj) {
        // 空值判断
        if (obj == null) {
            return null;
        }
        // Number类型直接转换
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        // String类型尝试解析
        if (obj instanceof String) {
            try {
                return Long.valueOf((String) obj);
            } catch (Exception ignored) {
                // 解析失败返回null
                return null;
            }
        }
        // 其他类型返回null
        return null;
    }
}

