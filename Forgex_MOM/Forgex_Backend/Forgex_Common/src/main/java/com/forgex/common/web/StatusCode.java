package com.forgex.common.web;

/**
 * 接口状态码常量类
 * <p>
 * 定义系统接口返回的状态码规范，用于标识接口的运行状态
 * </p>
 * <p>状态码分类：</p>
 * <ul>
 *   <li>成功状态码：200-299</li>
 *   <li>业务失败状态码：500-599</li>
 *   <li>认证授权失败状态码：600-699</li>
 *   <li>资源不存在状态码：400-499</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
public final class StatusCode {

    private StatusCode() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 资源不存在状态码
     * <p>
     * 用于接口不存在的情况，使用前需先检查模块是否启动
     * </p>
     */
    public static final int NOT_FOUND = 404;

    /**
     * 业务方法失败状态码
     * <p>
     * 默认的业务失败状态码，用于所有未明确指定的业务异常
     * </p>
     */
    public static final int BUSINESS_ERROR = 500;

    /**
     * 未授权状态码
     * <p>
     * 用于用户已登录但无权限访问某资源的情况
     * </p>
     */
    public static final int UNAUTHORIZED = 601;

    /**
     * 未登录状态码
     * <p>
     * 用于用户未登录或登录过期的情况
     * </p>
     */
    public static final int NOT_LOGIN = 602;

    /**
     * 模块未上线状态码
     * <p>
     * 用于接口对应模块未启动的情况
     * </p>
     */
    public static final int MODULE_OFFLINE = 603;

    /**
     * 未授权状态码
     * <p>
     * 用于缺少授权文件或尚未激活的情况。
     * </p>
     */
    public static final int LICENSE_REQUIRED = 604;

    /**
     * 授权无效状态码
     * <p>
     * 用于签名错误、机器码不匹配或授权已过期等情况。
     * </p>
     */
    public static final int LICENSE_INVALID = 605;
}
