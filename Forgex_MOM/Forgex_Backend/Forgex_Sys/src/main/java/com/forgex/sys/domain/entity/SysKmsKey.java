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
 * 密钥管理实体类。
 * <p>
 * 对应数据库表：sys_kms_key
 * 存储系统中所有加密密钥的元数据和加密后的密钥值。
 * 密钥值使用主密钥（Master Key）加密后存储，确保密钥安全。
 * </p>
 *
 * @author Forgex Team
 * @version 1.0.0
 * @see com.forgex.sys.domain.entity.SysKmsKeyLog
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_kms_key")
public class SysKmsKey extends BaseEntity {

    /** 密钥别名（业务标识） */
    @TableField("key_alias")
    private String keyAlias;

    /** 密钥类型：AES / SM4 / RSA / SM2 */
    @TableField("key_type")
    private String keyType;

    /** 密钥长度（位） */
    @TableField("key_size")
    private Integer keySize;

    /** 加密后的密钥值（Base64编码，使用主密钥加密） */
    @TableField("encrypted_key_value")
    private String encryptedKeyValue;

    /** 密钥版本号 */
    @TableField("key_version")
    private Integer keyVersion;

    /** 密钥状态：ACTIVE / ROTATED / DISABLED */
    @TableField("status")
    private String status;

    /** 过期时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    /** 密钥描述 */
    @TableField("description")
    private String description;
}

