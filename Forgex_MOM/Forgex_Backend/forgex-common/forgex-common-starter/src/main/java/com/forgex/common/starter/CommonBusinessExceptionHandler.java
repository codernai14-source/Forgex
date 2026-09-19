package com.forgex.common.starter;

import com.forgex.common.exception.BusinessException;
import com.forgex.common.exception.I18nBusinessException;
import com.forgex.common.i18n.I18nPrompt;
import com.forgex.common.i18n.LangContext;
import com.forgex.common.web.R;
import com.forgex.common.web.StatusCode;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * 无平台依赖的业务异常处理，保留业务状态码并以 R 返回。
 * 国际化提示使用枚举默认模板；平台处理器存在时不注册此兜底。
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnMissingBean(type = "com.forgex.common.web.GlobalExceptionHandler", name = "globalExceptionHandler")
public class CommonBusinessExceptionHandler {

    /**
     * 返回普通业务异常，不把未经分类的系统异常消息暴露给调用方。
     *
     * @param exception 业务异常
     * @return 统一业务失败结果
     */
    @ExceptionHandler(BusinessException.class)
    public R<Object> handleBusiness(BusinessException exception) {
        R<Object> response = R.fail();
        response.setCode(exception.getCode() == null ? StatusCode.BUSINESS_ERROR : exception.getCode());
        response.setMessage(exception.getMessage());
        return response;
    }

    /**
     * 无数据库时使用提示枚举的默认模板格式化业务异常。
     *
     * @param exception 国际化业务异常
     * @return 带默认文案及业务状态码的统一结果
     */
    @ExceptionHandler(I18nBusinessException.class)
    public R<Object> handleI18nBusiness(I18nBusinessException exception) {
        R<Object> response = R.failWithArgs(exception.getCode(), exception.getMsg(), exception.getMsgArgs());
        I18nPrompt prompt = exception.getMsg();
        if (prompt != null && prompt.getDefaultTemplate() != null) {
            Object[] args = exception.getMsgArgs() == null ? new Object[0] : exception.getMsgArgs();
            response.setMessage(new MessageFormat(prompt.getDefaultTemplate(),
                    Locale.forLanguageTag(LangContext.get())).format(args));
        }
        return response;
    }
}
