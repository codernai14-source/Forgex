package com.forgex.common.exception;

import com.forgex.common.i18n.I18nPrompt;

public class I18nBusinessException extends RuntimeException {
    private final int code;
    private final I18nPrompt msg;
    private final Object[] msgArgs;

    public I18nBusinessException(int code, I18nPrompt msg, Object... msgArgs) {
        super(msg == null ? null : msg.getPromptCode());
        this.code = code;
        this.msg = msg;
        this.msgArgs = msgArgs;
    }

    public int getCode() {
        return code;
    }

    public I18nPrompt getMsg() {
        return msg;
    }

    public Object[] getMsgArgs() {
        return msgArgs;
    }
}

