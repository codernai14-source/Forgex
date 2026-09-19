package com.forgex.common.enums;

import lombok.Getter;

/**
 * 用户来源枚举。
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Getter
public enum UserSourceEnum {

    SITE_CREATED(1, "本站新增"),
    SITE_IMPORTED(2, "本站导入"),
    THIRD_PARTY_SYNC(3, "第三方同步"),
    SELF_REGISTERED(4, "自行注册");

    private final int code;
    private final String desc;

    UserSourceEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
