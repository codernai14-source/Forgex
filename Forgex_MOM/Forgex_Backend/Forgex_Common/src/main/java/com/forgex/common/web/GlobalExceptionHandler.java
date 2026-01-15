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
package com.forgex.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import com.forgex.common.exception.I18nBusinessException;
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
 *   <li>未登录：401</li>
 *   <li>无权限：403</li>
 *   <li>参数错误：400（包含具体字段错误信息）</li>
 *   <li>其它异常：500（返回异常具体信息，避免笼统"系统异常"）</li>
 * </ul>
 * <p>主要功能：</p>
 * <ul>
 *   <li>{@link #handleNotLogin(NotLoginException)} - 处理未登录异常</li>
 *   <li>{@link #handleNotRole(NotRoleException)} - 处理无权限异常</li>
 *   <li>{@link #handleBadRequest(Exception)} - 处理参数校验异常</li>
 *   <li>{@link #handleDbConnect(CannotGetJdbcConnectionException)} - 处理数据库连接异常</li>
 *   <li>{@link #handleDataAccess(DataAccessException)} - 处理数据访问异常</li>
 *   <li>{@link #handleDefault(Exception)} - 处理通用异常</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.common.web.R
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
    @ExceptionHandler(NotLoginException.class)
    public R<Object> handleNotLogin(NotLoginException e) {
        log.warn("未登录或登录过期: {}", e.getMessage());

        // 仅在“token 超时/过期”场景尝试回写 logout_time/logout_reason（依赖模块实现）
        try {
            if (logoutAuditService != null) {
                String msg = e.getMessage() == null ? "" : e.getMessage();
                boolean maybeTimeout = msg.contains("过期") || msg.contains("超时") || msg.toLowerCase().contains("timeout");
                if (maybeTimeout) {
                    String tokenName = StpUtil.getTokenName();
                    String tokenValue = cn.dev33.satoken.context.SaHolder.getRequest().getHeader(tokenName);
                    if (!StringUtils.hasText(tokenValue)) {
                        tokenValue = cn.dev33.satoken.context.SaHolder.getRequest().getParam(tokenName);
                    }
                    if (StringUtils.hasText(tokenValue)) {
                        logoutAuditService.recordLogoutByToken(tokenValue, LogoutReason.TIMEOUT);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return R.fail(401, e.getMessage());
    }

    /** 无角色/无权限 */
    @ExceptionHandler(NotRoleException.class)
    public R<Object> handleNotRole(NotRoleException e) {
        log.warn("无权限: {}", e.getMessage());
        return R.fail(403, e.getMessage());
    }

    @ExceptionHandler(I18nBusinessException.class)
    public R<Object> handleI18nBusiness(I18nBusinessException e) {
        return R.fail(e.getCode(), e.getMsg(), e.getMsgArgs());
    }

    /** 参数解析/校验错误 */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class})
    public R<Object> handleBadRequest(Exception e) {
        String msg = "请求参数错误";
        if (e instanceof MethodArgumentNotValidException manv) {
            msg = manv.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ":" + (fe.getDefaultMessage() == null ? "不合法" : fe.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
        } else if (e instanceof BindException be) {
            msg = be.getBindingResult().getFieldErrors().stream()
                    .map(fe -> fe.getField() + ":" + (fe.getDefaultMessage() == null ? "不合法" : fe.getDefaultMessage()))
                    .collect(Collectors.joining("; "));
        } else if (e instanceof HttpMessageNotReadableException hm) {
            msg = hm.getMessage();
        }
        log.warn("参数错误: {}", msg);
        return R.fail(400, msg);
    }

    /** 数据库连接失败，返回业务错误 */
    @ExceptionHandler(CannotGetJdbcConnectionException.class)
    public R<Object> handleDbConnect(CannotGetJdbcConnectionException e) {
        log.error("数据库连接失败", e);
        return R.fail(503, "数据库连接失败，请检查数据库服务与配置");
    }

    /** 通用数据访问异常 */
    @ExceptionHandler(DataAccessException.class)
    public R<Object> handleDataAccess(DataAccessException e) {
        log.error("数据访问异常", e);
        String msg = e.getMessage() == null ? "数据访问异常" : e.getMessage();
        return R.fail(500, msg);
    }

    /** 其它未捕获异常：打印堆栈并返回具体信息 */
    @ExceptionHandler(Exception.class)
    public R<Object> handleDefault(Exception e) {
        log.error("未处理异常", e);
        String msg = e.getMessage() == null ? "服务器内部错误" : e.getMessage();
        return R.fail(500, msg);
    }
}
