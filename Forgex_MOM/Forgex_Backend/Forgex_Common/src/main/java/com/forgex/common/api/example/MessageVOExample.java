/*Copyright 2026 coder_nai@163.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/
package com.forgex.common.api.example;

import com.forgex.common.api.annotation.AutoFillUsername;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 使用示例：消息 VO
 * <p>演示如何使用 @AutoFillUsername 注解自动填充用户名</p>
 * 
 * @author coder_nai@163.com
 * @date 2026-01-27
 */
@Data
public class MessageVOExample {
    
    /** 消息ID */
    private Long id;
    
    /** 消息标题 */
    private String title;
    
    /** 消息内容 */
    private String content;
    
    /** 发送者用户ID */
    private Long senderId;
    
    /** 发送者用户名（自动填充） */
    @AutoFillUsername(userIdField = "senderId")
    private String senderName;
    
    /** 接收者用户ID */
    private Long receiverId;
    
    /** 接收者用户名（自动填充，必填） */
    @AutoFillUsername(userIdField = "receiverId", required = true)
    private String receiverName;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 创建人ID */
    private Long createBy;
    
    /** 创建人名称（自动填充） */
    @AutoFillUsername(userIdField = "createBy")
    private String createByName;
}

