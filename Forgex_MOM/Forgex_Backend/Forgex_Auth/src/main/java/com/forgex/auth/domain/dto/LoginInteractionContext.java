package com.forgex.auth.domain.dto;

import cn.hutool.json.JSONUtil;
import cn.hutool.json.JSONObject;

/**
 * 登录交互码绑定的身份上下文。
 *
 * @param userId 用户 ID
 * @param account 登录账号
 * @param loginTerminal 登录终端
 */
public record LoginInteractionContext(Long userId, String account, String loginTerminal) {

    /**
     * 创建登录交互上下文。
     *
     * @param userId 用户 ID
     * @param account 登录账号
     * @param loginTerminal 登录终端
     * @return 登录交互上下文
     */
    public static LoginInteractionContext of(Long userId, String account, String loginTerminal) {
        return new LoginInteractionContext(userId, account, loginTerminal);
    }

    /**
     * 序列化为 Redis 存储值。
     *
     * @return JSON 字符串
     */
    public String toJson() {
        return new JSONObject()
                .set("userId", userId)
                .set("account", account)
                .set("loginTerminal", loginTerminal)
                .toString();
    }

    /**
     * 从 Redis 存储值恢复上下文。
     *
     * @param json JSON 字符串
     * @return 登录交互上下文
     */
    public static LoginInteractionContext fromJson(String json) {
        JSONObject object = JSONUtil.parseObj(json);
        return new LoginInteractionContext(
                object.getLong("userId"),
                object.getStr("account"),
                object.getStr("loginTerminal")
        );
    }
}
