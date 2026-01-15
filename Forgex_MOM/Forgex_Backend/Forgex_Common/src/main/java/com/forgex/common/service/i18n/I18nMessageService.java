package com.forgex.common.service.i18n;

import com.forgex.common.i18n.I18nPrompt;

public interface I18nMessageService {
    String resolve(I18nPrompt prompt, Object[] args);
}

