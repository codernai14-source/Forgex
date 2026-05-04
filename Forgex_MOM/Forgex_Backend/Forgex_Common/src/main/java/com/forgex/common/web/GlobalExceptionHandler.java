package com.forgex.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.exception.BusinessException;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.CommonPrompt;
import com.forgex.common.i18n.LegacyMessageTranslator;
import com.forgex.common.security.LogoutAuditService;
import com.forgex.common.security.LogoutReason;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 作用：统一捕获各类异常并返回项目约定的业务错误结构；同时打印详细日志，便于定位问题。
 * </p>
 * <p>返回规范：</p>
 * <ul>
 *   <li>未登录：602</li>
 *   <li>无权限：601</li>
 *   <li>参数错误：500（使用CommonPrompt.BAD_REQUEST，message字段存储动态错误信息）</li>
 *   <li>其它异常：500（使用CommonPrompt对应枚举，message字段存储错误详情）</li>
 * </ul>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #handleNotLogin(NotLoginException)} - 处理未登录异常</li>
 *   <li>{@link #handleNotRole(NotRoleException)} - 处理无权限异常</li>
 *   <li>{@link #handleI18nBusiness(I18nBusinessException)} - 处理国际化业务异常</li>
 *   <li>{@link #handleBadRequest(Exception)} - 处理参数校验异常</li>
 *   <li>{@link #handleDbConnect(CannotGetJdbcConnectionException)} - 处理数据库连接异常</li>
 *   <li>{@link #handleDataAccess(DataAccessException)} - 处理数据访问异常</li>
 *   <li>{@link #handleDefault(Exception)} - 处理通用异常</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.web.R
 * @see com.forgex.common.web.StatusCode
 * @see com.forgex.common.security.LogoutAuditService
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 登出审计服务（可选）。
     * <p>
     * 由各业务模块按需实现并注入，用于在 token 超时等场景回写登录日志的登出信息。
     * </p>
     */
    private final LogoutAuditService logoutAuditService;

    /**
     * 构造方法注入（登出审计服务可为空）。
     *
     * @param logoutAuditService 登出审计服务
     */
    public GlobalExceptionHandler(org.springframework.beans.factory.ObjectProvider<LogoutAuditService> logoutAuditService) {
        this.logoutAuditService = logoutAuditService.getIfAvailable();
    }

    /** 未登录或登录过期 */
    /**
     * 处理notlogin。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(NotLoginException.class)
    public R<Object> handleNotLogin(NotLoginException e) {
        log.warn("未登录或登录过期: {}", e.getMessage());

        // 仅在"token 超时/过期"场景尝试回写 logout_time/logout_reason（依赖模块实现）
        try {
            if (logoutAuditService != null) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                boolean maybeTimeout = msg.contains("过期") || msg.contains("超时") || msg.toLowerCase().contains("timeout");
                if (maybeTimeout) {
                    String tokenName = StpUtil.getTokenName();
                    String tokenValue = cn.dev33.satoken.context.SaHolder.getRequest().getCookieValue(tokenName);
                    if (StringUtils.hasText(tokenValue)) {
                        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.TIMEOUT);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return R.fail(StatusCode.NOT_LOGIN, CommonPrompt.NOT_LOGIN);
    }

    /** 无角色/无权限 */
    /**
     * 处理not角色。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(NotRoleException.class)
    public R<Object> handleNotRole(NotRoleException e) {
        log.warn("无权限: {}", e.getMessage());
        return R.fail(StatusCode.UNAUTHORIZED, CommonPrompt.NO_PERMISSION);
    }

    /**
     * 处理国际化业务。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(I18nBusinessException.class)
    public R<Object> handleI18nBusiness(I18nBusinessException e) {
        int code = e.getCode();
        if (code == StatusCode.NOT_LOGIN || code == StatusCode.UNAUTHORIZED) {
            return R.failWithArgs(code, e.getMsg(), e.getMsgArgs());
        }
        return R.failWithArgs(StatusCode.BUSINESS_ERROR, e.getMsg(), e.getMsgArgs());
    }

    /** 普通业务异常 */
    /**
     * 处理业务。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(BusinessException.class)
    public R<Object> handleBusiness(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        Integer code = e.getCode();
        if (code == null) {
            code = StatusCode.BUSINESS_ERROR;
        }
        return R.fail(code, CommonPrompt.BAD_REQUEST, LegacyMessageTranslator.translate(e.getMessage()));
    }

    /** 参数解析/校验错误 */
    /**
     * 处理bad请求。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class})
    public R<Object> handleBadRequest(Exception e) {
        String msg = "";
        if (e instanceof MethodArgumentNotValidException manv) {
            msg = manv.getBindingResult().getFieldErrors().stream()
                    .map(fe -> formatFieldError(fe))
                    .collect(Collectors.joining("; "));
        } else if (e instanceof BindException be) {
            msg = be.getBindingResult().getFieldErrors().stream()
                    .map(fe -> formatFieldError(fe))
                    .collect(Collectors.joining("; "));
        } else if (e instanceof HttpMessageNotReadableException hm) {
            msg = LegacyMessageTranslator.translate(hm.getMessage());
        }
        log.warn("参数错误: {}", msg);
        return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.BAD_REQUEST, msg);
    }

    /** 数据库连接失败，返回业务错误 */
    /**
     * 处理dbconnect。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public R<Object> handleDbConnect(CannotGetJdbcConnectionException e) {
        log.error("数据库连接失败", e);
        return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.DB_CONNECT_FAILED);
    }

    /** 通用数据访问异常 */
    /**
     * 处理数据access。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(DataAccessException.class)
    public R<Object> handleDataAccess(DataAccessException e) {
        log.error("数据访问异常", e);
        return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.DATA_ACCESS_ERROR);
    }

    /** 其它未捕获异常：打印堆栈并返回具体信息 */
    /**
     * 处理默认。
     *
     * @param e e
     * @return 统一响应结果
     */
    @ExceptionHandler(Exception.class)
    public R<Object> handleDefault(Exception e) {
        log.error("未处理异常", e);
        String msg = e.getMessage() == null ? "" : e.getMessage();
        return R.fail(StatusCode.BUSINESS_ERROR, CommonPrompt.INTERNAL_SERVER_ERROR, LegacyMessageTranslator.translate(msg));
    }

    private String formatFieldError(FieldError fieldError) {
        String defaultMessage = fieldError.getDefaultMessage() == null ? "不合法" : fieldError.getDefaultMessage();
        return fieldError.getField() + ":" + LegacyMessageTranslator.translate(defaultMessage);
    }
}
