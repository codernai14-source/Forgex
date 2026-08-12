package com.forgex.common.domain.entity.i18n;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 国际化消息实体
 * <p>
 * 封装国际化消息的完整信息，用于存储多语言的提示文本。
 * </p>
 * <p><strong>主要字段：</strong></p>
 * <ul>
 *   <li>{@code module} - 模块标识，用于区分不同功能模块的消息</li>
 *   <li>{@code promptCode} - 提示代码，用于唯一标识一条消息</li>
 *   <li>{@code textI18nJson} - 文本国际化JSON，包含多语言的文本内容</li>
 *   <li>{@code enabled} - 是否启用，true=启用，false=禁用</li>
 *   <li>{@code version} - 版本号，用于消息的版本控制</li>
 * </ul>
 * <p><strong>使用场景：</strong></p>
 * <ul>
 *   <li>业务错误消息的国际化</li>
 *   <li>系统提示消息的国际化</li>
 *   <li>验证错误消息的国际化</li>
 *   <li>成功提示消息的国际化</li>
 * </ul>
 *
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("fx_i18n_message")
public class FxI18nMessage extends BaseEntity {
    /**
     * 模块标识
     * <p>用于区分不同功能模块的消息，如"sys"、"auth"等。</p>
     */
    private String module;

    /**
     * 提示代码
     * <p>用于唯一标识一条消息，如"USER_NOT_FOUND"、"LOGIN_FAILED"等。</p>
     */
    private String promptCode;

    /**
     * 文本国际化JSON
     * <p>包含多语言的文本内容，格式如：{"zh-CN":"用户不存在","en-US":"User not found"}。</p>
     */
    private String textI18nJson;

    /**
     * 是否启用
     * <p>true=启用，false=禁用。</p>
     */
    private Boolean enabled;

    /**
     * 版本号
     * <p>用于消息的版本控制，支持消息的增量更新。</p>
     */
    private Integer version;
}

