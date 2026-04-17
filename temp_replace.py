# -*- coding: utf-8 -*-
import re

# 读取文件
with open('d:/mine_product/forgex/Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/message/MessageSenderService.java', 'r', encoding='utf-8') as f:
    content = f.read()

# 替换三处异常
content = content.replace(
    'throw new BusinessException("发送消息失败：" + e.getMessage());',
    'throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, MessagePromptEnum.MSG_SEND_FAILED_WITH_ERROR, e.getMessage());'
)

content = content.replace(
    'throw new BusinessException("发送消息异常：" + e.getMessage());',
    'throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, MessagePromptEnum.MSG_SEND_EXCEPTION, e.getMessage());'
)

content = content.replace(
    'throw new BusinessException("消息模板不存在或已禁用：" + templateCode);',
    'throw new I18nBusinessException(StatusCode.BUSINESS_ERROR, MessagePromptEnum.MSG_TEMPLATE_NOT_FOUND_OR_DISABLED, templateCode);'
)

# 写回文件
with open('d:/mine_product/forgex/Forgex_MOM/Forgex_Backend/Forgex_Common/src/main/java/com/forgex/common/message/MessageSenderService.java', 'w', encoding='utf-8') as f:
    f.write(content)

print("MessageSenderService.java: 3 replacements done")
