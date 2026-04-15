/*
 * Copyright 2026 coder_nai@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 密钥使用日志实体类。
 * <p>
 * 对应数据库表：sys_kms_key_log
 * 记录所有密钥操作的审计日志，包括创建、轮换、检索、禁用等操作。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see SysKmsKey
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_kms_key_log")
public class SysKmsKeyLog extends BaseEntity {

    /** 密钥 ID */
    @TableField("key_id")
    private Long keyId;

    /** 密钥别名 */
    @TableField("key_alias")
    private String keyAlias;

    /** 操作类型：CREATE / ROTATE / RETRIEVE / DISABLE / DELETE */
    @TableField("action")
    private String action;

    /** 操作人 ID */
    @TableField("operator_id")
    private Long operatorId;

    /** 操作人用户名 */
    @TableField("operator_name")
    private String operatorName;

    /** 客户端 IP */
    @TableField("client_ip")
    private String clientIp;

    /** 操作结果：SUCCESS / FAIL */
    @TableField("result")
    private String result;

    /** 错误信息 */
    @TableField("error_message")
    private String errorMessage;

    /** 操作时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("operate_time")
    private LocalDateTime operateTime;
}

