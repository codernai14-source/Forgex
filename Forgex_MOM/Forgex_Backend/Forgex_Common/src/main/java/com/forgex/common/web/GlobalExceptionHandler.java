package com.forgex.common.web;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 作用：统一捕获各类异常并返回项目约定的业务错误结构；同时打印详细日志，便于定位问题。
 * 返回规范：
 * - 未登录：401；无权限：403
 * - 参数错误：400（包含具体字段错误信息）
 * - 其它异常：500（返回异常具体信息，避免笼统“系统异常”）
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /** 未登录或登录过期 */
    @ExceptionHandler(NotLoginException.class)
    public R<Object> handleNotLogin(NotLoginException e) {
        log.warn("未登录或登录过期: {}", e.getMessage());
        return R.fail(401, e.getMessage());
    }

    /** 无角色/无权限 */
    @ExceptionHandler(NotRoleException.class)
    public R<Object> handleNotRole(NotRoleException e) {
        log.warn("无权限: {}", e.getMessage());
        return R.fail(403, e.getMessage());
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
