package com.forgex.auth.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社交登录实体类
 * <p>存储用户的社交登录绑定信息。</p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_social_login")
public class SysSocialLogin extends BaseEntity {

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 社交平台（wechat、dingtalk等）
     */
    @TableField("platform")
    private String platform;

    /**
     * 开放平台用户ID
     */
    @TableField("open_id")
    private String openId;

    /**
     * 开放平台用户UnionID
     */
    @TableField("union_id")
    private String unionId;

    /**
     * 绑定时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("bind_time")
    private LocalDateTime bindTime;
}

