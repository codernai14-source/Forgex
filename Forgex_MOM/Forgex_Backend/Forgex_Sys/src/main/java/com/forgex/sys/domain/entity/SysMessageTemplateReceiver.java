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
package com.forgex.sys.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.forgex.common.base.BaseEntity;
import lombok.Data;

/**
 * 消息模板接收人表实体
 * <p>
 * 映射表：sys_message_template_receiver
 * 用于配置消息模板的接收人规则
 * </p>
 * 
 * @author Forgex Team
 * @version 1.0.0
 */
@Data
@TableName("sys_message_template_receiver")
public class SysMessageTemplateReceiver extends BaseEntity {
    
    /** 模板主表 ID */
    private Long templateId;
    
    /** 接收类型 (ROLE=角色，DEPT=部门，POSITION=职位，USER=指定人，CUSTOM=自定义) */
    private String receiverType;
    
    /** 接收人 ID 列表 (JSON 数组格式，如 [1,2,3]) */
    private String receiverIds;
}
